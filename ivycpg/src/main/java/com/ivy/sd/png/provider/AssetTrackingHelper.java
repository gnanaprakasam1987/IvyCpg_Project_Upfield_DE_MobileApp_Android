package com.ivy.sd.png.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.AssetAddDetailBO;
import com.ivy.sd.png.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Vector;

@SuppressLint("UseSparseArrays")
public class AssetTrackingHelper {
    private final Context context;
    private final BusinessModel mBusinessModel;
    private static AssetTrackingHelper instance = null;
    private AssetTrackingBO mAssetTrackingBO;

    /**
     * This ArrayList contains downloaded AssetTracking records
     */
    private ArrayList<AssetTrackingBO> mAssetTrackingList = new ArrayList<>();

    /**
     * This ArrayList contains downloaded AssetReason records
     */
    private Vector<AssetTrackingBO> mAddRemoveAssets = null;

    private Vector<AssetAddDetailBO> mAssetSpinner = null;
    private Vector<AssetAddDetailBO> mBrandSpinner = null;

    private ArrayList<ReasonMaster> mAssetReasonList = new ArrayList<>();
    /**
     * This ArrayList contains downloaded AssetRemarks records
     */
    private ArrayList<ReasonMaster> mAssetRemarkList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetConditionList = new ArrayList<>();

    /**
     * This ArrayList contains download POSM reason,condition and remarks records
     */
    private ArrayList<ReasonMaster> mPOSMReasonList = new ArrayList<>();
    private ArrayList<ReasonMaster> mPOSMConditionList = new ArrayList<>();
    private ArrayList<ReasonMaster> mPOSMRemarkList = new ArrayList<>();


    /**
     * This String used to store captured images asset id
     */
    public int mSelectedAssetID = 0;
    /**
     * This String used to store captured image's image Name
     */
    public String mSelectedImageName = "";
    public String mSelectedSerial = "";

    //Column Configuration - AT01
    private static final String CODE_ASSET_COLUMNS = "AT01";
    public boolean SHOW_ASSET_TARGET;
    public boolean SHOW_ASSET_QTY;
    public boolean SHOW_ASSET_REASON;
    public boolean SHOW_ASSET_PHOTO;
    public boolean SHOW_ASSET_CONDITION;

    public boolean SHOW_ASSET_INSTALL_DATE;
    public boolean SHOW_ASSET_SERVICE_DATE;
    public boolean SHOW_COMPETITOR_QTY;
    public boolean SHOW_ASSET_GRP;
    public boolean SHOW_ASSET_EXECUTED;

    private static final String CODE_ASSET_BARCODE = "AT02";
    public boolean SHOW_ASSET_BARCODE;

    private static final String CODE_ASSET_ADD = "AT03";
    public boolean SHOW_ADD_NEW_ASSET;

    private static final String CODE_REMOVE_ASSET = "AT04";
    public boolean SHOW_REMOVE_ASSET;

    private static final String CODE_SHOW_ALL = "AT05";
    public boolean SHOW_ASSET_ALL;

    private static final String CODE_REMARKS_ASSET = "AT06";
    public boolean SHOW_REMARKS_ASSET;

    private static final String CODE_ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = "AT08";
    public boolean ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK;

    private static final String CODE_MOVE_ASSET = "AT09";
    public boolean SHOW_MOVE_ASSET;

    /**
     * Reason Type - Std List Code
     */
    private static final String ASSET_REASON = "AR";
    private static final String ASSET_REMARK = "ARR";
    private static final String ASSET_CONDITION = "CD";

    private static final String POSM_REASON = "POSMR";
    private static final String POSM_CONDITION = "POSMCD";

    private static final String CODE_POSM_COLUMNS = "POSM01";
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

    private static final String CODE_POSM_BARCODE = "POSM02";
    public boolean SHOW_POSM_BARCODE;

    /*private static final String CODE_POSM_ADD = "POSM03";
    private boolean SHOW_ADD_NEW_POSM;*/

   /* private static final String CODE_REMOVE_POSM = "POSM04";
    private boolean SHOW_REMOVE_POSM;*/

    private static final String CODE_SHOW_ALL_POSM = "POSM05";
    public boolean SHOW_POSM_ALL;

