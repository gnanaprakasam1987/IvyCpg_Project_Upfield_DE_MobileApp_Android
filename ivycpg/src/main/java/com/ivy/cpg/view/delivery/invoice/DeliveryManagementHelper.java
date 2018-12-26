package com.ivy.cpg.view.delivery.invoice;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.util.SparseArray;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajesh.k on 24-02-2016.
 */
public class DeliveryManagementHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static DeliveryManagementHelper instance = null;
    // delivery management
    private ArrayList<InvoiceHeaderBO> mInvoiceList;
    private ArrayList<ProductMasterBO> mInvoiceDetailsList;

    public static DeliveryManagementHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DeliveryManagementHelper(context);
        }
        return instance;
    }

    private DeliveryManagementHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();

    }

    public void downloadInvoiceDetails() {
        DBUtil db = null;
        try {
            mInvoiceList = new ArrayList<>();
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String query = "select invoiceno,invoicedate,invNetamount,linespercall,invoicerefno,PickListId from InvoiceDeliveryMaster " +
                    " where retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +
                    " and invoiceno not in(select vh.invoiceid from vandeliveryheader vh)";

            Cursor c = db.selectSQL(query);
            InvoiceHeaderBO invoiceHeaderBO;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    invoiceHeaderBO = new InvoiceHeaderBO();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setLinesPerCall(c.getInt(3));
                    invoiceHeaderBO.setInvoiceRefNo(c.getString(4));
                    invoiceHeaderBO.setPickListId(c.getString(5));
                    mInvoiceList.add(invoiceHeaderBO);

                }
            }
            c.close();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }


    }

    public ArrayList<RetailerMasterBO> getInvoicedRetailerList() {
        ArrayList<RetailerMasterBO> invoicedRetailerList = new ArrayList<>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            String s = "select distinct RM.Retailerid,RM.RetailerName from invoicemaster IM INNER JOIN RetailerMaster RM on RM.RetailerID = IM.Retailerid"
                    + " where IM.InvoiceNo not in(select vh.invoiceid from vandeliveryheader vh)";
            Cursor c = db.selectSQL(s);
            RetailerMasterBO retailerMasterBO;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    retailerMasterBO = new RetailerMasterBO();
                    retailerMasterBO.setRetailerID(c.getString(0));
                    retailerMasterBO.setRetailerName(c.getString(1));
                    invoicedRetailerList.add(retailerMasterBO);
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }

        return invoicedRetailerList;
    }

    public ArrayList<InvoiceHeaderBO> getInvoiceList() {
        if (mInvoiceList != null) {
            return mInvoiceList;
        }
        return new ArrayList<>();

    }

    public void downloadDeliveryProductDetails(String invoiceno) {
        SparseArray<ProductMasterBO> invoicedProducts = new SparseArray<>();
        mInvoiceDetailsList = new ArrayList<>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String query = "select id.productid,id.qty,id.uomid,id.uomcount,id.uomprice,id.batchid,bm.batchnum,PM.psname,PM.piece_uomid as pieceUomID," +
                    "PM.dUomId as caseUomId,PM.dUomQty as caseSize, PM.dOuomid as outerUomId,PM.dOuomQty as outerSize,PM.sih from invoicedetailuomwise id" +
                    " Inner JOIN ProductMaster PM on PM.PID = id.productid" +
                    " left join batchmaster bm  on bm.pid=productid and bm.batchid=id.batchid  where invoiceid=" +
                    bmodel.QT(invoiceno) + "  order by productid,id.batchid";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                ProductMasterBO invoiceProductBO;
                int productid;
                while (c.moveToNext()) {
                    productid = c.getInt(c.getColumnIndex("productid"));
                    if (invoicedProducts.get(productid) == null) {
                        invoiceProductBO = new ProductMasterBO();
                        invoiceProductBO.setProductID(productid + "");
                        invoiceProductBO.setProductShortName(c.getString(c.getColumnIndex("psname")));
                        invoiceProductBO.setSIH(c.getInt(c.getColumnIndex("sih")));
                    } else {
                        invoiceProductBO = invoicedProducts.get(productid);
                    }
                    if (c.getInt(c.getColumnIndex("uomid")) == c.getInt(c.getColumnIndex("pieceUomID"))) {
                        invoiceProductBO.setOrderedPcsQty(c.getInt(1));
                        invoiceProductBO.setPcUomid(c.getInt(2));
                        invoiceProductBO.setLocalOrderPieceqty(c.getInt(1));
                    } else if (c.getInt(c.getColumnIndex("uomid")) == c.getInt(c.getColumnIndex("caseUomId"))) {
                        invoiceProductBO.setOrderedCaseQty(c.getInt(1));
                        invoiceProductBO.setCaseUomId(c.getInt(2));
                        invoiceProductBO.setLocalOrderCaseqty(c.getInt(1));
                        invoiceProductBO.setCaseSize(c.getInt(c.getColumnIndex("caseSize")));
                    } else if (c.getInt(c.getColumnIndex("uomid")) == c.getInt(c.getColumnIndex("outerUomId"))) {
                        invoiceProductBO.setOrderedOuterQty(c.getInt(1));
                        invoiceProductBO.setOuUomid(c.getInt(2));
                        invoiceProductBO.setLocalOrderOuterQty(c.getInt(1));
                        invoiceProductBO.setOutersize(c.getInt(c.getColumnIndex("outerSize")));
                    }
                    invoicedProducts.put(productid, invoiceProductBO);
                }

                for (int i = 0; i < invoicedProducts.size(); i++) {
                    int key = invoicedProducts.keyAt(i);
                    mInvoiceDetailsList.add(invoicedProducts.get(key));
                }
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }
    }

    public ArrayList<ProductMasterBO> getmInvoiceDetailsList() {
        if (mInvoiceDetailsList == null)
            mInvoiceDetailsList = new ArrayList<>();
        return mInvoiceDetailsList;
    }

    public void saveDeliveryManagement(String invoiceno, String selectedItem, String SignName, String SignPath, String contactName, String contactNo) {
        DBUtil db = null;
        try {
            InvoiceHeaderBO invoiceHeaderBO = null;
            for (InvoiceHeaderBO invoiceBO : mInvoiceList) {
                if (invoiceno.equals(invoiceBO.getInvoiceNo())) {
                    invoiceHeaderBO = invoiceBO;
                    break;
                }
            }

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            String deliveryheadercolumns = "uid,retailerid,invoiceddate,deliverydate,status,latitude,longtitude,utcdate," +
                    "invoiceid,SignName,Proofpicture,contactName,contactNo,SignaturePath,PickListId";
            String status = "";
            if (selectedItem.equals(mContext.getResources().getString(R.string.fullfilled))) {
                status = "F";
            } else if (selectedItem.equals(mContext.getResources().getString(R.string.partially_fullfilled))) {
                status = "P";
            } else if (selectedItem.equals(mContext.getResources().getString(R.string.rejected))) {
                status = "R";
            }

            String uid = AppUtils.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));
            String header = (uid + "," + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ",") +
                    AppUtils.QT(invoiceHeaderBO.getInvoiceDate()) + "," + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," +
                    AppUtils.QT(status) + "," + AppUtils.QT(bmodel.mSelectedRetailerLatitude + "") + "," + AppUtils.QT(bmodel.mSelectedRetailerLongitude + "") + "," +
                    DatabaseUtils.sqlEscapeString(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")) +
                    "," + AppUtils.QT(invoiceno) +
                    "," + AppUtils.QT(SignName) +//internal colunm
                    "," + AppUtils.QT(SignPath) +// proofPicture not used... so using same column
                    "," + AppUtils.QT(contactName) +
                    "," + AppUtils.QT(contactNo) +
                    "," + AppUtils.QT(SignPath) +
                    "," + AppUtils.QT(invoiceHeaderBO.getPickListId());
            db.insertSQL(DataMembers.tbl_van_delivery_header, deliveryheadercolumns, header);

            String values = AppUtils.QT(invoiceHeaderBO.getPickListId()) + ","
                    + AppUtils.QT(invoiceno) + ","
                    + AppUtils.QT(status);

            db.insertSQL(DataMembers.tbl_picklist_invoice, DataMembers.tbl_picklist_invoice_cols, values);

            if (selectedItem.equals(mContext.getResources().getString(R.string.partially_fullfilled))) {
                String detailColumns = "uid,pid,uomid,batchid,invoiceqty,deliveredqty,returnqty,retailerid";


                StringBuilder details;
                for (ProductMasterBO productMasterBO : mInvoiceDetailsList) {
                    String batchid = "0";
                    if (productMasterBO.getBatchid() != null)
                        batchid = productMasterBO.getBatchid();


                    if (productMasterBO.getOrderedPcsQty() > 0 || productMasterBO.getInit_pieceqty() > 0) {
                        details = new StringBuilder();
                        details.append(uid).append(",").append(productMasterBO.getProductID()).append(",").append(productMasterBO.getPcUomid()).append(",").append(batchid);
                        details.append(",").append(productMasterBO.getTotalQty()).append(",").append(productMasterBO.getInit_pieceqty()).append(",").append(productMasterBO.getOrderedPcsQty());
                        details.append(",").append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                    if (productMasterBO.getOrderedCaseQty() > 0 || productMasterBO.getInit_caseqty() > 0) {
                        details = new StringBuilder();
                        details.append(uid).append(",").append(productMasterBO.getProductID()).append(",").append(productMasterBO.getCaseUomId()).append(",").append(batchid);
                        details.append(",").append(productMasterBO.getTotalQty()).append(",").append(productMasterBO.getInit_caseqty()).append(",").append(productMasterBO.getOrderedCaseQty());
                        details.append(",").append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                    if (productMasterBO.getOrderedOuterQty() > 0 || productMasterBO.getInit_OuterQty() > 0) {
                        details = new StringBuilder();
                        details.append(uid).append(",").append(productMasterBO.getProductID()).append(",").append(productMasterBO.getOuUomid()).append(",").append(batchid);
                        details.append(",").append(productMasterBO.getTotalQty()).append(",").append(productMasterBO.getInit_OuterQty()).append(",").append(productMasterBO.getOrderedOuterQty());
                        details.append(",").append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                }

            }

            if (bmodel.configurationMasterHelper.IS_GENERATE_SR_IN_DELIVERY) {
                saveSalesReturn(invoiceno);
            }

            // update SIH
            if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY) {
                if (!selectedItem.equals(mContext.getResources().getString(R.string.rejected))) {
                    for (ProductMasterBO productMasterBO : mInvoiceDetailsList) {
                        if (productMasterBO.getInit_pieceqty() > 0 || productMasterBO.getInit_caseqty() > 0 || productMasterBO.getInit_OuterQty() > 0) {

                            int deliveredQty = productMasterBO.getInit_pieceqty() + (productMasterBO.getInit_caseqty() * productMasterBO.getCaseSize())
                                    + (productMasterBO.getInit_OuterQty() * productMasterBO.getOutersize());

                            if (deliveredQty > 0) {
                                int sih = bmodel.productHelper.getProductMasterBOById(productMasterBO.getProductID()).getSIH() - deliveredQty;
                                bmodel.productHelper.getProductMasterBOById(productMasterBO.getProductID()).setSIH(sih);
                                db.updateSQL("update StockInHandMaster set qty=(qty-" + deliveredQty + ") where pid=" + productMasterBO.getProductID());

                                db.updateSQL("update ProductMaster set sih=sih- " + deliveredQty
                                        + " where PID = " + productMasterBO.getProductID());
                            }

                        }
                    }
                }
            }

            //Collect Empty if available and update SIH

            String headerColumns = "Tid, Date, TimeZone, Value";
            String detailColumns = "Tid, PId, Qty, Price, UomId, UomCount, LineValue";

            Cursor c;


            String tid = "";
            for (ProductMasterBO bo : mInvoiceDetailsList) {
                if (bo.getInit_pieceqty() > 0
                        || bo.getInit_caseqty() > 0
                        || bo.getInit_OuterQty() > 0) {


                    c = db.selectSQL("select bpid,qty,uomid from bommaster where pid=" + bo.getProductID());
                    if (c != null) {
                        while (c.moveToNext()) {
                            int qty = c.getInt(1);
                            int bottleToCollect = 0;
                            int linevalue = 0;

                            ProductMasterBO emptyProductBO = bmodel.productHelper.getProductMasterBOById(c.getInt(0) + "");

                            if (bo.getInit_pieceqty() > 0 && c.getInt(2) == bo.getPcUomid()) {
                                bottleToCollect += (qty * bo.getInit_pieceqty());
                                linevalue += (qty * emptyProductBO.getBaseprice());
                            }
                            if (bo.getInit_caseqty() > 0 && c.getInt(2) == bo.getCaseUomId()) {
                                bottleToCollect += (qty * bo.getInit_caseqty());
                                linevalue += (qty * emptyProductBO.getBaseprice());
                            }
                            if (bo.getInit_OuterQty() > 0 && c.getInt(2) == bo.getOuUomid()) {
                                bottleToCollect += (qty * bo.getInit_OuterQty());
                                linevalue += (qty * emptyProductBO.getBaseprice());
                            }


                            if (bottleToCollect > 0) {// If empty product available to collect

                                // adding returned qty to SIH
                                if (emptyProductBO != null) {
                                    bmodel.productHelper.getProductMasterBOById(c.getInt(0) + "").setSIH(emptyProductBO.getSIH() + bottleToCollect);
                                }

                                db.updateSQL("update ProductMaster set sih=sih+ " + bottleToCollect
                                        + " where PID = " + c.getInt(0));
                                if (isProductAvailableinSIHmaster(c.getInt(0) + "")) {

                                    db.updateSQL("update StockInHandMaster set qty=(qty+" + bottleToCollect + ") where pid=" + c.getInt(0));
                                } else {
                                    db.insertSQL("StockInHandMaster",
                                            "pid,qty,batchid", c.getInt(0) + "," + (bottleToCollect) + "," + (emptyProductBO.getBatchid() != null ? emptyProductBO.getBatchid() : "0"));
                                }


                                if (c.getPosition() == 0) {

                                    Cursor cursor = db.selectSQL("SELECT Tid FROM EmptyReconciliationHeader WHERE Date = "
                                            + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
                                    // delete transaction if exist
                                    if (cursor.getCount() > 0) {
                                        cursor.moveToNext();
                                        db.deleteSQL("EmptyReconciliationHeader",
                                                "Tid=" + bmodel.QT(cursor.getString(0)), false);
                                    }

                                    tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                                            + SDUtil.now(SDUtil.DATE_TIME_ID);

                                    db.insertSQL("EmptyReconciliationHeader", headerColumns,
                                            bmodel.QT(tid) + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                                    + bmodel.QT(bmodel.getTimeZone()) + "," + 0);
                                }

                                db.insertSQL("EmptyReconciliationDetail", detailColumns, bmodel.QT(tid) + "," + c.getInt(0) + ","
                                        + bottleToCollect + "," + emptyProductBO.getBaseprice()
                                        + "," + c.getInt(2) + "," + '1' + ","
                                        + bmodel.QT(linevalue + ""));
                            }
                        }
                        c.close();
                    }
                }
            }

            //


        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }
    }

    private void saveSalesReturn(String invoiceno) {

        DBUtil db;

        db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();

        String id = bmodel.QT("SR" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));
        boolean isData = false;
        double totalReturnValue = 0;
        int lpc = 0;
        String columns = "uid,ProductID,Pqty,Cqty,Condition,duomQty,oldmrp,mfgdate,expdate,outerQty,dOuomQty,dOuomid,duomid,batchid,invoiceno,srpedited,totalQty,totalamount,RetailerID,reason_type,LotNumber,piece_uomid,status,HsnCode";
        for (ProductMasterBO productMasterBO : mInvoiceDetailsList) {

            int pieceQty = 0, caseQty = 0, outerQty = 0;
            if (productMasterBO.getOrderedPcsQty() > productMasterBO.getInit_pieceqty())
                pieceQty = productMasterBO.getOrderedPcsQty() - productMasterBO.getInit_pieceqty();
            if (productMasterBO.getOrderedCaseQty() > productMasterBO.getInit_caseqty())
                caseQty = productMasterBO.getOrderedCaseQty() - productMasterBO.getInit_caseqty();
            if (productMasterBO.getOrderedOuterQty() > productMasterBO.getInit_OuterQty())
                outerQty = productMasterBO.getOrderedOuterQty() - productMasterBO.getInit_OuterQty();

            int totalQty = pieceQty + (caseQty * productMasterBO.getCaseSize()) + (outerQty * productMasterBO.getOutersize());
            double totalValue = (pieceQty * productMasterBO.getSrp()) + (caseQty * productMasterBO.getCsrp()) + (outerQty * productMasterBO.getOsrp());

            totalReturnValue += totalValue;

            if (totalQty > 0) {

                String values = id
                        + ","
                        + DatabaseUtils.sqlEscapeString(productMasterBO.getProductID())
                        + ","
                        + pieceQty
                        + ","
                        + caseQty
                        + ","
                        + 0
                        + ","
                        + productMasterBO.getCaseSize()
                        + ","
                        + 0
                        + ","
                        + bmodel.QT("")
                        + ","
                        + bmodel.QT("")
                        + ","
                        + outerQty
                        + ","
                        + productMasterBO.getOutersize()
                        + ","
                        + productMasterBO.getOuUomid()
                        + ","
                        + productMasterBO.getCaseUomId()
                        + ","
                        + bmodel.productHelper
                        .getOldBatchIDByMfd(productMasterBO
                                .getProductID())
                        + ","
                        + bmodel.QT((invoiceno == null || "null".equals(invoiceno)) ? "" : invoiceno)
                        + ","
                        + 0
                        + ","
                        + totalQty
                        + ","
                        + totalValue
                        + ","
                        + bmodel.QT(bmodel.retailerMasterBO
                        .getRetailerID()) + ","
                        + 1 + "," + bmodel.QT("") + "," + productMasterBO.getPcUomid()
                        + "," + bmodel.QT("") + "," + bmodel.QT(productMasterBO.getHsnCode());

                db.insertSQL(
                        DataMembers.tbl_SalesReturnDetails,
                        columns, values);

                lpc += 1;
                isData = true;
            }

        }

        if (isData) {
            columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule";
            String values = id + ","
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + bmodel.QT(bmodel.retailerMasterBO.getRetailerID()) + ","
                    + bmodel.retailerMasterBO.getBeatID() + ","
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "," + bmodel.QT(SDUtil.format(totalReturnValue,
                    bmodel.configurationMasterHelper.PERCENT_PRECISION_COUNT, 0)) + "," + lpc + ","
                    + bmodel.QT(bmodel.retailerMasterBO.getRetailerCode()) + ","
                    + bmodel.QT(bmodel.getSaleReturnNote()) + ","
                    + bmodel.QT(bmodel.mSelectedRetailerLatitude + "") + ","
                    + bmodel.QT(bmodel.mSelectedRetailerLongitude + "") + ","
                    + bmodel.retailerMasterBO.getDistributorId() + ","
                    + bmodel.retailerMasterBO.getDistParentId() + ","
                    + bmodel.QT("") + ","
                    + bmodel.QT("") + ","
                    + 1; // 1 means Indicative, 0 means normal

            values = values + "," + bmodel.QT("") + "," + bmodel.QT("");

            db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

        }
    }

    private boolean isProductAvailableinSIHmaster(String productId) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select qty from StockInHandMaster where pid=" + productId);
            if (c != null) {
                if (c.getCount() > 0) {
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }
        return false;
    }

    public boolean isDeliveryMgtDone() {
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            String query = "select count(uid) from VanDeliveryHeader where Retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    if (count > 0) return true;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return false;
    }

    public boolean isDeliveryModuleAvailable() {
        boolean flag = false;
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String query = "select count(*) from HhtMenuMaster where flag=1 and hhtcode='MENU_DELIVERY_MGMT'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    if (count == 1) {
                        flag = true;
                    }
                }
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return flag;
    }

    /**
     * Change update flag to N for not delivery invoice,if day close done
     */
    public void updateNotDeliveryDetails() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String deliveryheadercolumns = "uid,retailerid,invoiceddate,status,latitude,longtitude,utcdate,invoiceid,DeliveryDate";
            String sb = "select invoiceno,retailerid,invoicedate from  invoicemaster  where invoiceNo not in" +
                    "(select invoiceid from VandeliveryHeader)";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                StringBuffer header;
                while (c.moveToNext()) {
                    String invoiceno = c.getString(0);
                    String retailerid = c.getString(1);
                    String invoicedate = c.getString(2);

                    header = new StringBuffer();
                    String uid = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + SDUtil.now(SDUtil.DATE_TIME_ID));
                    header.append(uid).append(",").append(bmodel.QT(retailerid)).append(",");
                    header.append(bmodel.QT(invoicedate)).append(",");
                    header.append(bmodel.QT("N")).append(",").append(bmodel.QT(LocationUtil.latitude + "")).append(",").append(bmodel.QT(LocationUtil.longitude + "")).append(",");
                    header.append(DatabaseUtils.sqlEscapeString(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")));
                    header.append(",").append(bmodel.QT(invoiceno));
                    header.append(",").append(bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
                    db.insertSQL(DataMembers.tbl_van_delivery_header, deliveryheadercolumns, header.toString());

                }
            }
            //  db.updateSQL(sb.toString());


        } catch (Exception e) {
            Commons.print(e.getMessage());

        } finally {
            db.closeDB();
        }

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    private String userName, userPassword;

    public void downloadEmailAccountCredentials() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_EMAIL' and listtype='DELIVERY_MAIL'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userName = c.getString(0);
                }
                c.close();
            }

            s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_PWD' and listtype='DELIVERY_MAIL'";

            c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userPassword = c.getString(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    //To get whether the retailer has sales return or not
    public boolean hasDeliveryReturn() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT count(uid) from SalesReturnHeader where RetailerID =" + AppUtils.QT(bmodel.getRetailerMasterBO().getRetailerID());
            int count = 0;
            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    count = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
            return count > 0;

        } catch (SQLException e) {
            Commons.printException(e);
        }

        return false;
    }

}
