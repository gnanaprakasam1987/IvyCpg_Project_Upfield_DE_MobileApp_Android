package com.ivy.ui.task.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskRetailerBo implements Parcelable {

    private String retailerId;
    private String retailerName;
    private String lastVisitDate;
    private int nextVisitDaysCount;
    private String retAddress;

    public TaskRetailerBo() {

    }

    protected TaskRetailerBo(Parcel in) {
        retailerId = in.readString();
        retailerName = in.readString();
        lastVisitDate = in.readString();
        nextVisitDaysCount = in.readInt();
        retAddress = in.readString();
    }

    public static final Creator<TaskRetailerBo> CREATOR = new Creator<TaskRetailerBo>() {
        @Override
        public TaskRetailerBo createFromParcel(Parcel in) {
            return new TaskRetailerBo(in);
        }

        @Override
        public TaskRetailerBo[] newArray(int size) {
            return new TaskRetailerBo[size];
        }
    };

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getNextVisitDaysCount() {
        return nextVisitDaysCount;
    }

    public void setNextVisitDaysCount(int nextVisitDaysCount) {
        this.nextVisitDaysCount = nextVisitDaysCount;
    }

    public String getRetAddress() {
        return retAddress;
    }

    public void setRetAddress(String retAddress) {
        this.retAddress = retAddress;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(retailerId);
        parcel.writeString(retailerName);
        parcel.writeString(lastVisitDate);
        parcel.writeInt(nextVisitDaysCount);
        parcel.writeString(retAddress);
    }
}
