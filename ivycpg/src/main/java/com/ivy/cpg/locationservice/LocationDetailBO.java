package com.ivy.cpg.locationservice;


import java.io.Serializable;

public class LocationDetailBO implements Serializable {

    private String userName,latitude,longitude,accuracy,time,path,userId,
            activityType,provider;
    private boolean isGpsEnabled,isMockLocationEnabled;
    private int batteryStatus;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        isGpsEnabled = gpsEnabled;
    }

    public boolean isMockLocationEnabled() {
        return isMockLocationEnabled;
    }

    public void setMockLocationEnabled(boolean mockLocationEnabled) {
        isMockLocationEnabled = mockLocationEnabled;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
