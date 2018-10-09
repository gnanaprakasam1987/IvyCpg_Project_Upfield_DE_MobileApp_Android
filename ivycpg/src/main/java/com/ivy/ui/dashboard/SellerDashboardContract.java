package com.ivy.ui.dashboard;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.photocapture.PhotoCaptureContract;

import java.util.ArrayList;
import java.util.HashMap;

public interface SellerDashboardContract {

    interface SellerDashboardView extends BaseIvyView {

        void updateDashSpinner(ArrayList<String> dashList);

        void setupMultiSelectDistributorSpinner(ArrayList<DistributorMasterBO> distributorMasterBOS);

        void setUpDistributorSpinner(ArrayList<DistributorMasterBO> distributorMasterBOS);

        void setUpUserSpinner(ArrayList<UserMasterBO> userMasterBOS);

        void setUpMultiSelectUserSpinner(ArrayList<UserMasterBO> userMasterBOS);

        void setDashboardListAdapter(ArrayList<DashBoardBO> dashBoardBOS);

        void setUpMonthSpinner(ArrayList<String> monthList);

        void setWeekSpinner(ArrayList<String> weekList, int currentWeek);

    }


    interface SellerDashboardPresenter<V extends SellerDashboardContract.SellerDashboardView> extends BaseIvyPresenter<V> {

        void saveModuleCompletion(String menuCode);

        void updateTimeStampModuleWise();

        void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType);

        void fetchSellerDashboardDataForUser(int selectedUser);

        void fetchSellerDashboardDataForWeek(int selectedUser);

        void fetchSellerDashboardForUserAndInterval(int selectedUser, String interval);

        void fetchRouteDashboardData(String interval);

        void fetchRetailerDashboard(String interval);

        void fetchKpiMonths(boolean isFromRetailer);

        void fetchWeeks();

        boolean isSMPBasedDash();

        boolean isUserBasedDash();

        boolean isDistributorBasedDash();

        boolean isNiveaBasedDash();

        boolean shouldShowTrendChart();

        void fetchP3MSellerDashboardData();

        void fetchDistributorList(boolean isMultiSelect);

        void fetchUserList(String userId, boolean isMultiSelect);

        void fetchKPIDashboardData(String userid, String interval);

        HashMap<String, String> getLabelsMap();

        ArrayList<DashBoardBO> getDashboardListData();

        UserMasterBO getCurrentUser();

    }
}
