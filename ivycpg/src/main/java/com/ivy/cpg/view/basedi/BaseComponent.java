package com.ivy.cpg.view.basedi;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.asset.assetservicedi.AssetServiceModule;
import com.ivy.cpg.view.reports.ReportActivity;
import com.ivy.ui.task.view.TaskActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {BaseModule.class})
public interface BaseComponent {
void inject(ReportActivity reportActivity);
void inject(TaskActivity taskActivity);
}
