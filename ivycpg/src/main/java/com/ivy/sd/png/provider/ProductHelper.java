package com.ivy.sd.png.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.LoyaltyBO;
import com.ivy.sd.png.bo.LoyaltyBenifitsBO;
import com.ivy.sd.png.bo.NearExpiryDateBO;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SalesReturnReasonBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SerialNoBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.StoreWsieDiscountBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.bo.TaxTempBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

public class ProductHelper {
    public int locid = 0, chid = 0;
    public int achLevelID = 0, totLevelID = 0;//used for call analysis

    private Context mContext;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> productMaster;
    private Vector<ProductMasterBO> shortProductMaster;

    private Map<String, ProductMasterBO> productMasterById;


    private static final String CASH_TYPE = "CASH";

    private static ProductHelper instance = null;
    private float buffer = 0;
    private Map<String, Integer> oldBatchId;
    private Map<String, Double> oldBatchBasePrice;

    public ArrayList<LocationBO> locations;
    public ArrayList<LoyaltyBO> lotyPointsList;
    private ArrayList<LoyaltyBenifitsBO> ltyBenifitsList;
    private ArrayList<NearExpiryDateBO> dateList = new ArrayList<NearExpiryDateBO>();
    private Vector<ProductMasterBO> mTaggedProducts;
    private Map<String, ProductMasterBO> mTaggedProductById;
    private ArrayList<Integer> mIndicativeList;
    private ArrayList<TaxBO> mTaxList = new ArrayList<TaxBO>();
    private ArrayList<TaxBO> mTaxProdList = new ArrayList<TaxBO>();
    private ArrayList<TaxTempBO> mTaxSumProdList = new ArrayList<TaxTempBO>();
    public String mHomeScreenThreeActivityName = "";
    private Vector<StandardListBO> inStoreLocation = new Vector<StandardListBO>();
    private Vector<LevelBO> globalCategory = new Vector<LevelBO>();
    private Vector<LoyaltyBO> loyaltyproductList = new Vector<LoyaltyBO>();


    private Vector<ParentLevelBo> mParentLevelBo;
    private Vector<ChildLevelBo> mChildLevelBo;
    private Vector<ParentLevelBo> mRetailerModuleparentLevelBO;
    private Vector<ChildLevelBo> mRetailerModuleChildLevelBO;

    private Vector<ParentLevelBo> plevelMaster;

    private Vector<LevelBO> pfilterlevel;
    private HashMap<Integer, Vector<LevelBO>> mfilterlevelBo;
    private Vector<LevelBO> sequencevalues;
    private HashMap<Integer, Vector<LevelBO>> mRetailerModuleFilterObjectBySequence;
    private Vector<LevelBO> mrRetailerModuleSequence;

    private HashMap<Integer, ArrayList<StoreWsieDiscountBO>> mProductIdListByDiscoutId;
    private ArrayList<Integer> mDiscountIdList;

    private HashMap<String, HashMap<Integer, Double>> mDiscountmapByProductwithBathid = new HashMap<String, HashMap<Integer, Double>>();
    private SparseArray<ArrayList<Integer>> mDiscountIdListByTypeid;
    private ArrayList<Integer> mTypeIdList;
    private HashMap<Integer, String> mDescriptionByTypeId;

    // Bill wise discount details list and hashmap
    private ArrayList<StoreWsieDiscountBO> mBillWiseDiscountList;
    // Bill wise  payterm discount details list and hashmap
    private ArrayList<StoreWsieDiscountBO> mBillWisePayternDiscountList;


    // Item level tax details list and hashmap initialization

    private ArrayList<String> mProductTaxList;


    private HashMap<String, ArrayList<TaxBO>> mTaxListByProductId;

    public HashMap<String, ArrayList<TaxBO>> getmTaxListByProductId() {
        return mTaxListByProductId;
    }


    private ArrayList<TaxBO> mGroupIdList;
    private LinkedHashMap<String, HashSet<String>> mProductIdByTaxGroupId;
    private LinkedHashMap<Integer, HashSet<Double>> mTaxPercentagerListByGroupId;
    private SparseArray<LinkedHashSet<TaxBO>> mTaxBOByGroupId;
    private SparseArray<ArrayList<SerialNoBO>> mSerialNoListByProductid;
    private SparseArray<LoadManagementBO> mLoadManagementBOByProductId;


    public int getmSelectedLocationIndex() {
        return mSelectedLocationIndex;
    }

    public void setmSelectedLocationIndex(int mSelectedLocationIndex) {
        this.mSelectedLocationIndex = mSelectedLocationIndex;
    }

    public int mSelectedLocationIndex = 0;
    private int mSelectedGLobalLocationIndex = 0;
    private int mSelectedGlobalLevelID = 0;
    private int mSelectedGlobalProductId = 0;
    private int mLoadedGlobalProductId = 0;


    public int getmSelectedGLobalLocationIndex() {
        return mSelectedGLobalLocationIndex;
    }

    public void setmSelectedGLobalLocationIndex(int mSelectedGLobalLocationIndex) {
        this.mSelectedGLobalLocationIndex = mSelectedGLobalLocationIndex;
    }

    public Vector<StandardListBO> getInStoreLocation() {
        return inStoreLocation;
    }

    public int getmSelectedGLobalLevelID() {
        return mSelectedGlobalLevelID;
    }

    public void setmSelectedGLobalLevelID(int mSelectedGlobalLevelID) {
        this.mSelectedGlobalLevelID = mSelectedGlobalLevelID;
    }

    public int getmSelectedGlobalProductId() {
        return mSelectedGlobalProductId;
    }

    public void setmSelectedGlobalProductId(int mSelectedGlobalProductId) {
        this.mSelectedGlobalProductId = mSelectedGlobalProductId;
    }

    public int getmLoadedGlobalProductId() {
        return mLoadedGlobalProductId;
    }

    public void setmLoadedGlobalProductId(int mLoadedGlobalProductId) {
        this.mLoadedGlobalProductId = mLoadedGlobalProductId;
    }


    private ArrayList<ProductMasterBO> mIndicateOrderList = new ArrayList<ProductMasterBO>();

    private LinkedList<String> mProductidOrderByEntry = new LinkedList<>();

    public HashMap<Integer, Integer> getmProductidOrderByEntryMap() {
        return mProductidOrderByEntryMap;
    }

    public void setmProductidOrderByEntryMap(HashMap<Integer, Integer> mProductidOrderByEntryMap) {
        this.mProductidOrderByEntryMap = mProductidOrderByEntryMap;
    }

    private HashMap<Integer, Integer> mProductidOrderByEntryMap = new HashMap<>();

