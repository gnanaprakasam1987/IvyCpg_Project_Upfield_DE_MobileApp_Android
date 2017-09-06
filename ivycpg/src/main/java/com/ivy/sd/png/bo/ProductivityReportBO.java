package com.ivy.sd.png.bo;

/**
 * Created by maheswaran.m on 15-10-2015.
 */
public class ProductivityReportBO {

    private String mClassName;
    private int mTotalCall, mProductiveCall, mNonProductiveCall;
    private float mOrderValue;

    public String getmClassName() {
        return mClassName;
    }

    public void setmClassName(String mClassName) {
        this.mClassName = mClassName;
    }

    public int getmTotalCall() {
        return mTotalCall;
    }

    public void setmTotalCall(int mTotalCall) {
        this.mTotalCall = mTotalCall;
    }

    public int getmProductiveCall() {
        return mProductiveCall;
    }

    public void setmProductiveCall(int mProductiveCall) {
        this.mProductiveCall = mProductiveCall;
    }

    public int getmNonProductiveCall() {
        return mNonProductiveCall;
    }

    public void setmNonProductiveCall(int mNonProductiveCall) {
        this.mNonProductiveCall = mNonProductiveCall;
    }

    public float getmOrderValue() {
        return mOrderValue;
    }

    public void setmOrderValue(float mOrderValue) {
        this.mOrderValue = mOrderValue;
    }
}
