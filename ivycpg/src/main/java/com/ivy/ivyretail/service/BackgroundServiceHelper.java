package com.ivy.ivyretail.service;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class BackgroundServiceHelper {

    private static BackgroundServiceHelper instance = null;
    private BusinessModel bmodel;

    private BackgroundServiceHelper(Context context) {

        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static BackgroundServiceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundServiceHelper(context);
        }
        return instance;
    }

    public void uploadUserLocation(String[] currentLocation){
//        Commons.print("AlarmManager LOCATION "+currentLocation[0]+" - "+currentLocation[1]+" - "+currentLocation[2]);

        try {
            if(currentLocation.length > 0){
                bmodel.saveUserLocation(currentLocation[0],currentLocation[1],currentLocation[2]);

                if (bmodel.isOnline()) {
                    if(bmodel.isUserLocationAvailable()){
                        bmodel.userMasterHelper.downloadUserDetails();
                        bmodel.userMasterHelper.downloadDistributionDetails();
                        bmodel.uploadLocationTracking();
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }

    public UserMasterBO getUserDetail(Context context){
        UserMasterBO userMasterBO = null;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor cursor = db.selectSQL("select userid,username from usermaster where isDeviceuser=1");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                userMasterBO = new UserMasterBO();
                userMasterBO.setUserid(cursor.getInt(0));
                userMasterBO.setUserName(cursor.getString(1));
                cursor.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        db.closeDB();
        return userMasterBO;
    }

}
