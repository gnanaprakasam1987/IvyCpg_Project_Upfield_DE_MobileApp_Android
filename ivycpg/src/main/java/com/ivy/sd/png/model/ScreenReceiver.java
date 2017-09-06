package com.ivy.sd.png.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ivy.sd.png.view.OrderSummary;

/**
 * Created by vinodh.r on 03-11-2015.
 */
public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            ((OrderSummary) context).setDiscountDialog(false);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            ((OrderSummary) context).setDiscountDialog(false);
        }
    }

}
