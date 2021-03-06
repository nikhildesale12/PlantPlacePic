package com.ibin.plantplacepic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.SpeciesAroundYouActivity;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetAllUploadedDataService extends Service{
    DatabaseHelper databaseHelper;
    List<Information> informationList;
    public static boolean isRunning = false;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;
    private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<>(15);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue, sThreadFactory);

    public GetAllUploadedDataService() {
        informationList = new ArrayList<>();
        isRunning = true;
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
        if(Constants.isNetworkAvailable(getApplicationContext())){
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
                    .build();
            ApiService service = retrofit.create(ApiService.class);
            Call<InformationResponseBean> call = service.getAllSpeciesDetail();
            call.enqueue(new Callback<InformationResponseBean>() {
                @Override
                public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {
                    informationList = new ArrayList<>();
                    if (response != null && response.body() != null) {
                        if (response.body().getSuccess().toString().trim().equals("1")) {
                            if(response.body().getInformation() != null){
                                //databaseHelper.removeAllSaveDataFromTable();
                                //int count = databaseHelper.getTotalALLUploadedData();
                                new SaveInDbAsync(response).executeOnExecutor(sExecutor);
                            }else{
                                informationList = new ArrayList<>();
                            }
                        } else if (response.body().getSuccess().toString().trim().equals("0")) {
                            Log.d("NO records", "NO records --->");
                            SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
                            SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
                            isRunning = false;
                        } else {
                            Log.d("Technical Error !!!", "Technical Error !!! --->");
                            SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
                            SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
                            isRunning = false;
                        }
                    }else {
                        Log.d("Technical Error !!!", "Technical Error !!! --->");
                        SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
                        SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
                        isRunning = false;
                    }
                }
                @Override
                public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                    Log.d("error", "error :--->"+t.toString());
                    SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
                    SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
                    isRunning = false;
                }
            });
        }else{
            SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
            SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
            isRunning = false;
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }


    private class SaveInDbAsync extends AsyncTask<Void, Integer, String> {
        Response<InformationResponseBean> response = null;
        private SaveInDbAsync(Response<InformationResponseBean> response) {
            this.response = response;
        }
        @Override
        protected String doInBackground(Void... params) {
            for(int i = 0 ;i<response.body().getInformation().size();i++){
                Information information = new Information();
                information.setUserId(response.body().getInformation().get(i).getUserId());
                information.setUserName(response.body().getInformation().get(i).getUserName());
                information.setImages(response.body().getInformation().get(i).getImages());
                information.setSpecies(response.body().getInformation().get(i).getSpecies());
                information.setRemark(response.body().getInformation().get(i).getRemark());
                information.setTag(response.body().getInformation().get(i).getTag());
                information.setStatus(response.body().getInformation().get(i).getStatus());
                information.setTitle(response.body().getInformation().get(i).getTitle());
                if((response.body().getInformation().get(i).getLat().equals("0.0") || response.body().getInformation().get(i).getLat().equals("0"))
                        && (response.body().getInformation().get(i).getLng().equals("0.0") || response.body().getInformation().get(i).getLng().equals("0"))){
                    Geocoder geocode = new Geocoder(getApplicationContext());
                    double latitude = 0.0;
                    double longitude = 0.0;
                    Address address = null;
                    List<Address> addressList = null;
                    String addressStr = response.body().getInformation().get(i).getAddress();
                    try {
                        if (!TextUtils.isEmpty(addressStr)) {
                            addressList = geocode.getFromLocationName(addressStr, 5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (null != addressList && addressList.size() > 0) {
                        address = addressList.get(0);
                    }
                    if (null != address && address.hasLatitude()
                            && address.hasLongitude()) {
                        latitude = address.getLatitude();
                        longitude = address.getLongitude();
                    }
                    information.setLat(String.valueOf(latitude));
                    information.setLng(String.valueOf(longitude));
                }else {
                    information.setLat(response.body().getInformation().get(i).getLat());
                    information.setLng(response.body().getInformation().get(i).getLng());
                }
                String finalAddr = response.body().getInformation().get(i).getAddress().replace(",null","");
                finalAddr = finalAddr.replace(",,","");
                information.setAddress(finalAddr);
                information.setCrop(response.body().getInformation().get(i).getCrop());
                information.setTime(response.body().getInformation().get(i).getTime());
                informationList.add(information);
                long insertedToLater = -1;
                insertedToLater = databaseHelper.insertDataInTableAllInformationToSave(information);
                Log.d("Done", "Inserted ---> "+i+" == "+ insertedToLater);
            }
         return "";
        }
        @Override
        protected void onPostExecute(String result) {
            if(SpeciesAroundYouActivity.getInstance() != null && SpeciesAroundYouActivity.getInstance().animateText != null) {
                SpeciesAroundYouActivity.getInstance().animateText.clearAnimation();
                SpeciesAroundYouActivity.getInstance().animateText.setVisibility(View.INVISIBLE);
            isRunning = false;
            List<Information> mainDataList = databaseHelper.getAllImageUploadedInfo();
            SpeciesAroundYouActivity.getInstance().mMap.setOnInfoWindowClickListener(SpeciesAroundYouActivity.getInstance());
            for(int i = 0 ;i<mainDataList.size();i++){
                if(mainDataList.get(i).getLat() != null && mainDataList.get(i).getLng() != null
                        && !mainDataList.get(i).getLat().equals("0") && !mainDataList.get(i).getLat().equals("0.0")
                        && !mainDataList.get(i).getLng().equals("0") && !mainDataList.get(i).getLng().equals("0.0")
                        && !mainDataList.get(i).getLat().equals("") && !mainDataList.get(i).getLng().equals("")){
                    double latitude = Double.parseDouble(mainDataList.get(i).getLat());
                    double longitude = Double.parseDouble(mainDataList.get(i).getLng());
                    if(mainDataList.get(i).getUserName() != null && mainDataList.get(i).getUserName().trim().length()>0){
                        SpeciesAroundYouActivity.getInstance().mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                                .title(mainDataList.get(i).getSpecies()+"("+mainDataList.get(i).getUserName().trim()+")"))
                                .setTag(mainDataList.get(i).getImages());
                    }else{
                        SpeciesAroundYouActivity.getInstance().mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                                .title(mainDataList.get(i).getSpecies()))
                                .setTag(mainDataList.get(i).getImages());
                    }

                }
              }
            }
        }
    }


}
