package com.ivy.ui.profile.data;


import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;


import io.reactivex.Observable;
import io.reactivex.Single;

public interface IProfileDataManager {

    Observable<ArrayList<NewOutletBO>> getContactTitle();

    Observable<ArrayList<NewOutletBO>> getContactStatus();

    Observable<LinkedHashMap<Integer, ArrayList<LocationBO>>> getLocationListByLevId();

    Observable<HashMap<String, String>> getPreviousProfileChanges(String RetailerID);

    Observable<Vector<RetailerMasterBO>> downloadLinkRetailer();

    Observable<ArrayList<RetailerFlexBO>> downloadRetailerFlexValues(String type);

    Single<Boolean> checkProfileImagePath(RetailerMasterBO ret);

    void saveNearByRetailers(String id,Vector<RetailerMasterBO> NearByRetailers);

    Observable<ArrayList<String>> getNearbyRetailerIds(String RetailerID);

    Observable<HashMap<String, String>> getNearbyRetailersEditRequest(String retailerId);

    Observable<ArrayList<StandardListBO>> downloadPriorityProducts();

    Observable<ArrayList<String>> downloadPriorityProductsForRetailer(String retailerId);

}
