package com.ivy.ui.AssetServiceRequest.data;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public class AssetServiceRequestHelper implements AssetServiceRequestDataManager {

    private BusinessModel businessModel;
    private static AssetServiceRequestHelper instance = null;
    private DBUtil mDbUtil;

    public ArrayList<SerializedAssetBO> getAssetRequestList() {
        return assetRequestList;
    }

    public void setAssetRequestList(ArrayList<SerializedAssetBO> assetRequestList) {
        this.assetRequestList = assetRequestList;
    }

    private ArrayList<SerializedAssetBO> assetRequestList;

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    private AssetServiceRequestHelper(Context context){
        businessModel=(BusinessModel)context.getApplicationContext();

    }

    public static AssetServiceRequestHelper getInstance(Context context) {
        if (instance == null)
            instance = new AssetServiceRequestHelper(context);

        return instance;
    }

    @Override
    public Observable<ArrayList<SerializedAssetBO>> fetchAssetServiceRequests(String retailerId, boolean isFromReport) {
        return Observable.fromCallable(() -> {

            try {
                initDb();
                setAssetRequestList(new ArrayList<>());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Select AssetId,AssetName,SerialNumber,Date,SM.listName as issueType ,SM1.listName as serviceProvider" +
                        ",IssueDescription,ImagePath,Status,ExpectedResolutionDate");
                stringBuilder.append("from SerializedAssetServiceRequest A left join AssetMaster B ON A.assetId=B.AssetId");
                stringBuilder.append("left join StandardListMaster SM ON SM.listId=A.reasonId");
                stringBuilder.append("left join StandardListMaster SM1 ON SM1.listId=A.ServiceProviderId");
                Cursor cursor = mDbUtil.selectSQL(stringBuilder.toString());
                if (cursor.getCount() > 0) {
                    SerializedAssetBO assetBO;
                    while (cursor.moveToNext()) {
                        assetBO = new SerializedAssetBO();
                        assetBO.setAssetID(cursor.getInt(0));
                        assetBO.setAssetName(cursor.getString(1));
                        assetBO.setSerialNo(cursor.getString(2));
                        assetBO.setServiceDate(cursor.getString(3));// service requested date
                        assetBO.setReasonDesc(cursor.getString(4));// Issue Type
                        assetBO.setServiceProvider(cursor.getString(5));
                        assetBO.setIssueDescription(cursor.getString(6));
                        //assetBO.setImageName();
                        assetBO.setAssetServiceReqStatus(cursor.getString(8));
                        assetBO.setNewInstallDate(cursor.getString(9)); // Resolution date

                        getAssetRequestList().add(assetBO);

                    }
                }
                shutDownDb();
            }
            catch (Exception ex){
                Commons.printException(ex);
                shutDownDb();
            }

            return getAssetRequestList();
        });
    }

    @Override
    public Observable<ArrayList<SerializedAssetBO>> fetchAssets(String retailerId) {
        return Observable.fromCallable(() -> {

            ArrayList<SerializedAssetBO> assetList = new ArrayList<>();
            try {

                StringBuilder sb = new StringBuilder();
                sb.append("select Distinct A.assetId,A.assetName,B.serialNumber,'',B.NFCNumber,'' as ParentHierarchy,AllocationRefId,");
                sb.append("ifnull(A.assettype,'') as AssetTypeId,A.capacity as capacity,A.vendorid as vendorid,A.modelid as modelid,ifnull(SAVM.name,'') as name,");
                sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=A.modelid ");
                sb.append("and SLM.ListType='ASSET_MODEL_TYPE'),'') as ModelName,");
                sb.append("ifnull((select SLM.Listname from StandardListMaster SLM where SLM.ListId=A.AssetType ");
                sb.append("and SLM.ListType='ASSET_TYPE'),'') as AssetType,");
                sb.append("B.installDate as installDate,B.lastserviceDate as serviceDate ");
                sb.append("from SerializedAssetMaster A ");
                sb.append("inner join SerializedAssetMapping B on A.AssetId=B.AssetId ");
                sb.append("left join SerializedAssetProductMapping C on C.AssetId=A.AssetId ");
                sb.append("left join SerializedAssetVendorMaster SAVM on SAVM.id=A.vendorid ");

                String allMasterSb = sb.toString();

                sb.append("Where Retailerid in(0,");
                sb.append(StringUtils.QT(businessModel.getRetailerMasterBO().getRetailerID())).append(")");

                sb.append(" GROUP BY RetailerId,B.AssetId,B.SerialNumber ORDER BY RetailerId");

                Cursor c = mDbUtil.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    SerializedAssetBO assetTrackingBO;
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
                        assetTrackingBO.setReferenceId(c.getInt(c.getColumnIndex("AllocationRefId")));
                        assetTrackingBO.setCapacity(c.getInt(c.getColumnIndex("capacity")));
                        assetTrackingBO.setVendorId(c.getString(c.getColumnIndex("vendorid")));
                        assetTrackingBO.setVendorName(c.getString(c.getColumnIndex("name")));
                        assetTrackingBO.setModelId(c.getString(c.getColumnIndex("modelid")));
                        assetTrackingBO.setModelName(c.getString(c.getColumnIndex("ModelName")));
                        assetTrackingBO.setAssetTypeId(c.getString(c.getColumnIndex("AssetTypeId")));
                        assetTrackingBO.setAssetType(c.getString(c.getColumnIndex("AssetType")));
                        assetTrackingBO.setmLastInstallDate(c.getString(c.getColumnIndex("installDate")));
                        assetTrackingBO.setInstallDate(c.getString(c.getColumnIndex("installDate")));
                        assetTrackingBO.setServiceDate(c.getString(c.getColumnIndex("serviceDate")));


                        assetList.add(assetTrackingBO);

                    }

                }

            } catch (Exception ex) {
                Commons.printException(ex);
            }
            return assetList;
        });
    }

    /*public boolean downloadRequests(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        setAssetRequestList(new ArrayList<>());
        try {
            db.openDataBase();

            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("Select AssetId,AssetName,SerialNumber,Date,SM.listName as issueType ,SM1.listName as serviceProvider" +
                    ",IssueDescription,ImagePath,Status,ExpectedResolutionDate");
            stringBuilder.append("from SerializedAssetServiceRequest A left join AssetMaster B ON A.assetId=B.AssetId");
            stringBuilder.append("left join StandardListMaster SM ON SM.listId=A.reasonId");
            stringBuilder.append("left join StandardListMaster SM1 ON SM1.listId=A.ServiceProviderId");
            Cursor cursor=db.selectSQL(stringBuilder.toString());
            if(cursor.getCount()>0) {
                SerializedAssetBO assetBO;
                while (cursor.moveToNext()) {
                    assetBO=new SerializedAssetBO();
                    assetBO.setAssetID(cursor.getInt(0));
                    assetBO.setAssetName(cursor.getString(1));
                    assetBO.setSerialNo(cursor.getString(2));
                    assetBO.setServiceDate(cursor.getString(3));// service requested date
                    assetBO.setReasonDesc(cursor.getString(4));// Issue Type
                    assetBO.setServiceProvider(cursor.getString(5));
                    assetBO.setIssueDescription(cursor.getString(6));
                    //assetBO.setImageName();
                    assetBO.setAssetServiceReqStatus(cursor.getString(8));
                    assetBO.setNewInstallDate(cursor.getString(9)); // Resolution date

                    getAssetRequestList().add(assetBO);

                }
            }

        }
        catch (Exception ex){
            Commons.printException(ex);
            return false;
        }

        return true;
    }*/

    private void saveServiceRequest(Context context,SerializedAssetBO assetBO){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        setAssetRequestList(new ArrayList<>());
        try {
            db.openDataBase();
        String columns="uid,AssetId,RetailerId,Date,SerialNumber,ReasonId,serviceProviderId,IssueDescription,ImagePath,Status,ExpectedResolutionDate,Upload";

        String id = businessModel.getAppDataProvider().getUser().getUserid()
                + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        StringBuilder stringBuilder =new StringBuilder();
        stringBuilder.append(id+",");
        stringBuilder.append(assetBO.getAssetID()+",");
        stringBuilder.append(assetBO.getServiceDate()+",");
        stringBuilder.append(assetBO.getSerialNo()+",");
        stringBuilder.append(assetBO.getReasonID()+",");
        stringBuilder.append(assetBO.getServiceProviderId()+",");
        stringBuilder.append(StringUtils.QT(assetBO.getIssueDescription())+",");
        stringBuilder.append(StringUtils.QT(assetBO.getImageName())+",");
        stringBuilder.append(StringUtils.QT(assetBO.getAssetServiceReqStatus()+","));
        stringBuilder.append(StringUtils.QT(assetBO.getNewInstallDate())+",");
        stringBuilder.append("'N'");

        db.insertSQL(DataMembers.tbl_SerializedAssetServiceRequest,columns,stringBuilder.toString());

        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }



    @Override
    public Single<Boolean> saveNewServiceRequest(SerializedAssetBO assetBO) {
        return Single.fromCallable(() -> {

            setAssetRequestList(new ArrayList<>());
            try {
                initDb();

                String columns="uid,AssetId,RetailerId,Date,SerialNumber,ReasonId,serviceProviderId,IssueDescription,ImagePath,Status,ExpectedResolutionDate,Upload";

                String id = businessModel.getAppDataProvider().getUser().getUserid()
                        + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                StringBuilder stringBuilder =new StringBuilder();
                stringBuilder.append(id+",");
                stringBuilder.append(assetBO.getAssetID()+",");
                stringBuilder.append(assetBO.getServiceDate()+",");
                stringBuilder.append(assetBO.getSerialNo()+",");
                stringBuilder.append(assetBO.getReasonID()+",");
                stringBuilder.append(assetBO.getServiceProviderId()+",");
                stringBuilder.append(StringUtils.QT(assetBO.getIssueDescription())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getImageName())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getAssetServiceReqStatus()+","));
                stringBuilder.append(StringUtils.QT(assetBO.getNewInstallDate())+",");
                stringBuilder.append("'N'");

                mDbUtil.insertSQL(DataMembers.tbl_SerializedAssetServiceRequest,columns,stringBuilder.toString());

            }
            catch (Exception ex){
                Commons.printException(ex);
                return false;
            }

            return true;
        });
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
