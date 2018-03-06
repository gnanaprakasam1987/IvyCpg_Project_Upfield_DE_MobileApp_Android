package com.ivy.sd.png.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.maplib.BaiduMapDialogue;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class NewOutletFragment extends IvyBaseFragment implements NearByRetailerDialog.NearByRetailerInterface {
    private double lattitude = 0;
    private double longitude = 0;

    private final String MENU_NEW_RETAILER = "MENU_NEW_RET";

    private boolean isLatLong;
    private String imageName;
    private ArrayList<LocationBO> mLocationMasterList1;
    private ArrayList<LocationBO> mLocationMasterList2;
    private ArrayList<LocationBO> mLocationMasterList3;
    private ArrayList<NewOutletBO> mretailertypeMasterList;
    private ArrayList<NewOutletBO> mcontractStatusList;
    private ArrayList<NewOutletBO> mcontactTitleList;
    private ArrayList<DistributorMasterBO> mdistributortypeMasterList;
    private ArrayList<StandardListBO> mTaxTypeList;
    private ArrayList<StandardListBO> mClassTypeList;
    private ArrayList<StandardListBO> mPriorityProductList;
    private ArrayList<UserMasterBO> mUserList;
    private ArrayList<NewOutletAttributeBO> mAttributeParentList; // List of parentid = 0
    private HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap; // List of first spinner for each level
    private HashMap<Integer, NewOutletAttributeBO> selectedAttribList; // Hashmap to retreive selected level of Attribute
    private ArrayList<NewOutletAttributeBO> selectedAttributeLevel; // List of chosen last level
    private HashMap<Integer, ArrayList<Integer>> mAttributeListByChannelID;


    private ArrayAdapter<NewOutletBO> contactTitleAdapter;
    private ArrayAdapter<DistributorMasterBO> distributortypeAdapter;
    private ArrayAdapter<LocationBO> locationAdapter1;
    private ArrayAdapter<LocationBO> locationAdapter2;
    private ArrayAdapter<LocationBO> locationAdapter3;
    private ArrayAdapter<BeatMasterBO> routeAdapter;
    private Timer timer;
    private ArrayAdapter<String> mImageTypeAdapter;
    private String PHOTO_PATH = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String TAG = "NewOutlet Screen";
    private final String moduleName = "NO_";
    private boolean isLocSelectedManually = false;
    private NewOutletBO outlet;
    private Vector<ChannelBO> channelMaster;
    private Vector<SubchannelBO> subchannelMaster;
    private Vector<BeatMasterBO> beatMaster;
    private AppCompatCheckBox marketCheckBox[] = null;
    private AppCompatCheckBox weekNoCheckBox[] = null;
    private AppCompatCheckBox inSEZcheckBox = null;
    private String checkedDaysAll = null;
    private String weekNoStr = null;
    private String beatName = null;
    private String routeMname = "";
    private static final int MAX_NO_OF_DAYS = 7;
    private static String[] days = null;
    private static final String[] daysForUpload = new String[]{"SUN", "MON",
            "TUE", "WED", "THU", "FRI", "SAT"};
    private static final int NUMBER_OF_WEEKS = 5;
    private String uID;
    private BusinessModel bmodel;
    private static StringBuffer sb;
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;
    private int mSelectedpostion = -1;


    private Vector<ConfigureBO> profileConfig;
    private ScrollView scrollview2;
    private MaterialSpinner channel, subchannel, location1, location2, location3,
            route, paymentType, distributorSpinner, taxTypeSpinner, contactTitleSpinner1,
            contactTitleSpinner2, contractSpinner, classSpinner, userSpinner;
    private TextView latlongtextview;
    private AppCompatAutoCompleteTextView priorityProductAutoCompleteTextView, nearbyAutoCompleteTextView;

    private AppCompatEditText editText[] = null;
    private TextView textview[] = null;
    private TextInputLayout edittextinputLayout, edittextinputLayout2, edittextinputLayout3, edittextinputLayout4;

    private LinearLayout.LayoutParams commonsparams, commonsparams3, commonsparams4, params, params2, params3, params3new, params4new, params4aflollipop, params4, params5, params5new, params5aflollipop, params6, paramsaflollipop, params6aflollipop, params8, params9, params10, params11, params12, params13, paramsAttrib, paramsAttribSpinner;
    private LinearLayout.LayoutParams weight1, weight2, weight3, weight6, weight0, weight0wrap, weight0marginbottom, editweightmargin, weight1new;
    private LinearLayout.LayoutParams textinputlayoutparams, textinputlayoutparams2, textinputlayoutparams3;

    boolean isChannel = false;
    boolean issubChannel = false;
    boolean isRoute = false;
    boolean isLocation = false;
    private boolean isLocation1 = false;
    private boolean isLocation2 = false;
    private boolean isContactTitle = false;
    private boolean isAttribute = false;

    NewRetailerReceiver receiver;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    int width = 0;

    public static String sub_chanel_mname;


    ArrayList<StandardListBO> priorityProductIDList;
    Vector<RetailerMasterBO> mselectedRetailers;
    int screenMode;
    String retailerId_edit = "";

    private final int VIEW = 1;
    private final int EDIT = 2;
    private final int CREATE_FRM_EDT_SCREEN = 4;
    private Vector<RetailerMasterBO> mNearbyRetailerList;

    private int screenwidth = 0;
    private HashMap<Integer, String> mEnteredeEditTextByKeyValue;
    public ArrayList<String> mEnteredEditTextkeyValue = new ArrayList<>();

    private View view;
    private HashMap<String, MaterialSpinner> spinnerHashMap = null;
    private HashMap<String, ArrayAdapter<NewOutletAttributeBO>> spinnerAdapterMap = null;
    private String screenTitle = null;

    private ArrayList<InputFilter> inputFilters = new ArrayList<>();
    static TextView tinExpDateTextView;
    private SurveyHelperNew surveyHelperNew;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        surveyHelperNew = SurveyHelperNew.getInstance(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(3);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_newoutlet, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        width = displaymetrics.widthPixels;

//        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle(bmodel.mSelectedActivityName);
//            actionBar.setIcon(R.drawable.icon_new_retailer);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//        }
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Set Screen Title
        try {
            if (getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_NEW_RETAILER"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } catch (Exception e) {

            setScreenTitle(getResources().getString(R.string.new_retailer));
            Commons.printException(e);
        }

        if (Build.VERSION.SDK_INT >= 14) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            screenwidth = size.x;
        } else {
            screenwidth = displaymetrics.widthPixels;
        }
        bmodel.newOutletHelper.setMaterialSpinner(null);
        bmodel.newOutletHelper.setEditText(null);

        Button saveBtn = (Button) view.findViewById(R.id.new_outlet_save);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.bottom_layout);
        editText = new AppCompatEditText[100];
        textview = new TextView[100];

        uID = SDUtil.now(SDUtil.DATE_TIME_ID);
        bmodel.newOutletHelper.setId(bmodel.userMasterHelper.getUserMasterBO()
                .getDistributorid()
                + ""
                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + ""
                + SDUtil.now(SDUtil.DATE_TIME_ID));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bool = validateProfile();
                if (bool) {

                    if (bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                        if (lattitude == 0 || longitude == 0 || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl != 0 && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)) {
                            Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    setValues();

                    if (!bmodel.newOutletHelper.getNewoutlet().getOutletName().isEmpty()) {
                        if (screenMode == EDIT
                                || (!bmodel.newOutletHelper.isRetailerAlreadyAvailable(bmodel.newOutletHelper.getNewoutlet().getOutletName(),
                                bmodel.newOutletHelper.getNewoutlet().getPincode()))) {
                            if (!bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_UPLOAD) {
                                new SaveNewOutlet().execute("");
                            } else {
                                new SaveNewOutlet().execute("1");
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.retailer_already_available, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.newretailer_empty, Toast.LENGTH_LONG).show();
                    }


                }
            }
        });


        outlet = new NewOutletBO();
        mLocationMasterList1 = new ArrayList<>();
        mLocationMasterList2 = new ArrayList<>();
        mLocationMasterList3 = new ArrayList<>();

        screenMode = getActivity().getIntent().getIntExtra("screenMode", 0);

        if (screenMode == VIEW)
            linearLayout.setVisibility(View.GONE);

        if (screenMode == VIEW || screenMode == EDIT) {
            retailerId_edit = getActivity().getIntent().getStringExtra("retailerId");
            outlet = bmodel.newOutletHelper.getmNewRetailerById().get(retailerId_edit);
        }

        if (retailerId_edit.equals("")) {
            bmodel.setOrderHeaderNote("");
        }
        profileConfig = new Vector<>();
        profileConfig = bmodel.newOutletHelper.getProfileConfiguraion();

        bmodel.newOutletHelper.loadImageType();
        bmodel.newOutletHelper.loadContactTitle();
        bmodel.newOutletHelper.loadContactStatus();
        mImageTypeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        distributortypeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);


        for (NewOutletBO temp : bmodel.newOutletHelper.getImageTypeList()) {
            mImageTypeAdapter.add(temp.getListName());
        }
        //for checking to show contact title spinner
        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase("CONTACTTITLE") && configureBO.isFlag() == 1) {
                isContactTitle = true;
                mcontactTitleList = new ArrayList<>();
                mcontactTitleList.add(0, new NewOutletBO(-1, getResources().getString(R.string.select_str) + " " + "Title"));
                mcontactTitleList.addAll(bmodel.newOutletHelper.getContactTitleList());

                mcontactTitleList.add(bmodel.newOutletHelper.getContactTitleList().size() + 1, new NewOutletBO(0, "OTHERS"));
                Commons.print("Size Contact List title : " + bmodel.newOutletHelper.getContactTitleList().size());
                contactTitleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontactTitleList);
                contactTitleAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            }
        }

        updateLocationMasterList();
        // Validating download masters
        ValidateandDownloadMasters();
        // load profile
        loadProfile();


        registerReceiver();


        return view;
    }

    private void loadsubchannel(int channelid) {

        Vector items = bmodel.subChannelMasterHelper.getSubChannelMaster();

        int siz = items.size();
        if (siz == 0) {
            return;
        }
        ArrayAdapter<SpinnerBO> subchannelAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        subchannelAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        subchannelAdapter.add(new SpinnerBO(0, getActivity().getResources()
                .getString(R.string.select_str) + " " + sub_chanel_mname));

        for (int i = 0; i < siz; ++i) {
            SubchannelBO ret = (SubchannelBO) items.elementAt(i);
            if (channelid != 0) {
                if (channelid == ret.getChannelid()) {
                    subchannelAdapter.add(new SpinnerBO(ret.getSubchannelid(),
                            ret.getSubChannelname()));
                }
            }
        }
        subchannel.setAdapter(subchannelAdapter);
        subchannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerBO subChannelBO = (SpinnerBO) parent.getSelectedItem();

                if (mAttributeListByChannelID != null && mAttributeListByChannelID.get(subChannelBO.getId()) != null) {
                    for (ConfigureBO bo : profileConfig) {
                        if (bo.getConfigCode().equalsIgnoreCase("ATTRIBUTE")) {

                            addAttributeView(bo.getMenuName(), bo.getMandatory(), 1);
                            break;
                        }

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (screenMode == VIEW || screenMode == EDIT) {
            for (int i = 0; i < subchannelAdapter.getCount(); i++) {
                if (subchannelAdapter.getItem(i).getId() == outlet.getSubChannel())
                    subchannel.setSelection(i);
            }

            if (screenMode == VIEW)
                subchannel.setEnabled(false);
        }

    }

    private void ValidateandDownloadMasters() {

        for (int i = 0; i < profileConfig.size(); i++) {
            if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("CHANNEL")) {
                isChannel = true;
                channelMaster = bmodel.channelMasterHelper.getChannelMaster();
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("SUBCHANNEL")) {
                subchannelMaster = bmodel.subChannelMasterHelper
                        .getSubChannelMaster();
                issubChannel = true;
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("ROUTE")) {
                beatMaster = bmodel.beatMasterHealper.getBeatMaster();
                isRoute = true;
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("LOCATION")) {

                isLocation = true;
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("LOCATION01")) {

                isLocation1 = true;
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("LOCATION02")) {

                isLocation2 = true;
            } else if (profileConfig.get(i).getConfigCode()
                    .equalsIgnoreCase("USER")) {
                mUserList = bmodel.userMasterHelper.downloadAllUser();

            } else if ("ATTRIBUTE"
                    .equalsIgnoreCase(profileConfig.get(i).getConfigCode())) {
                isAttribute = true;
                bmodel.newOutletAttributeHelper.downloadAttributeParentList();
                bmodel.newOutletAttributeHelper.downloadRetailerAttribute();
                mAttributeParentList = bmodel.newOutletAttributeHelper.getAttributeParentList();
                attribMap = bmodel.newOutletAttributeHelper.getAttribMap();

                bmodel.newOutletAttributeHelper.downloadCommonAttributeList();


            }
        }

        if (isAttribute && issubChannel)
            mAttributeListByChannelID = bmodel.newOutletAttributeHelper.downloadChannelWiseAttributeList();

        if (isChannel && channelMaster.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_channels_download),
                    Toast.LENGTH_LONG).show();
        } else if (issubChannel && subchannelMaster.size() == 0) {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.no_sub_channels_download),
                    Toast.LENGTH_LONG).show();
        } else if (isRoute && beatMaster.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_beats_download),
                    Toast.LENGTH_LONG).show();
        } else if (isLocation) {
            if (mLocationMasterList1 == null) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            } else if (mLocationMasterList1.size() == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            }

        } else if (isLocation1) {
            if (mLocationMasterList2 == null) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            } else if (mLocationMasterList2.size() == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            }

        } else if (isLocation2) {
            if (mLocationMasterList3 == null) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            } else if (mLocationMasterList3.size() == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_location_download),
                        Toast.LENGTH_LONG).show();
            }
        } else if (isAttribute && mAttributeParentList.isEmpty()) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_retailer_attribute),
                    Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
        if (timer != null) {
            timer.cancel();
        }

        if (screenMode != VIEW) {
            timer = new Timer();
            MyTimerTask myTimerTask = new MyTimerTask();
            timer.schedule(myTimerTask, 800, 4000);
        }

        if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG && isLatLong)
            if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                Integer resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
                if (resultCode != ConnectionResult.SUCCESS) {
                    bmodel.requestLocation(getActivity());
                } else
                    onCreateDialogNew();
            }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * getActivity() would clear all the resources used of the layout.
     * <p>
     * param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    private void loadProfile() {
        params5 = new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params5.gravity = Gravity.CENTER;
        params5.setMargins(0, 25, 0, 0);


        weight3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight3.weight = 3;
        weight3.gravity = Gravity.CENTER;

        params3 = new LinearLayout.LayoutParams(width / 4, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.weight = 1;


        params3new = new LinearLayout.LayoutParams(width / 6, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params3new.weight = 1;


        params4new = new LinearLayout.LayoutParams(width / 4, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params4new.weight = 1;
        params4new.setMargins(7, 27, 0, 0);

        params4aflollipop = new LinearLayout.LayoutParams(width / 4, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params4aflollipop.weight = 1;
        params4aflollipop.setMargins(7, 39, 0, 0);

        params5new = new LinearLayout.LayoutParams(width / 4, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params5new.weight = 1;
        params5new.setMargins(0, 27, 0, 0);

        params5aflollipop = new LinearLayout.LayoutParams(width / 4, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params5aflollipop.weight = 1;
        params5aflollipop.setMargins(0, 39, 0, 0);


        scrollview2 = (ScrollView) view.findViewById(R.id.scrollview2);

        editText = new AppCompatEditText[100];
        textview = new TextView[100];


        commonsparams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams.setMargins(10, 15, 10, 0);

        commonsparams3 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams3.setMargins(0, 10, 0, 0);

        commonsparams4 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams4.weight = 1;

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(7, 0, 0, 0);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        params2 = new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.CENTER_VERTICAL;
        params2.setMargins(0, 10, 0, 0);

        params4 = new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params4.gravity = Gravity.CENTER;
        params4.setMargins(0, 0, 0, 5);


        params6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params6.gravity = Gravity.CENTER;
        params6.setMargins(0, 20, 0, 0);

        paramsaflollipop = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsaflollipop.setMargins(0, 64, 0, 0);

        params6aflollipop = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params6aflollipop.gravity = Gravity.CENTER;
        params6aflollipop.setMargins(0, 25, 0, 0);

        params8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params8.gravity = Gravity.CENTER;
        params8.setMargins(7, 0, 0, 0);

        params9 = new LinearLayout.LayoutParams(50,
                50);
        params9.setMargins(5, 0, 0, 0);

        params13 = new LinearLayout.LayoutParams(50,
                50);
        params13.setMargins(5, 20, 0, 0);


        params10 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params10.weight = 1;
        params10.setMargins(0, 18, 0, 0);

        params11 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params11.setMargins(0, 10, 0, 0);
        params11.gravity = Gravity.CENTER;


        weight0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        weight0wrap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight0wrap.setMargins(10, 0, 0, 5);

        weight0marginbottom = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight0marginbottom.setMargins(60, 10, 0, 0);

        editweightmargin = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        editweightmargin.weight = 1;
        editweightmargin.setMargins(7, 0, 0, 0);


        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight1new = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1new.weight = 1;
        weight1new.setMargins(70, 0, 0, 10);

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.setMargins(30, 0, 0, 0);
        weight2.gravity = Gravity.CENTER;

        params12 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params12.setMargins(7, 0, 7, 0);


        weight6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight6.weight = 6;
        weight6.gravity = Gravity.CENTER;

        textinputlayoutparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        textinputlayoutparams2 = new LinearLayout.LayoutParams(width / 4,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);


        textinputlayoutparams3 = new LinearLayout.LayoutParams(width / 6,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

        paramsAttrib = new LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAttrib.weight = .7f;
        paramsAttrib.setMargins(0, 5, 10, 0);

        paramsAttribSpinner = new LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsAttribSpinner.weight = 2.3f;


        final LinearLayout totalView = new LinearLayout(getActivity());
        totalView.setOrientation(LinearLayout.VERTICAL);
        try {

            int size = profileConfig.size();
            for (int i = 0; i < size; i++) {

                int mandatory;
                int hasLink = profileConfig.get(i).getHasLink();
                String mName = profileConfig.get(i).getMenuName();
                String configCode = profileConfig.get(i).getConfigCode();
                mandatory = profileConfig.get(i).getMandatory();
                Commons.print("configCode -- " + configCode + ",   mandatory  " + mandatory + ",Menu Number" + i);

                if (configCode.equalsIgnoreCase("STORENAME")
                        || configCode.equalsIgnoreCase("ADDRESS1")
                        || configCode.equalsIgnoreCase("CONTACTPERSON1")
                        || configCode.equalsIgnoreCase("ADDRESS2")
                        || configCode.equalsIgnoreCase("ADDRESS3")
                        || configCode.equalsIgnoreCase("CITY")
                        || configCode.equalsIgnoreCase("STATE")
                        || configCode.equalsIgnoreCase("CONTACTPERSON2")
                        ) {

                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME, mandatory),
                            commonsparams);

                } else if (configCode.equalsIgnoreCase("EMAIL")) {
                    totalView.addView(
                            getEditTextView(i, mName,
                                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("PHNO1")
                        || configCode.equalsIgnoreCase("PHNO2")
                        || configCode.equalsIgnoreCase("CREDITLIMIT")
                        || configCode.equalsIgnoreCase("FAX")
                        || configCode.equalsIgnoreCase("CREDITPERIOD")) {

                    totalView.addView(
                            getEditTextView(i, mName,
                                    InputType.TYPE_CLASS_NUMBER, mandatory),
                            commonsparams);

                } else if (configCode.equalsIgnoreCase("CHANNEL")
                        || configCode.equalsIgnoreCase("SUBCHANNEL")
                        || configCode.equalsIgnoreCase("ROUTE")
                        || configCode.equalsIgnoreCase("LOCATION")
                        || configCode.equalsIgnoreCase("LOCATION01")
                        || configCode.equalsIgnoreCase("LOCATION02")
                        || configCode.equalsIgnoreCase("PAYMENTTYPE")
                        || configCode.equalsIgnoreCase("DISTRIBUTOR")
                        || configCode.equalsIgnoreCase("CONTRACT")
                        || configCode.equalsIgnoreCase("TAXTYPE")
                        || configCode.equalsIgnoreCase("CLASS")
                        || configCode.equalsIgnoreCase("USER")
                        ) {

                    totalView.addView(
                            getSpinnerView(i, mName, configCode,
                                    mandatory, hasLink), commonsparams);

                } else if (configCode.equalsIgnoreCase("PLAN")) {

                    // Days CheckBox
                    days = getActivity().getResources().getStringArray(
                            R.array.days_in_week);
                    TableLayout daysTableLayout = new TableLayout(getActivity());
                    daysTableLayout.setStretchAllColumns(false);

                    TableRow daysableRow1 = new TableRow(getActivity());
                    daysableRow1.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                    daysableRow1.setWeightSum(8);

                    TableRow daysableRow2 = new TableRow(getActivity());
                    daysableRow2.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                    daysableRow2.setWeightSum(6);

                    marketCheckBox = new AppCompatCheckBox[MAX_NO_OF_DAYS];
                    marketCheckBox[0] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[0].setScaleX((float) 1.1);
                        marketCheckBox[0].setScaleY((float) 1.1);
                        marketCheckBox[0].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.6f));
                    }

                    daysableRow1.addView(marketCheckBox[0]);

                    marketCheckBox[1] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[1].setScaleX((float) 1.1);
                        marketCheckBox[1].setScaleY((float) 1.1);
                        marketCheckBox[1].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.6f));
                    }

                    daysableRow1.addView(marketCheckBox[1]);

                    marketCheckBox[2] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[2].setScaleX((float) 1.1);
                        marketCheckBox[2].setScaleY((float) 1.1);
                        marketCheckBox[2].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.6f));
                    }

                    daysableRow1.addView(marketCheckBox[2]);

                    marketCheckBox[3] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[3].setScaleX((float) 1.1);
                        marketCheckBox[3].setScaleY((float) 1.1);
                        marketCheckBox[3].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.6f));
                    }

                    daysableRow1.addView(marketCheckBox[3]);

                    marketCheckBox[4] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[4].setScaleX((float) 1.1);
                        marketCheckBox[4].setScaleY((float) 1.1);
                        marketCheckBox[4].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));
                    }

                    daysableRow2.addView(marketCheckBox[4]);

                    marketCheckBox[5] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[5].setScaleX((float) 1.1);
                        marketCheckBox[5].setScaleY((float) 1.1);
                        marketCheckBox[5].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));
                    }

                    daysableRow2.addView(marketCheckBox[5]);

                    marketCheckBox[6] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        marketCheckBox[6].setScaleX((float) 1.1);
                        marketCheckBox[6].setScaleY((float) 1.1);
                        marketCheckBox[6].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));
                    }

                    daysableRow2.addView(marketCheckBox[6]);

                    daysTableLayout.addView(daysableRow1);
                    daysTableLayout.addView(daysableRow2);

                    for (int h = 0; h < MAX_NO_OF_DAYS; h++) {
                        marketCheckBox[h].setChecked(false);
                        marketCheckBox[h].setText(days[h]);
                        marketCheckBox[h].setVisibility(CheckBox.VISIBLE);

                    }

                    //
                    if (screenMode == VIEW || screenMode == EDIT) {
                        for (int h = 0; h < MAX_NO_OF_DAYS; h++) {
                            if (outlet.getVisitDays().contains(daysForUpload[h]))
                                marketCheckBox[h].setChecked(true);

                            if (screenMode == VIEW)
                                marketCheckBox[h].setEnabled(false);
                        }
                    }

                    // Week CheckBox
                    weekNoCheckBox = new AppCompatCheckBox[NUMBER_OF_WEEKS];
                    TableLayout weekTableLayout = new TableLayout(getActivity());

                    TableRow weekTableRow1 = new TableRow(getActivity());
                    weekTableRow1.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                    weekTableRow1.setWeightSum(4);
                    TableRow weekTableRow2 = new TableRow(getActivity());
                    weekTableRow2.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                    weekTableRow2.setWeightSum(4);

                    weekNoCheckBox[0] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        weekNoCheckBox[0].setScaleX((float) 1.1);
                        weekNoCheckBox[0].setScaleY((float) 1.1);
                        weekNoCheckBox[0].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
                    }

                    weekNoCheckBox[0].setText(getResources().getString(R.string.wk1));
                    weekNoCheckBox[0].setTextColor(Color.BLACK);
                    weekTableRow1.addView(weekNoCheckBox[0]);

                    weekNoCheckBox[1] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        weekNoCheckBox[1].setScaleX((float) 1.1);
                        weekNoCheckBox[1].setScaleY((float) 1.1);
                        weekNoCheckBox[1].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
                    }

                    weekNoCheckBox[1].setText(getResources().getString(R.string.wk2));
                    weekTableRow1.addView(weekNoCheckBox[1]);

                    weekNoCheckBox[2] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        weekNoCheckBox[2].setScaleX((float) 1.1);
                        weekNoCheckBox[2].setScaleY((float) 1.1);
                        weekNoCheckBox[2].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
                    }

                    weekNoCheckBox[2].setText(getResources().getString(R.string.wk3));
                    weekTableRow2.addView(weekNoCheckBox[2]);

                    weekNoCheckBox[3] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        weekNoCheckBox[3].setScaleX((float) 1.1);
                        weekNoCheckBox[3].setScaleY((float) 1.1);
                        weekNoCheckBox[3].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
                    }

                    weekNoCheckBox[3].setText(getResources().getString(R.string.wk4));
                    weekTableRow2.addView(weekNoCheckBox[3]);


                    weekNoCheckBox[4] = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        weekNoCheckBox[4].setScaleX((float) 1.1);
                        weekNoCheckBox[4].setScaleY((float) 1.1);
                        weekNoCheckBox[4].setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.80f));
                    }

                    weekNoCheckBox[4].setText(getResources().getString(R.string.wk5));
                    weekTableRow2.addView(weekNoCheckBox[4]);


                    weekTableLayout.addView(weekTableRow1);
                    weekTableLayout.addView(weekTableRow2);

                    if (screenMode == VIEW || screenMode == EDIT) {
                        for (int h = 0; h < NUMBER_OF_WEEKS; h++) {
                            if (outlet.getVisitDays().contains(weekNoCheckBox[h].getText()))
                                weekNoCheckBox[h].setChecked(true);

                            if (screenMode == VIEW)
                                weekNoCheckBox[h].setEnabled(false);
                        }
                    }


                    LinearLayout weekLayout = new LinearLayout(getActivity());
                    weekLayout.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout weektext = new LinearLayout(getActivity());
                    weektext.setOrientation(LinearLayout.HORIZONTAL);

                    if (mandatory == 1) {
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText("*");
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.RED);
                        weektext.addView(mn_textview, weight0);
                    }

                    TextView week = new TextView(getActivity());
                    week.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    String strWeek = getResources().getString(R.string.week) + ":";
                    week.setText(strWeek);
                    week.setTextColor(Color.BLACK);
                    weektext.addView(week, params8);


                    weekLayout.addView(weektext);
                    weekLayout.addView(weekTableLayout, params);
                    totalView.addView(weekLayout, commonsparams);

                    LinearLayout dayLayout = new LinearLayout(getActivity());
                    dayLayout.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout daystext = new LinearLayout(getActivity());
                    daystext.setOrientation(LinearLayout.HORIZONTAL);

                    if (mandatory == 1) {
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText("*");
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.RED);
                        daystext.addView(mn_textview, weight0);
                    }

                    TextView days = new TextView(getActivity());
                    String strDay = getResources().getString(R.string.day) + ":";
                    days.setText(strDay);
                    days.setTextColor(Color.BLACK);
                    days.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    daystext.addView(days, params8);

                    dayLayout.addView(daystext);
                    dayLayout.addView(daysTableLayout, params);
                    totalView.addView(dayLayout, commonsparams);

                } else if (configCode.equalsIgnoreCase("LATLONG")) {
                    latlongtextview = new TextView(getActivity());
                    latlongtextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    latlongtextview.setTextColor(Color.BLACK);
                    totalView.addView(
                            getTextView(i, mName, 0.0 + "", mandatory),
                            commonsparams);
                    isLatLong = true;
                } else if (configCode.equalsIgnoreCase("TINNUM")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_TEXT, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("TINEXPDATE")) {
                    totalView.addView(
                            getTextView(i, mName, "Select Date", mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("RFIELD3")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_TEXT, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("RFIELD5")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_TEXT, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("RFIELD6")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_TEXT, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("PINCODE")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_NUMBER, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("NEARBYRET")) {
                    totalView.addView(
                            getNearByRetailerView(mName,
                                    mandatory), commonsparams);
                } else if (configCode.equalsIgnoreCase("PRIORITYPRODUCT")) {


                    totalView.addView(getPriorityProductView(mName,
                            mandatory, hasLink), commonsparams);
                } else if (configCode.equalsIgnoreCase("ATTRIBUTE")) {
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    LLParams.setMargins(10, 5, 10, 5);
                    totalView.addView(
                            addAttributeView(mName,
                                    mandatory, 0), LLParams);
                } else if (configCode.equalsIgnoreCase("GST_NO")) {
                    totalView.addView(getEditTextView(i, mName,
                            InputType.TYPE_CLASS_TEXT, mandatory),
                            commonsparams);
                } else if (configCode.equalsIgnoreCase("INSEZ")) {

                    LinearLayout baselayout = new LinearLayout(getActivity());
                    baselayout.setOrientation(LinearLayout.VERTICAL);


                    //
                    LinearLayout linearlayout = new LinearLayout(getActivity());
                    linearlayout.setOrientation(LinearLayout.VERTICAL);

                    inSEZcheckBox = new AppCompatCheckBox(getActivity());
                    if (screenwidth > 520) {
                        inSEZcheckBox.setScaleX((float) 1.1);
                        inSEZcheckBox.setScaleY((float) 1.1);
                        inSEZcheckBox.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    }
                    linearlayout.addView(inSEZcheckBox, weight2);
                    //

                    LinearLayout textLayout = new LinearLayout(getActivity());
                    textLayout.setOrientation(LinearLayout.HORIZONTAL);

                    if (mandatory == 1) {
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText("*");
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.RED);
                        textLayout.addView(mn_textview, weight0);
                    }

                    TextView days = new TextView(getActivity());
                    days.setText(mName);
                    days.setTextColor(Color.BLACK);
                    days.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    textLayout.addView(days, params8);
                    //


                    baselayout.addView(textLayout);
                    baselayout.addView(linearlayout);
                    totalView.addView(baselayout, commonsparams);

                }

            }

            scrollview2.addView(totalView);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {


        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (edittextinputLayout != null) {
                edittextinputLayout.setError(null);
                edittextinputLayout.setErrorEnabled(false);
            }
            if (edittextinputLayout2 != null) {
                edittextinputLayout2.setError(null);
                edittextinputLayout2.setErrorEnabled(false);
            }
            if (edittextinputLayout3 != null) {
                edittextinputLayout3.setError(null);
                edittextinputLayout3.setErrorEnabled(false);
            }
            if (edittextinputLayout4 != null) {
                edittextinputLayout4.setError(null);
                edittextinputLayout4.setErrorEnabled(false);
            }

        }
    };

    private void scrollToSpecificEditText(final TextInputLayout secificEditText) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Determine where to set the scroll-to to by measuring the distance from the top of the scroll view
                // to the control to focus on by summing the "top" position of each view in the hierarchy.
                int yDistanceToControlsView = 0;
                View parentView = secificEditText;
                while (true) {
                    if (parentView.equals(scrollview2)) {
                        break;
                    }
                    yDistanceToControlsView += parentView.getTop();
                    parentView = (View) parentView.getParent();
                }

                // Compute the final position value for the top and bottom of the control in the scroll view.
                final int topInScrollView = yDistanceToControlsView + secificEditText.getTop();
                final int bottomInScrollView = yDistanceToControlsView + secificEditText.getBottom();

                // Post the scroll action to happen on the scrollView with the UI thread.
                scrollview2.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = secificEditText.getHeight();
                        scrollview2.smoothScrollTo(0, ((topInScrollView + bottomInScrollView) / 2) - height);

                    }
                });
            }
        }).start();

    }

    private void scrollToSpecificSpinner(final MaterialSpinner specificSpinner) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Determine where to set the scroll-to to by measuring the distance from the top of the scroll view
                // to the control to focus on by summing the "top" position of each view in the hierarchy.
                int yDistanceToControlsView = 0;
                View parentView = (View) specificSpinner.getParent();
                while (true) {
                    if (parentView.equals(scrollview2)) {
                        break;
                    }
                    yDistanceToControlsView += parentView.getTop();
                    parentView = (View) parentView.getParent();
                }

                // Compute the final position value for the top and bottom of the control in the scroll view.
                final int topInScrollView = yDistanceToControlsView + specificSpinner.getTop();
                final int bottomInScrollView = yDistanceToControlsView + specificSpinner.getBottom();

                // Post the scroll action to happen on the scrollView with the UI thread.
                scrollview2.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = specificSpinner.getHeight();
                        scrollview2.smoothScrollTo(0, ((topInScrollView + bottomInScrollView) / 2) - height);

                    }
                });
            }
        }).start();
    }

    @SuppressLint("NewApi")
    private boolean validateProfile() {
        boolean validate = true;
        try {

            int size = profileConfig.size();
            for (int j = 0; j < size; j++)
                Commons.print(" " + profileConfig.get(j).getConfigCode() + " " + profileConfig.get(j).getMandatory());
            for (int i = 0; i < size; i++) {

                int mandatory;
                String menuName = profileConfig.get(i).getMenuName();
                mandatory = profileConfig.get(i).getMandatory();

                if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("STORENAME")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;

                    }


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("ADDRESS1")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("ADDRESS2")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        validate = false;

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("ADDRESS3")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        validate = false;

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CITY")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        validate = false;

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("STATE")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();

                    if (editText[i].getText().toString().trim().length() == 0) {
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        validate = false;

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CONTACTPERSON1")
                        && mandatory == 1) {

                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    edittextinputLayout2 = (TextInputLayout) editText[i + 50].getParentForAccessibility();
                    if (isContactTitle) {
                        edittextinputLayout4 = (TextInputLayout) editText[i + 50 + 5].getParentForAccessibility();
                        if (contactTitleSpinner1.getSelectedItem().toString()
                                .contains("Select")) {
                            validate = false;
                            scrollToSpecificSpinner(contactTitleSpinner1);
                            contactTitleSpinner1.requestFocus();

                            contactTitleSpinner1.setError(getResources().getString(R.string.select_str) + " Title");
                            break;
                        }
                        if (contactTitleSpinner1.getSelectedItem().toString()
                                .contains("OTHERS")) {

                            if (editText[i + 50 + 5].getText().toString().trim().length() == 0) {
                                validate = false;
                                scrollToSpecificEditText(edittextinputLayout4);
                                editText[i + 50 + 5].requestFocus();
                                edittextinputLayout4.setError(getResources().getString(R.string.enter) + " Title");
                                editText[i + 50 + 5].addTextChangedListener(watcher);
                                break;


                            }
                        }
                    }

                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.contact_person_first_name));
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                    if (editText[i + 50].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout2);
                        editText[i + 50].requestFocus();
                        edittextinputLayout2.setError(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.contact_person_last_name));
                        editText[i + 50].addTextChangedListener(watcher);
                        break;
                    }


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CONTACTPERSON2")
                        && mandatory == 1) {

                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    edittextinputLayout2 = (TextInputLayout) editText[i + 51].getParentForAccessibility();
                    if (isContactTitle) {
                        edittextinputLayout3 = (TextInputLayout) editText[i + 51 + 5].getParentForAccessibility();
                        if (contactTitleSpinner2.getSelectedItem().toString()
                                .contains("Select")) {
                            validate = false;
                            scrollToSpecificSpinner(contactTitleSpinner2);
                            contactTitleSpinner2.requestFocus();
                            contactTitleSpinner2.setError(getResources().getString(R.string.select_str) + " Title");
                            break;
                        }
                        if (contactTitleSpinner2.getSelectedItem().toString()
                                .contains("OTHERS")) {
                            if (editText[i + 51 + 5].getText().toString().trim().length() == 0) {
                                validate = false;
                                scrollToSpecificEditText(edittextinputLayout3);
                                editText[i + 51 + 5].requestFocus();
                                edittextinputLayout3.setError(getResources().getString(R.string.enter) + " Title");
                                editText[i + 51 + 5].addTextChangedListener(watcher);
                                break;
                            }
                        }
                    }

                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.contact_person_first_name));
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                    if (editText[i + 51].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout2);
                        editText[i + 51].requestFocus();
                        edittextinputLayout2.setError(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.contact_person_first_name));
                        editText[i + 51].addTextChangedListener(watcher);
                        break;
                    }


                } else if ((profileConfig.get(i).getConfigCode().equalsIgnoreCase("PHNO1")
                        && profileConfig.get(i).getMaxLengthNo() > 0)
                        ? mandatory == 1 || mandatory == 0
                        : mandatory == 1 && profileConfig.get(i).getConfigCode().equalsIgnoreCase("PHNO1")) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
