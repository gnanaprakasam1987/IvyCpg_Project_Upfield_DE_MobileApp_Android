package com.ivy.cpg.view.reports.asset;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by abbas.a on 17/07/18.
 */

public class AssetTrackingReportsHelper {

    private Context mContext;

    public AssetTrackingReportsHelper(Context context){
        mContext =  context;
    }



    public ArrayList<RetailerNamesBO> downloadAssetTrackingRetailerMaster() {
        ArrayList<RetailerNamesBO> assetRetailerList = new ArrayList<>();
        try {
            RetailerNamesBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.RetailerID,B.RetailerName from SOD_Assets_Detail A inner join RetailerMaster B on A.RetailerID = B.RetailerID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                assetRetailerList = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new RetailerNamesBO();
                    orderreport.setRetailerId(c.getInt(0));
                    orderreport.setRetailerName(c.getString(1));
                    assetRetailerList.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return assetRetailerList;
    }

    public ArrayList<AssetTrackingBrandBO> downloadAssetTrackingBrandMaster() {
        ArrayList<AssetTrackingBrandBO> assetBrandList =new ArrayList<>();
        try {
            AssetTrackingBrandBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.ProductID,B.PName from SOD_Assets_Detail A " +
                    "inner join ProductMaster B on A.ProductID = B.PID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {

                while (c.moveToNext()) {
                    orderreport = new AssetTrackingBrandBO();
                    orderreport.setBrandID(c.getInt(0));
                    orderreport.setBrandName(c.getString(1));
                    assetBrandList.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return assetBrandList;
    }

    public ArrayList<AssetTrackingReportBO> downloadAssetTrackingreport(int retailerID, int brandID) {
        ArrayList<AssetTrackingReportBO> assetTrackingReportBOArrayList = null;
        try {
            AssetTrackingReportBO assetTrackingReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.PosmDesc,D.PName,ifnull(B.Target,0),ifnull(B.Actual,0),ifnull(C.ListName,'') from PosmMaster A ");
            sb.append("inner join SOD_Assets_Detail B on A.PosmID = B.AssetID ");
            sb.append("left join StandardListMaster C on C.ListID = B.ReasonID and C.ListType = 'REASON' ");
            sb.append("inner join ProductMaster D on B.ProductID = D.PID ");
            sb.append("inner join RetailerMaster E on B.RetailerID = E.RetailerID ");
            sb.append("where E.RetailerID = '" + retailerID + "' ");
            if (brandID != 0) {
                sb.append("and B.ProductID = '" + brandID + "'");
            }

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                assetTrackingReportBOArrayList = new ArrayList<>();
                while (c.moveToNext()) {
                    assetTrackingReportBO = new AssetTrackingReportBO();
                    assetTrackingReportBO.setAssetDescription(c.getString(0));
                    assetTrackingReportBO.setBrandname(c.getString(1));
                    assetTrackingReportBO.setTarget(c.getString(2));
                    assetTrackingReportBO.setActual(c.getString(3));
                    assetTrackingReportBO.setReason(c.getString(4));
                    assetTrackingReportBOArrayList.add(assetTrackingReportBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return assetTrackingReportBOArrayList;
    }
}
