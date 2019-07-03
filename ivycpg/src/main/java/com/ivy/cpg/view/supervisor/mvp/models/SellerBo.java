package com.ivy.cpg.view.supervisor.mvp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;


public class SellerBo implements Parcelable {

    private int userId,billed,covered,target,retailerId,lpc,totallpc,productivityPercent,deviationCount;
    private String userName,RetailerName,imagePath,uid="";
    private boolean isAttendanceDone = false,isSellerWorking = true;
    private double latitude,longitude,totalOrderValue,orderValue, totalweight,achievedTotalWeight,targetTotalWeight;
    private long  inTime, outTime,totalCallDuration;
    private Marker marker;


    private int targetLines,achievedLines,targetCoverage,achievedCoverage;
    private long targetValue,achievedValue;

    public SellerBo(){

    }

    public SellerBo(Parcel in) {
        userId = in.readInt();
        billed = in.readInt();
        covered = in.readInt();
        target = in.readInt();
        retailerId = in.readInt();
        lpc = in.readInt();
        totallpc = in.readInt();
        productivityPercent = in.readInt();
        deviationCount = in.readInt();
        userName = in.readString();
        RetailerName = in.readString();
        imagePath = in.readString();
        uid = in.readString();
        isAttendanceDone = in.readByte() != 0;
        isSellerWorking = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        orderValue = in.readDouble();
        inTime = in.readLong();
        outTime = in.readLong();
        totalOrderValue = in.readDouble();
        totalweight = in.readDouble();
        achievedTotalWeight = in.readDouble();
        targetTotalWeight = in.readDouble();
        totalCallDuration = in.readLong();
        targetLines = in.readInt();
        achievedLines = in.readInt();
        targetCoverage = in.readInt();
        achievedCoverage = in.readInt();
        targetValue = in.readLong();
        achievedValue = in.readLong();
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
        return RetailerName;
    }

    public void setRetailerName(String retailerName) {
        this.RetailerName = retailerName;
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

    public double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(double orderValue) {
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

    public double getTotalOrderValue() {
        return totalOrderValue;
    }

    public void setTotalOrderValue(double totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }

    public int getLpc() {
        return lpc;
    }

    public void setLpc(int lpc) {
        this.lpc = lpc;
    }

    public int getProductivityPercent() {
        return productivityPercent;
    }

    public void setProductivityPercent(int productivityPercent) {
        this.productivityPercent = productivityPercent;
    }

    public int getDeviationCount() {
        return deviationCount;
    }

    public void setDeviationCount(int deviationCount) {
        this.deviationCount = deviationCount;
    }

    public long getTotalCallDuration() {
        return totalCallDuration;
    }

    public void setTotalCallDuration(long totalCallDuration) {
        this.totalCallDuration = totalCallDuration;
    }

    public int getTotallpc() {
        return totallpc;
    }

    public void setTotallpc(int totallpc) {
        this.totallpc = totallpc;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isSellerWorking() {
        return isSellerWorking;
    }

    public void setSellerWorking(boolean sellerWorking) {
        isSellerWorking = sellerWorking;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getTotalweight() {
        return totalweight;
    }

    public void setTotalweight(double totalweight) {
        this.totalweight = totalweight;
    }

    public double getAchievedTotalWeight() {
        return achievedTotalWeight;
    }

    public void setAchievedTotalWeight(double achievedTotalWeight) {
        this.achievedTotalWeight = achievedTotalWeight;
    }

    public double getTargetTotalWeight() {
        return targetTotalWeight;
    }

    public void setTargetTotalWeight(double targetTotalWeight) {
        this.targetTotalWeight = targetTotalWeight;
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
        dest.writeInt(retailerId);
        dest.writeInt(lpc);
        dest.writeInt(totallpc);
        dest.writeInt(productivityPercent);
        dest.writeInt(deviationCount);
        dest.writeString(userName);
        dest.writeString(RetailerName);
        dest.writeString(imagePath);
        dest.writeString(uid);
        dest.writeByte((byte) (isAttendanceDone ? 1 : 0));
        dest.writeByte((byte) (isSellerWorking ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(orderValue);
        dest.writeLong(inTime);
        dest.writeLong(outTime);
        dest.writeDouble(totalOrderValue);
        dest.writeLong(totalCallDuration);
        dest.writeInt(targetLines);
        dest.writeInt(achievedLines);
        dest.writeInt(targetCoverage);
        dest.writeInt(achievedCoverage);
        dest.writeLong(targetValue);
        dest.writeLong(achievedValue);
        dest.writeDouble(totalweight);
        dest.writeDouble(achievedTotalWeight);
        dest.writeDouble(targetTotalWeight);
    }
}
