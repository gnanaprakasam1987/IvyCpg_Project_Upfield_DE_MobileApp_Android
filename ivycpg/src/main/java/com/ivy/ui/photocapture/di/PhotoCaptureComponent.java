package com.ivy.ui.photocapture.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.photocapture.view.PhotoCaptureActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {PhotoCaptureModule.class})
public interface PhotoCaptureComponent {

    void inject(PhotoCaptureActivity photoCaptureActivity);
}
