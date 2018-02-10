package com.ibin.plantplacepic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by NN on 09/02/2018.
 */

public class CommentResponseBean implements Parcelable {

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("userName")
    @Expose
    private String userName;

    @SerializedName("image_name")
    @Expose
    private String image_name;

    @SerializedName("comment")
    @Expose
    private String comment;

    @SerializedName("date")
    @Expose
    private String date;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    public CommentResponseBean() {

    }
    public CommentResponseBean(Parcel in) {
        userId = in.readString();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
    }

    public static final Creator<CommentResponseBean> CREATOR = new Parcelable.Creator<CommentResponseBean>() {
        @Override
        public CommentResponseBean createFromParcel(Parcel source) {
            return new CommentResponseBean(source);
        }
        @Override
        public CommentResponseBean[] newArray(int size) {
            return new CommentResponseBean[size];
        }
    };
}
