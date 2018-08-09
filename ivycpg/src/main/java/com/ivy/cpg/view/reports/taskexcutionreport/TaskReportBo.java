package com.ivy.cpg.view.reports.taskexcutionreport;

import java.util.HashMap;

public class TaskReportBo {
	private String mRetailerName;
	private int  mRetailerId;
    private int beatId;
    private String beatDescription;

    public int getBeatId() {
        return beatId;
    }

    public void setBeatId(int beatId) {
        this.beatId = beatId;
    }

    public String getBeatDescription() {
        return beatDescription;
    }

    public void setBeatDescription(String beatDescription) {
        this.beatDescription = beatDescription;
    }

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
