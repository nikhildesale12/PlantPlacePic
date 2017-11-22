package com.ibin.plantplacepic.bean;

import com.google.android.gms.maps.model.LatLng;

public class SpeciesPoints implements com.google.maps.android.clustering.ClusterItem {

    private final LatLng mPosition;
    public String species;
    //public int profilePhoto = 0;
    public String imageName ;
    private String address;

    public SpeciesPoints(double lat, double lng, String species, String address,String imageName) {
        this.species = species;
        this.address = address;
       this.imageName = imageName;
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return species;
    }
    public String getImageName() {
        return imageName;
    }

    @Override
    public String getSnippet() {
        return address;
    }
}
