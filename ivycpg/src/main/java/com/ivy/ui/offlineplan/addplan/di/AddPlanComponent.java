package com.ivy.ui.offlineplan.addplan.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.addplan.view.AddPlanDialogFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {AddPlanModule.class})
public interface AddPlanComponent {
    void inject(AddPlanDialogFragment planDialogFragment);
}
