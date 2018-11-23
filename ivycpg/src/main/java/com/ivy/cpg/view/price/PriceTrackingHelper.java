package com.ivy.cpg.view.price;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PriceTrackingHelper {
    private Context context;
    private final BusinessModel bmodel;
    private static PriceTrackingHelper instance = null;

    private final String mPriceChangeHeader = "PriceCheckHeader";
    private final String mPriceChangeDetail = "PriceCheckDetail";
    public int mSelectedFilter = -1;

    public boolean SHOW_PRICE_CA;
    public boolean SHOW_PRICE_PC;
    public boolean SHOW_PRICE_OU;
    public boolean SHOW_PRICE_SRP;
    public boolean SHOW_PRICE_CHANGED;
    public boolean SHOW_PRICE_COMPLIANCE;
    public boolean SHOW_PRICE_LASTVP;
    public int IS_PRICE_CHANGE_REASON = 0;

    // 0 - product ,1 - Competitor product , 2 - Product & Competitior product
    public int LOAD_PRICE_COMPETITOR = 0;
    public boolean IS_LOAD_PRICE_COMPETITOR = false;
    public boolean SHOW_PREV_MRP_IN_PRICE = false;

    private String CODE_PRICE_UOM = "PRICE_UOM";
    private String CODE_PRICE_COMPETITOR = "PRICE_COMPETITOR";
    private String CODE_PRICE_SRP = "PRICE_SRP";
    private String CODE_PRICE_CHANGED = "PRICE_CHANGED";
    private String CODE_PRICE_COMPLIANCE = "PRICE_COMPLIANCE";
    private String CODE_SHOW_PREV_MRP_IN_PRICE = "PRICE_LAST_VP_MRP";
    private String CODE_PRICE_LASTVP = "PRICE_LAST_VP";
    public ArrayList<String> mSearchTypeArray = new ArrayList<>();


    private PriceTrackingHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static PriceTrackingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PriceTrackingHelper(context);
        }
        return instance;
    }

    public void prepareAdapters() {
        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(context.getResources().getString(R.string.all));
        mSearchTypeArray.add(context.getResources().getString(R.string.product_name));
        mSearchTypeArray.add(context.getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(context.getResources().getString(
                R.string.order_dialog_barcode));
    }

    public void clearInstance() {
        instance = null;
    }

    /**
     * Load SKU from Detail Table
     */
    public void loadPriceTransaction(Context mContext) {
        String mLastVisitPrice = "LastVisitPrice";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String tid = "";

            // To load previous transaction prices
            String sql1 = "SELECT PId,Price,Uomid,mrp,isown FROM " + mLastVisitPrice
                    + " WHERE Rid = " + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor cur = db.selectSQL(sql1);
            if (cur != null) {
                while (cur.moveToNext()) {
                    setPrevPrice(cur.getString(0), cur.getString(1),
                            cur.getInt(2), cur.getString(3), cur.getInt(4));
                }
                cur.close();
            }


            // To load Current Transaction Prices
            // Get Tid From Header

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT Tid FROM ");
            sb.append(mPriceChangeHeader);
            sb.append(" WHERE retailerid = ");
            sb.append(bmodel.getRetailerMasterBO().getRetailerID());
            sb.append(" AND distributorid=" + bmodel.getRetailerMasterBO().getDistributorId());

            if (!bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) {
                sb.append(" AND date = ");
                sb.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
                sb.append(" and upload= 'N'");
            }


            Cursor orderHeaderCursor = db.selectSQL(sb.toString());

            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext()) {
                    tid = orderHeaderCursor.getString(0);
                }
                orderHeaderCursor.close();
            }


            sql1 = "SELECT PId, Changed, Price, Compliance, ReasonId, Own,UomID,mrp,mop,price_change_reasonid FROM "
                    + mPriceChangeDetail + " WHERE Tid = " + QT(tid);
            cur = db.selectSQL(sql1);
            if (cur != null) {
                while (cur.moveToNext()) {
                    setPrice(cur.getString(0), cur.getInt(1), cur.getString(2),
                            cur.getInt(3), cur.getString(4), cur.getInt(5),
                            cur.getInt(6), cur.getString(7), cur.getString(8), cur.getString(9));
                }
                cur.close();
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Set the Previous Price Detail
     *
     * @param pid
     * @param price
     */
    private void setPrevPrice(String pid, String price, int uomid, String mrp, int own) {
        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        ProductMasterBO sku;
        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
            sku = bmodel.productHelper.getProductMasterBOById(pid);
        } else {
            sku = bmodel.productHelper.getTaggedProductBOById(pid);
        }
        if (sku != null) {
            if (sku.getOwn() == own) {
                if (sku.getCaseUomId() == uomid) {
                    sku.setPrevPrice_ca(price);
                    sku.setPrevMRP_ca(mrp);
                }
                if (sku.getPcUomid() == uomid) {
                    sku.setPrevPrice_pc(price);
                    sku.setPrevMRP_pc(mrp);
                }
                if (sku.getOuUomid() == uomid) {
                    sku.setPrevPrice_oo(price);
                    sku.setPrevMRP_ou(mrp);
                }

            }
        }
    }

    /**
     * Set the Tracking Detail
     *
     * @param pid
     * @param price
     */
    private void setPrice(String pid, int changed, String price,
                          int compliance, String reasonId, int own, int uomID, String mrp, String mop, String priceChangeRid) {

        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        ProductMasterBO productBO;
        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
            productBO = bmodel.productHelper.getProductMasterBOById(pid);
        } else {
            productBO = bmodel.productHelper.getTaggedProductBOById(pid);
        }
        if (productBO != null) {
            if (productBO.getOwn() == own) {
                productBO.setPriceChanged(changed);
                productBO.setPriceCompliance(compliance);
                productBO.setReasonID(reasonId);
                productBO.setPriceMOP(mop);
                productBO.setPriceChangeReasonID(priceChangeRid);

                if (productBO.getCaseUomId() == uomID) {
                    productBO.setPrice_ca(price);
                    productBO.setMrp_ca(mrp);
                }
                if (productBO.getPcUomid() == uomID) {
                    productBO.setPrice_pc(price);
                    productBO.setMrp_pc(mrp);

                }
                if (productBO.getOuUomid() == uomID) {
                    productBO.setPrice_oo(price);
                    productBO.setMrp_ou(mrp);
                }

            }
        }
    }


    /**
     * Save Tracking Detail in Detail Table
     */
    public void savePriceTransaction(Context mContext, List<ProductMasterBO> productList) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String tid;
            String sql;
            Cursor headerCursor;

            String headerColumns = "Tid, RetailerId, Date, TimeZone,distributorid";
            String detailColumns = "Tid, PId, Changed, Price, Compliance, ReasonId, Own, RetailerId,uomID,mrp,mop,price_change_reasonid";

            String values;

            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + bmodel.getRetailerMasterBO().getRetailerID() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // delete transaction if exist
            sql = "SELECT Tid FROM " + mPriceChangeHeader
                    + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " AND upload='N'";


            headerCursor = db.selectSQL(sql);

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL(mPriceChangeHeader,
                        "Tid=" + QT(headerCursor.getString(0)), false);
                db.deleteSQL(mPriceChangeDetail,
                        "Tid=" + QT(headerCursor.getString(0)), false);
                headerCursor.close();
            }

            //Weightage Calculation
            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                bmodel.fitscoreHelper.getWeightage(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_PRICE);
            }

            // save header
            int moduleWeightage = 0, productWeightage = 0, sum = 0;

            values = QT(tid) + ","
                    + bmodel.getRetailerMasterBO().getRetailerID() + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + QT(bmodel.getTimeZone()) + ","
                    + bmodel.retailerMasterBO.getDistributorId();

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                detailColumns = detailColumns + ",Score";
            }

            db.insertSQL(mPriceChangeHeader, headerColumns, values);

            // Save Details
            for (ProductMasterBO sku : productList) {
                if (!sku.getPrice().equals("0")
                        || sku.getPriceCompliance() == 1
                        || !sku.getReasonID().equals("0")
                        || !sku.getPrice_ca().equals("0")
                        || !sku.getPrice_pc().equals("0")
                        || !sku.getPrice_oo().equals("0")
                        || !sku.getMrp_ca().equals("0")
                        || !sku.getMrp_pc().equals("0")
                        || !sku.getMrp_ou().equals("0")
                        ) {
                    boolean isInserted = false;

                    sku.setPrice_ca(bmodel.checkDecimalValue(sku.getPrice_ca(), 8, 2));
                    sku.setPrice_oo(bmodel.checkDecimalValue(sku.getPrice_oo(), 8, 2));
                    sku.setPrice_pc(bmodel.checkDecimalValue(sku.getPrice_pc(), 8, 2));

                    if ((!sku.getPrice_ca().equals("0") && !sku.getPrice_ca().equals("0.0")) || (!sku.getMrp_ca().equals("0") && !sku.getMrp_ca().equals("0.0"))) {
                        values = QT(tid) + "," + sku.getProductID() + ","
                                + sku.getPriceChanged() + ","
                                + QT(sku.getPrice_ca())
                                + "," + sku.getPriceCompliance() + ","
                                + sku.getReasonID() + "," + sku.getOwn() + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + sku.getCaseUomId() + "," + sku.getMrp_ca() + "," + sku.getPriceMOP()+","+sku.getPriceChangeReasonID();

                        if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                            productWeightage = bmodel.fitscoreHelper.checkWeightage(sku.getProductID());
                            values = values + "," + productWeightage;
                            sum = sum + productWeightage;
                        }

                        db.insertSQL(mPriceChangeDetail, detailColumns, values);
                        isInserted = true;
                    }
                    if ((!sku.getPrice_pc().equals("0") && !sku.getPrice_pc().equals("0.0")) || (!sku.getMrp_pc().equals("0") && !sku.getMrp_pc().equals("0.0"))) {
                        values = QT(tid) + "," + sku.getProductID() + ","
                                + sku.getPriceChanged() + ","
                                + QT(sku.getPrice_pc())
                                + "," + sku.getPriceCompliance() + ","
                                + sku.getReasonID() + "," + sku.getOwn() + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + sku.getPcUomid() + "," + sku.getMrp_pc() + "," + sku.getPriceMOP()+","+sku.getPriceChangeReasonID();

                        if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                            productWeightage = bmodel.fitscoreHelper.checkWeightage(sku.getProductID());
                            values = values + "," + productWeightage;
                            sum = sum + productWeightage;
                        }

                        db.insertSQL(mPriceChangeDetail, detailColumns, values);
                        isInserted = true;
                    }
                    if ((!sku.getPrice_oo().equals("0") && !sku.getPrice_oo().equals("0.0")) || (!sku.getMrp_ou().equals("0") && !sku.getMrp_ou().equals("0.0"))) {
                        values = QT(tid) + "," + sku.getProductID() + ","
                                + sku.getPriceChanged() + ","
                                + QT(sku.getPrice_oo())
                                + "," + sku.getPriceCompliance() + ","
                                + sku.getReasonID() + "," + sku.getOwn() + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + sku.getOuUomid() + "," + sku.getMrp_ou() + "," + sku.getPriceMOP()+","+sku.getPriceChangeReasonID();

                        if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                            productWeightage = bmodel.fitscoreHelper.checkWeightage(sku.getProductID());
                            values = values + "," + productWeightage;
                            sum = sum + productWeightage;
                        }

                        db.insertSQL(mPriceChangeDetail, detailColumns, values);
                        isInserted = true;
                    }

                    if (!isInserted && !sku.getReasonID().equals("0") || sku.getPriceCompliance() == 1) {
                        values = QT(tid) + "," + sku.getProductID() + ","
                                + sku.getPriceChanged() + ","
                                + QT(sku.getPrice_pc())
                                + "," + sku.getPriceCompliance() + ","
                                + sku.getReasonID() + "," + sku.getOwn() + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + 0 + "," + sku.getMrp_ou() + "," + sku.getPriceMOP();

                        if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                            productWeightage = bmodel.fitscoreHelper.checkWeightage(sku.getProductID());
                            values = values + "," + productWeightage;
                            sum = sum + productWeightage;
                        }

                        db.insertSQL(mPriceChangeDetail, detailColumns, values);
                    }
                }
            }

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                bmodel.calculateFitscoreandInsert(db, sum, DataMembers.FIT_PRICE);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    //to check whether modification or data added
    public boolean hasDataTosave(List<ProductMasterBO> productList) {


        for (ProductMasterBO sku : productList) {
            if (sku.getPrice().equals(".") && sku.getPriceCompliance() != 0 && sku.getPriceChanged() != 0
                    || sku.getPrice_ca().equals(".")
                    || sku.getPrice_pc().equals(".")
                    || sku.getPrice_oo().equals(".")
                    || sku.getMrp_ca().equals(".")
                    || sku.getMrp_pc().equals(".")
                    || sku.getReasonID().equals(".")
                    || sku.getMrp_ou().equals("."))
                return false;
        }

        for (ProductMasterBO sku : productList) {
            if (!sku.getPrice().equals("0") || sku.getPriceCompliance() != 0 ||sku.getPriceChanged() != 0
                    || !sku.getPrice_ca().equals("0")
                    || !sku.getPrice_pc().equals("0")
                    || !sku.getPrice_oo().equals("0")
                    || !sku.getMrp_ca().equals("0")
                    || !sku.getMrp_pc().equals("0")
                    || !sku.getReasonID().equals("0")
                    || !sku.getMrp_ou().equals("0"))
                return true;
        }

        return false;
    }
    public boolean hasPriceChangeReason(List<ProductMasterBO> productList) {

        for (ProductMasterBO sku : productList) {
            if (sku.getPriceChanged() != 0 && sku.getPriceChangeReasonID().equals("0"))
                return false;
        }

        return true;
    }


    private String QT(String data) {
        return "'" + data + "'";
    }

    public void updateLastVisitPriceAndMRP() {
        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        for (ProductMasterBO productMasterBO : bmodel.productHelper.getTaggedProducts()) {
            productMasterBO.setPrice_pc(productMasterBO.getPrevPrice_pc());
            productMasterBO.setPrice_ca(productMasterBO.getPrevPrice_ca());
            productMasterBO.setPrice_oo(productMasterBO.getPrevPrice_oo());

            productMasterBO.setMrp_pc(productMasterBO.getPrevMRP_pc());
            productMasterBO.setMrp_ca(productMasterBO.getPrevMRP_ca());
            productMasterBO.setMrp_ou(productMasterBO.getPrevMRP_ou());
        }

    }


    //to update visited status for price check module icon
    public boolean isPriceCheckDone(Context mContext) {
        boolean flag = false;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String sb = "select tid from PriceCheckHeader where retailerid=" +
                    bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and upload='N'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                if (c.moveToFirst())
                    flag = true;
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e.getMessage());
        } finally {
            db.closeDB();
        }
        return flag;

    }

    //to refresh price check object
    public void clearPriceCheck() {
        Vector<ProductMasterBO> priceCheckDetails = bmodel.productHelper.getTaggedProducts();
        if (priceCheckDetails != null) {
            for (ProductMasterBO productMasterBO : priceCheckDetails) {
                productMasterBO.setPrice_ca(0 + "");
                productMasterBO.setPrice_oo(0 + "");
                productMasterBO.setPrice_pc(0 + "");
                productMasterBO.setReasonID(0 + "");
                productMasterBO.setPriceChangeReasonID(0 + "");
                productMasterBO.setPriceChanged(0);
                productMasterBO.setPriceCompliance(0);
            }
        }
    }

    public void loadPriceCheckConfiguration(Context mContext, int subChannelId) {
        try {

            SHOW_PRICE_PC = false;
            SHOW_PRICE_OU = false;
            SHOW_PRICE_CA = false;
            SHOW_PRICE_SRP = false;
            SHOW_PRICE_CHANGED = false;
            SHOW_PRICE_COMPLIANCE = false;
            LOAD_PRICE_COMPETITOR = 0;
            IS_PRICE_CHANGE_REASON = 0;
            IS_LOAD_PRICE_COMPETITOR = false;
            SHOW_PREV_MRP_IN_PRICE = false;
            SHOW_PRICE_LASTVP = false;

            String codeValue = null;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_UOM) + " and Flag=1 and ForSwitchSeller = 0 and subchannelid=" + subChannelId;
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            } else {
                sql = "select RField from "
                        + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode=" + bmodel.QT(CODE_PRICE_UOM) + " and Flag=1 and ForSwitchSeller = 0 and subchannelid=0";
                c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        codeValue = c.getString(0);
                    }
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "OP":
                            this.SHOW_PRICE_PC = true;
                            break;
                        case "OO":
                            this.SHOW_PRICE_OU = true;
                            break;
                        case "OC":
                            this.SHOW_PRICE_CA = true;
                            break;
                    }

                }
            }
            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_SRP) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_SRP = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_CHANGED) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_CHANGED = true;
                    this.IS_PRICE_CHANGE_REASON = c.getInt(0);
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_COMPLIANCE) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_COMPLIANCE = true;
                }
                c.close();
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_COMPETITOR) + " and ForSwitchSeller = 0 and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.LOAD_PRICE_COMPETITOR = c.getInt(0);
                    this.IS_LOAD_PRICE_COMPETITOR = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_PREV_MRP_IN_PRICE) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PREV_MRP_IN_PRICE = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRICE_LASTVP) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_LASTVP = true;
                }
                c.close();
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
