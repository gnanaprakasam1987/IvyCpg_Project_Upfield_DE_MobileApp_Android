package com.ivy.ui.reports.dynamicreport.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;
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
    public Observable<HashMap<String, HashMap<String, DynamicReportBO>>> fetchDisplayFields(String menucode) {
        return Observable.fromCallable(new Callable<HashMap<String, HashMap<String, DynamicReportBO>>>() {
            @Override
            public HashMap<String, HashMap<String, DynamicReportBO>> call() throws Exception {
                try {
                    initDb();
                    HashMap<String, HashMap<String, DynamicReportBO>> displayFields = new HashMap<>();
                    LinkedHashMap<String, DynamicReportBO> valueMap = new LinkedHashMap<>();
                    Cursor cursor = mDbUtil.selectSQL("select distinct reportname,displayname,fieldname,align,length from RawDataReportFieldDefinition where menucode=" + StringUtils.QT(menucode) + " order by reportname,fieldname");
                    if (cursor != null) {
                        String reportname = "";
                        while (cursor.moveToNext()) {
                            DynamicReportBO reportBO = new DynamicReportBO();
                            if (!reportname.isEmpty() && !reportname.equals(cursor.getString(0))) {
                                displayFields.put(reportname, valueMap);
                                valueMap = new LinkedHashMap<>();
                            }
                            reportBO.setReportName(cursor.getString(0));
                            reportBO.setDisplayName(cursor.getString(1));
                            reportBO.setFieldName(cursor.getString(2));
                            reportBO.setAlign(cursor.getString(3));
                            reportBO.setLength(cursor.getInt(4));
                            reportBO.setSelected(false);
                            reportBO.setSearched(false);
                            reportBO.setSorted(false);
                            reportBO.setSearchText("");
                            valueMap.put(cursor.getString(2), reportBO);
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
    public Observable<HashMap<String, HashMap<String, HashMap<String, String>>>> fetchReportData(String rid, String menucode) {
        return Observable.fromCallable(new Callable<HashMap<String, HashMap<String, HashMap<String, String>>>>() {
            @Override
            public HashMap<String, HashMap<String, HashMap<String, String>>> call() throws Exception {
                try {
                    initDb();
                    HashMap<String, HashMap<String, HashMap<String, String>>> dataMap = new HashMap<>();
                    HashMap<String, HashMap<String, String>> reportWiseMap = new HashMap<>();
                    String reportNames="";
                    Cursor cursor = mDbUtil.selectSQL("select distinct reportname from RawDataReportFieldDefinition where menucode=" + StringUtils.QT(menucode));
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            reportNames += StringUtils.QT(cursor.getString(0)) + ",";
                        }

                        cursor.close();
                    }

                    if (!StringUtils.isEmptyString(reportNames))
                        reportNames = reportNames.substring(0, reportNames.length()-1);

                    cursor = mDbUtil.selectSQL("select * from RawDataReportDetail where rid=" + StringUtils.QT(rid) + " and reportname in(" + reportNames + ")");
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
