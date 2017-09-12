package com.ibin.plantplacepic.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ibin.plantplacepic.bean.LoginResponse;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageUploadService extends Service{
    DatabaseHelper databaseHelper;
    //SubmitRequest submitRequest;
    List<SubmitRequest> submitRequestList;
    //String serverFolderPath="";
   // String filePath = "";
    long totalSize = 0;
    public ImageUploadService() {
        submitRequestList = new ArrayList<>();
    }
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;

    private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<>(15);
//
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
                public Thread newThread(Runnable r) {
                        return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
                    }
    };

    private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue, sThreadFactory);


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
        if(intent != null && intent.getSerializableExtra("submitRequest") != null){
            submitRequestList = (List<SubmitRequest>) intent.getSerializableExtra("submitRequest");
            if(submitRequestList != null){
                if(Constants.isNetworkAvailable(getApplicationContext())){
                    for(int i=0;i<submitRequestList.size();i++){
                        SubmitRequest submitRequest = submitRequestList.get(i);
                        if(submitRequest.getImageUrl() != null && (submitRequest.getStatus() == null || submitRequest.getStatus() != null && submitRequest.getStatus().equals("false"))){
                            submitRequest.setStatus("true");
                            submitRequest.setIsSaveInLocal("NO");
                            /*compress starts*/
                            File file = new File(submitRequest.getImageUrl());
                            if(file != null) {
                                if (file.length() / 1024 > 2048) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    Bitmap bitmap = BitmapFactory.decodeFile(submitRequest.getImageUrl(), options);
                                    Matrix matrix = new Matrix();
                                    //matrix.postRotate(90);
                                    Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                            bitmap.getWidth(), bitmap.getHeight(),
                                            matrix, true);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    try {
                                        //File file1 = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+"compress"+".jpg");
                                        FileOutputStream out = new FileOutputStream(file);
                                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                        out.flush();
                                        out.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                /*compress end*/
                                new UploadFileToServer(submitRequest).executeOnExecutor(sExecutor);
                            }
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Image is not in proper format",Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        SubmitRequest submitRequest = new SubmitRequest();
        private UploadFileToServer(SubmitRequest submitRqt) {
            submitRequest = submitRqt;
        }
        @Override
        protected String doInBackground(Void... params) {
            return uploadFile(submitRequest.getImageUrl());
        }
        @SuppressWarnings("deprecation")
        private String uploadFile(String filePath) {
            String result = "";
            final String boundary = "-------------" + System.currentTimeMillis();
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();

            URLConnection connection = null;
            HttpURLConnection httpConn = null;
            try
            {

                entity.setBoundary(boundary);
                File sourceFile = new File(filePath);
                entity.addPart("image", new FileBody(sourceFile));
                entity.addPart("folderpath",new StringBody(Constants.SERVER_FOLDER_PATH_ALL));

                java.net.URL url = new URL(Constants.FILE_UPLOAD_URL);
                connection = url.openConnection();
                httpConn = (HttpURLConnection) connection;
                httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                httpConn.setConnectTimeout(60000);
                httpConn.setRequestMethod("POST");
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                httpConn.connect();

                OutputStream os = httpConn.getOutputStream();
                entity.build().writeTo(os);
                os.flush();
                os.close();

                int statusCode;
                try {
                    statusCode = httpConn.getResponseCode();
                } catch (EOFException e) {
                    return "";
                }
                InputStreamReader isr;
                if (statusCode != 200 && statusCode != 204 && statusCode != 201) {
                    isr = new InputStreamReader(
                            httpConn.getErrorStream());
                } else {
                    isr = new InputStreamReader(
                            httpConn.getInputStream());
                }
                BufferedReader br = new BufferedReader(isr);
                String line;
                String tempResponse = "";
                // Create a string using response from web services
                while ((line = br.readLine()) != null)
                    tempResponse = tempResponse + line;
                result = tempResponse;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(httpConn != null){
                    httpConn.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("ImageUploadService", "Response from server: " + result);
            if(result.equals("true")){
                callRetrofitToSaveDataServer(submitRequest);
            }else{
                if(submitRequest.getIsSaveInLocal() != null && submitRequest.getIsSaveInLocal().equals("NO")){
                    Log.d("ImageUploadService", "Already in DB");
                }else{
                    saveInLocalForLaterUpload(submitRequest);
                }

            }
            super.onPostExecute(result);
        }
    }

    private void saveInLocalForLaterUpload(SubmitRequest submitRequest){
        submitRequest.setStatus("false");
        if(!databaseHelper.isDataAvialableInLocalDb(submitRequest)){
            long insertedToLater = databaseHelper.insertDataInTableInformation(submitRequest);
            if(insertedToLater != -1){
                Log.d("Done","Inserted In Service  : "+insertedToLater);
            }else{
                Toast.makeText(getApplicationContext(),"Unable to save",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void callRetrofitToSaveDataServer(final SubmitRequest submitRequest){
        Log.d("In callRetrofitToSaveDataServer","In callRetrofitToSaveDataServer");
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        final String USERID = submitRequest.getUserId();
        String IMAGE = submitRequest.getImageName();
        String TIME = submitRequest.getTime();
        String SPECIES = submitRequest.getSpecies();
        String REMARK = submitRequest.getRemark();
        String TAG = submitRequest.getTag();
        String STATUS = submitRequest.getStatus();
        String TITLE= submitRequest.getTitle();
        String LAT = submitRequest.getLatitude();
        String LNG = submitRequest.getLongitude();
        String ADDRESS = submitRequest.getAddress();
        String CROP = submitRequest.getCrop();
        String UPLOAD_FROM = submitRequest.getUploadedFrom();
        Call<LoginResponse> call = service.dataUploadService(USERID,IMAGE,SPECIES,REMARK,TAG,STATUS,TITLE,LAT,LNG,ADDRESS,CROP,TIME,UPLOAD_FROM);
        final String finalIMAGE = IMAGE;
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response != null && response.body() != null ){
                    if(response.body().getSuccess().toString().trim().equals("1")) {
                        Log.d("Result dataUploadService : ","Result :"+response.body().getResult());
                        //delete row if it is in db
                        if(databaseHelper.deleteFromLocal(USERID,finalIMAGE) != 1){
                            Log.d("Deleted from Local","Deleted from Local : "+ finalIMAGE);
                        }
                        //delete image from local
                        if(submitRequest.getImageUrl() != null && submitRequest.getImageUrl().trim().length()>0){
                            File tempFile = new File(submitRequest.getImageUrl());
                            if(tempFile != null){
                                tempFile.delete();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Data upload successfully", Toast.LENGTH_SHORT).show();
                        List<SubmitRequest> dataList = databaseHelper.getImageInfoToUpload(USERID);
                        Log.d("List : ","List : "+dataList.size());
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }else  if(response.body().getSuccess().toString().trim().equals("0")) {
                        Log.d("Result DataUploadService : ","Result :"+response.body().getResult());
                    }else {
                        Log.d("Error dataUploadService ","Error dataUploadService : Technical Error !!!");
                    }
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d("Exception dataUploadService: ","Exception dataUploadService: "+t.toString());
            }
        });
    }
}
