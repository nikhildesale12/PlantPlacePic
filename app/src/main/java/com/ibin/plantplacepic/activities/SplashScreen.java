package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;

import org.json.JSONException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashScreen extends AppCompatActivity {
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
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        };
        init.start();

        final SharedPreferences sharedPreferences = SplashScreen.this.getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        final boolean login = sharedPreferences.getBoolean(Constants.KEY_IS_LOGIN, false);
        final String userId = sharedPreferences.getString(Constants.KEY_USERID, "0");
        if(Constants.isNetworkAvailable(SplashScreen.this)){
            SplashTask myAsyncTasks = new SplashTask();
            myAsyncTasks.execute(userId);
                            //final Gson gson = new GsonBuilder().setLenient().create();
//                            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                                    .readTimeout(15, TimeUnit.SECONDS)
//                                    .connectTimeout(15, TimeUnit.SECONDS)
//                                    .build();
//                            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
////                                    .addConverterFactory(GsonConverterFactory.create(gson))
//                                    .addConverterFactory(GsonConverterFactory.create())
//                                    .client(okHttpClient)
//                                    .build();
//                            ApiService service = retrofit.create(ApiService.class);
//                            Call<LoginResponse> call = service.getUplodCount(userId);
//                            call.enqueue(new Callback<LoginResponse>() {
//                                @Override
//                                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                                            if(response != null && response.body() != null && response.body().getSuccess() == 1){
//                                                if(response.body().getResult().trim().equals(version)){
//                                                    if (login) {
//                                                        if(response.body().getCount().trim().equals("0")){
//                                                            databaseHelper.removeAllSaveDataFromTable();
//                                                        }
//                                                        uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
//                                                        if(userId.equals("0")){
//                                                            Intent i = new Intent(SplashScreen.this,SignInActivity.class);
//                                                            i.putExtra("uploadedCount",uploadedCount);
//                                                            startActivity(i);
//                                                            finish();
//                                                        }else {
//                                                            uploadedCount = response.body().getCount();
//                                                            Intent i = new Intent(SplashScreen.this, Dashboard.class);
//                                                            i.putExtra("uploadedCount", uploadedCount);
//                                                            startActivity(i);
//                                                            finish();
//                                                        }
//                                                    }else{
//                                                        uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
//                                                        if(userId.equals("0")){
//                                                            Intent i = new Intent(SplashScreen.this,SignInActivity.class);
//                                                            i.putExtra("uploadedCount",uploadedCount);
//                                                            startActivity(i);
//                                                            finish();
//                                                        }else {
//                                                            Intent i = new Intent(SplashScreen.this, Dashboard.class);
//                                                            i.putExtra("uploadedCount", uploadedCount);
//                                                            startActivity(i);
//                                                            finish();
//                                                        }
//                                                    }
//                                                }else{
//                                                    Constants.dispalyDialogInternet(SplashScreen.this,"Upgrade Application","New update "+response.body().getResult()+" is available , Please update it from playstore",false,false);
//                                                }
//                                    } else {
//                                        uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
//                                        Intent i = new Intent(SplashScreen.this,SignInActivity.class);
//                                        i.putExtra("uploadedCount",uploadedCount);
//                                        startActivity(i);
//                                        finish();
//                                    }
//                                }
//                                @Override
//                                public void onFailure(Call<LoginResponse> call, Throwable t) {
//                                    uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
//                                    if(userId.equals("0")){
//                                        Intent i = new Intent(SplashScreen.this,SignInActivity.class);
//                                        i.putExtra("uploadedCount",uploadedCount);
//                                        startActivity(i);
//                                        finish();
//                                    }else {
//                                        Intent i = new Intent(SplashScreen.this, Dashboard.class);
//                                        i.putExtra("uploadedCount", uploadedCount);
//                                        startActivity(i);
//                                        finish();
//                                    }
//                                }
//                            });
        }else{
            //Internet not available
            uploadedCount = ""+databaseHelper.getTotalUploadedData(userId);
            if(userId.equals("0")){
                Intent i = new Intent(SplashScreen.this,SignInActivity.class);
                i.putExtra("uploadedCount",uploadedCount);
                startActivity(i);
                finish();
            }else {
                Intent i = new Intent(SplashScreen.this, Dashboard.class);
                i.putExtra("uploadedCount", uploadedCount);
                startActivity(i);
                finish();
            }
        }

    }//end create

    public class SplashTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog for good user experiance
            progressDialog = new ProgressDialog(SplashScreen.this);
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            String userid=params[0];
            try {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL("http://ibin.plantplacepicture.com/plantplace/getUploadCount.php");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(60000);
                    urlConnection.setRequestProperty("USERID",userid );
                    urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader isw = new InputStreamReader(in);
                    int data = isw.read();
                    while (data != -1) {
                        response += (char) data;
                        data = isw.read();
                        System.out.print(response);
                    }
                    return response;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                response =  e.getMessage();
            }
            return response;
        }
        @Override
        protected void onPostExecute(String response) {
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            Log.d("data", response.toString());

        }
    }
}
