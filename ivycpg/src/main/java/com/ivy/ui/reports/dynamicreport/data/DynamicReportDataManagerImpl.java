package com.ivy.ui.reports.dynamicreport.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;

public class DynamicReportDataManagerImpl implements DynamicReportDataManager {

    private DBUtil mDbUtil;

    @Inject
    public DynamicReportDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
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
    public Observable<HashMap<String, HashMap<String, String>>> fetchDisplayFields(String menucode) {
        return Observable.fromCallable(new Callable<HashMap<String, HashMap<String, String>>>() {
            @Override
            public HashMap<String, HashMap<String, String>> call() throws Exception {
                try {
                    initDb();
                    HashMap<String, HashMap<String, String>> displayFields = new HashMap<>();
                    LinkedHashMap<String, String> valueMap = new LinkedHashMap<>();
                    Cursor cursor = mDbUtil.selectSQL("select distinct reportname,displayname,fieldname from RawDataReportFieldDefinition where menucode=" + StringUtils.QT(menucode) + " order by reportname,fieldname");
                    if (cursor != null) {
                        String reportname = "";
                        while (cursor.moveToNext()) {
                            if (!reportname.isEmpty() && !reportname.equals(cursor.getString(0))) {
                                displayFields.put(reportname, valueMap);
                                valueMap = new LinkedHashMap<>();
                            }
                            valueMap.put(cursor.getString(2), cursor.getString(1));
                            reportname = cursor.getString(0);

                            if (cursor.isLast()) {
                                displayFields.put(reportname, valueMap);
                            }
                        }
                        cursor.close();
                    }
                    return displayFields;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new HashMap<>();
            }
        });
    }

    @Override
    public Observable<HashMap<String, HashMap<String, HashMap<String, String>>>> fetchReportData(String rid) {
        return Observable.fromCallable(new Callable<HashMap<String, HashMap<String, HashMap<String, String>>>>() {
            @Override
            public HashMap<String, HashMap<String, HashMap<String, String>>> call() throws Exception {
                try {
                    initDb();
                    HashMap<String, HashMap<String, HashMap<String, String>>> dataMap = new HashMap<>();
                    HashMap<String, HashMap<String, String>> reportWiseMap = new HashMap<>();
                    Cursor cursor = mDbUtil.selectSQL("select * from RawDataReportDetail where rid=" + StringUtils.QT(rid));
                    if (cursor != null) {
                        int count = 0;
                        String reportname = "";
                        while (cursor.moveToNext()) {
                            if (!reportname.isEmpty() && !reportname.equals(cursor.getString(0))) {
                                dataMap.put(reportname, reportWiseMap);
                                reportWiseMap = new HashMap<>();
                                count = 0;
                            }
                            HashMap<String, String> valueMap = new HashMap<>();
                            for (int i = 1; i <= (cursor.getColumnCount() - 2); i++) {
                                String key = "Field" + i;
                                valueMap.put(key, cursor.getString(cursor.getColumnIndex(key)));
                            }
                            reportWiseMap.put(String.valueOf(count), valueMap);
                            count++;

                            reportname = cursor.getString(0);

                            if (cursor.isLast()) {
                                dataMap.put(reportname, reportWiseMap);
                            }
                        }
                        cursor.close();
                    }
                    return dataMap;
                } catch (Exception e) {
                    Commons.printException(e);
                }

                shutDownDb();
                return new HashMap<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> fetchReportHeader(String menucode) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                try {
                    initDb();
                    ArrayList<String> tabList = new ArrayList<>();
                    Cursor cursor = mDbUtil.selectSQL("select distinct reportname from RawDataReportFieldDefinition where menucode=" + StringUtils.QT(menucode) + " order by fieldname");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            tabList.add(cursor.getString(0));
                        }
                        cursor.close();
                    }
                    shutDownDb();
                    return tabList;
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
