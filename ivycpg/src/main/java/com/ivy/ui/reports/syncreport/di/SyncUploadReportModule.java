package com.ivy.ui.reports.syncreport.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.SyncUploadReportContract;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManager;
import com.ivy.ui.reports.syncreport.data.SyncReportDataManagerImpl;
import com.ivy.ui.reports.syncreport.presenter.SyncUploadReportPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class SyncUploadReportModule {

    private SyncUploadReportContract.SyncUploadReportView mView;

    public SyncUploadReportModule(SyncUploadReportContract.SyncUploadReportView mView) {
        this.mView = mView;
    }

    @Provides
    public SyncUploadReportContract.SyncUploadReportView provideView() {
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
    SyncUploadReportContract.SyncUploadReportPresenter<SyncUploadReportContract.SyncUploadReportView> providesSyncReportPresenter(SyncUploadReportPresenterImpl<SyncUploadReportContract.SyncUploadReportView> syncReportPresenter) {
        return syncReportPresenter;
    }
}
