package com.ibin.plantplacepic.fragment;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.SpeciesAroundYouActivity;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.SpeciesPoints;
import com.ibin.plantplacepic.utility.GPSTracker;

import java.util.ArrayList;

public class DistributionFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Information> mainDataList = null;
    String selectedSpecies = "";
    private static View view;
    private ClusterManager<SpeciesPoints> mClusterManager;
    public DistributionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_distribution, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        mainDataList = new ArrayList<>();
        if (getArguments() != null){
            mainDataList = getArguments().getParcelableArrayList("mainDataList");
            selectedSpecies = getArguments().getString("selectedSpecies");
        }
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapDistrubution);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

        if(mainDataList != null && mainDataList.size()>0){
            double latitude = 0;
            double longitude = 0;
            mClusterManager = new ClusterManager<>(getContext(), googleMap);
            googleMap.setOnCameraIdleListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            for (int i=0;i< mainDataList.size();i++){
                if(mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(selectedSpecies)){
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
                        mClusterManager.addItem(new SpeciesPoints(latitude, longitude,mainDataList.get(i).getSpecies() , address ,mainDataList.get(i).getImages()));
                    }
                }
            }
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            mClusterManager.cluster();

        }
    }

    public interface OnFragmentInteractionListner {
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        Fragment fragment = (fm.findFragmentById(R.id.mapDistrubution));
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.remove(fragment);
//        ft.commit();
//    }
}
