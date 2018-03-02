package com.ivy.sd.png.view.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import android.view.Display;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ivy.location.LocationUtil;
import com.ivy.maplib.BaiduMapDialogue;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.MapDialogue;
import com.ivy.sd.png.view.NearByRetailerDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hanifa.m on 3/28/2017.
 */

public class ProfileEditFragment extends IvyBaseFragment {

    private BusinessModel bmodel;

    private int REQUEST_CODE = 100;

    WindowManager wmanager;
    Display display;
    int width = 0;
    private Vector<ChannelBO> channelMaster;

    private ArrayList<LocationBO> mLocationMasterList1;
    private ArrayList<LocationBO> mLocationMasterList2;
    private ArrayList<LocationBO> mLocationMasterList3;

    private ScrollView profielEditScrollView;

    private TextView textview[] = null;
    private TextView textview2[] = null;

    private TextView latlongtextview, priorityproducttextview;

    LinearLayout.LayoutParams commonsparams, params, params3, params4, params5;
    LinearLayout.LayoutParams weight1, weight2, weight3, weight0;
    private Vector<ConfigureBO> profileConfig = new Vector<ConfigureBO>();
    private ArrayList<NewOutletBO> finalProfileList;
    private Vector<ConfigureBO> profileChannelConfig = new Vector<ConfigureBO>();
//    Button saveReason, startorder;
//    private double mnth_actual = 0.0, Balance = 0.0;
//    private double day_acheive = 0;

    RetailerMasterBO retailerObj;
    int locid = 0, loc2id = 0;
    private MaterialSpinner channel, subchannel, location1, location2, location3, saveSpinner,
            contactTitleSpinner1, contactTitleSpinner2, contractSpinner;


    private ArrayAdapter<LocationBO> locationAdapter1;
    private ArrayAdapter<LocationBO> locationAdapter2;
    private ArrayAdapter<LocationBO> locationAdapter3;

    private ArrayAdapter<ChannelBO> channelAdapter;
    private ArrayAdapter<SpinnerBO> subchannelAdapter;
    private ArrayAdapter<ReasonMaster> saveSpinnerAdapter;
    private ArrayAdapter<NewOutletBO> contactTitleAdapter;
    private ArrayAdapter<NewOutletBO> contractStatusAdapter;
    private ArrayList<NewOutletBO> mcontactTitleList;
    private ArrayList<NewOutletBO> mcontractStatusList;
    private ArrayList<StandardListBO> mPriorityProductList;
    private ArrayList<StandardListBO> selectedPriorityProductList;
    private String selectedProductID = "";
    boolean visit;
    int other1_editText_index, other2_editText_index, lName1_editText_index, lName2_editText_index;

    private AppCompatEditText editText[] = null;
    private TextInputLayout editTextInputLayout, editTextInputLayout1, editTextInputLayout2, editTextInputLayout3, editTextInputLayout4;
    static String lat = "";
    static String longitude = "";
    Vector<RetailerMasterBO> mNearbyRetIds;
    Vector<RetailerMasterBO> mSelectedIds = new Vector<>();
    private boolean is_contact_title1 = false, is_contact_title2 = false;
    private String mcontact_title1_lovId = "",
            mcontact_title2_lovId = "", mcontact_title1_text = "0", mcontact_title2_text = "0";
    ToggleButton btn_Deactivate;
    LinearLayout bottomLayout;
    RelativeLayout relativeLayout;
    double outStanding = 0.0, invoiceAmount = 0.0, mnth_acheive = 0;
    String retailerCreditLimit = "0.0";
    String physicalLocation = "", gstType = "", taxType = "";
    boolean isGstType;
    private View view;
    private Button saveTxtBtn;
    TextView nearbyTextView;
    private String sub_chanel_mname;

    private ArrayList<NewOutletAttributeBO> attributeList;
    private ArrayList<NewOutletAttributeBO> attributeHeaderList;
    private ArrayList<Integer> attributeIndexList;
    private HashMap<String, ArrayList<Integer>> attributeIndexMap;
    private HashMap<String, ArrayList<ArrayList<NewOutletAttributeBO>>> listHashMap;
    private HashMap<Integer, NewOutletAttributeBO> selectedAttribList; // Hashmap to retreive selected level of Attribute
    private HashMap<String, MaterialSpinner> spinnerHashMap = null;
    private HashMap<String, ArrayAdapter<NewOutletAttributeBO>> spinnerAdapterMap = null;
    HashMap<String, ArrayList<NewOutletAttributeBO>> attribMap;
    boolean isSubChannel = false;
    HashMap<Integer, ArrayList<Integer>> mAttributeListByChannelId;

    LinearLayout.LayoutParams paramsAttrib, paramsAttribSpinner;
    int check = 0;
    int spinnerCount = 0;
    private ImageView imageView;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int LATLONG_CAMERA_REQUEST_CODE = 2;
    private String imageFileName;
    private String cameraFilePath = "", latlongCameraFilePath = "";
    private AppCompatCheckBox inSEZcheckBox = null;

    private ImageView latlongCameraBtn;
    boolean isLatLongCameravailable = false;

    private ArrayList<InputFilter> inputFilters = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        profielEditScrollView = (ScrollView) view.findViewById(R.id.profile_edit_scrollview);
        saveTxtBtn = (Button) view.findViewById(R.id.profile_edit_save);
        saveTxtBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        setHasOptionsMenu(true);


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
        // }

