package com.ivy.ui.reports.syncreport.presenter;

import android.arch.lifecycle.LifecycleObserver;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.reports.syncreport.SyncReportContract;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManager;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class SyncReportPresenterImpl<V extends SyncReportContract.SyncReportView> extends BasePresenter<V> implements SyncReportContract.SyncReportPresenter<V>, LifecycleObserver {

    private SyncReportDataManager syncReportDataManager;

    @Inject
    public SyncReportPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, SyncReportDataManager syncReportDataManager,
                                   ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.syncReportDataManager = syncReportDataManager;

    }

    @Override
    public void fetchData() {

        getIvyView().showLoading();
        getCompositeDisposable().add(syncReportDataManager.fetchSyncReport()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, ArrayList<SyncReportBO>>>() {
                    @Override
                    public void accept(HashMap<String, ArrayList<SyncReportBO>> dataMap) throws Exception {
                        if (!dataMap.isEmpty()) {
                            ArrayList<SyncReportBO> apiList = new ArrayList<>();
                            for (String key : dataMap.keySet()) {
                                SyncReportBO reportBO = dataMap.get(key).get(0);
                                apiList.add(reportBO);
                            }
                            if (!apiList.isEmpty()) {
                                Collections.sort(apiList);
                                getIvyView().setApiDownloadDetails(dataMap, apiList);
                            } else {
                                getIvyView().showDataNotMappedMsg();
                            }
                        } else {
                            getIvyView().showDataNotMappedMsg();
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }
}
