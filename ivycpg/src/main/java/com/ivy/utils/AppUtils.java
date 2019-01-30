package com.ivy.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class AppUtils {


    private AppUtils() {

    }

    public static void checkFileExist(String imageName, String retailerID, boolean isLatLongImage) {
        try {
            String fName = (!isLatLongImage) ? "PRO_" : "LATLONG_" + retailerID;
            File sourceDir = new File(HomeScreenFragment.photoPath);
            File[] files = sourceDir.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(fName) &&
                        !file.getName().equals(imageName))
                    file.delete();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public static String validateInput(String input) {
        String str = "";
        if (input != null && input != "") {
            str = Html.fromHtml(input).toString();
        }
        return str;
    }

    public static boolean isValidRegx(CharSequence target, String regx) {

        if (regx.equals("")) {
            return true;
        }
        String value = regx.replaceAll("\\<.*?\\>", "");
        return !TextUtils.isEmpty(target) && Pattern.compile(value).matcher(target).matches();
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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


    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }

    public static boolean checkImagePresent(String path) {
        File f = new File(path);
        return f.exists();
    }


    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    public static Bitmap decodeFile(File f) {
        int IMAGE_MAX_SIZE = 500;
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return b;
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
        if ((files != null) && (files.length >= 1)) {

            for (File tempFile : files) {
                if (tempFile != null) {
                    if (tempFile.getName().startsWith(fnamesStarts))
                        tempFile.delete();
                }
            }
        }
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


    public static boolean validRegex(String pattern, String str) {

        if (pattern.equals("")) {
            return true;
        }

        Pattern mPattern = Pattern.compile(pattern);
        Matcher matcher = mPattern.matcher(str);

        // Entered text does not match the pattern
        if (!matcher.matches()) {
            // It does not match partially too
            if (!matcher.hitEnd())
                return false;
        }
        return true;
    }

    public static int dpToPx(Context context,double dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return (int)(dp * density);
    }

    /**
     * Open the Image in Photo Gallery while onClick
     *
     * @param fileName File name
     */
    public static void openImage(String fileName,Context context) {
        if (fileName.trim().length() > 0) {
            try {
                Uri path;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(fileName));
                } else {
                    path = Uri.fromFile(new File(fileName));
                }

                intent.setDataAndType(path, "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Commons.printException("" + e);
                Toast.makeText(
                        context,
                        context.getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context,
                    context.getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    }


}
