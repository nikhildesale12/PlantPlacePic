package com.ibin.plantplacepic.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.bean.InformationResponseBean;
import com.ibin.plantplacepic.retrofit.ApiService;
import com.ibin.plantplacepic.utility.Constants;
import com.ibin.plantplacepic.utility.GPSTracker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpeciesAroundYouActivity extends FragmentActivity  implements OnMapReadyCallback {
    private GoogleMap mMap;
    Button mapSpeciesSarch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_around_you);

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


    private void callServiceToGetSpeciesNames(final GoogleMap googleMap) {
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
                            for(int i = 0 ;i<response.body().getInformation().size();i++){
                                if(response.body().getInformation().get(i).getLat() != null && response.body().getInformation().get(i).getLng() != null
                                        && !response.body().getInformation().get(i).getLat().equals("0") && !response.body().getInformation().get(i).getLat().equals("0.0")
                                        && !response.body().getInformation().get(i).getLng().equals("0") && !response.body().getInformation().get(i).getLng().equals("0.0")
                                        && !response.body().getInformation().get(i).getLat().equals("") && !response.body().getInformation().get(i).getLng().equals("")){
                                    double latitude = Double.parseDouble(response.body().getInformation().get(i).getLat());
                                    double longitude = Double.parseDouble(response.body().getInformation().get(i).getLng());
                                    googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(response.body().getInformation().get(i).getSpecies()));
                                }
                            }
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
            if (Constants.isNetworkAvailable(SpeciesAroundYouActivity.this)) {
                callServiceToGetSpeciesNames(googleMap);
            }
        }
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
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
