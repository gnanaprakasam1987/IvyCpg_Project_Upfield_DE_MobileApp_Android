package com.ivy.ui.retailer.viewretailers.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailer.viewretailers.view.list.RetailerListFragment;
import com.ivy.ui.retailer.viewretailers.view.map.RetailerMapFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {RetailerModule.class})
public interface RetailerComponent {
    void inject(RetailerMapFragment retailerMapFragment);
    void inject(RetailerListFragment retailerMapFragment);
}
