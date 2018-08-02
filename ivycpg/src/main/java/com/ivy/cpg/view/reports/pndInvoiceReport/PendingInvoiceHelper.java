package com.ivy.cpg.view.reports.pndInvoiceReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Hanifa on 24/7/18.
 */

public class PendingInvoiceHelper {
    private Context mContext;
    private static PendingInvoiceHelper instance;

    protected PendingInvoiceHelper() {

    }

    public static PendingInvoiceHelper getInstance() {
        if (instance == null)
            instance = new PendingInvoiceHelper();
        return instance;
    }


    //Pending invoice report

    public Observable<ArrayList<PndInvoiceReportBo>> downloadPndInvoice(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<PndInvoiceReportBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<PndInvoiceReportBo>> subscribe) throws Exception {
                DBUtil db = null;
                ArrayList<PndInvoiceReportBo> pndInvoiceReportArrayList = new ArrayList<>();
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();

                    StringBuffer sb = new StringBuffer();
                    sb.append("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt,");
                    sb.append(" Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+Inv.paidAmount,2) as RcvdAmt,");
                    sb.append(" Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os,");
                    sb.append(" RM.RetailerName as RetailerName");
                    sb.append(" FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo");
                    sb.append(" LEFT OUTER JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid");
                    sb.append(" INNER JOIN RetailerMaster RM ON inv.Retailerid = RM.RetailerID");
                    sb.append(" GROUP BY Inv.InvoiceNo,inv.Retailerid");
                    sb.append(" ORDER BY Inv.InvoiceDate");

                    Cursor c = db.selectSQL(sb.toString());
                    if (c != null) {
                        PndInvoiceReportBo pndInvoiceReportBo;

                        while (c.moveToNext()) {
                            pndInvoiceReportBo = new PndInvoiceReportBo();
                            pndInvoiceReportBo.setInvoiceNo(c.getString(0));
                            pndInvoiceReportBo.setInvoiceDate(c.getString(1));
                            pndInvoiceReportBo.setInvoiceAmount(c.getDouble(2));
                            pndInvoiceReportBo.setPaidAmount(c.getDouble(3));
                            pndInvoiceReportBo.setBalance(c.getDouble(4));
                            pndInvoiceReportBo.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));

                        }
                        c.close();
                    }
                    subscribe.onNext(pndInvoiceReportArrayList);
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
