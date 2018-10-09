package com.ivy.cpg.view.stockcheck;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.FitScoreHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mansoor on 03/10/2018
 */
public class StockCheckHelper {

    private static StockCheckHelper instance = null;
    private final BusinessModel bmodel;

    public boolean SHOW_STOCK_SP;
    public boolean SHOW_STOCK_SC;
    public boolean SHOW_STOCK_CB;
    public boolean SHOW_STOCK_RSN;
    public boolean SHOW_SHELF_OUTER;
    public boolean SHOW_STOCK_TOTAL;
    public boolean SHOW_STOCK_FC;
    public boolean CHANGE_AVAL_FLOW;

    public boolean SHOW_COMB_STOCK_SC;
    public boolean SHOW_COMB_STOCK_SP;
    public boolean SHOW_COMB_STOCK_SHELF_OUTER;
    public boolean SHOW_COMB_STOCK_CB;

    public boolean SHOW_STOCK_LD;// Listed checkbox
    public boolean SHOW_STOCK_DD;// Distributes=d checkbox

    public boolean SHOW_STOCK_PRICECHECK_PCS;
    public boolean SHOW_STOCK_PRICECHECK_CS;
    public boolean SHOW_STOCK_PRICECHECK_OU;
    public boolean SHOW_STOCK_PRICECHECK_MRP_PCS;
    public boolean SHOW_STOCK_PRICECHECK_MRP_CS;
    public boolean SHOW_STOCK_PRICECHECK_MRP_OU;

    public boolean SHOW_STOCK_NEAREXPIRY_PCS;
    public boolean SHOW_STOCK_NEAREXPIRY_CB;
    public boolean SHOW_STOCK_NEAREXPIRY_CS;
    public boolean SHOW_STOCK_NEAREXPIRY_OU;


