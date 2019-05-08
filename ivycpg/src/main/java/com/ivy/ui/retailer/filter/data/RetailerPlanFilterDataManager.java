package com.ivy.ui.retailer.filter.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

import io.reactivex.Single;

public interface RetailerPlanFilterDataManager extends AppDataManagerContract {

    Observable<HashMap<String,AttributeBO>> prepareAttributeList();

    Observable<ArrayList<String>> prepareConfigurationMaster();

    Single<ArrayList<String>> getFilterValues(RetailerPlanFilterBo planFilterBo);
}
