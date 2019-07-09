package com.ivy.ui.profile.create.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.create.view.NewOutletFragmentNew;


import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {NewRetailerCreationModule.class})
public interface NewRetailerComponent {
    void inject(NewOutletFragmentNew newOutletFragmentNew);
}
