package com.ibin.plantplacepic.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("count")
    @Expose
    private String count;

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
