package com.ivy.ui.retailer.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class RetailerPresenterImpl<V extends RetailerContract.RetailerView> extends BasePresenter<V> implements RetailerContract.RetailerPresenter<V> {

    private AppDataProvider appDataProvider;
    private ProfileDataManagerImpl profileDataManager;

    @Inject
    RetailerPresenterImpl(DataManager dataManager,
                          SchedulerProvider schedulerProvider,
                          CompositeDisposable compositeDisposable,
                          ConfigurationMasterHelper configurationMasterHelper,
                          V view,
                          AppDataProvider appDataProvider, ProfileDataManagerImpl profileDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.appDataProvider = appDataProvider;
        this.profileDataManager = profileDataManager;
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
    public void setRetailerMasterBo(RetailerMasterBO retailerMasterBO) {
        appDataProvider.setRetailerMaster(retailerMasterBO);
    }

    @Override
    public void fetchLinkRetailer() {
        profileDataManager.getLinkRetailer();
    }

}
