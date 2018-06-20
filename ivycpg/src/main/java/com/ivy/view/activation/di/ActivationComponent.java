package com.ivy.view.activation.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.view.activation.view.ActivationActivity;

import javax.inject.Singleton;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ActivationModule.class})
public interface ActivationComponent {

    void inject(ActivationActivity activationActivity);
}