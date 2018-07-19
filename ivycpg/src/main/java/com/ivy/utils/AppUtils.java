package com.ivy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.io.File;

public class AppUtils {


    private AppUtils() {

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
        TextView alertTitle = (TextView) dialog.getWindow().getDecorView().findViewById(alertTitleId);
        alertTitle.setTextColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0)); // change title text color

        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, context));
        negativeBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        Button postiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        postiveBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, context));
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


    /*
     * This method will return total acheived value of the seller for the day.
     * OrderHeader if preseller or InvoiceMaster. Deviated retailer acheived
     * value will not be considered.
     */

    public static String QT(String data) {
        return "'" + data + "'";
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }

    /**
     * To check file availability
     *
     * @param path File path
     * @return Availability
     */
    public static boolean isFileExisting(String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * Getting file URI
     *
     * @param path File path
     * @return URI
     */
    public static Uri getUriFromFile(Context mContext, String path) {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", f);

        } else {
            return Uri.fromFile(f);
        }

    }

    /**
     * @return <code>true<code/> if external storage available else <code>false<code/>
     */

    public static boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true && mbAvailable > 10) {
            return true;
        } else {
            return false;
        }
    }


    /*
     * It returns true if the folder contains the n or more than n files
     * which starts name fnameStarts otherwiese returns false;
     */

    public static boolean checkForNFilesInFolder(String folderPath, int n,
                                                 String fNameStarts) {
        if (n < 1)
            return true;

        File folder = new File(folderPath);
        if (!folder.exists()) {
            return false;
        } else {
            String fnames[] = folder.list();
            if ((fnames == null) || (fnames.length < n)) {
                return false;
            } else {
                int count = 0;
                for (String str : fnames) {
                    if ((str != null) && !fNameStarts.equals("") && (str.length() > 0)) {
                        if (str.startsWith(fNameStarts)) {
                            count++;
                        }
                    }

                    if (count == n) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public static void deleteFiles(String folderPath, String fnamesStarts) {
        File folder = new File(folderPath);

        File files[] = folder.listFiles();
        if ((files == null) || (files.length < 1)) {
            return;
        } else {

            for (File tempFile : files) {
                if (tempFile != null) {
                    if (tempFile.getName().startsWith(fnamesStarts))
                        tempFile.delete();
                }
            }
        }
    }

}
