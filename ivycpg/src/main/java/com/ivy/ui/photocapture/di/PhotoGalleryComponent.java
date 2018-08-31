package com.ivy.ui.photocapture.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.photocapture.view.PhotoGalleryActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {PhotoGalleryModule.class})
public interface PhotoGalleryComponent {
    void inject(PhotoGalleryActivity photoGalleryActivity);
}
