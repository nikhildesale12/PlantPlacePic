package com.ibin.plantplacepic.activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.SpeciesPoints;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.services.GetAllUploadedDataService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SpeciesAroundYouActivity extends FragmentActivity  implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,ClusterManager.OnClusterClickListener<SpeciesPoints>, ClusterManager.OnClusterInfoWindowClickListener<SpeciesPoints>, ClusterManager.OnClusterItemClickListener<SpeciesPoints>, ClusterManager.OnClusterItemInfoWindowClickListener<SpeciesPoints>{
    public GoogleMap mMap;
    Button mapSpeciesSarch;
    AutoCompleteTextView ACTtEnterSpeciesName;
    public ArrayList<String> speciesList;
    public DatabaseHelper databaseHelper;
    public List<Information> mainDataList = null;
    private RadioGroup rgViews;
    private ImageView refreshPoints;
    //String userName = "";
    ProgressDialog dialog;
    public TextView animateText;
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

    public static SpeciesAroundYouActivity activity;
    public static View view;
    public static SpeciesAroundYouActivity getInstance(){
        return activity;
    }

    //    private ClusterManager<SpeciesPoints> mClusterManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_around_you);
        animateText = (TextView) findViewById(R.id.animateText);
        view = animateText;
        /*SharedPreferences prefs1 = getSharedPreferences(Constants.MY_PREFS_USER_INFO, MODE_PRIVATE);
        userName  = prefs1.getString(Constants.KEY_FIRSTNAME, "") + " "
                +prefs1.getString(Constants.KEY_MIDDLENAME, "")  + " "
                + prefs1.getString(Constants.KEY_LASTNAME, "");*/
        activity=this;
        if(GetAllUploadedDataService.isRunning){
        //if(isServiceRunning(GetAllUploadedDataService.class)){
            animateText.setVisibility(View.VISIBLE);
            Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_animation);
            animateText.startAnimation(startAnimation);
        }else{
            animateText.setVisibility(View.INVISIBLE);
        }


        ACTtEnterSpeciesName=(AutoCompleteTextView)findViewById(R.id.autoCompletSpeciesSearch);
        speciesList = new ArrayList<>();
        mainDataList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getDatabaseInstance(SpeciesAroundYouActivity.this);

        ACTtEnterSpeciesName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                Toast.makeText(SpeciesAroundYouActivity.this,ACTtEnterSpeciesName.getText().toString(),Toast.LENGTH_SHORT).show();
                if(mainDataList != null && mainDataList.size()>0){
                    double latitude = 0;
                    double longitude = 0;
//                    mClusterManager.clearItems();
                    mMap.clear();
                    mMap.setOnInfoWindowClickListener(SpeciesAroundYouActivity.this);
                    for (int i=0;i< mainDataList.size();i++){
                        if(mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(ACTtEnterSpeciesName.getText().toString().trim())){
                            if(mainDataList.get(i).getLat() != null && mainDataList.get(i).getLng() != null
                                    && !mainDataList.get(i).getLat().equals("0") && !mainDataList.get(i).getLat().equals("0.0")
                                    && !mainDataList.get(i).getLng().equals("0") && !mainDataList.get(i).getLng().equals("0.0")
                                    && !mainDataList.get(i).getLat().equals("") && !mainDataList.get(i).getLng().equals("")) {
                                latitude = Double.parseDouble(mainDataList.get(i).getLat());
                                longitude = Double.parseDouble(mainDataList.get(i).getLng());
//                                if(mainDataList.get(i).getAddress() != null && mainDataList.get(i).getAddress().trim().contains(",,")){
//                                    mainDataList.get(i).getAddress().trim().replace(",,","");
//                                }
                                if(mainDataList.get(i).getUserName() != null && mainDataList.get(i).getUserName().trim().length()>0){
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                            .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                                            .title(mainDataList.get(i).getSpecies()+"("+mainDataList.get(i).getUserName().trim()+")"))
                                            .setTag(mainDataList.get(i).getImages());
                                }else{
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                            .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                                            .title(mainDataList.get(i).getSpecies()))
                                            .setTag(mainDataList.get(i).getImages());
                                }
//                                String address = "";
//                                if(mainDataList.get(i).getAddress().trim().contains(",null")){
//                                    address = mainDataList.get(i).getAddress().trim().replace(",null","");
//                                }
//                                mClusterManager.addItem(new SpeciesPoints(latitude, longitude,mainDataList.get(i).getSpecies() , address,mainDataList.get(i).getImages()));
                            }else{
                                //get address and get it on map
                                if(mainDataList.get(i).getAddress() != null && mainDataList.get(i).getAddress().length()>3){
                                    getAddressOnMap (mainDataList.get(i).getAddress(), i);
                                }
                            }
                        }
                    }
                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
