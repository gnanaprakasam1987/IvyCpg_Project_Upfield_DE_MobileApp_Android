package com.ivy.ui.profile.edit.presenter;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;

import android.util.SparseArray;


import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;

import com.ivy.location.LocationUtil;
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
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.data.ChannelWiseAttributeList;
import com.ivy.ui.profile.data.IProfileDataManager;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.utils.AppUtils;
import com.ivy.utils.rx.SchedulerProvider;


import org.jetbrains.annotations.NonNls;

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
import io.reactivex.functions.Function6;

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

    private ArrayList<NewOutletBO> mContactTitle;
    private ArrayList<NewOutletBO> mContactStatus;
    private LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevId;
    private HashMap<String, String> mPreviousProfileChanges;
    private Vector<RetailerMasterBO> mDownloadLinkRetailer;
    private ArrayList<RetailerFlexBO> downloadRetailerFlexValues;
    private Vector<RetailerMasterBO> RetailerMasterList;


    /*Location ArrayList*/
    private ArrayList<LocationBO> mLocationMasterList1 = null;
    private ArrayList<LocationBO> mLocationMasterList2 = null;
    private ArrayList<LocationBO> mLocationMasterList3 = null;
    private SparseArray<Vector<RetailerMasterBO>> mLinkRetailerListByDistributorId;
    private Vector<RetailerMasterBO> nearByRetailers = new Vector<>();
    private Vector<ConfigureBO> profileConfig = null;
    private Vector<ChannelBO> channelMaster = null;

    /*Attributes */
    private ArrayList<Integer> mCommonAttributeList;
    private HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID = null;
    private HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID = null;
    private ArrayList<NewOutletAttributeBO> mEditAttributeList = null;
    private ArrayList<NewOutletAttributeBO> mAttributeChildList = null;
    private ArrayList<NewOutletAttributeBO> mAttributeParentList = null;
    private ArrayList<NewOutletAttributeBO> mAttributeList = null;
    private HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap = null;
    private ArrayList<Integer> mChannelAttributeList = null;// attributes for selected channel already(from DB)..

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
    public void downLoadDataFromDataBase() {

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

            if ((configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.LATTITUDE_LONGITUDE) && configureBO.isFlag() == 1)
                    || (configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.PROFILE_31) && configureBO.isFlag() == 1)) {
                isLatLong = true;
                lat = retailerMasterBO.getLatitude() + "";
                longitude = retailerMasterBO.getLongitude() + "";
            }

            if (configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.ATTRIBUTE) && configureBO.isFlag() == 1)
                downLoadAtrributeList();
        }

        downloadProfileEditList();
    }


    private void downLoadAtrributeList() {
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.downloadCommonAttributeList(),
                mProfileDataManager.downloadChannelWiseAttributeList(),
                mProfileDataManager.downloadAttributeListForRetailer(retailerMasterBO.getRetailerID()),
                mProfileDataManager.downloadEditAttributeList(retailerMasterBO.getRetailerID()),
                mProfileDataManager.downloadRetailerAttribute(),
                new Function5<ArrayList<Integer>, ChannelWiseAttributeList, ArrayList<NewOutletAttributeBO>,
                        ArrayList<NewOutletAttributeBO>, ArrayList<NewOutletAttributeBO>, Boolean>() {
                    @Override
                    public Boolean apply(
                            ArrayList<Integer> commonAttributeList,
                            ChannelWiseAttributeList channelWiseAttributeModel,
                            ArrayList<NewOutletAttributeBO> mAttributeBOArrayList,
                            ArrayList<NewOutletAttributeBO> editAttributeList,
                            ArrayList<NewOutletAttributeBO> attributeBOArrayListChild) throws Exception {

                        mCommonAttributeList = commonAttributeList;
                        //Below both aerialist come from ChannelWiseAtttributeModel.class
                        mAttributeListByLocationID = channelWiseAttributeModel.getAttributeListByLocationID();
                        mAttributeBOListByLocationID = channelWiseAttributeModel.getAttributeBOListByLocationID();
                        //Below both function just update in retailer MasterBo for future use
                        retailerMasterBO.setAttributeBOArrayList(mAttributeBOArrayList);
                        mEditAttributeList = editAttributeList;
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
                        System.out.println("ProfileEditPresenterImp.." + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        downloadAttributeParent();
                    }
                }));

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
                new BiFunction<ArrayList<NewOutletAttributeBO>,
                        ArrayList<NewOutletAttributeBO>, Boolean>() {
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
                        System.out.println("ProfileEditPresenterImp.." + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
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


    @Override
    public void validateOTP(String type, String value) {

    }

    @Override
    public void updateProfile() {

    }

    @Override
    public void imageLongClickListener(boolean isForLatLong) {
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
    public void latlongCameraBtnClickListene(boolean isForLatLong) {
        if (!isForLatLong) {
            imageFileName = "PRO_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
        } else {
            AppUtils.latlongImageFileName = "LATLONG_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
        }
        getIvyView().takePhoto(imageFileName, isForLatLong);
    }

    @Override
    public void isCameraReqestCode() {
        if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
            lat = LocationUtil.latitude + "";
            longitude = LocationUtil.longitude + "";
            if (lat.equals("") || SDUtil.convertToDouble(lat) == 0 || longitude.equals("") || SDUtil.convertToDouble(longitude) == 0) {
                getIvyView().showMessage("Location not captured.");
            } else {
                if (!isLatLong) {
                    profileConfig.add(new ConfigureBO(ProfileConstant.LATTITUDE_LONGITUDE, "Latitude", lat, 0, 0, 0));
                    profileConfig.add(new ConfigureBO(ProfileConstant.PROFILE_31, "Latitude", longitude, 0, 0, 0));
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
    public ArrayList<NewOutletBO> getContractStatusList() {
        return mContactStatus;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList1() {
        return mLocationMasterList1;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList2() {
        return mLocationMasterList2;
    }

    @Override
    public ArrayList<LocationBO> getLocationMasterList3() {
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
    public void downloadRetailerFlexValues(String type) {
        getCompositeDisposable().add(mProfileDataManager.downloadRetailerFlexValues(type)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<RetailerFlexBO>>() {
                    @Override
                    public void onNext(ArrayList<RetailerFlexBO> retailerFlexBOS) {
                        getIvyView().updateRetailerFlexValues(retailerFlexBOS);
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
    public void isCommonAttributeView() {
        // attributes mapped to channel already are added here
        if (isChannelAvailable()) {
            mChannelAttributeList = new ArrayList<>();
            int subChannelID;
            if (mPreviousProfileChanges.get(ProfileConstant.SUBCHANNEL) != null)
                subChannelID = SDUtil.convertToInt(mPreviousProfileChanges.get(ProfileConstant.SUBCHANNEL));
            else
                subChannelID = retailerMasterBO.getSubchannelid();
            if (mAttributeListByLocationID != null)
                mChannelAttributeList.addAll(mAttributeListByLocationID.get(subChannelID));

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
    public void imageOnClickListener() {
        final String imagePath = retailerMasterBO.getProfileImagePath();
        getCompositeDisposable().add(mProfileDataManager.checkProfileImagePath(retailerMasterBO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   if (response && !AppUtils.isEmptyString(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().imageViewOnClick(userMasterHelper.getUserMasterBO().getUserid(), path, response);
                                   } else if (!AppUtils.isEmptyString(imagePath)) {
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
                                   if (response && !AppUtils.isEmptyString(imagePath)) {
                                       imgPaths = imagePath.split("/");
                                       path = imgPaths[imgPaths.length - 1];
                                       getIvyView().createImageView(path);
                                   } else if (!AppUtils.isEmptyString(imagePath)) {
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
                                   System.out.println("ProfileEditPrenterImp" + throwable.getMessage());
                               }
                           }
                ));
    }


    private void downloadProfileEditList() {
        getIvyView().showLoading();
        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.getContactTitle(),
                mProfileDataManager.getContactStatus(),
                mProfileDataManager.getLocationListByLevId(),
                mProfileDataManager.getPreviousProfileChanges(retailerMasterBO.getRetailerID()),
                mProfileDataManager.downloadLinkRetailer(),
                loadContractData(),
                new Function6<ArrayList<NewOutletBO>,
                        ArrayList<NewOutletBO>,
                        LinkedHashMap<Integer, ArrayList<LocationBO>>,
                        HashMap<String, String>,
                        Vector<RetailerMasterBO>,
                        Boolean, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<NewOutletBO> contactTitle,
                                         ArrayList<NewOutletBO> contactStatus,
                                         LinkedHashMap<Integer, ArrayList<LocationBO>> locationListByLevId,
                                         HashMap<String, String> previousProfileChanges,
                                         Vector<RetailerMasterBO> downloadLinkRetailer,
                                         Boolean aBoolean) throws Exception {
                        mContactTitle = contactTitle;
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
                        downloadLinkRetailer();
                        getIvyView().hideLoading();
                    }
                }));
    }


    private void checkConfigrationForEditText(int mNumber, String configCode, String menuName, String values) {

        String mConfigCode = profileConfig.get(mNumber).getConfigCode();
        if (!comparConfigerCode(mConfigCode, ProfileConstant.EMAIL) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.PAN_NUMBER) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.GSTN)) {   /*EMAIL, PenNumber,GST*/
            //regex
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            getIvyView().checkRegex(profileConfig.get(mNumber).getRegex());
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
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_27)
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
                || comparConfigerCode(mConfigCode, ProfileConstant.CREDITPERIOD)) {

            int Mandatory = profileConfig.get(mNumber).getMandatory();
            int MAX_CREDIT_DAYS = configurationMasterHelper.MAX_CREDIT_DAYS;
            getIvyView().createEditTextView(mNumber, configCode, menuName, values, IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS);
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
                    case ProfileConstant.LATTITUDE_LONGITUDE:
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
                    case ProfileConstant.PROFILE_27:
                        prepareProfile27();
                        break;
                    case ProfileConstant.PRIORITYPRODUCT:
                        downloadPriority(mNumber, mName);
                        break;
                    case ProfileConstant.ATTRIBUTE:
                        getIvyView().createAttributeView(0);
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
                }
            } else {
                //write the code here  for without flag and order condition
            }
        }

    }


    private void prepareCountry() {
        if (AppUtils.isEmptyString(retailerMasterBO.getCountry()))
            retailerMasterBO.setCountry("");
        String text = retailerMasterBO.getCountry();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareRegion() {
        if (AppUtils.isEmptyString(retailerMasterBO.getRegion()))
            retailerMasterBO.setRegion("");
        String text = retailerMasterBO.getRegion();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFAX() {
        if (AppUtils.isEmptyString(retailerMasterBO.getFax()))
            retailerMasterBO.setFax("");
        String text = retailerMasterBO.getFax();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareMobile() {
        if (AppUtils.isEmptyString(retailerMasterBO.getMobile()))
            retailerMasterBO.setMobile("");
        String text = retailerMasterBO.getMobile();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareEmail() {
        if (AppUtils.isEmptyString(retailerMasterBO.getEmail()))
            retailerMasterBO.setEmail("");
        String text = retailerMasterBO.getEmail();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareDrugLicenseNumber() {
        if (AppUtils.isEmptyString(retailerMasterBO.getDLNo()))
            retailerMasterBO.setDLNo("");
        String text = retailerMasterBO.getDLNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareFoodLicenceNumber() {

        if (AppUtils.isEmptyString(retailerMasterBO.getFoodLicenceNo()))
            retailerMasterBO.setFoodLicenceNo("");
        String text = retailerMasterBO.getFoodLicenceNo();
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);

    }


    private void preparePanNumber() {
        if (AppUtils.isEmptyString(retailerMasterBO.getPanNumber()))
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
        if (AppUtils.isEmptyString(retailerMasterBO.getRetailerName()))
            retailerMasterBO.setRetailerName("");
        String retailderName = retailerMasterBO.getRetailerName() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(retailderName))
                retailderName = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, retailderName);
    }


    private void prepareAddress1() {
        if (AppUtils.isEmptyString(retailerMasterBO.getAddress1()))
            retailerMasterBO.setAddress1("");
        String text = retailerMasterBO.getAddress1() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress2() {
        if (AppUtils.isEmptyString(retailerMasterBO.getAddress2()))
            retailerMasterBO.setAddress2("");
        String text = retailerMasterBO.getAddress2() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareAddress3() {
        if (AppUtils.isEmptyString(retailerMasterBO.getAddress3()))
            retailerMasterBO.setAddress3("");
        String text = retailerMasterBO.getAddress3() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareCity() {
        if (AppUtils.isEmptyString(retailerMasterBO.getCity()))
            retailerMasterBO.setCity("");
        String text = retailerMasterBO.getCity() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.CITY + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareState() {
        if (AppUtils.isEmptyString(retailerMasterBO.getState()))
            retailerMasterBO.setState("");
        String text = retailerMasterBO.getState() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.STATE + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void preparePincode() {
        if (AppUtils.isEmptyString(retailerMasterBO.getPincode()))
            retailerMasterBO.setPincode("");
        String text = retailerMasterBO.getPincode() + "";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(text))
                text = mPreviousProfileChanges.get(configCode);
        Commons.print(ProfileConstant.PINCODE + "," + "" + profileConfig.get(mNumber).getModule_Order());
        checkConfigrationForEditText(mNumber, configCode, mName, text);
    }


    private void prepareContectNumber() {
        if (AppUtils.isEmptyString(retailerMasterBO.getContactnumber()))
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
        channelMaster = channelMasterHelper.getChannelMaster();
        getIvyView().createSpinnerView(channelMaster, mNumber, mName, configCode, id);
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
        @NonNls String MenuName = "LatLong";
        if (mPreviousProfileChanges.get(configCode) != null)
            if (!mPreviousProfileChanges.get(configCode).equals(textLat))
                textLat = mPreviousProfileChanges.get(configCode);
        for (int j = 0; j < profileConfig.size(); j++) {
            if (profileConfig.get(j).getConfigCode().equals(ProfileConstant.PROFILE_31)
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


    private Observable<Boolean> loadContractData() {
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


    public void downloadLinkRetailer() {
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
    private boolean comparConfigerCode(String configCode, @NonNls String configCodeFromDB) {
        return configCode.equalsIgnoreCase(configCodeFromDB);
    }

    @Override
    public void onDetach() {
        mProfileDataManager.closeDB();
        super.onDetach();

    }
}

