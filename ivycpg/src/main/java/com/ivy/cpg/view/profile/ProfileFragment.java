package com.ivy.cpg.view.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static com.ivy.cpg.view.profile.ProfileActivity.retailerLat;
import static com.ivy.cpg.view.profile.ProfileActivity.retailerLng;
import static com.ivy.ui.profile.ProfileConstant.*;

/*
 * Created by nivetha.s on 09-12-2015.
 */
public class ProfileFragment extends IvyBaseFragment {

    private View view;
    private static BusinessModel bmodel;

    private int locid = 0;
    // --Commented out by Inspection (12/27/2017 3:31 PM):public int screenwidth = 0;

    private double mnth_actual = 0.0, Balance = 0.0;
    private double outStanding = 0.0, invoiceAmount = 0.0;
    private double loyaltyPoints = 0, loyaltyBalancePoints = 0;

    private String phoneNoCall;
    private String retailerCreditLimit = "0.0";
    private String physicalLocation = "", gstType = "", taxType = "";

    private boolean is_contact_title1 = false, is_contact_title2 = false;
    private boolean isGstType;

    private Vector<ConfigureBO> profileConfig = new Vector<>();
    private Vector<RetailerMasterBO> mNearbyRetIds;
    private Vector<RetailerMasterBO> mSelectedIds = new Vector<>();

    private ArrayList<NewOutletBO> finalProfileList;
    private ArrayList<NewOutletBO> mcontactTitleList;
    private ArrayList<NewOutletAttributeBO> attributeList;

    private ToggleButton btn_Deactivate;
    private TextView storeTxt, addressTxt, addressTxt3, cspTxt, rContTxt, rcodeTxt;
    private RecyclerView recyclerView;
    private LinearLayout contact_lay;

    private RetailerMasterBO retailerObj;
    private TransferUtility transferUtility;

