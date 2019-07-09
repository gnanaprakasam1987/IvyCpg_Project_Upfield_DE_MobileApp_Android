package com.ivy.cpg.view.denomination;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

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
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
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

    public Observable<ArrayList<DenominationUpdateBO>> downloadDenomintionSavedData(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<DenominationUpdateBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DenominationUpdateBO>> subscribe) throws Exception {

                ArrayList<DenominationUpdateBO> denominationUpdateBOArrayList = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    Cursor cursor = db.selectSQL("Select * from DenominationDetails");

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            DenominationUpdateBO denominationUpdateBO = new DenominationUpdateBO();
                            denominationUpdateBO.setUid(cursor.getString(0));
                            denominationUpdateBO.setValue(cursor.getString(1));
                            denominationUpdateBO.setCount(cursor.getString(2));
                            denominationUpdateBO.setLineAmount(cursor.getString(3));
                            denominationUpdateBO.setIsCoin(cursor.getInt(4));
                            denominationUpdateBOArrayList.add(denominationUpdateBO);
                        }
                        cursor.close();
                    }
                    subscribe.onNext(denominationUpdateBOArrayList);
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
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
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
                    Commons.printException(e);
                }
                return initialTotalAmount;
            }
        });
    }

    public Single<Boolean> insertDenomination(final Context context, final ArrayList<DenominationBO> denominationInputValues,
                                              final String initialTotalAmount) {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean insertDenomination = false;
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME);
                    db.openDataBase();
                    db.deleteSQL("DenominationDetails", "", true);
                    db.deleteSQL("DenominationHeader", "", true);

                    String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                    String id = "DEN" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);


                    for (int i = 0; i < denominationInputValues.size(); i++) {

                        double lineAmount = Double.valueOf(denominationInputValues.get(i).getDenominationDisplayNameValues()) * Double.valueOf(denominationInputValues.get(i).getCount());

                        String columns = "uid,value,count,lineAmount,isCoin";

                        String values = StringUtils.getStringQueryParam(id) + ","
                                + StringUtils.getStringQueryParam(denominationInputValues.get(i).getDenominationDisplayNameValues()) + ","
                                + StringUtils.getStringQueryParam(denominationInputValues.get(i).getCount()) + ","
                                + StringUtils.getStringQueryParam(String.valueOf(lineAmount)) + ","
                                + denominationInputValues.get(i).getIsCoin();

                        db.insertSQL("DenominationDetails", columns, values);

                        insertDenomination = true;
                    }

                    if (insertDenomination) {

                        String columns = "uid,date,amount";

                        String values = StringUtils.getStringQueryParam(id) + "," + StringUtils.getStringQueryParam(currentDate) + "," + StringUtils.getStringQueryParam(initialTotalAmount);

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
