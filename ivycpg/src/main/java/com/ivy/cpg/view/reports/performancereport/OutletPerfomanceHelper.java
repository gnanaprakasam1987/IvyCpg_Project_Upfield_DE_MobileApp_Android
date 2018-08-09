package com.ivy.cpg.view.reports.performancereport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by Hanifa on 24/7/18.
 */

public class OutletPerfomanceHelper {
    private BusinessModel bModel;
    private Context mContext;
    private static OutletPerfomanceHelper instance = null;

    protected OutletPerfomanceHelper(Context context) {
        mContext = context;
        bModel = (BusinessModel) context.getApplicationContext();
    }

    public static OutletPerfomanceHelper getInstance(Context context) {
        if (instance == null)
            instance = new OutletPerfomanceHelper(context);
        return instance;
    }


    public ArrayList<OutletReportBO> downloadOutletReports() {
        ArrayList<OutletReportBO> lst = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct UseriD,UserName,Retailerid,RetailerName,LocationName,Address,isPlanned,isVisited");
            sb.append(",TimeIn,TimeOut,Duration,SalesValue,VisitedLat,VisitedLong,SalesVolume,FitScore from OutletPerfomanceReport order by UseriD,timein,timeout");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                OutletReportBO outletReportBO;
                while (c.moveToNext()) {
                    outletReportBO = new OutletReportBO();
                    outletReportBO.setUserId(c.getInt(0));
                    outletReportBO.setUserName(c.getString(1));
                    outletReportBO.setRetailerId(c.getInt(2));
                    outletReportBO.setRetailerName(c.getString(3));
                    outletReportBO.setLocationName(c.getString(4));
                    outletReportBO.setAddress(c.getString(5));
                    outletReportBO.setIsPlanned(c.getInt(6));
                    outletReportBO.setIsVisited(c.getInt(7));
                    outletReportBO.setTimeIn(c.getString(8));
                    outletReportBO.setTimeOut(c.getString(9));
                    outletReportBO.setDuration(c.getString(10));
                    outletReportBO.setSalesValue(c.getString(11));
                    outletReportBO.setLatitude(c.getDouble(12));
                    outletReportBO.setLongitude(c.getDouble(13));
                    outletReportBO.setSalesVolume(c.getString(14));
                    outletReportBO.setFitScore(c.getString(15));
                    lst.add(outletReportBO);

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return lst;

    }


    public ArrayList<OutletReportBO> getLstUsers() {
        return lstUsers;
    }

    private ArrayList<OutletReportBO> lstUsers;

    public ArrayList downloadUsers() {
        lstUsers = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct UseriD,UserName from OutletPerfomanceReport");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                OutletReportBO outletReportBO;
                while (c.moveToNext()) {
                    outletReportBO = new OutletReportBO();
                    outletReportBO.setUserId(c.getInt(0));
                    outletReportBO.setUserName(c.getString(1));
                    outletReportBO.setChecked(true);// to show all user by default
                    lstUsers.add(outletReportBO);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return lstUsers;

    }


    public Integer downloadlastVisitedRetailer(int userId) {
        int retailerid = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct Retailerid from OutletPerfomanceReport where userid=" + userId + " order by timein,timeout  desc limit 1");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    retailerid = c.getInt(0);

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return retailerid;

    }


    public boolean isPerformReport() {
        boolean isAvailable = false;

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select UseriD from OutletPerfomanceReport");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getCount() > 0) {
                        isAvailable = true;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return isAvailable;
    }

    public String getPerformRptUrl() {
        String url = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select URL from UrlDownloadMaster where MasterName = 'RPT_RETAILER_PERFORMANCE'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    url = c.getString(0);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return url;
    }

    public String mSelectedActivityName;

}
