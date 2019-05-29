package com.ivy.cpg.view.asset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class PosmTrackingFragment extends IvyBaseFragment implements
        OnEditorActionListener, BrandDialogInterface, DataPickerDialogFragment.UpdateDateInterface, FiveLevelFilterCallBack {


    private BusinessModel mBModel;
    private StandardListBO mSelectedStandardListBO;

    private DrawerLayout mDrawerLayout;
    private AlertDialog alertDialog;
    private ListView mListView;
    private Button dateBtn;
    private EditText qtyEditText;
    Button btnSave;

    private static final String TAG = "POSM Screen";
    private static final String TAG_DATE_PICKER_INSTALLED = "date_picker_installed";
    private static final String TAG_DATE_PICKER_SERVICED = "date_picker_serviced";
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private static final String ALL = "ALL";
    private final String moduleName = "AT_";
    private String strBarCodeSearch = "ALL";
    private static final String MENU_POSM = "MENU_POSM";
    private static final String MENU_POSM_CS = "MENU_POSM_CS";
    private String screenCode = "MENU_POSM";
    private String append = "";
    private static String outPutDateFormat;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int POSM_GALLERY = 2;
    private String photoPath = "";
    private int mSelectedLocationIndex;
    private int mSelectedLastFilterSelection = -1;
    private String imageName;
    private boolean isShowed = false;
    private String mCapturedNFCTag = "";
    private String mBrandButton;
    private boolean isFromChild;
    private String mFilterText;


    private ArrayList<AssetTrackingBO> myList;
    private ArrayList<AssetTrackingBO> mAssetTrackingList;
    private ArrayList<ReasonMaster> mPOSMReasonList;
    private ArrayList<ReasonMaster> mPOSMConditionList;
    private ArrayAdapter<ReasonMaster> mPOSMReasonSpinAdapter;
    private ArrayAdapter<ReasonMaster> mPOSMConditionAdapter;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private ArrayAdapter<StandardListBO> mInStoreLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private int mFilteredPid;
    private ArrayList<Integer> mAttributeProducts;

    AssetTrackingHelper assetTrackingHelper;
    private ActionBar actionBar;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());

        this.context = context;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(assetTrackingHelper.mSelectedActivityName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posm_tracking, container,
                false);

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        Bundle extras = getArguments();
        if (extras == null) {
            extras = ((Activity)context).getIntent().getExtras();
        }

        if (extras != null) {
            screenCode = extras.getString("CurrentActivityCode");
            screenCode = screenCode != null ? screenCode : MENU_POSM;
        }
        isFromChild = ((Activity)context).getIntent().getBooleanExtra("isFromChild", false);

        if (!assetTrackingHelper.SHOW_LOCATION_POSM)
            view.findViewById(R.id.tv_store_loc).setVisibility(View.GONE);

        btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                nextButtonClick();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = view.findViewById(R.id.list);
        mListView.setCacheColorHint(0);


        FrameLayout drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                if (actionBar != null) {
                    setScreenTitle(assetTrackingHelper.mSelectedActivityName);
                }
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;

        if (mBModel.configurationMasterHelper.isAuditEnabled()) {
            TextView tvAudit = view.findViewById(R.id.audit);
            tvAudit.setVisibility(View.VISIBLE);
        }

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mBModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);

        if (!mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
        }

        if (mLocationAdapter.getCount() > 0) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }

        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        if (!isShowed) {
            if (!mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION
                    && mBModel.productHelper.getInStoreLocation().size() > 1 && !assetTrackingHelper.SHOW_LOCATION_POSM)
                showLocation();
            loadedItem();
            isShowed = true;
        }

        hideAndSeeK(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFilteredPid != 0 || mSelectedIdByLevelId != null || mAttributeProducts != null) {
            updateFromFiveLevelFilter(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
        } else {
            updateBrandText(BRAND, mSelectedLastFilterSelection);
        }

        mSelectedFilterMap.put("General", GENERAL);
        updateGeneralText(GENERAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(
                R.menu.menu_asset, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean drawerOpen = false;

        if (mDrawerLayout != null)
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        if (assetTrackingHelper.SHOW_REMARKS_POSM)
            menu.findItem(R.id.menu_remarks).setVisible(true);
        else
            menu.findItem(R.id.menu_remarks).setVisible(false);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (mBModel.productHelper.isFilterAvaiable(MENU_POSM)) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        }

        menu.findItem(R.id.menu_add).setVisible(false);
        menu.findItem(R.id.menu_remove).setVisible(false);

        if (assetTrackingHelper.SHOW_POSM_ALL) {
            menu.findItem(R.id.menu_all).setVisible(true);
        }
        menu.findItem(R.id.menu_survey).setVisible(false);

        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION || screenCode.equals(MENU_POSM_CS) || assetTrackingHelper.SHOW_LOCATION_POSM)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (mBModel.productHelper.getInStoreLocation().size() <= 1)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }
        menu.findItem(R.id.menu_assetservice).setVisible(false);
        menu.findItem(R.id.menu_assetScan).setVisible(false);
        //Move Asset is removed in Posm
        menu.removeItem(R.id.menu_move);

        if (drawerOpen)
            menu.clear();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {

                if (!isPreVisit)
                    mBModel.outletTimeStampHelper
                        .updateTimeStampModuleWise(DateTimeUtils
                                .now(DateTimeUtils.TIME));

                if (screenCode.equalsIgnoreCase(MENU_POSM_CS)) {
                    Intent intent = new Intent(context, HomeScreenTwo.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    startActivity(intent.putExtra("menuCode", "MENU_COUNTER"));
                } else if (screenCode.equalsIgnoreCase(MENU_POSM)) {

                    Intent intent = new Intent(context, HomeScreenTwo.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    if (isFromChild)
                        intent.putExtra("isStoreMenu", true);


                    startActivity(intent);
                }
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                getActivity().finish();
            }
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_all) {
            updateList(-1, mSelectedStandardListBO);
            if (mCapturedNFCTag != null) {
                mCapturedNFCTag = "";
            }
            return true;
        } else if (i == R.id.menu_remarks) {
            FragmentTransaction ft = getActivity()
                    .getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog1 = new RemarksDialog("MENU_ASSET");
            dialog1.setCancelable(false);
            dialog1.show(ft, "menu_asset");
            return true;
        } else if (i == R.id.menu_barcode) {

            return true;

        } else if (i == R.id.menu_survey) {
            Intent intent = new Intent(context, SurveyActivityNew.class);
            if (isPreVisit)
                intent.putExtra("PreVisit",true);

            startActivity(intent);
            return true;
        } else if (i == R.id.menu_add) {

            AddAssetDialogFragment dialog = new AddAssetDialogFragment();
            dialog.show(getFragmentManager(), "Asset");

            return true;
        } else if (i == R.id.menu_remove) {
            Intent intent = new Intent(getActivity(), AssetPosmRemoveActivity.class);
            intent.putExtra("module", screenCode);
            if (isPreVisit)
                intent.putExtra("PreVisit",true);
            startActivity(intent);
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (mBModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_assetScan) {

            scanBarCode();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * update list by NFC selection
     *
     * @param mNFCTag NFC tag
     */
    public void updateListByNFCTag(String mNFCTag) {
        mCapturedNFCTag = mNFCTag;
        strBarCodeSearch = "ALL";
        updateList(-1, mSelectedStandardListBO);
    }

    /**
     * Method that to loaded values and set into arrayList and adapter
     */
    private void loadedItem() {
        ReasonMaster reason = new ReasonMaster();
        ReasonMaster reason1 = new ReasonMaster();
        reason.setReasonID(Integer.toString(0));
        reason.setReasonDesc(getResources().getString(R.string.plain_select));
        reason1.setReasonID(Integer.toString(0));
        reason1.setReasonDesc(getResources().getString(R.string.plain_select));

        mPOSMReasonList = assetTrackingHelper.getPOSMReasonList();
        mPOSMReasonList.add(0, reason);

        mPOSMConditionList = assetTrackingHelper.getPOSMConditionList();
        mPOSMConditionList.add(0, reason1);

        mPOSMReasonSpinAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mPOSMReasonList);
        mPOSMReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        mPOSMConditionAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mPOSMConditionList);
        mPOSMConditionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        mInStoreLocationAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mBModel.productHelper.getInStoreLocation());
        mInStoreLocationAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


    }


    /**
     * update POSM list
     *
     * @param bid            Brand Id
     * @param standardListBO Selected Location Object
     */
    private void updateList(int bid, StandardListBO standardListBO) {
        myList = new ArrayList<>();
        mAssetTrackingList = standardListBO.getAssetTrackingList();
        if (mAssetTrackingList != null) {
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                assetBO.setLocationID(SDUtil.convertToInt(standardListBO.getListID()));
                assetTrackingHelper.mSelectedLocationID = assetBO.getLocationID();
                if ("ALL".equals(strBarCodeSearch)) {
                    if ("".equals(mCapturedNFCTag)) {
                        if ((bid == -1 && "Brand".equals(mBrandButton)) || bid == assetBO.getProductId()) {
                            myList.add(assetBO);
                        }
                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                        assetBO.setAvailQty(1);
                        myList.add(assetBO);
                    }
                } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                    myList.add(assetBO);
                }

            }

            int size = myList.size();
            refreshList();
            if (size == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_posm_exists),
                        Toast.LENGTH_SHORT).show();

            }
        }

        strBarCodeSearch = "ALL";
    }

    /**
     * Method that to refresh item in listView
     */
    private void refreshList() {

        MyAdapter adapter = new MyAdapter(myList);
        mListView.setAdapter(adapter);

    }

    /**
     * This is ListView(mListView) adapter class
     */
    private class MyAdapter extends BaseAdapter {
        private final ArrayList<AssetTrackingBO> items;

        public MyAdapter(ArrayList<AssetTrackingBO> items) {
            super();
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_posm_tracking, parent, false);

                row.setTag(holder);

                holder.audit = row
                        .findViewById(R.id.btn_audit);
                holder.assetNameTV = row
                        .findViewById(R.id.tv_asset_name);
                holder.targetTV = row
                        .findViewById(R.id.tv_target);
                holder.availQtyET = row
                        .findViewById(R.id.edit_availability_qty);
                holder.reasonLL = (LinearLayout) row.findViewById(R.id.llReason);
                holder.reason1Spin = row
                        .findViewById(R.id.spin_reason1);

                holder.reason1Spin.setAdapter(mPOSMReasonSpinAdapter);
                holder.conditionLL = (LinearLayout) row.findViewById(R.id.llCondition);
                holder.mConditionSpin = row
                        .findViewById(R.id.spin_condition);
                holder.mConditionSpin.setAdapter(mPOSMConditionAdapter);

                holder.mLocationSpin = row
                        .findViewById(R.id.spin_location);
                holder.mLocationSpin.setAdapter(mInStoreLocationAdapter);

                holder.mInstallDate = row
                        .findViewById(R.id.Btn_install_Date);
                holder.mServiceDate = row
                        .findViewById(R.id.Btn_service_Date);
                holder.photoBTN = row
                        .findViewById(R.id.btn_photo);
                holder.photoCount = row
                        .findViewById(R.id.txt_count);

                holder.compQtyET = row.findViewById(R.id.edit_competitor_qty);
                holder.execQtyET = row.findViewById(R.id.edit_exe_qty);
                holder.grpTV = row.findViewById(R.id.tv_grp);
                holder.locationNameTv = row.findViewById(R.id.tv_location_name);
                holder.executeLL = row.findViewById(R.id.ll_exec_qty);
                holder.execQtyCheckBox = row.findViewById(R.id.check_exec_qty);

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.assetBO.getAudit() == IvyConstants.AUDIT_DEFAULT) {

                            holder.assetBO
                                    .setAudit(IvyConstants.AUDIT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_OK) {

                            holder.assetBO
                                    .setAudit(IvyConstants.AUDIT_NOT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_NOT_OK) {

                            holder.assetBO
                                    .setAudit(IvyConstants.AUDIT_DEFAULT);
                            holder.audit.setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                holder.availQtyET.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.availQtyET.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            int mAvailQty = SDUtil.convertToInt(holder.availQtyET
                                    .getText().toString());
                            holder.assetBO.setAvailQty(mAvailQty);

                            if (holder.assetBO.getAvailQty() > 0) {

                                if (!assetTrackingHelper.SHOW_POSM_TARGET || holder.assetBO.getAvailQty() >= holder.assetBO.getTarget()) {

                                    if (assetTrackingHelper.SHOW_LOCATION_POSM) {
                                        if (holder.assetBO.getTargetLocId() == holder.assetBO.getLocationID()) {
                                            holder.reason1Spin.setEnabled(false);
                                            holder.reason1Spin.setSelection(0);
                                        } else
                                            holder.reason1Spin.setEnabled(true);
                                    } else {
                                        holder.reason1Spin.setEnabled(false);
                                        holder.reason1Spin.setSelection(0);
                                    }
                                }

                                holder.photoBTN.setEnabled(true);
                                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                                holder.photoCount.setText("" + holder.assetBO.getImageList().size());
                                holder.mConditionSpin.setEnabled(true);
                                holder.mConditionSpin.setSelection(0);
                                holder.mInstallDate.setEnabled(true);
                                holder.mServiceDate.setEnabled(true);

                            } else {

                                holder.reason1Spin.setEnabled(true);
                                holder.photoBTN.setEnabled(false);
                                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                                holder.photoCount.setText("0");
                                holder.mConditionSpin.setEnabled(false);
                                holder.mConditionSpin.setSelection(0);
                                holder.mInstallDate.setEnabled(false);
                                holder.mServiceDate.setEnabled(false);
                                holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                                holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                                holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                                holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));

                            }
                        }

                    }
                });
                holder.availQtyET.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        qtyEditText = holder.availQtyET;
                        qtyEditText.setTag(holder.availQtyET);
                        int inType = holder.availQtyET.getInputType();
                        holder.availQtyET.setInputType(InputType.TYPE_NULL);
                        holder.availQtyET.onTouchEvent(arg1);
                        holder.availQtyET.setInputType(inType);
                        holder.availQtyET.requestFocus();
                        if (holder.availQtyET.getText().length() > 0)
                            holder.availQtyET.setSelection(holder.availQtyET.getText().length());
                        return true;
                    }
                });
                holder.execQtyET.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                break;
                            case MotionEvent.ACTION_UP:
                                qtyEditText = holder.execQtyET;
                                qtyEditText.setTag(holder.execQtyET);
                                int inType = holder.execQtyET.getInputType();
                                holder.execQtyET.setInputType(InputType.TYPE_NULL);
                                holder.execQtyET.onTouchEvent(motionEvent);
                                holder.execQtyET.setInputType(inType);
                                holder.execQtyET.requestFocus();
                                if (holder.execQtyET.getText().length() > 0)
                                    holder.execQtyET.setSelection(holder.execQtyET.getText().length());
                                view.performClick();
                                break;
                            default:
                                break;
                        }

                        return true;
                    }

                });
                holder.execQtyET.performClick();


                holder.execQtyET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.execQtyET.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            int mExceqQty = SDUtil.convertToInt(holder.execQtyET
                                    .getText().toString());
                            holder.assetBO.setExecutorQty(mExceqQty);
                        }
                    }
                });
                holder.compQtyET.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        qtyEditText = holder.compQtyET;
                        qtyEditText.setTag(holder.compQtyET);
                        int inType = holder.compQtyET.getInputType();
                        holder.compQtyET.setInputType(InputType.TYPE_NULL);
                        holder.compQtyET.onTouchEvent(arg1);
                        holder.compQtyET.setInputType(inType);
                        holder.compQtyET.requestFocus();
                        if (holder.compQtyET.getText().length() > 0)
                            holder.compQtyET.setSelection(holder.compQtyET.getText().length());
                        return true;
                    }
                });
                holder.reason1Spin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {
                                ReasonMaster reasonBO = (ReasonMaster) holder.reason1Spin
                                        .getSelectedItem();

                                holder.assetBO.setReason1ID(reasonBO
                                        .getReasonID());
                                holder.assetBO.setReasonDesc(reasonBO
                                        .getReasonDesc());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });
                holder.mConditionSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {
                                ReasonMaster reasonBO = (ReasonMaster) holder.mConditionSpin
                                        .getSelectedItem();

                                holder.assetBO.setConditionID(reasonBO
                                        .getConditionID());
                                holder.assetBO.setReasonDesc(reasonBO
                                        .getReasonDesc());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });


                holder.mLocationSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {
                                StandardListBO reasonBO = (StandardListBO) holder.mLocationSpin
                                        .getSelectedItem();

                                holder.assetBO.setLocationID(SDUtil.convertToInt(reasonBO
                                        .getListID()));
                                assetTrackingHelper.mSelectedLocationID = holder.assetBO.getLocationID();

                                if (holder.assetBO.getTargetLocId() == holder.assetBO.getLocationID() && holder.assetBO.getAvailQty() > 0) {
                                    if (!assetTrackingHelper.SHOW_POSM_TARGET || holder.assetBO.getAvailQty() >= holder.assetBO.getTarget()) {
                                        holder.reason1Spin.setEnabled(false);
                                        holder.reason1Spin.setSelection(0);
                                    } else
                                        holder.reason1Spin.setEnabled(true);
                                } else
                                    holder.reason1Spin.setEnabled(true);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });

                holder.mInstallDate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mInstallDate;
                        dateBtn.setTag(holder.assetBO);
                        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                        newFragment.show(getChildFragmentManager(), TAG_DATE_PICKER_INSTALLED);
                    }
                });
                holder.mServiceDate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mServiceDate;
                        dateBtn.setTag(holder.assetBO);
                        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                        newFragment.show(getChildFragmentManager(), TAG_DATE_PICKER_SERVICED);
                    }
                });

                holder.photoBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (mBModel.synchronizationHelper
                                .isExternalStorageAvailable()) {

                            photoPath = getActivity().getExternalFilesDir(
                                    Environment.DIRECTORY_PICTURES)
                                    + "/" + DataMembers.photoFolderName + "/";

                            imageName = moduleName
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.assetBO.getAssetID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            assetTrackingHelper.mSelectedAssetID = holder.assetBO
                                    .getAssetID();
                            assetTrackingHelper.mSelectedSerialNumber = holder.assetBO.getSerialNo();
                            assetTrackingHelper.mSelectedProductID = holder.assetBO.getProductId();

                            assetTrackingHelper.mSelectedImageName = imageName;

                            if (holder.assetBO.getImageList().size() != 0) {
                                Intent intent = new Intent(getActivity(), PosmGallery.class);
                                intent.putExtra("listId", mSelectedStandardListBO.getListID());
                                intent.putExtra("assetId", holder.assetBO.getAssetID());
                                intent.putExtra("serialNo", holder.assetBO.getSerialNo());
                                intent.putExtra("productID", holder.assetBO.getProductId());
                                startActivityForResult(intent, POSM_GALLERY);
                            } else
                                captureCustom();

                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.sdcard_is_not_ready_to_capture_img), Toast.LENGTH_SHORT)
                                    .show();
                            getActivity().finish();
                        }

                    }
                });

                holder.execQtyCheckBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.assetBO.getExecutorQty() == 0) {
                            holder.execQtyCheckBox.setChecked(true);
                            holder.assetBO.setExecutorQty(1);
                        } else {
                            holder.execQtyCheckBox.setChecked(false);
                            holder.assetBO.setExecutorQty(0);
                        }


                        if (holder.assetBO.getExecutorQty() > 0) {

                            if (assetTrackingHelper.SHOW_LOCATION_POSM) {
                                if (holder.assetBO.getTargetLocId() == holder.assetBO.getLocationID()) {
                                    holder.reason1Spin.setEnabled(false);
                                    holder.reason1Spin.setSelection(0);
                                } else
                                    holder.reason1Spin.setEnabled(true);
                            } else {
                                holder.reason1Spin.setEnabled(false);
                                holder.reason1Spin.setSelection(0);
                            }

                            holder.photoBTN.setEnabled(true);
                            holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                            holder.photoCount.setText("" + holder.assetBO.getImageList().size());
                            holder.mConditionSpin.setEnabled(true);
                            holder.mConditionSpin.setSelection(0);
                            holder.mInstallDate.setEnabled(true);
                            holder.mServiceDate.setEnabled(true);

                        } else {

                            holder.reason1Spin.setEnabled(true);
                            holder.photoBTN.setEnabled(false);
                            holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                            holder.photoCount.setText("0");
                            holder.mConditionSpin.setEnabled(false);
                            holder.mConditionSpin.setSelection(0);
                            holder.mInstallDate.setEnabled(false);
                            holder.mServiceDate.setEnabled(false);
                            holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));

                        }
                    }
                });


            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.assetBO = items.get(position);

            if (holder.assetBO.getAudit() == IvyConstants.AUDIT_DEFAULT)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_NOT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.assetNameTV.setText(holder.assetBO.getAssetName());

            String availQty = holder.assetBO.getAvailQty() + "";
            holder.availQtyET.setText(availQty);

            String excutedQty = holder.assetBO.getExecutorQty() + "";
            holder.execQtyET.setText(excutedQty);

            String competitorQty = holder.assetBO.getCompetitorQty() + "";
            holder.compQtyET.setText(competitorQty);

            String strTarget = holder.assetBO.getTarget() + "";
            holder.targetTV.setText(strTarget);

            holder.mInstallDate
                    .setText((holder.assetBO.getInstallDate() == null) ? DateTimeUtils
                            .convertFromServerDateToRequestedFormat(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getInstallDate());
            holder.mServiceDate
                    .setText((holder.assetBO.getServiceDate() == null) ? DateTimeUtils
                            .convertFromServerDateToRequestedFormat(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getServiceDate());
            holder.grpTV.setText(holder.assetBO.getGroupLevelName());
            holder.locationNameTv.setText(holder.assetBO.getLocationName());

            //First time when screen appears
            if (holder.assetBO.getAvailQty() > 0
                    || holder.assetBO.getExecutorQty() > 0) {

                holder.reason1Spin.setEnabled(false);
                holder.photoBTN.setEnabled(true);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                holder.photoCount.setText(String.valueOf(holder.assetBO.getImageList().size()));
                holder.reason1Spin.setSelection(0);
                holder.mConditionSpin.setEnabled(true);
                holder.mConditionSpin.setSelection(assetTrackingHelper
                        .getItemIndex(holder.assetBO.getConditionID(),
                                mPOSMConditionList));
                holder.mInstallDate.setEnabled(true);
                holder.mServiceDate.setEnabled(true);

            } else {

                holder.reason1Spin.setEnabled(true);
                holder.photoBTN.setEnabled(false);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.photoCount.setText("0");
                holder.mConditionSpin.setEnabled(false);
                holder.mConditionSpin.setSelection(0);
                holder.mInstallDate.setEnabled(false);
                holder.mServiceDate.setEnabled(false);
                holder.assetBO.setImageName("");
                holder.assetBO.setImgName("");
                holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
            }

            holder.reason1Spin.setSelection(assetTrackingHelper
                    .getItemIndex(holder.assetBO.getReason1ID(), mPOSMReasonList));

            if (!assetTrackingHelper.SHOW_POSM_TARGET) {
                holder.targetTV.setVisibility(View.GONE);
            }

            if (!assetTrackingHelper.SHOW_POSM_QTY) {
                holder.availQtyET.setVisibility(View.GONE);
            }

            if (!assetTrackingHelper.SHOW_POSM_REASON) {
                holder.reasonLL.setVisibility(View.GONE);
                //holder.reason1Spin.setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_POSM_CONDITION) {
                holder.conditionLL.setVisibility(View.GONE);
                //holder.mConditionSpin.setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_POSM_INSTALL_DATE) {
                row.findViewById(R.id.ll_install_date).setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_POSM_SERVICE_DATE) {
                row.findViewById(R.id.ll_service_date).setVisibility(View.GONE);
            }

            if (!assetTrackingHelper.SHOW_POSM_PHOTO) {
                holder.photoBTN.setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY) {
                holder.compQtyET.setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_POSM_GRP) {
                holder.grpTV.setVisibility(View.GONE);
            }
            if (!assetTrackingHelper.SHOW_LOCATION_POSM) {
                holder.locationNameTv.setVisibility(View.GONE);
                holder.mLocationSpin.setVisibility(View.GONE);
            } else {
                if (holder.assetBO.getLocationID() > 0)
                    holder.mLocationSpin.setSelection(assetTrackingHelper
                            .getItemIndex(holder.assetBO.getLocationID(),
                                    mBModel.productHelper.getInStoreLocation()));
                else
                    holder.mLocationSpin.setSelection(assetTrackingHelper
                            .getItemIndex(holder.assetBO.getTargetLocId(),
                                    mBModel.productHelper.getInStoreLocation()));
            }

            if (assetTrackingHelper.SHOW_POSM_EXECUTED
                    && assetTrackingHelper.SHOW_POSM_QTY) {
                holder.executeLL.setVisibility(View.GONE);
            } else if (assetTrackingHelper.SHOW_POSM_EXECUTED) {
                holder.execQtyET.setVisibility(View.GONE);
            } else {
                holder.executeLL.setVisibility(View.GONE);
                holder.execQtyET.setVisibility(View.GONE);
            }

            if (mBModel.configurationMasterHelper.isAuditEnabled()){

                holder.audit.setVisibility(View.VISIBLE);
                holder.availQtyET.setEnabled(false);
                holder.reason1Spin.setEnabled(false);
                holder.reason1Spin.setClickable(false);
                holder.photoBTN.setEnabled(false);
                holder.photoBTN.setClickable(false);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.mConditionSpin.setEnabled(false);
                holder.mInstallDate.setEnabled(false);
                holder.mInstallDate.setClickable(false);
                holder.mServiceDate.setEnabled(false);
                holder.mServiceDate.setClickable(false);
                holder.compQtyET.setEnabled(false);
            }

            if (!holder.photoBTN.isEnabled()) {
                holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_grey_24dp));
                holder.photoCount.setText("0");
            } else {
                holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_blue_24dp));
                holder.photoCount.setText("" + holder.assetBO.getImageList().size());
            }

            if (holder.assetBO.getExecutorQty() == 1) {
                holder.execQtyCheckBox.setChecked(true);
            } else {
                holder.execQtyCheckBox.setChecked(false);
            }

            return row;
        }
    }

    class ViewHolder {
        AssetTrackingBO assetBO;
        TextView assetNameTV;
        TextView targetTV;
        EditText availQtyET;
        EditText compQtyET;
        EditText execQtyET;
        Spinner reason1Spin;
        Spinner mConditionSpin;
        Spinner mLocationSpin;
        ImageView photoBTN;
        Button mInstallDate;
        Button mServiceDate;
        ImageButton audit;
        TextView grpTV;
        TextView locationNameTv;
        TextView photoCount;
        CheckBox execQtyCheckBox;
        LinearLayout executeLL, reasonLL, conditionLL;
    }


    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }


    public void numberPressed(View vw) {
        if (qtyEditText == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(qtyEditText.getText()
                        .toString());
                s = s / 10;
                String strQty = s + "";
                qtyEditText.setText(strQty);
            } else {
                if (getView() != null) {
                    Button ed = getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    private void eff() {
        String s = qtyEditText.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQty = qtyEditText.getText() + append;
            qtyEditText.setText(strQty);
        } else
            qtyEditText.setText(append);
    }

    /**
     * Method to check already image captured or not if Already captured, it
     * will show Alert Dialog In Alert Dialog, if click yes,remove image in
     * sdcard and retake photo. If click No, Alert Dialog dismiss
     *
     * @param mAssetId        Asset Id
     * @param imageNameStarts Image Name
     */
    private void showFileDeleteAlertWithImage(final String mAssetId,
                                              final String imageNameStarts,
                                              final String imageSrc) {
        final CommonDialog commonDialog = new CommonDialog(getActivity().getApplicationContext(),
                getActivity(),
                "",
                getResources().getString(R.string.word_already) + " " + 1 + " " + getResources().getString(R.string.word_photocaptured_delete_retake),
                true,
                getResources().getString(R.string.yes),
                getResources().getString(R.string.no),
                false,
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        ArrayList<AssetTrackingBO> items = assetTrackingHelper
                                .getAssetTrackingList();

                        for (AssetTrackingBO assetBO : items) {
                            if (mAssetId.equals(Integer.toString(assetBO.getAssetID()))) {
                                assetBO.setImageName("");
                                assetBO.setImgName("");
                            }
                        }
                        assetTrackingHelper
                                .deleteImageName(getContext().getApplicationContext(), imageNameStarts);
                        mBModel.synchronizationHelper.deleteFiles(photoPath,
                                imageNameStarts);

                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String _path = photoPath + "/" + imageName;
                        intent.putExtra(CameraActivity.PATH, _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }


    /**
     * Method to set captured image name in AssetTrackingBO
     *
     * @param assetID Asset Id
     * @param imgName Image Name
     */
    private void onSaveImageName(int assetID, int locationID, String serialNo, int productID, String imgName) {

        String imagePath = "Asset/"
                + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imgName;

        for (AssetTrackingBO assetBO : mAssetTrackingList) {
            if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            if (assetID == assetBO.getAssetID() && serialNo.equals(assetBO.getSerialNo()) && productID == assetBO.getProductId() && locationID == assetBO.getLocationID()) {
                ArrayList<String> imageList = assetBO.getImageList();
                imageList.add(imagePath);
                assetBO.setImageList(imageList);
                break;
            }
        }
        refreshList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(TAG + "," +
                        "Camera Activity : Successfully Captured.");
                if (assetTrackingHelper.mSelectedAssetID != 0) {
                    onSaveImageName(
                            assetTrackingHelper.mSelectedAssetID,  assetTrackingHelper.mSelectedLocationID, assetTrackingHelper.mSelectedSerialNumber, assetTrackingHelper.mSelectedProductID,
                            assetTrackingHelper.mSelectedImageName);
                }
            } else {
                Commons.print(TAG + "," + "Camera Activity : Canceled");
            }
        } else if (requestCode == POSM_GALLERY) {
            updateList(-1, mSelectedStandardListBO);
        } else {

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                } else {
                    strBarCodeSearch = result.getContents();
                }
            }
        }
    }

    /**
     * Delete un used images
     */
    public void deleteUnusedImages() {

        for (AssetTrackingBO temp : myList) {
            if (temp.getAvailQty() == 0 && !"".equals(temp.getImageName())) {
                String fileName = temp.getImageName();
                deleteFiles(fileName);
            }
        }
    }

    /**
     * Delete file
     *
     * @param filename File Name
     */
    private void deleteFiles(String filename) {
        File folder = new File(FileUtils.photoFolderPath + "/");

        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print(TAG + " Image Delete," + "Success");
            }
        }
    }

    /**
     * This AsyncTask class is used to save Asset Details in table
     */
    private class SaveAsset extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            deleteUnusedImages();
            assetTrackingHelper.saveAsset(getContext().getApplicationContext(), screenCode);
            mBModel.saveModuleCompletion(screenCode, true);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            alertDialog.dismiss();
            mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));

            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    null, new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    Intent intent;
                    if (screenCode.equals(MENU_POSM)) {
                        intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                        }
                    } else {
                        intent = new Intent(getActivity(),
                                HomeScreenActivity.class);
                        intent.putExtra("menuCode", "MENU_COUNTER");
                    }

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    startActivity(intent);
                    getActivity().finish();

                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                }
            }).show();
        }
    }

    /**
     * Method to call saveAsset function while click nextButton in action bar
     */
    private void nextButtonClick() {
        if (assetTrackingHelper.hasAssetTaken()) {
            new SaveAsset().execute("");
        } else {
            AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                    getActivity());
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)
                    .setTitle(getResources().getString(R.string.no_posm_exists))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                }
                            });

            mBModel.applyAlertDialogTheme(alertDialogBuilder1);
        }
    }

    /**
     * Method that to show visibility and hided column
     */
    private void hideAndSeeK(@NonNull View view) {
        if (assetTrackingHelper.SHOW_POSM_QTY
                || assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY
                || assetTrackingHelper.SHOW_POSM_EXECUTED) {
            view.findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.keypad).setVisibility(View.GONE);
        }

        if (assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY)
            view.findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        if (mBModel.configurationMasterHelper.IS_TEAMLEAD)
            view.findViewById(R.id.keypad).setVisibility(View.GONE);
        if (!assetTrackingHelper.SHOW_POSM_TARGET) {
            view.findViewById(R.id.tv_header_target).setVisibility(View.GONE);
        } else {
            try {
                if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_target).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_target))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_target).getTag()));

                }
            } catch (Exception e) {
                Commons.printException(e.toString());
            }
        }

        if (!assetTrackingHelper.SHOW_LOCATION_POSM)
            view.findViewById(R.id.tv_store_loc).setVisibility(View.GONE);

        if (!assetTrackingHelper.SHOW_POSM_QTY) {
            view.findViewById(R.id.tv_header_qty).setVisibility(View.GONE);
        } else {
            try {
                if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_qty))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_qty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException(e.toString());
            }
        }


        try {
            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.tv_header_asset_name).getTag()) != null) {
                ((TextView) view.findViewById(R.id.tv_header_asset_name))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.tv_header_asset_name).getTag()));

            }
        } catch (Exception e) {
            Commons.printException(e.toString());
        }
        if (!assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY)
            view.findViewById(R.id.tv_competitor_qty).setVisibility(View.GONE);
        else {
            try {
                if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_competitor_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_competitor_qty))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_competitor_qty).getTag()));
                }
            } catch (Exception e) {
                Commons.printException(e.toString());
            }
        }

        if (!assetTrackingHelper.SHOW_POSM_EXECUTED)
            view.findViewById(R.id.tv_executed_qty).setVisibility(View.GONE);
        else {
            try {
                if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_executed_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_executed_qty))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_executed_qty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException(e.toString());
            }
        }


    }

    /**
     * Custom camera call
     */
    private void captureCustom() {
        try {
            Intent intent = new Intent(getActivity(),
                    CameraActivity.class);
            intent.putExtra(CameraActivity.QUALITY, 40);
            String path = photoPath + "/" + imageName;
            intent.putExtra(CameraActivity.PATH, path);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } catch (Exception e) {
            Commons.printException(e.toString());
        }
    }


    @Override
    public void updateDate(Date date, String tag) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar selectedDate = new GregorianCalendar(year, month, day);
        AssetTrackingBO bo = (AssetTrackingBO) dateBtn.getTag();
        if (TAG_DATE_PICKER_INSTALLED.equals(tag)) {

            if (selectedDate.after(Calendar.getInstance())) {
                Toast.makeText(getActivity(),
                        R.string.future_date_not_allowed,
                        Toast.LENGTH_SHORT).show();
                bo.setInstallDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        Calendar.getInstance().getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                        .getInstance().getTime(), outPutDateFormat));
            } else {

                bo.setInstallDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }
        } else if (TAG_DATE_PICKER_SERVICED.equals(tag)) {

            if (bo.getInstallDate() != null
                    && bo.getInstallDate().length() > 0) {
                Date mInstallDate = DateTimeUtils.convertStringToDateObject(
                        bo.getInstallDate(), outPutDateFormat);
                if (mInstallDate != null && selectedDate.getTime() != null
                        && mInstallDate.after(selectedDate.getTime())) {
                    Toast.makeText(getActivity(),
                            R.string.servicedate_set_after_installdate,
                            Toast.LENGTH_SHORT).show();
                } else {
                    bo.setServiceDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else {

                bo.setServiceDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        mBrandButton = mFilterText;
        mDrawerLayout.closeDrawers();
        mSelectedLastFilterSelection = id;
        mCapturedNFCTag = "";
        updateList(id, mSelectedStandardListBO);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, @NotNull HashMap<Integer, Integer> mSelectedIdByLevelId, @NotNull ArrayList<Integer> mAttributeProducts, String mFilterText) {
        this.mFilteredPid = mProductId;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mAttributeProducts = mAttributeProducts;
        this.mFilterText = mFilterText;

        myList = new ArrayList<>();
        mAssetTrackingList = mSelectedStandardListBO.getAssetTrackingList();
        mBrandButton = mFilterText;
        if (mFilterText.equals("")) {
            mBrandButton = BRAND;
        }


        if (mAssetTrackingList == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mAttributeProducts != null && mProductId != 0) {//Both Product and attribute filter selected
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (assetBO.getParentHierarchy() != null && assetBO.getParentHierarchy().contains("/" + mProductId + "/")) {

                    if (ALL.equals(strBarCodeSearch)) {
                        if (mCapturedNFCTag.isEmpty()) {
                            if ((mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductId())
                                    && (mAttributeProducts.contains(assetBO.getProductId()))) {
                                myList.add(assetBO);
                            }
                        } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                            assetBO.setAvailQty(1);
                            myList.add(assetBO);
                        }
                    } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                        myList.add(assetBO);
                    }
                }
            }
        } else if (mAttributeProducts == null && mProductId != 0) {// product filter alone selected
            if (mSelectedIdByLevelId.size() == 0 || AppUtils.isMapEmpty(mSelectedIdByLevelId)) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY)

                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        if (assetBO.getProductId() > 0 && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        myList.add(assetBO);
                    }
                else
                    myList.addAll(mAssetTrackingList);
            } else {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (assetBO.getProductId()>0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (assetBO.getParentHierarchy() != null && assetBO.getParentHierarchy().contains("/" + mProductId + "/")) {

                        if (ALL.equals(strBarCodeSearch)) {
                            if (mCapturedNFCTag.isEmpty()) {
                                if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductId()) {
                                    myList.add(assetBO);
                                }
                            } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                assetBO.setAvailQty(1);
                                myList.add(assetBO);
                            }
                        } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                            myList.add(assetBO);
                        }
                    }
                }
            }
        } else if (mAttributeProducts != null && mProductId == 0) {// Attribute filter alone selected
            for (int pid : mAttributeProducts) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (assetBO.getProductId()>0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (pid == assetBO.getProductId()) {

                        if (ALL.equals(strBarCodeSearch)) {
                            if (mCapturedNFCTag.isEmpty()) {
                                if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductId()) {
                                    myList.add(assetBO);
                                }
                            } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                assetBO.setAvailQty(1);
                                myList.add(assetBO);
                            }
                        } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                            myList.add(assetBO);
                        }
                    }
                }
            }
        } else if (mFilterText.length() == 0) {
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (assetBO.getProductId() >0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (ALL.equals(strBarCodeSearch)) {
                    if (mCapturedNFCTag.isEmpty()) {
                        if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductId()) {
                            myList.add(assetBO);
                        }
                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                        assetBO.setAvailQty(1);
                        myList.add(assetBO);
                    }
                } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                    myList.add(assetBO);
                }

            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

        refreshList();
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, mSelectedLastFilterSelection);
    }

    /**
     * Show location alert
     */
    private void showLocation() {
        AlertDialog.Builder builderDialog;

        builderDialog = new AlertDialog.Builder(getActivity());
        builderDialog.setTitle(null);
        builderDialog.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
                        dialog.dismiss();
                        updateList(mSelectedLastFilterSelection, mSelectedStandardListBO);
                    }
                });

        mBModel.applyAlertDialogTheme(builderDialog);
    }

    /**
     * Product filter(Five level) click
     */
    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    mBModel.configurationMasterHelper.getGenFilter());
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            bundle.putString("isFrom", "posm");
            // set Fragment class Arguments
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, mFragment, "FiveFilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e.toString());
        }
    }


    private void scanBarCode() {
        ((PosmTrackingActivity) getActivity()).checkAndRequestPermissionAtRunTime(2);
        int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                @Override
                protected void startActivityForResult(Intent intent, int code) {
                    PosmTrackingFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
                }
            };
            integrator.setBeepEnabled(false).initiateScan();
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.permission_enable_msg)
                            + " " + getResources().getString(R.string.permission_camera)
                    , Toast.LENGTH_LONG).show();
        }


    }
}
