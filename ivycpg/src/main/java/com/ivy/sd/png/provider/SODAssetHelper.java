package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AssetTrackingBO;
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

public class SODAssetHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static SODAssetHelper instance = null;
    private ArrayList<SODBO> mSODList;
    public int mSelectedBrandID = 0;
    private String moduleSODAsset = "MENU_SOD_ASSET";


    protected SODAssetHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static SODAssetHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SODAssetHelper(context);
        }
        return instance;
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
                    loopEnd = mContentLevel;
                else
                    loopEnd = mContentLevel - mParentLevel + 1;

                if (!mSFModuleSequence.isEmpty())
                    mFirstLevel = mSFModuleSequence.get(mSFModuleSequence.size() - 1).getProductID();
                else
                    mFirstLevel = mChildLevel;

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

            if (moduleName.equals(moduleSODAsset)) {
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
                loadCompetitors(moduleSODAsset);
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

            if (moduleName.equals(moduleSODAsset)) {

                cursor = db.selectSQL(sBuffer.toString());
                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        for (SODBO prodBO : getmSODList()) {

                            if (prodBO.getProductID() == cursor.getInt(0)) {

                                SODBO comLevel = new SODBO();
                                comLevel.setParentID(prodBO.getParentID());
                                comLevel.setProductID(cursor.getInt(1));
                                comLevel.setProductName(cursor.getString(2));
                                comLevel.setIsOwn(cursor.getInt(3));
                                comLevel.setNorm(cursor.getInt(4));

                                getmSODList().add(comLevel);
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
    public boolean saveSalesFundamentalDetails(String moduleName, ArrayList<AssetTrackingBO> assetList) {
        String modName = moduleName.replaceAll("MENU_", "");
        int count = 1;
        try {
            String refId = "0";
            String headerValues;
            String detailValues;
            String assetValues;
            String headerColumns = "Uid,RetailerId,Date,Remark,refid";
            String detailColumns = "Uid,Pid,RetailerId,Norm,ParentTotal,Required,Actual,Percentage,Gap,ReasonId,ImageName,IsOwn,ParentID,Isdone,MappingId,LocId";
            String assertColumns = "Uid,AssetID,Actual,ReasonID,LocationID,Retailerid,ProductId,isPromo,isDisplay,Target";


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
                db.deleteSQL("SOD_Assets_Detail",
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);

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

            try {
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

                        }

                    }

                }
            } catch (Exception e) {
                Commons.printException("SOD Assettrack Details Insert" + e);
            }

            try {
                for (AssetTrackingBO assetTrackingBO : assetList) {
                    if (assetTrackingBO.getActual() > 0
                            || assetTrackingBO.getReasonID() > 0 || assetTrackingBO.getLocationID() > 0
                            || !assetTrackingBO.getIsPromo().equals("N") || !assetTrackingBO.getIsDisplay().equals("N")) {

                        assetValues = bmodel.QT(uid) + ","
                                + assetTrackingBO.getAssetID() + ","
                                + assetTrackingBO.getActual()
                                + "," + assetTrackingBO.getReasonID() + ","
                                + assetTrackingBO.getLocationID() + ","
                                + bmodel.QT(bmodel.retailerMasterBO.getRetailerID()) + "," + assetTrackingBO.getProductid()
                                + "," + bmodel.QT(assetTrackingBO.getIsPromo()) + ","
                                + bmodel.QT(assetTrackingBO.getIsDisplay()) + ","
                                + assetTrackingBO.getTarget();

                        db.insertSQL("SOD_Assets_Detail",
                                assertColumns, assetValues.toString());

                    }
                }
            } catch (Exception e) {
                Commons.printException("SOD_Assets_Detail" + e);
            }


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return true;
    }


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

                StringBuffer sb = new StringBuffer();

                sb.append("SELECT SF.Pid,SF.Norm,SF.ParentTotal,SF.Required,SF.Actual,SF.Percentage,SF.Gap,SF.ReasonId,SF.ImageName,SF.Isown,SF.Isdone,SF.Parentid,SF.locid");
                sb.append(" From " + moduleName + "_Tracking_Detail SF");
                sb.append(" WHERE SF.Uid=" + bmodel.QT(uid));

                Cursor detailCursor = db.selectSQL(sb.toString());

                if (detailCursor.getCount() > 0) {

                    while (detailCursor.moveToNext()) {
                        if (modName.equalsIgnoreCase(moduleSODAsset)) {
                            for (SODBO msod : getmSODList()) {
                                if (msod.getProductID() == detailCursor
                                        .getInt(0)) {
                                    for (int i = 0; i < msod.getLocations().size(); i++) {
                                        if (msod.getLocations().get(i).getLocationId() == detailCursor.getInt(12)) {
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
                        }
                    }
                }

                detailCursor.close();

                sb = new StringBuffer();

                sb.append("SELECT AssetID,ProductId,Actual,ReasonID,LocationID,isPromo,isDisplay");
                sb.append(" From SOD_Assets_Detail ");
                sb.append(" WHERE Uid=" + bmodel.QT(uid));

                Cursor assetTrackDetails = db.selectSQL(sb.toString());

                if (assetTrackDetails.getCount() > 0) {
                    while (assetTrackDetails.moveToNext()) {
                        updateSODAsset(assetTrackDetails.getInt(0), assetTrackDetails.getInt(1), assetTrackDetails.getInt(2),
                                assetTrackDetails.getInt(3), assetTrackDetails.getInt(4), assetTrackDetails.getString(5), assetTrackDetails.getString(6));
                    }
                }
                assetTrackDetails.close();
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
            if (moduleName.equals(HomeScreenTwo.MENU_SOD_ASSET)) {
                for (int i = 0; i < getmSODList().size(); ++i) {
                    SODBO sod = getmSODList().get(i);
                    if (sod.getProductID() == mBrandID) {
                        getmSODList().get(i).getLocations().get(locationIndex).setImageName(imgName);
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

        if (module.equalsIgnoreCase(moduleSODAsset)) {
            for (SODBO levelbo : getmSODList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public ArrayList<SODBO> getmSODList() {
        if (mSODList == null)
            return new ArrayList<SODBO>();
        return mSODList;
    }

    public void setmSODList(ArrayList<SODBO> mSODList) {
        this.mSODList = mSODList;
    }

    private void updateSODAsset(int assetID, int productId, int actual, int reasonId, int locationId, String isPromo, String isDisplay) {

        for (AssetTrackingBO assetTrackingBO : bmodel.assetTrackingHelper.getAssetTrackingList()) {
            if (assetTrackingBO.getAssetID() == assetID && assetTrackingBO.getProductid() == productId) {
                assetTrackingBO.setActual(actual);
                assetTrackingBO.setReasonID(reasonId);
                assetTrackingBO.setLocationID(locationId);
                assetTrackingBO.setIsPromo(isPromo);
                assetTrackingBO.setIsDisplay(isDisplay);
            }
        }

    }

}
