package com.ivy.cpg.view.order;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StoreWsieDiscountBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 29/1/18.
 */

public class DiscountHelper {

    private static DiscountHelper instance = null;
    private Context mContext;
    private BusinessModel businessModel;

    private HashMap<String, HashMap<Integer, Double>> mDiscountmapByProductwithBathid = new HashMap<String, HashMap<Integer, Double>>();
    private ArrayList<StoreWsieDiscountBO> mBillWiseDiscountList;
    // Bill wise  payterm discount details list and hashmap
    private ArrayList<StoreWsieDiscountBO> mBillWisePayternDiscountList;

    private DiscountHelper(Context context) {
        this.mContext = context;
        this.businessModel = (BusinessModel) context;
    }


    public static DiscountHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DiscountHelper(context);
        }
        return instance;
    }


    /**
     * Method to use clear discount and tax value
     *
     * @param orderList
     */
    public void clearProductDiscountAndTaxValue(List<ProductMasterBO> orderList) {
        for (ProductMasterBO productMasterBO : orderList) {
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productMasterBO.getBatchwiseProductCount() > 0) {
                ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                if (batchList != null) {
                    for (ProductMasterBO batchProduct : batchList) {
                        batchProduct.setProductDiscAmount(0);
                        batchProduct.setSchemeDiscAmount(0);
                        batchProduct.setTaxValue(0);
                    }
                }

            } else {
                productMasterBO.setProductDiscAmount(0);
                productMasterBO.setSchemeDiscAmount(0);
                productMasterBO.setTaxValue(0);
            }
        }

    }


    /**
     * Calculate entry level discount and update product line value
     * @param orderedList Ordered product List
     * @return Total discount value of ordered products
     */
    public double calculateEntryLevelDiscount(List<ProductMasterBO> orderedList) {

        mDiscountmapByProductwithBathid = new HashMap<>();
        double totalDiscountValue = 0;

        if (orderedList != null) {

            for (ProductMasterBO productBO : orderedList) {

                double discountvalue = 0.0;
                double totalEnteredDiscount = productBO.getD1()
                        + productBO.getD2() + productBO.getD3();
                boolean isBatchWise;

                if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                        && businessModel.configurationMasterHelper.IS_INVOICE
                        && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        isBatchWise = true;

                    } else {
                        isBatchWise = false;
                    }
                } else {
                    isBatchWise = false;
                }

                if (totalEnteredDiscount > 0) {
                    discountvalue = getProductsDiscountValue(productBO,
                            totalEnteredDiscount, 1, isBatchWise, 0, true);

                } else if (productBO.getDA() > 0) {
                    discountvalue = getProductsDiscountValue(productBO,
                            productBO.getDA(), 0, isBatchWise, 0, true);

                }

                if (productBO.getDiscount_order_value() > 0) {
                    productBO.setDiscount_order_value(productBO
                            .getDiscount_order_value() - discountvalue);
                }

                totalDiscountValue = totalDiscountValue + discountvalue;
            }
        }
        return totalDiscountValue;

    }

    public HashMap<String, HashMap<Integer, Double>> getDiscountMapByProductwidthBatchid() {
        return mDiscountmapByProductwithBathid;
    }

    /**
     * Calculate discount value for given product
     * @param productBO Product object
     * @param value discount
     * @param discOrAmt if 1 - percentage,0 - amount based discount
     * @param isBatchWise Is batchWise product
     * @param discountId Discount Id
     * @param isCompanyDiscount Is company discount
     * @return
     */
    private double getProductsDiscountValue(ProductMasterBO productBO,
                                            double value, int discOrAmt, boolean isBatchWise, int discountId, boolean isCompanyDiscount) {
        double totalDiscOrAmtValue = 0;

        if (isBatchWise) {
            totalDiscOrAmtValue = getProductDiscountValueBatchWise(productBO,
                    value, discOrAmt, discountId);

        } else {

            int totalQty = productBO.getOrderedPcsQty()
                    + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                    + productBO.getOrderedOuterQty() * productBO.getOutersize();

            double line_total_price = (productBO.getOrderedCaseQty() * productBO
                    .getCsrp())
                    + (productBO.getOrderedPcsQty() * productBO.getSrp())
                    + (productBO.getOrderedOuterQty() * productBO.getOsrp());

            double totalValue;

            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productBO.getBatchwiseProductCount() > 0) {
                totalValue = businessModel.schemeDetailsMasterHelper
                        .getbatchWiseTotalValue(productBO);
            } else {
                totalValue = line_total_price;

            }

            if (isCompanyDiscount) {
                totalValue = totalValue - productBO.getDistributorTypeDiscount();
            }

            if (discOrAmt == 1) {
                totalDiscOrAmtValue = totalValue * value / 100;

            } else if (discOrAmt == 0) {
                totalDiscOrAmtValue = totalQty * value;

            }

            if (discountId == 0) {
                productBO.setApplyValue(totalDiscOrAmtValue);
            }

            if (isCompanyDiscount) {
                productBO.setCompanyTypeDiscount(productBO.getCompanyTypeDiscount() + totalDiscOrAmtValue);
            } else {
                productBO.setDistributorTypeDiscount(productBO.getDistributorTypeDiscount() + totalDiscOrAmtValue);
            }

            HashMap<Integer, Double> discountValueByDiscountId = mDiscountmapByProductwithBathid.get(productBO.getProductID());
            if (discountValueByDiscountId == null)
                discountValueByDiscountId = new HashMap<>();

            discountValueByDiscountId.put(discountId, totalDiscOrAmtValue);

            mDiscountmapByProductwithBathid.put(productBO.getProductID(), discountValueByDiscountId);

        }

        return totalDiscOrAmtValue;
    }


    /**
     * Calculate product discount value batch wise
     * @param productBO Product for which discount value to be calculated
     * @param value Discount
     * @param discOrAmt if 1 - percentage,0 - amount based discount
     * @param discountid Discount Id
     * @return Discount value for given product
     */
    private double getProductDiscountValueBatchWise(
            ProductMasterBO productBO, double value, int discOrAmt, int discountid) {


        double totalProductDisOrAmtValue = 0.0;
        ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        if (batchList != null) {
            for (ProductMasterBO batchProductBo : batchList) {
                double totalbatchDiscOrAmtValue = 0;
                int totalQty = batchProductBo.getOrderedPcsQty()
                        + batchProductBo.getOrderedCaseQty()
                        * productBO.getCaseSize()
                        + batchProductBo.getOrderedOuterQty()
                        * productBO.getOutersize();
                if (totalQty > 0) {
                    double totalValue = 0.0;
                    if (batchProductBo.getSchemeAppliedValue() > 0) {
                        totalValue = batchProductBo.getSchemeAppliedValue();
                    } else {
                        totalValue = batchProductBo.getOrderedPcsQty()
                                * batchProductBo.getSrp()
                                + batchProductBo.getOrderedCaseQty()
                                * batchProductBo.getCsrp()
                                + batchProductBo.getOrderedOuterQty()
                                * batchProductBo.getOsrp();

                    }

                    if (discOrAmt == 1) {
                        totalbatchDiscOrAmtValue = totalValue * value / 100;
                    } else if (discOrAmt == 0) {
                        totalbatchDiscOrAmtValue = totalValue - (totalQty * (batchProductBo.getSrp() - value));

                    }
                    String productWithbatchId = batchProductBo.getProductID() + batchProductBo.getBatchid();
                    HashMap<Integer, Double> discountValueByDiscountId = mDiscountmapByProductwithBathid.get(productWithbatchId);
                    if (discountValueByDiscountId == null)
                        discountValueByDiscountId = new HashMap<Integer, Double>();
                    batchProductBo.setProductDiscAmount(batchProductBo.getProductDiscAmount() + totalbatchDiscOrAmtValue);


                    discountValueByDiscountId.put(discountid, totalbatchDiscOrAmtValue);

                    mDiscountmapByProductwithBathid.put(productWithbatchId, discountValueByDiscountId);


                    if (batchProductBo.getDiscount_order_value() > 0) {
                        batchProductBo.setDiscount_order_value(batchProductBo
                                .getDiscount_order_value()
                                - totalbatchDiscOrAmtValue);
                    }

                    if (discountid == 0) {
                        batchProductBo.setApplyValue(totalbatchDiscOrAmtValue);
                    }


                    totalProductDisOrAmtValue = totalProductDisOrAmtValue
                            + totalbatchDiscOrAmtValue;

                }
            }
        }
        return totalProductDisOrAmtValue;

    }


    /**
     * Calculating item level discount
     * @return Discount value
     */
    public double calculateItemLevelDiscount() {

        double totalDiscountValue = 0.0;

        if (businessModel.productHelper.getDiscountIdList() != null) {
            for (Integer discountID : businessModel.productHelper.getDiscountIdList()) {

                double discountValue = updateItemLevelDiscountByPercentageOrAmt(discountID);

                totalDiscountValue = totalDiscountValue + discountValue;
            }
        }

        return totalDiscountValue;
    }

    private double updateItemLevelDiscountByPercentageOrAmt(int discountId) {
        double totalDiscountValue = 0.0;

        ArrayList<StoreWsieDiscountBO> discountProductIdList = businessModel.productHelper.getProductDiscountListByDiscountID()
                .get(discountId);
        if (discountProductIdList != null) {
            for (StoreWsieDiscountBO storeWiseDiscountBO : discountProductIdList) {

                ProductMasterBO productBo = businessModel.productHelper
                        .getProductMasterBOById(String.valueOf(storeWiseDiscountBO
                                .getProductId()));

                if (productBo != null) {
                    if (productBo.getOrderedPcsQty() > 0
                            || productBo.getOrderedCaseQty() > 0
                            || productBo.getOrderedOuterQty() > 0) {

                        boolean isBatchWise;

                        int percentageORAmountDiscount = storeWiseDiscountBO
                                .getIsPercentage() == 1 ? 1 : 0;

                        if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && businessModel.configurationMasterHelper.IS_INVOICE
                                && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            if (productBo.getBatchwiseProductCount() > 0) {
                                isBatchWise = true;

                            } else {
                                isBatchWise = false;
                            }

                        } else {
                            isBatchWise = false;
                        }

                        boolean isCompanyWiseDisc;
                        if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                            isCompanyWiseDisc = true;
                        } else {
                            isCompanyWiseDisc = false;
                        }


                        final double discountValue = getProductsDiscountValue(productBo,
                                storeWiseDiscountBO.getDiscount(), percentageORAmountDiscount,
                                isBatchWise, discountId, isCompanyWiseDisc);

                        productBo.setProductDiscAmount(productBo.getProductDiscAmount() + discountValue);


                        if (productBo.getDiscount_order_value() > 0) {
                            productBo.setDiscount_order_value(productBo
                                    .getDiscount_order_value() - discountValue);
                        }

                        totalDiscountValue = totalDiscountValue + discountValue;

                    }
                }
            }
        }

        return totalDiscountValue;

    }


    /////////////////////// Bill Wise Discount  ///////////////////////

    public void downloadBillwiseDiscount() {

        try {

            StoreWsieDiscountBO discountbo;
            mBillWiseDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c ;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.schemeDetailsMasterHelper.getChannelidForScheme(businessModel.getRetailerMasterBO().getSubchannelid()) + ") OR ");
            sb.append(" locationid in(" + businessModel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ") OR ");
            sb.append(" Accountid =" + businessModel.getRetailerMasterBO().getAccountid() + " and Accountid!=0 ))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid not in (select ListId from StandardListMaster where ListCode='PAYTERM')");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountbo = new StoreWsieDiscountBO();
                    discountbo.setDiscount(c.getDouble(0));
                    discountbo.setIsPercentage(c.getInt(1));
                    discountbo.setType(c.getInt(2));
                    discountbo.setDescription(c.getString(3));
                    discountbo.setApplyLevel(c.getInt(4));
                    discountbo.setModule(c.getInt(5));
                    discountbo.setProductId(c.getInt(6));
                    discountbo.setDiscountId(c.getInt(7));
                    discountbo.setIsCompanyGiven(c.getInt(8));
                    discountbo.setToDiscount(c.getDouble(9));
                    discountbo.setMinAmount(c.getDouble(10));
                    discountbo.setMaxAmount(c.getDouble(11));
                    mBillWiseDiscountList.add(discountbo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public ArrayList<StoreWsieDiscountBO> getBillWiseDiscountList() {
        if (mBillWiseDiscountList != null)
            return mBillWiseDiscountList;
        return new ArrayList<>();
    }

    /**
     * Method to use get total value after applying range wise bill discount
     *
     * @param totalOrderValue
     * @return
     */
    public double updateBillwiseRangeDiscount(double totalOrderValue) {
        double discountValue = 0;
        if (mBillWiseDiscountList != null) {
            for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                if (totalOrderValue >= storeWsieDiscountBO.getMinAmount() && totalOrderValue <= storeWsieDiscountBO.getMaxAmount()) {
                    if (storeWsieDiscountBO.getIsPercentage() == 1) {
                        discountValue = (totalOrderValue * storeWsieDiscountBO.getAppliedDiscount() / 100);

                    } else if (storeWsieDiscountBO.getIsPercentage() == 0) {
                        discountValue = storeWsieDiscountBO.getAppliedDiscount();
                    }
                    storeWsieDiscountBO.setDiscountValue(discountValue);

                    businessModel.getOrderHeaderBO().setDiscountValue(discountValue);
                    businessModel.getOrderHeaderBO().setDiscount(storeWsieDiscountBO.getDiscount());
                    businessModel.getOrderHeaderBO().setDiscountId(storeWsieDiscountBO.getDiscountId());
                    businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWsieDiscountBO.getIsCompanyGiven());
                    break;
                }
            }

        }
        return discountValue;


    }


    public void updateRangeWiseBillDiscountFromDB() {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        sb.append("select value,Percentage,discountid from invoicediscountdetail id ");
        sb.append(" inner join orderHeader od on id.orderid=od.orderid  ");
        sb.append(" where  id.retailerid=" + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
        sb.append(" and invoicestatus=0 and id.upload='N'");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                final double value = c.getDouble(0);
                final double percentage = c.getDouble(1);
                final int discountid = c.getInt(2);
                if (mBillWiseDiscountList != null) {
                    for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                        if (storeWsieDiscountBO.getDiscountId() == discountid) {
                            storeWsieDiscountBO.setApplied(true);
                            if (value > 0) {
                                storeWsieDiscountBO.setAppliedDiscount(value);
                            } else if (percentage > 0) {
                                storeWsieDiscountBO.setAppliedDiscount(percentage);
                            }
                            break;
                        }

                    }
                }
            }
        }
        c.close();
        db.closeDB();
    }

    public double updateBillwiseDiscount(double totalOrderValue) {
        double totalValue = totalOrderValue;
        double totalBillwiseDiscountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWiseDiscountList != null && mBillWiseDiscountList.size() > 0) {
            for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                if (storeWsieDiscountBO.getIsCompanyGiven() == 1) {
                    totalOrderValue = totalValue - billWiseDistributorDiscount;
                }
                double discountValue = 0;
                if (storeWsieDiscountBO.getIsPercentage() == 1) {
                    discountValue = totalOrderValue * storeWsieDiscountBO.getDiscount() / 100;
                } else if (storeWsieDiscountBO.getType() == 0) {
                    discountValue = storeWsieDiscountBO.getDiscount();
                }

                storeWsieDiscountBO.setDiscountValue(discountValue);
                if (storeWsieDiscountBO.getIsCompanyGiven() == 1) {
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                } else {
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;
                }

                totalBillwiseDiscountValue = totalBillwiseDiscountValue + discountValue;
            }

        }
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);


        return totalBillwiseDiscountValue;
    }


    public void updateMinimumRangeAsBillwiseDisc() {
        if (mBillWiseDiscountList != null) {
            for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                storeWsieDiscountBO.setAppliedDiscount(storeWsieDiscountBO.getDiscount());
                storeWsieDiscountBO.setApplied(false);
            }
        }
    }


    public void insertBillWiseDisc(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        for (StoreWsieDiscountBO discountBO : mBillWiseDiscountList) {
            StringBuffer sb = new StringBuffer();
            sb.append(uid + "," + "0," + discountBO.getType() + ",");
            if (discountBO.getIsPercentage() == 1) {
                sb.append(discountBO.getDiscountValue() + "," + discountBO.getDiscount());
            } else {
                sb.append(discountBO.getDiscountValue() + ",0");
            }

            sb.append("," + discountBO.getApplyLevel() + "," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) + "," + discountBO.getDiscountId() + "," + discountBO.getIsCompanyGiven());
            db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
            db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());
        }


    }

    /**
     * save bill wise discount by range wise
     *
     * @param orderid
     * @param db
     */
    public void saveBillWiseDiscountRangewise(String orderid, DBUtil db) {
        if (mBillWiseDiscountList != null) {
            String columns = "OrderId,Typeid,Value,Percentage,ApplyLevelid,RetailerId,discountid,isCompanyGiven,pid";
            for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                if (storeWsieDiscountBO.isApplied()) {
                    double value = 0;
                    double percentage = 0;
                    if (storeWsieDiscountBO.getIsPercentage() == 1) {
                        percentage = storeWsieDiscountBO.getAppliedDiscount();
                    } else {
                        value = storeWsieDiscountBO.getAppliedDiscount();
                    }
                    StringBuffer sb = new StringBuffer();
                    sb.append(orderid + "," + storeWsieDiscountBO.getType() + "," + value + "," + percentage);
                    sb.append("," + percentage + storeWsieDiscountBO.getApplyLevel());
                    sb.append("," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                    sb.append("," + storeWsieDiscountBO.getDiscountId());
                    sb.append("," + storeWsieDiscountBO.getIsCompanyGiven());
                    sb.append(",0");
                    db.insertSQL("InvoiceDiscountDetail", columns,
                            sb.toString());
                    db.insertSQL("OrderDiscountDetail", columns,
                            sb.toString());
                    break;

                }

            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////// Pay term discount /////////////////////////////////

    public void downloadBillwisePaytermDiscount() {

        try {

            StoreWsieDiscountBO discountbo;
            mBillWisePayternDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c ;

            StringBuffer sb = new StringBuffer();
            sb.append("select distinct Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.schemeDetailsMasterHelper.getChannelidForScheme(businessModel.getRetailerMasterBO().getSubchannelid()) + ") OR ");
            sb.append(" locationid in(" + businessModel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ") OR ");
            sb.append(" Accountid =" + businessModel.getRetailerMasterBO().getAccountid() + "))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid in(select ListId from StandardListMaster where ListCode='PAYTERM') ");
            sb.append(" and " + businessModel.getRetailerMasterBO().getCreditDays() + " between minvalue and maxvalue");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountbo = new StoreWsieDiscountBO();
                    discountbo.setDiscount(c.getDouble(0));
                    discountbo.setIsPercentage(c.getInt(1));
                    discountbo.setType(c.getInt(2));
                    discountbo.setDescription(c.getString(3));
                    discountbo.setApplyLevel(c.getInt(4));
                    discountbo.setModule(c.getInt(5));
                    discountbo.setProductId(c.getInt(6));
                    discountbo.setDiscountId(c.getInt(7));
                    discountbo.setIsCompanyGiven(c.getInt(8));
                    discountbo.setToDiscount(c.getDouble(9));
                    discountbo.setMinAmount(c.getDouble(10));
                    discountbo.setMaxAmount(c.getDouble(11));
                    mBillWisePayternDiscountList.add(discountbo);
                }
                c.close();
            }
            db.closeDB();
            if (mBillWisePayternDiscountList.size() == 0)
                downloadRetailerBillwisePaytermDiscount();
        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public void downloadRetailerBillwisePaytermDiscount() {

        try {

            StoreWsieDiscountBO discountbo;
            mBillWisePayternDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            int applyLevelID = 0;
            Cursor c ;


            c = db.selectSQL("select ListId from StandardListMaster where ListCode='BILL' and ListType='DISCOUNT_APPLY_TYPE'");
            if (c != null) {
                while (c.moveToNext()) {
                    applyLevelID = c.getInt(0);
                }
                c.close();
            }

            StringBuffer sb = new StringBuffer();
            sb.append("select Percentage,DiscountTypeID from PayTermDiscount ");
            sb.append("where Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID());
            sb.append(" and " + businessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " between FromDate and ToDate");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountbo = new StoreWsieDiscountBO();
                    discountbo.setDiscount(c.getDouble(0));
                    discountbo.setType(c.getInt(1));
                    discountbo.setIsPercentage(1);
                    discountbo.setDescription("Pay Term");
                    discountbo.setApplyLevel(applyLevelID);
                    discountbo.setModule(0);
                    discountbo.setProductId(0);
                    discountbo.setDiscountId(0);
                    discountbo.setIsCompanyGiven(0);
                    discountbo.setToDiscount(0);
                    discountbo.setMinAmount(0);
                    discountbo.setMaxAmount(0);
                    mBillWisePayternDiscountList.add(discountbo);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public double calculateBillWisePayTermDiscount(double totalOrderValue) {


        double totalValue = totalOrderValue;
        double discountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWisePayternDiscountList != null) {
            for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWisePayternDiscountList) {
                if (storeWsieDiscountBO.getIsCompanyGiven() == 0) {

                    totalOrderValue = totalValue - billWiseCompanyDiscount;
                }
                if (storeWsieDiscountBO.getIsPercentage() == 1) {
                    discountValue = (totalOrderValue * storeWsieDiscountBO.getDiscount() / 100);

                } else if (storeWsieDiscountBO.getIsPercentage() == 0) {
                    discountValue = storeWsieDiscountBO.getDiscount();
                }

                if (storeWsieDiscountBO.getIsCompanyGiven() == 1)
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                else
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;


                storeWsieDiscountBO.setDiscountValue(discountValue);

                businessModel.getOrderHeaderBO().setDiscountValue(discountValue);
                businessModel.getOrderHeaderBO().setDiscount(storeWsieDiscountBO.getDiscount());
                businessModel.getOrderHeaderBO().setDiscountId(storeWsieDiscountBO.getDiscountId());
                businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWsieDiscountBO.getIsCompanyGiven());
                break;

            }

        }
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);
        return discountValue;


    }

    public void insertBillWisePaytermDisc(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        if (mBillWisePayternDiscountList != null) {
            for (StoreWsieDiscountBO discountBO : mBillWisePayternDiscountList) {
                StringBuffer sb = new StringBuffer();
                sb.append(uid + "," + "0," + discountBO.getType() + ",");
                if (discountBO.getIsPercentage() == 1) {
                    sb.append(discountBO.getDiscountValue() + "," + discountBO.getDiscount());
                } else {
                    sb.append(discountBO.getDiscountValue() + ",0");
                }

                sb.append("," + discountBO.getApplyLevel() + "," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()) + "," + discountBO.getDiscountId() + "," + discountBO.getIsCompanyGiven());
                db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
                db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());
            }
        }


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////

}
