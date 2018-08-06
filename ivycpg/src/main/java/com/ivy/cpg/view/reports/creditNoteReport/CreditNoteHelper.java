package com.ivy.cpg.view.reports.creditNoteReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by Hanifa on 24/7/18.
 */

public class CreditNoteHelper {
    private static CreditNoteHelper instance;

    protected CreditNoteHelper() {
    }

    public static CreditNoteHelper getInstance() {
        if (instance == null) {
            instance = new CreditNoteHelper();
        }
        return instance;
    }

    public ArrayList<CreditNoteListBO> loadCreditNote(Context context) {
        ArrayList<CreditNoteListBO> creditNoteList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT CN.id,CN.amount,RM.RetailerName,isused FROM CreditNote CN INNER JOIN RetailerMaster RM ON RM.retailerid=CN.retailerid");
            if (c != null) {
                while (c.moveToNext()) {
                    CreditNoteListBO obj = new CreditNoteListBO();
                    obj.setId(c.getString(0));
                    obj.setAmount(c.getDouble(1));
                    obj.setRetailerName(c.getString(2));

                    boolean flag;
                    flag = c.getInt(3) == 1;
                    if (flag)
                        obj.setUsed(true);
                    else
                        obj.setUsed(false);

                    creditNoteList.add(obj);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e + " ");
        }
        return creditNoteList;
    }
}
