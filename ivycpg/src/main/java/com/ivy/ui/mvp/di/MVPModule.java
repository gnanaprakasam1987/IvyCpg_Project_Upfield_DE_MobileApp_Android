package com.ivy.ui.mvp.di;

import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.dashboard.data.SellerDashboardDataManager;
import com.ivy.ui.dashboard.data.SellerDashboardDataManagerImpl;
import com.ivy.ui.mvp.MVPContractor;
import com.ivy.ui.mvp.data.MVPDataManager;
import com.ivy.ui.mvp.data.MVPDataManagerImpl;
import com.ivy.ui.mvp.presenter.MVPPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class MVPModule {

    private MVPContractor.MVPView mvpView;

    public MVPModule(MVPContractor.MVPView mvpView){ this.mvpView = mvpView;}

    @Provides
    MVPContractor.MVPView provideView() { return mvpView; }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    MVPDataManager mvpDataManager(MVPDataManagerImpl mvpDataManagerImpl) {
        return mvpDataManagerImpl;
    }

    @Provides
    SellerDashboardDataManager providesSellerDashboardDataManager(SellerDashboardDataManagerImpl sellerDashboardDataManagerImpl){
        return sellerDashboardDataManagerImpl;
    }

    @Provides
    UserDataManager providesUserDataManager(UserDataManagerImpl userDataManagerImpl){
        return userDataManagerImpl;
    }

    @Provides
    @PerActivity
    MVPContractor.MVPPresenter<MVPContractor.MVPView> provideMVPPresenter(MVPPresenterImpl<MVPContractor.MVPView> mvpPresenter) {
        return mvpPresenter;
    }
}
