package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by ramkumard on 17/4/18
 */

public class SBDHelper {

    private BusinessModel bmodel;
    private static SBDHelper instance = null;
    private HashMap<String, HashMap<Integer, Integer>> mProductIdListByGroupName;
    private HashMap<String, HashMap<Integer, Integer>> mHistoryOrderedListByGroupName;
    private HashMap<String, String> mParentHierarchyByProductId = new HashMap<>();

    private SBDHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static SBDHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SBDHelper(context);
        }
        return instance;
    }

    /**
     * This method will set whether product is RPS product or not. Since its
     * based on the subchannel we are setting this after selecting the retailer.
     */
    public void loadSBDFocusData(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Vector<Integer> sbdDist = new Vector<>();
            // Order Header
            String sql = "select productid from SbdDistributionMaster where Channelid in (0,"
                    + bmodel.getChannelids() + ")";
            Cursor c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    sbdDist.add(c.getInt(0));
                }
                c.close();
            }

            Vector<Integer> sbdDistAcheived = new Vector<Integer>();
            // Order Header
            String sql1 = "select productid from SbdDistributionMaster where Channelid in (0,"
                    + bmodel.getChannelids()
                    + ") and grpName in (select gname from SbdDistributionAchievedMaster where rid="
                    + bmodel.retailerMasterBO.getRetailerID() + ")";
            Cursor c1 = db.selectSQL(sql1);

            if (c1 != null) {
                while (c1.moveToNext()) {
                    sbdDistAcheived.add(c1.getInt(0));
                }
                c1.close();
            }

            db.closeDB();

            for (int i = 0; i < bmodel.productHelper.getProductMaster().size(); i++) {
                ProductMasterBO p = bmodel.productHelper.getProductMaster().get(i);
                Commons.print("sbdDist : " + sbdDist + "sbdDistAcheived : "
                        + sbdDistAcheived + "getProductID :  "
                        + p.getProductID());

                if (sbdDist.contains(Integer.valueOf(p.getProductID()))) {
                    p.setSBDProduct(true);
                    bmodel.productHelper.getProductMaster().setElementAt(p, i);
                }
                if (sbdDistAcheived.contains(Integer.valueOf(p.getProductID()))) {
                    p.setSBDAcheived(true);
                    bmodel.productHelper.getProductMaster().setElementAt(p, i);
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    public HashMap<String, HashMap<String, String>> calculateSBDDistribution(Context mContext) {
        HashMap<String, HashMap<String, String>> mSBDGapProductsByGroup = new HashMap<>();
        try {
            HashMap<String, HashMap<Integer, Integer>> mProductIdListByGroupName = new HashMap<>();
            HashMap<Integer, Integer> mOrderedQtyByProductId = new HashMap<>();
            HashMap<String, HashMap<Integer, Integer>> mHistoryOrderedListByGroupName = new HashMap<>();
            int achievedGroupCount = 0;
            ArrayList<String> mAchievedGroupNames = new ArrayList<>();

            HashMap<String, String> mParentHierarchyByProductId = new HashMap<>();
            HashMap<String, String> mProductNameByProductId = new HashMap<>();

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select grpName,ProductId,DrpQty,PM.pname from SbdDistributionMaster SM left join ProductMaster PM ON PM.pid=SM.ProductId"
                    + " where channelId in(0," + bmodel.getChannelids() + ")");
            if (c != null) {
                while (c.moveToNext()) {
                    if (mProductIdListByGroupName.get(c.getString(0)) != null) {
                        mProductIdListByGroupName.get(c.getString(0)).put(c.getInt(1), c.getInt(2));
                    } else {
                        HashMap<Integer, Integer> mQtyByProduct = new HashMap<>();
                        mQtyByProduct.put(c.getInt(1), c.getInt(2));
                        mProductIdListByGroupName.put(c.getString(0), mQtyByProduct);
                    }

                    mProductNameByProductId.put(c.getString(1), c.getString(3));
                }
                c.close();
                setmProductIdListByGroupName(mProductIdListByGroupName);
            }

            String query = "select OD.productId,OD.qty,PM.ParentHierarchy from " + DataMembers.tbl_orderDetails + " OD left join ProductMaster PM ON PM.pid=OD.productId"
                    + " where retailerId=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {

                    if (mOrderedQtyByProductId.get(c.getInt(0)) != null) {
                        int qty = mOrderedQtyByProductId.get(c.getInt(0));
                        mOrderedQtyByProductId.put(c.getInt(0), (qty + c.getInt(1)));
                    } else
                        mOrderedQtyByProductId.put(c.getInt(0), c.getInt(1));

                    mParentHierarchyByProductId.put(c.getString(0), c.getString(2));

                }
            }

            query = "select SM.pid,SM.gname,SM.pcsQty,PM.ParentHierarchy from SbdDistributionAchievedMaster SM left join ProductMaster PM ON PM.pid=SM.pid"
                    + " where rid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            c = db.selectSQL(query);
            if (c != null) {
                setmParentHierarchyByProductId(new HashMap<String, String>());
                while (c.moveToNext()) {

                    if (mHistoryOrderedListByGroupName.get(c.getString(1)) != null) {
                        HashMap<Integer, Integer> mProductList = mHistoryOrderedListByGroupName.get(c.getString(1));
                        if (mProductList.get(c.getInt(0)) != null) {
                            int qty = mProductList.get(c.getInt(0));
                            mProductList.put(c.getInt(0), (qty + c.getInt(2)));
                        } else
                            mProductList.put(c.getInt(0), c.getInt(2));
                    } else {
                        HashMap<Integer, Integer> mProductList = new HashMap<>();
                        mProductList.put(c.getInt(0), c.getInt(2));
                        mHistoryOrderedListByGroupName.put(c.getString(1), mProductList);
                    }

                    mParentHierarchyByProductId.put(c.getString(0), c.getString(3));
                    getmParentHierarchyByProductId().put(c.getString(0), c.getString(3));
                }
                setmHistoryOrderedListByGroupName(mHistoryOrderedListByGroupName);
                c.close();
            }


            for (String groupName : mProductIdListByGroupName.keySet()) {
                HashMap<Integer, Integer> mProductList = mProductIdListByGroupName.get(groupName);
                for (int productId : mProductList.keySet()) {
                    int orderedQty = 0;

                    //calculating ordered qty
                    for (int orderedPid : mOrderedQtyByProductId.keySet()) {
                        String productHierarchy = mParentHierarchyByProductId.get(orderedPid + "");
                        List<String> hierarchy = Arrays.asList(productHierarchy.split("/"));
                        if (mOrderedQtyByProductId.get(productId) != null
                                || hierarchy.contains(productId + "")) {

                            orderedQty += mOrderedQtyByProductId.get(orderedPid);
                        }
                    }


                    // calculating ordered qty from history records
                    HashMap<Integer, Integer> mHistoryProductList = mHistoryOrderedListByGroupName.get(groupName);
                    if (mHistoryProductList != null) {
                        for (int historyPid : mHistoryProductList.keySet()) {
                            String productHierarchy = mParentHierarchyByProductId.get(historyPid + "");
                            List<String> hierarchyList = new ArrayList<>();
                            if (productHierarchy != null)
                                hierarchyList = Arrays.asList(productHierarchy.split("/"));

                            if (mHistoryProductList.get(productId) != null) {

                                orderedQty += mHistoryProductList.get(historyPid);
                            } else if (productHierarchy != null && hierarchyList.contains(productId + "")) {
                                orderedQty += mHistoryProductList.get(historyPid);
                            }
                        }
                    }


                    if (orderedQty >= mProductList.get(productId)) {
                        achievedGroupCount += 1;
                        mAchievedGroupNames.add(groupName);
                        break;
                    }

                }

            }


            for (String groupName : mProductIdListByGroupName.keySet()) {
                if (!mAchievedGroupNames.contains(groupName)) {
                    HashMap<String, String> temp = new HashMap<>();

                    for (int productId : mProductIdListByGroupName.get(groupName).keySet()) {
                        temp.put(mProductNameByProductId.get(String.valueOf(productId))
                                , String.valueOf(mProductIdListByGroupName.get(groupName).get(productId)));
                    }

                    mSBDGapProductsByGroup.put(groupName, temp);
                }
            }


            bmodel.getRetailerMasterBO().setSbd_dist_achieve(achievedGroupCount);
            bmodel.getRetailerMasterBO().setSbd_dist_target(mProductIdListByGroupName.size());


        } catch (Exception ex) {

            Commons.printException(ex);
        }


        return mSBDGapProductsByGroup;
    }

    public int getAchievedSBD(Vector<ProductMasterBO> orderedList) {
        int achievedGroupCount = 0;
        try {

            if (getmProductIdListByGroupName() != null) {
                for (String groupName : getmProductIdListByGroupName().keySet()) {
                    HashMap<Integer, Integer> mProductList = getmProductIdListByGroupName().get(groupName);
                    for (int productId : mProductList.keySet()) {
                        int orderedQty = 0;

                        //calculating ordered qty
                        for (ProductMasterBO orderedProduct : orderedList) {
                            String productHierarchy = orderedProduct.getParentHierarchy();
                            List<String> hierarchy = Arrays.asList(productHierarchy.split("/"));
                            if (hierarchy.contains(productId + "")) {

                                orderedQty += getOrderedQty(orderedProduct);
                            }
                        }

                        // calculating ordered qty from history records
                        HashMap<Integer, Integer> mHistoryProductList = getmHistoryOrderedListByGroupName().get(groupName);
                        if (mHistoryProductList != null) {
                            for (int historyPid : mHistoryProductList.keySet()) {
                                String parentHierarchy = getmParentHierarchyByProductId().get(historyPid + "");
                                List<String> hierarchyList = new ArrayList<>();
                                if (parentHierarchy != null)
                                    hierarchyList = Arrays.asList(parentHierarchy.split("/"));

                                if (mHistoryProductList.get(productId) != null) {

                                    orderedQty += mHistoryProductList.get(historyPid);
                                } else if (parentHierarchy != null && hierarchyList.contains(productId + "")) {
                                    orderedQty += mHistoryProductList.get(historyPid);
                                }
                            }
                        }

                        if (orderedQty >= mProductList.get(productId)) {
                            achievedGroupCount += 1;
                            break;
                        }

                    }

                }
            }
        } catch (Exception ex) {

            Commons.printException(ex);
        }


        return achievedGroupCount;
    }


    private int getOrderedQty(ProductMasterBO productMasterBO) {
        int pieceCount = 0;
        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (productMasterBO.getBatchwiseProductCount() > 0) {
                ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
                        .getBatchlistByProductID().get(
                                productMasterBO.getProductID());
                if (batchList != null) {
                    for (ProductMasterBO batchProductBO : batchList) {
                        if (batchProductBO.getOrderedPcsQty() > 0
                                || batchProductBO
                                .getOrderedCaseQty() > 0
                                || batchProductBO
                                .getOrderedOuterQty() > 0) {
                            pieceCount = batchProductBO.getOrderedPcsQty()
                                    + batchProductBO.getOrderedCaseQty()
                                    * productMasterBO.getCaseSize()
                                    + batchProductBO.getOrderedOuterQty()
                                    * productMasterBO.getOutersize();
                        }
                    }
                }
            } else {
                pieceCount = productMasterBO.getOrderedPcsQty()
                        + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                        + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
            }

        } else {
            pieceCount = productMasterBO.getOrderedPcsQty()
                    + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                    + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
        }

        return pieceCount;
    }

    private HashMap<String, HashMap<Integer, Integer>> getmProductIdListByGroupName() {
        return mProductIdListByGroupName;
    }

    private void setmProductIdListByGroupName(HashMap<String, HashMap<Integer, Integer>> mProductIdListByGroupName) {
        this.mProductIdListByGroupName = mProductIdListByGroupName;
    }

    private HashMap<String, HashMap<Integer, Integer>> getmHistoryOrderedListByGroupName() {
        return mHistoryOrderedListByGroupName;
    }

    private void setmHistoryOrderedListByGroupName(HashMap<String, HashMap<Integer, Integer>> mHistoryOrderedListByGroupName) {
        this.mHistoryOrderedListByGroupName = mHistoryOrderedListByGroupName;
    }

    private HashMap<String, String> getmParentHierarchyByProductId() {
        return mParentHierarchyByProductId;
    }

    private void setmParentHierarchyByProductId(HashMap<String, String> mParentHierarchyByProductId) {
        this.mParentHierarchyByProductId = mParentHierarchyByProductId;
    }

    public void saveDayTarget(int kpiid, double target,Context mContext){

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select kpiid from RetailerKPIModifiedDetail where kpiid =" + kpiid);
            String columnName = "KPIId,KPIParamLovId,Target";
            boolean flag = false;

            if (c != null){
                if (c.moveToNext())
                    flag = true;
            }

            if (flag)
            db.updateSQL("update RetailerKPIModifiedDetail set target =" + target + " where kpiid =" + kpiid);
            else {
                String values = bmodel.getRetailerMasterBO().getKpiid_day()
                                + ","
                                + bmodel.getRetailerMasterBO().getKpi_param_day()
                                + ","
                                + target;
                db.insertSQL("RetailerKPIModifiedDetail", columnName, values);
            }
            bmodel.getRetailerMasterBO().setDaily_target(target);
            db.closeDB();
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }
}
