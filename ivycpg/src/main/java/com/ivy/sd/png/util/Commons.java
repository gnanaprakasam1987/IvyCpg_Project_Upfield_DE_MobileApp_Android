package com.ivy.sd.png.util;

import android.util.Log;

import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.model.ApplicationConfigs;



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Commons {
    public static final int DATE_TIME = 5;
    public static final int DATE = 6;

    public static void print( String message) {
        if (BuildConfig.DEBUG)
            Log.d(ApplicationConfigs.LOG, message);
    }

    public static void printException( String message, Throwable e) {
        Log.e(ApplicationConfigs.LOG, message, e);
    }

    public static void printException(Throwable e) {
        Log.e(ApplicationConfigs.LOG, "~", e);
    }

    public static void printException(String message) {
        Log.e(ApplicationConfigs.LOG, message);
    }

    public static void printInformation(String message) {
        if (BuildConfig.DEBUG)
            Log.i(ApplicationConfigs.LOG, message);
    }

    /**
     * @param dateFormat - to be converted
     * @return formatted date
     */
    public static String now(int dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf;
        if (DATE_TIME == dateFormat)
            sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        else if (DATE == dateFormat)
            sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        else
            sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

        return sdf.format(cal.getTime());
    }

    //Get First Day of the month
    public static String getFirstDayOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,
                Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",
                Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }


    public static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }
}
