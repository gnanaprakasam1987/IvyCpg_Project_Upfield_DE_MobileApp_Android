package com.ivy.ui.reports.beginstockreport.data;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;


import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class BeginningReportManagerImpl implements BeginningReportManager {

    public Observable<ArrayList<StockReportMasterBO>> downloadBeginningStock(final Context context) {

        return Observable.create(new ObservableOnSubscribe<ArrayList<StockReportMasterBO>>() {
            @Override
            public void subscribe(final ObservableEmitter<ArrayList<StockReportMasterBO>> subscriber) throws Exception {

                ArrayList<StockReportMasterBO> beginningStockReport = new ArrayList<>();
                try {
                    StockReportMasterBO stock;
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
                            stock = new StockReportMasterBO();
                            stock.setProductId(c.getInt(0));
                            stock.setCaseQuantity(c.getInt(1));
                            stock.setPieceQuantity(c.getInt(2));
                            stock.setProductName(c.getString(3));
                            stock.setProductShortName(c.getString(4));
                            stock.setMrp(c.getFloat(5));
                            stock.setCaseSize(c.getInt(6));
                            stock.setUid(c.getString(7));
                            stock.setOuterQty(c.getInt(8));
                            stock.setOuterSize(c.getInt(9));
                            stock.setTotalQty((c.getInt(1) * c.getInt(6)) + c.getInt(2)
                                    + (c.getInt(8) * c.getInt(9)));

                            stock.setBasePrice(c.getFloat(10));
                            stock.setLoadNO(c.getString(11));
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
