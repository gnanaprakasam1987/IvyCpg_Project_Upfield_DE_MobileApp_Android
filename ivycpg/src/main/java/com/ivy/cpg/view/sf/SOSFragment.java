package com.ivy.cpg.view.sf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SODBO;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.ShelfShareCallBackListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SOSFragment extends IvyBaseFragment implements
        BrandDialogInterface {


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
    private ShelfShareDialogFragment dialogFragment = null;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sos, container, false);

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());

        initializeViews(view);

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

        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);

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

        if (mSFHelper.getSOSList() != null)
            calculateTotalValues();

    }

    /**
     * Initialize views
     *
     * @param view Parent view
     */
    private void initializeViews(View view) {

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        if (view != null) {
            mListView = (ListView) view.findViewById(R.id.list);
            mListView.setCacheColorHint(0);
        }

        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

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
                    setScreenTitle(mSFHelper.mSelectedActivityName);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);

        //setting Header Title Fonts
        ((TextView) view.findViewById(R.id.levelName)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hTotal)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hlength)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hlengthacttar)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.hpercent)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.hpercentacttar)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.hGap)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvSelectedName = (TextView) view.findViewById(R.id.levelName);
        Button btn_save = (Button) view.findViewById(R.id.btn_save);
        btn_save.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

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


    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
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
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
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
            reasonMasterBo.setReasonDesc(getActivity().getResources().getString(R.string.select_reason));
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
        }
    }

    /**
     * Delete un wanted images
     */
    private void deleteUnsavedImageFromFolder() {
        for (String imgList : totalImgList) {
            mBModel.deleteFiles(HomeScreenFragment.photoPath,
                    imgList);
        }
    }

    /**
     * Two level filter call
     */
    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            bundle.putString("isFrom", "SOS");
            bundle.putString("filterHeader", mBModel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            bundle.putSerializable("serilizeContent",
                    mBModel.productHelper.getRetailerModuleChildLevelBO());

            if (mBModel.productHelper.getRetailerModuleParentLeveBO() != null
                    && mBModel.productHelper.getRetailerModuleParentLeveBO().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", mBModel.productHelper
                        .getRetailerModuleParentLeveBO().get(0).getPl_productLevel());

                mBModel.productHelper.setPlevelMaster(mBModel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set Fragment class Arguments
            HashMap<String, String> mSelectedFilterMap = new HashMap<>();
            FilterFragment mFragment = new FilterFragment(mSelectedFilterMap);
            mFragment.setArguments(bundle);
            ft.add(R.id.right_drawer, mFragment, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
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
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);
            ft.replace(R.id.right_drawer, mFragment, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        updateFiveFilterSelection(mParentIdList, mSelectedIdByLevelId, mFilterText);
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
            if (!brandFilterText.equals(BRAND))
                menu.findItem(R.id.menu_product_filter).setIcon(
                        R.drawable.ic_action_filter_select);

            if (mBModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
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

            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (mBModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mBModel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_SOS))
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
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                if (mSFHelper.getLstSOS_PRJSpecific() != null || totalImgList.size() > 0)
                    showAlertOnBackClick();
                else {
                    mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                            .now(SDUtil.TIME));
                    if (isFromChild)
                        startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                                .putExtra("isStoreMenu", true));
                    else
                        startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                    getActivity().finish();
                }
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_next) {
            saveSOS();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
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
            tvSelectedName.setText(mFilterText);

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
     * @param mParentIdList        Parent Id List
     * @param mSelectedIdByLevelId Selected product Id's by level ID
     */
    private void updateFiveFilterSelection(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String mFilterText) {
        ArrayList<SOSBO> items = mSFHelper.getSOSList();
        if (items == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        List<SOSBO> myList = new ArrayList<>();
        if (mFilterText.length() > 0) {
            for (LevelBO levelBO : mParentIdList) {
                for (SOSBO temp : items) {
                    if (temp.getParentID() == levelBO.getProductID() && temp.getIsOwn() == 1) {
                        myList.add(temp);
                    }
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
                getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.do_u_want_to_delete_tran));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                                .now(SDUtil.TIME));

                        if (totalImgList != null)
                            deleteUnsavedImageFromFolder();

                        if (isFromChild)
                            startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                                    .putExtra("isStoreMenu", true));
                        else
                            startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                        getActivity().finish();

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
        final CommonDialog commonDialog = new CommonDialog(getActivity().getApplicationContext(), //Context
                getActivity(), //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + 1 + " " + getResources().getString(R.string.word_photocaptured_delete_retake), //Message
                true, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        mBModel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        if (dialog != null)
                            dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String path = HomeScreenFragment.photoPath + "/" + mImageName;
                        intent.putExtra("path", path);
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

    @Override
    public void loadStartVisit() {

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

        mParentTotal = (EditText) dialog.findViewById(R.id.et_total);

        // setting no of characters from configuration
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mSFHelper.sosDigits);
        mParentTotal.setFilters(FilterArray);
        mParentTotal.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) dialog.findViewById(R.id.tvTotal)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((Button) dialog.findViewById(R.id.btn_cancel)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) dialog.findViewById(R.id.btn_done)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mCategoryForDialog.clear();
        // All Brands in Total PopUp
        if (mSFHelper.getSOSList() != null) {
            for (SOSBO sosBO : mSFHelper.getSOSList()) {
                if (sosBO.getParentID() == categoryId) {
                    mCategoryForDialog.add(sosBO);
                }
            }
        }

        ListView listView = (ListView) dialog.findViewById(R.id.lv);
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

                                    sosBO.getLocations().get(mSelectedLocationIndex).setTarget(SDUtil.roundIt(target, 2));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel
                                            .formatPercent(percentage));
                                    sosBO.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(-gap, 2));
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
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_sfcategory_total);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        ((TextView) dialog.findViewById(R.id.dialog_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((Button) dialog.findViewById(R.id.btn_done)).setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

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

        ListView listView = (ListView) dialog.findViewById(R.id.lv);
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

                                    int total = Integer.valueOf(sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal());
                                    int actualVal = Integer.valueOf(sosBO.getLocations().get(mSelectedLocationIndex).getActual());

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

                                            sosBO.getLocations().get(mSelectedLocationIndex).setTarget(SDUtil.roundIt(target, 2));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(mBModel
                                                    .formatPercent(percentage));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setGap(SDUtil.roundIt(-gap, 2));
                                        } else {
                                            sosBO.getLocations().get(mSelectedLocationIndex).setTarget(Integer.toString(0));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setPercentage(Integer.toString(0));
                                            sosBO.getLocations().get(mSelectedLocationIndex).setGap(Integer.toString(0));
                                        }
                                    } else {
                                        isValid = false;
                                    }
                                }
                            }
                            if (isValid)
                                calculateTotalValues();
                            else
                                Toast.makeText(getActivity(), getResources().
                                        getString(R.string.total_value_lesser_actual), Toast.LENGTH_LONG).show();
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


    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    /**
     * Show location dialog
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
        SOSBO mSOS;
        TextView tvBrandName;
        TextView tvNorm;
        TextView tvTarget;
        TextView tvActual;
        TextView tvPercentage;
        TextView tvGap;
        EditText etTotal;
        Spinner spnReason;
        ImageButton audit;
        ImageView btnPhoto;
        EditText edt_other_remarks;
    }

    /**
     * ListView Adapter
     */
    private class MyAdapter extends ArrayAdapter<SOSBO> {
        private final List<SOSBO> items;

        public MyAdapter(List<SOSBO> mList) {
            super(getActivity(), R.layout.row_sos, mList);
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

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());
                row = inflater.inflate(R.layout.row_sos, parent, false);

                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);
                holder.tvBrandName = (TextView) row
                        .findViewById(R.id.tvBrandName);
                holder.tvNorm = (TextView) row
                        .findViewById(R.id.tvNorm);

                holder.tvTarget = (TextView) row
                        .findViewById(R.id.tvTarget);
                holder.tvActual = (TextView) row
                        .findViewById(R.id.tvActual);
                holder.tvPercentage = (TextView) row
                        .findViewById(R.id.tvPercentage);
                holder.tvGap = (TextView) row.findViewById(R.id.tvGap);
                holder.btnPhoto = (ImageView) row
                        .findViewById(R.id.btn_photo);
                holder.spnReason = (Spinner) row
                        .findViewById(R.id.spnReason);

                holder.etTotal = (EditText) row
                        .findViewById(R.id.etTotal);

                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);

                holder.etTotal.setTag(holder);
                holder.edt_other_remarks = (EditText) row.findViewById(R.id.edt_other_remarks);


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

                        if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 2) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex).setAudit(1);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_yes);

                        } else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 1) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex).setAudit(0);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_no);

                        } else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 0) {

                            holder.mSOS.getLocations().get(mSelectedLocationIndex).setAudit(2);
                            holder.audit
                                    .setImageResource(R.drawable.ic_audit_none);
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
                        } else if (dialogFragment == null) {
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
                            dialogFragment = new ShelfShareDialogFragment();
                            dialogFragment.setArguments(bundle);
                            dialogFragment
                                    .setStyle(
                                            DialogFragment.STYLE_NO_TITLE,
                                            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                            dialogFragment.setCancelable(false);
                            dialogFragment
                                    .setOnShelfShareListener(new ShelfShareCallBackListener() {

                                        @Override
                                        public void SOSBOCallBackListener(
                                                List<SOSBO> sosBOList) {

                                            mCategoryForDialog.clear();
                                            mCategoryForDialog
                                                    .addAll(sosBOList);
                                            dialogFragment.dismiss();
                                            dialogFragment = null;
                                            calculateTotalValues();
                                            mListView.invalidateViews();
                                        }

                                        @Override
                                        public void SODDOCallBackListener(List<SODBO> sosBOList) {

                                        }

                                        @Override
                                        public void handleDialogClose() {
                                            dialogFragment.dismiss();
                                            dialogFragment = null;
                                        }
                                    });
                            dialogFragment.show(getChildFragmentManager(),
                                    "Shelf Share");
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
                                            HomeScreenFragment.photoPath,
                                            1, mFirstName);
                            if (nFilesThere) {

                                showFileDeleteAlertWithImage(mFirstName, holder.mSOS.getLocations().get(mSelectedLocationIndex).getImgName());
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra("quality", 40);
                                String path = HomeScreenFragment.photoPath + "/"
                                        + mImageName;
                                intent.putExtra("path", path);
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

                if (mBModel.configurationMasterHelper.IS_TEAMLEAD) {
                    holder.audit.setVisibility(View.VISIBLE);

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

            //typeface
            holder.tvBrandName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.etTotal.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvActual.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvTarget.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvPercentage.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvNorm.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvGap.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


            if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 2)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 1)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.mSOS.getLocations().get(mSelectedLocationIndex).getAudit() == 0)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.tvBrandName.setText(holder.mSOS.getProductName());
            String strNorm = holder.mSOS.getNorm() + "";
            holder.tvNorm.setText(strNorm);

            if ("0.0".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal())) {
                holder.etTotal.setText("0");
            } else {
                holder.etTotal.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getParentTotal());
            }
            holder.tvActual.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getActual());
            holder.tvTarget.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getTarget());
            holder.tvPercentage.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getPercentage());
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
                                holder.edt_other_remarks.setVisibility(View.VISIBLE);
                                holder.edt_other_remarks.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getRemarks());
                            } else {
                                holder.mSOS.getLocations().get(mSelectedLocationIndex).setRemarks("");
                                holder.edt_other_remarks.setVisibility(View.GONE);

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
                holder.edt_other_remarks.setVisibility(View.VISIBLE);
                holder.edt_other_remarks.setText(holder.mSOS.getLocations().get(mSelectedLocationIndex).getRemarks());

            } else {
                holder.mSOS.getLocations().get(mSelectedLocationIndex).setRemarks("");
                holder.edt_other_remarks.setVisibility(View.GONE);
            }

            if ((holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName() != null)
                    && (!"".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName()))
                    && (!"null".equals(holder.mSOS.getLocations().get(mSelectedLocationIndex).getImageName()))) {

                Glide.with(getActivity())
                        .load(HomeScreenFragment.photoPath + "/" + holder.mSOS.getLocations().get(mSelectedLocationIndex).getImgName())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_camera)
                        .transform(mBModel.circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera));
            }
            TypedArray mTypedArray = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(mTypedArray.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(mTypedArray.getColor(R.styleable.MyTextView_listcolor, 0));
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
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SOS);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
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

            if (alertDialog != null)
                alertDialog.dismiss();
            if (result == Boolean.TRUE) {
                totalImgList.clear();
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

                holder.tv = (TextView) row.findViewById(R.id.tv);
                holder.et = (EditText) row.findViewById(R.id.et);

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

            holder.tv.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.et.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());

                row = inflater.inflate(
                        R.layout.row_sfcategory_total_list, parent, false);

                holder.tv = (TextView) row.findViewById(R.id.tv);
                holder.etTotal = (EditText) row.findViewById(R.id.et_total);
                holder.etActual = (EditText) row.findViewById(R.id.et_actual);

                holder.tv.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etTotal.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.etActual.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_total_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.tv_actual_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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

                        if (!"".equals(s)) {
                            int tot = 0;
                            try {
                                if (!holder.etTotal.getText().toString().isEmpty())
                                    tot = Integer.valueOf(holder.sosBO.getLocations().get(mSelectedLocationIndex).getParentTotal());
                                if (tot >= Integer.valueOf(s.toString()))
                                holder.sosBO.getLocations().get(mSelectedLocationIndex).setActual(s.toString());
                                else {
                                    mBModel.showAlert(getResources().
                                            getString(R.string.actual_value_exceeds_total), 0);
                                    holder.etActual.setText(holder.sosBO.getLocations().get(mSelectedLocationIndex).getActual());
                                }
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
                Toast.makeText(getActivity(), getResources().getString(R.string.exceed_limt), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
