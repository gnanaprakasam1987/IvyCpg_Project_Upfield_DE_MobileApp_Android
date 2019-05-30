package com.ivy.ui.AssetServiceRequest.data;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class AssetServiceRequestHelper implements AssetServiceRequestDataManager {

    private BusinessModel businessModel;
    private static AssetServiceRequestHelper instance = null;
    private DBUtil mDbUtil;
    private AppDataProvider appDataProvider;

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

   /* private AssetServiceRequestHelper(Context context){
        businessModel=(BusinessModel)context.getApplicationContext();

    }

    public static AssetServiceRequestHelper getInstance(Context context) {
        if (instance == null)
            instance = new AssetServiceRequestHelper(context);

        return instance;
    }*/

    @Inject
    AssetServiceRequestHelper(@DataBaseInfo DBUtil mDbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = mDbUtil;
        this.appDataProvider = appDataProvider;

    }

    @Override
    public Observable<ArrayList<SerializedAssetBO>> fetchAssetServiceRequests( boolean isFromReport) {
        return Observable.fromCallable(() -> {

            try {
                initDb();
                setAssetRequestList(new ArrayList<>());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Select A.AssetId,AssetName,SerialNumber,Date,SM.listName as issueType ,SM1.listName as serviceProvider" +
                        ",IssueDescription,ImagePath,ApprovalStatus,ExpectedResolutionDate,A.uid,A.reasonId,RM.RetailerName,createdBy,Status");
                stringBuilder.append(" from SerializedAssetServiceRequest A left join SerializedAssetMaster B ON A.assetId=B.AssetId");
                stringBuilder.append(" left join StandardListMaster SM ON SM.listId=A.reasonId");
                stringBuilder.append(" left join StandardListMaster SM1 ON SM1.listId=A.ServiceProviderId");
                stringBuilder.append(" left join RetailerMaster RM ON RM.retailerId=A.retailerId");

                if(!isFromReport)
                stringBuilder.append(" where A.retailerId="+appDataProvider.getRetailMaster().getRetailerID());

                Cursor cursor = mDbUtil.selectSQL(stringBuilder.toString());
                if (cursor.getCount() > 0) {
                    SerializedAssetBO assetBO;
                    while (cursor.moveToNext()) {
                        assetBO = new SerializedAssetBO();
                        assetBO.setRField(cursor.getString(10));
                        assetBO.setAssetID(cursor.getInt(0));
                        assetBO.setAssetName(cursor.getString(1));
                        assetBO.setSerialNo(cursor.getString(2));
                        assetBO.setServiceDate(cursor.getString(3));// service requested date
                        assetBO.setReasonDesc(cursor.getString(4));// Issue Type
                        assetBO.setReasonID(cursor.getInt(11));// Issue type id
                        assetBO.setServiceProvider(cursor.getString(5));
                        assetBO.setIssueDescription(cursor.getString(6));
                        //assetBO.setImageName();
                        assetBO.setAssetServiceReqStatus(cursor.getString(8));
                        assetBO.setNewInstallDate(cursor.getString(9)); // Resolution date
                        assetBO.setServiceRequestedRetailer(cursor.getString(12));
                        assetBO.setRemarks(cursor.getString(13));// reused to hold created by value.
                        assetBO.setRemarks(assetBO.getRemarks()!=null?assetBO.getRemarks():"-");
                        assetBO.setStatus(cursor.getString(14));

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
    public Observable<ArrayList<SerializedAssetBO>> fetchAssets(boolean isFromReport) {
        return Observable.fromCallable(() -> {

            ArrayList<SerializedAssetBO> assetList = new ArrayList<>();
            try {

                initDb();

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

                if(isFromReport) {

                    sb.append("Where Retailerid =");
                    sb.append(appDataProvider.getRetailMaster().getRetailerID());
                }

                sb.append("GROUP BY RetailerId,B.AssetId,B.SerialNumber ORDER BY RetailerId");

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

                shutDownDb();

            } catch (Exception ex) {
                Commons.printException(ex);
            }
            return assetList;
        });
    }


    @Override
    public Observable<ArrayList<ReasonMaster>> fetchServiceProvider() {
        return Observable.fromCallable(() -> {

            ArrayList<ReasonMaster> serviceProviderList = new ArrayList<>();
            try {

                initDb();

                StringBuilder sb = new StringBuilder();
                sb.append("select serviceProviderId,serviceProviderName from SerializedAssetServiceProvider");

                Cursor c = mDbUtil.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    ReasonMaster reasonBO;
                    while (c.moveToNext()) {
                        reasonBO = new ReasonMaster();
                        reasonBO.setReasonID(c.getString(0));
                        reasonBO.setReasonDesc(c.getString(1));



                        serviceProviderList.add(reasonBO);

                    }

                }

                shutDownDb();

            } catch (Exception ex) {
                Commons.printException(ex);
            }
            return serviceProviderList;
        });
    }


    @Override
    public Single<Boolean> saveNewServiceRequest(SerializedAssetBO assetBO) {
        return Single.fromCallable(() -> {

            setAssetRequestList(new ArrayList<>());
            try {
                initDb();

                String columns="uid,AssetId,RetailerId,Date,SerialNumber,ReasonId,serviceProviderId,IssueDescription,ImagePath,approvalStatus,ExpectedResolutionDate,Upload,Status";

                String id = appDataProvider.getUser().getUserid()
                        + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                StringBuilder stringBuilder =new StringBuilder();
                stringBuilder.append(id+",");
                stringBuilder.append(assetBO.getAssetID()+",");
                stringBuilder.append(appDataProvider.getRetailMaster().getRetailerID()+",");
                stringBuilder.append(StringUtils.QT(assetBO.getServiceDate())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getSerialNo())+",");
                stringBuilder.append(assetBO.getReasonID()+",");
                stringBuilder.append(assetBO.getServiceProviderId()+",");
                stringBuilder.append(StringUtils.QT(assetBO.getIssueDescription())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getImageName())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getAssetServiceReqStatus())+",");
                stringBuilder.append(StringUtils.QT(assetBO.getNewInstallDate())+",");
                stringBuilder.append("'N'"+",");
                stringBuilder.append("'I'");

                mDbUtil.insertSQL(DataMembers.tbl_SerializedAssetServiceRequest,columns,stringBuilder.toString());

                shutDownDb();

            }
            catch (Exception ex){
                Commons.printException(ex);
                return false;
            }

            return true;
        });
    }

    @Override
    public Single<Boolean> updateServiceRequest(SerializedAssetBO assetBO) {
        return Single.fromCallable(() -> {

            setAssetRequestList(new ArrayList<>());
            try {
                initDb();


                StringBuilder stringBuilder =new StringBuilder();

                stringBuilder.append("update "+DataMembers.tbl_SerializedAssetServiceRequest+" set ");
                stringBuilder.append("AssetId="+assetBO.getAssetID()+",");
                stringBuilder.append("SerialNumber="+StringUtils.QT(assetBO.getSerialNo())+",");
                stringBuilder.append("ReasonId="+assetBO.getReasonID()+",");
                stringBuilder.append("serviceProviderId="+assetBO.getServiceProviderId()+",");
                stringBuilder.append("IssueDescription="+StringUtils.QT(assetBO.getIssueDescription())+",");
                stringBuilder.append("ImagePath="+StringUtils.QT(assetBO.getImageName())+",");
                stringBuilder.append("ApprovalStatus="+StringUtils.QT(assetBO.getAssetServiceReqStatus())+",");
                stringBuilder.append("ExpectedResolutionDate="+StringUtils.QT(assetBO.getNewInstallDate())+",");
                stringBuilder.append("Upload='N'");

                if(!assetBO.getStatus().equals("I"))// If not a new one then updating as U to denote it.
                 stringBuilder.append(",status='U'");

                stringBuilder.append(" where uid="+StringUtils.QT(assetBO.getRField()));



                mDbUtil.updateSQL(stringBuilder.toString());

                shutDownDb();

            }
            catch (Exception ex){
                Commons.printException(ex);
                return false;
            }

            return true;
        });
    }

    @Override
    public Single<Boolean> cancelServiceRequest(String requestId) {
        return Single.fromCallable(() -> {

            setAssetRequestList(new ArrayList<>());

            try {

                initDb();

                String query="update "+DataMembers.tbl_SerializedAssetServiceRequest+" set ApprovalStatus='CANCELLED' where uid="+StringUtils.QT(requestId);

                mDbUtil.updateSQL(query);

                shutDownDb();

            }
            catch (Exception ex){
                Commons.printException(ex);
                return false;
            }

            return true;
        });
    }

    @Override
    public Observable<ArrayList<String>> loadConfigs() {
        return Observable.fromCallable(() -> {

            ArrayList<String> configs=new ArrayList<>();

            try {


               initDb();

                String sql = "SELECT hhtCode, RField FROM "
                        + DataMembers.tbl_HhtModuleMaster
                        + " WHERE menu_type = 'ASSET_SERVICE' AND flag='1' and ForSwitchSeller = 0";

                Cursor c = mDbUtil.selectSQL(sql);

                if (c != null && c.getCount() != 0) {
                    while (c.moveToNext()) {
                        if (c.getString(0).equalsIgnoreCase(CODE_SHOW_SERVICE_PROVIDER))
                            configs.add("SHOW_SERVICE_PROVIDER");

                    }
                    c.close();
                }
               shutDownDb();


            } catch (Exception e) {
                Commons.printException(e);
            }

            return configs;
        });
    }

    private String CODE_SHOW_SERVICE_PROVIDER="ASR01";


    @Override
    public void tearDown() {
        shutDownDb();
    }
}