//                    mClusterManager.cluster();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapSpeciesSarch=(Button)findViewById(R.id.mapSpeciesSearch);

        mapSpeciesSarch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(SpeciesAroundYouActivity.this,SpeciesInfoActivity.class);
                startActivity(intent2);
            }
        });

        rgViews=(RadioGroup) findViewById(R.id.rg_views_around);
        rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb_normal_around){
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }else if(checkedId == R.id.rb_satellite_around){
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            }
        });

        refreshPoints = (ImageView) findViewById(R.id.refreshPoints);
        refreshPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(Constants.isNetworkAvailable(SpeciesAroundYouActivity.this)){
                                    animateText.setVisibility(View.VISIBLE);
                                    Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_animation);
                                    animateText.startAnimation(startAnimation);
                                    Intent intentService = new Intent(SpeciesAroundYouActivity.this, GetAllUploadedDataService.class);
                                    startService(intentService);
                                }else{
                                    Toast.makeText(SpeciesAroundYouActivity.this,"No Internet Connction",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        });
    }

    /*private boolean isServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/
    private void getAddressOnMap(String addressStr,int i) {
        Geocoder geocode = new Geocoder(this);
        double latitude = 0;
        double longitude = 0;
            Address address = null;
            List<Address> addressList = null;
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
            if (latitude != 0 && longitude != 0) {
                if (mainDataList.get(i).getUserName() != null && mainDataList.get(i).getUserName().trim().length() > 0) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                            .title(mainDataList.get(i).getSpecies() + "(" + mainDataList.get(i).getUserName().trim() + ")"))
                            .setTag(mainDataList.get(i).getImages());
                } else {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            .snippet(mainDataList.get(i).getAddress().replace(",null","").replace(",,",""))
                            .title(mainDataList.get(i).getSpecies()));
                }
            }
    }

    private void callServiceToGetSpeciesNames(final GoogleMap googleMap) {
        final ProgressDialog dialog = new ProgressDialog(SpeciesAroundYouActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.getAllSpeciesDetail();
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {

                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null && response.body().getInformation().size()>0){
                            googleMap.setOnInfoWindowClickListener(SpeciesAroundYouActivity.this);
                            mainDataList = response.body().getInformation();
                            for(int i = 0 ;i<response.body().getInformation().size();i++){
                                if(response.body().getInformation().get(i).getLat() != null && response.body().getInformation().get(i).getLng() != null
                                        && !response.body().getInformation().get(i).getLat().equals("0") && !response.body().getInformation().get(i).getLat().equals("0.0")
                                        && !response.body().getInformation().get(i).getLng().equals("0") && !response.body().getInformation().get(i).getLng().equals("0.0")
                                        && !response.body().getInformation().get(i).getLat().equals("") && !response.body().getInformation().get(i).getLng().equals("")){
                                    double latitude = Double.parseDouble(response.body().getInformation().get(i).getLat());
                                    double longitude = Double.parseDouble(response.body().getInformation().get(i).getLng());
                                    if(response.body().getInformation().get(i).getAddress() != null && response.body().getInformation().get(i).getAddress().trim().contains(",,")){
                                        response.body().getInformation().get(i).getAddress().trim().replace(",,","");
                                    }
                                    if(response.body().getInformation().get(i).getUserName() != null && response.body().getInformation().get(i).getUserName().trim().length()>0){
                                        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                                .snippet(response.body().getInformation().get(i).getAddress().replace(",null","").replace(",,",""))
                                                .title(response.body().getInformation().get(i).getSpecies()+"("+response.body().getInformation().get(i).getUserName().trim()+")"))
                                                .setTag(response.body().getInformation().get(i).getImages());
                                    }else{
                                        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                                .snippet(response.body().getInformation().get(i).getAddress().replace(",null","").replace(",,",""))
                                                .title(response.body().getInformation().get(i).getSpecies()))
                                                .setTag(response.body().getInformation().get(i).getImages());
                                    }

                                    /*String address = "";
                                    if(response.body().getInformation().get(i).getAddress().trim().contains(",null")){
                                        address = response.body().getInformation().get(i).getAddress().trim().replace(",null","");
                                    }
                                    mClusterManager.addItem(new SpeciesPoints(latitude, longitude,response.body().getInformation().get(i).getSpecies() , address , response.body().getInformation().get(i).getImages()));
*/
                                    if(!speciesList.contains(response.body().getInformation().get(i).getSpecies().trim())){
                                        if(response.body().getInformation().get(i).getSpecies().trim().length()>0){
                                            speciesList.add(response.body().getInformation().get(i).getSpecies().trim());
                                        }
                                    }
                                }else{
                                    if(response.body().getInformation().get(i).getAddress() != null && response.body().getInformation().get(i).getAddress().trim().contains(",,")){
                                        response.body().getInformation().get(i).getAddress().trim().replace(",,","");
                                    }
                                    if(!speciesList.contains(response.body().getInformation().get(i).getSpecies().trim())){
                                        if(response.body().getInformation().get(i).getSpecies().trim().length()>0){
                                            speciesList.add(response.body().getInformation().get(i).getSpecies().trim());
                                        }
                                    }
                                    //get address and get it on map
                                    if(mainDataList.get(i).getAddress() != null && mainDataList.get(i).getAddress().length()>3){
//                                        final int finalI = i;
//                                        new Thread() {
//                                            public void run() {
//                                                runOnUiThread(new Runnable() {
//
//                                                    public void run() {
//                                                    }
//                                                });
//                                            }
//                                        }.start();
                                        getAddressOnMap (mainDataList.get(i).getAddress(), i);
                                    }
                                }
                            }
