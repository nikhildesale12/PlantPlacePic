package com.ibin.plantplacepic.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetUploadedDataService extends Service{
    DatabaseHelper databaseHelper;
    List<Information> informationList;
    public GetUploadedDataService() {
        informationList = new ArrayList<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
        // Gson gson = new GsonBuilder().setLenient().create();
//        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .readTimeout(60, TimeUnit.SECONDS)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        String userId = prefs.getString(Constants.KEY_USERID, "0");
        Call<InformationResponseBean> call = service.downloadDataById(userId);
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null){
                            boolean isInsert = false;
                            if(response.body().getInformation().size() != databaseHelper.getTotalUploadedData(response.body().getInformation().get(0).getUserId())) {
                                isInsert = true;
                                databaseHelper.removeAllSaveDataFromTable();

                                for(int i = 0 ;i<response.body().getInformation().size();i++){
                                    Information information = new Information();
                                    information.setUserId(response.body().getInformation().get(i).getUserId());
                                    information.setImages(response.body().getInformation().get(i).getImages());
                                    information.setSpecies(response.body().getInformation().get(i).getSpecies());
                                    information.setRemark(response.body().getInformation().get(i).getRemark());
                                    information.setTag(response.body().getInformation().get(i).getTag());
                                    information.setStatus(response.body().getInformation().get(i).getStatus());
                                    information.setTitle(response.body().getInformation().get(i).getTitle());
                                    information.setLat(response.body().getInformation().get(i).getLat());
                                    information.setLng(response.body().getInformation().get(i).getLng());
                                    information.setAddress(response.body().getInformation().get(i).getAddress());
                                    information.setCrop(response.body().getInformation().get(i).getCrop());
                                    information.setTime(response.body().getInformation().get(i).getTime());
                                    informationList.add(information);
                                    long insertedToLater = -1;
                                    if(isInsert) {
                                        insertedToLater = databaseHelper.insertDataInTableInformationToSave(information);
                                    }
                                    if (insertedToLater != -1) {
                                        Log.d("Done", "Inserted ---> "+i +" == "+ insertedToLater);
                                    }
                                }
//                            setupViewPager(viewPager);
//                            tabLayout.setupWithViewPager(viewPager);
//                            setupTabIcons();

                                Intent intent = new Intent();
                                intent.setAction("com.ibin.plantplacepic.CUSTOM_INTENT");
                                Bundle data = new Bundle();
                                data.putParcelableArrayList("reviewList", (ArrayList<? extends Parcelable>) informationList);
                                intent.putExtras(data);
                                sendBroadcast(intent);
                            }
                        }else{
                            informationList = new ArrayList<>();
                        }
                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        //Constants.dispalyDialogInternet(getApplicationContext(),"Result","No Records Found",false,true);
                        Toast.makeText(getApplicationContext(),"No Records Found",Toast.LENGTH_SHORT).show();
                    } else {
                        //Constants.dispalyDialogInternet(getApplicationContext(),"Error","Technical Error !!!",false,true);
                        Toast.makeText(getApplicationContext(),"Technical Error !!!",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //Constants.dispalyDialogInternet(getApplicationContext(),"Error","Technical Error !!!",false,true);
                    Toast.makeText(getApplicationContext(),"Technical Error !!!",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                //Constants.dispalyDialogInternet(getApplicationContext(),"Result",t.toString(),false,true);
                Toast.makeText(getApplicationContext(),t.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }


}
