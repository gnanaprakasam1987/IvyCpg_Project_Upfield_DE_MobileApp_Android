package com.ivy.sd.png.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.loyality.LoyaltyBO;
import com.ivy.cpg.view.loyality.LoyaltyBenifitsBO;
import com.ivy.cpg.view.nearexpiry.NearExpiryDateBO;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.tax.TaxInterface;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.StoreWiseDiscountBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class ProductHelper {

    /* Variable declarations*/

    public int locid = 0, chid = 0;
    public int achLevelID = 0, totLevelID = 0;//used for call analysis
    private static final String CASH_TYPE = "CASH";
    private static ProductHelper instance = null;
    private float buffer = 0;

    private Context mContext;
    private BusinessModel bmodel;

    private Vector<ProductMasterBO> productMaster = null;
    private Map<String, ProductMasterBO> productMasterById;

    private Map<String, Integer> oldBatchId;
    private Map<String, Double> oldBatchBasePrice;

    public ArrayList<LocationBO> getStoreLocations() {
        return locations;
    }

    public ArrayList<LocationBO> locations;
    private Vector<StandardListBO> inStoreLocation = new Vector<>();

    private ArrayList<LoyaltyBenifitsBO> ltyBenifitsList;
    private Vector<LoyaltyBO> loyaltyproductList = new Vector<LoyaltyBO>();

    ArrayList<NearExpiryDateBO> getNearExpiryDateList() {
        return nearExpiryDateList;
    }

    private ArrayList<NearExpiryDateBO> nearExpiryDateList = new ArrayList<NearExpiryDateBO>();


    private ArrayList<Integer> mIndicativeList;

    private Vector<LevelBO> globalCategory = new Vector<LevelBO>();


    private Vector<LevelBO> filterProductLevels;
    private Vector<LevelBO> filterProductLevelsRex;

    private HashMap<Integer, Vector<LevelBO>> filterProductsByLevelId;
    private HashMap<Integer, Vector<LevelBO>> filterProductsByLevelIdRex;

    private HashMap<Integer, ArrayList<StoreWiseDiscountBO>> mProductIdListByDiscoutId;
    private ArrayList<Integer> mDiscountIdList;
    private SparseArray<ArrayList<Integer>> mDiscountIdListByTypeid;

    private ArrayList<Integer> mTypeIdList;
    private HashMap<Integer, String> mDescriptionByTypeId;

    private SparseArray<LoadManagementBO> mLoadManagementBOByProductId;

    private HashMap<Integer, Vector<CompetitorFilterLevelBO>> mCompetitorFilterlevelBo;
    private Vector<CompetitorFilterLevelBO> mCompetitorSequenceValues;
    private Vector<ProductMasterBO> competitorProductMaster = new Vector<>();


    private ArrayList<ProductMasterBO> mIndicateOrderList = new ArrayList<ProductMasterBO>();

    private LinkedList<String> mProductidOrderByEntry = new LinkedList<>();

    private HashMap<Integer, Integer> mProductidOrderByEntryMap = new HashMap<>();

    public TaxInterface taxHelper;

    public int mSelectedLocationIndex = 0;
    private int mSelectedGLobalLocationIndex = 0;
    private int mSelectedGlobalLevelID = 0;
    private int mSelectedGlobalProductId = 0;
    private int mLoadedGlobalProductId = 0;

    private HashMap<Integer, Vector<Integer>> mAttributeByProductId;
    private HashMap<Integer, Vector<Integer>> mProductIdByBrandId;

    private ArrayList<AttributeBO> lstProductAttributeMapping;

    private ArrayList<BomReturnBO> bomReturnProducts;
    private ArrayList<BomMasterBO> bomMaster;
    private ArrayList<BomReturnBO> bomReturnTypeProducts;

    /* Getters and setters*/

    public HashMap<Integer, Vector<CompetitorFilterLevelBO>> getCompetitorFiveLevelFilters() {
        return mCompetitorFilterlevelBo;
    }

    public Vector<CompetitorFilterLevelBO> getCompetitorSequenceValues() {
        return mCompetitorSequenceValues;
    }

    public ArrayList<AttributeBO> getLstProductAttributeMapping() {
        return lstProductAttributeMapping;
    }

    public int getmSelectedLocationIndex() {
        return mSelectedLocationIndex;
    }

    public void setmSelectedLocationIndex(int mSelectedLocationIndex) {
        this.mSelectedLocationIndex = mSelectedLocationIndex;
    }

    /**
     * @return
     * @See {@link AppDataProvider#getGlobalLocationIndex()}
     * @deprecated
     */
    public int getmSelectedGLobalLocationIndex() {
        return mSelectedGLobalLocationIndex;
    }

    /**
     * @param mSelectedGLobalLocationIndex
     * @See {@link AppDataProvider#setGlobalLocationIndex(int)}
     * @deprecated
     */
    public void setmSelectedGLobalLocationIndex(int mSelectedGLobalLocationIndex) {
        this.mSelectedGLobalLocationIndex = mSelectedGLobalLocationIndex;
        bmodel.codeCleanUpUtil.setGlobalLocationId(mSelectedGLobalLocationIndex);
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





    public HashMap<Integer, Integer> getmProductidOrderByEntryMap() {
        return mProductidOrderByEntryMap;
    }

    public void setmProductidOrderByEntryMap(HashMap<Integer, Integer> mProductidOrderByEntryMap) {
        this.mProductidOrderByEntryMap = mProductidOrderByEntryMap;
    }

    private ProductHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
        productMaster = new Vector<ProductMasterBO>();
    }

    public void clearProductHelper() {
        productMaster = null;
        productMasterById = null;
        mSelectedGlobalProductId = 0;
        System.gc();
    }

    public static ProductHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ProductHelper(context);
        }
        return instance;
    }

    public void setProductMaster(Vector<ProductMasterBO> productMaster) {
        this.productMaster = productMaster;
    }

    public void setProductMasterById(Map<String, ProductMasterBO> productMasterById) {
        this.productMasterById = productMasterById;
    }

    public Vector<ProductMasterBO> getProductMaster() {
        if (productMaster == null)
            productMaster = new Vector<>();
        return productMaster;
    }



    public HashMap<Integer, Vector<LevelBO>> getFilterProductsByLevelId() {
        return filterProductsByLevelId;
    }

    public void setFilterProductsByLevelId(HashMap<Integer, Vector<LevelBO>> filterProductsByLevelId) {
        this.filterProductsByLevelId = filterProductsByLevelId;
    }

    public Vector<LevelBO> getFilterProductLevels() {
        return filterProductLevels;
    }

    public void setFilterProductLevels(Vector<LevelBO> filterProductLevels) {
        this.filterProductLevels = filterProductLevels;
    }

    public void setFilterProductLevelsRex(Vector<LevelBO> filterProductLevelsRex) {
        this.filterProductLevelsRex = filterProductLevelsRex;
    }

    public void setFilterProductsByLevelIdRex(HashMap<Integer, Vector<LevelBO>> filterProductsByLevelIdRex) {
        this.filterProductsByLevelIdRex = filterProductsByLevelIdRex;
    }

    public HashMap<Integer, Vector<LevelBO>> getRetailerModuleFilterProductsByLevelId() {
        return filterProductsByLevelIdRex;
    }


    public Vector<LevelBO> getRetailerModuleSequenceValues() {
        return filterProductLevelsRex;
    }

    public HashMap<Integer, Vector<Integer>> getmAttributeByProductId() {
        return mAttributeByProductId;
    }

    public HashMap<Integer, Vector<Integer>> getmProductIdByBrandId() {
        if (mProductIdByBrandId == null)
            return new HashMap<>();
        return mProductIdByBrandId;
    }

    public float getBuffer() {
        return buffer;
    }

    public void setBuffer(float buffer) {
        this.buffer = buffer;
    }


    /* Start of implementations*/

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    /**
     * Load Past 4 Order and past 4 stock.
     */
    public void loadRetailerWiseProductWiseP4StockAndOrderQty() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            HashMap<String, String> hashMap = new HashMap<>();
            HashMap<String, String> hashMap1 = new HashMap<>();
            HashMap<String, Integer> oosMap = new HashMap<>();
            ArrayList<String> deadProductList = new ArrayList<>();

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

            sql = "select pid from RtrWiseDeadProducts where rid=" + QT(bmodel.retailerMasterBO.getRetailerID());
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    deadProductList.add(c.getString(0));
                }
                c.close();
            }
            db.closeDB();
            if (hashMap.size() > 0 || hashMap1.size() > 0 || oosMap.size() > 0 || !deadProductList.isEmpty()) {
                for (ProductMasterBO p : productMaster) {

                    if (deadProductList.contains(p.getProductID()))
                        p.setmDeadProduct(1);
                    else
                        p.setmDeadProduct(0);

                    String p4Qty = hashMap.get(p.getProductID());
                    p.setRetailerWiseProductWiseP4Qty(p4Qty != null ? p4Qty : "0,0,0,0");

                    String stockQty = hashMap1.get(p.getProductID());
                    p.setRetailerWiseP4StockQty(stockQty != null ? stockQty : "0,0,0,0");

                    if(stockQty != null){
                        String[] splitQty = stockQty.split(",");
                        if (splitQty.length >= 4) {
                            p.setLastVisitColor(splitQty[0].equals("") ? android.R.color.darker_gray :
                                    (Pattern.compile("[1-9]").matcher(splitQty[0]).find() ? android.R.color.holo_green_dark :
                                            android.R.color.holo_red_dark));
                            p.setLastVisit1Color(splitQty[1].equals("") ? android.R.color.darker_gray :
                                    (Pattern.compile("[1-9]").matcher(splitQty[1]).find() ? android.R.color.holo_green_dark :
                                    android.R.color.holo_red_dark));
                            p.setLastVisit2Color(splitQty[2].equals("") ? android.R.color.darker_gray :
                                    (Pattern.compile("[1-9]").matcher(splitQty[2]).find() ? android.R.color.holo_green_dark :
                                    android.R.color.holo_red_dark));
                            p.setLastVisit3Color(splitQty[3].equals("") ? android.R.color.darker_gray :
                                    (Pattern.compile("[1-9]").matcher(splitQty[3]).find() ? android.R.color.holo_green_dark :
                                    android.R.color.holo_red_dark));

                        }
                    }

                    p.setOos(oosMap.get(p.getProductID()) == null || oosMap.get(p.getProductID()) == 0 ? -2 : oosMap.get(p.getProductID()));
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            if (hashMap.size() > 0) {
                for (ProductMasterBO p : productMaster) {
                    Integer value = hashMap.get(p.getProductID());
                    if (value != null) {
                        p.setIsPurchased(value);
                    }
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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

        product = ProductTaggingHelper.getInstance(mContext).getTaggedProductBOById(productid);

        if (product != null) {
            if (product.getProductID().equals(productid)) {
                product.setQty_klgs(qty);
                product.setRfield1_klgs(rField1);
                product.setRfield2_klgs(rField2);
                product.setCalc_klgs(calulatedValue);

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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
                    int ico = c.getInt(1);
                    ProductMasterBO pbo = productMasterById.get(c.getString(0));
                    pbo.setICO(ico);
                    pbo.setSoInventory(ico);
                }
                c.close();
            }
            db.closeDB();
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
    static ArrayList<NearExpiryDateBO> cloneDateList(
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
    static ArrayList<LocationBO> cloneInStoreLocationList(
            ArrayList<LocationBO> list) {
        ArrayList<LocationBO> clone = new ArrayList<LocationBO>(list.size());
        for (LocationBO item : list)
            clone.add(new LocationBO(item));
        return clone;
    }

    /**
     * Download Instore location and set in location BO.
     */
    public void downloadInStoreLocationsForStockCheck() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            locations = new ArrayList<LocationBO>();
            LocationBO locBO;
            Cursor locCur = null;


            locCur = db
                    .selectSQL("SELECT ListId FROM StandardListMaster"
                            + " WHERE listtype ='PL' ORDER BY ListId");


            if (locCur != null) {
                while (locCur.moveToNext()) {
                    locBO = new LocationBO();
                    locBO.setLocationId(locCur.getInt(0));
                    locations.add(locBO);
                }
                locCur.close();
                db.closeDB();
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

    private void generateDateListForNearExpiry() {

        NearExpiryDateBO bo;
        nearExpiryDateList.clear();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        bo = new NearExpiryDateBO();
        String dateF = df.format(c.getTime());
        bo.setDate("");
        bo.setDateID(0);
        nearExpiryDateList.add(bo);

        for (int i = 1; i <= 5; i++) {
            c.add(Calendar.MONTH, 1);
            dateF = df.format(c.getTime());
            bo = new NearExpiryDateBO();
            bo.setDate("");
            bo.setDateID(i);
            nearExpiryDateList.add(bo);
        }
    }

    @SuppressLint("UseSparseArrays")
    public void downloadCompetitorFiveFilterLevels() {


        List<String> mLevels = Arrays.asList(bmodel.configurationMasterHelper.COMPETITOR_FILTER_LEVELS.split(","));

        if (mLevels.size() > 0) {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String contentLevelId = mLevels.get(mLevels.size() - 1);


            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(" SELECT PL.LevelID , PL.LevelName ,  PL.Sequence FROM ProductLevel  PL "
                    + " where "
                    + " PL.LevelID =" + mLevels.get(0));
            if (mLevels.size() > 2) {
                stringBuffer.append(" OR PL.LevelID =" + mLevels.get(1));
            }
            if (mLevels.size() > 3) {
                stringBuffer.append(" OR PL.LevelID =" + mLevels.get(2));
            }
            if (mLevels.size() > 4) {
                stringBuffer.append(" OR PL.LevelID =" + mLevels.get(3));
            }
            if (mLevels.size() > 5) {
                stringBuffer.append(" OR PL.LevelID =" + mLevels.get(4));
            }
            Cursor listCursor = db.selectSQL(stringBuffer.toString());

            CompetitorFilterLevelBO mLevelBO;
            mCompetitorSequenceValues = new Vector<>();
            while (listCursor.moveToNext()) {

                mLevelBO = new CompetitorFilterLevelBO();
                mLevelBO.setProductId(listCursor.getInt(0));
                mLevelBO.setLevelName(listCursor.getString(1));
                mLevelBO.setSequence(listCursor.getInt(2));

                mCompetitorSequenceValues.add(mLevelBO);
            }

            listCursor.close();

            int mContentLevel = 0;
            int loopEnd = 0;

            if (!contentLevelId.equals("0")) {
                Cursor seqCur = db
                        .selectSQL("SELECT IFNULL(PL.Sequence,0) "
                                + "FROM ProductLevel PL "
                                + "WHERE  PL.levelid=" + contentLevelId);
                if (seqCur.moveToNext()) {
                    mContentLevel = seqCur.getInt(0);
                }
                seqCur.close();
            }

            mCompetitorFilterlevelBo = new HashMap<>();

            try {

                if (mCompetitorSequenceValues.size() > 0) {

                    loopEnd = mContentLevel - mCompetitorSequenceValues.get(0).getSequence()
                            + 1;
                    loadCompetitorParentFilter(loopEnd, mCompetitorSequenceValues.get(0).getProductId());

                    for (int i = 1; i < mCompetitorSequenceValues.size(); i++) {
                        loopEnd = mContentLevel
                                - mCompetitorSequenceValues.get(i - 1).getSequence() + 1;

                        loadCompetitorChildFilter(loopEnd,
                                mCompetitorSequenceValues.get(i).getSequence(),
                                mCompetitorSequenceValues.get(i - 1).getSequence(),
                                mCompetitorSequenceValues.get(i).getProductId(),
                                mCompetitorSequenceValues.get(i - 1).getProductId());
                    }
                }

            } catch (Exception e) {
                Commons.print(e.getMessage());
            }
        }
    }

    private void loadCompetitorParentFilter(int loopEnd, int mProductLevelId) {
        //Select CPM.CPID,CPM.CPName,PL.LevelName from CompetitorProductMaster CPM Left join ProductLevel PL on PL.LevelId = CPM.Plid

        Vector<CompetitorFilterLevelBO> mFilterLevel;


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();

        String query = "SELECT DISTINCT CPM1.CPID,CPM1.CPName FROM CompetitorProductMaster CPM1";

        for (int i = 2; i <= loopEnd; i++)
            query = query + " INNER JOIN CompetitorProductMaster CPM" + i + " ON CPM" + i
                    + ".CPTid = CPM" + (i - 1) + ".CPID";

        query = query
                + " WHERE CPM1.PLid = " + mProductLevelId + " Order By CPM1.RowId";

        Cursor c = db.selectSQL(query);

        if (c != null) {

            mFilterLevel = new Vector<>();
            while (c.moveToNext()) {
                CompetitorFilterLevelBO mLevelBO = new CompetitorFilterLevelBO();
                mLevelBO.setProductId(c.getInt(0));
                mLevelBO.setLevelName(c.getString(1));
                mFilterLevel.add(mLevelBO);
            }
            mCompetitorFilterlevelBo.put(mProductLevelId, mFilterLevel);
            c.close();
        }

    }

    private void loadCompetitorChildFilter(int loopEnd, int mChildLevel,
                                           int mParentLevel, int mLevelId, int mParentLevelId) {

        int filterGap = mChildLevel - mParentLevel + 1;

        String query = "SELECT DISTINCT CPM1.CPID, CPM" + filterGap + ".CPID,  CPM"
                + filterGap + ".CPName FROM CompetitorProductMaster CPM1 ";

        for (int i = 2; i <= loopEnd; i++)
            query = query + " INNER JOIN CompetitorProductMaster CPM" + i + " ON CPM" + i
                    + ".CPTid = CPM" + (i - 1) + ".CPID";

        query = query + " WHERE CPM1.PLid = " + mParentLevelId
                + " Order By CPM" + filterGap + ".RowId,CPM1.RowId";

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);

        db.openDataBase();

        Cursor c = db.selectSQL(query);
        Vector<CompetitorFilterLevelBO> mFilterLevel;
        if (c != null) {
            mFilterLevel = new Vector<>();
            while (c.moveToNext()) {
                CompetitorFilterLevelBO mLevelBO = new CompetitorFilterLevelBO();
                mLevelBO.setParentId(c.getInt(0));
                mLevelBO.setProductId(c.getInt(1));
                mLevelBO.setLevelName(c.getString(2));
                mFilterLevel.add(mLevelBO);
            }
            mCompetitorFilterlevelBo.put(mLevelId, mFilterLevel);
            c.close();
            db.close();
        }
    }


    /**
     * Download filter product levels mapped for a particular module.
     *
     * @param moduleName
     * @return Vector of Type LevelBO
     */
    public Vector<LevelBO> downloadFilterLevel(String moduleName) {
        Vector<LevelBO> filterLevel = new Vector<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);

            db.openDataBase();

            Cursor listCursor = db
                    .selectSQL(" SELECT distinct PL.LevelID , PL.LevelName ,  PL.Sequence FROM ProductLevel  PL "
                            + " INNER JOIN ConfigActivityFilter CA  ON "
                            + " PL.LevelID =CA.ProductFilter1 OR  "
                            + " PL.LevelID =CA.ProductFilter2 OR  "
                            + " PL.LevelID =CA.ProductFilter3 OR  "
                            + " PL.LevelID =CA.ProductFilter4 OR  "
                            + " PL.LevelID =CA.ProductFilter5  "
                            + " WHERE  CA.ActivityCode='" + moduleName + "' order by PL.Sequence");

            LevelBO mLevelBO;
            while (listCursor.moveToNext()) {
                mLevelBO = new LevelBO();
                mLevelBO.setProductID(listCursor.getInt(0));
                mLevelBO.setLevelName(listCursor.getString(1));
                mLevelBO.setSequence(listCursor.getInt(2));

                filterLevel.add(mLevelBO);
            }
            listCursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (moduleName.equals("MENU_STK_ORD") && bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
            mLoadedGlobalProductId = mSelectedGlobalProductId;

        return filterLevel;
    }

    public HashMap<Integer, Vector<LevelBO>> downloadFilterLevelProducts(Vector<LevelBO> filterProductLevels, boolean isDsdModule) {
        HashMap<Integer, Vector<LevelBO>> filterLevelPrdByLevelId = new HashMap<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();


            if (filterProductLevels != null) {

                StringBuilder pLIds = new StringBuilder();
                for (LevelBO levelBO : filterProductLevels) {

                    if (pLIds.length() > 0)
                        pLIds.append(",");

                    pLIds.append(levelBO.getProductID());

                }

                String query = "SELECT DISTINCT PM.PID, PM.PName,PM.ParentHierarchy,PM.PLid,PM.ParentId FROM ProductMaster PM "
                        + " WHERE PM.PLid in (" + pLIds + ") ";

                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && mSelectedGlobalProductId != 0)
                    query = query + " and PM.ParentHierarchy LIKE '%/' || " + mSelectedGlobalProductId + " || '/%'";

                query = query + " Order By PM.RowId";

                Cursor seqCur = db.selectSQL(query);
                if (seqCur != null) {


                    while (seqCur.moveToNext()) {
                        LevelBO mLevelBO = new LevelBO();
                        mLevelBO.setProductID(seqCur.getInt(0));
                        mLevelBO.setLevelName(seqCur.getString(1));
                        mLevelBO.setParentHierarchy(seqCur.getString(2));
                        mLevelBO.setParentID(seqCur.getInt(4));
                        if (isDsdModule) {
                            if (isPrdAvailable(mLevelBO.getProductID())) {
                                if (filterLevelPrdByLevelId.get(seqCur.getInt(3)) != null) {
                                    filterLevelPrdByLevelId.get(seqCur.getInt(3)).add(mLevelBO);
                                } else {
                                    Vector<LevelBO> filterProducts = new Vector<>();
                                    filterProducts.add(mLevelBO);
                                    filterLevelPrdByLevelId.put(seqCur.getInt(3), filterProducts);
                                }
                            }
                        } else {
                            if (filterLevelPrdByLevelId.get(seqCur.getInt(3)) != null) {
                                filterLevelPrdByLevelId.get(seqCur.getInt(3)).add(mLevelBO);
                            } else {
                                Vector<LevelBO> filterProducts = new Vector<>();
                                filterProducts.add(mLevelBO);
                                filterLevelPrdByLevelId.put(seqCur.getInt(3), filterProducts);
                            }
                        }
                    }
                    seqCur.close();
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return filterLevelPrdByLevelId;
    }


    private boolean isPrdAvailable(int pid) {
        boolean isAvailable = false;
        if (productMaster != null) {
            for (ProductMasterBO productMasterBO : productMaster) {
                if (productMasterBO.getParentHierarchy().contains("/" + pid + "/")) {
                    isAvailable = true;
                    break;
                }
            }
        } else
            isAvailable = true;

        return isAvailable;

    }

    public GenericObjectPair downloadProducts(String moduleCode) {

        Map<String, ProductMasterBO> productMasterById = new HashMap<>();
        Vector<ProductMasterBO> productMaster;
        GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = null;

        try {

            // load location and date
            downloadInStoreLocationsForStockCheck();
            generateDateListForNearExpiry();
            ArrayList<StandardListBO> uomList = downloadUomList();

            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

            db.openDataBase();

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


            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);
            String sql = "";

            int mContentLevelId = getContentLevel(db, moduleCode);
            sql = "select A.pid as productId, A.pcode as pcode,A.pname as pname,A.parentid as parentId,A.sih as sih, "
                    + "A.psname as psname,A.barcode as barcode,A.vat as vat ,A.isfocus as isfocus, max(ifnull("
                    + str
                    + ")) as srp , max(ifnull("
                    + csrp
                    + ")) as csrp ,max(ifnull("
                    + osrp
                    + ")) as osrp ,A.msqqty as msqqty,"
                    + "A.dUomQty as caseQty,A.duomid as caseUomId, u.ListCode as ListCode ,A.MRP as MRP,"
                    + " ifnull(sbd.DrpQty,0) as DrpQty,ifnull(sbd.grpName,'') as grpName,A.RField1 as RField1 ,PWHS.qty as PWHSqty ,A.IsAlloc as IsAlloc, "
                    + getSpecialFilterQuery(mContentLevelId)
                    + "dOuomQty as outersize ,dOuomid as dOuomid,caseBarcode as caseBarcode,outerBarcode as outerBarcode,"
                    + " piece_uomid as piece_uomid ,A.mrp as mrp,"
                    + " A.isSalable as isSalable,A.isReturnable as isReturnable,A.TypeID as TypeID,A.baseprice as baseprice,"
                    + " A.weight as weight,"
                    + " A.Hasserial as Hasserial,(CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId()
                    + " THEN F.scid ELSE 0 END) as groupid,"
                    + " A.tagDescription as tagDescription,"
                    + " A.HSNId as HSNId,"
                    + " HSN.HSNCode as HSNCode,"
                    + " A.IsDrug as IsDrug,A.ParentHierarchy as ParentHierarchy,"
                    + " F.priceoffvalue as priceoffvalue,F.PriceOffId as priceoffid,F.ASRP as asrp,"
                    + " (CASE WHEN F.scid =" + bmodel.getRetailerMasterBO().getGroupId() + " THEN F.scid ELSE 0 END) as groupid,"
                    + " (CASE WHEN PWHS.PID=A.PID then 'true' else 'false' end) as IsAvailWareHouse,A.DefaultUom,F.MarginPrice as marginprice"
                    + (bmodel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE ? ",FSH.qty as freeSIH" : ",0 as freeSIH,")
                    + "(CASE WHEN A.PID in (PPM.PID) then '1' else '0' end) as  isTradePromo"
                    + " from ProductMaster A";

            sql = sql + " left join PriceMaster F on A.Pid = F.pid and F.scid = " + bmodel.getRetailerMasterBO().getGroupId()
                    + " left join PriceMaster G on A.Pid = G.pid  and G.scid = 0 "
                    + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                    + " left join SbdDistributionMaster sbd on A.pid=sbd.productid and sbd.channelid=" + bmodel.getRetailerMasterBO().getChannelID()
                    + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=A.pid and PWHS.UomID=A.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                    + " LEFT JOIN HSNMaster HSN ON HSN.HSNId=A.HSNId"
                    + (bmodel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE ? " LEFT JOIN FreeStockInHandMaster FSH ON FSH.pid=A.pid" : "")
                    + " left join PromotionProductMapping PPM on PPM.PID = A.PID"
                    + " WHERE A.PLid IN(" + mContentLevelId + ") ";

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISTRIBUTION) {
                //downloading product distribution and preparing query to get products mapped..
                String pdQuery = downloadProductDistribution(mContentLevelId);
                if (pdQuery.length() > 0) {
                    sql = sql + " and A.pid in(" + pdQuery + ")";
                }
            }

            sql = sql + " group by A.pid ORDER BY " + filter + " A.rowid";


            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(c.getColumnIndex("productId")));
                    product.setProductCode(c.getString(c.getColumnIndex("pcode")));
                    product.setProductName(c.getString(c.getColumnIndex("pname")));
                    product.setParentid(c.getInt(c.getColumnIndex("parentId")));
                    product.setSIH(c.getInt(c.getColumnIndex("sih")));
                    product.setDSIH(c.getInt(c.getColumnIndex("sih")));
                    product.setFreeSIH(c.getInt(c.getColumnIndex("freeSIH")));
                    product.setProductShortName(c.getString(c.getColumnIndex("psname")));
                    product.setBarCode(c.getString(c.getColumnIndex("barcode")));
                    product.setVat(c.getFloat(c.getColumnIndex("vat")));

                    product.setSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(c.getColumnIndex("srp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setTempSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(c.getColumnIndex("srp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setPrevPrice_pc(SDUtil.format(c.getFloat(c.getColumnIndex("srp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0));
                    product.setCsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(c.getColumnIndex("csrp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setPrevPrice_ca(SDUtil.format(c.getFloat(c.getColumnIndex("csrp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0));
                    product.setOsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(c.getColumnIndex("osrp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setPrevPrice_oo(SDUtil.format(c.getFloat(c.getColumnIndex("osrp")), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0));
                    product.setMSQty(c.getInt(c.getColumnIndex("msqqty")));
                    product.setCaseSize(c.getInt(c.getColumnIndex("caseQty")));
                    product.setCaseUomId(c.getInt(c.getColumnIndex("caseUomId")));
                    product.setOU(c.getString(c.getColumnIndex("ListCode")));
                    product.setMRP(c.getDouble(c.getColumnIndex("MRP")));
                    product.setDropQty(c.getInt(c.getColumnIndex("DrpQty")));
                    product.setSbdGroupName(c.getString(c.getColumnIndex("grpName")));
                    product.setRField1(c.getString(c.getColumnIndex("RField1")));
                    product.setWSIH(c.getInt(c.getColumnIndex("PWHSqty")));
                    product.setAllocation(c.getInt(c.getColumnIndex("IsAlloc")));
                    product.setIsMustSell(c.getInt(c.getColumnIndex("IsMustSell")));
                    product.setIsFocusBrand(c.getInt(c.getColumnIndex("IsFocusBrand")));
                    product.setIsFocusBrand2(c.getInt(c.getColumnIndex("IsFocusBrand2")));
                    product.setOutersize(c.getInt(c.getColumnIndex("outersize")));
                    product.setOuUomid(c.getInt(c.getColumnIndex("dOuomid")));
                    product.setCasebarcode(c.getString(c.getColumnIndex("caseBarcode")));
                    product.setOuterbarcode(c.getString(c.getColumnIndex("outerBarcode")));
                    product.setPcUomid(c.getInt(c.getColumnIndex("piece_uomid")));// Pc Uomid
                    product.setMinprice(c.getInt(c.getColumnIndex("mrp")));
                    product.setMaxPrice(c.getInt(c.getColumnIndex("mrp")));
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                        product.setBatchwiseProductCount(1);
                    product.setBatchwiseProductCount(0);
                    product.setIsSaleable(c.getInt(c.getColumnIndex("isSalable")));
                    product.setIsReturnable(c.getInt(c.getColumnIndex("isReturnable")));
                    product.setTypeID(c.getInt(c.getColumnIndex("TypeID")));
                    product.setBaseprice(c.getFloat(c.getColumnIndex("baseprice")));
                    product.setBrandname("");
                    product.setcParentid(0);

                    product.setGroupid(c.getInt(c.getColumnIndex("groupid")));
                    product.setLocations(cloneInStoreLocationList(locations));

                    for (int i = 0; i < locations.size(); i++) {
                        product.getLocations().get(i)
                                .setNearexpiryDate(cloneDateList(nearExpiryDateList));
                    }
                    product.setIsNMustSell(c.getInt(c.getColumnIndex("IsNMustSell")));
                    product.setWeight(c.getFloat(c.getColumnIndex("weight")));
                    product.setScannedProduct(c.getInt(c.getColumnIndex("Hasserial")));

                    product.setIsFocusBrand3(c.getInt(c.getColumnIndex("IsFocusBrand3")));
                    product.setIsFocusBrand4(c.getInt(c.getColumnIndex("IsFocusBrand4")));
                    product.setIsSMP(c.getInt(c.getColumnIndex("IsSMP")));
                    product.setDescription(c.getString(c.getColumnIndex("tagDescription")));

                    product.setIsNearExpiryTaggedProduct(c.getInt(c.getColumnIndex("isNearExpiry")));

                    product.setPriceoffvalue(c.getDouble(c.getColumnIndex("priceoffvalue")));
                    product.setPriceOffId(c.getInt(c.getColumnIndex("priceoffid")));
                    product.setASRP(c.getFloat(c.getColumnIndex("asrp"))); //added by murugan

                    product.setAvailableinWareHouse(c.getString(c.getColumnIndex("IsAvailWareHouse")).equals("true"));
                    product.setHsnId(c.getInt(c.getColumnIndex("HSNId")));
                    product.setHsnCode(c.getString(c.getColumnIndex("HSNCode")));
                    product.setIsDrug(c.getInt(c.getColumnIndex("IsDrug")));
                    product.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));
                    product.setmTradePromotion(c.getInt(c.getColumnIndex("isTradePromo")));
                    if (bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM) {
                        if (c.getInt(c.getColumnIndex("DefaultUom")) == 0) {
                            if (product.getPcUomid() > 0)
                                product.setDefaultUomId(product.getPcUomid());
                            else if (product.getCaseUomId() > 0)
                                product.setDefaultUomId(product.getCaseUomId());
                            else if (product.getOuUomid() > 0)
                                product.setDefaultUomId(product.getOuUomid());
                        } else
                            product.setDefaultUomId(c.getInt(c.getColumnIndex("DefaultUom")));
                        product.setProductWiseUomList(cloneUOMList(uomList, product));
                    }
                    product.setMarginPrice(c.getString(c.getColumnIndex("marginprice")));
                    productMaster.add(product);
                    productMasterById.put(product.getProductID(), product);
                    genericObjectPair = new GenericObjectPair<>(productMaster, productMasterById);


                }
                c.close();
            }

            db.closeDB();

            if (bmodel.configurationMasterHelper.SHOW_TAX_MASTER) {
                taxHelper.downloadProductTaxDetails();
            }

            downloadAttributes();
            if (getmAttributesList().size() > 0)
                downloadAttributeProductMapping();


            if (filterProductLevels.size() > 0
                    && (getLstProductAttributeMapping() != null
                    && getLstProductAttributeMapping().size() > 0))
                downloadLeastBrandProductMapping(mContentLevelId, filterProductLevels.size(), moduleCode);


        } catch (Exception e) {
            Commons.printException(e);
        }
        return genericObjectPair;
    }

    private String getSpecialFilterQuery(int mContentLevelId) {

        StringBuilder stringBuilder = new StringBuilder();
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

        ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(mContext);
        if (filter10) {
            MSLproductIds = productTaggingHelper.getTaggedProductIds(mContext,"MSL", mContentLevelId);
            stringBuilder.append("A.pid in(" + MSLproductIds + ") as IsMustSell,");
        } else {
            stringBuilder.append("0 as IsMustSell,");
        }
        if (filter16) {
            NMSLproductIds = productTaggingHelper.getTaggedProductIds(mContext,"NMSL", mContentLevelId);
            stringBuilder.append("A.pid in(" + NMSLproductIds + ") as IsNMustSell,");

        } else {
            stringBuilder.append("0 as IsNMustSell,");
        }
        if (filter11) {
            FCBNDproductIds = productTaggingHelper.getTaggedProductIds(mContext,"FCBND", mContentLevelId);
            stringBuilder.append("A.pid in(" + FCBNDproductIds + ") as IsFocusBrand,");
        } else {
            stringBuilder.append("0 as IsFocusBrand,");
        }
        if (filter12) {
            FCBND2productIds = productTaggingHelper.getTaggedProductIds(mContext,"FCBND2", mContentLevelId);
            stringBuilder.append("A.pid in(" + FCBND2productIds + ") as IsFocusBrand2,");
        } else {
            stringBuilder.append("0 as IsFocusBrand2,");
        }
        if (filter20) {
            FCBND3productIds = productTaggingHelper.getTaggedProductIds(mContext,"FCBND3", mContentLevelId);
            stringBuilder.append("A.pid in(" + FCBND3productIds + ") as IsFocusBrand3,");
        } else {
            stringBuilder.append("0 as IsFocusBrand3,");
        }
        if (filter21) {
            FCBND4productIds = productTaggingHelper.getTaggedProductIds(mContext,"FCBND4", mContentLevelId);
            stringBuilder.append("A.pid in(" + FCBND4productIds + ") as IsFocusBrand4,");
        } else {
            stringBuilder.append("0 as IsFocusBrand4,");
        }
        if (filter22) {
            SMPproductIds = productTaggingHelper.getTaggedProductIds(mContext,"SMP", mContentLevelId);
            stringBuilder.append("A.pid in(" + SMPproductIds + ") as IsSMP,");
        } else {
            stringBuilder.append("0 as IsSMP,");
        }
        if (filter19) {
            nearExpiryTaggedProductIds = productTaggingHelper.getTaggedProductIds(mContext,"MENU_NEAREXPIRY", mContentLevelId);
            stringBuilder.append("A.pid in(" + nearExpiryTaggedProductIds + ") as isNearExpiry,");
        } else {
            stringBuilder.append("0 as isNearExpiry,");
        }

        return stringBuilder.toString();

    }


    /**
     * Download products based on given distribution type(Route/Retailer/SalesType)
     *
     * @param mContentLevelId to identify given products level id
     * @return Returns a query which gets products(content level) mapped to current distribution type
     */
    private String downloadProductDistribution(int mContentLevelId) {

        String PRODUCT_DISTRIBUTION_TYPE_ROUTE = "ROUTE";
        String PRODUCT_DISTRIBUTION_TYPE_RETAILER = "RETAILER";
        String PRODUCT_DISTRIBUTION_TYPE_SALES_TYPE = "SALES_TYPE";
        String PRODUCT_DISTRIBUTION_TYPE_CHANNEL = "CHANNEL";

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        String productIds = "";

        try {
            db.openDataBase();
            Cursor cursor;
            StringBuilder stringBuilder;

            if (!bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE.equals("")) {

                //getting products mapped
                stringBuilder = new StringBuilder();
                stringBuilder.append("select distinct PM.PID  from ProductDistribution " +
                        " inner join ProductMaster PM on PM.ParentHierarchy LIKE '%/' || productid || '/%'and PM.PLid = " + mContentLevelId +
                        " where criteriaType=" + bmodel.QT(bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE));
                if (bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE.equals(PRODUCT_DISTRIBUTION_TYPE_ROUTE)) {
                    stringBuilder.append(" and criteriaid IN(" + getRetailerBeat(bmodel.getRetailerMasterBO().getRetailerID(), db) + ")");
                } else if (bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE.equals(PRODUCT_DISTRIBUTION_TYPE_RETAILER)) {
                    stringBuilder.append(" and criteriaid IN(" + bmodel.getRetailerMasterBO().getRetailerID() + ")");
                } else if (bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE.equals(PRODUCT_DISTRIBUTION_TYPE_SALES_TYPE)) {
                    stringBuilder.append(" and criteriaid IN(" + bmodel.getRetailerMasterBO().getSalesTypeId() + ")");
                } else if (bmodel.configurationMasterHelper.PRD_DISTRIBUTION_TYPE.equals(PRODUCT_DISTRIBUTION_TYPE_CHANNEL)) {
                    stringBuilder.append(" and criteriaid IN(" + bmodel.getRetailerMasterBO().getSubchannelid() + ") ");
                    stringBuilder.append(" OR criteriaid IN(" + bmodel.channelMasterHelper.getChannelHierarchy(bmodel.getRetailerMasterBO().getSubchannelid(), mContext) + ")");
                }
                cursor = db.selectSQL(stringBuilder.toString());
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        if (productIds.length() > 0)
                            productIds += ",";

                        productIds += cursor.getString(0);
                    }
                    cursor.close();
                }


            }

            db.closeDB();
        } catch (Exception ex) {
            Commons.printException(ex);
            return "";
        } finally {
            db.closeDB();
        }

        return productIds;

    }

    /**
     * Get beat ids for current retailer
     *
     * @param retailerId
     * @param db
     * @return
     */
    private String getRetailerBeat(String retailerId, DBUtil db) {
        String beatids = "";
        try {
            //getting retailer beat..
            String beats = "";
            Cursor cursor = db.selectSQL("select beatid from RetailerBeatMapping where retailerid=" + retailerId);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (beats.length() > 0)
                        beats += ",";

                    beats += cursor.getString(0);
                }
                cursor.close();
            }

            beatids = beats;
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return beatids;
    }


    private void downloadAttributeProductMapping() {
        DBUtil db = null;
        mAttributeByProductId = new HashMap<>();
        lstProductAttributeMapping = new ArrayList<>();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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

    private void downloadLeastBrandProductMapping(int contentlevelId, int childLevel, String moduleCode) {
        try {
            String sql = "SELECT prdm.PID,PM.PID FROM ProductMaster PM "
                    + " INNER JOIN ProductMaster prdm on prdm.ParentHierarchy LIKE '%/' || PM.PID || '/%' and prdm.PLid =" + contentlevelId
                    + " WHERE PM.PLid IN (SELECT ProductFilter" + childLevel + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = " + bmodel.QT(moduleCode) + ")";

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                mProductIdByBrandId = new HashMap<>();
                while (c.moveToNext()) {

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

    public void updateProductColorAndSequance() {// for piramal

        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select pid,SM.listName,SM.flex1,RField from RetailerProductDisplay RP " +
                    "left join standardListMaster SM ON RP.colorId=SM.listId where RP.RetailerId=" +
                    bmodel.retailerMasterBO.getRetailerID());
            if (c != null) {
                while (c.moveToNext()) {

                    for (ProductMasterBO productMasterBO : getProductMaster()) {
                        if (productMasterBO.getParentid() == c.getInt(0)) {
                            productMasterBO.setColorCode(c.getString(1));
                            productMasterBO.setProductSequence(c.getInt(2));
                            productMasterBO.setSoInventory(c.getInt(3));
                            break;
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

    private StringBuffer downloadProductSequenceFromFilter() {
        StringBuffer filter = new StringBuffer();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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
     * This method will check whether the product is an initiative product or
     * not and update in ProductBO. Also load the Drop size if the product is
     * initiative product. Data fetched from InitiativeDetailMaster table.
     */
    public void loadInitiativeProducts() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            if (productIds.size() > 0 && map.size() > 0) {
                for (int i = 0; i < productMaster.size(); i++) {
                    ProductMasterBO p = productMaster.get(i);
                    Integer prodId = Integer.valueOf(p.getProductID());
                    if (productIds.contains(prodId)) {
                        p.setIsInitiativeProduct(1);
                        p.setInitDropSize(map.get(prodId));
                        productMaster.setElementAt(p, i);
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadOldBatchIDMap() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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

    public int getOldBatchIDByMfd(String prodId) {
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



    public void clearOrderTableForInitiative() {
        ProductMasterBO product;
        if (productMaster != null) {
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);
                product.setOrderedPcsQty(0);
                product.setOrderedCaseQty(0);
                product.setOrderedOuterQty(0);
                product.setFoc(0);
            }
        }
    }

    public void clearOrderTable() {
        ProductMasterBO product;
        bmodel.setOrderHeaderBO(null);
        if (productMaster != null) {
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);
                product.setOrderedPcsQty(0);
                product.setOrderedCaseQty(0);
                product.setOrderedOuterQty(0);
                product.setLocalOrderPieceqty(0);
                product.setLocalOrderCaseqty(0);
                product.setLocalOrderOuterQty(0);
                product.setFoc(0);
                product.setSelectedUomId(0);
                //clear product wise reason
                product.setSoreasonId(0);
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
                product.setProductLevelDiscountValue(0);
                product.setDistributorTypeDiscount(0);
                product.setCompanyTypeDiscount(0);
                int size = product.getLocations().size();
                for (int z = 0; z < size; z++) {
                    product.getLocations().get(z).setShelfOuter(-1);
                    product.getLocations().get(z).setShelfCase(-1);
                    product.getLocations().get(z).setShelfPiece(-1);

                    product.getLocations().get(z).setAvailability(-1);

                    product.getLocations().get(z).setWHOuter(0);
                    product.getLocations().get(z).setWHCase(0);
                    product.getLocations().get(z).setWHPiece(0);
                    product.getLocations().get(z).setFacingQty(0);
                }
                product.setTotalStockQty(0);

                //clear delivered qty
                product.setDeliveredCaseQty(0);
                product.setDeliveredOuterQty(0);
                product.setDeliveredPcsQty(0);

                //clear suggested Qty
                product.setSocInventory(0);
                product.setSoInventory(0);

                product.setRepPieceQty(0);
                product.setRepCaseQty(0);
                product.setRepOuterQty(0);
                product.setSelectedSalesReturnPosition(0);
                product.setSeparateBill(false);
                product.setTaxableAmount(0);

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
                product.setDSIH(product.getSIH() >= newsi ? product.getSIH()
                        - newsi : 0);

            }
        }
    }

    public void clearCombindStockCheckedTable() {
        ProductMasterBO product;
        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);


            product.setIsDistributed(0);
            product.setIsListed(0);
            product.setPrice_ca("0");
            product.setPrice_oo("0");
            product.setPrice_pc("0");
            product.setReasonID("0");
            product.setPriceChangeReasonID("0");
            product.setPriceChanged(0);
            product.setPriceCompliance(0);

            product.setMrp_pc("0");
            product.setMrp_ca("0");
            product.setMrp_ou("0");
            int size = product.getLocations().size();
            for (int z = 0; z < size; z++) {
                product.getLocations().get(z).setShelfOuter(-1);
                product.getLocations().get(z).setShelfCase(-1);
                product.getLocations().get(z).setShelfPiece(-1);

                product.getLocations().get(z).setAvailability(-1);
                product.getLocations().get(z).setReasonId(0);
                product.getLocations().get(z).setFacingQty(0);

                if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK) {
                    int nearSize = product.getLocations().get(z).getNearexpiryDate().size();
                    for (int x = 0; x < nearSize; x++) {
                        product.getLocations().get(z).getNearexpiryDate().get(x)
                                .setNearexpPC("0");

                        product.getLocations().get(z).getNearexpiryDate().get(x)
                                .setNearexpCA("0");

                        product.getLocations().get(z).getNearexpiryDate().get(x)
                                .setNearexpOU("0");
                    }
                }

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
            product.setFoc(0);
            product.setCheked(false);
        }
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

    public boolean isMustSellFilledStockCheck(boolean isTaggedProducts, Context context) {

        boolean isSkuFilled = true;

        Vector<ProductMasterBO> productList;
        if (isTaggedProducts) {
            productList = ProductTaggingHelper.getInstance(context).getTaggedProducts();
        } else {
            productList = getProductMaster();
        }

        int siz = productList.size();
        if (siz == 0)
            return false;
        loop:
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productList.get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getIsMustSell() == 1) {
                    if ((product.getLocations().get(j).getShelfPiece() < 0
                            && product.getLocations().get(j).getShelfCase() < 0
                            && product.getLocations().get(j).getShelfOuter() < 0
                            && product.getLocations().get(j).getWHPiece() == 0
                            && product.getLocations().get(j).getWHCase() == 0
                            && product.getLocations().get(j).getWHOuter() == 0
                            && product.getLocations().get(j).getCockTailQty() == 0
                            && product.getIsListed() == 0
                            && product.getIsDistributed() == 0
                            && product.getLocations().get(j).getReasonId() == 0
                            && product.getLocations().get(j).getAvailability() < 0)) {
                        if (j == siz1 - 1) {
                            isSkuFilled = false;
                            break loop;
                        }
                    } else {
                        if (product.getLocations().get(j).getAvailability() == 0 && StockCheckHelper.getInstance(context).SHOW_STOCK_RSN && product.getLocations().get(j).getReasonId() == 0) {
                            isSkuFilled = false;
                            break loop;
                        } else {
                            isSkuFilled = true;
                            j = siz1;
                        }
                    }
                }

            }
        }
        return isSkuFilled;
    }

    public void updateProductColor() {
        try {
            ProductMasterBO product;
            int siz = productMaster.size();
            for (int i = 0; i < siz; ++i) {
                product = productMaster.get(i);

                if (product.getmDeadProduct() == 1
                        && getFilterColor("Filt15") != 0)
                    product.setTextColor(getFilterColor("Filt15"));
                else if (product.getIsMustSell() == 1
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
                            R.color.list_item_primary_text_color));

                if (bmodel.configurationMasterHelper.SHOW_HIGHLIGHT_FOR_OOS
                        && product.getWSIH() == 0)
                    product.setTextColor(mContext.getResources().getColor(
                            R.color.RED));

            }
        } catch (Exception e) {

            Commons.printException(e);
        }

    }

    public int getFilterColor(String filtername) {

        Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                .getGenFilter();
        try {
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getConfigCode()
                        .equalsIgnoreCase(filtername)) {
                    if (!genfilter.get(i).getMenuNumber().equals("0") && !genfilter.get(i).getMenuNumber().equals(""))
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
        bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
        ArrayList<InvoiceHeaderBO> items = bmodel.getInvoiceHeaderBO();
        if (items != null && items.size() > 0) {

            for (InvoiceHeaderBO invoiceHeaderBo : items) {

                String invoiceDay = invoiceHeaderBo.getInvoiceDate();
                String currentDay = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                int dayCount = DateTimeUtils.getDateCount(invoiceDay, currentDay,
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

    /*To check whether the due date has expired or not for the invoices done*/
    public boolean isDueDateExpired() {
        bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
        ArrayList<InvoiceHeaderBO> items = bmodel.getInvoiceHeaderBO();
        try {
            if (items != null && !items.isEmpty()) {

                for (InvoiceHeaderBO invoiceHeaderBo : items) {

                    String dueDate = invoiceHeaderBo.getDueDate();
                    String collectionDate = invoiceHeaderBo.getCollectionDate();
                    SimpleDateFormat format = new SimpleDateFormat(ConfigurationMasterHelper.outDateFormat, Locale.getDefault());
                    Date date = format.parse(dueDate);
                    dueDate = DateTimeUtils.convertDateObjectToRequestedFormat(
                            date, "yyyy/MM/dd");
                    String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                    int dayCount = 0;
                    if (collectionDate == null || collectionDate.isEmpty()) {
                        dayCount = DateTimeUtils.getDateCount(dueDate, currentDate,
                                "yyyy/MM/dd");
                    }
                    if (dayCount > 0) {
                        return false;
                    }

                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return true;
    }

    /**
     * Download the isReturnable products and its Quantity from ProductMaster
     * and BomMaster, UomMaster and PriceMaster
     */
    public void downlaodReturnableProducts(String module) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                bomReturnProducts = new ArrayList<BomReturnBO>();
                while (c.moveToNext()) {
                    BomReturnBO bomMasterBO = new BomReturnBO();
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
                        int pid = SDUtil.convertToInt(bomMasterBO.getPid());
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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

        for (BomReturnBO bom : getBomReturnProducts()) {
            bom.setLiableQty(0);
            bom.setReturnQty(0);
        }
        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
            for (BomReturnBO bom : getBomReturnTypeProducts()) {
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

                        for (BomReturnBO returnBo : getBomReturnProducts()) {
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

            for (BomReturnBO bomReturnBo : getBomReturnProducts()) {
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

            for (BomReturnBO bomReturnBo : getBomReturnTypeProducts()) {
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            String uid = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

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

            ArrayList<BomReturnBO> returnProducts = null;
            String returncolumns = "OrderID,Pid,LiableQty,ReturnQty,Qty,Price, UomID,TypeID,LineValue,RetailerID";
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                returnProducts = bmodel.productHelper
                        .getBomReturnTypeProducts();
            } else {
                returnProducts = bmodel.productHelper.getBomReturnProducts();

            }
            String pid;
            for (BomReturnBO bomReturnBo : returnProducts) {

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
                        + "") + " WHERE upload !='X' and RetailerID = "
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                int prodcutID = SDUtil.convertToInt(indicativeProductBO
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                            + indicativeOrderId + " and invoicestatus=1 and upload!='X' ");
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            // sb.append("Select Distinct PM.Pid,PM.Pname From ProductMaster PM where PM.isReturnable =1 and TypeID  !=0 order by PM.Pid");
            sb.append("Select Distinct PM.Pid,PM.Pname,PM.Psname From ProductMaster PM INNER JOIN StandardListMaster SLM on PM.TypeId = SLM.ListId");
            sb.append(" and SLM.ListCode ='GENERIC' WHERE PM.isReturnable = 1 ORDER BY PM.Pid");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                bomReturnTypeProducts = new ArrayList<BomReturnBO>();
                while (c.moveToNext()) {
                    BomReturnBO bomMasterBO = new BomReturnBO();
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

            for (BomReturnBO groupWiseProducts : bmodel.productHelper
                    .getBomReturnTypeProducts()) {
                total = 0;

                for (BomReturnBO bomReturnProducts : bmodel.productHelper
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
            for (BomReturnBO temp : getBomReturnProducts()) {
                temp.setLiableQty(0);
                temp.setReturnQty(0);
            }
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                for (BomReturnBO temp : getBomReturnTypeProducts()) {
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            sb.append(" and OH.upload!='X' and OD.upload='N' GROUP BY IO.pid");
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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

        ArrayList<BomReturnBO> returnProducts = null;
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
        for (BomReturnBO bomReturnBo : returnProducts) {
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
                sb.append(QT(String.valueOf(SDUtil.formatAsPerCalculationConfig((bomReturnBo.getLiableQty() - bomReturnBo
                        .getReturnQty()) * (double) bomReturnBo.getpSrp()))) + ",");
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                    for (BomReturnBO bomReturnBo : getBomReturnTypeProducts()) {
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
                for (BomReturnBO bomReturnBo : getBomReturnTypeProducts()) {

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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor cur = db
                    .selectSQL("SELECT  B.Bpid,A.PID,A.Pname  FROM ProductMaster A inner JOIN BomMaster B on A.pid = B.pid WHERE A.isReturnable = '1' Order by A.pid");

            if (cur != null) {
                while (cur.moveToNext()) {
                    for (BomReturnBO product : getBomReturnProducts()) {

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

            inStoreLocation = new Vector<>();
            StandardListBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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

    public ArrayList<BomReturnBO> getBomReturnProducts() {
        if (bomReturnProducts == null)
            bomReturnProducts = new ArrayList<>();
        return bomReturnProducts;
    }

    public ArrayList<BomMasterBO> getBomMaster() {
        return bomMaster;
    }

    public ArrayList<BomReturnBO> getBomReturnTypeProducts() {
        if (bomReturnTypeProducts == null)
            return new ArrayList<>();
        return bomReturnTypeProducts;
    }

    private Vector<LoadManagementBO> productlist;

    public Vector<LoadManagementBO> downloadLoadMgmtProductsWithFiveLevel(
            String moduleCode, String batchmenucode) {
        mLoadManagementBOByProductId = new SparseArray<>();
        String sql = "", sql1 = "", sql2 = "", sql3 = "", sql4 = "";
        String nSIHColumn = "";
        productlist = new Vector<>();
        LoadManagementBO bo;
        Vector<LoadManagementBO> list;
        LoadManagementBO batchnobo;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();

        if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")
                || batchmenucode.equals("MENU_VAN_UNLOAD")
                || batchmenucode.equals("MENU_CUR_STK_BATCH")
                || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
            sql = "  LEFT JOIN StockInHandMaster SIH ON SIH.pid=PM.PID"
                    + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid AND PM.pid=BM.pid)";
            sql1 = ",IFNULL(BM.batchNum,'') as batchNum,SIH.qty as qty,SIH.adjusted_qty,SIH.batchid";

            // Unload non salable qty from NonSalableSIHMaster
            sql4 = " left join ( select pid, SUM(qty) as nsihqty from NonSalableSIHMaster group by pid) as NSIH ON NSIH.pid = PM.PID";
            nSIHColumn = ",NSIH.nsihqty";
        } else {
            sql = "";
            sql1 = "";
            sql4 = "";
        }
        if (batchmenucode.equals("MENU_STOCK_PROPOSAL")
                || batchmenucode.equals("MENU_STK_PRO")) {
            sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM.PID";
            sql3 = " ,A.qty, A.pcsQty, A.caseQty, A.outerQty";
        } else {
            sql2 = "";
            sql3 = "";
        }
        String query = "SELECT PM.ParentId, PM.PID, PM.PName,"
                + " (select qty from StockProposalNorm PSQ  where uomid =PM.piece_uomid and PM.PID = PSQ.PID) as sugpcs,"
                + " PM.psname, PM.dUomQty,"
                + " PM.sih, PWHS.Qty, PM.IsAlloc, PM.mrp, PM.barcode, PM.RField1, PM.dOuomQty,"
                + " PM.isMust, PM.maxQty,(select qty from ProductStandardStockMaster PSM  where uomid =PM.piece_uomid and PM.PID = PSM.PID) as stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM.dUomId and PM.PID = PSM.PID) as stdcase,(select qty from ProductStandardStockMaster PSM where uomid =PM.dOuomid and PM.PID = PSM.PID) as stdouter, PM.dUomId, PM.dOuomid,"
                + " PM.baseprice, PM.piece_uomid, PM.PLid, PM.pCode, PM.msqQty, PM.issalable" // + ",(CASE WHEN PWHS.PID=PM.PID then 'true' else 'false' end) as IsAvailWareHouse"
                + sql3
                + sql1
                + " ,(select qty from StockProposalNorm PSQ  where uomid =PM.dUomId and PM.PID = PSQ.PID) as sugcs,"
                + " (select qty from StockProposalNorm PSQ  where uomid =PM.dOuomid and PM.PID = PSQ.PID) as sugou,PM.pCode as ProCode,"
                + "  PM.ParentHierarchy as ParentHierarchy" + nSIHColumn
                + " FROM ProductMaster PM"
                + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM.pid and PWHS.UomID=PM.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                + sql2
                + sql
                + sql4
                + " WHERE PM.PLid IN"
                + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                + bmodel.QT(moduleCode) + ")";
        Cursor c = db.selectSQL(query);
        if (c != null) {
            while (c.moveToNext()) {
                bo = new LoadManagementBO();
                bo.setParentid(c.getInt(0));
                bo.setProductid(c.getInt(1));
                bo.setProductname(c.getString(2));
                bo.setProductCode(c.getString(c.getColumnIndex("ProCode")));
                bo.setSuggestqty(c.getInt(c.getColumnIndex("sugpcs")) +
                        (c.getInt(c.getColumnIndex("sugcs")) * c.getInt(5)) +
                        (c.getInt(c.getColumnIndex("sugou")) * c.getInt(12)));
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
                bo.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));

                if (batchmenucode.equals("MENU_STOCK_PROPOSAL")
                        || batchmenucode.equals("MENU_STK_PRO")) {
                    bo.setStkprototalQty(c.getInt(c.getColumnIndex("qty")));
                    bo.setStkpropcsqty(c.getInt(c.getColumnIndex("pcsQty")));
                    bo.setStkprocaseqty(c.getInt(c.getColumnIndex("caseQty")));
                    bo.setStkproouterqty(c.getInt(c.getColumnIndex("outerQty")));
                } else {
                    bo.setNonSalableQty(c.getInt(c.getColumnIndex("nsihqty")));
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

                list = new Vector<>();
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
                bo.setIsFree(0); // loaded free sih
                mLoadManagementBOByProductId.put(bo.getProductid(), bo);

                productlist.add(bo);

            }

            //freeStockInHandMaster
            if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")
                    || batchmenucode.equals("MENU_VAN_UNLOAD")
                    || batchmenucode.equals("MENU_CUR_STK_BATCH")
                    || batchmenucode.equals("MENU_STOCK_ADJUSTMENT"))
                loadDataFromFreeSIHMaster(db, moduleCode);

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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            sBuffer = new StringBuffer();
            sBuffer.append("SELECT IFNULL(SUM(" + columnName + "),0) FROM "
                    + headertable + " OH INNER JOIN  " + detailTable
                    + " OD ON " + ID);
            sBuffer.append(" WHERE  OH.upload!='X' and OH.RetailerID = ");
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

    public Vector<LoadManagementBO> getLoadMgmtProducts() {
        return productlist;
    }

    private void downloadDescriptionByTypeId() {
        mDescriptionByTypeId = new HashMap<Integer, String>();
        mTypeIdList = new ArrayList<Integer>();
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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

        mProductIdListByDiscoutId = new HashMap<>();

        mDiscountIdList = new ArrayList<>();
        DBUtil db;
        StringBuilder sb;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            sb = new StringBuilder();
            sb.append(" select Value,IsPercentage,dm.Typeid,SM.Listname,ApplyLevelid,Moduleid,dm.DiscountId,PM.PID,dm.isCompanyGiven,dm.ComputeAfterTax,dm.ApplyAfterTax");
            sb.append(" from DiscountProductMapping dpm ");
            sb.append(" inner join DiscountMaster dm on dm.DiscountId=dpm.DiscountId ");
            sb.append(" Left Join StandardListmaster SM on SM.Listid=dm.Typeid ");
            sb.append(" inner Join ProductMaster PM on PM.ParentHierarchy LIKE '%/'|| dpm.ProductId ||'/%' and PM.issalable =1 ");
            sb.append(" where dm.DiscountId in (select DiscountId from DiscountMapping ");
            sb.append(" where (Retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + " OR ");
            sb.append(" distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR ");
            sb.append(" Channelid=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR ");
            sb.append(" Channelid in(" + bmodel.channelMasterHelper.getChannelHierarchy(bmodel.getRetailerMasterBO().getSubchannelid(), mContext) + ") OR ");
            sb.append(" locationid in(" + bmodel.channelMasterHelper.getLocationHierarchy(mContext) + ") OR ");
            sb.append(" Accountid =" + bmodel.getRetailerMasterBO().getAccountid() + " AND Accountid != 0" + ") OR ");
            sb.append(" (Retailerid=0 AND distributorid=0 AND Channelid=0 AND locationid =0 AND Accountid =0))");
            sb.append(" and dm.moduleid=(select ListId from StandardListMaster where ListCode='INVOICE' and ListType = 'DISCOUNT_MODULE_TYPE') ");
            sb.append(" and dm.ApplyLevelid=(select ListId from StandardListMaster ");
            sb.append(" where ListCode='ITEM' and ListType='DISCOUNT_APPLY_TYPE') ");
            sb.append(" and dm.Typeid not in (select ListId from StandardListMaster where ListCode='GLDSTORE')");
            sb.append(" and dm.DiscountId not in (select DiscountId from DiscountMappingExclusion where CriteriaId =" + bmodel.getRetailerMasterBO().getRetailerID() + " and CriteriaType = 'RETAILER')");
            sb.append(" order by dm.DiscountId,dm.isCompanyGiven desc");
            ArrayList<StoreWiseDiscountBO> productdiscountList = new ArrayList<StoreWiseDiscountBO>();
            StoreWiseDiscountBO storeWiseDiscountBO;
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    int discountid = 0;
                    while (c.moveToNext()) {
                        storeWiseDiscountBO = new StoreWiseDiscountBO();
                        storeWiseDiscountBO.setDiscount(c.getDouble(0));
                        storeWiseDiscountBO.setIsPercentage(c.getInt(1));
                        storeWiseDiscountBO.setType(c.getInt(2));
                        storeWiseDiscountBO.setDescription(c.getString(3));
                        storeWiseDiscountBO.setApplyLevel(c.getInt(4));
                        storeWiseDiscountBO.setModule(c.getInt(5));
                        storeWiseDiscountBO.setDiscountId(c.getInt(6));
                        storeWiseDiscountBO.setProductId(c.getInt(7));
                        storeWiseDiscountBO.setIsCompanyGiven(c.getInt(8));
                        storeWiseDiscountBO.setComputeAfterTax(c.getInt(9));
                        storeWiseDiscountBO.setApplyAfterTax(c.getInt(10));

                        if (discountid != storeWiseDiscountBO.getDiscountId()) {
                            if (discountid != 0) {
                                mProductIdListByDiscoutId.put(discountid,
                                        productdiscountList);
                                productdiscountList = new ArrayList<StoreWiseDiscountBO>();
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
                        ProductMasterBO productMasterBO = getProductMasterBOById(storeWiseDiscountBO.getProductId() + "");
                        if (productMasterBO != null) {
                            productMasterBO.setIsDiscountable(1);
                        }

                    }
                    if (productdiscountList.size() > 0) {
                        mProductIdListByDiscoutId.put(discountid,
                                productdiscountList);
                        updateProductColor();
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

    public HashMap<Integer, ArrayList<StoreWiseDiscountBO>> getProductDiscountListByDiscountID() {
        return mProductIdListByDiscoutId;
    }


    public void saveItemLevelDiscount(String orderID, DBUtil db) {

        String columns = "OrderId,Pid,Typeid,Value,Percentage,ApplyLevelid,RetailerId,discountid,isCompanyGiven";
        if (mDiscountIdList != null) {
            StringBuffer sb = null;
            for (Integer discountid : mDiscountIdList) {
                ArrayList<StoreWiseDiscountBO> storewiseDiscountList = mProductIdListByDiscoutId
                        .get(discountid);
                if (storewiseDiscountList != null) {
                    for (StoreWiseDiscountBO storewiseDiscountBo : storewiseDiscountList) {
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


    public void updateInvoiceIdInDiscountTable(DBUtil db, String invid,
                                               String orderId) {

        String query = "update InvoiceDiscountDetail set InvoiceId=" + bmodel.QT(invid)
                + " where OrderId=" + orderId;
        db.updateSQL(query);

    }


    public int getMappingLocationId(int loclevelid, int Retlocid) {
        int locid = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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


    public void downloadDistributorProducts(String moduleCode) {
        productMasterById = new HashMap<>();
        downloadInStoreLocationsForStockCheck();
        try {


            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

            db.openDataBase();

            StringBuffer filter = downloadProductSequenceFromFilter();
            Commons.print("filter" + filter);

            String sql = "select A.pid, A.pcode,A.pname,A.parentid,A.sih, "
                    + "A.psname,A.barcode,srp1,Csrp1,Osrp1,A.msqqty, "
                    + "A.dUomQty,A.duomid,u.ListCode, "
                    + " dOuomQty,dOuomid,caseBarcode,outerBarcode,count(A.pid),piece_uomid,A.mrp, "
                    + " A.isSalable,A.isReturnable,A.isBom,A.TypeID,A.baseprice, '' as brandname,0,A.ParentHierarchy"
                    + " from ProductMaster A left join "
                    + "PriceMaster F on A.Pid = F.pid and F.scid = "
                    + bmodel.QT(bmodel.distributorMasterHelper.getDistributor().getGroupId())
                    + " LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') U ON A.dUOMId = U.ListId"
                    + " WHERE A.isSalable = 1 AND A.PLid IN"
                    + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + bmodel.QT(moduleCode)
                    + ")"
                    + " group by A.pid ORDER BY A.rowid";

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                productMaster = new Vector<>();
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductCode(c.getString(1));
                    product.setProductName(c.getString(2));
                    product.setParentid(c.getInt(3));
                    product.setSIH(c.getInt(4));
                    product.setDSIH(c.getInt(4));
                    product.setProductShortName(c.getString(5));
//                    product.setBarCode(c.getString(6));

                    product.setSrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(7), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setCsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(8), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                    product.setOsrp(SDUtil.convertToFloat(SDUtil.format(c.getFloat(9), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

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
                    product.setParentHierarchy(c.getString(28));
                    product.setLocations(cloneInStoreLocationList(locations));

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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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

            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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


    public ArrayList<StandardListBO> getTypeList(String type) {
        DBUtil db = null;
        ArrayList<StandardListBO> orderTypeList = null;

        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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
        if (competitorProductMaster != null) {
            competitorProductMaster.clear();
        }
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            int mFiltrtLevel = 0;
            int mContentLevel = 0;
            int loopEnd = 0;

            if (mCompetitorSequenceValues != null && mCompetitorSequenceValues.size() > 0) {
                mFiltrtLevel = mCompetitorSequenceValues.get(mCompetitorSequenceValues.size() - 1).getSequence();
            }
            if (bmodel.configurationMasterHelper.COMPETITOR_FILTER_LEVELS != null) {
                List<String> mLevels = Arrays.asList(bmodel.configurationMasterHelper.COMPETITOR_FILTER_LEVELS.split(","));

                if (mLevels.size() > 0) {
                    Cursor filterCur = db
                            .selectSQL("SELECT Distinct IFNULL(Sequence,0) FROM ProductLevel" +
                                    " where levelId = " + mLevels.get(mLevels.size() - 1));
                    if (filterCur != null) {
                        if (filterCur.moveToNext()) {
                            mContentLevel = filterCur.getInt(0);
                        }
                        filterCur.close();
                    }
                }

                loopEnd = mContentLevel - mFiltrtLevel + 1;
            }
            if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(mContext);
                productTaggingHelper.getAlCompetitorTaggedProducts(mContext,loopEnd);
            } else {

                Cursor cur = db
                        .selectSQL("SELECT CP.CPID, CP.CPName, PM.parentId,PM.duomid,PM.dOuomid,PM.piece_uomid,CPCode,PM.pid,CP.CompanyID,ifnull(CP.Barcode,''),PM.ParentHierarchy"
                                + " FROM CompetitorProductMaster CP"
                                + " INNER JOIN CompetitorMappingMaster CPM ON CPM.CPId = CP.CPID"
                                + " INNER JOIN ProductMaster PM ON PM.PID = CPM.PID AND PM.isSalable=1"
                                + " WHERE PM.PLid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode =" + QT(moduleCode) + ")" +
                                "group by CP.CPID");

                if (cur != null) {

                    while (cur.moveToNext()) {
                        ProductMasterBO product = new ProductMasterBO();
                        product.setProductID(cur.getString(0));
                        product.setProductName(cur.getString(1));
                        product.setProductShortName(cur.getString(1));
                        product.setParentid(cur.getInt(2));
                        product.setIsSaleable(1);
                        product.setBarCode(cur.getString(9));
                        product.setCasebarcode(cur.getString(9));
                        product.setOuterbarcode(cur.getString(9));
                        product.setOwn(0);
                        product.setCaseUomId(cur.getInt(3));
                        product.setOuUomid(cur.getInt(4));
                        product.setPcUomid(cur.getInt(5));
                        product.setProductCode(cur.getString(6));
                        product.setOwnPID(cur.getString(7));
                        product.setCompanyId(cur.getInt(8));
                        product.setParentHierarchy(cur.getString(10));

                        // for level skiping
                        ProductMasterBO ownprodbo = productMasterById.get(product.getOwnPID());
                        if (ownprodbo != null)
                            product.setParentid(ownprodbo.getParentid());
                        else
                            product.setParentid(0);

                        product.setLocations(cloneInStoreLocationList(locations));
                        for (int i = 0; i < locations.size(); i++) {
                            product.getLocations().get(i)
                                    .setNearexpiryDate(cloneDateList(nearExpiryDateList));
                        }
                    /*bmodel.productHelper.getTaggedProducts().add(product);
                    mTaggedProductById.put(product.getProductID(), product);*/
                        competitorProductMaster.add(product);

                    }
                    cur.close();

                }

            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }

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
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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
        if (mAttributeTypes == null)
            mAttributeTypes = new ArrayList<>();
        return mAttributeTypes;
    }


    protected double getTotalBillwiseDiscount() {
        double discountValue = 0;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
        sb.append(" and invoiceid=0 and upload!='X'");
        db.updateSQL(sb.toString());
    }

    public void updateSalesReturnInfoInProductObj(DBUtil db, String invoiceid, boolean isFromReport) {

        if (db == null) {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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


    public void downloadDiscountRange() {
        // HashMap<String,ProductMasterBO> lstRangesByProductId=null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("Select PM.PID, MinValue, MaxValue from DiscountProductMapping dpm "
                    + " inner Join ProductMaster PM on PM.ParentHierarchy LIKE '%/'|| dpm.ProductId ||'/%' and PM.issalable =1");
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
                                    batchProductBo.setProductLevelDiscountValue(batchProductBo.getProductLevelDiscountValue() + batchDiscountValue);
                                } else if (productMasterBO.getDA() > 0) {

                                    batchProductBo.setProductLevelDiscountValue(batchProductBo.getProductLevelDiscountValue() + batchDiscAmoutValue);

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

                productMasterBO.setProductLevelDiscountValue(productMasterBO.getProductLevelDiscountValue() + totalDiscValue);


            }


        }


    }

    public void insertBillWiseEntryDisc(DBUtil db, String uid) {
        String columns = "Orderid,pid,typeid,Value,Percentage,Applylevelid,Retailerid,DiscountId,isCompanyGiven";
        StringBuffer sb = new StringBuffer();
        sb.append(uid + "," + "0,0,");
        if (bmodel.configurationMasterHelper.discountType == 1) {
            sb.append(bmodel.getOrderHeaderBO().getBillLevelDiscountValue() + "," + bmodel.getOrderHeaderBO().getDiscount());
        } else if (bmodel.configurationMasterHelper.discountType == 2) {
            sb.append(bmodel.getOrderHeaderBO().getBillLevelDiscountValue() + ",0");
        }

        sb.append(",0," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + "," + bmodel.getOrderHeaderBO().getDiscountId() + "," + bmodel.getOrderHeaderBO().getIsCompanyGiven());
        db.insertSQL(DataMembers.tbl_InvoiceDiscountDetail, columns, sb.toString());
        db.insertSQL(DataMembers.tbl_OrderDiscountDetail, columns, sb.toString());

    }


    public void updateSchemeAndDiscAndTaxValue(DBUtil db, String invoiceid) {
        OrderHelper.getInstance(mContext).invoiceDiscount = 0 + "";

        double totDiscVaue = 0;
        double totSchemeAmountValue = 0;
        double totTaxValue = 0;
        double totPriceOffValue = 0;
        double totalInvoiceAmount = 0;
        StringBuffer sb = new StringBuffer();
        // sum of product discount , scheme amount and tax amount

        sb.append("select sum(SchemeAmount),sum(DiscountAmount),sum(TaxAmount),sum(priceoffvalue),sum(qty*rate) from invoicedetails ");
        sb.append(" where invoiceid=" + bmodel.QT(invoiceid));
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                totSchemeAmountValue = totSchemeAmountValue + c.getDouble(0);
                totDiscVaue = totDiscVaue + c.getDouble(1);
                totTaxValue = totTaxValue + c.getDouble(2);
                totPriceOffValue = totPriceOffValue + c.getDouble(3);
                totalInvoiceAmount = totalInvoiceAmount + c.getDouble(4);
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
                OrderHelper.getInstance(mContext).invoiceDiscount = billWiseDisc + "";
            }
        }
        sb = new StringBuffer();
        sb.append("update invoiceMaster set schemeAmount=" + totSchemeAmountValue);
        sb.append(",discount=" + totDiscVaue + ",taxAmount=" + totTaxValue + ",priceoffAmount=" + totPriceOffValue);
        sb.append(",invoiceAmount=" + totalInvoiceAmount);
        sb.append(" where invoiceno=" + bmodel.QT(invoiceid));
        db.updateSQL(sb.toString());

        c.close();
        OrderHelper.getInstance(mContext).invoiceDiscount = totDiscVaue + "";

    }

    public void updateBillWiseDiscountInObj(String invoiceid) {
        OrderHelper.getInstance(mContext).invoiceDiscount = 0 + "";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        sb.append("select sum(value) from InvoiceDiscountDetail");
        sb.append(" where pid=0  and invoiceid=" + bmodel.QT(invoiceid));
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                double billWiseDisc = c.getDouble(0);

                OrderHelper.getInstance(mContext).invoiceDiscount = billWiseDisc + "";
            }
        }
        c.close();
        db.closeDB();
    }

    public void updateEntryLevelDiscount(DBUtil db, String orderID, double distVal) {

        StringBuffer sb = new StringBuffer();
        // sum of product discount , scheme amount and tax amount
        double totDiscVaue = 0;


        sb.append("select orderid from OrderHeader");
        sb.append(" where orderid=" + orderID);
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                totDiscVaue = distVal;
            }
        }
        sb = new StringBuffer();
        sb.append("update orderheader set ");
        sb.append("discount=discount+" + SDUtil.convertToDouble(SDUtil.format(totDiscVaue, bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT, 0)));
        sb.append(" where orderid=" + orderID);
        db.updateSQL(sb.toString());

        c.close();

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
        bmodel.getOrderHeaderBO().setDiscount(totDiscVaue);
        c.close();


    }

    public void downloadDocketPricing() {

        StringBuffer sb = new StringBuffer();
        sb.append("select producid,price,AvailQty from ContractPricing ");
        sb.append("where retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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

            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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


    //add loyalty points
    public void downloadLoyaltyDescription(String retailerID) {
        try {

            loyaltyproductList = new Vector<LoyaltyBO>();
            LoyaltyBO loyalties;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql1 = "SELECT Distinct  IFNULL(LM.LoyaltyId,0),IFNULL(LM.Description,'Common'),LP.RetailerId,LP.Points,LP.PointsTypeID FROM LoyaltyPoints LP LEFT JOIN LoyaltyMaster LM ON LM.LoyaltyId = LP.LoyaltyId"
                    + " WHERE LP.RetailerId =" + retailerID;

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    loyalties = new LoyaltyBO();
                    loyalties.setLoyaltyId(c.getInt(0));
                    loyalties.setLoyaltyDescription(c.getString(1));
                    loyalties.setRetailerId(c.getInt(2));
                    loyalties.setGivenPoints(c.getInt(3));
                    loyalties.setPointTypeId(c.getInt(4));
                    loyaltyproductList.add(loyalties);
                }
                if (loyaltyproductList != null && loyaltyproductList.size() > 0) {
                    for (LoyaltyBO loyaltyBO : bmodel.productHelper.getProductloyalties()) {

                        ArrayList<LoyaltyBenifitsBO> clonedList = new ArrayList<LoyaltyBenifitsBO>(ltyBenifitsList.size());
                        for (LoyaltyBenifitsBO loyaltysBO : ltyBenifitsList) {

                            if (loyaltyBO.getPointTypeId() == loyaltysBO.getPointTypeId()) {
                                clonedList.add(new LoyaltyBenifitsBO(loyaltysBO));
                            }
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
                    ltyBenifits.setPointTypeId(c.getInt(4));
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
        if (getFilterProductsByLevelId() != null) {
            //if(getFilterProductsByLevelId().get(categoryLevelId)!=null){
            if (filterProductsByLevelId.get(filterProductLevels.get(0)) != null) {
                categoryExpandableList.addAll(filterProductsByLevelId.get(filterProductLevels.get(0)));//getFilterProductsByLevelId().get(categoryLevelId));
            }
            //if(getFilterProductsByLevelId().get(brandLevelId)!=null){
            if (filterProductsByLevelId.get(filterProductLevels.get(1)) != null) {
                parentChildMap.addAll(filterProductsByLevelId.get(filterProductLevels.get(1)));//getFilterProductsByLevelId().get(brandLevelId));
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

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );

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
            parentChildMap.addAll(getFilterProductsByLevelId().get(brandLevelId));
        }
    }

    private Vector<LevelBO> categoryExpandableList = new Vector<>();


    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "select orderid from orderheader where IndicativeOrderID ="
                    + indicativeOrderId + " and upload !='X' and invoicestatus=1";
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


    public LinkedList<String> getmProductidOrderByEntry() {
        return mProductidOrderByEntry;
    }

    public void setmProductidOrderByEntry(LinkedList<String> mProductidOrderByEntry) {
        this.mProductidOrderByEntry = mProductidOrderByEntry;
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
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


    public void updateOutletOrderedProducts(String rId) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "select ODR.ProductID,ODR.Qty,ODR.uomid,OHR.Remarks from OrderHeaderRequest OHR " +
                    "INNER JOIN OrderDetailRequest ODR ON OHR.OrderID=ODR.OrderID " +
                    "where OHR.RetailerID=" + QT(rId) + " AND OHR.upload='N'";
            Cursor c = db.selectSQL(query);
            String pdi;
            int qty;
            int uomid;
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        pdi = c.getString(0);
                        uomid = c.getInt(2);
                        qty = c.getInt(1);
                        bmodel.setOrderHeaderNote(c.getString(c.getColumnIndex("Remarks")));

                        if (bmodel.productHelper.getProductMasterBOById(pdi).getPcUomid() == uomid)
                            bmodel.productHelper.getProductMasterBOById(pdi).setOrderedPcsQty(qty);
                        else if (bmodel.productHelper.getProductMasterBOById(pdi).getCaseUomId() == uomid)
                            bmodel.productHelper.getProductMasterBOById(pdi).setOrderedCaseQty(qty);
                        else if (bmodel.productHelper.getProductMasterBOById(pdi).getCaseUomId() == uomid)
                            bmodel.productHelper.getProductMasterBOById(pdi).setOrderedOuterQty(qty);

                        //update ordered product details in edit mode
                        bmodel.newOutletHelper.getOrderedProductList()
                                .add(bmodel.productHelper.getProductMasterBOById(pdi));
                    }
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
        }


    }


    public boolean isSBDFilterAvaiable() {
        DBUtil db = null;
        boolean isAvailable = false;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "select HHTCode from HhtMenuMaster where HHTCode = 'Filt02' or 'Filt03'";
            Cursor c = db.selectSQL(query);

            if (c != null) {
                if (c.getCount() > 0)
                    isAvailable = true;

                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        db.closeDB();
        return isAvailable;
    }

    public Vector<ProductMasterBO> getCompetitorProductMaster() {
        if (competitorProductMaster == null)
            return new Vector<ProductMasterBO>();
        return competitorProductMaster;
    }




    public ArrayList<ConfigureBO> downloadOrderSummaryDialogFields(Context context) {
        ArrayList<ConfigureBO> list = new ArrayList<>();
        try {

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sqlQuery;

            if (!bmodel.configurationMasterHelper.IS_ATTRIBUTE_MENU)
                sqlQuery = "select HHTCode,MName,RField1,RField  from HhtMenuMaster where flag=1 and lower(MenuType)="
                        + bmodel.QT("ORDER_SUM_DLG").toLowerCase()
                        + " and lang=" + bmodel.QT(language) + " and AttributeId = 0 "
                        + " Order By MNumber";
            else
                sqlQuery = "select HHTCode,MName,RField1,RField  from HhtMenuMaster where flag=1 and lower(MenuType)="
                        + bmodel.QT("ORDER_SUM_DLG").toLowerCase()
                        + " and lang=" + bmodel.QT(language) + " and attributeId in (0, "
                        + bmodel.getRetailerAttributeList()
                        + ")"
                        + " Order By MNumber";


            Cursor cur = db
                    .selectSQL(sqlQuery);


            if (cur != null && cur.getCount() > 0) {
                ConfigureBO configureBO;
                while (cur.moveToNext()) {
                    configureBO = new ConfigureBO();
                    configureBO.setConfigCode(cur.getString(0));
                    configureBO.setMenuName(cur.getString(1));
                    configureBO.setMandatory(cur.getInt(2));
                    configureBO.setRField(cur.getString(3));
                    list.add(configureBO);
                }
                cur.close();
            }
        } catch (Exception ex) {
            Commons.printException(ex);
            return new ArrayList<>();
        }
        return list;
    }


    /**
     * If SAO Config enabled this method will be called this method will take
     * ProductId and compare with BomMaster and passes Product name
     *
     * @param productId product id
     * @return List of Product Short Name
     */
    public ArrayList<String> getSkuMixtureProductName(String productId) {

        ArrayList<String> mBpids = new ArrayList<>();
        ArrayList<String> productShortName = new ArrayList<>();

        if (bmodel.productHelper.getBomMaster() != null) {
            for (BomMasterBO bomMasterBO : bmodel.productHelper.getBomMaster()) {
                if (bomMasterBO.getPid().equalsIgnoreCase(productId))
                    for (BomBO bom : bomMasterBO.getBomBO()) {
                        mBpids.add(bom.getbPid());
                    }
            }
        }
        if (mBpids.size() > 0) {
            for (int i = 0; i < mBpids.size(); i++) {
                ProductMasterBO bo = bmodel.productHelper.getProductMasterBOById(mBpids.get(i));
                if (bo != null)
                    productShortName.add(bo.getProductShortName());
            }
            return productShortName;
        }
        return null;
    }

    public float getSalesReturnValue() {
        float total = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select ifnull(sum(returnvalue),0) from SalesReturnHeader where retailerid="
                            + QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and distributorid=" + bmodel.getRetailerMasterBO().getDistributorId());
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToNext();
                    total = c.getFloat(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return total;
    }


    public boolean isDrugOrder(LinkedList<ProductMasterBO> mOrderedProductList) {
        for (ProductMasterBO bo : mOrderedProductList) {
            if (bo.getIsDrug() == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isDLDateExpired() {

        String expiryDate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                bmodel.getRetailerMasterBO().getDLNoExpDate(), "yyyy/MM/dd");
        try {
            if (!DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equals(expiryDate))//this for checking today date since before method not woking for today date
                if (DateTimeUtils.convertStringToDateObject(
                        bmodel.getRetailerMasterBO().getDLNoExpDate(), "yyyy/MM/dd").before(new Date())) {
                    return true;
                }
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return false;
    }


    /**
     * Download UOM List from StandardListMaster.
     *
     * @return ArrayList of type StandardList BO.
     */
    private ArrayList<StandardListBO> downloadUomList() {
        DBUtil db = null;
        ArrayList<StandardListBO> uomList = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(("select listid,listname from standardlistmaster where listtype=" + bmodel.QT("PRODUCT_UOM")));
            if (c.getCount() > 0) {
                StandardListBO standardListBO;
                uomList = new ArrayList<StandardListBO>();
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    uomList.add(standardListBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return uomList;
    }

    /**
     * load product wise uom List
     *
     * @param list list
     * @return clone list
     */
    private ArrayList<StandardListBO> cloneUOMList(
            ArrayList<StandardListBO> list, ProductMasterBO productObj) {
        ArrayList<StandardListBO> clone = new ArrayList<StandardListBO>(list.size());
        for (StandardListBO item : list) {
            if (item.getListID().equals(productObj.getPcUomid() + "") ||
                    item.getListID().equals(productObj.getCaseUomId() + "") ||
                    item.getListID().equals(productObj.getOuUomid() + ""))
                clone.add(new StandardListBO(item));
        }
        return clone;
    }

    //to load data from free sih master to unload free qty separate in Van unlaod screen
    //Mansoor
    private void loadDataFromFreeSIHMaster(DBUtil db, String moduleCode) {

        String query = "SELECT PM.ParentId, PM.PID, PM.PName,"
                + " (select qty from StockProposalNorm PSQ  where uomid =PM.piece_uomid and PM.PID = PSQ.PID) as sugpcs,"
                + " PM.psname, PM.dUomQty,"
                + " PM.sih, PWHS.Qty, PM.IsAlloc, PM.mrp, PM.barcode, PM.RField1, PM.dOuomQty,"
                + " PM.isMust, PM.maxQty,(select qty from ProductStandardStockMaster PSM  where uomid =PM.piece_uomid and PM.PID = PSM.PID) as stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM.dUomId and PM.PID = PSM.PID) as stdcase,(select qty from ProductStandardStockMaster PSM where uomid =PM.dOuomid and PM.PID = PSM.PID) as stdouter, PM.dUomId, PM.dOuomid,"
                + " PM.baseprice, PM.piece_uomid, PM.PLid, PM.pCode, PM.msqQty, PM.issalable"
                + ",IFNULL(BM.batchNum,'') as batchNum,SIH.qty as qty,SIH.batchid as batchid"
                + " ,(select qty from StockProposalNorm PSQ  where uomid =PM.dUomId and PM.PID = PSQ.PID) as sugcs,"
                + " (select qty from StockProposalNorm PSQ  where uomid =PM.dOuomid and PM.PID = PSQ.PID) as sugou,PM.pCode as ProCode,"
                + "  PM.ParentHierarchy as ParentHierarchy "
                + " FROM ProductMaster PM"
                + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM.pid and PWHS.UomID=PM.piece_uomid and (PWHS.DistributorId=" + bmodel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                + " INNER JOIN FreeStockInHandMaster SIH ON SIH.pid=PM.PID"
                + "  LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid AND PM.pid=BM.pid)"
                + " WHERE PM.PLid IN"
                + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                + bmodel.QT(moduleCode) + ")";

        Cursor c = db.selectSQL(query);
        if (c != null) {
            while (c.moveToNext()) {
                LoadManagementBO bo = new LoadManagementBO();

                bo.setParentid(c.getInt(0));
                bo.setProductid(c.getInt(1));
                bo.setProductname(c.getString(2));
                bo.setProductCode(c.getString(c.getColumnIndex("ProCode")));
                bo.setSuggestqty(c.getInt(c.getColumnIndex("sugpcs")) +
                        (c.getInt(c.getColumnIndex("sugcs")) * c.getInt(5)) +
                        (c.getInt(c.getColumnIndex("sugou")) * c.getInt(12)));
                bo.setProductshortname(c.getString(4));
                bo.setCaseSize(c.getInt(5));
                bo.setSih(c.getInt(c.getColumnIndex("qty"))); //  for freeproduct sih is from product master not considered
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
                bo.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));
                bo.setBatchNo(c.getString(c.getColumnIndex("batchNum")));
                bo.setStocksih(c.getInt(c.getColumnIndex("qty")));
                bo.setBatchId(c.getString(c.getColumnIndex("batchid")));

                Vector<LoadManagementBO> list = new Vector<>();
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

                bo.setIsFree(1); // loaded free sih

                if (mLoadManagementBOByProductId.get(bo.getProductid()) == null)
                    mLoadManagementBOByProductId.put(bo.getProductid(), bo);

                productlist.add(bo);

            }

            c.close();
        }

    }



    public int getContentLevel(DBUtil db, String activityCode){
        int mContentLevelId = 0;
        // for hybrid seeler if pre seller need to take order at different level
        if (bmodel.configurationMasterHelper.IS_SWITCH_SELLER_CONFIG_LEVEL && bmodel.getRetailerMasterBO().getIsVansales() == 0 &&
                bmodel.configurationMasterHelper.switchConfigLevel > 0 && activityCode.equalsIgnoreCase("MENU_STK_ORD")) {
            mContentLevelId = bmodel.configurationMasterHelper.switchConfigLevel;
        } else {
            Cursor cur = db.selectSQL("SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = " + bmodel.QT(activityCode));
            if (cur != null) {
                if (cur.moveToNext()) {
                    mContentLevelId = cur.getInt(0);
                }
                cur.close();
            }
        }
        return mContentLevelId;
    }

    public int getContentLevel(Context mContext, String activityCode){
        int mContentLevelId = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        // for hybrid seeler if pre seller need to take order at different level
        if (bmodel.configurationMasterHelper.IS_SWITCH_SELLER_CONFIG_LEVEL && bmodel.getRetailerMasterBO().getIsVansales() == 0 &&
                bmodel.configurationMasterHelper.switchConfigLevel > 0 && activityCode.equalsIgnoreCase("MENU_STK_ORD")) {
            mContentLevelId = bmodel.configurationMasterHelper.switchConfigLevel;
        } else {
            Cursor cur = db.selectSQL("SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = " + bmodel.QT(activityCode));
            if (cur != null) {
                if (cur.moveToNext()) {
                    mContentLevelId = cur.getInt(0);
                }
                cur.close();
            }
        }
        db.closeDB();
        return mContentLevelId;
    }
}




