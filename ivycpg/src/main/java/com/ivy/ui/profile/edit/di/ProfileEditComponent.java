package com.ivy.ui.profile.edit.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.view.profile.ProfileEditFragment;


import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ProfileEditModule.class})
public interface ProfileEditComponent {
    void inject(ProfileEditFragment profileEditFragment);
}
