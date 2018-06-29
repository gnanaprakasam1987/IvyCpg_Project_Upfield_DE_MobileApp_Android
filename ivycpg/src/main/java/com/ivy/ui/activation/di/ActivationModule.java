package com.ivy.ui.activation.di;


import com.ivy.core.di.module.ActivityModule;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.ui.activation.data.ActivationDataManagerImpl;
import com.ivy.ui.activation.presenter.ActivationPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class ActivationModule {

    private ActivationContract.ActivationView mView;

    public ActivationModule(ActivationContract.ActivationView mView) {
        this.mView = mView;
    }


    @Provides
    public ActivationContract.ActivationView provideView() {
        return mView;
    }

    @Provides
    ActivationDataManager providesActivationDataManager() {
        return new ActivationDataManagerImpl();
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
    ActivationContract.ActivationPresenter<ActivationContract.ActivationView> providesActivationPresenter(ActivationPresenterImpl<ActivationContract.ActivationView> activationPresenter) {
        return activationPresenter;
    }

}
