package com.ivy.ui.attendance.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.attendance.TimeTrackingContract;
import com.ivy.ui.attendance.data.TimeTrackDataManager;
import com.ivy.ui.attendance.data.TimeTrackDataMangerImpl;
import com.ivy.ui.attendance.presenter.TimeTrackPresenterImpl;
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

    public TimeTrackModule(TimeTrackingContract.TimeTrackingView mView) {
        this.mView = mView;
    }

    @Provides
    public TimeTrackingContract.TimeTrackingView provideView() {
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
    TimeTrackDataManager providesPhotoCaptureDataManager(TimeTrackDataMangerImpl timeTrackDataManger){
        return timeTrackDataManger;
    }

    @Provides
    @PerActivity
    TimeTrackingContract.TimeTrackingPresenter<TimeTrackingContract.TimeTrackingView> providesPhotoCapturePresenter(TimeTrackPresenterImpl<TimeTrackingContract.TimeTrackingView> photoCapturePresenter) {
        return photoCapturePresenter;
    }
}
