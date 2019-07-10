package com.ivy.cpg.view.stockcheck;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.FitScoreHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

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
    public boolean SHOW_STOCK_BARCODE;
    public boolean SHOW_SHELF_OUTER;
    public boolean SHOW_STOCK_TOTAL;
    public boolean SHOW_STOCK_FC;
    public boolean SHOW_IS_DISTRIBUTED;
    public boolean CHANGE_AVAL_FLOW;
    public boolean SHOW_STOCK_AVGDAYS;

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
    public boolean SHOW_PRICE_CHANGED;

    public boolean SHOW_STOCK_PRICE_TAG_AVAIL;
    public boolean SHOW_STOCK_LOCATION_FILTER;
    public boolean SHOW_COMB_STOCK_PRICE_TAG_AVAIL;
    public boolean SHOW_COMB_LOCATION_FILTER;
    private ProductTaggingHelper productTaggingHelper;


    private StockCheckHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
        productTaggingHelper=ProductTaggingHelper.getInstance(context);
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
        SHOW_IS_DISTRIBUTED = false;
        CHANGE_AVAL_FLOW = false;
        SHOW_STOCK_AVGDAYS = false;
        SHOW_STOCK_BARCODE = false;
        SHOW_STOCK_PRICE_TAG_AVAIL = false;
        SHOW_STOCK_LOCATION_FILTER = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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
                    case "ISDIST":
                        SHOW_IS_DISTRIBUTED = true;
                        break;
                    case "CB01":
                        CHANGE_AVAL_FLOW = true;
                        break;
                    case "AVGDAYS":
                        SHOW_STOCK_AVGDAYS = true;
                        break;
                    case "BARCODE":
                        SHOW_STOCK_BARCODE = true;
                        break;
                    case "PT":
                        SHOW_STOCK_PRICE_TAG_AVAIL = true;
                        break;
                    case "LOC":
                        SHOW_STOCK_LOCATION_FILTER = true;
                        break;


                }
        }
    }

    public void loadCmbStkChkConfiguration(Context context, int subChannelID) {
        SHOW_COMB_STOCK_SP = false;
        SHOW_COMB_STOCK_SC = false;
        SHOW_COMB_STOCK_SHELF_OUTER = false;
        SHOW_COMB_STOCK_CB = false;
        SHOW_STOCK_RSN = false;
        SHOW_STOCK_DD = false;
        SHOW_STOCK_FC = false;
        SHOW_STOCK_LD = false;
        CHANGE_AVAL_FLOW = false;

        SHOW_STOCK_PRICECHECK_PCS = false;
        SHOW_STOCK_PRICECHECK_CS = false;
        SHOW_STOCK_PRICECHECK_OU = false;
        SHOW_STOCK_PRICECHECK_MRP_PCS = false;
        SHOW_STOCK_PRICECHECK_MRP_CS = false;
        SHOW_STOCK_PRICECHECK_MRP_OU = false;
        SHOW_PRICE_CHANGED = false;
        SHOW_COMB_STOCK_PRICE_TAG_AVAIL = false;
        SHOW_COMB_LOCATION_FILTER = false;

        SHOW_STOCK_NEAREXPIRY_PCS = false;
        SHOW_STOCK_NEAREXPIRY_CB = false;
        SHOW_STOCK_NEAREXPIRY_CS = false;
        SHOW_STOCK_NEAREXPIRY_OU = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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
                    case "CB01":
                        CHANGE_AVAL_FLOW = true;
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
                    else if (temp.equals("PT"))
                        SHOW_COMB_STOCK_PRICE_TAG_AVAIL = true;
                    else if (temp.equals("LOC"))
                        SHOW_COMB_LOCATION_FILTER = true;
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

        sql = "select RField from "
                + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode='PRICE_CHANGED' and ForSwitchSeller = 0 and Flag=1";
        c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                SHOW_PRICE_CHANGED = true;
            }
            c.close();
        }

    }

    public boolean hasStockCheck() {

        int siz = productTaggingHelper.getTaggedProducts().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productTaggingHelper.getTaggedProducts().get(i);

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

    boolean isReasonSelectedForAllProducts(boolean isCombinedStock) {
        Vector<ProductMasterBO> productList = (isCombinedStock) ? productTaggingHelper.getTaggedProducts() : bmodel.productHelper.getProductMaster();
        if (productList.size() == 0) return false;
        for (ProductMasterBO product : productList) {
            for (LocationBO location : product.getLocations()) {
                if (SHOW_STOCK_CB && location.getAvailability() == 0) {
                    boolean isQtyAvaiable = ((SHOW_STOCK_SP && location.getShelfPiece() > 0) ||
                            (SHOW_STOCK_SC && location.getShelfCase() > 0) ||
                            (SHOW_SHELF_OUTER && location.getShelfOuter() > 0));
                    if (!isQtyAvaiable && location.getReasonId() == 0) {
                        return false;
                    }
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
        DBUtil db;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            String tid;
            String sql;
            Cursor headerCursor;
            String refId = "0";

            String headerColumns = "Tid, RetailerId, Date, TimeZone, RefId,ridSF,VisitId";
            String detailColumns = "Tid, PId,LocId, UOMId, UOMQty,expdate,retailerid,isOwn";

            String values;
            boolean isData;

            tid = bmodel.getAppDataProvider().getUser().getUserid() + ""
                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + ""
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            // delete transaction if exist
            sql = "SELECT Tid, RefId FROM NearExpiry_Tracking_Header"
                    + " WHERE RetailerId = "
                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID();

            headerCursor = db.selectSQL(sql);

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL("NearExpiry_Tracking_Header", "Tid="
                        + StringUtils.getStringQueryParam(headerCursor.getString(0)), false);
                db.deleteSQL("NearExpiry_Tracking_Detail", "Tid="
                        + StringUtils.getStringQueryParam(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Saving Transaction Detail
            isData = false;
            for (ProductMasterBO skubo : bmodel.productHelper.getProductMaster()) {

                for (int j = 0; j < skubo.getLocations().size(); j++) {

                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpPC().equals("0")) {

                        values = StringUtils.getStringQueryParam(tid)
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
                                + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                + "," + skubo.getOwn();

                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }
                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpOU().equals("0")) {
                        values = StringUtils.getStringQueryParam(tid)
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
                                + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                + "," + skubo.getOwn();

                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }
                    if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                            .getNearexpCA().equals("0")) {
                        values = StringUtils.getStringQueryParam(tid)
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
                                + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                .getTime()))) + ","
                                + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + "," + skubo.getOwn();
                        db.insertSQL("NearExpiry_Tracking_Detail",
                                detailColumns, values);
                        isData = true;
                    }

                }
            }
            if (productTaggingHelper.getTaggedProducts() != null) {
                for (ProductMasterBO skubo : productTaggingHelper.getTaggedProducts()) {
                    if (skubo.getOwn() == 0) {
                        for (int j = 0; j < skubo.getLocations().size(); j++) {

                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpPC().equals("0")) {

                                values = StringUtils.getStringQueryParam(tid)
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
                                        + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                        + "," + skubo.getOwn();

                                db.insertSQL("NearExpiry_Tracking_Detail",
                                        detailColumns, values);
                                isData = true;
                            }
                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpOU().equals("0")) {
                                values = StringUtils.getStringQueryParam(tid)
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
                                        + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                        + "," + skubo.getOwn();

                                db.insertSQL("NearExpiry_Tracking_Detail",
                                        detailColumns, values);
                                isData = true;
                            }
                            if (!skubo.getLocations().get(j).getNearexpiryDate().get(0)
                                    .getNearexpCA().equals("0")) {
                                values = StringUtils.getStringQueryParam(tid)
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
                                        + StringUtils.getStringQueryParam(changeMonthNameToNoyyyymmdd(df.format(c
                                        .getTime()))) + ","
                                        + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + "," + skubo.getOwn();
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
                values = StringUtils.getStringQueryParam(tid) + "," + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                        + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                        + StringUtils.getStringQueryParam(getTimeZone()) + "," + StringUtils.getStringQueryParam(refId) + ","
                        + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                        + bmodel.getAppDataProvider().getUniqueId();

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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update " + DataMembers.tbl_retailerMaster
                    + " set isReviewPlan=" + StringUtils.getStringQueryParam("Y") + " where retailerid="
                    + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void saveClosingStock(Context context, boolean isFromOrder) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            boolean isData;
            String id = StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
            if (bmodel.isEditStockCheck()) {
                Cursor closingStockCursor = db
                        .selectSQL("select StockID from ClosingStockHeader where RetailerID="
                                + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + "");

                if (closingStockCursor.getCount() > 0) {
                    closingStockCursor.moveToNext();
                    id = StringUtils.getStringQueryParam(closingStockCursor.getString(0));
                    db.deleteSQL("ClosingStockHeader", "StockID=" + id, false);
                    db.deleteSQL("ClosingStockDetail", "StockID=" + id, false);
                }
                closingStockCursor.close();
            }

            if (bmodel.PRD_FOR_SKT) // Update is Productive only when the config is enabled.
                updateIsStockCheck(context);


            //Weightage Calculation
            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                FitScoreHelper.getInstance(context).getWeightage(bmodel.getAppDataProvider().getRetailMaster().getRetailerID(), DataMembers.FIT_STOCK);
            }

            String columns, values;

            int productWeightage, sum = 0;

            ProductMasterBO product;

            // ClosingStock Detail entry

            columns = "StockID,Date,ProductID,uomqty,retailerid,uomid,msqqty,Qty,ouomid,ouomqty,"
                    + " Shelfpqty,Shelfcqty,shelfoqty,whpqty,whcqty,whoqty,LocId,isDistributed,isListed,reasonID,isAuditDone,Facing,IsOwn,PcsUOMId,RField1,RField2,RField3,isAvailable,hasPriceTag";

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                columns = columns + ",Score";
            }
            isData = false;
            int siz;

            if (isFromOrder)
                siz = bmodel.productHelper.getProductMaster().size();
            else
                siz = productTaggingHelper.getTaggedProducts().size();

            for (int i = 0; i < siz; ++i) {
                if (isFromOrder)
                    product = bmodel.productHelper.getProductMaster().elementAt(i);
                else
                    product = productTaggingHelper.getTaggedProducts().elementAt(i);

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
                        if (SHOW_STOCK_AVGDAYS) {
                            rField1 = product.getQty_klgs();
                            rField2 = product.getRfield1_klgs();
                            rField3 = product.getRfield2_klgs();

                        }

                        int shelfCase = ((product.getLocations().get(j).getShelfCase() == -1) ? 0 : product.getLocations().get(j).getShelfCase());
                        int shelfPiece = ((product.getLocations().get(j).getShelfPiece() == -1) ? 0 : product.getLocations().get(j).getShelfPiece());
                        int shelfOuter = ((product.getLocations().get(j).getShelfOuter() == -1) ? 0 : product.getLocations().get(j).getShelfOuter());
                        int availability = ((product.getLocations().get(j).getAvailability() == -1) ? 0 : product.getLocations().get(j).getAvailability());
                        values = (id) + ","
                                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                + StringUtils.getStringQueryParam(product.getProductID()) + ","
                                + product.getCaseSize() + ","
                                + StringUtils.getStringQueryParam(bmodel.retailerMasterBO.getRetailerID()) + ","
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
                                + "," + availability
                                + "," + product.getLocations().get(j).getPriceTagAvailability();


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
                columns = "StockID,Date,RetailerID,RetailerCode,remark,DistributorID,AvailabilityShare,ridSF,VisitId";

                values = (id) + ", " + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                        + ", " + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()) + ", "
                        + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRetailerCode()) + ","
                        + StringUtils.getStringQueryParam(bmodel.getStockCheckRemark()) + "," + bmodel.getAppDataProvider().getRetailMaster().getDistributorId();

                if (bmodel.configurationMasterHelper.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) {
                    String availabilityShare = (bmodel.getAvailablilityShare() == null ||
                            bmodel.getAvailablilityShare().trim().length() == 0) ? "0.0" : bmodel.getAvailablilityShare();
                    values = values + "," + StringUtils.getStringQueryParam(availabilityShare);
                } else {
                    values = values + "," + StringUtils.getStringQueryParam("0.0");
                }

                values = values + "," + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                        + bmodel.getAppDataProvider().getUniqueId();

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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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
                    .selectSQL("select Tid from RetailerScoreHeader where RetailerID=" + bmodel.getRetailerMasterBO().getRetailerID() + " and Date = " + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

            if (closingStockCursor.getCount() > 0) {
                closingStockCursor.moveToNext();
                if (closingStockCursor.getString(0) != null) {
                    headerID = StringUtils.getStringQueryParam(closingStockCursor.getString(0));
                    db.deleteSQL("RetailerScoreDetails", "Tid=" + headerID + " and ModuleCode = " + StringUtils.getStringQueryParam(module), false);
                }
            }
            closingStockCursor.close();

            String tid = (headerID.trim().length() == 0) ? StringUtils.getStringQueryParam(bmodel.userMasterHelper.getUserMasterBO().getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)) : headerID;
            int moduleWeightage = FitScoreHelper.getInstance(context).getModuleWeightage(module);
            double achieved = ((sum / (double) 100) * moduleWeightage);
            fitscoreDetailValues = (tid) + ", " + StringUtils.getStringQueryParam(module) + ", " + moduleWeightage + ", " + achieved + ", " + StringUtils.getStringQueryParam("N");
            db.insertSQL(DataMembers.tbl_retailerscoredetail, fitscoreDetailColumns, fitscoreDetailValues);

            if (headerID.trim().length() == 0) {
                String retailerID = bmodel.getRetailerMasterBO().getRetailerID();
                String date = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                fitscoreHeaderValues = (tid) + ", " + StringUtils.getStringQueryParam(retailerID) + ", " + date + ", " + achieved + ", " + StringUtils.getStringQueryParam("N");
                db.insertSQL(DataMembers.tbl_retailerscoreheader, fitscoreHeaderColumns, fitscoreHeaderValues);
            } else {
                Cursor achievedCursor = db
                        .selectSQL("select sum(0+ifnull(B.Score,0)) from RetailerScoreHeader A inner join RetailerScoreDetails B on A.Tid = B.Tid where A.RetailerID="
                                + bmodel.getRetailerMasterBO().getRetailerID() + " and A.Date = " + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

                if (achievedCursor.getCount() > 0) {
                    achievedCursor.moveToNext();
                    headerScore = achievedCursor.getDouble(0);
                }
                achievedCursor.close();
                db.updateSQL("Update " + DataMembers.tbl_retailerscoreheader + " set Score = " + headerScore + " where " +
                        " Date = " + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "" +
                        " and RetailerID = " + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
