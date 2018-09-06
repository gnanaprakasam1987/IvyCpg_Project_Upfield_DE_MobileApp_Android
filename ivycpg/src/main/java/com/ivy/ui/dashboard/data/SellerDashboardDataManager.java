package com.ivy.ui.dashboard.data;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface SellerDashboardDataManager {

    Observable<ArrayList<String>> getRouteDashList();

    Observable<ArrayList<String>> getSellerDashList();

    Observable<ArrayList<String>> getRetailerDashList();
}
