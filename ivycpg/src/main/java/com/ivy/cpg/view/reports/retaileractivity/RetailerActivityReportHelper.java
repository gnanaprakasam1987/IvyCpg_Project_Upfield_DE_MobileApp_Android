package com.ivy.cpg.view.reports.retaileractivity;

import android.database.Cursor;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by abbas.a on 25/06/18.
 */

public class RetailerActivityReportHelper {

    private BusinessModel businessModel;

    public RetailerActivityReportHelper(BusinessModel context) {
        this.businessModel = context;
    }

    public ArrayList<RetailerMasterBO> downloadRetailerActivityReport() {
        int index = 1;
        ArrayList<RetailerMasterBO> retailer = new ArrayList<>();
        try {
            RetailerMasterBO retailerBo, ret;
            ret = new RetailerMasterBO();

            DBUtil db = new DBUtil(businessModel, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            for (RetailerMasterBO retailerList : businessModel.getRetailerMaster()) {
                if (retailerList.getIsToday() == 1 || (retailerList.getIsDeviated() != null && retailerList.getIsDeviated().equalsIgnoreCase("Y"))) {
                    retailerBo = new RetailerMasterBO();
                    retailerBo.setRetailerID(retailerList.getRetailerID());
                    retailerBo.setRetailerName(retailerList.getRetailerName());
                    retailerBo.setWalkingSequence(index);
                    index++;
                    retailer.add(retailerBo);

                }
            }
            Cursor c = db.selectSQL("select RetailerID, sum(OrderValue),sum(LinesPerCall) from OrderHeader group by retailerid");

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < retailer.size(); i++) {
                        ret = retailer.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setVisit_Actual(c.getDouble(1));
                            ret.setTotalLines(c.getInt(2));
                        }
                    }
                }
                c.close();
            }

            // Below code commented because Retailer Daily Target sets using RetailerKPI table instead of RetailerTargetMaster
            //TODO: Update Target based on KPI Table.

            c = db.selectSQL("select retailerid,substr(timeIn,12,5),substr(timeout,12,5),timein,timeout " +
                    "from OutletTimestamp group by retailerid");

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < retailer.size(); i++) {
                        ret = retailer.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setRField1(c.getString(1));
                            ret.setRfield2(c.getString(2));
                            ret.setRField4(timeDifference(c.getString(3), c.getString(4)));
                        }
                    }
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return retailer;

    }


    public String timeDifference(String sTime, String eTime) {
        String duration = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(sTime);
            d2 = format.parse(eTime);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            //  long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays > 0) {
                duration += diffDays + " ";
            }
            if (diffHours > 0) {
                if (!duration.equals(""))
                    duration += ":";

                duration += diffHours;
            }

            if (diffMinutes > 0) {
                if (!duration.equals(""))
                    duration += ":";
                if (duration.equals(""))
                    duration += "00:";

                duration += diffMinutes;
                int fullLen = duration.length();//1:2
                int len = duration.substring(duration.indexOf(":")).length();

                if (len == 2) {
                    if (fullLen == 3)
                        duration = duration.substring(0, 1) + ":0" + duration.substring(2);
                    else
                        duration = duration.substring(0, 2) + ":0" + duration.substring(3);

                }
            }

            if (duration.equals(""))
                duration = "0";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return duration;
    }

}