//                          mClusterManager.cluster();
//                          new RenderClusterInfoWindow(SpeciesAroundYouActivity.this,googleMap,mClusterManager);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SpeciesAroundYouActivity.this,android.R.layout.simple_list_item_1, speciesList);
                            ACTtEnterSpeciesName.setThreshold(1);
                            ACTtEnterSpeciesName.setAdapter(adapter);
                        }
                    }
                    if (response.body().getSuccess().toString().trim().equals("0")) {
                    }
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<InformationResponseBean> call, Throwable t) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(SpeciesAroundYouActivity.this,"Unable to load data due to "+t.toString(),Toast.LENGTH_LONG).show();
                //Log.d("resp
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if(android.os.Build.VERSION.SDK_INT >= Constants.API_LEVEL_23){
            if(checkPermission()){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                // Get LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                // Create a criteria object to retrieve provider
                Criteria criteria = new Criteria();
                // Get the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);
                // Get Current Location
    //        Location myLocation = locationManager.getLastKnownLocation(provider);
    //        double latitude = myLocation.getLatitude();
    //        double longitude = myLocation.getLongitude();
    //        LatLng latLng = new LatLng(latitude, longitude);

                GPSTracker gps = null;
                LatLng latLng = null;
                if(!isGPSEnabled()){
                    showSettingsAlert();
                }else{
                    gps = new GPSTracker(SpeciesAroundYouActivity.this);
                    if(gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        latLng = new LatLng(latitude, longitude);
                    }
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    // Show the current location in Google Map
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Zoom in the Google Map
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

//                    mClusterManager = new ClusterManager<>(this, googleMap);

//                    googleMap.setOnCameraIdleListener(mClusterManager);
//                    googleMap.setOnMarkerClickListener(mClusterManager);
//                    googleMap.setOnInfoWindowClickListener(mClusterManager);
//                    mClusterManager.setOnClusterClickListener(this);
//                    mClusterManager.setOnClusterInfoWindowClickListener(this);
//                    mClusterManager.setOnClusterItemClickListener(this);
//                    mClusterManager.setOnClusterItemInfoWindowClickListener(this);
//                    addSpeciesPointsItems(mClusterManager,googleMap);

                    //mClusterManager.cluster();
                    if(databaseHelper.getTotalALLUploadedData() == 0) {
                        if (Constants.isNetworkAvailable(SpeciesAroundYouActivity.this)) {
                            Runnable runThis = new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    if(databaseHelper.getTotalALLUploadedData() == 0) {
                                        callServiceToGetSpeciesNames(googleMap);
                                    }
                                    Looper.loop();
                                }
                            };
                            Thread th = new Thread(runThis);
                            th.start();
                        }
                    }else{
                        //use async task to prevent ui freez
                        new showPointsFromLocalAsync(googleMap).executeOnExecutor(sExecutor);
                    }
            }
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
            }else {
                requestPermission();
            }
        }
    }

    private class showPointsFromLocalAsync extends AsyncTask<Void, Integer, String> {
        GoogleMap googleMap;
        private showPointsFromLocalAsync(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SpeciesAroundYouActivity.this);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            mainDataList = databaseHelper.getAllImageUploadedInfo();
            return "";
        }
        @Override
        protected void onPostExecute(String result) {
            googleMap.setOnInfoWindowClickListener(SpeciesAroundYouActivity.this);
            for(int i = 0 ;i<mainDataList.size();i++){
                if(mainDataList.get(i).getLat() != null && mainDataList.get(i).getLng() != null
                        && !mainDataList.get(i).getLat().equals("0") && !mainDataList.get(i).getLat().equals("0.0")
                        && !mainDataList.get(i).getLng().equals("0") && !mainDataList.get(i).getLng().equals("0.0")
                        && !mainDataList.get(i).getLat().equals("") && !mainDataList.get(i).getLng().equals("")){
                    double latitude = Double.parseDouble(mainDataList.get(i).getLat());
                    double longitude = Double.parseDouble(mainDataList.get(i).getLng());
                    if(mainDataList.get(i).getAddress() != null && mainDataList.get(i).getAddress().trim().contains(",,")){
                        mainDataList.get(i).getAddress().trim().replace(",,","");
                    }
                    if(mainDataList.get(i).getUserName() != null && mainDataList.get(i).getUserName().trim().length()>0){
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .snippet(mainDataList.get(i).getAddress())
                                .title(mainDataList.get(i).getSpecies()+"("+mainDataList.get(i).getUserName().trim()+")"))
                                .setTag(mainDataList.get(i).getImages());
                    }else{
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .snippet(mainDataList.get(i).getAddress())
                                .title(mainDataList.get(i).getSpecies()))
                                .setTag(mainDataList.get(i).getImages());
                    }
                    if(!speciesList.contains(mainDataList.get(i).getSpecies().trim())){
                        if(mainDataList.get(i).getSpecies().trim().length()>0){
                            speciesList.add(mainDataList.get(i).getSpecies().trim());
                        }
                    }
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SpeciesAroundYouActivity.this,android.R.layout.simple_list_item_1, speciesList);
            ACTtEnterSpeciesName.setThreshold(1);
            ACTtEnterSpeciesName.setAdapter(adapter);

            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
        }
    }




    /*private void addSpeciesPointsItems(ClusterManager<SpeciesPoints> mClusterManager,GoogleMap googleMap) {
        if (Constants.isNetworkAvailable(SpeciesAroundYouActivity.this)) {
            callServiceToGetSpeciesNames(googleMap);
        }
    }*/


