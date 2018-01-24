package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;

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
    protected DeliveryManagementHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;

    }

    public void downloadInvoiceDetails(){
        DBUtil db = null;
        try {
            mInvoiceList=new ArrayList<InvoiceHeaderBO>();
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb=new StringBuffer();
            sb.append("select invoiceno,invoicedate,invNetamount,linespercall from invoicemaster ");
            sb.append(" where retailerid="+bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            sb.append(" and invoiceno not in(select vh.invoiceid from vandeliveryheader vh)");
            Cursor c=db.selectSQL(sb.toString());
            InvoiceHeaderBO invoiceHeaderBO;
            if(c.getCount()>0){
                while(c.moveToNext()){
                    invoiceHeaderBO=new InvoiceHeaderBO();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setLinesPerCall(c.getInt(3));
                    mInvoiceList.add(invoiceHeaderBO);

                }
            }
            c.close();
        }catch (Exception e) {
            Commons.print(e.getMessage());
        }finally {
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
    public ArrayList<InvoiceHeaderBO> getInvoiceList(){
        if(mInvoiceList!=null){
            return mInvoiceList;
        }
        return new ArrayList<InvoiceHeaderBO>();

    }

    public void downloadDeliveryProductDetails(String invoiceno){
        HashMap<Integer, ProductMasterBO> invoicedProducts = new HashMap<>();
        mInvoiceDetailsList=new ArrayList<ProductMasterBO>();
        DBUtil db=null;
        try{
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            //sb.append("select id.productid,id.qty,id.uomid,id.uomcount,id.uomprice,id.batchid,bm.batchnum from invoicedetailuomwise id ");
            //sb.append("left join batchmaster bm  on bm.pid=productid and bm.batchid=id.batchid ");
            //sb.append(" where invoiceid="+bmodel.QT(invoiceno));
            //sb.append("  order by productid,id.batchid");

            sb.append("select id.productid,id.qty,id.uomid,id.uomcount,id.uomprice,id.batchid,bm.batchnum,PM.psname,PM.piece_uomid as pieceUomID," +
                    "PM.dUomId as caseUomId,PM.dUomQty as caseSize, PM.dOuomid as outerUomId,PM.dOuomQty as outerSize,PM.sih from invoicedetailuomwise id");
            sb.append(" Inner JOIN ProductMaster PM on PM.PID = id.productid");
            sb.append(" left join batchmaster bm  on bm.pid=productid and bm.batchid=id.batchid  where invoiceid="
                    + bmodel.QT(invoiceno) + "  order by productid,id.batchid");
/*select id.productid,id.qty,id.uomid,id.uomcount,id.uomprice,id.batchid,bm.batchnum,PM.psname,PM.piece_uomid as pieceUomID,
PM.dUomId as caseUomId,PM.dUomQty as caseSize, PM.dOuomid as outerUomId,PM.dOuomQty as outerSize from invoicedetailuomwise id
Inner JOIN ProductMaster PM on PM.PID = id.productid
left join batchmaster bm  on bm.pid=productid and bm.batchid=id.batchid  where invoiceid='123456'  order by productid,id.batchid*/

            Cursor c=db.selectSQL(sb.toString());
            if(c.getCount()>0){
                ProductMasterBO invoiceProductBO=null;
                int productid=0;
                int batchid=0;
                while (c.moveToNext()){
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
                        invoiceProductBO.setLocalOrderPieceqty(c.getInt(1));

                        //invoiceProductBO.setSrp(c.getFloat(4));
                    } else if (c.getInt(c.getColumnIndex("uomid")) == c.getInt(c.getColumnIndex("caseUomId"))) {
                        invoiceProductBO.setOrderedCaseQty(c.getInt(1));
                        invoiceProductBO.setLocalOrderCaseqty(c.getInt(1));
                        invoiceProductBO.setCaseSize(c.getInt(c.getColumnIndex("caseSize")));
                    } else if (c.getInt(c.getColumnIndex("uomid")) == c.getInt(c.getColumnIndex("outerUomId"))) {
                        invoiceProductBO.setOrderedOuterQty(c.getInt(1));
                        invoiceProductBO.setLocalOrderOuterQty(c.getInt(1));
                        invoiceProductBO.setOutersize(c.getInt(c.getColumnIndex("outerSize")));
                    }


                    /*ProductMasterBO product=bmodel.productHelper.getProductMasterBOById(c.getString(0));

                    if(product!=null) {
                        if (productid == c.getInt(0) && batchid == c.getInt(5)) {
                            invoiceProductBO.setProductID(c.getString(0));
                            invoiceProductBO.setBatchId(c.getInt(5) + "");
                            invoiceProductBO.setBatchNo(c.getString(6));
                            invoiceProductBO.setProductShortName(product.getProductShortName());
                            invoiceProductBO.setCheked(true);
                            if (c.getInt(2) == product.getPcUomid()) {
                                invoiceProductBO.setOrderedPcsQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderPieceqty(c.getInt(1));

                                invoiceProductBO.setSrp(c.getFloat(4));

                            } else if (c.getInt(2) == product.getCaseUomId()) {
                                invoiceProductBO.setOrderedCaseQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderCaseqty(c.getInt(1));
                                invoiceProductBO.setCaseSize(product.getCaseSize());
                                invoiceProductBO.setCsrp(product.getCsrp());

                            } else if (c.getInt(2) == product.getOuUomid()) {
                                invoiceProductBO.setOrderedOuterQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderOuterQty(c.getInt(1));
                                invoiceProductBO.setOutersize(product.getOutersize());
                                invoiceProductBO.setOsrp(product.getOsrp());

                            }


                        } else {
                            invoiceProductBO = new ProductMasterBO();
                            if(productid!=0)
                                mInvoiceDetailsList.add(invoiceProductBO);

                            invoiceProductBO.setProductID(c.getString(0));
                            invoiceProductBO.setBatchId(c.getInt(5)+"");
                            invoiceProductBO.setProductShortName(product.getProductShortName());
                            invoiceProductBO.setCheked(true);
                            invoiceProductBO.setBatchNo(c.getString(6));
                            invoiceProductBO.setPcUomid(product.getPcUomid());
                            invoiceProductBO.setCaseUomId(product.getCaseUomId());
                            invoiceProductBO.setOuUomid(product.getOuUomid());

                            if (c.getInt(2) == product.getPcUomid()) {
                                invoiceProductBO.setOrderedPcsQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderPieceqty(c.getInt(1));

                                invoiceProductBO.setSrp(c.getFloat(4));

                            } else if (c.getInt(2) == product.getCaseUomId()) {
                                invoiceProductBO.setOrderedCaseQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderCaseqty(c.getInt(1));
                                invoiceProductBO.setCaseSize(product.getCaseSize());
                                invoiceProductBO.setCsrp(product.getCsrp());

                            } else if (c.getInt(2) == product.getOuUomid()) {
                                invoiceProductBO.setOrderedOuterQty(c.getInt(1));
                                invoiceProductBO.setLocalOrderOuterQty(c.getInt(1));
                                invoiceProductBO.setOutersize(product.getOutersize());
                                invoiceProductBO.setOsrp(product.getOsrp());




                            }
                            productid=c.getInt(0);
                            batchid=c.getInt(5);
                        }
                    }
*/
                    invoicedProducts.put(productid, invoiceProductBO);
                }

                for (Map.Entry<Integer, ProductMasterBO> map : invoicedProducts.entrySet()) {

                    mInvoiceDetailsList.add(map.getValue());

                }
                /*if(productid!=0)
                    mInvoiceDetailsList.add(invoiceProductBO);*/
            }
        }catch (Exception e){
            Commons.print(e.getMessage());
        }finally {
            db.closeDB();
        }
    }

    public void downloadInvoiceProductDetails(String invoiceno){
        mInvoiceDetailsList=new ArrayList<ProductMasterBO>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select PM.pid,PM.psname,ID.pcsQty,ID.caseQty,ID.OuterQty,ifnull(BM.Batchnum,\"\"),PM.duomQty,PM.douomQty,ID.Qty,PM.piece_uomid,PM.dUomId,PM.dOuomid from InvoiceDetails ID ");
            sb.append("inner join Productmaster PM on PM.pid=ID.productid ");
            sb.append("left join batchmaster BM on ID.productid=BM.pid and ID.batchid=BM.batchid ");
            sb.append("where ID.invoiceID="+bmodel.QT(invoiceno));
            Cursor c=db.selectSQL(sb.toString());
            if(c.getCount()>0){
                ProductMasterBO productBO;
                while(c.moveToNext()){




                    productBO=new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductShortName(c.getString(1));
                    productBO.setOrderedPcsQty(c.getInt(2));
                    productBO.setLocalOrderPieceqty(c.getInt(2));
                    productBO.setOrderedCaseQty(c.getInt(3));
                    productBO.setLocalOrderCaseqty(c.getInt(3));
                    productBO.setOrderedOuterQty(c.getInt(4));
                    productBO.setLocalOrderOuterQty(c.getInt(4));

                    productBO.setBatchNo(c.getString(5));
                    productBO.setCaseSize(c.getInt(6));
                    productBO.setOutersize(c.getInt(7));
                    productBO.setTotalQty(c.getInt(8));
                    productBO.setPcUomid(c.getInt(9));
                    productBO.setCaseUomId(c.getInt(10));
                    productBO.setOuUomid(c.getInt(11));
                    productBO.setCheked(true);
                    mInvoiceDetailsList.add(productBO);
                }
            }

        }catch(Exception e){
            Commons.printException(e);
        }finally {
            db.closeDB();
        }
    }
    public ArrayList<ProductMasterBO> getmInvoiceDetailsList(){
        if(mInvoiceDetailsList!=null){
            return mInvoiceDetailsList;
        }
        return new ArrayList<ProductMasterBO>();
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
                    "invoiceid,SignName,Proofpicture,contactName,contactNo,SignaturePath";
            String status = "";
            if (selectedItem.equals(mContext.getResources().getString(R.string.fullfilled))) {
                status = "F";
            } else if (selectedItem.equals(mContext.getResources().getString(R.string.partially_fullfilled))) {
                status = "P";
            } else if (selectedItem.equals(mContext.getResources().getString(R.string.rejected))) {
                status = "R";
            }

            String uid = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));
            StringBuffer header = new StringBuffer();
            header.append(uid + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ",");
            header.append(bmodel.QT(invoiceHeaderBO.getInvoiceDate()) + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",");
            header.append(bmodel.QT(status) + "," + bmodel.QT(bmodel.mSelectedRetailerLatitude + "") + "," + bmodel.QT(bmodel.mSelectedRetailerLongitude + "") + ",");
            header.append(DatabaseUtils.sqlEscapeString( Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")));
            header.append("," + bmodel.QT(invoiceno));
            header.append("," + bmodel.QT(SignName));//internal colunm
            header.append("," + bmodel.QT(SignPath));// proofPicture not used... so using same column
            header.append("," + bmodel.QT(contactName));
            header.append("," + bmodel.QT(contactNo));
            header.append("," + bmodel.QT(SignPath));
            db.insertSQL(DataMembers.tbl_van_delivery_header, deliveryheadercolumns, header.toString());

            if (selectedItem.equals(mContext.getResources().getString(R.string.partially_fullfilled))) {
                String detailColumns = "uid,pid,uomid,batchid,invoiceqty,deliveredqty,returnqty,retailerid";


                StringBuffer details;
                for (ProductMasterBO productMasterBO : mInvoiceDetailsList) {
                    String batchid = "0";
                    if (productMasterBO.getBatchid() != null)
                        batchid = productMasterBO.getBatchid();


                    if (productMasterBO.getOrderedPcsQty() > 0 || productMasterBO.getInit_pieceqty() > 0) {
                        details = new StringBuffer();
                        details.append(uid + "," + productMasterBO.getProductID() + "," + productMasterBO.getPcUomid() + "," + batchid);
                        details.append("," + productMasterBO.getTotalQty() + "," + productMasterBO.getInit_pieceqty() + "," + productMasterBO.getOrderedPcsQty());
                        details.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                    if (productMasterBO.getOrderedCaseQty() > 0 || productMasterBO.getInit_caseqty() > 0) {
                        details = new StringBuffer();
                        details.append(uid + "," + productMasterBO.getProductID() + "," + productMasterBO.getCaseUomId() + "," + batchid);
                        details.append("," + productMasterBO.getTotalQty() + "," + productMasterBO.getInit_caseqty() + "," + productMasterBO.getOrderedCaseQty());
                        details.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                    if (productMasterBO.getOrderedOuterQty() > 0 || productMasterBO.getInit_OuterQty() > 0) {
                        details = new StringBuffer();
                        details.append(uid + "," + productMasterBO.getProductID() + "," + productMasterBO.getOuUomid() + "," + batchid);
                        details.append("," + productMasterBO.getTotalQty() + "," + productMasterBO.getInit_OuterQty() + "," + productMasterBO.getOrderedOuterQty());
                        details.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                        db.insertSQL(DataMembers.tbl_van_delivery_detail, detailColumns, details.toString());
                    }
                }

            }

            // update SIH
            if(bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY) {
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

            Cursor c =null;


            String tid="";
            for (ProductMasterBO bo : mInvoiceDetailsList) {
                if(bo.getInit_pieceqty() > 0
                        || bo.getInit_caseqty() > 0
                        || bo.getInit_OuterQty() > 0){


                    c=db.selectSQL("select bpid,qty,uomid from bommaster where pid="+bo.getProductID());
                    if(c!=null){
                        while (c.moveToNext()){
                            int qty=c.getInt(1);
                            int bottleToCollect= 0;
                            int linevalue=0;

                            ProductMasterBO emptyProductBO=bmodel.productHelper.getProductMasterBOById(c.getInt(0)+"");

                            if(bo.getInit_pieceqty() > 0&& c.getInt(2)==bo.getPcUomid()){
                                bottleToCollect+=(qty*bo.getInit_pieceqty());
                                linevalue+=(qty*emptyProductBO.getBaseprice());
                            }
                            if(bo.getInit_caseqty() > 0&&c.getInt(2)==bo.getCaseUomId()){
                                bottleToCollect+=(qty*bo.getInit_caseqty());
                                linevalue+=(qty*emptyProductBO.getBaseprice());
                            }
                            if(bo.getInit_OuterQty()>0&&c.getInt(2)==bo.getOuUomid()){
                                bottleToCollect+=(qty*bo.getInit_OuterQty());
                                linevalue+=(qty*emptyProductBO.getBaseprice());
                            }


                            if(bottleToCollect>0) {// If empty product available to collect

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
                                            "pid,qty,batchid", c.getInt(0) + "," + (bottleToCollect)+","+(emptyProductBO.getBatchid()!=null?emptyProductBO.getBatchid():"0"));
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

    public boolean isProductAvailableinSIHmaster(String productId){
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c=db.selectSQL("select qty from StockInHandMaster where pid="+productId);
            if(c!=null) {
                if (c.getCount() > 0) {
                    return true;
                }
                c.close();
            }
        }
        catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }
        return false;
    }

    public boolean isDeliveryMgtDone(){
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            String query="select count(uid) from VanDeliveryHeader where Retailerid="+bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            Cursor c=db.selectSQL(query);
            if(c.getCount()>0){
                while(c.moveToNext()){
                    int count=c.getInt(0);
                    if(count>0) return true;
                }
            }
            c.close();
            db.closeDB();
        }catch (Exception e){
            Commons.print(e.getMessage());
        }
        return false;
    }
    public boolean isDeliveryModuleAvailable(){
        boolean flag=false;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String query="select count(*) from HhtMenuMaster where flag=1 and hhtcode='MENU_DELIVERY_MGMT'";
            Cursor c=db.selectSQL(query);
            if(c.getCount()>0){
                while(c.moveToNext()){
                    int count=c.getInt(0);
                    if(count==1) {
                        flag=true;
                    }
                }
            }
        }catch (Exception e){
           Commons.print(e.getMessage());
        }
        return flag;
    }

    /**
     * Change update flag to N for not delivery invoice,if day close done
     */
    public void updateNotDeliveryDetails(){
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String deliveryheadercolumns="uid,retailerid,invoiceddate,status,latitude,longtitude,utcdate,invoiceid,DeliveryDate";
            StringBuffer sb=new StringBuffer();
            sb.append("select invoiceno,retailerid,invoicedate from  invoicemaster  where invoiceNo not in");
            sb.append("(select invoiceid from VandeliveryHeader)");
            Cursor c=db.selectSQL(sb.toString());
            if(c.getCount()>0){
                StringBuffer header;
                while(c.moveToNext()){
                    String invoiceno=c.getString(0);
                    String retailerid=c.getString(1);
                    String invoicedate=c.getString(2);

                   header=new StringBuffer();
                    String uid = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + SDUtil.now(SDUtil.DATE_TIME_ID));
                    header.append(uid+","+bmodel.QT(retailerid)+",");
                    header.append(bmodel.QT(invoicedate)+",");
                    header.append(bmodel.QT("N")+","+bmodel.QT(LocationUtil.latitude+"")+","+bmodel.QT(LocationUtil.longitude+"")+",");
                    header.append(DatabaseUtils.sqlEscapeString(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")));
                    header.append(","+bmodel.QT(invoiceno));
                    header.append(","+bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
                    db.insertSQL(DataMembers.tbl_van_delivery_header, deliveryheadercolumns, header.toString());

                }
            }
          //  db.updateSQL(sb.toString());



        }catch (Exception e){
            Commons.print(e.getMessage());

        }finally {
          db.closeDB();
        }

    }


    ArrayList<ProductMasterBO> mDeliveryStocks;

    public ArrayList<ProductMasterBO> getmDeliveryStocks() {
        if(mDeliveryStocks==null)
        {
            return new ArrayList<>();
        }
        return mDeliveryStocks;
    }

    HashMap<String,ProductMasterBO> mDeliveryProductsBObyId;
    public void downloadDeliveryStock(){

        DBUtil db = null;
        try {
            String retailerIds="";
            for(RetailerMasterBO retailer:bmodel.getRetailerMaster()){
                if(retailer.getIsToday()==1||retailer.getIsDeviated().equalsIgnoreCase("Y")) {
                    if(retailerIds.length()>1)
                        retailerIds+=","+retailer.getRetailerID();
                    else
                        retailerIds+=retailer.getRetailerID();
                }
            }
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String sql="select productid,pm.pname,pm.psname,PM.piece_uomid,PM.duomid,Pm.dOuomid"
                    +",uomid as orderedUomId,qty as orderedQty from InvoiceDetailUOMWise ID"
                    +" Left join ProductMaster pm on pm.pid=ID.productid"
                    +" where invoiceid in(select invoiceno from invoicemaster where retailerid in ("+retailerIds+"))";

            Cursor c=db.selectSQL(sql);
            if(c.getCount()>0) {
                mDeliveryProductsBObyId=new HashMap<>();
                mDeliveryStocks=new ArrayList<>();
                ProductMasterBO bo;
                while (c.moveToNext()) {
                    if(mDeliveryProductsBObyId.get(c.getString(0))!=null){
                        ProductMasterBO productMasterBO=mDeliveryProductsBObyId.get(c.getString(0));
                        if(productMasterBO.getPcUomid()==c.getInt(6))
                            productMasterBO.setOrderedPcsQty((productMasterBO.getOrderedPcsQty()+c.getInt(7)));
                        if(productMasterBO.getCaseUomId()==c.getInt(6))
                            productMasterBO.setOrderedCaseQty((productMasterBO.getOrderedCaseQty()+c.getInt(7)));
                        if(productMasterBO.getOuUomid()==c.getInt(6))
                            productMasterBO.setOrderedOuterQty((productMasterBO.getOrderedOuterQty()+c.getInt(7)));
                    }
                    else {
                        bo = new ProductMasterBO();
                        bo.setProductID(c.getString(0));
                        bo.setProductName(c.getString(1));
                        bo.setProductShortName(c.getString(2));

                        bo.setPcUomid(c.getInt(3));
                        bo.setCaseUomId(c.getInt(4));
                        bo.setOuUomid(c.getInt(5));

                        if (bo.getPcUomid() == c.getInt(6))
                            bo.setOrderedPcsQty(c.getInt(7));
                        if (bo.getCaseUomId() == c.getInt(6))
                            bo.setOrderedCaseQty(c.getInt(7));
                        if (bo.getOuUomid() == c.getInt(6))
                            bo.setOrderedOuterQty(c.getInt(7));

                        mDeliveryProductsBObyId.put(bo.getProductID(), bo);
                        mDeliveryStocks.add(bo);
                    }

                }
            }
        }
        catch (Exception ex){
           Commons.printException(ex);
        }
        finally {
            mDeliveryProductsBObyId=null;
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

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    private String userName,userPassword;
    public void downloadEmailAccountCredentials() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_EMAIL' and listtype='DELIVERY_MAIL'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userName=c.getString(0);
                }
                c.close();
            }

             s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_PWD' and listtype='DELIVERY_MAIL'";

             c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userPassword=c.getString(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

}
