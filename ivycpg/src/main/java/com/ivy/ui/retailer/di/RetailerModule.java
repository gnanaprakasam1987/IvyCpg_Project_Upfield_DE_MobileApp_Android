package com.ivy.ui.retailer.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.data.RetailerDataManager;
import com.ivy.ui.retailer.data.RetailerDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;


@Module
public class RetailerModule {

    private RetailerContract.RetailerView mView;
    private Context mContext;

    public RetailerModule(RetailerContract.RetailerView mView, Context context) {
        this.mView = mView;
        this.mContext = context;
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
