package com.ivy.cpg.view.sf;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SODAssetHelper {
    private static SODAssetHelper instance = null;
    public int mSelectedBrandID = 0;
    public String mSelectedActivityName;
    private int mChannelId, mLocationId;
    private ArrayList<SFLocationBO> mLocationList;
    private Context mContext;
    private BusinessModel mBModel;
    private ArrayList<SODBO> mSODList;
    private String moduleSODAsset = "MENU_SOD_ASSET";
    private Vector<LevelBO> mFilterLevel;
    private HashMap<Integer, Vector<LevelBO>> mFilterLevelBo;
    private Vector<LevelBO> mSFModuleSequence;


    private SODAssetHelper(Context context) {
        this.mContext = context;
        this.mBModel = (BusinessModel) context;
    }

    public static SODAssetHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SODAssetHelper(context);
        }
        return instance;
    }

    private static ArrayList<SFLocationBO> cloneLocationList(
            ArrayList<SFLocationBO> list) {
        ArrayList<SFLocationBO> clone = new ArrayList<>(list.size());
        for (SFLocationBO item : list)
            clone.add(new SFLocationBO(item));
        return clone;
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

    private void loadParentFilter(int mProductLevelId) {

        String query;
        if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mProductLevelId - mBModel.productHelper.getmSelectedGLobalLevelID() + 1;

            query = "SELECT DISTINCT PM" + filterGap + ".PID, PM" + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mBModel.productHelper.getmSelectedGLobalLevelID() + " and PM1.PID =" + mBModel.productHelper.getmSelectedGlobalProductId();

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

    private void loadChildFilter(int mChildLevel, int mParentLevel,
                                 int mProductLevelId, int mParentLevelId) {

        String query;
        if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            int filterGap = mChildLevel - mBModel.configurationMasterHelper.globalSeqId + 1;
            int PM1Level = mParentLevel - mBModel.configurationMasterHelper.globalSeqId + 1;

            query = "SELECT DISTINCT PM" + PM1Level + ".PID, PM" + filterGap + ".PID,  PM"
                    + filterGap + ".PName FROM ProductMaster PM1 ";

            for (int i = 2; i <= filterGap; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM" + i
                        + ".ParentId = PM" + (i - 1) + ".PID";

            query = query + " WHERE PM1.PLid = " + mBModel.productHelper.getmSelectedGLobalLevelID() + " AND PM1.PID = " + mBModel.productHelper.getmSelectedGlobalProductId();

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

    public void loadSavedTracking(String modName) {
        DBUtil db = null;
        String sql;
        String uid;
        String moduleName = modName.replaceAll("MENU_", "");
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            mBModel.setNote("");
            sql = "SELECT Uid,Remark FROM " + moduleName + "_Tracking_Header"
                    + " WHERE RetailerId="
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + " and (upload='N' OR refid!=0)";

            Cursor headerCursor = db.selectSQL(sql);
            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                uid = headerCursor.getString(0);
                // Set Remark in Object
                mBModel.setNote(headerCursor.getString(1));

                StringBuffer sb = new StringBuffer();

                sb.append("SELECT SF.Pid,SF.Norm,SF.ParentTotal,SF.Required,SF.Actual,SF.Percentage,SF.Gap,SF.ReasonId,SF.ImageName,SF.Isown,SF.Isdone,SF.Parentid,SF.locid");
                sb.append(" From " + moduleName + "_Tracking_Detail SF");
                sb.append(" WHERE SF.Uid=" + mBModel.QT(uid));

                Cursor detailCursor = db.selectSQL(sb.toString());

                if (detailCursor.getCount() > 0) {

                    while (detailCursor.moveToNext()) {
                        if (modName.equalsIgnoreCase(moduleSODAsset)) {
                            for (SODBO msod : getSODList()) {
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
                sb.append(" WHERE Uid=");
                sb.append(mBModel.QT(uid));

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
            if (db != null) {
                db.closeDB();
            }
            Commons.printException("loadSavedTracking" + modName + e);
        }
    }

    /**
     * set Image Name in Each Objects
     *
     * @param mBrandID Brand ID
     * @param imgName  image Name
     */

    public void onSaveImageName(int mBrandID, String imgName, String moduleName, int locationIndex) {
        try {
            if (moduleName.equals(HomeScreenTwo.MENU_SOD_ASSET)) {
                for (int i = 0; i < getSODList().size(); ++i) {
                    SODBO sod = getSODList().get(i);
                    if (sod.getProductID() == mBrandID) {
                        getSODList().get(i).getLocations().get(locationIndex).setImageName(imgName);
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
     * @param module Module Name
     * @return Availability
     */
    public boolean hasData(String module) {

        if (module.equalsIgnoreCase(moduleSODAsset)) {
            for (SODBO levelbo : getSODList()) {
                for (int i = 0; i < levelbo.getLocations().size(); i++) {
                    if (!levelbo.getLocations().get(i).getParentTotal().equals("0") || levelbo.getLocations().get(i).getAudit() != 2) {
                        return true;
                    }
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

    private void setSODList(ArrayList<SODBO> mList) {
        mSODList = mList;
    }

    private void updateSODAsset(int assetID, int productId, int actual, int reasonId, int locationId, String isPromo, String isDisplay) {

        AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(mContext);

        for (AssetTrackingBO assetTrackingBO : assetTrackingHelper.getAssetTrackingList()) {
            if (assetTrackingBO.getAssetID() == assetID && assetTrackingBO.getProductId() == productId) {
                assetTrackingBO.setActual(actual);
                assetTrackingBO.setReasonID(reasonId);
                assetTrackingBO.setLocationID(locationId);
                assetTrackingBO.setIsPromo(isPromo);
                assetTrackingBO.setIsDisplay(isDisplay);
            }
        }

    }

    /**
     * Download SOD Assets
     *
     * @param moduleName Module Name
     * @param IsAccount  Is Account Wise filter
     * @param IsRetailer Is Retailer Wise filter
     * @param IsClass    Is Class wise filter
     * @param LocId      Is Location wise  filter
     * @param ChId       Is Channel wise filter
     */
    private void downloadSalesFundamental(String moduleName, boolean IsAccount, boolean IsRetailer, boolean IsClass, int LocId, int ChId) {
        DBUtil db = null;
        try {
            Cursor cursor;
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            int mParentLevel = 0;
            int mChildLevel = 0;
            int mContentLevel = 0;
            int mFirstLevel;
            int loopEnd;
            String query = "";


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
                            + mBModel.QT(moduleName));

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


            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT DISTINCT A1.Pid,A" + loopEnd + ".pid,");
            stringBuilder.append("A" + loopEnd + ".pname ,1 isOwn,");
            stringBuilder.append("IFNULL(SFN.Norm,0) as Norm,SFN.MappingId FROM ProductMaster A1");

            for (int i = 2; i <= loopEnd; i++) {

                query = query + " INNER JOIN ProductMaster A" + i + " ON A" + i
                        + ".ParentId = A" + (i - 1) + ".PID";
            }
            stringBuilder.append(query);

            stringBuilder.append(" LEFT JOIN "
                    + moduleName.replace("MENU_", "") + "_NormMapping  SFN ON A" + loopEnd
                    + ".pid = SFN.pid  ");

            if (IsRetailer) {
                stringBuilder.append("and SFN.RetailerId =");
                stringBuilder.append(mBModel.getRetailerMasterBO().getRetailerID());
            }
            if (IsAccount) {
                stringBuilder.append(" and SFN.AccId=" + mBModel.getRetailerMasterBO().getAccountid());
            }
            if (IsClass) {
                stringBuilder.append(" and SFN.ClassId=" + mBModel.getRetailerMasterBO().getClassid());
            }

            if (LocId > 0)
                stringBuilder.append(" and SFN.LocId=" + mBModel.productHelper.getMappingLocationId(LocId, mBModel.getRetailerMasterBO().getLocationId()));
            if (ChId > 0)
                stringBuilder.append(" and SFN.ChId=" + mBModel.productHelper.getMappingChannelId(ChId, mBModel.getRetailerMasterBO().getSubchannelid()));

            stringBuilder.append(" LEFT JOIN " + moduleName.replace("MENU_", "") + "_NormMaster   SF ON SF.HId = SFN.HId");
            stringBuilder.append(" AND " + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " BETWEEN SF.StartDate AND SF.EndDate");
            stringBuilder.append(" WHERE A1.PLID IN (" + mFirstLevel + ")");

            if (moduleName.equals(moduleSODAsset)) {
                SODBO mSOD;
                cursor = db.selectSQL(stringBuilder.toString());
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
                        mSOD.setLocations(cloneLocationList(getLocationList()));
                        getSODList().add(mSOD);
                    }
                    cursor.close();
                }
                loadCompetitors(moduleSODAsset);
            }
            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
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
        try {
            Cursor cursor;
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT DISTINCT PM.pid ,CP.CPID, CP.CPName,0,0 FROM CompetitorProductMaster CP");
            stringBuilder.append(" INNER JOIN CompetitorMappingMaster CM ON CM.CPid = CP.CPId");
            stringBuilder.append(" INNER JOIN ProductMaster PM ON PM.PID = CM.PID");
            stringBuilder.append(" WHERE  CP.Plid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + mBModel.QT(moduleName) + ")");

            if (moduleName.equals(moduleSODAsset)) {

                cursor = db.selectSQL(stringBuilder.toString());
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

                                getSODList().add(comLevel);
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
     * @param moduleName Module Name
     * @return is Saved
     */
    public boolean saveSalesFundamentalDetails(String moduleName, ArrayList<AssetTrackingBO> assetList) {
        String modName = moduleName.replaceAll("MENU_", "");
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

            String uid = (mBModel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                    .now(SDUtil.DATE_TIME_ID));

            String query = "select Uid,refid from " + modName
                    + "_Tracking_Header  where RetailerId="
                    + mBModel.QT(mBModel.retailerMasterBO.getRetailerID());
            query += " and (upload='N' OR refid!=0)";

            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(modName + "_Tracking_Header",
                        "Uid=" + mBModel.QT(cursor.getString(0)), false);
                db.deleteSQL(modName + "_Tracking_Detail",
                        "Uid=" + mBModel.QT(cursor.getString(0)), false);
                db.deleteSQL("SOD_Assets_Detail",
                        "Uid=" + mBModel.QT(cursor.getString(0)), false);

                refId = cursor.getString(1);
                // uid = cursor.getString(0);
            }
            cursor.close();
            // Inserting Header in Tables

            headerValues = mBModel.QT(uid)
                    + "," + mBModel.getRetailerMasterBO().getRetailerID()
                    + "," + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + "," + mBModel.QT(mBModel.getNote())
                    + "," + mBModel.QT(refId);

            db.insertSQL(modName + "_Tracking_Header", headerColumns,
                    headerValues);

            try {
                for (SODBO sodBo : getSODList()) {
                    for (int i = 0; i < sodBo.getLocations().size(); i++) {
                        if (!sodBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sodBo.getLocations().get(i).getParentTotal().equals("0.0") || sodBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = mBModel.QT(uid) + ","
                                    + sodBo.getProductID() + ","
                                    + mBModel.getRetailerMasterBO().getRetailerID()
                                    + "," + sodBo.getNorm() + ","
                                    + sodBo.getLocations().get(i).getParentTotal() + ","
                                    + sodBo.getLocations().get(i).getTarget() + "," + sodBo.getLocations().get(i).getActual()
                                    + "," + sodBo.getLocations().get(i).getPercentage() + ","
                                    + sodBo.getLocations().get(i).getGap() + "," + sodBo.getLocations().get(i).getReasonId()
                                    + "," + mBModel.QT(sodBo.getLocations().get(i).getImageName()) + ","
                                    + sodBo.getIsOwn() + "," + sodBo.getParentID() + "," + sodBo.getLocations().get(i).getAudit() + "," + sodBo.getMappingId() + "," + sodBo.getLocations().get(i).getLocationId();

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues);

                        }

                    }

                }
            } catch (Exception e) {
                Commons.printException("SOD Asset track Details Insert" + e);
            }

            try {
                for (AssetTrackingBO assetTrackingBO : assetList) {
                    if (assetTrackingBO.getActual() > 0
                            || assetTrackingBO.getReasonID() > 0 || assetTrackingBO.getLocationID() > 0
                            || !assetTrackingBO.getIsPromo().equals("N") || !assetTrackingBO.getIsDisplay().equals("N")) {

                        assetValues = mBModel.QT(uid) + ","
                                + assetTrackingBO.getAssetID() + ","
                                + assetTrackingBO.getActual()
                                + "," + assetTrackingBO.getReasonID() + ","
                                + assetTrackingBO.getLocationID() + ","
                                + mBModel.QT(mBModel.retailerMasterBO.getRetailerID()) + "," + assetTrackingBO.getProductId()
                                + "," + mBModel.QT(assetTrackingBO.getIsPromo()) + ","
                                + mBModel.QT(assetTrackingBO.getIsDisplay()) + ","
                                + assetTrackingBO.getTarget();

                        db.insertSQL("SOD_Assets_Detail",
                                assertColumns, assetValues);

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

    public void loadSODAssetData(String mMenuName) {
        try {

            int level;
            level = getRetailerLevel(mMenuName);
            if (mMenuName.equals("MENU_SOD_ASSET")) {
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

    private int getRetailerLevel(String mMenuCode) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
}
