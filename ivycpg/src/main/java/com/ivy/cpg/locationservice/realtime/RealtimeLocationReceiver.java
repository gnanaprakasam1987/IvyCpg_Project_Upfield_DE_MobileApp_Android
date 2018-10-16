package com.ivy.cpg.locationservice.realtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ivy.cpg.locationservice.LocationServiceHelper;

public class RealtimeLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
        boolean isInWork = pref.getBoolean("INWORK",false);

        if(intent.getAction()!=null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (isInWork && !LocationServiceHelper.getInstance().isMyServiceRunning(context, RealTimeLocationService.class.getName())) {
                RealTimeLocationTracking.startLocationTracking(new FireBaseRealtimeLocationUpload(),context);
            }
        }
    }
}
