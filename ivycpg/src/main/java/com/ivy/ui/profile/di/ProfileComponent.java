package com.ivy.ui.profile.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.view.ProfileContainerFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ProfileModule.class})
public interface ProfileComponent {
    void inject(ProfileContainerFragment profileBaseFragment);
}