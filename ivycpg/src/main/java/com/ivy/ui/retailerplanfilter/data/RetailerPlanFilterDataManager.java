package com.ivy.ui.retailerplanfilter.data;

import com.ivy.core.data.AppDataManagerContract;

import java.util.ArrayList;

import io.reactivex.Single;

public interface RetailerPlanFilterDataManager extends AppDataManagerContract {
    Single<ArrayList<String>> prepareConfigurationMaster();
}
