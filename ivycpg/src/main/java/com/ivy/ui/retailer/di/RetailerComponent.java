package com.ivy.ui.retailer.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailer.view.map.RetailerMapFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {RetailerModule.class})
public interface RetailerComponent {
    void inject(RetailerMapFragment retailerMapFragment);
}
