package com.ivy.cpg.view.salesdeliveryreturn;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.AppUtils;

import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SalesReturnDeliveryHelper {
    private static SalesReturnDeliveryHelper instance = null;

    private SalesReturnDeliveryHelper() {
    }

    public static SalesReturnDeliveryHelper getInstance() {
        if (instance == null) {
            instance = new SalesReturnDeliveryHelper();
        }
        return instance;
    }

    public Observable<Vector<SalesReturnDeliveryDataModel>> getSaleReturnDelivery(final Context context) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataModel>> subscriber) throws Exception {

                try {
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    Vector<SalesReturnDeliveryDataModel> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
                    dbUtil.openDataBase();
                    Cursor cursor = dbUtil.selectSQL("Select * from SalesReturnHeader where retailerId = " + businessModel.getRetailerMasterBO().getRetailerID()
                            + " AND  upload='X'");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                            salesReturnDeliveryDataModel.setUId(cursor.getString(0));
                            salesReturnDeliveryDataModel.setDate(cursor.getString(1));
                            salesReturnDeliveryDataModel.setReturnValue(cursor.getString(6));
                            salesReturnDeliveryDataModel.setLpc(cursor.getInt(7));
                            salesReturnDeliveryDataModel.setInvoiceId(cursor.getString(18));
                            returnDeliveryDataModelVector.add(salesReturnDeliveryDataModel);

                            setSignaturePath(String.valueOf(cursor.getString(22)) != null ? String.valueOf(cursor.getString(22)) : "");
                            setSignatureName(String.valueOf(cursor.getString(21)) != null ? String.valueOf(cursor.getString(21)) : "");
                            setRefModule(String.valueOf(cursor.getString(23)) != null ? String.valueOf(cursor.getString(23)) : "");
                            setRefModuleTId(String.valueOf(cursor.getString(24)));
                            setReturnValue(Double.valueOf(cursor.getString(6)));
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


    public Observable<Vector<SalesReturnDeliveryDataModel>> getSaleReturnDeliveryDetails(final Context context, final String uId) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataModel>> subscriber) throws Exception {

                try {
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    Vector<SalesReturnDeliveryDataModel> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
                    dbUtil.openDataBase();

                    Cursor cursor = dbUtil
                            .selectSQL("SELECT distinct A.ListName as reasonDesc,PM.PName as ProductName,PM.PID as ProductID,srd .* from SalesReturnDetails srd"
                                    + " LEFT JOIN ProductMaster PM ON srd.productID=PM.PID"
                                    + " inner join StandardListMaster A INNER JOIN StandardListMaster B ON"
                                    + " A.ParentId = B.ListId AND"
                                    + " ( B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                                    + "' OR B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "')"
                                    + " AND A.listId = srd.condition WHERE A.ListType = 'REASON'"
                                    + " AND srd.RetailerID=" + AppUtils.QT(businessModel.getRetailerMasterBO().getRetailerID())
                                    + " AND upload ='X'");


                    Cursor cursor1 = dbUtil.selectSQL("SELECT Pqty,Cqty from SalesReturnDetails where RetailerID=" + AppUtils.QT(businessModel.getRetailerMasterBO().getRetailerID())
                            + " AND upload ='N'");


                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                            salesReturnDeliveryDataModel.setUId(cursor.getString(0));
                            salesReturnDeliveryDataModel.setProductName(cursor.getString(cursor.getColumnIndex("ProductName")));
                            salesReturnDeliveryDataModel.setProductId(cursor.getString(cursor.getColumnIndex("ProductID")));


                            salesReturnDeliveryDataModel.setReturnCaseQuantity(cursor.getInt(cursor.getColumnIndex("Cqty")));
                            salesReturnDeliveryDataModel.setReturnPieceQuantity(cursor.getInt(cursor.getColumnIndex("Pqty")));

                            salesReturnDeliveryDataModel.setReason(cursor.getString(cursor.getColumnIndex("reasonDesc")));
                            salesReturnDeliveryDataModel.setReasonCategory(cursor.getString(7));
                            salesReturnDeliveryDataModel.setReasonID(cursor.getString(7));
                            salesReturnDeliveryDataModel.setCaseSize(cursor.getInt(3));

                            salesReturnDeliveryDataModel.setOldMrp(cursor.getDouble(11));
                            salesReturnDeliveryDataModel.setMfgDate(cursor.getString(12));

                            salesReturnDeliveryDataModel.setExpDate(cursor.getString(13));
                            salesReturnDeliveryDataModel.setOuterQty(cursor.getInt(14));

                            salesReturnDeliveryDataModel.setdOUomQty(cursor.getInt(15));
                            salesReturnDeliveryDataModel.setdOUomId(cursor.getInt(16));

                            salesReturnDeliveryDataModel.setdUomId(cursor.getInt(17));
                            salesReturnDeliveryDataModel.setInVoiceNumber(cursor.getString(18));

                            salesReturnDeliveryDataModel.setSrpedit(cursor.getFloat(19));
                            salesReturnDeliveryDataModel.setTotalQuantity(cursor.getInt(20));

                            salesReturnDeliveryDataModel.setTotalAmount(cursor.getString(21));
                            salesReturnDeliveryDataModel.setRetailerId(cursor.getString(22));

                            salesReturnDeliveryDataModel.setReasonType(cursor.getInt(23));

                            salesReturnDeliveryDataModel.setLotNumber(cursor.getInt(24));
                            salesReturnDeliveryDataModel.setPieceUomId(cursor.getInt(25));

                            salesReturnDeliveryDataModel.setStatus(cursor.getInt(26));
                            salesReturnDeliveryDataModel.setHnsCode(cursor.getString(27)  != null ?cursor.getString(27) :"" );

                            if (cursor1 != null) {
                                while (cursor1.moveToNext()) {
                                    salesReturnDeliveryDataModel.setActualPieceQuantity(cursor1.getInt(0));
                                    salesReturnDeliveryDataModel.setActualCaseQuantity(cursor1.getInt(1));
                                }
                            }


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

    public boolean saveSalesReturnDelivery(Context mContext, List<SalesReturnDeliveryDataModel> list) {
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


            String sb = "select uid from SalesReturnHeader where uid=" + AppUtils.QT(getUid()) +
                    " AND RetailerID=" +
                    AppUtils.QT(businessModel.getRetailerMasterBO().getRetailerID()) +
                    " AND upload='N' and distributorid=" + businessModel.retailerMasterBO.getDistributorId();
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

            setUid("SR"
                    + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));


            // To generate Seqno based Sales Return Id
            if (businessModel.configurationMasterHelper.SHOW_SR_SEQUENCE_NO) {
                String seqNo;
                businessModel.insertSeqNumber("SR");
                seqNo = businessModel.downloadSequenceNo("SR");
                setUid(seqNo);
            }

            isData = false;
            String columns;
            String values;
            int totalQty;
            double totalvalue = 0;
            for (int i = 0; i < list.size(); i++) {
                SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = list.get(i);
                totalQty = salesReturnDeliveryDataModel.getActualPieceQuantity()
                        + (salesReturnDeliveryDataModel.getActualCaseQuantity() * salesReturnDeliveryDataModel
                        .getCaseSize())
                        + (salesReturnDeliveryDataModel.getOuterQty() * salesReturnDeliveryDataModel
                        .getOuterSize());

                columns = "uid,ProductID,Pqty,Cqty,Condition,duomQty,oldmrp,mfgdate,expdate,outerQty,dOuomQty,dOuomid,duomid,batchid,invoiceno,srpedited,totalQty,totalamount,RetailerID,reason_type,LotNumber,piece_uomid,status,HsnCode";
                if (salesReturnDeliveryDataModel.getActualPieceQuantity() > 0 ||
                        salesReturnDeliveryDataModel.getActualCaseQuantity() > 0) {


                    values = AppUtils.QT(getUid())
                            + ","
                            + AppUtils.QT(salesReturnDeliveryDataModel.getProductId())
                            + ","
                            + salesReturnDeliveryDataModel.getActualPieceQuantity()
                            + ","
                            + salesReturnDeliveryDataModel.getActualCaseQuantity()
                            + ","
                            + DatabaseUtils.sqlEscapeString(salesReturnDeliveryDataModel
                            .getReasonID())
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
                            + salesReturnDeliveryDataModel.getReasonCategory() + "," + AppUtils.QT(salesReturnDeliveryDataModel.getLotNumber() + "") + "," + salesReturnDeliveryDataModel.getPieceUomId()
                            + "," + AppUtils.QT(salesReturnDeliveryDataModel.getStatus() + "") + "," + AppUtils.QT(salesReturnDeliveryDataModel.getHnsCode());


                    db.insertSQL(
                            DataMembers.tbl_SalesReturnDetails,
                            columns, values);
                }
                isData = true;
            }

            if (isData) {
                // Preapre and save salesreturn header.
                columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,IFlag,RefModuleTId,RefModule";

                if (businessModel.configurationMasterHelper.IS_INVOICE_SR)
                    columns = columns + ",invoiceid";

                values = AppUtils.QT(getUid()) + ","
                        + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + AppUtils.QT(businessModel.retailerMasterBO.getRetailerID()) + ","
                        + businessModel.retailerMasterBO.getBeatID() + ","
                        + businessModel.userMasterHelper.getUserMasterBO().getUserid()
                        + "," + AppUtils.QT(String.valueOf(getReturnValue())) + "," + getLpc() + ","
                        + AppUtils.QT(businessModel.retailerMasterBO.getRetailerCode()) + ","
                        + AppUtils.QT(businessModel.getSaleReturnNote()) + ","
                        + AppUtils.QT(businessModel.mSelectedRetailerLatitude + "") + ","
                        + AppUtils.QT(businessModel.mSelectedRetailerLongitude + "") + ","
                        + businessModel.retailerMasterBO.getDistributorId() + ","
                        + businessModel.retailerMasterBO.getDistParentId() + ","
                        + AppUtils.QT(getSignaturePath() != null ? getSignaturePath() : "") + ","
                        + AppUtils.QT(getSignatureName()) + ","
                        + indicativeFlag + ","
                        + AppUtils.QT(getRefModuleTId()) + ","
                        + AppUtils.QT(getRefModule());


                if (businessModel.configurationMasterHelper.IS_INVOICE_SR)
                    values = values + "," + AppUtils.QT(getInvoiceNo());

                db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

            }
            db.closeDB();
            return true;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }

    }

    private String uid;
    private String InvoiceNo;
    private double returnValue;
    private int lpc;
    private String signaturePath = "";
    private String signatureName = "";
    private String refModuleTId = "";
    private String refModule = "";

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getRefModuleTId() {
        return refModuleTId;
    }

    public void setRefModuleTId(String refModuleTId) {
        this.refModuleTId = refModuleTId;
    }

    public String getRefModule() {
        return refModule;
    }

    public void setRefModule(String refModule) {
        this.refModule = refModule;
    }

    public int getLpc() {
        return lpc;
    }

    public void setLpc(int lpc) {
        this.lpc = lpc;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setInvoiceNo(String invoiceNo) {
        InvoiceNo = invoiceNo;
    }

    public String getInvoiceNo() {
        return InvoiceNo;
    }

    public void setReturnValue(double returnValue) {
        this.returnValue = returnValue;
    }

    public double getReturnValue() {
        return returnValue;
    }
}
