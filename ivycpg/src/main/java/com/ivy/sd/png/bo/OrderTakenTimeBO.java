package com.ivy.sd.png.bo;

/**
 * Created by maheswaran.m on 09-10-2015.
 */
public class OrderTakenTimeBO {
    private String mTimePeriod;
    private int mTotalcall, mProductiveCall, mLinesSold;
    private float mValues;

    public float getmValues() {
        return mValues;
    }

    public void setmValues(float mValues) {
        this.mValues = mValues;
    }

    public int getmLinesSold() {
        return mLinesSold;
    }

    public void setmLinesSold(int mLinesSold) {
        this.mLinesSold = mLinesSold;
    }

    public int getmProductiveCall() {
        return mProductiveCall;
    }

    public void setmProductiveCall(int mProductiveCall) {
        this.mProductiveCall = mProductiveCall;
    }

    public int getmTotalcall() {
        return mTotalcall;
    }

    public void setmTotalcall(int mTotalcall) {
        this.mTotalcall = mTotalcall;
    }

    public String getmTimePeriod() {
        return mTimePeriod;
    }

    public void setmTimePeriod(String mTimePeriod) {
        this.mTimePeriod = mTimePeriod;
    }
}