//                    if ((profileConfig.get(i).getMaxLengthNo() > 0)
//                            ? editText[i].getText().toString().trim().length() == 0
//                            || editText[i].getText().toString().length() != profileConfig.get(i).getMaxLengthNo()
//                            : editText[i].getText().toString().trim().length() == 0) {
                    if ((profileConfig.get(i).getMaxLengthNo() > 0)
                            ? editText[i].getText().toString().trim().length() == 0
                            || editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                            : editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();
                        edittextinputLayout.setErrorEnabled(true);
                        if (editText[i].getText().toString().trim().length() == 0 && (mandatory == 1))
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        else
                            edittextinputLayout.setError(menuName + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());

                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if ((profileConfig.get(i).getConfigCode().equalsIgnoreCase("PHNO2")
                        && profileConfig.get(i).getMaxLengthNo() > 0)
                        ? mandatory == 1 || mandatory == 0
                        : mandatory == 1 && profileConfig.get(i).getConfigCode().equalsIgnoreCase("PHNO2")) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if ((profileConfig.get(i).getMaxLengthNo() > 0) ?
                            editText[i].getText().toString().trim().length() == 0 ||
                                    editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo() :
                            editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        if (editText[i].getText().toString().trim().length() == 0 && mandatory == 1)
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        else
                            edittextinputLayout.setError(menuName + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());

                        editText[i].addTextChangedListener(watcher);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CHANNEL")
                        && mandatory == 1) {

                    if (channel.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(channel);
                        channel.requestFocus();
                        channel.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CONTRACT")
                        && mandatory == 1) {

                    if (contractSpinner.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(contractSpinner);
                        contractSpinner.requestFocus();
                        contractSpinner.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("SUBCHANNEL")
                        && mandatory == 1) {
                    if (subchannel.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(subchannel);
                        subchannel.requestFocus();
                        subchannel.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("DISTRIBUTOR")
                        && mandatory == 1) {

                    if (distributorSpinner.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(distributorSpinner);
                        distributorSpinner.requestFocus();
                        distributorSpinner.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("ROUTE")
                        && mandatory == 1) {

                    if (route.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(route);
                        route.requestFocus();
                        route.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("LOCATION02")
                        && mandatory == 1) {
                    if (location3.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(location3);
                        location3.requestFocus();
                        location3.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("LOCATION01")
                        && mandatory == 1) {
                    if (location2.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(location2);
                        location2.requestFocus();
                        location2.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("LOCATION")
                        && mandatory == 1) {
                    if (location1.getSelectedItem().toString()
                            .contains("Select")) {
                        validate = false;
                        scrollToSpecificSpinner(location1);
                        location1.requestFocus();
                        location1.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PLAN")
                        && mandatory == 1) {
                    if (!getWhatCheckBoxForWeekNoTicked()) {
                        validate = false;
                        Toast.makeText(getActivity(), getResources().getString(R.string.choose) + getResources().getString(R.string.week),
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else if (!getWhatCheckBoxForDayTicked()) {
                        validate = false;
                        Toast.makeText(getActivity(), getResources().getString(R.string.choose) + getResources().getString(R.string.day),
                                Toast.LENGTH_SHORT).show();
                        break;
                    }


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("LATLONG")
                        && mandatory == 1) {

                    if (latlongtextview.getText().toString().startsWith("0.0")) {
                        validate = false;
                        latlongtextview.requestFocus();
                        scrollview2.smoothScrollTo(0, latlongtextview.getTop());
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.choose_location),
                                Toast.LENGTH_SHORT).show();
                        break;
                    }


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("EMAIL")
                        ) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (mandatory == 1 || editText[i].getText().toString().trim().length() != 0) {
                        if (!isValidEmail(editText[i].getText().toString())) {
                            validate = false;
                            scrollToSpecificEditText(edittextinputLayout);
                            editText[i].requestFocus();

                            edittextinputLayout.setErrorEnabled(true);
                            edittextinputLayout.setError(getResources().getString(R.string.enter_valid_email_id));
                            editText[i].addTextChangedListener(watcher);

                            break;
                        }
                    }
                } else if ((profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("FAX") && profileConfig.get(i).getMaxLengthNo() > 0)
                        ? mandatory == 1 || mandatory == 0
                        : mandatory == 1 && profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("FAX")) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if ((profileConfig.get(i).getMaxLengthNo() > 0)
                            ? editText[i].getText().toString().trim().length() == 0
                            || editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                            : editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        if (editText[i].getText().toString().trim().length() == 0 && mandatory == 1)
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        else
                            edittextinputLayout.setError(menuName + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CREDITPERIOD")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PAYMENTTYPE")
                        && mandatory == 1) {
                    if (paymentType.getSelectedItem().toString().contains("Select")) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        paymentType.requestFocus();
                        paymentType.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("TINNUM")
                        && mandatory == 1) {
                    Commons.print("tin");
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("TINEXPDATE")
                        && mandatory == 1) {
                    Commons.print("tin exp date");
                    if (tinExpDateTextView.getText().toString().isEmpty() || tinExpDateTextView.getText().toString().equalsIgnoreCase("Select Date")) {
                        validate = false;
                        tinExpDateTextView.requestFocus();
                        scrollview2.smoothScrollTo(0, tinExpDateTextView.getTop());
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.select_str) + " " + menuName,
                                Toast.LENGTH_SHORT).show();
                        break;
                    }

                } else if ((profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PINCODE")
                        && profileConfig.get(i).getMaxLengthNo() > 0)
                        ? mandatory == 1 || mandatory == 0
                        : mandatory == 1 && profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PINCODE")) {
                    Commons.print("pin code");
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if ((profileConfig.get(i).getMaxLengthNo() > 0) ?
                            editText[i].getText().toString().trim().length() == 0
                                    || editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                            : editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        if (editText[i].getText().toString().trim().length() == 0 && mandatory == 1)
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        else
                            edittextinputLayout.setError(menuName + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("RFIELD3")
                        && mandatory == 1) {
                    Commons.print("rf");
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;

                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("RFIELD5")
                        && mandatory == 1) {
                    Commons.print("rf5");
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;

                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("RFIELD6")
                        && mandatory == 1) {
                    Commons.print("rf6");
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;

                    }
                } else if ((profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CREDITLIMIT") && profileConfig.get(i).getMaxLengthNo() > 0)
                        ? mandatory == 1 || mandatory == 0
                        : mandatory == 1 && profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("CREDITLIMIT")) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if ((profileConfig.get(i).getMaxLengthNo() > 0) ?
                            editText[i].getText().toString().trim().length() == 0
                                    || editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                            : editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        if (editText[i].getText().toString().trim().length() == 0 && mandatory == 1)
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        else
                            edittextinputLayout.setError(menuName + " Length Must Be " + profileConfig.get(i).getMaxLengthNo());
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase("TAXTYPE") && mandatory == 1) {
                    if (taxTypeSpinner.getSelectedItem().toString().contains("Select")) {
                        scrollToSpecificSpinner(taxTypeSpinner);
                        taxTypeSpinner.requestFocus();
                        validate = false;
                        taxTypeSpinner.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase("CLASS") && mandatory == 1) {

                    if (classSpinner.getSelectedItem().toString().contains("Select")) {
                        scrollToSpecificSpinner(classSpinner);
                        classSpinner.requestFocus();
                        validate = false;
                        classSpinner.setError(getResources().getString(R.string.select_str) + " " + menuName);
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase("PRIORITYPRODUCT") && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) priorityProductAutoCompleteTextView.getParentForAccessibility();
                    if (priorityProductIDList.size() == 0) {
                        if (priorityProductAutoCompleteTextView.getText().toString().trim().length() == 0) {
                            validate = false;
                            scrollToSpecificEditText(edittextinputLayout);
                            priorityProductAutoCompleteTextView.requestFocus();
                            edittextinputLayout.setErrorEnabled(true);
                            edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                            priorityProductAutoCompleteTextView.addTextChangedListener(watcher);
                            break;
                        }
                    }

                } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase("NEARBYRET") && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) nearbyAutoCompleteTextView.getParentForAccessibility();
                    if (nearbyAutoCompleteTextView.getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        nearbyAutoCompleteTextView.requestFocus();
                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        nearbyAutoCompleteTextView.addTextChangedListener(watcher);
                        break;

                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("ATTRIBUTE")) {
                    // mandatory checked individualy for attribute..
                    selectedAttributeLevel = new ArrayList<>();
                    boolean isAdded = true;

                    for (NewOutletAttributeBO attributeBO : mAttributeParentList) {
                        if (bmodel.newOutletAttributeHelper.getmCommonAttributeList().contains(attributeBO.getAttrId())) {

                            NewOutletAttributeBO tempBO = selectedAttribList.get(attributeBO.getAttrId());
                            if (attributeBO.getIsMandatory() == 1) {
                                if (tempBO != null && tempBO.getAttrId() != -1) {
                                    selectedAttributeLevel.add(tempBO);
                                } else {
                                    isAdded = false;
                                    Toast.makeText(getActivity(), getResources().getString(R.string.attribute) + " " + attributeBO.getAttrName() + " is Mandatory",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            } else {
                                if (tempBO != null && tempBO.getAttrId() != -1)
                                    selectedAttributeLevel.add(tempBO);
                            }
                        }
                    }

                    if (issubChannel && isAdded) {
                        if (bmodel.newOutletAttributeHelper.getmAttributeBOListByLocationID() != null)
                            for (NewOutletAttributeBO attributeBo : bmodel.newOutletAttributeHelper.getmAttributeBOListByLocationID().get(((SpinnerBO) subchannel.getSelectedItem()).getId())) {
                                NewOutletAttributeBO tempBO = selectedAttribList.get(attributeBo.getAttrId());
                                if (attributeBo.getIsMandatory() == 1) {
                                    if (tempBO != null && tempBO.getAttrId() != -1) {
                                        selectedAttributeLevel.add(tempBO);
                                    } else {
                                        isAdded = false;
                                        Toast.makeText(getActivity(), getResources().getString(R.string.attribute) + " " + attributeBo.getAttrName() + " is Mandatory",
                                                Toast.LENGTH_SHORT).show();
                                        break;
                                    }

                                } else {
                                    if (tempBO != null && tempBO.getAttrId() != -1)
                                        selectedAttributeLevel.add(tempBO);
                                }

                            }
                    }

                    if (!isAdded) {
                        validate = false;
                        break;
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("GST_NO")
                        && mandatory == 1) {
                    edittextinputLayout = (TextInputLayout) editText[i].getParentForAccessibility();
                    if (editText[i].getText().toString().trim().length() == 0) {
                        validate = false;
                        scrollToSpecificEditText(edittextinputLayout);
                        editText[i].requestFocus();

                        edittextinputLayout.setErrorEnabled(true);
                        edittextinputLayout.setError(getResources().getString(R.string.enter) + " " + menuName);
                        editText[i].addTextChangedListener(watcher);
                        break;
                    }

                }

            }


        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return validate;
    }

    private LinearLayout getEditTextView(final int mNumber, String MName,
                                         int edittexttype, int mandatory) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);

        if (!profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("EMAIL")) {
            //regex
            addLengthFilter(profileConfig.get(mNumber).getRegex());
            checkRegex(profileConfig.get(mNumber).getRegex());
        }

        if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("CONTACTPERSON1")) {
            edittextinputLayout = new TextInputLayout(getActivity());
            edittextinputLayout2 = new TextInputLayout(getActivity());
            edittextinputLayout4 = new TextInputLayout(getActivity());


            if (mandatory == 1) {
                TextView mn_textview = new TextView(getActivity());
                mn_textview.setText("*");
                mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                mn_textview.setTextColor(Color.RED);
                if (Build.VERSION.SDK_INT >= 21) {
                    linearlayout.addView(mn_textview, params6aflollipop);
                } else {
                    linearlayout.addView(mn_textview, params6);
                }
            }

            editText[mNumber + 50 + 5] = new AppCompatEditText(getActivity());
            editText[mNumber + 50 + 5].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber + 50 + 5].setHint("Titile");
            editText[mNumber + 50 + 5].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            editText[mNumber + 50 + 5].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber + 50 + 5].setFilters(stockArr);
                if (inputFilters.size() == 2)
                    editText[mNumber + 50 + 5].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            edittextinputLayout4.addView(editText[mNumber + 50 + 5], params3new);

            if (isContactTitle) {
                contactTitleSpinner1 = new MaterialSpinner(getActivity());
                contactTitleSpinner1.setId(mNumber);
                contactTitleSpinner1.setFloatingLabelText(MName);
                contactTitleSpinner1.setAdapter(contactTitleAdapter);

                final int title_id = mNumber + 50 + 5;

                contactTitleSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


                        NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                        if (tempBo.getListId() == 0) {
                            edittextinputLayout4.setVisibility(View.VISIBLE);
                            editText[title_id].requestFocus();
                        } else if (tempBo.getListId() == -1) {
                            edittextinputLayout4.setVisibility(View.GONE);
                            outlet.setContact1title("0");
                            outlet.setContact1titlelovid("0");
                        } else {
                            edittextinputLayout4.setVisibility(View.GONE);
                            outlet.setContact1title("0");
                            outlet.setContact1titlelovid("" + tempBo.getListId());
                        }

                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    if (outlet.getContact1titlelovid().equals("0") && !outlet.getContact1title().equals("0") && outlet.getContact1title().length() > 0) {
                        contactTitleSpinner1.setSelection(getPosition(profileConfig.get(mNumber).getConfigCode()));
                        editText[mNumber + 50 + 5].setText(outlet.getContact1title());
                    }
                    if (screenMode == VIEW)
                        contactTitleSpinner1.setEnabled(false);
                }
            }

            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            if (isContactTitle)
                editText[mNumber].setHint(getResources().getString(R.string.contact_person_first_name));
            else
                editText[mNumber].setHint(MName + " " + getResources().getString(R.string.contact_person_first_name));

            editText[mNumber + 50] = new AppCompatEditText(getActivity());
            editText[mNumber + 50].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            editText[mNumber + 50].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber + 50].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            if (isContactTitle)
                editText[mNumber + 50].setHint(getResources().getString(R.string.contact_person_last_name));
            else
                editText[mNumber + 50].setHint(MName + " " + getResources().getString(R.string.contact_person_last_name));

            if (screenMode != 0) {
                editText[mNumber].setText(outlet.getContactpersonname());
                editText[mNumber + 50].setText(outlet.getContactpersonnameLastName());
                if (screenMode == VIEW) {
                    editText[mNumber].setFocusable(false);
                    editText[mNumber + 50].setFocusable(false);
                }
            }

            if (isContactTitle) {
                if (Build.VERSION.SDK_INT >= 21) {
                    linearlayout.addView(contactTitleSpinner1, params4aflollipop);
                } else {
                    linearlayout.addView(contactTitleSpinner1, params4new);
                }
                linearlayout.addView(edittextinputLayout4, params3new);
            }

            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                editText[mNumber + 50].setFilters(stockArr);
                if (inputFilters.size() == 2) {
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText[mNumber + 50].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }

            edittextinputLayout.addView(editText[mNumber], params3);
            linearlayout.addView(edittextinputLayout, params3);
            edittextinputLayout2.addView(editText[mNumber + 50], params3);
            linearlayout.addView(edittextinputLayout2, params3);


        } else if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("CONTACTPERSON2")) {

            edittextinputLayout = new TextInputLayout(getActivity());
            edittextinputLayout2 = new TextInputLayout(getActivity());
            edittextinputLayout3 = new TextInputLayout(getActivity());

            if (mandatory == 1) {
                TextView mn_textview = new TextView(getActivity());
                mn_textview.setText("*");
                mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                mn_textview.setTextColor(Color.RED);
                if (Build.VERSION.SDK_INT >= 21) {
                    linearlayout.addView(mn_textview, params6aflollipop);
                } else {
                    linearlayout.addView(mn_textview, params6);
                }

            }

            if (isContactTitle) {
                contactTitleSpinner2 = new MaterialSpinner(getActivity());
                contactTitleSpinner2.setId(mNumber);
                contactTitleSpinner2.setFloatingLabelText(MName);
                contactTitleSpinner2.setAdapter(contactTitleAdapter);

                editText[mNumber + 51 + 5] = new AppCompatEditText(getActivity());
                editText[mNumber + 51 + 5].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                editText[mNumber + 51 + 5].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                editText[mNumber + 51 + 5].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                editText[mNumber + 51 + 5].setHint("Title");

                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[mNumber + 51 + 5].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[mNumber + 51 + 5].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                edittextinputLayout3.addView(editText[mNumber + 51 + 5], params3new);

                final int title_id = mNumber + 51 + 5;

                contactTitleSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


                        NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                        if (tempBo.getListId() == 0) {
                            edittextinputLayout3.setVisibility(View.VISIBLE);
                            editText[title_id].requestFocus();
                        } else if (tempBo.getListId() == -1) {
                            edittextinputLayout3.setVisibility(View.GONE);
                            outlet.setContact2title("0");
                            outlet.setContact2titlelovid("0");
                        } else {
                            edittextinputLayout3.setVisibility(View.GONE);
                            outlet.setContact2title("0");
                            outlet.setContact2titlelovid("" + tempBo.getListId());
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    if (outlet.getContact1titlelovid().equals("0") && !outlet.getContact1title().equals("0") && outlet.getContact1title().length() > 0) {
                        contactTitleSpinner1.setSelection(getPosition(profileConfig.get(mNumber).getConfigCode()));
                        editText[mNumber + 50 + 5].setText(outlet.getContact1title());
                    }
                    if (screenMode == VIEW)
                        contactTitleSpinner2.setEnabled(false);
                }
            }

            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            //for title displaying
            if (isContactTitle)
                editText[mNumber].setHint(getResources().getString(R.string.contact_person_first_name));
            else
                editText[mNumber].setHint(MName + " " + getResources().getString(R.string.contact_person_first_name));

            editText[mNumber + 51] = new AppCompatEditText(getActivity());
            editText[mNumber + 51].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            editText[mNumber + 51].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber + 51].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            //for title displaying
            if (isContactTitle)
                editText[mNumber + 51].setHint(getResources().getString(R.string.contact_person_last_name));
            else
                editText[mNumber + 51].setHint(MName + " " + getResources().getString(R.string.contact_person_last_name));


            if (screenMode != 0) {
                editText[mNumber].setText(outlet.getContactpersonname2());
                editText[mNumber + 51].setText(outlet.getContactpersonname2LastName());
                if (screenMode == VIEW) {
                    editText[mNumber].setFocusable(false);
                    editText[mNumber + 51].setFocusable(false);
                }
            }

            if (isContactTitle) {
                if (Build.VERSION.SDK_INT >= 21) {
                    linearlayout.addView(contactTitleSpinner2, params5aflollipop);
                } else {
                    linearlayout.addView(contactTitleSpinner2, params5new);
                }
                linearlayout.addView(edittextinputLayout3, params3new);
            }

            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                editText[mNumber + 51].setFilters(stockArr);
                if (inputFilters.size() == 2) {
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText[mNumber + 51].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }

            edittextinputLayout.addView(editText[mNumber], params3);
            linearlayout.addView(edittextinputLayout, params3);
            edittextinputLayout2.addView(editText[mNumber + 51], params3);
            linearlayout.addView(edittextinputLayout2, params3);


        } else {
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("CREDITPERIOD")) {
                edittextinputLayout = new TextInputLayout(getActivity());
                if (mandatory == 1) {
                    TextView mn_textview = new TextView(getActivity());
                    mn_textview.setText("*");
                    mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    mn_textview.setTextColor(Color.RED);
                    if (Build.VERSION.SDK_INT >= 21) {
                        linearlayout.addView(mn_textview, paramsaflollipop);
                    } else {
                        linearlayout.addView(mn_textview, params6);
                    }

                }
                editText[mNumber] = new AppCompatEditText(getActivity());
                editText[mNumber].setId(mNumber);
                editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                if (edittexttype == InputType.TYPE_TEXT_VARIATION_PERSON_NAME) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_CLASS_NUMBER) {
                    editText[mNumber].setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_CLASS_TEXT) {
                    editText[mNumber].setInputType(InputType.TYPE_CLASS_TEXT);
                    editText[mNumber].setHint(MName);
                }

                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[mNumber].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                if (screenMode != 0) {
                    editText[mNumber].setText(getValue(profileConfig.get(mNumber).getConfigCode()));
                    if (screenMode == VIEW)
                        editText[mNumber].setFocusable(false);
                }

                editText[mNumber].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!qty.equals("")) {
                            if (Integer.parseInt(qty) > bmodel.configurationMasterHelper.MAX_CREDIT_DAYS) {
                                //Delete the last entered number and reset the qty
                                editText[mNumber].setText(qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0");
                                Toast.makeText(getActivity(), getResources().getString(R.string.max_credit_days_allowed) + " " + bmodel.configurationMasterHelper.MAX_CREDIT_DAYS, Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    }
                });

                if (mandatory != 1) {
                    edittextinputLayout.addView(editText[mNumber], editweightmargin);

                } else {

                    edittextinputLayout.addView(editText[mNumber], weight1);

                }


                linearlayout.addView(edittextinputLayout, commonsparams3);
            } else {
                edittextinputLayout = new TextInputLayout(getActivity());
                if (mandatory == 1) {
                    TextView mn_textview = new TextView(getActivity());
                    mn_textview.setText("*");
                    mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    mn_textview.setTextColor(Color.RED);
                    if (Build.VERSION.SDK_INT >= 21) {
                        linearlayout.addView(mn_textview, paramsaflollipop);
                    } else {
                        linearlayout.addView(mn_textview, params6);
                    }

                }
                editText[mNumber] = new AppCompatEditText(getActivity());
                editText[mNumber].setId(mNumber);
                editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                if (edittexttype == InputType.TYPE_TEXT_VARIATION_PERSON_NAME) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS) {
                    editText[mNumber]
                            .setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_CLASS_NUMBER) {
                    editText[mNumber].setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText[mNumber].setHint(MName);
                } else if (edittexttype == InputType.TYPE_CLASS_TEXT) {
                    editText[mNumber].setInputType(InputType.TYPE_CLASS_TEXT);
                    editText[mNumber].setHint(MName);
                }
                //cmd for not apply inputfilter value for email id
                if (!profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("EMAIL"))
                    if (inputFilters != null && inputFilters.size() > 0) {
                        InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                        stockArr = inputFilters.toArray(stockArr);
                        editText[mNumber].setFilters(stockArr);
                        if (inputFilters.size() == 2)
                            editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                    }
                if (screenMode != 0) {
                    editText[mNumber].setText(getValue(profileConfig.get(mNumber).getConfigCode()));
                    if (screenMode == VIEW)
                        editText[mNumber].setFocusable(false);
                }

                if (mandatory != 1) {
                    edittextinputLayout.addView(editText[mNumber], editweightmargin);

                } else {

                    edittextinputLayout.addView(editText[mNumber], weight1);

                }

                linearlayout.addView(edittextinputLayout, commonsparams3);


            }
        }
        return linearlayout;

    }

    private void addLengthFilter(String regex) {
        inputFilters = new ArrayList<>();
        InputFilter fil = new InputFilter.LengthFilter(25);
        String str = regex;
        if (str != null && !str.isEmpty()) {
            if (str.contains("{") && str.contains("}")) {

                String len = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
                if (!len.isEmpty()) {
                    if (len.contains(",")) {
                        try {
                            fil = new InputFilter.LengthFilter(Integer.parseInt(len.split(",")[1]));
                        } catch (Exception ex) {
                            Commons.printException("regex length split", ex);
                        }
                    } else {
                        fil = new InputFilter.LengthFilter(Integer.parseInt(len));
                    }
                }
            }
        }

        inputFilters.add(fil);
    }

    private void checkRegex(String regex) {
        final String reg;
        String temp;
        try {
            if (regex != null && !regex.isEmpty()) {
                if (regex.contains("<") && regex.contains(">")) {
                    temp = regex.replaceAll("\\<.*?\\>", "");
                } else {
                    temp = regex;
                }
                String[] a = temp.split("\\{");
                if (a.length >= 2)
                    temp = "[" + a[0] + "]{" + a[1];
                else {
                    temp = "[" + temp + "]";
                }
                reg = temp;
                //data.replaceAll("\\(.*?\\)", "()"); //if you want to keep the brackets
                InputFilter filter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            String checkMe = String.valueOf(source.charAt(i));

                            if (!Pattern.compile(reg).matcher(checkMe).matches()) {
                                Log.d("", "invalid");
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

    private String getValue(String code) {
        switch (code) {
            case "STORENAME":
                return outlet.getOutletName();
            case "ADDRESS1":
                return outlet.getAddress();
            case "CONTACTPERSON1":
                return outlet.getContactpersonname();
            case "ADDRESS2":
                return outlet.getAddress2();
            case "ADDRESS3":
                return outlet.getAddress3();
            case "CITY":
                return outlet.getCity();
            case "STATE":
                return outlet.getState();
            case "CONTACTPERSON2":
                return outlet.getContactpersonname2();
            case "PHNO1":
                return outlet.getPhone();
            case "PHNO2":
                return outlet.getPhone2();
            case "PLAN":
                return outlet.getVisitDays();
            case "FAX":
                return outlet.getFax();
            case "EMAIL":
                return outlet.getEmail();
            case "CREDITLIMIT":
                return outlet.getCreditLimit();
            case "TINNUM":
                return outlet.getTinno();
            case "TINEXPDATE":
                return outlet.getTinExpDate();
            case "PINCODE":
                return outlet.getPincode();
            case "RFIELD3":
                return outlet.getRfield3();
            case "RFIELD5":
                return outlet.getRfield5();
            case "RFIELD6":
                return outlet.getRfield6();
            case "CREDITPERIOD":
                return outlet.getCreditDays();
            case "GST_NO":
                return outlet.getGstNum();
        }


        return "";
    }

    private int getPosition(String code) {
        int default_value = 0;
        if (code.equals("CONTACTPERSON1")) {
            for (int i = 0; i < mcontactTitleList.size(); i++) {
                if (mcontactTitleList.get(i).getListId() == Integer.parseInt(outlet.getContact1titlelovid())) {
                    return i;
                }
            }
        } else if (code.equals("CONTACTPERSON2")) {
            for (int i = 0; i < mcontactTitleList.size(); i++) {
                if (mcontactTitleList.get(i).getListId() == Integer.parseInt(outlet.getContact2titlelovid())) {
                    return i;
                }
            }
        } else if (code.equals("DISTRIBUTOR")) {
            for (int i = 0; i < distributortypeAdapter.getCount(); i++) {
                if (distributortypeAdapter.getItem(i).getDId().equals(outlet.getDistid())) {
                    return i;
                }
            }
        } else if (code.equals("LOCATION02")) {

            if (outlet.getLocid() != 0) {
                String[] loc2 = bmodel.mRetailerHelper.getParentLevelName(
                        outlet.getLocid(), true);
                int loc2id = SDUtil.convertToInt((loc2[0]).toString());

                String[] loc3 = bmodel.mRetailerHelper.getParentLevelName(
                        loc2id, true);
                int loc3id = SDUtil.convertToInt((loc3[0]).toString());
                for (int i = 0; i < locationAdapter3.getCount(); i++) {
                    if (locationAdapter3.getItem(i).getLocId() == loc3id) {
                        return i;
                    }
                }

            }


        } else if (code.equals("LOCATION01")) {
            String[] loc2 = bmodel.mRetailerHelper.getParentLevelName(
                    outlet.getLocid(), true);

            int loc2id = SDUtil.convertToInt((loc2[0]).toString());

            for (int i = 0; i < locationAdapter2.getCount(); i++) {
                if (locationAdapter2.getItem(i).getLocId() == loc2id) {
                    return i;
                }
            }

        } else if (code.equals("LOCATION")) {
            for (int i = 0; i < locationAdapter1.getCount(); i++) {
                if (locationAdapter1.getItem(i).getLocId() == outlet.getLocid()) {
                    return i;
                }
            }

        } else if (code.equalsIgnoreCase("CONTRACT")) {
            for (int i = 0; i < mcontractStatusList.size(); i++) {
                if (mcontractStatusList.get(i).getListId() == outlet.getContractStatuslovid()) {
                    return i;
                }
            }
        } else if (code.equals("PAYMENTTYPE")) {
            for (int i = 0; i < mretailertypeMasterList.size(); i++) {
                if (mretailertypeMasterList.get(i).getListId() == Integer.parseInt(outlet.getPayment())) {
                    return i;
                }
            }
        } else if (code.equals("TAXTYPE")) {

            for (int i = 0; i < mTaxTypeList.size(); i++) {
                if (mTaxTypeList.get(i).getListID().equals(outlet.getTaxTypeId())) {
                    return i;
                }
            }
        } else if (code.equalsIgnoreCase("CLASS")) {
            for (int i = 0; i < mClassTypeList.size(); i++) {
                if (mClassTypeList.get(i).getListID().equals(outlet.getClassTypeId())) {
                    return i;
                }
            }
        } else if (code.equals("ROUTE")) {
            for (int i = 0; i < routeAdapter.getCount(); i++) {
                if (routeAdapter.getItem(i).getBeatId() == outlet.getRouteid()) {
                    return i;
                }
            }

        } else if (code.equals("USER")) {
            for (int i = 0; i < mUserList.size(); i++) {
                if (mUserList.get(i).getUserid() == outlet.getUserId()) {
                    return i;
                }
            }

        }


        return default_value;
    }


    @SuppressLint("RestrictedApi")
    private LinearLayout getTextView(int mNumber, String MName,
                                     String textname, int mandatory) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout secondlayout = new LinearLayout(getActivity());
        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory == 1) {
            TextView mn_textview = new TextView(getActivity());
            mn_textview.setText("*");
            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            mn_textview.setTextColor(Color.RED);
            mn_textview.setMinWidth(10);
            firstlayout.addView(mn_textview, weight6);
        }

        textview[mNumber] = new TextView(getActivity());
        textview[mNumber].setText(MName);
        textview[mNumber].setTextColor(Color.BLACK);
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        textview[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        firstlayout.addView(textview[mNumber], params8);
        if (textname.startsWith("0.0")) {//lat long text starts with 0.0
            Commons.print("latlong" + textname);
            latlongtextview.setId(mNumber);
            latlongtextview.setText(textname);
            latlongtextview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            latlongtextview.setTextColor(Color.BLACK);
            latlongtextview.setGravity(Gravity.START);
            latlongtextview.setTypeface(latlongtextview.getTypeface(), Typeface.NORMAL);

            secondlayout.addView(latlongtextview, weight0wrap);

            if (screenMode == VIEW || screenMode == EDIT) {
                String latlng = outlet.getNewOutletlattitude() + "," + outlet.getNewOutletLongitude();
                latlongtextview.setText(latlng);
                if (screenMode == VIEW)
                    latlongtextview.setEnabled(false);


            }

            latlongtextview.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onMapViewClicked();
                }
            });

        } else {
            tinExpDateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
            tinExpDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tinExpDateTextView.setTextColor(Color.BLACK);
            tinExpDateTextView.setId(mNumber);
            tinExpDateTextView.setText(textname);
            tinExpDateTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            tinExpDateTextView.setTextColor(Color.BLACK);//edittext_bottom_border
            tinExpDateTextView.setGravity(Gravity.CENTER);
            tinExpDateTextView.setTypeface(tinExpDateTextView.getTypeface(), Typeface.NORMAL);

            secondlayout.addView(tinExpDateTextView, weight0wrap);

            if (screenMode == VIEW || screenMode == EDIT) {
                String tindate = outlet.getTinExpDate();
                tinExpDateTextView.setText(tindate);
                if (screenMode == VIEW)
                    tinExpDateTextView.setEnabled(false);


            }

            tinExpDateTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getActivity().getSupportFragmentManager(), "tinDatePicker");
                }
            });

        }
        finallayout.addView(firstlayout, params8);
        finallayout.addView(secondlayout, weight2);
        linearlayout.addView(finallayout, params11);
        return linearlayout;

    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (selectedDate.after(Calendar.getInstance())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                tinExpDateTextView.setText(sdf.format(selectedDate.getTime()));
            } else {
                Toast.makeText(getActivity(),
                        "Select future date",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private LinearLayout getNearByRetailerView(String MName, int mandatory) {
        if (bmodel.getNearByRetailers() != null)
            bmodel.getNearByRetailers().clear();

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView mn_textview = new TextView(getActivity());
        if (mandatory == 1) {
            mn_textview.setText("*");
            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            mn_textview.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= 21) {
                layout.addView(mn_textview, params6aflollipop);
            } else {
                layout.addView(mn_textview, params6);
            }
        }

        nearbyAutoCompleteTextView = new AppCompatAutoCompleteTextView(getActivity());
        edittextinputLayout = new TextInputLayout(getActivity());
        nearbyAutoCompleteTextView.setMovementMethod(new ScrollingMovementMethod());
        nearbyAutoCompleteTextView.setSingleLine(true);
        nearbyAutoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        nearbyAutoCompleteTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        if (clickDuration < MAX_CLICK_DURATION) {

                            if (!outlet.getDistid().equals("0")) {
                                mNearbyRetailerList = bmodel.newOutletHelper.getLinkRetailerListByDistributorId().get(SDUtil.convertToInt(outlet.getDistid()));
                                if (mNearbyRetailerList != null && mNearbyRetailerList.isEmpty()) {
                                    for (RetailerMasterBO bo : mNearbyRetailerList) {
                                        bo.setIsNearBy(false);
                                    }
                                    NearByRetailerDialog dialog = new NearByRetailerDialog(getActivity(), bmodel, mNearbyRetailerList, mselectedRetailers);
                                    dialog.show();
                                    dialog.setCancelable(false);
                                } else {
                                    Toast.makeText(getActivity(), "Nearby Retailer's Not Available", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Please select Distributor", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }

                return false;
            }

        });

        if (screenMode == VIEW || screenMode == EDIT) {
            ArrayList<String> nearByRetailers = bmodel.newOutletHelper.downloadNearbyRetailers(retailerId_edit);
            mselectedRetailers = new Vector<>();
            StringBuilder stringBuilder = new StringBuilder();
            if (nearByRetailers != null) {
                for (RetailerMasterBO bo : mNearbyRetailerList) {
                    if (nearByRetailers.contains(bo.getRetailerID())) {
                        mselectedRetailers.add(bo);

                        if (stringBuilder.length() > 0)
                            stringBuilder.append(", ");

                        stringBuilder.append(bo.getRetailerName());
                    }

                }
                nearbyAutoCompleteTextView.setText(stringBuilder.toString());
            }
            if (screenMode == VIEW)
                nearbyAutoCompleteTextView.setEnabled(false);

        }

        if (mandatory != 1) {
            edittextinputLayout.addView(nearbyAutoCompleteTextView, editweightmargin);

        } else {

            edittextinputLayout.addView(nearbyAutoCompleteTextView, weight1);

        }
        layout.addView(edittextinputLayout, commonsparams3);
        return layout;
    }

    private LinearLayout getPriorityProductView(final String mName, int mandatory, final int hasLink) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (mandatory == 1) {
            TextView mn_textview = new TextView(getActivity());
            mn_textview.setText("*");
            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            mn_textview.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= 21) {
                layout.addView(mn_textview, params6aflollipop);
            } else {
                layout.addView(mn_textview, params6);
            }
        }


        priorityProductIDList = new ArrayList<>();

        mPriorityProductList = bmodel.newOutletHelper.downloadPriorityProducts();
        edittextinputLayout = new TextInputLayout(getActivity());
        priorityProductAutoCompleteTextView = new AppCompatAutoCompleteTextView(getActivity());
        priorityProductAutoCompleteTextView.setSingleLine(true);
        priorityProductAutoCompleteTextView.setMovementMethod(new ScrollingMovementMethod());
        priorityProductAutoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        priorityProductAutoCompleteTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        priorityProductAutoCompleteTextView.setHint(mName);
        priorityProductAutoCompleteTextView.setKeyListener(null);

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
                        if (clickDuration < MAX_CLICK_DURATION) {
                            priorityProductAutoCompleteTextView.requestFocus();
                            if (mPriorityProductList != null) {

                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                CustomFragment dialogFragment = new CustomFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("title", mName);
                                bundle.putString("screentitle", mName);
                                bundle.putInt("hasLink", hasLink);
                                dialogFragment.setArguments(bundle);
                                dialogFragment.show(fm, "Sample Fragment");
                                dialogFragment.setCancelable(false);


                            } else {
                                Toast.makeText(getActivity(), "Priority Products Not Available", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                }
                return false;
            }
        });
        if (screenMode == VIEW || screenMode == EDIT) {
            ArrayList<String> products = bmodel.newOutletHelper.downloadPriorityProductsForRetailer(outlet.getRetailerId());
            sb = new StringBuffer();
            if (products != null) {
                for (StandardListBO bo : mPriorityProductList) {
                    if (products.contains(bo.getListID())) {
                        bo.setChecked(true);

                        if (sb.length() > 0)
                            sb.append(", ");

                        sb.append(bo.getListName());
                    }

                }
                if (mPriorityProductList.size() > 0) {
                    priorityProductAutoCompleteTextView.setText(sb.toString());

                } else {
                    priorityProductAutoCompleteTextView.setText("");
                }
            }


            if (screenMode == VIEW)
                priorityProductAutoCompleteTextView.setEnabled(false);

        }
        if (mandatory != 1) {
            edittextinputLayout.addView(priorityProductAutoCompleteTextView, editweightmargin);

        } else {

            edittextinputLayout.addView(priorityProductAutoCompleteTextView, weight1);

        }
        layout.addView(edittextinputLayout, commonsparams3);
        return layout;

    }


    private LinearLayout getSpinnerView(int mNumber, String MName,
                                        String menuCode, int mandatory, int hasLink) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstlayout = new LinearLayout(getActivity());

        if (mandatory == 1) {
            TextView mn_textview = new TextView(getActivity());
            mn_textview.setText("*");
            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            mn_textview.setTextColor(Color.RED);
            mn_textview.setMinWidth(10);
            layout.addView(mn_textview, params2);

        }


        if (menuCode.equals("CHANNEL")) {
            channel = new MaterialSpinner(getActivity());
            channel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            channel.setId(mNumber);
            channel.setFloatingLabelText(MName);
            if (subchannel == null)
                subchannel = new MaterialSpinner(getActivity());
            if (mandatory != 1) {
                firstlayout.addView(channel, params12);
                layout.addView(firstlayout, editweightmargin);
            } else {
                firstlayout.addView(channel, params12);
                layout.addView(firstlayout, params10);
            }
            ArrayAdapter<ChannelBO> channelAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
            channelAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER && bmodel.newOutletHelper.getmSelectedChannelid() > 0)
                channelAdapter.add(new ChannelBO(bmodel.newOutletHelper.getmSelectedChannelid(), bmodel.newOutletHelper.getmSelectedChannelname()));

            else {
                channelAdapter.add(new ChannelBO(0, getActivity().getResources()
                        .getString(R.string.select_str) + " " + MName));
                if (channelMaster != null)
                    for (ChannelBO temp : channelMaster) {
                        channelAdapter.add(temp);
                    }
            }

            channel.setAdapter(channelAdapter);
            channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                    ChannelBO tempBo = (ChannelBO) parent.getSelectedItem();
                    loadsubchannel(tempBo.getChannelId());
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

            if (screenMode == VIEW || screenMode == EDIT) {

                for (int i = 0; i < bmodel.subChannelMasterHelper.getSubChannelMaster().size(); i++) {
                    if (bmodel.subChannelMasterHelper.getSubChannelMaster().get(i).getSubchannelid() == outlet.getSubChannel()) {

                        for (int j = 0; j < channelAdapter.getCount(); j++) {
                            if (channelAdapter.getItem(j).getChannelId() == (bmodel.subChannelMasterHelper.getSubChannelMaster().get(i).getChannelid())) {
                                channel.setSelection(j);
                                break;

                            }
                        }

                        break;
                    }

                }

                if (screenMode == VIEW)
                    channel.setEnabled(false);

            }

        }

        if (menuCode.equalsIgnoreCase("CONTRACT")) {
            contractSpinner = new MaterialSpinner(getActivity());
            contractSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            contractSpinner.setId(mNumber);
            contractSpinner.setFloatingLabelText(MName);
            if (mandatory != 1) {
                firstlayout.addView(contractSpinner, params12);
                layout.addView(firstlayout, editweightmargin);
            } else {
                firstlayout.addView(contractSpinner, params12);
                layout.addView(firstlayout, params10);
            }
            mcontractStatusList = new ArrayList<>();
            mcontractStatusList.add(0, new NewOutletBO(0, getResources().getString(R.string.select_str) + " " + MName));
            mcontractStatusList.addAll(bmodel.newOutletHelper.getContractStatusList());
            ArrayAdapter<NewOutletBO> contractStatusAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontractStatusList);
            contractStatusAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            contractSpinner.setAdapter(contractStatusAdapter);
            contractSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


                    NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                    outlet.setContractStatuslovid(tempBo.getListId());
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

            if (screenMode == VIEW || screenMode == EDIT) {
                contractSpinner.setSelection(getPosition(menuCode));
                if (screenMode == VIEW)
                    contractSpinner.setEnabled(false);
            }

        }
        switch (menuCode) {
            case "SUBCHANNEL":
                if (subchannel == null)
                    subchannel = new MaterialSpinner(getActivity());
                subchannel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                subchannel.setId(mNumber);
                sub_chanel_mname = MName;
                subchannel.setFloatingLabelText(MName);

                if (mandatory != 1) {
                    firstlayout.addView(subchannel, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(subchannel, params12);
                    layout.addView(firstlayout, params10);
                }
                break;
            case "ROUTE":
                route = new MaterialSpinner(getActivity());
                route.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                route.setId(mNumber);
                route.setFloatingLabelText(MName);
                if (mandatory != 1) {
                    firstlayout.addView(route, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(route, params12);
                    layout.addView(firstlayout, params10);
                }


                routeAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item);
                routeAdapter
                        .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                routeAdapter.add(new BeatMasterBO(0, getActivity().getResources()
                        .getString(R.string.select_str) + " " + MName, 0));
                routeMname = MName;

                if (beatMaster != null)
                    if (mUserList != null) {//user spinner available
                        for (BeatMasterBO temp : beatMaster) {

                            String routeCaps = temp.getBeatDescription();
                            temp.setBeatDescription(routeCaps);
                            routeAdapter.add(temp);

                        }
                    } else {// load login user beats
                        for (BeatMasterBO temp : bmodel.beatMasterHealper
                                .getBeatMaster()) {

                            String routeCaps = temp.getBeatDescription();
                            temp.setBeatDescription(routeCaps);
                            routeAdapter.add(temp);

                        }
                    }
                route.setAdapter(routeAdapter);
                route.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {


                        BeatMasterBO tempBo = (BeatMasterBO) parent
                                .getSelectedItem();
                        outlet.setRouteid(tempBo.getBeatId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    route.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        route.setEnabled(false);
                }


                break;
            case "LOCATION":

                location1 = new MaterialSpinner(getActivity());
                location1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location1.setId(mNumber);
                location1.setFloatingLabelText(MName);
                if (mLocationMasterList1 == null) {
                    mLocationMasterList1 = new ArrayList<>();
                }
                mLocationMasterList1.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));


                locationAdapter1 = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList1);
                locationAdapter1.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                location1.setAdapter(locationAdapter1);
                location1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {

                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        outlet.setLocid(tempBo.getLocId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                if (!isLocation1) {
                    if (screenMode == VIEW || screenMode == EDIT) {
                        location1.setSelection(getPosition(menuCode));
                        if (screenMode == VIEW)
                            location1.setEnabled(false);
                    }
                }


                if (mandatory != 1) {
                    firstlayout.addView(location1, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(location1, params12);
                    layout.addView(firstlayout, params10);
                }


                break;
            case "LOCATION01":
                location2 = new MaterialSpinner(getActivity());
                location2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location2.setId(mNumber);
                location2.setFloatingLabelText(MName);
                if (mLocationMasterList2 == null) {
                    mLocationMasterList2 = new ArrayList<>();
                }
                mLocationMasterList2.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));

                locationAdapter2 = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList2);
                locationAdapter2.setDropDownViewResource(R.layout.spinner_bluetext_list_item);

                location2.setAdapter(locationAdapter2);
                location2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {


                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        outlet.setLoc1id(tempBo.getLocId());
                        updateLocationAdapter1(outlet.getLoc1id());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });


                if (mandatory != 1) {
                    firstlayout.addView(location2, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(location2, params12);
                    layout.addView(firstlayout, params10);
                }

                break;
            case "LOCATION02":
                location3 = new MaterialSpinner(getActivity());
                location3.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location3.setId(mNumber);
                location3.setFloatingLabelText(MName);
                if (mLocationMasterList3 == null) {
                    mLocationMasterList3 = new ArrayList<>();
                }
                mLocationMasterList3.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));


                locationAdapter3 = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList3);
                locationAdapter3.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                location3.setAdapter(locationAdapter3);
                location3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {


                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        outlet.setLoc2id(tempBo.getLocId());
                        updateLocationAdapter2(outlet.getLoc2id());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    location3.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        location3.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(location3, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(location3, params12);
                    layout.addView(firstlayout, params10);
                }

                break;
            case "PAYMENTTYPE":

                bmodel.newOutletHelper.loadRetailerType();

                paymentType = new MaterialSpinner(getActivity());
                paymentType.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                paymentType.setId(mNumber);
                paymentType.setFloatingLabelText(MName);
                mretailertypeMasterList = new ArrayList<>();
                mretailertypeMasterList.add(0, new NewOutletBO(0, getResources().getString(R.string.select_str) + " " + MName));
                mretailertypeMasterList.addAll(bmodel.newOutletHelper.getRetailerTypeList());
                Commons.print("Size Payment type : " + bmodel.newOutletHelper.getRetailerTypeList().size());
                ArrayAdapter<NewOutletBO> retailertypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mretailertypeMasterList);
                retailertypeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                paymentType.setAdapter(retailertypeAdapter);
                paymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                        outlet.setPayment(tempBo.getListId() + "");
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    paymentType.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        paymentType.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(paymentType, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(paymentType, params12);
                    layout.addView(firstlayout, params10);
                }

                break;
            case "DISTRIBUTOR":

                distributorSpinner = new MaterialSpinner(getActivity());
                distributorSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                distributorSpinner.setId(mNumber);
                distributorSpinner.setFloatingLabelText(MName);
                bmodel.distributorMasterHelper.downloadDistributorsList();

                mdistributortypeMasterList = new ArrayList<>();
                mdistributortypeMasterList.add(0, new DistributorMasterBO("0", getResources().getString(R.string.select_str) + " " + MName));
                for (DistributorMasterBO tempDis : bmodel.distributorMasterHelper.getDistributors()) {
                    mdistributortypeMasterList.add(tempDis);
                }
                Commons.print("Size Distributor  : " + bmodel.distributorMasterHelper.getDistributors().size());
                distributortypeAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, mdistributortypeMasterList);
                distributortypeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                distributorSpinner.setAdapter(distributortypeAdapter);
                distributorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


                        DistributorMasterBO tempBo = (DistributorMasterBO) parent.getSelectedItem();
                        outlet.setDistid(tempBo.getDId() + "");
                        if (!tempBo.getDId().equals("0") && route != null && routeAdapter != null) {
                            ArrayList<String> beatIds = new ArrayList<>();
                            ArrayList<String> retailerIds = bmodel.newOutletHelper.getRetialerIds(tempBo.getDId());

                            for (int i = 0; i < retailerIds.size(); i++) {
                                String retailerId = retailerIds.get(i);
                                for (RetailerMasterBO bo : bmodel.retailerMaster) {
                                    if (bo.getRetailerID().equals(retailerId))
                                        beatIds.add(bo.getBeatID() + "");
                                }
                            }
                            Set<String> hs = new HashSet<>();
                            hs.addAll(beatIds);
                            beatIds.clear();
                            beatIds.addAll(hs);

                            routeAdapter.clear();
                            routeAdapter.add(new BeatMasterBO(0, getActivity().getResources()
                                    .getString(R.string.select_str) + " " + routeMname, 0));

                            for (String beatId : beatIds) {
                                if (beatMaster != null)
                                    routeAdapter.add(bmodel.beatMasterHealper.getBeatMasterBOByID(SDUtil.convertToInt(beatId)));
                            }

                            route.setAdapter(routeAdapter);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    distributorSpinner.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        distributorSpinner.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(distributorSpinner, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(distributorSpinner, params12);
                    layout.addView(firstlayout, params10);
                }

                break;
            case "TAXTYPE": {
                taxTypeSpinner = new MaterialSpinner(getActivity());
                taxTypeSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                taxTypeSpinner.setId(mNumber);
                taxTypeSpinner.setFloatingLabelText(MName);
                mTaxTypeList = bmodel.newOutletHelper.downloadTaxType();
                if (mTaxTypeList == null)
                    mTaxTypeList = new ArrayList<>();

                StandardListBO standardListBO = new StandardListBO();
                standardListBO.setListID(0 + "");
                standardListBO.setListName("Select " + MName);
                mTaxTypeList.add(0, standardListBO);
                ArrayAdapter<StandardListBO> taxTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mTaxTypeList);
                taxTypeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                taxTypeSpinner.setAdapter(taxTypeAdapter);
                taxTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        StandardListBO standardListBO = (StandardListBO) parent.getSelectedItem();
                        outlet.setTaxTypeId(standardListBO.getListID());

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    taxTypeSpinner.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        taxTypeSpinner.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(taxTypeSpinner, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(taxTypeSpinner, params12);
                    layout.addView(firstlayout, params10);
                }

                break;
            }
            case "PRIORITYPRODUCT": {

                mPriorityProductList = bmodel.newOutletHelper.downloadPriorityProducts();
                StandardListBO standardListBO = new StandardListBO();
                standardListBO.setListID(0 + "");
                standardListBO.setListName("Select " + MName);

                if (mPriorityProductList == null)
                    mPriorityProductList = new ArrayList<>();

                mPriorityProductList.add(0, standardListBO);

                if (hasLink == 0) {
                    MaterialSpinner priorityProductSpinner = new MaterialSpinner(getActivity());
                    priorityProductSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    priorityProductSpinner.setId(mNumber);
                    priorityProductSpinner.setFloatingLabelText(MName);
                    ArrayAdapter<StandardListBO> priorityProductAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item, mPriorityProductList);
                    priorityProductAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                    priorityProductSpinner.setAdapter(priorityProductAdapter);
                    priorityProductSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                            StandardListBO standardListBO = (StandardListBO) parent.getSelectedItem();
                            outlet.setPriorityProductId(standardListBO.getListID());
                            outlet.setPriorityProductLevelId(standardListBO.getListCode());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    if (mandatory != 1) {
                        firstlayout.addView(priorityProductSpinner, params12);
                        layout.addView(firstlayout, editweightmargin);
                    } else {
                        firstlayout.addView(priorityProductSpinner, params12);
                        layout.addView(firstlayout, params10);
                    }
                } else if (hasLink == 1) {
                    Button btn = new Button(getActivity());
                    btn.setText("");
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    layout.addView(btn, commonsparams3);
                }

                break;
            }
            case "CLASS": {
                classSpinner = new MaterialSpinner(getActivity());
                classSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                classSpinner.setId(mNumber);
                classSpinner.setFloatingLabelText(MName);
                mClassTypeList = bmodel.newOutletHelper.downloadClaasType();
                if (mClassTypeList == null)
                    mClassTypeList = new ArrayList<>();

                StandardListBO standardListBO = new StandardListBO();
                standardListBO.setListID(0 + "");
                standardListBO.setListName("Select " + MName);
                mClassTypeList.add(0, standardListBO);
                ArrayAdapter<StandardListBO> classTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mClassTypeList);
                classTypeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                classSpinner.setAdapter(classTypeAdapter);
                classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                        StandardListBO standardListBO = (StandardListBO) parent.getSelectedItem();
                        outlet.setClassTypeId(standardListBO.getListID());

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    classSpinner.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        classSpinner.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(classSpinner, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(classSpinner, params12);
                    layout.addView(firstlayout, params10);
                }
                break;
            }
            case "USER": {
                userSpinner = new MaterialSpinner(getActivity());
                userSpinner.setId(mNumber);
                userSpinner.setFloatingLabelText(MName);

                UserMasterBO bo = new UserMasterBO();
                bo.setUserid(0);
                bo.setUserName("Select " + MName);
                mUserList.add(0, bo);

                ArrayAdapter<UserMasterBO> userAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mUserList);
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                userSpinner.setAdapter(userAdapter);
                //Pre select login user
                for (int i = 0; i < mUserList.size(); i++) {
                    if (mUserList.get(i).getUserid() == bmodel.userMasterHelper.getUserMasterBO().getUserid()) {
                        userSpinner.setSelection(i);
                        break;
                    }
                }
                userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (route != null) {
                            UserMasterBO bo = (UserMasterBO) parent.getSelectedItem();
                            outlet.setUserId(bo.getUserid());
                            updateBeat(bo.getUserid());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if (screenMode == VIEW || screenMode == EDIT) {
                    userSpinner.setSelection(getPosition(menuCode));
                    if (screenMode == VIEW)
                        userSpinner.setEnabled(false);
                }

                if (mandatory != 1) {
                    firstlayout.addView(userSpinner, params12);
                    layout.addView(firstlayout, editweightmargin);
                } else {
                    firstlayout.addView(userSpinner, params12);
                    layout.addView(firstlayout, params10);
                }
                break;
            }
        }

        return layout;

    }

    private void updateBeat(int userid) {

        routeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        routeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeAdapter.add(new BeatMasterBO(0, getActivity().getResources()
                .getString(R.string.select_str) + " " + routeMname, 0));

        if (beatMaster != null)
            for (BeatMasterBO temp : beatMaster) {

                if (userid == temp.getUserId()) {

                    String routeCaps = temp.getBeatDescription();
                    temp.setBeatDescription(routeCaps);
                    routeAdapter.add(temp);
                }

            }

        route.setAdapter(routeAdapter);


    }

    //To create layout for Retailer Attribute
    private LinearLayout addAttributeView(final String MName, int mandatory, int flag) {
        //flag=0 - add common atrributes
        // flag==1 - add channel attributes
        boolean isCommon = false, isFromChannel = false;
        if (flag == 0)
            isCommon = true;
        else if (flag == 1)
            isFromChannel = true;


        ArrayList<Integer> mCommonAttributeList = null;
        ArrayList<Integer> mChannelAttributeList = null;

        LinearLayout parentLayout = null;
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        innerParams.setMargins(0, 0, 10, 0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(3, 0, 0, 0);

        if (isCommon) {
            parentLayout = new LinearLayout(getActivity());
            parentLayout.setTag("attributeLayout");
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout titleLayout = new LinearLayout(getActivity());
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);

            if (mandatory == 1) {
                TextView mn_textview = new TextView(getActivity());
                mn_textview.setText("*");
                mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                mn_textview.setTextColor(Color.RED);
                titleLayout.addView(mn_textview, titleParams);
            }
            TextView titleTV = new TextView(getActivity());
            titleTV.setText(MName);
            titleTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_medium));
            titleTV.setTextColor(Color.BLACK);
            titleLayout.addView(titleTV, titleParams);

            parentLayout.addView(titleLayout, LLParams);

            mCommonAttributeList = new ArrayList<>();
            if (bmodel.newOutletAttributeHelper.getmCommonAttributeList() != null)
                mCommonAttributeList.addAll(bmodel.newOutletAttributeHelper.getmCommonAttributeList());

        } else if (isFromChannel) {
            if (getView() != null) {
                parentLayout = (LinearLayout) getView().findViewWithTag("attributeLayout");
                if (parentLayout != null) {
                    for (int i = 0; i < parentLayout.getChildCount(); i++) {
                        if (parentLayout.getChildAt(i).getTag() != null && parentLayout.getChildAt(i).getTag().equals("channel"))
                            parentLayout.removeViewAt(i);

                    }
                }
                mChannelAttributeList = new ArrayList<>();
                if (mAttributeListByChannelID.get(((SpinnerBO) subchannel.getSelectedItem()).getId()) != null)
                    mChannelAttributeList.addAll(mAttributeListByChannelID.get(((SpinnerBO) subchannel.getSelectedItem()).getId()));
            }
        }


        spinnerHashMap = new HashMap<>();
        spinnerAdapterMap = new HashMap<>();
        int rowCount = mAttributeParentList.size();
        selectedAttribList = new HashMap<>();
        for (int i = 0; i < rowCount; i++) {
            NewOutletAttributeBO parentBO = mAttributeParentList.get(i);
            if ((isCommon && mCommonAttributeList.contains(parentBO.getAttrId()))
                    || (isFromChannel && (mChannelAttributeList != null && mChannelAttributeList.contains(parentBO.getAttrId())))) {

                LinearLayout layout = new LinearLayout(getActivity());
                if (isFromChannel)
                    layout.setTag("channel");

                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.CENTER_VERTICAL);
                layout.setWeightSum(3f);
                layout.setLayoutParams(LLParams);
                final String attribName = parentBO.getAttrName();
                TextView mn_textview = new TextView(getActivity());
                mn_textview.setText(attribName);
                mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                mn_textview.setTextColor(Color.BLACK);
                mn_textview.setLayoutParams(paramsAttrib);
                layout.addView(mn_textview);

                final int parentAttrId = parentBO.getAttrId();
                final int columnCount = getLevel(parentAttrId);
                MaterialSpinner spinner;
                LinearLayout innerLL = new LinearLayout(getActivity());
                innerLL.setOrientation(LinearLayout.VERTICAL);
                innerLL.setLayoutParams(paramsAttribSpinner);
                LinearLayout innerHL = new LinearLayout(getActivity());
                innerHL.setWeightSum(2);
                innerHL.setLayoutParams(LLParams);
                innerHL.setOrientation(LinearLayout.HORIZONTAL);
                boolean isAdded = false;
                for (int j = 0; j < columnCount; j++) {
                    isAdded = false;
                    final ArrayList<NewOutletAttributeBO> attrbList;
                    final int index = j;
                    if (j == 0) {
                        spinner = new MaterialSpinner(getActivity());
                        attrbList = new ArrayList<>();
                        attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                .getString(R.string.select_str) + " " + attribName));
                        attrbList.addAll(attribMap.get(attribName));

                        final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item, attrbList);
                        spinner.setAdapter(arrayAdapter);
                        spinner.setSelection(0);
                        arrayAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                        spinner.setLayoutParams(innerParams);
                        innerHL.addView(spinner);
                        spinnerAdapterMap.put(attribName + index, arrayAdapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedAttribList.remove(parentAttrId);
                                selectedAttribList.put(parentAttrId, attrbList.get(position));


                                loadAttributeSpinner(attribName + parentAttrId, attrbList.get(position).getAttrId(), MName);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    } else {
                        spinner = new MaterialSpinner(getActivity());
                        attrbList = new ArrayList<>();
                        attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                .getString(R.string.select_str) + " " + attribName));

                        final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item, attrbList);
                        spinner.setAdapter(arrayAdapter);
                        spinner.setSelection(0);
                        arrayAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                        spinner.setLayoutParams(innerParams);
                        innerHL.addView(spinner);
                        spinnerAdapterMap.put(attribName + index, arrayAdapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedAttribList.remove(parentAttrId);
                                selectedAttribList.put(parentAttrId, attrbList.get(position));


                                if (index < columnCount) {
                                    loadAttributeSpinner(attribName + parentAttrId, attrbList.get(position).getAttrId(), MName);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                    spinnerHashMap.put(attribName + index, spinner);
                    if ((j + 1) % 2 == 0) {
                        innerLL.addView(innerHL);
                        innerHL = new LinearLayout(getActivity());
                        innerHL.setWeightSum(2);
                        innerHL.setLayoutParams(LLParams);
                        innerHL.setOrientation(LinearLayout.HORIZONTAL);
                        isAdded = true;
                    }
                }
                if (!isAdded) {
                    innerLL.addView(innerHL);
                }

                layout.addView(innerLL);
                parentLayout.addView(layout);
            }
        }

        return parentLayout;
    }

    private int getLevel(int attrId) {
        int count = 0;
        ArrayList<NewOutletAttributeBO> arrayList = bmodel.newOutletAttributeHelper.getAttributeList();
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

    //To load spinner values for Retailer Attribute based on it's previous spinner selection
    private void loadAttributeSpinner(String key, int attribId, final String mName) {
        MaterialSpinner spinner = spinnerHashMap.get(key);
        ArrayAdapter<NewOutletAttributeBO> adapter = spinnerAdapterMap.get(key);
        if (spinner != null && adapter != null) {
            ArrayList<NewOutletAttributeBO> arrayList = new ArrayList<>();
            arrayList.add(new NewOutletAttributeBO(-1, getActivity().getResources()
                    .getString(R.string.select_str) + " " + mName));
            for (NewOutletAttributeBO attributeBO : bmodel.newOutletAttributeHelper.getAttributeList()) {
                if (attribId == attributeBO.getParentId()) {
                    arrayList.add(attributeBO);
                }
            }

            adapter.clear();
            adapter.addAll(arrayList);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; getActivity() adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_new_retailer, menu);
        if (!bmodel.configurationMasterHelper.IS_NEWOUTLET_IMAGETYPE) {
            menu.findItem(R.id.menu_capture).setVisible(false);
        }
        if (!bmodel.configurationMasterHelper
                .downloadFloatingSurveyConfig(MENU_NEW_RETAILER))
            menu.findItem(R.id.menu_survey).setVisible(false);

        menu.findItem(R.id.menu_oppr).setVisible(bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR);
        menu.findItem(R.id.menu_order).setVisible(bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER);

        if (screenMode == EDIT)
            menu.findItem(R.id.menu_capture).setVisible(false);
        else if (screenMode == VIEW) {
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
            if (surveyHelperNew.isSurveyAvaliable(bmodel.newOutletHelper.getId()))
                showAlert(getResources().getString(R.string.are_you_sure_to_close_without_savingthe_data));
            else if (screenMode == VIEW || screenMode == EDIT || screenMode == CREATE_FRM_EDT_SCREEN) {
                startActivity(new Intent(getActivity(),
                        HomeScreenActivity.class).putExtra("menuCode", "MENU_NEWRET_EDT"));
                getActivity().finish();
            } else {
                startActivity(new Intent(getActivity(),
                        HomeScreenActivity.class));
                getActivity().finish();
            }

            return true;
        }
        if (i == R.id.menu_survey) {
            boolean validated = validateProfile();
            if (validated) {
                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_NEW_RETAILER)) {
                    surveyHelperNew.setFromHomeScreen(true);
                    surveyHelperNew.downloadModuleId("NEW_RETAILER");
                    surveyHelperNew.downloadQuestionDetails(MENU_NEW_RETAILER);
                    if (screenMode == EDIT || screenMode == VIEW) {
                        surveyHelperNew.loadNewRetailerSurveyAnswers(outlet.getRetailerId());// passing selected retailerid
                        bmodel.newOutletHelper.setRetailerId_edit(outlet.getRetailerId());
                    } else {
                        surveyHelperNew.loadNewRetailerSurveyAnswers(bmodel.newOutletHelper.getId());//passing generated id
                    }

                    Intent intent = new Intent(getActivity(),
                            SurveyActivityNew.class);
                    intent.putExtra("menucode", MENU_NEW_RETAILER);
                    intent.putExtra("screenMode", screenMode);
                    startActivity(intent);
                }


            }
            return true;
        } else if (i == R.id.menu_capture) {// AlertDialog.Builder builder;
            //Dont allow if Fun57 is enabled and mandatory,
            //Generally check for location and show toast if no location found.

            boolean isLatLongMenuAvail = false;
            for (int conf = 0; conf < profileConfig.size(); conf++) {
                if (profileConfig.get(conf).getConfigCode().equalsIgnoreCase("LATLONG") &&
                        profileConfig.get(conf).getMandatory() == 1) {
                    isLatLongMenuAvail = true;
                    break;
                }
            }

            if (!isLatLongMenuAvail && bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE && (LocationUtil.latitude == 0 || LocationUtil.longitude == 0)
                    || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl != 0 && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)) {

                Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                return true;
            } else {
                if (LocationUtil.latitude == 0 || LocationUtil.longitude == 0) {
                    Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                } else {
                    if (!isLatLongMenuAvail && (outlet.getNewOutletlattitude() == 0 || outlet.getNewOutletLongitude() == 0)) {
                        lattitude = LocationUtil.latitude;
                        longitude = LocationUtil.longitude;
                        outlet.setNewOutletlattitude(lattitude);
                        outlet.setNewOutletLongitude(longitude);
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(null)
                        .setSingleChoiceItems(mImageTypeAdapter, 0,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int item) {
                                        if (bmodel.synchronizationHelper
                                                .isExternalStorageAvailable()) {
                                            PHOTO_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                                    + "/"
                                                    + DataMembers.photoFolderName
                                                    + "/";
                                            Commons.print(TAG + ":Photo path :" + PHOTO_PATH);

                                            int ImageId = bmodel.newOutletHelper
                                                    .getImageTypeList()
                                                    .get(item).getListId();
                                            imageName = moduleName + uID + "_"
                                                    + ImageId + "_img.jpg";
                                            String fnameStarts = moduleName + uID
                                                    + "_" + ImageId;
                                            Commons.print(TAG + ",FName Starts :"
                                                    + fnameStarts);
                                            boolean nfiles_there = bmodel.checkForNFilesInFolder(
                                                    PHOTO_PATH, 1,
                                                    fnameStarts);

                                            if (nfiles_there) {
                                                showFileDeleteAlert(fnameStarts);
                                            } else {

                                                captureCustom();
                                                outlet.ImageId.add(ImageId);
                                                outlet.ImageName.add(imageName);
                                                dialog.dismiss();
                                                return;

                                            }

                                        } else {
                                            Toast.makeText(getActivity(),
                                                    getResources().getString(R.string.external_storage_not_available),
                                                    Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                        }
                                        dialog.dismiss();
                                    }

                                });
                bmodel.applyAlertDialogTheme(builder);
                return true;
            }
        } else if (i == R.id.menu_order) {
            bmodel.configurationMasterHelper.downloadProductDetailsList();
            /* Settign color **/
            bmodel.configurationMasterHelper.downloadFilterList();
            bmodel.productHelper.updateProductColor();
            bmodel.productHelper.downloadInStoreLocations();

            Intent intent = new Intent(getActivity(),
                    OrderNewOutlet.class);
            intent.putExtra("OrderFlag", "Nothing");
            intent.putExtra("ScreenCode",
                    ConfigurationMasterHelper.MENU_ORDER);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            return true;
        } else if (i == R.id.menu_oppr) {
            bmodel.configurationMasterHelper.downloadProductDetailsList();

            bmodel.productHelper.downloadTaggedProducts(MENU_NEW_RETAILER);
            bmodel.productHelper.downloadCompetitorProducts("MENU_STK_ORD");
            bmodel.productHelper.downloadCompetitorTaggedProducts(MENU_NEW_RETAILER);

            /* Settign color **/
            bmodel.configurationMasterHelper.downloadFilterList();
            bmodel.productHelper.updateProductColor();
            bmodel.productHelper.downloadInStoreLocations();

            Intent intent = new Intent(getActivity(),
                    OpportunityNewOutlet.class);
            intent.putExtra("OrderFlag", "Nothing");
            intent.putExtra("ScreenCode",
                    ConfigurationMasterHelper.MENU_ORDER);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            return true;
        }
        return false;
    }

    protected void onCreateDialogNew() {
        AlertDialog.Builder builderGPS = new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setTitle(getResources().getString(R.string.enable_gps))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                Intent myIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                            }
                        });
        bmodel.applyAlertDialogTheme(builderGPS);
    }

    private void setValues() {

        try {
            int size = profileConfig.size();
            boolean isVisitPlanAvailable = false;
            boolean isStoreName = false;
            boolean isTinExpDate = false;
            boolean isAddress1 = false;
            boolean isAddress2 = false;
            boolean isCity = false;
            boolean isState = false;
            boolean isContactPerson1 = false;
            boolean isContactPerson2 = false;
            boolean isPhoneNo1 = false;
            boolean isPhoneNo2 = false;
            boolean isCreditLimit = false;
            boolean isRPType = false;
            boolean isContractStatus = false;
            boolean isEmail = false;
            boolean isFaxNo = false;
            boolean tinno = false;
            boolean rfield3 = false;
            boolean rfield5 = false;
            boolean rfield6 = false;
            boolean pinno = false;
            boolean isCreditDays = false;


            for (int i = 0; i < size; i++) {

                String configCode = profileConfig.get(i).getConfigCode();

                if (configCode.equalsIgnoreCase("STORENAME")) {
                    isStoreName = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setOutletName("");
                    } else {
                        outlet.setOutletName(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equalsIgnoreCase("ADDRESS1")) {
                    isAddress1 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setAddress("");
                    } else {
                        outlet.setAddress(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equalsIgnoreCase("CONTACTPERSON1")) {
                    isContactPerson1 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setContactpersonname("");
                        outlet.setContactpersonnameLastName("");
                    } else {
                        outlet.setContactpersonname(SDUtil
                                .removeQuotes(bmodel.validateInput(editText[i].getText().toString())));
                        outlet.setContactpersonnameLastName(SDUtil.removeQuotes(bmodel.validateInput(editText[i + 50].getText().toString())));
                    }
                    if (isContactTitle) {
                        if (contactTitleSpinner1.getSelectedItem().toString()
                                .contains("OTHERS")) {
                            outlet.setContact1titlelovid("0");
                            outlet.setContact1title(SDUtil.removeQuotes(bmodel.validateInput(editText[i + 50 + 5].getText().toString())));

                        }
                        if (contactTitleSpinner1.getSelectedItem().toString()
                                .contains("Select")) {
                            outlet.setContact1title("0");
                            outlet.setContact1titlelovid("0");

                        }
                    }
                } else if (configCode.equalsIgnoreCase("ADDRESS2")) {
                    isAddress2 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setAddress2("");
                    } else {
                        outlet.setAddress2(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equalsIgnoreCase("ADDRESS3")) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setAddress3("");
                    } else {
                        outlet.setAddress3(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equalsIgnoreCase("CITY")) {
                    isCity = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setCity("");
                    } else {
                        outlet.setCity(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equalsIgnoreCase("STATE")) {
                    isState = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setState("");
                    } else {
                        outlet.setState(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equalsIgnoreCase("CONTACTPERSON2")) {
                    isContactPerson2 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setContactpersonname2("");
                        outlet.setContactpersonname2LastName("");
                    } else {
                        outlet.setContactpersonname2(SDUtil
                                .removeQuotes(bmodel.validateInput(editText[i].getText().toString())));
                        outlet.setContactpersonname2LastName(SDUtil
                                .removeQuotes(bmodel.validateInput(editText[i + 51].getText().toString())));
                    }
                    if (isContactTitle) {
                        if (contactTitleSpinner2.getSelectedItem().toString()
                                .contains("OTHERS")) {
                            outlet.setContact2titlelovid("0");
                            outlet.setContact2title(SDUtil.removeQuotes(bmodel.validateInput(editText[i + 51 + 5].getText().toString())));
                        }
                        if (contactTitleSpinner2.getSelectedItem().toString()
                                .contains("Select")) {
                            outlet.setContact2title("0");
                            outlet.setContact2titlelovid("0");

                        }
                    } else {
                        outlet.setContact2title("0");
                        outlet.setContact2titlelovid("0");
                    }
                } else if (configCode.equalsIgnoreCase("PHNO1")) {
                    isPhoneNo1 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setPhone("");
                    } else {
                        outlet.setPhone(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("PHNO2")) {
                    isPhoneNo2 = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setPhone2("");
                    } else {
                        outlet.setPhone2(bmodel.validateInput(editText[i].getText().toString()));
                    }

                } else if (configCode.equalsIgnoreCase("CHANNEL")) {
                    ChannelBO cBo = (ChannelBO) channel.getSelectedItem();
                    if (channelMaster != null)
                        if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER && bmodel.newOutletHelper.getmSelectedChannelid() > 0)
                            outlet.setSubChannel(cBo.getChannelId());
                        else
                            outlet.setChannel(cBo.getChannelId());
                } else if (configCode.equalsIgnoreCase("SUBCHANNEL")) {
                    if (channelMaster != null)
                        outlet.setSubChannel(((SpinnerBO) subchannel
                                .getSelectedItem()).getId());
                } else if (configCode.equalsIgnoreCase("DISTRIBUTOR")) {
                    DistributorMasterBO distBo = (DistributorMasterBO) distributorSpinner.getSelectedItem();
                    if (mdistributortypeMasterList != null)
                        outlet.setDistid(distBo.getDId());
                } else if (configCode.equalsIgnoreCase("LOCATION")) {
                    if (mLocationMasterList1 != null) {
                        if (mLocationMasterList1.size() > 0) {
                            try {
                                outlet.setLocid(((LocationBO) location1
                                        .getSelectedItem()).getLocId());
                            } catch (Exception e) {
                                outlet.setLocid(0);
                                Commons.printException("" + e);
                            }
                        }
                    }
                } else if (configCode.equalsIgnoreCase("CONTRACT")) {
                    isContractStatus = true;
                    if (mcontractStatusList != null) {
                        if (mcontractStatusList.size() > 0) {
                            try {
                                outlet.setContractStatuslovid(((NewOutletBO) contractSpinner
                                        .getSelectedItem()).getListId());
                            } catch (Exception e) {
                                outlet.setContractStatuslovid(0);
                                Commons.printException("" + e);
                            }
                        }
                    }
                } else if (configCode.equalsIgnoreCase("PLAN")) {

                    getWhatCheckBoxForWeekNoTicked();
                    getWhatCheckBoxForDayTicked();

                    isVisitPlanAvailable = true;
                    outlet.setVisitDays(checkedDaysAll);
                    outlet.setWeekNo(weekNoStr);
                } else if (configCode.equalsIgnoreCase("LATLONG")) {
                    outlet.setNewOutletlattitude(lattitude);
                    outlet.setNewOutletLongitude(longitude);
                } else if (configCode.equalsIgnoreCase("FAX")) {
                    isFaxNo = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setFax("0");
                    } else {
                        outlet.setFax(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("EMAIL")) {

                    isEmail = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setEmail("");
                    } else {
                        outlet.setEmail(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("PAYMENTTYPE")) {

                    isRPType = true;

                    NewOutletBO tempBo = (NewOutletBO) paymentType.getSelectedItem();
                    if (mretailertypeMasterList != null) {
                        if (mretailertypeMasterList.size() > 0) {
                            try {
                                outlet.setPayment(tempBo.getListId() + "");
                                Commons.print("ListId : " + tempBo.getListId());
                            } catch (Exception e) {
                                outlet.setPayment("0");
                                Commons.printException("" + e);
                            }
                        }
                    }
                } else if (configCode.equalsIgnoreCase("CREDITLIMIT")) {

                    isCreditLimit = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setCreditLimit("0");
                    } else {
                        outlet.setCreditLimit(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("TINNUM")) {

                    tinno = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setTinno("0");
                    } else {
                        outlet.setTinno(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("TINEXPDATE")) {

                    isTinExpDate = true;

                    if (tinExpDateTextView.getText().toString().equalsIgnoreCase("Select Date") || TextUtils.isEmpty(bmodel.validateInput(tinExpDateTextView.getText().toString()))) {
                        outlet.setTinExpDate("");
                    } else {
                        outlet.setTinExpDate(bmodel.validateInput(tinExpDateTextView.getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("PINCODE")) {

                    pinno = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setPincode("");
                    } else {
                        outlet.setPincode(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("RFIELD3")) {

                    rfield3 = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setRfield3("0");
                    } else {
                        outlet.setRfield3(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("RFIELD5")) {

                    rfield5 = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setRfield5("0");
                    } else {
                        outlet.setRfield5(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("RFIELD6")) {

                    rfield6 = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setRfield6("0");
                    } else {
                        outlet.setRfield6(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("TAXTYPE")) {
                    outlet.setTaxTypeId(((StandardListBO) taxTypeSpinner.getSelectedItem()).getListID());
                } else if (configCode.equalsIgnoreCase("PRIORITYPRODUCT")) {
                    outlet.setPriorityProductList(priorityProductIDList);

                } else if (configCode.equalsIgnoreCase("CREDITPERIOD")) {
                    isCreditDays = true;
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setCreditDays(null);
                    } else {
                        outlet.setCreditDays(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("ATTRIBUTE")) {
                    isAttribute = true;
                    outlet.setAttributeList(selectedAttributeLevel);
                } else if (configCode.equalsIgnoreCase("ROUTE")) {
                    if (beatMaster != null) {
                        if (beatMaster.size() > 0) {
                            try {
                                outlet.setRouteid(((BeatMasterBO) route
                                        .getSelectedItem()).getBeatId());
                            } catch (Exception e) {
                                outlet.setRouteid(0);
                                Commons.printException("" + e);
                            }
                        }
                    }
                } else if (configCode.equalsIgnoreCase("GST_NO")) {

                    tinno = true;

                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        outlet.setGstNum("");
                    } else {
                        outlet.setGstNum(bmodel.validateInput(editText[i].getText().toString()));
                    }
                } else if (configCode.equalsIgnoreCase("INSEZ")) {

                    if (inSEZcheckBox.isChecked()) {
                        outlet.setIsSEZ(1);
                    } else {
                        outlet.setIsSEZ(0);
                    }
                }


            }
            if (!isVisitPlanAvailable) {
                outlet.setVisitDays("");
                outlet.setWeekNo("");
            }

            if (!isStoreName) {
                outlet.setOutletName("");
            }
            if (!isAddress1) {
                outlet.setAddress("");
            }
            if (!isAddress2) {
                outlet.setAddress2("");
            }
            if (!isContactPerson1) {
                outlet.setContactpersonname("");
                outlet.setContactpersonnameLastName("");

            }
            if (!isContactPerson2) {
                outlet.setContactpersonname2("");
                outlet.setContactpersonname2LastName("");
            }

            if (!isContactTitle) {
                outlet.setContact1titlelovid("0");
                outlet.setContact1title("0");
                outlet.setContact2titlelovid("0");
                outlet.setContact2title("0");
            }
            if (!isPhoneNo1) {
                outlet.setPhone("");
            }
            if (!isPhoneNo2) {
                outlet.setPhone2("");
            }
            if (!isContractStatus) {
                outlet.setContractStatuslovid(0);
            }
            if (!isCreditLimit) {
                outlet.setCreditLimit("0");
            }
            if (!isRPType) {
                outlet.setPayment("0");
            }
            if (!isEmail) {
                outlet.setEmail("");
            }
            if (!isFaxNo) {
                outlet.setFax("0");
            }
            if (!tinno) {
                outlet.setTinno("0");
            }
            if (!isTinExpDate) {
                outlet.setTinExpDate("");
            }
            if (!rfield3) {
                outlet.setRfield3("0");
            }
            if (!rfield5) {
                outlet.setRfield5("0");
            }
            if (!rfield6) {
                outlet.setRfield6("0");
            }
            if (!pinno) {
                outlet.setPincode("");
            }
            if (!isCity)
                outlet.setCity("");
            if (!isState)
                outlet.setState("");

            /*if (!isCreditDays)
                outlet.setCreditDays("-1");*/

            if (!isAttribute)
                outlet.setAttributeList(new ArrayList<NewOutletAttributeBO>());

            if (isRoute)
                outlet.setMarket(bmodel.beatMasterHealper.getBeatsId(beatName));
            else
                outlet.setMarket("");


            outlet.setImageId(outlet.ImageId);
            outlet.setImageName(outlet.ImageName);


        } catch (Exception e) {
            Commons.printException("" + e);
        }

        bmodel.newOutletHelper.setNewoutlet(outlet);
        bmodel.setNearByRetailers(mselectedRetailers);

    }

    public void captureCustom() {
        try {

            Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra("quality", 40);
            String _path = PHOTO_PATH + "/" + imageName;
            intent.putExtra("path", _path);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + 1
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.synchronizationHelper.deleteFiles(PHOTO_PATH,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String _path = PHOTO_PATH + "/" + imageName;
                        intent.putExtra("path", _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    class SaveNewOutlet extends AsyncTask<String, Void, Boolean> {

        private String mParam;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            mParam = params[0];

            if (screenMode == EDIT)
                return bmodel.newOutletHelper.saveNewOutlet(true);
            else
                return bmodel.newOutletHelper.saveNewOutlet(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            alertDialog.dismiss();


            if (result) {

                if (mParam.equalsIgnoreCase("1")) {

                    new UploadNewOutlet().execute("");
                } else {

                    bmodel.downloadRetailerMaster();

                    if (bmodel.configurationMasterHelper.CALC_QDVP3)
                        bmodel.updateSurveyScoreHistoryRetailerWise();

                    bmodel.downloadVisit_Actual_Achieved();

                    showToast(getResources().getString(
                            R.string.saved_successfully));
                    if (bmodel.newOutletHelper.getOrderedProductList().size() > 0)
                        bmodel.newOutletHelper.getOrderedProductList().clear();
                    if (screenMode == VIEW || screenMode == EDIT || screenMode == CREATE_FRM_EDT_SCREEN) {
                        startActivity(new Intent(getActivity(),
                                HomeScreenActivity.class).putExtra("menuCode", "MENU_NEWRET_EDT"));
//                        getActivity().finish();
                    } else {
                        startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                        getActivity().finish();
                    }

                }


            } else {
                bmodel = (BusinessModel) getActivity().getApplicationContext();
                bmodel.showAlert(
                        "Error: "
                                + getResources().getString(
                                R.string.new_store_infn_not_saved), 0);
            }

        }

    }

    class UploadNewOutlet extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder,
                    getResources().getString(R.string.uploading_new_store));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            if (bmodel.isOnline()) {
                String rid = bmodel.synchronizationHelper.uploadNewOutlet(getHandler());

                if (rid.equals("-1")) {
                    getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
                    return false;
                } else if (rid.equals("2")) {
                    getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_URL_NOT_CONFIGURED);
                    return false;
                } else if (!rid.equals("")) {

                    if (bmodel.configurationMasterHelper.IS_NEARBY_RETAILER) {
                        bmodel.saveNearByRetailers(rid);
                    }

                    bmodel.synchronizationHelper.downloadNewRetailerUrl();

                    ArrayList<String> newRetailerUrlList = bmodel.synchronizationHelper.getNewRetailerDownloadurlList();

                    if (newRetailerUrlList.size() > 0) {
                        bmodel.synchronizationHelper.downloadNewRetailerFromUrl(rid);
                        return true;
                    } else {
                        getHandler().sendEmptyMessage(
                                DataMembers.RETAILER_DOWNLOAD_FAILED);
                        return false;
                    }


                } else {
                    getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                    return false;
                }
            } else {
                getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_NO_INTERNET);
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_NEW_OUTLET_SAVED);
            }


        }

    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataMembers.NOTIFY_NEW_OUTLET_SAVED:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.new_store_has_been_saved),
                            DataMembers.NOTIFY_NEW_OUTLET_SAVED);
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getActivity().getApplicationContext();
                    bmodel.showAlert(
                            "Error: "
                                    + getResources().getString(
                                    R.string.new_store_infn_not_saved), 0);
                    break;
                case DataMembers.SAVENEWOUTLET:
                    alertDialog.dismiss();
                    showToast(getResources().getString(
                            R.string.saved_successfully));
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                    getActivity().finish();
                case DataMembers.RETAILER_DOWNLOAD_FAILED:
                    alertDialog.dismiss();
                    showToast(getResources().getString(
                            R.string.data_not_downloaded));
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                    getActivity().finish();

                    break;
                case DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL:
                    alertDialog.dismiss();
                    showToast(getResources().getString(
                            R.string.sessionout_loginagain));
                    getActivity().finish();
                case DataMembers.NOTIFY_URL_NOT_CONFIGURED:
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getActivity().getApplicationContext();
                    bmodel.showAlert(
                            getResources().getString(R.string.url_not_mapped), 0);
                    break;
                default:
                    break;
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }

    // to update the location value in TextView
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {


            if (isAdded()) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (!isLocSelectedManually) {
                                lattitude = LocationUtil.latitude;
                                longitude = LocationUtil.longitude;
                                if (latlongtextview != null)
                                    latlongtextview.setText((lattitude + "," + longitude)
                                            .trim());
                            }
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    }
                });
            }
        }
    }


    // map view from location layout
    public void onMapViewClicked() {
        int REQUEST_CODE = 100;
        Intent in;
        if (bmodel.configurationMasterHelper.IS_BAIDU_MAP)
            in = new Intent(getActivity(), BaiduMapDialogue.class);
        else
            in = new Intent(getActivity(), MapDialogue.class);


        in.putExtra("lat", lattitude);
        in.putExtra("lon", longitude);
        startActivityForResult(in, REQUEST_CODE);

    }

    public boolean getWhatCheckBoxForDayTicked() {

        checkedDaysAll = "";
        beatName = null;

        boolean bool = false;

        for (int k = 0; k < MAX_NO_OF_DAYS; k++) {
            if (marketCheckBox[k].isChecked()) {
                String tempStr = daysForUpload[k];
                if ((checkedDaysAll == null)
                        || (checkedDaysAll.length() < 1))
                    checkedDaysAll = tempStr + ":" + weekNoStr + ";";
                else {
                    checkedDaysAll = checkedDaysAll + tempStr + ":"
                            + weekNoStr + ";";
                }

                if ((beatName == null) || (beatName.length() < 1))
                    beatName = "" + k;

                bool = true;

            }
        }

        return bool;
    }

    public boolean getWhatCheckBoxForWeekNoTicked() {
        boolean bool = false;
        weekNoStr = "";
        for (int k = 0; k < NUMBER_OF_WEEKS; k++) {
            if (weekNoCheckBox[k].isChecked()) {
                String tempStr = (String) weekNoCheckBox[k].getText();

                if ((weekNoStr == null) || (weekNoStr.length() < 1))
                    weekNoStr = tempStr;
                else {
                    weekNoStr = weekNoStr + "," + tempStr;
                }
                bool = true;
            }
        }
        return bool;
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.hasExtra("lat") && data.hasExtra("isChanged")) {

                lattitude = data.getExtras().getDouble("lat");
                longitude = data.getExtras().getDouble("lon");
                if (data.getExtras().getBoolean("isChanged")) {

                    isLocSelectedManually = true;
                    String latlng = lattitude + "," + longitude;
                    latlongtextview.setText(latlng);
                }
            }

        }
    }


    public class NewRetailerReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.NewRetailerDownload";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReveiver(arg1);
        }

    }

    private void updateReveiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);

        switch (method) {
            case SynchronizationHelper.NEW_RETAILER_DOWNLOAD_INSERT:
                if (SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE
                        .equals(errorCode)) {

                    bmodel.synchronizationHelper.downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.NEW_RETAILER, SynchronizationHelper.DOWNLOAD_FINISH_UPDATE);


                } else {
                    String errorDownlodCode = bundle
                            .getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorDownlodCode);
                    if (errorDownloadMessage != null) {
                        Toast.makeText(getActivity(), errorDownloadMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                    Intent i = new Intent(getActivity(), HomeScreenActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }
                break;
            case SynchronizationHelper.DOWNLOAD_FINISH_UPDATE:

                deleteNewRetailer();
                bmodel.downloadRetailerMaster();
                alertDialog.dismiss();
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.data_download_successfully),
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(), HomeScreenActivity.class);
                startActivity(i);
                getActivity().finish();
                break;
        }

    }

    private void updateLocationMasterList() {
        bmodel.newOutletHelper.downloadLocationMaster();

        LinkedHashMap<Integer, ArrayList<LocationBO>> locationListByLevId = bmodel.newOutletHelper.getLocationListByLevId();
        if (locationListByLevId != null) {
            int count = 0;
            for (Map.Entry<Integer, ArrayList<LocationBO>> entry : locationListByLevId.entrySet()) {
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


    }

    private void updateLocationAdapter1(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();

        for (LocationBO locationBO : mLocationMasterList1) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }

        locationAdapter1 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter1.getCount() > 0) {
            if (!locationAdapter1.getItem(0).getLocName().toLowerCase().contains("select"))
                locationAdapter1.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter1.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);

        location1.setAdapter(locationAdapter1);

        if (screenMode == VIEW || screenMode == EDIT) {
            location1.setSelection(getPosition("LOCATION"));
            if (screenMode == 1)
                location1.setEnabled(false);
        }

    }

    private void updateLocationAdapter2(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();
        for (LocationBO locationBO : mLocationMasterList2) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }

        locationAdapter2 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter2.getCount() > 0) {
            if (!locationAdapter2.getItem(0).getLocName().toLowerCase().contains("select"))
                locationAdapter2.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter2.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);
        location2.setAdapter(locationAdapter2);


        if (screenMode == VIEW || screenMode == EDIT) {
            location2.setSelection(getPosition("LOCATION01"));
            if (screenMode == VIEW)
                location2.setEnabled(false);
        }

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                NewRetailerReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new NewRetailerReceiver();
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    public boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        StringBuilder sb = new StringBuilder();
        for (RetailerMasterBO bo : list) {
            if (sb.length() > 0)
                sb.append(", ");

            sb.append(bo.getRetailerName());
        }
        nearbyAutoCompleteTextView.setText(sb.toString());

        mselectedRetailers = list;
    }

    public void showAlert(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        surveyHelperNew.deleteNewRetailerSurvey(bmodel.newOutletHelper.getId());
                        if (screenMode == VIEW || screenMode == EDIT) {
                            startActivity(new Intent(getActivity(),
                                    NewOutletEdit.class));
                            getActivity().finish();
                        } else {
                            startActivity(new Intent(getActivity(),
                                    HomeScreenActivity.class));
                            getActivity().finish();
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    @SuppressLint("ValidFragment")
    public class CustomFragment extends DialogFragment {
        private String mTitle = "";
        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mPriorityproductLV;

        private int hasLink = 0;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
            hasLink = getArguments().getInt("hasLink");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            getDialog().setTitle(mTitle);
            if (getView() != null) {
                mTitleTV = (TextView) getView().findViewById(R.id.title);
                mTitleTV.setVisibility(View.GONE);
            }
            mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
            if (hasLink == 0)
                mOkBtn.setVisibility(View.GONE);

            mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
            mDismisBtn.setVisibility(View.GONE);
            mPriorityproductLV = (ListView) getView().findViewById(R.id.lv_colletion_print);
            if (hasLink == 0) {
                ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, mPriorityProductList);
                mPriorityproductLV.setAdapter(adapter);
                mPriorityproductLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                if (mSelectedpostion != -1)
                    mPriorityproductLV.setItemChecked(mSelectedpostion, true);
                mPriorityproductLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        priorityProductIDList.clear();
                        StandardListBO standardListBO = mPriorityProductList.get(position);
                        priorityProductIDList.add(standardListBO);
                        priorityProductAutoCompleteTextView.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                priorityProductAutoCompleteTextView.showDropDown();

                            }
                        }, 500);
                        priorityProductAutoCompleteTextView.setText(standardListBO.getListName());
                        priorityProductAutoCompleteTextView.setSelection(priorityProductAutoCompleteTextView.getText().length());
                        mSelectedpostion = position;
                        getDialog().dismiss();
                    }
                });
            } else if (hasLink == 1) {
                MyAdapter adapter = new MyAdapter();
                mPriorityproductLV.setAdapter(adapter);
            }


            mOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    updatePriorityProducts();
                    getDialog().dismiss();

                }
            });

        }

        class MyAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return mPriorityProductList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.list_priotityproduct,
                            parent, false);
                    holder.productNameTV = (TextView) convertView.findViewById(R.id.tv_product_name);
                    holder.productSelectCB = (CheckBox) convertView.findViewById(R.id.cb_productselect);
                    holder.productSelectCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            holder.standardListBO.setChecked(isChecked);
                        }
                    });
                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.standardListBO = mPriorityProductList.get(position);
                holder.productNameTV.setText(holder.standardListBO.getListName());
                holder.productSelectCB.setChecked(holder.standardListBO.isChecked());
                return convertView;
            }
        }

        class ViewHolder {
            StandardListBO standardListBO;
            TextView productNameTV;
            CheckBox productSelectCB;

        }

        private void updatePriorityProducts() {
            priorityProductIDList = new ArrayList<>();
            priorityProductIDList.clear();
            sb = new StringBuffer();

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
        }
    }

    private void deleteNewRetailer() {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

            for (String tableName : keys) {
                if (tableName.equals(DataMembers.tbl_nearbyRetailer) ||
                        tableName.equals(DataMembers.tbl_retailerPotential)) {
                    db.deleteSQL(tableName, "rid ='" + bmodel.newOutletHelper.getId() + "'", false);
                } else {
                    db.deleteSQL(tableName, "RetailerID ='" + bmodel.newOutletHelper.getId() + "'", false);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        MaterialSpinner materialSpinner[] = new MaterialSpinner[13];
        materialSpinner[0] = channel;
        materialSpinner[1] = subchannel;
        materialSpinner[2] = location1;
        materialSpinner[3] = location2;
        materialSpinner[4] = location3;
        materialSpinner[5] = route;
        materialSpinner[6] = paymentType;
        materialSpinner[7] = distributorSpinner;
        materialSpinner[8] = taxTypeSpinner;
        materialSpinner[9] = contactTitleSpinner1;
        materialSpinner[10] = contactTitleSpinner2;
        materialSpinner[11] = contractSpinner;
        materialSpinner[12] = classSpinner;
        bmodel.newOutletHelper.setMaterialSpinner(materialSpinner);
        outState.putSerializable("ImageIdList", outlet.getImageId());
        outState.putSerializable("ImageNameList", outlet.getImageName());
        outState.putString("uid", uID);
        if (!HomeScreenFragment.fromHomeScreen)
            ((NewOutlet) getActivity()).passData(editText, outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (bmodel.newOutletHelper.getEditText() != null)
            editText = bmodel.newOutletHelper.getEditText();

        MaterialSpinner materialSpinner[] = bmodel.newOutletHelper.getMaterialSpinner();
        if (materialSpinner != null) {
            int size = materialSpinner.length;
            for (int i = 0; i < size; i++) {

                if (i == 0) {
                    channel = materialSpinner[i];
                } else if (i == 1) {
                    subchannel = materialSpinner[i];
                } else if (i == 2) {
                    location1 = materialSpinner[i];
                } else if (i == 3) {
                    location2 = materialSpinner[i];
                } else if (i == 4) {
                    location3 = materialSpinner[i];
                } else if (i == 5) {
                    route = materialSpinner[i];
                } else if (i == 6) {
                    paymentType = materialSpinner[i];
                } else if (i == 7) {
                    distributorSpinner = materialSpinner[i];
                } else if (i == 8) {
                    taxTypeSpinner = materialSpinner[i];
                } else if (i == 9) {
                    contactTitleSpinner1 = materialSpinner[i];
                } else if (i == 10) {
                    contactTitleSpinner2 = materialSpinner[i];
                } else if (i == 11) {
                    contractSpinner = materialSpinner[i];
                } else if (i == 12) {
                    classSpinner = materialSpinner[i];
                }


            }
        }

        if (savedInstanceState != null) {
            outlet.setImageId((Vector<Integer>) savedInstanceState.getSerializable("ImageIdList"));
            outlet.setImageName((Vector<String>) savedInstanceState.getSerializable("ImageNameList"));
            uID = savedInstanceState.getString("uid");
        }


    }

}
