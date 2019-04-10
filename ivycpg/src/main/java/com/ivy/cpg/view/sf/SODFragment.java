package com.ivy.cpg.view.sf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.core.IvyConstants;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SODFragment extends IvyBaseFragment implements
        BrandDialogInterface, FiveLevelFilterCallBack {

    private SalesFundamentalHelper mSFHelper;
    private BusinessModel mBModel;

    private DrawerLayout mDrawerLayout;
    private Dialog dialog = null;
    private ViewHolder mSelectedHolder;
    private EditText mSelectedET;
    private EditText mParentTotal;
    private TextView tvSelectedName;
    private ListView mListView;

    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private final List<SODBO> mCategoryForDialog = new ArrayList<>();
    private ArrayAdapter<SFLocationBO> mLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    private static final String BRAND = "Brand";
    private String brandFilterText = "BRAND";
    private int mSelectedFilterId = -1;
    private String mImageName;
    private int mSelectedLocationIndex;
    private boolean isFromChild;
    private final int CAMERA_REQUEST_CODE = 1;
    private String sb = "";

    private final int SOD_RESULT_CODE = 113;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeView(view);
    }

    /**
     * Initialize views
     *
     * @param view Parent view
     */
    private void initializeView(View view) {

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());

        if (view != null) {
            mListView = view.findViewById(R.id.list);
            mListView.setCacheColorHint(0);
        }

        FrameLayout drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);


        tvSelectedName = view.findViewById(R.id.levelName);
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);

        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(
                            mSFHelper.mSelectedActivityName);
                }
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);

        Button btn_save = view.findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSOS();
            }
        });

        if (mBModel.configurationMasterHelper.isAuditEnabled())
            view.findViewById(R.id.audit).setVisibility(View.VISIBLE);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(mSFHelper.mSelectedActivityName);
            getActionBar().setElevation(0);
        }

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (SFLocationBO temp : mSFHelper.getLocationList())
            mLocationAdapter.add(temp);
        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
        }

        updateBrandText(BRAND, mSelectedFilterId);

        loadReasons();

        if (mSFHelper.getSODList() != null)
            calculateTotalValues();
    }

    /**
     * Add sum of values and show in bottom of the Screen
     */
    private void calculateTotalValues() {
        try {
            ArrayList<Integer> parentIds = new ArrayList<>();
            float mactual = 0;
            float mtotal = 0;
            float mtarget = 0;
            float mGap = 0;
            float mparcentagetot = 0;
            float mNamtot = 0;
            for (SODBO temp : mSFHelper.getSODList()) {
                if (temp.getIsOwn() == 1) {
                    if (!parentIds.contains(temp.getParentID())) {
                        mtotal = mtotal + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getParentTotal());
                        parentIds.add(temp.getParentID());
                    }
                    mtarget = mtarget + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getTarget());
                    mactual = mactual + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getActual());
                    mparcentagetot = mparcentagetot
                            + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getPercentage());
                    mGap = mGap + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getGap());
                    mNamtot = mNamtot + temp.getNorm();
                }

            }
            parentIds.clear();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Initialize Adapter and add reason for SOD module Reason Category : SOD
     */
    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if ("SOD".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }

        if (!(spinnerAdapter.getCount() > 0)) {
            ReasonMaster reasonMasterBo = new ReasonMaster();
            reasonMasterBo.setReasonDesc(getActivity().getResources().getString(R.string.select_reason));
            reasonMasterBo.setReasonID("0");
            spinnerAdapter.add(reasonMasterBo);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(mSFHelper.mSelectedActivityName
                        + "Camera Activity : Successfully Captured.");
                if (mSFHelper.mSelectedBrandID != 0) {
                    mSFHelper.onSaveImageName(
                            mSFHelper.mSelectedBrandID,
                            mImageName, HomeScreenTwo.MENU_SOD, mSelectedLocationIndex);
                }
            } else {
                Commons.print(mSFHelper.mSelectedActivityName
                        + "Camera Activity : Canceled");
            }
        } else if (requestCode == SOD_RESULT_CODE) {

            if (getActivity() != null)
                getActivity().overridePendingTransition(0, R.anim.zoom_exit);

            if (resultCode == 112) {

                mCategoryForDialog.clear();
                mCategoryForDialog.addAll(mSFHelper.getmCategoryForDialogSODBO());
                calculateTotalValues();
                mListView.invalidateViews();
            }
        }
    }

    /**
     * Five level filter call
     */
    private void FiveFilterFragment() {
        try {
            Collections.addAll(new Vector<String>(), getResources().getStringArray(
                    R.array.productFilterArray));

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
            bundle.putString("isFrom", "SF");
            bundle.putBoolean("isAttributeFilter", false);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mDrawerLayout.closeDrawers();

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        updateFiveFilterSelection(mFilteredPid, mSelectedIdByLevelId, mFilterText);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sales_fundamental, menu);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        // Change color if Filter is selected
        try {
            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            if (mBModel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            if (mBModel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOD))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (drawerOpen)
                menu.clear();
            // return super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.menu_next).setVisible(false);

            if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (mSFHelper.getLocationList().size() < 2)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_next) {
            saveSOS();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(
                    HomeScreenTwo.MENU_SOD);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_SOD);
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandFilterText = mFilterText;
            mSelectedFilterId = id;
            tvSelectedName.setText(mFilterText);
            ArrayList<SODBO> items = mSFHelper
                    .getSODList();
            if (items == null) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            ArrayList<SODBO> myList = new ArrayList<>();
            for (SODBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentID() == id || id == -1 && temp.getIsOwn() == 1) {
                    myList.add(temp);
                }
            }

            // set the new list to list view
            MyAdapter mSchedule = new MyAdapter(myList);
            mListView.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Load list based on five level filter selection
     *
     * @param mFilteredPid         FilteredPid Product ID
     * @param mSelectedIdByLevelId Selected product Id by level id
     */
    private void updateFiveFilterSelection(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, String mFilterText) {
        ArrayList<SODBO> items = mSFHelper.getSODList();
        if (items == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        ArrayList<SODBO> myList = new ArrayList<>();
        if (mFilterText.length() > 0) {
            for (SODBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                    if (temp.getIsOwn() == 1)
                        myList.add(temp);
                }
            }
        } else {
            myList.addAll(items);
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        MyAdapter mSchedule = new MyAdapter(myList);
        mListView.setAdapter(mSchedule);
    }

    /**
     * Save record in transaction table
     */
    private void saveSOS() {
        try {
            if (mSFHelper
                    .hasData(HomeScreenTwo.MENU_SOD)) {
                new SaveAsyncTask().execute();
            } else {
                mBModel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Shows alert dialog to denote image availability
     *
     * @param imageNameStarts Image Name
     */
    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + 1
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        if (dialog != null)
                            dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                CAMERA_REQUEST_CODE);

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    /**
     * Open Dialog with Competitor to Get Actual Values and Calculate Total
     * Value
     */
    private void getTotalValue(final int categoryId) {
        mSelectedET = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_salesfundamental_total);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        mParentTotal = dialog.findViewById(R.id.et_total);

        // setting no of charcters from congifuration
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sodDigits);
        mParentTotal.setFilters(FilterArray);

        mCategoryForDialog.clear();

        // All Brands in Total PopUp
        if (mSFHelper.getSODList() != null) {
            for (SODBO sodBO : mSFHelper.getSODList()) {
                if (sodBO.getParentID() == categoryId) {
                    mCategoryForDialog.add(sodBO);
                }
            }
        }

        ListView listView = dialog.findViewById(R.id.lv);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = displayMetrics.heightPixels / 3;
        listView.setLayoutParams(params);
        listView.setAdapter(new TotalDialogAdapter());
        dialog.findViewById(R.id.calczero)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcone)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calctwo)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcthree)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcfour)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcfive)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcsix)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcseven)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calceight)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcnine)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdel)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdot)
                .setOnClickListener(new MyClickListener());
        dialog.findViewById(R.id.calcdot).setVisibility(View.GONE);

        dialog.findViewById(R.id.btn_done)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mCategoryForDialog.isEmpty()) {
                            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                                SODBO sodbo = mCategoryForDialog.get(i);
                                if (sodbo.getLocations() != null && sodbo.getLocations().size() > 0) {
                                    sodbo.getLocations().get(mSelectedLocationIndex).setParentTotal(mParentTotal.getText().toString());
                                    sodbo.setGap(Integer.toString(0));
                                }

                                if (SDUtil.convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex).getParentTotal()) > 0) {

                                    float parentTotal = SDUtil
                                            .convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex).getParentTotal());
                                    float mNorm = sodbo.getNorm();
                                    float actual = SDUtil.convertToFloat(sodbo.getLocations().get(mSelectedLocationIndex)
                                            .getActual());

                                    float target = (parentTotal * mNorm) / 100;
                                    float gap = target - actual;
                                    float percentage = 0;
                                    if (parentTotal > 0)
                                        percentage = (actual / parentTotal) * 100;

                                    sodbo.getLocations().get(mSelectedLocationIndex).setTarget(mBModel.formatValue(target));
                                    sodbo.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel
                                            .formatPercent(percentage));
                                    sodbo.getLocations().get(mSelectedLocationIndex).setGap(mBModel.formatValue(-gap));
                                } else {
                                    sodbo.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                                    sodbo.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                                    sodbo.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
                                }
                            }
                        }
                        calculateTotalValues();
                        if (dialog != null)
                            dialog.dismiss();
                        mListView.invalidateViews();
                        dialog = null;
                    }
                });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null)
                    dialog.dismiss();
                mListView.invalidateViews();
                dialog = null;
            }
        });


        dialog.show();
    }

    /**
     * Add Actual Values and Update the Total
     */
    private void updateTotal() {
        int tot = 0;
        if (!mCategoryForDialog.isEmpty()) {
            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                SODBO sodbo = mCategoryForDialog.get(i);
                tot = tot + SDUtil.convertToInt(sodbo.getLocations().get(mSelectedLocationIndex).getActual());
            }
            String strTotal = tot + "";
            mParentTotal.setText(strTotal);

        }
    }


    /**
     * Alert dialog to show location
     */
    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        if (dialog != null)
                            dialog.dismiss();
                        updateBrandText(BRAND, mSelectedFilterId);
                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }

    class ViewHolder {
        SODBO mSOD;
        TextView tvBrandName;
        TextView tvActual;
        TextView tvPercentage;
        TextView tvGap;
        EditText etTotal;
        Spinner spnReason;
        ImageView btnPhoto;
        ImageButton audit;
        LinearLayout remark_layout, auditLayout;
    }

    /**
     * Adapter for listView
     */
    private class MyAdapter extends ArrayAdapter<SODBO> {
        private final ArrayList<SODBO> items;

        public MyAdapter(ArrayList<SODBO> mylist) {
            super(getActivity(), R.layout.row_sos, mylist);
            this.items = mylist;
        }

        public SODBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());
                row = inflater.inflate(R.layout.row_sos, parent, false);
                holder.tvBrandName = row
                        .findViewById(R.id.tvBrandName);
                holder.tvActual = row
                        .findViewById(R.id.tvActual);
                holder.tvPercentage = row
                        .findViewById(R.id.tvPercentage);
                holder.tvGap = row.findViewById(R.id.tvGap);
                holder.btnPhoto = row
                        .findViewById(R.id.btn_photo);
                holder.spnReason = row
                        .findViewById(R.id.spnReason);

                holder.etTotal = row
                        .findViewById(R.id.etTotal);
                holder.etTotal.setTag(holder);
                holder.audit = row
                        .findViewById(R.id.btn_audit);

                holder.auditLayout = row.findViewById(R.id.ll_audit);

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                                == IvyConstants.AUDIT_DEFAULT) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                                == IvyConstants.AUDIT_OK) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_NOT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                                == IvyConstants.AUDIT_NOT_OK) {

                            holder.mSOD.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_DEFAULT);
                            holder.audit.setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                holder.etTotal.setFocusable(false);

                holder.etTotal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mSFHelper.mSOSTotalPopUpType == 0) {
                            if (dialog != null) {
                                if (!dialog.isShowing()) {
                                    dialog.cancel();
                                    dialog = null;

                                }
                            }
                            // Open dialog
                            if (dialog == null) {
                                mSelectedHolder = (ViewHolder) v.getTag();
                                getTotalValue(mSelectedHolder.mSOD.getParentID());
                            }
                        } else {
                            mSelectedHolder = (ViewHolder) v.getTag();
                            Bundle bundle = new Bundle();

                            bundle.putInt("parent_id",
                                    mSelectedHolder.mSOD.getParentID());

                            bundle.putInt("parent_type_id",
                                    0);
                            bundle.putInt("product_id",
                                    mSelectedHolder.mSOD.getProductID());
                            bundle.putInt("flag", ShelfShareHelper.SOD);
                            bundle.putInt("selectedlocation", mSelectedLocationIndex);

                            Intent intent = new Intent(getActivity(), SODMeasureActivity.class);
                            intent.putExtras(bundle);

                            startActivityForResult(intent, SOD_RESULT_CODE);
                            getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.hold);
                        }

                    }

                });

                holder.spnReason.setAdapter(spinnerAdapter);
                holder.spnReason
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.spnReason
                                        .getSelectedItem();

                                holder.mSOD.getLocations().get(mSelectedLocationIndex).setReasonId(SDUtil
                                        .convertToInt(reString.getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBModel.isExternalStorageAvailable()) {
                            mImageName = "SOD_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            mSFHelper.mSelectedBrandID = holder.mSOD
                                    .getProductID();
                            String fnameStarts = "SOD_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOD.getProductID() + "_"
                                    + Commons.now(Commons.DATE);

                            boolean nfiles_there = mBModel
                                    .checkForNFilesInFolder(
                                            FileUtils.photoFolderPath,
                                            1, fnameStarts);
                            if (nfiles_there) {

                                showFileDeleteAlert(fnameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                String _path = FileUtils.photoFolderPath + "/"
                                        + mImageName;
                                intent.putExtra(CameraActivity.PATH, _path);
                                startActivityForResult(intent,
                                        CAMERA_REQUEST_CODE);
                                holder.btnPhoto.requestFocus();
                            }

                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    R.string.sdcard_is_not_ready_to_capture_img,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                if (mBModel.configurationMasterHelper.isAuditEnabled()) {

                    holder.audit.setVisibility(View.VISIBLE);
                    holder.auditLayout.setVisibility(View.VISIBLE);

                    holder.spnReason.setEnabled(false);
                    holder.spnReason.setClickable(false);
                    holder.btnPhoto.setEnabled(false);
                    holder.btnPhoto.setClickable(false);
                    holder.etTotal.setEnabled(false);
                    holder.etTotal.setEnabled(false);
                }


                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSOD = items.get(position);

            if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_DEFAULT)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSOD.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_NOT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.tvBrandName.setText(holder.mSOD.getProductName());

            if ("0.0".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getParentTotal())) {
                holder.etTotal.setText("0");
            } else {
                holder.etTotal.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getParentTotal());
            }

            String actual = holder.mSOD.getLocations().get(mSelectedLocationIndex).getActual() != null
                    ? holder.mSOD.getLocations().get(mSelectedLocationIndex).getActual() : "0";
            String target = holder.mSOD.getLocations().get(mSelectedLocationIndex).getTarget() != null
                    ? holder.mSOD.getLocations().get(mSelectedLocationIndex).getTarget() : "0";

            if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                float parentTotal = Float.parseFloat(holder.mSOD.getLocations().get(mSelectedLocationIndex).getParentTotal() != null ?
                        holder.mSOD.getLocations().get(mSelectedLocationIndex).getParentTotal() : "0");

                float percentage = 0;
                if (parentTotal > 0)
                    percentage = (Float.parseFloat(actual) / parentTotal) * 100;

                holder.mSOD.getLocations().get(mSelectedLocationIndex).setPercentage(percentage+"");
            }

            String percent = holder.mSOD.getLocations().get(mSelectedLocationIndex).getPercentage() != null
                    ? holder.mSOD.getLocations().get(mSelectedLocationIndex).getPercentage() : "0";


            String strActTarget = (actual + "/"+ target);

            holder.tvActual.setText(strActTarget);

            String strPerNorm = (percent + "/"+ String.valueOf(holder.mSOD.getNorm()));

            holder.tvPercentage.setText(strPerNorm);

            holder.tvGap.setText(holder.mSOD.getLocations().get(mSelectedLocationIndex).getGap());

            if (SDUtil.convertToFloat(holder.mSOD.getLocations().get(mSelectedLocationIndex).getGap()) < 0)
                holder.tvGap.setTextColor(Color.RED);
            else if (SDUtil.convertToFloat(holder.mSOD.getGap()) > 0)
                holder.tvGap.setTextColor(Color.rgb(34, 139, 34));
            else
                holder.tvGap.setTextColor(Color.BLACK);

            holder.spnReason.setSelection(getReasonIndex(holder.mSOD
                    .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
            holder.spnReason.setSelected(true);

            if ((holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName() != null)
                    && (!"".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))
                    && (!"null".equals(holder.mSOD.getLocations().get(mSelectedLocationIndex).getImageName()))) {
                Glide.with(getActivity())
                        .load(FileUtils.photoFolderPath + "/" + holder.mSOD.getLocations().get(mSelectedLocationIndex).getImgName())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_camera)
                        .transform(mBModel.circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera));
            }
            return row;
        }

        /**
         * Get the selected reason id, iterate and get position and set in the
         * spinner item
         *
         * @param reasonId
         * @return Index position
         */
        private int getReasonIndex(String reasonId) {
            if (spinnerAdapter.getCount() == 0)
                return 0;
            int len = spinnerAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster mReasonBO = spinnerAdapter.getItem(i);
                if (mReasonBO != null) {
                    if (mReasonBO.getReasonID().equals(reasonId))
                        return i;
                }
            }
            return -1;
        }
    }

    /**
     * Save call
     */
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mSFHelper
                        .saveSalesFundamentalDetails(HomeScreenTwo.MENU_SOD);
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SOD, true);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {

                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                        "", getResources().getString(R.string.saved_successfully),
                        false, getActivity().getResources().getString(R.string.ok),
                        null, new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
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

    }

    /**
     * List of Products with Actual Edit Text
     */
    private class TotalDialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCategoryForDialog.size();
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
            final CompetitorHolder holder;
            View row = convertView;
            if (row == null) {
                holder = new CompetitorHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());

                row = inflater.inflate(
                        R.layout.row_salesfundamental_total_list, parent, false);

                holder.tv = row.findViewById(R.id.tv);
                holder.et = row.findViewById(R.id.et);

                // setting no of charcters from congifuration
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sodDigits);

                holder.et.setFilters(FilterArray);

                holder.et.setOnTouchListener(new OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = holder.et;
                        int inType = holder.et.getInputType();
                        holder.et.setInputType(InputType.TYPE_NULL);
                        holder.et.onTouchEvent(event);
                        holder.et.setInputType(inType);
                        if (holder.et.getText().toString().equals("0") || holder.et.getText().toString().equals("0.0")
                                || holder.et.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.et.getText().toString().equals("0") || !holder.et.getText().toString().equals("0.0")
                                || !holder.et.getText().toString().equals("0.00"))
                            sb = holder.et.getText().toString();
                        if (holder.et.getText().length() > 0)
                            holder.et.setSelection(holder.et.getText().length());
                        return true;
                    }
                });
                holder.et.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                        if (holder.et.getText().toString().equals("0") || holder.et.getText().toString().equals("0.0")
                                || holder.et.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.et.getText().toString().equals("0") || !holder.et.getText().toString().equals("0.0")
                                || !holder.et.getText().toString().equals("0.00"))
                            sb = holder.et.getText().toString();
                        if (!"".equals(s.toString())) {
                            if (holder.et.length() > 0)
                                holder.et.setSelection(holder.et.length());
                            try {
                                holder.sodBO.getLocations().get(mSelectedLocationIndex).setActual(s.toString());
                            } catch (Exception e) {
                                holder.sodBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.sodBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
                        }
                        updateTotal();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                row.setTag(holder);
            } else {
                holder = (CompetitorHolder) row.getTag();
            }

            if (position == 0 && mSelectedET == null) {
                holder.et.requestFocus();
                mSelectedET = holder.et;
            }

            SODBO brand = mCategoryForDialog.get(position);

            holder.sodBO = brand;
           /* holder.tv.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.et.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));*/

            holder.tv.setText(brand.getProductName());
            holder.et.setText(brand.getLocations().get(mSelectedLocationIndex).getActual());

            return row;
        }

    }

    class CompetitorHolder {
        SODBO sodBO;
        TextView tv;
        EditText et;
    }

    /**
     * NumberPad click listener
     */
    private class MyClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero) {
                updateValue(0);

            } else if (i == R.id.calcone) {
                updateValue(1);

            } else if (i == R.id.calctwo) {
                updateValue(2);

            } else if (i == R.id.calcthree) {
                updateValue(3);

            } else if (i == R.id.calcfour) {
                updateValue(4);

            } else if (i == R.id.calcfive) {
                updateValue(5);

            } else if (i == R.id.calcsix) {
                updateValue(6);

            } else if (i == R.id.calcseven) {
                updateValue(7);

            } else if (i == R.id.calceight) {
                updateValue(8);

            } else if (i == R.id.calcnine) {
                updateValue(9);

            } else if (i == R.id.calcdel) {
                String s = mSelectedET.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);

                    if (s.length() == 0) {
                        s = "0";
                    }
                    mSelectedET.setText(s);
                }
            } else if (i == R.id.calcdot) {
                String s1 = mSelectedET.getText().toString();
                if (!s1.contains(".")) {
                    String strS1 = s1 + ".";
                    mSelectedET.setText(strS1);
                }

            }

        }
    }

    /**
     * Update value in view
     *
     * @param val selected value
     */
    private void updateValue(int val) {

        if (mSelectedET != null && mSelectedET.getText() != null) {
            String s = mSelectedET.getText().toString();
            sb = sb + val;
            if (sb.length() <= mSFHelper.sosDigits) {

                if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s)) {
                    mSelectedET.setText(String.valueOf(val));
                } else {
                    String strVal = mSelectedET.getText() + String.valueOf(val);
                    mSelectedET.setText(strVal);
                }
            } else {
                sb = "";
                Toast.makeText(getActivity(), getResources().getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void onBackButonClick() {

        if (mSFHelper
                .hasData(HomeScreenTwo.MENU_SOD)) {
            showAlert();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        }
    }

    private void showAlert() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                    mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                            .now(DateTimeUtils.TIME));
                    if (isFromChild)
                        startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                                .putExtra("isStoreMenu", true));
                    else
                        startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

            }
        }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

}
