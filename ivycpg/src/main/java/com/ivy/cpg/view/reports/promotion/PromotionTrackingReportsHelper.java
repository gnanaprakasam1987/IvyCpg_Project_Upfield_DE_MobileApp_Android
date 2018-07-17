package com.ivy.cpg.view.reports.promotion;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by abbas.a on 17/07/18.
 * on
 */

public class PromotionTrackingReportsHelper {

    private Context mContext;

    public PromotionTrackingReportsHelper(Context context){
        mContext = context;
    }

    public ArrayList<PromotionTrackingReportBO> downloadPromotionTrackingreport(int retailerID) {
        ArrayList<PromotionTrackingReportBO> reportordbooking = null;
        try {
            PromotionTrackingReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT D.PSName as PSName, A.PromoName, Case When (B.IsExecuted =1) then 'YES' else 'NO' End as IsExecuted, Case When (B.HasAnnouncer =1) then ");
            sb.append("'YES' else 'NO' End as HasAnnouncer, ifNull(C.ListName,'') as Reason  FROM PromotionProductMapping A ");
            sb.append("inner join PromotionDetail B on A.PID = B.BrandID ");
            sb.append("left join StandardListMaster C on C.ListID = B.ReasonID and C.ListType = 'REASON' ");
            sb.append("inner join ProductMaster D on D.Pid = B.BrandID ");
            sb.append("inner join RetailerMaster E on B.RetailerID = E.RetailerID Where E.RetailerID='" + retailerID + "'");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new PromotionTrackingReportBO();
                    orderreport.setBrandName(c.getString(0));
                    orderreport.setPromoName(c.getString(1));
                    orderreport.setIsExecuted(c.getString(2));
                    orderreport.setHasAnnouncer(c.getString(3));
                    orderreport.setReason(c.getString(4));
                    reportordbooking.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportordbooking;
    }

    /**
     * Get List of retailer for which promotion is done.
     * @return List<RetailerNamesBO>
     */
    public ArrayList<RetailerNamesBO> downloadPromotionTrackingRetailerMaster() {
        ArrayList<RetailerNamesBO> retailerNamesList = null;
        try {
            RetailerNamesBO retailerNamesBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.RetailerID, A.RetailerName from RetailerMaster A " +
                    "inner join PromotionHeader B on A.RetailerID = B.RetailerID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                retailerNamesList = new ArrayList<>();
                while (c.moveToNext()) {
                    retailerNamesBO = new RetailerNamesBO();
                    retailerNamesBO.setRetailerId(c.getInt(0));
                    retailerNamesBO.setRetailerName(c.getString(1));
                    retailerNamesList.add(retailerNamesBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return retailerNamesList;
    }
}
