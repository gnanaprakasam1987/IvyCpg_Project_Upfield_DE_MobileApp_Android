package com.ivy.ui.reports.syncreport.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.view.SyncReportDownloadFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {SyncReportModule.class})
public interface SyncReportComponent {

    void inject(SyncReportDownloadFragment syncReportFragment);
}
