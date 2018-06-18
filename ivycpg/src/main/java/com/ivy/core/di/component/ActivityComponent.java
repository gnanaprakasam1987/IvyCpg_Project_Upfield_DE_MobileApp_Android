package com.ivy.core.di.component;


import com.ivy.core.di.module.ActivityModule;
import com.ivy.core.di.scope.PerActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
}
