package com.ibin.plantplacepic.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by NN on 05/06/2017.
 */

public class UploadResponse {
    private boolean success;
    @SerializedName("message")
    private String message;

    String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }
}
