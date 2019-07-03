package com.ivy.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Html;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getSharedPreferenceByName(Context context,String name) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(name, MODE_PRIVATE);
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

    /**
     * Build the url to download using Azure
     * @param appendUrl - file path in download url
     * @return
     */
    public static String buildAzureUrl(String appendUrl){
        return DataMembers.AZURE_CONNECTION_STRING + "/"+DataMembers.AZURE_CONTAINER+"/"+appendUrl+ DataMembers.AZURE_SAS;
    }

    /**
     * This method used re
     * @param context
     * @param imageView
     * @param radius
     * @return
     */
    public static BitmapImageViewTarget getRoundedImageTarget(@NonNull final Context context, @NonNull final ImageView imageView,
                                                              final float radius) {
        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(final Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCornerRadius(radius);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        };
    }

    /**
     * Used to share file's through email
     * @param context - Activity context
     * @param filePath - Path of the file
     * @param subject - Email subject
     * @param recipients - List of recipients to send this mail
     * @param title - Title of the email intent
     */
    public static void sendEmail(Context context, String filePath, String subject, String[] recipients, String title) {
        Uri path;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                } else {
                    path = Uri.fromFile(file);
                }
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("vnd.android.cursor.dir/email");
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_STREAM, path);
                context.startActivity(Intent.createChooser(intent , title));
            }
        } catch (ActivityNotFoundException e) {
            Commons.printException(e);
        }
    }

    /**
     * Hide kye board
     * @param focusedView
     * @param context
     */
    public static void hideKeyboard(View focusedView, Context context) {
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }


    /* For Generating Barcode */
    /**************************************************************
     * getting from com.google.zxing.client.android.encode.QRCodeEncoder
     *
     * See the sites below
     * http://code.google.com/p/zxing/
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/EncodeActivity.java
     * http://code.google.com/p/zxing/source/browse/trunk/android/src/com/google/zxing/client/android/encode/QRCodeEncoder.java
     */

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