//    private void addPersonItems() {
//        for (int i = 0; i < 3; i++) {
//            mClusterManager.addItem(new SpeciesPoints(-26.187616, 28.079329, "PJ", "https://twitter.com/pjapplez"));
//            mClusterManager.addItem(new SpeciesPoints(-26.207616, 28.079329, "PJ2", "https://twitter.com/pjapplez"));
//            mClusterManager.addItem(new SpeciesPoints(-26.217616, 28.079329, "PJ3", "https://twitter.com/pjapplez"));
//        }
//    }

    @Override
    public boolean onClusterClick(Cluster<SpeciesPoints> cluster) {
//        String firstName = cluster.getItems().iterator().next().species;
//        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        final LatLngBounds bounds = builder.build();

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<SpeciesPoints> cluster) {

    }

    @Override
    public boolean onClusterItemClick(SpeciesPoints speciesPoints) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(SpeciesPoints speciesPoints) {
//        Toast.makeText(this, speciesPoints.getImageName() , Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SpeciesAroundYouActivity.this,LargeZoomActivity.class);
        List<Information> dataList = new ArrayList<>();
        Information e = new Information();
        e.setImages(speciesPoints.getImageName());
        if(speciesPoints.getTitle().contains("(")){
            String[] split = speciesPoints.getTitle().split("\\(") ;
            e.setSpecies(split[0]);
        }else{
            e.setSpecies(speciesPoints.getTitle());
        }
        dataList.add(e);
        Bundle data = new Bundle();
        data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
        data.putString("FromMap","FromMap");
        i.putExtras(data);
        startActivity(i);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(this, ""+marker.getTag() , Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SpeciesAroundYouActivity.this,LargeZoomActivity.class);
        List<Information> dataList = new ArrayList<>();
        Information e = new Information();
        e.setImages((String) marker.getTag());
        if(marker.getTitle().contains("(")){
            String[] split = marker.getTitle().split("\\(") ;
            e.setSpecies(split[0]);
        }else{
            e.setSpecies(marker.getTitle());
        }
        dataList.add(e);
        Bundle data = new Bundle();
        data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
        data.putString("FromMap","FromMap");
        i.putExtras(data);
        startActivity(i);
        finish();
    }

    private class RenderClusterInfoWindow extends DefaultClusterRenderer<SpeciesPoints> {

        RenderClusterInfoWindow(Context context, GoogleMap map, ClusterManager<SpeciesPoints> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onClusterRendered(Cluster<SpeciesPoints> cluster, Marker marker) {
            super.onClusterRendered(cluster, marker);
        }

        @Override
        protected void onBeforeClusterItemRendered(SpeciesPoints item, MarkerOptions markerOptions) {
            markerOptions.title(item.getTitle());

            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }

    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SpeciesAroundYouActivity.this);
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


    /** Permission code starts*/
    private void requestPermission() {
        ActivityCompat.requestPermissions(SpeciesAroundYouActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
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
                        Toast toast = Toast.makeText(SpeciesAroundYouActivity.this,"Permission Denied,Accept it to use application", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        finish();
                    }
                }
                break;
        }
    }
    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED ;
    }
    /**Permission code end*/

   /* private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("service.service.getClassName() : ","service.service.getClassName() :==> " +service.service.getClassName());
            if (GetAllUploadedDataService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

}
