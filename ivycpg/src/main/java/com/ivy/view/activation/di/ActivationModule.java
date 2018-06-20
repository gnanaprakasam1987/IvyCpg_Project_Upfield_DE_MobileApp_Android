package com.ivy.view.activation.di;


import com.ivy.core.di.scope.PerActivity;
import com.ivy.view.activation.ActivationContract;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivationModule {

    @Provides
    @PerActivity
    ActivationContract.ActivationPresenter<ActivationContract.ActivationView> providesActivationPresenter(ActivationContract.ActivationPresenter<ActivationContract.ActivationView> activationPresenter) {
        return activationPresenter;
    }

}