    private static final String CODE_SHOW_POSM_REMARKS = "POSM06";
    public boolean SHOW_REMARKS_POSM;

    private static final String MERCH = "MERCH";
    private static final String MENU_ASSET = "MENU_ASSET";
    private static final String MERCH_INIT = "MERCH_INIT";

    private AssetTrackingHelper(Context context) {
        this.context = context;
        this.mBusinessModel = (BusinessModel) context;
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
        if (instance == null) {
            instance = new AssetTrackingHelper(context);
        }
        return instance;
    }

    public void loadDataForAssetPOSM(String mMenuCode) {
        if (mBusinessModel.configurationMasterHelper
                .downloadFloatingSurveyConfig(mMenuCode)) {
            mBusinessModel.mSurveyHelperNew.setFromHomeScreen(false);
            mBusinessModel.mSurveyHelperNew.downloadModuleId("STANDARD");
            mBusinessModel.mSurveyHelperNew.downloadQuestionDetails(mMenuCode);
            mBusinessModel.mSurveyHelperNew.loadSurveyAnswers(0);
            mBusinessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
        }

        if (MENU_ASSET.equalsIgnoreCase(mMenuCode))
            loadAssetConfigs();
        else if ("MENU_POSM".equalsIgnoreCase(mMenuCode) || "MENU_POSM_CS".equalsIgnoreCase(mMenuCode))
            loadPOSMConfigs();

        mBusinessModel.productHelper.downloadInStoreLocations();

        mBusinessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);

        downloadAssetMaster(mMenuCode);

