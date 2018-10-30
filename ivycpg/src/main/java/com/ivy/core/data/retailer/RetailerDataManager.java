package com.ivy.core.data.retailer;

import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface RetailerDataManager {


    Observable<ArrayList<RetailerMasterBO>> fetchRetailers();

    Observable<Boolean> updateRouteConfig();

    Observable<ArrayList<IndicativeBO>> fetchIndicativeRetailers();


}
