package com.ivy.ui.mvp.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.dashboard.di.SellerDashboardModule;
import com.ivy.ui.mvp.view.MVPFragmentNew;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {MVPModule.class})
public interface MVPComponent {

    void inject(MVPFragmentNew mvpFragment);
}
