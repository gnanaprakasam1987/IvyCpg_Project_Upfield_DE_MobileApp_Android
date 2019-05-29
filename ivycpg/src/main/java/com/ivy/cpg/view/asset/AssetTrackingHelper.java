package com.ivy.cpg.view.asset;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetAddDetailBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@SuppressLint("UseSparseArrays")
public class AssetTrackingHelper {

    private static AssetTrackingHelper instance = null;
    private final BusinessModel mBusinessModel;
    private AssetTrackingBO mAssetTrackingBO;
    public String mSelectedActivityName;

    private ArrayList<AssetTrackingBO> mAssetTrackingList = new ArrayList<>();
    private Vector<AssetTrackingBO> mAddRemoveAssets = null;
    private Vector<AssetAddDetailBO> mAssetSpinner = null;
    private Vector<AssetAddDetailBO> mBrandSpinner = null;
    private ArrayList<ReasonMaster> mAssetReasonList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetRemarkList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetConditionList = new ArrayList<>();
    private ArrayList<ReasonMaster> mPOSMReasonList = new ArrayList<>();
    private ArrayList<ReasonMaster> mPOSMConditionList = new ArrayList<>();
    private HashMap<String, String> mUniqueSerialNo;

    private Vector<AssetTrackingBO> assetServiceList = null;

    public int mSelectedAssetID = 0;
    public String mSelectedSerialNumber = "";
    public int mSelectedProductID = 0;
    public String mSelectedImageName = "";
    public int mSelectedLocationID = 0;

    // Asset configuration
    private static final String CODE_ASSET_COLUMNS = "AT01";
    private static final String CODE_ASSET_BARCODE = "AT02";
    private static final String CODE_ASSET_ADD = "AT03";
    private static final String CODE_REMOVE_ASSET = "AT04";
    private static final String CODE_SHOW_ALL = "AT05";
    private static final String CODE_REMARKS_ASSET = "AT06";
    private static final String CODE_ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = "AT08";
    private static final String CODE_MOVE_ASSET = "AT09";
    private static final String ASSET_REASON = "AR";
    private static final String ASSET_REMARK = "ARR";
    private static final String ASSET_CONDITION = "CD";
    private static final String CODE_ASSET_SERVICE = "AT11";
    public boolean SHOW_ASSET_QTY;
    public boolean SHOW_ASSET_REASON;
    public boolean SHOW_ASSET_PHOTO;
    public boolean SHOW_ASSET_CONDITION;
    public boolean SHOW_ASSET_INSTALL_DATE;
    public boolean SHOW_ASSET_SERVICE_DATE;
    public boolean SHOW_COMPETITOR_QTY;
    public boolean SHOW_ASSET_GRP;
    public boolean SHOW_ASSET_EXECUTED;
    public boolean SHOW_ASSET_BARCODE;
    public boolean SHOW_ADD_NEW_ASSET;
    public boolean SHOW_REMOVE_ASSET;
    public boolean SHOW_ASSET_ALL;
    public boolean SHOW_REMARKS_ASSET;
    public boolean ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK;
    public boolean SHOW_MOVE_ASSET;

    public boolean SHOW_SERVICE_ASSET;


    //POSM configuration
    private static final String POSM_REASON = "POSMR";
    private static final String POSM_CONDITION = "POSMCD";
    private static final String CODE_POSM_COLUMNS = "POSM01";
    private static final String CODE_POSM_BARCODE = "POSM02";
    private static final String CODE_SHOW_ALL_POSM = "POSM05";
    private static final String CODE_SHOW_POSM_REMARKS = "POSM06";
    private static final String CODE_SHOW_POSM_LOCATION = "POSM07";
    private static final String CODE_POSM_PHOTO_COUNT = "POSM08";
    private static final String CODE_ATTR_POSM = "ATRPOSM";
    public boolean SHOW_POSM_TARGET;
    public boolean SHOW_POSM_QTY;
    public boolean SHOW_POSM_REASON;
    public boolean SHOW_POSM_PHOTO;
    public boolean SHOW_POSM_CONDITION;
    public boolean SHOW_POSM_INSTALL_DATE;
    public boolean SHOW_POSM_SERVICE_DATE;
    public boolean SHOW_POSM_COMPETITOR_QTY;
    public boolean SHOW_POSM_GRP;
    public boolean SHOW_POSM_EXECUTED;
    public boolean SHOW_POSM_BARCODE;
    public boolean SHOW_POSM_ALL;
    public boolean SHOW_REMARKS_POSM;
    public boolean SHOW_LOCATION_POSM;
    public boolean SHOW_ATTR_POSM;
    public int POSM_PHOTO_COUNT = 1;

    private static final String MERCH = "MERCH";
    private static final String MENU_ASSET = "MENU_ASSET";
    private static final String MERCH_INIT = "MERCH_INIT";
    private static final String MENU_POSM = "MENU_POSM";

