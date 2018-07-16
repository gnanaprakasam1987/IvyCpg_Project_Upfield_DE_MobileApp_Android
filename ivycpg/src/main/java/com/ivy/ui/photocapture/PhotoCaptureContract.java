package com.ivy.ui.photocapture;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface PhotoCaptureContract {

    interface PhotoCaptureView extends BaseIvyView {

    }


    @PerActivity
    interface PhotoCapturePresenter<V extends PhotoCaptureView> extends BaseIvyPresenter<V> {

        boolean isMaxPhotoLimitReached();
    }

}
