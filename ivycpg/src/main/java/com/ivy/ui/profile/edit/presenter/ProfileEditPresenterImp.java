package com.ivy.ui.profile.edit.presenter;


import android.annotation.SuppressLint;
import android.util.SparseArray;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.ui.profile.view.ProfileBaseBo;
import com.ivy.utils.AppUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;
import com.stepstone.stepper.StepperLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function5;
import io.reactivex.observers.DisposableObserver;


public class ProfileEditPresenterImp<V extends IProfileEditContract.ProfileEditView>
        extends BasePresenter<V> implements IProfileEditContract.ProfileEditPresenter<V> {

    private ProfileDataManager mProfileDataManager;

    /*Helper Usage for ProfileEdit Fragment */
    private ConfigurationMasterHelper configurationMasterHelper;
    private RetailerMasterBO retailerMasterBO;
    private UserMasterHelper userMasterHelper;
    private ChannelMasterHelper channelMasterHelper;
    private SubChannelMasterHelper subChannelMasterHelper;
    private RetailerHelper retailerHelper;


    private ArrayList<ContractStatus> mContractStatuses;
    private LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevId;
    private HashMap<String, String> mPreviousProfileChanges;
    private Vector<RetailerMasterBO> mDownloadLinkRetailer;
    private Vector<RetailerMasterBO> RetailerMasterList;


    /*Location ArrayList*/
    private ArrayList<LocationBO> mLocationMasterList1 = new ArrayList<>();
    private ArrayList<LocationBO> mLocationMasterList2 = new ArrayList<>();
    private ArrayList<LocationBO> mLocationMasterList3 = new ArrayList<>();
    private SparseArray<Vector<RetailerMasterBO>> mLinkRetailerListByDistributorId = new SparseArray<>();
    private Vector<RetailerMasterBO> nearByRetailers = new Vector<>();
    private Vector<ConfigureBO> profileConfig = new Vector<>();
    private Vector<ChannelBO> channelMaster = new Vector<>();

    /*SelectedPrioProducts */
    private ArrayList<StandardListBO> selectedPrioProducts = new ArrayList<>();

    //PriorityProduct
    private ArrayList<String> products = null;
    private ArrayList<StandardListBO> mPriorityProductList = null;
    private String selectedProductID;

    private boolean isLatLong = false;
    private String path;
    private String[] imgPaths;
    private String lat = "", longitude = "";
    private String imageFileName;
    private boolean IS_UPPERCASE_LETTER;
    private int locid = 0, loc2id = 0;
    private boolean validate = true;
    private boolean isMobileNoVerfied = true, isEmailVerfied = true, isWebUrlVerified = true;
    //by default  both mobile and email false. once otp verify will be become true. as of now i have used true.


    @Inject
    public ProfileEditPresenterImp(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable,
                                   ConfigurationMasterHelper configurationMasterHelper,
                                   V view,
                                   ProfileDataManager profileDataManager,
                                   @Profile UserMasterHelper userMasterHelper,
                                   @Profile RetailerMasterBO retailerMasterBO,
                                   @Profile ChannelMasterHelper channelMasterHelper,
                                   @Profile SubChannelMasterHelper subChannelMasterHelper,
                                   @Profile RetailerHelper retailerHelper,
                                   @Profile NewOutletHelper newOutletHelper,
                                   @Profile Vector<RetailerMasterBO> RetailerMasterList) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mProfileDataManager = profileDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.retailerMasterBO = retailerMasterBO;
        this.retailerHelper = retailerHelper;
        this.userMasterHelper = userMasterHelper;
        this.channelMasterHelper = channelMasterHelper;
        this.subChannelMasterHelper = subChannelMasterHelper;
        this.RetailerMasterList = RetailerMasterList;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyView() {
        retailerMasterBO = null;
        userMasterHelper = null;
        channelMasterHelper = null;
        retailerHelper = null;
        subChannelMasterHelper = null;
    }

    @Override
    public void getProfileEditDataFromLocalDataBase() {

        profileConfig = configurationMasterHelper.getProfileModuleConfig();
        IS_UPPERCASE_LETTER = configurationMasterHelper.IS_UPPERCASE_LETTER;

        if (profileConfig.size() != 0) {
            //Check the Profile Image config is enable or not using PROFILE60
            if (profileConfig.get(0).getConfigCode().equals(ProfileConstant.PROFILE_60) &&
                    (profileConfig.get(0).isFlag() == 1) &&
                    (profileConfig.get(0).getModule_Order() == 1)) {
                prepareProfileImage();
            }
        }

        for (ConfigureBO configureBO : profileConfig) {    /*First level  looping for prepare condition */

            if ((configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.LATTITUDE) && configureBO.isFlag() == 1)
                    || (configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.LONGITUDE) && configureBO.isFlag() == 1)) {
                isLatLong = true;
                lat = retailerMasterBO.getLatitude() + "";
                longitude = retailerMasterBO.getLongitude() + "";
            }

        }

        getProfileEditDetails();
    }

    private void getProfileEditDetails() {
        getIvyView().showLoading();
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.getContactStatus(),
                mProfileDataManager.getLocationListByLevId(),
                mProfileDataManager.getPreviousProfileChanges(retailerMasterBO.getRetailerID()),
                mProfileDataManager.getLinkRetailer(),
                getContractData(),
                new Function5<
                        ArrayList<ContractStatus>,
                        LinkedHashMap<Integer, ArrayList<LocationBO>>,
                        HashMap<String, String>,
                        Vector<RetailerMasterBO>,
                        Boolean, Boolean>() {
                    @Override
                    public Boolean apply(
                            ArrayList<ContractStatus> contractStatuses,
                            LinkedHashMap<Integer, ArrayList<LocationBO>> locationListByLevId,
                            HashMap<String, String> previousProfileChanges,
                            Vector<RetailerMasterBO> downloadLinkRetailer,
                            Boolean aBoolean) throws Exception {
                        mContractStatuses = contractStatuses;
                        mLocationListByLevId = locationListByLevId;
                        mPreviousProfileChanges = previousProfileChanges;
                        mDownloadLinkRetailer = downloadLinkRetailer;
                        return true;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                           /* System.out.println("mContactTitleSize-->" + mContactTitle.size());
                            System.out.println("mContactStatusSize-->" + mContractStatuses.size());
                            System.out.println("mLocationListByLevIdize-->" + mLocationListByLevId.size());
                            System.out.println("mPreviousProfileChangesSize-->" + mPreviousProfileChanges.size());
                            System.out.println("mDownloadLinkRetailerSize-->" + mDownloadLinkRetailer.size());*/
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        prepareLayout();
                        getLocation(); //Get Location
                        getLinkRetailer();
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void validateOTP(String type, String value) {

    }

    @Override
    public void updateLatLong(String lat, String longitude) {
        this.lat = lat;
        this.longitude = longitude;
    }

    @Override
    public void getImageLongClickListener(boolean isForLatLong) {
        if (!isLatLong && configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE
                && (LocationUtil.latitude == 0 || LocationUtil.longitude == 0) || (configurationMasterHelper.retailerLocAccuracyLvl != 0
                && LocationUtil.accuracy > configurationMasterHelper.retailerLocAccuracyLvl)) {
            getIvyView().showMessage("Location not captured.");
        } else {
            if (LocationUtil.latitude == 0 || LocationUtil.longitude == 0) {
                getIvyView().showMessage("Location not captured.");
            }
            if (!isForLatLong) {
                imageFileName = "PRO_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            } else {
                AppUtils.latlongImageFileName = "LATLONG_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            }
            getIvyView().takePhoto(imageFileName, isForLatLong);
        }
    }

    @Override
    public void getLatLongCameraBtnClickListene(boolean isForLatLong) {
        if (!isForLatLong) {
            imageFileName = "PRO_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
        } else {
            AppUtils.latlongImageFileName = "LATLONG_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
        }
        getIvyView().takePhoto(imageFileName, isForLatLong);
    }

    @Override
    public void getCameraReqestCode() {
        if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
            lat = LocationUtil.latitude + "";
            longitude = LocationUtil.longitude + "";
            if (lat.equals("") || SDUtil.convertToDouble(lat) == 0 || longitude.equals("") || SDUtil.convertToDouble(longitude) == 0) {
                getIvyView().showMessage("Location not captured.");
            } else {
                if (!isLatLong) {
                    profileConfig.add(new ConfigureBO(ProfileConstant.LATTITUDE, "Latitude", lat, 0, 0, 0));
                    profileConfig.add(new ConfigureBO(ProfileConstant.LONGITUDE, "Latitude", longitude, 0, 0, 0));
                } else {
                    getIvyView().setlatlongtextview(lat, longitude);
                }
                getIvyView().showMessage("Location captured successfully.");
            }
        }
    }

    @Override
    public Vector<SubchannelBO> getSubChannelMaster() {
        return subChannelMasterHelper.getSubChannelMaster();
    }

    @Override
    public int getSubchannelid() {
        return retailerMasterBO.getSubchannelid();
    }

    @Override
    public String getPreviousProfileChangesList(String configCode) {
        return mPreviousProfileChanges.get(configCode);
    }

    @Override
    public ArrayList<ContractStatus> getContractStatusList(String listName) {
        mContractStatuses.add(0, new ContractStatus(0, listName));
        return mContractStatuses;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList1(String locationName) {
        if (locationName != null && !locationName.isEmpty()) {
            mLocationMasterList1.add(0, new LocationBO(0, locationName));
            return mLocationMasterList1;
        }
        return mLocationMasterList1;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList2(String locationName) {
        if (locationName != null && !locationName.isEmpty()) {
            mLocationMasterList2.add(0, new LocationBO(0, locationName));
            return mLocationMasterList2;
        }
        return mLocationMasterList2;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList3(String locationName) {
        if (locationName != null && !locationName.isEmpty()) {
            mLocationMasterList3.add(0, new LocationBO(0, locationName));
            return mLocationMasterList3;
        }
        return mLocationMasterList3;
    }

    @Override
    public String[] getParentLevelName(boolean b) {
        return retailerHelper.getParentLevelName(loc2id, b);
    }

    @Override
    public String[] getParentLevelName(int locid, boolean b) {
        return retailerHelper.getParentLevelName(locid, b);
    }

    @Override
    public void downloadRetailerFlexValues(String type, final String menuCode, final String MName) {
        getCompositeDisposable().add(mProfileDataManager.downloadRetailerFlexValues(type)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<RetailerFlexBO>>() {
                    @Override
                    public void onNext(ArrayList<RetailerFlexBO> retailerFlexBOS) {
                        getIvyView().updateRetailerFlexValues(retailerFlexBOS, menuCode, MName);
                    }

                    @Override
                    public void onError(Throwable e) {

                        System.out.println(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));


    }

    @Override
    public boolean IS_BAIDU_MAP() {
        return configurationMasterHelper.IS_BAIDU_MAP;
    }


    @Override
    public Vector<RetailerMasterBO> getNearByRetailers() {
        return nearByRetailers;
    }

    @Override
    public void setNearByRetailers(Vector<RetailerMasterBO> nearByRetailers) {
        this.nearByRetailers = nearByRetailers;
    }

    @Override
    public void getNearbyRetailerIds() {

        getCompositeDisposable().add(mProfileDataManager.getNearbyRetailerIds(retailerMasterBO.getRetailerID())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> ids) {
                        if (ids != null) {
                            Vector<RetailerMasterBO> mNearbyRetIds = new Vector<>();
                            for (int i = 0; i < ids.size(); i++) {
                                for (RetailerMasterBO bo : RetailerMasterList) {
                                    if (bo.getRetailerID().equals(ids.get(i))) {
                                        mNearbyRetIds.add(bo);
                                    }
                                }
                            }
                            getIvyView().getNearbyRetailerIds(mNearbyRetIds);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                }));

    }


    @Override
    public void getNearbyRetailersEditRequest() {

        getCompositeDisposable().add(mProfileDataManager.getNearbyRetailersEditRequest(retailerMasterBO.getRetailerID())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<HashMap<String, String>>() {
                    @Override
                    public void onNext(HashMap<String, String> stringHashMap) {

                        if (stringHashMap.size() > 0) {
                            ArrayList<String> tempIds = new ArrayList<>();
                            Vector<RetailerMasterBO> mSelectedIds = new Vector<>();
                            for (String retId : stringHashMap.keySet()) {
                                if (stringHashMap.get(retId).equals("N")) {
                                    tempIds.add(retId);
                                }
                            }
                            for (String retId : tempIds) {
                                for (RetailerMasterBO bo : mDownloadLinkRetailer) {
                                    if (bo.getRetailerID().equals(retId + "")) {
                                        mSelectedIds.add(bo);
                                    }
                                }
                            }
                            getIvyView().getNearbyRetailersEditRequest(mSelectedIds);
                        } else {
                            getNearbyRetailerIds();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }


    @Override
    public void getLinkRetailerListByDistributorId() {
        Vector<RetailerMasterBO> retailersList = mLinkRetailerListByDistributorId.get(retailerMasterBO.getDistributorId());
        getIvyView().retailersButtonOnClick(retailersList, configurationMasterHelper.VALUE_NEARBY_RETAILER_MAX);
    }

    // to check sub channel is available or not
    // channel may be mapped in any sequance, so its availbily identified using iteration
    private boolean isChannelAvailable() {
        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.CHANNEL)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setProfileValues(final boolean isSave, StepperLayout.OnNextClickedCallback nextCallback, StepperLayout.OnCompleteClickedCallback completeCallback) {

        getIvyView().showLoading();

        if (profileConfig.get(0).getConfigCode().equals(ProfileConstant.PROFILE_60) &&
                (profileConfig.get(0).isFlag() == 1) &&
                (profileConfig.get(0).getModule_Order() == 1)) {
            if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                if ((lat.equals("") || SDUtil.convertToDouble(lat) == 0 || longitude.equals("")
                        || SDUtil.convertToDouble(longitude) == 0)
                        || (configurationMasterHelper.retailerLocAccuracyLvl != 0
                        && LocationUtil.accuracy > configurationMasterHelper.retailerLocAccuracyLvl)) {
                    getIvyView().showMessage(R.string.location_not_captured);
                }
            }
        }

        setNearByRetailers(getIvyView().getSelectedIds());

        getCompositeDisposable().add(Observable.zip(mProfileDataManager.downloadPriorityProductsForRetailerUpdate(retailerMasterBO.getRetailerID()),
                mProfileDataManager.getNearbyRetailerIds(retailerMasterBO.getRetailerID()),
                new BiFunction<ArrayList<String>, ArrayList<String>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<String> priorityProdList, ArrayList<String> nearbyRetailersList) throws Exception {

//                        Set Priority Product List
                        ArrayList<StandardListBO> priorityProducts = new ArrayList<>();
                        ArrayList<String> products = priorityProdList;
                        if (products == null)
                            products = new ArrayList<>();
                        if (getIvyView().getSelectedPriorityProductList() != null) {
                            for (StandardListBO bo : getIvyView().getSelectedPriorityProductList()) {
                                if (!products.contains(bo.getListID())) {
                                    bo.setStatus("N");
                                    priorityProducts.add(bo);
                                }
                            }
                        }
                        if (mPriorityProductList != null) {
                            if (priorityProducts.size() > 0) {
                                for (StandardListBO bo : mPriorityProductList) {
                                    if (products.contains(bo.getListID())) {
                                        bo.setStatus(ProfileConstant.D);
                                        priorityProducts.add(bo);
                                    }
                                }
                            }
                        }

                        setSelectedPrioProducts(priorityProducts);


//                        Update NearBy Retailers
                        final HashMap<String, String> nearByRetailers = new HashMap<>();
                        if (getNearByRetailers().size() > 0) {
                            for (RetailerMasterBO bo : getNearByRetailers()) {
                                nearByRetailers.put(bo.getRetailerID(), "N");
                            }
                        }

                        if (nearbyRetailersList != null) {
                            if (nearByRetailers.size() > 0) {
                                for (String id : nearbyRetailersList) {
                                    if (nearByRetailers.get(id) != null) {
                                        nearByRetailers.remove(id);
                                    } else {
                                        nearByRetailers.put(id, "D");
                                    }
                                }
                            }
                        }

                        setNearByRetailerList(nearByRetailers);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        setValues();
                        prepareProfileEditValues(isSave,nextCallback,completeCallback);

                        getIvyView().hideLoading();
                    }
                })
        );
    }

    private void prepareProfileEditValues(boolean isSave,StepperLayout.OnNextClickedCallback nextCallback,StepperLayout.OnCompleteClickedCallback completeCallback) {

        ArrayList<ConfigureBO> retailerFieldList = new ArrayList<>();
        ArrayList<StandardListBO> priorityProdList = new ArrayList<>();
        HashMap<String, String> nearByRetailerMapList = new HashMap<>();

        for (ConfigureBO configBO : profileConfig) {
            String conficCode = configBO.getConfigCode();
            if (configBO.getModule_Order() == 1) {
                switch (conficCode) {
                    case ProfileConstant.STORENAME:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getLocationId() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.ADDRESS1:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getAddress1() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.ADDRESS2:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getAddress2() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.ADDRESS3:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getAddress3() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CITY:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getCity() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.STATE:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getState() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CONTRACT:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getContractLovid() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.PINCODE:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getPincode() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CONTACT_NUMBER:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getContactnumber() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CHANNEL:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getChannelID() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.SUBCHANNEL:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getSubchannelid() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.LATTITUDE:
                        if (!configBO.getMenuNumber().equals("0.0")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getLatitude() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.LONGITUDE:
                        if (!configBO.getMenuNumber().equals("0.0")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getLongitude() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.PHOTO_CAPTURE:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        retailerFieldList.add(configBO);
                        break;
                    case ProfileConstant.RFiled1:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRField1() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.RField2:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRfield2() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CREDIT_INVOICE_COUNT:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getCredit_invoice_count() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.RField4:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRField4() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.RFIELD5:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRField5() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.RFIELD6:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRField6() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.RFIELD7:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getRField7() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.LOCATION01:
                        if (!configBO.getMenuNumber().equals("0")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getLocationId() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.CREDITPERIOD:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getCreditDays()+"",retailerFieldList);
                        break;
                    case ProfileConstant.PROFILE_60:
                        if (!configBO.getMenuNumber().equals("")) {
                            configBO.setRefId(retailerMasterBO.getRetailerID());
                            getRetailerProfileObject(configBO, retailerMasterBO.getProfileImagePath() + "", retailerFieldList);
                        }
                        break;
                    case ProfileConstant.GSTN:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getGSTNumber()+"",retailerFieldList);
                        break;
                    case ProfileConstant.INSEZ:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getIsSEZzone()+"",retailerFieldList);
                        break;
                    case ProfileConstant.PAN_NUMBER:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getPanNumber(),retailerFieldList);
                        break;
                    case ProfileConstant.FOOD_LICENCE_NUM:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getFoodLicenceNo(),retailerFieldList);
                        break;
                    case ProfileConstant.DRUG_LICENSE_NUM:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getDLNo(),retailerFieldList);
                        break;
                    case ProfileConstant.FOOD_LICENCE_EXP_DATE:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getFoodLicenceExpDate(),retailerFieldList);
                        break;
                    case ProfileConstant.DRUG_LICENSE_EXP_DATE:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getDLNoExpDate(),retailerFieldList);
                        break;
                    case ProfileConstant.EMAIL:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getEmail(),retailerFieldList);
                        break;
                    case ProfileConstant.WEB_SITE_URL:
                        updateWebSiteUrl(configBO);
                        break;
                    case ProfileConstant.MOBILE:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getMobile(),retailerFieldList);
                        break;
                    case ProfileConstant.FAX:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getFax(),retailerFieldList);
                        break;
                    case ProfileConstant.REGION:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRegion(),retailerFieldList);
                        break;
                    case ProfileConstant.COUNTRY:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getCountry(),retailerFieldList);
                        break;
                    case ProfileConstant.NEARBYRET:
                        nearByRetailerMapList.clear();
                        nearByRetailerMapList.putAll(getNearByRetailerList());
                        break;
                    case ProfileConstant.PRIORITYPRODUCT:
                        priorityProdList.clear();
                        priorityProdList.addAll(getSelectedPrioProducts());
                        break;
                    case ProfileConstant.DISTRICT:
                        configBO.setRefId(retailerMasterBO.getAddressid());
                        getRetailerProfileObject(configBO,retailerMasterBO.getDistrict(),retailerFieldList);
                        break;

                    case ProfileConstant.RFIELD8:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField8(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD9:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField9(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD10:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField10(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD11:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField11(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD12:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField12(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD13:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField13(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD14:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField14(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD15:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField15(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD16:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField16(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD17:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField17(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD18:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField18(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD19:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField19(),retailerFieldList);
                        break;
                    case ProfileConstant.RFIELD20:
                        configBO.setRefId(retailerMasterBO.getRetailerID());
                        getRetailerProfileObject(configBO,retailerMasterBO.getRField20(),retailerFieldList);
                        break;


                }
            } else if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                switch (conficCode) {
                    case ProfileConstant.LATTITUDE:
                        if (!configBO.getMenuNumber().equals("0.0")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO,retailerMasterBO.getLatitude()+"",retailerFieldList);
                        }
                        break;
                    case ProfileConstant.LONGITUDE:
                        if (!configBO.getMenuNumber().equals("0.0")) {
                            configBO.setRefId(retailerMasterBO.getAddressid());
                            getRetailerProfileObject(configBO,retailerMasterBO.getLongitude()+"",retailerFieldList);
                        }
                        break;
                }
            }
        }

        HashMap<String,Object> profileMapObject = new HashMap<>();

        if (!retailerFieldList.isEmpty())
            profileMapObject.put("ProfileFields",retailerFieldList);

        if (!priorityProdList.isEmpty())
            profileMapObject.put("Priority",priorityProdList);

        if (!nearByRetailerMapList.isEmpty())
            profileMapObject.put("NearByRetailer",nearByRetailerMapList);

        ProfileBaseBo profileBaseBo = new ProfileBaseBo();
        profileBaseBo.setStatus(isSave?"Save":"Update");
        profileBaseBo.setFieldName("Profile");
        profileBaseBo.setProfileFields(profileMapObject);

        if (isSave)
            getIvyView().onCompleteClicked(profileBaseBo,completeCallback);
        else
            getIvyView().onNextStepClicked(profileBaseBo,nextCallback);

    }

    private HashMap<String, String> nearByRetailerList = new HashMap<>();

    private HashMap<String, String> getNearByRetailerList() {
        return nearByRetailerList;
    }

    private void setNearByRetailerList(HashMap<String, String> nearByRetailerList) {
        this.nearByRetailerList = nearByRetailerList;
    }

    private void getRetailerProfileObject(ConfigureBO configBO, String originalFieldValue,ArrayList<ConfigureBO> retailerFieldList){

        configBO.setRetailerId(retailerMasterBO.getRetailerID());
        if ((originalFieldValue + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            configBO.setDeleteRow(true);
            retailerFieldList.add(configBO);
        } else if ((!(originalFieldValue + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            retailerFieldList.add(configBO);
        }
    }

    public void setSelectedPrioProducts(ArrayList<StandardListBO> selectedPrioProducts) {
        this.selectedPrioProducts = selectedPrioProducts;
    }

    public ArrayList<StandardListBO> getSelectedPrioProducts() {
        return selectedPrioProducts;
    }

    public boolean doValidateProdileEdit() {
        validate = true;
        for (int i = 0; i < profileConfig.size(); i++) {

            if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.STORENAME)
                    && profileConfig.get(i).getModule_Order() == 1) {
                try {
                    if (getIvyView().getDynamicEditTextValues(i).length() == 0) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showMessage(profileConfig.get(i).getMenuName() + " should not Be Empty");
                        validate = false;
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.CHANNEL)
                    && profileConfig.get(i).getModule_Order() == 1) {
                try {
                    if (getIvyView().getChennalSelectedItem().contains("select")) {
                        getIvyView().setChennalFocus();
                        getIvyView().showMessage("Choose " + profileConfig.get(i).getMenuName());
                        validate = false;
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.SUBCHANNEL)
                    && profileConfig.get(i).getModule_Order() == 1) {
                try {
                    if (getIvyView().getSubChennalSelectedItem().contains("select")) {
                        getIvyView().setSubChennalFocus();
                        getIvyView().showMessage("Choose " + profileConfig.get(i).getMenuName());
                        validate = false;
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.CONTACT_NUMBER)
                    && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {
                try {
                    if (getIvyView().getDynamicEditTextValues(i).length() == 0 ||
                            getIvyView().getDynamicEditTextValues(i).length() < profileConfig.get(i).getMaxLengthNo()) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showMessage(profileConfig.get(i).getMenuName() + " Length Must Be "
                                + profileConfig.get(i).getMaxLengthNo());
                        validate = false;
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.EMAIL)
                    && profileConfig.get(i).getModule_Order() == 1
                    && getIvyView().getDynamicEditTextValues(i).length() != 0) {
                try {
                    if (!StringUtils.isValidEmail(getIvyView().getDynamicEditTextValues(i))) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showMessage(R.string.enter_valid_email_id);
                        validate = false;
                        break;
                    }
                    if (!isEmailVerfied) {
                        getIvyView().setDynamicEditTextFocus(i);
                        validate = false;
                        getIvyView().showMessage(R.string.profile_edit_verify_email_id);
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.WEB_SITE_URL)
                    && profileConfig.get(i).getModule_Order() == 1
                    && getIvyView().getDynamicEditTextValues(i).length() != 0) {
                try {
                    if (!StringUtils.isValidURL(getIvyView().getDynamicEditTextValues(i))) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showMessage(R.string.enter_valid_url);
                        validate = false;
                        break;
                    }
                    if (!isWebUrlVerified) {
                        getIvyView().setDynamicEditTextFocus(i);
                        validate = false;
                        getIvyView().showMessage(R.string.please_verify_url);
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase(ProfileConstant.MOBILE)
                    && profileConfig.get(i).getModule_Order() == 1
                    && getIvyView().getDynamicEditTextValues(i).length() != 0) {
                try {
                    if (!isMobileNoVerfied) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showMessage(R.string.profile_edit_verify_mobile_no);
                        validate = false;
                        break;
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase(ProfileConstant.PAN_NUMBER)
                    && profileConfig.get(i).getModule_Order() == 1) {
                try {
                    int length = getIvyView().getDynamicEditTextValues(i).length();
                    if (length < profileConfig.get(i).getMaxLengthNo() ||
                            !StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i), profileConfig.get(i).getRegex())) {
                        if (length > 0 && length < profileConfig.get(i).getMaxLengthNo()) {
                            validate = false;
                            getIvyView().setDynamicEditTextFocus(i);
                            getIvyView().showMessage(profileConfig.get(i).getMenuName() + " Length Must Be" + profileConfig.get(i).getMaxLengthNo());
                            break;
                        } else if (length > 0 && !StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i), profileConfig.get(i).getRegex())) {
                            validate = false;
                            getIvyView().setDynamicEditTextFocus(i);
                            String errorMessage = profileConfig.get(i).getMenuName();
                            getIvyView().profileEditShowMessage(R.string.enter_valid, errorMessage);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase(ProfileConstant.GSTN)
                    && profileConfig.get(i).getModule_Order() == 1) {
                int length = getIvyView().getDynamicEditTextValues(i).length();
                try {
                    if (length == 0 && profileConfig.get(i).getMandatory() == 1) {
                        validate = false;
                        String errorMessage = profileConfig.get(i).getMenuName();
                        getIvyView().profileEditShowMessage(R.string.enter, errorMessage);
                        break;
                    } else if (length > 0 && length < profileConfig.get(i).getMaxLengthNo()) {
                        validate = false;
                        getIvyView().showMessage(profileConfig.get(i).getMenuName()
                                + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());
                        break;
                    } else if (length > 0 && !StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i)
                            , profileConfig.get(i).getRegex())) {
                        validate = false;
                        getIvyView().setDynamicEditTextFocus(i);
                        String errorMessage = profileConfig.get(i).getMenuName();
                        getIvyView().profileEditShowMessage(R.string.enter_valid, errorMessage);
                        break;
                    } else if (length > 0 && !isValidGSTINWithPAN(getIvyView().getDynamicEditTextValues(i))) {
                        validate = false;
                        getIvyView().setDynamicEditTextFocus(i);
                        String errorMessage = profileConfig.get(i).getMenuName();
                        getIvyView().profileEditShowMessage(R.string.enter_valid, errorMessage);
                        break;
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (profileConfig.get(i).getModule_Order() == 1) {
                try {
                    if (!StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        int length = getIvyView().getDynamicEditTextValues(i).length();
                        if (length < profileConfig.get(i).getMaxLengthNo() ||
                                !StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i), profileConfig.get(i).getRegex())) {
                            if (length > 0 && getIvyView().getDynamicEditTextValues(i).length() < profileConfig.get(i).getMaxLengthNo()) {
                                validate = false;
                                getIvyView().setDynamicEditTextFocus(i);
                                getIvyView().showMessage(profileConfig.get(i).getMenuName() + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());
                                break;
                            } else if (length > 0 && !StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i), profileConfig.get(i).getRegex())) {
                                validate = false;
                                getIvyView().setDynamicEditTextFocus(i);
                                String errorMessage = profileConfig.get(i).getMenuName();
                                getIvyView().profileEditShowMessage(R.string.enter_valid, errorMessage);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
        return validate;
    }

    private void setValues() {

        try {
            int size = profileConfig.size();
            for (int i = 0; i < size; i++) {
                String configCode = profileConfig.get(i).getConfigCode();
                if (configCode.equals(ProfileConstant.STORENAME) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS1) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS2) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS3) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.CONTACT_NUMBER) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.CHANNEL) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    ChannelBO cBo = (ChannelBO) getIvyView().getChennalSelectedItemBO();
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(cBo.getChannelId() + "");
                } else if (configCode.equals(ProfileConstant.SUBCHANNEL) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(getIvyView().getSubChennalSelectedItemId() + "");

                } else if (configCode.equals(ProfileConstant.CONTRACT) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mContractStatuses != null)
                        profileConfig.get(i).setMenuNumber(getIvyView().getContractSpinnerSelectedItemListId() + "");

                } else if (configCode.equals(ProfileConstant.LATTITUDE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(lat)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        //converting big decimal value while Exponential value occur
                        String lattiTude = (lat).contains("E")
                                ? (SDUtil.truncateDecimal(SDUtil.convertToDouble(lat), -1) + "").substring(0, 20)
                                : (lat.length() > 20 ? lat.substring(0, 20) : lat);

                        profileConfig.get(i).setMenuNumber(lattiTude);
                    }
                } else if (configCode.equals(ProfileConstant.LONGITUDE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(longitude)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        //converting big decimal value while Exponential value occur
                        String longiTude = (longitude).contains("E")
                                ? (SDUtil.truncateDecimal(SDUtil.convertToDouble(longitude), -1) + "").substring(0, 20)
                                : (longitude.length() > 20 ? longitude.substring(0, 20) : longitude);

                        profileConfig.get(i).setMenuNumber(longiTude);
                    }
                } else if (configCode.equals(ProfileConstant.PHOTO_CAPTURE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (AppUtils.latlongImageFileName == null || "".equals(AppUtils.latlongImageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(AppUtils.latlongImageFileName);
                    }
                } else if (configCode.equals(ProfileConstant.LOCATION01) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList1 != null) {
                        if (mLocationMasterList1.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(getIvyView().getLocation1SelectedItemLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                } else if (configCode.equals(ProfileConstant.LOCATION02) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList2 != null) {
                        if (mLocationMasterList2.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(getIvyView().getLocation2SelectedItemLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                } else if (configCode.equals(ProfileConstant.LOCATION) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList3 != null) {
                        if (mLocationMasterList3.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(getIvyView().getLocation3SelectedItemLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                }
                else if (configCode.equals(ProfileConstant.PINCODE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.CITY) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.STATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.CREDITPERIOD) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber(getIvyView().getDynamicEditTextValues(i));
                    }
                } else if (configCode.equals(ProfileConstant.RFiled1) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.RField2) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.CREDIT_INVOICE_COUNT) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.RField4) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = getIvyView().getRField4SpinnerSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(ProfileConstant.RFIELD5) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = getIvyView().getRField5SpinnerSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(ProfileConstant.RFIELD6) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = getIvyView().getRField6SpinnerSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(ProfileConstant.RFIELD7) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = getIvyView().getRField7SpinnerSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(ProfileConstant.PROFILE_60) && profileConfig.get(i).getModule_Order() == 1) {
                    if (imageFileName == null || "".equals(imageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(imageFileName);
                    }
                } else if (configCode.equals(ProfileConstant.GSTN) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.INSEZ) && profileConfig.get(i).getModule_Order() == 1) {
                    if (!getIvyView().getSEZcheckBoxCheckedValues()) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber("1");
                    }
                } else if (configCode.equals(ProfileConstant.PAN_NUMBER) && profileConfig.get(i).getModule_Order() == 1) {

                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FOOD_LICENCE_NUM) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.DRUG_LICENSE_NUM) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FOOD_LICENCE_EXP_DATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (getIvyView().getFoodLicenceExpDateValue().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(
                                AppUtils.validateInput(getIvyView().getFoodLicenceExpDateValue())));
                    }
                } else if (configCode.equals(ProfileConstant.DRUG_LICENSE_EXP_DATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (getIvyView().getDrugLicenceExpDateValue().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(
                                AppUtils.validateInput(getIvyView().getDrugLicenceExpDateValue())));
                    }

                } else if (configCode.equals(ProfileConstant.EMAIL) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.WEB_SITE_URL) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.MOBILE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FAX) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.REGION) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.COUNTRY) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.DISTRICT) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }

                else if (configCode.equals(ProfileConstant.RFIELD8) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD9) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD10) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD11) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD12) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD13) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD14) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD15) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD16) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD17) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD18) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD19) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }else if (configCode.equals(ProfileConstant.RFIELD20) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isNullOrEmpty(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                }


            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isValidGSTINWithPAN(CharSequence target) {
        for (int index = 0; index < profileConfig.size(); index++) {
            if (profileConfig.get(index).getConfigCode().equalsIgnoreCase(ProfileConstant.PAN_NUMBER)) {
                String panNumber = getIvyView().getDynamicEditTextValues(index);
                return panNumber.length() <= 0 || target.subSequence(2, target.length() - 3).equals(panNumber);
            }
        }
        return true;
    }

    @Override
    public void verifyOTP(final String mType, final String mValue) {

        getCompositeDisposable().add(mProfileDataManager.generateOtpUrl()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                        /* if(!AppUtils.isNullOrEmpty(s)){
                         *//*switch (mType) {
                            case "MOBILE":
                                if (mValue != null && !mValue.isEmpty() && mValue.length() == 10)
                                   // verifyOtpAsyncTask(mValue, mType); //Need to do later
                                    break;
                            case "EMAIL":
                                if (AppUtils.isValidEmail(mValue))
                                   // verifyOtpAsyncTask(mValue, mType); //Need to do later
                                else
                                    getIvyView().showMessage(R.string.invalid_email_address);
                                break;
                        }*//*
                }else
                    getIvyView().showMessage(R.string.otp_download_url_empty);*/
                    }
                }));

    }

    @Override
    public void getImageOnClickListener() {
        final String imagePath = retailerMasterBO.getProfileImagePath();
        getCompositeDisposable().add(mProfileDataManager.checkProfileImagePath(retailerMasterBO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   if (response && !StringUtils.isNullOrEmpty(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().imageViewOnClick(userMasterHelper.getUserMasterBO().getUserid(), path, response);
                                   } else if (!StringUtils.isNullOrEmpty(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().imageViewOnClick(userMasterHelper.getUserMasterBO().getUserid(), path, response);
                                   } else {
                                       getIvyView().imageViewOnClick(0, "", false);
                                   }
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                               }
                           }
                ));
    }


    private void prepareProfileImage() {
        final String imagePath = retailerMasterBO.getProfileImagePath();
        getCompositeDisposable().add(mProfileDataManager.checkProfileImagePath(retailerMasterBO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   if (response && !StringUtils.isNullOrEmpty(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().createImageView(path);
                                   } else if (!StringUtils.isNullOrEmpty(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().createImageView(userMasterHelper.getUserMasterBO().getUserid(), path);
                                   } else {
                                       getIvyView().createImageView();
                                   }
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));
    }


    private void checkConfigrationForEditText(int mNumber, String configCode, String menuName, String values) {

        String mConfigCode = profileConfig.get(mNumber).getConfigCode();
        if (!comparConfigerCode(mConfigCode, ProfileConstant.EMAIL) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.PAN_NUMBER) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.GSTN) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.WEB_SITE_URL)) {   /*EMAIL, PenNumber,GST,WEB_SITE_URL*/
            //regex
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //getIvyView().addRegexFilter(profileConfig.get(mNumber).getRegex());
        }

        if (comparConfigerCode(mConfigCode, ProfileConstant.PAN_NUMBER)) {  /*PanNumber*/
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkPANRegex(mNumber);
        }

        if (comparConfigerCode(mConfigCode, ProfileConstant.GSTN)) {   /*GST*/
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkGSTRegex(mNumber);
        }

        if (comparConfigerCode(mConfigCode, ProfileConstant.STORENAME)
                || comparConfigerCode(mConfigCode, ProfileConstant.ADDRESS1)
                || comparConfigerCode(mConfigCode, ProfileConstant.ADDRESS2)
                || comparConfigerCode(mConfigCode, ProfileConstant.ADDRESS3)
                || comparConfigerCode(mConfigCode, ProfileConstant.CITY)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFiled1)
                || comparConfigerCode(mConfigCode, ProfileConstant.RField2)
                || comparConfigerCode(mConfigCode, ProfileConstant.CREDIT_INVOICE_COUNT)
                || (comparConfigerCode(mConfigCode, ProfileConstant.RField4) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.RFIELD5) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.RFIELD6) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.RFIELD7) && profileConfig.get(mNumber).getHasLink() == 0)
                || comparConfigerCode(mConfigCode, ProfileConstant.STATE)
                || comparConfigerCode(mConfigCode, ProfileConstant.PINCODE)
                || comparConfigerCode(mConfigCode, ProfileConstant.GSTN)
                || comparConfigerCode(mConfigCode, ProfileConstant.PAN_NUMBER)
                || comparConfigerCode(mConfigCode, ProfileConstant.FOOD_LICENCE_NUM)
                || comparConfigerCode(mConfigCode, ProfileConstant.DRUG_LICENSE_NUM)
                || comparConfigerCode(mConfigCode, ProfileConstant.EMAIL)
                || comparConfigerCode(mConfigCode, ProfileConstant.WEB_SITE_URL)
                || comparConfigerCode(mConfigCode, ProfileConstant.REGION)
                || comparConfigerCode(mConfigCode, ProfileConstant.COUNTRY)
                || comparConfigerCode(mConfigCode, ProfileConstant.CONTACT_NUMBER)
                || comparConfigerCode(mConfigCode, ProfileConstant.MOBILE)
                || comparConfigerCode(mConfigCode, ProfileConstant.FAX)
                || comparConfigerCode(mConfigCode, ProfileConstant.CREDITPERIOD)
                || comparConfigerCode(mConfigCode, ProfileConstant.DISTRICT)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD8)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD9)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD10)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD11)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD12)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD13)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD14)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD15)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD16)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD17)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD18)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD19)
                || comparConfigerCode(mConfigCode, ProfileConstant.RFIELD20)) {

            int Mandatory = profileConfig.get(mNumber).getMandatory();
            int MAX_CREDIT_DAYS = configurationMasterHelper.MAX_CREDIT_DAYS;
            getIvyView().createEditTextView(mNumber, configCode, menuName, values
                    , IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS);
        }


    }

    private int mNumber;
    private String mName;
    private String configCode;
    private int flag;

    private void prepareLayout() {

        for (mNumber = 0; mNumber < profileConfig.size(); mNumber++) {
            flag = profileConfig.get(mNumber).isFlag();
            int order = profileConfig.get(mNumber).getModule_Order();
            mName = profileConfig.get(mNumber).getMenuName();
            configCode = profileConfig.get(mNumber).getConfigCode();

            if (flag == 1 && order == 1) {
                switch (configCode) {
                    case ProfileConstant.STORENAME:
                        prepareStorName();
                        break;
                    case ProfileConstant.ADDRESS1:
                        prepareAddress1();
                        break;
                    case ProfileConstant.ADDRESS2:
                        prepareAddress2();
                        break;
                    case ProfileConstant.ADDRESS3:
                        prepareAddress3();
                        break;
                    case ProfileConstant.CITY:
                        prepareCity();
                        break;
                    case ProfileConstant.STATE:
                        prepareState();
                        break;
                    case ProfileConstant.PINCODE:
                        preparePincode();
                        break;
                    case ProfileConstant.CONTACT_NUMBER:
                        prepareContectNumber();
                        break;
                    case ProfileConstant.CHANNEL:
                        prepareChennel();
                        break;
                    case ProfileConstant.SUBCHANNEL:
                        prepareSubChennal();
                        break;
                    case ProfileConstant.CONTRACT:
                        prepareContract();
                        break;
                    case ProfileConstant.LATTITUDE:
                        prepareLatLong();
                        break;
                    case ProfileConstant.PHOTO_CAPTURE:
                        getIvyView().isLatLongCameravailable(true);
                        break;
                    case ProfileConstant.LOCATION01:
                        prepareLocation1();
                        break;
                    case ProfileConstant.LOCATION02:
                        prepareLocation2();
                        break;
                    case ProfileConstant.LOCATION:
                        prepareLocation();
                        break;
                    case ProfileConstant.NEARBYRET:
                        prepareNearByRetailer();
                        break;
                    case ProfileConstant.CREDITPERIOD:
                        prepareCrediPreriod();
                        break;
                    case ProfileConstant.RFiled1:
                        prepareRfield1();
                        break;
                    case ProfileConstant.RField2:
                        prepareRfield2();
                        break;
                    case ProfileConstant.RField4:
                        prepareRfield4();
                        break;
                    case ProfileConstant.RFIELD5:
                        prepareRfield5();
                        break;
                    case ProfileConstant.RFIELD6:
                        prepareRfield6();
                        break;
                    case ProfileConstant.RFIELD7:
                        prepareRfield7();
                        break;
                    case ProfileConstant.CREDIT_INVOICE_COUNT:
                        prepareProfile27();
                        break;
                    case ProfileConstant.PRIORITYPRODUCT:
                        downloadPriority(mNumber, mName);
                        break;
                    case ProfileConstant.GSTN:
                        prepareGSTN();
                        break;
                    case ProfileConstant.INSEZ:
                        prepareSezCheckBox();
                        break;
                    case ProfileConstant.PAN_NUMBER:
                        preparePanNumber();
                        break;
                    case ProfileConstant.FOOD_LICENCE_NUM:
                        prepareFoodLicenceNumber();
                        break;
                    case ProfileConstant.DRUG_LICENSE_NUM:
                        prepareDrugLicenseNumber();
                        break;
                    case ProfileConstant.EMAIL:
                        prepareEmail();
                        break;
                    case ProfileConstant.WEB_SITE_URL:
                        prepareWebUrl();
                        break;
                    case ProfileConstant.MOBILE:
                        prepareMobile();
                        break;
                    case ProfileConstant.FAX:
                        prepareFAX();
                        break;
                    case ProfileConstant.REGION:
                        prepareRegion();
                        break;
                    case ProfileConstant.COUNTRY:
                        prepareCountry();
                        break;
                    case ProfileConstant.FOOD_LICENCE_EXP_DATE:
                        prepareFoodLiceneExpDate();
                        break;
                    case ProfileConstant.DRUG_LICENSE_EXP_DATE:
                        prepareDrugLiceneExpDate();
                        break;
                    case ProfileConstant.DISTRICT:
                        prepareDistrict();
                        break;
                    case ProfileConstant.RFIELD8:
                        prepareRField(retailerMasterBO.getRField8());
                        break;
                    case ProfileConstant.RFIELD9:
                        prepareRField(retailerMasterBO.getRField9());
                        break;
                    case ProfileConstant.RFIELD10:
                        prepareRField(retailerMasterBO.getRField10());
                        break;
                    case ProfileConstant.RFIELD11:
                        prepareRField(retailerMasterBO.getRField11());
                        break;
                    case ProfileConstant.RFIELD12:
                        prepareRField(retailerMasterBO.getRField12());
                        break;
                    case ProfileConstant.RFIELD13:
                        prepareRField(retailerMasterBO.getRField13());
                        break;
                    case ProfileConstant.RFIELD14:
                        prepareRField(retailerMasterBO.getRField14());
                        break;
                    case ProfileConstant.RFIELD15:
                        prepareRField(retailerMasterBO.getRField15());
                        break;
                    case ProfileConstant.RFIELD16:
                        prepareRField(retailerMasterBO.getRField16());
                        break;
                    case ProfileConstant.RFIELD17:
                        prepareRField(retailerMasterBO.getRField17());
                        break;
                    case ProfileConstant.RFIELD18:
                        prepareRField(retailerMasterBO.getRField18());
                        break;
                    case ProfileConstant.RFIELD19:
                        prepareRField(retailerMasterBO.getRField19());
                        break;
                    case ProfileConstant.RFIELD20:
                        prepareRField(retailerMasterBO.getRField20());
                        break;
                }
            } else {
                //write the code here  for without flag and order condition
            }
        }

    }

    private void prepareRField(String fieldValue) {
        String text = fieldValue + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }

    private void prepareDrugLiceneExpDate() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getDLNoExpDate()))
            retailerMasterBO.setDLNoExpDate("Select Date");
        String text = retailerMasterBO.getDLNoExpDate();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        getIvyView().createDrugLicenseExpDate(mName, mNumber, text);
    }

    private void prepareFoodLiceneExpDate() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getFoodLicenceExpDate()))
            retailerMasterBO.setFoodLicenceExpDate("Select Date");
        String text = retailerMasterBO.getFoodLicenceExpDate();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        getIvyView().createFoodLicenceExpDate(mName, mNumber, text);
    }


    private void prepareCountry() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getCountry()))
            retailerMasterBO.setCountry("");
        String text = retailerMasterBO.getCountry();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareRegion() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getRegion()))
            retailerMasterBO.setRegion("");
        String text = retailerMasterBO.getRegion();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFAX() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getFax()))
            retailerMasterBO.setFax("");
        String text = retailerMasterBO.getFax();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareMobile() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getMobile()))
            retailerMasterBO.setMobile("");
        String text = retailerMasterBO.getMobile();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareEmail() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getEmail()))
            retailerMasterBO.setEmail("");
        String text = retailerMasterBO.getEmail();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }

    private void prepareWebUrl() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getWebUrl()))
            retailerMasterBO.setWebUrl("");
        String text = retailerMasterBO.getWebUrl();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareDrugLicenseNumber() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getDLNo()))
            retailerMasterBO.setDLNo("");
        String text = retailerMasterBO.getDLNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFoodLicenceNumber() {

        if (StringUtils.isNullOrEmpty(retailerMasterBO.getFoodLicenceNo()))
            retailerMasterBO.setFoodLicenceNo("");
        String text = retailerMasterBO.getFoodLicenceNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);

    }


    private void preparePanNumber() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getPanNumber()))
            retailerMasterBO.setPanNumber("");
        String text = retailerMasterBO.getPanNumber();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareSezCheckBox() {
        int Mandatory = profileConfig.get(mNumber).getMandatory();
        String text = retailerMasterBO.getIsSEZzone() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        getIvyView().createCheckBoxView(text, Mandatory, mName);
    }


    private void prepareStorName() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getRetailerName()))
            retailerMasterBO.setRetailerName("");
        String retailderName = retailerMasterBO.getRetailerName() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(retailderName))
                retailderName = mPreviousProfileChanges.get(configCode);

        checkConfigrationForEditText(mNumber, configCode, mName, retailderName);
    }


    private void prepareAddress1() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getAddress1()))
            retailerMasterBO.setAddress1("");
        String text = retailerMasterBO.getAddress1() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress2() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getAddress2()))
            retailerMasterBO.setAddress2("");
        String text = retailerMasterBO.getAddress2() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress3() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getAddress3()))
            retailerMasterBO.setAddress3("");
        String text = retailerMasterBO.getAddress3() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareCity() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getCity()))
            retailerMasterBO.setCity("");
        String text = retailerMasterBO.getCity() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.CITY + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareState() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getState()))
            retailerMasterBO.setState("");
        String text = retailerMasterBO.getState() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.STATE + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void preparePincode() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getPincode()))
            retailerMasterBO.setPincode("");
        String text = retailerMasterBO.getPincode() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.PINCODE + "," + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareContectNumber() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getContactnumber()))
            retailerMasterBO.setContactnumber("");
        String text = retailerMasterBO.getContactnumber() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareChennel() {
        int id = retailerMasterBO.getChannelID();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
        channelMaster.addAll(channelMasterHelper.getChannelMaster());
        getIvyView().createSpinnerView(mNumber, mName, configCode, id);
    }

    private void prepareDistrict() {
        if (StringUtils.isNullOrEmpty(retailerMasterBO.getDistrict()))
            retailerMasterBO.setDistrict("");
        String text = retailerMasterBO.getDistrict();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }

    @Override
    public Vector<ChannelBO> getChannelMaster() {
        return channelMaster;
    }

    private void prepareSubChennal() {
        int id = retailerMasterBO.getSubchannelid();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
        getIvyView().createSpinnerView(mNumber, mName, configCode, id);
    }


    private void prepareContract() {
        int id = retailerMasterBO.getContractLovid();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
        getIvyView().createSpinnerView(mNumber, mName, configCode, id);
    }


    private void prepareLatLong() {
        String textLat = retailerMasterBO.getLatitude() + "";
        String MenuName = "LatLong";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(textLat))
                textLat = mPreviousProfileChanges.get(configCode);
        for (int j = 0; j < profileConfig.size(); j++) {
            if (profileConfig.get(j).getConfigCode().equals(ProfileConstant.LONGITUDE)
                    && flag == 1 && profileConfig.get(mNumber).getModule_Order() == 1) {
                String textLong = retailerMasterBO.getLongitude() + "";
                if (mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode()) != null)
                    if (!mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode()).equals(textLong))
                        textLong = mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode());
                String text = textLat + ", " + textLong;
                getIvyView().createLatlongTextView(mNumber, MenuName, text);
            }
        }
    }


    private void prepareLocation1() {
        try {
            String title = "";
            locid = retailerMasterBO.getLocationId();
            if (locid != 0) {
                String[] loc1 = retailerHelper.getParentLevelName(locid, false);
                title = loc1[2];
            }
            int id = retailerMasterBO.getLocationId();
            if (mPreviousProfileChanges.get(configCode) != null)
                if (mPreviousProfileChanges.get(configCode).equals(id + ""))
                    id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
            getIvyView().createSpinnerView(mNumber, title, configCode, id, locid);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private void prepareLocation2() {
        try {
            String title = "";
            String[] loc2 = retailerHelper.getParentLevelName(locid, true);
            if (loc2 != null) {
                loc2id = SDUtil.convertToInt(loc2[0]);
                title = loc2[2];
            }
            int id = retailerMasterBO.getLocationId();
            if (mPreviousProfileChanges.get(configCode) != null)
                if (mPreviousProfileChanges.get(configCode).equals(id + ""))
                    id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
            getIvyView().createSpinnerView(mNumber, title, configCode, id);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private void prepareLocation() {
        try {
            String title = "";
            String[] loc3 = retailerHelper.getParentLevelName(loc2id, true);
            if (loc3 != null) {
                title = loc3[2];
            }
            int id = retailerMasterBO.getLocationId();
            if (mPreviousProfileChanges.get(configCode) != null)
                if (mPreviousProfileChanges.get(configCode).equals(id + ""))
                    id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
            getIvyView().createSpinnerView(mNumber, title, configCode, id);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private void prepareCrediPreriod() {
        String text = retailerMasterBO.getCreditDays() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareNearByRetailer() {
        if (!retailerMasterBO.getIsNew().equals("Y"))
            if (getNearByRetailers() != null)
                getNearByRetailers().clear();
        getIvyView().createNearByRetailerView(mNumber, mName, true);
    }


    private void prepareRfield1() {
        String text = retailerMasterBO.getRField1() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareRfield2() {
        String text = retailerMasterBO.getRfield2() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareProfile27() {
        String text = retailerMasterBO.getCredit_invoice_count() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareRfield4() {
        String text = retailerMasterBO.getRField4() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        if (profileConfig.get(mNumber).getHasLink() == 0)
            checkConfigrationForEditText(mNumber, configCode, mName, text);
        else {
            if (text.equals(""))
                text = "0";
            getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
        }
    }


    private void prepareRfield5() {
        String text = retailerMasterBO.getRField5() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        if (profileConfig.get(mNumber).getHasLink() == 0)
            checkConfigrationForEditText(mNumber, configCode, mName, text);
        else {
            if (text.equals(""))
                text = "0";
            getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
        }
    }


    private void prepareRfield6() {
        String text = retailerMasterBO.getRField6() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        if (profileConfig.get(mNumber).getHasLink() == 0)
            checkConfigrationForEditText(mNumber, configCode, mName, text);
        else {
            if (text.equals(""))
                text = "0";
            getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
        }
    }


    private void prepareRfield7() {
        String text = retailerMasterBO.getRField7() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        if (profileConfig.get(mNumber).getHasLink() == 0)
            checkConfigrationForEditText(mNumber, configCode, mName, text);
        else {
            if (text.equals(""))
                text = "0";
            getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
        }
    }


    private void prepareGSTN() {
        String text = retailerMasterBO.getGSTNumber() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void downloadPriority(final int mNumber, final String mName) {
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.downloadPriorityProducts(),
                mProfileDataManager.downloadPriorityProductsForRetailer(retailerMasterBO.getRetailerID()),
                new BiFunction<ArrayList<StandardListBO>, ArrayList<String>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<StandardListBO> priorityProducts,
                                         ArrayList<String> priorityProductsForRetailer) throws Exception {
                        mPriorityProductList = priorityProducts;
                        products = priorityProductsForRetailer;
                        return true;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        String productID = "";
                        StringBuffer sb = new StringBuffer();
                        if (products != null) {
                            for (StandardListBO bo : mPriorityProductList) {
                                if (products.contains(bo.getListID())) {
                                    bo.setChecked(true);
                                    if (sb.length() > 0)
                                        sb.append(", ");
                                    sb.append(bo.getListName());
                                    selectedProductID = bo.getListID();
                                }
                            }
                        }
                        getIvyView().createPriorityProductView(mPriorityProductList, selectedProductID, mNumber, mName, sb.toString(), productID);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                }));


    }


    private Observable<Boolean> getContractData() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                retailerHelper.loadContractData();
                return true;
            }
        });
    }


    private void getLocation() {
        if (mLocationListByLevId != null) {
            int count = 0;
            for (Map.Entry<Integer, ArrayList<LocationBO>> entry : mLocationListByLevId.entrySet()) {
                count++;
                Commons.print("level id," + entry.getKey() + "");
                if (entry.getValue() != null) {
                    if (count == 1) {
                        mLocationMasterList1 = entry.getValue();
                    } else if (count == 2) {
                        mLocationMasterList2 = entry.getValue();
                    } else if (count == 3) {
                        mLocationMasterList3 = entry.getValue();
                    }
                }
            }
        }
    } //Location


    private void getLinkRetailer() {
        try {
            mLinkRetailerListByDistributorId = new SparseArray<>();

            Vector<RetailerMasterBO> linkRetailerList = new Vector<>();

            int distributorId = 0;

            RetailerMasterBO linkRetailerBO;

            if (mDownloadLinkRetailer.size() > 0) {

                for (int i = 0; i < mDownloadLinkRetailer.size(); i++) {
                    linkRetailerBO = mDownloadLinkRetailer.get(i);
                    if (distributorId != linkRetailerBO.getDistributorId()) {
                        if (distributorId != 0) {
                            mLinkRetailerListByDistributorId.put(distributorId, linkRetailerList);
                            linkRetailerList = new Vector<>();
                            linkRetailerList.add(linkRetailerBO);
                            distributorId = linkRetailerBO.getDistributorId();
                        } else {
                            linkRetailerList.add(linkRetailerBO);
                            distributorId = linkRetailerBO.getDistributorId();
                        }
                    } else {
                        linkRetailerList.add(linkRetailerBO);
                    }
                }
                if (linkRetailerList.size() > 0) {
                    mLinkRetailerListByDistributorId.put(distributorId, linkRetailerList);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /*comparing two values with equalsIgnoreCase*/
    private boolean comparConfigerCode(String configCode, String configCodeFromDB) {
        return configCode.equalsIgnoreCase(configCodeFromDB);
    }

    @Override
    public void onDetach() {
        mProfileDataManager.closeDB();
        super.onDetach();

    }

    @Override
    public boolean checkRegex(int menuNumber, String typedText) {
        return StringUtils.validRegex(profileConfig.get(menuNumber).getRegex(), typedText);
    }
}

