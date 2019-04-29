package com.ivy.ui.retailerplanfilter.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterContract;
import com.ivy.ui.retailerplanfilter.data.RetailerPlanFilterDataManager;
import com.ivy.ui.retailerplanfilter.data.RetailerPlanFilterDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class RetailerPlanFilterModule {

    private RetailerPlanFilterContract.RetailerPlanFilterView view;

    public RetailerPlanFilterModule(RetailerPlanFilterContract.RetailerPlanFilterView view){
        this.view = view;
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
    RetailerPlanFilterContract.RetailerPlanFilterView provideView(){
        return view;
    }

    @Provides
    @PerActivity
    RetailerPlanFilterContract.RetailerPlanFilterPresenter<RetailerPlanFilterContract.RetailerPlanFilterView> providePresenter(RetailerPlanFilterContract.RetailerPlanFilterPresenter<RetailerPlanFilterContract.RetailerPlanFilterView> planFilterPresenter){
        return planFilterPresenter;
    }

    @Provides
    RetailerPlanFilterDataManager retailerPlanFilterDataManager(RetailerPlanFilterDataManagerImpl planFilterDataManager){
        return planFilterDataManager;
    }

}
