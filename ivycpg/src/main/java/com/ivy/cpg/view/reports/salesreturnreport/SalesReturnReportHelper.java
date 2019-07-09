package com.ivy.cpg.view.reports.salesreturnreport;


import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.salesreturnreport.salesreportdetails.SalesReturnDetailsReportBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SalesReturnReportHelper {


    public List<SalesReturnReportBo> getSalesReturnHeaderValue(Context context) {
        List<SalesReturnReportBo> salesReturnReportBoList = new ArrayList<>();
        try {


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();


            String query = "SELECT  distinct RM.RetailerName,srh.* from SalesReturnHeader srh"
                    + " LEFT JOIN RetailerMaster RM ON srh.RetailerID=RM.RetailerID"
                    + " where srh.upload!='X' and date =" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    SalesReturnReportBo salesReturnReportBo = new SalesReturnReportBo();
                    salesReturnReportBo.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));
                    salesReturnReportBo.setUId(c.getString(c.getColumnIndex("uid")));
                    salesReturnReportBo.setDate(c.getString(c.getColumnIndex("date")));
                    salesReturnReportBo.setReturnValue(c.getDouble(c.getColumnIndex("ReturnValue")));
                    salesReturnReportBo.setLpc(c.getInt(c.getColumnIndex("Lpc")));
                    salesReturnReportBo.setDistributorId(c.getInt(c.getColumnIndex("distributorid")));
                    salesReturnReportBo.setRetailerId(c.getInt(c.getColumnIndex("RetailerID")));
                    salesReturnReportBo.setInvoiceNumber(c.getString(c.getColumnIndex("invoiceid")));
                    salesReturnReportBoList.add(salesReturnReportBo);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return new ArrayList<>();

        }
        return salesReturnReportBoList;
    }


    public Observable<Vector<SalesReturnDetailsReportBo>> getSaleReturnDeliveryDetails(final Context context, final String uId) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDetailsReportBo>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDetailsReportBo>> subscriber) throws Exception {

                try {


                    Vector<SalesReturnDetailsReportBo> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME);
                    dbUtil.openDataBase();

                    Cursor cursor = dbUtil
                            .selectSQL("SELECT  distinct PM.PName as ProductName,PM.PID as ProductID,srd.* ,SM.ListName,SM.ListType,SMP.ListName as type from SalesReturnDetails srd"
                                    + " LEFT JOIN ProductMaster PM ON srd.productID=PM.PID"
                                    + " LEFT JOIN StandardListMaster SM on SM.ListId = srd.Condition"
                                    + " LEFT JOIN StandardListMaster SMP on SMP.ListId = SM.Parentid"
                                    + " WHERE srd.uid=" + StringUtils.getStringQueryParam(uId));


                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDetailsReportBo salesReturnDeliveryDataModel = new SalesReturnDetailsReportBo();
                            salesReturnDeliveryDataModel.setUid(cursor.getString(0));
                            salesReturnDeliveryDataModel.setProductName(cursor.getString(cursor.getColumnIndex("ProductName")));
                            salesReturnDeliveryDataModel.setProductId(cursor.getString(cursor.getColumnIndex("ProductID")));


                            salesReturnDeliveryDataModel.setCaseQty(cursor.getInt(cursor.getColumnIndex("Cqty")));
                            salesReturnDeliveryDataModel.setPieceQty(cursor.getInt(cursor.getColumnIndex("Pqty")));
                            salesReturnDeliveryDataModel.setOuterQty(cursor.getInt(cursor.getColumnIndex("outerQty")));


                            double value = (cursor.getInt(cursor.getColumnIndex("srpedited"))) *
                                    (cursor.getInt(cursor.getColumnIndex("totalQty")));

                            salesReturnDeliveryDataModel.setReturnValue(value);
                            salesReturnDeliveryDataModel.setReason(cursor.getString(cursor.getColumnIndex("ListName")));
                            salesReturnDeliveryDataModel.setReasonType(cursor.getString(cursor.getColumnIndex("type")));
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



    public int getTotalReturnValueHeader(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        Cursor c = db.selectSQL("select sum (ReturnValue) from SalesReturnHeader where date=" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
        int totalVal = 0;
        if (c != null) {
            if (c.moveToNext()) {
                totalVal = c.getInt(0);

            }
            c.close();
        }

        return totalVal;
    }
}
