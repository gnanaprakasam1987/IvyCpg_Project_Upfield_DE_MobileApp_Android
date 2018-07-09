package com.ivy.ui.reports.beginstockreport.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.reports.beginstockreport.view.BeginningStockFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {BeginningReportModule.class})
public interface BeginningReportComponent {
    void inject(BeginningStockFragment beginningStockFragment);

}
