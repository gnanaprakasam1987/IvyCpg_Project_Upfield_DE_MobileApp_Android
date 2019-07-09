package com.ivy.ui.profile.create.presenter;


import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.VolleyError;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.DistributorInfo;
import com.ivy.core.di.scope.RetailerInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.core.model.UrlMaster;
import com.ivy.core.network.IvyNetworkException;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.CensusLocationBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.profile.create.INewRetailerContract;
import com.ivy.ui.profile.create.NewRetailerConstant;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.PaymentType;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.ui.survey.data.SurveyDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subscribers.ResourceSubscriber;

import static com.ivy.sd.png.util.DataMembers.NOTIFY_UPLOAD_ERROR;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS1;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS2;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS3;
import static com.ivy.ui.profile.create.NewRetailerConstant.CHANNEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.CITY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CLASS;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON1;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON2;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_TITLE;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTRACT;
import static com.ivy.ui.profile.create.NewRetailerConstant.COUNTRY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CREDITLIMIT;
import static com.ivy.ui.profile.create.NewRetailerConstant.CREDITPERIOD;
import static com.ivy.ui.profile.create.NewRetailerConstant.DAY_TEXT_LABEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.DISTRIBUTOR;
import static com.ivy.ui.profile.create.NewRetailerConstant.DISTRICT;
import static com.ivy.ui.profile.create.NewRetailerConstant.DRUG_LICENSE_EXP_DATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.DRUG_LICENSE_NUM;
import static com.ivy.ui.profile.create.NewRetailerConstant.EMAIL;
import static com.ivy.ui.profile.create.NewRetailerConstant.FAX;
import static com.ivy.ui.profile.create.NewRetailerConstant.FOOD_LICENCE_EXP_DATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.FOOD_LICENCE_NUM;
import static com.ivy.ui.profile.create.NewRetailerConstant.GST_NO;
import static com.ivy.ui.profile.create.NewRetailerConstant.IN_SEZ;
import static com.ivy.ui.profile.create.NewRetailerConstant.LATLONG;
import static com.ivy.ui.profile.create.NewRetailerConstant.LOCATION;
import static com.ivy.ui.profile.create.NewRetailerConstant.LOCATION1;
import static com.ivy.ui.profile.create.NewRetailerConstant.LOCATION2;
import static com.ivy.ui.profile.create.NewRetailerConstant.MOBILE;
import static com.ivy.ui.profile.create.NewRetailerConstant.NEARBYRET;
import static com.ivy.ui.profile.create.NewRetailerConstant.PAN_NUMBER;
import static com.ivy.ui.profile.create.NewRetailerConstant.PAYMENTTYPE;
import static com.ivy.ui.profile.create.NewRetailerConstant.PHNO1;
import static com.ivy.ui.profile.create.NewRetailerConstant.PHNO2;
import static com.ivy.ui.profile.create.NewRetailerConstant.PINCODE;
import static com.ivy.ui.profile.create.NewRetailerConstant.PLAN;
import static com.ivy.ui.profile.create.NewRetailerConstant.PRIORITYPRODUCT;
import static com.ivy.ui.profile.create.NewRetailerConstant.REGION;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD3;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD4;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD5;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD6;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD7;
import static com.ivy.ui.profile.create.NewRetailerConstant.ROUTE;
import static com.ivy.ui.profile.create.NewRetailerConstant.STATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.STORENAME;
import static com.ivy.ui.profile.create.NewRetailerConstant.SUBCHANNEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.TAXTYPE;
import static com.ivy.ui.profile.create.NewRetailerConstant.TIN_EXP_DATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.TIN_NUM;
import static com.ivy.ui.profile.create.NewRetailerConstant.USER;
import static com.ivy.ui.profile.create.NewRetailerConstant.WEEK_TEXT_LABEL;

