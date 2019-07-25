package com.ivy.cpg.view.promotion;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.view.OnSingleClickListener;
import com.ivy.sd.png.view.CircleTransform;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;


public class PromotionTrackingFragment extends IvyBaseFragment implements BrandDialogInterface,
        DataPickerDialogFragment.UpdateDateInterface, FiveLevelFilterCallBack, RemarksDialog.RemarksListener {

    private BusinessModel businessModel;
    private PromotionHelper promotionHelper;
    private String mImageName;

    // Drawer Implementation
    private DrawerLayout mDrawerLayout;
    /* Location adapter related elements */
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private StandardListBO mSelectedStandardListBO;
    /* Keyboard related elements */
    private CardView card_keyboard;
    private InputMethodManager inputManager;
    private EditText QUANTITY;
    private String append = "";
    /* Five filter elements */
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private int mFilteredPid;
    private ArrayList<Integer> mAttributeProducts;
    private String filter_text;
    private boolean isFilterAvailable;
    /* List view group*/
    private ListView listView;
    private ArrayList<PromotionBO> promoList;
    private MyAdapter promotionAdapter;
    private ArrayAdapter<ReasonMaster> reasonAdapter;
    private ArrayAdapter<StandardListBO> mRatingAdapter;
    int selectedposition = -1;

    private Context context;

    private HorizontalScrollView hscrl_spl_filter;

    // To define Accepted or NonAccepted State
    private ArrayAdapter<ConfigureBO> mStateAdapter;
    private int mSelectedPromoStateIndex;
    private ConfigureBO mSelectedPromoStateBO;

    //To Define Trade or Platform Promotions
    private boolean isGroupSelectedFilterAvailable = false;
    private boolean isAcceptedFilterSelected = false;
    private String groupSelectedFilter;

    MyExpAdapter promotionExpAdapter;
    private CircleTransform circleTransform;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotion, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = view.findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        promotionHelper = PromotionHelper.getInstance(getContext());
        circleTransform = CircleTransform.getInstance(getContext());
        isFilterAvailable = businessModel.productHelper.isFilterAvaiable(HomeScreenTwo.MENU_PROMO);
        // Initialize the UI components
        viewInitialization(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Option to enable action bar in fragment
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(businessModel.mSelectedActivityName);
        getActionBar().setElevation(0);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(businessModel.mSelectedActivityName);
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

        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        loadReason();
        loadRating();

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : businessModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (businessModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = businessModel.productHelper.getmSelectedGLobalLocationIndex();
        }
        if (mLocationAdapter.getCount() > 0) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }

        if(promotionHelper.isNonAcceptedPromotionsAvailable()) {
            mStateAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice);
            for (ConfigureBO temp : promotionHelper.getPromoStateFilter())
                mStateAdapter.add(temp);
            if (mStateAdapter.getCount() > 0) {
                mSelectedPromoStateBO = mStateAdapter.getItem(mSelectedPromoStateIndex);
            }
        }
        if (mFilteredPid != 0 || mSelectedIdByLevelId != null || mAttributeProducts != null) {
            updateFromFiveLevelFilter(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, filter_text);
        } else {
            updateBrandText("Brand", -1);
        }
        FiveFilterFragment();


        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void viewInitialization(@NotNull View view) {
        if (view != null) {
            listView = view.findViewById(R.id.list);
            listView.setCacheColorHint(0);
        }

        card_keyboard = view.findViewById(R.id.card_keyboard);

        if (!promotionHelper.SHOW_PROMO_PHOTO) {
            view.findViewById(R.id.tvPhoto).setVisibility(View.GONE);

        } else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tvPhoto).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tvPhoto))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tvPhoto).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!promotionHelper.SHOW_PROMO_REASON) {
            view.findViewById(R.id.tvReason).setVisibility(View.GONE);

        } else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tvReason).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tvReason))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tvReason).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!promotionHelper.SHOW_PROMO_RATING) {
            view.findViewById(R.id.tv_executing_rating).setVisibility(View.GONE);

        } else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_executing_rating).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_executing_rating))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_executing_rating).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (!promotionHelper.SHOW_PROMO_QTY) {
            card_keyboard.setVisibility(View.GONE);
            view.findViewById(R.id.tvPromoQty).setVisibility(View.GONE);

        } else {
            card_keyboard.setVisibility(View.VISIBLE);
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tvPromoQty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tvPromoQty))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tvPromoQty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (promotionHelper.SHOW_PROMO_ANNOUNCER) {
            view.findViewById(R.id.tvAnnouncer).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.tvAnnouncer).setVisibility(View.GONE);
        }
        Button btn_save = view.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                savePromotionData();
            }
        });

        if(promotionHelper.getPromoFilter() != null && promotionHelper.getPromoFilter().size() > 0) {
            loadSpecialFilterView(view);
            scrollToSelectedTabPosition();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String mImagePath = "Promotion/"
                + businessModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + businessModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;
        if (requestCode == businessModel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(businessModel.mSelectedActivityName
                        + "Camera Activity : Successfully Captured.");
                if (promotionHelper.mSelectedPromoID != 0) {
                    promotionHelper
                            .onSaveImageName(mSelectedStandardListBO.getListID(),
                                    promotionHelper.mSelectedPromoID,
                                    mImageName, mImagePath,promotionHelper.mSelectedProductId);
                }
            } else {
                Commons.print(businessModel.mSelectedActivityName
                        + "Camera Activity : Canceled");
            }
        }
        QUANTITY = null;
    }

    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId reason id for which the index need to be get
     * @return position of the reason id
     */
    private int getReasonIndex(String reasonId) {

        int len = reasonAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = reasonAdapter.getItem(i);
            if (s != null && s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    private int getRatingIndex(String id) {
        if (mRatingAdapter != null) {
            int len = mRatingAdapter.getCount();
            if (len == 0) {
                return 0;
            }
            for (int i = 0; i < len; i++) {
                StandardListBO standardListBO = mRatingAdapter.getItem(i);
                if (standardListBO != null && standardListBO.getListID().equals(id)) {
                    return i;
                }
            }
        }
        return 0;

    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        reasonAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        reasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : businessModel.reasonHelper.getReasonList()) {
            if ("PROR".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                reasonAdapter.add(temp);
        }
    }

    private void loadRating() {
        promotionHelper.downloadPromotionRating(getContext().getApplicationContext());
        ArrayList<StandardListBO> ratingList = promotionHelper.getRatingList();
        if (ratingList != null) {
            mRatingAdapter = new ArrayAdapter<>(getActivity(),
                    R.layout.spinner_bluetext_layout, ratingList);
            mRatingAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_promo, menu);
        menu.findItem(R.id.menu_add).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());

    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        // Change color if Filter is selected
        try {
            // If the nav drawer is open, hide action items related to the
            // content view
            boolean drawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (businessModel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(false);


            if (isFilterAvailable) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
            }

            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            if (businessModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (businessModel.productHelper.getInStoreLocation().size() < 2
                        || !promotionHelper.SHOW_PROMO_LOCATION)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
                else menu.findItem(R.id.menu_loc_filter).setVisible(true);
            }
            menu.findItem(R.id.menu_promostate_filter).setVisible(promotionHelper.isNonAcceptedPromotionsAvailable());
            if (drawerOpen)
                menu.clear();
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
        } else if (i == R.id.menu_remarks) {
            FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog remarksDialog = new RemarksDialog(HomeScreenTwo.MENU_PROMO);
            remarksDialog.setCancelable(false);
            remarksDialog.show(ft, HomeScreenTwo.MENU_PROMO);
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_promostate_filter) {
            showAccepted();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);
            FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("FiveFilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("isFrom", "Promo");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set FragmentClass Arguments
            FilterFiveFragment<Object> fragObj = new FilterFiveFragment<>();
            fragObj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragObj, "FiveFilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    @Override
    public void updateBrandText(String filter_text, int mPid) {
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            ArrayList<PromotionBO> items = mSelectedStandardListBO.getPromotionTrackingList();
            if (items == null) {
                businessModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            promoList = new ArrayList<>();
            Set<String> promotionKeys = new HashSet<>();
            boolean isAccepted=false;
            if(mSelectedPromoStateBO != null){
                 isAccepted = mSelectedPromoStateBO.getConfigCode().equals("A");
            }


            // Iterate the List and the items to the ListHolder
            for (PromotionBO temp : items) {
                if (temp.getProductId() > 0 && businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !temp.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (temp.getProductId() == mPid || mPid == -1) {
                    if(isGroupSelectedFilterAvailable && isAcceptedFilterSelected){
                        if(temp.getPromotionGroupName().equals(groupSelectedFilter) && (temp.isAccepted() == isAccepted)){
                            promoList.add(temp);
                            promotionKeys.add(temp.getPromoName());
                        }
                    } else if(isGroupSelectedFilterAvailable && !isAcceptedFilterSelected){
                        if(temp.getPromotionGroupName().equals(groupSelectedFilter)){
                            promoList.add(temp);
                            promotionKeys.add(temp.getPromoName());
                        }
                    } else if(isAcceptedFilterSelected && !isGroupSelectedFilterAvailable){
                        if(temp.isAccepted() == isAccepted){
                            promoList.add(temp);
                            promotionKeys.add(temp.getPromoName());
                        }
                    } else {
                        promoList.add(temp);
                        promotionKeys.add(temp.getPromoName());
                    }
                }
            }
            // set the list values to the adapter
            if(businessModel.configurationMasterHelper.IS_SHOW_EXPLIST_IN_PROMO) {
                promotionExpAdapter = new MyExpAdapter(promoList, promotionKeys);
                listView.setAdapter(promotionExpAdapter);
            } else {
                promotionAdapter = new MyAdapter(promoList);
                listView.setAdapter(promotionAdapter);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Save the Promotion Data in DB And validation is data entered or not
     */
    private void savePromotionData() {
        try {
            // Check Promotion data is or not
            if (promotionHelper.hasPromoData())
                new SavePromotionData().execute();
            else
                businessModel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /* show alert before deleting the existing picture
     * params
     * promotionId - promotion io of the captured image
     * imageNameStarts - starting name of the image*/
    private void showFileDeleteAlert(final String promotionId,
                                     final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                //+ mImageCount
                + "1"
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<PromotionBO> items = promotionHelper
                                .getPromotionList();
                        for (int i = 0; i < items.size(); i++) {
                            PromotionBO promoBO = items.get(i);
                            if (promoBO.getPromoId() == SDUtil.convertToInt(promotionId)) {
                                promoBO.setImageName("");
                            }
                        }
                        businessModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                businessModel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        businessModel.applyAlertDialogTheme(builder);
    }

    @Override
    public void updateGeneralText(String filter_text) {
    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }


    /* Shows in store locations as dialog */
    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
                        updateBrandText("", -1);
                        dialog.dismiss();

                    }
                });

        businessModel.applyAlertDialogTheme(builder);
    }

    /* Shows in store locations as dialog */
    private void showAccepted() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mStateAdapter, mSelectedPromoStateIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedPromoStateIndex = item;
                        mSelectedPromoStateBO = mStateAdapter.getItem(mSelectedPromoStateIndex);
                        isAcceptedFilterSelected = true;
                        updateBrandText("",-1);
                        dialog.dismiss();
                    }
                });

        businessModel.applyAlertDialogTheme(builder);
    }

    /* To get the numbers from the customized keyboard while pressing the number */
    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    /* Append the number to the focused edit text */
    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void updateRemarks(String remark) {
        promoList.get(selectedposition).setRemarks(remark);
        if (promotionAdapter != null)
        promotionAdapter.notifyDataSetChanged();
    }

    class ViewHolder {
        PromotionBO mPromotionMasterBO;
        TextView tv_promoName, tv_promoHeader, tv_product_price;
        EditText etPromoQty;
        CheckBox rbExecuted;
        CheckBox rbAnnounced;
        ImageView btnPhoto;
        Spinner reasonSpin;
        Spinner ratingSpin;
        TextView tvGroupName;
        TextView tvProductName;
        Button mFromDateBTN;
        Button mToDateBTN;
        LinearLayout ll_Rating, ll_price;
        ImageView img_remarks;
        ImageButton imgAttachment;
    }

    private class MyAdapter extends ArrayAdapter<PromotionBO> {
        private final ArrayList<PromotionBO> items;

        public MyAdapter(ArrayList<PromotionBO> myList) {
            super(getActivity(), R.layout.row_promo, myList);
            this.items = myList;
        }

        public PromotionBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();
                row = LayoutInflater.from(getActivity()
                        .getBaseContext()).inflate(R.layout.row_promo, parent, false);

                holder.tv_promoName = row
                        .findViewById(R.id.tvPromoName);
                holder.rbExecuted = row
                        .findViewById(R.id.executed_CB);
                holder.rbAnnounced = row
                        .findViewById(R.id.announced_CB);

                holder.etPromoQty = row.findViewById(R.id.et_promo_qty);


                holder.btnPhoto = row
                        .findViewById(R.id.btn_photo);
                holder.tvGroupName = row.findViewById(R.id.tv_group_name);

                holder.reasonSpin = row
                        .findViewById(R.id.spin_reason);

                //Promotion Enhancement
                holder.tvProductName = row
                        .findViewById(R.id.tv_product_name);
                holder.mFromDateBTN = row.findViewById(R.id.btn_fromdatepicker);
                holder.mToDateBTN = row.findViewById(R.id.btn_todatepicker);

                holder.reasonSpin.setAdapter(reasonAdapter);
                holder.ratingSpin = row.findViewById(R.id.spin_rating);
                holder.ll_Rating = row.findViewById(R.id.ll_rating);
                holder.img_remarks = row.findViewById(R.id.img_feedback);
                if (mRatingAdapter != null)
                    holder.ratingSpin.setAdapter(mRatingAdapter);


                if (!promotionHelper.SHOW_PROMO_PHOTO)
                    row.findViewById(R.id.ll_photo).setVisibility(View.GONE);

                if (promotionHelper.SHOW_PROMO_TYPE) {
                    holder.tvGroupName.setVisibility(View.VISIBLE);
                } else {
                    holder.tvGroupName.setVisibility(View.GONE);
                }
                if (promotionHelper.SHOW_PROMO_RATING) {
                    holder.ll_Rating.setVisibility(View.VISIBLE);
                    try {
                        if (businessModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.executing_rating_label).getTag()) != null) {
                            ((TextView) row.findViewById(R.id.executing_rating_label))
                                    .setText(businessModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.executing_rating_label).getTag()));

                        }
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    holder.ll_Rating.setVisibility(View.GONE);
                }
                if (promotionHelper.SHOW_PROMO_QTY) {
                    holder.etPromoQty.setVisibility(View.VISIBLE);
                } else {
                    holder.etPromoQty.setVisibility(View.GONE);
                }
                if (!promotionHelper.SHOW_PROMO_ANNOUNCER)
                    (row.findViewById(R.id.ll_announced)).setVisibility(View.GONE);
                if (!promotionHelper.SHOW_PROMO_REASON)
                    (row.findViewById(R.id.ll_reason)).setVisibility(View.GONE);
                if (promotionHelper.SHOW_PROMO_FEEDBACK) {
                    holder.img_remarks.setVisibility(View.VISIBLE);
                } else {
                    holder.img_remarks.setVisibility(View.GONE);
                }

                holder.rbExecuted.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                            holder.rbExecuted.setChecked(false);
                            holder.mPromotionMasterBO.setIsExecuted(0);
                            holder.btnPhoto.setEnabled(false);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                            holder.reasonSpin.setEnabled(true);
                            holder.etPromoQty.setEnabled(false);
                            holder.etPromoQty.setText("0");
                            QUANTITY = null;
                        } else {
                            holder.rbExecuted.setChecked(true);
                            holder.mPromotionMasterBO.setIsExecuted(1);
                            holder.btnPhoto.setEnabled(true);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                            holder.reasonSpin.setEnabled(false);
                            holder.reasonSpin.setSelection(0);
                            holder.mPromotionMasterBO.setReasonID("0");
                            holder.etPromoQty.setEnabled(true);
                        }
                    }
                });
                holder.rbAnnounced.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mPromotionMasterBO.getHasAnnouncer() == 1) {
                            holder.rbAnnounced.setChecked(false);
                            holder.mPromotionMasterBO.setHasAnnouncer(0);
                        } else {
                            holder.rbAnnounced.setChecked(true);
                            holder.mPromotionMasterBO.setHasAnnouncer(1);
                        }
                    }
                });

                holder.reasonSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.reasonSpin
                                        .getSelectedItem();

                                holder.mPromotionMasterBO.setReasonID(reString
                                        .getReasonID());
                                holder.mPromotionMasterBO
                                        .setReasonDesc(reString.getReasonDesc());

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.ratingSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        StandardListBO standardListBO = (StandardListBO) holder.ratingSpin.getSelectedItem();
                        holder.mPromotionMasterBO.setRatingId(standardListBO.getListID());
                        holder.mPromotionMasterBO.setRatingDec(standardListBO.getListName());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                holder.etPromoQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!"".equals(s.toString())) {
                            if (s.toString().length() > 0)
                                holder.etPromoQty.setSelection(s.toString().length());
                            int scQty = SDUtil.convertToInt(holder.etPromoQty
                                    .getText().toString());
                            holder.mPromotionMasterBO.setPromoQty(scQty);
                        }


                    }
                });

                holder.etPromoQty.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.etPromoQty;
                        QUANTITY.setTag(holder.mPromotionMasterBO);
                        int inType = holder.etPromoQty.getInputType();
                        holder.etPromoQty.setInputType(InputType.TYPE_NULL);
                        holder.etPromoQty.onTouchEvent(event);
                        holder.etPromoQty.setInputType(inType);
                        holder.etPromoQty.requestFocus();
                        if (holder.etPromoQty.getText().length() > 0)
                            holder.etPromoQty.setSelection(holder.etPromoQty.getText().length());
                        inputManager.hideSoftInputFromWindow(holder.etPromoQty.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FileUtils.isExternalStorageAvailable(10)) {
                            mImageName = "PT_"
                                    + businessModel.getAppDataProvider().getRetailMaster()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    +"_"+holder.mPromotionMasterBO.getProductId()
                                    + "_" + DateTimeUtils.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            promotionHelper.mSelectedPromoID = holder.mPromotionMasterBO.getPromoId();
                            promotionHelper.mSelectedProductId = holder.mPromotionMasterBO.getProductId();
                            String fNameStarts = "PT_"
                                    + businessModel.getAppDataProvider().getRetailMaster()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    +"_"+holder.mPromotionMasterBO.getProductId()
                                    + "_" + DateTimeUtils.now(Commons.DATE);

                            boolean nFilesThere = FileUtils
                                    .checkForNFilesInFolder(
                                            FileUtils.photoFolderPath, 1,
                                            fNameStarts);
                            if (nFilesThere) {
                                showFileDeleteAlert(
                                        holder.mPromotionMasterBO.getPromoId()
                                                + "", fNameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                String path = FileUtils.photoFolderPath + "/"
                                        + mImageName;
                                intent.putExtra(CameraActivity.PATH, path);
                                startActivityForResult(intent,
                                        businessModel.CAMERA_REQUEST_CODE);
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

                //Promotion Enhancement
                if (businessModel.configurationMasterHelper.IS_ENABLE_PROMOTION_SKUNAME) {
                    holder.tvProductName.setVisibility(View.VISIBLE);
                    holder.tvGroupName.setVisibility(View.GONE);
                } else {
                    holder.tvProductName.setVisibility(View.GONE);
                    holder.tvGroupName.setVisibility(View.VISIBLE);
                }

                if (!businessModel.configurationMasterHelper.IS_ENABLE_PROMOTION_DATES) {
                    row.findViewById(R.id.ll_install_date).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_service_date).setVisibility(View.GONE);
                }
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mPromotionMasterBO = items.get(position);

            holder.tv_promoName
                    .setText(holder.mPromotionMasterBO.getPromoName());
            if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                holder.rbExecuted.setChecked(true);
                holder.btnPhoto.setEnabled(true);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                holder.reasonSpin.setEnabled(false);
                holder.reasonSpin.setSelection(0);
                holder.mPromotionMasterBO.setReasonID("0");
            } else {
                holder.rbExecuted.setChecked(false);
                holder.btnPhoto.setEnabled(false);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.reasonSpin.setEnabled(true);
            }
            holder.rbAnnounced.setChecked(holder.mPromotionMasterBO.getHasAnnouncer() == 1);

            if (holder.mPromotionMasterBO.getIsExecuted() == 0) {
                holder.btnPhoto.setImageBitmap(null);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));

            } else if ((holder.mPromotionMasterBO.getImageName() != null)
                    && (!"".equals(holder.mPromotionMasterBO.getImageName()))
                    && (!"null"
                    .equals(holder.mPromotionMasterBO.getImageName()))) {
                File imgFile = new File(FileUtils.photoFolderPath + "/" + holder.mPromotionMasterBO.getImageName());
                Glide.with(getActivity())
                        .load(imgFile.getAbsoluteFile())
                        .asBitmap()
                        .centerCrop()
                        .transform(circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageBitmap(null);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
            }
            holder.etPromoQty.setEnabled(holder.mPromotionMasterBO.getIsExecuted() == 1);

            if ("I".equals(holder.mPromotionMasterBO.getFlag()))
                holder.tv_promoName.setTextColor(getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView).getColor(R.styleable.MyTextView_accentcolor, 0));
            else
                holder.tv_promoName.setTextColor(ContextCompat.getColor(getActivity(),
                        R.color.list_item_text_primary_color));
            holder.reasonSpin
                    .setSelection(getReasonIndex(holder.mPromotionMasterBO
                            .getReasonID()));
            holder.ratingSpin.setSelection(getRatingIndex(holder.mPromotionMasterBO.getRatingId()));

            String promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            try {
                if (businessModel.labelsMasterHelper.applyLabels(holder.tvGroupName.getTag()) != null)
                    promo_groupName = businessModel.labelsMasterHelper
                            .applyLabels(holder.tvGroupName.getTag()) + ": "
                            + holder.mPromotionMasterBO.getGroupName();
                else
                    promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            } catch (Exception e) {
                Commons.printException("" + e);
                promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            }
            holder.tvGroupName.setText(promo_groupName);


            String promoQty = holder.mPromotionMasterBO.getPromoQty() + "";
            holder.etPromoQty.setText(promoQty);

            holder.tvProductName.setText(holder.mPromotionMasterBO.getpName() == null ? "" : holder.mPromotionMasterBO.getpName());
            holder.mFromDateBTN.setText(holder.mPromotionMasterBO.getFromDate() == null ? "" :
                    DateTimeUtils.convertFromServerDateToRequestedFormat(
                            holder.mPromotionMasterBO.getFromDate(), ConfigurationMasterHelper.outDateFormat));
            holder.mToDateBTN.setText(holder.mPromotionMasterBO.getToDate() == null ? "" :
                    DateTimeUtils.convertFromServerDateToRequestedFormat(
                            holder.mPromotionMasterBO.getToDate(), ConfigurationMasterHelper.outDateFormat));

            holder.mFromDateBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedposition = position;
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "");
                    newFragment.setArguments(args);
                    newFragment.show(getChildFragmentManager(), "fromdatePicker");
                }
            });

            holder.mToDateBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedposition = position;
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "");
                    newFragment.setArguments(args);
                    newFragment.show(getChildFragmentManager(), "toPicker");
                }
            });

            if (!StringUtils.isNullOrEmpty(holder.mPromotionMasterBO.getRemarks()))
                holder.img_remarks.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feedback_promo, null));
            else
                holder.img_remarks.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feedback_no_promo, null));

            holder.img_remarks.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedposition = position;
                    FragmentTransaction ft = getActivity()
                            .getSupportFragmentManager().beginTransaction();
                    RemarksDialog dialog = new RemarksDialog(holder.mPromotionMasterBO.getRemarks(), "MENU_PROMO_REMARKS",
                            PromotionTrackingFragment.this);
                    dialog.setCancelable(false);
                    dialog.show(ft, "MENU_PROMO_REMARKS");
                }
            });
            return row;
        }
    }

    private class SavePromotionData extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                promotionHelper.savePromotionDetails(getContext().getApplicationContext());
                promotionHelper.deleteUnusedImages();
                businessModel.saveModuleCompletion(HomeScreenTwo.MENU_PROMO, true);
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
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
            // result is the value returned from doInBackground
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
                            intent.putExtra("IsMoveNextActivity", businessModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
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
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        try {
            this.mFilteredPid = mFilteredPid;
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            this.mAttributeProducts = mAttributeProducts;
            this.filter_text = mFilterText;
            ArrayList<PromotionBO> items = mSelectedStandardListBO.getPromotionTrackingList();
            if (items == null) {
                businessModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            promoList = new ArrayList<>();

            if (mAttributeProducts != null) {
                if (mFilteredPid != 0) {
                    for (PromotionBO productBO : items) {
                        if (productBO.getProductId() > 0 && businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !productBO.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")
                                && mAttributeProducts.contains(productBO.getProductId())) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            promoList.add(productBO);
                        }
                    }
                } else {
                    for (int pid : mAttributeProducts) {
                        for (PromotionBO promoBO : items) {
                            if (promoBO.getProductId() > 0 && businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !promoBO.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (pid == promoBO.getProductId()) {
                                promoList.add(promoBO);
                            }
                        }
                    }
                }
            } else {
                if (mSelectedIdByLevelId.size() == 0 || AppUtils.isMapEmpty(mSelectedIdByLevelId)) {
                    promoList.addAll(items);
                } else {
                    if (mFilterText.length() > 0) {
                        for (PromotionBO promoBO : items) {
                            if (promoBO.getProductId() > 0 && businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !promoBO.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (promoBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                                promoList.add(promoBO);
                            }

                        }
                    } else {
                        for (PromotionBO promoBO : items) {
                            if (promoBO.getProductId() > 0 && businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !promoBO.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            promoList.add(promoBO);
                        }
                    }
                }
            }
            // set the list values to the adapter
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
            promotionAdapter = new MyAdapter(promoList);
            listView.setAdapter(promotionAdapter);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        promotionHelper.clearInstance();
    }

    @Override
    public void updateDate(Date date, String tag) {
        try {
            String paidDate = DateTimeUtils.convertDateObjectToRequestedFormat(
                    date, "yyyy/MM/dd");
            if (selectedposition != -1) {
                if (tag.equals("fromdatePicker")) {
                    promoList.get(selectedposition).setFromDate(paidDate);
                    promoList.get(selectedposition).setToDate("");
                } else {
                    if (promoList.get(selectedposition).getFromDate() != null && promoList.get(selectedposition).getFromDate().length() > 0) {
                        Date fromDate = null;
                        try {
                            fromDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(promoList.get(selectedposition).getFromDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (dateValidation(fromDate, date))
                            promoList.get(selectedposition).setToDate(paidDate);
                        else
                            Toast.makeText(getContext(), getResources().getString(R.string.todate_validation), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.text_select_fromdate), Toast.LENGTH_LONG).show();
                    }
                }
                promotionAdapter.notifyDataSetChanged();
                selectedposition = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean dateValidation(Date fromDate, Date toDate) {
        return toDate.after(fromDate) || toDate.equals(fromDate);
    }

    private void onBackButonClick() {

        if (promotionHelper.hasPromoData()) {
            showBackDialog();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                if (!isPreVisit)
                    businessModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));

                Intent intent = new Intent(getActivity(),HomeScreenTwo.class);
                if (isPreVisit)
                    intent.putExtra("PreVisit",true);

                if (((Activity)context).getIntent().getBooleanExtra("isFromChild", false))
                    startActivity(intent.putExtra("isStoreMenu", true));
                else
                    startActivity(intent);
                ((Activity)context).finish();
            }
            ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showBackDialog() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {

                if (!isPreVisit)
                    businessModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                Intent intent = new Intent(getActivity(),HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit",true);

                if (((Activity)context).getIntent().getBooleanExtra("isFromChild", false))
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

    private class MyExpAdapter extends ArrayAdapter<PromotionBO> {
        private final ArrayList<PromotionBO> items;
        private final Set<String> keySet;

        public MyExpAdapter(ArrayList<PromotionBO> myList, Set<String> keySet) {
            super(getActivity(), R.layout.row_promo, myList);
            this.items = myList;
            this.keySet = keySet;
        }

        public PromotionBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                holder = new ViewHolder();
                row = LayoutInflater.from(getActivity()
                        .getBaseContext()).inflate(R.layout.row_promo, parent, false);

                holder.tv_promoName = row
                        .findViewById(R.id.tvPromoName);
                holder.tv_promoName.setVisibility(View.GONE);

                holder.tv_promoHeader = row
                        .findViewById(R.id.tvPromoHeader);

                holder.imgAttachment = row.findViewById(R.id.imgAttachment);
                holder.imgAttachment.setVisibility(View.GONE);

                holder.tv_product_price = row
                        .findViewById(R.id.price);
                holder.rbExecuted = row
                        .findViewById(R.id.executed_CB);
                holder.rbAnnounced = row
                        .findViewById(R.id.announced_CB);

                holder.etPromoQty = row.findViewById(R.id.et_promo_qty);


                holder.btnPhoto = row
                        .findViewById(R.id.btn_photo);
                holder.tvGroupName = row.findViewById(R.id.tv_group_name);

                holder.reasonSpin = row
                        .findViewById(R.id.spin_reason);

                //Promotion Enhancement
                holder.tvProductName = row
                        .findViewById(R.id.tv_product_name);
                holder.mFromDateBTN = row.findViewById(R.id.btn_fromdatepicker);
                holder.mToDateBTN = row.findViewById(R.id.btn_todatepicker);

                holder.reasonSpin.setAdapter(reasonAdapter);
                holder.ratingSpin = row.findViewById(R.id.spin_rating);
                holder.ll_Rating = row.findViewById(R.id.ll_rating);
                holder.ll_price = row.findViewById(R.id.ll_price);
                holder.img_remarks = row.findViewById(R.id.img_feedback);
                if (mRatingAdapter != null)
                    holder.ratingSpin.setAdapter(mRatingAdapter);


                if (!promotionHelper.SHOW_PROMO_PHOTO)
                    row.findViewById(R.id.ll_photo).setVisibility(View.GONE);

                if (promotionHelper.SHOW_PROMO_TYPE) {
                    holder.tvGroupName.setVisibility(View.VISIBLE);
                } else {
                    holder.tvGroupName.setVisibility(View.GONE);
                }
                if (promotionHelper.SHOW_PROMO_RATING) {
                    holder.ll_Rating.setVisibility(View.VISIBLE);
                    try {
                        if (businessModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.executing_rating_label).getTag()) != null) {
                            ((TextView) row.findViewById(R.id.executing_rating_label))
                                    .setText(businessModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.executing_rating_label).getTag()));

                        }
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    holder.ll_Rating.setVisibility(View.GONE);
                }
                if (promotionHelper.SHOW_PROMO_PRICE) {
                    holder.ll_price.setVisibility(View.VISIBLE);
                    try {
                        if (businessModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.price_label).getTag()) != null) {
                            ((TextView) row.findViewById(R.id.price_label))
                                    .setText(businessModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.price_label).getTag()));

                        }
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    holder.ll_price.setVisibility(View.GONE);
                }

                if (promotionHelper.SHOW_PROMO_QTY) {
                    holder.etPromoQty.setVisibility(View.VISIBLE);
                } else {
                    holder.etPromoQty.setVisibility(View.GONE);
                }
                if (!promotionHelper.SHOW_PROMO_ANNOUNCER)
                    (row.findViewById(R.id.ll_announced)).setVisibility(View.GONE);
                if (!promotionHelper.SHOW_PROMO_REASON)
                    (row.findViewById(R.id.ll_reason)).setVisibility(View.GONE);
                if (promotionHelper.SHOW_PROMO_FEEDBACK) {
                    holder.img_remarks.setVisibility(View.VISIBLE);
                } else {
                    holder.img_remarks.setVisibility(View.GONE);
                }

                holder.rbExecuted.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                            holder.rbExecuted.setChecked(false);
                            holder.mPromotionMasterBO.setIsExecuted(0);
                            holder.btnPhoto.setEnabled(false);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                            holder.reasonSpin.setEnabled(true);
                            holder.etPromoQty.setEnabled(false);
                            holder.etPromoQty.setText("0");
                            QUANTITY = null;
                        } else {
                            holder.rbExecuted.setChecked(true);
                            holder.mPromotionMasterBO.setIsExecuted(1);
                            holder.btnPhoto.setEnabled(true);
                            holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                            holder.reasonSpin.setEnabled(false);
                            holder.reasonSpin.setSelection(0);
                            holder.mPromotionMasterBO.setReasonID("0");
                            holder.etPromoQty.setEnabled(true);
                        }
                    }
                });
                holder.rbAnnounced.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mPromotionMasterBO.getHasAnnouncer() == 1) {
                            holder.rbAnnounced.setChecked(false);
                            holder.mPromotionMasterBO.setHasAnnouncer(0);
                        } else {
                            holder.rbAnnounced.setChecked(true);
                            holder.mPromotionMasterBO.setHasAnnouncer(1);
                        }
                    }
                });

                holder.reasonSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.reasonSpin
                                        .getSelectedItem();

                                holder.mPromotionMasterBO.setReasonID(reString
                                        .getReasonID());
                                holder.mPromotionMasterBO
                                        .setReasonDesc(reString.getReasonDesc());

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.ratingSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        StandardListBO standardListBO = (StandardListBO) holder.ratingSpin.getSelectedItem();
                        holder.mPromotionMasterBO.setRatingId(standardListBO.getListID());
                        holder.mPromotionMasterBO.setRatingDec(standardListBO.getListName());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                holder.etPromoQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!"".equals(s.toString())) {
                            if (s.toString().length() > 0)
                                holder.etPromoQty.setSelection(s.toString().length());
                            int scQty = SDUtil.convertToInt(holder.etPromoQty
                                    .getText().toString());
                            holder.mPromotionMasterBO.setPromoQty(scQty);
                        }


                    }
                });

                holder.etPromoQty.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.etPromoQty;
                        QUANTITY.setTag(holder.mPromotionMasterBO);
                        int inType = holder.etPromoQty.getInputType();
                        holder.etPromoQty.setInputType(InputType.TYPE_NULL);
                        holder.etPromoQty.onTouchEvent(event);
                        holder.etPromoQty.setInputType(inType);
                        holder.etPromoQty.requestFocus();
                        if (holder.etPromoQty.getText().length() > 0)
                            holder.etPromoQty.setSelection(holder.etPromoQty.getText().length());
                        inputManager.hideSoftInputFromWindow(holder.etPromoQty.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.btnPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FileUtils.isExternalStorageAvailable(10)) {
                            mImageName = "PT_"
                                    + businessModel.getAppDataProvider().getRetailMaster()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    +"_"+holder.mPromotionMasterBO.getProductId()
                                    + "_" + DateTimeUtils.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            promotionHelper.mSelectedPromoID = holder.mPromotionMasterBO.getPromoId();
                            promotionHelper.mSelectedProductId = holder.mPromotionMasterBO.getProductId();
                            String fNameStarts = "PT_"
                                    + businessModel.getAppDataProvider().getRetailMaster()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.mPromotionMasterBO.getPromoId()
                                    +"_"+holder.mPromotionMasterBO.getProductId()
                                    + "_" + DateTimeUtils.now(Commons.DATE);

                            boolean nFilesThere = FileUtils
                                    .checkForNFilesInFolder(
                                            FileUtils.photoFolderPath, 1,
                                            fNameStarts);
                            if (nFilesThere) {
                                showFileDeleteAlert(
                                        holder.mPromotionMasterBO.getPromoId()
                                                + "", fNameStarts);
                            } else {
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                String path = FileUtils.photoFolderPath + "/"
                                        + mImageName;
                                intent.putExtra(CameraActivity.PATH, path);
                                startActivityForResult(intent,
                                        businessModel.CAMERA_REQUEST_CODE);
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

                //Promotion Enhancement
                if (businessModel.configurationMasterHelper.IS_ENABLE_PROMOTION_SKUNAME) {
                    holder.tvProductName.setVisibility(View.VISIBLE);
                    holder.tvGroupName.setVisibility(View.GONE);
                } else {
                    holder.tvProductName.setVisibility(View.GONE);
                    holder.tvGroupName.setVisibility(View.VISIBLE);
                }

                if (!businessModel.configurationMasterHelper.IS_ENABLE_PROMOTION_DATES) {
                    row.findViewById(R.id.ll_install_date).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_service_date).setVisibility(View.GONE);
                }
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mPromotionMasterBO = items.get(position);

//            row.findViewById(R.id.ll_attachment).setVisibility(View.GONE);
            if(keySet.contains(holder.mPromotionMasterBO.getPromoName())){
                row.findViewById(R.id.ll_attachment).setVisibility(View.VISIBLE);
                holder.tv_promoHeader.setText(holder.mPromotionMasterBO.getPromoName());
                keySet.remove(holder.mPromotionMasterBO.getPromoName());
            }

            if(holder.mPromotionMasterBO.getPromotionAttchmentList() != null &&
                    holder.mPromotionMasterBO.getPromotionAttchmentList().size() > 0){
                holder.imgAttachment.setVisibility(View.VISIBLE);
            } else {
                holder.imgAttachment.setVisibility(View.GONE);
            }
            //holder.tv_product_price.setText(holder.mPromotionMasterBO.getProductPrice() + "");
            if (holder.mPromotionMasterBO.getIsExecuted() == 1) {
                holder.rbExecuted.setChecked(true);
                holder.btnPhoto.setEnabled(true);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                holder.reasonSpin.setEnabled(false);
                holder.reasonSpin.setSelection(0);
                holder.mPromotionMasterBO.setReasonID("0");
            } else {
                holder.rbExecuted.setChecked(false);
                holder.btnPhoto.setEnabled(false);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.reasonSpin.setEnabled(true);
            }
            holder.rbAnnounced.setChecked(holder.mPromotionMasterBO.getHasAnnouncer() == 1);

            if (holder.mPromotionMasterBO.getIsExecuted() == 0) {
                holder.btnPhoto.setImageBitmap(null);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));

            } else if ((holder.mPromotionMasterBO.getImageName() != null)
                    && (!"".equals(holder.mPromotionMasterBO.getImageName()))
                    && (!"null"
                    .equals(holder.mPromotionMasterBO.getImageName()))) {
                File imgFile = new File(FileUtils.photoFolderPath + "/" + holder.mPromotionMasterBO.getImageName());
                Glide.with(getActivity())
                        .load(imgFile.getAbsoluteFile())
                        .asBitmap()
                        .centerCrop()
                        .transform(circleTransform)
                        .into(new BitmapImageViewTarget(holder.btnPhoto));

            } else {
                holder.btnPhoto.setImageBitmap(null);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
            }
            holder.etPromoQty.setEnabled(holder.mPromotionMasterBO.getIsExecuted() == 1);

