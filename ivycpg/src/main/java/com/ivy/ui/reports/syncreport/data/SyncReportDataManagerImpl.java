package com.ivy.ui.reports.syncreport.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SyncReportDataManagerImpl implements SyncReportDataManager {

    private DBUtil mDbUtil;

    @Inject
    public SyncReportDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        mDbUtil = dbUtil;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    @Override
    public Observable<HashMap<String, ArrayList<SyncReportBO>>> fetchSyncReport() {
        return Observable.fromCallable(new Callable<HashMap<String, ArrayList<SyncReportBO>>>() {
            @Override
            public HashMap<String, ArrayList<SyncReportBO>> call() {
                try {
                    initDb();
                    HashMap<String, ArrayList<SyncReportBO>> syncReportMap = new HashMap<>();
                    Cursor cursor = mDbUtil.selectSQL("select SDAS.tid,SDAS.apiname,SDTS.TableName,SDAS.starttime,SDAS.endtime,SDTS.LineCount " +
                            "from SyncDownloadApiStatus SDAS inner join SyncDownloadTableStatus SDTS on SDAS.TableName = SDTS.TableName order by SDTS.TableName");
                    if (cursor != null) {
                        ArrayList<SyncReportBO> reportList = new ArrayList<>();
                        String apiname = "";
                        while (cursor.moveToNext()) {
                            SyncReportBO reportBO = new SyncReportBO();
                            reportBO.setApiname(cursor.getString(1));
                            reportBO.setTablename(cursor.getString(2));
                            reportBO.setStartTime(cursor.getString(3));
                            reportBO.setEndTime(cursor.getString(4));
                            reportBO.setRecordCount(cursor.getInt(5));

                            if (!StringUtils.isEmptyString(apiname) && !apiname.equals(cursor.getString(1))) {
                                syncReportMap.put(apiname, reportList);
                                reportList = new ArrayList<>();
                            }
                            reportList.add(reportBO);
                            apiname = cursor.getString(1);

                            if (cursor.isLast())
                                syncReportMap.put(apiname, reportList);
                        }
                    }
                    shutDownDb();
                    return syncReportMap;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new HashMap<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<SyncReportBO>> fetchSyncUploadReport() {
        return Observable.fromCallable(new Callable<ArrayList<SyncReportBO>>() {
            @Override
            public ArrayList<SyncReportBO> call() {
                try {
                    initDb();
                    ArrayList<SyncReportBO> dataList = new ArrayList<>();
                    Cursor cursor = mDbUtil.selectSQL("select synctype,starttime,endtime,totalcount from synclogdetails where synctype !='DOWNLOAD'");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            SyncReportBO reportBO = new SyncReportBO();
                            reportBO.setApiname(cursor.getString(0));
                            reportBO.setStartTime(cursor.getString(1));
                            reportBO.setEndTime(cursor.getString(2));
                            reportBO.setRecordCount(cursor.getInt(3));
                            dataList.add(reportBO);
                        }
                    }
                    shutDownDb();
                    return dataList;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
