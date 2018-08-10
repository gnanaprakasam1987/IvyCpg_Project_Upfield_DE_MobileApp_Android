package com.ivy.cpg.view.reports.collectionreport;



public interface ICollectionReportModelPresenter {
    void setUpAdapter();
    void loadCollectionReport();

    CollectionReportHelper getReportHelper();

}
