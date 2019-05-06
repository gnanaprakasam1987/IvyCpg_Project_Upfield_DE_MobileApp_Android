package com.ivy.ui.filter.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.filter.view.RetailerPlanFilterFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class,modules = {RetailerPlanFilterModule.class})
public interface RetailerPlanFilterComponent {

    void inject(RetailerPlanFilterFragment planFilterFragment);

}
