package com.ivy.ui.announcement.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.announcement.view.AnnouncementFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {AnnouncementModel.class})
public interface AnnouncementComponent {

    void inject(AnnouncementFragment announcementFragment);
}
