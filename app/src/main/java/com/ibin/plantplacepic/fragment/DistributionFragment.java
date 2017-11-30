package com.ibin.plantplacepic.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.ibin.plantplacepic.R;
import com.ibin.plantplacepic.activities.LargeZoomActivity;
import com.ibin.plantplacepic.activities.SpeciesAroundYouActivity;
import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.SpeciesPoints;

import java.util.ArrayList;
import java.util.List;

public class DistributionFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener,
        ClusterManager.OnClusterClickListener<SpeciesPoints>, ClusterManager.OnClusterInfoWindowClickListener<SpeciesPoints>, ClusterManager.OnClusterItemClickListener<SpeciesPoints>, ClusterManager.OnClusterItemInfoWindowClickListener<SpeciesPoints> {

    private GoogleMap mMap;
    ArrayList<Information> mainDataList = null;
    String selectedSpecies = "";
    private static View view;
    //private ClusterManager<SpeciesPoints> mClusterManager;

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
        if (getArguments() != null) {
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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
//            mClusterManager = new ClusterManager<>(getContext(), googleMap);
//            googleMap.setOnCameraIdleListener(mClusterManager);
//            googleMap.setOnMarkerClickListener(mClusterManager);
//            googleMap.setOnInfoWindowClickListener(mClusterManager);

            for (int i=0;i< mainDataList.size();i++){
                if(mainDataList.get(i).getSpecies().trim().equalsIgnoreCase(selectedSpecies)){
                    if(mainDataList.get(i).getLat() != null && mainDataList.get(i).getLng() != null
                            && !mainDataList.get(i).getLat().equals("0") && !mainDataList.get(i).getLat().equals("0.0")
                            && !mainDataList.get(i).getLng().equals("0") && !mainDataList.get(i).getLng().equals("0.0")
                            && !mainDataList.get(i).getLat().equals("") && !mainDataList.get(i).getLng().equals("")) {
                        latitude = Double.parseDouble(mainDataList.get(i).getLat());
                        longitude = Double.parseDouble(mainDataList.get(i).getLng());
                        googleMap.addMarker(new MarkerOptions()
                                .snippet(mainDataList.get(i).getAddress())
                                .position(new LatLng(latitude, longitude))
                                .title(mainDataList.get(i).getSpecies()))
                                .setTag(mainDataList.get(i).getImages());
                        googleMap.setOnInfoWindowClickListener(this);
//                        String address = "";
//                        if(mainDataList.get(i).getAddress().trim().contains(",null")){
//                            address = mainDataList.get(i).getAddress().trim().replace(",null","");
//                        }
//                        mClusterManager.addItem(new SpeciesPoints(latitude, longitude,mainDataList.get(i).getSpecies() , address ,mainDataList.get(i).getImages()));
                    }
                }
            }
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

//            mClusterManager.cluster();

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

        return false;
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
        //Toast.makeText(this, speciesPoints.getImageName() , Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getContext(),LargeZoomActivity.class);
        List<Information> dataList = new ArrayList<>();
        Information e = new Information();
        e.setImages(speciesPoints.getImageName());
        dataList.add(e);
        Bundle data = new Bundle();
        data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
        data.putString("FromMap","FromMap");
        i.putExtras(data);
        startActivity(i);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent i = new Intent(getContext(),LargeZoomActivity.class);
        List<Information> dataList = new ArrayList<>();
        Information e = new Information();
        e.setImages((String) marker.getTag());
        dataList.add(e);
        Bundle data = new Bundle();
        data.putParcelableArrayList("imageDataList", (ArrayList<? extends Parcelable>) dataList);
        data.putString("FromMap","FromMap");
        i.putExtras(data);
        startActivity(i);
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
