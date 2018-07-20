package com.ivy.cpg.view.supervisor.mvp;


import com.google.android.gms.maps.model.Marker;

public class RetailerBo {

    private int retailerId,userId,visitedSequence,masterSequence;
    private String retailerName;
    private double latitude,longitude,masterLatitude,masterLongitude;
    private boolean isDeviated,isOrdered;
    private long totalOrderValue,orderValue,timeIn,timeOut;
    private Marker marker;

    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVisitedSequence() {
        return visitedSequence;
    }

    public void setVisitedSequence(int visitedSequence) {
        this.visitedSequence = visitedSequence;
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

    public boolean getIsDeviated() {
        return isDeviated;
    }

    public void setIsDeviated(boolean deviated) {
        isDeviated = deviated;
    }

    public boolean getIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(boolean ordered) {
        isOrdered = ordered;
    }

    public long getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(long orderValue) {
        this.orderValue = orderValue;
    }

    public long getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(long timeIn) {
        this.timeIn = timeIn;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public double getMasterLatitude() {
        return masterLatitude;
    }

    public void setMasterLatitude(double masterLatitude) {
        this.masterLatitude = masterLatitude;
    }

    public double getMasterLongitude() {
        return masterLongitude;
    }

    public void setMasterLongitude(double masterLongitude) {
        this.masterLongitude = masterLongitude;
    }

    public int getMasterSequence() {
        return masterSequence;
    }

    public void setMasterSequence(int masterSequence) {
        this.masterSequence = masterSequence;
    }

    public long getTotalOrderValue() {
        return totalOrderValue;
    }

    public void setTotalOrderValue(long totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
