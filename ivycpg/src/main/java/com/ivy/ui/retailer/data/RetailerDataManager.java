package com.ivy.ui.retailer.data;

import com.ivy.core.data.AppDataManagerContract;

import io.reactivex.Single;

public interface RetailerDataManager extends AppDataManagerContract {

    Single<String> getRoutePath(String url);
}
