package com.ivy.cpg.view.supervisor.mvp;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SupervisorModelBo {

    private String userName,status,activityName,retailerName;
    private int userId,batterStatus,retailerId,billed,covered,target;
    private boolean isMockLocationEnabled,isGpsEnabled,isDeviated,isOrdered,isAttendanceDone = false;
    private double latitude,longitude;
    private Long orderValue,timeIn,timeOut,time;
    private MarkerOptions markerOptions;
    private Marker marker;
    private int firestoreSequence, masterSequence;



    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
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

    public boolean setIsMockLocationEnabled() {
        return isMockLocationEnabled;
    }

    public void setIsMockLocationEnabled(boolean mockLocationEnabled) {
        isMockLocationEnabled = mockLocationEnabled;
    }

    public boolean setIsGpsEnabled() {
        return isGpsEnabled;
    }

    public void setIsGpsEnabled(boolean gpsEnabled) {
        isGpsEnabled = gpsEnabled;
    }

    public boolean setIsDeviated() {
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

    public boolean isAttendanceDone() {
        return isAttendanceDone;
    }

    public void setAttendanceDone(boolean attendanceDone) {
        isAttendanceDone = attendanceDone;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getFirestoreSequence() {
        return firestoreSequence;
    }

    public void setFirestoreSequence(int firestoreSequence) {
        this.firestoreSequence = firestoreSequence;
    }

    public int getMasterSequence() {
        return masterSequence;
    }

    public void setMasterSequence(int masterSequence) {
        this.masterSequence = masterSequence;
    }
}
