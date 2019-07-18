package com.ivy.ui.profile.create.presenter;

import android.location.Location;

import com.android.volley.TimeoutError;
import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.model.UrlMaster;
import com.ivy.core.network.IvyNetworkException;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
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
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.ui.profile.create.INewRetailerContract;
import com.ivy.ui.profile.create.NewOutletTestDataFactory;
import com.ivy.ui.profile.create.NewRetailerConstant;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.LocationLevel;
import com.ivy.ui.profile.create.model.PaymentType;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.survey.data.SurveyDataManager;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static com.ivy.sd.png.util.DataMembers.NOTIFY_UPLOAD_ERROR;
import static com.ivy.sd.png.util.DataMembers.NOTIFY_URL_NOT_CONFIGURED;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS1;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS2;
import static com.ivy.ui.profile.create.NewRetailerConstant.ADDRESS3;
import static com.ivy.ui.profile.create.NewRetailerConstant.CHANNEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.CITY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CLASS;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON1;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON2;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CONTRACT;
import static com.ivy.ui.profile.create.NewRetailerConstant.COUNTRY;
import static com.ivy.ui.profile.create.NewRetailerConstant.CREDITLIMIT;
import static com.ivy.ui.profile.create.NewRetailerConstant.CREDITPERIOD;
import static com.ivy.ui.profile.create.NewRetailerConstant.ContactTitleOption.TITLE;
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
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD10;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD11;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD12;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD13;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD14;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD15;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD16;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD17;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD18;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD19;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD3;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD4;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD5;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD6;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD7;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD8;
import static com.ivy.ui.profile.create.NewRetailerConstant.RFIELD9;
import static com.ivy.ui.profile.create.NewRetailerConstant.ROUTE;
import static com.ivy.ui.profile.create.NewRetailerConstant.STATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.STORENAME;
import static com.ivy.ui.profile.create.NewRetailerConstant.SUBCHANNEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.TAXTYPE;
import static com.ivy.ui.profile.create.NewRetailerConstant.TIN_EXP_DATE;
import static com.ivy.ui.profile.create.NewRetailerConstant.TIN_NUM;
import static com.ivy.ui.profile.create.NewRetailerConstant.USER;
import static com.ivy.ui.profile.create.NewRetailerConstant.WEEK_TEXT_LABEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.internal.verification.VerificationModeFactory.atMost;

@RunWith(MockitoJUnitRunner.class)
public class NewOutletPresenterImplTest {

    @Mock
    private INewRetailerContract.INewRetailerView view;

    @Mock
    private ConfigurationMasterHelper mockConfigurationMasterHelper;

    @Mock
    private DataManager dataManager;

    @Mock
    private ProfileDataManager profileDataManager;


    @Mock
    private BeatDataManager beatDataManager;

    @Mock
    private ChannelMasterHelper channelMasterHelper;

    @Mock
    private SubChannelMasterHelper subChannelMasterHelper;

    @Mock
    private ChannelDataManager channelDataManager;

    @Mock
    private NewOutletHelper newOutletHelper;

    @Mock
    private DistributorDataManager distributorDataManager;

    @Mock
    private UserDataManager userDataManager;

    @Mock
    private SurveyDataManager surveyDataManager;

    @Mock
    private RetailerDataManager retailerDataManager;

    @Mock
    private SynchronizationDataManager synchronizationDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    private TestScheduler testScheduler = new TestScheduler();

    private NewOutletPresenterImpl<INewRetailerContract.INewRetailerView> mPresenter;


