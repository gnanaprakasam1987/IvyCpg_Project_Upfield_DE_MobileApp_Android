package com.ivy.ui.attendance.inout.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.attendance.inout.view.TimeTrackingFragment;

import dagger.Component;

/**
 * Created by mansoor on 28/12/2018
 */
@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {TimeTrackModule.class})
public interface TimeTrackComponent {
    void inject(TimeTrackingFragment TimeTrackingFragment);
}

