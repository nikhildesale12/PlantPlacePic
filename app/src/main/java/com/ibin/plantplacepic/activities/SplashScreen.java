package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.GetAllUploadedDataService;
import com.ibin.plantplacepic.utility.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    TextView versionName;
    DatabaseHelper databaseHelper;
    String uploadedCount;
    String version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        databaseHelper = DatabaseHelper.getDatabaseInstance(SplashScreen.this);
        versionName = (TextView) findViewById(R.id.version_name);
        PackageInfo pInfo = null;
        try {
            pInfo = SplashScreen.this.getPackageManager().getPackageInfo(SplashScreen.this.getPackageName(), 0);
            version = pInfo.versionName;
            versionName.setText("Version : "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Thread init = new Thread() {
            public void run() {
            try {
                sleep(3000);

                final SharedPreferences sharedPreferences = SplashScreen.this.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                final boolean isLogin = sharedPreferences.getBoolean(Constants.KEY_IS_LOGIN, false);
                final String userId = sharedPreferences.getString(Constants.KEY_USERID, "0");

                if(Constants.isNetworkAvailable(SplashScreen.this)) {
                    checkAppVersion(isLogin, userId);
                }else{
                    //Internet not available
                    uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
                    if(userId.equals("0")){
                        Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                        i.putExtra("uploadedCount",uploadedCount);
                        startActivity(i);
                        finish();
                    }else if(!isLogin) {
                        Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                        i.putExtra("uploadedCount",uploadedCount);
                        startActivity(i);
                        finish();
                    }else{
                        Intent i = new Intent(SplashScreen.this, Dashboard.class);
                        i.putExtra("uploadedCount", uploadedCount);
                        startActivity(i);
                        finish();
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        };
        init.start();
    }//end create

    private void checkAppVersion(final boolean isLogin, final String userId) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginResponse> call = service.getAppVersion();
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response != null && response.body() != null){
                    if(response.body().getResult().trim().equals(version)){
                        /*Service to get all records starts*/
                        if(databaseHelper.getTotalALLUploadedData() < 1800){
                            getAllUploadedCount();
                        }
                        /*Service to get all reccord end*/
                        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGOUT, MODE_PRIVATE);
                        boolean logoutUser  = prefs.getBoolean(Constants.KEY_FIRST_TIME_LOGOUT_USER,true);
                        if(logoutUser){
                            SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_LOGOUT, MODE_PRIVATE).edit();
                            editor1.putBoolean(Constants.KEY_FIRST_TIME_LOGOUT_USER, false);
                            editor1.commit();
                            SharedPreferences.Editor editor = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE).edit();
                            editor.clear();
                            editor.commit();
                            Intent intent = new Intent(SplashScreen.this, LoginMainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            getUploadedCount(isLogin,userId);
                        }
                    }else{
                        Constants.dispalyDialogInternet(SplashScreen.this,"Upgrade Application","New update "+response.body().getResult()+" is available , Please update it from playstore",false,false);
                    }
                } else {
                    uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
                    if(userId.equals("0")){
                        Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                        startActivity(i);
                        finish();
                    }else if(!isLogin) {
                        Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        Intent i = new Intent(SplashScreen.this, Dashboard.class);
                        i.putExtra("uploadedCount", uploadedCount);
                        startActivity(i);
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
                if(userId.equals("0")){
                    Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                    startActivity(i);
                    finish();
                }else if(!isLogin) {
                    Intent i = new Intent(SplashScreen.this,LoginMainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Intent i = new Intent(SplashScreen.this, Dashboard.class);
                    i.putExtra("uploadedCount", uploadedCount);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private void getAllUploadedCount() {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .build();
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.getAllUplodCount();
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response != null && response.body() != null && response.body().getSuccess() == 1) {
                        Log.d("Counttt from service : ",response.body().getCount());
                        Log.d("Counttt from database : ",""+databaseHelper.getTotalALLUploadedData());
                        if(!response.body().getCount().trim().equals(""+databaseHelper.getTotalALLUploadedData())){
                            new Thread() {
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intentService = new Intent(SplashScreen.this, GetAllUploadedDataService.class);
                                            startService(intentService);
                                        }
                                    });
                                }
                            }.start();
                        }
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                }
            });
    }

    private void getUploadedCount(final boolean isLogin, final String userId) {
        if (userId.equals("0")) {
            Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
            i.putExtra("uploadedCount", uploadedCount);
            startActivity(i);
            finish();
        } else {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.getUplodCount(userId);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response != null && response.body() != null && response.body().getSuccess() == 1) {
                        if (isLogin) {
                            if (response.body().getCount().trim().equals("0")) {
                                databaseHelper.removeSaveDataFromTable();
                                uploadedCount = "" + databaseHelper.getTotalUploadedData(userId);
                            } else {
                                uploadedCount = response.body().getCount();
                            }
                            Intent i = new Intent(SplashScreen.this, Dashboard.class);
                            i.putExtra("uploadedCount", uploadedCount);
                            startActivity(i);
                            finish();
                        } else {
                            uploadedCount = "" + databaseHelper.getTotalUploadedData(userId);
                            Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    } else {
                        uploadedCount = "" + databaseHelper.getTotalUploadedData(userId);
                        if (userId.equals("0")) {
                            Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
                            startActivity(i);
                            finish();
                        } else if (!isLogin) {
                            Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(SplashScreen.this, Dashboard.class);
                            i.putExtra("uploadedCount", uploadedCount);
                            startActivity(i);
                            finish();
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    uploadedCount = "" + databaseHelper.getTotalUploadedData(userId);
                    if (userId.equals("0")) {
                        Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
                        startActivity(i);
                        finish();
                    } else if (!isLogin) {
                        Intent i = new Intent(SplashScreen.this, LoginMainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(SplashScreen.this, Dashboard.class);
                        i.putExtra("uploadedCount", uploadedCount);
                        startActivity(i);
                        finish();
                    }
                }
            });
        }

//        class SplashTask extends AsyncTask<String, String, String> {
//            ProgressDialog progressDialog;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                // display a progress dialog for good user experiance
//                progressDialog = new ProgressDialog(SplashScreen.this);
//                progressDialog.setMessage("Please Wait");
//                progressDialog.setCancelable(false);
//                progressDialog.show();
//            }
//
//            @Override
//            protected String doInBackground(String... params) {
//                String response = "";
//                String userid = params[0];
//                try {
//                    URL url;
//                    HttpURLConnection urlConnection = null;
//                    try {
//                        String finalUrl = "http://plantplacepicture.com/plantplace/getUploadedCount.php?USERID=" + userid;
//                        url = new URL(finalUrl);
//                        urlConnection = (HttpURLConnection) url.openConnection();
//                        urlConnection.setDoOutput(false);
//                        //urlConnection.setRequestMethod("GET");
//                        urlConnection.setRequestProperty("Accept", "*/*");
//                        urlConnection.setConnectTimeout(60000);
//                        //urlConnection.setRequestProperty("USERID",userid );
//                        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                        InputStream in = null;
//                        int status = urlConnection.getResponseCode();
//                        if (status != HttpURLConnection.HTTP_OK)
//                            in = urlConnection.getErrorStream();
//                        else
//                            in = urlConnection.getInputStream();
//                        InputStreamReader isw = new InputStreamReader(in);
//                        int data = isw.read();
//                        while (data != -1) {
//                            response += (char) data;
//                            data = isw.read();
//                            System.out.print(response);
//                        }
//                        return response;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (urlConnection != null) {
//                            urlConnection.disconnect();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    response = e.getMessage();
//                }
//                return response;
//            }
//
//            @Override
//            protected void onPostExecute(String response) {
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                Log.d("data", response.toString());
//
//            }
//        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed:", "onConnectionFailed:" + connectionResult);
    }
}
