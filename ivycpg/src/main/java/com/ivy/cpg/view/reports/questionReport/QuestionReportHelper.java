package com.ivy.cpg.view.reports.questionReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Hanifa on 31/7/18.
 */

public class QuestionReportHelper {

    private Context mContext;

    public QuestionReportHelper(Context context){
        mContext =context;
    }

    public ArrayList<QuestionReportBO> loadQuestionReport() {
        QuestionReportBO chkReportBo;
        ArrayList<QuestionReportBO> questionReport = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Text, V1,V2,V3,V4 FROM L3SuperwiserAuditReport ");
            if (c != null) {

                while (c.moveToNext()) {
                    chkReportBo = new QuestionReportBO();
                    chkReportBo.setText(c.getString(0));
                    chkReportBo.setV1(c.getInt(1));
                    chkReportBo.setV2(c.getInt(2));
                    chkReportBo.setV3(c.getInt(3));
                    chkReportBo.setV4(c.getInt(4));
                    questionReport.add(chkReportBo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return questionReport;
    }
}
