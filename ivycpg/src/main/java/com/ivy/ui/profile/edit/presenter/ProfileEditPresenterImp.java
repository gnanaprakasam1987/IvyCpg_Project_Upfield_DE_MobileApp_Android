package com.ivy.ui.profile.edit.presenter;


import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.SparseArray;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
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
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.data.ChannelWiseAttributeList;
import com.ivy.ui.profile.data.IProfileDataManager;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;

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

    private IProfileDataManager mProfileDataManager;

    /*Helper Usage for ProfileEdit Fragment */
    private ConfigurationMasterHelper configurationMasterHelper;
    private RetailerMasterBO retailerMasterBO;
    private UserMasterHelper userMasterHelper;
    private ChannelMasterHelper channelMasterHelper;
    private SubChannelMasterHelper subChannelMasterHelper;
    private RetailerHelper retailerHelper;


    private ArrayList<NewOutletBO> mContactStatus;
    private LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevId;
    private HashMap<String, String> mPreviousProfileChanges;
    private Vector<RetailerMasterBO> mDownloadLinkRetailer;
    private ArrayList<RetailerFlexBO> downloadRetailerFlexValues;
    private Vector<RetailerMasterBO> RetailerMasterList;
    private NewOutletHelper newOutletHelper;


    /*Location ArrayList*/
    private ArrayList<LocationBO> mLocationMasterList1 = new ArrayList<>();
    private ArrayList<LocationBO> mLocationMasterList2 = new ArrayList<>();
    private ArrayList<LocationBO> mLocationMasterList3 = new ArrayList<>();
    private SparseArray<Vector<RetailerMasterBO>> mLinkRetailerListByDistributorId = new SparseArray<>();
    private Vector<RetailerMasterBO> nearByRetailers = new Vector<>();
    private Vector<ConfigureBO> profileConfig = new Vector<>();
    private Vector<ChannelBO> channelMaster = new Vector<>();

    /*Attributes */
    private ArrayList<Integer> mCommonAttributeList;
    private HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID = new HashMap<>();
    private HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID = new HashMap<>();
    private ArrayList<NewOutletAttributeBO> mEditAttributeList = new ArrayList<>();
    private ArrayList<NewOutletAttributeBO> mAttributeChildList = new ArrayList<>();
    private ArrayList<NewOutletAttributeBO> mAttributeParentList = new ArrayList<>();
    private ArrayList<NewOutletAttributeBO> mAttributeList = new ArrayList<>();
    private HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap = new HashMap<>();
    private ArrayList<Integer> mChannelAttributeList = new ArrayList<>();
    private ArrayList<StandardListBO> selectedPrioProducts = new ArrayList<>();

    //PriorityProduct
    private ArrayList<String> products = null;
    private ArrayList<StandardListBO> mPriorityProductList = null;
    private String selectedProductID;
    private ArrayList<NewOutletAttributeBO> attributeList;

    private boolean isLatLong = false;
    private String path;
    private String[] imgPaths;
    private String lat = "", longitude = "";
    private String imageFileName;
    private boolean IS_UPPERCASE_LETTER;
    private int locid = 0, loc2id = 0;
    private boolean validate = true;
    private boolean isMobileNoVerfied = true, isEmailVerfied = true;
    //by default  both mobile and email false. once otp verify will be become true. as of now i have used true.


    @Inject
    public ProfileEditPresenterImp(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable,
                                   ConfigurationMasterHelper configurationMasterHelper,
                                   V view,
                                   IProfileDataManager profileDataManager,
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
        this.newOutletHelper = newOutletHelper;
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
                        ArrayList<NewOutletBO>,
                        LinkedHashMap<Integer, ArrayList<LocationBO>>,
                        HashMap<String, String>,
                        Vector<RetailerMasterBO>,
                        Boolean, Boolean>() {
                    @Override
                    public Boolean apply(
                            ArrayList<NewOutletBO> contactStatus,
                            LinkedHashMap<Integer, ArrayList<LocationBO>> locationListByLevId,
                            HashMap<String, String> previousProfileChanges,
                            Vector<RetailerMasterBO> downloadLinkRetailer,
                            Boolean aBoolean) throws Exception {
                        mContactStatus = contactStatus;
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
                            System.out.println("mContactStatusSize-->" + mContactStatus.size());
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


    private void getAtrributeList() {
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.downloadCommonAttributeList(),
                mProfileDataManager.downloadChannelWiseAttributeList(),
                mProfileDataManager.downloadAttributeListForRetailer(retailerMasterBO.getRetailerID()),
                mProfileDataManager.downloadEditAttributeList(retailerMasterBO.getRetailerID()),
                mProfileDataManager.downloadRetailerChildAttribute(),
                new Function5<ArrayList<Integer>, ChannelWiseAttributeList, ArrayList<NewOutletAttributeBO>,
                        ArrayList<NewOutletAttributeBO>, ArrayList<NewOutletAttributeBO>, Boolean>() {
                    @Override
                    public Boolean apply(
                            ArrayList<Integer> commonAttributeList,
                            ChannelWiseAttributeList channelWiseAttributeModel,
                            ArrayList<NewOutletAttributeBO> attributeListForRetailer,
                            ArrayList<NewOutletAttributeBO> editedAttributeList,
                            ArrayList<NewOutletAttributeBO> attributeBOArrayListChild) throws Exception {

                        mCommonAttributeList = commonAttributeList;
                        //Below both list come from ChannelWiseAtttributeModel.class
                        mAttributeListByLocationID = channelWiseAttributeModel.getAttributeListByLocationID();
                        mAttributeBOListByLocationID = channelWiseAttributeModel.getAttributeBOListByLocationID();
                        //Below both function just update in retailer MasterBo for future use
                        retailerMasterBO.setAttributeBOArrayList(attributeListForRetailer);
                        mEditAttributeList = editedAttributeList;
                        mAttributeChildList = attributeBOArrayListChild;
                        return true;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        downloadAttributeParent();
                    }
                }));

    }


    private HashMap<Integer, ArrayList<NewOutletAttributeBO>> getAttributeBOListByLocationID() {
        return mAttributeBOListByLocationID;
    }


    private void downloadAttributeParent() {
        getCompositeDisposable().add(mProfileDataManager.downloadAttributeParentList(mAttributeChildList)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<NewOutletAttributeBO>>() {
                    @Override
                    public void onNext(ArrayList<NewOutletAttributeBO> attributeParentList) {
                        mAttributeParentList = attributeParentList;
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("ProfileEditPresenterImp.." + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        getAttributeMap();
                        updateUserMasterAttribute();
                    }
                }));
    }


    private void updateUserMasterAttribute() {
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.updateRetailerMasterAttribute(mEditAttributeList, mAttributeChildList, mAttributeParentList),
                mProfileDataManager.updateRetailerMasterAttribute(retailerMasterBO.getAttributeBOArrayList(), mAttributeChildList, mAttributeParentList),
                new BiFunction<ArrayList<NewOutletAttributeBO>, ArrayList<NewOutletAttributeBO>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<NewOutletAttributeBO> mTempList,
                                         ArrayList<NewOutletAttributeBO> mattributeList) throws Exception {
                        mAttributeList = mattributeList;
                        getTempList(mTempList);
                        return true;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        getProfileEditDetails();
                    }
                })
        );
    }


    private void getTempList(ArrayList<NewOutletAttributeBO> tempList) {
        try {
            if (!tempList.isEmpty()) {
                int size = mAttributeList.size();
                if (mAttributeList.size() > 0) {
                    ArrayList<NewOutletAttributeBO> newOutletAttributeBOS = new ArrayList<>();
                    newOutletAttributeBOS.addAll(mAttributeList);
                    for (int i = 0; i < tempList.size(); i++) {
                        for (int j = 0; j < size; j++) {
                            if (newOutletAttributeBOS.get(j).getParentId() == tempList.get(i).getParentId()
                                    && newOutletAttributeBOS.get(j).getAttrId() == tempList.get(i).getAttrId()
                                    && tempList.get(i).getStatus().equalsIgnoreCase(ProfileConstant.D)) {
                                for (int k = 0; k < mAttributeList.size(); k++)
                                    if (mAttributeList.get(k).getParentId() == tempList.get(i).getParentId()
                                            && mAttributeList.get(k).getAttrId() == tempList.get(i).getAttrId()
                                            && tempList.get(i).getStatus().equalsIgnoreCase(ProfileConstant.D))
                                        mAttributeList.remove(j);
                            } else {
                                if (j == size - 1) mAttributeList.add(tempList.get(i));
                            }
                        }
                    }
                } else mAttributeList.addAll(tempList);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void getAttributeMap() {
        try {
            attribMap = new HashMap<>();
            ArrayList<NewOutletAttributeBO> tempList;
            for (NewOutletAttributeBO parent : mAttributeParentList) {
                tempList = new ArrayList<>();
                for (NewOutletAttributeBO child : mAttributeChildList) {
                    if (parent.getAttrId() == child.getParentId()) {
                        tempList.add(child);
                    }
                }
                attribMap.put(parent.getAttrName(), tempList);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public ArrayList<NewOutletAttributeBO> getRetailerAttribute() {
        if (attributeList == null) {
            attributeList = new ArrayList<>();
        }
        return attributeList;
    }

    public void setRetailerAttribute(ArrayList<NewOutletAttributeBO> list) {
        this.attributeList = list;
    }


    @Override
    public void validateOTP(String type, String value) {

    }

    @Override
    public void updateProfile() {

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
    public ArrayList<NewOutletBO> getContractStatusList(String listName) {
        mContactStatus.add(0, new NewOutletBO(0, listName));
        return mContactStatus;
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


    @Override
    public void checkIsCommonAttributeView() {
        // attributes mapped to channel already are added here
        if (isChannelAvailable()) {
            mChannelAttributeList = new ArrayList<>();
            int subChannelID;
            if (mPreviousProfileChanges.get(ProfileConstant.SUBCHANNEL) != null)
                subChannelID = SDUtil.convertToInt(mPreviousProfileChanges.get(ProfileConstant.SUBCHANNEL));
            else
                subChannelID = retailerMasterBO.getSubchannelid();
            if (mAttributeListByLocationID != null)
                if (mAttributeListByLocationID.get(subChannelID) != null) {
                    mChannelAttributeList.addAll(mAttributeListByLocationID.get(subChannelID));
                }
        }
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


    @SuppressLint("UseSparseArrays")
    @Override
    public HashMap<Integer, ArrayList<Integer>> getAttributeListByLocationId() {
        if (mAttributeListByLocationID == null) {
            mAttributeListByLocationID = new HashMap<>();
        }
        return mAttributeListByLocationID;
    }


    @Override
    public ArrayList<NewOutletAttributeBO> getAttributeParentList() {
        if (mAttributeParentList == null) {
            mAttributeParentList = new ArrayList<>();
        }
        return mAttributeParentList;
    }

    @Override
    public ArrayList<Integer> getCommonAttributeList() {
        if (mCommonAttributeList == null) {
            mCommonAttributeList = new ArrayList<>();
        }
        return mCommonAttributeList;
    }


    @Override
    public int getLevel(int attrId) {
        int count = 0;
        ArrayList<NewOutletAttributeBO> arrayList = mAttributeChildList;
        NewOutletAttributeBO tempBO;
        for (int i = 0; i < arrayList.size(); i++) {
            tempBO = arrayList.get(i);
            int parentID = tempBO.getParentId();
            if (attrId == parentID) {
                attrId = tempBO.getAttrId();
                count++;
            }
        }
        return count;
    }


    @Override
    public ArrayList<NewOutletAttributeBO> getAttributeMapList(String attribName) {
        if (attribMap == null) {
            attribMap = new HashMap<>();
        }
        return attribMap.get(attribName);
    }

    @Override
    public ArrayList<NewOutletAttributeBO> getAttributeList() {
        if (mAttributeList == null) {
            mAttributeList = new ArrayList<>();
        }
        return mAttributeList;
    }

    @Override
    public ArrayList<NewOutletAttributeBO> getAttributeListChild() {
        if (mAttributeChildList == null) {
            mAttributeChildList = new ArrayList<>();
        }
        return mAttributeChildList;
    }


    @Override
    public ArrayList<Integer> getChannelAttributeList() {
        if (mChannelAttributeList == null) {
            mChannelAttributeList = new ArrayList<>();
        }
        return mChannelAttributeList;
    }

    @Override
    public void saveUpdatedProfileEdit() {
        try {
            if (doValidateProdileEdit()) {
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
                saveEditProfile();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void saveEditProfile() {

        getIvyView().showLoading();

        getCompositeDisposable().add(mProfileDataManager.downloadPriorityProductsForRetailerUpdate(retailerMasterBO.getRetailerID())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> mProducts) {

                        ArrayList<StandardListBO> tempList = new ArrayList<>();
                        ArrayList<String> products = mProducts;
                        if (products == null)
                            products = new ArrayList<String>();
                        if (getIvyView().getSelectedPriorityProductList() != null) {
                            for (StandardListBO bo : getIvyView().getSelectedPriorityProductList()) {
                                if (!products.contains(bo.getListID())) {
                                    bo.setStatus("N");
                                    tempList.add(bo);
                                }
                            }
                        }
                        if (mPriorityProductList != null) {
                            if (tempList.size() > 0) {
                                for (StandardListBO bo : mPriorityProductList) {
                                    if (products.contains(bo.getListID())) {
                                        bo.setStatus(ProfileConstant.D);
                                        tempList.add(bo);
                                    }
                                }
                            }
                        }

                        setSelectedPrioProducts(tempList); //step 2
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        setNearByRetailers(getIvyView().getSelectedIds()); //step 1
                        setValues();//step3
                        updateProfileEdit();//step4

                    }
                }));
    }

    private String tid;
    private boolean isData = false;
    private String currentDate;

    private void updateProfileEdit() {

        currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
        tid = userMasterHelper.getUserMasterBO().getUserid()
                + "" + retailerMasterBO.getRetailerID()
                + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        getCompositeDisposable().add(mProfileDataManager.checkHeaderAvailablility(retailerMasterBO.getRetailerID(), currentDate)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                               @Override
                               public void accept(String response) throws Exception {
                                   if (!StringUtils.isEmptyString(response))
                                       tid = response;
                                   prepareProfileEditValues();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Commons.print(throwable.getMessage());
                                   prepareProfileEditValues();
                               }
                           }
                ));
    }

    private void prepareProfileEditValues() {

        for (ConfigureBO configBO : profileConfig) {
            String conficCode = configBO.getConfigCode();
            if (configBO.getModule_Order() == 1) {
                switch (conficCode) {
                    case ProfileConstant.STORENAME:
                        updateStoreName(configBO);
                        break;
                    case ProfileConstant.ADDRESS1:
                        updateAddress1(configBO);
                        break;
                    case ProfileConstant.ADDRESS2:
                        updateAddress2(configBO);
                        break;
                    case ProfileConstant.ADDRESS3:
                        updateAddress3(configBO);
                        break;
                    case ProfileConstant.CITY:
                        updateCity(configBO);
                        break;
                    case ProfileConstant.STATE:
                        updateState(configBO);
                        break;
                    case ProfileConstant.CONTRACT:
                        updateContract(configBO);
                        break;
                    case ProfileConstant.PINCODE:
                        updatePincode(configBO);
                        break;
                    case ProfileConstant.CONTACT_NUMBER:
                        updateContactNumber(configBO);
                        break;
                    case ProfileConstant.CHANNEL:
                        updateChannel(configBO);
                        break;
                    case ProfileConstant.SUBCHANNEL:
                        updateSubChannel(configBO);
                        break;
                    case ProfileConstant.LATTITUDE:
                        updateLatitude(configBO);
                        break;
                    case ProfileConstant.LONGITUDE:
                        updateLongitude(configBO);
                        break;
                    case ProfileConstant.PHOTO_CAPTURE:
                        updatePhotoCapture(configBO);
                        break;
                    case ProfileConstant.RFiled1:
                        updateRFiled1(configBO);
                        break;
                    case ProfileConstant.RField2:
                        updateRFiled2(configBO);
                        break;
                    case ProfileConstant.CREDIT_INVOICE_COUNT:
                        updateCreditInvoiceCount(configBO);
                        break;
                    case ProfileConstant.RField4:
                        updateRfield4(configBO);
                        break;
                    case ProfileConstant.RFIELD5:
                        updateRfield5(configBO);
                        break;
                    case ProfileConstant.RFIELD6:
                        updateRfield6(configBO);
                        break;
                    case ProfileConstant.RFIELD7:
                        updateRfield7(configBO);
                        break;
                    case ProfileConstant.LOCATION01:
                        updateLocation1(configBO);
                        break;
                    case ProfileConstant.CREDITPERIOD:
                        updateCreditPeriod(configBO);
                        break;
                    case ProfileConstant.PROFILE_60:
                        updateProfileImage(configBO);
                        break;
                    case ProfileConstant.GSTN:
                        updateGSTN(configBO);
                        break;
                    case ProfileConstant.INSEZ:
                        updateSezCheckBox(configBO);
                        break;
                    case ProfileConstant.PAN_NUMBER:
                        updatePanNumber(configBO);
                        break;
                    case ProfileConstant.FOOD_LICENCE_NUM:
                        updateFoodLIcenceNumber(configBO);
                        break;
                    case ProfileConstant.DRUG_LICENSE_NUM:
                        updateDrugLIcenceNumber(configBO);
                        break;
                    case ProfileConstant.FOOD_LICENCE_EXP_DATE:
                        updateFoodLcenceExpDate(configBO);
                        break;
                    case ProfileConstant.DRUG_LICENSE_EXP_DATE:
                        updateDrugLcenceExpDate(configBO);
                        break;
                    case ProfileConstant.EMAIL:
                        updateEmail(configBO);
                        break;
                    case ProfileConstant.MOBILE:
                        updateMobile(configBO);
                        break;
                    case ProfileConstant.FAX:
                        updateFax(configBO);
                        break;
                    case ProfileConstant.REGION:
                        updateRegion(configBO);
                        break;
                    case ProfileConstant.COUNTRY:
                        updateCountry(configBO);
                        break;
                    case ProfileConstant.NEARBYRET:
                        updateNearByRetailer();
                        break;
                    case ProfileConstant.PRIORITYPRODUCT:
                        updatePriorityProduct();
                        break;
                    case ProfileConstant.ATTRIBUTE:
                        updateRetailerMasterAttributeList();
                        break;
                    case ProfileConstant.DISTRICT:
                        updateDistrict(configBO);
                        break;
                }
            } else if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                switch (conficCode) {
                    case ProfileConstant.LATTITUDE:
                        updateLatitude(configBO);
                        break;
                    case ProfileConstant.LONGITUDE:
                        updateLongitude(configBO);
                        break;
                }
            }
        }

        if (configurationMasterHelper.IS_CONTACT_TAB) {
            /*Check the RetailerContactList if any changes happened update it*/
            if (newOutletHelper.getRetailerContactList().size() > 0) {
                for (RetailerContactBo retailerContactBo : newOutletHelper.getRetailerContactList()) {
                    if (retailerContactBo.getStatus().equalsIgnoreCase("U")
                            || retailerContactBo.getStatus().equalsIgnoreCase("I")
                            || retailerContactBo.getStatus().equalsIgnoreCase("D")) {
                        updateRetailerContactEditList();
                        break;
                    }
                }
            }
            /*inset the RetailerContactList */
            getCompositeDisposable().add(mProfileDataManager.insertRetailerContactEdit(tid,
                    retailerMasterBO.getRetailerID(),
                    newOutletHelper.getRetailerContactList())
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<Boolean>() {
                                   @Override
                                   public void accept(Boolean response) throws Exception {
                                   }
                               }, new Consumer<Throwable>() {
                                   @Override
                                   public void accept(Throwable throwable) throws Exception {
                                       Commons.print(throwable.getMessage());
                                   }
                               }
                    ));
        }

        /*if (!isData)
            getIvyView().hideLoading();
        else
            updateHeaderList();*/
        updateHeaderList();

    }

    private void updateRetailerContactEditList() {
        String mCustomquery = StringUtils.QT("CONTACTEDIT")
                + "," + StringUtils.QT("1")
                + "," + retailerMasterBO.getRetailerID()
                + "," + retailerMasterBO.getRetailerID() + ")";
        insertRow("CONTACTEDIT", retailerMasterBO.getRetailerID(), mCustomquery);
    }


    private ArrayList<NewOutletAttributeBO> updateRetailerMasterAttribute(ArrayList<NewOutletAttributeBO> list) {

        //Load Child Attribute list which parent is not zero
        ArrayList<NewOutletAttributeBO> childList = mAttributeChildList;
        if (childList == null) {
            childList = new ArrayList<>();
        }

        //Load Parent Attribute List which Parent id is zero
        ArrayList<NewOutletAttributeBO> parentList = mAttributeParentList;
        if (parentList == null) {
            parentList = new ArrayList<>();
        }

        ArrayList<NewOutletAttributeBO> tempList = new ArrayList<>();
        int attribID;
        int tempAttribID;
        int parentID;
        int tempParentID = 0;
        String attribName = "";
        String attribHeader = "";
        int levelId;
        String status;
        NewOutletAttributeBO tempBO;
        for (NewOutletAttributeBO attributeBO : list) {
            tempBO = new NewOutletAttributeBO();
            attribID = attributeBO.getAttrId();
            status = attributeBO.getStatus();
            levelId = attributeBO.getLevelId();
            for (int i = childList.size() - 1; i >= 0; i--) {
                NewOutletAttributeBO attributeBO1 = childList.get(i);
                tempAttribID = attributeBO1.getAttrId();
                if (attribID == tempAttribID) {
                    attribName = attributeBO1.getAttrName();
                    tempParentID = attributeBO1.getParentId();
                    continue;
                }
                if (tempAttribID == tempParentID)
                    tempParentID = attributeBO1.getParentId();
            }

            for (NewOutletAttributeBO attributeBO2 : parentList) {
                parentID = attributeBO2.getAttrId();
                if (tempParentID == parentID)
                    attribHeader = attributeBO2.getAttrName();
            }
            tempBO.setAttrId(attribID);
            tempBO.setParentId(tempParentID);
            tempBO.setAttrName(attribName);
            tempBO.setAttrParent(attribHeader);
            tempBO.setStatus(status);
            tempBO.setLevelId(levelId);
            tempList.add(tempBO);
        }
        return tempList;
    }


    private void updateRetailerMasterAttributeList() {
        if (getIvyView().getSelectedAttribList().size() != 0) {
            ArrayList<NewOutletAttributeBO> tempList = new ArrayList<>();
            ArrayList<NewOutletAttributeBO> attributeList = updateRetailerMasterAttribute(getRetailerAttribute());
            ArrayList<NewOutletAttributeBO> attList = updateRetailerMasterAttribute(retailerMasterBO.getAttributeBOArrayList());
            NewOutletAttributeBO tempBO1;
            NewOutletAttributeBO tempBO2 = null;
            if (attributeList.size() > 0) {
                for (int i = 0; i < attributeList.size(); i++) {
                    tempBO1 = attributeList.get(i);
                    if (attList.size() > 0) {
                        boolean isDiffParent = true;
                        ArrayList<Integer> porcessedAttributes = new ArrayList<>();
                        for (int j = 0; j < attList.size(); j++) {
                            tempBO2 = attList.get(j);
                            if (tempBO1.getParentId() == tempBO2.getParentId()) {
                                if (tempBO1.getAttrId() != tempBO2.getAttrId()) {
                                    tempBO1.setStatus("N");
                                    tempList.add(tempBO1);
                                    tempBO2.setStatus("D");
                                    tempList.add(tempBO2);
                                    isDiffParent = false;
                                    porcessedAttributes.add(tempBO2.getAttrId());
                                }
                            }

                        }
                        /**
                         * add attribute list while change parent id
                         * isDiffParent
                         * true - parentId is mismatched
                         * false - parentId is matched
                         * add previous attribute data
                         * which is not available in processedAttribute list
                         *
                         */
                        if (isDiffParent) {
                            tempBO1.setStatus("N");
                            tempList.add(tempBO1);

                            for (NewOutletAttributeBO bo : attList) {
                                if (!porcessedAttributes.contains(bo.getAttrId())) {
                                    assert tempBO2 != null;
                                    tempBO2.setStatus("D");
                                    tempList.add(tempBO2);
                                }
                            }
                        }

                    } else {
                        tempBO1.setStatus("N");
                        tempList.add(tempBO1);
                    }
                }
            } else {
                for (int j = 0; j < attList.size(); j++) {
                    tempBO2 = attList.get(j);
                    tempBO2.setStatus("D");
                    tempList.add(tempBO2);
                }
            }
            getCompositeDisposable().add(mProfileDataManager.updateRetailerMasterAttribute(tid, retailerMasterBO.getRetailerID(), tempList)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<Boolean>() {
                                   @Override
                                   public void accept(Boolean response) throws Exception {
                                       isData = response;
                                   }
                               }, new Consumer<Throwable>() {
                                   @Override
                                   public void accept(Throwable throwable) throws Exception {
                                       getIvyView().hideLoading();
                                       Commons.print(throwable.getMessage());
                                   }
                               }
                    ));
        }

    }


    private void updatePriorityProduct() {
        getCompositeDisposable().add(mProfileDataManager.updateRetailerEditPriorityProducts(tid,
                retailerMasterBO.getRetailerID(), getSelectedPrioProducts())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   isData = response;
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   getIvyView().hideLoading();
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));

    }


    private void updateNearByRetailer() {

        final HashMap<String, String> temp = new HashMap<>();
        if (getNearByRetailers().size() > 0) {
            isData = true;
            for (RetailerMasterBO bo : getNearByRetailers()) {
                temp.put(bo.getRetailerID(), "N");
            }
        }
        getCompositeDisposable().add(mProfileDataManager.getNearbyRetailerIds(retailerMasterBO.getRetailerID())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> nearbyRetailerIds) {
                        if (nearbyRetailerIds != null) {
                            if (temp.size() > 0) {
                                for (String id : nearbyRetailerIds) {
                                    if (temp.get(id) != null) {
                                        temp.remove(id);
                                    } else {
                                        temp.put(id, "D");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        getCompositeDisposable().add(mProfileDataManager.updateNearByRetailers(tid, retailerMasterBO.getRetailerID(), temp)
                                .subscribeOn(getSchedulerProvider().io())
                                .observeOn(getSchedulerProvider().ui())
                                .subscribe(new Consumer<Boolean>() {
                                               @Override
                                               public void accept(Boolean response) throws Exception {
                                                   isData = response;
                                               }
                                           }, new Consumer<Throwable>() {
                                               @Override
                                               public void accept(Throwable throwable) throws Exception {
                                                   getIvyView().hideLoading();
                                                   Commons.print(throwable.getMessage());
                                               }
                                           }
                                ));
                    }
                }));


    }


    private void updateCountry(ConfigureBO configBO) {
        if ((retailerMasterBO.getCountry() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getCountry() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateRegion(ConfigureBO configBO) {
        if ((retailerMasterBO.getRegion() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getRegion() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateFax(ConfigureBO configBO) {
        if ((retailerMasterBO.getFax() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getFax() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateMobile(ConfigureBO configBO) {
        if ((retailerMasterBO.getMobile() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getMobile() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateEmail(ConfigureBO configBO) {
        if ((retailerMasterBO.getEmail() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getEmail() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateDrugLcenceExpDate(ConfigureBO configBO) {
        if ((retailerMasterBO.getDLNoExpDate() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getDLNoExpDate() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateFoodLcenceExpDate(ConfigureBO configBO) {
        if ((retailerMasterBO.getFoodLicenceExpDate() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getFoodLicenceExpDate() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateDrugLIcenceNumber(ConfigureBO configBO) {
        if ((retailerMasterBO.getDLNo() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getDLNo() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateFoodLIcenceNumber(ConfigureBO configBO) {
        if ((retailerMasterBO.getFoodLicenceNo() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getFoodLicenceNo() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updatePanNumber(ConfigureBO configBO) {
        if ((retailerMasterBO.getPanNumber() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getPanNumber() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }


    private void updateSezCheckBox(ConfigureBO configBO) {
        if ((retailerMasterBO.getIsSEZzone() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getIsSEZzone() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateGSTN(ConfigureBO configBO) {
        if ((retailerMasterBO.getGSTNumber() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getGSTNumber() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateProfileImage(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getProfileImagePath() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
                FileUtils.checkFileExist(configBO.getMenuNumber() + "", retailerMasterBO.getRetailerID(), false);

            } else if ((!(retailerMasterBO.getProfileImagePath() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String imagePath = "Profile" + "/" + userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                        + "/" + userMasterHelper.getUserMasterBO().getUserid()
                        + "/" + configBO.getMenuNumber();
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
                String mCustomquery = StringUtils.QT(configBO.getConfigCode()) + "," + StringUtils.QT(imagePath) + ","
                        + retailerMasterBO.getRetailerID() + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
                FileUtils.checkFileExist(configBO.getMenuNumber() + "", retailerMasterBO.getRetailerID(), false);
            }

        }

    }

    private void updateCreditPeriod(ConfigureBO configBO) {
        if ((retailerMasterBO.getCreditDays() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getCreditDays() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getRetailerID()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateLocation1(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("0")) {
            if ((retailerMasterBO.getLocationId() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if (retailerMasterBO.getLocationId() != 0
                    && ((!(retailerMasterBO.getLocationId() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber()))))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRfield7(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRField7() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRField7() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRfield6(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRField6() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {

                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRField6() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRfield5(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRField5() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {

                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRField5() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRfield4(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRField4() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {

                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRField4() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateCreditInvoiceCount(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getCredit_invoice_count() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {

                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getCredit_invoice_count() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRFiled2(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRfield2() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {

                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRfield2() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateRFiled1(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getRField1() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getRField1() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updatePhotoCapture(ConfigureBO configBO) {

        String imagePath = "Profile" + "/" + userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                + "/" + userMasterHelper.getUserMasterBO().getUserid() + "/" + configBO.getMenuNumber();
        String mCustomquery = StringUtils.QT(configBO.getConfigCode()) + ","
                + StringUtils.QT(imagePath) + "," + retailerMasterBO.getAddressid() + "," + retailerMasterBO.getRetailerID() + ")";
        insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);

        FileUtils.checkFileExist(AppUtils.latlongImageFileName + "", retailerMasterBO.getRetailerID(), true);
    }

    private void updateLongitude(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("0.0")) {
            if ((retailerMasterBO.getLongitude() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getLongitude() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateLatitude(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("0.0")) {
            if ((retailerMasterBO.getLatitude() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getLatitude() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateSubChannel(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getSubchannelid() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getSubchannelid() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateChannel(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getChannelID() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getChannelID() + "").equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateContactNumber(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getContactnumber().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getContactnumber().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updatePincode(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getPincode().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getPincode().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateContract(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if ((retailerMasterBO.getContractLovid() + "").equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!(retailerMasterBO.getContractLovid() + "").equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateState(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getState().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getState().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateCity(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getCity().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getCity().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateStoreName(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getRetailerName().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getRetailerName().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getRetailerID()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateAddress1(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getAddress1().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getAddress1().equals(configBO.getMenuNumber()) && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateAddress2(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getAddress2().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getAddress2().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateAddress3(ConfigureBO configBO) {
        if (!configBO.getMenuNumber().equals("")) {
            if (retailerMasterBO.getAddress3().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
                deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
            } else if ((!retailerMasterBO.getAddress3().equals(configBO.getMenuNumber())
                    && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                    || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                    && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {
                String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                        + "," + StringUtils.QT(configBO.getMenuNumber())
                        + "," + retailerMasterBO.getAddressid()
                        + "," + retailerMasterBO.getRetailerID() + ")";
                insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
            }
        }
    }

    private void updateDistrict(ConfigureBO configBO) {
        if ((retailerMasterBO.getDistrict() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) != null) {
            deletePreviousRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID());
        } else if ((!(retailerMasterBO.getDistrict() + "").equals(configBO.getMenuNumber())
                && mPreviousProfileChanges.get(configBO.getConfigCode()) == null)
                || (mPreviousProfileChanges.get(configBO.getConfigCode()) != null
                && (!mPreviousProfileChanges.get(configBO.getConfigCode()).equals(configBO.getMenuNumber())))) {

            String mCustomquery = StringUtils.QT(configBO.getConfigCode())
                    + "," + StringUtils.QT(configBO.getMenuNumber())
                    + "," + retailerMasterBO.getAddressid()
                    + "," + retailerMasterBO.getRetailerID() + ")";
            insertRow(configBO.getConfigCode(), retailerMasterBO.getRetailerID(), mCustomquery);
        }
    }

    private void updateHeaderList() {

        getCompositeDisposable().add(mProfileDataManager.updateRetailer(tid, retailerMasterBO.getRetailerID(), currentDate)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   if (response) {
                                       AppUtils.latlongImageFileName = "";
                                       lat = "";
                                       longitude = "";
                                   }
                                   getIvyView().hideLoading();
                                   getIvyView().showAlert();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   getIvyView().hideLoading();
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));

    }


    private void deletePreviousRow(String configCode, String RetailerId) {
        getCompositeDisposable().add(mProfileDataManager.deleteQuery(configCode, RetailerId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   isData = response;
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   getIvyView().hideLoading();
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));
    }

    private void insertRow(String configCode, String RetailerId, String mCustomquery) {

        getCompositeDisposable().add(mProfileDataManager.insertNewRow(configCode, RetailerId, tid, mCustomquery)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   isData = response;
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   getIvyView().hideLoading();
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));
    }


    public void setSelectedPrioProducts(ArrayList<StandardListBO> selectedPrioProducts) {
        this.selectedPrioProducts = selectedPrioProducts;
    }

    public ArrayList<StandardListBO> getSelectedPrioProducts() {
        return selectedPrioProducts;
    }

    private boolean doValidateProdileEdit() {
        validate = true;
        for (int i = 0; i < profileConfig.size(); i++) {

            String configCode = profileConfig.get(i).getConfigCode();

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
            } else if (configCode.equals(ProfileConstant.ATTRIBUTE)
                    && profileConfig.get(i).getModule_Order() == 1) {
                if (getIvyView().getSelectedAttribList().size() != 0) {
                    validateAttribute();
                }
                break;
            } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(ProfileConstant.EMAIL)
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
                    if (!StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS1) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS2) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.ADDRESS3) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.CONTACT_NUMBER) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
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
                    if (mContactStatus != null)
                        profileConfig.get(i).setMenuNumber(getIvyView().getContractSpinnerSelectedItemListId() + "");

                } else if (configCode.equals(ProfileConstant.LATTITUDE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(lat)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        //converting big decimal value while Exponential value occur
                        String lattiTude = (lat).contains("E")
                                ? (SDUtil.truncateDecimal(SDUtil.convertToDouble(lat), -1) + "").substring(0, 20)
                                : (lat.length() > 20 ? lat.substring(0, 20) : lat);

                        profileConfig.get(i).setMenuNumber(lattiTude);
                    }
                } else if (configCode.equals(ProfileConstant.LONGITUDE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(longitude)) {
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
                } else if (configCode.equals(ProfileConstant.CITY) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.STATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.CREDITPERIOD) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber(getIvyView().getDynamicEditTextValues(i));
                    }
                } else if (configCode.equals(ProfileConstant.RFiled1) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.RField2) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.CREDIT_INVOICE_COUNT) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    }
                } else if (configCode.equals(ProfileConstant.RField4) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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
                        if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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
                        if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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
                        if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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
                    if (StringUtils.isEmptyString(getIvyView().getDynamicEditTextValues(i))) {
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

                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FOOD_LICENCE_NUM) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.DRUG_LICENSE_NUM) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FOOD_LICENCE_EXP_DATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))) ||
                            getIvyView().getFoodLicenceExpDateValue().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(
                                AppUtils.validateInput(getIvyView().getFoodLicenceExpDateValue())));
                    }
                } else if (configCode.equals(ProfileConstant.DRUG_LICENSE_EXP_DATE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))) ||
                            getIvyView().getDrugLicenceExpDateValue().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(
                                AppUtils.validateInput(getIvyView().getDrugLicenceExpDateValue())));
                    }

                } else if (configCode.equals(ProfileConstant.EMAIL) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.MOBILE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.FAX) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.REGION) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.COUNTRY) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(StringUtils.removeQuotes(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i))));
                    }
                } else if (configCode.equals(ProfileConstant.DISTRICT) && profileConfig.get(i).getModule_Order() == 1) {
                    if (StringUtils.isEmptyString(AppUtils.validateInput(getIvyView().getDynamicEditTextValues(i)))) {
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

    private void validateAttribute() {

        boolean isAdded = true;
        ArrayList<NewOutletAttributeBO> selectedAttributeLevel = new ArrayList<>();

        try {
            // to check all common mandatory attributes selected
            for (NewOutletAttributeBO attributeBO : getAttributeParentList()) {

                if (getCommonAttributeList().contains(attributeBO.getAttrId())) {

                    NewOutletAttributeBO tempBO = getIvyView().getSelectedAttribList().get(attributeBO.getAttrId());

                    if (attributeBO.getIsMandatory() == 1) {
                        if (tempBO != null && tempBO.getAttrId() != -1) {
                            selectedAttributeLevel.add(tempBO);
                        } else {
                            isAdded = false;
                            String errorMessage = attributeBO.getAttrName() + " is Mandatory";
                            getIvyView().profileEditShowMessage(R.string.attribute, errorMessage);
                            break;
                        }
                    } else {
                        if (tempBO != null && tempBO.getAttrId() != -1)
                            selectedAttributeLevel.add(tempBO);
                    }
                }
            }
            //to check all mandatory channel's attributes selected
            if (isChannelAvailable() && isAdded) {

                try {
                    for (NewOutletAttributeBO attributeBo : getAttributeBOListByLocationID().get(getIvyView().subChannelGetSelectedItem())) {

                        NewOutletAttributeBO tempBO = getIvyView().getSelectedAttribList().get(attributeBo.getAttrId());

                        if (attributeBo.getIsMandatory() == 1) {
                            if (tempBO != null && tempBO.getAttrId() != -1) {
                                selectedAttributeLevel.add(tempBO);
                            } else {
                                isAdded = false;
                                String errorMessage = attributeBo.getAttrName() + " is Mandatory";
                                getIvyView().profileEditShowMessage(R.string.attribute, errorMessage);
                                break;
                            }
                        } else {
                            if (tempBO != null && tempBO.getAttrId() != -1)
                                selectedAttributeLevel.add(tempBO);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!isAdded) {
                validate = false;
            }
            setRetailerAttribute(selectedAttributeLevel);
        } catch (Exception e) {
            getIvyView().hideLoading();
            Commons.printException(e);
        }
    }


    @Override
    public void verifyOTP(final String mType, final String mValue) {

        getCompositeDisposable().add(mProfileDataManager.generateOtpUrl()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                        /* if(!AppUtils.isEmptyString(s)){
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
                                   if (response && !StringUtils.isEmptyString(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().imageViewOnClick(userMasterHelper.getUserMasterBO().getUserid(), path, response);
                                   } else if (!StringUtils.isEmptyString(imagePath)) {
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
                                   if (response && !StringUtils.isEmptyString(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().createImageView(path);
                                   } else if (!StringUtils.isEmptyString(imagePath)) {
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
                !comparConfigerCode(mConfigCode, ProfileConstant.GSTN)) {   /*EMAIL, PenNumber,GST*/
            //regex
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //getIvyView().checkRegex(profileConfig.get(mNumber).getRegex());
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
                || comparConfigerCode(mConfigCode, ProfileConstant.REGION)
                || comparConfigerCode(mConfigCode, ProfileConstant.COUNTRY)
                || comparConfigerCode(mConfigCode, ProfileConstant.CONTACT_NUMBER)
                || comparConfigerCode(mConfigCode, ProfileConstant.MOBILE)
                || comparConfigerCode(mConfigCode, ProfileConstant.FAX)
                || comparConfigerCode(mConfigCode, ProfileConstant.CREDITPERIOD)
                || comparConfigerCode(mConfigCode, ProfileConstant.DISTRICT)) {

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
                    case ProfileConstant.ATTRIBUTE:
                        //getIvyView().createAttributeView(0);
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
                }
            } else {
                //write the code here  for without flag and order condition
            }
        }

    }

    private void prepareDrugLiceneExpDate() {
        if (StringUtils.isEmptyString(retailerMasterBO.getDLNoExpDate()))
            retailerMasterBO.setDLNoExpDate("Select Date");
        String text = retailerMasterBO.getDLNoExpDate();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        getIvyView().createDrugLicenseExpDate(mName, mNumber, text);
        ;
    }

    private void prepareFoodLiceneExpDate() {
        if (StringUtils.isEmptyString(retailerMasterBO.getFoodLicenceExpDate()))
            retailerMasterBO.setFoodLicenceExpDate("Select Date");
        String text = retailerMasterBO.getFoodLicenceExpDate();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        getIvyView().createFoodLicenceExpDate(mName, mNumber, text);
    }


    private void prepareCountry() {
        if (StringUtils.isEmptyString(retailerMasterBO.getCountry()))
            retailerMasterBO.setCountry("");
        String text = retailerMasterBO.getCountry();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareRegion() {
        if (StringUtils.isEmptyString(retailerMasterBO.getRegion()))
            retailerMasterBO.setRegion("");
        String text = retailerMasterBO.getRegion();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFAX() {
        if (StringUtils.isEmptyString(retailerMasterBO.getFax()))
            retailerMasterBO.setFax("");
        String text = retailerMasterBO.getFax();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareMobile() {
        if (StringUtils.isEmptyString(retailerMasterBO.getMobile()))
            retailerMasterBO.setMobile("");
        String text = retailerMasterBO.getMobile();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareEmail() {
        if (StringUtils.isEmptyString(retailerMasterBO.getEmail()))
            retailerMasterBO.setEmail("");
        String text = retailerMasterBO.getEmail();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareDrugLicenseNumber() {
        if (StringUtils.isEmptyString(retailerMasterBO.getDLNo()))
            retailerMasterBO.setDLNo("");
        String text = retailerMasterBO.getDLNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFoodLicenceNumber() {

        if (StringUtils.isEmptyString(retailerMasterBO.getFoodLicenceNo()))
            retailerMasterBO.setFoodLicenceNo("");
        String text = retailerMasterBO.getFoodLicenceNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);

    }


    private void preparePanNumber() {
        if (StringUtils.isEmptyString(retailerMasterBO.getPanNumber()))
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
        if (StringUtils.isEmptyString(retailerMasterBO.getRetailerName()))
            retailerMasterBO.setRetailerName("");
        String retailderName = retailerMasterBO.getRetailerName() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(retailderName))
                retailderName = mPreviousProfileChanges.get(configCode);

        checkConfigrationForEditText(mNumber, configCode, mName, retailderName);
    }


    private void prepareAddress1() {
        if (StringUtils.isEmptyString(retailerMasterBO.getAddress1()))
            retailerMasterBO.setAddress1("");
        String text = retailerMasterBO.getAddress1() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress2() {
        if (StringUtils.isEmptyString(retailerMasterBO.getAddress2()))
            retailerMasterBO.setAddress2("");
        String text = retailerMasterBO.getAddress2() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress3() {
        if (StringUtils.isEmptyString(retailerMasterBO.getAddress3()))
            retailerMasterBO.setAddress3("");
        String text = retailerMasterBO.getAddress3() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareCity() {
        if (StringUtils.isEmptyString(retailerMasterBO.getCity()))
            retailerMasterBO.setCity("");
        String text = retailerMasterBO.getCity() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.CITY + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareState() {
        if (StringUtils.isEmptyString(retailerMasterBO.getState()))
            retailerMasterBO.setState("");
        String text = retailerMasterBO.getState() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.STATE + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void preparePincode() {
        if (StringUtils.isEmptyString(retailerMasterBO.getPincode()))
            retailerMasterBO.setPincode("");
        String text = retailerMasterBO.getPincode() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.PINCODE + "," + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareContectNumber() {
        if (StringUtils.isEmptyString(retailerMasterBO.getContactnumber()))
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
        if (StringUtils.isEmptyString(retailerMasterBO.getDistrict()))
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

