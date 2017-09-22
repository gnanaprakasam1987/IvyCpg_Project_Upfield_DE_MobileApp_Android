package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomRetunBo;
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
    private static final String TAG = "SchemeDetailsMasterHelper";

    /* SALES_VALUE is used for AmountType(SV) scheme apply */
    private static final String SALES_VALUE = "SV";
    /* QUANTITY_TYPE is used for Quantity Type(QTY) scheme apply */
    private static final String QUANTITY_TYPE = "QTY";

    private static final String ANY_LOGIC = "ANY"; // use ANY type scheme logic
    private static final String AND_LOGIC = "AND"; // use AND type scheme logic
    private static final String ONLY_LOGIC = "ONLY";// use ONLY type scheme
    // logic

    private static final String SCHEME_AMOUNT = "SCH_AMT";
    private static final String SCHEME_PERCENTAGE = "SCH_PER";
    private static final String SCHEME_PRICE = "SCH_PR";
    private static final String SCHEME_FREE_PRODUCT = "SCH_FPRD";
    public static final String SCHEME_PERCENTAGE_BILL = "BPER";
    private Context context;

    private BusinessModel bmodel;
    private ArrayList<SchemeBO> mOffInvoiceSchemeList;

    private static SchemeDetailsMasterHelper instance = null;

    private static final String PROCESS_TYPE_MULTIPLE_TIME_FOR_REMAINING = "MTR";
    private static final String PROCESS_TYPE_OTP = "OTP";
    private static final String PROCESS_TYPE_ONE_TIME_WITH_PERCENTAGE = "OTPR";

    private static final String PROCESS_TYPE_MTS = "MTS"; //Mulitple Time in Multiple Slab apply within the same scheme
    private static final String PROCESS_TYPE_PRORATA = "MSP"; //PRORATA-

    private ArrayList<SchemeBO> mOffInvoiceAppliedSchemeList;
    private Map<String, Integer> schemeIdCount = new HashMap<>();
    private int count = 0;

    ArrayList<String> mGroupIDList = new ArrayList<>();

    protected SchemeDetailsMasterHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static SchemeDetailsMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeDetailsMasterHelper(context);
        }
        return instance;
    }

    private List<SchemeBO> mSchemeList;
    private Map<String, SchemeBO> mSchemeById;
    private Map<String, List<SchemeBO>> mSchemeByProductId;
    private HashMap<String, ArrayList<String>> mFreeGroupTypeNameBySchemeId;
    private HashMap<String, String> mFreeGroupTypeByFreeGroupName;
    private HashMap<String, ArrayList<ProductMasterBO>> mSchemeHistoryListByschemeid;
    private HashMap<String, ArrayList<ProductMasterBO>> mSchemeHistoryValueListByschemeid;
    // HashMap used to check scheme already apply or not for same parent id
    private HashMap<Integer, ArrayList<String>> mSchemeIDListByParentID;
    // ArrayList used to store scheme parentid outlet wise
    private ArrayList<Integer> mParentIDList;
    // ArrayList used to store schemeBO,which object scheme achieved
    // successfully
    private ArrayList<SchemeBO> mApplySchemeList;


    private HashMap<String, ArrayList<Integer>> mParentIdListByProductId;
    private SparseArray<ArrayList<String>> mProductIdListByParentId;
    private HashMap<String, SchemeProductBO> mBuyProductBoBySchemeidWithpid;
    private HashMap<String, SchemeProductBO> mFreeProductBOBySchemeidWithPid;
    private SparseArray<ArrayList<String>> mProductidListByAlreadyApplySchemeId;
    private ArrayList<SchemeProductBO> mOffInvoiceSchemeFreeProductList;

    /**
     * Method to load all scheme related methods
     */
    public void downloadSchemeMethods() {

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            // download attributes for a current retailer
            //downloadRetailerAttributes();
            downloadValidSchemeGroups(db);
            downloadSchemeParentDetails(db);
            downloadBuySchemeDetails(db);
            updateFreeProducts(db);
            downloadFreeGroupName(db);
            updateFreeproductStocks();
            if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                downloadParentIdListByProduct(db);
                downloadProductIdListByParentId(db);
            }
            applyPeriodWiseScheme(db);


            setIsScheme();
            db.closeDB();

        } catch (Exception e) {
            Commons.print("" + e);
        }


    }


    public void downloadValidSchemeGroups(DBUtil db) {

        StringBuilder sb = new StringBuilder();

        ArrayList<String> retailerAttributes = bmodel.getAttributeParentListForCurrentRetailer();

        sb.append("select Distinct schemeid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SchemeAttributeMapping  SAM" +
                " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.AttributeId" +
                " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID order by schemeid,groupid");

       /* sb.append("select Distinct groupid,schemeid from SchemeAttributeMapping  SAM where schemeid not in(select schemeid from SchemeAttributeMapping where attributeid not in (" + bmodel.getRetailerAttributeList());
        sb.append("))and attributeid in(" + bmodel.getRetailerAttributeList() + ")");*/
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            int lastSchemeId = 0, lastGroupId = 0;
            boolean isGroupSatisfied = false;
            while (c.moveToNext()) {
                if (retailerAttributes != null && retailerAttributes.contains(c.getString(3))) {

                    if (lastSchemeId != c.getInt(0) || lastGroupId != c.getInt(1)) {

                        if (isGroupSatisfied) {
                            if (!mGroupIDList.contains(c.getString(1) + c.getString(0))) {
                                mGroupIDList.add(c.getString(1) + c.getString(0));
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


    }

    private boolean isSchemeApplicable(int schemeId, int groupId, int parentId) {


        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            StringBuilder sb = new StringBuilder();

            sb.append("select Distinct schemeid,groupid,EA1.AttributeName as ParentName,EA.ParentID from SchemeAttributeMapping  SAM" +
                    " inner join EntityAttributeMaster EA on EA.AttributeId = SAM.AttributeId" +
                    " Left join EntityAttributeMaster EA1 on EA1.AttributeId = EA.ParentID ");
            sb.append("where schemeid=" + schemeId + " and groupid=" + groupId + " and SAM.attributeid in(select RA.attributeid from RetailerAttribute RA" +
                    " inner join EntityAttributeMaster EA on EA.Attributeid = RA.Attributeid and EA.PArentid=" + parentId +
                    " where retailerid = " + bmodel.getRetailerMasterBO().getRetailerID() + ")");

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
     * @author rajesh.k method to use store scheme id by parent wise order by
     * highest slab
     */
    public void downloadSchemeParentDetails(DBUtil db) {
        mSchemeIDListByParentID = new HashMap<Integer, ArrayList<String>>();
        mParentIDList = new ArrayList<Integer>();

        StringBuilder sb = new StringBuilder();
        sb.append("select SM.schemeid,SM.parentid,sum(case when uomid is 0 then sbm.buyqty when SBM.uomid");
        sb.append(" is PM.duomid then  sbm.buyQty*pm.duomQty  when SBM.uomid is PM.douomid then ");
        sb.append("sbm.buyQty*pm.douomqty else buyQty  end ) as totalQty,SCM.groupId,");
        sb.append(" Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag");
        sb.append(" from schemebuymaster sbm   inner join productmaster PM  on sbm.productid =pm.pid inner join schememaster SM on SM.schemeid=sbm.schemeid ");
        sb.append("inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");
        sb.append(" where SCM.distributorid in(0," + bmodel.getRetailerMasterBO().getDistributorId() + ")");
        sb.append(" and SCM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
        sb.append(" and SCM.channelid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")");
        sb.append(" and SCM.locationid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
        sb.append(" and SCM.accountid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")");
        sb.append(" and SCM.PriorityProductId in(0," + bmodel.getRetailerMasterBO().getPrioriryProductId() + ")");
        sb.append(" group by sm.schemeid,SM.parentid order by SM.parentid,totalQty desc");


        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            int parentid = 0;
            ArrayList<String> schemeIdList = new ArrayList<String>();
            while (c.moveToNext()) {
                String schemeid = c.getString(0);

                if (c.getInt(4) == 0 || (c.getInt(4) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(3) + c.getString(1)))) {

                    if (parentid != c.getInt(1)) {
                        if (parentid != 0) {
                            mParentIDList.add(parentid);
                            mSchemeIDListByParentID.put(parentid, schemeIdList);
                            schemeIdList = new ArrayList<String>();
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

    /**
     * @return
     * @author rajesh.k method of ArrayList used to retrieve only parent id for
     * selected retailer
     */
    public ArrayList<Integer> getParentIDList() {
        if (mParentIDList != null) {
            return mParentIDList;
        }
        return new ArrayList<Integer>();
    }

    /**
     * @return
     * @author rajesh.k hash map to retrive SchemeIDlist by parent id and
     * highest slab wise for selected retailer
     */
    public HashMap<Integer, ArrayList<String>> getSchemeIdlistByParentID() {
        return mSchemeIDListByParentID;
    }

    /**
     * @author rajesh.k method to use download scheme parent details and buying
     * product details by selecting retailer wise
     */

    public void downloadBuySchemeDetails(DBUtil db) {
        mSchemeById = new HashMap<String, SchemeBO>();
        mSchemeList = new ArrayList<SchemeBO>();
        mBuyProductBoBySchemeidWithpid = new HashMap<>();

        SchemeBO schemeBO = null;
        SchemeProductBO schemeProductBo;


        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct SM.SchemeID, SM.Description, SM.Type, SM.ShortName, BD.ProductID, ");
        sb.append("PM.Psname, PM.PName, BD.BuyQty,SM.parentid,SM.count,PM.pCode,SM.buyType,BD.GroupName,BD.GroupType,");
        sb.append("SM.IsCombination,BD.uomid,UM.ListName,SAC.SchemeApplyCount,BD.ToBuyQty,SM.IsBatch,BD.Batchid,PT.ListCode,SM.IsOnInvoice,");
        sb.append(" Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag,SCM.groupid, SM.GetType, SM.IsAutoApply");
        sb.append(" FROM SchemeMaster SM left join schemeApplyCountMaster SAC on SM.schemeid=SAC.schemeID ");
        sb.append("and (SAC.retailerid=0 OR SAC.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" OR SAC.userid=0 OR SAC.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ") ");
        sb.append(" INNER JOIN  SchemeBuyMaster BD ON BD.SchemeID = SM.SchemeID ");
        sb.append("and (SAC.retailerid=0 OR SAC.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" OR SAC.userid=0 OR SAC.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ") ");
        sb.append("INNER JOIN SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid ");
        sb.append("LEFT JOIN ProductMaster PM ON BD.ProductID = PM.PID ");
        sb.append("LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON BD.uomid = UM.ListId ");
        sb.append("LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster) PT ON SM.processTypeId = PT.ListId ");
        sb.append("LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");
        sb.append(" where SCM.distributorid in(0," + bmodel.getRetailerMasterBO().getDistributorId() + ")");
        sb.append(" and SCM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
        sb.append(" and SCM.channelid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")");
        sb.append(" and SCM.locationid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
        sb.append(" and SCM.accountid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")");
        sb.append(" and SCM.PriorityProductId in(0," + bmodel.getRetailerMasterBO().getPrioriryProductId() + ")");
        sb.append(" AND SAC.schemeApplyCOunt !=0 ORDER BY SM.IsCompanyCreated,SM.schemeID ASC");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            String schemeID = "";
            // store buy products for particular scheme
            ArrayList<SchemeProductBO> buyProductList = new ArrayList<SchemeProductBO>();
            // schemeNewBO object used to store distinct scheme parent
            // object
            SchemeBO schemeNewBO = null;
            while (c.moveToNext()) {
                if (c.getInt(23) == 0 || (c.getInt(23) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(24) + c.getString(8)))) {
                    schemeBO = new SchemeBO();
                    schemeBO.setSchemeId(c.getString(0));
                    schemeBO.setSchemeDescription(c.getString(1));
                    schemeBO.setType(c.getString(2));
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


                    mBuyProductBoBySchemeidWithpid.put(schemeBO.getSchemeId() + schemeProductBo.getProductId(), schemeProductBo);

                    if (!schemeID.equals(schemeBO.getSchemeId())) {
                        if (!schemeID.equals("")) {

                            mSchemeById.put(schemeID, schemeNewBO);
                            mSchemeList.add(schemeNewBO);
                            schemeNewBO.setBuyingProducts(buyProductList);
                            buyProductList = new ArrayList<SchemeProductBO>();

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

    public HashMap<String, SchemeProductBO> getBuyProductBOBySchemeidWithPid() {
        return mBuyProductBoBySchemeidWithpid;
    }

    /**
     * Load the free products scheme wise.
     */
    private void updateFreeProducts(DBUtil db) {
        mFreeGroupTypeByFreeGroupName = new HashMap<String, String>();
        mFreeProductBOBySchemeidWithPid = new HashMap<>();


        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT distinct FD.SchemeID, SFP.productid, PM.psname, SFP.FreeQty, SFP.MaxQty, FD.Rate,");
        sb.append("FD.MaxRate,PM.PName,FD.amount,FD.maxAmount,FD.percent,");
        sb.append("FD.maxPercent,PM.pCode,SFP.uomid,SFP.GroupName,SFP.GroupType,FD.isFreeCombination,FD.Type,UM.ListName,FD.everyuomid,FD.everyQty FROM SchemeFreeMaster ");
        sb.append("FD LEFT JOIN SchemeFreeProducts as SFP on SFP.schemeid=FD.schemeid LEFT JOIN ProductMaster PM ");
        sb.append("ON SFP.ProductID = PM.PID ");
        sb.append("LEFT JOIN (SELECT ListId, ListCode, ListName FROM StandardListMaster WHERE ListType = 'PRODUCT_UOM') UM ON SFP.uomid = UM.ListId ");
        sb.append(" WHERE  ((SFP.FreeQty>0 and SFP.MaxQty>0) OR SFP.productid ISNULL)");
        sb.append(" ORDER BY FD.SchemeID,SFP.GroupName");


        Cursor c = db.selectSQL(sb.toString());

        if (c.getCount() > 0) {
            SchemeBO schemeBO;
            SchemeProductBO productBO;
            String schemeID = "";
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
                    schemeBO.setIsFreeCombination(c.getInt(16));
                    schemeBO.setFreeType(c.getString(17));
                    productBO.setUomDescription(c.getString(18));
                    schemeBO.setEveryUomId(c.getInt(19));
                    schemeBO.setEveryQty(c.getInt(20));
                    mFreeProductBOBySchemeidWithPid.put(schemeID + productBO.getProductId(), productBO);

                    if (schemeBO.getFreeProducts() == null) {
                        schemeBO.setFreeProducts(new ArrayList<SchemeProductBO>());
                    }

                    schemeBO.getFreeProducts().add(productBO);
                    mFreeGroupTypeByFreeGroupName.put(
                            schemeID + productBO.getGroupName(),
                            productBO.getGroupBuyType());
                }
            }
        }

        c.close();


    }

    public HashMap<String, String> getGroupBuyTypeByGroupName() {
        return mFreeGroupTypeByFreeGroupName;
    }

    public HashMap<String, SchemeProductBO> getFreeProductBOBySchemeidWithPid() {
        return mFreeProductBOBySchemeidWithPid;
    }


    /**
     * @author rajesh.k Method to use download free scheme group name
     */
    private void downloadFreeGroupName(DBUtil db) {
        mFreeGroupTypeNameBySchemeId = new HashMap<String, ArrayList<String>>();
        ArrayList<String> freeBuyNameList = new ArrayList<String>();

        Cursor c = db
                .selectSQL("select distinct SFM.schemeID,SFP.GroupName from schemeFreeMaster SFM inner Join SchemeFreeProducts SFP "
                        + "on SFM.schemeID=SFP.schemeID where SFP.freeQty>0 and SFP.maxQty>0 order by SFM.schemeid,SFP.GroupName");
        if (c.getCount() > 0) {
            String schemeID = "";
            while (c.moveToNext()) {

                if (!schemeID.equals(c.getString(0))) {
                    if (!schemeID.equals("")) {
                        mFreeGroupTypeNameBySchemeId.put(schemeID,
                                freeBuyNameList);
                        freeBuyNameList = new ArrayList<String>();
                        freeBuyNameList.add(c.getString(1));
                        schemeID = c.getString(0);

                    } else {
                        freeBuyNameList.add(c.getString(1));
                        schemeID = c.getString(0);

                    }
                } else {
                    freeBuyNameList.add(c.getString(1));
                }

                if (freeBuyNameList.size() > 0) {
                    mFreeGroupTypeNameBySchemeId.put(schemeID,
                            freeBuyNameList);
                }

            }
        }
        c.close();

    }

    private void downloadOffInvoiceFreeGroupName(DBUtil db) {
        if (mFreeGroupTypeNameBySchemeId == null)
            mFreeGroupTypeNameBySchemeId = new HashMap<>();

        ArrayList<String> freeBuyNameList = new ArrayList<String>();

        Cursor c = db
                .selectSQL("select distinct slabid,GroupName from AccumulationSchemeFreeIssues   order by slabid,GroupName");
        if (c.getCount() > 0) {
            String schemeID = "";
            while (c.moveToNext()) {

                if (!schemeID.equals(c.getString(0))) {
                    if (!schemeID.equals("")) {
                        mFreeGroupTypeNameBySchemeId.put(schemeID,
                                freeBuyNameList);
                        freeBuyNameList = new ArrayList<String>();
                        freeBuyNameList.add(c.getString(1));
                        schemeID = c.getString(0);

                    } else {
                        freeBuyNameList.add(c.getString(1));
                        schemeID = c.getString(0);

                    }
                } else {
                    freeBuyNameList.add(c.getString(1));
                }

                if (freeBuyNameList.size() > 0) {
                    mFreeGroupTypeNameBySchemeId.put(schemeID,
                            freeBuyNameList);
                }

            }
        }
        c.close();

    }

    public HashMap<String, ArrayList<String>> getFreeProductBuyNameListBySchemeID() {
        return mFreeGroupTypeNameBySchemeId;
    }

    public List<SchemeBO> getSchemesByProduct(String productId) {
        return mSchemeByProductId.get(productId);
    }

    /**
     * @param schemeBO                - combination parent schemeBO
     * @param groupSchemeName         - ArrayList of child scheme name
     * @param groupBuyTypeBygroupName - HashMap key - child scheme name and value - child scheme
     *                                type (ex : AND,ONLY or AND)
     * @return true - if any one of child scheme done, combination scheme also
     * done
     * @author rajesh.k
     * <p>
     * Check ANY type of combination scheme
     */
    private boolean isCombinationAnyLogicSchemeDone(SchemeBO schemeBO,
                                                    ArrayList<String> groupSchemeName,
                                                    HashMap<String, String> groupBuyTypeBygroupName, int parentid) {
        boolean isSchemeDone = false;

        for (String groupName : groupSchemeName) {
            String groupBuyType = groupBuyTypeBygroupName.get(groupName);
            if (groupBuyType.equals(AND_LOGIC)) {
                isSchemeDone = isChildAndLogicDone(schemeBO, groupName, parentid);
                if (isSchemeDone) {
                    return isSchemeDone;
                }
            } else if (groupBuyType.equals(ANY_LOGIC)) {
                isSchemeDone = isChildAnyLogicDone(schemeBO, groupName, parentid);
                if (isSchemeDone) {
                    return isSchemeDone;
                }
            } else if (groupBuyType.equals(ONLY_LOGIC)) {
                isSchemeDone = isChildAndLogicDone(schemeBO, groupName, parentid);
                if (isSchemeDone) {
                    return isSchemeDone;
                }
            }

        }

        return isSchemeDone;
    }

    /**
     * @param schemeBO                - combination parent schemeBO
     * @param groupSchemeName         - ArrayList of child scheme name
     * @param groupBuyTypeBygroupName - HashMap key - child scheme name and value - child scheme
     *                                type (ex : AND,ONLY or ANY)
     * @return true - if All child scheme done, combination scheme also done
     * @author rajesh.k Check AND type of combination scheme
     */
    private boolean isCombinationAndLogicSchemeDone(SchemeBO schemeBO,
                                                    ArrayList<String> groupSchemeName,
                                                    HashMap<String, String> groupBuyTypeBygroupName, int parentid) {
        boolean isSchemeDone = true;

        for (String groupName : groupSchemeName) {
            String groupBuyType = groupBuyTypeBygroupName.get(groupName);
            if (groupBuyType.equals(AND_LOGIC)) {
                isSchemeDone = isChildAndLogicDone(schemeBO, groupName, parentid);
                if (!isSchemeDone) {
                    return false;
                }
            } else if (groupBuyType.equals(ANY_LOGIC)) {
                isSchemeDone = isChildAnyLogicDone(schemeBO, groupName, parentid);
                if (!isSchemeDone) {
                    return false;
                }
            } else if (groupBuyType.equals(ONLY_LOGIC)) {
                isSchemeDone = isChildAndLogicDone(schemeBO, groupName, parentid);
                if (!isSchemeDone) {
                    return false;
                }
            }
        }

        return isSchemeDone;
    }

    /**
     * @param schemeBO  - Combination schemeBO
     * @param groupName - child scheme group name
     * @return true - if AND and ONLY Logic Done,child scheme Done
     * @author rajesh.k this method is used to child scheme hit or not,if child
     * scheme logic is AND or ONLY
     */
    private boolean isChildAndLogicDone(SchemeBO schemeBO, String groupName, int parentid) {

        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();
        ArrayList<ProductMasterBO> schemeAccumulationList = null;
        if (bmodel.schemeDetailsMasterHelper.getSchemeHistoryListBySchemeid() != null) {
            schemeAccumulationList = bmodel.schemeDetailsMasterHelper

                    .getSchemeHistoryListBySchemeid().get(schemeBO.getParentId() + "");
        }
        ArrayList<String> productIdList = null;
        if (bmodel.schemeDetailsMasterHelper.getProdcutIdListByAlreadyAppliedSchemeId() != null) {
            productIdList = bmodel.schemeDetailsMasterHelper.getProdcutIdListByAlreadyAppliedSchemeId().get(Integer.parseInt(schemeBO.getSchemeId()));
        }
        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {

            if (productIdList != null) {
                if (productIdList.contains(schemeProductBo.getProductId())) {
                    return false;
                }
            }
            if (schemeProductBo.getGroupName().equals(groupName)) {
                ProductMasterBO productBo = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBo.getProductId());
                if (productBo != null) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        int orderedTotalqtyUomwise = 0;
                        int totalQty = 0;
                        if (schemeBO.isBatchWise() && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            // batch wise scheme
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBo.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batch : batchList) {
                                    if (batch.getBatchid().equals(schemeProductBo.getBatchId()))
                                        totalQty = productBo.getOrderedPcsQty()
                                                + (productBo.getOrderedCaseQty() * productBo
                                                .getCaseSize())
                                                + (productBo.getOrderedOuterQty() * productBo
                                                .getOutersize());
                                }
                            }

                        } else {
                            totalQty = productBo.getOrderedPcsQty()
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCaseSize())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOutersize());
                        }
                        if (totalQty > 0) {
                            schemeBO.setOrderedProductCount(schemeBO.getOrderedProductCount() + 1);
                        }
                        /* scheme accumulation starts */
                        if (schemeAccumulationList != null) { // Added scheme
                            // accumulation
                            // details
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

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()).toString());
                            totalQty = totalQty - totalAppliedQty;

                        }

						/* scheme accumulation ends */

                        if (schemeProductBo.getUomID() != 0) {

                            if (productBo.getCaseUomId() == schemeProductBo
                                    .getUomID()) { // check case wise scheme
                                if (productBo.getCaseSize() != 0) {
                                    orderedTotalqtyUomwise = totalQty
                                            / productBo.getCaseSize();
                                }
                            } else if (productBo.getOuUomid() == schemeProductBo
                                    .getUomID()) { // check outer wise scheme
                                if (productBo.getOutersize() != 0) {
                                    orderedTotalqtyUomwise = totalQty
                                            / productBo.getOutersize();
                                }
                            } else { // check piece wise scheme
                                orderedTotalqtyUomwise = totalQty;
                            }
                        } else {
                            orderedTotalqtyUomwise = totalQty;
                        }

                        if (schemeProductBo.getBuyQty() > orderedTotalqtyUomwise) {

                            return false;
                        }
                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {


                        double totalvalue = 0;
                        if (productBo.getBatchwiseProductCount() > 0 && schemeBO.isBatchWise() && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            totalvalue = getbatchWiseTotalValue(productBo, schemeProductBo.getBatchId());
                        } else {
                            totalvalue = (productBo.getOrderedPcsQty() * productBo
                                    .getSrp())
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCsrp())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOsrp());
                        }


                        if (schemeAccumulationList != null) {
                            /* scheme accumulation starts */
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (productBo.getProductID().equals(
                                        schemeAccBO.getProductID()) && (!schemeBO.isBatchWise() || (schemeBO.isBatchWise() && schemeProductBo.getBatchId().equals(schemeAccBO.getBatchid())))) {


                                    totalvalue = totalvalue
                                            + schemeAccBO.getTotalamount();
                                    break;
                                }
                            }
                            /* scheme accumulation ends */
                        }

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId());
                            totalvalue = totalvalue - totalAppliedQty;

                        }

                        if (totalvalue > 0) {
                            schemeBO.setOrderedProductCount(schemeBO.getOrderedProductCount() + 1);
                        }
                        if (schemeProductBo.getBuyQty() > totalvalue) {
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
     * @param schemeBO  - Combination schemeBO
     * @param groupName - child scheme group name
     * @return true - if ANY Logic Done,child scheme Done
     * @author rajesh.k this method will call,when child scheme buytype is ANY
     */
    private boolean isChildAnyLogicDone(SchemeBO schemeBO, String groupName, int parentid) {

        int totalQty = 0;
        double totalValue = 0;
        List<SchemeProductBO> schemeBuyProducts = schemeBO.getBuyingProducts();
        ArrayList<ProductMasterBO> schemeAccumulationList = null;
        if (bmodel.schemeDetailsMasterHelper.getSchemeHistoryListBySchemeid() != null) {
            schemeAccumulationList = bmodel.schemeDetailsMasterHelper

                    .getSchemeHistoryListBySchemeid().get(schemeBO.getParentId() + "");
        }

        ArrayList<String> productIdList = null;
        if (bmodel.schemeDetailsMasterHelper.getProdcutIdListByAlreadyAppliedSchemeId() != null) {
            productIdList = bmodel.schemeDetailsMasterHelper.getProdcutIdListByAlreadyAppliedSchemeId().get(Integer.parseInt(schemeBO.getSchemeId()));
        }
        for (SchemeProductBO schemeProductBo : schemeBuyProducts) {
            // already scheme applied in previous days
            if (productIdList != null && productIdList.contains(schemeProductBo.getProductId())) {
                break;
            }
            if (schemeProductBo.getGroupName().equals(groupName)) {
                ProductMasterBO productBo = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBo.getProductId());
                if (productBo != null) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        int orderedTotalQtybyUomwise = 0;
                        int totalProductQty = 0;
                        if (schemeBO.isBatchWise() && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            // batch wise scheme
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBo.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batch : batchList) {
                                    if (batch.getBatchid().equals(schemeProductBo.getBatchId()))
                                        totalQty = batch.getOrderedPcsQty()
                                                + (batch.getOrderedCaseQty() * productBo
                                                .getCaseSize())
                                                + (batch.getOrderedOuterQty() * productBo
                                                .getOutersize());
                                }
                            }

                        } else {
                            totalProductQty = productBo.getOrderedPcsQty()
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCaseSize())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOutersize());
                        }
                        if (totalProductQty > 0) {
                            schemeBO.setOrderedProductCount(schemeBO.getOrderedProductCount() + 1);
                        }

						/* scheme accumulation starts */
                        if (schemeAccumulationList != null) { // Added scheme
                            // accumulation
                            // details
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

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()).toString());
                            totalProductQty = totalProductQty - totalAppliedQty;
                        }

                        if (schemeProductBo.getUomID() != 0) {
                            if (productBo.getCaseUomId() == schemeProductBo
                                    .getUomID()) { // check case wise scheme
                                if (productBo.getCaseSize() != 0) {
                                    orderedTotalQtybyUomwise = totalProductQty
                                            / productBo.getCaseSize();
                                    totalQty = totalQty
                                            + orderedTotalQtybyUomwise;
                                }
                            } else if (productBo.getOuUomid() == schemeProductBo
                                    .getUomID()) { // check outer wise scheme
                                if (productBo.getOutersize() != 0) {
                                    orderedTotalQtybyUomwise = totalProductQty
                                            / productBo.getOutersize();
                                    totalQty = totalQty
                                            + orderedTotalQtybyUomwise;
                                }
                            } else { // check piece wise scheme
                                orderedTotalQtybyUomwise = totalProductQty;
                                totalQty = totalQty + orderedTotalQtybyUomwise;
                            }
                        } else {
                            orderedTotalQtybyUomwise = totalProductQty;
                            totalQty = totalQty + orderedTotalQtybyUomwise;
                        }

                        if (schemeProductBo.getBuyQty() <= totalQty) {

                            return true;
                        }
                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                        double totalProductvalue = 0.0;
                        if (productBo.getBatchwiseProductCount() > 0 && schemeBO.isBatchWise() && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                            totalProductvalue = getbatchWiseTotalValue(productBo, schemeProductBo.getBatchId());
                        } else {
                            totalProductvalue = (productBo.getOrderedPcsQty() * productBo
                                    .getSrp())
                                    + (productBo.getOrderedCaseQty() * productBo
                                    .getCsrp())
                                    + (productBo.getOrderedOuterQty() * productBo
                                    .getOsrp());
                        }


                        if (schemeAccumulationList != null) {
                            /* scheme accumulation starts */
                            for (ProductMasterBO schemeAccBO : schemeAccumulationList) {
                                if (productBo.getProductID().equals(
                                        schemeAccBO.getProductID()) && (!schemeBO.isBatchWise() || (schemeBO.isBatchWise() && schemeProductBo.getBatchId().equals(schemeAccBO.getBatchid())))) {
                                    totalProductvalue = totalProductvalue
                                            + schemeAccBO.getTotalamount();
                                    break;
                                }
                            }
                            /* scheme accumulation ends */
                        }

                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId()) != null) {
                            int totalAppliedQty = mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentid + schemeProductBo.getProductId());
                            totalProductvalue = totalProductvalue - totalAppliedQty;
                        }


                        if (totalProductvalue > 0) {
                            schemeBO.setOrderedProductCount(schemeBO.getOrderedProductCount() + 1);
                        }
                        totalValue = totalValue + totalProductvalue;
                        if (schemeProductBo.getBuyQty() <= totalValue) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }

    public double getbatchWiseTotalValue(ProductMasterBO productBO) {

        ArrayList<ProductMasterBO> batchWiseList = bmodel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        double totalValue = 0.0;
        if (batchWiseList != null) {
            for (ProductMasterBO batchProductBO : batchWiseList) {
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
        batchWiseList = null;
        return totalValue;

    }

    public double getbatchWiseTotalValue(ProductMasterBO productBO, String batchId) {

        ArrayList<ProductMasterBO> batchWiseList = bmodel.batchAllocationHelper
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
        batchWiseList = null;
        return totalValue;

    }

    /**
     * After scheme achieved successfully ,this method used to update the scheme
     * details like free product,percentage,amount and price
     *
     * @param schemeBO
     * @param maxSchemeValue
     */
    private void calculateSchemePerPieceNew(SchemeBO schemeBO,
                                            int maxSchemeValue) {

        if (schemeBO.getFreeProducts() != null) {

            int quantityActual = 0;
            int quantityMax = 0;
            double priceActual = 0;
            double priceMax = 0;
            double minPrecent = 0;
            double maxPrecent = 0;
            double minAmount = 0;
            double maxAmount = 0;

            for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {
                int minimumQuantity = schemeProductBO.getQuantityMinimum();

                int maximumQuantity = schemeProductBO.getQuantityMaximum();

                if (bmodel.configurationMasterHelper.SCHEME_APPLY_REM || (schemeBO.getProcessType() != null && schemeBO.getProcessType().equals(PROCESS_TYPE_MULTIPLE_TIME_FOR_REMAINING))) {//bmodel.configurationMasterHelper.SCHEME_APPLY_REM) {

					/* scheme type is Quantity Value */
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {

                        /** Calculate for quantity **/

                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        /** Calculate for amount discount **/

                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;


                    }
                    /* scheme type is Sales Value */
                    else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        // int qty = (int) (orderedtotalValue / schemeBO
                        // .getQuantity());

                        /** Calculate for quantity **/

                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        /** Calculate for amount discount **/

                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;

                    }

                } else if (schemeBO.getProcessType() != null && schemeBO.getProcessType().equals(PROCESS_TYPE_OTP)) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            quantityActual += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            quantityMax += remainingMaxQty;

                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;

                        // adding calculated percent of amount
                        minAmount = minAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxAmount = maxAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            quantityActual += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            quantityMax += remainingMaxQty;


                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;

                        // adding calculated percent of amount
                        minAmount = minAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxAmount = maxAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    }
                } else if (schemeBO.getProcessType() != null && schemeBO.getProcessType().equals(PROCESS_TYPE_PRORATA)) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            quantityActual += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            quantityMax += remainingMaxQty;

                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;

                        // adding calculated percent of amount
                        minAmount = minAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxAmount = maxAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        quantityActual = minimumQuantity * maxSchemeValue;
                        quantityMax = maximumQuantity * maxSchemeValue;

                        int remainingActualQty = (int) ((schemeBO.getBalancePercent() / 100) * minimumQuantity);
                        if (remainingActualQty >= 1)
                            quantityActual += remainingActualQty;

                        int remainingMaxQty = (int) ((schemeBO.getBalancePercent() / 100) * maximumQuantity);
                        if (remainingMaxQty >= 1)
                            quantityMax += remainingMaxQty;


                        minAmount = schemeProductBO.getMinAmount()
                                * maxSchemeValue;
                        maxAmount = schemeProductBO.getMaxAmount()
                                * maxSchemeValue;

                        // adding calculated percent of amount
                        minAmount = minAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMinAmount());
                        maxAmount = maxAmount + ((schemeBO.getBalancePercent() / 100) * schemeProductBO.getMaxAmount());

                    }
                } else {
                    quantityActual = Math.round((float) minimumQuantity);
                    quantityMax = Math.round((float) maximumQuantity);
                    if (schemeBO.getEveryQty() != 0) {
                        int count = getAmountBasedSchemeCount(schemeBO);
                        if (count == 0) count = 1; // atleast one time apply

                        minAmount = schemeProductBO.getMinAmount() * count;
                        maxAmount = schemeProductBO.getMaxAmount() * count;

                    } else {


                        minAmount = schemeProductBO
                                .getMinAmount();
                        maxAmount = schemeProductBO
                                .getMaxAmount();
                    }

                }

                schemeProductBO.setQuantityActualCalculated(quantityActual);
                schemeProductBO.setQuantityMaxiumCalculated(quantityMax);

                /** no calculation required for Price discount and % discount **/

                priceActual = schemeProductBO.getPriceActual();
                priceMax = schemeProductBO.getPriceMaximum();

                minPrecent = schemeProductBO.getMinPercent();
                maxPrecent = schemeProductBO.getMaxPercent();

                schemeProductBO.setMinPercentCalculated(minPrecent);
                schemeProductBO.setMaxPrecentCalculated(maxPrecent);

                schemeProductBO.setMinAmountCalculated(minAmount);
                schemeProductBO.setMaxAmountCalculated(maxAmount);

            }

            schemeBO.setActualQuantity(quantityActual);
            schemeBO.setMaximumQuantity(quantityMax);

            schemeBO.setActualPrice(priceActual);
            schemeBO.setMaximumPrice(priceMax);
            schemeBO.setSelectedPrice(priceActual);

            schemeBO.setMinimumPrecent(minPrecent);
            schemeBO.setMaximumPrecent(maxPrecent);
            schemeBO.setSelectedPrecent(minPrecent);

            schemeBO.setMinimumAmount(minAmount);
            schemeBO.setMaximumAmount(maxAmount);
            schemeBO.setSelectedAmount(minAmount);

        }
    }

    public List<SchemeBO> getmSchemeList() {
        return mSchemeList;
    }

    public void setmSchemeList(List<SchemeBO> mSchemeList) {
        this.mSchemeList = mSchemeList;
    }

    public Map<String, SchemeBO> getmSchemeById() {
        return mSchemeById;
    }

    public void setmSchemeById(Map<String, SchemeBO> mSchemeById) {
        this.mSchemeById = mSchemeById;
    }

    /**
     * update scheme with combination
     *
     * @param schemeBO
     */
    public void freeCombinationAvailable(SchemeBO schemeBO) {

        ArrayList<String> freeGroupNameList = bmodel.schemeDetailsMasterHelper
                .getFreeProductBuyNameListBySchemeID().get(
                        schemeBO.getSchemeId());
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

                    if (!bmodel.configurationMasterHelper.IS_SIH_VALIDATION || isSihAvailableForFreeProducts) {

                        for (SchemeProductBO schemePdtBO : freeProducts) {

                            if (freeGroupName.equals(schemePdtBO.getGroupName())) {

                                int freeQuantity = schemePdtBO
                                        .getQuantityActualCalculated();

                                int stock = 0;
                                productMasterBO = bmodel.productHelper
                                        .getProductMasterBOById(schemePdtBO
                                                .getProductId());

                                if (productMasterBO != null) {
                                    stock = productMasterBO.getSIH()
                                            - ((productMasterBO.getOrderedCaseQty() * productMasterBO
                                            .getCaseSize())
                                            + (productMasterBO
                                            .getOrderedOuterQty() * productMasterBO
                                            .getOutersize()) + productMasterBO
                                            .getOrderedPcsQty());
                                }

                                schemePdtBO.setStock(stock);

                                schemePdtBO.setQuantitySelected(0);


                                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                    if (bmodel.getResources().getBoolean(
                                            R.bool.config_is_sih_considered)) {
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
                                } else {
                                    schemePdtBO.setQuantitySelected(freeQuantity);
                                }

                                if (schemePdtBO.getGroupBuyType().equals(ANY_LOGIC)) { // child
                                    // ANY
                                    // logic
                                    break;
                                }

                            }

                        }
                        if (schemeBO.getFreeType().equals(ANY_LOGIC)) {
                            break; // used for parent ANY LOGIC
                        }
                    }
                }
            }
        } else if (schemeBO.getActualPrice() > 0
                && schemeBO.getMaximumPrice() > 0) {
            schemeBO.setPriceTypeSeleted(true);
        } else if (schemeBO.getMinimumAmount() > 0
                && schemeBO.getMaximumAmount() > 0) {
            schemeBO.setAmountTypeSelected(true);
        } else if (schemeBO.getMinimumPrecent() > 0
                && schemeBO.getMaximumPrecent() > 0) {
            schemeBO.setDiscountPrecentSelected(true);
        }

    }

    /**
     * update scheme without combination
     *
     * @param schemeBO
     */
    public void freeCombinationNotAvailable(SchemeBO schemeBO) {

        int freeQuantity = schemeBO.getActualQuantity();
        schemeBO.setQuantityTypeSelected(true);

        List<SchemeProductBO> freeProducts = schemeBO.getFreeProducts();
        ProductMasterBO productMasterBO;

        if (freeProducts != null) {
            for (SchemeProductBO schemePdtBO : freeProducts) {
                if (schemeBO.getFreeType().equals(AND_LOGIC)) {
                    freeQuantity = schemePdtBO.getQuantityActualCalculated();
                }

                int stock = 0;
                productMasterBO = bmodel.productHelper
                        .getProductMasterBOById(schemePdtBO.getProductId());

                if (productMasterBO != null) {
                    stock = productMasterBO.getSIH()
                            - ((productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize()) + productMasterBO
                            .getOrderedPcsQty());
                }


                schemePdtBO.setStock(stock);

                schemePdtBO.setQuantitySelected(0);
                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    if (bmodel.getResources().getBoolean(
                            R.bool.config_is_sih_considered)) {
                        if (stock > 0) {

                            if ((stock - freeQuantity) >= 0) {
                                schemePdtBO.setQuantitySelected(freeQuantity);
                                freeQuantity = 0;
                            } else {
                                schemePdtBO.setQuantitySelected(stock);
                                freeQuantity -= stock;
                            }
                        }
                    } else {
                        schemePdtBO.setQuantitySelected(freeQuantity);
                        freeQuantity = 0;
                    }
                } else {
                    schemePdtBO.setQuantitySelected(freeQuantity);
                    freeQuantity = 0;
                }

            }
        }

    }

    /**
     * @param schemeProductList
     * @param schemeGroupName
     * @param schemeGroupBuyTypeBygroupname
     * @param isQuantityType                true - qty false- salesvalue
     * @return
     * @author rajesh.k Method to use getcombination AND logic buy qty
     */
    private int getCombinationAndLogicBuyQty(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeBygroupname,
            boolean isQuantityType, boolean isBatchWise, int parentId, String processType, boolean isHighestSlab) {
        int tempCount = 0;
        double tempBalancePercent = 0;
        for (String s : schemeGroupName) {
            String type = schemeGroupBuyTypeBygroupname.get(s);
            if (type.equals(AND_LOGIC)) {

                int count = 1;
                if (isQuantityType) {
                    count = getAndLogicCount(schemeProductList, s, true, isBatchWise, parentId, processType, isHighestSlab);
                } else {
                    count = getAndLogicCountForSalesValue(schemeProductList,
                            s, true, isBatchWise, parentId, processType, isHighestSlab);
                }
                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            } else if (type.equals(ANY_LOGIC)) {
                int count = 1;
                if (isQuantityType) {
                    count = getAnyLogicCount(schemeProductList, s, true, isBatchWise, parentId, processType, isHighestSlab);
                } else {
                    count = getAnyLogicCountForSalesValue(schemeProductList, s,
                            true, isBatchWise, parentId, processType, isHighestSlab);
                }

                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            } else if (type.equals(ONLY_LOGIC)) {
                int count = 1;

                if (isQuantityType) {
                    count = getAndLogicCount(schemeProductList, s, true, isBatchWise, parentId, processType, isHighestSlab);
                } else {
                    count = getAndLogicCountForSalesValue(schemeProductList,
                            s, true, isBatchWise, parentId, processType, isHighestSlab);
                }

                if (tempCount > count || tempCount == 0) {
                    tempCount = count;
                }

                if (tempBalancePercent == 0 || tempBalancePercent > getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();
                }

            }


        }

        //
        setBalancePercent(tempBalancePercent);
        if (tempCount == 0) tempCount = 1;

        return tempCount;
    }

    /**
     * @param schemeProductList
     * @param schemeGroupName
     * @param schemeGroupBuyTypeBygroupname
     * @param isQuantityType                true - qty type ,false - salesvalue type
     * @return
     * @author rajesh.k method to use total buy qty in combination any logic
     */
    private int getCombinationAnyLogicBuyQty(
            List<SchemeProductBO> schemeProductList,
            ArrayList<String> schemeGroupName,
            HashMap<String, String> schemeGroupBuyTypeBygroupname,
            boolean isQuantityType, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {
        int tempCount = 1;
        double tempBalancePercent = 0;
        for (String s : schemeGroupName) {
            String type = schemeGroupBuyTypeBygroupname.get(s);
            if (type.equals(AND_LOGIC) || type.equals(ONLY_LOGIC)) {
                int count = 0;
                if (isQuantityType) {
                    count = getAndLogicCount(schemeProductList, s, true, isBatchWise, parentID, processType, isHighestSlab);
                } else {
                    count = getAndLogicCountForSalesValue(schemeProductList,
                            s, true, isBatchWise, parentID, processType, isHighestSlab);
                }

                if (tempCount < count) {
                    tempCount = count;

                }

                //
                if (tempBalancePercent < getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();

                }

            } else if (type.equals(ANY_LOGIC)) {
                int count = 0;
                if (isQuantityType) {
                    count = getAnyLogicCount(schemeProductList, s, true, isBatchWise, parentID, processType, isHighestSlab);
                } else {
                    count = getAnyLogicCountForSalesValue(schemeProductList, s,
                            true, isBatchWise, parentID, processType, isHighestSlab);
                }

                if (tempCount < count) {
                    tempCount = count;
                }

                //
                if (tempBalancePercent < getBalancePercent()) {
                    tempBalancePercent = getBalancePercent();

                }

            }
        }

        // (ANY) setting largest balance percent value as final balance
        setBalancePercent(tempBalancePercent);

        return tempCount;
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
     * @param schemeProductList
     * @param groupName
     * @return
     * @author rajesh.k get count of And logic combination scheme for Qty type
     */
    private int getAndLogicCount(List<SchemeProductBO> schemeProductList,
                                 String groupName, boolean isCombination, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {
        int tempCount = 0;
        double balancePercent = 0;
        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)
                    || !isCombination) {

                int count = 0;
                ProductMasterBO productMasterBO = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());

                if (productMasterBO != null) {
                    ArrayList<ProductMasterBO> schemeAccumulationList = null;
                    if (bmodel.schemeDetailsMasterHelper
                            .getSchemeHistoryListBySchemeid() != null) {

                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());

                        schemeAccumulationList = bmodel.schemeDetailsMasterHelper
                                .getSchemeHistoryListBySchemeid().get(
                                        schemeBO.getParentId() + "");
                    }
                    int quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize())
                            + productMasterBO.getOrderedPcsQty();

                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) != null) {
                            quantity = quantity - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()).toString());
                        }
                    }

					/* scheme accumulation starts */
                    if (schemeAccumulationList != null) { // Added scheme
                        // accumulation
                        // details
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

                                }

                            } else {
                                count = quantity
                                        / (int) schemeProductBO.getTobuyQty();
                                balanceQty = quantity
                                        % (int) schemeProductBO.getTobuyQty();
                            }
                        } else {
                            count = quantity / (int) schemeProductBO.getTobuyQty();
                            balanceQty = quantity
                                    % (int) schemeProductBO.getTobuyQty();

                        }

                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productMasterBO.getProductID()), (quantity - balanceQty));

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
                sb.append("InvoiceID =" + bmodel.QT(id));
            } else {// order report
                sb.append("OrderID =" + bmodel.QT(id));
            }
            sb.append(" AND SchemeType = '" + SCHEME_PERCENTAGE + "'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    // schemeProductBO.setQuantitySelected(c.getInt(3));
                    // schemeProductBO.setUomID(c.getInt(4));
                    String productid = c.getString(1);
                    // productBo is buy product object
                    ProductMasterBO produBo = bmodel.productHelper
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
     * @param schemeProductList
     * @param groupName
     * @return
     * @author rajesh.k get count of And logic combination scheme for Salesvalue
     * type
     */
    private int getAndLogicCountForSalesValue(
            List<SchemeProductBO> schemeProductList, String groupName,
            boolean isCombination, boolean isBatchWise, int parentId, String processType, boolean isHighestSlab) {

        int tempCount = 0;
        double tempBalancePercent = 0;
        double tempBal = 0;
        double balancePercent = 0;
        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)
                    || !isCombination) {
                int count = 0;
                ProductMasterBO productMasterBO = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());

                if (productMasterBO != null) {

                    int quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize())
                            + productMasterBO.getOrderedPcsQty();

                    double totalValue = 0;
                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        totalValue = getbatchWiseTotalValue(productMasterBO, schemeProductBO.getBatchId());
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
                            quantity = quantity - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentId + productMasterBO.getProductID()).toString());
                        }
                    }

					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationValueList = null;
                    if (bmodel.schemeDetailsMasterHelper
                            .getSchemeHistoryListBySchemeid() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationValueList = bmodel.schemeDetailsMasterHelper

                                .getSchemeHistoryListBySchemeid().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationValueList != null) { // Added scheme
                        // accumulation
                        // details
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

                    if (quantity > 0) {
                        double balanceValue = 0;
                        if (schemeProductBO.getTobuyQty() > 0
                                && schemeProductBO.getBuyQty() > 0) {
                            count = (int) totalValue
                                    / (int) schemeProductBO.getTobuyQty();

                            balanceValue = totalValue
                                    % schemeProductBO.getTobuyQty();
                        } else {
                            balanceValue = totalValue;

                        }

                        mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentId + productMasterBO.getProductID()), (int) (quantity - balanceValue));

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
			 * for computation to how many times scheme achived. but minimum buy
			 * qty used to apply scheme or not.
			 */
            return 1;
        }

    }

    /**
     * @param schemeProductList
     * @param groupName
     * @return
     * @author rajesh.k get count of Any logic combination scheme of QTY type
     */
    private int getAnyLogicCount(List<SchemeProductBO> schemeProductList,
                                 String groupName, boolean isCombination, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {
        int count = 0;
        int totalQty = 0;
        double tempBalancePercent = 0;
        // selectedFromBuyQty is used to store minimum buy qty
        double selectedFromBuyQty = 0;
        // selectedtoBuyQty is used to store maximum buy qty
        double selectedtoBuyQty = 0;
        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)
                    || !isCombination) {
                selectedtoBuyQty = schemeProductBO.getTobuyQty();
                selectedFromBuyQty = schemeProductBO.getBuyQty();
                ProductMasterBO productMasterBO = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());
                if (productMasterBO != null) {
                    int quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize())
                            + productMasterBO.getOrderedPcsQty();

                    //Quantity used(if previous slab applied) for scheme are reduced here.
                    if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                        if (mAchieved_qty_or_salesValue_by_schemeId_nd_productid != null && mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()) != null) {
                            quantity = quantity - Integer.parseInt(mAchieved_qty_or_salesValue_by_schemeId_nd_productid.get(parentID + productMasterBO.getProductID()).toString());
                        }
                    }


					/* scheme accumulation starts */
                    ArrayList<ProductMasterBO> schemeAccumulationList = null;
                    if (bmodel.schemeDetailsMasterHelper
                            .getSchemeHistoryListBySchemeid() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationList = bmodel.schemeDetailsMasterHelper

                                .getSchemeHistoryListBySchemeid().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationList != null) { // Added scheme
                        // accumulation
                        // details
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

                    totalQty = totalQty + quantity;

                    if (quantity > 0) {
                        if (schemeProductBO.getUomID() != 0) {
                            if (schemeProductBO.getUomID() == productMasterBO
                                    .getCaseUomId()) {
                                if (productMasterBO.getCaseSize() > 0) {
                                    count = count + quantity
                                            / productMasterBO.getCaseSize();
                                }

                            } else if (schemeProductBO.getUomID() == productMasterBO
                                    .getOuUomid()) {
                                if (productMasterBO.getOutersize() > 0) {
                                    count = count + quantity
                                            / productMasterBO.getOutersize();
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
            if (selectedFromBuyQty != 0 && selectedtoBuyQty != 0) {
                int balanceCount = 0;

                if (count > selectedtoBuyQty) {
                    balanceCount = count % (int) selectedtoBuyQty;
                    count = count / (int) selectedtoBuyQty;
				/*
				 * Now we are apply range wise scheme.so maximum qty only using
				 * for computation to how many times scheme achived
				 */
                    if (balanceCount >= selectedFromBuyQty) {
                        balanceCount = 1;
                    } else {
                        if (selectedFromBuyQty > 0)
                            tempBalancePercent = ((balanceCount / selectedFromBuyQty) * 100);
                        balanceCount = 0;
                    }
                } else {
                    if (count >= selectedFromBuyQty)
                        count = 1;
                }

                //list to maintain used(if current slab applied) quantity,scheme wise..
                // updating hasmap, qty used to apply scheme
                // Any Logic- So considered all products
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {

                    int qty = 0;
                    double tempToQty = 0;
                    tempToQty = selectedtoBuyQty;
                    for (SchemeProductBO schemeProductBO : schemeProductList) {
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {

                            qty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

                            if (qty > 0) {

                                if (tempToQty >= qty) {
                                    tempToQty -= qty;
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), qty);
                                } else {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), (int) tempToQty);
                                    break;
                                }


                            }

                        }

                    }
                }


                //
                setBalancePercent(tempBalancePercent);


                // For ProRata balance percentage is not needed if Highest slap is not achieved.
                if (!isHighestSlab && processType.equals(PROCESS_TYPE_PRORATA))
                    setBalancePercent(0);

//				balanceCount = balanceCount / selectedFromBuyQty;
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
     * @param schemeProductList
     * @param groupName
     * @return
     * @author rajesh.k get count of Any logic combination scheme of salesvalue
     * type
     */
    private int getAnyLogicCountForSalesValue(
            List<SchemeProductBO> schemeProductList, String groupName,
            boolean isCombination, boolean isBatchWise, int parentID, String processType, boolean isHighestSlab) {

        double totalvalue = 0;
        // selectedFromBuyQty is used to store minimum buy qty
        double selectedFromBuyQty = 0;
        // selectedtoBuyQty is used to store maximum buy qty
        double selectedtoBuyQty = 0;

        double balancePercent = 0;

        for (SchemeProductBO schemeProductBO : schemeProductList) {
            if (schemeProductBO.getGroupName().equals(groupName)
                    || !isCombination) {
                selectedFromBuyQty = schemeProductBO.getBuyQty();
                selectedtoBuyQty = schemeProductBO.getTobuyQty();
                ProductMasterBO productMasterBO = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());
                if (productMasterBO != null) {
                    int quantity = (productMasterBO.getOrderedCaseQty() * productMasterBO
                            .getCaseSize())
                            + (productMasterBO.getOrderedOuterQty() * productMasterBO
                            .getOutersize())
                            + productMasterBO.getOrderedPcsQty();

                    double value = 0;
                    if (isBatchWise && productMasterBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        value = getbatchWiseTotalValue(productMasterBO, schemeProductBO.getBatchId());
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
                    if (bmodel.schemeDetailsMasterHelper
                            .getSchemeHistoryListBySchemeid() != null) {
                        SchemeBO schemeBO = mSchemeById.get(schemeProductBO.getSchemeId());
                        schemeAccumulationValueList = bmodel.schemeDetailsMasterHelper

                                .getSchemeHistoryListBySchemeid().get(
                                        schemeBO.getParentId() + "");
                    }
                    if (schemeAccumulationValueList != null) { // Added scheme
                        // accumulation
                        // details
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

                        totalvalue = totalvalue + value;

                    }
                }
            }

        }
        if (totalvalue != 0) {
            if (selectedFromBuyQty > 0 && selectedtoBuyQty > 0) {
                int balanceCount = (int) totalvalue % (int) selectedtoBuyQty;
                int count = (int) totalvalue / (int) selectedtoBuyQty;

                if (balanceCount > selectedFromBuyQty)
                    count += 1;
                    //  balanceCount = balanceCount / (int)selectedFromBuyQty;
                else {
                    //
                    //double tempBalanceValue=balanceCount%selectedFromBuyQty;
                    balancePercent = ((balanceCount / selectedFromBuyQty) * 100);
                }

                /////////////
                //list to maintain used(if current slab applied) quantity,scheme wise..
                if (processType.equals(PROCESS_TYPE_MTS) || processType.equals(PROCESS_TYPE_PRORATA)) {
                    double totVal = 0;
                    double tempToQty = 0;
                    tempToQty = selectedtoBuyQty;
                    for (SchemeProductBO schemeProductBO : schemeProductList) {
                        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO.getOrderedPcsQty() > 0 || productBO.getOrderedCaseQty() > 0 || productBO.getOrderedOuterQty() > 0) {

                            if (isBatchWise && productBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                                totVal = getbatchWiseTotalValue(productBO, schemeProductBO.getBatchId());
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
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), (int) totVal);
                                } else {
                                    mAchieved_qty_or_salesValue_by_schemeId_nd_productid.put((parentID + productBO.getProductID()), (int) tempToQty);
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

    public void updateFreeproductStocks() {

        if (mSchemeList != null) {
            for (SchemeBO schemeBO : mSchemeList) {
                List<SchemeProductBO> schemeFreeProductList = schemeBO
                        .getFreeProducts();


                if (schemeFreeProductList != null) {
                    for (SchemeProductBO schemeProductBO : schemeFreeProductList) {
                        ProductMasterBO productBO = bmodel.productHelper
                                .getProductMasterBOById(schemeProductBO.getProductId());
                        if (productBO != null) {
                            schemeProductBO.setStock(productBO.getSIH());
                        }
                    }
                }
            }
        }
    }

    /**
     * @author rajesh.k Method to use load scheme history list
     */
    public void loadSchemeHistoryDetails() {
        ProductMasterBO productBO = null;
        mSchemeHistoryListByschemeid = new HashMap<String, ArrayList<ProductMasterBO>>();

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "SELECT DISTINCT A.pid, A.batchid, A.schid,IFNULL(PieceUOM.Qty,0) AS PieceQty ,IFNULL(OuterUOM.Qty,0) as OouterQty,"
                    + " IFNULL(CaseUOM.Qty,0) as CaseQty" +
                    ",(IFNULL(PieceUOM.value,0)+IFNULL(OuterUOM.value,0)+IFNULL(CaseUOM.value,0)) " +
                    " FROM SchemeAchHistory A"
                    + " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='PIECE' and rid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ") as PieceUOM ON PieceUOM.Pid = A.pid" +
                    " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='MSQ' and rid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ") as OuterUOM ON OuterUOM.Pid = A.pid" +
                    " LEFT JOIN (SELECT pid, qty,value from SchemeAchHistory where  uom='CASE' and rid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ") as CaseUOM ON CaseUOM .Pid = A.pid"
                    + " LEFT JOIN OrderHeader OH on OH.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and invoicestatus=1"
                    + " LEFT JOIN SchemeDetail SD on SD.parentid=A.schid and OH.orderid=SD.orderid"
                    + " where rid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and A.schid!=IFNULL(SD.parentid,0) order by schid";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                String schemeID = "";
                ArrayList<ProductMasterBO> schemeList = new ArrayList<ProductMasterBO>();
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
                            mSchemeHistoryListByschemeid.put(schemeID,
                                    schemeList);
                            schemeList = new ArrayList<ProductMasterBO>();
                            schemeList.add(productBO);
                            schemeID = c.getString(2);
                        } else {
                            schemeList.add(productBO);
                            schemeID = c.getString(2);
                        }
                    } else {
                        schemeList.add(productBO);
                    }
                    //schemeList.add(productBO);

                }
                if (schemeList.size() > 0) {
                    mSchemeHistoryListByschemeid.put(schemeID, schemeList);
                }


                c.close();

            }
          /*  mSchemeHistoryValueListByschemeid=new HashMap<>();
			final String locationIds=getLocationIdsForScheme();
			final String channelIds=getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid());

			StringBuffer sb=new StringBuffer();
			sb.append("SELECT DISTINCT A.pid, A.batchid, A.schid,sum(A.value) FROM SchemeAchHistory A ");
			sb.append("inner join SchemeMaster SM on SM.parentid=A.schid and ");
			sb.append("  (SM.distributorid="+bmodel.getRetailerMasterBO().getDistributorId()+" OR SM.distributorid=0)");
			sb.append(" AND ((SM.RetailerId=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
			sb.append(" OR SM.Chid ="+bmodel.getRetailerMasterBO().getSubchannelid());
			sb.append(" OR SM.ChId in("+ channelIds+")");
			sb.append(" OR SM.LocId in("+locationIds+"))");
			sb.append(" OR (SM.RetailerId=0 AND SM.chid=0 AND SM.LocId=0 )) ");
			sb.append("Left Join OrderHeader OH on OH.retailerid="+bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +" and invoicestatus=1 ");
			sb.append("Left Join SchemeDetail SD on A.schid =SD.parentid and OH.orderid=SD.orderid ");
			sb.append("where A.rid="+bmodel.getRetailerMasterBO().getRetailerID());
			sb.append(" and A.schid!=IFNULL(SD.parentid,0) and SM.buytype='SV' group by A.pid order by schid ");

			c = db.selectSQL(sb.toString());
			if (c.getCount() > 0) {
				String schemeID = "";
				ArrayList<ProductMasterBO> schemeList = new ArrayList<ProductMasterBO>();
				while (c.moveToNext()) {

					productBO = new ProductMasterBO();
					productBO.setProductID(c.getString(0));
					productBO.setBatchid(c.getString(1));

					productBO.setTotalamount(c.getDouble(3));

					if (!schemeID.equals(c.getString(2))) {
						if (!schemeID.equals("")) {
							mSchemeHistoryValueListByschemeid.put(schemeID,
									schemeList);
							schemeList = new ArrayList<ProductMasterBO>();
							schemeList.add(productBO);
							schemeID = c.getString(2);
						} else {
							schemeList.add(productBO);
							schemeID = c.getString(2);
						}
					} else {
						schemeList.add(productBO);
					}
					//schemeList.add(productBO);

				}
				if (schemeList.size() > 0) {
					mSchemeHistoryValueListByschemeid.put(schemeID, schemeList);
				}
				c.close();*/
            //}
            db.closeDB();


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @return
     * @author rajesh.k method to return hashmap of Schemeachievement history
     * details
     */
    public HashMap<String, ArrayList<ProductMasterBO>> getSchemeHistoryListBySchemeid() {
        return mSchemeHistoryListByschemeid;
    }

    public HashMap<String, ArrayList<ProductMasterBO>> getSchemeHistoryValueListByschemeid() {
        return mSchemeHistoryValueListByschemeid;
    }

    /**
     * @author rajesh.k method to use scheme Apply process after take order.Only
     * one scheme apply in a same parent id. either seller type dialog
     * configuration disable or seller type selected vansales
     */
    public void schemeApply() {

        mAchieved_qty_or_salesValue_by_schemeId_nd_productid = new HashMap<>();
        // save applied scheme
        mApplySchemeList = new ArrayList<SchemeBO>();
        if (mParentIDList != null) {
            for (Integer parentID : mParentIDList) {
                int slabPosition = 0;
                ArrayList<String> schemeIDList = mSchemeIDListByParentID
                        .get(parentID);
                if (schemeIDList != null) {
                    for (String schemeID : schemeIDList) {

                        slabPosition += 1;
                        SchemeBO schemeBO = mSchemeById.get(schemeID);

                        if (schemeBO != null) {

                            if (!schemeBO.isOffScheme()) {// only ON scheme will be allowed to apply

                                schemeBO.setOrderedProductCount(0);

                                boolean flag = isSchemeDone(schemeBO, parentID, (slabPosition == 1 ? true : false));
                                // if flag is true ,scheme achieved successfully
                                if (flag) {
                                    mApplySchemeList.add(schemeBO);
                                    if (schemeBO.isQuantityTypeSelected()) {
                                        List<SchemeProductBO> freeProductList = schemeBO
                                                .getFreeProducts();
                                        for (SchemeProductBO freeProductBo : freeProductList) {
                                            freeProductBo.setQuantitySelected(0);
                                        }

                                    }

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
        /// add off invoice scheme
        if (mOffInvoiceSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceSchemeList) {

                if (schemeBO != null) {
                    schemeBO.setIsOnInvoice(0);
                    schemeBO.setQuantityTypeSelected(true);
                    schemeBO.setApplyCount(1);
                    schemeBO.setIsFreeCombination(1);
                    mApplySchemeList.add(schemeBO);
                }

            }
        }

    }

    /**
     * @param schemeBO - check for this scheme objects apply or not
     * @return true - scheme achieved , false - not done
     * @author rajesh.k Method to check scheme done or not.if it return
     * true,scheme achieved successfully else not done
     */
    private boolean isSchemeDone(SchemeBO schemeBO, Integer parentID, boolean iSHighestSlab) {

        boolean isCombinationSchemeDone = false;
        int buyQty = 0;

        ArrayList<String> groupSchemeName = new ArrayList<String>();
        HashMap<String, String> groupBuyTypeByGroupName = new HashMap<String, String>();
        for (SchemeProductBO schemeProductBo : schemeBO.getBuyingProducts()) {
            if (!groupSchemeName.contains(schemeProductBo.getGroupName())) {
                groupSchemeName.add(schemeProductBo.getGroupName());
                groupBuyTypeByGroupName.put(schemeProductBo.getGroupName(),
                        schemeProductBo.getGroupBuyType());
            }

        }
        // set combination scheme
        // of buyqty

        if (schemeBO.getType().equals(ANY_LOGIC)) {
            // reset already entered free qty
            isCombinationSchemeDone = isCombinationAnyLogicSchemeDone(schemeBO,
                    groupSchemeName, groupBuyTypeByGroupName, parentID);
            if (isCombinationSchemeDone) {

                if (schemeBO.getBuyType() != null) {

                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        buyQty = getCombinationAnyLogicBuyQty(
                                schemeBO.getBuyingProducts(), groupSchemeName,
                                groupBuyTypeByGroupName, true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);
                        schemeBO.setApplyCount(buyQty);

                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());

                        calculateSchemePerPieceNew(schemeBO, buyQty);
                        Log.d("bal ANY_LOGIC", getBalancePercent() + "");

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {

                        buyQty = getCombinationAnyLogicBuyQty(
                                schemeBO.getBuyingProducts(), groupSchemeName,
                                groupBuyTypeByGroupName, false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);
                        schemeBO.setApplyCount(buyQty);

                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());

                        calculateSchemePerPieceNew(schemeBO, buyQty);

                    }

                    return true;

                }
            }
        } else if (schemeBO.getType().equals(AND_LOGIC)
                || schemeBO.getType().equals(ONLY_LOGIC)) {
            isCombinationSchemeDone = isCombinationAndLogicSchemeDone(schemeBO,
                    groupSchemeName, groupBuyTypeByGroupName, parentID);
            if (isCombinationSchemeDone) {

                if (schemeBO.getBuyType() != null) {
                    if (schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        buyQty = getCombinationAndLogicBuyQty(
                                schemeBO.getBuyingProducts(), groupSchemeName,
                                groupBuyTypeByGroupName, true, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);
                        schemeBO.setApplyCount(buyQty);

                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());

                        calculateSchemePerPieceNew(schemeBO, buyQty);

                        Log.d("bal AND_LOGIC", getBalancePercent() + "");

                    } else if (schemeBO.getBuyType().equals(SALES_VALUE)) {
                        buyQty = getCombinationAndLogicBuyQty(
                                schemeBO.getBuyingProducts(), groupSchemeName,
                                groupBuyTypeByGroupName, false, schemeBO.isBatchWise(), parentID, schemeBO.getProcessType(), iSHighestSlab);
                        schemeBO.setApplyCount(buyQty);

                        //over all scheme balance percent value
                        schemeBO.setBalancePercent(getBalancePercent());

                        calculateSchemePerPieceNew(schemeBO, buyQty);

                    }
                    return true;

                }
            }
        }

        return false;
    }

    /**
     * @return
     * @author rajesh.k
     * <p>
     * method to retrieve applied scheme list.
     */

    public ArrayList<SchemeBO> getAppliedSchemeList() {
        if (mApplySchemeList != null) {
            return mApplySchemeList;
        }
        return new ArrayList<SchemeBO>();
    }

    public boolean isFromCounterSale;

    /**
     * @param orderID - mapping to orderID
     * @author rajesh.k
     * <p>
     * Method to save all applied scheme details in SQLite
     */
    public void insertScemeDetails(String orderID, DBUtil db) {
        if (mApplySchemeList != null) {

            for (SchemeBO schemeBO : mApplySchemeList) {

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
     * @param schemeBO
     * @param db
     * @param orderID  - mapping for this orderID
     * @author rajesh.k Method to insert scheme buy product in scheme detail
     * table
     */
    private void insertSchemeBuyProductDetails(SchemeBO schemeBO, DBUtil db,
                                               String orderID) {
        String schemeDetailColumn = "OrderID,SchemeID,ProductID,SchemeType,Value,parentid,Retailerid,distributorid,upload,Amount";

        if (isFromCounterSale) {
            schemeDetailColumn = "Uid,SlabId,ProductId,SchemeType,Value,SchemeId,Retailerid,distributorid,upload,Amount";
        }

        List<SchemeProductBO> buyProductList = schemeBO.getBuyingProducts();

        if (schemeBO.isAmountTypeSelected()) {
            if (buyProductList != null) {
                for (SchemeProductBO schemeProductBO : buyProductList) {
                    ProductMasterBO productBO;
                    productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());

                    if (productBO != null) {
                        if (productBO.getOrderedPcsQty() > 0
                                || productBO.getOrderedCaseQty() > 0
                                || productBO.getOrderedOuterQty() > 0) {
                            if (schemeIdCount.containsKey(schemeProductBO.getSchemeId())) {
                                schemeIdCount.put(schemeProductBO.getSchemeId(), schemeIdCount.get(schemeProductBO.getSchemeId()) + 1);
                            } else {
                                count = 1;
                                schemeIdCount.put(schemeProductBO.getSchemeId(), count);
                            }
                        }
                    }
                }
            }
        }

        if (buyProductList != null) {
            for (SchemeProductBO schemeProductBO : buyProductList) {
                ProductMasterBO productBO;
                productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());

                if (productBO != null) {
                    if (productBO.getOrderedPcsQty() > 0
                            || productBO.getOrderedCaseQty() > 0
                            || productBO.getOrderedOuterQty() > 0) {
                        int value = 1;
                        StringBuffer sb = new StringBuffer();
                        sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                                + schemeProductBO.getProductId() + ",");
                        if (schemeBO.isQuantityTypeSelected()) {
                            sb.append(bmodel.QT(SCHEME_FREE_PRODUCT) + ",");
                            sb.append(+0);
                        } else if (schemeBO.isAmountTypeSelected()) {
                            sb.append(bmodel.QT(SCHEME_AMOUNT));


                            for (Map.Entry<String, Integer> entry : schemeIdCount.entrySet()) {
                                if (schemeBO.getSchemeId().equalsIgnoreCase(entry.getKey())) {
                                    value = entry.getValue();
                                }
                            }
                            sb.append("," + (schemeBO.getSelectedAmount() / value));

                        } else if (schemeBO.isPriceTypeSeleted()) {
                            sb.append(bmodel.QT(SCHEME_PRICE));
                            sb.append("," + schemeBO.getSelectedPrice());

                        } else if (schemeBO.isDiscountPrecentSelected()) {
                            if (schemeBO.getGetType().equalsIgnoreCase(SCHEME_PERCENTAGE_BILL)) {
                                sb = new StringBuffer();
                                sb.append(orderID + "," + schemeBO.getSchemeId() + ","
                                        + 0 + ",");
                            }

                            sb.append(bmodel.QT(SCHEME_PERCENTAGE));
                            sb.append("," + schemeBO.getSelectedPrecent());
                        }
                        sb.append("," + schemeBO.getParentId());
                        sb.append("," + bmodel.getRetailerMasterBO().getRetailerID());
                        sb.append("," + bmodel.getRetailerMasterBO().getDistributorId());
                        if (schemeBO.isQuantityTypeSelected()) {
                            sb.append(",'Y'");
                        } else {
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


        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId";
        if (isFromCounterSale) {
            freeDetailColumn = "uid,SlabId,ProductId,Qty,UomID,UomCount,BatchId,SchemeId,RetailerId,price,taxAmount";
        }
        if (mOffInvoiceAppliedSchemeList != null) {
            for (SchemeBO schemeBO : mOffInvoiceAppliedSchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    final List<SchemeProductBO> freeProductsList = schemeBO.getFreeProducts();
                    for (SchemeProductBO freeProductBO : freeProductsList) {
                        if (freeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(freeProductBO.getProductId());

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
                                sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));

                                db.insertSQL(DataMembers.tbl_scheme_free_detail, freeDetailColumn,
                                        sb.toString());
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
                                          String orderID) {
        String freeDetailColumn = "OrderID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,RetailerId,price,taxAmount";
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO freeProductBO : freeProductList) {
                if (freeProductBO.getQuantitySelected() > 0) {

                    ProductMasterBO productBO = bmodel
                            .getProductbyId(freeProductBO.getProductId());
                    if (productBO != null) {
                        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                && bmodel.configurationMasterHelper.IS_INVOICE) {
                            if (productBO.getBatchwiseProductCount() > 0) {
                                insertFreeproductWithbatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn);
                            } else {
                                insertFreeproductWithoutbatch(schemeBO, db,
                                        orderID, freeProductBO,
                                        freeDetailColumn);
                            }
                        } else {
                            insertFreeproductWithoutbatch(schemeBO, db,
                                    orderID, freeProductBO, freeDetailColumn);

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
    private void insertFreeproductWithoutbatch(SchemeBO schemeBO, DBUtil db,
                                               String orderID, SchemeProductBO freeProductBO,
                                               String freeDetailColumn) {

        ProductMasterBO productBO = bmodel.getProductbyId(freeProductBO
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
            sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ",");

            if (isFromCounterSale) {
                sb.append(0 + "," + 0);
            } else if (bmodel.configurationMasterHelper.IS_GST) {

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
                sb.append("," + bmodel.formatValue(freeProductBO.getTaxAmount()));
            } else {
                sb.append(0 + "," + 0);
            }

            if (isFromCounterSale) {
                db.insertSQL(DataMembers.tbl_CS_SchemeFreeProductDetail, freeDetailColumn,
                        sb.toString());
            } else {
                db.insertSQL(DataMembers.tbl_scheme_free_detail, freeDetailColumn,
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
    private void insertFreeproductWithbatch(SchemeBO schemeBO, DBUtil db,
                                            String orderID, SchemeProductBO schemeProductBo,
                                            String freeDetailColumn) {

        ArrayList<SchemeProductBatchQty> freeProductbatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bmodel.productHelper
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
                    sb.append("," + bmodel.getRetailerMasterBO().getRetailerID());

                    if (bmodel.configurationMasterHelper.IS_GST) {

                        sb.append(productBo.getSrp());
                        sb.append("," + bmodel.formatValue(schemeProductBo.getTaxAmount()));
                    } else {
                        sb.append(0 + "," + 0);
                    }

                    if (isFromCounterSale) {
                        db.insertSQL(DataMembers.tbl_CS_SchemeFreeProductDetail, freeDetailColumn,
                                sb.toString());
                    } else {
                        db.insertSQL(DataMembers.tbl_scheme_free_detail, freeDetailColumn,
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
                    + bmodel.QT(retailerID));
            sb.append(" and invoicestatus=0 and upload='N'");
            // if seller type selection dialog enable
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sb.append(" and is_vansales="
                        + bmodel.getRetailerMasterBO().getIsVansales());
            }


            sb.append(" and sid=" + bmodel.getRetailerMasterBO().getDistributorId());

            sb.append(" and orderid not in(select orderid from OrderDeliveryDetails)");// to prevent delivered orders

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
    public void loadOrderedBuyProducts(String id, DBUtil db) {
        mApplySchemeList = new ArrayList<SchemeBO>();
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct schemeid,SchemeType,value,count(productid) from SchemeDetail where ");

        sb.append("orderid=" + bmodel.QT(id));

        sb.append("  group by schemeid");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String schemeID = c.getString(0);
                String schemeType = c.getString(1);
                double value = c.getDouble(2);
                SchemeBO schemeBO = mSchemeById.get(schemeID);
                if (schemeBO != null) {
                    schemeBO.setOrderedProductCount(c.getInt(3));
                    if (schemeType.equals(SCHEME_AMOUNT)) {
                        schemeBO.setAmountTypeSelected(true);
                        schemeBO.setSelectedAmount(value);
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
                mApplySchemeList.add(schemeBO);

            }
        }

    }

    /**
     * @param id
     * @param db - if true, id is OrderID,false Invoiceid
     * @author rajesh.k method to preload free product object from sqlite
     */
    public void loadOrderedFreeProducts(String id, DBUtil db) {
        // clear free product details
        Cursor c1 = db
                .selectSQL("select distinct schemeid from schemeFreeProductDetail where orderid ="
                        + bmodel.QT(id) + " and upload='N'");
        if (c1.getCount() > 0) {
            while (c1.moveToNext()) {
                String schemeId = c1.getString(0);
                SchemeBO schemeBo = getmSchemeById().get(schemeId);
                if (schemeBo != null) {
                    clearSchemeFreeProduct(schemeBo);
                }
            }
        }

        Cursor c = db
                .selectSQL("select schemeid,FreeProductID,FreeQty,UomID,batchid from schemeFreeProductDetail "
                        + "where orderID="
                        + bmodel.QT(id)
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
     * @param schemeid
     * @param freeProductID
     * @param qty
     * @author rajesh.k free product value downloaded from sqlite and set in
     * object
     */
    private void setSchemeFreeProductDetails(String schemeid,
                                             String freeProductID, int qty) {
        SchemeBO schemeBO = mSchemeById.get(schemeid);
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
                + bmodel.QT(invoiceID) + " where orderID=" + orderID);
        db.updateSQL("update SchemeFreeProductDetail set Invoiceid="
                + bmodel.QT(invoiceID) + " where orderID=" + orderID);
        if (mApplySchemeList != null) {
            for (SchemeBO schemeBO : mApplySchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    List<SchemeProductBO> freeProductList = schemeBO
                            .getFreeProducts();
                    for (SchemeProductBO schemeProductBO : freeProductList) {
                        if (schemeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bmodel.productHelper
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

                                if (productBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
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
                            ProductMasterBO productBO = bmodel.productHelper
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

                                if (productBO.getBatchwiseProductCount() > 0 && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
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
        ProductMasterBO productBo = bmodel.productHelper
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
                    bmodel.batchAllocationHelper.setBatchwiseSIH(productBo,
                            schemeProductBatchQty.getBatchid() + "",
                            schemeProductBatchQty.getQty(), false);
                }

            }
        }

    }

    /**
     * @param orderID
     * @param invoiceID
     * @param schemeProductBo
     * @param parentid
     * @author rajesh.k Method to insert scheme Free product as batch wise in
     * scheme Free Product Details and at same time delete oldest scheme
     * free product in scheme Free product Details as same orderID and
     * update sih batch wise
     */
    private void insertFreeProductBatchWise(String orderID, String invoiceID,
                                            SchemeProductBO schemeProductBo, DBUtil db, int parentid) {
        db.updateSQL("delete from SchemeFreeProductDetail where orderID="
                + orderID + " and schemeID=" + schemeProductBo.getSchemeId()
                + " and freeproductID=" + schemeProductBo.getProductId());
        String freeDetailColumn = "OrderID,invoiceid,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid";
        ArrayList<SchemeProductBatchQty> freeProductbatchList = schemeProductBo
                .getBatchWiseQty();
        ProductMasterBO productBo = bmodel.productHelper
                .getProductMasterBOById(schemeProductBo.getProductId());
        if (freeProductbatchList != null) {
            StringBuffer sb = null;
            for (SchemeProductBatchQty schemeProductBatchQty : freeProductbatchList) {
                if (schemeProductBatchQty.getQty() > 0) {
                    sb = new StringBuffer();
                    sb.append(orderID + "," + invoiceID + ","
                            + schemeProductBo.getSchemeId() + ",");
                    sb.append(schemeProductBo.getProductId() + ","
                            + schemeProductBatchQty.getQty() + ",");
                    sb.append(productBo.getPcUomid() + ",1,"
                            + schemeProductBatchQty.getBatchid());
                    sb.append("," + parentid);
                    db.insertSQL(DataMembers.tbl_scheme_free_detail,
                            freeDetailColumn, sb.toString());
                    db.executeQ("update StockInHandMaster set upload='N',qty=(case when  ifnull(qty,0)>"
                            + schemeProductBatchQty.getQty()
                            + " then ifnull(qty,0)-"
                            + schemeProductBatchQty.getQty()
                            + " else 0 end) where pid="
                            + schemeProductBo.getProductId()
                            + " and batchid="
                            + schemeProductBatchQty.getBatchid());
                    bmodel.batchAllocationHelper.setBatchwiseSIH(productBo,
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
	/*	sb.append("update schememaster set count=count-1 where parentid="+parentID+" and count!=-1 and (");
		sb.append("schememaster.RetailerId="+bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
		sb.append(" OR schememaster.Chid="+bmodel.getRetailerMasterBO().getSubchannelid());
		sb.append(" OR schememaster.ChId in("+ getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid())+")");
		sb.append("OR schememaster.LocId in("+getLocationIdsForScheme()+"))");*/


        // update scheme apply count retailer wise
        String query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bmodel.QT(schemeid) + " and schemeApplyCount!=-1 and retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
        db.executeQ(query1);

        // update scheme apply count seller wise

        query1 = "update schemeApplyCountmaster set schemeApplyCount=schemeApplyCount-1 where Schemeid="
                + bmodel.QT(schemeid) + " and schemeApplyCount!=-1 and userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid();
        db.executeQ(query1);

    }

    /**
     * @param id   - it acts either invoiceid or orderid
     * @param flag - true - invoice,false - order
     * @author rajesh.k method to use show invoice report and order report
     */
    public void loadSchemeReportDetails(String id, boolean flag) {
        mApplySchemeList = new ArrayList<>();
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
                sb.append("SFD.invoiceid=" + bmodel.QT(id));
            } else {// order report
                sb.append("SFD.orderid=" + bmodel.QT(id));
            }
            sb.append(" AND SD.SCHEMETYPE = " + bmodel.QT(SCHEME_FREE_PRODUCT));
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
                    ProductMasterBO produBo = bmodel.productHelper
                            .getProductMasterBOById(productid);
                    if (produBo != null) {
                        if (produBo.getSchemeProducts() == null) {
                            produBo.setSchemeProducts(new ArrayList<SchemeProductBO>());
                        }
                        // scheme product is frree product object
                        ProductMasterBO schemeProduct = bmodel.productHelper
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
                sb.append(" invoiceid=").append(bmodel.QT(id));
            } else {// order report
                sb.append(" orderid=" + bmodel.QT(id));
            }
            ArrayList<String> schemeIdList = new ArrayList<>();

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String schemeid = c.getString(0);
                    SchemeBO schemeBO = getmSchemeById().get(schemeid);
                    if (schemeBO != null) {
                        List<SchemeProductBO> buyList = schemeBO.getBuyingProducts();
                        String productid = c.getString(1);
                        String schemeType = c.getString(2);
                        double percentage = c.getDouble(3);
                        double discountValue = c.getDouble(4);

                        if (buyList != null) {
                            if (schemeType.equals(SCHEME_AMOUNT)) {
                                schemeBO.setAmountTypeSelected(true);
                                schemeBO.setSelectedAmount(percentage);
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
                            mApplySchemeList.add(schemeBO);
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
                                       String type, boolean isBatchwise) {
        double total = 0.0;
        if (isBatchwise) {
            total = updateBatchwiseSchemeProducts(productBo, value, type);

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
    private double updateBatchwiseSchemeProducts(ProductMasterBO productBO,
                                                 double value, String type) {
        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper
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
    public void updataFreeproductBottleReturn() {
        if (mApplySchemeList != null) {
            for (SchemeBO schemeBO : mApplySchemeList) {
                if (schemeBO.isQuantityTypeSelected()) {
                    List<SchemeProductBO> freeProductList = schemeBO
                            .getFreeProducts();
                    for (SchemeProductBO schemeProductBO : freeProductList) {
                        if (schemeProductBO.getQuantitySelected() > 0) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(schemeProductBO
                                            .getProductId());
                            if (productBO != null && bmodel.productHelper.getBomMaster() != null && bmodel.productHelper.getBomMaster().size() > 0) {
                                for (BomMasterBO bomMasterBo : bmodel.productHelper
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

                                            for (BomRetunBo returnBo : bmodel.productHelper
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

    public String getChannelidForScheme(int channelid) {
        String sql, sql1 = "", str = "";
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelid + ") as contentlevel  from ChannelLevel");
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".ChId";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select " + sql1 + "  from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + channelid;
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    int count = c.getCount();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                    if (str.endsWith(","))
                        str = str.substring(0, str.length() - 1);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return str;
    }

    public String getLocationIdsForScheme() {
        String sql, sql1 = "", str = "" + bmodel.getRetailerMasterBO().getLocationId();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append("select min(Sequence) as childlevel,(select Sequence from LocationLevel l1 ");
            sb.append("inner join locationmaster lm on l1.id=LM.loclevelid where lm.locid=");
            sb.append(bmodel.getRetailerMasterBO().getLocationId());
            sb.append(") as contentlevel  from LocationLevel");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
            }
            c.close();

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".Locid";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select " + sql1 + "  from LocationMaster LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN LocationMaster LM" + i + " ON LM" + (i - 1)
                        + ".LocParentId = LM" + i + ".LocId";
            sql = sql + " where LM1.LocId=" + bmodel.getRetailerMasterBO().getLocationId();
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                    if (str.endsWith(","))
                        str = str.substring(0, str.length() - 1);
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return str;
    }

    private void downloadParentIdListByProduct(DBUtil db) {

        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SBM.productid,SM.parentid,SCM.groupId,Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag from SchemeBuyMaster SBM ");
        sb.append(" inner join SchemeMaster SM on SM.Schemeid=SBM.Schemeid ");
        sb.append("inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid ");
        sb.append("left join schemeApplyCountMaster SAC on SBM.schemeid=SAC.schemeID ");
        sb.append("and (SAC.retailerid=0 OR SAC.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" OR SAC.userid=0 OR SAC.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ")");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");
        sb.append(" where SCM.distributorid in(0," + bmodel.getRetailerMasterBO().getDistributorId() + ")");
        sb.append(" and SCM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
        sb.append(" and SCM.channelid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")");
        sb.append(" and SCM.locationid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
        sb.append(" and SCM.accountid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")");

        sb.append(" AND SAC.schemeApplyCOunt!=0  AND SM.IsOnInvoice=1 order by SBM.Productid");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            mParentIdListByProductId = new HashMap<>();
            String productid = "";
            ArrayList<Integer> parentIdList = new ArrayList<>();
            while (c.moveToNext()) {
                int parentid = c.getInt(1);
                if (c.getInt(3) == 0 || (c.getInt(3) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(2) + c.getString(1)))) {
                    if (!productid.equals(c.getString(0))) {
                        if (!productid.equals("")) {

                            mParentIdListByProductId.put(productid, parentIdList);
                            parentIdList = new ArrayList<Integer>();
                            parentIdList.add(parentid);
                            productid = c.getString(0);

                        } else {
                            parentIdList.add(parentid);
                            productid = c.getString(0);

                        }
                    } else {
                        parentIdList.add(parentid);

                    }
                }
            }
            if (parentIdList.size() > 0) {
                mParentIdListByProductId.put(productid, parentIdList);
            }

        }
        c.close();
    }

    public HashMap<String, ArrayList<Integer>> getParentIdListByProductId() {
        return mParentIdListByProductId;
    }

    private void downloadProductIdListByParentId(DBUtil db) {
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SBM.productid,SM.parentid,SCM.groupId,Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag from SchemeBuyMaster SBM ");
        sb.append(" inner join SchemeMaster SM on SM.Schemeid=SBM.Schemeid ");
        sb.append("inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid ");
        sb.append("left join schemeApplyCountMaster SAC on SM.schemeid=SAC.schemeID ");
        sb.append("and (SAC.retailerid=0 OR SAC.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" OR SAC.userid=0 OR SAC.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ")");
        sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");
        sb.append(" where SCM.distributorid in(0," + bmodel.getRetailerMasterBO().getDistributorId() + ")");
        sb.append(" and SCM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
        sb.append(" and SCM.channelid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")");
        sb.append(" and SCM.locationid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
        sb.append(" and SCM.accountid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")");


        sb.append(" AND SAC.schemeApplyCOunt!=0  AND SM.IsOnInvoice=1 order by SM.parentid");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            mProductIdListByParentId = new SparseArray<>();
            int parentId = 0;
            ArrayList<String> productIdList = new ArrayList<>();
            while (c.moveToNext()) {
                String productid = c.getString(0);
                if (c.getInt(3) == 0 || (c.getInt(3) == 1 && mGroupIDList != null && mGroupIDList.contains(c.getString(2) + c.getString(1)))) {
                    if (parentId != c.getInt(1)) {
                        if (parentId != 0) {

                            mProductIdListByParentId.put(parentId, productIdList);
                            productIdList = new ArrayList<String>();
                            productIdList.add(productid);
                            parentId = c.getInt(1);

                        } else {
                            productIdList.add(productid);
                            parentId = c.getInt(1);

                        }
                    } else {
                        productIdList.add(productid);

                    }
                }
            }
            if (productIdList.size() > 0) {
                mProductIdListByParentId.put(parentId, productIdList);
            }

        }
        c.close();
    }

    public SparseArray<ArrayList<String>> getProductIdListByParentId() {
        return mProductIdListByParentId;
    }

    public int getAmountBasedSchemeCount(SchemeBO schemeBO) {
        int count = 0;
        List<SchemeProductBO> schemeProductList = schemeBO.getBuyingProducts();
        for (SchemeProductBO schemeProductBO : schemeProductList) {
            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
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
     * Method to get details of already applied scheme in previous order
     *
     * @param db
     */
    private void applyPeriodWiseScheme(DBUtil db) {
        final String currentDate = SDUtil.now(SDUtil.DATE_GLOBAL_EIPHEN);
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct SM.schemeid,SB.productid,");
        sb.append("(julianday(" + bmodel.QT(currentDate) + ")-julianday(replace(date,'/','-') )) as daycount from Schememaster SM ");
        sb.append("inner join SchemePurchaseHistory SH on SM.parentid=SH.schemeid ");
        sb.append("inner join SchemeBuyMaster SB on SM.schemeid=SB.Schemeid ");
        sb.append("where (isapplied=1 AND SM.Days>=daycount)");
        sb.append("and SH.retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
        sb.append(" order by SM.schemeid ");
        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            int schemeid = 0;
            mProductidListByAlreadyApplySchemeId = new SparseArray<>();
            ArrayList<String> productIdList = new ArrayList<>();
            while (c.moveToNext()) {
                String productid = c.getString(1);

                if (schemeid != c.getInt(0)) {
                    if (schemeid != 0) {

                        mProductidListByAlreadyApplySchemeId.put(schemeid, productIdList);
                        productIdList = new ArrayList<String>();
                        productIdList.add(productid);
                        schemeid = c.getInt(0);

                    } else {
                        productIdList.add(productid);
                        schemeid = c.getInt(0);

                    }
                } else {
                    productIdList.add(productid);

                }
            }
            if (productIdList.size() > 0) {
                mProductidListByAlreadyApplySchemeId.put(schemeid, productIdList);
            }

        }
    }


    private SparseArray<ArrayList<String>> getProdcutIdListByAlreadyAppliedSchemeId() {
        return mProductidListByAlreadyApplySchemeId;
    }

    /**
     * From server
     */
    public void insertSchemeApplyCount() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            String columns = "Schemeid,SchemeApplyCount";


            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct SCH.schemeid from Schememaster SCH Left join SchemeApplyCountMaster SAM ");
            sb.append("on SCH.schemeid=SAM.schemeid where SCH.schemeid!=ifnull(SAM.schemeid,0) ");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    String schemeid = c.getString(0);
                    insertSchemeApplyCountDetails(db, schemeid, columns);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
    }

    private void insertSchemeApplyCountDetails(DBUtil db, String schemeid, String columns) {
        String values = schemeid + ",-1";
        db.insertSQL("SchemeApplyCountMaster", columns, values);
    }

	/*public void downloadRetailerAttributes(){
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);

			db.openDataBase();
			String sql="select A.attributeId,A.attributename from EntityAttributeMaster A INNER JOIN RetailerAttribute B ON A.attributeid=B.attributeid"
					+" where B.retailerid="+bmodel.getRetailerMasterBO().getRetailerID();
			Cursor c=db.selectSQL(sql);
			if(c!=null){
				while (c.moveToNext()){
					bmodel.getRetailerMasterBO().setAttributeId(c.getInt(0));
					bmodel.getRetailerMasterBO().setAttributeName(c.getString(1));
				}
				c.close();
			}

			db.closeDB();
		}catch (Exception e){
			Commons.print(e.getMessage());
		}
	}
*/


    /**
     * Method to use set product whether scheme available or not if scheme
     * available for a product,setIsscheme==1,else 0
     * assigned to parent sku if child sku having any scheme based on child sku ParentId
     */

    public void setIsChildScheme() {
        try {
            List<SchemeBO> schemeList = bmodel.schemeDetailsMasterHelper.getmSchemeList();

            for (SchemeBO schBo : schemeList) {
                for (SchemeProductBO objBo : schBo.getBuyingProducts()) {

                    for (ProductMasterBO childBo : bmodel.productHelper.getProductMaster()) {

                        if (childBo.isChildProduct() && objBo.getProductId().equals(childBo.getProductID())) {

                            ProductMasterBO schemeProductBo = bmodel.productHelper.getProductMasterBOById(String.valueOf(childBo.getParentid()));
                            if (schemeProductBo != null) {
                                schemeProductBo.setIsscheme(1);
                                schemeProductBo.setIsPromo(true);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }


    }


    /**
     * Method to use set product whether scheme available or not if scheme
     * available for a product,setIsscheme==1,else 0
     */
    public void setIsScheme() {
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

                    ProductMasterBO prdBO = bmodel.productHelper.getProductMasterBOById(c
                            .getString(0));
                    if (prdBO != null) {
                        prdBO.setIsscheme(0);
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
            sb.append("select  distinct(PID),SM.parentid,SCM.groupId,Case  IFNULL(OP.groupid,-1) when -1  then '0' else '1' END as flag from productMaster inner join SchemeBuyMaster SB on ProductMaster.pID=ProductID ");
            sb.append("inner join schememaster SM  on SM. schemeID=SB.schemeID  inner join schemeApplyCountMaster SAC on SAC.schemeid=SB.schemeid ");
            sb.append("inner join SchemeCriteriaMapping SCM ON SCM.schemeid=SM.parentid");
            sb.append(" LEFT JOIN SchemeAttributeMapping  OP on OP.GroupId= SCM.GroupID and OP.SchemeID=SCM.schemeid");
            sb.append("  where SM.count!=0  and SAC.schemeApplyCount!=0 and SAC.retailerid in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
            sb.append(" and SCM.distributorid in(0," + bmodel.getRetailerMasterBO().getDistributorId() + ")");
            sb.append(" and SCM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")");
            sb.append(" and SCM.channelid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")");
            sb.append(" and SCM.locationid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
            sb.append(" and SCM.accountid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")");

            Cursor schemeCursor = db.selectSQL(sb.toString());
            if (schemeCursor != null) {
                if (schemeCursor.getCount() > 0) {
                    while (schemeCursor.moveToNext()) {
                        ProductMasterBO schemeProductBo = bmodel.productHelper.getProductMasterBOById(schemeCursor.getString(0));
                        if (schemeCursor.getInt(3) == 0 || (schemeCursor.getInt(3) == 1 && mGroupIDList != null && mGroupIDList.contains(schemeCursor.getString(2) + schemeCursor.getString(1)))) {
                            if (schemeProductBo != null) {
                                schemeProductBo.setIsscheme(1);
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

    public boolean isSihAvailableForSchemeGroupFreeProducts(SchemeBO schemeBO, String groupName) {
        boolean flag = true;
        final List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO schemeProductBO : freeProductList) {
                if (groupName.equals(schemeProductBO.getGroupName())) {

                    int stock = 0;
                    ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                    if (productBO != null) {
                        int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                        if (isFromCounterSale)
                            stock = productBO.getCsFreeSIH() - totalQty;
                        else
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
                        }
                        if (schemeProductBO.getGroupBuyType().equals(ANY_LOGIC) || schemeProductBO.getGroupBuyType().equals(ONLY_LOGIC)) {
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


    public void downloadOffInvoiceSchemeDetails() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            mOffInvoiceSchemeList = new ArrayList<>();
            mOffInvoiceSchemeFreeProductList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT distinct productid,Pname,uomid,Qty,ASF.Slabid,ASF.schemeid,ASF.SchemeDesc,ASF.groupName, ASF.groupType,ASF.schemeLogic  from AccumulationSchemeFreeIssues ASF ");
            sb.append("inner join Productmaster PM on PM.pid=ASF.productid ");
            sb.append("where ASF.retailerid=");
            sb.append(bmodel.getRetailerMasterBO().getRetailerID());
            sb.append(" and ASF.slabid not in(select schemeid from SchemeFreeProductDetail where retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + ") order by ASF.schemeid");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {

                    ArrayList<SchemeProductBO> freeProductList = new ArrayList<>();
                    SchemeProductBO schemeProductBO;
                    int schemeid = 0;
                    String schemeDesc = "";
                    String freeType = "";
                    SchemeBO schemeBO = null;

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
                        //schemeProductBO.setQuantitySelected(freeQty);
                        schemeProductBO.setSchemeId(c.getInt(4) + "");
                        schemeProductBO.setAccProductParentId(c.getInt(5) + "");
                        schemeProductBO.setGroupName(c.getString(7));

                        schemeProductBO.setGroupBuyType(c.getString(8));
                        mOffInvoiceSchemeFreeProductList.add(schemeProductBO);
                        if (schemeid != c.getInt(4)) {
                            if (schemeid != 0) {
                                schemeBO = new SchemeBO();
                                schemeBO.setSchemeId(schemeid + "");
                                schemeBO.setFreeType(freeType);
                                schemeBO.setBuyType("SV");
                                schemeBO.setSchemeParentName(schemeDesc);
                                schemeBO.setSchemeDescription(schemeDesc);
                                mOffInvoiceSchemeList.add(schemeBO);


                                schemeBO.setFreeProducts(freeProductList);
                                freeProductList = new ArrayList<SchemeProductBO>();
                                freeProductList.add(schemeProductBO);
                                schemeid = c.getInt(4);
                                schemeDesc = c.getString(6);
                                freeType = c.getString(9);
                            } else {
                                freeProductList.add(schemeProductBO);
                                schemeid = c.getInt(4);
                                schemeDesc = c.getString(6);
                                freeType = c.getString(9);
                            }
                        } else {
                            freeProductList.add(schemeProductBO);
                        }


                        mFreeGroupTypeByFreeGroupName.put(c.getInt(4) + c.getString(7), c.getString(8));

                    }
                    if (freeProductList.size() > 0) {

                        schemeBO = new SchemeBO();
                        schemeBO.setSchemeId(schemeid + "");
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
            downloadOffInvoiceFreeGroupName(db);
            db.closeDB();
            //updateOffInvoiceFreeProductGroupName();
        } catch (Exception e) {
            Commons.print(e + "");
        }
    }

    private void updateOffInvoiceFreeProductGroupName() {
        for (SchemeBO schemeBO : mOffInvoiceSchemeList) {
            ArrayList<String> freeBuyNameList = new ArrayList<>();
            freeBuyNameList.add(schemeBO.getSchemeId());

            mFreeGroupTypeNameBySchemeId.put(schemeBO.getSchemeId(),
                    freeBuyNameList);
        }
    }

    public ArrayList<SchemeProductBO> getOffInvoiceSchemeFreeProductList() {
        if (mOffInvoiceSchemeFreeProductList == null) {
            mOffInvoiceSchemeFreeProductList = new ArrayList<>();
        }
        return mOffInvoiceSchemeFreeProductList;
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

        for (Iterator<SchemeBO> iterator = mApplySchemeList.iterator(); iterator.hasNext(); ) {
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

                groupNameList = mFreeGroupTypeNameBySchemeId.get(schemeid);
                if (previousGroupNameList != null) {
                    if (!previousGroupNameList.equals(groupNameList)) {
                        return false;
                    }
                }
                previousGroupNameList = mFreeGroupTypeNameBySchemeId.get(schemeid);
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
                sb.append("SFD.invoiceid=" + bmodel.QT(id));
            } else {// order report
                sb.append("SFD.orderid=" + bmodel.QT(id));
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

                    ProductMasterBO schemeProduct = bmodel.productHelper
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
     * load ALL Scheme Free Product's to show in Detail view
     */
    public ArrayList<SchemeBO> downLoadAllFreeSchemeDetail() {
        ArrayList<SchemeBO> schemeFreePrdList = null;
        SchemeBO schemeProductBO;
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT distinct parentid, Description, ShortName FROM "
                    + "SchemeMaster");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                schemeFreePrdList = new ArrayList<>();
                while (c.moveToNext()) {
                    schemeProductBO = new SchemeBO();
                    schemeProductBO.setParentId(c.getInt(0));
                    schemeProductBO.setSchemeDescription(c.getString(1));
                    schemeProductBO.setGroupName(c.getString(2));
                    schemeFreePrdList.add(schemeProductBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
        return schemeFreePrdList;
    }


}









