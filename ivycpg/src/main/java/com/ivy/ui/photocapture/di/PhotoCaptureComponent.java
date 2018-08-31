package com.ivy.ui.photocapture.di;


import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.ui.photocapture.view.PhotoCaptureActivity;
import com.ivy.ui.photocapture.view.PhotoGalleryActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {PhotoCaptureModule.class})
public interface PhotoCaptureComponent {

    void inject(PhotoCaptureActivity photoCaptureActivity);
}
