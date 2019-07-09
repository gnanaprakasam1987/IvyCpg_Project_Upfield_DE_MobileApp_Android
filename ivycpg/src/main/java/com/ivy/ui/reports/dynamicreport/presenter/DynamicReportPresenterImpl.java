package com.ivy.ui.reports.dynamicreport.presenter;


import androidx.lifecycle.LifecycleObserver;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.reports.dynamicreport.DynamicReportContract;
import com.ivy.ui.reports.dynamicreport.data.DynamicReportDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

public class DynamicReportPresenterImpl<V extends DynamicReportContract.DynamicReportView> extends BasePresenter<V> implements DynamicReportContract.DynamicReportPresenter<V>, LifecycleObserver {

    private DynamicReportDataManager dynamicReportDataManager;

    private ArrayList<String> headerList = new ArrayList<>();
    private HashMap<String, HashMap<String, String>> displayList = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, String>>> valueMap = new HashMap<>();

    @Inject
    public DynamicReportPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, V view,
                                      DynamicReportDataManager dynamicReportDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.dynamicReportDataManager = dynamicReportDataManager;
    }

    @Override
    public void fetchData(String menucode, String rid) {
        getIvyView().showLoading();
        getCompositeDisposable().add(Observable.zip(dynamicReportDataManager.fetchDisplayFields(menucode),
                dynamicReportDataManager.fetchReportData(rid),
                dynamicReportDataManager.fetchReportHeader(menucode),
                new Function3<HashMap<String, HashMap<String, String>>, HashMap<String, HashMap<String, HashMap<String, String>>>, ArrayList<String>, Object>() {
                    @Override
                    public Boolean apply(HashMap<String, HashMap<String, String>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> tabList) {

                        headerList.clear();
                        headerList.addAll(tabList);

                        displayList.clear();
                        displayList.putAll(fieldList);

                        valueMap.clear();
                        valueMap.putAll(dataMap);


                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {

                        if (!headerList.isEmpty() && !displayList.isEmpty() && !valueMap.isEmpty()) {
                            getIvyView().setReportData(displayList, valueMap, headerList);
                        } else {
                            getIvyView().showDataNotMappedMsg();
                        }

                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError(R.string.something_went_wrong);
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

}
