package com.ivy.ui.dashboard.data;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.utils.rx.Optional;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface SellerDashboardDataManager {

    Observable<List<String>> getDashList(SellerDashboardConstants.DashBoardType dashBoardType);

    Observable<List<DashBoardBO>> getP3MSellerDashboardData(String userId);

    Observable<List<DashBoardBO>> getSellerDashboardForWeek(String userId);

    Observable<List<DashBoardBO>> getRouteDashboardForInterval(String interval);

    Observable<List<DashBoardBO>> getSellerDashboardForInterval(String userId,String interval);

    Observable<List<DashBoardBO>> getRetailerDashboardForInterval(String retailerId,String interval);

    Observable<List<DashBoardBO>> getKPIDashboard(String userId, String interval);

    Observable<List<DashBoardBO>> getP3MTrendChart(String userId);

    Observable<List<Double>> getCollectedValue();

    Observable<List<String>> getKpiMonths(boolean isFromRetailer);

    Observable<List<String>> getKpiWeekList();

    Single<String> getCurrentWeekInterval();

    Single<Optional<DailyReportBO>> fetchOutletDailyReport();

    Single<Integer> fetchTotalCallsForTheDayExcludingDeviatedVisits();

    Single<Optional<DailyReportBO>> fetchNoOfInvoiceAndValue();

    Single<Optional<DailyReportBO>> fetchNoOfOrderAndValue();

    Single<Integer> getProductiveCallsForDay();

    Single<Integer> getVisitedCallsForTheDayExcludingDeviatedVisits();

    Single<Integer> getProductiveCallsForTheDayExcludingDeviatedVisits();

    Single<Double> fetchFocusBrandInvoiceAmt();

    Single<Double> fetchSalesReturnValue();

    Single<Optional<DailyReportBO>> fetchFulfilmentValue();

    Single<Integer> fetchPromotionCount();

    Single<Integer> fetchPromotionExecutedCount();

    Single<Optional<String>> fetchMslCount();

}
