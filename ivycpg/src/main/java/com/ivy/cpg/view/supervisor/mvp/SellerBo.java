package com.ivy.cpg.view.supervisor.mvp;

import com.google.android.gms.maps.model.Marker;


public class SellerBo {

    private int userId,billed,covered,target;
    private String userName,retailerName;
    private boolean isAttendanceDone = false;
    private double latitude,longitude;
    private long orderValue,timeIn,timeOut;
    private Marker marker;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public boolean isAttendanceDone() {
        return isAttendanceDone;
    }

    public void setAttendanceDone(boolean attendanceDone) {
        isAttendanceDone = attendanceDone;
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

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
