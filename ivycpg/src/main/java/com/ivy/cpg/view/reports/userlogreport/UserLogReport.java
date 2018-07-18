package com.ivy.cpg.view.reports.userlogreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by abbas.a on 18/07/18.
 */

public class UserLogReport {

    private Context mContext;
    public UserLogReport(Context context){
        mContext=context;
    }

    public ArrayList<LogReportBO> downloadLogReport() {
        ArrayList<LogReportBO> mLogReportList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT TimeIn from AttendanceDetail ");
            sb.append(" Where DateIn = '" + getTodayDate()+"'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName("Day Started");
                    logReportBO.setInTime("");
                    String time[] = c.getString(0).split(" ");
                    logReportBO.setOutTime(time[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }

            c.close();

            // get Break details of the user
            sb = new StringBuffer();
            sb.append("select RM.ListName,AT.inTime,AT.outTime from AttendanceTimeDetails AT ");
            sb.append("inner join StandardListMaster RM on RM.ListId= AT.reasonid ");
            sb.append(" where AT.date = '" + getTodayDate()+"'");

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName(c.getString(0));
                    String[] time1 = c.getString(1).split(" ");
                    logReportBO.setInTime(time1[1]);
                    String[] time2 = c.getString(2).split(" ");
                    logReportBO.setOutTime(time2[1]);
                    logReportBO.setInterval(true);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();

            // get Retailer wise activity log of the of the user
            sb = new StringBuffer();
            sb.append("select distinct RM.RetailerName,OT.TimeIn,OT.TimeOut from OutletTimestamp OT ");
            sb.append("inner join RetailerMaster RM on RM.RetailerID= OT.RetailerID ");
            sb.append(" where OT.VisitDate = '" + getTodayDate()+"'");

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName(c.getString(0));
                    String[] time1 = c.getString(1).split(" ");
                    logReportBO.setInTime(time1[1]);
                    String[] time2 = c.getString(2).split(" ");
                    logReportBO.setOutTime(time2[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();

            sb = new StringBuffer();
            sb.append("SELECT TimeOut from DayClose ");
            sb.append("where status = 1");

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName("Day Closed");
                    logReportBO.setInTime("");
                    String[] time = c.getString(0).split(" ");
                    logReportBO.setOutTime(time[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return mLogReportList;
    }

    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Date date = new Date();
        return formatter.format(date.getTime());
    }
}
