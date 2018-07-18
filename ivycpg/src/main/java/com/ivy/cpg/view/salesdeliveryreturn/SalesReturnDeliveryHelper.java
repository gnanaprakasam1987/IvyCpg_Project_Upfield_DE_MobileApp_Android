package com.ivy.cpg.view.salesdeliveryreturn;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SalesReturnDeliveryHelper {

    public SalesReturnDeliveryHelper() {
    }


    public Observable<Vector<SalesReturnDeliveryDataModel>> getSaleReturnDelivery(final Context context) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataModel>> subscriber) throws Exception {

                try {
                    Vector<SalesReturnDeliveryDataModel> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
                    dbUtil.openDataBase();
                    Cursor cursor = dbUtil.selectSQL("Select * from SalesReturnHeader");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                            salesReturnDeliveryDataModel.setUId(cursor.getInt(0));
                            salesReturnDeliveryDataModel.setDate(cursor.getString(1));
                            salesReturnDeliveryDataModel.setReturnValue(cursor.getString(6));
                            salesReturnDeliveryDataModel.setLpc(cursor.getInt(7));
                            salesReturnDeliveryDataModel.setInvoiceId(cursor.getString(18));
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


    public Observable<Vector<SalesReturnDeliveryDataModel>> getSaleReturnDeliveryDetails(final Context context) {

        return Observable.create(new ObservableOnSubscribe<Vector<SalesReturnDeliveryDataModel>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<SalesReturnDeliveryDataModel>> subscriber) throws Exception {

                try {
                    Vector<SalesReturnDeliveryDataModel> returnDeliveryDataModelVector = new Vector<>();
                    DBUtil dbUtil = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
                    dbUtil.openDataBase();
                    Cursor cursor = dbUtil.selectSQL("Select * from SalesReturnHeader");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SalesReturnDeliveryDataModel salesReturnDeliveryDataModel = new SalesReturnDeliveryDataModel();
                            salesReturnDeliveryDataModel.setUId(cursor.getInt(0));
                            salesReturnDeliveryDataModel.setDate(cursor.getString(1));
                            salesReturnDeliveryDataModel.setReturnValue(cursor.getString(6));
                            salesReturnDeliveryDataModel.setLpc(cursor.getInt(7));
                            salesReturnDeliveryDataModel.setInvoiceId(cursor.getString(18));
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
