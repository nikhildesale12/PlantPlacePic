
package com.ibin.plantplacepic.bean;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InformationResponseBean {

    @SerializedName("information")
    @Expose
    private List<Information> information = null;
    @SerializedName("success")
    @Expose
    private Integer success;

    public List<Information> getInformation() {
        return information;
    }

    public void setInformation(List<Information> information) {
        this.information = information;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

}
