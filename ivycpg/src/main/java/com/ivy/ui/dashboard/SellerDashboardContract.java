package com.ivy.ui.dashboard;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.photocapture.PhotoCaptureContract;

import java.util.ArrayList;

public interface SellerDashboardContract {

    interface SellerDashboardView extends BaseIvyView {

        void updateDashSpinner(ArrayList<String> dashList);

        void setupMultiSelectDistributorSpinner(ArrayList<DistributorMasterBO> distributorMasterBOS);

        void setUpMultiSelectUserSpinner(ArrayList<UserMasterBO> userMasterBOS);

        void setDashboardListAdapter();

    }


    interface SellerDashboardPresenter<V extends SellerDashboardContract.SellerDashboardView> extends BaseIvyPresenter<V> {

        void saveModuleCompletion(String menuCode);

        void updateTimeStampModuleWise();

        void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType);

        boolean isSMPBasedDash();

        boolean isUserBasedDash();

        boolean isDistributorBasedDash();

        boolean isNiveaBasedDash();

        boolean shouldShowTrendChart();

        void getP3MSellerDashboardData();

        void fetchDistributorList();

        void fetchUserList(String userId);
    }
}
