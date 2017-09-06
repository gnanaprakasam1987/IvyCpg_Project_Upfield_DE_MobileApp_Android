package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
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
import com.ivyretail.views.SODDialogFragment;
import com.ivyretail.views.ShelfShareDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SalesFundamentalHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static SalesFundamentalHelper instance = null;
    private ArrayList<SOSBO> mSOSList;
    private ArrayList<SODBO> mSODList;
    private ArrayList<SOSKUBO> mSOSKUList;
    public int mSelectedBrandID = 0;
    private String moduleSOS = "MENU_SOS";
    private String moduleSOSKU = "MENU_SOSKU";
    private String moduleSOD = "MENU_SOD";

    private ArrayList<ShelfShareBO> mShelfShareList;

    private ArrayList<String> modules;
    private String mSOSLevelName;
    public int mSOSTotalPopUpType;

    protected SalesFundamentalHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static SalesFundamentalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SalesFundamentalHelper(context);
        }
        return instance;
    }

    /**
     * Load TotalPopUPConfiguration from HhtModuleMaster HHTCode :
     * moduleName+"01" - For Products in Total PopUp HHTCode : moduleName+"02" -
     * For Total PopUp Type
     */
    public void setTotalPopUpConfig() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT COUNT(HHTCode) FROM HhtModuleMaster"
                    + " WHERE HHTCode='VISUALSF' AND Flag='1'");
            if (c != null) {
                if (c.moveToNext() && c.getInt(0) > 0) {
                    mSOSTotalPopUpType = 1;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    private Vector<LevelBO> mSFModuleSequence;
    private Vector<LevelBO> mFilterLevel;
    private HashMap<Integer, Vector<LevelBO>> mFilterLevelBo;

    public HashMap<Integer, Vector<LevelBO>> getFiveLevelFilters() {
        return mFilterLevelBo;
    }

    public Vector<LevelBO> getSequenceValues() {
        return mSFModuleSequence;
    }

    public void downloadSFFiveLevelFilter(String moduleName) {

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
        mSFModuleSequence = new Vector<>();
        while (listCursor.moveToNext()) {

            mLevelBO = new LevelBO();
            mLevelBO.setProductID(listCursor.getInt(0));
            mLevelBO.setLevelName(listCursor.getString(1));
            mLevelBO.setSequence(listCursor.getInt(2));

            mSFModuleSequence.add(mLevelBO);
        }

        listCursor.close();

        mFilterLevelBo = new HashMap<>();
        try {

            if (mSFModuleSequence.size() > 0) {
                loadParentFilter(mSFModuleSequence.get(0).getProductID());
                for (int i = 1; i < mSFModuleSequence.size(); i++) {
                    loadChildFilter(mSFModuleSequence.get(i).getSequence(),
                            mSFModuleSequence.get(i - 1).getSequence(),
                            mSFModuleSequence.get(i).getProductID(),
                            mSFModuleSequence.get(i - 1).getProductID());
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadParentFilter(int mProductLevelId) {

        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mProductLevelId - bmodel.productHelper.getmSelectedGLobalLevelID() + 1;

            query = "SELECT DISTINCT PM" + filterGap + ".PID, PM" + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + bmodel.productHelper.getmSelectedGLobalLevelID() + " and PM1.PID =" + bmodel.productHelper.getmSelectedGlobalProductId();

        } else {
            query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1"
                    + " WHERE PM1.PLid = " + mProductLevelId + " Order By PM1.RowId";
        }

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        Cursor c = db.selectSQL(query);

        if (c != null) {
            mFilterLevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setProductID(c.getInt(0));
                mLevelBO.setLevelName(c.getString(1));
                mFilterLevel.add(mLevelBO);
            }

            mFilterLevelBo.put(mProductLevelId, mFilterLevel);

            c.close();
            db.close();
        }
    }

    public void loadChildFilter(int mChildLevel, int mParentLevel,
                                int mProductLevelId, int mParentLevelId) {

        String query;
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mChildLevel - bmodel.configurationMasterHelper.globalSeqId + 1;
            int PM1Level = mParentLevel - bmodel.configurationMasterHelper.globalSeqId + 1;

            query = "SELECT DISTINCT PM" + PM1Level + ".PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + bmodel.productHelper.getmSelectedGLobalLevelID() + " AND PM1.PID = " + bmodel.productHelper.getmSelectedGlobalProductId();

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
            mFilterLevel = new Vector<>();
            while (c.moveToNext()) {
                LevelBO mLevelBO = new LevelBO();
                mLevelBO.setParentID(c.getInt(0));
                mLevelBO.setProductID(c.getInt(1));
                mLevelBO.setLevelName(c.getString(2));
                mFilterLevel.add(mLevelBO);
            }

            mFilterLevelBo.put(mProductLevelId, mFilterLevel);

            c.close();
            db.close();
        }
    }

    public void downloadSalesFundamental(String moduleName, boolean IsAccount, boolean IsRetailer, boolean IsClass, int LocId, int ChId) {
        DBUtil db = null;
        try {
            Cursor cursor = null;
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            int mParentLevel = 0;
            int mChildLevel = 0;
            int mContentLevel = 0;
            int mParentLevelId = 0, mChildLevelId = 0;
            int mFirstLevel;
            int loopEnd = 0;
            String query = "";

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {

                if (mSFModuleSequence != null) {
                    if (mSFModuleSequence.size() > 0) {
                        mChildLevel = mSFModuleSequence.size();
                    }
                }

                if (mChildLevel == 0)
                    mChildLevel = 1;

                Cursor filterCur = db
                        .selectSQL("SELECT IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter"
                                + mChildLevel
                                + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT(moduleName));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mParentLevel = filterCur.getInt(0);
                        mContentLevel = filterCur.getInt(1);
                    }
                    filterCur.close();
                }

                if (mParentLevel == 0)
                    loopEnd = mContentLevel - bmodel.configurationMasterHelper.globalSeqId + 1;
                else
                    loopEnd = mContentLevel - mParentLevel + 1;

                if (!mSFModuleSequence.isEmpty())
                    mFirstLevel = mSFModuleSequence.get(mSFModuleSequence.size() - 1).getProductID();
                else
                    mFirstLevel = bmodel.configurationMasterHelper.globalSeqId;

            } else {
                Cursor filterCur = db
                        .selectSQL("SELECT  IFNULL(PL1.Sequence,0),IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0), PL1.LevelId, PL2.LevelId"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                                + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT(moduleName));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mParentLevel = filterCur.getInt(0);
                        mChildLevel = filterCur.getInt(1);
                        mContentLevel = filterCur.getInt(2);
                        mParentLevelId = filterCur.getInt(3);
                        mChildLevelId = filterCur.getInt(4);
                    }
                    filterCur.close();
                }

                if (mChildLevel > 0) {
                    loopEnd = mContentLevel - mChildLevel + 1;
                    mFirstLevel = mChildLevelId;
                } else {
                    loopEnd = mContentLevel - mParentLevel + 1;
                    mFirstLevel = mParentLevelId;
                }
            }


            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("SELECT DISTINCT A1.Pid,A" + loopEnd + ".pid,");
            sBuffer.append("A" + loopEnd + ".pname ,1 isOwn,");
            sBuffer.append("IFNULL(SFN.Norm,0) as Norm,SFN.MappingId FROM ProductMaster A1");

            for (int i = 2; i <= loopEnd; i++) {

                query = query + " INNER JOIN ProductMaster A" + i + " ON A" + i
                        + ".ParentId = A" + (i - 1) + ".PID";
            }
            sBuffer.append(query);

            sBuffer.append(" LEFT JOIN "
                    + moduleName.replace("MENU_", "") + "_NormMapping  SFN ON A" + loopEnd
                    + ".pid = SFN.pid  ");

            if (IsRetailer) {
                sBuffer.append("and SFN.RetailerId =");
                sBuffer.append(bmodel.getRetailerMasterBO().getRetailerID());
            }
            if (IsAccount) {
                sBuffer.append(" and SFN.AccId=" + bmodel.getRetailerMasterBO().getAccountid());
            }
            if (IsClass) {
                sBuffer.append(" and SFN.ClassId=" + bmodel.getRetailerMasterBO().getClassid());
            }

            if (LocId > 0)
                sBuffer.append(" and SFN.LocId=" + bmodel.productHelper.getMappingLocationId(LocId, bmodel.getRetailerMasterBO().getLocationId()));
            if (ChId > 0)
                sBuffer.append(" and SFN.ChId=" + bmodel.productHelper.getMappingChannelId(ChId, bmodel.getRetailerMasterBO().getSubchannelid()));

            sBuffer.append(" LEFT JOIN " + moduleName.replace("MENU_", "") + "_NormMaster   SF ON SF.HId = SFN.HId");
            sBuffer.append(" AND " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " BETWEEN SF.StartDate AND SF.EndDate");
            sBuffer.append(" WHERE A1.PLID IN (" + mFirstLevel + ")");
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                sBuffer.append(" and A1.PID =" + bmodel.productHelper.getmSelectedGlobalProductId());

            if (moduleName.equals(moduleSOS)) {
                SOSBO mSOS;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setmSOSList(new ArrayList<SOSBO>());
                    while (cursor.moveToNext()) {
                        mSOS = new SOSBO();
                        mSOS.setParentID(cursor.getInt(0));
                        mSOS.setProductID(cursor.getInt(1));
                        mSOS.setProductName(cursor.getString(2));
                        mSOS.setIsOwn(cursor.getInt(3));
                        mSOS.setNorm(cursor.getFloat(4));
                        mSOS.setMappingId(cursor.getInt(5));
                        mSOS.setLocations(bmodel.productHelper.cloneLocationList(bmodel.productHelper.locations));
                        getmSOSList().add(mSOS);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOS);
            } else if (moduleName.equals(moduleSOD)) {
                SODBO mSOD;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setmSODList(new ArrayList<SODBO>());
                    while (cursor.moveToNext()) {
                        mSOD = new SODBO();
                        mSOD.setParentID(cursor.getInt(0));
                        mSOD.setProductID(cursor.getInt(1));
                        mSOD.setProductName(cursor.getString(2));
                        mSOD.setIsOwn(cursor.getInt(3));
                        mSOD.setNorm(cursor.getFloat(4));
                        mSOD.setMappingId(cursor.getInt(5));
                        mSOD.setLocations(bmodel.productHelper.cloneLocationList(bmodel.productHelper.locations));
                        getmSODList().add(mSOD);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOD);
            } else if (moduleName.equals(moduleSOSKU)) {
                SOSKUBO mSOSKU;
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {
                    setmSOSKUList(new ArrayList<SOSKUBO>());
                    while (cursor.moveToNext()) {
                        mSOSKU = new SOSKUBO();
                        mSOSKU.setParentID(cursor.getInt(0));
                        mSOSKU.setProductID(cursor.getInt(1));
                        mSOSKU.setProductName(cursor.getString(2));
                        mSOSKU.setIsOwn(cursor.getInt(3));
                        mSOSKU.setNorm(cursor.getFloat(4));
                        mSOSKU.setMappingId(cursor.getInt(5));
                        getmSOSKUList().add(mSOSKU);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSOSKU);
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(moduleName, e);
        }
    }

    /**
     * Load Competitors
     *
     * @param moduleName
     */
    public void loadCompetitors(String moduleName) {
        DBUtil db = null;
        ArrayList<Integer> lstCompetitiorPids;
        try {
            Cursor cursor = null;
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sBuffer = new StringBuffer();
            sBuffer.append("SELECT DISTINCT PM.pid ,CP.CPID, CP.CPName,0,0 FROM CompetitorProductMaster CP");
            sBuffer.append(" INNER JOIN CompetitorMappingMaster CM ON CM.CPid = CP.CPId");
            sBuffer.append(" INNER JOIN ProductMaster PM ON PM.PID = CM.PID");
            sBuffer.append(" WHERE  CP.Plid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + bmodel.QT(moduleName) + ")");

            cursor = db.selectSQL(sBuffer.toString());
            if (moduleName.equals(moduleSOS)) {
                if (cursor != null) {
                    lstCompetitiorPids = new ArrayList<>();
                    while (cursor.moveToNext()) {

                        if (!lstCompetitiorPids.contains(cursor.getInt(1))) {
                            for (SOSBO prodBO : getmSOSList()) {

                                if (prodBO.getProductID() == cursor.getInt(0)) {

                                    SOSBO comLevel = new SOSBO();
                                    comLevel.setParentID(prodBO.getParentID());
                                    comLevel.setProductID(cursor.getInt(1));
                                    comLevel.setProductName(cursor.getString(2));
                                    comLevel.setIsOwn(cursor.getInt(3));
                                    comLevel.setNorm(cursor.getInt(4));
                                    comLevel.setLocations(bmodel.productHelper.cloneLocationList(bmodel.productHelper.locations));
                                    lstCompetitiorPids.add(cursor.getInt(1));
                                    getmSOSList().add(comLevel);
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
                        for (SODBO prodBO : getmSODList()) {

                            if (prodBO.getProductID() == cursor.getInt(0)) {

                                SOSBO comLevel = new SOSBO();
                                comLevel.setParentID(prodBO.getParentID());
                                comLevel.setProductID(cursor.getInt(1));
                                comLevel.setProductName(cursor.getString(2));
                                comLevel.setIsOwn(cursor.getInt(3));
                                comLevel.setNorm(cursor.getInt(4));

                                getmSOSList().add(comLevel);
                                break;
                            }

                        }
                    }
                    cursor.close();
                }
            } else if (moduleName.equals(moduleSOSKU)) {
                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        for (SOSKUBO prodBO : getmSOSKUList()) {

                            if (prodBO.getProductID() == cursor.getInt(0)) {

                                SOSBO comLevel = new SOSBO();
                                comLevel.setParentID(prodBO.getParentID());
                                comLevel.setProductID(cursor.getInt(1));
                                comLevel.setProductName(cursor.getString(2));
                                comLevel.setIsOwn(cursor.getInt(3));
                                comLevel.setNorm(cursor.getInt(4));

                                getmSOSList().add(comLevel);
                                break;
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
     * @param moduleName
     * @return
     */
    public boolean saveSalesFundamentalDetails(String moduleName) {
        String modName = moduleName.replaceAll("MENU_", "");
        int count = 1;
        try {
            String refId = "0";
            String headerValues;
            String detailValues;
            String headerColumns = "Uid,RetailerId,Date,Remark,refid";
            String detailColumns = "Uid,Pid,RetailerId,Norm,ParentTotal,Required,Actual,Percentage,Gap,ReasonId,ImageName,IsOwn,ParentID,Isdone,MappingId,LocId";

            String mParentDetailColumns = "Uid, PId, BlockCount, ShelfCount,"
                    + " ShelfLength, ExtraShelf,total,RetailerId,LocId";
            String mParentDetailValues;

            String mBlockDetailColumns = "Uid,PId,ChildPid,SubCellId,CellId,Retailerid,LocId";
            String mBlockDetailValues;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String uid = (bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                    .now(SDUtil.DATE_TIME_ID));

            String query = "select Uid,refid from " + modName
                    + "_Tracking_Header  where RetailerId="
                    + bmodel.QT(bmodel.retailerMasterBO.getRetailerID());
            query += " and (upload='N' OR refid!=0)";

            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(modName + "_Tracking_Header",
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);
                db.deleteSQL(modName + "_Tracking_Detail",
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);
                if (modName.equals("SOS")) {
                    db.deleteSQL(modName + "_Tracking_Parent_Detail", "Uid="
                            + bmodel.QT(cursor.getString(0)), false);
                    db.deleteSQL(DataMembers.tbl_SOS__Block_Tracking_Detail,
                            "Uid=" + bmodel.QT(cursor.getString(0)), false);
                }
                refId = cursor.getString(1);
                // uid = cursor.getString(0);
            }
            cursor.close();
            // Inserting Header in Tables

            headerValues = bmodel.QT(uid)
                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                    + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + "," + bmodel.QT(bmodel.getNote())
                    + "," + bmodel.QT(refId);

            db.insertSQL(modName + "_Tracking_Header", headerColumns,
                    headerValues.toString());

            if (modName.equals("SOS")) {
                count = 1;
                int locid1 = 0;
                String tempkey = "";
                String mKey1 = "";
                for (SOSBO sosBo : getmSOSList()) {
                    for (int i = 0; i < sosBo.getLocations().size(); i++) {
                        if ((!sosBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sosBo.getLocations().get(i).getParentTotal().equals("0.0"))
                                || sosBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = bmodel.QT(uid)
                                    + "," + sosBo.getProductID()
                                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + sosBo.getNorm()
                                    + "," + sosBo.getLocations().get(i).getParentTotal()
                                    + "," + sosBo.getLocations().get(i).getTarget()
                                    + "," + sosBo.getLocations().get(i).getActual()
                                    + "," + sosBo.getLocations().get(i).getPercentage()
                                    + "," + sosBo.getLocations().get(i).getGap()
                                    + "," + sosBo.getLocations().get(i).getReasonId()
                                    + "," + bmodel.QT(sosBo.getLocations().get(i).getImageName())
                                    + "," + sosBo.getIsOwn()
                                    + "," + sosBo.getParentID()
                                    + "," + sosBo.getLocations().get(i).getAudit()
                                    + "," + sosBo.getMappingId()
                                    + "," + sosBo.getLocations().get(i).getLocationId();

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues.toString());

                            // For share shelf detail
                            HashMap<String, Object> hashMap = null;
                            mKey1 = sosBo.getParentID() + "";
                            locid1 = sosBo.getLocations().get(i).getLocationId();

                            if (bmodel.mShelfShareHelper.getLocations().get(i).getShelfDetailForSOS()
                                    .containsKey(mKey1))
                                hashMap = bmodel.mShelfShareHelper.getLocations().get(i)
                                        .getShelfDetailForSOS().get(mKey1);

                            if (hashMap != null) {
                                mParentDetailValues = bmodel.QT(uid)
                                        + "," + sosBo.getParentID()
                                        + "," + hashMap.get(ShelfShareHelper.BLOCK_COUNT)
                                        + "," + hashMap.get(ShelfShareHelper.SHELF_COUNT)
                                        + "," + hashMap.get(ShelfShareHelper.SHELF_LENGTH)
                                        + "," + hashMap.get(ShelfShareHelper.EXTRA_LENGTH)
                                        + "," + sosBo.getLocations().get(i).getParentTotal()
                                        + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
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
                                            + bmodel.QT(uid) + " AND PId = "
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
                                mParentDetailValues = bmodel.QT(uid)
                                        + "," + sosBo.getParentID()
                                        + "," + 0
                                        + "," + 0
                                        + "," + 0
                                        + "," + 0
                                        + "," + sosBo.getLocations().get(i).getParentTotal()
                                        + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
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
                                            + bmodel.QT(uid) + " AND PId = "
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
                String mKey = ";";
                count = 1;
                for (SOSBO sosBo1 : getmSOSList()) {
                    for (int i = 0; i < sosBo1.getLocations().size(); i++) {
                        if (!sosBo1.getLocations().get(i).getParentTotal().equals("0")
                                && !sosBo1.getLocations().get(i).getParentTotal().equals("0.0")) {
                            // For share shelf detail
                            HashMap<String, ShelfShareBO> blockHashMap = null;
                            mKey = sosBo1.getParentID() + "";
                            if (mKeytemp.equals(""))
                                mKeytemp = mKey;
                            blockHashMap = bmodel.mShelfShareHelper.getLocations().get(i)
                                    .getmShelfBlockDetailForSOS().get(mKey);
                            if (blockHashMap != null) {
                                for (String key : blockHashMap.keySet()) {
                                    ShelfShareBO ssBO = blockHashMap.get(key);
                                    if (ssBO.getFirstCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + "," + sosBo1.getParentID()
                                                + "," + sosBo1.getProductID()
                                                + "," + key
                                                + "," + "1"
                                                + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                                                + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equals(
                                            sosBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + sosBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }

                                    // if you uncomment below code, it will throw
                                    // java.util.ConcurrentModificationException
                                    // studentGrades.remove("Alan");


                                    /**Saving Empty Block Details **/
//Log.v("Test1",count +" "+ ssBO.getFirstCell().equalsIgnoreCase("Ext.Shelf"));
                                    //if (count > 0) {

                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }


                                    /**saving Extra shelf details **/
                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","
                                                + sosBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sosBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                DataMembers.tbl_SOS__Block_Tracking_Detail,
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
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
                int locid = 0;
                for (SODBO sodBo : getmSODList()) {
                    for (int i = 0; i < sodBo.getLocations().size(); i++) {
                        if (!sodBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sodBo.getLocations().get(i).getParentTotal().equals("0.0") || sodBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = bmodel.QT(uid) + ","
                                    + sodBo.getProductID() + ","
                                    + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "," + sodBo.getNorm() + ","
                                    + sodBo.getLocations().get(i).getParentTotal() + ","
                                    + sodBo.getLocations().get(i).getTarget() + "," + sodBo.getLocations().get(i).getActual()
                                    + "," + sodBo.getLocations().get(i).getPercentage() + ","
                                    + sodBo.getLocations().get(i).getGap() + "," + sodBo.getLocations().get(i).getReasonId()
                                    + "," + bmodel.QT(sodBo.getLocations().get(i).getImageName()) + ","
                                    + sodBo.getIsOwn() + "," + sodBo.getParentID() + "," + sodBo.getLocations().get(i).getAudit() + "," + sodBo.getMappingId() + "," + sodBo.getLocations().get(i).getLocationId();

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues.toString());

                            // For share shelf detail
                            HashMap<String, Object> hashMap = null;
                            mKey = sodBo.getParentID() + "";
                            if (tempkey.equals(""))
                                tempkey = mKey;
                            locid = sodBo.getLocations().get(i).getLocationId();
                            if (bmodel.mShelfShareHelper.getLocations().get(i).getShelfDetailForSOD()
                                    .containsKey(mKey))
                                hashMap = bmodel.mShelfShareHelper.getLocations().get(i)
                                        .getShelfDetailForSOD().get(mKey);

                            if (hashMap != null) {
                                mParentDetailValues = bmodel.QT(uid)
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
                                        + bmodel.QT(bmodel.getRetailerMasterBO()
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
                                            + bmodel.QT(uid) + " AND PId = "
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
                                mParentDetailValues = bmodel.QT(uid)
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
                                        + bmodel.QT(bmodel.getRetailerMasterBO()
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
                                            + bmodel.QT(uid) + " AND PId = "
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

                String Key = ";";
                String mKeytemp = "";
                count = 1;
                for (SODBO sodBo1 : getmSODList()) {
                    for (int i = 0; i < sodBo1.getLocations().size(); i++) {
                        if (!sodBo1.getLocations().get(i).getParentTotal().equals("0")
                                && !sodBo1.getLocations().get(i).getParentTotal().equals("0.0")) {
                            // For share shelf detail
                            HashMap<String, ShelfShareBO> blockHashMap = null;
                            Key = sodBo1.getParentID() + "";
                            if (mKeytemp.equals(""))
                                mKeytemp = Key;
                            blockHashMap = bmodel.mShelfShareHelper.getLocations().get(i)
                                    .getmShelfBlockDetailForSOD().get(Key);

                            if (blockHashMap != null) {
                                Commons.print("key set" + blockHashMap.keySet().toString());
                                for (String key : blockHashMap.keySet()) {
                                    ShelfShareBO ssBO = blockHashMap.get(key);
                                    if (ssBO.getFirstCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equals(
                                            sodBo1.getProductName())) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + sodBo1.getProductID()
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }

                                    // if you uncomment below code, it will throw
                                    // java.util.ConcurrentModificationException
                                    // studentGrades.remove("Alan");


                                    /**Extra Block Details **/
                                    //Log.v("Test1",count +" "+ ssBO.getFirstCell().equalsIgnoreCase("Ext.Shelf"));
                                    //if (count > 0) {

                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "Ext.Shelf")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + -1
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFirstCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "1"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getSecondCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "2"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getThirdCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "3"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }
                                    if (ssBO.getFourthCell().equalsIgnoreCase(
                                            "empty")) {
                                        mBlockDetailValues = bmodel.QT(uid)
                                                + ","

                                                + sodBo1.getParentID()
                                                + ","
                                                + 0
                                                + ","
                                                + key
                                                + ","
                                                + "4"
                                                + ","
                                                + bmodel.QT(bmodel
                                                .getRetailerMasterBO()
                                                .getRetailerID()) + "," + sodBo1.getLocations().get(i).getLocationId();

                                        db.insertSQL(
                                                "SOD_Tracking_Block_Detail",
                                                mBlockDetailColumns,
                                                mBlockDetailValues.toString());
                                    }

                                    //}
                                }

                            }
                        }
                    }

                    /*if (Key.equals(mKeytemp)) {
                        count = 0;
                    } else {
                        mKeytemp = mKey;
                        count = 1;
                    }*/
                }
            } else if (modName.equals("SOSKU")) {
                for (SOSKUBO soskuBO : getmSOSKUList()) {
                    if (soskuBO.getParentTotal() > 0) {
                        detailValues = bmodel.QT(uid) + ","
                                + soskuBO.getProductID() + ","
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "," + soskuBO.getNorm() + ","
                                + soskuBO.getParentTotal() + ","
                                + soskuBO.getTarget() + ","
                                + soskuBO.getActual() + ","
                                + soskuBO.getPercentage() + ","
                                + soskuBO.getGap() + ","
                                + soskuBO.getReasonId() + ","
                                + bmodel.QT(soskuBO.getImageName()) + ","
                                + soskuBO.getIsOwn() + ","
                                + soskuBO.getParentID() + ","
                                + 0 + ","
                                + soskuBO.getMappingId() + ","
                                + 0;

                        db.insertSQL(modName + "_Tracking_Detail",
                                detailColumns, detailValues.toString());
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
     * @param
     */
    public void loadSOSBlockDetails(String uid, String pid, int totalShelfs, int locid) {
        DBUtil db = null;
        // ShelfShareDialogFragment.mBrandsDetailsHashMap = new HashMap<String,
        // ShelfShareBO>();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("select sb.SubCellId,sb.CellId,sb.ChildPid,PM.pname from SOS_Tracking_Block_Detail SB ");
            sb.append("left join productmaster pm on PM.pid=SB.ChildPid");
            sb.append(" WHERE sb.Uid=" + bmodel.QT(uid) + "and SB.PID="
                    + bmodel.QT(pid) + " and SB.LocId=" + locid);
            sb.append(" order by sb.SubCellId ,sb.CellId  ");

            Cursor detailCursor = db.selectSQL(sb.toString());

            if (detailCursor.getCount() > 0) {

                for (int i = 0; i < totalShelfs; i++) {
                    ShelfShareBO shelfShareBO = new ShelfShareBO();

                    shelfShareBO.setFirstCell("empty");
                    shelfShareBO.setSecondCell("empty");
                    shelfShareBO.setThirdCell("empty");
                    shelfShareBO.setFourthCell("empty");
                    ShelfShareDialogFragment.mBrandsDetailsHashMap.put(
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
                                ShelfShareDialogFragment.mBrandsDetailsHashMap.put(
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
            db.closeDB();
        }
    }

    /**
     * Load and set Values to objects in Edit Mode
     *
     * @param modName
     */
    public void loadSavedTracking(String modName) {
        DBUtil db = null;
        String sql;
        String uid = "";
        String moduleName = modName.replaceAll("MENU_", "");
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            bmodel.setNote("");
            sql = "SELECT Uid,Remark FROM " + moduleName + "_Tracking_Header"
                    + " WHERE RetailerId="
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " and (upload='N' OR refid!=0)";
            /*
             * + " AND Date = " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
			 */

            Cursor headerCursor = db.selectSQL(sql);
            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                uid = headerCursor.getString(0);
                // Set Remark in Object
                bmodel.setNote(headerCursor.getString(1));

                DataMembers.uidSOS = uid;
                DataMembers.uidSOD = uid;
                StringBuffer sb = new StringBuffer();

                sb.append("SELECT SF.Pid,SF.Norm,SF.ParentTotal,SF.Required,SF.Actual,SF.Percentage,SF.Gap,SF.ReasonId,SF.ImageName,SF.Isown,SF.Isdone");
                if (moduleName.equals("SOS") || moduleName.equals("SOD")) {
                    sb.append(", IFNULL(B.BlockCount,'0'), IFNULL(B.ShelfCount,'0'),IFNULL(B.ShelfLength,'0'), IFNULL(B.ExtraShelf,'0'),SF.Parentid,SF.locid");
                }
                sb.append(" From " + moduleName + "_Tracking_Detail SF");
                {
                    sb.append(" LEFT JOIN " + moduleName
                            + "_Tracking_Parent_Detail B ON");
                    sb.append(" B.Uid = SF.Uid AND B.PId = SF.Parentid and SF.locid=B.locid");
                }
                sb.append(" WHERE SF.Uid=" + bmodel.QT(uid));

                // For shelf share data
                HashMap<String, Object> hashMap;

                Cursor detailCursor = db.selectSQL(sb.toString());

                if (detailCursor.getCount() > 0) {

                    while (detailCursor.moveToNext()) {
                        if (modName.equalsIgnoreCase(moduleSOS)) {
                            for (SOSBO msos : getmSOSList()) {
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
                                            msos.getLocations().get(i).setAudit(detailCursor.getInt(10));

                                        }
                                    }
                                }
                            }

                            String mKey = detailCursor.getInt(15) + "";
                            for (SOSBO msos : getmSOSList()) {
                                if (msos.getProductID() == detailCursor
                                        .getInt(0)
                                        && msos.getIsOwn() == detailCursor
                                        .getInt(9)) {
                                    for (int i = 0; i < msos.getLocations().size(); i++) {
                                        if (msos.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            if (!bmodel.mShelfShareHelper.getLocations().get(i).containsKeySOS(String
                                                    .valueOf(mKey))) {
                                                hashMap = new HashMap<String, Object>();
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
                                                bmodel.mShelfShareHelper.getLocations().get(i).setShelfDetailForSOS(
                                                        mKey, hashMap);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        // Load SOD
                        else if (modName.equalsIgnoreCase(moduleSOD)) {
                            for (SODBO msod : getmSODList()) {
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

                                        }
                                    }
                                }
                            }

                            String mKey = detailCursor.getInt(15) + "";
                            for (SODBO msod : getmSODList()) {
                                if (msod.getProductID() == detailCursor
                                        .getInt(0)) {
                                    for (int i = 0; i < msod.getLocations().size(); i++) {
                                        if (msod.getLocations().get(i).getLocationId() == detailCursor.getInt(16)) {
                                            if (!bmodel.mShelfShareHelper.containsKeySOD(String
                                                    .valueOf(mKey))) {
                                                hashMap = new HashMap<String, Object>();
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
                                                bmodel.mShelfShareHelper.getLocations().get(i).setShelfDetailForSOD(
                                                        mKey, hashMap);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (modName.equalsIgnoreCase(moduleSOSKU)) {
                            for (SOSKUBO soskuBO : getmSOSKUList()) {
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
                                    break;
                                }
                            }
                        }

                    }
                }

                detailCursor.close();
            } else {
                if (bmodel.configurationMasterHelper.IS_SOS_RETAIN_LAST_VISIT_TRAN) {
                    //Loading Last visit transaction for SOS
                    if (modName.equalsIgnoreCase(moduleSOS)) {

                        String query = "select LocId,pid,parentId,actualValue,reasonId,isOwn,ParentTotal from LastVisitSOS where retailerId=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());

                        Cursor cursor = db.selectSQL(query);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {

                                for (SOSBO msos : getmSOSList()) {
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
                                                    msos.getLocations().get(i).setPercentage(bmodel
                                                            .formatPercent(percentage));
                                                    msos.getLocations().get(i).setGap(SDUtil.roundIt(-gap, 2));
                                                } else {
                                                    msos.getLocations().get(i).setTarget(0 + "");
                                                    msos.getLocations().get(i).setPercentage(0 + "");
                                                    msos.getLocations().get(i).setGap(0 + "");
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
            headerCursor.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("loadSavedTracking" + modName + e);
        }
    }

    /**
     * set Image Name in Each Objects
     *
     * @param mBrandID
     * @param imgName
     */

    public void onsaveImageName(int mBrandID, String imgName, String moduleName, int locationIndex) {
        try {
            if (moduleName.equals(HomeScreenTwo.MENU_SOS)) {
                for (int i = 0; i < getmSOSList().size(); ++i) {
                    SOSBO sos = (SOSBO) getmSOSList().get(i);
                    if (sos.getProductID() == mBrandID) {
                        getmSOSList().get(i).getLocations().get(locationIndex).setImageName(imgName);
                        break;

                    }
                }
            } else if (moduleName.equals(HomeScreenTwo.MENU_SOD)) {
                for (int i = 0; i < getmSODList().size(); ++i) {
                    SODBO sod = getmSODList().get(i);
                    if (sod.getProductID() == mBrandID) {
                        getmSODList().get(i).getLocations().get(locationIndex).setImageName(imgName);
                        break;

                    }
                }
            } else if (moduleName.equals(HomeScreenTwo.MENU_SOSKU)) {
                for (int i = 0; i < getmSOSKUList().size(); ++i) {
                    SOSKUBO sosku = (SOSKUBO) getmSOSKUList().get(i);
                    if (sosku.getProductID() == mBrandID) {
                        sosku.setImageName(imgName);
                        getmSOSKUList().get(i).setImageName(imgName);
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
     * @param module
     * @return
     */
    public boolean hasData(String module) {

        if (module.equalsIgnoreCase(moduleSOS)) {
            for (SOSBO levelbo : getmSOSList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
                }
            }
        } else if (module.equalsIgnoreCase(moduleSOD)) {
            for (SODBO levelbo : getmSODList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
                }
            }
        } else if (module.equalsIgnoreCase(moduleSOSKU)) {
            for (SOSKUBO soskuBO : getmSOSKUList()) {
                if (soskuBO.getParentTotal() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSOSLevelName() {
        return mSOSLevelName;
    }

    public void setSOSLevelName(String mSOSLevelName) {
        this.mSOSLevelName = mSOSLevelName;
    }

    public ArrayList<SODBO> getmSODList() {
        if (mSODList == null)
            return new ArrayList<SODBO>();
        return mSODList;
    }

    public void setmSODList(ArrayList<SODBO> mSODList) {
        this.mSODList = mSODList;
    }

    public ArrayList<SOSBO> getmSOSList() {
        return mSOSList;
    }

    public void setmSOSList(ArrayList<SOSBO> mSOSList) {
        this.mSOSList = mSOSList;
    }

    public ArrayList<SOSKUBO> getmSOSKUList() {
        if (mSOSKUList == null)
            return new ArrayList<SOSKUBO>();
        return mSOSKUList;
    }

    public ArrayList<ShelfShareBO> getmShelfShareList() {
        return mShelfShareList;
    }

    public void setmShelfShareList(ArrayList<ShelfShareBO> mShelfShareList) {
        this.mShelfShareList = mShelfShareList;
    }

    public void setmSOSKUList(ArrayList<SOSKUBO> mSOSKUList) {
        this.mSOSKUList = mSOSKUList;
    }

    private Vector<SOSBO> sosVector = new Vector<SOSBO>();

    public Vector<SOSBO> getSOSBO() {
        return sosVector;
    }

    public void setSOSBO(Vector<SOSBO> sosVector) {
        this.sosVector = sosVector;
    }

    /**
     * Load and set Values to objects in Edit Mode
     *
     * @param
     */
    public void loadSODBlockDetails(String uid, String pid, int totalShelfs, int locid) {
        DBUtil db = null;
        // ShelfShareDialogFragment.mBrandsDetailsHashMap = new HashMap<String,
        // ShelfShareBO>();
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("select sb.SubCellId,sb.CellId,sb.ChildPid,PM.pname from SOD_Tracking_Block_Detail SB ");
            sb.append("left join productmaster pm on PM.pid=SB.ChildPid");
            sb.append(" WHERE sb.Uid=" + bmodel.QT(uid) + "and SB.PID="
                    + bmodel.QT(pid) + " and SB.locid=" + locid);
            sb.append(" order by sb.SubCellId ,sb.CellId  ");

            Cursor detailCursor = db.selectSQL(sb.toString());

            if (detailCursor.getCount() > 0) {

                for (int i = 0; i < totalShelfs; i++) {
                    ShelfShareBO shelfShareBO = new ShelfShareBO();

                    shelfShareBO.setFirstCell("empty");
                    shelfShareBO.setSecondCell("empty");
                    shelfShareBO.setThirdCell("empty");
                    shelfShareBO.setFourthCell("empty");
                    SODDialogFragment.mBrandsDetailsHashMap.put(
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
                                SODDialogFragment.mBrandsDetailsHashMap.put(
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
            db.closeDB();
        }
    }

    public ArrayList<SOSBO> getLstSOSproj() {
        return lstSOSproj;
    }

    public void setLstSOSproj(ArrayList<SOSBO> lstSOSproj) {
        this.lstSOSproj = lstSOSproj;
    }

    ArrayList<SOSBO> lstSOSproj;

    public ArrayList<SOSBO> downloadSOSgroups() {

        DBUtil db = null;
        try {
            downloadSOSProjAttributes();

            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder str = new StringBuilder();
            str.append("select  DISTINCT SD.SOSgroupid,groupName,SD.pid,SD.plid,isown,inTarget," +
                    "Case  IFNULL(SAM.groupid,-1) when -1  then '0' else '1' END as flag ,SM.groupid,SAM.sosNormid,SM.target,PM.pname");
            str.append(" from  SOS_GroupHeader_Proj SH inner join SOS_GroupDetail_Proj SD ON SH.SOSgroupid=SD.SOSgroupid" +
                    " left join productMaster PM on PM.pid=SD.pid" +
                    " inner join SOS_NormMapping_Proj SM ON SM.SOSgroupid==SD.SOSgroupid" +
                    " LEFT JOIN SOS_NormAttributeMapping_Proj SAM on SAM .GroupId= SM.GroupID and SAM.SOSNormID=SM.SOSNormID");
            str.append(" where SM.accid in(0," + bmodel.getRetailerMasterBO().getAccountid() + ")" +
                    "and SM.RetailerId in(0," + bmodel.getRetailerMasterBO().getRetailerID() + ")" +
                    "and SM.chid in(0," + bmodel.getRetailerMasterBO().getSubchannelid() + ")" +
                    "and SM.locid in(0," + bmodel.getRetailerMasterBO().getLocationId() + ")");
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
                        bo.setGroupTarget(c.getInt(9));
                        lstSOSproj.add(bo);
                    }


                }

            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
        }
        return lstSOSproj;
    }


    ArrayList<String> lstSOSProjAttributes;

    private void downloadSOSProjAttributes() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            StringBuilder str = new StringBuilder();
            str.append("select Distinct groupid,sosNormid from SOS_NormAttributeMapping_Proj SAM");
            str.append(" where sosNormid not in(");
            str.append("select sosNormid from SOS_NormAttributeMapping_Proj where attributeid not in (" + bmodel.getRetailerAttributeList() + ")) and attributeid in(" + bmodel.getRetailerAttributeList() + ")");
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
            db.closeDB();
        }
    }

    public void saveSOSproj() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String uid = (bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                    .now(SDUtil.DATE_TIME_ID));


            String values = "";
            boolean isData = false;
            for (SOSBO bo : getLstSOSproj()) {
                if (bo.getAvailability() > 0) {
                    values = uid + "," + bo.getGroupId() + "," + bo.getProductID() + "," + bo.getAvailability() + "," + bo.getIsOwn() + "," + bo.getInTarget() + "," + bo.getGroupTarget();
                    db.insertSQL(DataMembers.tbl_SOSDetail_Proj, DataMembers.tbl_SOSDetail_Proj_cols, values);
                    isData = true;
                }
            }

            if (isData) {
                db.insertSQL(DataMembers.tbl_SOSHeader_Proj, DataMembers.tbl_SOSHeader_Proj_cols, uid + "," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            }


            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
        }

    }
}
