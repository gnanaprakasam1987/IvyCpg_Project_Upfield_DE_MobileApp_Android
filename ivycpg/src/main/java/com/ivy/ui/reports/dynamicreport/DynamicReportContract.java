package com.ivy.ui.reports.dynamicreport;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;

import java.util.ArrayList;
import java.util.HashMap;

public interface DynamicReportContract {

    interface DynamicReportView extends BaseIvyView {
        void setReportData(HashMap<String, HashMap<String, DynamicReportBO>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> headerList);

        void showDataNotMappedMsg();
    }

    @PerActivity
    interface DynamicReportPresenter<V extends DynamicReportView> extends BaseIvyPresenter<V> {

        void fetchData(String menucode, String rid);

    }
}
