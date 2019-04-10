package com.ivy.ui.retailer.presenter;

import android.content.Context;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class RetailerPresenterImpl<V extends RetailerContract.RetailerView> extends BasePresenter<V> implements RetailerContract.RetailerPresenter<V> {

    private AppDataProvider appDataProvider;
    @Inject
    RetailerPresenterImpl(DataManager dataManager,
                          SchedulerProvider schedulerProvider,
                          CompositeDisposable compositeDisposable,
                          ConfigurationMasterHelper configurationMasterHelper,
                          V view,
                          AppDataProvider appDataProvider) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.appDataProvider = appDataProvider;
    }

    @Override
    public void fetchRetailerList() {

        getIvyView().populateRetailers(appDataProvider.getRetailerMasters());

    }

    @Override
    public void addRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void updateRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void deleteRetailerFromPlan(RetailerMasterBO retailerMasterBO) {

    }

    @Override
    public int getPixelsFromDpInt(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((39 + 20) * scale + 0.5f);
    }


}
