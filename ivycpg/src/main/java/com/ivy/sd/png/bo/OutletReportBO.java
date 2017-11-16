package com.ivy.sd.png.bo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rajkumar.s on 10/31/2017.
 */

public class OutletReportBO implements Parcelable {

    int userId, retailerId, isPlanned = 0, isVisited = 0;
    String userName, retailerName, locationName, Address;
    String timeIn, timeOut, duration;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getIsPlanned() {
        return isPlanned;
    }

    public void setIsPlanned(int isPlanned) {
        this.isPlanned = isPlanned;
    }

    public int getIsVisited() {
        return isVisited;
    }

    public void setIsVisited(int isVisited) {
        this.isVisited = isVisited;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSalesValue() {
        return salesValue;
    }

    public void setSalesValue(String salesValue) {
        this.salesValue = salesValue;
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

    String salesValue;
    double latitude = 0;
    double longitude = 0;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


    boolean isChecked;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    int sequence;

    public OutletReportBO(){

    }
    protected OutletReportBO(Parcel in) {
        userName = in.readString();
        isChecked=in.readByte()!=0;

    }

    public static final Creator<OutletReportBO> CREATOR = new Creator<OutletReportBO>() {
        @Override
        public OutletReportBO createFromParcel(Parcel in) {
            return new OutletReportBO(in);
        }

        @Override
        public OutletReportBO[] newArray(int size) {
            return new OutletReportBO[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
