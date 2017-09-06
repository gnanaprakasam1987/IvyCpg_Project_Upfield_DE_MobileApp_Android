package com.ivy.cpg.primarysale.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.primarysale.bo.DistInvoiceDetailsBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

/**
 * Created by dharmapriya.k on 22-09-2015.
 */
public class DisInvoiceDetailsHelper {
    private static DisInvoiceDetailsHelper instance = null;
    private Vector<DistInvoiceDetailsBO> selected_distributors_invoice;
    private Context context;


    /**
     * download the distributors list
     *
     * @return
     */

    public DisInvoiceDetailsHelper(Context context) {
        this.context = context;

    }

    public static DisInvoiceDetailsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DisInvoiceDetailsHelper(context);
        }
        return instance;
    }

    public void downloadInvoiceDetails(String DistId) {
        selected_distributors_invoice = new Vector<>();

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "select * from "
                    + DataMembers.tbl_DistInvoiceDetails + " where DistId = " + DistId
                    + " AND Upload != 'Y' ";

            Cursor c = db.selectSQL(sql);

            DistInvoiceDetailsBO con;
            if (c != null) {
                while (c.moveToNext()) {//InvoiceId,DistId,Value,LPC,Date,StatusId,Upload
                    con = new DistInvoiceDetailsBO();//InvoiceId,DistId,value,lpc,Date,Upload,Status
                    con.setInvoiceId(c.getString(c.getColumnIndex("InvoiceId")));
                    con.setDistId(c.getString(c.getColumnIndex("DistId")));
                    con.setValue(c.getString(c.getColumnIndex("Value")));
                    con.setLpc(c.getString(c.getColumnIndex("LPC")));
                    con.setDate(c.getString(c.getColumnIndex("Date")));
                    con.setStatus(c.getString(c.getColumnIndex("StatusLovId")));
                    con.setUpload(c.getString(c.getColumnIndex("Upload")));
                    selected_distributors_invoice.add(con);

                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }


    }

    public Vector<DistInvoiceDetailsBO> getInvoices() {
        return selected_distributors_invoice;
    }

    public void saveInvoiceDetails(Vector<DistInvoiceDetailsBO> disinvdetails) {
        String sql = "";
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            for (int i = 0; i < disinvdetails.size(); i++) {
                sql = "Update " + DataMembers.tbl_DistInvoiceDetails + " set StatusLovId = '"
                        + disinvdetails.get(i).getStatus() + "', Upload = '" + disinvdetails.get(i).getUpload() + "' Where DistId = '" + disinvdetails.get(i).getDistId()
                        + "' and InvoiceId = '" + disinvdetails.get(i).getInvoiceId() + "'";
                db.updateSQL(sql);


            }
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }

    }

    public boolean hasStatus() {
        for (DistInvoiceDetailsBO distInvoiceDetailsBO : selected_distributors_invoice) {

            if (!distInvoiceDetailsBO.getStatus().equals("0")) {
                return true;
            }
        }
        return false;
    }
}
