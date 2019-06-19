package com.ivy.ui.reports.syncreport.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.SyncReportContract;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManager;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManagerImpl;
import com.ivy.ui.reports.syncreport.presenter.SyncReportPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class SyncReportModule {

    private SyncReportContract.SyncReportView mView;

    public SyncReportModule(SyncReportContract.SyncReportView mView) {
        this.mView = mView;
    }

    @Provides
    public SyncReportContract.SyncReportView provideView() {
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
    SyncReportDataManager providesPhotoCaptureDataManager(SyncReportDataManagerImpl syncReportDataManager) {
        return syncReportDataManager;
    }

    @Provides
    @PerActivity
    SyncReportContract.SyncReportPresenter<SyncReportContract.SyncReportView> providesSyncReportPresenter(SyncReportPresenterImpl<SyncReportContract.SyncReportView> syncReportPresenter) {
        return syncReportPresenter;
    }
}
