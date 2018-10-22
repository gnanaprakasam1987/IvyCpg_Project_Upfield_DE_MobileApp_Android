package com.ivy.ui.dashboard.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.dashboard.adapter.DashboardListAdapter;
import com.ivy.ui.dashboard.view.SellerDashboardFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {SellerDashboardModule.class})
public interface SellerDashboardComponent {

    void inject(SellerDashboardFragment sellerDashboardFragment);

    void inject(DashboardListAdapter dashboardListAdapter);

}
