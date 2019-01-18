package com.ivy.cpg.view.reports.sfreport;

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
 * Created by ivyuser on 6/8/18.
 */

public class SalesFGReportHelper {

    private final Context mContext;

    public SalesFGReportHelper(Context context) {
        this.mContext = context;
    }


    public Observable<ArrayList<SalesFundamentalGapReportBO>> downloadSFGreport(final int BeatID, final String filter) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<SalesFundamentalGapReportBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<SalesFundamentalGapReportBO>> subscribe) throws Exception {
                ArrayList<SalesFundamentalGapReportBO> salesFundamentalGapReportBOArrayList = null;
                DBUtil db = null;
                try {
                    SalesFundamentalGapReportBO salesFundamentalGapReportBO;
                    db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();

                    String tableName = "", pm = "";
                    switch (filter) {
                        case "SOS":
                            pm = "SOSGap";
                            tableName = "SOS_Tracking_Detail";
                            break;
                        case "SOD":
                            pm = "SODGap";
                            tableName = "SOD_Tracking_Detail";
                            break;
                        case "SOSKU":
                            pm = "SOSKUGap";
                            tableName = "SOSKU_Tracking_Detail";
                            break;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("Select A.PName, ifnull(sum(0+B.Gap),0) as Gap, ifnull(sum(0+C." + pm + "),0) as PM from ProductMaster A ");
                    sb.append("left join " + tableName + " B on B.Pid = A.Pid ");
                    sb.append("left join SFGapReportMaster C on C.Brandid = A.Pid ");
                    sb.append("left join RetailerMaster D on B.RetailerID = D.RetailerID and C.RetailerID = D.RetailerID ");
                    sb.append("where A.PLid  = (Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)) and D.BeatID ='" + BeatID + "' ");
                    sb.append("group by A.PName order by A.Pname");

                    Cursor c = db
                            .selectSQL(sb.toString());
                    if (c != null) {
                        salesFundamentalGapReportBOArrayList = new ArrayList<>();
                        while (c.moveToNext()) {
                            salesFundamentalGapReportBO = new SalesFundamentalGapReportBO();
                            salesFundamentalGapReportBO.setPName(c.getString(0));
                            salesFundamentalGapReportBO.setGap(c.getString(1));
                            salesFundamentalGapReportBO.setPM(c.getString(2));
                            salesFundamentalGapReportBOArrayList.add(salesFundamentalGapReportBO);
                        }
                        c.close();
                    }

                    subscribe.onNext(salesFundamentalGapReportBOArrayList);
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
