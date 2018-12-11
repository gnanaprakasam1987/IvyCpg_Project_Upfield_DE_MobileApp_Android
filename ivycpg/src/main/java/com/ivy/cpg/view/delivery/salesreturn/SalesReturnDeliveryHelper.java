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
import com.ivy.utils.AppUtils;

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
    private ArrayList<SalesReturnDeliveryDataModel> skuLevelDataList;

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
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);

                    dbUtil.openDataBase();
                    Cursor cursor = dbUtil.selectSQL("Select " +
                            "uid,date,ReturnValue,Lpc,invoiceid,SignaturePath,ImgName,RefModule,RefModuleTId " +
                            "from SalesReturnHeader where retailerId ='" + businessModel.getRetailerMasterBO().getRetailerID() + "' " +
                            "AND upload='X' " +
                            "AND uid NOT IN (select ifnull(RefUID,0) from salesReturnHeader " +
                            "where upload='Y'and isCancel != 1) ");

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
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
                                Cursor cur = dbUtil.selectSQL("select pm.pid,pm.pname,ifnull(srd.pqty,0) as pqty,ifnull(srd.cqty,0) as cqty,srd .* from productmaster pm left join SalesReturnDetails srd on pm.pid = srd.productid where pm.parentid = '" +
                                        srdDataModel.getProductId() + "'");
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

                                        actCQty += cur.getInt(3);
                                        actPQty += cur.getInt(2);
                                        salesReturnDeliveryDataModel.setActualCaseQuantity(cur.getInt(3));
                                        salesReturnDeliveryDataModel.setActualPieceQuantity(cur.getInt(2));
                                        skuLevelReturnData.add(salesReturnDeliveryDataModel);
                                    }
                                    cur.close();
                                }
                                srdDataModel.setActualCaseQuantity(actCQty);
                                srdDataModel.setActualPieceQuantity(actPQty);
                                srdDataModelMap.put(srdDataModel.getProductId(), skuLevelReturnData);
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

    public boolean cancelSalesReturnDelivery(Context mContext, SalesReturnDeliveryDataBo salesReturnDeliveryDataBo){

        try{
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            BusinessModel businessModel = (BusinessModel) mContext.getApplicationContext();

            String uid =  ("SR"
                    + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));


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
            columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule,RefUID,isCancel";

            values = AppUtils.QT(uid) + ","
                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + AppUtils.QT(businessModel.retailerMasterBO.getRetailerID()) + ","
                    + businessModel.retailerMasterBO.getBeatID() + ","
                    + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "," + AppUtils.QT(salesReturnDeliveryDataBo.getReturnValue()) + "," + 0 + ","
                    + AppUtils.QT(businessModel.retailerMasterBO.getRetailerCode()) + ","
                    + AppUtils.QT(businessModel.getSaleReturnNote()) + ","
                    + AppUtils.QT(businessModel.mSelectedRetailerLatitude + "") + ","
                    + AppUtils.QT(businessModel.mSelectedRetailerLongitude + "") + ","
                    + businessModel.retailerMasterBO.getDistributorId() + ","
                    + businessModel.retailerMasterBO.getDistParentId() + ","
                    + AppUtils.QT(salesReturnDeliveryDataBo.getSignaturePath()) + ","
                    + AppUtils.QT(salesReturnDeliveryDataBo.getSignatureName()) + ","
                    + indicativeFlag + ","
                    + AppUtils.QT(salesReturnDeliveryDataBo.getRefModuleTId()) + ","
                    + AppUtils.QT(salesReturnDeliveryDataBo.getRefModule())+"," +
                    AppUtils.QT(salesReturnDeliveryDataBo.getUId())+","+AppUtils.QT("1");

            db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

            db.closeDB();
            return true;
        }catch (Exception e) {
            Commons.printException(e);
            return false;
        }

    }

    public boolean saveSalesReturnDelivery(Context mContext, List<SalesReturnDeliveryDataModel> list, SalesReturnDeliveryDataBo salesReturnDeliveryDataBo) {
        try {


            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            boolean isData;
            BusinessModel businessModel = (BusinessModel) mContext.getApplicationContext();

            int indicativeFlag = 0;

            if (businessModel.configurationMasterHelper.IS_INDICATIVE_SR)
                indicativeFlag = 1;


            String sb = "select uid from SalesReturnHeader where RefUID=" + AppUtils.QT(salesReturnDeliveryDataBo.getUId()) +
                    " AND RetailerID=" +
                    AppUtils.QT(businessModel.getRetailerMasterBO().getRetailerID()) +
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
                    + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));


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


                        values = AppUtils.QT(uid)
                                + ","
                                + AppUtils.QT(salesReturnDeliveryDataModel.getProductId())
                                + ","
                                + salesReturnDeliveryDataModel.getActualPieceQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getActualCaseQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getReasonID()
                                + ","
                                + salesReturnDeliveryDataModel.getCaseSize()
                                + ","
                                + AppUtils.QT(Utils.formatAsTwoDecimal(salesReturnDeliveryDataModel
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
                                + AppUtils.QT(salesReturnDeliveryDataModel.getInvoiceId())
                                + ","
                                + salesReturnDeliveryDataModel.getSrpedit()
                                + ","
                                + totalQty
                                + ","
                                + totalvalue
                                + ","
                                + AppUtils.QT(businessModel.retailerMasterBO
                                .getRetailerID()) + ","
                                + salesReturnDeliveryDataModel.getReasonCategory() + ","
                                + AppUtils.QT(salesReturnDeliveryDataModel.getLotNumber() + "")
                                + "," + salesReturnDeliveryDataModel.getPieceUomId()
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getStatus() + "")
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getHnsCode())
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getUId());


                        db.insertSQL(
                                DataMembers.tbl_SalesReturnDetails,
                                columns, values);
                    }
                    isData = true;
                }
            }

            ArrayList<SalesReturnDeliveryDataModel> srdDataList = getSkuLevelDataList();
            if (businessModel.configurationMasterHelper.IS_SR_DELIVERY_SKU_LEVEL && srdDataList != null) {
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


                        values = AppUtils.QT(uid)
                                + ","
                                + AppUtils.QT(salesReturnDeliveryDataModel.getProductId())
                                + ","
                                + salesReturnDeliveryDataModel.getActualPieceQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getActualCaseQuantity()
                                + ","
                                + salesReturnDeliveryDataModel.getReasonID()
                                + ","
                                + salesReturnDeliveryDataModel.getCaseSize()
                                + ","
                                + AppUtils.QT(Utils.formatAsTwoDecimal(salesReturnDeliveryDataModel
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
                                + AppUtils.QT(salesReturnDeliveryDataModel.getInvoiceId())
                                + ","
                                + salesReturnDeliveryDataModel.getSrpedit()
                                + ","
                                + totalQty
                                + ","
                                + totalvalue
                                + ","
                                + AppUtils.QT(businessModel.retailerMasterBO
                                .getRetailerID()) + ","
                                + salesReturnDeliveryDataModel.getReasonCategory() + ","
                                + AppUtils.QT(salesReturnDeliveryDataModel.getLotNumber() + "")
                                + "," + salesReturnDeliveryDataModel.getPieceUomId()
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getStatus() + "")
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getHnsCode())
                                + "," + AppUtils.QT(salesReturnDeliveryDataModel.getUId());


                        db.insertSQL(
                                DataMembers.tbl_SalesReturnDetails,
                                columns, values);
                    }
                    isData = true;
                }
            }

            if (isData) {
                // Preapre and save salesreturn header.
                columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule,RefUID";

                values = AppUtils.QT(uid) + ","
                        + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + AppUtils.QT(businessModel.retailerMasterBO.getRetailerID()) + ","
                        + businessModel.retailerMasterBO.getBeatID() + ","
                        + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                        + "," + AppUtils.QT(salesReturnDeliveryDataBo.getReturnValue()) + "," + 0 + ","
                        + AppUtils.QT(businessModel.retailerMasterBO.getRetailerCode()) + ","
                        + AppUtils.QT(businessModel.getSaleReturnNote()) + ","
                        + AppUtils.QT(businessModel.mSelectedRetailerLatitude + "") + ","
                        + AppUtils.QT(businessModel.mSelectedRetailerLongitude + "") + ","
                        + businessModel.retailerMasterBO.getDistributorId() + ","
                        + businessModel.retailerMasterBO.getDistParentId() + ","
                        + AppUtils.QT(salesReturnDeliveryDataBo.getSignaturePath()) + ","
                        + AppUtils.QT(salesReturnDeliveryDataBo.getSignatureName()) + ","
                        + indicativeFlag + ","
                        + AppUtils.QT(salesReturnDeliveryDataBo.getRefModuleTId()) + ","
                        + AppUtils.QT(salesReturnDeliveryDataBo.getRefModule()) + "," +
                        AppUtils.QT(salesReturnDeliveryDataBo.getUId());

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

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

    public ArrayList<SalesReturnDeliveryDataModel> getSkuLevelDataList() {
        return skuLevelDataList;
    }

    public void setSkuLevelDataList(ArrayList<SalesReturnDeliveryDataModel> skuLevelDataList) {
        this.skuLevelDataList = skuLevelDataList;
    }
}
