package com.ivy.ui.offlineplan;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.offlineplan.map.data.MapViewDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class OfflineBasePresenterImpl<V extends OfflinePlanBaseContract.OfflinePlanBaseView> extends BasePresenter<V> implements OfflinePlanBaseContract.OfflinePlanBasePresenter<V> {

    private MapViewDataManager mapViewDataManager;
    private AppDataProvider appDataProvider;

    public OfflineBasePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }

    @Inject
    OfflineBasePresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view,
                             MapViewDataManager mapViewDataManager,
                             AppDataProvider appDataProvider) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mapViewDataManager = mapViewDataManager;
        this.appDataProvider = appDataProvider;
    }

    @Override
    public void loadAllStoresData() {
        getIvyView().showAllStores(appDataProvider.getRetailerMasters());
    }

    @Override
    public void loadTodayVisitData() {

    }
}
