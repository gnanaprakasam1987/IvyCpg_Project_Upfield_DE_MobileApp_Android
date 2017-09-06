package com.ivy.sd.png.bo;

/**
 * Created by rajesh.k on 28-04-2016.
 */
public class TaskAssignBO {
    private String useid;

    private int retailerID;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked;


    private String retailerName;

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getUseid() {
        return useid;
    }

    public void setUseid(String useid) {
        this.useid = useid;
    }





    public int getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(int retailerID) {
        this.retailerID = retailerID;
    }





}
