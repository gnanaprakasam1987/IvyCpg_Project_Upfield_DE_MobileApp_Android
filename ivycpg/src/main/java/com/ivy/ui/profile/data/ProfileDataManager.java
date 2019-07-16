package com.ivy.ui.profile.data;


import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.CensusLocationBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.PaymentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ProfileDataManager {

    Observable<ArrayList<ConfigureBO>> getProfileConfigs(int channelId,
                                                         boolean isChannelSelectionNewRetailer, String language);

    Observable<ArrayList<String>> downloadNearbyRetailers(String retailerId);

    Observable<ArrayList<StandardListBO>> downloadClassType();

    Observable<ArrayList<StandardListBO>> getTaxType();

    Observable<ArrayList<String>> getRetailerBySupplierId(String suppilerID);

    Observable<ArrayList<PaymentType>> getRetailerType();

    Observable<ArrayList<NewOutletBO>> getNewRetailers();

    Observable<Vector<NewOutletBO>> getImageTypeList();

    Observable<ArrayList<ContactTitle>> getContactTitle();

    Observable<ArrayList<ContractStatus>> getContactStatus();

    Observable<LinkedHashMap<Integer, ArrayList<LocationBO>>> getLocationListByLevId();

    Observable<HashMap<String, String>> getPreviousProfileChanges(String RetailerID);

    Observable<Vector<RetailerMasterBO>> getLinkRetailer();

    Observable<ArrayList<RetailerMasterBO>> getLinkRetailerForADistributor(int distId);

    Observable<ArrayList<RetailerFlexBO>> downloadRetailerFlexValues(String type);

    Single<Boolean> checkProfileImagePath(RetailerMasterBO ret);

    Single<Boolean> saveNearByRetailers(String id, ArrayList<String> NearByRetailers);

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

    Observable<Vector<ConfigureBO>> downloadNewOutletConfiguration();

    Observable<ArrayList<NewOutletAttributeBO>> updateRetailerMasterAttribute(
            ArrayList<NewOutletAttributeBO> list,
            ArrayList<NewOutletAttributeBO> retailerAttribute,
            ArrayList<NewOutletAttributeBO> AttributeParentList);

    Single<String> generateOtpUrl();

    Single<String> checkHeaderAvailablility(String RetailerID, String currentDate);

    Single<Boolean> updateRetailer(String mTid, String RetailerID, String currentDate);

    Single<Boolean> deleteNewRetailer(String getId);

    Single<String> uploadNewOutlet(String retailerId, String deviceId, BusinessModel businessModel, AppDataProvider appDataProvider);

    Single<String> syncNewOutlet(String retailerId);

    Single<Boolean> checkRetailerAlreadyAvailable(String retailerName, String pincode);

    Observable<ArrayList<CensusLocationBO>> fetchCensusLocationDetails();

    Single<Boolean> saveNewOutletImages(String uId,NewOutletBO newOutletBO);

    Single<Boolean> deleteNewRetailerTablesForEdit(String uId,String retailerId);

    Single<Boolean> saveNewOutlet(String uId,NewOutletBO newOutletBO);

    Single<Boolean> saveNewOutletContactInformation(String uId,NewOutletBO newOutletBO);

    Single<Boolean> saveNewOutletContactTabInformation(String uId,ArrayList<RetailerContactBo> retailerContactBos);

    Single<Boolean> saveNewOutletAddressInformation(String uId,NewOutletBO newOutletBO);

    Single<Boolean> savePriorityProducts(String uId,ArrayList<StandardListBO> productIdList);

    Single<Boolean> saveOrderDetails(String uId, OrderHeader orderHeader, ArrayList<ProductMasterBO> productMasterBOS);

    Single<Boolean> saveOpportunityDetails(String uId, ArrayList<ProductMasterBO> productMasterBOS);

    Single<Boolean> setupSurveyScreenData(String id);

    Single<Boolean> setUpOpportunityProductsData();

    Single<Boolean> setUpOrderScreen();

    Single<Boolean> clearExistingOrder();

    Single<Boolean> saveEditProfileField(HashMap<String,?> retailerProfileField,String tid);

    Single<Boolean> saveEditContactData(ArrayList<RetailerContactBo> contactList,String tid);

    Single<Boolean> saveEditAttributeData(ArrayList<AttributeBO> attributeList, String tid);

    void closeDB();


}
