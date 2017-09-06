package com.ivy.sd.png.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

public class ScreenOrientation {
    /**
     * Method tells device factor
     *
     * @param activityContext
     * @return
     */
    public static boolean isTabletDevice(Context activityContext) {
        boolean device_large = ((activityContext.getResources()
                .getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE || (activityContext
                .getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        if (device_large) {
            DisplayMetrics metrics = new DisplayMetrics();
            Activity activity = (Activity) activityContext;
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT
                    || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH
                    || metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM
                    || metrics.densityDpi == DisplayMetrics.DENSITY_TV
                    || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCatalogDevice(Context activityContext) {

        DisplayMetrics metrics = new DisplayMetrics();
        Activity activity = (Activity) activityContext;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (metrics.widthPixels > 480) {
            return true;
        }
        return false;
    }
}
