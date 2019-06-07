package com.ivy.cpg.view.price;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

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
    public boolean SHOW_PRICE_TAG_CHECK;
    public boolean SHOW_PRICE_LASTVP;
    public int IS_PRICE_CHANGE_REASON;
    public boolean SHOW_PRICE_LOCATION_FILTER;

    // 0 - product ,1 - Competitor product , 2 - Product & Competitior product
    public int LOAD_PRICE_COMPETITOR = 0;
    public boolean IS_LOAD_PRICE_COMPETITOR = false;
    public boolean SHOW_PREV_MRP_IN_PRICE = false;

    public ArrayList<String> mSearchTypeArray = new ArrayList<>();

    private ArrayAdapter<StandardListBO> mLocationAdapter;

    public int mSelectedLocationIndex;

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

    private String checkDecimalValue(String value, int wholeValueCount,
                                     int decimalValueCount) {
        if (!value.contains("."))
            return value;
        else {
            String fString = "", lString = "";
            value = value.startsWith(".") ? "0" + value : value;
            value = value.endsWith(".") ? value + "0" : value;
            String[] valArr = value.split("\\.");
            if (valArr[0].length() > wholeValueCount)
                fString = valArr[0].substring(0, valArr[0].length() - 1);
            if (valArr[1].length() > decimalValueCount)
                lString = valArr[1].substring(0, valArr[0].length() - 1);
            if (valArr[0].length() <= wholeValueCount && valArr[1].length() <= decimalValueCount) {
                fString = valArr[0];
                lString = valArr[1];
            }
            return fString + "." + lString;
        }
    }

    public void prepareAdapters() {
        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(context.getResources().getString(R.string.all));
        mSearchTypeArray.add(context.getResources().getString(R.string.product_name));
        mSearchTypeArray.add(context.getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(context.getResources().getString(
                R.string.order_dialog_barcode));

        //location
        mLocationAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice);
        ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(context);
        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
        {
            if(productTaggingHelper.getTaggedLocations().size()>0) {
                if (productTaggingHelper.getTaggedLocations().contains(Integer.parseInt(temp.getListID())))
                    mLocationAdapter.add(temp);
            }else {
                mLocationAdapter.add(temp);
            }
        }
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }

    }

    public String getCurrentLocationId(){
        return mLocationAdapter.getItem(mSelectedLocationIndex).getListID();
    }

    public ArrayAdapter<StandardListBO> getLocationAdapter() {
        return mLocationAdapter;
    }

    public void clearInstance() {
        instance = null;
    }

    /**
     * Load SKU from Detail Table
     */
    public void loadPriceTransaction(Context mContext) {
        String mLastVisitPrice = "LastVisitPrice";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String tid = "";

            // To load previous transaction prices
            String sql1 = "SELECT PId,Price,Uomid,mrp,isown,inStoreLocId,hasPriceTag FROM " + mLastVisitPrice
                    + " WHERE Rid = " + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor cur = db.selectSQL(sql1);
            if (cur != null) {
                while (cur.moveToNext()) {
                    setPrevPrice(cur.getString(0), cur.getString(1),
                            cur.getInt(2), cur.getString(3), cur.getInt(4), cur.getInt(5), cur.getInt(6));
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
                sb.append(QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                sb.append(" and upload= 'N'");
            }


            Cursor orderHeaderCursor = db.selectSQL(sb.toString());

            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext()) {
                    tid = orderHeaderCursor.getString(0);
                }
                orderHeaderCursor.close();
            }


            sql1 = "SELECT PId, Changed, Price, Compliance, ReasonId, Own,UomID,mrp,mop,price_change_reasonid,inStoreLocId,hasPriceTag FROM "
                    + mPriceChangeDetail + " WHERE Tid = " + QT(tid);
            cur = db.selectSQL(sql1);
            if (cur != null) {
                while (cur.moveToNext()) {
                    setPrice(cur.getString(0), cur.getInt(1), cur.getString(2),
                            cur.getInt(3), cur.getInt(4), cur.getInt(5),
                            cur.getInt(6), cur.getString(7), cur.getString(8), cur.getString(9), cur.getInt(10), cur.getInt(11));
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
    private void setPrevPrice(String pid, String price, int uomid, String mrp, int own, int locationId, int isPriceTagAvailable) {
        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        ProductMasterBO sku;
        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
            sku = bmodel.productHelper.getProductMasterBOById(pid);
        } else {
            sku = ProductTaggingHelper.getInstance(context).getTaggedProductBOById(pid);
        }
        if (sku != null) {
            if (sku.getOwn() == own) {
                for (LocationBO locationBO : sku.getLocations()) {
                    if (locationBO.getLocationId() == locationId) {
                        if (sku.getCaseUomId() == uomid) {
                            locationBO.setPrevPrice_ca(price);
                            sku.setPrevMRP_ca(mrp);
                        }
                        if (sku.getPcUomid() == uomid) {
                            locationBO.setPrevPrice_pc(price);
                            sku.setPrevMRP_pc(mrp);
                        }
                        if (sku.getOuUomid() == uomid) {
                            locationBO.setPrevPrice_oo(price);
                            sku.setPrevMRP_ou(mrp);
                        }

                        locationBO.setPriceTagAvailability(isPriceTagAvailable);
                    }
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
                          int compliance, int reasonId, int own, int uomID, String mrp, String mop, String priceChangeRid, int locationId, int isPriceTagAvailable) {

        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        ProductMasterBO productBO;
        if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
            productBO = bmodel.productHelper.getProductMasterBOById(pid);
        } else {
            productBO = ProductTaggingHelper.getInstance(context).getTaggedProductBOById(pid);
        }
        if (productBO != null) {
            if (productBO.getOwn() == own) {

                for (LocationBO locationBO : productBO.getLocations()) {
                    if (locationBO.getLocationId() == locationId) {

                        locationBO.setPriceChanged(changed);
                        locationBO.setPriceCompliance(compliance);
                        locationBO.setReasonId(reasonId);
                        productBO.setPriceMOP(mop);
                        locationBO.setPriceChangeReasonID(priceChangeRid);

                        if (productBO.getCaseUomId() == uomID) {
                            locationBO.setPrice_ca(price);
                            locationBO.setMrp_ca(mrp);
                        }
                        if (productBO.getPcUomid() == uomID) {
                            locationBO.setPrice_pc(price);
                            locationBO.setMrp_pc(mrp);

                        }
                        if (productBO.getOuUomid() == uomID) {
                            locationBO.setPrice_oo(price);
                            locationBO.setMrp_ou(mrp);
                        }

                        locationBO.setPriceTagAvailability(isPriceTagAvailable);
                    }
                }


            }
        }
    }


    /**
     * Save Tracking Detail in Detail Table
     */
    public void savePriceTransaction(Context mContext, List<ProductMasterBO> productList) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String tid;
            String sql;
            Cursor headerCursor;

            String headerColumns = "Tid, RetailerId, Date, TimeZone,distributorid,ridSF,VisitId";
            String detailColumns = "Tid, PId, Changed, Price, Compliance, ReasonId, Own, RetailerId,uomID,mrp,mop,price_change_reasonid,inStoreLocId,hasPriceTag";

            String values;

            tid = bmodel.getAppDataProvider().getUser().getUserid() + ""
                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + ""
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            // delete transaction if exist
            sql = "SELECT Tid FROM " + mPriceChangeHeader
                    + " WHERE RetailerId = "
                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                    + " AND Date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            //+ " AND upload='N'";


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
                bmodel.fitscoreHelper.getWeightage(bmodel.getAppDataProvider().getRetailMaster().getRetailerID(), DataMembers.FIT_PRICE);
            }

            // save header
            int productWeightage, sum = 0;

            values = QT(tid) + ","
                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID() + ","
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + QT(DateTimeUtils.getTimeZone()) + ","
                    + bmodel.retailerMasterBO.getDistributorId() + ","
                    + QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                    + bmodel.getAppDataProvider().getUniqueId();

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                detailColumns = detailColumns + ",Score";
            }

            db.insertSQL(mPriceChangeHeader, headerColumns, values);

            // Save Details
            for (ProductMasterBO productBO : productList) {

                int siz = productBO.getLocations().size();
                for (int j = 0; j < siz; j++) {


                    LocationBO locationBO = productBO.getLocations().get(j);
                    if (locationBO.getPriceCompliance() == 1
                            || locationBO.getReasonId() != 0
                            || !locationBO.getPrice_ca().equals("0")
                            || !locationBO.getPrice_pc().equals("0")
                            || !locationBO.getPrice_oo().equals("0")
                            || !locationBO.getMrp_ca().equals("0")
                            || !locationBO.getMrp_pc().equals("0")
                            || !locationBO.getMrp_ou().equals("0")
                    ) {
                        boolean isInserted = false;

                        if (locationBO.getPrice_ca().trim().equals("."))
                            locationBO.setPrice_ca("0.0");
                        else if (locationBO.getPrice_ca().trim().equals(".0"))
                            locationBO.setPrice_ca("0.0");
                        else if (locationBO.getPrice_ca().trim().endsWith(".") && locationBO.getPrice_ca().trim().length() > 1)
                            locationBO.setPrice_ca(locationBO.getPrice_ca() + "0");

                        if (locationBO.getPrice_pc().trim().equals("."))
                            locationBO.setPrice_pc("0.0");
                        else if (locationBO.getPrice_pc().trim().equals(".0"))
                            locationBO.setPrice_pc("0.0");
                        else if (locationBO.getPrice_pc().trim().endsWith(".") && locationBO.getPrice_pc().trim().length() > 1)
                            locationBO.setPrice_pc(locationBO.getPrice_pc() + "0");

                        if (locationBO.getPrice_oo().trim().equals("."))
                            locationBO.setPrice_oo("0.0");
                        else if (locationBO.getPrice_oo().trim().equals(".0"))
                            locationBO.setPrice_oo("0.0");
                        else if (locationBO.getPrice_oo().trim().endsWith(".") && locationBO.getPrice_oo().trim().length() > 1)
                            locationBO.setPrice_oo(locationBO.getPrice_oo() + "0");

                        locationBO.setPrice_ca(checkDecimalValue(locationBO.getPrice_ca(), 8, bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION));
                        locationBO.setPrice_oo(checkDecimalValue(locationBO.getPrice_oo(), 8, bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION));
                        locationBO.setPrice_pc(checkDecimalValue(locationBO.getPrice_pc(), 8, bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION));

                        if ((!locationBO.getPrice_ca().equals("0") && !locationBO.getPrice_ca().equals("0.0")) || (!locationBO.getMrp_ca().equals("0") && !locationBO.getMrp_ca().equals("0.0"))) {
                            values = QT(tid) + "," + productBO.getProductID() + ","
                                    + locationBO.getPriceChanged() + ","
                                    + QT(locationBO.getPrice_ca())
                                    + "," + locationBO.getPriceCompliance() + ","
                                    + locationBO.getReasonId() + "," + productBO.getOwn() + ","
                                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + productBO.getCaseUomId() + "," + locationBO.getMrp_ca() + "," + productBO.getPriceMOP() + "," + locationBO.getPriceChangeReasonID() + "," + locationBO.getLocationId()
                                    + "," + locationBO.getPriceTagAvailability();

                            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                productWeightage = bmodel.fitscoreHelper.checkWeightage(productBO.getProductID());
                                values = values + "," + productWeightage;
                                sum = sum + productWeightage;
                            }

                            db.insertSQL(mPriceChangeDetail, detailColumns, values);
                            isInserted = true;
                        }
                        if ((!locationBO.getPrice_pc().equals("0") && !locationBO.getPrice_pc().equals("0.0")) || (!locationBO.getMrp_pc().equals("0") && !locationBO.getMrp_pc().equals("0.0"))) {
                            values = QT(tid) + "," + productBO.getProductID() + ","
                                    + locationBO.getPriceChanged() + ","
                                    + QT(locationBO.getPrice_pc())
                                    + "," + locationBO.getPriceCompliance() + ","
                                    + locationBO.getReasonId() + "," + productBO.getOwn() + ","
                                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + productBO.getPcUomid() + "," + locationBO.getMrp_pc() + "," + productBO.getPriceMOP() + "," + locationBO.getPriceChangeReasonID() + "," + locationBO.getLocationId()
                                    + "," + locationBO.getPriceTagAvailability();

                            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                productWeightage = bmodel.fitscoreHelper.checkWeightage(productBO.getProductID());
                                values = values + "," + productWeightage;
                                sum = sum + productWeightage;
                            }

                            db.insertSQL(mPriceChangeDetail, detailColumns, values);
                            isInserted = true;
                        }
                        if ((!locationBO.getPrice_oo().equals("0") && !locationBO.getPrice_oo().equals("0.0")) || (!locationBO.getMrp_ou().equals("0") && !locationBO.getMrp_ou().equals("0.0"))) {
                            values = QT(tid) + "," + productBO.getProductID() + ","
                                    + locationBO.getPriceChanged() + ","
                                    + QT(locationBO.getPrice_oo())
                                    + "," + locationBO.getPriceCompliance() + ","
                                    + locationBO.getReasonId() + "," + productBO.getOwn() + ","
                                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + productBO.getOuUomid() + "," + locationBO.getMrp_ou() + "," + productBO.getPriceMOP() + "," + locationBO.getPriceChangeReasonID() + "," + locationBO.getLocationId()
                                    + "," + locationBO.getPriceTagAvailability();

                            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                productWeightage = bmodel.fitscoreHelper.checkWeightage(productBO.getProductID());
                                values = values + "," + productWeightage;
                                sum = sum + productWeightage;
                            }

                            db.insertSQL(mPriceChangeDetail, detailColumns, values);
                            isInserted = true;
                        }

                        if (!isInserted && (locationBO.getReasonId() != 0 || locationBO.getPriceCompliance() == 1)) {
                            values = QT(tid) + "," + productBO.getProductID() + ","
                                    + locationBO.getPriceChanged() + ","
                                    + QT(locationBO.getPrice_pc())
                                    + "," + locationBO.getPriceCompliance() + ","
                                    + locationBO.getReasonId() + "," + productBO.getOwn() + ","
                                    + bmodel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + 0 + "," + locationBO.getMrp_ou() + "," + productBO.getPriceMOP() + "," + locationBO.getPriceChangeReasonID() + "," + locationBO.getLocationId()
                                    + "," + locationBO.getPriceTagAvailability();

                            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                productWeightage = bmodel.fitscoreHelper.checkWeightage(productBO.getProductID());
                                values = values + "," + productWeightage;
                                sum = sum + productWeightage;
                            }

                            db.insertSQL(mPriceChangeDetail, detailColumns, values);
                        }
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
            for (LocationBO locationBO : sku.getLocations()) {
                if (locationBO.getPriceCompliance() != 0 || locationBO.getPriceChanged() != 0
                        || (!locationBO.getPrice_ca().equals("0") && !locationBO.getPrice_ca().equals("."))
                        || (!locationBO.getPrice_pc().equals("0") && !locationBO.getPrice_pc().equals("."))
                        || (!locationBO.getPrice_oo().equals("0") && !locationBO.getPrice_oo().equals("."))
                        || (!locationBO.getMrp_ca().equals("0") && !locationBO.getMrp_ca().equals("."))
                        || (!locationBO.getMrp_pc().equals("0") && !locationBO.getMrp_pc().equals("."))
                        || locationBO.getReasonId() != 0
                        || (!locationBO.getMrp_ou().equals("0")) && !locationBO.getMrp_ou().equals("."))
                    return true;
            }
        }

        return false;
    }

    public boolean hasPriceChangeReason(List<ProductMasterBO> productList) {

        for (ProductMasterBO sku : productList) {
            for (LocationBO locationBO : sku.getLocations()) {
                if (locationBO.getPriceChanged() != 0 && locationBO.getPriceChangeReasonID().equals("0"))
                    return false;
            }
        }

        return true;
    }


    private String QT(String data) {
        return "'" + data + "'";
    }

    public void updateLastVisitPriceAndMRP() {
        //mTaggedProducts list only used in PriceCheck screen. So updating only in mTaggedProducts
        for (ProductMasterBO productMasterBO : ProductTaggingHelper.getInstance(context).getTaggedProducts()) {
            for (LocationBO locationBO : productMasterBO.getLocations()) {

                locationBO.setPrice_pc(locationBO.getPrevPrice_pc());
                locationBO.setPrice_ca(locationBO.getPrevPrice_ca());
                locationBO.setPrice_oo(locationBO.getPrevPrice_oo());

            }

            productMasterBO.setMrp_pc(productMasterBO.getPrevMRP_pc());
            productMasterBO.setMrp_ca(productMasterBO.getPrevMRP_ca());
            productMasterBO.setMrp_ou(productMasterBO.getPrevMRP_ou());

        }

    }


    //to update visited status for price check module icon
    public boolean isPriceCheckDone(Context mContext) {
        boolean flag = false;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sb = "select tid from PriceCheckHeader where retailerid=" +
                    StringUtils.QT(bmodel.getRetailerMasterBO().getRetailerID());

            if (!bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) {
                sb = sb + " and upload='N'";
            }
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
        Vector<ProductMasterBO> priceCheckDetails = ProductTaggingHelper.getInstance(context).getTaggedProducts();
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

            String CODE_PRICE_01 = "PRICE01";
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
            SHOW_PRICE_LOCATION_FILTER = false;

            String codeValue = null;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();


            String CODE_PRICE_UOM = "PRICE_UOM";
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(CODE_PRICE_UOM) + " and Flag=1 and ForSwitchSeller = 0 and subchannelid=" + subChannelId;
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            } else {
                sql = "select RField from "
                        + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode=" + StringUtils.QT(CODE_PRICE_UOM) + " and Flag=1 and ForSwitchSeller = 0 and subchannelid=0";
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
                    + " where hhtCode= " + StringUtils.QT(CODE_PRICE_01) + " and ForSwitchSeller = 0 and Flag=1";
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
                    switch (temp) {
                        case "SRP":
                            this.SHOW_PRICE_SRP = true;
                            break;
                        case "PT":
                            this.SHOW_PRICE_TAG_CHECK = true;
                            break;
                        case "LMRP":
                            this.SHOW_PREV_MRP_IN_PRICE = true;
                            break;
                        case "LSRP":
                            this.SHOW_PRICE_LASTVP = true;
                            break;
                    }

                }
            }


            String CODE_PRICE_CHANGED = "PRICE_CHANGED";
            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(CODE_PRICE_CHANGED) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_CHANGED = true;
                    this.IS_PRICE_CHANGE_REASON = c.getInt(0);
                }
                c.close();
            }


            String CODE_PRICE_COMPLIANCE = "PRICE_COMPLIANCE";
            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(CODE_PRICE_COMPLIANCE) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_COMPLIANCE = true;
                }
                c.close();
            }

            String CODE_PRICE_COMPETITOR = "PRICE_COMPETITOR";
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(CODE_PRICE_COMPETITOR) + " and ForSwitchSeller = 0 and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.LOAD_PRICE_COMPETITOR = c.getInt(0);
                    this.IS_LOAD_PRICE_COMPETITOR = true;
                }
                c.close();
            }

            String CODE_PRICE_LOCATION = "PRICE02";
            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(CODE_PRICE_LOCATION) + " and ForSwitchSeller = 0 and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_PRICE_LOCATION_FILTER = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
