package com.ibin.plantplacepic.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.BuildConfig;
import com.ibin.plantplacepic.HelpActivity;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.LoginResponse;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.bean.UserDetailResponseBean;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.ImageUploadService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Dashboard extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    RelativeLayout buttonReviewMyUploads;
    RelativeLayout buttonGallery;
    RelativeLayout buttonCamera;
    RelativeLayout buttonSearch;
    TextView textUploadCount;
    String currentDateTimeString;
    GPSTracker gps;
    TextView textUserName ;
    private File mFileTemp;
    private String userName ;
    private String userId ;
    private String emailId = "";
    private String personPhotoUrl;
    double latitude =0;
    double longitude =0;
    String uploadedCount="";
    Toolbar topToolBar;
    private String finalAddress = "";
    DatabaseHelper databaseHelper;
    //private ImageView profilePic;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initViews();
        //textUploadCount.setVisibility(View.INVISIBLE);
        if (getIntent() != null && getIntent().getBooleanExtra("EXIT", false)) {
            Dashboard.this.finish();
            System.exit(1);
        }else {
            topToolBar = (Toolbar)findViewById(R.id.toolbarDashboard);

            SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE);
            emailId  = prefs.getString(Constants.KEY_USERNAME, "Guest");
            userId = prefs.getString(Constants.KEY_USERID, "0");
            personPhotoUrl = prefs.getString(Constants.KEY_PHOTO, "");

            SharedPreferences prefs1 = getSharedPreferences(Constants.MY_PREFS_USER_INFO, MODE_PRIVATE);
            userName  = prefs1.getString(Constants.KEY_FIRSTNAME, "") + " "
                        +prefs1.getString(Constants.KEY_MIDDLENAME, "")  + " "
                        + prefs1.getString(Constants.KEY_LASTNAME, "");

            textUploadCount.setVisibility(View.VISIBLE);
            textUploadCount.setText(""+databaseHelper.getTotalUploadedData(userId));
            if(getIntent() != null && getIntent().getStringExtra("uploadedCount") != null){
                textUploadCount.setVisibility(View.VISIBLE);
                 uploadedCount =  getIntent().getStringExtra("uploadedCount");
                 if(uploadedCount.equals(Constants.FROM_)){
                     getUploadedCount(userId);
                 }else {
                     textUploadCount.setText(uploadedCount);
                 }
            }

//            File file = new File(Constants.FOLDER_PATH + File.separator + Constants.USER_PHOTO);
//            if(file != null && file.exists()){
//                // imageView.setImageURI(Uri.fromFile(file));
////                BitmapFactory.Options options = new BitmapFactory.Options();
////                options.inSampleSize = 2;
////                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
////                BitmapDrawable icon = new BitmapDrawable(topToolBar.getResources(), bitmap);
////                topToolBar.setNavigationIcon(icon);
//
//                Target target = new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                        Log.d("onBitmapLoaded", "onBitmapLoaded");
//                        Bitmap b = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
//                        BitmapDrawable icon = new BitmapDrawable(topToolBar.getResources(), b);
//                        topToolBar.setNavigationIcon(icon);
//                    }
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//                        Log.d("onBitmapFailed", "onBitmapFailed");
//                        topToolBar.setNavigationIcon(R.mipmap.userpic);
//                    }
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//                        Log.d("onPrepareLoad", "onPrepareLoad");
//                        topToolBar.setNavigationIcon(R.mipmap.userpic);
//                    }
//                };
//
//                Picasso.with(topToolBar.getContext())
//                        .load(file)
//                        .placeholder(R.mipmap.userpic)
//                        .error(R.mipmap.userpic)
//                        .into(target);
//            }else{
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Log.d("onBitmapLoaded", "onBitmapLoaded");
                        Bitmap b = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
                        BitmapDrawable icon = new BitmapDrawable(topToolBar.getResources(), b);
                        topToolBar.setNavigationIcon(icon);
                    }
                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        Log.d("onBitmapFailed", "onBitmapFailed");
                        topToolBar.setNavigationIcon(R.mipmap.userpic);
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Log.d("onPrepareLoad", "onPrepareLoad");
                        topToolBar.setNavigationIcon(R.mipmap.userpic);
                    }
                };
                if(personPhotoUrl != null && personPhotoUrl.length()>0){
                    Picasso.with(topToolBar.getContext())
                            .load(personPhotoUrl)
                            .placeholder(R.mipmap.userpic)
                            .error(R.mipmap.userpic)
                            .into(target);
                }else{
                    Picasso.with(topToolBar.getContext())
                            .load(R.mipmap.userpic)
                            .placeholder(R.mipmap.userpic)
                            .error(R.mipmap.userpic)
                            .into(target);
                }