        loadAssetData(mBusinessModel
                .getRetailerMasterBO().getRetailerID(), mMenuCode);
    }

    private void loadAssetConfigs() {
        try {
            SHOW_ASSET_TARGET = false;
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

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE menu_type = 'MENU_ASSET' AND flag='1'";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_ASSET_COLUMNS)) {
                        if (c.getString(1) != null) {
                            String codeSplit[] = c.getString(1).split(",");
                            for (String temp : codeSplit) {
                                switch (temp) {
                                    case "TGT":
                                        SHOW_ASSET_TARGET = true;
                                        break;
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
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_ASSET_REASON)
                downloadAssetPOSMReason(ASSET_REASON);
            if (SHOW_ASSET_CONDITION)
                downloadAssetPOSMReason(ASSET_CONDITION);
            if (SHOW_REMARKS_ASSET)
                downloadAssetPOSMReason(ASSET_REMARK);

        } catch (Exception e) {
            Commons.printException("loadAssetConfigs " + e);
        }
    }

    private void loadPOSMConfigs() {
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

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE menu_type = 'MENU_POSM' AND flag='1'";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_POSM_COLUMNS)) {
                        if (c.getString(1) != null) {
                            String codeSplit[] = c.getString(1).split(",");
                            for (String temp : codeSplit) {
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
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_POSM_BARCODE))
                        SHOW_POSM_BARCODE = true;
                   /* else if (c.getString(0).equalsIgnoreCase(CODE_POSM_ADD))
                        SHOW_ADD_NEW_POSM = true;*/
                    /*else if (c.getString(0).equalsIgnoreCase(CODE_REMOVE_POSM))
                        SHOW_REMOVE_POSM = true;*/
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_ALL_POSM))
                        SHOW_POSM_ALL = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_POSM_REMARKS))
                        SHOW_REMARKS_POSM = true;
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_POSM_REASON)
                downloadAssetPOSMReason(POSM_REASON);
            if (SHOW_POSM_CONDITION)
                downloadAssetPOSMReason(POSM_CONDITION);

        } catch (Exception e) {
            Commons.printException("loadPOSMConfigs " + e);
        }
    }

    /**
     * Method that to download Asset Details from SQLite
     *
     * @param moduleName module name
     */

    private void downloadAssetMaster(String moduleName) {
        ArrayList<AssetTrackingBO> mAllAssetTrackingList=null;

        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        mAssetTrackingList = new ArrayList<>();

        AssetTrackingBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            int level = mBusinessModel.productHelper.getRetailerlevel(moduleName);
            sb.append("select Distinct P.PosmId,P.Posmdesc,SBD.SerialNO,SBD.Target,SBD.Productid,SLM.listname,SLM.listid,SBD.NfcTagId from PosmMaster P  ");
            sb.append("inner join POSMCriteriaMapping SBD on P.PosmID=SBD.posmid ");
            sb.append("left join Standardlistmaster SLM on SLM.listid=SBD.PosmGroupLovId and ListType='POSM_GROUP_TYPE' ");
            sb.append("where  SBD.TypeLovId=(select listid from StandardListMaster where ListCode=");
            sb.append(mBusinessModel.QT(type));
            sb.append(" and ListType='SBD_TYPE') ");
            String allMasterSb = sb.toString();
            if (level == 1) {
                // account mapping
                sb.append(" and AccountId =");
                sb.append(mBusinessModel.getRetailerMasterBO().getAccountid());
            } else if (level == 2) {
                // retailer mapping
                sb.append(" and Retailerid=");
                sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
            } else if (level == 3) {
                // Class mapping
                sb.append(" and Classid = ");
                sb.append(mBusinessModel.getRetailerMasterBO().getClassid());
            } else if (level == 4) {
                // Location mapping
                sb.append(" and Locid in (");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(")");
            } else if (level == 5) {
                // Channel Mapping
                sb.append(" and (Channelid =");
                sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getChannelidForScheme(mBusinessModel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");
            } else if (level == 6) {

                // Location Mapping and Channel Mapping
                sb.append(" and Locid in(");
                sb.append(mBusinessModel.productHelper.getMappingLocationId(mBusinessModel.productHelper.locid, mBusinessModel.getRetailerMasterBO().getLocationId()));
                sb.append(")");
                sb.append(" and (Channelid =");
                sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getChannelidForScheme(mBusinessModel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");
            }

            if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                sb.append(" and (SBD.Productid = " + mBusinessModel.productHelper.getmSelectedGlobalProductId() + " OR SBD.Productid = 0 )");
                allMasterSb = allMasterSb + ("and (SBD.Productid = " + mBusinessModel.productHelper.getmSelectedGlobalProductId() + " OR SBD.Productid = 0 )");
            }

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
                    assetTrackingBO.setProductid(c.getInt(4));
                    if (c.getString(5) != null && !"null".equals(c.getString(5))) {
                        assetTrackingBO.setGroupLevelName(c.getString(5));
                        assetTrackingBO.setGroupLevelId(c.getInt(6));
                    } else {
                        assetTrackingBO.setGroupLevelName("");
                        assetTrackingBO.setGroupLevelId(0);
                    }

                    assetTrackingBO.setNFCTagId(c.getString(c.getColumnIndex("NfcTagId")));

                    mAssetTrackingList.add(assetTrackingBO);

                }

            }
            if (mAssetTrackingList != null) {
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
            if (c1.getCount() > 0) {
                mAllAssetTrackingList=new ArrayList<>();
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
                    assetTrackingBO.setProductid(c1.getInt(4));
                    if (c1.getString(5) != null && !"null".equals(c1.getString(5))) {
                        assetTrackingBO.setGroupLevelName(c1.getString(5));
                        assetTrackingBO.setGroupLevelId(c1.getInt(6));
                    } else {
                        assetTrackingBO.setGroupLevelName("");
                        assetTrackingBO.setGroupLevelId(0);
                    }

                    assetTrackingBO.setNFCTagId(c1.getString(c1.getColumnIndex("NfcTagId")));

                    mAllAssetTrackingList.add(assetTrackingBO);

                }

            }
            if (mAllAssetTrackingList != null) {
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
            Commons.printException("" + e);
            db.closeDB();

        }
    }

    /**
     * Method that to get loaded data from SQLite table
     *
     * @param mRetailerId Retailer ID
     * @param moduleName Module Name
     */

    private void loadAssetData(String mRetailerId, String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String sb = "select uid, IFNULL(remark,'') from AssetHeader where retailerid=" + QT(mRetailerId) + " and TypeLovId=" +
                    "(select listid from StandardListMaster where ListCode=" + mBusinessModel.QT(type) + " and ListType='SBD_TYPE') " +
                    "and (upload='N' OR refid!=0)";

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

            String sb2 = "select assetid,availqty,imagename,reasonid,SerialNumber,conditionId,installdate,servicedate,isAudit,Productid,CompQty,Locid,PosmGroupLovId,isExecuted,imgName  from assetDetail where uid=" +
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


                    setAssetDetails(
                            mAssetId,
                            qty,
                            imageName,
                            reasonId,

                            serialNo,
                            conditionId,
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(6),
                                    ConfigurationMasterHelper.outDateFormat),
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(7),
                                    ConfigurationMasterHelper.outDateFormat), audit, pid, compQty, locId, isExecuted, detailCursor.getString(detailCursor.getColumnIndex("imgName")));
                }
            }
            detailCursor.close();
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
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
    private void downloadAssetPOSMReason(String category) {
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
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            Cursor c = db.selectSQL(mBusinessModel.reasonHelper.getReasonFromStdListMaster(category));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    reasonBO = new ReasonMaster();
                    reasonBO.setReasonID(c.getString(0));
                    reasonBO.setReasonDesc(c.getString(1));
                    reasonBO.setConditionID(c.getString(0));
                    // if category is AR, Asset reason downloaded and category
                    // is ARR, Asset remarks downloaded
                    // from reason master table

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

    public void downloadAssetsPosm(String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = "CMP";
        else if ("MENU_POSM".equals(moduleName))
            type = "CMN";

        AssetAddDetailBO assetBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String sb = "select distinct  PosmId,Posmdesc from PosmMaster where" +
                    " TypeLovId=(select listid from StandardListMaster where parentid= " +
                    "(select listid from StandardListmaster where ListCode=" + QT(type) + " and ListType='POSM_TYPE'))";


            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                mAssetSpinner = new Vector<>();
                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setMposmid(c.getString(0));
                    assetBO.setMposmdesc(c.getString(1));

                    mAssetSpinner.add(assetBO);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public Vector<String> getAssetPosmNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = mAssetSpinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = mAssetSpinner.get(i);
                data.add(brand.getMposmdesc());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    public Vector<String> getAssetBrandNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = mBrandSpinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = mBrandSpinner.get(i);
                data.add(brand.getMassetbrandname());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    public String getAssetPosmIds(String mAssetPosmName) {
        AssetAddDetailBO brand;

        try {
            int siz = mAssetSpinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = mAssetSpinner.get(i);
                Commons.print("brand.getMposmdesc()="
                        + brand.getMposmdesc() + "," + mAssetPosmName);
                if (brand.getMposmdesc().equals(mAssetPosmName)) {

                    return brand.getMposmid();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return mAssetPosmName;
    }

    public String getAssetBrandIds(String mAssetBrandName) {
        AssetAddDetailBO brand;

        try {
            int siz = mBrandSpinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = mBrandSpinner.get(i);
                if (brand.getMassetbrandname().equals(mAssetBrandName)) {
                    return brand.getAssetbrandid();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return mAssetBrandName;
    }

    public void downloadAssetBrand(String brandPosm) {

        AssetAddDetailBO assetBO;
        mBrandSpinner = new Vector<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String sb = "SELECT PM.Pid,PM.PName FROM ProductMaster PM INNER JOIN POSMProductMapping PO ON PM.Pid = PO.Productid WHERE PO.PosmID =" + QT(brandPosm);

            Cursor c = db.selectSQL(sb);

            if (c.getCount() > 0) {

                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setAssetbrandid(c.getString(0));
                    assetBO.setMassetbrandname(c.getString(1));

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

    public void lodAddRemoveAssets(String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;
        AssetTrackingBO assetBO;

        mAddRemoveAssets = new Vector<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
            int level = mBusinessModel.productHelper.getRetailerlevel(moduleName);


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

            if (level == 2) {
                // retailer mapping
                sb.append(" and Retailerid=");
                sb.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
            } else if (level == 4) {
                // Location mapping
                sb.append(" and Locid in(");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(")");


            } else if (level == 5) {
                // Channel Mapping
                sb.append(" and (Channelid =");
                sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getChannelidForScheme(mBusinessModel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");

            } else if (level == 6) {

                // Location Mapping and Channel Mapping
                sb.append(" and Locid in(");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(" and (Channelid =");
                sb.append(mBusinessModel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(mBusinessModel.schemeDetailsMasterHelper.getChannelidForScheme(mBusinessModel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");
            }


            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setMposm(c.getString(0));
                    assetBO.setMposmname(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2)) && !"".equals(c.getString(2))) {
                        assetBO.setMsno(c.getString(2));


                    } else {
                        assetBO.setMsno("0");
                    }
                    assetBO.setMnewinstaldate(" ");
                    assetBO.setMflag("N");
                    assetBO.setMsbdid(c.getString(3));
                    assetBO.setMbrand(c.getString(4));
                    mAddRemoveAssets.add(assetBO);
                }
            }
            String sb1 = "select distinct  AssetId,P.Posmdesc,serialNum,installdate,flag ,uid,Productid  from PosmMaster P  inner  join AssetAddDelete AAD on P.PosmId=AAD.AssetId where flag!='D'  and retailerid=" +
                    QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + " and AAD.TypeLovId=" + typeListId;

            Cursor c1 = db.selectSQL(sb1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    assetBO = new AssetTrackingBO();
                    assetBO.setMposm(c1.getString(0));
                    assetBO.setMposmname(c1.getString(1));
                    if ("null".equals(c1.getString(2)) || "".equals(c1.getString(2))) {

                        assetBO.setMsno("0");
                    } else {
                        assetBO.setMsno(c1.getString(2));
                    }

                    assetBO.setMnewinstaldate(c1.getString(3));
                    assetBO.setMflag("Y");
                    assetBO.setMsbdid(" ");
                    assetBO.setMbrand(c1.getString(6));
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

    /**
     * Method return reason remarks arrayList
     *
     * @return ArrayList<ReasonMaster>
     */

    public ArrayList<ReasonMaster> getAssetRemarksList() {
        if (mAssetRemarkList != null) {
            return mAssetRemarkList;
        }
        return new ArrayList<>();
    }

    public ArrayList<ReasonMaster> getAssetConditionList() {
        if (mAssetConditionList != null) {
            return mAssetConditionList;
        }
        return new ArrayList<>();
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
     * Method return reason remarks arrayList
     *
     * @return ArrayList<ReasonMaster>
     */

    public ArrayList<ReasonMaster> getPOSMRemarksList() {
        if (mPOSMRemarkList != null) {
            return mPOSMRemarkList;
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
     * @param reasonId ReasonId
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

    /**
     * Method return the correct position to selected conditionId
     *
     * @param conditionId Condition Id
     * @param reasonList reason List
     * @return integer Index
     */
    public int getConditionItemIndex(String conditionId, ArrayList<ReasonMaster> reasonList) {
        int size = reasonList.size();

        for (int i = 0; i < size; i++) {
            ReasonMaster reasonBO = reasonList.get(i);
            if (reasonBO.getConditionID().equals(conditionId)) {
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
    public void deleteImageName(String imgName) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        db.updateSQL("UPDATE AssetDetail SET  imagename =" + QT("")
                + "WHERE imagename LIKE" + QT(imgName + "%"));
    }

    /**
     * Method to save Asset Details in sql table
     */
    public void saveAssetAddAndDeleteDetails(String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if ("MENU_POSM".equals(moduleName))
            type = MERCH_INIT;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            AssetTrackingBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,retailerid,AssetId,serialNum,productid,installdate,creationdate,TypeLovId,reasonid,remarks";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(assets.getMposm()) + "," + QT(assets.getMsno()) + ","
                    + QT(assets.getMbrand()) + ","
                    + QT(DateUtil.convertToServerDateFormat(
                    assets.getMnewinstaldate(),
                    ConfigurationMasterHelper.outDateFormat))
                    + "," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + typeListId + "," +
                    QT(assets.getMreasonId()) + "," + QT(assets.getMremarks());

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Method to save Asset Movement Details in sql table
     */
    public void saveAssetMovementDetails(String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if ("MENU_POSM".equals(moduleName))
            type = MERCH_INIT;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            AssetTrackingBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,retailerid,AssetId,serialNum,productid,creationdate,flag,TypeLovId,reasonid,remarks,toRetailerId";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(assets.getMposm()) + "," + QT(assets.getMsno()) + ","
                    + QT(assets.getMbrand()) + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + QT("M") + "," + typeListId + "," +
                    QT(assets.getMreasonId()) + "," + QT(assets.getMremarks()) + "," + QT(assets.getmToRetailerId());

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void saveAddAndDeleteDetails(String posmId, String mSno,
                                        String mSbdId, String mBrandId, String reasonId, String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else if ("MENU_POSM".equals(moduleName))
            type = MERCH_INIT;


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String addAssetColumns = "uid,retailerid,AssetId,serialNum,creationdate,flag,mappingid,Productid,TypeLovId,reasonid";

            String assetAddAndDeleteValues = id + "," + QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(posmId) + "," + QT(mSno) + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + QT("D") + ","
                    + QT(mSbdId) + "," + QT(mBrandId) + "," + typeListId + "," + QT(reasonId);

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addAssetColumns,
                    assetAddAndDeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void deletePosmDetails(String mSno) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_AssetAddDelete, "serialNum ="
                    + QT(mSno), false);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public boolean isExistingRetailerSno(String mSno) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
    public boolean isExistingAssetInRetailer(String serialNum) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

    public void saveAsset(String moduleName) {
        String type = "";
        if (MENU_ASSET.equals(moduleName)) {
            type = MERCH;
        } else if ("MENU_POSM".equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
            type = MERCH_INIT;
        }
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            String refId = "0";
            int typeListId = 0;
            String query1 = "select listid from StandardListMaster where ListCode=" + QT(type) + " and ListType='SBD_TYPE'";
            Cursor c1 = db.selectSQL(query1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    typeListId = c1.getInt(0);
                }
            }
            String query = "select uid,refid from AssetHeader where retailerid ="
                    + QT(mBusinessModel.getRetailerMasterBO().getRetailerID())
                    + " and TypeLovid=" + typeListId;
            query += " and (upload='N' OR refid!=0)";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                c.moveToNext();
                db.deleteSQL(DataMembers.tbl_AssetHeader,
                        "uid=" + QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_AssetDetail,
                        "uid=" + QT(c.getString(0)), false);
                refId = c.getString(1);
            }


            int moduleWeightAge = 0;
            double productWeightAge = 0, sum = 0;

            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String assetHeaderColumns = "uid,Date,RetailerId,remark,TypeLovid,tgtTotal,achTotal,refid";
            StringBuilder assetHeaderValues = new StringBuilder();
            assetHeaderValues.append(id);
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(mBusinessModel.getAssetRemark()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(typeListId);


            String AssetDetailColumns = "uid,AssetID,AvailQty,ImageName,ReasonID,SerialNumber,conditionId,installdate,servicedate,isAudit,Productid,CompQty,Retailerid,LocId,PosmGroupLovId,isExecuted,imgName";
            if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                assetHeaderColumns = assetHeaderColumns + ",Weightage,Score";
                AssetDetailColumns = AssetDetailColumns + ",Score";
            }
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
                                if (assetBo.getImageName() != null
                                        && !assetBo.getImageName().equals("") && assetBo.getAvailQty() > 0) {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(QT(assetBo.getImageName()));
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
                                                    .getMinstalldate() == null || assetBo
                                                    .getMinstalldate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMinstalldate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_SERVICE_DATE ? ((assetBo
                                                    .getMservicedate() == null || assetBo
                                                    .getMservicedate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMservicedate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                } else if ("MENU_POSM".equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_INSTALL_DATE ? ((assetBo
                                                    .getMinstalldate() == null || assetBo
                                                    .getMinstalldate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMinstalldate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_SERVICE_DATE ? ((assetBo
                                                    .getMservicedate() == null || assetBo
                                                    .getMservicedate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMservicedate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                }
                                assetDetailValues
                                        .append(",");
                                assetDetailValues.append(assetBo.getAudit());

                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getProductid());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getCompetitorQty());
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
                                assetDetailValues.append(",");
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
                                    assetDetailValues.append("," + productWeightAge);
                                    sum = sum + productWeightAge;
                                }

                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns,
                                        assetDetailValues.toString());
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
                                if (assetBo.getImageName() != null
                                        && !assetBo.getImageName().equals("") && assetBo.getAvailQty() > 0) {
                                    assetDetailValues.append(QT(assetBo.getImageName()));
                                } else {
                                    assetDetailValues.append(QT(""));
                                }


                                assetDetailValues.append(",");
                                assetDetailValues.append(0);
                                assetDetailValues.append(",");
                                assetDetailValues.append(QT(assetBo.getSerialNo()));
                                assetDetailValues.append(",");
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
                                                    .getMinstalldate() == null || assetBo
                                                    .getMinstalldate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMinstalldate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_ASSET_SERVICE_DATE ? ((assetBo
                                                    .getMservicedate() == null || assetBo
                                                    .getMservicedate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMservicedate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                } else if ("MENU_POSM".equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_INSTALL_DATE ? ((assetBo
                                                    .getMinstalldate() == null || assetBo
                                                    .getMinstalldate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMinstalldate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(DatabaseUtils
                                            .sqlEscapeString(SHOW_POSM_SERVICE_DATE ? ((assetBo
                                                    .getMservicedate() == null || assetBo
                                                    .getMservicedate()
                                                    .length() == 0) ? SDUtil
                                                    .now(SDUtil.DATE_GLOBAL)
                                                    : (DateUtil
                                                    .convertToServerDateFormat(
                                                            assetBo.getMservicedate(),
                                                            ConfigurationMasterHelper.outDateFormat)))
                                                    : ""));
                                }
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAudit());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getProductid());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getCompetitorQty());
                                assetDetailValues.append(",");
                                assetDetailValues.append(mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
                                assetDetailValues.append(",");
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
                                    assetDetailValues.append("," + productWeightAge);
                                    sum = sum + productWeightAge;
                                }
                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns, assetDetailValues.toString());
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

            if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                if (MENU_ASSET.equals(moduleName)) {
                    moduleWeightAge = mBusinessModel.fitscoreHelper.getModuleWeightage(DataMembers.FIT_ASSET);
                } else if ("MENU_POSM".equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                    moduleWeightAge = mBusinessModel.fitscoreHelper.getModuleWeightage(DataMembers.FIT_POSM);
                }
                assetHeaderValues.append("," + moduleWeightAge);
                double achieved = ((sum / (double) 100) * moduleWeightAge);
                assetHeaderValues.append("," + achieved);
            }

            db.insertSQL(DataMembers.tbl_AssetHeader, assetHeaderColumns,
                    assetHeaderValues.toString());

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Ordered asset record set to AssetTrackingBO object
     *
     * @param assetID Asset Id
     * @param qty Qty
     * @param imageName image Name
     * @param mReasonId reason Id
     * @param serialNo serial Number
     * @param imgName image Name
     */

    private void setAssetDetails(int assetID, int qty, String imageName,
                                 String mReasonId, String serialNo,
                                 String conditionId, String installDate, String serviceDate, int audit, int pid, int compQty, int locId, int isExec, String imgName) {

        AssetTrackingBO assetBO = null;
        mAssetTrackingList = null;
        for (StandardListBO standardListBO : mBusinessModel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locId))) {
                mAssetTrackingList = standardListBO.getAssetTrackingList();
                break;
            }
        }
        if (mAssetTrackingList != null) {
            int size = mAssetTrackingList.size();

            for (int i = 0; i < size; i++) {
                if (mAssetTrackingList.get(i).getProductid() == pid && mAssetTrackingList.get(i).getAssetID() == assetID) {
                    assetBO = mAssetTrackingList.get(i);
                    break;
                }
            }
            if (assetBO != null) {

                assetBO.setAvailQty(qty);
                assetBO.setImageName(imageName);
                assetBO.setReason1ID(mReasonId);
                assetBO.setConditionID(conditionId);
                assetBO.setMinstalldate(installDate);
                assetBO.setMservicedate(serviceDate);

                assetBO.setAudit(audit);
                if (!"null".equals(serialNo)) {
                    assetBO.setSerialNo(serialNo);
                } else {
                    assetBO.setSerialNo(Integer.toString(0));
                }

                assetBO.setCompetitorQty(compQty);

                assetBO.setExecutorQty(isExec);
                assetBO.setImgName(imgName);

            }
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

                    if (assetBO.getAvailQty() > 0 || assetBO.getAudit() != 2 || assetBO.getCompetitorQty() > 0 || assetBO.getExecutorQty() > 0) {
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

}
