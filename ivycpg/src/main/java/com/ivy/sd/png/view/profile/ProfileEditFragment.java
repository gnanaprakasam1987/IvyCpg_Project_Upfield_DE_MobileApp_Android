package com.ivy.sd.png.view.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.location.LocationUtil;
import com.ivy.maplib.BaiduMapDialogue;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.MapDialogue;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.RetailerOTPDialog;

import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class ProfileEditFragment extends IvyBaseFragment
        implements RetailerOTPDialog.OTPListener, IProfileEditCallback {

    public static final String TAG = ProfileEditFragment.class.getSimpleName();

    /*Configuration Profile id's List
    * ROFILE09, PROFILE10, PROFILE11, PROFILE12, PROFILE41, PROFILE42*/
    private static final String PROFILE_60 = "PROFILE60";//Retailer Image :Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_02 = "PROFILE02";//STORENAME,length validation=Y,Has Edit=0,RetailerMaster
    private static final String PROFILE_03 = "PROFILE03";//ADDRESS1,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_04 = "PROFILE04";//ADDRESS2,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_05 = "PROFILE05";//ADDRESS3,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_06 = "PROFILE06";//CHANNEL,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_07 = "PROFILE07";//SUBCHANNEL,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_08 = "PROFILE08";//Latitude,Longitude,length validation=Y,Has Edit=1,RetailerAddress

    private static final String PROFILE_09 = "PROFILE09";//CONTACTPERSON1,length validation=Y,Has Edit=0,RetailerContact
    private static final String PROFILE_10 = "PROFILE10";//PHNO1,length validation=Y,input validation=NUMBER,Has Edit=0,RetailerContact
    private static final String PROFILE_11 = "PROFILE11";//CONTACTPERSON2,length validation=Y,Has Edit=0,RetailerContact
    private static final String PROFILE_12 = "PROFILE12";//PHNO2,length validation=Y,input validation=NUMBER,Has Edit=0,RetailerContact
    private static final String PROFILE_41 = "PROFILE41";//CONTACTTITLE 1 ,length validation=Y,Has Edit=1,,RetailerContact
    private static final String PROFILE_42 = "PROFILE42";//CONTACTTITLE 2,length validation=Y,Has Edit=1,RetailerContact

    private static final String PROFILE_13 = "PROFILE13";//LOCATION01 One level up,length validation=Y,Has Edit=0,RetailerMaster
    private static final String PROFILE_14 = "PROFILE14";//LOCATION02 two level up,length validation=Y,Has Edit=0,RetailerMaster
    private static final String PROFILE_15 = "PROFILE15";//LOCATION Least level,length validation=Y,Has Edit=0,RetailerMaster
    private static final String PROFILE_20 = "PROFILE20";//RFiled1, IF (FUN42) -> CreditLimit - Outstanding,Has Edit=0
    private static final String PROFILE_22 = "PROFILE22";//Contract Type,Has Edit=0 Note:-Only Profile Config
    private static final String PROFILE_25 = "PROFILE25";//CREDITPERIOD,level,length validation=Y,Has Edit=0,RetailerMaster
    private static final String PROFILE_26 = "PROFILE26";//RField2,Has Edit=1 Note:-Only Profile Config
    private static final String PROFILE_27 = "PROFILE27";
    private static final String PROFILE_28 = "PROFILE28";//RField4,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_30 = "PROFILE30";//ContactNumber Note:-Only Profile Config
    private static final String PROFILE_31 = "PROFILE31";//Latitude,Longitude,length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_36 = "PROFILE36";//NEARBYRET(retailers)length validation=Y,Has Edit=1,RetailerMaster
    private static final String PROFILE_38 = "PROFILE38";//PINCODE,length validation=Y,Has Edit=0, RetailerAddress
    private static final String PROFILE_39 = "PROFILE39";//RetailerAddressCity :Note:-Only Profile Config,Has Edit=1
    private static final String PROFILE_40 = "PROFILE40";//STATE ,length validation=Y,Has Edit=0, RetailerAddress

    private static final String PROFILE_43 = "PROFILE43";//CONTRACT->contract status id,length validation=Y,Has Edit=0, ,RetailerMaster
    private static final String PROFILE_47 = "PROFILE47";//outstanding :Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_48 = "PROFILE48";//Return Credit Limit :Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_49 = "PROFILE49";//Invoice Amount Calculation :Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_50 = "PROFILE50";//location Name.Retailer Master : locationid :Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_51 = "PROFILE51";//TAXTYPE,length validation=Y,Has Edit=0, ,RetailerMaster
    private static final String PROFILE_53 = "PROFILE53";//RFIELD5-->RetailerMaster
    private static final String PROFILE_54 = "PROFILE54";//RFIELD6-->RetailerMaster
    private static final String PROFILE_55 = "PROFILE55";//RFIELD7-->RetailerMaster
    private static final String PROFILE_57 = "PROFILE57";//PRIORITYPRODUCT -->From  PriorityProducts Table
    private static final String PROFILE_58 = "PROFILE58";//ATTRIBUTE -->From RetailerAttribute Table
    private static final String PROFILE_61 = "PROFILE61";//GSTN Number,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_62 = "PROFILE62";//INSEZ,length validation=Y,Has Edit=0, ,RetailerMaster
    private static final String PROFILE_63 = "PROFILE63";//Photo Capture , lat and Long, Note:-Only Profile Config,Has Edit=0
    private static final String PROFILE_78 = "PROFILE78";//Email,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_79 = "PROFILE79";//MOBILE ,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_81 = "PROFILE81";//pan_number,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_82 = "PROFILE82";//FOOD_LICENCE_NUM,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_83 = "PROFILE83";//FOOD_LICENCE_EXP_DATE,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_84 = "PROFILE84";//DRUG_LICENSE_NUM,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_85 = "PROFILE85";//DRUG_LICENSE_EXP_DATE,length validation=Y,Has Edit=1,RetailerMaster;
    private static final String PROFILE_86 = "PROFILE86";//FAX,length validation=Y,Has Edit=1,RetailerAddress;
    private static final String PROFILE_87 = "PROFILE87";//REGION,length validation=Y,Has Edit=1,RetailerAddress;
    private static final String PROFILE_88 = "PROFILE88";//COUNTRY,length validation=Y,Has Edit=1,RetailerAddress;

    /*Configuration RField id's List */
    private static final String RFIELD_4 = "RFIELD4";
    private static final String RFIELD_5 = "RFIELD5";
    private static final String RFIELD_6 = "RFIELD6";
    private static final String RFIELD_7 = "RFIELD7";

    /*Configuration Static Constant */
    private static final String D = "D";
    private static final String LNAME = "LNAME";
    private static final String LNAME_2 = "LNAME2";
    private static final String CT_2_TITLE = "CT2TITLE";
    private static final String CT_1_TITLE = "CT1TITLE";

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int LATLONG_CAMERA_REQUEST_CODE = 2;

    private ScrollView mProfielEditScrollView;
    private TextInputLayout editTextInputLayout1, editTextInputLayout2, editTextInputLayout3, editTextInputLayout4;
    private TextView latlongtextview, priorityproducttextview, nearbyTextView;
    private Button mButtonSave;
    private ImageView imageView, latlongCameraBtn;
    private AppCompatCheckBox inSEZcheckBox = null;
    private AlertDialog alertDialog;

    @SuppressLint("StaticFieldLeak")
    static TextView dlExpDateTextView = null, flExpDateTextView = null;

    private LinearLayout.LayoutParams params3, params4, params5;
    private LinearLayout.LayoutParams weight1, weight2, weight3;
    private LinearLayout.LayoutParams paramsAttrib, paramsAttribSpinner;

    private BusinessModel bmodel = null;
    private RetailerMasterBO retailerObj = null;

    private ArrayAdapter<LocationBO> locationAdapter1 = null, locationAdapter2 = null;
    private ArrayAdapter<NewOutletBO> contactTitleAdapter = null;

    private TextView textview[] = new TextView[150];
    private AppCompatEditText editText[] = new AppCompatEditText[150];

    private ArrayList<NewOutletBO> mcontactTitleList = null, mcontractStatusList = null;
    private ArrayList<NewOutletAttributeBO> attributeList = null, attributeHeaderList = null;
    private ArrayList<StandardListBO> mPriorityProductList = null, selectedPriorityProductList = null;
    private ArrayList<Integer> attributeIndexList = null;
    private ArrayList<InputFilter> inputFilters = null;

    private ArrayList<LocationBO> mLocationMasterList1 = null, mLocationMasterList2 = null, mLocationMasterList3 = null;
    private Vector<ChannelBO> channelMaster = null;

    private Vector<ConfigureBO> profileConfig = null;
    private Vector<RetailerMasterBO> mNearbyRetIds = null;
    private Vector<RetailerMasterBO> mSelectedIds = new Vector<>();

    private HashMap<String, ArrayList<Integer>> attributeIndexMap = null;
    private HashMap<String, ArrayList<ArrayList<NewOutletAttributeBO>>> listHashMap = null;
    // HashMap to retreive selected level of Attribute
    private HashMap<Integer, NewOutletAttributeBO> selectedAttribList = null;
    private HashMap<String, MaterialSpinner> spinnerHashMap = null;
    private HashMap<String, ArrayAdapter<NewOutletAttributeBO>> spinnerAdapterMap = null;
    private HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap = null;
    private HashMap<Integer, ArrayList<Integer>> mAttributeListByChannelId;


    private int other1_editText_index, other2_editText_index, lName1_editText_index, lName2_editText_index;
    private int locid = 0, loc2id = 0, check = 0, spinnerCount = 0, subChannelSpinnerCount = 0;

    static String lat = "", longitude = "";
    private String selectedProductID = "", cameraFilePath = "", imageFileName;
    private String mcontact_title1_lovId = "", mcontact_title2_lovId = "",
            mcontact_title1_text = "0", mcontact_title2_text = "0";

    private boolean is_contact_title1 = false, is_contact_title2 = false, isLatLong = false;
    private boolean isLatLongCameravailable = false;
    private boolean isMobileNoVerfied = false, isEmailVerfied = false;

    private MaterialSpinner channel, subchannel, location1, location2, location3,
            contactTitleSpinner1, contactTitleSpinner2, contractSpinner,
            rField5Spinner, rField6Spinner, rField7Spinner, rField4Spinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        mProfielEditScrollView = view.findViewById(R.id.profile_edit_scrollview);
        mButtonSave = view.findViewById(R.id.profile_edit_save);
        mButtonSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        setHasOptionsMenu(true);

        onDownloadAsyncTask();//AsyncTask To download from local database
        onButtonClick();  //Save updated  details in DB..

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
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
        //Log.d(TAG, "onDestroyView() has been called.");
        if (dlExpDateTextView != null)
            dlExpDateTextView = null;
        if (flExpDateTextView != null)
            flExpDateTextView = null;
    }

    private void onButtonClick() {
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (validateEditProfile()) {
                        if (bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                            if ((lat.equals("") || SDUtil.convertToDouble(lat) == 0 || longitude.equals("")
                                    || SDUtil.convertToDouble(longitude) == 0)
                                    || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl != 0
                                    && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)) {
                                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.location_not_captured), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        new SaveEditAsyncTask().execute();
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        });
    }

    private Vector<RetailerMasterBO> getNearbyRetailerIds() {
        //  if (bmodel.configurationMasterHelper.IS_NEARBY_RETAILER) {
        ArrayList<String> ids = bmodel.newOutletHelper.getNearbyRetailerIds(bmodel.getRetailerMasterBO().getRetailerID());
        if (ids != null) {
            mNearbyRetIds = new Vector<>();
            for (int i = 0; i < ids.size(); i++) {
                for (RetailerMasterBO bo : bmodel.getRetailerMaster()) {
                    if (bo.getRetailerID().equals(ids.get(i))) {
                        mNearbyRetIds.add(bo);
                    }
                }
            }
        }
        return mNearbyRetIds;
    }

    @SuppressLint("RestrictedApi")
    private View createTabViewForProfileForEdit() {

        profileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();

        other1_editText_index = profileConfig.size() + 50; //Title edittext one
        other2_editText_index = profileConfig.size() + 51; //Title edittext two
        lName1_editText_index = profileConfig.size() + 25;
        lName2_editText_index = profileConfig.size() + 26;

        retailerObj = bmodel.getRetailerMasterBO();

        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_41) && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title1 = true;

            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_42) && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title2 = true;

            double outStanding = 0.0;
            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_47) && configureBO.isFlag() == 1)
                outStanding = bmodel.getInvoiceAmount() - bmodel.getOutStandingInvoiceAmount();


            //String retailerCreditLimit = "0.0";
            double invoiceAmount = 0.0;
            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_48) && configureBO.isFlag() == 1)
                //retailerCreditLimit = bmodel.getRetailerMasterBO().getProfile_creditLimit();

                if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_49) && configureBO.isFlag() == 1)
                    invoiceAmount = bmodel.getInvoiceAmount();

            /*// String physicalLocation = "";
            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_50) && configureBO.isFlag() == 1)
                // physicalLocation = bmodel.mRetailerHelper.getPhysicalLcoation(retailerObj.getLocationId());

                if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_51) && configureBO.isFlag() == 1) {
                    String gstType = bmodel.mRetailerHelper.getGSTType(retailerObj.getTaxTypeId());
                    @NonNls String taxType = "";
                    boolean isGstType;
                    if (gstType.length() > 0) {
                        isGstType = true;
                        taxType = "CST";
                    } else {
                        isGstType = false;
                        taxType = "VAT";
                    }
                }*/

            if (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_58) && configureBO.isFlag() == 1) {
                ArrayList<NewOutletAttributeBO> tempList = bmodel.newOutletHelper.updateRetailerMasterAttribute(
                        bmodel.newOutletAttributeHelper.getEditAttributeList(retailerObj.getRetailerID()));
                bmodel.newOutletAttributeHelper.downloadCommonAttributeList();
                mAttributeListByChannelId = bmodel.newOutletAttributeHelper.downloadChannelWiseAttributeList();
                //Load Retailer Based Attribute list and store in retailer master bo
                bmodel.getAttributeListForRetailer();
                //Load Attribute List which
                attributeList = bmodel.newOutletHelper.updateRetailerMasterAttribute(retailerObj.getAttributeBOArrayList());
                attribMap = bmodel.newOutletAttributeHelper.getAttribMap();
                try {
                    if (!tempList.isEmpty()) {

                        int size = attributeList.size();
                        if (attributeList.size() > 0) {
                            ArrayList<NewOutletAttributeBO> newOutletAttributeBOS = new ArrayList<>();
                            newOutletAttributeBOS.addAll(attributeList);
                            for (int i = 0; i < tempList.size(); i++) {
                                for (int j = 0; j < size; j++) {

                                    if (newOutletAttributeBOS.get(j).getParentId() == tempList.get(i).getParentId()
                                            && newOutletAttributeBOS.get(j).getAttrId() == tempList.get(i).getAttrId()
                                            && tempList.get(i).getStatus().equalsIgnoreCase(D)) {

                                        for (int k = 0; k < attributeList.size(); k++)
                                            if (attributeList.get(k).getParentId() == tempList.get(i).getParentId()
                                                    && attributeList.get(k).getAttrId() == tempList.get(i).getAttrId()
                                                    && tempList.get(i).getStatus().equalsIgnoreCase(D))
                                                attributeList.remove(j);

                                    } else {
                                        if (j == size - 1) {
                                            attributeList.add(tempList.get(i));
                                        }
                                    }

                                }
                            }
                        } else {
                            attributeList.addAll(tempList);
                        }
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

            if ((configureBO.getConfigCode().equalsIgnoreCase(PROFILE_08) && configureBO.isFlag() == 1)
                    || (configureBO.getConfigCode().equalsIgnoreCase(PROFILE_31) && configureBO.isFlag() == 1)) {
                isLatLong = true;
                lat = retailerObj.getLatitude() + "";
                longitude = retailerObj.getLongitude() + "";
            }
        }

        if (is_contact_title2 || is_contact_title1) {
            mcontactTitleList = new ArrayList<>();
            mcontactTitleList.add(0, new NewOutletBO(-1, getResources().getString(R.string.select_str) + "Title Name"));
            mcontactTitleList.addAll(bmodel.newOutletHelper.getContactTitleList());
            mcontactTitleList.add(bmodel.newOutletHelper.getContactTitleList().size() + 1, new NewOutletBO(0, "Others"));
            contactTitleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontactTitleList);
            contactTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        if (mProfielEditScrollView != null) mProfielEditScrollView.removeAllViews();

        LinearLayout.LayoutParams commonsparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams.setMargins(10, 15, 10, 0);

        params3 = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.weight = 1;
        params3.setMargins(0, (int) getResources().getDimension(R.dimen.profile_spinner_top_margin), 0, 0);

        params4 = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params4.weight = 1;

        params5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params5.setMargins(0, 0, 20, 0);
        params5.gravity = Gravity.CENTER;

        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;

        weight3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight3.weight = 3;
        weight3.gravity = Gravity.CENTER;

        paramsAttrib = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAttrib.weight = .7f;
        paramsAttrib.setMargins(0, 0, 10, 0);

        paramsAttribSpinner = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsAttribSpinner.weight = 2.3f;

        LinearLayout.LayoutParams weight0wrap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight0wrap.setMargins(10, 0, 0, 5);

        LinearLayout.LayoutParams weight4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight4.gravity = Gravity.CENTER;
        weight4.setMargins(30, 0, 0, 0);

        LinearLayout.LayoutParams params8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params8.gravity = Gravity.CENTER;
        params8.setMargins(0, 0, 0, 0);

        LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params6.setMargins(0, 0, 0, 0);
        params6.gravity = Gravity.CENTER;

        LinearLayout totalView = new LinearLayout(getActivity());
        totalView.setOrientation(LinearLayout.VERTICAL);
        totalView.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.background_noise));

        int size = profileConfig.size();
        try {
            if (size != 0) {  /* Set profile Edit Image start  */
                if (profileConfig.get(0).getConfigCode().equals(PROFILE_60) &&
                        (profileConfig.get(0).isFlag() == 1) &&
                        (profileConfig.get(0).getModule_Order() == 1)) {
                    totalView.addView(getImageView());
                }
            } /* Set profile Edit Image End  */

            for (int i = 0; i < size; i++) {
                int mNumber = i;
                int flag = profileConfig.get(i).isFlag();
                int Order = profileConfig.get(i).getModule_Order();
                String mName = profileConfig.get(i).getMenuName();
                String configCode = profileConfig.get(i).getConfigCode();

                if (configCode.equals(PROFILE_02) && flag == 1 && Order == 1) {
                    if (retailerObj.getRetailerName() == null || retailerObj.getRetailerName().equals("null"))
                        retailerObj.setRetailerName("");
                    String text = retailerObj.getRetailerName() + "";

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS), commonsparams);

                } else if (configCode.equals(PROFILE_03) && flag == 1 && Order == 1) {

                    if (retailerObj.getAddress1() == null || retailerObj.getAddress1().equals("null"))
                        retailerObj.setAddress1("");

                    String text = retailerObj.getAddress1() + "";

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS), commonsparams);


                } else if (configCode.equals(PROFILE_04) && flag == 1 && Order == 1) {
                    if (retailerObj.getAddress2() == null || retailerObj.getAddress2().equals("null")) {
                        retailerObj.setAddress2("");
                    }

                    String text = retailerObj.getAddress2() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);


                } else if (configCode.equals(PROFILE_05) && flag == 1 && Order == 1) {
                    if (retailerObj.getAddress3() == null || retailerObj.getAddress3().equals("null")) {
                        retailerObj.setAddress3("");
                    }
                    String text = retailerObj.getAddress3() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS), commonsparams);

                } else if (configCode.equals(PROFILE_39) && flag == 1 && Order == 1) {
                    if (retailerObj.getCity() == null || retailerObj.getCity().equals("null")) {
                        retailerObj.setCity("");
                    }

                    String text = retailerObj.getCity() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    Commons.print(PROFILE_39 + "" + profileConfig.get(i).getModule_Order());


                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);
                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                } else if (configCode.equals(PROFILE_40) && flag == 1 && Order == 1) {
                    if (retailerObj.getState() == null || retailerObj.getState().equals("null")) {retailerObj.setState("");
                    }
                    String text = retailerObj.getState() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    Commons.print(PROFILE_40 + "" + profileConfig.get(i).getModule_Order());
                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS), commonsparams);
                } else if (configCode.equals(PROFILE_38) && flag == 1 && Order == 1) {
                    if (retailerObj.getPincode() == null
                            || retailerObj.getPincode().equals("null")) {
                        retailerObj.setPincode("");
                    }

                    String text = retailerObj.getPincode() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    Commons.print(PROFILE_38 + "," + "" + profileConfig.get(i).getModule_Order());
                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                            commonsparams);
                } else if (configCode.equals(PROFILE_30) && flag == 1 && Order == 1) {
                    if (retailerObj.getContactnumber() == null
                            || retailerObj.getContactnumber().equals("null")) {
                        retailerObj.setContactnumber("");
                    }

                    String text = retailerObj.getContactnumber() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                }
                else if (configCode.equals(PROFILE_06) && flag == 1 && Order == 1) {
                    int id = retailerObj.getChannelID();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));
                    totalView.addView(getSpinnerView(mNumber, mName, configCode, id), commonsparams);
                }
                else if (configCode.equals(PROFILE_07) && flag == 1 && Order == 1) {
                    int id = retailerObj.getSubchannelid();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                    totalView.addView(
                            getSpinnerView(mNumber, mName, configCode, id
                            ), commonsparams);
                }
                else if (configCode.equals(PROFILE_43) && flag == 1 && Order == 1) {
                    int id = retailerObj.getContractLovid();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                    totalView.addView(getSpinnerView(mNumber, mName, configCode, id), commonsparams);
                }
                else if (configCode.equals(PROFILE_08) && flag == 1 && Order == 1) {
                    String textLat = retailerObj.getLatitude() + "";
                    @NonNls String MenuName = "LatLong";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(textLat))
                            textLat = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    for (int j = 0; j < size; j++) {
                        if (profileConfig.get(j).getConfigCode().equals(PROFILE_31)
                                && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                            String textLong = retailerObj.getLongitude() + "";
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode()) != null)
                                if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode()).equals(textLong))
                                    textLong = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode());

                            String text = textLat + ", " + textLong;
                            totalView.addView(getLatlongTextView(mNumber, MenuName, text), commonsparams);
                        }
                    }
                }
                else if (configCode.equals(PROFILE_63) && flag == 1 && Order == 1) {
                    isLatLongCameravailable = true;
                }
                else if (configCode.equals(PROFILE_09) && flag == 1 && Order == 1) {

                    if (retailerObj.getContactname() == null || retailerObj.getContactname().equals("null")) {
                        retailerObj.setContactname("");
                    }

                    String text = "";
                    text = retailerObj.getContactname();

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME), commonsparams);
                    else
                        totalView.addView(getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS), commonsparams);

                } else if (configCode.equals(PROFILE_10) && flag == 1 && Order == 1) {
                    if (retailerObj.getContactnumber1() == null
                            || retailerObj.getContactnumber1().equals("null")) {
                        retailerObj.setContactnumber1("");
                    }

                    String text = "";
                    text = retailerObj.getContactnumber1();

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals(PROFILE_11) && flag == 1 && Order == 1) {
                    if (retailerObj.getContactname2() == null
                            || retailerObj.getContactname2().equals("null")) {
                        retailerObj.setContactname2("");
                    }

                    String text = "";
                    text = retailerObj.getContactname2() + "";

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)


                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);


                } else if (configCode.equals(PROFILE_12) && flag == 1 && Order == 1) {

                    if (retailerObj.getContactnumber2() == null
                            || retailerObj.getContactnumber2().equals("null")) {
                        retailerObj.setContactnumber2("");
                    }

                    String text = "";
                    text = retailerObj
                            .getContactnumber2();


                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals(PROFILE_13) && flag == 1 && Order == 1) {
                    try {
                        String title = "";

                        locid = retailerObj.getLocationId();
                        if (locid != 0) {
                            String[] loc1 = bmodel.mRetailerHelper.getParentLevelName(
                                    locid, false);
                            title = loc1[2];
                        }

                        int id = retailerObj.getLocationId();
                        if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                                id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals(PROFILE_14) && flag == 1 && Order == 1) {

                    try {
                        String title = "";

                        String[] loc2 = bmodel.mRetailerHelper.getParentLevelName(
                                locid, true);
                        if (loc2 != null) {

                            loc2id = SDUtil.convertToInt(loc2[0]);
                            title = loc2[2];

                        }
                        int id = retailerObj.getLocationId();
                        if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                                id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals(PROFILE_15) && flag == 1 && Order == 1) {
                    try {
                        String title = "";
                        String[] loc3 = bmodel.mRetailerHelper
                                .getParentLevelName(loc2id, true);

                        if (loc3 != null) {
                            title = loc3[2];
                        }

                        int id = retailerObj.getLocationId();
                        if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                                id = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals(PROFILE_36)) {
                    if (!retailerObj.getIsNew().equals("Y"))
                        totalView.addView(
                                getNearByRetailerView(mNumber, mName, true
                                ), commonsparams);


                } else if (configCode.equals(PROFILE_25) && flag == 1 && Order == 1) {

                    String text = retailerObj.getCreditDays() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                            commonsparams);
                } else if (configCode.equals(PROFILE_20) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRField1() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);


                } else if (configCode.equals(PROFILE_26) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRfield2() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);


                } else if (configCode.equals(PROFILE_27) && flag == 1 && Order == 1) {

                    String text = retailerObj.getCredit_invoice_count() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);


                } else if (configCode.equals(PROFILE_28) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRField4() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (profileConfig.get(i).getHasLink() == 0)

                        if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                    commonsparams);

                        else
                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                    commonsparams);

                    else {
                        if (text.equals(""))
                            text = "0";
                        totalView.addView(
                                getSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text)
                                ), commonsparams);
                    }
                } else if (configCode.equals(PROFILE_53) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRField5() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (profileConfig.get(i).getHasLink() == 0)
                        if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                    commonsparams);
                        else
                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                    commonsparams);
                    else {
                        if (text.equals(""))
                            text = "0";
                        totalView.addView(
                                getSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text)
                                ), commonsparams);
                    }
                } else if (configCode.equals(PROFILE_54) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRField6() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (profileConfig.get(i).getHasLink() == 0)
                        if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                    commonsparams);
                        else
                            totalView.addView(
                                    getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                    commonsparams);
                    else {
                        if (text.equals(""))
                            text = "0";
                        totalView.addView(
                                getSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text)
                                ), commonsparams);
                    }
                } else if (configCode.equals(PROFILE_55) && flag == 1 && Order == 1) {

                    String text = retailerObj.getRField7() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (profileConfig.get(i).getHasLink() == 0)
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                                commonsparams);
                    else {
                        if (text.equals(""))
                            text = "0";
                        totalView.addView(
                                getSpinnerView(mNumber, mName, configCode, SDUtil.convertToInt(text)
                                ), commonsparams);
                    }
                } else if (configCode.equals(PROFILE_57) && flag == 1 && Order == 1) {
                    mPriorityProductList = bmodel.newOutletHelper.downloadPriorityProducts();
                    String productID = "";
                    ArrayList<String> products;
                    products = bmodel.newOutletHelper.downloadPriorityProductsForRetailerEdit(retailerObj.getRetailerID());
                    if (products == null) {
                        products = bmodel.newOutletHelper.downloadPriorityProductsForRetailer(retailerObj.getRetailerID());
                    }
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

                    totalView.addView(
                            getPriorityProductView(mNumber, mName, sb.toString(), productID),
                            commonsparams);
                } else if (configCode.equals(PROFILE_58) && flag == 1 && Order == 1) {
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    LLParams.setMargins(10, 5, 10, 5);
                    totalView.addView(
                            addAttributeView(0), LLParams);
                } else if (configCode.equals(PROFILE_61) && flag == 1 && Order == 1) {

                    String text = retailerObj.getGSTNumber() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_TEXT),
                            commonsparams);
                } else if (configCode.equals(PROFILE_62) && flag == 1 && Order == 1) {

                    LinearLayout baselayout = new LinearLayout(getActivity());
                    baselayout.setOrientation(LinearLayout.VERTICAL);
                    //
                    LinearLayout linearlayout = new LinearLayout(getActivity());
                    linearlayout.setOrientation(LinearLayout.VERTICAL);

                    inSEZcheckBox = new AppCompatCheckBox(getActivity());
                    String text = retailerObj.getIsSEZzone() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (text.equals("1")) {
                        inSEZcheckBox.setChecked(true);
                    } else {
                        inSEZcheckBox.setChecked(false);
                    }
                    linearlayout.addView(inSEZcheckBox, weight2);
                    //

                    LinearLayout textLayout = new LinearLayout(getActivity());
                    textLayout.setOrientation(LinearLayout.HORIZONTAL);

                    if (profileConfig.get(i).getMandatory() == 1) {
                        LinearLayout.LayoutParams weight0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
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
                } else if (configCode.equals(PROFILE_81) && flag == 1 && Order == 1) {

                    if (retailerObj.getPanNumber() == null
                            || retailerObj.getPanNumber().equals(
                            "null")) {
                        retailerObj.setPanNumber("");
                    }

                    String text = retailerObj.getPanNumber();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                } else if (configCode.equals(PROFILE_82) && flag == 1 && Order == 1) {

                    if (retailerObj.getFoodLicenceNo() == null
                            || retailerObj.getFoodLicenceNo().equals(
                            "null")) {
                        retailerObj.setFoodLicenceNo("");
                    }

                    String text = retailerObj.getFoodLicenceNo();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                } else if (configCode.equals(PROFILE_84) && flag == 1 && Order == 1) {

                    if (retailerObj.getDLNo() == null
                            || retailerObj.getDLNo().equals(
                            "null")) {
                        retailerObj.setDLNo("");
                    }

                    String text = retailerObj.getDLNo();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                } else if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase(PROFILE_85)) {
                    LinearLayout secondlayout = new LinearLayout(getActivity());
                    LinearLayout firstlayout = new LinearLayout(getActivity());
                    LinearLayout linearlayout = new LinearLayout(getActivity());
                    linearlayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout finallayout = new LinearLayout(getActivity());
                    finallayout.setOrientation(LinearLayout.HORIZONTAL);
                    TextView tv_label = new TextView(getActivity());
                    tv_label.setText(mName);
                    tv_label.setTextColor(Color.BLACK);
                    tv_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    tv_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    firstlayout.addView(tv_label, params8);
                    dlExpDateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
                    dlExpDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    dlExpDateTextView.setTextColor(Color.BLACK);
                    dlExpDateTextView.setGravity(Gravity.CENTER);
                    dlExpDateTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    dlExpDateTextView.setId(mNumber);
                    dlExpDateTextView.setTypeface(dlExpDateTextView.getTypeface(), Typeface.NORMAL);
                    if (retailerObj.getDLNoExpDate() == null
                            || retailerObj.getDLNoExpDate().equals(
                            "null") || retailerObj.getDLNoExpDate().isEmpty()) {
                        retailerObj.setDLNoExpDate("Select Date");
                    }

                    String text = retailerObj.getDLNoExpDate();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    dlExpDateTextView.setText(text);
                    secondlayout.addView(dlExpDateTextView, weight0wrap);
                    finallayout.addView(firstlayout, params8);
                    finallayout.addView(secondlayout, weight4);
                    linearlayout.addView(finallayout, params6);
                    totalView.addView(linearlayout, commonsparams);

                    dlExpDateTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Calendar c = Calendar.getInstance();
                            int year = c.get(Calendar.YEAR);
                            int month = c.get(Calendar.MONTH);
                            int day = c.get(Calendar.DAY_OF_MONTH);
                            @NonNls String date = dlExpDateTextView.getText().toString();
                            if (!date.equalsIgnoreCase("Select Date") && date.contains("/") && date.split("/").length == 3) {
                                year = Integer.valueOf(date.split("/")[0]);
                                month = Integer.valueOf(date.split("/")[1]) - 1;
                                day = Integer.valueOf(date.split("/")[2]);
                            }
                            @NonNls DialogFragment newFragment = new DatePickerFragment("DLEXPDATE", year, month, day);
                            newFragment.show(getActivity().getSupportFragmentManager(), "dlDatePicker");


                        }
                    });


                } else if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase(PROFILE_83)) {
                    LinearLayout secondlayout = new LinearLayout(getActivity());
                    LinearLayout firstlayout = new LinearLayout(getActivity());
                    LinearLayout linearlayout = new LinearLayout(getActivity());
                    linearlayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout finallayout = new LinearLayout(getActivity());
                    finallayout.setOrientation(LinearLayout.HORIZONTAL);
                    TextView tv_label = new TextView(getActivity());
                    tv_label.setText(mName);
                    tv_label.setTextColor(Color.BLACK);
                    tv_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    tv_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    firstlayout.addView(tv_label, params8);
                    flExpDateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
                    flExpDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                    flExpDateTextView.setTextColor(Color.BLACK);
                    flExpDateTextView.setGravity(Gravity.CENTER);
                    flExpDateTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    flExpDateTextView.setId(mNumber);
                    flExpDateTextView.setTypeface(flExpDateTextView.getTypeface(), Typeface.NORMAL);
                    if (retailerObj.getFoodLicenceExpDate() == null
                            || retailerObj.getFoodLicenceExpDate().equals(
                            "null") || retailerObj.getFoodLicenceExpDate().isEmpty()) {
                        retailerObj.setFoodLicenceExpDate("Select Date");
                    }

                    String text = retailerObj.getFoodLicenceExpDate();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    flExpDateTextView.setText(text);
                    secondlayout.addView(flExpDateTextView, weight0wrap);
                    finallayout.addView(firstlayout, params8);
                    finallayout.addView(secondlayout, weight4);
                    linearlayout.addView(finallayout, params6);
                    totalView.addView(linearlayout, commonsparams);

                    flExpDateTextView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            final Calendar c = Calendar.getInstance();
                            int year = c.get(Calendar.YEAR);
                            int month = c.get(Calendar.MONTH);
                            int day = c.get(Calendar.DAY_OF_MONTH);
                            @NonNls String date = flExpDateTextView.getText().toString();
                            if (!date.equalsIgnoreCase("Select Date") && date.contains("/") && date.split("/").length == 3) {
                                year = Integer.valueOf(date.split("/")[0]);
                                month = Integer.valueOf(date.split("/")[1]) - 1;
                                day = Integer.valueOf(date.split("/")[2]);
                            }
                            @NonNls DialogFragment newFragment = new DatePickerFragment("FLEXPDATE", year, month, day);
                            newFragment.show(getActivity().getSupportFragmentManager(), "flDatePicker");

                        }
                    });
                } else if (configCode.equals(PROFILE_78) && flag == 1 && Order == 1) {

                    if (retailerObj.getEmail() == null
                            || retailerObj.getEmail().equals(
                            "null")) {
                        retailerObj.setEmail("");
                    }

                    String text = retailerObj.getEmail();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
                            commonsparams);
                } else if (configCode.equals("PROFILE79") && flag == 1 && Order == 1) {
                    if (retailerObj.getMobile() == null
                            || retailerObj.getMobile().equals(
                            "null")) {
                        retailerObj.setMobile("");
                    }
                    String text = retailerObj.getMobile() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals(PROFILE_86) && flag == 1 && Order == 1) {
                    if (retailerObj.getFax() == null
                            || retailerObj.getFax().equals(
                            "null")) {
                        retailerObj.setFax("");
                    }
                    String text = retailerObj.getFax() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals(PROFILE_87) && flag == 1 && Order == 1) {
                    if (retailerObj.getRegion() == null
                            || retailerObj.getRegion().equals(
                            "null")) {
                        retailerObj.setRegion("");
                    }
                    String text = retailerObj.getRegion() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                } else if (configCode.equals(PROFILE_88) && flag == 1 && Order == 1) {
                    if (retailerObj.getCountry() == null
                            || retailerObj.getCountry().equals(
                            "null")) {
                        retailerObj.setCountry("");
                    }
                    String text = retailerObj.getCountry() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)

                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                                commonsparams);

                    else
                        totalView.addView(
                                getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS),
                                commonsparams);

                }


            }

            mProfielEditScrollView.addView(totalView);

        } catch (Exception e) {
            Commons.printException(e);
        }


        // return view;
        return null;
    }


    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout(int oriendation, int resourcesId) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }

    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout(int oriendation, int resourcesId, float weightSum) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        linearlayout.setWeightSum(weightSum);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }

    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout() {
        return new LinearLayout(getActivity());
    }

    /*This method is used to create a new LinearLayout with attributes */
    private TextView getSingleTextView(int positionNumber, String menuName) {
        textview[positionNumber] = new TextView(getActivity());
        textview[positionNumber].setText(menuName);
        textview[positionNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        textview[positionNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        textview[positionNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        return textview[positionNumber];
    }

    /*Get the EditTextView*/
    private AppCompatEditText getSingleEditTextView(int positionNumber, String configCode, String menuName, String values) {

        editText[positionNumber] = new AppCompatEditText(getActivity());
        editText[positionNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        editText[positionNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        editText[positionNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        editText[positionNumber].setText(values);
        editText[positionNumber].setHint(menuName);

        if (!comparConfigerCode(configCode, PROFILE_78)) { //if not Email   //cmd for not apply inputfilter value for email id
            getInputFilter(positionNumber);
        }

        if (!bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER)
            editText[positionNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        else
            editText[positionNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        if (comparConfigerCode(configCode, PROFILE_30) || comparConfigerCode(configCode, PROFILE_10) ||
                comparConfigerCode(configCode, PROFILE_12) || comparConfigerCode(configCode, PROFILE_79) ||
                comparConfigerCode(configCode, PROFILE_86)) {
            editText[positionNumber].setInputType(InputType.TYPE_CLASS_PHONE);
        }

        if (comparConfigerCode(configCode, PROFILE_25)) {
            editText[positionNumber].setInputType(InputType.TYPE_CLASS_NUMBER);
            if (values.equals("0") || values.equals("-1"))
                editText[positionNumber].setText("");
            else
                editText[positionNumber].setText(values);
        }


        return editText[positionNumber];
    }

    //InputFilder
    private void getInputFilter(int positionNumber) {
        if (inputFilters != null && inputFilters.size() > 0) {
            InputFilter[] stockArr = new InputFilter[inputFilters.size()];
            stockArr = inputFilters.toArray(stockArr);
            editText[positionNumber].setFilters(stockArr);
            if (inputFilters.size() == 2)
                editText[positionNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }


    private LinearLayout getEditTextView(final int mNumber, String MName, String textValue, int editStyle) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL,
                getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout secondlayout = createLinearLayout();

        String mConfigCode = profileConfig.get(mNumber).getConfigCode();

        if (!comparConfigerCode(mConfigCode, PROFILE_78) ||
                !comparConfigerCode(mConfigCode, PROFILE_81) ||
                !comparConfigerCode(mConfigCode, PROFILE_61)) {   /*Email, PenNumber,GST*/
            //regex
            addLengthFilter(profileConfig.get(mNumber).getRegex());
            checkRegex(profileConfig.get(mNumber).getRegex());
        }
        if (comparConfigerCode(mConfigCode, PROFILE_81)) {  /*PanNumber*/
            addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkPANRegex(mNumber);
        }

        if (comparConfigerCode(mConfigCode, PROFILE_61)) {   /*GST*/
            addLengthFilter(profileConfig.get(mNumber).getRegex());
            //checkGSTRegex(mNumber);
        }

      /*  STORENAME,ADDRESS1,ADDRESS2,ADDRESS3,RetailerAddressCity,RFiled1,RField2
         Contract Type,RField4,RFIELD5,RFIELD6,RFIELD7,STATE,PINCODE,GSTN Number
         pan_number,FOOD_LICENCE_NUM,DRUG_LICENSE_NUM,Email,REGION,COUNTRY*/
        TextInputLayout editTextInputLayout;
        if (comparConfigerCode(mConfigCode, PROFILE_02) || comparConfigerCode(mConfigCode, PROFILE_03) ||
                comparConfigerCode(mConfigCode, PROFILE_04) || comparConfigerCode(mConfigCode, PROFILE_05) ||
                comparConfigerCode(mConfigCode, PROFILE_39) || comparConfigerCode(mConfigCode, PROFILE_20) ||
                comparConfigerCode(mConfigCode, PROFILE_26) || comparConfigerCode(mConfigCode, PROFILE_27) ||
                (comparConfigerCode(mConfigCode, PROFILE_28) && profileConfig.get(mNumber).getHasLink() == 0) ||
                (comparConfigerCode(mConfigCode, PROFILE_53) && profileConfig.get(mNumber).getHasLink() == 0) ||
                (comparConfigerCode(mConfigCode, PROFILE_54) && profileConfig.get(mNumber).getHasLink() == 0) ||
                (comparConfigerCode(mConfigCode, PROFILE_55) && profileConfig.get(mNumber).getHasLink() == 0) ||
                comparConfigerCode(mConfigCode, PROFILE_40) || comparConfigerCode(mConfigCode, PROFILE_38) ||
                comparConfigerCode(mConfigCode, PROFILE_61) || comparConfigerCode(mConfigCode, PROFILE_81) ||
                comparConfigerCode(mConfigCode, PROFILE_82) || comparConfigerCode(mConfigCode, PROFILE_84) ||
                comparConfigerCode(mConfigCode, PROFILE_78) || comparConfigerCode(mConfigCode, PROFILE_87) ||
                comparConfigerCode(mConfigCode, PROFILE_88)) {

            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, MName, textValue));
            editText[mNumber].addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void afterTextChanged(Editable et) {
                    String s = et.toString();
                    if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                        s = s.toUpperCase();
                        editText[mNumber].setText(s);
                        editText[mNumber].setSelection(editText[mNumber].length());
                    }
                }
            });


            //if  Email
            if (comparConfigerCode(mConfigCode, PROFILE_78) && profileConfig.get(mNumber).getMandatory() == 1) {

                LinearLayout emailLayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 10);

                LinearLayout.LayoutParams emailParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                emailParam.weight = 7;

                LinearLayout.LayoutParams verifyButtonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                verifyButtonParams.weight = 3;
                verifyButtonParams.setMargins(0, 0, 0, 2);
                verifyButtonParams.gravity = Gravity.BOTTOM;

                emailLayout.addView(editTextInputLayout, emailParam);

                Button verifyBtn = new Button(getActivity());
                verifyBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                verifyBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                verifyBtn.setText(getResources().getString(R.string.verify));
                verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        verifyOTP("EMAIL", editText[mNumber].getText().toString());
                    }
                });

                emailLayout.addView(verifyBtn, verifyButtonParams);
                linearlayout.addView(emailLayout, weight1);
            } else
                linearlayout.addView(editTextInputLayout, weight1);

        }


        /*ContactNumber,PHNO1,PHNO2,MOBILE,FAX*/
        if (comparConfigerCode(mConfigCode, PROFILE_30) ||
                comparConfigerCode(mConfigCode, PROFILE_10) ||
                comparConfigerCode(mConfigCode, PROFILE_12) ||
                comparConfigerCode(mConfigCode, PROFILE_79) ||
                comparConfigerCode(mConfigCode, PROFILE_86)) {

            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, MName, textValue));


            if (comparConfigerCode(mConfigCode, PROFILE_79) && profileConfig.get(mNumber).getMandatory() == 1)  /*MOBILE*/ {

                LinearLayout mobileLayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 10);

                LinearLayout.LayoutParams mobileParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                mobileParam.weight = 7;

                LinearLayout.LayoutParams mobileParam1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                mobileParam1.setMargins(0, 0, 0, 2);
                mobileParam1.weight = 3;
                mobileParam1.gravity = Gravity.BOTTOM;

                mobileLayout.addView(editTextInputLayout, mobileParam);

                Button verifyBtn = new Button(getActivity());
                verifyBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                verifyBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                verifyBtn.setText(getResources().getString(R.string.verify));
                verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        verifyOTP("MOBILE", editText[mNumber].getText().toString());
                    }
                });

                mobileLayout.addView(verifyBtn, mobileParam1);

                linearlayout.addView(mobileLayout, weight1);
            } else
                linearlayout.addView(editTextInputLayout, weight1);

        }

        //CREDITPERIOD
        if (comparConfigerCode(mConfigCode, PROFILE_25)) {
            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, MName, textValue));
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
                        if (SDUtil.convertToInt(qty) > bmodel.configurationMasterHelper.MAX_CREDIT_DAYS) {
                            //Delete the last entered number and reset the qty
                            editText[mNumber].setText(qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0");
                            Toast.makeText(getActivity(), getResources().getString(R.string.max_credit_days_allowed) + " " + bmodel.configurationMasterHelper.MAX_CREDIT_DAYS, Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
            });
            linearlayout.addView(editTextInputLayout, weight1);
        }


        if (comparConfigerCode(mConfigCode, PROFILE_09) || comparConfigerCode(mConfigCode, PROFILE_11)) {

            LinearLayout firstlayout = createLinearLayout();
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            firstlayout.addView(getSingleTextView(mNumber, MName), weight1); //TextView Title
            editTextInputLayout = new TextInputLayout(getActivity());  //Contact 1 First Name
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, getResources().getString(R.string.contact_person_first_name), textValue));
            editText[mNumber].addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void afterTextChanged(Editable et) {
                    String s = et.toString();
                    if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                        s = s.toUpperCase();
                        editText[mNumber].setText(s);
                        editText[mNumber].setSelection(editText[mNumber].length());
                    }
                }
            });

            //Contact 1 Last Name
            if (comparConfigerCode(mConfigCode, PROFILE_09)) {

                if (retailerObj.getContactLname() == null || retailerObj.getContactLname().equals("null"))
                    retailerObj.setContactLname("");

                String text = retailerObj.getContactLname();
                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME).equals(text))
                        text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME);

                editTextInputLayout1 = new TextInputLayout(getActivity());
                editTextInputLayout1.addView(getSingleEditTextView(lName1_editText_index, mConfigCode, getResources().getString(R.string.contact_person_last_name), text));
                editText[lName1_editText_index].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void afterTextChanged(Editable et) {
                        String s = et.toString();
                        if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                            s = s.toUpperCase();
                            editText[lName1_editText_index].setText(s);
                            editText[lName1_editText_index].setSelection(editText[lName1_editText_index].length());
                        }
                    }
                });
            }

            //Contact2 Last Name
            if (comparConfigerCode(mConfigCode, PROFILE_11)) {

                if (retailerObj.getContactLname2() == null || retailerObj.getContactLname2().equals("null"))
                    retailerObj.setContactLname2("");

                String text = retailerObj.getContactLname2();
                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME_2) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME_2).equals(text))
                        text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(LNAME_2);

                editTextInputLayout2 = new TextInputLayout(getActivity());
                editTextInputLayout2.addView(getSingleEditTextView(lName2_editText_index, mConfigCode, getResources().getString(R.string.contact_person_last_name), text));
                editText[lName2_editText_index].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void afterTextChanged(Editable et) {
                        String s = et.toString();
                        if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                            s = s.toUpperCase();
                            editText[lName2_editText_index].setText(s);
                            editText[lName2_editText_index].setSelection(editText[lName2_editText_index].length());
                        }
                    }
                });
            }


            if (comparConfigerCode(mConfigCode, PROFILE_09) && is_contact_title1) {

                contactTitleSpinner1 = new MaterialSpinner(getActivity());
                contactTitleSpinner1.setId(mNumber);
                contactTitleSpinner1.setAdapter(contactTitleAdapter);

                editTextInputLayout3 = new TextInputLayout(getActivity());
                editTextInputLayout3.addView(getSingleEditTextView(other1_editText_index, mConfigCode, "Title", ""));
                editText[other1_editText_index].addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void afterTextChanged(Editable et) {
                        String s = et.toString();
                        if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                            s = s.toUpperCase();
                            editText[other1_editText_index].setText(s);
                            editText[other1_editText_index].setSelection(editText[other1_editText_index].length());
                        }
                    }
                });

                mcontact_title1_lovId = retailerObj.getContact1_titlelovid();
                mcontact_title1_text = retailerObj.getContact1_title();

                if (retailerObj.getContact1_titlelovid() == null || retailerObj.getContact1_titlelovid().equals("null"))
                    retailerObj.setContact1_titlelovid("");

                if (retailerObj.getContact1_title() == null || retailerObj.getContact1_title().equals("null"))
                    retailerObj.setContact1_title("");

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_41) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_41).equals(mcontact_title1_lovId))
                        mcontact_title1_lovId = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_41);

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_1_TITLE) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_1_TITLE).equals(mcontact_title1_text))
                        mcontact_title1_text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_1_TITLE);

                if (mcontact_title1_lovId != null)
                    if (mcontact_title1_lovId.equals("0") && !mcontact_title1_text.equals("0") && mcontact_title1_text.length() > 0) {
                        contactTitleSpinner1.setSelection(mcontactTitleList.size() - 1);
                        editTextInputLayout3.setVisibility(View.VISIBLE);
                        editText[other1_editText_index].setText(mcontact_title1_text);
                    } else if (mcontact_title1_lovId.equals("0"))
                        contactTitleSpinner1.setSelection(0);
                    else {
                        int selected_pos = 0;
                        for (int i = 0; i < mcontactTitleList.size(); i++) {
                            if (mcontact_title1_lovId.equals("" + mcontactTitleList.get(i).getListId()))
                                selected_pos = i;
                        }
                        contactTitleSpinner1.setSelection(selected_pos);
                    }

                contactTitleSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                        if (tempBo.getListId() == 0) {
                            editTextInputLayout3.setVisibility(View.VISIBLE);
                            editText[other1_editText_index].requestFocus();
                            mcontact_title1_lovId = "0";
                        } else if (tempBo.getListId() == -1) {
                            editTextInputLayout3.setVisibility(View.GONE);
                            mcontact_title1_lovId = "0";
                            mcontact_title1_text = "0";
                        } else {
                            editTextInputLayout3.setVisibility(View.GONE);
                            mcontact_title1_lovId = "" + tempBo.getListId();
                            mcontact_title1_text = "0";
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                secondlayout.addView(contactTitleSpinner1, params3);
                //editTextInputLayout1.addView(editText[other1_editText_index]);
                secondlayout.addView(editTextInputLayout3, params4);
            }


            if (comparConfigerCode(mConfigCode, PROFILE_11) && is_contact_title2) {

                contactTitleSpinner2 = new MaterialSpinner(getActivity());
                contactTitleSpinner2.setId(mNumber);
                contactTitleSpinner2.setAdapter(contactTitleAdapter);

                //contact title other edit text
                editTextInputLayout4 = new TextInputLayout(getActivity());
                editTextInputLayout4.addView(getSingleEditTextView(other2_editText_index, mConfigCode, "Title", ""));
                editText[other2_editText_index].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void afterTextChanged(Editable et) {
                        String s = et.toString();
                        if (bmodel.configurationMasterHelper.IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                            s = s.toUpperCase();
                            editText[other2_editText_index].setText(s);
                            editText[other2_editText_index].setSelection(editText[other2_editText_index].length());
                        }
                    }
                });

                mcontact_title2_lovId = retailerObj.getContact2_titlelovid();
                mcontact_title2_text = retailerObj.getContact2_title();

                if (retailerObj.getContact2_titlelovid() == null || retailerObj.getContact2_titlelovid().equals("null")) {
                    retailerObj.setContact2_titlelovid("");
                }

                if (retailerObj.getContact2_title() == null || retailerObj.getContact2_title().equals("null")) {
                    retailerObj.setContact2_title("");
                }

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_42) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_42).equals(mcontact_title2_lovId))
                        mcontact_title2_lovId = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_42);

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_2_TITLE) != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_2_TITLE).equals(mcontact_title2_text))
                        mcontact_title2_text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(CT_2_TITLE);

                if (mcontact_title2_lovId != null)
                    if (mcontact_title2_lovId.equals("0") && !mcontact_title2_text.equals("0") && mcontact_title2_text.length() > 0) {
                        contactTitleSpinner2.setSelection(mcontactTitleList.size() - 1);
                        editTextInputLayout4.setVisibility(View.VISIBLE);
                        editText[other2_editText_index].setText(mcontact_title2_text);
                    } else if (mcontact_title2_lovId.equals("0"))
                        contactTitleSpinner2.setSelection(0);
                    else {
                        int selected_pos = 0;
                        for (int i = 0; i < mcontactTitleList.size(); i++) {
                            if (mcontact_title2_lovId.equals("" + mcontactTitleList.get(i).getListId()))
                                selected_pos = i;
                        }
                        contactTitleSpinner2.setSelection(selected_pos);
                    }

                contactTitleSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        NewOutletBO tempBo = (NewOutletBO) parent.getSelectedItem();
                        if (tempBo.getListId() == 0) {
                            editTextInputLayout4.setVisibility(View.VISIBLE);
                            editText[other2_editText_index].requestFocus();
                            mcontact_title2_lovId = "0";
                        } else if (tempBo.getListId() == -1) {
                            editTextInputLayout4.setVisibility(View.GONE);
                            mcontact_title2_lovId = "0";
                            mcontact_title2_text = "0";
                        } else {
                            editTextInputLayout4.setVisibility(View.GONE);
                            mcontact_title2_lovId = "" + tempBo.getListId();
                            mcontact_title2_text = "0";
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });
                secondlayout.addView(contactTitleSpinner2, params3);
                secondlayout.addView(editTextInputLayout4, params4);
            }

            secondlayout.addView(editTextInputLayout, params4);

            if (comparConfigerCode(mConfigCode, PROFILE_09))
                secondlayout.addView(editTextInputLayout1, params4);
            if (comparConfigerCode(mConfigCode, PROFILE_11))
                secondlayout.addView(editTextInputLayout2, params4);
            linearlayout.addView(firstlayout, weight2);
            linearlayout.addView(secondlayout, weight2);

        }
        return linearlayout;

    }

    private void addLengthFilter(String regex) {
        inputFilters = new ArrayList<>();
        InputFilter fil = new InputFilter.LengthFilter(25);
        if (regex != null && !regex.isEmpty()) {
            if (regex.contains("<") && regex.contains(">")) {
                String len = regex.substring(regex.indexOf("<") + 1, regex.indexOf(">"));
                if (!len.isEmpty()) {
                    if (len.contains(",")) {
                        try {
                            fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len.split(",")[1]));
                        } catch (Exception ex) {
                            Commons.printException("regex length split", ex);
                        }
                    } else {
                        fil = new InputFilter.LengthFilter(SDUtil.convertToInt(len));
                    }
                }
            }
        }

        inputFilters.add(fil);
    }

    private void checkRegex(String regex) {
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

    private LinearLayout getSpinnerView(int mNumber, String MName, @NonNls String menuCode, int id) {

        LinearLayout layout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));
        LinearLayout.LayoutParams spinweight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinweight.weight = 1;
        spinweight.gravity = Gravity.CENTER;

        if (menuCode.equals(PROFILE_06)) {
            channel = new MaterialSpinner(getActivity());
            channel.setId(mNumber);
            channel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            channel.setFloatingLabelText(MName);
            //channel.setGravity(Gravity.CENTER);

            layout.addView(channel, spinweight);

            ArrayAdapter<ChannelBO> channelAdapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_spinner_item);
            channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            channelAdapter.add(new ChannelBO(0, getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));
            int position = 0, setPos = 0;
            int channelID = id;

            if (channelMaster != null)
                for (ChannelBO temp : bmodel.channelMasterHelper.getChannelMaster()) {
                    channelAdapter.add(temp);
                    if (temp.getChannelId() == channelID)
                        setPos = position + 1;
                    position++;
                }
            channel.setAdapter(channelAdapter);
            channel.setSelection(setPos, false);
            channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    ChannelBO tempBo = (ChannelBO) parent.getSelectedItem();
                    if (subchannel != null)
                        loadsubchannel(tempBo.getChannelId());
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

        }

        if (menuCode.equals(PROFILE_43)) {
            int selected_pos = 0;
            try {
                contractSpinner = new MaterialSpinner(getActivity());
                contractSpinner.setId(mNumber);
                contractSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                contractSpinner.setFloatingLabelText(MName);
                mcontractStatusList = new ArrayList<>();
                mcontractStatusList.add(0, new NewOutletBO(0, getResources().getString(R.string.select_str) + " " + MName));
                mcontractStatusList.addAll(bmodel.newOutletHelper.getContractStatusList());
                ArrayAdapter<NewOutletBO> contractStatusAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontractStatusList);
                contractStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                contractSpinner.setAdapter(contractStatusAdapter);
                contractSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                for (int i = 0; i < mcontractStatusList.size(); i++) {
                    if (id == mcontractStatusList.get(i).getListId())
                        selected_pos = i;

                }
                contractSpinner.setSelection(selected_pos);

                layout.addView(contractSpinner, spinweight);

            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        if (menuCode.equals(PROFILE_07)) {
            subchannel = new MaterialSpinner(getActivity());
            subchannel.setId(mNumber);
            subchannel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            subchannel.setFloatingLabelText(MName);
            layout.addView(subchannel, spinweight);

        } else if (menuCode.equals(PROFILE_13)) {
            try {
                location1 = new MaterialSpinner(getActivity());
                location1.setId(mNumber);
                location1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location1.setFloatingLabelText(MName);
                //location1.setGravity(Gravity.CENTER);
                if (mLocationMasterList1 == null) {
                    mLocationMasterList1 = new ArrayList<LocationBO>();
                }
                mLocationMasterList1.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));

                locationAdapter1 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList1);

                String loc1id = "";
                int pos = 0, setPos = 0;
                String[] loc1 = bmodel.mRetailerHelper.getParentLevelName(locid, false);

                if (loc1 != null) {
                    loc1id = loc1[0];
                }
                for (LocationBO loc : mLocationMasterList1) {
                    if (loc.getLocId() == SDUtil.convertToInt(loc1id)) {
                        setPos = pos;
                    }
                    pos++;
                }
                location1.setAdapter(locationAdapter1);
                location1.setSelection(setPos);
                location1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                layout.addView(location1, spinweight);

            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equals(PROFILE_14)) {
            try {
                location2 = new MaterialSpinner(getActivity());
                location2.setId(mNumber);
                location2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location2.setFloatingLabelText(MName);
                //location2.setGravity(Gravity.CENTER);
                if (mLocationMasterList2 == null) {
                    mLocationMasterList2 = new ArrayList<LocationBO>();
                }
                mLocationMasterList2.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));
                locationAdapter2 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList2);
                String loc2id = "";
                int pos = 0, setPos = 0;
                String[] loc2 = bmodel.mRetailerHelper
                        .getParentLevelName(locid, true);

                if (loc2 != null) {
                    loc2id = loc2[0];
                }
                for (LocationBO loc : mLocationMasterList2) {
                    if (loc.getLocId() == SDUtil.convertToInt(loc2id)) {
                        setPos = pos;
                    }
                    pos++;
                }
                location2.setAdapter(locationAdapter2);
                location2.setSelection(setPos);
                location2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        updateLocationAdapter1(tempBo.getLocId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
                location2.setPadding(0, 0, 0, 12);
                layout.addView(location2, spinweight);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equals(PROFILE_15)) {
            try {
                location3 = new MaterialSpinner(getActivity());
                location3.setId(mNumber);
                location3.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                location3.setFloatingLabelText(MName);
                //location3.setGravity(Gravity.CENTER);
                if (mLocationMasterList3 == null) {
                    mLocationMasterList3 = new ArrayList<LocationBO>();
                }
                mLocationMasterList3.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));

                ArrayAdapter<LocationBO> locationAdapter3 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList3);


                String locid = "";
                int pos = 0, setPos = 0;
                String[] loc3 = bmodel.mRetailerHelper
                        .getParentLevelName(loc2id, true);

                if (loc3 != null) {
                    locid = loc3[0];
                }
                for (LocationBO loc : mLocationMasterList3) {
                    if (loc.getLocId() == SDUtil.convertToInt(locid)) {
                        setPos = pos;
                    }
                    pos++;
                }

                location3.setAdapter(locationAdapter3);
                location3.setSelection(setPos);
                location3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        updateLocationAdapter2(tempBo.getLocId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                layout.addView(location3, spinweight);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equalsIgnoreCase(PROFILE_53)) {
            rField5Spinner = new MaterialSpinner(getActivity());
            rField5Spinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            rField5Spinner.setId(mNumber);
            rField5Spinner.setFloatingLabelText(MName);

            ArrayAdapter<RetailerFlexBO> rField5Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField5Adapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField5Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));

            int selPos = 0;
            for (int i = 0; i < bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_5).size(); i++) {
                RetailerFlexBO retBO = bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_5).get(i);
                rField5Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }


            rField5Spinner.setAdapter(rField5Adapter);
            rField5Spinner.setSelection(selPos);
            rField5Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

            layout.addView(rField5Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(PROFILE_54)) {
            rField6Spinner = new MaterialSpinner(getActivity());
            rField6Spinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            rField6Spinner.setId(mNumber);
            rField6Spinner.setFloatingLabelText(MName);

            ArrayAdapter<RetailerFlexBO> rField6Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField6Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField6Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));

            int selPos = 0;
            for (int i = 0; i < bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_6).size(); i++) {
                RetailerFlexBO retBO = bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_6).get(i);
                rField6Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }
            rField6Spinner.setAdapter(rField6Adapter);
            rField6Spinner.setSelection(selPos);
            rField6Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {

                }

                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });
            layout.addView(rField6Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(PROFILE_55)) {
            rField7Spinner = new MaterialSpinner(getActivity());
            rField7Spinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            rField7Spinner.setId(mNumber);
            rField7Spinner.setFloatingLabelText(MName);

            ArrayAdapter<RetailerFlexBO> rField7Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField7Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField7Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));

            int selPos = 0;
            for (int i = 0; i < bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_7).size(); i++) {
                RetailerFlexBO retBO = bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_7).get(i);
                rField7Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }


            rField7Spinner.setAdapter(rField7Adapter);
            rField7Spinner.setSelection(selPos);
            rField7Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {

                }

                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });

            layout.addView(rField7Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(PROFILE_28)) {
            rField4Spinner = new MaterialSpinner(getActivity());
            rField4Spinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            rField4Spinner.setId(mNumber);
            rField4Spinner.setFloatingLabelText(MName);

            ArrayAdapter<RetailerFlexBO> rField4Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField4Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField4Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));

            int selPos = 0;
            for (int i = 0; i < bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_4).size(); i++) {
                RetailerFlexBO retBO = bmodel.newOutletHelper.downloadRetailerFlexValues(RFIELD_4).get(i);
                rField4Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }


            rField4Spinner.setAdapter(rField4Adapter);
            rField4Spinner.setSelection(selPos);
            rField4Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });
            layout.addView(rField4Spinner, spinweight);

        }

        return layout;

    }

    private void loadsubchannel(int channelid) {
        if (subchannel != null) {
            Vector items = bmodel.subChannelMasterHelper.getSubChannelMaster();

            int siz = items.size();
            if (siz == 0) {
                return;
            }
            ArrayAdapter<SpinnerBO> subchannelAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            subchannelAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subchannelAdapter.add(new SpinnerBO(0, getActivity().getResources()
                    .getString(R.string.select_str)));

            int position = 0, setPos = 0;
            int subChannelID = bmodel.getRetailerMasterBO().getSubchannelid();
            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_07) != null)
                if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_07).equals(subChannelID + ""))
                    subChannelID = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_07));
            for (int i = 0; i < siz; ++i) {
                SubchannelBO ret = (SubchannelBO) items.elementAt(i);
                if (channelid != 0) {
                    if (channelid == ret.getChannelid()) {
                        subchannelAdapter.add(new SpinnerBO(ret.getSubchannelid(),
                                ret.getSubChannelname()));
                        if (ret.getSubchannelid() == subChannelID) {
                            setPos = position + 1;
                        }
                        position++;
                    }
                }
            }
            subchannel.setAdapter(subchannelAdapter);
            subchannel.setSelection(setPos, false);
            subchannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    //SpinnerBO tempBo = (SpinnerBO) parent.getSelectedItem();

                    if (++subChannelSpinnerCount > 1)
                        addAttributeView(1);
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });
        }
    }

    private void updateLocationAdapter1(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();
        for (LocationBO locationBO : mLocationMasterList1) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }
        locationAdapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter1.getCount() > 0) {
            if (!(locationAdapter1.getItem(0)).getLocName().toLowerCase().contains("select"))
                locationAdapter1.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter1.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);

        location1.setAdapter(locationAdapter1);
    }

    private void updateLocationAdapter2(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<LocationBO>();
        for (LocationBO locationBO : mLocationMasterList2) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }
        locationAdapter2 = new ArrayAdapter<LocationBO>(getActivity(),
                android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter2 != null && locationAdapter2.getCount() > 0) {
            if (!((LocationBO) locationAdapter2.getItem(0)).getLocName().toLowerCase().contains("select"))
                locationAdapter2.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter2.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);
        location2.setAdapter(locationAdapter2);

    }


    private LinearLayout getLatlongTextView(final int mNumber, String MName, final String textvalue) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = createLinearLayout(); //TextView Title
        firstlayout.setPadding(0, 0, 0, 0);
        firstlayout.addView(getSingleTextView(mNumber, MName));


        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 2f);
        secondlayout.setPadding(0, 0, 0, 0);
        secondlayout.setGravity(Gravity.CENTER_VERTICAL);


        latlongtextview = new TextView(getActivity());
        latlongtextview.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        latlongtextview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        latlongtextview.setText(textvalue);
        latlongtextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));

        latlongCameraBtn = new ImageView(getActivity());
        latlongCameraBtn.setBackgroundResource(R.drawable.ic_photo_camera);
        latlongCameraBtn.setScaleType(ImageView.ScaleType.FIT_XY);
        latlongCameraBtn.setVisibility(View.GONE);
        latlongCameraBtn.setPadding(5, 2, 5, 2);
        secondlayout.addView(latlongtextview, 0, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.7f));
        secondlayout.addView(latlongCameraBtn, 1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.1f));

        secondlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lat = LocationUtil.latitude + "";
                longitude = LocationUtil.longitude + "";
                onMapViewClicked();
            }
        });

        latlongCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto(retailerObj, true);
            }
        });

        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight2);
        return linearlayout;

    }

    private LinearLayout getPriorityProductView(final int mNumber, final String MName, final String textvalue, final String productID) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = createLinearLayout();
        firstlayout.setPadding(0, 0, 0, 12);
        firstlayout.addView(getSingleTextView(mNumber, MName)); //TextView

        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0);
        secondlayout.setPadding(0, 0, 0, 12);

        priorityproducttextview = new TextView(getActivity());
        priorityproducttextview.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        priorityproducttextview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        priorityproducttextview.setText(textvalue);


        secondlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPriorityProductList != null) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    @NonNls CustomFragment dialogFragment = new CustomFragment();
                    @NonNls Bundle bundle = new Bundle();
                    bundle.putString("title", MName);
                    bundle.putString("screentitle", MName);
                    bundle.putInt("hasLink", 0);
                    bundle.putString("productID", productID);
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(fm, "Sample Fragment");
                    dialogFragment.setCancelable(false);
                } else {
                    //  Toast.makeText(getActivity(), getResources().getString(R.string.priority_products_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        });


        priorityproducttextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));
        secondlayout.addView(priorityproducttextview);

        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight1);


        return linearlayout;

    }


    private LinearLayout getNearByRetailerView(int mNumber, String MName, boolean isEditMode) {
        if (bmodel.getNearByRetailers() != null)
            bmodel.getNearByRetailers().clear();
        LinearLayout layout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = createLinearLayout();
        firstlayout.addView(getSingleTextView(mNumber, MName), weight1);
        textview[mNumber].setTextColor(Color.BLACK);

        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0);

        Button retailerButton = new Button(getActivity());
        retailerButton.setText(R.string.edit);
        retailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vector<RetailerMasterBO> retailersList = bmodel.newOutletHelper.getLinkRetailerListByDistributorId().get(retailerObj.getDistributorId());
                if (retailersList != null
                        && retailersList.size() > 0) {
                    NearByRetailerDialog dialog = new NearByRetailerDialog(getActivity(), bmodel, retailersList, mSelectedIds);
                    dialog.show();
                    dialog.setCancelable(false);
                }
            }
        });


        nearbyTextView = new TextView(getActivity());
        nearbyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        nearbyTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        nearbyTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        nearbyTextView.setGravity(Gravity.CENTER);

        ScrollView scrl = new ScrollView(getActivity());
        LinearLayout lyt = createLinearLayout();
        lyt.addView(nearbyTextView, weight1);
        scrl.addView(lyt, weight1);

        secondlayout.addView(scrl, weight1);
        secondlayout.addView(retailerButton, weight3);
        layout.addView(firstlayout, weight2);
        layout.addView(secondlayout, weight1);


        if (!isEditMode) {
            retailerButton.setVisibility(View.GONE);
            // mSelectedIds = mNearbyRetIds;
            mSelectedIds = getNearbyRetailerIds();
        } else {

            bmodel.newOutletHelper.getNearbyRetailersEditRequest(SDUtil.convertToInt(bmodel.getRetailerMasterBO().getRetailerID()));

            if (bmodel.newOutletHelper.getLstEditRequests().size() > 0) {
                ArrayList<String> tempIds = new ArrayList<>();
                for (String retId : bmodel.newOutletHelper.getLstEditRequests().keySet()) {
                    if (bmodel.newOutletHelper.getLstEditRequests().get(retId).equals("N")) {
                        tempIds.add(retId);
                    }
                }
                mSelectedIds.clear();
                for (String retId : tempIds) {
                    for (RetailerMasterBO bo : bmodel.newOutletHelper.getLinkRetailerList()) {
                        if (bo.getRetailerID().equals(retId + "")) {
                            mSelectedIds.add(bo);
                        }
                    }
                }
            } else
                mSelectedIds = getNearbyRetailerIds();
            //mSelectedIds = mNearbyRetIds;
        }
        // showing nearby retailers
        for (RetailerMasterBO bo : mSelectedIds) {
            nearbyTextView.setText(nearbyTextView.getText() + DataMembers.CR1 + bo.getRetailerName());
        }


        return layout;
    }

    //To create layout for Retailer Attribute
    private LinearLayout addAttributeView(int flag) {
        @NonNls LinearLayout parentLayout = null;
        try {
            //flag=0 - add common atrributes and attributes for current(from DB) channel
            // flag==1 - add new(if user changing the channel, then corresponding attributes loaded) channel attributes
            boolean isCommon = false, isFromChannel = false;
            if (flag == 0)
                isCommon = true;
            else if (flag == 1)
                isFromChannel = true;

            boolean isNewChannel = false;

            //Params
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            innerParams.setMargins(0, 0, 10, 0);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(3, 0, 0, 0);


            ArrayList<Integer> mCommonAttributeList = null;// common attributes
            ArrayList<Integer> mChannelAttributeList = null;// attributes for selected channel already(from DB)..
            ArrayList<Integer> mNewChannelAttributeList = null;// Newly selected channel's attribute

            if (isCommon) {

                // creating parent layout for attributes
                parentLayout = new LinearLayout(getActivity());
                parentLayout.setTag("attributeLayout");
                parentLayout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout titleLayout = new LinearLayout(getActivity());
                titleLayout.setOrientation(LinearLayout.HORIZONTAL);

                // title
                TextView titleTV = new TextView(getActivity());
                titleTV.setText(getResources().getString(R.string.attribute));
                titleTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_medium));
                titleTV.setTextColor(Color.BLACK);
                titleLayout.addView(titleTV, titleParams);

                parentLayout.addView(titleLayout, LLParams);

                // adding common attributes
                mCommonAttributeList = new ArrayList<>();
                if (bmodel.newOutletAttributeHelper.getmCommonAttributeList() != null)
                    mCommonAttributeList.addAll(bmodel.newOutletAttributeHelper.getmCommonAttributeList());

                // attributes mapped to channel already are added here
                if (isChannelAvailable()) {
                    mChannelAttributeList = new ArrayList<>();
                    int subChannelID;
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_07) != null)
                        subChannelID = SDUtil.convertToInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(PROFILE_07));
                    else subChannelID = bmodel.getRetailerMasterBO().getSubchannelid();
                    mChannelAttributeList.addAll(mAttributeListByChannelId.get(subChannelID));
                }

            } else if (isFromChannel) {

//                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE07") != null
//                        && (Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE07")) == ((SpinnerBO) subchannel.getSelectedItem()).getId())) {
//                    isNewChannel = false;
//                } else if (((SpinnerBO) subchannel.getSelectedItem()).getId() != bmodel.getRetailerMasterBO().getSubchannelid()) {
                // in case of user selecting new sub channel.. then view wil be updated here..
                isNewChannel = true;

                // getting existing attribute layout and clearig childs for loading attributes of current channel
                parentLayout = (LinearLayout) getView().findViewWithTag("attributeLayout");
                if (parentLayout != null) {
                    for (int i = 0; i < parentLayout.getChildCount(); i++) {
                        if (parentLayout.getChildAt(i).getTag() != null && ((String) parentLayout.getChildAt(i).getTag()).equals("channel"))
                            parentLayout.removeViewAt(i);

                    }
                }

                // getting newly selected channel's attribute
                mNewChannelAttributeList = new ArrayList<>();
                if (mAttributeListByChannelId != null && mAttributeListByChannelId.get(((SpinnerBO) subchannel.getSelectedItem()).getId()) != null)
                    mNewChannelAttributeList.addAll(mAttributeListByChannelId.get(((SpinnerBO) subchannel.getSelectedItem()).getId()));

//                }

            }

            spinnerHashMap = new HashMap<>();
            spinnerAdapterMap = new HashMap<>();
            if (isFromChannel && isNewChannel) {

                // User selected a sub channel an it is new one.
                for (int i = 0; i < bmodel.newOutletAttributeHelper.getAttributeParentList().size(); i++) {

                    final NewOutletAttributeBO parentBO;
                    parentBO = bmodel.newOutletAttributeHelper.getAttributeParentList().get(i);

                    if (mNewChannelAttributeList.contains(parentBO.getAttrId())) {

                        @NonNls LinearLayout layout = new LinearLayout(getActivity());

                        // setting tag as channel, used to remove channel views particularly and update new one if channel changed
                        layout.setTag("channel");

                        layout.setOrientation(LinearLayout.HORIZONTAL);
                        layout.setGravity(Gravity.CENTER_VERTICAL);
                        layout.setWeightSum(3f);
                        layout.setLayoutParams(LLParams);

                        //Attribute label
                        final String attribName = parentBO.getAttrName();
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText(attribName);
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.BLACK);
                        mn_textview.setLayoutParams(paramsAttrib);
                        layout.addView(mn_textview);

                        final int columnCount = getLevel(parentBO.getAttrId());
                        MaterialSpinner spinner;
                        LinearLayout innerLL = new LinearLayout(getActivity());
                        innerLL.setOrientation(LinearLayout.VERTICAL);
                        innerLL.setLayoutParams(paramsAttribSpinner);
                        LinearLayout innerHL = new LinearLayout(getActivity());
                        innerHL.setWeightSum(2);
                        innerHL.setLayoutParams(LLParams);
                        innerHL.setOrientation(LinearLayout.HORIZONTAL);
                        boolean isAdded = false;
                        //  ArrayList<Integer> indexList = attributeIndexMap.get(attribName);

                        //creating child levels
                        for (int j = 0; j < columnCount; j++) {
                            isAdded = false;
                            final ArrayList<NewOutletAttributeBO> attrbList;
                            final int index = j;
                            //   final int selectedPos = indexList.get(j);
                            spinner = new MaterialSpinner(getActivity());

                            attrbList = new ArrayList<>();
                            attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                    .getString(R.string.select_str) + " " + getActivity().getResources()
                                    .getString(R.string.attribute)));
                            attrbList.addAll(attribMap.get(attribName));

                            final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.simple_spinner_item, attrbList);
                            spinner.setAdapter(arrayAdapter);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setLayoutParams(innerParams);
                            innerHL.addView(spinner);
                            spinnerAdapterMap.put(attribName + index, arrayAdapter);
                            //  spinner.setSelection(selectedPos + 1);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (++check > spinnerCount) {
                                        selectedAttribList.remove(parentBO.getAttrId());
                                        selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));

                                        if (index < columnCount)
                                            loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
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
            } else if (isCommon) {
                //Call while creating view first time
                //Call while creating a  view

                if (attributeList.size() > 0) {
                    // There is a attribute for current retailer..

                    ArrayList<Integer> mAddedCommonAttributeList = new ArrayList<>();
                    int rowCount = attributeList.size();
                    updateRetailerAttribute(attributeList);

                    // Even if there is a record for current retailer.. we should load common attributes in the view,
                    // so that user can add new attribute for current retailer
                    for (NewOutletAttributeBO newOutletAttributeBO : attributeHeaderList) {
                        if (mCommonAttributeList.contains(newOutletAttributeBO.getAttrId()))
                            mAddedCommonAttributeList.add(newOutletAttributeBO.getAttrId());
                    }
                    prepareCommonAttributeView(mCommonAttributeList, parentLayout, mAddedCommonAttributeList);
                    //

                    for (int i = 0; i < rowCount; i++) {

                        final NewOutletAttributeBO parentBO;
                        parentBO = attributeHeaderList.get(i);
                        //Allowing only if parent attribute is available in common list or channel's(from Db) attribute

                        // assert mChannelAttributeList != null;
                        if ((isCommon && (mCommonAttributeList.contains(parentBO.getAttrId()) || (mChannelAttributeList != null && mChannelAttributeList.contains(parentBO.getAttrId()))))) {

                            @NonNls LinearLayout layout = new LinearLayout(getActivity());
                            // setting tag as channel, used to remove channel views particularly and update new one if channel changed
                            if (mChannelAttributeList != null && mChannelAttributeList.contains(parentBO.getAttrId()))
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
                            final int columnCount = getLevel(parentBO.getAttrId());
                            MaterialSpinner spinner;
                            LinearLayout innerLL = new LinearLayout(getActivity());
                            innerLL.setOrientation(LinearLayout.VERTICAL);
                            innerLL.setLayoutParams(paramsAttribSpinner);
                            LinearLayout innerHL = new LinearLayout(getActivity());
                            innerHL.setWeightSum(2);
                            innerHL.setLayoutParams(LLParams);
                            innerHL.setOrientation(LinearLayout.HORIZONTAL);
                            boolean isAdded = false;
                            ArrayList<Integer> indexList = attributeIndexMap.get(attribName);
                            //creating child levels
                            for (int j = 0; j < columnCount; j++) {

                                isAdded = false;
                                final ArrayList<NewOutletAttributeBO> attrbList;
                                final int index = j;
                                final int selectedPos = indexList.get(j);
                                spinner = new MaterialSpinner(getActivity());
                                attrbList = new ArrayList<>();
                                attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                        .getString(R.string.select_str) + " " + getActivity().getResources()
                                        .getString(R.string.attribute)));
                                attrbList.addAll(listHashMap.get(attribName).get(j));
                                final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                        android.R.layout.simple_spinner_item, attrbList);
                                spinner.setAdapter(arrayAdapter);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setLayoutParams(innerParams);
                                innerHL.addView(spinner);
                                spinnerAdapterMap.put(attribName + index, arrayAdapter);
                                spinner.setSelection(selectedPos + 1);
                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (++check > spinnerCount) {
                                            selectedAttribList.remove(parentBO.getAttrId());
                                            selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));
                                            if (index < columnCount)
                                                loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
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
                } else {
                    // No attributes for current retailer..
                    // so just adding common attributes(Because user can add attribute for current retailer from edit screen).
                    prepareCommonAttributeView(mCommonAttributeList, parentLayout, new ArrayList<Integer>());
                }


            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return parentLayout;
    }


    private void prepareCommonAttributeView(ArrayList<Integer> mCommonAttributeList
            , LinearLayout parentLayout, ArrayList<Integer> mAddedCommonAttributeList) {

        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        int rowCount = bmodel.newOutletAttributeHelper.getAttributeParentList().size();
        selectedAttribList = new HashMap<>();
        for (int i = 0; i < rowCount; i++) {

            final NewOutletAttributeBO parentBO = bmodel.newOutletAttributeHelper.getAttributeParentList().get(i);
            if (mCommonAttributeList.contains(parentBO.getAttrId())
                    && !mAddedCommonAttributeList.contains(parentBO.getAttrId())) {

                LinearLayout layout = new LinearLayout(getActivity());
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
                final int columnCount = getLevel(parentBO.getAttrId());
                MaterialSpinner spinner;
                LinearLayout innerLL = new LinearLayout(getActivity());
                innerLL.setOrientation(LinearLayout.VERTICAL);
                innerLL.setLayoutParams(paramsAttribSpinner);
                LinearLayout innerHL = new LinearLayout(getActivity());
                innerHL.setWeightSum(2);
                innerHL.setLayoutParams(LLParams);
                innerHL.setOrientation(LinearLayout.HORIZONTAL);
                boolean isAdded = false;
                // ArrayList<Integer> indexList = attributeIndexMap.get(attribName);
                //creating child levels
                for (int j = 0; j < columnCount; j++) {
                    isAdded = false;
                    final ArrayList<NewOutletAttributeBO> attrbList;
                    final int index = j;
                    //final int selectedPos = indexList.get(j);
                    spinner = new MaterialSpinner(getActivity());
                    attrbList = new ArrayList<>();
                    attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                            .getString(R.string.select_str) + " " + getActivity().getResources()
                            .getString(R.string.attribute)));
                    attrbList.addAll(bmodel.newOutletAttributeHelper.getAttribMap().get(attribName));
                    final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item, attrbList);
                    spinner.setAdapter(arrayAdapter);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setLayoutParams(innerParams);
                    innerHL.addView(spinner);
                    spinnerAdapterMap.put(attribName + index, arrayAdapter);
                    // spinner.setSelection(selectedPos + 1);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (++check > spinnerCount) {
                                selectedAttribList.remove(parentBO.getAttrId());
                                selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));
                                if (index < columnCount)
                                    loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
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
    }


    // to check sub channel is available or not
    // channel may be mapped in any sequance, so its availbily identified using iteration
    private boolean isChannelAvailable() {
        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE06")) {
                return true;
            }
        }
        return false;
    }


    private View getImageView() {
        View v; // Creating an instance for View Object
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        v = inflater.inflate(R.layout.profile_edit_image_view, null);
        imageView = (ImageView) v.findViewById(R.id.profile_edit_image);

        if (bmodel.profilehelper.hasProfileImagePath(retailerObj) && retailerObj.getProfileImagePath() != null && !"".equals(retailerObj.getProfileImagePath())) {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            String filePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + path;
            if (bmodel.profilehelper.isImagePresent(filePath)) {
                setImageFromCamera(retailerObj);
            } else {
                imageView.setImageResource(R.drawable.face);
            }
        } else if (retailerObj.getProfileImagePath() != null && !"".equals(retailerObj.getProfileImagePath())) {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            File imgFile = new File(getActivity().getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS)
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DataMembers.DIGITAL_CONTENT
                    + "/"
                    + DataMembers.PROFILE + "/"
                    + path);
            if (imgFile.exists()) {
                setProfileImage();
            } else {
                imageView.setImageResource(R.drawable.face);
            }
        }

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Dont allow if Fun57 is enabled and mandatory,
                //Generally check for location and show toast if no location found.
                if (!isLatLong && bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE
                        && (LocationUtil.latitude == 0 || LocationUtil.longitude == 0)
                        || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl != 0
                        && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)) {

                    Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (LocationUtil.latitude == 0 || LocationUtil.longitude == 0) {
                        Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                    }
                    takePhoto(retailerObj, false);
                    return false;
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cameraFilePath != null && !"".equals(cameraFilePath)) {
                    if (new File(cameraFilePath).exists()) {
                        try {
                            openImage(new File(cameraFilePath));
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.unloadimage), Toast.LENGTH_SHORT).show();
                    }
                } else if (bmodel.retailerMasterBO.getProfileImagePath() != null
                        && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                    File filePath = null;
                    if (bmodel.profilehelper.hasProfileImagePath(bmodel.retailerMasterBO)
                            && bmodel.retailerMasterBO.getProfileImagePath() != null
                            && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + path);
                    } else if (bmodel.retailerMasterBO.getProfileImagePath() != null && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/" + DataMembers.PROFILE + "/" + path);
                    }

                    if (filePath != null && filePath.exists()) {
                        try {
                            openImage(filePath);
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.unloadimage),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        return v;
        //  return parentLayout;
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
    private void loadAttributeSpinner(String key, int attribId) {
        MaterialSpinner spinner = spinnerHashMap.get(key);
        ArrayAdapter<NewOutletAttributeBO> adapter = spinnerAdapterMap.get(key);
        if (spinner != null && adapter != null) {
            ArrayList<NewOutletAttributeBO> arrayList = new ArrayList<>();
            arrayList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                    .getString(R.string.select_str) + " " + getActivity().getResources()
                    .getString(R.string.attribute)));
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

    @SuppressLint("UseSparseArrays")
    private void updateRetailerAttribute(ArrayList<NewOutletAttributeBO> list) {
        attributeHeaderList = new ArrayList<>();
        ArrayList<ArrayList<NewOutletAttributeBO>> attributeGroupedList;
        listHashMap = new HashMap<>();
        attributeIndexMap = new HashMap<>();
        selectedAttribList = new HashMap<>();
        ArrayList<NewOutletAttributeBO> childList = bmodel.newOutletAttributeHelper.getAttributeList();
        ArrayList<NewOutletAttributeBO> parentList = bmodel.newOutletAttributeHelper.getAttributeParentList();
        int attribID;
        int tempAttribID;
        int parentID = 0;
        int tempParentID = 0;
        String title = "";
        NewOutletAttributeBO tempBO = new NewOutletAttributeBO();
        int tempLevel;
        for (NewOutletAttributeBO attributeBO : list) {
            attributeGroupedList = new ArrayList<>();
            attributeIndexList = new ArrayList<>();
            attribID = attributeBO.getAttrId();
            for (int i = childList.size() - 1; i >= 0; i--) {
                NewOutletAttributeBO attributeBO1 = childList.get(i);
                tempAttribID = attributeBO1.getAttrId();
                if (attribID == tempAttribID) {
                    tempBO = attributeBO1;
                    tempParentID = attributeBO1.getParentId();
                    attributeGroupedList.add(getAttributeGroupedList(tempParentID, childList, tempAttribID));
                    continue;
                }
                if (tempAttribID == tempParentID) {
                    tempParentID = attributeBO1.getParentId();
                    attributeGroupedList.add(getAttributeGroupedList(tempParentID, childList, tempAttribID));
                }
            }

            for (NewOutletAttributeBO attributeBO2 : parentList) {
                parentID = attributeBO2.getAttrId();
                tempLevel = getLevel(attributeBO2.getAttrId());
                if (tempParentID == parentID) {
                    spinnerCount += tempLevel;
                    title = attributeBO2.getAttrName();
                    attributeHeaderList.add(attributeBO2);
                }
            }
            Collections.reverse(attributeGroupedList);
            Collections.reverse(attributeIndexList);
            listHashMap.put(title, attributeGroupedList);
            attributeIndexMap.put(title, attributeIndexList);
            selectedAttribList.put(tempParentID, tempBO);
        }
    }

    private ArrayList<NewOutletAttributeBO> getAttributeGroupedList(int parentID, ArrayList<NewOutletAttributeBO> list, int attribID) {
        ArrayList<NewOutletAttributeBO> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NewOutletAttributeBO obj = list.get(i);
            if (parentID == obj.getParentId()) {
                arrayList.add(obj);
            }
        }

        for (int i = 0; i < arrayList.size(); i++) {
            NewOutletAttributeBO obj = arrayList.get(i);
            if (attribID == obj.getAttrId())
                attributeIndexList.add(i);
        }
        return arrayList;
    }

    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        nearbyTextView.setText("");
        for (RetailerMasterBO bo : list) {
            nearbyTextView.setText(nearbyTextView.getText() + DataMembers.CR1 + bo.getRetailerName());
        }
        mSelectedIds = list;
    }


    public void onMapViewClicked() {
        @NonNls Intent in;
        int REQUEST_CODE = 100;
        if (bmodel.configurationMasterHelper.IS_BAIDU_MAP)
            in = new Intent(getActivity(), BaiduMapDialogue.class);
        else
            in = new Intent(getActivity(), MapDialogue.class);

        double latdoub = Double.valueOf(lat);
        double longdoub = Double.valueOf(longitude);

        in.putExtra("lat", latdoub);
        in.putExtra("lon", longdoub);
        startActivityForResult(in, REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNls Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                if (bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                    lat = LocationUtil.latitude + "";
                    longitude = LocationUtil.longitude + "";

                    if (lat.equals("") || SDUtil.convertToDouble(lat) == 0 || longitude.equals("") || SDUtil.convertToDouble(longitude) == 0) {
                        Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                    } else {
                        if (!isLatLong) {
                            profileConfig.add(new ConfigureBO(PROFILE_08, "Latitude", lat, 0, 0, 0));
                            profileConfig.add(new ConfigureBO(PROFILE_31, "Latitude", longitude, 0, 0, 0));
                        } else {
                            if (latlongtextview != null)
                                latlongtextview.setText(lat + "," + longitude);
                        }
                        Toast.makeText(getActivity(), "Location captured successfully.", Toast.LENGTH_LONG).show();
                    }
                }

                Uri uri = bmodel.profilehelper.getUriFromFile(HomeScreenFragment.photoPath + "/" + imageFileName);
                cameraFilePath = HomeScreenFragment.photoPath + "/" + imageFileName;
                imageView.setImageDrawable(null);
                imageView.invalidate();
                imageView.setImageURI(uri);
                imageView.refreshDrawableState();
            }
        }
        if (requestCode == LATLONG_CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                //String latlongCameraFilePath = HomeScreenFragment.photoPath + "/" + bmodel.latlongImageFileName;
                Toast.makeText(getActivity(), "Photo Captured Successfully", Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK) {
            if (data.hasExtra("lat") && data.hasExtra("isChanged")) {

                lat = data.getExtras().getDouble("lat") + "";
                longitude = data.getExtras().getDouble("lon") + "";
                if (data.getExtras().getBoolean("isChanged")) {
                    latlongtextview.setText(lat + ", " + longitude);
                    if (isLatLongCameravailable) {
                        latlongCameraBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    @Override
    public void generateOTP() {
        verifyOtpAsyncTask("", "");
    }

    @Override
    public void dismissListener(@NonNls String type, boolean isVerfied) {
        if (isVerfied) {
            if (type.equals("MOBILE"))
                isMobileNoVerfied = true;
            if (type.equals("EMAIL"))
                isEmailVerfied = true;
        }

    }

    @SuppressLint("ValidFragment")
    public class CustomFragment extends DialogFragment {
        private String mTitle = "";
        private String mMenuName = "";


        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mPriorityproductLV;

        private int hasLink = 0;
        private int mSelectedpostion = -1;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
            mMenuName = getArguments().getString("screentitle");
            hasLink = getArguments().getInt("hasLink");


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            //getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().setTitle(mTitle);
            mTitleTV = (TextView) getView().findViewById(R.id.title);
            mTitleTV.setVisibility(View.GONE);
            mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
            if (hasLink == 0)
                mOkBtn.setVisibility(View.GONE);

            mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
            mDismisBtn.setVisibility(View.GONE);
            mPriorityproductLV = (ListView) getView().findViewById(R.id.lv_colletion_print);


            if (hasLink == 0) {
                for (int i = 0; i < mPriorityProductList.size(); i++) {
                    if (mPriorityProductList.get(i).getListID().equals(selectedProductID))
                        mSelectedpostion = i;
                }
                ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<StandardListBO>(getActivity(), android.R.layout.simple_list_item_single_choice, mPriorityProductList);
                mPriorityproductLV.setAdapter(adapter);
                mPriorityproductLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                if (mSelectedpostion != -1)
                    mPriorityproductLV.setItemChecked(mSelectedpostion, true);
                mPriorityproductLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedPriorityProductList = new ArrayList<StandardListBO>();
                        StandardListBO standardListBO = mPriorityProductList.get(position);
                        selectedProductID = standardListBO.getListID();
                        selectedPriorityProductList.add(standardListBO);
                        priorityproducttextview.setText(standardListBO.getListName());
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
                            null);
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
            selectedPriorityProductList = new ArrayList<>();
            selectedPriorityProductList.clear();
            StringBuffer sb = new StringBuffer();

            for (StandardListBO standardListBO : mPriorityProductList) {
                if (standardListBO.isChecked()) {

                    selectedPriorityProductList.add(standardListBO);


                    if (sb.length() > 0)
                        sb.append(", ");

                    sb.append(standardListBO.getListName());

                }
                if (selectedPriorityProductList.size() > 0) {
                    priorityproducttextview.setText(sb.toString());

                } else {
                    priorityproducttextview.setText("");
                }
            }
        }
    }


    //handled only for single selection products
    private ArrayList<StandardListBO> computeSelectedPriorityList() {
        ArrayList<StandardListBO> tempList = new ArrayList<>();
        ArrayList<String> products = bmodel.newOutletHelper.downloadPriorityProductsForRetailer(retailerObj.getRetailerID());

        if (products == null)
            products = new ArrayList<String>();

        if (selectedPriorityProductList != null) {
            for (@NonNls StandardListBO bo : selectedPriorityProductList) {
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
                        bo.setStatus(D);
                        tempList.add(bo);
                    }
                }
            }
        }

        return tempList;
    }

    private void setValues() {

        try {
            int size = profileConfig.size();
            for (int i = 0; i < size; i++) {
                @NonNls String configCode = profileConfig.get(i).getConfigCode();
                if (configCode.equals(PROFILE_02) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_03) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_04) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_05) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_30) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_06) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    ChannelBO cBo = (ChannelBO) channel.getSelectedItem();
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(cBo.getChannelId() + "");
                } else if (configCode.equals(PROFILE_07) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(((SpinnerBO) subchannel
                                .getSelectedItem()).getId() + "");
                } else if (configCode.equals(PROFILE_43) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontractStatusList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contractSpinner
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals(PROFILE_08) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(lat)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        //converting big decimal value while Exponential value occur
                        String lattiTude = (lat).contains("E")
                                ? (SDUtil.truncateDecimal(SDUtil.convertToDouble(lat), -1) + "").substring(0, 20)
                                : (lat.length() > 20 ? lat.substring(0, 20) : lat);

                        profileConfig.get(i).setMenuNumber(lattiTude);
                    }
                } else if (configCode.equals(PROFILE_31) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(longitude)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        //converting big decimal value while Exponential value occur
                        String longiTude = (longitude).contains("E")
                                ? (SDUtil.truncateDecimal(SDUtil.convertToDouble(longitude), -1) + "").substring(0, 20)
                                : (longitude.length() > 20 ? longitude.substring(0, 20) : longitude);

                        profileConfig.get(i).setMenuNumber(longiTude);
                    }
                } else if (configCode.equals(PROFILE_63) && profileConfig.get(i).getModule_Order() == 1) {
                    if (bmodel.latlongImageFileName == null || "".equals(bmodel.latlongImageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(bmodel.latlongImageFileName);
                    }
                } else if (configCode.equals(PROFILE_09) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equals(PROFILE_41) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontactTitleList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contactTitleSpinner1
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals(CT_1_TITLE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (contactTitleSpinner1.getSelectedItem().toString().toLowerCase()
                            .contains("select"))
                        profileConfig.get(i).setMenuNumber("0");
                    else if (contactTitleSpinner1.getSelectedItem().toString().equalsIgnoreCase("Others")) {
                        if (TextUtils.isEmpty(editText[other1_editText_index].getText().toString()))
                            profileConfig.get(i).setMenuNumber("0");
                        else
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[other1_editText_index].getText().toString()));
                    } else
                        profileConfig.get(i).setMenuNumber("0");

                } else if (configCode.equals(LNAME) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[lName1_editText_index].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[lName1_editText_index].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_10) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_11) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_12) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_42) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontactTitleList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contactTitleSpinner2
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals(CT_2_TITLE) && profileConfig.get(i).getModule_Order() == 1) {
                    if (contactTitleSpinner2.getSelectedItem().toString().toLowerCase()
                            .contains("select"))
                        profileConfig.get(i).setMenuNumber("0");
                    else if (contactTitleSpinner2.getSelectedItem().toString().equalsIgnoreCase("Others")) {
                        if (TextUtils.isEmpty(editText[other2_editText_index].getText().toString()))
                            profileConfig.get(i).setMenuNumber("0");
                        else
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[other2_editText_index].getText().toString()));
                    } else
                        profileConfig.get(i).setMenuNumber("0");
                } else if (configCode.equals(LNAME_2) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[lName2_editText_index].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[lName2_editText_index].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_13) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList1 != null) {
                        if (mLocationMasterList1.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(((LocationBO) location1
                                        .getSelectedItem()).getLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                } else if (configCode.equals(PROFILE_14) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList2 != null) {
                        if (mLocationMasterList2.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(((LocationBO) location2
                                        .getSelectedItem()).getLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                } else if (configCode.equals(PROFILE_15) && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mLocationMasterList3 != null) {
                        if (mLocationMasterList3.size() > 0) {
                            try {
                                profileConfig.get(i).setMenuNumber(((LocationBO) location3
                                        .getSelectedItem()).getLocId() + "");
                            } catch (Exception e) {
                                profileConfig.get(i).setMenuNumber("0");
                            }
                        }
                    }
                } else if (configCode.equals(PROFILE_39) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_40) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_25) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber(
                                editText[i].getText().toString());
                    }
                } else if (configCode.equals(PROFILE_20) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_26) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_27) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_28) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (TextUtils.isEmpty(editText[i].getText().toString())) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[i].getText().toString()));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = (RetailerFlexBO) rField4Spinner.getSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(PROFILE_53) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (TextUtils.isEmpty(editText[i].getText().toString())) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[i].getText().toString()));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = (RetailerFlexBO) rField5Spinner.getSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(PROFILE_54) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (TextUtils.isEmpty(editText[i].getText().toString())) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[i].getText().toString()));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = (RetailerFlexBO) rField6Spinner.getSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(PROFILE_55) && profileConfig.get(i).getModule_Order() == 1) {
                    if (profileConfig.get(i).getHasLink() == 0) {
                        if (TextUtils.isEmpty(editText[i].getText().toString())) {
                            profileConfig.get(i).setMenuNumber("");
                        } else {
                            profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                    editText[i].getText().toString()));
                        }
                    } else {
                        RetailerFlexBO retailerFlexBO = (RetailerFlexBO) rField7Spinner.getSelectedItem();
                        if (retailerFlexBO != null)
                            profileConfig.get(i).setMenuNumber(retailerFlexBO.getId());
                        else
                            profileConfig.get(i).setMenuNumber("0");
                    }
                } else if (configCode.equals(PROFILE_60) && profileConfig.get(i).getModule_Order() == 1) {
                    if (imageFileName == null || "".equals(imageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(imageFileName);
                    }
                } else if (configCode.equals(PROFILE_61) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals(PROFILE_62) && profileConfig.get(i).getModule_Order() == 1) {
                    if (!inSEZcheckBox.isChecked()) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber("1");
                    }
                } else if (configCode.equals(PROFILE_81) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_82) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_84) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_83) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(flExpDateTextView.getText().toString())) ||
                            flExpDateTextView.getText().toString().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(flExpDateTextView.getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_85) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(dlExpDateTextView.getText().toString())) ||
                            dlExpDateTextView.getText().toString().equalsIgnoreCase("Select Date")) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(dlExpDateTextView.getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_78) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_79) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_86) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_87) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals(PROFILE_88) && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class SaveEditAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.setNearByRetailers(mSelectedIds);
                bmodel.newOutletHelper.setSelectedPrioProducts(computeSelectedPriorityList());
                setValues();
                bmodel.newOutletHelper.updateRetailer();

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            progressDialogue.dismiss();
            bmodel.latlongImageFileName = "";
            lat = "";
            longitude = "";
            showAlert(getActivity().getResources().getString(R.string.profile_updated_scccess), 0);
        }

    }

    private void showAlert(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (idd == 0) {
                    getActivity().finish();

                }
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }


    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        int year;
        int month;
        int day;
        @NonNls
        String code;

        DatePickerFragment(String code, int year, int month, int day) {
            this.code = code;
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (selectedDate.after(Calendar.getInstance())) {
                @NonNls SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                if (code.equalsIgnoreCase("DLEXPDATE"))
                    dlExpDateTextView.setText(sdf.format(selectedDate.getTime()));

                else if (code.equalsIgnoreCase("FLEXPDATE"))
                    flExpDateTextView.setText(sdf.format(selectedDate.getTime()));
                this.year = year;
                this.day = day;
                this.month = month;
            } else {
                Toast.makeText(getActivity(), "Select future date", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            bmodel.latlongImageFileName = "";
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onVerifyOTPCompleted(Integer result, String type) {

        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
        if (result == 1) {
            if (getActivity() != null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                RetailerOTPDialog dialog1 = new RetailerOTPDialog(ProfileEditFragment.this, type);
                dialog1.setCancelable(false);
                dialog1.show(ft, "mobiledialog");
            }
        }
        if (result == -4) {
            if (!bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().
                            getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void OnDownloadTaskCompleted(Vector<ChannelBO> channelMaster,
                                        ArrayList<LocationBO> mLocationMasterList1,
                                        ArrayList<LocationBO> mLocationMasterList2,
                                        ArrayList<LocationBO> mLocationMasterList3) {
        this.channelMaster = channelMaster;
        this.mLocationMasterList1 = mLocationMasterList1;
        this.mLocationMasterList2 = mLocationMasterList2;
        this.mLocationMasterList3 = mLocationMasterList3;
        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
        createTabViewForProfileForEdit();

    }

    private void onDownloadAsyncTask() {
        setUpDialog();
        new ProfileEditDownloadTask(bmodel, this).execute();
    }

    private void verifyOtpAsyncTask(String value, String type) {
        setUpDialog();
        new ProfileEditVerifyTask(bmodel, value, type, this).execute();
    }

    private void setUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void verifyOTP(@NonNls String type, String value) {
        String otpGenerateUrl = bmodel.synchronizationHelper.generateOtpUrl();
        if (otpGenerateUrl.length() > 0) {
            switch (type) {
                case "MOBILE":
                    if (value != null && !value.isEmpty() && value.length() == 10)
                        verifyOtpAsyncTask(value, type);
                    else
                        //Toast.makeText(getActivity(), getResources().getString(R.string.invalid_mobile_number), Toast.LENGTH_LONG).show();
                        break;
                case "EMAIL":
                    if (isValidEmail(value))
                        verifyOtpAsyncTask(value, type);
                    else
                        Toast.makeText(getActivity(), getResources().getString(R.string.invalid_email_address), Toast.LENGTH_LONG).show();
                    break;
            }
        } else
            Toast.makeText(getActivity(), getResources().getString(R.string.otp_download_url_empty), Toast.LENGTH_LONG).show();
    }

    private void takePhoto(RetailerMasterBO retailerMasterBO, boolean isForLatLong) {
        if (bmodel.isExternalStorageAvailable()) {
            if (!isForLatLong) {
                imageFileName = "PRO_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            } else {
                bmodel.latlongImageFileName = "LATLONG_" + retailerMasterBO.getRetailerID() + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            }
            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), HomeScreenFragment.photoPath + "/" + ((!isForLatLong) ? imageFileName : bmodel.latlongImageFileName));
                startActivityForResult(intent, (!isForLatLong) ? CAMERA_REQUEST_CODE : LATLONG_CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(
                            R.string.unable_to_access_the_sdcard), Toast.LENGTH_SHORT).show();
        }
    }

    private void setProfileImage() {
        String[] imgPaths = retailerObj.getProfileImagePath().split("/");
        String path = imgPaths[imgPaths.length - 1];
        File imgFile = new File(getActivity().getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS) + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + DataMembers.DIGITAL_CONTENT
                + "/"
                + DataMembers.PROFILE + "/"
                + path);
        Bitmap myBitmap = bmodel.decodeFile(imgFile);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(myBitmap);
    }

    private void setImageFromCamera(RetailerMasterBO retailerObj) {
        try {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            Uri uri = bmodel.profilehelper.getUriFromFile(HomeScreenFragment.photoPath + "/" + path);
            imageView.invalidate();
            imageView.setImageURI(uri);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.face);
            Commons.printException("" + e);
        }
    }

    private void openImage(File fileName) {
        if (fileName.getAbsolutePath().trim().length() > 0) {
            Uri path;
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", fileName);
                    intent.setDataAndType(path, "image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    path = Uri.fromFile(fileName);
                    intent.setDataAndType(path, "image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                Commons.printException("" + e);
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    } //Open the Image in Photo Gallery while onClick

    private boolean validateEditProfile() {
        boolean validate = true;
        try {
            int size = profileConfig.size();
            for (int i = 0; i < size; i++) {
                String configCode = profileConfig.get(i).getConfigCode();
                if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(PROFILE_06) && profileConfig.get(i).getModule_Order() == 1) {

                    try {
                        if (channel.getSelectedItem().toString().toLowerCase().contains("select")) {
                            channel.requestFocus();
                            validate = false;
                            Toast.makeText(getActivity(), "Choose " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_07)
                        && profileConfig.get(i).getModule_Order() == 1) {
                    try {
                        if (subchannel.getSelectedItem().toString().toLowerCase()
                                .contains("select")) {
                            subchannel.requestFocus();
                            validate = false;
                            Toast.makeText(getActivity(),
                                    "Choose " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_30)
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

                    try {
                        if (editText[i].getText().toString().trim().length() == 0 ||
                                editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                                ) {

                            editText[i].requestFocus();
                            validate = false;

                            Toast.makeText(getActivity(),
                                    profileConfig.get(i).getMenuName() + " Length Must Be "
                                            + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_12)
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

                    try {
                        if (editText[i].getText().toString().trim().length() == 0 ||
                                editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                                ) {

                            editText[i].requestFocus();
                            validate = false;

                            Toast.makeText(getActivity(),
                                    profileConfig.get(i).getMenuName() + " Length Must Be "
                                            + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_10)
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

                    try {
                        if (editText[i].getText().toString().trim().length() == 0 ||
                                editText[i].getText().toString().length() < profileConfig.get(i).getMaxLengthNo()
                                ) {

                            editText[i].requestFocus();
                            validate = false;

                            Toast.makeText(getActivity(),
                                    profileConfig.get(i).getMenuName() + " Length Must Be "
                                            + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                } else if (configCode.equals(PROFILE_58) && profileConfig.get(i).getModule_Order() == 1) {
                    ArrayList<NewOutletAttributeBO> selectedAttributeLevel = new ArrayList<>();
                    boolean isAdded = true;

                    try {
                        // to check all common mandatory attributes selected
                        for (NewOutletAttributeBO attributeBO : bmodel.newOutletAttributeHelper.getAttributeParentList()) {

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

                        //to check all mandatory channel's attributes selected
                        if (isChannelAvailable() && isAdded) {
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
                        bmodel.setRetailerAttribute(selectedAttributeLevel);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode().equalsIgnoreCase(PROFILE_78)
                        && profileConfig.get(i).getModule_Order() == 1
                        && editText[i].getText().toString().trim().length() != 0) {
                    try {
                        if (!isValidEmail(editText[i].getText().toString())) {
                            editText[i].requestFocus();
                            validate = false;
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.enter_valid_email_id), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }

                        if (!isEmailVerfied) {
                            editText[i].requestFocus();
                            validate = false;
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.profile_edit_verify_email_id), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_79)
                        && profileConfig.get(i).getModule_Order() == 1
                        && editText[i].getText().toString().trim().length() != 0) {

                    try {
                        if (!isMobileNoVerfied) {
                            editText[i].requestFocus();
                            validate = false;
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.profile_edit_verify_mobile_no), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_81)
                        && profileConfig.get(i).getModule_Order() == 1) {

                    try {
                        if (editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo() ||
                                !isValidRegx(editText[i].getText().toString(), profileConfig.get(i).getRegex())) {

                            int length = editText[i].getText().toString().trim().length();

                            if (length > 0 && editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo()) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        profileConfig.get(i).getMenuName() + " Length Must Be " + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            } else if (length > 0 && !isValidRegx(editText[i].getText().toString(), profileConfig.get(i).getRegex())) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.enter_valid) + " " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase(PROFILE_61)
                        && profileConfig.get(i).getModule_Order() == 1) {

                    try {
                        if (editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo() ||
                                !isValidRegx(editText[i].getText().toString().trim(), profileConfig.get(i).getRegex()) ||
                                !isValidGSTINWithPAN(editText[i].getText().toString().trim())) {


                            int length = editText[i].getText().toString().trim().length();

                            if (length > 0 && editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo()) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        profileConfig.get(i).getMenuName() + " Length Must Be " + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            } else if (length > 0 && !isValidRegx(editText[i].getText().toString().trim(), profileConfig.get(i).getRegex())) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.enter_valid) + " " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            } else if (length > 0 && !isValidGSTINWithPAN(editText[i].getText().toString().trim())) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.enter_valid) + " " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            }

                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (profileConfig.get(i).getModule_Order() == 1) {

                    try {
                        if (editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo() ||
                                !isValidRegx(editText[i].getText().toString(), profileConfig.get(i).getRegex())) {

                            int length = editText[i].getText().toString().trim().length();

                            if (length > 0 && editText[i].getText().toString().trim().length() < profileConfig.get(i).getMaxLengthNo()) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        profileConfig.get(i).getMenuName() + " Length Must Be " + profileConfig.get(i).getMaxLengthNo(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            } else if (length > 0 && !isValidRegx(editText[i].getText().toString(), profileConfig.get(i).getRegex())) {
                                validate = false;
                                editText[i].requestFocus();
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.enter_valid) + " " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            }
                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }


            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return validate;
    }

    public boolean isValidRegx(CharSequence target, String regx) {

        if (regx.equals("")) {
            return true;
        }
        String value = regx.replaceAll("\\<.*?\\>", "");
        return !TextUtils.isEmpty(target) && Pattern.compile(value).matcher(target).matches();
    }

    public boolean isValidGSTINWithPAN(CharSequence target) {

        for (int index = 0; index < profileConfig.size(); index++) {
            if (profileConfig.get(index).getConfigCode()
                    .equalsIgnoreCase("PROFILE81")) {

                String panNumber = editText[index].getText().toString().trim();
                if (panNumber.length() > 0) {
                    if (target.subSequence(2, target.length() - 3).equals(panNumber))
                        return true;
                    else
                        return false;
                } else
                    return true;
            }
        }

        return true;
    }

    /*comparing two values with equalsIgnoreCase*/
    private boolean comparConfigerCode(String configCode, @NonNls String configCodeFromDB) {
        return configCode.equalsIgnoreCase(configCodeFromDB);
    }

}
