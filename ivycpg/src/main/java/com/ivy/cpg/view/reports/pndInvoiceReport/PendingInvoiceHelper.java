package com.ivy.cpg.view.reports.pndInvoiceReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

                BusinessModel businessModel = (BusinessModel) context;
                businessModel.configurationMasterHelper.loadInvoiceMasterDueDateAndDateConfig();
                DBUtil db = null;
                ArrayList<PndInvoiceReportBo> invoiceHeader = new ArrayList<>();
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();

                    StringBuffer sb = new StringBuffer();

                    sb.append("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt,");
                    sb.append(" Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+Inv.paidAmount,2) as RcvdAmt,");
                    sb.append(" Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os,");
                    sb.append(" payment.ChequeNumber,payment.ChequeDate,Round(Inv.discountedAmount,2),sum(PD.discountvalue),RM.RetailerName as RetailerName,IFNULL(RM.creditPeriod,'') as creditPeriod,DueDays,DueDate");
                    sb.append(" FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo");
                    sb.append(" LEFT OUTER JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid");
                    sb.append(" INNER JOIN RetailerMaster RM ON inv.Retailerid = RM.RetailerID");
                    sb.append(" GROUP BY Inv.InvoiceNo,inv.Retailerid");
                    sb.append(" ORDER BY Inv.InvoiceDate");

                    Cursor c = db.selectSQL(sb.toString());
                    if (c != null) {
                        PndInvoiceReportBo invocieHeaderBO;
                        while (c.moveToNext()) {
                            invocieHeaderBO = new PndInvoiceReportBo();
                            invocieHeaderBO.setInvoiceNo(c.getString(0));
                            invocieHeaderBO.setInvoiceDate(c.getString(1));
                            invocieHeaderBO.setInvoiceAmount(c.getDouble(2));
                            invocieHeaderBO.setPaidAmount(c.getDouble(3));
                            invocieHeaderBO.setBalance(c.getDouble(4));
                            invocieHeaderBO.setAppliedDiscountAmount(c.getDouble(8));
                            invocieHeaderBO.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));

                            int count = DateTimeUtils.getDateCount(invocieHeaderBO.getInvoiceDate(),
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
                            final double discountpercentage = CollectionHelper.getInstance(context).getDiscountSlabPercent(count + 1);

                            double remaingAmount = (invocieHeaderBO.getInvoiceAmount() - (invocieHeaderBO.getAppliedDiscountAmount() + invocieHeaderBO.getPaidAmount())) * discountpercentage / 100;
                            if (businessModel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                                remaingAmount = SDUtil.convertToDouble(SDUtil.format(remaingAmount,
                                        0,
                                        0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
                            }

                            invocieHeaderBO.setRemainingDiscountAmt(remaingAmount);
                            if (businessModel.configurationMasterHelper.COMPUTE_DUE_DATE) {
                                int crediiDays = c.getInt(c.getColumnIndex("creditPeriod"));

                                if (crediiDays != 0) {

                                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                                    Date date = format.parse(invocieHeaderBO.getInvoiceDate());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    calendar.add(Calendar.DAY_OF_YEAR, crediiDays);
                                    Date dueDate = format.parse(format.format(calendar.getTime()));

                                    invocieHeaderBO.setDueDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                                            dueDate, businessModel.configurationMasterHelper.outDateFormat));

                                }
                            } else {
                                invocieHeaderBO.setDueDate(c.getString(c.getColumnIndex("DueDate")));
                            }
                            if (!businessModel.configurationMasterHelper.COMPUTE_DUE_DAYS)
                                invocieHeaderBO.setDueDays(c.getString(c.getColumnIndex("DueDays")));
                            if (invocieHeaderBO.getBalance() > 0)
                                invoiceHeader.add(invocieHeaderBO);
                        }
                        c.close();
                    }
                    db.closeDB();
                    subscribe.onNext(invoiceHeader);
                    subscribe.onComplete();
                } catch (Exception e) {
                    subscribe.onError(e);
                    subscribe.onComplete();
                    Commons.printException(e);
                }

            }
        });
    }

}
