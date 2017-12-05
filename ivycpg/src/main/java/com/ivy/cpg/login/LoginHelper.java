package com.ivy.cpg.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by subramanian.r on 11-11-2015.
 */
public class LoginHelper {
    private final Context context;
    private final BusinessModel businessModel;
    private static LoginHelper instance = null;

    private final String SENDER_ID = "534457766184";
    private GoogleCloudMessaging gcm;
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static final String CODE_PWD_LOCK = "FUN46";
    private static final String CODE_MAXIMUM_ATTEMPTCOUNT = "Max_Login_Attempt_count";
    public boolean IS_PASSWORD_LOCK;
    public int MAXIMUM_ATTEMPT_COUNT = 0;

    private LoginHelper(Context context) {
        this.context = context;
        this.businessModel = (BusinessModel) context;
    }

    public static LoginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoginHelper(context);
        }
        return instance;
    }

    public String getSupportNo() {
        DBUtil db = null;
        String suppot_no = "";

        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            String sb = "select listname from standardlistmaster " +
                    "where listtype= 'HELPLINE_TYPE' and ListCode = 'PHONE'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    suppot_no = c.getString(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
            if (db != null) {
                db.closeDB();
            }
        }
        return suppot_no;
    }

    public void onGCMRegistration() {
        businessModel.regid = getRegistrationId(context);
        Commons.printInformation("REG ID IS : " + businessModel.regid);
        if (businessModel.regid.isEmpty()) {
            if (checkPlayServices()) {
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

    private String getRegistrationId(Context context) {
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
                MODE_PRIVATE);
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
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        /*int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);*/
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!googleApiAvailability.isUserResolvableError(resultCode)) {
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
                String msg;
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(context);

                    businessModel.regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + businessModel.regid;
                    businessModel.synchronizationHelper.updateAuthenticateToken();
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Commons.printException(ex);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                storeRegistrationId(context, businessModel.regid);
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

    public boolean isPasswordReset() {
        boolean isReset = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select IsResetPassword from usermaster where loginid ='" + businessModel.userNameTemp + "' COLLATE NOCASE");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    isReset = c.getInt(0) > 0;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return isReset;
    }

    public void deleteUserMaster() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_userMaster, null, true);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * This method will restore the database saved in External storage into
     * application.
     *
     * @return true - successful and false - failed
     */
    public boolean reStoreDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canRead()) {
                String currentDBPath = "data/com.ivy.sd.png.asean.view/databases/"
                        + DataMembers.DB_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/pandg/" + DataMembers.DB_NAME);

                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(currentDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return true;
                }
            }
        } catch (Exception e) {
            Commons.printException("Synchronisation," + e + "");
        }

        return false;
    }

    /**
     * deleteAllValues will be called before updating the apk. This method will
     * delete the database completely and also update AutoUpdate shared
     * preference.
     */
    public void deleteAllValues() {

        try {
            context.deleteDatabase(DataMembers.DB_NAME);
            businessModel.synchronizationHelper.deleteDBFromSD();
            SharedPreferences pref = context.getSharedPreferences("autoupdate",
                    MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = pref.edit();
            prefsEditor.putString("URL", "");
            prefsEditor.putString("isUpdateExist", "False");
            prefsEditor.apply();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadPasswordConfiguration() {
        DBUtil db;
        db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        StringBuffer sb;
        try {
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(businessModel.QT(CODE_PWD_LOCK));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        sb = new StringBuffer();
                        sb.append("select RField from hhtmodulemaster where hhtcode =");
                        sb.append(businessModel.QT(CODE_MAXIMUM_ATTEMPTCOUNT));
                        sb.append(" and Flag=1");
                        c = db.selectSQL(sb.toString());
                        if (c.getCount() > 0) {
                            if (c.moveToNext()) {

                                MAXIMUM_ATTEMPT_COUNT = c.getInt(0);

                            }
                        }
                        if (MAXIMUM_ATTEMPT_COUNT > 0) {
                            int listid = businessModel.configurationMasterHelper.getActivtyType("RESET_PWD");
                            if (listid != 0)
                                IS_PASSWORD_LOCK = true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            db.closeDB();
        }
    }
}
