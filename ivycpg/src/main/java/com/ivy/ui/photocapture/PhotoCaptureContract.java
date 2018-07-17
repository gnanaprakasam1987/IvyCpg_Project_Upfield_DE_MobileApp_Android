package com.ivy.ui.photocapture;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;

import java.util.ArrayList;

public interface PhotoCaptureContract {

    interface PhotoCaptureView extends BaseIvyView {

        void setProductListData(ArrayList<PhotoCaptureProductBO> productListData);
    }


    @PerActivity
    interface PhotoCapturePresenter<V extends PhotoCaptureView> extends BaseIvyPresenter<V> {

        boolean isMaxPhotoLimitReached();

        void fetchPhotoCaptureProducts();

        boolean isGlobalLocation();
    }

}
