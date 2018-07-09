package com.ivy.cpg.view.supervisor.mvp;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class SupervisorModelBo {

    private String userName,status,activityName,time,retailerName;
    private int userId,batterStatus,retailerId,billed,covered;
    private boolean isMockLocationEnabled,isGpsEnabled,isDeviated,isOrdered;
    private double latitude,longitude;
    private Long orderValue,timeIn,timeOut;

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    private ArrayList<SupervisorModelBo> supervisorModelBos = new ArrayList<>();

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

    public Long getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Long orderValue) {
        this.orderValue = orderValue;
    }

    public Long getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Long timeIn) {
        this.timeIn = timeIn;
    }

    public Long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
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



    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBatterStatus() {
        return batterStatus;
    }

    public void setBatterStatus(int batterStatus) {
        this.batterStatus = batterStatus;
    }

    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
    }

    public int getBilled() {
        return billed;
    }

    public void setBilled(int billed) {
        this.billed = billed;
    }

    public int getCovered() {
        return covered;
    }

    public void setCovered(int covered) {
        this.covered = covered;
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

    public boolean isDeviated() {
        return isDeviated;
    }

    public void setDeviated(boolean deviated) {
        isDeviated = deviated;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public void setOrdered(boolean ordered) {
        isOrdered = ordered;
    }

    public ArrayList<SupervisorModelBo> getSupervisorModelBos() {
        return supervisorModelBos;
    }

    public void setSupervisorModelBos(ArrayList<SupervisorModelBo> supervisorModelBos) {
        this.supervisorModelBos = supervisorModelBos;
    }
}
