package com.ivy.ui.reports.dynamicreport.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.dynamicreport.DynamicReportContract;
import com.ivy.ui.reports.dynamicreport.data.DynamicReportDataManager;
import com.ivy.ui.reports.dynamicreport.data.DynamicReportDataManagerImpl;
import com.ivy.ui.reports.dynamicreport.presenter.DynamicReportPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class DynamicReportModule {

    private DynamicReportContract.DynamicReportView mView;

    public DynamicReportModule(DynamicReportContract.DynamicReportView view) {
        this.mView = view;
    }

    @Provides
    public DynamicReportContract.DynamicReportView provideView() {
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
    DynamicReportDataManager providesPhotoCaptureDataManager(DynamicReportDataManagerImpl dynamicReportDataManager) {
        return dynamicReportDataManager;
    }

    @Provides
    @PerActivity
    DynamicReportContract.DynamicReportPresenter<DynamicReportContract.DynamicReportView> providesDynamicReportPresenter(DynamicReportPresenterImpl<DynamicReportContract.DynamicReportView> dynamicReportPresenter) {
        return dynamicReportPresenter;
    }
}
