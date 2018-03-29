package com.ivy.cpg.view.orderdelivery;

 /*New Module Order Delivery Details
    Download methods for Order Header,Order Detail,Salesreturn header
    Salesreturn Detail,Replacement Detail,SchemeFree product details
    Insertion and updation in Invoice Master, Invoice Detail, Invoice Tax, Invoice Discount
    */

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class OrderDeliveryHelper {

    private static OrderDeliveryHelper instance = null;
    private BusinessModel businessModel;
    private String orderDeliveryDiscountAmount;
    private String orderDeliveryTaxAmount;
    private String orderDeliveryTotalValue;
    private ArrayList<SchemeProductBO> schemeProductBOS =  new ArrayList<>();
    private HashMap<String,Integer> mTotalDeliverQtyByPid = new HashMap<>();
    private Vector<ProductMasterBO> orderedProductMasterBOS = new Vector<>();
    private ArrayList<OrderHeader> orderHeaders = new ArrayList<>();
    private int totalProductQty;

    private OrderDeliveryHelper(Context context) {

        this.businessModel = (BusinessModel) context.getApplicationContext();
    }

    public static OrderDeliveryHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderDeliveryHelper(context);
        }
        return instance;
    }

    public int getTotalProductQty() {
        return totalProductQty;
    }

    public void setTotalProductQty(int totalProductQty) {
        this.totalProductQty = totalProductQty;
    }

    public ArrayList<OrderHeader> getOrderHeaders() {
        return orderHeaders;
    }

    private void setOrderHeaders(ArrayList<OrderHeader> orderHeaders) {
        this.orderHeaders = orderHeaders;
    }

    public Vector<ProductMasterBO> getOrderedProductMasterBOS() {
        return orderedProductMasterBOS;
    }

    private void setOrderedProductMasterBOS(Vector<ProductMasterBO> orderedProductMasterBOS) {
        this.orderedProductMasterBOS = orderedProductMasterBOS;
    }

    public String getOrderDeliveryDiscountAmount() {
        return orderDeliveryDiscountAmount;
    }

    private void setOrderDeliveryDiscountAmount(String orderDeliveryDiscountAmount) {
        this.orderDeliveryDiscountAmount = orderDeliveryDiscountAmount;
    }

    public ArrayList<SchemeProductBO> getSchemeProductBOS() {
        return schemeProductBOS;
    }

    private void setSchemeProductBOS(ArrayList<SchemeProductBO> schemeProductBOS) {
        this.schemeProductBOS = schemeProductBOS;
    }

    public String getOrderDeliveryTaxAmount() {
        return orderDeliveryTaxAmount;
    }

    private void setOrderDeliveryTaxAmount(String orderDeliveryTaxAmount) {
        this.orderDeliveryTaxAmount = orderDeliveryTaxAmount;
    }

    public String getOrderDeliveryTotalValue() {
        return orderDeliveryTotalValue;
    }

    private void setOrderDeliveryTotalValue(String orderDeliveryTotalValue) {
        this.orderDeliveryTotalValue = orderDeliveryTotalValue;
    }

    public void downloadOrderDeliveryHeader(Context mContext){

        orderHeaders = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sb = ("select OD.OrderID,OrderValue,LinesPerCall,OrderDate,invoicestatus from "
                    + DataMembers.tbl_orderHeader + " OD ") +
                    " where OD.upload='X' and OD.RetailerID="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) ;

            Cursor orderHeaderCursor = db.selectSQL(sb);
            if (orderHeaderCursor != null) {
                while (orderHeaderCursor.moveToNext()) {

                    OrderHeader orderHeader = new OrderHeader();

                    orderHeader.setOrderid(orderHeaderCursor.getString(0));
                    orderHeader.setOrderValue(orderHeaderCursor.getDouble(1));
                    orderHeader.setLinesPerCall(orderHeaderCursor.getInt(2));
                    orderHeader.setOrderDate(orderHeaderCursor.getString(3));
                    orderHeader.setInvoiceStatus(orderHeaderCursor.getInt(4));

                    orderHeaders.add(orderHeader);
                }
                orderHeaderCursor.close();
            }
            db.closeDB();

            setOrderHeaders(orderHeaders);

        }catch(Exception e){
            Commons.printException(e);
        }
    }

    public void downloadOrderDeliveryDetail(Context mContext,String orderId){
        try{
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String sql1 = "select productId,caseqty,pieceqty,  Rate, D1, D2, D3,"
                    + "uomcount,DA,totalamount,outerQty,dOuomQty,batchid,weight,msqqty,PcsUOMId,PriceOffValue,PriceOffId," +
                    "HsnCode,dOuomid,uomid from "
                    + DataMembers.tbl_orderDetails
                    + " where orderId="
                    + businessModel.QT(orderId) + " order by rowid";


            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                String productId;
                while (orderDetailCursor.moveToNext()) {

                    int caseQty = orderDetailCursor.getInt(1);
                    int pieceQty = orderDetailCursor.getInt(2);
                    int outerQty = orderDetailCursor.getInt(10);
                    int caseSize = orderDetailCursor.getInt(7);
                    int outerSize = orderDetailCursor.getInt(11);
                    String batchId = orderDetailCursor.getString(12);
                    float weight = orderDetailCursor.getFloat(13);
                    float srp = orderDetailCursor.getFloat(3);

                    productId = orderDetailCursor.getString(0);
                    setOrderDeliveryProductDetails(productId, caseQty, pieceQty,
                            outerQty, srp, orderDetailCursor.getDouble(4),caseSize,outerSize,batchId,weight,orderDetailCursor.getInt(14),
                            orderDetailCursor.getInt(15),orderDetailCursor.getFloat(16),orderDetailCursor.getInt(17),
                            orderDetailCursor.getString(18),orderDetailCursor.getInt(19),orderDetailCursor.getInt(20));
                }

                orderDetailCursor.close();

                loadSalesReturnData(orderId,db);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void setOrderDeliveryProductDetails(String productId, int caseQty, int pieceQty,
                                                int outerQty, float srp, double pricePerPiece, int caseSize, int outerSize,
                                                String batchId,float weight,int msQty,int pcsUOMId,float priceOffValue,
                                                int priceOffId,String hsnCode,int dOuomid,int uomid) {
        ProductMasterBO product;

        if (productId == null)
            return;

        product = businessModel.productHelper.getProductMasterBOById(productId);

        product.setSchemeProducts(null);
        product.setOrderedPcsQty(pieceQty);
        product.setOrderedCaseQty(caseQty);
        product.setOrderedOuterQty(outerQty);
        product.setOrderPricePiece(pricePerPiece);
        product.setSrp(srp);
        product.setCaseSize(caseSize);
        product.setOutersize(outerSize);
        product.setBatchid(batchId);
        product.setWeight(weight);
        product.setMSQty(msQty);
        product.setPcUomid(pcsUOMId);
        product.setPriceoffvalue(priceOffValue);
        product.setPriceOffId(priceOffId);
        product.setHsnCode(hsnCode);
        product.setOuUomid(dOuomid);
        product.setCaseUomId(uomid);

        product.setCheked(true);


    }

    private void loadSalesReturnData(String orderId,DBUtil db) {
        businessModel.reasonHelper.downloadSalesReturnReason();
        if (businessModel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {
            businessModel.productHelper.cloneReasonMaster(true);
        }

        try {
            String uId ="" ;
            //previously stored status fetched from DB and set to obj
            String sb = "select SI.productid,SI.Condition,SI.Pqty,SI.Cqty,SI.outerqty,SH.uid" +
                    " from SalesReturnDetails SI inner join SalesReturnHeader SH ON SH.uid=SI.uid " +
                    "where SH.Retailerid=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) + " and SH.upload='X' and SH.RefModuleTId = '"+orderId+"' and SH.distributorid=" + businessModel.getRetailerMasterBO().getDistributorId();
            Cursor c = db.selectSQL(sb);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    int productid = c.getInt(0);
                    String condition = c.getString(1);
                    int pqty = c.getInt(2);
                    int cqty = c.getInt(3);
                    int oqty = c.getInt(4);
                    setSalesReturnObject(productid, condition, pqty, cqty, oqty);

                    uId = c.getString(5);
                }
            }
            if (c != null) {
                c.close();
            }

            loadSalesReplacementData(db,uId);
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void setSalesReturnObject(int pid, String condition, int pqty, int cqty, int oqty) {

        ProductMasterBO productBO;

        productBO = businessModel.productHelper.getProductMasterBOById(Integer.toString(pid));

        if (productBO != null) {
            for (SalesReturnReasonBO bo : businessModel.reasonHelper.getReasonSalesReturnMaster()) {
                if (bo.getReasonID().equals(condition)) {
                    SalesReturnReasonBO reasonBo = new SalesReturnReasonBO();
                    reasonBo.setReasonDesc(bo.getReasonDesc());
                    reasonBo.setReasonID(bo.getReasonID());
                    reasonBo.setCaseSize(productBO.getCaseSize());
                    reasonBo.setOuterSize(productBO.getOutersize());
                    reasonBo.setProductShortName(productBO.getProductShortName());
                    reasonBo.setOldMrp(productBO.getMRP());
                    reasonBo.setSrpedit(productBO.getSrp());
                    reasonBo.setPieceQty(pqty);
                    reasonBo.setCaseQty(cqty);
                    reasonBo.setOuterQty(oqty);
                    productBO.getSalesReturnReasonList().add(reasonBo);
                    return;
                }
            }
        }
    }

    private void loadSalesReplacementData(DBUtil db,String uid) {
        String sb = "select pid,batchid,uomid,qty from SalesReturnReplacementDetails " +
                " where Retailerid=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) +
                " and upload='X' and uid = "+businessModel.QT(uid);
        Cursor c = db.selectSQL(sb);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String pid = c.getString(0);
                ProductMasterBO productBO;
                productBO = businessModel.productHelper.getProductMasterBOById(pid);
                if (productBO != null) {
                    int uomid = c.getInt(2);
                    if (uomid == productBO.getPcUomid()) {
                        productBO.setRepPieceQty(c.getInt(3));
                    } else if (uomid == productBO.getCaseUomId()) {
                        productBO.setRepCaseQty(c.getInt(3));
                    } else if (uomid == productBO.getOuUomid()) {
                        productBO.setRepOuterQty(c.getInt(3));
                    }
                }
            }
        }
        c.close();
    }

    public void clearSalesReturnTable() { //true -> Stock and Order --- false -> SalesReturn
        ProductMasterBO product;
        Vector<ProductMasterBO> productMaster ;

        productMaster = businessModel.productHelper.getProductMaster();

        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);

            product.setRepPieceQty(0);
            product.setRepCaseQty(0);
            product.setRepOuterQty(0);
            product.setSelectedSalesReturnPosition(0);

            if (product.getSalesReturnReasonList() != null && product.getSalesReturnReasonList().size() != 0) {
                for (SalesReturnReasonBO bo : product
                        .getSalesReturnReasonList()) {
                    if (bo.getCaseQty() > 0 || bo.getPieceQty() > 0 || bo.getOuterQty() > 0) {
                        bo.setCaseQty(0);
                        bo.setPieceQty(0);
                        bo.setOuterQty(0);
                        bo.setSrpedit(0);
                        bo.setMfgDate("");
                        bo.setExpDate("");
                        bo.setOldMrp(0);
                        bo.setLotNumber("");
                        bo.setInvoiceno("");

                    }
                }
            }
        }
    }

    public void downloadOrderedProducts(){
        orderedProductMasterBOS = new Vector<>();
        mTotalDeliverQtyByPid = new HashMap<>();

        for (ProductMasterBO product : businessModel.productHelper.getProductMaster()) {
            if(product.getOrderedPcsQty() > 0 || product.getOrderedCaseQty() > 0 || product.getOrderedOuterQty() > 0 || checkSalesReturnAvail(product)) {
                orderedProductMasterBOS.add(product);

                int prodQty = product.getOrderedPcsQty()
                        + (product.getOrderedCaseQty() * product.getCaseSize())
                        + (product.getOrderedOuterQty() * product.getOutersize());

                prodQty = prodQty + (product.getRepCaseQty() * product.getCaseSize()) + product.getRepPieceQty()
                        + (product.getRepOuterQty() * product.getOutersize());
                mTotalDeliverQtyByPid.put(product.getProductID(),prodQty);
            }
        }

        setOrderedProductMasterBOS(orderedProductMasterBOS);
    }

    public void downloadSchemeFreeProducts(Context context,String id) {

        schemeProductBOS = new ArrayList<>();

        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            SchemeProductBO schemeProductBO;

            Cursor c = db
                    .selectSQL("select schemeid,FreeProductID,FreeQty,UomID,pm.PName from schemeFreeProductDetail " +
                            " Inner Join ProductMaster pm ON pm.PID = FreeProductID "
                            + "where orderID="
                            + businessModel.QT(id)
                            + " and upload='X' order by schemeid");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    schemeProductBO = new SchemeProductBO();

                    schemeProductBO.setProductId(c.getString(1));
                    schemeProductBO.setQuantitySelected(c.getInt(2));
                    schemeProductBO.setProductName(c.getString(4));
                    schemeProductBO.setUomID(c.getInt(3));

                    schemeProductBOS.add(schemeProductBO);
                }
            }
            c.close();

            db.closeDB();

        }catch(Exception e){
            Commons.printException(e);
        }

        setSchemeProductBOS(schemeProductBOS);
    }

    public void downloadOrderDeliveryAmountDetail(Context context,String id) {
        try {

            setOrderDeliveryDiscountAmount("0");
            setOrderDeliveryTotalValue("0");

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor orderHeader = db.selectSQL("select OrderValue,discount from OrderHeader " +
                    "where orderid=" + businessModel.QT(id));
            if (orderHeader.getCount() > 0 && orderHeader.moveToNext()) {
                setOrderDeliveryDiscountAmount(orderHeader.getString(1));
                setOrderDeliveryTotalValue(orderHeader.getString(0));
            }
            orderHeader.close();

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private int getSchemeFreeProdCount(ProductMasterBO productMasterBO){

        int qty = 0;
        if(getSchemeProductBOS() == null || schemeProductBOS.size() == 0)
            return qty;

        for(SchemeProductBO schemeProductBO : getSchemeProductBOS()){
            if(schemeProductBO.getProductId().equals(productMasterBO.getProductID())){
                if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                    qty = schemeProductBO.getQuantitySelected();
                } else if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                    qty = schemeProductBO.getQuantitySelected()* productMasterBO.getCaseSize();
                } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                    qty = schemeProductBO.getQuantitySelected()* productMasterBO.getOutersize();
                }
            }
        }

        return qty;
    }

    public double getProductTotalValue() {
        double totalvalue = 0;
        int totalProdQty = 0;
        float taxValue = businessModel.productHelper.taxHelper.includeProductWiseTax(getOrderedProductMasterBOS());
        for (int i = 0; i < getOrderedProductMasterBOS().size(); i++) {
            ProductMasterBO prodBo = getOrderedProductMasterBOS().elementAt(i);
            if (prodBo.getOrderedPcsQty() != 0 || prodBo.getOrderedCaseQty() != 0
                    || prodBo.getOrderedOuterQty() != 0) {
                double temp = (prodBo.getOrderedPcsQty() * prodBo.getSrp())
                        + (prodBo.getOrderedCaseQty() * prodBo.getCsrp())
                        + prodBo.getOrderedOuterQty() * prodBo.getOsrp();
                totalvalue = totalvalue + temp;

                totalProdQty = prodBo.getOrderedPcsQty()
                        + (prodBo.getOrderedCaseQty() * prodBo.getCaseSize())
                        + (prodBo.getOrderedOuterQty() * prodBo.getOutersize());
            }

            if(prodBo.getRepCaseQty() > 0 || prodBo.getRepPieceQty() > 0 || prodBo.getRepOuterQty() > 0){
                totalProdQty = totalProdQty + (prodBo.getRepCaseQty() * prodBo.getCaseSize()) + prodBo.getRepPieceQty()
                        + (prodBo.getRepOuterQty() * prodBo.getOutersize());
            }
        }

        setOrderDeliveryTotalValue(String.valueOf(totalvalue));
        setOrderDeliveryTaxAmount(String.valueOf(taxValue));
        setTotalProductQty(totalProdQty);

        return totalvalue;
    }

    private boolean checkSalesReturnAvail(ProductMasterBO productMasterBO){

        for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
            if(obj.getCaseQty() > 0 || obj.getPieceQty() > 0 || obj.getOuterQty() > 0)
                return true;

        return false;
    }

    public boolean isSIHAvailable(boolean isEdit) {
        for(ProductMasterBO headProductMasterBO : businessModel.productHelper.getProductMaster()) {

            int qty = 0;
            if(!isEdit)
                qty = getSchemeFreeProdCount(headProductMasterBO);

            for (ProductMasterBO productBO : getOrderedProductMasterBOS()) {
                if(productBO.getProductID().equals(headProductMasterBO.getProductID())) {
                    int prodQty = productBO.getOrderedPcsQty()
                            + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                            + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                    prodQty = prodQty + (productBO.getRepCaseQty() * productBO.getCaseSize()) + productBO.getRepPieceQty()
                            + (productBO.getRepOuterQty() * productBO.getOutersize());

                    qty = prodQty + qty;
                    break;
                }
            }

            if (qty > 0) {
                if (headProductMasterBO.getDSIH() < qty) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean updateTableValues(Context context,String orderId,boolean isEdit){
        boolean status = true;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String invoiceId;
            // Normally Generating Invoice ID
            invoiceId = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            if (businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO) {
                String seqNo;
                businessModel.insertSeqNumber("INV");
                seqNo = businessModel.downloadSequenceNo("INV");
                invoiceId = seqNo;
            }

            double totalOrderValue =  SDUtil.convertToDouble(getOrderDeliveryTotalValue()) + SDUtil.convertToDouble(getOrderDeliveryTaxAmount());
            if(!isEdit)
                totalOrderValue = SDUtil.convertToDouble(getOrderDeliveryTotalValue()) - SDUtil.convertToDouble(getOrderDeliveryDiscountAmount()) ;

            if(isEdit){

                int linesPerCall = 0;

                for(ProductMasterBO productBo : getOrderedProductMasterBOS()){

                    int totalqty = (productBo.getOrderedPcsQty())
                            + (productBo.getCaseSize() * productBo.getOrderedCaseQty())
                            + (productBo.getOrderedOuterQty() * productBo.getOutersize());

                    if(totalqty > 0) {

                        int pieceCount = productBo.getOrderedPcsQty()
                                + productBo.getOrderedCaseQty() * productBo.getCaseSize()
                                + productBo.getOrderedOuterQty() * productBo.getOutersize();

                        int orderPieceQty = productBo.getOrderedPcsQty();
                        int orderCaseQty = productBo.getOrderedCaseQty();
                        int orderOuterQty = productBo.getOrderedOuterQty();
                        double priceOffValue = productBo.getPriceoffvalue() * pieceCount;
                        double line_total_price = (productBo.getOrderedCaseQty() * productBo
                                .getCsrp())
                                + (productBo.getOrderedPcsQty() * productBo.getSrp())
                                + (productBo.getOrderedOuterQty() * productBo.getOsrp());

                        String columns = "invoiceId,productid,qty,rate,uomdesc,retailerid,uomid,msqqty,uomCount,caseQty,pcsQty," +
                                "d1,d2,d3,DA,totalamount,outerQty,dOuomQty,dOuomid,batchid,upload,CasePrice,OuterPrice," +
                                "PcsUOMId,priceoffvalue,PriceOffId,weight,HsnCode";

                        String sb = (businessModel.QT(invoiceId) + ",") +
                                businessModel.QT(productBo.getProductID()) + "," +
                                totalqty + "," + productBo.getSrp() + "," +
                                businessModel.QT(productBo.getOU()) + "," +
                                businessModel.QT(businessModel.retailerMasterBO.getRetailerID()) +
                                "," + productBo.getCaseUomId() + "," +
                                productBo.getMSQty() + "," +
                                productBo.getCaseSize() + "," +
                                orderCaseQty + "," +
                                orderPieceQty + "," +
                                productBo.getD1() + "," + productBo.getD2() +
                                "," + productBo.getD3() + "," +
                                productBo.getDA() + "," +
                                line_total_price +
                                "," + orderOuterQty + "," +
                                productBo.getOutersize() + "," +
                                productBo.getOuUomid() + "," + productBo.getBatchid() +
                                "," + businessModel.QT("N") +
                                "," + productBo.getCsrp() + "," + productBo.getOsrp() + ","
                                + productBo.getPcUomid() +
                                "," + priceOffValue + "," + productBo.getPriceOffId() +
                                "," + productBo.getWeight() +
                                "," + businessModel.QT(productBo.getHsnCode());

                        db.insertSQL(DataMembers.tbl_InvoiceDetails, columns, sb);

                        linesPerCall = linesPerCall + 1;
                    }
                }

                String invoiceHeaderQry = "Insert into InvoiceMaster (invoiceno,invoicedate,retailerId,invNetamount," +
                        "orderid,ImageName,invoiceAmount,latitude,longitude,return_amt," +
                        "LinesPerCall,totalWeight,SalesType,sid,SParentID,stype," +
                        "imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3)" +
                        " select "+invoiceId+","+businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",retailerid,"+totalOrderValue+",orderid," +
                        "imagename,"+totalOrderValue+",latitude,longitude,ReturnValue,"+linesPerCall+",totalWeight,SalesType," +
                        "sid,SParentID,stype,imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3" +
                        " from OrderHeader where OrderId = "+businessModel.QT(orderId);


                db.executeQ(invoiceHeaderQry);

            }else{

                String invoiceHeaderQry = "Insert into InvoiceMaster (invoiceno,invoicedate,retailerId,invNetamount," +
                        "orderid,ImageName,discount,invoiceAmount,latitude,longitude,return_amt," +
                        "discount_type,LinesPerCall,totalWeight,SalesType,sid,SParentID,stype," +
                        "imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3)" +
                        " select "+invoiceId+","+businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",retailerid,ordervalue,orderid," +
                        "imagename,discount,ordervalue,latitude,longitude,ReturnValue,discount_type,LinesPerCall,totalWeight,SalesType," +
                        "sid,SParentID,stype,imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3" +
                        " from OrderHeader where OrderId = "+businessModel.QT(orderId);

                db.executeQ(invoiceHeaderQry);

                String invoiceDetailQry = "Insert into InvoiceDetails " +
                        " (ProductID,retailerid,uomid,Qty,Rate,uomCount,pcsQty,CaseQty,d1,d2,d3,DA,outerQty," +
                        " dOuomQty,dOuomid,batchid,CasePrice,OuterPrice,PcsUOMId,OrderType,HsnCode,RField1,totalamount,PriceOffValue,PriceOffId,weight,invoiceID) " +
                        " select ProductID,retailerid,uomid,Qty,Rate,uomcount,pieceqty,caseQty,d1,d2,d3,DA,outerQty," +
                        " dOuomQty,dOuomid,BatchId,CasePrice,OuterPrice,PcsUOMId,OrderType,HsnCode,RField1,totalamount,PriceOffValue,PriceOffId,weight,"+businessModel.QT(invoiceId) +
                        " from OrderDetail where OrderId = "+businessModel.QT(orderId);
                db.executeQ(invoiceDetailQry);

                String invoiceDiscountQry = "Insert into InvoiceDiscountDetail (OrderId,Pid,TypeId,Value,Percentage,ApplyLevelId," +
                        " RetailerId,DiscountId,isCompanyGiven,invoiceID) select OrderId,Pid,TypeId,Value,Percentage,ApplyLevelId," +
                        " RetailerId,DiscountId,isCompanyGiven,"+invoiceId+" from OrderDiscountDetail where OrderId = "+businessModel.QT(orderId);

                db.executeQ(invoiceDiscountQry);

                db.updateSQL("update SchemeFreeProductDetail set upload='N',InvoiceID = "+businessModel.QT(invoiceId)+" where orderId = "+businessModel.QT(orderId));
            }

            if (businessModel.configurationMasterHelper.SHOW_TAX) {
                if(businessModel.productHelper.taxHelper.getmTaxListByProductId()!=null
                        && businessModel.productHelper.taxHelper.getmTaxListByProductId().size() > 0)
                    saveProductLeveltax(orderId, db,totalOrderValue,invoiceId);
            }

            db.updateSQL("update OrderHeader set invoicestatus = 1 where orderId = "+businessModel.QT(orderId));
            db.updateSQL("update SalesReturnHeader set invoiceid = "+businessModel.QT(invoiceId)+" where RefModuleTId = "+businessModel.QT(orderId));

            String uid = "";
            Cursor c = db.selectSQL("select uid from SalesReturnHeader where RefModuleTId = "+businessModel.QT(orderId));
            if(c.getCount() > 0 && c.moveToNext()) {
                uid = c.getString(0);
                db.updateSQL("update SalesReturnDetails set invoiceno = " + businessModel.QT(invoiceId) + " where uid = " + businessModel.QT(uid));
                c.close();
            }

            if (businessModel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION || businessModel.configurationMasterHelper.TAX_SHOW_INVOICE){
                SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(context);
                salesReturnHelper.setSalesReturnID(businessModel.QT(uid));
                salesReturnHelper.saveSalesReturnTaxAndCreditNoteDetail(db, businessModel.QT(uid),"ORDER",businessModel.retailerMasterBO.getRpTypeCode());
            }

            updateOrderDeliverySIH(db,isEdit);

            //
            businessModel.invoiceNumber = invoiceId;
            if(isEdit)
                OrderHelper.getInstance(context).invoiceDiscount = "0";
            else
                OrderHelper.getInstance(context).invoiceDiscount = getOrderDeliveryDiscountAmount();

            for(int i = 0;i<getOrderHeaders().size();i++){
                OrderHeader orderHeader = getOrderHeaders().get(i);
                if(orderHeader.getOrderid().equalsIgnoreCase(orderId)){
                    orderHeader.setInvoiceStatus(1);
                    break;
                }
            }

            db.closeDB();

        }catch(Exception e){
            Commons.printException(e);
            status = false;
        }
        return status;
    }

    private void saveProductLeveltax(String orderId, DBUtil db,double totalOrderValue,String invoiceId) {

        for (ProductMasterBO bo : getOrderedProductMasterBOS()) {

            if (businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) != null
                    && businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()).size() != 0) {
                if (bo.getOrderedPcsQty() > 0
                        || bo.getOrderedCaseQty() > 0
                        || bo.getOrderedOuterQty() > 0) {
                    ArrayList<TaxBO> taxList = businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID());
                    if (taxList != null) {
                        int totalQty = bo.getOrderedPcsQty()
                                + bo.getOrderedCaseQty()
                                * bo.getCaseSize()
                                + bo.getOrderedOuterQty()
                                * bo.getOutersize();
                        double remainingValue = totalOrderValue / totalQty;
                        for (TaxBO taxBO : taxList) {
                            if (businessModel.configurationMasterHelper.SHOW_MRP_LEVEL_TAX) {
                                if (taxBO.getApplyRange() == 1) {
                                    if (taxBO.getMinValue() <= remainingValue
                                            && taxBO.getMaxValue() >= remainingValue) {
                                        insertProductLevelTax(orderId, db,
                                                bo, taxBO,invoiceId);
                                    }

                                } else if (taxBO.getApplyRange() == 0) {
                                    insertProductLevelTax(orderId, db,
                                            bo, taxBO,invoiceId);
                                }
                            } else {
                                insertProductLevelTax(orderId, db,
                                        bo, taxBO,invoiceId);
                            }
                        }
                    }
                }
            }
        }
    }

    private void insertProductLevelTax(String orderId, DBUtil db,
                                       ProductMasterBO productBO, TaxBO taxBO,String invoiceId) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct,invoiceid";
        StringBuffer values = new StringBuffer();

        values.append(orderId + "," + productBO.getProductID() + ","
                + taxBO.getTaxRate() + ",");
        values.append(taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + businessModel.getRetailerMasterBO().getRetailerID());
        values.append("," + taxBO.getGroupId() + ",0" +","+businessModel.QT(invoiceId));
        db.insertSQL("InvoiceTaxDetails", columns, values.toString());
    }

    private void updateOrderDeliverySIH(DBUtil db,boolean isEdit){
        try {

            for(ProductMasterBO headProductMasterBO : businessModel.productHelper.getProductMaster()) {
                int qty = 0;
                int updateExcessSih = 0;
                if(!isEdit)
                    qty = getSchemeFreeProdCount(headProductMasterBO);

                for (ProductMasterBO productBO : getOrderedProductMasterBOS()) {
                    if(productBO.getProductID().equals(headProductMasterBO.getProductID())) {
                        int prodQty = productBO.getOrderedPcsQty()
                                + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                                + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                        prodQty = prodQty + (productBO.getRepCaseQty() * productBO.getCaseSize()) + productBO.getRepPieceQty()
                                + (productBO.getRepOuterQty() * productBO.getOutersize());


                        if(mTotalDeliverQtyByPid.get(productBO.getProductID())!=null){
                            if(mTotalDeliverQtyByPid.get(productBO.getProductID()) > prodQty){
                                updateExcessSih = mTotalDeliverQtyByPid.get(productBO.getProductID()) - prodQty;
                            }
                        }

                        qty = prodQty + qty;
                        break;
                    }
                }

                if (qty > 0 || updateExcessSih > 0) {

                    int totalSIH = headProductMasterBO.getDSIH() - qty;

                    ProductMasterBO productMasterBO = businessModel.productHelper.getProductMasterBOById(headProductMasterBO.getProductID());
                    productMasterBO.setDSIH(totalSIH);
                    productMasterBO.setSIH(totalSIH);
                    db.updateSQL("update stockinhandmaster set qty = " +
                            totalSIH + " where pid=" + headProductMasterBO.getProductID() + " and batchid= 0");
                    db.updateSQL("update ProductMaster set sih = " +
                            totalSIH + " where PID=" + headProductMasterBO.getProductID() );


                    if(updateExcessSih > 0){
                        Cursor c =  db.selectSQL("select qty from ExcessStockInHand where pid = "+ headProductMasterBO.getProductID());

                        if(c.getCount() > 0 && c.moveToNext() ){
                            updateExcessSih = c.getInt(0)+updateExcessSih;
                            db.executeQ("update ExcessStockInHand set qty="+updateExcessSih+" where pid = "+ headProductMasterBO.getProductID());
                        }else {
                            db.executeQ("insert into ExcessStockInHand (qty,pid) values("+updateExcessSih+","+headProductMasterBO.getProductID()+")");
                        }

                        c.close();
                    }
                }
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    private HashMap<String,Integer> excessQtyMap = new HashMap<>();

    public int getExcessQtyById(String productId) {
        if (excessQtyMap == null || excessQtyMap.size() == 0)
            return 0;
        return excessQtyMap.get(productId)!=null?excessQtyMap.get(productId):0;
    }

    public void updateProductWithExcessStock(Context mContext){
        try{
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor cur = db.selectSQL("Select pid,qty from ExcessStockInHand");
            if(cur.getCount() > 0){
                while (cur.moveToNext()){
                    excessQtyMap.put(cur.getString(0),cur.getInt(1));
                }
            }
            cur.close();

            for(int i = 0;i < businessModel.productHelper.getProductMaster().size();i++){
                ProductMasterBO productMasterBO = businessModel.productHelper.getProductMaster().elementAt(i);
                if(excessQtyMap.get(productMasterBO.getProductID())!=null)
                    productMasterBO.setSIH(excessQtyMap.get(productMasterBO.getProductID()));
                else
                    productMasterBO.setSIH(0);
            }

        }catch(Exception e){
            Commons.printException(e);
        }
    }

    class downloadOrderDeliveryDetail extends AsyncTask<Void,Void,Void> {

        private String orderId;
        private String from;
        private int invoiceStatus;
        private Context context;

        private downloadOrderDeliveryDetail(String orderId,String from,int invoiceStatus, Context context){
            this.orderId = orderId;
            this.from = from;
            this.invoiceStatus = invoiceStatus;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            businessModel.productHelper.clearOrderTable();
            clearSalesReturnTable();
            downloadOrderDeliveryDetail(context,orderId);
            downloadSchemeFreeProducts(context,orderId);
            downloadOrderDeliveryAmountDetail(context,orderId);
            downloadOrderedProducts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(from.equalsIgnoreCase("Approve")) {

                if (isSIHAvailable(false)) {

                    CommonDialog dialog = new CommonDialog(context.getApplicationContext(), context, "", context.getResources().getString(R.string.order_delivery_approve), false,
                            context.getResources().getString(R.string.ok), context.getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {

                            new UpdateOrderDeliveryTable(orderId,context,false).execute();

                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    });
                    dialog.show();
                    dialog.setCancelable(false);
                } else {
                    //Todo -- display toast based on condition
                }

            }

            else {
                //Todo --- Move to next activity
            }
        }
    }

    public class UpdateOrderDeliveryTable extends AsyncTask<Void,Void,Boolean>{

        private String orderId;
        private Context context;
        private boolean isEdit;

        private UpdateOrderDeliveryTable(String orderId, Context context,boolean isEdit){
            this.orderId = orderId;
            this.context = context;
            this.isEdit = isEdit;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return updateTableValues(context, orderId,isEdit);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {

        }
    }


}
