package com.ivy.cpg.primarysale.provider;

import android.content.Context;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

/**
 * Created by dharmapriya.k on 25-09-2015.
 */
public class DistTimeStampHeaderHelper {

    private static DistTimeStampHeaderHelper instance = null;
    private Context context;
    private BusinessModel bmodel;
    private String uid = new String();
    // timein is used to update timeout
    private String timeIn, timeInModuleWise;

    protected DistTimeStampHeaderHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static DistTimeStampHeaderHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DistTimeStampHeaderHelper(context);
        }
        return instance;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public String QT(String data) {
        return "'" + data + "'";
    }

    /**
     * Used to set Time Stamp.
     *
     * @param date
     * @param timeIn
     */

    public void saveTimeStamp(String date, String timeIn) {
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = " UId , DistId , Date , TimeIn , TimeOut ,Latitude,Longitude,DownloadedDate,Upload";
            setUid(QT("DTS" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)));


            String values = getUid() + ","
                    + QT(bmodel.distributorMasterHelper.getDistributor().getDId()) + "," + QT(date)
                    + ","
                    + QT(date + " " + timeIn) + "," + QT(date + " " + timeIn)
                    + "," + QT(LocationUtil.latitude + "") + ","
                    + QT(LocationUtil.longitude + "") + ","
                    +QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate())+","
                    + QT("N");

            db.insertSQL(DataMembers.tbl_DistTimeStampHeader, columns, values);


            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    /**
     * Set Time Out
     *
     * @param timeOut
     */
    public void updateTimeStamp(String timeOut) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String dateTime = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL) + " " + timeOut;
            String query = "UPDATE " + DataMembers.tbl_DistTimeStampHeader + " SET TimeOut = '" + dateTime
                    + "' WHERE DistId = '"
                    + bmodel.distributorMasterHelper.getDistributor().getDId()
                    + "' AND TimeIn = '" + getTimeIn() + "'";
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Used to set Time Stamp.
     *
     * @param date
     * @param timeIn
     */

    public void saveTimeStampModuleWise(String date, String timeIn, String moduleCode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = " UId , Date , MenuCode , TimeIn , TimeOut , Upload";
            timeInModuleWise = QT(date + " " + timeIn);
            String values = getUid() + ","
                    + QT(date) + ","
                    + QT(moduleCode) + ","
                    + timeInModuleWise + "," + timeInModuleWise
                    + ","
                    + QT("N");
            db.insertSQL(DataMembers.tbl_DistTimeStampDetails, columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    /**
     * Set Time Out
     *
     * @param timeOut
     */
    public void updateTimeStampModuleWise(String timeOut) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String dateTime = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL) + " " + timeOut;
            String query = "UPDATE " + DataMembers.tbl_DistTimeStampDetails + " SET TimeOut = '" + dateTime
                    + "'  WHERE TimeIn = " + timeInModuleWise + " AND UID = " + getUid();
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }


}
