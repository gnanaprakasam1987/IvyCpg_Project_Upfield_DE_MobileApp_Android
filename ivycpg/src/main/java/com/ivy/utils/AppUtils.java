package com.ivy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class AppUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private AppUtils() {

    }

    public static String validateInput(String input) {
        String str = "";
        if (input != null && input != "") {
            str = Html.fromHtml(input).toString();
        }
        return str;
    }

    public static String latlongImageFileName;

    public static InputFilter getInputFilter(String regex) {
        InputFilter fil = new InputFilter.LengthFilter(25);
        if (regex != null && !regex.isEmpty()) {
            if (regex.contains("<") && regex.contains(">")) {
                String len = regex.substring(regex.indexOf("<") + 1, regex.indexOf(">"));
                if (!len.isEmpty()) {
                    if (len.contains(",")) {
                        try {
                            fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len.split(",")[1]));
                        } catch (Exception ex) {
                            Commons.printException("regex length split", ex);
                        }
                    } else {
                        fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len));
                    }
                }
            }
        }
        return fil;
    }



    public static String getApplicationVersionName(Context context) {
        String versionName = "";
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return versionName;
    }

    // *****************************************************

    public static String getApplicationVersionNumber(Context context) {
        int versionNumber = 0;
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            versionNumber = pinfo.versionCode;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return versionNumber + "";
    }


    public static void useNetworkProvidedValues(final Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                int i = android.provider.Settings.Global.getInt(
                        context.getContentResolver(),
                        android.provider.Settings.Global.AUTO_TIME);
                if (i == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setIcon(null)
                            .setTitle(context.getResources().getString(R.string.enable_auto_date_time))
                            .setCancelable(false)
                            .setPositiveButton(context.getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            Intent intent = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);

                                        }
                                    });
                    applyAlertDialogTheme(context, builder);

                }
            } else {
                int i = android.provider.Settings.System.getInt(
                        context.getContentResolver(),
                        android.provider.Settings.System.AUTO_TIME);
                if (i == 0) {
                    android.provider.Settings.System.putInt(context.getContentResolver(),
                            android.provider.Settings.System.AUTO_TIME, 1);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public static AlertDialog applyAlertDialogTheme(Context context, AlertDialog.Builder builder) {
        TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        AlertDialog dialog = builder.show();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        int screenWidth = (int) (metrics.widthPixels * 0.80);
        dialog.getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        int alertTitleId = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView alertTitle = dialog.getWindow().getDecorView().findViewById(alertTitleId);
        alertTitle.setTextColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0)); // change title text color

        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        negativeBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        Button postiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        postiveBtn.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        postiveBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        // Set title divider color
        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));

        return dialog;
    }


    public static String convertToSting(Object object) {
        return new Gson().toJson(object);
    }

    public static Object convertToObject(String jsonString, Object object) {
        return new Gson().fromJson(jsonString, object.getClass());
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(HomeScreenActivity.class.getSimpleName(),
                MODE_PRIVATE);
    }


    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /* Checks if all values are null */
    public static boolean isMapEmpty(HashMap<Integer, Integer> aMap) {
        for (Integer v : aMap.values()) {
            if (v != 0) {
                return false;
            }
        }
        return true;
    }
}
