
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
    @SerializedName("userName")
    @Expose
    private String userName;
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
    @SerializedName("updateinfo")
    @Expose
    private String updateinfo;
    @SerializedName("upload_from")
    @Expose
    private String 	uploadFrom;
    @SerializedName("mounting_board")
    @Expose
    private String 	mountingBoard;
    @SerializedName("like_count")
    @Expose
    private String 	likeCount;
    @SerializedName("dislike_count")
    @Expose
    private String 	disLikeCount;
    @SerializedName("like")
    @Expose
    private String 	like;
    @SerializedName("dislike")
    @Expose
    private String 	disLike;

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

    public String getUpdateinfo() {
        return updateinfo;
    }

    public void setUpdateinfo(String updateinfo) {
        this.updateinfo = updateinfo;
    }

    public String getUploadFrom() {
        return uploadFrom;
    }

    public void setUploadFrom(String uploadFrom) {
        this.uploadFrom = uploadFrom;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMountingBoard() {
        return mountingBoard;
    }

    public void setMountingBoard(String mountingBoard) {
        this.mountingBoard = mountingBoard;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getDisLikeCount() {
        return disLikeCount;
    }

    public void setDisLikeCount(String disLikeCount) {
        this.disLikeCount = disLikeCount;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getDisLike() {
        return disLike;
    }

    public void setDisLike(String disLike) {
        this.disLike = disLike;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
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
        dest.writeString(updateinfo);
        dest.writeString(uploadFrom);
        dest.writeString(mountingBoard);
        dest.writeString(likeCount);
        dest.writeString(disLikeCount);
        dest.writeString(like);
        dest.writeString(disLike);
    }
    public Information() {

    }
    public Information(Parcel in) {
        userId = in.readString();
        userName = in.readString();
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
        updateinfo = in.readString();
        uploadFrom = in.readString();
        mountingBoard = in.readString();
        likeCount = in.readString();
        disLikeCount = in.readString();
        like = in.readString();
        disLike = in.readString();
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
