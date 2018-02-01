package com.ivy.cpg.view.order;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.SparseArray;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.Logs;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SerialNoBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 30/1/18.
 */

public class OrderHelper {

    private static OrderHelper instance = null;
    private Context mContext;
    private BusinessModel businessModel;
    public String selectedOrderId = "";
    private String orderid;
    public String invoiceDisount;

    private Vector<ProductMasterBO> mSortedOrderedProducts;
    private SparseArray<ArrayList<SerialNoBO>> mSerialNoListByProductid;


    private OrderHelper(Context context) {
        this.mContext = context;
        this.businessModel = (BusinessModel) context;
    }


    public static OrderHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderHelper(context);
        }
        return instance;
    }


    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public Vector<ProductMasterBO> getSortedOrderedProducts() {
        return mSortedOrderedProducts;
    }

    public void setSortedOrderedProducts(Vector<ProductMasterBO> mSortedList) {
        this.mSortedOrderedProducts = mSortedList;
    }

    public boolean hasOrder(LinkedList<ProductMasterBO> orderedList) {

        int siz = orderedList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = orderedList.get(i);
            if (product.getOrderedCaseQty() > 0
                    || product.getOrderedPcsQty() > 0
                    || product.getOrderedOuterQty() > 0)
                return true;
        }
        return false;
    }


    //Method to check wether stock is available to deliver
    public boolean isStockAvailableToDeliver(List<ProductMasterBO> orderList) {
        try {

            HashMap<String, Integer> mDeliverQtyByProductId = new HashMap<>();

            for (ProductMasterBO product : orderList) {


                if (product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0 || product
                        .getOrderedOuterQty() > 0) {

                    int totalQty = (product.getOrderedOuterQty() * product
                            .getOutersize())
                            + (product.getOrderedCaseQty() * product
                            .getCaseSize())
                            + (product.getOrderedPcsQty());
                    mDeliverQtyByProductId.put(product.getProductID(), totalQty);


                }
            }

            if (businessModel.configurationMasterHelper.IS_SCHEME_ON) {
                for (SchemeBO schemeBO : businessModel.schemeDetailsMasterHelper.getAppliedSchemeList()) {
                    if (schemeBO.getFreeProducts() != null) {
                        for (SchemeProductBO freeProductBO : schemeBO.getFreeProducts()) {
                            if (freeProductBO.getQuantitySelected() > 0) {

                                if (mDeliverQtyByProductId.get(freeProductBO.getProductId()) != null) {
                                    int qty = mDeliverQtyByProductId.get(freeProductBO.getProductId());
                                    mDeliverQtyByProductId.put(freeProductBO.getProductId(), (qty + freeProductBO.getQuantitySelected()));
                                } else {
                                    mDeliverQtyByProductId.put(freeProductBO.getProductId(), freeProductBO.getQuantitySelected());
                                }
                            }
                        }
                    }
                }


            }

            for (String productId : mDeliverQtyByProductId.keySet()) {
                ProductMasterBO product = businessModel.productHelper.getProductMasterBOById(productId);
                if (product != null) {
                    if (mDeliverQtyByProductId.get(productId) > product.getSIH())
                        return false;
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
            return false;
        }
        return true;
    }

    public boolean isAllScanned() {

        for (ProductMasterBO productBO : businessModel.productHelper.getProductMaster()) {
            int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                    + (productBO.getOrderedOuterQty() * productBO.getOutersize());
            if (totalQty > 0 && productBO.getScannedProduct() == 1) {
                if (totalQty != productBO.getTotalScannedQty()) {
                    return false;
                }
            }


        }
        return true;


    }


    // To check whether reason provided for un satisfied indicative order
    public boolean isReasonProvided(LinkedList<ProductMasterBO> orderedList) {
        int siz = orderedList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product =orderedList.get(i);
            if (businessModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) {
                if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                    if (product.getSoreasonId() == 0)
                        return false;
                }
            } else {
                if (product.getOrderedCaseQty() > 0)
                    if (product.getOrderedCaseQty() < product.getIndicativeOrder_oc())
                        if (product.getSoreasonId() == 0)
                            return false;
            }
        }
        return true;
    }


    /**
     * @AUTHOR Rajesh.K
     * <p>
     * Method used to add Off invoice scheme  free product in Last ordered  product (schemeproduct object).So that
     * we can show in Print
     */
    public void updateOffInvoiceSchemeInProductOBJ(LinkedList<ProductMasterBO> mOrderedProductList) {
        ProductMasterBO productBO = mOrderedProductList.get(mOrderedProductList.size() - 1);
        if (productBO != null) {
            ArrayList<SchemeBO> offInvoiceSchemeList = businessModel.schemeDetailsMasterHelper.getmOffInvoiceAppliedSchemeList();
            if (offInvoiceSchemeList != null) {
                for (SchemeBO schemeBO : offInvoiceSchemeList) {
                    if (schemeBO.isQuantityTypeSelected()) {
                        updateSchemeFreeproduct(schemeBO, productBO);
                    }
                }
            }
        }

    }

    /**
     * Method to add free product list into any one of scheme buy product
     *
     * @param schemeBO
     * @param productBO
     */
    public void updateSchemeFreeproduct(SchemeBO schemeBO,
                                        ProductMasterBO productBO) {
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (productBO.getSchemeProducts() == null) {
            productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
        }

        if (freeProductList != null) {
            for (SchemeProductBO freeProductBo : freeProductList) {
                if (freeProductBo.getQuantitySelected() > 0) {
                    ProductMasterBO product = businessModel.productHelper
                            .getProductMasterBOById(freeProductBo
                                    .getProductId());
                    if (product != null) {
                        productBO.getSchemeProducts().add(freeProductBo);
                    }
                }
            }
        }

    }

    public boolean isTaxAvailableForAllOrderedProduct(LinkedList<ProductMasterBO> mOrderedProductList) {
        for (ProductMasterBO bo : mOrderedProductList) {
            if (businessModel.productHelper.taxHelper.getmTaxListByProductId() == null) {
                return false;
            }
            if (businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) == null
                    || businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()).size() == 0) {
                return false;
            }
        }
        return true;
    }


    public boolean isTaxAppliedForAnyProduct(LinkedList<ProductMasterBO> mOrderedProductList) {

        int productsCount = mOrderedProductList.size();

        for (int i = 0; i < productsCount; i++) {
            ProductMasterBO productBO = mOrderedProductList.get(i);

            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {

                if (productBO.getTaxValue() > 0)
                    return true;
            }
        }

        return false;
    }



    //////////////////////////// Print ////////
    int print_count;
    public int getPrint_count() {
        return print_count;
    }
    public int getPrintCount(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select print_count from InvoiceMaster where invoiceNo='" + businessModel.invoiceNumber + "'");
            if (c != null) {
                if (c.moveToNext()) {

                    Commons.print("print_count," + c.getInt(0) + "");
                    print_count = c.getInt(0);
                    c.close();
                    db.closeDB();
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return print_count;
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method will save the Order into Database.
     */
    public void saveOrder(Context mContext) {
        try {
            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(mContext);
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            if (businessModel.configurationMasterHelper.IS_TEMP_ORDER_SAVE) {
                db.deleteSQL("TempOrderDetail", "RetailerID=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()),
                        false);
            }

            String timeStampid = "";
            int flag = 0; // flag for joint call
            int isVansales = 1;
            int indicativeFlag = 0;
            //
            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (!businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    isVansales = 0;
                    if (businessModel.configurationMasterHelper.IS_INDICATIVE_ORDER)
                        indicativeFlag = 1;

                }
            }

            String query = "select max(VisitID) from OutletTimestamp where retailerid="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    timeStampid = c.getString(0);

                    if (businessModel.outletTimeStampHelper.isJointCall(businessModel.userMasterHelper
                            .getUserMasterBO().getJoinCallUserList())) {
                        flag = 1;
                    }
                }
            }


            String id = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);
            String uid = businessModel.QT(id);

            if (!hasAlreadyOrdered(mContext,businessModel.getRetailerMasterBO().getRetailerID()) && businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO) {
                businessModel.insertSeqNumber("ORD");
                uid = businessModel.QT(businessModel.downloadSequenceNo("ORD"));
            }


            if ( hasAlreadyOrdered(mContext,businessModel.getRetailerMasterBO().getRetailerID())) {
                StringBuffer sb = new StringBuffer();
                sb.append("select OrderID from OrderHeader where RetailerID=");
                sb.append(businessModel.getRetailerMasterBO().getRetailerID());
                sb.append(" and upload='N'and invoicestatus = 0");
                if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//if existing order is updated
                    sb.append(" and OrderID=" + businessModel.QT(selectedOrderId));
                }
                // Add new for check vansales or presales at runtime
                if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                    sb.append(" and is_vansales=" + isVansales);
                }
                sb.append(" and orderid not in(select orderid from OrderDeliveryDetail)");

                Cursor orderDetailCursor = db.selectSQL(sb.toString());
                if (orderDetailCursor.getCount() > 0) {

                    if (orderDetailCursor.getCount() > 1) { // This is for IS
                        // having more that
                        // one odrer for
                        // same retailer
                        // case handled
                        orderDetailCursor.close();

                        sb = null;
                        sb = new StringBuffer();
                        sb.append("select OrderID from OrderHeader where RetailerID=");
                        sb.append(businessModel.getRetailerMasterBO().getRetailerID());
                        sb.append(" and upload='N' and invoicestatus = 0");
                        if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//if existing order is updated
                            sb.append(" and OrderID=" + businessModel.QT(selectedOrderId));
                        }
                        // Add new for check vansales or presales at runtime
                        if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                            sb.append(" and is_vansales=" + isVansales);
                        }
                        orderDetailCursor = db.selectSQL(sb.toString());

                    }
                    if (orderDetailCursor.getCount() > 0) {
                        orderDetailCursor.moveToNext();
                        uid = businessModel.QT(orderDetailCursor.getString(0));
                        /* No need for preseller
                        * SIH ll get updated while saving invoice */

                        db.deleteSQL("OrderHeader", "OrderID=" + uid, false);
                        db.deleteSQL("OrderDetail", "OrderID=" + uid, false);

                        // if scheme module enable ,delete tha scheme table
                        if (businessModel.configurationMasterHelper.IS_SCHEME_ON) {
                            db.deleteSQL(DataMembers.tbl_scheme_details,
                                    "OrderID=" + uid, false);
                            db.deleteSQL(DataMembers.tbl_SchemeFreeProductDetail,
                                    "OrderID=" + uid, false);
                        }
                        db.deleteSQL("OrderDiscountDetail", "OrderID=" + uid,
                                false);
                        db.deleteSQL("InvoiceDiscountDetail", "OrderID=" + uid,
                                false);
                        db.deleteSQL("InvoiceTaxDetails", "OrderID=" + uid,
                                false);
                        // If Product Return Module Enabled, then to delete the
                        // table having the OrderID
                        if (businessModel.configurationMasterHelper.SHOW_PRODUCTRETURN
                                && businessModel.configurationMasterHelper.IS_SIH_VALIDATION)
                            db.deleteSQL(DataMembers.tbl_orderReturnDetails,
                                    "OrderID=" + uid, false);
                    }
                }
                orderDetailCursor.close();
            }

            // set OrderId in bmodel, so that it can be used to show in
            // OrderSummary alert/
            this.setOrderid(uid);


            ProductMasterBO product;
            // For Malaysian User is_Process is 1 and IS is_process 0, it will
            // mot
            // affect the already working malaysian users
            int isProcess = 1;

            SupplierMasterBO supplierBO;
            if (businessModel.retailerMasterBO.getSupplierBO() != null) {
                supplierBO = businessModel.retailerMasterBO.getSupplierBO();
            } else {
                supplierBO = new SupplierMasterBO();

            }
            businessModel.invoiceNumber = uid.replaceAll("\'", "");
            businessModel.setInvoiceDate(new String(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), businessModel.configurationMasterHelper.outDateFormat)));
            // Order Header Entry
            String columns = "orderid,orderdate,retailerid,ordervalue,RouteId,linespercall,"
                    + "deliveryDate,isToday,retailerCode,retailerName,downloadDate,po,remark,freeProductsAmount,latitude,longitude,is_processed,timestampid,Jflag,ReturnValue,CrownCount,IndicativeOrderID,IFlag,sid,SParentID,stype,is_vansales,imagename,totalWeight,SalesType,orderTakenTime,FocusPackLines,MSPLines,MSPValues,FocusPackValues,imgName,PrintFilePath,RField1,RField2,ordertime,RemarksType,RField3";

            String printFilePath = "";
            if (businessModel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                printFilePath = StandardListMasterConstants.PRINT_FILE_PATH + SDUtil.now(SDUtil.DATE_GLOBAL).replace("/", "") + "/"
                        + businessModel.userMasterHelper.getUserMasterBO().getUserid() + "/" +
                        StandardListMasterConstants.PRINT_FILE_ORDER + businessModel.invoiceNumber + ".txt";
            }


            String values = uid
                    + ","
                    + businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + ","
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID())
                    + ","
                    + businessModel.QT(businessModel.formatValueBasedOnConfig(businessModel.getOrderHeaderBO().getOrderValue()))
                    + ","
                    + businessModel.getRetailerMasterBO().getBeatID()
                    + ","
                    + businessModel.getOrderHeaderBO().getLinesPerCall()

                    + ","
                    + businessModel.QT(DateUtil.convertToServerDateFormat(businessModel.getOrderHeaderBO().getDeliveryDate(), "yyyy/MM/dd"))
                    + ","
                    + (businessModel.getRetailerMasterBO().getIsToday())
                    + ","
                    + DatabaseUtils.sqlEscapeString(businessModel.getRetailerMasterBO()
                    .getRetailerCode())
                    + ","
                    + DatabaseUtils.sqlEscapeString(businessModel.getRetailerMasterBO()
                    .getRetailerName())
                    + ","
                    + businessModel.QT(businessModel.userMasterHelper.getUserMasterBO().getDownloadDate())
                    + ","
                    + businessModel.QT(businessModel.getOrderHeaderBO().getPO())
                    + ","
                    + businessModel.QT(businessModel.getOrderHeaderNote())
                    + ","
                    + businessModel.getOrderHeaderBO().getTotalFreeProductsAmount()
                    + ","
                    + businessModel.QT(businessModel.mSelectedRetailerLatitude + "")
                    + ","
                    + businessModel.QT(businessModel.mSelectedRetailerLongitude + "")
                    + ","
                    + isProcess
                    + ","
                    + businessModel.QT(timeStampid)
                    + ","
                    + flag
                    + ","
                    + businessModel.QT(businessModel.formatValueBasedOnConfig(businessModel.getOrderHeaderBO().getRemainigValue()))
                    + ","
                    + businessModel.getOrderHeaderBO().getCrownCount()
                    + ","
                    + businessModel.QT(businessModel.retailerMasterBO.getIndicativeOrderid() != null ? businessModel.retailerMasterBO
                    .getIndicativeOrderid() : "")
                    + ","
                    + indicativeFlag
                    + ","
                    + businessModel.getRetailerMasterBO().getDistributorId()
                    + ","
                    + businessModel.getRetailerMasterBO().getDistParentId()
                    + ","
                    + supplierBO.getSupplierType() + "," + isVansales
                    + "," + businessModel.QT(businessModel.getOrderHeaderBO().getSignaturePath())
                    + "," + businessModel.getOrderHeaderBO().getTotalWeight()
                    + "," + businessModel.QT(businessModel.retailerMasterBO.getOrderTypeId()) + "," + businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL) + " " + SDUtil.now(SDUtil.TIME))
                    + "," + businessModel.getOrderHeaderBO().getOrderedFocusBrands() + "," + businessModel.getOrderHeaderBO().getOrderedMustSellCount() + "," + businessModel.getOrderHeaderBO().getTotalMustSellValue()
                    + "," + (businessModel.getOrderHeaderBO().getTotalFocusProdValues())
                    + "," + businessModel.QT(businessModel.getOrderHeaderBO().getSignatureName()) // internal column imgName
                    + "," + businessModel.QT(printFilePath)
                    + "," + businessModel.QT(businessModel.getRField1())
                    + "," + businessModel.QT(businessModel.getRField2()) + "," + businessModel.QT(SDUtil.now(SDUtil.TIME))
                    + "," + businessModel.getRemarkType() + "," + businessModel.QT(businessModel.getRField3());


            db.insertSQL(DataMembers.tbl_orderHeader, columns, values);
            businessModel.getRetailerMasterBO().setIndicateFlag(0);


            if (businessModel.configurationMasterHelper.IS_HANGINGORDER) {
                updateHangingOrder(mContext,businessModel.getRetailerMasterBO());
            }
            businessModel.getRetailerMasterBO()
                    .setTotalLines(businessModel.getOrderHeaderBO().getLinesPerCall());

            //get entry level discount value
            double entryLevelDistSum = 0;

            // Order Details Entry
            Vector<ProductMasterBO> finalProductList = new Vector<>();
            columns = "orderid,productid,qty,rate,uomcount,pieceqty,caseqty,uomid,retailerid, msqqty, totalamount,ProductName,ProductshortName,pcode, D1,D2,D3,DA,outerQty,dOuomQty,dOuomid,soPiece,soCase,OrderType,CasePrice,OuterPrice,PcsUOMId,batchid,priceoffvalue,PriceOffId,weight,reasonId,HsnCode";
            if (businessModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                finalProductList = OrderHelper.getInstance(mContext).getSortedOrderedProducts();
            else
                finalProductList = businessModel.productHelper.getProductMaster();

            Vector<ProductMasterBO> mOrderedProductList = new Vector<>();
            for (int i = 0; i < finalProductList.size(); ++i) {
                product = finalProductList.elementAt(i);

                if (product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0
                        || product.getOrderedOuterQty() > 0) {

                    mOrderedProductList.add(product);
                    int pieceCount = (product.getOrderedCaseQty() * product
                            .getCaseSize())
                            + (product.getOrderedPcsQty() * product.getMSQty())
                            + (product.getOrderedOuterQty() * product
                            .getOutersize());
                    entryLevelDistSum = entryLevelDistSum + product.getApplyValue();

                    if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if (product.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper
                                    .getBatchlistByProductID().get(
                                            product.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {
                                    if (batchProductBO.getOrderedPcsQty() > 0
                                            || batchProductBO
                                            .getOrderedCaseQty() > 0
                                            || batchProductBO
                                            .getOrderedOuterQty() > 0) {
                                        values = getOrderDetails(product,
                                                batchProductBO, uid, true)
                                                .toString();
                                        db.insertSQL(
                                                DataMembers.tbl_orderDetails,
                                                columns, values);
                                    }
                                }
                            }
                        } else {
                            values = getOrderDetails(product, null, uid, false)
                                    .toString();
                            db.insertSQL(DataMembers.tbl_orderDetails, columns,
                                    values);
                        }

                    } else {
                        values = getOrderDetails(product, null, uid, false)
                                .toString();
                        db.insertSQL(DataMembers.tbl_orderDetails, columns,
                                values);
                    }


                    // Insert the Crown Product Details
                    if (businessModel.configurationMasterHelper.SHOW_CROWN_MANAGMENT
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                        if (product.getCrownOrderedPieceQty() > 0
                                || product.getCrownOrderedCaseQty() > 0
                                || product.getCrownOrderedOuterQty() > 0) {
                            Commons.print("Crown Product Insert Starts");
                            int crownpieceCount = (product
                                    .getCrownOrderedCaseQty() * product
                                    .getCaseSize())
                                    + (product.getCrownOrderedPieceQty() * product
                                    .getMSQty())
                                    + (product.getCrownOrderedOuterQty() * product
                                    .getOutersize());
                            values = uid
                                    + ","
                                    + businessModel.QT(product.getProductID())
                                    + ","
                                    + crownpieceCount
                                    + ","
                                    + product.getSrp()
                                    + ","
                                    + product.getCaseSize()
                                    + ","
                                    + product.getCrownOrderedPieceQty()
                                    + ","
                                    + product.getCrownOrderedCaseQty()
                                    + ","
                                    + product.getCaseUomId()
                                    + ","
                                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID())
                                    + ", "
                                    + product.getMSQty()
                                    + ","
                                    + 0 // No Price for Crown Products
                                    // + SDUtil.format(totalamount, 2, 0)
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductName())
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductShortName())
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductCode()) + ","
                                    + product.getD1() + "," + product.getD2()
                                    + "," + product.getD3() + ","
                                    + product.getDA() + ","
                                    + product.getCrownOrderedOuterQty() + ","
                                    + product.getOutersize() + ","
                                    + product.getOuUomid() + ","
                                    + product.getSoInventory() + ","

                                    + product.getSocInventory() + ","
                                    + businessModel.productHelper.getmOrderType().get(2)

                                    + "," + product.getCsrp() + ","
                                    + product.getOsrp() + ","
                                    + product.getPcUomid();

                            db.insertSQL(DataMembers.tbl_orderDetails, columns,
                                    values);
                            Commons.print("Crown Product Insert End");
                        }

                    } // End of Crown Product Insert

                    // Insert the Free Porduct Issue
                    if (businessModel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                        if (product.getFreePieceQty() > 0
                                || product.getFreeCaseQty() > 0
                                || product.getFreeOuterQty() > 0) {

                            Commons.print("Free Product Insert Starts");

                            int freePieceCount = (product.getFreeCaseQty() * product
                                    .getCaseSize())
                                    + (product.getFreePieceQty() * product
                                    .getMSQty())
                                    + (product.getFreeOuterQty() * product
                                    .getOutersize());
                            values = uid
                                    + ","
                                    + businessModel.QT(product.getProductID())
                                    + ","
                                    + freePieceCount
                                    + ","
                                    + product.getSrp()
                                    + ","
                                    + product.getCaseSize()
                                    + ","
                                    + product.getFreePieceQty()
                                    + ","
                                    + product.getFreeCaseQty()
                                    + ","
                                    + product.getCaseUomId()
                                    + ","
                                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID())
                                    + ", "
                                    + product.getMSQty()
                                    + ","
                                    + 0 // No Price for Crown Products
                                    // + SDUtil.format(totalamount, 2, 0)
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductName())
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductShortName())
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(product
                                    .getProductCode()) + ","
                                    + product.getD1() + "," + product.getD2()
                                    + "," + product.getD3() + ","
                                    + product.getDA() + ","
                                    + product.getFreeOuterQty() + ","
                                    + product.getOutersize() + ","
                                    + product.getOuUomid() + ","
                                    + product.getSoInventory() + ","
                                    + product.getSocInventory() + ","
                                    + businessModel.productHelper.getmOrderType().get(3)
                                    + "," + product.getCsrp() + ","
                                    + product.getOsrp() + ","
                                    + product.getPcUomid();

                            db.insertSQL(DataMembers.tbl_orderDetails, columns,
                                    values);
                            Commons.print("Free Product Insert Ends");
                        }

                    } // End of Free Product Insert

                } // End of For Loop

            }


            // insert itemlevel tax in SQLite
            if (businessModel.configurationMasterHelper.SHOW_TAX) {
                businessModel.productHelper.taxHelper.saveProductLeveltax(uid, db);

            }

            // start insert scheme details
            try {

                if (businessModel.configurationMasterHelper.IS_GST || businessModel.configurationMasterHelper.IS_GST_HSN) {
                    //update tax for scheme free product
                    //tax and price details are taken from ordered product which has highest tax rate.
                    // Also inserting in invoiceTaxDetail
                    businessModel.updateTaxForFreeProduct(mOrderedProductList, uid, db);
                }

                if (!businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                        || businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    businessModel.schemeDetailsMasterHelper.insertScemeDetails(uid, db, "N");
                }


                businessModel.schemeDetailsMasterHelper.insertAccumulationDetails(db, uid);


            } catch (Exception e1) {
                Commons.printException(e1);

            }

            // end insert scheme details

            // insert itemlevel discount in SQLite
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT) {
                businessModel.productHelper.saveItemLevelDiscount(this.getOrderid(), db);
            }

            DiscountHelper.getInstance(mContext).insertBillWisePaytermDisc(db, this.getOrderid());
            // insert billwise discount
            if (businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && businessModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
                DiscountHelper.getInstance(mContext).saveBillWiseDiscountRangewise(this.getOrderid(), db);
            } else if (businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && businessModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
                DiscountHelper.getInstance(mContext).insertBillWiseDisc(db, this.getOrderid());
            } else if (businessModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
                if (businessModel.getOrderHeaderBO().getDiscountValue() > 0) {
                    if (businessModel.configurationMasterHelper.discountType == 1 || businessModel.configurationMasterHelper.discountType == 2)
                        businessModel.productHelper.insertBillWiseEntryDisc(db, uid);
                }

            }


            try {
                if (businessModel.configurationMasterHelper.IS_SIH_VALIDATION
                        && businessModel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    businessModel.productHelper.saveReturnDetails(uid, 0, db);

                }
            } catch (Exception e1) {
                Commons.printException(e1);
            }

            if (businessModel.configurationMasterHelper.TAX_SHOW_INVOICE && !businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG && !businessModel.configurationMasterHelper.IS_INVOICE) {
                businessModel.productHelper.taxHelper.downloadBillWiseTaxDetails();
                businessModel.productHelper.taxHelper.applyBillWiseTax(businessModel.getOrderHeaderBO().getOrderValue());
                businessModel.productHelper.taxHelper.insertOrderTaxList(uid, db);
            }

            businessModel.productHelper.updateBillEntryDiscInOrderHeader(db, uid);
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT)
                businessModel.productHelper.updateEntryLevelDiscount(db, this.getOrderid(), entryLevelDistSum);

            db.closeDB();
            this.invoiceDisount = businessModel.getOrderHeaderBO().getDiscount() + "";

            try {
                if (!businessModel.configurationMasterHelper.IS_INVOICE)
                    businessModel.getRetailerMasterBO().setVisit_Actual(
                            (float) getAcheived(mContext,businessModel.retailerMasterBO
                                    .getRetailerID()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            businessModel.setOrderHeaderNote("");
            businessModel.getOrderHeaderBO().setPO("");
            businessModel.getOrderHeaderBO().setRemark("");
            businessModel.getOrderHeaderBO().setRField1("");
            businessModel.getOrderHeaderBO().setRField2("");

        } catch (Exception e) {
            Commons.printException(e);

        }

    }


    private StringBuffer getOrderDetails(ProductMasterBO productBo,
                                         ProductMasterBO batchProductBO, String orderId, boolean isBatchWise) {

        int pieceCount = 0;
        float srp = 0;
        double csrp = 0;
        double osrp = 0;
        double totalValue = 0;
        int orderPieceQty = 0;
        int orderCaseQty = 0;
        int orderOuterQty = 0;
        String batchid = "";
        double priceOffvalue = 0;
        int priceOffId = 0;
        int reasondId = 0;
        double line_total_price = 0;

        if (isBatchWise) {
            pieceCount = batchProductBO.getOrderedPcsQty()
                    + batchProductBO.getOrderedCaseQty()
                    * productBo.getCaseSize()
                    + batchProductBO.getOrderedOuterQty()
                    * productBo.getOutersize();
            srp = batchProductBO.getSrp();
            csrp = batchProductBO.getCsrp();
            osrp = batchProductBO.getOsrp();
            totalValue = batchProductBO.getDiscount_order_value();
            orderPieceQty = batchProductBO.getOrderedPcsQty();
            orderCaseQty = batchProductBO.getOrderedCaseQty();
            orderOuterQty = batchProductBO.getOrderedOuterQty();
            batchid = batchProductBO.getBatchid();
            priceOffvalue = batchProductBO.getPriceoffvalue() * pieceCount;
            priceOffId = batchProductBO.getPriceOffId();
            reasondId = batchProductBO.getSoreasonId();
            line_total_price = (batchProductBO.getOrderedCaseQty() * batchProductBO
                    .getCsrp())
                    + (batchProductBO.getOrderedPcsQty() * batchProductBO.getSrp())
                    + (batchProductBO.getOrderedOuterQty() * batchProductBO.getOsrp());
        } else {
            pieceCount = productBo.getOrderedPcsQty()
                    + productBo.getOrderedCaseQty() * productBo.getCaseSize()
                    + productBo.getOrderedOuterQty() * productBo.getOutersize();
            srp = productBo.getSrp();
            csrp = productBo.getCsrp();
            osrp = productBo.getOsrp();
            totalValue = productBo.getDiscount_order_value();
            orderPieceQty = productBo.getOrderedPcsQty();
            orderCaseQty = productBo.getOrderedCaseQty();
            orderOuterQty = productBo.getOrderedOuterQty();
            batchid = 0 + "";
            priceOffvalue = productBo.getPriceoffvalue() * pieceCount;

            priceOffId = productBo.getPriceOffId();
            reasondId = productBo.getSoreasonId();
            line_total_price = (productBo.getOrderedCaseQty() * productBo
                    .getCsrp())
                    + (productBo.getOrderedPcsQty() * productBo.getSrp())
                    + (productBo.getOrderedOuterQty() * productBo.getOsrp());
        }


        StringBuffer sb = new StringBuffer();
        sb.append(orderId + "," + productBo.getProductID() + ",");
        sb.append(pieceCount + "," + srp + "," + productBo.getCaseSize() + ","
                + orderPieceQty + "," + orderCaseQty + ",");
        sb.append(productBo.getCaseUomId() + ","
                + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) + ","
                + productBo.getMSQty() + ",");
        sb.append(businessModel.formatValueBasedOnConfig(line_total_price) + ","
                + DatabaseUtils.sqlEscapeString(productBo.getProductName()) + ","
                + DatabaseUtils.sqlEscapeString(productBo.getProductShortName()) + ",");
        sb.append(DatabaseUtils.sqlEscapeString(productBo.getProductCode())
                + ",");
        sb.append(productBo.getD1() + "," + productBo.getD2() + ","
                + productBo.getD3() + "," + productBo.getDA() + ",");
        sb.append(orderOuterQty + "," + productBo.getOutersize() + ","
                + productBo.getOuUomid() + ",");
        sb.append(productBo.getSoInventory() + ","
                + productBo.getSocInventory() + "," + 0 + ",");
        sb.append(csrp + "," + osrp + "," + productBo.getPcUomid() + ","
                + batchid);
        sb.append("," + priceOffvalue + "," + priceOffId);
        sb.append("," + productBo.getWeight());
        sb.append("," + reasondId);
        sb.append("," + businessModel.QT(productBo.getHsnCode()));

        return sb;

    }


    private double getAcheived(Context mContext,String retailerid) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        double f = 0;
        try {
            Cursor c = null;
            if (businessModel.configurationMasterHelper.IS_INVOICE)
                c = db.selectSQL("SELECT sum(invNetAmount) FROM InvoiceMaster where retailerid="
                        + retailerid);
                // c =
                // db.selectSQL("select  sum(i.invNetAmount) from InvoiceMaster i inner join retailermaster r on "
                // + " i.retailerid=r.retailerid   where r.istoday=1");

            else
                c = db.selectSQL("select sum (OrderValue) from OrderHeader where retailerid="
                        + retailerid);
            // c =
            // db.selectSQL("select  sum(o.OrderValue) from OrderHeader o inner join retailermaster r on "
            // + " o.retailerid=r.retailerid   where r.istoday=1");

            if (c != null) {
                if (c.moveToNext()) {
                    f = c.getDouble(0);

                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        db.closeDB();
        return f;
    }
    /**
     * Check weather order is placed for the particular retailer and its't sync
     * yet or not.
     *
     * @param retailerId
     * @return true|false
     */
    public boolean hasAlreadyOrdered(Context mContext,String retailerId) {
        boolean isEdit = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            // Order Header
            StringBuffer sb = new StringBuffer();


            sb.append("Select Distinct OH.OrderID from OrderHeader OH INNER JOIN OrderDetail OD on OH.OrderID = OD.OrderID ");
            sb.append(" where OH.upload='N' and OH.RetailerID =");
            sb.append(businessModel.QT(retailerId) + " and OH.invoiceStatus = 0 and sid=" + getRetailerMasterBO().getDistributorId());

            // Add new for check vansales or presales at runtime
            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sb.append(" and is_vansales="
                        + businessModel.retailerMasterBO.getIsVansales());
            }

            sb.append(" and OH.orderid not in(select orderid from OrderDeliveryDetail)");


            Cursor orderHeaderCursor = db.selectSQL(sb.toString());
            if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER && businessModel.getOrderIDList().size() > 0) {//existing list content must be cleared
                businessModel.getOrderIDList().clear();
            }
            if (orderHeaderCursor.getCount() > 0) {
                isEdit = true;
                if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//order id is saved to display in pop up
                    while (orderHeaderCursor.moveToNext()) {
                        businessModel.getOrderIDList().add(orderHeaderCursor.getString(0));
                    }
                }
            } else {
                isEdit = false;
            }
            orderHeaderCursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            isEdit = false;
        }
        return isEdit;
    }


    /**
     * Delete order Placed for the retailerId. Delete will only possible for
     * order which is not sync with server.
     *
     * @param retailerId
     */
    public void deleteOrder(Context context,String retailerId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String orderId = "";

            StringBuffer sb = new StringBuffer();
            sb.append("select orderId from orderHeader where RetailerId="
                    + businessModel.QT(retailerId));
            sb.append(" and upload='N' and invoiceStatus=0");
            if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//if existing order is deleted
                sb.append(" and OrderID=" + businessModel.QT(selectedOrderId));
            }
            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sb.append(" and is_vansales="
                        + businessModel.retailerMasterBO.getIsVansales()); // loaded data for
                // presales = 0
                // or vansales=1
            }

            Cursor orderDetailCursor = db.selectSQL(sb.toString());

            if (orderDetailCursor != null) {
                if (orderDetailCursor.moveToNext()) {
                    orderId = orderDetailCursor.getString(0);
                }
            }
            orderDetailCursor.close();
            setOrderid(orderId + "");
            /* No need for preseller
            * SIH ll get updated while saving invoice */

            db.deleteSQL(DataMembers.tbl_orderHeader, "OrderID=" + businessModel.QT(orderId)
                    + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_orderDetails, "OrderID=" + businessModel.QT(orderId)
                    + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_orderReturnDetails, "OrderID="
                    + businessModel.QT(orderId) + " and upload='N'", false);

            db.deleteSQL(DataMembers.tbl_scheme_details, "OrderID="
                    + businessModel.QT(orderId) + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_SchemeFreeProductDetail, "OrderID="
                    + businessModel.QT(orderId) + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_InvoiceDiscountDetail, "OrderID="
                    + businessModel.QT(orderId) + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_OrderDiscountDetail, "OrderID="
                    + businessModel.QT(orderId) + " and upload='N'", false);
            db.closeDB();
            businessModel.downloadIndicativeOrderedRetailer();
            businessModel.updateIndicativeOrderedRetailer(businessModel.getRetailerMasterBO());


            if (businessModel.configurationMasterHelper.IS_HANGINGORDER) {
                updateHangingOrder(context,businessModel.getRetailerMasterBO());
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    // Update hanging order for retailer
    public void updateHangingOrder(Context mContext,RetailerMasterBO retObj) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String retailerId = retObj.getRetailerID();
            List<String> OrderId = null;

            Cursor c = db
                    .selectSQL("SELECT OrderID FROM OrderHeader WHERE RetailerID = '"
                            + retailerId + "'");
            if (c != null) {
                OrderId = new ArrayList<String>();
                while (c.moveToNext()) {
                    OrderId.add(c.getString(0));
                }
                c.close();
            }

            if (OrderId == null || OrderId.size() == 0)
                retObj.setHangingOrder(false);
            else {
                for (String ordId : OrderId) {
                    Cursor c1 = db
                            .selectSQL("SELECT InvoiceNo FROM InvoiceMaster WHERE orderid = '"
                                    + ordId + "'");
                    if (c1 != null) {
                        if (c1.getCount() > 0) {
                            retObj.setHangingOrder(false);
                        } else
                            retObj.setHangingOrder(true);
                        c1.close();
                    } else {
                        retObj.setHangingOrder(true);
                        break;
                    }

                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }


    /**
     * Load the Order Details and Order Header data into product master to Edit
     * Order.
     */
    public void loadOrderedProducts(Context mContext,String retailerId, String orderId) {
        businessModel.productHelper.clearOrderTableForInitiative();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            OrderHeader ordHeadBO = new OrderHeader();
            businessModel.setOrderHeaderBO(ordHeadBO);
            String orderID = new String();
            StringBuffer sb = new StringBuffer();


            if (orderId != null) {

                sb.append("select OD.OrderID,ifnull(po,''),ifnull(remark,''),OrderValue,LinesPerCall,");
                if (businessModel.configurationMasterHelper.discountType == 1) {
                    sb.append("ID.percentage,");
                } else if (businessModel.configurationMasterHelper.discountType == 2) {
                    sb.append("ID.value,");
                } else {
                    sb.append("0,");
                }
                sb.append("deliveryDate,remark,freeProductsCount,ReturnValue,CrownCount,IFNULL(imagename,'') AS imagename,salesType,imgName,RField1,RField2 from "
                        + DataMembers.tbl_orderHeader + " OD");

                sb.append(" left join InvoiceDiscountDetail ID on ID.OrderId=OD.orderid and ID.typeid=0 and ID.pid=0 ");
                sb.append(" where OD.upload='N' and OD.OrderID=" + businessModel.QT(orderId)
                        + " and invoiceStatus=0");


            } else { // This is for IS having more that one odrer for same
                // retailer case handled


                sb.append("select OD.OrderID,ifnull(po,''),ifnull(remark,''),OrderValue,LinesPerCall,");
                if (businessModel.configurationMasterHelper.discountType == 1) {
                    sb.append("ID.percentage,");
                } else if (businessModel.configurationMasterHelper.discountType == 2) {
                    sb.append("ID.value,");
                } else {
                    sb.append("0,");
                }
                sb.append("deliveryDate,remark,freeProductsCount,ReturnValue,CrownCount,IFNULL(imagename,'') AS imagename,salesType,imgName,RField1,RField2 from "
                        + DataMembers.tbl_orderHeader + " OD ");

                sb.append(" left join InvoiceDiscountDetail ID on OD.OrderId=OD.orderid and ID.typeid=0 and ID.pid=0 ");
                sb.append(" where OD.upload='N' and OD.RetailerID="
                        + businessModel.QT(retailerId) + " and invoiceStatus=0");

            }

            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sb.append(" and is_vansales="
                        + businessModel.retailerMasterBO.getIsVansales()); // loaded data for
                // presales = 0
                // or vansales=1
            }

            sb.append(" and sid=" + businessModel.retailerMasterBO.getDistributorId());

            sb.append(" and OD.orderid not in(select orderid from OrderDeliveryDetail)");

            // Order Header
            /*
             * String sql =
			 * "select OrderID,ifnull(po,''),ifnull(remark,''),OrderValue,LinesPerCall,discount,"
			 * +
			 * "deliveryDate,remark,freeProductsCount,ReturnValue,CrownCount from "
			 * + DataMembers.tbl_orderHeader +
			 * " where upload='N' and RetailerID=" + QT(retailerId) +
			 * " and invoiceStatus=0";
			 */
            Cursor orderHeaderCursor = db.selectSQL(sb.toString());
            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext()) {
                    orderID = orderHeaderCursor.getString(0);

                    setOrderid(orderID);//used for delivery order

                    businessModel.getOrderHeaderBO().setPO(orderHeaderCursor.getString(1));
                    businessModel.getOrderHeaderBO()
                            .setRemark(orderHeaderCursor.getString(2));

                    // getOrderHeaderBO().setDistribution(
                    // orderHeaderCursor.getString(3));
                    businessModel.getOrderHeaderBO().setOrderValue(
                            orderHeaderCursor.getDouble(3));
                    businessModel.getOrderHeaderBO().setLinesPerCall(
                            orderHeaderCursor.getInt(4));

                    businessModel.getOrderHeaderBO().setDiscount(
                            orderHeaderCursor.getDouble(5));

                    businessModel.getOrderHeaderBO().setDeliveryDate(
                            orderHeaderCursor.getString(6));
                    businessModel.getOrderHeaderBO().setTotalFreeProductsCount(
                            orderHeaderCursor.getInt(8));
                    businessModel.getOrderHeaderBO().setRemainigValue(
                            orderHeaderCursor.getDouble(9));
                    businessModel.getOrderHeaderBO().setCrownCount(
                            orderHeaderCursor.getInt(10));
                    businessModel.getOrderHeaderBO().setSignaturePath(orderHeaderCursor.getString(11));
                    businessModel.getOrderHeaderBO().setSignatureName(orderHeaderCursor.getString(13));
                    if (businessModel.getOrderHeaderBO().getSignatureName() != null && !businessModel.getOrderHeaderBO().getSignatureName().equals("") && !getOrderHeaderBO().getSignatureName().equals("null")) {
                        businessModel.getOrderHeaderBO().setIsSignCaptured(true);
                    } else {
                        businessModel.getOrderHeaderBO().setIsSignCaptured(false);
                    }
                    businessModel.setOrderHeaderNote(orderHeaderCursor.getString(7));
                    //setOrderIDFormInvoice(orderID);
                    this.invoiceDisount = orderHeaderCursor.getDouble(5) + "";
                    businessModel.retailerMasterBO.setOrderTypeId(orderHeaderCursor.getString(12));

                    businessModel.setRField1(orderHeaderCursor.getString(14));
                    businessModel.setRField2(orderHeaderCursor.getString(15));

                    businessModel.getOrderHeaderBO()
                            .setRField1(orderHeaderCursor.getString(14));
                    businessModel.getOrderHeaderBO()
                            .setRField2(orderHeaderCursor.getString(15));

                }
            } else {
                businessModel.setOrderHeaderNote("");
                businessModel.setRField1("");
                businessModel.setRField2("");
            }
            orderHeaderCursor.close();

            String sql1 = "select productId,caseqty,pieceqty,  Rate, D1, D2, D3,"
                    + "uomcount,DA,totalamount,outerQty,dOuomQty,batchid,weight from "
                    + DataMembers.tbl_orderDetails
                    + " where orderId="
                    + businessModel.QT(orderID) + " order by rowid";


            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                String productId = "";
                while (orderDetailCursor.moveToNext()) {

                    int caseqty = orderDetailCursor.getInt(1);
                    int pieceqty = orderDetailCursor.getInt(2);
                    int caseSize = orderDetailCursor.getInt(7);
                    int outerQty = orderDetailCursor.getInt(10);
                    int outerSize = orderDetailCursor.getInt(11);
                    String batchid = orderDetailCursor.getString(12);
                    float weight = orderDetailCursor.getFloat(13);
                    float srp = orderDetailCursor.getFloat(3);

                    productId = orderDetailCursor.getString(0);

                    if (businessModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {

                        if (businessModel.productHelper.getmProductidOrderByEntry() == null) {
                            LinkedList<String> list = new LinkedList<>();
                            list.add(orderDetailCursor.getString(0));
                            businessModel.productHelper.setmProductidOrderByEntry(list);
                        } else
                            businessModel.productHelper.getmProductidOrderByEntry().add(orderDetailCursor.getString(0));

                        int qty = pieceqty + (caseqty * caseSize) + (outerQty * outerSize);
                        businessModel.productHelper.getmProductidOrderByEntryMap().put(Integer.parseInt(productId), qty);

                    }

                    if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        ProductMasterBO productBo = businessModel.getProductbyId(productId);
                        if (productBo != null) {
                            if (productBo.getBatchwiseProductCount() > 0) {
                                businessModel.batchAllocationHelper.setBatchwiseProducts(
                                        productId, caseqty, pieceqty, outerQty, srp,
                                        orderDetailCursor.getDouble(4),
                                        orderDetailCursor, caseSize, outerSize,
                                        batchid);
                            } else {
                                setProductDetails(productId, caseqty, pieceqty,
                                        outerQty, srp,
                                        orderDetailCursor.getDouble(4),
                                        orderDetailCursor, caseSize, outerSize, weight);
                            }
                        }

                    } else {
                        setProductDetails(productId, caseqty, pieceqty,
                                outerQty, srp, orderDetailCursor.getDouble(4),
                                orderDetailCursor, caseSize, outerSize, weight);
                    }
                    // }


                }
            }
            orderDetailCursor.close();

            if (businessModel.configurationMasterHelper.SHOW_PRODUCTRETURN
                    && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                String str = "SELECT Pid,LiableQty,ReturnQty,TypeID FROM "
                        + DataMembers.tbl_orderReturnDetails
                        + " WHERE OrderID =" + businessModel.QT(orderID);

                orderDetailCursor = db.selectSQL(str);
                if (orderDetailCursor != null) {
                    while (orderDetailCursor.moveToNext()) {
                        String pid = orderDetailCursor.getString(0);
                        int liableQty = orderDetailCursor.getInt(1);
                        int returnQty = orderDetailCursor.getInt(2);
                        String typeId = orderDetailCursor.getString(3);
                        if (businessModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                            for (BomRetunBo bomReturnBo : businessModel.productHelper
                                    .getBomReturnTypeProducts()) {
                                if (bomReturnBo.getPid().equals(typeId)) {
                                    bomReturnBo.setLiableQty(liableQty);
                                    bomReturnBo.setReturnQty(returnQty);
                                    bomReturnBo.setTypeId(typeId);
                                    break;
                                }
                            }
                        } else {
                            for (BomRetunBo bomReturnBo :businessModel. productHelper
                                    .getBomReturnProducts()) {
                                if (bomReturnBo.getPid().equals(pid)) {
                                    bomReturnBo.setLiableQty(liableQty);
                                    bomReturnBo.setReturnQty(returnQty);
                                    break;
                                }
                            }
                        }

                    }
                }
                orderDetailCursor.close();
            }
            // If Crow Management is Enabled then load the Values
            if (businessModel.configurationMasterHelper.SHOW_CROWN_MANAGMENT
                    && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                String sql2 = "select productId,caseqty,pieceqty,outerQty from "
                        + DataMembers.tbl_orderDetails
                        + " where orderId="
                        + businessModel.QT(orderID)
                        + " AND OrderType = "
                        + businessModel.productHelper.getmOrderType().get(2);

                Cursor c = db.selectSQL(sql2);
                if (c != null) {
                    while (c.moveToNext()) {
                        for (ProductMasterBO temp : businessModel.productHelper
                                .getProductMaster())
                            if (temp.getProductID().equals(c.getString(0))) {
                                temp.setCrownOrderedCaseQty(c.getInt(1));
                                temp.setCrownOrderedPieceQty(c.getInt(2));
                                temp.setCrownOrderedOuterQty(c.getInt(3));
                                break;
                            }

                    }
                }
                c.close();
            }

            // If Free is Enabled then load the Values
            if (businessModel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN) {

                String sql2 = "select productId,caseqty,pieceqty,outerQty from "
                        + DataMembers.tbl_orderDetails
                        + " where orderId="
                        + businessModel.QT(orderID)
                        + " AND OrderType = "
                        + businessModel.productHelper.getmOrderType().get(3);

                Cursor c = db.selectSQL(sql2);
                if (c != null) {
                    while (c.moveToNext()) {
                        for (ProductMasterBO temp : businessModel.productHelper
                                .getProductMaster())
                            if (temp.getProductID().equals(c.getString(0))) {
                                temp.setFreeCaseQty(c.getInt(1));
                                temp.setFreePieceQty(c.getInt(2));
                                temp.setFreeOuterQty(c.getInt(3));
                                break;
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

    /**
     * Update product Quantity for particular product Id.
     *
     * @param productid
     * @param --qty
     */
    private void setProductDetails(String productid, int caseqty, int pieceqty,
                                   int outerQty, float srp, double pricePerPiece, Cursor OrderDetails,
                                   int caseSize, int outerSize, float weight) {
        ProductMasterBO product;
        int siz = businessModel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        if (productid == null)
            return;

        Logs.debug("INV", "Product ID : " + productid);

        for (int i = 0; i < siz; ++i) {
            product = businessModel.productHelper.getProductMaster().get(i);

            if (product.getProductID().equals(productid)) {
                product.setSchemeProducts(null);
                product.setOrderedPcsQty(pieceqty);
                product.setOrderedCaseQty(caseqty);
                product.setOrderedOuterQty(outerQty);
                product.setOrderPricePiece(pricePerPiece);
                product.setSrp(srp);

                if (product.getSchemeBO() != null) {
                    product.setSchemeBO(new SchemeBO());
                    product.getSchemeBO().setSelectedPrice(pricePerPiece);
                    product.getSchemeBO().setPriceTypeSeleted(true);
                } else {
                    product.setSchemeBO(new SchemeBO());
                    product.getSchemeBO().setSelectedPrice(pricePerPiece);
                    product.getSchemeBO().setPriceTypeSeleted(true);
                }
                product.setCheked(true);

                if (OrderDetails != null) {

                    product.setD1(OrderDetails.getDouble(OrderDetails
                            .getColumnIndex("d1")));
                    product.setD2(OrderDetails.getDouble(OrderDetails
                            .getColumnIndex("d2")));
                    product.setD3(OrderDetails.getDouble(OrderDetails
                            .getColumnIndex("d3")));
                    product.setDA(OrderDetails.getDouble(OrderDetails
                            .getColumnIndex("DA")));
                    product.setTotalamount(OrderDetails.getDouble(OrderDetails
                            .getColumnIndex("totalamount")));
                    product.setDiscount_order_value(OrderDetails
                            .getDouble(OrderDetails
                                    .getColumnIndex("totalamount")));
                    product.setWeight(weight);

                }
                businessModel.productHelper.getProductMaster().setElementAt(product, i);

                return;
            }
        }
        return;
    }


    public void loadInvoiceProducts(String invoiceNumber) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String sql2 = "select discount,discount_type from InvoiceMaster where invoiceNo="
                + businessModel.QT(invoiceNumber) + "";
        Cursor invoiceDetailCursor = db.selectSQL(sql2);

        if (invoiceDetailCursor != null) {
            if (invoiceDetailCursor.moveToNext()) {
                this.invoiceDisount = invoiceDetailCursor.getString(0);
                businessModel.configurationMasterHelper.discountType = invoiceDetailCursor
                        .getInt(1);
            }
            invoiceDetailCursor.close();
        }

        String sql1 = "select productId,sum(pcsQty),sum(CaseQty), ordered_price,d1,d2,d3,DA,sum(totalamount) as totalamount,sum(outerQty),weight,Rate from "
                + DataMembers.tbl_InvoiceDetails
                + " where invoiceid="
                + businessModel.QT(invoiceNumber) + " group by productid";
        invoiceDetailCursor = db.selectSQL(sql1);
        if (invoiceDetailCursor != null) {
            String productId = "";
            while (invoiceDetailCursor.moveToNext()) {
                int pieceqty = invoiceDetailCursor.getInt(1);
                int caseqty = invoiceDetailCursor.getInt(2);
                int outerQty = invoiceDetailCursor.getInt(9);
                float weight = invoiceDetailCursor.getFloat(10);
                float srp = invoiceDetailCursor.getFloat(11);

                productId = invoiceDetailCursor.getString(0);
                setProductDetails(productId, caseqty, pieceqty, outerQty, srp,
                        invoiceDetailCursor.getDouble(3), invoiceDetailCursor,
                        0, 0, weight);


            }
        }
        invoiceDetailCursor.close();

        db.closeDB();
    }

    /**
     * This method will save the Invoice into InvoiceMaster table as well as the
     * Invoice details into InvoiceDEtails Table.
     * <p>
     * Saving Invoice will also update the SIH in ProductMaster.
     */
    public void saveNewInvoice(Context mContext) {

        SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(mContext);
        salesReturnHelper.getSalesReturnGoods(mContext);
        ArrayList<ProductMasterBO> batchList;

        int isCredtNoteCreated = SalesReturnHelper.getInstance(mContext).isCreditNoteCreated(mContext);

        double ordervalue = 0.0;
        if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                && isCredtNoteCreated != 1) {
            if (salesReturnHelper.isValueReturned(mContext)) {
                ordervalue = businessModel.getOrderHeaderBO().getOrderValue()
                        - salesReturnHelper.getSaleableValue();
            } else {
                ordervalue = businessModel.getOrderHeaderBO().getOrderValue();
            }
        } else {
            ordervalue = businessModel.getOrderHeaderBO().getOrderValue();
        }
        /*
         * update tax in invoice master Changed by Felix on 30-04-2015 For
		 * getting tax detail from order value
		 */
        if (businessModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
            businessModel.productHelper.taxHelper.downloadBillWiseTaxDetails();


            ordervalue = Double.parseDouble(SDUtil.format(ordervalue,
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));

            final double totalTaxValue = businessModel.productHelper.taxHelper.applyBillWiseTax(ordervalue);

            if (businessModel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX)
                ordervalue = ordervalue + totalTaxValue;

        }
        /*
         * if (configurationMasterHelper.IS_APPLY_PRODUCT_TAX) ordervalue =
		 * ordervalue + productHelper.caculateTotalProductwiseTax();
		 */

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            double discountPercentage = businessModel.collectionHelper.getSlabwiseDiscountpercentage();

            String invid = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);
            // Normally Generating Invoice ID
            invid = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // If this Configuration on, Invoice ID generation differently
            // according to rule
            if (businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO) {
                String seqNo = "";
                businessModel.insertSeqNumber("INV");
                seqNo = businessModel.downloadSequenceNo("INV");
                invid = seqNo.toString();
            }

            String timeStampid = "";
            String query = "select max(VisitID) from OutletTimestamp where retailerid="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    timeStampid = c.getString(0);
                }
            }

            this.invoiceNumber = invid;

            String printFilePath = "";
            if (businessModel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                printFilePath = StandardListMasterConstants.PRINT_FILE_PATH + SDUtil.now(SDUtil.DATE_GLOBAL).replace("/", "") + "/"
                        + businessModel.userMasterHelper.getUserMasterBO().getUserid() + "/" +
                        StandardListMasterConstants.PRINT_FILE_INVOICE + invoiceNumber + ".txt";

            businessModel.setInvoiceDate(new String(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), businessModel.configurationMasterHelper.outDateFormat)));
            String invoiceHeaderColumns = "invoiceno,invoicedate,retailerId,invNetamount,paidamount,orderid,ImageName,upload,beatid,discount,invoiceAmount,discountedAmount,latitude,longitude,return_amt,discount_type,salesreturned,LinesPerCall,IsPreviousInvoice,totalWeight,SalesType,sid,SParentID,stype,imgName,creditPeriod,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3";
            StringBuffer sb = new StringBuffer();
            sb.append(businessModel.QT(invid) + ",");
            sb.append(businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",");
            sb.append(businessModel.QT(businessModel.retailerMasterBO.getRetailerID()) + ",");

            sb.append(businessModel.formatValueBasedOnConfig(ordervalue) + ",");

            sb.append(0 + ",");
            sb.append(this.getOrderid() + ",");
            sb.append(businessModel.QT(businessModel.getOrderHeaderBO().getSignaturePath())
                    + ",");
            sb.append(businessModel.QT("N") + ",");
            sb.append(businessModel.getRetailerMasterBO().getBeatID() + ",");
            sb.append(businessModel.getOrderHeaderBO().getDiscount() + ",");
            sb.append(businessModel.formatValueBasedOnConfig(ordervalue) + ",");
            double discountedAmount = 0;
            if (businessModel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                if (discountPercentage > 0) {

                    double remaingAmount = (ordervalue * discountPercentage) / 100;

                    discountedAmount = ordervalue
                            - remaingAmount;
                    sb.append(businessModel.formatValueBasedOnConfig(discountedAmount) + ",");
                } else {
                    sb.append(businessModel.formatValueBasedOnConfig(ordervalue) + ",");
                }
            } else {
                sb.append(businessModel.formatValueBasedOnConfig(ordervalue) + ",");
            }
            sb.append(businessModel.QT(businessModel.mSelectedRetailerLatitude + "") + ",");
            sb.append(businessModel.QT(businessModel.mSelectedRetailerLongitude + "") + ",");
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCredtNoteCreated != 1) {
                Commons.print("if" + salesReturnHelper.getSaleableValue());
                if (salesReturnHelper.isValueReturned(getApplicationContext()))
                    sb.append(salesReturnHelper.getSaleableValue() + ",");
                else
                    sb.append(0 + ",");
            } else {
                Commons.print("else");
                sb.append(+businessModel.getOrderHeaderBO().getRemainigValue() + ",");

            }
            sb.append(businessModel.configurationMasterHelper.discountType + ",");
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCredtNoteCreated != 1
                    && salesReturnHelper.isValueReturned(mContext))
                sb.append(1);
            else
                sb.append(0);
            sb.append("," + businessModel.getOrderHeaderBO().getLinesPerCall() + "," + 0);
            sb.append("," + businessModel.getOrderHeaderBO().getTotalWeight());
            sb.append("," + businessModel.QT(businessModel.retailerMasterBO.getOrderTypeId()));


            /********** Added Sih , stype in InvoiceMaster ********/
            SupplierMasterBO supplierBO;
            if (businessModel.retailerMasterBO.getSupplierBO() != null) {
                supplierBO = businessModel.retailerMasterBO.getSupplierBO();
            } else {
                supplierBO = new SupplierMasterBO();

            }
            sb.append("," + businessModel.getRetailerMasterBO().getDistributorId());
            sb.append("," + businessModel.getRetailerMasterBO().getDistParentId());
            sb.append("," + supplierBO.getSupplierType());
            sb.append("," + businessModel.QT(businessModel.getOrderHeaderBO().getSignatureName()));
            sb.append("," + businessModel.getRetailerMasterBO().getCreditDays());
            sb.append("," + businessModel.QT(printFilePath));
            sb.append("," + businessModel.QT(timeStampid));
            sb.append("," + businessModel.getRemarkType());
            sb.append("," + businessModel.QT(businessModel.getRField1()));
            sb.append("," + businessModel.QT(businessModel.getRField2()));
            sb.append("," + businessModel.QT(businessModel.getRField3()));

            db.insertSQL(DataMembers.tbl_InvoiceMaster, invoiceHeaderColumns,
                    sb.toString());
            /*********************************************/


            if (businessModel.configurationMasterHelper.IS_HANGINGORDER) {
                updateHangingOrder(businessModel.getRetailerMasterBO());
            }
            /* update free products sih starts */

            if (!businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                    || businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                businessModel.schemeDetailsMasterHelper.updateFreeProductsSIH(
                        this.getOrderid(), invid, db);
            }
 /* insert tax details in Sqlite */
            if (businessModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                businessModel.productHelper.taxHelper.insertInvoiceTaxList(invid, db);

            }

            /* update free products sih ends */
            // update Invoiceid in InvoiceDiscountDetail table
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT || businessModel.configurationMasterHelper.discountType == 1
                    || businessModel.configurationMasterHelper.discountType == 2 || businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

                businessModel.productHelper.updateInvoiceIdInItemLevelDiscount(db, invid,
                        this.getOrderid());
            }

            // update Invoiceid in InvoiceTaxDetail table
            if (businessModel.configurationMasterHelper.SHOW_TAX) {
                businessModel.productHelper.taxHelper.updateInvoiceIdInProductLevelTax(db, invid,
                        this.getOrderid());
            }

            /** update invoicecreateed in SalesReturnHeader **/
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCredtNoteCreated != 1) {
                db.executeQ("update SalesReturnHeader set invoicecreated=1 where RetailerID="
                        + businessModel..getRetailerMasterBO().getRetailerID());
            }
            /** update credit not flag in sales return header **/
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCredtNoteCreated != 1
                    && salesReturnHelper.isValueReturned(mContext)) {
                db.executeQ("update SalesReturnHeader set credit_flag=2 where RetailerID="
                        + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
            }
            // update credit balance
            if (businessModel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE
                    && businessModel.getRetailerMasterBO().getCredit_balance() != -1) {
                double creditbalance = businessModel.getRetailerMasterBO()
                        .getCredit_balance() - businessModel.getOrderHeaderBO().getOrderValue();
                db.executeQ("update retailermaster set rfield1="
                        + creditbalance + " where retailerid="
                        + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                businessModel.getRetailerMasterBO().setCredit_balance(creditbalance);
                businessModel.getRetailerMasterBO().setRField1("" + (double) creditbalance);

            }
            //if (salesReturnHelper.SHOW_STOCK_REPLACE_PCS || salesReturnHelper.SHOW_STOCK_REPLACE_CASE || salesReturnHelper.SHOW_STOCK_REPLACE_OUTER) {
            businessModel.productHelper.updateInvoiceIdInSalesReturn(db, invid);
              /*  salesReturnHelper.clearSalesReturnTable();
                productHelper.updateSalesReturnInfoInProductObj(db, invid, false);*/
            // }

			/*
             * db.executeQ(
			 * "insert into  invoiceMaster  (invoiceno,invoicedate,retailerId,invNetamount,paidamount,orderid,upload,beatid,discount,invoiceAmount)  values ("
			 * + QT(invid) + "," + QT(SDUtil.now(SDUtil.DATE)) + "," +
			 * QT(retailerMasterBO.getRetailerID()) + "," +
			 * orderHeaderBO.getOrderValue() + "," + "0" + "," +
			 * this.getOrderid() + "," + QT("N") + "," +
			 * getRetailerMasterBO().getBeatID() + "," + this.invoiceDisount +
			 * "," + orderHeaderBO.getOrderValue() + ")");
			 */

            ProductMasterBO product;

            String columns = "invoiceId,productid,qty,rate,uomdesc,retailerid,uomid,msqqty,uomCount,caseQty,pcsQty,d1,d2,d3,DA,totalamount,outerQty,dOuomQty,dOuomid,batchid,upload,CasePrice,OuterPrice,PcsUOMId,OrderType,priceoffvalue,PriceOffId,weight,hasserial,schemeAmount,DiscountAmount,taxAmount,HsnCode";
            int siz = businessModel.productHelper.getProductMaster().size();
            for (int i = 0; i < siz; ++i) {
                product = businessModel.productHelper.getProductMaster()
                        .elementAt(i);

                if ((product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0 || product
                        .getOrderedOuterQty() > 0)) {

                    String values = "";
                    int totalqty = (product.getOrderedPcsQty())
                            + (product.getCaseSize() * product
                            .getOrderedCaseQty())
                            + (product.getOrderedOuterQty() * product
                            .getOutersize());
                    /**
                     * If scheme config is On and scheme mapped for this
                     * product, then we have to store free products as well
                     **/

                    if (product.getBatchwiseProductCount() == 0 || !businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {

                        values = getInvoiceDetailsRecords(product, null, invid,
                                db, false).toString();
                        db.insertSQL(DataMembers.tbl_InvoiceDetails, columns,
                                values);

                    } else {

                        batchList = businessModel.batchAllocationHelper
                                .getBatchlistByProductID().get(
                                        product.getProductID());

                        if (batchList != null) {
                            int count = 0;
                            for (ProductMasterBO batchwiseProductBO : batchList) {
                                if (batchwiseProductBO.getOrderedPcsQty() > 0
                                        || batchwiseProductBO
                                        .getOrderedCaseQty() > 0
                                        || batchwiseProductBO
                                        .getOrderedOuterQty() > 0) {
                                    values = getInvoiceDetailsRecords(product,
                                            batchwiseProductBO, invid, db, true)
                                            .toString();

                                    db.insertSQL(
                                            DataMembers.tbl_InvoiceDetails,
                                            columns, values);
                                }
                                count = count + 1;
                            }
                        }

                    }

                    if (product.isAllocation() == 1) {
                        int s = product.getSIH() > totalqty ? product.getSIH()
                                - totalqty : 0;
                        businessModel.productHelper.getProductMaster().get(i).setSIH(s);

                        // Update the SIH, this is mandatory if we have
                        // stock
                        // report
                        // and stock based validation
                        db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                + totalqty
                                + " then ifnull(sih,0)-"
                                + totalqty
                                + " else 0 end) where pid="
                                + product.getProductID());
                    }
                }
            }

            if (businessModel.configurationMasterHelper.IS_SIH_VALIDATION
                    && businessModel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                businessModel.productHelper.saveReturnDetails(businessModel.QT(invid), 1, db);
            }

            /**
             * Insert Product Details to Empty Reconciliation tables if Type
             * wise Group products disabled
             */
            if (!businessModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                businessModel.mEmptyReconciliationhelper.saveSKUWiseTransaction();

            // Update the OrderHeader that , Invoice is created for this Order
            // and the Order is
            // not allowed to edit again.
            String sql = "update " + DataMembers.tbl_orderHeader
                    + " set invoiceStatus=1  where RetailerID="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID())
                    + " and orderid=" + this.getOrderid();
            db.executeQ(sql);
            if (businessModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN)
                businessModel.productHelper.saveSerialNo(db);

            businessModel.productHelper.updateSchemeAndDiscAndTaxValue(db, invid);


            db.closeDB();

            try {
                businessModel.getRetailerMasterBO().setVisit_Actual(
                        (float) getAcheived(mContext,businessModel.retailerMasterBO.getRetailerID()));
            } catch (Exception e) {

                Commons.printException(e);
            }

        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public void loadSerialNo() {
        DBUtil db = null;
        try {
            mSerialNoListByProductid = new SparseArray<ArrayList<SerialNoBO>>();
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select productid,fromNo,toNo,scannedQty from temp_serialno ");
            sb.append("where retailerid =" + bmodel.getRetailerMasterBO().getRetailerID());
            sb.append(" order by productid");
            Cursor c = db.selectSQL(sb.toString());
            int produtid = 0;
            if (c.getCount() > 0) {
                ArrayList<SerialNoBO> serialNoList = new ArrayList<SerialNoBO>();
                SerialNoBO serialNoBO;
                while (c.moveToNext()) {
                    serialNoBO = new SerialNoBO();
                    serialNoBO.setFromNo(c.getString(1));
                    serialNoBO.setToNo(c.getString(2));
                    serialNoBO.setScannedQty(c.getInt(3));
                    if (produtid != c.getInt(0)) {
                        if (produtid != 0) {
                            mSerialNoListByProductid.put(produtid, serialNoList);
                            serialNoList = new ArrayList<SerialNoBO>();
                            serialNoList.add(serialNoBO);
                            produtid = c.getInt(0);
                        } else {
                            serialNoList = new ArrayList<SerialNoBO>();
                            serialNoList.add(serialNoBO);
                            produtid = c.getInt(0);
                        }
                    } else {
                        serialNoList.add(serialNoBO);
                    }


                }
                if (serialNoList.size() > 0) {
                    mSerialNoListByProductid.put(produtid, serialNoList);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }

    public void saveSerialNo(DBUtil db) {
        String columns = "orderid,invoiceid,pid,serialNumber,uomid,Retailerid";
        StringBuffer sb;
        if (mSerialNoListByProductid != null) {
            for (ProductMasterBO productBO : productMaster) {
                if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {

                    ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductid.get(Integer.parseInt(productBO.getProductID()));
                    if (serialNoList != null) {
                        for (SerialNoBO serialNoBo : serialNoList) {
                            if (serialNoBo.getScannedQty() > 0) {
                                for (int i = 0; i < serialNoBo.getScannedQty(); i++) {
                                    try {
                                        BigInteger serialNo = new BigInteger(serialNoBo.getFromNo());
                                        BigInteger one = new BigInteger(i + "");
                                        BigInteger sumValue = serialNo.add(one);
                                        sb = new StringBuffer();
                                        sb.append(bmodel.getOrderid() + "," + bmodel.QT(bmodel.getInvoiceNumber()) + ",");
                                        sb.append(productBO.getProductID() + "," + bmodel.QT(sumValue + "") + "," + productBO.getPcUomid());
                                        sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                        db.insertSQL("InvoiceSerialNumbers", columns, sb.toString());
                                    } catch (NumberFormatException e) {
                                        sb = new StringBuffer();
                                        sb.append(bmodel.getOrderid() + "," + bmodel.QT(bmodel.getInvoiceNumber()) + ",");
                                        sb.append(productBO.getProductID() + "," + bmodel.QT(serialNoBo.getFromNo() + "") + "," + productBO.getPcUomid());
                                        sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                        db.insertSQL("InvoiceSerialNumbers", columns, sb.toString());
                                    }

                                }
                            }

                        }
                    }
                }
            }
        }

        db.deleteSQL("temp_serialno", "retailerid=" + bmodel.getRetailerMasterBO().getRetailerID(), false);
        mSerialNoListByProductid = null;


    }

    public SparseArray<ArrayList<SerialNoBO>> getSerialNoListByProductid() {
        return mSerialNoListByProductid;
    }

    public void setmSerialNoListByProductid(SparseArray<ArrayList<SerialNoBO>> serialNoListByProductid) {
        this.mSerialNoListByProductid = serialNoListByProductid;
    }

    /**
     * Method to find duplicate serialnumber entered
     *
     * @return
     */
    public boolean isDuplicateSerialNo() {
        ArrayList<Integer> serialNo;
        if (mSerialNoListByProductid != null) {


            for (ProductMasterBO productBO : productMaster) {
                if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {
                    if (productBO.getScannedProduct() == 1) {
                        serialNo = new ArrayList<Integer>();

                        ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductid.get(Integer.parseInt(productBO.getProductID()));
                        if (serialNoList != null) {
                            for (SerialNoBO serialNoBO : serialNoList) {

                                for (int i = 0; i < serialNoBO.getScannedQty(); i++) {
                                    try {

                                        int number = Integer.parseInt(serialNoBO.getFromNo()) + i;

                                        if (!serialNo.contains(number)) {
                                            serialNo.add(number);
                                        } else {
                                            return true;
                                        }
                                    } catch (NumberFormatException e) {
                                        Commons.print(e.getMessage());
                                    }
                                }


                            }


                        }
                    }

                }
            }
        }


        return false;
    }

    public void saveSerialNoTemp() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL("temp_serialno", "retailerid=" + bmodel.getRetailerMasterBO().getRetailerID(), false);


            String columns = "productid,fromNo,toNo,Retailerid,scannedQty";
            StringBuffer sb;
            if (mSerialNoListByProductid != null) {
                for (int i = 0; i < mSerialNoListByProductid.size(); i++) {
                    int key = mSerialNoListByProductid.keyAt(i);
                    ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductid.valueAt(i);
                    if (serialNoList != null) {
                        for (SerialNoBO serialNoBO : serialNoList) {

                            sb = new StringBuffer();
                            sb.append(key + "," + bmodel.QT(serialNoBO.getFromNo()));
                            sb.append("," + bmodel.QT(serialNoBO.getToNo()));
                            sb.append("," + bmodel.getRetailerMasterBO().getRetailerID());
                            sb.append("," + serialNoBO.getScannedQty());
                            db.insertSQL("temp_serialno", columns, sb.toString());
                        }
                    }


                }
            }

            db.closeDB();


        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }



    /**
         * this method return StringBuffer value to insert records in invoice
         * details table for particular product
         *
         * @param productBO   - this productBO value inserted into SQLite
         * @param batchwiseBO - if batchwise count >1, All records store batchwise in
         *                    invoice details table
         * @param invoiceid   - this id generated runtime while saving invoiceCREATE TABLE "CS_ClosingStockDetails" ("Uid" nvarchar(35), "Date" DATETIME, "Pid" INTEGER, "UomID" INTEGER, "UomQty" INTEGER, "LocId" INTEGER DEFAULT 0, "IsOwn" INTEGER DEFAULT 1, "upload" nvarchar(2) DEFAULT 'N')myth
         * @param --isScheme  - if isScheme is true, given product is a schemeproduct, else
         *                    no scheme
         * @param db          -Dbutil used to insert,select or update records in sqlite
         * @return
         */

        private StringBuffer getInvoiceDetailsRecords(ProductMasterBO productBO,
                ProductMasterBO batchwiseBO, String invoiceid, DBUtil db,
        boolean isBatchwise) {
            ProductMasterBO product = productBO;
            ProductMasterBO batchwiseProductBO;
            StringBuffer sb = new StringBuffer();
            int schemeOrderType = 0;

            int orderedPcsQty = 0;
            int orderdeCaseQty = 0;
            int orderedOuterQty = 0;

            String batchid = 0 + "";
            double totalValue = 0.0;
            double priceOffValue = 0;
            int priceOffId = 0;

            int totalqty = 0;
            float srp = 0;
            double csrp = 0;
            double osrp = 0;
            double prodDisc = 0;
            double schemeDisc = 0;
            double taxAmount = 0;
            double line_total_price = 0;

            try {
                if (isBatchwise) {
                    batchwiseProductBO = batchwiseBO;
                    orderedPcsQty = batchwiseProductBO.getOrderedPcsQty();
                    orderdeCaseQty = batchwiseProductBO.getOrderedCaseQty();
                    orderedOuterQty = batchwiseProductBO.getOrderedOuterQty();
                    batchid = batchwiseProductBO.getBatchid();
                    schemeOrderType = businessModel.productHelper.getmOrderType().get(1);
                    srp = batchwiseProductBO.getSrp();
                    csrp = batchwiseProductBO.getCsrp();
                    osrp = batchwiseProductBO.getOsrp();

                    totalqty = orderedPcsQty
                            + (orderdeCaseQty * product.getCaseSize())
                            + (orderedOuterQty * product.getOutersize());
                    totalValue = batchwiseProductBO.getDiscount_order_value();

                    if (batchwiseProductBO.getSIH() >= totalqty) {
                        batchwiseProductBO.setSIH(batchwiseProductBO.getSIH()
                                - totalqty);
                    } else {
                        batchwiseProductBO.setSIH(0);
                    }

                    priceOffValue = batchwiseProductBO.getPriceoffvalue() * totalqty;
                    priceOffId = batchwiseProductBO.getPriceOffId();
                    schemeDisc = batchwiseProductBO.getSchemeDiscAmount();
                    prodDisc = batchwiseProductBO.getProductDiscAmount();
                    taxAmount = batchwiseProductBO.getTaxApplyvalue();
                    line_total_price = (batchwiseProductBO.getOrderedCaseQty() * batchwiseProductBO
                            .getCsrp())
                            + (batchwiseProductBO.getOrderedPcsQty() * batchwiseProductBO.getSrp())
                            + (batchwiseProductBO.getOrderedOuterQty() * batchwiseProductBO.getOsrp());


                } else {
                    orderedPcsQty = product.getOrderedPcsQty();
                    orderdeCaseQty = product.getOrderedCaseQty();
                    orderedOuterQty = product.getOrderedOuterQty();
                    srp = product.getSrp();
                    csrp = product.getCsrp();
                    osrp = product.getOsrp();
                    batchid = 0 + "";
                    schemeOrderType = businessModel.productHelper.getmOrderType().get(1);

                    totalqty = orderedPcsQty
                            + (orderdeCaseQty * product.getCaseSize())
                            + (orderedOuterQty * product.getOutersize());
                    totalValue = productBO.getDiscount_order_value();

                    priceOffValue = productBO.getPriceoffvalue() * totalqty;
                    priceOffId = productBO.getPriceOffId();
                    schemeDisc = productBO.getSchemeDiscAmount();
                    prodDisc = productBO.getProductDiscAmount();
                    taxAmount = productBO.getTaxApplyvalue();
                    line_total_price = (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO.getSrp())
                            + (productBO.getOrderedOuterQty() * productBO.getOsrp());

                }

                db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                        + totalqty
                        + " then ifnull(qty,0)-"
                        + totalqty
                        + " else 0 end) where pid="
                        + product.getProductID()
                        + " and batchid=" + businessModel.QT(batchid));

                sb.append(businessModel.QT(invoiceid) + ",");
                sb.append(businessModel.QT(product.getProductID()) + ",");
                sb.append(totalqty + "," + srp + ",");
                sb.append(businessModel.QT(product.getOU()) + ",");
                sb.append(businessModel.QT(businessModel.retailerMasterBO.getRetailerID()));
                sb.append("," + product.getCaseUomId() + ",");
                sb.append(product.getMSQty() + ",");
                sb.append(product.getCaseSize() + ",");
                sb.append(orderdeCaseQty + ",");
                sb.append(orderedPcsQty + ",");
                sb.append(product.getD1() + "," + product.getD2());
                sb.append("," + product.getD3() + ",");
                sb.append(product.getDA() + ",");
                // + productHelper.caculateTotalProductwiseTaxById(product)
                sb.append(line_total_price);
                sb.append("," + orderedOuterQty + ",");
                sb.append(product.getOutersize() + ",");
                sb.append(product.getOuUomid() + "," + batchid);
                sb.append("," + businessModel.QT("N"));
                sb.append("," + csrp + "," + osrp + ","
                        + product.getPcUomid() + ",");
                sb.append(schemeOrderType);
                sb.append("," + priceOffValue + "," + priceOffId);
                sb.append("," + product.getWeight());
                sb.append("," + product.getScannedProduct());
                sb.append("," + schemeDisc + "," + prodDisc);
                sb.append("," + taxAmount);
                sb.append("," + businessModel.QT(product.getHsnCode()));

                return sb;
            } catch (Exception e) {
                Commons.printException("" + e);
                return new StringBuffer();
            }

        }




        //////////////////////////////////////////////Delivery Order //////////////////////
        public void insertDeliveryOrderRecord(Context mContext,boolean isPartial) {

            try {

                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();

                String columns = "orderid,productid,qty,uomid,uomcount,price,taxprice,linevalue,upload";

                int siz = businessModel.productHelper.getProductMaster().size();
                ProductMasterBO product;
                String values;

                for (int i = 0; i < siz; ++i) {
                    product = businessModel.productHelper.getProductMaster()
                            .elementAt(i);
                    if (isPartial) {

                        if (product.getDeliveredCaseQty() > 0
                                || product.getDeliveredPcsQty() > 0
                                || product.getDeliveredOuterQty() > 0) {

                            if (product.getDeliveredCaseQty() > 0) {
                                values = businessModel.QT(getOrderid()) + "," + product.getProductID();
                                values += "," + product.getDeliveredCaseQty() + "," + product.getCaseUomId() + "," + product.getCaseSize()
                                        + "," + product.getCsrp() + "," + product.getCsrp() + "," + (product.getCsrp() * product.getDeliveredCaseQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (product.getDeliveredPcsQty() > 0) {
                                values = businessModel.QT(getOrderid()) + "," + product.getProductID();
                                values += "," + product.getDeliveredPcsQty() + "," + product.getPcUomid() + ",1"
                                        + "," + product.getSrp() + "," + product.getSrp() + "," + (product.getSrp() * product.getDeliveredPcsQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (product.getDeliveredOuterQty() > 0) {
                                values = businessModel.QT(getOrderid()) + "," + product.getProductID();
                                values += "," + product.getDeliveredOuterQty() + "," + product.getOuUomid() + "," + product.getOutersize()
                                        + "," + product.getOsrp() + "," + product.getOsrp() + "," + (product.getOsrp() * product.getDeliveredOuterQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }


                            //update SIH
                            int totalqty = (product.getDeliveredPcsQty())
                                    + (product.getCaseSize() * product
                                    .getDeliveredCaseQty())
                                    + (product.getDeliveredOuterQty() * product
                                    .getOutersize());

                            int s = product.getSIH() > totalqty ? product.getSIH()
                                    - totalqty : 0;

                            db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                    + totalqty
                                    + " then ifnull(sih,0)-"
                                    + totalqty
                                    + " else 0 end) where pid="
                                    + product.getProductID());

                            //
                            db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                                    + totalqty
                                    + " then ifnull(qty,0)-"
                                    + totalqty
                                    + " else 0 end) where pid="
                                    + product.getProductID());


                            //updating object
                            product.setSIH(s);


                        }
                    } else {

                        if (product.getOrderedCaseQty() > 0
                                || product.getOrderedPcsQty() > 0
                                || product.getOrderedOuterQty() > 0) {

                            if (product.getOrderedCaseQty() > 0) {
                                values = getOrderid() + "," + product.getProductID();
                                values += "," + product.getOrderedCaseQty() + "," + product.getCaseUomId() + "," + product.getCaseSize()
                                        + "," + product.getCsrp() + "," + product.getCsrp() + "," + (product.getCsrp() * product.getOrderedCaseQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (product.getOrderedPcsQty() > 0) {
                                values = getOrderid() + "," + product.getProductID();
                                values += "," + product.getOrderedPcsQty() + "," + product.getPcUomid() + ",1"
                                        + "," + product.getSrp() + "," + product.getSrp() + "," + (product.getSrp() * product.getOrderedPcsQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (product.getOrderedOuterQty() > 0) {
                                values = getOrderid() + "," + product.getProductID();
                                values += "," + product.getOrderedOuterQty() + "," + product.getOuUomid() + "," + product.getOutersize()
                                        + "," + product.getOsrp() + "," + product.getOsrp() + "," + (product.getOsrp() * product.getOrderedOuterQty()) + "," + QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }

                            //SIH will be update while saving invoice..

                        }

                    }
                }


                if (!isPartial) {
                    // inserting free products
                    for (SchemeBO schemeBO : businessModel.schemeDetailsMasterHelper.getAppliedSchemeList()) {

                        if (schemeBO.isQuantityTypeSelected()) {

                            for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {

                                ProductMasterBO productMasterBO = businessModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());


                                if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                                    values = getOrderid() + "," + schemeProductBO.getProductId();
                                    values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getPcUomid() + ",1"
                                            + "," + productMasterBO.getSrp() + "," + productMasterBO.getSrp() + "," + (productMasterBO.getSrp() * schemeProductBO.getQuantitySelected()) + "," + QT("N");
                                    db.insertSQL("OrderDeliveryDetail",
                                            columns, values);
                                }
                                if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                    values = getOrderid() + "," + schemeProductBO.getProductId();
                                    values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getCaseUomId() + "," + productMasterBO.getCaseSize()
                                            + "," + productMasterBO.getCsrp() + "," + productMasterBO.getCsrp() + "," + (productMasterBO.getCsrp() * schemeProductBO.getQuantitySelected()) + "," + QT("N");
                                    db.insertSQL("OrderDeliveryDetail",
                                            columns, values);
                                }
                                if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                    values = getOrderid() + "," + schemeProductBO.getProductId();
                                    values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getOuUomid() + "," + productMasterBO.getOutersize()
                                            + "," + productMasterBO.getOsrp() + "," + productMasterBO.getOsrp() + "," + (productMasterBO.getOsrp() * schemeProductBO.getQuantitySelected()) + "," + QT("N");
                                    db.insertSQL("OrderDeliveryDetail",
                                            columns, values);
                                }


                                //Update the SIH

                                int totalqty = 0;
                                if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                                    totalqty = schemeProductBO.getQuantitySelected();
                                } else if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                    totalqty = (schemeProductBO.getQuantitySelected() * productMasterBO.getCaseSize());
                                } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                    totalqty = (schemeProductBO.getQuantitySelected() * productMasterBO.getOutersize());
                                }

                                int s = productMasterBO.getSIH() > totalqty ? productMasterBO.getSIH()
                                        - totalqty : 0;

                                db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                        + totalqty
                                        + " then ifnull(sih,0)-"
                                        + totalqty
                                        + " else 0 end) where pid="
                                        + productMasterBO.getProductID());

                                //
                                db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                                        + totalqty
                                        + " then ifnull(qty,0)-"
                                        + totalqty
                                        + " else 0 end) where pid="
                                        + productMasterBO.getProductID());


                                //updating object
                                productMasterBO.setSIH(s);

                            }
                        }
                    }
                }


                db.closeDB();
            } catch (Exception ex) {

                Commons.printException(ex);
            }
        }

}
