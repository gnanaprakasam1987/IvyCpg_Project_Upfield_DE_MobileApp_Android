package com.ivy.appmodule;

import android.app.Application;
import android.content.Context;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by velraj.p on 5/24/2018.
 */

@Module
public class AppModule {
    private final BusinessModel application;

    public AppModule(BusinessModel application) {
        this.application = application;
    }

    @Provides
    @Singleton
    BusinessModel provideApplication() {
        return application;
    }

    @Provides
    @ApplicationContext
    protected Context providesContext() {
        return application;
    }

    @Singleton
    @Provides
    protected AppDataProvider providesAppData(AppDataProviderImpl appDataProvider){
        return appDataProvider;
    }
}
