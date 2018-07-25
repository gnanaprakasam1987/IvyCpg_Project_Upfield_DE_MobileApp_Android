package com.ivy.ui.reports.beginstockreport.data;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class BeginningReportManagerImpl implements BeginningReportManager {

    public Observable<ArrayList<BeginningStockReportBO>> downloadBeginningStock(final Context context) {

        return Observable.create(new ObservableOnSubscribe<ArrayList<BeginningStockReportBO>>() {
            @Override
            public void subscribe(final ObservableEmitter<ArrayList<BeginningStockReportBO>> subscriber) throws Exception {

                ArrayList<BeginningStockReportBO> beginningStockReport = new ArrayList<>();
                try {
                    BeginningStockReportBO stock;
                    DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();

                    String query = "select DISTINCT " +
                            "A.pid,A.caseQty,A.pcsQty,B.pname,B.psname,B.mrp," +
                            "B.dUomQty,A.uid,A.outerQty,B.dOuomQty,B.baseprice,IFNULL(A.LoadNo,A.uid) " +
                            "from VanLoad A " +
                            "inner join productmaster B on A.pid=B.pid ";
                    Cursor c = db.selectSQL(query);

                    if (c != null) {
                        beginningStockReport = new ArrayList<>();
                        while (c.moveToNext()) {
                            stock = new BeginningStockReportBO();
                            stock.setProductId(c.getInt(0));
                            stock.setCaseQuantity(c.getInt(1));
                            stock.setPcsQuantity(c.getInt(2));
                            stock.setProductName(c.getString(3));
                            stock.setProductShortName(c.getString(4));
                            stock.setCaseSize(c.getInt(6));
                            stock.setOuterQty(c.getInt(8));
                            stock.setOuterSize(c.getInt(9));
                            stock.setBasePrice(c.getFloat(10));
                            beginningStockReport.add(stock);


                        }
                        c.close();
                    }
                    db.closeDB();
                    subscriber.onNext(beginningStockReport);
                } catch (Exception e) {
                    subscriber.onError(new Throwable("SqLite Exception"));
                    Commons.printException(e);
                }
                subscriber.onComplete();
            }

        });

    }

}