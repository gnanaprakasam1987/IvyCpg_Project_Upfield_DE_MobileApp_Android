package com.ivy.ui.offlineplan.calendar.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.calendar.CalendarPlanContract;
import com.ivy.ui.offlineplan.calendar.data.CalendarPlanDataManager;
import com.ivy.ui.offlineplan.calendar.data.CalendarPlanDataManagerImpl;
import com.ivy.ui.offlineplan.calendar.presenter.CalendarPlanPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by mansoor on 27/03/2019
 */

@Module
public class CalendarPlanModule {

    private CalendarPlanContract.CalendarPlanView mView;
    private Context mContext;

    public CalendarPlanModule(CalendarPlanContract.CalendarPlanView mView, Context context) {
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
    public CalendarPlanContract.CalendarPlanView provideView() {
        return mView;
    }

    @Provides
    @PerActivity
    CalendarPlanContract.CalendarPlanPresenter<CalendarPlanContract.CalendarPlanView> providesOfflinePlanPresenter(CalendarPlanPresenterImpl<CalendarPlanContract.CalendarPlanView> offlinePlanPresenter) {
        return offlinePlanPresenter;
    }

    @Provides
    CalendarPlanDataManager proivdesOfflineDataManger(CalendarPlanDataManagerImpl offlinePlanDataManager) {
        return offlinePlanDataManager;
    }
}


