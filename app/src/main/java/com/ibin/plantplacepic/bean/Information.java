
package com.ibin.plantplacepic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Information implements Parcelable {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("images")
    @Expose
    private String images;
    @SerializedName("species")
    @Expose
    private String species;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("tag")
    @Expose
    private String tag;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("crop")
    @Expose
    private String crop;
    @SerializedName("time")
    @Expose
    private String time;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(images);
        dest.writeString(species);
        dest.writeString(remark);
        dest.writeString(tag);
        dest.writeString(status);
        dest.writeString(title);
        dest.writeString(lat);
        dest.writeString(lng);
        dest.writeString(address);
        dest.writeString(crop);
        dest.writeString(time);
    }
    public Information() {

    }
    public Information(Parcel in) {
        userId = in.readString();
        images = in.readString();
        species = in.readString();
        remark = in.readString();
        tag = in.readString();
        status = in.readString();
        title = in.readString();
        lat = in.readString();
        lng = in.readString();
        address = in.readString();
        crop = in.readString();
        time = in.readString();
    }

    public static final Creator<Information> CREATOR = new Parcelable.Creator<Information>() {
        @Override
        public Information createFromParcel(Parcel source) {
            return new Information(source);
        }
        @Override
        public Information[] newArray(int size) {
            return new Information[size];
        }
    };
}