        bmodel.newOutletHelper.loadContactTitle();
        bmodel.newOutletHelper.loadContactStatus();
        bmodel.newOutletHelper.downloadLinkRetailer();
        new DownloadAsync().execute();
      //  bmodel.configurationMasterHelper.downloadProfileModuleConfig();
        // get previous changes from retailerEdit  Header and Detail table
        bmodel.newOutletHelper.getPreviousProfileChanges(bmodel.getRetailerMasterBO().getRetailerID());
        createTabViewForProfileForEdit();
        saveTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (validateEditProfile()) {

                        if (bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                            if ((lat.equals("") || Double.parseDouble(lat) == 0 || longitude.equals("")
                                    || Double.parseDouble(longitude) == 0)
                                    || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl !=0
                                    && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)) {
                                Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
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
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
      /*  bmodel.newOutletHelper.downloadLinkRetailer();
        new DownloadAsync().execute();
        bmodel.configurationMasterHelper.downloadProfileModuleConfig();
        // get previous changes from retailerEdit  Header and Detail table
        bmodel.newOutletHelper.getPreviousProfileChanges(bmodel.getRetailerMasterBO().getRetailerID());
        createTabViewForProfileForEdit();
           saveTxtBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       if (validateEditProfile())
                           new SaveEditAsyncTask().execute();

                   } catch (Exception e) {
                       Commons.printException(e);
                   }
               }
           });*/
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

    private View createTabViewForProfileForEdit() {
        profileConfig = new Vector<>();
        profileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();

        retailerObj = bmodel.getRetailerMasterBO();

        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE41") && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title1 = true;

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE42") && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title2 = true;

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE47") && configureBO.isFlag() == 1)
                outStanding = bmodel.getInvoiceAmount() - bmodel.getOutStandingInvoiceAmount();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE48") && configureBO.isFlag() == 1)
                retailerCreditLimit = bmodel.getRetailerMasterBO().getProfile_creditLimit();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE49") && configureBO.isFlag() == 1)
                invoiceAmount = bmodel.getInvoiceAmount();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE50") && configureBO.isFlag() == 1)
                physicalLocation = bmodel.mRetailerHelper.getPhysicalLcoation(retailerObj.getLocationId());
            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE51") && configureBO.isFlag() == 1) {
                gstType = bmodel.mRetailerHelper.getGSTType(retailerObj.getTaxTypeId());
                if (gstType.length() > 0) {
                    isGstType = true;
                    taxType = "CST";
                } else {
                    isGstType = false;
                    taxType = "VAT";
                }
            }

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE58") && configureBO.isFlag() == 1) {
                ArrayList<NewOutletAttributeBO> tempList = bmodel.newOutletHelper.updateRetailerMasterAttribute(
                        bmodel.newOutletAttributeHelper.getEditAttributeList(retailerObj.getRetailerID()));

                bmodel.newOutletAttributeHelper.downloadCommonAttributeList();
                mAttributeListByChannelId = bmodel.newOutletAttributeHelper.downloadChannelWiseAttributeList();

                bmodel.getAttributeListForRetailer();
                attributeList = bmodel.newOutletHelper.updateRetailerMasterAttribute(retailerObj.getAttributeBOArrayList());

                attribMap = bmodel.newOutletAttributeHelper.getAttribMap();

                if (!tempList.isEmpty()) {
                    NewOutletAttributeBO tempBO1;
                    NewOutletAttributeBO tempBO2;
                    for (int i = 0; i < attributeList.size(); i++) {
                        tempBO1 = attributeList.get(i);
                        for (int j = 0; j < tempList.size(); j++) {
                            tempBO2 = tempList.get(j);
                            if (tempBO1.getParentId() == tempBO2.getParentId()) {
                                if (tempBO1.getAttrId() != tempBO2.getAttrId() && "N".equals(tempBO2.getStatus()))
                                    attributeList.set(i, tempBO2);
                            }
                        }
                    }
                }

            }


        }

        if (is_contact_title2 || is_contact_title1) {

            mcontactTitleList = new ArrayList<>();
            mcontactTitleList.add(0, new NewOutletBO(-1, getResources().getString(R.string.select_str) + " Title Name"));
            mcontactTitleList.addAll(bmodel.newOutletHelper.getContactTitleList());
            mcontactTitleList.add(bmodel.newOutletHelper.getContactTitleList().size() + 1, new NewOutletBO(0, "Others"));
            contactTitleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontactTitleList);
            contactTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        }

        if (profielEditScrollView != null)
            profielEditScrollView.removeAllViews();

        profielEditScrollView = (ScrollView) view.findViewById(R.id.profile_edit_scrollview);

        editText = new AppCompatEditText[100];

        textview = new TextView[100];
        textview2 = new TextView[100];

        commonsparams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commonsparams.setMargins(10, 15, 10, 0);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        params3 = new LinearLayout.LayoutParams(width / 6, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.weight = 1;
        params3.setMargins(0, (int) getResources().getDimension(R.dimen.profile_spinner_top_margin), 0, 0);

        params4 = new LinearLayout.LayoutParams(width / 6, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params4.weight = 1;

        params5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params5.setMargins(0, 0, 20, 0);
        params5.gravity = Gravity.CENTER;

        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;

        weight3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight3.weight = 3;
        weight3.gravity = Gravity.CENTER;

        paramsAttrib = new LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAttrib.weight = .7f;
        paramsAttrib.setMargins(0, 0, 10, 0);

        paramsAttribSpinner = new LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsAttribSpinner.weight = 2.3f;

        LinearLayout totalView = new LinearLayout(getActivity());
        totalView.setOrientation(LinearLayout.VERTICAL);
        totalView.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.background_noise));


        double mnth_acheive = (retailerObj
                .getMonthly_acheived());
        double day_acheive = 0;

        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            day_acheive = bmodel.getInvoiceAmount();
        } else {
            day_acheive = bmodel.getOrderValue();
        }

        double mnth_actual = day_acheive + mnth_acheive;


        double Balance = (retailerObj.getMonthly_target())
                - (mnth_actual);


        int size = profileConfig.size();

        try {
            //for profile image
            for (int i = 0; i < size; i++) {
                String configCode = profileConfig.get(i).getConfigCode();
                int flag = profileConfig.get(i).isFlag();
                if (configCode.equals("PROFILE60") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    LLParams.setMargins(10, 5, 10, 5);
                    totalView.addView(
                            getImageView(), LLParams);
                    break;
                }
            }
            for (int i = 0; i < size; i++) {

                int mNumber = i;
                int flag = profileConfig.get(i).isFlag();
                String mName = profileConfig.get(i).getMenuName();
                String configCode = profileConfig.get(i).getConfigCode();

                if (configCode.equals("PROFILE02") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    if (retailerObj.getRetailerName() == null
                            || retailerObj.getRetailerName().equals(
                            "null")) {
                        retailerObj.setRetailerName("");
                    }

                    String text = retailerObj.getRetailerName() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE03") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    if (retailerObj.getAddress1() == null
                            || retailerObj.getAddress1().equals(
                            "null")) {
                        retailerObj.setAddress1("");
                    }

                    String text = retailerObj.getAddress1() + "";

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE04") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getAddress2() == null
                            || retailerObj.getAddress2().equals(
                            "null")) {
                        retailerObj.setAddress2("");
                    }

                    String text = retailerObj.getAddress2() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE05") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getAddress3() == null
                            || retailerObj.getAddress3().equals(
                            "null")) {
                        retailerObj.setAddress3("");
                    }

                    String text = retailerObj.getAddress3() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE39") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getCity() == null
                            || retailerObj.getCity().equals(
                            "null")) {
                        retailerObj.setCity("");
                    }

                    String text = retailerObj.getCity() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
                    Commons.print("PROFILE39," + "" + profileConfig.get(i).getModule_Order());

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE40") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getState() == null
                            || retailerObj.getState().equals(
                            "null")) {
                        retailerObj.setState("");
                    }

                    String text = retailerObj.getState() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    Commons.print("PROFILE40," + "" + profileConfig.get(i).getModule_Order());


                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE38") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getPincode() == null
                            || retailerObj.getPincode().equals(
                            "null")) {
                        retailerObj.setPincode("");
                    }

                    String text = retailerObj.getPincode() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    Commons.print("PROFILE38," + "" + profileConfig.get(i).getModule_Order());
                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                            commonsparams);
                } else if (configCode.equals("PROFILE30") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getContactnumber1() == null
                            || retailerObj.getContactnumber1().equals(
                            "null")) {
                        retailerObj.setContactnumber1("");
                    }

                    String text = retailerObj.getContactnumber1() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals("PROFILE06") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    int id = retailerObj.getChannelID();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                    totalView.addView(
                            getSpinnerView(mNumber, mName, configCode, id
                            ), commonsparams);
                } else if (configCode.equals("PROFILE07") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    int id = retailerObj.getSubchannelid();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                    totalView.addView(
                            getSpinnerView(mNumber, mName, configCode, id
                            ), commonsparams);
                } else if (configCode.equals("PROFILE43") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    int id = retailerObj.getContractLovid();
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                            id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                    totalView.addView(
                            getSpinnerView(mNumber, mName, configCode, id
                            ), commonsparams);
                } else if (configCode.equals("PROFILE08") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    String textLat = retailerObj.getLatitude() + "";
                    String MenuName = "LatLong";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(textLat))
                            textLat = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    for (int j = 0; j < size; j++) {
                        if (profileConfig.get(j).getConfigCode().equals("PROFILE31")
                                && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                            String textLong = retailerObj.getLongitude() + "";
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode()) != null)
                                if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode()).equals(textLong))
                                    textLong = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(profileConfig.get(j).getConfigCode());

                            String text = textLat + ", " + textLong;

                            totalView.addView(
                                    getLatlongTextView(mNumber, MenuName, text),
                                    commonsparams);

                        }

                    }
                } else if (configCode.equals("PROFILE63") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    isLatLongCameravailable = true;
                }
