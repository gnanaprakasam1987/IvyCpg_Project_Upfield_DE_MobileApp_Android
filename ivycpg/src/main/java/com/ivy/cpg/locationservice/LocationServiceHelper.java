package com.ivy.cpg.locationservice;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.core.IvyConstants;
import com.ivy.cpg.locationservice.movementtracking.MovementTrackingAlarmReceiver;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import static android.content.Context.LOCATION_SERVICE;
import static com.ivy.cpg.locationservice.LocationConstants.GPS_NOTIFICATION_ID;
import static com.ivy.cpg.locationservice.LocationConstants.MOCK_NOTIFICATION_ID;
import static com.ivy.utils.AppUtils.getApplicationVersionName;
import static com.ivy.utils.AppUtils.getApplicationVersionNumber;
import static com.ivy.utils.StringUtils.getStringQueryParam;

public class LocationServiceHelper {

    private static LocationServiceHelper instance = null;

    private LocationServiceHelper() {
    }

    public static LocationServiceHelper getInstance() {
        if (instance == null) {
            instance = new LocationServiceHelper();
        }
        return instance;
    }

    //Check whether service is running or nt
    public boolean isMyServiceRunning(Context context,
                                      String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    /**
     * Return true if Google play services available in device
     */
    public boolean isGooglePlayServicesAvailable(Context context) {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == status;

    }

    //Checks whether if location accuracy is not set as high
    public boolean isLocationHighAccuracyEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        }
        return true;
    }

    //Check whether Mock Location is enabled or not
    public boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        return (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"));
    }

    //Check GPS is Enabled or not
    public boolean isGpsEnabled(Context context) {
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (service != null)
            return (service.isProviderEnabled(LocationManager.GPS_PROVIDER));

        return false;
    }

    //Check Location Permissions are granted or not
    public boolean hasLocationPermissionEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        boolean coarsePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean finePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }


    /*Notifies the user, if GPS not enabled send user to the GPS settings
     * Also cancel the notification if permission enabled
     */
    public boolean notifyGPSStatus(Context context) {

        if (!LocationServiceHelper.getInstance().isGpsEnabled(context)) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "GPS ENABLED")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.enable_gps))
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_gps_disabled);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(GPS_NOTIFICATION_ID, builder.build());

            return false;
        } else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null)
                nMgr.cancel(GPS_NOTIFICATION_ID);

            return true;
        }
    }

    /*Notifies the user, if Mock Location is enabled send user to the settings
     *Also cancel the notification if permission enabled
     */
    public boolean notifyMockLocationStatus(Context context, Location location) {

        boolean isMock;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && location != null)
            isMock = !location.isFromMockProvider();
        else
            isMock = LocationServiceHelper.getInstance().isMockSettingsON(context);

        if (!isMock) {

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MOCK LOCATION")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Disable mock Location")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_warning);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(MOCK_NOTIFICATION_ID, builder.build());

            return true;
        } else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null)
                nMgr.cancel(MOCK_NOTIFICATION_ID);

            return false;
        }
    }

    /**
     * Get current battery percentage
     */
    public int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    /**
     * Call Alarm manager and set Alarm for Every X minutes
     */
    public void setPendingIntent(Context context, int alarm_time) {

        Intent i = new Intent(context, MovementTrackingAlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(context, 1001, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long timeInMillis = System.currentTimeMillis() + (alarm_time * 60 * 1000);
//        Commons.print("AlarmManager Time in millis--> " + timeInMillis);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            }
        }
    }

    /**
     * returns interval value to set in alarm
     */
    public int getAlarmTime(Context context) {
        int timeInHrs = getCurrentTimeInHrs();
        int timeInMins = getCurrentTimeInMints();

        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);

        int alarm_time = pref.getInt("AlarmTime", 0);
        int start_Time = pref.getInt("StartTime", 0);
        int end_Time = pref.getInt("EndTime", 0);

        if (timeInHrs < start_Time) {
            alarm_time = (start_Time - timeInHrs) * 60 - timeInMins;
        } else if (timeInHrs >= end_Time) {
            alarm_time = ((24 - timeInHrs) + start_Time) * 60 - timeInMins;
        }

        return alarm_time;
    }


    /**
     * Get the Current hour of the day
     *
     * @return in 24hr format
     */
    public int getCurrentTimeInHrs() {
        try {
            Calendar calendar = new GregorianCalendar();
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return 0;
    }

    /**
     * Get the Current Minute
     *
     * @return in minutes
     */
    public int getCurrentTimeInMints() {
        try {
            Calendar calendar = new GregorianCalendar();
            return calendar.get(Calendar.MINUTE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return 0;
    }


    /**
     * Cancel the registered Alaram class from Alaram manager
     */
    public void cancelAlarm(Context context, Class aClass) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, aClass);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        /* cancel any pending alarm */
        if (alarm != null)
            alarm.cancel(pi);
    }


    public UserMasterBO downloadUserDetails(Context context) {

        UserMasterBO userMasterBO = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select " + DataMembers.tbl_userMaster_cols
                    + " from Usermaster where isDeviceUser=1");

            if (c != null) {
                if (c.moveToLast()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setDistributorid(c.getInt(c
                            .getColumnIndex("distributorid")));
                    userMasterBO.setBranchId(c.getInt(c
                            .getColumnIndex("branchid")));
                    userMasterBO.setVanId(c.getInt(c
                            .getColumnIndex("vanid")));
                    userMasterBO.setUserid(c.getInt(c
                            .getColumnIndex("userid")));
                    userMasterBO.setLoginName(c.getString(c
                            .getColumnIndex("loginid")));
                    userMasterBO.setDownloadDate(c.getString(c
                            .getColumnIndex("downloaddate")));
                    userMasterBO.setOrganizationId(c.getInt(c
                            .getColumnIndex("OrganisationId")));
                    userMasterBO.setBackupSellerID(c.getString(c
                            .getColumnIndex("BackupUserId")));
                    userMasterBO.setBackup(false);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return userMasterBO;
    }


    /**
     * Upload Transaction Sequence Table after Data Upload through seperate
     * method name Returns the response Success/Failure
     */
    public void saveUserLocation(Context ctx, LocationDetailBO locationDetailBO, UserMasterBO userMasterBO) {
        DBUtil db;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();

            String columns = "Tid,Date,Latitude,Longtitude,Accuracy,Activity,Battery,LocationProvider,IsLocationEnabled";

            String Tid = userMasterBO.getUserid() + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String values = getStringQueryParam(Tid) + "," + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getLatitude()))
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getLongitude()))
                    + "," + locationDetailBO.getAccuracy()
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getActivityType()))
                    + "," + locationDetailBO.getBatteryStatus()
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getProvider()))
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.isGpsEnabled()));

            db.insertSQL("LocationTracking", columns, values);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean isUserLocationAvailable(Context ctx, String tableName) {
        DBUtil db;
        boolean isAvail = false;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT count(*) FROM " + tableName + "  where upload = 'N'");

            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    isAvail = true;
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

        return isAvail;
    }

    public void uploadLocationTrackingAws(Context ctx, UserMasterBO userMasterBO,
                                          String uploadName, boolean isRealTime) {
        DBUtil db = null;
        try {

            SynchronizationHelper synchronizationHelper = SynchronizationHelper.getInstance(ctx);

            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            JSONObject jsonObjData;

            Set<String> keys;
            String tableName ="";

            if (isRealTime)
                tableName = DataMembers.tbl_movement_tracking_history;
            else
                tableName = DataMembers.tbl_location_tracking;

            jsonObjData = new JSONObject();

            JSONArray jsonArray = prepareDataForLocationTrackingUploadJSON(
                    db, tableName,
                    isRealTime?DataMembers.uploadMovementTrackingHistoryColumn.get(tableName):DataMembers.uploadLocationTrackingColumn.get(tableName));

            if (jsonArray.length() > 0)
                jsonObjData.put(tableName, jsonArray);

            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");
            try {
                if (!"0".equals(userMasterBO.getBackupSellerID())) {
                    jsonFormatter.addParameter("UserId", userMasterBO.getBackupSellerID());
                    jsonFormatter.addParameter("WorkingFor", userMasterBO.getUserid());
                } else {
                    jsonFormatter.addParameter("UserId", userMasterBO.getUserid());
                }
                jsonFormatter.addParameter("DistributorId", userMasterBO.getDistributorid());
                jsonFormatter.addParameter("BranchId", userMasterBO.getBranchId());
                jsonFormatter.addParameter("LoginId", userMasterBO.getLoginName());
                jsonFormatter.addParameter("DeviceId", DeviceUtils.getIMEINumber(ctx));
                jsonFormatter.addParameter("VersionCode", getApplicationVersionNumber(ctx));
                jsonFormatter.addParameter("OrganisationId", userMasterBO.getOrganizationId());
                jsonFormatter.addParameter("MobileDate", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("MobileUTCDateTime", Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("DownloadedDataDate", userMasterBO.getDownloadDate());
                jsonFormatter.addParameter("VanId", userMasterBO.getVanId());
                String LastDayClose = "";
                if (synchronizationHelper.isDayClosed()) {
                    LastDayClose = userMasterBO.getDownloadDate();
                }
                jsonFormatter.addParameter("LastDayClose", LastDayClose);
                jsonFormatter.addParameter("DataValidationKey", synchronizationHelper.generateChecksum(jsonObjData.toString()));
                jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, getApplicationVersionName(ctx));

                Commons.print(jsonFormatter.getDataInJson());
            } catch (Exception e) {
                Commons.printException(e);
            }

            String url = synchronizationHelper.getUploadUrl(uploadName);

            Vector<String> responseVector = synchronizationHelper
                    .getUploadResponse(jsonFormatter.getDataInJson(),
                            jsonObjData.toString(), url);

            int response = 0;

            if (responseVector.size() > 0) {


                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Response")) {
                            response = jsonObject.getInt("Response");

                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObject.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                response = 9;

                            }

                        }
                    }
                }
            } else {
                if (!synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    String errorMsg = synchronizationHelper.getErrormessageByErrorCode().get(synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Commons.print("errorMsg " + errorMsg);
//                        Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Commons.print("errorMsg " + ctx.getResources().getString(R.string.data_not_downloaded));
//                        Toast.makeText(ctx, ctx.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }
           /* if (responseVector != null) {

                for (String s : responseVector) {
                    JSONObject responseObject = new JSONObject(s);
                    response = responseObject.getInt("Response");
                }
            }*/

            if (response == 1) {

                System.gc();
                try {
                    db.executeQ("DELETE FROM "+tableName);
                    db.closeDB();
                } catch (Exception e) {
                    Commons.printException(e);
                }

            } else
                db.closeDB();

        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException(e);
        }

    }

    private JSONArray prepareDataForLocationTrackingUploadJSON(DBUtil db,
                                                               String tableName, String columns) {
        JSONArray ohRowsArray = new JSONArray();
        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");
            String sql = "select " + columns + " from " + tableName
                    + " where upload = 'N'";
            cursor = db.selectSQL(sql);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObjRow = new JSONObject();
                        int count = 0;
                        for (String col : columnArray) {
                            String value = cursor.getString(count);
                            jsonObjRow.put(col, value);
                            count++;
                        }
                        ohRowsArray.put(jsonObjRow);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Commons.printException(e);

        }
        return ohRowsArray;
    }


    /**
     * Upload Transaction Sequence Table after Data Upload through seperate
     * method name Returns the response Success/Failure
     */
    public void saveUserRealtimeLocation(Context ctx, LocationDetailBO locationDetailBO, UserMasterBO userMasterBO) {
        DBUtil db;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();

            String columns = "userid,latitude,longitude,date_time";

            String values = getStringQueryParam(userMasterBO.getUserid() + "")
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getLatitude()))
                    + "," + getStringQueryParam(String.valueOf(locationDetailBO.getLongitude()))
                    + "," + System.currentTimeMillis();
            db.insertSQL("MovementTrackingHistory", columns, values);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
