package com.ivy.ui.profile.data;


import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;


import io.reactivex.Observable;
import io.reactivex.Single;

public interface IProfileDataManager  {

    Observable<ArrayList<NewOutletBO>> getContactTitle();

    Observable<ArrayList<NewOutletBO>> getContactStatus();

    Observable<LinkedHashMap<Integer, ArrayList<LocationBO>>> getLocationListByLevId();

    Observable<HashMap<String, String>> getPreviousProfileChanges(String RetailerID);

    Observable<Vector<RetailerMasterBO>> getLinkRetailer();

    Observable<ArrayList<RetailerFlexBO>> downloadRetailerFlexValues(String type);

    Single<Boolean> checkProfileImagePath(RetailerMasterBO ret);

    void saveNearByRetailers(String id,Vector<RetailerMasterBO> NearByRetailers);

    Observable<ArrayList<String>> getNearbyRetailerIds(String RetailerID);

    Observable<HashMap<String, String>> getNearbyRetailersEditRequest(String retailerId);

    Observable<ArrayList<StandardListBO>> downloadPriorityProducts();

    Observable<ArrayList<String>> downloadPriorityProductsForRetailer(String retailerId);

    Observable<ArrayList<String>> downloadPriorityProductsForRetailerUpdate(String retailerId);

    Observable<ArrayList<Integer>> downloadCommonAttributeList();

    Observable<ArrayList<NewOutletAttributeBO>> downloadEditAttributeList(String retailerID);

    Observable<ChannelWiseAttributeList> downloadChannelWiseAttributeList();

    Observable<ArrayList<NewOutletAttributeBO>> downloadAttributeListForRetailer(String RetailerID);

    Observable<ArrayList<NewOutletAttributeBO>> downloadRetailerChildAttribute();

    Observable<ArrayList<NewOutletAttributeBO>> downloadAttributeParentList(ArrayList<NewOutletAttributeBO> attribList);

    Observable<ArrayList<NewOutletAttributeBO>> updateRetailerMasterAttribute(
            ArrayList<NewOutletAttributeBO> list,
            ArrayList<NewOutletAttributeBO> retailerAttribute,
            ArrayList<NewOutletAttributeBO> AttributeParentList);

    Single<String> generateOtpUrl();

    Single<String>checkHeaderAvailablility(String RetailerID, String currentDate);

    Single<Boolean>deleteQuery (String configCode,String RetailerID);

    Single<Boolean> insertNewRow( String configCode, String RetailerID, String mTid, String mCustomQuery);

    Single<Boolean> updateNearByRetailers( String mTid, String RetailerID,HashMap<String, String> temp);

    Single<Boolean> updateRetailerEditPriorityProducts( String mTid, String RetailerID,ArrayList<StandardListBO> selectedPrioProducts );

    Single<Boolean> updateRetailerMasterAttribute( String mTid, String RetailerID,ArrayList<NewOutletAttributeBO> tempList);

    Single<Boolean> updateRetailer(String mTid, String RetailerID,String currentDate);

    Single<Boolean> insertRetailerContactEdit(String mTid, String RetailerID,ArrayList<RetailerContactBo> retailerContactList);

    void closeDB();


}
