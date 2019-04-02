package com.ivy.ui.offlineplan.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.OfflinePlanContract;
import com.ivy.ui.offlineplan.data.OfflinePlanDataManager;
import com.ivy.ui.offlineplan.data.OfflinePlanDataManagerImpl;
import com.ivy.ui.offlineplan.presenter.OfflinePlanPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by mansoor on 27/03/2019
 */

@Module
public class OfflinePlanModule {

    private OfflinePlanContract.OfflinePlanView mView;
    private Context mContext;

    public OfflinePlanModule(OfflinePlanContract.OfflinePlanView mView, Context context) {
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
    public OfflinePlanContract.OfflinePlanView provideView() {
        return mView;
    }

    @Provides
    @PerActivity
    OfflinePlanContract.OfflinePlanPresenter<OfflinePlanContract.OfflinePlanView> providesOfflinePlanPresenter(OfflinePlanPresenterImpl<OfflinePlanContract.OfflinePlanView> offlinePlanPresenter) {
        return offlinePlanPresenter;
    }

    @Provides
    OfflinePlanDataManager proivdesOfflineDataManger(OfflinePlanDataManagerImpl offlinePlanDataManager) {
        return offlinePlanDataManager;
    }
}


