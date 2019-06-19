package com.ivy.ui.reports.syncreport;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;

import java.util.ArrayList;
import java.util.HashMap;

public interface SyncReportContract {

    interface SyncReportView extends BaseIvyView {

        void setApiDownloadDetails(HashMap<String, ArrayList<SyncReportBO>> downloadApiDetailsMap, ArrayList<SyncReportBO> apiList);

        void showDataNotMappedMsg();
    }

    @PerActivity
    interface SyncReportPresenter<V extends SyncReportView> extends BaseIvyPresenter<V> {

        void fetchData();

    }
}
