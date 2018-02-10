package com.ibin.plantplacepic.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by NN on 09/02/2018.
 */

public class CommentResponse {

    @SerializedName("information")
    @Expose
    private List<CommentResponseBean> information = null;
    @SerializedName("success")
    @Expose
    private Integer success;

    public List<CommentResponseBean> getInformation() {
        return information;
    }

    public void setInformation(List<CommentResponseBean> information) {
        this.information = information;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

}
