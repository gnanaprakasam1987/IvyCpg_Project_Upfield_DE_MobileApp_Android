package com.ivy.ui.retailer.viewretailers.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailer.viewretailers.RetailerContract;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManager;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;


@Module
public class RetailerModule {

    private RetailerContract.RetailerView mView;

    public RetailerModule(RetailerContract.RetailerView mView) {
        this.mView = mView;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    public RetailerContract.RetailerView provideView() {
        return mView;
    }

    @Provides
    @PerActivity
    RetailerContract.RetailerPresenter<RetailerContract.RetailerView> providesRetailerPresenter(RetailerContract.RetailerPresenter<RetailerContract.RetailerView> retailerPresenter) {
        return retailerPresenter;
    }

    @Provides
    RetailerDataManager retailerDataManager(RetailerDataManagerImpl retailerDataManagerImpl){
        return retailerDataManagerImpl;
    }
}
