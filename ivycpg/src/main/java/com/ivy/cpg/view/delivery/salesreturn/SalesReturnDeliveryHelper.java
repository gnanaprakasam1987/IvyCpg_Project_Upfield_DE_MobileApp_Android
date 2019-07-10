package com.ivy.cpg.view.delivery.salesreturn;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SalesReturnDeliveryHelper {
    private static SalesReturnDeliveryHelper instance = null;
    private HashMap<String, ArrayList<SalesReturnDeliveryDataModel>> salesReturnDelDataMap;

    private SalesReturnDeliveryHelper() {
    }

    public static SalesReturnDeliveryHelper getInstance() {
        if (instance == null) {
            instance = new SalesReturnDeliveryHelper();
        }
        return instance;
    }

    public Observable<Vector<SalesReturnDeliveryDataBo>> downloadSaleReturnDelivery(final Context context) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataBo>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataBo>> subscriber) {

                try {
                    loadConfigurations(context);
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    Vector<SalesReturnDeliveryDataBo> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME);

                    dbUtil.openDataBase();
                    Cursor cursor = dbUtil.selectSQL("Select " +
                            "uid,date,ReturnValue,Lpc,invoiceid,SignaturePath,ImgName,RefModule,RefModuleTId " +
                            "from SalesReturnHeader where retailerId ='" + businessModel.getRetailerMasterBO().getRetailerID() + "' " +
                            "AND upload='X' " +
                            "AND uid NOT IN (select ifnull(RefUID,0) from salesReturnHeader where upload='N' or isCancel == 1) ");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataBo salesReturnDeliveryDataModel = new SalesReturnDeliveryDataBo();
                            salesReturnDeliveryDataModel.setUId(cursor.getString(cursor.getColumnIndex("uid")));
                            salesReturnDeliveryDataModel.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            salesReturnDeliveryDataModel.setReturnValue(cursor.getString(cursor.getColumnIndex("ReturnValue")));
                            salesReturnDeliveryDataModel.setLpc(cursor.getInt(cursor.getColumnIndex("Lpc")));
                            salesReturnDeliveryDataModel.setInvoiceId(cursor.getString(cursor.getColumnIndex("invoiceid")));
                            salesReturnDeliveryDataModel.setSignaturePath(cursor.getString(cursor.getColumnIndex("SignaturePath")));
                            salesReturnDeliveryDataModel.setSignatureName(cursor.getString(cursor.getColumnIndex("ImgName")));
                            salesReturnDeliveryDataModel.setRefModule(cursor.getString(cursor.getColumnIndex("RefModule")));
                            salesReturnDeliveryDataModel.setRefModuleTId(cursor.getString(cursor.getColumnIndex("RefModuleTId")));


                            returnDeliveryDataModelVector.add(salesReturnDeliveryDataModel);
                        }

                        subscriber.onNext(returnDeliveryDataModelVector);
                        subscriber.onComplete();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    subscriber.onError(exception);
                    subscriber.onComplete();
                }
            }
        });
    }


    public Observable<Vector<SalesReturnDeliveryDataModel>> downloadSaleReturnDeliveryDetails(final Context context, final String uId) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataModel>> subscriber) {

                try {
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    Vector<SalesReturnDeliveryDataModel> returnDeliveryDataModelVector = new Vector<>();
                    ArrayList<SalesReturnDeliveryDataModel> skuLevelReturnData;
                    HashMap<String, ArrayList<SalesReturnDeliveryDataModel>> srdDataModelMap = new HashMap<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME);
                    dbUtil.openDataBase();

                    Cursor cursor = dbUtil
                            .selectSQL("SELECT DISTINCT SLM.ListName as reasonDesc,"
                                    + "PM.PName as ProductName,"
                                    + "IFNULL (SRD1.PQty,0) as ActPcsQty,"
                                    + "IFNULL (SRD1.CQty,0)  as ActCaseQty,"
                                    + "srd .* FROM SalesReturnDetails SRD"
                                    + " LEFT JOIN ProductMaster PM ON SRD.productID=PM.PID"
                                    + " INNER JOIN StandardListMaster SLM"
                                    + " ON SLM.listId = srd.condition"
                                    + " LEFT JOIN SAlesReturnDetails SRD1 ON SRD1.ProductId=SRD.ProductId " +
                                    "and SRD1.condition=SRD.condition and SRD1.upload='N' " +
                                    "and SRD1.RefUID = '" + uId + "'"
                                    + " WHERE"
                                    + " SRD.RetailerID='" + businessModel.getRetailerMasterBO().getRetailerID() + "'"
                                    + " AND SRD.uid='" + uId + "'"
                                    + " AND SRD.upload ='X'");


                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                            salesReturnDeliveryDataModel.setUId(cursor.getString(cursor.getColumnIndex("uid")));
                            salesReturnDeliveryDataModel.setProductName(cursor.getString(cursor.getColumnIndex("ProductName")));
                            salesReturnDeliveryDataModel.setProductId(cursor.getString(cursor.getColumnIndex("ProductID")));


                            salesReturnDeliveryDataModel.setReturnCaseQuantity(cursor.getInt(cursor.getColumnIndex("Cqty")));
                            salesReturnDeliveryDataModel.setReturnPieceQuantity(cursor.getInt(cursor.getColumnIndex("Pqty")));

                            salesReturnDeliveryDataModel.setReason(cursor.getString(cursor.getColumnIndex("reasonDesc")));
                            //salesReturnDeliveryDataModel.setReasonCategory(cursor.getString(7));
                            salesReturnDeliveryDataModel.setReasonID(cursor.getString(cursor.getColumnIndex("Condition")));
                            salesReturnDeliveryDataModel.setCaseSize(cursor.getInt(cursor.getColumnIndex("Cqty")));

                            salesReturnDeliveryDataModel.setOldMrp(cursor.getDouble(cursor.getColumnIndex("oldmrp")));
                            salesReturnDeliveryDataModel.setMfgDate(cursor.getString(cursor.getColumnIndex("mfgdate")));

                            salesReturnDeliveryDataModel.setExpDate(cursor.getString(cursor.getColumnIndex("expdate")));
                            salesReturnDeliveryDataModel.setOuterQty(cursor.getInt(cursor.getColumnIndex("outerQty")));

                            salesReturnDeliveryDataModel.setdOUomQty(cursor.getInt(cursor.getColumnIndex("dOuomQty")));
                            salesReturnDeliveryDataModel.setdOUomId(cursor.getInt(cursor.getColumnIndex("dOuomid")));

                            salesReturnDeliveryDataModel.setdUomId(cursor.getInt(cursor.getColumnIndex("duomid")));
                            salesReturnDeliveryDataModel.setInVoiceNumber(cursor.getString(cursor.getColumnIndex("invoiceno")));

                            salesReturnDeliveryDataModel.setSrpedit(cursor.getFloat(cursor.getColumnIndex("srpedited")));
                            salesReturnDeliveryDataModel.setTotalQuantity(cursor.getInt(cursor.getColumnIndex("totalQty")));

                            salesReturnDeliveryDataModel.setTotalAmount(cursor.getString(cursor.getColumnIndex("totalamount")));
                            salesReturnDeliveryDataModel.setRetailerId(cursor.getString(cursor.getColumnIndex("RetailerID")));

                            salesReturnDeliveryDataModel.setReasonType(cursor.getInt(cursor.getColumnIndex("reason_type")));

                            salesReturnDeliveryDataModel.setLotNumber(cursor.getInt(cursor.getColumnIndex("LotNumber")));
                            salesReturnDeliveryDataModel.setPieceUomId(cursor.getInt(cursor.getColumnIndex("piece_uomid")));

                            salesReturnDeliveryDataModel.setStatus(cursor.getInt(cursor.getColumnIndex("Status")));
                            salesReturnDeliveryDataModel.setHnsCode(cursor.getString(cursor.getColumnIndex("HsnCode")));

                            salesReturnDeliveryDataModel.setActualCaseQuantity(cursor.getInt(cursor.getColumnIndex("ActCaseQty")));
                            salesReturnDeliveryDataModel.setActualPieceQuantity(cursor.getInt(cursor.getColumnIndex("ActPcsQty")));


                            returnDeliveryDataModelVector.add(salesReturnDeliveryDataModel);
                        }

                        if (businessModel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL) {
                            for (SalesReturnDeliveryDataModel srdDataModel : returnDeliveryDataModelVector) {
                                skuLevelReturnData = new ArrayList<>();
                                int actCQty = 0, actPQty = 0;
                                Cursor cur = dbUtil.selectSQL("select pm.pid,pm.pname,ifnull(srd.pqty,0) as pqty,ifnull(srd.cqty,0) as cqty,ifnull(srd.condition,0) as reasonid,ifnull(srd.invoiceno,0) as invno,srd .* from productmaster pm left join SalesReturnDetails srd on pm.pid = srd.productid where pm.parentid = '" +
                                        srdDataModel.getProductId() + "' and reasonid in ('" + srdDataModel.getReasonID() + "',0) and invno in ('" + srdDataModel.getInVoiceNumber() + "',0)");
                                if (cur != null) {
                                    while (cur.moveToNext()) {
                                        SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                                        salesReturnDeliveryDataModel.setUId(srdDataModel.getUId());
                                        salesReturnDeliveryDataModel.setProductId(cur.getString(0));
                                        salesReturnDeliveryDataModel.setProductName(cur.getString(1));
                                        salesReturnDeliveryDataModel.setReason(srdDataModel.getReason());
                                        salesReturnDeliveryDataModel.setReturnCaseQuantity(srdDataModel.getReturnCaseQuantity());
                                        salesReturnDeliveryDataModel.setReturnPieceQuantity(srdDataModel.getReturnPieceQuantity());
                                        salesReturnDeliveryDataModel.setReasonID(srdDataModel.getReasonID());
                                        salesReturnDeliveryDataModel.setCaseSize(srdDataModel.getCaseSize());

                                        salesReturnDeliveryDataModel.setOldMrp(srdDataModel.getOldMrp());
                                        salesReturnDeliveryDataModel.setMfgDate(srdDataModel.getMfgDate());

                                        salesReturnDeliveryDataModel.setExpDate(srdDataModel.getExpDate());
                                        salesReturnDeliveryDataModel.setOuterQty(srdDataModel.getOuterQty());

                                        salesReturnDeliveryDataModel.setdOUomQty(srdDataModel.getdOUomQty());
                                        salesReturnDeliveryDataModel.setdOUomId(srdDataModel.getdOUomId());

                                        salesReturnDeliveryDataModel.setdUomId(srdDataModel.getdUomId());
                                        salesReturnDeliveryDataModel.setInVoiceNumber(srdDataModel.getInVoiceNumber());

                                        salesReturnDeliveryDataModel.setSrpedit(srdDataModel.getSrpedit());
                                        salesReturnDeliveryDataModel.setTotalQuantity(srdDataModel.getTotalQuantity());

                                        salesReturnDeliveryDataModel.setTotalAmount(srdDataModel.getTotalAmount());
                                        salesReturnDeliveryDataModel.setRetailerId(srdDataModel.getRetailerId());

                                        salesReturnDeliveryDataModel.setReasonType(srdDataModel.getReasonType());

                                        salesReturnDeliveryDataModel.setLotNumber(srdDataModel.getLotNumber());
                                        salesReturnDeliveryDataModel.setPieceUomId(srdDataModel.getPieceUomId());

                                        salesReturnDeliveryDataModel.setStatus(srdDataModel.getStatus());
                                        salesReturnDeliveryDataModel.setHnsCode(srdDataModel.getHnsCode());
                                        salesReturnDeliveryDataModel.setActualCaseQuantity(cur.getInt(3));
                                        salesReturnDeliveryDataModel.setActualPieceQuantity(cur.getInt(2));

                                        actCQty += cur.getInt(3);
                                        actPQty += cur.getInt(2);

                                        skuLevelReturnData.add(salesReturnDeliveryDataModel);
                                    }
                                    cur.close();
                                }
                                srdDataModel.setActualCaseQuantity(actCQty);
                                srdDataModel.setActualPieceQuantity(actPQty);
                                String key = srdDataModel.getProductId() + srdDataModel.getReasonID() + srdDataModel.getInVoiceNumber();
                                srdDataModelMap.put(key, skuLevelReturnData);
                            }
                            setSalesReturnDelDataMap(srdDataModelMap);
                        }
                        subscriber.onNext(returnDeliveryDataModelVector);
                        subscriber.onComplete();
                    }
                } catch (Exception exception) {
                    Commons.printException(exception);
                    subscriber.onError(exception);
                    subscriber.onComplete();
                }

            }
        });
    }

    public boolean cancelSalesReturnDelivery(Context mContext, SalesReturnDeliveryDataBo salesReturnDeliveryDataBo) {

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            BusinessModel businessModel = (BusinessModel) mContext.getApplicationContext();

            String uid = ("SR"
                    + businessModel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));


            // To generate Seqno based Sales Return Id
            if (businessModel.configurationMasterHelper.SHOW_SR_SEQUENCE_NO) {
                String seqNo;
                businessModel.insertSeqNumber("SR");
                seqNo = businessModel.downloadSequenceNo("SR");
                uid = seqNo;
            }

            int indicativeFlag = 0;
            if (businessModel.configurationMasterHelper.IS_INDICATIVE_SR)
                indicativeFlag = 1;

            String columns;
            String values;
            columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule,RefUID,isCancel,ridSF,VisitId";

            values = StringUtils.getStringQueryParam(uid) + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                    + StringUtils.getStringQueryParam(businessModel.retailerMasterBO.getRetailerID()) + ","
                    + businessModel.retailerMasterBO.getBeatID() + ","
                    + businessModel.getAppDataProvider().getUser().getUserid()
                    + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getReturnValue()) + "," + salesReturnDeliveryDataBo.getLpc() + ","
                    + StringUtils.getStringQueryParam(businessModel.retailerMasterBO.getRetailerCode()) + ","
                    + StringUtils.getStringQueryParam(businessModel.getSaleReturnNote()) + ","
                    + StringUtils.getStringQueryParam(businessModel.mSelectedRetailerLatitude + "") + ","
                    + StringUtils.getStringQueryParam(businessModel.mSelectedRetailerLongitude + "") + ","
                    + businessModel.retailerMasterBO.getDistributorId() + ","
                    + businessModel.retailerMasterBO.getDistParentId() + ","
                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getSignaturePath()) + ","
                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getSignatureName()) + ","
                    + indicativeFlag + ","
                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getRefModuleTId()) + ","
                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getRefModule()) + "," +
                    StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getUId()) + "," + StringUtils.getStringQueryParam("1") + ","
                    + StringUtils.getStringQueryParam(businessModel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                    + businessModel.getAppDataProvider().getUniqueId();

            db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

            db.closeDB();
            return true;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }

    }

    public boolean saveSalesReturnDelivery(Context mContext, List<SalesReturnDeliveryDataModel> list, SalesReturnDeliveryDataBo salesReturnDeliveryDataBo) {
        try {


            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            boolean isData;
            BusinessModel businessModel = (BusinessModel) mContext.getApplicationContext();

            int indicativeFlag = 0;

            String[] invAndUserId = getUserIdAndInvId(db, businessModel);
            int srUserId = SDUtil.convertToInt(invAndUserId[0]);
            String invoiceID = invAndUserId[1];

            if (businessModel.configurationMasterHelper.IS_INDICATIVE_SR)
                indicativeFlag = 1;


            String sb = "select uid from SalesReturnHeader where RefUID=" + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getUId()) +
                    " AND RetailerID=" +
                    StringUtils.getStringQueryParam(businessModel.getAppDataProvider().getRetailMaster().getRetailerID()) +
                    " AND upload='N'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    String uid = c.getString(0);
                    db.deleteSQL(DataMembers.tbl_SalesReturnHeader, "uid="
                            + DatabaseUtils.sqlEscapeString(uid), false);
                    db.deleteSQL(DataMembers.tbl_SalesReturnDetails, "uid="
                            + DatabaseUtils.sqlEscapeString(uid), false);
                }
                c.close();
            }

            String uid = ("SR"
                    + businessModel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));


            // To generate Seqno based Sales Return Id
            if (businessModel.configurationMasterHelper.SHOW_SR_SEQUENCE_NO) {
                String seqNo;
                businessModel.insertSeqNumber("SR");
                seqNo = businessModel.downloadSequenceNo("SR");
                uid = seqNo;
            }

            isData = false;
            String columns;
            String values;
            int totalQty;
            double totalvalue = 0;
            int lpcCount = 0;
            if (!businessModel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL) {
                for (int i = 0; i < list.size(); i++) {
                    SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = list.get(i);
                    totalQty = salesReturnDeliveryDataModel.getActualPieceQuantity()
                            + (salesReturnDeliveryDataModel.getActualCaseQuantity() * salesReturnDeliveryDataModel
                            .getCaseSize())
                            + (salesReturnDeliveryDataModel.getOuterQty() * salesReturnDeliveryDataModel
                            .getOuterSize());

                    columns = "uid,ProductID,Pqty,Cqty,Condition,duomQty,oldmrp,mfgdate,expdate,outerQty," +
                            "dOuomQty,dOuomid,duomid,batchid,invoiceno,srpedited,totalQty,totalamount," +
                            "RetailerID,reason_type,LotNumber,piece_uomid,status,HsnCode,RefUID";
                    if (salesReturnDeliveryDataModel.getActualPieceQuantity() > 0 ||
                            salesReturnDeliveryDataModel.getActualCaseQuantity() > 0) {


                        values = StringUtils.getStringQueryParam(uid)
                                + ","
                                + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getProductId())
                                + ","
                                + salesReturnDeliveryDataModel.getActualPieceQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getActualCaseQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getReasonID()
                                + ","
                                + salesReturnDeliveryDataModel.getCaseSize()
                                + ","
                                + StringUtils.getStringQueryParam(Utils.formatAsTwoDecimal(salesReturnDeliveryDataModel
                                .getOldMrp()))
                                + ","
                                + DatabaseUtils
                                .sqlEscapeString(salesReturnDeliveryDataModel.getMfgDate())
                                + ","
                                + DatabaseUtils
                                .sqlEscapeString(salesReturnDeliveryDataModel.getExpDate())
                                + ","
                                + salesReturnDeliveryDataModel.getOuterQty()
                                + ","
                                + salesReturnDeliveryDataModel.getOuterSize()
                                + ","
                                + salesReturnDeliveryDataModel.getdOUomId()
                                + ","
                                + salesReturnDeliveryDataModel.getCaseUomId()
                                + ","
                                + salesReturnDeliveryDataModel.getProductId()
                                + ","
                                + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getInvoiceId())
                                + ","
                                + salesReturnDeliveryDataModel.getSrpedit()
                                + ","
                                + totalQty
                                + ","
                                + totalvalue
                                + ","
                                + StringUtils.getStringQueryParam(businessModel.retailerMasterBO
                                .getRetailerID()) + ","
                                + salesReturnDeliveryDataModel.getReasonCategory() + ","
                                + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getLotNumber() + "")
                                + "," + salesReturnDeliveryDataModel.getPieceUomId()
                                + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getStatus() + "")
                                + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getHnsCode())
                                + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getUId());


                        db.insertSQL(
                                DataMembers.tbl_SalesReturnDetails,
                                columns, values);
                        lpcCount++;
                    }
                    isData = true;
                }
            }


            if (businessModel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL) {
                for (SalesReturnDeliveryDataModel dataModel : list) {
                    String key = dataModel.getProductId() + dataModel.getReasonID() + dataModel.getInVoiceNumber();
                    ArrayList<SalesReturnDeliveryDataModel> srdDataList = new ArrayList<>();
                    if (getSalesReturnDelDataMap() != null
                            && !getSalesReturnDelDataMap().isEmpty()
                            && getSalesReturnDelDataMap().containsKey(key))
                        srdDataList = getSalesReturnDelDataMap().get(key);

                    for (int i = 0; i < srdDataList.size(); i++) {
                        SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = srdDataList.get(i);
                        totalQty = salesReturnDeliveryDataModel.getActualPieceQuantity()
                                + (salesReturnDeliveryDataModel.getActualCaseQuantity() * salesReturnDeliveryDataModel
                                .getCaseSize())
                                + (salesReturnDeliveryDataModel.getOuterQty() * salesReturnDeliveryDataModel
                                .getOuterSize());

                        columns = "uid,ProductID,Pqty,Cqty,Condition,duomQty,oldmrp,mfgdate,expdate,outerQty," +
                                "dOuomQty,dOuomid,duomid,batchid,invoiceno,srpedited,totalQty,totalamount," +
                                "RetailerID,reason_type,LotNumber,piece_uomid,status,HsnCode,RefUID";
                        if (salesReturnDeliveryDataModel.getActualPieceQuantity() > 0 ||
                                salesReturnDeliveryDataModel.getActualCaseQuantity() > 0) {


                            values = StringUtils.getStringQueryParam(uid)
                                    + ","
                                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getProductId())
                                    + ","
                                    + salesReturnDeliveryDataModel.getActualPieceQuantity()
                                    + ","
                                    + salesReturnDeliveryDataModel.getActualCaseQuantity()
                                    + ","
                                    + salesReturnDeliveryDataModel.getReasonID()
                                    + ","
                                    + salesReturnDeliveryDataModel.getCaseSize()
                                    + ","
                                    + StringUtils.getStringQueryParam(Utils.formatAsTwoDecimal(salesReturnDeliveryDataModel
                                    .getOldMrp()))
                                    + ","
                                    + DatabaseUtils
                                    .sqlEscapeString(salesReturnDeliveryDataModel.getMfgDate())
                                    + ","
                                    + DatabaseUtils
                                    .sqlEscapeString(salesReturnDeliveryDataModel.getExpDate())
                                    + ","
                                    + salesReturnDeliveryDataModel.getOuterQty()
                                    + ","
                                    + salesReturnDeliveryDataModel.getOuterSize()
                                    + ","
                                    + salesReturnDeliveryDataModel.getdOUomId()
                                    + ","
                                    + salesReturnDeliveryDataModel.getCaseUomId()
                                    + ","
                                    + salesReturnDeliveryDataModel.getProductId()
                                    + ","
                                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getInVoiceNumber())
                                    + ","
                                    + salesReturnDeliveryDataModel.getSrpedit()
                                    + ","
                                    + totalQty
                                    + ","
                                    + totalvalue
                                    + ","
                                    + StringUtils.getStringQueryParam(businessModel.retailerMasterBO
                                    .getRetailerID()) + ","
                                    + salesReturnDeliveryDataModel.getReasonType() + ","
                                    + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getLotNumber() + "")
                                    + "," + salesReturnDeliveryDataModel.getPieceUomId()
                                    + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getStatus() + "")
                                    + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getHnsCode())
                                    + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataModel.getUId());


                            db.insertSQL(
                                    DataMembers.tbl_SalesReturnDetails,
                                    columns, values);
                            lpcCount++;
                        }
                        isData = true;
                    }
                }
            }

            if (isData) {
                // Preapre and save salesreturn header.
                columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule,RefUID,CollectStatus,invoiceid,ridSF,VisitId";

                values = StringUtils.getStringQueryParam(uid) + ","
                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                        + StringUtils.getStringQueryParam(businessModel.retailerMasterBO.getRetailerID()) + ","
                        + businessModel.retailerMasterBO.getBeatID() + ","
                        + srUserId
                        + "," + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getReturnValue()) + "," + lpcCount + ","
                        + StringUtils.getStringQueryParam(businessModel.retailerMasterBO.getRetailerCode()) + ","
                        + StringUtils.getStringQueryParam(businessModel.getSaleReturnNote()) + ","
                        + StringUtils.getStringQueryParam(businessModel.mSelectedRetailerLatitude + "") + ","
                        + StringUtils.getStringQueryParam(businessModel.mSelectedRetailerLongitude + "") + ","
                        + businessModel.retailerMasterBO.getDistributorId() + ","
                        + businessModel.retailerMasterBO.getDistParentId() + ","
                        + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getSignaturePath()) + ","
                        + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getSignatureName()) + ","
                        + indicativeFlag + ","
                        + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getRefModuleTId()) + ","
                        + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getRefModule()) + ","
                        + StringUtils.getStringQueryParam(salesReturnDeliveryDataBo.getUId()) + ","
                        + StringUtils.getStringQueryParam("F") + ","
                        + StringUtils.getStringQueryParam(invoiceID) + ","
                        + StringUtils.getStringQueryParam(businessModel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                        + businessModel.getAppDataProvider().getUniqueId();

                db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

            }
            db.closeDB();
            return true;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }

    }

    public boolean SHOW_SALES_RET_CASE, SHOW_SALES_RET_PCS;

    /**
     * This method will load salesreturn related configurations and set the variables.
     */
    public void loadConfigurations(Context mContext) {
        try {
            SHOW_SALES_RET_CASE = false;
            SHOW_SALES_RET_PCS = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SR01' and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            String codeValue = null;
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            if (codeValue != null) {
                String[] codeSplit = codeValue.split(",");
                for (String temp : codeSplit) {
                    if ("CS".equalsIgnoreCase(temp))
                        SHOW_SALES_RET_CASE = true;
                    else if ("PS".equalsIgnoreCase(temp))
                        SHOW_SALES_RET_PCS = true;

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public HashMap<String, ArrayList<SalesReturnDeliveryDataModel>> getSalesReturnDelDataMap() {
        return salesReturnDelDataMap;
    }

    public void setSalesReturnDelDataMap(HashMap<String, ArrayList<SalesReturnDeliveryDataModel>> salesReturnDelDataMap) {
        this.salesReturnDelDataMap = salesReturnDelDataMap;
    }

    public boolean hasDatatoSave(List<SalesReturnDeliveryDataModel> list) {
        int siz = list.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            SalesReturnDeliveryDataModel product = list.get(i);
            if (product.getActualCaseQuantity() > 0
                    || product.getActualPieceQuantity() > 0)
                return true;
        }
        return false;
    }


    private String[] getUserIdAndInvId(DBUtil db, BusinessModel businessModel) {
        try {
            String[] iD = new String[2];
            String query = "select UserID,invoiceid from SalesReturnHeader" +
                    " Where RetailerID=" + StringUtils.getStringQueryParam(businessModel.getRetailerMasterBO().getRetailerID()) +
                    " AND Upload = 'X'";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                if (c.moveToNext()) {
                    iD[0] = c.getString(0);
                    iD[1] = c.getString(1);
                }
            }
            c.close();
            return iD;
        } catch (Exception e) {
            Commons.printException(e);
        }
        return null;
    }
}