public class NewOutletPresenterImpl<V extends INewRetailerContract.INewRetailerView>
        extends BasePresenter<V> implements INewRetailerContract.INewRetailerPresenter<V>, LifecycleObserver {

    private SynchronizationDataManager synchronizationDataManager;
    /*DataManagers*/
    private ProfileDataManager mProfileDataManager;
    private DistributorDataManager distributorDataManager;
    private BeatDataManager beatDataManager;
    private UserDataManager userDataManager;
    private ChannelDataManager channelDataManager;
    private RetailerDataManager retailerDataManager;
    private SurveyDataManager surveyDataManager;

    /*Helper class */
    private ConfigurationMasterHelper configurationMasterHelper;
    private RetailerHelper retailerHelper;

    private NewOutletBO outlet;
    public ArrayList<ConfigureBO> profileConfig = new ArrayList<>();
    private Vector<NewOutletBO> mImageTypeList = new Vector<>();
    private ArrayList<ContractStatus> mContractStatusList = new ArrayList<>();
    private LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevId = new LinkedHashMap<>();

    private ArrayList<ContactTitle> mContactTitleList;
    private ArrayList<BeatMasterBO> beatMaster;
    private ArrayList<PaymentType> paymentTypeList;
    private ArrayList<DistributorMasterBO> mDistributorTypeMasterList;
    private ArrayList<UserMasterBO> mUserList;
    private ArrayList<ChannelBO> channelMasterList;
    private ArrayList<SubchannelBO> subChannelMasterList;
    private ArrayList<StandardListBO> mTaxTypeList;
    private ArrayList<RetailerFlexBO> rField4List;
    private ArrayList<RetailerFlexBO> rField5List;
    private ArrayList<RetailerFlexBO> rField6List;
    private ArrayList<RetailerFlexBO> rField7List;

    private ArrayList<StandardListBO> mClassTypeList = new ArrayList<>();


    private ArrayList<RetailerMasterBO> mNearbyRetailerList;
    private ArrayList<String> mSelectedRetailers;
    private ArrayList<String> mDownloadNearByRetailers;


    //PriorityProduct
    private ArrayList<StandardListBO> priorityProductIDList = new ArrayList<>();
    private ArrayList<StandardListBO> mPriorityProductList = new ArrayList<>();

    private String uID;
    private int leastlocId;
    private double lattitude = 0;
    private double longitude = 0;

    private boolean isLocation1 = false;
    private boolean isUppercaseLetter;
    private boolean isValid = true;
    private boolean isLatLongEnabled;

    /**
     * Spinner Configs Status
     **/
    private boolean isChannelEnabled = false;
    private boolean isContactTitleEnabled = false;
    private boolean isSubChannelEnabled = false;
    private boolean isLocationEnabled = false;
    @VisibleForTesting
    boolean isRouteEnabled = false;
    private boolean isUserEnabled = false;
    private boolean isPaymentTypeEnabled = false;
    private boolean isDistributorEnabled = false;
    private boolean isTaxEnabled = false;
    private boolean isPriorityProductEnabled = false;
    private boolean isClassTypeEnabled = false;
    private boolean isRField4Enabled = false;
    private boolean isRField5Enabled = false;
    private boolean isRField6Enabled = false;
    private boolean isRField7Enabled = false;
    private boolean isContactStatusEnabled = false;

    private V view;
    private NewOutletHelper newOutletHelper;
    private ArrayList<ProductMasterBO> orderedProductList;
    private ArrayList<ProductMasterBO> opportunityProducts;
    private OrderHeader orderHeader;

    @Inject
    public NewOutletPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                  CompositeDisposable compositeDisposable,
                                  ConfigurationMasterHelper configurationMasterHelper,
                                  V view,
                                  ProfileDataManager profileDataManager,
                                  BeatDataManager beatDataManager,
                                  @Profile RetailerHelper retailerHelper,
                                  @Profile NewOutletHelper newOutletHelper,
                                  @DistributorInfo DistributorDataManager distributorDataManager,
                                  @UserInfo UserDataManager userDataManager,
                                  @ChannelInfo ChannelDataManager channelDataManager,
                                  @RetailerInfo RetailerDataManager retailerDataManager,
                                  SurveyDataManager surveyDataManager,
                                  SynchronizationDataManager synchronizationDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mProfileDataManager = profileDataManager;
        this.distributorDataManager = distributorDataManager;
        this.userDataManager = userDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.beatDataManager = beatDataManager;
        this.retailerHelper = retailerHelper;
        this.newOutletHelper = newOutletHelper;
        this.channelDataManager = channelDataManager;
        this.retailerDataManager = retailerDataManager;
        this.surveyDataManager = surveyDataManager;
        this.synchronizationDataManager = synchronizationDataManager;
        this.view = view;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }

    public void setOutlet(NewOutletBO outlet) {
        this.outlet = outlet;
    }

    @Override
    public void onDetach() {
        mProfileDataManager.closeDB();
        mProfileDataManager = null;
        configurationMasterHelper = null;
        super.onDetach();

    }

    @Override
    public void init() {
        if (configurationMasterHelper.SHOW_CAPTURED_LOCATION)
            getIvyView().getLocationPermission();

        uID = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        if ("".equalsIgnoreCase(getIvyView().getRetailerId()))
            getDataManager().setOrderHeaderNote("");
    }

    @Override
    public void getSavedOutletData() {
        if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType()
                || getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            getCompositeDisposable().add(mProfileDataManager.getNewRetailers()
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribeWith(new DisposableObserver<ArrayList<NewOutletBO>>() {
                        @Override
                        public void onNext(ArrayList<NewOutletBO> arrayList) {
                            for (NewOutletBO newOutletBO : arrayList)
                                if (getIvyView().getRetailerId().equalsIgnoreCase(newOutletBO.getRetailerId())) {
                                    setOutlet(newOutletBO);
                                    if (newOutletBO.getImageName() != null)
                                        getIvyView().setImageNameList(newOutletBO.getImageName());

                                    if (newOutletBO.getImageId() != null)
                                        getIvyView().setImageIdList(newOutletBO.getImageId());
                                }

                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.println("NewRetailerPresenterImp.." + e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        }
    }

    @Override
    public void loadInitialData() {
        isUppercaseLetter = configurationMasterHelper.IS_UPPERCASE_LETTER;
        boolean isChannelSelectionNewRetailer = configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER;

        getIvyView().showLoading();

        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.getImageTypeList(),
                mProfileDataManager.getProfileConfigs(getIvyView().getChannelId(),
                        isChannelSelectionNewRetailer, getPrefLanguage()),
                new BiFunction<Vector<NewOutletBO>, ArrayList<ConfigureBO>, Boolean>() {
                    @Override
                    public Boolean apply(Vector<NewOutletBO> imageTypeList, ArrayList<ConfigureBO> profileConfigs) throws Exception {
                        mImageTypeList = imageTypeList;


                        NewOutletPresenterImpl.this.setProfileConfig(profileConfigs);

                        for (ConfigureBO configureBO : profileConfig) {
                            if (configureBO.getConfigCode().equals(CHANNEL))
                                isChannelEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(SUBCHANNEL))
                                isSubChannelEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(CONTACT_TITLE))
                                isContactTitleEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(CONTRACT))
                                isContactStatusEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(LOCATION) || configureBO.getConfigCode().equalsIgnoreCase(LOCATION1) || configureBO.getConfigCode().equalsIgnoreCase(LOCATION2))
                                isLocationEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(ROUTE))
                                isRouteEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(USER))
                                isUserEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(PAYMENTTYPE))
                                isPaymentTypeEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(DISTRIBUTOR))
                                isDistributorEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(TAXTYPE))
                                isTaxEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(PRIORITYPRODUCT))
                                isPriorityProductEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(CLASS))
                                isClassTypeEnabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(RFIELD4) && configureBO.getHasLink() == 1)
                                isRField4Enabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(RFIELD5) && configureBO.getHasLink() == 1)
                                isRField5Enabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(RFIELD6) && configureBO.getHasLink() == 1)
                                isRField6Enabled = true;
                            else if (configureBO.getConfigCode().equalsIgnoreCase(RFIELD7) && configureBO.getHasLink() == 1)
                                isRField7Enabled = true;

                        }
                        return true;
                    }
                })
                .flatMap((Function<Boolean, ObservableSource<ArrayList<ChannelBO>>>) aBoolean -> {
                    if (isChannelEnabled) {
                        return channelDataManager.fetchChannels();
                    }

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<ChannelBO>, ObservableSource<ArrayList<SubchannelBO>>>) channelBOS -> {
                    if (!channelBOS.isEmpty()) {
                        channelMasterList = new ArrayList<>();
                        channelMasterList.addAll(channelBOS);
                    }
                    if (isSubChannelEnabled)
                        return channelDataManager.fetchSubChannels();
                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<SubchannelBO>, ObservableSource<ArrayList<ContactTitle>>>) subChannelBOS -> {
                    if (!subChannelBOS.isEmpty()) {
                        subChannelMasterList = new ArrayList<>();
                        subChannelMasterList.addAll(subChannelBOS);
                    }

                    if (isContactTitleEnabled)
                        return mProfileDataManager.getContactTitle();

                    return Observable.fromCallable(ArrayList::new);

                }).flatMap((Function<ArrayList<ContactTitle>, ObservableSource<LinkedHashMap<Integer, ArrayList<LocationBO>>>>) contactTitles -> {
                    if (!contactTitles.isEmpty()) {
                        mContactTitleList = new ArrayList<>();
                        mContactTitleList.addAll(contactTitles);
                    }
                    if (isLocationEnabled)
                        return mProfileDataManager.getLocationListByLevId();

                    return Observable.fromCallable(LinkedHashMap::new);
                }).flatMap((Function<LinkedHashMap<Integer, ArrayList<LocationBO>>, ObservableSource<ArrayList<BeatMasterBO>>>) locationMap -> {
                    if (!locationMap.isEmpty())
                        mLocationListByLevId = locationMap;

                    if (isRouteEnabled)
                        return beatDataManager.fetchBeats();

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<BeatMasterBO>, ObservableSource<ArrayList<UserMasterBO>>>) beatMasterBOS -> {
                    if (!beatMasterBOS.isEmpty()) {
                        beatMaster = new ArrayList<>();
                        beatMaster.addAll(beatMasterBOS);
                    }
                    if (isUserEnabled)
                        return userDataManager.fetchAllUsers();
                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<UserMasterBO>, ObservableSource<ArrayList<PaymentType>>>) userMasterBOS -> {

                    if (!userMasterBOS.isEmpty()) {
                        mUserList = new ArrayList<>();
                        mUserList.addAll(userMasterBOS);
                    }
                    if (isPaymentTypeEnabled)
                        return mProfileDataManager.getRetailerType();

                    return Observable.fromCallable(ArrayList::new);

                }).flatMap((Function<ArrayList<PaymentType>, ObservableSource<ArrayList<DistributorMasterBO>>>) paymentTypes -> {

                    if (!paymentTypes.isEmpty()) {
                        paymentTypeList = new ArrayList<>();
                        paymentTypeList.addAll(paymentTypes);
                    }
                    if (isDistributorEnabled)
                        return distributorDataManager.fetchDistributorList();

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<DistributorMasterBO>, ObservableSource<ArrayList<StandardListBO>>>) distributorMasterBOS -> {

                    if (!distributorMasterBOS.isEmpty()) {
                        mDistributorTypeMasterList = new ArrayList<>();
                        mDistributorTypeMasterList.addAll(distributorMasterBOS);
                    }
                    if (isTaxEnabled)
                        return mProfileDataManager.getTaxType();

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<StandardListBO>, ObservableSource<ArrayList<StandardListBO>>>) taxList -> {
                    if (!taxList.isEmpty()) {
                        mTaxTypeList = new ArrayList<>();
                        mTaxTypeList.addAll(taxList);
                    }
                    if (isPriorityProductEnabled)
                        return mProfileDataManager.downloadPriorityProducts();

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<StandardListBO>, ObservableSource<ArrayList<StandardListBO>>>) priorityProductList -> {

                    if (isClassTypeEnabled)
                        return mProfileDataManager.downloadClassType();

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<StandardListBO>, ObservableSource<ArrayList<RetailerFlexBO>>>) classTypeList -> {
                    if (!classTypeList.isEmpty()) {
                        mClassTypeList = new ArrayList<>();
                        mClassTypeList.addAll(classTypeList);
                    }
                    if (isRField4Enabled)
                        return mProfileDataManager.downloadRetailerFlexValues(RFIELD4);

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<RetailerFlexBO>, ObservableSource<ArrayList<RetailerFlexBO>>>) retailerFlexBOS -> {
                    if (!retailerFlexBOS.isEmpty()) {
                        rField4List = new ArrayList<>();
                        rField4List.addAll(retailerFlexBOS);
                    }

                    if (isRField5Enabled)
                        return mProfileDataManager.downloadRetailerFlexValues(RFIELD5);

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<RetailerFlexBO>, ObservableSource<ArrayList<RetailerFlexBO>>>) retailerFlexBOS -> {
                    if (!retailerFlexBOS.isEmpty()) {
                        rField5List = new ArrayList<>();
                        rField5List.addAll(retailerFlexBOS);
                    }

                    if (isRField6Enabled)
                        return mProfileDataManager.downloadRetailerFlexValues(RFIELD6);

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<RetailerFlexBO>, ObservableSource<ArrayList<RetailerFlexBO>>>) retailerFlexBOS -> {
                    if (!retailerFlexBOS.isEmpty()) {
                        rField6List = new ArrayList<>();
                        rField6List.addAll(retailerFlexBOS);
                    }

                    if (isRField7Enabled)
                        return mProfileDataManager.downloadRetailerFlexValues(RFIELD7);

                    return Observable.fromCallable(ArrayList::new);
                }).flatMap((Function<ArrayList<RetailerFlexBO>, ObservableSource<ArrayList<ContractStatus>>>) retailerFlexBOS -> {
                    if (!retailerFlexBOS.isEmpty()) {
                        rField7List = new ArrayList<>();
                        rField7List.addAll(retailerFlexBOS);
                    }

                    if (isContactStatusEnabled)
                        return mProfileDataManager.getContactStatus();

                    return Observable.fromCallable(ArrayList::new);
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<ContractStatus>>() {
                    @Override
                    public void onNext(ArrayList<ContractStatus> contractStatuses) {
                        if (!contractStatuses.isEmpty()) {
                            mContractStatusList = new ArrayList<>();
                            mContractStatusList.addAll(contractStatuses);
                        }


                        validateMasters();
                        prepareView();
                    }

                    @Override
                    public void onError(Throwable e) {

                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    private void validateMasters() {

        for (int i = 0; i < profileConfig.size(); i++)
            switch (profileConfig.get(i).getConfigCode()) {
                case NewRetailerConstant.LOCATION:
                    if (outlet != null)
                        leastlocId = outlet.getLocid();
                    if (mLocationListByLevId.get(0) == null || mLocationListByLevId.get(0).size() == 0)
                        getIvyView().showNoLocationsError();
                    break;
                case NewRetailerConstant.LOCATION1:
                    isLocation1 = true;
                    if (getLocation2List().size() == 0)
                        getIvyView().showNoLocationsError();
                    break;
                case NewRetailerConstant.LOCATION2:
                    if (getLocation3List().size() == 0)
                        getIvyView().showNoLocationsError();
                    break;
                default:
                    break;
            }

    }

    private void prepareView() {

        for (int position = 0; position < profileConfig.size(); position++) {

            String configCode = profileConfig.get(position).getConfigCode();
            String mName = profileConfig.get(position).getMenuName();
            boolean mandatory = 1 == profileConfig.get(position).getMandatory();

            if (NewRetailerConstant.CONTACT_TITLE.equalsIgnoreCase(configCode)
                    && 1 == profileConfig.get(position).isFlag()) {
                isContactTitleEnabled = true;
                if (mContactTitleList == null)
                    mContactTitleList = new ArrayList<>();
                mContactTitleList.add(0, new ContactTitle(-1, view.getContactPersonTitle(true)));
                mContactTitleList.addAll(getContactTitleList());
                mContactTitleList.add(mContactTitleList.size(), new ContactTitle(0, view.getContactPersonTitle(false)));
            }

            switch (configCode) {
                case STORENAME:
                case ADDRESS1:
                case ADDRESS2:
                case ADDRESS3:
                case CITY:
                case STATE:
                case DRUG_LICENSE_NUM:
                case FOOD_LICENCE_NUM:
                case REGION:
                case COUNTRY:
                case DISTRICT: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createNewRetailerDetailsField(position, mName, mandatory, isUppercaseLetter, configCode);
                    break;
                }
                case PAN_NUMBER: {
                    getIvyView().addLengthFilter(profileConfig.get(position).getRegex());
                    getIvyView().createNewRetailerDetailsField(position, mName, mandatory, isUppercaseLetter, configCode);
                    break;
                }
                case CONTRACT: {
                    if (mContractStatusList == null || mContractStatusList.isEmpty())
                        getIvyView().showEmptyContactStatusError();
                    getIvyView().createContactSpinner(mandatory, position, mName, configCode);
                    break;
                }
                case CONTACT_PERSON1: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createNewRetailerContactPersonOne(position, mName, mandatory,
                            isUppercaseLetter, configCode, isContactTitleEnabled);
                    break;
                }
                case CONTACT_PERSON2: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createNewRetailerContactPersonTwo(position, mName, mandatory,
                            isUppercaseLetter, configCode, isContactTitleEnabled);
                    break;
                }
                case CREDITPERIOD: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createNewRetailerCreditPeriod(position, mName, mandatory,
                            isUppercaseLetter, configCode);
                    break;
                }
                case EMAIL: {
                    getIvyView().createNewRetailerContactEmail(position, mName, mandatory,
                            isUppercaseLetter, configCode);
                    break;
                }
                case PHNO1:
                case PHNO2:
                case CREDITLIMIT:
                case FAX:
                case MOBILE: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createNewRetailerContactType(position, mName, mandatory,
                            isUppercaseLetter, configCode);
                    break;
                }
                case CHANNEL: {
                    if (channelMasterList == null || channelMasterList.isEmpty())
                        getIvyView().showNoChannelsError();
                    getIvyView().createChannelSpinner(mandatory, position, mName);
                    break;
                }
                case SUBCHANNEL: {
                    if (subChannelMasterList == null || subChannelMasterList.isEmpty())
                        getIvyView().showNoSubChannelsError();
                    getIvyView().createSubChannelSpinner(mandatory, position, mName);
                    break;
                }
                case ROUTE: {
                    if (beatMaster == null || beatMaster.isEmpty())
                        getIvyView().showNoBeatsError();
                    getIvyView().createRouteSpinner(mandatory, position, mName, configCode);
                    break;
                }
                case PAYMENTTYPE: {
                    if (paymentTypeList == null || paymentTypeList.isEmpty())
                        getIvyView().showPaymentTypeListEmptyError();
                    getIvyView().createPaymentType(mandatory, position, mName, configCode);
                    break;
                }
                case DISTRIBUTOR: {
                    if (mDistributorTypeMasterList == null || mDistributorTypeMasterList.isEmpty())
                        getIvyView().showDistributorTypeMasterEmptyError();
                    getIvyView().createDistributor(mandatory, position, mName, configCode);
                    break;
                }
                case TAXTYPE: {
                    if (mTaxTypeList == null || mTaxTypeList.isEmpty())
                        getIvyView().showTaxListEmptyError();
                    getIvyView().createTaxTypeSpinner(mandatory, position, mName, configCode);
                    break;
                }
                case PRIORITYPRODUCT: {

                    getIvyView().createPriorityProductView(mName, mandatory, profileConfig.get(position).getHasLink());
                    break;
                }
                case CLASS: {
                    if (mClassTypeList == null || mClassTypeList.isEmpty())
                        getIvyView().showClassTypeEmptyError();
                    getIvyView().createClassTypeSpinner(mandatory, position, mName, configCode);
                    break;
                }
                case USER: {
                    if (mUserList == null || mUserList.isEmpty())
                        getIvyView().showNoUsersError();
                    getIvyView().createUserSpinner(mandatory, position, mName, configCode);
                    break;
                }
                case RFIELD3: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createRFieldEditText(position, mName, mandatory,
                            isUppercaseLetter, configCode);
                    break;
                }
                case RFIELD4: {
                    if (profileConfig.get(position).getHasLink() == 1)
                        getIvyView().createRField4Spinner(mandatory, position, mName, configCode);
                    else {
                        addLengthAndRegexFilters(position);
                        getIvyView().createRFieldEditText(position, mName, mandatory,
                                isUppercaseLetter, configCode);
                    }
                    break;
                }
                case RFIELD5: {
                    if (profileConfig.get(position).getHasLink() == 1)
                        getIvyView().createRField5Spinner(mandatory, position, mName, configCode);
                    else {
                        addLengthAndRegexFilters(position);
                        getIvyView().createRFieldEditText(position, mName, mandatory,
                                isUppercaseLetter, configCode);
                    }
                    break;
                }
                case RFIELD6: {
                    if (profileConfig.get(position).getHasLink() == 1)
                        getIvyView().createRField6Spinner(mandatory, position, mName, configCode);
                    else {
                        addLengthAndRegexFilters(position);
                        getIvyView().createRFieldEditText(position, mName, mandatory,
                                isUppercaseLetter, configCode);
                    }
                    break;
                }
                case RFIELD7: {
                    if (profileConfig.get(position).getHasLink() == 1)
                        getIvyView().createRField7Spinner(mandatory, position, mName, configCode);
                    else {
                        addLengthAndRegexFilters(position);
                        getIvyView().createRFieldEditText(position, mName, mandatory,
                                isUppercaseLetter, configCode);
                    }
                    break;
                }
                case PLAN: {
                    getIvyView().createDaysAndWeeks(mandatory);
                    break;
                }
                case TIN_NUM: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createTinNum(position, mName, mandatory, isUppercaseLetter, configCode);
                    break;
                }
                case PINCODE: {
                    addLengthAndRegexFilters(position);
                    getIvyView().createPinCode(position, mName, mandatory, isUppercaseLetter, configCode);
                    break;
                }
                case GST_NO: {
                    getIvyView().addLengthFilter(profileConfig.get(position).getRegex());
                    getIvyView().createGstNo(position, mName, mandatory, isUppercaseLetter, configCode);
                    break;
                }
                case LATLONG: {
                    isLatLongEnabled = true;
                    getIvyView().createLatLongTextView(position);
                    break;
                }
                case TIN_EXP_DATE: {
                    getIvyView().createTinExpDataTextView(position, mName, mandatory);
                    break;
                }
                case DRUG_LICENSE_EXP_DATE: {
                    getIvyView().createDrugLicenseExpDataTextView(position, mName, mandatory);
                    break;
                }
                case FOOD_LICENCE_EXP_DATE: {
                    getIvyView().createFoodLicenceExpDataTextView(position, mName, mandatory);
                    break;
                }
                case IN_SEZ: {
                    getIvyView().createSezCheckBox(mName, mandatory);
                    break;
                }
                case NEARBYRET: {
                    getIvyView().createNearByRetailerView(mName, mandatory);
                    break;
                }


            }

            /*if (STORENAME.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.ADDRESS1.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.ADDRESS2.equalsIgnoreCase(configCode)
                    || ADDRESS3.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.CITY.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.STATE.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.PAN_NUMBER.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.DRUG_LICENSE_NUM.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.FOOD_LICENCE_NUM.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.REGION.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.COUNTRY.equalsIgnoreCase(configCode)
                    || NewRetailerConstant.DISTRICT.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerDetailsField(position, mName, mandatory, isUppercaseLetter, configCode);

            } else if (NewRetailerConstant.CONTACT_PERSON1.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerContactPersonOne(position, mName, mandatory,
                        isUppercaseLetter, configCode, isContactTitleEnabled);
            } else if (CONTACT_PERSON2.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerContactPersonTwo(position, mName, mandatory,
                        isUppercaseLetter, configCode, isContactTitleEnabled);
            } else if (NewRetailerConstant.CREDITPERIOD.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerCreditPeriod(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            } else if (NewRetailerConstant.EMAIL.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerContactEmail(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            } else if (PHNO1.equalsIgnoreCase(configCode)
                    || PHNO2.equalsIgnoreCase(configCode)
                    || CREDITLIMIT.equalsIgnoreCase(configCode)
                    || FAX.equalsIgnoreCase(configCode)
                    || MOBILE.equalsIgnoreCase(configCode)) {
                getIvyView().createNewRetailerContactType(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            } else if (NewRetailerConstant.CHANNEL.equalsIgnoreCase(configCode)) {
                if (channelMasterList == null || channelMasterList.isEmpty())
                    getIvyView().showNoChannelsError();
                getIvyView().createChannelSpinner(mandatory, position, mName);
            } else if (NewRetailerConstant.SUBCHANNEL.equalsIgnoreCase(configCode)) {
                if (subChannelMasterList == null || subChannelMasterList.isEmpty())
                    getIvyView().showNoSubChannelsError();
                getIvyView().createSubChannelSpinner(mandatory, position, mName);
            } else
            if (CONTRACT.equalsIgnoreCase(configCode)) {
                getIvyView().createContactSpinner(mandatory, position, mName, configCode);
            } else if (NewRetailerConstant.ROUTE.equalsIgnoreCase(configCode)) {
                if (beatMaster == null || beatMaster.isEmpty())
                    getIvyView().showNoBeatsError();
                getIvyView().createRouteSpinner(mandatory, position, mName, configCode);
            } else*/
            if (NewRetailerConstant.LOCATION.equalsIgnoreCase(configCode)) {
                getIvyView().createLocation1Spinner(mandatory, position, mName, configCode, isLocation1);
            } else if (NewRetailerConstant.LOCATION1.equalsIgnoreCase(configCode)) {
                getIvyView().createLocation2Spinner(mandatory, position, mName, configCode);
            } else if (NewRetailerConstant.LOCATION2.equalsIgnoreCase(configCode)) {
                getIvyView().createLocation3Spinner(mandatory, position, mName, configCode);
            } /*else if (NewRetailerConstant.PAYMENTTYPE.equalsIgnoreCase(configCode)) {
                if (paymentTypeList == null || paymentTypeList.isEmpty())
                    getIvyView().showPaymentTypeListEmptyError();
                getIvyView().createPaymentType(mandatory, position, mName, configCode);
            } else if (NewRetailerConstant.DISTRIBUTOR.equalsIgnoreCase(configCode)) {
                if (mDistributorTypeMasterList == null || mDistributorTypeMasterList.isEmpty())
                    getIvyView().showDistributorTypeMasterEmptyError();
                getIvyView().createDistributor(mandatory, position, mName, configCode);
            } else if (NewRetailerConstant.TAXTYPE.equalsIgnoreCase(configCode)) {
                getIvyView().createTaxTypeSpinner(mandatory, position, mName, configCode);
            } else if (PRIORITYPRODUCT.equalsIgnoreCase(configCode)) {
                getIvyView().createPriorityProductSpinner(mandatory, position, mName, configCode, hasLink);
            } else if (NewRetailerConstant.CLASS.equalsIgnoreCase(configCode)) {
                getIvyView().createClassTypeSpinner(mandatory, position, mName, configCode);
            } else if (NewRetailerConstant.USER.equalsIgnoreCase(configCode)) {
                if (mUserList == null || mUserList.isEmpty())
                    getIvyView().showNoUsersError();
                getIvyView().createUserSpinner(mandatory, position, mName, configCode);
            }  else if (RFIELD4.equalsIgnoreCase(configCode) && hasLink == 1) {
                getIvyView().createRField4Spinner(mandatory, position, mName, configCode);
            }else if (NewRetailerConstant.RFIELD5.equalsIgnoreCase(configCode) && hasLink == 1) {
                getIvyView().createRField5Spinner(mandatory, position, mName, configCode);
            } else if (RFIELD6.equalsIgnoreCase(configCode) && hasLink == 1) {
                getIvyView().createRField6Spinner(mandatory, position, mName, configCode);
            } else if (RFIELD7.equalsIgnoreCase(configCode) && hasLink == 1) {
                getIvyView().createRField7Spinner(mandatory, position, mName, configCode);
            }else if (RFIELD4.equalsIgnoreCase(configCode) && hasLink == 0
                    || RFIELD5.equalsIgnoreCase(configCode) && hasLink == 0
                    || RFIELD6.equalsIgnoreCase(configCode) && hasLink == 0
                    || RFIELD7.equalsIgnoreCase(configCode) && hasLink == 0) {
                getIvyView().createRFieldEditText(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            }else if (RFIELD3.equalsIgnoreCase(configCode)) {
                getIvyView().createRFieldEditText(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            }  else if (NewRetailerConstant.PLAN.equalsIgnoreCase(configCode)) {
                getIvyView().createDaysAndWeeks(mandatory);
            } else if (NewRetailerConstant.TIN_NUM.equalsIgnoreCase(configCode)) {
                getIvyView().createTinNum(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            } else if (PINCODE.equalsIgnoreCase(configCode)) {
                getIvyView().createPinCode(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            }  else if (NewRetailerConstant.GST_NO.equalsIgnoreCase(configCode)) {
                getIvyView().createGstNo(position, mName, mandatory,
                        isUppercaseLetter, configCode);
            } else if (LATLONG.equalsIgnoreCase(configCode)) {
                isLatLongEnabled = true;
                getIvyView().createLatLongTextView(position);
            } else if (TIN_EXP_DATE.equalsIgnoreCase(configCode)) {
                getIvyView().createTinExpDataTextView(position, mName, mandatory);
            } else if (DRUG_LICENSE_EXP_DATE.equalsIgnoreCase(configCode)) {
                getIvyView().createDrugLicenseExpDataTextView(position, mName, mandatory);
            } else if (NewRetailerConstant.FOOD_LICENCE_EXP_DATE.equalsIgnoreCase(configCode)) {
                getIvyView().createFoodLicenceExpDataTextView(position, mName, mandatory);
            } else if (IN_SEZ.equalsIgnoreCase(configCode)) {
                getIvyView().createSezCheckBox(mName, mandatory);
                break;
            }*/
        }

        getIvyView().hideLoading();

    }


    public ArrayList<ConfigureBO> getProfileConfig() {
        return profileConfig;
    }

    public void setProfileConfig(ArrayList<ConfigureBO> profileConfig) {
        this.profileConfig = profileConfig;
    }

    void setContactTitleList(ArrayList<ContactTitle> mContactTitleList) {
        this.mContactTitleList = mContactTitleList;
    }

    void setContactTitleEnabled(boolean iContactTitleEnabled) {
        this.isContactTitleEnabled = iContactTitleEnabled;
    }

    @Override
    public String getUid() {
        return uID;
    }

    public void setUid(String uId) {
        this.uID = uId;
    }

    boolean isDistributorEnabled() {
        return isDistributorEnabled;
    }

    void setDistributorEnabled(boolean distributorEnabled) {
        isDistributorEnabled = distributorEnabled;
    }

    @Override
    public Vector<NewOutletBO> getImageTypeList() {
        return mImageTypeList;
    }

    @Override
    public ArrayList<ContactTitle> getContactTitleList() {
        return mContactTitleList;
    }

    @Override
    public ArrayList<ContractStatus> getContractStatusList() {
        return mContractStatusList;
    }

    @Override
    public String getOutletData(String menuCode) {
        switch (menuCode) {
            case STORENAME:
                return outlet.getOutletName();
            case NewRetailerConstant.ADDRESS1:
                return outlet.getAddress();
            case NewRetailerConstant.CONTACT_PERSON1:
                return outlet.getContactpersonname();
            case NewRetailerConstant.ADDRESS2:
                return outlet.getAddress2();
            case ADDRESS3:
                return outlet.getAddress3();
            case NewRetailerConstant.CITY:
                return outlet.getCity();
            case NewRetailerConstant.STATE:
                return outlet.getState();
            case CONTACT_PERSON2:
                return outlet.getContactpersonname2();
            case PHNO1:
                return outlet.getPhone();
            case PHNO2:
                return outlet.getPhone2();
            case NewRetailerConstant.PLAN:
                return outlet.getVisitDays();
            case FAX:
                return outlet.getFax();
            case NewRetailerConstant.EMAIL:
                return outlet.getEmail();
            case CREDITLIMIT:
                return outlet.getCreditLimit();
            case NewRetailerConstant.TIN_NUM:
                return outlet.getTinno();
            case TIN_EXP_DATE:
                return outlet.getTinExpDate() == null ? "" : outlet.getTinExpDate();
            case PINCODE:
                return outlet.getPincode();
            case RFIELD3:
                return outlet.getRfield3();
            case RFIELD5:
                return outlet.getRfield5();
            case RFIELD6:
                return outlet.getRfield6();
            case NewRetailerConstant.CREDITPERIOD:
                return outlet.getCreditDays();
            case NewRetailerConstant.GST_NO:
                return outlet.getGstNum();
            case NewRetailerConstant.PAN_NUMBER:
                return outlet.getPanNo();
            case NewRetailerConstant.DRUG_LICENSE_NUM:
                return outlet.getDrugLicenseNo();
            case NewRetailerConstant.FOOD_LICENCE_NUM:
                return outlet.getFoodLicenseNo();
            case DRUG_LICENSE_EXP_DATE:
                return outlet.getDlExpDate() == null ? "" : outlet.getDlExpDate();
            case NewRetailerConstant.FOOD_LICENCE_EXP_DATE:
                return outlet.getFlExpDate() == null ? "" : outlet.getFlExpDate();
            case RFIELD4:
                return outlet.getrField4();
            case RFIELD7:
                return outlet.getrField7();
            case NewRetailerConstant.REGION:
                return outlet.getRegion();
            case NewRetailerConstant.COUNTRY:
                return outlet.getCountry();
            case MOBILE:
                return outlet.getMobile();
            case NewRetailerConstant.DISTRICT:
                return outlet.getDistrict();
        }

        return "";
    }


    @Override
    public void getSelectedContactTitle(int menuNumber, String spinnerKey) {
        if (outlet.getContact1titlelovid() != null
                && "0".equals(outlet.getContact1titlelovid())
                && !StringUtils.isNullOrEmpty(outlet.getContact1title())
                && !"0".equals(outlet.getContact1title())) {
            int position = getSpinnerSelectedItem(profileConfig.get(menuNumber).getConfigCode());
            String values = outlet.getContact1title() != null ? outlet.getContact1title() : "";
            getIvyView().updateContactPersonSelectedTitle(menuNumber, position, values, spinnerKey);
        }
    }


    @Override
    public int getMaxCreditDays() {
        return configurationMasterHelper.MAX_CREDIT_DAYS;
    }

    @Override
    public ArrayList<ChannelBO> getChannelList() {
        if (configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER
                && getIvyView().getChannelId() > 0) {
            ArrayList<ChannelBO> channelBO = new ArrayList<>();
            channelBO.add(new ChannelBO(getIvyView().getChannelId(), getIvyView().getChannelName()));
            return channelBO;

        } else {
            ArrayList<ChannelBO> channelBO = new ArrayList<>();
            if (channelMasterList != null)
                channelBO.addAll(channelMasterList);
            return channelBO;
        }
    }

    @Override
    public void getChannelSelectedItem(int menuNumber) {
        getIvyView().updateChannelSelectedItem(subChannelMasterList, outlet, menuNumber);
    }

    @Override
    public ArrayList<SpinnerBO> getSubChannelsForAChannel(int channelId) {

        ArrayList<SpinnerBO> list = new ArrayList<>();
        if (channelId != 0) for (SubchannelBO subchannelBO : subChannelMasterList)
            if (channelId == subchannelBO.getChannelid())
                list.add(new SpinnerBO(subchannelBO.getSubchannelid(), subchannelBO.getSubChannelname()));
        return list;
    }

    @Override
    public int getSubChannel() {
        return outlet.getSubChannel();
    }


    @Override
    public void setContractStatusLovId(int id) {
        outlet.setContractStatuslovid(id);
    }

    @Override
    public int getSpinnerSelectedItem(String configCode) {

        int defaultValue = 0;

        switch (configCode) {
            case NewRetailerConstant.CONTACT_PERSON1:
                defaultValue = getSelectedContactPerson1Title();
                break;

            case CONTACT_PERSON2:
                defaultValue = getSelectedContactPerson2Title();
                break;

            case NewRetailerConstant.DISTRIBUTOR:
                defaultValue = getSelectedDistributor();
                break;

            case NewRetailerConstant.LOCATION2:
                defaultValue = getLocation2();
                break;

            case NewRetailerConstant.LOCATION1:
                defaultValue = getLocation1();
                break;

            case NewRetailerConstant.LOCATION:
                defaultValue = getLocationValues();
                break;

            case CONTRACT:
                defaultValue = getContact();
                break;

            case NewRetailerConstant.PAYMENTTYPE:
                defaultValue = getSelectedPaymentType();
                break;

            case NewRetailerConstant.TAXTYPE:
                defaultValue = getTaxType();
                break;

            case NewRetailerConstant.CLASS:
                defaultValue = getClassValue();
                break;

            case NewRetailerConstant.ROUTE:
                defaultValue = getSelectedRoute();
                break;

            case NewRetailerConstant.USER:
                defaultValue = getSelectedUser();
                break;

            case RFIELD5:
                defaultValue = getRfield5();
                break;

            case RFIELD6:
                defaultValue = getRfield6();
                break;

            case RFIELD7:
                defaultValue = getRfiled7();
                break;

            case RFIELD4:
                defaultValue = getRfield4();
                break;

            default:
                break;
        }
        return defaultValue;

    }

    @Override
    public ArrayList<BeatMasterBO> getCurrentUserRoutes() {
        if (beatMaster != null)
            return beatMaster;

        else return new ArrayList<>();
    }

    @Override
    public ArrayList<LocationBO> getLocation1List() {

        return mLocationListByLevId.size() >= 1 && mLocationListByLevId.get(0) != null ? mLocationListByLevId.get(0) : new ArrayList<>();
    }

    @Override
    public ArrayList<LocationBO> getLocation2List() {

        return mLocationListByLevId.size() >= 2 && mLocationListByLevId.get(1) != null ? mLocationListByLevId.get(1) : new ArrayList<>();
    }

    @Override
    public ArrayList<LocationBO> getLocation3List() {

        return mLocationListByLevId.size() >= 3 && mLocationListByLevId.get(2) != null ? mLocationListByLevId.get(2) : new ArrayList<>();
    }

    @Override
    public ArrayList<PaymentType> getRetailerPaymentTypeList() {
        return paymentTypeList;
    }

    void setPaymentTypeList(ArrayList<PaymentType> paymentTypeList) {
        this.paymentTypeList = paymentTypeList;
    }

    void setTaxTypeList(ArrayList<StandardListBO> taxTypeList) {
        this.mTaxTypeList = taxTypeList;
    }

    void setClassTypeList(ArrayList<StandardListBO> classTypeList) {
        this.mClassTypeList = classTypeList;
    }

    void setUserList(ArrayList<UserMasterBO> userList) {
        this.mUserList = userList;
    }

    void setRField4List(ArrayList<RetailerFlexBO> retailerFlexBOS) {
        this.rField4List = retailerFlexBOS;
    }

    void setRField5List(ArrayList<RetailerFlexBO> retailerFlexBOS) {
        this.rField5List = retailerFlexBOS;
    }

    void setRField6List(ArrayList<RetailerFlexBO> retailerFlexBOS) {
        this.rField6List = retailerFlexBOS;
    }

    void setRField7List(ArrayList<RetailerFlexBO> retailerFlexBOS) {
        this.rField7List = retailerFlexBOS;
    }

    void setChannelMasterList(ArrayList<ChannelBO> channelMasterList) {
        this.channelMasterList = channelMasterList;
    }

    void setSubChannelMasterList(ArrayList<SubchannelBO> subChannelMasterList) {
        this.subChannelMasterList = subChannelMasterList;
    }

    @Override
    public ArrayList<DistributorMasterBO> getDistributorTypeMasterList() {
        return mDistributorTypeMasterList;
    }

    void setmDistributorTypeMasterList(ArrayList<DistributorMasterBO> distributorTypeMasterList) {
        mDistributorTypeMasterList = distributorTypeMasterList;
    }

    @Override
    public void getRetailerRoutes(String distributorId) {
        getCompositeDisposable().add(mProfileDataManager.getRetailerBySupplierId(distributorId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> retailerIds) {

                        ArrayList<String> beatIds = new ArrayList<>();


                        for (RetailerMasterBO bo : getRetailerMaster()) {
                            if (retailerIds.contains(bo.getRetailerID()))
                                beatIds.add(bo.getBeatID() + "");
                        }

                        ArrayList<BeatMasterBO> currentRetailerBeats = new ArrayList<>();
                        for (BeatMasterBO beatMasterBO : getBeatMaster()) {
                            if (beatIds.contains("" + beatMasterBO.getBeatId()))
                                currentRetailerBeats.add(beatMasterBO);
                        }


                        getIvyView().updateRouteSpinnerData(currentRetailerBeats);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Commons.print(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    @Override
    public ArrayList<RetailerMasterBO> getRetailerMaster() {
        return getDataManager().getRetailerMasters();
    }

    @Override
    public ArrayList<BeatMasterBO> getBeatMaster() {
        return beatMaster;
    }

    void setBeatMaster(ArrayList<BeatMasterBO> beatMasters) {
        this.beatMaster = beatMasters;
    }

    void setContractStatusList(ArrayList<ContractStatus> contractStatusList) {
        this.mContractStatusList = contractStatusList;
    }

    @Override
    public ArrayList<StandardListBO> getTaxTypeList() {
        return mTaxTypeList;
    }

    @Override
    public ArrayList<StandardListBO> getClassTypeList() {
        return mClassTypeList;
    }

    @Override
    public ArrayList<UserMasterBO> getUserList() {
        if (mUserList == null)
            mUserList = new ArrayList<>();
        return mUserList;

    }

    @Override
    public ArrayList<RetailerFlexBO> getRField4List() {
        return rField4List;
    }

    @Override
    public ArrayList<RetailerFlexBO> getRField5List() {
        return rField5List;
    }

    @Override
    public ArrayList<RetailerFlexBO> getRField6List() {
        return rField6List;
    }

    @Override
    public ArrayList<RetailerFlexBO> getRField7List() {
        return rField7List;
    }

    @Override
    public UserMasterBO getUserMasterBO() {
        return getDataManager().getUser();
    }

    @Override
    public NewOutletBO getOutlet() {
        if (outlet == null)
            outlet = new NewOutletBO();
        return outlet;
    }

    @Override
    public String getCurrentRetailerId() {
        if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType() || getIvyView().getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType())
            return outlet.getRetailerId();

        return null;
    }

    @Override
    public boolean isBaiduMap() {
        return configurationMasterHelper.IS_BAIDU_MAP;
    }

    @Override
    public double getLatitude() {
        return lattitude;
    }

    @Override
    public void updateLatitude(double latitude) {
        this.lattitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void updateLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public void getLinkRetailerList(int distId) {

        getCompositeDisposable().add(Observable.zip(mProfileDataManager.getLinkRetailerForADistributor(distId)
                , mProfileDataManager.downloadNearbyRetailers(getIvyView().getRetailerId()), new BiFunction<ArrayList<RetailerMasterBO>, ArrayList<String>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<RetailerMasterBO> linkedRetailers, ArrayList<String> nearByRetailers) throws Exception {

                        mNearbyRetailerList = linkedRetailers;
                        mDownloadNearByRetailers = nearByRetailers;

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        mSelectedRetailers = new ArrayList<>();
                        getIvyView().showNearByRetailersDialog(mNearbyRetailerList,
                                configurationMasterHelper.VALUE_NEARBY_RETAILER_MAX);
                    }
                }));

    }

    @Override
    public ArrayList<String> getSelectedRetailers() {
        return mSelectedRetailers;
    }

    void setSelectedRetailers(ArrayList<String> mSelectedRetailers) {
        this.mSelectedRetailers = mSelectedRetailers;
    }

    @Override
    public ArrayList<RetailerMasterBO> getNearbyRetailerList() {
        return mNearbyRetailerList;
    }

    @Override
    public ArrayList<String> getDownloadNearByRetailers() {
        return mDownloadNearByRetailers;
    }

    @Override
    public void downloadPriorityProducts() {

        getCompositeDisposable().add(Observable.zip(
                mProfileDataManager.downloadPriorityProducts(),
                mProfileDataManager.downloadPriorityProductsForRetailer(getIvyView().getRetailerId()),
                new BiFunction<ArrayList<StandardListBO>, ArrayList<String>, StringBuffer>() {
                    @Override
                    public StringBuffer apply(ArrayList<StandardListBO> priorityProducts,
                                              ArrayList<String> priorityProductsForRetailer) throws Exception {
                        StringBuffer sb = new StringBuffer();
                        serPriorityProductList(priorityProducts);
                        if (priorityProductsForRetailer.size() > 0) {
                            for (StandardListBO priorityProduct : priorityProducts) {
                                if (priorityProductsForRetailer.contains(priorityProduct.getListID())) {
                                    priorityProduct.setChecked(true);
                                    if (sb.length() > 0)
                                        sb.append(", ");
                                    sb.append(priorityProduct.getListName());
                                }
                            }
                        }
                        return sb;
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<StringBuffer>() {
                    @Override
                    public void accept(StringBuffer sb) throws Exception {

                        if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType())
                            getIvyView().updatePriorityProductAutoCompleteTextView("", false);
                        else if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
                            if (mPriorityProductList.size() > 0)
                                getIvyView().updatePriorityProductAutoCompleteTextView(sb.toString(), true);
                            else
                                getIvyView().updatePriorityProductAutoCompleteTextView("", true);
                        }

                    }
                }));


    }

    private void serPriorityProductList(ArrayList<StandardListBO> priorityProducts) {
        mPriorityProductList = priorityProducts;
    }

    @Override
    public ArrayList<StandardListBO> getPriorityProductMasterList() {
        return mPriorityProductList;
    }

    @Override
    public void saveNewRetailer() {
        /*Image Capture...*/
        if (configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
            if (lattitude == 0 || longitude == 0
                    || (configurationMasterHelper.retailerLocAccuracyLvl != 0
                    && LocationUtil.accuracy > configurationMasterHelper.retailerLocAccuracyLvl)) {
                getIvyView().showMessage(R.string.location_not_captured);
                return;
            }
        }
        /*Set Values..*/
        buildNewOutlet();

        if (!outlet.getOutletName().isEmpty()) {

            getCompositeDisposable().add(mProfileDataManager
                    .checkRetailerAlreadyAvailable(outlet.getOutletName(), outlet.getPincode())
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isRetailerAvailable) throws Exception {

                            if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType()
                                    || !isRetailerAvailable) {
                                saveNewRetailerData();
                            } else {
                                getIvyView().showMessage(R.string.retailer_already_available);
                            }
                        }
                    }));

        } else {
            getIvyView().showMessage(R.string.newretailer_empty);
        }

    }


    private void saveNewRetailerData() {
        if (configurationMasterHelper.SHOW_NEW_OUTLET_ORDER
                && getDataManager().isOpenOrderExisting()
                && (isDistributorEnabled
                && getDataManager().getRetailMaster().getDistributorId() != 0
                && getDataManager().getRetailMaster().getDistributorId() != SDUtil.convertToInt(getIvyView()
                .getDistributorSpinnerSelectedItem().getDId()))) {
            getIvyView().showDistributorChangedDialog();
        } else {
            saveNewOutlet();
        }
    }


    @Override
    public void downloadRetailerMaster() {
        //businessModelHelper.downloadRetailerMaster();
        getCompositeDisposable().add(retailerDataManager.fetchRetailers()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(retailerMasterBOS -> getDataManager().setRetailerMasters(retailerMasterBOS)));
    }


    @Override
    public void clearOrdersAndSaveOutlet() {

        getCompositeDisposable().add(mProfileDataManager.clearExistingOrder()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (orderedProductList.size() > 0)
                            orderedProductList.clear();

                        saveNewOutlet();
                    }
                }));
    }

    @Override
    public void getHomeButtonClick() {
        getCompositeDisposable().add(surveyDataManager.isSurveyAvailableForRetailer(getId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) throws Exception {
                        if (isSuccess)
                            getIvyView().showAlertDialog(R.string.are_you_sure_to_close_without_savingthe_data);
                        else if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType()
                                || getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType()
                                || getIvyView().getScreenMode() == NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType()) {

                            getIvyView().doFinishActivity();
                        }
                    }
                }));

    }


    @Override
    public void deleteNewRetailerSurvey() {
        // surveyHelperNew.deleteNewRetailerSurvey(getId());
        getCompositeDisposable().add(surveyDataManager.deleteNewRetailerSurvey(getId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) throws Exception {
                        if (isSuccess)
                            getIvyView().onSurveyDeleteSuccess();
                    }
                }));
    }

    @Override
    public void onSurveyMenuClick() {

        if (isValidRetailer())
            getCompositeDisposable().add(getDataManager().isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isSuccess) throws Exception {
                            if (isSuccess)
                                setupSurveyScreenData();
                        }
                    }));


    }

    private void setupSurveyScreenData() {

        String id = getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType() || getIvyView().getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType() ? outlet.getRetailerId() : getId();
        getCompositeDisposable().add(mProfileDataManager.setupSurveyScreenData(id)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) throws Exception {
                        if (isSuccess)
                            getIvyView().callSurveyActivity();

                    }
                }));
    }

    @Override
    public void getMenuCaptureOptionClick() {
        if (!isLatLongEnabled && configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE
                && (LocationUtil.latitude == 0 || LocationUtil.longitude == 0)
                || (configurationMasterHelper.retailerLocAccuracyLvl != 0
                && LocationUtil.accuracy > configurationMasterHelper.retailerLocAccuracyLvl)) {
            getIvyView().showMessage("Location not captured.");
        } else {
            if (LocationUtil.latitude == 0 || LocationUtil.longitude == 0) {
                getIvyView().showMessage("Location not captured.");
            } else {
                if (!isLatLongEnabled && (outlet.getNewOutletlattitude() == 0 || outlet.getNewOutletLongitude() == 0)) {
                    lattitude = LocationUtil.latitude;
                    longitude = LocationUtil.longitude;
                    outlet.setNewOutletlattitude(lattitude);
                    outlet.setNewOutletLongitude(longitude);
                }
            }
            getIvyView().MenuCaptureAlert();
        }
    }

    @Override
    public void onOrderMenuClick() {
        if (isDistributorEnabled) {

            String dId = getIvyView().getDistributorSpinnerSelectedItem().getDId();
            if (("0").equalsIgnoreCase(dId)) {
                getIvyView().showMessage(R.string.select_distributor);
                return;
            }
            getDataManager().setRetailerMaster(new RetailerMasterBO());
            if (!getDataManager().isOpenOrderExisting()) {
                if (mDistributorTypeMasterList != null)
                    getDataManager().getRetailMaster().setDistributorId(Integer.parseInt(dId));

                updatePriceGroupId();
            }
        }

        fetchProductScreenData();

    }

    private void fetchProductScreenData() {

        getIvyView().showLoading();
        getCompositeDisposable().add(mProfileDataManager.setUpOrderScreen()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean)
                            getIvyView().navigateToNewOutletOrderScreen();

                        getIvyView().hideLoading();
                    }
                }));
    }


    private void updatePriceGroupId() {

        getCompositeDisposable().add(retailerDataManager.updatePriceGroupId(false)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe());
    }

    @Override
    public void onOpportunityProductsMenuClicked() {
        getIvyView().showLoading();
        getCompositeDisposable().add(mProfileDataManager.setUpOpportunityProductsData()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean)
                            getIvyView().navigateToOpportunityProductsScreen();

                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void loadLocationDataBasedOnPinCode(String pinCode) {

        getCompositeDisposable().add(mProfileDataManager.fetchCensusLocationDetails()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(censusLocationBOS -> {
                    for (CensusLocationBO locationBO : censusLocationBOS) {
                        if (!pinCode.isEmpty() && pinCode.equals(locationBO.getPincode())) {
                            int count = 0;
                            for (int i = 0; i < profileConfig.size(); i++) {
                                ConfigureBO configConfigureBO = profileConfig.get(i);
                                if (CITY.equalsIgnoreCase(configConfigureBO.getConfigCode())) {
                                    getIvyView().updateLocationDataBasedOnPinCode(i, locationBO.getLocationName());
                                    count++;
                                } else if (DISTRICT.equalsIgnoreCase(profileConfig.get(i).getConfigCode())) {
                                    getIvyView().updateLocationDataBasedOnPinCode(i, locationBO.getDistrict());
                                    count++;
                                } else if (STATE.equalsIgnoreCase(profileConfig.get(i).getConfigCode())) {
                                    getIvyView().updateLocationDataBasedOnPinCode(i, locationBO.getState());
                                    count++;
                                } else if (COUNTRY.equalsIgnoreCase(profileConfig.get(i).getConfigCode())) {
                                    getIvyView().updateLocationDataBasedOnPinCode(i, locationBO.getCountry());
                                    count++;
                                }
                                if (count == 4) // Avoid iterating whole profile config list
                                    break;
                            }
                            break;
                        }
                    }
                }));

    }

    @Override
    public String getPrefLanguage() {
        return getDataManager().getPreferredLanguage();
    }

    @Override
    public void setOrderedProductList(ArrayList<ProductMasterBO> productList) {
        this.orderedProductList = productList;
    }

    ArrayList<ProductMasterBO> getOrderedProductList() {
        return orderedProductList;
    }

    @Override
    public void setOrderHeader(OrderHeader orderHeader) {
        this.orderHeader = orderHeader;
    }

    @Override
    public void setOpportunityProductList(ArrayList<ProductMasterBO> productList) {
        this.opportunityProducts = productList;
    }

    private void clearDataToEditProfile() {
        getIvyView().showLoading(R.string.saving);

        getCompositeDisposable().add(mProfileDataManager.deleteNewRetailerTablesForEdit(getId(), outlet.getRetailerId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(aBoolean -> createNewOutlet()));

    }

    private void createNewOutlet() {
        getCompositeDisposable().add(mProfileDataManager.saveNewOutlet(getId(), outlet)
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
                    if (outlet.getPriorityProductList() != null && !outlet.getPriorityProductList().isEmpty())
                        mProfileDataManager.savePriorityProducts(getId(), outlet.getPriorityProductList());
                    return Single.fromCallable(() -> aBoolean);
                })
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
                    if (!configurationMasterHelper.IS_CONTACT_TAB)
                        mProfileDataManager.saveNewOutletContactInformation(getId(), outlet);
                    //TODO Contact Tab save yet to be handled

                    return Single.fromCallable(() -> aBoolean);
                })
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> mProfileDataManager.saveNewOutletAddressInformation(getId(), outlet))
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
                    if (configurationMasterHelper.SHOW_NEW_OUTLET_ORDER && orderedProductList != null && !orderedProductList.isEmpty()) {
                        return mProfileDataManager.saveOrderDetails(getId(), orderHeader, orderedProductList);
                    }
                    return Single.fromCallable(() -> aBoolean);
                })
                .flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
                    if (configurationMasterHelper.SHOW_NEW_OUTLET_OPPR && opportunityProducts != null && !opportunityProducts.isEmpty())
                        return mProfileDataManager.saveOpportunityDetails(getId(), opportunityProducts);
                    return Single.fromCallable(() -> aBoolean);
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isOutletSaved -> {
                    getIvyView().hideLoading();
                    if (isOutletSaved) {
                        //   configurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;
                        if (configurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD) {
                            if (getIvyView().isNetworkConnected()) {
                                uploadNewRetailer();
                            }
                        } else {
                            if (orderedProductList != null)
                                orderedProductList.clear();
                            downloadRetailersAndUpdateView();
                        }
                    } else
                        getIvyView().showAlertMessage();

                }));
    }

    private void uploadNewRetailer() {
        getIvyView().showLoading(R.string.uploading_new_store);
        getCompositeDisposable().add(mProfileDataManager.syncNewOutlet(getId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String responseStatus) throws Exception {
                        switch (responseStatus) {
                            case "-1":
                                getIvyView().showSessionExpiredMessage();
                                break;
                            case "-2":
                                getIvyView().showAlertDialog(DataMembers.NOTIFY_URL_NOT_CONFIGURED);
                                break;
                            case "":
                                getIvyView().showAlertDialog(NOTIFY_UPLOAD_ERROR);
                                break;
                            default:
                                fetchRetailerDownloadUrl(responseStatus);
                                break;
                        }
                    }
                }));

    }

    private void fetchRetailerDownloadUrl(String retailerId) {

        getCompositeDisposable().add(synchronizationDataManager.getSyncUrlList("SYNRET")
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(urlMasters -> {

                    if (urlMasters.isEmpty())
                        getIvyView().showRetailerDownloadFailedMessage();
                    else {
                        fetchNewRetailers(urlMasters, retailerId);

                        if (configurationMasterHelper.IS_NEARBY_RETAILER)
                            saveNearByRetailers(retailerId);
                    }

                }));

    }

    private void fetchNewRetailers(ArrayList<UrlMaster> urlMasters, String retailerId) {

        JSONObject json = new JSONObject();
        try {
            json.put("UserId", getDataManager().getUser()
                    .getUserid());

            json.put("VersionCode", getDataManager().getAppVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, getDataManager().getAppVersionName());
            json.put("RetailerId", retailerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<JSONObject> resultObjects = new ArrayList<>();

        ArrayList<Single<JSONObject>> singles = new ArrayList<>();
        for (UrlMaster urlMaster : urlMasters) {
            if (urlMaster.getIsMandatory() == 1)
                singles.add(synchronizationDataManager.downloadDataFromServer(urlMaster.getUrl(), json, urlMaster.getIsMandatory() == 1)
                        .onErrorReturn(throwable -> {
                            synchronizationDataManager.stopAllRequest();

                            getIvyView().showServerErrorMessage((synchronizationDataManager.getErrorMessage((VolleyError) throwable)));

                            return new JSONObject();
                        }));
            else
                singles.add(synchronizationDataManager.downloadDataFromServer(urlMaster.getUrl(), json, urlMaster.getIsMandatory() == 1));
        }


        getCompositeDisposable().add(Single.merge(singles)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new ResourceSubscriber<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (jsonObject.keys().hasNext()) {
                            try {
                                resultObjects.addAll(synchronizationDataManager.parseResponseJson(jsonObject));
                            } catch (IvyNetworkException e) {
                                getIvyView().hideLoading();
                                getIvyView().showServerErrorMessage(e.getMessage());
                                synchronizationDataManager.stopAllRequest();
                                resultObjects.clear();

                            }
                        } else
                            resultObjects.clear();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        if (resultObjects.size() > 0)
                            insertFetchedDataToDataBase(resultObjects);
                        finishDownload(json);
                    }
                }));

    }


    private void insertFetchedDataToDataBase(ArrayList<JSONObject> resultObjects) {

        ArrayList<Single<Boolean>> singles = new ArrayList<>();
        for (JSONObject jsonObject : resultObjects) {
            singles.add(synchronizationDataManager.parseAndInsertJSON(jsonObject, false));
        }

        getCompositeDisposable().add(Single.zip(singles, new Function<Object[], Boolean>() {
            @Override
            public Boolean apply(Object[] o) throws Exception {

                boolean status = false;
                for (Object object : o) {
                    status = (Boolean) object;

                }

                return status;
            }
        }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            deleteTempRetailerAndUpdateList();
                        }
                    }
                }));
    }

    private void deleteTempRetailerAndUpdateList() {
        getCompositeDisposable().add(mProfileDataManager.deleteNewRetailer(getId())
                .flatMapObservable(new Function<Boolean, ObservableSource<ArrayList<RetailerMasterBO>>>() {
                    @Override
                    public ObservableSource<ArrayList<RetailerMasterBO>> apply(Boolean aBoolean) throws Exception {
                        return retailerDataManager.fetchRetailers();

                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public void accept(ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        getDataManager().setRetailerMasters(retailerMasterBOS);

                        getIvyView().showSuccessMessage();

                        getIvyView().hideLoading();

                    }
                }));
    }

    private void finishDownload(JSONObject request) {
        getCompositeDisposable().add(synchronizationDataManager.downloadDataFromServer("/IncrementalSync/Finish", request, false)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe());
    }

    private void saveNearByRetailers(String retailerId) {
        getCompositeDisposable().add(mProfileDataManager.saveNearByRetailers(retailerId, mSelectedRetailers)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe());
    }


    private void downloadRetailersAndUpdateView() {

        getCompositeDisposable().add((retailerDataManager.fetchRetailers())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public void accept(ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        getDataManager().setRetailerMasters(retailerMasterBOS);

                        getIvyView().showSuccessMessage();

                        getIvyView().hideLoading();

                        if (configurationMasterHelper.IS_NEARBY_RETAILER)
                            saveNearByRetailers(getId());
                    }
                }));

    }

    private void saveNewOutlet() {

        getIvyView().showLoading(R.string.saving);

        if (getIvyView().getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType())
            clearDataToEditProfile();
        else
            createNewOutlet();
    }


    private void buildNewOutlet() {
        int size = profileConfig.size();
        for (int i = 0; i < size; i++) {
            String configCode = profileConfig.get(i).getConfigCode();

            switch (configCode) {
                case STORENAME: {
                    outlet.setOutletName(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case ADDRESS1: {
                    outlet.setAddress(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case ADDRESS2: {
                    outlet.setAddress2(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case ADDRESS3: {
                    outlet.setAddress3(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case CITY: {
                    outlet.setCity(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case STATE: {
                    outlet.setState(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case PHNO1: {
                    outlet.setPhone(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case PHNO2: {
                    outlet.setPhone2(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case EMAIL: {
                    outlet.setEmail(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case PINCODE: {
                    outlet.setPincode(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case RFIELD3: {
                    outlet.setRfield3(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case CREDITPERIOD: {
                    outlet.setCreditDays(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case GST_NO: {
                    outlet.setGstNum(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case PAN_NUMBER: {
                    outlet.setPanNo(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case FOOD_LICENCE_NUM: {
                    outlet.setFoodLicenseNo(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case DRUG_LICENSE_NUM: {
                    outlet.setDrugLicenseNo(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case REGION: {
                    outlet.setRegion(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case COUNTRY: {
                    outlet.setCountry(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case MOBILE: {
                    outlet.setMobile(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case DISTRICT: {
                    outlet.setDistrict(StringUtils.removeQuotes(getIvyView().getDynamicEditTextValues(i)));
                    break;
                }
                case CHANNEL: {
                    if (channelMasterList != null)
                        if (configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER
                                && view.getChannelId() > 0)
                            outlet.setSubChannel(getIvyView().getChannelSpinnerSelectedItem().getChannelId());
                        else
                            outlet.setChannel(getIvyView().getChannelSpinnerSelectedItem().getChannelId());

                    break;
                }
                case SUBCHANNEL: {
                    if (subChannelMasterList != null)
                        outlet.setSubChannel(getIvyView().getSubChannelSpinnerSelectedItem().getId());
                    break;
                }
                case DISTRIBUTOR: {
                    if (getIvyView().getDistributorTypeMasterList() != null)
                        outlet.setDistid(getIvyView().getDistributorSpinnerSelectedItem().getDId());
                    break;
                }
                case CONTRACT: {
                    if (mContractStatusList.size() > 0) {
                        try {
                            outlet.setContractStatuslovid(getIvyView().getContractSpinnerSelectedItem().getListId());
                        } catch (Exception e) {
                            outlet.setContractStatuslovid(0);
                            Commons.printException(e);
                        }
                    }
                    break;
                }
                case FAX: {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        outlet.setFax("0");
                    } else {
                        outlet.setFax(getIvyView().getDynamicEditTextValues(i));
                    }
                    break;
                }
                case PAYMENTTYPE: {
                    if (paymentTypeList.size() > 0) {
                        try {
                            outlet.setPayment(getIvyView().getPaymentType().getListId() + "");
                        } catch (Exception e) {
                            outlet.setPayment("0");
                            Commons.printException(e);
                        }
                    }
                    break;
                }
                case CREDITLIMIT: {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        outlet.setCreditLimit("0");
                    } else {
                        outlet.setCreditLimit(getIvyView().getDynamicEditTextValues(i));
                    }
                    break;
                }
                case TIN_NUM: {
                    if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                        outlet.setTinno("0");
                    } else {
                        outlet.setTinno(getIvyView().getDynamicEditTextValues(i));
                    }
                    break;
                }
                case TIN_EXP_DATE: {
                    if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                        outlet.setTinExpDate("");
                    } else {
                        outlet.setTinExpDate(getIvyView().getDynamicTextViewValues(i));
                    }
                    break;
                }
                case RFIELD5: {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            outlet.setRfield5("0");
                        } else {
                            outlet.setRfield5(getIvyView().getDynamicEditTextValues(i));
                        }
                    } else {
                        if (getIvyView().getRField5Spinner() != null)
                            outlet.setRfield5(getIvyView().getRField5Spinner().getId());
                    }
                    break;
                }
                case RFIELD6: {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            outlet.setRfield6("0");
                        } else {
                            outlet.setRfield6(getIvyView().getDynamicEditTextValues(i));
                        }
                    } else {
                        if (getIvyView().getRField6Spinner() != null)
                            outlet.setRfield6(getIvyView().getRField6Spinner().getId());
                    }
                    break;
                }
                case RFIELD7: {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            outlet.setrField7("0");
                        } else {
                            outlet.setrField7(getIvyView().getDynamicEditTextValues(i));
                        }
                    } else {
                        outlet.setrField7(getIvyView().getRField7Spinner().getId());
                    }
                    break;
                }
                case RFIELD4: {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (StringUtils.isNullOrEmpty(getIvyView().getDynamicEditTextValues(i))) {
                            outlet.setrField4("0");
                        } else {
                            outlet.setrField4(getIvyView().getDynamicEditTextValues(i));
                        }
                    } else {
                        outlet.setrField4(getIvyView().getRField4Spinner().getId());
                    }
                    break;
                }
                case TAXTYPE: {
                    outlet.setTaxTypeId(getIvyView().getTaxTypeSpinner().getListID());
                    break;
                }
                case CLASS: {
                    outlet.setClassTypeId(getIvyView().getClassTypeSpinner().getListID());
                    break;
                }
                case USER: {
                    if (isRouteEnabled)
                        outlet.setUserId(getIvyView().getUserSpinner().getUserid());
                    break;
                }
                case ROUTE: {
                    if (beatMaster != null && !beatMaster.isEmpty()) {
                        try {
                            outlet.setRouteid(getIvyView().getRouteSpinner().getBeatId());
                        } catch (Exception e) {
                            outlet.setRouteid(0);
                            Commons.printException(e);
                        }
                    }
                    break;
                }
                case IN_SEZ: {
                    if (getIvyView().isSEZCheckBoxChecked()) {
                        outlet.setIsSEZ(1);
                    } else {
                        outlet.setIsSEZ(0);
                    }
                    break;
                }
                case DRUG_LICENSE_EXP_DATE: {
                    if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                        outlet.setDlExpDate("");
                    } else {
                        outlet.setDlExpDate(getIvyView().getDynamicTextViewValues(i));
                    }
                    break;
                }
                case FOOD_LICENCE_EXP_DATE: {
                    if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                        outlet.setFlExpDate("");
                    } else {
                        outlet.setFlExpDate(getIvyView().getDynamicTextViewValues(i));
                    }
                    break;
                }

            }

            if (getIvyView().getImageIdList() != null)
                outlet.setImageId(getIvyView().getImageIdList());

            if (getIvyView().getImageNameList() != null)
                outlet.setImageName(getIvyView().getImageNameList());

            if (NewRetailerConstant.CONTACT_PERSON1.equalsIgnoreCase(configCode)) {

                outlet.setContactpersonname(StringUtils.removeQuotes(getIvyView()
                        .getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY)));
                outlet.setContactpersonnameLastName(StringUtils.removeQuotes(getIvyView()
                        .getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY)));

                if (isContactTitleEnabled) {
                    if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1) == 0) {
                        outlet.setContact1title("0");
                        outlet.setContact1titlelovid("0");
                    } else if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1) == mContactTitleList.size() - 1) {
                        outlet.setContact1titlelovid("0");
                        outlet.setContact1title(StringUtils.removeQuotes(
                                getIvyView().getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY)));
                    }
                }
            } else if (CONTACT_PERSON2.equalsIgnoreCase(configCode)) {

                outlet.setContactpersonname2(StringUtils.removeQuotes(getIvyView()
                        .getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY)));
                outlet.setContactpersonname2LastName(StringUtils.removeQuotes(getIvyView()
                        .getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY)));

                if (isContactTitleEnabled) {
                    if (getIvyView().getSpinnerSelectedItemPosition(CONTACT_PERSON2) == 0) {
                        outlet.setContact2title("0");
                        outlet.setContact2titlelovid("0");
                    }

                    if (getIvyView().getSpinnerSelectedItemPosition(CONTACT_PERSON2) == mContactTitleList.size() - 1) {
                        outlet.setContact2titlelovid("0");
                        outlet.setContact2title(StringUtils.removeQuotes(
                                getIvyView().getDynamicEditTextValues(i + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY)));
                    }
                } else {
                    outlet.setContact2title("0");
                    outlet.setContact2titlelovid("0");
                }

            } else if (NewRetailerConstant.LOCATION.equalsIgnoreCase(configCode)) {
                if (getLocation1List().size() > 0) {
                    try {
                        outlet.setLocid(getIvyView().getLocation1().getLocId());
                    } catch (Exception e) {
                        outlet.setLocid(0);
                        Commons.printException(e);
                    }
                }
            } else if (NewRetailerConstant.PLAN.equalsIgnoreCase(configCode)) {
                outlet.setVisitDays(getIvyView().getSelectedDays());
                outlet.setWeekNo(getIvyView().getSelectedWeeks());
            } else if (LATLONG.equalsIgnoreCase(configCode)) {
                outlet.setNewOutletlattitude(lattitude);
                outlet.setNewOutletLongitude(longitude);
            } else if (PRIORITYPRODUCT.equalsIgnoreCase(configCode)) {
                outlet.setPriorityProductList(priorityProductIDList);
            }
        }
    }

    @Override
    public boolean isValidRetailer() {
        isValid = true;
        for (int i = 0; i < profileConfig.size(); i++) {

            String configCode = profileConfig.get(i).getConfigCode();
            String menuName = profileConfig.get(i).getMenuName();
            boolean mandatory = profileConfig.get(i).getMandatory() == 1;

            if (STORENAME.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.ADDRESS1.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.ADDRESS2.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (ADDRESS3.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.CITY.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.STATE.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.CONTACT_PERSON1.equalsIgnoreCase(configCode) && mandatory) {
                if (isContactTitleEnabled) {
                    if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1) == 0) {
                        getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.CONTACT_PERSON1,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.TITLE));
                        isValid = false;
                        break;
                    }
                    if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1) == mContactTitleList.size() - 1) {

                        if (!doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.TITLE)))
                            isValid = false;
                        break;
                    }
                }

                if (!doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY,
                        getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.FIRSTNAME)) ||
                        !doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.LASTNAME)))
                    isValid = false;

            } else if (CONTACT_PERSON2.equalsIgnoreCase(configCode) && mandatory) {
                if (isContactTitleEnabled) {
                    if (getIvyView().getSpinnerSelectedItemPosition(CONTACT_PERSON2) == 0) {
                        getIvyView().setRequestFocusWithErrorMessage(CONTACT_PERSON2,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.TITLE));
                        isValid = false;
                        break;
                    }
                    if (getIvyView().getSpinnerSelectedItemPosition(CONTACT_PERSON2) == mContactTitleList.size() - 1) {
                        if (!doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.TITLE)))
                            isValid = false;
                        break;
                    }
                }

                if (!doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY,
                        getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.FIRSTNAME)) ||
                        !doCommonValidate(i + NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY,
                                getIvyView().getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.LASTNAME)))
                    isValid = false;

            } else if (PHNO1.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (PHNO2.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.CHANNEL.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CHANNEL) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.CHANNEL, menuName);
                    isValid = false;
                    break;
                }
            } else if (CONTRACT.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(CONTRACT) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(CONTRACT, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.SUBCHANNEL.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.SUBCHANNEL) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.SUBCHANNEL, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.DISTRIBUTOR.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.DISTRIBUTOR) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.DISTRIBUTOR, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.ROUTE.equalsIgnoreCase(configCode) && mandatory) {

                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.ROUTE) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.ROUTE, menuName);
                    isValid = false;
                    break;
                }

            } else if (NewRetailerConstant.LOCATION2.equalsIgnoreCase(configCode) && mandatory) {

                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.LOCATION2) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.LOCATION2, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.LOCATION1.equalsIgnoreCase(configCode) && mandatory) {

                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.LOCATION1) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.LOCATION1, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.LOCATION.equalsIgnoreCase(configCode) && mandatory) {

                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.LOCATION) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.LOCATION, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.PAYMENTTYPE.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.PAYMENTTYPE) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.PAYMENTTYPE, menuName);
                    isValid = false;
                }
            } else if (NewRetailerConstant.PLAN.equalsIgnoreCase(configCode) && mandatory) {
                if (!getIvyView().isWeekChecked()) {
                    getIvyView().showSelectPlanError(WEEK_TEXT_LABEL);
                    isValid = false;
                }
                if(!getIvyView().isDayChecked()){
                    getIvyView().showSelectPlanError(DAY_TEXT_LABEL);
                    isValid = false;
                }
            } else if (LATLONG.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSelectedLatLong().startsWith("0.0")) {
                    getIvyView().showMessage("");
                    isValid = false;
                }
            } else if (NewRetailerConstant.EMAIL.equalsIgnoreCase(configCode)) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
                else if (!StringUtils.isValidEmail(getIvyView().getDynamicEditTextValues(i))) {
                    getIvyView().setDynamicEditTextFocus(i);
                    getIvyView().showInvalidError(i, menuName);
                    isValid = false;
                }
            } else if (FAX.equalsIgnoreCase(configCode)) {
                if (mandatory || getIvyView().getDynamicEditTextValues(i).length() == 0) {
                    isValid = doCommonValidate(i, menuName);
                }
            } else if (NewRetailerConstant.CREDITPERIOD.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.TIN_NUM.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (TIN_EXP_DATE.equalsIgnoreCase(configCode) && mandatory) {
                if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                    getIvyView().showInvalidDateError(i, menuName);
                    isValid = false;
                }
            } else if (PINCODE.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (RFIELD3.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (RFIELD5.equalsIgnoreCase(configCode) && mandatory) {
                if (profileConfig.get(i).getHasLink() == 0) {
                    if (!doCommonValidate(i, menuName))
                        isValid = false;
                } else if (profileConfig.get(i).getHasLink() == 1 && getIvyView().getSpinnerSelectedItemPosition(RFIELD5) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(RFIELD5, menuName);
                    isValid = false;
                }
            } else if (RFIELD6.equalsIgnoreCase(configCode) && mandatory) {
                if (profileConfig.get(i).getHasLink() == 0) {
                    if (!doCommonValidate(i, menuName))
                        isValid = false;
                } else if (profileConfig.get(i).getHasLink() == 1 && getIvyView().getSpinnerSelectedItemPosition(RFIELD6) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(RFIELD6, menuName);
                    isValid = false;
                }

            } else if (RFIELD4.equalsIgnoreCase(configCode) && mandatory) {
                if (profileConfig.get(i).getHasLink() == 0) {
                    if (!doCommonValidate(i, menuName))
                        isValid = false;
                } else if (profileConfig.get(i).getHasLink() == 1 && getIvyView().getSpinnerSelectedItemPosition(RFIELD4) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(RFIELD4, menuName);
                    isValid = false;
                }

            } else if (RFIELD7.equalsIgnoreCase(configCode) && mandatory) {
                if (profileConfig.get(i).getHasLink() == 0) {
                    if (!doCommonValidate(i, menuName))
                        isValid = false;
                } else if (profileConfig.get(i).getHasLink() == 1 && getIvyView().getSpinnerSelectedItemPosition(RFIELD7) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(RFIELD7, menuName);
                    isValid = false;
                }
            } else if (CREDITLIMIT.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.TAXTYPE.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.TAXTYPE) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.TAXTYPE, menuName);
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.CLASS.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().getSpinnerSelectedItemPosition(NewRetailerConstant.CLASS) == 0) {
                    getIvyView().setRequestFocusWithErrorMessage(NewRetailerConstant.CLASS, menuName);
                    isValid = false;
                    break;
                }
            } else if (PRIORITYPRODUCT.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().validatePriorityProduct(menuName)) {
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.NEARBYRET.equalsIgnoreCase(configCode) && mandatory) {
                if (getIvyView().validateNearbyRetailer(menuName)) {
                    isValid = false;
                    break;
                }
            } else if (NewRetailerConstant.GST_NO.equalsIgnoreCase(configCode)) {
                if (mandatory && !doCommonValidate(i, menuName))
                    isValid = false;
                else if (getIvyView().getDynamicEditTextValues(i).length() > 0) {
                    if (!validateGSTAndPan(i, menuName))
                        isValid = false;
                    else if (!isValidGSTINWithPAN(getIvyView().getDynamicEditTextValues(i))) {
                        getIvyView().setDynamicEditTextFocus(i);
                        getIvyView().showInvalidError(i, menuName);
                        isValid = false;
                    }
                }
            } else if (NewRetailerConstant.PAN_NUMBER.equalsIgnoreCase(configCode)) {
                if (mandatory && !doCommonValidate(i, menuName))
                    isValid = false;
                else if (!validateGSTAndPan(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.DRUG_LICENSE_NUM.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.FOOD_LICENCE_NUM.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (DRUG_LICENSE_EXP_DATE.equalsIgnoreCase(configCode) && mandatory) {
                if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                    getIvyView().showInvalidDateError(i, menuName);
                    isValid = false;
                }
            } else if (NewRetailerConstant.FOOD_LICENCE_EXP_DATE.equalsIgnoreCase(configCode) && mandatory) {
                if (!isValidDateFormat(getIvyView().getDynamicTextViewValues(i))) {
                    getIvyView().showInvalidDateError(i, menuName);
                    isValid = false;
                }
            } else if (NewRetailerConstant.REGION.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.COUNTRY.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (MOBILE.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            } else if (NewRetailerConstant.DISTRICT.equalsIgnoreCase(configCode) && mandatory) {
                if (!doCommonValidate(i, menuName))
                    isValid = false;
            }
        }
        if (configurationMasterHelper.IS_CONTACT_TAB) {
            ArrayList<RetailerContactBo> contactList = newOutletHelper.getRetailerContactList();
            if (contactList.size() == 0) {
                isValid = false;
                getIvyView().showContactMandatoryErrorMessage();
            }
        }
        return isValid;
    }

    private boolean validateGSTAndPan(int i, String menuName) {

        if (getIvyView().getDynamicEditTextValues(i).length() < profileConfig.get(i).getMaxLengthNo()) {
            getIvyView().setDynamicEditTextFocus(i);
            getIvyView().showLengthMisMatchError(i, menuName, profileConfig.get(i).getMaxLengthNo());
            return false;
        } else if (!StringUtils.isValidRegx(getIvyView().getDynamicEditTextValues(i), profileConfig.get(i).getRegex())) {
            getIvyView().setDynamicEditTextFocus(i);
            getIvyView().showInvalidError(i, menuName);
            return false;
        }

        return true;
    }

    private boolean isValidDateFormat(String date) {
        return DateTimeUtils.isValidFormat("yyyy/MM/dd", date);
    }


    private boolean isValidGSTINWithPAN(CharSequence target) {
        for (int index = 0; index < profileConfig.size(); index++) {
            if (profileConfig.get(index).getConfigCode().equalsIgnoreCase(NewRetailerConstant.PAN_NUMBER)) {
                String panNumber = getIvyView().getDynamicEditTextValues(index);
                return panNumber.length() <= 0 || target.subSequence(2, target.length() - 3).equals(panNumber);
            }
        }
        return true;
    }


    private boolean doCommonValidate(int position, String menuName) {
        if (getIvyView().getDynamicEditTextValues(position).length() == 0) {
            getIvyView().setDynamicEditTextFocus(position);
            getIvyView().showMandatoryErrorMessage(position, menuName);
            return false;
        }

        return true;
    }


    private String getId() {
        return getDataManager().getUser().getDistributorid()
                + "" + getDataManager().getUser().getUserid() + "" + uID;
    }

    private void addLengthAndRegexFilters(int position) {
        getIvyView().addLengthFilter(profileConfig.get(position).getRegex());
        getIvyView().addRegexFilter(profileConfig.get(position).getRegex());
    }


    private int getSelectedContactPerson1Title() {
        for (int i = 0; i < mContactTitleList.size(); i++) {
            if (mContactTitleList.get(i).getListId() == SDUtil.convertToInt(outlet.getContact1titlelovid())) {
                return i;
            }
        }
        return 0;
    }

    private int getSelectedContactPerson2Title() {
        for (int i = 0; i < mContactTitleList.size(); i++) {
            if (mContactTitleList.get(i).getListId() == SDUtil.convertToInt(outlet.getContact2titlelovid())) {
                return i;
            }
        }
        return 0;
    }

    private int getSelectedDistributor() {
        if (mDistributorTypeMasterList != null) {
            for (int i = 0; i < mDistributorTypeMasterList.size(); i++) {
                if (outlet.getDistid().equalsIgnoreCase(mDistributorTypeMasterList.get(i).getDId()))
                    return i;
            }
        }
        return 0;
    }

    private int getLocation2() {
        if (outlet.getLocid() != 0) {
            String[] loc2 = retailerHelper.getParentLevelName(leastlocId, true);
            int loc2id = SDUtil.convertToInt(loc2[0]);

            String[] loc3 = retailerHelper.getParentLevelName(loc2id, true);
            int loc3id = SDUtil.convertToInt(loc3[0]);

            if (view.getLocationAdapter3() != null) {
                for (int i = 0; i < view.getLocationAdapter3().getCount(); i++) {
                    if (view.getLocationAdapter3().getItem(i).getLocId() == loc3id) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    private int getLocation1() {
        String[] loc2 = retailerHelper.getParentLevelName(
                leastlocId, true);

        int loc2id = 0;
        if (loc2[0] != null)
            loc2id = SDUtil.convertToInt(loc2[0]);

        if (view.getLocationAdapter2() != null) {
            for (int i = 0; i < view.getLocationAdapter2().getCount(); i++) {
                if (view.getLocationAdapter2().getItem(i).getLocId() == loc2id) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int getLocationValues() {
        if (view.getLocationAdapter1() != null)
            for (int i = 0; i < view.getLocationAdapter1().getCount(); i++) {
                if (view.getLocationAdapter1().getItem(i).getLocId() == leastlocId) {
                    return i;
                }
            }
        return 0;
    }

    private int getContact() {
        for (int i = 0; i < mContractStatusList.size(); i++) {
            if (mContractStatusList.get(i).getListId() == outlet.getContractStatuslovid()) {
                return i;
            }
        }
        return 0;
    }

    private int getSelectedPaymentType() {
        for (int i = 0; i < paymentTypeList.size(); i++) {
            if (paymentTypeList.get(i).getListId() == SDUtil.convertToInt(outlet.getPayment())) {
                return i;
            }
        }
        return 0;
    }

    private int getTaxType() {
        for (int i = 0; i < mTaxTypeList.size(); i++) {
            if (mTaxTypeList.get(i).getListID().equals(outlet.getTaxTypeId())) {
                return i;
            }
        }
        return 0;
    }

    private int getClassValue() {
        for (int i = 0; i < mClassTypeList.size(); i++) {
            if (mClassTypeList.get(i).getListID().equals(outlet.getClassTypeId())) {
                return i;
            }
        }
        return 0;
    }

    private int getSelectedRoute() {
        if (view.getRouteAdapter() != null) {
            for (int i = 0; i < view.getRouteAdapter().getCount(); i++) {
                if (view.getRouteAdapter().getItem(i).getBeatId() == outlet.getRouteid()) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int getSelectedUser() {
        for (int i = 0; i < mUserList.size(); i++) {
            if (mUserList.get(i).getUserid() == outlet.getUserId()) {
                return i;
            }
        }
        return 0;
    }

    private int getRfiled7() {
        if (rField7List != null)
            for (int i = 0; i < rField7List.size(); i++) {
                RetailerFlexBO tempBO = rField7List.get(i);
                if (tempBO != null && tempBO.getId().equals(outlet.getrField7())) {
                    return i;
                }
            }
        return 0;
    }

    private int getRfield4() {
        if (rField4List != null) {
            for (int i = 0; i < rField4List.size(); i++) {
                RetailerFlexBO tempBO = rField4List.get(i);
                if (tempBO != null && tempBO.getId().equals(outlet.getrField4())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int getRfield6() {
        if (rField6List != null) {
            for (int i = 0; i < rField6List.size(); i++) {
                RetailerFlexBO tempBO = rField6List.get(i);
                if (tempBO != null && tempBO.getId().equals(outlet.getRfield6())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int getRfield5() {
        if (rField5List != null) {
            for (int i = 0; i < rField5List.size(); i++) {
                RetailerFlexBO tempBO = rField5List.get(i);
                if (tempBO != null && tempBO.getId().equals(outlet.getRfield5())) {
                    return i;
                }
            }
        }
        return 0;
    }
}
