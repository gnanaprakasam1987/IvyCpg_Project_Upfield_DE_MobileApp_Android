package com.ivy.ui.reports.currentreport.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.ui.reports.currentreport.data.CurrentReportManagerImpl;
import com.ivy.ui.reports.currentreport.presenter.CurrentReportPresenterImpl;
import com.ivy.ui.reports.currentreport.ICurrentReportContract;
import com.ivy.ui.reports.currentreport.data.CurrentReportManager;

import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;


import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class CurrentReportModule {

    private ICurrentReportContract.ICurrentReportView mView;
    private Context mContext;

    public CurrentReportModule(ICurrentReportContract.ICurrentReportView mView,Context context) {
        this.mView = mView;
        this.mContext = context;
    }


    @Provides
    LabelsMasterHelper providesLabelsMasterHelper() {
        return new LabelsMasterHelper(mContext);
    }

    @Provides
    UserMasterHelper provideUserMasterHelper() {
        return new UserMasterHelper(mContext);
    }

    @Provides
    public ICurrentReportContract.ICurrentReportView provideView() {
        return mView;
    }

    @Provides
    CurrentReportManager provideCurrentReportManager() {
        return new CurrentReportManagerImpl();
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
    public ReportHelper provideReportHelper(){
        return new ReportHelper(mContext);
    }

    @Provides
    @PerActivity
    ICurrentReportContract.ICurrentReportModelPresenter<ICurrentReportContract.ICurrentReportView> providesCurrentReportPresenter(CurrentReportPresenterImpl<ICurrentReportContract.ICurrentReportView> currentReportPresenter) {
        return currentReportPresenter;
    }
}
