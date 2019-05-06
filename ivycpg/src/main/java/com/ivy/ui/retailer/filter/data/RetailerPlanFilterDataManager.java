package com.ivy.ui.retailer.filter.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;

import java.util.ArrayList;

import io.reactivex.Single;

public interface RetailerPlanFilterDataManager extends AppDataManagerContract {
    Single<ArrayList<String>> prepareConfigurationMaster();

    Single<ArrayList<String>> getFilterValues(RetailerPlanFilterBo planFilterBo);
}
