package com.ivy.ui.reports.syncreport;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;

import java.util.ArrayList;

public interface SyncUploadReportContract {

    interface SyncUploadReportView extends BaseIvyView {

        void setData(ArrayList<SyncReportBO> dataList);

        void showDataNotMappedMsg();
    }

    @PerActivity
    interface SyncUploadReportPresenter<V extends SyncUploadReportView> extends BaseIvyPresenter<V> {

        void fetchData();

    }
}
