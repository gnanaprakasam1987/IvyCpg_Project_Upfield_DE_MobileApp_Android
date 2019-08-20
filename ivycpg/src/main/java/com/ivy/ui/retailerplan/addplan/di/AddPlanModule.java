package com.ivy.ui.retailerplan.addplan.di;


import android.content.Context;

import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.core.data.retailer.RetailerDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManagerImpl;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManager;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class AddPlanModule {

    private AddPlanContract.AddPlanView mView;
    private Context context;

    public AddPlanModule(AddPlanContract.AddPlanView mView, Context context) {
        this.mView = mView;
        this.context = context;
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
    AddPlanContract.AddPlanView provideView(){
        return mView;
    }

    @Provides
    @PerActivity
    AddPlanContract.AddPlanPresenter<AddPlanContract.AddPlanView> provideAddPlanPresenter(AddPlanContract.AddPlanPresenter<AddPlanContract.AddPlanView> addPlanPresenter) {
        return addPlanPresenter;
    }

    @Provides
    AddPlanDataManager addPlanDataManager(AddPlanDataManagerImpl addPlanDataManager){
        return addPlanDataManager;
    }

    @Provides
    RetailerDataManager retailerDataManager(RetailerDataManagerImpl retailerDataManager){
        return retailerDataManager;
    }

    @Provides
    CalendarPlanDataManager providesOfflineDataManger(CalendarPlanDataManagerImpl calendarPlanDataManager) {
        return calendarPlanDataManager;
    }

    @Provides
    com.ivy.ui.retailer.viewretailers.data.RetailerDataManager planRetailerDataManager(com.ivy.ui.retailer.viewretailers.data.RetailerDataManagerImpl planRetailerDataManger){
        return planRetailerDataManger;
    }
}
