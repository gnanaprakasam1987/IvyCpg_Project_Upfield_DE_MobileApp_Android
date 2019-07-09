package com.ivy.cpg.view.order.tax;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mansoor on 19/1/18.
 */

public class TaxGstHelper implements TaxInterface {

    private static TaxGstHelper instance = null;
    private BusinessModel mBusinessModel;
    private Context mContext;

    private ArrayList<TaxBO> mBillTaxList = new ArrayList<>();
    private ArrayList<String> mProductTaxList;
    private HashMap<String, ArrayList<TaxBO>> mTaxListByProductId;
    private ArrayList<TaxBO> mGroupIdList;
    private LinkedHashMap<String, HashSet<String>> mProductIdByTaxGroupId;
    private LinkedHashMap<Integer, HashSet<Double>> mTaxPercentagerListByGroupId;
    private SparseArray<LinkedHashSet<TaxBO>> mTaxBOByGroupId;
    private HashMap<String, TaxBO> mTaxBoByBatchProduct = null;
    private HashMap<String, ArrayList<TaxBO>> mTaxBoBatchProduct = null;//used for batch wise product's
    private ArrayList<TaxBO> taxBOArrayList = null;


    public HashMap<String, ArrayList<TaxBO>> getmTaxBoBatchProduct() {
        return mTaxBoBatchProduct;
    }


    public HashMap<String, ArrayList<TaxBO>> getmTaxListByProductId() {
        return mTaxListByProductId;
    }


    public ArrayList<TaxBO> getBillTaxList() {
        if (mBillTaxList != null) {
            return mBillTaxList;
        }
        return new ArrayList<>();
    }

    public ArrayList<TaxBO> getGroupIdList() {
        if (mGroupIdList != null) {
            return mGroupIdList;
        }
        return new ArrayList<>();
    }

    public LinkedHashMap<String, HashSet<String>> getProductIdByTaxGroupId() {
        return mProductIdByTaxGroupId;
    }

    public LinkedHashMap<Integer, HashSet<Double>> getTaxPercentagerListByGroupId() {
        return mTaxPercentagerListByGroupId;
    }

    public SparseArray<LinkedHashSet<TaxBO>> getTaxBoByGroupId() {
        return mTaxBOByGroupId;
    }

    public static TaxGstHelper getInstance(Context context) {
        if (instance == null)
            instance = new TaxGstHelper(context);

        return instance;
    }

