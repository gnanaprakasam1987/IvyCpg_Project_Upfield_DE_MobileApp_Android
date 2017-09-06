package com.ivy.sd.png.bo;

import java.util.HashMap;

public class TaskReportBo {
	private String mRetailerName;
	private int  mRetailerId;
    private HashMap<String,Boolean>  mMenuCodeMap;

    public String getmRetailerName() {
        return mRetailerName;
    }

    public void setmRetailerName(String mRetailerName) {
        this.mRetailerName = mRetailerName;
    }

    public int getmRetailerId() {
        return mRetailerId;
    }

    public void setmRetailerId(int mRetailerId) {
        this.mRetailerId = mRetailerId;
    }

    public HashMap<String, Boolean> getmMenuCodeMap() {
        return mMenuCodeMap;
    }

    public void setmMenuCodeMap(HashMap<String, Boolean> mMenuCodeMap) {
        this.mMenuCodeMap = mMenuCodeMap;
    }
}
