package com.ivy.ui.reports.syncreport.presenter;

import android.arch.lifecycle.LifecycleObserver;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.reports.syncreport.SyncUploadReportContract;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManager;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class SyncUploadReportPresenterImpl<V extends SyncUploadReportContract.SyncUploadReportView> extends BasePresenter<V> implements SyncUploadReportContract.SyncUploadReportPresenter<V>, LifecycleObserver {

    private SyncReportDataManager syncReportDataManager;

    @Inject
    public SyncUploadReportPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                         SyncReportDataManager syncReportDataManager,
                                         ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.syncReportDataManager = syncReportDataManager;
    }

    @Override
    public void fetchData() {

        getIvyView().showLoading();
        getCompositeDisposable().add(syncReportDataManager.fetchSyncUploadReport()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<SyncReportBO>>() {
                    @Override
                    public void accept(ArrayList<SyncReportBO> dataList) throws Exception {
                        if (!dataList.isEmpty()) {
                            getIvyView().setData(dataList);
                        } else {
                            getIvyView().showDataNotMappedMsg();
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }
}
