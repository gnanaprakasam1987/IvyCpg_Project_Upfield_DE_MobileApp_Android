package com.ivy.cpg.view.reports.questionReport;

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
 * Created by Hanifa on 31/7/18.
 */

public class QuestionReportHelper {

    private static QuestionReportHelper instance = null;

    protected QuestionReportHelper() {
    }

    public static QuestionReportHelper getInstance() {
        if (instance == null) {
            instance = new QuestionReportHelper();
        }
        return instance;
    }


    public Observable<ArrayList<QuestionReportBO>> loadQuestionReports(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<QuestionReportBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<QuestionReportBO>> subscribe) throws Exception {
                DBUtil db = null;
                QuestionReportBO chkReportBo;
                ArrayList<QuestionReportBO> questionReport = new ArrayList<>();
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
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
                    subscribe.onNext(questionReport);
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
