package com.ivy.ui.retailerplanfilter.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplanfilter.view.RetailerPlanFilterFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class,modules = {RetailerPlanFilterModule.class})
public interface RetailerPlanFilterComponent {

    void inject(RetailerPlanFilterFragment planFilterFragment);

}