    private AssetTrackingHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }

    private AssetTrackingBO getAssetTrackingBO() {
        return mAssetTrackingBO;
    }

    public void setAssetTrackingBO(AssetTrackingBO mAssetTrackingBO) {
        this.mAssetTrackingBO = mAssetTrackingBO;
    }

    public Vector<AssetTrackingBO> getAddRemoveAssets() {
        return mAddRemoveAssets;
    }

    public static AssetTrackingHelper getInstance(Context context) {
        if (instance == null)
            instance = new AssetTrackingHelper(context);

        return instance;
    }

    public void clear() {
        instance = null;
    }

    /*
    Download Master Data needed for Asset Screen
     */
    public boolean loadDataForAssetPOSM(Context mContext, String mMenuCode) {
        if (mBusinessModel.configurationMasterHelper
                .downloadFloatingSurveyConfig(mMenuCode)) {
            SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(mContext);
            surveyHelperNew.setFromHomeScreen(false);
            surveyHelperNew.downloadModuleId("STANDARD");
            surveyHelperNew.downloadQuestionDetails(mMenuCode);
            surveyHelperNew.loadSurveyAnswers(0);
            // mBusinessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
            mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
            mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                    mBusinessModel.productHelper.getRetailerModuleSequenceValues(), false));

        }

        //update configurations
        if (MENU_ASSET.equalsIgnoreCase(mMenuCode))
            loadAssetConfigs(mContext);
        else if (MENU_POSM.equalsIgnoreCase(mMenuCode) || "MENU_POSM_CS".equalsIgnoreCase(mMenuCode))
            loadPOSMConfigs(mContext);

        //download locations
        mBusinessModel.productHelper.downloadInStoreLocations();

        //download filter levels
        // mBusinessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
        mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
        mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                mBusinessModel.productHelper.getRetailerModuleSequenceValues(), false));

        // Load master records
        if (MENU_POSM.equalsIgnoreCase(mMenuCode) && SHOW_ATTR_POSM)
            downloadPosmMaster(mContext);
        else
            downloadAssetMaster(mContext, mMenuCode);


        // Load data from transaction
        return loadAssetData(mContext, mBusinessModel
                .getRetailerMasterBO().getRetailerID(), mMenuCode);
    }

    /**
     * Load All Asset Screen Specific Configurations
     */
    private void loadAssetConfigs(Context mContext) {
        try {
            SHOW_ASSET_QTY = false;
            SHOW_ASSET_REASON = false;
            SHOW_ASSET_PHOTO = false;
            SHOW_ASSET_CONDITION = false;
            SHOW_ASSET_INSTALL_DATE = false;
            SHOW_ASSET_SERVICE_DATE = false;
            SHOW_COMPETITOR_QTY = false;
            SHOW_ASSET_GRP = false;
            SHOW_ASSET_EXECUTED = false;
            SHOW_ASSET_BARCODE = false;
            SHOW_ADD_NEW_ASSET = false;
            SHOW_REMOVE_ASSET = false;
            SHOW_ASSET_ALL = false;
            SHOW_REMARKS_ASSET = false;
            ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = false;
            SHOW_MOVE_ASSET = false;
            SHOW_SERVICE_ASSET = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE menu_type = 'MENU_ASSET' AND flag='1' and ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_ASSET_COLUMNS)) {
                        if (c.getString(1) != null) {
                            String codeSplit[] = c.getString(1).split(",");
                            for (String temp : codeSplit) {
                                updateAssetColumnConfig(temp);
                            }
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_BARCODE) && c.getString(1).equalsIgnoreCase("1"))
                        SHOW_ASSET_BARCODE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_ADD))
                        SHOW_ADD_NEW_ASSET = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_REMOVE_ASSET))
                        SHOW_REMOVE_ASSET = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_ALL))
                        SHOW_ASSET_ALL = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_REMARKS_ASSET))
                        SHOW_REMARKS_ASSET = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK))
                        ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_MOVE_ASSET))
                        SHOW_MOVE_ASSET = true;
                    else if (CODE_ASSET_SERVICE.equalsIgnoreCase(c.getString(0)))
                        SHOW_SERVICE_ASSET = true;
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_ASSET_REASON)
                downloadAssetPOSMReason(mContext, ASSET_REASON);
            if (SHOW_ASSET_CONDITION)
                downloadAssetPOSMReason(mContext, ASSET_CONDITION);
            if (SHOW_REMARKS_ASSET)
                downloadAssetPOSMReason(mContext, ASSET_REMARK);

        } catch (Exception e) {
            Commons.printException("loadAssetConfigs " + e);
        }
    }

    /**
     * Update Asset Column Configurations
     *
     * @param temp Configuration Code
     */
    private void updateAssetColumnConfig(String temp) {
        switch (temp) {

            case "QTY":
                SHOW_ASSET_QTY = true;
                break;
            case "REASON":
                SHOW_ASSET_REASON = true;
                break;
            case "PHOTO":
                SHOW_ASSET_PHOTO = true;
                break;
            case "CONDITION":
                SHOW_ASSET_CONDITION = true;
                break;
            case "AID":
                SHOW_ASSET_INSTALL_DATE = true;
                break;
            case "ASD":
                SHOW_ASSET_SERVICE_DATE = true;
                break;
            case "COMPQTY":
                SHOW_COMPETITOR_QTY = true;
                break;
            case "GRP":
                SHOW_ASSET_GRP = true;
                break;
            case "EXECUTED":
                SHOW_ASSET_EXECUTED = true;
                break;
        }
    }

    /**
     * Load all POSM related configurations
     */
    private void loadPOSMConfigs(Context mContext) {
        try {
            SHOW_POSM_TARGET = false;
            SHOW_POSM_QTY = false;
            SHOW_POSM_REASON = false;
            SHOW_POSM_PHOTO = false;
            SHOW_POSM_CONDITION = false;
            SHOW_POSM_INSTALL_DATE = false;
            SHOW_POSM_SERVICE_DATE = false;
            SHOW_POSM_COMPETITOR_QTY = false;
            SHOW_POSM_GRP = false;
            SHOW_POSM_EXECUTED = false;

            SHOW_POSM_BARCODE = false;
            // SHOW_ADD_NEW_POSM = false;
            // SHOW_REMOVE_POSM = false;
            SHOW_POSM_ALL = false;
            SHOW_REMARKS_POSM = false;
            SHOW_LOCATION_POSM = false;
            SHOW_ATTR_POSM = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE menu_type = '" + MENU_POSM + "' AND flag='1' and ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_POSM_COLUMNS)) {
                        if (c.getString(1) != null) {
                            String codeSplit[] = c.getString(1).split(",");
                            for (String temp : codeSplit) {
                                updatePOSMColumnConfig(temp);
                            }
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_POSM_BARCODE))
                        SHOW_POSM_BARCODE = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_ALL_POSM))
                        SHOW_POSM_ALL = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_POSM_REMARKS))
                        SHOW_REMARKS_POSM = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_POSM_LOCATION))
                        SHOW_LOCATION_POSM = true;
                    else if (c.getString(0).equalsIgnoreCase(POSM_REASON))
                        SHOW_POSM_REASON = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_ATTR_POSM))
                        SHOW_ATTR_POSM = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_POSM_PHOTO_COUNT))
                        if (c.getString(1) != null) {

                            int photoCount = c.getInt(1);
                            if (photoCount > 5)
                                photoCount = 5;
                            if (photoCount == 0)
                                photoCount = 1;

                            POSM_PHOTO_COUNT = photoCount;
                        }

                }
                c.close();
            }
            db.closeDB();


        } catch (Exception e) {
            Commons.printException("loadPOSMConfigs " + e);
        }

        if (SHOW_POSM_REASON)
            downloadAssetPOSMReason(mContext, POSM_REASON);
        if (SHOW_POSM_CONDITION)
            downloadAssetPOSMReason(mContext, POSM_CONDITION);

    }

    /**
     * Update POSM column configurations
     *
     * @param temp Configuration Code
     */
    private void updatePOSMColumnConfig(String temp) {
        switch (temp) {
            case "TGT":
                SHOW_POSM_TARGET = true;
                break;
            case "QTY":
                SHOW_POSM_QTY = true;
                break;
            case "REASON":
                SHOW_POSM_REASON = true;
                break;
            case "PHOTO":
                SHOW_POSM_PHOTO = true;
                break;
            case "CONDITION":
                SHOW_POSM_CONDITION = true;
                break;
            case "AID":
                SHOW_POSM_INSTALL_DATE = true;
                break;
            case "ASD":
                SHOW_POSM_SERVICE_DATE = true;
                break;
            case "COMPQTY":
                SHOW_POSM_COMPETITOR_QTY = true;
                break;
            case "GRP":
                SHOW_POSM_GRP = true;
                break;
            case "EXEUTED":
                SHOW_POSM_EXECUTED = true;
                break;
        }
    }

    /**
     * Method that to download Asset Details from SQLite
     *
     * @param moduleName module name
     */
    private void downloadAssetMaster(Context mContext, String moduleName) {
        ArrayList<AssetTrackingBO> mAllAssetTrackingList = null;

        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        mAssetTrackingList = new ArrayList<>();
        mUniqueSerialNo = new HashMap<>();

        AssetTrackingBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            mBusinessModel.productHelper.getRetailerlevel(moduleName);
            sb.append("select Distinct P.PosmId,P.Posmdesc,SBD.SerialNO,SBD.Target,SBD.Productid,SLM.listname,SLM.listid,SBD.NfcTagId,SBD.StoreLocId,SDM.listname as locname,PM.ParentHierarchy as ParentHierarchy, ");
            sb.append("SBDM.InstallDate,SBDM.LastServiceDate from PosmMaster P  ");
            sb.append("inner join POSMCriteriaMapping SBD on P.PosmID=SBD.posmid ");
            sb.append("left join SbdMerchandisingMaster SBDM on SBDM.SBDID=P.PosmId ");
            sb.append("left join Standardlistmaster SLM on SLM.listid=SBD.PosmGroupLovId and SLM.ListType='POSM_GROUP_TYPE' ");
            sb.append("left join Standardlistmaster SDM on SDM.listid=SBD.StoreLocId and SDM.ListType='PL' ");
            sb.append("left join ProductMaster PM on PM.PID=SBD.Productid ");
            sb.append("where  SBD.TypeLovId=(select listid from StandardListMaster where ListCode=");
            sb.append(mBusinessModel.QT(type));
            sb.append(" and ListType='SBD_TYPE') ");

            String allMasterSb = sb.toString();
            sb.append(" and SBD.AccountId in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getAccountid() + ")");
            sb.append(" and SBD.Retailerid in(0,");
            sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ")");
            sb.append(" and SBD.Classid in (0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getClassid() + ")");
            sb.append(" and SBD.Locid in(0,");
            sb.append(mBusinessModel.productHelper.getMappingLocationId(mBusinessModel.productHelper.locid, mBusinessModel.getRetailerMasterBO().getLocationId()));
            sb.append(")");
            sb.append(" and (SBD.Channelid in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid() + ")");
            sb.append(" OR SBD.Channelid in (0,");
            sb.append(mBusinessModel.channelMasterHelper.getChannelHierarchy(mBusinessModel.getRetailerMasterBO().getSubchannelid(), mContext) + "))");


            if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                sb.append(" and (SBD.Productid = ");
                sb.append(mBusinessModel.productHelper.getmSelectedGlobalProductId());
                sb.append(" OR SBD.Productid = 0 )");

                allMasterSb = allMasterSb + ("and (SBD.Productid = " + mBusinessModel.productHelper.getmSelectedGlobalProductId() + " OR SBD.Productid = 0 )");
            }

            sb.append(" GROUP BY SBD.RetailerId,SBD.AccountId,SBD.Channelid,SBD.Locid,SBD.Classid,SBD.Productid,SBD.PosmId,SBD.SerialNO ORDER BY SBD.RetailerId,SBD.AccountId,SBD.Channelid,SBD.Locid,SBD.Classid");

            Cursor c = db.selectSQL(sb.toString());
            Cursor c1 = db.selectSQL(allMasterSb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetTrackingBO = new AssetTrackingBO();
                    assetTrackingBO.setAssetID(c.getInt(0));
                    assetTrackingBO.setAssetName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2))) {
                        assetTrackingBO.setSerialNo(c.getString(2));
                    } else {
                        assetTrackingBO.setSerialNo(Integer.toString(0));
                    }

                    assetTrackingBO.setTarget(c.getInt(3));
                    assetTrackingBO.setProductId(c.getInt(4));
                    if (c.getString(5) != null && !"null".equals(c.getString(5))) {
                        assetTrackingBO.setGroupLevelName(c.getString(5));
                        assetTrackingBO.setGroupLevelId(c.getInt(6));
                    } else {
                        assetTrackingBO.setGroupLevelName("");
                        assetTrackingBO.setGroupLevelId(0);
                    }

                    assetTrackingBO.setNFCTagId(c.getString(c.getColumnIndex("NfcTagId")));
                    assetTrackingBO.setTargetLocId(c.getInt(c.getColumnIndex("StoreLocId")));
                    assetTrackingBO.setLocationName(c.getString(c.getColumnIndex("locname")));
                    assetTrackingBO.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));

                    assetTrackingBO.setDisableInstallDate(c.getString(c.getColumnIndex("InstallDate")) != null &&
                            !c.getString(c.getColumnIndex("InstallDate")).equals(""));

                    assetTrackingBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                            c.getString(c.getColumnIndex("InstallDate")),
                            ConfigurationMasterHelper.outDateFormat));
                    assetTrackingBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                            c.getString(c.getColumnIndex("LastServiceDate")),
                            ConfigurationMasterHelper.outDateFormat));

                    mAssetTrackingList.add(assetTrackingBO);

                }

            }

            //load serial no's into hash map for uniqueness

            if (MENU_ASSET.equals(moduleName)) {
                String sb1 = "select SerialNO from POSMCriteriaMapping where " +
                        "TypeLovId=(select listid from StandardListMaster where ListCode=" + mBusinessModel.QT(type) + ")";
                c = db.selectSQL(sb1);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (c.getString(0) != null)
                            mUniqueSerialNo.put(c.getString(0), c.getString(0));
                    }
                }
            }


            String sb1 = "select  serialNum from AssetAddDelete";
            Cursor cursorDelete = db.selectSQL(sb1);
            AssetTrackingBO assetBoDelete = null;
            List<AssetTrackingBO> deletedAssetList = new ArrayList<>();
            if (cursorDelete.getCount() > 0) {
                while (cursorDelete.moveToNext()) {
                    assetBoDelete = new AssetTrackingBO();

                    if (!"null".equals(cursorDelete.getString(0))) {
                        assetBoDelete.setSerialNo(cursorDelete.getString(0));
                    }
                    deletedAssetList.add(assetBoDelete);
                }
            }


            if (mAssetTrackingList != null && mAssetTrackingList.size() > 0) {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    // ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAssetTrackingList.size());
                    for (AssetTrackingBO assetDeleteBO : deletedAssetList) {
                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
                            if (assetDeleteBO.getSerialNo().equalsIgnoreCase(assetBO.getSerialNo())) {
                                mAssetTrackingList.remove(assetBO);
                                break;
                            }

                            // clonedList.add(assetBO);
                        }
                    }


                    //  for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    //  clonedList.add(assetBO);
                    // }
                    standardListBO.setAssetTrackingList(cloneAssetTrackingList(mAssetTrackingList));
                }

            } else {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    if (standardListBO.getAllAssetTrackingList() != null)
                        standardListBO.getAssetTrackingList().clear();
                }
            }
            if (c1.getCount() > 0) {
                mAllAssetTrackingList = new ArrayList<>();
                while (c1.moveToNext()) {
                    assetTrackingBO = new AssetTrackingBO();
                    assetTrackingBO.setAssetID(c1.getInt(0));
                    assetTrackingBO.setAssetName(c1.getString(1));
                    if (c1.getString(2) != null && !"null".equals(c1.getString(2))) {
                        assetTrackingBO.setSerialNo(c1.getString(2));
                    } else {
                        assetTrackingBO.setSerialNo(Integer.toString(0));
                    }

                    assetTrackingBO.setTarget(c1.getInt(3));
                    assetTrackingBO.setProductId(c1.getInt(4));
                    if (c1.getString(5) != null && !"null".equals(c1.getString(5))) {
                        assetTrackingBO.setGroupLevelName(c1.getString(5));
                        assetTrackingBO.setGroupLevelId(c1.getInt(6));
                    } else {
                        assetTrackingBO.setGroupLevelName("");
                        assetTrackingBO.setGroupLevelId(0);
                    }

                    assetTrackingBO.setNFCTagId(c1.getString(c1.getColumnIndex("NfcTagId")));
                    assetTrackingBO.setParentHierarchy(c1.getString(c1.getColumnIndex("ParentHierarchy")));

                    assetTrackingBO.setDisableInstallDate(c1.getString(c1.getColumnIndex("InstallDate")) != null &&
                            !c1.getString(c1.getColumnIndex("InstallDate")).equals(""));

                    assetTrackingBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                            c1.getString(c1.getColumnIndex("InstallDate")),
                            ConfigurationMasterHelper.outDateFormat));
                    assetTrackingBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                            c1.getString(c1.getColumnIndex("LastServiceDate")),
                            ConfigurationMasterHelper.outDateFormat));

                    mAllAssetTrackingList.add(assetTrackingBO);

                }

            }
            if (mAllAssetTrackingList != null && mAllAssetTrackingList.size() > 0) {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAllAssetTrackingList.size());
                    for (AssetTrackingBO assetBO : mAllAssetTrackingList) {
                        clonedList.add(new AssetTrackingBO(assetBO));
                    }
                    standardListBO.setAllAssetTrackingList(clonedList);
                }

            } else {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    standardListBO.getAssetTrackingList().clear();
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();

        }
    }

    /**
     * Duplicate the Location List
     *
     * @param list list
     * @return clone list
     */
    public static ArrayList<AssetTrackingBO> cloneAssetTrackingList(
            ArrayList<AssetTrackingBO> list) {
        ArrayList<AssetTrackingBO> clone = new ArrayList<AssetTrackingBO>(list.size());
        for (AssetTrackingBO item : list)
            clone.add(new AssetTrackingBO(item));
        return clone;
    }

    /**
     * Method that to get loaded data from SQLite table
     *
     * @param mRetailerId Retailer ID
     * @param moduleName  Module Name
     */
    private boolean loadAssetData(Context mContext, String mRetailerId, String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();

            String sb = "select uid, IFNULL(remark,'') from AssetHeader where retailerid=" + QT(mRetailerId) + " and TypeLovId=" +
                    "(select listid from StandardListMaster where ListCode=" + mBusinessModel.QT(type) + " and ListType='SBD_TYPE') " +
                    "and (upload='N' OR refid!=0)";

            if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM)
                sb = "select uid, IFNULL(remark,'') from AssetHeader where retailerid=" + QT(mRetailerId) + " and TypeLovId= 0 and (upload='N' OR refid!=0)";

            Cursor c = db.selectSQL(sb);
            String uid = "";
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    uid = c.getString(0);
                    mBusinessModel.setAssetRemark(c.getString(1));
                }
            } else {
                mBusinessModel.setAssetRemark("");
            }

            if (mBusinessModel.configurationMasterHelper.isAuditEnabled() && uid.trim().isEmpty()) {
                c.close();
                db.closeDB();
                return false;
            }

            String sb2 = "select assetid,availqty,imagename,reasonid,SerialNumber,conditionId,installdate,servicedate,isAuditDone,Productid,CompQty,Locid,PosmGroupLovId,isExecuted,imgName,MappingId  from assetDetail where uid=" +
                    QT(uid);


            Cursor detailCursor = db.selectSQL(sb2);
            if (detailCursor.getCount() > 0) {
                while (detailCursor.moveToNext()) {
                    int mAssetId = detailCursor.getInt(0);
                    int qty = detailCursor.getInt(1);
                    String imageName = detailCursor.getString(2);
                    String reasonId = detailCursor.getString(3);

                    String serialNo = detailCursor.getString(4);
                    String conditionId = detailCursor.getString(5);
                    int audit = detailCursor.getInt(8);
                    int pid = detailCursor.getInt(9);
                    int compQty = detailCursor.getInt(10);
                    int locId = detailCursor.getInt(11);
                    final int isExecuted = detailCursor.getInt(13);
                    String mappingID = detailCursor.getString(15);


                    setAssetDetails(mContext, moduleName,
                            mAssetId,
                            qty,
                            imageName,
                            reasonId,

                            serialNo,
                            conditionId,
                            DateTimeUtils.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(6),
                                    ConfigurationMasterHelper.outDateFormat),
                            DateTimeUtils.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(7),
                                    ConfigurationMasterHelper.outDateFormat), audit, pid, compQty, locId, isExecuted,
                            detailCursor.getString(detailCursor.getColumnIndex("imgName")),
                            mappingID, moduleName);
                }
            }
            detailCursor.close();
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
        return true;
    }

    /**
     * Method that to store asset tracking details in ArrayList
     *
     * @return ArrayList<AssetTrackingBO>
     */
    public ArrayList<AssetTrackingBO> getAssetTrackingList() {
        if (mAssetTrackingList != null) {
            return mAssetTrackingList;
        }

        return new ArrayList<>();

    }


    /**
     * Method that to download asset reason in ArrayList
     *
     * @param category Reason Category
     */
    private void downloadAssetPOSMReason(Context mContext, String category) {
        switch (category) {
            case ASSET_REASON:
                mAssetReasonList = new ArrayList<>();
                break;
            case ASSET_REMARK:
                mAssetRemarkList = new ArrayList<>();
                break;
            case ASSET_CONDITION:
                mAssetConditionList = new ArrayList<>();
                break;
            case POSM_REASON:
                mPOSMReasonList = new ArrayList<>();
                break;
            case POSM_CONDITION:
                mPOSMConditionList = new ArrayList<>();
                break;
        }
        ReasonMaster reasonBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            Cursor c = db.selectSQL(mBusinessModel.reasonHelper.getReasonFromStdListMaster(category));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    reasonBO = new ReasonMaster();
                    reasonBO.setReasonID(c.getString(0));
                    reasonBO.setReasonDesc(c.getString(1));
                    reasonBO.setConditionID(c.getString(0));

                    switch (category) {
                        case ASSET_REASON:
                            mAssetReasonList.add(reasonBO);
                            break;
                        case ASSET_REMARK:
                            mAssetRemarkList.add(reasonBO);
                            break;
                        case ASSET_CONDITION:
                            mAssetConditionList.add(reasonBO);
                            break;
                        case POSM_REASON:
                            mPOSMReasonList.add(reasonBO);
                            break;
                        case POSM_CONDITION:
                            mPOSMConditionList.add(reasonBO);
                            break;

                    }
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Download unique POSM available
     *
     * @param moduleName Module Name
     */
    public void downloadAssetsPosm(Context mContext, String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = "CMP";
        else if (MENU_POSM.equals(moduleName))
            type = "CMN";

        AssetAddDetailBO assetBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            String sb = "select distinct  PosmId,Posmdesc from PosmMaster where" +
                    " TypeLovId =(select listid from StandardListMaster where listcode = 'BRDASSET' and parentid= " +
                    "(select listid from StandardListmaster where ListCode=" + QT(type) + " and ListType='POSM_TYPE'))";


            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                mAssetSpinner = new Vector<>();
                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setPOSMId(c.getString(0));
                    assetBO.setPOSMDescription(c.getString(1));

                    mAssetSpinner.add(assetBO);

                }
            }

            //download exsisiting asset serial no from transaction table
            String sb1 = "select serialNum from AssetAddDelete";
            c = db.selectSQL(sb1);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mUniqueSerialNo.put(c.getString(0), c.getString(0));
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Get asset Names
     *
     * @return List of assets
     */
    public Vector<String> getAssetPosmNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = mAssetSpinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = mAssetSpinner.get(i);
                data.add(brand.getPOSMDescription());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    /**
     * Get asset brands
     *
     * @return Vector List of Asset Brands
     */
    public Vector<String> getAssetBrandNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = mBrandSpinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = mBrandSpinner.get(i);
                data.add(brand.getAssetBrandName());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    /**
     * Get asset Id for given asset name
     *
     * @param mAssetPosmName Asset name
     * @return Asset Id
     */
    public String getAssetPosmIds(String mAssetPosmName) {
        AssetAddDetailBO brand;

        try {
            int siz = mAssetSpinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = mAssetSpinner.get(i);
                Commons.print("brand.getPOSMDescription()="
                        + brand.getPOSMDescription() + "," + mAssetPosmName);
                if (brand.getPOSMDescription().equals(mAssetPosmName)) {

                    return brand.getPOSMId();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return mAssetPosmName;
    }

    /**
     * Get Asset brand Id for given Asset Brand Name
     *
     * @param mAssetBrandName Asset brand name
     * @return Brand id
     */
    public String getAssetBrandIds(String mAssetBrandName) {
        AssetAddDetailBO brand;

        try {
            int siz = mBrandSpinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = mBrandSpinner.get(i);
                if (brand.getAssetBrandName().equals(mAssetBrandName)) {
                    return brand.getAssetBrandId();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return mAssetBrandName;
    }

    /**
     * Download asset brands
     *
     * @param brandPosm POSM Id
     */
    public void downloadAssetBrand(Context mContext, String brandPosm) {

        AssetAddDetailBO assetBO;
        mBrandSpinner = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            String sb = "SELECT PM.Pid,PM.PName FROM ProductMaster PM INNER JOIN POSMProductMapping PO ON PM.Pid = PO.Productid WHERE PO.PosmID =" + QT(brandPosm);

            Cursor c = db.selectSQL(sb);

            if (c.getCount() > 0) {

                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setAssetBrandId(c.getString(0));
                    assetBO.setAssetBrandName(c.getString(1));

                    mBrandSpinner.add(assetBO);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }


    /**
     * Preparing List to Add or remove assets
     *
     * @param moduleName Module Name
     */
    public void lodAddRemoveAssets(Context mContext, String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;
        AssetTrackingBO assetBO;

        mAddRemoveAssets = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();


            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c0 = db.selectSQL(query1);
            if (c0.getCount() > 0) {
                while (c0.moveToNext()) {
                    typeListId = c0.getInt(0);
                }
            }
            mBusinessModel.productHelper.getRetailerlevel(moduleName);


            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.PosmId,P.Posmdesc,SBD.SerialNO,SBD.Mappingid,SBD.Productid  from PosmMaster P  inner join POSMCriteriaMapping SBD on P.PosmID=SBD.posmid where ");
            sb.append("(SBD.SerialNO  in (select distinct serialNum from AssetAddDelete AAD where flag!='D' and AAD.TypeLovId=");
            sb.append(typeListId);
            sb.append(") or SBD.SerialNO not in (select distinct serialNum from AssetAddDelete AAD1 where AAD1.TypeLovid=");
            sb.append(typeListId);
            sb.append("))");
            sb.append(" and SBD.TypeLovId=(select listid from StandardListMaster where ListCode=");
            sb.append(QT(type));
            sb.append(" and ListType='SBD_TYPE') ");
            sb.append(" and AccountId in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getAccountid() + ")");
            sb.append(" and Retailerid in(0,");
            sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ")");
            sb.append(" and Classid in (0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getClassid() + ")");
            sb.append(" and Locid in(0,");
            sb.append(mBusinessModel.productHelper.getMappingLocationId(mBusinessModel.productHelper.locid, mBusinessModel.getRetailerMasterBO().getLocationId()));
            sb.append(")");
            sb.append(" and (Channelid in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid() + ")");
            sb.append(" OR Channelid in (0,");
            sb.append(mBusinessModel.channelMasterHelper.getChannelHierarchy(mBusinessModel.getRetailerMasterBO().getSubchannelid(), mContext));
            sb.append(")) GROUP BY RetailerId,AccountId,Channelid,Locid,Classid,SBD.Productid,SBD.PosmId,SBD.SerialNO ORDER BY RetailerId,AccountId,Channelid,Locid,Classid");


            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setPOSM(c.getString(0));
                    assetBO.setPOSMName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2)) && !"".equals(c.getString(2))) {
                        assetBO.setSNO(c.getString(2));


                    } else {
                        assetBO.setSNO("0");
                    }
                    assetBO.setNewInstallDate(" ");
                    assetBO.setFlag("N");
                    assetBO.setmMappingID(c.getString(3));
                    assetBO.setBrand(c.getString(4));
                    mAddRemoveAssets.add(assetBO);
                }
            }
            String sb1 = "select distinct  AssetId,P.Posmdesc,serialNum,installdate,flag ,uid,Productid  from PosmMaster P  inner  join AssetAddDelete AAD on P.PosmId=AAD.AssetId where flag!='D'  and retailerid=" +
                    QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + " and AAD.TypeLovId=" + typeListId;

            Cursor c1 = db.selectSQL(sb1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setPOSM(c1.getString(0));
                    assetBO.setPOSMName(c1.getString(1));
                    if ("null".equals(c1.getString(2)) || "".equals(c1.getString(2))) {

                        assetBO.setSNO("0");
                    } else {
                        assetBO.setSNO(c1.getString(2));
                    }

                    assetBO.setNewInstallDate(c1.getString(3));
                    assetBO.setFlag("Y");
                    assetBO.setmMappingID(" ");
                    assetBO.setBrand(c1.getString(6));
                    mAddRemoveAssets.add(assetBO);
                }
            }

            c.close();
            c1.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Method return reason name arrayList
     *
     * @return ArrayList<ReasonMaster>
     */
    public ArrayList<ReasonMaster> getAssetReasonList() {
        if (mAssetReasonList != null) {
            return mAssetReasonList;
        }
        return new ArrayList<>();
    }


    public ArrayList<ReasonMaster> getAssetConditionList() {
        if (mAssetConditionList != null) {
            return mAssetConditionList;
        }
        return new ArrayList<>();
    }

    //check serial no is available are not in hash map for uniqueness
    public boolean getUniqueSerialNo(String serialNo) {
        if (mUniqueSerialNo == null || mUniqueSerialNo.size() == 0)
            return false;
        else {
            if (mUniqueSerialNo.get(serialNo) == null)
                return false;
            else
                return true;
        }
    }


    /**
     * Method return reason name arrayList
     *
     * @return ArrayList<ReasonMaster>
     */
    public ArrayList<ReasonMaster> getPOSMReasonList() {
        if (mPOSMReasonList != null) {
            return mPOSMReasonList;
        }
        return new ArrayList<>();
    }


    /**
     * Method return reason conditions arrayList
     *
     * @return ArrayList<ReasonMaster>
     */
    public ArrayList<ReasonMaster> getPOSMConditionList() {
        if (mPOSMConditionList != null) {
            return mPOSMConditionList;
        }
        return new ArrayList<>();
    }


    /**
     * Method return the correct position to selected ReasonId
     *
     * @param reasonId   ReasonId
     * @param reasonList Reason List
     * @return integer
     */
    public int getItemIndex(String reasonId, ArrayList<ReasonMaster> reasonList) {
        int size = reasonList.size();

        for (int i = 0; i < size; i++) {
            ReasonMaster reasonBO = reasonList.get(i);
            if (reasonBO.getReasonID().equals(reasonId)) {
                return i;
            }
        }
        return -1;
    }

    public int getItemIndex(int listID, Vector<StandardListBO> locationList) {
        int size = locationList.size();

        for (int i = 0; i < size; i++) {
            StandardListBO standardListBO = locationList.get(i);
            if (SDUtil.convertToInt(standardListBO.getListID()) == listID) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Method to delete imageName in sql table
     *
     * @param imgName imageName
     */
    public void deleteImageName(Context mContext, String imgName) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        db.updateSQL("UPDATE AssetDetail SET  imagename =" + QT("")
                + "WHERE imagename LIKE" + QT(imgName + "%"));
    }

    /**
     * Method to save Asset Details in sql table
     */
    public void saveAssetAddAndDeleteDetails(Context mContext, String moduleName) {
        String type = "";

        if (mUniqueSerialNo == null)
            mUniqueSerialNo = new HashMap<>();

        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if (MENU_POSM.equals(moduleName))
            type = MERCH_INIT;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c1 = db.selectSQL(query1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    typeListId = c1.getInt(0);
                }
            }
            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            AssetTrackingBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,retailerid,AssetId,serialNum,productid,installdate,creationdate,TypeLovId,reasonid,remarks";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(assets.getPOSM()) + "," + QT(assets.getSNO()) + ","
                    + QT(assets.getBrand()) + ","
                    + QT(DateTimeUtils.convertToServerDateFormat(
                    assets.getNewInstallDate(),
                    ConfigurationMasterHelper.outDateFormat))
                    + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + typeListId + "," +
                    QT(assets.getReasonId()) + "," + QT(assets.getRemarks());

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            //add serial no for uniqueness
            mUniqueSerialNo.put(assets.getSNO(), assets.getSNO());

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Method to save Asset Movement Details in sql table
     */
    public void saveAssetMovementDetails(Context mContext, String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if (MENU_POSM.equals(moduleName))
            type = MERCH_INIT;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c1 = db.selectSQL(query1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    typeListId = c1.getInt(0);
                }
            }
            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            AssetTrackingBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,retailerid,AssetId,serialNum,productid,creationdate,flag,TypeLovId,reasonid,remarks,toRetailerId";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(assets.getPOSM()) + "," + QT(assets.getSNO()) + ","
                    + QT(assets.getBrand()) + ","
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + QT("M") + "," + typeListId + "," +
                    QT(assets.getReasonId()) + "," + QT(assets.getRemarks()) + "," + QT(assets.getToRetailerId());

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Save added or deleted details
     *
     * @param posmId     POSM Id
     * @param mSno       SNO
     * @param mSbdId     SBD Id
     * @param mBrandId   Brand ID
     * @param reasonId   Reason ID
     * @param moduleName Module Name
     */
    public void saveAddAndDeleteDetails(Context mContext, String posmId, String mSno,
                                        String mSbdId, String mBrandId, String reasonId, String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if (MENU_POSM.equals(moduleName))
            type = MERCH_INIT;


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c1 = db.selectSQL(query1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    typeListId = c1.getInt(0);
                }
            }
            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String addAssetColumns = "uid,retailerid,AssetId,serialNum,creationdate,flag,mappingid,Productid,TypeLovId,reasonid";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(posmId) + "," + QT(mSno) + ","
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + QT("D") + ","
                    + QT(mSbdId) + "," + QT(mBrandId) + "," + typeListId + "," + QT(reasonId);

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Delete POSM details
     *
     * @param mSno SNO
     */
    public void deletePosmDetails(Context mContext, String mSno) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_AssetAddDelete, "serialNum ="
                    + QT(mSno), false);

            db.closeDB();

            //removed deleted serial no from hash map
            if (mUniqueSerialNo.get(mSno) != null)
                mUniqueSerialNo.remove(mSno);

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Checking Serial number availability
     *
     * @param mSno SNO
     * @return Is Serial Number available
     */
    public boolean isExistingRetailerSno(Context mContext, String mSno) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = "select serialNum from "
                    + DataMembers.tbl_AssetAddDelete + "  where serialNum="
                    + QT(mSno) + " and retailerid = "
                    + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + "";

            Cursor cursor = db.selectSQL(sql);

            if (cursor.getCount() > 0) {
                cursor.close();
                db.closeDB();
                return true;
            }
            cursor.close();
            db.closeDB();

            return false;
        } catch (Exception e) {
            Commons.printException("" + e);
            return false;
        }
    }

    /**
     * Method to check the Asset already scanned and mapped to other retailer in sql table
     */
    public boolean isExistingAssetInRetailer(Context mContext, String serialNum) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = "select AssetId from "
                    + DataMembers.tbl_AssetAddDelete + "  where serialNum="
                    + QT(serialNum) + " and retailerid = "
                    + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + "";

            Cursor cursor = db.selectSQL(sql);

            if (cursor.getCount() > 0) {
                cursor.close();
                db.closeDB();
                return true;
            }
            cursor.close();
            db.closeDB();

            return false;
        } catch (Exception e) {
            Commons.printException("" + e);
            return false;
        }
    }

    /**
     * Saving asset details
     *
     * @param moduleName Module Name
     */
    public void saveAsset(Context mContext, String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName)) {
            type = MERCH;
        } else if (MENU_POSM.equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
            type = MERCH_INIT;
        }
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            String refId = "0";
            int typeListId = 0;
            if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM) {
                typeListId = 0;
            } else {
                String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
                Cursor c1 = db.selectSQL(query1);
                if (c1.getCount() > 0) {
                    while (c1.moveToNext()) {
                        typeListId = c1.getInt(0);
                    }
                }
            }
            String query = "select uid,refid from AssetHeader where retailerid ="
                    + QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID())
                    + " and TypeLovid=" + typeListId;
            query += " and (upload='N' OR refid!=0)";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                c.moveToNext();
                db.deleteSQL(DataMembers.tbl_AssetHeader,
                        "uid=" + QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_AssetDetail,
                        "uid=" + QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_AssetImgInfo,
                        "uid=" + QT(c.getString(0)), false);
                refId = c.getString(1);
            }


            double productWeightAge, sum = 0;

            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String assetHeaderColumns = "uid,Date,RetailerId,remark,TypeLovid,tgtTotal,achTotal,refid,distributorid,ridSF,VisitId";
            StringBuilder assetHeaderValues = new StringBuilder();
            assetHeaderValues.append(id);
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.getAssetRemark()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(typeListId);


            String AssetDetailColumns = "uid,AssetID,AvailQty,ImageName,ReasonID,SerialNumber,conditionId,installdate,servicedate,isAuditDone,Productid,CompQty,Retailerid,LocId,PosmGroupLovId,isExecuted,imgName";
            String AssetImageInfoColumns = "uid,AssetID,ImageName,PId,LocId";
            if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                AssetDetailColumns = AssetDetailColumns + ",Score";
            }
            if (SHOW_LOCATION_POSM)
                AssetDetailColumns = AssetDetailColumns + ",TgtLocId";

            if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM)
                AssetDetailColumns = AssetDetailColumns + ",MappingId";

            int totalTarget = 0;
            int totalActualQty = 0;
            for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                mAssetTrackingList = standardListBO.getAssetTrackingList();
                if (mAssetTrackingList != null) {
                    productWeightAge = (double) 100 / (double) mAssetTrackingList.size();
                    totalTarget = 0;
                    for (AssetTrackingBO assetBo : mAssetTrackingList) {
                        totalTarget = totalTarget + assetBo.getTarget();
                        StringBuilder assetDetailValues = new StringBuilder();
                        if (assetBo.getReason1ID() != null) {
                            if (assetBo.getAvailQty() > 0
                                    || !assetBo.getReason1ID().equals(Integer.toString(0))
                                    || assetBo.getAudit() != 2 || assetBo.getExecutorQty() > 0
                                    || assetBo.getCompetitorQty() > 0) {
                                totalActualQty = totalActualQty + assetBo.getAvailQty();

                                assetDetailValues.append(id);
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAssetID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAvailQty());
                                if (assetBo.getImageList().size() > 0 && assetBo.getAvailQty() > 0) {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(assetBo.getImageList().get(0)));// added for backward compatibility
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(assetBo.getReason1ID());
                                } else {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(assetBo.getReason1ID());
                                }
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(assetBo.getSerialNo()));

                                if (assetBo.getConditionID() != null && !"null".equals(assetBo.getConditionID())) {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(assetBo.getConditionID()));
                                } else {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(""));
                                }
                                assetDetailValues.append(",");
                                if (MENU_ASSET.equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_INSTALL_DATE ? ((assetBo
                                                    .getInstallDate() == null || assetBo
                                                    .getInstallDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getInstallDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_SERVICE_DATE ? ((assetBo
                                                    .getServiceDate() == null || assetBo
                                                    .getServiceDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getServiceDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                } else if (MENU_POSM.equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_INSTALL_DATE ? ((assetBo
                                                    .getInstallDate() == null || assetBo
                                                    .getInstallDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getInstallDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_SERVICE_DATE ? ((assetBo
                                                    .getServiceDate() == null || assetBo
                                                    .getServiceDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getServiceDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                }
                                assetDetailValues
                                        .append(",");
                                assetDetailValues.append(assetBo.getAudit());

                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getProductId());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getCompetitorQty());
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()));
                                assetDetailValues.append(",");
                                if (MENU_POSM.equals(moduleName) && SHOW_LOCATION_POSM)
                                    assetDetailValues.append(assetBo.getLocationID());
                                else
                                    assetDetailValues.append(standardListBO.getListID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getGroupLevelId());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getExecutorQty());
                                assetDetailValues.append(",");
                                if (assetBo.getImgName() != null
                                        && !assetBo.getImgName().equals("")) {
                                    assetDetailValues.append(QT(assetBo.getImgName()));
                                } else {
                                    assetDetailValues.append(QT(""));
                                }


                                if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                    assetDetailValues.append("," + (assetBo.getAvailQty() > 0 || assetBo.getExecutorQty() > 0 ? productWeightAge : "0"));
                                    if (assetBo.getAvailQty() > 0 || assetBo.getExecutorQty() > 0)
                                        sum = sum + productWeightAge;
                                }

                                if (SHOW_LOCATION_POSM)
                                    assetDetailValues.append("," + assetBo.getTargetLocId());

                                if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM)
                                    assetDetailValues.append("," + assetBo.getmMappingID());

                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns,
                                        assetDetailValues.toString());

                                if (assetBo.getImageList().size() > 0) {
                                    for (String imageName : assetBo.getImageList()) {
                                        if (standardListBO.getListID().equals(String.valueOf(assetBo.getLocationID()))) {
                                            StringBuffer assetImgInofValues = new StringBuffer();
                                            assetImgInofValues.append(id);
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(assetBo.getAssetID());
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(QT(imageName));
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(assetBo.getProductId());
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append((MENU_POSM.equals(moduleName) && SHOW_LOCATION_POSM) ? assetBo.getLocationID() : standardListBO.getListID());
                                            db.insertSQL(DataMembers.tbl_AssetImgInfo,
                                                    AssetImageInfoColumns,
                                                    assetImgInofValues.toString());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (assetBo.getAvailQty() > 0
                                    || assetBo.getAudit() != 2 || assetBo.getExecutorQty() > 0
                                    || assetBo.getCompetitorQty() > 0) {
                                totalActualQty = totalActualQty + assetBo.getAvailQty();
                                assetDetailValues.append(id);
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAssetID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAvailQty());
                                assetDetailValues.append(",");
                                if (assetBo.getImageList().size() > 0
                                        && assetBo.getAvailQty() > 0)
                                    assetDetailValues.append(QT(assetBo.getImageList().get(0)));
                                else
                                    assetDetailValues.append(QT(""));


                                assetDetailValues.append(",");
                                assetDetailValues.append(0);
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(assetBo.getSerialNo()));
                                if (assetBo.getConditionID() != null && !"null".equals(assetBo.getConditionID())) {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(assetBo.getConditionID()));
                                } else {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(""));
                                }
                                assetDetailValues.append(",");
                                if (MENU_ASSET.equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_INSTALL_DATE ? ((assetBo
                                                    .getInstallDate() == null || assetBo
                                                    .getInstallDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getInstallDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_SERVICE_DATE ? ((assetBo
                                                    .getServiceDate() == null || assetBo
                                                    .getServiceDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getServiceDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                } else if (MENU_POSM.equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_INSTALL_DATE ? ((assetBo
                                                    .getInstallDate() == null || assetBo
                                                    .getInstallDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getInstallDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_SERVICE_DATE ? ((assetBo
                                                    .getServiceDate() == null || assetBo
                                                    .getServiceDate()
                                                    .length() == 0) ? DateTimeUtils
                                                    .now(DateTimeUtils.DATE_GLOBAL)
                                                    : (DateTimeUtils
                                                    .convertToServerDateFormat(
                                                            assetBo.getServiceDate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                }
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAudit());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getProductId());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getCompetitorQty());
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()));
                                assetDetailValues.append(",");
                                if (MENU_POSM.equals(moduleName) && SHOW_LOCATION_POSM)
                                    assetDetailValues.append(assetBo.getLocationID());
                                else
                                    assetDetailValues.append(standardListBO.getListID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getGroupLevelId());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getExecutorQty());
                                assetDetailValues.append(",");
                                if (assetBo.getImgName() != null
                                        && !assetBo.getImgName().equals("")) {
                                    assetDetailValues.append(QT(assetBo.getImgName()));
                                } else {
                                    assetDetailValues.append(QT(""));
                                }

                                if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                    assetDetailValues.append("," + (assetBo.getAvailQty() > 0 || assetBo.getExecutorQty() > 0? productWeightAge : "0"));
                                    if (assetBo.getAvailQty() > 0|| assetBo.getExecutorQty() > 0)
                                        sum = sum + productWeightAge;
                                }
                                if (SHOW_LOCATION_POSM)
                                    assetDetailValues.append("," + assetBo.getTargetLocId());

                                if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM)
                                    assetDetailValues.append("," + assetBo.getmMappingID());

                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns, assetDetailValues.toString());

                                if (assetBo.getImageList().size() > 0) {
                                    for (String imageName : assetBo.getImageList()) {
                                        if (standardListBO.getListID().equals(String.valueOf(assetBo.getLocationID()))) {
                                            StringBuffer assetImgInofValues = new StringBuffer();
                                            assetImgInofValues.append(id);
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(assetBo.getAssetID());
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(QT(imageName));
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(assetBo.getProductId());
                                            assetImgInofValues.append(",");
                                            assetImgInofValues.append(assetBo.getLocationID());
                                            db.insertSQL(DataMembers.tbl_AssetImgInfo,
                                                    AssetImageInfoColumns,
                                                    assetImgInofValues.toString());
                                        }
                                    }
                                }

                            }
                        }

                    }
                }
            }
            assetHeaderValues.append(",");
            assetHeaderValues.append(totalTarget);
            assetHeaderValues.append(",");
            assetHeaderValues.append(totalActualQty);
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(refId));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.retailerMasterBO.getDistributorId() + ""));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRidSF()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(mBusinessModel.getAppDataProvider().getUniqueId());

            db.insertSQL(DataMembers.tbl_AssetHeader, assetHeaderColumns,
                    assetHeaderValues.toString());

            if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                String moduleCode = ((MENU_ASSET.equals(moduleName)) ? DataMembers.FIT_ASSET : DataMembers.FIT_POSM);
                mBusinessModel.calculateFitscoreandInsert(db, sum, moduleCode);
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Ordered asset record set to AssetTrackingBO object
     *
     * @param assetID   Asset Id
     * @param qty       Qty
     * @param imageName image Name
     * @param mReasonId reason Id
     * @param serialNo  serial Number
     * @param imgName   image Name
     */
    private void setAssetDetails(Context mcontext, String menuName, int assetID, int qty, String imageName,
                                 String mReasonId, String serialNo,
                                 String conditionId, String installDate, String serviceDate, int audit,
                                 int pid, int compQty, int locId, int isExec, String imgName,
                                 String mappingId, String moduleName) {

        AssetTrackingBO assetBO = null;
        for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
            ArrayList<AssetTrackingBO> mAssetTrackingList = standardListBO.getAssetTrackingList();
            if (standardListBO.getListID().equals(Integer.toString(locId))) {
                if (mAssetTrackingList != null) {
                    for (AssetTrackingBO assetBOList :  mAssetTrackingList) {
                        if (assetBOList.getProductId() == pid &&
                                assetBOList.getAssetID() == assetID &&
                                assetBOList.getSerialNo().equalsIgnoreCase(serialNo)) {
                            if (MENU_POSM.equals(moduleName) && SHOW_ATTR_POSM) {
                                if (assetBOList.getmMappingID().equalsIgnoreCase(mappingId))
                                    assetBO = assetBOList;
                            } else
                                assetBO = assetBOList;
                            if (assetBO != null) {
                                assetBO.setAvailQty(qty);
                                assetBO.setImageName(imageName);
                                assetBO.setReason1ID(mReasonId);
                                assetBO.setConditionID(conditionId);
                                assetBO.setInstallDate(installDate);
                                assetBO.setServiceDate(serviceDate);
                                assetBO.setAudit(audit);
                                if (!"null".equals(serialNo)) {
                                    assetBO.setSerialNo(serialNo);
                                } else {
                                    assetBO.setSerialNo(Integer.toString(0));
                                }
                                assetBO.setCompetitorQty(compQty);
                                assetBO.setExecutorQty(isExec);
                                assetBO.setImgName(imgName);
                                if (SHOW_LOCATION_POSM)
                                    assetBO.setLocationID(locId);

                                if (menuName.equals(MENU_ASSET))
                                    assetBO.setImageList(getImagesList(mcontext, assetID, locId));
                                else
                                    assetBO.setImageList(getPosmImagesList(mcontext, assetID, pid, locId));
                            }

                        }
                    }

                }
            }
            standardListBO.setAssetTrackingList(mAssetTrackingList);
        }
    }

    /**
     * Method to check Asset already taken or not
     *
     * @return true if asset already taken
     */
    public boolean hasAssetTaken() {
        for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
            mAssetTrackingList = standardListBO.getAssetTrackingList();
            if (mAssetTrackingList != null) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (mBusinessModel.configurationMasterHelper.isAuditEnabled()) {
                        if (((assetBO.getAvailQty() > 0
                                || assetBO.getCompetitorQty() > 0
                                || assetBO.getExecutorQty() > 0)
                                && assetBO.getAudit() != 2)
                                || assetBO.getAudit() != 2) {
                            return true;
                        }
                    } else if (assetBO.getAvailQty() > 0 || assetBO.getAudit() != 2 || assetBO.getCompetitorQty() > 0 || assetBO.getExecutorQty() > 0) {
                        return true;
                    } else if (assetBO.getReason1ID() != null) {
                        if (!assetBO.getReason1ID().equals(Integer.toString(0))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String QT(String data) {
        return "'" + data + "'";
    }

    /**
     * Method to check the movement Asset in sql table
     */
    public ArrayList<String> getAssetMovementDetails(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        ArrayList<String> retailerMovedData = new ArrayList<>();
        Cursor c = db.selectSQL("SELECT DISTINCT AssetId from " + DataMembers.tbl_AssetAddDelete + " where flag='M'");
        if (c != null)
            while (c.moveToNext()) {
                retailerMovedData.add(c.getString(0));
            }
        return retailerMovedData;
    }

    public ArrayList<String> getImagesList(Context mContext, int assetId, int locId) {
        ArrayList<String> imageList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ImageName from AssetImageDetails "
                    + " where AssetID = " + assetId + " AND LocId = " + locId
                    + " AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    imageList.add(c.getString(0));
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageList;
    }

    public ArrayList<String> getPosmImagesList(Context mContext, int assetId, int productID, int locId) {
        ArrayList<String> imageList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ImageName from AssetImageDetails "
                    + " where AssetID = " + assetId + " AND LocId = " + locId + " AND Pid = " + productID
                    + " AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    imageList.add(c.getString(0));
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageList;
    }

    public void deleteImageProof(Context mContext, String ImageName) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            db.deleteSQL(DataMembers.tbl_AssetImgInfo, "ImageName="
                    + QT(ImageName), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public void deleteServiceTable(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            db.deleteSQL("AssetServiceRequest", "retailerid=" + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()), false);
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();
    }

    public void saveAssetServiceDetails(Context mContext, String assetId, String serialNo, String mReasonID, String moduleName) {

        if (mUniqueSerialNo == null)
            mUniqueSerialNo = new HashMap<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String addAssetColumns = "Uid,date,AssetId,serialNum,reasonid,retailerid";

            String assetAddAndDeleteValues = id + ","
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + QT(assetId) + "," + QT(serialNo) + ","
                    + QT(mReasonID) + ","
                    + QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

            db.insertSQL(DataMembers.tbl_AssetService, addAssetColumns,
                    assetAddAndDeleteValues);

            //add serial no for uniqueness
            mUniqueSerialNo.put(serialNo, serialNo);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void getAssetService(Context mContext, String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;
        AssetTrackingBO assetBO;

        assetServiceList = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();


            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c0 = db.selectSQL(query1);
            if (c0.getCount() > 0) {
                while (c0.moveToNext()) {
                    typeListId = c0.getInt(0);
                }
            }
            mBusinessModel.productHelper.getRetailerlevel(moduleName);


            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.PosmId,P.Posmdesc,SBD.SerialNO,SBD.Mappingid,SBD.Productid  from PosmMaster P  inner join POSMCriteriaMapping SBD on P.PosmID=SBD.posmid where ");
            sb.append("(SBD.SerialNO  in (select distinct serialNum from AssetAddDelete AAD where flag!='D' and AAD.TypeLovId=");
            sb.append(typeListId);
            sb.append(") or SBD.SerialNO not in (select distinct serialNum from AssetAddDelete AAD1 where AAD1.TypeLovid=");
            sb.append(typeListId);
            sb.append("))");
            sb.append(" and SBD.TypeLovId=(select listid from StandardListMaster where ListCode=");
            sb.append(QT(type));
            sb.append(" and ListType='SBD_TYPE') ");
            sb.append(" and AccountId in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getAccountid() + ")");
            sb.append(" and Retailerid in(0,");
            sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ")");
            sb.append(" and Classid in (0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getClassid() + ")");
            sb.append(" and Locid in(0,");
            sb.append(mBusinessModel.productHelper.getMappingLocationId(mBusinessModel.productHelper.locid, mBusinessModel.getRetailerMasterBO().getLocationId()));
            sb.append(")");
            sb.append(" and (Channelid in(0,");
            sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid() + ")");
            sb.append(" OR Channelid in (0,");
            sb.append(mBusinessModel.channelMasterHelper.getChannelHierarchy(mBusinessModel.getRetailerMasterBO().getSubchannelid(), mContext));
            sb.append(")) GROUP BY RetailerId,AccountId,Channelid,Locid,Classid,SBD.Productid,SBD.PosmId,SBD.SerialNO ORDER BY RetailerId,AccountId,Channelid,Locid,Classid");


            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setPOSM(c.getString(0));
                    assetBO.setPOSMName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2)) && !"".equals(c.getString(2))) {
                        assetBO.setSNO(c.getString(2));


                    } else {
                        assetBO.setSNO("0");
                    }
                    assetBO.setNewInstallDate(" ");
                    assetBO.setFlag("N");
                    assetBO.setmMappingID(c.getString(3));
                    assetBO.setBrand(c.getString(4));
                    assetServiceList.add(assetBO);
                }
            }
            String sb1 = "select distinct  AssetId,P.Posmdesc,serialNum,installdate,flag ,uid,Productid  from PosmMaster P  inner  join AssetAddDelete AAD on P.PosmId=AAD.AssetId where flag!='D'  and retailerid=" +
                    QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + " and AAD.TypeLovId=" + typeListId;

            Cursor c1 = db.selectSQL(sb1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setPOSM(c1.getString(0));
                    assetBO.setPOSMName(c1.getString(1));
                    if ("null".equals(c1.getString(2)) || "".equals(c1.getString(2))) {

                        assetBO.setSNO("0");
                    } else {
                        assetBO.setSNO(c1.getString(2));
                    }

                    assetBO.setNewInstallDate(c1.getString(3));
                    assetBO.setFlag("Y");
                    assetBO.setmMappingID(" ");
                    assetBO.setBrand(c1.getString(6));
                    assetServiceList.add(assetBO);
                }
            }


            String query = "select   AAD.AssetId,AAD.serialNum,AAD.reasonid  from PosmMaster P  inner  join AssetServiceRequest AAD on P.PosmId=AAD.AssetId where retailerid=" +
                    QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

            Cursor c2 = db.selectSQL(query);
            if (c2.getCount() > 0) {
                while (c2.moveToNext()) {
                    int reasonId = c2.getInt(c2.getColumnIndex("reasonid"));
                    String serialNum = c2.getString(c2.getColumnIndex("serialNum"));
                    int assetId = c2.getInt(c2.getColumnIndex("AssetId"));

                    for (int i = 0; i < assetServiceList.size(); i++) {
                        AssetTrackingBO assetTrackingBO = assetServiceList.get(i);
                        if (assetTrackingBO.getPOSM().equalsIgnoreCase(String.valueOf(assetId)) && serialNum.equalsIgnoreCase(assetTrackingBO.getSNO())) {
                            assetServiceList.get(i).setReason1ID(String.valueOf(reasonId));
                            assetServiceList.get(i).setSelectedReason(true);
                        }
                    }
                }
            }
            setAssetServiceList(assetServiceList);

            c.close();
            c1.close();
            c2.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void setAssetServiceList(Vector<AssetTrackingBO> assetServiceList) {
        this.assetServiceList = assetServiceList;
    }

    public Vector<AssetTrackingBO> getAssetServiceList() {
        return assetServiceList;
    }

    /**
     * Method that to download POSM Details by Attribute from SQLite
     *
     * @param mContext module name
     */
    private void downloadPosmMaster(Context mContext) {

        ArrayList<String> mappingIDs;

        mAssetTrackingList = new ArrayList<>();
        mUniqueSerialNo = new HashMap<>();

        AssetTrackingBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            mBusinessModel.productHelper.getRetailerlevel(MENU_POSM);
            mappingIDs = getPosmId(db, mContext);

            sb.append("select Distinct P.PosmId,P.Posmdesc,PGCM.Qty,PPM.Productid,PCM.InStoreLocId,SDM.listname as locname,PM.ParentHierarchy as ParentHierarchy,PCM.Id as MappingId,PCM.MappingSetId as GroupID from PosmMaster P  ");
            sb.append("inner join POSMGroupCriteriaMappingV2 PGCM on P.PosmID=PGCM.PosmId ");
            sb.append("inner join POSMCriteriaMappingV2 PCM on PCM.Id=PGCM.Id and PCM.MappingSetId=PGCM.MappingSetId ");
            sb.append("left join POSMProductMapping PPM on P.PosmID=PPM.posmid ");
            sb.append("left join Standardlistmaster SDM on SDM.listid=PCM.InStoreLocId and SDM.ListType='PL' ");
            sb.append("left join ProductMaster PM on PM.PID=PPM.Productid ");
            sb.append(" GROUP BY PCM.RetailerId,PCM.AccountId,PCM.Channelid,PCM.LocationId,PPM.Productid,PGCM.PosmId " +
                    " ORDER BY PCM.RetailerId,PCM.AccountId,PCM.Channelid,PCM.LocationId");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (mappingIDs.contains("/" + c.getString(c.getColumnIndex("MappingId")) + "" + c.getString(c.getColumnIndex("GroupID")) + "/")) {
                        assetTrackingBO = new AssetTrackingBO();
                        assetTrackingBO.setAssetID(c.getInt(0));
                        assetTrackingBO.setAssetName(c.getString(1));

                        assetTrackingBO.setTarget(c.getInt(2));
                        assetTrackingBO.setProductId(c.getInt(3));

                        assetTrackingBO.setGroupLevelName("");
                        assetTrackingBO.setGroupLevelId(0);

                        assetTrackingBO.setTargetLocId(c.getInt(4));
                        assetTrackingBO.setLocationName(c.getString(c.getColumnIndex("locname")));
                        assetTrackingBO.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));
                        assetTrackingBO.setmMappingID(c.getString(c.getColumnIndex("MappingId")));

                        mAssetTrackingList.add(assetTrackingBO);
                    }

                }

            }


            if (mAssetTrackingList != null && mAssetTrackingList.size() > 0) {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAssetTrackingList.size());
                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        clonedList.add(new AssetTrackingBO(assetBO));
                    }
                    standardListBO.setAssetTrackingList(clonedList);
                }

            } else {
                for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
                    standardListBO.getAssetTrackingList().clear();
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }

    private ArrayList<String> getPosmId(DBUtil db, Context mContext) {
        ArrayList<String> mappingsetId = new ArrayList<>();
        ArrayList<String> attrmappingsetId = new ArrayList<>();


        String attrQuery = "Select distinct Id,MappingSetId from POSMCriteriaAttributesMappingV2 PCA"
                + " inner join RetailerAttribute RA on RA.AttributeId = PCA.RetailerAttibuteId and RA.RetailerId ="
                + StringUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

        Cursor c = db.selectSQL(attrQuery);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                attrmappingsetId.add("/" + c.getInt(0) + "" + c.getInt(1) + "/");
            }
        }

        String criteriaQuery = "Select Distinct pcm.Id,pcm.MappingSetId,pcm.AccountId,pcm.Retailerid,pcm.LocationId,pcm.ChannelId,IFNULL(pca.Id,0) as attrMapped from POSMCriteriaMappingV2 pcm "
                + " Left Join POSMCriteriaAttributesMappingV2 pca on pca.Id = pcm.Id and pca.MappingSetId = pcm.MappingSetId "
                + "where AccountId in(0," + mBusinessModel.getRetailerMasterBO().getAccountid() + ") and "
                + " Retailerid in(0," + StringUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ") and "
                + " LocationId in(0," + mBusinessModel.productHelper.getMappingLocationId(mBusinessModel.productHelper.locid, mBusinessModel.getRetailerMasterBO().getLocationId()) + ") and "
                + " ChannelId in(0," + mBusinessModel.getRetailerMasterBO().getSubchannelid() + ") OR "
                + " ChannelId in (0," + StringUtils.QT(mBusinessModel.channelMasterHelper.getChannelHierarchy(mBusinessModel.getRetailerMasterBO().getSubchannelid(), mContext)) + ")";

        c = db.selectSQL(criteriaQuery);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {

                // only mapped through attribute
                if (c.getInt(2) == 0 && c.getInt(3) == 0 && c.getInt(4) == 0 && c.getInt(5) == 0) {
                    if (attrmappingsetId.contains("/" + c.getInt(0) + "" + c.getInt(1) + "/"))
                        mappingsetId.add("/" + c.getInt(0) + "" + c.getInt(1) + "/");
                } // only Channel mapped
                else if (c.getInt(6) == 0) {
                    mappingsetId.add("/" + c.getInt(0) + "" + c.getInt(1) + "/");
                } // both channel and attr mapped AND condition
                else if (c.getInt(6) > 0 && attrmappingsetId.contains("/" + c.getInt(0) + "" + c.getInt(1) + "/")) {
                    mappingsetId.add("/" + c.getInt(0) + "" + c.getInt(1) + "/");
                }
            }
        }


        c.close();

        return mappingsetId;

    }
}


