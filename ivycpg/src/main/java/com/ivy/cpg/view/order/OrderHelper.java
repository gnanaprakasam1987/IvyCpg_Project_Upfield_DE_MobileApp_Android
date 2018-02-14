package com.ivy.cpg.view.order;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import android.util.SparseArray;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SerialNoBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 30/1/18.
 * Order screen related helper methods
 */

public class OrderHelper {

    private static OrderHelper instance = null;
    private BusinessModel businessModel;
    public String selectedOrderId = "";
    private String orderId;
    public String invoiceDiscount;
    private int print_count;

    private Vector<ProductMasterBO> mSortedOrderedProducts;
    private SparseArray<ArrayList<SerialNoBO>> mSerialNoListByProductId;


    private OrderHelper(Context context) {

        this.businessModel = (BusinessModel) context;
    }


    public static OrderHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderHelper(context);
        }
        return instance;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setSortedOrderedProducts(Vector<ProductMasterBO> mSortedList) {
        this.mSortedOrderedProducts = mSortedList;
    }

    public SparseArray<ArrayList<SerialNoBO>> getSerialNoListByProductId() {
        return mSerialNoListByProductId;
    }

    public void setSerialNoListByProductId(SparseArray<ArrayList<SerialNoBO>> serialNoListByProductId) {
        this.mSerialNoListByProductId = serialNoListByProductId;
    }

    public int getPrint_count() {
        return print_count;
    }


    /**
     * To check is order available
     *
     * @param orderedList Product list
     * @return Availability
     */
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

    /**
     * Save order
     *
     * @param mContext current context
     */
    public void saveOrder(Context mContext) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            if (businessModel.configurationMasterHelper.IS_TEMP_ORDER_SAVE) {
                db.deleteSQL("TempOrderDetail", "RetailerID=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()),
                        false);
            }

            String timeStampId = "";
            int flag = 0; // flag for joint call
            int isVanSales = 1;
            int indicativeFlag = 0;

            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (!businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    isVanSales = 0;
                    if (businessModel.configurationMasterHelper.IS_INDICATIVE_ORDER)
                        indicativeFlag = 1;

                }
            }

            String query = "select max(VisitID) from OutletTimestamp where retailerid="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    timeStampId = c.getString(0);

                    if (businessModel.outletTimeStampHelper.isJointCall(businessModel.userMasterHelper
                            .getUserMasterBO().getJoinCallUserList())) {
                        flag = 1;
                    }
                }
            }


            String id = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);
            String uid = businessModel.QT(id);

            if (!hasAlreadyOrdered(mContext, businessModel.getRetailerMasterBO().getRetailerID()) && businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO) {
                businessModel.insertSeqNumber("ORD");
                uid = businessModel.QT(businessModel.downloadSequenceNo("ORD"));
            }


            // Deleting existing order
            if (hasAlreadyOrdered(mContext, businessModel.getRetailerMasterBO().getRetailerID())) {

                StringBuffer sb = new StringBuffer();
                sb.append("select OrderID from OrderHeader where RetailerID=");
                sb.append(businessModel.getRetailerMasterBO().getRetailerID());
                sb.append(" and upload='N'and invoicestatus = 0");
                if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//if existing order is updated
                    sb.append(" and OrderID=" + businessModel.QT(selectedOrderId));
                }
                if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                    sb.append(" and is_vansales=" + isVanSales);
                }
                sb.append(" and orderid not in(select orderid from OrderDeliveryDetail)");

                Cursor orderDetailCursor = db.selectSQL(sb.toString());
                if (orderDetailCursor.getCount() > 0) {

                    if (orderDetailCursor.getCount() > 1) {
                        orderDetailCursor.close();

                        sb = new StringBuffer();
                        sb.append("select OrderID from OrderHeader where RetailerID=");
                        sb.append(businessModel.getRetailerMasterBO().getRetailerID());
                        sb.append(" and upload='N' and invoicestatus = 0");
                        if (businessModel.configurationMasterHelper.IS_MULTI_STOCKORDER) {//if existing order is updated
                            sb.append(" and OrderID=" + businessModel.QT(selectedOrderId));
                        }
                        if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                            sb.append(" and is_vansales=" + isVanSales);
                        }
                        orderDetailCursor = db.selectSQL(sb.toString());

                    }
                    if (orderDetailCursor.getCount() > 0) {
                        orderDetailCursor.moveToNext();
                        uid = businessModel.QT(orderDetailCursor.getString(0));

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

                        // If Product Return Module Enabled, then deleting corresponding return transactions
                        if (businessModel.configurationMasterHelper.SHOW_PRODUCTRETURN
                                && businessModel.configurationMasterHelper.IS_SIH_VALIDATION)
                            db.deleteSQL(DataMembers.tbl_orderReturnDetails,
                                    "OrderID=" + uid, false);
                    }
                }
                orderDetailCursor.close();
            }

            // It can be used to show in OrderSummary alert
            setOrderId(uid);


            ProductMasterBO product;
            // For Malaysian User is_Process is 1 and IS is_process 0, it will not affect the already working malaysian users
            int isProcess = 1;

            SupplierMasterBO supplierBO;
            if (businessModel.retailerMasterBO.getSupplierBO() != null) {
                supplierBO = businessModel.retailerMasterBO.getSupplierBO();
            } else {
                supplierBO = new SupplierMasterBO();

            }
            businessModel.invoiceNumber = uid.replaceAll("\'", "");
            businessModel.setInvoiceDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat));

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
                    + businessModel.QT(timeStampId)
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
                    + supplierBO.getSupplierType() + "," + isVanSales
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
                updateHangingOrder(mContext, businessModel.getRetailerMasterBO());
            }
            businessModel.getRetailerMasterBO()
                    .setTotalLines(businessModel.getOrderHeaderBO().getLinesPerCall());


            // Save order details
            Vector<ProductMasterBO> finalProductList;
            columns = "orderid,productid,qty,rate,uomcount,pieceqty,caseqty,uomid,retailerid, msqqty, totalamount,ProductName,ProductshortName,pcode, D1,D2,D3,DA,outerQty,dOuomQty,dOuomid,soPiece,soCase,OrderType,CasePrice,OuterPrice,PcsUOMId,batchid,priceoffvalue,PriceOffId,weight,reasonId,HsnCode";
            if (businessModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                finalProductList = mSortedOrderedProducts;
            else
                finalProductList = businessModel.productHelper.getProductMaster();

            //get entry level discount value
            double entryLevelDistSum = 0;
            Vector<ProductMasterBO> mOrderedProductList = new Vector<>();
            for (int i = 0; i < finalProductList.size(); ++i) {
                product = finalProductList.elementAt(i);

                if (product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0
                        || product.getOrderedOuterQty() > 0) {

                    mOrderedProductList.add(product);
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
                            int crownPieceCount = (product
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
                                    + crownPieceCount
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
                                    + 0
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

                    }

                    // Insert the Free product Issue
                    if (businessModel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                        if (product.getFreePieceQty() > 0
                                || product.getFreeCaseQty() > 0
                                || product.getFreeOuterQty() > 0) {


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
                                    + 0
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
                        }

                    }

                }

            }


            // insert item level tax in SQLite
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


            // insert item level discount in SQLite
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT) {
                businessModel.productHelper.saveItemLevelDiscount(this.getOrderId(), db);
            }

            DiscountHelper.getInstance(mContext).insertBillWisePayTermDisc(db, this.getOrderId());

            // insert bill wise discount
            if (businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && businessModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
                DiscountHelper.getInstance(mContext).saveBillWiseDiscountRangeWise(this.getOrderId(), db);
            } else if (businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && businessModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
                DiscountHelper.getInstance(mContext).insertBillWiseDiscount(db, this.getOrderId());
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

            // update discount in order header table
            businessModel.productHelper.updateBillEntryDiscInOrderHeader(db, uid);
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT)
                businessModel.productHelper.updateEntryLevelDiscount(db, this.getOrderId(), entryLevelDistSum);

            db.closeDB();

            this.invoiceDiscount = businessModel.getOrderHeaderBO().getDiscount() + "";

            try {
                if (!businessModel.configurationMasterHelper.IS_INVOICE)
                    businessModel.getRetailerMasterBO().setVisit_Actual(
                            (float) getRetailerOrderValue(mContext, businessModel.retailerMasterBO
                                    .getRetailerID()));
            } catch (Exception e) {
                Commons.printException(e);
            }

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(mContext);

            if (businessModel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION
                    && businessModel.retailerMasterBO.getRpTypeCode().equals(salesReturnHelper.CREDIT_TYPE))
                updateCreditNoteprintList();

            salesReturnHelper.saveSalesReturn(mContext, this.getOrderId(), "ORDER");
            salesReturnHelper.clearSalesReturnTable();

            businessModel.setOrderHeaderNote("");
            businessModel.getOrderHeaderBO().setPO("");
            businessModel.getOrderHeaderBO().setRemark("");
            businessModel.getOrderHeaderBO().setRField1("");
            businessModel.getOrderHeaderBO().setRField2("");

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void updateCreditNoteprintList() {

        int totalBalanceQty = 0;
        float totalBalanceAmount = 0;

        for (ProductMasterBO product : businessModel.productHelper.getSalesReturnProducts()) {
            List<SalesReturnReasonBO> reasonList = product.getSalesReturnReasonList();

            int totalSalesReturnQty = 0;
            float totalSalesReturnAmt = 0;
            float replacementPrice = 0;
            if (reasonList != null) {

                for (SalesReturnReasonBO reasonBO : reasonList) {
                    if (reasonBO.getPieceQty() > 0 || reasonBO.getCaseQty() > 0 || reasonBO.getOuterQty() > 0) {
                        //Calculate sales return total qty and price.
                        int totalQty = reasonBO.getPieceQty() + (reasonBO.getCaseQty() * product.getCaseSize()) + (reasonBO.getOuterQty() * product.getOutersize());
                        totalSalesReturnQty = totalSalesReturnQty + totalQty;
                        totalSalesReturnAmt = totalSalesReturnAmt + (totalQty * reasonBO.getSrpedit());
                        // Higher SRP edit price will be considered for replacement product price.
                        if (replacementPrice < reasonBO.getSrpedit())
                            replacementPrice = reasonBO.getSrpedit();
                    }
                }
            }

            // Calculate replacement qty price.
            int totalReplaceQty = product.getRepPieceQty() + (product.getRepCaseQty() * product.getCaseSize()) + (product.getRepOuterQty() * product.getOutersize());
            float totalReplacementPrice = totalReplaceQty * replacementPrice;

            totalBalanceQty = totalBalanceQty + (totalSalesReturnQty - totalReplaceQty);
            totalBalanceAmount = totalBalanceAmount + (totalSalesReturnAmt - totalReplacementPrice);

            // set the total qty and value in ProductBO to enable print.
        }

        if (totalBalanceQty > 0) {
            Log.d("TotalBalanceQty",totalBalanceQty+"");
            Log.d("TotalBalanceAmount",totalBalanceAmount+"");
        }
    }


    /**
     * prepare order detail value to insert
     *
     * @param productBo      current product object
     * @param batchProductBO batch product object
     * @param orderId        order id
     * @param isBatchWise    is Batch Wise
     * @return return order detail table values
     */
    private StringBuffer getOrderDetails(ProductMasterBO productBo,
                                         ProductMasterBO batchProductBO, String orderId, boolean isBatchWise) {

        int pieceCount;
        float srp;
        double csrp;
        double osrp;
        int orderPieceQty;
        int orderCaseQty;
        int orderOuterQty;
        String batchid;
        double priceOffValue;
        int priceOffId;
        int reasonId;
        double line_total_price;

        if (isBatchWise) {
            pieceCount = batchProductBO.getOrderedPcsQty()
                    + batchProductBO.getOrderedCaseQty()
                    * productBo.getCaseSize()
                    + batchProductBO.getOrderedOuterQty()
                    * productBo.getOutersize();
            srp = batchProductBO.getSrp();
            csrp = batchProductBO.getCsrp();
            osrp = batchProductBO.getOsrp();
            orderPieceQty = batchProductBO.getOrderedPcsQty();
            orderCaseQty = batchProductBO.getOrderedCaseQty();
            orderOuterQty = batchProductBO.getOrderedOuterQty();
            batchid = batchProductBO.getBatchid();
            priceOffValue = batchProductBO.getPriceoffvalue() * pieceCount;
            priceOffId = batchProductBO.getPriceOffId();
            reasonId = batchProductBO.getSoreasonId();
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
            orderPieceQty = productBo.getOrderedPcsQty();
            orderCaseQty = productBo.getOrderedCaseQty();
            orderOuterQty = productBo.getOrderedOuterQty();
            batchid = 0 + "";
            priceOffValue = productBo.getPriceoffvalue() * pieceCount;

            priceOffId = productBo.getPriceOffId();
            reasonId = productBo.getSoreasonId();
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
        sb.append("," + priceOffValue + "," + priceOffId);
        sb.append("," + productBo.getWeight());
        sb.append("," + reasonId);
        sb.append("," + businessModel.QT(productBo.getHsnCode()));

        return sb;

    }

    /**
     * Check weather order is placed for the particular retailer and its't sync
     * yet or not.
     *
     * @param retailerId Retailer Id
     * @return order availability
     */
    public boolean hasAlreadyOrdered(Context mContext, String retailerId) {
        boolean isEdit;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append("Select Distinct OH.OrderID from OrderHeader OH INNER JOIN OrderDetail OD on OH.OrderID = OD.OrderID ");
            sb.append(" where OH.upload='N' and OH.RetailerID =");
            sb.append(businessModel.QT(retailerId) + " and OH.invoiceStatus = 0 and sid=" + businessModel.getRetailerMasterBO().getDistributorId());

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
     * Delete the closing stock header and details table.
     */
    public void deleteStockAndOrder(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String id;
            Cursor closingStockCursor = db
                    .selectSQL("select StockID from ClosingStockHeader where RetailerID="
                            + businessModel.getRetailerMasterBO().getRetailerID() + "");
            if (closingStockCursor.getCount() > 0) {
                closingStockCursor.moveToNext();
                id = businessModel.QT(closingStockCursor.getString(0));
                db.deleteSQL("ClosingStockHeader", "StockID=" + id
                        + " and upload='N'", false);
                db.deleteSQL("ClosingStockDetail", "StockID=" + id
                        + " and upload='N'", false);
            }
            closingStockCursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Delete order Placed for the retailerId. Delete will only possible for
     * order which is not sync with server.
     *
     * @param retailerId current retailer Id
     */
    public void deleteOrder(Context context, String retailerId) {
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
                        + businessModel.retailerMasterBO.getIsVansales());
            }

            Cursor orderDetailCursor = db.selectSQL(sb.toString());

            if (orderDetailCursor != null) {
                if (orderDetailCursor.moveToNext()) {
                    orderId = orderDetailCursor.getString(0);
                }
            }
            orderDetailCursor.close();
            setOrderId(orderId + "");

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
                updateHangingOrder(context, businessModel.getRetailerMasterBO());
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * Load the Order Details and Order Header data into product master to Edit
     * Order.
     */
    public void loadOrderedProducts(Context mContext, String retailerId, String orderId) {
        businessModel.productHelper.clearOrderTableForInitiative();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            OrderHeader ordHeadBO = new OrderHeader();
            businessModel.setOrderHeaderBO(ordHeadBO);
            String orderID = "";
            StringBuilder sb = new StringBuilder();


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


            } else {

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
                        + businessModel.retailerMasterBO.getIsVansales());
            }

            sb.append(" and sid=" + businessModel.retailerMasterBO.getDistributorId());

            sb.append(" and OD.orderid not in(select orderid from OrderDeliveryDetail)");

            Cursor orderHeaderCursor = db.selectSQL(sb.toString());
            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext()) {
                    orderID = orderHeaderCursor.getString(0);

                    setOrderId(orderID);

                    businessModel.getOrderHeaderBO().setPO(orderHeaderCursor.getString(1));
                    businessModel.getOrderHeaderBO()
                            .setRemark(orderHeaderCursor.getString(2));

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
                    if (businessModel.getOrderHeaderBO().getSignatureName() != null && !businessModel.getOrderHeaderBO().getSignatureName().equals("") && !businessModel.getOrderHeaderBO().getSignatureName().equals("null")) {
                        businessModel.getOrderHeaderBO().setIsSignCaptured(true);
                    } else {
                        businessModel.getOrderHeaderBO().setIsSignCaptured(false);
                    }
                    businessModel.setOrderHeaderNote(orderHeaderCursor.getString(7));
                    this.invoiceDiscount = orderHeaderCursor.getDouble(5) + "";
                    businessModel.retailerMasterBO.setOrderTypeId(orderHeaderCursor.getString(12));

                    businessModel.setRField1(orderHeaderCursor.getString(14));
                    businessModel.setRField2(orderHeaderCursor.getString(15));

                    businessModel.getOrderHeaderBO()
                            .setRField1(orderHeaderCursor.getString(14));
                    businessModel.getOrderHeaderBO()
                            .setRField2(orderHeaderCursor.getString(15));

                }
                orderHeaderCursor.close();
            } else {
                businessModel.setOrderHeaderNote("");
                businessModel.setRField1("");
                businessModel.setRField2("");
            }


            String sql1 = "select productId,caseqty,pieceqty,  Rate, D1, D2, D3,"
                    + "uomcount,DA,totalamount,outerQty,dOuomQty,batchid,weight from "
                    + DataMembers.tbl_orderDetails
                    + " where orderId="
                    + businessModel.QT(orderID) + " order by rowid";


            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                String productId;
                while (orderDetailCursor.moveToNext()) {

                    int caseQty = orderDetailCursor.getInt(1);
                    int pieceQty = orderDetailCursor.getInt(2);
                    int caseSize = orderDetailCursor.getInt(7);
                    int outerQty = orderDetailCursor.getInt(10);
                    int outerSize = orderDetailCursor.getInt(11);
                    String batchId = orderDetailCursor.getString(12);
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

                        int qty = pieceQty + (caseQty * caseSize) + (outerQty * outerSize);
                        businessModel.productHelper.getmProductidOrderByEntryMap().put(Integer.parseInt(productId), qty);

                    }

                    if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        ProductMasterBO productBo = businessModel.getProductbyId(productId);
                        if (productBo != null) {
                            if (productBo.getBatchwiseProductCount() > 0) {
                                businessModel.batchAllocationHelper.setBatchwiseProducts(
                                        productId, caseQty, pieceQty, outerQty, srp,
                                        orderDetailCursor.getDouble(4),
                                        orderDetailCursor, caseSize, outerSize,
                                        batchId);
                            } else {
                                setProductDetails(productId, caseQty, pieceQty,
                                        outerQty, srp,
                                        orderDetailCursor.getDouble(4),
                                        orderDetailCursor, caseSize, outerSize, weight);
                            }
                        }

                    } else {
                        setProductDetails(productId, caseQty, pieceQty,
                                outerQty, srp, orderDetailCursor.getDouble(4),
                                orderDetailCursor, caseSize, outerSize, weight);
                    }


                }
                orderDetailCursor.close();
            }


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
                            for (BomReturnBO bomReturnBo : businessModel.productHelper
                                    .getBomReturnTypeProducts()) {
                                if (bomReturnBo.getPid().equals(typeId)) {
                                    bomReturnBo.setLiableQty(liableQty);
                                    bomReturnBo.setReturnQty(returnQty);
                                    bomReturnBo.setTypeId(typeId);
                                    break;
                                }
                            }
                        } else {
                            for (BomReturnBO bomReturnBo : businessModel.productHelper
                                    .getBomReturnProducts()) {
                                if (bomReturnBo.getPid().equals(pid)) {
                                    bomReturnBo.setLiableQty(liableQty);
                                    bomReturnBo.setReturnQty(returnQty);
                                    break;
                                }
                            }
                        }

                    }
                    orderDetailCursor.close();
                }

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
                    c.close();
                }

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
     * @param productId Current Product Id
     */
    private void setProductDetails(String productId, int caseQty, int pieceQty,
                                   int outerQty, float srp, double pricePerPiece, Cursor OrderDetails,
                                   int caseSize, int outerSize, float weight) {
        ProductMasterBO product;
        int siz = businessModel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        if (productId == null)
            return;


        for (int i = 0; i < siz; ++i) {
            product = businessModel.productHelper.getProductMaster().get(i);

            if (product.getProductID().equals(productId)) {
                product.setSchemeProducts(null);
                product.setOrderedPcsQty(pieceQty);
                product.setOrderedCaseQty(caseQty);
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
    }

    /**
     * This method will save the Invoice into InvoiceMaster table as well as the
     * Invoice details into Invoice Details Table.
     * Saving Invoice will also update the SIH in ProductMaster.
     */
    public void saveInvoice(Context mContext) {

        SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(mContext);
        salesReturnHelper.getSalesReturnGoods(mContext);
        ArrayList<ProductMasterBO> batchList;

        int isCreditNoteCreated = SalesReturnHelper.getInstance(mContext).isCreditNoteCreated(mContext);

        double orderValue;
        if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                && isCreditNoteCreated != 1) {
            if (salesReturnHelper.isValueReturned(mContext)) {
                orderValue = businessModel.getOrderHeaderBO().getOrderValue()
                        - salesReturnHelper.getSaleableValue();
            } else {
                orderValue = businessModel.getOrderHeaderBO().getOrderValue();
            }
        } else {
            orderValue = businessModel.getOrderHeaderBO().getOrderValue();
        }

        /*
         * update tax in invoice master Changed by Felix on 30-04-2015 For
		 * getting tax detail from order value
		 */
        if (businessModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
            businessModel.productHelper.taxHelper.downloadBillWiseTaxDetails();


            orderValue = Double.parseDouble(SDUtil.format(orderValue,
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));

            final double totalTaxValue = businessModel.productHelper.taxHelper.applyBillWiseTax(orderValue);

            if (businessModel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX)
                orderValue = orderValue + totalTaxValue;

        }


        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            double discountPercentage = businessModel.collectionHelper.getSlabwiseDiscountpercentage();

            String invoiceId;
            // Normally Generating Invoice ID
            invoiceId = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // If this Configuration on, Invoice ID generation differently
            // according to rule
            if (businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO) {
                String seqNo;
                businessModel.insertSeqNumber("INV");
                seqNo = businessModel.downloadSequenceNo("INV");
                invoiceId = seqNo;
            }

            String timeStampId = "";
            String query = "select max(VisitID) from OutletTimestamp where retailerid="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    timeStampId = c.getString(0);
                }
            }

            businessModel.invoiceNumber = invoiceId;

            String printFilePath = "";
            if (businessModel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                printFilePath = StandardListMasterConstants.PRINT_FILE_PATH + SDUtil.now(SDUtil.DATE_GLOBAL).replace("/", "") + "/"
                        + businessModel.userMasterHelper.getUserMasterBO().getUserid() + "/" +
                        StandardListMasterConstants.PRINT_FILE_INVOICE + businessModel.invoiceNumber + ".txt";

            // Save invoice header
            businessModel.setInvoiceDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat));
            String invoiceHeaderColumns = "invoiceno,invoicedate,retailerId,invNetamount,paidamount,orderid,ImageName,upload,beatid,discount,invoiceAmount,discountedAmount,latitude,longitude,return_amt,discount_type,salesreturned,LinesPerCall,IsPreviousInvoice,totalWeight,SalesType,sid,SParentID,stype,imgName,creditPeriod,PrintFilePath,timestampid,RemarksType,RField1,RField2,RField3";
            StringBuilder sb = new StringBuilder();
            sb.append(businessModel.QT(invoiceId) + ",");
            sb.append(businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",");
            sb.append(businessModel.QT(businessModel.retailerMasterBO.getRetailerID()) + ",");

            sb.append(businessModel.formatValueBasedOnConfig(orderValue) + ",");

            sb.append(0 + ",");
            sb.append(this.getOrderId() + ",");
            sb.append(businessModel.QT(businessModel.getOrderHeaderBO().getSignaturePath())
                    + ",");
            sb.append(businessModel.QT("N") + ",");
            sb.append(businessModel.getRetailerMasterBO().getBeatID() + ",");
            sb.append(businessModel.getOrderHeaderBO().getDiscount() + ",");
            sb.append(businessModel.formatValueBasedOnConfig(orderValue) + ",");
            double discountedAmount;
            if (businessModel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                if (discountPercentage > 0) {

                    double remainingAmount = (orderValue * discountPercentage) / 100;

                    discountedAmount = orderValue
                            - remainingAmount;
                    sb.append(businessModel.formatValueBasedOnConfig(discountedAmount) + ",");
                } else {
                    sb.append(businessModel.formatValueBasedOnConfig(orderValue) + ",");
                }
            } else {
                sb.append(businessModel.formatValueBasedOnConfig(orderValue) + ",");
            }
            sb.append(businessModel.QT(businessModel.mSelectedRetailerLatitude + "") + ",");
            sb.append(businessModel.QT(businessModel.mSelectedRetailerLongitude + "") + ",");
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCreditNoteCreated != 1) {
                if (salesReturnHelper.isValueReturned(mContext))
                    sb.append(salesReturnHelper.getSaleableValue() + ",");
                else
                    sb.append(0 + ",");
            } else {
                sb.append(+businessModel.getOrderHeaderBO().getRemainigValue() + ",");

            }
            sb.append(businessModel.configurationMasterHelper.discountType + ",");
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCreditNoteCreated != 1
                    && salesReturnHelper.isValueReturned(mContext))
                sb.append(1);
            else
                sb.append(0);
            sb.append("," + businessModel.getOrderHeaderBO().getLinesPerCall() + "," + 0);
            sb.append("," + businessModel.getOrderHeaderBO().getTotalWeight());
            sb.append("," + businessModel.QT(businessModel.retailerMasterBO.getOrderTypeId()));


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
            sb.append("," + businessModel.QT(timeStampId));
            sb.append("," + businessModel.getRemarkType());
            sb.append("," + businessModel.QT(businessModel.getRField1()));
            sb.append("," + businessModel.QT(businessModel.getRField2()));
            sb.append("," + businessModel.QT(businessModel.getRField3()));

            db.insertSQL(DataMembers.tbl_InvoiceMaster, invoiceHeaderColumns,
                    sb.toString());


            if (businessModel.configurationMasterHelper.IS_HANGINGORDER) {
                updateHangingOrder(mContext, businessModel.getRetailerMasterBO());
            }

            /* update free products sih starts */
            if (!businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                    || businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                businessModel.schemeDetailsMasterHelper.updateFreeProductsSIH(
                        this.getOrderId(), invoiceId, db);
            }

            /* insert tax details  */
            if (businessModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                businessModel.productHelper.taxHelper.insertInvoiceTaxList(invoiceId, db);

            }


            // update Invoice id in InvoiceDiscountDetail table
            if (businessModel.configurationMasterHelper.SHOW_DISCOUNT || businessModel.configurationMasterHelper.discountType == 1
                    || businessModel.configurationMasterHelper.discountType == 2 || businessModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

                businessModel.productHelper.updateInvoiceIdInItemLevelDiscount(db, invoiceId,
                        this.getOrderId());
            }

            // update Invoice id in InvoiceTaxDetail table
            if (businessModel.configurationMasterHelper.SHOW_TAX) {
                businessModel.productHelper.taxHelper.updateInvoiceIdInProductLevelTax(db, invoiceId,
                        this.getOrderId());
            }

            // update invoice createed in SalesReturnHeader **/
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCreditNoteCreated != 1) {
                db.executeQ("update SalesReturnHeader set invoicecreated=1 where RetailerID="
                        + businessModel.getRetailerMasterBO().getRetailerID());
            }
            // update credit not flag in sales return header **/
            if (businessModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && isCreditNoteCreated != 1
                    && salesReturnHelper.isValueReturned(mContext)) {
                db.executeQ("update SalesReturnHeader set credit_flag=2 where RetailerID="
                        + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
            }
            // update credit balance
            if (businessModel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE
                    && businessModel.getRetailerMasterBO().getCredit_balance() != -1) {
                double creditBalance = businessModel.getRetailerMasterBO()
                        .getCredit_balance() - businessModel.getOrderHeaderBO().getOrderValue();
                db.executeQ("update retailermaster set rfield1="
                        + creditBalance + " where retailerid="
                        + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                businessModel.getRetailerMasterBO().setCredit_balance(creditBalance);
                businessModel.getRetailerMasterBO().setRField1("" + creditBalance);

            }

            businessModel.productHelper.updateInvoiceIdInSalesReturn(db, invoiceId);

            // Save invoice details table and update sih
            ProductMasterBO product;
            String columns = "invoiceId,productid,qty,rate,uomdesc,retailerid,uomid,msqqty,uomCount,caseQty,pcsQty,d1,d2,d3,DA,totalamount,outerQty,dOuomQty,dOuomid,batchid,upload,CasePrice,OuterPrice,PcsUOMId,OrderType,priceoffvalue,PriceOffId,weight,hasserial,schemeAmount,DiscountAmount,taxAmount,HsnCode";
            int siz = businessModel.productHelper.getProductMaster().size();
            for (int i = 0; i < siz; ++i) {
                product = businessModel.productHelper.getProductMaster()
                        .elementAt(i);

                if ((product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0 || product
                        .getOrderedOuterQty() > 0)) {

                    String values;
                    int totalqty = (product.getOrderedPcsQty())
                            + (product.getCaseSize() * product
                            .getOrderedCaseQty())
                            + (product.getOrderedOuterQty() * product
                            .getOutersize());

                    if (product.getBatchwiseProductCount() == 0 || !businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {

                        values = getInvoiceDetailsRecords(product, null, invoiceId,
                                db, false).toString();
                        db.insertSQL(DataMembers.tbl_InvoiceDetails, columns,
                                values);

                    } else {

                        batchList = businessModel.batchAllocationHelper
                                .getBatchlistByProductID().get(
                                        product.getProductID());

                        if (batchList != null) {
                            int count = 0;
                            for (ProductMasterBO batchWiseProductBO : batchList) {
                                if (batchWiseProductBO.getOrderedPcsQty() > 0
                                        || batchWiseProductBO
                                        .getOrderedCaseQty() > 0
                                        || batchWiseProductBO
                                        .getOrderedOuterQty() > 0) {
                                    values = getInvoiceDetailsRecords(product,
                                            batchWiseProductBO, invoiceId, db, true)
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
                businessModel.productHelper.saveReturnDetails(businessModel.QT(invoiceId), 1, db);
            }


            // Insert Product Details to Empty Reconciliation tables if Type  wise Group products disabled

            if (!businessModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                businessModel.mEmptyReconciliationhelper.saveSKUWiseTransaction();

            // Update the OrderHeader that , Invoice is created for this Order
            // and the Order is
            // not allowed to edit again.
            String sql = "update " + DataMembers.tbl_orderHeader
                    + " set invoiceStatus=1  where RetailerID="
                    + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID())
                    + " and orderid=" + this.getOrderId();
            db.executeQ(sql);

            if (businessModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN)
                saveSerialNo(db);

            businessModel.productHelper.updateSchemeAndDiscAndTaxValue(db, invoiceId);


            db.closeDB();

            try {
                businessModel.getRetailerMasterBO().setVisit_Actual(
                        (float) getRetailerOrderValue(mContext, businessModel.retailerMasterBO.getRetailerID()));
            } catch (Exception e) {

                Commons.printException(e);
            }

        } catch (Exception e) {

            Commons.printException(e);
        }
    }


    /**
     * Prepare invoice detail records to insert
     *
     * @param productBO   current product object
     * @param batchWiseBO current batch product
     * @param invoiceId   invoice id
     * @param db          database object
     * @param isBatchWise is batch wise or not
     * @return return values
     */
    private StringBuffer getInvoiceDetailsRecords(ProductMasterBO productBO,
                                                  ProductMasterBO batchWiseBO, String invoiceId, DBUtil db,
                                                  boolean isBatchWise) {
        ProductMasterBO product = productBO;
        ProductMasterBO batchWiseProductBO;
        StringBuffer sb = new StringBuffer();
        int schemeOrderType;

        int orderedPcsQty;
        int orderedCaseQty;
        int orderedOuterQty;

        String batchId;
        double priceOffValue;
        int priceOffId;

        int totalqty;
        float srp;
        double csrp;
        double osrp;
        double prodDisc;
        double schemeDisc;
        double taxAmount;
        double line_total_price;

        try {
            if (isBatchWise) {
                batchWiseProductBO = batchWiseBO;
                orderedPcsQty = batchWiseProductBO.getOrderedPcsQty();
                orderedCaseQty = batchWiseProductBO.getOrderedCaseQty();
                orderedOuterQty = batchWiseProductBO.getOrderedOuterQty();
                batchId = batchWiseProductBO.getBatchid();
                schemeOrderType = businessModel.productHelper.getmOrderType().get(1);
                srp = batchWiseProductBO.getSrp();
                csrp = batchWiseProductBO.getCsrp();
                osrp = batchWiseProductBO.getOsrp();

                totalqty = orderedPcsQty
                        + (orderedCaseQty * product.getCaseSize())
                        + (orderedOuterQty * product.getOutersize());

                if (batchWiseProductBO.getSIH() >= totalqty) {
                    batchWiseProductBO.setSIH(batchWiseProductBO.getSIH()
                            - totalqty);
                } else {
                    batchWiseProductBO.setSIH(0);
                }

                priceOffValue = batchWiseProductBO.getPriceoffvalue() * totalqty;
                priceOffId = batchWiseProductBO.getPriceOffId();
                schemeDisc = batchWiseProductBO.getSchemeDiscAmount();
                prodDisc = batchWiseProductBO.getProductDiscAmount();
                taxAmount = batchWiseProductBO.getTaxApplyvalue();
                line_total_price = (batchWiseProductBO.getOrderedCaseQty() * batchWiseProductBO
                        .getCsrp())
                        + (batchWiseProductBO.getOrderedPcsQty() * batchWiseProductBO.getSrp())
                        + (batchWiseProductBO.getOrderedOuterQty() * batchWiseProductBO.getOsrp());


            } else {
                orderedPcsQty = product.getOrderedPcsQty();
                orderedCaseQty = product.getOrderedCaseQty();
                orderedOuterQty = product.getOrderedOuterQty();
                srp = product.getSrp();
                csrp = product.getCsrp();
                osrp = product.getOsrp();
                batchId = 0 + "";
                schemeOrderType = businessModel.productHelper.getmOrderType().get(1);

                totalqty = orderedPcsQty
                        + (orderedCaseQty * product.getCaseSize())
                        + (orderedOuterQty * product.getOutersize());

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

            // update SIH
            db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                    + totalqty
                    + " then ifnull(qty,0)-"
                    + totalqty
                    + " else 0 end) where pid="
                    + product.getProductID()
                    + " and batchid=" + businessModel.QT(batchId));

            sb.append(businessModel.QT(invoiceId) + ",");
            sb.append(businessModel.QT(product.getProductID()) + ",");
            sb.append(totalqty + "," + srp + ",");
            sb.append(businessModel.QT(product.getOU()) + ",");
            sb.append(businessModel.QT(businessModel.retailerMasterBO.getRetailerID()));
            sb.append("," + product.getCaseUomId() + ",");
            sb.append(product.getMSQty() + ",");
            sb.append(product.getCaseSize() + ",");
            sb.append(orderedCaseQty + ",");
            sb.append(orderedPcsQty + ",");
            sb.append(product.getD1() + "," + product.getD2());
            sb.append("," + product.getD3() + ",");
            sb.append(product.getDA() + ",");
            sb.append(line_total_price);
            sb.append("," + orderedOuterQty + ",");
            sb.append(product.getOutersize() + ",");
            sb.append(product.getOuUomid() + "," + batchId);
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


    /**
     * get total ordered value for given retailer
     *
     * @param mContext   current context
     * @param retailerId retailer id
     * @return total order value
     */
    private double getRetailerOrderValue(Context mContext, String retailerId) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        double f = 0;
        try {
            Cursor c;
            if (businessModel.configurationMasterHelper.IS_INVOICE)
                c = db.selectSQL("SELECT sum(invNetAmount) FROM InvoiceMaster where retailerid="
                        + retailerId);

            else
                c = db.selectSQL("select sum (OrderValue) from OrderHeader where retailerid=" + retailerId);


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
     * get total ordered focus and must sell products count
     *
     * @param mOrderedProductList product list ordered
     */
    public void getFocusAndMustSellOrderedProducts(LinkedList<ProductMasterBO> mOrderedProductList) {
        int focusBrandProducts;
        int focusBrandProducts1 = 0;
        int focusBrandProducts2 = 0;
        int focusBrandProducts3 = 0;
        int focusBrandProducts4 = 0;
        int mustSellProducts = 0;
        double mustSellProdValues = 0;
        double focusBrandProdValues = 0;

        for (ProductMasterBO bo : mOrderedProductList) {
            if (bo.getIsFocusBrand() == 1 || bo.getIsFocusBrand2() == 1 || bo.getIsFocusBrand3() == 1 || bo.getIsFocusBrand4() == 1) {
                focusBrandProdValues += bo.getDiscount_order_value();
            }
            if (bo.getIsFocusBrand() == 1) {
                focusBrandProducts1 = 1;
            }
            if (bo.getIsFocusBrand2() == 1) {
                focusBrandProducts2 = 1;
            }
            if (bo.getIsFocusBrand3() == 1) {
                focusBrandProducts3 = 1;
            }
            if (bo.getIsFocusBrand4() == 1) {
                focusBrandProducts4 = 1;
            }


            if (bo.getIsMustSell() == 1) {
                mustSellProdValues += bo.getDiscount_order_value();
                mustSellProducts += 1;
            }
        }
        focusBrandProducts = focusBrandProducts1 + focusBrandProducts2 + focusBrandProducts3 + focusBrandProducts4;

        if (businessModel.getOrderHeaderBO() != null) {
            businessModel.getOrderHeaderBO().setOrderedFocusBrands(focusBrandProducts);
            businessModel.getOrderHeaderBO().setOrderedMustSellCount(mustSellProducts);
            businessModel.getOrderHeaderBO().setTotalMustSellValue(mustSellProdValues);
            businessModel.getOrderHeaderBO().setTotalFocusProdValues(focusBrandProdValues);
        }
    }


    /**
     * Organizing products by user ordered OR entered flow
     *
     * @return Ordered product list
     */
    public LinkedList<ProductMasterBO> organizeProductsByUserEntry() {
        LinkedList<ProductMasterBO> mOrderedProductList = new LinkedList<>();
        LinkedList<String> productIdList = businessModel.productHelper.getmProductidOrderByEntry();
        if (productIdList != null) {

            for (String productId : productIdList) {
                ProductMasterBO productBO = businessModel.productHelper.getProductMasterBOById(productId);
                if (productBO != null) {
                    if (productBO.getOrderedCaseQty() > 0 || productBO.getOrderedPcsQty() > 0 || productBO.getOrderedOuterQty() > 0) {
                        mOrderedProductList.add(productBO);
                    }
                }
            }
        }

        return mOrderedProductList;
    }


    /**
     * get retailer hanging(order without invoice) order availability status
     *
     * @param mContext current context
     * @param retObj   retailer object
     */
    public void updateHangingOrder(Context mContext, RetailerMasterBO retObj) {
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
                OrderId = new ArrayList<>();
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
            if (db != null)
                db.closeDB();
        }
    }


    /**
     * Load invoiced products
     *
     * @param context       current context
     * @param invoiceNumber invoice number
     */
    public void loadInvoiceProducts(Context context, String invoiceNumber) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String sql2 = "select discount,discount_type from InvoiceMaster where invoiceNo="
                + businessModel.QT(invoiceNumber) + "";
        Cursor invoiceDetailCursor = db.selectSQL(sql2);

        if (invoiceDetailCursor != null) {
            if (invoiceDetailCursor.moveToNext()) {
                this.invoiceDiscount = invoiceDetailCursor.getString(0);
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
            String productId;
            while (invoiceDetailCursor.moveToNext()) {
                int pieceQty = invoiceDetailCursor.getInt(1);
                int caseQty = invoiceDetailCursor.getInt(2);
                int outerQty = invoiceDetailCursor.getInt(9);
                float weight = invoiceDetailCursor.getFloat(10);
                float srp = invoiceDetailCursor.getFloat(11);

                productId = invoiceDetailCursor.getString(0);
                setProductDetails(productId, caseQty, pieceQty, outerQty, srp,
                        invoiceDetailCursor.getDouble(3), invoiceDetailCursor,
                        0, 0, weight);


            }
            invoiceDetailCursor.close();
        }


        db.closeDB();
    }


    /**
     * Load serial number
     *
     * @param mContext
     */
    public void loadSerialNo(Context mContext) {
        DBUtil db;
        try {
            mSerialNoListByProductId = new SparseArray<>();
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select productid,fromNo,toNo,scannedQty from temp_serialno ");
            sb.append("where retailerid =" + businessModel.getRetailerMasterBO().getRetailerID());
            sb.append(" order by productid");
            Cursor c = db.selectSQL(sb.toString());
            int productId = 0;
            if (c.getCount() > 0) {
                ArrayList<SerialNoBO> serialNoList = new ArrayList<>();
                SerialNoBO serialNoBO;
                while (c.moveToNext()) {
                    serialNoBO = new SerialNoBO();
                    serialNoBO.setFromNo(c.getString(1));
                    serialNoBO.setToNo(c.getString(2));
                    serialNoBO.setScannedQty(c.getInt(3));
                    if (productId != c.getInt(0)) {
                        if (productId != 0) {
                            mSerialNoListByProductId.put(productId, serialNoList);
                            serialNoList = new ArrayList<>();
                            serialNoList.add(serialNoBO);
                            productId = c.getInt(0);
                        } else {
                            serialNoList = new ArrayList<>();
                            serialNoList.add(serialNoBO);
                            productId = c.getInt(0);
                        }
                    } else {
                        serialNoList.add(serialNoBO);
                    }


                }
                if (serialNoList.size() > 0) {
                    mSerialNoListByProductId.put(productId, serialNoList);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }

    /**
     * Save serial number
     *
     * @param db database object
     */
    private void saveSerialNo(DBUtil db) {
        String columns = "orderid,invoiceid,pid,serialNumber,uomid,Retailerid";
        StringBuffer sb;
        if (mSerialNoListByProductId != null) {
            for (ProductMasterBO productBO : businessModel.productHelper.getProductMaster()) {
                if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {

                    ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductId.get(Integer.parseInt(productBO.getProductID()));
                    if (serialNoList != null) {
                        for (SerialNoBO serialNoBo : serialNoList) {
                            if (serialNoBo.getScannedQty() > 0) {
                                for (int i = 0; i < serialNoBo.getScannedQty(); i++) {
                                    try {
                                        BigInteger serialNo = new BigInteger(serialNoBo.getFromNo());
                                        BigInteger one = new BigInteger(i + "");
                                        BigInteger sumValue = serialNo.add(one);
                                        sb = new StringBuffer();
                                        sb.append(businessModel.getOrderid() + "," + businessModel.QT(businessModel.getInvoiceNumber()) + ",");
                                        sb.append(productBO.getProductID() + "," + businessModel.QT(sumValue + "") + "," + productBO.getPcUomid());
                                        sb.append("," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                                        db.insertSQL("InvoiceSerialNumbers", columns, sb.toString());
                                    } catch (NumberFormatException e) {
                                        sb = new StringBuffer();
                                        sb.append(businessModel.getOrderid() + "," + businessModel.QT(businessModel.getInvoiceNumber()) + ",");
                                        sb.append(productBO.getProductID() + "," + businessModel.QT(serialNoBo.getFromNo() + "") + "," + productBO.getPcUomid());
                                        sb.append("," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                                        db.insertSQL("InvoiceSerialNumbers", columns, sb.toString());
                                    }

                                }
                            }

                        }
                    }
                }
            }
        }

        db.deleteSQL("temp_serialno", "retailerid=" + businessModel.getRetailerMasterBO().getRetailerID(), false);
        mSerialNoListByProductId = null;


    }

    /**
     * Is all ordered product is scanned
     *
     * @return Is all scanned or not
     */
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


    /**
     * Is any product is scanned
     *
     * @param mOrderedProductList ordered product list
     * @return Is scanned
     */
    public boolean isOrderedSerialNoProducts(LinkedList<ProductMasterBO> mOrderedProductList) {
        for (ProductMasterBO productBO : mOrderedProductList) {
            if (productBO.getScannedProduct() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to find duplicate serialnumber entered     *
     *
     * @return Is serial number duplicated
     */
    public boolean isDuplicateSerialNo() {
        ArrayList<Integer> serialNo;
        if (mSerialNoListByProductId != null) {


            for (ProductMasterBO productBO : businessModel.productHelper.getProductMaster()) {
                if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {
                    if (productBO.getScannedProduct() == 1) {
                        serialNo = new ArrayList<>();

                        ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductId.get(Integer.parseInt(productBO.getProductID()));
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

    /**
     * Save serial number to temp table
     *
     * @param mContext current context
     */
    public void saveSerialNoTemp(Context mContext) {
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL("temp_serialno", "retailerid=" + businessModel.getRetailerMasterBO().getRetailerID(), false);


            String columns = "productid,fromNo,toNo,Retailerid,scannedQty";
            StringBuffer sb;
            if (mSerialNoListByProductId != null) {
                for (int i = 0; i < mSerialNoListByProductId.size(); i++) {
                    int key = mSerialNoListByProductId.keyAt(i);
                    ArrayList<SerialNoBO> serialNoList = mSerialNoListByProductId.valueAt(i);
                    if (serialNoList != null) {
                        for (SerialNoBO serialNoBO : serialNoList) {

                            sb = new StringBuffer();
                            sb.append(key + "," + businessModel.QT(serialNoBO.getFromNo()));
                            sb.append("," + businessModel.QT(serialNoBO.getToNo()));
                            sb.append("," + businessModel.getRetailerMasterBO().getRetailerID());
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
     * Save delivery order details and update SIH
     *
     * @param mContext  current context
     * @param isPartial Is partial delivery
     */
    public void insertDeliveryOrderRecord(Context mContext, boolean isPartial) {

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
                            values = businessModel.QT(getOrderId()) + "," + product.getProductID();
                            values += "," + product.getDeliveredCaseQty() + "," + product.getCaseUomId() + "," + product.getCaseSize()
                                    + "," + product.getCsrp() + "," + product.getCsrp() + "," + (product.getCsrp() * product.getDeliveredCaseQty()) + "," + businessModel.QT("N");
                            db.insertSQL("OrderDeliveryDetail",
                                    columns, values);
                        }
                        if (product.getDeliveredPcsQty() > 0) {
                            values = businessModel.QT(getOrderId()) + "," + product.getProductID();
                            values += "," + product.getDeliveredPcsQty() + "," + product.getPcUomid() + ",1"
                                    + "," + product.getSrp() + "," + product.getSrp() + "," + (product.getSrp() * product.getDeliveredPcsQty()) + "," + businessModel.QT("N");
                            db.insertSQL("OrderDeliveryDetail",
                                    columns, values);
                        }
                        if (product.getDeliveredOuterQty() > 0) {
                            values = businessModel.QT(getOrderId()) + "," + product.getProductID();
                            values += "," + product.getDeliveredOuterQty() + "," + product.getOuUomid() + "," + product.getOutersize()
                                    + "," + product.getOsrp() + "," + product.getOsrp() + "," + (product.getOsrp() * product.getDeliveredOuterQty()) + "," + businessModel.QT("N");
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
                            values = getOrderId() + "," + product.getProductID();
                            values += "," + product.getOrderedCaseQty() + "," + product.getCaseUomId() + "," + product.getCaseSize()
                                    + "," + product.getCsrp() + "," + product.getCsrp() + "," + (product.getCsrp() * product.getOrderedCaseQty()) + "," + businessModel.QT("N");
                            db.insertSQL("OrderDeliveryDetail",
                                    columns, values);
                        }
                        if (product.getOrderedPcsQty() > 0) {
                            values = getOrderId() + "," + product.getProductID();
                            values += "," + product.getOrderedPcsQty() + "," + product.getPcUomid() + ",1"
                                    + "," + product.getSrp() + "," + product.getSrp() + "," + (product.getSrp() * product.getOrderedPcsQty()) + "," + businessModel.QT("N");
                            db.insertSQL("OrderDeliveryDetail",
                                    columns, values);
                        }
                        if (product.getOrderedOuterQty() > 0) {
                            values = getOrderId() + "," + product.getProductID();
                            values += "," + product.getOrderedOuterQty() + "," + product.getOuUomid() + "," + product.getOutersize()
                                    + "," + product.getOsrp() + "," + product.getOsrp() + "," + (product.getOsrp() * product.getOrderedOuterQty()) + "," + businessModel.QT("N");
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
                                values = getOrderId() + "," + schemeProductBO.getProductId();
                                values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getPcUomid() + ",1"
                                        + "," + productMasterBO.getSrp() + "," + productMasterBO.getSrp() + "," + (productMasterBO.getSrp() * schemeProductBO.getQuantitySelected()) + "," + businessModel.QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                values = getOrderId() + "," + schemeProductBO.getProductId();
                                values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getCaseUomId() + "," + productMasterBO.getCaseSize()
                                        + "," + productMasterBO.getCsrp() + "," + productMasterBO.getCsrp() + "," + (productMasterBO.getCsrp() * schemeProductBO.getQuantitySelected()) + "," + businessModel.QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }
                            if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                values = getOrderId() + "," + schemeProductBO.getProductId();
                                values += "," + schemeProductBO.getQuantitySelected() + "," + productMasterBO.getOuUomid() + "," + productMasterBO.getOutersize()
                                        + "," + productMasterBO.getOsrp() + "," + productMasterBO.getOsrp() + "," + (productMasterBO.getOsrp() * schemeProductBO.getQuantitySelected()) + "," + businessModel.QT("N");
                                db.insertSQL("OrderDeliveryDetail",
                                        columns, values);
                            }


                            //Update the SIH

                            int totalQty = 0;
                            if (schemeProductBO.getUomID() == productMasterBO.getPcUomid()) {
                                totalQty = schemeProductBO.getQuantitySelected();
                            } else if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                totalQty = (schemeProductBO.getQuantitySelected() * productMasterBO.getCaseSize());
                            } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                totalQty = (schemeProductBO.getQuantitySelected() * productMasterBO.getOutersize());
                            }

                            int s = productMasterBO.getSIH() > totalQty ? productMasterBO.getSIH()
                                    - totalQty : 0;

                            db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                    + totalQty
                                    + " then ifnull(sih,0)-"
                                    + totalQty
                                    + " else 0 end) where pid="
                                    + productMasterBO.getProductID());

                            //
                            db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                                    + totalQty
                                    + " then ifnull(qty,0)-"
                                    + totalQty
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


    /**
     * To check whether reason provided for un satisfied indicative order
     *
     * @param orderedList ordered product list
     * @return reason availability
     */
    public boolean isReasonProvided(LinkedList<ProductMasterBO> orderedList) {
        int siz = orderedList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = orderedList.get(i);
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
     * Method used to add Off invoice scheme  free product in Last ordered  product (scheme product object).So that
     * we can show in Print
     *
     * @param mOrderedProductList ordered product list
     */
    public void updateOffInvoiceSchemeInProductOBJ(LinkedList<ProductMasterBO> mOrderedProductList) {
        ProductMasterBO productBO = mOrderedProductList.get(mOrderedProductList.size() - 1);
        if (productBO != null) {
            ArrayList<SchemeBO> offInvoiceSchemeList = businessModel.schemeDetailsMasterHelper.getmOffInvoiceAppliedSchemeList();
            if (offInvoiceSchemeList != null) {
                for (SchemeBO schemeBO : offInvoiceSchemeList) {
                    if (schemeBO.isQuantityTypeSelected()) {
                        updateSchemeFreeProduct(schemeBO, productBO);
                    }
                }
            }
        }

    }

    /**
     * Method to add free product list into any one of scheme buy product     *
     *
     * @param schemeBO  Current Scheme
     * @param productBO current product
     */
    public void updateSchemeFreeProduct(SchemeBO schemeBO,
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

    /**
     * To check is tax available for all product
     *
     * @param mOrderedProductList Ordered product list
     * @return tax availability
     */
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


    /**
     * To check is tax available for any product
     *
     * @param mOrderedProductList Ordered product list
     * @return tax availability
     */
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


    /**
     * Method to check whether stock is available to deliver
     *
     * @param orderList Orderd list
     * @return stock avilability
     */
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


    /**
     * Numer of print taken for current invoice
     *
     * @param mContext current context
     * @return number of print taken
     */
    public int getPrintedCountForCurrentInvoice(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select print_count from InvoiceMaster where invoiceNo='" + businessModel.invoiceNumber + "'");
            if (c != null) {
                if (c.moveToNext()) {

                    print_count = c.getInt(0);
                    c.close();
                    db.closeDB();
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return print_count;
    }


    /**
     * Is stock menu enabled
     *
     * @return stock check availability
     */
    public boolean isStockCheckMenuEnabled() {
        Vector<ConfigureBO> config = businessModel.configurationMasterHelper.getActivityMenu();
        for (int i = 0; i < config.size(); i++) {
            ConfigureBO con = config.get(i);
            if (con.getConfigCode().equals("MENU_STOCK")
                    || con.getConfigCode().equals("MENU_STK_ORD"))
                if (con.getHasLink() == 1 && con.isFlag() == 1)
                    return true;
        }

        return false;
    }

    public boolean isOverDueAvail(Context mContext){

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        boolean isDuePassed = false;
        try{
            Cursor c = db.selectSQL("select InvoiceDate from InvoiceMaster where Retailerid='" + businessModel.getRetailerMasterBO().getRetailerID() + "' and invNetAmount > paidAmount");
            if (businessModel.getRetailerMasterBO().getCreditDays() > 0 && c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    Date date = format.parse(c.getString(0));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_YEAR, businessModel.retailerMasterBO.getCreditDays());
                    Date dueDate = format.parse(format.format(calendar.getTime()));
                    Date currDate = format.parse(SDUtil.now(4));
                    Commons.print("Order Helper," + "dueDate " + dueDate + " -- currDate "+currDate);

                    if (dueDate.compareTo(currDate) != 0 && currDate.after(dueDate)) {
                        isDuePassed = true;
                        break;
                    }
                }
                c.close();
            }

            db.closeDB();

            return isDuePassed;

        }catch(Exception e){
            db.closeDB();
            Commons.printException("" + e);
        }
        return isDuePassed;
    }


}