//            }
            //getUploadedCount(userId);

           // textUserName.setText("Welcome "+userName+" !");
            setSupportActionBar(topToolBar);
            if(userName.length()>0){
                getSupportActionBar().setTitle(userName);
            }else{
                getSupportActionBar().setTitle(prefs.getString(Constants.KEY_USERNAME, "Guest"));
            }
            topToolBar.setNavigationIcon(R.mipmap.userpic);

            buttonGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setType("image*//*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);*/
                    if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23){
                        if(checkPermission()){
                            if(!isGPSEnabled()){
                                showSettingsAlert();
                            }else{
                                Intent photoPickerIntent = new Intent(Dashboard.this,CustomGallery.class);
                                startActivity(photoPickerIntent);
                                finish();
                            }
                        }else {
                            requestPermission();
                        }
                    }else{
                        if(!isGPSEnabled()){
                            showSettingsAlert();
                        }else{
                            Intent photoPickerIntent = new Intent(Dashboard.this,CustomGallery.class);
                            startActivity(photoPickerIntent);
                            finish();
                        }
                    }
                }
            });

            buttonCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23 ){
                        if(checkPermission()){
                            if(!isGPSEnabled()){
                                showSettingsAlert();
                            }else{
                                openCamera();
                            }
                        }else {
                            requestPermission();
                        }
                    }else{
                        if(!isGPSEnabled()){
                            showSettingsAlert();
                        }else{
                            openCamera();
                        }
                    }
                }
            });
            buttonReviewMyUploads.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(Dashboard.this,ReviewMyUploadTabActivity.class);
                    startActivity(intent1);
                    finish();
//                    if(Constants.isNetworkAvailable(Dashboard.this)){
//
//                    }else{
//                        Constants.dispalyDialogInternet(Dashboard.this,"Internet Unavailable","Please check internet connection",false,false);
//                    }
                }
            });

            buttonSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(Dashboard.this, SpeciesSearchActivity.class);
                    startActivity(intent1);
                    finish();
