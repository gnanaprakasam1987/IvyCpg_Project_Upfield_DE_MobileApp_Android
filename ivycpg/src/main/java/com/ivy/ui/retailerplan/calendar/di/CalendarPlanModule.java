package com.ivy.ui.retailerplan.calendar.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManagerImpl;
import com.ivy.ui.retailerplan.calendar.CalendarPlanContract;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManager;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManagerImpl;
import com.ivy.ui.retailerplan.calendar.presenter.CalendarPlanPresenterImpl;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManager;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManagerImpl;
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
    CalendarPlanContract.CalendarPlanPresenter<CalendarPlanContract.CalendarPlanView> providesCalendarPlanPresenter(CalendarPlanPresenterImpl<CalendarPlanContract.CalendarPlanView> calendarPlanPresenter) {
        return calendarPlanPresenter;
    }

    @Provides
    CalendarPlanDataManager proivdesOfflineDataManger(CalendarPlanDataManagerImpl calendarPlanDataManager) {
        return calendarPlanDataManager;
    }

    @Provides
    RetailerDataManager retailerDataManager(RetailerDataManagerImpl retailerDataManagerImpl){
        return retailerDataManagerImpl;
    }

    @Provides
    AddPlanDataManager addPlanDataManager(AddPlanDataManagerImpl addPlanDataManagerImpl){
        return addPlanDataManagerImpl;
    }
    @Provides
    com.ivy.core.data.retailer.RetailerDataManager coreRetailerDataManager(com.ivy.core.data.retailer.RetailerDataManagerImpl retailerDataManagerImpl){
        return retailerDataManagerImpl;
    }

}