    @Before
    public void setup() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new NewOutletPresenterImpl<>(dataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationMasterHelper,
                view,
                profileDataManager, beatDataManager,
                newOutletHelper,
                distributorDataManager,
                userDataManager, channelDataManager,
                retailerDataManager, surveyDataManager,
                synchronizationDataManager);
    }

    private void getProfileDataPreConditions(ArrayList<ConfigureBO> testProfileConfig) {

        given(dataManager.getPreferredLanguage()).willReturn("en");

        given(profileDataManager.getImageTypeList()).willReturn(Observable.fromCallable(() -> new Vector<>()));

        given(profileDataManager.getProfileConfigs(0, false, "en"))
                .willReturn(Observable.fromCallable(() -> testProfileConfig));
    }

    @Test
    public void testEnableLocationPermission() {
        mockConfigurationMasterHelper.SHOW_CAPTURED_LOCATION = true;

        mPresenter.init();

        then(view).should().getLocationPermission();
        then(view).should().getRetailerId();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testUpdateEmptyOrderHeaderNote() {
        mockConfigurationMasterHelper.SHOW_CAPTURED_LOCATION = true;
        given(view.getRetailerId()).willReturn("");

        mPresenter.init();

        then(view).should().getLocationPermission();
        then(view).should().getRetailerId();
        then(dataManager).should().setOrderHeaderNote("");
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testGetSavedOutletForViewMode() {
        ArrayList<NewOutletBO> newOutletBOS = new ArrayList<>();
        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("1");
        newOutletBOS.add(newOutletBO);

        newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("2");
        newOutletBOS.add(newOutletBO);

        newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("3");
        newOutletBOS.add(newOutletBO);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());

        given(view.getRetailerId()).willReturn("3");

        given(profileDataManager.getNewRetailers()).willReturn(Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                return newOutletBOS;
            }
        }));

        mPresenter.getSavedOutletData();
        testScheduler.triggerActions();

        assertEquals(mPresenter.getOutlet(), newOutletBO);
    }

    @Test
    public void testGetSavedOutletForEditMode() {
        ArrayList<NewOutletBO> newOutletBOS = new ArrayList<>();
        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("1");
        newOutletBOS.add(newOutletBO);

        NewOutletBO newOutletBO1 = new NewOutletBO();
        newOutletBO1.setRetailerId("2");
        newOutletBOS.add(newOutletBO1);

        newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("3");
        newOutletBOS.add(newOutletBO);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());

        given(view.getRetailerId()).willReturn("2");

        given(profileDataManager.getNewRetailers()).willReturn(Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                return newOutletBOS;
            }
        }));

        mPresenter.getSavedOutletData();
        testScheduler.triggerActions();

        assertEquals(mPresenter.getOutlet(), newOutletBO1);
    }

    @Test
    public void testGetSavedOutletForOtherMode() {
        ArrayList<NewOutletBO> newOutletBOS = new ArrayList<>();
        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("1");
        newOutletBOS.add(newOutletBO);

        NewOutletBO newOutletBO1 = new NewOutletBO();
        newOutletBO1.setRetailerId("2");
        newOutletBOS.add(newOutletBO1);

        newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("3");
        newOutletBOS.add(newOutletBO);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.OTHER.getMenuType());

        given(view.getRetailerId()).willReturn("1");

        given(profileDataManager.getNewRetailers()).willReturn(Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                return newOutletBOS;
            }
        }));

        mPresenter.getSavedOutletData();
        testScheduler.triggerActions();

        assertEquals(mPresenter.getOutlet().getOutletName(),"");
    }


    /* Test Field Creations Starts */
    @Test
    public void testStoreNameCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, STORENAME);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testAddress1Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", false, false, ADDRESS1);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testAddress2Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS2);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", false, false, ADDRESS2);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testAddress3Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.ADDRESS3);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", false, false, NewRetailerConstant.ADDRESS3);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testCityCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CITY);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = true;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", false, true, NewRetailerConstant.CITY);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testRegionCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.REGION);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.REGION);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testCountryCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.COUNTRY);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.COUNTRY);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testDistrictCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.DISTRICT);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.DISTRICT);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testFoodLicenseCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.FOOD_LICENCE_NUM);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.FOOD_LICENCE_NUM);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testDrugLicenseCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.DRUG_LICENSE_NUM);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.DRUG_LICENSE_NUM);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }

    @Test
    public void testPanCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.PAN_NUMBER);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).createNewRetailerDetailsField(0, "Config_Menu", true, false, NewRetailerConstant.PAN_NUMBER);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();


    }


    @Test
    public void testContactCreationWithTitle() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_TITLE);
        storeName.setFlag(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);


        storeName = new ConfigureBO();
        storeName.setMenuName("Config_Menu");
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        storeName.setRegex("contactregex");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(profileDataManager.getContactTitle())
                .willReturn(Observable.fromCallable(ArrayList::new));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).getContactPersonTitle(true);
        then(view).should(order).getContactPersonTitle(false);
        then(view).should(order).addLengthFilter("contactregex");
        then(view).should(order).addRegexFilter("contactregex");
        then(view).should(order).createNewRetailerContactPersonOne(1, "Config_Menu", true, false, NewRetailerConstant.CONTACT_PERSON1, true);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testContactCreationPerson2WithoutTitle() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();


        ConfigureBO storeName = new ConfigureBO();
        storeName.setMenuName("Config_Menu");
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON2);
        storeName.setRegex("contactregex");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);


        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("contactregex");
        then(view).should(order).addRegexFilter("contactregex");
        then(view).should(order).createNewRetailerContactPersonTwo(0, "Config_Menu", true, false, NewRetailerConstant.CONTACT_PERSON2, false);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testNewRetailerCreditPeriod() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CREDITPERIOD);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerCreditPeriod(0, "Config_Menu", true, false, NewRetailerConstant.CREDITPERIOD);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testCreateContactEmail() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.EMAIL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = true;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createNewRetailerContactEmail(0, "Config_Menu", false, true, NewRetailerConstant.EMAIL);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPhoneNumber1Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO1);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerContactType(0, "Config_Menu", true, false, PHNO1);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPhoneNumber2Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.PHNO2);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = true;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerContactType(0, "Config_Menu", false, true, NewRetailerConstant.PHNO2);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testCreditLimitCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CREDITLIMIT);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerContactType(0, "Config_Menu", false, false, NewRetailerConstant.CREDITLIMIT);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testFaxNumberCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.FAX);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerContactType(0, "Config_Menu", false, false, NewRetailerConstant.FAX);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testMobileNumberCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.MOBILE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mockConfigurationMasterHelper.IS_UPPERCASE_LETTER = false;
        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createNewRetailerContactType(0, "Config_Menu", false, false, NewRetailerConstant.MOBILE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testChannelCreationWithEmptyChannels() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CHANNEL);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(channelDataManager.fetchChannels()).willReturn(Observable.fromCallable(new Callable<ArrayList<ChannelBO>>() {
            @Override
            public ArrayList<ChannelBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoChannelsError();
        then(view).should(order).createChannelSpinner(true, 0, "Config_Menu");
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testChannelCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CHANNEL);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(channelDataManager.fetchChannels()).willReturn(Observable.fromCallable(new Callable<ArrayList<ChannelBO>>() {
            @Override
            public ArrayList<ChannelBO> call() throws Exception {
                ChannelBO channelBO = new ChannelBO();
                ArrayList<ChannelBO> channelBOS = new ArrayList<>();
                channelBOS.add(channelBO);

                return channelBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createChannelSpinner(true, 0, "Config_Menu");
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testSubChannelCreationWithEmptySubChannels() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.SUBCHANNEL);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(channelDataManager.fetchSubChannels()).willReturn(Observable.fromCallable(new Callable<ArrayList<SubchannelBO>>() {
            @Override
            public ArrayList<SubchannelBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoSubChannelsError();
        then(view).should(order).createSubChannelSpinner(true, 0, "Config_Menu");
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testSubChannelCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.SUBCHANNEL);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(channelDataManager.fetchSubChannels()).willReturn(Observable.fromCallable(new Callable<ArrayList<SubchannelBO>>() {
            @Override
            public ArrayList<SubchannelBO> call() throws Exception {
                SubchannelBO channelBO = new SubchannelBO();
                ArrayList<SubchannelBO> channelBOS = new ArrayList<>();
                channelBOS.add(channelBO);

                return channelBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createSubChannelSpinner(true, 0, "Config_Menu");
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testLocationCreationWithEmptyLocation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.LOCATION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(LinkedHashMap::new));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoLocationsError();
        then(view).should(order).createLocationSpinner(false,0, "Config_Menu",   NewRetailerConstant.LOCATION, false);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }


    @Test
    public void testLocationCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.LOCATION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(() -> {
                    LinkedHashMap<Integer, ArrayList<LocationBO>> locationMap = new LinkedHashMap();
                    ArrayList<LocationBO> locationBOS = new ArrayList<>();
                    locationBOS.add(new LocationBO());
                    locationMap.put(0, locationBOS);
                    return locationMap;
                }));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createLocationSpinner(false,0, "Config_Menu",   NewRetailerConstant.LOCATION, false);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testLocation1CreationWithEmptyLocation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(LinkedHashMap::new));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoLocationsError();
        then(view).should(order).createLocation1Spinner(false,0, "Config_Menu",   LOCATION1);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testLocation1Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(() -> {
                    LinkedHashMap<Integer, ArrayList<LocationBO>> locationMap = new LinkedHashMap();
                    ArrayList<LocationBO> locationBOS = new ArrayList<>();
                    locationBOS.add(new LocationBO());
                    locationMap.put(0, locationBOS);
                    locationMap.put(1,locationBOS);
                    return locationMap;
                }));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createLocation1Spinner(false,0, "Config_Menu",   LOCATION1);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }


    @Test
    public void testLocation2CreationWithEmptyLocation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(LinkedHashMap::new));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoLocationsError();
        then(view).should(order).createLocation2Spinner(false,0, "Config_Menu",   LOCATION2);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testLocation2Creation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        given(profileDataManager.getLocationListByLevId())
                .willReturn(Observable.fromCallable(() -> {
                    LinkedHashMap<Integer, ArrayList<LocationBO>> locationMap = new LinkedHashMap();
                    ArrayList<LocationBO> locationBOS = new ArrayList<>();
                    locationBOS.add(new LocationBO());
                    locationMap.put(0, locationBOS);
                    locationMap.put(1,locationBOS);
                    locationMap.put(2,locationBOS);
                    return locationMap;
                }));

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createLocation2Spinner(false,0, "Config_Menu",   LOCATION2);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();

    }


    @Test
    public void testRouteCreationWithEmptyBeats() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.ROUTE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(beatDataManager.fetchBeats()).willReturn(Observable.fromCallable(new Callable<ArrayList<BeatMasterBO>>() {
            @Override
            public ArrayList<BeatMasterBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoBeatsError();
        then(view).should(order).createRouteSpinner(false, 0, "Config_Menu", ROUTE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRouteCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ROUTE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(beatDataManager.fetchBeats()).willReturn(Observable.fromCallable(new Callable<ArrayList<BeatMasterBO>>() {
            @Override
            public ArrayList<BeatMasterBO> call() throws Exception {
                BeatMasterBO channelBO = new BeatMasterBO();
                ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
                beatMasterBOS.add(channelBO);

                return beatMasterBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createRouteSpinner(true, 0, "Config_Menu", ROUTE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPaymentTypeCreationWithEmptyPaymentTypes() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAYMENTTYPE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getRetailerType()).willReturn(Observable.fromCallable(new Callable<ArrayList<PaymentType>>() {
            @Override
            public ArrayList<PaymentType> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showPaymentTypeListEmptyError();
        then(view).should(order).createPaymentType(true, 0, "Config_Menu", PAYMENTTYPE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPaymentTypeCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAYMENTTYPE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getRetailerType()).willReturn(Observable.fromCallable(new Callable<ArrayList<PaymentType>>() {
            @Override
            public ArrayList<PaymentType> call() throws Exception {
                PaymentType channelBO = new PaymentType();
                ArrayList<PaymentType> userMasterBOS = new ArrayList<>();
                userMasterBOS.add(channelBO);

                return userMasterBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createPaymentType(true, 0, "Config_Menu", PAYMENTTYPE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testUserCreationWithEmptyUsers() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.USER);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(userDataManager.fetchAllUsers()).willReturn(Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showNoUsersError();
        then(view).should(order).createUserSpinner(false, 0, "Config_Menu", USER);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testUserCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(USER);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(userDataManager.fetchAllUsers()).willReturn(Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                UserMasterBO channelBO = new UserMasterBO();
                ArrayList<UserMasterBO> userMasterBOS = new ArrayList<>();
                userMasterBOS.add(channelBO);

                return userMasterBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createUserSpinner(true, 0, "Config_Menu", USER);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testContactStatusCreationWithEmptyContactStatus() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CONTRACT);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getContactStatus()).willReturn(Observable.fromCallable(new Callable<ArrayList<ContractStatus>>() {
            @Override
            public ArrayList<ContractStatus> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showEmptyContactStatusError();
        then(view).should(order).createContactSpinner(false, 0, "Config_Menu", CONTRACT);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testContactStatusCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CONTRACT);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getContactStatus()).willReturn(Observable.fromCallable(new Callable<ArrayList<ContractStatus>>() {
            @Override
            public ArrayList<ContractStatus> call() throws Exception {
                ContractStatus contractStatus = new ContractStatus();
                ArrayList<ContractStatus> contractStatuses = new ArrayList<>();
                contractStatuses.add(contractStatus);

                return contractStatuses;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createContactSpinner(true, 0, "Config_Menu", CONTRACT);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDistributorCreationWithEmptyDistributors() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRIBUTOR);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(distributorDataManager.fetchDistributorList()).willReturn(Observable.fromCallable(new Callable<ArrayList<DistributorMasterBO>>() {
            @Override
            public ArrayList<DistributorMasterBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showDistributorTypeMasterEmptyError();
        then(view).should(order).createDistributor(false, 0, "Config_Menu", DISTRIBUTOR);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDistributorCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRIBUTOR);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(distributorDataManager.fetchDistributorList()).willReturn(Observable.fromCallable(new Callable<ArrayList<DistributorMasterBO>>() {
            @Override
            public ArrayList<DistributorMasterBO> call() throws Exception {
                DistributorMasterBO distributorMasterBO = new DistributorMasterBO();
                ArrayList<DistributorMasterBO> distributorMasterBOS = new ArrayList<>();
                distributorMasterBOS.add(distributorMasterBO);

                return distributorMasterBOS;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createDistributor(true, 0, "Config_Menu", DISTRIBUTOR);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testTaxTypeWithEmptyTaxTypes() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TAXTYPE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getTaxType()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showTaxListEmptyError();
        then(view).should(order).createTaxTypeSpinner(false, 0, "Config_Menu", TAXTYPE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testTaxTypeCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TAXTYPE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.getTaxType()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                StandardListBO taxType = new StandardListBO();
                ArrayList<StandardListBO> taxTypeList = new ArrayList<>();
                taxTypeList.add(taxType);

                return taxTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createTaxTypeSpinner(true, 0, "Config_Menu", TAXTYPE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }


    @Test
    public void testPriorityProductCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PRIORITYPRODUCT);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadPriorityProducts()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                StandardListBO taxType = new StandardListBO();
                ArrayList<StandardListBO> taxTypeList = new ArrayList<>();
                taxTypeList.add(taxType);

                return taxTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createPriorityProductView("Config_Menu", true, 0);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }


    @Test
    public void testNearByRetailerCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NEARBYRET);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createNearByRetailerView("Config_Menu", true );
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }


    @Test
    public void testClassTypeCreationWithEmptyClassType() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CLASS);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadClassType()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).showClassTypeEmptyError();
        then(view).should(order).createClassTypeSpinner(false, 0, "Config_Menu", CLASS);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testClassTypeCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CLASS);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadClassType()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                StandardListBO taxType = new StandardListBO();
                ArrayList<StandardListBO> classTypeList = new ArrayList<>();
                classTypeList.add(taxType);

                return classTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createClassTypeSpinner(true, 0, "Config_Menu", CLASS);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField4SpinnerCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadRetailerFlexValues(RFIELD4)).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerFlexBO>>() {
            @Override
            public ArrayList<RetailerFlexBO> call() throws Exception {
                RetailerFlexBO taxType = new RetailerFlexBO();
                ArrayList<RetailerFlexBO> classTypeList = new ArrayList<>();
                classTypeList.add(taxType);

                return classTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createRField4Spinner(true, 0, "Config_Menu", RFIELD4);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField4EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", true, false, RFIELD4);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField5SpinnerCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadRetailerFlexValues(RFIELD5)).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerFlexBO>>() {
            @Override
            public ArrayList<RetailerFlexBO> call() throws Exception {
                RetailerFlexBO taxType = new RetailerFlexBO();
                ArrayList<RetailerFlexBO> classTypeList = new ArrayList<>();
                classTypeList.add(taxType);

                return classTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createRField5Spinner(true, 0, "Config_Menu", RFIELD5);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField5EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", true, false, RFIELD5);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField6SpinnerCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadRetailerFlexValues(RFIELD6)).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerFlexBO>>() {
            @Override
            public ArrayList<RetailerFlexBO> call() throws Exception {
                RetailerFlexBO taxType = new RetailerFlexBO();
                ArrayList<RetailerFlexBO> classTypeList = new ArrayList<>();
                classTypeList.add(taxType);

                return classTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createRField6Spinner(true, 0, "Config_Menu", RFIELD6);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField6EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", true, false, RFIELD6);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField7SpinnerCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);
        given(profileDataManager.downloadRetailerFlexValues(RFIELD7)).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerFlexBO>>() {
            @Override
            public ArrayList<RetailerFlexBO> call() throws Exception {
                RetailerFlexBO taxType = new RetailerFlexBO();
                ArrayList<RetailerFlexBO> classTypeList = new ArrayList<>();
                classTypeList.add(taxType);

                return classTypeList;
            }
        }));

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createRField7Spinner(true, 0, "Config_Menu", RFIELD7);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField7EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", true, false, RFIELD7);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField3EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD3);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD3);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField8EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD8);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD8);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField9EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD9);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD9);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField10EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD10);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD10);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField11EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD11);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD11);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField12EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD12);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD12);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField13EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD13);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD13);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField14EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD14);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD14);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField15EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD15);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD15);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField16EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD16);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD16);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField17EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD17);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD17);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField18EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD18);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD18);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testRField19EditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD19);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createRFieldEditText(0, "Config_Menu", false, false, RFIELD19);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPlanCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PLAN);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createDaysAndWeeks(false);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testTinNumberEditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_NUM);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createTinNum(0, "Config_Menu", true, false, TIN_NUM);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testPinCodeEditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).addRegexFilter("abcdef");
        then(view).should(order).createPinCode(0, "Config_Menu", false, false, PINCODE);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGSTEditTextCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).addLengthFilter("abcdef");
        then(view).should(order).createGstNo(0, "Config_Menu", false, false, GST_NO);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testLatLngFieldCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LATLONG);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createLatLongTextView("Config_Menu",0);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testTinExpiryDateCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createTinExpDataTextView(0, "Config_Menu", true);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDrugLicenseExpiryDateCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createDrugLicenseExpDataTextView(0, "Config_Menu", true);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testFoodLicenseExpiryDateCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(0);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createFoodLicenceExpDataTextView(0, "Config_Menu", false);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testInSezCreation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(IN_SEZ);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        getProfileDataPreConditions(testProfileConfig);

        mPresenter.loadInitialData();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).showLoading();
        then(view).should(order).getChannelId();
        then(view).should(order).createSezCheckBox("Config_Menu", true);
        then(view).should(order).hideLoading();
        then(view).shouldHaveNoMoreInteractions();
    }


    /* Test Field Creations Ends */

    @Test
    public void testValidateEmptyStoreName() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Config_Menu");

    }

    @Test
    public void testValidateNonEmptyStoreName() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("abc");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().getDynamicEditTextValues(0);
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testValidateEmptyAddressOne() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Address 1");
    }

    @Test
    public void testValidateEmptyAddressTwo() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS2);
        storeName.setMenuName("Address 2");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Address 2");
    }

    @Test
    public void testValidateEmptyAddressThree() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.ADDRESS3);
        storeName.setMenuName("Address 3");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Address 3");
    }

    @Test
    public void testValidateEmptyCityName() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CITY);
        storeName.setMenuName("City Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "City Name");
    }

    @Test
    public void testValidateEmptyStateName() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.STATE);
        storeName.setMenuName("State Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "State Name");
    }

    @Test
    public void testValidateFirstContactPersonNameTitleNotSelected() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        storeName.setMenuName("First Contact Person Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1)).willReturn(0);

        mPresenter.setContactTitleEnabled(true);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.CONTACT_PERSON1, null);
    }

    @Test
    public void testValidateFirstContactPersonNameOtherTitleNotEntered() {

        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(1, "Mrs");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(2, "Other");
        contactTitles.add(contactTitle);

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        storeName.setMenuName("First Contact Person Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON1)).willReturn(2);
        given(view.getContactPersonTitleOption(TITLE)).willReturn("Title");
        given(view.getDynamicEditTextValues(CONTACT_PERSON_OTHERNAME_KEY)).willReturn("");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setContactTitleEnabled(true);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(CONTACT_PERSON_OTHERNAME_KEY);
        then(view).should().showMandatoryErrorMessage(CONTACT_PERSON_OTHERNAME_KEY, "Title");
    }

    @Test
    public void testValidateFirstContactEmptyFirstName() {


        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        storeName.setMenuName("First Contact Person Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON2);
        storeName.setMenuName("First Contact Person Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.FIRSTNAME)).willReturn("First Name");
        given(view.getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption.LASTNAME)).willReturn("Last Name");
        given(view.getDynamicEditTextValues(CONTACT_PERSON_FIRSTNAME_KEY)).willReturn("");
        given(view.getDynamicEditTextValues(CONTACT_PERSON_LASTNAME_KEY)).willReturn("");
        given(view.getDynamicEditTextValues(1 + CONTACT_PERSON_FIRSTNAME_KEY)).willReturn("");
        given(view.getDynamicEditTextValues(1 + CONTACT_PERSON_LASTNAME_KEY)).willReturn("");

        mPresenter.setContactTitleEnabled(false);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(CONTACT_PERSON_FIRSTNAME_KEY);
        then(view).should().showMandatoryErrorMessage(CONTACT_PERSON_FIRSTNAME_KEY, "First Name");

        then(view).should().setDynamicEditTextFocus(CONTACT_PERSON_LASTNAME_KEY);
        then(view).should().showMandatoryErrorMessage(CONTACT_PERSON_LASTNAME_KEY, "Last Name");

        then(view).should().setDynamicEditTextFocus(1 + CONTACT_PERSON_FIRSTNAME_KEY);
        then(view).should().showMandatoryErrorMessage(1 + CONTACT_PERSON_FIRSTNAME_KEY, "First Name");

        then(view).should().setDynamicEditTextFocus(1 + CONTACT_PERSON_LASTNAME_KEY);
        then(view).should().showMandatoryErrorMessage(1 + CONTACT_PERSON_LASTNAME_KEY, "Last Name");
    }

    @Test
    public void testValidateEmptySecondContactPersonName() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON2);
        storeName.setMenuName("Second Contact Person Name");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.CONTACT_PERSON2)).willReturn(0);

        mPresenter.setContactTitleEnabled(true);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.CONTACT_PERSON2, null);
    }

    @Test
    public void testValidateEmptyPhoneNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO1);
        storeName.setMenuName("Valid phone Number..");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Valid phone Number..");
    }

    @Test
    public void testValidateEmptySecondPhoneNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.PHNO2);
        storeName.setMenuName("Valid phone Number..");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Valid phone Number..");
    }

    @Test
    public void testValidateEmptyChannel() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CHANNEL);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.CHANNEL)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.CHANNEL, null);
    }

    @Test
    public void testValidateEmptyContract() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CONTRACT);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(CONTRACT)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(CONTRACT, null);
    }

    @Test
    public void testValidateEmptySubChannel() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.SUBCHANNEL);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.SUBCHANNEL)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.SUBCHANNEL, null);
    }

    @Test
    public void testValidateEmptyDistributor() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRIBUTOR);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(DISTRIBUTOR)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(DISTRIBUTOR, null);
    }

    @Test
    public void testValidateEmptyRoute() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.ROUTE);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.ROUTE)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.ROUTE, null);
    }

    @Test
    public void testValidateEmptyLocation2() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION2);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(LOCATION2)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(LOCATION2, null);
    }

    @Test
    public void testValidateEmptyLocation1() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION1);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(LOCATION1)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(LOCATION1, null);
    }

    @Test
    public void testValidateEmptyLocation() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.LOCATION);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.LOCATION)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.LOCATION, null);
    }

    @Test
    public void testValidateEmptyPaymentType() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAYMENTTYPE);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(PAYMENTTYPE)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(PAYMENTTYPE, null);
    }

    @Test
    public void testValidateEmptyFax() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.FAX);
        storeName.setMenuName("Fax Number");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Fax Number");
    }

    @Test
    public void testValidateEmptyMobile() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(MOBILE);
        storeName.setMenuName("Mobile Number");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "Mobile Number");
    }

    @Test
    public void testValidateEmptyCreditPeriod() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CREDITPERIOD);
        storeName.setMenuName("CREDITPERIOD");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "CREDITPERIOD");
    }

    @Test
    public void testValidateEmptyGSTNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "GST");
    }

    @Test
    public void testValidateShortGSTNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("hllo");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showLengthMisMatchError(0, "GST", 5);
    }

    @Test
    public void testValidateInValidGSTNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        storeName.setRegex("\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}[Z]{1}[A-Z\\d]{1}");
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("hello");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showInvalidError(0, "GST");
    }

    @Test
    public void testValidateEmptyEmail() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(EMAIL);
        storeName.setMenuName("EMail");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "EMail");
    }

    @Test
    public void testValidateInvalidEmail() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(EMAIL);
        storeName.setMenuName("EMail");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("abc@123");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showInvalidError(0, "EMail");
    }

    @Test
    public void testValidateEmptyPanNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "GST");
    }

    @Test
    public void testValidateShortPanNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("hllo");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showLengthMisMatchError(0, "GST", 5);
    }

    @Test
    public void testEmptyPriorityProducts(){
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PRIORITYPRODUCT);
        storeName.setMenuName("Priority Product");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);


        given(view.getSelectedPriorityProducts()).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showPriorityProductsMandatoryMessage("Priority Product");
    }


    @Test
    public void testEmptyNearByRetailers(){
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NEARBYRET);
        storeName.setMenuName("NearByRetailers");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);


        given(view.getSelectedNearByRetailers()).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showNearByRetailersMandatory("NearByRetailers");
    }


    @Test
    public void testNoWeeksSelected(){
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PLAN);
        storeName.setMenuName("Plan");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        testProfileConfig.add(storeName);

        given(view.isWeekChecked()).willReturn(false);
        given(view.isDayChecked()).willReturn(false);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showSelectPlanError(WEEK_TEXT_LABEL);
        then(view).should().showSelectPlanError(DAY_TEXT_LABEL);
    }

    @Test
    public void testValidateInValidPanNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        storeName.setRegex("AAPFU0939F");
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("hello");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showInvalidError(0, "GST");
    }


    @Test
    public void testValidateGSTNumberWithoutPan() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("GST");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        storeName.setRegex("\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}[Z]{1}[A-Z\\d]{1}");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("PAN");
        storeName.setMandatory(1);
        storeName.setMaxLengthNo(5);
        storeName.setRegex("[A-Z]{5}[0-9]{4}[A-Z]{1}");
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("27AAPFU0129F1ZV");
        given(view.getDynamicEditTextValues(1)).willReturn("AAPFU0939F");


        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showInvalidError(0, "GST");
    }

    @Test
    public void testValidateEmptyTinnum() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.TIN_NUM);
        storeName.setMenuName("TIN_NUM");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "TIN_NUM");
    }

    @Test
    public void testValidateEmptyPincode() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.PINCODE);
        storeName.setMenuName("PINCODE");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "PINCODE");
    }

    @Test
    public void testValidateEmptyRfield3() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.RFIELD3);
        storeName.setMenuName("RFIELD3");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD3");
    }

    @Test
    public void testValidateEmptyRfield8() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD8);
        storeName.setMenuName("RFIELD8");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD8");
    }

    @Test
    public void testValidateEmptyRfield9() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD9);
        storeName.setMenuName("RFIELD9");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD9");
    }

    @Test
    public void testValidateEmptyRfield10() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD10);
        storeName.setMenuName("RFIELD10");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD10");
    }

    @Test
    public void testValidateEmptyRfield11() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.RFIELD11);
        storeName.setMenuName("RFIELD11");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD11");
    }

    @Test
    public void testValidateEmptyRfield12() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD12);
        storeName.setMenuName("RFIELD12");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD12");
    }

    @Test
    public void testValidateEmptyRfield13() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.RFIELD13);
        storeName.setMenuName("RFIELD13");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD13");
    }


    @Test
    public void testValidateEmptyRfield14() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD14);
        storeName.setMenuName("RFIELD14");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD14");
    }

    @Test
    public void testValidateEmptyRfield15() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD15);
        storeName.setMenuName("RFIELD15");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD15");
    }

    @Test
    public void testValidateEmptyRfield16() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD16);
        storeName.setMenuName("RFIELD16");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD16");
    }

    @Test
    public void testValidateEmptyRfield17() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD17);
        storeName.setMenuName("RFIELD17");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD17");
    }

    @Test
    public void testValidateEmptyRfield18() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD18);
        storeName.setMenuName("RFIELD18");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD18");
    }


    @Test
    public void testValidateEmptyRfield19() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD19);
        storeName.setMenuName("RFIELD19");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "RFIELD19");
    }


    @Test
    public void testValidateEmptyCreditlimit() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CREDITLIMIT);
        storeName.setMenuName("CREDITLIMIT");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "CREDITLIMIT");
    }

    @Test
    public void testValidateEmptyDrugLicenseNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.DRUG_LICENSE_NUM);
        storeName.setMenuName("DRUG_LICENSE_NUM");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "DRUG_LICENSE_NUM");
    }

    @Test
    public void testInvalidDrugLicenseDate() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_EXP_DATE);
        storeName.setMenuName("DRUG_LICENSE_EXP_DATE");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicTextViewValues(0)).willReturn("Selecte Date");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showInvalidDateError(0, "DRUG_LICENSE_EXP_DATE");
    }

    @Test
    public void testValidateEmptyFoodLicenceNumber() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.FOOD_LICENCE_NUM);
        storeName.setMenuName("FOOD_LICENCE_NUM");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "FOOD_LICENCE_NUM");
    }

    @Test
    public void testInvalidFoodLicenseDate() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_EXP_DATE);
        storeName.setMenuName("FOOD_LICENCE_EXP_DATE");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicTextViewValues(0)).willReturn("Selecte Date");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showInvalidDateError(0, "FOOD_LICENCE_EXP_DATE");
    }

    @Test
    public void testInvalidTinExpDate() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_EXP_DATE);
        storeName.setMenuName("TIN_EXP_DATE");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicTextViewValues(0)).willReturn("Selecte Date");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showInvalidDateError(0, "TIN_EXP_DATE");
    }


    @Test
    public void testValidateEmptyTaxType() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.TAXTYPE);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.TAXTYPE)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.TAXTYPE, null);
    }

    @Test
    public void testValidateEmptyRegion() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.REGION);
        storeName.setMenuName("REGION");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "REGION");
    }

    @Test
    public void testValidateEmptyCountry() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.COUNTRY);
        storeName.setMenuName("COUNTRY");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "COUNTRY");
    }

    @Test
    public void testValidateEmptyDistrict() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.DISTRICT);
        storeName.setMenuName("DISTRICT");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setDynamicEditTextFocus(0);
        then(view).should().showMandatoryErrorMessage(0, "DISTRICT");
    }

    @Test
    public void testRetailerContactList() {

        mockConfigurationMasterHelper.IS_CONTACT_TAB = true;
        ArrayList<RetailerContactBo> contactList = new ArrayList<>();
        newOutletHelper.setRetailerContactList(contactList);
        mPresenter.isValidRetailer();

        then(view).should().showContactMandatoryErrorMessage();
    }

    @Test
    public void testValidateEmptyClass() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CLASS);
        storeName.setMenuName(null);
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(NewRetailerConstant.CLASS)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(NewRetailerConstant.CLASS, null);
    }

    @Test
    public void testValidateEmptyRfield4() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("RFIELD4");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showMandatoryErrorMessage(0, "RFIELD4");
    }

    @Test
    public void testValidateInvalidRfield4Spinner() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("RFIELD4");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(RFIELD4)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(RFIELD4, "RFIELD4");
    }

    @Test
    public void testValidateEmptyRfield5() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("RFIELD5");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showMandatoryErrorMessage(0, "RFIELD5");
    }

    @Test
    public void testValidateInvalidRfield5Spinner() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("RFIELD5");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(RFIELD5)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(RFIELD5, "RFIELD5");
    }

    @Test
    public void testValidateEmptyRfield6() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("RFIELD6");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showMandatoryErrorMessage(0, "RFIELD6");
    }

    @Test
    public void testValidateInvalidRfield6Spinner() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("RFIELD6");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(RFIELD6)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(RFIELD6, "RFIELD6");
    }

    @Test
    public void testValidateEmptyRfield7() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("RFIELD7");
        storeName.setMandatory(1);
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showMandatoryErrorMessage(0, "RFIELD7");
    }

    @Test
    public void testValidateInvalidRfield7Spinner() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("RFIELD7");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        given(view.getSpinnerSelectedItemPosition(RFIELD7)).willReturn(0);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().setRequestFocusWithErrorMessage(RFIELD7, "RFIELD7");
    }

    @Test
    public void validateInvalidLatLong(){
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LATLONG);
        storeName.setMenuName("LAT LONG");
        storeName.setMandatory(1);
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        given(view.getSelectedLatLong()).willReturn("0.0,0.0");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.isValidRetailer();

        then(view).should().showInvalidDateError(0,"LAT LONG");
    }

    @Test
    public void testGetOutletData() {

        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setOutletName("Ivy Mobility");
        newOutletBO.setAddress("Prince Info City");
        newOutletBO.setAddress2("Perungudi");
        newOutletBO.setAddress3("Chennai");
        newOutletBO.setCity("Chennai");
        newOutletBO.setState("Tamil Nadu");
        newOutletBO.setContactpersonname("NS");
        newOutletBO.setContactpersonname2("Rajiv");
        newOutletBO.setPhone("1234567890");
        newOutletBO.setPhone2("0987654321");
        newOutletBO.setVisitDays("10");
        newOutletBO.setFax("13579");
        newOutletBO.setEmail("a@b.com");
        newOutletBO.setCreditLimit("10");
        newOutletBO.setTinno("#1234");
        newOutletBO.setTinExpDate("1/1/1990");
        newOutletBO.setPincode("600032");
        newOutletBO.setRfield3("rfield3");
        newOutletBO.setRfield5("rfield5");
        newOutletBO.setRfield6("rfield6");
        newOutletBO.setCreditDays("11");
        newOutletBO.setGstNum("GST123456");
        newOutletBO.setPanNo("PANN1234G");
        newOutletBO.setDrugLicenseNo("#0987");
        newOutletBO.setFoodLicenseNo("#FOOD1234");
        newOutletBO.setDlExpDate("10/10/1990");
        newOutletBO.setFlExpDate("10/10/1980");
        newOutletBO.setrField4("rfield4");
        newOutletBO.setrField7("rfield7");
        newOutletBO.setRegion("South");
        newOutletBO.setMobile("9898989898");
        newOutletBO.setCountry("India");
        newOutletBO.setDistrict("Kancheepuram");

        newOutletBO.setRfield8("rfield8");
        newOutletBO.setRfield9("rfield9");
        newOutletBO.setRfield10("rfield10");
        newOutletBO.setRfield11("rfield11");
        newOutletBO.setRfield12("rfield12");
        newOutletBO.setRfield13("rfield13");
        newOutletBO.setRfield14("rfield14");
        newOutletBO.setRfield15("rfield15");
        newOutletBO.setRfield16("rfield16");
        newOutletBO.setRfield17("rfield17");
        newOutletBO.setRfield18("rfield18");
        newOutletBO.setRfield19("rfield19");

        mPresenter.setOutlet(newOutletBO);

        assertEquals(mPresenter.getOutletData(STORENAME), "Ivy Mobility");
        assertEquals(mPresenter.getOutletData(ADDRESS1), "Prince Info City");
        assertEquals(mPresenter.getOutletData(CONTACT_PERSON1), "NS");
        assertEquals(mPresenter.getOutletData(CONTACT_PERSON2), "Rajiv");
        assertEquals(mPresenter.getOutletData(ADDRESS2), "Perungudi");
        assertEquals(mPresenter.getOutletData(ADDRESS3), "Chennai");
        assertEquals(mPresenter.getOutletData(CITY), "Chennai");
        assertEquals(mPresenter.getOutletData(STATE), "Tamil Nadu");
        assertEquals(mPresenter.getOutletData(PHNO1), "1234567890");
        assertEquals(mPresenter.getOutletData(PHNO2), "0987654321");
        assertEquals(mPresenter.getOutletData(PLAN), "10");
        assertEquals(mPresenter.getOutletData(FAX), "13579");
        assertEquals(mPresenter.getOutletData(EMAIL), "a@b.com");
        assertEquals(mPresenter.getOutletData(CREDITLIMIT), "10");
        assertEquals(mPresenter.getOutletData(TIN_NUM), "#1234");
        assertEquals(mPresenter.getOutletData(TIN_EXP_DATE), "1/1/1990");
        assertEquals(mPresenter.getOutletData(PINCODE), "600032");
        assertEquals(mPresenter.getOutletData(RFIELD3), "rfield3");
        assertEquals(mPresenter.getOutletData(RFIELD5), "rfield5");
        assertEquals(mPresenter.getOutletData(RFIELD6), "rfield6");
        assertEquals(mPresenter.getOutletData(CREDITPERIOD), "11");
        assertEquals(mPresenter.getOutletData(GST_NO), "GST123456");
        assertEquals(mPresenter.getOutletData(PAN_NUMBER), "PANN1234G");
        assertEquals(mPresenter.getOutletData(DRUG_LICENSE_NUM), "#0987");
        assertEquals(mPresenter.getOutletData(FOOD_LICENCE_NUM), "#FOOD1234");
        assertEquals(mPresenter.getOutletData(DRUG_LICENSE_EXP_DATE), "10/10/1990");
        assertEquals(mPresenter.getOutletData(FOOD_LICENCE_EXP_DATE), "10/10/1980");
        assertEquals(mPresenter.getOutletData(RFIELD4), "rfield4");
        assertEquals(mPresenter.getOutletData(RFIELD7), "rfield7");
        assertEquals(mPresenter.getOutletData(REGION), "South");
        assertEquals(mPresenter.getOutletData(MOBILE), "9898989898");
        assertEquals(mPresenter.getOutletData(COUNTRY), "India");
        assertEquals(mPresenter.getOutletData(DISTRICT), "Kancheepuram");
        assertEquals(mPresenter.getOutletData(""), "");
        assertEquals(mPresenter.getOutletData(RFIELD8), "rfield8");
        assertEquals(mPresenter.getOutletData(RFIELD9), "rfield9");
        assertEquals(mPresenter.getOutletData(RFIELD10), "rfield10");
        assertEquals(mPresenter.getOutletData(RFIELD11), "rfield11");
        assertEquals(mPresenter.getOutletData(RFIELD12), "rfield12");
        assertEquals(mPresenter.getOutletData(RFIELD13), "rfield13");
        assertEquals(mPresenter.getOutletData(RFIELD14), "rfield14");
        assertEquals(mPresenter.getOutletData(RFIELD15), "rfield15");
        assertEquals(mPresenter.getOutletData(RFIELD16), "rfield16");
        assertEquals(mPresenter.getOutletData(RFIELD17), "rfield17");
        assertEquals(mPresenter.getOutletData(RFIELD18), "rfield18");
        assertEquals(mPresenter.getOutletData(RFIELD19), "rfield19");


    }

    @Test
    public void testGetSelectedContactTitleFailure() {

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact1titlelovid(null);
        outletBO.setContact1title("Mr");

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();

        outletBO.setContact1titlelovid("1");
        outletBO.setContact1title("Mr");

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();

        outletBO.setContact1titlelovid("0");
        outletBO.setContact1title(null);

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();

        outletBO.setContact1titlelovid("0");
        outletBO.setContact1title("");

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();

        outletBO.setContact1titlelovid("0");
        outletBO.setContact1title("null");

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();

        outletBO.setContact1titlelovid("0");
        outletBO.setContact1title("0");

        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(1, "Contact");

        then(view).shouldHaveZeroInteractions();


    }

    @Test
    public void testGetSelectedContact1TitleSuccess() {

        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(1, "Mrs");
        contactTitles.add(contactTitle);

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setMenuName("Config_Menu");
        storeName.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        storeName.setRegex("contactregex");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);


        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact1titlelovid("0");
        outletBO.setContact1title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(0, "Contact");

        then(view).should().updateContactPersonSelectedTitle(0, 0, "Mr", "Contact");
    }

    @Test
    public void testGetSelectedContact2TitleSuccess() {

        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(1, "Mrs");
        contactTitles.add(contactTitle);

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setMenuName("Config_Menu");
        storeName.setConfigCode(CONTACT_PERSON2);
        storeName.setRegex("contactregex");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);


        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact2titlelovid("0");
        outletBO.setContact2title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(outletBO);
        mPresenter.getSelectedContactTitle(0, "Contact");

        then(view).should().updateContactPersonSelectedTitle(0, 0, "Mr", "Contact");
    }

    @Test
    public void getSelectedContact1Title() {
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(1, "Mrs");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(2, "Other");
        contactTitles.add(contactTitle);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact1titlelovid("1");
        outletBO.setContact1title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CONTACT_PERSON1), 1);

    }

    @Test
    public void getSelectedContact1TitleNotPresent() {
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        contactTitles.add(contactTitle);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact1titlelovid("1");
        outletBO.setContact1title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CONTACT_PERSON1), 0);

    }

    @Test
    public void getSelectedContact2Title() {
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(1, "Mrs");
        contactTitles.add(contactTitle);
        contactTitle = new ContactTitle(2, "Other");
        contactTitles.add(contactTitle);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact2titlelovid("2");
        outletBO.setContact2title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CONTACT_PERSON2), 2);

    }

    @Test
    public void getSelectedContact2TitleNotPresent() {
        ArrayList<ContactTitle> contactTitles = new ArrayList<>();
        ContactTitle contactTitle = new ContactTitle(0, "Mr");
        contactTitles.add(contactTitle);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setContact2titlelovid("1");
        outletBO.setContact2title("Mr");

        mPresenter.setContactTitleList(contactTitles);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CONTACT_PERSON2), 0);

    }

    @Test
    public void getSelectedDistributor() {
        ArrayList<DistributorMasterBO> distributorMasterBOS = new ArrayList<>();
        DistributorMasterBO distributorMasterBO = new DistributorMasterBO("0", "Mr");
        distributorMasterBOS.add(distributorMasterBO);
        distributorMasterBO = new DistributorMasterBO("1", "Mrs");
        distributorMasterBOS.add(distributorMasterBO);
        distributorMasterBO = new DistributorMasterBO("2", "Other");
        distributorMasterBOS.add(distributorMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setDistid("2");

        mPresenter.setmDistributorTypeMasterList(distributorMasterBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(DISTRIBUTOR), 2);

    }

    @Test
    public void getSelectedDistributorNotPresent() {
        ArrayList<DistributorMasterBO> distributorMasterBOS = new ArrayList<>();
        DistributorMasterBO distributorMasterBO = new DistributorMasterBO("0", "Mr");
        distributorMasterBOS.add(distributorMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setDistid("1");

        mPresenter.setmDistributorTypeMasterList(distributorMasterBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(DISTRIBUTOR), 0);

    }

    @Test
    public void getSelectedPaymentType() {
        ArrayList<PaymentType> paymentTypes = new ArrayList<>();
        PaymentType paymentType = new PaymentType(0, "Mr");
        paymentTypes.add(paymentType);
        paymentType = new PaymentType(1, "Mrs");
        paymentTypes.add(paymentType);
        paymentType = new PaymentType(2, "Other");
        paymentTypes.add(paymentType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setPayment("2");

        mPresenter.setPaymentTypeList(paymentTypes);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(PAYMENTTYPE), 2);

    }

    @Test
    public void getSelectedPaymentTypeNotPresent() {
        ArrayList<PaymentType> paymentTypes = new ArrayList<>();
        PaymentType paymentType = new PaymentType(1, "Mr");
        paymentTypes.add(paymentType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setPayment("5");

        mPresenter.setPaymentTypeList(paymentTypes);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(PAYMENTTYPE), 0);

    }

    @Test
    public void getSelectedTaxType() {
        ArrayList<StandardListBO> taxTypes = new ArrayList<>();
        StandardListBO taxType = new StandardListBO("0", "Mr");
        taxTypes.add(taxType);
        taxType = new StandardListBO("1", "Mrs");
        taxTypes.add(taxType);
        taxType = new StandardListBO("2", "Other");
        taxTypes.add(taxType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setTaxTypeId("1");

        mPresenter.setTaxTypeList(taxTypes);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(TAXTYPE), 1);

    }

    @Test
    public void getSelectedTaxTypeNotPresent() {
        ArrayList<StandardListBO> taxTypes = new ArrayList<>();
        StandardListBO taxType = new StandardListBO("0", "Mr");
        taxTypes.add(taxType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setTaxTypeId("1");

        mPresenter.setTaxTypeList(taxTypes);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(TAXTYPE), 0);

    }

    @Test
    public void getSelectedClassType() {
        ArrayList<StandardListBO> classList = new ArrayList<>();
        StandardListBO classType = new StandardListBO("0", "Mr");
        classList.add(classType);
        classType = new StandardListBO("1", "Mrs");
        classList.add(classType);
        classType = new StandardListBO("2", "Other");
        classList.add(classType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setClassTypeId("1");

        mPresenter.setClassTypeList(classList);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CLASS), 1);

    }

    @Test
    public void getSelectedClassTypeNotPresent() {
        ArrayList<StandardListBO> classTypes = new ArrayList<>();
        StandardListBO classType = new StandardListBO("0", "Mr");
        classTypes.add(classType);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setClassTypeId("1");

        mPresenter.setClassTypeList(classTypes);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(CLASS), 0);

    }


    @Test
    public void getSelectedRoute() {
        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        BeatMasterBO beatMasterBO = new BeatMasterBO(0, "Mr",1);
        beatMasterBOS.add(beatMasterBO);
        beatMasterBO = new BeatMasterBO(1, "Mrs",0);
        beatMasterBOS.add(beatMasterBO);
        beatMasterBO = new BeatMasterBO(2, "Other",1);
        beatMasterBOS.add(beatMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRouteid(1);

        given(view.getRouteAdapter()).willReturn(beatMasterBOS);

        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(ROUTE), 1);

    }

    @Test
    public void getSelectedBeatNotPresent() {
        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        BeatMasterBO beatMasterBO = new BeatMasterBO(0, "Mr",1);
        beatMasterBOS.add(beatMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRouteid(1);

        given(view.getRouteAdapter()).willReturn(beatMasterBOS);

        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(ROUTE), 0);

    }

    @Test
    public void getSelectedUser() {
        ArrayList<UserMasterBO> userMasterList = new ArrayList<>();
        UserMasterBO userMasterBO = new UserMasterBO(0, "Mr");
        userMasterList.add(userMasterBO);
        userMasterBO = new UserMasterBO(1, "Mrs");
        userMasterList.add(userMasterBO);
        userMasterBO = new UserMasterBO(2, "Other");
        userMasterList.add(userMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setUserId(2);

        mPresenter.setUserList(userMasterList);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(USER), 2);

    }

    @Test
    public void getSelectedUserNotPresent() {
        ArrayList<UserMasterBO> userMasterList = new ArrayList<>();
        UserMasterBO userMasterBO = new UserMasterBO(0, "Mr");
        userMasterList.add(userMasterBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setUserId(2);

        mPresenter.setUserList(userMasterList);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(USER), 0);

    }

    @Test
    public void getSelectedRfield4() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("1", "Mrs");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("2", "Other");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setrField4("1");

        mPresenter.setRField4List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD4), 1);
    }

    @Test
    public void getSelectedRfield4NotPresent() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setrField4("1");

        mPresenter.setRField4List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD4), 0);

    }

    @Test
    public void getSelectedRfield5() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("1", "Mrs");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("2", "Other");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRfield5("1");

        mPresenter.setRField5List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD5), 1);
    }

    @Test
    public void getSelectedRfield5NotPresent() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRfield5("1");

        mPresenter.setRField5List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD5), 0);

    }

    @Test
    public void getSelectedRfield6() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("1", "Mrs");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("2", "Other");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRfield6("1");

        mPresenter.setRField6List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD6), 1);
    }

    @Test
    public void getSelectedRfield6NotPresent() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setRfield6("1");

        mPresenter.setRField6List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD6), 0);

    }

    @Test
    public void getSelectedRfield7() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("1", "Mrs");
        flexBOS.add(flexBO);
        flexBO = new RetailerFlexBO("2", "Other");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setrField7("1");

        mPresenter.setRField7List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD7), 1);
    }

    @Test
    public void getSelectedRfield7NotPresent() {
        ArrayList<RetailerFlexBO> flexBOS = new ArrayList<>();
        RetailerFlexBO flexBO = new RetailerFlexBO("0", "Mr");
        flexBOS.add(flexBO);

        NewOutletBO outletBO = new NewOutletBO();
        outletBO.setrField7("1");

        mPresenter.setRField7List(flexBOS);
        mPresenter.setOutlet(outletBO);

        assertEquals(mPresenter.getSpinnerSelectedItem(RFIELD7), 0);

    }

    @Test
    public void testGetSelectedLocation(){

        mPresenter.setLeastlocId(1);
        ArrayList<LocationBO> locationBOS = new ArrayList<>();
        LocationBO locationBO = new LocationBO(0,"Hello");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(1,"Hello1");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(2,"Hello2");
        locationBOS.add(locationBO);

        given(view.getLocationAdapter()).willReturn(locationBOS);

        mPresenter.getSelectedLocationPosition();

        then(view).should().setSpinnerPosition(LOCATION,1);
    }

    @Test
    public void testGetSelectedLocation1(){

        mPresenter.setLeastlocId(1);

        ArrayList<LocationBO> locationBOS = new ArrayList<>();
        LocationBO locationBO = new LocationBO(0,"Hello");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(1,"Hello1");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(2,"Hello2");
        locationBOS.add(locationBO);

        given(profileDataManager.getParentLevelName(1,true)).willReturn(Single.fromCallable(() -> {
            LocationLevel locationLevel = new LocationLevel();
            locationLevel.setLocationId(2);
            return locationLevel;
        }));
        given(view.getLocationAdapter1()).willReturn(locationBOS);

        mPresenter.getSelectedLocation1Position();
        testScheduler.triggerActions();

        then(view).should().setSpinnerPosition(LOCATION1,2);
    }

    @Test
    public void testGetSelectedLocation2(){


        mPresenter.setLeastlocId(1);

        ArrayList<LocationBO> locationBOS = new ArrayList<>();
        LocationBO locationBO = new LocationBO(0,"Hello");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(1,"Hello1");
        locationBOS.add(locationBO);
        locationBO = new LocationBO(2,"Hello2");
        locationBOS.add(locationBO);

        given(profileDataManager.getParentLevelName(1,true)).willReturn(Single.fromCallable(() -> {
            LocationLevel locationLevel = new LocationLevel();
            locationLevel.setLocationId(2);
            return locationLevel;
        }));

        given(profileDataManager.getParentLevelName(2,true)).willReturn(Single.fromCallable(() -> {
            LocationLevel locationLevel = new LocationLevel();
            locationLevel.setLocationId(0);
            return locationLevel;
        }));

        given(view.getLocationAdapter2()).willReturn(locationBOS);

        mPresenter.getSelectedLocation2Position();
        testScheduler.triggerActions();

        then(view).should().setSpinnerPosition(LOCATION2,0);
    }

    @Test
    public void testGetChannelListWithPreSelectedChannel() {

        given(view.getChannelId()).willReturn(1);

        mockConfigurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER = true;

        ArrayList<ChannelBO> channelList = mPresenter.getChannelList();

        then(view).should(times(2)).getChannelId();
        then(view).should().getChannelName();

        assertEquals(channelList.size(), 1);

    }

    @Test
    public void testGetChannelListWithoutPreSelectedChannel() {

        given(view.getChannelId()).willReturn(1);

        mockConfigurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER = false;

        ArrayList<ChannelBO> channelBOS = new ArrayList<>();
        ChannelBO channelBO = new ChannelBO(1, "Hello");
        channelBOS.add(channelBO);
        channelBO = new ChannelBO(1, "Hello1");
        channelBOS.add(channelBO);

        mPresenter.setChannelMasterList(channelBOS);

        ArrayList<ChannelBO> mPresenterChannelList = mPresenter.getChannelList();

        then(view).should(never()).getChannelId();

        assertEquals(mPresenterChannelList.size(), 2);

    }

    @Test
    public void testGetSubChannelForAChannel() {
        ArrayList<SubchannelBO> subChannelBOS = new ArrayList<>();
        SubchannelBO subchannelBO = new SubchannelBO();
        subchannelBO.setChannelid(10);
        subchannelBO.setSubChannelname("Hello");
        subChannelBOS.add(subchannelBO);

        subchannelBO = new SubchannelBO();
        subchannelBO.setChannelid(11);
        subchannelBO.setSubChannelname("Hello1");
        subChannelBOS.add(subchannelBO);

        mPresenter.setSubChannelMasterList(subChannelBOS);

        assertEquals(mPresenter.getSubChannelsForAChannel(10).size(), 1);

    }

    @Test
    public void testDownloadRetailerMaster() {
        ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();
        RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1", "Retailer 1");
        retailerMasterBOS.add(retailerMasterBO);

        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                return retailerMasterBOS;
            }
        }));

        mPresenter.downloadRetailerMaster();
        testScheduler.triggerActions();

        then(dataManager).should().setRetailerMasters(retailerMasterBOS);
    }

    @Test
    public void testLoadLocationDataBasedOnPinCode() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.CITY);
        testProfileConfig.add(storeName);
        storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRICT);
        testProfileConfig.add(storeName);
        storeName = new ConfigureBO();
        storeName.setConfigCode(STATE);
        testProfileConfig.add(storeName);
        storeName = new ConfigureBO();
        storeName.setConfigCode(COUNTRY);
        testProfileConfig.add(storeName);

        ArrayList<CensusLocationBO> censusLocationBOS = new ArrayList<>();
        CensusLocationBO censusLocationBO = new CensusLocationBO();
        censusLocationBO.setPincode("600032");
        censusLocationBO.setLocationName("Guindy");
        censusLocationBO.setDistrict("Chennai");
        censusLocationBO.setState("Tamil Nadu");
        censusLocationBO.setCountry("India");
        censusLocationBOS.add(censusLocationBO);

        mPresenter.setProfileConfig(testProfileConfig);
        given(profileDataManager.fetchCensusLocationDetails()).willReturn(Observable.fromCallable(new Callable<ArrayList<CensusLocationBO>>() {
            @Override
            public ArrayList<CensusLocationBO> call() throws Exception {
                return censusLocationBOS;
            }
        }));

        mPresenter.loadLocationDataBasedOnPinCode("600032");
        testScheduler.triggerActions();

        InOrder order = inOrder(view);

        then(view).should(order).updateLocationDataBasedOnPinCode(0, "Guindy");
        then(view).should(order).updateLocationDataBasedOnPinCode(1, "Chennai");
        then(view).should(order).updateLocationDataBasedOnPinCode(2, "Tamil Nadu");
        then(view).should(order).updateLocationDataBasedOnPinCode(3, "India");
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRetailerRoutes() {

        ArrayList<String> retailerList = new ArrayList<>();
        retailerList.add("1");
        retailerList.add("10");
        retailerList.add("11");

        given(profileDataManager.getRetailerBySupplierId("10")).willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {

                return retailerList;
            }
        }));

        ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();
        RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1", "Retailer");
        retailerMasterBO.setRetailerID("1");
        retailerMasterBO.setBeatID(11);
        retailerMasterBOS.add(retailerMasterBO);
        retailerMasterBO = new RetailerMasterBO("10", "Retailer");
        retailerMasterBO.setRetailerID("10");
        retailerMasterBO.setBeatID(12);
        retailerMasterBOS.add(retailerMasterBO);
        retailerMasterBO = new RetailerMasterBO("3", "Retailer");
        retailerMasterBO.setRetailerID("3");
        retailerMasterBO.setBeatID(13);
        retailerMasterBOS.add(retailerMasterBO);

        given(dataManager.getRetailerMasters()).willReturn(retailerMasterBOS);

        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();

        ArrayList<BeatMasterBO> verificationList = new ArrayList<>();
        BeatMasterBO beatMasterBO = new BeatMasterBO(11, "Route 1", 1);
        beatMasterBOS.add(beatMasterBO);
        verificationList.add(beatMasterBO);
        beatMasterBO = new BeatMasterBO(12, "Route 2", 1);
        beatMasterBOS.add(beatMasterBO);
        verificationList.add(beatMasterBO);
        beatMasterBO = new BeatMasterBO(15, "Route 3", 1);
        beatMasterBOS.add(beatMasterBO);
        mPresenter.setBeatMaster(beatMasterBOS);

        mPresenter.getRetailerRoutes("10");
        testScheduler.triggerActions();


        ArgumentCaptor<ArrayList> argumentCaptor = ArgumentCaptor.forClass(ArrayList.class);

        then(view).should().updateRouteSpinnerData(argumentCaptor.capture());
        // assertArrayEquals(captor.capture().toArray(),verificationList.toArray());
    }

    @Test
    public void testDownloadPriorityProductsInViewMode() {
        given(view.getRetailerId()).willReturn("1");

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());

        given(profileDataManager.downloadPriorityProducts()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        given(profileDataManager.downloadPriorityProductsForRetailer("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.downloadPriorityProducts();
        testScheduler.triggerActions();

        InOrder order = inOrder(view);
        then(view).should(order).getRetailerId();
        then(view).should(order).getScreenMode();
        then(view).should(order).updatePriorityProductAutoCompleteTextView("", false);
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDownloadPriorityProductsInEditModeWithEmptyProducts() {

        given(view.getRetailerId()).willReturn("1");

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());

        given(profileDataManager.downloadPriorityProducts()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        given(profileDataManager.downloadPriorityProductsForRetailer("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                return new ArrayList<>();
            }
        }));

        mPresenter.downloadPriorityProducts();
        testScheduler.triggerActions();

        then(view).should().getRetailerId();
        then(view).should(atMost(2)).getScreenMode();
        then(view).should().updatePriorityProductAutoCompleteTextView("", true);
        then(view).shouldHaveNoMoreInteractions();

    }


    @Test
    public void testDownloadPriorityProductsInEditMode() {

        ArrayList<StandardListBO> priorityProducts = new ArrayList<>();
        StandardListBO priorityProduct = new StandardListBO("0", "Pen");
        priorityProducts.add(priorityProduct);
        priorityProduct = new StandardListBO("1", "Pencil");
        priorityProducts.add(priorityProduct);
        priorityProduct = new StandardListBO("2", "Eraser");
        priorityProducts.add(priorityProduct);
        priorityProduct = new StandardListBO("3", "Scale");
        priorityProducts.add(priorityProduct);
        priorityProduct = new StandardListBO("4", "Ink");
        priorityProducts.add(priorityProduct);

        given(view.getRetailerId()).willReturn("1");

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());

        given(profileDataManager.downloadPriorityProducts()).willReturn(Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                return priorityProducts;
            }
        }));

        given(profileDataManager.downloadPriorityProductsForRetailer("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> retailerPriorityProductList = new ArrayList<>();
                retailerPriorityProductList.add("1");
                retailerPriorityProductList.add("3");
                retailerPriorityProductList.add("4");
                return retailerPriorityProductList;
            }
        }));

        mPresenter.downloadPriorityProducts();
        testScheduler.triggerActions();

        then(view).should().getRetailerId();
        then(view).should(atMost(2)).getScreenMode();
        then(view).should().updatePriorityProductAutoCompleteTextView("Pencil, Scale, Ink", true);
        then(view).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testDeleteNewRetailerSurvey() {
        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");

        given(surveyDataManager.deleteNewRetailerSurvey(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));

        mPresenter.deleteNewRetailerSurvey();
        testScheduler.triggerActions();

        then(view).should().onSurveyDeleteSuccess();

    }

    @Test
    public void testSaveRetailerLatitudeEmptyOnLocationMandatory() {

        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = true;

        mPresenter.updateLatitude(10.00);

        mPresenter.updateLatitude(0);

        mPresenter.saveNewRetailer();

        then(view).should().showMessage(R.string.location_not_captured);

    }

    @Test
    public void testSaveRetailerLongitudeEmptyOnLocationMandatory() {

        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = true;

        mPresenter.updateLatitude(10.00);

        mPresenter.updateLatitude(0);

        mPresenter.saveNewRetailer();

        then(view).should().showMessage(R.string.location_not_captured);

    }

    @Test
    public void testBuildOutletWithEmptyStoreName() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getOutletName(), "");

    }

    @Test
    public void testBuildOutletWithStoreName() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getOutletName(), "Reliance");

    }


    @Test
    public void testBuildOutletWithEmptyAddress1() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress(), "");

    }

    @Test
    public void testBuildOutletWithAddress1() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyAddress2() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS2);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress2(), "");

    }

    @Test
    public void testBuildOutletWithAddress2() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS2);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress2(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyAddress3() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS3);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress3(), "");

    }

    @Test
    public void testBuildOutletWithAddress3() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS3);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getAddress3(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyCity() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CITY);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCity(), "");

    }

    @Test
    public void testBuildOutletWithCity() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CITY);
        storeName.setMenuName("Config_Menu");
        storeName.setMandatory(1);
        storeName.setRegex("abcdef");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCity(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyState() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getState(), "");

    }

    @Test
    public void testBuildOutletWithState() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getState(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyPhone1() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPhone(), "");

    }

    @Test
    public void testBuildOutletWithPhone1() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPhone(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyPhone2() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        given(profileDataManager.checkRetailerAlreadyAvailable("", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPhone2(), "");

    }

    @Test
    public void testBuildOutletWithPhone2() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PHNO2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        given(profileDataManager.checkRetailerAlreadyAvailable("Reliance", "")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPhone2(), "Reliance");

    }

    @Test
    public void testBuildOutletForChannelWithPreSelectedChannel() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CHANNEL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);
        mPresenter.setProfileConfig(testProfileConfig);

        mockConfigurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER = true;

        mPresenter.setChannelMasterList(new ArrayList<>());

        given(view.getChannelId()).willReturn(10);

        given(view.getChannelSpinnerSelectedItem()).willReturn(new ChannelBO(10, "Super Market"));

        mPresenter.setOutlet(new NewOutletBO());


        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getSubChannel(), 10);
    }

    @Test
    public void testBuildOutletForChannel() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CHANNEL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);
        mPresenter.setProfileConfig(testProfileConfig);

        mockConfigurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER = false;

        mPresenter.setChannelMasterList(new ArrayList<>());

        given(view.getChannelSpinnerSelectedItem()).willReturn(new ChannelBO(10, "Super Market"));

        mPresenter.setOutlet(new NewOutletBO());


        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getChannel(), 10);
    }

    @Test
    public void testBuildOutletForSubChannel() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(SUBCHANNEL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);
        mPresenter.setProfileConfig(testProfileConfig);

        mPresenter.setSubChannelMasterList(new ArrayList<>());

        SpinnerBO subchannelBO = new SpinnerBO(10, "Super market");
        given(view.getSubChannelSpinnerSelectedItem()).willReturn(subchannelBO);

        mPresenter.setOutlet(new NewOutletBO());

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getSubChannel(), 10);
    }

    @Test
    public void testBuildOutletForDistributor() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRIBUTOR);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);
        mPresenter.setProfileConfig(testProfileConfig);

        mPresenter.setmDistributorTypeMasterList(new ArrayList<>());

        DistributorMasterBO distributorMasterBO = new DistributorMasterBO("10", "Super market");
        given(view.getDistributorSpinnerSelectedItem()).willReturn(distributorMasterBO);

        mPresenter.setOutlet(new NewOutletBO());

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDistid(), "10");
    }

    @Test
    public void testBuildOutletWithEmptyFax() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FAX);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFax(), "0");

    }

    @Test
    public void testBuildOutletWithFax() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FAX);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFax(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyEmail() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(EMAIL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getEmail(), "");

    }

    @Test
    public void testBuildOutletWithEmail() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(EMAIL);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getEmail(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyCreditLimit() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CREDITLIMIT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCreditLimit(), "0");

    }

    @Test
    public void testBuildOutletWithCreditLimit() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CREDITLIMIT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCreditLimit(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyTinNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getTinno(), "0");

    }

    @Test
    public void testBuildOutletWithTinNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getTinno(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyPinCode() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPincode(), "");

    }

    @Test
    public void testBuildOutletWithPinCode() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPincode(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField3() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD3);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield3(), "");

    }

    @Test
    public void testBuildOutletWithRField3() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD3);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield3(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField8() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD8);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield8(), "");

    }

    @Test
    public void testBuildOutletWithRField8() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD8);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield8(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField9() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD9);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield9(), "");

    }

    @Test
    public void testBuildOutletWithRField9() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD9);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield9(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField10() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD10);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield3(), "");

    }

    @Test
    public void testBuildOutletWithRField10() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD10);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield10(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField11() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD11);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield11(), "");

    }

    @Test
    public void testBuildOutletWithRField11() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD11);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield11(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField12() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD12);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield12(), "");

    }

    @Test
    public void testBuildOutletWithRField12() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD12);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield12(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField13() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD13);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield13(), "");

    }

    @Test
    public void testBuildOutletWithRField13() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD13);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield13(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField14() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD14);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield14(), "");

    }

    @Test
    public void testBuildOutletWithRField14() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD14);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield14(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField15() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD15);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield15(), "");

    }

    @Test
    public void testBuildOutletWithRField15() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD15);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield15(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField16() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD16);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield16(), "");

    }

    @Test
    public void testBuildOutletWithRField16() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD16);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield16(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField17() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD17);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield17(), "");

    }

    @Test
    public void testBuildOutletWithRField17() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD17);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield17(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField18() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD18);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield18(), "");

    }

    @Test
    public void testBuildOutletWithRField18() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD18);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield18(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField19() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD19);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield19(), "");

    }

    @Test
    public void testBuildOutletWithRField19() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD19);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield19(), "Reliance");

    }

    @Test
    public void testBuildOutletWithTaxType() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TAXTYPE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        given(view.getTaxTypeSpinner()).willReturn(new StandardListBO("10", "Hello"));

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getTaxTypeId(), "10");

    }

    @Test
    public void testBuildOutletWithClass() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CLASS);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        given(view.getClassTypeSpinner()).willReturn(new StandardListBO("10", "Hello"));

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getClassTypeId(), "10");

    }

    @Test
    public void testBuildOutletWithUser() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(USER);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        mPresenter.isRouteEnabled = true;
        given(view.getUserSpinner()).willReturn(new UserMasterBO(1, "Hello"));

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getUserId(), 1);

    }

    @Test
    public void testBuildOutletWithUserWithRouteDisabled() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(USER);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        mPresenter.isRouteEnabled = false;
        given(view.getUserSpinner()).willReturn(new UserMasterBO(1, "Hello"));

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getUserId(), 0);

    }

    @Test
    public void testBuildOutletWithEmptyCreditPeriod() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CREDITPERIOD);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCreditDays(), "");

    }

    @Test
    public void testBuildOutletWithCreditPeriod() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CREDITPERIOD);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCreditDays(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyGSTNo() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getGstNum(), "");

    }

    @Test
    public void testBuildOutletWithGSTNo() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(GST_NO);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getGstNum(), "Reliance");

    }

    @Test
    public void testBuildOutletAtSez() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(IN_SEZ);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getIsSEZ(), 0);

    }

    @Test
    public void testBuildOutletOutsideSez() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(IN_SEZ);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.isSEZCheckBoxChecked()).willReturn(true);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getIsSEZ(), 1);

    }

    @Test
    public void testBuildOutletWithEmptyPanNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPanNo(), "");

    }

    @Test
    public void testBuildOutletWithPanNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAN_NUMBER);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPanNo(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyDrugLicenseNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDrugLicenseNo(), "");

    }

    @Test
    public void testBuildOutletWithDrugLicenseNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDrugLicenseNo(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyFoodLicenseNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFoodLicenseNo(), "");

    }

    @Test
    public void testBuildOutletWithFoodLicenseNumber() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_NUM);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFoodLicenseNo(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyTinExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getTinExpDate(), "");

    }

    @Test
    public void testBuildOutletWithTinExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(TIN_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("2019/12/12");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getTinExpDate(), "2019/12/12");

    }

    @Test
    public void testBuildOutletWithEmptyDrugExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDlExpDate(), "");

    }

    @Test
    public void testBuildOutletWithDrugExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DRUG_LICENSE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("2019/12/12");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDlExpDate(), "2019/12/12");

    }

    @Test
    public void testBuildOutletWithEmptyFoodLicenseExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFlExpDate(), "");

    }

    @Test
    public void testBuildOutletWithFoodLicenseExpDate() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(FOOD_LICENCE_EXP_DATE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicTextViewValues(0)).willReturn("2019/12/12");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getFlExpDate(), "2019/12/12");

    }

    @Test
    public void testBuildOutletWithEmptyRegion() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(REGION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRegion(), "");

    }

    @Test
    public void testBuildOutletWithRegion() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(REGION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRegion(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyCountry() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(COUNTRY);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCountry(), "");

    }

    @Test
    public void testBuildOutletWithCountry() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(COUNTRY);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getCountry(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyMobile() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(MOBILE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getMobile(), "");

    }

    @Test
    public void testBuildOutletWithMobile() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(MOBILE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getMobile(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyDistrict() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRICT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDistrict(), "");

    }

    @Test
    public void testBuildOutletWithDistrict() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(DISTRICT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getDistrict(), "Reliance");

    }

    @Test
    public void testBuildOutletWithEmptyRField4EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField4(), "0");
    }

    @Test
    public void testBuildOutletWithRField4EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField4(), "Reliance");
    }

    @Test
    public void testBuildOutletWithRField4Spinner() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD4);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getRField4Spinner()).willReturn(new RetailerFlexBO("100", "Name"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField4(), "100");
    }

    @Test
    public void testBuildOutletWithEmptyRField5EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield5(), "0");
    }

    @Test
    public void testBuildOutletWithRField5EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield5(), "Reliance");
    }

    @Test
    public void testBuildOutletWithRField5Spinner() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD5);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getRField5Spinner()).willReturn(new RetailerFlexBO("100", "Name"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield5(), "100");
    }

    @Test
    public void testBuildOutletWithEmptyRField6EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield6(), "0");
    }

    @Test
    public void testBuildOutletWithRField6EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield6(), "Reliance");
    }

    @Test
    public void testBuildOutletWithRField6Spinner() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD6);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getRField6Spinner()).willReturn(new RetailerFlexBO("100", "Name"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRfield6(), "100");
    }

    @Test
    public void testBuildOutletWithEmptyRField7EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField7(), "0");
    }

    @Test
    public void testBuildOutletWithRField7EditText() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(0);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getDynamicEditTextValues(0)).willReturn("Reliance");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField7(), "Reliance");
    }

    @Test
    public void testBuildOutletWithRField7Spinner() {
        mockConfigurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = false;

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(RFIELD7);
        storeName.setMenuName("Config_Menu");
        storeName.setHasLink(1);
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getRField7Spinner()).willReturn(new RetailerFlexBO("100", "Name"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getrField7(), "100");
    }

    @Test
    public void testBuildOutletWithPaymentType() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAYMENTTYPE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<PaymentType> paymentTypes = new ArrayList<>();
        paymentTypes.add(new PaymentType());
        mPresenter.setPaymentTypeList(paymentTypes);

        given(view.getPaymentType()).willReturn(new PaymentType(100, "Payment"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPayment(), "100");
    }

    @Test
    public void testBuildOutletWithoutPaymentType() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PAYMENTTYPE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<PaymentType> paymentTypes = new ArrayList<>();
        paymentTypes.add(new PaymentType());
        mPresenter.setPaymentTypeList(paymentTypes);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getPayment(), "0");
    }

    @Test
    public void testBuildOutletWithRoute() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ROUTE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        beatMasterBOS.add(new BeatMasterBO());
        mPresenter.setBeatMaster(beatMasterBOS);

        given(view.getRouteSpinner()).willReturn(new BeatMasterBO(100, "beat", 100));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRouteid(), 100);
    }

    @Test
    public void testBuildOutletWithoutRoute() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ROUTE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        beatMasterBOS.add(new BeatMasterBO());
        mPresenter.setBeatMaster(beatMasterBOS);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getRouteid(), 0);
    }


    @Test
    public void testBuildOutletWithPlan() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PLAN);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        beatMasterBOS.add(new BeatMasterBO());
        mPresenter.setBeatMaster(beatMasterBOS);

        given(view.getSelectedDays()).willReturn("Sun,Mon");
        given(view.getSelectedWeeks()).willReturn("Wk1,Wk2");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getWeekNo(), "Wk1,Wk2");
        assertEquals(mPresenter.getOutlet().getVisitDays(), "Sun,Mon");
    }

    @Test
    public void testBuildOutletWithEmptyPlan() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(PLAN);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        beatMasterBOS.add(new BeatMasterBO());
        mPresenter.setBeatMaster(beatMasterBOS);

        given(view.getSelectedDays()).willReturn("");
        given(view.getSelectedWeeks()).willReturn("");

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getWeekNo(), "");
        assertEquals(mPresenter.getOutlet().getVisitDays(), "");
    }


    @Test
    public void testBuildOutletWithContractStatus() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CONTRACT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<ContractStatus> contractStatuses = new ArrayList<>();
        contractStatuses.add(new ContractStatus());
        mPresenter.setContractStatusList(contractStatuses);

        given(view.getContractSpinnerSelectedItem()).willReturn(new ContractStatus(100, "beat"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getContractStatuslovid(), 100);
    }

    @Test
    public void testBuildOutletWithoutContractStatus() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(CONTRACT);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        ArrayList<ContractStatus> contractStatuses = new ArrayList<>();
        contractStatuses.add(new ContractStatus());
        mPresenter.setContractStatusList(contractStatuses);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getContractStatuslovid(), 0);
    }


    @Test
    public void testBuildOutletWithLocation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION)).willReturn(new LocationBO(100, "beat"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLocid(), 100);
    }

    @Test
    public void testBuildOutletWithoutLocation() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION)).willReturn(null);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLocid(), 0);
    }

    @Test
    public void testBuildOutletWithLocation1() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION1)).willReturn(new LocationBO(100, "beat"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLoc1id(), 100);
    }

    @Test
    public void testBuildOutletWithoutLocation1() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION1);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION1)).willReturn(null);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLoc1id(), 0);
    }


    @Test
    public void testBuildOutletWithLocation2() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION2)).willReturn(new LocationBO(100, "beat"));

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLoc2id(), 100);
    }

    @Test
    public void testBuildOutletWithoutLocation2() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LOCATION2);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getSelectedLocation(LOCATION2)).willReturn(null);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getLoc2id(), 0);
    }

    @Test
    public void testBuildOutletWithLatLong() {
        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(LATLONG);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        mPresenter.updateLatitude(12.00);
        mPresenter.updateLongitude(71.00);

        mPresenter.saveNewRetailer();

        assertEquals(mPresenter.getOutlet().getNewOutletlattitude(), 12.00,0);
        assertEquals(mPresenter.getOutlet().getNewOutletLongitude(), 71.00,0);
    }



    @Test
    public void testRetailerAlreadyExists() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should().showMessage(R.string.retailer_already_available);
    }

    @Test
    public void testSaveOutletWithDistributorChanged() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOutlet(new NewOutletBO());

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = true;
        given(dataManager.isOpenOrderExisting()).willReturn(true);
        mPresenter.setDistributorEnabled(true);
        given(dataManager.getRetailMaster()).willReturn(retailerMasterBO);
        given(view.getDistributorSpinnerSelectedItem()).willReturn(new DistributorMasterBO("15", "hello"));


        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should().showDistributorChangedDialog();

    }


    @Test
    public void testClearAndSaveEditProfileMode() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));
        given(profileDataManager.clearExistingOrder()).willReturn(Single.fromCallable(() -> true));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));

        ArrayList<ProductMasterBO> orderedProducts = new ArrayList<>();
        orderedProducts.add(new ProductMasterBO());
        mPresenter.setOrderedProductList(orderedProducts);

        mPresenter.clearOrdersAndSaveOutlet();
        testScheduler.triggerActions();

        then(profileDataManager).should().clearExistingOrder();
        assertEquals(mPresenter.getOrderedProductList().size(), 0);
        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveEditProfileMode() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveEditProfileModeWithNearbyRetailers() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.IS_NEARBY_RETAILER = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);


        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        ArrayList<String> selectedRetailers = new ArrayList<>();
        mPresenter.setSelectedRetailers(selectedRetailers);
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));
        given(profileDataManager.saveNearByRetailers(1 + "" + 5 + "12OCT1990", selectedRetailers)).willReturn(Single.fromCallable(() -> true));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should().showSuccessMessage();
        then(profileDataManager).should().saveNearByRetailers(1 + "" + 5 + "12OCT1990", selectedRetailers);
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncTokenExpired() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "-1"));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showSessionExpiredMessage();
        then(view).should().hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncUrlEmpty() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "-2"));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showAlertDialog(NOTIFY_URL_NOT_CONFIGURED);
        then(view).should().hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncUploadError() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> ""));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showAlertDialog(NOTIFY_UPLOAD_ERROR);
        then(view).should().hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncEmptyDownloadUrl() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "12345"));
        given(synchronizationDataManager.getSyncUrlList("SYNRET")).willReturn(Single.fromCallable(ArrayList::new));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showRetailerDownloadFailedMessage();
        then(view).should().hideLoading();

    }

    @Test
    public void testSaveProfileWithSync() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "12345"));
        given(synchronizationDataManager.getSyncUrlList("SYNRET")).willReturn(Single.fromCallable(new Callable<ArrayList<UrlMaster>>() {
            @Override
            public ArrayList<UrlMaster> call() throws Exception {
                ArrayList<UrlMaster> urlMasters = new ArrayList<>();
                UrlMaster urlMaster = new UrlMaster();
                urlMaster.setUrl("http://www.ivy.com");
                urlMaster.setIsMandatory(0);
                urlMasters.add(urlMaster);

                urlMaster.setUrl("http://www.ivy.co.in");
                urlMaster.setIsMandatory(0);
                urlMasters.add(urlMaster);

                return urlMasters;
            }
        }));

        given(dataManager.getAppVersionNumber()).willReturn("Ivy");
        given(dataManager.getAppVersionName()).willReturn("1.0.0");

        JSONObject json = new JSONObject();
        try {
            json.put("UserId", dataManager.getUser()
                    .getUserid());

            json.put("VersionCode", dataManager.getAppVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, dataManager.getAppVersionName());
            json.put("RetailerId", "12345");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TimeoutError volleyError = new TimeoutError();

        given(synchronizationDataManager.downloadDataFromServer(anyString(), any(JSONObject.class), anyBoolean())).willReturn(Single.fromCallable(() -> NewOutletTestDataFactory.successResponse));

        ArrayList<JSONObject> parsedObjects = new ArrayList<>();
        parsedObjects.add(NewOutletTestDataFactory.successResponse);
        parsedObjects.add(NewOutletTestDataFactory.successResponse);

        try {
            given(synchronizationDataManager.parseResponseJson(NewOutletTestDataFactory.successResponse)).willReturn(parsedObjects);
        } catch (IvyNetworkException e) {
            e.printStackTrace();
        }

        given(synchronizationDataManager.parseAndInsertJSON(NewOutletTestDataFactory.successResponse, false)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.deleteNewRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> true));

        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncWithNearbyEnabled() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        mockConfigurationMasterHelper.IS_NEARBY_RETAILER = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "12345"));
        given(synchronizationDataManager.getSyncUrlList("SYNRET")).willReturn(Single.fromCallable(new Callable<ArrayList<UrlMaster>>() {
            @Override
            public ArrayList<UrlMaster> call() throws Exception {
                ArrayList<UrlMaster> urlMasters = new ArrayList<>();
                UrlMaster urlMaster = new UrlMaster();
                urlMaster.setUrl("http://www.ivy.com");
                urlMaster.setIsMandatory(0);
                urlMasters.add(urlMaster);

                urlMaster.setUrl("http://www.ivy.co.in");
                urlMaster.setIsMandatory(0);
                urlMasters.add(urlMaster);

                return urlMasters;
            }
        }));

        given(dataManager.getAppVersionNumber()).willReturn("Ivy");
        given(dataManager.getAppVersionName()).willReturn("1.0.0");


        JSONObject json = new JSONObject();
        try {
            json.put("UserId", dataManager.getUser()
                    .getUserid());

            json.put("VersionCode", dataManager.getAppVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, dataManager.getAppVersionName());
            json.put("RetailerId", "12345");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TimeoutError volleyError = new TimeoutError();

        given(synchronizationDataManager.downloadDataFromServer(anyString(), any(JSONObject.class), anyBoolean())).willReturn(Single.fromCallable(() -> NewOutletTestDataFactory.successResponse));

        ArrayList<JSONObject> parsedObjects = new ArrayList<>();
        parsedObjects.add(NewOutletTestDataFactory.successResponse);
        parsedObjects.add(NewOutletTestDataFactory.successResponse);

        try {
            given(synchronizationDataManager.parseResponseJson(NewOutletTestDataFactory.successResponse)).willReturn(parsedObjects);
        } catch (IvyNetworkException e) {
            e.printStackTrace();
        }

        ArrayList<String> selectedRetailers = new ArrayList<>();
        mPresenter.setSelectedRetailers(selectedRetailers);
        given(profileDataManager.saveNearByRetailers("12345", selectedRetailers)).willReturn(Single.fromCallable(() -> true));

        given(synchronizationDataManager.parseAndInsertJSON(NewOutletTestDataFactory.successResponse, false)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.deleteNewRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> true));

        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNearByRetailers("12345", selectedRetailers);
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileWithSyncWithMandatoryError() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10")).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));

        given(view.isNetworkConnected()).willReturn(true);
        given(profileDataManager.syncNewOutlet(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> "12345"));
        given(synchronizationDataManager.getSyncUrlList("SYNRET")).willReturn(Single.fromCallable(new Callable<ArrayList<UrlMaster>>() {
            @Override
            public ArrayList<UrlMaster> call() throws Exception {
                ArrayList<UrlMaster> urlMasters = new ArrayList<>();
                UrlMaster urlMaster = new UrlMaster();
                urlMaster.setUrl("http://www.ivy.com");
                urlMaster.setIsMandatory(1);
                urlMasters.add(urlMaster);

                return urlMasters;
            }
        }));

        given(dataManager.getAppVersionNumber()).willReturn("Ivy");
        given(dataManager.getAppVersionName()).willReturn("1.0.0");

        JSONObject json = new JSONObject();
        try {
            json.put("UserId", dataManager.getUser()
                    .getUserid());

            json.put("VersionCode", dataManager.getAppVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, dataManager.getAppVersionName());
            json.put("RetailerId", "12345");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TimeoutError volleyError = new TimeoutError();

        given(synchronizationDataManager.downloadDataFromServer(anyString(), any(JSONObject.class), anyBoolean())).willReturn(Single.fromCallable(() -> NewOutletTestDataFactory.errorResponse));

        ArrayList<JSONObject> parsedObjects = new ArrayList<>();
        parsedObjects.add(NewOutletTestDataFactory.successResponse);
        parsedObjects.add(NewOutletTestDataFactory.successResponse);

        try {
            given(synchronizationDataManager.parseResponseJson(NewOutletTestDataFactory.successResponse)).willReturn(parsedObjects);
        } catch (IvyNetworkException e) {
            e.printStackTrace();
        }

        given(synchronizationDataManager.parseAndInsertJSON(NewOutletTestDataFactory.successResponse, false)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.deleteNewRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> true));

        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));

        try {
            given(synchronizationDataManager.parseResponseJson(NewOutletTestDataFactory.errorResponse)).willThrow(new IvyNetworkException("E05"));
        } catch (IvyNetworkException e) {
            e.printStackTrace();
        }
        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(profileDataManager).should().deleteNewRetailerTablesForEdit(1 + "" + 5 + "12OCT1990", "10");
        then(view).should().isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(view).should().showServerErrorMessage("E05");
        then(synchronizationDataManager).should().stopAllRequest();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileModeWithContactTab() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.OTHER.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;
        mockConfigurationMasterHelper.IS_CONTACT_TAB = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(ArrayList::new));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should(never()).saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileModeWithPriorityProducts() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        mPresenter.setProfileConfig(testProfileConfig);
        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        ArrayList<StandardListBO> priorityProducts = new ArrayList<>();
        priorityProducts.add(new StandardListBO("0", "Hello"));
        priorityProducts.add(new StandardListBO("1", "Hello1"));
        newOutlet.setPriorityProductList(priorityProducts);
        mPresenter.setOutlet(newOutlet);

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.OTHER.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));

        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;
        mockConfigurationMasterHelper.IS_CONTACT_TAB = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);
        mPresenter.setUid("12OCT1990");
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));
        given(profileDataManager.savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts)).willReturn(Single.fromCallable(() -> true));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts);
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should(never()).saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileModeWithOrderedProducts() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        ArrayList<StandardListBO> priorityProducts = new ArrayList<>();
        priorityProducts.add(new StandardListBO("0", "Hello"));
        priorityProducts.add(new StandardListBO("1", "Hello1"));
        newOutlet.setPriorityProductList(priorityProducts);


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;
        mockConfigurationMasterHelper.IS_CONTACT_TAB = true;
        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        ArrayList<ProductMasterBO> orderedProducts = new ArrayList<>();
        orderedProducts.add(new ProductMasterBO());

        OrderHeader orderHeader = new OrderHeader();

        mPresenter.setUid("12OCT1990");
        mPresenter.setOutlet(newOutlet);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOrderedProductList(orderedProducts);
        mPresenter.setOrderHeader(orderHeader);

        given(dataManager.getUser()).willReturn(userMasterBO);
        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.OTHER.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));
        given(profileDataManager.savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveOrderDetails(1 + "" + 5 + "12OCT1990", orderHeader, orderedProducts)).willReturn(Single.fromCallable(() -> true));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts);
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should(never()).saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveOrderDetails(1 + "" + 5 + "12OCT1990", orderHeader, orderedProducts);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testSaveProfileModeWithOpportunityProducts() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(STORENAME);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);

        storeName = new ConfigureBO();
        storeName.setConfigCode(PINCODE);
        storeName.setMenuName("Config_Menu");
        testProfileConfig.add(storeName);


        NewOutletBO newOutlet = new NewOutletBO();
        newOutlet.setRetailerId("10");
        ArrayList<StandardListBO> priorityProducts = new ArrayList<>();
        priorityProducts.add(new StandardListBO("0", "Hello"));
        priorityProducts.add(new StandardListBO("1", "Hello1"));
        newOutlet.setPriorityProductList(priorityProducts);


        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
        retailerMasterBO.setDistributorId(10);

        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_ORDER = false;
        mockConfigurationMasterHelper.IS_CONTACT_TAB = true;
        mockConfigurationMasterHelper.SHOW_NEW_OUTLET_OPPR = true;

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        ArrayList<ProductMasterBO> opportunityProducts = new ArrayList<>();
        opportunityProducts.add(new ProductMasterBO());


        mPresenter.setUid("12OCT1990");
        mPresenter.setOutlet(newOutlet);
        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.setOpportunityProductList(opportunityProducts);

        given(dataManager.getUser()).willReturn(userMasterBO);
        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.OTHER.getMenuType());
        given(view.getDynamicEditTextValues(0)).willReturn("Name");
        given(view.getDynamicEditTextValues(1)).willReturn("600036");
        given(profileDataManager.checkRetailerAlreadyAvailable("Name", "600036")).willReturn(Single.fromCallable(() -> false));
        given(profileDataManager.saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet)).willReturn(Single.fromCallable(() -> true));
        given(retailerDataManager.fetchRetailers()).willReturn(Observable.fromCallable(() -> new ArrayList<>()));
        given(profileDataManager.savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts)).willReturn(Single.fromCallable(() -> true));
        given(profileDataManager.saveOpportunityDetails(1 + "" + 5 + "12OCT1990", opportunityProducts)).willReturn(Single.fromCallable(() -> true));

        mPresenter.saveNewRetailer();
        testScheduler.triggerActions();

        then(view).should(never()).isNetworkConnected();
        then(profileDataManager).should().savePriorityProducts(1 + "" + 5 + "12OCT1990", priorityProducts);
        then(profileDataManager).should().saveNewOutlet(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should(never()).saveNewOutletContactInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveNewOutletAddressInformation(1 + "" + 5 + "12OCT1990", newOutlet);
        then(profileDataManager).should().saveOpportunityDetails(1 + "" + 5 + "12OCT1990", opportunityProducts);
        then(retailerDataManager).should().fetchRetailers();
        then(view).should().showSuccessMessage();
        then(view).should(atLeast(2)).hideLoading();

    }

    @Test
    public void testHomeButtonClickWithSurveyAvailable() {
        mPresenter.setUid("12OCT1990");

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);

        given(surveyDataManager.isSurveyAvailableForRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> true));

        mPresenter.getHomeButtonClick();
        testScheduler.triggerActions();

        then(view).should().showAlertDialog(R.string.are_you_sure_to_close_without_savingthe_data);

    }

    @Test
    public void testHomeButtonClickWithoutSurveyViewMode() {
        mPresenter.setUid("12OCT1990");

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);

        given(surveyDataManager.isSurveyAvailableForRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> false));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());

        mPresenter.getHomeButtonClick();
        testScheduler.triggerActions();

        then(view).should().doFinishActivity();

    }

    @Test
    public void testHomeButtonClickWithoutSurveyEditMode() {
        mPresenter.setUid("12OCT1990");

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);

        given(surveyDataManager.isSurveyAvailableForRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> false));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());

        mPresenter.getHomeButtonClick();
        testScheduler.triggerActions();

        then(view).should().doFinishActivity();

    }

    @Test
    public void testHomeButtonClickWithoutSurveyCreateMode() {
        mPresenter.setUid("12OCT1990");

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);

        given(surveyDataManager.isSurveyAvailableForRetailer(1 + "" + 5 + "12OCT1990")).willReturn(Single.fromCallable(() -> false));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType());

        mPresenter.getHomeButtonClick();
        testScheduler.triggerActions();

        then(view).should().doFinishActivity();

    }

    @Test
    public void testDownLoadNearByRetailers() {

        ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();
        RetailerMasterBO retailerMasterBO = new RetailerMasterBO("10", "Hello");
        retailerMasterBOS.add(retailerMasterBO);

        ArrayList<String> selectedIds = new ArrayList<>();
        selectedIds.add("10");

        given(view.getRetailerId()).willReturn("1");

        given(profileDataManager.getLinkRetailerForADistributor(5)).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                return retailerMasterBOS;
            }
        }));

        mockConfigurationMasterHelper.VALUE_NEARBY_RETAILER_MAX = 1;

        given(profileDataManager.downloadNearbyRetailers("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                return selectedIds;
            }
        }));

        mPresenter.getLinkRetailerList(5);
        testScheduler.triggerActions();

        then(view).should().showNearByRetailersDialog(retailerMasterBOS, 1);
    }

    @Test
    public void testSurveyClickInvalidData() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("");

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.onSurveyMenuClick();

        then(dataManager).should(never()).isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER);

    }

    @Test
    public void testSurveyClickFloatingSurveyDisabled() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("Hello");

        given(dataManager.isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER)).willReturn(Single.fromCallable(() -> false));

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.onSurveyMenuClick();
        testScheduler.triggerActions();

        then(profileDataManager).should(never()).setupSurveyScreenData(anyString());

    }

    @Test
    public void testSurveyClickForEditType() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("Hello");

        given(dataManager.isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER)).willReturn(Single.fromCallable(() -> true));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.EDIT.getMenuType());

        given(profileDataManager.setupSurveyScreenData(any())).willReturn(Single.fromCallable(() -> true));


        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("10");
        mPresenter.setOutlet(newOutletBO);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.onSurveyMenuClick();
        testScheduler.triggerActions();

        then(profileDataManager).should().setupSurveyScreenData("10");
        then(view).should().callSurveyActivity();

    }

    @Test
    public void testSurveyClickForViewType() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("Hello");

        given(dataManager.isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER)).willReturn(Single.fromCallable(() -> true));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.VIEW.getMenuType());

        given(profileDataManager.setupSurveyScreenData(any())).willReturn(Single.fromCallable(() -> true));

        NewOutletBO newOutletBO = new NewOutletBO();
        newOutletBO.setRetailerId("10");
        mPresenter.setOutlet(newOutletBO);

        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.onSurveyMenuClick();
        testScheduler.triggerActions();

        then(profileDataManager).should().setupSurveyScreenData("10");
        then(view).should().callSurveyActivity();

    }

    @Test
    public void testSurveyClickForOtherType() {

        ArrayList<ConfigureBO> testProfileConfig = new ArrayList<>();
        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(ADDRESS1);
        storeName.setMenuName("Address 1");
        storeName.setMandatory(1);
        testProfileConfig.add(storeName);

        given(view.getDynamicEditTextValues(0)).willReturn("Hello");

        given(dataManager.isFloatingSurveyEnabled(NewRetailerConstant.MENU_NEW_RETAILER)).willReturn(Single.fromCallable(() -> true));

        given(view.getScreenMode()).willReturn(NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType());

        given(profileDataManager.setupSurveyScreenData(any())).willReturn(Single.fromCallable(() -> true));

        mPresenter.setUid("12OCT1990");

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(5);
        userMasterBO.setDistributorid(1);

        given(dataManager.getUser()).willReturn(userMasterBO);


        mPresenter.setProfileConfig(testProfileConfig);
        mPresenter.onSurveyMenuClick();
        testScheduler.triggerActions();

        then(profileDataManager).should().setupSurveyScreenData(1 + "" + 5 + "12OCT1990");
        then(view).should().callSurveyActivity();

    }

    @Test
    public void testOpportunityProductsClick() {

        given(profileDataManager.setUpOpportunityProductsData()).willReturn(Single.fromCallable(() -> true));

        mPresenter.onOpportunityProductsMenuClicked();
        testScheduler.triggerActions();

        then(view).should().showLoading();
        then(view).should().navigateToOpportunityProductsScreen();
        then(view).should().hideLoading();
    }

    @Test
    public void testOnOrderMenuClick() {

        given(profileDataManager.setUpOrderScreen()).willReturn(Single.fromCallable(() -> true));

        mPresenter.onOrderMenuClick();
        testScheduler.triggerActions();

        then(view).should().showLoading();
        then(view).should().navigateToNewOutletOrderScreen();
        then(view).should().hideLoading();
    }

    @Test
    public void testOnOrderMenuClickDistributorEnabled() {
        mPresenter.setDistributorEnabled(true);

        given(profileDataManager.setUpOrderScreen()).willReturn(Single.fromCallable(() -> true));
        given(view.getDistributorSpinnerSelectedItem()).willReturn(new DistributorMasterBO("0", "Hello"));

        mPresenter.onOrderMenuClick();
        testScheduler.triggerActions();

        then(view).should().showMessage(R.string.select_distributor);
        then(view).should(never()).showLoading();
        then(view).should(never()).navigateToNewOutletOrderScreen();
        then(view).should(never()).hideLoading();
    }


    @After
    public void tearDown() {
        mPresenter.onDetach();
    }


}