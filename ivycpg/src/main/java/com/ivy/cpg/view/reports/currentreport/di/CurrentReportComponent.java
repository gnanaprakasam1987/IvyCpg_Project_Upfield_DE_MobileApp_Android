package com.ivy.cpg.view.reports.currentreport.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.reports.currentreport.view.CurrentReportViewFragment;

import dagger.Component;
@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {CurrentReportModule.class})
public interface CurrentReportComponent {
    void inject(CurrentReportViewFragment currentReportViewFragment);
}
