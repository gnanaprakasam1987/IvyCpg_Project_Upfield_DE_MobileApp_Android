package com.ivy.ui.retailerplan.calendar.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplan.calendar.view.CalendarPlanFragment;

import dagger.Component;

/**
 * Created by mansoor on 27/03/2019
 */
@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {CalendarPlanModule.class})
public interface CalendarPlanComponent {
    void inject(CalendarPlanFragment calendarPlanFragment);
}
