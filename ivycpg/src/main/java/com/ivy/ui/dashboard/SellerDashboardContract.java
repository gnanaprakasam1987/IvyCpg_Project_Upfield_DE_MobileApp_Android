package com.ivy.ui.dashboard;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.ui.photocapture.PhotoCaptureContract;

public interface SellerDashboardContract {

    interface SellerDashboardView extends BaseIvyView {

    }


    interface SellerDashboardPresenter<V extends SellerDashboardContract.SellerDashboardView> extends BaseIvyPresenter<V> {

    }
}
