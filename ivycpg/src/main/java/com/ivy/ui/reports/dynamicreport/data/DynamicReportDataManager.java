package com.ivy.ui.reports.dynamicreport.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

public interface DynamicReportDataManager extends AppDataManagerContract {

    Observable<HashMap<String, HashMap<String, DynamicReportBO>>> fetchDisplayFields(String menucode);

    Observable<HashMap<String, HashMap<String, HashMap<String, String>>>> fetchReportData(String rid, String menucode);

    Observable<ArrayList<String>> fetchReportHeader(String menucode);
}
