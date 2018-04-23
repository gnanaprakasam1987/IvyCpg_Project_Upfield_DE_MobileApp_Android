package com.ivy.cpg.view.dashboard.olddashboard;

import com.ivy.cpg.view.dashboard.DashBoardBO;

import java.util.List;

public interface DashboardContractor {

    interface DashboardView{
        void gridListDataLoad(int position);
        void updateDashboardList(List<DashBoardBO> dashBoardList);
    }

    interface DashboardPresenter{
        void setView(DashboardView view);
        void loadDownloadMethods(String retailerId, String type);
        void updateProductiveAndPlanedCall();
        void computeDashboardList(String type, String subFilter);
    }
}