//            if ("I".equals(holder.mPromotionMasterBO.getFlag()))
//                holder.tv_promoName.setTextColor(getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView).getColor(R.styleable.MyTextView_accentcolor, 0));
//            else
//                holder.tv_promoName.setTextColor(ContextCompat.getColor(getActivity(),
//                        R.color.list_item_text_primary_color));
            holder.reasonSpin
                    .setSelection(getReasonIndex(holder.mPromotionMasterBO
                            .getReasonID()));
            holder.ratingSpin.setSelection(getRatingIndex(holder.mPromotionMasterBO.getRatingId()));

            String promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            try {
                if (businessModel.labelsMasterHelper.applyLabels(holder.tvGroupName.getTag()) != null)
                    promo_groupName = businessModel.labelsMasterHelper
                            .applyLabels(holder.tvGroupName.getTag()) + ": "
                            + holder.mPromotionMasterBO.getGroupName();
                else
                    promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            } catch (Exception e) {
                Commons.printException("" + e);
                promo_groupName = getString(R.string.group_name) + ": " + holder.mPromotionMasterBO.getGroupName();
            }
            holder.tvGroupName.setText(promo_groupName);


            String promoQty = holder.mPromotionMasterBO.getPromoQty() + "";
            holder.etPromoQty.setText(promoQty);

            holder.tvProductName.setText(holder.mPromotionMasterBO.getpName() == null ? "" : holder.mPromotionMasterBO.getpName());
            holder.mFromDateBTN.setText(holder.mPromotionMasterBO.getFromDate() == null ? "" :
                    DateTimeUtils.convertFromServerDateToRequestedFormat(
                            holder.mPromotionMasterBO.getFromDate(), ConfigurationMasterHelper.outDateFormat));
            holder.mToDateBTN.setText(holder.mPromotionMasterBO.getToDate() == null ? "" :
                    DateTimeUtils.convertFromServerDateToRequestedFormat(
                            holder.mPromotionMasterBO.getToDate(), ConfigurationMasterHelper.outDateFormat));

            holder.mFromDateBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedposition = position;
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "");
                    newFragment.setArguments(args);
                    newFragment.show(getChildFragmentManager(), "fromdatePicker");
                }
            });

            holder.mToDateBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedposition = position;
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "");
                    newFragment.setArguments(args);
                    newFragment.show(getChildFragmentManager(), "toPicker");
                }
            });

            if (!StringUtils.isNullOrEmpty(holder.mPromotionMasterBO.getRemarks()))
                holder.img_remarks.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feedback_promo, null));
            else
                holder.img_remarks.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.feedback_no_promo, null));

            holder.img_remarks.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getActivity()
                            .getSupportFragmentManager().beginTransaction();
                    RemarksDialog dialog = new RemarksDialog(holder.mPromotionMasterBO.getRemarks(), "MENU_PROMO_REMARKS",
                            PromotionTrackingFragment.this);
                    dialog.setCancelable(false);
                    dialog.show(ft, "MENU_PROMO_REMARKS");
                }
            });
            if(promotionHelper.SHOW_PROMO_PRICE){
                holder.ll_price.setVisibility(View.VISIBLE);
                holder.tv_product_price.setText(holder.mPromotionMasterBO.getProductPrice() + "");
            } else {
                holder.ll_price.setVisibility(View.GONE);
            }

            holder.imgAttachment.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    FragmentTransaction ft = getActivity()
                            .getSupportFragmentManager().beginTransaction();
                    PromotionAttachmentDialog dialog = new PromotionAttachmentDialog("MENU_PROMO_REMARKS", holder.mPromotionMasterBO.getPromotionAttchmentList());
                    dialog.setCancelable(false);
                    dialog.show(ft, "MENU_PROMO_ATTACHMENT");
                }
            });
            return row;
        }
    }

    LinearLayout ll_spl_filter, ll_tab_selection;
    private int x, y;
    Vector<ConfigureBO> generalFilter = new Vector<>();

    private void loadSpecialFilterView(View view) {
        hscrl_spl_filter = view.findViewById(R.id.hscrl_spl_filter);
        hscrl_spl_filter.setVisibility(View.VISIBLE);
        ll_spl_filter = view.findViewById(R.id.ll_spl_filter);
        ll_tab_selection = view.findViewById(R.id.ll_tab_selection);

        generalFilter = promotionHelper.getPromoFilter();
        if(generalFilter.size() > 0){
            isGroupSelectedFilterAvailable = true;
        }
        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        int width = (int) (scale / generalFilter.size());

        float den = getContext().getResources().getDisplayMetrics().density;
        float dimen_wd = getResources().getDimension(R.dimen.special_filter_item_width);
        if (width < (int) (dimen_wd * den + 25.5f)) {
            scale = den;
            width = (int) (dimen_wd * scale + 25.5f);
        }

        for (int i = 0; i < generalFilter.size(); i++) {

            ConfigureBO config = generalFilter.get(i);
            TypedArray typeArr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typeArr.getColor(R.styleable.MyTextView_textColor, 0);
            final int indicator_color = typeArr.getColor(R.styleable.MyTextView_accentcolor, 0);
            Button tab = new Button(getActivity());
            tab.setText(config.getMenuName());
            tab.setTag(config.getConfigCode());
            tab.setGravity(Gravity.CENTER);
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tab.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
            tab.setWidth(width);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupSelectedFilter = view.getTag().toString();
                    updateBrandText("", -1);
                    selectTab(view.getTag());
                }
            });


            ll_spl_filter.addView(tab);

            if (i == 0) {
                x = tab.getLeft();
                y = tab.getTop();
            }

            Button tv_selection_identifier = new Button(getActivity());
            tv_selection_identifier.setTag(config.getConfigCode() + config.getMenuName());
            tv_selection_identifier.setWidth(width);
            tv_selection_identifier.setBackgroundColor(indicator_color);
            if (i == 0) {
                tv_selection_identifier.setVisibility(View.VISIBLE);
                groupSelectedFilter = generalFilter.get(i).getConfigCode();
                updateBrandText("", -1);
            } else {
                tv_selection_identifier.setVisibility(View.GONE);
            }

            ll_tab_selection.addView(tv_selection_identifier);
        }


    }

    private void selectTab(Object tag) {
        for (ConfigureBO config : promotionHelper.getPromoFilter()) {
            View view = getView().findViewWithTag(config.getConfigCode());
            View view1 = getView().findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName());
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.VISIBLE);
                }


            } else {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName());
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.INVISIBLE);
                }

            }
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    public void scrollToSelectedTabPosition() {
        if (hscrl_spl_filter != null) {
            hscrl_spl_filter.scrollTo(x, y);
        }
        selectTab(generalFilter.get(0).getConfigCode());
    }
}
