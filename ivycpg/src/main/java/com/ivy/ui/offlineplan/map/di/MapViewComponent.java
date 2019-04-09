package com.ivy.ui.offlineplan.map.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.map.view.PlanningMapViewFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {MapViewModule.class})
public interface MapViewComponent {
    void inject(PlanningMapViewFragment planningMapViewFragment);
}