////                else if (configCode.equals("PROFILE31") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
////
////                    String text = retailerObj.getLongitude() + "";
////                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
////                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(retailerObj.getLongitude() + ""))
////                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);
////
////                    totalView.addView(
////                            getLatlongTextView(mNumber, mName, text, configCode),
////                            commonsparams);
////                }
                else if (configCode.equals("PROFILE09") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    if (retailerObj.getContactname() == null
                            || retailerObj.getContactname().equals(
                            "null")) {
                        retailerObj.setContactname("");
                    }

                    String text = "";
                    text = retailerObj
                            .getContactname();

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);


                } else if (configCode.equals("PROFILE10") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getContactnumber() == null
                            || retailerObj.getContactnumber().equals(
                            "null")) {
                        retailerObj.setContactnumber("");
                    }

                    String text = "";
                    text = retailerObj.getContactnumber();

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_PHONE),
                            commonsparams);
                } else if (configCode.equals("PROFILE11") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    if (retailerObj.getContactname2() == null
                            || retailerObj.getContactname2().equals(
                            "null")) {
                        retailerObj.setContactname2("");
                    }

                    String text = "";
                    text = retailerObj.getContactname2() + "";

                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);


                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);

                } else if (configCode.equals("PROFILE12") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    if (retailerObj.getContactnumber2() == null
                            || retailerObj.getContactnumber2().equals(
                            "null")) {
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
                } else if (configCode.equals("PROFILE13") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
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
                                id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals("PROFILE14") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    try {
                        String title = "";

                        String[] loc2 = bmodel.mRetailerHelper.getParentLevelName(
                                locid, true);
                        if (loc2 != null) {

                            loc2id = Integer.parseInt(loc2[0]);
                            title = loc2[2];

                        }
                        int id = retailerObj.getLocationId();
                        if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                            if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(id + ""))
                                id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals("PROFILE15") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
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
                                id = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode));

                        totalView.addView(
                                getSpinnerView(mNumber, title, configCode, id
                                ), commonsparams);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else if (configCode.equals("PROFILE36")) {
                    if (!retailerObj.getIsNew().equals("Y"))
                        totalView.addView(
                                getNearByRetailerView(mNumber, mName, true
                                ), commonsparams);


                } else if (configCode.equals("PROFILE25") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getCreditDays() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                            commonsparams);
                } else if (configCode.equals("PROFILE20") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRField1() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE26") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRfield2() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE27") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getCredit_invoice_count() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE28") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRField4() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE53") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRField5() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE54") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRField6() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_TEXT_VARIATION_PERSON_NAME),
                            commonsparams);
                } else if (configCode.equals("PROFILE55") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getRField7() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_NUMBER),
                            commonsparams);
                }
                /* else if (configCode.equals("PROFILE37")) {
//                    final String taxName = bmodel.newOutletHelper.getListName(retailerObj.getApplyLevelId(), "TAX_TYPE");
//
//                    totalView.addView(
//                            getTextView(mNumber, mName, taxName),
//                            commonsparams);
//                }*/
                else if (configCode.equals("PROFILE57") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
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
                } else if (configCode.equals("PROFILE58") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    LLParams.setMargins(10, 5, 10, 5);
                    totalView.addView(
                            addAttributeView(0), LLParams);
                } else if (configCode.equals("PROFILE61") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

                    String text = retailerObj.getGSTNumber() + "";
                    if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode) != null)
                        if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode).equals(text))
                            text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get(configCode);

                    totalView.addView(
                            getEditTextView(mNumber, mName, text, InputType.TYPE_CLASS_TEXT),
                            commonsparams);
                } else if (configCode.equals("PROFILE62") && flag == 1 && profileConfig.get(i).getModule_Order() == 1) {

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
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText("*");
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.RED);
                        textLayout.addView(mn_textview, weight0);
                    }

                    LinearLayout.LayoutParams params8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params8.gravity = Gravity.CENTER;
                    params8.setMargins(7, 0, 0, 0);

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

            profielEditScrollView.addView(totalView);

        } catch (Exception e) {
            Commons.printException(e);
        }


        // return view;
        return null;
    }


    private LinearLayout getEditTextView(final int mNumber, String MName, String textValue,
                                         int editStyle) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        linearlayout.setBackgroundColor(getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout secondlayout = new LinearLayout(getActivity());
        //secondlayout.addView(editText[mNumber], params);
        //regex
        addLengthFilter(profileConfig.get(mNumber).getRegex());
        checkRegex(profileConfig.get(mNumber).getRegex());
        if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE02") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE03") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE04") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE05") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE39") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE20") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE26") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE27") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE28") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE53") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE54") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE55") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE40") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE38") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE61")

                ) {

            LinearLayout firstlayout = new LinearLayout(getActivity());
            firstlayout.setOrientation(LinearLayout.HORIZONTAL);
            editTextInputLayout = new TextInputLayout(getActivity());


            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            editText[mNumber].setText(textValue);
            editText[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
            editText[mNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            editText[mNumber].setHint(MName);
            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                if (inputFilters.size() == 2)
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            editTextInputLayout.addView(editText[mNumber]);

//            secondlayout.addView(editTextInputLayout, commonsparams);
//            linearlayout.addView(firstlayout, weight2);
            linearlayout.addView(editTextInputLayout, weight1);

        }
        if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE30") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE10") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE12")
                ) {

            LinearLayout firstlayout = new LinearLayout(getActivity());
            firstlayout.setOrientation(LinearLayout.HORIZONTAL);
            //    firstlayout.setPadding(0, 0, 0, 0);
            editTextInputLayout = new TextInputLayout(getActivity());


            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            editText[mNumber].setText(textValue);
            editText[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
            editText[mNumber].setInputType(InputType.TYPE_CLASS_PHONE);
            editText[mNumber].setHint(MName);
            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                if (inputFilters.size() == 2)
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            editTextInputLayout.addView(editText[mNumber]);


//            if (profileConfig.get(mNumber).getMaxLengthNo() > 0) {
//                int maxNo = profileConfig.get(mNumber).getMaxLengthNo();
//                editText[mNumber].setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxNo)});
//            } else {
//                editText[mNumber].setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
//            }
//            secondlayout.addView(editText[mNumber], commonsparams);
//            linearlayout.addView(firstlayout, weight2);
            linearlayout.addView(editTextInputLayout, weight1);

        }
        if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE25")) {

            LinearLayout firstlayout = new LinearLayout(getActivity());
            firstlayout.setOrientation(LinearLayout.HORIZONTAL);
            editTextInputLayout = new TextInputLayout(getActivity());

            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            if (textValue.equals("0"))
                editText[mNumber].setText("");
            else
                editText[mNumber].setText(textValue);

            editText[mNumber].setInputType(InputType.TYPE_CLASS_NUMBER);
            editText[mNumber].setHint(MName);
            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                if (inputFilters.size() == 2)
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            editTextInputLayout.addView(editText[mNumber]);

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

//            secondlayout.addView(editText[mNumber], commonsparams);
//            linearlayout.addView(firstlayout, weight2);
            linearlayout.addView(editTextInputLayout, weight1);

        }

        if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE09") ||
                profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE11")) {
            LinearLayout firstlayout = new LinearLayout(getActivity());
            firstlayout.setOrientation(LinearLayout.HORIZONTAL);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            editTextInputLayout = new TextInputLayout(getActivity());
            //    firstlayout.setPadding(0, 0, 0, 0);
            textview[mNumber] = new TextView(getActivity());
            textview[mNumber].setText(MName);
            textview[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
            textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));
            firstlayout.addView(textview[mNumber], weight1);


            editText[mNumber] = new AppCompatEditText(getActivity());
            editText[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
            editText[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
            editText[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            editText[mNumber].setHint("First Name");
            editText[mNumber].setText(textValue);
            editText[mNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            if (inputFilters != null && inputFilters.size() > 0) {
                InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                stockArr = inputFilters.toArray(stockArr);
                editText[mNumber].setFilters(stockArr);
                if (inputFilters.size() == 2)
                    editText[mNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            editTextInputLayout.addView(editText[mNumber]);


            //Contact 1 Last Name
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE09")) {
                editTextInputLayout1 = new TextInputLayout(getActivity());
                editText[lName1_editText_index] = new AppCompatEditText(getActivity());
                editText[lName1_editText_index].setHint("Last Name");
                editText[lName1_editText_index].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
                editText[lName1_editText_index].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                editText[lName1_editText_index].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                editText[lName1_editText_index].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[lName1_editText_index].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[lName1_editText_index].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                editTextInputLayout1.addView(editText[lName1_editText_index]);
                if (retailerObj.getContactLname() == null
                        || retailerObj.getContactLname().equals(
                        "null")) {
                    retailerObj.setContactLname("");
                }

                String text = "";
                text = retailerObj
                        .getContactLname();

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME").equals(text))
                        text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME");

                editText[lName1_editText_index].setText(text);

            }

            //Contact2 Last Name
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE11")) {
                editTextInputLayout2 = new TextInputLayout(getActivity());
                editText[lName2_editText_index] = new AppCompatEditText(getActivity());
                editText[lName2_editText_index].setHint("Last Name");
                editText[lName2_editText_index].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
                editText[lName2_editText_index].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                editText[lName2_editText_index].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                editText[lName2_editText_index].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[lName2_editText_index].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[lName2_editText_index].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                editTextInputLayout2.addView(editText[lName2_editText_index]);
                if (retailerObj.getContactLname2() == null
                        || retailerObj.getContactLname2().equals(
                        "null")) {
                    retailerObj.setContactLname2("");
                }

                String text = "";
                text = retailerObj
                        .getContactLname2();

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME2") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME2").equals(text))
                        text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("LNAME2");

                editText[lName2_editText_index].setText(text);

            }
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE09") &&
                    is_contact_title1) {
                contactTitleSpinner1 = new MaterialSpinner(getActivity());
                contactTitleSpinner1.setId(mNumber);
                contactTitleSpinner1.setAdapter(contactTitleAdapter);

                //contact title other edit text
                editTextInputLayout3 = new TextInputLayout(getActivity());
                editText[other1_editText_index] = new AppCompatEditText(getActivity());
                editText[other1_editText_index].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                editText[other1_editText_index].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                editText[other1_editText_index].setHint("Title");
                editText[other1_editText_index].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
                editText[other1_editText_index].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[other1_editText_index].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[other1_editText_index].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                editTextInputLayout3.addView(editText[other1_editText_index]);
                if (retailerObj.getContact1_titlelovid() == null
                        || retailerObj.getContact1_titlelovid().equals(
                        "null")) {
                    retailerObj.setContact1_titlelovid("");
                }

                mcontact_title1_lovId = retailerObj.getContact1_titlelovid();

                if (retailerObj.getContact1_title() == null
                        || retailerObj.getContact1_title().equals(
                        "null")) {
                    retailerObj.setContact1_title("");
                }
                mcontact_title1_text = retailerObj.getContact1_title();

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE41") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE41").equals(mcontact_title1_lovId))
                        mcontact_title1_lovId = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE41");

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT1TITLE") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT1TITLE").equals(mcontact_title1_text))
                        mcontact_title1_text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT1TITLE");

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
                            //editTextInputLayout.setVisibility(View.VISIBLE);
                            editTextInputLayout3.setVisibility(View.VISIBLE);
                            editText[other1_editText_index].requestFocus();
                            mcontact_title1_lovId = "0";
                        } else if (tempBo.getListId() == -1) {
                            // editTextInputLayout.setVisibility(View.GONE);
                            editTextInputLayout3.setVisibility(View.GONE);
                            mcontact_title1_lovId = "0";
                            mcontact_title1_text = "0";
                        } else {
                            // editTextInputLayout.setVisibility(View.GONE);
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


            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE11") &&
                    is_contact_title2) {
                contactTitleSpinner2 = new MaterialSpinner(getActivity());
                contactTitleSpinner2.setId(mNumber);
                contactTitleSpinner2.setAdapter(contactTitleAdapter);

                //contact title other edit text
                editTextInputLayout4 = new TextInputLayout(getActivity());
                editText[other2_editText_index] = new AppCompatEditText(getActivity());
                editText[other2_editText_index].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                editText[other2_editText_index].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                editText[other2_editText_index].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
                editText[other2_editText_index].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                editText[other2_editText_index].setHint("Title");
                if (inputFilters != null && inputFilters.size() > 0) {
                    InputFilter[] stockArr = new InputFilter[inputFilters.size()];
                    stockArr = inputFilters.toArray(stockArr);
                    editText[other2_editText_index].setFilters(stockArr);
                    if (inputFilters.size() == 2)
                        editText[other2_editText_index].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                editTextInputLayout4.addView(editText[other2_editText_index]);

                if (retailerObj.getContact2_titlelovid() == null
                        || retailerObj.getContact2_titlelovid().equals(
                        "null")) {
                    retailerObj.setContact2_titlelovid("");
                }

                mcontact_title2_lovId = retailerObj.getContact2_titlelovid();

                if (retailerObj.getContact2_title() == null
                        || retailerObj.getContact2_title().equals(
                        "null")) {
                    retailerObj.setContact2_title("");
                }
                mcontact_title2_text = retailerObj.getContact2_title();

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE42") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE42").equals(mcontact_title2_lovId))
                        mcontact_title2_lovId = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE42");

                if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT2TITLE") != null)
                    if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT2TITLE").equals(mcontact_title2_text))
                        mcontact_title2_text = bmodel.newOutletHelper.getmPreviousProfileChangesList().get("CT2TITLE");

                if (mcontact_title2_lovId != null)
                    if (mcontact_title2_lovId.equals("0") && !mcontact_title2_text.equals("0") && mcontact_title2_text.length() > 0) {
                        contactTitleSpinner2.setSelection(mcontactTitleList.size() - 1);
                        //editTextInputLayout.setVisibility(View.VISIBLE);
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
                            //editTextInputLayout.setVisibility(View.VISIBLE);
                            editTextInputLayout4.setVisibility(View.VISIBLE);
                            editText[other2_editText_index].requestFocus();
                            mcontact_title2_lovId = "0";
                        } else if (tempBo.getListId() == -1) {
                            //editTextInputLayout.setVisibility(View.GONE);
                            editTextInputLayout4.setVisibility(View.GONE);
                            mcontact_title2_lovId = "0";
                            mcontact_title2_text = "0";
                        } else {
                            //editTextInputLayout.setVisibility(View.GONE);
                            editTextInputLayout4.setVisibility(View.GONE);
                            mcontact_title2_lovId = "" + tempBo.getListId();
                            mcontact_title2_text = "0";
                        }

                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                secondlayout.addView(contactTitleSpinner2, params3);
                //editTextInputLayout2.addView(editText[other2_editText_index]);
                secondlayout.addView(editTextInputLayout4, params4);
            }


            secondlayout.addView(editTextInputLayout, params4);
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE09"))
                secondlayout.addView(editTextInputLayout1, params4);
            if (profileConfig.get(mNumber).getConfigCode().equalsIgnoreCase("PROFILE11"))
                secondlayout.addView(editTextInputLayout2, params4);
            linearlayout.addView(firstlayout, weight2);
            linearlayout.addView(secondlayout, weight2);

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
                if (len != null && !len.isEmpty()) {
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

    private LinearLayout getSpinnerView(int mNumber, String MName,
                                        String menuCode, int id) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundColor(getActivity().getResources().getColor(R.color.white_box_start));

//        LinearLayout firstlayout = new LinearLayout(getActivity());
//        firstlayout.setPadding(0, 0, 0, 12);
//        textview[mNumber] = new TextView(getActivity());
//        textview[mNumber].setText(MName);
//        textview[mNumber].setTextColor(Color.BLACK);
//        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));
//        firstlayout.addView(textview[mNumber], weight1);
//
//
//        layout.addView(firstlayout, weight2);

        LinearLayout.LayoutParams spinweight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        spinweight.weight = 1;
        spinweight.gravity = Gravity.CENTER;

        if (menuCode.equals("PROFILE06")) {
            channel = new MaterialSpinner(getActivity());
            channel.setId(mNumber);
            channel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            channel.setFloatingLabelText(MName);
            //channel.setGravity(Gravity.CENTER);

            layout.addView(channel, spinweight);

            channelAdapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_spinner_item);
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
            channel.setSelection(setPos);
            channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    ChannelBO tempBo = (ChannelBO) parent.getSelectedItem();
                    loadsubchannel(tempBo.getChannelId());
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

        }


