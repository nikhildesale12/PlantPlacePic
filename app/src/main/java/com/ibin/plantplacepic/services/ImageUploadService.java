package com.ibin.plantplacepic.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.AndroidMultiPartEntity;
import com.ibin.plantplacepic.utility.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class ImageUploadService extends Service{
    DatabaseHelper databaseHelper;
    SubmitRequest submitRequest;
    String serverFolderPath="";
    String filePath = "";
    long totalSize = 0;
    public ImageUploadService() {
        submitRequest = new SubmitRequest();
    }
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;

    private static final BlockingQueue<Runnable> sWorkQueue =
            new LinkedBlockingQueue<Runnable>(10);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
                public Thread newThread(Runnable r) {
                        return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
                    }
    };

    private static final ThreadPoolExecutor sExecutor =
            new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue, sThreadFactory);


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
        if(intent != null && intent.getSerializableExtra("submitRequest") != null){
            submitRequest = (SubmitRequest) intent.getSerializableExtra("submitRequest");
            if(submitRequest != null){
                if(submitRequest.getImageUrl() != null && submitRequest.getImageUrl().length()>0){
                    filePath = submitRequest.getImageUrl();
                }
                /*if(submitRequest.getTag() != null){
                    if(submitRequest.getTag().equals(Constants.TAG_TREE)){
                        serverFolderPath = Constants.SERVER_FOLDER_PATH_TREE;
                    }else
                    if(submitRequest.getTag().equals(Constants.TAG_FLOWER)){
                        serverFolderPath = Constants.SERVER_FOLDER_PATH_FLOWER;
                    }else
                    if(submitRequest.getTag().equals(Constants.TAG_FRUIT)){
                        serverFolderPath = Constants.SERVER_FOLDER_PATH_FRUIT;
                    }else
                    if(submitRequest.getTag().equals(Constants.TAG_LEAF)){
                        serverFolderPath = Constants.SERVER_FOLDER_PATH_LEAF;
                    }else{
                        serverFolderPath = Constants.SERVER_FOLDER_PATH_ALL;
                    }
                }else{
                    serverFolderPath = Constants.SERVER_FOLDER_PATH_ALL;
                }*/
                serverFolderPath = Constants.SERVER_FOLDER_PATH_ALL;
            }else{
                submitRequest = new SubmitRequest();
            }
        }
        if(Constants.isNetworkAvailable(getApplicationContext())){
            /*if(submitRequest != null && submitRequest.getImagesPathList()!= null && submitRequest.getImagesPathList().size() >0){
                for(int i=0;i < submitRequest.getImagesPathList().size();i++){
                    String path = submitRequest.getImagesPathList().get(i);
                    ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    String datetime =  exif.getAttribute(ExifInterface.TAG_DATETIME);
                    String fileName = path.substring(path.lastIndexOf('/')+1, path.length());
                    submitRequest.setTime(datetime);
                    submitRequest.setImageUrl(path);
                    submitRequest.setImageName(fileName);
                    new UploadFileToServer(submitRequest,i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }else{
                new UploadFileToServer(submitRequest,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }*/
//           /*compress starts*/
            File file = new File(submitRequest.getImageUrl());
            if(file.length()/1024 > 2048 ) {
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
            new UploadFileToServer(submitRequest,0).executeOnExecutor(sExecutor);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        int position = 0;
        public UploadFileToServer(SubmitRequest submitRqt,int i) {

            filePath =  submitRqt.getImageUrl();
            submitRequest = submitRqt;
            position = i;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
        }
        @Override
        protected String doInBackground(Void... params) {
            //filePath = params[0];
            return uploadFile(filePath);
        }

        @SuppressWarnings("deprecation")
        private String uploadFile(String filePath) {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constants.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                File sourceFile = new File(filePath);
                entity.addPart("image", new FileBody(sourceFile));
                entity.addPart("folderpath",new StringBody(serverFolderPath));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("ImageUploadService", "Response from server: " + result);
            Log.d("ImageUploadService", "Response from server: " + result);
            if(result.equals("true")){
                callRetrofitToSaveDataServer(submitRequest,position);
            }else{
                saveInLocalForLaterUpload(submitRequest);
            }
            super.onPostExecute(result);
        }

    }

    private void saveInLocalForLaterUpload(SubmitRequest submitRequest){
        submitRequest.setStatus("false");
        long insertedToLater = databaseHelper.insertDataInTableInformation(submitRequest);
        if(insertedToLater != -1){
            Log.d("Done","Inserted In Service  : "+insertedToLater);
        }else{
            Toast.makeText(getApplicationContext(),"Unable to save",Toast.LENGTH_SHORT).show();
        }
    }
    private void callRetrofitToSaveDataServer(final SubmitRequest submitRequest, int position){
        Log.d("In callRetrofitToSaveDataServer","In callRetrofitToSaveDataServer");
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        final String USERID = submitRequest.getUserId();
        String path ="";
        String IMAGE = "";
        String TIME = "";
       /* if(submitRequest.getImagesPathList() != null && submitRequest.getImagesPathList().size() >0){
            path = submitRequest.getImagesPathList().get(position);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String datetime =  exif.getAttribute(ExifInterface.TAG_DATETIME);
            String fileName = path.substring(path.lastIndexOf('/')+1, path.length());
            IMAGE = fileName;
            TIME = datetime;
        }else{
            //path = submitRequest.getImageUrl();
            IMAGE = submitRequest.getImageName();
            TIME = submitRequest.getTime();
        }*/

        IMAGE = submitRequest.getImageName();
        TIME = submitRequest.getTime();
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
                        Toast.makeText(getApplicationContext(), "Information upload successfully", Toast.LENGTH_SHORT).show();
                        List<SubmitRequest> dataList = databaseHelper.getImageInfoToUpload(USERID);
                        Log.d("List : ","List : "+dataList.size());
                        stopSelf();
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
