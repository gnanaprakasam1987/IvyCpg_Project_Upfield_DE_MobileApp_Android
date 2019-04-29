package com.ivy.cpg.view.order.scheme;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemaQPSAchHistoryBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SchemeProductBatchQty;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SchemeDetailsMasterHelper {

    private BusinessModel bModel;
    private static SchemeDetailsMasterHelper instance = null;

    private static final String TAG = "SchemeDetailsMasterHelper";

    /* SALES_VALUE is used for AmountType(SV) scheme apply */
    private static final String SALES_VALUE = "SV";
    /* QUANTITY_TYPE is used for Quantity Type(QTY) scheme apply */
    private static final String QUANTITY_TYPE = "QTY";

    private static final String ANY_LOGIC = "ANY"; // use ANY type scheme logic
    private static final String AND_LOGIC = "AND"; // use AND type scheme logic
    public static final String ONLY_LOGIC = "ONLY";// use ONLY type scheme

    private static final String SCHEME_AMOUNT = "SCH_AMT";
    private static final String SCHEME_PERCENTAGE = "SCH_PER";
    private static final String SCHEME_PRICE = "SCH_PR";
    private static final String SCHEME_FREE_PRODUCT = "SCH_FPRD";
    private static final String SCHEME_PERCENTAGE_BILL = "BPER";

    private static final String PROCESS_TYPE_MULTIPLE_TIME_FOR_REMAINING = "MTR";
    private static final String PROCESS_TYPE_ONE_TIME_WITH_PERCENTAGE = "OTPR";
    private static final String PROCESS_TYPE_MTS = "MTS";
    private static final String PROCESS_TYPE_PRORATA = "MSP";//(Emami Specific)


    //All applicable schemes
    private ArrayList<Integer> mParentIDList;
    //Slab ID list by scheme ID
    private HashMap<Integer, ArrayList<String>> mSchemeIDListByParentID;
    //All scheme object list
    private List<SchemeBO> mSchemeList;
    //Scheme object by its scheme ID
    private Map<String, SchemeBO> mSchemeById;
    //Buy product object by its scheme ID and product ID
    private HashMap<String, SchemeProductBO> mBuyProductBoBySchemeIdWithPid;


    //Lists used for showing all schemes applicable for the product
    private HashMap<String, ArrayList<Integer>> mParentIdListByProductId;
    private SparseArray<ArrayList<String>> mProductIdListByParentId;

    //To show scheme free products in free product selection dialog and product profile screen
    private HashMap<String, ArrayList<String>> mFreeGroupNameListBySchemeId;
    private HashMap<String, String> mFreeGroupTypeByFreeGroupName;

    //Accumulation scheme - Ordered product list by its scheme Id
    private HashMap<String, ArrayList<ProductMasterBO>> mSchemeHistoryListBySchemeId;
    //Already applied scheme - Buy product list by it scheme Id
    private SparseArray<ArrayList<String>> mProductIdListByAlreadyApplySchemeId;

    //Accumulation scheme free issues (or) Off invoice scheme list
    private ArrayList<SchemeBO> mOffInvoiceSchemeList;
    //Applied Off invoice scheme list
    private ArrayList<SchemeBO> mOffInvoiceAppliedSchemeList;


    private double balancePercent = 0;
    //key: (parentid+productid)  value: Total (qty or sales) value already used to apply scheme
    private HashMap<String, Integer> mAchieved_qty_or_salesValue_by_schemeId_nd_productid;

    // List of schemes applied for the current order
    private ArrayList<SchemeBO> mAppliedSchemeList;
    private Vector<ProductMasterBO> mOrderedProductList;
    private HashMap<String, ArrayList<ProductMasterBO>> mBatchListByProductId;
    private HashMap<String, ProductMasterBO> mOrderedProductBOById;
    private HashMap<String, ProductMasterBO> mProductMasterBOById;


    //Display Scheme
    private ArrayList<SchemeBO> mDisplaySchemeMasterList;
    private ArrayList<SchemeBO> mDisplaySchemeSlabs;
    private ArrayList<SchemeBO> mDisplaySchemeTrackingList;
    private HashMap<String, SchemaQPSAchHistoryBO> mSchemaQPSAchHistoryList;
    private ArrayList<Integer> schemeParentId;

    protected SchemeDetailsMasterHelper(Context context) {
        bModel = (BusinessModel) context.getApplicationContext();
    }

    public static SchemeDetailsMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeDetailsMasterHelper(context);
        }
        return instance;
    }


    private static final String CODE_SCHEME_ON = "SCH01";
    private static final String CODE_SCHEME_EDITABLE = "SCH02";
    private static final String CODE_SCHEME_SHOW_SCREEN = "SCH03";
    private static final String CODE_FOC_ACCUMULATION_VALIDATION = "SCH04";
    private static final String CODE_SCHEME_SLAB_ON = "SCH08";
    private static final String CODE_SCHEME_CHECK = "SCH09";
    private static final String CODE_UP_SELLING = "SCH05";
    private static final String CODE_CHECK_SCHEME_WITH_ASRP = "SCH10";
    private static final String CODE_SHOW_ALL_SCHEMES_ORDER = "SCH11";

    public boolean IS_SCHEME_ON;
    public boolean IS_SCHEME_EDITABLE;
    public boolean IS_SCHEME_SHOW_SCREEN;
    public boolean IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE;
    public boolean IS_SCHEME_SLAB_ON;
    public boolean IS_SCHEME_CHECK;
    public boolean IS_SCHEME_CHECK_DISABLED;
    public boolean IS_SCHEME_ON_MASTER;
    public boolean IS_SCHEME_SHOW_SCREEN_MASTER;
    public boolean IS_UP_SELLING;
    public boolean IS_SCHEME_QPS_TRACKING;
    private int UP_SELLING_PERCENTAGE = 70;
    private boolean IS_CHECK_SCHEME_WITH_ASRP;
    public boolean IS_SHOW_ALL_SCHEMES_ORDER;

    private boolean isBatchWiseProducts;


    /**
     * Method to load all scheme related methods
     */
    public void initializeScheme(Context mContext, int mUserId, boolean isBatchWiseProducts) {

        DBUtil db;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            loadSchemeConfigs(mContext);

            int distributorId = bModel.getAppDataProvider().getRetailMaster().getDistributorId();
            String retailerId = bModel.getAppDataProvider().getRetailMaster().getRetailerID();
            int channelId = bModel.getAppDataProvider().getRetailMaster().getSubchannelid();
            int locationId = bModel.getAppDataProvider().getRetailMaster().getLocationId();
            int accountId = bModel.getAppDataProvider().getRetailMaster().getAccountid();
            int priorityProductId = bModel.getAppDataProvider().getRetailMaster().getPrioriryProductId();

            //  loading valid scheme groups based on retailer attributes
            String mGroupIdList = downloadValidSchemeGroups(db, retailerId);
            // loading valid scheme id's based on with and with out mGroupList
            String validSchemeIds = getValidSchemeIds(db, distributorId, retailerId, channelId, locationId, accountId, priorityProductId, mUserId, mGroupIdList);

            if (IS_SCHEME_ON_MASTER) {

                this.isBatchWiseProducts = isBatchWiseProducts;

                //  for loading highest slab parent ids
                downloadSchemeParentDetails(db, validSchemeIds);
                //  load buy product list
                downloadBuySchemeDetails(db, validSchemeIds);
                //  update free product
                downloadFreeProducts(db);

                if (bModel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG || bModel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                    downloadParentIdListByProduct(db, validSchemeIds);
                }

                downloadPeriodWiseScheme(db, retailerId);
            } else {
                setIsScheme(mContext, validSchemeIds);
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.print("" + e);
        }


    }


    public void clearScheme() {
        /*mParentIDList=null;
        mSchemeIDListByParentID=null;
        mSchemeList=null;
        mSchemeById=null;
        mBuyProductBoBySchemeIdWithPid=null;
        mParentIdListByProductId=null;
        mProductIdListByParentId=null;
        mFreeGroupNameListBySchemeId=null;
        mFreeGroupTypeByFreeGroupName=null;
        mSchemeHistoryListBySchemeId=null;
        mProductIdListByAlreadyApplySchemeId=null;
        mOffInvoiceSchemeList=null;
        mOffInvoiceAppliedSchemeList=null;
        mAppliedSchemeList=null;*/
        instance = null;
    }


    /**
     * Load All Scheme Configurations
     */
    private void loadSchemeConfigs(Context mContext) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_ON)) {
                        IS_SCHEME_ON = true;
                        IS_SCHEME_ON_MASTER = true;
                        if (c.getInt(1) > 0) {
                            IS_SCHEME_QPS_TRACKING = true;
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_EDITABLE))
                        IS_SCHEME_EDITABLE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_SHOW_SCREEN)) {
                        IS_SCHEME_SHOW_SCREEN = true;
                        IS_SCHEME_SHOW_SCREEN_MASTER = true;
                    } else if (c.getString(0).equalsIgnoreCase(CODE_FOC_ACCUMULATION_VALIDATION))
                        IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_SLAB_ON))
                        IS_SCHEME_SLAB_ON = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_CHECK_SCHEME_WITH_ASRP))
                        IS_CHECK_SCHEME_WITH_ASRP = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_UP_SELLING)) {
                        IS_UP_SELLING = true;
                        if (c.getInt(1) > 0) {
                            UP_SELLING_PERCENTAGE = c.getInt(1);
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_CHECK_SCHEME_WITH_ASRP))
                        IS_CHECK_SCHEME_WITH_ASRP = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_ALL_SCHEMES_ORDER))
                        IS_SHOW_ALL_SCHEMES_ORDER = true;

                    if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_CHECK)) {
                        IS_SCHEME_CHECK = true;
                        if (c.getString(1) != null && c.getString(1).equals("1")) {
                            IS_SCHEME_CHECK_DISABLED = true;
                        }
                    }


                }
                c.close();
            }
            db.closeDB();


        } catch (Exception e) {
            Commons.printException("loadSchemeConfigs " + e);
        }
    }


    /**
     * Downloading valid scheme groups
     *
     * @param db         Database object
     * @param retailerId retailer Id
     * @return Valid scheme group Id list
     */
    public String downloadValidSchemeGroups(DBUtil db, String retailerId) {

        StringBuilder sb = new StringBuilder();
        StringBuilder mValidGrpIDs = new StringBuilder();
        ArrayList<String> retailerAttributes = bModel.getAttributeParentListForCurrentRetailer(retailerId);

        sb.append("select Distinct schemeid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SchemeAttributeMapping  SAM" +
                " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.AttributeId" +
                " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID order by schemeid,groupid");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            int lastSchemeId = 0, lastGroupId = 0;
            boolean isGroupSatisfied = false;
            while (c.moveToNext()) {
                if (retailerAttributes != null && retailerAttributes.contains(c.getString(3))) {

                    if (lastSchemeId != c.getInt(0) || lastGroupId != c.getInt(1)) {

                        if (isGroupSatisfied) {
                            if (!mValidGrpIDs.toString().contains(lastGroupId + "")) {

                                if (mValidGrpIDs.toString().equals("")) {
                                    mValidGrpIDs = new StringBuilder(lastGroupId + "");
                                } else {
                                    mValidGrpIDs.append(",").append(String.valueOf(lastGroupId));
                                }
                            }
                        }

                    }

                    isGroupSatisfied = isSchemeApplicable(db, c.getInt(0), c.getInt(1), c.getInt(3), retailerId);


                    lastSchemeId = c.getInt(0);
                    lastGroupId = c.getInt(1);


                }

            }
            if (isGroupSatisfied) {
                if (!mValidGrpIDs.toString().contains(lastGroupId + "")) {

                    if (mValidGrpIDs.toString().equals("")) {
                        mValidGrpIDs = new StringBuilder(lastGroupId + "");
                    } else {
                        mValidGrpIDs.append(",").append(String.valueOf(lastGroupId));
                    }
                }
            }

        }
        c.close();

        return mValidGrpIDs.toString();


    }


    /**
     * To check is current scheme applicable
     *
     * @param schemeId   Slab Id
     * @param groupId    group Id
     * @param parentId   Scheme ID
     * @param retailerId retailer ID
     * @return is Applicable or not
     */
    private boolean isSchemeApplicable(DBUtil db, int schemeId, int groupId, int parentId, String retailerId) {
        try {

            StringBuilder sb = new StringBuilder();

            sb.append("select Distinct schemeid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SchemeAttributeMapping  SAM" +
                    " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.AttributeId" +
                    " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID ");
            sb.append("where schemeid=" + schemeId + " and groupid=" + groupId + " and SAM.attributeid in(select RA.attributeid from RetailerAttribute RA" +
                    " inner join EntityAttributeMaster EA on EA.Attributeid = RA.Attributeid and EA.PArentid=" + parentId +
                    " where retailerid = " + retailerId + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                return true;
            }
            c.close();
        } catch (Exception ex) {

            Commons.printException(ex);
        }
        return false;
    }

    /**
     * Downloading valid scheme id's based on valid scheme groups
     *
     * @param db
     * @param distributorId
     * @param retailerId
     * @param channelId
     * @param locationId
     * @param accountId
     * @param priorityProductId
     * @param userId
     * @param mValidGrpIds
     * @return scheme id's
     */
    private String getValidSchemeIds(DBUtil db, int distributorId, String retailerId, int channelId, int locationId, int accountId, int priorityProductId, int userId, String mValidGrpIds) {
        StringBuilder schemeIds = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        String orderBy = "";
        if (IS_SCHEME_QPS_TRACKING) {
            orderBy = "ORDER BY CAST(SM.parentID as integer),CAST(SM.SchemeID as integer) ASC";
        } else {
            orderBy = "ORDER BY SM.IsCompanyCreated,SM.schemeID ASC";
        }
        try {
            sb.append("SELECT DISTINCT SM.parentid  FROM SchemeMaster SM LEFT JOIN " +
                    " schemeApplyCountMaster SAC ON SM.schemeid = SAC.schemeID ");

            sb.append(" and ((SAC.retailerid=0 OR SAC.retailerid=").append(retailerId).append(")");
            sb.append(" AND (SAC.userid=0 OR SAC.userid=").append(userId).append(")) ");

            sb.append("INNER JOIN SchemeCriteriaMapping SCM ON SCM.schemeid = SM.parentid ");

            sb.append("LEFT JOIN SchemeAttributeMapping OP ON OP.GroupId = SCM.GroupID AND OP.SchemeID = SCM.schemeid ");

            sb.append(" WHERE SCM.distributorid in(0,").append(distributorId).append(")");

            sb.append(" AND SCM.RetailerId in(0,").append(retailerId).append(") AND ")
                    .append(retailerId).append(" NOT IN (Select CriteriaId from SchemeMappingExclusion SME " +
                    "where SME.CriteriaType = 'RTR' and SME.SchemeId = SCM.schemeid and SME.GroupId = SCM.groupId )");

            sb.append(" AND SCM.channelid in(0,").append(channelId).append(") AND ")
                    .append(channelId).append(" NOT IN (Select CriteriaId from SchemeMappingExclusion SME " +
                    "where SME.CriteriaType = 'CHANNEL' and SME.SchemeId = SCM.schemeid and SME.GroupId = SCM.groupId )");

            sb.append(" AND SCM.locationid in(0,").append(locationId).append(")");
            sb.append(" AND SCM.accountid in(0,").append(accountId).append(")");
            sb.append(" AND SCM.PriorityProductId in(0,").append(priorityProductId).append(")");
            sb.append(" (AND (OP.GroupID IN(").append(mValidGrpIds).append(")").append(" OR OP.GroupID IS NULL) ");// if given scheme is not mapped for attribute wise  than IS NULL condition will work
            sb.append(" AND SAC.schemeApplyCOunt !=0 ").append(orderBy);

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (schemeIds.toString().equals("")) {
                        schemeIds = new StringBuilder(c.getString(0));
                    } else {
                        schemeIds.append(",").append(c.getString(0));
                    }
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return schemeIds.toString();
    }

    /**
     * Preparing scheme list by parentId..
     * List will be ordered based on total Buy quantity(highest slab on top).
     *
     * @param db             Database object
     * @param validSchemeIds Valid scheme IDs*
     */
    public void downloadSchemeParentDetails(DBUtil db, String validSchemeIds) {
        mSchemeIDListByParentID = new HashMap<>();
        mParentIDList = new ArrayList<>();

        String sb = "select SM.schemeid,SM.parentid,sum(case when uomid is 0 then sbm.buyqty when SBM.uomid" +
                " is PM.duomid then  sbm.buyQty*pm.duomQty  when SBM.uomid is PM.douomid then " +
                "sbm.buyQty*pm.douomqty else buyQty  end ) as totalQty" +
                " from schemebuymaster sbm   inner join productmaster PM  on sbm.productid =pm.pid" +
                " inner join schememaster SM on SM.schemeid=sbm.schemeid" +
                " where SM.parentid IN(" + validSchemeIds + ")" +
                " group by sm.schemeid,SM.parentid order by SM.parentid,totalQty desc";

        db.openDataBase();

        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            int parentId = 0;
            ArrayList<String> schemeIdList = new ArrayList<>();
            while (c.moveToNext()) {
                String schemeid = c.getString(0);

                if (parentId != c.getInt(1)) {
                    if (parentId != 0) {
                        mParentIDList.add(parentId);
                        mSchemeIDListByParentID.put(parentId, schemeIdList);
                        schemeIdList = new ArrayList<>();
                        schemeIdList.add(schemeid);
                        parentId = c.getInt(1);

                    } else {
                        schemeIdList.add(schemeid);
                        parentId = c.getInt(1);

                    }
                } else {
                    schemeIdList.add(schemeid);

                }

            }
            if (schemeIdList.size() > 0) {
                mSchemeIDListByParentID.put(parentId, schemeIdList);
                mParentIDList.add(parentId);
            }
        }
        c.close();
        if (IS_SCHEME_QPS_TRACKING) {
            loadParentSchemInfo(db, mParentIDList);
            loadQPSCumulativeAchHistory(db);
        }
    }

    public ArrayList<Integer> getParentIDList() {
        return mParentIDList;
    }

    public HashMap<Integer, ArrayList<String>> getSchemeIdListByParentID() {
        return mSchemeIDListByParentID;
    }


    /**
     * Downloading schemes with their BUY product details
     *
     * @param db             database object
     * @param validSchemeIds valid scheme Id's *
     */
    public void downloadBuySchemeDetails(DBUtil db, String validSchemeIds) {
        mSchemeById = new HashMap<>();
        mSchemeList = new ArrayList<>();
        mBuyProductBoBySchemeIdWithPid = new HashMap<>();

        SchemeBO schemeBO;
        SchemeProductBO schemeProductBo;
        StringBuilder sb = new StringBuilder();
        String orderBy = "";
        if (IS_SCHEME_QPS_TRACKING) {
            orderBy = " ORDER BY CAST(SM.parentID as integer),CAST(SM.SchemeID as integer) ASC";
        } else {
            orderBy = " ORDER BY SM.IsCompanyCreated,SM.schemeID ASC";
        }

        sb.append("SELECT distinct SM.SchemeID, SM.Description, SM.Type, SM.ShortName, BD.ProductID, ");
        sb.append("PM.Psname, PM.PName, BD.BuyQty,SM.parentid,SM.count,PM.pCode,SM.buyType,BD.GroupName,BD.GroupType,");
        sb.append("SM.IsCombination,BD.uomid,UM.ListName,BD.ToBuyQty,SM.IsBatch,BD.Batchid,PT.ListCode,SM.IsOnInvoice,");
        sb.append("SM.GetType, SM.IsAutoApply,SM.IsAccumulation as IsAccumulation,");
        sb.append(" ifNull(SM.FromDate,'') as fromDate,ifNull(SM.ToDate,'') as toDate,BD.VariantCount");

        sb.append(" FROM SchemeMaster SM INNER JOIN  SchemeBuyMaster BD ON BD.SchemeID = SM.SchemeID");
        sb.append(" LEFT JOIN ProductMaster PM ON BD.ProductID = PM.PID");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON BD.uomid = UM.ListId");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster) PT ON SM.processTypeId = PT.ListId");
        sb.append(" WHERE SM.parentid IN(").append(validSchemeIds).append(")");
        sb.append(orderBy);

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            String schemeID = "";
            // store buy products for particular scheme
            ArrayList<SchemeProductBO> buyProductList = new ArrayList<>();
            // schemeNewBO object used to store distinct scheme parent
            // object
            SchemeBO schemeNewBO = null;
            while (c.moveToNext()) {
                schemeBO = new SchemeBO();
                schemeBO.setSchemeId(c.getString(0));
                schemeBO.setSchemeDescription(c.getString(1));
                schemeBO.setParentLogic(c.getString(2));
                schemeBO.setSchemeParentName(c.getString(3));
                schemeBO.setSkuBuyProdID(c.getString(4));
                schemeBO.setSkuBuyProdName(c.getString(6));
                schemeBO.setQuantity(c.getInt(7));
                schemeBO.setParentId(c.getInt(8));
                schemeBO.setNoOfTimesApply(c.getInt(9));
                schemeBO.setBuyType(c.getString(11));
                schemeBO.setGroupName(c.getString(12));
                schemeBO.setGroupType(c.getString(13));
                schemeBO.setIsCombination(c.getInt(14));
                schemeBO.setIsAutoApply(c.getInt(c.getColumnIndex("IsAutoApply")));
                schemeBO.setFromDate(c.getString(25));
                schemeBO.setToDate(c.getString(26));
                schemeBO.setVariantCount(c.getInt(27));
                if (c.getInt(c.getColumnIndex("IsAccumulation")) == 1)
                    schemeBO.setAccumulationScheme(true);
                else schemeBO.setAccumulationScheme(false);

                if (c.getString(20) != null)
                    schemeBO.setProcessType(c.getString(20));
                else
                    schemeBO.setProcessType("");

                if (c.getInt(21) == 0)
                    schemeBO.setOffScheme(true);
                else
                    schemeBO.setOffScheme(false);

                if (c.getInt(18) == 1)
                    schemeBO.setBatchWise(true);

                schemeBO.setGetType(c.getString(c.getColumnIndex("GetType")));

                // store child wise scheme and logic

                schemeProductBo = new SchemeProductBO();
                schemeProductBo.setSchemeId(c.getString(0));
                schemeProductBo.setProductId(c.getString(4));
                schemeProductBo.setProductName(c.getString(5));
                schemeProductBo.setProductFullName(c.getString(6));
                schemeProductBo.setBuyQty(c.getDouble(7));
                schemeProductBo.setBuyType(c.getString(11));
                schemeProductBo.setGroupName(c.getString(12));
                schemeProductBo.setGroupBuyType(c.getString(13));
                schemeProductBo.setUomID(c.getInt(15));

                schemeProductBo.setUomDescription(c.getString(16));

                schemeProductBo.setTobuyQty(c.getDouble(17));
                schemeProductBo.setBatchId(c.getString(19));

                //updating Promo flag in product master list
                updatePROMOFlag(schemeProductBo.getProductId());


                mBuyProductBoBySchemeIdWithPid.put(schemeBO.getSchemeId() + schemeProductBo.getProductId(), schemeProductBo);

                if (!schemeID.equals(schemeBO.getSchemeId())) {
                    if (!schemeID.equals("")) {

                        mSchemeById.put(schemeID, schemeNewBO);
                        mSchemeList.add(schemeNewBO);
                        schemeNewBO.setBuyingProducts(buyProductList);
                        buyProductList = new ArrayList<>();

                        buyProductList.add(schemeProductBo);
                        schemeID = schemeBO.getSchemeId();
                        schemeNewBO = schemeBO;

                    } else {

                        buyProductList.add(schemeProductBo);
                        schemeID = schemeBO.getSchemeId();
                        schemeNewBO = schemeBO;

                    }
                } else {

                    buyProductList.add(schemeProductBo);
                }

            }

            if (buyProductList.size() > 0) {
                if (schemeNewBO != null) {
                    mSchemeById.put(schemeID, schemeNewBO);
                    schemeNewBO.setBuyingProducts(buyProductList);
                    mSchemeList.add(schemeNewBO);
                }
            }
        }
        c.close();


    }

    public HashMap<String, SchemeProductBO> getBuyProductBOBySchemeIdWithPid() {
        return mBuyProductBoBySchemeIdWithPid;
    }

    public List<SchemeBO> getSchemeList() {
        return mSchemeList;
    }

    public Map<String, SchemeBO> getSchemeById() {
        return mSchemeById;
    }

    /**
     * updating scheme object with the free product details
     *
     * @param db Database object
     */
    private void downloadFreeProducts(DBUtil db) {
        mFreeGroupTypeByFreeGroupName = new HashMap<>();
        mFreeGroupNameListBySchemeId = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct FD.SchemeID, SFP.productid, PM.psname, SFP.FreeQty, SFP.MaxQty, FD.Rate,");
        sb.append("FD.MaxRate,PM.PName,FD.amount,FD.maxAmount,FD.percent,");
        sb.append("FD.maxPercent,PM.pCode,SFP.uomid,SFP.GroupName,SFP.GroupType,FD.isFreeCombination,");
        sb.append("FD.Type,UM.ListName,FD.everyuomid,FD.everyQty, FD.SlabMaxValue FROM SchemeFreeMaster FD");

        sb.append(" LEFT JOIN SchemeFreeProducts as SFP on SFP.schemeid=FD.schemeid");
        sb.append(" LEFT JOIN ProductMaster PM ON SFP.ProductID = PM.PID");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON SFP.uomid = UM.ListId");

        sb.append(" WHERE  ((SFP.FreeQty>0 and SFP.MaxQty>0) OR SFP.productid ISNULL)");
        sb.append(" ORDER BY FD.SchemeID,SFP.GroupName");


        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            SchemeBO schemeBO;
            SchemeProductBO productBO;
            String schemeID;
            while (c.moveToNext()) {
                schemeID = c.getString(0);

                schemeBO = mSchemeById.get(schemeID);

                if (schemeBO != null) {
                    productBO = new SchemeProductBO();
                    productBO.setSchemeId(c.getString(0));
                    productBO.setProductId(c.getString(1));
                    productBO.setProductName(c.getString(2));
                    double minimumRate;
                    double maximumRate;
                    minimumRate = c.getDouble(5);
                    maximumRate = c.getDouble(6);
                    productBO.setQuantityMinimum(c.getInt(3));
                    productBO.setQuantityMaximum(c.getInt(4));
                    productBO.setPriceActual(minimumRate);
                    productBO.setPriceMaximum(maximumRate);
                    productBO.setProductFullName(c.getString(7));
                    productBO.setMinAmount(c.getDouble(8));
                    productBO.setMaxAmount(c.getDouble(9));
                    productBO.setMinPercent(c.getDouble(10));
                    productBO.setMaxPercent(c.getDouble(11));
                    productBO.setUomID(c.getInt(13));
                    productBO.setGroupName(c.getString(14));
                    productBO.setGroupBuyType(c.getString(15));
                    productBO.setUomDescription(c.getString(18));

                    //updating scheme object
                    schemeBO.setIsFreeCombination(c.getInt(16));
                    schemeBO.setFreeType(c.getString(17));
                    schemeBO.setEveryUomId(c.getInt(19));
                    schemeBO.setEveryQty(c.getInt(20));
                    schemeBO.setMaximumSlab(c.getInt(21));

                    //updating stock for free products
                    if (bModel.productHelper.getProductMasterBOById(productBO.getProductId()) != null) {
                        if (!bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
                            productBO.setStock(bModel.productHelper.getProductMasterBOById(productBO.getProductId()).getSIH());
                        else
                            productBO.setStock(bModel.productHelper.getProductMasterBOById(productBO.getProductId()).getFreeSIH());
                    }

                    schemeBO.getFreeProducts().add(productBO);


                    //Preparing list of groupName by its slab Id
                    if (productBO.getGroupName() != null) {
                        mFreeGroupTypeByFreeGroupName.put(
                                schemeID + productBO.getGroupName(),
                                productBO.getGroupLogic());

                        if (mFreeGroupNameListBySchemeId.get(productBO.getSchemeId()) != null) {
                            ArrayList<String> mGroupNames = mFreeGroupNameListBySchemeId.get(productBO.getSchemeId());
                            if (!mGroupNames.contains(productBO.getGroupName())) {
                                mGroupNames.add(productBO.getGroupName());
                            }
                            mFreeGroupNameListBySchemeId.put(productBO.getSchemeId(), mGroupNames);

                        } else {

                            ArrayList<String> mGroupNames = new ArrayList<>();
                            mGroupNames.add(productBO.getGroupName());
                            mFreeGroupNameListBySchemeId.put(productBO.getSchemeId(), mGroupNames);
                        }
                    }
                }
            }
        }

        c.close();


    }

    public HashMap<String, String> getGroupBuyTypeByGroupName() {
        return mFreeGroupTypeByFreeGroupName;
    }

    public HashMap<String, ArrayList<String>> getFreeGroupNameListBySchemeID() {
        return mFreeGroupNameListBySchemeId;
    }


    /**
     * Prepare product's scheme details to show in schemes in product profile screen
     *
     * @param db             Database object
     * @param validSchemeIds list of scheme id's allowed
     */
    private void downloadParentIdListByProduct(DBUtil db, String validSchemeIds) {

        String sb = "SELECT distinct SBM.productid,SM.parentid from SchemeBuyMaster SBM" +
                " INNER JOIN SchemeMaster SM on SM.Schemeid=SBM.Schemeid " +
                " WHERE SM.parentid IN(" + validSchemeIds + ")" +
                " AND SM.IsOnInvoice=1 order by SBM.Productid";

        Cursor c = db.selectSQL(sb);
        if (c.getCount() > 0) {
            mParentIdListByProductId = new HashMap<>();
            mProductIdListByParentId = new SparseArray<>();
            HashSet<Integer> schemeSet = new HashSet<>();
            String productId = "";
            ArrayList<Integer> parentIdList = new ArrayList<>();
            ArrayList<String> productIdList = new ArrayList<>();
            while (c.moveToNext()) {

                //Preparing parentId list by product Id
                if (!productId.equals(c.getString(0))) {
                    if (!productId.equals("")) {

                        mParentIdListByProductId.put(productId, parentIdList);
                        parentIdList = new ArrayList<>();
                        parentIdList.add(c.getInt(1));
                        productId = c.getString(0);

                    } else {
                        parentIdList.add(c.getInt(1));
                        productId = c.getString(0);

                    }
                } else {
                    parentIdList.add(c.getInt(1));

                }

                //Preparing product Id list by parent Id
                if (mProductIdListByParentId.get(c.getInt(1)) != null) {
                    mProductIdListByParentId.get(c.getInt(1), productIdList).add(c.getString(0));
                } else {
                    productIdList = new ArrayList<>();
                    productIdList.add(c.getString(0));
                    mProductIdListByParentId.put(c.getInt(1), productIdList);
                }

                schemeSet.add(c.getInt(1));

            }
            if (parentIdList.size() > 0) {
                mParentIdListByProductId.put(productId, parentIdList);
            }

            schemeParentId = new ArrayList<>(schemeSet);
        }
        c.close();
    }

    public HashMap<String, ArrayList<Integer>> getParentIdListByProductId() {
        if (mParentIdListByProductId == null)
            mParentIdListByProductId = new HashMap<>();
        return mParentIdListByProductId;
    }

    public SparseArray<ArrayList<String>> getProductIdListByParentId() {
        return mProductIdListByParentId;
    }


    /**
     * Method to get details of already applied scheme in previous order.
     * These schemes are not allowed to apply again until given period.
     *
     * @param db         Database object
     * @param retailerId Retailer Id
     */
    private void downloadPeriodWiseScheme(DBUtil db, String retailerId) {
        final String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_HYPHEN);
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SM.schemeid,SB.productid,");
        sb.append("(julianday(" + bModel.QT(currentDate) + ")-julianday(replace(date,'/','-') )) as daycount from Schememaster SM ");
        sb.append("inner join SchemePurchaseHistory SH on SM.parentid=SH.schemeid ");
        sb.append("inner join SchemeBuyMaster SB on SM.schemeid=SB.Schemeid ");
        sb.append("where (isapplied=1 AND SM.Days>=daycount)");
        sb.append("and SH.retailerid=" + bModel.QT(retailerId));
        sb.append(" order by SM.schemeid ");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            int schemeId = 0;
            mProductIdListByAlreadyApplySchemeId = new SparseArray<>();
            ArrayList<String> productIdList = new ArrayList<>();
            while (c.moveToNext()) {
                String productId = c.getString(1);

                if (schemeId != c.getInt(0)) {
                    if (schemeId != 0) {

                        mProductIdListByAlreadyApplySchemeId.put(schemeId, productIdList);
                        productIdList = new ArrayList<>();
                        productIdList.add(productId);
                        schemeId = c.getInt(0);

                    } else {
                        productIdList.add(productId);
                        schemeId = c.getInt(0);

                    }
                } else {
                    productIdList.add(productId);

                }
            }
            if (productIdList.size() > 0) {
                mProductIdListByAlreadyApplySchemeId.put(schemeId, productIdList);
            }

        }
    }


    private SparseArray<ArrayList<String>> getProductIdListByAlreadyAppliedSchemeId() {
        return mProductIdListByAlreadyApplySchemeId;
    }


    /**
     * Download accumulation schemes
     */
    public void downloadSchemeHistoryDetails(Context mContext, String retailerId, boolean isOrderEdit, String orderId) {
        ProductMasterBO productBO;
        mSchemeHistoryListBySchemeId = new HashMap<>();

        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "SELECT DISTINCT A.pid, A.batchid, A.schid,IFNULL(PieceUOM.Qty,0) AS PieceQty ,IFNULL(OuterUOM.Qty,0) as OouterQty,"
                    + " IFNULL(CaseUOM.Qty,0) as CaseQty,(IFNULL(PieceUOM.value,0)+IFNULL(OuterUOM.value,0)+IFNULL(CaseUOM.value,0)) "
                    + " FROM SchemeAchHistory A"

                    + " LEFT JOIN (SELECT pid, qty,value,schid from SchemeAchHistory where  uom='PIECE' and rid=" + bModel.QT(retailerId) + ") as PieceUOM ON PieceUOM.Pid = A.pid and PieceUOM.schid=A.schid"
                    + " LEFT JOIN (SELECT pid, qty,value,schid from SchemeAchHistory where  uom='MSQ' and rid=" + bModel.QT(retailerId) + ") as OuterUOM ON OuterUOM.Pid = A.pid and OuterUOM.schid=A.schid"
                    + " LEFT JOIN (SELECT pid, qty,value,schid from SchemeAchHistory where  uom='CASE' and rid=" + bModel.QT(retailerId) + ") as CaseUOM ON CaseUOM .Pid = A.pid and CaseUOM.schid=A.schid"
                    + " LEFT JOIN OrderHeader OH on OH.retailerid=" + bModel.QT(retailerId) + " and invoicestatus=1 and OH.upload!='X'"
                    + " LEFT JOIN SchemeDetail SD on SD.parentid=A.schid and OH.orderid=SD.orderid"
                    + " LEFT JOIN SchemeFreeProductDetail SPD on SPD.parentid=A.schid and OH.orderid=SPD.orderid"

                    + " where rid=" + bModel.QT(retailerId)
                    + " and A.schid!=IFNULL(SD.parentid,0) and A.schid!=IFNULL(SPD.parentid,0) order by A.schid";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                String schemeID = "";
                ArrayList<ProductMasterBO> schemeList = new ArrayList<>();
                while (c.moveToNext()) {

                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setBatchid(c.getString(1));

                    productBO.setOrderedCaseQty(c.getInt(5));
                    productBO.setOrderedPcsQty(c.getInt(3));
                    productBO.setOrderedOuterQty(c.getInt(4));
                    productBO.setTotalamount(c.getDouble(6));

                    if (!schemeID.equals(c.getString(2))) {
                        if (!schemeID.equals("")) {
                            mSchemeHistoryListBySchemeId.put(schemeID,
                                    schemeList);
                            schemeList = new ArrayList<>();
                            schemeList.add(productBO);
                            schemeID = c.getString(2);
                        } else {
                            schemeList.add(productBO);
                            schemeID = c.getString(2);
                        }
                    } else {
                        schemeList.add(productBO);
                    }

                }
                if (schemeList.size() > 0) {
                    mSchemeHistoryListBySchemeId.put(schemeID, schemeList);
                }


                c.close();

            }

            db.closeDB();

            if (mSchemeHistoryListBySchemeId.size() > 0)
                updateLocalOrderQty(mContext, retailerId, isOrderEdit, orderId);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Getting local order quantity for the given retailer.
     *
     * @param mContext   Current context
     * @param retailerId Retailer Id
     */
    private void updateLocalOrderQty(Context mContext, String retailerId, boolean isOrderEdit, String orderId) {

        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "select distinct ProductID,sum(pieceQty),sum(caseQty),sum(outerQty) from orderDetail where upload='N' and retailerId=" + retailerId + " ";
            if (isOrderEdit)
                query += " and OrderID not in (" + bModel.QT(orderId) + ") ";
            query += " group by  ProductId";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    ProductMasterBO productMasterBO = mProductMasterBOById.get(c.getString(0));
                    productMasterBO.setLocalOrderPieceqty(c.getInt(1));
                    productMasterBO.setLocalOrderCaseqty(c.getInt(2));
                    productMasterBO.setLocalOrderOuterQty(c.getInt(3));


                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }


    /**
     * Download accumulation scheme free issues
     * Validation - Particular scheme should not be in 'SchemeFreeProductDetail' table(To ensure that scheme is already not delivered)
     */
    public void downloadOffInvoiceSchemeDetails(Context mContext, String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            mOffInvoiceSchemeList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT distinct productid,Pname,uomid,Qty,ASF.Slabid,ASF.schemeid,ASF.SchemeDesc,ASF.groupName");
            sb.append(",ASF.groupType,ASF.schemeLogic,UM.ListName  from AccumulationSchemeFreeIssues ASF");
            sb.append(" inner join Productmaster PM on PM.pid=ASF.productid");
            sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON ASF.uomid = UM.ListId ");
            sb.append(" where ASF.retailerid=");
            sb.append(retailerId);
            sb.append(" and ASF.slabid not in(select schemeid from SchemeFreeProductDetail where retailerid=" + retailerId + ") order by ASF.schemeid");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {

                    ArrayList<SchemeProductBO> freeProductList = new ArrayList<>();
                    SchemeProductBO schemeProductBO;
                    int schemeId = 0;
                    int parentId = 0;
                    String schemeDesc = "";
                    String freeType = "";
                    SchemeBO schemeBO;

                    if (mFreeGroupTypeByFreeGroupName == null) {
                        mFreeGroupTypeByFreeGroupName = new HashMap<>();
                    }

                    while (c.moveToNext()) {
                        schemeProductBO = new SchemeProductBO();
                        schemeProductBO.setProductId(c.getString(0));
                        schemeProductBO.setProductName(c.getString(1));
                        schemeProductBO.setUomID(c.getInt(2));
                        schemeProductBO.setUomDescription(c.getString(10));
                        final int freeQty = c.getInt(3);
                        schemeProductBO.setQuantityMinimum(freeQty);
                        schemeProductBO.setQuantityMaximum(freeQty);
                        schemeProductBO.setQuantityActualCalculated(freeQty);
                        schemeProductBO.setQuantityMaxiumCalculated(freeQty);
                        schemeProductBO.setSchemeId(c.getInt(4) + "");
                        schemeProductBO.setAccProductParentId(c.getInt(5) + "");
                        schemeProductBO.setGroupName(c.getString(7));

                        schemeProductBO.setGroupBuyType(c.getString(8));
                        if (schemeId != c.getInt(4)) {
                            if (schemeId != 0) {
                                schemeBO = new SchemeBO();
                                schemeBO.setSchemeId(schemeId + "");
                                schemeBO.setParentId(parentId);
                                schemeBO.setFreeType(freeType);
                                schemeBO.setBuyType("SV");
                                schemeBO.setSchemeParentName(schemeDesc);
                                schemeBO.setSchemeDescription(schemeDesc);
                                mOffInvoiceSchemeList.add(schemeBO);


                                schemeBO.setFreeProducts(freeProductList);
                                freeProductList = new ArrayList<>();
                                freeProductList.add(schemeProductBO);
                                schemeId = c.getInt(4);
                                schemeDesc = c.getString(6);
                                freeType = c.getString(9);
                                parentId = c.getInt(5);
                            } else {
                                freeProductList.add(schemeProductBO);
                                schemeId = c.getInt(4);
                                parentId = c.getInt(5);
                                schemeDesc = c.getString(6);
                                freeType = c.getString(9);
                            }
                        } else {
                            freeProductList.add(schemeProductBO);
                        }


                        mFreeGroupTypeByFreeGroupName.put(c.getInt(4) + c.getString(7), c.getString(8));

                        //preparing free group name list by scheme id
                        if (mFreeGroupNameListBySchemeId.get(schemeProductBO.getSchemeId()) != null) {
                            ArrayList<String> mGroupNames = mFreeGroupNameListBySchemeId.get(schemeProductBO.getSchemeId());
                            if (!mGroupNames.contains(schemeProductBO.getGroupName())) {
                                mGroupNames.add(schemeProductBO.getGroupName());
                            }
                            mFreeGroupNameListBySchemeId.put(schemeProductBO.getSchemeId(), mGroupNames);

                        } else {
                            ArrayList<String> mGroupNames = new ArrayList<>();
                            mGroupNames.add(schemeProductBO.getGroupName());
                            mFreeGroupNameListBySchemeId.put(schemeProductBO.getSchemeId(), mGroupNames);
                        }
                        //


                    }
                    if (freeProductList.size() > 0) {

                        schemeBO = new SchemeBO();
                        schemeBO.setSchemeId(schemeId + "");
                        schemeBO.setParentId(parentId);
                        schemeBO.setFreeType(freeType);
                        schemeBO.setBuyType("SV");
                        schemeBO.setSchemeParentName(schemeDesc);
                        schemeBO.setSchemeDescription(schemeDesc);
                        mOffInvoiceSchemeList.add(schemeBO);
                        schemeBO.setFreeProducts(freeProductList);
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e + "");
        }
    }

    /**
     * Method to use set product whether scheme available or not if scheme
     * available for a product,'setIsscheme'==1,else 0
     */
    private void setIsScheme(Context mContext, String validSchemeIds) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            // clearing promo flag in product object
            clearPROMOFlag(db);

            /*
             * update scheme product setIsScheme is one condition - schemeCount
             * should not be equal zero in Scheme master table
             */
            String sb = "select  distinct(PID) from productMaster" +
                    " inner join SchemeBuyMaster SB on ProductMaster.pID=ProductID" +
                    " inner join schememaster SM  on SM. schemeID=SB.schemeID" +
                    " WHERE SM.parentid IN(" + validSchemeIds + ")" +
                    " AND SM.count !=0";

            Cursor schemeCursor = db.selectSQL(sb.toString());
            if (schemeCursor != null) {
                if (schemeCursor.getCount() > 0) {
                    while (schemeCursor.moveToNext()) {
                        updatePROMOFlag(schemeCursor.getString(0));
                    }
                }
                schemeCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Clearing PROMO flag of all products in the scheme buy master.
     *
     * @param db Database object
     */
    private void clearPROMOFlag(DBUtil db) {

        String sql = "select distinct(PID) from productMaster inner join SchemeBuyMaster on ProductMaster.pID=ProductID";
        Cursor c = db.selectSQL(sql);
        if (c != null) {
            while (c.moveToNext()) {

                ProductMasterBO prdBO = bModel.productHelper.getProductMasterBOById(c
                        .getString(0));
                if (prdBO != null) {
                    prdBO.setIsPromo(false);
                }

            }
            c.close();
        }
    }

    /**
     * Updating flag to denote the scheme availability for the product
     *
     * @param productId Product Id
     */
    private void updatePROMOFlag(String productId) {
        try {
            ProductMasterBO productMasterBO = bModel.productHelper.getProductMasterBOById(productId);
            if (productMasterBO != null) {
                productMasterBO.setIsPromo(true);

            } else {
                // In case of product mapped to parent level in the hierarchy
                for (ProductMasterBO productBO : bModel.productHelper.getProductMaster()) {
                    if (productBO.getProductID().equals(productId)
                            || productBO.getParentHierarchy().contains("/" + productId + "/")) {
                        productBO.setIsPromo(true);
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    /**
     * Download scheme report
     *
     * @param mContext context
     * @param id       Transaction Id
     * @param flag     Flag to identify invoice or order
     */
    public void downloadSchemeReport(Context mContext, String id, boolean flag) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct SchemeID ,ProductID ,Value  from SchemeDetail  where ");
            if (flag) { // invoice report
                sb.append("InvoiceID =" + bModel.QT(id));
            } else {// order report
                sb.append("OrderID =" + bModel.QT(id));
            }
            sb.append(" AND SchemeType = '" + SCHEME_PERCENTAGE + "'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    String productId = c.getString(1);
                    // productBo is buy product object
                    ProductMasterBO productBO = bModel.productHelper
                            .getProductMasterBOById(productId);
                    if (productBO != null) {
                        productBO.setMschemeper(c.getDouble(2));
                    }
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
        }

    }


///////////////////////////

    /**
     * Prepare necessary lists needed for applying scheme
     *
     * @param mProductMasterList Product master list
     */
    public void prepareNecessaryLists(Vector<ProductMasterBO> mProductMasterList) {
        int totalQuantity = 0;
        mOrderedProductList = new Vector<>();
        mOrderedProductBOById = new HashMap<>();
        mProductMasterBOById = new HashMap<>();

        for (ProductMasterBO productMasterBO : mProductMasterList) {

            if (isBatchWiseProducts) {
                ArrayList<ProductMasterBO> batchWiseList = bModel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());

                if (batchWiseList != null) {
                    for (ProductMasterBO batchProductBO : batchWiseList) {

                        if (batchProductBO.getOrderedPcsQty() > 0 || batchProductBO.getOrderedCaseQty() > 0 || batchProductBO.getOrderedOuterQty() > 0) {
                            totalQuantity += batchProductBO.getOrderedPcsQty()
                                    + (batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                    + (batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize());

                        }

                    }
                }
            } else {
                totalQuantity = productMasterBO.getOrderedPcsQty() + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                        + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());
            }

            if (totalQuantity > 0) {
                mOrderedProductList.add(productMasterBO);
                mOrderedProductBOById.put(productMasterBO.getProductID(), productMasterBO);
            }
            mProductMasterBOById.put(productMasterBO.getProductID(), productMasterBO);
        }
    }

    /**
     * Preparing a achieved scheme list based on the current order to show in the screen.
     * If Off Invoice is available than it will be added to the applied list directly to show in the screen
     * And updating stock availability for free products in the scheme object
     */
    public void schemeApply(Vector<ProductMasterBO> mProductMasterList) {

        prepareNecessaryLists(mProductMasterList);

        if (mOrderedProductList != null && mOrderedProductList.size() > 0) {

            mAchieved_qty_or_salesValue_by_schemeId_nd_productid = new HashMap<>();
            mAppliedSchemeList = new ArrayList<>();

            if (mParentIDList != null) {
                for (Integer parentID : mParentIDList) {

                    int slabPosition = 0;
                    ArrayList<String> schemeIDList = mSchemeIDListByParentID.get(parentID);

                    if (schemeIDList != null) {
                        for (String schemeID : schemeIDList) {

                            slabPosition += 1;
                            SchemeBO schemeBO = mSchemeById.get(schemeID);

                            if (schemeBO != null && !schemeBO.isOffScheme()) {
                                // only ON scheme will be allowed to apply

                                if (isSchemeDone(schemeBO, parentID, slabPosition == 1)) {
                                    schemeBO.setChecked(false);
                                    mAppliedSchemeList.add(schemeBO);

                                    //MTS-Allowed to next slab if scheme type is MTS
                                    //MSP(PRORATA)- Allowed to next slab if scheme not fell on highest slab.
                                    if ((!schemeBO.getProcessType().equals(PROCESS_TYPE_MTS) && !schemeBO.getProcessType().equals(PROCESS_TYPE_PRORATA))
                                            || (schemeBO.getProcessType().equals(PROCESS_TYPE_PRORATA) && slabPosition == 1)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Adding Off invoice schemes(Accumulation scheme free issues) to applied scheme list. So that it will be shown in scheme apply screen
        if (mOffInvoiceSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceSchemeList) {
                if (schemeBO != null) {
                    if (isSihAvailableForSchemeGroupFreeProducts(schemeBO, schemeBO.getSchemeId())) {
                        schemeBO.setIsOnInvoice(0);
                        schemeBO.setQuantityTypeSelected(true);
                        schemeBO.setApplyCount(1);
                        schemeBO.setIsFreeCombination(1);
                        schemeBO.setChecked(false);
                        mAppliedSchemeList.add(schemeBO);
                    }
                }

            }
        }

        // Updating stock availability for free products
        updateSIHAvailabilityForFreeProducts();


    }

    /**
     * Gives list of slabs that are near(greater than given percentage) based on current order
     *
     * @param mProductMasterList Product master list
     * @return Nearest slabs
     */
    public ArrayList<String> upSelling(Vector<ProductMasterBO> mProductMasterList) {
        ArrayList<String> nearestSchemes = new ArrayList<>();

        try {

            prepareNecessaryLists(mProductMasterList);


            mAchieved_qty_or_salesValue_by_schemeId_nd_productid = new HashMap<>();

            HashMap<String, Double> schemeIdByPercentage = new HashMap<>();

            if (mParentIDList != null) {
                for (Integer parentID : mParentIDList) {

                    ArrayList<String> schemeIDList = mSchemeIDListByParentID.get(parentID);

                    if (schemeIDList != null) {
                        for (String schemeID : schemeIDList) {


                            SchemeBO schemeBO = mSchemeById.get(schemeID);

                            if (schemeBO == null)
                                break;

                            if (!schemeBO.getParentLogic().equals(ONLY_LOGIC))
                                break;

                            if (!schemeBO.isOffScheme()) {
                                // only ON scheme will be allowed to apply

                                // Preparing group lists available for the scheme
                                ArrayList<String> mGroupNameList = new ArrayList<>();
                                HashMap<String, String> mGroupLogicTypeByGroupName = new HashMap<>();

                                for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {
                                    if (!mGroupNameList.contains(schemeProductBo.getGroupName())) {

                                        mGroupNameList.add(schemeProductBo.getGroupName());
                                        mGroupLogicTypeByGroupName.put(schemeProductBo.getGroupName(),
                                                schemeProductBo.getGroupLogic());
                                    }

                                }

                                // If current slab is achieved then no need to apply 'UPSelling' logic
                                if (isParentAndLogicDone(schemeBO, mGroupNameList, mGroupLogicTypeByGroupName, parentID))
                                    break;


                                // Applying UPSelling Logic and preparing list(slab Id by percentage).
                                for (String groupName : mGroupNameList) {
                                    String groupBuyType = mGroupLogicTypeByGroupName.get(groupName);
                                    if (groupBuyType.equals(AND_LOGIC) || groupBuyType.equals(ONLY_LOGIC)) {
                                        double totalProductPercentage = 0;

                                        for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {

                                            double toBuy = schemeProductBo.getBuyQty();
                                            double bought = 0;

                                            if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                                                bought += getTotalOrderedQuantity(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeProductBo.getUomID(), parentID, schemeBO.isAccumulationScheme());
                                            } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                                                bought += getTotalOrderedValue(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), parentID, schemeBO.isAccumulationScheme(), false);
                                            }


                                            totalProductPercentage += ((bought / toBuy) * 100);

                                        }

                                        double group_percentage = (totalProductPercentage / (schemeBO.getBuyingProducts().size() * 100)) * 100;
                                        schemeIdByPercentage.put(schemeBO.getSchemeId(), group_percentage);

                                    } else if (groupBuyType.equals(ANY_LOGIC)) {

                                        double total_bought = 0;
                                        double total_toBuy = 0;

                                        for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {

                                            total_toBuy = schemeProductBo.getBuyQty();

                                            if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                                                total_bought += getTotalOrderedQuantity(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeProductBo.getUomID(), parentID, schemeBO.isAccumulationScheme());
                                            } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                                                total_bought += getTotalOrderedValue(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), parentID, schemeBO.isAccumulationScheme(), false);
                                            }

                                        }

                                        double group_percentage = (total_bought / total_toBuy) * 100;
                                        schemeIdByPercentage.put(schemeBO.getSchemeId(), group_percentage);

                                    }
                                }


                            }

                        }
                    }
                }
            }

            //Adding slabs with more than given percentage
            for (String schemeId : schemeIdByPercentage.keySet()) {
                if (schemeIdByPercentage.get(schemeId) >= UP_SELLING_PERCENTAGE) {
                    nearestSchemes.add(schemeId);

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }


        return nearestSchemes;


    }


    public ArrayList<SchemeBO> getAppliedSchemeList() {
        if (mAppliedSchemeList != null) {
            return mAppliedSchemeList;
        }
        return new ArrayList<>();
    }

    /**
     * Checking whether current slab is achieved or not
     *
     * @param schemeBO      Current slab
     * @param parentID      Current Scheme Id
     * @param iSHighestSlab Is highest slab
     * @return Is Slab achieved
     */
    private boolean isSchemeDone(SchemeBO schemeBO, Integer parentID, boolean iSHighestSlab) {

        int mApplyCount = 0;

        // Preparing list of groups available for current slab
        ArrayList<String> mGroupNameList = new ArrayList<>();
        HashMap<String, String> mGroupLogicTypeByGroupName = new HashMap<>();

        for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {
            if (!mGroupNameList.contains(schemeProductBo.getGroupName())) {

                mGroupNameList.add(schemeProductBo.getGroupName());
                mGroupLogicTypeByGroupName.put(schemeProductBo.getGroupName(),
                        schemeProductBo.getGroupLogic());
            }

        }

        // Checking is slab is achieved or not..
        if (schemeBO.getBuyType() != null) {

            if (schemeBO.getParentLogic().equals(ANY_LOGIC)) {

                if (isParentAnyLogicDone(schemeBO, mGroupNameList, mGroupLogicTypeByGroupName, parentID)) {

                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        mApplyCount = getNumberOfTimesSlabApplied_ForAnyLogic(schemeBO.getBuyingProducts(), mGroupNameList, mGroupLogicTypeByGroupName,
                                true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab, schemeBO.isAccumulationScheme());

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        mApplyCount = getNumberOfTimesSlabApplied_ForAnyLogic(schemeBO.getBuyingProducts(), mGroupNameList, mGroupLogicTypeByGroupName,
                                false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab, schemeBO.isAccumulationScheme());

                    }

                    if (mApplyCount > 0) {
                        schemeBO.setApplyCount(mApplyCount);
                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());
                        calculateFreeForAchievedScheme(schemeBO, mApplyCount);
                    }

                    return true;


                }
            } else if (schemeBO.getParentLogic().equals(AND_LOGIC)
                    || schemeBO.getParentLogic().equals(ONLY_LOGIC)) {

                if (isParentAndLogicDone(schemeBO, mGroupNameList, mGroupLogicTypeByGroupName, parentID)) {

                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        mApplyCount = getNumberOfTimesSlabApplied_ForAndLogic(
                                schemeBO.getBuyingProducts(), mGroupNameList,
                                mGroupLogicTypeByGroupName, true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab, schemeBO.isAccumulationScheme());


                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                        mApplyCount = getNumberOfTimesSlabApplied_ForAndLogic(
                                schemeBO.getBuyingProducts(), mGroupNameList,
                                mGroupLogicTypeByGroupName, false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab, schemeBO.isAccumulationScheme());

                    }

                    if (mApplyCount > 0) {
                        schemeBO.setApplyCount(mApplyCount);
                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());
                        calculateFreeForAchievedScheme(schemeBO, mApplyCount);
                    }

                    return true;

                }

            }
        }

        return false;
    }


    /**
     * Checking is parent logic(ANY) achieved or not
     * Parent logic is ANY, So if any one of the group under this slab is achieved then this slab is considered as achieved
     *
     * @param schemeBO                  Current slab
     * @param groupSchemeName           list of groups available in current slab
     * @param groupLogicTypeByGroupName List with group logic type by its group name
     * @param mParentId                 Scheme Id
     * @return Is Slab achieved or not
     */
    private boolean isParentAnyLogicDone(SchemeBO schemeBO, ArrayList<String> groupSchemeName,
                                         HashMap<String, String> groupLogicTypeByGroupName, int mParentId) {

        for (String groupName : groupSchemeName) {
            String groupLogic = groupLogicTypeByGroupName.get(groupName);
            if (groupLogic.equals(AND_LOGIC) || groupLogic.equals(ONLY_LOGIC)) {
                if (isGroupAndLogicDone(schemeBO, groupName, mParentId))
                    return true;

            } else if (groupLogic.equals(ANY_LOGIC)) {
                if (isGroupAnyLogicDone(schemeBO, groupName, mParentId))
                    return true;

            }

        }

        return false;
    }

    /**
     * Checking is parent logic(AND) achieved or not
     * Parent logic is AND, So only if all of the groups under this slab is achieved then this slab is considered as achieved
     *
     * @param schemeBO                   Current slab
     * @param groupSchemeName            list of groups available in current slab
     * @param mGroupLogicTypeByGroupName List with group logic type by its group name
     * @param mParentId                  Scheme Id
     * @return Is Slab achieved or not
     */
    private boolean isParentAndLogicDone(SchemeBO schemeBO,
                                         ArrayList<String> groupSchemeName,
                                         HashMap<String, String> mGroupLogicTypeByGroupName, int mParentId) {

        for (String groupName : groupSchemeName) {
            String groupBuyType = mGroupLogicTypeByGroupName.get(groupName);
            if (groupBuyType.equals(AND_LOGIC) || groupBuyType.equals(ONLY_LOGIC)) {
                if (!isGroupAndLogicDone(schemeBO, groupName, mParentId)) {
                    return false;
                }
            } else if (groupBuyType.equals(ANY_LOGIC)) {
                if (!isGroupAnyLogicDone(schemeBO, groupName, mParentId)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checking is current group is achieved or not
     *
     * @param schemeBO  Current slab
     * @param groupName Current group name
     * @param mParentId Scheme Id
     * @return is Current group achieved or not
     */
    private boolean isGroupAndLogicDone(SchemeBO schemeBO, String groupName, int mParentId) {

        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();

        ArrayList<String> mAlreadyAppliedProducts = null;
        if (getProductIdListByAlreadyAppliedSchemeId() != null) {
            mAlreadyAppliedProducts = getProductIdListByAlreadyAppliedSchemeId().get(SDUtil.convertToInt(schemeBO.getSchemeId()));
        }

        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {

            if (mAlreadyAppliedProducts != null) {
                if (mAlreadyAppliedProducts.contains(schemeProductBo.getProductId())) {
                    return false;
                }
            }

            if (schemeProductBo.getGroupName().equals(groupName)) {


                if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                    int orderedTotalQuantityUomWise;

                    orderedTotalQuantityUomWise = getTotalOrderedQuantity(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeProductBo.getUomID(), schemeBO.getParentId(), schemeBO.isAccumulationScheme());


                    //Just reducing quantity which is used already for applying scheme.
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                        int totalAppliedQty = SDUtil.convertToInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()).toString());
                        orderedTotalQuantityUomWise = orderedTotalQuantityUomWise - totalAppliedQty;

                    }

                    if (schemeProductBo.getBuyQty() > orderedTotalQuantityUomWise) {
                        return false;
                    }

                } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                    double totalValue;
                    totalValue = getTotalOrderedValue(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeBO.getParentId(), schemeBO.isAccumulationScheme(), IS_CHECK_SCHEME_WITH_ASRP);

                    //Just reducing value which is used already for applying scheme.
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                        int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId());
                        totalValue = totalValue - totalAppliedQty;

                    }

                    if (schemeProductBo.getBuyQty() > totalValue) {
                        return false;
                    }
                }

            }

        }
        return true;
    }

    /**
     * Checking is current group(ANY) is achieved or not
     *
     * @param schemeBO  Current slab
     * @param groupName Current group name
     * @param mParentId Scheme Id
     * @return is Current group achieved or not
     */
    private boolean isGroupAnyLogicDone(SchemeBO schemeBO, String groupName, int mParentId) {

        int totalQty = 0;
        double totalValue = 0;
        // for Minimum Product count to be ordered -Mansoor
        int count = 0;

        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();

        ArrayList<String> mAlreadyAppliedProductList = null;
        if (getProductIdListByAlreadyAppliedSchemeId() != null) {
            mAlreadyAppliedProductList = getProductIdListByAlreadyAppliedSchemeId().get(SDUtil.convertToInt(schemeBO.getSchemeId()));
        }

        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {
            // already scheme applied in previous days
            if (mAlreadyAppliedProductList != null && mAlreadyAppliedProductList.contains(schemeProductBo.getProductId())) {
                break;
            }

            if (schemeProductBo.getGroupName().equals(groupName)) {

                if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                    int orderedTotalQuantityByUOMWise;

                    orderedTotalQuantityByUOMWise = getTotalOrderedQuantity(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeProductBo.getUomID(), schemeBO.getParentId(), schemeBO.isAccumulationScheme());


                    if (schemeBO.getVariantCount() > 0) {
                        if (orderedTotalQuantityByUOMWise > 0)
                            count++;
                    }
                    //Just reducing quantity which is used already for applying scheme.
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                        int totalAppliedQty = SDUtil.convertToInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()).toString());
                        orderedTotalQuantityByUOMWise = orderedTotalQuantityByUOMWise - totalAppliedQty;
                    }

                    totalQty += orderedTotalQuantityByUOMWise;

                    if (schemeBO.getVariantCount() > 0) {
                        if (count >= schemeBO.getVariantCount())
                            if (schemeProductBo.getBuyQty() <= totalQty) {
                                return true;
                            }
                    } else if (schemeProductBo.getBuyQty() <= totalQty) {
                        return true;
                    }

                } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                    double totalProductValue;
                    totalProductValue = getTotalOrderedValue(schemeProductBo.getProductId(), schemeBO.isBatchWise(), schemeProductBo.getBatchId(), schemeBO.getParentId(), schemeBO.isAccumulationScheme(), IS_CHECK_SCHEME_WITH_ASRP);

                    if (schemeBO.getVariantCount() > 0) {
                        if (totalProductValue > 0)
                            count++;
                    }
                    //Just reducing value which is used already for applying scheme.
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                        int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId());
                        totalProductValue = totalProductValue - totalAppliedQty;
                    }


                    totalValue += totalProductValue;

                    if (schemeBO.getVariantCount() > 0) {
                        if (count >= schemeBO.getVariantCount())
                            if (schemeProductBo.getBuyQty() <= totalValue) {
                                return true;
                            }
                    } else if (schemeProductBo.getBuyQty() <= totalValue) {
                        return true;
                    }
                }


            }
        }

        return false;

    }


    /**
     * Getting number of times current slab(Parent logic - AND) is achieved
     *
     * @param schemeProductList             Buy products for current slab
     * @param schemeGroupName               List of groups available under the given slab
     * @param schemeGroupBuyTypeByGroupName List with group logic by its group name
     * @param isQuantityType                Buy type is quantity or sales value
     * @param isBatchWise                   Is batch wise products available
     * @param parentId                      Scheme Id
     * @param processType                   Scheme process type
     * @param isHighestSlab                 Is current slab is highest slab or not
     * @param isAccumulationScheme          Is accumulation scheme or not
     * @return Number of times current slab is achieved
     */
    private int getNumberOfTimesSlabApplied_ForAndLogic(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeByGroupName,
            boolean isQuantityType, boolean isBatchWise, int parentId, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {

        int tempCount = 0;
        double tempBalancePercent = 0;
        for (String groupName : schemeGroupName) {
            String type = schemeGroupBuyTypeByGroupName.get(groupName);
            if (type.equals(AND_LOGIC) || type.equals(ONLY_LOGIC)) {

                int count;
                if (isQuantityType) {
                    count = getAndLogicAppliedCountForQuantity(schemeProductList, groupName, isBatchWise, parentId, processType, isHighestSlab, isAccumulationScheme);
                } else {
                    count = getAndLogicAppliedCountForSalesValue(schemeProductList, groupName, isBatchWise, parentId, processType, isHighestSlab, isAccumulationScheme);
                }

                //Getting lowest value as it is a AND/ONLY logic
                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            } else if (type.equals(ANY_LOGIC)) {
                int count;
                if (isQuantityType) {
                    count = getANYLogicAppliedCountForQuantity(schemeProductList, groupName, isBatchWise, parentId, processType, isHighestSlab, isAccumulationScheme);
                } else {
                    count = getAnyLogicAppliedCountForSalesValue(schemeProductList, groupName, isBatchWise, parentId, processType, isHighestSlab, isAccumulationScheme);
                }

                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            }


        }


        setBalancePercent(tempBalancePercent);

        if (tempCount == 0) tempCount = 1;

        return tempCount;
    }

    /**
     * Getting number of times current slab(Parent logic - ANY) is achieved
     *
     * @param schemeProductList             Buy products for current slab
     * @param schemeGroupName               List of groups available under the given slab
     * @param schemeGroupBuyTypeByGroupName List with group logic by its group name
     * @param isQuantityType                Buy type is quantity or sales value
     * @param isBatchWise                   Is batch wise products available
     * @param parentID                      Scheme Id
     * @param processType                   Scheme process type
     * @param isHighestSlab                 Is current slab is highest slab or not
     * @param isAccumulationScheme          Is accumulation scheme or not
     * @return Number of times current slab is achieved
     */
    private int getNumberOfTimesSlabApplied_ForAnyLogic(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeByGroupName,
            boolean isQuantityType, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {

        int mAppliedCount = 1;
        double balancePercent = 0;

        for (String s : schemeGroupName) {
            String type = schemeGroupBuyTypeByGroupName.get(s);

            if (type.equals(AND_LOGIC) || type.equals(ONLY_LOGIC)) {
                int count;

                if (isQuantityType) {
                    count = getAndLogicAppliedCountForQuantity(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab, isAccumulationScheme);
                } else {
                    count = getAndLogicAppliedCountForSalesValue(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab, isAccumulationScheme);
                }

                //Getting highest value as its parent is ANY logic
                if (mAppliedCount < count) {
                    mAppliedCount = count;

                }
                //
                if (balancePercent < getBalancePercent()) {
                    balancePercent = getBalancePercent();

                }

            } else if (type.equals(ANY_LOGIC)) {
                int count;

                if (isQuantityType) {
                    count = getANYLogicAppliedCountForQuantity(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab, isAccumulationScheme);
                } else {
                    count = getAnyLogicAppliedCountForSalesValue(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab, isAccumulationScheme);
                }

                //Getting highest value as its parent is ANY logic
                if (mAppliedCount < count) {
                    mAppliedCount = count;
                }
                //
                if (balancePercent < getBalancePercent()) {
                    balancePercent = getBalancePercent();

                }

            }
        }


        setBalancePercent(balancePercent);

        return mAppliedCount;
    }


    /**
     * Getting number of times current group(AND) is achieved. Buy Type- Quantity
     *
     * @param schemeProductList Current slabs buy product list
     * @param groupName         current group name
     * @param isBatchWise       Is batch wise products available
     * @param parentID          Scheme Id
     * @param processType       Scheme Process type
     * @param isHighestSlab     Is highest slab
     * @return Total number of times current group achieved
     */
    private int getAndLogicAppliedCountForQuantity(List<SchemeProductBO> schemeProductList,
                                                   String groupName, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {
        int tempCount = 0;
        double balancePercent = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                int count = 0;

                int quantity;
                quantity = getTotalOrderedQuantity(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), schemeProductBO.getUomID(), parentID, isAccumulationScheme);

                //Removing already used quantity if any
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) != null) {
                        quantity = quantity - SDUtil.convertToInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()).toString());
                    }
                }


                if (quantity > 0) {

                    int balanceQty;
                    count = (quantity / (int) schemeProductBO.getTobuyQty());
                    balanceQty = (quantity % (int) schemeProductBO.getTobuyQty());

                    //updating used quantity for applying scheme in the list
                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                            !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + schemeProductBO.getProductId())) {
                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()), quantity);
                    } else {
                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()), (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) + quantity));
                    }

                    if (balanceQty >= schemeProductBO.getBuyQty()) {
                        count = count + 1;
                    } else {
                        //
                        double tempBalPercent = 0;
                        if (schemeProductBO.getBuyQty() > 0)
                            tempBalPercent = ((balanceQty / schemeProductBO.getBuyQty()) * 100);

                        if (balancePercent < tempBalPercent)
                            balancePercent = tempBalPercent;
                    }

                }


                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

            }
        }
        //
        setBalancePercent(balancePercent);

        if (tempCount > 0) {
            return tempCount;
        } else {
            /*
             * Now we are apply range wise scheme.so maximum buy qty only using
             * for computation to how many times scheme achieved. but minimum buy
             * qty used to apply scheme or not.
             */
            return 1;
        }

    }


    /**
     * Getting number of times current group(AND) is achieved. Buy Type- Sales value
     *
     * @param schemeProductList    Current slabs buy product list
     * @param groupName            current group name
     * @param isBatchWise          Is batch wise products available
     * @param parentId             Scheme Id
     * @param processType          Scheme Process type
     * @param isHighestSlab        Is highest slab
     * @param isAccumulationScheme Is accumulation scheme or not
     * @return Total number of times current group achieved
     */
    private int getAndLogicAppliedCountForSalesValue(
            List<SchemeProductBO> schemeProductList, String groupName
            , boolean isBatchWise, int parentId, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {

        int tempCount = 0;
        double tempBalancePercent;
        double tempBal;
        double balancePercent = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                int count = 0;


                double totalValue;
                totalValue = getTotalOrderedValue(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), parentId, isAccumulationScheme, IS_CHECK_SCHEME_WITH_ASRP);

                if (totalValue > 0) {

                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + schemeProductBO.getProductId()) != null) {
                            totalValue = totalValue - SDUtil.convertToInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + schemeProductBO.getProductId()).toString());
                        }
                    }

                    if (totalValue > 0) {
                        double balanceValue;
                        if (schemeProductBO.getTobuyQty() > 0
                                && schemeProductBO.getBuyQty() > 0) {

                            count = (int) totalValue / (int) schemeProductBO.getTobuyQty();
                            balanceValue = totalValue % schemeProductBO.getTobuyQty();
                        } else {
                            balanceValue = totalValue;

                        }

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentId + schemeProductBO.getProductId())) {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentId + schemeProductBO.getProductId()), (int) (totalValue - balanceValue));
                        } else {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentId + schemeProductBO.getProductId()),
                                    (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + schemeProductBO.getProductId()) + ((int) (totalValue - balanceValue))));
                        }

                        if (schemeProductBO.getBuyQty() > 0) {
                            count = count + (int) balanceValue
                                    / (int) schemeProductBO.getBuyQty();

                            tempBal = balanceValue % schemeProductBO.getBuyQty();
                            tempBalancePercent = ((tempBal / schemeProductBO.getBuyQty()) * 100);

                            if (tempBalancePercent < balancePercent)
                                balancePercent = tempBalancePercent;
                        }


                    }
                }


                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

            }
        }
        setBalancePercent(balancePercent);

        if (tempCount > 0) {
            return tempCount;
        } else {
            /*
             * Now we are apply range wise scheme.so maximum buy qty only using
             * for computation to how many times scheme achieved. but minimum buy
             * qty used to apply scheme or not.
             */
            return 1;
        }

    }

    /**
     * Getting number of times current group(ANY) is achieved. Buy Type- Quantity
     *
     * @param schemeProductList Current slabs buy product list
     * @param groupName         current group name
     * @param isBatchWise       Is batch wise products available
     * @param parentID          Scheme Id
     * @param processType       Scheme Process type
     * @param isHighestSlab     Is highest slab
     * @return Total number of times current group achieved
     */
    private int getANYLogicAppliedCountForQuantity(List<SchemeProductBO> schemeProductList,
                                                   String groupName, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {
        int count = 0;
        double tempBalancePercent = 0;
        double minimumBuyQuantity = 0;
        double maximumBuyQuantity = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                maximumBuyQuantity = schemeProductBO.getTobuyQty();
                minimumBuyQuantity = schemeProductBO.getBuyQty();

                int quantity;
                quantity = getTotalOrderedQuantity(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), schemeProductBO.getUomID(), parentID, isAccumulationScheme);

                if (quantity > 0) {

                    //Quantity used(if previous slab applied) for scheme are reduced here.
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) != null) {
                            quantity = quantity - SDUtil.convertToInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()).toString());
                        }
                    }


                    count = count + quantity;
                }

            }

        }

        if (count != 0) {
            if (minimumBuyQuantity != 0 && maximumBuyQuantity != 0) {
                int balanceCount = 0;

                if (count > maximumBuyQuantity) {
                    balanceCount = count % (int) maximumBuyQuantity;
                    count = count / (int) maximumBuyQuantity;

                    if (balanceCount >= minimumBuyQuantity) {
                        balanceCount = 1;
                    } else {
                        if (minimumBuyQuantity > 0)
                            tempBalancePercent = ((balanceCount / minimumBuyQuantity) * 100);
                        balanceCount = 0;
                    }
                } else {
                    if (count >= minimumBuyQuantity)
                        count = 1;
                    else count = 0;
                }

                //list to maintain used(if current slab applied) quantity,scheme wise..
                // updating hashMap, qty used to apply scheme
                // Any Logic- So considered all products
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {

                    int orderedQuantity;
                    double tempToQty;
                    int appliedQuantity;

                    tempToQty = maximumBuyQuantity * count;

                    for (SchemeProductBO schemeProductBO : schemeProductList) {

                        orderedQuantity = getTotalOrderedQuantity(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), schemeProductBO.getUomID(), parentID, isAccumulationScheme);

                        if (orderedQuantity > 0) {
                            appliedQuantity = orderedQuantity;

                            if (tempToQty > appliedQuantity)
                                tempToQty = tempToQty - appliedQuantity;
                            else {
                                appliedQuantity = (int) tempToQty;
                                tempToQty = 0;
                            }

                            if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                    !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + schemeProductBO.getProductId())) {
                                mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()), appliedQuantity);
                            } else {
                                mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()),
                                        (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) + appliedQuantity));
                            }


                        }


                    }
                }


                //
                setBalancePercent(tempBalancePercent);

                // For PRORATA balance percentage is not needed if Highest slap is not achieved.
                if (!isHighestSlab && processType.equals(PROCESS_TYPE_PRORATA))
                    setBalancePercent(0);

                count = count + balanceCount;
                if (count != 0) {
                    return count;
                } else {
                    return 1;
                }
            }

        }


        return 1;
    }

    /**
     * Getting number of times current group(ANY) is achieved. Buy Type- Sales value
     *
     * @param schemeProductList    Current slabs buy product list
     * @param groupName            current group name
     * @param isBatchWise          Is batch wise products available
     * @param parentID             Scheme Id
     * @param processType          Scheme Process type
     * @param isHighestSlab        Is highest slab
     * @param isAccumulationScheme Is accumulation scheme or not
     * @return Total number of times current group achieved
     */
    private int getAnyLogicAppliedCountForSalesValue(List<SchemeProductBO> schemeProductList, String groupName,
                                                     boolean isBatchWise, int parentID, String processType, boolean isHighestSlab, boolean isAccumulationScheme) {

        double totalValue = 0;
        double minimumBuyValue = 0;
        double maximumBuyValue = 0;
        double balancePercent = 0;
        double appliedSchemeValue;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                minimumBuyValue = schemeProductBO.getBuyQty();
                maximumBuyValue = schemeProductBO.getTobuyQty();

                double value;
                value = getTotalOrderedValue(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), parentID, isAccumulationScheme, IS_CHECK_SCHEME_WITH_ASRP);

                if (value > 0) {

                    //Quantity used(if previous slab applied) for scheme are reduced here.
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) != null) {
                            value = value - mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId());
                        }
                    }


                    totalValue = totalValue + value;
                }

            }

        }

        if (totalValue != 0) {
            if (minimumBuyValue > 0 && maximumBuyValue > 0) {

                int balanceCount = (int) totalValue % (int) maximumBuyValue;
                appliedSchemeValue = totalValue - balanceCount;
                int count = (int) totalValue / (int) maximumBuyValue;

                if (balanceCount > minimumBuyValue)
                    count += 1;
                else {
                    balancePercent = ((balanceCount / minimumBuyValue) * 100);
                }


                //Updating list to maintain used(if current slab applied) quantity,scheme wise..
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                    double totVal;
                    double tempToQty;
                    tempToQty = maximumBuyValue;

                    for (SchemeProductBO schemeProductBO : schemeProductList) {

                        totVal = getTotalOrderedValue(schemeProductBO.getProductId(), isBatchWise, schemeProductBO.getBatchId(), parentID, isAccumulationScheme, IS_CHECK_SCHEME_WITH_ASRP);

                        if (totVal > 0) {

                            if (tempToQty >= totVal) {
                                tempToQty -= totVal;

                                if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                        !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + schemeProductBO.getProductId())) {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()), (int) totVal);
                                } else {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()),
                                            (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) + ((int) totVal)));
                                }

                            } else {
                                if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                        !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + schemeProductBO.getProductId())) {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()), (int) appliedSchemeValue);
                                } else {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + schemeProductBO.getProductId()),
                                            (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + schemeProductBO.getProductId()) + ((int) appliedSchemeValue)));
                                }
                                break;
                            }


                        }


                    }
                }
                /////////////

                setBalancePercent(balancePercent);

                // For ProRata balance percentage is not needed if Highest slap is not achieved.
                if (!isHighestSlab && processType.equals(PROCESS_TYPE_PRORATA))
                    setBalancePercent(0);

                if (count != 0) {
                    return count;
                } else {
                    /*
                     * Now we are apply range wise scheme.so maximum qty only
                     * using for computation to how many times scheme achieved
                     */
                    return 1;
                }
            }
        }


        return 1;

    }


    /**
     * After scheme achieved successfully ,this method used to update the scheme free
     * details like product,percentage,amount and price     *
     *
     * @param schemeBO                   Current Slab
     * @param mNumberOfTimesSlabAchieved Number of times slab achieved
     */
    private void calculateFreeForAchievedScheme(SchemeBO schemeBO, int mNumberOfTimesSlabAchieved) {

        if (schemeBO.getFreeProducts() != null) {

            int minFreeQuantity = 0;
            int maxFreeQuantity = 0;
            double minFreePrice = 0;
            double maxFreePrice = 0;
            double minFreePercent = 0;
            double maxFreePercent = 0;
            double minFreeAmount = 0;
            double maxFreeAmount = 0;

            for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {

                int minimumQuantity = schemeProductBO.getQuantityMinimum();
                int maximumQuantity = schemeProductBO.getQuantityMaximum();

                if (schemeBO.getProcessType() != null && (schemeBO.getProcessType().equals(PROCESS_TYPE_MULTIPLE_TIME_FOR_REMAINING)
                        || schemeBO.getProcessType().equals(PROCESS_TYPE_MTS))) {

                    /* scheme Buy type is Quantity */
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {


                        //Calculating min and max free quantity
                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        // Calculate for amount discount
                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;


                    }
                    /* scheme type is Sales Value */
                    else if (schemeBO.getBuyType().equals(SALES_VALUE)) {


                        // Calculate for quantity
                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        // Calculate for amount discount
                        minFreeAmount = schemeProductBO.getMinAmount()
                                * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount()
                                * mNumberOfTimesSlabAchieved;

                    }

                } else if (schemeBO.getProcessType() != null && schemeBO.getProcessType().equals(PROCESS_TYPE_ONE_TIME_WITH_PERCENTAGE)) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        // adding calculated percent of quantity
                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            minFreeQuantity += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            maxFreeQuantity += remainingMaxQty;
                        //

                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;
                        // adding calculated percent of amount
                        minFreeAmount = minFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxFreeAmount = maxFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            minFreeQuantity += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            maxFreeQuantity += remainingMaxQty;


                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;

                        // adding calculated percent of amount
                        minFreeAmount = minFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxFreeAmount = maxFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    }
                } else if (schemeBO.getProcessType() != null && schemeBO.getProcessType().equals(PROCESS_TYPE_PRORATA)) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            minFreeQuantity += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            maxFreeQuantity += remainingMaxQty;

                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;

                        // adding calculated percent of amount
                        minFreeAmount = minFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxFreeAmount = maxFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            minFreeQuantity += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            maxFreeQuantity += remainingMaxQty;


                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;

                        // adding calculated percent of amount
                        minFreeAmount = minFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxFreeAmount = maxFreeAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    }
                } else {
                    minFreeQuantity = Math.round((float) minimumQuantity);
                    maxFreeQuantity = Math.round((float) maximumQuantity);

                    if (schemeBO.getEveryQty() != 0) {
                        int count = calculateApplyCountBasedOnEveryUOM(schemeBO);
                        if (count == 0) count = 1; // at least one time apply

                        minFreeAmount = schemeProductBO.getMinAmount() * count;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * count;

                    } else {

                        minFreeAmount = schemeProductBO.getMinAmount();
                        maxFreeAmount = schemeProductBO.getMaxAmount();
                    }

                }

                schemeProductBO.setQuantityActualCalculated(minFreeQuantity);
                schemeProductBO.setQuantityMaxiumCalculated(maxFreeQuantity);

                // no calculation required for Price discount and % discount

                minFreePrice = schemeProductBO.getPriceActual();
                maxFreePrice = schemeProductBO.getPriceMaximum();

                minFreePercent = schemeProductBO.getMinPercent();
                maxFreePercent = schemeProductBO.getMaxPercent();

                schemeProductBO.setMinPercentCalculated(minFreePercent);
                schemeProductBO.setMaxPrecentCalculated(maxFreePercent);

                schemeProductBO.setMinAmountCalculated(minFreeAmount);
                schemeProductBO.setMaxAmountCalculated(maxFreeAmount);

            }

            schemeBO.setActualQuantity(minFreeQuantity);
            schemeBO.setMaximumQuantity(maxFreeQuantity);

            schemeBO.setActualPrice(minFreePrice);
            schemeBO.setMaximumPrice(maxFreePrice);
            schemeBO.setSelectedPrice(minFreePrice);

            schemeBO.setMinimumPrecent(minFreePercent);
            schemeBO.setMaximumPrecent(maxFreePercent);
            schemeBO.setSelectedPrecent(minFreePercent);

            schemeBO.setMinimumAmount(minFreeAmount);
            schemeBO.setMaximumAmount(maxFreeAmount);
            schemeBO.setSelectedAmount(minFreeAmount);

            //Updating flag to show in Scheme Apply Screen
            if (schemeBO.getActualPrice() > 0 && schemeBO.getMaximumPrice() > 0) {
                schemeBO.setPriceTypeSeleted(true);
            } else if (schemeBO.getMinimumAmount() > 0 && schemeBO.getMaximumAmount() > 0) {
                schemeBO.setAmountTypeSelected(true);
            } else if (schemeBO.getMinimumPrecent() > 0 && schemeBO.getMaximumPrecent() > 0) {
                schemeBO.setDiscountPrecentSelected(true);
            } else if (schemeBO.getActualQuantity() > 0 && schemeBO.getMaximumQuantity() > 0) {
                schemeBO.setQuantityTypeSelected(true);
            }


        }
    }

    /**
     * Updating SIH availability for free products
     * and altering actual value based on the stock availability.
     */
    private void updateSIHAvailabilityForFreeProducts() {

        for (SchemeBO schemeBO : mAppliedSchemeList) {

            ArrayList<String> freeGroupNameList = getFreeGroupNameListBySchemeID().get(schemeBO.getSchemeId());
            if (freeGroupNameList != null) {

                schemeBO.setQuantityTypeSelected(true);

                if (schemeBO.getFreeType().equals(AND_LOGIC)) {
                    schemeBO.setSihAvailableForFreeProducts(true);
                } else {
                    schemeBO.setSihAvailableForFreeProducts(false);
                }

                List<SchemeProductBO> freeProducts = schemeBO.getFreeProducts();
                ProductMasterBO productMasterBO;

                if (freeProducts != null) {
                    int i = 0;

                    for (String freeGroupName : freeGroupNameList) {
                        boolean isSihAvailableForFreeProducts = isSihAvailableForSchemeGroupFreeProducts(schemeBO, freeGroupName);

                        if (isSihAvailableForFreeProducts) {
                            if (i == 0 || !schemeBO.getFreeType().equals(AND_LOGIC))
                                schemeBO.setSihAvailableForFreeProducts(true);
                        } else {
                            if (schemeBO.getFreeType().equals(AND_LOGIC)) {
                                schemeBO.setSihAvailableForFreeProducts(false);
                            }
                        }

                        i++;

                        if (!bModel.configurationMasterHelper.IS_SIH_VALIDATION || isSihAvailableForFreeProducts) {
                            int freeQuantity = 0;
                            int count = 0;

                            for (SchemeProductBO schemePdtBO : freeProducts) {

                                if (freeGroupName.equals(schemePdtBO.getGroupName())) {

                                    if (schemePdtBO.getGroupLogic().equals(ANY_LOGIC)) {//check any logic condition
                                        if (count == 0) {
                                            freeQuantity = schemePdtBO.getQuantityActualCalculated();
                                        }

                                    } else {
                                        freeQuantity = schemePdtBO.getQuantityActualCalculated();

                                    }
                                    count++;

                                    int stock = 0;
                                    productMasterBO = mProductMasterBOById.get(schemePdtBO.getProductId());

                                    if (productMasterBO != null) {
                                        if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE) {
                                            stock = productMasterBO.getFreeSIH();
                                        } else {
                                            stock = productMasterBO.getSIH()
                                                    - ((productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                                    + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                                    .getOutersize()) + productMasterBO.getOrderedPcsQty());
                                        }
                                    }

                                    schemePdtBO.setQuantitySelected(0);

                                    if (bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                        if (bModel.getResources().getBoolean(R.bool.config_is_sih_considered)) {
                                            if (stock > 0) {

                                                if ((stock - freeQuantity) >= 0) {
                                                    schemePdtBO
                                                            .setQuantitySelected(freeQuantity);
                                                    freeQuantity = 0;
                                                } else {
                                                    schemePdtBO
                                                            .setQuantitySelected(stock);
                                                    freeQuantity -= stock;
                                                }
                                            }
                                        } else {
                                            schemePdtBO
                                                    .setQuantitySelected(freeQuantity);
                                        }

                                        if (schemePdtBO.getGroupLogic().equals(ANY_LOGIC)) {// child
                                            // ANY
                                            // logic
                                            if (freeQuantity == 0)
                                                break;
                                        }
                                    } else {
                                        schemePdtBO.setQuantitySelected(freeQuantity);
                                        if (schemePdtBO.getGroupLogic().equals(ANY_LOGIC)) { // child
                                            // ANY
                                            // logic
                                            break;
                                        }
                                    }

                                }

                            }
                            if (schemeBO.getFreeType().equals(ANY_LOGIC)) {
                                break; // used for parent ANY LOGIC
                            }
                        }
                    }
                }
            }
        }

        //Updating current stock for free products of all applied scheme
        for (SchemeBO schemeBO : mAppliedSchemeList) {
            for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {
                ProductMasterBO productMasterBO = mProductMasterBOById.get(schemeProductBO.getProductId());

                if (productMasterBO != null) {
                    int stock;
                    if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE) {
                        stock = productMasterBO.getFreeSIH();
                    } else {
                        stock = productMasterBO.getSIH()
                                - ((productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOutersize()) + productMasterBO.getOrderedPcsQty());
                    }
                    if (stock > 0)
                        schemeProductBO.setStock(stock);
                    else schemeProductBO.setStock(0);
                }
            }

        }

    }


    /**
     * Checking whether SIH is available for given group
     *
     * @param schemeBO  Slab object
     * @param groupName group Name
     * @return Is SIH available
     */
    private boolean isSihAvailableForSchemeGroupFreeProducts(SchemeBO schemeBO, String groupName) {
        boolean flag = true;
        final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();

        if (freeProductList != null) {
            for (SchemeProductBO schemeProductBO : freeProductList) {
                if (groupName != null && groupName.equals(schemeProductBO.getGroupName())) {

                    int stock;
                    ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                    if (productBO != null) {

                        if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE) {
                            stock = productBO.getFreeSIH();
                        } else {
                            int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                            stock = productBO.getSIH() - totalQty;
                        }

                        int freeProductQty = 0;
                        if (schemeProductBO.getUomID() == productBO.getPcUomid() || schemeProductBO.getUomID() == 0) {
                            freeProductQty = schemeProductBO.getQuantityMinimum();
                        } else if (schemeProductBO.getUomID() == productBO.getCaseUomId()) {
                            freeProductQty = schemeProductBO.getQuantityMinimum() * productBO.getCaseSize();
                        } else if (schemeProductBO.getUomID() == productBO.getOuUomid()) {
                            freeProductQty = schemeProductBO.getQuantityMinimum() * productBO.getOutersize();
                        }
                        freeProductQty = freeProductQty * schemeBO.getApplyCount();

                        if (stock < freeProductQty) {
                            flag = false;
                        } else {// to check for any logic
                            if (!schemeProductBO.getGroupLogic().equals(AND_LOGIC))
                                flag = true;
                        }
                        if (schemeProductBO.getGroupLogic().equals(ANY_LOGIC) || schemeProductBO.getGroupLogic().equals(ONLY_LOGIC)) {
                            if (flag) return true;
                        } else {
                            if (!flag) return false;
                        }


                    } else {
                        flag = false;
                    }
                }
            }

        } else {
            flag = false;
        }
        return flag;

    }

    private double getBalancePercent() {
        return balancePercent;
    }

    private void setBalancePercent(double balancePercent) {
        this.balancePercent = balancePercent;
    }


    public HashMap<String, ArrayList<ProductMasterBO>> getSchemeHistoryListBySchemeId() {
        return mSchemeHistoryListBySchemeId;
    }


    public int getTotalOrderedQuantity(String productId, boolean isBatchWise, String batchId, int uomId, int schemeId, boolean isAccumulationScheme) {
        int total = 0;

        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            if (productMasterBO.getProductID().equals(productId)
                    || productMasterBO.getParentHierarchy().contains("/" + productId + "/")) {

                if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0) {
                    if (mBatchListByProductId != null) {

                        ArrayList<ProductMasterBO> batchWiseList = mBatchListByProductId.get(productMasterBO.getProductID());
                        if (batchWiseList != null) {

                            for (ProductMasterBO batchProductBO : batchWiseList) {
                                if (batchProductBO.getBatchid().equals(batchId)) {

                                    if (batchProductBO.getOrderedPcsQty() > 0 || batchProductBO.getOrderedCaseQty() > 0 || batchProductBO.getOrderedOuterQty() > 0) {

                                        int totalQuantity = batchProductBO.getOrderedPcsQty()
                                                + (batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                                + (batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize());


                                        total += getNumberOfGivenUOM(productMasterBO, uomId, totalQuantity);

                                    }
                                }
                            }
                        }
                    }
                } else {
                    int totalQuantity = productMasterBO.getOrderedPcsQty() + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());

                    total += getNumberOfGivenUOM(productMasterBO, uomId, totalQuantity);
                }
            }
        }

        // Getting accumulation qty for every buy products
        if (isAccumulationScheme) {
            int totalQuantity = getTotalAccumulationQuantity(schemeId, bModel.productHelper.getProductMasterBOById(productId).getProductID(), isBatchWise, batchId);
            total += getNumberOfGivenUOM(bModel.productHelper.getProductMasterBOById(productId), uomId, totalQuantity);
        }

        return total;

    }

    public double getTotalOrderedValue(String productId, boolean isBatchWise, String batchId, int schemeId, boolean isAccumulationScheme, boolean isFromASRP) {
        double totalValue = 0;
        if (mOrderedProductList == null)
            prepareNecessaryLists(bModel.productHelper.getProductMaster());
        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            if (productMasterBO.getProductID().equals(productId) || productMasterBO.getParentHierarchy().contains("/" + productId + "/")) {

                if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0) {
                    if (mBatchListByProductId != null) {

                        ArrayList<ProductMasterBO> batchWiseList = mBatchListByProductId.get(productMasterBO.getProductID());
                        if (batchWiseList != null) {

                            for (ProductMasterBO batchProductBO : batchWiseList) {

                                if (batchProductBO.getBatchid().equals(batchId)) {

                                    if (batchProductBO.getOrderedPcsQty() > 0 || batchProductBO.getOrderedCaseQty() > 0 || batchProductBO.getOrderedOuterQty() > 0) {

                                        if (isFromASRP) {
                                            int qty = batchProductBO.getOrderedPcsQty()
                                                    + (batchProductBO.getOrderedCaseQty() * batchProductBO.getCaseSize())
                                                    + (batchProductBO.getOrderedOuterQty() * batchProductBO.getOutersize());
                                            totalValue += qty * batchProductBO.getASRP();
                                        } else {
                                            totalValue += (batchProductBO.getOrderedPcsQty() * batchProductBO.getSrp())
                                                    + (batchProductBO.getOrderedCaseQty() * batchProductBO.getCsrp())
                                                    + (batchProductBO.getOrderedOuterQty() * batchProductBO.getOsrp());
                                        }


                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (isFromASRP) {
                        int qty = productMasterBO.getOrderedPcsQty()
                                + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());

                        totalValue += qty * productMasterBO.getASRP();
                    } else {
                        totalValue += (productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp())
                                + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp());
                    }


                }
            }
        }

        // Getting accumulation value for every buy products
        if (isAccumulationScheme)
            totalValue += getTotalAccumulationValue(schemeId, productId, isBatchWise, batchId);

        return SDUtil.formatAsPerCalculationConfig(totalValue);

    }

    private int getNumberOfGivenUOM(ProductMasterBO productMasterBO, int uomId, int totalQuantity) {
        int total = 0;
        if (productMasterBO != null) {
            if (uomId == productMasterBO.getCaseUomId()) {
                total = totalQuantity / productMasterBO.getCaseSize();
            } else if (uomId == productMasterBO.getOuUomid()) {
                total = totalQuantity / productMasterBO.getOutersize();
            } else {
                total = totalQuantity;
            }
        }
        return total;
    }

    private int getTotalAccumulationQuantity(int schemeId, String productId, boolean isBatchWise, String batchId) {

        int totalQty = 0;
        ArrayList<ProductMasterBO> mAccumulationList = getSchemeHistoryListBySchemeId().get(schemeId + "");

        if (mAccumulationList != null) {
            for (ProductMasterBO schemeAccBO : mAccumulationList) {

                ProductMasterBO productMasterBO = mProductMasterBOById.get(schemeAccBO.getProductID());
                if (productMasterBO != null)
                    if (productId.equals(schemeAccBO.getProductID()) || productMasterBO.getParentHierarchy().contains(productId)) {

                        if (!isBatchWise || schemeAccBO.getBatchid().equals(batchId)) {
                            totalQty += (
                                    schemeAccBO.getOrderedPcsQty()
                                            + (schemeAccBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                            + (schemeAccBO.getOrderedOuterQty() * productMasterBO.getOutersize()));
                            break;
                        }
                    }
            }
        }

        //Getting local order value for the current product
        ProductMasterBO productMasterBO = mProductMasterBOById.get(productId);
        if (productMasterBO != null)
            totalQty += (
                    productMasterBO.getInit_pieceqty()
                            + (productMasterBO.getInit_caseqty() * productMasterBO.getCaseSize())
                            + (productMasterBO.getInit_OuterQty() * productMasterBO.getOutersize()));


        return totalQty;
    }

    private double getTotalAccumulationValue(int schemeId, String productId, boolean isBatchWise, String batchId) {

        double totalValue = 0;
        ArrayList<ProductMasterBO> mAccumulationList = getSchemeHistoryListBySchemeId().get(schemeId + "");

        if (mAccumulationList != null) {
            for (ProductMasterBO schemeAccBO : mAccumulationList) {

                ProductMasterBO productMasterBO = mProductMasterBOById.get(schemeAccBO.getProductID());
                if (productMasterBO != null)
                    if (productId.equals(schemeAccBO.getProductID()) || productMasterBO.getParentHierarchy().contains(productId)) {

                        if (!isBatchWise || (schemeAccBO.getBatchid().equals(batchId))) {
                            totalValue += schemeAccBO.getTotalamount();
                            break;
                        }
                    }
            }
        }

        //Getting local order value for the current product
        ProductMasterBO productMasterBO = mProductMasterBOById.get(productId);
        if (productMasterBO != null)
            totalValue += (
                    (productMasterBO.getInit_pieceqty() * productMasterBO.getSrp())
                            + (productMasterBO.getInit_caseqty() * productMasterBO.getCsrp())
                            + (productMasterBO.getInit_OuterQty() * productMasterBO.getOsrp()));


        return totalValue;
    }


    /////////////////////////////////////////////////////////////


    /**
     * Insert scheme discounts
     *
     * @param orderID order Id
     * @param db      Database Object
     */
    public void insertSchemeDetails(String orderID, DBUtil db) {
        if (mAppliedSchemeList != null) {

            for (SchemeBO schemeBO : mAppliedSchemeList) {

                if (schemeBO.isAmountTypeSelected()
                        || schemeBO.isPriceTypeSeleted()
                        || schemeBO.isDiscountPrecentSelected()
                        || schemeBO.isQuantityTypeSelected()) {
                    insertSchemeBuyProductDetails(schemeBO, db, orderID);
                }

                if (schemeBO.isQuantityTypeSelected()) {
                    insertFreeProductDetails(schemeBO, db, orderID);
                }

            }
        }

    }

    /**
     * Insert Buy product details with discount type
     *
     * @param schemeBO Slab to insert
     * @param db       database Object
     * @param orderID  order Id
     */
    private void insertSchemeBuyProductDetails(SchemeBO schemeBO, DBUtil db,
                                               String orderID) {
        String schemeDetailColumn = "OrderID,SchemeID,ProductID,SchemeType,Value,parentid,Retailerid,distributorid,upload,Amount";


        List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();

        if (buyProductList != null) {
            for (SchemeProductBO schemeProductBO : buyProductList) {
                ProductMasterBO productBO;
                productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());

                if (productBO != null) {
                    if ((productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0)
                            || (schemeBO.isQuantityTypeSelected() && schemeBO.getFreeProducts() != null && schemeBO.getFreeProducts().size() > 0)) {//this condition checked for current accumulation scheme if buy product's not available

                        saveProductSchemeDetail(schemeBO, db, orderID, schemeDetailColumn, schemeProductBO, productBO);

                    }

                } else {
                    if (mOrderedProductList != null) {
                        for (int index = 0; index < mOrderedProductList.size(); index++) {
                            ProductMasterBO productMasterBO = mOrderedProductList.get(index);
                            if (productMasterBO.getParentHierarchy().contains("/" + schemeProductBO.getProductId() + "/")) {
                                if ((productMasterBO.getOrderedPcsQty() > 0 || productMasterBO.getOrderedCaseQty() > 0 || productMasterBO.getOrderedOuterQty() > 0)) {//this condition checked for current accumulation scheme if buy product's not available
                                    saveProductSchemeDetail(schemeBO, db, orderID, schemeDetailColumn, schemeProductBO, productMasterBO);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void saveProductSchemeDetail(SchemeBO schemeBO, DBUtil db, String orderID, String schemeDetailColumn, SchemeProductBO schemeProductBO, ProductMasterBO productBO) {
        StringBuffer sb = new StringBuffer();
        sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                + schemeProductBO.getProductId() + ",");
        if (schemeBO.isQuantityTypeSelected()) {
            sb.append(bModel.QT(SCHEME_FREE_PRODUCT) + ",");
            sb.append(+0);
        } else if (schemeBO.isAmountTypeSelected()) {
            sb.append(bModel.QT(SCHEME_AMOUNT));

            sb.append("," + (schemeProductBO.getDiscountValue()));

        } else if (schemeBO.isPriceTypeSeleted()) {
            sb.append(bModel.QT(SCHEME_PRICE));
            sb.append("," + schemeBO.getSelectedPrice());

        } else if (schemeBO.isDiscountPrecentSelected()) {
            if (schemeBO.getGetType().equalsIgnoreCase(SCHEME_PERCENTAGE_BILL)) {
                sb = new StringBuffer();
                sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                        + 0 + ",");
            }

            sb.append(bModel.QT(SCHEME_PERCENTAGE));
            sb.append("," + schemeBO.getSelectedPrecent());
        }
        sb.append("," + schemeBO.getParentId());
        sb.append("," + bModel.getRetailerMasterBO().getRetailerID());
        sb.append("," + bModel.getRetailerMasterBO().getDistributorId());
        if (schemeBO.isQuantityTypeSelected()) {
            sb.append(",'Y'");
        } else {
            sb.append(",'N'");
        }

        sb.append("," + schemeProductBO.getDiscountValue());

        db.insertSQL(DataMembers.tbl_scheme_details,
                schemeDetailColumn, sb.toString());
    }


    /**
     * Insert free products
     *
     * @param schemeBO Slab to insert
     * @param db       database Object
     * @param orderID  order Id
     */
    private void insertFreeProductDetails(SchemeBO schemeBO, DBUtil db,
                                          String orderID) {
        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId,price,taxAmount,HsnCode";


        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO freeProductBO : freeProductList) {
                if (freeProductBO.getQuantitySelected() > 0) {

                    ProductMasterBO productBO = bModel
                            .getProductbyId(freeProductBO.getProductId());
                    if (productBO != null) {
                        if (isBatchWiseProducts
                                && bModel.configurationMasterHelper.IS_SIH_VALIDATION
                                && bModel.configurationMasterHelper.IS_INVOICE) {
                            if (productBO.getBatchwiseProductCount() > 0) {
                                insertFreeProductWithBatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn);
                            } else {
                                insertFreeProductWithoutBatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn);
                            }
                        } else {
                            insertFreeProductWithoutBatch(schemeBO, db,
                                    orderID, freeProductBO, freeDetailColumn);

                        }
                    }

                }
            }
        }

    }


    /**
     * Insert free product with out batch
     *
     * @param schemeBO         slab
     * @param db               Database Object
     * @param orderID          Order Id
     * @param freeProductBO    Free Product Object
     * @param freeDetailColumn ColumnNames
     */
    private void insertFreeProductWithoutBatch(SchemeBO schemeBO, DBUtil db,
                                               String orderID, SchemeProductBO freeProductBO,
                                               String freeDetailColumn) {

        ProductMasterBO productBO = bModel.getProductbyId(freeProductBO
                .getProductId());
        if (productBO != null) {

            StringBuffer sb = new StringBuffer();
            sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                    + freeProductBO.getProductId() + ",");
            sb.append(freeProductBO.getQuantitySelected() + ","
                    + freeProductBO.getUomID() + ",");
            if (freeProductBO.getUomID() == productBO.getCaseUomId()
                    && productBO.getCaseUomId() != 0) {
                sb.append(productBO.getCaseSize() + ",");
            } else if (freeProductBO.getUomID() == productBO.getOuUomid()
                    && productBO.getOuUomid() != 0) {
                sb.append(productBO.getOutersize() + ",");
            } else if (freeProductBO.getUomID() == productBO.getPcUomid()
                    || freeProductBO.getUomID() == 0) {
                sb.append(1 + ",");
            } else {
                sb.append(1 + ",");
            }
            sb.append(0 + "," + schemeBO.getParentId());
            sb.append("," + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()) + ",");

            if (bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN) {

                if (freeProductBO.getUomID() == productBO.getCaseUomId()
                        && productBO.getCaseUomId() != 0) {
                    sb.append(productBO.getCsrp());
                } else if (freeProductBO.getUomID() == productBO.getOuUomid()
                        && productBO.getOuUomid() != 0) {
                    sb.append(productBO.getOsrp());
                } else if (freeProductBO.getUomID() == productBO.getPcUomid()
                        || freeProductBO.getUomID() == 0) {
                    sb.append(productBO.getSrp());
                }
                sb.append("," + bModel.formatValue(freeProductBO.getTaxAmount()));
            } else {
                sb.append(0 + "," + 0);
            }

            sb.append("," + bModel.QT(productBO.getHsnCode()));

            db.insertSQL(DataMembers.tbl_SchemeFreeProductDetail, freeDetailColumn,
                    sb.toString());


        }
    }

    /**
     * Insert free product  with batch
     *
     * @param schemeBO         Slab to insert
     * @param db               database object
     * @param orderID          Order Id
     * @param schemeProductBo  Free product object
     * @param freeDetailColumn column Names
     */
    private void insertFreeProductWithBatch(SchemeBO schemeBO, DBUtil db,
                                            String orderID, SchemeProductBO schemeProductBo,
                                            String freeDetailColumn) {

        ArrayList<SchemeProductBatchQty> freeProductBatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bModel.productHelper
                .getProductMasterBOById(schemeProductBo.getProductId());
        if (freeProductBatchList != null) {
            StringBuffer sb;
            for (SchemeProductBatchQty schemeProductBatchQty : freeProductBatchList) {
                if (schemeProductBatchQty.getQty() > 0 || schemeProductBatchQty.getCaseQty() > 0 ||
                        schemeProductBatchQty.getOuterQty() > 0) {
                    sb = new StringBuffer();
                    sb.append(orderID + "," + schemeProductBo.getSchemeId() + ",");
                    sb.append(schemeProductBo.getProductId() + ",");
                    if (schemeProductBo.getUomID() == productBo.getCaseUomId()
                            && productBo.getCaseUomId() != 0) {
                        sb.append(schemeProductBatchQty.getCaseQty() + "," + schemeProductBo.getUomID() + "," + productBo.getCaseSize() + ",");
                    } else if (schemeProductBo.getUomID() == productBo.getOuUomid()
                            && productBo.getOuUomid() != 0) {
                        sb.append(schemeProductBatchQty.getOuterQty() + "," + schemeProductBo.getUomID() + "," + productBo.getOutersize() + ",");
                    } else if (schemeProductBo.getUomID() == productBo.getPcUomid()
                            || schemeProductBo.getUomID() == 0) {
                        sb.append(schemeProductBatchQty.getQty() + "," + schemeProductBo.getUomID() + "," + 1 + ",");
                    }
                    sb.append(schemeProductBatchQty.getBatchid());
                    sb.append("," + schemeBO.getSchemeId());
                    sb.append("," + bModel.getRetailerMasterBO().getRetailerID());

                    if (bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN) {

                        if (schemeProductBo.getUomID() == productBo.getCaseUomId()
                                && productBo.getCaseUomId() != 0) {
                            sb.append(productBo.getCsrp());
                        } else if (schemeProductBo.getUomID() == productBo.getOuUomid()
                                && productBo.getOuUomid() != 0) {
                            sb.append(productBo.getOsrp());
                        } else if (schemeProductBo.getUomID() == productBo.getPcUomid()
                                || schemeProductBo.getUomID() == 0) {
                            sb.append(productBo.getSrp());
                        }
                        sb.append("," + bModel.formatValue(schemeProductBo.getTaxAmount()));
                    } else {
                        sb.append(",0,0");
                    }

                    sb.append("," + bModel.QT(productBo.getHsnCode()));

                    db.insertSQL(DataMembers.tbl_SchemeFreeProductDetail, freeDetailColumn,
                            sb.toString());


                }
            }
        }

    }


    /**
     * Insert Off invoice scheme free issues
     *
     * @param mContext Current Context
     * @param db       Database Object
     * @param orderID  Order Id
     */
    public void insertAccumulationDetails(Context mContext, DBUtil db, String orderID) {


        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId,HsnCode";

        if (mOffInvoiceAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceAppliedSchemeList) {
                if (!IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE
                        || OrderHelper.getInstance(mContext).getValidAccumulationSchemes().contains(String.valueOf(schemeBO.getParentId()))) {

                    if (schemeBO.isQuantityTypeSelected()) {
                        final List<SchemeProductBO> freeProductsList = schemeBO.getFreeProducts();
                        for (SchemeProductBO freeProductBO : freeProductsList) {
                            if (freeProductBO.getQuantitySelected() > 0) {
                                ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(freeProductBO.getProductId());

                                if (productBO != null) {
                                    StringBuffer sb = new StringBuffer();
                                    sb.append(orderID + "," + freeProductBO.getSchemeId() + ","
                                            + freeProductBO.getProductId() + ",");
                                    sb.append(freeProductBO.getQuantitySelected() + ","
                                            + freeProductBO.getUomID() + ",");
                                    if (freeProductBO.getUomID() == productBO.getCaseUomId()
                                            && productBO.getCaseUomId() != 0) {
                                        sb.append(productBO.getCaseSize() + ",");
                                    } else if (freeProductBO.getUomID() == productBO.getOuUomid()
                                            && productBO.getOuUomid() != 0) {
                                        sb.append(productBO.getOutersize() + ",");
                                    } else if (freeProductBO.getUomID() == productBO.getPcUomid()
                                            || freeProductBO.getUomID() == 0) {
                                        sb.append(1 + ",");
                                    } else {
                                        sb.append(1 + ",");
                                    }
                                    sb.append(0 + "," + freeProductBO.getAccProductParentId());
                                    sb.append("," + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()));
                                    sb.append("," + bModel.QT(productBO.getHsnCode()));

                                    db.insertSQL(DataMembers.tbl_SchemeFreeProductDetail, freeDetailColumn,
                                            sb.toString());
                                }
                            }
                        }


                    }
                }
            }
        }


    }


    /**
     * Load scheme from transactions
     *
     * @param mContext   Current Context
     * @param retailerID Retailer Id
     */
    public void loadSchemeDetails(Context mContext, String retailerID) {

        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select orderID from orderHeader where retailerid="
                    + bModel.QT(retailerID));
            sb.append(" and invoicestatus=0 and upload='N'");

            if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                sb.append(" and is_vansales="
                        + bModel.getRetailerMasterBO().getIsVansales());
            }

            sb.append(" and sid=" + bModel.getRetailerMasterBO().getDistributorId());

            sb.append(" and orderid not in(select orderid from OrderDeliveryDetail)");// to prevent delivered orders

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    String orderID = c.getString(0);
                    loadBuyProducts(orderID, db);
                    loadFreeProducts(orderID, db);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            if (db != null)
                db.closeDB();
        }
    }

    /**
     * Load buy product details
     *
     * @param id Order Id
     * @param db Database Object
     */
    private void loadBuyProducts(String id, DBUtil db) {
        mAppliedSchemeList = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct schemeid,SchemeType,value,amount,count(productid) from SchemeDetail where ");

        sb.append("orderid=" + bModel.QT(id));

        sb.append("  group by schemeid");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String schemeID = c.getString(0);
                String schemeType = c.getString(1);
                double value = c.getDouble(2);
                SchemeBO schemeBO = mSchemeById.get(schemeID);
                if (schemeBO != null) {

                    if (schemeType.equals(SCHEME_AMOUNT)) {
                        schemeBO.setAmountTypeSelected(true);
                        //only amount column haves full free amount.. so getting from it
                        schemeBO.setSelectedAmount(c.getDouble(3));
                    } else if (schemeType.equals(SCHEME_FREE_PRODUCT)) {

                        schemeBO.setQuantityTypeSelected(true);

                    } else if (schemeType.equals(SCHEME_PERCENTAGE)) {
                        schemeBO.setDiscountPrecentSelected(true);
                        schemeBO.setSelectedPrecent(value);
                    } else if (schemeType.equals(SCHEME_PRICE)) {
                        schemeBO.setPriceTypeSeleted(true);
                        schemeBO.setSelectedPrice(value);
                    }

                }
                mAppliedSchemeList.add(schemeBO);

            }
        }

    }

    /**
     * Load Scheme Free products
     *
     * @param id Order Id
     * @param db Database Object
     */
    private void loadFreeProducts(String id, DBUtil db) {
        // clear free product details
        Cursor c1 = db
                .selectSQL("select distinct schemeid from schemeFreeProductDetail where orderid ="
                        + bModel.QT(id) + " and upload='N'");
        if (c1.getCount() > 0) {
            while (c1.moveToNext()) {
                String schemeId = c1.getString(0);
                SchemeBO schemeBo = getSchemeById().get(schemeId);
                if (schemeBo != null) {
                    clearSchemeFreeProduct(schemeBo);
                }
            }
        }

        Cursor c = db
                .selectSQL("select schemeid,FreeProductID,FreeQty,UomID,batchid from schemeFreeProductDetail "
                        + "where orderID="
                        + bModel.QT(id)
                        + " and upload='N' order by schemeid");
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String schemeID = c.getString(0);
                String freeProductID = c.getString(1);
                int freeProductQty = c.getInt(2);

                SchemeBO schemeBO = mSchemeById.get(schemeID);
                if (schemeBO != null) {
                    List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                    if (freeProductList != null) {
                        for (SchemeProductBO freeProductBO : freeProductList) {
                            if (freeProductBO.getProductId().equals(freeProductID)) {
                                freeProductBO.setQuantitySelected(freeProductBO
                                        .getQuantitySelected() + freeProductQty);
                                break;
                            }
                        }
                    }
                }


            }
        }
        c.close();
    }


    /**
     * Clear free products
     *
     * @param schemeBO Slab object
     */
    private void clearSchemeFreeProduct(SchemeBO schemeBO) {
        if (schemeBO != null) {
            List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
            if (freeProductList != null) {
                for (SchemeProductBO freeProductBo : freeProductList) {
                    freeProductBo.setQuantitySelected(0);
                }
            }
        }
    }


    /**
     * Reducing free products from SIH
     *
     * @param db Database Objects
     */
    public void reduceFreeProductsFromSIH(DBUtil db) {

        if (mAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mAppliedSchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    List<SchemeProductBO> freeProductList = schemeBO
                            .getFreeProducts();
                    for (SchemeProductBO schemeProductBO : freeProductList) {
                        if (schemeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bModel.productHelper
                                    .getProductMasterBOById(schemeProductBO
                                            .getProductId());
                            if (productBO != null) {
                                int totalFreeQty = 0;
                                // case wise free product update sih
                                if (schemeProductBO.getUomID() == productBO
                                        .getCaseUomId()
                                        && productBO.getCaseUomId() != 0) {
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getCaseSize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getOuUomid()
                                        && productBO.getOuUomid() != 0) { // outer

                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getOutersize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getPcUomid()
                                        || schemeProductBO.getUomID() == 0) { // piece

                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected();
                                }

                                if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE) {
                                    int s = productBO.getFreeSIH() > totalFreeQty ? productBO
                                            .getFreeSIH() - totalFreeQty
                                            : 0;
                                    productBO.setFreeSIH(s);
                                } else {
                                    int s = productBO.getSIH() > totalFreeQty ? productBO
                                            .getSIH() - totalFreeQty
                                            : 0;
                                    productBO.setSIH(s);
                                }
                                db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                        + totalFreeQty
                                        + " then ifnull(sih,0)-"
                                        + totalFreeQty
                                        + " else 0 end) where pid="
                                        + productBO.getProductID());

                                if (isBatchWiseProducts && productBO.getBatchwiseProductCount() > 0) {
                                    reduceFreeProductFromSIHBatchWise(
                                            schemeProductBO, db);
                                } else {

                                    String stockTable = "StockInHandMaster";
                                    if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
                                        stockTable = "FreeStockInHandMaster";

                                    db.executeQ("update " + stockTable + " set upload='N',qty=(case when  ifnull(qty,0)>"
                                            + totalFreeQty
                                            + " then ifnull(qty,0)-"
                                            + totalFreeQty
                                            + " else 0 end) where pid="
                                            + productBO.getProductID());
                                }

                            }

                        }
                    }
                }

                updateSchemeCountApply(
                        schemeBO.getSchemeId(), db);
            }
        }

        // update sih off invoice scheme
        if (mOffInvoiceAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceAppliedSchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    List<SchemeProductBO> freeProductList = schemeBO
                            .getFreeProducts();
                    for (SchemeProductBO schemeProductBO : freeProductList) {
                        if (schemeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bModel.productHelper
                                    .getProductMasterBOById(schemeProductBO
                                            .getProductId());
                            if (productBO != null) {
                                int totalFreeQty = 0;
                                if (schemeProductBO.getUomID() == productBO
                                        .getCaseUomId()
                                        && productBO.getCaseUomId() != 0) {
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getCaseSize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getOuUomid()
                                        && productBO.getOuUomid() != 0) { // outer

                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getOutersize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getPcUomid()
                                        || schemeProductBO.getUomID() == 0) { // piece

                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected();
                                }
                                if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE) {
                                    int s = productBO.getFreeSIH() > totalFreeQty ? productBO
                                            .getFreeSIH() - totalFreeQty
                                            : 0;
                                    productBO.setFreeSIH(s);
                                } else {
                                    int s = productBO.getSIH() > totalFreeQty ? productBO
                                            .getSIH() - totalFreeQty
                                            : 0;
                                    productBO.setSIH(s);
                                }

                                db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                        + totalFreeQty
                                        + " then ifnull(sih,0)-"
                                        + totalFreeQty
                                        + " else 0 end) where pid="
                                        + productBO.getProductID());

                                if (isBatchWiseProducts && productBO.getBatchwiseProductCount() > 0) {
                                    reduceFreeProductFromSIHBatchWise(
                                            schemeProductBO, db);
                                } else {

                                    String stockTable = "StockInHandMaster";
                                    if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
                                        stockTable = "FreeStockInHandMaster";

                                    db.executeQ("update " + stockTable + " set upload='N',qty=(case when  ifnull(qty,0)>"
                                            + totalFreeQty
                                            + " then ifnull(qty,0)-"
                                            + totalFreeQty
                                            + " else 0 end) where pid="
                                            + productBO.getProductID());
                                }

                            }

                        }
                    }
                }

                updateSchemeCountApply(
                        schemeBO.getSchemeId(), db);
            }
        }

    }

    /**
     * Reducing scheme apply count
     * No need to update if count is -1. If it is -1 then scheme can be achieved n number of times
     *
     * @param schemeId Scheme Id
     * @param db       Database Objects
     */
    private void updateSchemeCountApply(String schemeId, DBUtil db) {

        // update scheme apply count retailer wise
        String query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bModel.QT(schemeId) + " and schemeApplyCount!=-1 and retailerid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID());
        db.executeQ(query1);

        // update scheme apply count seller wise

        query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bModel.QT(schemeId) + " and schemeApplyCount!=-1 and userid=" + bModel.userMasterHelper.getUserMasterBO().getUserid();
        db.executeQ(query1);

    }

    /**
     * Reducing free products batch wise from SIH
     *
     * @param schemeProductBo Free product Bo
     * @param db              Database Object
     */
    private void reduceFreeProductFromSIHBatchWise(SchemeProductBO schemeProductBo, DBUtil db) {

        String stockTable = "StockInHandMaster";
        if (bModel.configurationMasterHelper.IS_FREE_SIH_AVAILABLE)
            stockTable = "FreeStockInHandMaster";

        ArrayList<SchemeProductBatchQty> freeProductBatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bModel.productHelper
                .getProductMasterBOById(schemeProductBo.getProductId());
        if (freeProductBatchList != null) {
            for (SchemeProductBatchQty schemeProductBatchQty : freeProductBatchList) {
                if (schemeProductBatchQty.getQty() > 0) {
                    db.executeQ("update " + stockTable + " set upload='N',qty=(case when  ifnull(qty,0)>"
                            + schemeProductBatchQty.getQty()
                            + " then ifnull(qty,0)-"
                            + schemeProductBatchQty.getQty()
                            + " else 0 end) where pid="
                            + schemeProductBo.getProductId()
                            + " and batchid="
                            + schemeProductBatchQty.getBatchid());
                    bModel.batchAllocationHelper.setBatchwiseSIH(productBo,
                            schemeProductBatchQty.getBatchid() + "",
                            schemeProductBatchQty.getQty(), false);
                }

            }
        }

    }


    /**
     * To ensure whether the same group is available in other slab. Based on this view will be prepared differently.
     *
     * @param parentId Scheme Id
     * @return Is Available or not
     */
    public boolean isSameGroupAvailableInOtherSlab(int parentId) {

        ArrayList<String> groupNameList;
        ArrayList<String> previousGroupNameList = null;
        ArrayList<String> schemeIdList = mSchemeIDListByParentID.get(parentId);
        if (schemeIdList != null) {
            for (String schemeId : schemeIdList) {

                groupNameList = mFreeGroupNameListBySchemeId.get(schemeId);
                if (previousGroupNameList != null) {
                    if (!previousGroupNameList.equals(groupNameList)) {
                        return false;
                    }
                }
                previousGroupNameList = mFreeGroupNameListBySchemeId.get(schemeId);
            }
        }

        return true;
    }

    /**
     * Calculate discount(Price/percentage) value
     *
     * @param productBo
     * @param value
     * @param type
     * @param isBatchWise
     * @return
     */
    public double calculateDiscountValue(ProductMasterBO productBo, double value,
                                         String type, boolean isBatchWise) {
        double total = 0.0;
        if (isBatchWise) {
            total = calculateDiscountValueBatchWise(productBo, value, type);

        } else {
            double totalValue;
            totalValue = productBo.getOrderedPcsQty() * productBo.getSrp()
                    + productBo.getOrderedCaseQty() * productBo.getCsrp()
                    + productBo.getOrderedOuterQty() * productBo.getOsrp();


            if (type.equals(SCHEME_PERCENTAGE)) {

                double totalPercentageValue = totalValue * value / 100;
                totalPercentageValue = SDUtil.formatAsPerCalculationConfig(totalPercentageValue);

                productBo.setSchemeDiscAmount(productBo.getSchemeDiscAmount() + totalPercentageValue);
                total = total + totalPercentageValue;


            } else if (type.equals(SCHEME_PRICE)) {
                int totalQty = productBo.getOrderedPcsQty()
                        + productBo.getOrderedCaseQty()
                        * productBo.getCaseSize()
                        + productBo.getOrderedOuterQty()
                        * productBo.getOutersize();
                double totalPriceDiscount = (totalQty * productBo.getSrp()) - (totalQty * ((productBo.getSrp() - value)));
                totalPriceDiscount = SDUtil.formatAsPerCalculationConfig(totalPriceDiscount);

                productBo.setSchemeDiscAmount(productBo.getSchemeDiscAmount() + totalPriceDiscount);
                total = total + totalPriceDiscount;

            } else if (type.equals("PRODUCT_DISC_AMT")) {
                value = SDUtil.formatAsPerCalculationConfig(value);
                total = total + value;

            }

        }

        return total;
    }

    /**
     * Calculate discount(Price/percentage) value batch wise
     *
     * @param productBO Buy product BO
     * @param value     Value of discount type
     * @param type      Discount type
     * @return Total discount value
     */
    private double calculateDiscountValueBatchWise(ProductMasterBO productBO,
                                                   double value, String type) {
        ArrayList<ProductMasterBO> batchList = bModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());

        double totalDisPriceValue = 0.0;
        if (batchList != null) {
            for (ProductMasterBO batchProductBo : batchList) {
                if (batchProductBo.getOrderedPcsQty() > 0
                        || batchProductBo.getOrderedCaseQty() > 0
                        || batchProductBo.getOrderedOuterQty() > 0) {
                    double totalValue;
                    if (batchProductBo.getNetValue() > 0) {
                        totalValue = batchProductBo.getNetValue();
                    } else {
                        totalValue = batchProductBo.getOrderedPcsQty()
                                * batchProductBo.getSrp()
                                + batchProductBo.getOrderedCaseQty()
                                * batchProductBo.getCsrp()
                                + batchProductBo.getOrderedOuterQty()
                                * batchProductBo.getOsrp();
                    }


                    if (type.equals(SCHEME_PERCENTAGE)) {

                        double totalPercentageValue = totalValue * value / 100;
                        batchProductBo.setSchemeDiscAmount(batchProductBo.getSchemeDiscAmount() + totalPercentageValue);

                        totalPercentageValue = SDUtil.formatAsPerCalculationConfig(totalPercentageValue);
                        totalDisPriceValue = totalDisPriceValue + totalPercentageValue;

                        if (batchProductBo.getNetValue() > 0) {
                            batchProductBo
                                    .setNetValue(batchProductBo
                                            .getNetValue()
                                            - totalPercentageValue);

                        } else {
                            batchProductBo.setNetValue(totalValue
                                    - totalPercentageValue);
                        }
                        if (batchProductBo.getLineValueAfterSchemeApplied() > 0) {
                            batchProductBo
                                    .setLineValueAfterSchemeApplied(batchProductBo
                                            .getLineValueAfterSchemeApplied()
                                            - totalPercentageValue);

                        } else {
                            batchProductBo.setLineValueAfterSchemeApplied(totalValue
                                    - totalPercentageValue);
                        }

                    } else if (type.equals(SCHEME_PRICE)) {
                        int totalQty = batchProductBo.getOrderedPcsQty()
                                + batchProductBo.getOrderedCaseQty()
                                * productBO.getCaseSize()
                                + batchProductBo.getOrderedOuterQty()
                                * productBO.getOutersize();
                        double totalPriceValue = totalQty * value;

                        batchProductBo.setSchemeDiscAmount(batchProductBo.getSchemeDiscAmount() + totalPriceValue);

                        totalPriceValue = SDUtil.formatAsPerCalculationConfig(totalPriceValue);
                        totalDisPriceValue = totalDisPriceValue + totalPriceValue;

                        if (batchProductBo.getNetValue() > 0) {
                            batchProductBo
                                    .setNetValue(batchProductBo
                                            .getNetValue()
                                            - totalPriceValue);
                        } else {
                            batchProductBo.setNetValue(totalValue
                                    - totalPriceValue);
                        }
                        if (batchProductBo.getLineValueAfterSchemeApplied() > 0) {
                            batchProductBo
                                    .setLineValueAfterSchemeApplied(batchProductBo
                                            .getLineValueAfterSchemeApplied()
                                            - totalPriceValue);

                        } else {
                            batchProductBo.setLineValueAfterSchemeApplied(totalValue
                                    - totalPriceValue);
                        }

                    } else if (type.equals("PRODUCT_DISC")) {
                        double totalPercentageValue = totalValue * value / 100;

                        totalPercentageValue = SDUtil.formatAsPerCalculationConfig(totalPercentageValue);
                        totalDisPriceValue = totalDisPriceValue + totalPercentageValue;

                    } else if (type.equals("PRODUCT_DISC_AMT")) {
                        double totalAmountValue = value;

                        totalAmountValue = SDUtil.formatAsPerCalculationConfig(totalAmountValue);
                        totalDisPriceValue = totalDisPriceValue + totalAmountValue;

                        if (batchProductBo.getNetValue() > 0) {
                            batchProductBo
                                    .setNetValue(batchProductBo
                                            .getNetValue()
                                            - totalAmountValue);
                        } else {
                            batchProductBo.setNetValue(totalValue
                                    - totalAmountValue);
                        }

                    }

                }

            }
        }

        return totalDisPriceValue;
    }


    /**
     * Is value applied between minimum and maximum value
     *
     * @param mSchemeDoneList List of applied schemes
     * @return Is all selected scheme value is between the range or not
     */
    public boolean isValuesAppliedBetweenTheRange(ArrayList<SchemeBO> mSchemeDoneList) {
        if (mSchemeDoneList != null && !mSchemeDoneList.isEmpty())
            for (SchemeBO schemeBO : mSchemeDoneList) {
                if (schemeBO != null) {

                    if (schemeBO.isPriceTypeSeleted()) {

                        if (!(SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedPrice(), 2, 0)) >= SDUtil.convertToDouble(SDUtil.format(schemeBO.getActualPrice(), 2, 0))
                                && SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedPrice(), 2, 0)) <= SDUtil.convertToDouble(SDUtil.format(schemeBO.getMaximumPrice(), 2, 0))
                                && SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedPrice(), 2, 0)) > 0)) {
                            return false;
                        }

                    } else if (schemeBO.isAmountTypeSelected()) {
                        if (!(SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedAmount(), 2, 0)) >= SDUtil.convertToDouble(SDUtil.format(schemeBO.getMinimumAmount(), 2, 0))
                                && SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedAmount(), 2, 0)) <= SDUtil.convertToDouble(SDUtil.format(schemeBO.getMaximumAmount(), 2, 0))
                                && SDUtil.convertToDouble(SDUtil.format(schemeBO.getSelectedAmount(), 2, 0)) > 0)) {
                            return false;
                        }

                    } else if (schemeBO.isDiscountPrecentSelected()) {
                        if (!(schemeBO.getSelectedPrecent() >= schemeBO.getMinimumPrecent()
                                && schemeBO.getSelectedPrecent() <= schemeBO
                                .getMaximumPrecent() && schemeBO.getSelectedPrecent() > 0)) {
                            return false;
                        }
                    }


                }

            }
        return true;

    }

    /**
     * Checking at least minimum offered quantity is entered or not
     *
     * @param mSchemeBO         Scheme Object
     * @param mFreeProductsList Free product list
     * @return Invalid group name or 0 if all groups are valid
     */
    public String isEnteredMinimumOffered(SchemeBO mSchemeBO, List<SchemeProductBO> mFreeProductsList
    ) {

        ArrayList<String> mFreeGroupNameList = getFreeGroupNameListBySchemeID().get(mSchemeBO.getSchemeId());

        if (mSchemeBO.isSihAvailableForFreeProducts() || !bModel.configurationMasterHelper.IS_INVOICE) {

            if (mSchemeBO.getFreeType().equals(AND_LOGIC) || mSchemeBO.getFreeType().equals(ONLY_LOGIC)) {

                String tempGroupName = "";

                if (mFreeGroupNameList != null) {
                    for (String groupName : mFreeGroupNameList) {

                        int totalFreeQty = 0;
                        int anyLogicMinimumCount = 0;

                        for (SchemeProductBO schemeProductBo : mFreeProductsList) {
                            if (groupName.equals(schemeProductBo.getGroupName())) {

                                if (schemeProductBo.getGroupLogic().equals(
                                        AND_LOGIC) || schemeProductBo.getGroupLogic().equals(ONLY_LOGIC)) {

                                    if (schemeProductBo.getQuantitySelected() < schemeProductBo
                                            .getQuantityActualCalculated()) {

                                        return schemeProductBo.getGroupName();
                                    }

                                } else if (schemeProductBo.getGroupLogic()
                                        .equals(ANY_LOGIC)) {

                                    totalFreeQty = totalFreeQty
                                            + schemeProductBo
                                            .getQuantitySelected();

                                    tempGroupName = schemeProductBo.getGroupName();

                                    if (totalFreeQty >= schemeProductBo
                                            .getQuantityActualCalculated()) {
                                        anyLogicMinimumCount = anyLogicMinimumCount + 1;

                                    }

                                }
                            }

                        }
                        if (getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + groupName) != null) {
                            if (getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + groupName)
                                    .equals(ANY_LOGIC)) {
                                if (anyLogicMinimumCount == 0) {
                                    return tempGroupName;
                                }
                            }
                        }

                    }

                }
            } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {

                String type = "";
                for (SchemeProductBO schemeProductBO : mFreeProductsList) {
                    if (schemeProductBO.getQuantitySelected() > 0) {
                        type = schemeProductBO.getGroupName();
                        break;
                    }
                }
                //
                String tempGroupName = "";
                if (!type.equals("")) {

                    int totalFreeQty = 0;
                    int anyLogicMinimumCount = 0;
                    for (SchemeProductBO schemeProductBo : mFreeProductsList) {
                        if (type.equals(schemeProductBo.getGroupName())) {
                            if (schemeProductBo.getGroupLogic().equals(
                                    AND_LOGIC) || schemeProductBo.getGroupLogic().equals(ONLY_LOGIC)) {

                                if (schemeProductBo.getQuantitySelected() < schemeProductBo
                                        .getQuantityActualCalculated()) {
                                    return schemeProductBo.getGroupName();
                                }

                            } else if (schemeProductBo.getGroupLogic()
                                    .equals(ANY_LOGIC)) {
                                totalFreeQty = totalFreeQty
                                        + schemeProductBo.getQuantitySelected();
                                tempGroupName = schemeProductBo.getGroupName();
                                if (totalFreeQty >= schemeProductBo
                                        .getQuantityActualCalculated()) {
                                    anyLogicMinimumCount = anyLogicMinimumCount + 1;

                                }

                            }
                        }

                    }
                    if (getGroupBuyTypeByGroupName().get(mSchemeBO.getSchemeId() + type)
                            .equals(ANY_LOGIC)) {
                        if (anyLogicMinimumCount == 0) {
                            return tempGroupName;
                        }
                    }

                } else {
                    return "";
                }

            }
        }

        return "0";

    }


    /**
     * Checking entered quantity exceeds maximum offered or not
     *
     * @param mSchemeBO       Scheme object
     * @param schemeProductBo Scheme Product BO
     * @param qtyEntered      quantity entered
     * @return is Exceeds
     */
    public boolean isEnteredQuantityExceedsMaximumOffered(SchemeBO mSchemeBO, SchemeProductBO schemeProductBo,
                                                          int qtyEntered, List<SchemeProductBO> mFreeProductsList) {

        if (mSchemeBO.getFreeType().equals(AND_LOGIC) || mSchemeBO.getFreeType().equals(ONLY_LOGIC)) {

            if (schemeProductBo.getGroupLogic().equals(AND_LOGIC) || schemeProductBo.getGroupLogic().equals(ONLY_LOGIC)) {

                return (qtyEntered > schemeProductBo.getQuantityMaxiumCalculated());

            } else if (schemeProductBo.getGroupLogic().equals(ANY_LOGIC)) {

                int totalFreeQty = qtyEntered;
                for (SchemeProductBO schemePrtBO : mFreeProductsList) {
                    if (schemeProductBo.getGroupName().equals(schemePrtBO.getGroupName())) {

                        if (!schemeProductBo.getProductId().equals(schemePrtBO.getProductId())) {
                            totalFreeQty = totalFreeQty + schemePrtBO.getQuantitySelected();
                        }

                    }
                }

                return (totalFreeQty > schemeProductBo.getQuantityMaxiumCalculated());
            }
        } else if (mSchemeBO.getFreeType().equals(ANY_LOGIC)) {

            boolean isOtherChildSchemeAlreadyEntered = false;
            for (SchemeProductBO schemeProductBO : mFreeProductsList) {
                if (!schemeProductBo.getGroupName().equals(schemeProductBO.getGroupName())) {
                    if (schemeProductBO.getQuantitySelected() > 0) {
                        isOtherChildSchemeAlreadyEntered = true;
                    }
                }
            }

            if (!isOtherChildSchemeAlreadyEntered) {

                if (schemeProductBo.getGroupLogic().equals(AND_LOGIC) || schemeProductBo.getGroupLogic().equals(ONLY_LOGIC)) {
                    return (qtyEntered > schemeProductBo.getQuantityMaxiumCalculated());

                } else if (schemeProductBo.getGroupLogic().equals(ANY_LOGIC)) {

                    int totalFreeQty = qtyEntered;
                    for (SchemeProductBO schemePrtBO : mFreeProductsList) {
                        if (schemeProductBo.getGroupName().equals(schemePrtBO.getGroupName())) {

                            if (!schemeProductBo.getProductId().equals(schemePrtBO.getProductId())) {
                                totalFreeQty = totalFreeQty + schemePrtBO.getQuantitySelected();
                            }

                        }
                    }

                    return (totalFreeQty > schemeProductBo.getQuantityMaxiumCalculated());

                }

            } else {
                return (qtyEntered > 0);
            }
        }

        return true;
    }


    /**
     * Calculating scheme apply count based on every UOM qty given
     *
     * @param schemeBO Scheme object to calculate
     * @return Applied count
     */
    private int calculateApplyCountBasedOnEveryUOM(SchemeBO schemeBO) {
        int count = 0;
        int totalConvertedQty = 0;
        List<SchemeProductBO> schemeProductList = schemeBO.getBuyingProducts();
        for (SchemeProductBO schemeProductBO : schemeProductList) {
            ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
            if (productBO != null) {
                int totalOrderQty = (productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize()));
                if (schemeBO.getEveryUomId() == productBO.getCaseUomId()) {
                    if (productBO.getCaseSize() != 0) {
                        totalConvertedQty = totalConvertedQty + (totalOrderQty / productBO.getCaseSize());
                    }
                } else if (schemeBO.getEveryUomId() == productBO.getOuUomid()) {
                    if (productBO.getOutersize() != 0) {
                        totalConvertedQty = totalConvertedQty + (totalOrderQty / productBO.getOutersize());
                    }
                } else {
                    totalConvertedQty = totalConvertedQty + totalOrderQty;
                }
            }
        }
        count = count + (totalConvertedQty / schemeBO.getEveryQty());
        return count;
    }

    /**
     * Method to use update free product empty bottle return
     */
    public void updateFreeProductBottleReturn() {
        if (mAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mAppliedSchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    List<SchemeProductBO> freeProductList = schemeBO
                            .getFreeProducts();
                    for (SchemeProductBO schemeProductBO : freeProductList) {
                        if (schemeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bModel.productHelper
                                    .getProductMasterBOById(schemeProductBO
                                            .getProductId());
                            if (productBO != null && bModel.productHelper.getBomMaster() != null && bModel.productHelper.getBomMaster().size() > 0) {
                                for (BomMasterBO bomMasterBo : bModel.productHelper
                                        .getBomMaster()) {

                                    if (productBO.getProductID().equals(
                                            bomMasterBo.getPid())) {

                                        for (BomBO bomBo : bomMasterBo
                                                .getBomBO()) {

                                            if (bomBo.getUomID() == productBO
                                                    .getPcUomid()
                                                    && productBO.getPcUomid() == schemeProductBO
                                                    .getUomID())
                                                bomBo.setTotalQty(bomBo
                                                        .getQty()
                                                        * schemeProductBO
                                                        .getQuantitySelected());
                                            else if (bomBo.getUomID() == productBO
                                                    .getOuUomid()
                                                    && productBO.getOuUomid() == schemeProductBO
                                                    .getUomID())
                                                bomBo.setTotalQty(bomBo
                                                        .getQty()
                                                        * schemeProductBO
                                                        .getQuantitySelected());

                                            else if (bomBo.getUomID() == productBO
                                                    .getCaseUomId()
                                                    && productBO.getCaseUomId() == schemeProductBO
                                                    .getUomID())
                                                bomBo.setTotalQty(bomBo
                                                        .getQty()
                                                        * schemeProductBO
                                                        .getQuantitySelected());

                                            if (bModel.productHelper
                                                    .getBomReturnProducts() != null) {
                                                for (BomReturnBO returnBo : bModel.productHelper
                                                        .getBomReturnProducts()) {
                                                    if (bomBo.getbPid().equals(
                                                            returnBo.getPid())) {
                                                        returnBo.setLiableQty(returnBo
                                                                .getLiableQty()
                                                                + bomBo.getTotalQty());
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Load Scheme Report
     *
     * @param mContext Current Context
     * @param id       Invoice.Order Id
     * @param flag     - true - invoice,false - order
     */
    public void loadSchemeReportDetails(Context mContext, String id, boolean flag) {
        mAppliedSchemeList = new ArrayList<>();
        SchemeProductBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct SD.schemeid,MIN(SD.productid),SFD.freeproductID,SFD.FreeQty,");
            sb.append("SFD.uomID,SFD.batchid from SchemeDetail SD  inner join SchemeFreeproductDetail SFD ");
            sb.append("on SFD.schemeid=SD.schemeID where ");
            if (flag) { // invoice report
                sb.append("SFD.invoiceid=" + bModel.QT(id));
            } else {// order report
                sb.append("SFD.orderid=" + bModel.QT(id));
            }
            sb.append(" AND SD.SCHEMETYPE = " + bModel.QT(SCHEME_FREE_PRODUCT));
            sb.append("GROUP BY  SD.schemeid,SFD.freeproductID,SFD.FreeQty,SFD.uomID,SFD.batchid ");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    schemeProductBO = new SchemeProductBO();
                    schemeProductBO.setSchemeId(c.getString(0));
                    schemeProductBO.setProductId(c.getString(2));
                    schemeProductBO.setQuantitySelected(c.getInt(3));
                    schemeProductBO.setUomID(c.getInt(4));
                    String productId = c.getString(1);
                    // productBo is buy product object
                    ProductMasterBO productBO = bModel.productHelper
                            .getProductMasterBOById(productId);
                    if (productBO != null) {
                        if (productBO.getSchemeProducts() == null) {
                            productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
                        }
                        // scheme product is free product object
                        ProductMasterBO schemeProduct = bModel.productHelper
                                .getProductMasterBOById(schemeProductBO
                                        .getProductId());
                        if (schemeProduct != null) {
                            schemeProductBO.setProductName(schemeProduct
                                    .getProductShortName());
                            schemeProductBO.setProductFullName(schemeProduct
                                    .getProductName());
                            productBO.getSchemeProducts().add(schemeProductBO);
                        }
                    }
                }
            }

            sb = new StringBuffer();
            sb.append("select schemeid,productid,schemetype,value,amount from SchemeDetail where ");
            if (flag) { // invoice report
                sb.append(" invoiceid=").append(bModel.QT(id));
            } else {// order report
                sb.append(" orderid=" + bModel.QT(id));
            }
            ArrayList<String> schemeIdList = new ArrayList<>();

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String schemeId = c.getString(0);
                    SchemeBO schemeBO = getSchemeById().get(schemeId);
                    if (schemeBO != null) {
                        List<SchemeProductBO> buyList = schemeBO.getBuyingProducts();
                        String productId = c.getString(1);
                        String schemeType = c.getString(2);
                        double discountValue = c.getDouble(4);

                        if (buyList != null) {
                            if (schemeType.equals(SCHEME_AMOUNT)) {
                                schemeBO.setAmountTypeSelected(true);
                                //amount column only have  total scheme amount
                                schemeBO.setSelectedAmount(c.getDouble(4));
                            } else {
                                for (SchemeProductBO schProductBO : buyList) {
                                    if (productId.equals(schProductBO.getProductId())) {
                                        if (schemeType.equals(SCHEME_PERCENTAGE)) {
                                            schemeBO.setDiscountPrecentSelected(true);
                                            schProductBO.setDiscountValue(discountValue);

                                        } else if (schemeType.equals(SCHEME_PRICE)) {
                                            schemeBO.setPriceTypeSeleted(true);
                                            schProductBO.setDiscountValue(discountValue);
                                        }
                                    }

                                }
                            }
                        }
                        if (!schemeIdList.contains(schemeId)) {
                            mAppliedSchemeList.add(schemeBO);
                            schemeIdList.add(schemeId);
                        }


                    }
                }
            }


            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
        }

    }


    public int getMaximumLineOfSchemeHeight(int schemeWidth, int mParentId) {
        int maximumLength = schemeWidth / 10;
        int maximumLengthOfSchemeName = 0;
        ArrayList<String> schemeIDList = mSchemeIDListByParentID
                .get(mParentId);
        for (String schemeId : schemeIDList) {
            SchemeBO schemeBO = mSchemeById.get(schemeId);
            if (schemeBO != null) {
                if (schemeBO.getScheme().length() > maximumLengthOfSchemeName) {
                    maximumLengthOfSchemeName = schemeBO.getScheme().length();
                }

            }
        }
        if (maximumLengthOfSchemeName > 0) {
            if (maximumLength == 0)
                maximumLength = 1;
            return maximumLengthOfSchemeName / maximumLength;
        }

        return 1;
    }

    public void clearOffInvoiceSchemeList() {
        mOffInvoiceAppliedSchemeList = new ArrayList<>();

        for (Iterator<SchemeBO> iterator = getAppliedSchemeList().iterator(); iterator.hasNext(); ) {
            SchemeBO schemeBO = iterator.next();
            if (schemeBO.getIsOnInvoice() == 0) {
                if (schemeBO.isSihAvailableForFreeProducts()) {
                    mOffInvoiceAppliedSchemeList.add(schemeBO);
                }
                iterator.remove();
            }
        }

    }

    public ArrayList<SchemeBO> getOffInvoiceAppliedSchemeList() {
        if (mOffInvoiceAppliedSchemeList != null) {
            return mOffInvoiceAppliedSchemeList;
        }
        return new ArrayList<>();
    }

    /**
     * Loading scheme accumulation report
     *
     * @param id   - it acts either invoiceId or orderId
     * @param flag - true - invoice,false - order     *
     */
    public ArrayList<SchemeProductBO> downLoadAccumulationSchemeDetailReport(Context mContext, String id, boolean flag) {
        ArrayList<SchemeProductBO> mAccumulationFreePrdList = null;
        SchemeProductBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("select distinct SFD.SchemeID,SFD.freeproductID,SFD.FreeQty,");
            sb.append("SFD.uomID,SFD.batchid from SchemeFreeproductDetail  SFD inner join AccumulationSchemeFreeIssues ASF ");
            sb.append("on ASF.SlabId=SFD.SchemeID " +
                    "and ASF.ProductId=SFD.FreeProductID" +
                    " where ");
            if (flag) { // invoice report
                sb.append("SFD.invoiceid=" + bModel.QT(id));
            } else {// order report
                sb.append("SFD.orderid=" + bModel.QT(id));
            }

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                mAccumulationFreePrdList = new ArrayList<>();
                while (c.moveToNext()) {
                    schemeProductBO = new SchemeProductBO();
                    schemeProductBO.setSchemeId(c.getString(0));
                    schemeProductBO.setProductId(c.getString(1));
                    schemeProductBO.setQuantitySelected(c.getInt(2));
                    schemeProductBO.setUomID(c.getInt(3));

                    // scheme product is free product object

                    ProductMasterBO schemeProduct = bModel.productHelper
                            .getProductMasterBOById(schemeProductBO
                                    .getProductId());
                    if (schemeProduct != null) {
                        schemeProductBO.setProductName(schemeProduct
                                .getProductShortName());
                        schemeProductBO.setProductFullName(schemeProduct
                                .getProductName());
                    }

                    mAccumulationFreePrdList.add(schemeProductBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
        }
        return mAccumulationFreePrdList;

    }

    /**
     * Loading Next available slab
     *
     * @param schemeId     schemeId
     * @param type         type
     * @param channelId    channel id
     * @param subChannelId sub channel id
     * @param productID    product id
     * @param quantity     quantity
     */
    public void loadSchemePromotion(Context mContext, String schemeId, String type,
                                    String channelId, String subChannelId, String productID,
                                    int quantity) {
        if (mSchemePromotion == null) {
            mSchemePromotion = new ArrayList<>();
        } else {
            mSchemePromotion.clear();
        }
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        StringBuilder sb = new StringBuilder("");
        if ("ANY".equalsIgnoreCase(type)) {

            Cursor c = db
                    .selectSQL("SELECT ProductID, BuyQty FROM SchemeBuyMaster WHERE SchemeID = '"
                            + schemeId + "'");
            if (c != null && c.getCount() > 0) {

                sb.append(" AND BD.ProductID IN (");
                while (c.moveToNext()) {
                    sb.append("'");
                    sb.append(c.getString(0));
                    sb.append("'");
                    sb.append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append(") ");
                sb.append("AND TYPE = 'ANY' ");
                c.close();
            }

        } else {
            sb = new StringBuilder(" AND BD.ProductID = '" + productID
                    + "' AND TYPE = 'ONLY' ");
        }

        Cursor c = db
                .selectSQL("SELECT SM.SchemeID, SM.Description, SM.Type, SM.ShortName, SM.ChannelID, SM.SubChannelID, "
                        + "BD.ProductID, PM.PName, BD.BuyQty, FD.FreeQty, FD.MaxQty, FD.Rate, FD.MaxRate, FD.SlabMaxValue FROM SchemeMaster SM "
                        + "INNER JOIN  SchemeBuyMaster BD ON BD.SchemeID = SM.SchemeID  INNER JOIN ProductMaster PM ON BD.ProductID = PM.PID "
                        + "INNER JOIN SchemeFreeMaster FD ON FD.FreeProductID = BD.ProductID AND FD.SchemeID = BD.SchemeID WHERE SM.ChannelID = '"
                        + channelId
                        + "' AND "
                        + "SM.SubChannelID = '"
                        + subChannelId
                        + "'"
                        + sb
                        + "AND BD.BuyQty > "
                        + quantity
                        + " ORDER BY SM.SchemeID, BD.ProductID ASC, BD.BuyQty DESC");

        if (c != null && c.getCount() > 0) {
            SchemeBO schemeBO = new SchemeBO();

            while (c.moveToNext()) {
                schemeBO.setSchemeId(c.getString(0));
                schemeBO.setSchemeDescription(c.getString(1));
                schemeBO.setParentLogic(c.getString(2));
                if (c.getString(3) != null) {
                    schemeBO.setSchemeDescription(c.getString(3));
                }
                schemeBO.setChannelId(c.getString(4));
                schemeBO.setSubChannelId(c.getString(5));
                schemeBO.setSchemeParentName(c.getString(7));
                schemeBO.setSelectedQuantity(c.getInt(8)); // Buy Qty
                schemeBO.setActualQuantity(c.getInt(9)); // Min Qty
                schemeBO.setMaximumQuantity(c.getInt(10)); // Max Qty
                schemeBO.setActualPrice(c.getInt(11)); // Min Disc Rate
                schemeBO.setMaximumPrice(c.getInt(12)); // Max Disc Rate
                schemeBO.setMaximumSlab(c.getInt(13));
                mSchemePromotion.add(schemeBO);
            }
            c.close();
        }
        db.closeDB();

    }

    public List<SchemeBO> getSchemePromotion() {
        return mSchemePromotion;
    }

    private List<SchemeBO> mSchemePromotion;


    //// Display Scheme ///


    /**
     * Download display scheme
     *
     * @param mContext Current context
     */
    public void downloadDisplayScheme(Context mContext) {
        mDisplaySchemeMasterList = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("Select DM.schemeId,schemeShortDesc,schemeDesc,DisplayPeriodStart,DisplayPeriodEnd,BookingPeriodStart,BookingPeriodEnd");
            sb.append(",PayoutFrequency,qualifiers from DisplaySchemeMaster DM INNER JOIN DisplaySchemeMapping DMP ON DMP.schemeId=DM.schemeId");
            sb.append(" WHERE DMP.retailerId=" + bModel.getRetailerMasterBO().getRetailerID());

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                SchemeBO schemeBO;
                while (c.moveToNext()) {
                    schemeBO = new SchemeBO();
                    schemeBO.setSchemeId(c.getString(0));
                    schemeBO.setSchemeParentName(c.getString(1));
                    schemeBO.setSchemeDescription(c.getString(2));
                    schemeBO.setDisplayPeriodStart(c.getString(3));
                    schemeBO.setDisplayPeriodEnd(c.getString(4));
                    schemeBO.setBookingPeriodStart(c.getString(5));
                    schemeBO.setBookingPeriodEnd(c.getString(6));
                    schemeBO.setPayoutFrequency(c.getString(7));
                    schemeBO.setQualifier(c.getString(8));
                    mDisplaySchemeMasterList.add(schemeBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);
        }
    }

    public ArrayList<SchemeBO> getDisplaySchemeSlabs() {
        if (mDisplaySchemeSlabs == null) {
            mDisplaySchemeSlabs = new ArrayList<>();
        }
        return mDisplaySchemeSlabs;
    }

    public ArrayList<SchemeBO> getDisplaySchemeMasterList() {
        return mDisplaySchemeMasterList;
    }

    /**
     * Download display scheme applicable products
     */
    public void downloadDisplaySchemeSlabs(Context mContext) {
        mDisplaySchemeSlabs = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("Select A.slabid,A.slabDesc,A.getType,A.value,A.schemeid from DisplaySchemeSlab A");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                SchemeBO schemeBO;
                while (c.moveToNext()) {
                    schemeBO = new SchemeBO();
                    schemeBO.setSchemeId(c.getString(0));
                    schemeBO.setSchemeDescription(c.getString(1));
                    schemeBO.setGetType(c.getString(2));
                    schemeBO.setDisplaySchemeValue(c.getString(3));
                    schemeBO.setParentId(c.getInt(4));

                    mDisplaySchemeSlabs.add(schemeBO);
                }
            }

            //update free products
            if (mDisplaySchemeSlabs.size() > 0) {
                sb = new StringBuffer();
                sb.append("Select slabid,productid,qty,uomid,UM.listname from DisplaySchemeSlabFOC A");
                sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON A.uomid = UM.ListId ");

                c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    SchemeProductBO productBO;
                    while (c.moveToNext()) {

                        for (SchemeBO bo : mDisplaySchemeSlabs) {
                            if (bo.getSchemeId().equals(c.getString(0))) {

                                productBO = new SchemeProductBO();
                                productBO.setProductId(c.getString(1));
                                productBO.setProductName(bModel.productHelper.getProductMasterBOById(c.getString(1)).getProductName());
                                productBO.setQuantityMaximum(c.getInt(2));
                                productBO.setUomID(c.getInt(3));
                                productBO.setUomDescription(c.getString(4));

                                if (bo.getFreeProducts() == null) {
                                    bo.setFreeProducts(new ArrayList<SchemeProductBO>());
                                }
                                bo.getFreeProducts().add(productBO);
                            }
                        }
                    }
                }
            }


            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);
        }

    }

    /**
     * Download display scheme applicable products
     */
    public ArrayList<String> downloadDisplaySchemeProducts(Context mContext, String schemeId) {
        ArrayList<String> mProductList = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("Select productId from DisplaySchemeProduct");
            sb.append(" WHERE schemeid=" + schemeId);

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mProductList.add(bModel.productHelper.getProductMasterBOById((c.getString(0))).getProductName());
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);
        }

        return mProductList;
    }

    /**
     * Saving display scheme in transaction table
     *
     * @param mContext Current context
     * @return Is Saved
     */
    public boolean saveDisplayScheme(Context mContext) {
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor cursor = db
                    .selectSQL("SELECT Tid FROM DisplaySchemeEnrollmentHeader WHERE distributorId = "
                            + bModel.userMasterHelper.getUserMasterBO().getDistributorid() + " and retailerId=" + bModel.getRetailerMasterBO().getRetailerID());
            if (cursor.getCount() > 0) {
                db.deleteSQL(DataMembers.tbl_display_scheme_enrollment_header,
                        "distributorId=" + bModel.userMasterHelper.getUserMasterBO().getDistributorid()
                                + " and retailerId=" + bModel.getRetailerMasterBO().getRetailerID()
                                + " and upload='N'", false);
            }
            cursor.close();


            String columns = "Tid,Date,UserId,DistributorId,RetailerId,SchemeId,SlabId";
            StringBuffer sb;
            String id = bModel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            for (SchemeBO schemeBO : getDisplaySchemeSlabs()) {
                if (schemeBO.isSchemeSelected()) {

                    sb = new StringBuffer();
                    sb.append(id + ",");
                    sb.append(bModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ",");
                    sb.append(bModel.userMasterHelper.getUserMasterBO().getUserid() + ",");
                    sb.append(bModel.userMasterHelper.getUserMasterBO().getDistributorid() + ",");
                    sb.append(bModel.getRetailerMasterBO().getRetailerID() + ",");
                    sb.append(schemeBO.getParentId() + ",");
                    sb.append(schemeBO.getSchemeId());

                    db.insertSQL(DataMembers.tbl_display_scheme_enrollment_header, columns,
                            sb.toString());
                }
            }

            return true;
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);

            return false;
        }


    }


    public ArrayList<SchemeBO> getDisplaySchemeTrackingList() {
        if (mDisplaySchemeTrackingList == null) {
            mDisplaySchemeTrackingList = new ArrayList<>();
        }
        return mDisplaySchemeTrackingList;
    }

    /**
     * Download display scheme tracking masters
     *
     * @param mContext Current context
     */
    public void downloadDisplaySchemeTracking(Context mContext) {
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            String query = "Select distinct schemeId,schemeDesc,slabId,slabDesc from DisplaySchemeTrackingMaster";
            query += " WHERE retailerId=" + bModel.getRetailerMasterBO().getRetailerID();

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                mDisplaySchemeTrackingList = new ArrayList<>();
                SchemeBO schemeBO;
                while (c.moveToNext()) {
                    schemeBO = new SchemeBO();
                    schemeBO.setParentId(c.getInt(0));
                    schemeBO.setSchemeParentName(c.getString(1));
                    schemeBO.setSchemeId(c.getString(2));
                    schemeBO.setSchemeDescription(c.getString(3));

                    mDisplaySchemeTrackingList.add(schemeBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);
        }

    }

    /**
     * Saving display scheme tracking detail in transaction table
     *
     * @param mContext Current context
     * @return Is Saved
     */
    public boolean saveDisplaySchemeTracking(Context mContext) {
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor cursor = db
                    .selectSQL("SELECT Tid FROM DisplaySchemeTrackingHeader WHERE distributorId = "
                            + bModel.userMasterHelper.getUserMasterBO().getDistributorid() + " and retailerId=" + bModel.getRetailerMasterBO().getRetailerID());
            if (cursor.getCount() > 0) {
                db.deleteSQL(DataMembers.tbl_display_scheme_tracking_header,
                        "distributorId=" + bModel.userMasterHelper.getUserMasterBO().getDistributorid()
                                + " and retailerId=" + bModel.getRetailerMasterBO().getRetailerID()
                                + " and upload='N'", false);
            }
            cursor.close();

            String columns = "Tid,Date,UserId,DistributorId,RetailerId,SchemeId,SlabId,IsAvailable";
            StringBuffer sb;
            String id = bModel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            for (SchemeBO schemeBO : getDisplaySchemeTrackingList()) {
                sb = new StringBuffer();
                sb.append(id + ",");
                sb.append(bModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ",");
                sb.append(bModel.userMasterHelper.getUserMasterBO().getUserid() + ",");
                sb.append(bModel.userMasterHelper.getUserMasterBO().getDistributorid() + ",");
                sb.append(bModel.getRetailerMasterBO().getRetailerID() + ",");
                sb.append(schemeBO.getParentId() + ",");
                sb.append(schemeBO.getSchemeId() + ",");
                if (schemeBO.isSchemeSelected())
                    sb.append("1");
                else sb.append("0");

                db.insertSQL(DataMembers.tbl_display_scheme_tracking_header, columns,
                        sb.toString());
            }

            return true;
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("" + e);

            return false;
        }


    }

    ArrayList<ParentSchemeBO> parentSchemeList = new ArrayList<>();

    public ArrayList<ParentSchemeBO> getParentSchemeList() {
        return parentSchemeList;
    }

    public void setParentSchemeList(ArrayList<ParentSchemeBO> parentSchemeList) {
        this.parentSchemeList = parentSchemeList;
    }

    public void loadParentSchemInfo(DBUtil db, ArrayList<Integer> schemeList) {

        StringBuilder sb = new StringBuilder();
        String id = "";
        for (Integer scheme : schemeList) {
            id = id + scheme + ",";
        }
        id = id.substring(0, id.length() - 1);
        sb.append("Select distinct SM.parentID, SM.shortName, SM.buyType from SchemeMaster SM where parentID in (" + id + ")");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            parentSchemeList = new ArrayList<>();
            while (c.moveToNext()) {
                ParentSchemeBO schemeBO = new ParentSchemeBO();
                schemeBO.setSchemeID(c.getInt(0));
                schemeBO.setSchemeDesc(c.getString(1));
                schemeBO.setBuyType(c.getString(2));
                schemeBO.setCumulativePurchase(0);
                schemeBO.setCurSlabCumSchAmt(0);
                schemeBO.setCurSlabrsorPer(0);
                schemeBO.setNextSlabBalance(0);
                schemeBO.setNextSlabCumSchAmt(0);
                schemeBO.setNextSlabrsorPer(0);
                parentSchemeList.add(schemeBO);
            }
        }
        c.close();

        setParentSchemeList(parentSchemeList);
    }

    public void resetSchemeQPSList() {
        if (mSchemeList != null) {
            for (SchemeBO scheme : mSchemeList) {
                scheme.setFromQty(0);
                scheme.setToQty(0);
                scheme.setTotalPieceQty(0);
                scheme.setCurrentSlab(false);
                scheme.setNextSlab(false);
                for (SchemeProductBO productBO : scheme.getBuyingProducts()) {
                    productBO.setParentID("");
                    productBO.setGetType("");
                    productBO.setOrderedCasesQty(0);
                    productBO.setCasesPrice(0);
                    productBO.setOrderedPcsQty(0);
                    productBO.setPcsPrice(0);
                    productBO.setIncreasedPcsQty(0);
                    productBO.setIncreasedCasesQty(0);
                }
            }
        }
    }

    public void resetSchemeQPSListforData() {
        if (mSchemeList != null) {
            for (SchemeBO scheme : mSchemeList) {
                scheme.setFromQty(0);
                scheme.setToQty(0);
                scheme.setTotalPieceQty(0);
                scheme.setCurrentSlab(false);
                scheme.setNextSlab(false);
            }
        }
    }

    public HashMap<String, SchemaQPSAchHistoryBO> getmSchemaQPSAchHistoryList() {
        return mSchemaQPSAchHistoryList;
    }

    public void setmSchemaQPSAchHistoryList(HashMap<String, SchemaQPSAchHistoryBO> mSchemaQPSAchHistoryList) {
        this.mSchemaQPSAchHistoryList = mSchemaQPSAchHistoryList;
    }

    public void loadQPSCumulativeAchHistory(DBUtil db) {

        StringBuilder sb = new StringBuilder();
        sb.append("Select distinct SchemeID ,Cumulative_Purchase,CurSlab_Sch_Amt,CurSlab_FlatAmt_Per, " +
                "NextSlab_Balance,NextSlab_Sch_Amt,NextSlab_FlatAmt_Per from SchemaQPSAchHistory");
        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            mSchemaQPSAchHistoryList = new HashMap<>();
            while (c.moveToNext()) {
                SchemaQPSAchHistoryBO schemeBO = new SchemaQPSAchHistoryBO();
                schemeBO.setSchemeID(c.getInt(0));
                schemeBO.setCumulative_Purchase(c.getDouble(1));
                schemeBO.setCurSlab_Sch_Amt(c.getDouble(2));
                schemeBO.setCurSlab_Rs_Per(c.getDouble(3));
                schemeBO.setNextSlab_balance(c.getDouble(4));
                schemeBO.setNextSlab_Sch_Amt(c.getDouble(5));
                schemeBO.setNextSlab_Rs_Per(c.getDouble(6));
                mSchemaQPSAchHistoryList.put(c.getString(0), schemeBO);
            }
        }
        c.close();

        setmSchemaQPSAchHistoryList(mSchemaQPSAchHistoryList);
    }

    public ArrayList<Integer> getmParentIDList() {
        return schemeParentId;
    }
}









