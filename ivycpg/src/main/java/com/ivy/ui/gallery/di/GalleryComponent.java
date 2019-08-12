package com.ivy.ui.gallery.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.gallery.view.GalleryFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {GalleryModule.class})
public interface GalleryComponent {

    void inject(GalleryFragment galleryFragment);
}
