package com.ivy.sd.png.view.asset;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.survey.SurveyActivityNew;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.ScannedUnmappedDialogFragment;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class
AssetTrackingFragment extends IvyBaseFragment implements  OnEditorActionListener, BrandDialogInterface,
        DataPickerDialogFragment.UpdateDateInterface{

    private DrawerLayout mDrawerLayout;
    private AlertDialog alertDialog;
    private ListView listview;
    private Button dateBtn;

    private BusinessModel mBusinessModel;
    private StandardListBO mSelectedStandardListBO;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int MOVEMENT_ASSET = 2;
    private int mSelectedLocationIndex;
    private int mSelectedLastFilterSelection = -1;
    private final String moduleName = "AT_";
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private static final String ALL = "ALL";
    private String photoPath = "";
    private static final String MENU_ASSET = "MENU_ASSET";
    private String strBarCodeSearch = "ALL";
    private String mCapturedNFCTag = "";
    private String imageName;
    private String filterText;
    private static final String TAG = "AssetTracking Screen";
    private static final String TAG_DATE_PICKER_INSTALLED = "date_picker_installed";
    private static final String TAG_DATE_PICKER_SERVICED = "date_picker_serviced";
    private String brandButton;
    private static String outPutDateFormat;
    private boolean isShowed = false;

    private ArrayList<AssetTrackingBO> myList;
    private ArrayList<AssetTrackingBO> mAssetTrackingList;
    private ArrayList<ReasonMaster> mAssetReasonList;
    private ArrayList<ReasonMaster> mAssetConditionList;
    private ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter;
    private ArrayAdapter<ReasonMaster> mAssetConditionAdapter;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private Vector<LevelBO> parentIdList;
    private ArrayList<Integer> mAttributeProducts;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBusinessModel = (BusinessModel) getActivity().getApplicationContext();
        mBusinessModel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_asset_tracking, container,
                false);

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setTypeface(mBusinessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButtonClick();
            }
        });

        FloatingActionButton btnBarcode = (FloatingActionButton) view.findViewById(R.id.fab_barcode);
        if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_BARCODE)
            btnBarcode.setVisibility(View.GONE);

        btnBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AssetTrackingActivity) getActivity()).checkAndRequestPermissionAtRunTime(2);
                int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                        @Override
                        protected void startActivityForResult(Intent intent, int code) {
                            AssetTrackingFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
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
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        //GA screen tracking
        BusinessModel.getInstance().trackScreenView(TAG);
    }

    @Override
    public void onStart() {
        super.onStart();

        mBusinessModel = (BusinessModel) getActivity().getApplicationContext();
        mBusinessModel.setContext(getActivity());

        if (getView() != null) {
            listview = (ListView) getView().findViewById(R.id.list);
            listview.setCacheColorHint(0);
        }


        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(mBusinessModel.assetTrackingHelper.mSelectedActivityName);
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (actionBar != null)
                    setScreenTitle(mBusinessModel.mSelectedActivityName);

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null)
                    setScreenTitle(getResources().getString(R.string.filter));

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;

        if (mBusinessModel.configurationMasterHelper.IS_TEAMLEAD && getView() != null) {
            TextView tvAudit = (TextView) getView().findViewById(R.id.audit);
            tvAudit.setVisibility(View.VISIBLE);

        }

        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mBusinessModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBusinessModel.productHelper.getmSelectedGLobalLocationIndex();
        }
        if (mLocationAdapter.getCount() > 0) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }

        if (!isShowed) {
            loadedItem();
            isShowed = true;
        }

        hideAndSeeK();

        if (parentIdList != null || mSelectedIdByLevelId != null || mAttributeProducts != null) {
            updateFromFiveLevelFilter(parentIdList, mSelectedIdByLevelId, mAttributeProducts, filterText);
        } else {
            updateBrandText(BRAND, mSelectedLastFilterSelection);
        }

        if (mBusinessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            mSelectedFilterMap.put("General", GENERAL);
            updateGeneralText(GENERAL);
        }
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

        if (mDrawerLayout != null) {
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        }

        menu.findItem(R.id.menu_add).setTitle(R.string.addnewasset);
        menu.findItem(R.id.menu_remove).setTitle(R.string.removeasset);

        if (!brandButton.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);

        if (mBusinessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        if (!mBusinessModel.assetTrackingHelper.SHOW_REMARKS_ASSET) {
            menu.findItem(R.id.menu_remarks).setVisible(false);
        }

        if (!mBusinessModel.assetTrackingHelper.SHOW_ADD_NEW_ASSET) {
            menu.findItem(R.id.menu_add).setVisible(false);
        }
        if (!mBusinessModel.assetTrackingHelper.SHOW_REMOVE_ASSET) {
            menu.findItem(R.id.menu_remove).setVisible(false);
        }
        if (mBusinessModel.assetTrackingHelper.SHOW_ASSET_ALL) {
            menu.findItem(R.id.menu_all).setVisible(true);
        }
        if (mBusinessModel.configurationMasterHelper.floating_Survey) {
            menu.findItem(R.id.menu_survey).setVisible(true);
        }

        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (mBusinessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mBusinessModel.productHelper.isFilterAvaiable(MENU_ASSET)) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        }
        if (!mBusinessModel.assetTrackingHelper.SHOW_MOVE_ASSET) {
            menu.findItem(R.id.menu_move).setVisible(false);
        }
        if (mBusinessModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (mBusinessModel.productHelper.getInStoreLocation().size() < 2)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }

        if (drawerOpen) {
            menu.clear();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {
                mBusinessModel.outletTimeStampHelper
                        .updateTimeStampModuleWise(SDUtil
                                .now(SDUtil.TIME));
                startActivity(new Intent(getActivity(),
                        HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_all) {
            strBarCodeSearch = ALL;
            if (mCapturedNFCTag != null) {
                mCapturedNFCTag = "";
            }
            updateList(-1, mSelectedStandardListBO);
            return true;
        } else if (i == R.id.menu_remarks) {
            FragmentTransaction ft = getActivity()
                    .getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog1 = new RemarksDialog(MENU_ASSET);
            dialog1.setCancelable(false);
            dialog1.show(ft, MENU_ASSET);
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_add) {

            AddAssetDialogFragment dialog = new AddAssetDialogFragment();
            dialog.show(getFragmentManager(), MENU_ASSET);

            return true;
        } else if (i == R.id.menu_remove) {
            Intent intent = new Intent(getActivity(), AssetPosmRemoveActivity.class);
            intent.putExtra("module", MENU_ASSET);
            startActivity(intent);
            return true;
        } else if (i == R.id.menu_product_filter) {
            if (mBusinessModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                mSelectedFilterMap.put("General", GENERAL);
            }
            productFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (mBusinessModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_move) {
            if (myList.size() >= 0) {
                Intent intent = new Intent(getActivity(), AssetMovementActivity.class);
                intent.putExtra("index", mSelectedLocationIndex);
                intent.putExtra("module", MENU_ASSET);
                startActivityForResult(intent, MOVEMENT_ASSET);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that to loaded values and set into arrayList and adapter
     */
    private void loadedItem() {

        String select_reason = "Select Reason";
        String select = "Select";
        String select_condition = "Select Condition";

        mAssetTrackingList = mBusinessModel.assetTrackingHelper.getAssetTrackingList();

        ReasonMaster reason1 = new ReasonMaster();
        reason1.setReasonID(Integer.toString(0));
        reason1.setReasonDesc(select_reason);
        mAssetReasonList = mBusinessModel.assetTrackingHelper.getAssetReasonList();
        mAssetReasonList.add(0, reason1);

        //Load Remarks
        ArrayList<ReasonMaster> mAssetRemarksList;
        ArrayAdapter<ReasonMaster> mAssetRemarksSpinAdapter;
        ReasonMaster reason2 = new ReasonMaster();
        reason2.setReasonID(Integer.toString(0));
        reason2.setReasonDesc(select);
        mAssetRemarksList = mBusinessModel.assetTrackingHelper.getAssetRemarksList();
        mAssetRemarksList.add(0, reason2);
        mAssetRemarksSpinAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout,
                mAssetRemarksList);
        mAssetRemarksSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        //

        ReasonMaster reason3 = new ReasonMaster();
        reason3.setConditionID(Integer.toString(0));
        reason3.setReasonDesc(select_condition);
        mAssetConditionList = mBusinessModel.assetTrackingHelper.getAssetConditionList();
        mAssetConditionList.add(0, reason3);

        mAssetReasonSpinAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mAssetReasonList);
        mAssetReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


        mAssetConditionAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mAssetConditionList);
        mAssetConditionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

    }

    /**
     * Method that to refresh item in listView
     */
    private void updateList(int bid, StandardListBO standardListBO) {
        int k = 0;
        myList = new ArrayList<>();
        ScannedUnmappedDialogFragment scannedUnmappedDialogFragment;

        ArrayList<AssetTrackingBO> mAllAssetTrackingList;
        mAssetTrackingList = standardListBO.getAssetTrackingList();
        mAllAssetTrackingList = standardListBO.getAllAssetTrackingList();

        if (mAssetTrackingList != null) {

            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (ALL.equals(strBarCodeSearch)) {

                    if ("".equals(mCapturedNFCTag)) {
                        if ((bid == -1 && "Brand".equals(brandButton)) || bid == assetBO.getProductId()) {
                            myList.add(assetBO);
                        }
                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                        assetBO.setAvailQty(1);
                        myList.add(assetBO);
                    }

                } else if (strBarCodeSearch.equals(assetBO.getSerialNo())) {
                    assetBO.setScanComplete(1);
                    myList.add(assetBO);
                } else {
                    if (mAllAssetTrackingList != null) {
                        mAllAssetTrackingList.remove(assetBO);
                    }
                }
            }

            if (mBusinessModel.assetTrackingHelper.SHOW_ASSET_BARCODE) {
                if (mAllAssetTrackingList != null) {

                    for (int i = 0; i < mAllAssetTrackingList.size(); i++) {
                        if (strBarCodeSearch.equalsIgnoreCase(mAllAssetTrackingList.get(i).getSerialNo())) {

                            if (!mBusinessModel.assetTrackingHelper.isExistingAssetInRetailer(strBarCodeSearch)) {
                                scannedUnmappedDialogFragment = new ScannedUnmappedDialogFragment();
                                Bundle args = new Bundle();
                                args.putString("serialNo", strBarCodeSearch);
                                args.putString("assetName", mAllAssetTrackingList.get(i).getAssetName());
                                args.putInt("assetId", mAllAssetTrackingList.get(i).getAssetID());
                                args.putString("brand", mAllAssetTrackingList.get(i).getBrand());
                                args.putString("retailerName", mBusinessModel.getRetailerMasterBO().getRetailerName());
                                scannedUnmappedDialogFragment.setArguments(args);
                                scannedUnmappedDialogFragment.show(getFragmentManager(), "Asset");
                                k = 1;
                                break;
                            } else {
                                Toast.makeText(mBusinessModel, "Asset Already Scanned and Mapped. Waiting for Approval.", Toast.LENGTH_SHORT).show();
                                k = 1;
                                break;
                            }
                        }
                    }
                }
            }
        }

        int size = myList.size();
        refreshList();
        if (size == 0 && k == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_assets_exists),
                    Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * Updating List by NFC tag
     *
     * @param mNFCTag NFC tag
     */
    public void updateListByNFCTag(String mNFCTag) {
        mCapturedNFCTag = mNFCTag;
        strBarCodeSearch = ALL;
        updateList(-1, mSelectedStandardListBO);
    }

    /**
     * Method that to refresh item in listView
     */
    private void refreshList() {

        MyAdapter adapter = new MyAdapter(myList);
        listview.setAdapter(adapter);
        int size = myList.size();
        if (size == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_assets_exists),
                    Toast.LENGTH_SHORT).show();

        }
    }


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
                        R.layout.row_asset_tracking, parent, false);
                row.setTag(holder);

                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);
                holder.assetNameTV = (TextView) row
                        .findViewById(R.id.tv_asset_name);
                holder.assetNameTV.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.reason1Spin = (Spinner) row
                        .findViewById(R.id.spin_reason1);
                holder.reason1Spin.setAdapter(mAssetReasonSpinAdapter);
                holder.mConditionSpin = (Spinner) row
                        .findViewById(R.id.spin_condition);
                holder.mConditionSpin.setAdapter(mAssetConditionAdapter);
                holder.mInstallDate = (Button) row
                        .findViewById(R.id.Btn_install_Date);
                holder.llInstallDate = (LinearLayout) row
                        .findViewById(R.id.ll_install_date);
                holder.mInstallDate.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mServiceDate = (Button) row
                        .findViewById(R.id.Btn_service_Date);
                holder.ll_service_date = (LinearLayout) row
                        .findViewById(R.id.ll_service_date);
                holder.mServiceDate.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.photoBTN = (ImageView) row
                        .findViewById(R.id.btn_photo);
                holder.availQtyRB = (CheckBox) row
                        .findViewById(R.id.radio_avail_qty);
                holder.availQtyLL = (LinearLayout) row
                        .findViewById(R.id.ll_avail_qty);
                holder.serialNoTV = (TextView) row
                        .findViewById(R.id.tv_serialNo);
                holder.serialNoTV.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.grpTV = (TextView) row.findViewById(R.id.tv_grp);
                holder.grpTV.setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.execQtyLL = (LinearLayout) row.findViewById(R.id.ll_exec_qty);
                holder.execQtyRB = (CheckBox) row.findViewById(R.id.radio_exec_qty);


                if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_QTY)
                    holder.availQtyLL.setVisibility(View.GONE);

                if (mBusinessModel.assetTrackingHelper.SHOW_ASSET_EXECUTED)
                    holder.execQtyLL.setVisibility(View.VISIBLE);

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.assetBO.getAudit() == 2) {

                            holder.assetBO.setAudit(1);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.assetBO.getAudit() == 1) {

                            holder.assetBO.setAudit(0);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.assetBO.getAudit() == 0) {

                            holder.assetBO.setAudit(2);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_none);
                        }

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

                holder.mInstallDate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mInstallDate;
                        dateBtn.setTag(holder.assetBO);
                        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                        newFragment.show(getActivity()
                                .getSupportFragmentManager(), TAG_DATE_PICKER_INSTALLED);
                    }
                });
                holder.mServiceDate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mServiceDate;
                        dateBtn.setTag(holder.assetBO);
                        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                        newFragment.show(getActivity()
                                .getSupportFragmentManager(), TAG_DATE_PICKER_SERVICED);
                    }
                });

                holder.photoBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (mBusinessModel.synchronizationHelper
                                .isExternalStorageAvailable()) {

                            photoPath = getActivity().getExternalFilesDir(
                                    Environment.DIRECTORY_PICTURES)
                                    + "/" + DataMembers.photoFolderName + "/";

                            imageName = moduleName
                                    + mBusinessModel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.assetBO.getAssetID() + "_"
                                    + holder.assetBO.getSerialNo() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            String fileNameStarts = moduleName
                                    + mBusinessModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + mSelectedStandardListBO.getListID() + "_" + holder.assetBO.getAssetID() + "_"
                                    + holder.assetBO.getSerialNo() + "_"
                                    + Commons.now(Commons.DATE);

                            Commons.print(TAG + ",FName Starts :" + fileNameStarts);

                            mBusinessModel.assetTrackingHelper.mSelectedAssetID = holder.assetBO
                                    .getAssetID();
                            mBusinessModel.assetTrackingHelper.mSelectedImageName = imageName;
                            mBusinessModel.assetTrackingHelper.mSelectedSerial = holder.assetBO.getSerialNo();

                            boolean nFilesThere = mBusinessModel.checkForNFilesInFolder(photoPath, 1,
                                    fileNameStarts);
                            if (nFilesThere) {
                                showFileDeleteAlertWithImage(holder.assetBO.getAssetID()
                                        + "", fileNameStarts, holder.assetBO.getImgName());
                            } else {
                                captureCustom();
                                holder.photoBTN.requestFocus();
                            }

                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.external_storage_not_available)
                                    , Toast.LENGTH_SHORT)
                                    .show();
                            getActivity().finish();
                        }

                    }
                });
                holder.availQtyRB.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (holder.assetBO.getAvailQty() == 0) {

                            holder.availQtyRB.setChecked(true);
                            holder.assetBO.setAvailQty(1);
                            holder.reason1Spin.setEnabled(false);

                            if ((holder.assetBO.getImageName() != null)
                                    && (!holder.assetBO.getImageName().isEmpty())
                                    ) {
                                holder.photoBTN.setEnabled(true);
                                setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);
                            } else {
                                holder.photoBTN.setEnabled(true);
                                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                            }

                            holder.reason1Spin.setSelection(0);
                            holder.mConditionSpin.setEnabled(true);
                            holder.mConditionSpin.setSelection(0);
                            holder.mInstallDate.setEnabled(true);
                            holder.mServiceDate.setEnabled(true);
                        } else {

                            holder.availQtyRB.setChecked(false);
                            holder.assetBO.setAvailQty(0);
                            holder.reason1Spin.setEnabled(true);
                            holder.photoBTN.setEnabled(false);
                            holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                            holder.mConditionSpin.setEnabled(false);
                            holder.mConditionSpin.setSelection(0);
                            holder.mInstallDate.setEnabled(false);
                            holder.mServiceDate.setEnabled(false);
                            holder.assetBO.setInstallDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                            holder.assetBO.setServiceDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                            holder.mInstallDate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                            holder.mServiceDate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));

                        }

                    }
                });

                holder.execQtyRB.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (holder.assetBO.getExecutorQty() == 0) {
                            holder.execQtyRB.setChecked(true);
                            holder.assetBO.setExecutorQty(1);
                        } else {
                            holder.execQtyRB.setChecked(false);
                            holder.assetBO.setExecutorQty(0);
                        }
                    }
                });

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.assetBO = items.get(position);

            if (holder.assetBO.getAudit() == 2)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.assetBO.getAudit() == 1)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.assetBO.getAudit() == 0)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.assetNameTV.setText(holder.assetBO.getAssetName());
            holder.reason1Spin.setSelection(mBusinessModel.assetTrackingHelper
                    .getItemIndex(holder.assetBO.getReason1ID(),
                            mAssetReasonList));

            String serialNo = getResources().getString(R.string.serial_no)
                    + ":" + holder.assetBO.getSerialNo();
            holder.serialNoTV.setText(serialNo);

            holder.mInstallDate
                    .setText((holder.assetBO.getInstallDate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getInstallDate());
            holder.mServiceDate
                    .setText((holder.assetBO.getServiceDate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getServiceDate());
            holder.grpTV.setText(holder.assetBO.getGroupLevelName());

            if (holder.assetBO.getAvailQty() > 0) {

                holder.reason1Spin.setEnabled(false);
                if ((holder.assetBO.getImageName() != null)
                        && (!holder.assetBO.getImageName().isEmpty())
                        ) {
                    holder.photoBTN.setEnabled(true);
                    setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);
                } else {
                    holder.photoBTN.setEnabled(true);
                    holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                }

                holder.reason1Spin.setSelection(0);
                holder.mConditionSpin.setEnabled(true);
                holder.mConditionSpin.setSelection(mBusinessModel.assetTrackingHelper
                        .getConditionItemIndex(holder.assetBO.getConditionID(),
                                mAssetConditionList));
                holder.mInstallDate.setEnabled(true);
                holder.mServiceDate.setEnabled(true);

            } else {

                holder.reason1Spin.setEnabled(true);
                holder.photoBTN.setEnabled(false);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.mConditionSpin.setEnabled(false);
                holder.mConditionSpin.setSelection(0);
                holder.mInstallDate.setEnabled(false);
                holder.mServiceDate.setEnabled(false);
                holder.assetBO.setImageName("");
                holder.assetBO.setImgName("");
                holder.assetBO.setInstallDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.assetBO.setServiceDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.mInstallDate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.mServiceDate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));

            }

            if (mBusinessModel.assetTrackingHelper.ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK) {
                if (holder.assetBO.getNFCTagId().isEmpty())
                    holder.availQtyRB.setEnabled(true);
                else
                    holder.availQtyRB.setEnabled(false);
            } else {
                holder.availQtyRB.setEnabled(true);
            }


            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_REASON) {
                holder.reason1Spin.setVisibility(View.GONE);
            }
            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_CONDITION) {
                holder.mConditionSpin.setVisibility(View.GONE);
            }
            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_INSTALL_DATE) {
                holder.mInstallDate.setVisibility(View.GONE);
                holder.llInstallDate.setVisibility(View.GONE);
            }
            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_SERVICE_DATE) {
                holder.mServiceDate.setVisibility(View.GONE);
                holder.ll_service_date.setVisibility(View.GONE);
            }

            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_PHOTO) {
                holder.photoBTN.setVisibility(View.GONE);
            }

            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_GRP) {
                holder.grpTV.setVisibility(View.GONE);
            }

            if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_EXECUTED) {
                holder.execQtyLL.setVisibility(View.GONE);
            }

            if ((holder.assetBO.getImageName() != null)
                    && (!holder.assetBO.getImageName().isEmpty())
                    ) {
                setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);

            } else {
                if (!holder.photoBTN.isEnabled())
                    holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_grey_24dp));
                else
                    holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_blue_24dp));
            }

            if (holder.assetBO.getAvailQty() == 1) {
                holder.availQtyRB.setChecked(true);
            } else {
                holder.availQtyRB.setChecked(false);
            }

            if (mBusinessModel.assetTrackingHelper.SHOW_ASSET_BARCODE)
                if (holder.assetBO.getScanComplete() == 1) {
                    holder.availQtyRB.setChecked(true);
                    holder.availQtyRB.setEnabled(false);
                } else {
                    holder.availQtyRB.setChecked(false);
                    holder.availQtyRB.setEnabled(true);
                }


            return row;
        }
    }

    /**
     * Load picture in Image View
     *
     * @param imageName Image Name
     * @param imageView Image View to show Image
     */
    private void setPictureToImageView(String imageName, ImageView imageView) {

        Glide.with(getActivity()).load(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + "/" + DataMembers.photoFolderName + "/" + imageName)
                .centerCrop()
                .placeholder(R.drawable.ic_photo_camera_blue_24dp)
                .error(R.drawable.no_image_available)
                .override(35, 20)
                .transform(mBusinessModel.circleTransform)
                .into(imageView);
    }

    class ViewHolder {
        AssetTrackingBO assetBO;
        TextView assetNameTV;
        TextView serialNoTV;
        Spinner reason1Spin;
        Spinner mConditionSpin;
        ImageView photoBTN;
        Button mInstallDate;
        LinearLayout llInstallDate;
        Button mServiceDate;
        LinearLayout ll_service_date;
        CheckBox availQtyRB;
        LinearLayout availQtyLL;
        ImageButton audit;
        TextView grpTV;
        CheckBox execQtyRB;
        LinearLayout execQtyLL;
    }


    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }


    /**
     * Method to check already image captured or not if Already captured, it
     * will show Alert Dialog In Alert Dialog, if click yes,remove image in
     * sdcard and retake photo. If click No, Alert Dialog dismiss
     *
     * @param mAssetId        AssetId
     * @param imageNameStarts imageName
     * @param imageSrc        imagePath
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
                new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
                            if (mAssetId.equals(Integer.toString(assetBO.getAssetID()))) {
                                assetBO.setImageName("");
                            }
                        }
                        mBusinessModel.assetTrackingHelper
                                .deleteImageName(imageNameStarts);
                        mBusinessModel.synchronizationHelper.deleteFiles(photoPath,
                                imageNameStarts);

                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String path = photoPath + "/" + imageName;
                        intent.putExtra("path", path);
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
     * @param assetID  Asset Id
     * @param serialNo Serial Number
     * @param imgName  Image Name
     */
    private void onSaveImageName(int assetID, String serialNo, String imgName) {

        String imagePath = "Asset/"
                + mBusinessModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + mBusinessModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imgName;

        for (AssetTrackingBO assetBO : mAssetTrackingList) {
            if (assetID == assetBO.getAssetID() &&
                    serialNo.equals(assetBO.getSerialNo())) {
                assetBO.setImageName(imagePath);
                assetBO.setImgName(imgName);
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
                if (mBusinessModel.assetTrackingHelper.mSelectedAssetID != 0) {
                    onSaveImageName(
                            mBusinessModel.assetTrackingHelper.mSelectedAssetID,
                            mBusinessModel.assetTrackingHelper.mSelectedSerial,
                            mBusinessModel.assetTrackingHelper.mSelectedImageName);
                }

            } else {
                Commons.print(TAG + "," + "Camera Activity : Canceled");
            }
        } else if (requestCode == MOVEMENT_ASSET) {
            mBusinessModel.assetTrackingHelper.loadDataForAssetPOSM(MENU_ASSET);
        } else {

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                    } else {
                        strBarCodeSearch = result.getContents();
                    }
                }
            }

        }


    }

    /**
     * Deleting Un used images
     */
    public void deleteUnusedImages() {

        for (AssetTrackingBO temp : myList) {
            if (temp.getAvailQty() == 0 && !"".equals(temp.getImgName())) {
                String fileName = temp.getImgName();
                deleteFiles(fileName);
            }
        }
    }

    /**
     * Deleting image files
     *
     * @param filename File Name
     */
    private void deleteFiles(String filename) {

        File folder = new File(HomeScreenFragment.photoPath + "/");
        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Success");
            }
        }
    }

    /**
     * This AsyncTask class is used to save Asset Details in table
     */
    private class SaveAsset extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            mBusinessModel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            deleteUnusedImages();
            mBusinessModel.assetTrackingHelper.saveAsset(MENU_ASSET);
            mBusinessModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            alertDialog.dismiss();
            mBusinessModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));

            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    null, new CommonDialog.positiveOnClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                    Bundle extras = getActivity().getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtra("IsMoveNextActivity", mBusinessModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                        intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                    }

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

        if (mBusinessModel.assetTrackingHelper.hasAssetTaken()) {
            new SaveAsset().execute("");
        } else {

            AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                    getActivity());
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)
                    .setTitle(getResources().getString(R.string.no_assets_exists))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.cancel();
                                }
                            });

            mBusinessModel.applyAlertDialogTheme(alertDialogBuilder1);

        }
    }

    /**
     * Method that to show visibility and hided column
     */
    private void hideAndSeeK() {

        View view = getView();
        if (view != null && (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_TARGET || !mBusinessModel.assetTrackingHelper.SHOW_ASSET_QTY)) {
            view.findViewById(R.id.keypad).setVisibility(View.GONE);
        }
        if (view != null && mBusinessModel.assetTrackingHelper.SHOW_COMPETITOR_QTY)
            view.findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        if (view != null && mBusinessModel.configurationMasterHelper.IS_TEAMLEAD)
            getView().findViewById(R.id.keypad).setVisibility(View.GONE);
        if (view != null && !mBusinessModel.assetTrackingHelper.SHOW_ASSET_PHOTO) {
            view.findViewById(R.id.tv_is_photo).setVisibility(View.GONE);
        }

        if (!mBusinessModel.assetTrackingHelper.SHOW_ASSET_QTY && view != null) {
            view.findViewById(R.id.tv_isAvail).setVisibility(View.GONE);
        } else {
            try {
                if (view != null) {
                    if (mBusinessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.tv_isAvail).getTag()) != null) {
                        ((TextView) view.findViewById(R.id.tv_isAvail))
                                .setText(mBusinessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.tv_isAvail).getTag()));

                    }
                    ((TextView) view.findViewById(R.id.tv_isAvail)).setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }


        try {
            if (view != null) {
                if (mBusinessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_asset_name).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_asset_name))
                            .setText(mBusinessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_asset_name).getTag()));
                }
                ((TextView) view.findViewById(R.id.tv_header_asset_name)).setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        if (view != null && !mBusinessModel.assetTrackingHelper.SHOW_ASSET_EXECUTED)
            view.findViewById(R.id.tv_is_executed).setVisibility(View.GONE);
        else {

            try {
                if (view != null) {
                    if (mBusinessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.tv_is_executed).getTag()) != null) {
                        ((TextView) view.findViewById(R.id.tv_is_executed))
                                .setText(mBusinessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.tv_is_executed).getTag()));

                    }
                    ((TextView) view.findViewById(R.id.tv_is_executed)).setTypeface(mBusinessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

    }

    /**
     * Custom camera call
     */
    private void captureCustom() {

        final String actionScannerInputPlugin = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
        final String extraParameter = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
        final String disablePlugin = "DISABLE_PLUGIN";
        try {
            Intent i = new Intent();
            i.setAction(actionScannerInputPlugin);
            i.putExtra(extraParameter, disablePlugin);
            getActivity().sendBroadcast(i);

            Thread.sleep(1000);

            Intent intent = new Intent(getActivity(),
                    CameraActivity.class);
            intent.putExtra("quality", 40);
            String path = photoPath + "/" + imageName;
            intent.putExtra("path", path);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * Show location dialog
     */
    private void showLocation() {

        AlertDialog.Builder builderDialog;
        builderDialog = new AlertDialog.Builder(getActivity());
        builderDialog.setTitle(null);
        builderDialog.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        strBarCodeSearch = ALL;
                        mSelectedLocationIndex = item;
                        mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
                        dialog.dismiss();
                        updateList(mSelectedLastFilterSelection, mSelectedStandardListBO);
                    }
                });

        mBusinessModel.applyAlertDialogTheme(builderDialog);
    }


    /**
     * Product filter click
     */
    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", "Brand");
            bundle.putString("filterHeader", mBusinessModel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            bundle.putString("isFrom", "Survey");
            bundle.putSerializable("serilizeContent",
                    mBusinessModel.productHelper.getRetailerModuleChildLevelBO());

            if (mBusinessModel.productHelper.getRetailerModuleParentLeveBO() != null
                    && mBusinessModel.productHelper.getRetailerModuleParentLeveBO()
                    .size() > 0) {

                bundle.putBoolean("isFormBrand", true);
                bundle.putString("pfilterHeader", mBusinessModel.productHelper
                        .getRetailerModuleParentLeveBO().get(0)
                        .getPl_productLevel());

                mBusinessModel.productHelper.setPlevelMaster(mBusinessModel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set FragmentClass Arguments
            FilterFragment mFragment = new FilterFragment(mSelectedFilterMap);
            mFragment.setArguments(bundle);
            ft.add(R.id.right_drawer, mFragment, "filter");
            ft.commit();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        brandButton = mFilterText;
        mDrawerLayout.closeDrawers();
        mCapturedNFCTag = "";
        mSelectedLastFilterSelection = id;
        updateList(id, mSelectedStandardListBO);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {
    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, mSelectedLastFilterSelection);
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

        myList = new ArrayList<>();
        mAssetTrackingList = mSelectedStandardListBO.getAssetTrackingList();
        for (LevelBO levelBO : mParentIdList) {
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (levelBO.getProductID() == assetBO.getProductId()) {

                    if (ALL.equals(strBarCodeSearch)) {
                        if ("".equals(mCapturedNFCTag)) {
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

        mDrawerLayout.closeDrawers();
        refreshList();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        this.parentIdList = mParentIdList;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mAttributeProducts = mAttributeProducts;
        this.filterText = mFilterText;

        myList = new ArrayList<>();
        mAssetTrackingList = mSelectedStandardListBO.getAssetTrackingList();
        brandButton = mFilterText;
        if (mAssetTrackingList == null) {
            mBusinessModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mAttributeProducts != null && !mParentIdList.isEmpty()) {//Both Product and attribute filter selected
            for (LevelBO levelBO : mParentIdList) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (levelBO.getProductID() == assetBO.getProductId()) {

                        if (ALL.equals(strBarCodeSearch)) {
                            if ("".equals(mCapturedNFCTag)) {
                                if ((mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductId())
                                        && mAttributeProducts.contains(assetBO.getProductId())) {
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
        } else if (mAttributeProducts == null && !mParentIdList.isEmpty()) {// product filter alone selected
            if (mSelectedIdByLevelId.size() == 0 || mBusinessModel.isMapEmpty(mSelectedIdByLevelId)) {
                myList.addAll(mAssetTrackingList);
            } else {
                for (LevelBO levelBO : mParentIdList) {
                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        if (levelBO.getProductID() == assetBO.getProductId()) {

                            if (ALL.equals(strBarCodeSearch)) {
                                if ("".equals(mCapturedNFCTag)) {
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
            }
        } else if (mAttributeProducts != null && !mParentIdList.isEmpty()) {// Attribute filter alone selected
            for (int pid : mAttributeProducts) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (pid == assetBO.getProductId()) {

                        if (ALL.equals(strBarCodeSearch)) {
                            if ("".equals(mCapturedNFCTag)) {
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
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

        refreshList();
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
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    mBusinessModel.configurationMasterHelper.getGenFilter());
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            bundle.putString("isFrom", "asset");

            // set FragmentClass Arguments
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, mFragment, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
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
                bo.setInstallDate(DateUtil.convertDateObjectToRequestedFormat(
                        Calendar.getInstance().getTime(), outPutDateFormat));
                dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                        .getInstance().getTime(), outPutDateFormat));
            } else {

                bo.setInstallDate(DateUtil.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }

        } else if (TAG_DATE_PICKER_SERVICED.equals(tag)) {

            if (bo.getInstallDate() != null
                    && bo.getInstallDate().length() > 0) {
                Date mInstallDate = DateUtil.convertStringToDateObject(
                        bo.getInstallDate(), outPutDateFormat);
                if (mInstallDate != null && selectedDate.getTime() != null
                        && mInstallDate.after(selectedDate.getTime())) {
                    Toast.makeText(getActivity(),
                            R.string.servicedate_set_after_installdate,
                            Toast.LENGTH_SHORT).show();
                } else if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setServiceDate(DateUtil.convertDateObjectToRequestedFormat(
                            Calendar.getInstance().getTime(), outPutDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    bo.setServiceDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else {

                bo.setServiceDate(DateUtil.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }
        }

    }
}
