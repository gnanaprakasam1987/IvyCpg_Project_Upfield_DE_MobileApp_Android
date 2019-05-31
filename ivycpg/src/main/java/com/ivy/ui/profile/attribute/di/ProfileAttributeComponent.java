package com.ivy.ui.profile.attribute.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.attribute.view.ProfileAttributeFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {ProfileAttributeModule.class})
public interface ProfileAttributeComponent {
    void inject(ProfileAttributeFragment attributeFragment);
}
