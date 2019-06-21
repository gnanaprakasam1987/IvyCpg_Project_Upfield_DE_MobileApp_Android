package com.ivy.ui.reports.syncreport.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.syncreport.view.SyncReportUploadFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {SyncUploadReportModule.class})
public interface SyncUploadReportComponent {

    void inject(SyncReportUploadFragment syncReportUploadFragment);

}
