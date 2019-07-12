package com.ivy.ui.profile;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.view.ProfileBaseFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ProfileModule.class})
public interface ProfileComponent {
    void inject(ProfileBaseFragment profileBaseFragment);
}
