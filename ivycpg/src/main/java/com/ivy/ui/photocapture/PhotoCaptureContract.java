package com.ivy.ui.photocapture;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;

import java.util.ArrayList;
import java.util.HashMap;

public interface PhotoCaptureContract {

    interface PhotoCaptureView extends BaseIvyView {

        void setProductListData(ArrayList<PhotoCaptureProductBO> productListData);

        void setPhotoTypeData(ArrayList<PhotoTypeMasterBO> photoTypeData);
    }


    @PerActivity
    interface PhotoCapturePresenter<V extends PhotoCaptureView> extends BaseIvyPresenter<V> {

        boolean isMaxPhotoLimitReached();

        void fetchPhotoCaptureProducts();

        void fetchPhotoCaptureTypes();

        void fetchLocations();

        void fetchEditedPhotoTypes();

        boolean isGlobalLocation();

        boolean isDateEnabled();

        void fetchData();

        HashMap<String,PhotoCaptureLocationBO> getEditedPhotoListData();

        int getGlobalLocationId();
    }

}
