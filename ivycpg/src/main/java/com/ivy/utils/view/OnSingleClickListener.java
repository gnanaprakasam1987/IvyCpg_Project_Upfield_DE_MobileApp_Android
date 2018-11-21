package com.ivy.utils.view;

import android.view.View;

import java.util.concurrent.TimeUnit;

public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_CLICK_INTERVAL = 1000;// Click Interval Duration set 1 Second
    private long mLastClickTime = System.currentTimeMillis();//Maintain LastClick Millis Seconds

    @Override
    public void onClick(View v) {
        // Get Current Time Millis seconds
        long elapsedTime = System.currentTimeMillis() - mLastClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL) {// return value should be less then of MIN_CLICK_INTERVAL
            mLastClickTime = System.currentTimeMillis();// Update last Millis Second
            return;
        }

        onSingleClick(v);//Call Click Event

        mLastClickTime = System.currentTimeMillis();// Update last Millis Second
    }

    public abstract void onSingleClick(View v);


}
