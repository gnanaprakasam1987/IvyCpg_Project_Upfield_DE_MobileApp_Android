package com.ivy.countersales.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.countersales.bo.CS_StockApplyHeaderBO;
import com.ivy.countersales.bo.CS_StockApplyProductBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by subramanian on 6/14/17.
 */

public class CS_StockApplyHelper {

    private Context context;
    private BusinessModel bmodel;
    private static CS_StockApplyHelper instance = null;

    ArrayList<StandardListBO> mStockType;
    private Vector<CS_StockApplyProductBO> manualProducts;
    private Vector<CS_StockApplyHeaderBO> mCSStockApplyHeader;
    private HashMap<String, Vector<CS_StockApplyProductBO>> mCounterStockHeaderDetails;
    private Vector<ProductMasterBO> mTaggedProducts;

    public Vector<CS_StockApplyProductBO> getManualProducts() {
        return manualProducts;
    }

    public Vector<CS_StockApplyHeaderBO> getCSStockApplyHeader() {
        return mCSStockApplyHeader;
    }

    public HashMap<String, Vector<CS_StockApplyProductBO>> getCounterStockHeaderDetails() {
        return mCounterStockHeaderDetails;
    }

    protected CS_StockApplyHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static CS_StockApplyHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CS_StockApplyHelper(context);
        }
        return instance;
    }

    public void loadProductForManualLoad() {

        manualProducts = new Vector<>();
        CS_StockApplyProductBO productBO;
        for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
            productBO = new CS_StockApplyProductBO();
            productBO.setProductId(Integer.parseInt(bo.getProductID()));
            productBO.setProductName(bo.getProductName());
            productBO.setBarcode(bo.getBarCode());
            productBO.setUomId(bo.getPcUomid());
            productBO.setQty(0);
            productBO.setParentId(bo.getParentid());
            productBO.setMrp(bo.getMRP());
            productBO.setIsSalable(bo.getIsSaleable());
            productBO.setIsReturnable(bo.getIsReturnable());

            manualProducts.add(productBO);
        }

    }

    public void loadStockDetails() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();


        //load stock headers
        String query = "SELECT DISTINCT A.receipt_id, A.receipt_no, A.status, A.receipt_date, A.stock_type, B.ListName as StockTypeName, A.upload FROM CS_StockReceiptHeader A"
                + " LEFT JOIN StandardListMaster B on B.ListId = A.stock_type ORDER BY A.receipt_date DESC";

        Cursor c = db.selectSQL(query);

        if (c != null) {
            CS_StockApplyHeaderBO stockHeader;
            mCSStockApplyHeader = new Vector<>();
            mCounterStockHeaderDetails = new HashMap<>();
            while (c.moveToNext()) {
                stockHeader = new CS_StockApplyHeaderBO();
                stockHeader.setReceiptId(c.getString(c.getColumnIndex("receipt_id")));
                stockHeader.setReferenceNo(c.getString(c.getColumnIndex("receipt_no")));
                stockHeader.setStatus(c.getString(c.getColumnIndex("status")));
                stockHeader.setReceiptDate(c.getString(c.getColumnIndex("receipt_date")));
                stockHeader.setStockTypeId(c.getInt(c.getColumnIndex("stock_type")));
                if (c.getInt(c.getColumnIndex("stock_type")) == 0)
                    stockHeader.setStockType("Normal");
                else
                    stockHeader.setStockType(c.getString(c.getColumnIndex("StockTypeName")));
                stockHeader.setUpload(c.getString(c.getColumnIndex("upload")));
                mCSStockApplyHeader.add(stockHeader);
                mCounterStockHeaderDetails.put(stockHeader.getReceiptId(), new Vector<CS_StockApplyProductBO>());
            }
            c.close();
        }

        //load stock details for header
        if (mCounterStockHeaderDetails != null && mCounterStockHeaderDetails.size() > 0) {
            query = "SELECT DISTINCT receipt_id, pid, qty, uomid, damagedQty FROM CS_StockReceiptDetails ";

            c = db.selectSQL(query);

            if (c != null) {
                CS_StockApplyProductBO productBO;
                while (c.moveToNext()) {
                    productBO = new CS_StockApplyProductBO();
                    productBO.setReceiptId(c.getString(c.getColumnIndex("receipt_id")));
                    productBO.setProductId(c.getInt(c.getColumnIndex("pid")));

                    productBO.setQty(c.getInt(c.getColumnIndex("qty")));
                    productBO.setUomId(c.getInt(c.getColumnIndex("uomid")));
                    productBO.setDamagedQty(c.getInt(c.getColumnIndex("damagedQty")));

                    if (bmodel.productHelper.getProductMasterBOById(productBO.getProductId() + "") != null) {
                        productBO.setBarcode(bmodel.productHelper.getProductMasterBOById(productBO.getProductId() + "").getBarCode());
                        productBO.setParentId(bmodel.productHelper.getProductMasterBOById(productBO.getProductId() + "").getParentid());
                        productBO.setProductName(bmodel.productHelper.getProductMasterBOById(productBO.getProductId() + "").getProductName());
                        productBO.setMrp(bmodel.productHelper.getProductMasterBOById(productBO.getProductId() + "").getMRP());
                    }
                    mCounterStockHeaderDetails.get(productBO.getReceiptId()).add(productBO);
                }
                c.close();
            }
        }

        db.closeDB();

    }

    public ArrayList<StandardListBO> getStockType() {
        return mStockType;
    }

    public void loadStockType() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        mStockType = new ArrayList<>();
        StandardListBO type;
        type = new StandardListBO();
        type.setListID("0");
        type.setListCode("NORMAL");
        type.setListName("Normal");
        mStockType.add(type);


        Cursor c = db.selectSQL("SELECT ListId,listCode, ListName FROM StandardListMaster WHERE ListType = 'COUNTER_STOCK_TYPE'");

        if (c != null) {

            while (c.moveToNext()) {
                type = new StandardListBO();
                type.setListID(c.getString(0));
                type.setListCode(c.getString(1));
                type.setListName(c.getString(2));
                mStockType.add(type);
            }
            c.close();
        }

        db.closeDB();
    }

    public void updateCSSihForStockCheck() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        mTaggedProducts = new Vector<ProductMasterBO>();

        Cursor c = db.selectSQL("SELECT pid, sih,stock_type FROM CS_SIHDetails");

        if (c != null) {

            while (c.moveToNext()) {
                for (ProductMasterBO pBO : bmodel.productHelper.getTaggedProducts())

                {
                    if (pBO.getProductID().equals(c.getString(0))) {
                        for (int j = 0; j < pBO.getLocations().size(); j++) {
                            if (pBO.getLocations().get(j).getLocationId() == c.getInt(2)) {
                                pBO.getLocations().get(j).setmSIH(c.getInt(1));
                                mTaggedProducts.add(pBO);
                            }
                        }
                    }

                }

            }
            c.close();
        }

        db.closeDB();
    }

    public void updateCSSihFromRejectedVariance() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();


        Cursor c = db.selectSQL("SELECT pid,qty,stock_type FROM CS_RejectedVariance");
        if (c != null) {
            while (c.moveToNext()) {
                db.updateSQL("update CS_SIHDetails set sih=sih+" + c.getInt(1) + ",upload='N' where stock_type="
                        + c.getInt(2) + " and pid=" + c.getInt(0));

            }
            c.close();
        }


        c = db.selectSQL("SELECT distinct uid FROM CS_RejectedVariance");
        if (c != null) {
            while (c.moveToNext()) {
                db.insertSQL("CS_RejectedVarianceStatus",
                        "uid", c.getString(0));
            }
            c.close();
        }

        db.deleteSQL("CS_RejectedVariance", "", true);
        db.closeDB();
    }


    public void saveTransaction(CS_StockApplyHeaderBO header, Vector<CS_StockApplyProductBO> productList, String requestType) {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        String headerTableName = "CS_StockReceiptHeader";
        String detailTableName = "CS_StockReceiptDetails";

        if (requestType.equals("A") || requestType.equals("R")) {
            db.updateSQL("UPDATE " + headerTableName
                    + " SET status =" + bmodel.QT(requestType)
                    + " , date = " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " , upload = 'N'"
                    + " WHERE " + " receipt_id =" + bmodel.QT(header.getReceiptId()));
        } else if (requestType.equalsIgnoreCase("P")) {
            db.updateSQL("UPDATE " + headerTableName
                    + " SET status =" + bmodel.QT(requestType)
                    + " , date = " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " , upload = 'N'"
                    + " WHERE " + " receipt_id =" + bmodel.QT(header.getReceiptId()));

            for (CS_StockApplyProductBO product : productList) {
                if (product.getDamagedQty() > 0) {
                    db.updateSQL("UPDATE " + detailTableName
                            + " SET damagedQty =" + product.getDamagedQty()
                            + " , upload = 'N'"
                            + " WHERE " + " receipt_id =" + bmodel.QT(header.getReceiptId())
                            + " AND pid = " + product.getProductId()
                            + " AND uomid = " + product.getUomId());
                }
            }
        } else if (requestType.equalsIgnoreCase("M")) {

            String headerColumns = "receipt_id, receipt_no, status, receipt_date, stock_type, date, retailerid, counterid, upload";
            String detailColumns = "receipt_id, pid, qty, uomid, damagedQty, upload";

            String tid;
            String values;

            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            boolean isDetailInserted = false;

            for (CS_StockApplyProductBO product : productList) {
                if (product.getQty() > 0) {
                    values = bmodel.QT(tid)
                            + "," + product.getProductId()
                            + "," + product.getQty()
                            + "," + product.getUomId()
                            + "," + product.getDamagedQty()
                            + "," + bmodel.QT("N");

                    db.insertSQL(detailTableName, detailColumns, values);

                    isDetailInserted = true;
                }
            }

            if (isDetailInserted) {

                values = bmodel.QT(tid)
                        + "," + bmodel.QT(header.getReferenceNo())
                        + "," + bmodel.QT("M")
                        + "," + bmodel.QT(header.getReceiptDate())
                        + "," + header.getStockTypeId()
                        + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + "," + bmodel.getCounterRetailerId()
                        + "," + bmodel.getCounterId()
                        + "," + bmodel.QT("N");

                db.insertSQL(headerTableName, headerColumns, values);
            }
        }

        if (!requestType.equals("R")) {
            updateCounterStock(productList, header.getStockTypeId());
        }
    }

    public void updateCounterStock(Vector<CS_StockApplyProductBO> productList, int mStockTypeId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sihTableName = "CS_SIHDetails";
            String columns = "pid, sih, stock_type, counterid, upload";

            for (CS_StockApplyProductBO product : productList) {
                if (product.getBalanceQty() > 0) {
                    String query = "SELECT COUNT(pid), sih FROM CS_SIHDetails where pid = " + product.getProductId()
                            + " AND stock_type = " + mStockTypeId;
                    Cursor c = db.selectSQL(query);
                    if (c.getCount() > 0 && c.moveToNext()) {
                        if (c.getInt(0) > 0) {
                            int currentStock = c.getInt(1) + product.getBalanceQty();
                            db.updateSQL("UPDATE CS_SIHDetails"
                                    + " SET sih =" + currentStock
                                    + " , counterid = " + bmodel.getCounterId()
                                    + " , upload = 'N'"
                                    + " WHERE pid = " + product.getProductId()
                                    + " AND stock_type = " + mStockTypeId);
                        } else {
                            String values = product.getProductId()
                                    + "," + product.getBalanceQty()
                                    + "," + mStockTypeId
                                    + "," + bmodel.getCounterId()
                                    + "," + bmodel.QT("N");

                            db.insertSQL(sihTableName, columns, values);
                        }

                    }
                    c.close();
                }
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public boolean isCounterSIHDataToUpload() {

        boolean hasData = false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT count(*) FROM CS_SIHDetails WHERE upload = 'N'");

            if (c != null) {
                if (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();

            return hasData;

        } catch (Exception e) {

            Commons.printException(e);
            return hasData;
        }
    }

    public boolean isCounterStockApplyDataToUpload() {

        boolean hasData = false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT count(*) FROM CS_StockReceiptHeader WHERE upload = 'N'");

            if (c != null) {
                if (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();

            return hasData;

        } catch (Exception e) {

            Commons.printException(e);
            return hasData;
        }
    }

    public boolean isCSRejectedVarianceStatus() {

        boolean hasData = false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT count(*) FROM CS_RejectedVarianceStatus WHERE upload = 'N'");

            if (c != null) {
                if (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();

            return hasData;

        } catch (Exception e) {

            Commons.printException(e);
            return hasData;
        }
    }


    public ArrayList<StandardListBO> loadFeedBakcs() {
        ArrayList<StandardListBO> lst = null;
        try {
            StandardListBO reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster "
                    + " WHERE ListTYPE = '" + StandardListMasterConstants.COUNTER_SALES_STOCK_APPLY_FEEDBAKCS_TYPE + "'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                lst = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new StandardListBO();
                    reason.setListID(c.getString(0));
                    reason.setListName(c.getString(1));
                    lst.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return lst;
    }


}
