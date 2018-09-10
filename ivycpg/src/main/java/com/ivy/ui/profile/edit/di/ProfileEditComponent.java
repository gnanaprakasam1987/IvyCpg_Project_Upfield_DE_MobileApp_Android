package com.ivy.ui.profile.edit.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;


import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ProfileEditModule.class})
public interface ProfileEditComponent {
    void inject(ProfileEditFragmentNew profileEditFragment);
}