    private StockCheckHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static StockCheckHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StockCheckHelper(context);
        }
        return instance;
    }

    public void loadStockCheckConfiguration(Context context, int subChannelID) {


        SHOW_STOCK_SP = false;
        SHOW_STOCK_SC = false;
        SHOW_STOCK_CB = false;
        SHOW_STOCK_RSN = false;
        SHOW_SHELF_OUTER = false;
        SHOW_STOCK_TOTAL = false;
        SHOW_STOCK_FC = false;
        CHANGE_AVAL_FLOW = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        String codeValue = null;
        String sql = "select RField from "
                + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode='CSSTK01' and SubchannelId="
                + subChannelID;
        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                codeValue = c.getString(0);
            }
            c.close();
        } else {
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='CSSTK01' and SubChannelId= 0 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

        }
        if (codeValue != null) {

            String codeSplit[] = codeValue.split(",");
            for (String temp : codeSplit)
                switch (temp) {
                    case "SP":
                        SHOW_STOCK_SP = true;
                        break;
                    case "SC":
                        SHOW_STOCK_SC = true;
                        break;
                    case "SHO":
                        SHOW_SHELF_OUTER = true;
                        break;
                    case "CB":
                        SHOW_STOCK_CB = true;
                        break;
                    case "REASON":
                        SHOW_STOCK_RSN = true;
                        break;
                    case "TOTAL":
                        SHOW_STOCK_TOTAL = true;
                        break;
                    case "FC":
                        SHOW_STOCK_FC = true;
                        break;
                    case "CB01":
                        CHANGE_AVAL_FLOW = true;
                        break;


                }
        }
    }

    public void loadCmbStkChkConfiguration(Context context, int subChannelID) {
        SHOW_COMB_STOCK_SP = true;
        SHOW_COMB_STOCK_SC = true;
        SHOW_COMB_STOCK_SHELF_OUTER = true;
        SHOW_COMB_STOCK_CB = true;

        SHOW_STOCK_PRICECHECK_PCS = false;
        SHOW_STOCK_PRICECHECK_CS = false;
        SHOW_STOCK_PRICECHECK_OU = false;
        SHOW_STOCK_PRICECHECK_MRP_PCS = false;
        SHOW_STOCK_PRICECHECK_MRP_CS = false;
        SHOW_STOCK_PRICECHECK_MRP_OU = false;

        SHOW_STOCK_NEAREXPIRY_PCS = false;
        SHOW_STOCK_NEAREXPIRY_CB = false;
        SHOW_STOCK_NEAREXPIRY_CS = false;
        SHOW_STOCK_NEAREXPIRY_OU = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        String codeValue = null;
        String sql = "select RField from "
                + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode='CSSTK08' and SubchannelId="
                + subChannelID;
        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                codeValue = c.getString(0);
            }
            c.close();
        } else {
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='CSSTK08' and SubChannelId= 0 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

        }
        if (codeValue != null) {

            String codeSplit[] = codeValue.split(",");
            for (String temp : codeSplit)
                switch (temp) {
                    case "CSP":
                        SHOW_COMB_STOCK_SP = true;
                        break;
                    case "CSC":
                        SHOW_COMB_STOCK_SC = true;
                        break;
                    case "CSHO":
                        SHOW_COMB_STOCK_SHELF_OUTER = true;
                        break;
                    case "CCB":
                        SHOW_COMB_STOCK_CB = true;
                        break;
                    case "LM":
                        SHOW_STOCK_LD = true;
                        break;
                    case "DB":
                        SHOW_STOCK_DD = true;
                        break;
                    case "FC":
                        SHOW_STOCK_FC = true;
                        break;
                    case "REASON":
                        SHOW_STOCK_RSN = true;
                        break;

                }
        }

        if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
            codeValue = null;

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='CSSTK02' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("PS"))
                        SHOW_STOCK_PRICECHECK_PCS = true;
                    else if (temp.equals("OU"))
                        SHOW_STOCK_PRICECHECK_OU = true;
                    else if (temp.equals("CS"))
                        SHOW_STOCK_PRICECHECK_CS = true;
                    else if (temp.equals("MPS"))
                        SHOW_STOCK_PRICECHECK_MRP_PCS = true;
                    else if (temp.equals("MOU"))
                        SHOW_STOCK_PRICECHECK_MRP_OU = true;
                    else if (temp.equals("MCS"))
                        SHOW_STOCK_PRICECHECK_MRP_CS = true;
                }
            }
        } // if scheme is on disable product wise discount
        if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK) {
            codeValue = null;

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='ORDB46' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("PS"))
                        SHOW_STOCK_NEAREXPIRY_PCS = true;
                    else if (temp.equals("CB"))
                        SHOW_STOCK_NEAREXPIRY_CB = true;
                    else if (temp.equals("OU"))
                        SHOW_STOCK_NEAREXPIRY_OU = true;
                    else if (temp.equals("CS"))
                        SHOW_STOCK_NEAREXPIRY_CS = true;
                }
            }
        }
    }

    public boolean hasStockCheck() {

        int siz = bmodel.productHelper.getTaggedProducts().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper
                    .getTaggedProducts().get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if ((SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                        || (SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                        || (SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                        || (SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)
                        || product.getLocations().get(j).getWHPiece() > 0
                        || product.getLocations().get(j).getWHCase() > 0
                        || product.getLocations().get(j).getWHOuter() > 0
                        || product.getLocations().get(j).getCockTailQty() > 0
                        || product.getIsListed() > 0
                        || product.getIsDistributed() > 0
                        || product.getLocations().get(j).getReasonId() != 0)
                    return true;
            }
        }
        return false;
    }

    public boolean isReasonSelectedForAllProducts() {

        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper
                    .getProductMaster().get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getIsFocusBrand() == 1 || product.getIsFocusBrand() == 2) {
                    if ((SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() == -1)
                            && (SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() == -1)
                            && (SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() == -1)
                            && (SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() == 0)
                            && product.getLocations().get(j).getReasonId() == 0)
                        return false;
                }
            }
        }
        return true;
    }

    public void saveNearExpiry(Context context) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 30);
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String tid;
            String sql;
            Cursor headerCursor;
            String refId = "0";

            String headerColumns = "Tid, RetailerId, Date, TimeZone, RefId";
            String detailColumns = "Tid, PId,LocId, UOMId, UOMQty,expdate,retailerid,isOwn";

            String values;
            boolean isData;

            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + bmodel.getRetailerMasterBO().getRetailerID() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // delete transaction if exist
            sql = "SELECT Tid, RefId FROM NearExpiry_Tracking_Header"
                    + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID();

            headerCursor = db.selectSQL(sql);

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL("NearExpiry_Tracking_Header", "Tid="
                        + AppUtils.QT(headerCursor.getString(0)), false);
                db.deleteSQL("NearExpiry_Tracking_Detail", "Tid="
                        + AppUtils.QT(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Saving Transaction Detail
            isData = false;
            for (ProductMasterBO skubo : bmodel.productHelper.getProductMaster()) {

                for (int j = 0; j < skubo.getLocations().size(); j++) {

                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpPC().equals("0")) {

                        values = AppUtils.QT(tid)
                                + ","
                                + skubo.getProductID()
                                + ","
                                + skubo.getLocations().get(j).getLocationId()
                                + ","
                                + skubo.getPcUomid()
                                + ","
                                + skubo.getLocations().get(j)
                                .getNearexpiryDate().get(0)
                                .getNearexpPC()
                                + ","
                                + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + skubo.getOwn();

                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }
                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpOU().equals("0")) {
                        values = AppUtils.QT(tid)
                                + ","
                                + skubo.getProductID()
                                + ","
                                + skubo.getLocations().get(j).getLocationId()
                                + ","
                                + skubo.getOuUomid()
                                + ","
                                + skubo.getLocations().get(j)
                                .getNearexpiryDate().get(0)
                                .getNearexpOU()
                                + ","
                                + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + skubo.getOwn();

                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }
                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpCA().equals("0")) {
                        values = AppUtils.QT(tid)
                                + ","
                                + skubo.getProductID()
                                + ","
                                + skubo.getLocations().get(j).getLocationId()
                                + ","
                                + skubo.getCaseUomId()
                                + ","
                                + skubo.getLocations().get(j)
                                .getNearexpiryDate().get(0)
                                .getNearexpCA()
                                + ","
                                + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getRetailerMasterBO().getRetailerID() + "," + skubo.getOwn();
                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }

                }
            }
            if (bmodel.productHelper.getTaggedProducts() != null) {
                for (ProductMasterBO skubo : bmodel.productHelper.getTaggedProducts()) {
                    if (skubo.getOwn() == 0) {
                        for (int j = 0; j < skubo.getLocations().size(); j++) {

                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpPC().equals("0")) {

                                values = AppUtils.QT(tid)
                                        + ","
                                        + skubo.getProductID()
                                        + ","
                                        + skubo.getLocations().get(j).getLocationId()
                                        + ","
                                        + skubo.getPcUomid()
                                        + ","
                                        + skubo.getLocations().get(j)
                                        .getNearexpiryDate().get(0)
                                        .getNearexpPC()
                                        + ","
                                        + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getRetailerMasterBO().getRetailerID()
                                        + "," + skubo.getOwn();

                                db.insertSQL("NearExpiry_Tracking_Detail",
                                        detailColumns, values);
                                isData = true;
                            }
                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpOU().equals("0")) {
                                values = AppUtils.QT(tid)
                                        + ","
                                        + skubo.getProductID()
                                        + ","
                                        + skubo.getLocations().get(j).getLocationId()
                                        + ","
                                        + skubo.getOuUomid()
                                        + ","
                                        + skubo.getLocations().get(j)
                                        .getNearexpiryDate().get(0)
                                        .getNearexpOU()
                                        + ","
                                        + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getRetailerMasterBO().getRetailerID()
                                        + "," + skubo.getOwn();

                                db.insertSQL("NearExpiry_Tracking_Detail",
                                        detailColumns, values);
                                isData = true;
                            }
                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpCA().equals("0")) {
                                values = AppUtils.QT(tid)
                                        + ","
                                        + skubo.getProductID()
                                        + ","
                                        + skubo.getLocations().get(j).getLocationId()
                                        + ","
                                        + skubo.getCaseUomId()
                                        + ","
                                        + skubo.getLocations().get(j)
                                        .getNearexpiryDate().get(0)
                                        .getNearexpCA()
                                        + ","
                                        + AppUtils.QT(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getRetailerMasterBO().getRetailerID() + "," + skubo.getOwn();
                                db.insertSQL("NearExpiry_Tracking_Detail",
                                        detailColumns, values);
                                isData = true;
                            }

                        }
                    }
                }
            }

            // Saving Transaction Header if There is Any Detail
            if (isData) {
                values = AppUtils.QT(tid) + "," + bmodel.getRetailerMasterBO().getRetailerID()
                        + "," + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + AppUtils.QT(getTimeZone()) + "," + AppUtils.QT(refId);

                db.insertSQL("NearExpiry_Tracking_Header", headerColumns,
                        values);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private String changeMonthNameToNoyyyymmdd(String date) {
        // Logs.debug("Utils", date);

        if (null != date)
            if (!date.equals("")) {
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

            }
        // return DateFormat.getDateInstance().format(new Date(date));

        return "";
    }

    private String getTimeZone() {
        try {
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT,
                    Locale.ENGLISH);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return "UTC";
    }

    /**
     * Set Review plan in DB. This will update the isReviewPlan field in
     * Retailermaster to 'Y'
     */
    public void setReviewPlanInDB(Context context) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update " + DataMembers.tbl_retailerMaster
                    + " set isReviewPlan=" + AppUtils.QT("Y") + " where retailerid="
                    + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void saveClosingStock(Context context, boolean isFromOrder) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            boolean isData;
            String id = AppUtils.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));
            if (bmodel.isEditStockCheck()) {
                Cursor closingStockCursor = db
                        .selectSQL("select StockID from ClosingStockHeader where RetailerID="
                                + bmodel.getRetailerMasterBO().getRetailerID() + "");

                if (closingStockCursor.getCount() > 0) {
                    closingStockCursor.moveToNext();
                    id = AppUtils.QT(closingStockCursor.getString(0));
                    db.deleteSQL("ClosingStockHeader", "StockID=" + id, false);
                    db.deleteSQL("ClosingStockDetail", "StockID=" + id, false);
                }
                closingStockCursor.close();
            }

            if (bmodel.PRD_FOR_SKT) // Update is Productive only when the config is enabled.
                updateIsStockCheck(context);


            //Weightage Calculation
            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                FitScoreHelper.getInstance(context).getWeightage(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_STOCK);
            }

            String columns, values;

            int moduleWeightage = 0, productWeightage = 0, sum = 0;

            ProductMasterBO product;

            // ClosingStock Detail entry

            columns = "StockID,Date,ProductID,uomqty,retailerid,uomid,msqqty,Qty,ouomid,ouomqty,"
                    + " Shelfpqty,Shelfcqty,shelfoqty,whpqty,whcqty,whoqty,LocId,isDistributed,isListed,reasonID,isDone,Facing,IsOwn,PcsUOMId,RField1,RField2,RField3,isAvailable";

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                columns = columns + ",Score";
            }
            isData = false;
            int siz;

            if (isFromOrder)
                siz = bmodel.productHelper.getProductMaster().size();
            else
                siz = bmodel.productHelper.getTaggedProducts().size();

            for (int i = 0; i < siz; ++i) {
                if (isFromOrder)
                    product = bmodel.productHelper.getProductMaster().elementAt(i);
                else
                    product = bmodel.productHelper.getTaggedProducts().elementAt(i);

                int dd = product.getIsDistributed();
                int ld = product.getIsListed();

                int siz1 = product.getLocations().size();
                for (int j = 0; j < siz1; j++) {
                    if ((SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                            || (SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                            || (SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                            || (SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)
                            || product.getLocations().get(j).getWHPiece() > 0
                            || product.getLocations().get(j).getWHCase() > 0
                            || product.getLocations().get(j).getWHOuter() > 0
                            || product.getLocations().get(j).getIsPouring() > 0
                            || product.getLocations().get(j).getCockTailQty() > 0
                            || product.getLocations().get(j).getFacingQty() > 0
                            || product.getLocations().get(j).getAudit() != 2
                            || product.getLocations().get(j).getReasonId() != 0) {

                        int count = product.getLocations().get(j)
                                .getShelfPiece()
                                + product.getLocations().get(j).getWHPiece();
                        int rField1 = product.getLocations().get(j).getIsPouring();
                        int rField2 = product.getLocations().get(j).getIsPouring();
                        int rField3 = 0;
                        if (bmodel.configurationMasterHelper.SHOW_STOCK_AVGDAYS) {
                            rField1 = product.getQty_klgs();
                            rField2 = product.getRfield1_klgs();
                            rField3 = product.getRfield2_klgs();

                        }

                        int shelfCase = ((product.getLocations().get(j).getShelfCase() == -1) ? 0 : product.getLocations().get(j).getShelfCase());
                        int shelfPiece = ((product.getLocations().get(j).getShelfPiece() == -1) ? 0 : product.getLocations().get(j).getShelfPiece());
                        int shelfOuter = ((product.getLocations().get(j).getShelfOuter() == -1) ? 0 : product.getLocations().get(j).getShelfOuter());
                        int availability = ((product.getLocations().get(j).getAvailability() == -1) ? 0 : product.getLocations().get(j).getAvailability());
                        values = (id) + ","
                                + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                + AppUtils.QT(product.getProductID()) + ","
                                + product.getCaseSize() + ","
                                + AppUtils.QT(bmodel.retailerMasterBO.getRetailerID()) + ","
                                + product.getCaseUomId() + ","
                                + product.getMSQty() + "," + count + ","
                                + product.getOuUomid() + ","
                                + product.getOutersize() + ","
                                + shelfPiece
                                + ","
                                + shelfCase
                                + ","
                                + shelfOuter
                                + ","
                                + product.getLocations().get(j).getWHPiece()
                                + ","
                                + product.getLocations().get(j).getWHCase()
                                + ","
                                + product.getLocations().get(j).getWHOuter()
                                + ","
                                + product.getLocations().get(j).getLocationId()
                                + "," + dd + "," + ld + ","
                                + product.getLocations().get(j).getReasonId() + ","
                                + product.getLocations().get(j).getAudit()
                                + ","
                                + product.getLocations().get(j).getFacingQty()
                                + "," + product.getOwn()
                                + "," + product.getPcUomid()
                                + "," + rField1
                                + "," + rField2
                                + "," + rField3
                                + "," + availability;


                        if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                            int pieces = (shelfCase * product.getCaseSize())
                                    + (shelfOuter * product.getOutersize())
                                    + shelfPiece;
                            productWeightage = FitScoreHelper.getInstance(context).checkWeightage(product.getProductID(), pieces);
                            values = values + "," + productWeightage;
                            sum = sum + productWeightage;
                        }

                        db.insertSQL(DataMembers.tbl_closingstockdetail,
                                columns, values);
                        isData = true;
                    }
                }
            }

            // ClosingStock Header entry
            if (isData) {
                columns = "StockID,Date,RetailerID,RetailerCode,remark,DistributorID,AvailabilityShare";

                values = (id) + ", " + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ", " + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ", "
                        + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerCode()) + ","
                        + AppUtils.QT(bmodel.getStockCheckRemark()) + "," + bmodel.getRetailerMasterBO().getDistributorId();

                if (bmodel.configurationMasterHelper.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) {
                    String availabilityShare = (bmodel.getAvailablilityShare() == null ||
                            bmodel.getAvailablilityShare().trim().length() == 0) ? "0.0" : bmodel.getAvailablilityShare();
                    values = values + "," + AppUtils.QT(availabilityShare);
                } else {
                    values = values + "," + AppUtils.QT("0.0");
                }

                db.insertSQL(DataMembers.tbl_closingstockheader, columns, values);

                if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                    calculateFitscoreandInsert(db, sum, DataMembers.FIT_STOCK, context);
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void updateIsStockCheck(Context context) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.updateSQL("Update RetailerBeatMapping set isProductive='Y' where RetailerID ="
                    + bmodel.getRetailerMasterBO().getRetailerID() + " and BeatID=" + bmodel.getRetailerMasterBO().getBeatID());

            db.closeDB();

            // update loaded retailerMaster flag.
            int siz = bmodel.getRetailerMaster().size();
            for (int i = 0; i < siz; i++) {
                RetailerMasterBO ret = bmodel.retailerMaster.get(i);
                if (ret.getRetailerID().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())) {
                    ret.setProductive("Y");
                }
            }

            // Updated selected object flag
            bmodel.getRetailerMasterBO().setProductive("Y");

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void calculateFitscoreandInsert(DBUtil db, double sum, String module, Context context) {
        String headerID = "";
        double headerScore = 0;
        String fitscoreHeaderColumns = "Tid,RetailerID,Date,Score,Upload";
        String fitscoreHeaderValues = "";
        String fitscoreDetailColumns = "Tid, ModuleCode,Weightage,Score,Upload";
        String fitscoreDetailValues = "";

        try {
            Cursor closingStockCursor = db
                    .selectSQL("select Tid from RetailerScoreHeader where RetailerID=" + bmodel.getRetailerMasterBO().getRetailerID() + " and Date = " + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (closingStockCursor.getCount() > 0) {
                closingStockCursor.moveToNext();
                if (closingStockCursor.getString(0) != null) {
                    headerID = AppUtils.QT(closingStockCursor.getString(0));
                    db.deleteSQL("RetailerScoreDetails", "Tid=" + headerID + " and ModuleCode = " + AppUtils.QT(module), false);
                }
            }
            closingStockCursor.close();

            String tid = (headerID.trim().length() == 0) ? AppUtils.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID)) : headerID;
            int moduleWeightage = FitScoreHelper.getInstance(context).getModuleWeightage(module);
            double achieved = ((sum / (double) 100) * moduleWeightage);
            fitscoreDetailValues = (tid) + ", " + AppUtils.QT(module) + ", " + moduleWeightage + ", " + achieved + ", " + AppUtils.QT("N");
            db.insertSQL(DataMembers.tbl_retailerscoredetail, fitscoreDetailColumns, fitscoreDetailValues);

            if (headerID.trim().length() == 0) {
                String retailerID = bmodel.getRetailerMasterBO().getRetailerID();
                String date = AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
                fitscoreHeaderValues = (tid) + ", " + AppUtils.QT(retailerID) + ", " + date + ", " + achieved + ", " + AppUtils.QT("N");
                db.insertSQL(DataMembers.tbl_retailerscoreheader, fitscoreHeaderColumns, fitscoreHeaderValues);
            } else {
                Cursor achievedCursor = db
                        .selectSQL("select sum(0+ifnull(B.Score,0)) from RetailerScoreHeader A inner join RetailerScoreDetails B on A.Tid = B.Tid where A.RetailerID="
                                + bmodel.getRetailerMasterBO().getRetailerID() + " and A.Date = " + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

                if (achievedCursor.getCount() > 0) {
                    achievedCursor.moveToNext();
                    headerScore = achievedCursor.getDouble(0);
                }
                achievedCursor.close();
                db.updateSQL("Update " + DataMembers.tbl_retailerscoreheader + " set Score = " + headerScore + " where " +
                        " Date = " + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "" +
                        " and RetailerID = " + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
