package com.ivy.cpg.view.supervisor;


import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class DetailsBo implements Serializable{

    private String userName,status,activityName,inTime,outTime,time,retailerName,orderValue;
    private int userId,batterStatus;
    private boolean isMockLocationEnabled,isGpsEnabled,isDeviated;

    private Marker marker;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public int getBatterStatus() {
        return batterStatus;
    }

    public void setBatterStatus(int batterStatus) {
        this.batterStatus = batterStatus;
    }

    public boolean isMockLocationEnabled() {
        return isMockLocationEnabled;
    }

    public void setMockLocationEnabled(boolean mockLocationEnabled) {
        isMockLocationEnabled = mockLocationEnabled;
    }

    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        isGpsEnabled = gpsEnabled;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(String orderValue) {
        this.orderValue = orderValue;
    }

    public boolean isDeviated() {
        return isDeviated;
    }

    public void setDeviated(boolean deviated) {
        isDeviated = deviated;
    }
}
