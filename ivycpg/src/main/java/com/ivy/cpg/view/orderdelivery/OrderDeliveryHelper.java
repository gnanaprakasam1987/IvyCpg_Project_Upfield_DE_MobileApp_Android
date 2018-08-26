package com.ivy.cpg.view.orderdelivery;

 /*New Module Order Delivery Details
    Download methods for Order Header,Order Detail,Salesreturn header
    Salesreturn Detail,Replacement Detail,SchemeFree product details
    Insertion and updation in Invoice Master, Invoice Detail, Invoice Tax, Invoice Discount
    */

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
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
    private ArrayList<SchemeProductBO> schemeProductBOS = new ArrayList<>();
    private HashMap<String, Integer> mTotalDeliverQtyByPid = new HashMap<>();
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

    int getTotalProductQty() {
        return totalProductQty;
    }

    private void setTotalProductQty(int totalProductQty) {
        this.totalProductQty = totalProductQty;
    }

    public ArrayList<OrderHeader> getOrderHeaders() {
        return orderHeaders;
    }

    private void setOrderHeaders(ArrayList<OrderHeader> orderHeaders) {
        this.orderHeaders = orderHeaders;
    }

    Vector<ProductMasterBO> getOrderedProductMasterBOS() {
        return orderedProductMasterBOS;
    }

    private void setOrderedProductMasterBOS(Vector<ProductMasterBO> orderedProductMasterBOS) {
        this.orderedProductMasterBOS = orderedProductMasterBOS;
    }

    String getOrderDeliveryDiscountAmount() {
        return orderDeliveryDiscountAmount;
    }

    private void setOrderDeliveryDiscountAmount(String orderDeliveryDiscountAmount) {
        this.orderDeliveryDiscountAmount = orderDeliveryDiscountAmount;
    }

    ArrayList<SchemeProductBO> getSchemeProductBOS() {
        return schemeProductBOS;
    }

    private void setSchemeProductBOS(ArrayList<SchemeProductBO> schemeProductBOS) {
        this.schemeProductBOS = schemeProductBOS;
    }

    String getOrderDeliveryTaxAmount() {
        return orderDeliveryTaxAmount;
    }

    private void setOrderDeliveryTaxAmount(String orderDeliveryTaxAmount) {
        this.orderDeliveryTaxAmount = orderDeliveryTaxAmount;
    }

    String getOrderDeliveryTotalValue() {
        return orderDeliveryTotalValue;
    }

    private void setOrderDeliveryTotalValue(String orderDeliveryTotalValue) {
        this.orderDeliveryTotalValue = orderDeliveryTotalValue;
    }

    public void downloadOrderDeliveryHeader(Context mContext) {

        orderHeaders = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sb = ("select OD.OrderID,OrderValue,LinesPerCall,OrderDate,invoicestatus from "
                    + DataMembers.tbl_orderHeader + " OD ") +
                    " where OD.upload='X' and OD.RetailerID="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID());

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

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    void downloadOrderDeliveryDetail(Context mContext, String orderId) {
        try {
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
                            outerQty, srp, orderDetailCursor.getDouble(4), caseSize, outerSize, batchId, weight, orderDetailCursor.getInt(14),
                            orderDetailCursor.getInt(15), orderDetailCursor.getFloat(16), orderDetailCursor.getInt(17),
                            orderDetailCursor.getString(18), orderDetailCursor.getInt(19), orderDetailCursor.getInt(20));
                }

                orderDetailCursor.close();

                loadSalesReturnData(orderId, db,mContext);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void setOrderDeliveryProductDetails(String productId, int caseQty, int pieceQty,
                                                int outerQty, float srp, double pricePerPiece, int caseSize, int outerSize,
                                                String batchId, float weight, int msQty, int pcsUOMId, float priceOffValue,
                                                int priceOffId, String hsnCode, int dOuomid, int uomid) {
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

    private void loadSalesReturnData(String orderId, DBUtil db , Context mContext) {
        businessModel.reasonHelper.downloadSalesReturnReason();
        if (businessModel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {
            SalesReturnHelper.getInstance(mContext).cloneReasonMaster(true);
        }

        try {
            String uId = "";
            //previously stored status fetched from DB and set to obj
            String sb = "select SI.productid,SI.Condition,SI.Pqty,SI.Cqty,SI.outerqty,SH.uid" +
                    " from SalesReturnDetails SI inner join SalesReturnHeader SH ON SH.uid=SI.uid " +
                    "where SH.Retailerid=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) + " and SH.RefModuleTId = '" + orderId + "' and SH.distributorid=" + businessModel.getRetailerMasterBO().getDistributorId();
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

            loadSalesReplacementData(db, uId);
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

    private void loadSalesReplacementData(DBUtil db, String uid) {
        String sb = "select pid,batchid,uomid,qty from SalesReturnReplacementDetails " +
                " where Retailerid=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) +
                " and uid = " + businessModel.QT(uid);
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

    void clearSalesReturnTable() { //true -> Stock and Order --- false -> SalesReturn
        ProductMasterBO product;
        Vector<ProductMasterBO> productMaster;

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

    void downloadOrderedProducts() {
        orderedProductMasterBOS = new Vector<>();
        mTotalDeliverQtyByPid = new HashMap<>();

        for (ProductMasterBO product : businessModel.productHelper.getProductMaster()) {
            if (product.getOrderedPcsQty() > 0 || product.getOrderedCaseQty() > 0 || product.getOrderedOuterQty() > 0 || checkSalesReturnAvail(product)) {
                orderedProductMasterBOS.add(product);

                int prodQty = product.getOrderedPcsQty()
                        + (product.getOrderedCaseQty() * product.getCaseSize())
                        + (product.getOrderedOuterQty() * product.getOutersize());

                prodQty = prodQty + (product.getRepCaseQty() * product.getCaseSize()) + product.getRepPieceQty()
                        + (product.getRepOuterQty() * product.getOutersize());
                mTotalDeliverQtyByPid.put(product.getProductID(), prodQty);
            }
        }

        setOrderedProductMasterBOS(orderedProductMasterBOS);
    }

    void downloadSchemeFreeProducts(Context context, String id) {

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
                            + " order by schemeid");
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

        } catch (Exception e) {
            Commons.printException(e);
        }

        setSchemeProductBOS(schemeProductBOS);
    }

    void downloadOrderDeliveryAmountDetail(Context context, String id) {
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

    private int getSchemeFreeProdCount(ProductMasterBO productMasterBO) {

        int qty = 0;
        if (getSchemeProductBOS() == null || schemeProductBOS.size() == 0)
            return qty;

        for (SchemeProductBO schemeProductBO : getSchemeProductBOS()) {
            if (schemeProductBO.getProductId().equals(productMasterBO.getProductID())) {
                if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                    qty = schemeProductBO.getQuantitySelected();
                } else if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                    qty = schemeProductBO.getQuantitySelected() * productMasterBO.getCaseSize();
                } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                    qty = schemeProductBO.getQuantitySelected() * productMasterBO.getOutersize();
                }
            }
        }

        return qty;
    }

    /* Total product count, value of total product,total tax are calculated here */
    public double getProductTotalValue() {
        double totalvalue = 0;
        int totalProdQty = 0;
        //Product wise Tax amount will be calculated according to the tax rate
        double taxValue = businessModel.productHelper.taxHelper.updateProductWiseIncludeTax(getOrderedProductMasterBOS());
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

            if (prodBo.getRepCaseQty() > 0 || prodBo.getRepPieceQty() > 0 || prodBo.getRepOuterQty() > 0) {
                totalProdQty = totalProdQty + (prodBo.getRepCaseQty() * prodBo.getCaseSize()) + prodBo.getRepPieceQty()
                        + (prodBo.getRepOuterQty() * prodBo.getOutersize());
            }
        }

        setOrderDeliveryTotalValue(String.valueOf(totalvalue));
        setOrderDeliveryTaxAmount(String.valueOf(taxValue));
        setTotalProductQty(totalProdQty);

        return totalvalue;
    }

    private boolean checkSalesReturnAvail(ProductMasterBO productMasterBO) {

        for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
            if (obj.getCaseQty() > 0 || obj.getPieceQty() > 0 || obj.getOuterQty() > 0)
                return true;

        return false;
    }

    boolean isSIHAvailable(boolean isEdit) {
        for (ProductMasterBO headProductMasterBO : businessModel.productHelper.getProductMaster()) {

            int qty = 0;
            if (!isEdit)
                qty = getSchemeFreeProdCount(headProductMasterBO);

            for (ProductMasterBO productBO : getOrderedProductMasterBOS()) {
                if (productBO.getProductID().equals(headProductMasterBO.getProductID())) {
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

    boolean updateTableValues(Context context, String orderId, boolean isEdit,String menuCode) {
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

            double totalOrderValue = SDUtil.convertToDouble(getOrderDeliveryTotalValue());
            if (!isEdit)
                totalOrderValue = SDUtil.convertToDouble(getOrderDeliveryTotalValue()) - SDUtil.convertToDouble(getOrderDeliveryDiscountAmount());


            int salesReturned = 0;
            double discountedAmount = 0;

            try {

                int isCreditNoteCreated = isCreditNoteCreated(context, orderId);

                if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                        && isCreditNoteCreated != 1
                        && isValueReturned(context, orderId))
                    salesReturned = 1;
                else
                    salesReturned = 0;

                double discountPercentage = businessModel.collectionHelper.getSlabwiseDiscountpercentage();

                if (businessModel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                    if (discountPercentage > 0) {

                        double remainingAmount = (SDUtil.convertToDouble(getOrderDeliveryTotalValue()) * discountPercentage) / 100;
                        remainingAmount = SDUtil.convertToDouble(businessModel.formatValueBasedOnConfig(remainingAmount));

                        discountedAmount = SDUtil.convertToDouble(getOrderDeliveryTotalValue()) - remainingAmount;
                    } else {
                        //discountedAmount = SDUtil.convertToDouble(getOrderDeliveryTotalValue());
                        discountedAmount = (SDUtil.convertToDouble(getOrderDeliveryTaxAmount()) +
                                SDUtil.convertToDouble(getOrderDeliveryTotalValue())) - SDUtil.convertToDouble(getOrderDeliveryDiscountAmount());
                    }
                } else {
                    //discountedAmount = SDUtil.convertToDouble(getOrderDeliveryTotalValue());
                    discountedAmount = (SDUtil.convertToDouble(getOrderDeliveryTaxAmount()) +
                            SDUtil.convertToDouble(getOrderDeliveryTotalValue())) - SDUtil.convertToDouble(getOrderDeliveryDiscountAmount());
                }
            } catch (Exception e) {
                Commons.printException(e);
            }

            double totalAmount = getOrderedTotalValue();
            if (isEdit) {

                int linesPerCall = 0;

                for (ProductMasterBO productBo : getOrderedProductMasterBOS()) {

                    int totalqty = (productBo.getOrderedPcsQty())
                            + (productBo.getCaseSize() * productBo.getOrderedCaseQty())
                            + (productBo.getOrderedOuterQty() * productBo.getOutersize());

                    if (totalqty > 0) {

                        int pieceCount = productBo.getOrderedPcsQty()
                                + productBo.getOrderedCaseQty() * productBo.getCaseSize()
                                + productBo.getOrderedOuterQty() * productBo.getOutersize();

                        int orderPieceQty = productBo.getOrderedPcsQty();
                        int orderCaseQty = productBo.getOrderedCaseQty();
                        int orderOuterQty = productBo.getOrderedOuterQty();
                        double priceOffValue = productBo.getPriceoffvalue() * pieceCount;
//                        double line_total_price = (productBo.getOrderedCaseQty() * productBo
//                                .getCsrp())
//                                + (productBo.getOrderedPcsQty() * productBo.getSrp())
//                                + (productBo.getOrderedOuterQty() * productBo.getOsrp());

                        double line_total_price = productBo.getDiscount_order_value();

                        String columns = "invoiceId,productid,qty,rate,uomdesc,retailerid,uomid,msqqty,uomCount,caseQty,pcsQty," +
                                "d1,d2,d3,DA,totalamount,outerQty,dOuomQty,dOuomid,batchid,upload,CasePrice,OuterPrice," +
                                "PcsUOMId,priceoffvalue,PriceOffId,weight,HsnCode,NetAmount";

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
                                "," + businessModel.QT(productBo.getHsnCode())+
                                "," + line_total_price;

                        db.insertSQL(DataMembers.tbl_InvoiceDetails, columns, sb);

                        linesPerCall = linesPerCall + 1;
                    }
                }

                String invoiceHeaderQry = "Insert into InvoiceMaster (invoiceno,invoicedate,beatid,retailerId,invNetamount,discountedAmount," +
                        "orderid,ImageName,invoiceAmount,latitude,longitude,return_amt," +
                        "LinesPerCall,totalWeight,SalesType,sid,SParentID,stype," +
                        "imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3,upload,TaxAmount,salesreturned,creditPeriod,IsPreviousInvoice,totalamount,paidamount)" +
                        " select " + invoiceId + "," + businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",RouteId,retailerid," +
                        (totalOrderValue + SDUtil.convertToDouble(getOrderDeliveryTaxAmount())) + "," + (totalOrderValue + SDUtil.convertToDouble(getOrderDeliveryTaxAmount())) + ",orderid," +
                        "imagename," + (totalOrderValue) + ",latitude,longitude,ReturnValue," + linesPerCall + ",totalWeight,SalesType," +
                        "sid,SParentID,stype,imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3,'N'," + businessModel.QT(getOrderDeliveryTaxAmount()) + " , " + salesReturned + " , " + businessModel.getRetailerMasterBO().getCreditDays() + " , " + 0 +
                        "," +totalAmount + ",0 from OrderHeader where OrderId = " + businessModel.QT(orderId);


                db.executeQ(invoiceHeaderQry);

            } else {

                String invoiceHeaderQry = "Insert into InvoiceMaster (invoiceno,invoicedate,beatid,retailerId,invNetamount," +
                        "orderid,ImageName,discount,invoiceAmount,latitude,longitude,return_amt," +
                        "discount_type,LinesPerCall,totalWeight,SalesType,sid,SParentID,stype," +
                        "imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3,upload,TaxAmount,salesreturned,creditPeriod,IsPreviousInvoice,discountedAmount,totalamount,paidamount)" +
                        " select " + invoiceId + "," + businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",RouteId,retailerid," + businessModel.QT(businessModel.formatValueBasedOnConfig(totalOrderValue + SDUtil.convertToDouble(getOrderDeliveryTaxAmount()))) +
                        ",orderid,imagename,discount," + businessModel.QT(getOrderDeliveryTotalValue()) +",latitude,longitude,ReturnValue,discount_type,LinesPerCall,totalWeight,SalesType," +
                        "sid,SParentID,stype,imgName,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3,'N'," +
                        businessModel.QT(getOrderDeliveryTaxAmount()) + " , " + salesReturned + " , " + businessModel.getRetailerMasterBO().getCreditDays() + " , " + 0 + " , " + businessModel.QT(businessModel.formatValueBasedOnConfig(discountedAmount)) +
                        "," +totalAmount + ",0 from OrderHeader where OrderId = " + businessModel.QT(orderId);

                db.executeQ(invoiceHeaderQry);

                String invoiceDetailQry = "Insert into InvoiceDetails " +
                        " (ProductID,retailerid,uomid,Qty,Rate,uomCount,pcsQty,CaseQty,d1,d2,d3,DA,outerQty," +
                        " dOuomQty,dOuomid,batchid,CasePrice,OuterPrice,PcsUOMId,OrderType,HsnCode,RField1,totalamount,PriceOffValue,PriceOffId,weight,invoiceID,NetAmount) " +
                        " select ProductID,retailerid,uomid,Qty,Rate,uomcount,pieceqty,caseQty,d1,d2,d3,DA,outerQty," +
                        " dOuomQty,dOuomid,BatchId,CasePrice,OuterPrice,PcsUOMId,OrderType,HsnCode,RField1,totalamount,PriceOffValue,PriceOffId,weight," + businessModel.QT(invoiceId) +
                        " ,NetAmount from OrderDetail where OrderId = " + businessModel.QT(orderId);
                db.executeQ(invoiceDetailQry);

                String invoiceDiscountQry = "Insert into InvoiceDiscountDetail (OrderId,Pid,TypeId,Value,Percentage,ApplyLevelId," +
                        " RetailerId,DiscountId,isCompanyGiven,invoiceID) select OrderId,Pid,TypeId,Value,Percentage,ApplyLevelId," +
                        " RetailerId,DiscountId,isCompanyGiven," + invoiceId + " from OrderDiscountDetail where OrderId = " + businessModel.QT(orderId);

                db.executeQ(invoiceDiscountQry);

                db.updateSQL("update SchemeFreeProductDetail set upload='N',InvoiceID = " + businessModel.QT(invoiceId) + " where orderId = " + businessModel.QT(orderId));
            }

            if (businessModel.configurationMasterHelper.SHOW_TAX) {
                if (businessModel.productHelper.taxHelper.getmTaxListByProductId() != null
                        && businessModel.productHelper.taxHelper.getmTaxListByProductId().size() > 0)
                    saveProductLeveltax(orderId, db, totalOrderValue, invoiceId);
            }

            db.updateSQL("update OrderHeader set invoicestatus = 1,totalamount =" + totalAmount +" where orderId = " + businessModel.QT(orderId));
            db.updateSQL("update SalesReturnHeader set upload='N',invoiceid = " + businessModel.QT(invoiceId) + " where RefModuleTId = " + businessModel.QT(orderId));

            String uid = "";
            Cursor c = db.selectSQL("select uid from SalesReturnHeader where RefModuleTId = " + businessModel.QT(orderId));
            if (c.getCount() > 0 && c.moveToNext()) {
                uid = c.getString(0);
                db.updateSQL("update SalesReturnDetails set upload='N', invoiceno = " + businessModel.QT(invoiceId) + " where uid = " + businessModel.QT(uid));
                c.close();
                db.updateSQL("update SalesReturnReplacementDetails set upload='N' where uid = " + businessModel.QT(uid));
            }

            if (businessModel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION || businessModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(context);
                salesReturnHelper.setSalesReturnID(businessModel.QT(uid));
                salesReturnHelper.saveSalesReturnTaxAndCreditNoteDetail(db, businessModel.QT(uid), "ORDER", businessModel.retailerMasterBO.getRpTypeCode(),false);
            }

            updateOrderDeliverySIH(db, isEdit);

            //For Print saved in Discount and invoice number
            businessModel.invoiceNumber = invoiceId;
            if (isEdit)
                OrderHelper.getInstance(context).invoiceDiscount = "0";
            else
                OrderHelper.getInstance(context).invoiceDiscount = getOrderDeliveryDiscountAmount();

            /* Invoice status 1 --> invoice generated for the order */
            for (int i = 0; i < getOrderHeaders().size(); i++) {
                OrderHeader orderHeader = getOrderHeaders().get(i);
                if (orderHeader.getOrderid().equalsIgnoreCase(orderId)) {
                    orderHeader.setInvoiceStatus(1);
                    break;
                }
            }

            db.closeDB();

            businessModel.saveModuleCompletion(menuCode);

        } catch (Exception e) {
            Commons.printException(e);
            status = false;
        }
        return status;
    }

    private void saveProductLeveltax(String orderId, DBUtil db, double totalOrderValue, String invoiceId) {

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
                                                bo, taxBO, invoiceId);
                                    }

                                } else if (taxBO.getApplyRange() == 0) {
                                    insertProductLevelTax(orderId, db,
                                            bo, taxBO, invoiceId);
                                }
                            } else {
                                insertProductLevelTax(orderId, db,
                                        bo, taxBO, invoiceId);
                            }
                        }
                    }
                }
            }
        }
    }

    private void insertProductLevelTax(String orderId, DBUtil db,
                                       ProductMasterBO productBO, TaxBO taxBO, String invoiceId) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct,invoiceid";
        StringBuffer values = new StringBuffer();

        values.append(businessModel.QT(orderId) + "," + productBO.getProductID() + ","
                + taxBO.getTaxRate() + ",");
        values.append(taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + businessModel.getRetailerMasterBO().getRetailerID());
        values.append("," + taxBO.getGroupId() + ",0" + "," + businessModel.QT(invoiceId));
        db.insertSQL("InvoiceTaxDetails", columns, values.toString());
    }

    public int isCreditNoteCreated(Context mContext, String orderId) {
        int flag = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select credit_flag from SalesReturnHeader where upload = 'X' and RefModuleTId =" + businessModel.QT(orderId) + " and RetailerID="
                            + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    flag = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isValueReturned(Context mContext, String orderId) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select invoicecreated from SalesReturnHeader where upload ='X' and RefModuleTId =" + businessModel.QT(orderId) + " and Retailerid="
                            + businessModel.getRetailerMasterBO().getRetailerID() + " and distributorid=" + businessModel.retailerMasterBO.getDistributorId());
            if (c != null) {
                while (c.moveToNext()) {
                    flag = c.getDouble(0) == 1;
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }


    /* To update SIH values in Product master and in Excess Stock master */
    private void updateOrderDeliverySIH(DBUtil db, boolean isEdit) {
        try {

            for (ProductMasterBO headProductMasterBO : businessModel.productHelper.getProductMaster()) {
                int qty = 0;
                int updateExcessSih = 0;
                if (!isEdit)
                    qty = getSchemeFreeProdCount(headProductMasterBO);

                for (ProductMasterBO productBO : getOrderedProductMasterBOS()) {
                    if (productBO.getProductID().equals(headProductMasterBO.getProductID())) {
                        int prodQty = productBO.getOrderedPcsQty()
                                + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                                + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                        prodQty = prodQty + (productBO.getRepCaseQty() * productBO.getCaseSize()) + productBO.getRepPieceQty()
                                + (productBO.getRepOuterQty() * productBO.getOutersize());


                        if (mTotalDeliverQtyByPid.get(productBO.getProductID()) != null) {
                            if (mTotalDeliverQtyByPid.get(productBO.getProductID()) > prodQty) {
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
                            totalSIH + " where PID=" + headProductMasterBO.getProductID());


                    if (updateExcessSih > 0) {
                        Cursor c = db.selectSQL("select qty from ExcessStockInHand where pid = " + headProductMasterBO.getProductID());

                        if (c.getCount() > 0 && c.moveToNext()) {
                            updateExcessSih = c.getInt(0) + updateExcessSih;
                            db.executeQ("update ExcessStockInHand set qty=" + updateExcessSih + " where pid = " + headProductMasterBO.getProductID());
                        } else {
                            db.executeQ("insert into ExcessStockInHand (qty,pid) values(" + updateExcessSih + "," + headProductMasterBO.getProductID() + ")");
                        }

                        c.close();
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private HashMap<String, Integer> excessQtyMap = new HashMap<>();

    public int getExcessQtyById(String productId) {
        if (excessQtyMap == null || excessQtyMap.size() == 0)
            return 0;
        return excessQtyMap.get(productId) != null ? excessQtyMap.get(productId) : 0;
    }

    /*Update Excess stock QTY in Product master SIH. Works only inside this module*/
    public void updateProductWithExcessStock(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor cur = db.selectSQL("Select pid,qty from ExcessStockInHand");
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    excessQtyMap.put(cur.getString(0), cur.getInt(1));
                }
            }
            cur.close();

            for (int i = 0; i < businessModel.productHelper.getProductMaster().size(); i++) {
                ProductMasterBO productMasterBO = businessModel.productHelper.getProductMaster().elementAt(i);
                if (excessQtyMap.get(productMasterBO.getProductID()) != null)
                    productMasterBO.setSIH(excessQtyMap.get(productMasterBO.getProductID()));
                else
                    productMasterBO.setSIH(0);
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /*Preparing ProductMasterBo For printing purpose*/
    Vector<ProductMasterBO> preparePrintData(Context context, String orderId) {
        Vector<ProductMasterBO> mInvoiceDetailsList = new Vector<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String invoiceno = "";
            Cursor cursor = db.selectSQL("select invoiceno,discount from invoicemaster where orderid =" + businessModel.QT(orderId));
            if (cursor.getCount() > 0 && cursor.moveToNext()) {
                invoiceno = cursor.getString(0);
                OrderHelper.getInstance(context).invoiceDiscount = cursor.getString(1);

                cursor.close();
            }
            businessModel.invoiceNumber = invoiceno;

            StringBuffer sb = new StringBuffer();
            sb.append("select PM.pid,PM.psname,ID.pcsQty,ID.caseQty,ID.OuterQty,ifnull(BM.Batchnum,\"\"),PM.duomQty,PM.douomQty,ID.Qty,PM.piece_uomid,PM.dUomId,PM.dOuomid,rate,caseprice,outerprice from InvoiceDetails ID ");
            sb.append("inner join Productmaster PM on PM.pid=ID.productid ");
            sb.append("left join batchmaster BM on ID.productid=BM.pid and ID.batchid=BM.batchid ");
            sb.append("where ID.invoiceID=" + businessModel.QT(invoiceno));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                ProductMasterBO productBO;
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductShortName(c.getString(1));
                    productBO.setOrderedPcsQty(c.getInt(2));
                    productBO.setOrderedCaseQty(c.getInt(3));
                    productBO.setOrderedOuterQty(c.getInt(4));

                    productBO.setBatchNo(c.getString(5));
                    productBO.setCaseSize(c.getInt(6));
                    productBO.setOutersize(c.getInt(7));
                    productBO.setTotalQty(c.getInt(8));
                    productBO.setPcUomid(c.getInt(9));
                    productBO.setCaseUomId(c.getInt(10));
                    productBO.setOuUomid(c.getInt(11));
                    productBO.setSrp(c.getFloat(12));
                    productBO.setCsrp(c.getFloat(13));
                    productBO.setOsrp(c.getFloat(14));
                    productBO.setCheked(true);

                    mInvoiceDetailsList.add(productBO);
                }
                c.close();
            }

            String replQry = "select pid,batchid,uomid,qty from SalesReturnHeader SH" +
                    " inner join SalesReturnReplacementDetails SRPD on SRPD.uid = SH.uid where SH.invoiceid =" + businessModel.QT(invoiceno);
            Cursor cursorRpl = db.selectSQL(replQry);
            if (cursorRpl.getCount() > 0) {
                while (cursorRpl.moveToNext()) {
                    String pid = cursorRpl.getString(0);
                    boolean isProductAvail = false;
                    for (int i = 0; i < mInvoiceDetailsList.size(); i++) {
                        if (mInvoiceDetailsList.get(i).getProductID().equals(pid)) {
                            isProductAvail = true;
                            int uomid = cursorRpl.getInt(2);
                            if (uomid == mInvoiceDetailsList.get(i).getPcUomid()) {
                                mInvoiceDetailsList.get(i).setRepPieceQty(cursorRpl.getInt(3));
                            } else if (uomid == mInvoiceDetailsList.get(i).getCaseUomId()) {
                                mInvoiceDetailsList.get(i).setRepCaseQty(cursorRpl.getInt(3));
                            } else if (uomid == mInvoiceDetailsList.get(i).getOuUomid()) {
                                mInvoiceDetailsList.get(i).setRepOuterQty(cursorRpl.getInt(3));
                            }
                        }
                    }

                    if (!isProductAvail) {
                        ProductMasterBO productBORplace = new ProductMasterBO();
                        if (businessModel.productHelper.getProductMasterBOById(pid) != null) {
                            int uomid = cursorRpl.getInt(2);
                            if (uomid == businessModel.productHelper.getProductMasterBOById(pid).getPcUomid()) {
                                productBORplace.setRepPieceQty(cursorRpl.getInt(3));
                            } else if (uomid == businessModel.productHelper.getProductMasterBOById(pid).getCaseUomId()) {
                                productBORplace.setRepCaseQty(cursorRpl.getInt(3));
                            } else if (uomid == businessModel.productHelper.getProductMasterBOById(pid).getOuUomid()) {
                                productBORplace.setRepOuterQty(cursorRpl.getInt(3));
                            }
                            productBORplace.setProductName(businessModel.productHelper.getProductMasterBOById(pid).getProductName());
                            productBORplace.setProductID(pid);
                            productBORplace.setCaseUomId(businessModel.productHelper.getProductMasterBOById(pid).getCaseUomId());
                            productBORplace.setPcUomid(businessModel.productHelper.getProductMasterBOById(pid).getPcUomid());
                            productBORplace.setOuUomid(businessModel.productHelper.getProductMasterBOById(pid).getOuUomid());
                            mInvoiceDetailsList.add(productBORplace);
                        }
                    }
                }
                cursorRpl.close();
            }

            businessModel.productHelper.taxHelper.updateProductWiseIncludeTax(mInvoiceDetailsList);

            /*Updating master tax value for print purpose*/
            for (ProductMasterBO productMasterBO : mInvoiceDetailsList) {
                if (businessModel.productHelper.getProductMasterBOById(productMasterBO.getProductID()) != null)
                    businessModel.productHelper.getProductMasterBOById(productMasterBO.getProductID()).setTaxValue(productMasterBO.getTaxValue());
            }

            mInvoiceDetailsList.get(mInvoiceDetailsList.size() - 1).setSchemeProducts(downloadSchemeFreePrint(context, orderId));

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

        return mInvoiceDetailsList;
    }

    /*Preparing SchemeFreeProducts For printing purpose*/
    ArrayList<SchemeProductBO> downloadSchemeFreePrint(Context context, String orderId) {

        ArrayList<SchemeProductBO> schemeProductBOS = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        try {

            String invoiceId = "";
            Cursor invoice = db.selectSQL("Select invoiceno from invoicemaster where orderid=" + businessModel.QT(orderId));
            if (invoice.getCount() > 0 && invoice.moveToNext()) {
                invoiceId = invoice.getString(0);
                invoice.close();
            }

            SchemeProductBO schemeProductBO;

            Cursor c = db
                    .selectSQL("select schemeid,FreeProductID,FreeQty,UomID,pm.PName from schemeFreeProductDetail " +
                            " Inner Join ProductMaster pm ON pm.PID = FreeProductID "
                            + "where invoiceid="
                            + businessModel.QT(invoiceId)
                            + " order by schemeid");
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

        } catch (Exception e) {
            Commons.printException(e);
        }

        return schemeProductBOS;

    }

    /*To get invoice total value without applying tax and discount*/
    private double getOrderedTotalValue() {
        double line_total_price = 0;

        int siz = getOrderedProductMasterBOS().size();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = getOrderedProductMasterBOS().elementAt(i);

            if ((product.getOrderedPcsQty() > 0
                    || product.getOrderedCaseQty() > 0 || product
                    .getOrderedOuterQty() > 0)) {

                line_total_price += (product.getOrderedCaseQty() * product.getCsrp())
                        + (product.getOrderedPcsQty() * product.getSrp())
                        + (product.getOrderedOuterQty() * product.getOsrp());
            }
        }
        return line_total_price;
    }
}
