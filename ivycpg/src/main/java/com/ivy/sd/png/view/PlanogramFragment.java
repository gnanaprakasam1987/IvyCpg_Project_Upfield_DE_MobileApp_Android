package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
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

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.PlanogramBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.van.LoadManagementScreen;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PlanogramFragment extends IvyBaseFragment implements
        OnClickListener, BrandDialogInterface {

    private static final String TAG = "PlanogramFragment";
    private static final int CAMERA_REQUEST_CODE = 1;
    private DrawerLayout mDrawerLayout;
    private static final String BRAND = "Brand";
    private BusinessModel bmodel;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private Vector<PlanogramBO> vPlanogram = new Vector<>();

    private String filter_Heading;
    private int filterId;
    private ArrayAdapter<StandardListBO> locationAdapter;
    private int selecteditem;
    private int locSelectionId = -1;
    private int selectedCategory, productId;
    private String locationName = "";
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private String photoNamePath;
    private String calledBy = "0";
    private boolean isDialogPopup;
    private PlanogramAdapter planoAdapter;
    private RecyclerView plano_recycler;
    private int mSelectedBrandID = 0;
    private String brandbutton;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private String generalbutton;
    private static final String GENERAL = "General";
    private String imageFileName = "";
    private boolean isFromChild;
    private String menuCode = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_planogram, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        // download data for planogram
        vPlanogram = bmodel.planogramMasterHelper.getPlanogramMaster();
        plano_recycler = (RecyclerView) view.findViewById(R.id.plano_recycler);
        plano_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        Button btnSave = (Button) view.findViewById(R.id.saveButton);
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setOnClickListener(this);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        final Intent i = getActivity().getIntent();
        calledBy = i.getStringExtra("from");
        menuCode = i.getStringExtra("CurrentActivityCode");
        isFromChild = i.getBooleanExtra("isFromChild", false);
        isDialogPopup = false;
        photoNamePath = HomeScreenFragment.photoPath + "/";

        Commons.print("Photo Path ," + "" + photoNamePath);

        loadReason();

        locSelectionId = Integer.parseInt(bmodel.productHelper
                .getInStoreLocation().get(0).getListID());
        if (savedInstanceState != null) {
            updatebrandtext(BRAND,
                    savedInstanceState.getInt("id"));
            filterId = savedInstanceState.getInt("id");
            filter_Heading = savedInstanceState.getString("filterName");
        }


        return view;
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
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if ("POG".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory())) {
                spinnerAdapter.add(temp);

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());


            // set a custom shadow that overlays the main content when the
            // drawer
            // opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.END);

            mDrawerLayout
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setElevation(0);
            }

            setScreenTitle(bmodel.mSelectedActivityName);


            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /*
                                                                     * host
																	 * Activity
																	 */
                    mDrawerLayout, /* DrawerLayout object */
                    R.string.ok, /* "open drawer" description for accessibility */
                    R.string.close /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    if (actionBar != null)
                        setScreenTitle(bmodel.mSelectedActivityName);

                    getActivity().supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {

                    if (actionBar != null)
                        setScreenTitle(getResources().getString(R.string.filter));

                    getActivity().supportInvalidateOptionsMenu();
                }
            };

            locationAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice);

            for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
                locationAdapter.add(temp);
            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
                StandardListBO selectedId = locationAdapter
                        .getItem(bmodel.productHelper.getmSelectedGLobalLocationIndex());
                selecteditem = bmodel.productHelper.getmSelectedGLobalLocationIndex();
                locSelectionId = Integer.parseInt(selectedId
                        .getListID());
                locationName = " -" + selectedId.getListName();
                if (actionBar != null) {
                    actionBar.setTitle(bmodel.mSelectedActivityName
                            + locationName);
                }
            }


            mDrawerLayout.addDrawerListener(mDrawerToggle);

            if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM) {
                updatebrandtext(BRAND, 0);
            } else {

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                    mSelectedFilterMap.put("General", GENERAL);
                    updategeneraltext(GENERAL);
                } else {
                    if (bmodel.mSFSelectedFilter == -1)
                        if (selectedCategory == 0)
                            bmodel.mSFSelectedFilter = bmodel.planogramMasterHelper
                                    .getmChildLevelBo().get(0).getProductid();
                        else
                            bmodel.mSFSelectedFilter = bmodel.planogramMasterHelper
                                    .getmParentLevelBo().get(0).getPl_productid();

                    mSelectedFilterMap.put("Brand",
                            String.valueOf(bmodel.mSFSelectedFilter));
                    updatebrandtext(BRAND, bmodel.mSFSelectedFilter);
                    productFilterClickedFragment();
                }

                mDrawerLayout.closeDrawer(GravityCompat.END);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Planogram");
        //if statement to make sure the alert is displayed only for the first time
        if (bmodel.productHelper.getInStoreLocation().size() != 1 && !isDialogPopup) {
            if (!bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                showLocationFilterAlert();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", filterId);
        outState.putString("filterName", filter_Heading);
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
            if (checkDataforSave())
                nextButtonClick();
            else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.please_fill_adherence), 0);
            }
        }
    }

    /**
     * method calls while after capture the photo and return to this page and
     * save the image
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                Commons.print(TAG + ",Camers Activity : Sucessfully Captured.");
                for (PlanogramBO planBo : vPlanogram) {
                    if (planBo.getPid() == productId) {
                        planBo.setPlanogramCameraImgName(imageFileName);
                    }
                }
                planoAdapter.notifyDataSetChanged();
                Commons.print(imageFileName);
            } else {
                Commons.print(TAG + ",Camers Activity : Canceled");
                for (PlanogramBO planBo : vPlanogram) {
                    if (planBo.getPid() == productId) {
                        planBo.setAdherence(null);
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void takePhoto(PlanogramBO planogramBO) {
        // Disable Motorola ET1 Scanner Plugin
        final String ACTION_SCANNERINPUTPLUGIN = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
        final String EXTRA_PARAMETER = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
        final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
        if (bmodel.isExternalStorageAvailable()) {

            if ("1".equals(calledBy)) {
                imageFileName = "VPL_" + "0" + "_" + selectedCategory + "_"
                        + locSelectionId + "_" + Commons.now(Commons.DATE_TIME)
                        + "_img.jpg";

            } else {
                imageFileName = "PL_" + planogramBO.getPid()
                        + "_" + selectedCategory + "_" + locSelectionId + "_"
                        + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            }

            String path = photoNamePath + imageFileName;
            /*planogramBO.setPlanogramCameraImgName(imageFileName);
            planoAdapter.notifyDataSetChanged();*/
            try {
                Intent i = new Intent();
                i.setAction(ACTION_SCANNERINPUTPLUGIN);
                i.putExtra(EXTRA_PARAMETER, DISABLE_PLUGIN);
                getActivity().sendBroadcast(i);

                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), path);
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


    /*private void searchAndUpdateImage() {
        String path = imageFileName;
        bmodel.planogramMasterHelper.setImagePath(selectedCategory, path,
                locSelectionId);

        enableAdherence();
        clearImageViews();

        setImagefromCamera(selectedCategory, locSelectionId);
    }*/

    @Override
    public void updatebrandtext(String filtertext, int bid) {
        mSelectedBrandID = bid;
        try {

            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = filtertext;

            filterId = bid;

            selectedCategory = bid;
            bmodel.mSFSelectedFilter = bid;

            vPlanogram = new Vector<>();
            Vector<PlanogramBO> items = bmodel.planogramMasterHelper.getPlanogramMaster();
            for (final PlanogramBO planogramBO : items) {
                if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM
                        && planogramBO.getLocationID() == locSelectionId) {
                    if (bid == planogramBO.getPid() || (bid == 0 && "Brand".equals(filtertext)))
                        vPlanogram.add(planogramBO);
                } else if (locSelectionId == -1) {
                    if (bid == planogramBO.getPid() || (bid == -1 && "Brand".equals(filtertext)))
                        vPlanogram.add(planogramBO);
                }
            }
            refreshList();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public void updategeneraltext(String filtertext) {
        generalbutton = filtertext;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();
        updatebrandtext(BRAND, bmodel.mSFSelectedFilter);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();// Close the drawer
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    // /**
    // * Called whenever we call invalidateOptionsMenu()
    // */
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

            if (!brandbutton.equals(BRAND))
                menu.findItem(R.id.menu_product_filter).setIcon(
                        R.drawable.ic_action_filter_select);

            menu.findItem(R.id.menu_product_filter).setVisible(true);

            menu.findItem(R.id.menu_next).setVisible(false);
            menu.findItem(R.id.menu_location_filter).setVisible(true);

            if (bmodel.productHelper.getInStoreLocation().size() == 1) {
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            }

            if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM)
                menu.findItem(R.id.menu_product_filter).setVisible(false);

            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && !menuCode.equals("MENU_PLANOGRAM_CS") && bmodel.productHelper.isFilterAvaiable(menuCode)) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
            } /*else {
                menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
                menu.findItem(R.id.menu_fivefilter).setVisible(false);
            }*/

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }
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
            else {
                if ("1".equals(calledBy)) {
                    startActivity(new Intent(getActivity(),
                            LoadManagementScreen.class));
                    getActivity().finish();
                }
                if ("2".equals(calledBy)) {
                    bmodel.outletTimeStampHelper
                            .updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                    getActivity().finish();
                }
                if ("3".equals(calledBy)) {
//                    startActivity(new Intent(getActivity(),
//                            CSHomeScreen.class));
                    getActivity().finish();
                }


            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_photo) {
            try {
                for (final PlanogramBO planogramBO : vPlanogram) {
                    if ((planogramBO.getPid() == selectedCategory || selectedCategory == -1 || bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM)
                            && (planogramBO.getLocationID() == locSelectionId) && planogramBO.getImageName() != null) {
                        File imgFile = new File(getActivity()
                                .getExternalFilesDir(
                                        Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.PLANOGRAM
                                + "/"
                                + planogramBO.getImageName());
                        if (imgFile.exists()) {
                            // takePhoto();

                        } else {
                            Toast.makeText(getActivity(),
                                    "Server image not available",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_location_filter) {
            showLocationFilterAlert();
            return true;
        } else if (i == R.id.menu_product_filter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            productFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_remarks) {
            android.support.v4.app.FragmentManager ft = getActivity()
                    .getSupportFragmentManager();
            RemarksDialog dialog = new RemarksDialog("MENU_PLANOGRAM");
            dialog.setCancelable(false);
            dialog.show(ft, "MENU_PLANOGRAM");
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextButtonClick() {
        try {
            if (checkforValidation()) {
                new SaveAsyncTask().execute();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.select_reason),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

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
            bundle.putString("filterHeader", bmodel.planogramMasterHelper
                    .getmChildLevelBo().get(0).getProductLevel());
            bundle.putBoolean("ishideAll", true);
            bundle.putSerializable("serilizeContent",
                    bmodel.planogramMasterHelper.getmChildLevelBo());

            if (bmodel.planogramMasterHelper.getmParentLevelBo() != null
                    && bmodel.planogramMasterHelper.getmParentLevelBo()
                    .size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader",
                        bmodel.planogramMasterHelper.getmParentLevelBo()
                                .get(0).getPl_productLevel());

                bmodel.productHelper
                        .setPlevelMaster(bmodel.planogramMasterHelper
                                .getmParentLevelBo());
            } else
                bundle.putBoolean("isFormBrand", false);

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
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
                bmodel.planogramMasterHelper.savePhotocapture();
                if (calledBy != null && !"3".equals(calledBy))
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_PLANOGRAM);
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
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
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        if ("1".equals(calledBy)) {
                            startActivity(new Intent(getActivity(),
                                    LoadManagementScreen.class));
                            getActivity().finish();
                        }
                        if ("2".equals(calledBy)) {
                            bmodel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(SDUtil
                                            .now(SDUtil.TIME));
                            bmodel.updateIsVisitedFlag();

                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            Bundle extras = getActivity().getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

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

    private boolean checkforValidation() {
        boolean flag = true;
        for (final PlanogramBO planogramBO : vPlanogram) {
            flag = true;
            if (planogramBO.getAdherence() != null && "0".equals(planogramBO.getAdherence())
                    && "0".equals(planogramBO.getReasonID())) {
                return false;
            }
        }
        return flag;
    }

    private boolean checkDataforSave() {
        for (final PlanogramBO planogramBO : vPlanogram) {
            if (planogramBO.getAdherence() != null)
                return true;
        }
        return false;
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

    private void setCameraImage(PlanogramBO planogramBO) {
        try {
            takePhoto(planogramBO);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /*
     * Show Location wise Filter
     */
    private void showLocationFilterAlert() {
        AlertDialog.Builder builder;
        isDialogPopup = true;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(locationAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        StandardListBO selectedId = locationAdapter
                                .getItem(item);
                        selecteditem = item;
                        locSelectionId = Integer.parseInt(selectedId
                                .getListID());
                        locationName = " -" + selectedId.getListName();
                        ActionBar actionBar = ((AppCompatActivity) getActivity())
                                .getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setTitle(bmodel.mSelectedActivityName
                                    + locationName);
                        }
                        dialog.dismiss();
                        updatebrandtext(BRAND, 0);

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private int getStatusIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    public void showFileDeleteAlert(final String imageNameStarts, final PlanogramBO planogramBO) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        bmodel.planogramMasterHelper
                                .deleteImageName(planogramBO.getPlanogramCameraImgName());
                        bmodel.competitorTrackingHelper.deleteFiles(
                                HomeScreenFragment.folder.getPath(), planogramBO.getPlanogramCameraImgName());
                        planogramBO.setPlanogramCameraImgName("");

                        dialog.dismiss();

                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        String _path = HomeScreenFragment.photoPath + "/" + imageNameStarts;
                        intent.putExtra(
                                getResources().getString(R.string.quality), 40);
                        intent.putExtra(
                                getResources().getString(R.string.path), _path);
                        intent.putExtra(
                                getResources().getString(R.string.saverequired),
                                false);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);

                        return;
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        return;
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    class PlanogramAdapter extends RecyclerView.Adapter<PlanogramAdapter.ViewHolder> {

        private Vector<PlanogramBO> items;

        private PlanogramAdapter(Vector<PlanogramBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.planogram_list_child, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            photoNamePath = HomeScreenFragment.photoPath + "/";
            holder.planoObj = items.get(position);
            holder.productName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.tvAdherence.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvClicktoTakePic.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.rdYes.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.rdNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.productName.setText(holder.planoObj.getProductName());

            if ("0".equals(holder.planoObj.getAdherence())) {
                holder.adherence_reason.setVisibility(View.VISIBLE);
            } else {
                holder.adherence_reason.setVisibility(View.INVISIBLE);
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
                        holder.rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_green));
                        holder.rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                        holder.rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                        holder.planoObj.setAdherence("1");
                        holder.adherence_reason.setVisibility(View.INVISIBLE);
                        holder.planoObj.setReasonID("0");
                        holder.adherence_reason.setSelection(0);
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
                        holder.adherence_reason.setVisibility(View.VISIBLE);
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
            holder.imgFromServer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.planoObj.getImageName() != null) {
                        File imgFile = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
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
            holder.ivCamera.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoNamePath = HomeScreenFragment.photoPath + "/";
                    if ("1".equals(calledBy)) {
                        imageFileName = "VPL_" + "0" + "_" + selectedCategory + "_"
                                + locSelectionId + "_" + Commons.now(Commons.DATE) + "_img.jpg";

                    } else {
                        imageFileName = "PL_" + holder.planoObj.getPid()
                                + "_" + selectedCategory + "_" + locSelectionId + "_"
                                + Commons.now(Commons.DATE) + "_img.jpg";
                    }
                    if (!"".equals(holder.planoObj.getPlanogramCameraImgName())) {
                        String path = photoNamePath
                                + holder.planoObj.getPlanogramCameraImgName();
                        if (bmodel.planogramMasterHelper.isImagePresent(path)) {
                            showFileDeleteAlert(imageFileName, holder.planoObj);
                        }else {
                            setCameraImage(holder.planoObj);
                        }
                    } else {
                        productId = holder.planoObj.getPid();
                        setCameraImage(holder.planoObj);
                    }
                }
            });
            holder.llCamerImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoNamePath = HomeScreenFragment.photoPath + "/";
                    if ("1".equals(calledBy)) {
                        imageFileName = "VPL_" + "0" + "_" + selectedCategory + "_"
                                + locSelectionId + "_" + Commons.now(Commons.DATE) + "_img.jpg";

                    } else {
                        imageFileName = "PL_" + holder.planoObj.getPid()
                                + "_" + selectedCategory + "_" + locSelectionId + "_"
                                + Commons.now(Commons.DATE) + "_img.jpg";
                    }
                    if (!"".equals(holder.planoObj.getPlanogramCameraImgName())) {
                        String path = photoNamePath
                                + holder.planoObj.getPlanogramCameraImgName();
                        if (bmodel.planogramMasterHelper.isImagePresent(path)) {
                            showFileDeleteAlert(imageFileName, holder.planoObj);
                        }else {
                            setCameraImage(holder.planoObj);
                        }
                    } else {
                        productId = holder.planoObj.getPid();
                        setCameraImage(holder.planoObj);
                    }
                }
            });
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
            ImageView ivCamera;
            RadioButton rdYes;
            RadioButton rdNo;
            TextView productName;
            TextView tvAdherence;
            TextView tvClicktoTakePic;
            Spinner adherence_reason;
            PlanogramBO planoObj;
            LinearLayout llCamerImage;

            public ViewHolder(View v) {
                super(v);
                imgFromCamera = (ImageView) v.findViewById(R.id.capture_image_view);
                imgFromServer = (ImageView) v.findViewById(R.id.planogram_image_view);
                ivCamera = (ImageView) v.findViewById(R.id.cameraImage);
                rdYes = (RadioButton) v.findViewById(R.id.yes);
                rdNo = (RadioButton) v.findViewById(R.id.no);
                adherence_reason = (Spinner) v.findViewById(R.id.sp_reason);
                productName = (TextView) v.findViewById(R.id.plano_product);
                tvAdherence = (TextView) v.findViewById(R.id.adherence_text_view);
                tvClicktoTakePic = (TextView) v.findViewById(R.id.tvClicktoTakePic);
                adherence_reason.setAdapter(spinnerAdapter);
                llCamerImage = (LinearLayout) v.findViewById(R.id.ll_cameraImage);
            }

            private void setImageFromCamera() {
                if (!"".equals(planoObj.getPlanogramCameraImgName())) {
                    String path = photoNamePath
                            + planoObj.getPlanogramCameraImgName();
                    if (bmodel.planogramMasterHelper.isImagePresent(path)) {
                        Uri uri = bmodel.planogramMasterHelper
                                .getUriFromFile(path);
                        ivCamera.setVisibility(View.VISIBLE);
                        ivCamera.invalidate();
                        ivCamera.setImageURI(uri);
                        llCamerImage.setVisibility(View.GONE);
                        rdYes.setEnabled(true);
                        rdYes.setChecked(false);
                        rdNo.setEnabled(true);
                        rdNo.setChecked(false);
                    }else {
                        imgFromCamera
                                .setImageResource(R.drawable.ic_photo_camera);
                        rdYes.setEnabled(false);
                        rdNo.setEnabled(false);
                    }
                } else {
                    imgFromCamera
                            .setImageResource(R.drawable.ic_photo_camera);
                    rdYes.setEnabled(false);
                    rdNo.setEnabled(false);
                }
               // rdYes.setEnabled(true);
              //  rdNo.setEnabled(true);
                if (planoObj.getAdherence() != null
                        && "1".equals(planoObj.getAdherence())) {
                    rdYes.setChecked(true);
                    rdNo.setChecked(false);
                    rdYes.setButtonDrawable(R.drawable.ic_tick_enable);
                    rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_green));
                    rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdNo.setButtonDrawable(R.drawable.ic_cross_disable);
                    adherence_reason.setVisibility(View.INVISIBLE);
                    planoObj.setAdherence("1");
                } else if (planoObj.getAdherence() != null
                        && "0".equals(planoObj.getAdherence())) {
                    rdNo.setChecked(true);
                    rdYes.setChecked(false);
                    rdYes.setButtonDrawable(R.drawable.ic_tick_disable);
                    rdNo.setButtonDrawable(R.drawable.ic_cross_enable);
                    rdYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_yes_grey));
                    rdNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.plano_no_red));
                    adherence_reason.setVisibility(View.VISIBLE);
                    planoObj.setAdherence("0");
                    adherence_reason.setSelection(getStatusIndex(planoObj
                            .getReasonID()));
                    adherence_reason.setSelected(true);
                }
            }

            private void setImageFromServer() {
                if (planoObj.getImageName() != null) {
                    File imgFile = new File(getActivity().getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
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
               /* if (is_supervisor) {
                    File imgFile = new File(getActivity()
                            .getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.DIGITAL_CONTENT);
                    if (imgFile.exists()) {
//                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
//                                .getAbsolutePath());
                      //  imgFromSuperior.setImageBitmap(myBitmap);
                    }
                }*/
            }
        }
    }

    private void refreshList() {
        planoAdapter = new PlanogramAdapter(vPlanogram);
        plano_recycler.setAdapter(planoAdapter);
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        Vector<PlanogramBO> items = bmodel.planogramMasterHelper.getPlanogramMaster();

        vPlanogram = new Vector<>();

        for (LevelBO levelBO : parentidList) {
            for (PlanogramBO planogramBO : items) {
                if (levelBO.getProductID() == planogramBO.getPid()) {
                    if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM && planogramBO.getLocationID() == locSelectionId) {
                        if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                            vPlanogram.add(planogramBO);
                        }
                    } else if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                        vPlanogram.add(planogramBO);
                    }
                }
            }
        }
        mDrawerLayout.closeDrawers();
        refreshList();
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        Vector<PlanogramBO> items = bmodel.planogramMasterHelper.getPlanogramMaster();
        brandbutton = filtertext;
        vPlanogram = new Vector<>();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
            for (LevelBO levelBO : parentidList) {
                for (PlanogramBO planogramBO : items) {
                    if (levelBO.getProductID() == planogramBO.getPid()) {
                        if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM && planogramBO.getLocationID() == locSelectionId) {
                            if ((planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0)
                                    && mAttributeProducts.contains(planogramBO.getPid())) {
                                vPlanogram.add(planogramBO);
                            }
                        } else if ((planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1)
                                && mAttributeProducts.contains(planogramBO.getPid())) {
                            vPlanogram.add(planogramBO);
                        }
                    }
                }
            }
        } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
            for (LevelBO levelBO : parentidList) {
                for (PlanogramBO planogramBO : items) {
                    if (levelBO.getProductID() == planogramBO.getPid()) {
                        if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM && planogramBO.getLocationID() == locSelectionId) {
                            if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                                vPlanogram.add(planogramBO);
                            }
                        } else if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                            vPlanogram.add(planogramBO);
                        }
                    }
                }
            }
        } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
            for (int pid : mAttributeProducts) {
                for (PlanogramBO planogramBO : items) {
                    if (pid == planogramBO.getPid()) {
                        if (bmodel.configurationMasterHelper.IS_LOCATION_WISE_PLANOGRAM && planogramBO.getLocationID() == locSelectionId) {
                            if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == 0) {
                                vPlanogram.add(planogramBO);
                            }
                        } else if (planogramBO.getPid() == mSelectedBrandID || mSelectedBrandID == -1) {
                            vPlanogram.add(planogramBO);
                        }
                    }
                }
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

        refreshList();
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
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            bundle.putString("isFrom", "Planogram");
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void loadStartVisit() {
    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {

    }
}