    private TaxGstHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
        this.mContext = context.getApplicationContext();
    }


    /**
     * @author rajesh.k Method to use download product wise tax details
     * 1. IS_TAX_LOC - true
     * Check location wise tax
     * LocationId (RetailerLocation)
     * sourceLocId(SupplierLocation)
     * isSameZone - Both LocationID are same
     * isDifferentZone - Both LocationID are not same
     * 2. IS_TAX_LOC - false
     * Check isSameZone
     * isSameZone - true means same location
     * isSameZone - false measn different location
     */

    public void downloadProductTaxDetails() {

        mProductTaxList = new ArrayList<>();
        mTaxListByProductId = new HashMap<>();
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c;

            String sb = "select distinct A.pid,TM.TaxDesc,TM.taxrate,SLM.ListName,TM.TaxType,TM.minvalue,TM.maxValue,TM.applyRange,TM.groupid,ifnull(TM.parentType,0),SLM.flex1,TM.applylevelid from  productmaster A ";

            if (mBusinessModel.configurationMasterHelper.IS_GST)
                sb = sb + " inner JOIN ProductTaxMaster PTM on  PTM.pid = A.pid ";
            if (mBusinessModel.configurationMasterHelper.IS_GST_HSN)
                sb = sb + " inner JOIN ProductTaxMaster PTM on  PTM.HSNId = A.HSNId ";

            sb = sb + " inner JOIN TaxMaster TM on  PTM.groupid = TM.groupid "
                    + " inner JOIN StandardListMaster SLM ON SLM.Listid = TM.TaxType "
                    + " inner JOIN (select listid from standardlistmaster where  listcode='ITEM' and ListType='TAX_APPLY_TYPE') SD ON TM.applylevelid =SD.listid "
                    + " where PTM.TaxTypeId = " + mBusinessModel.getRetailerMasterBO().getTaxTypeId();

            if (mBusinessModel.configurationMasterHelper.IS_TAX_LOC) {
                if (mBusinessModel.getRetailerMasterBO().getRetailerTaxLocId()
                        != mBusinessModel.getRetailerMasterBO().getSupplierTaxLocId()) {
                    sb = sb + (" AND PTM.LocationId = 0");
                } else {
                    sb = sb + (" AND PTM.LocationId = " + mBusinessModel.getRetailerMasterBO().getRetailerTaxLocId());
                }

                sb = sb + (" AND PTM.sourceLocId = " + mBusinessModel.getRetailerMasterBO().getSupplierTaxLocId());
            } else
                sb = sb + (" AND PTM.isSameZone = " + mBusinessModel.getRetailerMasterBO().isSameZone());

            sb = sb + " order by A.pid";

            c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                TaxBO taxBo;
                ArrayList<TaxBO> taxList = new ArrayList<>();

                String productid = "";

                while (c.moveToNext()) {
                    taxBo = new TaxBO();
                    taxBo.setPid(c.getInt(0));
                    taxBo.setTaxRate(c.getDouble(2));
                    taxBo.setTaxDesc(c.getString(3));
                    taxBo.setTaxType(c.getString(4));
                    taxBo.setMinValue(c.getDouble(5));
                    taxBo.setMaxValue(c.getDouble(6));
                    taxBo.setApplyRange(c.getInt(7));
                    taxBo.setGroupId(c.getInt(8));
                    taxBo.setParentType(c.getString(9));
                    taxBo.setTaxDesc2(c.getString(10));
                    taxBo.setApplyLevelId(c.getInt(11));

                    if (!productid.equals(taxBo.getPid() + "")) {
                        if (!productid.equals("")) {

                            mTaxListByProductId.put(productid, taxList);
                            taxList = new ArrayList<>();
                            taxList.add(taxBo);
                            productid = taxBo.getPid() + "";
                            mProductTaxList.add(productid);

                        } else {
                            taxList.add(taxBo);
                            productid = taxBo.getPid() + "";
                            mProductTaxList.add(productid);
                        }
                    } else {
                        taxList.add(taxBo);
                    }
                }
                if (taxList.size() > 0) {
                    mTaxListByProductId.put(productid, taxList);
                }

            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadBillWiseTaxDetails() {
        mBillTaxList = new ArrayList<>();
        TaxBO taxBO;
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            String sb = "select TM.TaxType,TM.TaxRate,TM.Sequence,SLM.ListName,TM.ParentType,TM.applylevelid from  TaxMaster TM "
                    + "inner JOIN ProductTaxMaster PTM on  PTM.groupid = TM.groupid "
                    + "INNER JOIN StandardListMaster SLM ON SLM.Listid = TM.TaxType "
                    + "inner JOIN (select listid from standardlistmaster where  listcode='BILL' and ListType='TAX_APPLY_TYPE') SD ON TM.applylevelid =SD.listid "
                    + "where PTM.TaxTypeId = " + mBusinessModel.getRetailerMasterBO().getTaxTypeId() + " AND PTM.PID = 0";

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    taxBO = new TaxBO();
                    taxBO.setTaxType(c.getString(0));
                    taxBO.setTaxRate(c.getDouble(1));
                    taxBO.setSequence(c.getString(2));
                    taxBO.setTaxDesc(c.getString(3));
                    taxBO.setParentType(c.getString(4));
                    taxBO.setApplyLevelId(c.getInt(5));
                    taxBO.setGroupId(c.getInt(6));

                    mBillTaxList.add(taxBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * @param invoiceid invoiceid
     * @author rajesh.k Method to use insert tax details in SQLite
     */
    public void insertInvoiceTaxList(String invoiceid, DBUtil db) {

        if (mBillTaxList != null) {
            String columns = "RetailerId,invoiceid,taxRate,taxType,taxValue,applyLevelId";
            StringBuffer sb;
            for (TaxBO taxBO : mBillTaxList) {
                sb = new StringBuffer();
                sb.append(StringUtils.getStringQueryParam(mBusinessModel.getRetailerMasterBO().getRetailerID()))
                        .append(",").append(StringUtils.getStringQueryParam(invoiceid))
                        .append(",").append(taxBO.getTaxRate())
                        .append(",").append(StringUtils.getStringQueryParam(taxBO.getTaxType()))
                        .append(",").append(taxBO.getTotalTaxAmount())
                        .append(",").append(taxBO.getApplyLevelId());
                db.insertSQL("InvoiceTaxDetails", columns, sb.toString());
            }
        }
    }


    /**
     * @param orderId orderId
     * @param db      db
     * @author rajesh.k Method to use insert tax details in SQLite
     */
    public void insertBillLevelTax(String orderId, DBUtil db) {

        db.deleteSQL("OrderTaxDetails", "OrderID=" + orderId,
                false);
        if (mBillTaxList != null) {
            String columns = "RetailerId,orderid,taxRate,taxType,taxValue,pid,taxName,parentType,groupId,applyLevelId";
            StringBuffer sb;
            for (TaxBO taxBO : mBillTaxList) {
                sb = new StringBuffer();
                sb.append(StringUtils.getStringQueryParam(mBusinessModel.getRetailerMasterBO().getRetailerID()))
                        .append(",").append(orderId)
                        .append(",").append(taxBO.getTaxRate())
                        .append(",").append(StringUtils.getStringQueryParam(taxBO.getTaxType()))
                        .append(",").append(SDUtil.roundIt(taxBO.getTotalTaxAmount(), 2))
                        .append(",").append("0,'").append(taxBO.getTaxDesc2())
                        .append("',").append(taxBO.getParentType())
                        .append(",").append(taxBO.getGroupId())
                        .append(",").append(taxBO.getApplyLevelId());

                db.insertSQL("OrderTaxDetails", columns, sb.toString());

            }
        }
    }

    @Override
    public HashMap<String, Double> prepareProductTaxForPrint(Context context, String orderId, boolean isFromInvoice) {
        DBUtil db = null;
        HashMap<String, Double> mTaxesApplied = new HashMap<>();
        try {
            String tableName = "OrderTaxDetails";
            if (isFromInvoice)
                tableName = "InvoiceTaxDetails";

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select taxType,taxRate,taxName,parentType,taxValue,pid from " + tableName + " IT" +
                    " where orderid=" + orderId + "  order by taxType,taxRate,taxName desc");
            Cursor c = db.selectSQL(sb.toString());
            String lastTaxType = "", lastTaxRate = "", lastTaxName = "";
            double totalTaxByType = 0, totalTaxableAmountByType = 0;
            ArrayList<String> uniqueTaxTypeWithRate = new ArrayList<>();
            while (c.moveToNext()) {

                String taxType = c.getString(0);
                String taxRate = c.getString(1);
                String taxName = c.getString(2);
                double taxAmount = c.getDouble(4);
                double taxableAmount = mBusinessModel.productHelper.getProductMasterBOById(c.getString(5)).getTaxableAmount();

                if (!lastTaxType.equals("") && (!lastTaxType.equals(taxType) || !lastTaxRate.equals(taxRate))) {

                    if (!uniqueTaxTypeWithRate.contains(lastTaxType + lastTaxRate)) {
                        mTaxesApplied.put(lastTaxName + " " + lastTaxRate + "% " + context.getResources().getString(R.string.tax_on) + " " + totalTaxableAmountByType, totalTaxByType);
                        uniqueTaxTypeWithRate.add(lastTaxType + lastTaxRate);
                    }

                    totalTaxByType = taxAmount;
                    totalTaxableAmountByType = taxableAmount;

                } else {
                    totalTaxByType += taxAmount;
                    totalTaxableAmountByType += taxableAmount;
                }

                //
                lastTaxName = taxName;
                lastTaxRate = taxRate;
                lastTaxType = taxType;

            }
            if (!uniqueTaxTypeWithRate.contains(lastTaxType + lastTaxRate)) {
                mTaxesApplied.put(lastTaxName + " " + lastTaxRate + "% " + context.getResources().getString(R.string.tax_on) + " " + SDUtil.format(totalTaxableAmountByType, mBusinessModel.configurationMasterHelper.VALUE_PRECISION_COUNT, 0), totalTaxByType);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return mTaxesApplied;
    }

    /**
     * Method to use load tax information for print zebra 3inch in titan project
     *
     * @param invoiceid invoiceid
     */
    public void loadTaxDetailsForPrint(String invoiceid) {

        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            String sb = "select distinct IT.taxType,IT.taxRate,slm.flex1,TM.ParentType from OrderTaxDetails IT" +
                    " inner join taxmaster TM on IT.groupid=TM.Groupid and IT.TaxType=TM.taxtype " +
                    " left join standardlistmaster slm on TM.taxtype=slm.listid " +
                    " where orderid=" + StringUtils.getStringQueryParam(invoiceid) + " order by IT.taxType,IT.taxRate,slm.flex1 desc";

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                int groupid = 0;
                mGroupIdList = new ArrayList<>();

                mTaxPercentagerListByGroupId = new LinkedHashMap<>();
                mTaxBOByGroupId = new SparseArray<>();
                HashSet<Double> taxPercentagelist = new HashSet<>();
                LinkedHashSet<TaxBO> taxList = new LinkedHashSet<>();
                TaxBO taxBO;
                while (c.moveToNext()) {

                    taxBO = new TaxBO();
                    taxBO.setGroupId(c.getInt(0));

                    taxBO.setTaxDesc2(c.getString(2));
                    taxBO.setTaxRate(c.getDouble(1));
                    taxBO.setParentType(c.getString(3));

                    if (groupid != c.getInt(0)) {
                        if (groupid != 0) {
                            mTaxPercentagerListByGroupId.put(groupid, taxPercentagelist);
                            taxPercentagelist = new HashSet<>();
                            taxPercentagelist.add(c.getDouble(2));
                            mTaxBOByGroupId.put(groupid, taxList);
                            taxList = new LinkedHashSet<>();
                            taxList.add(taxBO);
                            groupid = c.getInt(0);
                            mGroupIdList.add(taxBO);
                        } else {
                            taxPercentagelist.add(c.getDouble(2));
                            taxList.add(taxBO);
                            groupid = c.getInt(0);
                            mGroupIdList.add(taxBO);
                        }
                    } else {
                        taxPercentagelist.add(c.getDouble(2));
                        taxList.add(taxBO);
                    }
                }

                if (taxPercentagelist.size() > 0) {
                    mTaxPercentagerListByGroupId.put(groupid, taxPercentagelist);
                }
                if (taxList.size() > 0) {
                    mTaxBOByGroupId.put(groupid, taxList);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadTaxProductDetailsForPrint(String invoiceid) {

        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb = "select distinct IT.taxType,pid,IT.taxRate,IT.isFreeProduct from OrderTaxDetails IT" +
                    " left join taxmaster TM on IT.groupid=TM.Groupid" +
                    " left join standardlistmaster slm on TM.taxtype=slm.listid " +
                    " where orderid=" + StringUtils.getStringQueryParam(invoiceid) + " and IT.isFreeProduct=0 order by IT.taxType,IT.taxRate";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                String groupid = "";

                mProductIdByTaxGroupId = new LinkedHashMap<>();
                HashSet<String> producttaxlist = new HashSet<>();


                while (c.moveToNext()) {
                    String productid = c.getString(1);

                    if (!groupid.equals(c.getInt(0) + "" + c.getDouble(2))) {
                        if (!groupid.equals("")) {
                            mProductIdByTaxGroupId.put(groupid, producttaxlist);
                            producttaxlist = new HashSet<>();
                            producttaxlist.add(productid);
                            groupid = c.getInt(0) + "" + c.getDouble(2);
                        } else {
                            producttaxlist.add(productid);
                            groupid = c.getInt(0) + "" + c.getDouble(2);
                        }
                    } else {
                        producttaxlist.add(productid);
                    }
                }
                if (producttaxlist.size() > 0) {
                    mProductIdByTaxGroupId.put(groupid, producttaxlist);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public LinkedHashMap<String, HashSet<String>> loadTaxFreeProductDetails(String invoiceid) {

        LinkedHashMap<String, HashSet<String>> mFreeProductIdByTaxGroupId = new LinkedHashMap<>();
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb = "select distinct IT.taxType,pid,IT.taxRate,IT.isFreeProduct from invoicetaxdetails IT" +
                    " left join taxmaster TM on IT.groupid=TM.Groupid " +
                    " left join standardlistmaster slm on TM.taxtype=slm.listid " +
                    " where invoiceid=" + StringUtils.getStringQueryParam(invoiceid) + "and IT.isFreeProduct=1 order by IT.taxType,IT.taxRate";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                String groupid = "";
                HashSet<String> producttaxlist = new HashSet<>();
                while (c.moveToNext()) {
                    String productid = c.getString(1);

                    if (!groupid.equals(c.getInt(0) + "" + c.getDouble(2))) {
                        if (!groupid.equals("")) {
                            mFreeProductIdByTaxGroupId.put(groupid, producttaxlist);
                            producttaxlist = new HashSet<>();
                            producttaxlist.add(productid);
                            groupid = c.getInt(0) + "" + c.getDouble(2);
                        } else {
                            producttaxlist.add(productid);
                            groupid = c.getInt(0) + "" + c.getDouble(2);
                        }
                    } else {
                        producttaxlist.add(productid);
                    }
                }
                if (producttaxlist.size() > 0) {
                    mFreeProductIdByTaxGroupId.put(groupid, producttaxlist);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return mFreeProductIdByTaxGroupId;
    }


    /**
     * @author rajesh.k Method to use update productwise tax value only object
     * level not DB level
     */
    public void updateProductWiseExcludeTax() {
        if (mProductTaxList != null) {
            mTaxBoBatchProduct = new HashMap<>();
            for (String productId : mProductTaxList) {
                ProductMasterBO productBo = mBusinessModel.productHelper.getProductMasterBOById(productId);

                if (productBo != null) {
                    if (productBo.getOrderedPcsQty() > 0
                            || productBo.getOrderedCaseQty() > 0
                            || productBo.getOrderedOuterQty() > 0) {
                        ArrayList<TaxBO> taxList = mTaxListByProductId
                                .get(productId);
                        if (taxList != null) {
                            int totalQty = productBo.getOrderedPcsQty()
                                    + productBo.getOrderedCaseQty()
                                    * productBo.getCaseSize()
                                    + productBo.getOrderedOuterQty()
                                    * productBo.getOutersize();
                            double totalValue = productBo
                                    .getNetValue();
                            double remainingValue = totalValue / totalQty;
                            double taxRate = 0;
                            taxBOArrayList = new ArrayList<>();
                            for (TaxBO taxBO : taxList) {
                                if (mBusinessModel.configurationMasterHelper.SHOW_MRP_LEVEL_TAX) {
                                    if (taxBO.getApplyRange() == 1) {

                                        if (taxBO.getMinValue() <= remainingValue
                                                && taxBO.getMaxValue() >= remainingValue) {

                                            if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                                                taxRate += taxBO.getTaxRate();
                                            }
                                            calculateTaxOnTax(productBo, taxBO, false);
                                        }

                                    } else {
                                        if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                                            taxRate += taxBO.getTaxRate();
                                        }
                                        calculateTaxOnTax(productBo, taxBO, false);
                                    }
                                } else {
                                    if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                                        taxRate += taxBO.getTaxRate();
                                    }
                                    calculateTaxOnTax(productBo, taxBO, false);
                                }
                            }
                            //  calculateTotalTaxForProduct(productBo);
                            calculateProductExcludeTax(productBo, taxRate);

                            calculateandDistributeTax(productBo, taxRate, taxList);
                        }
                    }
                }
            }
        }

    }

    private void calculateandDistributeTax(ProductMasterBO productBO, double taxRate, ArrayList<TaxBO> taxList) {
        for (TaxBO taxBO : taxList) {
            if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                taxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(productBO.getTaxableAmount() * (taxBO.getTaxRate() / 100)));
            }
        }
    }

    // Excluding tax value from product total value and setting it in taxlist(mTaxListByProductId) against to product id
    public void calculateTaxOnTax(ProductMasterBO productMasterBO, TaxBO taxBO, boolean isFreeProduct) {

        double productPriceWithoutTax;
        double taxAmount;

        if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
            //Allowing only parent tax type

            if (productMasterBO.getBatchwiseProductCount() > 0 && mBusinessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                // If batch available

                ArrayList<ProductMasterBO> batchList = mBusinessModel.batchAllocationHelper
                        .getBatchlistByProductID().get(productMasterBO.getProductID());
                if (batchList != null) {
                    mTaxBoByBatchProduct = new HashMap<>();
                    TaxBO batchTaxBO = null;
                    for (ProductMasterBO batchProductBO : batchList) {
                        if (batchProductBO.getOrderedPcsQty() > 0
                                || batchProductBO.getOrderedCaseQty() > 0
                                || batchProductBO.getOrderedOuterQty() > 0) {
                            // calculating tax value batchwise

                            batchTaxBO = cloneTaxBo(taxBO);

                            productPriceWithoutTax = batchProductBO.getNetValue() / (1 + (batchTaxBO.getTaxRate() / 100));
                            taxAmount = productPriceWithoutTax * batchTaxBO.getTaxRate() / 100;

                            batchTaxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(taxAmount));
                            batchTaxBO.setTaxableAmount(SDUtil.formatAsPerCalculationConfig(productPriceWithoutTax));
                            if (mTaxBoBatchProduct.get(batchProductBO.getProductID()) == null)
                                taxBOArrayList.add(batchTaxBO);

                            if (mTaxBoBatchProduct == null)
                                mTaxBoBatchProduct.put(batchProductBO.getProductID(), taxBOArrayList);
                            else if (mTaxBoBatchProduct.get(batchProductBO.getProductID()) != null)
                                mTaxBoBatchProduct.get(batchProductBO.getProductID()).add(batchTaxBO);
                            else
                                mTaxBoBatchProduct.put(batchProductBO.getProductID(), taxBOArrayList);


                            mTaxBoByBatchProduct.put(batchProductBO.getProductID() + batchProductBO.getBatchid(), batchTaxBO);


                        }
                    }

                    HashMap<String, TaxBO> tempList = new HashMap<>();
                    if (batchTaxBO != null)
                        tempList.put(taxBO.getTaxType(), batchTaxBO);
                    excludeChildTaxIfAvailable(productMasterBO, tempList, isFreeProduct);

                }
            } else {
                // calculating tax value

                productPriceWithoutTax = productMasterBO.getNetValue() / (1 + (taxBO.getTaxRate() / 100));
                taxAmount = productPriceWithoutTax * taxBO.getTaxRate() / 100;

                //setting tax and taxable amount against to each tax object
                taxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(taxAmount));
                taxBO.setTaxableAmount(SDUtil.formatAsPerCalculationConfig(productPriceWithoutTax));

                // calculating tax amount for child taxes..
                HashMap<String, TaxBO> tempList = new HashMap<>();
                tempList.put(taxBO.getTaxType(), taxBO);
                excludeChildTaxIfAvailable(productMasterBO, tempList, isFreeProduct);
            }
        }

    }


    // excluding child tax value if available for parent(mParentTaxBoByTaxType) tax
    // recursive call used to calculate tax value for every(child of child..) child.
    private void excludeChildTaxIfAvailable(ProductMasterBO productMasterBO, HashMap<String, TaxBO> mParentTaxBoByTaxType, boolean isFreeProduct) {

        try {

            double completeParentTaxAmount;
            double childTaxAmount;
            HashMap<String, TaxBO> mTempParentTaxBoByTaxType = new HashMap<>();

            for (String parentTaxType : mParentTaxBoByTaxType.keySet()) {

                for (TaxBO childTaxBO : (!isFreeProduct ? mTaxListByProductId.get(productMasterBO.getProductID()) : mBusinessModel.getmFreeProductTaxListByProductId().get(productMasterBO.getProductID()))) {
                    if (childTaxBO.getParentType().equals(mParentTaxBoByTaxType.get(parentTaxType).getTaxType())) {
                        // child tax available

                        if (productMasterBO.getBatchwiseProductCount() > 0 && mBusinessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            ArrayList<ProductMasterBO> batchList = mBusinessModel.batchAllocationHelper
                                    .getBatchlistByProductID().get(productMasterBO.getProductID());
                            if (batchList != null) {
                                // batch available
                                for (ProductMasterBO batchProductBO : batchList) {
                                    if (batchProductBO.getOrderedPcsQty() > 0
                                            || batchProductBO.getOrderedCaseQty() > 0
                                            || batchProductBO.getOrderedOuterQty() > 0) {

                                        TaxBO batchChildTaxBO = cloneTaxBo(childTaxBO);

                                        completeParentTaxAmount = mTaxBoByBatchProduct.get(batchProductBO.getProductID() + batchProductBO.getBatchid()).getTotalTaxAmount() / (1 + (batchChildTaxBO.getTaxRate() / 100));
                                        childTaxAmount = completeParentTaxAmount * batchChildTaxBO.getTaxRate() / 100;

                                        batchChildTaxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(childTaxAmount));
                                        batchChildTaxBO.setTaxableAmount(mTaxBoByBatchProduct.get(batchProductBO.getProductID() + batchProductBO.getBatchid()).getTotalTaxAmount());

                                        mTaxBoByBatchProduct.put(batchProductBO.getProductID() + batchProductBO.getBatchid(), batchChildTaxBO);

                                        mTempParentTaxBoByTaxType.put(childTaxBO.getTaxType(), childTaxBO);

                                    }
                                }
                            }
                        } else {

                            // calculating tax values for child tax..
                            completeParentTaxAmount = mParentTaxBoByTaxType.get(parentTaxType).getTotalTaxAmount() / (1 + (childTaxBO.getTaxRate() / 100));
                            childTaxAmount = completeParentTaxAmount * childTaxBO.getTaxRate() / 100;

                            childTaxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(childTaxAmount));
                            childTaxBO.setTaxableAmount(mParentTaxBoByTaxType.get(parentTaxType).getTotalTaxAmount());

                            mTempParentTaxBoByTaxType.put(childTaxBO.getTaxType(), childTaxBO);
                        }
                    }

                }
            }

            // Recursive call until current taxBo has child
            if (mTempParentTaxBoByTaxType.size() > 0) {
                excludeChildTaxIfAvailable(productMasterBO, mTempParentTaxBoByTaxType, isFreeProduct);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    public TaxBO cloneTaxBo(TaxBO taxBO) {

        return new TaxBO(taxBO.getTaxType(), taxBO.getTaxRate(), taxBO.getSequence(), taxBO.getTaxDesc(), taxBO.getParentType(), taxBO.getTotalTaxAmount()
                , taxBO.getPid(), taxBO.getApplyLevelId(), taxBO.getMinValue(), taxBO.getMaxValue(), taxBO.getApplyRange(), taxBO.getGroupId(), taxBO.getTaxDesc2());
    }

    private void calculateProductExcludeTax(ProductMasterBO productBO,
                                            double taxRate) {
        double taxValue = 0.0;
        double totalAppliedTaxValue = 0.0;
        //batch wise tax update
        if (productBO.getBatchwiseProductCount() > 0 && mBusinessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
            ArrayList<ProductMasterBO> batchList = mBusinessModel.batchAllocationHelper
                    .getBatchlistByProductID().get(productBO.getProductID());
            if (batchList != null) {
                for (ProductMasterBO batchProductBO : batchList) {
                    if (batchProductBO.getOrderedPcsQty() > 0
                            || batchProductBO.getOrderedCaseQty() > 0
                            || batchProductBO.getOrderedOuterQty() > 0) {
                        double batchTaxValue = batchProductBO.getNetValue() / (1 + (taxRate / 100));
                        double appliedTaxValue = batchTaxValue * taxRate / 100;

                        batchTaxValue = SDUtil.formatAsPerCalculationConfig(batchTaxValue);
                        appliedTaxValue = SDUtil.formatAsPerCalculationConfig(appliedTaxValue);

                        taxValue = taxValue + batchTaxValue;
                        totalAppliedTaxValue = totalAppliedTaxValue + appliedTaxValue;
                        batchProductBO.setTaxableAmount(batchTaxValue);
                        batchProductBO.setTaxAmount(appliedTaxValue);
                    }
                }
                productBO.setTaxableAmount(taxValue);
                productBO.setTaxAmount(totalAppliedTaxValue);
            }

        } else {
            taxValue = productBO.getNetValue() / (1 + (taxRate / 100));
            totalAppliedTaxValue = taxValue * taxRate / 100;

            taxValue = SDUtil.formatAsPerCalculationConfig(taxValue);
            totalAppliedTaxValue = SDUtil.formatAsPerCalculationConfig(totalAppliedTaxValue);

            productBO.setTaxAmount(totalAppliedTaxValue);
            productBO.setTaxableAmount(taxValue);
        }

    }

    /**
     * Metod to save product level tax
     *
     * @param orderId orderId
     */
    public void saveProductLeveltax(String orderId, DBUtil db) {

        if (mProductTaxList != null) {
            for (String productId : mProductTaxList) {
                ProductMasterBO productBo = mBusinessModel.productHelper.getProductMasterBOById(productId);
                if (productBo != null) {
                    if (productBo.getOrderedPcsQty() > 0
                            || productBo.getOrderedCaseQty() > 0
                            || productBo.getOrderedOuterQty() > 0) {
                        ArrayList<TaxBO> taxList = mTaxListByProductId
                                .get(productId);
                        if (taxList != null) {
                            int totalQty = productBo.getOrderedPcsQty()
                                    + productBo.getOrderedCaseQty()
                                    * productBo.getCaseSize()
                                    + productBo.getOrderedOuterQty()
                                    * productBo.getOutersize();
                            double totalValue = productBo
                                    .getNetValue();
                            double remainingValue = totalValue / totalQty;
                            for (TaxBO taxBO : taxList) {
                                if (mBusinessModel.configurationMasterHelper.SHOW_MRP_LEVEL_TAX) {
                                    if (taxBO.getApplyRange() == 1) {
                                        if (taxBO.getMinValue() <= remainingValue
                                                && taxBO.getMaxValue() >= remainingValue) {
                                            insertProductLevelTax(orderId, db,
                                                    productBo, taxBO);
                                        }

                                    } else if (taxBO.getApplyRange() == 0) {
                                        insertProductLevelTax(orderId, db,
                                                productBo, taxBO);
                                    }
                                } else {
                                    insertProductLevelTax(orderId, db,
                                            productBo, taxBO);
                                }

                            }
                        }

                    }
                }
            }
        }

    }


    public void updateInvoiceIdInProductLevelTax(DBUtil db, String invid,
                                                 String orderId) {
        String query = "update InvoiceTaxDetails set InvoiceId=" + StringUtils.getStringQueryParam(invid)
                + " where OrderId=" + orderId;
        db.updateSQL(query);

    }

    /**
     * Method to use insert tax details as productwise
     *
     * @param orderId   orderId
     * @param db        db
     * @param productBO productBO
     * @param taxBO     taxBO
     */
    private void insertProductLevelTax(String orderId, DBUtil db,
                                       ProductMasterBO productBO, TaxBO taxBO) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct,applyLevelId";
        String values = orderId + "," + productBO.getProductID() + ","
                + taxBO.getTaxRate() + "," + taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + mBusinessModel.getRetailerMasterBO().getRetailerID() + "," + taxBO.getGroupId() + ",0," + taxBO.getApplyLevelId();

        db.insertSQL("OrderTaxDetails", columns, values);

        if (mBusinessModel.getRetailerMasterBO().getIsVansales() == 1
                || mBusinessModel.configurationMasterHelper.IS_INVOICE)
            db.insertSQL("InvoiceTaxDetails", columns, values);
    }

    public void insertProductLevelTaxForFreeProduct(String orderId, DBUtil db,
                                                    String productId, TaxBO taxBO) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct,applyLevelId";
        String values = orderId + "," + productId + "," + taxBO.getTaxRate() + "," + taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + mBusinessModel.getRetailerMasterBO().getRetailerID() + "," + taxBO.getGroupId() + ",1," + taxBO.getApplyLevelId();

        db.insertSQL("OrderTaxDetails", columns, values);

        if (mBusinessModel.getRetailerMasterBO().getIsVansales() == 1
                || mBusinessModel.configurationMasterHelper.IS_INVOICE)
            db.insertSQL("InvoiceTaxDetails", columns, values);
    }

    /**
     * Method to apply bill wise  tax either include or exclude
     *
     * @param totalOrderValue totalOrderValue
     */
    public double applyBillWiseTax(double totalOrderValue) {
        double totalExclusiveOrderAmount = SDUtil.convertToDouble(SDUtil.format(totalOrderValue,
                mBusinessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                0, false));
        double totalTaxValue = 0.0;
        if (!mBusinessModel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
            double totalTaxRate = 0;
            for (TaxBO taxBO : mBillTaxList) {
                totalTaxRate = totalTaxRate + taxBO.getTaxRate();
            }

            totalExclusiveOrderAmount = totalOrderValue / (1 + (totalTaxRate / 100));
        }


        for (TaxBO taxBO : mBillTaxList) {
            double taxValue = totalExclusiveOrderAmount * (taxBO.getTaxRate() / 100);
            taxValue = SDUtil.formatAsPerCalculationConfig(taxValue);

            totalTaxValue = totalTaxValue + taxValue;
            taxBO.setTotalTaxAmount(taxValue);
        }
        return totalTaxValue;
    }

    public double getTotalBillTaxAmount(boolean isOrder) {
        double taxValue = 0;
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb;
            if (isOrder) {
                sb = "select sum(taxValue) from OrderTaxDetails where orderid=" + OrderHelper.getInstance(mContext).getOrderId();
            } else {
                sb = "select sum(taxValue) from InvoiceTaxDetails where invoiceid=" + StringUtils.getStringQueryParam(mBusinessModel.invoiceNumber);
            }
            Cursor c = db.selectSQL(sb);
            if (c.moveToFirst()) {
                taxValue = c.getDouble(0);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return taxValue;
    }


    @Override
    public void removeTaxFromPrice(boolean isAllProducts) {

        try {
            for (ProductMasterBO productMasterBO : mBusinessModel.productHelper.getProductMaster()) {

                productMasterBO.setOriginalSrp(productMasterBO.getSrp());

                if (isAllProducts||(productMasterBO.getOrderedCaseQty() > 0
                        || productMasterBO.getOrderedPcsQty() > 0
                        || productMasterBO.getOrderedOuterQty() > 0)) {
                if (productMasterBO.getSrp() > 0) {

                    float srpWithoutTax = SDUtil.truncateDecimal(productMasterBO.getSrp() - getTaxAmountInPrice(productMasterBO.getProductID()), 2).floatValue();

                    if (srpWithoutTax > 0)
                        productMasterBO.setSrp(srpWithoutTax);
                    else productMasterBO.setSrp(0);

                }
            }

             }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    private float getTaxAmountInPrice(String productId) {
        float taxAmount = 0;
        try {
            ProductMasterBO bo = mBusinessModel.productHelper.getProductMasterBOById(productId);
            if (mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(productId) != null) {
                for (TaxBO taxBO : mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(productId)) {
                    if (taxBO.getParentType().equals("0")) {
                        taxAmount += SDUtil.truncateDecimal(bo.getSrp() * (taxBO.getTaxRate() / 100), 2).floatValue();
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return taxAmount;
    }

    @Override
    public void applyRemovedTax(LinkedList<ProductMasterBO> mOrderedProductList) {

    }

    @Override
    public double updateProductWiseIncludeTax(List<ProductMasterBO> productMasterBOS) {
        double totalTaxAmount = 0;
        if (productMasterBOS != null && productMasterBOS.size() > 0) {
            for (ProductMasterBO productMasterBO : productMasterBOS) {
                if (productMasterBO != null) {
                    if (productMasterBO.getOrderedPcsQty() > 0
                            || productMasterBO.getOrderedCaseQty() > 0
                            || productMasterBO.getOrderedOuterQty() > 0) {

                        if (mTaxListByProductId != null && mTaxListByProductId.get(productMasterBO.getProductID()) != null) {

                            ArrayList<TaxBO> taxList = mTaxListByProductId.get(productMasterBO.getProductID());
                            if (taxList != null) {
                                double taxAmount = 0;

                                for (TaxBO taxBO : taxList) {
                                    if (taxBO.getParentType().equals("0")) {
                                        double calTax = SDUtil.truncateDecimal(productMasterBO.getNetValue() * (taxBO.getTaxRate() / 100), 2).floatValue();
                                        calTax = SDUtil.formatAsPerCalculationConfig(calTax);
                                        taxBO.setTotalTaxAmount(calTax);
                                        taxAmount += calTax;
                                    }
                                }

                                totalTaxAmount = totalTaxAmount + taxAmount;
                                productMasterBO.setTaxAmount(taxAmount);
                                productMasterBO.setTaxableAmount(productMasterBO.getNetValue());

                                productMasterBO.setNetValue(productMasterBO.getNetValue() + taxAmount);
                            }
                        }
                    }
                }
            }
        }
        return totalTaxAmount;
    }

    @Override
    public float getTaxAmountByProduct(ProductMasterBO bo) {
        float taxAmount = 0;
        try {
            if (mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) != null) {
                for (TaxBO taxBO : mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID())) {
                    if (taxBO.getParentType().equals("0")) {
                        taxAmount += taxBO.getTotalTaxAmount();
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return taxAmount;
    }
}
