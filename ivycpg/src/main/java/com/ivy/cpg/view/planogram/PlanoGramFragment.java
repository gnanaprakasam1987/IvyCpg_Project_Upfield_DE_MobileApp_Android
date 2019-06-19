package com.ivy.cpg.view.planogram;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
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
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import me.relex.circleindicator.CircleIndicator;

public class PlanoGramFragment extends IvyBaseFragment implements
        OnClickListener, BrandDialogInterface,FiveLevelFilterCallBack {

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String BRAND = "Brand";
    private int filterId = -1;
    private int mSelectedLocationIndex;
    private int mSelectedLocationId = -1;
    private int selectedCategory, productId;
    private String locationName = "";
    private String photoNamePath;
    private String calledBy = "0";
    private boolean isDialogPopup;
    private int mSelectedBrandID = 0;
    private String mBrandButton;
    private String imageFileName = "";
    private String menuCode = "";

    private Vector<PlanoGramBO> mPlanoGramList;
    private ArrayAdapter<StandardListBO> locationAdapter;
    private ArrayAdapter<ReasonMaster> reasonAdapter;
    private ArrayAdapter<ReasonMaster> percentageAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    private PlanoGramHelper mPlanoGramHelper;
    private BusinessModel mBModel;
    private PlanogramAdapter planoAdapter;

    private DrawerLayout mDrawerLayout;
    private RecyclerView plano_recycler;
    ActionBar actionBar;

    private Context context;

    private boolean isPreVisit = false;

    public enum PlanogramCheck {
        NOTCOMPLETED,
        ADHERENCEFAILED,
        COMPLETED
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) context.getApplicationContext();
        mBModel.setContext(getActivity());
        mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_planogram, container, false);

        try {
            mBModel = (BusinessModel) context.getApplicationContext();
            mBModel.setContext(((Activity)context));

            // download data for planoGram
            mPlanoGramList = mPlanoGramHelper.getPlanogramMaster();

            final Intent i = ((Activity)context).getIntent();
            calledBy = i.getStringExtra("from");
            menuCode = i.getStringExtra("CurrentActivityCode");
            isDialogPopup = false;
            photoNamePath = FileUtils.photoFolderPath + "/";

            isPreVisit = i.getBooleanExtra("PreVisit",false);

            loadReason();
            //mSelectedLocationId = Integer.parseInt(mPlanoGramHelper.getInStoreLocation().get(0).getListID());
            if (savedInstanceState != null) {
                filterId = savedInstanceState.getInt("id");
            }

            initializeViews(view);

            //load locations
            locationAdapter = new ArrayAdapter<>(context,
                    android.R.layout.select_dialog_singlechoice);
            for (StandardListBO temp : mPlanoGramHelper.getInStoreLocation())
                locationAdapter.add(temp);

            if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
                StandardListBO selectedId = locationAdapter
                        .getItem(mBModel.productHelper.getmSelectedGLobalLocationIndex());
                mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
                if (selectedId != null) {
                    mSelectedLocationId = SDUtil.convertToInt(selectedId
                            .getListID());
                    locationName = " -" + selectedId.getListName();
                }
                if (actionBar != null) {
                    actionBar.setTitle(mPlanoGramHelper.mSelectedActivityName
                            + locationName);
                }
            } else if (mPlanoGramHelper.getInStoreLocation().size() == 1){
                mSelectedLocationIndex = 0;
                StandardListBO selectedId = locationAdapter.getItem(mSelectedLocationIndex);
                if (selectedId != null)
                    mSelectedLocationId = Integer.parseInt(selectedId.getListID());
            }

            // load data
            if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM) {
                updateBrandText(BRAND, 0);
            } else {
                updateBrandText(BRAND, filterId);
            }

            mDrawerLayout.closeDrawer(GravityCompat.END);
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return view;
    }

    private void initializeViews(View view) {

        plano_recycler =  view.findViewById(R.id.plano_recycler);
        plano_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        Button btnSave =  view.findViewById(R.id.saveButton);
        btnSave.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setOnClickListener(this);
        mDrawerLayout =  view.findViewById(R.id.drawer_layout);
        FrameLayout drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout
                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (actionBar != null)
                    setScreenTitle(mPlanoGramHelper.mSelectedActivityName);

                ((Activity)context).invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {

                if (actionBar != null)
                    setScreenTitle(getResources().getString(R.string.filter));

                ((Activity)context).invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        reasonAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        reasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        percentageAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        percentageAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if ("POG".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory())) {
                reasonAdapter.add(temp);

            }
        }


        ArrayList<ReasonMaster> percentageList=mPlanoGramHelper.downloadPercentages(getActivity());
                percentageAdapter.addAll(percentageList);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(mPlanoGramHelper.mSelectedActivityName);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mBModel = (BusinessModel) getActivity().getApplicationContext();
            mBModel.setContext(getActivity());

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        //if statement to make sure the alert is displayed only for the first time
        if(mPlanoGramHelper == null){
            mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
        }
        if (mPlanoGramHelper.getInStoreLocation().size() != 1 && !isDialogPopup) {
            if (!mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                showLocationFilterAlert();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", filterId);
    }


    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    private Bitmap decodeFile(File f) {
        int IMAGE_MAX_SIZE = 500;
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return b;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.saveButton) {
            String result = checkDataForSave();
            if (result.equals(PlanogramCheck.NOTCOMPLETED.toString())) {
                mBModel.showAlert(
                        getResources().getString(
                                R.string.fill_photo_and_adherence), 0);
            } else if (result.equals(PlanogramCheck.ADHERENCEFAILED.toString())) {
                mBModel.showAlert(
                        getResources().getString(
                                R.string.please_fill_adherence), 0);
            } else
                nextButtonClick();
        }
    }

    /**
     * method calls while after capture the photo and return to this page and
     * save the image
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1 && mPlanoGramList != null) {
                for (PlanoGramBO planBo : mPlanoGramList) {
                    if (planBo.getPid() == productId && planBo.getLocationID() == mSelectedLocationId) {
//                        planBo.setPlanogramCameraImgName(imageFileName);
                        if(planBo.getPlanoGramCameraImgList().size()>0)
                            planBo.getPlanoGramCameraImgList().add(imageFileName);
                        else{
                            ArrayList<String> strings = new ArrayList<>();
                            strings.add(imageFileName);
                            planBo.setPlanoGramCameraImgList(strings);
                        }
                    }
                }
                planoAdapter.notifyDataSetChanged();
            } else if (mPlanoGramList != null) {
                for (PlanoGramBO planBo : mPlanoGramList) {
                    if (planBo.getPid() == productId) {
                        planBo.setAdherence(null);
                    }
                }
            }
        }
    }

    /**
     * Camera call
     */
    private void takePhoto(PlanoGramBO planoGramBO) {
        if (mBModel.isExternalStorageAvailable()) {

            if ("1".equals(calledBy)) {
                imageFileName = "VPL_" + "0" + "_" + selectedCategory + "_"
                        + mSelectedLocationId + "_" + Commons.now(Commons.DATE_TIME)
                        + "_img.jpg";

            } else {
                imageFileName = "PL_" + planoGramBO.getPid()
                        + "_" + selectedCategory + "_" + mSelectedLocationId + "_"
                        + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            }

            String path = photoNamePath + imageFileName;

            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(CameraActivity.QUALITY, 40);
                intent.putExtra(CameraActivity.PATH, path);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

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


    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        try {

            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            mBrandButton = mFilterText;
            filterId = bid;
            selectedCategory = bid;

            mPlanoGramList = new Vector<>();
            Vector<PlanoGramBO> items = mPlanoGramHelper.getPlanogramMaster();

            for (final PlanoGramBO planoGramBO : items) {
                if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM
                        && planoGramBO.getLocationID() == mSelectedLocationId) {
                    if (bid == planoGramBO.getPid() || (bid == 0 && "Brand".equals(mFilterText)))
                        mPlanoGramList.add(planoGramBO);
                } else if (mSelectedLocationId == -1) {
                    if (bid == planoGramBO.getPid() || (bid == -1 && "Brand".equals(mFilterText)))
                        mPlanoGramList.add(planoGramBO);
                }
            }

            refreshList();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public void updateGeneralText(String mFilterText) {
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();
        updateBrandText(BRAND, 0);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        Vector<PlanoGramBO> items = mPlanoGramHelper.getPlanogramMaster();
        this.productId = mProductId;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mPlanoGramList = new Vector<>();
        if (items == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mAttributeProducts != null && mProductId!=0) {//Both Product and attribute filter selected
                for (PlanoGramBO planoGramBO : items) {
                    if (planoGramBO.getParentHierarchy() != null && planoGramBO.getParentHierarchy().contains("/"+mProductId+"/")) {
                        if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM && planoGramBO.getLocationID() == mSelectedLocationId) {
                            if ((planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0)
                                    && mAttributeProducts.contains(planoGramBO.getPid())) {
                                mPlanoGramList.add(planoGramBO);
                            }
                        } else if ((planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1)
                                && mAttributeProducts.contains(planoGramBO.getPid())) {
                            mPlanoGramList.add(planoGramBO);
                        }
                    }
            }
        } else if (mAttributeProducts == null && mProductId!=0) {// product filter alone selected
                for (PlanoGramBO planoGramBO : items) {
                    if (planoGramBO.getParentHierarchy() != null && planoGramBO.getParentHierarchy().contains("/"+mProductId+"/")) {
                        if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM && planoGramBO.getLocationID() == mSelectedLocationId) {
                            if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                                mPlanoGramList.add(planoGramBO);
                            }
                        } else if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                            mPlanoGramList.add(planoGramBO);
                        }
                    }
            }
        } else if (mAttributeProducts != null && mProductId!=0) {// Attribute filter alone selected
            for (int pid : mAttributeProducts) {
                for (PlanoGramBO planoGramBO : items) {
                    if (pid == planoGramBO.getPid()) {
                        if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM && planoGramBO.getLocationID() == mSelectedLocationId) {
                            if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                                mPlanoGramList.add(planoGramBO);
                            }
                        } else if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                            mPlanoGramList.add(planoGramBO);
                        }
                    }
                }
            }
        } else if (mAttributeProducts == null && mProductId!=0) {
            for (PlanoGramBO planoGramBO : items) {
                if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM && planoGramBO.getLocationID() == mSelectedLocationId) {
                    if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                        mPlanoGramList.add(planoGramBO);
                    }
                } else if (planoGramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                    mPlanoGramList.add(planoGramBO);
                }
            }
        } else if (mAttributeProducts == null && productId==0){
            for (final PlanoGramBO planoGramBO : items) {
                if (mPlanoGramHelper.IS_LOCATION_WISE_PLANOGRAM
                        && planoGramBO.getLocationID() == mSelectedLocationId) {
                    mPlanoGramList.add(planoGramBO);
                }
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

        refreshList();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_photo).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);

        // If the nav drawer is open, hide action items related to the content
        // view
        try {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            menu.findItem(R.id.menu_next).setVisible(false);
            menu.findItem(R.id.menu_location_filter).setVisible(true);

            if (mPlanoGramHelper.getInStoreLocation().size() == 1) {
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (!menuCode.equals("MENU_PLANOGRAM_CS") && mBModel.productHelper.isFilterAvaiable(menuCode)) {
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

            menu.findItem(R.id.menu_reason).setVisible(mBModel.configurationMasterHelper.floating_np_reason_photo);

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
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_location_filter) {
            showLocationFilterAlert();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog dialog = new RemarksDialog("MENU_PLANOGRAM");
            dialog.setCancelable(false);
            dialog.show(ft, "MENU_PLANOGRAM");
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_reason) {
            mBModel.reasonHelper.downloadNpReason(mBModel.retailerMasterBO.getRetailerID(), menuCode);
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mBModel.reasonHelper.isNpReasonPhotoAvaiable(mBModel.retailerMasterBO.getRetailerID(), menuCode)) {

                        if (!isPreVisit)
                            mBModel.saveModuleCompletion(menuCode, true);

                        Intent intent = new Intent(getActivity(),HomeScreenTwo.class);

                        if (isPreVisit)
                            intent.putExtra("PreVisit",true);

                        startActivity(intent);
                        ((Activity)context).finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", menuCode);
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextButtonClick() {
        try {
            if (isReasonSelected()) {
                if(isPercentageSelected()) {
                    new SaveAsyncTask().execute();
                }
                else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.select_percentage),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.select_reason),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mPlanoGramHelper.savePlanoGram(getContext().getApplicationContext());
                if(menuCode.equalsIgnoreCase("MENU_VAN_PLANOGRAM"))
                    mBModel.saveModuleCompletion("MENU_VAN_PLANOGRAM", false);
                else if (calledBy != null && !"3".equals(calledBy))
                    mBModel.saveModuleCompletion(HomeScreenTwo.MENU_PLANOGRAM, true);
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
                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                        "", getResources().getString(R.string.saved_successfully),
                        false, getActivity().getResources().getString(R.string.ok),
                        null, new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        if ("1".equals(calledBy)) {
                            startActivity(new Intent(getActivity(),
                                    HomeScreenActivity.class).putExtra("menuCode", "MENU_LOAD_MANAGEMENT"));
                            getActivity().finish();
                        }
                        if ("2".equals(calledBy)) {
                            if (!isPreVisit)
                                mBModel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(DateTimeUtils
                                            .now(DateTimeUtils.TIME));
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                            Bundle extras = getActivity().getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            if (isPreVisit)
                                intent.putExtra("PreVisit",true);

                            startActivity(intent);
                            getActivity().finish();
                        }
                        if ("3".equals(calledBy)) {
                            startActivity(new Intent(getActivity(),
                                    HomeScreenActivity.class).putExtra("menuCode", "MENU_COUNTER"));
                            getActivity().finish();
                        }
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();

            }

        }

    }

    private boolean isReasonSelected() {
        for (final PlanoGramBO planoGramBO : mPlanoGramList) {
            if (planoGramBO.getAdherence() != null && "0".equals(planoGramBO.getAdherence())
                    && "0".equals(planoGramBO.getReasonID())) {
                return false;
            }
        }
        return true;
    }
    private boolean isPercentageSelected() {
        for (final PlanoGramBO planoGramBO : mPlanoGramList) {
            if (planoGramBO.getAdherence() != null && "-1".equals(planoGramBO.getAdherence())
                    && "0".equals(planoGramBO.getPercentageId())) {
                return false;
            }
        }
        return true;
    }

    /**
     *check whether the adherence is filled or not
     * check whether image is captured or not
     * 0 - no adherence found
     * 1 - no image found
     * 2 - Success
     */
    private String checkDataForSave() {
        int cnt = 0;
        for (final PlanoGramBO planoGramBO : mPlanoGramList) {
            if (planoGramBO.getPlanoGramCameraImgList().size() > 0) {
                cnt++;
                if (planoGramBO.getAdherence() == null) {
                    return PlanogramCheck.ADHERENCEFAILED.toString();
                }
            }
        }
        return (cnt == 0) ? PlanogramCheck.NOTCOMPLETED.toString() : PlanogramCheck.COMPLETED.toString();
    }

    /**
     * Open the Image in Photo Gallery while onClick
     *
     * @param fileName File name
     */
    private void openImage(String fileName) {
        if (fileName.trim().length() > 0) {
            try {
                Uri path;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(fileName));
                } else {
                    path = Uri.fromFile(new File(fileName));
                }

                intent.setDataAndType(path, "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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

    private void setCameraImage(PlanoGramBO planoGramBO) {
        try {
            takePhoto(planoGramBO);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Show Location wise Filter
     */
    private void showLocationFilterAlert() {
        AlertDialog.Builder builder;
        isDialogPopup = true;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(locationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        StandardListBO selectedId = locationAdapter
                                .getItem(item);
                        mSelectedLocationIndex = item;
                        if (selectedId != null) {
                            mSelectedLocationId = SDUtil.convertToInt(selectedId
                                    .getListID());
                            locationName = " -" + selectedId.getListName();
                        }
                        ActionBar actionBar = ((AppCompatActivity) getActivity())
                                .getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setTitle(mPlanoGramHelper.mSelectedActivityName
                                    + locationName);
                        }
                        dialog.dismiss();
                        updateBrandText(BRAND, 0);

                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }

    /**
     * Get index for given reason id
     *
     * @param reasonId Reason Id
     * @return Index
     */
    private int getStatusIndex(String reasonId) {
        if (reasonAdapter.getCount() == 0)
            return 0;
        int len = reasonAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster mReasonBO = reasonAdapter.getItem(i);
            if (mReasonBO != null) {
                if (mReasonBO.getReasonID().equals(reasonId))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Get index for given reason id
     *
     * @param reasonId Reason Id
     * @return Index
     */
    private int getPercentageIndex(String reasonId) {
        if (percentageAdapter.getCount() == 0)
            return 0;
        int len = percentageAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster mReasonBO = percentageAdapter.getItem(i);
            if (mReasonBO != null) {
                if (mReasonBO.getReasonID().equals(reasonId))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Show file delete alert
     */
    public void showFileDeleteAlert(final String imageName, final ArrayList<String> stringArrayList) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mBModel.deleteFiles(photoNamePath, imageName);

                        if(stringArrayList.contains(imageName)){
                            stringArrayList.remove(stringArrayList.indexOf(imageName));
                            mPlanoGramHelper
                                    .deleteImageName(getContext().getApplicationContext(), imageName);
                        }

                        planoAdapter.notifyDataSetChanged();

                        Intent intent = new Intent(getActivity(),CameraActivity.class);
                        String _path = FileUtils.photoFolderPath + "/" + imageName;
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        intent.putExtra(CameraActivity.PATH, _path);
                        intent.putExtra(CameraActivity.ISSAVEREQUIRED,
                                false);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);

                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }

    /**
     * PlanoGram adapter
     */
    class PlanogramAdapter extends RecyclerView.Adapter<PlanogramAdapter.ViewHolder> {

        private Vector<PlanoGramBO> items;

        private PlanogramAdapter(Vector<PlanoGramBO> items) {
            this.items = items;
        }

        void setListData(Vector<PlanoGramBO> listData){
            this.items = listData;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.planogram_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            photoNamePath = FileUtils.photoFolderPath + "/";
            holder.planoObj = items.get(position);
            holder.productName.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.tvAdherence.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.text_clickToTakePicture.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.rdYes.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.rdNo.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.rdPartial.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.productName.setText(holder.planoObj.getProductName());

            if ("0".equals(holder.planoObj.getAdherence())) {
                holder.layout_reason.setVisibility(View.VISIBLE);
                holder.adherence_reason.setVisibility(View.VISIBLE);
                holder.spinner_percentage.setVisibility(View.GONE);
            } else if("-1".equals(holder.planoObj.getAdherence())) {
                holder.layout_reason.setVisibility(View.VISIBLE);
                holder.adherence_reason.setVisibility(View.GONE);
                holder.spinner_percentage.setVisibility(View.VISIBLE);
            }else {
                holder.layout_reason.setVisibility(View.GONE);
            }
            holder.setImageFromServer();
            holder.setImageFromCamera();
            holder.rdYes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        holder.rdNo.setChecked(false);
                        holder.rdYes.setButtonDrawable(R.drawable.ic_tick_enable);
                        holder.rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                        holder.rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                        holder.planoObj.setAdherence("1");
                        holder.layout_reason.setVisibility(View.GONE);
                        holder.planoObj.setReasonID("0");
                        holder.adherence_reason.setSelection(0);

                        holder.rdPartial.setChecked(false);
                        holder.rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdPartial.setButtonDrawable(R.drawable.ic_partial_disabled);
                        holder.planoObj.setPercentageId("0");
                        holder.spinner_percentage.setSelection(0);
                    }
                }
            });

            holder.rdNo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        holder.rdYes.setChecked(false);
                        holder.rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                        holder.rdNo.setButtonDrawable(R.drawable.ic_cross_enable);
                        holder.rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_no_red));
                        holder.planoObj.setAdherence("0");
                        holder.layout_reason.setVisibility(View.VISIBLE);

                        holder.rdPartial.setChecked(false);
                        holder.rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdPartial.setButtonDrawable(R.drawable.ic_partial_disabled);
                        holder.planoObj.setPercentageId("0");
                        holder.spinner_percentage.setSelection(0);
                        holder.adherence_reason.setVisibility(View.VISIBLE);
                        holder.spinner_percentage.setVisibility(View.GONE);
                    }
                }
            });

            holder.rdPartial.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if(isChecked) {
                        holder.rdYes.setChecked(false);
                        holder.rdNo.setChecked(false);

                        holder.rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                        holder.rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                        holder.rdPartial.setButtonDrawable(R.drawable.ic_partial_enabled);

                        holder.rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_no_red));

                        holder.planoObj.setAdherence("-1");
                        holder.layout_reason.setVisibility(View.VISIBLE);
                        holder.adherence_reason.setVisibility(View.GONE);
                        holder.spinner_percentage.setVisibility(View.VISIBLE);
                    }

                }
            });

            holder.adherence_reason.setSelection(getStatusIndex(holder.planoObj
                    .getReasonID()));
            holder.adherence_reason
                    .setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {
                            ReasonMaster reString = (ReasonMaster) arg0
                                    .getSelectedItem();
                            holder.planoObj.setReasonID(reString.getReasonID());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });
            holder.spinner_percentage.setSelection(getPercentageIndex(holder.planoObj.getPercentageId()));
            holder.spinner_percentage.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ReasonMaster reString = (ReasonMaster) adapterView
                            .getSelectedItem();
                    holder.planoObj.setPercentageId(reString.getReasonID());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            holder.imgFromServer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.planoObj.getImageName() != null) {
                        File imgFile = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + mBModel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.PLANOGRAM
                                + "/"
                                + holder.planoObj.getImageName());
                        if (imgFile.exists() && !"".equals(holder.planoObj.getImageName())) {
                            try {
                                openImage(imgFile.getAbsolutePath());
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

            holder.layout_cameraImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoNamePath = FileUtils.photoFolderPath + "/";
                    if ("1".equals(calledBy)) {
                        imageFileName = "VPL_" + "0" + "_" + selectedCategory + "_"
                                + mSelectedLocationId + "_" + Commons.now(Commons.DATE) + "_img.jpg";

                    } else {
                        imageFileName = "PL_" + holder.planoObj.getPid()
                                + "_" + selectedCategory + "_" + mSelectedLocationId + "_"
                                + Commons.now(Commons.DATE_TIME) + "_img.jpg";
                    }

                    if (holder.planoObj.getPlanoGramCameraImgList().size() > 0
                            && mBModel.configurationMasterHelper.PLANO_IMG_COUNT == holder.planoObj.getPlanoGramCameraImgList().size()) {
                        mBModel.showAlert(
                                getResources().getString(
                                        R.string.planogram_image_count_reached), 0);
                    } else {

                        if(holder.planoObj.isLoadedFromLastVisitTran()) {
                            // Clearing last visit tran images as user is taking photo now..
                            holder.planoObj.getPlanoGramCameraImgList().clear();
                            holder.planoObj.setLoadedFromLastVisitTran(false);
                        }

                        productId = holder.planoObj.getPid();
                        setCameraImage(holder.planoObj);
                    }
                }
            });

            if(holder.planoObj.isLoadedFromLastVisitTran()){
                photoNamePath=getActivity().getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + mBModel.userMasterHelper.getUserMasterBO()
                        .getUserid()
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.PLANOGRAM
                        + "/";
            }
            GalleryAdapter adapter =
                    new GalleryAdapter(getActivity(),holder.planoObj.getPlanoGramCameraImgList(),photoNamePath);
            // Set the custom click listener on the adapter directly
            adapter.setOnItemClickListener(new ItemClickSupport() {
                @Override
                public void onItemClicked(String path,boolean isDelete) {
                    // inner view pager page was clicked
                    try{
                        if(!isDelete) {
                            File imgFile = new File(path);
                            if (imgFile.exists())
                                openImage(imgFile.getAbsolutePath());
                            else
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.unloadimage),
                                        Toast.LENGTH_SHORT).show();
                        }else{
                            showFileDeleteAlert(path,holder.planoObj.getPlanoGramCameraImgList());
                        }
                    }catch(Exception e){
                        Commons.printException(e);
                    }
                }
            });
            // Set the adapter on the view pager
            holder.imageViewPager.setAdapter(adapter);

            holder.indicator.setViewPager(holder.imageViewPager);

            if (holder.planoObj.getAudit() == IvyConstants.AUDIT_DEFAULT)
                holder.audit.setImageResource(R.drawable.ic_audit_none);
            else if (holder.planoObj.getAudit() == IvyConstants.AUDIT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_yes);
            else if (holder.planoObj.getAudit() == IvyConstants.AUDIT_NOT_OK)
                holder.audit.setImageResource(R.drawable.ic_audit_no);

            holder.audit.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (holder.planoObj.getAudit() == IvyConstants.AUDIT_DEFAULT) {

                        holder.planoObj.setAudit(IvyConstants.AUDIT_OK);
                        holder.audit.setImageResource(R.drawable.ic_audit_yes);

                    } else if (holder.planoObj.getAudit() == IvyConstants.AUDIT_OK) {

                        holder.planoObj.setAudit(IvyConstants.AUDIT_NOT_OK);
                        holder.audit.setImageResource(R.drawable.ic_audit_no);

                    } else if (holder.planoObj.getAudit() == IvyConstants.AUDIT_NOT_OK) {

                        holder.planoObj.setAudit(IvyConstants.AUDIT_DEFAULT);
                        holder.audit.setImageResource(R.drawable.ic_audit_none);
                    }

                }
            });

