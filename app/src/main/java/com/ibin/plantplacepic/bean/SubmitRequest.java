package com.ibin.plantplacepic.bean;

import android.app.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NN on 05/06/2017.
 */

public class SubmitRequest implements Serializable {

    private String userId;
    private String imageUrl;
    private ArrayList<String> imagesPathList;
    private String imageName;
    private String title ;
    private String Species ;
    private String remark;
    private String tag;
    private String status;
    private String latitude;
    private String longitude;
    private String time;
    private String crop ;
    private String address;
    private String uploadedFrom;

    private String isSaveInLocal;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecies() {
        return Species;
    }

    public void setSpecies(String species) {
        Species = species;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public ArrayList<String> getImagesPathList() {
        return imagesPathList;
    }

    public String getUploadedFrom() {
        return uploadedFrom;
    }

    public void setUploadedFrom(String uploadedFrom) {
        this.uploadedFrom = uploadedFrom;
    }

    public void setImagesPathList(ArrayList<String> imagesPathList) {
        this.imagesPathList = imagesPathList;
    }
    public String getIsSaveInLocal() {
        return isSaveInLocal;
    }
    public void setIsSaveInLocal(String isSaveInLocal) {
        this.isSaveInLocal = isSaveInLocal;
    }
}
