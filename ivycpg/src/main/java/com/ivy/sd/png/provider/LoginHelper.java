package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

/**
 * Created by subramanian.r on 11-11-2015.
 */
public class LoginHelper {
    private Context context;
    private BusinessModel bmodel;
    private static LoginHelper instance = null;

    private String SENDER_ID = "534457766184";
    private GoogleCloudMessaging gcm;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";


    private LoginHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static LoginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoginHelper(context);
        }
        return instance;
    }

    public void onGCMRegistration(){
        bmodel.regid = getRegistrationId(context);
        Commons.printInformation("REG ID IS : " + bmodel.regid);
        if (bmodel.regid.isEmpty()) {
            if (checkPlayServices()) {
                Commons.print("play true");
                gcm = GoogleCloudMessaging.getInstance(context);
                registerInBackground();
            } else {
                Commons.printInformation("No valid Google Play Services APK found.");
            }
        }
    }
    /**
     * Gets the current registration ID for application on GCM service, if there
     * is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Commons.printInformation("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Commons.printInformation("App version changed.");
            return "";
        }
        Commons.print(registrationId);
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(HomeScreenActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new Resources.NotFoundException("Could not get package name: " + e);
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        Commons.print("check play service");
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                /*GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();*/
            } else {
                Commons.printInformation("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(context);

                    bmodel.regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + bmodel.regid;
                    bmodel.synchronizationHelper.updateAuthenticateToken();
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Commons.printException(ex);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                storeRegistrationId(context, bmodel.regid);
                Commons.print("Google Cloud Registration Message:" + msg);
            }
        }.execute(null, null, null);
    }
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Commons.printInformation("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }


}
