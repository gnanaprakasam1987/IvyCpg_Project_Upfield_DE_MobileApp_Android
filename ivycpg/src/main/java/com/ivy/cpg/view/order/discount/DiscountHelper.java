package com.ivy.cpg.view.order.discount;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.OrderHelper;
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
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

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
    private boolean isWihtHoldApplied;
    private String schemeData;
    private String distDiscountData;
    private String compDiscountData;

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

                if (productBO.getNetValue() > 0) {
                    productBO.setNetValue(productBO.getNetValue() - discountValue);
                }

                if (productBO.getLineValueAfterSchemeApplied() > 0) {
                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - discountValue);
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

            double line_total_price = productBO.getNetValue();

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
            totalDiscOrAmtValue = SDUtil.formatAsPerCalculationConfig(totalDiscOrAmtValue);


            //if (discountId == 0) {
            productBO.setApplyValue(totalDiscOrAmtValue);
            //}

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
                    if (batchProductBo.getLineValueAfterSchemeApplied() > 0) {
                        totalValue = batchProductBo.getLineValueAfterSchemeApplied();
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
                    batchProductBo.setProductLevelDiscountValue(batchProductBo.getProductLevelDiscountValue() + totalDiscountValue);


                    discountValueByDiscountId.put(discountId, totalDiscountValue);

                    mDiscountListByProductId.put(productWithBatchId, discountValueByDiscountId);


                    if (batchProductBo.getNetValue() > 0) {
                        batchProductBo.setNetValue(batchProductBo
                                .getNetValue()
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
    public double calculateItemLevelDiscount(List<ProductMasterBO> orderedList) {

        double totalDiscountValue = 0.0;
        String discountName = "";
        String compDiscount = "";

        if (businessModel.productHelper.getDiscountIdList() != null) {
            for (Integer discountID : businessModel.productHelper.getDiscountIdList()) {

                ArrayList<StoreWiseDiscountBO> discountProductIdList = businessModel.productHelper.getProductDiscountListByDiscountID()
                        .get(discountID);
                if (discountProductIdList != null) {
                    for (StoreWiseDiscountBO storeWiseDiscountBO : discountProductIdList) {

                        for (ProductMasterBO productBo : orderedList) {
                            if (productBo.getParentHierarchy().contains("/" + storeWiseDiscountBO
                                    .getProductId() + "/")) {
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
                                        if (!"".equals(compDiscount)) {
                                            compDiscount = compDiscount + "\n" + storeWiseDiscountBO.getDescription();
                                        } else
                                            compDiscount = storeWiseDiscountBO.getDescription();
                                    } else {
                                        if (!"".equals(discountName)) {
                                            discountName = discountName + "\n" + storeWiseDiscountBO.getDescription();
                                        } else
                                            discountName = storeWiseDiscountBO.getDescription();
                                    }


                                    final double discountValue = calculateProductWiseDiscount(productBo,
                                            storeWiseDiscountBO.getDiscount(), percentageORAmountDiscount,
                                            isBatchWise, discountID, isCompanyWiseDisc);

                                    storeWiseDiscountBO.setDiscountValue(discountValue);
                                    productBo.setProductLevelDiscountValue(productBo.getProductLevelDiscountValue() + discountValue);


                                    if (productBo.getLineValueAfterSchemeApplied() > 0) {
                                        productBo.setLineValueAfterSchemeApplied(productBo.getLineValueAfterSchemeApplied() - discountValue);
                                    }

                                    totalDiscountValue = totalDiscountValue + discountValue;

                                }
                            }
                        }
                    }

                }

            }
            // for computing Final discount order value for  a product .
            // added because of Multiple discount applied for same product
            for (ProductMasterBO productBo : orderedList) {
                if (productBo.getOrderedPcsQty() > 0
                        || productBo.getOrderedCaseQty() > 0
                        || productBo.getOrderedOuterQty() > 0) {

                    if (productBo.getNetValue() > 0) {
                        productBo.setNetValue(productBo
                                .getNetValue() - productBo.getProductLevelDiscountValue());
                    }
                }


            }
        }

        setDistDiscountData(discountName);
        setCompDiscountData(compDiscount);
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,dm.Typeid,Description,ApplyLevelid,Moduleid,PM.PID,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue,dm.ComputeAfterTax,dm.ApplyAfterTax");
            sb.append(" from DiscountProductMapping dpm ");
            sb.append(" left Join ProductMaster PM on PM.ParentHierarchy LIKE '%/'|| dpm.ProductId ||'/%' and PM.issalable =1");
            sb.append(" inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append(" where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
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
                    discountBO.setComputeAfterTax(c.getInt(12));
                    discountBO.setApplyAfterTax(c.getInt(13));
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
    public double calculateBillWiseRangeDiscount(double totalOrderValue, int isFlag) {
        double discountValue = 0;
        double finalTotalOrdValue = totalOrderValue;
        if (mBillWiseDiscountList != null) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {
                if (totalOrderValue >= storeWiseDiscountBO.getMinAmount()
                        && totalOrderValue <= storeWiseDiscountBO.getMaxAmount()) {

                    if (isFlag == storeWiseDiscountBO.getComputeAfterTax()) {

                        if (storeWiseDiscountBO.getIsPercentage() == 1) {
                            discountValue = (totalOrderValue * storeWiseDiscountBO.getAppliedDiscount() / 100);

                        } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                            discountValue = storeWiseDiscountBO.getAppliedDiscount();
                        }

                        discountValue = SDUtil.formatAsPerCalculationConfig(discountValue);
                        storeWiseDiscountBO.setDiscountValue(discountValue);
                    }

                    businessModel.getOrderHeaderBO().setBillLevelDiscountValue(storeWiseDiscountBO.getDiscountValue());
                    businessModel.getOrderHeaderBO().setDiscount(storeWiseDiscountBO.getDiscount());
                    businessModel.getOrderHeaderBO().setDiscountId(storeWiseDiscountBO.getDiscountId());
                    businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWiseDiscountBO.getIsCompanyGiven());

                    if (isFlag == storeWiseDiscountBO.getApplyAfterTax())
                        finalTotalOrdValue = finalTotalOrdValue - storeWiseDiscountBO.getDiscountValue();

                    break;
                }
            }

        }
        return finalTotalOrdValue;


    }


    public void loadExistingBillWiseRangeDiscount(Context mContext) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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

    public double calculateBillWiseDiscount(double totalOrderValue, int isFlag) {
        double totalValue = totalOrderValue;
        double totalBillWiseDiscountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        double finalTotOrdDiscValue = totalOrderValue;
        String discountName = "";
        String compDiscount = "";
        if (mBillWiseDiscountList != null && mBillWiseDiscountList.size() > 0) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWiseDiscountList) {

                double discountValue = 0;
                if (isFlag == storeWiseDiscountBO.getComputeAfterTax()) {

                    if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                        totalOrderValue = totalValue - billWiseDistributorDiscount;
                    }

                    if (storeWiseDiscountBO.getIsPercentage() == 1) {
                        discountValue = totalOrderValue * storeWiseDiscountBO.getDiscount() / 100;
                    } else {
                        //Rajkumar - Type id is coming for Bill wise discount also..
                        // So If it is not percentage type discount, then it is considered as amount type discount.
                        discountValue = storeWiseDiscountBO.getDiscount();
                    }
                    discountValue = SDUtil.formatAsPerCalculationConfig(discountValue);
                    storeWiseDiscountBO.setDiscountValue(discountValue);
                }


                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                    if (!"".equals(compDiscount)) {
                        compDiscount = compDiscount + "\n" + storeWiseDiscountBO.getDescription();
                    } else
                        compDiscount = storeWiseDiscountBO.getDescription();
                } else {
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;
                    if (!"".equals(discountName)) {
                        discountName = discountName + "\n" + storeWiseDiscountBO.getDescription();
                    } else
                        discountName = storeWiseDiscountBO.getDescription();
                }

                totalBillWiseDiscountValue = totalBillWiseDiscountValue + discountValue;

                if (isFlag == storeWiseDiscountBO.getApplyAfterTax())
                    finalTotOrdDiscValue = finalTotOrdDiscValue - storeWiseDiscountBO.getDiscountValue();
            }

        }
        setDistDiscountData(discountName);
        setCompDiscountData(compDiscount);
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(
                businessModel.getRetailerMasterBO().getBillWiseCompanyDiscount() + billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(
                businessModel.getRetailerMasterBO().getBillWiseDistributorDiscount() + billWiseDistributorDiscount);
        businessModel.getOrderHeaderBO().setBillLevelDiscountValue(
                businessModel.getOrderHeaderBO().getBillLevelDiscountValue() + totalBillWiseDiscountValue);


        return finalTotOrdDiscValue;
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
                } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                    discountValue = storeWiseDiscountBO.getDiscount();
                }
                discountValue = SDUtil.formatAsPerCalculationConfig(discountValue);

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
        if (mBillWiseDiscountList != null) {
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c;

            StringBuffer sb = new StringBuffer();
            sb.append("select distinct Value,IsPercentage,dm.Typeid,Description,ApplyLevelid,Moduleid,PM.PID,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue,dm.ComputeAfterTax,dm.ApplyAfterTax");
            sb.append(" from DiscountProductMapping dpm ");
            sb.append(" left Join ProductMaster PM on PM.ParentHierarchy LIKE '%/'|| dpm.ProductId ||'/%' and PM.issalable =1");
            sb.append(" inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append(" where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
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
                    discountBO.setComputeAfterTax(c.getInt(12));
                    discountBO.setApplyAfterTax(c.getInt(13));
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,dm.Typeid,Description,ApplyLevelid,Moduleid,PM.PID,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append(" left Join ProductMaster PM on PM.ParentHierarchy LIKE '%/'|| dpm.ProductId ||'/%' and PM.issalable =1");
            sb.append(" inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append(" where (Retailerid=" + businessModel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + businessModel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + businessModel.channelMasterHelper.getChannelHierarchy(businessModel.getRetailerMasterBO().getSubchannelid(), mContext) + ") OR ");
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            sb.append(" and " + businessModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " between FromDate and ToDate");
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
    public double calculateBillWisePayTermDiscount(double totalOrderValue, int isFlag) {


        double totalValue = totalOrderValue;
        double discountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        double finalTotOrdDiscValue = totalOrderValue;
        String discountName = "";
        String compDiscount = "";
        if (mBillWisePaytTermDiscountList != null) {
            for (StoreWiseDiscountBO storeWiseDiscountBO : mBillWisePaytTermDiscountList) {


                if (storeWiseDiscountBO.getIsCompanyGiven() == 0) {

                    totalOrderValue = totalValue - billWiseCompanyDiscount;
                }

                if (isFlag == storeWiseDiscountBO.getComputeAfterTax()) {

                    if (storeWiseDiscountBO.getIsPercentage() == 1) {
                        discountValue = (totalOrderValue * storeWiseDiscountBO.getDiscount() / 100);

                    } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                        discountValue = storeWiseDiscountBO.getDiscount();
                    }
                    discountValue = SDUtil.formatAsPerCalculationConfig(discountValue);
                    storeWiseDiscountBO.setDiscountValue(discountValue);
                }

                if (storeWiseDiscountBO.getIsCompanyGiven() == 1) {
                    billWiseCompanyDiscount = billWiseCompanyDiscount + discountValue;
                    if (!"".equals(compDiscount)) {
                        compDiscount = compDiscount + "\n" + storeWiseDiscountBO.getDescription();
                    } else
                        compDiscount = storeWiseDiscountBO.getDescription();
                } else {
                    billWiseDistributorDiscount = billWiseDistributorDiscount + discountValue;
                    if (!"".equals(discountName)) {
                        discountName = discountName + "\n" + storeWiseDiscountBO.getDescription();
                    } else
                        discountName = storeWiseDiscountBO.getDescription();
                }

                if (isFlag == storeWiseDiscountBO.getApplyAfterTax())
                    finalTotOrdDiscValue = finalTotOrdDiscValue - storeWiseDiscountBO.getDiscountValue();

                businessModel.getOrderHeaderBO().setBillLevelDiscountValue(
                        businessModel.getOrderHeaderBO().getBillLevelDiscountValue() + storeWiseDiscountBO.getDiscountValue());
                businessModel.getOrderHeaderBO().setDiscount(storeWiseDiscountBO.getDiscount());
                businessModel.getOrderHeaderBO().setDiscountId(storeWiseDiscountBO.getDiscountId());
                businessModel.getOrderHeaderBO().setIsCompanyGiven(storeWiseDiscountBO.getIsCompanyGiven());
                break;

            }

        }
        setDistDiscountData(discountName);
        setCompDiscountData(compDiscount);
        businessModel.getRetailerMasterBO().setBillWiseCompanyDiscount(
                businessModel.getRetailerMasterBO().getBillWiseCompanyDiscount() + billWiseCompanyDiscount);
        businessModel.getRetailerMasterBO().setBillWiseDistributorDiscount(
                businessModel.getRetailerMasterBO().getBillWiseDistributorDiscount() + billWiseDistributorDiscount);

        return finalTotOrdDiscValue;


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
        String strAppliedSchemes = "";

        ArrayList<SchemeBO> appliedSchemeList = schemeHelper.getAppliedSchemeList();
        if (appliedSchemeList != null) {

            for (SchemeBO schemeBO : appliedSchemeList) {
                boolean isFreeProductGiven = false;
                if (schemeBO.isAmountTypeSelected() || schemeBO.isDiscountPrecentSelected() ||
                        schemeBO.isQuantityTypeSelected() || schemeBO.isPriceTypeSeleted()) {
                    if (!"".equals(strAppliedSchemes))
                        strAppliedSchemes = strAppliedSchemes + "\n" + schemeBO.getScheme();
                    else
                        strAppliedSchemes = schemeBO.getScheme();
                }
                if (schemeBO != null) {
                    if (schemeBO.isAmountTypeSelected()) {
                        if (businessModel.configurationMasterHelper.IS_SKIP_SCHEME_APPLY &&
                                schemeBO.getMaximumSlab() != 0 &&
                                schemeBO.getSelectedAmount() > schemeBO.getMaximumSlab()) {// Checking the Maximum slab Cap here
                            schemeBO.setSelectedAmount(schemeBO.getMaximumSlab());
                        }
                        totalSchemeDiscountValue += schemeBO.getSelectedAmount();
                        totalSchemeDiscountValue = SDUtil.formatAsPerCalculationConfig(totalSchemeDiscountValue);
                    }

                    List<SchemeProductBO> schemeProductList = schemeBO
                            .getBuyingProducts();
                    int i = 0;
                    boolean isBuyProductAvailable = false;
                    if (schemeProductList != null) {

                        // Getting total order value of buy products
                        double totalOrderValueOfBuyProducts = 0;
                        double totalPercentageDiscountAmt = 0;
                        if (schemeBO.isAmountTypeSelected()) {
                            for (SchemeProductBO schemeProductBo : schemeProductList) {
                                totalOrderValueOfBuyProducts += schemeHelper.getTotalOrderedValue(schemeProductBo.getProductId(),
                                        schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeBO.getParentId(), false, false);
                            }
                        } else if (schemeBO.isDiscountPrecentSelected()) {
                            for (SchemeProductBO schemeProductBo : schemeProductList) {
                                totalOrderValueOfBuyProducts += schemeHelper.getTotalOrderedValue(schemeProductBo.getProductId(),
                                        schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeBO.getParentId(), false, false);
                            }
                            totalPercentageDiscountAmt = (totalOrderValueOfBuyProducts * schemeBO.getSelectedPrecent()) / 100;


                        }


                        ArrayList<String> productIdList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeProductList) {

                            for (ProductMasterBO productBO : mOrderedList) {
                                if (productBO.getParentHierarchy().contains("/" + schemeProductBo.getProductId() + "/")) {
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
                                                amount_free = SDUtil.formatAsPerCalculationConfig(amount_free);
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
                                                                    amount_free_batch = SDUtil.formatAsPerCalculationConfig(amount_free_batch);
                                                                    //

                                                                    batchProduct.setSchemeDiscAmount(batchProduct.getSchemeDiscAmount() + amount_free_batch);

                                                                    //
                                                                    batchProduct.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - amount_free_batch);

                                                                    //to update summation of all the batch wise disocunt amount into product BO for to show in print screen
                                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free_batch);
                                                                    if (productBO.getNetValue() > 0) {
                                                                        productBO.setNetValue(productBO.getNetValue() - amount_free_batch);

                                                                    }

                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                        schemeProductBo.setDiscountValue(amount_free);
                                                        if (productBO.getNetValue() > 0) {
                                                            productBO.setNetValue(productBO.getNetValue() - amount_free);

                                                        }
                                                        //
                                                        productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - amount_free);
                                                    }
                                                } else {
                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                    schemeProductBo.setDiscountValue(amount_free);
                                                    if (productBO.getNetValue() > 0) {
                                                        productBO.setNetValue(productBO.getNetValue() - amount_free);

                                                    }
                                                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - amount_free);
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

                                                if (productBO.getNetValue() > 0) {
                                                    productBO
                                                            .setNetValue(productBO
                                                                    .getNetValue()
                                                                    - totalPriceDiscount);

                                                }
                                                if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - totalPriceDiscount);
                                                }

                                                schemeProductBo.setDiscountValue(totalPriceDiscount);

                                                //update total price scheme discount value into productBo to show in print screen
                                                productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + totalPriceDiscount);

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
                                                if (schemeBO.getMaximumSlab() != 0 && totalPercentageDiscount > schemeBO.getMaximumSlab()) {// Checking the Maximum slab Cap here
                                                    double percentage_productContribution = ((totalPercentageDiscount / totalPercentageDiscountAmt) * 100);
                                                    totalPercentageDiscount = schemeBO.getMaximumSlab() * (percentage_productContribution / 100);
                                                }
                                                if (productBO.getNetValue() > 0) {
                                                    productBO
                                                            .setNetValue(productBO
                                                                    .getNetValue()
                                                                    - totalPercentageDiscount);
                                                }

                                                if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - totalPercentageDiscount);
                                                }
                                                schemeProductBo.setDiscountValue(totalPercentageDiscount);

                                                //update total percentage value into product BO to show in print screen
                                                productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + totalPercentageDiscount);

                                                totalSchemeDiscountValue += totalPercentageDiscount;
                                            } else if (schemeBO
                                                    .isQuantityTypeSelected()) {
                                                if (!isFreeProductGiven) {
                                                    orderHelper.updateSchemeFreeProduct(schemeBO, productBO);
                                                    isFreeProductGiven = true;
                                                }
                                                break;
                                            }
                                        } else {
                                            if (schemeBO.isQuantityTypeSelected()) {
                                                // if  Accumulation scheme's buy product not available, free product set in First order product object
                                                if (!isFreeProductGiven && !isBuyProductAvailable && mOrderedList.size() > 0) {
                                                    ProductMasterBO firstProductBO = mOrderedList.get(0);
                                                    orderHelper.updateSchemeFreeProduct(schemeBO, firstProductBO);
                                                    isFreeProductGiven = true;
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
        }

        setSchemeData(strAppliedSchemes);
        return totalSchemeDiscountValue;
    }

    /**
     * Clear scheme free products
     *
     * @param context
     * @param mOrderedProductList Ordered product list
     */
    public void clearSchemeFreeProduct(Context context, LinkedList<ProductMasterBO> mOrderedProductList) {
        if (mOrderedProductList != null)
            for (ProductMasterBO productB0 : mOrderedProductList) {
                if (productB0.getSchemeProducts() != null) {
                    productB0.getSchemeProducts().clear();
                }
                productB0.setCompanyTypeDiscount(0);
                productB0.setDistributorTypeDiscount(0);
                productB0.setSoreasonId(0);

            }
        //Mansoor Clear Applied Scheme List
        SchemeDetailsMasterHelper schemeDetailsMasterHelper = SchemeDetailsMasterHelper.getInstance(context);
        if (schemeDetailsMasterHelper.getSchemeList() != null) {
            for (SchemeBO schemeBO : schemeDetailsMasterHelper.getSchemeList()) {
                schemeBO.setChecked(false);
                for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts())
                    schemeProductBO.setQuantitySelected(0);
            }
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
     * @param productMasterBO ordered product object
     */
    public void clearProductDiscountAndTaxValue(ProductMasterBO productMasterBO) {
        if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productMasterBO.getBatchwiseProductCount() > 0) {
            ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
            if (batchList != null) {
                for (ProductMasterBO batchProduct : batchList) {
                    batchProduct.setProductLevelDiscountValue(0);
                    batchProduct.setSchemeDiscAmount(0);
                    batchProduct.setTaxableAmount(0);
                }
            }

        } else {
            productMasterBO.setProductLevelDiscountValue(0);
            productMasterBO.setTaxableAmount(0);
        }
         /*this object used in print screen we compute batch wise scheme discount amt into product BO for to show value int item wise
        so need to reset this obj when reload ordered product to avoid value mismatch
        */
        productMasterBO.setSchemeDiscAmount(0);
    }

    public boolean isWihtHoldApplied() {
        return isWihtHoldApplied;
    }

    public void setWihtHoldApplied(boolean wihtHoldApplied) {
        isWihtHoldApplied = wihtHoldApplied;
    }

    public HashMap<String, Double> prepareProductDiscountForPrint(Context context, String orderId) {
        DBUtil db = null;
        HashMap<String, Double> mDiscountsApplied = new HashMap<>();
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select typeid,discountType,sum(Value) from OrderDiscountDetail" +
                    " where orderid=" + StringUtils.QT(orderId) + "  group by typeid");
            Cursor c = db.selectSQL(sb.toString());
            while (c.moveToNext()) {
                mDiscountsApplied.put(c.getString(1), c.getDouble(2));

            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return mDiscountsApplied;

    }

    public String getSchemeData() {
        return schemeData;
    }

    public void setSchemeData(String schemeData) {
        this.schemeData = schemeData;
    }

    public String getDistDiscountData() {
        return distDiscountData;
    }

    public void setDistDiscountData(String distDiscountData) {
        this.distDiscountData = distDiscountData;
    }

    public String getCompDiscountData() {
        return compDiscountData;
    }

    public void setCompDiscountData(String compDiscountData) {
        this.compDiscountData = compDiscountData;
    }
}
