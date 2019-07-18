package com.ivy.cpg.view.sf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
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

public class SOSFragment extends IvyBaseFragment implements
        BrandDialogInterface, FiveLevelFilterCallBack {


    private static final String BRAND = "Brand";
    private int mSelectedFilterId = -1;
    private String brandFilterText = "BRAND";
    private String mImageName;
    private int mSelectedLocationIndex;
    private boolean isFromChild;
    private String sb = "";
    private final int CAMERA_REQUEST_CODE = 1;

    SalesFundamentalHelper mSFHelper;
    private BusinessModel mBModel;

    private DrawerLayout mDrawerLayout;
    private ListView mListView;
    private Dialog dialog = null;
    private ViewHolder mSelectedHolder;
    private EditText mSelectedET;
    private EditText mParentTotal;
    private TextView tvSelectedName;

    private final List<SOSBO> mCategoryForDialog = new ArrayList<>();
    private ArrayAdapter<SFLocationBO> mLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private ArrayList<String> totalImgList = new ArrayList<>();
    private ArrayAdapter<ReasonMaster> spinnerAdapter;

    private final int SHARE_SHELF_RESULT_CODE = 112;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBModel = (BusinessModel) context.getApplicationContext();
        mBModel.setContext(((Activity)context));
        mSFHelper = SalesFundamentalHelper.getInstance(context);
        initializeViews(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();


        if (mBModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(context,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            ((Activity)context).finish();
        }

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(mSFHelper.mSelectedActivityName);
            getActionBar().setElevation(0);
        }

        isFromChild = ((Activity)context).getIntent().getBooleanExtra("isFromChild", false);

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice);

        for (SFLocationBO temp : mSFHelper.getLocationList())
            mLocationAdapter.add(temp);
        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
        }

        updateBrandText(BRAND, mSelectedFilterId);
        loadReasons();

        if (mSFHelper.getSOSList() != null)
            calculateTotalValues();

    }

    /**
     * Initialize views
     *
     * @param view Parent view
     */
    private void initializeViews(View view) {

        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);

        if (view != null) {
            mListView = view.findViewById(R.id.list);
            mListView.setCacheColorHint(0);
        }

        FrameLayout drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(((Activity)context), /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(mSFHelper.mSelectedActivityName);
                }

                ((Activity)context).invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                ((Activity)context).invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);


        tvSelectedName = view.findViewById(R.id.levelName);
        Button btn_save = view.findViewById(R.id.btn_save);

        try {
            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.hlength).getTag()) != null)
                ((TextView) view.findViewById(R.id.hlength))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.hlength).getTag()));

            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.hpercent).getTag()) != null)
                ((TextView) view.findViewById(R.id.hpercent))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.hpercent).getTag()));


            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.levelName).getTag()) != null)
                ((TextView) view.findViewById(R.id.levelName))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.levelName).getTag()));

            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.hTotal).getTag()) != null)
                ((TextView) view.findViewById(R.id.hTotal))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.hTotal).getTag()));

            if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.hGap).getTag()) != null)
                ((TextView) view.findViewById(R.id.hGap))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.hGap).getTag()));


        } catch (Exception e) {
            Commons.printException(e + "");
        }


        btn_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSOS();
            }
        });

        if (mBModel.configurationMasterHelper.isAuditEnabled())
            view.findViewById(R.id.audit).setVisibility(View.VISIBLE);

    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) context).getSupportActionBar();
    }

    /**
     * Add sum of values and show in bottom of the Screen
     */
    private void calculateTotalValues() {
        try {
            ArrayList<Integer> parentIds = new ArrayList<>();
            float mActual = 0;
            float mPercentageTotal = 0;
            float mTarget = 0;
            float mTotal = 0;
            float mGap = 0;
            float mNormTotal = 0;
            for (SOSBO temp : mSFHelper.getSOSList()) {
                if (temp.getIsOwn() == 1) {
                    if (!parentIds.contains(temp.getParentID())) {
                        mTotal = mTotal
                                + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getParentTotal());
                        parentIds.add(temp.getParentID());
                    }
                    mTarget = mTarget + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getTarget());
                    mActual = mActual + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getActual());
                    mGap = mGap + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getGap());
                    mPercentageTotal = mPercentageTotal
                            + SDUtil.convertToFloat(temp.getLocations().get(mSelectedLocationIndex).getPercentage());
                    mNormTotal = mNormTotal + temp.getNorm();
                }

            }
            parentIds.clear();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Initialize Adapter and add reason
     */
    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ReasonMaster reason = new ReasonMaster();
        reason.setReasonID("-1");
        reason.setReasonDesc(getResources().getString(R.string.other_reason));
        reason.setReasonCategory("SOS");

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if ("SOS".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }

        if (!(spinnerAdapter.getCount() > 0)) {
            ReasonMaster reasonMasterBo = new ReasonMaster();
            reasonMasterBo.setReasonDesc(getResources().getString(R.string.select_reason));
            reasonMasterBo.setReasonID("0");
            spinnerAdapter.add(reasonMasterBo);
        }
        spinnerAdapter.add(reason);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == 1
                && mSFHelper.mSelectedBrandID != 0) {

            // Photo saved successfully
            totalImgList.add(mImageName);
            mSFHelper.onSaveImageName(
                    mSFHelper.mSelectedBrandID,
                    mImageName, HomeScreenTwo.MENU_SOS, mSelectedLocationIndex);
        }else if (requestCode == SHARE_SHELF_RESULT_CODE){

            ((Activity)context).overridePendingTransition(0, R.anim.zoom_exit);

            if (resultCode == 1){
                mCategoryForDialog.clear();
                mCategoryForDialog.addAll(mSFHelper.getmCategoryForDialogSOSBO());
                calculateTotalValues();
                mListView.invalidateViews();
            }
        }
    }

    /**
     * Delete un wanted images
     */
    private void deleteUnsavedImageFromFolder() {
        for (String imgList : totalImgList) {
            mBModel.deleteFiles(FileUtils.photoFolderPath,
                    imgList);
        }
    }

    /**
     * Five filter call
     */
    private void FiveFilterFragment() {
        try {
            Collections.addAll(new Vector<>(), getResources().getStringArray(
                    R.array.productFilterArray));

            mDrawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    mBModel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "SF");
            bundle.putBoolean("isAttributeFilter", false);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);
            ft.replace(R.id.right_drawer, mFragment, "Fivefilter");
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

            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (drawerOpen)
                menu.clear();

            menu.findItem(R.id.menu_next).setVisible(false);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (mBModel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOS))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

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
            FragmentManager ft = ((FragmentActivity)context)
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(
                    HomeScreenTwo.MENU_SOS);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_SOS);
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
            tvSelectedName.setText(getResources().getString(R.string.brand));

            ArrayList<SOSBO> items = mSFHelper
                    .getSOSList();
            if (items == null) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            List<SOSBO> myList = new ArrayList<>();
            for (SOSBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentID() == id || id == -1 && temp.getIsOwn() == 1)
                    myList.add(temp);
            }

            // set the new list to list view
            MyAdapter mSchedule = new MyAdapter(myList);
            mListView.setAdapter(mSchedule);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * update list based on filter selection
     *
     * @param mFilteredPid         Filtred Least Level Product ID
     * @param mSelectedIdByLevelId Selected product Id's by level ID
     */
    private void updateFiveFilterSelection(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, String mFilterText) {
        ArrayList<SOSBO> items = mSFHelper.getSOSList();
        if (items == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        List<SOSBO> myList = new ArrayList<>();
        if (mFilterText.length() > 0) {
            for (SOSBO temp : items) {
                if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getParentHierarchy().contains("/" + mFilteredPid + "/") && temp.getIsOwn() == 1) {
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
     * Save transaction
     */
    private void saveSOS() {
        try {
            if (mSFHelper
                    .hasData(HomeScreenTwo.MENU_SOS))
                new SaveAsyncTask().execute();
            else
                mBModel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Showing alert dialog while moving back
     */
    private void showAlertOnBackClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                context);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.do_u_want_to_delete_tran));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (!isPreVisit)
                            mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                                .now(DateTimeUtils.TIME));

                        if (totalImgList != null)
                            deleteUnsavedImageFromFolder();

                        Intent intent = new Intent(context, HomeScreenTwo.class);

                        if (isPreVisit)
                            intent.putExtra("PreVisit",true);

                        if (isFromChild)
                            startActivity(intent.putExtra("isStoreMenu", true));
                        else
                            startActivity(intent);

                        ((Activity)context).finish();

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }

    /**
     * Showing alert dialog to denote image availability..
     *
     * @param imageNameStarts Image Name
     * @param imageSrc        Image Path
     */
    private void showFileDeleteAlertWithImage(final String imageNameStarts,
                                              final String imageSrc) {
        final CommonDialog commonDialog = new CommonDialog(context.getApplicationContext(), //Context
                context, //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + 1 + " " + getResources().getString(R.string.word_photocaptured_delete_retake), //Message
                true, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        mBModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        if (dialog != null)
                            dialog.dismiss();
                        Intent intent = new Intent(context,
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                CAMERA_REQUEST_CODE);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }

    /**
     * Open Dialog with Competitor to Get Actual Values and Calculate Total
     * Value
     */
    private void getTotalValue(final int categoryId) {
        mSelectedET = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_salesfundamental_total);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        mParentTotal = dialog.findViewById(R.id.et_total);

        // setting no of characters from configuration
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sosDigits);
        mParentTotal.setFilters(FilterArray);

        mCategoryForDialog.clear();
        // All Brands in Total PopUp
        if (mSFHelper.getSOSList() != null) {
            for (SOSBO sosBO : mSFHelper.getSOSList()) {
                if (sosBO.getParentID() == categoryId) {
                    mCategoryForDialog.add(sosBO);
                }
            }
        }

        ListView listView =  dialog.findViewById(R.id.lv);
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
        dialog.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);


        dialog.findViewById(R.id.btn_done)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mCategoryForDialog.isEmpty()) {
                            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                                SOSBO sosBO = mCategoryForDialog.get(i);
                                if (sosBO.getLocations() != null && sosBO.getLocations().size() > 0) {
                                    sosBO.getLocations().get(mSelectedLocationIndex).setParentTotal(SDUtil
                                            .convertToFloat(mParentTotal.getText()
                                                    .toString())
                                            + "");
                                }

                                if (SDUtil.convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal()) > 0) {

                                    float parentTotal = SDUtil
                                            .convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal());
                                    float mNorm = sosBO.getNorm();
                                    float actual = SDUtil.convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex)
                                            .getActual());

                                    float target = (parentTotal * mNorm) / 100;
                                    float gap = target - actual;
                                    float percentage = 0;
                                    if (parentTotal > 0)
                                        percentage = (actual / parentTotal) * 100;

                                    sosBO.getLocations().get(mSelectedLocationIndex).setTarget(mBModel.formatValue(target));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel
                                            .formatPercent(percentage));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setGap(mBModel.formatValue(-gap));
                                } else {
                                    sosBO.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
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
        float tot = 0;
        if (!mCategoryForDialog.isEmpty()) {
            for (int i = 0; i < mCategoryForDialog.size(); i++) {

                SOSBO sosBO = mCategoryForDialog.get(i);
                tot = tot + SDUtil.convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex).getActual());
            }
            String strTotal = tot + "";
            mParentTotal.setText(strTotal);

        }
    }


    /**
     * Open Dialog with Competitor to Get Actual Values and Calculate Total
     * Value
     */
    private void getCategoryTotalValue(final int categoryId) {
        mSelectedET = null;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_sfcategory_total);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        mCategoryForDialog.clear();
        // All Brands in Total PopUp
        if (mSFHelper.getSOSList() != null) {
            for (SOSBO sosBO : mSFHelper.getSOSList()) {
                if (sosBO.getProductID() == categoryId) {
                    mCategoryForDialog.add(sosBO);
                    break;
                }
            }
        }

        ListView listView = dialog.findViewById(R.id.lv);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = displayMetrics.heightPixels / 3;
        listView.setLayoutParams(params);
        listView.setAdapter(new CategoryDialogAdapter());
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
        dialog.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);


        dialog.findViewById(R.id.btn_done)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isValid = false;
                        try {
                            if (!mCategoryForDialog.isEmpty()) {
                                for (int i = 0; i < mCategoryForDialog.size(); i++) {

                                    SOSBO sosBO = mCategoryForDialog.get(i);

                                    float total = SDUtil.convertToFloat((sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal()));
                                    float actualVal = SDUtil.convertToFloat((sosBO.getLocations().get(mSelectedLocationIndex).getActual()));

                                    if (total >= actualVal) {
                                        isValid = true;

                                        if (SDUtil.convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal()) > 0) {

                                            float parentTotal = SDUtil
                                                    .convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal());
                                            float mNorm = sosBO.getNorm();
                                            float actual = SDUtil.convertToFloat(sosBO.getLocations().get(mSelectedLocationIndex)
                                                    .getActual());

                                            float target = (parentTotal * mNorm) / 100;
                                            float gap = target - actual;
                                            float percentage = 0;
                                            if (parentTotal > 0)
                                                percentage = (actual / parentTotal) * 100;

                                            sosBO.getLocations().get(mSelectedLocationIndex).setTarget(mBModel.formatValue(target));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel
                                                    .formatPercent(percentage));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setGap(mBModel.formatValue(-gap));
                                        } else {
                                            sosBO.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
                                        }
                                    } else {
                                        isValid = false;
                                        break;
                                    }
                                }
                            }
                            if (isValid)
                                calculateTotalValues();
                            else
                                Toast.makeText(context, getResources().
                                        getString(R.string.total_value_less_than_actual_value), Toast.LENGTH_LONG).show();
                            calculateTotalValues();
                            if (dialog != null && isValid) {
                                dialog.dismiss();
                                mListView.invalidateViews();
                                dialog = null;
                            }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                });


        dialog.show();
    }


    /**
     * Show location dialog
     */
    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);
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
        SOSBO mSOS;
        TextView tvBrandName;
        TextView tvActual;
        TextView tvPercentage;
        TextView tvGap;
        EditText etTotal;
        Spinner spnReason;
        ImageButton audit;
        ImageView btnPhoto;
        EditText edt_other_remarks;
        LinearLayout remark_layout,auditLayout;
    }

    /**
     * ListView Adapter
     */
    private class MyAdapter extends ArrayAdapter<SOSBO> {
        private final List<SOSBO> items;

        public MyAdapter(List<SOSBO> mList) {
            super(context, R.layout.row_sos, mList);
            this.items = mList;
        }

        public SOSBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @SuppressLint("SetTextI18n")
        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from((context));
                row = inflater.inflate(R.layout.row_sos, parent, false);

                holder.audit = row
                        .findViewById(R.id.btn_audit);
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

                holder.audit = row
                        .findViewById(R.id.btn_audit);

                holder.auditLayout = row.findViewById(R.id.ll_audit);

                holder.etTotal.setTag(holder);

                holder.remark_layout = row.findViewById(R.id.remark_layout);
                holder.edt_other_remarks = row.findViewById(R.id.edt_other_remarks);

                holder.edt_other_remarks.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        holder.mSOS.getLocations().get(mSelectedLocationIndex).setRemarks(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.audit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() ==
                                IvyConstants.AUDIT_DEFAULT) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit()
                                == IvyConstants.AUDIT_OK) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_NOT_OK);
                            holder.audit.setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit()
                                == IvyConstants.AUDIT_NOT_OK) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex)
                                    .setAudit(IvyConstants.AUDIT_DEFAULT);
                            holder.audit.setImageResource(R.drawable.ic_audit_none);
                        }

                    }
                });

                holder.etTotal.setFocusable(false);

                holder.etTotal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mSFHelper.mSOSCatgPopUpType == 1) {
                            if (dialog != null && !dialog.isShowing()) {
                                dialog.cancel();
                                dialog = null;
                            }
                            // Open dialog
                            if (dialog == null) {
                                mSelectedHolder = (ViewHolder) v.getTag();
                                getCategoryTotalValue(mSelectedHolder.mSOS
                                        .getProductID());

                            }
                        } else if (mSFHelper.mSOSTotalPopUpType == 0) {
                            if (dialog != null && !dialog.isShowing()) {
                                dialog.cancel();
                                dialog = null;
                            }
                            // Open dialog
                            if (dialog == null) {
                                mSelectedHolder = (ViewHolder) v.getTag();
                                getTotalValue(mSelectedHolder.mSOS
                                        .getParentID());
                            }
                        } else {
                            mSelectedHolder = (ViewHolder) v.getTag();
                            Bundle bundle = new Bundle();

                            bundle.putInt("parent_id",
                                    mSelectedHolder.mSOS.getParentID());

                            bundle.putInt("parent_type_id",
                                    0);
                            bundle.putInt("product_id",
                                    mSelectedHolder.mSOS.getProductID());
                            bundle.putInt("flag", ShelfShareHelper.SOS);
                            bundle.putInt("selectedlocation", mSelectedLocationIndex);

                            Intent intent = new Intent(context,SOSMeasureActivity.class);

                            if (isPreVisit)
                                intent.putExtra("PreVisit",true);

                            intent.putExtras(bundle);

                            startActivityForResult(intent, SHARE_SHELF_RESULT_CODE);
                            ((Activity)context).overridePendingTransition(R.anim.zoom_enter,R.anim.hold);
                        }
                    }
                });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBModel.isExternalStorageAvailable()) {
                            mImageName = "SOS_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOS.getProductID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            mSFHelper.mSelectedBrandID = holder.mSOS
                                    .getProductID();
                            String mFirstName = "SOS_"
                                    + mBModel.getRetailerMasterBO()
                                    .getRetailerID() + "_"
                                    + holder.mSOS.getProductID() + "_"
                                    + Commons.now(Commons.DATE);

                            boolean nFilesThere = mBModel
                                    .checkForNFilesInFolder(
                                            FileUtils.photoFolderPath,
                                            1, mFirstName);
                            if (nFilesThere) {

                                showFileDeleteAlertWithImage(mFirstName, holder.mSOS.getLocations().get(mSelectedLocationIndex).getImgName());
                            } else {
                                Intent intent = new Intent(context,
                                        CameraActivity.class);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                String path = FileUtils.photoFolderPath + "/"
                                        + mImageName;
                                intent.putExtra(CameraActivity.PATH, path);
                                startActivityForResult(intent,
                                        CAMERA_REQUEST_CODE);
                                holder.btnPhoto.requestFocus();
                            }

                        } else {
                            Toast.makeText(
                                    context,
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

            holder.mSOS = items.get(position);

            if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_DEFAULT)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit()
                    == IvyConstants.AUDIT_NOT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.tvBrandName.setText(holder.mSOS.getProductName());

            if ("0.0".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal())) {
                holder.etTotal.setText("0");
            } else {
                holder.etTotal.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal());
            }

            String actual = holder.mSOS.getLocations().get(mSelectedLocationIndex).getActual() != null
                    ? holder.mSOS.getLocations().get(mSelectedLocationIndex).getActual() : "0";
            String target = holder.mSOS.getLocations().get(mSelectedLocationIndex).getTarget() != null
                    ? holder.mSOS.getLocations().get(mSelectedLocationIndex).getTarget() : "0";

            if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                float parentTotal = Float.parseFloat(holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal() != null ?
                        holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal() : "0");

                float percentage = 0;
                if (parentTotal > 0)
                    percentage = (Float.parseFloat(actual) / parentTotal) * 100;

                holder.mSOS.getLocations().get(mSelectedLocationIndex).setPercentage(percentage+"");
            }

            String percent = holder.mSOS.getLocations().get(mSelectedLocationIndex).getPercentage() != null
                    ? holder.mSOS.getLocations().get(mSelectedLocationIndex).getPercentage() : "0";

            holder.tvActual.setText(actual + "/" + target);
            holder.tvPercentage.setText(percent + "/" + holder.mSOS.getNorm() + "");
            holder.tvGap.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getGap());

            if (SDUtil.convertToFloat(holder.mSOS.getLocations().get(mSelectedLocationIndex).getGap()) < 0)
                holder.tvGap.setTextColor(Color.RED);
            else if (SDUtil.convertToFloat(holder.mSOS.getLocations().get(mSelectedLocationIndex).getGap()) > 0)
                holder.tvGap.setTextColor(Color.rgb(34, 139, 34));
            else
                holder.tvGap.setTextColor(Color.BLACK);

            holder.spnReason.setAdapter(spinnerAdapter);
            holder.spnReason
                    .setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {

                            ReasonMaster reString = (ReasonMaster) holder.spnReason
                                    .getSelectedItem();

                            holder.mSOS.getLocations().get(mSelectedLocationIndex).setReasonId(SDUtil
                                    .convertToInt(reString.getReasonID()));

                            if (reString.getReasonID().equals("-1")) {
                                holder.remark_layout.setVisibility(View.VISIBLE);
                                holder.edt_other_remarks.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getRemarks());
                            } else {
                                holder.mSOS.getLocations().get(mSelectedLocationIndex).setRemarks("");
                                holder.remark_layout.setVisibility(View.INVISIBLE);

                            }

                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

            if (!holder.mSOS.getLocations().get(mSelectedLocationIndex).getRemarks().equals("")) {
                holder.spnReason.setSelection(getReasonIndex("-1"));
            } else {
                holder.spnReason.setSelection(getReasonIndex(holder.mSOS.getLocations().get(mSelectedLocationIndex)
                        .getReasonId() + ""));
            }
            holder.spnReason.setSelected(true);

            if (((ReasonMaster) holder.spnReason.getSelectedItem()).getReasonID().equals("-1")) {
                holder.remark_layout.setVisibility(View.VISIBLE);
                holder.edt_other_remarks.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getRemarks());

            } else {
                holder.mSOS.getLocations().get(mSelectedLocationIndex).setRemarks("");
                holder.remark_layout.setVisibility(View.INVISIBLE);
            }

            if ((holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName() != null)
                    && (!"".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName()))
                    && (!"null".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName()))) {

                Glide.with(context)
                        .load(FileUtils.photoFolderPath + "/" + holder.mSOS.getLocations().get(mSelectedLocationIndex).getImgName())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_camera)
                        .transform(mBModel.circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_photo_camera));
            }

            return row;
        }

        /**
         * Get the selected reason id, iterate and get position and set in the
         * spinner item
         *
         * @param reasonId Reason Id
         * @return Index of reason Id
         */
        private int getReasonIndex(String reasonId) {
            if (spinnerAdapter.getCount() == 0)
                return 0;
            int len = spinnerAdapter.getCount();
            if (len == 0)
                return 0;
            for (int i = 0; i < len; ++i) {
                ReasonMaster reasonBO = spinnerAdapter.getItem(i);
                if (reasonBO != null) {
                    if (reasonBO.getReasonID().equals(reasonId))
                        return i;
                }
            }
            return -1;
        }
    }

    /**
     * Save data
     */
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mSFHelper
                        .saveSalesFundamentalDetails(HomeScreenTwo.MENU_SOS);
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SOS, true);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(context);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            if (alertDialog != null)
                alertDialog.dismiss();
            if (result == Boolean.TRUE) {
                totalImgList.clear();
                new CommonDialog(context.getApplicationContext(), context,
                        "", getResources().getString(R.string.saved_successfully),
                        false, getResources().getString(R.string.ok),
                        null, new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(context, HomeScreenTwo.class);

                        Bundle extras = ((Activity)context).getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                        }

                        if (isPreVisit)
                            intent.putExtra("PreVisit",true);

                        startActivity(intent);
                        ((Activity)context).finish();
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

                LayoutInflater inflater = LayoutInflater.from(context);

                row = inflater.inflate(
                        R.layout.row_salesfundamental_total_list, parent, false);

                holder.tv = row.findViewById(R.id.tv);
                holder.et = row.findViewById(R.id.et);

                // setting no of characters from configuration
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sosDigits);
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

                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                holder.et.setSelection(s.toString().length());
                            try {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(s.toString());
                            } catch (Exception e) {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
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
            SOSBO brand = mCategoryForDialog.get(position);
            holder.sosBO = brand;

            holder.tv.setText(brand.getProductName());
            holder.et.setText(brand.getLocations().get(mSelectedLocationIndex).getActual());

            return row;
        }

    }

    class CompetitorHolder {
        SOSBO sosBO;
        TextView tv;
        EditText et;
    }


    /**
     * List of Products with Actual Edit Text
     */
    private class CategoryDialogAdapter extends BaseAdapter {

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
            final CategoryHolder holder;
            View row = convertView;
            if (row == null) {
                holder = new CategoryHolder();

                LayoutInflater inflater = LayoutInflater.from(context);

                row = inflater.inflate(
                        R.layout.row_sfcategory_total_list, parent, false);

                holder.tv = row.findViewById(R.id.tv);
                holder.etTotal = row.findViewById(R.id.et_total);
                holder.etActual = row.findViewById(R.id.et_actual);


                // setting no of characters from configuration
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sosDigits);
                holder.etTotal.setFilters(FilterArray);
                holder.etActual.setFilters(FilterArray);

                holder.etActual.setOnTouchListener(new OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = holder.etActual;
                        int inType = holder.etActual.getInputType();
                        holder.etActual.setInputType(InputType.TYPE_NULL);
                        holder.etActual.onTouchEvent(event);
                        holder.etActual.setInputType(inType);
                        if (holder.etActual.getText().length() > 0)
                            holder.etActual.setSelection(holder.etActual.getText().length());
                        if (holder.etActual.getText().toString().equals("0") || holder.etActual.getText().toString().equals("0.0")
                                || holder.etActual.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.etActual.getText().toString().equals("0") || !holder.etActual.getText().toString().equals("0.0")
                                || !holder.etActual.getText().toString().equals("0.00"))
                            sb = holder.etActual.getText().toString();
                        return true;
                    }
                });
                holder.etActual.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.etActual.getText().toString().equals("0") || holder.etActual.getText().toString().equals("0.0")
                                || holder.etActual.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.etActual.getText().toString().equals("0") || !holder.etActual.getText().toString().equals("0.0")
                                || !holder.etActual.getText().toString().equals("0.00"))
                            sb = holder.etActual.getText().toString();

                        if (sb.length() > 0)
                            holder.etActual.setSelection(sb.length());

                        if (!"".equals(s)) {
                            try {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(s.toString());

                            } catch (Exception e) {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(Integer.toString(0));
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                holder.etTotal.setOnTouchListener(new OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSelectedET = holder.etTotal;
                        int inType = holder.etTotal.getInputType();
                        holder.etTotal.setInputType(InputType.TYPE_NULL);
                        holder.etTotal.onTouchEvent(event);
                        holder.etTotal.setInputType(inType);
                        if (holder.etTotal.getText().length() > 0)
                            holder.etTotal.setSelection(holder.etTotal.getText().length());
                        if (holder.etTotal.getText().toString().equals("0") || holder.etTotal.getText().toString().equals("0.0")
                                || holder.etTotal.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.etTotal.getText().toString().equals("0") || !holder.etTotal.getText().toString().equals("0.0")
                                || !holder.etTotal.getText().toString().equals("0.00"))
                            sb = holder.etTotal.getText().toString();
                        return true;
                    }
                });
                holder.etTotal.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.etTotal.getText().toString().equals("0") || holder.etTotal.getText().toString().equals("0.0")
                                || holder.etTotal.getText().toString().equals("0.00"))
                            sb = "";
                        else if (!holder.etTotal.getText().toString().equals("0") || !holder.etTotal.getText().toString().equals("0.0")
                                || !holder.etTotal.getText().toString().equals("0.00"))
                            sb = holder.etTotal.getText().toString();

                        if (sb.length() > 0)
                            holder.etTotal.setSelection(sb.length());

                        if (!"".equals(s)) {
                            try {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setParentTotal(s.toString());
                            } catch (Exception e) {
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setParentTotal(Integer.toString(0));
                                Commons.printException("" + e);
                            }
                        } else {
                            holder.sosBO.getLocations().get(mSelectedLocationIndex).setParentTotal(Integer.toString(0));
                        }
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
                holder = (CategoryHolder) row.getTag();
            }

            if (position == 0 && mSelectedET == null) {
                holder.etTotal.requestFocus();
                mSelectedET = holder.etTotal;
            }
            SOSBO brand = mCategoryForDialog.get(position);
            holder.sosBO = brand;


            holder.tv.setText(brand.getProductName());
            holder.etActual.setText(brand.getLocations().get(mSelectedLocationIndex).getActual());
            holder.etTotal.setText(brand.getLocations().get(mSelectedLocationIndex).getParentTotal());

            return row;
        }

    }

    class CategoryHolder {
        SOSBO sosBO;
        TextView tv;
        EditText etTotal, etActual;
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
                } else
                    s = "0";
                mSelectedET.setText(s);
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
     * update values in view
     *
     * @param val value
     */
    private void updateValue(int val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {
            String s = mSelectedET.getText().toString();
            sb = sb + val;
            if (sb.length() <= mSFHelper.sosDigits) {
                if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s)) {

                    mSelectedET.setText(String.valueOf(val));
                } else {
                    String strVal = mSelectedET.getText()
                            + String.valueOf(val);
                    mSelectedET.setText(strVal);
                }
            } else {
                sb = "";
                Toast.makeText(context, getResources().getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onBackButonClick() {

        if (mSFHelper.hasData(HomeScreenTwo.MENU_SOS)) {
            showBackDialog();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                if (mSFHelper.getLstSOS_PRJSpecific() != null || totalImgList.size() > 0)
                    showAlertOnBackClick();
                else {

                    if (!isPreVisit)
                        mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                            .now(DateTimeUtils.TIME));

                    Intent intent = new Intent(context, HomeScreenTwo.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    if (isFromChild)
                        startActivity(intent.putExtra("isStoreMenu", true));
                    else
                        startActivity(intent);

                    ((Activity)context).finish();

                }
            }
            ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showBackDialog() {
        CommonDialog dialog = new CommonDialog(context, getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {

                if (!isPreVisit)
                    mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));

                Intent intent = new Intent(context, HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit",true);

                if (isFromChild)
                    startActivity(intent.putExtra("isStoreMenu", true));
                else
                    startActivity(intent);

                ((Activity)context).finish();

                ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
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
