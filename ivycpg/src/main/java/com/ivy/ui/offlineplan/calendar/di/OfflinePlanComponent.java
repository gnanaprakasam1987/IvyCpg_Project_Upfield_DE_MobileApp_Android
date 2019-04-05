package com.ivy.ui.offlineplan.calendar.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.calendar.view.OfflinePlanFragment;

import dagger.Component;

/**
 * Created by mansoor on 27/03/2019
 */
@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {OfflinePlanModule.class})
public interface OfflinePlanComponent {
    void inject(OfflinePlanFragment offlinePlanFragment);
}
