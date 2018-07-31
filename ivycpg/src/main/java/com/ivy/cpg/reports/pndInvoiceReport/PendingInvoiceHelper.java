package com.ivy.cpg.reports.pndInvoiceReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by Hanifa on 24/7/18.
 */

public class PendingInvoiceHelper {
    private Context mContext;
    private BusinessModel bModel;
    private static PendingInvoiceHelper instance;

    protected PendingInvoiceHelper(Context context) {
        mContext = context;
        bModel = (BusinessModel) context;
    }

    public static PendingInvoiceHelper getInstance(Context context) {
        if (instance == null)
            instance = new PendingInvoiceHelper(context);
        return instance;
    }


    //Pending invoice report

    public void downloadInvoice() {
        try {
            /*bModel.configurationMasterHelper.loadInvoiceMasterDueDateAndDateConfig();*/
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
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
                ArrayList<PndInvoiceReportBo> pndInvoiceReportArrayList = new ArrayList<>();
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
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }


}
