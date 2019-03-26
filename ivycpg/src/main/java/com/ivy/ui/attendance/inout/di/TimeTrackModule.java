package com.ivy.ui.attendance.inout.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.location.LocationUtil;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.data.TimeTrackDataManager;
import com.ivy.ui.attendance.inout.data.TimeTrackDataMangerImpl;
import com.ivy.ui.attendance.inout.presenter.TimeTrackPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by mansoor on 28/12/2018
 */

@Module
public class TimeTrackModule {

    private TimeTrackingContract.TimeTrackingView mView;
    private Context mContext;

    public TimeTrackModule(TimeTrackingContract.TimeTrackingView mView, Context context) {
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
    public TimeTrackingContract.TimeTrackingView provideView() {
        return mView;
    }

    @Provides
    @PerActivity
    TimeTrackingContract.TimeTrackingPresenter<TimeTrackingContract.TimeTrackingView> providesPhotoCapturePresenter(TimeTrackPresenterImpl<TimeTrackingContract.TimeTrackingView> timeTrackPresenter) {
        return timeTrackPresenter;
    }

    @Provides
    TimeTrackDataManager providesTimeTrackDataManager(TimeTrackDataMangerImpl timeTrackDataManger) {
        return timeTrackDataManger;
    }

    @Provides
    LocationUtil providesLocationUtil() {
        return LocationUtil.getInstance(mContext);
    }


}