//                    if(Constants.isNetworkAvailable(Dashboard.this)){
//
//                    }else{
//                        Constants.dispalyDialogInternet(Dashboard.this,"Internet Unavailable","Please check internet connection",false,false);
//                    }
                }
            });

            if(Constants.isNetworkAvailable(Dashboard.this)){
                List<SubmitRequest> dataList = databaseHelper.getImageInfoToUpload(userId);
                Log.d("List : ","List : "+dataList.size());
                if(dataList!=null && dataList.size()>0){
                    if(userId.equals("0")){
                        Constants.dispalyDialogInternet(Dashboard.this,"Invalid User","Please login again to upload information",false,false);
                    }else{
                        Intent intentService = new Intent(Dashboard.this, ImageUploadService.class);
                        intentService.putExtra("submitRequest", (Serializable) dataList);
                        startService(intentService);
                    }
                   /* for(int i=0;i<dataList.size();i++){
                        SubmitRequest  submitRequest = new SubmitRequest();
                        submitRequest.setUserId(dataList.get(i).getUserId());
                        submitRequest.setRemark(dataList.get(i).getRemark());
                        submitRequest.setSpecies(dataList.get(i).getSpecies());
                        submitRequest.setAddress(dataList.get(i).getAddress());
                        submitRequest.setCrop(dataList.get(i).getCrop());
                        submitRequest.setImageName(dataList.get(i).getImageName());
                        submitRequest.setImagesPathList(dataList.get(i).getImagesPathList());
                        submitRequest.setImageUrl(dataList.get(i).getImageUrl());
                        submitRequest.setLatitude(dataList.get(i).getLatitude());
                        submitRequest.setLongitude(dataList.get(i).getLongitude());
                        submitRequest.setTitle(dataList.get(i).getTitle());
                        submitRequest.setTag(dataList.get(i).getTag());
                        submitRequest.setStatus(dataList.get(i).getStatus());
                        submitRequest.setTime(dataList.get(i).getTime());
                        submitRequest.setIsSaveInLocal("NO");
                    }
                    if(submitRequest.getImageUrl() != null && (submitRequest.getStatus() == null || submitRequest.getStatus() != null && submitRequest.getStatus().equals("false"))){

                    }*/
                }
            }
        }
    } /*onCreate End*/


   /* private class StarServiceInAsync extends AsyncTask<Void, Integer, String> {
        SubmitRequest submitRequest = new SubmitRequest();
        private StarServiceInAsync(SubmitRequest submitRqt) {
            submitRequest = submitRqt;
        }
        @Override
        protected String doInBackground(Void... params) {
            Intent intentService = new Intent(Dashboard.this, ImageUploadService.class);
            intentService.putExtra("submitRequest",submitRequest);
            startService(intentService);
            return null;
        }

    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logoutmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.MY_PREFS_LOGIN, MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                        }
            });
            Intent intent = new Intent(Dashboard.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        if (id == R.id.action_feedback) {
            //Toast.makeText(Dashboard.this, "feeback", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Dashboard.this, FeedbackActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_share) {
            //Toast.makeText(Dashboard.this, "share", Toast.LENGTH_SHORT).show();
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.ibin.plantplacepic&hl=en \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch(Exception e) {
                //e.toString();
            }
        }
        if (id == R.id.action_about) {
            //Toast.makeText(Dashboard.this, "about", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Dashboard.this, AboutActivity.class);
            intent.putExtra("from","dashboard");
            startActivity(intent);
        }

        if (id == R.id.action_help) {
//            Intent intent = new Intent(Dashboard.this, HelpActivity.class);
//            intent.putExtra("from","dashboard");
//            startActivity(intent);
            File file = new File(Constants.FOLDER_PATH + File.separator + "PlantPlacePicture.pdf");
            if (file != null && file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        }


        if (id == R.id.action_profile) {
            //getProfileInfo and upate
           getProfileInfo();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getProfileInfo() {
        final ProgressDialog dialog = new ProgressDialog(Dashboard.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<UserDetailResponseBean> call = service.getUserProfile(userId);
        call.enqueue(new Callback<UserDetailResponseBean>() {
            @Override
            public void onResponse(Call<UserDetailResponseBean> call, Response<UserDetailResponseBean> response) {
                if (response != null && response.body() != null) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        openProfilePopUp(response);
                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(Dashboard.this,"Result",response.body().getResult(),true,false);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(Dashboard.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(Dashboard.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<UserDetailResponseBean> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(Dashboard.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void openProfilePopUp(Response<UserDetailResponseBean> response) {
        {
            final Dialog userDetailPopup = new Dialog(Dashboard.this);
            userDetailPopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
            userDetailPopup.setContentView(R.layout.popup_update_user_detail);
            userDetailPopup.setCanceledOnTouchOutside(false);
            final EditText editFirstName = (EditText) userDetailPopup.findViewById(R.id.editFirstNameUpdate);
            final EditText editMiddleName = (EditText) userDetailPopup.findViewById(R.id.editMiddleNameUpdate);
            final EditText editLastName = (EditText) userDetailPopup.findViewById(R.id.editLastNameUpdate);
            final EditText editOccupation = (EditText) userDetailPopup.findViewById(R.id.editOccupationUpdate);
            final EditText editMobile = (EditText) userDetailPopup.findViewById(R.id.editMobileUpdate);
            editFirstName.setText(response.body().getFirstName());
            editMiddleName.setText(response.body().getMiddleName());
            editLastName.setText(response.body().getLastName());
            editOccupation.setText(response.body().getOccupation());
            editMobile.setText(response.body().getMobile());
            Button submitBtton = (Button) userDetailPopup.findViewById(R.id.submitUserInfoUpdate);
            submitBtton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editFirstName.getText().toString().length() == 0 ){
                        editFirstName.requestFocus();
                        editFirstName.setError("Please Enter First Name");
                    }else if(editLastName.getText().toString().length() == 0 ){
                        editLastName.requestFocus();
                        editLastName.setError("Please Enter Last Name");
                    }else if(editOccupation.getText().toString().length() == 0 ){
                        editOccupation.requestFocus();
                        editOccupation.setError("Please Enter Occupation");
                    }else{
                        userDetailPopup.dismiss();
                        updateUserInformation(userId,editFirstName.getText().toString(),editMiddleName.getText().toString(),editLastName.getText().toString(),
                                editOccupation.getText().toString(),editMobile.getText().toString());
                    }
                }
            });
            Button cancleButton = (Button) userDetailPopup.findViewById(R.id.cancle_user_profile);
            cancleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userDetailPopup.dismiss();
                }
            });
            userDetailPopup.show();
        }
    }


    private void updateUserInformation(String userId,String firstName,String middleName,String LastName, String occupation,String mobileNum){
        //update Info
        final ProgressDialog dialog = new ProgressDialog(Dashboard.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<UserDetailResponseBean> call = service.updateUserDetail(userId,firstName, middleName,LastName,occupation,mobileNum);
        call.enqueue(new Callback<UserDetailResponseBean>() {
            @Override
            public void onResponse(Call<UserDetailResponseBean> call, Response<UserDetailResponseBean> response) {
                if (response != null && response.body() != null) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.MY_PREFS_USER_INFO, MODE_PRIVATE).edit();
                        editor1.putString(Constants.KEY_FIRSTNAME, response.body().getFirstName());
                        editor1.putString(Constants.KEY_MIDDLENAME, response.body().getMiddleName());
                        editor1.putString(Constants.KEY_LASTNAME, response.body().getLastName());
                        editor1.putString(Constants.KEY_OCCUPATION, response.body().getOccupation());
                        editor1.putString(Constants.KEY_MOBILE, response.body().getMobile());
                        editor1.commit();
                        Toast.makeText(Dashboard.this,"Updated Success",Toast.LENGTH_SHORT).show();

                    } else if (response.body().getSuccess().toString().trim().equals("0")) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(Dashboard.this,"Result",response.body().getResult(),true,false);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Constants.dispalyDialogInternet(Dashboard.this,"Error","Technical Error !!!",false,false);
                    }
                }else{
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Constants.dispalyDialogInternet(Dashboard.this,"Error","Technical Error !!!",false,false);
                }
            }
            @Override
            public void onFailure(Call<UserDetailResponseBean> call, Throwable t) {
                //Log.d("resp
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Constants.dispalyDialogInternet(Dashboard.this,"Result",t.toString(),false,false);
            }
        });
    }

    private void getUploadedCount(final String userId) {
        if(Constants.isNetworkAvailable(Dashboard.this)){
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
            ApiService service = retrofit.create(ApiService.class);
            Call<LoginResponse> call = service.getUplodCount(userId);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response != null && response.body() != null ){
                        if(response != null && response.body() != null && response.body().getSuccess().equals(1) ){
                            //databaseHelper.removeAllSaveDataFromTable();
                            textUploadCount.setVisibility(View.VISIBLE);
                            textUploadCount.setText(response.body().getCount());
                        }
                    }else{
                        textUploadCount.setVisibility(View.VISIBLE);
                        textUploadCount.setText(""+databaseHelper.getTotalUploadedData(userId));
                    }
                }
                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    //textUploadCount.setText("0");
                    textUploadCount.setVisibility(View.VISIBLE);
                    textUploadCount.setText(""+databaseHelper.getTotalUploadedData(userId));
                }
            });
        }else{
            //no internet
            textUploadCount.setText(""+databaseHelper.getTotalUploadedData(userId));
        }
    }

    private void openCamera(){
        currentDateTimeString =new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date());
//        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        currentDateTimeString = currentDateTimeString.replace(" ","_");
        currentDateTimeString = currentDateTimeString.replace(":","-");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String state = Environment.getExternalStorageState();
        File rootDirectory = new File(Constants.FOLDER_PATH);
        if (!rootDirectory.exists()) {
            rootDirectory.mkdir();
        }
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+currentDateTimeString+"_"+userId+".jpeg");
        }
        else {
            mFileTemp = new File(getFilesDir(), Constants.IMAGE_NAME+currentDateTimeString+"_"+userId+".jpeg");
        }
        /*Uri.fromFile(mFileTemp)*/
        Uri photoURI = null;
        if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23 ){
            photoURI = FileProvider.getUriForFile(Dashboard.this, BuildConfig.APPLICATION_ID+ ".provider", mFileTemp);
        }else {
            photoURI = Uri.fromFile(mFileTemp);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent,Constants.RESULT_OPEN_CAMERA);
    }


    public List<Address> getAddress(double lat ,double lng) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (lat != 0 || lng != 0) {
                addresses = geocoder.getFromLocation(lat ,lng , 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                System.out.println(address+" - "+city+" - "+country);

                return addresses;

            } else {
//                Toast.makeText(this, "latitude and longitude are null",
//                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode)
        {
            case Constants.RESULT_OPEN_CAMERA:
                if(resultCode== Activity.RESULT_OK) {
                    /*ProgressDialog dialog = new ProgressDialog(Dashboard.this);
                    dialog.setMessage("Please Wait...");
                    dialog.setIndeterminate(false);
                    dialog.setCancelable(false);
                    dialog.show();*/
                   File file = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+currentDateTimeString+"_"+userId+".jpeg");
                   try {
                       if(file != null){
                           gps = new GPSTracker(Dashboard.this);
                           if(gps.canGetLocation()){
                               latitude = gps.getLatitude();
                               longitude = gps.getLongitude();
                               List<Address> addresses =  getAddress(latitude,longitude);
                               String address="",city="",country="";
                               if(addresses != null){
                                   address = addresses.get(0).getAddressLine(0);
                                   city = addresses.get(0).getAddressLine(1);
                                   country = addresses.get(0).getAddressLine(2);
                               }
                               finalAddress = address+","+city+","+country;
                           }

//                           if(file.length()/1024 > 2048 ) {
//                               //compress Image
//                               BitmapFactory.Options options = new BitmapFactory.Options();
//                               options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                               Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PATH + File.separator + Constants.IMAGE_NAME + currentDateTimeString +"_"+userId+ ".jpeg", options);
//                               Matrix matrix = new Matrix();
//                               //matrix.postRotate(90);
//                               Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                                       bitmap.getWidth(), bitmap.getHeight(),
//                                       matrix, true);
//                               if (file.exists()) {
//                                   file.delete();
//                               }
//                               try {
//                                   //File file1 = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+"compress"+".jpg");
//                                   FileOutputStream out = new FileOutputStream(file);
//                                   finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
//                                   out.flush();
//                                   out.close();
//                                   Constants.countSelectedPhotoFromGallery =1 ;
//                                   Intent intentData = new Intent(this, ImageInfoActivity.class);
//                                   intentData.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                   /*photoURI.getPath()*/
//                                   //Uri photoURI = FileProvider.getUriForFile(Dashboard.this, BuildConfig.APPLICATION_ID+ ".provider", file);
//                               /*    intentData.putExtra("imageUri",Uri.fromFile(file).getPath());
//                                   intentData.putExtra("imageName", Constants.IMAGE_NAME + currentDateTimeString + ".jpeg");
//                                   intentData.putExtra("finalAddress", finalAddress);
//                                   intentData.putExtra("latitude", latitude);
//                                   intentData.putExtra("longitude", longitude);
//                                   intentData.putExtra("currentDateTimeString", currentDateTimeString);*/
//                                   SubmitRequest sr = new SubmitRequest();
//                                   sr.setImageUrl(Uri.fromFile(file).getPath());
//                                   sr.setImageName(Constants.IMAGE_NAME + currentDateTimeString + ".jpeg");
//                                   sr.setAddress(finalAddress);
//                                   sr.setLatitude(String.valueOf(latitude));
//                                   sr.setLongitude(String.valueOf(longitude));
//                                   sr.setTime(currentDateTimeString);
//                                   intentData.putExtra("submitRequest",sr);
//                                   startActivity(intentData);
//                                   finish();
//                               } catch (Exception e) {
//                                   e.printStackTrace();
//                               }
//                           }else{
                               Log.e("file.length() : ","file.length()  : "+file.length());
                               Constants.countSelectedPhotoFromGallery=1;
                               Intent intentData = new Intent(this,ImageInfoActivity.class);
                               if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23 ) {
                                   intentData.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                               }
                               /*photoURI.getPath()*/
                               //Uri photoURI = FileProvider.getUriForFile(Dashboard.this, BuildConfig.APPLICATION_ID+ ".provider", file);
                              /* intentData.putExtra("imageUri", Uri.fromFile(file).getPath());
                               intentData.putExtra("imageName",Constants.IMAGE_NAME+currentDateTimeString+".jpeg");
                               intentData.putExtra("finalAddress",finalAddress);
                               intentData.putExtra("latitude",latitude);
                               intentData.putExtra("longitude",longitude);
                               intentData.putExtra("currentDateTimeString",currentDateTimeString);*/
                               SubmitRequest sr = new SubmitRequest();
                               sr.setImageUrl(Uri.fromFile(file).getPath());
                               sr.setImageName(Constants.IMAGE_NAME + currentDateTimeString +"_"+userId+ ".jpeg");
                               sr.setAddress(finalAddress);
                               sr.setLatitude(String.valueOf(latitude));
                               sr.setLongitude(String.valueOf(longitude));
                               sr.setTime(currentDateTimeString);
                               sr.setUploadedFrom(Constants.UPLOAD_FROM_CAMERA);
                               intentData.putExtra("submitRequest",sr);
                               startActivity(intentData);
                               finish();
                           }
    //                   }
                   }
                   catch(ActivityNotFoundException aNFE){
                       Toast.makeText(this, "Sorry - your device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
                   }
                } else if(resultCode==Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Image not capture", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        showCloseAppPopUp();
    }

    private void initViews() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //profilePic = (ImageView) findViewById(R.id.profilePic);
        buttonCamera = (RelativeLayout)findViewById(R.id.buttonCamera);
        buttonReviewMyUploads = (RelativeLayout)findViewById(R.id.buttonReviewMyUploads);
        buttonGallery = (RelativeLayout)findViewById(R.id.buttonGallery);
        buttonSearch = (RelativeLayout)findViewById(R.id.buttonSearchMain);
        textUserName = (TextView) findViewById(R.id.textUserName);
        textUploadCount = (TextView)findViewById(R.id.text_uploaded_count);
        databaseHelper = DatabaseHelper.getDatabaseInstance(Dashboard.this);
    }
    private void showCloseAppPopUp(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Dashboard.this);
        alertDialog.setTitle("Alert !");
        alertDialog.setMessage("Do you want to close application?");
        alertDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                //finish();
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Dashboard.this);
        alertDialog.setTitle("GPS Is Disable");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    protected boolean isGPSEnabled(){
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean GPSStatus = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GPSStatus;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(Dashboard.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        INTERNET,
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE
                }, Constants.REQUESTPERMISSIONCODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUESTPERMISSIONCODE:
                if (grantResults.length > 0) {
                    boolean AccessFineLocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessCoarseLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean InternetPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteInternalStoragePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadInternalStoragePermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    if (AccessFineLocationPermission && AccessCoarseLocPermission && InternetPermission && WriteInternalStoragePermission && ReadInternalStoragePermission) {
                    }
                    else {
                        Toast.makeText(Dashboard.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int ForthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ForthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed:", "onConnectionFailed:" + connectionResult);
    }
}