    private boolean _hasLoadedOnce = false;
    private static final int REQUEST_PHONE_CALL = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        if (!_hasLoadedOnce) {
            initializeViews();
        }
        outletInfo();
        LoadDataAsync async = new LoadDataAsync();
        if (async.getStatus() == AsyncTask.Status.PENDING)
            async.execute();
        updateRetailerStatus();
        return view;
    }


    private void showDeActivateAlert(String msg, final boolean isData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isData) {
                    bmodel.profilehelper.deleteRetailerEditRecords(bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                }
                bmodel.profilehelper.deActivateRetailer(bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                btn_Deactivate.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_rounded_corner_red, null));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btn_Deactivate.setChecked(false);
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        DownloadAsync downloadAsync = new DownloadAsync();
        if (downloadAsync.getStatus() == AsyncTask.Status.PENDING)
            downloadAsync.execute();
    }


    private void initializeViews() {
        storeTxt = (TextView) view.findViewById(R.id.profile_str_name);
        rcodeTxt = (TextView) view.findViewById(R.id.profile_retailer_code);
        addressTxt = (TextView) view.findViewById(R.id.profile_add_onetwo);
        addressTxt3 = (TextView) view.findViewById(R.id.profile_add_three);
        cspTxt = (TextView) view.findViewById(R.id.profile_csp);
        rContTxt = (TextView) view.findViewById(R.id.profile_retailer_cno);
        contact_lay = (LinearLayout) view.findViewById(R.id.contact_lay);
        recyclerView = (RecyclerView) view.findViewById(R.id.profile_recyclerview);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(false);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setFocusable(false);
        }

        ImageView callLinkBtn = (ImageView) view.findViewById(R.id.call_btnimg);
        boolean fromHomeClick = getArguments().getBoolean("fromHomeClick");
        boolean non_visit = getArguments().getBoolean("non_visit");

        storeTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        rcodeTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        addressTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        addressTxt3.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        cspTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        rContTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        GridLayoutManager gridlaymanager;
        if (is7InchTablet) {
            gridlaymanager = new GridLayoutManager(getActivity(), 3);
        } else {
            gridlaymanager = new GridLayoutManager(getActivity(), 2);
        }

        recyclerView.setLayoutManager(gridlaymanager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).size(1).color(Color.parseColor("#EEEEEE")).margin(22, 22).build());

        if (fromHomeClick || non_visit) {
            callLinkBtn.setVisibility(View.GONE);
        } else {
            if (retailerLat != 0.0 && retailerLng != 0.0) {
                callLinkBtn.setVisibility(View.VISIBLE);
            }
        }

        /*
         * get NearByRetailer based on distributed id
         */
        try {
            ArrayList<String> ids = bmodel.newOutletHelper.getNearbyRetailerIds(bmodel.getRetailerMasterBO().getRetailerID());
            if (ids != null) {
                Vector<RetailerMasterBO> retailersList = bmodel.newOutletHelper.getLinkRetailerListByDistributorId().get(bmodel.getRetailerMasterBO().getDistributorId());
                mNearbyRetIds = new Vector<>();
                if (retailersList != null)
                    for (int i = 0; i < ids.size(); i++) {
                        for (RetailerMasterBO bo : retailersList) {
                            if (bo.getRetailerID().equals(ids.get(i))) {
                                mNearbyRetIds.add(bo);
                            }
                        }

                    }
            }
        } catch (Exception e) {
            Commons.print("Null Pointer Exception in Distributed ID" + e);
        }

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
        retailerObj = bmodel.getRetailerMasterBO();


        if (retailerObj.getContactnumber() != null && !retailerObj.getContactnumber().isEmpty()) {
            if (retailerObj.getContactnumber().contains("-")) {
                phoneNoCall = retailerObj.getContactnumber().replace("-", "");
            } else {
                phoneNoCall = retailerObj.getContactnumber();
            }
        } else {
            callLinkBtn.setVisibility(View.GONE);
        }

        callLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNoCall));
                int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CALL_PHONE);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    getActivity().startActivity(callIntent);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
            }
        });
        _hasLoadedOnce = true;
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);
        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                _hasLoadedOnce = true;
            }
        }
    }


    private void updateRetailerStatus() {
        if (bmodel.configurationMasterHelper.IS_DEACTIVATE_RETAILER) {
            LinearLayout ll_status = view.findViewById(R.id.ll_Deactivate);
            ll_status.setVisibility(View.VISIBLE);

            btn_Deactivate = view.findViewById(R.id.btn_Deactivate);
            btn_Deactivate.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
            if (bmodel.profilehelper.isDeActivated(bmodel.getAppDataProvider().getRetailMaster().getRetailerID())) {
                btn_Deactivate.setChecked(true);
            } else
                btn_Deactivate.setChecked(false);

            btn_Deactivate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btn_Deactivate.isChecked()) {
                        if (bmodel.profilehelper.isRetailerEditDetailAvailable(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()))
                            showDeActivateAlert(getResources().getString(R.string.retailer_edit_records_available), true);
                        else {
                            showDeActivateAlert(getResources().getString(R.string.do_you_want_to_deactivate_retailer), false);

                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.do_you_want_to_activate_retailer);
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bmodel.profilehelper.deleteRetailerEditRecords(bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                                btn_Deactivate.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_rounded_corner_orange, null));
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                btn_Deactivate.setChecked(true);
                            }
                        });
                        bmodel.applyAlertDialogTheme(builder);

                    }
                }
            });
        }

    }

    private void outletInfo() {


        //bmodel.configurationMasterHelper.downloadProfileModuleConfig();
        profileConfig = new Vector<>();
        profileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();

        int size = 0;
        if (profileConfig != null) {
            size = profileConfig.size();
            for (int i = 0; i < size; i++) {

                int flag = profileConfig.get(i).isFlag();
                String configCode = profileConfig.get(i).getConfigCode();

                if (flag == 1) {
                    switch (configCode) {
                        case "PROFILE01":

                            if (retailerObj.getRetailerCode() != null) {
                                rcodeTxt.setVisibility(View.VISIBLE);
                                rcodeTxt.setText(retailerObj
                                        .getRetailerCode());
                            } else {
                                rcodeTxt.setVisibility(View.GONE);
                            }

                            break;
                        case "PROFILE02":
                            if (retailerObj.getRetailerName() != null) {
                                storeTxt.setVisibility(View.VISIBLE);
                                storeTxt.setText(retailerObj
                                        .getRetailerName());
                            } else {
                                storeTxt.setVisibility(View.GONE);
                            }

                            break;
                        case "PROFILE03":
                            if (retailerObj.getAddress1() != null) {
                                addressTxt.setVisibility(View.VISIBLE);
                                addressTxt.setText(retailerObj
                                        .getAddress1());
                            } else {
                                addressTxt.setVisibility(View.GONE);
                            }

                            break;
                        case "PROFILE04":
                            if (retailerObj.getAddress2() != null || addressTxt.getText().toString().length() > 0) {
                                addressTxt.setVisibility(View.VISIBLE);
                                if (addressTxt.getText().toString().length() > 0)
                                    addressTxt.append(", " + retailerObj
                                            .getAddress2());
                                else
                                    addressTxt.append(retailerObj
                                            .getAddress2());
                            } else {
                                addressTxt.setVisibility(View.GONE);
                            }

                            break;
                        case "PROFILE05":
                            if (retailerObj.getAddress3() != null) {
                                addressTxt3.setVisibility(View.VISIBLE);
                                addressTxt3.setText(retailerObj
                                        .getAddress3());
                            } else {
                                addressTxt3.setVisibility(View.GONE);
                            }


                            break;
                        case "PROFILE30":
                            if (retailerObj.getContactnumber() != null) {
                                rContTxt.setVisibility(View.VISIBLE);
                                contact_lay.setVisibility(View.VISIBLE);
                                rContTxt.setText(retailerObj.getContactnumber());
                            } else {
                                rContTxt.setVisibility(View.GONE);
                                contact_lay.setVisibility(View.GONE);
                            }

                            break;

                        case "PROFILE38": {
                            switch (configCode) {
                                case "PROFILE39":
                                    if ((retailerObj.getCity() != null) && (!retailerObj.getCity().equalsIgnoreCase("")) && (!retailerObj.getCity().equalsIgnoreCase("null")))
                                        cspTxt.setText(retailerObj.getCity());
                                    break;
                                case "PROFILE40":
                                    if ((retailerObj.getState() != null) && (!retailerObj.getState().equalsIgnoreCase("")) && (!retailerObj.getState().equalsIgnoreCase("null")))
                                        cspTxt.append(", " + retailerObj.getState());
                                    break;
                                case "PROFILE38":
                                    if ((retailerObj.getPincode() != null) && (!retailerObj.getPincode().equalsIgnoreCase("")) && (!retailerObj.getPincode().equalsIgnoreCase("null")))
                                        cspTxt.append(" -" + retailerObj.getPincode());
                                    break;
                            }
                            cspTxt.setVisibility(View.VISIBLE);
                            break;
                        }
                        case "PROFILE39": {
                            switch (configCode) {
                                case "PROFILE39":
                                    if ((retailerObj.getCity() != null) && (!retailerObj.getCity().equalsIgnoreCase("")) && (!retailerObj.getCity().equalsIgnoreCase("null")))
                                        cspTxt.setText(retailerObj.getCity());
                                    break;
                                case "PROFILE40":
                                    if ((retailerObj.getState() != null) && (!retailerObj.getState().equalsIgnoreCase("")) && (!retailerObj.getState().equalsIgnoreCase("null")))
                                        cspTxt.append(", " + retailerObj.getState());
                                    break;
                                case "PROFILE38":
                                    if ((retailerObj.getPincode() != null) && (!retailerObj.getPincode().equalsIgnoreCase("")) && (!retailerObj.getPincode().equalsIgnoreCase("null")))
                                        cspTxt.append(" -" + retailerObj.getPincode());
                                    break;
                            }
                            cspTxt.setVisibility(View.VISIBLE);
                            break;
                        }
                        case "PROFILE40": {
                            switch (configCode) {
                                case "PROFILE39":
                                    if ((retailerObj.getCity() != null) && (!retailerObj.getCity().equalsIgnoreCase("")) && (!retailerObj.getCity().equalsIgnoreCase("null")))
                                        cspTxt.setText(retailerObj.getCity());
                                    break;
                                case "PROFILE40":
                                    if ((retailerObj.getState() != null) && (!retailerObj.getState().equalsIgnoreCase("")) && (!retailerObj.getState().equalsIgnoreCase("null")))
                                        cspTxt.append(", " + retailerObj.getState());
                                    break;
                                case "PROFILE38":
                                    if ((retailerObj.getPincode() != null) && (!retailerObj.getPincode().equalsIgnoreCase("")) && (!retailerObj.getPincode().equalsIgnoreCase("null")))
                                        cspTxt.append(" -" + retailerObj.getPincode());
                                    break;
                            }
                            cspTxt.setVisibility(View.VISIBLE);
                            break;
                        }
                        case "PROFILE60":

                            if (retailerObj.getProfileImagePath() != null && !"".equals(retailerObj.getProfileImagePath())) {
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
                                    AWSConnectionHelper.getInstance().setAWSDBValues(bmodel.getApplicationContext());
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put(DataMembers.IMG_DOWN_URL + "" + retailerObj.getProfileImagePath(),
                                            DataMembers.PROFILE);
                                    transferUtility = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), getActivity());
                                    Thread downloaderThread = new DownloaderThreadNew(getActivity(),
                                            activityHandler, hashMap,
                                            bmodel.userMasterHelper.getUserMasterBO()
                                                    .getUserid(), transferUtility,new HashMap<String, String>());
                                    downloaderThread.start();
                                }
                            }
                            break;
                    }
                }
            }
        }

    }

    /**
     * RecyclerAdapter for Profile data.
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<ProfileFragment.RecyclerViewAdapter.ViewHolder> {

        private final ArrayList<NewOutletBO> items;

        public RecyclerViewAdapter(ArrayList<NewOutletBO> items) {
            this.items = items;
        }

        @Override
        public ProfileFragment.RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_profile_list_item, parent, false);
            return new ProfileFragment.RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProfileFragment.RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.projectObj = items.get(position);
            holder.menuText.setText(holder.projectObj.getmName());
            holder.valueText.setText(holder.projectObj.getValueText());
            holder.itemView.setTag(holder.projectObj);
            Commons.print("view constructtime " + DateTimeUtils.now(DateTimeUtils.TIME));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView menuText;
            private final TextView valueText;
            private NewOutletBO projectObj;

            public ViewHolder(View itemView) {
                super(itemView);
                menuText = (TextView) itemView.findViewById(R.id.menu_name);
                valueText = (TextView) itemView.findViewById(R.id.value_name);
                menuText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                valueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }
        }
    }

    /**
     * Method used to load profile data.
     */
    private void loadProfileDatas() {
        retailerObj = bmodel.getRetailerMasterBO();
        //   bmodel.configurationMasterHelper.downloadProfileModuleConfig();
        Vector<ConfigureBO> mTempProfileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();

        ArrayList<String> unWantedMenus = new ArrayList<>();
        unWantedMenus.add("PROFILE01");
        unWantedMenus.add("PROFILE02");
        unWantedMenus.add("PROFILE03");
        unWantedMenus.add("PROFILE04");
        unWantedMenus.add("PROFILE05");
        unWantedMenus.add("PROFILE21");
        unWantedMenus.add("PROFILE30");
        unWantedMenus.add("PROFILE44");
        unWantedMenus.add("PROFILE45");

        profileConfig = new Vector<>();

        for (int r = 0; r < mTempProfileConfig.size(); r++) {
            String configCode = mTempProfileConfig.get(r).getConfigCode();

            if (!unWantedMenus.contains(configCode)) {
                profileConfig.add(mTempProfileConfig.get(r));
            }
        }
        double mnth_acheive = (retailerObj
                .getMonthly_acheived());
        double day_acheive;
        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            day_acheive = bmodel.getInvoiceAmount();
        } else {
            day_acheive = bmodel.getOrderValue();
        }

        mnth_actual = day_acheive + mnth_acheive;

        Balance = (retailerObj.getMonthly_target())
                - (mnth_actual);

        for (ConfigureBO configureBO : profileConfig) {
            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE41") && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title1 = true;

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE42") && configureBO.isFlag() == 1 && configureBO.getModule_Order() == 1)
                is_contact_title2 = true;

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE47") && configureBO.isFlag() == 1)
                outStanding = bmodel.getRetailerInvoiceAmount() - bmodel.getOutStandingInvoiceAmount();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE48") && configureBO.isFlag() == 1)
                retailerCreditLimit = bmodel.getRetailerMasterBO().getProfile_creditLimit();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE49") && configureBO.isFlag() == 1)
                invoiceAmount = bmodel.getInvoiceAmount();

            //Calculating Loyalty Points
            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE64") && configureBO.isFlag() == 1)
                loyaltyPoints = bmodel.getLoyaltyPoints();

            if (configureBO.getConfigCode().equalsIgnoreCase("PROFILE65") && configureBO.isFlag() == 1)
                loyaltyBalancePoints = bmodel.getLoyaltyBalancePoints();

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

        }

        if (is_contact_title2 || is_contact_title1) {

            mcontactTitleList = new ArrayList<>();
            mcontactTitleList.add(0, new NewOutletBO(-1, getResources().getString(R.string.select_str) + " Title Name"));
            mcontactTitleList.addAll(bmodel.newOutletHelper.getContactTitleList());
            mcontactTitleList.add(bmodel.newOutletHelper.getContactTitleList().size() + 1, new NewOutletBO(0, "Others"));
            ArrayAdapter<NewOutletBO> contactTitleAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontactTitleList);
            contactTitleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        finalProfileList = new ArrayList<>();
        NewOutletBO outletBO;
        int size = profileConfig.size();

        int mPrevMenuNumber = 0;

        for (int i = 0; i < size; i++) {
            String menuNo = profileConfig.get(i).getMenuNumber();
            menuNo = (menuNo.equals("") ? "0" : menuNo);

            String configCode = profileConfig.get(i).getConfigCode();
            if (unWantedMenus.contains(configCode))
                continue;

            for (int j = (mPrevMenuNumber + 1); j < SDUtil.convertToInt(menuNo); j++) {
                outletBO = new NewOutletBO();
                outletBO.setmName(" ");
                outletBO.setValueText(" ");
                finalProfileList.add(outletBO);
                mPrevMenuNumber++;
            }
            if (profileConfig.get(i).isFlag() == 1) {
                loadProfileList(i);
            }
            mPrevMenuNumber = SDUtil.convertToInt(menuNo);
        }

    }


    private void loadProfileList(int i) {
        NewOutletBO outletBO;
        String mName = profileConfig.get(i).getMenuName();
        String configCode = profileConfig.get(i).getConfigCode();
        //  Log.e("ProfileCode",configCode+"\t"+mName);
        switch (configCode) {

            case "PROFILE06": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(bmodel.channelMasterHelper.getChannelName(retailerObj.getChannelID() + ""));
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE07": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(bmodel.subChannelMasterHelper
                        .getSubChannelName(retailerObj.getSubchannelid() + ""));
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE80": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getTinExpDate());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE29": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(bmodel.mRetailerHelper.getREclassification(retailerObj.getClassid()));
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE41": {
                NewOutletBO newBo;
                String text = "";
                if (is_contact_title1) {
                    String mcontact_title1_lovId;
                    mcontact_title1_lovId = retailerObj.getContact1_titlelovid();

                    for (int j = 0; j < mcontactTitleList.size(); j++) {
                        newBo = mcontactTitleList.get(j);
                        if ((mcontact_title1_lovId != null) && (!mcontact_title1_lovId.equalsIgnoreCase(""))
                                && (mcontact_title1_lovId.equals("" + newBo.getContact1titlelovid())))
                            text = newBo.getContact1title();
                    }
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE42": {
                NewOutletBO newBo;
                String text = "";
                if (is_contact_title2) {
                    String mcontact_title2_lovId = retailerObj.getContact1_titlelovid();

                    for (int j = 0; j < mcontactTitleList.size(); j++) {
                        newBo = mcontactTitleList.get(j);
                        if ((mcontact_title2_lovId != null) && (mcontact_title2_lovId.equals("" + newBo.getContact1titlelovid())))
                            text = newBo.getContact1title();
                    }
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE43": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(bmodel.newOutletHelper
                        .getContractStatus(retailerObj
                                .getContractLovid()));
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE09": {
                String text = "";
                if (retailerObj.getContactname() != null) {

                    if (retailerObj.getContact1_titlelovid() != null && is_contact_title1 && retailerObj.getContactLname() != null) {
                        if (retailerObj.getContact1_title() != null && retailerObj.getContact1_title().length() > 0)
                            text = retailerObj.getContact1_title() + ":" + retailerObj.getContactname() + " " + retailerObj.getContactLname();
                        else
                            text = retailerObj.getContactname() + " " + retailerObj.getContactLname();
                    } else if (retailerObj.getContact1_titlelovid() != null && is_contact_title1 && retailerObj.getContact1_title() != null && retailerObj.getContact1_title().length() > 0)
                        text = retailerObj.getContact1_title() + ":" + retailerObj.getContactname();
                    else
                        text = retailerObj.getContactname();
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE10": {
                String text = "";
                if (retailerObj.getContactnumber1() != null
                        && !retailerObj.getContactnumber1().equals(
                        "null")) {
                    text = retailerObj.getContactnumber1();
                }

                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE11": {
                String text = "";
                if (retailerObj.getContactname2() != null) {
                    if (retailerObj.getContact2_titlelovid() != null && is_contact_title2 && retailerObj.getContactLname2() != null) {
                        if (retailerObj.getContact2_title() != null && retailerObj.getContact2_title().length() > 0)
                            text = retailerObj.getContact2_title() + ":" + retailerObj.getContactname2() + " " + retailerObj.getContactLname2();
                        else
                            text = retailerObj.getContactname2() + " " + retailerObj.getContactLname2();
                    } else if (retailerObj.getContact2_titlelovid() != null && is_contact_title2 && retailerObj.getContact2_title() != null && retailerObj.getContact2_title().length() > 0)
                        text = retailerObj.getContact2_title() + ":" + retailerObj.getContactname2();
                    else
                        text = retailerObj.getContactname2();
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE12": {
                String text = "";
                if (retailerObj.getContactnumber2() != null
                        && !retailerObj.getContactnumber2().equals(
                        "null")) {
                    text = retailerObj
                            .getContactnumber2();
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE13": {
                try {
                    String value = "";
                    locid = retailerObj.getLocationId();
                    if (locid != 0) {
                        String[] loc1 = bmodel.mRetailerHelper.getParentLevelName(
                                locid, false);
                        value = loc1[1];
                    }
                    outletBO = new NewOutletBO();
                    outletBO.setmName(mName);
                    outletBO.setValueText(value);
                    finalProfileList.add(outletBO);
                } catch (Exception e) {
                    Commons.printException(e);
                }
                break;
            }
            case "PROFILE14": {
                try {
                    String value = "";
                    String[] loc2 = bmodel.mRetailerHelper.getParentLevelName(
                            locid, true);
                    if (loc2 != null) {
                        value = loc2[1];
                    }
                    outletBO = new NewOutletBO();
                    outletBO.setmName(mName);
                    outletBO.setValueText(value);
                    finalProfileList.add(outletBO);
                } catch (Exception e) {
                    Commons.printException(e);
                }
                break;
            }
            case "PROFILE16": {
                String text;

                text = bmodel
                        .formatValue(retailerObj.getMonthly_target());
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE17": {
                String text = bmodel.formatValue(mnth_actual);
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE18": {
                String text = bmodel.formatValue(Balance);
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE19": {
                String text = SDUtil.getWithoutExponential(retailerObj
                        .getCreditLimit());
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE20": {
                String text = retailerObj.getRField1();
                if (bmodel.configurationMasterHelper.CALC_OUTSTANDING) {
                    double bal = (retailerObj.getCreditLimit() - outStanding);

                    if (bal > 0.0)
                        text = "0";
                    else
                        text = bal + "";
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE22": {
                String text = bmodel.mRetailerHelper.getContractType();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE23": {
                String text = DateTimeUtils.convertFromServerDateToRequestedFormat(bmodel.mRetailerHelper.getContractExpiryDate(), ConfigurationMasterHelper.outDateFormat);
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE24": {
                String text = ((retailerObj.getVisit_frequencey() > 0) ? retailerObj.getVisit_frequencey() : "0") + "";
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE25": {
                String text = retailerObj.getCreditDays() + "";
                text = text.equalsIgnoreCase("-1") ? "0" : text;
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE26": {
                String text = retailerObj
                        .getRfield2() + "";
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE27": {
                String text = retailerObj
                        .getCredit_invoice_count();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE28": {
                String text = retailerObj.getRField4();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (profileConfig.get(i).getHasLink() == 1)
                    for (RetailerFlexBO retailerFlexBO : bmodel.newOutletHelper.downloadRetailerFlexValues("RFIELD4"))
                        if (retailerFlexBO.getId().equals(text)) {
                            text = retailerFlexBO.getName();
                            break;
                        }
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE32": {
                String text = bmodel.mRetailerHelper.getContractID();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE33": {
                String text = DateTimeUtils.convertFromServerDateToRequestedFormat(bmodel.mRetailerHelper.getContractStartDate(), ConfigurationMasterHelper.outDateFormat);
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE34": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getTinnumber());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE35": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getDob());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE36": {
                mSelectedIds = mNearbyRetIds;
                String text = null;

                if (mSelectedIds != null && mSelectedIds.size() > 0) {
                    for (RetailerMasterBO bo : mSelectedIds) {
                        text = DataMembers.CR1 + bo.getRetailerName();
                    }

                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(text);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE37": {
                final String taxName = bmodel.newOutletHelper.getListName(retailerObj.getTaxTypeId(), "TAX_TYPE");
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(taxName);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE46": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (bmodel.beatMasterHealper.getBeatMaster().size() > 0)
                    outletBO.setValueText(bmodel.beatMasterHealper.
                            getBeatMasterBOByID(retailerObj.
                                    getBeatID()).getBeatDescription());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE47": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(outStanding + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE48": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerCreditLimit + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE49": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(invoiceAmount + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE50": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(physicalLocation + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE51": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(taxType + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE52": {
                if (isGstType) {
                    outletBO = new NewOutletBO();
                    outletBO.setmName(mName);
                    outletBO.setValueText(gstType + "");
                    finalProfileList.add(outletBO);
                }
                break;
            }
            case "PROFILE53": {
                String rField5 = retailerObj.getRField5();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (profileConfig.get(i).getHasLink() == 1)
                    for (RetailerFlexBO retailerFlexBO : bmodel.newOutletHelper.downloadRetailerFlexValues("RFIELD5"))
                        if (retailerFlexBO.getId().equals(rField5)) {
                            rField5 = retailerFlexBO.getName();
                            break;
                        }
                outletBO.setValueText(rField5);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE54": {
                String rField6 = retailerObj.getRField6();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (profileConfig.get(i).getHasLink() == 1)
                    for (RetailerFlexBO retailerFlexBO : bmodel.newOutletHelper.downloadRetailerFlexValues("RFIELD6"))
                        if (retailerFlexBO.getId().equals(rField6)) {
                            rField6 = retailerFlexBO.getName();
                            break;
                        }
                outletBO.setValueText(rField6);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE55": {
                String rField7 = retailerObj.getRField7();
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (profileConfig.get(i).getHasLink() == 1)
                    for (RetailerFlexBO retailerFlexBO : bmodel.newOutletHelper.downloadRetailerFlexValues("RFIELD7"))
                        if (retailerFlexBO.getId().equals(rField7)) {
                            rField7 = retailerFlexBO.getName();
                            break;
                        }

                outletBO.setValueText(rField7);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE56": {

                String distributorName = "";
                for (DistributorMasterBO bo : bmodel.distributorMasterHelper.getDistributors()) {
                    if (SDUtil.convertToInt(bo.getDId()) == (retailerObj.getDistributorId())) {
                        distributorName = bo.getDName();
                    }
                }
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(distributorName);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE57": {

                ArrayList<StandardListBO> mPriorityProductList = bmodel.newOutletHelper.downloadPriorityProducts();
                ArrayList<String> products = bmodel.newOutletHelper.downloadPriorityProductsForRetailer(retailerObj.getRetailerID());
                StringBuilder sb = new StringBuilder();
                if (products != null) {
                    for (StandardListBO bo : mPriorityProductList) {
                        if (products.contains(bo.getListID())) {
                            bo.setChecked(true);

                            if (sb.length() > 0)
                                sb.append(",");
                            sb.append(bo.getListName());
                        }
                    }
                }

                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(sb.toString());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE58": {

                bmodel.getAttributeListForRetailer();
                updateRetailerAttribute(retailerObj.getAttributeBOArrayList());
                for (NewOutletAttributeBO attributeBO : attributeList) {
                    outletBO = new NewOutletBO();
                    outletBO.setmName(attributeBO.getAttrParent());
                    outletBO.setValueText(attributeBO.getAttrName());
                    finalProfileList.add(outletBO);
                }
                break;
            }
            case "PROFILE59": {

                int typeId = retailerObj.getSalesTypeId();
                String SalesTypName = bmodel.getStandardListName(typeId);
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(SalesTypName);
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE62": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                if (retailerObj.getIsSEZzone() == 1)
                    outletBO.setValueText(getResources().getString(R.string.yes));
                else
                    outletBO.setValueText(getResources().getString(R.string.no));

                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE61": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getGSTNumber());

                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE08": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getLatitude() + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE31": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getLongitude() + "");
                finalProfileList.add(outletBO);
                break;
            }
            //Setting Loyalty Points
            case "PROFILE64": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(loyaltyPoints + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE65": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(loyaltyBalancePoints + "");
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE81": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getPanNumber());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE82": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getFoodLicenceNo());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE83": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getFoodLicenceExpDate());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE84": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getDLNo());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE85": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getDLNoExpDate());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE78": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getEmail());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE79": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getMobile());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE86": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getFax());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE87": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRegion());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE88": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getCountry());
                finalProfileList.add(outletBO);
                break;
            }
            case "PROFILE89": {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getDistrict());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD10: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField10());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD11: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField11());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD12: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField12());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD13: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField13());
                finalProfileList.add(outletBO);
                break;
            }case RFIELD14: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField14());
                finalProfileList.add(outletBO);
                break;
            }case RFIELD15: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField15());
                finalProfileList.add(outletBO);
                break;
            }case RFIELD16: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField16());
                finalProfileList.add(outletBO);
                break;
            }case RFIELD17: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField17());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD18: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField18());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD19: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField19());
                finalProfileList.add(outletBO);
                break;
            }
            case RFIELD20: {
                outletBO = new NewOutletBO();
                outletBO.setmName(mName);
                outletBO.setValueText(retailerObj.getRField20());
                finalProfileList.add(outletBO);
                break;
            }

            default:
                break;
        }

    }

    private class LoadDataAsync extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            loadProfileDatas();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (finalProfileList.size() > 0)
                updateProfileListView(finalProfileList);
        }
    }

    private static class DownloadAsync extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            //updateLocationMasterList();
            bmodel.newOutletHelper.downloadLinkRetailer();
            bmodel.newOutletHelper.loadContactTitle();
            bmodel.newOutletHelper.loadContactStatus();
            bmodel.mRetailerHelper.loadContractData();

            return true;
        }
    }

    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        mSelectedIds = list;
    }


    private void updateProfileListView(ArrayList<NewOutletBO> finalProfileList) {
        RecyclerViewAdapter profileSchedule = new RecyclerViewAdapter(finalProfileList);
        recyclerView.setAdapter(profileSchedule);

    }

    private void updateRetailerAttribute(ArrayList<NewOutletAttributeBO> list) {
        attributeList = new ArrayList<>();
        bmodel.newOutletAttributeHelper.downloadAttributeParentList();
        bmodel.newOutletAttributeHelper.downloadRetailerAttributeChildList();
        ArrayList<NewOutletAttributeBO> childList = bmodel.newOutletAttributeHelper.getAttributeList();
        ArrayList<NewOutletAttributeBO> parentList = bmodel.newOutletAttributeHelper.getmAttributeParentList();
        int attribID;
        int tempAttribID;
        int parentID;
        int tempParentID = 0;
        String attribName = "";
        String attribHeader = "";
        NewOutletAttributeBO tempBO;
        for (NewOutletAttributeBO attributeBO : list) {
            tempBO = new NewOutletAttributeBO();
            attribID = attributeBO.getAttrId();
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
            attributeList.add(tempBO);
        }
    }

    private void clearAmazonDownload() {
        if (transferUtility != null) {
            transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
        }
    }

    /**
     * This is the Handler for this activity. It will receive messages from the
     * ApkDownloaderThread and make the necessary updates to the UI.
     */
    @SuppressLint("HandlerLeak")
    private final Handler activityHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    Commons.print("Retailer Image Downloaded");
                    break;
                case DataMembers.MESSAGE_DOWNLOAD_CANCELED:
                    clearAmazonDownload();
                    Commons.print("Retailer Image Downloading Cancelled");
                    break;
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    Commons.print("Retailer Image Downloading Error");
                    break;
                default:
                    // nothing to do here
                    break;
            }
        }
    };

}

