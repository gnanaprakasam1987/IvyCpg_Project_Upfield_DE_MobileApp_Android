package com.ivy.ui.dashboard.data;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.ui.dashboard.SellerDashboardConstants;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface SellerDashboardDataManager {

    Observable<ArrayList<String>> getDashList(SellerDashboardConstants.DashBoardType dashBoardType);

    Observable<ArrayList<DashBoardBO>> getP3MSellerDashboardData(String userId);

    Observable<ArrayList<DashBoardBO>> getSellerDashboardForWeek(String userId);

    Observable<ArrayList<DashBoardBO>> getRouteDashboardForInterval(String interval);

    Observable<ArrayList<DashBoardBO>> getSellerDashboardForInterval(String userId,String interval);

    Observable<ArrayList<DashBoardBO>> getRetailerDashboardForInterval(String retailerId,String interval);

    Observable<ArrayList<DashBoardBO>> getKPIDashboard(String userId, String interval);

    Observable<ArrayList<DashBoardBO>> getP3MTrendChart(String userId);

    Observable<ArrayList<Double>> getCollectedValue();

    Observable<ArrayList<String>> getKpiMonths(boolean isFromRetailer);

    Observable<ArrayList<String>> getKpiWeekList();

    Single<String> getCurrentWeekInterval();

    Single<DailyReportBO> fetchOutletDailyReport();

    Single<Integer> fetchTotalCallsForTheDayExcludingDeviatedVisits();

    Single<DailyReportBO> fetchNoOfInvoiceAndValue();

    Single<DailyReportBO> fetchNoOfOrderAndValue();

    Single<Integer> getProductiveCallsForDay();

    Single<Integer> getVisitedCallsForTheDayExcludingDeviatedVisits();

    Single<Integer> getProductiveCallsForTheDayExcludingDeviatedVisits();

    Single<Double> fetchFocusBrandInvoiceAmt();

}
