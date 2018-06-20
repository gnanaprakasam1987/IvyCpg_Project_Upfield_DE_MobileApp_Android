package com.ivy.core.di.component;

import android.app.Application;
import android.content.Context;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.module.ActivityModule;
import com.ivy.core.di.module.IvyAppModule;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {IvyAppModule.class, ActivityModule.class})
public interface IvyAppComponent {


    @ApplicationContext
    Context context();

    DataManager dataManager();

    void inject(BusinessModel businessModel);

    void inject(BaseFragment baseFragment);

}
