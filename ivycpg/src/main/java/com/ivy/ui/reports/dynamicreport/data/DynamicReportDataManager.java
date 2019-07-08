package com.ivy.ui.reports.dynamicreport.data;

import com.ivy.core.data.AppDataManagerContract;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

public interface DynamicReportDataManager extends AppDataManagerContract {

    Observable<HashMap<String, HashMap<String, String>>> fetchDisplayFields(String menucode);

    Observable<HashMap<String, HashMap<String, HashMap<String, String>>>> fetchReportData(String rid);

    Observable<ArrayList<String>> fetchReportHeader(String menucode);
}
