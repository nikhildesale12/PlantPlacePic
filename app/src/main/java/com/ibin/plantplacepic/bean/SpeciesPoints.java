package com.ibin.plantplacepic.bean;

import com.google.android.gms.maps.model.LatLng;

public class SpeciesPoints implements com.google.maps.android.clustering.ClusterItem {

    private final LatLng mPosition;
    private String name;
    private String twitterHandle;

    public SpeciesPoints(double lat, double lng, String name, String twitterHandle) {
        this.name = name;
        this.twitterHandle = twitterHandle;
        mPosition = new LatLng(lat, lng);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return twitterHandle;
    }
}
