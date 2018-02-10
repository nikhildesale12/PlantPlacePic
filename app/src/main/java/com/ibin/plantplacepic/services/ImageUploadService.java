package com.ibin.plantplacepic.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.ibin.plantplacepic.activities.Dashboard;
import com.ibin.plantplacepic.bean.Information;
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
import java.io.FileNotFoundException;
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
                        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
                        final String USERID = prefs.getString(Constants.KEY_USERID, "0");
                        if(!USERID.equals("0")){
                            if(submitRequest.getImageUrl() != null && (submitRequest.getStatus() == null || submitRequest.getStatus() != null && submitRequest.getStatus().equals("false"))){
                                submitRequest.setStatus("true");
                                submitRequest.setIsSaveInLocal("NO");
                            /*compress starts*/
                                File file = new File(submitRequest.getImageUrl());
                                if(file != null) {
                                    compressImage(submitRequest);
//                                    BitmapFactory.Options options = new BitmapFactory.Options();
//                                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                                    if(submitRequest.getImageUrl() != null){
//                                        Bitmap bitmap = BitmapFactory.decodeFile(submitRequest.getImageUrl(), options);
//                                        if(bitmap != null) {
//                                            compressImageOld(bitmap,file);
//                                         /*compress end*/
//                                            new UploadFileToServer(submitRequest).executeOnExecutor(sExecutor);
//                                        }
//                                    }else{
//                                        stopSelf();
//                                        Toast.makeText(getApplicationContext(),"Error while uploading image please try again",Toast.LENGTH_SHORT).show();
//                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Image not available",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            stopSelf();
                            Toast.makeText(getApplicationContext(),"Error while uploading data, please login again and upload",Toast.LENGTH_SHORT).show();
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


    private void compressImageOld(Bitmap bitmap,File file){
        Matrix matrix = new Matrix();
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

    /*Image Compression new starts*/
    public void compressImage(SubmitRequest submitRequest) {
        String imagePath = getRealPathFromURI(submitRequest.getImageUrl());
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
//      max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = 0;
        float maxRatio = 0;
        if(actualHeight != 0){
            imgRatio = actualWidth / actualHeight;
        }
        if(maxHeight != 0){
            maxRatio = maxWidth / maxHeight;
        }
//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        //String filename = getFilename();
        File outFile = new File(imagePath);
        if(outFile != null && outFile.exists()){
            outFile.delete();
        }
        try {
            out = new FileOutputStream(outFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            /*Uploading image to server*/
            new UploadFileToServer(submitRequest).executeOnExecutor(sExecutor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    /*Image Compression end*/

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        boolean alreadySave = false;
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
                httpConn.setConnectTimeout(50000);
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
                saveInLocalForLaterUpload(submitRequest);
                alreadySave = true;
                e.printStackTrace();
            }
            catch (IOException e)
            {
                alreadySave = true;
                saveInLocalForLaterUpload(submitRequest);
                showErrorToast();
                if(e.toString().contains("failed to connect to plantplacepicture.com")){
                    result = "failed to connect to plantplacepicture.com";
                }
                e.printStackTrace();
            }
            catch (Exception e)
            {
                alreadySave = true;
                saveInLocalForLaterUpload(submitRequest);
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
//                if(submitRequest.getIsSaveInLocal() != null && !submitRequest.getIsSaveInLocal().equals("NO")){
//                    Log.d("ImageUploadService", "Already in DB");
//                }else{
                    if(!alreadySave){
                        saveInLocalForLaterUpload(submitRequest);
                    }
//                }
            }
            super.onPostExecute(result);
        }
    }

    private void showErrorToast() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Failed to connect to ibin.plantplacepicture.com , Data will upload automatically later",Toast.LENGTH_LONG).show();
            }
        });
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
        //final String USERID = submitRequest.getUserId();
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
        final String USERID = prefs.getString(Constants.KEY_USERID, "0");
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
        String MOUNTING_BOARD = submitRequest.getMountingBoard();
        Call<LoginResponse> call = service.dataUploadService(USERID,IMAGE,SPECIES,REMARK,TAG,STATUS,TITLE,LAT,LNG,ADDRESS,CROP,TIME,UPLOAD_FROM,MOUNTING_BOARD);
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
                        /*Save uploaded into local too starts*/
                        Information information = new Information();
                        information.setUserId(USERID);
                        if(submitRequest.getImageName() != null){
                            information.setImages(submitRequest.getImageName());
                        }
                        if(submitRequest.getSpecies() != null){
                            information.setSpecies(submitRequest.getSpecies());
                        }
                        if(submitRequest.getRemark() != null){
                            information.setRemark(submitRequest.getRemark());
                        }
                        if(submitRequest.getTag() != null){
                            information.setTag(submitRequest.getTag());
                        }
                        if(submitRequest.getStatus() != null){
                            information.setStatus(submitRequest.getStatus());
                        }
                        if(submitRequest.getTitle() != null){
                            information.setTitle(submitRequest.getTitle());
                        }
                        if(submitRequest.getLatitude() != null){
                            information.setLat(submitRequest.getLatitude());
                        }
                        if(submitRequest.getLongitude() != null){
                            information.setLng(submitRequest.getLongitude());
                        }
                        if(submitRequest.getAddress() != null){
                            information.setAddress(submitRequest.getAddress());
                        }
                        if(submitRequest.getCrop() != null){
                            information.setCrop(submitRequest.getCrop());
                        }
                        if(submitRequest.getTime() != null){
                            information.setTime(submitRequest.getTime());
                        }
                        if(submitRequest.getUpdateInfo() != null){
                            information.setUpdateinfo(submitRequest.getUpdateInfo());
                        }
                        if(submitRequest.getUploadedFrom() != null){
                            information.setUploadFrom(submitRequest.getUploadedFrom());
                        }
                        if(submitRequest.getMountingBoard() != null){
                            information.setMountingBoard(submitRequest.getMountingBoard());
                        }

                        databaseHelper.insertDataInTableAllInformationToSave(information);
                        databaseHelper.insertDataInTableInformationToSave(information);
                        /*save uploaded into locaal end*/
                        try {
                            int updatedCount = Integer.parseInt(Dashboard.getInstance().textUploadCount.getText().toString()) + 1;
                            Dashboard.getInstance().textUploadCount.setText("" + updatedCount);
                            Toast.makeText(getApplicationContext(), "Data upload successfully", Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        //List<SubmitRequest> dataList = databaseHelper.getImageInfoToUpload(USERID);
                        //Log.d("List : ","List : "+dataList.size());
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
