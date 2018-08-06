package com.ivy.cpg.view.supervisor.mvp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;


public class SellerBo implements Parcelable {

    private int userId,billed,covered,target,retailerId,lpc;
    private String userName,retailerName;
    private boolean isAttendanceDone = false;
    private double latitude,longitude,productivityPercent;
    private long orderValue, inTime, outTime,totalOrderValue;
    private Marker marker;


    private int targetLines,achievedLines,targetCoverage,achievedCoverage;
    private long targetValue,achievedValue;

    public SellerBo(){

    }

    private SellerBo(Parcel in) {
        userId = in.readInt();
        billed = in.readInt();
        covered = in.readInt();
        target = in.readInt();
        userName = in.readString();
        retailerName = in.readString();
        isAttendanceDone = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        orderValue = in.readLong();
        inTime = in.readLong();
        outTime = in.readLong();
        retailerId = in.readInt();
    }

    public static final Creator<SellerBo> CREATOR = new Creator<SellerBo>() {
        @Override
        public SellerBo createFromParcel(Parcel in) {
            return new SellerBo(in);
        }

        @Override
        public SellerBo[] newArray(int size) {
            return new SellerBo[size];
        }
    };

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

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
    }

    public int getTargetLines() {
        return targetLines;
    }

    public void setTargetLines(int targetLines) {
        this.targetLines = targetLines;
    }

    public int getAchievedLines() {
        return achievedLines;
    }

    public void setAchievedLines(int achievedLines) {
        this.achievedLines = achievedLines;
    }

    public int getTargetCoverage() {
        return targetCoverage;
    }

    public void setTargetCoverage(int targetCoverage) {
        this.targetCoverage = targetCoverage;
    }

    public int getAchievedCoverage() {
        return achievedCoverage;
    }

    public void setAchievedCoverage(int achievedCoverage) {
        this.achievedCoverage = achievedCoverage;
    }

    public long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(long targetValue) {
        this.targetValue = targetValue;
    }

    public long getAchievedValue() {
        return achievedValue;
    }

    public void setAchievedValue(long achievedValue) {
        this.achievedValue = achievedValue;
    }

    public long getTotalOrderValue() {
        return totalOrderValue;
    }

    public void setTotalOrderValue(long totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }

    public int getLpc() {
        return lpc;
    }

    public void setLpc(int lpc) {
        this.lpc = lpc;
    }

    public double getProductivityPercent() {
        return productivityPercent;
    }

    public void setProductivityPercent(double productivityPercent) {
        this.productivityPercent = productivityPercent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeInt(billed);
        dest.writeInt(covered);
        dest.writeInt(target);
        dest.writeString(userName);
        dest.writeString(retailerName);
        dest.writeByte((byte) (isAttendanceDone ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(orderValue);
        dest.writeLong(inTime);
        dest.writeLong(outTime);
        dest.writeInt(retailerId);
    }
}