    private ProductHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
        productMaster = new Vector<ProductMasterBO>();
        mTaggedProducts = new Vector<ProductMasterBO>();
        // bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static ProductHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ProductHelper(context);
        }
        return instance;
    }

    public Vector<ParentLevelBo> getRetailerModuleParentLeveBO() {
        if (mRetailerModuleparentLevelBO != null) {
            return mRetailerModuleparentLevelBO;
        }
        return new Vector<ParentLevelBo>();
    }

    public Vector<ChildLevelBo> getRetailerModuleChildLevelBO() {
        if (mRetailerModuleChildLevelBO != null) {
            return mRetailerModuleChildLevelBO;
        }
        return new Vector<ChildLevelBo>();
    }

    public Vector<ParentLevelBo> getParentLevelBo() {
        if (mParentLevelBo != null) {
            return mParentLevelBo;
        }
        return new Vector<ParentLevelBo>();
    }

    public Vector<ParentLevelBo> getPlevelMaster() {
        return plevelMaster;
    }

    public void setPlevelMaster(Vector<ParentLevelBo> plevelMaster) {
        this.plevelMaster = plevelMaster;
    }

    public Vector<ChildLevelBo> getChildLevelBo() {
        return mChildLevelBo;
    }

    private void setChildLevelBo(Vector<ChildLevelBo> mChildLevelBo) {
        this.mChildLevelBo = mChildLevelBo;
    }

    public Vector<ProductMasterBO> getProductMaster() {
        return productMaster;
    }

    public Vector<ProductMasterBO> getTaggedProducts() {
        return mTaggedProducts;
    }

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    /**
     * Load Past 4 Order and past 4 stock.
     */
    public void loadRetailerWiseProductWiseP4StockAndOrderQty() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            /*SparseArray<String> hashMap = new SparseArray<String>();
            SparseArray<String> hashMap1 = new SparseArray<String>();
            SparseIntArray oosMap = new SparseIntArray();*/

            HashMap<String, String> hashMap = new HashMap<>();
            HashMap<String, String> hashMap1 = new HashMap<>();
            HashMap<String, Integer> oosMap = new HashMap<>();

            String sql = "select pid,Ordp4,Stkp4,OOS from RtrWiseP4OrderAndStockMaster where rid="
                    + QT(bmodel.retailerMasterBO.getRetailerID()) + "";

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {

                    hashMap.put(c.getString(0), c.getString(1));
                    hashMap1.put(c.getString(0), c.getString(2));
                    oosMap.put(c.getString(0), c.getInt(3));
                }
                c.close();
            }
            db.closeDB();

            for (ProductMasterBO p : productMaster) {
                String value = hashMap
                        .get(p.getProductID());
                if (value != null) {
                    p.setRetailerWiseProductWiseP4Qty(value);
                    p.setRetailerWiseP4StockQty(hashMap1.get(p.getProductID()));
                    p.setOos(oosMap.get(p.getProductID()));
                } else {
                    p.setRetailerWiseProductWiseP4Qty("0,0,0,0");
                    p.setRetailerWiseP4StockQty("0,0,0,0");
                    p.setOos(-2);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * To load whether the sku is purchased in last 3 months or not in
     * ProductMasterBO.
     */
    public void loadRetailerWiseProductWisePurchased() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            //SparseArray<Integer> hashMap = new SparseArray<Integer>();
            HashMap<String, Integer> hashMap = new HashMap<>();

            String sql = "select pid,flag from RtrWiseProductWisePurchased where rid="
                    + QT(bmodel.retailerMasterBO.getRetailerID()) + "";

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    hashMap.put(c.getString(0), c.getInt(1));
                }
                c.close();
            }
            db.closeDB();
            for (ProductMasterBO p : productMaster) {
                Integer value = hashMap.get(p.getProductID());
                if (value != null) {
                    p.setIsPurchased(value);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * This method will load the Norm value to ProductBO and set the value in
     * setSoInventory variable. this Norm is used to calculate Suggested
     * Order(SO). RtrWiseInventoryMaster table is refereed by this method.
     */
    public void loadRetailerWiseInventoryFlexQty() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql = "select productid,qty,Rfield1,Rfield2,(((qty+RField1)*1.00)/RField2)as avgdays from RtrWiseInventoryMaster "
                    + " where retailerid="
                    + QT(bmodel.retailerMasterBO.getRetailerID()) + " and (avgdays <= 10 or avgdays > 50)";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    setRtrFlexDetails(c.getString(0), c.getInt(1), c.getInt(2), c.getInt(2), c.getString(3));
                }
                c.close();
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
    private void setRtrFlexDetails(String productid, int qty,
                                   int rField1, int rField2, String calulatedValue) {

        //mTaggedProducts list only used in StockCheck screen. So updating only in mTaggedProducts
        ProductMasterBO product = null;

        product = getTaggedProductBOById(productid);

        if (product != null) {
            if (product.getProductID().equals(productid)) {
                product.setQty_klgs(qty);
                product.setRfield1_klgs(rField1);
                product.setRfield2_klgs(rField2);
                product.setCalc_klgs(calulatedValue);

                return;
            }
        }


    }

    /**
     * This method will load the Norm value to ProductBO and set the value in
     * setSoInventory variable. this Norm is used to calculate Suggested
     * Order(SO). RtrWiseInventoryMaster table is refereed by this method.
     */
    public void loadRetailerWiseInventoryOrderQty() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            SparseArray<Integer> hasmap = new SparseArray<Integer>();
            String sql = "select productid,qty from RtrWiseInventoryMaster "
                    + " where retailerid="
                    + QT(bmodel.retailerMasterBO.getRetailerID()) + "";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    hasmap.put(c.getInt(0), c.getInt(1));
                }
                c.close();
            }
            db.closeDB();

            for (int i = 0; i < productMaster.size(); i++) {
                ProductMasterBO p = productMaster.get(i);
                int ico = 0;

                Integer value = hasmap
                        .get(SDUtil.convertToInt(p.getProductID()));
                if (value != null)
                    ico = value;

                p.setICO(ico);
                int size = p.getLocations().size();
                int shelfpcs = 0, whpcs = 0;
                for (int j = 0; j < size; j++) {
                    shelfpcs = shelfpcs
                            + p.getLocations().get(j).getShelfPiece();
                    whpcs = whpcs + p.getLocations().get(j).getWHPiece();

                }
                p.setSoInventory(calculateSO(ico, shelfpcs + whpcs, p.isRPS(),
                        p.getIsInitiativeProduct(), p.getDropQty(),
                        p.getInitDropSize()));


                productMaster.setElementAt(p, i);
                ico = 0;
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Duplicate the list
     *
     * @param list list
     * @return clone list
     */
    private static ArrayList<NearExpiryDateBO> cloneDateList(
            ArrayList<NearExpiryDateBO> list) {
        ArrayList<NearExpiryDateBO> clone = new ArrayList<NearExpiryDateBO>(
                list.size());
        try {
            for (NearExpiryDateBO item : list)
                clone.add(new NearExpiryDateBO(item));
            return clone;
        } catch (Exception e) {
            Commons.printException(e);
            return clone;
        }
    }

    /**
     * Duplicate the Location List
     *
     * @param list list
     * @return clone list
     */
    public static ArrayList<LocationBO> cloneLocationList(
            ArrayList<LocationBO> list) {
        ArrayList<LocationBO> clone = new ArrayList<LocationBO>(list.size());
        for (LocationBO item : list)
            clone.add(new LocationBO(item));
        return clone;
    }

    public void getLocations() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            locations = new ArrayList<LocationBO>();
            LocationBO locBO;
            Cursor locCur = null;

            // Load All Locations
            if (bmodel.getCounterId() == 0) {
                locCur = db
                        .selectSQL("SELECT ListId FROM StandardListMaster"
                                + " WHERE listtype ='PL' ORDER BY ListId");
            } else {
                locCur = db
                        .selectSQL("SELECT ListId FROM StandardListMaster WHERE ListType = 'COUNTER_STOCK_TYPE'");
            }

            if (locCur != null) {
                while (locCur.moveToNext()) {
                    locBO = new LocationBO();
                    locBO.setLocationId(locCur.getInt(0));
                    locations.add(locBO);
                }
                locCur.close();
                db.closeDB();
            }

            if (bmodel.getCounterId() != 0) {
                locBO = new LocationBO();
                locBO.setLocationId(0);
                locations.add(0, locBO);
            }

            // Set 0 for Default Location if No Locations
            if (locations.size() == 0) {
                locBO = new LocationBO();
                locBO.setLocationId(0);
                locations.add(locBO);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void generateDate() {

        NearExpiryDateBO bo;
        dateList.clear();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        bo = new NearExpiryDateBO();
        String dateF = df.format(c.getTime());
        bo.setDate(dateF);
        bo.setDateID(0);
        dateList.add(bo);

        for (int i = 1; i <= 5; i++) {
            c.add(Calendar.MONTH, 1);
            dateF = df.format(c.getTime());
            bo = new NearExpiryDateBO();
            bo.setDate(dateF);
            bo.setDateID(i);
            dateList.add(bo);
        }
    }

    public HashMap<Integer, Vector<LevelBO>> getFiveLevelFilters() {

        return mfilterlevelBo;

    }

    public Vector<LevelBO> getSequenceValues() {
        return sequencevalues;

    }

    public HashMap<Integer, Vector<LevelBO>> getRetailerModuleFilerContentBySequenct() {
        return mRetailerModuleFilterObjectBySequence;
    }

    public Vector<LevelBO> getRetailerModuleSequenceValues() {
        return mrRetailerModuleSequence;
    }

    @SuppressLint("UseSparseArrays")
    public void downloadFiveLevelFilterNonProducts(String moduleName) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor listCursor = db
                .selectSQL(" SELECT distinct PL.LevelID , PL.LevelName ,  PL.Sequence FROM ProductLevel  PL "
                        + " INNER JOIN ConfigActivityFilter CA  ON "
                        + " PL.LevelID =CA.ProductFilter1 OR  "
                        + " PL.LevelID =CA.ProductFilter2 OR  "
                        + " PL.LevelID =CA.ProductFilter3 OR  "
                        + " PL.LevelID =CA.ProductFilter4 OR  "
                        + " PL.LevelID =CA.ProductFilter5  "
                        + " WHERE  CA.ActivityCode='" + moduleName + "'");

        LevelBO mLevelBO;
        mrRetailerModuleSequence = new Vector<>();
        while (listCursor.moveToNext()) {

            mLevelBO = new LevelBO();
            mLevelBO.setProductID(listCursor.getInt(0));
            mLevelBO.setLevelName(listCursor.getString(1));
            mLevelBO.setSequence(listCursor.getInt(2));

            mrRetailerModuleSequence.add(mLevelBO);
        }

        listCursor.close();

        mRetailerModuleFilterObjectBySequence = new HashMap<>();
        try {

            if (mrRetailerModuleSequence.size() > 0) {
                loadParentFilter(moduleName, mrRetailerModuleSequence.get(0)
                        .getProductID());
                for (int i = 1; i < mrRetailerModuleSequence.size(); i++) {
                    loadChildFilter(mrRetailerModuleSequence.get(i).getSequence(),
                            mrRetailerModuleSequence.get(i - 1).getSequence(),
                            moduleName, mrRetailerModuleSequence.get(i).getProductID(),
                            mrRetailerModuleSequence.get(i - 1).getProductID());
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void loadParentFilter(String mModuleCode, int mProductLevelId) {
        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mProductLevelId - mSelectedGlobalLevelID + 1;

            query = "SELECT DISTINCT PM" + filterGap + ".PID, PM" + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mSelectedGlobalLevelID + " and PM1.PID =" + mSelectedGlobalProductId;

        } else {
            query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1"
                    + " WHERE PM1.PLid = " + mProductLevelId + " Order By PM1.RowId";
        }


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        Cursor c = db.selectSQL(query);

        if (c != null) {
            pfilterlevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setProductID(c.getInt(0));
                mLevelBO.setLevelName(c.getString(1));
                pfilterlevel.add(mLevelBO);
            }
            if (isRetailerModule(mModuleCode)) {
                mRetailerModuleFilterObjectBySequence.put(mProductLevelId,
                        pfilterlevel);
            } else {
                mfilterlevelBo.put(mProductLevelId, pfilterlevel);
            }
            c.close();
        }
        db.close();
    }

    private void loadChildFilter(int mChildLevel, int mParentLevel,
                                 String mModuleCode, int mProductLevelId, int mParentLevelId) {
        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mChildLevel - bmodel.configurationMasterHelper.globalSeqId + 1;
            int PM1Level = mParentLevel - bmodel.configurationMasterHelper.globalSeqId + 1;

            query = "SELECT DISTINCT PM" + PM1Level + ".PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mSelectedGlobalLevelID + " AND PM1.PID = " + mSelectedGlobalProductId;

        } else {

            int filterGap = mChildLevel - mParentLevel + 1;

            query = "SELECT DISTINCT PM1.PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mParentLevelId;
        }

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        db.openDataBase();

        Cursor c = db.selectSQL(query);

        if (c != null) {
            pfilterlevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setParentID(c.getInt(0));
                mLevelBO.setProductID(c.getInt(1));
                mLevelBO.setLevelName(c.getString(2));
                pfilterlevel.add(mLevelBO);
            }

            if (isRetailerModule(mModuleCode)) {
                mRetailerModuleFilterObjectBySequence.put(mProductLevelId,
                        pfilterlevel);
            } else {
                mfilterlevelBo.put(mProductLevelId, pfilterlevel);
            }
            c.close();
            db.close();
        }
    }

    @SuppressLint("UseSparseArrays")
    public void downloadFiveFilterLevels(String moduleName) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        Cursor listCursor = db
                .selectSQL(" SELECT PL.LevelID , PL.LevelName ,  PL.Sequence FROM ProductLevel  PL "
                        + " INNER JOIN ConfigActivityFilter CA  ON "
                        + " PL.LevelID =CA.ProductFilter1 OR  "
                        + " PL.LevelID =CA.ProductFilter2 OR  "
                        + " PL.LevelID =CA.ProductFilter3 OR  "
                        + " PL.LevelID =CA.ProductFilter4 OR  "
                        + " PL.LevelID =CA.ProductFilter5  "
                        + " WHERE  CA.ActivityCode='" + moduleName + "'");

        LevelBO mLevelBO;
        sequencevalues = new Vector<>();
        while (listCursor.moveToNext()) {

            mLevelBO = new LevelBO();
            mLevelBO.setProductID(listCursor.getInt(0));
            mLevelBO.setLevelName(listCursor.getString(1));
            mLevelBO.setSequence(listCursor.getInt(2));

            sequencevalues.add(mLevelBO);
        }

        listCursor.close();

        int contentlevel = 0;
        int loopEnd = 0;

        Cursor seqCur = db
                .selectSQL("SELECT IFNULL(PL.Sequence,0) "
                        + "FROM ConfigActivityFilter CF "
                        + "LEFT JOIN ProductLevel PL ON PL.LevelId = CF.ProductContent "
                        + "WHERE  CF.ActivityCode= '" + moduleName + "'");
        if (seqCur.moveToNext()) {
            contentlevel = seqCur.getInt(0);
        }

        seqCur.close();

        mfilterlevelBo = new HashMap<>();

        try {

            if (sequencevalues.size() > 0) {

                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                    loopEnd = contentlevel - bmodel.configurationMasterHelper.globalSeqId
                            + 1;
                else
                    loopEnd = contentlevel - sequencevalues.get(0).getSequence()
                            + 1;
                loadParentFilter(loopEnd, sequencevalues.get(0).getProductID());

                for (int i = 1; i < sequencevalues.size(); i++) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                        loopEnd = contentlevel - bmodel.configurationMasterHelper.globalSeqId
                                + 1;
                    else
                        loopEnd = contentlevel
                                - sequencevalues.get(i - 1).getSequence() + 1;

                    loadChildFilter(loopEnd,
                            sequencevalues.get(i).getSequence(),
                            sequencevalues.get(i - 1).getSequence(),
                            sequencevalues.get(i).getProductID(),
                            sequencevalues.get(i - 1).getProductID());
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (moduleName.equals("MENU_STK_ORD") && bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
            mLoadedGlobalProductId = mSelectedGlobalProductId;
    }

    private void loadParentFilter(int loopEnd, int mProductLevelId) {
        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {

            int filterGap = mProductLevelId - mSelectedGlobalLevelID + 1;

            query = "SELECT DISTINCT PM" + filterGap + ".PID, PM" + filterGap + ".PName FROM ProductMaster PM1";

            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query
                    + " WHERE PM1.PLid = " + mSelectedGlobalLevelID + " AND PM1.PID = " + mSelectedGlobalProductId + " Order By PM1.RowId";
        } else {
            query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1";

            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query
                    + " WHERE PM1.PLid = " + mProductLevelId + " Order By PM1.RowId";
        }


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        db.openDataBase();

        Cursor c = db.selectSQL(query);

        if (c != null) {

            pfilterlevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setProductID(c.getInt(0));
                mLevelBO.setLevelName(c.getString(1));
                pfilterlevel.add(mLevelBO);
            }
            mfilterlevelBo.put(mProductLevelId, pfilterlevel);
            c.close();
        }
    }

    private void loadChildFilter(int loopEnd, int mChildLevel,
                                 int mParentLevel, int mLevelId, int mParentLevelId) {

        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mChildLevel - bmodel.configurationMasterHelper.globalSeqId + 1;
            int PM1Level = mParentLevel - bmodel.configurationMasterHelper.globalSeqId + 1;

            query = "SELECT DISTINCT PM" + PM1Level + ".PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mSelectedGlobalLevelID
                    + " AND PM1.PID = " + mSelectedGlobalProductId + " Order By PM" + filterGap + ".RowId,PM1.RowId";
        } else {
            int filterGap = mChildLevel - mParentLevel + 1;

            query = "SELECT DISTINCT PM1.PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mParentLevelId
                    + " Order By PM" + filterGap + ".RowId,PM1.RowId";
        }


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        db.openDataBase();

        Cursor c = db.selectSQL(query);

        if (c != null) {
            pfilterlevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setParentID(c.getInt(0));
                mLevelBO.setProductID(c.getInt(1));
                mLevelBO.setLevelName(c.getString(2));
                pfilterlevel.add(mLevelBO);
            }
            mfilterlevelBo.put(mLevelId, pfilterlevel);
            c.close();
            db.close();
        }
    }

    public void downloadProductFilter(String moduleName) {
        if (!isRetailerModule(moduleName)) {
            mParentLevelBo = new Vector<ParentLevelBo>();
            mChildLevelBo = new Vector<ChildLevelBo>();
        }
        mRetailerModuleChildLevelBO = new Vector<ChildLevelBo>();
        mRetailerModuleparentLevelBO = new Vector<ParentLevelBo>();

        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mParentLevel = 0;
            int mChildLevel = 0;
            int mContentLevel = 0;

            String mParentLevelName = "";
            String mChildLevelName = "";

            ParentLevelBo mParentLevelBo;
            ChildLevelBo mChildLevelBo;

            ParentLevelBo stkandordModuleparentLevelBO;
            ChildLevelBo stkandordModuleChildlevelBO;

            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL2.Sequence,0),"
                            + " IFNULL(PL3.Sequence,0), PL1.LevelName, PL2.LevelName FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                            + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                            + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                            + " WHERE CF.ActivityCode = '" + moduleName + "'");

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mParentLevel = filterCur.getInt(0);
                    mChildLevel = filterCur.getInt(1);
                    mContentLevel = filterCur.getInt(2);
                    mParentLevelName = filterCur.getString(3);
                    mChildLevelName = filterCur.getString(4);
                }
                filterCur.close();
            }

            // Two Level Filter
            if (mParentLevel != 0 && mChildLevel != 0) {

                int loopEnd = mContentLevel - mParentLevel + 1;

                // Load Parent Level Filter

                String query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1";

                for (int i = 2; i <= loopEnd; i++)
                    query = query + " INNER JOIN ProductMaster PM" + i
                            + " ON PM" + i + ".ParentId = PM" + (i - 1)
                            + ".PID";

                query = query
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode = '" + moduleName + "')"
                        + " Order By PM1.RowId";

                Cursor c = db.selectSQL(query);

                if (c != null) {
//                    setParentLevelBo(new Vector<ParentLevelBo>());
                    while (c.moveToNext()) {
                        if (isRetailerModule(moduleName)) {

                            stkandordModuleparentLevelBO = new ParentLevelBo();
                            stkandordModuleparentLevelBO.setPl_productid(c
                                    .getInt(0));
                            stkandordModuleparentLevelBO.setPl_levelName(c
                                    .getString(1));
                            stkandordModuleparentLevelBO
                                    .setPl_productLevel(mParentLevelName);
                            mRetailerModuleparentLevelBO
                                    .add(stkandordModuleparentLevelBO);

                        } else {

                            mParentLevelBo = new ParentLevelBo();
                            mParentLevelBo.setPl_productid(c.getInt(0));
                            mParentLevelBo.setPl_levelName(c.getString(1));
                            mParentLevelBo.setPl_productLevel(mParentLevelName);
                            getParentLevelBo().add(mParentLevelBo);

                        }
                    }
                    c.close();
                }

                // Load Child Level Filter

                int filterGap = mChildLevel - mParentLevel + 1;

                query = "SELECT DISTINCT PM1.PID, PM" + filterGap + ".PID,  PM"
                        + filterGap + ".PName FROM ProductMaster PM1";

                for (int i = 2; i <= filterGap; i++)
                    query = query + " INNER JOIN ProductMaster PM" + i
                            + " ON PM" + i + ".ParentId = PM" + (i - 1)
                            + ".PID";

                query = query
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode = '" + moduleName + "')"
                        + " Order By PM" + filterGap + ".RowId,PM1.RowId";

                c = db.selectSQL(query);

                if (c != null) {
//                    setChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        if (isRetailerModule(moduleName)) {

                            stkandordModuleChildlevelBO = new ChildLevelBo();
                            stkandordModuleChildlevelBO
                                    .setParentid(c.getInt(0));
                            stkandordModuleChildlevelBO.setProductid(c
                                    .getInt(1));
                            stkandordModuleChildlevelBO.setPlevelName(c
                                    .getString(2));
                            stkandordModuleChildlevelBO
                                    .setProductLevel(mChildLevelName);
                            mRetailerModuleChildLevelBO
                                    .add(stkandordModuleChildlevelBO);

                        } else {

                            mChildLevelBo = new ChildLevelBo();
                            mChildLevelBo.setParentid(c.getInt(0));
                            mChildLevelBo.setProductid(c.getInt(1));
                            mChildLevelBo.setPlevelName(c.getString(2));
                            mChildLevelBo.setProductLevel(mChildLevelName);
                            getChildLevelBo().add(mChildLevelBo);
                        }
                    }
                    c.close();
                }
            } else if (mParentLevel != 0) {// One Level Filter
                int loopEnd = mContentLevel - mParentLevel + 1;

                String query = "SELECT DISTINCT PM1.PID, PM1.PName,PM1.ParentId FROM ProductMaster PM1";

                for (int i = 2; i <= loopEnd; i++)
                    query = query + " INNER JOIN ProductMaster PM" + i
                            + " ON PM" + i + ".ParentId = PM" + (i - 1)
                            + ".PID";

                query = query
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode = '" + moduleName + "') "
                        + " Order By PM1.RowId";

                Cursor c = db.selectSQL(query);

                if (c != null) {
                    setChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        if (isRetailerModule(moduleName)) {
                            stkandordModuleChildlevelBO = new ChildLevelBo();
                            stkandordModuleChildlevelBO.setProductid(c
                                    .getInt(0));
                            stkandordModuleChildlevelBO.setPlevelName(c
                                    .getString(1));
                            stkandordModuleChildlevelBO
                                    .setProductLevel(mParentLevelName);
                            stkandordModuleChildlevelBO
                                    .setParentid(c.getInt(2));
                            mRetailerModuleChildLevelBO
                                    .add(stkandordModuleChildlevelBO);

                        } else {
                            mChildLevelBo = new ChildLevelBo();
                            mChildLevelBo.setProductid(c.getInt(0));
                            mChildLevelBo.setPlevelName(c.getString(1));
                            mChildLevelBo.setProductLevel(mParentLevelName);
                            mChildLevelBo.setParentid(c.getInt(2));
                            getChildLevelBo().add(mChildLevelBo);
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

    private boolean isRetailerModule(String moduleName) {
        if (moduleName.equals("MENU_SURVEY")
                || moduleName.equals("MENU_NEAREXPIRY")
                || moduleName.equals("MENU_PRICE")
                || moduleName.equals("MENU_AVAILABILITY")
                || moduleName.equals("MENU_SOS")
                || moduleName.equals("MENU_SOD")
                || moduleName.equals("MENU_SOSKU")
                || moduleName.equals("MENU_PROMO")
                || moduleName.equals("MENU_ASSET")
                || moduleName.equals("MENU_POSM")
                || moduleName.equals("MENU_PLANOGRAM")
                || moduleName.equals("MENU_DGT")
                || moduleName.equals("MENU_DGT_CS")) {
            return true;
        }

        return false;
    }

    public void downloadProductsWithFiveLevelFilter(String moduleCode) {

        productMasterById = new HashMap<String, ProductMasterBO>();

        try {

            // load location and date
            getLocations();
            generateDate();

            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mChildLevel = 0;
            int mFiltrtLevel = 0;
            int mContentLevel = 0;

            String str = "F.srp1,G.srp1";
            String csrp = "F.csrp1,G.csrp1";
            String osrp = "F.osrp1,G.osrp1";
            try {
                if (bmodel.getRetailerMasterBO().getRpTypeCode()
                        .equals(CASH_TYPE)) {
                    str = "F.srp1,G.srp1";
                    csrp = "F.csrp1,G.csrp1";
                    osrp = "F.osrp1,G.osrp1";
                } else {
                    str = "F.srp2,G.srp2";
                    csrp = "F.csrp2,G.csrp2";
                    osrp = "F.osrp2,G.osrp2";
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            Vector<ConfigureBO> filterBO = bmodel.configurationMasterHelper.downloadFilterList();
            boolean filter10 = false; //Must Sell
            boolean filter11 = false; // Focus Brand
            boolean filter12 = false; // Focus Brand2
            boolean filter16 = false; // NMust Sell
            boolean filter20 = false; // Focus Brand 3
            boolean filter21 = false; // Focus Brand 4
            boolean filter22 = false; // Small Pack
            boolean filter19 = false; // nearExpiry tagged
            if (filterBO != null && filterBO.size() > 0) {
                for (ConfigureBO bo : filterBO) {
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt10"))
                        filter10 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt11"))
                        filter11 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt12"))
                        filter12 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt16"))
                        filter16 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt20"))
                        filter20 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt21"))
                        filter21 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt22"))
                        filter22 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt19"))
                        filter19 = true;
                }
            }
            // get tagging details
            String MSLproductIds = "";
            String NMSLproductIds = "";
            String FCBNDproductIds = "";
            String FCBND2productIds = "";
            String FCBND3productIds = "";
            String FCBND4productIds = "";
            String SMPproductIds = "";
            String nearExpiryTaggedProductIds = "";
            String[] detail;

            if (filter10) {
                MSLproductIds = getTaggingDetails("MSL");

            }
            if (filter16) {
                NMSLproductIds = getTaggingDetails("NMSL");

            }
            if (filter11) {
                FCBNDproductIds = getTaggingDetails("FCBND");

            }
            if (filter12) {
                FCBND2productIds = getTaggingDetails("FCBND2");

            }
            if (filter20) {
                FCBND3productIds = getTaggingDetails("FCBND3");

            }
            if (filter21) {
                FCBND4productIds = getTaggingDetails("FCBND4");

            }
            if (filter22) {
                SMPproductIds = getTaggingDetails("SMP");

            }
            if (filter19) {
                nearExpiryTaggedProductIds = getTaggingDetails("MENU_NEAREXPIRY");

            }

            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);
            String sql = "";

            if (sequencevalues != null) {
                if (sequencevalues.size() > 0) {
                    mChildLevel = sequencevalues.size();
                }
            }

            if (mChildLevel == 0 && !bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                sql = "select A.pid, A.pcode,A.pname,A.parentid,A.sih, "
                        + "A.psname,A.barcode,A.vat,A.isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A.msqqty,"
                        + "A.dUomQty,A.duomid, u.ListCode,A.MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A.RField1,PWHS.qty,A.IsAlloc, "
                        + ((filter10) ? "A.pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A.pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A.pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "dOuomQty,dOuomid,caseBarcode,outerBarcode,count(A.pid),piece_uomid,A.mrp, A.mrp,"
                        + " A.isSalable,A.isReturnable,A.isBom,A.TypeID,A.baseprice, '' as brandname,0"
                        + ((filter16) ? ",A.pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ") + ",A.weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount"
                        + ",A.Hasserial,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                        + ((filter20) ? "A.pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3,")
                        + ((filter21) ? "A.pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4,")
                        + ((filter22) ? "A.pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP,")
                        + "A.tagDescription,"
                        + ((filter19) ? "A.pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid ")
                        + ",(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid"
                        + ",(CASE WHEN PWHS.PID=A.PID then 'true' else 'false' end) as IsAvailWareHouse"
                        + " from ProductMaster A left join "
                        + "PriceMaster F on A.Pid = F.pid and F.scid = "
                        + bmodel.getRetailerMasterBO().getGroupId()
                        + " left join PriceMaster G on A.Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A.pid=sbd.productid and sbd.channelid="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A.pid and PWHS.UomID=A.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A.pid"
                        + " WHERE A.isSalable = 1 AND A.PLid IN"
                        + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        + " group by A.pid ORDER BY " + filter + " A.rowid";

            } else {

                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && mChildLevel == 0)
                    mChildLevel = 1;

                Cursor filterCur = db
                        .selectSQL("SELECT IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter"
                                + mChildLevel
                                + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT(moduleCode));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mFiltrtLevel = filterCur.getInt(0);
                        mContentLevel = filterCur.getInt(1);
                    }
                    filterCur.close();
                }

                int loopEnd;
                int parentLevelID = 1;
                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                    loopEnd = mContentLevel - bmodel.configurationMasterHelper.globalSeqId + 1;
                } else
                    loopEnd = mContentLevel - mFiltrtLevel + 1;
                String batchWiseHighestPrice = " ";
                if (!bmodel.configurationMasterHelper.IS_APPLY_BATCH_PRICE_FROM_PRODUCT) {
                    batchWiseHighestPrice = " and F.batchid="
                            + "ifnull((select batchid from batchmaster where mfgdate=(select max(mfgdate) from batchmaster) and  "
                            + "batchmaster .pid=A" + loopEnd + ".pid),0)";
                }


                sql = "select A"
                        + loopEnd
                        + ".pid, A"
                        + loopEnd
                        + ".pcode,A"
                        + loopEnd
                        + ".pname,A" + parentLevelID + ".pid,A"
                        + loopEnd
                        + ".sih,A"
                        + loopEnd
                        + ".psname,A"
                        + loopEnd
                        + ".barcode,A"
                        + loopEnd
                        + ".vat,A"
                        + loopEnd
                        + ".isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A"
                        + loopEnd
                        + ".msqqty,A"
                        + loopEnd
                        + ".dUomQty,A"
                        + loopEnd
                        + ".duomid, u.ListCode,A"
                        + loopEnd
                        + ".MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A"
                        + loopEnd
                        + ".RField1,PWHS.qty,A"
                        + loopEnd
                        + ".IsAlloc, "
                        + ((filter10) ? "A" + loopEnd + ".pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A" + loopEnd + ".pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A" + loopEnd + ".pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "A1.dOuomQty,A" + loopEnd + ".dOuomid,A" + loopEnd
                        + ".caseBarcode,A" + loopEnd
                        + ".outerBarcode,count(A1.pid),A" + loopEnd
                        + ".piece_uomid,A" + loopEnd + ".mrp, A" + loopEnd
                        + ".mrp,A" + loopEnd + ".isSalable,A" + loopEnd
                        + ".isReturnable,A" + loopEnd + ".isBom,A" + loopEnd
                        + ".TypeID,A" + loopEnd
                        + ".baseprice,A1.pname as brandname,A1.parentid"
                        + ((filter16) ? ",A" + loopEnd + ".pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ")
                        + ",A" + loopEnd + ".weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount,A" + loopEnd + ".Hasserial ,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                        + ((filter20) ? "A" + loopEnd + ".pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3,")
                        + ((filter21) ? "A" + loopEnd + ".pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4,")
                        + ((filter22) ? "A" + loopEnd + ".pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP,")
                        + "A" + loopEnd + ".tagDescription,"
                        + ((filter19) ? "A" + loopEnd + ".pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry")
                        //+ ",(Select imagename from DigitalContentMaster where imageid=(Select imgid from DigitalContentProductMapping where pid=A" + loopEnd + ".pid)) as imagename "
                        + ",(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid"
                        + ",(CASE WHEN PWHS.PID=A" + loopEnd + ".PID then 'true' else 'false' end) as IsAvailWareHouse"
                        + " from ProductMaster A1 ";

                for (int i = 2; i <= loopEnd; i++)
                    sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                            + ".ParentId = A" + (i - 1) + ".PID";

                sql = sql + " left join " + "PriceMaster F on A" + loopEnd
                        + ".Pid = F.pid and F.scid = "
                        + bmodel.getRetailerMasterBO().getGroupId()
                        + batchWiseHighestPrice

                        + " left join PriceMaster G on A" + loopEnd
                        + ".Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A" + loopEnd + ".dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A" + loopEnd
                        + ".pid=sbd.productid and sbd.channelid="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A" + loopEnd + ".pid and PWHS.UomID=A" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A" + loopEnd + ".pid";
                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                    sql = sql + " WHERE A1.PLid = " + bmodel.productHelper.getmSelectedGLobalLevelID()
                            + " AND A1.PID = " + bmodel.productHelper.getmSelectedGlobalProductId()
                            // + " AND A" + loopEnd
                            // + ".isSalable = 1 "
                            + " group by A" + loopEnd + ".pid ORDER BY " + filter
                            + " A" + loopEnd + ".rowid";
                } else {
                    sql = sql + " WHERE A1.PLid IN (SELECT ProductFilter"
                            + mChildLevel + " FROM ConfigActivityFilter"
                            + " WHERE ActivityCode = "
                            + bmodel.QT(moduleCode)
                            + ")"
                            // + " AND A" + loopEnd
                            // + ".isSalable = 1 "
                            + " group by A" + loopEnd + ".pid ORDER BY " + filter
                            + " A" + loopEnd + ".rowid";
                }

            }

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<ProductMasterBO>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductCode(c.getString(1));
                    product.setProductName(c.getString(2));
                    product.setParentid(c.getInt(3));
                    product.setSIH(c.getInt(4));
                    product.setProductShortName(c.getString(5));
                    product.setBarCode(c.getString(6));
                    product.setVat(c.getFloat(7));
                    product.setSrp(c.getFloat(9));
                    product.setPrevPrice_pc(c.getFloat(9) + "");
                    product.setCsrp(c.getFloat(10));
                    product.setPrevPrice_ca(c.getFloat(10) + "");
                    product.setOsrp(c.getFloat(11));
                    product.setPrevPrice_oo(c.getFloat(11) + "");
                    product.setMSQty(c.getInt(12));
                    product.setCaseSize(c.getInt(13));
                    product.setCaseUomId(c.getInt(14)); // caseuomid
                    product.setOU(c.getString(15));
                    product.setMRP(c.getDouble(16));
                    product.setDropQty(c.getInt(17));
                    product.setSbdGroupName(c.getString(18));
                    product.setRField1(c.getString(19));
                    product.setWSIH(c.getInt(20));
                    product.setAllocation(c.getInt(21));
                    product.setIsMustSell(c.getInt(22));
                    product.setIsFocusBrand(c.getInt(23));
                    product.setIsFocusBrand2(c.getInt(24));
                    product.setOutersize(c.getInt(25));
                    product.setOuUomid(c.getInt(26)); // outerid
                    product.setCasebarcode(c.getString(27));
                    product.setOuterbarcode(c.getString(28));
                    product.setPcUomid(c.getInt(30));// Pc Uomid
                    product.setMinprice(c.getInt(31));
                    product.setMaxPrice(c.getInt(32));
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                        product.setBatchwiseProductCount(1);
                    product.setBatchwiseProductCount(0);
                    product.setIsSaleable(c.getInt(33));
                    product.setIsReturnable(c.getInt(34));
                    product.setTypeID(c.getInt(36));
                    product.setBaseprice(c.getFloat(37));
                    product.setBrandname(c.getString(38));
                    product.setcParentid(c.getInt(39));

                    product.setGroupid(c.getInt(c.getColumnIndex("groupid")));
                    product.setLocations(cloneLocationList(locations));

                    for (int i = 0; i < locations.size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(cloneDateList(dateList));
                    }
                    /*
                     * product.setSalesReturnReasonList(cloneIsolateList(
					 * bmodel.reasonHelper.getReasonSalesReturnMaster(),
					 * product.getCaseSize(), product.getOutersize()));
					 */
                    product.setIsNMustSell(c.getInt(c.getColumnIndex("IsNMustSell")));
                    product.setWeight(c.getFloat(c.getColumnIndex("weight")));
                    product.setIsDiscountable(c.getInt(c.getColumnIndex("IsDiscount")));
                    product.setScannedProduct(c.getInt(c.getColumnIndex("Hasserial")));

                    product.setIsFocusBrand3(c.getInt(c.getColumnIndex("IsFocusBrand3")));
                    product.setIsFocusBrand4(c.getInt(c.getColumnIndex("IsFocusBrand4")));
                    product.setIsSMP(c.getInt(c.getColumnIndex("IsSMP")));
                    product.setDescription(c.getString(c.getColumnIndex("tagDescription")));

                    product.setIsNearExpiryTaggedProduct(c.getInt(c.getColumnIndex("isNearExpiry")));

                    product.setPriceoffvalue(c.getDouble(c.getColumnIndex("priceoffvalue")));
                    product.setPriceOffId(c.getInt(c.getColumnIndex("priceoffid")));

                    product.setAvailableinWareHouse(c.getString(c.getColumnIndex("IsAvailWareHouse")).equals("true"));

                    productMaster.add(product);
                    productMasterById.put(product.getProductID(), product);


                }
                c.close();
            }

            db.closeDB();

            if (bmodel.configurationMasterHelper.SHOW_TAX_MASTER) {
                downloadExcludeProductTaxDetails();
            }

            if (mChildLevel > 0)
                downloadLeastBrandProductMapping((mContentLevel - mFiltrtLevel + 1), mChildLevel, filter + "", moduleCode);

            downloadAttributeProductMapping();
            downloadAttributes();


        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    public HashMap<Integer, Vector<Integer>> getmAttributeByProductId() {
        return mAttributeByProductId;
    }

    public HashMap<Integer, Vector<Integer>> getmProductIdByBrandId() {
        return mProductIdByBrandId;
    }

    private HashMap<Integer, Vector<Integer>> mAttributeByProductId;
    private HashMap<Integer, Vector<Integer>> mProductIdByBrandId;

    public ArrayList<AttributeBO> getLstProductAttributeMapping() {
        return lstProductAttributeMapping;
    }

    private ArrayList<AttributeBO> lstProductAttributeMapping;

    private void downloadAttributeProductMapping() {
        DBUtil db = null;
        mAttributeByProductId = new HashMap<>();
        lstProductAttributeMapping = new ArrayList<>();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            // plid is not used now.. considering given products is sku level
            Cursor c = db.selectSQL("select A.pid,pam.att_id,am.att_name from ProductMaster A inner join ProductAttributeMapping pam on pam.pid=A.pid left join attributeMaster am on am.att_id=pam.att_id order by A.pid");
            if (c.getCount() > 0) {
                AttributeBO attBO;
                while (c.moveToNext()) {
                    attBO = new AttributeBO();
                    attBO.setProductId(c.getInt(0));
                    attBO.setAttributeId(c.getInt(1));
                    // attBO.setAttributeName(c.getString(2));
                    lstProductAttributeMapping.add(attBO);
                    if (mAttributeByProductId.get(c.getInt(0)) == null) {
                        Vector<Integer> temp = new Vector<>();
                        temp.add(c.getInt(1));
                        mAttributeByProductId.put(c.getInt(0), temp);
                    } else {
                        mAttributeByProductId.get(c.getInt(0)).add(c.getInt(1));
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }

    private void downloadLeastBrandProductMapping(int loopend, int childLevel, String filter, String moduleCode) {
        DBUtil db = null;
        try {
            String sql = "select A"
                    + loopend
                    + ".pid,A1.pid as parentid"
                    + " from ProductMaster A1 ";

            for (int i = 2; i <= loopend; i++)
                sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                        + ".ParentId = A" + (i - 1) + ".PID";

            sql = sql + " WHERE A1.PLid IN (SELECT ProductFilter"
                    + childLevel + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = "
                    + bmodel.QT(moduleCode)
                    + ")"

                    + " ORDER BY " + filter
                    + " A1.pid";

            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                mProductIdByBrandId = new HashMap<>();
                AttributeBO attBO;
                while (c.moveToNext()) {
                    attBO = new AttributeBO();
                    attBO.setProductId(c.getInt(0));
                    attBO.setLeastParentId(c.getInt(1));

                    if (mProductIdByBrandId.get(c.getInt(1)) == null) {
                        Vector<Integer> temp = new Vector<>();
                        temp.add(c.getInt(0));
                        mProductIdByBrandId.put(c.getInt(1), temp);
                    } else {
                        mProductIdByBrandId.get(c.getInt(1)).add(c.getInt(0));
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
    }

    /**
     * This method will download the product information form ProductMaster
     * table. Since the price channel varies from retailer to retailer, this
     * method should call only after selecting retailer.
     */
    public void downloadProducts(String moduleCode) {
        productMasterById = new HashMap<String, ProductMasterBO>();

        try {

            // load location and date
            getLocations();
            generateDate();

            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mParentLevel = 0;
            int mChildLevel = 0;
            int mContentLevel = 0;

            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL1.Sequence,0),IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                            + " FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                            + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                            + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                            + " WHERE CF.ActivityCode = "
                            + bmodel.QT(moduleCode));

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mParentLevel = filterCur.getInt(0);
                    mChildLevel = filterCur.getInt(1);
                    mContentLevel = filterCur.getInt(2);
                }
                filterCur.close();
            }

            String str = "F.srp1,G.srp1";
            String csrp = "F.csrp1,G.csrp1";
            String osrp = "F.osrp1,G.osrp1";
            try {
                if (bmodel.getRetailerMasterBO().getRpTypeCode()
                        .equals(CASH_TYPE)) {
                    str = "F.srp1,G.srp1";
                    csrp = "F.csrp1,G.csrp1";
                    osrp = "F.osrp1,G.osrp1";
                } else {
                    str = "F.srp2,G.srp2";
                    csrp = "F.csrp2,G.csrp2";
                    osrp = "F.osrp2,G.osrp2";
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            Vector<ConfigureBO> filterBO = bmodel.configurationMasterHelper.downloadFilterList();
            boolean filter10 = false; //Must Sell
            boolean filter11 = false; // Focus Brand
            boolean filter12 = false; // Focus Brand2
            boolean filter16 = false; // NMust Sell
            boolean filter20 = false; // Focus Brand 3
            boolean filter21 = false; // Focus Brand 4
            boolean filter22 = false; // Small pack
            boolean filter19 = false; // nearExpiry tagged
            if (filterBO != null && filterBO.size() > 0) {
                for (ConfigureBO bo : filterBO) {
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt10"))
                        filter10 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt11"))
                        filter11 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt12"))
                        filter12 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt16"))
                        filter16 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt20"))
                        filter20 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt21"))
                        filter21 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt22"))
                        filter22 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt19"))
                        filter19 = true;
                }
            }

            // get tagging details
            String MSLproductIds = "";
            String NMSLproductIds = "";
            String FCBNDproductIds = "";
            String FCBND2productIds = "";
            String FCBND3productIds = "";
            String FCBND4productIds = "";
            String SMPproductIds = "";
            String nearExpiryTaggedProductIds = "";


            if (filter10) {
                MSLproductIds = getTaggingDetails("MSL");

            }
            if (filter16) {
                NMSLproductIds = getTaggingDetails("NMSL");

            }
            if (filter11) {
                FCBNDproductIds = getTaggingDetails("FCBND");

            }
            if (filter12) {
                FCBND2productIds = getTaggingDetails("FCBND2");

            }
            if (filter20) {
                FCBND3productIds = getTaggingDetails("FCBND3");

            }
            if (filter21) {
                FCBND4productIds = getTaggingDetails("FCBND4");

            }
            if (filter22) {
                SMPproductIds = getTaggingDetails("SMP");

            }
            if (filter19) {
                nearExpiryTaggedProductIds = getTaggingDetails("MENU_NEAREXPIRY");

            }


            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);
            String sql = "";

            if (mParentLevel == 0 && mChildLevel == 0) {
                sql = "select A.pid, A.pcode,A.pname,A.parentid,A.sih, "
                        + "A.psname,A.barcode,A.vat,A.isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A.msqqty,"
                        + "A.dUomQty,A.duomid, u.ListCode,A.MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A.RField1,PWHS.Qty,A.IsAlloc, "
                        + ((filter10) ? "A.pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A.pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A.pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "dOuomQty,dOuomid,caseBarcode,outerBarcode,count(A.pid),piece_uomid,A.mrp, A.mrp,"
                        + " A.isSalable,A.isReturnable,A.isBom,A.TypeID,A.baseprice, '' as brandname,0"
                        + ((filter16) ? ",A.pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ") + ",A.weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount,A.Hasserial,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                        + ((filter20) ? "A.pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3,")
                        + ((filter21) ? "A.pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4,")
                        + ((filter22) ? "A.pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP,")
                        + "A.tagDescription,"
                        + ((filter19) ? "A.pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry")
                        + ",(CASE WHEN PWHS.PID=A.PID then 'true' else 'false' end) as IsAvailWareHouse"
                        + " from ProductMaster A left join "
                        + "PriceMaster F on A.Pid = F.pid and F.scid = "
                        + bmodel.getRetailerMasterBO().getGroupId()
                        + " left join PriceMaster G on A.Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A.pid=sbd.productid and sbd.channelid="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A.pid and PWHS.UomID=A.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A.pid"
                        + " WHERE "
                        //  A.isSalable = 1 AND
                        + "A.PLid IN"
                        + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        + " group by A.pid ORDER BY " + filter + " A.rowid";

            } else {

                int loopEnd = 0;
                String parentFilter = "";

                if (mChildLevel != 0) {
                    loopEnd = mContentLevel - mChildLevel + 1;
                    parentFilter = "ProductFilter2";
                } else {
                    loopEnd = mContentLevel - mParentLevel + 1;
                    parentFilter = "ProductFilter1";
                }

                String batchWiseHighestPrice = " ";
                if (!bmodel.configurationMasterHelper.IS_APPLY_BATCH_PRICE_FROM_PRODUCT) {
                    batchWiseHighestPrice = " and F.batchid="
                            + "ifnull((select batchid from batchmaster where mfgdate=(select max(mfgdate) from batchmaster) and  "
                            + "batchmaster .pid=A" + loopEnd + ".pid),0)";
                }

                sql = "select A"
                        + loopEnd
                        + ".pid, A"
                        + loopEnd
                        + ".pcode,A"
                        + loopEnd
                        + ".pname,A1.pid,A"
                        + loopEnd
                        + ".sih,A"
                        + loopEnd
                        + ".psname,A"
                        + loopEnd
                        + ".barcode,A"
                        + loopEnd
                        + ".vat,A"
                        + loopEnd
                        + ".isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A"
                        + loopEnd
                        + ".msqqty,A"
                        + loopEnd
                        + ".dUomQty,A"
                        + loopEnd
                        + ".duomid, u.ListCode,A"
                        + loopEnd
                        + ".MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A"
                        + loopEnd
                        + ".RField1,PWHS.Qty,A"
                        + loopEnd
                        + ".IsAlloc, "
                        + ((filter10) ? "A" + loopEnd + ".pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A" + loopEnd + ".pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A" + loopEnd + ".pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "A1.dOuomQty,A"
                        + loopEnd
                        + ".dOuomid,A"
                        + loopEnd
                        + ".caseBarcode,A"
                        + loopEnd
                        + ".outerBarcode,count(A1.pid),A"
                        + loopEnd
                        + ".piece_uomid,A"
                        + loopEnd
                        + ".mrp, A"
                        + loopEnd
                        + ".mrp,A"
                        + loopEnd
                        + ".isSalable,A"
                        + loopEnd
                        + ".isReturnable,A"
                        + loopEnd
                        + ".isBom,A"
                        + loopEnd
                        + ".TypeID,A"
                        + loopEnd
                        + ".baseprice,A1.pname as brandname,A1.parentid,F.priceoffvalue,F.PriceOffId"
                        + ((filter16) ? ",A" + loopEnd + ".pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ") + ",A" + loopEnd + ".weight as weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount "
                        + ",A" + loopEnd + ".Hasserial, (CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid, "
                        + ((filter20) ? "A" + loopEnd + ".pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3, ")
                        + ((filter21) ? "A" + loopEnd + ".pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4, ")
                        + ((filter22) ? "A" + loopEnd + ".pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP, ")
                        + "A" + loopEnd + ".tagDescription,"
                        + ((filter19) ? "A" + loopEnd + ".pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry")
                        + ",(CASE WHEN PWHS.PID=A" + loopEnd + ".PID then 'true' else 'false' end) as IsAvailWareHouse"
                        //+ ",(Select imagename from DigitalContentMaster where imageid=(Select imgid from DigitalContentProductMapping where pid=A" + loopEnd + ".pid)) as imagename "
                        + " from ProductMaster A1 ";

                for (int i = 2; i <= loopEnd; i++)
                    sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                            + ".ParentId = A" + (i - 1) + ".PID";

                sql = sql
                        + " left join "
                        + "PriceMaster F on A"
                        + loopEnd
                        + ".Pid = F.pid and F.scid = "
                        + bmodel.getRetailerMasterBO().getGroupId()
                        + batchWiseHighestPrice
                        + " left join PriceMaster G on A"
                        + loopEnd
                        + ".Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A" + loopEnd + ".dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A" + loopEnd
                        + ".pid=sbd.productid and sbd.channelid="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A" + loopEnd + ".pid and PWHS.UomID=A" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A" + loopEnd + ".pid"

                        + " WHERE A1.PLid IN (SELECT " + parentFilter
                        + " FROM ConfigActivityFilter"

                        + " WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        // + " AND A" + loopEnd
                        // + ".isSalable = 1 "
                        + " group by A" + loopEnd + ".pid ORDER BY " + filter
                        + " A" + loopEnd + ".rowid";

            }

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<ProductMasterBO>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductCode(c.getString(1));
                    product.setProductName(c.getString(2));
                    product.setParentid(c.getInt(3));
                    product.setSIH(c.getInt(4));
                    product.setProductShortName(c.getString(5));
                    product.setBarCode(c.getString(6));
                    product.setVat(c.getFloat(7));
                    product.setSrp(c.getFloat(9));
                    product.setPrevPrice_pc(c.getFloat(9) + "");
                    product.setCsrp(c.getFloat(10));
                    product.setPrevPrice_ca(c.getFloat(10) + "");
                    product.setOsrp(c.getFloat(11));
                    product.setPrevPrice_oo(c.getFloat(11) + "");
                    product.setMSQty(c.getInt(12));
                    product.setCaseSize(c.getInt(13));
                    product.setCaseUomId(c.getInt(14)); // caseuomid
                    product.setOU(c.getString(15));
                    product.setMRP(c.getDouble(16));
                    product.setDropQty(c.getInt(17));
                    product.setSbdGroupName(c.getString(18));
                    product.setRField1(c.getString(19));
                    product.setWSIH(c.getInt(20));
                    product.setAllocation(c.getInt(21));
                    product.setIsMustSell(c.getInt(22));
                    product.setIsFocusBrand(c.getInt(23));
                    product.setIsFocusBrand2(c.getInt(24));
                    product.setOutersize(c.getInt(25));
                    product.setOuUomid(c.getInt(26)); // outerid
                    product.setCasebarcode(c.getString(27));
                    product.setOuterbarcode(c.getString(28));
                    product.setPcUomid(c.getInt(30));// Pc Uomid
                    product.setMinprice(c.getInt(31));
                    product.setMaxPrice(c.getInt(32));
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                        product.setBatchwiseProductCount(1);
                    product.setIsSaleable(c.getInt(33));
                    product.setIsReturnable(c.getInt(34));
                    product.setTypeID(c.getInt(36));
                    product.setBaseprice(c.getFloat(37));
                    product.setBrandname(c.getString(38));
                    product.setcParentid(c.getInt(39));
                    product.setPriceoffvalue(c.getDouble(40));
                    product.setPriceOffId(c.getInt(41));
                    product.setLocations(cloneLocationList(locations));
                    product.setGroupid(c.getInt(c.getColumnIndex("groupid")));
                    for (int i = 0; i < locations.size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(cloneDateList(dateList));
                    }
                    /*
                     * product.setSalesReturnReasonList(cloneIsolateList(
					 * bmodel.reasonHelper.getReasonSalesReturnMaster(),
					 * product.getCaseSize(), product.getOutersize()));
					 */
                    product.setIsNMustSell(c.getInt(c.getColumnIndex("IsNMustSell")));
                    product.setWeight(c.getFloat(c.getColumnIndex("weight")));

                    product.setIsDiscountable(c.getInt(c.getColumnIndex("IsDiscount")));
                    product.setScannedProduct(c.getInt(c.getColumnIndex("Hasserial")));
                    product.setIsFocusBrand3(c.getInt(c.getColumnIndex("IsFocusBrand3")));
                    product.setIsFocusBrand4(c.getInt(c.getColumnIndex("IsFocusBrand4")));
                    product.setIsSMP(c.getInt(c.getColumnIndex("IsSMP")));

                    product.setDescription(c.getString(c.getColumnIndex("tagDescription")));

                    product.setIsNearExpiryTaggedProduct(c.getInt(c.getColumnIndex("isNearExpiry")));
                    product.setAvailableinWareHouse(c.getString(c.getColumnIndex("IsAvailWareHouse")).equals("true"));
                    productMaster.add(product);
                    productMasterById.put(product.getProductID(), product);
                }
                c.close();
            }

            db.closeDB();

            if (bmodel.configurationMasterHelper.SHOW_TAX_MASTER)
                downloadExcludeProductTaxDetails();



        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateProductColorAndSequance() {// for piramal

        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select pid,SM.listName,SM.flex1 from RetailerProductDisplay RP " +
                    "left join standardListMaster SM ON RP.colorId=SM.listId");
            if (c != null) {
                while (c.moveToNext()) {

                    for (ProductMasterBO productMasterBO : getProductMaster()) {
                        if (productMasterBO.getParentid() == c.getInt(0)) {
                            productMasterBO.setColorCode(c.getString(1));
                            productMasterBO.setProductSequence(c.getInt(2));
                            productMasterBO.setSoInventory(c.getInt(3));
                        }
                    }


                }
                c.close();
            }
            db.closeDB();
        } catch (Exception ex) {

            Commons.printException(ex);
        }

        Vector<ProductMasterBO> productListWithSequence = new Vector<>();
        Vector<ProductMasterBO> productListWithoutSequence = new Vector<>();
        for (ProductMasterBO productMasterBO : getProductMaster()) {
            if (productMasterBO.getProductSequence() != 0) {
                productListWithSequence.add(productMasterBO);
            } else {
                productListWithoutSequence.add(productMasterBO);
            }
        }

        Collections.sort(productListWithSequence, ProductMasterBO.SequenceComparator);
        Collections.sort(productListWithoutSequence, ProductMasterBO.ProductNameComparator);

        getProductMaster().clear();
        getProductMaster().addAll(productListWithSequence);
        getProductMaster().addAll(productListWithoutSequence);

    }

    ArrayList<ProductMasterBO> lstChildProducts;

    public void downloadChildSKUs() {
        //For counter sales

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        int contentLevelId = 0, contentLevel;
        Cursor c = db.selectSQL("SELECT MAX(Sequence),levelId FROM ProductLevel");
        if (c != null) {
            if (c.moveToNext()) {
                contentLevel = c.getInt(0);
                contentLevelId = c.getInt(1);
            }
        }

        String str = "F.srp1,G.srp1";
        String csrp = "F.csrp1,G.csrp1";
        String osrp = "F.osrp1,G.osrp1";
        try {
            if (bmodel.getRetailerMasterBO().getRpTypeCode()
                    .equals(CASH_TYPE)) {
                str = "F.srp1,G.srp1";
                csrp = "F.csrp1,G.csrp1";
                osrp = "F.osrp1,G.osrp1";
            } else {
                str = "F.srp2,G.srp2";
                csrp = "F.csrp2,G.csrp2";
                osrp = "F.osrp2,G.osrp2";
            }
        } catch (Exception e) {
            Commons.printException(e);
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select A.pid, A.pcode,A.pname,A.parentid,A.psname,A.barcode,A.dUomQty,A.dOUomQty");
        stringBuilder.append(",ifnull(" + str + ") as srp , ifnull(" + csrp + ") as csrp ,ifnull(" + osrp + ") as osrp,A.isSalable,A.isReturnable,A.MRP,A.piece_uomid,A.dUomId,A.dOuomid");
        stringBuilder.append(" from productMaster A left join PriceMaster F on A.Pid = F.pid and F.scid = " + bmodel.getRetailerMasterBO().getGroupId() +
                " left join PriceMaster G on A.Pid = G.pid  and G.scid = 0 ");
        stringBuilder.append(" where plid=" + contentLevelId);
        c = db.selectSQL(stringBuilder.toString());
        if (c != null) {
            if (c.getCount() > 0) {
                ProductMasterBO productMasterBO;
                while (c.moveToNext()) {
                    productMasterBO = new ProductMasterBO();
                    productMasterBO.setProductID(c.getString(0));
                    productMasterBO.setProductCode(c.getString(1));
                    productMasterBO.setProductName(c.getString(2));
                    productMasterBO.setParentid(c.getInt(3));
                    productMasterBO.setProductShortName(c.getString(4));
                    productMasterBO.setBarCode(c.getString(5));
                    productMasterBO.setCaseSize(c.getInt(6));
                    productMasterBO.setOutersize(c.getInt(7));
                    productMasterBO.setSrp(c.getFloat(8));
                    productMasterBO.setCsrp(c.getFloat(9));
                    productMasterBO.setOsrp(c.getFloat(10));
                    productMasterBO.setIsSaleable(c.getInt(11));
                    productMasterBO.setIsReturnable(c.getInt(12));
                    productMasterBO.setMRP(c.getFloat(13));
                    productMasterBO.setPcUomid(c.getInt(14));
                    productMasterBO.setCaseUomId(c.getInt(15));
                    productMasterBO.setOuUomid(c.getInt(16));
                    productMasterBO.setChildProduct(true);

                    //updating isAccessory for PSKU if child(SKU) has salable and retunable is 0
                    if (productMasterBO.getIsSaleable() == 0 && productMasterBO.getIsReturnable() == 0) {
                        if (bmodel.productHelper.getProductMasterBOById(String.valueOf(productMasterBO.getParentid())) != null)
                            bmodel.productHelper.getProductMasterBOById(String.valueOf(productMasterBO.getParentid())).setAccessory(true);
                    }


                    // lstChildProducts.add(productMasterBO);
                    getProductMaster().add(productMasterBO);
                    productMasterById.put(productMasterBO.getProductID(), productMasterBO);

                }
            }
        }


    }

    private StringBuffer downloadProductSequenceFromFilter() {
        StringBuffer filter = new StringBuffer();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor cur = db
                .selectSQL("select HHTCode  from HhtMenuMaster where flag=1 and lower(MenuType)="
                        + bmodel.QT("FILTER").toLowerCase()
                        + " and lang='en' and RField1 =1 order by RField  desc");
        if (cur != null) {
            while (cur.moveToNext()) {
                if (cur.getString(0).equalsIgnoreCase("Filt11"))
                    filter.append("IsFocusBrand desc,");
                else if (cur.getString(0).equalsIgnoreCase("Filt12"))
                    filter.append("IsFocusBrand2 desc,");
                else if (cur.getString(0).equalsIgnoreCase("Filt10"))
                    filter.append("IsMustSell desc,");
                else
                    filter.append("");
            }
            cur.close();
        }
        db.closeDB();
        return filter;

    }

    /**
     * get tagged products and update the productBO.
     *
     * @param mMenuCode menu code
     */
    public void downloadTaggedProducts(String mMenuCode) {
        try {

            String productIds = getTaggingDetails(mMenuCode);
            List<String> mSKUId = new ArrayList<>();

            mSKUId = Arrays.asList(productIds.split(","));

            mTaggedProducts = new Vector<ProductMasterBO>();
            mTaggedProductById = new HashMap<String, ProductMasterBO>();

            if (productIds != null && !productIds.trim().equals("")) {
                for (ProductMasterBO sku : getProductMaster()) {
                    if (mSKUId.contains(sku.getProductID())) {
                        mTaggedProducts.add(sku);
                        mTaggedProductById.put(sku.getProductID(), sku);
                    }
                }
            } else {
                for (ProductMasterBO sku : getProductMaster()) {
                    mTaggedProducts.add(sku);
                    mTaggedProductById.put(sku.getProductID(), sku);
                }
            }

        } catch (Exception e) {
            Commons.printException("downloadTaggedProducts", e);
        }

    }


    /**
     * Method will return tagged products list as a string with comma separator.
     *
     * @param taggingType tagging type
     * @return productId with comma separated string.
     */
    public String getTaggingDetails(String taggingType) {
        try {
            String mappingId = "0", moduletypeid = "0", locationId = "0";

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            Cursor c1 = db
                    .selectSQL("SELECT criteriatype, TaggingTypelovID,criteriaid,locid FROM ProductTaggingCriteriaMapping PCM " +
                            "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid WHERE PM.TaggingTypelovID = "
                            + " (SELECT ListId FROM StandardListMaster WHERE ListCode = '"
                            + taggingType + "' AND ListType = 'PRODUCT_TAGGING') AND (PCM.distributorid=0 OR PCM.distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + ")");

            if (c1 != null) {
                if (c1.moveToNext()) {


                    if (c1.getString(0).equals("CHANNEL")) {
                        mappingId = bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "," + bmodel.getRetailerMasterBO().getSubchannelid();

                        if (c1.getInt(3) != 0)
                            locationId = bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "," + bmodel.getRetailerMasterBO().getLocationId();

                    } else if (c1.getString(0).equals("DISTRIBUTOR"))
                        mappingId = bmodel.getRetailerMasterBO().getDistributorId() + "";
                    else if (c1.getString(0).equals("LOCATION")) {
                        locationId = bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "," + bmodel.getRetailerMasterBO().getLocationId();
                    } else if (c1.getString(0).equals("USER"))
                        mappingId = bmodel.userMasterHelper.getUserMasterBO().getUserid() + "";

                    moduletypeid = c1.getString(1);
                }
                c1.close();
            }

            StringBuilder productIds = new StringBuilder();
            Cursor c2 = db
                    .selectSQL("SELECT pid FROM ProductTaggingCriteriaMapping PCM " +
                            "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
                            " INNER JOIN ProductTaggingGroupMapping PGM ON PGM.groupid=PM.groupid " +
                            "WHERE PM.TaggingTypelovID = " + moduletypeid +
                            " AND PCM.criteriaid IN(" + mappingId + ") AND locid IN(" + locationId + ") AND (PCM.distributorid=0 OR PCM.distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + ")");

            if (c2 != null) {
                while (c2.moveToNext()) {
                    if (!productIds.toString().equals(""))
                        productIds.append(",");
                    productIds.append(c2.getInt(0));
                }
                c2.close();
            }
            db.closeDB();

            return productIds.toString();
        } catch (Exception e) {
            return "";
        }
    }


    public void cloneReasonMaster() {
        try {
            for (ProductMasterBO product : productMaster) {
                product.setSalesReturnReasonList(cloneIsolateList(product));
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    protected static List<SalesReturnReasonBO> cloneIsolateList(ProductMasterBO product) {
        List<SalesReturnReasonBO> clone = null;
        try {
            clone = new ArrayList<SalesReturnReasonBO>();
            SalesReturnReasonBO item = new SalesReturnReasonBO();
            item.setCaseSize(product.getCaseSize());
            item.setOuterSize(product.getOutersize());
            item.setProductShortName(product.getProductShortName());
            item.setOldMrp(product.getMRP());
            item.setSrpedit(product.getSrp());
            clone.add(new SalesReturnReasonBO(item));
        } catch (Exception e) {
            Commons.printException(e);
        }
        return clone;
    }

    public int calculateSO(int ap3m, int closingStock, boolean isSBD,
                           int isInitiative, int sbdDropSize, int initiativeDropSize) {
        double so = 0;

        // Not a SBD or Initative SKU
        if ((!isSBD) && isInitiative != 1) {

            if (ap3m == 0 && (closingStock == 0 || closingStock == -1)) {// Never distributed
                so = 0;
            } else if (ap3m > 0 && closingStock == 0) {// Outof Stock condition
                so = ((float) ap3m * (1.0 + getBuffer()));
            } else { // Normal condition
                if (closingStock == -1)
                    so = ap3m;
                else
                    so = ap3m - closingStock;

            }

        } else if ((isSBD) && isInitiative != 1) { // SBD but not initiative
            if (ap3m == 0 && closingStock == 0) {// Never distributed
                so = sbdDropSize;
            } else if (ap3m > 0 && closingStock == 0) {// Outof Stock condition
                so = ((float) ap3m * (1.0 + getBuffer()));
            } else { // Normal condition
                so = ap3m - closingStock;
            }
        } else if ((!isSBD) && isInitiative == 1) { // initiative but not SBD
            if (ap3m == 0 && closingStock == 0) {// Never distributed
                so = 0;
            } else if (ap3m > 0 && closingStock == 0) {// OutofStock condition
                so = ((float) ap3m * (1.0 + getBuffer()));
            } else { // Normal condition
                so = ap3m - closingStock;
            }
            so = so > initiativeDropSize ? so : initiativeDropSize;
        } else if ((isSBD) && isInitiative == 1) {
            if (ap3m == 0 && closingStock == 0) {// Never distributed
                so = sbdDropSize;
            } else if (ap3m > 0 && closingStock == 0) {// Outof Stock condition
                so = ((float) ap3m * (1.0 + getBuffer()));
            } else { // Normal condition
                so = ap3m - closingStock;
            }
            so = so > initiativeDropSize ? so : initiativeDropSize;
        }

        int newso = (int) Math.ceil(so);

        return newso > 0 ? newso : 0;
    }

    /**
     * This method will check whether the product is an initiative product or
     * not and update in ProductBO. Also load the Drop size if the product is
     * initiative product. Data fetched from InitiativeDetailMaster table.
     */
    public void loadInitiativeProducts() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Vector<Integer> productIds = new Vector<Integer>();
            HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
            String sql = "select distinct(A.productInitId),case when B.initType='QTY' "
                    + "then A.TargetValue-ifnull(C.Qty,0) else 0 end from InitiativeDetailMaster A "
                    + "inner join InitiativeHeaderMaster B on A.initid=B.initid left join "
                    + "InitiativeAchievementMaster C on A.productInitId=C.ProductId and retailerid="
                    + QT(bmodel.retailerMasterBO.getRetailerID())
                    + " where A.LocalChannelId="
                    + bmodel.retailerMasterBO.getSubchannelid()
                    + " and B.IsCombination=0";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    productIds.add(c.getInt(0));
                    map.put(c.getInt(0), c.getInt(1));
                }
                c.close();
            }
            db.closeDB();
            for (int i = 0; i < productMaster.size(); i++) {
                ProductMasterBO p = productMaster.get(i);
                Integer prodId = Integer.valueOf(p.getProductID());
                if (productIds.contains(prodId)) {
                    p.setIsInitiativeProduct(1);
                    p.setInitDropSize(map.get(prodId));
                    productMaster.setElementAt(p, i);
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadOldBatchIDMap() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        try {
            oldBatchId = new HashMap<String, Integer>();
            oldBatchBasePrice = new HashMap<String, Double>();
            Cursor c = db
                    .selectSQL("SELECT BM.pid,BM.batchid,min(BM.mfgdate), IFNULL(PM.baseprice,0) AS base_price FROM BatchMaster BM LEFT JOIN ProductMaster PM ON BM.pid = PM.PID GROUP BY BM.pid");
            // SELECT pid,batchid,min(mfgdate) FROM BatchMaster GROUP BY pid
            if (c != null) {
                while (c.moveToNext()) {
                    oldBatchId.put(c.getString(0), c.getInt(1));
                    oldBatchBasePrice.put(c.getString(0), c.getDouble(3));
                }
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
    }

    protected int getOldBatchIDByMfd(String prodId) {
        try {
            return oldBatchId.get(prodId);
        } catch (Exception e) {
            return 0;
        }
    }


    public ProductMasterBO getProductMasterBOById(String productId) {
        if (productMasterById == null)
            return null;
        return productMasterById.get(productId);
    }


    public LoadManagementBO getLoadManagementBOById(int productId) {
        if (mLoadManagementBOByProductId == null)
            return null;
        return mLoadManagementBOByProductId.get(productId);
    }

    public ProductMasterBO getTaggedProductBOById(String productId) {
        if (mTaggedProductById == null)
            return null;
        return mTaggedProductById.get(productId);
    }


    /**
     * This method will set weather product is RPS product or not. Since its
     * based on the subchannel we are setting this after selecting the retailer.
     */
    public void loadSBDFocusData() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Vector<Integer> sbdDist = new Vector<Integer>();
            // Order Header
            String sql = "select productid from SbdDistributionMaster where Channelid="
                    + bmodel.retailerMasterBO.getChannelID() + "";
            Cursor c = db.selectSQL(sql);

            if (c != null) {
                // c.moveToFirst();
                while (c.moveToNext()) {
                    sbdDist.add(c.getInt(0));
                }
                c.close();
            }

            Vector<Integer> sbdDistAcheived = new Vector<Integer>();
            // Order Header
            String sql1 = "select productid from SbdDistributionMaster where Channelid="
                    + bmodel.retailerMasterBO.getChannelID()
                    + " and grpName in (select gname from SbdDistributionAchievedMaster where rid="
                    + bmodel.retailerMasterBO.getRetailerID() + ")";
            Cursor c1 = db.selectSQL(sql1);

            if (c1 != null) {
                while (c1.moveToNext()) {
                    sbdDistAcheived.add(c1.getInt(0));

                }
                c1.close();
            }

            db.closeDB();

            for (int i = 0; i < productMaster.size(); i++) {
                ProductMasterBO p = productMaster.get(i);
                Commons.print("sbdDist : " + sbdDist + "sbdDistAcheived : "
                        + sbdDistAcheived + "getProductID :  "
                        + p.getProductID());

                if (sbdDist.contains(Integer.valueOf(p.getProductID()))) {
                    p.setSBDProduct(true);
                    productMaster.setElementAt(p, i);
                }
                if (sbdDistAcheived.contains(Integer.valueOf(p.getProductID()))) {
                    p.setSBDAcheived(true);
                    productMaster.setElementAt(p, i);
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void clearOrderTableForInitiative() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);
            product.setOrderedPcsQty(0);
            product.setOrderedCaseQty(0);
            product.setOrderedOuterQty(0);
        }
    }

    public void clearOrderTable() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);
            product.setOrderedPcsQty(0);
            product.setOrderedCaseQty(0);
            product.setOrderedOuterQty(0);
            product.setLocalOrderPieceqty(0);
            product.setLocalOrderCaseqty(0);
            product.setLocalOrderOuterQty(0);
            // clear discount fields
            product.setD1(0);
            product.setD2(0);
            product.setD3(0);
            product.setDA(0);
            product.setSBDAcheivedLocal(false);
            // clear Crown Management Fields
            product.setCrownOrderedCaseQty(0);
            product.setCrownOrderedOuterQty(0);
            product.setCrownOrderedPieceQty(0);
            // clear Free Product Fields
            product.setFreeCaseQty(0);
            product.setFreeOuterQty(0);
            product.setFreePieceQty(0);
            product.setSchemeDiscAmount(0);
            product.setProductDiscAmount(0);
            product.setDistributorTypeDiscount(0);
            product.setCompanyTypeDiscount(0);
            int size = product.getLocations().size();
            for (int z = 0; z < size; z++) {
                product.getLocations().get(z).setShelfOuter(-1);
                product.getLocations().get(z).setShelfCase(-1);
                product.getLocations().get(z).setShelfPiece(-1);

                product.getLocations().get(z).setWHOuter(0);
                product.getLocations().get(z).setWHCase(0);
                product.getLocations().get(z).setWHPiece(0);
            }

            //clear delivered qty
            product.setDeliveredCaseQty(0);
            product.setDeliveredOuterQty(0);
            product.setDeliveredPcsQty(0);
        }


    }

    public void clearOrderTableAndUpdateSIH() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);

            if (product.isAllocation() == 1
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                int newsi = product.getOrderedPcsQty()
                        + (product.getOrderedCaseQty() * product.getCaseSize() + product
                        .getOrderedOuterQty() * product.getOutersize());
                product.setSIH(product.getSIH() >= newsi ? product.getSIH()
                        - newsi : 0);
            }
        }
    }

    public void clearOrderTableChecked() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);
            product.setOrderedPcsQty(0);
            product.setOrderedCaseQty(0);
            product.setOrderedOuterQty(0);
            product.setCheked(false);
        }
    }

    public float getBuffer() {
        return buffer;
    }

    public void setBuffer(float buffer) {
        this.buffer = buffer;
    }


    public boolean isMustSellFilled() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);
            if (product.getIsMustSell() == 1
                    && product.getOrderedCaseQty() == 0
                    && product.getOrderedPcsQty() == 0
                    && product.getOrderedOuterQty() == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isCSMustSellFilled() {//For counter Sales
        if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {

            ProductMasterBO product;
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);
                if (product.isChildProduct()) {
                    if (product.getIsMustSell() == 1
                            && product.getCsCase() == 0
                            && product.getCsOuter() == 0
                            && product.getCsPiece() == 0) {
                        return false;
                    }
                }
            }
            return true;

        } else {

            ProductMasterBO product;
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);
                if (product.getIsMustSell() == 1
                        && product.getCsCase() == 0
                        && product.getCsOuter() == 0
                        && product.getCsPiece() == 0) {
                    return false;
                }
            }
            return true;
        }

    }

    public void updateProductColor() {
        try {
            ProductMasterBO product;
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);

                if (product.getIsMustSell() == 1
                        && getFilterColor("Filt10") != 0)
                    product.setTextColor(getFilterColor("Filt10"));
                else if (product.getIsFocusBrand() == 1
                        && getFilterColor("Filt11") != 0)
                    product.setTextColor(getFilterColor("Filt11"));
                else if (product.getIsFocusBrand2() == 1
                        && getFilterColor("Filt12") != 0)
                    product.setTextColor(getFilterColor("Filt12"));
                else if (product.getIsFocusBrand3() == 1
                        && getFilterColor("Filt20") != 0)
                    product.setTextColor(getFilterColor("Filt20"));
                else if (product.getIsFocusBrand4() == 1
                        && getFilterColor("Filt21") != 0)
                    product.setTextColor(getFilterColor("Filt21"));
                else if (product.getIsNMustSell() == 1
                        && getFilterColor("Filt16") != 0)
                    product.setTextColor(getFilterColor("Filt16"));
                else if (product.isRPS() && getFilterColor("Filt02") != 0)
                    product.setTextColor(getFilterColor("Filt02"));
                else if (product.isPromo() && getFilterColor("Filt09") != 0)
                    product.setTextColor(getFilterColor("Filt09"));
                else if (product.getIsDiscountable() == 1
                        && getFilterColor("Filt18") != 0)
                    product.setTextColor(getFilterColor("Filt18"));
                else if (product.getIsNearExpiryTaggedProduct() == 1
                        && getFilterColor("Filt19") != 0)
                    product.setTextColor(getFilterColor("Filt19"));
                else if (product.getIsSMP() == 1
                        && getFilterColor("Filt22") != 0)
                    product.setTextColor(getFilterColor("Filt22"));
                else
                    product.setTextColor(mContext.getResources().getColor(
                            android.R.color.black));

                if (bmodel.configurationMasterHelper.SHOW_HIGHLIGHT_FOR_OOS
                        && product.getWSIH() == 0)
                    product.setTextColor(mContext.getResources().getColor(
                            R.color.RED));

            }
        } catch (Exception e) {

            Commons.printException(e);
        }

    }


    public void updateCounterSalesProductColor() {
        try {
            ProductMasterBO product;
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);

                if (product.getIsMustSell() == 1
                        && getFilterColor("Filt10") != 0)
                    product.setTextColor(getFilterColor("Filt10"));
                else if (product.getIsFocusBrand() == 1
                        && getFilterColor("Filt11") != 0)
                    product.setTextColor(getFilterColor("Filt11"));
                else
                    product.setTextColor(mContext.getResources().getColor(
                            android.R.color.black));

            }
        } catch (Exception e) {

            Commons.printException(e);
        }

    }

    private int getFilterColor(String filtername) {

        Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                .getGenFilter();
        try {
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getConfigCode()
                        .equalsIgnoreCase(filtername)) {
                    if (!genfilter.get(i).getMenuNumber().equals("0"))
                        return Color.parseColor((genfilter.get(i)
                                .getMenuNumber()));
                    else
                        return 0;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
            return 0;
        }
        return 0;

    }

    public boolean isSIHAvailable() {
        for (ProductMasterBO productBO : productMaster) {
            int qty = productBO.getOrderedPcsQty()
                    + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                    + (productBO.getOrderedOuterQty() * productBO
                    .getOutersize());
            if (qty > 0) {
                if (productBO.getSIH() < qty) {

                    return false;
                }
            }

        }
        return true;

    }


    public boolean isCheckCreditPeriod() {
        bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID());
        ArrayList<InvoiceHeaderBO> items = bmodel.getInvoiceHeaderBO();
        if (items != null && items.size() > 0) {

            for (InvoiceHeaderBO invoiceHeaderBo : items) {

                String invoiceDay = invoiceHeaderBo.getInvoiceDate();
                String currentDay = SDUtil.now(SDUtil.DATE_GLOBAL);
                int dayCount = DateUtil.getDateCount(invoiceDay, currentDay,
                        "yyyy/MM/dd");
                Commons.print("product Helper," + "dayCount " + dayCount);
                if (bmodel.getRetailerMasterBO().getCreditDays() == 0) {
                    return true;
                } else if (dayCount > bmodel.getRetailerMasterBO()
                        .getCreditDays()) {
                    return false;
                }

            }

        }
        return true;
    }


    private ArrayList<BomRetunBo> bomReturnProducts;
    private ArrayList<BomMasterBO> bomMaster;
    private ArrayList<BomRetunBo> bomReturnTypeProducts;

    /**
     * Download the isReturnable products and its Quantity from ProductMaster
     * and BomMaster, UomMaster and PriceMaster
     */
    public void downlaodReturnableProducts(String module) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

			/*
             * if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
			 * sb.append(
			 * "SELECT Distinct PM.Pid,PM.Bid,PM.Pname,PM.Barcode,PM.Psname,PM.TypeID,PM.basePrice,PM.Pcode FROM  BomMaster Bm Inner join ProductMaster PM on PM.pid = BM.BPid  and PM.Typeid=0"
			 * ); sb.append(" WHERE PM.isReturnable = 1 Order by PM.pid"); }
			 * else { sb.append(
			 * "SELECT Distinct PM.Pid,PM.Bid,PM.Pname,PM.Barcode,PM.Psname,0,PM.basePrice,PM.Pcode FROM  BomMaster Bm Inner join ProductMaster PM on PM.pid = BM.BPid "
			 * ); sb.append("WHERE PM.isReturnable = 1 Order by PM.pid"); }
			 */
            sb.append("SELECT Distinct PM.Pid,PM.parentid,PM.Pname,PM.Barcode,PM.Psname,PM.TypeID,PM.basePrice,PM.Pcode FROM  BomMaster Bm Inner join ProductMaster PM on PM.pid = BM.BPid");
            sb.append(" Where PM.TypeId NOT IN (SELECT ListID FROM StandardListMaster WHERE  ListCode ='GENERIC') AND PM.isReturnable = 1 Order by PM.pid");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                bomReturnProducts = new ArrayList<BomRetunBo>();
                while (c.moveToNext()) {
                    BomRetunBo bomMasterBO = new BomRetunBo();
                    bomMasterBO.setPid(c.getString(0));
                    bomMasterBO.setParentID(c.getInt(1));
                    bomMasterBO.setProductName(c.getString(2));
                    bomMasterBO.setBarcode(c.getString(3));
                    bomMasterBO.setProductShortName(c.getString(4));
                    int pieceUomid = 0;
                    double srp = 0;
                    LoadManagementBO loadManagementBO = null;
                    ProductMasterBO productBo = null;
                    if (module.equals("MENU_LOAD_MANAGEMENT")) {
                        int pid = Integer.parseInt(bomMasterBO.getPid());
                        loadManagementBO = mLoadManagementBOByProductId.get(pid);
                        pieceUomid = loadManagementBO.getPiece_uomid();
                        srp = loadManagementBO.getBaseprice();
                    } else {
                        productBo = getProductMasterBOById(bomMasterBO
                                .getPid());
                        pieceUomid = productBo.getPcUomid();
                        srp = productBo.getSrp();
                    }

                    if (productBo != null || loadManagementBO != null) {
                        bomMasterBO.setpSrp((float) srp);
                        bomMasterBO.setPieceUomId(pieceUomid);
                        bomMasterBO.setTypeId(c.getString(5));
                        bomMasterBO.setBasePrice(c.getFloat(6));
                        bomMasterBO.setProdCode(c.getString(7));
                        bomReturnProducts.add(bomMasterBO);
                    }
                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Method to Download ProductId, BomProdcutid,Qty from BOMMaster Table
     */
    public void downloadBomMaster() {

        BomMasterBO bomMasterBO = null;
        String temp = "-1";
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql = "Select Pid,BPid,UomID,Qty from BomMaster Order by Pid";

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                bomMaster = new ArrayList<BomMasterBO>();
                while (c.moveToNext()) {

                    if (!temp.equals(c.getString(0))) {

                        bomMasterBO = new BomMasterBO();
                        bomMasterBO.setPid(c.getString(0));

                        ArrayList<BomBO> bomBO1 = new ArrayList<BomBO>();
                        BomBO b = new BomBO();
                        b.setbPid(c.getString(1));
                        b.setUomID(c.getInt(2));
                        b.setQty(c.getInt(3));
                        bomBO1.add(b);
                        bomMasterBO.setBomBO(bomBO1);
                        bomMaster.add(bomMasterBO);
                        // Assign Productid to temp variable
                        temp = c.getString(0);

                    } else {
                        for (BomMasterBO mm : getBomMaster()) {

                            if (mm.getPid().equals(temp)) {
                                BomBO b = new BomBO();
                                b.setbPid(c.getString(1));
                                b.setUomID(c.getInt(2));
                                b.setQty(c.getInt(3));

                                mm.getBomBO().add(b);

                                break;
                            }
                        }
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * set Return Qty to the ReturnableBomMasterBo and set
     */
    public void setReturnQty() {

        for (BomRetunBo bom : getBomReturnProducts()) {
            bom.setLiableQty(0);
            bom.setReturnQty(0);
        }
        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
            for (BomRetunBo bom : getBomReturnTypeProducts()) {
                bom.setLiableQty(0);
                // bom.setReturnQty(0);
            }
        }

        for (ProductMasterBO sku : getProductMaster()) {
            for (BomMasterBO bomMasterBo : getBomMaster()) {

                if (sku.getProductID().equals(bomMasterBo.getPid())) {

                    for (BomBO bomBo : bomMasterBo.getBomBO()) {

                        if (bomBo.getUomID() == sku.getPcUomid())
                            bomBo.setTotalQty(bomBo.getQty()
                                    * (sku.getOrderedPcsQty()
                                    + sku.getCrownOrderedPieceQty() + sku
                                    .getFreePieceQty()));
                        else if (bomBo.getUomID() == sku.getOuUomid())
                            bomBo.setTotalQty(bomBo.getQty()
                                    * (sku.getOrderedOuterQty())
                                    + sku.getCrownOrderedOuterQty()
                                    + sku.getFreeOuterQty());
                        else if (bomBo.getUomID() == sku.getCaseUomId())
                            bomBo.setTotalQty(bomBo.getQty()
                                    * (sku.getOrderedCaseQty()
                                    + sku.getCrownOrderedCaseQty() + sku
                                    .getFreeCaseQty()));

                        for (BomRetunBo returnBo : getBomReturnProducts()) {
                            if (bomBo.getbPid().equals(returnBo.getPid())) {
                                returnBo.setLiableQty(returnBo.getLiableQty()
                                        + bomBo.getTotalQty());
                                break;
                            }
                        }
                    }

                    break;
                }
            }

        }

    }

    /**
     * Calculate total Order ReturnValue using Piece price * remaining Qty
     */
    public void calculateOrderReturnValue() {
        try {
            double balance = 0;

            for (BomRetunBo bomReturnBo : getBomReturnProducts()) {
                balance = balance
                        + ((bomReturnBo.getLiableQty() - bomReturnBo
                        .getReturnQty()) * bomReturnBo.getpSrp());
            }

            bmodel.getOrderHeaderBO().setRemainigValue(balance);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Calculate total Order ReturnValue(GroupWise) using Piece price *
     * remaining Qty
     */
    public void calculateOrderReturnTypeWiseValue() {
        try {
            double balance = 0;

            for (BomRetunBo bomReturnBo : getBomReturnTypeProducts()) {
                balance = balance
                        + ((bomReturnBo.getLiableQty() - bomReturnBo
                        .getReturnQty()) * bomReturnBo.getpSrp());

            }
            bmodel.getOrderHeaderBO().setRemainigValue(balance);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Update in OrderReturnDetail if it Return Products value modified Update
     * in OrderHeader if it Return Products value according to that Order value
     * modified
     */
    public void updateReturnProductValue() {
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            String uid = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));

            cursor = db
                    .selectSQL("select OrderID from OrderHeader where RetailerID="
                            + bmodel.getRetailerMasterBO().getRetailerID()
                            + " and upload='N'and invoicestatus = 0");
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                uid = QT(cursor.getString(0));

                db.deleteSQL(DataMembers.tbl_orderReturnDetails, "OrderID="
                        + uid, false);
            }
            cursor.close();

            ArrayList<BomRetunBo> returnProducts = null;
            String returncolumns = "OrderID,Pid,LiableQty,ReturnQty,Qty,Price, UomID,TypeID,LineValue,RetailerID";
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                returnProducts = bmodel.productHelper
                        .getBomReturnTypeProducts();
            } else {
                returnProducts = bmodel.productHelper.getBomReturnProducts();

            }
            String pid;
            for (BomRetunBo bomReturnBo : returnProducts) {

                if (bomReturnBo.getLiableQty() > 0
                        || bomReturnBo.getReturnQty() > 0) {

                    if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                        pid = "0";
                    else
                        pid = bomReturnBo.getPid();

                    String values = uid
                            + ","
                            + QT(pid)
                            + ","
                            + bomReturnBo.getLiableQty()
                            + ","
                            + bomReturnBo.getReturnQty()
                            + ","
                            + (bomReturnBo.getLiableQty() - bomReturnBo
                            .getReturnQty())
                            + ","
                            + bomReturnBo.getpSrp()
                            + ","
                            + bomReturnBo.getPieceUomId()
                            + ","
                            + bomReturnBo.getTypeId()
                            + ","
                            + ((bomReturnBo.getLiableQty() - bomReturnBo
                            .getReturnQty()) * bomReturnBo.getpSrp())
                            + ","
                            + QT(bmodel.getRetailerMasterBO().getRetailerID());

                    db.insertSQL(DataMembers.tbl_orderReturnDetails,
                            returncolumns, values);
                }

            }

            // Update the OrderHeader if ReturnProducts once Edited
            if (bmodel.getOrderHeaderBO().getOrderValue() > 0) {
                db.updateSQL("UPDATE "
                        + DataMembers.tbl_orderHeader
                        + " SET OrderValue ="
                        + QT(SDUtil.format(bmodel.getOrderHeaderBO()
                        .getOrderValue(), 2, 0)
                        + "")
                        + " "
                        + " ,ReturnValue ="
                        + QT(SDUtil.format(bmodel.getOrderHeaderBO()
                        .getRemainigValue(), 2, 0)
                        + "") + " WHERE RetailerID = "
                        + bmodel.getRetailerMasterBO().getRetailerID());
                // }

            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Method to download indicative order
     */
    public void downloadIndicativeOrder(int indicativeOrderId) {
        ProductMasterBO productBO;
        mIndicateOrderList = new ArrayList<ProductMasterBO>();

        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String query = "select uid,pid,op,oc,oo,RField_op,RField_oc,RField_oo from indicativeorder where rid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and uid=" + indicativeOrderId;
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    bmodel.getRetailerMasterBO().setIndicativeOrderid(
                            c.getString(0));
                    bmodel.getRetailerMasterBO().setIndicateFlag(1);

                    int productid = c.getInt(1);
                    productBO = new ProductMasterBO();
                    productBO.setProductID(productid + "");

                    productBO.setIndicativeOrder_op(c.getInt(2));
                    productBO.setIndicativeOrder_oc(c.getInt(3));
                    productBO.setIndicativeOrder_oo(c.getInt(4));
                    productBO.setIndicative_flex_op(c.getInt(5));
                    productBO.setIndicative_flex_oc(c.getInt(6));
                    productBO.setIndicative_flex_oo(c.getInt(7));

                    mIndicateOrderList.add(productBO);

                }

                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * update indicate orderDetails in productBO
     */
    public void updateIndicateOrder() {
        if (mIndicateOrderList != null && mIndicateOrderList.size() > 0) {
            //clear indicative data if multiple indicative id presents
            for (ProductMasterBO productMasterBO : getProductMaster()) {

                productMasterBO.setIndicativeOrder_oc(0);
                productMasterBO.setIndicativeOrder_op(0);
                productMasterBO.setIndicativeOrder_oo(0);

                productMasterBO.setIndicative_flex_op(0);
                productMasterBO.setIndicative_flex_oc(0);
                productMasterBO.setIndicative_flex_oo(0);

            }
            for (ProductMasterBO indicativeProductBO : mIndicateOrderList) {
                int prodcutID = Integer.parseInt(indicativeProductBO
                        .getProductID());
                if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                    // No need to update on ordered qty's
                    ProductMasterBO productBO = getProductMasterBOById(prodcutID + "");
                    productBO.setIndicativeOrder_oc(indicativeProductBO.getIndicativeOrder_oc());
                    productBO.setIndicativeOrder_op(indicativeProductBO.getIndicativeOrder_op());
                    productBO.setIndicativeOrder_oo(indicativeProductBO.getIndicativeOrder_oo());

                    productBO.setIndicative_flex_op(indicativeProductBO.getIndicative_flex_op());
                    productBO.setIndicative_flex_oc(indicativeProductBO.getIndicative_flex_oc());
                    productBO.setIndicative_flex_oo(indicativeProductBO.getIndicative_flex_oo());
                } else {

                    int op = indicativeProductBO.getIndicativeOrder_op();
                    int oc = indicativeProductBO.getIndicativeOrder_oc();
                    int oo = indicativeProductBO.getIndicativeOrder_oo();
                    setProductDetails(prodcutID, op, oc, oo);
                }
            }
        }
    }

    /**
     * check already order taken of mapped retailer for indicative order or not
     */
    public boolean isAlreadyIndicativeOrderTaken(int indicativeOrderId) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
          /*  Cursor indicativeCursor = db
                    .selectSQL("select distinct(uid) from indicativeorder where rid="
                            + QT(bmodel.getRetailerMasterBO().getRetailerID()));
            String uid = "";
            if (indicativeCursor.getCount() > 0) {
                if (indicativeCursor.moveToFirst()) {
                    uid = indicativeCursor.getString(0);
                }
            }*/
            Cursor c = db
                    .selectSQL("select orderid from orderheader where IndicativeOrderID ="
                            + indicativeOrderId + " and invoicestatus=1");
            if (c.getCount() > 0) {
                bmodel.getRetailerMasterBO().setIndicativeOrderid("");
                bmodel.getRetailerMasterBO().setIndicateFlag(0);

                if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                    // Invoice done. so clearing object.. so that user cannot do indicative order again
                    for (ProductMasterBO productMasterBO : getProductMaster())
                        productMasterBO.setIndicativeOrder_oc(0);
                }

                c.close();
                return true;
            }
            c.close();
            db.closeDB();
            return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }

    }

    /**
     * @param pid
     * @param op
     * @param oc
     * @param oo
     * @author rajesh.k update indicate order in corresponding product object
     */
    private void setProductDetails(int pid, int op, int oc, int oo) {

        ProductMasterBO productBO = getProductMasterBOById(pid + "");
        if (productBO != null) {

            int caseQty = 0;
            int outerQty = 0;

            int sih = productBO.getSIH();
            if (sih >= oc * productBO.getCaseSize() && sih > 0) {
                sih = sih - oc * productBO.getCaseSize();
                caseQty = oc;

            } else {
                if (productBO.getCaseSize() > 0 && sih > 0) {
                    caseQty = sih / productBO.getCaseSize();
                    sih = sih % productBO.getCaseSize();

                }

            }
            if (sih >= oo * productBO.getOutersize()) {
                sih = sih - oo * productBO.getOutersize();
                outerQty = oo;

            } else {
                if (productBO.getOutersize() > 0) {
                    outerQty = sih / productBO.getOutersize();
                    sih = sih % productBO.getOutersize();
                }
            }
            if (sih > op) {
                sih = op;
            }
            productBO.setOrderedPcsQty(sih);
            productBO.setOrderedCaseQty(caseQty);
            productBO.setOrderedOuterQty(outerQty);

        }

    }

    /**
     * Download ReturnPoducts to show in Groupwise
     */
    public void downloadTypeProducts() {
        DBUtil db = null;

        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            // sb.append("Select Distinct PM.Pid,PM.Pname From ProductMaster PM where PM.isReturnable =1 and TypeID  !=0 order by PM.Pid");
            sb.append("Select Distinct PM.Pid,PM.Pname,PM.Psname From ProductMaster PM INNER JOIN StandardListMaster SLM on PM.TypeId = SLM.ListId");
            sb.append(" and SLM.ListCode ='GENERIC' WHERE PM.isReturnable = 1 ORDER BY PM.Pid");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                bomReturnTypeProducts = new ArrayList<BomRetunBo>();
                while (c.moveToNext()) {
                    BomRetunBo bomMasterBO = new BomRetunBo();
                    bomMasterBO.setPid(c.getString(0));
                    bomMasterBO.setProductName(c.getString(1));
                    bomMasterBO.setProductShortName(c.getString(2));
                    ProductMasterBO productBo = getProductMasterBOById(bomMasterBO
                            .getPid());
                    float srpPrice = 0;
                    if (productBo != null) {
                        srpPrice = productBo.getSrp();
                        bomMasterBO.setpSrp(srpPrice);
                        bomMasterBO.setTypeId(c.getString(0));
                        bomMasterBO.setPieceUomId(productBo.getPcUomid());
                        bomReturnTypeProducts.add(bomMasterBO);
                    }
                }

                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void setGroupWiseReturnQty() {
        try {
            int total;

            for (BomRetunBo groupWiseProducts : bmodel.productHelper
                    .getBomReturnTypeProducts()) {
                total = 0;

                for (BomRetunBo bomReturnProducts : bmodel.productHelper
                        .getBomReturnProducts()) {

                    if (groupWiseProducts.getPid().equals(
                            bomReturnProducts.getTypeId())) {

						/*
                         * if (total == 0) {
						 * groupWiseProducts.setpSrp(bomReturnProducts
						 * .getpSrp());
						 * groupWiseProducts.setPieceUomId(bomReturnProducts
						 * .getPieceUomId());
						 * groupWiseProducts.setTypeId(bomReturnProducts
						 * .getTypeId()); }
						 */

                        total = total + bomReturnProducts.getLiableQty();
                    }

                }
                groupWiseProducts.setLiableQty(total);

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Check weather product Master has any Crown order.
     *
     * @return
     */
    public int getTotalCrownQty() {

        int totalQty = 0;
        try {
            int siz = getProductMaster().size();
            if (siz == 0)
                return 0;
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO product = getProductMaster()
                        .get(i);
                if (product.getCrownOrderedCaseQty() > 0
                        || product.getCrownOrderedPieceQty() > 0
                        || product.getCrownOrderedOuterQty() > 0) {
                    totalQty = totalQty
                            + (product.getCrownOrderedCaseQty()
                            * product.getCaseSize()
                            + product.getCrownOrderedPieceQty()
                            + product
                            .getCrownOrderedOuterQty()
                            * product.getOutersize());

                }

            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return totalQty;

    }

    public void clearBomReturnProductsTable() {
        try {
            for (BomRetunBo temp : getBomReturnProducts()) {
                temp.setLiableQty(0);
                temp.setReturnQty(0);
            }
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                for (BomRetunBo temp : getBomReturnTypeProducts()) {
                    temp.setLiableQty(0);
                    temp.setReturnQty(0);
                }
            }
            // Manually clear the objects in OrderHeaderBO
            bmodel.setOrderHeaderBO(null);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * get TotalIndicative Order value of all invoices
     *
     * @return
     */
    public double getTotalIndicativeOrderAmount() {
        double totalAmount = 0.0;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select IO.pid, sum(Qty)from IndicativeOrder  IO inner join OrderDetail OD ");
            sb.append("on OD.productid=IO.pid inner join OrderHeader OH on OD.OrderID=OH.OrderID ");
            sb.append("where IO.rid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            if (bmodel.configurationMasterHelper.IS_INVOICE) {
                sb.append(" and invoicestatus =1 ");
            }
            sb.append(" and OD.upload='N' GROUP BY IO.pid");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String productid = c.getString(0);
                    int totalQty = c.getInt(1);
                    if (mIndicateOrderList != null
                            && mIndicateOrderList.size() > 0) {
                        for (ProductMasterBO indicativeProductBO : mIndicateOrderList) {
                            if (productid.equals(indicativeProductBO
                                    .getProductID())) {
                                ProductMasterBO productBO = getProductMasterBOById(productid);
                                if (productBO != null) {
                                    int indcateOrderQty = indicativeProductBO
                                            .getIndicativeOrder_op()
                                            + indicativeProductBO
                                            .getIndicativeOrder_oc()
                                            * indicativeProductBO.getCaseSize()
                                            + indicativeProductBO
                                            .getIndicativeOrder_oo()
                                            * indicativeProductBO
                                            .getOutersize();
                                    if (indcateOrderQty < totalQty) {
                                        totalAmount = totalAmount
                                                + indcateOrderQty
                                                * productBO.getSrp();
                                    } else {
                                        totalAmount = totalAmount + totalQty
                                                * productBO.getSrp();
                                    }

                                }
                            }
                        }
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return 0.0;
        }
        return totalAmount;
    }


    public void downloadTaxDetails() {
        mTaxList = new ArrayList<TaxBO>();
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
                    + bmodel.getRetailerMasterBO().getTaxTypeId());
            sb.append(" AND PTM.PID = 0");


            // String query = " Select TM.TaxType,TM.TaxRate,TM.Sequence,SLM.ListName,TM.ParentType From TaxMaster TM INNER JOIN StandardListMaster SLM on SLM.Listid = TM.TaxType Where ApplyLevelId in (Select ListId from StandardListMaster  Where  ListCode='BILL')";
            // String query =
            // "select TaxType,TaxRate,Sequence,TaxDesc,ParentType from taxmaster  where applylevelid in (select listid from standardlistmaster where  listcode='INVOICE')";
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    taxBO = new TaxBO();
                    taxBO.setTaxType(c.getString(0));
                    taxBO.setTaxRate(c.getDouble(1));
                    taxBO.setSequence(c.getString(2));
                    taxBO.setTaxDesc(c.getString(3));
                    taxBO.setParentType(c.getString(4));
                    taxBO.setTaxTypeId(c.getInt(5));

                    mTaxList.add(taxBO);
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
    public void updateTaxList(String invoiceid, DBUtil db) {

        if (mTaxList != null) {
            String columns = "RetailerId,invoiceid,taxRate,taxType,taxValue";
            StringBuffer sb;
            for (TaxBO taxBO : mTaxList) {
                sb = new StringBuffer();
                sb.append(bmodel.QT(bmodel.getRetailerMasterBO()
                        .getRetailerID()) + ",");
                sb.append(bmodel.QT(invoiceid) + "," + taxBO.getTaxRate() + ",");
                sb.append(bmodel.QT(taxBO.getTaxType()) + ","
                        + SDUtil.format(taxBO.getTotalTaxAmount(), 2, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                db.insertSQL("InvoiceTaxDetails", columns, sb.toString());

            }
        }

    }

    /**
     * @param orderId
     * @param db
     * @author rajesh.k Method to use insert tax details in SQLite
     */
    public void updateTaxListInOrderId(String orderId, DBUtil db) {

        db.deleteSQL("OrderTaxDetails", "OrderID=" + orderId,
                false);
        if (mTaxList != null) {
            String columns = "RetailerId,orderid,taxRate,taxType,taxValue,pid";
            StringBuffer sb;
            for (TaxBO taxBO : mTaxList) {
                sb = new StringBuffer();
                sb.append(bmodel.QT(bmodel.getRetailerMasterBO()
                        .getRetailerID()) + ",");
                sb.append(orderId + "," + taxBO.getTaxRate() + ",");
                sb.append(bmodel.QT(taxBO.getTaxType()) + ","
                        + SDUtil.roundIt(taxBO.getTotalTaxAmount(), 2) + "," + "0");
                db.insertSQL("OrderTaxDetails", columns, sb.toString());

            }
        }

    }


    /**
     * @param invoiceid
     * @author Felix Method to use insert product tax details in SQLite
     */
    public void updateProductTaxList(String invoiceid, DBUtil db) {

        if (mTaxProdList != null) {//
            String columns = "RetailerId,invoiceid,pid,taxRate,taxType,taxValue";
            StringBuffer sb;
            for (TaxBO taxBO : mTaxProdList) {
                for (ProductMasterBO sku : productMaster) {
                    int taxSize = sku.getTaxes().size();
                    for (int i = 0; i < taxSize; i++) {
                        if (sku.getProductID().equals(taxBO.getPid() + "")
                                && sku.getTaxes().get(i).getTaxType()
                                .equals(taxBO.getTaxType())) {
                            if (sku.getOrderedCaseQty() > 0
                                    || sku.getOrderedPcsQty() > 0
                                    || sku.getOrderedOuterQty() > 0) {

                                double taxValue = (sku.getOrderedPcsQty() * ((sku
                                        .getSrp() * sku.getTaxes().get(i)
                                        .getTaxRate()) / 100))
                                        + (sku.getOrderedCaseQty() * ((sku
                                        .getCsrp() * sku.getTaxes()
                                        .get(i).getTaxRate()) / 100))
                                        + (sku.getOrderedOuterQty() * ((sku
                                        .getOsrp() * sku.getTaxes()
                                        .get(i).getTaxRate()) / 100));

                                sb = new StringBuffer();
                                sb.append(bmodel.QT(bmodel
                                        .getRetailerMasterBO().getRetailerID())
                                        + ",");
                                sb.append(bmodel.QT(invoiceid) + ","
                                        + sku.getProductID() + ","
                                        + sku.getTaxes().get(i).getTaxRate()
                                        + ",");
                                sb.append((taxBO.getTaxTypeId()) + ","
                                        + taxValue);
                                db.insertSQL("InvoiceTaxDetails", columns,
                                        sb.toString());
                            }
                        }
                    }
                }
            }
        }

    }

    ArrayList<Integer> mOrderType = new ArrayList<Integer>();

    public ArrayList<Integer> getmOrderType() {
        return mOrderType;
    }

    public void setmOrderType(ArrayList<Integer> mOrderType) {
        this.mOrderType = mOrderType;
    }

    public void downloadOrdeType() {
        ArrayList<Integer> orderTypeList = new ArrayList<Integer>();
        DBUtil db = null;
        StringBuffer sb = new StringBuffer();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            sb.append("SELECT Distinct ");
            sb.append(" IFNULL((Select ListID from StandardListMaster where ListCode='NORMAL'),1) as Normal,");
            sb.append(" IFNULL((Select ListID from StandardListMaster where ListCode='SCHEME'),2)  as Scheme,");
            sb.append(" IFNULL((Select ListID from StandardListMaster where ListCode='CROWN'),3) as Crown,");
            sb.append(" IFNULL((Select ListID from StandardListMaster where ListCode='FREE'),4) as Free");
            sb.append(" FROM StandardListMaster where ListType='SCHEME_SUB_TYPE'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.moveToNext()) {
                    orderTypeList.add(c.getInt(0));
                    orderTypeList.add(c.getInt(1));
                    orderTypeList.add(c.getInt(2));
                    orderTypeList.add(c.getInt(3));

                    setmOrderType(orderTypeList);
                } else {
                    orderTypeList.add(0, 0);
                    orderTypeList.add(1, 0);
                    orderTypeList.add(2, 0);
                    orderTypeList.add(3, 0);
                    setmOrderType(orderTypeList);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * @param uid  - Either orderid or invoice id
     * @param flag - 1 - invoice ,0 - order
     *             <p>
     *             Method to save bottle return values in Order return details or
     *             invoice return details
     * @author rajesh.k
     */
    public void saveReturnDetails(String uid, int flag, DBUtil db) {

        ArrayList<BomRetunBo> returnProducts = null;
        String tableName = "";
        String returncolumns = "";
        if (flag == 1) {
            tableName = "InvoiceReturnDetail";
            returncolumns = "invoiceID,Pid,LiableQty,ReturnQty,Qty,Price, UomID,TypeID,LineValue,RetailerID";

        } else {
            tableName = "OrderReturnDetail";
            returncolumns = "OrderID,Pid,LiableQty,ReturnQty,Qty,Price, UomID,TypeID,LineValue,RetailerID";
        }

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
            returnProducts = getBomReturnTypeProducts();
        } else {
            returnProducts = getBomReturnProducts();

        }
        String pid;
        for (BomRetunBo bomReturnBo : returnProducts) {
            StringBuffer sb = new StringBuffer();
            if (bomReturnBo.getLiableQty() > 0
                    || bomReturnBo.getReturnQty() > 0) {

                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                    pid = "0";
                else
                    pid = bomReturnBo.getPid();
                sb.append(uid + "," + QT(pid) + ","
                        + bomReturnBo.getLiableQty() + ",");
                sb.append(bomReturnBo.getReturnQty()
                        + ","
                        + (bomReturnBo.getLiableQty() - bomReturnBo
                        .getReturnQty()) + ",");
                sb.append(bomReturnBo.getpSrp() + ","
                        + bomReturnBo.getPieceUomId() + ","
                        + bomReturnBo.getTypeId() + ",");
                sb.append((bomReturnBo.getLiableQty() - bomReturnBo
                        .getReturnQty()) * bomReturnBo.getpSrp() + ",");
                sb.append(QT(bmodel.getRetailerMasterBO().getRetailerID()));

                db.insertSQL(tableName, returncolumns, sb.toString());
            }

        }
        // If Flag == 1(Creating Invoice) and Group wise Product Return Enabled
        if (flag == 1
                && bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            saveOrUpdateReturnProductDetails();

    }

    public boolean isGoldenStoreInCurrentandLastVisit() {
        int merchtargetpercent = 0;
        int disttargetpercent = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;

            c = db.selectSQL("select MNumber from "
                    + DataMembers.tbl_HhtMenuMaster
                    + "  where  HHTCode='CallA6'");
            if (c != null) {
                while (c.moveToNext()) {
                    merchtargetpercent = c.getInt(0);
                }
            }

            c = db.selectSQL("select MNumber from  "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where HHTCode='CallA13' or  HHTCode='CallA14'");
            if (c != null) {
                while (c.moveToNext()) {
                    disttargetpercent = c.getInt(0);
                }
                c.close();
            }

            db.closeDB();

            float sbdDistTarget = (float) bmodel.getRetailerMasterBO()
                    .getSbdDistributionTarget()
                    * (float) disttargetpercent
                    / 100;

            float sbdMerchTarget = (float) bmodel.getRetailerMasterBO()
                    .getSBDMerchTarget() * (float) merchtargetpercent / 100;

            if (sbdDistTarget <= bmodel.getRetailerMasterBO()
                    .getSbdDistAchieved()
                    && sbdMerchTarget <= bmodel.retailerMasterBO
                    .getSBDMerchAchieved()) {
                // Update If the store achieves the Golden Store
                // status in DailyTargetPlanned :added by chiru for P&G Kenya
                bmodel.orderAndInvoiceHelper.updateGoldenStoreDetails(1);

                if (WasGoldStore()) {
                    return true;
                }
            }

        } catch (SQLException e) {
            Commons.printException(e);
        }

        return false;

    }

    public double applyGoldStoreLineDiscount() {
        int discount = 0, type = 0;

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select discount,type from StoreWiseDiscount where retailerid="
                        + bmodel.getRetailerMasterBO().getRetailerID()
                        + " and code=(select ListId from StandardListMaster where ListCode='GLDSTORE')");
        if (c != null) {
            while (c.moveToNext()) {
                discount = c.getInt(0);
                type = c.getInt(1);
            }
            c.close();
        }
        db.closeDB();

        return discount;
    }

    private boolean WasGoldStore() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;

            c = db.selectSQL("select IFNULL(wasGoldStore,'') from RetailerMaster where RetailerID="
                    + bmodel.retailerMasterBO.getRetailerID());
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getString(0).equals("Y"))
                        flag = true;
                    else
                        flag = false;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    private void saveOrUpdateReturnProductDetails() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql, values;
            Cursor cursor;
            String columns = "Pid,Qty";
            // Get the Values from OrderRetrun Details and Insert in to the
            // OrderReturnQty Table
            sql = "SELECT Pid FROM OrderReturnQty Group by Pid";
            cursor = db.selectSQL(sql);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    for (BomRetunBo bomReturnBo : getBomReturnTypeProducts()) {
                        if (bomReturnBo.getLiableQty() > 0
                                || bomReturnBo.getReturnQty() > 0) {
                            String pid = cursor.getString(0);
                            if (pid.equals(bomReturnBo.getTypeId())) {
                                sql = "UPDATE OrderReturnQty SET QTY = QTY+"
                                        + bomReturnBo.getReturnQty()
                                        + " WHERE PID ="
                                        + bomReturnBo.getTypeId();
                                db.updateSQL(sql);

                            }
                        }
                    }
                }
                cursor.close();
            } else {
                for (BomRetunBo bomReturnBo : getBomReturnTypeProducts()) {

                    values = bomReturnBo.getTypeId() + ","
                            + bomReturnBo.getReturnQty();
                    db.insertSQL("OrderReturnQty", columns, values);

                }

            }
            cursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Download the Generic SKu Product Id From ProductMaster and BomMaster
     */
    public void downloadGenericProductID() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor cur = db
                    .selectSQL("SELECT  B.Bpid,A.PID,A.Pname  FROM ProductMaster A inner JOIN BomMaster B on A.pid = B.pid WHERE A.isReturnable = '1' Order by A.pid");

            if (cur != null) {
                while (cur.moveToNext()) {
                    for (BomRetunBo product : getBomReturnProducts()) {

                        if (product.getPid().equals(cur.getString(0))) {
                            product.setTypeId(cur.getString(1));
                            break;
                        }
                    }

                }
                cur.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Download In store Locations from SatandardListMaster of Type PL
     */
    public void downloadInStoreLocations() {
        try {

            // Return if size greater than 0. No need to fire query.
            if (inStoreLocation.size() > 0)
                return;

            inStoreLocation = new Vector<StandardListBO>();
            StandardListBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL  where SL.Listtype='PL' ORDER BY SL.ListId";

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new StandardListBO();
                    locations.setListID(c.getString(0));
                    locations.setListName(c.getString(1));
                    inStoreLocation.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (inStoreLocation.size() == 0) {
                locations = new StandardListBO();
                locations.setListID("0");
                locations.setListName("Store");
                inStoreLocation.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }

    //Global Category Selection based on config FUN24
    public void downloadGloabalCategory() {
        try {

            // Return if size greater than 0. No need to fire query.
            if (globalCategory.size() > 0)
                return;

            globalCategory = new Vector<LevelBO>();
            LevelBO levelBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String query = "SELECT DISTINCT PM1.PID, PM1.PName, PL.LevelId FROM ProductMaster PM1"
                    + " INNER JOIN ProductLevel PL on PL.Sequence =" + bmodel.configurationMasterHelper.globalSeqId
                    + " WHERE PM1.PLid =  PL.LevelId Order By PM1.RowId";

            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {
                    levelBO = new LevelBO();
                    levelBO.setProductID(c.getInt(0));
                    levelBO.setLevelName(c.getString(1));
                    levelBO.setParentID(c.getInt(2));
                    globalCategory.add(levelBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("Download Category", e);
        }

    }

    /**
     * Download Module Locations
     */
    public void downloadStockApply() {
        try {

            inStoreLocation = new Vector<StandardListBO>();
            StandardListBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL  where SL.Listtype='PL' ORDER BY SL.ListId";

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new StandardListBO();
                    locations.setListID(c.getString(0));
                    locations.setListName(c.getString(1));
                    inStoreLocation.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (inStoreLocation.size() == 0) {
                locations = new StandardListBO();
                locations.setListID("0");
                locations.setListName("Store");
                inStoreLocation.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }


    /**
     * Download Module Locations
     */
    public void downloadPlanogramProdutLocations(String moduleName, String retailer, String query1) {
        try {

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);

            inStoreLocation = new Vector<StandardListBO>();
            StandardListBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL";

            if (moduleName.equals("MENU_PLANOGRAM")) {
                sql1 += " inner join PlanogramMapping MP on MP.StoreLocId=SL.ListId"
                        + " inner join PlanogramMaster P on P.HId=MP.HId"
                        + " where " + query1
                        + " SL.Listtype='PL'"
                        + " ORDER BY SL.ListId";
            } else if (moduleName.equals("MENU_VAN_PLANOGRAM")) {
                sql1 += " inner join PlanogramMapping PM on PM.StoreLocId=sl.listcode"
                        + " inner join PlanogramMaster P on P.HId=PM.HId"
                        + " where SL.Listtype='SL' and PM.RetailerId= " + bmodel.QT(retailer)
                        + " ORDER BY SL.ListId";
            } else {
                sql1 += " where SL.Listtype='PL' ORDER BY SL.ListId";
            }

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new StandardListBO();
                    locations.setListID(c.getString(0));
                    locations.setListName(c.getString(1));
                    inStoreLocation.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (inStoreLocation.size() == 0) {
                locations = new StandardListBO();
                locations.setListID("0");
                locations.setListName("Store");
                inStoreLocation.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }


    public ArrayList<TaxBO> getTaxList() {
        if (mTaxList != null) {
            return mTaxList;
        }
        return new ArrayList<TaxBO>();
    }

    public ArrayList<TaxTempBO> getTaxSumProdList() {
        if (mTaxSumProdList != null) {
            return mTaxSumProdList;
        }
        return new ArrayList<TaxTempBO>();
    }

    public ArrayList<BomRetunBo> getBomReturnProducts() {
        return bomReturnProducts;
    }

    public ArrayList<BomMasterBO> getBomMaster() {
        return bomMaster;
    }

    public ArrayList<BomRetunBo> getBomReturnTypeProducts() {
        return bomReturnTypeProducts;
    }

    private Vector<LoadManagementBO> productlist;

    public Vector<LoadManagementBO> loadProductsWithFiveLevel(
            String moduleCode, String batchmenucode) {
        mLoadManagementBOByProductId = new SparseArray<>();
        String sql = "", sql1 = "", sql2 = "", sql3 = "";
        productlist = new Vector<>();
        LoadManagementBO bo;
        Vector<LoadManagementBO> batchno;
        Vector<LoadManagementBO> list;
        LoadManagementBO batchnobo;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        int mChildLevel = 0;
        int mContentLevel = 0;
        int mFilterLevel = 0;

        if (sequencevalues != null) {
            if (sequencevalues.size() > 0) {
                mChildLevel = sequencevalues.size();
            }
        }


        if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")
                || batchmenucode.equals("MENU_VAN_UNLOAD")
                || batchmenucode.equals("MENU_CUR_STK_BATCH")
                || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
            sql = "  LEFT JOIN StockInHandMaster SIH ON SIH.pid=PM.PID"
                    + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid AND PM.pid=BM.pid)";
            sql1 = ",IFNULL(BM.batchNum,'') as batchNum,SIH.qty as qty,SIH.adjusted_qty,SIH.batchid";
        } else {
            sql = "";
            sql1 = "";
        }
        if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
            sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM.PID";
            sql3 = " ,A.qty, A.pcsQty, A.caseQty, A.outerQty";
        } else {
            sql2 = "";
            sql3 = "";
        }
        String query = "";
        if (mChildLevel == 0) {
            query = "SELECT PM.ParentId, PM.PID, PM.PName, PM.suggestqty, PM.psname, PM.dUomQty,"
                    + " PM.sih, PWHS.Qty, PM.IsAlloc, PM.mrp, PM.barcode, PM.RField1, PM.dOuomQty,"
                    + " PM.isMust, PM.maxQty,(select qty from ProductStandardStockMaster PSM  where uomid =PM.piece_uomid and PM.PID = PSM.PID) as stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM.dUomId and PM.PID = PSM.PID) as stdcase,(select qty from ProductStandardStockMaster PSM where uomid =PM.dOuomid and PM.PID = PSM.PID) as stdouter, PM.dUomId, PM.dOuomid,"
                    + " PM.baseprice, PM.piece_uomid, PM.PLid, PM.pCode, PM.msqQty, PM.issalable" // + ",(CASE WHEN PWHS.PID=PM.PID then 'true' else 'false' end) as IsAvailWareHouse"
                    + sql3
                    + sql1
                    + " FROM ProductMaster PM"
                    + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM.pid and PWHS.UomID=PM.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                    + sql2
                    + sql
                    + " WHERE PM.PLid IN"
                    + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + bmodel.QT(moduleCode) + ")";
        } else {
            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                            + " FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter"
                            + mChildLevel
                            + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                            + " WHERE CF.ActivityCode = " + bmodel.QT(moduleCode));

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mFilterLevel = filterCur.getInt(0);
                    mContentLevel = filterCur.getInt(1);
                }
                filterCur.close();
            }
            int loopEnd = mContentLevel - mFilterLevel + 1;
            query = "select PM1.PID, PM"
                    + loopEnd
                    + ".PID, PM"
                    + loopEnd
                    + ".PName, PM"
                    + loopEnd
                    + ".suggestqty,"
                    + " PM"
                    + loopEnd
                    + ".psname, PM"
                    + loopEnd
                    + ".dUomQty, PM"
                    + loopEnd
                    + ".sih, PWHS.Qty,PM"
                    + loopEnd
                    + ".IsAlloc, PM"
                    + loopEnd
                    + ".mrp, PM"
                    + loopEnd
                    + ".barcode, PM"
                    + loopEnd
                    + ".RField1,"
                    + " PM"
                    + loopEnd
                    + ".dOuomQty, PM"
                    + loopEnd
                    + ".isMust, PM"
                    + loopEnd
                    + ".maxQty,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".piece_uomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".dUomId and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdcase, (select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd + ".dOuomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as stdouter, PM" + loopEnd
                    + ".dUomId, PM" + loopEnd + ".dOuomid," + " PM" + loopEnd
                    + ".baseprice, PM" + loopEnd + ".piece_uomid, PM" + loopEnd
                    + ".PLid, PM" + loopEnd + ".pCode," + " PM" + loopEnd
                    + ".msqQty, PM" + loopEnd + ".issalable" /*+ ",(CASE WHEN PWHS.PID=PM" + loopEnd + ".PID then 'true' else 'false' end) as IsAvailWareHouse" */ + sql3 + sql1
                    + " FROM ProductMaster PM1";
            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM"
                        + i + ".ParentId = PM" + (i - 1) + ".PID";
            query = query + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM" + loopEnd + ".pid and PWHS.UomID=PM" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)";
            sql = " LEFT JOIN StockInHandMaster SIH ON SIH.pid = PM" + loopEnd
                    + ".PID"
                    + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid  AND BM.pid="
                    + "PM"
                    + loopEnd
                    + ".PID)";
            sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM" + loopEnd
                    + ".PID";


            query = query
                    + sql2
                    + sql
                    + " WHERE PM1.PLid IN (SELECT ProductFilter" + mChildLevel + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = " + bmodel.QT(moduleCode) + ")"
                    + " ORDER BY PM" + loopEnd + ".rowid";
        }
        Cursor c = db.selectSQL(query);
        if (c != null) {
            while (c.moveToNext()) {
                batchno = new Vector<LoadManagementBO>();
                bo = new LoadManagementBO();
                bo.setParentid(c.getInt(0));
                bo.setProductid(c.getInt(1));
                bo.setProductname(c.getString(2));
                bo.setSuggestqty(c.getInt(3));
                bo.setProductshortname(c.getString(4));
                bo.setCaseSize(c.getInt(5));
                bo.setSih(c.getInt(6));
                bo.setWsih(c.getInt(7));
                bo.setIsalloc(c.getInt(8));
                bo.setMrp(c.getDouble(9));
                bo.setBarcode(c.getString(10));
                bo.setRField1(c.getString(11));
                bo.setOuterSize(c.getInt(12));
                bo.setIsMust(c.getInt(13));
                bo.setMaxQty(c.getInt(14));
                bo.setStdpcs(c.getInt(15));
                bo.setStdcase(c.getInt(16));
                bo.setStdouter(c.getInt(17));
                bo.setdUomid(c.getInt(18));
                bo.setdOuonid(c.getInt(19));
                bo.setBaseprice(c.getDouble(20));
                bo.setPiece_uomid(c.getInt(21));
                bo.setPLid(c.getInt(22));
                bo.setpCode(c.getString(23));
                bo.setMsqQty(c.getInt(24));
                bo.setIssalable(c.getInt(25));
                if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
                    bo.setStkprototalQty(c.getInt(c.getColumnIndex("qty")));
                    bo.setStkpropcsqty(c.getInt(c.getColumnIndex("pcsQty")));
                    bo.setStkprocaseqty(c.getInt(c.getColumnIndex("caseQty")));
                    bo.setStkproouterqty(c.getInt(c.getColumnIndex("outerQty")));
                }
                if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")
                        || batchmenucode.equals("MENU_VAN_UNLOAD")
                        || batchmenucode.equals("MENU_CUR_STK_BATCH")
                        || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
                    bo.setBatchNo(c.getString(c.getColumnIndex("batchNum")));
                    bo.setStocksih(c.getInt(c.getColumnIndex("qty")));
                    bo.setOld_diff_sih(c.getInt(c
                            .getColumnIndex("adjusted_qty")));
                    bo.setBatchId(c.getString(c.getColumnIndex("batchid")));
                }

                list = new Vector<LoadManagementBO>();
                LoadManagementBO ret;

                for (int i = 0; i < 3; ++i) {
                    ret = new LoadManagementBO();
                    ret.setProductid(c.getInt(0));
                    ret.setCaseqty(0);
                    ret.setPieceqty(0);
                    ret.setOuterQty(0);
                    ret.setBatchNo("");
                    list.add(ret);
                }

                bo.setBatchlist(list);
                mLoadManagementBOByProductId.put(bo.getProductid(), bo);

                productlist.add(bo);

            }

            c.close();
        }

        Cursor c1 = db
                .selectSQL("select batchNum, batchid, Pid from BatchMaster order by Pid");

        if (c1 != null) {
            int temp = -1;
            Vector<LoadManagementBO> batchno1 = null;
            HashMap<Integer, Vector<LoadManagementBO>> prodBatchList = new HashMap<>();
            while (c1.moveToNext()) {

                if (temp != c1.getInt(2)) {
                    prodBatchList.put(temp, batchno1);
                    batchno1 = new Vector<LoadManagementBO>();
                    temp = c1.getInt(2);
                }

                batchnobo = new LoadManagementBO();
                batchnobo.setBatchNo(c1.getString(0));
                batchnobo.setBatchId(c1.getString(1));
                batchno1.add(batchnobo);
            }
            c1.close();

            temp = productlist.size();
            for (int i = 0; i < temp; i++) {
                LoadManagementBO load = productlist.get(i);
                if (prodBatchList.get(load.getProductid()) != null) {
                    int s1 = load.getBatchlist().size();
                    for (int j = 0; j < s1; j++) {
                        LoadManagementBO ret = load.getBatchlist().get(j);
                        ret.setBatchnolist(cloneVanloadList(prodBatchList.get(load.getProductid())));
                    }
                }

            }
        }

        db.closeDB();
        return productlist;
    }

    public Vector<LoadManagementBO> loadProducts(String moduleCode,
                                                 String batchmenucode) {
        mLoadManagementBOByProductId = new SparseArray<>();
        String sql = "", sql1 = "", sql2 = "", sql3 = "";
        productlist = new Vector<LoadManagementBO>();
        LoadManagementBO bo;
        Vector<LoadManagementBO> batchno;
        Vector<LoadManagementBO> list;
        LoadManagementBO batchnobo;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        int mParentLevel = 0;
        int mChildLevel = 0;
        int mContentLevel = 0;

        Cursor filterCur = db
                .selectSQL("SELECT IFNULL(PL1.Sequence,0),IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                        + " FROM ConfigActivityFilter CF"
                        + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                        + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                        + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                        + " WHERE CF.ActivityCode = " + bmodel.QT(moduleCode));

        if (filterCur != null) {
            if (filterCur.moveToNext()) {
                mParentLevel = filterCur.getInt(0);
                mChildLevel = filterCur.getInt(1);
                mContentLevel = filterCur.getInt(2);
            }
            filterCur.close();
        }
        if (batchmenucode.equals("MENU_VAN_UNLOAD")
                || batchmenucode.equals("MENU_CUR_STK_BATCH")
                || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
            sql = "  LEFT JOIN StockInHandMaster SIH ON SIH.pid=PM.PID"
                    + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid and PM.PID=BM.pid)";
            sql1 = ",IFNULL(BM.batchNum,'') as batchNum,SIH.qty as qty,SIH.adjusted_qty,SIH.batchid";
        } else {
            sql = "";
            sql1 = "";
        }
        if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
            sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM.PID";
            sql3 = " ,A.qty, A.pcsQty, A.caseQty, A.outerQty";
        } else {
            sql2 = "";
            sql3 = "";
        }
        String query = "";
        if (mParentLevel == 0 && mChildLevel == 0) {
            query = "SELECT  PM.ParentId, PM.PID, PM.PName, PM.suggestqty, PM.psname, PM.dUomQty,"
                    + " PM.sih, PWHS.Qty, PM.IsAlloc, PM.mrp, PM.barcode, PM.RField1, PM.dOuomQty,"
                    + " PM.isMust, PM.maxQty,(select qty from ProductStandardStockMaster PSM  where uomid =PM.piece_uomid and PM.PID = PSM.PID) as stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM.dUomId and PM.PID = PSM.PID) as stdcase,(select qty from ProductStandardStockMaster PSM where uomid =PM.dOuomid and PM.PID = PSM.PID) as stdouter, PM.dUomId, PM.dOuomid,"
                    + " PM.baseprice, PM.piece_uomid, PM.PLid, PM.pCode, PM.msqQty, PM.issalable" //+ ",(CASE WHEN PWHS.PID=PM.PID then 'true' else 'false' end) as IsAvailWareHouse "
                    + sql3
                    + sql1
                    + " FROM ProductMaster PM"
                    + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM.pid and PWHS.UomID=PM.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                    + sql2
                    + sql
                    + " WHERE PM.PLid IN"
                    + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + bmodel.QT(moduleCode) + ")";
        } else {

            int loopEnd = 0;
            String parentFilter = "";
            if (mChildLevel != 0) {
                loopEnd = mContentLevel - mChildLevel + 1;
                parentFilter = "ProductFilter2";
            } else {
                loopEnd = mContentLevel - mParentLevel + 1;
                parentFilter = "ProductFilter1";
            }
            query = "select PM1.PID, PM"
                    + loopEnd
                    + ".PID, PM"
                    + loopEnd
                    + ".PName, PM"
                    + loopEnd
                    + ".suggestqty,"
                    + " PM"
                    + loopEnd
                    + ".psname, PM"
                    + loopEnd
                    + ".dUomQty, PM"
                    + loopEnd
                    + ".sih, PWHS.Qty,PM"
                    + loopEnd
                    + ".IsAlloc, PM"
                    + loopEnd
                    + ".mrp, PM"
                    + loopEnd
                    + ".barcode, PM"
                    + loopEnd
                    + ".RField1,"
                    + " PM"
                    + loopEnd
                    + ".dOuomQty, PM"
                    + loopEnd
                    + ".isMust, PM"
                    + loopEnd
                    + ".maxQty,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".piece_uomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".dUomId and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdcase, (select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd + ".dOuomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as stdouter, PM" + loopEnd
                    + ".dUomId, PM" + loopEnd + ".dOuomid," + " PM" + loopEnd
                    + ".baseprice, PM" + loopEnd + ".piece_uomid, PM" + loopEnd
                    + ".PLid, PM" + loopEnd + ".pCode," + " PM" + loopEnd
                    + ".msqQty, PM" + loopEnd + ".issalable" /*+ ",(CASE WHEN PWHS.PID=PM" + loopEnd + ".PID then 'true' else 'false' end) as IsAvailWareHouse " */ + sql3 + sql1
                    + " FROM ProductMaster PM1";
            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM"
                        + i + ".ParentId = PM" + (i - 1) + ".PID";
            query = query + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM" + loopEnd + ".pid and PWHS.UomID=PM" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)";
            if (batchmenucode.equals("MENU_VAN_UNLOAD")
                    || batchmenucode.equals("MENU_CUR_STK_BATCH")
                    || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
                sql = " LEFT JOIN StockInHandMaster SIH ON SIH.pid = PM" + loopEnd
                        + ".PID"
                        + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM .batchid AND BM.pid = PM" +
                        +loopEnd + ".PID)";
            } else {
                sql = "";
            }
            if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
                sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM" + loopEnd
                        + ".PID";
            } else {
                sql2 = "";
            }

            query = query + sql2 + sql + " WHERE PM1.PLid IN (SELECT "
                    + parentFilter + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = " + bmodel.QT(moduleCode) + ")"
                    + " ORDER BY PM" + loopEnd + ".rowid";
        }
        Cursor c = db.selectSQL(query);
        if (c != null) {
            while (c.moveToNext()) {
                batchno = new Vector<LoadManagementBO>();
                bo = new LoadManagementBO();
                bo.setParentid(c.getInt(0));
                bo.setProductid(c.getInt(1));
                bo.setProductname(c.getString(2));
                bo.setSuggestqty(c.getInt(3));
                bo.setProductshortname(c.getString(4));
                bo.setCaseSize(c.getInt(5));
                bo.setSih(c.getInt(6));
                bo.setWsih(c.getInt(7));
                bo.setIsalloc(c.getInt(8));
                bo.setMrp(c.getDouble(9));
                bo.setBarcode(c.getString(10));
                bo.setRField1(c.getString(11));
                bo.setOuterSize(c.getInt(12));
                bo.setIsMust(c.getInt(13));
                bo.setMaxQty(c.getInt(14));
                bo.setStdpcs(c.getInt(15));
                bo.setStdcase(c.getInt(16));
                bo.setStdouter(c.getInt(17));
                bo.setdUomid(c.getInt(18));
                bo.setdOuonid(c.getInt(19));
                bo.setBaseprice(c.getDouble(20));
                bo.setPiece_uomid(c.getInt(21));
                bo.setPLid(c.getInt(22));
                bo.setpCode(c.getString(23));
                bo.setMsqQty(c.getInt(24));
                bo.setIssalable(c.getInt(25));
                if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
                    bo.setStkprototalQty(c.getInt(c.getColumnIndex("qty")));
                    bo.setStkpropcsqty(c.getInt(c.getColumnIndex("pcsQty")));
                    bo.setStkprocaseqty(c.getInt(c.getColumnIndex("caseQty")));
                    bo.setStkproouterqty(c.getInt(c.getColumnIndex("outerQty")));
                }
                if (batchmenucode.equals("MENU_VAN_UNLOAD")
                        || batchmenucode.equals("MENU_CUR_STK_BATCH")
                        || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
                    bo.setBatchNo(c.getString(c.getColumnIndex("batchNum")));
                    bo.setStocksih(c.getInt(c.getColumnIndex("qty")));
                    bo.setOld_diff_sih(c.getInt(c
                            .getColumnIndex("adjusted_qty")));
                    bo.setBatchId(c.getString(c.getColumnIndex("batchid")));
                }
                if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")) {

                    list = new Vector<LoadManagementBO>();
                    LoadManagementBO ret;

                    for (int i = 0; i < 3; ++i) {
                        ret = new LoadManagementBO();
                        ret.setProductid(c.getInt(0));
                        ret.setCaseqty(0);
                        ret.setPieceqty(0);
                        ret.setOuterQty(0);
                        ret.setBatchNo("");
                        list.add(ret);
                    }

                    bo.setBatchlist(list);
                }
                mLoadManagementBOByProductId.put(bo.getProductid(), bo);
                productlist.add(bo);

            }

            c.close();
        }

        if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")) {
            Cursor c1 = db
                    .selectSQL("select batchNum, batchid, Pid from BatchMaster order by Pid");

            if (c1 != null) {
                int temp = -1;
                Vector<LoadManagementBO> batchno1 = null;
                HashMap<Integer, Vector<LoadManagementBO>> prodBatchList = new HashMap<>();
                while (c1.moveToNext()) {

                    if (temp != c1.getInt(2)) {
                        prodBatchList.put(temp, batchno1);
                        batchno1 = new Vector<LoadManagementBO>();
                        temp = c1.getInt(2);
                    }

                    batchnobo = new LoadManagementBO();
                    batchnobo.setBatchNo(c1.getString(0));
                    batchnobo.setBatchId(c1.getString(1));
                    batchno1.add(batchnobo);
                }
                c1.close();

                temp = productlist.size();
                for (int i = 0; i < temp; i++) {
                    LoadManagementBO load = productlist.get(i);
                    if (prodBatchList.get(load.getProductid()) != null) {
                        int s1 = load.getBatchlist().size();
                        for (int j = 0; j < s1; j++) {
                            LoadManagementBO ret = load.getBatchlist().get(j);
                            ret.setBatchnolist(cloneVanloadList(prodBatchList.get(load.getProductid())));
                        }
                    }

                }
            }
        }


        db.closeDB();
        return productlist;
    }

    public static Vector<LoadManagementBO> cloneVanloadList(
            Vector<LoadManagementBO> list) {

        Vector<LoadManagementBO> clone = new Vector<LoadManagementBO>(
                list.size());
        for (LoadManagementBO item : list)
            clone.add(new LoadManagementBO(item));
        return clone;

    }

    /**
     * get total sold Cases/ Bottle of the specific Retailer
     *
     * @param headertable
     * @param detailTable
     * @param columnName
     * @param ID
     * @return
     */
    public String getOrderDetailVolume(String headertable, String detailTable,
                                       String columnName, String ID) {
        String totalVoume = "0";
        DBUtil db = null;
        StringBuffer sBuffer = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            sBuffer = new StringBuffer();
            sBuffer.append("SELECT IFNULL(SUM(" + columnName + "),0) FROM "
                    + headertable + " OH INNER JOIN  " + detailTable
                    + " OD ON " + ID);
            sBuffer.append(" WHERE  OH.RetailerID = ");
            sBuffer.append(bmodel.QT(bmodel.getRetailerMasterBO()
                    .getRetailerID()));

            Cursor cursor = db.selectSQL(sBuffer.toString());
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    totalVoume = cursor.getString(0);
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return totalVoume;
    }

    public Vector<LoadManagementBO> getProducts() {
        return productlist;
    }

    private void downloadDescriptionByTypeId() {
        mDescriptionByTypeId = new HashMap<Integer, String>();
        mTypeIdList = new ArrayList<Integer>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select SM.Listid,SM.Listname from StandardListMaster SM where ListType='DISCOUNT_TYPE' and listcode!='PRICEOFF'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mDescriptionByTypeId.put(c.getInt(0), c.getString(1));
                    mTypeIdList.add(c.getInt(0));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }


    }


    /**
     * Method to use download details of product level discount
     */

    public void downloadProductDiscountDetails() {

        mProductIdListByDiscoutId = new HashMap<Integer, ArrayList<StoreWsieDiscountBO>>();

        mDiscountIdList = new ArrayList<Integer>();
        DBUtil db = null;
        StringBuilder sb = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            sb = new StringBuilder();
            sb.append("select Value,IsPercentage,Typeid,SM.Listname,ApplyLevelid,Moduleid,dm.DiscountId,ProductId,dm.isCompanyGiven from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId ");
            sb.append("Left Join StandardListmaster SM on SM.Listid=dm.Typeid ");
            sb.append("where dm.DiscountId in (select DiscountId from DiscountMapping ");
            sb.append("where (Retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR ");
            sb.append(" Channelid=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR ");
            sb.append(" Channelid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + ") OR ");
            sb.append(" locationid in(" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ") OR ");
            sb.append(" Accountid =" + bmodel.getRetailerMasterBO().getAccountid() + " AND Accountid != 0" + ") OR ");
            sb.append(" (Retailerid=0 AND distributorid=0 AND Channelid=0 AND locationid =0 AND Accountid =0))");
            sb.append(" and dm.moduleid=(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append("and dm.ApplyLevelid=(select ListId from StandardListMaster ");
            sb.append("where ListCode='ITEM' and ListType='DISCOUNT_APPLY_TYPE') ");
            sb.append("and dm.Typeid not in (select ListId from StandardListMaster where ListCode='GLDSTORE')");
            sb.append(" order by dm.DiscountId,dm.isCompanyGiven desc");
            ArrayList<StoreWsieDiscountBO> productdiscountList = new ArrayList<StoreWsieDiscountBO>();
            StoreWsieDiscountBO storeWiseDiscountBO;
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    int discountid = 0;
                    while (c.moveToNext()) {
                        storeWiseDiscountBO = new StoreWsieDiscountBO();
                        storeWiseDiscountBO.setDiscount(c.getDouble(0));
                        storeWiseDiscountBO.setIsPercentage(c.getInt(1));
                        storeWiseDiscountBO.setType(c.getInt(2));
                        storeWiseDiscountBO.setDescription(c.getString(3));
                        storeWiseDiscountBO.setApplyLevel(c.getInt(4));
                        storeWiseDiscountBO.setModule(c.getInt(5));
                        storeWiseDiscountBO.setDiscountId(c.getInt(6));
                        storeWiseDiscountBO.setProductId(c.getInt(7));
                        storeWiseDiscountBO.setIsCompanyGiven(c.getInt(8));

                        if (discountid != storeWiseDiscountBO.getDiscountId()) {
                            if (discountid != 0) {
                                mProductIdListByDiscoutId.put(discountid,
                                        productdiscountList);
                                productdiscountList = new ArrayList<StoreWsieDiscountBO>();
                                productdiscountList.add(storeWiseDiscountBO);
                                discountid = storeWiseDiscountBO
                                        .getDiscountId();
                                mDiscountIdList.add(discountid);

                            } else {
                                productdiscountList.add(storeWiseDiscountBO);
                                discountid = storeWiseDiscountBO
                                        .getDiscountId();
                                mDiscountIdList.add(discountid);
                            }

                        } else {
                            productdiscountList.add(storeWiseDiscountBO);
                        }

                    }
                    if (productdiscountList.size() > 0) {
                        mProductIdListByDiscoutId.put(discountid,
                                productdiscountList);
                    }

                }
                c.close();
            }
            db.closeDB();
            downloadDescriptionByTypeId();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public ArrayList<Integer> getDiscountIdList() {
        if (mDiscountIdList != null) {
            return mDiscountIdList;
        }
        return new ArrayList<Integer>();
    }

    public HashMap<Integer, ArrayList<StoreWsieDiscountBO>> getProductDiscountListByDiscountID() {
        return mProductIdListByDiscoutId;
    }


    /**
     * Method to use apply itemlevel discount
     *
     * @return
     */
    public double updateItemLevelDiscount() {
        double totalDiscountValue = 0.0;
        if (mDiscountIdList != null) {
            for (Integer discountID : mDiscountIdList) {

                double discountValue = updateItemLeveDiscountbyPercentageOrAmt(discountID);

                totalDiscountValue = totalDiscountValue + discountValue;
            }
        }

        return totalDiscountValue;
    }

    private double updateItemLeveDiscountbyPercentageOrAmt(int discouId) {
        double totalDiscountValue = 0.0;

        ArrayList<StoreWsieDiscountBO> discountProductIdList = mProductIdListByDiscoutId
                .get(discouId);
        if (discountProductIdList != null) {
            for (StoreWsieDiscountBO storewiseDiscountBO : discountProductIdList) {
                ProductMasterBO productBo = bmodel.productHelper
                        .getProductMasterBOById(storewiseDiscountBO
                                .getProductId() + "");
                if (productBo != null) {
                    if (productBo.getOrderedPcsQty() > 0
                            || productBo.getOrderedCaseQty() > 0
                            || productBo.getOrderedOuterQty() > 0) {

                        boolean isBatchwise = false;
                        int perorAmtDiscount = storewiseDiscountBO
                                .getIsPercentage() == 1 ? 1 : 0;
                        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && bmodel.configurationMasterHelper.IS_INVOICE
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                            if (productBo.getBatchwiseProductCount() > 0) {
                                isBatchwise = true;

                            } else {
                                isBatchwise = false;
                            }
                        } else {
                            isBatchwise = false;
                        }
                        boolean isCompanyWiseDisc = false;
                        if (storewiseDiscountBO.getIsCompanyGiven() == 1) {
                            isCompanyWiseDisc = true;
                        } else {
                            isCompanyWiseDisc = false;
                        }


                        final double discountValue = getProductsDisCountValue(productBo,
                                storewiseDiscountBO.getDiscount(), perorAmtDiscount,
                                isBatchwise, discouId, isCompanyWiseDisc);

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

    public void saveItemLevelDiscount(String orderID, DBUtil db) {

        String columns = "OrderId,Pid,Typeid,Value,Percentage,ApplyLevelid,RetailerId,discountid,isCompanyGiven";
        if (mDiscountIdList != null) {
            StringBuffer sb = null;
            for (Integer discountid : mDiscountIdList) {
                ArrayList<StoreWsieDiscountBO> storewiseDiscountList = mProductIdListByDiscoutId
                        .get(discountid);
                if (storewiseDiscountList != null) {
                    for (StoreWsieDiscountBO storewiseDiscountBo : storewiseDiscountList) {
                        ProductMasterBO productBo = bmodel.productHelper
                                .getProductMasterBOById(storewiseDiscountBo
                                        .getProductId() + "");
                        if (productBo != null) {
                            if (productBo.getOrderedPcsQty() > 0
                                    || productBo.getOrderedCaseQty() > 0
                                    || productBo.getOrderedOuterQty() > 0) {
                                double value = 0;
                                double percentage = 0;
                                if (storewiseDiscountBo.getIsPercentage() == 1) {
                                    percentage = storewiseDiscountBo.getDiscount();
                                } else {
                                    value = storewiseDiscountBo.getDiscount();
                                }

                                sb = new StringBuffer();
                                sb.append(orderID

                                        + "," + storewiseDiscountBo.getProductId()
                                        + ",");
                                sb.append(storewiseDiscountBo.getType() + ","

                                        + value
                                        + ",");
                                sb.append(percentage
                                        + ","
                                        + storewiseDiscountBo.getApplyLevel());

                                sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                sb.append("," + storewiseDiscountBo.getDiscountId());
                                sb.append("," + storewiseDiscountBo.getIsCompanyGiven());
                                db.insertSQL("InvoiceDiscountDetail", columns,
                                        sb.toString());
                                db.insertSQL("OrderDiscountDetail", columns,
                                        sb.toString());

                            }

                        }

                    }

                }
            }
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
                    sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
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

    /**
     * @author rajesh.k Method to use download product wise tax details
     */

    public void downloadExcludeProductTaxDetails() {

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
            if (!bmodel.configurationMasterHelper.IS_GST && bmodel.configurationMasterHelper.IS_LOCATION_WISE_TAX_APPLIED) {
                c = db.selectSQL("SELECT (SELECT Sequence FROM LocationLevel WHERE id = (SELECT LocLevelId FROM LocationMaster WHERE LocId = (SELECT locationid FROM RetailerMaster WHERE RetailerID = '" + bmodel.getRetailerMasterBO().getRetailerID() + "'))) - (SELECT Sequence FROM LocationLevel WHERE id = (SELECT Id FROM LocationLevel WHERE Code = '" + bmodel.configurationMasterHelper.STRING_LOCATION_WISE_TAX_APPLIED + "'))");
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
                query += " WHERE LOC_MAS1.LocId  = '" + bmodel.getRetailerMasterBO().getLocationId() + "'";
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
                    + bmodel.getRetailerMasterBO().getTaxTypeId());
            if (bmodel.configurationMasterHelper.IS_GST) {
                sb.append(" AND PTM.isSameZone = " + bmodel.getRetailerMasterBO().isSameZone());
            } else if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_TAX_APPLIED) {
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

    /**
     * Method to use load tax information for print zebra 3inch in titan project
     *
     * @param invoiceid
     */
    public void loadTaxDetailsForPrint(String invoiceid) {

        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct IT.taxType,IT.taxRate,slm.flex1 from invoicetaxdetails IT");
            sb.append(" inner join taxmaster TM on IT.groupid=TM.Groupid and IT.TaxType=TM.taxtype ");
            sb.append(" left join standardlistmaster slm on TM.taxtype=slm.listid ");
            sb.append(" where invoiceid=" + bmodel.QT(invoiceid) + " order by IT.taxType,IT.taxRate,slm.flex1 desc");
            Cursor c = db.selectSQL(sb.toString());
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
            sb.append("select distinct IT.taxType,pid,IT.taxRate,IT.isFreeProduct from invoicetaxdetails IT");
            sb.append(" left join taxmaster TM on IT.groupid=TM.Groupid");
            sb.append(" left join standardlistmaster slm on TM.taxtype=slm.listid ");
            sb.append(" where invoiceid=" + bmodel.QT(invoiceid) + " and IT.isFreeProduct=0 order by IT.taxType,IT.taxRate");
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
            sb.append(" where invoiceid=" + bmodel.QT(invoiceid) + "and IT.isFreeProduct=1 order by IT.taxType,IT.taxRate");
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

    public SparseArray<LinkedHashSet<TaxBO>> getGroupDesc2ByGroupId() {
        return mTaxBOByGroupId;
    }


    /**
     * @author rajesh.k Method to use update productwise tax value only object
     * level not DB level
     */
    public void updateProductWiseTax() {
        if (mProductTaxList != null) {
            for (String productId : mProductTaxList) {
                ProductMasterBO productBo = getProductMasterBOById(productId);

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
                                    .getDiscount_order_value();
                            double remainingValue = totalValue / totalQty;
                            double taxRate = 0;
                            for (TaxBO taxBO : taxList) {
                                if (bmodel.configurationMasterHelper.SHOW_MRP_LEVEL_TAX) {
                                    if (taxBO.getApplyRange() == 1) {

                                        if (taxBO.getMinValue() <= remainingValue
                                                && taxBO.getMaxValue() >= remainingValue) {
                                            excludeProductTax(productBo, taxBO, false);
                                        }

                                    } else {
                                        excludeProductTax(productBo, taxBO, false);
                                    }
                                } else {
                                    excludeProductTax(productBo, taxBO, false);
                                }
                            }
                            calculateTotalTaxForProduct(productBo);
                        }
                    }
                }
            }
        }

    }


    private HashMap<String, TaxBO> mTaxBoByBatchProduct = null;

    // Excluding tax value from product total value and setting it in taxlist(mTaxListByProductId) against to product id
    public void excludeProductTax(ProductMasterBO productMasterBO, TaxBO taxBO, boolean isFreeProduct) {

        double productPriceWithoutTax = 0.0;
        double taxAmount = 0.0;

        if (taxBO.getParentType() == null || taxBO.getParentType().equals("0")) {
            //Allowing only parent tax type

            if (productMasterBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                // If batch available

                ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
                        .getBatchlistByProductID().get(productMasterBO.getProductID());
                if (batchList != null) {
                    mTaxBoByBatchProduct = new HashMap<>();
                    for (ProductMasterBO batchProductBO : batchList) {
                        if (batchProductBO.getOrderedPcsQty() > 0
                                || batchProductBO.getOrderedCaseQty() > 0
                                || batchProductBO.getOrderedOuterQty() > 0) {
                            // calculating tax value batchwise

                            TaxBO batchTaxBO = cloneTaxBo(taxBO);

                            productPriceWithoutTax = batchProductBO.getDiscount_order_value() / (1 + (batchTaxBO.getTaxRate() / 100));
                            taxAmount = productPriceWithoutTax * batchTaxBO.getTaxRate() / 100;

                            batchTaxBO.setTotalTaxAmount(taxAmount);
                            batchTaxBO.setTaxableAmount(productPriceWithoutTax);
                            mTaxBoByBatchProduct.put(batchProductBO.getProductID() + batchProductBO.getBatchid(), batchTaxBO);


                        }
                    }

                    HashMap<String, TaxBO> tempList = new HashMap<>();
                    tempList.put(taxBO.getTaxType(), taxBO);
                    excludeChildTaxIfAvailable(productMasterBO, tempList, isFreeProduct);

                }
            } else {
                // calculating tax value

                productPriceWithoutTax = productMasterBO.getDiscount_order_value() / (1 + (taxBO.getTaxRate() / 100));
                taxAmount = productPriceWithoutTax * taxBO.getTaxRate() / 100;

                //setting tax and taxable amount against to each tax object
                taxBO.setTotalTaxAmount(taxAmount);
                taxBO.setTaxableAmount(productPriceWithoutTax);

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

            double completeParentTaxAmount = 0.0;
            double childTaxAmount = 0.0;
            HashMap<String, TaxBO> mTempParentTaxBoByTaxType = new HashMap<>();

            for (String parentTaxType : mParentTaxBoByTaxType.keySet()) {

                for (TaxBO childTaxBO : (!isFreeProduct ? mTaxListByProductId.get(productMasterBO.getProductID()) : bmodel.getmFreeProductTaxListByProductId().get(productMasterBO.getProductID()))) {
                    if (childTaxBO.getParentType().equals(mParentTaxBoByTaxType.get(parentTaxType).getTaxType())) {
                        // child tax available

                        if (productMasterBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
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

                                        batchChildTaxBO.setTotalTaxAmount(childTaxAmount);
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

                            childTaxBO.setTotalTaxAmount(childTaxAmount);
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

    private void calculateTotalTaxForProduct(ProductMasterBO productMasterBO) {
        // calculating total tax applied for a product

        double totalTaxAmount = 0;

        for (TaxBO taxBO : mTaxListByProductId
                .get(productMasterBO.getProductID())) {
            if (taxBO.getParentType().equals("0")) {// child tax amount no need to add

                totalTaxAmount += taxBO.getTotalTaxAmount();
            }
        }

        productMasterBO.setTaxApplyvalue(totalTaxAmount);
        productMasterBO.setTaxValue(productMasterBO.getDiscount_order_value() - totalTaxAmount);
    }


    public TaxBO cloneTaxBo(TaxBO taxBO) {

        return new TaxBO(taxBO.getTaxType(), taxBO.getTaxRate(), taxBO.getSequence(), taxBO.getTaxDesc(), taxBO.getParentType(), taxBO.getTotalTaxAmount()
                , taxBO.getPid(), taxBO.getTaxTypeId(), taxBO.getMinValue(), taxBO.getMaxValue(), taxBO.getApplyRange(), taxBO.getGroupId(), taxBO.getTaxDesc2());
    }

    private void calculateProductExcludeTax(ProductMasterBO productBO,
                                            double taxRate) {
        double taxValue = 0.0;
        double totalAppliedTaxValue = 0.0;
        //batch wise tax update
        if (productBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
                    .getBatchlistByProductID().get(productBO.getProductID());
            if (batchList != null) {
                for (ProductMasterBO batchProductBO : batchList) {
                    if (batchProductBO.getOrderedPcsQty() > 0
                            || batchProductBO.getOrderedCaseQty() > 0
                            || batchProductBO.getOrderedOuterQty() > 0) {
                        double batchTaxValue = batchProductBO.getDiscount_order_value() / (1 + (taxRate / 100));
                        double appliedTaxValue = batchTaxValue * taxRate / 100;
                        taxValue = taxValue + batchTaxValue;
                        totalAppliedTaxValue = totalAppliedTaxValue + appliedTaxValue;
                        batchProductBO.setTaxValue(batchTaxValue);
                        batchProductBO.setTaxApplyvalue(appliedTaxValue);
                    }
                }
                productBO.setTaxValue(taxValue);
                productBO.setTaxApplyvalue(totalAppliedTaxValue);
            }

        } else {
            taxValue = productBO.getDiscount_order_value() / (1 + (taxRate / 100));
            totalAppliedTaxValue = taxValue * taxRate / 100;
            productBO.setTaxApplyvalue(totalAppliedTaxValue);
            productBO.setTaxValue(taxValue);
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
                ProductMasterBO productBo = getProductMasterBOById(productId);
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
                                    .getDiscount_order_value();
                            double remainingValue = totalValue / totalQty;
                            for (TaxBO taxBO : taxList) {
                                if (bmodel.configurationMasterHelper.SHOW_MRP_LEVEL_TAX) {
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

    public void updateInvoiceIdInItemLevelDiscount(DBUtil db, String invid,
                                                   String orderId) {

        String query = "update InvoiceDiscountDetail set InvoiceId=" + bmodel.QT(invid)
                + " where OrderId=" + orderId;
        db.updateSQL(query);

    }

    public void updateInvoiceIdInProductLevelTax(DBUtil db, String invid,
                                                 String orderId) {
        String query = "update InvoiceTaxDetails set InvoiceId=" + bmodel.QT(invid)
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

        double taxvalue = productBO.getTaxValue() * taxBO.getTaxRate() / 100;
        values.append(orderId + "," + productBO.getProductID() + ","
                + taxBO.getTaxRate() + ",");
        values.append(taxBO.getTaxType() + "," + taxvalue
                + "," + bmodel.getRetailerMasterBO().getRetailerID());
        values.append("," + taxBO.getGroupId() + ",0");
        db.insertSQL("InvoiceTaxDetails", columns, values.toString());
        db.insertSQL("OrderTaxDetails", columns, values.toString());
        values = null;


    }

    public void insertProductLevelTaxForFreeProduct(String orderId, DBUtil db,
                                                    String productId, TaxBO taxBO) {
        String columns = "orderId,pid,taxRate,taxType,taxValue,retailerid,groupid,IsFreeProduct";
        StringBuffer values = new StringBuffer();

        values.append(orderId + "," + productId + ","
                + taxBO.getTaxRate() + ",");
        values.append(taxBO.getTaxType() + "," + taxBO.getTotalTaxAmount()
                + "," + bmodel.getRetailerMasterBO().getRetailerID());
        values.append("," + taxBO.getGroupId() + ",1");
        db.insertSQL("InvoiceTaxDetails", columns, values.toString());
        db.insertSQL("OrderTaxDetails", columns, values.toString());
        values = null;


    }

    /**
     * @param orderedList
     * @return
     * @author rajesh.k Method to use update use entry level product discount
     */

    public double updateProductDiscountUsingEntry(
            List<ProductMasterBO> orderedList) {
        mDiscountmapByProductwithBathid = new HashMap<String, HashMap<Integer, Double>>();
        double totalDiscountValue = 0;
        if (orderedList != null) {

            for (ProductMasterBO productBO : orderedList) {
                double discountvalue = 0.0;
                double totalEnteredDiscount = productBO.getD1()
                        + productBO.getD2() + productBO.getD3();
                boolean isBatchwise = false;
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        isBatchwise = true;

                    } else {
                        isBatchwise = false;
                    }
                } else {
                    isBatchwise = false;
                }
                if (totalEnteredDiscount > 0) {
                    discountvalue = getProductsDisCountValue(productBO,
                            totalEnteredDiscount, 1, isBatchwise, 0, true);

                } else if (productBO.getDA() > 0) {
                    discountvalue = getProductsDisCountValue(productBO,
                            productBO.getDA(), 0, isBatchwise, 0, true);

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

    private double getProductsDisCountValue(ProductMasterBO productBO,
                                            double value, int discOrAmt, boolean isBatchwise, int discountId, boolean isCompanyDiscount) {
        double totalDiscOrAmtValue = 0;

        if (isBatchwise) {
            totalDiscOrAmtValue = getProductDiscountValueIsBatchwise(productBO,
                    value, discOrAmt, discountId);

        } else {
            int totalQty = productBO.getOrderedPcsQty()
                    + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                    + productBO.getOrderedOuterQty() * productBO.getOutersize();
            double totalValue = 0.0;
            if (productBO.getSchemeAppliedValue() > 0) {
                totalValue = productBO.getSchemeAppliedValue();
            } else {
                totalValue = bmodel.schemeDetailsMasterHelper
                        .getbatchWiseTotalValue(productBO);
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
                discountValueByDiscountId = new HashMap<Integer, Double>();

            discountValueByDiscountId.put(discountId, totalDiscOrAmtValue);

            mDiscountmapByProductwithBathid.put(productBO.getProductID(), discountValueByDiscountId);

        }

        return totalDiscOrAmtValue;
    }

    /**
     * Method to use apply product levele discount
     *
     * @param productBO - apply for this proudct object
     * @param value     - discount value
     * @param discOrAmt - if 1 - percentage,0 - amount based discount
     * @return
     */
    private double getProductDiscountValueIsBatchwise(
            ProductMasterBO productBO, double value, int discOrAmt, int discountid) {


        double totalProductDisOrAmtValue = 0.0;
        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
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

    public int getMappingLocationId(int loclevelid, int Retlocid) {
        int locid = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select Sequence as childlevel,(select Sequence from LocationLevel LL inner join LocationMaster LM on LM.LocLevelId=LL.Id where  LocId=" + Retlocid + ") as contentlevel  from LocationLevel   where id=" + loclevelid);
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;
            String sql;
            sql = "select LM" + loopEnd + ".LocId from LocationMaster LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN LocationMaster LM" + i + " ON LM" + (i - 1)
                        + ".LocParentId = LM" + i + ".LocId";
            sql = sql + " where LM1.LocId=" + Retlocid;
            Cursor c1 = db.selectSQL(sql);
            if (c1 != null) {
                while (c1.moveToNext()) {
                    locid = c1.getInt(0);
                }
                c1.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return locid;
    }

    public int getMappingChannelId(int chid, int RetSubCHid) {
        int channelid = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select Sequence as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + RetSubCHid + ") as contentlevel  from ChannelLevel   where LevelId=" + chid);
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;
            String sql;
            sql = "select LM" + loopEnd + ".ChId from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + RetSubCHid;
            Cursor c1 = db.selectSQL(sql);
            if (c1 != null) {
                while (c1.moveToNext()) {
                    channelid = c1.getInt(0);
                }
                c1.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return channelid;
    }


    public int getRetailerlevel(String menucode) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select IsAccount,IsRetailer,IsClass,LocLevelId,ChLevelId from ConfigActivityFilter where ActivityCode=" + bmodel.QT(menucode));
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 1 && c.getInt(4) > 0) {
                        chid = c.getInt(4);
                        return 8;
                    } else if (c.getInt(0) == 1)
                        return 1;
                    else if (c.getInt(1) == 1)
                        return 2;
                    else if (c.getInt(2) == 1) {
                        if (menucode.equalsIgnoreCase("MENU_PROMO") && c.getInt(4) > 0) {
                            chid = c.getInt(4);
                            return 7;
                        }
                        return 3;
                    } else {
                        if (c.getInt(3) > 0 && c.getInt(4) > 0) {
                            locid = c.getInt(3);
                            chid = c.getInt(4);
                            return 6;
                        } else if (c.getInt(3) > 0) {
                            locid = c.getInt(3);
                            return 4;
                        } else if (c.getInt(4) > 0) {
                            chid = c.getInt(4);
                            return 5;
                        }
                    }

                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return -1;
    }


    public void loadSODAssetData(String menuname) {
        try {

            int level;
            level = getRetailerlevel(menuname);
            Commons.print("level=" + level + " menuname=" + menuname);
            if (menuname.equals("MENU_SOD_ASSET")) {
                switch (level) {
                    case 1:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, true, false, false, 0, 0);
                        break;
                    case 2:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, false, true, false, 0, 0);
                        break;
                    case 3:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, false, false, true, 0, 0);
                        break;
                    case 4:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, false, false, false, locid, 0);
                        break;
                    case 5:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, false, false, false, 0, chid);
                        break;
                    case 6:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, false, false, false, locid, chid);
                        break;
                    case 8:
                        bmodel.sodAssetHelper.downloadSalesFundamental(menuname, true, false, false, 0, chid);
                        break;
                    case -1:
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadData(String menuname) {
        try {

            int level;
            level = getRetailerlevel(menuname);
            Commons.print("level=" + level + " menuname=" + menuname);
            if (menuname.equals("MENU_SOS") || menuname.equals("MENU_SOSKU") || menuname.equals("MENU_SOD")) {
                switch (level) {
                    case 1:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, true, false, false, 0, 0);
                        break;
                    case 2:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, false, true, false, 0, 0);
                        break;
                    case 3:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, false, false, true, 0, 0);
                        break;
                    case 4:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, false, false, false, locid, 0);
                        break;
                    case 5:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, false, false, false, 0, chid);
                        break;
                    case 6:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, false, false, false, locid, chid);
                        break;
                    case 8:
                        bmodel.salesFundamentalHelper.downloadSalesFundamental(menuname, true, false, false, 0, chid);
                        break;
                    case -1:
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                        break;

                }
            } else if (menuname.equals("MENU_PLANOGRAM")) {
                switch (level) {
                    case 1:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", true, false, false, 0, 0);
                        break;
                    case 2:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", false, true, false, 0, 0);
                        break;
                    case 3:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", false, false, true, 0, 0);
                        break;
                    case 4:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", false, false, false, locid, 0);
                        break;
                    case 5:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", false, false, false, 0, chid);
                        break;
                    case 6:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM", false, false, false, locid, chid);
                        break;
                    case -1:
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (menuname.equals("MENU_PLANOGRAM_CS")) {
                switch (level) {
                    case 1:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", true, false, false, 0, 0);
                        break;
                    case 2:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, true, false, 0, 0);
                        break;
                    case 3:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, false, true, 0, 0);
                        break;
                    case 4:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, locid, 0);
                        break;
                    case 5:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, 0, chid);
                        break;
                    case 6:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, locid, chid);
                        break;
//                    case -1:
//                        Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
//                        break;

                    default:
                        bmodel.planogramMasterHelper.downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, 0, 0);
                        break;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadDistributorProductsWithFiveLevelFilter(String moduleCode) {
        productMasterById = new HashMap<String, ProductMasterBO>();
        getLocations();
        try {


            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mChildLevel = 0;

            int mLastFilterLevel = 0;
            int mContentLevel = 0;


            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);
            String sql = "";

            if (sequencevalues != null) {
                if (sequencevalues.size() > 0) {
                    mChildLevel = sequencevalues.size();
                }
            }

            if (mChildLevel == 0) {
                sql = "select A.pid, A.pcode,A.pname,A.parentid,A.sih, "
                        + "A.psname,A.barcode,srp1,Csrp1,Osrp1,A.msqqty, "
                        + "A.dUomQty,A.duomid,u.ListCode, "
                        + " dOuomQty,dOuomid,caseBarcode,outerBarcode,count(A.pid),piece_uomid,A.mrp, "
                        + " A.isSalable,A.isReturnable,A.isBom,A.TypeID,A.baseprice, '' as brandname,0"
                        + " from ProductMaster A left join "
                        + "PriceMaster F on A.Pid = F.pid and F.scid = "
                        + bmodel.QT(bmodel.distributorMasterHelper.getDistributor().getGroupId())
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                        + " WHERE A.isSalable = 1 AND A.PLid IN"
                        + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        + " group by A.pid ORDER BY A.rowid";
            } else {

                Cursor filterCur = db
                        .selectSQL("SELECT IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter"
                                + mChildLevel
                                + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT(moduleCode));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mLastFilterLevel = filterCur.getInt(0);
                        mContentLevel = filterCur.getInt(1);
                    }
                    filterCur.close();
                }

                int loopEnd = mContentLevel - mLastFilterLevel + 1;
                sql = "select A"
                        + loopEnd
                        + ".pid, A"
                        + loopEnd
                        + ".pcode,A"
                        + loopEnd
                        + ".pname,A1.pid,A"
                        + loopEnd
                        + ".sih,A"
                        + loopEnd
                        + ".psname,A"
                        + loopEnd
                        + ".barcode,"
                        + "srp1,Csrp1,Osrp1"
                        + ",A"
                        + loopEnd
                        + ".msqqty,A"
                        + loopEnd
                        + ".dUomQty,A"
                        + loopEnd
                        + ".duomid, u.ListCode,"
                        + "A1.dOuomQty,A" + loopEnd + ".dOuomid,A" + loopEnd
                        + ".caseBarcode,A" + loopEnd
                        + ".outerBarcode,count(A1.pid),A" + loopEnd
                        + ".piece_uomid,A" + loopEnd + ".mrp, A" + loopEnd
                        + ".isSalable,A" + loopEnd
                        + ".isReturnable,A" + loopEnd + ".isBom,A" + loopEnd
                        + ".TypeID,A" + loopEnd
                        + ".baseprice,A1.pname as brandname,A1.parentid"
                        + " from ProductMaster A1 ";

                for (int i = 2; i <= loopEnd; i++)
                    sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                            + ".ParentId = A" + (i - 1) + ".PID";

                sql = sql + " left join " + "PriceMaster F on A" + loopEnd
                        + ".Pid = F.pid and F.scid = "
                        + bmodel.QT(bmodel.distributorMasterHelper.getDistributor().getGroupId())
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A" + loopEnd + ".dUOMId = U.ListId"
                        + " WHERE A1.PLid IN (SELECT ProductFilter"
                        + mChildLevel + " FROM ConfigActivityFilter"
                        + " WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        // + " AND A" + loopEnd
                        // + ".isSalable = 1 "
                        + " group by A" + loopEnd + ".pid ORDER BY " + filter
                        + " A" + loopEnd + ".rowid";
            }
            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<ProductMasterBO>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductCode(c.getString(1));
                    product.setProductName(c.getString(2));
                    product.setParentid(c.getInt(3));
                    product.setSIH(c.getInt(4));
                    product.setProductShortName(c.getString(5));
//                    product.setBarCode(c.getString(6));
                    product.setSrp(c.getFloat(7));
                    product.setCsrp(c.getFloat(8));
                    product.setOsrp(c.getFloat(9));
                    product.setMSQty(c.getInt(10));
                    product.setCaseSize(c.getInt(11));
                    product.setCaseUomId(c.getInt(12)); // caseuomid
                    product.setOU(c.getString(13));
                    product.setMRP(c.getDouble(20));
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                        product.setBatchwiseProductCount(1);

                    product.setOutersize(c.getInt(14));
                    product.setOuUomid(c.getInt(15)); // outerid
//                    product.setCasebarcode(c.getString(16));
//                    product.setOuterbarcode(c.getString(17));
                    product.setPcUomid(c.getInt(19));// Pc Uomid
                    product.setMinprice(c.getInt(20));
                    product.setMaxPrice(c.getInt(20));
                    product.setBatchwiseProductCount(1);
                    product.setIsSaleable(c.getInt(21));
//                    product.setIsReturnable(c.getInt(22));
//                    product.setIsBom(c.getInt(23));
//                    product.setTypeID(c.getInt(24));
                    product.setBaseprice(c.getFloat(25));
//                    product.setBrandname(c.getString(26));
                    product.setcParentid(c.getInt(27));
                    product.setLocations(cloneLocationList(locations));

                    productMaster.add(product);
                    productMasterById.put(product.getProductID(), product);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void downloadDiscountIdListByTypeId() {
        DBUtil db = null;


        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select distinct typeid,discountid from discountmaster order by typeid");

            if (c.getCount() > 0) {

                mDiscountIdListByTypeid = new SparseArray<ArrayList<Integer>>();
                ArrayList<Integer> discountIdList = new ArrayList<Integer>();

                int typeId = 0;
                while (c.moveToNext()) {

                    if (typeId != c.getInt(0)) {
                        if (typeId != 0) {
                            mDiscountIdListByTypeid.put(typeId, discountIdList);
                            discountIdList = new ArrayList<Integer>();
                            discountIdList.add(c.getInt(1));
                            typeId = c.getInt(0);

                        } else {
                            discountIdList = new ArrayList<Integer>();
                            discountIdList.add(c.getInt(1));
                            typeId = c.getInt(0);

                        }
                    } else {
                        discountIdList.add(c.getInt(1));
                    }


                }

                if (discountIdList.size() > 0) {
                    mDiscountIdListByTypeid.put(typeId, discountIdList);
                }
            }
            c.close();
            db.closeDB();


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

    public boolean isAllScanned() {

        for (ProductMasterBO productBO : productMaster) {
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

    public ArrayList<Integer> getTypeIdList() {
        if (mTypeIdList != null) {
            return mTypeIdList;
        }
        return new ArrayList<Integer>();
    }

    public SparseArray<ArrayList<Integer>> getDiscountIdListByTypeId() {
        return mDiscountIdListByTypeid;
    }

    public HashMap<Integer, String> getDescriptionByTypeId() {
        return mDescriptionByTypeId;
    }

    public String getPriceOffTextname() {
        String priceOffText = "";
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            String query = "select SM.Listname from StandardListMaster SM where ListType='DISCOUNT_TYPE' and SM.LISTCODE='PRICEOFF'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    priceOffText = c.getString(0);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return priceOffText;

    }

    public float getTotalWeight(String retailerid) {
        DBUtil db = null;
        float totalWeight = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select sum(totalweight) from orderheader where OrderDate=");
            sb.append(bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            if (!retailerid.equals("")) {
                sb.append("and retailerid=" + bmodel.QT(retailerid));
            }
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    totalWeight = c.getFloat(0);

                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return totalWeight;
    }

    public int getTotalOrderQty() {
        DBUtil db = null;
        int totQty = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select sum(qty) from invoicedetails ");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    totQty = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

        return totQty;
    }


    public ArrayList<StandardListBO> getTypeList(String type) {
        DBUtil db = null;
        ArrayList<StandardListBO> orderTypeList = null;

        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select listid,listname,listcode from standardlistmaster ");
            sb.append("where listtype=" + bmodel.QT(type));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                StandardListBO standardListBO;
                orderTypeList = new ArrayList<StandardListBO>();
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    standardListBO.setListCode(c.getString(2));
                    orderTypeList.add(standardListBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return orderTypeList;
    }

    public void downloadCompetitorProducts(String moduleCode) {

        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            Cursor cur = db
                    .selectSQL("SELECT CP.CPID, CP.CPName, PM.parentId,PM.duomid,PM.dOuomid,PM.piece_uomid,CPCode,PM.pid FROM CompetitorProductMaster CP"
                            + " INNER JOIN CompetitorMappingMaster CPM ON CPM.CPId = CP.CPID"
                            + " INNER JOIN ProductMaster PM ON PM.PID = CPM.PID AND PM.isSalable=1"
                            + " WHERE PM.PLid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode =" + QT(moduleCode) + ")" +
                            "group by CP.CPID");

            if (cur != null) {
                while (cur.moveToNext()) {
                    ProductMasterBO product = new ProductMasterBO();
                    product.setProductID(cur.getString(0));
                    product.setProductShortName(cur.getString(1));
                    product.setParentid(cur.getInt(2));
                    product.setIsSaleable(1);
                    product.setBarCode("");
                    product.setCasebarcode("");
                    product.setOuterbarcode("");
                    product.setOwn(0);
                    product.setCaseUomId(cur.getInt(3));
                    product.setOuUomid(cur.getInt(4));
                    product.setPcUomid(cur.getInt(5));
                    product.setProductCode(cur.getString(6));
                    product.setOwnPID(cur.getString(7));

                    // for level skiping
                    ProductMasterBO ownprodbo = productMasterById.get(product.getOwnPID());
                    if (ownprodbo != null)
                        product.setParentid(ownprodbo.getParentid());
                    else
                        product.setParentid(0);

                    product.setLocations(cloneLocationList(locations));
                    for (int i = 0; i < locations.size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(cloneDateList(dateList));
                    }
                    bmodel.productHelper.getTaggedProducts().add(product);
                    mTaggedProductById.put(product.getProductID(), product);
                }
                cur.close();
            }
            db.closeDB();

            Vector<ProductMasterBO> tagItems = bmodel.productHelper.getTaggedProducts();
            if (tagItems != null)
                for (ProductMasterBO tagBo : tagItems) {
                    if (tagBo.getOwn() == 0 && bmodel.productHelper.getFilterColor("Filt23") != 0) {
                        tagBo.setTextColor(bmodel.productHelper.getFilterColor("Filt23"));
                    } else {
                        if (tagBo.getOwn() == 0)
                            tagBo.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
                    }
                }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public SparseArray<ArrayList<SerialNoBO>> getSerialNoListByProductid() {
        return mSerialNoListByProductid;
    }

    public void setmSerialNoListByProductid(SparseArray<ArrayList<SerialNoBO>> serialNoListByProductid) {
        this.mSerialNoListByProductid = serialNoListByProductid;
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


    public ArrayList<AttributeBO> getmAttributesList() {
        return mAttributesList;
    }

    private ArrayList<AttributeBO> mAttributesList;
    private ArrayList<AttributeBO> mAttributeTypes;

    public void downloadAttributes() {
        DBUtil db = null;
        mAttributesList = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        ArrayList<Integer> temp;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            Cursor c = db.selectSQL("select att_id,att_name,att_type_lov_id,stm.listname from AttributeMaster am left join standardlistmaster stm on stm.listid=am.att_type_lov_id");//select att_id,att_name,att_type_lov_id from AttributeMaster");
            if (c.getCount() > 0) {

                temp = new ArrayList<>();
                AttributeBO attBO;
                while (c.moveToNext()) {
                    attBO = new AttributeBO();
                    attBO.setAttributeId(c.getInt(0));
                    attBO.setAttributeName(c.getString(1));
                    attBO.setAttributeLovId(c.getInt(2));
                    mAttributesList.add(attBO);
                    if (!temp.contains(c.getInt(2))) {
                        temp.add(c.getInt(2));
                        AttributeBO attTypeBO = new AttributeBO();
                        attTypeBO.setAttributeTypeId(c.getInt(2));
                        attTypeBO.setAttributeTypename(c.getString(3));
                        mAttributeTypes.add(attTypeBO);
                    }
                }
            }
            temp = null;
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public ArrayList<AttributeBO> getmAttributeTypes() {
        return mAttributeTypes;
    }

    /**
     * Method to apply bill wise  tax either include or exclude
     *
     * @param totalOrderValue
     */
    public double applyBillWiseTax(double totalOrderValue) {
        double totalExclusiveOrderAmount = Double.parseDouble(SDUtil.format(totalOrderValue,
                bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        double totalTaxValue = 0.0;
        if (!bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
            double totalTaxRate = 0;
            for (TaxBO taxBO : mTaxList) {
                totalTaxRate = totalTaxRate + taxBO.getTaxRate();
            }

            totalExclusiveOrderAmount = totalOrderValue / (1 + (totalTaxRate / 100));
        }


        for (TaxBO taxBO : mTaxList) {
            double taxValue = totalExclusiveOrderAmount * (taxBO.getTaxRate() / 100);
            totalTaxValue = totalTaxValue + taxValue;
            taxBO.setTotalTaxAmount(taxValue);
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
                sb.append("where orderid=" + bmodel.getOrderid());
            } else {
                sb.append("select sum(taxValue) from InvoiceTaxDetails ");
                sb.append("where invoiceid=" + bmodel.QT(bmodel.invoiceNumber));
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

    protected double getTotalBillwiseDiscount() {
        double discountValue = 0;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select value from OrderDiscountDetail ");
            sb.append(" where orderid=" + bmodel.getOrderid());
            Cursor c = db.selectSQL(sb.toString());
            if (c.moveToFirst()) {
                discountValue = c.getDouble(0);

            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return discountValue;
    }

    /**
     * Method to update invoiceid in Salesreturn header
     *
     * @param db
     * @param invoiceid
     */
    public void updateInvoiceIdInSalesReturn(DBUtil db, String invoiceid) {
        StringBuffer sb = new StringBuffer();
        sb.append("update SalesReturnHeader set invoiceid=");
        sb.append(QT(invoiceid) + " where retailerid=" + QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" and invoiceid=0");
        db.updateSQL(sb.toString());
    }

    public void updateSalesReturnInfoInProductObj(DBUtil db, String invoiceid, boolean isFromReport) {

        if (db == null) {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
        }
        StringBuffer sb = new StringBuffer();

        sb.append("select pid,uomid,qty from SalesReturnReplacementDetails SR ");
        sb.append(" inner join salesreturnheader SH on SH.uid=SR.uid ");
        sb.append(" where SH.invoiceid=" + QT(invoiceid));
        sb.append(" and SH.retailerid=" + QT(bmodel.getRetailerMasterBO().getRetailerID()));
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                final String productid = c.getString(0);
                final int uomid = c.getInt(1);
                final int repQty = c.getInt(2);
                ProductMasterBO productBO = productMasterById.get(productid);
                if (productBO != null) {

                    if (uomid == productBO.getPcUomid()) {
                        productBO.setRepPieceQty(productBO.getRepPieceQty() + repQty);
                    } else if (uomid == productBO.getCaseUomId()) {
                        productBO.setRepCaseQty(productBO.getRepCaseQty() + repQty);
                    } else if (uomid == productBO.getOuUomid()) {
                        productBO.setRepOuterQty(productBO.getRepOuterQty() + repQty);
                    }

                }
            }
        }
        //  if (isFromReport)
        db.closeDB();


    }

    public void updateDistributorDetails() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select listname,listcode  from standardlistmaster ");
            sb.append(" where listtype='ALT_DIST_INFO'");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String listName = c.getString(0);
                    String listCode = c.getString(1);
                    if (listCode.equalsIgnoreCase("NAME")) {
                        bmodel.userMasterHelper.getUserMasterBO().setDistributorName(listName);
                    } else if (listCode.equalsIgnoreCase("ADDRESS")) {
                        bmodel.userMasterHelper.getUserMasterBO().setDistributorAddress1(listName);
                    } else if (listCode.equalsIgnoreCase("ADDRESS2")) {
                        bmodel.userMasterHelper.getUserMasterBO().setDistributorAddress2(listName);
                    } else if (listCode.equalsIgnoreCase("TINNO")) {
                        bmodel.userMasterHelper.getUserMasterBO().setDistributorTinNumber(listName);
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
    }

    public void downloadBillwiseDiscount() {

        try {

            StoreWsieDiscountBO discountbo;
            mBillWiseDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = null;


            StringBuffer sb = new StringBuffer();
            sb.append("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + bmodel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + ") OR ");
            sb.append(" locationid in(" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ") OR ");
            sb.append(" Accountid =" + bmodel.getRetailerMasterBO().getAccountid() + " and Accountid!=0 ))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid not in (select ListId from StandardListMaster where ListCode='PAYTERM')");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

           /* c = db
                    .selectSQL("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId from DiscountProductMapping dpm inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping where ChannelId="
                            + bmodel.getRetailerMasterBO().getChannelID()
                            + ") and dm.moduleid=(select ListId from StandardListMaster where ListCode='INVOICE') and dm.ApplyLevelid=(select ListId from StandardListMaster where ListCode='BILL') and dm.Typeid not in (select ListId from StandardListMaster where ListCode='GLDSTORE') ");*/
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
                    // adapt.add(discountbo);
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

    public static boolean isApply = true;

    public double updateBillwiseDiscount(double totalOrderValue) {
        double totalValue = totalOrderValue;
        double totalBillwiseDiscountValue = 0;
        double billWiseCompanyDiscount = 0;
        double billWiseDistributorDiscount = 0;
        if (mBillWiseDiscountList != null && mBillWiseDiscountList.size() > 0 && isApply) {
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
        bmodel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        bmodel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);


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

    public void downloadDiscountRange() {
        // HashMap<String,ProductMasterBO> lstRangesByProductId=null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("Select ProductId, MinValue, MaxValue from DiscountProductMapping");
            if (c != null) {
                while (c.moveToNext()) {
                    ProductMasterBO productMasterBO = getProductMasterBOById(c.getString(0));
                    if (productMasterBO != null) {
                        productMasterBO.setFrom_range(c.getInt(1));
                        productMasterBO.setTo_range(c.getInt(2));
                    }

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void calculateProdEntryLevelDisc(List<ProductMasterBO> orderslist) {
        for (ProductMasterBO productMasterBO : orderslist) {
            final double discountPer = productMasterBO.getD1() + productMasterBO.getD2() + productMasterBO.getD3();
            double totalDiscValue = 0;


            if (discountPer > 0 || productMasterBO.getDA() > 0) {
                double totalOrderValue = 0;
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productMasterBO.getBatchwiseProductCount() > 0) {
                    final ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                    if (batchList != null && batchList.size() > 0) {

                        final double batchDiscAmoutValue = productMasterBO.getDA() / productMasterBO.getOrderedBatchCount();

                        for (ProductMasterBO batchProductBo : batchList) {
                            if (batchProductBo.getOrderedPcsQty() > 0 || batchProductBo.getOrderedCaseQty() > 0 || batchProductBo.getOrderedOuterQty() > 0) {
                                double totalBatchOrderValue = (batchProductBo.getOrderedPcsQty() * productMasterBO.getSrp())
                                        + (batchProductBo.getOrderedCaseQty() * productMasterBO.getCsrp())
                                        + (batchProductBo.getOrderedOuterQty() * productMasterBO.getOsrp());
                                if (discountPer > 0) {
                                    final double batchDiscountValue = totalBatchOrderValue * (discountPer / 100);
                                    totalDiscValue = totalDiscValue + batchDiscountValue;
                                    batchProductBo.setProductDiscAmount(batchProductBo.getProductDiscAmount() + batchDiscountValue);
                                } else if (productMasterBO.getDA() > 0) {

                                    batchProductBo.setProductDiscAmount(batchProductBo.getProductDiscAmount() + batchDiscAmoutValue);

                                }

                                totalOrderValue = totalOrderValue + totalBatchOrderValue;

                            }
                        }
                    }
                } else {
                    totalOrderValue = productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp() + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                            + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp();
                    if (discountPer > 0) {
                        totalDiscValue = totalOrderValue * (discountPer / 100);
                    } else if (productMasterBO.getDA() > 0) {
                        totalDiscValue = productMasterBO.getDA();
                    }


                }

                productMasterBO.setProductDiscAmount(productMasterBO.getProductDiscAmount() + totalDiscValue);


            }


        }


    }

    public void insertBillWiseEntryDisc(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        StringBuffer sb = new StringBuffer();
        sb.append(uid + "," + "0,0,");
        if (bmodel.configurationMasterHelper.discountType == 1) {
            sb.append(bmodel.getOrderHeaderBO().getDiscountValue() + "," + bmodel.getOrderHeaderBO().getDiscount());
        } else if (bmodel.configurationMasterHelper.discountType == 2) {
            sb.append(bmodel.getOrderHeaderBO().getDiscountValue() + ",0");
        }

        sb.append(",0," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + "," + bmodel.getOrderHeaderBO().getDiscountId() + "," + bmodel.getOrderHeaderBO().getIsCompanyGiven());
        db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
        db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());

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

            sb.append("," + discountBO.getApplyLevel() + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + "," + discountBO.getDiscountId() + "," + discountBO.getIsCompanyGiven());
            db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
            db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());
        }


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

                sb.append("," + discountBO.getApplyLevel() + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + "," + discountBO.getDiscountId() + "," + discountBO.getIsCompanyGiven());
                db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
                db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());
            }
        }


    }

    /**
     * Method to use clear discount and tax value
     *
     * @param orderList
     */
    public void clearProductDiscAndTaxValue(List<ProductMasterBO> orderList) {
        for (ProductMasterBO productMasterBO : orderList) {
            if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productMasterBO.getBatchwiseProductCount() > 0) {
                ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
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


    public void updateSchemeAndDiscAndTaxValue(DBUtil db, String invoiceid) {
        bmodel.invoiceDisount = 0 + "";

        double totDiscVaue = 0;
        double totSchemeAmountValue = 0;
        double totTaxValue = 0;
        double totPriceOffValue = 0;
        StringBuffer sb = new StringBuffer();
        // sum of product discount , scheme amount and tax amount

        sb.append("select sum(SchemeAmount),sum(DiscountAmount),sum(TaxAmount),sum(priceoffvalue) from invoicedetails ");
        sb.append(" where invoiceid=" + bmodel.QT(invoiceid));
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                totSchemeAmountValue = totSchemeAmountValue + c.getDouble(0);
                totDiscVaue = totDiscVaue + c.getDouble(1);
                totTaxValue = totTaxValue + c.getDouble(2);
                totPriceOffValue = totPriceOffValue + c.getDouble(3);
            }
        }

        sb = new StringBuffer();
        sb.append("select sum(taxValue) from invoicetaxdetails ");
        sb.append("where pid=0 and invoiceid=" + bmodel.QT(invoiceid));
        c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                totTaxValue = totTaxValue + c.getDouble(0);
            }
        }
        sb = new StringBuffer();
        sb.append("select sum(value) from InvoiceDiscountDetail");
        sb.append(" where pid=0  and invoiceid=" + bmodel.QT(invoiceid));
        c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                double billWiseDisc = c.getDouble(0);
                totDiscVaue = totDiscVaue + billWiseDisc;
                bmodel.invoiceDisount = billWiseDisc + "";
            }
        }
        sb = new StringBuffer();
        sb.append("update invoiceMaster set schemeAmount=" + totSchemeAmountValue);
        sb.append(",discount=" + totDiscVaue + ",taxAmount=" + totTaxValue + ",priceoffAmount=" + totPriceOffValue);
        sb.append(" where invoiceno=" + bmodel.QT(invoiceid));
        db.updateSQL(sb.toString());

        c.close();
        bmodel.invoiceDisount = totDiscVaue + "";

    }

    public void updateBillWiseDiscountInObj(String invoiceid) {
        bmodel.invoiceDisount = 0 + "";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        sb.append("select sum(value) from InvoiceDiscountDetail");
        sb.append(" where pid=0  and invoiceid=" + bmodel.QT(invoiceid));
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                double billWiseDisc = c.getDouble(0);

                bmodel.invoiceDisount = billWiseDisc + "";
            }
        }
        c.close();
        db.closeDB();
    }

    public void updateBillEntryDiscInOrderHeader(DBUtil db, String orderId) {

        double totDiscVaue = 0;

        StringBuffer sb = new StringBuffer();
        // sum of product discount , scheme amount and tax amount


        sb.append("select sum(value) from OrderDiscountDetail");
        sb.append(" where pid=0  and OrderId=" + orderId);
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                totDiscVaue = totDiscVaue + c.getDouble(0);
            }
        }
        sb = new StringBuffer();
        sb.append("update orderheader set ");
        sb.append("discount=" + totDiscVaue);
        sb.append(" where orderid=" + orderId);
        db.updateSQL(sb.toString());

        c.close();


    }

    public void downloadDocketPricing() {

        StringBuffer sb = new StringBuffer();
        sb.append("select producid,price,AvailQty from ContractPricing ");
        sb.append("where retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String proudctid = c.getString(0);
                    float price = c.getFloat(1);
                    int availQty = c.getInt(2);
                    ProductMasterBO productBO = getProductMasterBOById(proudctid);
                    if (productBO != null) {
                        productBO.setSrp(price);
                        productBO.setCpsih(availQty);
                        productBO.setCbsihAvailable(true);
                    }
                }
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }


    }

    public void getDistributionLevels() {

        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            int plID = 0;


            String query = "select RField from HhtMenuMaster where HHTCode ='CallA35'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    plID = c.getInt(0);
                }
            }
            int mParentLevel = 0;
            query = "select Sequence from ProductLevel where LevelId =" + plID;
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mParentLevel = c.getInt(0);
                }
            }

            String mOrderId = "";
            query = "select distinct OrderID from OrderDetail where RetailerID =" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and upload='N'";
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                int count = 0;
                while (c.moveToNext()) {
                    count++;
                    mOrderId += bmodel.QT(c.getString(0));
                    if (count != c.getCount())
                        mOrderId += ",";
                }
            }
            //  Cursor filterCur1=db.selectSQL("select PL1.sequence");


            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL3.Sequence,0)"
                            + " FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                            + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                            + " WHERE CF.ActivityCode = 'MENU_STK_ORD'");


            int mContentLevel = 0;

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    //mParentLevel = filterCur.getInt(0);
                    mContentLevel = filterCur.getInt(1);
                }
                filterCur.close();
            }
            int loopEnd = mContentLevel - mParentLevel + 1;


            StringBuilder sb = new StringBuilder();
            sb.append("select distinct PM1.pid from productmaster PM1 ");
            for (int i = 2; i <= loopEnd; i++) {
                sb.append(" INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID");
            }
            sb.append(" Inner Join OrderDetail OD on OD.ProductID = PM" + (loopEnd) + ".PID");
            sb.append(" where PM1.PLID =" + plID + " AND OD.OrderID in (" + mOrderId + ")");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    achLevelID = c.getCount();
                }
            }

            query = "select distinct PM1.pid from productmaster PM1 where PM1.PLID =" + plID;
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    totLevelID = c.getCount();
                }
            }
            c.close();

            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }


    }

    public void updateRangeWiseBillDiscountFromDB() {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        sb.append("select value,Percentage,discountid from invoicediscountdetail id ");
        sb.append(" inner join orderHeader od on id.orderid=od.orderid  ");
        sb.append(" where  id.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
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

                    bmodel.getOrderHeaderBO().setDiscountValue(discountValue);
                    bmodel.getOrderHeaderBO().setDiscount(storeWsieDiscountBO.getDiscount());
                    bmodel.getOrderHeaderBO().setDiscountId(storeWsieDiscountBO.getDiscountId());
                    bmodel.getOrderHeaderBO().setIsCompanyGiven(storeWsieDiscountBO.getIsCompanyGiven());
                    break;
                }
            }

        }
        return discountValue;


    }

    //add loyalty points
    public void downloadLoyaltyDescription(String retailerID) {
        try {

            loyaltyproductList = new Vector<LoyaltyBO>();
            LoyaltyBO loyalties;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct  IFNULL(LM.LoyaltyId,0),IFNULL(LM.Description,'Common'),LP.RetailerId,LP.Points FROM LoyaltyPoints LP LEFT JOIN LoyaltyMaster LM ON LM.LoyaltyId = LP.LoyaltyId"
                    + " WHERE LP.RetailerId =" + retailerID;

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    loyalties = new LoyaltyBO();
                    loyalties.setLoyaltyId(c.getInt(0));
                    loyalties.setLoyaltyDescription(c.getString(1));
                    loyalties.setRetailerId(c.getInt(2));
                    loyalties.setGivenPoints(c.getInt(3));
                    loyaltyproductList.add(loyalties);
                }
                if (loyaltyproductList != null && loyaltyproductList.size() > 0) {
                    for (LoyaltyBO loyaltyBO : bmodel.productHelper.getProductloyalties()) {

                        ArrayList<LoyaltyBenifitsBO> clonedList = new ArrayList<LoyaltyBenifitsBO>(ltyBenifitsList.size());
                        for (LoyaltyBenifitsBO loyaltysBO : ltyBenifitsList) {
                            clonedList.add(new LoyaltyBenifitsBO(loyaltysBO));
                        }

                        loyaltyBO.setLoyaltyTrackingList(clonedList);
                    }

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("Download LoyaltyList", e);
        }

    }


    public ArrayList<LoyaltyBenifitsBO> getLoyaltBenifitsList() {
        return ltyBenifitsList;
    }

    public void setLoyaltyBenifitsList(ArrayList<LoyaltyBenifitsBO> ltyBenifitsList) {
        this.ltyBenifitsList = ltyBenifitsList;
    }

    public int getmSelectedLoyaltyIndex() {
        return mSelectedLoyaltyIndex;
    }

    public void setmSelectedLoyaltyIndex(int mSelectedLoyaltyIndex) {
        this.mSelectedLoyaltyIndex = mSelectedLoyaltyIndex;
    }

    public int mSelectedLoyaltyIndex = 0;

    public Vector<LoyaltyBO> getProductloyalties() {
        return loyaltyproductList;
    }

    public void downloadloyaltyBenifits() {
        try {
            LoyaltyBenifitsBO ltyBenifits;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT * FROM LoyaltyBenefits");

            if (c != null) {
                setLoyaltyBenifitsList(new ArrayList<LoyaltyBenifitsBO>());
                while (c.moveToNext()) {
                    ltyBenifits = new LoyaltyBenifitsBO();
                    ltyBenifits.setBenifitsId(c.getInt(0));
                    ltyBenifits.setBenifitDescription(c.getString(1));
                    ltyBenifits.setImagePath(c.getString(2));
                    ltyBenifits.setBenifitPoints(c.getInt(3));
                    getLoyaltBenifitsList().add(ltyBenifits);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private int categoryLevelId = 0, brandLevelId = 0;
    // private Vector<LevelBO> categoryExpandableList=new Vector<>();
    private Vector<LevelBO> parentChildMap = new Vector<>();

    public void loadCategoryExpandableList() {
        if (getFiveLevelFilters() != null) {
            //if(getFiveLevelFilters().get(categoryLevelId)!=null){
            if (mfilterlevelBo.get(sequencevalues.get(0)) != null) {
                categoryExpandableList.addAll(mfilterlevelBo.get(sequencevalues.get(0)));//getFiveLevelFilters().get(categoryLevelId));
            }
            //if(getFiveLevelFilters().get(brandLevelId)!=null){
            if (mfilterlevelBo.get(sequencevalues.get(1)) != null) {
                parentChildMap.addAll(mfilterlevelBo.get(sequencevalues.get(1)));//getFiveLevelFilters().get(brandLevelId));
            }
        }
        LevelBO levelBO = new LevelBO();
        levelBO.setLevelName("All");
        //levelBO.setProductLevel("0");
        levelBO.setParentID(0);
        categoryExpandableList.add(0, levelBO);
    }

    public Vector<LevelBO> getChildCategory() {
        return parentChildMap;
    }


    public Vector<LevelBO> getPdtids() {
        return pdtids;
    }

    private Vector<LevelBO> pdtids = new Vector<>();

    public void loadBrands(int levelId, String isFrom, int size) {
        String query1 = "";
        if (levelId != 0) {
            if (isFrom.equals("Brand")) {
                pdtids.clear();

            } else {
                query1 = "Select PID from ProductMaster where ParentID=" + levelId;
                if (size == -1) {
                    parentChildMap.clear();
                    parentChildMap = new Vector<>();
                }
            }

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();
            Cursor c1 = null;
            if (!query1.equals("")) {
                c1 = db.selectSQL(query1);
            }

            if (c1 != null) {
                while (c1.moveToNext()) {
                    String query = "SELECT ParentID,PID,PName from ProductMaster where ParentID=" + c1.getInt(0);
                    Cursor c = db.selectSQL(query);

                    if (c != null) {
                        while (c.moveToNext()) {
                            LevelBO mLevelBO = new LevelBO();
                            mLevelBO.setParentID(c.getInt(0));
                            mLevelBO.setProductID(c.getInt(1));
                            mLevelBO.setLevelName(c.getString(2));
                            if (isFrom.equals("Brand")) {
                                pdtids.add(mLevelBO);
                            } else {
                                parentChildMap.add(mLevelBO);
                            }
                        }
                        c.close();

                    }
                }
                c1.close();
                db.close();
            } else {
                String query = "SELECT ParentID,PID,PName from ProductMaster where ParentID=(Select PID from ProductMaster where ParentID=" + levelId + ")";
                Cursor c = db.selectSQL(query);

                if (c != null) {
                    while (c.moveToNext()) {
                        LevelBO mLevelBO = new LevelBO();
                        mLevelBO.setParentID(c.getInt(0));
                        mLevelBO.setProductID(c.getInt(1));
                        mLevelBO.setLevelName(c.getString(2));
                        if (isFrom.equals("Brand")) {
                            pdtids.add(mLevelBO);
                        } else {
                            parentChildMap.add(mLevelBO);
                        }
                    }
                    c.close();
                    db.close();
                }
            }


        } else {
            parentChildMap = new Vector<>();
            parentChildMap.addAll(getFiveLevelFilters().get(brandLevelId));
        }
    }

    private Vector<LevelBO> categoryExpandableList = new Vector<>();

    public void downloadBillwisePaytermDiscount() {

        try {

            StoreWsieDiscountBO discountbo;
            mBillWisePayternDiscountList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = null;


            StringBuffer sb = new StringBuffer();
            sb.append("select distinct Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId,dm.DiscountId,dm.isCompanyGiven,toValue,minValue,maxValue from DiscountProductMapping dpm ");
            sb.append("inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping  ");
            sb.append("where (Retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" Channelid=" + bmodel.getRetailerMasterBO().getSubchannelid() + "  OR ");
            sb.append(" Channelid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + ") OR ");
            sb.append(" locationid in(" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + ") OR ");
            sb.append(" Accountid =" + bmodel.getRetailerMasterBO().getAccountid() + "))");
            sb.append(" and dm.moduleid in(select ListId from StandardListMaster where ListCode='INVOICE') ");
            sb.append(" and dm.ApplyLevelid in(select ListId from StandardListMaster where ListCode='BILL') ");
            sb.append(" and dm.Typeid in(select ListId from StandardListMaster where ListCode='PAYTERM') ");
            // sb.append(" and dm.Typeid not in (select ListId from StandardListMaster where ListCode='GLDSTORE')");
            sb.append(" and " + bmodel.getRetailerMasterBO().getCreditDays() + " between minvalue and maxvalue");
            sb.append(" order by dm.isCompanyGiven asc");
            c = db.selectSQL(sb.toString());

           /* c = db
                    .selectSQL("select Value,IsPercentage,Typeid,Description,ApplyLevelid,Moduleid,ProductId from DiscountProductMapping dpm inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId where dm.DiscountId in (select DiscountId from DiscountMapping where ChannelId="
                            + bmodel.getRetailerMasterBO().getChannelID()
                            + ") and dm.moduleid=(select ListId from StandardListMaster where ListCode='INVOICE') and dm.ApplyLevelid=(select ListId from StandardListMaster where ListCode='BILL') and dm.Typeid not in (select ListId from StandardListMaster where ListCode='GLDSTORE') ");*/
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
                    // adapt.add(discountbo);
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
            Cursor c = null;


            c = db.selectSQL("select ListId from StandardListMaster where ListCode='BILL' and ListType='DISCOUNT_APPLY_TYPE'");
            if (c != null) {
                while (c.moveToNext()) {
                    applyLevelID = c.getInt(0);
                }
                c.close();
            }

            StringBuffer sb = new StringBuffer();
            sb.append("select Percentage,Typeid from PayTermDiscount ");
            sb.append("where Retailerid=" + bmodel.getRetailerMasterBO().getRetailerID());
            sb.append(" and " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " between FromDate and ToDate");
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
                    // adapt.add(discountbo);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public double updateBillwisePaytermDiscount(double totalOrderValue) {


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

                bmodel.getOrderHeaderBO().setDiscountValue(discountValue);
                bmodel.getOrderHeaderBO().setDiscount(storeWsieDiscountBO.getDiscount());
                bmodel.getOrderHeaderBO().setDiscountId(storeWsieDiscountBO.getDiscountId());
                bmodel.getOrderHeaderBO().setIsCompanyGiven(storeWsieDiscountBO.getIsCompanyGiven());
                break;

            }

        }
        bmodel.getRetailerMasterBO().setBillWiseCompanyDiscount(billWiseCompanyDiscount);
        bmodel.getRetailerMasterBO().setBillWiseDistributorDiscount(billWiseDistributorDiscount);
        return discountValue;


    }


    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = null;


        String sb = "SELECT ListName FROM StandardListMaster where ListCode='PRODUCT_IMAGE'";

        c = db.selectSQL(sb.toString());


        if (c != null) {
            while (c.moveToNext()) {
                this.productImageUrl = c.getString(c.getColumnIndex("ListName"));
            }
            c.close();
            db.close();
        }

    }

    private String productImageUrl;

    public void loadBillwiseDiscount(String invoiceid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select discountid,value from InvoiceDiscountDetail ");
            sb.append(" where invoiceid=").append(bmodel.QT(invoiceid));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int discountId = c.getInt(0);
                    double value = c.getDouble(1);
                    if (mBillWiseDiscountList != null) {
                        for (StoreWsieDiscountBO storeWsieDiscountBO : mBillWiseDiscountList) {
                            if (discountId == storeWsieDiscountBO.getDiscountId()) {
                                storeWsieDiscountBO.setDiscountValue(value);
                                break;
                            }
                        }
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }

    public boolean isSihAvailableForOrderProducts(List<ProductMasterBO> orderList) {

        for (ProductMasterBO productBO : orderList) {
            int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();
            if (totalQty > productBO.getSIH()) {
                return false;
            }

        }
        return true;

    }

    public Vector<LevelBO> getCategoryExpandableList() {
        return categoryExpandableList;
    }

    public double getSchemeAmount(String invoiceid) {
        double amount = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select SchemeAmount from InvoiceMaster ");
            sb.append(" where InvoiceNo=").append(bmodel.QT(invoiceid));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    amount = c.getDouble(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
            amount = 0;
        }
        return amount;
    }

    public void downloadIndicativeOrderList() {
        mIndicativeList = new ArrayList<Integer>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select uid from indicativeorder where rid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (isAlreadyInvoiced(c.getInt(0)))
                        mIndicativeList.add(c.getInt(0));
                }

                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isAlreadyInvoiced(int indicativeOrderId) {
        DBUtil db = null;
        boolean isAvailable = true;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select orderid from orderheader where IndicativeOrderID ="
                    + indicativeOrderId + " and invoicestatus=1";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                isAvailable = false;
                c.close();
            } else
                isAvailable = true;
        } catch (Exception e) {
            Commons.printException(e);
        }
        db.closeDB();
        return isAvailable;
    }

    public ArrayList<Integer> getIndicativeList() {
        if (mIndicativeList != null) {
            return mIndicativeList;
        }
        return new ArrayList<Integer>();

    }


    /*public SchemeDialog(final Context context, List<SchemeBO> schemes,
                        String pdname, String prodId, ProductMasterBO productObj, int flag, int totalScreenSize)*/

    private String pdname;
    private String prodId;
    private ProductMasterBO productObj;
    private int flag, totalScreenSize;
    private List<SchemeBO> schemes;

    public List<SchemeBO> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<SchemeBO> schemes) {
        this.schemes = schemes;
    }

    public int getTotalScreenSize() {
        return totalScreenSize;
    }

    public void setTotalScreenSize(int totalScreenSize) {
        this.totalScreenSize = totalScreenSize;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public ProductMasterBO getProductObj() {
        return productObj;
    }

    public void setProductObj(ProductMasterBO productObj) {
        this.productObj = productObj;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getPdname() {
        return pdname;
    }

    public void setPdname(String pdname) {
        this.pdname = pdname;
    }

    public void clearDiscountQuantity() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);

            product.setD1(0);
            product.setD2(0);
            product.setD3(0);
            product.setDA(0);
        }
    }

    public LinkedList<String> getmProductidOrderByEntry() {
        return mProductidOrderByEntry;
    }

    public void setmProductidOrderByEntry(LinkedList<String> mProductidOrderByEntry) {
        this.mProductidOrderByEntry = mProductidOrderByEntry;
    }

    public Vector<ProductMasterBO> getShortProductMaster() {
        return shortProductMaster;
    }

    public void setShortProductMaster(Vector<ProductMasterBO> shortProductMaster) {
        this.shortProductMaster = shortProductMaster;
    }

    public Vector<LevelBO> getGlobalCategory() {
        return globalCategory;
    }

    public void setGlobalCategory(Vector<LevelBO> globalCategory) {
        this.globalCategory = globalCategory;
    }

    public boolean isFilterAvaiable(String menuCode) {
        DBUtil db = null;
        boolean isAvailable = false;
        int productFilter1 = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select ProductFilter1 from ConfigActivityFilter where ActivityCode ="
                    + QT(menuCode);
            Cursor c = db.selectSQL(query);

            if (c != null) {
                while (c.moveToNext()) {
                    productFilter1 = c.getInt(0);
                }
                if (productFilter1 != 0)
                    isAvailable = true;

                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        db.closeDB();
        return isAvailable;
    }

    public void downloadProductsNewOutlet(String moduleCode) {

        productMasterById = new HashMap<String, ProductMasterBO>();

        try {

            // load location and date
            getLocations();
            generateDate();

            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mChildLevel = 0;
            int mFiltrtLevel = 0;
            int mContentLevel = 0;

            String str = "F.srp1,G.srp1";
            String csrp = "F.csrp1,G.csrp1";
            String osrp = "F.osrp1,G.osrp1";
            try {
                if (bmodel.getRetailerMasterBO().getRpTypeCode()
                        .equals(CASH_TYPE)) {
                    str = "F.srp1,G.srp1";
                    csrp = "F.csrp1,G.csrp1";
                    osrp = "F.osrp1,G.osrp1";
                } else {
                    str = "F.srp2,G.srp2";
                    csrp = "F.csrp2,G.csrp2";
                    osrp = "F.osrp2,G.osrp2";
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            Vector<ConfigureBO> filterBO = bmodel.configurationMasterHelper.downloadFilterList();
            boolean filter10 = false; //Must Sell
            boolean filter11 = false; // Focus Brand
            boolean filter12 = false; // Focus Brand2
            boolean filter16 = false; // NMust Sell
            boolean filter20 = false; // Focus Brand 3
            boolean filter21 = false; // Focus Brand 4
            boolean filter22 = false; // Small Pack
            boolean filter19 = false; // nearExpiry tagged
            if (filterBO != null && filterBO.size() > 0) {
                for (ConfigureBO bo : filterBO) {
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt10"))
                        filter10 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt11"))
                        filter11 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt12"))
                        filter12 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt16"))
                        filter16 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt20"))
                        filter20 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt21"))
                        filter21 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt22"))
                        filter22 = true;
                    if (bo.getConfigCode() != null && bo.getConfigCode().equalsIgnoreCase("Filt19"))
                        filter19 = true;
                }
            }
            // get tagging details
            String MSLproductIds = "";
            String NMSLproductIds = "";
            String FCBNDproductIds = "";
            String FCBND2productIds = "";
            String FCBND3productIds = "";
            String FCBND4productIds = "";
            String SMPproductIds = "";
            String nearExpiryTaggedProductIds = "";
            String[] detail;

            if (filter10) {
                MSLproductIds = getTaggingDetails("MSL");

            }
            if (filter16) {
                NMSLproductIds = getTaggingDetails("NMSL");

            }
            if (filter11) {
                FCBNDproductIds = getTaggingDetails("FCBND");

            }
            if (filter12) {
                FCBND2productIds = getTaggingDetails("FCBND2");

            }
            if (filter20) {
                FCBND3productIds = getTaggingDetails("FCBND3");

            }
            if (filter21) {
                FCBND4productIds = getTaggingDetails("FCBND4");

            }
            if (filter22) {
                SMPproductIds = getTaggingDetails("SMP");

            }
            if (filter19) {
                nearExpiryTaggedProductIds = getTaggingDetails("MENU_NEAREXPIRY");

            }

            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);
            String sql = "";

            if (sequencevalues != null) {
                if (sequencevalues.size() > 0) {
                    mChildLevel = sequencevalues.size();
                }
            }

            if (mChildLevel == 0) {
                sql = "select A.pid, A.pcode,A.pname,A.parentid,A.sih, "
                        + "A.psname,A.barcode,A.vat,A.isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A.msqqty,"
                        + "A.dUomQty,A.duomid, u.ListCode,A.MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A.RField1,PWHS.qty,A.IsAlloc, "
                        + ((filter10) ? "A.pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A.pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A.pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "dOuomQty,dOuomid,caseBarcode,outerBarcode,count(A.pid),piece_uomid,A.mrp, A.mrp,"
                        + " A.isSalable,A.isReturnable,A.isBom,A.TypeID,A.baseprice, '' as brandname,0"
                        + ((filter16) ? ",A.pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ") + ",A.weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount"
                        + ",A.Hasserial,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                        + ((filter20) ? "A.pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3,")
                        + ((filter21) ? "A.pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4,")
                        + ((filter22) ? "A.pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP,")
                        + "A.tagDescription,"
                        + ((filter19) ? "A.pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid ")
                        + ",(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid"
                        + " from ProductMaster A left join "
                        + "PriceMaster F on A.Pid = F.pid and F.scid = 0"
                        + " left join PriceMaster G on A.Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A.pid=sbd.productid "
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A.pid and PWHS.UomID=A.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A.pid"
                        + " WHERE A.isSalable = 1 AND A.PLid IN"
                        + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        + " group by A.pid ORDER BY " + filter + " A.rowid";

            } else {

                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && mChildLevel == 0)
                    mChildLevel = 1;

                Cursor filterCur = db
                        .selectSQL("SELECT IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter"
                                + mChildLevel
                                + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT(moduleCode));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mFiltrtLevel = filterCur.getInt(0);
                        mContentLevel = filterCur.getInt(1);
                    }
                    filterCur.close();
                }

                int loopEnd;
                int parentLevelID = 1;

                loopEnd = mContentLevel - mFiltrtLevel + 1;


                sql = "select A"
                        + loopEnd
                        + ".pid, A"
                        + loopEnd
                        + ".pcode,A"
                        + loopEnd
                        + ".pname,A" + parentLevelID + ".pid,A"
                        + loopEnd
                        + ".sih,A"
                        + loopEnd
                        + ".psname,A"
                        + loopEnd
                        + ".barcode,A"
                        + loopEnd
                        + ".vat,A"
                        + loopEnd
                        + ".isfocus, max(ifnull("
                        + str
                        + ")) as srp , ifnull("
                        + csrp
                        + ") as csrp ,ifnull("
                        + osrp
                        + ") as osrp ,A"
                        + loopEnd
                        + ".msqqty,A"
                        + loopEnd
                        + ".dUomQty,A"
                        + loopEnd
                        + ".duomid, u.ListCode,A"
                        + loopEnd
                        + ".MRP,ifnull(sbd.DrpQty,0),ifnull(sbd.grpName,''),A"
                        + loopEnd
                        + ".RField1,PWHS.qty,A"
                        + loopEnd
                        + ".IsAlloc, "
                        + ((filter10) ? "A" + loopEnd + ".pid in(" + MSLproductIds + ") as IsMustSell, " : " 0 as IsMustSell, ")
                        + ((filter11) ? "A" + loopEnd + ".pid in(" + FCBNDproductIds + ") as IsFocusBrand," : " 0 as IsFocusBrand, ")
                        + ((filter12) ? "A" + loopEnd + ".pid in(" + FCBND2productIds + ") as IsFocusBrand2, " : " 0 as IsFocusBrand2, ")
                        + "A1.dOuomQty,A" + loopEnd + ".dOuomid,A" + loopEnd
                        + ".caseBarcode,A" + loopEnd
                        + ".outerBarcode,count(A1.pid),A" + loopEnd
                        + ".piece_uomid,A" + loopEnd + ".mrp, A" + loopEnd
                        + ".mrp,A" + loopEnd + ".isSalable,A" + loopEnd
                        + ".isReturnable,A" + loopEnd + ".isBom,A" + loopEnd
                        + ".TypeID,A" + loopEnd
                        + ".baseprice,A1.pname as brandname,A1.parentid"
                        + ((filter16) ? ",A" + loopEnd + ".pid IN(" + NMSLproductIds + ") as IsNMustSell" : ", 0 as IsNMustSell ")
                        + ",A" + loopEnd + ".weight,(CASE WHEN ifnull(DPM.productid,0) >0 THEN 1 ELSE 0 END) as IsDiscount,A" + loopEnd + ".Hasserial ,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                        + ((filter20) ? "A" + loopEnd + ".pid in(" + FCBND3productIds + ") as IsFocusBrand3, " : " 0 as IsFocusBrand3,")
                        + ((filter21) ? "A" + loopEnd + ".pid in(" + FCBND4productIds + ") as IsFocusBrand4, " : " 0 as IsFocusBrand4,")
                        + ((filter22) ? "A" + loopEnd + ".pid in(" + SMPproductIds + ") as IsSMP, " : " 0 as IsSMP,")
                        + "A" + loopEnd + ".tagDescription,"
                        + ((filter19) ? "A" + loopEnd + ".pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry " : " 0 as isNearExpiry")
                        //+ ",(Select imagename from DigitalContentMaster where imageid=(Select imgid from DigitalContentProductMapping where pid=A" + loopEnd + ".pid)) as imagename "
                        + ",(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid"
                        + " from ProductMaster A1 ";

                for (int i = 2; i <= loopEnd; i++)
                    sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                            + ".ParentId = A" + (i - 1) + ".PID";

                sql = sql + " left join " + "PriceMaster F on A" + loopEnd
                        + ".Pid = F.pid and F.scid = 0"
                        + " left join PriceMaster G on A" + loopEnd
                        + ".Pid = G.pid  and G.scid = 0 "
                        + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A" + loopEnd + ".dUOMId = U.ListId"
                        + " left join SbdDistributionMaster sbd on A" + loopEnd
                        + ".pid=sbd.productid "
                        + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A" + loopEnd + ".pid and PWHS.UomID=A" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                        + " LEFT JOIN DiscountProductMapping DPM ON DPM.productid=A" + loopEnd + ".pid";

                sql = sql + " WHERE A1.PLid IN (SELECT ProductFilter"
                        + mChildLevel + " FROM ConfigActivityFilter"
                        + " WHERE ActivityCode = "
                        + bmodel.QT(moduleCode)
                        + ")"
                        // + " AND A" + loopEnd
                        // + ".isSalable = 1 "
                        + " group by A" + loopEnd + ".pid ORDER BY " + filter
                        + " A" + loopEnd + ".rowid";


            }

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<ProductMasterBO>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductCode(c.getString(1));
                    product.setProductName(c.getString(2));
                    product.setParentid(c.getInt(3));
                    product.setSIH(c.getInt(4));
                    product.setProductShortName(c.getString(5));
                    product.setBarCode(c.getString(6));
                    product.setVat(c.getFloat(7));
                    product.setSrp(c.getFloat(9));
                    product.setPrevPrice_pc(c.getFloat(9) + "");
                    product.setCsrp(c.getFloat(10));
                    product.setPrevPrice_ca(c.getFloat(10) + "");
                    product.setOsrp(c.getFloat(11));
                    product.setPrevPrice_oo(c.getFloat(11) + "");
                    product.setMSQty(c.getInt(12));
                    product.setCaseSize(c.getInt(13));
                    product.setCaseUomId(c.getInt(14)); // caseuomid
                    product.setOU(c.getString(15));
                    product.setMRP(c.getDouble(16));
                    product.setDropQty(c.getInt(17));
                    product.setSbdGroupName(c.getString(18));
                    product.setRField1(c.getString(19));
                    product.setWSIH(c.getInt(20));
                    product.setAllocation(c.getInt(21));
                    product.setIsMustSell(c.getInt(22));
                    product.setIsFocusBrand(c.getInt(23));
                    product.setIsFocusBrand2(c.getInt(24));
                    product.setOutersize(c.getInt(25));
                    product.setOuUomid(c.getInt(26)); // outerid
                    product.setCasebarcode(c.getString(27));
                    product.setOuterbarcode(c.getString(28));
                    product.setPcUomid(c.getInt(30));// Pc Uomid
                    product.setMinprice(c.getInt(31));
                    product.setMaxPrice(c.getInt(32));
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                        product.setBatchwiseProductCount(1);
                    product.setBatchwiseProductCount(0);
                    product.setIsSaleable(c.getInt(33));
                    product.setIsReturnable(c.getInt(34));
                    product.setTypeID(c.getInt(36));
                    product.setBaseprice(c.getFloat(37));
                    product.setBrandname(c.getString(38));
                    product.setcParentid(c.getInt(39));

                    product.setGroupid(c.getInt(c.getColumnIndex("groupid")));
                    product.setLocations(cloneLocationList(locations));

                    for (int i = 0; i < locations.size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(cloneDateList(dateList));
                    }
                    /*
                     * product.setSalesReturnReasonList(cloneIsolateList(
					 * bmodel.reasonHelper.getReasonSalesReturnMaster(),
					 * product.getCaseSize(), product.getOutersize()));
					 */
                    product.setIsNMustSell(c.getInt(c.getColumnIndex("IsNMustSell")));
                    product.setWeight(c.getFloat(c.getColumnIndex("weight")));
                    product.setIsDiscountable(c.getInt(c.getColumnIndex("IsDiscount")));
                    product.setScannedProduct(c.getInt(c.getColumnIndex("Hasserial")));

                    product.setIsFocusBrand3(c.getInt(c.getColumnIndex("IsFocusBrand3")));
                    product.setIsFocusBrand4(c.getInt(c.getColumnIndex("IsFocusBrand4")));
                    product.setIsSMP(c.getInt(c.getColumnIndex("IsSMP")));
                    product.setDescription(c.getString(c.getColumnIndex("tagDescription")));

                    product.setIsNearExpiryTaggedProduct(c.getInt(c.getColumnIndex("isNearExpiry")));

                    product.setPriceoffvalue(c.getDouble(c.getColumnIndex("priceoffvalue")));
                    product.setPriceOffId(c.getInt(c.getColumnIndex("priceoffid")));

                    productMaster.add(product);
                    productMasterById.put(product.getProductID(), product);


                }
                c.close();
            }

            db.closeDB();

            if (bmodel.configurationMasterHelper.SHOW_TAX_MASTER) {
                downloadExcludeProductTaxDetails();
            }

            if (mChildLevel > 0)
                downloadLeastBrandProductMapping((mContentLevel - mFiltrtLevel + 1), mChildLevel, filter + "", moduleCode);

            downloadAttributeProductMapping();
            downloadAttributes();

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                updateProductColorAndSequance();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }
}




