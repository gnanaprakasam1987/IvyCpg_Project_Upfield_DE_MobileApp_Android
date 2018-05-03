package com.ivy.cpg.locationservice.movementtracking;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ActivationHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class MovementTrackingUploadHelper {


    private static MovementTrackingUploadHelper instance = null;

    private MovementTrackingUploadHelper() {
    }

    public static MovementTrackingUploadHelper getInstance() {
        if (instance == null) {
            instance = new MovementTrackingUploadHelper();
        }
        return instance;
    }

    UserMasterBO downloadUserDetails(Context context) {

        UserMasterBO userMasterBO = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
    void saveUserLocation(Context ctx, LocationDetailBO locationDetailBO, UserMasterBO userMasterBO) {
        DBUtil db;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String columns = "Tid,Date,Latitude,Longtitude,Accuracy,Activity,Battery,LocationProvider,IsLocationEnabled";

            String Tid = userMasterBO.getUserid() + ""+ SDUtil.now(SDUtil.DATE_TIME_ID);

            String values = QT(Tid) + "," + QT(SDUtil.now(SDUtil.DATE_TIME))
                    + "," + QT(String.valueOf(locationDetailBO.getLatitude()))
                    + "," + QT(String.valueOf(locationDetailBO.getLongitude()))
                    + "," + locationDetailBO.getAccuracy()
                    + "," + QT(String.valueOf(locationDetailBO.getActivityType()))
                    + "," + locationDetailBO.getBatteryStatus()
                    + "," + QT(String.valueOf(locationDetailBO.getProvider()))
                    + "," + QT(String.valueOf(locationDetailBO.isGpsEnabled()));

            db.insertSQL("LocationTracking", columns, values);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    boolean isUserLocationAvailable(Context ctx) {
        DBUtil db ;
        boolean isAvail = false;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT Tid FROM LocationTracking where upload = 'N'");

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

    void uploadLocationTracking(Context ctx,UserMasterBO userMasterBO) {

        DBUtil db = null;
        try {

            SynchronizationHelper synchronizationHelper = SynchronizationHelper.getInstance(ctx);
            ActivationHelper activationHelper = ActivationHelper.getInstance(ctx);

            db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            JSONObject jsonObjData;

            Set<String> keys = DataMembers.uploadLocationTrackingColumn
                    .keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForLocationTrackingUploadJSON(
                        db, tableName,
                        DataMembers.uploadLocationTrackingColumn.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

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
                jsonFormatter.addParameter("DeviceId",activationHelper.getIMEINumber());
                jsonFormatter.addParameter("VersionCode",getApplicationVersionNumber(ctx));
                jsonFormatter.addParameter("OrganisationId", userMasterBO.getOrganizationId());
                jsonFormatter.addParameter("MobileDate", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("MobileUTCDateTime",Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("DownloadedDataDate",userMasterBO.getDownloadDate());
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
            String url = synchronizationHelper.getUploadUrl("UPLDTRAN");
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
                if (!synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    String errorMsg = synchronizationHelper.getErrormessageByErrorCode().get(synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Commons.print("errorMsg "+errorMsg);
//                        Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Commons.print("errorMsg "+ctx.getResources().getString(R.string.data_not_downloaded));
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
                    db.executeQ("DELETE FROM LocationTracking");
                    db.closeDB();
                } catch (Exception e) {
                    Commons.printException(e);
                }

            }else
                db.closeDB();

        } catch (Exception e) {
            if(db!=null)
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


    boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;

    }

    private String getApplicationVersionName(Context context) {
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

    private String getApplicationVersionNumber(Context context) {
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

    public String QT(String data) {
        return "'" + data + "'";
    }
}
