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

import static com.baidu.platform.comapi.map.f.e;

@SuppressLint("UseSparseArrays")
public class AssetTrackingHelper {
    private final Context context;
    private final BusinessModel bmodel;
    private static AssetTrackingHelper instance = null;
    private AssetTrackingBO massetTrackingBO;

    /**
     * This ArrayList contains downloaded assettracking records
     */
    private ArrayList<AssetTrackingBO> mAssetTrackingList = new ArrayList<>();
    /**
     * This ArrayList contains downloaded All assettracking records
     */
    private ArrayList<AssetTrackingBO> mAllAssetTrackingList = new ArrayList<>();

    /**
     * Key - AssetID, return AssetTrackingBO
     */


    /**
     * This ArrayList contains downloaded assetreason records
     */
    private Vector<AssetTrackingBO> addremoveassets = null;

    private Vector<AssetAddDetailBO> assetspinner = null;
    private Vector<AssetAddDetailBO> brandspinner = null;

    private ArrayList<ReasonMaster> mAssetReasonList = new ArrayList<>();
    /**
     * This ArrayList contains downloaded assetremarks records
     */
    private ArrayList<ReasonMaster> mAssetRemarkList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetconditionList = new ArrayList<>();

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
    public boolean SHOW_ASSET_EXEUTED;

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

    /**
     * Reason Type - Std List Code
     */
    private static final String ASSET_REASON = "AR";
    private static final String ASSET_REMARK = "ARR";
    private static final String ASSET_CONDITION = "CD";

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

    private static final String CODE_POSM_ADD = "POSM03";
    public boolean SHOW_ADD_NEW_POSM;

    private static final String CODE_REMOVE_POSM = "POSM04";
    public boolean SHOW_REMOVE_POSM;

    private static final String CODE_SHOW_ALL_POSM = "POSM05";
    public boolean SHOW_POSM_ALL;

    private static final String CODE_SHOW_POSM_REMARKS = "POSM06";
    public boolean SHOW_REMARKS_POSM;

    private static final String MERCH = "MERCH";
    private static final String MENU_ASSET = "MENU_ASSET";
    private static final String MERCH_INIT = "MERCH_INIT";

    private AssetTrackingHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    private AssetTrackingBO getMassetTrackingBO() {
        return massetTrackingBO;
    }

    public void setMassetTrackingBO(AssetTrackingBO massetTrackingBO) {
        this.massetTrackingBO = massetTrackingBO;
    }

    public Vector<AssetTrackingBO> getAddremoveassets() {
        return addremoveassets;
    }