//            if(holder.planoObj.getPlanoGramCameraImgList().size() < 2)
//                holder.indicator.setVisibility(View.GONE);
//            else
//                holder.indicator.setVisibility(View.VISIBLE);

            if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                holder.auditLayout.setVisibility(View.VISIBLE);
                holder.imgFromCamera.setEnabled(false);
                holder.layout_cameraImage.setEnabled(false);
                holder.rdYes.setEnabled(false);
                holder.rdNo.setEnabled(false);
                holder.adherence_reason.setEnabled(false);
            }
            else
                holder.auditLayout.setVisibility(View.GONE);


            adapter.registerDataSetObserver(holder.indicator.getDataSetObserver());
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imgFromServer;
            ImageView imgFromCamera;
            RadioButton rdYes;
            RadioButton rdNo;
            RadioButton rdPartial;
            TextView productName;
            TextView tvAdherence;
            TextView text_clickToTakePicture;
            Spinner adherence_reason,spinner_percentage;
            LinearLayout layout_reason;
            PlanoGramBO planoObj;
            LinearLayout layout_cameraImage;
            android.support.v4.view.ViewPager imageViewPager;
            CircleIndicator indicator;
            LinearLayout auditLayout;
            ImageView audit;

            public ViewHolder(View v) {
                super(v);
                imgFromCamera = v.findViewById(R.id.capture_image_view);
                imgFromServer = v.findViewById(R.id.planogram_image_view);
                rdYes = v.findViewById(R.id.yes);
                rdNo =  v.findViewById(R.id.no);
                rdPartial =  v.findViewById(R.id.partial);
                adherence_reason = v.findViewById(R.id.sp_reason);
                spinner_percentage=v.findViewById(R.id.sp_percentage);
                productName =  v.findViewById(R.id.plano_product);
                tvAdherence =  v.findViewById(R.id.adherence_text_view);
                text_clickToTakePicture =  v.findViewById(R.id.tvClicktoTakePic);
                adherence_reason.setAdapter(reasonAdapter);
                spinner_percentage.setAdapter(percentageAdapter);

                layout_cameraImage =  v.findViewById(R.id.ll_cameraImage);
                imageViewPager = v.findViewById(R.id.image_view_pager);
                indicator = v.findViewById(R.id.indicator);
                auditLayout = v.findViewById(R.id.audit_layout);
                audit = v.findViewById(R.id.btn_audit);
                layout_reason=v.findViewById(R.id.layout_reason);

                if (isPreVisit)
                    layout_cameraImage.setEnabled(false);
            }

            private void setImageFromCamera() {
                if (planoObj.getPlanoGramCameraImgList().size() > 0) {

                    boolean isImgPresent = false;

                    for(int i=0;i<planoObj.getPlanoGramCameraImgList().size();i++){
                        String path = photoNamePath
                                + planoObj.getPlanoGramCameraImgList().get(i);
                        if(planoObj.isLoadedFromLastVisitTran()){
                            path=getActivity().getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS)
                                    + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO()
                                    .getUserid()
                                    + DataMembers.DIGITAL_CONTENT
                                    + "/"
                                    + DataMembers.PLANOGRAM
                                    + "/";
                        }
                        if (FileUtils.isFileExisting(path)) {
                            isImgPresent = true;
                        }
                    }

                    if (isImgPresent && !isPreVisit) {
                        rdYes.setEnabled(true);
                        rdYes.setChecked(false);
                        rdNo.setEnabled(true);
                        rdNo.setChecked(false);
                        rdPartial.setEnabled(true);
                        rdPartial.setChecked(false);
                    } else {
                        rdYes.setEnabled(false);
                        rdNo.setEnabled(false);
                        rdPartial.setEnabled(false);

                        rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                        rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                        rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));

                        rdPartial.setButtonDrawable(R.drawable.ic_partial_disabled);
                        rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    }
                } else {
                    rdYes.setEnabled(false);
                    rdNo.setEnabled(false);
                    rdPartial.setEnabled(false);

                    rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                    rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                    rdPartial.setButtonDrawable(R.drawable.ic_partial_disabled);

                    rdYes.setTextColor(ContextCompat.getColor(context, R.color.plano_yes_grey));
                    rdNo.setTextColor(ContextCompat.getColor(context, R.color.plano_yes_grey));
                    rdPartial.setTextColor(ContextCompat.getColor(context, R.color.plano_yes_grey));
                }
                if (planoObj.getAdherence() != null
                        && "1".equals(planoObj.getAdherence())) {
                    rdYes.setChecked(true);
                    rdNo.setChecked(false);
                    rdPartial.setChecked(false);
                    rdYes.setButtonDrawable(R.drawable.ic_tick_enable);
                    rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                    rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdNo.setButtonDrawable(R.drawable.ic_cross_disable);

                    rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdPartial.setButtonDrawable(R.drawable.ic_partial_disabled);

                    layout_reason.setVisibility(View.GONE);
                    planoObj.setAdherence("1");
                } else if (planoObj.getAdherence() != null
                        && "0".equals(planoObj.getAdherence())) {
                    rdNo.setChecked(true);
                    rdYes.setChecked(false);
                    rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                    rdNo.setButtonDrawable(R.drawable.ic_cross_enable);
                    rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_no_red));
                    layout_reason.setVisibility(View.VISIBLE);
                    spinner_percentage.setVisibility(View.GONE);
                    planoObj.setAdherence("0");
                    adherence_reason.setSelection(getStatusIndex(planoObj
                            .getReasonID()));
                    adherence_reason.setSelected(true);
                }
                else if (planoObj.getAdherence() != null
                        && "-1".equals(planoObj.getAdherence())) {
                    rdNo.setChecked(false);
                    rdPartial.setChecked(true);
                    rdYes.setChecked(false);
                    rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                    rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                    rdPartial.setButtonDrawable(R.drawable.ic_partial_enabled);
                    rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdPartial.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_no_red));
                    layout_reason.setVisibility(View.VISIBLE);
                    planoObj.setAdherence("-1");
                    adherence_reason.setSelection(getStatusIndex(planoObj
                            .getReasonID()));
                    adherence_reason.setSelected(true);
                    spinner_percentage.setSelection(getPercentageIndex(planoObj
                            .getPercentageId()));
                    spinner_percentage.setSelected(true);
                }
            }

            private void setImageFromServer() {
                if (planoObj.getImageName() != null) {
                    File imgFile = new File(getActivity().getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + mBModel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.DIGITAL_CONTENT
                            + "/"
                            + DataMembers.PLANOGRAM
                            + "/"
                            + planoObj.getImageName());
                    Commons.print("img name" + planoObj.getImageName());
                    if (imgFile.exists() && !"".equals(planoObj.getImageName())) {
                        try {
                            Commons.print("img path"
                                    + imgFile.getAbsolutePath());
                            Bitmap myBitmap = decodeFile(imgFile);
                            imgFromServer.setScaleType(ScaleType.FIT_XY);
                            imgFromCamera.setAdjustViewBounds(true);
                            imgFromServer.setImageBitmap(myBitmap);
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        imgFromServer
                                .setImageResource(R.drawable.no_image_available);
                    }
                }

            }
        }
    }

    /**
     * Refresh list view
     */
    private void refreshList() {
        if (planoAdapter == null) {
            planoAdapter = new PlanogramAdapter(mPlanoGramList);
            plano_recycler.setAdapter(planoAdapter);
        } else {
            planoAdapter.setListData(mPlanoGramList);
            planoAdapter.notifyDataSetChanged();
        }
    }

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
            bundle.putString("isFrom", "Planogram");

            // set Fragment class Arguments
            FilterFiveFragment<Object> fragment = new FilterFiveFragment<>();
            fragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragment, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlanoGramHelper.clearInstance();
    }

    interface ItemClickSupport{
        void onItemClicked(String path,boolean isDelete);
    }

    private void onBackButonClick() {

        String result = checkDataForSave();
        if (!PlanogramCheck.NOTCOMPLETED.toString().equals(result) && !PlanogramCheck.ADHERENCEFAILED.toString().equals(result)) {
            showAlert();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                if ("1".equals(calledBy) || "3".equals(calledBy)) {
                    getActivity().finish();
                } else if ("2".equals(calledBy)) {
                    if (!isPreVisit)
                        mBModel.outletTimeStampHelper
                            .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));

                    Intent intent = new Intent(context,HomeScreenTwo.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    startActivity(intent);

                    ((Activity)context).finish();
                }
            }
        }
    }

    private void showAlert() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if ("1".equals(calledBy) || "3".equals(calledBy)) {
                    getActivity().finish();
                } else if ("2".equals(calledBy)) {

                    if (!isPreVisit)
                        mBModel.outletTimeStampHelper
                            .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));

                    Intent intent = new Intent(context,HomeScreenTwo.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit",true);

                    startActivity(intent);

                    ((Activity)context).finish();
                }
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
