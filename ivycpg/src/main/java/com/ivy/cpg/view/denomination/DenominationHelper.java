package com.ivy.cpg.view.denomination;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.damageReturn.PendingDeliveryBO;
import com.ivy.cpg.view.reports.deliveryStockReport.DeliveryStockBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by murugan on 2/8/18.
 */

public class DenominationHelper {
    private static DenominationHelper instance = null;

    private DenominationHelper() {
    }

    public static DenominationHelper getInstance() {
        if (instance == null) {
            instance = new DenominationHelper();
        }
        return instance;
    }


    public Observable<ArrayList<DenominationBO>> downloadDenomintionData(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<DenominationBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DenominationBO>> subscribe) throws Exception {

                ArrayList<DenominationBO> denominationBOS = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    Cursor cursor = db.selectSQL("Select * from DenominationMaster");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            DenominationBO denominationBO = new DenominationBO();
                            denominationBO.setDenomintionId(cursor.getString(0));
                            denominationBO.setDenominationDisplayName(cursor.getString(1));
                            denominationBO.setDenominationDisplayNameValues(cursor.getString(2));
                            denominationBO.setIsCoin(cursor.getInt(3));
                            denominationBOS.add(denominationBO);
                        }
                        cursor.close();
                    }
                    subscribe.onNext(denominationBOS);
                    subscribe.onComplete();
                } catch (Exception e) {
                    Commons.printException(e);
                    subscribe.onError(e);
                    subscribe.onComplete();
                } finally {
                    if (db != null)
                        db.closeDB();
                }
            }
        });
    }

    public Single<String> downLoadTotalCashInHand(final Context context) {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String initialTotalAmount = "";
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    Cursor cursor = db.selectSQL("SELECT ifnull(sum(amount),0) FROM Payment pt inner join StandardListMaster sd on sd.ListId = pt.CashMode where sd.ListCode = 'CA'");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            initialTotalAmount = cursor.getString(0);
                        }
                        cursor.close();
                    }
                    db.closeDB();
                } catch (Exception e) {
                    ;
                    Commons.printException(e);
                }
                return initialTotalAmount;
            }
        });
    }

    public Single<Boolean> insertDenomination(final Context context, final ArrayList<DenominationBO> denominationInputValues
            , final String initialTotalAmount) {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean insertDenomination = false;
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    db.deleteSQL("DenominationDetails", "", true);
                    db.deleteSQL("DenominationHeader", "", true);

                    String currentDate = SDUtil.now(SDUtil.DATE_GLOBAL);
                    String id = "DEN" + SDUtil.now(SDUtil.DATE_TIME_ID);


                    for (int i = 0; i < denominationInputValues.size(); i++) {

                        double lineAmount = Double.valueOf(denominationInputValues.get(i).getDenominationDisplayNameValues()) *
                                Double.valueOf(denominationInputValues.get(i).getCount());

                        String columns = "uid,value,count,lineAmount,isCoin";

                        String values = AppUtils.QT(id) + ","
                                + AppUtils.QT(denominationInputValues.get(i).getDenominationDisplayNameValues()) + ","
                                + AppUtils.QT(denominationInputValues.get(i).getCount()) + ","
                                + AppUtils.QT(String.valueOf(lineAmount)) + ","
                                + denominationInputValues.get(i).getIsCoin();

                        db.insertSQL("DenominationDetails", columns, values);

                        insertDenomination = true;
                    }

                    if (insertDenomination) {

                        String columns = "uid,date,amount";

                        String values = AppUtils.QT(id) + "," + AppUtils.QT(currentDate) + "," + AppUtils.QT(initialTotalAmount);

                        db.insertSQL("DenominationHeader", columns, values);
                    }

                } catch (Exception e) {

                    Commons.printException(e);
                }

                return insertDenomination;
            }
        });
    }


}
