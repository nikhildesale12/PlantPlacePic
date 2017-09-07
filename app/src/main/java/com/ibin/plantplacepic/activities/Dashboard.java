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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibin.plantplacepic.BuildConfig;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.ImageUploadService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    TextView textUploadCount;
    String currentDateTimeString;
    GPSTracker gps;
    TextView textUserName ;
    private File mFileTemp;
    private String userName ;
    private String userId ;
    private String personPhotoUrl;
    double latitude ;
    double longitude ;
    Toolbar topToolBar;
    private String finalAddress ;
    DatabaseHelper databaseHelper;
    //private ImageView profilePic;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (getIntent() != null && getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        initViews();
        topToolBar = (Toolbar)findViewById(R.id.toolbarDashboard);
        setSupportActionBar(topToolBar);
        topToolBar.setNavigationIcon(R.mipmap.userpic);

        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        userName  = prefs.getString(Constants.KEY_USERNAME, "Guest");
        userId = prefs.getString(Constants.KEY_USERID, "0");
        personPhotoUrl = prefs.getString(Constants.KEY_PHOTO, "");

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

        getUploadedCount(userId);

        textUserName.setText("Welcome "+userName+" !");
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
                if(Constants.isNetworkAvailable(Dashboard.this)){
                    Intent intent1 = new Intent(Dashboard.this,ReviewMyUploadTabActivity.class);
                    startActivity(intent1);
                    finish();
                }else{
                    Constants.dispalyDialogInternet(Dashboard.this,"Internet Unavailable","Please check internet connection",false,false);
                }
            }
        });
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
        if(Constants.isNetworkAvailable(Dashboard.this)){
            List<SubmitRequest> dataList = databaseHelper.getImageInfoToUpload(userId);
            Log.d("List : ","List : "+dataList.size());
            if(dataList!=null && dataList.size()>0){
                for(int i=0;i<dataList.size();i++){
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
                    if(submitRequest.getImageUrl() != null){
                        Intent intentService = new Intent(Dashboard.this, ImageUploadService.class);
                        intentService.putExtra("submitRequest",submitRequest);
                        startService(intentService);
                    }
                }
            }
        }
    } /*onCreate End*/

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
            SharedPreferences.Editor editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit();
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

        return super.onOptionsItemSelected(item);
    }

    private void getUploadedCount(String userId) {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<String> call = service.getUplodCount(userId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response != null && response.body() != null ){
                    textUploadCount.setText(response.body());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                textUploadCount.setText("0");
            }
        });
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
            mFileTemp = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+currentDateTimeString+".jpeg");
        }
        else {
            mFileTemp = new File(getFilesDir(), Constants.IMAGE_NAME+currentDateTimeString+".jpeg");
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
                   File file = new File(Constants.FOLDER_PATH, Constants.IMAGE_NAME+currentDateTimeString+".jpeg");
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
//                               Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PATH + File.separator + Constants.IMAGE_NAME + currentDateTimeString + ".jpeg", options);
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
                               sr.setImageName(Constants.IMAGE_NAME + currentDateTimeString + ".jpeg");
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
        textUserName = (TextView) findViewById(R.id.textUserName);
        textUploadCount = (TextView)findViewById(R.id.textUploadCount);
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
