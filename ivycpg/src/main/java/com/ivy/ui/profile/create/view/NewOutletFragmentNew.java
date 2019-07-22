package com.ivy.ui.profile.create.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.maplib.BaiduMapDialogue;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ChannelBO;
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
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.MapDialogue;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.OpportunityNewOutlet;
import com.ivy.sd.png.view.OrderNewOutlet;
import com.ivy.sd.png.view.PrioritySelectionDialog;
import com.ivy.ui.profile.create.INewRetailerContract;
import com.ivy.ui.profile.create.NewRetailerConstant;
import com.ivy.ui.profile.create.di.DaggerNewRetailerComponent;
import com.ivy.ui.profile.create.di.NewRetailerCreationModule;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.PaymentType;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.ivy.ui.profile.create.NewRetailerConstant.DAY_TEXT_LABEL;
import static com.ivy.ui.profile.create.NewRetailerConstant.LOCATION;
import static com.ivy.ui.profile.create.NewRetailerConstant.WEEK_TEXT_LABEL;


public class NewOutletFragmentNew extends BaseFragment
        implements INewRetailerContract.INewRetailerView,
        PrioritySelectionDialog.PrioritySelectionListener,
        NearByRetailerDialog.NearByRetailerInterface {

    public static final int NEW_RETAILER_ORDER_CREATION = 1000;
    public static final int NEW_RETAILER_OPPORTUNITY_PRODUCTS = 1002;
    private static final int LOCATION_REQUEST_CODE = 1001;

    @Inject
    INewRetailerContract.INewRetailerPresenter<INewRetailerContract.INewRetailerView> mNewRetailerPresenter;

    @BindView(R.id.new_outlet_save)
    Button mButtonSave;

    @BindView(R.id.bottom_layout)
    LinearLayout footerLinearLayout;

    @BindView(R.id.scrollview2)
    ScrollView mScrollView;

    @BindView(R.id.rootLinearLayout)
    LinearLayout mRootLinearLayout;


    private TextInputLayout mEditTextInputLayout;
    private TextView latLongTextView;
    private AppCompatAutoCompleteTextView priorityProductAutoCompleteTextView;
    private AppCompatAutoCompleteTextView nearbyAutoCompleteTextView;
    private AppCompatCheckBox isSEZCheckBox = null;
    private PrioritySelectionDialog prioritySelectionDialog;


    private LinearLayout.LayoutParams commonsParams;
    private LinearLayout.LayoutParams editWeightMargin;
    private LinearLayout.LayoutParams commonsparams3;
    private LinearLayout.LayoutParams weight1;
    private LinearLayout.LayoutParams spinnerMargin;
    private LinearLayout.LayoutParams mandatoryTextViewParams;
    private LinearLayout.LayoutParams params;
    private LinearLayout.LayoutParams latlongTextViewWeight;
    private LinearLayout.LayoutParams weight2;


    private ArrayAdapter<LocationBO> locationAdapter;
    private ArrayAdapter<LocationBO> locationAdapter1;
    private ArrayAdapter<LocationBO> locationAdapter2;
    private ArrayAdapter<BeatMasterBO> routeAdapter;
    private ArrayAdapter<ChannelBO> channelAdapter;
    private ArrayAdapter<SpinnerBO> subChannelAdapter;

    private ArrayList<InputFilter> inputFilters = null;
    private ArrayList<StandardListBO> priorityProductIDList;
    private ArrayList<DistributorMasterBO> mDistributorTypeMasterList;


    private int screenWidth = 0;
    private int screenMode = 0;
    private int channelId = 0;
    private String channelName;
    private String retailerId = "";
    private int selectedPosition = -1;
    private int imageId = 0;
    private long startClickTime;
    private String routeMenuName = "";
    private String imageName;
    private String weekNoStr = null;
    private String beatName = null;
    private String PHOTO_PATH = "";


    private SparseArray<AppCompatEditText> editTextHashMap;

    private SparseArray<TextView> textViewHashMap;

    private SparseArray<AppCompatCheckBox> daysCheckBoxHashMap;

    private SparseArray<AppCompatCheckBox> weekNoCheckBox;

    private HashMap<String, MaterialSpinner> spinnerHashMap = new HashMap<>();


    private ArrayList<String> imageNameList;

    private ArrayList<Integer> imageIdList;

    @Override
    public void initializeDi() {
        DaggerNewRetailerComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .newRetailerCreationModule(new NewRetailerCreationModule(this))
                .build().inject(this);
        setBasePresenter((BasePresenter) mNewRetailerPresenter);

        mNewRetailerPresenter.init();
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_newoutlet;
    }

    @Override
    public void init(View view) {
        initializeToolbarAction();
        getScreenAspectRatio();
        initializeLayoutParams();
    }


    @Override
    protected void setUpViews() {
        mButtonSave.setTypeface(FontUtils.getFontBalooHai(getContext(), FontUtils.FontType.REGULAR));
    }

    @Override
    protected void getMessageFromAliens() {
        screenMode = getActivity().getIntent().getIntExtra("screenMode", 0);
        retailerId = getActivity().getIntent().getStringExtra("retailerId");
        channelId = getActivity().getIntent().getIntExtra("channelId", 0);
        channelName = getActivity().getIntent().getStringExtra("channelName");

        if (getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType())
            hideFooterView();

        mNewRetailerPresenter.getSavedOutletData();
        mNewRetailerPresenter.loadInitialData();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getScreenMode() {
        return screenMode;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public String getRetailerId() {
        return retailerId;
    }


    @Override
    public void hideFooterView() {
        footerLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public ArrayList<Integer> getImageIdList() {
        return imageIdList;
    }

    @Override
    public ArrayList<String> getImageNameList() {
        return imageNameList;
    }

    @Override
    public void setImageNameList(ArrayList<String> imageNameList) {
        this.imageNameList = imageNameList;
    }

    @Override
    public void setImageIdList(ArrayList<Integer> imageIdList) {
        this.imageIdList = imageIdList;
    }

    @Override
    public void addLengthFilter(String regex) {
        if (inputFilters == null)
            inputFilters = new ArrayList<>();
        else
            inputFilters.clear();
        inputFilters.add(AppUtils.getInputFilter(regex));
    }


    @Override
    public void addRegexFilter(String regex) {
        final String reg;
        try {
            if (regex != null && !regex.isEmpty()) {
                if (regex.contains("<") && regex.contains(">")) {
                    reg = regex.replaceAll("\\<.*?\\>", "");
                } else {
                    reg = regex;
                }
                //data.replaceAll("\\(.*?\\)", "()"); //if you want to keep the brackets
                InputFilter filter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            String checkMe = String.valueOf(source.charAt(i));
                            if (!Pattern.compile(reg).matcher(checkMe).matches()) {
                                Commons.print("invalid");
                                return "";
                            }
                        }
                        return null;
                    }
                };
                inputFilters.add(filter);
            }
        } catch (Exception ex) {
            Commons.printException("regex check", ex);
        }
    }


    @Override
    public void createNewRetailerDetailsField(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode) {

        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsParams);
    }

    @Override
    public void createNewRetailerContactPersonOne(int menuNumber, String menuName, boolean mandatory,
                                                  boolean isUppercaseLetter, String mConfigCode,
                                                  boolean isContactTitle) {
        mRootLinearLayout.addView(createContactList(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, isContactTitle, NewRetailerConstant.CONTACT_PERSON1), commonsParams);
    }

    @Override
    public void createNewRetailerContactPersonTwo(int mNumber, String mName,
                                                  boolean mandatory, boolean isUppercaseLetter,
                                                  String configCode, boolean isContactTitle) {
        mRootLinearLayout.addView(createContactList(mNumber, mName, mandatory, isUppercaseLetter, configCode, isContactTitle, NewRetailerConstant.CONTACT_PERSON2), commonsParams);

    }

    @Override
    public void createNewRetailerContactType(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode) {

        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, InputType.TYPE_CLASS_NUMBER), commonsParams);
    }

    @Override
    public void createNewRetailerCreditPeriod(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode) {

        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, InputType.TYPE_CLASS_NUMBER, true), commonsParams);
    }

    @Override
    public void createNewRetailerContactEmail(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode) {

        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS), commonsParams);
    }

    @Override
    public String getContactPersonTitle(boolean option) {
        if (option)
            return getResources().getString(R.string.contact_title);
        else
            return getResources().getString(R.string.other_reason_with_credit);
    }


    @Override
    public ArrayList<LocationBO> getLocationAdapter() {
        if (locationAdapter != null) {
            ArrayList<LocationBO> locationBOS = new ArrayList<>();
            for (int i = 0; i < locationAdapter.getCount(); i++) {
                locationBOS.add(locationAdapter.getItem(i));
            }
            return locationBOS;
        }
        return null;
    }

    @Override
    public ArrayList<LocationBO> getLocationAdapter1() {
        if (locationAdapter1 != null) {
            ArrayList<LocationBO> locationBOS = new ArrayList<>();
            for (int i = 0; i < locationAdapter1.getCount(); i++) {
                locationBOS.add(locationAdapter1.getItem(i));
            }
            return locationBOS;
        }

        return null;
    }


    @Override
    public ArrayList<LocationBO> getLocationAdapter2() {

        if (locationAdapter2 != null) {
            ArrayList<LocationBO> locationBOS = new ArrayList<>();
            for (int i = 0; i < locationAdapter2.getCount(); i++) {
                locationBOS.add(locationAdapter2.getItem(i));
            }
            return locationBOS;
        }

        return null;
    }

    @Override
    public ArrayList<BeatMasterBO> getRouteAdapter() {
        if (routeAdapter != null) {
            ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
            for (int i = 0; i < routeAdapter.getCount(); i++) {
                beatMasterBOS.add(routeAdapter.getItem(i));
            }
            return beatMasterBOS;
        }
        return null;
    }


    @Override
    public void updateContactPersonSelectedTitle(int menuNumber, int position, String value, String spinnerKey) {
        spinnerHashMap.get(spinnerKey).setSelection(position);
        editTextHashMap.get(menuNumber + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY).setText(value);
    }

    @Override
    public void createChannelSpinner(boolean mandatory, int menuNumber, String menuName) {

        MaterialSpinner channel = createNewMaterialSpinner(mandatory, menuNumber, menuName, "");

        ArrayList<ChannelBO> channelBOS = new ArrayList<>();

        channelBOS.add(new ChannelBO(0, getResources().getString(R.string.select_str) + " " + menuName));
        if (mNewRetailerPresenter.getChannelList() != null)
            channelBOS.addAll(mNewRetailerPresenter.getChannelList());

        channelAdapter = (ArrayAdapter<ChannelBO>) getArrayAdapter(channelBOS);

        channel.setAdapter(channelAdapter);

        channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (channel.getSelectedItemPosition() != 0) {
                    ChannelBO tempBo = (ChannelBO) parent.getSelectedItem();
                    loadSubChannel(tempBo.getChannelId());
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() ||
                screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {

            channel.setEnabled(screenMode != NewRetailerConstant.MenuType.VIEW.getMenuType());

            mNewRetailerPresenter.getChannelSelectedItem(menuNumber);

        }
        spinnerHashMap.put(NewRetailerConstant.CHANNEL, channel);

    }

    @Override
    public void createContactSpinner(boolean mandatory, int menuNumber, String menuName, String configCode) {
        MaterialSpinner contractSpinner = createNewMaterialSpinner(mandatory, menuNumber, menuName, configCode);

        ArrayList<ContractStatus> mContractStatusList = new ArrayList<>();
        mContractStatusList.add(0, new ContractStatus(0, getResources().getString(R.string.select_str) + " " + menuName));
        if (mNewRetailerPresenter.getContractStatusList() != null)
            mContractStatusList.addAll(mNewRetailerPresenter.getContractStatusList());

        ArrayAdapter<ContractStatus> contractStatusAdapter = (ArrayAdapter<ContractStatus>) getArrayAdapter(mContractStatusList);
        contractSpinner.setAdapter(contractStatusAdapter);

        spinnerHashMap.put(NewRetailerConstant.CONTRACT, contractSpinner);

    }

    @Override
    public void createSubChannelSpinner(boolean mandatory, int menuNumber, String menuName) {

        MaterialSpinner subChannel = createNewMaterialSpinner(mandatory, menuNumber, menuName, "");

        subChannelAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        subChannelAdapter.setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);
        subChannelAdapter.add(new SpinnerBO(0, getResources().getString(R.string.select_str) + menuName));
        subChannel.setAdapter(subChannelAdapter);

        spinnerHashMap.put(NewRetailerConstant.SUBCHANNEL, subChannel);
    }


    @Override
    public void createRouteSpinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner route = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<BeatMasterBO> routeList = new ArrayList<>();

        routeList.add(new BeatMasterBO(0, getString(R.string.select_str) + " " + mName, 0));
        if (mNewRetailerPresenter.getCurrentUserRoutes() != null)
            routeList.addAll(mNewRetailerPresenter.getCurrentUserRoutes());

        routeAdapter = (ArrayAdapter<BeatMasterBO>) getArrayAdapter(routeList);
        routeMenuName = mName;

        route.setAdapter(routeAdapter);

        spinnerHashMap.put(NewRetailerConstant.ROUTE, route);

    }

    @Override
    public void createLocationSpinner(boolean mandatory, int mNumber, String mName, String configCode,
                                      boolean isLocation1) {
        MaterialSpinner location = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<LocationBO> mLocationMasterList = new ArrayList<>();
        mLocationMasterList.add(0, new LocationBO(0,
                getResources().getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getLocationList() != null)
            mLocationMasterList.addAll(mNewRetailerPresenter.getLocationList());

        locationAdapter = (ArrayAdapter<LocationBO>) getArrayAdapter(mLocationMasterList);
        location.setAdapter(locationAdapter);

        spinnerHashMap.put(NewRetailerConstant.LOCATION, location);

        if (!isLocation1) {
            defaultLocationSpinner(NewRetailerConstant.LOCATION);
        }
    }

    @Override
    public void createLocation1Spinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner location = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<LocationBO> mLocationMasterList = new ArrayList<>();
        mLocationMasterList.add(0, new LocationBO(0,
                getResources().getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getLocation1List() != null)
            mLocationMasterList.addAll(mNewRetailerPresenter.getLocation1List());

        locationAdapter1 = (ArrayAdapter<LocationBO>) getArrayAdapter(mLocationMasterList);

        location.setAdapter(locationAdapter1);

        spinnerHashMap.get(NewRetailerConstant.LOCATION).setAdapter(locationAdapter1);

        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                mNewRetailerPresenter.getOutlet().setLoc1id(tempBo.getLocId());
                updateLocationAdapter1(tempBo.getLocId());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        spinnerHashMap.put(NewRetailerConstant.LOCATION1, location);
        defaultLocationSpinner(NewRetailerConstant.LOCATION1);

    }

    @Override
    public void createLocation2Spinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner location3 = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<LocationBO> mLocationMasterList3 = new ArrayList<>();
        mLocationMasterList3.add(0, new LocationBO(0,
                getResources().getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getLocation2List() != null)
            mLocationMasterList3.addAll(mNewRetailerPresenter.getLocation2List());

        locationAdapter2 = (ArrayAdapter<LocationBO>) getArrayAdapter(mLocationMasterList3);
        location3.setAdapter(locationAdapter2);


        location3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                mNewRetailerPresenter.getOutlet().setLoc2id(tempBo.getLocId());
                updateLocationAdapter2(tempBo.getLocId());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        spinnerHashMap.put(NewRetailerConstant.LOCATION2, location3);

        defaultLocationSpinner(NewRetailerConstant.LOCATION2);

    }

    @Override
    public void createPaymentType(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner paymentType = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<PaymentType> mretailertypeMasterList = new ArrayList<>();
        mretailertypeMasterList.add(0, new PaymentType(0,
                getResources().getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getRetailerPaymentTypeList() != null)
            mretailertypeMasterList.addAll(mNewRetailerPresenter.getRetailerPaymentTypeList());

        ArrayAdapter<PaymentType> retailerPaymentTypeAdapter = (ArrayAdapter<PaymentType>) getArrayAdapter(mretailertypeMasterList);

        paymentType.setAdapter(retailerPaymentTypeAdapter);

        spinnerHashMap.put(NewRetailerConstant.PAYMENTTYPE, paymentType);

    }

    private DistributorMasterBO selectedDistributor;

    @Override
    public void createDistributor(boolean mandatory, int mNumber, String mName, String configCode) {

        MaterialSpinner distributorSpinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        if (mDistributorTypeMasterList == null)
            mDistributorTypeMasterList = new ArrayList<>();

        mDistributorTypeMasterList.add(0, new DistributorMasterBO("0", getResources().getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getDistributorTypeMasterList() != null)
            mDistributorTypeMasterList.addAll(mNewRetailerPresenter.getDistributorTypeMasterList());

        ArrayAdapter<DistributorMasterBO> distributorTypeAdapter = (ArrayAdapter<DistributorMasterBO>) getArrayAdapter(mDistributorTypeMasterList);
        distributorSpinner.setAdapter(distributorTypeAdapter);

        distributorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedDistributor = (DistributorMasterBO) parent.getSelectedItem();
                mNewRetailerPresenter.getRetailerRoutes(selectedDistributor.getDId());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinnerHashMap.put(NewRetailerConstant.DISTRIBUTOR, distributorSpinner);
    }

    private MaterialSpinner createNewMaterialSpinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner materialSpinner = new MaterialSpinner(getActivity());
        materialSpinner.setId(mNumber);
        materialSpinner.setFloatingLabelText(mName);
        setSpinnerView(mandatory, materialSpinner, configCode);
        materialSpinner.setOnItemSelectedListener(spinnerSelectedListener);
        return materialSpinner;
    }


    private ArrayAdapter<?> getArrayAdapter(ArrayList<?> adapterList) {

        ArrayAdapter<?> spinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, adapterList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);

        return spinnerAdapter;
    }

    @Override
    public void updateRouteSpinnerData(ArrayList<BeatMasterBO> beatMasterBOS) {

        if (!selectedDistributor.getDId().equals("0")
                && spinnerHashMap.get(NewRetailerConstant.ROUTE) != null && routeAdapter != null) {

            routeAdapter.clear();
            routeAdapter.add(new BeatMasterBO(0,
                    getActivity().getResources().getString(R.string.select_str) + " " + routeMenuName, 0));
            routeAdapter.addAll(beatMasterBOS);
            spinnerHashMap.get(NewRetailerConstant.ROUTE).setAdapter(routeAdapter);
        }
    }

    @Override
    public void createTaxTypeSpinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner taxTypeSpinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<StandardListBO> taxTypeList = new ArrayList<>();
        StandardListBO standardListBO = new StandardListBO();
        standardListBO.setListID(0 + "");
        standardListBO.setListName(getResources().getString(R.string.select_str) + mName);
        taxTypeList.add(0, standardListBO);
        if (mNewRetailerPresenter.getTaxTypeList() != null)
            taxTypeList.addAll(mNewRetailerPresenter.getTaxTypeList());

        ArrayAdapter<StandardListBO> taxTypeAdapter = (ArrayAdapter<StandardListBO>) getArrayAdapter(taxTypeList);
        taxTypeSpinner.setAdapter(taxTypeAdapter);

        spinnerHashMap.put(NewRetailerConstant.TAXTYPE, taxTypeSpinner);

    }


    @Override
    public void createClassTypeSpinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner classSpinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<StandardListBO> classTypeList = new ArrayList<>();
        StandardListBO standardListBO = new StandardListBO();
        standardListBO.setListID(0 + "");
        standardListBO.setListName(getResources().getString(R.string.select_str) + " " + mName);
        classTypeList.add(0, standardListBO);
        if (mNewRetailerPresenter.getTaxTypeList() != null)
            classTypeList.addAll(mNewRetailerPresenter.getTaxTypeList());

        ArrayAdapter<StandardListBO> classTypeAdapter = (ArrayAdapter<StandardListBO>) getArrayAdapter(classTypeList);

        classSpinner.setAdapter(classTypeAdapter);

        spinnerHashMap.put(NewRetailerConstant.CLASS, classSpinner);


    }

    @Override
    public void createUserSpinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner userSpinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<UserMasterBO> userMasterBOS = new ArrayList<>();
        UserMasterBO bo = new UserMasterBO();
        bo.setUserid(0);
        bo.setUserName(getResources().getString(R.string.select_str) + mName);
        userMasterBOS.add(0, bo);

        if (mNewRetailerPresenter.getUserList() != null)
            userMasterBOS.addAll(mNewRetailerPresenter.getUserList());

        ArrayAdapter<UserMasterBO> userAdapter = (ArrayAdapter<UserMasterBO>) getArrayAdapter(userMasterBOS);
        userSpinner.setAdapter(userAdapter);

        //Pre select login user
        if (mNewRetailerPresenter.getUserList() != null)
            for (int i = 0; i < mNewRetailerPresenter.getUserList().size(); i++) {
                if (mNewRetailerPresenter.getUserList().get(i).getUserid() == mNewRetailerPresenter.getUserMasterBO().getUserid()) {
                    userSpinner.setSelection(i + 1);
                    break;
                }
            }
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerHashMap.get(NewRetailerConstant.ROUTE) != null) {
                    updateBeat(userMasterBOS.get(position).getUserid());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    @Override
    public void createRField5Spinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner rField5Spinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<RetailerFlexBO> retailerFlexBOS = new ArrayList<>();

        retailerFlexBOS.add(new RetailerFlexBO("0", getActivity().getResources()
                .getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getRField5List() != null)
            retailerFlexBOS.addAll(mNewRetailerPresenter.getRField5List());

        ArrayAdapter<RetailerFlexBO> rField5Adapter = (ArrayAdapter<RetailerFlexBO>) getArrayAdapter(retailerFlexBOS);
        rField5Spinner.setAdapter(rField5Adapter);

        spinnerHashMap.put(NewRetailerConstant.RFIELD5, rField5Spinner);
    }


    @Override
    public void createRField6Spinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner rField6Spinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<RetailerFlexBO> retailerFlexBOS = new ArrayList<>();

        retailerFlexBOS.add(new RetailerFlexBO("0", getActivity().getResources()
                .getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getRField6List() != null)
            retailerFlexBOS.addAll(mNewRetailerPresenter.getRField6List());

        ArrayAdapter<RetailerFlexBO> rField6Adapter = (ArrayAdapter<RetailerFlexBO>) getArrayAdapter(retailerFlexBOS);
        rField6Spinner.setAdapter(rField6Adapter);

        spinnerHashMap.put(NewRetailerConstant.RFIELD6, rField6Spinner);
    }


    @Override
    public void createRField7Spinner(boolean mandatory, int mNumber, String mName, String configCode) {
        MaterialSpinner rField7Spinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<RetailerFlexBO> retailerFlexBOS = new ArrayList<>();

        retailerFlexBOS.add(new RetailerFlexBO("0", getActivity().getResources()
                .getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getRField7List() != null)
            retailerFlexBOS.addAll(mNewRetailerPresenter.getRField7List());

        ArrayAdapter<RetailerFlexBO> rField7Adapter = (ArrayAdapter<RetailerFlexBO>) getArrayAdapter(retailerFlexBOS);
        rField7Spinner.setAdapter(rField7Adapter);

        spinnerHashMap.put(NewRetailerConstant.RFIELD7, rField7Spinner);
    }


    @Override
    public void createRField4Spinner(boolean mandatory, int mNumber, String mName, String configCode) {

        MaterialSpinner rField4Spinner = createNewMaterialSpinner(mandatory, mNumber, mName, configCode);

        ArrayList<RetailerFlexBO> retailerFlexBOS = new ArrayList<>();

        retailerFlexBOS.add(new RetailerFlexBO("0", getActivity().getResources()
                .getString(R.string.select_str) + " " + mName));
        if (mNewRetailerPresenter.getRField4List() != null)
            retailerFlexBOS.addAll(mNewRetailerPresenter.getRField4List());

        ArrayAdapter<RetailerFlexBO> rField4Adapter = (ArrayAdapter<RetailerFlexBO>) getArrayAdapter(retailerFlexBOS);
        rField4Spinner.setAdapter(rField4Adapter);

        spinnerHashMap.put(NewRetailerConstant.RFIELD4, rField4Spinner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void createDaysAndWeeks(boolean mandatory) {

        getDaysView(mandatory);
        getWeeksView(mandatory);
    }

    @Override
    public void createTinNum(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String configCode) {
        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, configCode, InputType.TYPE_CLASS_TEXT), commonsParams);
    }

    @Override
    public void createRField3(int menuNumber, String menuName, boolean mandatory,
                              boolean isUppercaseLetter, String configCode) {
        mRootLinearLayout.addView(createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, configCode, InputType.TYPE_CLASS_TEXT), commonsParams);
    }

    @Override
    public void createRFieldEditText(int mNumber, String mName, boolean mandatory,
                                     boolean isUppercaseLetter, String configCode) {
        mRootLinearLayout.addView(createCommonEditTextView(mNumber, mName, mandatory, isUppercaseLetter, configCode, InputType.TYPE_CLASS_TEXT), commonsParams);
    }

    @Override
    public void createPinCode(int mNumber, String mName,
                              boolean mandatory, boolean isUppercaseLetter, String configCode) {
        mRootLinearLayout.addView(createCommonEditTextView(mNumber, mName, mandatory, isUppercaseLetter, configCode, InputType.TYPE_CLASS_NUMBER), commonsParams);
    }

    @Override
    public void createGstNo(int mNumber, String mName, boolean mandatory,
                            boolean isUppercaseLetter, String configCode) {
        mRootLinearLayout.addView(createCommonEditTextView(mNumber, mName, mandatory, isUppercaseLetter, configCode, InputType.TYPE_CLASS_TEXT), commonsParams);
    }

    @Override
    public void createLatLongTextView(String mName, int mNumber) {

        LinearLayout secondlayout = new LinearLayout(getActivity());
        secondlayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView latLongTextHeader;
        latLongTextHeader = getDateTextLabel(mName);
        secondlayout.addView(latLongTextHeader, mandatoryTextViewParams);

        latLongTextView =  new TextView(new ContextThemeWrapper(getActivity(), R.style.locationButton), null, 0);
        latLongTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        latLongTextView.setTextColor(Color.BLACK);
        latLongTextView.setGravity(Gravity.CENTER);
        latLongTextView.setId(mNumber);
        latLongTextView.setText("0.0");
        latLongTextView.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        if (textViewHashMap == null)
            textViewHashMap = new SparseArray<>();
        textViewHashMap.put(mNumber, latLongTextView);

        secondlayout.addView(latLongTextView, weight2);
        mRootLinearLayout.addView(secondlayout, latlongTextViewWeight);

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            String latlng = mNewRetailerPresenter.getOutlet().getNewOutletlattitude()
                    + "," + mNewRetailerPresenter.getOutlet().getNewOutletLongitude();
            latLongTextView.setText(latlng);
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                latLongTextView.setEnabled(false);
        }

        latLongTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapViewClicked();
            }
        });
    }


    @Override
    public void createTinExpDataTextView(int mNumber, String mName, boolean mandatory) {

        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout secondlayout = new LinearLayout(getActivity());

        if (mandatory)
            setMandatoryView(firstlayout);

        TextView tinExpDataLabel = getDateTextLabel(mName);

        firstlayout.addView(tinExpDataLabel, mandatoryTextViewParams);

        TextView tinExpDateTextView = getDateTextView(mNumber);

        secondlayout.addView(tinExpDateTextView, latlongTextViewWeight);
        finallayout.addView(firstlayout, mandatoryTextViewParams);
        finallayout.addView(secondlayout, weight2);

        mRootLinearLayout.addView(finallayout, commonsParams);

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            String tindate = mNewRetailerPresenter.getOutlet().getTinExpDate();
            tinExpDateTextView.setText(tindate);
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                tinExpDateTextView.setEnabled(false);
        }

        tinExpDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateViewClicked((TextView) v, tinExpDateTextView);
            }
        });
    }

    private void onDateViewClicked(TextView v, TextView dateTextView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String date = dateTextView.getText().toString();
        if (!date.equalsIgnoreCase(getResources().getString(R.string.select_date))
                && date.contains("/") && date.split("/").length == 3) {
            year = Integer.valueOf(date.split("/")[0]);
            month = Integer.valueOf(date.split("/")[1]) - 1;
            day = Integer.valueOf(date.split("/")[2]);
        }
        DatePickerDialog tinDatePickerDialog = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = new GregorianCalendar(year1, month1, dayOfMonth);
            if (selectedDate.after(Calendar.getInstance())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                v.setText(sdf.format(selectedDate.getTime()));
                v.setError(null);
            } else {
                showMessage(getActivity().getString(R.string.select_future_date));
            }
        }, year, month, day);

        tinDatePickerDialog.show();
    }


    @Override
    public void createDrugLicenseExpDataTextView(int mNumber, String mName, boolean mandatory) {

        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout secondlayout = new LinearLayout(getActivity());

        if (mandatory)
            setMandatoryView(firstlayout);

        TextView drugLicenseLabel = getDateTextLabel(mName);

        firstlayout.addView(drugLicenseLabel, mandatoryTextViewParams);

        TextView dlExpDateTextView = getDateTextView(mNumber);

        secondlayout.addView(dlExpDateTextView, latlongTextViewWeight);

        finallayout.addView(firstlayout, mandatoryTextViewParams);
        finallayout.addView(secondlayout, weight2);

        mRootLinearLayout.addView(finallayout, commonsParams);

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            String dlExpDate = mNewRetailerPresenter.getOutlet().getDlExpDate();
            dlExpDateTextView.setText(dlExpDate);
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                dlExpDateTextView.setEnabled(false);
        }

        dlExpDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateViewClicked((TextView) v, dlExpDateTextView);

            }
        });
    }


    @Override
    public void createFoodLicenceExpDataTextView(int mNumber, String mName, boolean mandatory) {

        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout secondlayout = new LinearLayout(getActivity());

        if (mandatory)
            setMandatoryView(firstlayout);

        TextView foodLicenceLabel = getDateTextLabel(mName);

        firstlayout.addView(foodLicenceLabel, mandatoryTextViewParams);

        TextView flExpDateTextView = getDateTextView(mNumber);


        secondlayout.addView(flExpDateTextView, latlongTextViewWeight);
        finallayout.addView(firstlayout, mandatoryTextViewParams);
        finallayout.addView(secondlayout, weight2);

        mRootLinearLayout.addView(finallayout, commonsParams);

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            String tindate = mNewRetailerPresenter.getOutlet().getFlExpDate();
            flExpDateTextView.setText(tindate);
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                flExpDateTextView.setEnabled(false);
        }

        flExpDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateViewClicked((TextView) v, flExpDateTextView);


            }
        });
    }

    private TextView getDateTextLabel(String mName) {
        TextView foodLicenceLabel = new MandatoryTextView(getActivity());
        foodLicenceLabel.setText(mName);
        foodLicenceLabel.setTextColor(Color.BLACK);
        foodLicenceLabel.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        return foodLicenceLabel;
    }

    private TextView getDateTextView(int mNumber) {
        TextView dateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        dateTextView.setTextColor(Color.BLACK);
        dateTextView.setGravity(Gravity.CENTER);
        dateTextView.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        dateTextView.setId(mNumber);
        dateTextView.setText(getResources().getString(R.string.select_date));
        dateTextView.setFocusableInTouchMode(true);
        dateTextView.setFocusable(true);

        if (textViewHashMap == null)
            textViewHashMap = new SparseArray<>();
        textViewHashMap.put(mNumber, dateTextView);

        return dateTextView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void createNearByRetailerView(String MName, boolean mandatory) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory)
            setMandatoryView(layout);

        nearbyAutoCompleteTextView = new AppCompatAutoCompleteTextView(getActivity());
        mEditTextInputLayout = new TextInputLayout(getActivity());
        nearbyAutoCompleteTextView.setMovementMethod(new ScrollingMovementMethod());
        nearbyAutoCompleteTextView.setSingleLine(true);
        nearbyAutoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.font_small));
        nearbyAutoCompleteTextView.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        nearbyAutoCompleteTextView.setHint(MName);
        nearbyAutoCompleteTextView.setKeyListener(null);

        nearbyAutoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!nearbyAutoCompleteTextView.isEnabled())
                    return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < NewRetailerConstant.MAX_CLICK_DURATION) {

                            if (!getDistributorSpinnerSelectedItem().getDId().equals("0"))
                                mNewRetailerPresenter.getLinkRetailerList(SDUtil.convertToInt(getDistributorSpinnerSelectedItem().getDId()));
                            else
                                showMessage(getResources().getString(R.string.please_select_a_Distributor_to_download));

                        }
                    }
                }
                return false;
            }
        });

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {

            StringBuilder stringBuilder = new StringBuilder();

            if (mNewRetailerPresenter.getDownloadNearByRetailers() != null) {

                for (RetailerMasterBO bo : mNewRetailerPresenter.getNearbyRetailerList()) {
                    if (mNewRetailerPresenter.getDownloadNearByRetailers().contains(bo.getRetailerID())) {
                        mNewRetailerPresenter.getSelectedRetailers().add(bo.getRetailerID());
                        if (stringBuilder.length() > 0)
                            stringBuilder.append(", ");

                        stringBuilder.append(bo.getRetailerName());
                    }
                }

                nearbyAutoCompleteTextView.setText(stringBuilder.toString());
            }

            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                nearbyAutoCompleteTextView.setEnabled(false);
        }

        if (!mandatory)
            mEditTextInputLayout.addView(nearbyAutoCompleteTextView, editWeightMargin);
        else
            mEditTextInputLayout.addView(nearbyAutoCompleteTextView, weight1);

        layout.addView(mEditTextInputLayout, commonsparams3);

        mRootLinearLayout.addView(layout, commonsParams);

    }


    @Override
    public void showNearByRetailersDialog(ArrayList<RetailerMasterBO> mNearbyRetailerList, int valueNearbyRetailerMax) {
        if (mNearbyRetailerList != null && !mNearbyRetailerList.isEmpty()) {
            for (RetailerMasterBO bo : mNearbyRetailerList) {
                bo.setIsNearBy(false);
            }
            NearByRetailerDialog dialog = new NearByRetailerDialog(getActivity(), valueNearbyRetailerMax, new Vector<>(mNearbyRetailerList), mNewRetailerPresenter.getSelectedRetailers());
            dialog.show();
            dialog.setCancelable(false);
        } else
            showMessage("Nearby Retailer's Not Available");

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void createPriorityProductView(String mName, boolean mandatory, int hasLink) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory)
            setMandatoryView(layout);

        mEditTextInputLayout = new TextInputLayout(getActivity());
        priorityProductAutoCompleteTextView = new AppCompatAutoCompleteTextView(getActivity());
        priorityProductAutoCompleteTextView.setSingleLine(true);
        priorityProductAutoCompleteTextView.setMovementMethod(new ScrollingMovementMethod());
        priorityProductAutoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        priorityProductAutoCompleteTextView.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        priorityProductAutoCompleteTextView.setHint(mName);
        priorityProductAutoCompleteTextView.setKeyListener(null);

        mNewRetailerPresenter.downloadPriorityProducts();

        priorityProductAutoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!priorityProductAutoCompleteTextView.isEnabled())
                    return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < NewRetailerConstant.MAX_CLICK_DURATION) {
                            priorityProductAutoCompleteTextView.requestFocus();
                            if (mNewRetailerPresenter.getPriorityProductMasterList() != null)
                                showPriorityDialog(mName, hasLink, selectedPosition, mNewRetailerPresenter.getPriorityProductMasterList());
                            else
                                showMessage("Priority Products Not Available");
                        }
                    }
                }
                return false;
            }
        });

        if (!mandatory) {
            mEditTextInputLayout.addView(priorityProductAutoCompleteTextView, editWeightMargin);
        } else {
            mEditTextInputLayout.addView(priorityProductAutoCompleteTextView, weight1);
        }

        layout.addView(mEditTextInputLayout, commonsparams3);

        mRootLinearLayout.addView(layout, commonsParams);

    }


    @Override
    public void updatePriorityProductAutoCompleteTextView(String values, boolean isEnabled) {
        priorityProductAutoCompleteTextView.setText(values);
        priorityProductAutoCompleteTextView.setEnabled(isEnabled);
    }

    @Override
    public void createSezCheckBox(String menuName, boolean mandatory) {
        LinearLayout baselayout = new LinearLayout(getActivity());
        baselayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.VERTICAL);

        isSEZCheckBox = new AppCompatCheckBox(getActivity());
        if (screenWidth > 520) {
            isSEZCheckBox.setScaleX((float) 1.1);
            isSEZCheckBox.setScaleY((float) 1.1);
            isSEZCheckBox.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        linearlayout.addView(isSEZCheckBox, weight2);

        LinearLayout textLayout = new LinearLayout(getActivity());
        textLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory)
            setMandatoryView(textLayout);

        TextView days = new TextView(getActivity());
        days.setText(menuName);
        days.setTextColor(Color.BLACK);
        days.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        textLayout.addView(days, editWeightMargin);

        baselayout.addView(textLayout);
        baselayout.addView(linearlayout);
        mRootLinearLayout.addView(baselayout, commonsParams);
    }

    @Override
    public String getDynamicEditTextValues(int mNumber) {
        AppCompatEditText value = editTextHashMap.get(mNumber);
        if (value != null) {
            return value.getText().toString().trim();
        } else {
            return "";
        }
    }

    @Override
    public String getDynamicTextViewValues(int mNumber) {
        TextView value = textViewHashMap.get(mNumber);
        if (value != null) {
            return value.getText().toString().trim();
        } else {
            return "";
        }
    }

    @Override
    public String getSelectedPriorityProducts() {
        return priorityProductAutoCompleteTextView.getText().toString();
    }

    @Override
    public String getSelectedNearByRetailers() {
        return nearbyAutoCompleteTextView.getText().toString();
    }


    @Override
    public void setDynamicEditTextFocus(int mNumber) {
        AppCompatEditText value = editTextHashMap.get(mNumber);
        if (value != null) {
            value.requestFocus();
        }
    }

    @Override
    public void showMandatoryErrorMessage(int position, String menu) {
        editTextHashMap.get(position).setError(menu + " " + getString(R.string.menu_mandatory_message));
    }

    @Override
    public void showPriorityProductsMandatoryMessage(String menuName) {
        priorityProductAutoCompleteTextView.setError(menuName + " " + getString(R.string.menu_mandatory_message));
    }

    @Override
    public void showNearByRetailersMandatory(String menuName) {
        nearbyAutoCompleteTextView.setError(menuName + " " + getString(R.string.menu_mandatory_message));
    }


    @Override
    public void showLengthMisMatchError(int position, String menuName, int minLength) {
        editTextHashMap.get(position).setError(menuName + " " + getString(R.string.menu_length_must_be) + " " + minLength);
    }

    @Override
    public void showInvalidError(int position, String menuName) {
        editTextHashMap.get(position).setError(getString(R.string.enter_valid) + " " + menuName);
    }

    @Override
    public void showInvalidDateError(int position, String menuName) {
        textViewHashMap.get(position).setError(getString(R.string.enter_valid) + " " + menuName);
    }

    @Override
    public void showSelectPlanError(int position) {
        textViewHashMap.get(position).setError(getString(R.string.select_str) + " " + textViewHashMap.get(position).getText());
    }

    @Override
    public String getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption contactTitleOption) {

        switch (contactTitleOption) {
            case FIRSTNAME:
                return getResources().getString(R.string.contact_person_first_name);
            case LASTNAME:
                return getResources().getString(R.string.contact_person_last_name);
            case TITLE:
                return getResources().getString(R.string.contact_title);
        }
        return "";
    }


    @Override
    public SpinnerBO getSubChannelSpinnerSelectedItem() {
        return (SpinnerBO) spinnerHashMap.get(NewRetailerConstant.SUBCHANNEL).getSelectedItem();
    }

    @Override
    public DistributorMasterBO getDistributorSpinnerSelectedItem() {
        return (DistributorMasterBO) spinnerHashMap.get(NewRetailerConstant.DISTRIBUTOR).getSelectedItem();
    }

    @Override
    public String getSelectedLatLong() {
        return latLongTextView.getText().toString();
    }

    @Override
    public LocationBO getSelectedLocation(String locationConfigName) {
        if (spinnerHashMap.containsKey(locationConfigName))
            return (LocationBO) spinnerHashMap.get(locationConfigName).getSelectedItem();
        else return null;
    }

    @Override
    public ContractStatus getContractSpinnerSelectedItem() {
        return (ContractStatus) spinnerHashMap.get(NewRetailerConstant.CONTRACT).getSelectedItem();
    }

    @Override
    public PaymentType getPaymentType() {
        return (PaymentType) spinnerHashMap.get(NewRetailerConstant.PAYMENTTYPE).getSelectedItem();
    }

    @Override
    public String getSelectedDays() {
        StringBuilder checkedDaysAll = new StringBuilder();
        String[] daysForUpload = getResources().getStringArray(R.array.days_in_week);
        for (int k = 0; k < NewRetailerConstant.MAX_NO_OF_DAYS; k++) {
            if (daysCheckBoxHashMap.get(k).isChecked()) {
                String tempStr = daysForUpload[k];
                if (checkedDaysAll.length() < 1)
                    checkedDaysAll = new StringBuilder(tempStr + ":" + weekNoStr + ";");
                else {
                    checkedDaysAll.append(tempStr).append(":").append(weekNoStr).append(";");
                }

            }
        }

        return checkedDaysAll.toString();
    }

    @Override
    public String getSelectedWeeks() {
        StringBuilder weekNoStr = new StringBuilder();
        for (int k = 0; k < NewRetailerConstant.NUMBER_OF_WEEKS; k++) {
            if (weekNoCheckBox.get(k).isChecked()) {
                String tempStr = (String) weekNoCheckBox.get(k).getText();
                if (weekNoStr.length() < 1)
                    weekNoStr = new StringBuilder(tempStr);
                else {
                    weekNoStr.append(",").append(tempStr);
                }
            }
        }

        return weekNoStr.toString();
    }

    @Override
    public String getBeatName() {
        return beatName;
    }

    @Override
    public boolean isSEZCheckBoxChecked() {
        return isSEZCheckBox.isChecked();
    }

    @Override
    public RetailerFlexBO getRField5Spinner() {
        return (RetailerFlexBO) spinnerHashMap.get(NewRetailerConstant.RFIELD5).getSelectedItem();
    }

    @Override
    public RetailerFlexBO getRField6Spinner() {
        return (RetailerFlexBO) spinnerHashMap.get(NewRetailerConstant.RFIELD6).getSelectedItem();
    }

    @Override
    public RetailerFlexBO getRField7Spinner() {
        return (RetailerFlexBO) spinnerHashMap.get(NewRetailerConstant.RFIELD7).getSelectedItem();
    }

    @Override
    public RetailerFlexBO getRField4Spinner() {
        return (RetailerFlexBO) spinnerHashMap.get(NewRetailerConstant.RFIELD7).getSelectedItem();
    }

    @Override
    public StandardListBO getTaxTypeSpinner() {
        return (StandardListBO) spinnerHashMap.get(NewRetailerConstant.TAXTYPE).getSelectedItem();
    }

    @Override
    public StandardListBO getClassTypeSpinner() {
        return (StandardListBO) spinnerHashMap.get(NewRetailerConstant.CLASS).getSelectedItem();
    }

    @Override
    public BeatMasterBO getRouteSpinner() {
        return (BeatMasterBO) spinnerHashMap.get(NewRetailerConstant.ROUTE).getSelectedItem();
    }

    @Override
    public UserMasterBO getUserSpinner() {
        return (UserMasterBO) spinnerHashMap.get(NewRetailerConstant.USER).getSelectedItem();
    }

    @Override
    public void showAlertMessage() {
        showMessage(getResources().getString(R.string.error) + " :"
                + getResources().getString(R.string.new_store_infn_not_saved));
    }


    @Override
    public void showSuccessMessage() {
        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType()) {
            showMessage(R.string.saved_successfully);
            doFinishActivity();
        } else
            showAlert("", getString(R.string.saved_successfully), () -> detachFragment());
    }


    @Override
    public int getSpinnerSelectedItemPosition(String key) {
        return spinnerHashMap.get(key).getSelectedItemPosition();
    }

    private AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            TextView errorText = (TextView) view;
            if (errorText.getCurrentTextColor() == Color.RED)
                errorText.setTextColor(Color.BLACK);


        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void setRequestFocusWithErrorMessage(String key, String errorMessage) {
        spinnerHashMap.get(key).requestFocus();
        TextView errorText = (TextView) spinnerHashMap.get(key).getSelectedView();
        errorText.setTextColor(Color.RED);
    }

    @Override
    public void setSpinnerPosition(String configName, int position) {
        if (spinnerHashMap.containsKey(configName))
            spinnerHashMap.get(configName).setSelection(position);
    }

    @Override
    public void showNoChannelsError() {
        showMessage(R.string.no_channels_download);
    }

    @Override
    public void showNoSubChannelsError() {
        showMessage(R.string.no_sub_channels_download);
    }

    @Override
    public void showNoLocationsError() {
        showMessage(R.string.no_location_download);
    }


    @Override
    public void showNoBeatsError() {
        showMessage(R.string.no_beats_download);
    }

    @Override
    public ArrayList<DistributorMasterBO> getDistributorTypeMasterList() {
        return mDistributorTypeMasterList;
    }

    @Override
    public ChannelBO getChannelSpinnerSelectedItem() {
        return (ChannelBO) spinnerHashMap.get(NewRetailerConstant.CHANNEL).getSelectedItem();
    }

    @Override
    public boolean isWeekChecked() {
        for (int k = 0; k < NewRetailerConstant.NUMBER_OF_WEEKS; k++) {
            if (weekNoCheckBox.get(k).isChecked()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDayChecked() {
        for (int k = 0; k < NewRetailerConstant.MAX_NO_OF_DAYS; k++) {
            if (daysCheckBoxHashMap.get(k).isChecked()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == LOCATION_REQUEST_CODE) {
            if (data.hasExtra("lat") && data.hasExtra("isChanged")) {
                mNewRetailerPresenter.updateLatitude(data.getExtras().getDouble("lat"));
                mNewRetailerPresenter.updateLongitude(data.getExtras().getDouble("lon"));

                String latlng = mNewRetailerPresenter.getLatitude()
                        + "," + mNewRetailerPresenter.getLongitude();
                latLongTextView.setText(latlng);
                latLongTextView.setError(null);
            }
        } else if (requestCode == NewRetailerConstant.CAMERA_REQUEST_CODE && resultCode == 1) {
            if (imageIdList == null)
                imageIdList = new ArrayList<>();
            if (imageNameList == null)
                imageNameList = new ArrayList<>();

            if (imageName != null)
                imageNameList.add(imageName);
            if (imageId != 0)
                imageIdList.add(imageId);
        } else if (resultCode == RESULT_OK && requestCode == NEW_RETAILER_ORDER_CREATION) {
            if (data.hasExtra("ordered_products")) {
                mNewRetailerPresenter.setOrderedProductList((ArrayList<ProductMasterBO>) data.getSerializableExtra("ordered_products"));
                mNewRetailerPresenter.setOrderHeader((OrderHeader) data.getSerializableExtra("order_header"));
            }
        } else if (resultCode == RESULT_OK && requestCode == NEW_RETAILER_OPPORTUNITY_PRODUCTS) {
            if (data.hasExtra("opportunity_products")) {
                mNewRetailerPresenter.setOpportunityProductList((ArrayList<ProductMasterBO>) data.getSerializableExtra("opportunity_products"));
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private void getWeeksView(boolean mandatory) {

        TableLayout weekTableLayout = new TableLayout(getActivity());

        TableRow weekTableRow1 = new TableRow(getActivity());
        weekTableRow1.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        weekTableRow1.setWeightSum(4);

        TableRow weekTableRow2 = new TableRow(getActivity());
        weekTableRow2.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        weekTableRow2.setWeightSum(4);

        for (int j = 0; j < NewRetailerConstant.NUMBER_OF_WEEKS; j++) {

            if (j >= 2) {
                setWeekAttributeSet(j, weekTableRow2);
            } else {
                setWeekAttributeSet(j, weekTableRow1);
            }
        }
        weekTableLayout.addView(weekTableRow1);
        weekTableLayout.addView(weekTableRow2);

        LinearLayout weekLayout = new LinearLayout(getActivity());
        weekLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout weekText = new LinearLayout(getActivity());
        weekText.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory)
            setMandatoryView(weekText);

        TextView week = new MandatoryTextView(getActivity());
        week.setTextColor(Color.BLACK);
        week.setText(getResources().getString(R.string.week) + ":");
        weekText.addView(week, mandatoryTextViewParams);
        if (textViewHashMap == null)
            textViewHashMap = new SparseArray<>();
        textViewHashMap.put(WEEK_TEXT_LABEL, week);

        weekLayout.addView(weekText);
        weekLayout.addView(weekTableLayout, params);
        mRootLinearLayout.addView(weekLayout, commonsParams);


    }

    private void setWeekAttributeSet(int j, TableRow weekTableRow) {

        AppCompatCheckBox weekCheckBox = new AppCompatCheckBox(getActivity());
        weekCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textViewHashMap.get(WEEK_TEXT_LABEL).setError(null);
            }
        });
        if (weekNoCheckBox == null)
            weekNoCheckBox = new SparseArray<>();
        weekNoCheckBox.put(j, weekCheckBox);

        if (screenWidth > 520) {
            weekNoCheckBox.get(j).setScaleX((float) 1.1);
            weekNoCheckBox.get(j).setScaleY((float) 1.1);
            weekNoCheckBox.get(j).setLayoutParams(new TableRow.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
        }
        if (j == 0) {
            weekNoCheckBox.get(j).setText(getResources().getString(R.string.wk1));
            weekNoCheckBox.get(j).setTextColor(Color.BLACK);
            weekTableRow.addView(weekNoCheckBox.get(j));
        } else if (j == 1) {
            weekNoCheckBox.get(j).setText(getResources().getString(R.string.wk2));
            weekNoCheckBox.get(j).setTextColor(Color.BLACK);
            weekTableRow.addView(weekNoCheckBox.get(j));
        } else if (j == 2) {
            weekNoCheckBox.get(j).setText(getResources().getString(R.string.wk3));
            weekTableRow.addView(weekNoCheckBox.get(j));
        } else if (j == 3) {
            weekNoCheckBox.get(j).setText(getResources().getString(R.string.wk4));
            weekTableRow.addView(weekNoCheckBox.get(j));
        } else {
            weekNoCheckBox.get(j).setText(getResources().getString(R.string.wk5));
            weekTableRow.addView(weekNoCheckBox.get(j));
        }

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            if (mNewRetailerPresenter.getOutlet().getVisitDays() != null) {
                if (mNewRetailerPresenter.getOutlet().getVisitDays().contains(weekNoCheckBox.get(j).getText()))
                    weekNoCheckBox.get(j).setChecked(true);
            }
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                weekNoCheckBox.get(j).setEnabled(false);
        }

    }

    @SuppressLint("SetTextI18n")
    private void getDaysView(boolean mandatory) {
        TableLayout daysTableLayout = new TableLayout(getActivity());
        daysTableLayout.setStretchAllColumns(false);

        TableRow daysableRow1 = new TableRow(getActivity());
        daysableRow1.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        daysableRow1.setWeightSum(8);

        TableRow daysableRow2 = new TableRow(getActivity());
        daysableRow2.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        daysableRow2.setWeightSum(6);

        String[] days = getResources().getStringArray(R.array.days_in_week);
        for (int i = 0; i < days.length; i++) {
            if (i >= 4) {
                setDaysAttributeSet(i, days[i], daysableRow2);
            } else {
                setDaysAttributeSet(i, days[i], daysableRow1);
            }
        }
        daysTableLayout.addView(daysableRow1);
        daysTableLayout.addView(daysableRow2);

        /*set Mandatory view*/
        LinearLayout dayLayout = new LinearLayout(getActivity());
        dayLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout daystext = new LinearLayout(getActivity());
        daystext.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory)
            setMandatoryView(daystext);

        TextView day = new MandatoryTextView(getActivity());
        day.setTextColor(Color.BLACK);
        day.setText(getResources().getString(R.string.day) + ":");
        daystext.addView(day, mandatoryTextViewParams);
        if (textViewHashMap == null)
            textViewHashMap = new SparseArray<>();
        textViewHashMap.put(DAY_TEXT_LABEL, day);

        dayLayout.addView(daystext);
        dayLayout.addView(daysTableLayout, params);
        mRootLinearLayout.addView(dayLayout, commonsParams);
    }

    private void setDaysAttributeSet(int position, String checkBoxNameValue, TableRow daysRow) {

        AppCompatCheckBox daysCheckbox = new AppCompatCheckBox(getActivity());
        daysCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (textViewHashMap.get(DAY_TEXT_LABEL).getError() != null)
                    textViewHashMap.get(DAY_TEXT_LABEL).setError(null);
            }
        });
        if (daysCheckBoxHashMap == null)
            daysCheckBoxHashMap = new SparseArray<>();
        daysCheckBoxHashMap.put(position, daysCheckbox);

        if (position >= 4) {
            if (screenWidth > 520) {
                daysCheckBoxHashMap.get(position).setScaleX((float) 1.1);
                daysCheckBoxHashMap.get(position).setScaleY((float) 1.1);
                daysCheckBoxHashMap.get(position).setLayoutParams(new TableRow.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));
            }
            daysRow.addView(daysCheckBoxHashMap.get(position));

        } else {
            if (screenWidth > 520) {
                daysCheckBoxHashMap.get(position).setScaleX((float) 1.1);
                daysCheckBoxHashMap.get(position).setScaleY((float) 1.1);
                daysCheckBoxHashMap.get(position).setLayoutParams(new TableRow.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1.6f));
            }
            daysRow.addView(daysCheckBoxHashMap.get(position));
        }

        daysCheckBoxHashMap.get(position).setChecked(false);
        daysCheckBoxHashMap.get(position).setText(checkBoxNameValue);
        daysCheckBoxHashMap.get(position).setVisibility(CheckBox.VISIBLE);

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            if (mNewRetailerPresenter.getOutlet().getVisitDays() != null) {
                if (mNewRetailerPresenter.getOutlet().getVisitDays().contains(checkBoxNameValue))
                    daysCheckBoxHashMap.get(position).setChecked(true);
            }
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                daysCheckBoxHashMap.get(position).setEnabled(false);
        }
    }


    @Override
    public void updateChannelSelectedItem(ArrayList<SubchannelBO> subChannelMaster, NewOutletBO outlet, int menuNumber) {
        for (int i = 0; i < subChannelMaster.size(); i++) {
            if (subChannelMaster.get(i).getSubchannelid() == outlet.getSubChannel()) {
                for (int j = 0; j < channelAdapter.getCount(); j++) {
                    if (channelAdapter.getItem(j).getChannelId() == subChannelMaster.get(i).getChannelid()) {
                        spinnerHashMap.get(menuNumber).setSelection(j);
                        break;
                    }
                }
                break;
            }
        }

    }

    @Override
    public void updateSelectedItems(int position, StandardListBO standardListBO) {
        if (priorityProductIDList == null)
            priorityProductIDList = new ArrayList<>();
        else
            priorityProductIDList.clear();

        priorityProductIDList.add(standardListBO);
        priorityProductAutoCompleteTextView.postDelayed(new Runnable() {
            @Override
            public void run() {

                priorityProductAutoCompleteTextView.showDropDown();

            }
        }, 500);
        priorityProductAutoCompleteTextView.setText(standardListBO.getListName());
        priorityProductAutoCompleteTextView.setSelection(priorityProductAutoCompleteTextView.getText().length());
        selectedPosition = position;
        prioritySelectionDialog.dismiss();

    }


    @Override
    public void updatePriorityProducts(ArrayList<StandardListBO> mPriorityProductList) {
        if (priorityProductIDList == null)
            priorityProductIDList = new ArrayList<>();
        else
            priorityProductIDList.clear();
        StringBuffer sb = new StringBuffer();
        for (StandardListBO standardListBO : mPriorityProductList) {
            if (standardListBO.isChecked()) {
                priorityProductIDList.add(standardListBO);
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(standardListBO.getListName());
            }
            if (priorityProductIDList.size() > 0) {
                priorityProductAutoCompleteTextView.setText(sb.toString());
            } else {
                priorityProductAutoCompleteTextView.setText("");
            }
        }
        prioritySelectionDialog.dismiss();

    }


    @Override
    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        mNewRetailerPresenter.getSelectedRetailers().clear();
        StringBuilder sb = new StringBuilder();
        for (RetailerMasterBO bo : list) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(bo.getRetailerName());
            mNewRetailerPresenter.getSelectedRetailers().add(bo.getRetailerID());
        }
        nearbyAutoCompleteTextView.setText(sb.toString());

    }


    @OnClick(R.id.new_outlet_save)
    public void onSaveClick() {
        if (mNewRetailerPresenter.isValidRetailer())
            mNewRetailerPresenter.saveNewRetailer();
    }

    @Override
    public void showDistributorChangedDialog() {

        showAlert("", getString(R.string.new_outlet_order), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                mNewRetailerPresenter.clearOrdersAndSaveOutlet();
            }
        });
    }

    @Override
    public void doFinishActivity() {
        getActivity().finish();
    }

    @Override
    public void showAlertDialog(int title) {
        String titleText;
        if (title == DataMembers.NOTIFY_URL_NOT_CONFIGURED)
            titleText = "Error: " + getResources().getString(R.string.new_store_infn_not_saved);
        else if (title == DataMembers.NOTIFY_UPLOAD_ERROR)
            titleText = getResources().getString(R.string.url_not_mapped);
        else
            titleText = getResources().getString(R.string.are_you_sure_to_close_without_savingthe_data);

        showAlert(getString(R.string.error), titleText, () -> mNewRetailerPresenter.deleteNewRetailerSurvey());

    }

    @Override
    public void onSurveyDeleteSuccess() {
        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType()) {
            doFinishActivity();
        } else
            detachFragment();
    }

    @Override
    public void showServerErrorMessage(String errorCode) {
        showMessage(AppUtils.getErrorMessageByErrorCode(getActivity(), errorCode));
        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType()) {
            doFinishActivity();
        } else
            detachFragment();
    }

    @Override
    public void showSessionExpiredMessage() {
        showMessage(R.string.sessionout_loginagain);
    }

    @Override
    public void showRetailerDownloadFailedMessage() {
        showMessage(R.string.data_not_downloaded);
    }

    @Override
    public void showNoNetworkMessage() {
        showMessage(R.string.please_connect_to_internet);
    }

    @Override
    public void navigateToOpportunityProductsScreen() {
        Intent intent = new Intent(getActivity(), OpportunityNewOutlet.class);
        intent.putExtra("OrderFlag", "Nothing");
        intent.putExtra("ScreenCode", ConfigurationMasterHelper.MENU_ORDER);
        startActivityForResult(intent, NEW_RETAILER_OPPORTUNITY_PRODUCTS);
        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void navigateToNewOutletOrderScreen() {
        Intent intent = new Intent(getActivity(), OrderNewOutlet.class);
        intent.putExtra("OrderFlag", "Nothing");
        intent.putExtra("ScreenCode", ConfigurationMasterHelper.MENU_ORDER);
        startActivityForResult(intent, NEW_RETAILER_ORDER_CREATION);
        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void callSurveyActivity() {
        Intent intent = new Intent(getActivity(), SurveyActivityNew.class);
        intent.putExtra("menucode", NewRetailerConstant.MENU_NEW_RETAILER);
        intent.putExtra("screenMode", screenMode);

        ((BusinessModel) getActivity().getApplication()).mSelectedActivityName = "Survey";

        if (getScreenMode() == NewRetailerConstant.MenuType.EDIT.getMenuType() || getScreenMode() == NewRetailerConstant.MenuType.VIEW.getMenuType())
            intent.putExtra("editRetailerId", mNewRetailerPresenter.getCurrentRetailerId());
        startActivity(intent);
    }

    @Override
    public void MenuCaptureAlert() {


        if (FileUtils.isExternalStorageAvailable(5)) {

            ArrayAdapter<String> mImageTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
            for (NewOutletBO temp : mNewRetailerPresenter.getImageTypeList()) {
                mImageTypeAdapter.add(temp.getListName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(null)
                    .setSingleChoiceItems(mImageTypeAdapter, 0,
                            (dialog, item) -> {
                                PHOTO_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                        + "/" + DataMembers.photoFolderName + "/";

                                Commons.print("Photo path :" + PHOTO_PATH);

                                imageId = mNewRetailerPresenter.getImageTypeList().get(item).getListId();

                                imageName = NewRetailerConstant.moduleName + mNewRetailerPresenter.getUid() + "_" + imageId + "_img.jpg";
                                String fnameStarts = "";
                                if (screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType() && imageNameList != null) {
                                    for (String img : imageNameList) {
                                        if ((img).contains(imageId + "")) {
                                            fnameStarts = img;
                                            break;
                                        }
                                    }
                                } else {
                                    fnameStarts = NewRetailerConstant.moduleName + mNewRetailerPresenter.getUid() + "_" + imageId;
                                }

                                boolean fileExists = FileUtils.checkForNFilesInFolder(PHOTO_PATH, 1, fnameStarts);

                                if (fileExists) {
                                    showFileDeleteAlert(fnameStarts);
                                } else {
                                    navigateToCameraActivity();
                                    dialog.dismiss();
                                    return;
                                }

                                dialog.dismiss();
                            });
            applyAlertDialogTheme(getActivity(), builder);
        } else {
            showMessage(R.string.external_storage_not_available);
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()
                    || screenMode == NewRetailerConstant.MenuType.CREATE_FRM_EDT_SCREEN.getMenuType()) {
                doFinishActivity();
            } else {
                detachFragment();
            }
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; getActivity() adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_new_retailer, menu);
        if (!mNewRetailerPresenter.getConfigurationMasterHelper().IS_NEWOUTLET_IMAGETYPE) {
            menu.findItem(R.id.menu_capture).setVisible(false);
        }
        if (!mNewRetailerPresenter.getConfigurationMasterHelper().downloadFloatingSurveyConfig(NewRetailerConstant.MENU_NEW_RETAILER))
            menu.findItem(R.id.menu_survey).setVisible(false);

        menu.findItem(R.id.menu_oppr).setVisible(mNewRetailerPresenter.getConfigurationMasterHelper().SHOW_NEW_OUTLET_OPPR);
        menu.findItem(R.id.menu_order).setVisible(mNewRetailerPresenter.getConfigurationMasterHelper().SHOW_NEW_OUTLET_ORDER);

        if (mNewRetailerPresenter.getConfigurationMasterHelper().IS_NEWOUTLET_IMAGETYPE
                && screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType())
            menu.findItem(R.id.menu_capture).setVisible(true);
        else if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()) {
            menu.findItem(R.id.menu_capture).setVisible(false);
            menu.findItem(R.id.menu_oppr).setVisible(false);
            menu.findItem(R.id.menu_order).setVisible(false);
        }

        menu.findItem(R.id.menu_sort).setVisible(false);
        menu.findItem(R.id.menu_add).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            mNewRetailerPresenter.getHomeButtonClick();
            return true;
        }

        if (i == R.id.menu_survey) {
            mNewRetailerPresenter.onSurveyMenuClick();
            return true;

        } else if (i == R.id.menu_capture) {// AlertDialog.Builder builder;
            //Dont allow if Fun57 is enabled and mandatory,
            //Generally check for location and show toast if no location found.
            mNewRetailerPresenter.getMenuCaptureOptionClick();
            return true;
        } else if (i == R.id.menu_order) {

            mNewRetailerPresenter.onOrderMenuClick();

            return true;

        } else if (i == R.id.menu_oppr) {
            mNewRetailerPresenter.onOpportunityProductsMenuClicked();
            return true;
        }
        return false;
    }

    public void showFileDeleteAlert(final String imageNameStarts) {

        new CommonDialog(getActivity(), "", getResources().getString(R.string.word_already) + 1
                + getResources().getString(R.string.word_photocaptured_delete_retake),
                getString(R.string.yes),
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        FileUtils.deleteFiles(PHOTO_PATH, imageNameStarts);
                        if (imageNameList != null)
                            imageNameList.remove(imageNameStarts);
                        if (imageIdList != null)
                            imageIdList.remove(Integer.valueOf(imageId));
                        navigateToCameraActivity();
                    }
                }, getString(R.string.no),
                new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }, false).show();


    }

    private void navigateToCameraActivity() {
        Intent intent = new Intent(getActivity(),
                CameraActivity.class);
        intent.putExtra("quality", 40);
        String _path = PHOTO_PATH + "/" + imageName;
        intent.putExtra("path", _path);
        startActivityForResult(intent, NewRetailerConstant.CAMERA_REQUEST_CODE);
    }


    private void showPriorityDialog(String mName, int hasLink, int mSelectedpostion,
                                    ArrayList<StandardListBO> mPriorityProductList) {
        prioritySelectionDialog = new PrioritySelectionDialog(getActivity(), mName,
                hasLink, mSelectedpostion, mPriorityProductList);
        prioritySelectionDialog.setPrioritySelectionListener(this);
        prioritySelectionDialog.show();
    }

    public void onMapViewClicked() {

        Intent in;
        if (mNewRetailerPresenter.isBaiduMap())
            in = new Intent(getActivity(), BaiduMapDialogue.class);
        else
            in = new Intent(getActivity(), MapDialogue.class);

        in.putExtra("lat", mNewRetailerPresenter.getLatitude());
        in.putExtra("lon", mNewRetailerPresenter.getLongitude());
        startActivityForResult(in, LOCATION_REQUEST_CODE);
    }


    private void updateBeat(int userid) {
        ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();
        beatMasterBOS.add(new BeatMasterBO(0, getActivity().getResources()
                .getString(R.string.select_str) + " " + routeMenuName, 0));

        if (mNewRetailerPresenter.getBeatMaster() != null)
            for (BeatMasterBO temp : mNewRetailerPresenter.getBeatMaster()) {
                if (userid == temp.getUserId()) {
                    String routeCaps = temp.getBeatDescription();
                    temp.setBeatDescription(routeCaps);
                    beatMasterBOS.add(temp);
                }

            }
        routeAdapter = (ArrayAdapter<BeatMasterBO>) getArrayAdapter(beatMasterBOS);

        spinnerHashMap.get(NewRetailerConstant.ROUTE).setAdapter(routeAdapter);
    }


    private void updateLocationAdapter2(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();

        locationList.add(new LocationBO(0, getString(R.string.select_str)));

        for (LocationBO locationBO : mNewRetailerPresenter.getLocation1List()) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }

        locationAdapter1 = (ArrayAdapter<LocationBO>) getArrayAdapter(locationList);

        spinnerHashMap.get(NewRetailerConstant.LOCATION1).setAdapter(locationAdapter1);


        defaultLocationSpinner(NewRetailerConstant.LOCATION1);

    }

    private void defaultLocationSpinner(String configName) {
        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            switch (configName) {
                case LOCATION: {
                    mNewRetailerPresenter.getSelectedLocationPosition();
                }
            }

            spinnerHashMap.get(configName).setSelection(mNewRetailerPresenter.getSpinnerSelectedItem(configName));
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                spinnerHashMap.get(configName).setEnabled(false);
        }
    }


    private void updateLocationAdapter1(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();
        locationList.add(new LocationBO(0, getString(R.string.select_str)));
        for (LocationBO locationBO : mNewRetailerPresenter.getLocationList()) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }
        locationAdapter = (ArrayAdapter<LocationBO>) getArrayAdapter(locationList);

        spinnerHashMap.get(NewRetailerConstant.LOCATION).setAdapter(locationAdapter);

        defaultLocationSpinner(NewRetailerConstant.LOCATION);

    }


    private void loadSubChannel(int channelId) {

        ArrayList<SpinnerBO> subchannelBOS = new ArrayList<>();

        subchannelBOS.add(new SpinnerBO(0, getResources().getString(R.string.select_str) + " " + "Sub Chanel"));
        subchannelBOS.addAll(mNewRetailerPresenter.getSubChannelsForAChannel(channelId));

        subChannelAdapter = (ArrayAdapter<SpinnerBO>) getArrayAdapter(subchannelBOS);

        spinnerHashMap.get(NewRetailerConstant.SUBCHANNEL).setAdapter(subChannelAdapter);


        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                spinnerHashMap.get(NewRetailerConstant.SUBCHANNEL).setEnabled(false);
            for (int i = 0; i < subChannelAdapter.getCount(); i++) {
                if (subChannelAdapter.getItem(i).getId() == mNewRetailerPresenter.getSubChannel())
                    spinnerHashMap.get(NewRetailerConstant.SUBCHANNEL).setSelection(i);
            }
        }

    }


    private void initializeLayoutParams() {
        commonsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsParams.setMargins(10, 15, 10, 0);

        editWeightMargin = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editWeightMargin.weight = 1;
        editWeightMargin.setMargins(7, 0, 0, 0);

        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.setMargins(30, 0, 0, 0);
        weight2.gravity = Gravity.CENTER;

        commonsparams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams3.setMargins(0, 10, 0, 0);

        spinnerMargin = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams paramsAttrib = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAttrib.weight = .7f;
        paramsAttrib.setMargins(0, 5, 10, 0);

        LinearLayout.LayoutParams paramsAttribSpinner = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsAttribSpinner.weight = 2.3f;

        mandatoryTextViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mandatoryTextViewParams.gravity = Gravity.CENTER;
        mandatoryTextViewParams.setMargins(7, 0, 0, 0);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(7, 0, 0, 0);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        latlongTextViewWeight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        latlongTextViewWeight.setMargins(10, 0, 0, 5);

    }


    private void initializeToolbarAction() {
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }


    private void getScreenAspectRatio() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        screenWidth = size.x;

    }


    private LinearLayout createCommonEditTextView(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode, int inputType, boolean creditPeriod) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        mEditTextInputLayout = new TextInputLayout(getActivity());

        if (mandatory)
            setMandatoryView(linearlayout);

        getSingleEditTextView(menuNumber, menuName, isUppercaseLetter, mConfigCode, mandatory, inputType, creditPeriod);

        linearlayout.addView(mEditTextInputLayout, commonsparams3);

        return linearlayout;
    }


    private LinearLayout createCommonEditTextView(int menuNumber, String menuName, boolean mandatory, boolean isUppercaseLetter, String mConfigCode, int inputType) {

        return createCommonEditTextView(menuNumber, menuName, mandatory, isUppercaseLetter, mConfigCode, inputType, false);
    }


    private LinearLayout createContactList(int menuNumber, String menuName, boolean isMandatory,
                                           boolean isUppercaseLetter, String configCode,
                                           boolean isContactTitle, String spinnerKey) {

        LinearLayout contactCreationLL = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.view_contact, null);

        if (isMandatory)
            setMandatoryView(contactCreationLL);

        if (editTextHashMap == null)
            editTextHashMap = new SparseArray<>();

        TextInputLayout titleWrapper = contactCreationLL.findViewById(R.id.titleWrapper);
        AppCompatEditText contatTitleEt = contactCreationLL.findViewById(R.id.contactOther);
        editTextHashMap.put(menuNumber + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY, contatTitleEt);

        TextInputLayout contacFirstNameWrapper = contactCreationLL.findViewById(R.id.contacFirstNameWrapper);
        AppCompatEditText contacFirstNameEt = contacFirstNameWrapper.findViewById(R.id.contacFirstName);
        editTextHashMap.put(menuNumber + NewRetailerConstant.CONTACT_PERSON_FIRSTNAME_KEY, contacFirstNameEt);

        TextInputLayout contactLastNameWrapper = contactCreationLL.findViewById(R.id.contactLastNameWrapper);
        AppCompatEditText contactLastNameEt = contactCreationLL.findViewById(R.id.contactLastName);
        editTextHashMap.put(menuNumber + NewRetailerConstant.CONTACT_PERSON_LASTNAME_KEY, contactLastNameEt);

        String hintFirstName = menuName + " " + getResources().getString(R.string.contact_person_first_name);
        setContactEditTextAttributes(contacFirstNameEt, contacFirstNameWrapper, isUppercaseLetter,
                hintFirstName, configCode);

        String hintLastName = menuName + " " + getResources().getString(R.string.contact_person_last_name);
        setContactEditTextAttributes(contactLastNameEt, contactLastNameWrapper, isUppercaseLetter,
                hintLastName, configCode);

        if (isContactTitle) {
            contactCreationLL.findViewById(R.id.contactTitleSpinner).setVisibility(View.VISIBLE);
            spinnerHashMap.put(spinnerKey, contactCreationLL.findViewById(R.id.contactTitleSpinner));
            getTitleSpinnerView(titleWrapper, isUppercaseLetter, menuNumber, menuName, configCode, spinnerKey);

        }

        if (NewRetailerConstant.MenuType.OTHER.getMenuType() != screenMode) {
            contacFirstNameEt.setText(mNewRetailerPresenter.getOutlet().getContactpersonname());
            contactLastNameEt.setText(mNewRetailerPresenter.getOutlet().getContactpersonnameLastName());
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()) {
                contacFirstNameEt.setFocusable(false);
                contactLastNameEt.setFocusable(false);
            }
        }

        return contactCreationLL;

    }


    private void getTitleSpinnerView(TextInputLayout titleWrapper,
                                     boolean isUppercaseLetter, int menuNumber,
                                     String menuName, String mConfigCode, String spinnerKey) {

        setContactEditTextAttributes(editTextHashMap.get(menuNumber + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY),
                titleWrapper, isUppercaseLetter, getResources().getString(R.string.contact_title), mConfigCode);

        MaterialSpinner contactTitleSpinner = spinnerHashMap.get(spinnerKey);
        contactTitleSpinner.setId(menuNumber);
        contactTitleSpinner.setFloatingLabelText(menuName);

        ArrayAdapter<ContactTitle> contactTitleAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mNewRetailerPresenter.getContactTitleList());
        contactTitleAdapter.setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);

        contactTitleSpinner.setAdapter(contactTitleAdapter);
        contactTitleSpinner.setSelection(0);

        contactTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                if (tempBo.getListId() == 0) {
                    titleWrapper.setVisibility(View.VISIBLE);
                    editTextHashMap.get(menuNumber + NewRetailerConstant.CONTACT_PERSON_OTHERNAME_KEY).requestFocus();
                } else if (tempBo.getListId() == -1) {
                    titleWrapper.setVisibility(View.GONE);
                    mNewRetailerPresenter.getOutlet().setContact1title("0");
                    mNewRetailerPresenter.getOutlet().setContact1titlelovid("0");
                } else {
                    titleWrapper.setVisibility(View.GONE);
                    mNewRetailerPresenter.getOutlet().setContact2title("0");
                    mNewRetailerPresenter.getOutlet().setContact2titlelovid("" + tempBo.getListId());
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType()
                || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                contactTitleSpinner.setEnabled(false);
            mNewRetailerPresenter.getSelectedContactTitle(menuNumber, spinnerKey);
        }

    }


    private void setMandatoryView(LinearLayout linearlayout) {
        if (Build.VERSION.SDK_INT >= 21)
            linearlayout.addView(new MandatoryTextView(getActivity()), 0);//layoutParamsAfterLollipop
        else
            linearlayout.addView(new MandatoryTextView(getActivity()), 0);//centerGravityParam
    }


    private void setContactEditTextAttributes(AppCompatEditText editText, TextInputLayout wrapper,
                                              boolean isUppercaseLetter, String hint, String mConfigCode) {

        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        editText.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        if (!isUppercaseLetter)
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        else
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        wrapper.setHint(hint);

        setInputFilter(mConfigCode, editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {
                String s = et != null ? et.toString() : "";
                if (!NewRetailerConstant.EMAIL.equalsIgnoreCase(mConfigCode)
                        && isUppercaseLetter && !s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    editText.setText(s);
                    editText.setSelection(editText.length());
                }


            }
        });

    }


    @Override
    public void updateLocationDataBasedOnPinCode(int menuNumber, String locationName) {
        editTextHashMap.get(menuNumber).setText(locationName);
    }

    @Override
    public void showContactMandatoryErrorMessage() {
        showMessage(R.string.contact_list_mandatory);
    }

    @Override
    public void showNoUsersError() {

    }

    @Override
    public void showPaymentTypeListEmptyError() {

    }

    @Override
    public void showDistributorTypeMasterEmptyError() {

    }

    @Override
    public void showTaxListEmptyError() {

    }

    @Override
    public void showPriorityProductsEmptyError() {

    }

    @Override
    public void showClassTypeEmptyError() {

    }

    @Override
    public void showEmptyContactStatusError() {

    }

    private void getSingleEditTextView(final int menuNumber, String menuName, final boolean isUppercaseLetter, final String mConfigCode, boolean mandatory, int inputType, boolean creditPeriod) {

        AppCompatEditText appCompatEditText = new AppCompatEditText(getActivity());
        appCompatEditText.setId(menuNumber);
        appCompatEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        appCompatEditText.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        appCompatEditText.setTextColor(ContextCompat.getColor(getActivity(), R.color.filer_level_text_color));
        setInputType(appCompatEditText, inputType, isUppercaseLetter, menuName);
        setInputFilter(mConfigCode, appCompatEditText);

        if (screenMode != 0) {
            appCompatEditText.setText(mNewRetailerPresenter.getOutletData(mConfigCode));
            if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                appCompatEditText.setFocusable(false);
        }

        if (!mandatory)
            mEditTextInputLayout.addView(appCompatEditText, editWeightMargin);
        else
            mEditTextInputLayout.addView(appCompatEditText, weight1);

        if (editTextHashMap == null)
            editTextHashMap = new SparseArray<>();
        editTextHashMap.put(menuNumber, appCompatEditText);

        addTextWatcher(menuNumber, mConfigCode, isUppercaseLetter, creditPeriod);

    }


    private void addTextWatcher(final int menuNumber, final String mConfigCode,
                                final boolean isUppercaseLetter, boolean creditPeriod) {
        editTextHashMap.get(menuNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {
                String s = et.toString();
                if (!creditPeriod) {
                    if (!NewRetailerConstant.EMAIL.equalsIgnoreCase(mConfigCode) &&
                            isUppercaseLetter && !s.equals(s.toUpperCase())) {
                        s = s.toUpperCase();
                        editTextHashMap.get(menuNumber).setText(s);
                        editTextHashMap.get(menuNumber).setSelection(editTextHashMap.get(menuNumber).length());
                    }
                } else {
                    if (!"".equalsIgnoreCase(s)) {
                        if (SDUtil.convertToInt(s) > mNewRetailerPresenter.getMaxCreditDays()) {
                            editTextHashMap.get(menuNumber).setText(s.length() > 1 ? s.substring(0, s.length() - 1) : "0");
                            showAlert(getResources().getString(R.string.max_credit_days_allowed), String.valueOf(mNewRetailerPresenter.getMaxCreditDays()));

                        }
                    }
                }

                if (NewRetailerConstant.PINCODE.equalsIgnoreCase(mConfigCode)) {
                    mNewRetailerPresenter.loadLocationDataBasedOnPinCode(s);
                }
            }
        });
    }


    private void setInputFilter(String mConfigCode, AppCompatEditText appCompatEditText) {
        if (!NewRetailerConstant.EMAIL.equalsIgnoreCase(mConfigCode)) {
            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                appCompatEditText.setFilters(stockArr);
                if (inputFilters.size() == 2)
                    appCompatEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        }
    }


    private void setInputType(AppCompatEditText appCompatEditText,
                              int editTextType, boolean isUppercaseLetter, String menuName) {
        if (editTextType == InputType.TYPE_TEXT_VARIATION_PERSON_NAME) {
            if (!isUppercaseLetter)
                appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            else
                appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            appCompatEditText.setHint(menuName);
        } else if (editTextType == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
            appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            appCompatEditText.setHint(menuName);
        } else if (editTextType == InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS) {
            if (!isUppercaseLetter)
                appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
            else
                appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            appCompatEditText.setHint(menuName);
        } else if (editTextType == InputType.TYPE_CLASS_NUMBER) {
            appCompatEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            appCompatEditText.setHint(menuName);
        } else if (editTextType == InputType.TYPE_CLASS_TEXT) {
            if (!isUppercaseLetter)
                appCompatEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            else
                appCompatEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

            appCompatEditText.setHint(menuName);
        }
    }


    private void setSpinnerView(boolean mandatory, MaterialSpinner materialSpinner, String configCode) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        if (mandatory)
            setMandatoryView(layout);
        LinearLayout firstLayout = new LinearLayout(getActivity());
        if (!mandatory) {
            spinnerMargin.setMargins(7, 0, 7, 0);
            firstLayout.addView(materialSpinner, spinnerMargin);
            layout.addView(firstLayout, editWeightMargin);
        } else {
            firstLayout.addView(materialSpinner, spinnerMargin);
            spinnerMargin.weight = 1;
            spinnerMargin.setMargins(0, 18, 0, 0);
            layout.addView(firstLayout, spinnerMargin);
        }

        if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType() || screenMode == NewRetailerConstant.MenuType.EDIT.getMenuType()) {
            if (!StringUtils.isNullOrEmpty(configCode)) {
                materialSpinner.setSelection(mNewRetailerPresenter.getSpinnerSelectedItem(configCode) + 1);
                if (screenMode == NewRetailerConstant.MenuType.VIEW.getMenuType())
                    materialSpinner.setEnabled(false);
            }
        }
        mRootLinearLayout.addView(layout, commonsParams);
    }


    private void detachFragment() {
        HomeScreenFragment currentFragment = (HomeScreenFragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
        if (currentFragment != null) {
            currentFragment.detach("MENU_NEW_RET");
        }
    }


}
