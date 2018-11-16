package com.ivy.cpg.view.asset;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class
AssetTrackingFragment extends IvyBaseFragment implements OnEditorActionListener, BrandDialogInterface,
        DataPickerDialogFragment.UpdateDateInterface, AssetContractor.AssetView, FiveLevelFilterCallBack {

    private DrawerLayout mDrawerLayout;
    private AlertDialog alertDialog;
    private ListView listview;
    private ActionBar actionBar;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int MOVEMENT_ASSET = 2;
    private int mSelectedLocationIndex;
    private static final String ALL = "ALL";
    private static final String MENU_ASSET = "MENU_ASSET";
    private static final String TAG = "AssetTracking Screen";

    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    private AssetTrackingHelper assetTrackingHelper;
    private AssetPresenterImpl assetPresenter;
    private AssetAdapter adapter;
    private BusinessModel mBModel;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());
        assetPresenter = new AssetPresenterImpl(getContext(), mBModel, assetTrackingHelper);
        assetPresenter.setView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(assetTrackingHelper.mSelectedActivityName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_asset_tracking, container,
                false);

        initializeViews(view);
        assetPresenter.initialLoad();

        return view;
    }

    private void initializeViews(View view) {

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        FrameLayout drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
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
                if (actionBar != null)
                    setScreenTitle(assetTrackingHelper.mSelectedActivityName);

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null)
                    setScreenTitle(getResources().getString(R.string.filter));

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.isEmpty()){
                    String titleText = getString(R.string.no_data_tosave);
                    AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                            getActivity());
                    alertDialogBuilder1
                            .setIcon(null)
                            .setCancelable(false)
                            .setTitle(titleText)
                            .setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            dialog.cancel();
                                        }
                                    });

                    mBModel.applyAlertDialogTheme(alertDialogBuilder1);
                } else {
                    assetPresenter.checkDataExistToSave();
                }
            }
        });

        FloatingActionButton btnBarcode = (FloatingActionButton) view.findViewById(R.id.fab_barcode);
        if (!assetTrackingHelper.SHOW_ASSET_BARCODE)
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


        listview = (ListView) view.findViewById(R.id.list);
        listview.setCacheColorHint(0);

        if (mBModel.configurationMasterHelper.IS_TEAMLEAD) {
            TextView tvAudit = (TextView) view.findViewById(R.id.audit);
            tvAudit.setVisibility(View.VISIBLE);

        }
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
        hideAndSeeK();

    }

    /**
     * Method that to show visibility and hided column
     */
    private void hideAndSeeK() {

        View view = getView();
        if (view != null && !assetTrackingHelper.SHOW_ASSET_PHOTO) {
            view.findViewById(R.id.tv_is_photo).setVisibility(View.GONE);
        }

        if (!assetTrackingHelper.SHOW_ASSET_QTY && view != null) {
            view.findViewById(R.id.tv_isAvail).setVisibility(View.GONE);
        } else {
            try {
                if (view != null) {
                    if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.tv_isAvail).getTag()) != null) {
                        ((TextView) view.findViewById(R.id.tv_isAvail))
                                .setText(mBModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.tv_isAvail).getTag()));

                    }
                    ((TextView) view.findViewById(R.id.tv_isAvail)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        try {
            if (view != null) {
                if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_asset_name).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_asset_name))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_asset_name).getTag()));
                }
                ((TextView) view.findViewById(R.id.tv_header_asset_name)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        if (view != null && !assetTrackingHelper.SHOW_ASSET_EXECUTED)
            view.findViewById(R.id.tv_is_executed).setVisibility(View.GONE);
        else {

            try {
                if (view != null) {
                    if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.tv_is_executed).getTag()) != null) {
                        ((TextView) view.findViewById(R.id.tv_is_executed))
                                .setText(mBModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.tv_is_executed).getTag()));

                    }
                    ((TextView) view.findViewById(R.id.tv_is_executed)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

    }


    @Override
    public void updateInitialLoad(Vector<StandardListBO> mList) {

        // prepare location adapter
        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mList)
            mLocationAdapter.add(temp);

        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = mBModel.productHelper.getmSelectedGLobalLocationIndex();
        }

        assetPresenter.updateList();

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

        String str_addasset;
        String str_removeasset;
        String str_assetservice;

        if (mBModel.labelsMasterHelper.applyLabels("add_asset") != null)
            str_addasset = mBModel.labelsMasterHelper.applyLabels("add_asset");
        else
            str_addasset = getResources().getString(R.string.addnewasset);

        if (mBModel.labelsMasterHelper.applyLabels("remove_asset") != null)
            str_removeasset = mBModel.labelsMasterHelper.applyLabels("remove_asset");
        else
            str_removeasset = getResources().getString(R.string.removeasset);

        if (mBModel.labelsMasterHelper.applyLabels("asset_service") != null)
            str_assetservice = mBModel.labelsMasterHelper.applyLabels("asset_service");
        else
            str_assetservice = getResources().getString(R.string.asset_service);

        menu.findItem(R.id.menu_add).setTitle(str_addasset);
        menu.findItem(R.id.menu_remove).setTitle(str_removeasset);
        menu.findItem(R.id.menu_assetservice).setTitle(str_assetservice);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        if (!assetTrackingHelper.SHOW_REMARKS_ASSET) {
            menu.findItem(R.id.menu_remarks).setVisible(false);
        }

        if (!assetTrackingHelper.SHOW_ADD_NEW_ASSET) {
            menu.findItem(R.id.menu_add).setVisible(false);
        }
        if (!assetTrackingHelper.SHOW_REMOVE_ASSET) {
            menu.findItem(R.id.menu_remove).setVisible(false);
        }
        if (assetTrackingHelper.SHOW_ASSET_ALL) {
            menu.findItem(R.id.menu_all).setVisible(true);
        }
        if (mBModel.configurationMasterHelper.floating_Survey) {
            menu.findItem(R.id.menu_survey).setVisible(true);
        }

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (mBModel.productHelper.isFilterAvaiable(MENU_ASSET)) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        }
        if (!assetTrackingHelper.SHOW_MOVE_ASSET) {
            menu.findItem(R.id.menu_move).setVisible(false);
        }
        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (mBModel.productHelper.getInStoreLocation().size() < 2)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }

        if (!assetTrackingHelper.SHOW_SERVICE_ASSET) {
            menu.findItem(R.id.menu_assetservice).setVisible(false);
        }

        menu.findItem(R.id.menu_reason).setVisible(mBModel.configurationMasterHelper.floating_np_reason_photo);

        if (drawerOpen) {
            menu.clear();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if(adapter.isEmpty()){
                save();
            } else {
                try {
                    Iterator it = mBModel.getPhotosTakeninCurrentAssetTracking().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String mAssetId = pair.getKey().toString();
                        String photoPath = pair.getValue().toString();

                        String fileName = photoPath.substring(photoPath.lastIndexOf('/') + 1, photoPath.length());
                        String fileNameStarts = fileName.substring(0, fileName.length() - 14);

                        String path = photoPath.substring(0, photoPath.lastIndexOf('/'));

                        assetPresenter.removeExistingImage(mAssetId, fileNameStarts, path);

                        System.out.println("Deleted Image " + mAssetId + " = " + photoPath);
                        //it.remove(); // avoids a ConcurrentModificationException
                    }
                    mBModel.getPhotosTakeninCurrentAssetTracking().clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    assetPresenter.updateTimeStamp();
                    startActivity(new Intent(getActivity(),
                            HomeScreenTwo.class));
                    getActivity().finish();
                }
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
            return true;
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_all) {
            assetPresenter.setBarcode(ALL);
            assetPresenter.updateList();
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
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_fivefilter) {

            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_move) {
            if (assetPresenter.getAssetListSize() >= 0) {
                Intent intent = new Intent(getActivity(), AssetMovementActivity.class);
                intent.putExtra("index", mSelectedLocationIndex);
                intent.putExtra("module", MENU_ASSET);
                startActivityForResult(intent, MOVEMENT_ASSET);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (i == R.id.menu_assetservice) {


            Intent intent = new Intent(getActivity(), AssetServiceActivity.class);
            intent.putExtra("module", MENU_ASSET);
            startActivity(intent);

            return true;
        }else if(i== R.id.menu_assetScan){

            scanBarCode();

        }
        else if (i == R.id.menu_reason) {
            try {
                mBModel.reasonHelper.downloadNpReason(mBModel.retailerMasterBO.getRetailerID(), MENU_ASSET);
                ReasonPhotoDialog dialog = new ReasonPhotoDialog();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mBModel.reasonHelper.isNpReasonPhotoAvaiable(mBModel.retailerMasterBO.getRetailerID(), MENU_ASSET)) {
                            mBModel.saveModuleCompletion(MENU_ASSET);
                            mBModel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                            startActivity(new Intent(getActivity(),
                                    HomeScreenTwo.class));
                            getActivity().finish();
                        }
                    }
                });
                Bundle args = new Bundle();
                args.putString("modulename", MENU_ASSET);
                dialog.setCancelable(false);
                dialog.setArguments(args);
                dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            }
            catch (Exception ex){
                Commons.printException(ex);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanBarCode(){
        {
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

    }


    @Override
    public void updateAssets(ArrayList<AssetTrackingBO> mList, boolean isUnMapped, Bundle mBundle) {

        updateListView(mList);

        if (mList.size() == 0) {
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
        assetPresenter.setNFCTag(mNFCTag);
        assetPresenter.setBarcode(ALL);
        assetPresenter.updateList();
    }


    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(TAG + "," +
                        "Camera Activity : Successfully Captured.");
                assetPresenter.updateImageName();
                adapter.notifyDataSetChanged();

            } else {
                Commons.print(TAG + "," + "Camera Activity : Canceled");
            }
        } else if (requestCode == MOVEMENT_ASSET) {
            assetTrackingHelper.loadDataForAssetPOSM(getActivity().getApplicationContext(), MENU_ASSET);
        } else {

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                    } else {
                        assetPresenter.setBarcode(result.getContents());
                        assetPresenter.updateList();
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        }


    }

    @Override
    public void isDataExistToSave(boolean isAssetExist, boolean isPhotoExist, boolean isReasonExist, String errorMsg) {
        if (isAssetExist && isPhotoExist
                && isReasonExist) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

            assetPresenter.save(MENU_ASSET);
        } else {
            String titleText;
            if (errorMsg.isEmpty())
                titleText = getString(R.string.no_assets_exists);
            else
                titleText = errorMsg;

            AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                    getActivity());
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)
                    .setTitle(titleText)
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.cancel();
                                }
                            });

            mBModel.applyAlertDialogTheme(alertDialogBuilder1);
        }
    }




    @Override
    public void showError(String errorMsg) {
        String titleText;
        if (errorMsg.isEmpty())
            titleText = getString(R.string.no_assets_exists);
        else
            titleText = errorMsg;

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder1
                .setIcon(null)
                .setCancelable(false)
                .setTitle(titleText)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });

        mBModel.applyAlertDialogTheme(alertDialogBuilder1);
    }

    @Override
    public void save() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getResources().getString(R.string.saving));
        alertDialog = builder.create();
        alertDialog.show();

        assetPresenter.save(MENU_ASSET);
    }

    @Override
    public void cancelProgressDialog() {
        if(adapter.isEmpty()){
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {
                assetPresenter.updateTimeStamp();
                startActivity(new Intent(getActivity(),
                        HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        } else {
            assetPresenter.updateTimeStamp();
            if (alertDialog != null) {
                alertDialog.dismiss();
            }

            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    null, new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    try{
                        mBModel.getPhotosTakeninCurrentAssetTracking().clear();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
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
                        assetPresenter.setBarcode(ALL);
                        mSelectedLocationIndex = item;
                        dialog.dismiss();
                        assetPresenter.updateLocationIndex(item);
                        assetPresenter.updateList();
                    }
                });

        mBModel.applyAlertDialogTheme(builderDialog);
    }


    @Override
    public void updateBrandText(String mFilterText, int id) {

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        assetPresenter.updateFiveFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateFiveFilteredList(ArrayList<AssetTrackingBO> mFilteredList) {
        updateListView(mFilteredList);
        mDrawerLayout.closeDrawers();
    }

    private void updateListView(ArrayList<AssetTrackingBO> list) {
        android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
        AssetTrackingFragment fragment = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);

        adapter = new AssetAdapter(getActivity(), mBModel, assetPresenter, fragment, list);
        listview.setAdapter(adapter);

        int size = list.size();
        if (size == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_assets_exists),
                    Toast.LENGTH_SHORT).show();

        }
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
                    mBModel.configurationMasterHelper.getGenFilter());
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
        adapter.updateDate(date, tag);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        assetTrackingHelper.clear();
    }
}
