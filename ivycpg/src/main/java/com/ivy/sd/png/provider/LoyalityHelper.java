package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoyaltyBO;
import com.ivy.sd.png.bo.LoyaltyBenifitsBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

import static com.ivy.lib.Utils.QT;

/**
 * Created by hanifa.m on 10/18/2016.
 */

public class LoyalityHelper {

    private Context context;
    private BusinessModel bmodel;
    private static LoyalityHelper instance = null;
    public int mSelectedLocationIndex = 0;
    private ArrayList<LoyaltyBenifitsBO> mylist;
    LoyaltyBO ret;
    private Vector<LoyaltyBO> loyaltyproductList;

    String mLoyaltyRedemptionHeader = "LoyaltyRedemptionHeader";
    String mLoyaltyRedemptionDetail = "LoyaltyRedemptionDetail";


    protected LoyalityHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static LoyalityHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoyalityHelper(context);
        }
        return instance;
    }

    //Save Loyalty Points into LoyaltyRedemptionHerad and LoyaltyRedemptionDetail Tables

    public void saveLoyaltyPoints(Vector<LoyaltyBO> loyaltyitems) {

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String uid;
            String sql;
            Cursor headerCursor;
            LoyaltyBO ret;
            String headerColumns = "UID, RetailerId,LoyaltyId,TotalPoints, Date, TimeZone,PointsTypeID";
            String detailColumns = "UID,BenefitId,Qty,Points ";

            String values;


            // delete LoyaltyBenefits if exist
            sql = "SELECT UID FROM " + mLoyaltyRedemptionHeader
                    + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " AND Upload='N'";

            headerCursor = db.selectSQL(sql);

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL(mLoyaltyRedemptionHeader,
                        "UID=" + QT(headerCursor.getString(0)), false);
                db.deleteSQL(mLoyaltyRedemptionDetail,
                        "UID=" + QT(headerCursor.getString(0)), false);
                headerCursor.close();
            }

            for (LoyaltyBO ltyBo : loyaltyitems) {
                uid = bmodel.getRetailerMasterBO().getRetailerID() + ""
                        + ltyBo.getLoyaltyId() + ""
                        + SDUtil.now(SDUtil.DATE_TIME_ID);

                if (ltyBo.getSelectedPoints() > 0) {
                    values = QT(uid) + ","
                            + ltyBo.getRetailerId() + ","
                            + ltyBo.getLoyaltyId() + ","
                            + ltyBo.getSelectedPoints() + ","
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                            + QT(bmodel.getTimeZone()) + ","
                            + ltyBo.getPointTypeId();

                    db.insertSQL(mLoyaltyRedemptionHeader, headerColumns, values);//save into  LoyaltyRedemption Header Table

                }
                //Update Balanced Points into Loyalty Points Table
                db.updateSQL("UPDATE LoyaltyPoints "
                        + "SET BalancePoints ="
                        + (ltyBo.getSelectedPoints() != 0 ? ltyBo.getBalancePoints() : ltyBo.getGivenPoints())
                        + ", Upload = 'N'"
                        + " WHERE LoyaltyId ="
                        + ltyBo.getLoyaltyId()
                        + " AND PointsTypeID=" + ltyBo.getPointTypeId()
                        + " AND RetailerID ="
                        + bmodel.getRetailerMasterBO().getRetailerID());


                for (LoyaltyBenifitsBO lty : ltyBo.getLoyaltyTrackingList()) {
                    if (lty.getBenifitQty() > 0) {
                        values = QT(uid) + ","
                                + lty.getBenifitsId() + ","
                                + lty.getBenifitQty() + ","
                                + lty.getBenifitPoints();
                        db.insertSQL(mLoyaltyRedemptionDetail, detailColumns, values);//save into LoyaltyRedemptionDetail Table
                    }
                }

            }


        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }


    public boolean hasUpdatedLoyalties(String retailerId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql = "select UID from "
                    + mLoyaltyRedemptionHeader + " where RetailerID="
                    + QT(retailerId);
            sql += " AND date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            sql += " and Upload= 'N'";
            Cursor loyaltyHeaderCursor = db.selectSQL(sql);
            if (loyaltyHeaderCursor.getCount() > 0) {
                loyaltyHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                loyaltyHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {

            Commons.printException("hasUpdatedLoyalties", e);
            return false;
        }
    }

    public ArrayList<StandardListBO> downloadLoyaltyPointsType() {
        DBUtil db = null;
        ArrayList<StandardListBO> lstPointTypes = new ArrayList<>();
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String sql = "select listid,listname,listcode from StandardListMaster where listtype='LOYALTY_POINTS_TYPE'";

            Cursor cursor = db.selectSQL(sql);
            if (cursor.getCount() > 0) {
                StandardListBO standardListBO;
                while (cursor.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(cursor.getString(0));
                    standardListBO.setListName(cursor.getString(1));
                    standardListBO.setListCode(cursor.getString(2));

                    lstPointTypes.add(standardListBO);
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }

        return lstPointTypes;
    }


    public void updatedLoyaltyPoints(String retailerId) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String uId = null;
            int ltyId = 0;
            int benefitId = 0;
            int qty = 0;
            int tpoints = 0;
            int pointTypeId = 0;
            Vector<LoyaltyBO> ltyBo = bmodel.productHelper.getProductloyalties();
            String sql1 = "SELECT Distinct LH.UID,LH.LoyaltyId,LH.TotalPoints,LD.BenefitId,LD.Qty,LH.PointsTypeID FROM LoyaltyRedemptionHeader LH INNER JOIN LoyaltyRedemptionDetail LD ON LH.UID = LD.UID"
                    + " WHERE LH.RetailerID =" + retailerId
                    + " AND Date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " AND LH.Upload='N'";

            Cursor cHeader = db.selectSQL(sql1);
            if (cHeader != null) {
                while (cHeader.moveToNext()) {
                    uId = cHeader.getString(0);
                    ltyId = cHeader.getInt(1);
                    tpoints = cHeader.getInt(2);
                    benefitId = cHeader.getInt(3);
                    qty = cHeader.getInt(4);
                    pointTypeId = cHeader.getInt(5);
                    if (ltyBo != null) {
                        for (int i = 0; i < ltyBo.size(); i++) {
                            LoyaltyBO ret = ltyBo.elementAt(i);
                            if (ret.getLoyaltyId() == ltyId && pointTypeId == ret.getPointTypeId()) {
                                ret.setSelectedPoints(tpoints);
                            }
                            for (int j = 0; j < ret.getLoyaltyTrackingList().size(); j++) {
                                if (ret.getLoyaltyId() == ltyId && pointTypeId == ret.getPointTypeId()
                                        && ret.getLoyaltyTrackingList().get(j).getBenifitsId() == benefitId) {

                                    ret.getLoyaltyTrackingList().get(j).setBenifitQty(qty);
                                }
                            }
                        }
                    }
                }
                cHeader.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();

        }


    }


}
