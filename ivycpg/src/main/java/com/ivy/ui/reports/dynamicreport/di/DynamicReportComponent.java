package com.ivy.ui.reports.dynamicreport.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.dynamicreport.view.DynamicReportFragmentNew;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {DynamicReportModule.class})
public interface DynamicReportComponent {
    void inject(DynamicReportFragmentNew dynamicReportFragmentNew);
}
