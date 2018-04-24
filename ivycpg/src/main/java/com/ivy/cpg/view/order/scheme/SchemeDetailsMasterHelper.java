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
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SchemeProductBatchQty;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemeDetailsMasterHelper {

    private Context context;
    private BusinessModel bModel;
    private static SchemeDetailsMasterHelper instance = null;

    private static final String TAG = "SchemeDetailsMasterHelper";

    /* SALES_VALUE is used for AmountType(SV) scheme apply */
    private static final String SALES_VALUE = "SV";
    /* QUANTITY_TYPE is used for Quantity Type(QTY) scheme apply */
    private static final String QUANTITY_TYPE = "QTY";

    private static final String ANY_LOGIC = "ANY"; // use ANY type scheme logic
    private static final String AND_LOGIC = "AND"; // use AND type scheme logic
    private static final String ONLY_LOGIC = "ONLY";// use ONLY type scheme

    private static final String SCHEME_AMOUNT = "SCH_AMT";
    private static final String SCHEME_PERCENTAGE = "SCH_PER";
    private static final String SCHEME_PRICE = "SCH_PR";
    private static final String SCHEME_FREE_PRODUCT = "SCH_FPRD";
    private static final String SCHEME_PERCENTAGE_BILL = "BPER";

    private static final String PROCESS_TYPE_MULTIPLE_TIME_FOR_REMAINING = "MTR";
    private static final String PROCESS_TYPE_OTP = "OTP";
    private static final String PROCESS_TYPE_ONE_TIME_WITH_PERCENTAGE = "OTPR";
    private static final String PROCESS_TYPE_MTS = "MTS"; //Mulitple Time in Multiple Slab apply within the same scheme
    private static final String PROCESS_TYPE_PRORATA = "MSP"; //PRORATA-


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


    // List of schemes applied for the current order
    private ArrayList<SchemeBO> mAppliedSchemeList;

    //Display Scheme
    private ArrayList<SchemeBO> mDisplaySchemeMasterList;
    private ArrayList<SchemeBO> mDisplaySchemeSlabs;
    private ArrayList<SchemeBO> mDisplaySchemeTrackingList;


    protected SchemeDetailsMasterHelper(Context context) {
        this.context = context;
        bModel = (BusinessModel) context;
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

    public boolean IS_SCHEME_ON;
    public boolean IS_SCHEME_EDITABLE;
    public boolean IS_SCHEME_SHOW_SCREEN;
    public boolean IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE;
    public boolean IS_SCHEME_SLAB_ON;
    public boolean IS_SCHEME_CHECK;
    public boolean IS_SCHEME_CHECK_DISABLED;
    public boolean IS_SCHEME_ON_MASTER;
    public boolean IS_SCHEME_SHOW_SCREEN_MASTER;

    /**
     * Load All Scheme Configurations
     */
    private void loadSchemeConfigs(Context mContext) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster;
                   // + " WHERE menu_type = 'SCHEME' AND flag='1'";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_ON)) {
                        IS_SCHEME_ON = true;
                        IS_SCHEME_ON_MASTER = true;
                    } else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_EDITABLE))
                        IS_SCHEME_EDITABLE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_SHOW_SCREEN)) {
                        IS_SCHEME_SHOW_SCREEN = true;
                        IS_SCHEME_SHOW_SCREEN_MASTER = true;
                    } else if (c.getString(0).equalsIgnoreCase(CODE_FOC_ACCUMULATION_VALIDATION))
                        IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SCHEME_SLAB_ON))
                        IS_SCHEME_SLAB_ON = true;

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
     * Method to load all scheme related methods
     */
    public void initializeScheme() {

        DBUtil db;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            loadSchemeConfigs(context);

            if (IS_SCHEME_ON_MASTER) {
                RetailerInfo retailerInfo=(RetailerInfo)context;
                int distributorId = bModel.getRetailerMasterBO().getDistributorId();
                String retailerId = retailerInfo.getRetailerId();
                int channelId = bModel.getRetailerMasterBO().getSubchannelid();
                int locationId = bModel.getRetailerMasterBO().getLocationId();
                int accountId = bModel.getRetailerMasterBO().getAccountid();
                int priorityProductId = bModel.getRetailerMasterBO().getPrioriryProductId();
                int userId = bModel.userMasterHelper.getUserMasterBO().getUserid();

                //  loading scheme groups based on retailer attributes
                ArrayList<String> mGroupIdList = downloadValidSchemeGroups(db);
                //  for loading highest slab parent ids
                downloadSchemeParentDetails(db, distributorId, retailerId, channelId, locationId, accountId, priorityProductId, mGroupIdList);
                //  load buy product list
                downloadBuySchemeDetails(db, retailerId, userId, distributorId, channelId, locationId, accountId, priorityProductId, mGroupIdList);
                //  update free product
                downloadFreeProducts(db);

                if (bModel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG || bModel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                    downloadParentIdListByProduct(db, mGroupIdList);
                }

                downloadPeriodWiseScheme(db);
            }
            else {
                setIsScheme(new ArrayList<String>());
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.print("" + e);
        }


    }


    /**
     * Downloading valid scheme groups
     * @param db Database object
     * @return
     */
    private ArrayList<String> downloadValidSchemeGroups(DBUtil db) {

        StringBuilder sb = new StringBuilder();
        ArrayList<String> mGroupIDList=new ArrayList<>();
        ArrayList<String> retailerAttributes = bModel.getAttributeParentListForCurrentRetailer();

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
                            if (!mGroupIDList.contains(lastGroupId+"" + lastSchemeId)) {
                                mGroupIDList.add(lastGroupId+"" + lastSchemeId);
                            }
                        }

                    }

                    if (isSchemeApplicable(c.getInt(0), c.getInt(1), c.getInt(3)))
                        isGroupSatisfied = true;
                    else
                        isGroupSatisfied = false;

                    lastSchemeId = c.getInt(0);
                    lastGroupId = c.getInt(1);


                }

            }
            if (isGroupSatisfied) {
                if (!mGroupIDList.contains(lastGroupId + "" + lastSchemeId)) {
                    mGroupIDList.add(lastGroupId + "" + lastSchemeId);
                }
            }

        }
        c.close();

        return mGroupIDList;


    }



    private boolean isSchemeApplicable(int schemeId, int groupId, int parentId) {


        DBUtil db;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            StringBuilder sb = new StringBuilder();

            sb.append("select Distinct schemeid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SchemeAttributeMapping  SAM" +
                    " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.AttributeId" +
                    " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID ");
            sb.append("where schemeid=" + schemeId + " and groupid=" + groupId + " and SAM.attributeid in(select RA.attributeid from RetailerAttribute RA" +
                    " inner join EntityAttributeMaster EA on EA.Attributeid = RA.Attributeid and EA.PArentid=" + parentId +
                    " where retailerid = " + bModel.getRetailerMasterBO().getRetailerID() + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                return true;
            }
            c.close();
            db.closeDB();

        } catch (Exception ex) {

            Commons.printException(ex);
        }
        return false;
    }


    /**
     * Preparing scheme list by parentId..
     * List will be ordered based on total Buy quantity(highest slab on top).
     * @param db
     * @param distributorId
     * @param retailerId
     * @param channelId
     * @param locationId
     * @param accountId
     * @param priorityProductId
     * @return
     */
    public void downloadSchemeParentDetails(DBUtil db,int distributorId,String retailerId,int channelId
            ,int locationId,int accountId,int priorityProductId,ArrayList<String> mGroupIDList) {
        mSchemeIDListByParentID = new HashMap<>();
        mParentIDList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("select SM.schemeid,SM.parentid,sum(case when uomid is 0 then sbm.buyqty when SBM.uomid");
        sb.append(" is PM.duomid then  sbm.buyQty*pm.duomQty  when SBM.uomid is PM.douomid then ");
        sb.append("sbm.buyQty*pm.douomqty else buyQty  end ) as totalQty,SCM.groupId,");
        sb.append(" Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag");

        sb.append(" from schemebuymaster sbm   inner join productmaster PM  on sbm.productid =pm.pid");
        sb.append(" inner join schememaster SM on SM.schemeid=sbm.schemeid");
        sb.append(" inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");

        sb.append(" where SCM.distributorid in(0," + distributorId + ")");
        sb.append(" and SCM.RetailerId in(0," +retailerId + ")");
        sb.append(" and SCM.channelid in(0," +channelId + ")");
        sb.append(" and SCM.locationid in(0," + locationId + ")");
        sb.append(" and SCM.accountid in(0," +accountId + ")");
        sb.append(" and SCM.PriorityProductId in(0," + priorityProductId + ")");
        sb.append(" group by sm.schemeid,SM.parentid order by SM.parentid,totalQty desc");


        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            int parentid = 0;
            ArrayList<String> schemeIdList = new ArrayList<>();
            while (c.moveToNext()) {
                String schemeid = c.getString(0);

                if (c.getInt(4) == 0 || (c.getInt(4) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(3) + c.getString(1)))) {

                    if (parentid != c.getInt(1)) {
                        if (parentid != 0) {
                            mParentIDList.add(parentid);
                            mSchemeIDListByParentID.put(parentid, schemeIdList);
                            schemeIdList = new ArrayList<>();
                            schemeIdList.add(schemeid);
                            parentid = c.getInt(1);

                        } else {
                            schemeIdList.add(schemeid);
                            parentid = c.getInt(1);

                        }
                    } else {
                        schemeIdList.add(schemeid);

                    }
                }
            }
            if (schemeIdList.size() > 0) {
                mSchemeIDListByParentID.put(parentid, schemeIdList);
                mParentIDList.add(parentid);
            }
        }
        c.close();


    }

    public ArrayList<Integer> getParentIDList() {
        return mParentIDList;
    }
    public HashMap<Integer, ArrayList<String>> getSchemeIdListByParentID() {
        return mSchemeIDListByParentID;
    }


    /**
     * Downloading schemes with their BUY product details
     * @param db
     * @param retailerId
     * @param userId
     * @param distributorId
     * @param channelId
     * @param locationId
     * @param accountId
     * @param priorityProductId
     * @return
     */
    public void downloadBuySchemeDetails(DBUtil db,String retailerId,int userId,int distributorId,int channelId,int locationId
                                           ,int accountId,int priorityProductId,ArrayList<String> mGroupIDList) {
        mSchemeById = new HashMap<>();
        mSchemeList = new ArrayList<>();
        mBuyProductBoBySchemeIdWithPid = new HashMap<>();



        SchemeBO schemeBO ;
        SchemeProductBO schemeProductBo;


        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct SM.SchemeID, SM.Description, SM.Type, SM.ShortName, BD.ProductID, ");
        sb.append("PM.Psname, PM.PName, BD.BuyQty,SM.parentid,SM.count,PM.pCode,SM.buyType,BD.GroupName,BD.GroupType,");
        sb.append("SM.IsCombination,BD.uomid,UM.ListName,SAC.SchemeApplyCount,BD.ToBuyQty,SM.IsBatch,BD.Batchid,PT.ListCode,SM.IsOnInvoice,");
        sb.append("Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag,SCM.groupid, SM.GetType, SM.IsAutoApply");

        sb.append(" FROM SchemeMaster SM left join schemeApplyCountMaster SAC on SM.schemeid=SAC.schemeID");

        sb.append(" and ((SAC.retailerid=0 OR SAC.retailerid=" + bModel.QT(retailerId)+")");
        sb.append(" AND (SAC.userid=0 OR SAC.userid=" + userId + ")) ");

        sb.append(" INNER JOIN  SchemeBuyMaster BD ON BD.SchemeID = SM.SchemeID");
        sb.append(" INNER JOIN SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid");
        sb.append(" LEFT JOIN ProductMaster PM ON BD.ProductID = PM.PID");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON BD.uomid = UM.ListId");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster) PT ON SM.processTypeId = PT.ListId");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");

        sb.append(" where SCM.distributorid in(0," + distributorId + ")");
        sb.append(" and SCM.RetailerId in(0," + retailerId + ")");
        sb.append(" and SCM.channelid in(0," + channelId + ")");
        sb.append(" and SCM.locationid in(0," + locationId + ")");
        sb.append(" and SCM.accountid in(0," + accountId + ")");
        sb.append(" and SCM.PriorityProductId in(0," + priorityProductId + ")");
        sb.append(" AND SAC.schemeApplyCOunt !=0 ORDER BY SM.IsCompanyCreated,SM.schemeID ASC");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            String schemeID = "";
            // store buy products for particular scheme
            ArrayList<SchemeProductBO> buyProductList = new ArrayList<>();
            // schemeNewBO object used to store distinct scheme parent
            // object
            SchemeBO schemeNewBO = null;
            while (c.moveToNext()) {
                if (c.getInt(23) == 0 || (c.getInt(23) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(24) + c.getString(8)))) {


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

                    if (c.getString(21) != null)
                        schemeBO.setProcessType(c.getString(21));
                    else
                        schemeBO.setProcessType("");

                    if (c.getInt(22) == 0)
                        schemeBO.setOffScheme(true);
                    else
                        schemeBO.setOffScheme(false);

                    if (c.getInt(19) == 1)
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

                    schemeProductBo.setTobuyQty(c.getDouble(18));
                    schemeProductBo.setBatchId(c.getString(20));

                    //updating Promo flag in product master list
                    ProductMasterBO productMasterBO= bModel.productHelper.getProductMasterBOById(schemeProductBo.getProductId());
                    if(productMasterBO!=null) {
                        productMasterBO.setIsPromo(true);
                    }


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

    /**
     * updating scheme object with the free product details
     * @param db Database object
     */
    private void downloadFreeProducts(DBUtil db) {
        mFreeGroupTypeByFreeGroupName = new HashMap<>();
        mFreeGroupNameListBySchemeId=new HashMap<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct FD.SchemeID, SFP.productid, PM.psname, SFP.FreeQty, SFP.MaxQty, FD.Rate,");
        sb.append("FD.MaxRate,PM.PName,FD.amount,FD.maxAmount,FD.percent,");
        sb.append("FD.maxPercent,PM.pCode,SFP.uomid,SFP.GroupName,SFP.GroupType,FD.isFreeCombination,");
        sb.append("FD.Type,UM.ListName,FD.everyuomid,FD.everyQty FROM SchemeFreeMaster FD");

        sb.append(" LEFT JOIN SchemeFreeProducts as SFP on SFP.schemeid=FD.schemeid");
        sb.append(" LEFT JOIN ProductMaster PM ON SFP.ProductID = PM.PID");
        sb.append(" LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON SFP.uomid = UM.ListId");

        sb.append(" WHERE  ((SFP.FreeQty>0 and SFP.MaxQty>0) OR SFP.productid ISNULL)");
        sb.append(" ORDER BY FD.SchemeID,SFP.GroupName");


        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            SchemeBO schemeBO;
            SchemeProductBO productBO;
            String schemeID ;
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
                    if (schemeBO.getFreeProducts() == null) {
                        schemeBO.setFreeProducts(new ArrayList<SchemeProductBO>());
                    }


                    //updating stock for free products
                    if (bModel.productHelper.getProductMasterBOById(productBO.getProductId()) != null) {
                        productBO.setStock(bModel.productHelper.getProductMasterBOById(productBO.getProductId()).getSIH());
                    }

                    schemeBO.getFreeProducts().add(productBO);

                    mFreeGroupTypeByFreeGroupName.put(
                            schemeID + productBO.getGroupName(),
                            productBO.getGroupLogic());

                    if(mFreeGroupNameListBySchemeId.get(productBO.getSchemeId())!=null){
                        ArrayList<String> mGroupNames=mFreeGroupNameListBySchemeId.get(productBO.getSchemeId());
                        if(!mGroupNames.contains(productBO.getGroupName())) {
                            mGroupNames.add(productBO.getGroupName());
                        }
                        mFreeGroupNameListBySchemeId.put(productBO.getSchemeId(),mGroupNames);

                    }
                    else {
                        ArrayList<String> mGroupNames=new ArrayList<>();
                        mGroupNames.add(productBO.getGroupName());
                        mFreeGroupNameListBySchemeId.put(productBO.getSchemeId(),mGroupNames);
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
     * @param db Database object
     * @param mGroupIDList list of scheme groups allowed
     */
    private void downloadParentIdListByProduct(DBUtil db,ArrayList<String> mGroupIDList) {

        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SBM.productid,SM.parentid,SCM.groupId,Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag from SchemeBuyMaster SBM ");

        sb.append(" inner join SchemeMaster SM on SM.Schemeid=SBM.Schemeid ");
        sb.append("inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid ");
        sb.append("left join schemeApplyCountMaster SAC on SBM.schemeid=SAC.schemeID ");
        sb.append("and (SAC.retailerid=0 OR SAC.retailerid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID())+")");
        sb.append(" AND (SAC.userid=0 OR SAC.userid=" + bModel.userMasterHelper.getUserMasterBO().getUserid() + ")");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");

        sb.append(" where SCM.distributorid in(0," + bModel.getRetailerMasterBO().getDistributorId() + ")");
        sb.append(" and SCM.RetailerId in(0," + bModel.getRetailerMasterBO().getRetailerID() + ")");
        sb.append(" and SCM.channelid in(0," + bModel.getRetailerMasterBO().getSubchannelid() + ")");
        sb.append(" and SCM.locationid in(0," + bModel.getRetailerMasterBO().getLocationId() + ")");
        sb.append(" and SCM.accountid in(0," + bModel.getRetailerMasterBO().getAccountid() + ")");

        sb.append(" AND SAC.schemeApplyCOunt!=0  AND SM.IsOnInvoice=1 order by SBM.Productid");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            mParentIdListByProductId = new HashMap<>();
            mProductIdListByParentId=new SparseArray<>();
            String productId = "";
            ArrayList<Integer> parentIdList = new ArrayList<>();
            ArrayList<String> productIdList = new ArrayList<>();
            int parentId=0;
            while (c.moveToNext()) {
                if (c.getInt(3) == 0 || (c.getInt(3) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(2) + c.getString(1)))) {


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
                    if (parentId != c.getInt(1)) {
                        if (parentId != 0) {

                            mProductIdListByParentId.put(parentId, productIdList);
                            productIdList = new ArrayList<>();
                            productIdList.add(productId);
                            parentId = c.getInt(1);

                        } else {
                            productIdList.add(productId);
                            parentId = c.getInt(1);

                        }
                    } else {
                        productIdList.add(productId);

                    }

                }
            }
            if (parentIdList.size() > 0) {
                mParentIdListByProductId.put(productId, parentIdList);
            }
            if (productIdList.size() > 0) {
                mProductIdListByParentId.put(parentId, productIdList);
            }

        }
        c.close();
    }

    public HashMap<String, ArrayList<Integer>> getParentIdListByProductId() {
        return mParentIdListByProductId;
    }

    public SparseArray<ArrayList<String>> getProductIdListByParentId() {
        return mProductIdListByParentId;
    }


    /**
     * Method to get details of already applied scheme in previous order.
     * These schemes are not allowed to apply again until given period.
     *
     * @param db Database object
     */
    private void downloadPeriodWiseScheme(DBUtil db) {
        final String currentDate = SDUtil.now(SDUtil.DATE_GLOBAL_EIPHEN);
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SM.schemeid,SB.productid,");
        sb.append("(julianday(" + bModel.QT(currentDate) + ")-julianday(replace(date,'/','-') )) as daycount from Schememaster SM ");
        sb.append("inner join SchemePurchaseHistory SH on SM.parentid=SH.schemeid ");
        sb.append("inner join SchemeBuyMaster SB on SM.schemeid=SB.Schemeid ");
        sb.append("where (isapplied=1 AND SM.Days>=daycount)");
        sb.append("and SH.retailerid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()));
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
    public void downloadSchemeHistoryDetails() {
        ProductMasterBO productBO ;
        mSchemeHistoryListBySchemeId = new HashMap<>();

        DBUtil db ;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "SELECT DISTINCT A.pid, A.batchid, A.schid,IFNULL(PieceUOM.Qty,0) AS PieceQty ,IFNULL(OuterUOM.Qty,0) as OouterQty,"
                    + " IFNULL(CaseUOM.Qty,0) as CaseQty,(IFNULL(PieceUOM.value,0)+IFNULL(OuterUOM.value,0)+IFNULL(CaseUOM.value,0)) "
                    + " FROM SchemeAchHistory A"

                    + " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='PIECE' and rid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()) + ") as PieceUOM ON PieceUOM.Pid = A.pid"
                    + " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='MSQ' and rid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()) + ") as OuterUOM ON OuterUOM.Pid = A.pid"
                    + " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='CASE' and rid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()) + ") as CaseUOM ON CaseUOM .Pid = A.pid"
                    + " LEFT JOIN OrderHeader OH on OH.retailerid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID()) + " and invoicestatus=1"
                    + " LEFT JOIN SchemeDetail SD on SD.parentid=A.schid and OH.orderid=SD.orderid"
                    + " LEFT JOIN SchemeFreeProductDetail SPD on SPD.parentid=A.schid and OH.orderid=SPD.orderid"

                    + " where OH.upload!='X' and rid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID())
                    + " and A.schid!=IFNULL(SD.parentid,0) and A.schid!=IFNULL(SPD.parentid,0) order by schid";

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


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * Download accumulation scheme free issues
     * Validation - Particular scheme should not be in 'SchemeFreeProductDetail' table(To ensure that scheme is already not delivered)
     */
    public void downloadOffInvoiceSchemeDetails() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            mOffInvoiceSchemeList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT distinct productid,Pname,uomid,Qty,ASF.Slabid,ASF.schemeid,ASF.SchemeDesc,ASF.groupName");
            sb.append(",ASF.groupType,ASF.schemeLogic  from AccumulationSchemeFreeIssues ASF");
            sb.append(" inner join Productmaster PM on PM.pid=ASF.productid");
            sb.append(" where ASF.retailerid=");
            sb.append(bModel.getRetailerMasterBO().getRetailerID());
            sb.append(" and ASF.slabid not in(select schemeid from SchemeFreeProductDetail where retailerid=" + bModel.getRetailerMasterBO().getRetailerID() + ") order by ASF.schemeid");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {

                    ArrayList<SchemeProductBO> freeProductList = new ArrayList<>();
                    SchemeProductBO schemeProductBO;
                    int schemeId = 0;
                    int parentId = 0;
                    String schemeDesc = "";
                    String freeType = "";
                    SchemeBO schemeBO ;

                    if (mFreeGroupTypeByFreeGroupName == null) {
                        mFreeGroupTypeByFreeGroupName = new HashMap<>();
                    }

                    while (c.moveToNext()) {
                        schemeProductBO = new SchemeProductBO();
                        schemeProductBO.setProductId(c.getString(0));
                        schemeProductBO.setProductName(c.getString(1));
                        schemeProductBO.setUomID(c.getInt(2));
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
                        if(mFreeGroupNameListBySchemeId.get(schemeProductBO.getSchemeId())!=null){
                            ArrayList<String> mGroupNames=mFreeGroupNameListBySchemeId.get(schemeProductBO.getSchemeId());
                            if(!mGroupNames.contains(schemeProductBO.getGroupName())) {
                                mGroupNames.add(schemeProductBO.getGroupName());
                            }
                            mFreeGroupNameListBySchemeId.put(schemeProductBO.getSchemeId(),mGroupNames);

                        }
                        else {
                            ArrayList<String> mGroupNames=new ArrayList<>();
                            mGroupNames.add(schemeProductBO.getGroupName());
                            mFreeGroupNameListBySchemeId.put(schemeProductBO.getSchemeId(),mGroupNames);
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

///////////////////////////

    /**
     * Preparing a achieved scheme list based on the current order to show in the screen
     * If Off Invoice is available than it will be added to the applied list directly to show in the screen
     * And updating stock availability for free products in the scheme object
     */
    public void schemeApply() {

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

                        if (schemeBO != null&&!schemeBO.isOffScheme()) {
                            // only ON scheme will be allowed to apply

                                if (isSchemeDone(schemeBO, parentID, slabPosition == 1)) {

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

        // Adding Off invoice schemes(Accumulation scheme free issues) to applied scheme list. So that it will be shown in scheme apply screen
        if (mOffInvoiceSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceSchemeList) {
                if (schemeBO != null) {
                    if (isSihAvailableForSchemeGroupFreeProducts(schemeBO, schemeBO.getSchemeId())) {
                        schemeBO.setIsOnInvoice(0);
                        schemeBO.setQuantityTypeSelected(true);
                        schemeBO.setApplyCount(1);
                        schemeBO.setIsFreeCombination(1);
                        mAppliedSchemeList.add(schemeBO);
                    }
                }

            }
        }

        // Updating stock availability for free products
        updateSIHAvailabilityForFreeProducts();


    }


    /**
     * Checking whether current slab is achieved or not
     * @param schemeBO Current slab
     * @param parentID Current Scheme Id
     * @param iSHighestSlab Is highest slab
     * @return Is Slab achieved
     */
    private boolean isSchemeDone(SchemeBO schemeBO, Integer parentID, boolean iSHighestSlab) {

        int mApplyCount=0;

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
                                true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        mApplyCount = getNumberOfTimesSlabApplied_ForAnyLogic(schemeBO.getBuyingProducts(), mGroupNameList, mGroupLogicTypeByGroupName,
                                false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);

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
                                mGroupLogicTypeByGroupName, true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);


                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                        mApplyCount = getNumberOfTimesSlabApplied_ForAndLogic(
                                schemeBO.getBuyingProducts(), mGroupNameList,
                                mGroupLogicTypeByGroupName, false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);

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
     * @param schemeBO Current slab
     * @param groupSchemeName list of groups available in current slab
     * @param groupLogicTypeByGroupName List with group logic type by its group name
     * @param mParentId Scheme Id
     * @return Is Slab achieved or not
     */
    private boolean isParentAnyLogicDone(SchemeBO schemeBO, ArrayList<String> groupSchemeName,
                                         HashMap<String, String> groupLogicTypeByGroupName, int mParentId) {

        for (String groupName : groupSchemeName) {
            String groupLogic = groupLogicTypeByGroupName.get(groupName);
            if (groupLogic.equals(AND_LOGIC)||groupLogic.equals(ONLY_LOGIC)) {
                 if(isGroupAndLogicDone(schemeBO, groupName, mParentId))
                     return true;

            } else if (groupLogic.equals(ANY_LOGIC)) {
                if(isGroupAnyLogicDone(schemeBO, groupName, mParentId))
                    return true;

            }

        }

        return false;
    }

    /**
     * Checking is parent logic(AND) achieved or not
     * Parent logic is AND, So only if all of the groups under this slab is achieved then this slab is considered as achieved
     * @param schemeBO Current slab
     * @param groupSchemeName list of groups available in current slab
     * @param mGroupLogicTypeByGroupName List with group logic type by its group name
     * @param mParentId Scheme Id
     * @return Is Slab achieved or not
     */
    private boolean isParentAndLogicDone(SchemeBO schemeBO,
                                         ArrayList<String> groupSchemeName,
                                         HashMap<String, String> mGroupLogicTypeByGroupName, int mParentId) {

        for (String groupName : groupSchemeName) {
            String groupBuyType = mGroupLogicTypeByGroupName.get(groupName);
            if (groupBuyType.equals(AND_LOGIC)||groupBuyType.equals(ONLY_LOGIC)) {
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
     * @param schemeBO Current slab
     * @param groupName Current group name
     * @param mParentId Scheme Id
     * @return is Current group achieved or not
     */
    private boolean isGroupAndLogicDone(SchemeBO schemeBO, String groupName, int mParentId) {

        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();

        ArrayList<ProductMasterBO> schemeAccumulationList = null;
        if (getSchemeHistoryListBySchemeId() != null) {
            schemeAccumulationList = getSchemeHistoryListBySchemeId().get(schemeBO.getParentId() + "");
        }

        ArrayList<String> mAlreadyAppliedProducts = null;
        if (getProductIdListByAlreadyAppliedSchemeId() != null) {
            mAlreadyAppliedProducts = getProductIdListByAlreadyAppliedSchemeId().get(Integer.parseInt(schemeBO.getSchemeId()));
        }

        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {

            if (mAlreadyAppliedProducts != null) {
                if (mAlreadyAppliedProducts.contains(schemeProductBo.getProductId())) {
                    return false;
                }
            }

            if (schemeProductBo.getGroupName().equals(groupName)) {

                ProductMasterBO productBo = bModel.productHelper
                        .getProductMasterBOById(schemeProductBo.getProductId());
                if (productBo != null) {

                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        int orderedTotalQuantityUomWise = 0;
                        int totalQty;
                        if (schemeBO.isBatchWise() && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            // batch wise scheme
                           totalQty=getBatchTotalQuantity(productBo,schemeProductBo.getBatchId());

                        } else {
                            totalQty = productBo.getOrderedPcsQty()
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCaseSize())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOutersize());
                        }


                        /* scheme accumulation starts */
                        if (schemeAccumulationList != null) {
                            //Adding accumulation product quantities
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (schemeProductBo.getProductId().equals(
                                        schemeAccBO.getProductID())) {
                                    totalQty = totalQty
                                            + schemeAccBO.getOrderedPcsQty()
                                            + (schemeAccBO.getOrderedCaseQty() * productBo
                                            .getCaseSize())
                                            + (schemeAccBO.getOrderedOuterQty() * productBo
                                            .getOutersize());
                                    break;
                                }
                            }
                        }
                        /* scheme accumulation ends */

                        //Just reducing quantity which is used already for applying scheme.
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()).toString());
                            totalQty = totalQty - totalAppliedQty;

                        }


                        if (schemeProductBo.getUomID() != 0) {
                            if (productBo.getCaseUomId() == schemeProductBo.getUomID()) { // check case wise scheme
                                if (productBo.getCaseSize() != 0) {
                                    orderedTotalQuantityUomWise = totalQty
                                            / productBo.getCaseSize();
                                }
                            } else if (productBo.getOuUomid() == schemeProductBo.getUomID()) { // check outer wise scheme
                                if (productBo.getOutersize() != 0) {
                                    orderedTotalQuantityUomWise = totalQty
                                            / productBo.getOutersize();
                                }
                            } else { // check piece wise scheme
                                orderedTotalQuantityUomWise = totalQty;
                            }
                        } else {
                            orderedTotalQuantityUomWise = totalQty;
                        }

                        if (schemeProductBo.getBuyQty() > orderedTotalQuantityUomWise) {
                            return false;
                        }

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        double totalValue;

                        if (productBo.getBatchwiseProductCount() > 0 && schemeBO.isBatchWise() && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            totalValue = getBatchTotalValue(productBo, schemeProductBo.getBatchId());
                        } else {
                            totalValue = (productBo.getOrderedPcsQty() * productBo
                                    .getSrp())
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCsrp())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOsrp());
                        }


                        /* scheme accumulation starts */
                        if (schemeAccumulationList != null) {
                            //Adding accumulation product values
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (productBo.getProductID().equals(
                                        schemeAccBO.getProductID()) && (!schemeBO.isBatchWise() || (schemeBO.isBatchWise() && schemeProductBo.getBatchId().equals(schemeAccBO.getBatchid())))) {


                                    totalValue = totalValue
                                            + schemeAccBO.getTotalamount();
                                    break;
                                }
                            }

                        }
                        /* scheme accumulation ends */

                        //Just reducing value which is used already for applying scheme.
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId());
                            totalValue = totalValue - totalAppliedQty;

                        }


                        if (schemeProductBo.getBuyQty() > totalValue) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * Checking is current group(ANY) is achieved or not
     * @param schemeBO Current slab
     * @param groupName Current group name
     * @param mParentId Scheme Id
     * @return is Current group achieved or not
     */
    private boolean isGroupAnyLogicDone(SchemeBO schemeBO, String groupName, int mParentId) {

        int totalQty = 0;
        double totalValue = 0;

        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();
        ArrayList<ProductMasterBO> schemeAccumulationList = null;

        if (getSchemeHistoryListBySchemeId() != null) {
            schemeAccumulationList = getSchemeHistoryListBySchemeId().get(schemeBO.getParentId() + "");
        }

        ArrayList<String> mAlreadyAppliedProductList = null;
        if (getProductIdListByAlreadyAppliedSchemeId() != null) {
            mAlreadyAppliedProductList = getProductIdListByAlreadyAppliedSchemeId().get(Integer.parseInt(schemeBO.getSchemeId()));
        }

        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {
            // already scheme applied in previous days
            if (mAlreadyAppliedProductList != null && mAlreadyAppliedProductList.contains(schemeProductBo.getProductId())) {
                break;
            }

            if (schemeProductBo.getGroupName().equals(groupName)) {
                ProductMasterBO productBo = bModel.productHelper.getProductMasterBOById(schemeProductBo.getProductId());
                if (productBo != null) {

                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        int orderedTotalQuantityByUOMWise ;
                        int totalProductQty = 0;

                        if (schemeBO.isBatchWise() && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            // batch wise scheme
                            totalQty=getBatchTotalQuantity(productBo,schemeProductBo.getBatchId());

                        } else {
                            totalProductQty = productBo.getOrderedPcsQty()
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCaseSize())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOutersize());
                        }


						/* scheme accumulation starts */
                        if (schemeAccumulationList != null) {
                            //Adding accumulation product quantities
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (schemeProductBo.getProductId().equals(
                                        schemeAccBO.getProductID())) {
                                    totalProductQty = totalProductQty
                                            + schemeAccBO.getOrderedPcsQty()
                                            + (schemeAccBO.getOrderedCaseQty() * productBo
                                            .getCaseSize())
                                            + (schemeAccBO.getOrderedOuterQty() * productBo
                                            .getOutersize());
                                    break;
                                }
                            }
                        }
                        /* scheme accumulation ends */

                        //Just reducing quantity which is used already for applying scheme.
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()).toString());
                            totalProductQty = totalProductQty - totalAppliedQty;
                        }

                        if (schemeProductBo.getUomID() != 0) {
                            if (productBo.getCaseUomId() == schemeProductBo
                                    .getUomID()) { // check case wise scheme
                                if (productBo.getCaseSize() != 0) {
                                    orderedTotalQuantityByUOMWise = totalProductQty
                                            / productBo.getCaseSize();
                                    totalQty = totalQty
                                            + orderedTotalQuantityByUOMWise;
                                }
                            } else if (productBo.getOuUomid() == schemeProductBo
                                    .getUomID()) { // check outer wise scheme
                                if (productBo.getOutersize() != 0) {
                                    orderedTotalQuantityByUOMWise = totalProductQty
                                            / productBo.getOutersize();
                                    totalQty = totalQty
                                            + orderedTotalQuantityByUOMWise;
                                }
                            } else { // check piece wise scheme
                                orderedTotalQuantityByUOMWise = totalProductQty;
                                totalQty = totalQty + orderedTotalQuantityByUOMWise;
                            }
                        } else {
                            orderedTotalQuantityByUOMWise = totalProductQty;
                            totalQty = totalQty + orderedTotalQuantityByUOMWise;
                        }

                        if (schemeProductBo.getBuyQty() <= totalQty) {

                            return true;
                        }
                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        double totalProductValue ;

                        if (productBo.getBatchwiseProductCount() > 0 && schemeBO.isBatchWise() && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            totalProductValue = getBatchTotalValue(productBo, schemeProductBo.getBatchId());
                        } else {
                            totalProductValue = (productBo.getOrderedPcsQty() * productBo
                                    .getSrp())
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCsrp())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOsrp());
                        }

                        /* scheme accumulation starts */
                        if (schemeAccumulationList != null) {
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (productBo.getProductID().equals(
                                        schemeAccBO.getProductID()) && (!schemeBO.isBatchWise() || (schemeBO.isBatchWise() && schemeProductBo.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                    totalProductValue = totalProductValue
                                            + schemeAccBO.getTotalamount();
                                    break;
                                }
                            }

                        }
                        /* scheme accumulation ends */

                        //Just reducing value which is used already for applying scheme.
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(mParentId + schemeProductBo.getProductId());
                            totalProductValue = totalProductValue - totalAppliedQty;
                        }


                        totalValue = totalValue + totalProductValue;
                        if (schemeProductBo.getBuyQty() <= totalValue) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }


    private double getBatchTotalValue(ProductMasterBO productBO, String batchId) {

        ArrayList<ProductMasterBO> batchWiseList = bModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        double totalValue = 0.0;
        if (batchWiseList != null) {
            for (ProductMasterBO batchProductBO : batchWiseList) {
                if (batchProductBO.getBatchid().equals(batchId)) {
                    if (batchProductBO.getOrderedPcsQty() > 0
                            || batchProductBO.getOrderedCaseQty() > 0
                            || batchProductBO.getOrderedOuterQty() > 0) {
                        double totalBatchValue = batchProductBO.getOrderedPcsQty()
                                * batchProductBO.getSrp()
                                + batchProductBO.getOrderedCaseQty()
                                * batchProductBO.getCsrp()
                                + batchProductBO.getOrderedOuterQty()
                                * batchProductBO.getOsrp();
                        totalValue = totalValue + totalBatchValue;
                        batchProductBO.setDiscount_order_value(totalBatchValue);
                        batchProductBO.setSchemeAppliedValue(totalBatchValue);
                    }
                }
            }
        }
        return totalValue;

    }

    public int getBatchTotalQuantity(ProductMasterBO productBO, String batchId) {

        ArrayList<ProductMasterBO> batchWiseList = bModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        int totalQuantity=0;
        if (batchWiseList != null) {
            for (ProductMasterBO batchProductBO : batchWiseList) {
                if (batchProductBO.getBatchid().equals(batchId)) {
                    if (batchProductBO.getOrderedPcsQty() > 0
                            || batchProductBO.getOrderedCaseQty() > 0
                            || batchProductBO.getOrderedOuterQty() > 0) {
                        totalQuantity = batchProductBO.getOrderedPcsQty()
                                + (batchProductBO.getOrderedCaseQty()*productBO.getCaseSize())
                                + (batchProductBO.getOrderedOuterQty()*productBO.getOutersize());

                    }
                }
            }
        }
        return totalQuantity;

    }

    /**
     * After scheme achieved successfully ,this method used to update the scheme free
     * details like product,percentage,amount and price     *
     * @param schemeBO Current Slab
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

                        /** Calculate for amount discount **/

                        minFreeAmount = schemeProductBO.getMinAmount() * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * mNumberOfTimesSlabAchieved;


                    }
                    /* scheme type is Sales Value */
                    else if (schemeBO.getBuyType().equals(SALES_VALUE)) {


                        /** Calculate for quantity **/

                        minFreeQuantity = minimumQuantity * mNumberOfTimesSlabAchieved;
                        maxFreeQuantity = maximumQuantity * mNumberOfTimesSlabAchieved;

                        /** Calculate for amount discount **/

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

                        minFreeAmount = schemeProductBO.getMinAmount()  * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount()  * mNumberOfTimesSlabAchieved;
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


                        minFreeAmount = schemeProductBO.getMinAmount()   * mNumberOfTimesSlabAchieved;
                        maxFreeAmount = schemeProductBO.getMaxAmount()   * mNumberOfTimesSlabAchieved;

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
                        int count = getAmountBasedSchemeCount(schemeBO);
                        if (count == 0) count = 1; // atleast one time apply

                        minFreeAmount = schemeProductBO.getMinAmount() * count;
                        maxFreeAmount = schemeProductBO.getMaxAmount() * count;

                    } else {

                        minFreeAmount = schemeProductBO.getMinAmount();
                        maxFreeAmount = schemeProductBO.getMaxAmount();
                    }

                }

                schemeProductBO.setQuantityActualCalculated(minFreeQuantity);
                schemeProductBO.setQuantityMaxiumCalculated(maxFreeQuantity);

                /** no calculation required for Price discount and % discount **/

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
            if (schemeBO.getActualPrice() > 0  && schemeBO.getMaximumPrice() > 0) {
                schemeBO.setPriceTypeSeleted(true);
            } else if (schemeBO.getMinimumAmount() > 0 && schemeBO.getMaximumAmount() > 0) {
                schemeBO.setAmountTypeSelected(true);
            } else if (schemeBO.getMinimumPrecent() > 0 && schemeBO.getMaximumPrecent() > 0) {
                schemeBO.setDiscountPrecentSelected(true);
            }
            else if (schemeBO.getMinimumPrecent() > 0 && schemeBO.getMaximumPrecent() > 0) {
                schemeBO.setDiscountPrecentSelected(true);
            }
            else if(schemeBO.getActualQuantity()>0&&schemeBO.getMaximumQuantity()>0){
                schemeBO.setQuantityTypeSelected(true);
            }


        }
    }

    public List<SchemeBO> getSchemeList() {
        return mSchemeList;
    }


    public Map<String, SchemeBO> getSchemeById() {
        return mSchemeById;
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
                                    productMasterBO = bModel.productHelper.getProductMasterBOById(schemePdtBO.getProductId());

                                    if (productMasterBO != null) {
                                        stock = productMasterBO.getSIH()
                                                - ((productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                                .getOutersize()) + productMasterBO.getOrderedPcsQty());
                                    }

                                    schemePdtBO.setStock(stock);
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

    }

    /**
     * Getting number of times current slab(Parent logic - AND) is achieved
     * @param schemeProductList Buy products for current slab
     * @param schemeGroupName List of groups available under the given slab
     * @param schemeGroupBuyTypeByGroupName List with group logic by its group name
     * @param isQuantityType  Buy type is quantity or sales value
     * @param isBatchWise Is batch wise products available
     * @param parentId Scheme Id
     * @param processType Scheme process type
     * @param isHighestSlab Is current slab is highest slab or not
     * @return Number of times current slab is achieved
     */
    private int getNumberOfTimesSlabApplied_ForAndLogic(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeByGroupName,
            boolean isQuantityType, boolean isBatchWise, int parentId, String processType, boolean isHighestSlab) {

        int tempCount = 0;
        double tempBalancePercent = 0;
        for (String groupName : schemeGroupName) {
            String type = schemeGroupBuyTypeByGroupName.get(groupName);
            if (type.equals(AND_LOGIC) || type.equals(ONLY_LOGIC)) {

                int count;
                if (isQuantityType) {
                    count = getAndLogicAppliedCountForQuantity(schemeProductList, groupName,  isBatchWise, parentId, processType, isHighestSlab);
                } else {
                    count = getAndLogicAppliedCountForSalesValue(schemeProductList, groupName,  isBatchWise, parentId, processType, isHighestSlab);
                }

                //Getting lowest value as it is a AND/ONLY logic
                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            } else if (type.equals(ANY_LOGIC)) {
                int count ;
                if (isQuantityType) {
                    count = getANYLogicAppliedCountForQuantity(schemeProductList, groupName,  isBatchWise, parentId, processType, isHighestSlab);
                } else {
                    count = getAnyLogicAppliedCountForSalesValue(schemeProductList, groupName, isBatchWise, parentId, processType, isHighestSlab);
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
     * @param schemeProductList Buy products for current slab
     * @param schemeGroupName List of groups available under the given slab
     * @param schemeGroupBuyTypeByGroupName List with group logic by its group name
     * @param isQuantityType  Buy type is quantity or sales value
     * @param isBatchWise Is batch wise products available
     * @param parentID Scheme Id
     * @param processType Scheme process type
     * @param isHighestSlab Is current slab is highest slab or not
     * @return Number of times current slab is achieved
     */
    private int getNumberOfTimesSlabApplied_ForAnyLogic(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeByGroupName,
            boolean isQuantityType, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {

        int mAppliedCount = 1;
        double balancePercent = 0;

        for (String s : schemeGroupName) {
            String type = schemeGroupBuyTypeByGroupName.get(s);

            if (type.equals(AND_LOGIC) || type.equals(ONLY_LOGIC)) {
                int count;

                if (isQuantityType) {
                    count = getAndLogicAppliedCountForQuantity(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab);
                } else {
                    count = getAndLogicAppliedCountForSalesValue(schemeProductList, s,  isBatchWise, parentID, processType, isHighestSlab);
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
                int count ;

                if (isQuantityType) {
                    count = getANYLogicAppliedCountForQuantity(schemeProductList, s,  isBatchWise, parentID, processType, isHighestSlab);
                } else {
                    count = getAnyLogicAppliedCountForSalesValue(schemeProductList, s, isBatchWise, parentID, processType, isHighestSlab);
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


    public double getBalancePercent() {
        return balancePercent;
    }

    public void setBalancePercent(double balancePercent) {
        this.balancePercent = balancePercent;
    }

    double balancePercent = 0;


    //key: (parentid+productid)  value: Total (qty or sales) value already used to apply scheme
    HashMap<String, Integer> mAchieved_qty_or_salesValue_by_schemeId_nd_productid;

    /**
     * Getting number of times current group(AND) is achieved. Buy Type- Quantity
     * @param schemeProductList Current slabs buy product list
     * @param groupName current group name
     * @param isBatchWise Is batch wise products available
     * @param parentID Scheme Id
     * @param processType Scheme Process type
     * @param isHighestSlab Is highest slab
     * @return Total number of times current group achieved
     */
    private int getAndLogicAppliedCountForQuantity(List<SchemeProductBO> schemeProductList,
                                                   String groupName, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {
        int tempCount = 0;
        double balancePercent = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                int count = 0;
                ProductMasterBO productMasterBO = bModel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());

                if (productMasterBO != null) {
                    int quantity;

                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        quantity = getBatchTotalQuantity(productMasterBO, schemeProductBO.getBatchId());
                    } else {
                        quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                                .getCaseSize())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOutersize())
                                + productMasterBO.getOrderedPcsQty();
                    }

                    //Removing already used quantity if any
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) != null) {
                            quantity = quantity - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()).toString());
                        }
                    }

					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationList = null;
                    if (getSchemeHistoryListBySchemeId() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationList = getSchemeHistoryListBySchemeId().get(schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationList != null) {
                        //Adding accumulation quantity
                        for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                            if (schemeProductBO.getProductId().equals(
                                    schemeAccBO.getProductID()) && (!isBatchWise || (isBatchWise && schemeProductBO.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                quantity = quantity
                                        + schemeAccBO.getOrderedPcsQty()
                                        + (schemeAccBO.getOrderedCaseQty() * productMasterBO
                                        .getCaseSize())
                                        + (schemeAccBO.getOrderedOuterQty() * productMasterBO
                                        .getOutersize());
                                break;
                            }
                        }
                    }
                    /* scheme accumulation ends */

                    if (quantity > 0) {
                        int balanceQty = 0;
                        int balanceQtyInPieces = 0;
                        if (schemeProductBO.getUomID() != 0) {
                            if (schemeProductBO.getUomID() == productMasterBO
                                    .getCaseUomId()) {
                                if (productMasterBO.getCaseSize() > 0
                                        && schemeProductBO.getTobuyQty() > 0
                                        && schemeProductBO.getBuyQty() > 0) {
                                    count = ((quantity / productMasterBO
                                            .getCaseSize()) / (int) schemeProductBO
                                            .getTobuyQty());
                                    balanceQty = ((quantity / productMasterBO
                                            .getCaseSize()) % (int) schemeProductBO
                                            .getTobuyQty());
                                    balanceQtyInPieces = balanceQty * productMasterBO
                                            .getCaseSize() + (quantity % productMasterBO.getCaseSize());

                                }

                            } else if (schemeProductBO.getUomID() == productMasterBO
                                    .getOuUomid()) {
                                if (productMasterBO.getOutersize() > 0
                                        && schemeProductBO.getTobuyQty() > 0
                                        && schemeProductBO.getBuyQty() > 0) {
                                    count = ((quantity / productMasterBO
                                            .getOutersize()) / (int) schemeProductBO
                                            .getTobuyQty());
                                    balanceQty = ((quantity / productMasterBO
                                            .getOutersize()) % (int) schemeProductBO
                                            .getTobuyQty());
                                    balanceQtyInPieces = balanceQty * productMasterBO
                                            .getOutersize() + (quantity % productMasterBO.getOutersize());

                                }

                            } else {
                                count = quantity
                                        / (int) schemeProductBO.getTobuyQty();
                                balanceQty = quantity
                                        % (int) schemeProductBO.getTobuyQty();
                                balanceQtyInPieces = balanceQty;
                            }
                        } else {
                            count = quantity / (int) schemeProductBO.getTobuyQty();
                            balanceQty = quantity
                                    % (int) schemeProductBO.getTobuyQty();
                            balanceQtyInPieces = balanceQty;

                        }


                        //updating used quantity for applying scheme in the list
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productMasterBO.getProductID())) {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productMasterBO.getProductID()),  (quantity - balanceQtyInPieces));
                        } else {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productMasterBO.getProductID()), (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) + ((quantity - balanceQtyInPieces))));
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
			 * for computation to how many times scheme achived. but minimum buy
			 * qty used to apply scheme or not.
			 */
            return 1;
        }

    }

    public void loadSchemeReport(String id, boolean flag) {
        // SchemeProductBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            // sb.append("select distinct SD.schemeid,MIN(SD.productid),SFD.freeproductID,SFD.FreeQty,");
            // sb.append("SFD.uomID,SFD.batchid from SchemeDetail SD  inner join SchemeFreeproductDetail SFD ");
            // sb.append("on SFD.schemeid=SD.schemeID where ");
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

                    // schemeProductBO.setQuantitySelected(c.getInt(3));
                    // schemeProductBO.setUomID(c.getInt(4));
                    String productid = c.getString(1);
                    // productBo is buy product object
                    ProductMasterBO produBo = bModel.productHelper
                            .getProductMasterBOById(productid);
                    if (produBo != null) {
                        produBo.setMschemeper(c.getDouble(2));
                    }
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

    }

    /**
     * Getting number of times current group(AND) is achieved. Buy Type- Sales value
     * @param schemeProductList Current slabs buy product list
     * @param groupName current group name
     * @param isBatchWise Is batch wise products available
     * @param parentId Scheme Id
     * @param processType Scheme Process type
     * @param isHighestSlab Is highest slab
     * @return Total number of times current group achieved
     */
    private int getAndLogicAppliedCountForSalesValue(
            List<SchemeProductBO> schemeProductList, String groupName
           , boolean isBatchWise, int parentId, String processType, boolean isHighestSlab) {

        int tempCount = 0;
        double tempBalancePercent ;
        double tempBal ;
        double balancePercent = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                int count = 0;
                ProductMasterBO productMasterBO = bModel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());

                if (productMasterBO != null) {

                    double totalValue ;
                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        totalValue = getBatchTotalValue(productMasterBO, schemeProductBO.getBatchId());
                    } else {
                        totalValue = (productMasterBO.getOrderedCaseQty() * productMasterBO
                                .getCsrp())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOsrp())
                                + productMasterBO.getOrderedPcsQty()
                                * productMasterBO.getSrp();
                    }

                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + productMasterBO.getProductID()) != null) {
                            totalValue = totalValue - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + productMasterBO.getProductID()).toString());
                        }
                    }

					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationValueList = null;
                    if (getSchemeHistoryListBySchemeId() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationValueList = getSchemeHistoryListBySchemeId().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationValueList != null) {
                        //Adding accumulation amount to the current order amount
                        for (ProductMasterBO schemeAccBO : schemeAccumulationValueList) {
                            if (schemeProductBO.getProductId().equals(
                                    schemeAccBO.getProductID()) && (!isBatchWise || (isBatchWise && schemeProductBO.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                totalValue = totalValue
                                        + schemeAccBO.getTotalamount();
                                break;
                            }
                        }
                    }
                    /* scheme accumulation ends */

                    if (totalValue > 0) {
                        double balanceValue ;
                        if (schemeProductBO.getTobuyQty() > 0
                                && schemeProductBO.getBuyQty() > 0) {

                            count = (int) totalValue / (int) schemeProductBO.getTobuyQty();
                            balanceValue = totalValue % schemeProductBO.getTobuyQty();
                        } else {
                            balanceValue = totalValue;

                        }

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentId + productMasterBO.getProductID())) {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentId + productMasterBO.getProductID()), (int) (totalValue - balanceValue));
                        } else {
                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentId + productMasterBO.getProductID()),
                                    (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + productMasterBO.getProductID()) + ((int) (totalValue - balanceValue))));
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
     * @param schemeProductList Current slabs buy product list
     * @param groupName current group name
     * @param isBatchWise Is batch wise products available
     * @param parentID Scheme Id
     * @param processType Scheme Process type
     * @param isHighestSlab Is highest slab
     * @return Total number of times current group achieved
     */
    private int getANYLogicAppliedCountForQuantity(List<SchemeProductBO> schemeProductList,
                                                   String groupName,  boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {
        int count = 0;
        double tempBalancePercent = 0;
        double minimumBuyQuantity = 0;
        double maximumBuyQuantity = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName) ) {

                maximumBuyQuantity = schemeProductBO.getTobuyQty();
                minimumBuyQuantity = schemeProductBO.getBuyQty();

                ProductMasterBO productMasterBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                if (productMasterBO != null) {

                    int quantity;
                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        quantity = getBatchTotalQuantity(productMasterBO, schemeProductBO.getBatchId());
                    }
                    else {
                        quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                                .getCaseSize())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOutersize())
                                + productMasterBO.getOrderedPcsQty();
                    }

                    //Quantity used(if previous slab applied) for scheme are reduced here.
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) != null) {
                            quantity = quantity - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()).toString());
                        }
                    }


					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationList = null;
                    if (getSchemeHistoryListBySchemeId() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationList = getSchemeHistoryListBySchemeId().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationList != null) {
                        // Adding accumulation quantity to the current order quantity
                        for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                            if (schemeProductBO.getProductId().equals(
                                    schemeAccBO.getProductID()) && (!isBatchWise || (isBatchWise && schemeProductBO.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                quantity = quantity
                                        + schemeAccBO.getOrderedPcsQty()
                                        + (schemeAccBO.getOrderedCaseQty() * productMasterBO
                                        .getCaseSize())
                                        + (schemeAccBO.getOrderedOuterQty() * productMasterBO
                                        .getOutersize());
                                break;
                            }
                        }
                    }
                    /* scheme accumulation ends */


                    if (quantity > 0) {
                        if (schemeProductBO.getUomID() != 0) {
                            if (schemeProductBO.getUomID() == productMasterBO.getCaseUomId()) {
                                if (productMasterBO.getCaseSize() > 0) {
                                    count = count + quantity / productMasterBO.getCaseSize();
                                }

                            } else if (schemeProductBO.getUomID() == productMasterBO.getOuUomid()) {
                                if (productMasterBO.getOutersize() > 0) {
                                    count = count + quantity / productMasterBO.getOutersize();
                                }

                            } else {
                                count = count + quantity;
                            }
                        } else {
                            count = count + quantity;
                        }
                    }
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
                }

                //list to maintain used(if current slab applied) quantity,scheme wise..
                // updating hashMap, qty used to apply scheme
                // Any Logic- So considered all products
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {

                    int orderedQuantity ;
                    double tempToQty ;
                    int appliedQuantity ;
                    tempToQty = maximumBuyQuantity * count;
                    for (SchemeProductBO schemeProductBO : schemeProductList) {
                        ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {

                            if (isBatchWise && productBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                                orderedQuantity=getBatchTotalQuantity(productBO,schemeProductBO.getBatchId());
                            }
                            else {
                                orderedQuantity = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                            }

                            if (orderedQuantity>0) {

                                if (schemeProductBO.getUomID() == productBO.getOuUomid()) {
                                    if (productBO.getOutersize() != 0) {
                                        appliedQuantity = orderedQuantity / productBO.getOutersize();
                                        if (tempToQty > appliedQuantity)
                                            tempToQty = tempToQty - appliedQuantity;
                                        else {
                                            appliedQuantity = (int) tempToQty;
                                            tempToQty = 0;
                                        }

                                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                                !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productBO.getProductID())) {
                                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), appliedQuantity * productBO.getOutersize());
                                        } else {
                                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()),
                                                    (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productBO.getProductID()) + appliedQuantity * productBO.getOutersize()));
                                        }


                                    }

                                } else if (schemeProductBO.getUomID() == productBO.getCaseUomId()) {
                                    if (productBO.getCaseSize() != 0) {
                                        appliedQuantity = orderedQuantity / productBO.getCaseSize();
                                        if (tempToQty > appliedQuantity)
                                            tempToQty = tempToQty - appliedQuantity;
                                        else {
                                            appliedQuantity = (int) tempToQty;
                                            tempToQty = 0;
                                        }
                                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                                !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productBO.getProductID())) {
                                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), appliedQuantity * productBO.getCaseSize());
                                        } else {
                                            mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()),
                                                    (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productBO.getProductID()) + appliedQuantity * productBO.getCaseSize()));
                                        }
                                    }

                                } else {
                                    if (tempToQty > orderedQuantity)
                                        tempToQty = tempToQty - orderedQuantity;
                                    else {
                                        orderedQuantity = (int) tempToQty;
                                        tempToQty = 0;
                                    }

                                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                            !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productBO.getProductID())) {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), orderedQuantity);
                                    } else {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()),
                                                (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productBO.getProductID()) + orderedQuantity));
                                    }
                                }


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
     * @param schemeProductList Current slabs buy product list
     * @param groupName current group name
     * @param isBatchWise Is batch wise products available
     * @param parentID Scheme Id
     * @param processType Scheme Process type
     * @param isHighestSlab Is highest slab
     * @return Total number of times current group achieved
     */
    private int getAnyLogicAppliedCountForSalesValue( List<SchemeProductBO> schemeProductList, String groupName,
            boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {

        double totalValue = 0;
        double minimumBuyValue = 0;
        double maximumBuyValue = 0;
        double balancePercent = 0;
        double appliedSchemeValue ;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)) {

                minimumBuyValue = schemeProductBO.getBuyQty();
                maximumBuyValue = schemeProductBO.getTobuyQty();

                ProductMasterBO productMasterBO = bModel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());
                if (productMasterBO != null) {

                    int quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize())
                            + productMasterBO.getOrderedPcsQty();

                    double value;
                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        value = getBatchTotalValue(productMasterBO, schemeProductBO.getBatchId());
                    } else {
                        value = (productMasterBO.getOrderedCaseQty() * productMasterBO
                                .getCsrp())
                                + (productMasterBO.getOrderedOuterQty() * productMasterBO
                                .getOsrp())
                                + productMasterBO.getOrderedPcsQty()
                                * productMasterBO.getSrp();
                    }

                    //Quantity used(if previous slab applied) for scheme are reduced here.
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) != null) {
                            value = value - mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID());
                        }
                    }


					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationValueList = null;
                    if (getSchemeHistoryListBySchemeId() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationValueList = getSchemeHistoryListBySchemeId().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationValueList != null) {
                        for (ProductMasterBO schemeAccBO : schemeAccumulationValueList) {
                            if (schemeProductBO.getProductId().equals(
                                    schemeAccBO.getProductID()) && (!isBatchWise || (isBatchWise && schemeProductBO.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                value = value
                                        + schemeAccBO.getTotalamount();
                                break;
                            }
                        }
                    }
                    /* scheme accumulation ends */

                    if (quantity > 0) {
                        totalValue = totalValue + value;
                    }
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
                    double totVal ;
                    double tempToQty ;
                    tempToQty = maximumBuyValue;

                    for (SchemeProductBO schemeProductBO : schemeProductList) {
                        ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {

                            if (isBatchWise && productBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                                totVal = getBatchTotalValue(productBO, schemeProductBO.getBatchId());
                            } else {
                                totVal = (productBO.getOrderedCaseQty() * productBO
                                        .getCsrp())
                                        + (productBO.getOrderedOuterQty() * productBO
                                        .getOsrp())
                                        + productBO.getOrderedPcsQty()
                                        * productBO.getSrp();
                            }

                            if (totVal > 0) {

                                if (tempToQty >= totVal) {
                                    tempToQty -= totVal;

                                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                            !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productBO.getProductID())) {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), (int) totVal);
                                    } else {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()),
                                                (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productBO.getProductID()) + ((int) totVal)));
                                    }

                                } else {
                                    if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null &&
                                            !mAchieved_qty_or_salesValue_by_schemeId_nd_productid.containsKey(parentID + productBO.getProductID())) {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), (int) appliedSchemeValue);
                                    } else {
                                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()),
                                                (mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productBO.getProductID()) + ((int) appliedSchemeValue)));
                                    }
                                    break;
                                }


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
					 * using for computation to how many times scheme achived
					 */
                    return 1;
                }
            }
        }


        return 1;

    }


    /**
     * @return
     * @author rajesh.k method to return hashmap of Schemeachievement history
     * details
     */
    public HashMap<String, ArrayList<ProductMasterBO>> getSchemeHistoryListBySchemeId() {
        return mSchemeHistoryListBySchemeId;
    }




    /**
     * @return
     * @author rajesh.k
     * <p>
     * method to retrieve applied scheme list.
     */

    public ArrayList<SchemeBO> getAppliedSchemeList() {
        if (mAppliedSchemeList != null) {
            return mAppliedSchemeList;
        }
        return new ArrayList<>();
    }

    public boolean isFromCounterSale;

    /**
     * @param orderID - mapping to orderID
     * @author rajesh.k
     * <p>
     * Method to save all applied scheme details in SQLite
     */
    public void insertSchemeDetails(String orderID, DBUtil db, String flag) {
        if (mAppliedSchemeList != null) {

            for (SchemeBO schemeBO : mAppliedSchemeList) {

                if (schemeBO.isAmountTypeSelected()
                        || schemeBO.isPriceTypeSeleted()
                        || schemeBO.isDiscountPrecentSelected()
                        || schemeBO.isQuantityTypeSelected()) {
                    insertSchemeBuyProductDetails(schemeBO, db, orderID, flag);
                }

                if (schemeBO.isQuantityTypeSelected()) {
                    insertFreeProductDetails(schemeBO, db, orderID, flag);
                }

            }
        }

    }

    /**
     * @param schemeBO
     * @param db
     * @param orderID  - mapping for this orderID
     * @author rajesh.k Method to insert scheme buy product in scheme detail
     * table
     */
    private void insertSchemeBuyProductDetails(SchemeBO schemeBO, DBUtil db,
                                               String orderID, String flag) {
        String schemeDetailColumn = "OrderID,SchemeID,ProductID,SchemeType,Value,parentid,Retailerid,distributorid,upload,Amount";

        if (isFromCounterSale) {
            schemeDetailColumn = "Uid,SlabId,ProductId,SchemeType,Value,SchemeId,Retailerid,distributorid,upload,Amount";
        }

        List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();

        double totalOrderValueOfBuyProducts=0;
        if (schemeBO.isAmountTypeSelected()) {

            if (schemeBO.isAmountTypeSelected()) {
                for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {
                    ProductMasterBO productBO = bModel.productHelper
                            .getProductMasterBOById(schemeProductBo
                                    .getProductId());
                    totalOrderValueOfBuyProducts += (productBO.getOrderedCaseQty() * productBO.getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO.getSrp())
                            + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                }
            }

        }

        if (buyProductList != null) {
            for (SchemeProductBO schemeProductBO : buyProductList) {
                ProductMasterBO productBO;
                productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());

                if (productBO != null) {
                    if (productBO.getOrderedPcsQty() > 0
                            || productBO.getOrderedCaseQty() > 0
                            || productBO.getOrderedOuterQty() > 0) {
                        int value = 1;
                        StringBuffer sb = new StringBuffer();
                        sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                                + schemeProductBO.getProductId() + ",");
                        if (schemeBO.isQuantityTypeSelected()) {
                            sb.append(bModel.QT(SCHEME_FREE_PRODUCT) + ",");
                            sb.append(+0);
                        } else if (schemeBO.isAmountTypeSelected()) {
                            sb.append(bModel.QT(SCHEME_AMOUNT));

                            double line_value = (productBO.getOrderedCaseQty() * productBO.getCsrp())
                                    + (productBO.getOrderedPcsQty() * productBO.getSrp())
                                    + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                            double percentage_productContribution=((line_value/totalOrderValueOfBuyProducts)*100);
                            double amount_free=schemeBO.getSelectedAmount()*(percentage_productContribution/100);

                            sb.append("," + (amount_free));

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
                            if (flag.equals("I"))
                                sb.append(",'I'");
                            else
                                sb.append(",'N'");
                        }

                        // saving product wise discount value if scheme is amount type
                        if (schemeBO.isAmountTypeSelected()) {

                            sb.append("," + (schemeProductBO.getDiscountValue() / value));
                        } else {
                            sb.append("," + schemeProductBO.getDiscountValue());
                        }

                        if (isFromCounterSale) {
                            db.insertSQL(DataMembers.tbl_CS_scheme_details,
                                    schemeDetailColumn, sb.toString());
                        } else {
                            db.insertSQL(DataMembers.tbl_scheme_details,
                                    schemeDetailColumn, sb.toString());
                        }


                    }

                }
            }
        }

    }

    public void insertAccumulationDetails(DBUtil db, String orderID) {


        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId,HsnCode";
        if (isFromCounterSale) {
            freeDetailColumn = "uid,SlabId,ProductId,Qty,UomID,UomCount,BatchId,SchemeId,RetailerId,price,taxAmount";
        }
        if (mOffInvoiceAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceAppliedSchemeList) {
                if (!IS_VALIDATE_FOC_VALUE_WITH_ORDER_VALUE
                        || OrderHelper.getInstance(context).getValidAccumulationSchemes().contains(String.valueOf(schemeBO.getParentId()))) {

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
     * @param schemeBO - applied schemeBO
     * @param db
     * @param orderID  - mapping for this orderid with order header table
     * @author rajesh.k Method to insert scheme free porduct in
     * schemefreeproductdetail table
     */
    private void insertFreeProductDetails(SchemeBO schemeBO, DBUtil db,
                                          String orderID, String flag) {
        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId,price,taxAmount,HsnCode";

        if (isFromCounterSale) {
            freeDetailColumn = "uid,SlabId,ProductId,Qty,UomID,UomCount,BatchId,SchemeId,RetailerId,price,taxAmount,upload";
        }

        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO freeProductBO : freeProductList) {
                if (freeProductBO.getQuantitySelected() > 0) {

                    ProductMasterBO productBO = bModel
                            .getProductbyId(freeProductBO.getProductId());
                    if (productBO != null) {
                        if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && bModel.configurationMasterHelper.IS_SIH_VALIDATION
                                && bModel.configurationMasterHelper.IS_INVOICE) {
                            if (productBO.getBatchwiseProductCount() > 0) {
                                insertFreeProductWithBatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn);
                            } else {
                                insertFreeProductWithoutBatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn, flag);
                            }
                        } else {
                            insertFreeProductWithoutBatch(schemeBO, db,
                                    orderID, freeProductBO, freeDetailColumn, flag);

                        }
                    }

                }
            }
        }

    }

    /**
     * Method to use insert free product in with out batch
     *
     * @param schemeBO
     * @param db
     * @param orderID
     * @param freeProductBO
     * @param freeDetailColumn
     */
    private void insertFreeProductWithoutBatch(SchemeBO schemeBO, DBUtil db,
                                               String orderID, SchemeProductBO freeProductBO,
                                               String freeDetailColumn, String flag) {

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

            if (isFromCounterSale) {
                sb.append(0 + "," + 0);
            } else if (bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN) {

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
            if (isFromCounterSale && flag.equals("I"))
                sb.append(",'I'");
            else if (isFromCounterSale)
                sb.append(",'N'");

            sb.append("," + bModel.QT(productBO.getHsnCode()));

            if (isFromCounterSale) {
                db.insertSQL(DataMembers.tbl_CS_SchemeFreeProductDetail, freeDetailColumn,
                        sb.toString());
            } else {
                db.insertSQL(DataMembers.tbl_SchemeFreeProductDetail, freeDetailColumn,
                        sb.toString());
            }

        }
    }

    /**
     * Method to use insert free product in with batch
     *
     * @param schemeBO
     * @param db
     * @param orderID
     * @param schemeProductBo
     * @param freeDetailColumn
     */
    private void insertFreeProductWithBatch(SchemeBO schemeBO, DBUtil db,
                                            String orderID, SchemeProductBO schemeProductBo,
                                            String freeDetailColumn) {

        ArrayList<SchemeProductBatchQty> freeProductbatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bModel.productHelper
                .getProductMasterBOById(schemeProductBo.getProductId());
        if (freeProductbatchList != null) {
            StringBuffer sb = null;
            for (SchemeProductBatchQty schemeProductBatchQty : freeProductbatchList) {
                if (schemeProductBatchQty.getQty() > 0) {
                    sb = new StringBuffer();
                    sb.append(orderID + "," + schemeProductBo.getSchemeId()
                            + ",");
                    sb.append(schemeProductBo.getProductId() + ","
                            + schemeProductBatchQty.getQty() + ",");
                    sb.append(productBo.getPcUomid() + ",1,"
                            + schemeProductBatchQty.getBatchid());
                    sb.append("," + schemeBO.getSchemeId());
                    sb.append("," + bModel.getRetailerMasterBO().getRetailerID());

                    if (bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN) {

                        sb.append(productBo.getSrp());
                        sb.append("," + bModel.formatValue(schemeProductBo.getTaxAmount()));
                    } else {
                        sb.append(0 + "," + 0);
                    }

                    sb.append("," + bModel.QT(productBo.getHsnCode()));

                    if (isFromCounterSale) {
                        db.insertSQL(DataMembers.tbl_CS_SchemeFreeProductDetail, freeDetailColumn,
                                sb.toString());
                    } else {
                        db.insertSQL(DataMembers.tbl_SchemeFreeProductDetail, freeDetailColumn,
                                sb.toString());
                    }

                }
            }
        }

    }


    /**
     * @param retailerID
     * @author rajesh.k Method to use reload applied scheme objects
     */
    public void loadSchemeDetails(String retailerID) {

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select orderID from orderHeader where retailerid="
                    + bModel.QT(retailerID));
            sb.append(" and invoicestatus=0 and upload='N'");
            // if seller type selection dialog enable
            if (bModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sb.append(" and is_vansales="
                        + bModel.getRetailerMasterBO().getIsVansales());
            }


            sb.append(" and sid=" + bModel.getRetailerMasterBO().getDistributorId());

            sb.append(" and orderid not in(select orderid from OrderDeliveryDetail)");// to prevent delivered orders

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    String orderID = c.getString(0);
                    loadOrderedBuyProducts(orderID, db);
                    loadOrderedFreeProducts(orderID, db);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
        }
    }

    /**
     * @param id - use this Orderid to retrive scheme ordered buy products
     * @param db - to retrive data from SQlite
     * @author rajesh.k Method to download ordered scheme buy products
     */
    private void loadOrderedBuyProducts(String id, DBUtil db) {
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
                        schemeBO.setSelectedAmount( c.getDouble(3));
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
     * @param id
     * @param db - if true, id is OrderID,false Invoiceid
     * @author rajesh.k method to preload free product object from sqlite
     */
    private void loadOrderedFreeProducts(String id, DBUtil db) {
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
                String freeproductID = c.getString(1);
                int freeProductQty = c.getInt(2);

                setSchemeFreeProductDetails(schemeID, freeproductID,
                        freeProductQty);
            }
        }
        c.close();
    }

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
     * @param schemeId
     * @param freeProductID
     * @param qty
     * @author rajesh.k free product value downloaded from sqlite and set in
     * object
     */
    private void setSchemeFreeProductDetails(String schemeId,
                                             String freeProductID, int qty) {
        SchemeBO schemeBO = mSchemeById.get(schemeId);
        if (schemeBO != null) {
            List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
            if (freeProductList != null) {
                for (SchemeProductBO freeproductBo : freeProductList) {
                    if (freeproductBo.getProductId().equals(freeProductID)) {
                        freeproductBo.setQuantitySelected(freeproductBo
                                .getQuantitySelected() + qty);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param orderID   - for corresponding orderid
     * @param invoiceID - for corresponding invoiceid
     * @param db        - used for update records in sqlite
     * @author rajesh.k Method to use update sih in product master and
     * StockinHandMaster and update invoiceid in schemeDetail and
     * schemeFreeProductDetail table
     */
    public void updateFreeProductsSIH(String orderID, String invoiceID,
                                      DBUtil db) {
        db.updateSQL("update SchemeDetail set Invoiceid="
                + bModel.QT(invoiceID) + " where orderID=" + orderID);
        db.updateSQL("update SchemeFreeProductDetail set Invoiceid="
                + bModel.QT(invoiceID) + " where orderID=" + orderID);
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
                                    // wise
                                    // free
                                    // product
                                    // update
                                    // sih
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getOutersize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getPcUomid()
                                        || schemeProductBO.getUomID() == 0) { // piece
                                    // wise
                                    // free
                                    // product
                                    // update
                                    // sih
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected();
                                }
                                int s = productBO.getSIH() > totalFreeQty ? productBO
                                        .getSIH() - totalFreeQty
                                        : 0;
                                productBO.setSIH(s);
                                db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                        + totalFreeQty
                                        + " then ifnull(sih,0)-"
                                        + totalFreeQty
                                        + " else 0 end) where pid="
                                        + productBO.getProductID());

                                if (productBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                                    updateFreeProductSihbatchwise(
                                            schemeProductBO, db);
                                } else {

                                    db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
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

                updateSchemeCountApply(schemeBO.getParentId(),
                        schemeBO.getSchemeId(), db);
            }
        }
// update sih offinvoice scheme
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
                                    // wise
                                    // free
                                    // product
                                    // update
                                    // sih
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected()
                                            * productBO.getOutersize();
                                } else if (schemeProductBO.getUomID() == productBO
                                        .getPcUomid()
                                        || schemeProductBO.getUomID() == 0) { // piece
                                    // wise
                                    // free
                                    // product
                                    // update
                                    // sih
                                    totalFreeQty = schemeProductBO
                                            .getQuantitySelected();
                                }
                                int s = productBO.getSIH() > totalFreeQty ? productBO
                                        .getSIH() - totalFreeQty
                                        : 0;
                                productBO.setSIH(s);
                                db.executeQ("update productmaster set sih=(case when  ifnull(sih,0)>"
                                        + totalFreeQty
                                        + " then ifnull(sih,0)-"
                                        + totalFreeQty
                                        + " else 0 end) where pid="
                                        + productBO.getProductID());

                                if (productBO.getBatchwiseProductCount() > 0 && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                                    updateFreeProductSihbatchwise(
                                            schemeProductBO, db);
                                } else {

                                    db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
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

                updateSchemeCountApply(schemeBO.getParentId(),
                        schemeBO.getSchemeId(), db);
            }
        }

    }

    /**
     * Method to user update free product SIH
     *
     * @param schemeProductBo
     * @param db
     */
    private void updateFreeProductSihbatchwise(SchemeProductBO schemeProductBo,
                                               DBUtil db) {
        ArrayList<SchemeProductBatchQty> freeProductbatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bModel.productHelper
                .getProductMasterBOById(schemeProductBo.getProductId());
        if (freeProductbatchList != null) {
            for (SchemeProductBatchQty schemeProductBatchQty : freeProductbatchList) {
                if (schemeProductBatchQty.getQty() > 0) {
                    db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
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
     * this method used to reduce scheme count from scheme master table,if how
     * many times scheme apply
     *
     * @param parentID - reduce scheme count for this parentID
     */
    private void updateSchemeCountApply(int parentID, String schemeid, DBUtil db) {

        StringBuffer sb = new StringBuffer();


        // update scheme apply count retailer wise
        String query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bModel.QT(schemeid) + " and schemeApplyCount!=-1 and retailerid=" + bModel.QT(bModel.getRetailerMasterBO().getRetailerID());
        db.executeQ(query1);

        // update scheme apply count seller wise

        query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bModel.QT(schemeid) + " and schemeApplyCount!=-1 and userid=" + bModel.userMasterHelper.getUserMasterBO().getUserid();
        db.executeQ(query1);

    }

    /**
     * @param id   - it acts either invoiceid or orderid
     * @param flag - true - invoice,false - order
     * @author rajesh.k method to use show invoice report and order report
     */
    public void loadSchemeReportDetails(String id, boolean flag) {
        mAppliedSchemeList = new ArrayList<>();
        SchemeProductBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
                    String productid = c.getString(1);
                    // productBo is buy product object
                    ProductMasterBO produBo = bModel.productHelper
                            .getProductMasterBOById(productid);
                    if (produBo != null) {
                        if (produBo.getSchemeProducts() == null) {
                            produBo.setSchemeProducts(new ArrayList<SchemeProductBO>());
                        }
                        // scheme product is frree product object
                        ProductMasterBO schemeProduct = bModel.productHelper
                                .getProductMasterBOById(schemeProductBO
                                        .getProductId());
                        if (schemeProduct != null) {
                            schemeProductBO.setProductName(schemeProduct
                                    .getProductShortName());
                            schemeProductBO.setProductFullName(schemeProduct
                                    .getProductName());
                            produBo.getSchemeProducts().add(schemeProductBO);
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
                    String schemeid = c.getString(0);
                    SchemeBO schemeBO = getSchemeById().get(schemeid);
                    if (schemeBO != null) {
                        List<SchemeProductBO> buyList = schemeBO.getBuyingProducts();
                        String productid = c.getString(1);
                        String schemeType = c.getString(2);
                        double percentage = c.getDouble(3);
                        double discountValue = c.getDouble(4);

                        if (buyList != null) {
                            if (schemeType.equals(SCHEME_AMOUNT)) {
                                schemeBO.setAmountTypeSelected(true);
                                //amount column only have  total scheme amount
                                schemeBO.setSelectedAmount(c.getDouble(4));
                            } else {
                                for (SchemeProductBO schProductBO : buyList) {
                                    if (productid.equals(schProductBO.getProductId())) {
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
                        if (!schemeIdList.contains(schemeid)) {
                            mAppliedSchemeList.add(schemeBO);
                            schemeIdList.add(schemeid);
                        }


                    }
                }
            }


            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

    }

    public double updateSchemeProducts(ProductMasterBO productBo, double value,
                                       String type, boolean isBatchWise) {
        double total = 0.0;
        if (isBatchWise) {
            total = updateBatchWiseSchemeProducts(productBo, value, type);

        } else {
            double totalValue = 0.0;
            totalValue = productBo.getOrderedPcsQty() * productBo.getSrp()
                    + productBo.getOrderedCaseQty() * productBo.getCsrp()
                    + productBo.getOrderedOuterQty() * productBo.getOsrp();


            if (type.equals(SCHEME_PERCENTAGE)) {

                double totalPercentageValue = totalValue * value / 100;
                productBo.setSchemeDiscAmount(productBo.getSchemeDiscAmount() + totalPercentageValue);
                total = total + totalPercentageValue;


            } else if (type.equals(SCHEME_PRICE)) {
                int totalQty = productBo.getOrderedPcsQty()
                        + productBo.getOrderedCaseQty()
                        * productBo.getCaseSize()
                        + productBo.getOrderedOuterQty()
                        * productBo.getOutersize();
                double totalPriceDiscount = (totalQty * productBo.getSrp()) - (totalQty * ((productBo.getSrp() - value)));
                productBo.setSchemeDiscAmount(productBo.getSchemeDiscAmount() + totalPriceDiscount);
                total = total + totalPriceDiscount;

            } else if (type.equals("PRODUCT_DISC_AMT")) {
                total = total + value;

            }

        }

        return total;
    }

    /**
     * Method to get percentage and price scheme discount amount value batchwise
     *
     * @param productBO
     * @param value
     * @param type      - SCH_PER - Percentage,SCH_PR - price,PRODUCT_DISC - product Discount by percentage,
     *                  PRODUCT_DISC_AMT -product discount by amount,
     * @return
     */
    private double updateBatchWiseSchemeProducts(ProductMasterBO productBO,
                                                 double value, String type) {
        ArrayList<ProductMasterBO> batchList = bModel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());

        double totalDisPriceValue = 0.0;
        if (batchList != null) {
            for (ProductMasterBO batchProductBo : batchList) {
                if (batchProductBo.getOrderedPcsQty() > 0
                        || batchProductBo.getOrderedCaseQty() > 0
                        || batchProductBo.getOrderedOuterQty() > 0) {
                    double totalValue = 0.0;
                    if (batchProductBo.getDiscount_order_value() > 0) {
                        totalValue = batchProductBo.getDiscount_order_value();
                    } else {
                        totalValue = batchProductBo.getOrderedPcsQty()
                                * batchProductBo.getSrp()
                                + batchProductBo.getOrderedCaseQty()
                                * batchProductBo.getCsrp()
                                + batchProductBo.getOrderedOuterQty()
                                * batchProductBo.getOsrp();
                    }


                    if (type.equals(SCHEME_PERCENTAGE)) {

                        double totalpercentageValue = totalValue * value / 100;
                        batchProductBo.setSchemeDiscAmount(batchProductBo.getSchemeDiscAmount() + totalpercentageValue);

                        totalDisPriceValue = totalDisPriceValue
                                + totalpercentageValue;
                        if (batchProductBo.getDiscount_order_value() > 0) {
                            batchProductBo
                                    .setDiscount_order_value(batchProductBo
                                            .getDiscount_order_value()
                                            - totalpercentageValue);

                        } else {
                            batchProductBo.setDiscount_order_value(totalValue
                                    - totalpercentageValue);
                        }
                        if (batchProductBo.getSchemeAppliedValue() > 0) {
                            batchProductBo
                                    .setSchemeAppliedValue(batchProductBo
                                            .getSchemeAppliedValue()
                                            - totalpercentageValue);

                        } else {
                            batchProductBo.setSchemeAppliedValue(totalValue
                                    - totalpercentageValue);
                        }

                    } else if (type.equals(SCHEME_PRICE)) {
                        int totalQty = batchProductBo.getOrderedPcsQty()
                                + batchProductBo.getOrderedCaseQty()
                                * productBO.getCaseSize()
                                + batchProductBo.getOrderedOuterQty()
                                * productBO.getOutersize();
                        double totalPriceValue = totalQty * value;

                        batchProductBo.setSchemeDiscAmount(batchProductBo.getSchemeDiscAmount() + totalPriceValue);
                        totalDisPriceValue = totalDisPriceValue
                                + totalPriceValue;

                        if (batchProductBo.getDiscount_order_value() > 0) {
                            batchProductBo
                                    .setDiscount_order_value(batchProductBo
                                            .getDiscount_order_value()
                                            - totalPriceValue);
                        } else {
                            batchProductBo.setDiscount_order_value(totalValue
                                    - totalPriceValue);
                        }
                        if (batchProductBo.getSchemeAppliedValue() > 0) {
                            batchProductBo
                                    .setSchemeAppliedValue(batchProductBo
                                            .getSchemeAppliedValue()
                                            - totalPriceValue);

                        } else {
                            batchProductBo.setSchemeAppliedValue(totalValue
                                    - totalPriceValue);
                        }

                    } else if (type.equals("PRODUCT_DISC")) {
                        double totalpercentageValue = totalValue * value / 100;

                        totalDisPriceValue = totalDisPriceValue
                                + totalpercentageValue;

                    } else if (type.equals("PRODUCT_DISC_AMT")) {
                        double totalAmountValue = value;
                        totalDisPriceValue = totalDisPriceValue
                                + totalAmountValue;
                        if (batchProductBo.getDiscount_order_value() > 0) {
                            batchProductBo
                                    .setDiscount_order_value(batchProductBo
                                            .getDiscount_order_value()
                                            - totalAmountValue);
                        } else {
                            batchProductBo.setDiscount_order_value(totalValue
                                    - totalAmountValue);
                        }

                    }

                }

            }
        }

        return totalDisPriceValue;
    }

    /**
     * Method to use update free product empty bottle return
     */
    public void updataFreeProductBottleReturn() {
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





    private int getAmountBasedSchemeCount(SchemeBO schemeBO) {
        int count = 0;
        List<SchemeProductBO> schemeProductList = schemeBO.getBuyingProducts();

        for (SchemeProductBO schemeProductBO : schemeProductList) {

            ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
            if (productBO != null) {

                int totalOrderQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                if (schemeBO.getEveryUomId() == productBO.getCaseUomId()) {

                    if (productBO.getCaseSize() != 0) {
                        totalOrderQty = totalOrderQty / productBO.getCaseSize();
                        count = count + (totalOrderQty / schemeBO.getEveryQty());
                    }

                } else if (schemeBO.getEveryUomId() == productBO.getOuUomid()) {

                    if (productBO.getOutersize() != 0) {
                        totalOrderQty = totalOrderQty / productBO.getOutersize();
                        count = count + (totalOrderQty / schemeBO.getEveryQty());
                    }

                } else {
                    count = count + (totalOrderQty / schemeBO.getEveryQty());
                }
            }
        }


        return count;
    }


    /**
     * Method to use set product whether scheme available or not if scheme
     * available for a product,'setIsscheme'==1,else 0
     */
    private void setIsScheme(ArrayList<String> mGroupIDList) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            // before update scheme available or not, first reallocate all
            // product setIsScheme is zero
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

			/*
             * update scheme product setisScheme is one condition - schemeCount
			 * should not be equal zero in Schememaster table
			 */
            StringBuffer sb = new StringBuffer();
            sb.append("select  distinct(PID),SM.parentid,SCM.groupId,Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag from productMaster");
            sb.append(" inner join SchemeBuyMaster SB on ProductMaster.pID=ProductID");
            sb.append(" inner join schememaster SM  on SM. schemeID=SB.schemeID");
            sb.append(" inner join schemeApplyCountMaster SAC on SAC.schemeid=SB.schemeid");
            sb.append(" inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid");
            sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");

            sb.append(" where SM.count!=0  and SAC.schemeApplyCount!=0 and SAC.retailerid in(0," + bModel.getRetailerMasterBO().getRetailerID() + ")");
            sb.append(" and SCM.distributorid in(0," + bModel.getRetailerMasterBO().getDistributorId() + ")");
            sb.append(" and SCM.RetailerId in(0," + bModel.getRetailerMasterBO().getRetailerID() + ")");
            sb.append(" and SCM.channelid in(0," + bModel.getRetailerMasterBO().getSubchannelid() + ")");
            sb.append(" and SCM.locationid in(0," + bModel.getRetailerMasterBO().getLocationId() + ")");
            sb.append(" and SCM.accountid in(0," + bModel.getRetailerMasterBO().getAccountid() + ")");

            Cursor schemeCursor = db.selectSQL(sb.toString());
            if (schemeCursor != null) {
                if (schemeCursor.getCount() > 0) {
                    while (schemeCursor.moveToNext()) {
                        ProductMasterBO schemeProductBo = bModel.productHelper.getProductMasterBOById(schemeCursor.getString(0));
                        if (schemeCursor.getInt(3) == 0 || (schemeCursor.getInt(3) == 1 && mGroupIDList != null && mGroupIDList.contains(schemeCursor.getString(2) + schemeCursor.getString(1)))) {
                            if (schemeProductBo != null) {
                                schemeProductBo.setIsPromo(true);
                            }
                        }
                    }
                }
                schemeCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private boolean isSihAvailableForSchemeGroupFreeProducts(SchemeBO schemeBO, String groupName) {
        boolean flag = true;
        final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();

        if (freeProductList != null) {
            for (SchemeProductBO schemeProductBO : freeProductList) {
                if (groupName.equals(schemeProductBO.getGroupName())) {

                    int stock ;
                    ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                    if (productBO != null) {

                        int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                        stock = productBO.getSIH() - totalQty;

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





    public int getMaximumLineOfSchemeHeight(int schemeWidth, int parentid) {
        int maximumLength = schemeWidth / 10;
        int maximumLenghOfSchemeName = 0;
        ArrayList<String> schemeIDList = mSchemeIDListByParentID
                .get(parentid);
        for (String schemeId : schemeIDList) {
            SchemeBO schemeBO = mSchemeById.get(schemeId);
            if (schemeBO != null) {
                if (schemeBO.getScheme().length() > maximumLenghOfSchemeName) {
                    maximumLenghOfSchemeName = schemeBO.getScheme().length();
                }

            }
        }
        if (maximumLenghOfSchemeName > 0) {
            if (maximumLength == 0)
                maximumLength = 1;
            return maximumLenghOfSchemeName / maximumLength;
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

    public ArrayList<SchemeBO> getmOffInvoiceAppliedSchemeList() {
        if (mOffInvoiceAppliedSchemeList != null) {
            return mOffInvoiceAppliedSchemeList;
        }
        return new ArrayList<>();
    }

    public boolean isSameGroupAvailableinDifferentSlab(int parentId) {

        ArrayList<String> groupNameList = null;
        ArrayList<String> previousGroupNameList = null;
        ArrayList<String> schemeIdList = mSchemeIDListByParentID.get(parentId);
        if (schemeIdList != null) {
            for (String schemeid : schemeIdList) {

                groupNameList = mFreeGroupNameListBySchemeId.get(schemeid);
                if (previousGroupNameList != null) {
                    if (!previousGroupNameList.equals(groupNameList)) {
                        return false;
                    }
                }
                previousGroupNameList = mFreeGroupNameListBySchemeId.get(schemeid);
            }
        }

        return true;
    }


    /**
     * @param id   - it acts either invoiceid or orderid
     * @param flag - true - invoice,false - order
     * @author Hanifa.M method to use show invoice report and order report
     * load Accumulation Free Product's to show in Report print screen
     */
    public ArrayList<SchemeProductBO> downLoadAccumulationSchemeDetailReport(String id, boolean flag) {
        ArrayList<SchemeProductBO> mAccumulationFreePrdList = null;
        SchemeProductBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
            db.closeDB();
            Commons.printException("" + e);
        }
        return mAccumulationFreePrdList;

    }


    /**
     * This method is used to get the next available up scheme. Designed by
     * Vinoth.R for a demo.
     *
     * @param schemeId     schemeid
     * @param type         type
     * @param channelId    channel id
     * @param subChannelId subchannel id
     * @param productID    product id
     * @param quantity     quantity
     */
    public void loadSchemePromotion(String schemeId, String type,
                                    String channelId, String subChannelId, String productID,
                                    int quantity) {
        if (mSchemePromotion == null) {
            mSchemePromotion = new ArrayList<>();
        } else {
            mSchemePromotion.clear();
        }
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
                        + "BD.ProductID, PM.PName, BD.BuyQty, FD.FreeQty, FD.MaxQty, FD.Rate, FD.MaxRate FROM SchemeMaster SM "
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
                mSchemePromotion.add(schemeBO);
            }
            c.close();
        }
        db.closeDB();

    }

    public List<SchemeBO> getmSchemePromotion() {
        return mSchemePromotion;
    }

    private List<SchemeBO> mSchemePromotion;


    public ArrayList<SchemeBO> getmDisplaySchemeMasterList() {
        return mDisplaySchemeMasterList;
    }

    /**
     * Download display scheme
     *
     * @param mContext Current context
     */
    public void downloadDisplayScheme(Context mContext) {
        mDisplaySchemeMasterList = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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


    /**
     * Download display scheme applicable products
     */
    public ArrayList<SchemeBO> downloadDisplaySchemeSlabs(Context mContext) {
        mDisplaySchemeSlabs = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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

        return mDisplaySchemeSlabs;
    }

    /**
     * Download display scheme applicable products
     */
    public ArrayList<String> downloadDisplaySchemeProducts(Context mContext, String schemeId) {
        ArrayList<String> mProductList = new ArrayList<>();
        DBUtil db = null;
        try {

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            for (SchemeBO schemeBO : getDisplaySchemeSlabs()) {
                if (schemeBO.isSchemeSelected()) {

                    sb = new StringBuffer();
                    sb.append(id + ",");
                    sb.append(bModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",");
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

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            for (SchemeBO schemeBO : getDisplaySchemeTrackingList()) {
                sb = new StringBuffer();
                sb.append(id + ",");
                sb.append(bModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",");
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
}









