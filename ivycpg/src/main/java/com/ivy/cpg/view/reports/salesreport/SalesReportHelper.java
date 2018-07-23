package com.ivy.cpg.view.reports.salesreport;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

public class SalesReportHelper {


    private void getSalesReturnReportHelper(Context context) {

        try {

            BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String query = "select * from SalesReturnHeader where date =" + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + "AND  RetailerID = " + AppUtils.QT(businessModel.getRetailerMasterBO().getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
