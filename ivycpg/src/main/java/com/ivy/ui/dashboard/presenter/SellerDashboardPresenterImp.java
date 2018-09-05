package com.ivy.ui.dashboard.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.utils.rx.SchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

public class SellerDashboardPresenter extends BasePresenter<SellerDashboardContract.SellerDashboardView> {

    public SellerDashboardPresenter(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, SellerDashboardContract.SellerDashboardView view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }
}
