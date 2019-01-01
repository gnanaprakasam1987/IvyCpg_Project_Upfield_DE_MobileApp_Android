package com.ivy.ui.dashboard;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.photocapture.PhotoCaptureContract;

import java.util.List;
import java.util.HashMap;

public interface SellerDashboardContract {

    interface SellerDashboardView extends BaseIvyView {

        void updateDashSpinner(List<String> dashList);

        void setupMultiSelectDistributorSpinner(List<DistributorMasterBO> distributorMasterBOS);

        void setUpDistributorSpinner(List<DistributorMasterBO> distributorMasterBOS);

        void setUpUserSpinner(List<UserMasterBO> userMasterBOS);

        void setUpMultiSelectUserSpinner(List<UserMasterBO> userMasterBOS);

        void setDashboardListAdapter(List<DashBoardBO> dashBoardBOS, boolean isFromUser);

        void setDashboardListAdapter(List<DashBoardBO> dashBoardBOS);

        void setUpMonthSpinner(List<String> monthList);

        void setWeekSpinner(List<String> weekList, int currentWeek);

        void setupRouteSpinner(List<BeatMasterBO> beatMasterBOS);

        void createP3MChartFragment(List<DashBoardBO> dashBoardBOS);
    }


    interface SellerDashboardPresenter<V extends SellerDashboardContract.SellerDashboardView> extends BaseIvyPresenter<V> {

        void fetchListRowLabels();

        void saveModuleCompletion(String menuCode);

        void updateTimeStampModuleWise();

        void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType);

        void fetchSellerDashboardDataForUser(String selectedUser, boolean isFromDash);

        void fetchSellerDashboardDataForUser(String selectedUser);

        void fetchSellerDashboardDataForWeek(String selectedUser);

        void fetchSellerDashboardForUserAndInterval(String selectedUser, String interval);

        void fetchSellerDashboardForUserAndInterval(String selectedUser, String interval, boolean isFromDash);

        void fetchRouteDashboardData(String interval);

        void fetchRetailerDashboard(String interval);

        void fetchKpiMonths(boolean isFromRetailer);

        void fetchP3mTrendChartData(String userId);

        void fetchWeeks();

        void fetchBeats();

        boolean isSMPBasedDash();

        boolean isUserBasedDash();

        boolean isDistributorBasedDash();

        boolean isNiveaBasedDash();

        boolean shouldShowTrendChart();

        boolean shouldShowP3MDash();

        boolean shouldShowSMPDash();

        boolean shouldShowInvoiceDash();

        boolean shouldShowKPIBarChart();

        void fetchP3MSellerDashboardData();

        void fetchDistributorList(boolean isMultiSelect);

        void fetchUserList(String userId, boolean isMultiSelect);

        void fetchKPIDashboardData(String userid, String interval);

        HashMap<String, String> getLabelsMap();

        List<DashBoardBO> getDashboardListData();

        UserMasterBO getCurrentUser();

        void computeDayAchievements();


    }
}
