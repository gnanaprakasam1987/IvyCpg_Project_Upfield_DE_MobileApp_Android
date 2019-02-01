package com.ivy.cpg.view.reports.asset;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * Created by abbas.a on 17/07/18.
 */

public class AssetTrackingReportsHelper {

    private Context mContext;

    public AssetTrackingReportsHelper(Context context) {
        mContext = context;
    }


    public Observable<ArrayList<RetailerNamesBO>> downloadAssetTrackingRetailerMaster() {
        return Observable.fromCallable(new Callable<ArrayList<RetailerNamesBO>>() {
            @Override
            public ArrayList<RetailerNamesBO> call() throws Exception {
                ArrayList<RetailerNamesBO> assetRetailerList = new ArrayList<>();
                DBUtil db = null;
                try {
                    RetailerNamesBO orderreport;
                    db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Select distinct A.RetailerID,B.RetailerName from SOD_Assets_Detail A inner join RetailerMaster B on A.RetailerID = B.RetailerID");

                    Cursor c = db
                            .selectSQL(sb.toString());
                    if (c != null) {
                        assetRetailerList = new ArrayList<>();
                        while (c.moveToNext()) {
                            orderreport = new RetailerNamesBO();
                            orderreport.setRetailerId(c.getInt(0));
                            orderreport.setRetailerName(c.getString(1));
                            assetRetailerList.add(orderreport);
                        }
                        c.close();
                    }

                    return assetRetailerList;

                } catch (Exception e) {
                    Commons.printException(e);
                } finally {
                    if (db != null)
                        db.closeDB();
                }

                return new ArrayList<>();
            }
        });
    }


    public Observable<ArrayList<AssetTrackingBrandBO>> downloadAssetTrackingBrandMaster() {
        return Observable.fromCallable(new Callable<ArrayList<AssetTrackingBrandBO>>() {
            @Override
            public ArrayList<AssetTrackingBrandBO> call() throws Exception {
                ArrayList<AssetTrackingBrandBO> assetBrandList = new ArrayList<>();
                DBUtil db = null;
                try {
                    AssetTrackingBrandBO orderreport;
                    db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Select distinct A.ProductID,B.PName from SOD_Assets_Detail A " +
                            "inner join ProductMaster B on A.ProductID = B.PID");

                    Cursor c = db
                            .selectSQL(sb.toString());
                    if (c != null) {

                        while (c.moveToNext()) {
                            orderreport = new AssetTrackingBrandBO();
                            orderreport.setBrandID(c.getInt(0));
                            orderreport.setBrandName(c.getString(1));
                            assetBrandList.add(orderreport);
                        }
                        c.close();
                    }
                    return assetBrandList;
                } catch (Exception e) {
                    Commons.printException(e);
                } finally {
                    if (db != null)
                        db.closeDB();
                }

                return new ArrayList<>();
            }
        });
    }


    public Observable<ArrayList<AssetTrackingReportBO>> downloadAssetTrackingreport(final int retailerID, final int brandID) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<AssetTrackingReportBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<AssetTrackingReportBO>> subscribe) throws Exception {
                ArrayList<AssetTrackingReportBO> assetTrackingReportBOArrayList = new ArrayList<>();
                DBUtil db = null;
                try {
                    AssetTrackingReportBO assetTrackingReportBO;
                    db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Select distinct A.PosmDesc,D.PName,ifnull(B.Target,0),ifnull(B.Actual,0),ifnull(C.ListName,'') from PosmMaster A ");
                    sb.append("inner join SOD_Assets_Detail B on A.PosmID = B.AssetID ");
                    sb.append("left join StandardListMaster C on C.ListID = B.ReasonID and C.ListType = 'REASON' ");
                    sb.append("inner join ProductMaster D on B.ProductID = D.PID ");
                    sb.append("inner join RetailerMaster E on B.RetailerID = E.RetailerID ");
                    sb.append("where E.RetailerID = '" + retailerID + "' ");
                    if (brandID != 0) {
                        sb.append("and B.ProductID = '" + brandID + "'");
                    }

                    Cursor c = db
                            .selectSQL(sb.toString());
                    if (c != null) {
                        while (c.moveToNext()) {
                            assetTrackingReportBO = new AssetTrackingReportBO();
                            assetTrackingReportBO.setAssetDescription(c.getString(0));
                            assetTrackingReportBO.setBrandname(c.getString(1));
                            assetTrackingReportBO.setTarget(c.getString(2));
                            assetTrackingReportBO.setActual(c.getString(3));
                            assetTrackingReportBO.setReason(c.getString(4));
                            assetTrackingReportBOArrayList.add(assetTrackingReportBO);
                        }
                        c.close();
                    }
                    subscribe.onNext(assetTrackingReportBOArrayList);
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