//        if (menuCode.equals("PROFILE21")) {
//            // Saved position variable
//            int position = 0;
//            int reasonId = bmodel.reasonHelper
//                    .getSavedNonVisitReason(retailerObj);
//
//            saveSpinner = new MaterialSpinner(getActivity());
//            saveSpinner.setId(mNumber);
//            saveSpinner.setFloatingLabelText(MName);
//            // layout.addView(saveSpinner, weight1);
//            //saveSpinner.setGravity(Gravity.CENTER);
//
//            saveSpinnerAdapter = new ArrayAdapter<ReasonMaster>(
//                    getActivity(), android.R.layout.simple_spinner_item);
//            saveSpinnerAdapter.add(new ReasonMaster(0 + "", "Select " + MName));
//            for (int i = 0; i < bmodel.reasonHelper.getNonVisitReasonMaster()
//                    .size(); i++) {
//                ReasonMaster temp = bmodel.reasonHelper.getNonVisitReasonMaster()
//                        .get(i);
//                if (temp.getReasonID().equals(reasonId + ""))
//                    position = i + 1;
//                saveSpinnerAdapter.add(temp);
//            }
//            saveSpinnerAdapter
//                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            saveSpinner
//                    .setAdapter(saveSpinnerAdapter);
//            saveSpinner
//                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                        public void onItemSelected(AdapterView<?> parent,
//                                                   View view, int position, long id) {
//
////                            TextView item = (TextView) view;
////                            String ti = (String) item.getText();
//                            // Hide/Enable Save reason button
////                            if (!ti.equals("Select")) {
////                                saveReason
////                                        .setVisibility(View.VISIBLE);
////
////
////                            } else {
////                                saveReason.setVisibility(View.GONE);
////
////                            /*    if (variablehistory.saveReason != null)
////                                    variablehistory.saveReason
////                                            .setVisibility(View.GONE);
////                            }*/
////                            }
//                        }
//
//                        public void onNothingSelected(AdapterView<?> parent) {
//                        }
//                    });
//
//            // Set position of spinner if already reason saved
//            saveSpinner.setSelection(position);
//            saveSpinner.setPadding(0, 0, 0, 12);
//            layout.addView(saveSpinner, spinweight);
//        }
        if (menuCode.equals("PROFILE43")) {
            int selected_pos = 0;
            try {
                contractSpinner = new MaterialSpinner(getActivity());
                contractSpinner.setId(mNumber);
                contractSpinner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                contractSpinner.setFloatingLabelText(MName);
                mcontractStatusList = new ArrayList<>();
                mcontractStatusList.add(0, new NewOutletBO(0, getResources().getString(R.string.select_str) + " " + MName));
                mcontractStatusList.addAll(bmodel.newOutletHelper.getContractStatusList());
                contractStatusAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontractStatusList);
                contractStatusAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        if (menuCode.equals("PROFILE07")) {
            subchannel = new MaterialSpinner(getActivity());
            subchannel.setId(mNumber);
            sub_chanel_mname = MName;
            subchannel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            subchannel.setFloatingLabelText(MName);
            //subchannel.setGravity(Gravity.CENTER);

            layout.addView(subchannel, spinweight);
        } else if (menuCode.equals("PROFILE13")) {
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

//			if(locationMaster!=null)
//				for (LocationBO temp : bmodel.getLocationMaster()) {
//					mLocationMasterList1.add(temp);
//				}

                locationAdapter1 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList1);

                String loc1id = "";
                int pos = 0, setPos = 0;
                String[] loc1 = bmodel.mRetailerHelper
                        .getParentLevelName(locid, false);

                if (loc1 != null) {
                    loc1id = loc1[0];
                }
                for (LocationBO loc : mLocationMasterList1) {
                    if (loc.getLocId() == Integer.parseInt(loc1id)) {
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
        } else if (menuCode.equals("PROFILE14")) {
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
                    if (loc.getLocId() == Integer.parseInt(loc2id)) {
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
        } else if (menuCode.equals("PROFILE15")) {
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

                locationAdapter3 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList3);


                String locid = "";
                int pos = 0, setPos = 0;
                String[] loc3 = bmodel.mRetailerHelper
                        .getParentLevelName(loc2id, true);

                if (loc3 != null) {
                    locid = loc3[0];
                }
                for (LocationBO loc : mLocationMasterList3) {
                    if (loc.getLocId() == Integer.parseInt(locid)) {
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
        }


        return layout;

    }

    private void loadsubchannel(int channelid) {

        Vector items = bmodel.subChannelMasterHelper.getSubChannelMaster();

        int siz = items.size();
        if (siz == 0) {
            return;
        }
        subchannelAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        subchannelAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subchannelAdapter.add(new SpinnerBO(0, getActivity().getResources()
                .getString(R.string.select_str)));

        int position = 0, setPos = 0;
        int subChannelID = bmodel.getRetailerMasterBO().getSubchannelid();
        if (bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE07") != null)
            if (!bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE07").equals(subChannelID + ""))
                subChannelID = Integer.parseInt(bmodel.newOutletHelper.getmPreviousProfileChangesList().get("PROFILE07"));
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
        subchannel.setSelection(setPos);
        subchannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //SpinnerBO tempBo = (SpinnerBO) parent.getSelectedItem();
                addAttributeView(1);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

    }


    private void updateLocationAdapter1(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();
//        if(mLocationMasterList1==null || mLocationMasterList1.size()==0)
//            locationList.add(new LocationBO(0, NewOutlet.this
//                .getResources().getString(R.string.select)));
//        else {
//            if(!((LocationBO)mLocationMasterList1.get(0)).getLocName().toLowerCase()
//                    .contains("select")) {
//                locationList.add(new LocationBO(0, NewOutlet.this
//                        .getResources().getString(R.string.select)));
//            }
//        }
        for (LocationBO locationBO : mLocationMasterList1) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }

        locationAdapter1 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, locationList);
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
//        if(mLocationMasterList2==null || mLocationMasterList2.size()==0)
//            locationList.add(new LocationBO(0, NewOutlet.this
//                    .getResources().getString(R.string.select)));
//        else {
//            if(!((LocationBO)mLocationMasterList2.get(0)).getLocName().toLowerCase()
//                    .contains("select")) {
//                locationList.add(new LocationBO(0, NewOutlet.this
//                        .getResources().getString(R.string.select)));
//            }
//        }
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


    private LinearLayout getLatlongTextView(final int mNumber, String MName,
                                            final String textvalue) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        linearlayout.setBackgroundColor(getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = new LinearLayout(getActivity());
        firstlayout.setPadding(0, 0, 0, 0);
        textview[mNumber] = new TextView(getActivity());
        textview[mNumber].setText(MName);
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        textview[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        textview[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        firstlayout.addView(textview[mNumber]);


        LinearLayout secondlayout = new LinearLayout(getActivity());
        secondlayout.setOrientation(LinearLayout.HORIZONTAL);
        secondlayout.setPadding(0, 0, 0, 0);
        secondlayout.setWeightSum(2f);
        secondlayout.setGravity(Gravity.CENTER_VERTICAL);
//        Button latlngButton = new Button(getActivity());
//        latlngButton.setBackgroundResource(R.drawable.redmarker);


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
        // if (Code.equals("PROFILE08")) {
//        secondlayout.addView(latlngButton, weight0);
        // }
        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight2);
        return linearlayout;

    }

    private LinearLayout getPriorityProductView(final int mNumber, final String MName,
                                                final String textvalue, final String productID) {

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        linearlayout.setBackgroundColor(getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = new LinearLayout(getActivity());
        firstlayout.setPadding(0, 0, 0, 12);
        textview[mNumber] = new TextView(getActivity());
        textview[mNumber].setText(MName);
        textview[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));
        textview[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        firstlayout.addView(textview[mNumber]);


        LinearLayout secondlayout = new LinearLayout(getActivity());
        secondlayout.setOrientation(LinearLayout.HORIZONTAL);
        secondlayout.setPadding(0, 0, 0, 12);
//        Button latlngButton = new Button(getActivity());
//        latlngButton.setBackgroundResource(R.drawable.redmarker);


        priorityproducttextview = new TextView(getActivity());
        priorityproducttextview.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        priorityproducttextview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        priorityproducttextview.setText(textvalue);


        secondlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mPriorityProductList != null) {

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    CustomFragment dialogFragment = new CustomFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title", MName);
                    bundle.putString("screentitle", MName);
                    bundle.putInt("hasLink", 0);
                    bundle.putString("productID", productID);
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(fm, "Sample Fragment");

                    dialogFragment.setCancelable(false);


                } else {
                    Toast.makeText(getActivity(), "Priority Products Not Available", Toast.LENGTH_SHORT).show();
                }

            }
        });


        priorityproducttextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));
        secondlayout.addView(priorityproducttextview);
        // if (Code.equals("PROFILE08")) {
//        secondlayout.addView(latlngButton, weight0);
        // }

        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight1);


        return linearlayout;

    }


    private LinearLayout getNearByRetailerView(int mNumber, String MName, boolean isEditMode) {
        if (bmodel.getNearByRetailers() != null)
            bmodel.getNearByRetailers().clear();
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundColor(getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = new LinearLayout(getActivity());

        textview[mNumber] = new TextView(getActivity());
        textview[mNumber].setText(MName);
        textview[mNumber].setTextColor(Color.BLACK);
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        textview[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        textview[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        firstlayout.addView(textview[mNumber], weight1);

        LinearLayout secondlayout = new LinearLayout(getActivity());
        secondlayout.setOrientation(LinearLayout.HORIZONTAL);

        Button retailerButton = new Button(getActivity());
        retailerButton.setText(R.string.edit);
        retailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vector<RetailerMasterBO> retailersList = bmodel.newOutletHelper.getLinkRetailerListByDistributorId().get(retailerObj.getDistributorId());


                if (retailersList.size() > 0) {
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
        LinearLayout lyt = new LinearLayout(getActivity());
        lyt.addView(nearbyTextView, weight1);
        scrl.addView(lyt, weight1);

        secondlayout.addView(scrl, weight1);
        secondlayout.addView(retailerButton, weight3);
        layout.addView(firstlayout, weight2);
        layout.addView(secondlayout, weight1);


        if (!isEditMode) {
            retailerButton.setVisibility(View.GONE);
            mSelectedIds = mNearbyRetIds;
        } else {
            bmodel.newOutletHelper.getNearbyRetailersEditRequest(Integer.parseInt(bmodel.getRetailerMasterBO().getRetailerID()));
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
                mSelectedIds = mNearbyRetIds;
        }
        // showing nearby retailers
        for (RetailerMasterBO bo : mSelectedIds) {
            nearbyTextView.setText(nearbyTextView.getText() + DataMembers.CR1 + bo.getRetailerName());
        }


        return layout;
    }

    //To create layout for Retailer Attribute
    private LinearLayout addAttributeView(int flag) {
        LinearLayout parentLayout = null;
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
                    mChannelAttributeList.addAll(mAttributeListByChannelId.get(bmodel.getRetailerMasterBO().getSubchannelid()));
                }

            } else if (isFromChannel) {
                if (((SpinnerBO) subchannel.getSelectedItem()).getId() != bmodel.getRetailerMasterBO().getSubchannelid()) {

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

                }

            }

            spinnerHashMap = new HashMap<>();
            spinnerAdapterMap = new HashMap<>();

            if (isFromChannel && isNewChannel) {
                // User selected a sub channel an it is new one.

                for (int i = 0; i < bmodel.newOutletAttributeHelper.getAttributeParentList().size(); i++) {

                    final NewOutletAttributeBO parentBO;
                    parentBO = bmodel.newOutletAttributeHelper.getAttributeParentList().get(i);

                    if (mNewChannelAttributeList.contains(parentBO.getAttrId())) {

                        LinearLayout layout = new LinearLayout(getActivity());

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

                int rowCount = attributeList.size();
                updateRetailerAttribute(attributeList);

                for (int i = 0; i < rowCount; i++) {

                    final NewOutletAttributeBO parentBO;
                    parentBO = attributeHeaderList.get(i);

                    //Allowing only if parent attribute is available in common list or channel's(from Db) attribute
                    if ((isCommon && (mCommonAttributeList.contains(parentBO.getAttrId()) || mChannelAttributeList.contains(parentBO.getAttrId())))
                            ) {

                        LinearLayout layout = new LinearLayout(getActivity());

                        // setting tag as channel, used to remove channel views particularly and update new one if channel changed
                        if (mChannelAttributeList.contains(parentBO.getAttrId()))
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

            }
        } catch (Exception ex) {

        }

        return parentLayout;
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


    private RelativeLayout getImageView() {
        int width = (int) getResources().getDimension(R.dimen.user_logo_width);
        int height = (int) getResources().getDimension(R.dimen.user_logo_height);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(width,
                height);
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout parentLayout = new RelativeLayout(getActivity());

        imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.face);
        parentLayout.addView(imageView, imageParams);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean isLatLongMenuAvail = false;
                for (int conf = 0; conf < profileConfig.size(); conf++) {
                    if(profileConfig.get(conf).getConfigCode().equalsIgnoreCase("PROFILE08") &&
                            profileConfig.get(conf).getModule_Order() == 1) {
                        for (int conf1 = 0; conf1 < profileConfig.size(); conf1++) {
                            if (profileConfig.get(conf1).getConfigCode().equalsIgnoreCase("PROFILE31") &&
                                    profileConfig.get(conf1).getModule_Order() == 1) {
                                isLatLongMenuAvail = true;
                                break;
                            }
                        }
                    }
                }
                //Dont allow if Fun57 is enabled and mandatory,
                //Generally check for location and show toast if no location found.
                if(!isLatLongMenuAvail && bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE && (LocationUtil.latitude == 0 || LocationUtil.longitude == 0)
                        || (bmodel.configurationMasterHelper.retailerLocAccuracyLvl!=0 && LocationUtil.accuracy > bmodel.configurationMasterHelper.retailerLocAccuracyLvl)){

                    Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                    return false;
                }else {
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
                            openImage(new File(cameraFilePath).getAbsolutePath());
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.unloadimage),
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (bmodel.retailerMasterBO.getProfileImagePath() != null &&
                        !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                    File filePath = null;
                    if (bmodel.profilehelper.hasProfileImagePath(bmodel.retailerMasterBO) &&
                            bmodel.retailerMasterBO.getProfileImagePath() != null && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                + DataMembers.photoFolderName + "/" + path);
                    } else if (bmodel.retailerMasterBO.getProfileImagePath() != null &&
                            !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.PROFILE + "/"
                                + path);
                    }

                    if (filePath != null && filePath.exists()) {
                        try {
                            openImage(filePath.getAbsolutePath());
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
        if (bmodel.profilehelper.hasProfileImagePath(retailerObj) &&
                retailerObj.getProfileImagePath() != null && !"".equals(retailerObj.getProfileImagePath())) {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            String filePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                    + DataMembers.photoFolderName + "/" + path;
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
                    + bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid()
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
        Intent in;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                if (bmodel.configurationMasterHelper.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) {
                    lat = LocationUtil.latitude + "";
                    longitude = LocationUtil.longitude + "";

                    if (lat.equals("") || Double.parseDouble(lat) == 0 || longitude.equals("") || Double.parseDouble(longitude) == 0) {
                        Toast.makeText(getActivity(), "Location not captured.", Toast.LENGTH_LONG).show();
                    } else {
                        profileConfig.add(new ConfigureBO("PROFILE08", "Latitude", lat, 0, 0, 0));
                        profileConfig.add(new ConfigureBO("PROFILE31", "Latitude", longitude, 0, 0, 0));
                        Toast.makeText(getActivity(), "Location captured successfully.", Toast.LENGTH_LONG).show();
                    }
                }

                Uri uri = bmodel.profilehelper
                        .getUriFromFile(HomeScreenFragment.photoPath + "/" + imageFileName);
                cameraFilePath = HomeScreenFragment.photoPath + "/" + imageFileName;
                imageView.setImageDrawable(null);
                imageView.invalidate();
                imageView.setImageURI(uri);
                imageView.refreshDrawableState();
            }
        }
        if (requestCode == LATLONG_CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                latlongCameraFilePath = HomeScreenFragment.photoPath + "/" + bmodel.latlongImageFileName;
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


    private boolean validateEditProfile() {
        boolean validate = true;
        try {
            int size = profileConfig.size();
            for (int i = 0; i < size; i++) {

                String configCode = profileConfig.get(i).getConfigCode();
                if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PROFILE06")
                        && profileConfig.get(i).getModule_Order() == 1) {

                    if (channel.getSelectedItem().toString().toLowerCase()
                            .contains("select")) {
                        channel.requestFocus();
                        validate = false;
                        Toast.makeText(getActivity(),
                                "Choose " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PROFILE07")
                        && profileConfig.get(i).getModule_Order() == 1) {
                    if (subchannel.getSelectedItem().toString().toLowerCase()
                            .contains("select")) {
                        subchannel.requestFocus();
                        validate = false;
                        Toast.makeText(getActivity(),
                                "Choose " + profileConfig.get(i).getMenuName(), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    }

                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PROFILE30")
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

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


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PROFILE12")
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

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


                } else if (profileConfig.get(i).getConfigCode()
                        .equalsIgnoreCase("PROFILE10")
                        && profileConfig.get(i).getModule_Order() == 1 && profileConfig.get(i).getMaxLengthNo() > 0) {

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


                } else if (configCode.equals("PROFILE58") && profileConfig.get(i).getModule_Order() == 1) {
                    ArrayList<NewOutletAttributeBO> selectedAttributeLevel = new ArrayList<>();
                    boolean isAdded = true;

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
                }


            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return validate;
    }


    //handled only for single selection products
    private ArrayList<StandardListBO> computeSelectedPriorityList() {
        ArrayList<StandardListBO> tempList = new ArrayList<>();
        ArrayList<String> products = bmodel.newOutletHelper.downloadPriorityProductsForRetailer(retailerObj.getRetailerID());

        if (products == null)
            products = new ArrayList<String>();

        if (selectedPriorityProductList != null) {
            for (StandardListBO bo : selectedPriorityProductList) {
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
                        bo.setStatus("D");
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

                String configCode = profileConfig.get(i).getConfigCode();

                if (configCode.equals("PROFILE02") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE03") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE04") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE05") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE30") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE06") && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    ChannelBO cBo = (ChannelBO) channel.getSelectedItem();
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(cBo.getChannelId() + "");
                } else if (configCode.equals("PROFILE07") && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (channelMaster != null)
                        profileConfig.get(i).setMenuNumber(((SpinnerBO) subchannel
                                .getSelectedItem()).getId() + "");
                } else if (configCode.equals("PROFILE43") && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontractStatusList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contractSpinner
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals("PROFILE08") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(lat)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        profileConfig.get(i).setMenuNumber(lat);
                    }
                } else if (configCode.equals("PROFILE31") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(longitude)) {
                        profileConfig.get(i).setMenuNumber("0.0");
                    } else {
                        profileConfig.get(i).setMenuNumber(longitude);
                    }
                } else if (configCode.equals("PROFILE63") && profileConfig.get(i).getModule_Order() == 1) {
                    if (bmodel.latlongImageFileName == null || "".equals(bmodel.latlongImageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(bmodel.latlongImageFileName);
                    }
                } else if (configCode.equals("PROFILE09") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString())));
                    }
                } else if (configCode.equals("PROFILE41") && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontactTitleList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contactTitleSpinner1
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals("CT1TITLE") && profileConfig.get(i).getModule_Order() == 1) {
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

                } else if (configCode.equals("LNAME") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[lName1_editText_index].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[lName1_editText_index].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE10") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE11") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(bmodel.validateInput(editText[i].getText().toString()))) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                bmodel.validateInput(editText[i].getText().toString()))
                        );
                    }
                } else if (configCode.equals("PROFILE42") && profileConfig.get(i).getModule_Order() == 1) {
                    profileConfig.get(i).setMenuNumber("0");
                    if (mcontactTitleList != null)
                        profileConfig.get(i).setMenuNumber(((NewOutletBO) contactTitleSpinner2
                                .getSelectedItem()).getListId() + "");
                } else if (configCode.equals("CT2TITLE") && profileConfig.get(i).getModule_Order() == 1) {
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
                } else if (configCode.equals("LNAME2") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[lName2_editText_index].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[lName2_editText_index].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE13") && profileConfig.get(i).getModule_Order() == 1) {
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
                } else if (configCode.equals("PROFILE14") && profileConfig.get(i).getModule_Order() == 1) {
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
                } else if (configCode.equals("PROFILE15") && profileConfig.get(i).getModule_Order() == 1) {
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
                } else if (configCode.equals("PROFILE39") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE40") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE25") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber(
                                editText[i].getText().toString());
                    }
                } else if (configCode.equals("PROFILE20") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE26") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE27") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE28") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE53") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE54") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE55") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE60") && profileConfig.get(i).getModule_Order() == 1) {
                    if (imageFileName == null || "".equals(imageFileName)) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(imageFileName);
                    }
                } else if (configCode.equals("PROFILE61") && profileConfig.get(i).getModule_Order() == 1) {
                    if (TextUtils.isEmpty(editText[i].getText().toString())) {
                        profileConfig.get(i).setMenuNumber("");
                    } else {
                        profileConfig.get(i).setMenuNumber(SDUtil.removeQuotes(
                                editText[i].getText().toString()));
                    }
                } else if (configCode.equals("PROFILE62") && profileConfig.get(i).getModule_Order() == 1) {
                    if (!inSEZcheckBox.isChecked()) {
                        profileConfig.get(i).setMenuNumber("0");
                    } else {
                        profileConfig.get(i).setMenuNumber("1");
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
            showAlert(
                    getActivity().getResources().getString(
                            R.string.profile_updated_scccess), 0);


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


    private class DownloadAsync extends
            AsyncTask<Integer, Integer, Boolean> {


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            updateLocationMasterList();
            bmodel.mRetailerHelper.loadContractData();
            channelMaster = bmodel.channelMasterHelper.getChannelMaster();

            return true;
        }

        protected void onPostExecute(Boolean result) {

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

    private void takePhoto(RetailerMasterBO retailerMasterBO, boolean isForLatLong) {
        if (bmodel.isExternalStorageAvailable()) {
            if (!isForLatLong) {
                imageFileName = "PRO_" + retailerMasterBO.getRetailerID() + "_"
                        + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            } else {
                bmodel.latlongImageFileName = "LATLONG_" + retailerMasterBO.getRetailerID() + "_"
                        + Commons.now(Commons.DATE_TIME) + "_img.jpg";
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
            Toast.makeText(
                    getActivity(),
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setProfileImage() {
        String[] imgPaths = retailerObj.getProfileImagePath().split("/");
        String path = imgPaths[imgPaths.length - 1];
        File imgFile = new File(getActivity().getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS)
                + "/"
                + bmodel.userMasterHelper.getUserMasterBO()
                .getUserid()
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

    /*
     * Open the Image in Photo Gallery while onClick
     */
    private void openImage(String fileName) {
        if (fileName.trim().length() > 0) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + fileName),
                        "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    }
}
