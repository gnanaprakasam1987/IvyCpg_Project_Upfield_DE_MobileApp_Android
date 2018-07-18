package com.ivy.ui.reports.beginstockreport.di;


import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.beginstockreport.BeginningReportContract;
import com.ivy.ui.reports.beginstockreport.data.BeginningReportManagerImpl;
import com.ivy.ui.reports.beginstockreport.presenter.BeginningReportPresenterImpl;
import com.ivy.ui.reports.beginstockreport.data.BeginningReportManager;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class BeginningReportModule {

    private BeginningReportContract.IBeginningStockView mView;

    private Context mContext;

    public BeginningReportModule(BeginningReportContract.IBeginningStockView mView, Context context) {
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
    public BeginningReportContract.IBeginningStockView provideView() {
        return mView;
    }

    @Provides
    BeginningReportManager provideBeginningReportManager() {
        return new BeginningReportManagerImpl();
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
    @PerActivity
    BeginningReportContract.IBeginningStockModelPresenter<BeginningReportContract.IBeginningStockView>
    providesBeginningReportPresenter(BeginningReportPresenterImpl<BeginningReportContract.IBeginningStockView> currentReportPresenter) {
        return currentReportPresenter;
    }

}
