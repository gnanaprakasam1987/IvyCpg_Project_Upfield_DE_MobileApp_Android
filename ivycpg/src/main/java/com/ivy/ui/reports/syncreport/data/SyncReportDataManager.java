package com.ivy.ui.reports.syncreport.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

public interface SyncReportDataManager extends AppDataManagerContract {

    Observable<HashMap<String, ArrayList<SyncReportBO>>> fetchSyncReport();

    Observable<ArrayList<SyncReportBO>> fetchSyncUploadReport();
}
