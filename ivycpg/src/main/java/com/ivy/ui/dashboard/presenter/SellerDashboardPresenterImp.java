package com.ivy.ui.dashboard.presenter;

import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.data.SellerDashboardDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class SellerDashboardPresenterImp<V extends SellerDashboardContract.SellerDashboardView> extends BasePresenter<V> implements SellerDashboardContract.SellerDashboardPresenter<V>, LifecycleObserver {

    private OutletTimeStampDataManager mOutletTimeStampDataManager;
    private SellerDashboardDataManager sellerDashboardDataManager;

    @Inject
    public SellerDashboardPresenterImp(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                       ConfigurationMasterHelper configurationMasterHelper, V view, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager,
                                       SellerDashboardDataManager sellerDashboardDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.sellerDashboardDataManager =sellerDashboardDataManager;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }

    @Override
    public void saveModuleCompletion(String menuCode) {
        getCompositeDisposable().add(getDataManager().saveModuleCompletion(menuCode)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean value) {
                    }
                }));
    }

    @Override
    public void updateTimeStampModuleWise() {
        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                }));
    }

    @Override
    public void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType) {
        getCompositeDisposable().add(sellerDashboardDataManager.getDashList(dashBoardType)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> dashList) {
                        getIvyView().updateDashSpinner(dashList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public boolean isSMPBasedDash() {
        return getConfigurationMasterHelper().IS_SMP_BASED_DASH;
    }

    @Override
    public boolean shouldShowTrendChart() {
        return false;
    }
}
