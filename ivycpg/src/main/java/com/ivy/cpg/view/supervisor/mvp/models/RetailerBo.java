package com.ivy.cpg.view.supervisor.mvp.models;


import com.google.android.gms.maps.model.Marker;

import java.util.HashSet;
import java.util.Set;

public class RetailerBo {

    private int retailerId,userId,visitedSequence,masterSequence,lastVisitedRetailer,channelId;
    private String retailerName,date,address,imgPath,userName,ParentHierarchy;
    private double latitude,longitude,masterLatitude,masterLongitude;
    private boolean isDeviated=false
            ,isOrdered=false
            ,isSkipped = false,isVisited = false;
    private long totalOrderValue,orderValue, inTime, outTime;
    private Marker marker;
    private Set<Integer> productIds = new HashSet<>();

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

    public long getInTime() {
        return inTime;
    }

    public void setInTime(long inTime) {
        this.inTime = inTime;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
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

    public boolean isSkipped() {
        return isSkipped;
    }

    public void setSkipped(boolean skipped) {
        isSkipped = skipped;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public int getLastVisitedRetailer() {
        return lastVisitedRetailer;
    }

    public void setLastVisitedRetailer(int lastVisitedRetailer) {
        this.lastVisitedRetailer = lastVisitedRetailer;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getParentHierarchy() {
        return ParentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        ParentHierarchy = parentHierarchy;
    }

    public Set<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(Set<Integer> productIds) {
        this.productIds = productIds;
    }
}
