package com.ivy.sd.png.provider.asset;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SODAssetHelper {
    private Context mContext;
    private BusinessModel mBModel;
    private static SODAssetHelper instance = null;
    private ArrayList<SODBO> mSODList;
    public int mSelectedBrandID = 0;
    private String moduleSODAsset = "MENU_SOD_ASSET";


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

    private Vector<LevelBO> mFilterLevel;
    private HashMap<Integer, Vector<LevelBO>> mFilterLevelBo;

    public void downloadSFFiveLevelFilter(String moduleName) {
        Vector<LevelBO> mSFModuleSequence;
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
        String uid = "";
        String moduleName = modName.replaceAll("MENU_", "");
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            mBModel.setNote("");
            sql = "SELECT Uid,Remark FROM " + moduleName + "_Tracking_Header"
                    + " WHERE RetailerId="
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + " and (upload='N' OR refid!=0)";
            /*
             * + " AND Date = " + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
			 */

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
                sb.append(" WHERE Uid=" + mBModel.QT(uid));

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
            if(db!=null) {
                db.closeDB();
            }
            Commons.printException("loadSavedTracking" + modName + e);
        }
    }

    /**
     * set Image Name in Each Objects
     *
     * @param mBrandID Brand ID
     * @param imgName image Name
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

    private void updateSODAsset(int assetID, int productId, int actual, int reasonId, int locationId, String isPromo, String isDisplay) {

        for (AssetTrackingBO assetTrackingBO : mBModel.assetTrackingHelper.getAssetTrackingList()) {
            if (assetTrackingBO.getAssetID() == assetID && assetTrackingBO.getProductId() == productId) {
                assetTrackingBO.setActual(actual);
                assetTrackingBO.setReasonID(reasonId);
                assetTrackingBO.setLocationID(locationId);
                assetTrackingBO.setIsPromo(isPromo);
                assetTrackingBO.setIsDisplay(isDisplay);
            }
        }

    }

}
