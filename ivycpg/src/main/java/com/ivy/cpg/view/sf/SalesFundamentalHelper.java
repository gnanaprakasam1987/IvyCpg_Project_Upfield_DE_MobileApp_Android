package com.ivy.cpg.view.sf;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.bo.SOSKUBO;
import com.ivy.sd.png.bo.ShelfShareBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SalesFundamentalHelper {

    private Context mContext;
    private BusinessModel mBModel;
    private ShelfShareHelper mShelfShareHelper;
    private static SalesFundamentalHelper instance = null;


    private ArrayList<SOSBO> lstSOSproj;
    private ArrayList<String> lstSOSProjAttributes;
    private ArrayList<SOSBO> mSOSList;
    private ArrayList<SODBO> mSODList;
    private ArrayList<SOSKUBO> mSOSKUList;
    private ArrayList<SFLocationBO> mLocationList;
    private Vector<LevelBO> mSFModuleSequence;
    private Vector<LevelBO> mFilterLevel;
    private HashMap<Integer, Vector<LevelBO>> mFilterProductsByLevelId;


    private String moduleSOS = "MENU_SOS";
    private String moduleSOSKU = "MENU_SOSKU";
    private String moduleSOD = "MENU_SOD";
    public int mSelectedBrandID = 0;
    public String mSelectedActivityName;
    public int mSOSTotalPopUpType;
    public int mSOSCatgPopUpType;
    private int mChannelId, mLocationId;

    public int sosDigits = 4;
    public int sodDigits = 4;
    public static final String CODE_SOS_DIGITS = "SOS03";
    public static final String CODE_SOD_DIGITS = "SOD01";


    public List<SOSBO> mCategoryForDialogSOSBO = null;
    public List<SODBO> mCategoryForDialogSODBO = null;


    protected SalesFundamentalHelper(Context context) {
        this.mContext = context;
        this.mBModel = (BusinessModel) context.getApplicationContext();
        mShelfShareHelper = ShelfShareHelper.getInstance();
    }

    public static SalesFundamentalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SalesFundamentalHelper(context);
        }
        return instance;
    }

    /**
     * update SF configurations
     */
    public void updateSalesFundamentalConfigurations() {
        try {

            String sql = "select hhtCode, RField,menu_type from "
                    + DataMembers.tbl_HhtModuleMaster + " where menu_type='COMMON' and flag=1 and ForSwitchSeller = 0";

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            if (c.getCount() != 0) {
                while (c.moveToNext()) {
                    String code = c.getString(0);
                    if (code.equals(CODE_SOS_DIGITS)) {
                        sosDigits = c.getInt(1);
                        sosDigits = sosDigits >= 4 ? 4 : sosDigits;
                    } else if (code.equals(CODE_SOD_DIGITS)) {
                        sodDigits = c.getInt(1);
                        sodDigits = sodDigits >= 4 ? 4 : sodDigits;
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    /**
     * Method used to clone given list
     *
     * @param list List to clone
     * @return
     */
    public static ArrayList<SFLocationBO> cloneLocationList(
            ArrayList<SFLocationBO> list) {
        ArrayList<SFLocationBO> clone = new ArrayList<>(list.size());
        for (SFLocationBO item : list)
            clone.add(new SFLocationBO(item));
        return clone;
    }

    /**
     * Load TotalPopUPConfiguration from HhtModuleMaster HHTCode :
     * moduleName+"01" - For Products in Total PopUp HHTCode : moduleName+"02" -
     * For Total PopUp Type
     */
    public void setTotalPopUpConfig() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT COUNT(HHTCode) FROM HhtModuleMaster"
                    + " WHERE HHTCode='VISUALSF' AND Flag='1' and  ForSwitchSeller = 0");
            if (c != null) {
                if (c.moveToNext() && c.getInt(0) > 0) {
                    mSOSTotalPopUpType = 1;
                }
                c.close();
            }

            c = db.selectSQL("SELECT COUNT(HHTCode) FROM HhtModuleMaster"
                    + " WHERE HHTCode='CATGSF' AND Flag='1' and  ForSwitchSeller = 0");
            if (c != null) {
                if (c.moveToNext() && c.getInt(0) > 0) {
                    mSOSCatgPopUpType = 1;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            if (db != null) {
                db.closeDB();
            }
        }
    }

    public HashMap<Integer, Vector<LevelBO>> getFiveLevelFilters() {
        return mFilterProductsByLevelId;
    }

    public Vector<LevelBO> getSequenceValues() {
        return mSFModuleSequence;
    }

    public void setmSFModuleSequence(Vector<LevelBO> mSFModuleSequence) {
        this.mSFModuleSequence = mSFModuleSequence;
    }

    public void setmFilterProductsByLevelId(HashMap<Integer, Vector<LevelBO>> mFilterProductsByLevelId) {
        this.mFilterProductsByLevelId = mFilterProductsByLevelId;
    }

    /**
     * Download masters for sales fundamental
     *
     * @param moduleName Module Name
     * @param IsAccount  Is Account wise mapped
     * @param IsRetailer Is Retailer wise mapped
     * @param IsClass    Is class wise mapped
     * @param LocId      Is location wise mapped
     * @param ChId       Is channel wise mapped
     */
    private void downloadSalesFundamental(String moduleName, boolean IsAccount, boolean IsRetailer, boolean IsClass, int LocId, int ChId) {
        DBUtil db = null;
        try {
            Cursor cursor;
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            int mContentLevelId = 0;

            StringBuilder accountGroupIds=new StringBuilder();
            String accountQuery="Select groupId from AccountGroupDetail where retailerId="+ mBModel.getRetailerMasterBO().getRetailerID();
            Cursor accountCursor=db.selectSQL(accountQuery);
            if(accountCursor.getCount()>0){
                while (accountCursor.moveToNext()){
                    accountGroupIds.append(accountCursor.getString(0));

                    if(accountGroupIds.toString().length()>0)
                        accountGroupIds.append(",");
                }
            }

            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL.LevelId,0) "
                            + "FROM ConfigActivityFilter CF "
                            // Left join is to ensure configured level id is valid.
                            + "LEFT JOIN ProductLevel PL ON PL.LevelId = CF.ProductContent "
                            + " WHERE CF.ActivityCode = "
                            + mBModel.QT(moduleName));


            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mContentLevelId = filterCur.getInt(0);
                }
                filterCur.close();
            }


            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("SELECT DISTINCT A.ParentId,A.pid,");
            sBuffer.append("A.pname ,1 isOwn,");
            sBuffer.append("IFNULL(SFN.Norm,0) as Norm,SFN.MappingId,A.ParentHierarchy FROM ProductMaster A");


            if (mBModel.configurationMasterHelper.IS_SF_NORM_CHECK) {
                sBuffer.append(" INNER JOIN ");
            } else {
                sBuffer.append(" LEFT JOIN ");
            }
            sBuffer.append(moduleName.replace("MENU_", "") + "_NormMapping  SFN ON A.pid = SFN.pid  ");

            if(accountGroupIds.toString().length()>0) {
               sBuffer.append(" AND SFN.accountGroupId in("+accountGroupIds.toString()+")");
            }
            else {
                if (IsRetailer) {
                    sBuffer.append("and SFN.RetailerId =");
                    sBuffer.append(mBModel.getRetailerMasterBO().getRetailerID());
                }
                if (IsAccount) {
                    sBuffer.append(" and SFN.AccId=" + mBModel.getRetailerMasterBO().getAccountid());
                }
                if (IsClass) {
                    sBuffer.append(" and SFN.ClassId=" + mBModel.getRetailerMasterBO().getClassid());
                }

                if (LocId > 0)
                    sBuffer.append(" and SFN.LocId=" + mBModel.productHelper.getMappingLocationId(LocId, mBModel.getRetailerMasterBO().getLocationId()));
                if (ChId > 0)
                    sBuffer.append(" and SFN.ChId=" + mBModel.productHelper.getMappingChannelId(ChId, mBModel.getRetailerMasterBO().getSubchannelid()));
            }

            sBuffer.append(" LEFT JOIN " + moduleName.replace("MENU_", "") + "_NormMaster   SF ON SF.HId = SFN.HId");
            sBuffer.append(" AND " + mBModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " BETWEEN SF.StartDate AND SF.EndDate");
            sBuffer.append(" WHERE A.PLID IN (" + mContentLevelId + ")");


            if (moduleName.equals(moduleSOS)) {
                SOSBO mSOS;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setSOSList(new ArrayList<SOSBO>());
                    while (cursor.moveToNext()) {
                        mSOS = new SOSBO();
                        mSOS.setParentID(cursor.getInt(0));
                        mSOS.setProductID(cursor.getInt(1));
                        mSOS.setProductName(cursor.getString(2));
                        mSOS.setIsOwn(cursor.getInt(3));
                        mSOS.setNorm(cursor.getFloat(4));
                        mSOS.setMappingId(cursor.getInt(5));
                        mSOS.setParentHierarchy(cursor.getString(6));
                        mSOS.setLocations(cloneLocationList(getLocationList()));
                        getSOSList().add(mSOS);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOS);
            } else if (moduleName.equals(moduleSOD)) {
                SODBO mSOD;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setSODList(new ArrayList<SODBO>());
                    while (cursor.moveToNext()) {
                        mSOD = new SODBO();
                        mSOD.setParentID(cursor.getInt(0));
                        mSOD.setProductID(cursor.getInt(1));
                        mSOD.setProductName(cursor.getString(2));
                        mSOD.setIsOwn(cursor.getInt(3));
                        mSOD.setNorm(cursor.getFloat(4));
                        mSOD.setMappingId(cursor.getInt(5));
                        mSOD.setParentHierarchy(cursor.getString(6));
                        mSOD.setLocations(cloneLocationList(getLocationList()));
                        getSODList().add(mSOD);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOD);
            } else if (moduleName.equals(moduleSOSKU)) {
                SOSKUBO mSOSKU;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setSOSKUList(new ArrayList<SOSKUBO>());
                    while (cursor.moveToNext()) {
                        mSOSKU = new SOSKUBO();
                        mSOSKU.setParentID(cursor.getInt(0));
                        mSOSKU.setProductID(cursor.getInt(1));
                        mSOSKU.setProductName(cursor.getString(2));
                        mSOSKU.setIsOwn(cursor.getInt(3));
                        mSOSKU.setNorm(cursor.getFloat(4));
                        mSOSKU.setMappingId(cursor.getInt(5));
                        mSOSKU.setParentHierarchy(cursor.getString(6));
                        getSOSKUList().add(mSOSKU);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOSKU);
            }
            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException(moduleName, e);
        }
    }

    /**
     * Load Competitors
     *
     * @param moduleName Module Name
     */
    private void loadCompetitors(String moduleName) {
        DBUtil db;
        ArrayList<String> lstCompetitiorPids;
        try {
            Cursor cursor;
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("SELECT DISTINCT PM.pid ,CP.CPID, CP.CPName,0,0,PM.ParentHierarchy FROM CompetitorProductMaster CP");
            sBuffer.append(" INNER JOIN CompetitorMappingMaster CM ON CM.CPid = CP.CPId");
            sBuffer.append(" INNER JOIN ProductMaster PM ON PM.PID = CM.PID");
            sBuffer.append(" WHERE  CP.Plid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + mBModel.QT(moduleName) + ")");

            cursor = db.selectSQL(sBuffer.toString());
            if (moduleName.equals(moduleSOS)) {
                if (cursor != null) {
                    lstCompetitiorPids = new ArrayList<>();
                    while (cursor.moveToNext()) {

                        if (!lstCompetitiorPids.contains(cursor.getInt(1) + ","+ cursor.getInt(0))) {
                            for (SOSBO prodBO : getSOSList()) {

                                if (prodBO.getProductID() == cursor.getInt(0)) {

                                    SOSBO comLevel = new SOSBO();
                                    comLevel.setParentID(prodBO.getParentID());
                                    comLevel.setProductID(cursor.getInt(1));
                                    comLevel.setProductName(cursor.getString(2));
                                    comLevel.setIsOwn(cursor.getInt(3));
                                    comLevel.setNorm(cursor.getInt(4));
                                    comLevel.setLocations(cloneLocationList(getLocationList()));
                                    comLevel.setParentHierarchy(cursor.getString(5));
                                    lstCompetitiorPids.add(cursor.getInt(1) + ","+ cursor.getInt(0));
                                    getSOSList().add(comLevel);
                                    break;
                                }

                            }
                        }
                    }

                    cursor.close();
                }
            } else if (moduleName.equals(moduleSOD)) {

                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        for (SODBO prodBO : getSODList()) {

                            if (prodBO.getProductID() == cursor.getInt(0)) {

                                SODBO comLevel = new SODBO();
                                comLevel.setParentID(prodBO.getParentID());
                                comLevel.setProductID(cursor.getInt(1));
                                comLevel.setProductName(cursor.getString(2));
                                comLevel.setIsOwn(cursor.getInt(3));
                                comLevel.setNorm(cursor.getInt(4));
                                comLevel.setLocations(cloneLocationList(getLocationList()));
                                comLevel.setParentHierarchy(cursor.getString(5));
                                getSODList().add(comLevel);
                                break;
                            }

                        }
                    }
                    cursor.close();
                }
            } else if (moduleName.equals(moduleSOSKU)) {
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    lstCompetitiorPids = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        if (!lstCompetitiorPids.contains(cursor.getInt(1) + ","+ cursor.getInt(0))) {
                            for (SOSKUBO prodBO : getSOSKUList()) {

                                if (prodBO.getProductID() == cursor.getInt(0)) {

                                    SOSKUBO comLevel = new SOSKUBO();
                                    comLevel.setParentID(prodBO.getParentID());
                                    comLevel.setProductID(cursor.getInt(1));
                                    comLevel.setProductName(cursor.getString(2));
                                    comLevel.setIsOwn(cursor.getInt(3));
                                    comLevel.setNorm(cursor.getInt(4));
                                    lstCompetitiorPids.add(cursor.getInt(1) + ","+ cursor.getInt(0));
                                    getSOSKUList().add(comLevel);
                                    break;
                                }

                            }
                        }
                    }
                    cursor.close();
                }
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Save Sales Fundamentals Module wise
     *
     * @param moduleName Module Name
     * @return status
     */
    public boolean saveSalesFundamentalDetails(String moduleName) {
        String modName = moduleName.replaceAll("MENU_", "");
        int count;
        try {
            String refId = "0";
            String headerValues;
            String detailValues;
            String headerColumns = "Uid,RetailerId,Date,Remark,refid,ridSF,VisitId";
            String detailColumns = "Uid,Pid,RetailerId,Norm,ParentTotal,Required,Actual,Percentage,Gap,ReasonId,ImageName,IsOwn,ParentID,IsAuditDone,MappingId,LocId,imgName";

            String mParentDetailColumns = "Uid, PId, BlockCount, ShelfCount,"
                    + " ShelfLength, ExtraShelf,total,RetailerId,LocId";
            String mParentDetailValues;

            String mBlockDetailColumns = "Uid,PId,ChildPid,SubCellId,CellId,Retailerid,LocId";
            String mBlockDetailValues;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String uid = (mBModel.getAppDataProvider().getUser().getUserid() + DateTimeUtils
                    .now(DateTimeUtils.DATE_TIME_ID));

            String query = "select Uid,refid from " + modName
                    + "_Tracking_Header  where RetailerId="
                    + StringUtils.getStringQueryParam(mBModel.retailerMasterBO.getRetailerID());
            query += " and (upload='N' OR refid!=0)";

            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(modName + "_Tracking_Header",
                        "Uid=" + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                db.deleteSQL(modName + "_Tracking_Detail",
                        "Uid=" + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                if (modName.equals("SOS")) {
                    db.deleteSQL(modName + "_Tracking_Parent_Detail", "Uid="
                            + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                    db.deleteSQL(DataMembers.tbl_SOS__Block_Tracking_Detail,
                            "Uid=" + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                }
                refId = cursor.getString(1);
                // uid = cursor.getString(0);
            }
            cursor.close();
            // Inserting Header in Tables

            headerValues = StringUtils.getStringQueryParam(uid)
                    + "," + mBModel.getAppDataProvider().getRetailMaster().getRetailerID()
                    + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + "," + StringUtils.getStringQueryParam(mBModel.getNote())
                    + "," + StringUtils.getStringQueryParam(refId)
                    + "," + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster().getRidSF())
                    + "," + mBModel.getAppDataProvider().getUniqueId();

            db.insertSQL(modName + "_Tracking_Header", headerColumns,
                    headerValues);

            if (modName.equals("SOS")) {
                count = 1;
                int locid1;
                String tempkey = "";
                String mKey1 = "";
                detailColumns += ",remarks";
                for (SOSBO sosBo : getSOSList()) {
                    for (int i = 0; i < sosBo.getLocations().size(); i++) {
                        if ((!sosBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sosBo.getLocations().get(i).getParentTotal().equals("0.0"))
                                || sosBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = StringUtils.getStringQueryParam(uid)
                                    + "," + sosBo.getProductID()
                                    + "," + mBModel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + sosBo.getNorm()
                                    + "," + sosBo.getLocations().get(i).getParentTotal()
                                    + "," + sosBo.getLocations().get(i).getTarget()
                                    + "," + sosBo.getLocations().get(i).getActual()
                                    + "," + sosBo.getLocations().get(i).getPercentage()
                                    + "," + sosBo.getLocations().get(i).getGap()
                                    + "," + (sosBo.getLocations().get(i).getReasonId() == -1 ? 0 : sosBo.getLocations().get(i).getReasonId())
                                    + "," + StringUtils.getStringQueryParam(sosBo.getLocations().get(i).getImageName())
                                    + "," + sosBo.getIsOwn()
                                    + "," + sosBo.getParentID()
                                    + "," + sosBo.getLocations().get(i).getAudit()
                                    + "," + sosBo.getMappingId()
                                    + "," + sosBo.getLocations().get(i).getLocationId()
                                    + "," + StringUtils.getStringQueryParam(sosBo.getLocations().get(i).getImgName())
                                    + "," + StringUtils.getStringQueryParam(sosBo.getLocations().get(i).getRemarks());

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues);

                            // For share shelf detail
                            HashMap<String, Object> hashMap = null;
                            mKey1 = sosBo.getParentID() + "";
                            locid1 = sosBo.getLocations().get(i).getLocationId();

                            if (mShelfShareHelper.getLocations().get(i).getShelfDetailForSOS()
                                    .containsKey(mKey1))
                                hashMap = mShelfShareHelper.getLocations().get(i)
                                        .getShelfDetailForSOS().get(mKey1);

                            if (hashMap != null) {
                                mParentDetailValues = StringUtils.getStringQueryParam(uid)
                                        + "," + sosBo.getParentID()
                                        + "," + hashMap.get(ShelfShareHelper.BLOCK_COUNT)
                                        + "," + hashMap.get(ShelfShareHelper.SHELF_COUNT)
                                        + "," + hashMap.get(ShelfShareHelper.SHELF_LENGTH)
                                        + "," + hashMap.get(ShelfShareHelper.EXTRA_LENGTH)
                                        + "," + sosBo.getLocations().get(i).getParentTotal()
                                        + "," + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster().getRetailerID())
                                        + "," + sosBo.getLocations().get(i).getLocationId();


                                if (count > 0 && !mKey1.equals(tempkey)) {
                                    db.insertSQL(modName
                                                    + "_Tracking_Parent_Detail",
                                            mParentDetailColumns,
                                            mParentDetailValues);
                                    count = 0;
                                } else {
                                    cursor = db.selectSQL("SELECT * FROM " + modName
                                            + "_Tracking_Parent_Detail WHERE Uid = "
                                            + StringUtils.getStringQueryParam(uid) + " AND PId = "
                                            + sosBo.getParentID() + " and locid=" + locid1);
                                    if (cursor.getCount() == 0) {
                                        db.insertSQL(modName
                                                        + "_Tracking_Parent_Detail",
                                                mParentDetailColumns,
                                                mParentDetailValues);
                                        cursor.close();
                                    } else {
                                        cursor.close();
                                    }
                                }

                            } else {
                                mParentDetailValues = mBModel.QT(uid)
                                        + "," + sosBo.getParentID()
                                        + "," + 0
                                        + "," + 0
                                        + "," + 0
                                        + "," + 0
                                        + "," + sosBo.getLocations().get(i).getParentTotal()
                                        + "," + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster().getRetailerID())
                                        + "," + sosBo.getLocations().get(i).getLocationId();

                                if (count > 0 && !mKey1.equals(tempkey)) {
                                    db.insertSQL(modName
                                                    + "_Tracking_Parent_Detail",
                                            mParentDetailColumns,
                                            mParentDetailValues);
                                    count = 0;
                                } else {
                                    cursor = db.selectSQL("SELECT * FROM " + modName
                                            + "_Tracking_Parent_Detail WHERE Uid = "
                                            + StringUtils.getStringQueryParam(uid) + " AND PId = "
                                            + sosBo.getParentID() + " and locid=" + locid1);
                                    if (cursor.getCount() == 0) {
                                        db.insertSQL(modName
                                                        + "_Tracking_Parent_Detail",
                                                mParentDetailColumns,
                                                mParentDetailValues);
                                        cursor.close();
                                    } else {
                                        cursor.close();
                                    }
                                }
                            }
                        }

                    }
                    if (mKey1.equals(tempkey))
                        count = 0;
                    else {
                        tempkey = mKey1;
                        count = 1;
                    }

                }

                String mKeytemp = "";
                String mKey;
                for (SOSBO sosBo1 : getSOSList()) {
                    for (int i = 0; i < sosBo1.getLocations().size(); i++) {
                        if (!sosBo1.getLocations().get(i).getParentTotal().equals("0")
                                && !sosBo1.getLocations().get(i).getParentTotal().equals("0.0")) {
                            // For share shelf detail
                            HashMap<String, ShelfShareBO> blockHashMap;
                            mKey = sosBo1.getParentID() + "";
                            if (mKeytemp.equals(""))
                                mKeytemp = mKey;
                            blockHashMap = mShelfShareHelper.getLocations().get(i)
                                    .getShelfBlockDetailForSOS().get(mKey);
                            if (blockHashMap != null) {
                                for (String key : blockHashMap.keySet()) {
                                    ShelfShareBO ssBO = blockHashMap.get(key);
                                    if (ssBO.getFirstCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + "," + sosBo1.getParentID()
                                                + "," + sosBo1.getProductID()
                                                + "," + key
                                                + "," + "1"
                                                + "," + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster().getRetailerID())
                                                + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }

                                    //Saving Empty Block Details

                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = mBModel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = mBModel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = mBModel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }


                                    //saving Extra shelf details **/
                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","
                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }

                                    //}
                                }

                            }

                        }
                    }
                    /*if (mKey.equals(mKeytemp)) {
                        count = 0;
                    } else {
                        mKeytemp = mKey;
                        count = 1;
                    }*/


                }

            } else if (modName.equals("SOD")) {
                count = 1;
                String mKey = "", tempkey = "";
                int locid;
                for (SODBO sodBo : getSODList()) {
                    for (int i = 0; i < sodBo.getLocations().size(); i++) {
                        if (!sodBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sodBo.getLocations().get(i).getParentTotal().equals("0.0") || sodBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = StringUtils.getStringQueryParam(uid) + ","
                                    + sodBo.getProductID() + ","
                                    + mBModel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + sodBo.getNorm() + ","
                                    + sodBo.getLocations().get(i).getParentTotal() + ","
                                    + sodBo.getLocations().get(i).getTarget() + "," + sodBo.getLocations().get(i).getActual()
                                    + "," + sodBo.getLocations().get(i).getPercentage() + ","
                                    + sodBo.getLocations().get(i).getGap() + "," + sodBo.getLocations().get(i).getReasonId()
                                    + "," + StringUtils.getStringQueryParam(sodBo.getLocations().get(i).getImageName()) + ","
                                    + sodBo.getIsOwn() + "," + sodBo.getParentID() + "," + sodBo.getLocations().get(i).getAudit() + "," + sodBo.getMappingId()
                                    + "," + sodBo.getLocations().get(i).getLocationId()
                                    + "," + StringUtils.getStringQueryParam(sodBo.getLocations().get(i).getImgName());

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues);

                            // For share shelf detail
                            HashMap<String, Object> hashMap = null;
                            mKey = sodBo.getParentID() + "";
                            if (tempkey.equals(""))
                                tempkey = mKey;
                            locid = sodBo.getLocations().get(i).getLocationId();
                            if (mShelfShareHelper.getLocations().get(i).getShelfDetailForSOD()
                                    .containsKey(mKey))
                                hashMap = mShelfShareHelper.getLocations().get(i)
                                        .getShelfDetailForSOD().get(mKey);

                            if (hashMap != null) {
                                mParentDetailValues = StringUtils.getStringQueryParam(uid)
                                        + ","
                                        + sodBo.getParentID()
                                        + ","
                                        + hashMap.get(ShelfShareHelper.BLOCK_COUNT)
                                        + ","
                                        + hashMap.get(ShelfShareHelper.SHELF_COUNT)
                                        + ","
                                        + hashMap
                                        .get(ShelfShareHelper.SHELF_LENGTH)
                                        + ","
                                        + hashMap
                                        .get(ShelfShareHelper.EXTRA_LENGTH)
                                        + ","
                                        + sodBo.getLocations().get(i).getParentTotal()
                                        + ","
                                        + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster()
                                        .getRetailerID()) + "," + sodBo.getLocations().get(i).getLocationId();
                                if (count > 0 && !mKey.equals(tempkey)) {
                                    db.insertSQL(modName
                                                    + "_Tracking_Parent_Detail",
                                            mParentDetailColumns,
                                            mParentDetailValues);
                                    count = 0;
                                } else {
                                    cursor = db.selectSQL("SELECT * FROM " + modName
                                            + "_Tracking_Parent_Detail WHERE Uid = "
                                            + StringUtils.getStringQueryParam(uid) + " AND PId = "
                                            + sodBo.getParentID() + " and LocId=" + locid);

                                    if (cursor.getCount() == 0) {
                                        db.insertSQL(modName
                                                        + "_Tracking_Parent_Detail",
                                                mParentDetailColumns,
                                                mParentDetailValues);
                                        cursor.close();
                                    } else {
                                        cursor.close();
                                    }
                                }
                            } else {
                                mParentDetailValues = StringUtils.getStringQueryParam(uid)
                                        + ","
                                        + sodBo.getParentID()
                                        + ","
                                        + 0
                                        + ","
                                        + 0
                                        + ","
                                        + 0
                                        + ","
                                        + 0
                                        + ","
                                        + sodBo.getLocations().get(i).getParentTotal()
                                        + ","
                                        + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster()
                                        .getRetailerID()) + "," + sodBo.getLocations().get(i).getLocationId();

                                if (count > 0 && !mKey.equals(tempkey)) {
                                    db.insertSQL(modName
                                                    + "_Tracking_Parent_Detail",
                                            mParentDetailColumns,
                                            mParentDetailValues);
                                    count = 0;
                                } else {
                                    cursor = db.selectSQL("SELECT * FROM " + modName
                                            + "_Tracking_Parent_Detail WHERE Uid = "
                                            + StringUtils.getStringQueryParam(uid) + " AND PId = "
                                            + sodBo.getParentID() + " and LocId=" + locid);

                                    if (cursor.getCount() == 0) {
                                        db.insertSQL(modName
                                                        + "_Tracking_Parent_Detail",
                                                mParentDetailColumns,
                                                mParentDetailValues);
                                        cursor.close();
                                    } else {
                                        cursor.close();
                                    }
                                }
                            }


                        }
                    }
                    if (mKey.equals(tempkey))
                        count = 0;
                    else {
                        tempkey = mKey;

                        count = 1;
                    }
                }

                String Key;
                String mKeytemp = "";
                for (SODBO sodBo1 : getSODList()) {
                    for (int i = 0; i < sodBo1.getLocations().size(); i++) {
                        if (!sodBo1.getLocations().get(i).getParentTotal().equals("0")
                                && !sodBo1.getLocations().get(i).getParentTotal().equals("0.0")) {
                            // For share shelf detail
                            HashMap<String, ShelfShareBO> blockHashMap;
                            Key = sodBo1.getParentID() + "";
                            if (mKeytemp.equals(""))
                                mKeytemp = Key;
                            blockHashMap = mShelfShareHelper.getLocations().get(i)
                                    .getShelfBlockDetailForSOD().get(Key);

                            if (blockHashMap != null) {
                                Commons.print("key set" + blockHashMap.keySet().toString());
                                for (String key : blockHashMap.keySet()) {
                                    ShelfShareBO ssBO = blockHashMap.get(key);
                                    if (ssBO.getFirstCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }

                                    //Extra Block Details **/
                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = StringUtils.getStringQueryParam(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + StringUtils.getStringQueryParam(mBModel
                                                .getAppDataProvider().getRetailMaster()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues);
                                    }

                                    //}
                                }

                            }
                        }
                    }

                }
            } else if (modName.equals("SOSKU")) {
                for (SOSKUBO soskuBO : getSOSKUList()) {
                    if (soskuBO.getParentTotal() > 0) {
                        detailValues = StringUtils.getStringQueryParam(uid) + ","
                                + soskuBO.getProductID() + ","
                                + mBModel.getAppDataProvider().getRetailMaster().getRetailerID()
                                + "," + soskuBO.getNorm() + ","
                                + soskuBO.getParentTotal() + ","
                                + soskuBO.getTarget() + ","
                                + soskuBO.getActual() + ","
                                + soskuBO.getPercentage() + ","
                                + soskuBO.getGap() + ","
                                + soskuBO.getReasonId() + ","
                                + StringUtils.getStringQueryParam(soskuBO.getImageName()) + ","
                                + soskuBO.getIsOwn() + ","
                                + soskuBO.getParentID() + ","
                                + 0 + ","
                                + soskuBO.getMappingId() + ","
                                + 0 + ","
                                + StringUtils.getStringQueryParam(soskuBO.getImgName());

                        db.insertSQL(modName + "_Tracking_Detail",
                                detailColumns, detailValues);
                    }

                }

            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return true;
    }

    /**
     * Load and set Values to objects in Edit Mode
     *
     * @param uid         Transaction Id
     * @param pid         Product Id
     * @param totalShelf  Total Shelf
     * @param mLocationId Location Id
     */
    public HashMap<String, ShelfShareBO> loadSOSBlockDetails(String uid, String pid, int totalShelf, int mLocationId) {
        DBUtil db = null;
        HashMap<String, ShelfShareBO> mBrandsDetailsHashMap = new HashMap<>();

        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("select sb.SubCellId,sb.CellId,sb.ChildPid,PM.pname from SOS_Tracking_Block_Detail SB ");
            sb.append("left join productmaster pm on PM.pid=SB.ChildPid");
            sb.append(" WHERE sb.Uid=" + mBModel.QT(uid) + "and SB.PID="
                    + mBModel.QT(pid) + " and SB.LocId=" + mLocationId);
            sb.append(" order by sb.SubCellId ,sb.CellId  ");

            Cursor detailCursor = db.selectSQL(sb.toString());

            if (detailCursor.getCount() > 0) {

                for (int i = 0; i < totalShelf; i++) {
                    ShelfShareBO shelfShareBO = new ShelfShareBO();

                    shelfShareBO.setFirstCell("empty");
                    shelfShareBO.setSecondCell("empty");
                    shelfShareBO.setThirdCell("empty");
                    shelfShareBO.setFourthCell("empty");
                    mBrandsDetailsHashMap.put(String.valueOf(i), shelfShareBO);

                    while (detailCursor.moveToNext()) {

                        if (i == detailCursor.getInt(0)) {
                            if (detailCursor.getInt(2) != 0) {
                                if (detailCursor.getInt(1) == 1)
                                    shelfShareBO.setFirstCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 2)
                                    shelfShareBO.setSecondCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 3)
                                    shelfShareBO.setThirdCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 4)
                                    shelfShareBO.setFourthCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                shelfShareBO.setOthersCount(1);
                                mBrandsDetailsHashMap.put(String.valueOf(i), shelfShareBO);
                            }
                        } else {
                            detailCursor.moveToPrevious();
                            break;
                        }
                    }

                }
            }
            detailCursor.close();

            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
        }

        return mBrandsDetailsHashMap;
    }

    /**
     * Load and set Values to objects in Edit Mode
     *
     * @param mModuleName Module Name
     */
    public boolean loadSavedTracking(String mModuleName) {
        DBUtil db = null;
        String sql;
        String uid;
        String moduleName = mModuleName.replaceAll("MENU_", "");
        boolean isDataAvailableSOS = false, isDataAvailableSOD = false, isDataAvailableSOSKU = false;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            mBModel.setNote("");
            sql = "SELECT Uid,Remark FROM " + moduleName + "_Tracking_Header"
                    + " WHERE RetailerId="
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + " and (upload='N' OR refid!=0)";
            /*
             * + " AND Date = " + mBModel.getStringQueryParam(SDUtil.now(SDUtil.DATE_GLOBAL));
             */

            Cursor headerCursor = db.selectSQL(sql);
            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                uid = headerCursor.getString(0);
                // Set Remark in Object
                mBModel.setNote(headerCursor.getString(1));

                DataMembers.uidSOS = uid;
                DataMembers.uidSOD = uid;
                StringBuffer sb = new StringBuffer();

                sb.append("SELECT SF.Pid,SF.Norm,SF.ParentTotal,SF.Required,SF.Actual,SF.Percentage,SF.Gap,SF.ReasonId,SF.ImageName,SF.Isown,IFNULL(SF.IsAuditDone,'2')");
                if (moduleName.equals("SOS") || moduleName.equals("SOD")) {
                    sb.append(", IFNULL(B.BlockCount,'0'), IFNULL(B.ShelfCount,'0'),IFNULL(B.ShelfLength,'0'), IFNULL(B.ExtraShelf,'0'),SF.Parentid,SF.locid");
                    if (moduleName.equals("SOS")) {
                        sb.append(",SF.remarks");
                    }
                }
                sb.append(" , SF.imgName as tempImageName From " + moduleName + "_Tracking_Detail SF");
                {
                    sb.append(" LEFT JOIN " + moduleName
                            + "_Tracking_Parent_Detail B ON");
                    sb.append(" B.Uid = SF.Uid AND B.PId = SF.Parentid and SF.locid=B.locid");
                }
                sb.append(" WHERE SF.Uid=" + mBModel.QT(uid));

                // For shelf share data
                HashMap<String, Object> hashMap;

                Cursor detailCursor = db.selectSQL(sb.toString());

                if (detailCursor.getCount() > 0) {

                    while (detailCursor.moveToNext()) {
                        if (mModuleName.equalsIgnoreCase(moduleSOS)) {
                            for (SOSBO msos : getSOSList()) {
                                if (msos.getProductID() == detailCursor
                                        .getInt(0)
                                        && msos.getIsOwn() == detailCursor
                                        .getInt(9)) {
                                    for (int i = 0; i < msos.getLocations().size(); i++) {
                                        if (msos.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            msos.setNorm(detailCursor.getFloat(1));
                                            msos.getLocations().get(i).setParentTotal(SDUtil
                                                    .convertToFloat(detailCursor
                                                            .getString(2))
                                                    + "");
                                            msos.getLocations().get(i).setTarget(detailCursor.getString(3));
                                            msos.getLocations().get(i).setActual(detailCursor.getString(4));
                                            msos.getLocations().get(i).setPercentage(detailCursor
                                                    .getString(5));
                                            msos.getLocations().get(i).setGap(detailCursor.getString(6));
                                            msos.getLocations().get(i).setReasonId(detailCursor.getInt(7));
                                            msos.getLocations().get(i).setImageName(detailCursor.getString(8));
                                            msos.getLocations().get(i).setRemarks(detailCursor.getString(detailCursor.getColumnIndex("remarks")));
                                            msos.getLocations().get(i).setImgName(detailCursor.getString(detailCursor.getColumnIndex("tempImageName")));
                                            msos.getLocations().get(i).setAudit(detailCursor.getInt(10));
                                            isDataAvailableSOS = true;
                                        }
                                    }
                                }
                            }

                            String mKey = detailCursor.getInt(15) + "";
                            for (SOSBO msos : getSOSList()) {
                                if (msos.getProductID() == detailCursor
                                        .getInt(0)
                                        && msos.getIsOwn() == detailCursor
                                        .getInt(9)) {
                                    for (int i = 0; i < msos.getLocations().size(); i++) {
                                        if (msos.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            if (!mShelfShareHelper.getLocations().get(i).containsKeySOS(String
                                                    .valueOf(mKey))) {
                                                hashMap = new HashMap<>();
                                                hashMap.put(ShelfShareHelper.BLOCK_COUNT,
                                                        detailCursor.getInt(11));
                                                hashMap.put(ShelfShareHelper.SHELF_COUNT,
                                                        detailCursor.getInt(12));
                                                hashMap.put(ShelfShareHelper.SHELF_LENGTH,
                                                        detailCursor.getString(13));
                                                hashMap.put(ShelfShareHelper.EXTRA_LENGTH,
                                                        detailCursor.getInt(14));
                                                hashMap.put(ShelfShareHelper.LOADING_FROM_DB,
                                                        true);
                                                mShelfShareHelper.getLocations().get(i).setShelfDetailForSOS(
                                                        mKey, hashMap);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        // Load SOD
                        else if (mModuleName.equalsIgnoreCase(moduleSOD)) {
                            for (SODBO msod : getSODList()) {
                                if (msod.getProductID() == detailCursor
                                        .getInt(0)) {
                                    for (int i = 0; i < msod.getLocations().size(); i++) {
                                        if (msod.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            msod.setNorm(detailCursor.getFloat(1));
                                            msod.getLocations().get(i).setParentTotal(SDUtil
                                                    .convertToFloat(detailCursor.getString(2)) + "");
                                            msod.getLocations().get(i).setTarget(detailCursor.getString(3));
                                            msod.getLocations().get(i).setActual(detailCursor.getString(4));
                                            msod.getLocations().get(i).setPercentage(detailCursor
                                                    .getString(5));
                                            msod.getLocations().get(i).setGap(detailCursor.getString(6));
                                            msod.getLocations().get(i).setReasonId(detailCursor.getInt(7));
                                            msod.getLocations().get(i).setImageName(detailCursor.getString(8));
                                            msod.getLocations().get(i).setImgName(detailCursor.getString(detailCursor.getColumnIndex("tempImageName")));
                                            msod.getLocations().get(i).setAudit(detailCursor.getInt(10));
                                            isDataAvailableSOD = true;
                                        }
                                    }
                                }
                            }

                            String mKey = detailCursor.getInt(15) + "";
                            for (SODBO msod : getSODList()) {
                                if (msod.getProductID() == detailCursor
                                        .getInt(0)) {
                                    for (int i = 0; i < msod.getLocations().size(); i++) {
                                        if (msod.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            if (!mShelfShareHelper.containsKeySOD(String
                                                    .valueOf(mKey))) {
                                                hashMap = new HashMap<>();
                                                hashMap.put(ShelfShareHelper.BLOCK_COUNT,
                                                        detailCursor.getInt(11));
                                                hashMap.put(ShelfShareHelper.SHELF_COUNT,
                                                        detailCursor.getInt(12));
                                                hashMap.put(ShelfShareHelper.SHELF_LENGTH,
                                                        detailCursor.getString(13));
                                                hashMap.put(ShelfShareHelper.EXTRA_LENGTH,
                                                        detailCursor.getInt(14));
                                                hashMap.put(ShelfShareHelper.LOADING_FROM_DB,
                                                        true);
                                                mShelfShareHelper.getLocations().get(i).setShelfDetailForSOD(
                                                        mKey, hashMap);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (mModuleName.equalsIgnoreCase(moduleSOSKU)) {
                            for (SOSKUBO soskuBO : getSOSKUList()) {
                                if (soskuBO.getProductID() == detailCursor
                                        .getInt(0)) {
                                    soskuBO.setNorm(detailCursor.getFloat(1));
                                    soskuBO.setParentTotal(detailCursor
                                            .getInt(2));
                                    soskuBO.setTarget(detailCursor.getString(3));
                                    soskuBO.setActual(detailCursor.getInt(4));
                                    soskuBO.setPercentage(detailCursor
                                            .getString(5));
                                    soskuBO.setGap(detailCursor.getString(6));
                                    soskuBO.setReasonId(detailCursor.getInt(7));
                                    soskuBO.setImageName(detailCursor
                                            .getString(8));
                                    soskuBO.setImgName(detailCursor.getString(detailCursor.getColumnIndex("tempImageName")));
                                    isDataAvailableSOSKU = true;
                                    break;
                                }
                            }
                        }

                    }
                }

                detailCursor.close();
            } else {
                if (mBModel.configurationMasterHelper.IS_SOS_RETAIN_LAST_VISIT_TRAN) {
                    //Loading Last visit transaction for SOS
                    if (mModuleName.equalsIgnoreCase(moduleSOS)) {

                        String query = "select LocId,pid,parentId,actualValue,reasonId,isOwn,ParentTotal from LastVisitSOS where retailerId=" + mBModel.QT(mBModel.getRetailerMasterBO().getRetailerID());

                        Cursor cursor = db.selectSQL(query);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {

                                for (SOSBO msos : getSOSList()) {
                                    if (msos.getProductID() == cursor
                                            .getInt(1)
                                            && msos.getIsOwn() == cursor
                                            .getInt(5)) {
                                        for (int i = 0; i < msos.getLocations().size(); i++) {
                                            if (msos.getLocations().get(i).getLocationId() == cursor.getInt(0)) {

                                                msos.getLocations().get(i).setActual(cursor.getString(3));
                                                msos.getLocations().get(i).setParentTotal(cursor.getString(6));
                                                msos.getLocations().get(i).setReasonId(cursor.getInt(4));


                                                if (SDUtil.convertToFloat(msos.getLocations().get(i).getParentTotal()) > 0) {

                                                    float mParentTotal = SDUtil
                                                            .convertToFloat(msos.getLocations().get(i).getParentTotal());
                                                    float mNorm = msos.getNorm();
                                                    float actual = SDUtil.convertToFloat(msos.getLocations().get(i).getActual());

                                                    float target = (mParentTotal * mNorm) / 100;
                                                    float gap = target - actual;
                                                    float percentage = 0;
                                                    if (mParentTotal > 0)
                                                        percentage = (actual / mParentTotal) * 100;

                                                    msos.getLocations().get(i).setTarget(SDUtil.roundIt(target, 2));
                                                    msos.getLocations().get(i).setPercentage(mBModel
                                                            .formatPercent(percentage));
                                                    msos.getLocations().get(i).setGap(SDUtil.roundIt(-gap, 2));
                                                } else {
                                                    msos.getLocations().get(i).setTarget(0 + "");
                                                    msos.getLocations().get(i).setPercentage(0 + "");
                                                    msos.getLocations().get(i).setGap(0 + "");
                                                }
                                                isDataAvailableSOS = true;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            headerCursor.close();
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("loadSavedTracking" + mModuleName + e);
        }

        if (mBModel.configurationMasterHelper.isAuditEnabled()) {
            if (mModuleName.equalsIgnoreCase(moduleSOS))
                return isDataAvailableSOS;
            else if (mModuleName.equalsIgnoreCase(moduleSOD))
                return isDataAvailableSOD;
            else if (mModuleName.equalsIgnoreCase(moduleSOSKU))
                return isDataAvailableSOSKU;
            else return false;
        } else return true;
    }

    /**
     * set Image Name in Each Objects
     *
     * @param mBrandID   Brand ID
     * @param mImageName Image Name
     */

    public void onSaveImageName(int mBrandID, String mImageName, String moduleName, int locationIndex) {
        String imagePath = mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;
        try {
            if (moduleName.equals(HomeScreenTwo.MENU_SOS)) {
                imagePath = "SOS/" + imagePath;
                for (int i = 0; i < getSOSList().size(); ++i) {
                    SOSBO sos = getSOSList().get(i);
                    if (sos.getProductID() == mBrandID) {
                        getSOSList().get(i).getLocations().get(locationIndex).setImageName(imagePath);
                        getSOSList().get(i).getLocations().get(locationIndex).setImgName(mImageName);
                        break;

                    }
                }
            } else if (moduleName.equals(HomeScreenTwo.MENU_SOD)) {
                imagePath = "SOD/" + imagePath;
                for (int i = 0; i < getSODList().size(); ++i) {
                    SODBO sod = getSODList().get(i);
                    if (sod.getProductID() == mBrandID) {
                        getSODList().get(i).getLocations().get(locationIndex).setImageName(imagePath);
                        getSODList().get(i).getLocations().get(locationIndex).setImgName(mImageName);
                        break;

                    }
                }
            } else if (moduleName.equals(HomeScreenTwo.MENU_SOSKU)) {
                imagePath = "SOSKU/" + imagePath;
                for (int i = 0; i < getSOSKUList().size(); ++i) {
                    SOSKUBO sosku = getSOSKUList().get(i);
                    if (sosku.getProductID() == mBrandID) {
                        getSOSKUList().get(i).setImageName(imagePath);
                        getSOSKUList().get(i).setImgName(mImageName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Check Whether the Module is Tracked or Not, Based on ParentTotal
     *
     * @param module Module Code
     * @return Is data available for given menu code
     */
    public boolean hasData(String module) {

        if (module.equalsIgnoreCase(moduleSOS)) {
            for (SOSBO levelbo : getSOSList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                        if((!levelbo.getLocations().get(i).getParentTotal().equals("0")
                                && levelbo.getLocations().get(i).getAudit() != 2)
                                || levelbo.getLocations().get(i).getAudit() != 2)
                            return true;
                    }
                    else if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
                }
            }
        } else if (module.equalsIgnoreCase(moduleSOD)) {
            for (SODBO levelbo : getSODList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                        if((!levelbo.getLocations().get(i).getParentTotal().equals("0")
                                && levelbo.getLocations().get(i).getAudit() != 2)
                                || levelbo.getLocations().get(i).getAudit() != 2)
                            return true;
                    }
                    else if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
                }
            }
        } else if (module.equalsIgnoreCase(moduleSOSKU)) {
            for (SOSKUBO soskuBO : getSOSKUList()) {
                if (soskuBO.getParentTotal() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<SODBO> getSODList() {
        if (mSODList == null)
            return new ArrayList<>();
        return mSODList;
    }

    public void setSODList(ArrayList<SODBO> mSODList) {
        this.mSODList = mSODList;
    }

    public ArrayList<SOSBO> getSOSList() {
        if (mSOSList == null)
            return new ArrayList<>();
        return mSOSList;
    }

    public void setSOSList(ArrayList<SOSBO> mSOSList) {
        this.mSOSList = mSOSList;
    }

    public ArrayList<SOSKUBO> getSOSKUList() {
        if (mSOSKUList == null)
            return new ArrayList<>();
        return mSOSKUList;
    }

    public void setSOSKUList(ArrayList<SOSKUBO> mSOSKUList) {
        this.mSOSKUList = mSOSKUList;
    }

    /**
     * Load and set Values to objects in Edit Mode
     *
     * @param uid         Transaction ID
     * @param pid         Product Id
     * @param mTotalShelf Total Shelf
     * @param mLocationId Location Id
     */
    public HashMap<String, ShelfShareBO> loadSODBlockDetails(String uid, String pid, int mTotalShelf, int mLocationId) {
        DBUtil db = null;
        HashMap<String, ShelfShareBO> mBrandsDetailsHashMap = new HashMap<>();

        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("select sb.SubCellId,sb.CellId,sb.ChildPid,PM.pname from SOD_Tracking_Block_Detail SB ");
            sb.append("left join productmaster pm on PM.pid=SB.ChildPid");
            sb.append(" WHERE sb.Uid=" + mBModel.QT(uid) + "and SB.PID="
                    + mBModel.QT(pid) + " and SB.locid=" + mLocationId);
            sb.append(" order by sb.SubCellId ,sb.CellId  ");

            Cursor detailCursor = db.selectSQL(sb.toString());

            if (detailCursor.getCount() > 0) {

                for (int i = 0; i < mTotalShelf; i++) {
                    ShelfShareBO shelfShareBO = new ShelfShareBO();

                    shelfShareBO.setFirstCell("empty");
                    shelfShareBO.setSecondCell("empty");
                    shelfShareBO.setThirdCell("empty");
                    shelfShareBO.setFourthCell("empty");
                    mBrandsDetailsHashMap.put(
                            String.valueOf(i), shelfShareBO);

                    while (detailCursor.moveToNext()) {
                        if (i == detailCursor.getInt(0)) {
                            if (detailCursor.getInt(2) != 0) {
                                if (detailCursor.getInt(1) == 1)
                                    shelfShareBO.setFirstCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 2)
                                    shelfShareBO.setSecondCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 3)
                                    shelfShareBO.setThirdCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                if (detailCursor.getInt(1) == 4)
                                    shelfShareBO.setFourthCell(((detailCursor
                                            .getString(3) == null) ? "Ext.Shelf" : detailCursor.getString(3)));
                                shelfShareBO.setOthersCount(1);
                                mBrandsDetailsHashMap.put(
                                        String.valueOf(i), shelfShareBO);
                            }
                        } else {
                            detailCursor.moveToPrevious();
                            break;
                        }
                    }

                }
            }
            detailCursor.close();

            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
        }

        return mBrandsDetailsHashMap;
    }

    public ArrayList<SOSBO> getLstSOS_PRJSpecific() {
        return lstSOSproj;
    }

    public void setLstSOS_PRJSpecific(ArrayList<SOSBO> lstSOSproj) {
        this.lstSOSproj = lstSOSproj;
    }

    /**
     * Download SOS record group wise
     *
     * @return List group wise
     */
    public ArrayList<SOSBO> downloadSOSGroups() {

        DBUtil db = null;
        try {
            downloadSOS_PRJSpecific_Attributes();

            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuilder str = new StringBuilder();
            str.append("select  DISTINCT SD.SOSgroupid,groupName,SD.pid,SD.plid,isown,inTarget," +
                    "Case  IFNULL(SAM.groupid,-1) when -1  then '0' else '1' END as flag ,SM.groupid,SAM.sosNormid,SM.target,PM.pname");
            str.append(" from  SOS_GroupHeader_Proj SH inner join SOS_GroupDetail_Proj SD ON SH.SOSgroupid=SD.SOSgroupid" +
                    " left join object1 PM on PM.pid=SD.pid" +
                    " inner join SOS_NormMapping_Proj SM ON SM.SOSgroupid==SD.SOSgroupid" +
                    " LEFT JOIN SOS_NormAttributeMapping_Proj SAM on SAM .GroupId= SM.GroupID and SAM.SOSNormID=SM.SOSNormID");
            str.append(" where SM.accid in(0," + mBModel.getRetailerMasterBO().getAccountid() + ")" +
                    "and SM.RetailerId in(0," + mBModel.getRetailerMasterBO().getRetailerID() + ")" +
                    "and SM.chid in(0," + mBModel.getRetailerMasterBO().getSubchannelid() + ")" +
                    "and SM.locid in(0," + mBModel.getRetailerMasterBO().getLocationId() + ")");
            str.append(" order by SD.sosGroupId,SAm.SOSNormid");

            Cursor c = db.selectSQL(str.toString());
            if (c.getCount() > 0) {
                SOSBO bo;
                lstSOSproj = new ArrayList<>();
                while (c.moveToNext()) {

                    if (c.getInt(6) == 0 || (c.getInt(6) == 1 && lstSOSProjAttributes != null
                            && lstSOSProjAttributes.contains(c.getString(7) + c.getString(8)))) {

                        bo = new SOSBO();
                        bo.setGroupId(c.getInt(0));
                        bo.setGroupName(c.getString(1));
                        bo.setProductID(c.getInt(2));
                        bo.setProductName(c.getString(10));
                        bo.setProductLevelId(c.getInt(3));
                        bo.setIsOwn(c.getInt(4));
                        bo.setInTarget(c.getInt(5));
                        bo.setGroupTarget(c.getDouble(9));
                        lstSOSproj.add(bo);
                    }


                }

            }

            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
        }
        return lstSOSproj;
    }

    /**
     * Download attributes
     */
    private void downloadSOS_PRJSpecific_Attributes() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            StringBuilder str = new StringBuilder();
            str.append("select Distinct groupid,sosNormid from SOS_NormAttributeMapping_Proj SAM");
            str.append(" where sosNormid not in(");
            str.append("select sosNormid from SOS_NormAttributeMapping_Proj where attributeid not in (" + mBModel.getRetailerAttributeList() + ")) and attributeid in(" + mBModel.getRetailerAttributeList() + ")");
            str.append("  order by groupId");

            Cursor c = db.selectSQL(str.toString());
            if (c.getCount() > 0) {
                lstSOSProjAttributes = new ArrayList<>();
                while (c.moveToNext()) {

                    lstSOSProjAttributes.add(c.getString(0) + c.getString(1));
                }

            }

            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
        }
    }

    /**
     * Save SOS project specific transactions
     */
    public void saveSOS_PRJSpecific_Transaction() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String query = "select Uid from  SOS_Tracking_Header where retailerid=" + mBModel.getAppDataProvider().getRetailMaster().getRetailerID() + " and date=" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    db.deleteSQL("SOS_Tracking_Header",
                            "uid=" + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                    db.deleteSQL("SOS_Tracking_Detail",
                            "uid=" + StringUtils.getStringQueryParam(cursor.getString(0)), false);
                }
            }

            String uid = (mBModel.getAppDataProvider().getUser().getUserid() + DateTimeUtils
                    .now(DateTimeUtils.DATE_TIME_ID));

            String detailColumns = "Uid,MappingId,pid,Actual,IsOwn,Flex1,Norm";
            String headerColumns = "Uid,RetailerId,Date,ridSF,VisitId";

            String values;
            boolean isData = false;
            for (SOSBO bo : getLstSOS_PRJSpecific()) {
                if (bo.getAvailability() > 0) {
                    values = uid + "," + bo.getGroupId() + "," + bo.getProductID() + "," + bo.getAvailability() + "," + bo.getIsOwn() + "," + bo.getInTarget() + "," + bo.getGroupTarget();
                    db.insertSQL("SOS_Tracking_Detail", detailColumns, values);
                    isData = true;
                }
            }

            if (isData) {
                db.insertSQL("SOS_Tracking_Header", headerColumns, uid + "," + mBModel.getAppDataProvider().getRetailMaster().getRetailerID() + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + "," + StringUtils.getStringQueryParam(mBModel.getAppDataProvider().getRetailMaster().getRidSF())
                            + "," + mBModel.getAppDataProvider().getUniqueId());
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }

    }

    /**
     * download transaction data
     */

    public void downloadSOSProjTransactions() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            StringBuilder str = new StringBuilder();
            str.append("select MappingId,pid,Actual,IsOwn from SOS_Tracking_Detail SD");
            str.append(" INNER JOIN SOS_Tracking_Header SH ON SH.uid=SD.uid");
            str.append(" WHERE SH.retailerid=" + mBModel.getRetailerMasterBO().getRetailerID() + " and SH.date=" + mBModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            str.append("  order by SD.MappingId");

            Cursor c = db.selectSQL(str.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    for (SOSBO bo : getLstSOS_PRJSpecific()) {
                        if (bo.getGroupId() == c.getInt(0) && bo.getProductID() == c.getInt(1) && bo.getIsOwn() == c.getInt(3)) {
                            bo.setAvailability(c.getInt(2));
                        }
                    }
                }

            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
        }
    }


    /**
     * Load data for SOS/SOD/SOSKU
     *
     * @param mMenuName Module Code
     */
    public void loadData(String mMenuName) {
        try {
            int level;
            level = getMappingLevels(mMenuName);

            if (mMenuName.equals("MENU_SOS") || mMenuName.equals("MENU_SOSKU") || mMenuName.equals("MENU_SOD")) {
                switch (level) {
                    case 1:
                        downloadSalesFundamental(mMenuName, true, false, false, 0, 0);
                        break;
                    case 2:
                        downloadSalesFundamental(mMenuName, false, true, false, 0, 0);
                        break;
                    case 3:
                        downloadSalesFundamental(mMenuName, false, false, true, 0, 0);
                        break;
                    case 4:
                        downloadSalesFundamental(mMenuName, false, false, false, mLocationId, 0);
                        break;
                    case 5:
                        downloadSalesFundamental(mMenuName, false, false, false, 0, mChannelId);
                        break;
                    case 6:
                        downloadSalesFundamental(mMenuName, false, false, false, mLocationId, mChannelId);
                        break;
                    case 8:
                        downloadSalesFundamental(mMenuName, true, false, false, 0, mChannelId);
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

    /**
     * Get mapping levels(Retailer/account wise) for given menu code
     *
     * @param mMenuCode Module Code
     * @return Mapping type
     */
    private int getMappingLevels(String mMenuCode) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select IsAccount,IsRetailer,IsClass,LocLevelId,ChLevelId from ConfigActivityFilter where ActivityCode=" + mBModel.QT(mMenuCode));
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 1 && c.getInt(4) > 0) {
                        mChannelId = c.getInt(4);
                        return 8;
                    } else if (c.getInt(0) == 1)
                        return 1;
                    else if (c.getInt(1) == 1)
                        return 2;
                    else if (c.getInt(2) == 1) {
                        return 3;
                    } else {
                        if (c.getInt(3) > 0 && c.getInt(4) > 0) {
                            mLocationId = c.getInt(3);
                            mChannelId = c.getInt(4);
                            return 6;
                        } else if (c.getInt(3) > 0) {
                            mLocationId = c.getInt(3);
                            return 4;
                        } else if (c.getInt(4) > 0) {
                            mChannelId = c.getInt(4);
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

    public ArrayList<SFLocationBO> getLocationList() {
        return mLocationList;
    }

    /**
     * Download In store Locations
     */
    public void downloadLocations() {
        try {

            mLocationList = new ArrayList<>();
            SFLocationBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL  where SL.Listtype='PL' ORDER BY SL.ListId";

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new SFLocationBO();
                    locations.setLocationId(c.getInt(0));
                    locations.setLocationName(c.getString(1));
                    mLocationList.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (mLocationList.size() == 0) {
                locations = new SFLocationBO();
                locations.setLocationId(0);
                locations.setLocationName("Store");
                mLocationList.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }

    public List<SOSBO> getmCategoryForDialogSOSBO() {
        return mCategoryForDialogSOSBO!=null?mCategoryForDialogSOSBO:new ArrayList<>();
    }

    public void setmCategoryForDialogSOSBO(List<SOSBO> mCategoryForDialogSOSBO) {
        this.mCategoryForDialogSOSBO = mCategoryForDialogSOSBO;
    }

    public List<SODBO> getmCategoryForDialogSODBO() {
        return mCategoryForDialogSODBO!=null?mCategoryForDialogSODBO:new ArrayList<>();
    }

    public void setmCategoryForDialogSODBO(List<SODBO> mCategoryForDialogSOSBO) {
        this.mCategoryForDialogSODBO = mCategoryForDialogSODBO;
    }
}
