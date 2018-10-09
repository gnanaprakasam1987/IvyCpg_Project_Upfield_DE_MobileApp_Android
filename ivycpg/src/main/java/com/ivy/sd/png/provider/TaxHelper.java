package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.TaxInterface;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mansoor on 17/1/18.
 */

public class TaxHelper implements TaxInterface {
    private static TaxHelper instance = null;
    private BusinessModel mBusinessModel;
    private Context mContext;

    private ArrayList<TaxBO> mBillTaxList = new ArrayList<TaxBO>();
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
        return new ArrayList<TaxBO>();
    }

    public ArrayList<TaxBO> getGroupIdList() {
        if (mGroupIdList != null) {
            return mGroupIdList;
        }
        return new ArrayList<TaxBO>();
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

    public static TaxHelper getInstance(Context context) {
        if (instance == null)
            instance = new TaxHelper(context);

        return instance;
    }

    private TaxHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
        this.mContext = context.getApplicationContext();
    }


    /**
     * @author rajesh.k Method to use download product wise tax details
     */
    public void downloadProductTaxDetails() {

        mProductTaxList = new ArrayList<String>();
        mTaxListByProductId = new HashMap<String, ArrayList<TaxBO>>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;
            int seqToSwap = 0;
            int locationId = 0;

            //Location not needed if GST applied
            if (mBusinessModel.configurationMasterHelper.IS_LOCATION_WISE_TAX_APPLIED) {
                c = db.selectSQL("SELECT (SELECT Sequence FROM LocationLevel WHERE id = " +
                        "(SELECT LocLevelId FROM LocationMaster WHERE LocId = " +
                        "(SELECT locationid FROM RetailerMaster WHERE RetailerID = '" +
                        mBusinessModel.getRetailerMasterBO().getRetailerID() + "'))) - " +
                        "(SELECT Sequence FROM LocationLevel WHERE id = " +
                        "(SELECT Id FROM LocationLevel WHERE Code = '"
                        + mBusinessModel.configurationMasterHelper.STRING_LOCATION_WISE_TAX_APPLIED + "'))");
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        seqToSwap = c.getInt(0) + 1;
                    }
                }
                c.close();

                String query = "SELECT LOC_MAS" + seqToSwap + ".LocId FROM LocationMaster LOC_MAS1";
                for (int i = 2; i <= seqToSwap; i++)
                    query += " INNER JOIN LocationMaster LOC_MAS" + i + " ON LOC_MAS"
                            + (i - 1) + ".LocParentId = LOC_MAS" + i + ".LocId";
                query += " WHERE LOC_MAS1.LocId  = '" + mBusinessModel.getRetailerMasterBO().getLocationId() + "'";
                c = db.selectSQL(query);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        locationId = c.getInt(0);
                    }
                }
                c.close();

            }

            StringBuilder sb = new StringBuilder();
            sb.append("select distinct A.pid,TM.TaxDesc,TM.taxrate,SLM.ListName,TM.TaxType,TM.minvalue,TM.maxValue,TM.applyRange,TM.groupid,ifnull(TM.parentType,0) from  productmaster A ");
            sb.append("inner JOIN ProductTaxMaster PTM on  PTM.pid = A.pid ");
            sb.append("inner JOIN TaxMaster TM on  PTM.groupid = TM.groupid ");
            sb.append("INNER JOIN StandardListMaster SLM ON SLM.Listid = TM.TaxType ");
            sb.append("inner JOIN (select listid from standardlistmaster where  listcode='ITEM' and ListType='TAX_APPLY_TYPE') SD ON TM.applylevelid =SD.listid ");
            sb.append("where PTM.TaxTypeId = "
                    + mBusinessModel.getRetailerMasterBO().getTaxTypeId());
            if (mBusinessModel.configurationMasterHelper.IS_LOCATION_WISE_TAX_APPLIED) {
                sb.append(" AND PTM.locationid = " + locationId);
            }
            sb.append("  order by A.pid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                TaxBO taxBo;
                ArrayList<TaxBO> taxList = new ArrayList<TaxBO>();

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
                    if (!productid.equals(taxBo.getPid() + "")) {
                        if (!productid.equals("")) {

                            mTaxListByProductId.put(productid, taxList);
                            taxList = new ArrayList<TaxBO>();
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
            sb = null;
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadBillWiseTaxDetails() {
        mBillTaxList = new ArrayList<TaxBO>();
        TaxBO taxBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("select TM.TaxType,TM.TaxRate,TM.Sequence,SLM.ListName,TM.ParentType,TM.applylevelid from  TaxMaster TM ");
            sb.append("inner JOIN ProductTaxMaster PTM on  PTM.groupid = TM.groupid ");
            sb.append("INNER JOIN StandardListMaster SLM ON SLM.Listid = TM.TaxType ");
            sb.append("inner JOIN (select listid from standardlistmaster where  listcode='BILL' and ListType='TAX_APPLY_TYPE') SD ON TM.applylevelid =SD.listid ");
            sb.append("where PTM.TaxTypeId = "
                    + mBusinessModel.getRetailerMasterBO().getTaxTypeId());
            sb.append(" AND PTM.PID = 0");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    taxBO = new TaxBO();
                    taxBO.setTaxType(c.getString(0));
                    taxBO.setTaxRate(c.getDouble(1));
                    taxBO.setSequence(c.getString(2));
                    taxBO.setTaxDesc(c.getString(3));
                    taxBO.setParentType(c.getString(4));
                    taxBO.setApplyLevelId(c.getInt(5));

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
     * @param invoiceid
     * @author rajesh.k Method to use insert tax details in SQLite
     */
    public void insertInvoiceTaxList(String invoiceid, DBUtil db) {

        if (mBillTaxList != null) {
            String columns = "RetailerId,invoiceid,taxRate,taxType,taxValue";
            StringBuffer sb;
            for (TaxBO taxBO : mBillTaxList) {
                sb = new StringBuffer();
                sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO()
                        .getRetailerID()) + ",");
                sb.append(mBusinessModel.QT(invoiceid) + "," + taxBO.getTaxRate() + ",");
                sb.append(mBusinessModel.QT(taxBO.getTaxType()) + ","
                        + taxBO.getTotalTaxAmount());
                db.insertSQL("InvoiceTaxDetails", columns, sb.toString());

            }
        }

    }


    /**
     * @param orderId
     * @param db
     * @author rajesh.k Method to use insert tax details in SQLite
     */
    public void insertOrderTaxList(String orderId, DBUtil db) {

        db.deleteSQL("OrderTaxDetails", "OrderID=" + orderId,
                false);
        if (mBillTaxList != null) {
            String columns = "RetailerId,orderid,taxRate,taxType,taxValue,pid";
            StringBuffer sb;
            for (TaxBO taxBO : mBillTaxList) {
                sb = new StringBuffer();
                sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO()
                        .getRetailerID()) + ",");
                sb.append(orderId + "," + taxBO.getTaxRate() + ",");
                sb.append(mBusinessModel.QT(taxBO.getTaxType()) + ","
                        + taxBO.getTotalTaxAmount());
                db.insertSQL("OrderTaxDetails", columns, sb.toString());

            }
        }

    }

    @Override
    public HashMap<String, Double> prepareProductTaxForPrint(Context context, String orderId) {
        DBUtil db = null;
        HashMap<String,Double> mTaxesApplied=new HashMap<>();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select taxType,taxRate,taxName,parentType,taxValue,pid from OrderTaxDetails IT" +
                    " where orderid="+orderId+"  order by taxType,taxRate,taxName desc");
            Cursor c = db.selectSQL(sb.toString());
            String lastTaxType="",lastTaxRate="",lastTaxName="";
            double totalTaxByType=0,totalTaxableAmountByType=0;
            while (c.moveToNext()){

                String taxType=c.getString(0);
                String taxRate=c.getString(1);
                String taxName = c.getString(2);
                double taxAmount=c.getDouble(4);
                double taxableAmount=mBusinessModel.productHelper.getProductMasterBOById(c.getString(5)).getTaxableAmount();

                if(!lastTaxType.equals("")&&!lastTaxType.equals(taxType)&&!lastTaxRate.equals(taxRate)){

                    mTaxesApplied.put(lastTaxName +" "+ lastTaxRate+"% "+context.getResources().getString(R.string.tax_on)+" "+totalTaxableAmountByType,totalTaxByType);

                    totalTaxByType=taxAmount;
                    totalTaxableAmountByType=taxableAmount;

                }
                else {
                    totalTaxByType+=taxAmount;
                    totalTaxableAmountByType+=taxableAmount;
                }

                //
                lastTaxName=taxName;
                lastTaxRate=taxRate;
                lastTaxType=taxType;

            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
        return mTaxesApplied;
    }



    /**
     * Method to use load tax information for print zebra 3inch in titan project
     *
     * @param invoiceid
     */
    public void loadTaxDetailsForPrint(String invoiceid) {
        mGroupIdList = new ArrayList<>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct IT.taxType,IT.taxRate,slm.flex1,TM.ParentType from OrderTaxDetails IT");
            sb.append(" inner join taxmaster TM on IT.groupid=TM.Groupid and IT.TaxType=TM.taxtype ");
            sb.append(" left join standardlistmaster slm on TM.taxtype=slm.listid ");
            sb.append(" where orderid=" + mBusinessModel.QT(invoiceid) + " order by IT.taxType,IT.taxRate,slm.flex1 desc");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                int groupid = 0;

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

                            taxPercentagelist = new HashSet<Double>();
                            taxPercentagelist.add(c.getDouble(2));

                            mTaxBOByGroupId.put(groupid, taxList);
                            taxList = new LinkedHashSet<TaxBO>();
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

        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct IT.taxType,pid,IT.taxRate,IT.isFreeProduct from OrderTaxDetails IT");
            sb.append(" left join taxmaster TM on IT.groupid=TM.Groupid");
            sb.append(" left join standardlistmaster slm on TM.taxtype=slm.listid ");
            sb.append(" where orderid=" + mBusinessModel.QT(invoiceid) + " and IT.isFreeProduct=0 order by IT.taxType,IT.taxRate");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String groupid = "";

                mProductIdByTaxGroupId = new LinkedHashMap<String, HashSet<String>>();
                HashSet<String> producttaxlist = new HashSet<String>();


                while (c.moveToNext()) {
                    String productid = c.getString(1);

                    if (!groupid.equals(c.getInt(0) + "" + c.getDouble(2))) {
                        if (!groupid.equals("")) {
                            mProductIdByTaxGroupId.put(groupid, producttaxlist);
                            producttaxlist = new HashSet<String>();
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
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct IT.taxType,pid,IT.taxRate,IT.isFreeProduct from invoicetaxdetails IT");
            sb.append(" left join taxmaster TM on IT.groupid=TM.Groupid");
            sb.append(" left join standardlistmaster slm on TM.taxtype=slm.listid ");
            sb.append(" where invoiceid=" + mBusinessModel.QT(invoiceid) + "and IT.isFreeProduct=1 order by IT.taxType,IT.taxRate");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String groupid = "";

                HashSet<String> producttaxlist = new HashSet<String>();


                while (c.moveToNext()) {
                    String productid = c.getString(1);

                    if (!groupid.equals(c.getInt(0) + "" + c.getDouble(2))) {
                        if (!groupid.equals("")) {
                            mFreeProductIdByTaxGroupId.put(groupid, producttaxlist);
                            producttaxlist = new HashSet<String>();
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
                                        }

                                    } else {
                                        if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                                            taxRate += taxBO.getTaxRate();
                                        }
                                    }
                                } else {
                                    if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
                                        taxRate += taxBO.getTaxRate();
                                    }
                                }
                            }
                            calculateProductExcludeTax(productBo, taxRate);

                        }
                    }
                }
            }
        }

    }


    public TaxBO cloneTaxBo(TaxBO taxBO) {

        return new TaxBO(taxBO.getTaxType(), taxBO.getTaxRate(), taxBO.getSequence(), taxBO.getTaxDesc(), taxBO.getParentType(), taxBO.getTotalTaxAmount()
                , taxBO.getPid(), taxBO.getApplyLevelId(), taxBO.getMinValue(), taxBO.getMaxValue(), taxBO.getApplyRange(), taxBO.getGroupId(), taxBO.getTaxDesc2());
    }

    @Override
    public void calculateTaxOnTax(ProductMasterBO productMasterBO, TaxBO taxBO, boolean isFreeProduct) {

    }

    @Override
    public void insertProductLevelTaxForFreeProduct(String orderId, DBUtil db, String productId, TaxBO taxBO) {

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

                        batchTaxValue=SDUtil.formatAsPerCalculationConfig(batchTaxValue);
                        appliedTaxValue=SDUtil.formatAsPerCalculationConfig(appliedTaxValue);

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

            taxValue=SDUtil.formatAsPerCalculationConfig(taxValue);
            totalAppliedTaxValue=SDUtil.formatAsPerCalculationConfig(totalAppliedTaxValue);

            productBO.setTaxAmount(totalAppliedTaxValue);
            productBO.setTaxableAmount(taxValue);
        }

    }

    /**
     * Metod to save product level tax
     *
     * @param orderId
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
        String query = "update InvoiceTaxDetails set InvoiceId=" + mBusinessModel.QT(invid)
                + " where OrderId=" + orderId;
        db.updateSQL(query);

    }

    /**
     * Method to use insert tax details as productwise
     *
     * @param orderId
     * @param db
     * @param productBO
     * @param taxBO
     */
    private void insertProductLevelTax(String orderId, DBUtil db,
                                       ProductMasterBO productBO, TaxBO taxBO) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct";
        StringBuffer values = new StringBuffer();

        values.append(orderId + "," + productBO.getProductID() + ","
                + taxBO.getTaxRate() + ",");
        values.append(taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + mBusinessModel.getRetailerMasterBO().getRetailerID());
        values.append("," + taxBO.getGroupId() + ",0");

        db.insertSQL("OrderTaxDetails", columns, values.toString());
        if (mBusinessModel.getRetailerMasterBO().getIsVansales() == 1 || mBusinessModel.configurationMasterHelper.IS_INVOICE)
            db.insertSQL("InvoiceTaxDetails", columns, values.toString());


    }

    /**
     * Method to apply bill wise  tax either include or exclude
     *
     * @param totalOrderValue
     */
    public double applyBillWiseTax(double totalOrderValue) {
        double totalExclusiveOrderAmount = totalOrderValue;
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
            totalTaxValue = totalTaxValue + SDUtil.formatAsPerCalculationConfig(taxValue);
            taxBO.setTotalTaxAmount(SDUtil.formatAsPerCalculationConfig(taxValue));
        }
        return totalTaxValue;
    }

    public double getTotalBillTaxAmount(boolean isOrder) {
        double taxValue = 0;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            if (isOrder) {
                sb.append("select sum(taxValue) from OrderTaxDetails ");
                sb.append("where orderid=" + OrderHelper.getInstance(mContext).getOrderId());
            } else {
                sb.append("select sum(taxValue) from InvoiceTaxDetails ");
                sb.append("where invoiceid=" + mBusinessModel.QT(mBusinessModel.invoiceNumber));
            }
            Cursor c = db.selectSQL(sb.toString());
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
    public void removeTaxFromPrice() {
        try {
            for (ProductMasterBO productMasterBO : mBusinessModel.productHelper.getProductMaster()) {

                productMasterBO.setOriginalSrp(productMasterBO.getSrp());

                if (productMasterBO.getOrderedCaseQty() > 0
                        || productMasterBO.getOrderedPcsQty() > 0
                        || productMasterBO.getOrderedOuterQty() > 0) {
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

        for (ProductMasterBO bo : mOrderedProductList) {
            float finalAmount = 0;

            if (mBusinessModel.productHelper.taxHelper.getmTaxListByProductId() != null) {
                if (mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) != null) {
                    for (TaxBO taxBO : mBusinessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID())) {
                        if (taxBO.getParentType().equals("0")) {
                            double taxValue=bo.getNetValue() * (taxBO.getTaxRate() / 100);
                            finalAmount += SDUtil.formatAsPerCalculationConfig(taxValue);

                        }
                    }
                }
            }

            bo.setNetValue((bo.getNetValue() + finalAmount));
        }

    }

    @Override
    public double updateProductWiseIncludeTax(List<ProductMasterBO> productMasterBOS) {
        double totalTaxAmount = 0;
        if (productMasterBOS != null && productMasterBOS.size() > 0) {
            for (ProductMasterBO productMasterBO : productMasterBOS) {
                ProductMasterBO productBo = productMasterBO;
                if (productBo != null) {
                    if (productBo.getOrderedPcsQty() > 0
                            || productBo.getOrderedCaseQty() > 0
                            || productBo.getOrderedOuterQty() > 0) {

                        if (mTaxListByProductId != null && mTaxListByProductId.get(productBo.getProductID()) != null) {

                            ArrayList<TaxBO> taxList = mTaxListByProductId.get(productBo.getProductID());
                            if (taxList != null) {
                                double taxAmount = 0;
                                for (TaxBO taxBO : taxList) {
                                    if (taxBO.getParentType().equals("0")) {
                                        double calTax;
                                        calTax=SDUtil.formatAsPerCalculationConfig(productBo.getNetValue() * (taxBO.getTaxRate() / 100));

                                        taxBO.setTotalTaxAmount(calTax);
                                        taxAmount += calTax;
                                    }
                                }

                                totalTaxAmount = totalTaxAmount + taxAmount;
                                productBo.setTaxAmount(taxAmount);
                                productBo.setTaxableAmount(productBo.getNetValue());

                                productBo.setNetValue(productBo.getNetValue() + taxAmount);
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
            if (mBusinessModel.productHelper.taxHelper.getmTaxListByProductId() != null)
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