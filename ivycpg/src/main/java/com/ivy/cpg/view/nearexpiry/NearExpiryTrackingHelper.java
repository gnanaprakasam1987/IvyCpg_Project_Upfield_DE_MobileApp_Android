package com.ivy.cpg.view.nearexpiry;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NearExpiryTrackingHelper {

    private final BusinessModel mBModel;
    private static NearExpiryTrackingHelper instance = null;

    private final String mTrackingHeader = "NearExpiry_Tracking_Header";
    private final String mTrackingDetail = "NearExpiry_Tracking_Detail";

    public int mSelectedLocationIndex = 0;
    public String mSelectedLocationName = "";
    private int k = 0;

    public String mSelectedActivityName = "";

    public boolean SHOW_BATCH_NO;


    private NearExpiryTrackingHelper(Context context) {
        this.mBModel = (BusinessModel) context.getApplicationContext();
    }

    public static NearExpiryTrackingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NearExpiryTrackingHelper(context);
        }
        return instance;
    }

    public void clear() {
        instance = null;
    }

    public void loadNearExpiryConfig(Context context) {
        SHOW_BATCH_NO = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='NEXP02' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    SHOW_BATCH_NO = true;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    public void loadLastVisitSKUTracking(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String sql1 = "SELECT ProductId, LocId,expdate, UOMId, Qty,isOwn"
                    + " FROM LastVisitNearExpiry"
                    + " WHERE retailerid = " + mBModel.getRetailerMasterBO().getRetailerID();
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                int curLocId = 0;
                boolean isLocChanged;

                String curDateString = "";
                boolean isDateChanged;
                String lastPid = "";

                while (orderDetailCursor.moveToNext()) {
                    String pid = orderDetailCursor.getString(0);
                    int locationId = orderDetailCursor.getInt(1);
                    String date = orderDetailCursor.getString(2);
                    int uomId = orderDetailCursor.getInt(3);
                    String uomQty = orderDetailCursor.getString(4);

                    isLocChanged = false;
                    isDateChanged = false;

                    if (curLocId != locationId || !lastPid.equals(pid)) {
                        curLocId = locationId;
                        lastPid = pid;
                        k = 0;
                    }


                    setSKUTrackingDetails(mContext,pid, locationId,
                            uomId, uomQty, date, isLocChanged,
                            isDateChanged, false);
                }
                orderDetailCursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public boolean hasAlreadySKUTrackingDone(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql = "select tid from "
                    + mTrackingHeader + " where RetailerID="
                    + mBModel.getRetailerMasterBO().getRetailerID();
            sql += " AND date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            sql += " and (upload='N' OR refid!=0)";
            Cursor orderHeaderCursor = db.selectSQL(sql);
            if (orderHeaderCursor.getCount() > 0) {
                orderHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                orderHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {
            Commons.printException("hasAlreadySKUTrackinDone", e);
            return false;
        }
    }

    /**
     * Load SKU from Detail Table
     */
    public void loadSKUTracking(Context mContext, boolean isTaggedProduct) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            k = -1;
            String tid;

            String sb = "SELECT Tid FROM " + mTrackingHeader + " WHERE retailerid = "
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + " and (upload='N' OR refid!=0)";
            // Get Tid From Header


            Cursor orderHeaderCursor = db.selectSQL(sb);
            tid = "";
            if (orderHeaderCursor != null && orderHeaderCursor.moveToNext()) {
                tid = orderHeaderCursor.getString(0);
                orderHeaderCursor.close();
            }

            String sql1 = "SELECT PId, LocId,expdate, UOMId, UOMQty,IFNULL(isAuditDone,'2'),isOwn"
                    + " FROM "
                    + mTrackingDetail
                    + " WHERE Tid = "
                    + QT(tid) + " order by pid, locid, expdate";
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {


                int curLocId = 0;
                boolean isLocChanged;
                String lastPid = "";

                String curDateString = "";
                boolean isDateChanged;

                while (orderDetailCursor.moveToNext()) {
                    String pid = orderDetailCursor.getString(0);
                    int locationId = orderDetailCursor.getInt(1);
                    String date = orderDetailCursor.getString(2);
                    int uomId = orderDetailCursor.getInt(3);
                    String uomQty = orderDetailCursor.getString(4);


                    isLocChanged = false;
                    isDateChanged = false;

					if (curLocId != locationId || !lastPid.equals(pid)) {
                        curLocId = locationId;
                        lastPid = pid;
						isLocChanged = true;

						curDateString = date;

                        if (!curDateString.equals(date)) {
                            curDateString = date;
                            isDateChanged = true;
                        }

					} else if (!curDateString.equals(date)) {
							curDateString = date;
							isDateChanged = true;
					}

                    /*if (curLocId != locationId || !lastPid.equals(pid)) {
                        curLocId = locationId;
                        lastPid = pid;
                        k = 0;
                    }*/

                    setSKUTrackingDetails(mContext,pid, locationId,
                            uomId, uomQty, date, isLocChanged,
                            isDateChanged, isTaggedProduct);
                }
                orderDetailCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Set the Tracking Detail
     *
     * @param pid
     * @param --availabilty
     * @param locationId
     * @param uomId
     * @param uomQty
     */
    private void setSKUTrackingDetails(Context context,String pid, int locationId,
                                       int uomId, String uomQty, String date,
                                       boolean isLocChanged, boolean isDateChanged, boolean isTaggedProduct) {
        ProductMasterBO productBO;

        if (isTaggedProduct) {
            productBO = ProductTaggingHelper.getInstance(context).getTaggedProductBOById(pid);
        } else {
            productBO = mBModel.productHelper.getProductMasterBOById(pid);
        }
        if (productBO != null) {
            for (int j = 0; j < productBO.getLocations().size(); j++) {
                if (productBO.getLocations().get(j).getLocationId() == locationId) {

                    if (isLocChanged) {
                        k = 0;
                    }

                    if (isDateChanged) {
                        k++;
                    }

                    productBO.getLocations().get(j).getNearexpiryDate()
                            .get(k).setDate(changeMonthNoToName(date));

                    if (productBO.getPcUomid() == uomId)
                        productBO.getLocations().get(j).getNearexpiryDate()
                                .get(k).setNearexpPC(uomQty);
                    if (productBO.getOuUomid() == uomId)
                        productBO.getLocations().get(j).getNearexpiryDate()
                                .get(k).setNearexpOU(uomQty);
                    if (productBO.getCaseUomId() == uomId)
                        productBO.getLocations().get(j).getNearexpiryDate()
                                .get(k).setNearexpCA(uomQty);
                    //k++;

                    return;
                }
            }
        }


    }

    /**
     * Save Tracking Detail in Detail Table
     */
    public void saveSKUTracking(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String tid;
            String sql;
            Cursor headerCursor;
            String refId = "0";

            String headerColumns = "Tid, RetailerId, Date, TimeZone, RefId";
            String detailColumns = "Tid, PId,LocId, UOMId, UOMQty,expdate,retailerid,isAuditDone,batchNumber";


            String values;
            boolean isData;


            tid = mBModel.userMasterHelper.getUserMasterBO().getUserid()
                    + ""
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + ""
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            // delete transaction if exist
            sql = "SELECT Tid, RefId FROM "
                    + mTrackingHeader
                    + " WHERE RetailerId = "
                    + mBModel.getRetailerMasterBO().getRetailerID();
            sql += " and (upload='N' OR refid!=0)";


            headerCursor = db.selectSQL(sql);

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL(mTrackingHeader,
                        "Tid=" + QT(headerCursor.getString(0)), false);
                db.deleteSQL(mTrackingDetail,
                        "Tid=" + QT(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Saving Transaction Detail
            isData = false;
            for (ProductMasterBO skubo : mBModel.productHelper.getProductMaster()) {

                for (int j = 0; j < skubo.getLocations().size(); j++) {

                    for (int k = 0; k < (skubo.getLocations()
                            .get(j).getNearexpiryDate().size()); k++) {

                        if (!"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpPC()) || skubo.getLocations()
                                .get(mSelectedLocationIndex).getAudit() != 2) {

                            values = QT(tid)
                                    + ","
                                    + skubo.getProductID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getLocationId()
                                    + ","
                                    + skubo.getPcUomid()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k)
                                    .getNearexpPC()
                                    + ","
                                    + QT(changeMonthNameToNoyyyymmdd(skubo
                                    .getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k).getDate()))
                                    + ","
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j).getAudit()
                                    + ","
                                    + QT(skubo.getLocations().get(j).getNearexpiryDate().get(k).getBatchNo());

                            db.insertSQL(mTrackingDetail,
                                    detailColumns, values);
                            isData = true;
                        }
                        if (!"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpOU()) || skubo.getLocations()
                                .get(mSelectedLocationIndex).getAudit() != 2) {
                            values = QT(tid)
                                    + ","
                                    + skubo.getProductID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getLocationId()
                                    + ","
                                    + skubo.getOuUomid()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k)
                                    .getNearexpOU()
                                    + ","
                                    + QT(changeMonthNameToNoyyyymmdd(skubo
                                    .getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k).getDate()))
                                    + ","
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j).getAudit()
                                    + ","
                                    + QT(skubo.getLocations().get(j).getNearexpiryDate().get(k).getBatchNo());

                            db.insertSQL(mTrackingDetail,
                                    detailColumns, values);
                            isData = true;
                        }
                        if (!"0"
                                .equals(skubo.getLocations().get(j)
                                        .getNearexpiryDate()
                                        .get(k).getNearexpCA()) || skubo.getLocations()
                                .get(mSelectedLocationIndex).getAudit() != 2) {
                            values = QT(tid)
                                    + ","
                                    + skubo.getProductID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getLocationId()
                                    + ","
                                    + skubo.getCaseUomId()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k)
                                    .getNearexpCA()
                                    + ","
                                    + QT(changeMonthNameToNoyyyymmdd(skubo
                                    .getLocations()
                                    .get(j)
                                    .getNearexpiryDate()
                                    .get(k).getDate()))
                                    + ","
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID()
                                    + ","
                                    + skubo.getLocations()
                                    .get(j).getAudit()
                                    + ","
                                    + QT(skubo.getLocations().get(j).getNearexpiryDate().get(k).getBatchNo());
                            db.insertSQL(mTrackingDetail,
                                    detailColumns, values);
                            isData = true;
                        }
                    }

                }
            }

            // Saving Transaction Header if There is Any Detail
            if (isData) {
                values = QT(tid)
                        + ","
                        + mBModel.getRetailerMasterBO().getRetailerID()
                        + ","
                        + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                        + QT(DateTimeUtils.getTimeZone()) + "," + QT(refId);

                db.insertSQL(mTrackingHeader, headerColumns, values);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public boolean checkDataToSave() {

        for (ProductMasterBO skubo : mBModel.productHelper.getProductMaster()) {
            for (int j = 0; j < skubo.getLocations().size(); j++) {
                for (int k = 0; k < (skubo.getLocations().get(j)
                        .getNearexpiryDate().size()); k++) {

                    if (mBModel.configurationMasterHelper.isAuditEnabled()){
                        if (
                                (
                                (!"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                                .get(k).getNearexpPC())
                                || !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                                .get(k).getNearexpOU())
                                || !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                                .get(k).getNearexpCA()))
                                && skubo.getLocations()
                                .get(mSelectedLocationIndex).getAudit() != 2
                                )
                                || skubo.getLocations()
                                .get(mSelectedLocationIndex).getAudit() != 2){

                            return true;

                        }
                    }else if (!"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                            .get(k).getNearexpPC())
                            || !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                            .get(k).getNearexpOU())
                            || !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
                            .get(k).getNearexpCA())
                            || skubo.getLocations()
                            .get(mSelectedLocationIndex).getAudit() != 2)
                        return true;
                }
            }
        }
        return false;
    }

    public String dateformat(int year, int monthOfYear, int dayOfMonth) {
        String month;
        String day;

        if (monthOfYear + 1 < 9)
            month = "0" + (monthOfYear + 1);
        else
            month = Integer.toString(monthOfYear + 1);

        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;
        else
            day = Integer.toString(dayOfMonth);

        return year + "/" + month + "/" + day;

    }

    public String changeMonthNameToNoyyyymmdd(String date) {

        if (null != date && !"".equals(date))
            try {
                String[] dat = date.split(" ");

                SimpleDateFormat cf = new SimpleDateFormat("dd/MMM/yyyy",
                        Locale.ENGLISH);
                Date dt = cf.parse(dat[0]);
                SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd",
                        Locale.ENGLISH);
                return sf.format(dt);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        return "";
    }

    public String changeMonthNoToName(String date) {

        if (null != date && !"".equals(date))
            try {
                String[] dat = date.split(" ");

                SimpleDateFormat cf = new SimpleDateFormat("yyyy/MM/dd",
                        Locale.ENGLISH);
                Date dt = cf.parse(dat[0]);
                SimpleDateFormat sf = new SimpleDateFormat("dd/MMM/yyyy",
                        Locale.ENGLISH);
                return sf.format(dt);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        return "";
    }

    public String changeDate(String date) {

        if (null != date && !"".equals(date))
            try {
                String[] dat = date.split(" ");

                SimpleDateFormat cf = new SimpleDateFormat("yyyy/MM/dd",
                        Locale.ENGLISH);
                Date dt = cf.parse(dat[0]);
                SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy",
                        Locale.ENGLISH);
                return sf.format(dt);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        return "";
    }

    public String changeMonthNameToNommddyyyy(String date) {

        if (null != date && !"".equals(date))
            try {
                String[] dat = date.split(" ");

                SimpleDateFormat cf = new SimpleDateFormat("dd/MMM/yyyy",
                        Locale.ENGLISH);
                Date dt = cf.parse(dat[0]);
                SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy",
                        Locale.ENGLISH);
                return sf.format(dt);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        return "";
    }

    private String QT(String data) {
        return "'" + data + "'";
    }
}
