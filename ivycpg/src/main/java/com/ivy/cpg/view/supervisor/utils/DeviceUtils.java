package com.ivy.cpg.view.supervisor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;


/**
 * All Device level utility methods are defined here
 */

public class DeviceUtils {

    /**
     * This utility class is not publicly instantiable
     */
    private DeviceUtils() {

    }


    @SuppressLint("MissingPermission")
    public static String getIMEINumber(Context context) {
        String deviceId = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        } catch (Exception e) {
            return "0";
        }
        if (deviceId == null)
            return "0";
        else
            return deviceId;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }




}
