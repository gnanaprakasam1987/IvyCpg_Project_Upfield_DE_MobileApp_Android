package com.ivy.cpg.view.reports.damageReturn;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import java.util.ArrayList;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by ivyuser on 2/8/18.
 */

public class DamageReturenReportHelper {
    private static DamageReturenReportHelper instance = null;

    private DamageReturenReportHelper() {
    }

    public static DamageReturenReportHelper getInstance() {
        if (instance == null) {
            instance = new DamageReturenReportHelper();
        }
        return instance;
    }


    public Observable<ArrayList<PandingDeliveryBO>> downloadPendingDeliveryReport(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<PandingDeliveryBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<PandingDeliveryBO>> subscribe) throws Exception {
                PandingDeliveryBO pandingDeliveryBO;
                ArrayList<PandingDeliveryBO> pandingDeliveryBOS = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    Cursor c = db
                            .selectSQL("Select ID.InvoiceNo, ID.InvoiceDate, ID.Retailerid,ID.invNetamount, from InvoiceDeliveryMaster ID");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            pandingDeliveryBO = new PandingDeliveryBO();
                            pandingDeliveryBO.setInvoiceNo(c.getString(0));
                            pandingDeliveryBO.setInvoiceDate(c.getString(1));
                            pandingDeliveryBO.setRetailerid(c.getString(2));
                            pandingDeliveryBO.setInvNetamount(c.getString(3));
                            pandingDeliveryBO.setUpload(c.getString(4));
                            pandingDeliveryBOS.add(pandingDeliveryBO);
                        }
                        c.close();
                    }
                    subscribe.onNext(pandingDeliveryBOS);
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

}
