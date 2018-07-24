package com.ivy.cpg.view.reports.salesreport;


import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.salesreport.salesreportdetails.SalesReturnDeliveryReportBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SalesReportHelper {


    public List<SalesReturnReportBo> getSalesReturnHeaderValue(Context context) {
        List<SalesReturnReportBo> salesReturnReportBoList = new ArrayList<>();
        try {


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();


            String query = "SELECT  distinct RM.RetailerName,srh.* from SalesReturnHeader srh"
                    + " LEFT JOIN RetailerMaster RM ON srh.RetailerID=RM.RetailerID"
                    + " where date =" + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    SalesReturnReportBo salesReturnReportBo = new SalesReturnReportBo();
                    salesReturnReportBo.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));
                    salesReturnReportBo.setUId(c.getString(c.getColumnIndex("uid")));
                    salesReturnReportBo.setDate(c.getString(c.getColumnIndex("date")));
                    salesReturnReportBo.setReturnValue(c.getString(c.getColumnIndex("ReturnValue")));
                    salesReturnReportBo.setLpc(c.getInt(c.getColumnIndex("Lpc")));
                    salesReturnReportBo.setDistributorId(c.getInt(c.getColumnIndex("distributorid")));
                    salesReturnReportBo.setRetailerId(c.getInt(c.getColumnIndex("RetailerID")));
                    salesReturnReportBo.setInvoiceNumber(c.getString(c.getColumnIndex("invoiceid")));
                    salesReturnReportBoList.add(salesReturnReportBo);
                }
            }
            c.close();
            db.closeDB();
            return salesReturnReportBoList;
        } catch (Exception e) {
            Commons.printException(e);
        }
        return salesReturnReportBoList;
    }


    public Observable<Vector<SalesReturnDeliveryReportBo>> getSaleReturnDeliveryDetails(final Context context, final String uId, final int retailerId) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryReportBo>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryReportBo>> subscriber) throws Exception {

                try {
                    Vector<SalesReturnDeliveryReportBo> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
                    dbUtil.openDataBase();
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();

                    Cursor cursor = dbUtil
                            .selectSQL("SELECT  distinct PM.PName as ProductName,PM.PID as ProductID,srd.* ,SM.ListName as reasonDesc from SalesReturnDetails srd"
                                    + " LEFT JOIN ProductMaster PM ON srd.productID=PM.PID"
                                    + " LEFT JOIN StandardListMaster SM on SM.ListType = srd.Condition"
                                    + " AND srd.RetailerID=" + retailerId
                                    + " AND srd.uid=" + AppUtils.QT(uId));


                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryReportBo salesReturnDeliveryDataModel = new SalesReturnDeliveryReportBo();
                            salesReturnDeliveryDataModel.setUid(cursor.getString(0));
                            salesReturnDeliveryDataModel.setProductName(cursor.getString(cursor.getColumnIndex("ProductName")));
                            salesReturnDeliveryDataModel.setProductId(cursor.getString(cursor.getColumnIndex("ProductID")));


                            salesReturnDeliveryDataModel.setCaseQty(cursor.getInt(cursor.getColumnIndex("Cqty")));
                            salesReturnDeliveryDataModel.setPieceQty(cursor.getInt(cursor.getColumnIndex("Pqty")));

                            int value = (cursor.getInt(cursor.getColumnIndex("srpedited"))) *
                                    (cursor.getInt(cursor.getColumnIndex("totalQty")));

                            salesReturnDeliveryDataModel.setReturnValue(value);
                            salesReturnDeliveryDataModel.setReason(cursor.getString(cursor.getColumnIndex("reasonDesc")));
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
}
