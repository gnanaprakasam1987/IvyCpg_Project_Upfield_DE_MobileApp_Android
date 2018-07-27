package com.ivy.ui.profile.edit.presenter;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.text.InputType;
import android.util.SparseArray;


import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;

import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ChannelMasterHelper;

import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.ProfileConstant;
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
import io.reactivex.functions.Consumer;
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
    }

    @Override
    public void downLoadDataFromDataBase() {

        getProfileEditDownloadedList();
        profileConfig = configurationMasterHelper.getProfileModuleConfig();
        IS_UPPERCASE_LETTER = configurationMasterHelper.IS_UPPERCASE_LETTER;
        /*First level  looping for prepare condition */
        for (ConfigureBO configureBO : profileConfig) {
            if ((configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.PROFILE_08) && configureBO.isFlag() == 1)
                    || (configureBO.getConfigCode().equalsIgnoreCase(ProfileConstant.PROFILE_31) && configureBO.isFlag() == 1)) {
                isLatLong = true;
                lat = retailerMasterBO.getLatitude() + "";
                longitude = retailerMasterBO.getLongitude() + "";
            }
        }
        if (profileConfig.size() != 0) {
            //Check the Profile Image config is enable or not using PROFILE60
            if (profileConfig.get(0).getConfigCode().equals(ProfileConstant.PROFILE_60) &&
                    (profileConfig.get(0).isFlag() == 1) &&
                    (profileConfig.get(0).getModule_Order() == 1)) {
                prepareProfileImage();
            }
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
                    profileConfig.add(new ConfigureBO(ProfileConstant.PROFILE_08, "Latitude", lat, 0, 0, 0));
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

                        if(stringHashMap.size()>0){
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
                        }else{
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
        Vector<RetailerMasterBO> retailersList= mLinkRetailerListByDistributorId.get(retailerMasterBO.getDistributorId());
        getIvyView().retailersButtonOnClick(retailersList,configurationMasterHelper.VALUE_NEARBY_RETAILER_MAX);
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


    private void dataPreparation() {
        //Second Level looping for prepare view
        for (int i = 0; i < profileConfig.size(); i++) {
            int mNumber = i;
            int flag = profileConfig.get(i).isFlag();
            int Order = profileConfig.get(i).getModule_Order();
            String mName = profileConfig.get(i).getMenuName();
            String configCode = profileConfig.get(i).getConfigCode();

            if (comparConfigerCode(configCode, ProfileConstant.PROFILE_02) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getRetailerName()))
                    retailerMasterBO.setRetailerName("");
                String retailderName = retailerMasterBO.getRetailerName() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(retailderName))
                        retailderName = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, retailderName);
            } else if (configCode.equals(ProfileConstant.PROFILE_03) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getAddress1()))
                    retailerMasterBO.setAddress1("");
                String text = retailerMasterBO.getAddress1() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            } else if (configCode.equals(ProfileConstant.PROFILE_04) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getAddress2()))
                    retailerMasterBO.setAddress2("");
                String text = retailerMasterBO.getAddress2() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            } else if (configCode.equals(ProfileConstant.PROFILE_05) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getAddress3()))
                    retailerMasterBO.setAddress3("");
                String text = retailerMasterBO.getAddress3() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            } else if (configCode.equals(ProfileConstant.PROFILE_39) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getCity()))
                    retailerMasterBO.setCity("");
                String text = retailerMasterBO.getCity() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                Commons.print(ProfileConstant.PROFILE_39 + "" + profileConfig.get(i).getModule_Order());
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            } else if (configCode.equals(ProfileConstant.PROFILE_40) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getState()))
                    retailerMasterBO.setState("");
                String text = retailerMasterBO.getState() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                Commons.print(ProfileConstant.PROFILE_40 + "" + profileConfig.get(i).getModule_Order());
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            } else if (configCode.equals(ProfileConstant.PROFILE_38) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getPincode()))
                    retailerMasterBO.setPincode("");
                String text = retailerMasterBO.getPincode() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                Commons.print(ProfileConstant.PROFILE_38 + "," + "" + profileConfig.get(i).getModule_Order());
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_30) && flag == 1 && Order == 1) {
                if (AppUtils.isEmptyString(retailerMasterBO.getContactnumber()))
                    retailerMasterBO.setContactnumber("");
                String text = retailerMasterBO.getContactnumber() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_06) && flag == 1 && Order == 1) {
                int id = retailerMasterBO.getChannelID();
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                        id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
                channelMaster = channelMasterHelper.getChannelMaster();
                getIvyView().createSpinnerView(channelMaster, mNumber, mName, configCode, id);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_07) && flag == 1 && Order == 1) {
                int id = retailerMasterBO.getSubchannelid();
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                        id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
                getIvyView().createSpinnerView(mNumber, mName, configCode, id);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_43) && flag == 1 && Order == 1) {
                int id = retailerMasterBO.getContractLovid();
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(id + ""))
                        id = SDUtil.convertToInt(mPreviousProfileChanges.get(configCode));
                getIvyView().createSpinnerView(mNumber, mName, configCode, id);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_08) && flag == 1 && Order == 1) {
                String textLat = retailerMasterBO.getLatitude() + "";
                @NonNls String MenuName = "LatLong";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(textLat))
                        textLat = mPreviousProfileChanges.get(configCode);
                for (int j = 0; j < profileConfig.size(); j++) {
                    if (profileConfig.get(j).getConfigCode().equals(ProfileConstant.PROFILE_31)
                            && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                        String textLong = retailerMasterBO.getLongitude() + "";
                        if (mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode()) != null)
                            if (!mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode()).equals(textLong))
                                textLong = mPreviousProfileChanges.get(profileConfig.get(j).getConfigCode());
                        String text = textLat + ", " + textLong;
                        getIvyView().createLatlongTextView(mNumber, MenuName, text);
                    }
                }
            }
            else if (configCode.equals(ProfileConstant.PROFILE_63) && flag == 1 && Order == 1) {
                getIvyView().isLatLongCameravailable(true);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_13) && flag == 1 && Order == 1) {
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
            else if (configCode.equals(ProfileConstant.PROFILE_14) && flag == 1 && Order == 1) {
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
            else if (configCode.equals(ProfileConstant.PROFILE_15) && flag == 1 && Order == 1) {
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
            else if (configCode.equals(ProfileConstant.PROFILE_36)) {
                if (!retailerMasterBO.getIsNew().equals("Y"))
                    if (getNearByRetailers() != null)
                        getNearByRetailers().clear();
                getIvyView().createNearByRetailerView(mNumber, mName, true);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_25) && flag == 1 && Order == 1) {

                String text = retailerMasterBO.getCreditDays() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_20) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRField1() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_26) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRfield2() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_27) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getCredit_invoice_count() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                checkConfigrationForEditText(mNumber, configCode, mName, text);
            }
            else if (configCode.equals(ProfileConstant.PROFILE_28) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRField4() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                if (profileConfig.get(i).getHasLink() == 0)
                    checkConfigrationForEditText(mNumber, configCode, mName, text);
                else {
                    if (text.equals(""))
                        text = "0";
                    getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
                }
            }
            else if (configCode.equals(ProfileConstant.PROFILE_53) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRField5() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                if (profileConfig.get(i).getHasLink() == 0)
                    checkConfigrationForEditText(mNumber, configCode, mName, text);
                else {
                    if (text.equals(""))
                        text = "0";
                    getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
                }
            }
            else if (configCode.equals(ProfileConstant.PROFILE_54) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRField6() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                if (profileConfig.get(i).getHasLink() == 0)
                    checkConfigrationForEditText(mNumber, configCode, mName, text);
                else {
                    if (text.equals(""))
                        text = "0";
                    getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
                }
            }
            else if (configCode.equals(ProfileConstant.PROFILE_55) && flag == 1 && Order == 1) {
                String text = retailerMasterBO.getRField7() + "";
                if (mPreviousProfileChanges.get(configCode) != null)
                    if (!mPreviousProfileChanges.get(configCode).equals(text))
                        text = mPreviousProfileChanges.get(configCode);
                if (profileConfig.get(i).getHasLink() == 0)
                    checkConfigrationForEditText(mNumber, configCode, mName, text);
                else {
                    if (text.equals(""))
                        text = "0";
                    getIvyView().createSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text));
                }
            }

        }
    }


    private void checkConfigrationForEditText(int mNumber, String configCode, String menuName,
                                              String values) {

        String mConfigCode = profileConfig.get(mNumber).getConfigCode();

        if (!comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_78) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_81) ||
                !comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_61)) {   /*Email, PenNumber,GST*/
            //regex
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            getIvyView().checkRegex(profileConfig.get(mNumber).getRegex());
        }
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_81)) {  /*PanNumber*/
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkPANRegex(mNumber);
        }
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_61)) {   /*GST*/
            getIvyView().addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkGSTRegex(mNumber);
        }
        /* STORENAME,ADDRESS1,ADDRESS2,ADDRESS3,RetailerAddressCity,RFiled1,RField2
         Contract Type,RField4,RFIELD5,RFIELD6,RFIELD7,STATE,PINCODE,GSTN Number
         pan_number,FOOD_LICENCE_NUM,DRUG_LICENSE_NUM,Email,REGION,COUNTRY*/
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_02)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_03)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_04)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_05)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_39)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_20)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_26)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_27)
                || (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_28) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_53) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_54) && profileConfig.get(mNumber).getHasLink() == 0)
                || (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_55) && profileConfig.get(mNumber).getHasLink() == 0)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_40)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_38)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_61)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_81)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_82)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_84)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_78)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_87)
                || comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_88)) {

            int Mandatory = profileConfig.get(mNumber).getMandatory();
            int MAX_CREDIT_DAYS = configurationMasterHelper.MAX_CREDIT_DAYS;
            getIvyView().createEditTextView(mNumber, configCode,
                    menuName, values, IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS);
        }

         /*ContactNumber,PHNO1,PHNO2,MOBILE,FAX*/
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_30) ||
                comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_79) ||
                comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_86)) {
            int Mandatory = profileConfig.get(mNumber).getMandatory();
            int MAX_CREDIT_DAYS = configurationMasterHelper.MAX_CREDIT_DAYS;
            getIvyView().createEditTextView(mNumber, configCode,
                    menuName, values, IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS);
        }

        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_25)) {
            int Mandatory = profileConfig.get(mNumber).getMandatory();
            int MAX_CREDIT_DAYS = configurationMasterHelper.MAX_CREDIT_DAYS;
            getIvyView().createEditTextView(mNumber, configCode,
                    menuName, values, IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS);
        }

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

                               }
                           }
                ));
    }


    private void getProfileEditDownloadedList() {
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
                    public Boolean apply(ArrayList<NewOutletBO> ContactTitle,
                                         ArrayList<NewOutletBO> ContactStatus,
                                         LinkedHashMap<Integer, ArrayList<LocationBO>> LocationListByLevId,
                                         HashMap<String, String> PreviousProfileChanges,
                                         Vector<RetailerMasterBO> DownloadLinkRetailer,
                                         Boolean aBoolean) throws Exception {
                        ProfileEditPresenterImp.this.mContactTitle = ContactTitle;
                        ProfileEditPresenterImp.this.mContactStatus = ContactStatus;
                        ProfileEditPresenterImp.this.mLocationListByLevId = LocationListByLevId;
                        ProfileEditPresenterImp.this.mPreviousProfileChanges = PreviousProfileChanges;
                        ProfileEditPresenterImp.this.mDownloadLinkRetailer = DownloadLinkRetailer;
                        getLocation(); //Get Location
                        return true;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                           /* System.out.println("mContactTitleSize-->"+mContactTitle.size());
                            System.out.println("mContactStatusSize-->"+mContactStatus.size());
                            System.out.println("mLocationListByLevIdize-->"+mLocationListByLevId.size());
                            System.out.println("mPreviousProfileChangesSize-->"+mPreviousProfileChanges.size());
                            System.out.println("mDownloadLinkRetailerSize-->"+mDownloadLinkRetailer.size());
                          */
                            downloadLinkRetailer();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        dataPreparation();
                        getIvyView().hideLoading();
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

}

