package com.ivy.ui.dashboard.di;

import com.ivy.core.di.scope.ActivityContext;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashPresenterImpl;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashboardContractor;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashboardFragment;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.adapter.DashboardListAdapter;
import com.ivy.ui.dashboard.data.SellerDashboardDataManager;
import com.ivy.ui.dashboard.data.SellerDashboardDataManagerImpl;
import com.ivy.ui.dashboard.presenter.SellerDashboardPresenterImp;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;


import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class SellerDashboardModule {

    private SellerDashboardContract.SellerDashboardView mView;

    public SellerDashboardModule(SellerDashboardContract.SellerDashboardView view){
        mView=view;
    }

    @Provides
    public SellerDashboardContract.SellerDashboardView provideView() {
        return mView;
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
    SellerDashboardDataManager providesSellerDashboardDataManager(SellerDashboardDataManagerImpl sellerDashboardDataManager){
        return sellerDashboardDataManager;
    }

    @Provides
    @PerActivity
    SellerDashboardContract.SellerDashboardPresenter<SellerDashboardContract.SellerDashboardView> providesSellerDashboardPresenter(SellerDashboardPresenterImp<SellerDashboardContract.SellerDashboardView> sellerDashPresenter) {
        return sellerDashPresenter;
    }



}
