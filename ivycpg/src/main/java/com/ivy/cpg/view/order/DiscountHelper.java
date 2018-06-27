package com.ivy.cpg.view.order;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StoreWiseDiscountBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rajkumar on 29/1/18.
 * Discount related method implementations
 */

public class DiscountHelper {

    private static DiscountHelper instance = null;
    private BusinessModel businessModel;
    private OrderHelper orderHelper;

    //Product Id is appended with batch Id in case of base available
    private HashMap<String, HashMap<Integer, Double>> mDiscountListByProductId = new HashMap<>();
    private ArrayList<StoreWiseDiscountBO> mBillWiseDiscountList;
    private ArrayList<StoreWiseDiscountBO> mBillWisePaytTermDiscountList;
    private ArrayList<StoreWiseDiscountBO> mBillWiseWithHoldDiscountList;

    private DiscountHelper(Context context) {
        this.businessModel = (BusinessModel) context;
        orderHelper = OrderHelper.getInstance(context);
    }


    public static DiscountHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DiscountHelper(context);
        }
        return instance;
    }


    public HashMap<String, HashMap<Integer, Double>> getDiscountListByProductId() {
        return mDiscountListByProductId;
    }


    public ArrayList<StoreWiseDiscountBO> getBillWiseDiscountList() {
        if (mBillWiseDiscountList != null)
            return mBillWiseDiscountList;
        return new ArrayList<>();
    }


    /**
     * Calculate user entry level discount and update product line value
     *
     * @param orderedList Ordered product List
     * @return Total discount value of ordered products
     */
    public double calculateUserEntryLevelDiscount(List<ProductMasterBO> orderedList) {

        mDiscountListByProductId = new HashMap<>();
        double totalDiscountValue = 0;

        if (orderedList != null) {

            for (ProductMasterBO productBO : orderedList) {

                double discountValue = 0.0;
                double totalEnteredDiscount = productBO.getD1()
                        + productBO.getD2() + productBO.getD3();
                boolean isBatchWise = false;

                if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                        && businessModel.configurationMasterHelper.IS_INVOICE
                        && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    if (productBO.getBatchwiseProductCount() > 0)
                        isBatchWise = true;

                } else {
                    isBatchWise = false;
                }

                if (totalEnteredDiscount > 0) {
                    discountValue = calculateProductWiseDiscount(productBO,
                            totalEnteredDiscount, 1, isBatchWise, 0, true);

                } else if (productBO.getDA() > 0) {
                    discountValue = calculateProductWiseDiscount(productBO,
                            productBO.getDA(), 0, isBatchWise, 0, true);

                }

                if (productBO.getDiscount_order_value() > 0) {
                    productBO.setDiscount_order_value(productBO.getDiscount_order_value() - discountValue);
                }

                totalDiscountValue = totalDiscountValue + discountValue;
            }
        }
        return totalDiscountValue;

    }

    /**
     * Calculate discount value for given product(Used from both user entry level and item level discount types)
     *
     * @param productBO         Product object
     * @param value             discount
     * @param discOrAmt         if 1 - percentage,0 - amount based discount
     * @param isBatchWise       Is batchWise product
     * @param discountId        Discount Id
     * @param isCompanyDiscount Is company discount
     * @return total product discount value
     */
    private double calculateProductWiseDiscount(ProductMasterBO productBO,
                                                double value, int discOrAmt, boolean isBatchWise, int discountId, boolean isCompanyDiscount) {
        double totalDiscOrAmtValue = 0;

        if (isBatchWise) {
            totalDiscOrAmtValue = calculateProductDiscountBatchWise(productBO,
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
                totalValue = orderHelper
                        .getTotalValueOfAllBatches(productBO);
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

            HashMap<Integer, Double> discountValueByDiscountId = mDiscountListByProductId.get(productBO.getProductID());
            if (discountValueByDiscountId == null)
                discountValueByDiscountId = new HashMap<>();

            discountValueByDiscountId.put(discountId, totalDiscOrAmtValue);

            mDiscountListByProductId.put(productBO.getProductID(), discountValueByDiscountId);

        }

        return totalDiscOrAmtValue;
    }


    /**
     * Calculate product discount value batch wise
     *
     * @param productBO  Product for which discount value to be calculated
     * @param value      Discount
     * @param discOrAmt  if 1 - percentage,0 - amount based discount
     * @param discountId Discount Id
     * @return Discount value for given product
     */
    private double calculateProductDiscountBatchWise(
            ProductMasterBO productBO, double value, int discOrAmt, int discountId) {


        double totalProductDisOrAmtValue = 0.0;
        ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        if (batchList != null) {
            for (ProductMasterBO batchProductBo : batchList) {
                double totalDiscountValue = 0;
                int totalQty = batchProductBo.getOrderedPcsQty()
                        + batchProductBo.getOrderedCaseQty()
                        * productBO.getCaseSize()
                        + batchProductBo.getOrderedOuterQty()
                        * productBO.getOutersize();
                if (totalQty > 0) {
                    double totalValue;
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
                        totalDiscountValue = totalValue * value / 100;
                    } else if (discOrAmt == 0) {
                        totalDiscountValue = totalValue - (totalQty * (batchProductBo.getSrp() - value));

                    }
                    String productWithBatchId = batchProductBo.getProductID() + batchProductBo.getBatchid();
                    HashMap<Integer, Double> discountValueByDiscountId = mDiscountListByProductId.get(productWithBatchId);
                    if (discountValueByDiscountId == null)
                        discountValueByDiscountId = new HashMap<>();
                    batchProductBo.setProductDiscAmount(batchProductBo.getProductDiscAmount() + totalDiscountValue);


                    discountValueByDiscountId.put(discountId, totalDiscountValue);

                    mDiscountListByProductId.put(productWithBatchId, discountValueByDiscountId);


                    if (batchProductBo.getDiscount_order_value() > 0) {
                        batchProductBo.setDiscount_order_value(batchProductBo
                                .getDiscount_order_value()
                                - totalDiscountValue);
                    }

                    if (discountId == 0) {
                        batchProductBo.setApplyValue(totalDiscountValue);
                    }


                    totalProductDisOrAmtValue = totalProductDisOrAmtValue
                            + totalDiscountValue;

                }
            }
        }
        return totalProductDisOrAmtValue;

    }


    /**
     * Calculating item level discount. This discount not depends on user data completely.
     *
     * @return Discount value
     */
    public double calculateItemLevelDiscount() {

        double totalDiscountValue = 0.0;

        if (businessModel.productHelper.getDiscountIdList() != null) {
            for (Integer discountID : businessModel.productHelper.getDiscountIdList()) {

                ArrayList<StoreWiseDiscountBO> discountProductIdList = businessModel.productHelper.getProductDiscountListByDiscountID()
                        .get(discountID);
                if (discountProductIdList != null) {
                    for (StoreWiseDiscountBO storeWiseDiscountBO : discountProductIdList) {

                        ProductMasterBO productBo = businessModel.productHelper
                                .getProductMasterBOById(String.valueOf(storeWiseDiscountBO
                                        .getProductId()));

                        if (productBo != null) {
                            if (productBo.getOrderedPcsQty() > 0
                                    || productBo.getOrderedCaseQty() > 0
                                    || productBo.getOrderedOuterQty() > 0) {

                                boolean isBatchWise = false;

                                int percentageORAmountDiscount = storeWiseDiscountBO
                                        .getIsPercentage() == 1 ? 1 : 0;

                                if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                        && businessModel.configurationMasterHelper.IS_INVOICE
                                        && businessModel.configurationMasterHelper.IS_SIH_VALIDATION) {

                                    if (productBo.getBatchwiseProductCount() > 0)
                                        isBatchWise = true;


                                } else {
                                    isBatchWise = false;
                                }

                                boolean isCompanyWiseDisc = false;
                                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                                    isCompanyWiseDisc = true;
                                }


                                final double discountValue = calculateProductWiseDiscount(productBo,
                                        storeWiseDiscountBO.getDiscount(), percentageORAmountDiscount,
                                        isBatchWise, discountID, isCompanyWiseDisc);

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

            }
        }

        return totalDiscountValue;
    }


    /**
     * download bill wise discount
     *
     * @param mContext current context
     */
    public void downloadBillWiseDiscount(Context mContext) {

        try {

            StoreWiseDiscountBO discountBO;
            mBillWiseDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" distributorid=" + businessModel.getRetailerMasterBO().getDistributorId() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.channelMasterHelper.getChannelHierarchyForDiscount(businessModel.getRetailerMasterBO().getSubchannelid(), mContext) + ") OR ");
            sb.append(" locationid in(" + businessModel.channelMasterHelper.getLocationHierarchy(mContext) + ") OR ");
            sb.append(" Accountid =" + businessModel.getRetailerMasterBO().getAccountid() + " and Accountid!=0 ))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid not in (select ListId from StandardListMaster where ListCode='PAYTERM')");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountBO = new StoreWiseDiscountBO();
                    discountBO.setDiscount(c.getDouble(0));
                    discountBO.setIsPercentage(c.getInt(1));
                    discountBO.setType(c.getInt(2));
                    discountBO.setDescription(c.getString(3));
                    discountBO.setApplyLevel(c.getInt(4));
                    discountBO.setModule(c.getInt(5));
                    discountBO.setProductId(c.getInt(6));
                    discountBO.setDiscountId(c.getInt(7));
                    discountBO.setIsCompanyGiven(c.getInt(8));
                    discountBO.setToDiscount(c.getDouble(9));
                    discountBO.setMinAmount(c.getDouble(10));
                    discountBO.setMaxAmount(c.getDouble(11));
                    mBillWiseDiscountList.add(discountBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }


    }


    /**
     * Method to use get total value after applying range wise bill discount
     *
     * @param totalOrderValue total order value
     * @return total discount
     */
    public double calculateBillWiseRangeDiscount(double totalOrderValue) {
        double discountValue = 0;
        if (mBillWiseDiscountList != null) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                if (totalOrderValue >= storeWiseDiscountBO.getMinAmount() && totalOrderValue <= storeWiseDiscountBO.getMaxAmount()) {
                    if (storeWiseDiscountBO.getIsPercentage() == 1) {
                        discountValue = (totalOrderValue * storeWiseDiscountBO.getAppliedDiscount() / 100);

                    } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                        discountValue = storeWiseDiscountBO.getAppliedDiscount();
                    }
                    storeWiseDiscountBO.setDiscountValue(discountValue);

                    businessModel.getOrderHeaderBO().setDiscountValue(discountValue);
                    businessModel.getOrderHeaderBO().setDiscount(storeWiseDiscountBO.getDiscount());
                    businessModel.getOrderHeaderBO().setDiscountId(storeWiseDiscountBO.getDiscountId());
                    businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWiseDiscountBO.getIsCompanyGiven());
                    break;
                }
            }

        }
        return discountValue;


    }


    public void loadExistingBillWiseRangeDiscount(Context mContext) {

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
                final int discountId = c.getInt(2);
                if (mBillWiseDiscountList != null) {
                    for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                        if (storeWiseDiscountBO.getDiscountId() == discountId) {
                            storeWiseDiscountBO.setApplied(true);
                            if (value > 0) {
                                storeWiseDiscountBO.setAppliedDiscount(value);
                            } else if (percentage > 0) {
                                storeWiseDiscountBO.setAppliedDiscount(percentage);
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

    public double calculateBillWiseDiscount(double totalOrderValue) {
        double totalValue = totalOrderValue;
        double totalBillWiseDiscountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWiseDiscountList != null && mBillWiseDiscountList.size() > 0) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    totalOrderValue = totalValue - billWiseDistributorDiscount;
                }
                double discountValue = 0;
                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                    discountValue = totalOrderValue * storeWiseDiscountBO.getDiscount() / 100;
                } else {
                    //Rajkumar - Type id is coming for Bill wise discount also..
                    // So If it is not percentage type discount, then it is considered as amount type discount.
                    discountValue = storeWiseDiscountBO.getDiscount();
                }

                storeWiseDiscountBO.setDiscountValue(discountValue);
                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                } else {
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;
                }

                totalBillWiseDiscountValue = totalBillWiseDiscountValue + discountValue;
            }

        }
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);


        return totalBillWiseDiscountValue;
    }


    public void setMinimumRangeAsBillWiseDiscount() {
        if (mBillWiseDiscountList != null) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                storeWiseDiscountBO.setAppliedDiscount(storeWiseDiscountBO.getDiscount());
                storeWiseDiscountBO.setApplied(false);
            }
        }
    }


    public double calculateWithHoldDiscount(double totalOrderValue) {
        double totalValue = totalOrderValue;
        double totalBillWiseDiscountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWiseWithHoldDiscountList != null && mBillWiseWithHoldDiscountList.size() > 0) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseWithHoldDiscountList) {
                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    totalOrderValue = totalValue - billWiseDistributorDiscount;
                }
                double discountValue = 0;
                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                    discountValue = totalOrderValue * storeWiseDiscountBO.getDiscount() / 100;
                } else if (storeWiseDiscountBO.getType() == 0) {
                    discountValue = storeWiseDiscountBO.getDiscount();
                }

                storeWiseDiscountBO.setDiscountValue(discountValue);
                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                } else {
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;
                }

                totalBillWiseDiscountValue = totalBillWiseDiscountValue + discountValue;
            }

        }


        return totalBillWiseDiscountValue;
    }

    /**
     * Save bill wise discount
     *
     * @param db  database objects
     * @param uid transaction Id
     */
    public void insertBillWiseDiscount(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        for (StoreWiseDiscountBO discountBO : mBillWiseDiscountList) {
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
     * Save bill wise with hold discount
     *
     * @param db  database objects
     * @param uid transaction Id
     */
    public void insertWithHoldDiscount(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        for (StoreWiseDiscountBO discountBO : mBillWiseWithHoldDiscountList) {
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
     * @param orderId order Id
     * @param db      database object
     */
    public void saveBillWiseDiscountRangeWise(String orderId, DBUtil db) {
        if (mBillWiseDiscountList != null) {
            String columns = "OrderId,Typeid,Value,Percentage,ApplyLevelid,RetailerId,discountid,isCompanyGiven,pid";
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                if (storeWiseDiscountBO.isApplied()) {
                    double value = 0;
                    double percentage = 0;
                    if (storeWiseDiscountBO.getIsPercentage() == 1) {
                        percentage = storeWiseDiscountBO.getAppliedDiscount();
                    } else {
                        value = storeWiseDiscountBO.getAppliedDiscount();
                    }
                    StringBuffer sb = new StringBuffer();
                    sb.append(orderId + "," + storeWiseDiscountBO.getType() + "," + value + "," + percentage);
                    sb.append("," + percentage + storeWiseDiscountBO.getApplyLevel());
                    sb.append("," + businessModel.QT(businessModel.getRetailerMasterBO().getRetailerID()));
                    sb.append("," + storeWiseDiscountBO.getDiscountId());
                    sb.append("," + storeWiseDiscountBO.getIsCompanyGiven());
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


    /**
     * Download bill wise payTerm discount
     *
     * @param mContext current context
     */
    public void downloadBillWisePayTermDiscount(Context mContext) {

        try {

            StoreWiseDiscountBO discountBO;
            mBillWisePaytTermDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c;

            StringBuffer sb = new StringBuffer();
            sb.append("select distinct Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.channelMasterHelper.getChannelHierarchy(businessModel.getRetailerMasterBO().getSubchannelid(), mContext) + ") OR ");
            sb.append(" locationid in(" + businessModel.channelMasterHelper.getLocationHierarchy(mContext) + ") OR ");
            sb.append(" Accountid =" + businessModel.getRetailerMasterBO().getAccountid() + "))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid in(select ListId from StandardListMaster where ListCode='PAYTERM') ");
            sb.append(" and " + businessModel.getRetailerMasterBO().getCreditDays() + " between minvalue and maxvalue");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountBO = new StoreWiseDiscountBO();
                    discountBO.setDiscount(c.getDouble(0));
                    discountBO.setIsPercentage(c.getInt(1));
                    discountBO.setType(c.getInt(2));
                    discountBO.setDescription(c.getString(3));
                    discountBO.setApplyLevel(c.getInt(4));
                    discountBO.setModule(c.getInt(5));
                    discountBO.setProductId(c.getInt(6));
                    discountBO.setDiscountId(c.getInt(7));
                    discountBO.setIsCompanyGiven(c.getInt(8));
                    discountBO.setToDiscount(c.getDouble(9));
                    discountBO.setMinAmount(c.getDouble(10));
                    discountBO.setMaxAmount(c.getDouble(11));
                    mBillWisePaytTermDiscountList.add(discountBO);
                }
                c.close();
            }
            db.closeDB();
            if (mBillWisePaytTermDiscountList.size() == 0)
                downloadRetailerBillWisePayTermDiscount(mContext);
        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public ArrayList<StoreWiseDiscountBO> getBillWisePayternDiscountList() {
        return mBillWisePaytTermDiscountList;
    }


    public ArrayList<StoreWiseDiscountBO> getBillWiseWithHoldDiscountList() {
        return mBillWiseWithHoldDiscountList;
    }

    /**
     * Download bill wise with hold discount
     *
     * @param mContext current context
     */
    public void downloadBillWiseWithHoldDiscount(Context mContext) {

        try {

            StoreWiseDiscountBO discountBO;
            mBillWiseWithHoldDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.channelMasterHelper.getChannelHierarchy(businessModel.getRetailerMasterBO().getSubchannelid(),mContext) + ") OR ");
            sb.append(" locationid in(" + businessModel.channelMasterHelper.getLocationHierarchy(mContext) + ") OR ");
            sb.append(" Accountid =" + businessModel.getRetailerMasterBO().getAccountid() + " and Accountid!=0 ))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid in (select ListId from StandardListMaster where ListCode='WHT')");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    discountBO = new StoreWiseDiscountBO();
                    discountBO.setDiscount(c.getDouble(0));
                    discountBO.setIsPercentage(c.getInt(1));
                    discountBO.setType(c.getInt(2));
                    discountBO.setDescription(c.getString(3));
                    discountBO.setApplyLevel(c.getInt(4));
                    discountBO.setModule(c.getInt(5));
                    discountBO.setProductId(c.getInt(6));
                    discountBO.setDiscountId(c.getInt(7));
                    discountBO.setIsCompanyGiven(c.getInt(8));
                    discountBO.setToDiscount(c.getDouble(9));
                    discountBO.setMinAmount(c.getDouble(10));
                    discountBO.setMaxAmount(c.getDouble(11));
                    mBillWiseWithHoldDiscountList.add(discountBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }


    }



    /**
     * download pay term discount for current retailer
     *
     * @param mContext current context
     */
    private void downloadRetailerBillWisePayTermDiscount(Context mContext) {

        try {

            StoreWiseDiscountBO discountBO;
            mBillWisePaytTermDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            int applyLevelID = 0;
            Cursor c;


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
                    discountBO = new StoreWiseDiscountBO();
                    discountBO.setDiscount(c.getDouble(0));
                    discountBO.setType(c.getInt(1));
                    discountBO.setIsPercentage(1);
                    discountBO.setDescription("Pay Term");
                    discountBO.setApplyLevel(applyLevelID);
                    discountBO.setModule(0);
                    discountBO.setProductId(0);
                    discountBO.setDiscountId(0);
                    discountBO.setIsCompanyGiven(0);
                    discountBO.setToDiscount(0);
                    discountBO.setMinAmount(0);
                    discountBO.setMaxAmount(0);
                    mBillWisePaytTermDiscountList.add(discountBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    /**
     * calculate bill wise payTerm discount
     *
     * @param totalOrderValue Total order value
     * @return discount value
     */
    public double calculateBillWisePayTermDiscount(double totalOrderValue) {


        double totalValue = totalOrderValue;
        double discountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWisePaytTermDiscountList != null) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWisePaytTermDiscountList) {
                if (storeWiseDiscountBO.getIsCompanyGiven() == 0) {

                    totalOrderValue = totalValue - billWiseCompanyDiscount;
                }
                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                    discountValue = (totalOrderValue * storeWiseDiscountBO.getDiscount() / 100);

                } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                    discountValue = storeWiseDiscountBO.getDiscount();
                }

                if (storeWiseDiscountBO.getIsCompanyGiven() == 1)
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                else
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;


                storeWiseDiscountBO.setDiscountValue(discountValue);

                businessModel.getOrderHeaderBO().setDiscountValue(discountValue);
                businessModel.getOrderHeaderBO().setDiscount(storeWiseDiscountBO.getDiscount());
                businessModel.getOrderHeaderBO().setDiscountId(storeWiseDiscountBO.getDiscountId());
                businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWiseDiscountBO.getIsCompanyGiven());
                break;

            }

        }
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);
        return discountValue;


    }

    /**
     * Insert bill wise pay term discount
     *
     * @param db  database object
     * @param uid transaction id
     */
    public void insertBillWisePayTermDisc(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        if (mBillWisePaytTermDiscountList != null) {
            for (StoreWiseDiscountBO discountBO : mBillWisePaytTermDiscountList) {
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


    /**
     * Calculating scheme discounts for applied(Scheme selected in scheme apply screen) scheme
     * and updating those values in product object.
     * If Free products available then it will be added in any one of the buy product to show in print.
     */
    public double calculateSchemeDiscounts(LinkedList<ProductMasterBO> mOrderedList, Context mContext) {

        double totalSchemeDiscountValue = 0;
        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(mContext);

        ArrayList<SchemeBO> appliedSchemeList = schemeHelper.getAppliedSchemeList();
        if (appliedSchemeList != null) {

            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {
                    if (schemeBO.isAmountTypeSelected()) {
                        totalSchemeDiscountValue += schemeBO.getSelectedAmount();
                    }

                    List<SchemeProductBO> schemeProductList = schemeBO
                            .getBuyingProducts();
                    int i = 0;
                    boolean isBuyProductAvailable = false;
                    if (schemeProductList != null) {

                        // Getting total order value of buy products
                        double totalOrderValueOfBuyProducts = 0;
                        if (schemeBO.isAmountTypeSelected()) {
                            for (SchemeProductBO schemeProductBo : schemeProductList) {
                                ProductMasterBO productBO = businessModel.productHelper
                                        .getProductMasterBOById(schemeProductBo
                                                .getProductId());
                                totalOrderValueOfBuyProducts += (productBO.getOrderedCaseQty() * productBO.getCsrp())
                                        + (productBO.getOrderedPcsQty() * productBO.getSrp())
                                        + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                            }
                        }
                        //

                        ArrayList<String> productIdList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeProductList) {
                            ProductMasterBO productBO = businessModel.productHelper
                                    .getProductMasterBOById(schemeProductBo
                                            .getProductId());
                            if (productBO != null) {
                                if (!productIdList.contains(productBO.getProductID())) {
                                    productIdList.add(productBO.getProductID());
                                    i = i++;
                                    if (productBO.getOrderedPcsQty() > 0
                                            || productBO.getOrderedCaseQty() > 0
                                            || productBO.getOrderedOuterQty() > 0) {
                                        isBuyProductAvailable = true;
                                        if (schemeBO.isAmountTypeSelected()) {
                                            schemeProductBo.setDiscountValue(schemeBO.getSelectedAmount());

                                            // calculating free amount for current product by contribution to total value of buy products
                                            double line_value = (productBO.getOrderedCaseQty() * productBO.getCsrp())
                                                    + (productBO.getOrderedPcsQty() * productBO.getSrp())
                                                    + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                                            double percentage_productContribution = ((line_value / totalOrderValueOfBuyProducts) * 100);
                                            double amount_free = schemeBO.getSelectedAmount() * (percentage_productContribution / 100);
                                            //

                                            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                    && businessModel.configurationMasterHelper.IS_SIH_VALIDATION
                                                    && businessModel.configurationMasterHelper.IS_INVOICE) {
                                                if (productBO
                                                        .getBatchwiseProductCount() > 0) {
                                                    ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                                                    if (batchList != null && !batchList.isEmpty()) {

                                                        // To get total order value of batch buy products
                                                        double totalOrderValueOfBuyProducts_batch = 0;
                                                        for (ProductMasterBO batchProduct : batchList) {
                                                            totalOrderValueOfBuyProducts_batch += (batchProduct.getOrderedCaseQty() * productBO.getCsrp())
                                                                    + (batchProduct.getOrderedPcsQty() * productBO.getSrp())
                                                                    + (batchProduct.getOrderedOuterQty() * productBO.getOsrp());
                                                        }
                                                        //

                                                        for (ProductMasterBO batchProduct : batchList) {
                                                            int totalQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * productBO.getCaseSize())
                                                                    + (batchProduct.getOrderedOuterQty() * productBO.getOutersize());
                                                            if (totalQty > 0) {

                                                                // calculating free amount for current batch product(by contribution to total value(Sum of all line value of batches in a product)).
                                                                double line_value_batch = (batchProduct.getOrderedCaseQty() * productBO.getCsrp())
                                                                        + (batchProduct.getOrderedPcsQty() * productBO.getSrp())
                                                                        + (batchProduct.getOrderedOuterQty() * productBO.getOsrp());
                                                                double percentage_batchProductContribution = ((line_value_batch / totalOrderValueOfBuyProducts_batch) * 100);
                                                                double amount_free_batch = amount_free * (percentage_batchProductContribution / 100);
                                                                //

                                                                batchProduct.setSchemeDiscAmount(batchProduct.getSchemeDiscAmount() + amount_free_batch);

                                                            }
                                                        }
                                                    }
                                                } else {
                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                    if (productBO.getDiscount_order_value() > 0) {
                                                        productBO.setDiscount_order_value(productBO
                                                                .getDiscount_order_value()
                                                                - amount_free);

                                                    }
                                                }
                                            } else {
                                                productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                if (productBO.getDiscount_order_value() > 0) {
                                                    productBO.setDiscount_order_value(productBO
                                                            .getDiscount_order_value()
                                                            - amount_free);

                                                }
                                            }
                                        } else if (schemeBO.isPriceTypeSeleted()) {
                                            double totalPriceDiscount;

                                            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                    && businessModel.configurationMasterHelper.IS_SIH_VALIDATION
                                                    && businessModel.configurationMasterHelper.IS_INVOICE) {
                                                if (productBO
                                                        .getBatchwiseProductCount() > 0) {
                                                    totalPriceDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrice(),
                                                                    "SCH_PR", true);
                                                } else {
                                                    totalPriceDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrice(),
                                                                    "SCH_PR", false);
                                                }

                                            } else {
                                                totalPriceDiscount = schemeHelper
                                                        .calculateDiscountValue(
                                                                productBO,
                                                                schemeBO.getSelectedPrice(),
                                                                "SCH_PR", false);
                                            }

                                            if (productBO.getDiscount_order_value() > 0) {
                                                productBO
                                                        .setDiscount_order_value(productBO
                                                                .getDiscount_order_value()
                                                                - totalPriceDiscount);

                                            }
                                            if (productBO.getSchemeAppliedValue() > 0) {
                                                productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalPriceDiscount);
                                            }

                                            schemeProductBo.setDiscountValue(totalPriceDiscount);

                                            totalSchemeDiscountValue += totalPriceDiscount;


                                        } else if (schemeBO
                                                .isDiscountPrecentSelected()) {
                                            double totalPercentageDiscount;
                                            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                    && businessModel.configurationMasterHelper.IS_SIH_VALIDATION
                                                    && businessModel.configurationMasterHelper.IS_INVOICE) {
                                                if (productBO
                                                        .getBatchwiseProductCount() > 0) {
                                                    totalPercentageDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrecent(),
                                                                    "SCH_PER", true);
                                                } else {
                                                    totalPercentageDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrecent(),
                                                                    "SCH_PER",
                                                                    false);
                                                }
                                            } else {
                                                totalPercentageDiscount = schemeHelper
                                                        .calculateDiscountValue(
                                                                productBO,
                                                                schemeBO.getSelectedPrecent(),
                                                                "SCH_PER", false);
                                            }

                                            if (productBO.getDiscount_order_value() > 0) {
                                                productBO
                                                        .setDiscount_order_value(productBO
                                                                .getDiscount_order_value()
                                                                - totalPercentageDiscount);
                                            }

                                            if (productBO.getSchemeAppliedValue() > 0) {
                                                productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalPercentageDiscount);
                                            }
                                            schemeProductBo.setDiscountValue(totalPercentageDiscount);
                                            totalSchemeDiscountValue += totalPercentageDiscount;
                                        } else if (schemeBO
                                                .isQuantityTypeSelected()) {
                                            orderHelper.updateSchemeFreeProduct(schemeBO, productBO);
                                            break;
                                        }
                                    } else {
                                        if (schemeBO.isQuantityTypeSelected()) {
                                            // if  Accumulation scheme's buy product not available, free product set in First order product object
                                            if (i == schemeProductList.size() && !isBuyProductAvailable) {
                                                ProductMasterBO firstProductBO = mOrderedList.get(0);
                                                orderHelper.updateSchemeFreeProduct(schemeBO, firstProductBO);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        return totalSchemeDiscountValue;
    }


    /**
     * Clear scheme free products
     *
     * @param mOrderedProductList Ordered product list
     */
    public void clearSchemeFreeProduct(LinkedList<ProductMasterBO> mOrderedProductList) {
        if (mOrderedProductList != null)
            for (ProductMasterBO productB0 : mOrderedProductList) {
                if (productB0.getSchemeProducts() != null) {
                    productB0.getSchemeProducts().clear();
                }
                productB0.setCompanyTypeDiscount(0);
                productB0.setDistributorTypeDiscount(0);
                productB0.setSoreasonId(0);

            }

    }

    /**
     * clear discount values product wise
     */
    public void clearDiscountQuantity() {
        ProductMasterBO product;
        int siz = businessModel.productHelper.getProductMaster().size();
        for (int i = 0; i < siz; ++i) {
            product = businessModel.productHelper.getProductMaster().get(i);

            product.setD1(0);
            product.setD2(0);
            product.setD3(0);
            product.setDA(0);
            product.setApplyValue(0);
        }
    }


    /**
     * clear tax and discount values product wise
     *
     * @param orderList Ordered product list
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

}
