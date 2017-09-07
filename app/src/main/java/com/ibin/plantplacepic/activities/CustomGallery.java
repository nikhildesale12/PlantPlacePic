package com.ibin.plantplacepic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.adapter.ImageAdapter;
import com.ibin.plantplacepic.bean.SubmitRequest;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;
import com.ibin.plantplacepic.utility.ItemOffsetDecoration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class CustomGallery extends AppCompatActivity {
    private ImageAdapter imageAdapter;
    Button buttonUploadMultiple ;
    Button buttonBackGallery;
    double latitude ;
    double longitude ;
    String userId="0";
    String finalAddress ;
    private static final int REQUEST_FOR_STORAGE_PERMISSION = 123;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_gallery);
        initViews();
        getLocationInfo();

        buttonUploadMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
                if (selectedItems!= null && selectedItems.size() == 0) {
                    Toast.makeText(CustomGallery.this, "Please select atleast one image to upload", Toast.LENGTH_SHORT).show();
                }
                if (selectedItems!= null && selectedItems.size() > 0) {
                    if(selectedItems.size() > 5){
                        Toast.makeText(CustomGallery.this, "Maximum 5 Images allowed", Toast.LENGTH_SHORT).show();
                    }else{
                        UploadImageServiceCall(selectedItems);
                    }
                }
            }

        });
        buttonBackGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentdASH = new Intent(CustomGallery.this, Dashboard.class);
                startActivity(intentdASH);
                finish();
            }
        });
        populateImagesFromGallery();
        databaseHelper = DatabaseHelper.getDatabaseInstance(getApplicationContext());
    }

    private void UploadImageServiceCall(ArrayList<String> imageList) {

        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getString("USERID", "0");
        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.setUserId(userId);
        submitRequest.setImagesPathList(imageList);
        //submitRequest.setLatitude(String.valueOf(latitude));
        //submitRequest.setLongitude(String.valueOf(longitude));
        submitRequest.setStatus("false");
        //call Service and pass data
        Constants.countSelectedPhotoFromGallery = imageList.size();
        if(imageList != null && imageList != null && imageList.size() >0) {
            for (int i = 0; i < imageList.size(); i++) {
                String path = imageList.get(i);
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                submitRequest.setTime(datetime);
                if(datetime == null){
                    String currentDateTimeString =new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date());
                    currentDateTimeString = currentDateTimeString.replace(" ","_");
                    currentDateTimeString = currentDateTimeString.replace(":","-");
                    submitRequest.setTime(currentDateTimeString);
                }
                String fileName = path.substring(path.lastIndexOf('/') + 1, path.length());
                submitRequest.setImageUrl(path);
                submitRequest.setImageName(fileName);
                submitRequest.setUploadedFrom(Constants.UPLOAD_FROM_GALLERY);
                Intent intentData = new Intent(this, ImageInfoActivity.class);
                intentData.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intentData.putExtra("submitRequest",submitRequest);
                startActivity(intentData);
            }
            finish();
        }
        //call Service and pass data
        /*if(Constants.isNetworkAvailable(CustomGallery.this)){
            Toast.makeText(CustomGallery.this,"Uploading Images...",Toast.LENGTH_LONG).show();
            Intent intentService = new Intent(CustomGallery.this, ImageUploadService.class);
            intentService.putExtra("submitRequest",submitRequest);
            startService(intentService);
        }else{
            //save in local db
            long save = -1;
            if(submitRequest != null && submitRequest.getImagesPathList()!= null && submitRequest.getImagesPathList().size() >0){
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
                    save = databaseHelper.insertDataInTableInformation(submitRequest);
                }
            }
            if(save != -1){
                Toast.makeText(getApplicationContext(),"Internet Unavailable your data will upload automatically later",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Unable to save",Toast.LENGTH_SHORT).show();
            }

        }
        Intent intentdASH = new Intent(this, Dashboard.class);
        startActivity(intentdASH);
        finish();*/

    }


    private void getLocationInfo() {
        GPSTracker gps = new GPSTracker(CustomGallery.this);
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

    private void initViews() {
        buttonUploadMultiple = (Button)findViewById(R.id.buttonUploadMultiple);
        buttonBackGallery = (Button)findViewById(R.id.buttonBackGallery);
    }

    private void populateImagesFromGallery() {
        if (!mayRequestGalleryImages()) {
         return;
        }

        ArrayList<String> imageUrls = loadPhotosFromNativeGallery();
        initializeRecyclerView(imageUrls);
    }

    private boolean mayRequestGalleryImages() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            //promptStoragePermission();
            showPermissionRationaleSnackBar();
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_FOR_STORAGE_PERMISSION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FOR_STORAGE_PERMISSION: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        populateImagesFromGallery();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                            showPermissionRationaleSnackBar();
                        } else {
                            Toast.makeText(this, "Go to settings and enable permission", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                break;
            }
        }
    }

    private ArrayList<String> loadPhotosFromNativeGallery() {
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");

        ArrayList<String> imageUrls = new ArrayList<String>();

        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));
            System.out.println("====> Array path => "+imageUrls.get(i));
        }

        return imageUrls;
    }

    private void initializeRecyclerView(ArrayList<String> imageUrls) {
        imageAdapter = new ImageAdapter(this, imageUrls);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.item_offset));
        recyclerView.setAdapter(imageAdapter);
    }

    private void showPermissionRationaleSnackBar() {
        Snackbar.make(findViewById(R.id.button1),"Storage permission is needed for fetching images from Gallery.",
                Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request the permission
                ActivityCompat.requestPermissions(CustomGallery.this,
                        new String[]{READ_EXTERNAL_STORAGE},
                        REQUEST_FOR_STORAGE_PERMISSION);
            }
        }).show();

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(CustomGallery.this,"Task Cancel...",Toast.LENGTH_LONG).show();
        Intent intentdASH = new Intent(this, Dashboard.class);
        startActivity(intentdASH);
        finish();
    }
}
