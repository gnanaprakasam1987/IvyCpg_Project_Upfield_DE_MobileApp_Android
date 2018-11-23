package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.asset.AssetAddDetailBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SerializedAssetHelper {

    private BusinessModel mBusinessModel;
    private static SerializedAssetHelper instance;
    public String mSelectedActivityName;
    private SerializedAssetBO mAssetTrackingBO;

    // Asset configuration
    private static final String CODE_ASSET_COLUMNS = "SAT01";
    private static final String CODE_ASSET_BARCODE = "SAT02";
    private static final String CODE_ASSET_ADD = "SAT03";
    private static final String CODE_REMOVE_ASSET = "SAT04";
    private static final String CODE_SHOW_ALL = "SAT05";
    private static final String CODE_REMARKS_ASSET = "SAT06";
    private static final String CODE_NFC_SEARCH_IN_ASSET = "SAT07";
    private static final String CODE_ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = "SAT08";
    private static final String CODE_MOVE_ASSET = "SAT09";
    private static final String CODE_ASSET_PHOTO_VALIDATION = "SAT10";
    private static final String ASSET_REASON = "AR";
    private static final String ASSET_REMARK = "ARR";
    private static final String ASSET_CONDITION = "CD";
    private static final String CODE_ASSET_SERVICE = "SAT11";
    public boolean SHOW_ASSET_REASON;
    public boolean SHOW_ASSET_PHOTO;
    public boolean SHOW_ASSET_CONDITION;
    public boolean SHOW_ASSET_INSTALL_DATE;
    public boolean SHOW_ASSET_SERVICE_DATE;
    public boolean SHOW_ASSET_EXECUTED;
    public boolean SHOW_ASSET_BARCODE;
    public boolean SHOW_ADD_NEW_ASSET;
    public boolean SHOW_REMOVE_ASSET;
    public boolean SHOW_ASSET_ALL;
    public boolean SHOW_REMARKS_ASSET;
    public boolean SHOW_NFC_SEARCH_IN_ASSET;
    public boolean ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK;
    public boolean SHOW_MOVE_ASSET;
    public boolean SHOW_SERVICE_ASSET;
    public boolean ASSET_PHOTO_VALIDATION;

    //

    private ArrayList<ReasonMaster> mAssetReasonList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetRemarkList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetConditionList = new ArrayList<>();
    private HashMap<String, String> mUniqueSerialNo;
    private ArrayList<SerializedAssetBO> mAssetTrackingList = new ArrayList<>();
    private Vector<AssetAddDetailBO> mAssetSpinner = null;
    private Vector<AssetAddDetailBO> mBrandSpinner = null;
    private Vector<SerializedAssetBO> mRemovableAssets = null;
    private Vector<SerializedAssetBO> assetServiceList = null;


    private ArrayList<SerializedAssetBO> mAllAssetTrackingList = null;
    //

    private static final String MENU_SERIALIZED_ASSET = "MENU_SERIALIZED_ASSET";


    private SerializedAssetHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }

    public static SerializedAssetHelper getInstance(Context context) {
        if (instance == null)
            instance = new SerializedAssetHelper(context);

        return instance;
    }

    private SerializedAssetHelper() {
    }

    public void clear() {
        instance = null;
    }

    /*
  Download Master Data needed for Asset Screen
   */
    public void loadDataForAssetPOSM(Context mContext, String mMenuCode) {

        if (mBusinessModel.configurationMasterHelper
                .downloadFloatingSurveyConfig(mMenuCode)) {
            SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(mContext);
            surveyHelperNew.setFromHomeScreen(false);
            surveyHelperNew.downloadModuleId("STANDARD");
            surveyHelperNew.downloadQuestionDetails(mMenuCode);
            surveyHelperNew.loadSurveyAnswers(0);
            mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
            mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                    mBusinessModel.productHelper.getRetailerModuleSequenceValues(),false));

        }

        //update configurations
            downloadConfigs(mContext);

        //download filter levels
        mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
        mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                mBusinessModel.productHelper.getRetailerModuleSequenceValues(),false));

        // Load master records
        downloadAssetMaster(mContext, mMenuCode);

        // Load data from transaction
        loadAssetData(mContext, mBusinessModel
                .getRetailerMasterBO().getRetailerID(), mMenuCode);
    }


    /**
     * Load All Asset Screen Specific Configurations
     */
    private void downloadConfigs(Context mContext) {
        try {
            SHOW_ASSET_REASON = false;
            SHOW_ASSET_PHOTO = false;
            SHOW_ASSET_CONDITION = false;
            SHOW_ASSET_INSTALL_DATE = false;
            SHOW_ASSET_SERVICE_DATE = false;
            SHOW_ASSET_EXECUTED = false;
            SHOW_ASSET_BARCODE = false;
            SHOW_ADD_NEW_ASSET = false;
            SHOW_REMOVE_ASSET = false;
            SHOW_ASSET_ALL = false;
            SHOW_REMARKS_ASSET = false;
            ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = false;
            SHOW_MOVE_ASSET = false;
            SHOW_SERVICE_ASSET = false;
            ASSET_PHOTO_VALIDATION=false;
            SHOW_NFC_SEARCH_IN_ASSET=false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE menu_type = 'MENU_SERIALIZED_ASSET' AND flag='1' and ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_ASSET_COLUMNS)) {
                        if (c.getString(1) != null) {
                            String codeSplit[] = c.getString(1).split(",");
                            for (String temp : codeSplit) {
                                updateColumnConfig(temp);
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
                    else if (CODE_NFC_SEARCH_IN_ASSET.equalsIgnoreCase(c.getString(0)))
                        SHOW_NFC_SEARCH_IN_ASSET = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK))
                        ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_MOVE_ASSET))
                        SHOW_MOVE_ASSET = true;
                    else if (c.getString(0).equalsIgnoreCase(CODE_ASSET_PHOTO_VALIDATION))
                        ASSET_PHOTO_VALIDATION = true;
                    else if (CODE_ASSET_SERVICE.equalsIgnoreCase(c.getString(0)))
                        SHOW_SERVICE_ASSET = true;
                }
                c.close();
            }
            db.closeDB();

            if (SHOW_ASSET_REASON)
                downloadReasons(mContext, ASSET_REASON);
            if (SHOW_ASSET_CONDITION)
                downloadReasons(mContext, ASSET_CONDITION);
            if (SHOW_REMARKS_ASSET)
                downloadReasons(mContext, ASSET_REMARK);

        } catch (Exception e) {
            Commons.printException("downloadConfigs " + e);
        }
    }

    /**
     * Update Asset Column Configurations
     *
     * @param temp Configuration Code
     */
    private void updateColumnConfig(String temp) {
        switch (temp) {

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
            case "EXECUTED":
                SHOW_ASSET_EXECUTED = true;
                break;
        }
    }

    /**
     * Method that to download asset reason in ArrayList
     *
     * @param category Reason Category
     */
    private void downloadReasons(Context mContext, String category) {
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

        }
        ReasonMaster reasonBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
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
     * Method that to download Asset Details from SQLite
     *
     * @param moduleName module name
     */
    private void downloadAssetMaster(Context mContext, String moduleName) {

        mAllAssetTrackingList=new ArrayList<>();
        mAssetTrackingList = new ArrayList<>();
        mUniqueSerialNo = new HashMap<>();

        SerializedAssetBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            sb.append("select Distinct A.assetId,A.assetName,B.serialNumber,C.Productid,B.NFCNumber,PM.ParentHierarchy as ParentHierarchy,AllocationRefId from SerializedAssetMaster A  ");
            sb.append("inner join SerializedAssetMapping B on A.AssetId=B.AssetId ");
            sb.append("left join SerializedAssetProductMapping C on C.AssetId=A.AssetId ");

            sb.append("left join ProductMaster PM on PM.PID=C.Productid ");

            String allMasterSb = sb.toString();

            sb.append(" and Retailerid in(0,");
            sb.append(AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()) + ")");

            if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                sb.append(" and (C.Productid = ");
                sb.append(mBusinessModel.productHelper.getmSelectedGlobalProductId());
                sb.append(" OR C.Productid = 0 )");

                allMasterSb = allMasterSb + ("and (C.Productid = " + mBusinessModel.productHelper.getmSelectedGlobalProductId() + " OR C.Productid = 0 )");
            }

            sb.append(" GROUP BY RetailerId,C.Productid,B.AssetId,B.SerialNumber ORDER BY RetailerId");

            Cursor c = db.selectSQL(sb.toString());
            Cursor c1 = db.selectSQL(allMasterSb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetTrackingBO = new SerializedAssetBO();
                    assetTrackingBO.setAssetID(c.getInt(0));
                    assetTrackingBO.setAssetName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2))) {
                        assetTrackingBO.setSerialNo(c.getString(2));
                    } else {
                        assetTrackingBO.setSerialNo(Integer.toString(0));
                    }

                    assetTrackingBO.setProductId(c.getInt(3));

                    assetTrackingBO.setNFCTagId(c.getString(c.getColumnIndex("NFCNumber")));
                    assetTrackingBO.setParentHierarchy(c.getString(c.getColumnIndex("ParentHierarchy")));
                    assetTrackingBO.setReferenceId(c.getInt(c.getColumnIndex("AllocationRefId")));


                    mAssetTrackingList.add(assetTrackingBO);

                }

            }

            //load serial no's into hash map for uniqueness

            if (MENU_SERIALIZED_ASSET.equals(moduleName)) {
                String sb1 = "select SerialNumber from SerializedAssetMapping A INNER JOIN SerializedAssetMaster B ON A.assetId=B.assetId " ;
                c = db.selectSQL(sb1);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (c.getString(0) != null)
                            mUniqueSerialNo.put(c.getString(0), c.getString(0));
                    }
                }
            }


            String sb1 = "select  serialNumber from SerializedAssetTransfer where transfer_type='RTR_WH' and retailerid="+mBusinessModel.getRetailerMasterBO().getRetailerID();
            Cursor cursorDelete = db.selectSQL(sb1);
            SerializedAssetBO assetBoDelete = null;
            List<SerializedAssetBO> deletedAssetList = new ArrayList<>();
            if (cursorDelete.getCount() > 0) {
                while (cursorDelete.moveToNext()) {
                    assetBoDelete = new SerializedAssetBO();

                    if (!"null".equals(cursorDelete.getString(0))) {
                        assetBoDelete.setSerialNo(cursorDelete.getString(0));
                    }
                    deletedAssetList.add(assetBoDelete);
                }
            }

            if (mAssetTrackingList != null && mAssetTrackingList.size() > 0) {
                    for (SerializedAssetBO assetDeleteBO : deletedAssetList) {
                        for (SerializedAssetBO assetBO : mAssetTrackingList) {
                            if (assetDeleteBO.getSerialNo().equalsIgnoreCase(assetBO.getSerialNo())) {
                                mAssetTrackingList.remove(assetBO);
                                break;
                            }

                        }
                    }


            }

            if (c1.getCount() > 0) {
                mAllAssetTrackingList = new ArrayList<>();
                while (c1.moveToNext()) {
                    assetTrackingBO = new SerializedAssetBO();
                    assetTrackingBO.setAssetID(c1.getInt(0));
                    assetTrackingBO.setAssetName(c1.getString(1));
                    if (c1.getString(2) != null && !"null".equals(c1.getString(2))) {
                        assetTrackingBO.setSerialNo(c1.getString(2));
                    } else {
                        assetTrackingBO.setSerialNo(Integer.toString(0));
                    }

                    assetTrackingBO.setProductId(c1.getInt(3));

                    assetTrackingBO.setNFCTagId(c1.getString(c1.getColumnIndex("NFCNumber")));
                    assetTrackingBO.setParentHierarchy(c1.getString(c1.getColumnIndex("ParentHierarchy")));

                    mAllAssetTrackingList.add(assetTrackingBO);

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
     * Method that to get loaded data from SQLite table
     *
     * @param mRetailerId Retailer ID
     * @param moduleName  Module Name
     */
    private void loadAssetData(Context mContext, String mRetailerId, String moduleName) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String sb = "select uid, IFNULL(remarks,'') from SerializedAssetHeader where retailerid=" + AppUtils.QT(mRetailerId) +
                    " and upload='N'";

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

            String sb2 = "select assetid,isAvailable,reasonId,conditionId,serialNumber,NFCNumber,installDate,lastServiceDate  from SerializedAssetDetail where uid=" +
                    AppUtils.QT(uid);


            Cursor detailCursor = db.selectSQL(sb2);
            if (detailCursor.getCount() > 0) {
                while (detailCursor.moveToNext()) {
                    int mAssetId = detailCursor.getInt(0);
                    int isAvailable = detailCursor.getInt(1);
                    String reasonId = detailCursor.getString(2);

                    String serialNo = detailCursor.getString(4);
                    String conditionId = detailCursor.getString(3);



                    setAssetDetails(mContext,
                            mAssetId,
                            isAvailable,
                            reasonId,
                            serialNo,
                            conditionId,
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(6),
                                    ConfigurationMasterHelper.outDateFormat),
                            DateUtil.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(7),
                                    ConfigurationMasterHelper.outDateFormat));
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
     * Ordered asset record set to AssetTrackingBO object
     *
     * @param assetID   Asset Id
     * @param isAvailable       Qty
     * @param installDate Installed date
     * @param mReasonId reason Id
     * @param serialNo  serial Number
     * @param serviceDate   service date
     */
    private void setAssetDetails(Context mcontext, int assetID, int isAvailable,
                                 String mReasonId, String serialNo,
                                 String conditionId, String installDate, String serviceDate) {

        SerializedAssetBO assetBO = null;

        if (mAssetTrackingList != null) {
            int size = mAssetTrackingList.size();

            for (int i = 0; i < size; i++) {
                if (mAssetTrackingList.get(i).getAssetID() == assetID &&
                        mAssetTrackingList.get(i).getSerialNo().equalsIgnoreCase(serialNo)) {
                    assetBO = mAssetTrackingList.get(i);
                    if (assetBO != null) {

                        assetBO.setAvailQty(isAvailable);
                        assetBO.setReason1ID(mReasonId);
                        assetBO.setConditionID(conditionId);
                        assetBO.setInstallDate(installDate);
                        assetBO.setServiceDate(serviceDate);

                        if (!"null".equals(serialNo)) {
                            assetBO.setSerialNo(serialNo);
                        } else {
                            assetBO.setSerialNo(Integer.toString(0));
                        }
                        assetBO.setImageList(getImagesList(mcontext, assetID,assetBO));

                    }

                }
            }

        }
    }


    private ArrayList<String> getImagesList(Context mContext, int assetId,SerializedAssetBO assetBO) {
        ArrayList<String> imageList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ImageName,imgName from SerializedAssetImageDetails "
                    + " where AssetID = " + assetId
                    + " AND Upload = " + AppUtils.QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    imageList.add(c.getString(0));

                    // Now max image is one, so setting directly in assetBO
                    assetBO.setImgName(c.getString(1));
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageList;
    }

    /**
     * Method that to store asset tracking details in ArrayList
     *
     * @return ArrayList<AssetTrackingBO>
     */
    public ArrayList<SerializedAssetBO> getAssetTrackingList() {
        if (mAssetTrackingList != null) {
            return mAssetTrackingList;
        }

        return new ArrayList<>();

    }

    public ArrayList<SerializedAssetBO> getAllAssetTrackingList() {
        if (mAssetTrackingList != null) {
            return mAllAssetTrackingList;
        }
        return new ArrayList<>();
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


    public void deleteImageProof(Context mContext, String ImageName) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            db.deleteSQL(DataMembers.tbl_AssetImgInfo, "ImageName LIKE"
                    + AppUtils.QT(ImageName+"%"), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Saving asset details
     *
     * @param moduleName Module Name
     */
    public void saveAsset(Context mContext, String moduleName) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();


            String query = "select uid from SerializedAssetHeader where retailerid ="
                    + AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());
            query += " and upload='N'";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                c.moveToNext();
                db.deleteSQL(DataMembers.tbl_SerializedAssetHeader,
                        "uid=" + AppUtils.QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_SerializedAssetDetail,
                        "uid=" + AppUtils.QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_SerializedAssetImageDetail,
                        "uid=" + AppUtils.QT(c.getString(0)), false);
            }


            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String assetHeaderColumns = "uid,DateTime,RetailerId,remarks";
            StringBuilder assetHeaderValues = new StringBuilder();
            assetHeaderValues.append(id);
            assetHeaderValues.append(",");
            assetHeaderValues.append(AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            assetHeaderValues.append(",");
            assetHeaderValues.append(AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()));
            assetHeaderValues.append(",");
            assetHeaderValues.append(AppUtils.QT(mBusinessModel.getAssetRemark()));

            db.insertSQL(DataMembers.tbl_SerializedAssetHeader, assetHeaderColumns,
                    assetHeaderValues.toString());


            String AssetDetailColumns = "uid,AssetID,isAvailable,ReasonID,SerialNumber,conditionId,NFCNumber,installdate,lastServicedate";
            String AssetImageInfoColumns = "uid,AssetID,ImageName,serialNumber,imgName";


                    for (SerializedAssetBO assetBo : mAssetTrackingList) {
                        StringBuilder assetDetailValues = new StringBuilder();
                            if (assetBo.getAvailQty() > 0
                                    || !assetBo.getReason1ID().equals(Integer.toString(0))) {

                                assetDetailValues.append(id);
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAssetID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getAvailQty());
                                assetDetailValues.append(",");
                                assetDetailValues.append(assetBo.getReason1ID());
                                assetDetailValues.append(",");
                                assetDetailValues.append(AppUtils.QT(assetBo.getSerialNo()));
                                if (assetBo.getConditionID() != null && !"null".equals(assetBo.getConditionID())) {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(AppUtils.QT(assetBo.getConditionID()));
                                } else {
                                    assetDetailValues.append(",");
                                    assetDetailValues.append(AppUtils.QT(""));
                                }
                                assetDetailValues.append(",");
                                assetDetailValues.append(AppUtils.QT(assetBo.getNFCTagId()));
                                assetDetailValues.append(",");
                                assetDetailValues.append(DatabaseUtils
                                        .sqlEscapeString(SHOW_ASSET_INSTALL_DATE ? ((assetBo
                                                .getInstallDate() == null || assetBo
                                                .getInstallDate()
                                                .length() == 0) ? SDUtil
                                                .now(SDUtil.DATE_GLOBAL)
                                                : (DateUtil
                                                .convertToServerDateFormat(
                                                        assetBo.getInstallDate(),
                                                        ConfigurationMasterHelper.outDateFormat)))
                                                : ""));
                                assetDetailValues.append(",");
                                assetDetailValues.append(DatabaseUtils
                                        .sqlEscapeString(SHOW_ASSET_SERVICE_DATE ? ((assetBo
                                                .getServiceDate() == null || assetBo
                                                .getServiceDate()
                                                .length() == 0) ? SDUtil
                                                .now(SDUtil.DATE_GLOBAL)
                                                : (DateUtil
                                                .convertToServerDateFormat(
                                                        assetBo.getServiceDate(),
                                                        ConfigurationMasterHelper.outDateFormat)))
                                                : ""));


                                db.insertSQL(DataMembers.tbl_SerializedAssetDetail,
                                        AssetDetailColumns,
                                        assetDetailValues.toString());

                                if (assetBo.getImageList().size() > 0) {
                                    for (String imageName : assetBo.getImageList()) {
                                        StringBuffer assetImgInofValues = new StringBuffer();
                                        assetImgInofValues.append(id);
                                        assetImgInofValues.append(",");
                                        assetImgInofValues.append(assetBo.getAssetID());
                                        assetImgInofValues.append(",");
                                        assetImgInofValues.append(AppUtils.QT(imageName));
                                        assetImgInofValues.append(",");
                                        assetImgInofValues.append(AppUtils.QT(assetBo.getNFCTagId()));
                                        assetImgInofValues.append(",");
                                        assetImgInofValues.append(AppUtils.QT(assetBo.getImgName()));

                                        db.insertSQL(DataMembers.tbl_SerializedAssetImageDetail,
                                                AssetImageInfoColumns,
                                                assetImgInofValues.toString());
                                    }
                                }
                            }


                    }


            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    private SerializedAssetBO getAssetTrackingBO() {
        return mAssetTrackingBO;
    }

    public void setAssetTrackingBO(SerializedAssetBO mAssetTrackingBO) {
        this.mAssetTrackingBO = mAssetTrackingBO;
    }


    /**
     * Method to save Asset Details in sql table
     */
    public void saveNewAsset(Context mContext) {

        if (mUniqueSerialNo == null)
            mUniqueSerialNo = new HashMap<>();


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            SerializedAssetBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId";

            String assetAddAndDeleteValues = id + ","
                    + AppUtils.QT(assets.getPOSM()) + ","
                    + AppUtils.QT(assets.getSNO()) + ","
                    + AppUtils.QT(assets.getNFCTagId()) + ","
                    + AppUtils.QT(DateUtil.convertToServerDateFormat(assets.getNewInstallDate(), ConfigurationMasterHelper.outDateFormat)) + ","
                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + "'I'"+","
                    + AppUtils.QT(assets.getReasonId()) + ","
                    + AppUtils.QT(assets.getRemarks())+","
                    +mBusinessModel.getRetailerMasterBO().getRetailerID()+","
                    +"0"+","
                    +"'WH_RTR'"+","
                    +0;

            db.insertSQL(DataMembers.tbl_SerializedAssetTransfer, addAssetColumns,
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
     * Download unique POSM available
     *
     * @param moduleName Module Name
     */
    public void downloadUniqueAssets(Context mContext, String moduleName) {

        AssetAddDetailBO assetBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String sb = "select distinct  AssetId,AssetName from SerializedAssetMaster";



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

    public Vector<String> getAssetNames() {
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
     * Download asset brands
     *
     * @param brandPosm POSM Id
     */
    public void downloadAssetBrand(Context mContext, String brandPosm) {

        AssetAddDetailBO assetBO;
        mBrandSpinner = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String sb = "SELECT PM.pid,PM.PName FROM ProductMaster PM INNER JOIN SerializedAssetProductMapping PO ON PM.Pid = PO.Productid WHERE PO.AssetId =" + AppUtils.QT(brandPosm);

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
     * Get asset Id for given asset name
     *
     * @param mAssetName Asset name
     * @return Asset Id
     */
    public String getAssetIds(String mAssetName) {
        AssetAddDetailBO brand;

        try {
            int siz = mAssetSpinner.size();
            if (siz == 0)
                return null;

            for (int i = 0; i < siz; ++i) {
                brand = mAssetSpinner.get(i);
                Commons.print("brand.getPOSMDescription()="
                        + brand.getPOSMDescription() + "," + mAssetName);
                if (brand.getPOSMDescription().equals(mAssetName)) {

                    return brand.getPOSMId();

                }

            }
        } catch (Exception ex) {
            Commons.printException("" + ex);
        }
        return mAssetName;
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
     * Preparing List to Add or remove assets
     *
     * @param moduleName Module Name
     */
    public void loadRemovableAssets(Context mContext, String moduleName) {
        String type;

        SerializedAssetBO assetBO;

        mRemovableAssets = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            mBusinessModel.productHelper.getRetailerlevel(moduleName);


            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.AssetId,P.AssetName,SAM.SerialNumber,SBD.Productid,AllocationRefId  from SerializedAssetMaster P  inner join SerializedAssetProductMapping SBD on P.AssetId=SBD.AssetId ");
            sb.append(" inner join SerializedAssetMapping SAM ON SAM.assetId=P.AssetId");
            sb.append(" where (SAM.SerialNumber  in (select distinct SerialNumber from SerializedAssetTransfer AAD where Transfer_Type!='RTR_WH'");
            sb.append(") or SAM.SerialNumber not in (select distinct SerialNumber from SerializedAssetTransfer AAD1");
            sb.append(")) and retailerid in (0,"+mBusinessModel.QT(mBusinessModel.getRetailerMasterBO().getRetailerID())+")");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetBO = new SerializedAssetBO();
                    assetBO.setPOSM(c.getString(0));
                    assetBO.setPOSMName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2)) && !"".equals(c.getString(2))) {
                        assetBO.setSNO(c.getString(2));

                    } else {
                        assetBO.setSNO("0");
                    }
                    assetBO.setNewInstallDate(" ");
                    assetBO.setFlag("N");
                    assetBO.setBrand(c.getString(3));
                    assetBO.setReferenceId(c.getInt(4));
                    mRemovableAssets.add(assetBO);
                }
            }
            String sb1 = "select distinct  P.AssetId,P.AssetName,serialNumber,installdate  from SerializedAssetMaster P  inner  join SerializedAssetTransfer AAD on P.AssetId=AAD.AssetId where Transfer_Type!='RTR_WH'  and retailerid=" +
                    AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

            Cursor c1 = db.selectSQL(sb1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    assetBO = new SerializedAssetBO();
                    assetBO.setPOSM(c1.getString(0));
                    assetBO.setPOSMName(c1.getString(1));
                    if ("null".equals(c1.getString(2)) || "".equals(c1.getString(2))) {

                        assetBO.setSNO("0");
                    } else {
                        assetBO.setSNO(c1.getString(2));
                    }

                    assetBO.setNewInstallDate(c1.getString(3));
                    assetBO.setFlag("Y");
                    assetBO.setSBDId(" ");
                    //assetBO.setBrand(c1.getString(4));
                    mRemovableAssets.add(assetBO);
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

    public Vector<SerializedAssetBO> getRemovableAssets() {
        return mRemovableAssets;
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
    public void deleteAsset(Context mContext, String posmId, String mSno,
                            String mSbdId, String mBrandId, String reasonId, String moduleName, String NFCId,int refId) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);


            String columns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId";

            String values = id + ","
                    + AppUtils.QT(posmId) + "," + AppUtils.QT(mSno) + ","+AppUtils.QT(NFCId)+","+AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))+","
                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + AppUtils.QT("T") + ","
                    + AppUtils.QT(reasonId) + "," + AppUtils.QT("") + "," + mBusinessModel.getRetailerMasterBO().getRetailerID()+","+0 + "," + AppUtils.QT("RTR_WH")+","+refId;

            db.insertSQL(DataMembers.tbl_SerializedAssetTransfer, columns,
                    values);

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
    public void deleteAssetTransaction(Context mContext, String mSno) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_SerializedAssetTransfer, "serialNumber ="
                    + AppUtils.QT(mSno), false);

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
     * Method to check the movement Asset in sql table
     */
    public ArrayList<String> getAssetMovementDetails(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        ArrayList<String> retailerMovedData = new ArrayList<>();
        Cursor c = db.selectSQL("SELECT DISTINCT AssetId from " + DataMembers.tbl_SerializedAssetTransfer + " where RequestType='T'");
        if (c != null)
            while (c.moveToNext()) {
                retailerMovedData.add(c.getString(0));
            }
        return retailerMovedData;
    }

    /**
     * Method to save Asset Movement Details in sql table
     */
    public void saveAssetMovementDetails(Context mContext, String movementType,int referenceId) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            SerializedAssetBO assets = getAssetTrackingBO();
            String columns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId";


            String values = id + "," +AppUtils.QT(assets.getPOSM())+"," + AppUtils.QT(assets.getSNO()) + ","
                    + AppUtils.QT(assets.getNFCTagId()) + "," +AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))+","
                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + AppUtils.QT("T") + ","  +
                    AppUtils.QT(assets.getReasonId()) + "," + AppUtils.QT(assets.getRemarks()) + ","+mBusinessModel.getRetailerMasterBO().getRetailerID()+"," + AppUtils.QT(assets.getToRetailerId())+","+AppUtils.QT(movementType)+","+referenceId;


            db.insertSQL(DataMembers.tbl_SerializedAssetTransfer, columns,
                    values);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void getAssetService(Context mContext, String moduleName) {

        SerializedAssetBO assetBO;

        assetServiceList = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();

            mBusinessModel.productHelper.getRetailerlevel(moduleName);

            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.AssetId,P.AssetName,SAM.SerialNumber,SBD.Productid  from SerializedAssetMaster P  inner join SerializedAssetProductMapping SBD on P.AssetId=SBD.AssetId ");
            sb.append(" inner join SerializedAssetMapping SAM ON SAM.assetId=P.AssetId");
            sb.append(" where (SAM.SerialNumber  in (select distinct SerialNumber from SerializedAssetTransfer AAD where Transfer_Type!='RTR_WH'");
            sb.append(") or SAM.SerialNumber not in (select distinct SerialNumber from SerializedAssetTransfer AAD1");
            sb.append("))");


            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    assetBO = new SerializedAssetBO();
                    assetBO.setPOSM(c.getString(0));
                    assetBO.setPOSMName(c.getString(1));
                    if (c.getString(2) != null && !"null".equals(c.getString(2)) && !"".equals(c.getString(2))) {
                        assetBO.setSNO(c.getString(2));


                    } else {
                        assetBO.setSNO("0");
                    }
                    assetBO.setNewInstallDate(" ");
                    assetBO.setFlag("N");
                    assetBO.setBrand(c.getString(3));
                    assetServiceList.add(assetBO);
                }
            }
            String sb1 = "select distinct  P.AssetId,P.AssetName,serialNumber,installdate  from SerializedAssetMaster P  inner  join SerializedAssetTransfer AAD on P.AssetId=AAD.AssetId where Transfer_Type!='RTR_WH'  and retailerid=" +
                    AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

            Cursor c1 = db.selectSQL(sb1);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    assetBO = new SerializedAssetBO();
                    assetBO.setPOSM(c1.getString(0));
                    assetBO.setPOSMName(c1.getString(1));
                    if ("null".equals(c1.getString(2)) || "".equals(c1.getString(2))) {

                        assetBO.setSNO("0");
                    } else {
                        assetBO.setSNO(c1.getString(2));
                    }

                    assetBO.setNewInstallDate(c1.getString(3));
                    assetBO.setFlag("Y");
                    assetBO.setSBDId(" ");
                    //assetBO.setBrand(c1.getString(6));
                    assetServiceList.add(assetBO);
                }
            }


            String query = "select distinct  P.AssetId,P.AssetName,serialNumber,reasonid  from SerializedAssetMaster P  inner  join SerializedAssetServiceRequest AAD on P.AssetId=AAD.AssetId Where retailerid=" +
                    AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());


            Cursor c2 = db.selectSQL(query);
            if (c2.getCount() > 0) {
                while (c2.moveToNext()) {
                    int reasonId = c2.getInt(c2.getColumnIndex("reasonid"));
                    String serialNum = c2.getString(c2.getColumnIndex("serialNumber"));
                    int assetId = c2.getInt(c2.getColumnIndex("AssetId"));

                    for (int i = 0; i < assetServiceList.size(); i++) {
                        SerializedAssetBO assetTrackingBO = assetServiceList.get(i);
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

    public void setAssetServiceList(Vector<SerializedAssetBO> assetServiceList) {
        this.assetServiceList = assetServiceList;
    }

    public Vector<SerializedAssetBO> getAssetServiceList() {
        return assetServiceList;
    }

    public void deleteServiceTable(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            db.deleteSQL("SerializedAssetServiceRequest", "retailerid=" + AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID()), false);
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();
    }

    public void saveAssetServiceDetails(Context mContext, String assetId, String serialNo, String mReasonID, String moduleName) {

        if (mUniqueSerialNo == null)
            mUniqueSerialNo = new HashMap<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            String id = mBusinessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String addAssetColumns = "Uid,date,AssetId,serialNumber,reasonid,retailerid";

            String assetAddAndDeleteValues = id + ","
                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + AppUtils.QT(assetId) + "," + AppUtils.QT(serialNo) + ","
                    + AppUtils.QT(mReasonID) + ","
                    + AppUtils.QT(mBusinessModel.getRetailerMasterBO().getRetailerID());

            db.insertSQL(DataMembers.tbl_SerializedAssetServiceRequest, addAssetColumns,
                    assetAddAndDeleteValues);

            //add serial no for uniqueness
            mUniqueSerialNo.put(serialNo, serialNo);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

}
