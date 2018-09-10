package com.ivy.ui.dashboard.data;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.ui.dashboard.SellerDashboardConstants;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface SellerDashboardDataManager {

    Observable<ArrayList<String>> getDashList(SellerDashboardConstants.DashBoardType dashBoardType);

    Observable<ArrayList<DashBoardBO>> getP3MSellerDashboardData(String userId);

    Observable<ArrayList<DashBoardBO>> getSellerDashboardForWeek(String userId);

    Observable<ArrayList<DashBoardBO>> getRouteDashboardForInterval(String interval);

    Observable<ArrayList<DashBoardBO>> getSellerDashboardForInterval(String userId,String interval);

    Observable<ArrayList<DashBoardBO>> getRetailerDashboardForInterval(String retailerId,String interval);

}
