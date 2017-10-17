package com.ibin.plantplacepic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.bean.SpeciesPoints;
import com.ibin.plantplacepic.database.DatabaseHelper;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;
import com.ibin.plantplacepic.utility.MultiDrawable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpeciesAroundYouActivity extends FragmentActivity  implements OnMapReadyCallback , ClusterManager.OnClusterClickListener<SpeciesPoints>, ClusterManager.OnClusterInfoWindowClickListener<SpeciesPoints>, ClusterManager.OnClusterItemClickListener<SpeciesPoints>, ClusterManager.OnClusterItemInfoWindowClickListener<SpeciesPoints>{
    private GoogleMap mMap;
    Button mapSpeciesSarch;
    AutoCompleteTextView actoEnterSpeciesName;
    public ArrayList<String> speciesList;
    DatabaseHelper databaseHelper;
    List<Information> mainDataList = null;

    private ClusterManager<SpeciesPoints> mClusterManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_around_you);

        actoEnterSpeciesName=(AutoCompleteTextView)findViewById(R.id.autoCompletSpeciesSearch);
        speciesList = new ArrayList<>();
        mainDataList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getDatabaseInstance(SpeciesAroundYouActivity.this);

        actoEnterSpeciesName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                Toast.makeText(SpeciesAroundYouActivity.this,actoEnterSpeciesName.getText().toString(),Toast.LENGTH_SHORT).show();
                if(mainDataList != null && mainDataList.size()>0){
                    double latitude = 0;
                    double longitude = 0;
                    mClusterManager.clearItems();

                    //mClusterManager = new ClusterManager<SpeciesPoints>(SpeciesAroundYouActivity.this, mMap);
                    //mClusterManager.setRenderer(new SpeciesRenderer());

                    for (int i=0;i< mainDataList.size();i++){
                        if(mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(actoEnterSpeciesName.getText().toString().trim())){
                            if(mainDataList.get(i).getLat() != null && mainDataList.get(i).getLng() != null
                                    && !mainDataList.get(i).getLat().equals("0") && !mainDataList.get(i).getLat().equals("0.0")
                                    && !mainDataList.get(i).getLng().equals("0") && !mainDataList.get(i).getLng().equals("0.0")
                                    && !mainDataList.get(i).getLat().equals("") && !mainDataList.get(i).getLng().equals("")) {

                                latitude = Double.parseDouble(mainDataList.get(i).getLat());
                                longitude = Double.parseDouble(mainDataList.get(i).getLng());
                                //googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(response.body().getInformation().get(i).getSpecies()));
                                String address = "";
                                if(mainDataList.get(i).getAddress().trim().contains(",null")){
                                    address = mainDataList.get(i).getAddress().trim().replace(",null","");
                                }
                                //mClusterManager.addItem(new SpeciesPoints(latitude, longitude,mainDataList.get(i).getSpecies() , address , R.drawable.iconleaf));
                                mClusterManager.addItem(new SpeciesPoints(latitude, longitude,mainDataList.get(i).getSpecies() , address ,mainDataList.get(i).getImages()));
                            }
                        }
                    }
                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                    mClusterManager.cluster();

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
    }


    private void callServiceToGetSpeciesNames(final ClusterManager<SpeciesPoints> mClusterManager,final GoogleMap googleMap) {
        final ProgressDialog dialog = new ProgressDialog(SpeciesAroundYouActivity.this);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        ApiService service = retrofit.create(ApiService.class);
        Call<InformationResponseBean> call = service.getAllSpeciesDetail();
        call.enqueue(new Callback<InformationResponseBean>() {
            @Override
            public void onResponse(Call<InformationResponseBean> call, Response<InformationResponseBean> response) {

                if (response != null && response.body() != null) {
                    if (response.body().getSuccess().toString().trim().equals("1")) {
                        if(response.body().getInformation() != null && response.body().getInformation().size()>0){
                            mainDataList = response.body().getInformation();
                            for(int i = 0 ;i<response.body().getInformation().size();i++){
                                if(response.body().getInformation().get(i).getLat() != null && response.body().getInformation().get(i).getLng() != null
                                        && !response.body().getInformation().get(i).getLat().equals("0") && !response.body().getInformation().get(i).getLat().equals("0.0")
                                        && !response.body().getInformation().get(i).getLng().equals("0") && !response.body().getInformation().get(i).getLng().equals("0.0")
                                        && !response.body().getInformation().get(i).getLat().equals("") && !response.body().getInformation().get(i).getLng().equals("")){
                                    double latitude = Double.parseDouble(response.body().getInformation().get(i).getLat());
                                    double longitude = Double.parseDouble(response.body().getInformation().get(i).getLng());
                                    //googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(response.body().getInformation().get(i).getSpecies()));
                                    String address = "";
                                    if(response.body().getInformation().get(i).getAddress().trim().contains(",null")){
                                        address = response.body().getInformation().get(i).getAddress().trim().replace(",null","");
                                    }
                                    mClusterManager.addItem(new SpeciesPoints(latitude, longitude,response.body().getInformation().get(i).getSpecies() , address ,response.body().getInformation().get(i).getImages()));
                                    if(!speciesList.contains(response.body().getInformation().get(i).getSpecies().trim())){
                                        if(response.body().getInformation().get(i).getSpecies().trim().length()>0){
                                            speciesList.add(response.body().getInformation().get(i).getSpecies().trim());
                                        }
                                    }
                                }
                            }
                            mClusterManager.cluster();
                            //new RenderClusterInfoWindow(SpeciesAroundYouActivity.this,googleMap,mClusterManager);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SpeciesAroundYouActivity.this,android.R.layout.simple_list_item_1, speciesList);
                            actoEnterSpeciesName.setThreshold(1);
                            actoEnterSpeciesName.setAdapter(adapter);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

            mClusterManager = new ClusterManager<SpeciesPoints>(this, mMap);
            mClusterManager.setRenderer(new SpeciesRenderer());
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);
            addSpeciesPointsItems(mClusterManager,googleMap);

        }
    }

    private void addSpeciesPointsItems(ClusterManager<SpeciesPoints> mClusterManager,GoogleMap googleMap) {
        if (Constants.isNetworkAvailable(SpeciesAroundYouActivity.this)) {
                callServiceToGetSpeciesNames(mClusterManager,googleMap);
        }
    }

    @Override
    public boolean onClusterClick(Cluster<SpeciesPoints> cluster) {
        String firstName = cluster.getItems().iterator().next().species;
        //Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        final LatLngBounds bounds = builder.build();

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
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
        Toast.makeText(this, speciesPoints.getImageName() , Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SpeciesAroundYouActivity.this,LargeZoomActivity.class);
        i.putExtra("FromMap","FromMap");
        startActivity(i);
    }

    private class SpeciesRenderer extends DefaultClusterRenderer<SpeciesPoints> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public SpeciesRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_species, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(SpeciesPoints speciesPoints, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            //mImageView.setImageResource(speciesPoints.profilePhoto);
            Glide.with(SpeciesAroundYouActivity.this)
                    .load(Constants.IMAGE_DOWNLOAD_PATH+speciesPoints.getImageName())
                    .placeholder(R.mipmap.mapicon)
                    .error(R.mipmap.mapicon)
                    .into(mImageView);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(speciesPoints.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<SpeciesPoints> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            final List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            final int width = mDimension;
            final int height = mDimension;

            for (final SpeciesPoints p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4)
                    break;

                Glide.with(SpeciesAroundYouActivity.this)
                    .load(Constants.IMAGE_DOWNLOAD_PATH+p.getImageName())
                    .asBitmap()
                    //.fitCenter() or .centerCrop() depending on what was the android:scaleType on the ImageView
                    .into(new SimpleTarget<Bitmap>(width, height) {
                        @Override public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(getResources(), resource);
                            drawable.setBounds(0, 0, width, height);
                            profilePhotos.add(drawable);
                        }
                    });

            if(profilePhotos != null && profilePhotos.size()>0){
                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                multiDrawable.setBounds(0, 0, width, height);

                mClusterImageView.setImageDrawable(multiDrawable);
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }
        }
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
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

}
