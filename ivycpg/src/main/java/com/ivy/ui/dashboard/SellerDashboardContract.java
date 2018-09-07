package com.ivy.ui.dashboard;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.ui.photocapture.PhotoCaptureContract;

import java.util.ArrayList;

public interface SellerDashboardContract {

    interface SellerDashboardView extends BaseIvyView {

        void updateDashSpinner(ArrayList<String> dashList);

    }


    interface SellerDashboardPresenter<V extends SellerDashboardContract.SellerDashboardView> extends BaseIvyPresenter<V> {

        void saveModuleCompletion(String menuCode);

        void updateTimeStampModuleWise();

        void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType);

        boolean isSMPBasedDash();

        boolean shouldShowTrendChart();
    }
}
