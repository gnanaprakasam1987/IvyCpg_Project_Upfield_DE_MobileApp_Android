package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.cpg.view.asset.bo.AssetAddDetailBO;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
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
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

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
    private static final String ASSET_BARCODE_REASON = "BARCODE_REASON";
    private static final String CODE_ASSET_SERVICE = "SAT11";
    private static final String CODE_NEW_ASSET_PHOTO = "SAT12";
    private static final String CODE_HIDE_SERIAL_NO_REASON = "SAT13";
    private static final String CODE_SHOW_ASSET_RENTAL_PRICE_AND_EFF_DATE_AND_SNO = "SAT14";
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
    public boolean NEW_ASSET_PHOTO;
    public boolean NEW_ASSET_PHOTO_MANDATORY;
    public boolean SHOW_ASSET_VENDOR;
    public boolean SHOW_ASSET_CAPACITY;
    public boolean SHOW_ASSET_MODEL;
    public boolean SHOW_ASSET_TYPE;
    public boolean SHOW_SERIAL_NO_REASON;
    public boolean IS_SERIAL_NO_NOT_MANDATORY;
    public boolean SHOW_ASSET_RENTAL_PRICE;
    public boolean SHOW_ASSET_EFFECTIVE_DATE;
    public boolean SHOW_SERIAL_NO_IN_UPDATE_REQUEST;
    boolean SHOW_ASSET_SURVEY_OPTION;
    boolean SHOW_ASSET_SIH;


    //

    private ArrayList<ReasonMaster> mAssetReasonList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetConditionList = new ArrayList<>();
    private ArrayList<ReasonMaster> mAssetBCReasonList = new ArrayList<>();
    private HashMap<String, String> mUniqueSerialNo;
    private ArrayList<SerializedAssetBO> mAssetTrackingList = new ArrayList<>();
    private Vector<SerializedAssetBO> mRemovableAssets = null;
    private Vector<SerializedAssetBO> assetServiceList = null;


    private ArrayList<SerializedAssetBO> mAllAssetTrackingList = null;
    //

    private static final String MENU_SERIALIZED_ASSET = "MENU_SERIALIZED_ASSET";

    private String outletLastVisitID;


    private SerializedAssetHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }

    public static SerializedAssetHelper getInstance(Context context) {
        if (instance == null)
            instance = new SerializedAssetHelper(context);

        return instance;
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
            surveyHelperNew.downloadQuestionDetails(mMenuCode, null);
            surveyHelperNew.loadSurveyAnswers(0, null);
            mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
            mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                    mBusinessModel.productHelper.getRetailerModuleSequenceValues(), false));

        }

        //update configurations
        downloadConfigs(mContext);

        //download filter levels
        mBusinessModel.productHelper.setFilterProductLevelsRex(mBusinessModel.productHelper.downloadFilterLevel(mMenuCode));
        mBusinessModel.productHelper.setFilterProductsByLevelIdRex(mBusinessModel.productHelper.downloadFilterLevelProducts(
                mBusinessModel.productHelper.getRetailerModuleSequenceValues(), false));

        // Load master records
        downloadAssetMaster(mContext, mMenuCode);

        //get OutletTimeStamp VisitID
        getLastRetailerVisitId(mContext);

        // Load data from transaction
        loadAssetData(mContext, mBusinessModel
                .getAppDataProvider().getRetailMaster().getRetailerID(), mMenuCode);
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
            ASSET_PHOTO_VALIDATION = false;
            SHOW_NFC_SEARCH_IN_ASSET = false;
            NEW_ASSET_PHOTO = false;
            NEW_ASSET_PHOTO_MANDATORY = false;
            SHOW_ASSET_VENDOR = false;
            SHOW_ASSET_MODEL = false;
            SHOW_ASSET_CAPACITY = false;
            SHOW_ASSET_TYPE = false;
            SHOW_SERIAL_NO_REASON = false;
            IS_SERIAL_NO_NOT_MANDATORY = false;
            SHOW_ASSET_RENTAL_PRICE = false;
            SHOW_ASSET_EFFECTIVE_DATE = false;
            SHOW_SERIAL_NO_IN_UPDATE_REQUEST = false;
            SHOW_ASSET_SURVEY_OPTION = false;
            SHOW_ASSET_SIH = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                    else if (CODE_NEW_ASSET_PHOTO.equalsIgnoreCase(c.getString(0))) {
                        NEW_ASSET_PHOTO = true;
                        if ("1".equals(c.getString(1)))
                            NEW_ASSET_PHOTO_MANDATORY = true;
                    } else if (CODE_HIDE_SERIAL_NO_REASON.equalsIgnoreCase(c.getString(0))) {
                        SHOW_SERIAL_NO_REASON = true;
                        if ("1".equals(c.getString(1)))
                            IS_SERIAL_NO_NOT_MANDATORY = true;
                    }
                }
                c.close();
            }

            String codeValue = null;
            sql = "SELECT RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE hhtCode =" + StringUtils.getStringQueryParam(CODE_SHOW_ASSET_RENTAL_PRICE_AND_EFF_DATE_AND_SNO) +
                    " AND menu_type = 'MENU_SERIALIZED_ASSET' AND flag='1' and ForSwitchSeller = 0";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "RP":
                            SHOW_ASSET_RENTAL_PRICE = true;
                            break;
                        case "ED":
                            SHOW_ASSET_EFFECTIVE_DATE = true;
                            break;
                        case "SNO":
                            SHOW_SERIAL_NO_IN_UPDATE_REQUEST = true;
                            break;
                        case "SIH":
                            SHOW_ASSET_SIH = true;
                            break;
                    }
                }
            }


            db.closeDB();

            if (SHOW_ASSET_REASON)
                downloadReasons(mContext, ASSET_REASON);
            if (SHOW_ASSET_CONDITION)
                downloadReasons(mContext, ASSET_CONDITION);
            if (SHOW_REMARKS_ASSET)
                downloadReasons(mContext, ASSET_REMARK);
            if (SHOW_ASSET_BARCODE)
                downloadReasons(mContext, ASSET_BARCODE_REASON);

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
            case "VDR":
                SHOW_ASSET_VENDOR = true;
                break;
            case "MOD":
                SHOW_ASSET_MODEL = true;
                break;
            case "CAP":
                SHOW_ASSET_CAPACITY = true;
                break;
            case "TYPE":
                SHOW_ASSET_TYPE = true;
                break;
            case "ASSETSURV":
                SHOW_ASSET_SURVEY_OPTION = true;
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
            case ASSET_CONDITION:
                mAssetConditionList = new ArrayList<>();
                break;
            case ASSET_BARCODE_REASON:
                mAssetBCReasonList = new ArrayList<>();
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
                        case ASSET_CONDITION:
                            mAssetConditionList.add(reasonBO);
                            break;
                        case ASSET_BARCODE_REASON:
                            mAssetBCReasonList.add(reasonBO);
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

        mAllAssetTrackingList = new ArrayList<>();
        mAssetTrackingList = new ArrayList<>();
        mUniqueSerialNo = new HashMap<>();

        SerializedAssetBO assetTrackingBO;
        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            sb.append("select Distinct A.assetId,A.assetName,B.serialNumber,'',B.NFCNumber,'' as ParentHierarchy,AllocationRefId,");
            sb.append("ifnull(A.assettype,'') as AssetTypeId,A.capacity as capacity,A.vendorid as vendorid,A.modelid as modelid,ifnull(SAVM.name,'') as name,");
            sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=A.modelid ");
            sb.append("and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName,");
            sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=A.AssetType ");
            sb.append("and SLM.ListType='ASSET_TYPE'),'') as AssetType,");
            sb.append("IFNULL(B.installDate,'') as installDate,IFNULL(B.lastserviceDate,'') as serviceDate,");
            sb.append("B.rentPrice as RentalPrice,B.FromDate as effFromDate,IFNULL(B.ToDate,'') as effToDate,A.Price as AssetPrice,A.SIH as AssetSIH,IFNULL(AH.EntityReferenceNo,'') as surveyDone  ");
            sb.append("from SerializedAssetMaster A ");
            sb.append("inner join SerializedAssetMapping B on A.AssetId=B.AssetId ");
            sb.append("left join SerializedAssetProductMapping C on C.AssetId=A.AssetId ");
            sb.append("left join SerializedAssetVendorMaster SAVM on SAVM.id=A.vendorid ");
            sb.append("left join AnswerHeader AH on AH.EntityReferenceNo=B.SerialNumber ");

            String allMasterSb = sb.toString();

            sb.append("Where B.Retailerid in(0,");
            sb.append(StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID())).append(")");

            sb.append(" GROUP BY B.RetailerId,B.AssetId,B.SerialNumber ORDER BY B.RetailerId");

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


                    assetTrackingBO.setNFCTagId(c.getString(c.getColumnIndex("NFCNumber")));
                    assetTrackingBO.setParentHierarchy("");
                    assetTrackingBO.setReferenceId(c.getString(c.getColumnIndex("AllocationRefId")));
                    assetTrackingBO.setCapacity(c.getInt(c.getColumnIndex("capacity")));
                    assetTrackingBO.setVendorId(c.getString(c.getColumnIndex("vendorid")));
                    assetTrackingBO.setVendorName(c.getString(c.getColumnIndex("name")));
                    assetTrackingBO.setModelId(c.getString(c.getColumnIndex("modelid")));
                    assetTrackingBO.setModelName(c.getString(c.getColumnIndex("ModelName")));
                    assetTrackingBO.setAssetTypeId(c.getString(c.getColumnIndex("AssetTypeId")));
                    assetTrackingBO.setAssetType(c.getString(c.getColumnIndex("AssetType")));

                    assetTrackingBO.setmLastInstallDate(
                            ((!c.getString(c.getColumnIndex("installDate")).isEmpty()) ? DateTimeUtils
                                    .convertFromServerDateToRequestedFormat(c.getString(c.getColumnIndex("installDate")), ConfigurationMasterHelper.outDateFormat) : ""));

                    assetTrackingBO.setInstallDate(
                            ((!c.getString(c.getColumnIndex("installDate")).isEmpty()) ? DateTimeUtils
                                    .convertFromServerDateToRequestedFormat(c.getString(c.getColumnIndex("installDate")), ConfigurationMasterHelper.outDateFormat) : ""));

                    assetTrackingBO.setServiceDate(
                            ((!c.getString(c.getColumnIndex("serviceDate")).isEmpty()) ? DateTimeUtils
                                    .convertFromServerDateToRequestedFormat(c.getString(c.getColumnIndex("serviceDate")), ConfigurationMasterHelper.outDateFormat) : ""));

                    assetTrackingBO.setRentalPrice(c.getDouble(c.getColumnIndex("RentalPrice")));
                    assetTrackingBO.setEffectiveFromDate(DateTimeUtils.convertFromServerDateToRequestedFormat(c.getString(c.getColumnIndex("effFromDate")), ConfigurationMasterHelper.outDateFormat));
                    assetTrackingBO.setEffectiveToDate(DateTimeUtils.convertFromServerDateToRequestedFormat(c.getString(c.getColumnIndex("effToDate")), ConfigurationMasterHelper.outDateFormat));
                    assetTrackingBO.setAssetPrice(c.getDouble(c.getColumnIndex("AssetPrice")));
                    assetTrackingBO.setSihQty(c.getInt(c.getColumnIndex("AssetSIH")));
                    assetTrackingBO.setSurveyDone((!c.getString(c.getColumnIndex("surveyDone")).isEmpty()));
                    mAssetTrackingList.add(assetTrackingBO);
                }

            }

            sb = new StringBuilder();

            sb.append("select B.AssetId,replace(Group_Concat(PM.ParentHierarchy),',','') AS ParentHierarchy from SerializedAssetMaster A  ");
            sb.append("inner join SerializedAssetMapping B on A.AssetId=B.AssetId ");
            sb.append("left join SerializedAssetProductMapping C on C.AssetId=A.AssetId ");
            sb.append("left join ProductMaster PM on PM.PID=C.Productid ");
            sb.append("Where Retailerid in(0,");
            sb.append(StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ")");
            sb.append(" GROUP BY B.AssetId,B.SerialNumber ORDER BY RetailerId");
            Cursor c2 = db.selectSQL(sb.toString());
            if (c2.getCount() > 0) {
                while (c2.moveToNext()) {
                    for (SerializedAssetBO serial : mAssetTrackingList) {
                        if (serial.getAssetID() == c2.getInt(0))
                            serial.setParentHierarchy(c2.getString(1));
                    }
                }
            }

            //load serial no's into hash map for uniqueness

            if (MENU_SERIALIZED_ASSET.equals(moduleName)) {
                String sb1 = "select SerialNumber from SerializedAssetMapping A INNER JOIN SerializedAssetMaster B ON A.assetId=B.assetId ";
                c = db.selectSQL(sb1);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (c.getString(0) != null)
                            mUniqueSerialNo.put(c.getString(0), c.getString(0));
                    }
                }
            }


            String sb1 = "select  serialNumber from SerializedAssetTransfer where transfer_type='RTR_WH' and retailerid=" + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID();
            Cursor cursorDelete = db.selectSQL(sb1);
            SerializedAssetBO assetBoDelete;
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


                    assetTrackingBO.setNFCTagId(c1.getString(c1.getColumnIndex("NFCNumber")));
                    assetTrackingBO.setParentHierarchy("");

                    mAllAssetTrackingList.add(assetTrackingBO);
                }
            }

            sb = new StringBuilder();

            sb.append("select B.AssetId,replace(Group_Concat(PM.ParentHierarchy),',','') AS ParentHierarchy from SerializedAssetMaster A  ");
            sb.append("inner join SerializedAssetMapping B on A.AssetId=B.AssetId ");
            sb.append("left join SerializedAssetProductMapping C on C.AssetId=A.AssetId ");
            sb.append("left join ProductMaster PM on PM.PID=C.Productid ");
            sb.append(" GROUP BY B.AssetId,B.SerialNumber ORDER BY RetailerId");
            Cursor c3 = db.selectSQL(sb.toString());
            if (c3.getCount() > 0) {
                while (c3.moveToNext()) {
                    for (SerializedAssetBO serial : mAllAssetTrackingList) {
                        if (serial.getAssetID() == c3.getInt(0))
                            serial.setParentHierarchy(c3.getString(1));
                    }
                }
            }

            c.close();
            c1.close();
            c2.close();
            c3.close();
            db.closeDB();
            mAssetTrackingList = removeMovedAsset(mContext);
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();

        }
    }

    public ArrayList<SerializedAssetBO> removeMovedAsset(Context mContext) {
        try {
            ArrayList<String> mMovedList = getAssetMovementDetails(mContext);
            ArrayList<Integer> toRemovePos = new ArrayList<>();
            if (mAssetTrackingList != null && mAssetTrackingList.size() > 0) {
                if (mMovedList != null && mMovedList.size() > 0) {
                    for (int i = 0; i < mMovedList.size(); i++) {
                        String tempMoved = mMovedList.get(i);
                        for (int j = 0; j < mAssetTrackingList.size(); j++) {
                            if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !mAssetTrackingList.get(j).getParentHierarchy().contains("/" + mBusinessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (tempMoved.equalsIgnoreCase(String.valueOf(mAssetTrackingList.get(j).getAssetID()) + mAssetTrackingList.get(j).getSerialNo())) {
                                toRemovePos.add(j);
                            }
                        }
                    }
                    ArrayList<SerializedAssetBO> assetTrackingList = new ArrayList<>();
                    for (int i = 0; i < mAssetTrackingList.size(); i++) {
                        if (!toRemovePos.contains(i)) {
                            assetTrackingList.add(mAssetTrackingList.get(i));
                        }
                    }
                    mAssetTrackingList = assetTrackingList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAssetTrackingList;
    }


    /**
     * Method that to get loaded data from SQLite table
     *
     * @param mRetailerId Retailer ID
     * @param moduleName  Module Name
     */
    private void loadAssetData(Context mContext, String mRetailerId, String moduleName) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();

            String sb = "select uid, IFNULL(remarks,'') from SerializedAssetHeader where retailerid=" + StringUtils.getStringQueryParam(mRetailerId) +
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
                    StringUtils.getStringQueryParam(uid);


            Cursor detailCursor = db.selectSQL(sb2);
            if (detailCursor.getCount() > 0) {
                while (detailCursor.moveToNext()) {
                    int mAssetId = detailCursor.getInt(0);
                    int isAvailable = detailCursor.getInt(1);
                    String reasonId = detailCursor.getString(2);

                    String serialNo = detailCursor.getString(4);
                    String conditionId = detailCursor.getString(3);
                    String nfcTagID = detailCursor.getString(5);


                    setAssetDetails(mContext,
                            mAssetId,
                            isAvailable,
                            reasonId,
                            serialNo,
                            conditionId,
                            DateTimeUtils.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(6),
                                    ConfigurationMasterHelper.outDateFormat),
                            DateTimeUtils.convertFromServerDateToRequestedFormat(
                                    detailCursor.getString(7),
                                    ConfigurationMasterHelper.outDateFormat), nfcTagID);
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
     * @param assetID     Asset Id
     * @param isAvailable Qty
     * @param installDate Installed date
     * @param mReasonId   reason Id
     * @param serialNo    serial Number
     * @param serviceDate service date
     */
    private void setAssetDetails(Context mcontext, int assetID, int isAvailable,
                                 String mReasonId, String serialNo,
                                 String conditionId, String installDate, String serviceDate, String nfcTagID) {

        SerializedAssetBO assetBO;

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
                        assetBO.setNFCTagId(nfcTagID);
                        assetBO.setImageList(getImagesList(mcontext, assetID, assetBO.getNFCTagId(), assetBO));

                    }

                }
            }

        }
    }


    private ArrayList<String> getImagesList(Context mContext, int assetId, String serialNo, SerializedAssetBO assetBO) {
        ArrayList<String> imageList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ImageName,imgName from SerializedAssetImageDetails "
                    + " where AssetID = " + assetId + " and serialNumber = " + StringUtils.getStringQueryParam(serialNo)
                    + " AND Upload = " + StringUtils.getStringQueryParam("N");
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

    public ArrayList<ReasonMaster> getmAssetBCReasonList() {
        if (mAssetBCReasonList != null) {
            return mAssetBCReasonList;
        }
        return new ArrayList<>();
    }

    public void deleteImageProof(Context mContext, String ImageName) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            db.deleteSQL(DataMembers.tbl_AssetImgInfo, "ImageName LIKE"
                    + StringUtils.getStringQueryParam(ImageName + "%"), false);
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

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            String query = "select uid from SerializedAssetHeader where retailerid ="
                    + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
            query += " and upload='N'";

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                c.moveToNext();
                db.deleteSQL(DataMembers.tbl_SerializedAssetHeader,
                        "uid=" + StringUtils.getStringQueryParam(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_SerializedAssetDetail,
                        "uid=" + StringUtils.getStringQueryParam(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_SerializedAssetImageDetail,
                        "uid=" + StringUtils.getStringQueryParam(c.getString(0)), false);
            }


            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            boolean isData = false;
            double totAchievedScr = 0, totTargetSrc = 0;

            String assetHeaderColumns = "uid,DateTime,RetailerId,remarks,VisitId";
            String AssetDetailColumns = "uid,AssetID,isAvailable,ReasonID,SerialNumber,conditionId,NFCNumber,installdate,lastServicedate,RetailerID";
            String AssetImageInfoColumns = "uid,AssetID,ImageName,serialNumber,imgName,RetailerID";
            String surveyScore;
            if (SHOW_ASSET_SURVEY_OPTION
                    && mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                assetHeaderColumns += ",Score,TargetScore";
                AssetDetailColumns += ",Score,TargetScore";
            }

            for (SerializedAssetBO assetBo : mAssetTrackingList) {
                StringBuilder assetDetailValues = new StringBuilder();
                String surveyAchSrc = "0", surveyTgtSrc = "0";
                if (assetBo.getAvailQty() > 0
                        || !assetBo.getReason1ID().equals(Integer.toString(0))) {

                    if (SHOW_ASSET_SURVEY_OPTION
                            && mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                        surveyScore = getSurveyScore(db, assetBo.getSerialNo());
                        surveyAchSrc = surveyScore.split(",")[0];
                        surveyTgtSrc = surveyScore.split(",")[1];
                    }

                    assetDetailValues.append(id);
                    assetDetailValues.append(",");
                    assetDetailValues.append(assetBo.getAssetID());
                    assetDetailValues.append(",");
                    assetDetailValues.append(assetBo.getAvailQty());
                    assetDetailValues.append(",");
                    assetDetailValues.append(assetBo.getReason1ID());
                    assetDetailValues.append(",");
                    assetDetailValues.append(StringUtils.getStringQueryParam(assetBo.getSerialNo()));
                    if (assetBo.getConditionID() != null && !"null".equals(assetBo.getConditionID())) {
                        assetDetailValues.append(",");
                        assetDetailValues.append(StringUtils.getStringQueryParam(assetBo.getConditionID()));
                    } else {
                        assetDetailValues.append(",");
                        assetDetailValues.append(StringUtils.getStringQueryParam(""));
                    }
                    assetDetailValues.append(",");
                    assetDetailValues.append(StringUtils.getStringQueryParam(assetBo.getNFCTagId()));
                    assetDetailValues.append(",");
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
                    assetDetailValues.append(",");
                    assetDetailValues.append(StringUtils
                            .getStringQueryParam(mBusinessModel.getAppDataProvider()
                                    .getRetailMaster().getRetailerID()));

                    if (SHOW_ASSET_SURVEY_OPTION
                            && mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                        assetDetailValues.append(",").append(StringUtils.getStringQueryParam(surveyAchSrc));
                        assetDetailValues.append(",").append(surveyTgtSrc);
                        totAchievedScr += SDUtil.convertToInt(surveyAchSrc);
                        totTargetSrc += SDUtil.convertToInt(surveyTgtSrc);
                    }

                    db.insertSQL(DataMembers.tbl_SerializedAssetDetail,
                            AssetDetailColumns,
                            assetDetailValues.toString());
                    isData = true;

                    if (assetBo.getImageList().size() > 0) {
                        for (String imageName : assetBo.getImageList()) {

                            String assetImgInofValues = id +
                                    "," +
                                    assetBo.getAssetID() +
                                    "," +
                                    StringUtils.getStringQueryParam(imageName) +
                                    "," +
                                    StringUtils.getStringQueryParam(assetBo.getNFCTagId()) +
                                    "," +
                                    StringUtils.getStringQueryParam(assetBo.getImgName()) +
                                    "," +
                                    StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
                            db.insertSQL(DataMembers.tbl_SerializedAssetImageDetail,
                                    AssetImageInfoColumns,
                                    assetImgInofValues);
                        }
                    }
                }

            }


            if (isData) {
                StringBuilder assetHeaderValues = new StringBuilder();
                assetHeaderValues.append(id);
                assetHeaderValues.append(",");
                assetHeaderValues.append(StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                assetHeaderValues.append(",");
                assetHeaderValues.append(StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()));
                assetHeaderValues.append(",");
                assetHeaderValues.append(StringUtils.getStringQueryParam(mBusinessModel.getAssetRemark()));
                assetHeaderValues.append(",");
                assetHeaderValues.append(StringUtils.getStringQueryParam(outletLastVisitID));

                if (SHOW_ASSET_SURVEY_OPTION
                        && mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                    assetHeaderValues.append(",");
                    assetHeaderValues.append(StringUtils.getStringQueryParam(String.valueOf(totAchievedScr)));
                    assetHeaderValues.append(",");
                    assetHeaderValues.append(StringUtils.getStringQueryParam(String.valueOf(totTargetSrc)));
                }

                db.insertSQL(DataMembers.tbl_SerializedAssetHeader, assetHeaderColumns,
                        assetHeaderValues.toString());
            }

            if (mBusinessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                String moduleCode = DataMembers.FIT_ASSET;
                double sum = (double) 100 / totTargetSrc * totAchievedScr;
                mBusinessModel.calculateFitscoreandInsert(db, sum, moduleCode);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }


    private String getSurveyScore(DBUtil db, String serialNo) {
        String surveyScore = null;
        try {
            String sql = "SELECT IFNULL(SUM(achScore),0),IFNULL(SUM(tgtScore),0) FROM AnswerHeader WHERE EntityReferenceNo=" + StringUtils.getStringQueryParam(serialNo);
            Cursor c = db.selectSQL(sql);
            if (c != null
                    && c.getCount() > 0) {
                if (c.moveToNext()) {
                    surveyScore = c.getString(0);
                    surveyScore += "," + c.getString(1);
                }
                c.close();
            }
            return surveyScore;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return surveyScore;
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


        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            SerializedAssetBO assets = getAssetTrackingBO();
            String addAssetColumns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId,rentPrice,ToDate,VisitId";

            String assetAddAndDeleteValues = id + ","
                    + StringUtils.getStringQueryParam(assets.getPOSM()) + ","
                    + StringUtils.getStringQueryParam(assets.getSNO()) + ","
                    + StringUtils.getStringQueryParam(assets.getNFCTagId()) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.convertToServerDateFormat(assets.getNewInstallDate(), ConfigurationMasterHelper.outDateFormat)) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + "'I'" + ","
                    + StringUtils.getStringQueryParam(assets.getReasonId()) + ","
                    + StringUtils.getStringQueryParam(assets.getRemarks()) + ","
                    + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ","
                    + "0" + ","
                    + "'WH_RTR'" + ","
                    + 0 + ","
                    + SDUtil.getWithoutExponential(assets.getRentalPrice()) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.convertToServerDateFormat(assets.getEffectiveToDate(), ConfigurationMasterHelper.outDateFormat)) + ","
                    + StringUtils.getStringQueryParam(outletLastVisitID);

            db.insertSQL(DataMembers.tbl_SerializedAssetTransfer, addAssetColumns,
                    assetAddAndDeleteValues);

            //add serial no for uniqueness
            mUniqueSerialNo.put(assets.getSNO(), assets.getSNO());

            if (assets.getImageName() != null && assets.getImageName().length() > 0) {
                String assetImageColumns = "Uid,ImageName,ImgName,RetailerID";

                String values = id + ","
                        + StringUtils.getStringQueryParam(assets.getImageName()) + ","
                        + StringUtils.getStringQueryParam(assets.getImgName()) + ","
                        + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());

                db.insertSQL(DataMembers.tbl_SerializedAssetTransferImages, assetImageColumns,
                        values);
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Download unique POSM available
     */
    Observable<ArrayList<AssetAddDetailBO>> fetchAddAssets(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    db.openDataBase();
                    StringBuilder accountGroupId = new StringBuilder();

                    Cursor c = db.selectSQL("select groupid from AccountGroupDetail where retailerid=" + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
                    if (c != null) {
                        if (c.moveToNext()) {
                            if (accountGroupId.length() > 0)
                                accountGroupId.append(",");
                            accountGroupId.append(c.getString(0));
                        }
                        c.close();
                    }
                    return accountGroupId.toString();
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return "";
            }
        }).flatMap(new Function<String, SingleSource<?>>() {
            @Override
            public SingleSource<?> apply(String groupIds) throws Exception {
                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            StringBuilder mappingAssetIds = new StringBuilder();
                            Cursor c = db.selectSQL("select AssetId from SerializedAssetMasterMapping where AccoutGroupId in(" + groupIds + ")");
                            if (c != null) {
                                if (c.moveToNext()) {
                                    if (mappingAssetIds.length() > 0)
                                        mappingAssetIds.append(",");
                                    mappingAssetIds.append(c.getString(0));
                                }
                                c.close();
                            }
                            return mappingAssetIds.toString();
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                        return "";
                    }
                });
            }
        }).flatMapObservable(new Function<Object, ObservableSource<? extends ArrayList<AssetAddDetailBO>>>() {
            @Override
            public ObservableSource<? extends ArrayList<AssetAddDetailBO>> apply(Object assetIDs) throws Exception {
                return Observable.fromCallable(new Callable<ArrayList<AssetAddDetailBO>>() {
                    @Override
                    public ArrayList<AssetAddDetailBO> call() throws Exception {
                        ArrayList<AssetAddDetailBO> posmList = new ArrayList<>();
                        try {
                            String assetIdCond = "";
                            if (!assetIDs.toString().isEmpty())
                                assetIdCond = " Where AssetId in(" + assetIDs + ")";

                            String sb = "select distinct AssetId,AssetName,ifnull(vendorid,''),ifnull(modelid,''),capacity,ifnull(assettype,'') from SerializedAssetMaster" + assetIdCond;


                            Cursor c = db.selectSQL(sb);
                            if (c.getCount() > 0) {
                                Vector<AssetAddDetailBO> mAssetSpinner = new Vector<>();
                                while (c.moveToNext()) {
                                    AssetAddDetailBO assetBO = new AssetAddDetailBO();
                                    assetBO.setPOSMId(c.getString(0));
                                    assetBO.setPOSMDescription(c.getString(1));
                                    assetBO.setVendorId(c.getString(2));
                                    assetBO.setModelId(c.getString(3));
                                    assetBO.setCapacity(c.getString(4));
                                    assetBO.setTypeId(c.getString(5));
                                    mAssetSpinner.add(assetBO);
                                    posmList.add(assetBO);

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
                            return posmList;

                        } catch (Exception e) {
                            Commons.printException("" + e);
                            db.closeDB();
                        }
                        return new ArrayList<>();
                    }
                });
            }
        });
    }


    public ArrayList<AssetAddDetailBO> downloadUniqueAssets(Context mContext) {

        ArrayList<AssetAddDetailBO> posmList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {

            db.openDataBase();
            String sb = "select distinct  AssetId,AssetName,ifnull(vendorid,''),ifnull(modelid,''),capacity,ifnull(assettype,'') from SerializedAssetMaster";


            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                Vector<AssetAddDetailBO> mAssetSpinner = new Vector<>();
                while (c.moveToNext()) {
                    AssetAddDetailBO assetBO = new AssetAddDetailBO();
                    assetBO.setPOSMId(c.getString(0));
                    assetBO.setPOSMDescription(c.getString(1));
                    assetBO.setVendorId(c.getString(2));
                    assetBO.setModelId(c.getString(3));
                    assetBO.setCapacity(c.getString(4));
                    assetBO.setTypeId(c.getString(5));
                    mAssetSpinner.add(assetBO);
                    posmList.add(assetBO);

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

        return posmList;
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

        SerializedAssetBO assetBO;

        mRemovableAssets = new Vector<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            mBusinessModel.productHelper.getRetailerlevel(moduleName);


            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.AssetId,P.AssetName,SAM.SerialNumber,AllocationRefId,");
            sb.append("ifnull(P.assettype,'') as AssetTypeId,P.capacity as capacity,");
            sb.append("P.vendorid as vendorid,P.modelid as modelid,ifnull(SAVM.name,'') as name,");
            sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.modelid and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName,");
            sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.AssetType and SLM.ListType='ASSET_TYPE'),'') as AssetType");
            sb.append(" from SerializedAssetMaster P  inner join SerializedAssetProductMapping SBD on P.AssetId=SBD.AssetId");
            sb.append(" inner join SerializedAssetMapping SAM ON SAM.assetId=P.AssetId");
            sb.append(" left join SerializedAssetVendorMaster SAVM on SAVM.id=P.vendorid");
            sb.append(" where (SAM.SerialNumber  in (select distinct SerialNumber from SerializedAssetTransfer AAD where Transfer_Type!='RTR_WH'");
            sb.append(") or SAM.SerialNumber not in (select distinct SerialNumber from SerializedAssetTransfer AAD1");
            sb.append(")) and retailerid in (0," + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ")");

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
                    assetBO.setReferenceId(c.getString(3));
                    assetBO.setCapacity(c.getInt(c.getColumnIndex("capacity")));
                    assetBO.setVendorId(c.getString(c.getColumnIndex("vendorid")));
                    assetBO.setVendorName(c.getString(c.getColumnIndex("name")));
                    assetBO.setModelId(c.getString(c.getColumnIndex("modelid")));
                    assetBO.setModelName(c.getString(c.getColumnIndex("ModelName")));
                    assetBO.setAssetTypeId(c.getString(c.getColumnIndex("AssetTypeId")));
                    assetBO.setAssetType(c.getString(c.getColumnIndex("AssetType")));
                    mRemovableAssets.add(assetBO);
                }
            }

            String sb1 = "select distinct  P.AssetId,P.AssetName,serialNumber,installdate," +
                    "ifnull(P.assettype,'') as AssetTypeId,P.capacity as capacity,P.vendorid as vendorid,P.modelid as modelid,ifnull(SAVM.name,'') as name," +
                    "ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.modelid and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName," +
                    "ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.AssetType and SLM.ListType='ASSET_TYPE'),'') as AssetType" +
                    " from SerializedAssetMaster P inner  join SerializedAssetTransfer AAD on P.AssetId=AAD.AssetId" +
                    " left join SerializedAssetVendorMaster SAVM on SAVM.id=P.vendorid" +
                    " where Transfer_Type!='RTR_WH'  and retailerid=" +
                    StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());

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
                    assetBO.setCapacity(c.getInt(c.getColumnIndex("capacity")));
                    assetBO.setVendorId(c.getString(c.getColumnIndex("vendorid")));
                    assetBO.setVendorName(c.getString(c.getColumnIndex("name")));
                    assetBO.setModelId(c.getString(c.getColumnIndex("modelid")));
                    assetBO.setModelName(c.getString(c.getColumnIndex("ModelName")));
                    assetBO.setAssetTypeId(c.getString(c.getColumnIndex("AssetTypeId")));
                    assetBO.setAssetType(c.getString(c.getColumnIndex("AssetType")));
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
     * @param reasonId   Reason ID
     * @param moduleName Module Name
     */
    public void deleteAsset(Context mContext, String posmId, String mSno,
                            String mSbdId, String reasonId, String moduleName, String NFCId, String refId) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);


            String columns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId";

            String values = id + ","
                    + StringUtils.getStringQueryParam(posmId) + "," + StringUtils.getStringQueryParam(mSno) + "," + StringUtils.getStringQueryParam(NFCId == null ? "" : NFCId) + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + StringUtils.getStringQueryParam("R") + ","
                    + StringUtils.getStringQueryParam(reasonId) + "," + StringUtils.getStringQueryParam("") + "," + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + "," + 0 + "," + StringUtils.getStringQueryParam("RTR_WH") + "," + StringUtils.getStringQueryParam(refId);

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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_SerializedAssetTransfer, "serialNumber ="
                    + StringUtils.getStringQueryParam(mSno), false);

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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        ArrayList<String> retailerMovedData = new ArrayList<>();
        Cursor c = db.selectSQL("SELECT DISTINCT AssetId,serialNumber from " + DataMembers.tbl_SerializedAssetTransfer + " where RequestType='T'");
        if (c != null)
            while (c.moveToNext()) {
                retailerMovedData.add(c.getString(0) + c.getString(1));
            }
        db.closeDB();
        return retailerMovedData;
    }

    /**
     * Method to save Asset Movement Details in sql table
     */
    public void saveAssetMovementDetails(Context mContext, String movementType, String referenceId) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            SerializedAssetBO assets = getAssetTrackingBO();
            String columns = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId,rentPrice,toDate";


            String values = id + "," + StringUtils.getStringQueryParam(assets.getPOSM()) + "," + StringUtils.getStringQueryParam(assets.getSNO()) + ","
                    + StringUtils.getStringQueryParam(assets.getNFCTagId() == null ? "" : assets.getNFCTagId())
                    + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + StringUtils.getStringQueryParam("T") + "," +
                    StringUtils.getStringQueryParam(assets.getReasonId()) + ","
                    + StringUtils.getStringQueryParam(assets.getRemarks()) + ","
                    + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + ","
                    + StringUtils.getStringQueryParam(assets.getToRetailerId()) + ","
                    + StringUtils.getStringQueryParam(movementType) + ","
                    + StringUtils.getStringQueryParam(referenceId) + ","
                    + assets.getRentalPrice() + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.convertToServerDateFormat(assets.getEffectiveToDate(), ConfigurationMasterHelper.outDateFormat));


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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            mBusinessModel.productHelper.getRetailerlevel(moduleName);

            StringBuilder sb = new StringBuilder();
            sb.append("select distinct P.AssetId,P.AssetName,SAM.SerialNumber,SBD.Productid,");
            sb.append("ifnull(P.assettype,'') as AssetTypeId,P.capacity as capacity,P.vendorid as vendorid,");
            sb.append("P.modelid as modelid,ifnull(SAVM.name,'') as name,ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.modelid and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName,");
            sb.append(" ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.AssetType and SLM.ListType='ASSET_TYPE'),'') as AssetType");
            sb.append(" from SerializedAssetMaster P  inner join SerializedAssetProductMapping SBD on P.AssetId=SBD.AssetId");
            sb.append(" inner join SerializedAssetMapping SAM ON SAM.assetId=P.AssetId");
            sb.append(" left join SerializedAssetVendorMaster SAVM on SAVM.id=P.vendorid");
            sb.append(" where (SAM.SerialNumber  in (select distinct SerialNumber from SerializedAssetTransfer AAD where Transfer_Type!='RTR_WH'");
            sb.append(") or SAM.SerialNumber not in (select distinct SerialNumber from SerializedAssetTransfer AAD1");
            sb.append("))");
            sb.append(" and Retailerid in(0,");
            sb.append(StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ")");


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
                    assetBO.setCapacity(c.getInt(c.getColumnIndex("capacity")));
                    assetBO.setVendorId(c.getString(c.getColumnIndex("vendorid")));
                    assetBO.setVendorName(c.getString(c.getColumnIndex("name")));
                    assetBO.setModelId(c.getString(c.getColumnIndex("modelid")));
                    assetBO.setModelName(c.getString(c.getColumnIndex("ModelName")));
                    assetBO.setAssetTypeId(c.getString(c.getColumnIndex("AssetTypeId")));
                    assetBO.setAssetType(c.getString(c.getColumnIndex("AssetType")));
                    assetServiceList.add(assetBO);
                }
            }
            String sb1 = "select distinct  P.AssetId,P.AssetName,serialNumber,installdate," +
                    "ifnull(P.assettype,'') as AssetTypeId,P.capacity as capacity,P.vendorid as vendorid,P.modelid as modelid,ifnull(SAVM.name,'') as name," +
                    "ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.modelid and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName," +
                    "ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=P.AssetType and SLM.ListType='ASSET_TYPE'),'') as AssetType" +
                    " from SerializedAssetMaster P  inner  join SerializedAssetTransfer AAD on P.AssetId=AAD.AssetId" +
                    " left join SerializedAssetVendorMaster SAVM on SAVM.id=P.vendorid" +
                    " where Transfer_Type!='RTR_WH'  and retailerid=" +
                    StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());

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
                    StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());


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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();
            db.deleteSQL("SerializedAssetServiceRequest", "retailerid=" + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()), false);
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
            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String addAssetColumns = "Uid,date,AssetId,serialNumber,reasonid,retailerid";

            String assetAddAndDeleteValues = id + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + StringUtils.getStringQueryParam(assetId) + "," + StringUtils.getStringQueryParam(serialNo) + ","
                    + StringUtils.getStringQueryParam(mReasonID) + ","
                    + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());

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

    public ArrayList<SerializedAssetBO> getAssetVendors(Context context) {
        ArrayList<SerializedAssetBO> vendorList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            Cursor cursor = db.selectSQL("select distinct id,name from SerializedAssetVendorMaster");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    SerializedAssetBO serializedAssetBO = new SerializedAssetBO(1);
                    serializedAssetBO.setVendorId(cursor.getString(0));
                    serializedAssetBO.setVendorName(cursor.getString(1));
                    vendorList.add(serializedAssetBO);
                }
            }
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();

        return vendorList;
    }

    public ArrayList<SerializedAssetBO> getAssetModels(Context context) {
        ArrayList<SerializedAssetBO> modelList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            Cursor cursor = db.selectSQL("select listid,listname from standardlistmaster where listtype='ASSET_MODEL_TYPE'");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    SerializedAssetBO serializedAssetBO = new SerializedAssetBO(2);
                    serializedAssetBO.setModelId(cursor.getString(0));
                    serializedAssetBO.setModelName(cursor.getString(1));
                    modelList.add(serializedAssetBO);
                }
            }
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();

        return modelList;
    }

    public ArrayList<SerializedAssetBO> getAssetTypes(Context context) {
        ArrayList<SerializedAssetBO> typeList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            Cursor cursor = db.selectSQL("select listid,listname from standardlistmaster where listtype='ASSET_TYPE'");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    SerializedAssetBO serializedAssetBO = new SerializedAssetBO(3);
                    serializedAssetBO.setAssetTypeId(cursor.getString(0));
                    serializedAssetBO.setAssetType(cursor.getString(1));
                    typeList.add(serializedAssetBO);
                }
            }
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();

        return typeList;
    }

    public ArrayList<String> getAssetCapacity(Context context) {
        ArrayList<String> capacityList = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            Cursor cursor = db.selectSQL("select distinct capacity from SerializedAssetMaster");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String capacity = cursor.getString(0);
                    if (!"0".equals(capacity))
                        capacityList.add(capacity);
                }
            }
        } catch (Exception e) {
            db.closeDB();
            e.printStackTrace();
        }
        db.closeDB();

        return capacityList;
    }


    public Single<Boolean> saveNewRequestAsset(Context mContext) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                try {

                    db.openDataBase();

                    String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                            + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
                    SerializedAssetBO assets = getAssetTrackingBO();
                    String addAssetColumns = "uid,AssetId,serialNumber,NFCNumber,DeliveryDate,creationdate,RequestType,retailerId,Transfer_To,Transfer_Type,AllocationRefId,QTY,VisitId";

                    String assetAddAndDeleteValues = id + ","
                            + StringUtils.getStringQueryParam(assets.getPOSM()) + ","
                            + StringUtils.getStringQueryParam(assets.getSNO()) + ","
                            + StringUtils.getStringQueryParam(assets.getNFCTagId()) + ","
                            + StringUtils.getStringQueryParam(DateTimeUtils.convertToServerDateFormat(assets.getDeliveryDate(), ConfigurationMasterHelper.outDateFormat)) + ","
                            + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                            + "'N'" + ","
                            + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + ","
                            + "0" + ","
                            + "'WH_RTR_REQ'" + ","
                            + StringUtils.getStringQueryParam(assets.getReferenceId()) + ","
                            + 1 + ","
                            + StringUtils.getStringQueryParam(outletLastVisitID);

                    db.insertSQL(DataMembers.tbl_SerializedAssetTransfer, addAssetColumns,
                            assetAddAndDeleteValues);

                    db.closeDB();

                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    db.closeDB();
                }
                return false;
            }
        });
    }


    public Single<String> fetchSerialNo(Context mContext) {
        StringBuilder assetIds = new StringBuilder();
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                StringBuilder serialNo = new StringBuilder();
                for (SerializedAssetBO sNoBo : mAssetTrackingList) {
                    if (assetIds.length() > 0)
                        assetIds.append(",");

                    if (serialNo.length() > 0)
                        serialNo.append(",");

                    assetIds.append(sNoBo.getAssetID());
                    serialNo.append(sNoBo.getSerialNo());
                }
                return serialNo.toString();
            }
        }).flatMap(new Function<String, SingleSource<? extends String>>() {
            @Override
            public SingleSource<? extends String> apply(String serialNo) throws Exception {
                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                        try {
                            db.openDataBase();

                            String query = "Select NewSerialNumber,AssetId,rentPrice,toDate From SerializedAssetUpdate " +
                                    " Where AssetId in (" + assetIds + ")" +
                                    " And serialNumber in(" + StringUtils.getStringQueryParam(serialNo) + ")";

                            Cursor c = db.selectSQL(query);
                            if (c.getCount() > 0) {
                                while (c.moveToNext()) {
                                    for (SerializedAssetBO sNoBo : mAssetTrackingList) {
                                        if (sNoBo.getAssetID() == c.getInt(1)) {
                                            sNoBo.setNewSerialNo(c.getString(0));
                                            sNoBo.setRentalPrice(c.getDouble(2));
                                            sNoBo.setEffectiveToDate(DateTimeUtils.convertFromServerDateToRequestedFormat(c.getString(3), ConfigurationMasterHelper.outDateFormat));
                                        }

                                    }
                                }
                                c.close();
                            }
                            db.closeDB();
                        } catch (Exception e) {
                            Commons.printException("" + e);
                            db.closeDB();
                        }
                        return "";
                    }
                });
            }
        });

    }


    public Single<Boolean> updateSerialNo(final Context mContext, ArrayList<SerializedAssetBO> serialNoAssetList) {

        return Single.fromCallable(() -> {
            if (mUniqueSerialNo == null)
                mUniqueSerialNo = new HashMap<>();

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            try {

                db.openDataBase();

                for (SerializedAssetBO assetBO : serialNoAssetList) {

                    String query = "select uid from SerializedAssetUpdate where AssetId =" + assetBO.getAssetID() +
                            " and retailerId ="
                            + StringUtils.getStringQueryParam(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
                    query += " and upload='N'";

                    Cursor c = db.selectSQL(query);
                    if (c.getCount() > 0) {
                        c.moveToNext();
                        db.deleteSQL("SerializedAssetUpdate", String.valueOf(assetBO.getAssetID()), false);
                    }


                    String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                            + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                    String addAssetColumns = "uid,AssetId,serialNumber,AllocationRefId,newSerialNumber,Date,retailerId,rentPrice,toDate,VisitId";

                    String assetAddAndDeleteValues = StringUtils.getStringQueryParam(id) + ","
                            + assetBO.getAssetID() + ","
                            + DatabaseUtils.sqlEscapeString(assetBO.getSerialNo()) + ","
                            + StringUtils.getStringQueryParam(assetBO.getReferenceId()) + ","
                            + DatabaseUtils.sqlEscapeString(assetBO.getNewSerialNo()) + ","
                            + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                            + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + ","
                            + assetBO.getRentalPrice() + ","
                            + StringUtils.getStringQueryParam(DateTimeUtils
                            .convertToServerDateFormat(assetBO.getEffectiveToDate(),
                                    ConfigurationMasterHelper.outDateFormat)) + ","
                            + StringUtils.getStringQueryParam(outletLastVisitID);

                    db.insertSQL("SerializedAssetUpdate", addAssetColumns,
                            assetAddAndDeleteValues);

                    //add serial no for uniqueness
                    mUniqueSerialNo.put(assetBO.getNewSerialNo(), assetBO.getNewSerialNo());
                }

                db.closeDB();
                return true;

            } catch (Exception e) {
                Commons.printException("" + e);
                db.closeDB();
            }
            return false;
        });
    }

    public Observable<ArrayList<SerializedAssetBO>> fetchAssetApprovalData(final Context mContext) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<SerializedAssetBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<SerializedAssetBO>> subscribe) throws Exception {
                ArrayList<SerializedAssetBO> approvalAssetList = new ArrayList<>();
                SerializedAssetBO approvalAssetBo;
                StringBuilder sb = new StringBuilder();
                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                try {

                    db.openDataBase();

                    sb.append("SELECT SAA.AssetID,SAM.AssetName,SAA.serialNumber,SAA.RequestedDate,SAA.ApprovalStatus,SAA.Type");
                    sb.append(" FROM SerializedAssetMaster SAM INNER JOIN SerializedAssetApproval SAA");
                    sb.append(" ON SAM.AssetId = SAA.AssetId Where SAA.RetailerId=").append(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
                    sb.append(" ORDER BY SAA.Type ASC");

                    Cursor c = db.selectSQL(sb.toString());

                    if (c != null
                            && c.getCount() > 0) {
                        while (c.moveToNext()) {
                            approvalAssetBo = new SerializedAssetBO();
                            approvalAssetBo.setAssetID(c.getInt(0));
                            approvalAssetBo.setAssetName(c.getString(1));
                            approvalAssetBo.setSerialNo(c.getString(2));
                            approvalAssetBo.setRequestedDate(c.getString(3));
                            approvalAssetBo.setApprovalStatus(c.getString(4));
                            approvalAssetBo.setTransferType(c.getString(5));
                            approvalAssetList.add(approvalAssetBo);
                        }
                        c.close();
                    }
                    db.closeDB();
                    subscribe.onNext(approvalAssetList);
                    subscribe.onComplete();
                } catch (Exception e) {
                    db.closeDB();
                    subscribe.onError(e);
                    subscribe.onComplete();
                }
            }
        });
    }


    public Single<Boolean> updateApproval(Context mContext, ArrayList<SerializedAssetBO> approvalList) {
        return Single.fromCallable(() -> {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            try {

                db.openDataBase();

                for (SerializedAssetBO assetBO : approvalList) {

                    if (assetBO.getApprovalStatus().equalsIgnoreCase("Approved")) {
                        db.updateSQL(" Update SerializedAssetApproval Set ApprovalStatus="
                                + StringUtils.getStringQueryParam(assetBO.getApprovalStatus()) + ","
                                + "ApprovalDate=" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                + "Upload='N' "
                                + "Where AssetId=" + assetBO.getAssetID() +
                                " And RetailerId=" + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID());
                    }
                }
                db.closeDB();
                return true;

            } catch (Exception e) {
                Commons.printException("" + e);
                db.closeDB();
            }
            return false;
        });
    }


    Observable<ArrayList<String>> fetchAssetImages(Context mContext, int assetId) {


        return Observable.create(new ObservableOnSubscribe<ArrayList<String>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<String>> subscribe) throws Exception {
                ArrayList<String> assetImageList = new ArrayList<>();
                SerializedAssetBO approvalAssetBo;
                StringBuilder sb = new StringBuilder();
                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                try {
                    db.openDataBase();

                    String imgQuery = "Select ImageName From AssetMasterImageDetail Where AssetID =" + assetId;
                    Cursor c = db.selectSQL(imgQuery);

                    if (c != null
                            && c.getCount() > 0) {
                        while (c.moveToNext()) {
                            assetImageList.add(c.getString(0));
                        }
                        c.close();
                    }
                    db.closeDB();
                    subscribe.onNext(assetImageList);
                    subscribe.onComplete();

                } catch (Exception e) {
                    db.closeDB();
                    subscribe.onError(e);
                    subscribe.onComplete();
                }
            }
        });
    }

    private void getLastRetailerVisitId(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT VisitID FROM OutletTimestamp " +
                    " Where RetailerID=" + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + " order by rowid");
            if (c != null) {
                if (c.moveToLast()) {
                    outletLastVisitID = c.getString(0);
                }
                c.close();
            }
            db.close();
        } catch (Exception e) {
            db.close();
            Commons.printException(e);
        }
    }

    Single<Boolean> deleteAssetSurvey(Context mContext, ArrayList<String> serialNoArrayList) {
        StringBuilder deletedSRNo = new StringBuilder();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    db.openDataBase();
                    for (String serialNo : serialNoArrayList) {
                        String query = "Select serialNumber From SerializedAssetDetail Where serialNumber ="
                                + StringUtils.getStringQueryParam(serialNo) + " And isAvailable=1";
                        Cursor c = db.selectSQL(query);

                        if (c != null) {
                            if (c.getCount() == 0) {

                                if (deletedSRNo.length() > 0)
                                    deletedSRNo.append(",");

                                deletedSRNo.append(StringUtils.getStringQueryParam(serialNo));
                            }
                            c.close();
                        }
                    }
                    return deletedSRNo.length() > 0;

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return false;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean isDeleted) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            if (isDeleted) {
                                String sql = "SELECT uid FROM AnswerHeader WHERE EntityReferenceNo in(" + deletedSRNo + ")";
                                Cursor c = db.selectSQL(sql);
                                if (c != null
                                        && c.getCount() > 0) {
                                    while (c.moveToNext()) {
                                        callSurveyDeleteQuery(db, c.getString(0));
                                    }
                                    c.close();
                                }
                                db.closeDB();
                                return true;
                            }
                        } catch (Exception e) {
                            db.closeDB();
                            Commons.printException(e);
                        }
                        return false;
                    }
                });
            }
        });
    }

    Single<Boolean> deleteAssetSurvey(Context mContext, String serialNo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                boolean isFlag = false;
                try {
                    db.openDataBase();
                    if (serialNo.length() > 0) {
                        String sql = "SELECT uid FROM AnswerHeader WHERE EntityReferenceNo=" + StringUtils.getStringQueryParam(serialNo);
                        Cursor c = db.selectSQL(sql);
                        if (c != null
                                && c.getCount() > 0) {
                            while (c.moveToNext()) {
                                callSurveyDeleteQuery(db, c.getString(0));
                                isFlag = true;
                            }
                            c.close();
                        }
                    }
                    db.closeDB();
                    return isFlag;
                } catch (Exception e) {
                    db.closeDB();
                    Commons.printException(e);
                }
                return isFlag;
            }
        });
    }

    private void callSurveyDeleteQuery(DBUtil db, String uId) {

        db.deleteSQL("AnswerHeader",
                "uid = " + StringUtils.getStringQueryParam(uId), false);
        db.deleteSQL("AnswerDetail",
                "uid = " + StringUtils.getStringQueryParam(uId), false);
        db.deleteSQL("AnswerImageDetail",
                "uid = " + StringUtils.getStringQueryParam(uId), false);
        db.deleteSQL("AnswerScoreDetail",
                "uid = " + StringUtils.getStringQueryParam(uId), false);
    }

}