    public static AssetTrackingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AssetTrackingHelper(context);
        }
        return instance;
    }

    public void loadDataForAssetPOSM(String mMenuCode) {
        if (bmodel.configurationMasterHelper
                .downloadFloatingSurveyConfig(mMenuCode)) {
            bmodel.mSurveyHelperNew.setFromHomeScreen(false);
            bmodel.mSurveyHelperNew.downloadModuleId("STANDARD");
            bmodel.mSurveyHelperNew.downloadQuestionDetails(mMenuCode);
            bmodel.mSurveyHelperNew.loadSurveyAnswers(0);
            bmodel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
        }

        if (MENU_ASSET.equalsIgnoreCase(mMenuCode))
            loadAssetConfigs();
        else if ("MENU_POSM".equalsIgnoreCase(mMenuCode) || "MENU_POSM_CS".equalsIgnoreCase(mMenuCode))
            loadPOSMConfigs();

        bmodel.productHelper.downloadInStoreLocations();

        bmodel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
        // bmodel.productHelper.downloadProductFilter(mMenuCode);

        downloadAssetMaster(mMenuCode);

        loadAssetData(bmodel
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
            SHOW_ASSET_EXEUTED = false;

            SHOW_ASSET_BARCODE = false;
            SHOW_ADD_NEW_ASSET = false;
            SHOW_REMOVE_ASSET = false;
            SHOW_ASSET_ALL = false;
            SHOW_REMARKS_ASSET = false;
            ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = false;

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
                                        SHOW_ASSET_EXEUTED = true;
                                        break;
                                }
                            }
                        }
                    } else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_BARCODE))
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
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_ASSET_REASON)
                downloadAssetReason(ASSET_REASON);
            if (SHOW_ASSET_CONDITION)
                downloadAssetReason(ASSET_CONDITION);
            if (SHOW_REMARKS_ASSET)
                downloadAssetReason(ASSET_REMARK);

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
            SHOW_ADD_NEW_POSM = false;
            SHOW_REMOVE_POSM = false;
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
                    else if (c.getString(0).equalsIgnoreCase(CODE_POSM_ADD))
                        SHOW_ADD_NEW_POSM = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_REMOVE_POSM))
                        SHOW_REMOVE_POSM = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_ALL_POSM))
                        SHOW_POSM_ALL = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_SHOW_POSM_REMARKS))
                        SHOW_REMARKS_POSM = true;
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_POSM_REASON)
                downloadAssetReason(ASSET_REASON);
            if (SHOW_POSM_CONDITION)
                downloadAssetReason(ASSET_CONDITION);
            if (SHOW_REMARKS_POSM)
                downloadAssetReason(ASSET_REMARK);

        } catch (Exception e) {
            Commons.printException("loadPOSMConfigs " + e);
        }
    }

    /**
     * Method that to download Asset Details from SQLite
     *
     * @param moduleName
     */

    private void downloadAssetMaster(String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        mAssetTrackingList = new ArrayList<>();
        mAllAssetTrackingList = new ArrayList<>();

        AssetTrackingBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            int level = bmodel.productHelper.getRetailerlevel(moduleName);
            sb.append("select Distinct P.PosmId,P.Posmdesc,SBD.SerialNO,SBD.Target,SBD.Productid,SLM.listname,SLM.listid,SBD.NfcTagId from PosmMaster P  ");
            sb.append("inner join POSMCriteriaMapping SBD on P.PosmID=SBD.posmid ");
            sb.append("left join Standardlistmaster SLM on SLM.listid=SBD.PosmGroupLovId and ListType='POSM_GROUP_TYPE' ");
            sb.append("where  SBD.TypeLovId=(select listid from StandardListMaster where ListCode=");
            sb.append(bmodel.QT(type));
            sb.append(" and ListType='SBD_TYPE') ");
            String allMasterSb = sb.toString();
            if (level == 2) {
                // retailer mapping
                sb.append(" and Retailerid=");
                sb.append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            } else if (level == 4) {
                // Location mapping
                sb.append(" and Locid in (");
                sb.append(bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(")");


            } else if (level == 5) {
                // Channel Mapping
                sb.append(" and (Channelid =");
                sb.append(bmodel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");
            } else if (level == 6) {

                // Location Mapping and Channel Mapping
                sb.append(" and Locid in(");
                sb.append(bmodel.productHelper.getMappingLocationId(bmodel.productHelper.locid, bmodel.getRetailerMasterBO().getLocationId()));
                sb.append(")");
                sb.append(" and (Channelid =");
                sb.append(bmodel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");
            }

            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                sb.append("and (SBD.Productid = " + bmodel.productHelper.getmSelectedGlobalProductId() + " OR SBD.Productid = 0 )");
                allMasterSb = allMasterSb + ("and (SBD.Productid = " + bmodel.productHelper.getmSelectedGlobalProductId() + " OR SBD.Productid = 0 )");
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

//                if (mAssetTrackingList != null) {
//                    for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
//
//                        ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAssetTrackingList.size());
//                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
//                            clonedList.add(new AssetTrackingBO(assetBO));
//                        }
//                        standardListBO.setAssetTrackingList(clonedList);
//                    }
//
//
//                }
            }
            if (mAssetTrackingList != null) {
                for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                    ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAssetTrackingList.size());
                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        clonedList.add(new AssetTrackingBO(assetBO));
                    }
                    standardListBO.setAssetTrackingList(clonedList);
                }

            } else {
                for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                    standardListBO.getAssetTrackingList().clear();
                }
            }
            if (c1.getCount() > 0) {
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
                for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                    ArrayList<AssetTrackingBO> clonedList = new ArrayList<>(mAllAssetTrackingList.size());
                    for (AssetTrackingBO assetBO : mAllAssetTrackingList) {
                        clonedList.add(new AssetTrackingBO(assetBO));
                    }
                    standardListBO.setAllAssetTrackingList(clonedList);
                }

            } else {
                for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
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
     * Method that to retrive loaded data from sqlite table
     *
     * @param retailerid
     * @param moduleName
     */

    private void loadAssetData(String retailerid, String moduleName) {
        String type;
        if (MENU_ASSET.equals(moduleName))
            type = MERCH;
        else
            type = MERCH_INIT;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String sb = "select uid, ifnull(remark,'') from AssetHeader where retailerid=" + QT(retailerid) + " and TypeLovId=" +
                    "(select listid from StandardListMaster where ListCode=" + bmodel.QT(type) + " and ListType='SBD_TYPE') " +
                    "and (upload='N' OR refid!=0)";

            Cursor c = db.selectSQL(sb);
            String uid = "";
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    uid = c.getString(0);
                    bmodel.setAssetRemark(c.getString(1));
                }
            } else {
                bmodel.setAssetRemark("");
            }
            /*String sb2 = "select assetid,availqty,imagename,reasonid,SerialNumber,conditionId,installdate,servicedate,isAudit,Productid,CompQty,Locid,PosmGroupLovId,isExecuted  from assetDetail where uid=" +
                    QT(uid);*/
            String sb2 = "select assetid,availqty,imagename,reasonid,SerialNumber,conditionId,installdate,servicedate,isAudit,Productid,CompQty,Locid,PosmGroupLovId,isExecuted,imgName  from assetDetail where uid=" +
                    QT(uid);


            Cursor detailCursor = db.selectSQL(sb2);
            if (detailCursor.getCount() > 0) {
                while (detailCursor.moveToNext()) {
                    int assetid = detailCursor.getInt(0);
                    int qty = detailCursor.getInt(1);
                    String imageName = detailCursor.getString(2);
                    String reasonid = detailCursor.getString(3);

                    String serialNo = detailCursor.getString(4);
                    String conditionid = detailCursor.getString(5);
                    int audit = detailCursor.getInt(8);
                    int pid = detailCursor.getInt(9);
                    int compQty = detailCursor.getInt(10);
                    int locid = detailCursor.getInt(11);
                    final int isExecuted = detailCursor.getInt(13);


                    setAssetDetails(
                            assetid,
                            qty,
                            imageName,
                            reasonid,

                            serialNo,
                            conditionid,
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(6),
                                    ConfigurationMasterHelper.outDateFormat),
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(7),
                                    ConfigurationMasterHelper.outDateFormat), audit, pid, compQty, locid, isExecuted, detailCursor.getString(detailCursor.getColumnIndex("imgName")));
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
     * @param category
     */
    private void downloadAssetReason(String category) {
        switch (category) {
            case ASSET_REASON:
                mAssetReasonList = new ArrayList<>();
                break;
            case ASSET_REMARK:
                mAssetRemarkList = new ArrayList<>();
                break;
            case ASSET_CONDITION:
                mAssetconditionList = new ArrayList<>();
                break;
        }
        ReasonMaster reasonBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(category));
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
                            mAssetconditionList.add(reasonBO);
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

    public void downloadAssetsposm(String moduleName) {
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
                assetspinner = new Vector<>();
                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setMposmid(c.getString(0));
                    assetBO.setMposmdesc(c.getString(1));

                    assetspinner.add(assetBO);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public Vector<String> getassetposmNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = assetspinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = assetspinner.get(i);
                data.add(brand.getMposmdesc());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    public Vector<String> getassetbrandNames() {
        AssetAddDetailBO brand;
        Vector<String> data = new Vector<>();
        try {
            int siz = brandspinner.size();
            if (siz == 0)
                return data;

            for (int i = 0; i < siz; ++i) {
                brand = brandspinner.get(i);
                data.add(brand.getMassetbrandname());
            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return data;
    }

    public String getassetposmids(String massetposmName) {
        AssetAddDetailBO brand;

        try {
            int siz = assetspinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = assetspinner.get(i);
                Commons.print("brand.getMposmdesc()="
                        + brand.getMposmdesc() + "," + massetposmName);
                if (brand.getMposmdesc().equals(massetposmName)) {

                    return brand.getMposmid();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return massetposmName;
    }

    public String getassetbrandids(String massetbrandName) {
        AssetAddDetailBO brand;

        try {
            int siz = brandspinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = brandspinner.get(i);
                if (brand.getMassetbrandname().equals(massetbrandName)) {
                    return brand.getAssetbrandid();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return massetbrandName;
    }

    public void downloadAssetbrand(String brandposm) {

        AssetAddDetailBO assetBO;
        brandspinner = new Vector<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String sb = "SELECT PM.Pid,PM.PName FROM ProductMaster PM INNER JOIN POSMProductMapping PO ON PM.Pid = PO.Productid WHERE PO.PosmID =" + QT(brandposm);

            Cursor c = db.selectSQL(sb);

            if (c.getCount() > 0) {

                while (c.moveToNext()) {
                    assetBO = new AssetAddDetailBO();
                    assetBO.setAssetbrandid(c.getString(0));
                    assetBO.setMassetbrandname(c.getString(1));

                    brandspinner.add(assetBO);

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

        addremoveassets = new Vector<>();
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
            int level = bmodel.productHelper.getRetailerlevel(moduleName);


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
                sb.append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            } else if (level == 4) {
                // Location mapping
                sb.append(" and Locid in(");
                sb.append(bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(")");


            } else if (level == 5) {
                // Channel Mapping
                sb.append(" and (Channelid =");
                sb.append(bmodel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()));
                sb.append("))");

            } else if (level == 6) {

                // Location Mapping and Channel Mapping
                sb.append(" and Locid in(");
                sb.append(bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme());
                sb.append(" and (Channelid =");
                sb.append(bmodel.getRetailerMasterBO().getSubchannelid());
                sb.append(" OR Channelid in (");
                sb.append(bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()));
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
                    addremoveassets.add(assetBO);
                }
            }
            String sb1 = "select distinct  AssetId,P.Posmdesc,serialNum,installdate,flag ,uid,Productid  from PosmMaster P  inner  join AssetAddDelete AAD on P.PosmId=AAD.AssetId where flag!='D'  and retailerid=" +
                    QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and AAD.TypeLovId=" + typeListId;

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
                    addremoveassets.add(assetBO);
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
     * Method return reason name arraylist
     *
     * @return ArrayList<Reasonmaster>
     */

    public ArrayList<ReasonMaster> getAssetReasonList() {
        if (mAssetReasonList != null) {
            return mAssetReasonList;
        }
        return new ArrayList<>();
    }

    /**
     * Method return reason remarks arraylist
     *
     * @return ArrayList<Reasonmaster>
     */

    public ArrayList<ReasonMaster> getAssetRemarksList() {
        if (mAssetRemarkList != null) {
            return mAssetRemarkList;
        }
        return new ArrayList<>();
    }

    public ArrayList<ReasonMaster> getmAssetconditionList() {
        if (mAssetconditionList != null) {
            return mAssetconditionList;
        }
        return new ArrayList<>();
    }

    /**
     * Method return the correct position to selected reasonid
     *
     * @param reasonid
     * @param reasonList
     * @return integer
     */
    public int getItemIndex(String reasonid, ArrayList<ReasonMaster> reasonList) {
        int size = reasonList.size();

        for (int i = 0; i < size; i++) {
            ReasonMaster reasonBO = reasonList.get(i);
            if (reasonBO.getReasonID().equals(reasonid)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Method return the correct position to selected conditionId
     *
     * @param conditionId
     * @param reasonList
     * @return integer
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
     * Method to delete imagename in sql table
     *
     * @param imgName
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
    public void saveAssetAddandDeletedetails(String moduleName) {
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
            String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            AssetTrackingBO assets = getMassetTrackingBO();
            String addassetColumns = "uid,retailerid,AssetId,serialNum,productid,installdate,creationdate,TypeLovId,reasonid,remarks";

            String assetaddanddeleteValues = id + "," + QT(bmodel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(assets.getMposm()) + "," + QT(assets.getMsno()) + ","
                    + QT(assets.getMbrand()) + ","
                    + QT(DateUtil.convertToServerDateFormat(
                    assets.getMnewinstaldate(),
                    ConfigurationMasterHelper.outDateFormat))
                    + "," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + typeListId + "," +
                    QT(assets.getMreasonId()) + "," + QT(assets.getMremarks());

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addassetColumns,
                    assetaddanddeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void saveAddandDeletedetails(String posmid, String msno,
                                        String msbdid, String mbrandid, String moduleName) {
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
            String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String addassetColumns = "uid,retailerid,AssetId,serialNum,creationdate,flag,mappingid,Productid,TypeLovId";

            String assetaddanddeleteValues = id + "," + QT(bmodel.getRetailerMasterBO().getRetailerID()) + ","
                    + QT(posmid) + "," + QT(msno) + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + QT("D") + ","
                    + QT(msbdid) + "," + QT(mbrandid) + "," + typeListId;

            db.insertSQL(DataMembers.tbl_AssetAddDelete, addassetColumns,
                    assetaddanddeleteValues);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void deletePosmdetails(String msno) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_AssetAddDelete, "serialNum ="
                    + QT(msno), false);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public boolean isExistingRetailersno(String msno) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String sql = "select serialNum from "
                    + DataMembers.tbl_AssetAddDelete + "  where serialNum="
                    + QT(msno) + " and retailerid = "
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()) + "";

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
                    + QT(bmodel.getRetailerMasterBO().getRetailerID())
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


            int moduleWeightage = 0;
            double productWeightage = 0, sum = 0;

            String id = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String assetHeaderColumns = "uid,Date,RetailerId,remark,TypeLovid,tgtTotal,achTotal,refid";
            StringBuilder assetHeaderValues = new StringBuilder();
            assetHeaderValues.append(id);
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(bmodel.getRetailerMasterBO().getRetailerID()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(QT(bmodel.getAssetRemark()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(typeListId);


            String AssetDetailColumns = "uid,AssetID,AvailQty,ImageName,ReasonID,SerialNumber,conditionId,installdate,servicedate,isAudit,Productid,CompQty,Retailerid,LocId,PosmGroupLovId,isExecuted,imgName";
            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                assetHeaderColumns = assetHeaderColumns + ",Weightage,Score";
                AssetDetailColumns = AssetDetailColumns + ",Score";
            }
            int totalTarget = 0;
            int totalActualQty = 0;
            for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                mAssetTrackingList = standardListBO.getAssetTrackingList();
                if (mAssetTrackingList != null) {
                    productWeightage = (double) 100 / (double) mAssetTrackingList.size();
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
                                assetDetailValues.append(QT(bmodel.getRetailerMasterBO().getRetailerID()));
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

                                if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                    assetDetailValues.append("," + productWeightage);
                                    sum = sum + productWeightage;
                                }

                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns,
                                        assetDetailValues.toString());
                            }
                        } else {
//                            if (assetBo.getAvailQty() > 0
//                                    || assetBo.getAudit() != 2 || assetBo.getExecutorQty() > 0
//                                    || assetBo.getCompetitorQty() > 0) {
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
                                assetDetailValues.append(assetBo.getRemarkID());
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
                                assetDetailValues.append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
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

                                if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                    assetDetailValues.append("," + productWeightage);
                                    sum = sum + productWeightage;
                                }

                                db.insertSQL(DataMembers.tbl_AssetDetail,
                                        AssetDetailColumns, assetDetailValues.toString());
//                            }
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

            if (bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                if (MENU_ASSET.equals(moduleName)) {
                    moduleWeightage = bmodel.fitscoreHelper.getModuleWeightage(DataMembers.FIT_ASSET);
                } else if ("MENU_POSM".equals(moduleName) || "MENU_POSM_CS".equals(moduleName)) {
                    moduleWeightage = bmodel.fitscoreHelper.getModuleWeightage(DataMembers.FIT_POSM);
                }
                assetHeaderValues.append("," + moduleWeightage);
                double achieved = ((sum / (double) 100) * moduleWeightage);
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
     * @param assetID
     * @param qty
     * @param imageName
     * @param reasonid
     * @param --remarksid
     * @param serialNo
     * @param imgName
     */

    private void setAssetDetails(int assetID, int qty, String imageName,
                                 String reasonid, String serialNo,
                                 String conditionid, String minstalldate, String mservicedate, int audit, int pid, int compQty, int locid, int isExec, String imgName) {

        AssetTrackingBO assetBO = null;
        mAssetTrackingList = null;
        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locid))) {
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
                assetBO.setReason1ID(reasonid);
                assetBO.setConditionID(conditionid);
                assetBO.setMinstalldate(minstalldate);
                assetBO.setMservicedate(mservicedate);

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
        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
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
