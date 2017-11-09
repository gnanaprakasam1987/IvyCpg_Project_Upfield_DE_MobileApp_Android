package com.ivy.sd.png.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.ivy.sd.png.bo.AssetTrackingBO;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PosmFragment extends IvyBaseFragment implements
        OnEditorActionListener, BrandDialogInterface {

    private static final String TAG = "POSM Screen";
    private BusinessModel bmodel;

    private DrawerLayout mDrawerLayout;
    private AlertDialog alertDialog;
    private ListView lvwplist;
    private static Button dateBtn;
    private EditText qtyEditText;

    private String append = "";
    private static String outPutDateFormat;
    private Dialog dialog = null;
    private int mYear;
    private int mMonth;
    private int mDay;
    private static final int CAMERA_REQUEST_CODE = 1;
    private final String moduleName = "AT_";

    private static Button addinstalldate;
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";


    private Spinner masset;
    private Spinner mbrand;
    private EditText mSNO;

    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    /**
     * This PATH_NAME used to store Asset photos in sdcard
     */

    private String photoPath = "";

    // Drawer Implimentation
    private ArrayList<AssetTrackingBO> myList = new ArrayList<>();

    /**
     * This ArrayList contains downloaded assettracking records
     */
    private ArrayList<AssetTrackingBO> mAssetTrackingList;
    /**
     * This ArrayList contains downloaded assetreason records
     */
    private ArrayList<ReasonMaster> mPOSMReasonList;
    /**
     * This ArrayList contains downloaded assetremarks records
     */
  /*  private ArrayList<ReasonMaster> mAssetRemarksList;*/

    private ArrayList<ReasonMaster> mPOSMConditionList;
    /**
     * This ArrayAdapter used to set AssetReason in spinner
     */
    private ArrayAdapter<ReasonMaster> mPOSMReasonSpinAdapter;
    /**
     * This ArrayAdapter used to set AssetRemarks in spinner
     */
   /* private ArrayAdapter<ReasonMaster> mAssetRemarksSpinAdapter;*/
    private ArrayAdapter<ReasonMaster> mPOSMConditionAdapter;

    /**
     * After scanned Asset barcode value stored in this string
     */
    private String strBarCodeSearch = "ALL";

    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private int mSelectedLastFilterSelection = -1;
    private StandardListBO mSelectedStandardListBO;
    private String imageName;

    private static final String MENU_POSM = "MENU_POSM";
    private static final String SELECT = "-Select-";
    private boolean isShowed = false;
    Button btnSave;
    FloatingActionButton btnBarcode;
    private String screenCode = "MENU_POSM";
    private String mCapturedNFCTag = "";
    private String brandbutton;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private boolean isFromChild;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_posm_tracking_frag, container,
                false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }

        if (extras != null) {
            screenCode = extras.getString("CurrentActivityCode");
            screenCode = screenCode != null ? screenCode : "MENU_POSM";
        }
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                nextButtonClick();
            }
        });
        btnBarcode = (FloatingActionButton) view.findViewById(R.id.fab_barcode);
        if (bmodel.assetTrackingHelper.SHOW_POSM_BARCODE) {
            btnBarcode.setVisibility(View.VISIBLE);
            btnBarcode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((PosmTrackingScreen) getActivity()).checkAndRequestPermissionAtRunTime(2);
                    int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA);
                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                            @Override
                            protected void startActivityForResult(Intent intent, int code) {
                                PosmFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
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
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (getView() != null) {
            lvwplist = (ListView) getView().findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);
        }
        FrameLayout drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);


        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                if (actionBar != null) {
                    actionBar.setTitle(bmodel.mSelectedActivityName);
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null) {
                    actionBar.setTitle(getResources().getString(R.string.filter));
                }
                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD && getView() != null) {
            TextView tvaudit = (TextView) getView().findViewById(R.id.audit);
            tvaudit.setVisibility(View.VISIBLE);
        }

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (!bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }
        if (mLocationAdapter.getCount() > 0) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }

        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");
        if (!isShowed) {
            if (!bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION && bmodel.productHelper.getInStoreLocation().size() > 1)
                showLocation();
            loadedItem();
            isShowed = true;
        }
        hideAndSeeK();
        updatebrandtext(BRAND, mSelectedLastFilterSelection);
        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            mSelectedFilterMap.put("General", GENERAL);
            updategeneraltext(GENERAL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Commons.print("OnResume Called");
        BusinessModel.getInstance().trackScreenView("POSM Tracking");
        switchProfile();
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

        if (bmodel.assetTrackingHelper.SHOW_REMARKS_POSM)
            menu.findItem(R.id.menu_remarks).setVisible(true);
        else
            menu.findItem(R.id.menu_remarks).setVisible(false);

        if (!brandbutton.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.productHelper.isFilterAvaiable(MENU_POSM)) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        } /*else {
            menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
        }*/

        menu.findItem(R.id.menu_add).setVisible(false);
        menu.findItem(R.id.menu_remove).setVisible(false);
        if (bmodel.assetTrackingHelper.SHOW_POSM_ALL) {
            menu.findItem(R.id.menu_all).setVisible(true);
        }
        menu.findItem(R.id.menu_survey).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION || screenCode.equals("MENU_POSM_CS"))
            menu.findItem(R.id.menu_loc_filter).setVisible(false);

        if (bmodel.productHelper.getInStoreLocation().size() <= 1)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        //Move Asset is removed in Posm
        menu.removeItem(R.id.menu_move);
        // hardcoded for demo
        if (screenCode.equals("MENU_POSM_CS"))
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

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
                bmodel.outletTimeStampHelper
                        .updateTimeStampModuleWise(SDUtil
                                .now(SDUtil.TIME));

                if (screenCode.equalsIgnoreCase("MENU_POSM_CS")) {
                    startActivity(new Intent(getActivity(),
                            HomeScreenActivity.class).putExtra("menuCode", "MENU_COUNTER"));
                } else if (screenCode.equalsIgnoreCase("MENU_POSM")) {
                    if (isFromChild)
                        startActivity(new Intent(getActivity(),
                                HomeScreenTwo.class).
                                putExtra("isStoreMenu", true));
                    else
                        startActivity(new Intent(getActivity(),
                                HomeScreenTwo.class));
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
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_add) {

            AddAssetDialogFragment dialog = new AddAssetDialogFragment();
            dialog.show(getFragmentManager(), "Asset");

            return true;
        } else if (i == R.id.menu_remove) {
            Intent intent = new Intent(getActivity(), AssetPosmRemoveActivity.class);
            intent.putExtra("module", screenCode);
            startActivity(intent);
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateListByNFCTag(String mNFCtag) {
        mCapturedNFCTag = mNFCtag;
        strBarCodeSearch = "ALL";
        updateList(-1, mSelectedStandardListBO);
    }

    /**
     * Method that to loaded values and set into arraylist and adapter
     */
    private void loadedItem() {
        ReasonMaster reason = new ReasonMaster();
        ReasonMaster reason1 = new ReasonMaster();
        reason.setReasonID(Integer.toString(0));
        reason.setReasonDesc("" + getResources().getString(R.string.select_reason));
        reason1.setReasonID(Integer.toString(0));
        reason1.setReasonDesc("Select " + getResources().getString(R.string.condition));


        mPOSMReasonList = bmodel.assetTrackingHelper.getPOSMReasonList();
        mPOSMReasonList.add(0, reason);

      /*  mAssetRemarksList = bmodel.assetTrackingHelper.getAssetRemarksList();
        mAssetRemarksList.add(0, reason);*/

        mPOSMConditionList = bmodel.assetTrackingHelper.getmPOSMconditionList();
        mPOSMConditionList.add(0, reason1);

        mPOSMReasonSpinAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mPOSMReasonList);
        mPOSMReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

      /*  mAssetRemarksSpinAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout,
                mAssetRemarksList);
        mAssetRemarksSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);*/

        mPOSMConditionAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout, mPOSMConditionList);
        mPOSMConditionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

    }


    private void updateList(int bid, StandardListBO standardListBO) {
        myList = new ArrayList<>();
        mAssetTrackingList = standardListBO.getAssetTrackingList();
        if (mAssetTrackingList != null) {
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if ("ALL".equals(strBarCodeSearch)) {
                    if ("".equals(mCapturedNFCTag)) {
                        if ((bid == -1 && "Brand".equals(brandbutton)) || bid == assetBO.getProductid()) {
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
     * Method that to refresh item in listview
     */
    private void refreshList() {

        MyAdapter adapter = new MyAdapter(myList);
        lvwplist.setAdapter(adapter);

    }

    /**
     * This is ListView(lvwplist) adapter class
     */

    private class MyAdapter extends BaseAdapter {
        private final ArrayList<AssetTrackingBO> items;

        /**
         * One arg constructor method
         *
         * @param --ArrayList <AssetTrackingBO>
         */

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
                        R.layout.row_posm, parent, false);

                row.setTag(holder);

                holder.audit = (ImageButton) row
                        .findViewById(R.id.btn_audit);
                holder.assetNameTV = (TextView) row
                        .findViewById(R.id.tv_asset_name);
                holder.targetTV = (TextView) row
                        .findViewById(R.id.tv_target);
                holder.availQtyET = (EditText) row
                        .findViewById(R.id.edit_aviail_qty);
                holder.reason1Spin = (Spinner) row
                        .findViewById(R.id.spin_reason1);

                holder.reason1Spin.setAdapter(mPOSMReasonSpinAdapter);
              /*  holder.reason2Spin = (Spinner) row
                        .findViewById(R.id.spin_reason2);*/
                //  holder.reason2Spin.setAdapter(mAssetRemarksSpinAdapter);
                holder.mconditionSpin = (Spinner) row
                        .findViewById(R.id.spin_condition);
                holder.mconditionSpin.setAdapter(mPOSMConditionAdapter);
                holder.minstalldate = (Button) row
                        .findViewById(R.id.Btn_instal_Date);
                holder.mservicedate = (Button) row
                        .findViewById(R.id.Btn_service_Date);
                holder.photoBTN = (ImageView) row
                        .findViewById(R.id.btn_photo);

                holder.compQtyET = (EditText) row.findViewById(R.id.edit_competitor_qty);
                holder.execQtyET = (EditText) row.findViewById(R.id.edit_exe_qty);
                holder.grpTV = (TextView) row.findViewById(R.id.tv_grp);


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
                        if (!"".equals(qty)) {
                            int scqty = SDUtil.convertToInt(holder.availQtyET
                                    .getText().toString());
                            holder.assetBO.setAvailQty(scqty);

                            if (holder.assetBO.getAvailQty() > 0) {

                                holder.reason1Spin.setEnabled(false);
                                holder.reason1Spin.setSelection(0);
                                if ((holder.assetBO.getImageName() != null)
                                        && (!"".equals(holder.assetBO.getImageName()))
                                        && (!"null".equals(holder.assetBO.getImageName()))) {
                                    holder.photoBTN.setEnabled(true);
                                    setPictureToImageView(holder.assetBO.getImageName(), holder.photoBTN);
                                } else {
                                    //No Image Found So, setting Default Icon
                                    holder.photoBTN.setEnabled(true);
                                    holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                                }
                                holder.mconditionSpin.setEnabled(true);
                                holder.mconditionSpin.setSelection(0);
                                holder.minstalldate.setEnabled(true);
                                holder.mservicedate.setEnabled(true);

                            } else {

                                holder.reason1Spin.setEnabled(true);
                                holder.photoBTN.setEnabled(false);
                                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                                holder.mconditionSpin.setEnabled(false);
                                holder.mconditionSpin.setSelection(0);
                                holder.minstalldate.setEnabled(false);
                                holder.mservicedate.setEnabled(false);
                                //  holder.assetBO.setImageName("");
                                //holder.assetBO.setImgName("");
                                holder.assetBO.setMinstalldate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                                holder.assetBO.setMservicedate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                                holder.minstalldate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                                holder.mservicedate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));

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
                        holder.availQtyET.selectAll();
                        holder.availQtyET.requestFocus();
                        return true;
                    }
                });
                holder.execQtyET.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        qtyEditText = holder.execQtyET;
                        qtyEditText.setTag(holder.execQtyET);
                        int inType = holder.execQtyET.getInputType();
                        holder.execQtyET.setInputType(InputType.TYPE_NULL);
                        holder.execQtyET.onTouchEvent(motionEvent);
                        holder.execQtyET.setInputType(inType);
                        holder.execQtyET.selectAll();
                        holder.execQtyET.requestFocus();

                        return true;
                    }
                });
                holder.compQtyET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (!"".equals(qty)) {
                            int scqty = SDUtil.convertToInt(holder.compQtyET
                                    .getText().toString());
                            holder.assetBO.setCompetitorQty(scqty);
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
                        holder.compQtyET.selectAll();
                        holder.compQtyET.requestFocus();
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
                                // TODO Auto-generated method stub

                            }
                        });
                holder.mconditionSpin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {
                                ReasonMaster reasonBO = (ReasonMaster) holder.mconditionSpin
                                        .getSelectedItem();

                                holder.assetBO.setConditionID(reasonBO
                                        .getConditionID());
                                holder.assetBO.setReasonDesc(reasonBO
                                        .getReasonDesc());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                // TODO Auto-generated method stub

                            }
                        });

                holder.minstalldate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.minstalldate;
                        dateBtn.setTag(holder.assetBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getActivity()
                                .getSupportFragmentManager(), "datePicker1");
                    }
                });
                holder.mservicedate.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dateBtn = holder.mservicedate;
                        dateBtn.setTag(holder.assetBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getActivity()
                                .getSupportFragmentManager(), "datePicker2");
                    }
                });

              /*  holder.reason2Spin
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {
                                ReasonMaster reasonBO = (ReasonMaster) holder.reason2Spin
                                        .getSelectedItem();

                                holder.assetBO.setRemarkID(reasonBO
                                        .getReasonID());

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                // TODO Auto-generated method stub

                            }
                        });*/
                holder.photoBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (bmodel.synchronizationHelper
                                .isExternalStorageAvailable()) {
                            photoPath = getActivity().getExternalFilesDir(
                                    Environment.DIRECTORY_PICTURES)
                                    + "/" + DataMembers.photoFolderName + "/";

                            imageName = moduleName
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.assetBO.getAssetID() + "_"
                                    + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            String fnameStarts = moduleName
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID() + "_" + mSelectedStandardListBO.getListID() + "_"
                                    + holder.assetBO.getAssetID() + "_"
                                    + Commons.now(Commons.DATE);
                            Commons.print(TAG + ",FName Starts :" + fnameStarts);
                            bmodel.assetTrackingHelper.mSelectedAssetID = holder.assetBO
                                    .getAssetID();
                            bmodel.assetTrackingHelper.mSelectedImageName = imageName;
                            boolean nFilesThere = bmodel.checkForNFilesInFolder(photoPath, 1,
                                    fnameStarts);

                            if (nFilesThere) {
//                                showFileDeleteAlert(holder.assetBO.getAssetID()
//                                        + "", fnameStarts);
                                showFileDeleteAlertWithImage(holder.assetBO.getAssetID()
                                        + "", fnameStarts, holder.assetBO.getImageName());
                            } else {
                                captureCustom();
                            }

                        } else {
                            Toast.makeText(getActivity(),
                                    "SDCard Not Available.", Toast.LENGTH_SHORT)
                                    .show();
                            getActivity().finish();
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
            holder.reason1Spin.setSelection(bmodel.assetTrackingHelper
                    .getItemIndex(holder.assetBO.getReason1ID(), mPOSMReasonList));

           /* holder.reason2Spin.setSelection(bmodel.assetTrackingHelper
                    .getItemIndex(holder.assetBO.getRemarkID(),
                            mAssetRemarksList));*/
            String availQty = holder.assetBO.getAvailQty() + "";
            holder.availQtyET.setText(availQty);
            String competitorQty = holder.assetBO.getCompetitorQty() + "";
            holder.compQtyET.setText(competitorQty);
            String strTarget = holder.assetBO.getTarget() + "";
            holder.targetTV.setText(strTarget);

            holder.minstalldate
                    .setText((holder.assetBO.getMinstalldate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getMinstalldate());
            holder.mservicedate
                    .setText((holder.assetBO.getMservicedate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    outPutDateFormat) : holder.assetBO
                            .getMservicedate());
            holder.grpTV.setText(holder.assetBO.getGroupLevelName());

            //First time when screen apprears
            if (holder.assetBO.getAvailQty() > 0) {


                holder.reason1Spin.setEnabled(false);
                holder.photoBTN.setEnabled(true);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                holder.reason1Spin.setSelection(0);
                holder.mconditionSpin.setEnabled(true);
                holder.mconditionSpin.setSelection(bmodel.assetTrackingHelper
                        .getItemIndex(holder.assetBO.getConditionID(),
                                mPOSMConditionList));
                holder.minstalldate.setEnabled(true);
                holder.mservicedate.setEnabled(true);

            } else {

                holder.reason1Spin.setEnabled(true);
                holder.photoBTN.setEnabled(false);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.mconditionSpin.setEnabled(false);
                holder.mconditionSpin.setSelection(0);
                holder.minstalldate.setEnabled(false);
                holder.mservicedate.setEnabled(false);
                holder.assetBO.setImageName("");
                holder.assetBO.setImgName("");
                holder.assetBO.setMinstalldate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.assetBO.setMservicedate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.minstalldate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.mservicedate.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
            }


            if (!bmodel.assetTrackingHelper.SHOW_POSM_TARGET) {
                holder.targetTV.setVisibility(View.GONE);
            }

            if (!bmodel.assetTrackingHelper.SHOW_POSM_QTY) {
                holder.availQtyET.setVisibility(View.GONE);
            }


            if (!bmodel.assetTrackingHelper.SHOW_POSM_REASON) {
                holder.reason1Spin.setVisibility(View.GONE);
            }
            if (!bmodel.assetTrackingHelper.SHOW_POSM_CONDITION) {
                holder.mconditionSpin.setVisibility(View.GONE);
            }
            if (!bmodel.assetTrackingHelper.SHOW_POSM_INSTALL_DATE) {
                row.findViewById(R.id.ll_instal_date).setVisibility(View.GONE);
            }
            if (!bmodel.assetTrackingHelper.SHOW_POSM_SERVICE_DATE) {
                row.findViewById(R.id.ll_service_date).setVisibility(View.GONE);
            }

            if (!bmodel.assetTrackingHelper.SHOW_POSM_PHOTO) {
                holder.photoBTN.setVisibility(View.GONE);
            }
            if (!bmodel.assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY) {
                holder.compQtyET.setVisibility(View.GONE);
            }
            if (!bmodel.assetTrackingHelper.SHOW_POSM_GRP) {
                holder.grpTV.setVisibility(View.GONE);
            }

            if (!bmodel.assetTrackingHelper.SHOW_POSM_EXECUTED) {
                holder.execQtyET.setVisibility(View.GONE);
            }


            if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                holder.audit.setVisibility(View.VISIBLE);
                holder.availQtyET.setEnabled(false);
                holder.reason1Spin.setEnabled(false);
                holder.reason1Spin.setClickable(false);
                holder.photoBTN.setEnabled(false);
                holder.photoBTN.setClickable(false);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.mconditionSpin.setEnabled(false);
                holder.minstalldate.setEnabled(false);
                holder.minstalldate.setClickable(false);
                holder.mservicedate.setEnabled(false);
                holder.mservicedate.setClickable(false);
                holder.compQtyET.setEnabled(false);
            }

            if ((holder.assetBO.getImageName() != null)
                    && (!"".equals(holder.assetBO.getImageName()))
                    && (!"null".equals(holder.assetBO.getImageName()))) {
//                Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera_blue_24dp);
                setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);
//                Glide.with(getActivity()).load(
//                        getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//                                + "/" + DataMembers.photoFolderName + "/" + holder.assetBO.getImageName())
//                        .centerCrop()
//                        .placeholder(new BitmapDrawable(getResources(), defaultIcon))
//                        .error(R.drawable.no_image_available)
//                        .override(35,20)
////                        .transform(new CircleTransform(getContext()))
//                       .transform(bmodel.circleTransform)
//                        .into(holder.photoBTN) ;
//                        {
//                    @Override
//                    protected void setResource(Bitmap resource) {
//                       holder.photoBTN.setImageDrawable(new BitmapDrawable(getResources(), bmodel.getCircularBitmapFrom(resource)));
//                    }
//                });

            } else {
                if (!holder.photoBTN.isEnabled())
                    holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_grey_24dp));
                else
                    holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_photo_camera_blue_24dp));
            }


        /*    TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }*/

            return row;
        }
    }

    private void setPictureToImageView(String imageName, ImageView imageView) {
        Glide.with(getActivity()).load(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + "/" + DataMembers.photoFolderName + "/" + imageName)
                .centerCrop()
                .placeholder(R.drawable.ic_photo_camera_blue_24dp)
                .error(R.drawable.no_image_available)
                .override(35, 20)
//                        .transform(new CircleTransform(getContext()))
                .transform(bmodel.circleTransform)
                .into(imageView);
    }

    class ViewHolder {
        AssetTrackingBO assetBO;
        TextView assetNameTV;
        TextView targetTV;
        EditText availQtyET;
        EditText compQtyET;
        EditText execQtyET;
        Spinner reason1Spin;
        //  Spinner reason2Spin;
        Spinner mconditionSpin;
        ImageView photoBTN;
        Button minstalldate;
        Button mservicedate;
        ImageButton audit;
        TextView grpTV;
    }


    private void mDialog1() {
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

        bmodel.applyAlertDialogTheme(alertDialogBuilder1);
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }


    public void numberPressed(View vw) {
        if (qtyEditText == null) {
            bmodel.showAlert(
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
                    Button ed = (Button) getView().findViewById(vw.getId());
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
     * @param bbid
     * @param imageNameStarts
     */
    private void showFileDeleteAlertWithImage(final String bbid,
                                              final String imageNameStarts,
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
                new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        ArrayList<AssetTrackingBO> items = bmodel.assetTrackingHelper
                                .getAssetTrackingList();

                        for (AssetTrackingBO assetBO : items) {
                            if (bbid.equals(Integer.toString(assetBO.getAssetID()))) {
                                assetBO.setImageName("");
                                assetBO.setImgName("");
                            }
                        }
                        bmodel.assetTrackingHelper
                                .deleteImageName(imageNameStarts);
                        bmodel.synchronizationHelper.deleteFiles(photoPath,
                                imageNameStarts);

                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String _path = photoPath + "/" + imageName;
                        intent.putExtra("path", _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
//                dialog.dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    private void showFileDeleteAlert(final String bbid,
                                     final String imageNameStarts) {

        AlertDialog.Builder builderDialog = new AlertDialog.Builder(
                getActivity());
        builderDialog.setTitle("");
        builderDialog.setMessage(getResources().getString(R.string.word_already)
                + " " + 1
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builderDialog.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<AssetTrackingBO> items = bmodel.assetTrackingHelper
                                .getAssetTrackingList();

                        for (AssetTrackingBO assetBO : items) {
                            if (bbid.equals(Integer.toString(assetBO.getAssetID()))) {
                                assetBO.setImageName("");
                            }
                        }
                        bmodel.assetTrackingHelper
                                .deleteImageName(imageNameStarts);
                        bmodel.synchronizationHelper.deleteFiles(photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String _path = photoPath + "/" + imageName;
                        intent.putExtra("path", _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                });

        builderDialog.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderDialog.setCancelable(false);
        bmodel.applyAlertDialogTheme(builderDialog);
    }

    /**
     * Method to set captured image name in AssetTrackingBO
     *
     * @param assetID
     * @param --imageName
     */

    private void onsaveImageName(int assetID, String imgName) {

        String imagePath = "Asset/"
                + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imgName;
        for (AssetTrackingBO assetBO : mAssetTrackingList) {
            if (assetID == assetBO.getAssetID()) {
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
                Commons.print("AssetTracking," +
                        "Camers Activity : Sucessfully Captured.");
                if (bmodel.assetTrackingHelper.mSelectedAssetID != 0) {
                    onsaveImageName(
                            bmodel.assetTrackingHelper.mSelectedAssetID,
                            bmodel.assetTrackingHelper.mSelectedImageName);
                }
            } else {
                Commons.print("AssetTracking," + "Camers Activity : Canceled");
            }
        } else {

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    strBarCodeSearch = result.getContents();
                }
            }
        }
    }

    public void deleteUnusedImages() {

        for (AssetTrackingBO temp : myList) {
            if (temp.getAvailQty() == 0 && !"".equals(temp.getImageName())) {
                String fileName = temp.getImageName();
                Commons.print("Image Delete," + "Coming In");
                deleteFiles(fileName);
            }
        }
    }

    private void deleteFiles(String filename) {
        File folder = new File(HomeScreenFragment.photoPath + "/");

        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Sucess");
            }
        }
    }

    /**
     * This AsynTask class is used to save Asset Details in table
     */
    private class SaveAsset extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            deleteUnusedImages();
            bmodel.assetTrackingHelper.saveAsset(screenCode);
            bmodel.saveModuleCompletion(screenCode);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));

            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    null, new CommonDialog.positiveOnClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    Intent intent = new Intent();
                    if (screenCode.equals(MENU_POSM)) {
                        intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                        }
                    } else {
                        intent = new Intent(getActivity(),
                                HomeScreenActivity.class);
                        intent.putExtra("menuCode", "MENU_COUNTER");
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
     * Method to call saveAsset funticon while click nextButton in action bar
     */
    private void nextButtonClick() {
        if (bmodel.assetTrackingHelper.hasAssetTaken()) {
            new SaveAsset().execute("");
        } else {
            mDialog1();
        }
    }

    /**
     * Method that to show visibility and hided column
     */

    private void hideAndSeeK() {
        View view = getView();
        if (view != null && (!bmodel.assetTrackingHelper.SHOW_POSM_TARGET && !bmodel.assetTrackingHelper.SHOW_POSM_QTY)) {
            view.findViewById(R.id.keypad).setVisibility(View.GONE);
        }
        if (view != null && bmodel.assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY)
            view.findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        if (view != null && bmodel.configurationMasterHelper.IS_TEAMLEAD)
            getView().findViewById(R.id.keypad).setVisibility(View.GONE);
        if (!bmodel.assetTrackingHelper.SHOW_POSM_TARGET && view != null) {
            view.findViewById(R.id.tv_header_target).setVisibility(View.GONE);
        } else {
            try {
                if (view != null && bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_target).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_target))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_target).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (view != null && !bmodel.assetTrackingHelper.SHOW_POSM_QTY) {
            view.findViewById(R.id.tv_header_qty).setVisibility(View.GONE);
        } else {
            try {
                if (view != null && bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_header_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_header_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_header_qty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }


        try {
            if (view != null && bmodel.labelsMasterHelper.applyLabels(getView().findViewById(
                    R.id.tv_header_assetname).getTag()) != null) {
                ((TextView) view.findViewById(R.id.tv_header_assetname))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.tv_header_assetname).getTag()));

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        if (view != null && !bmodel.assetTrackingHelper.SHOW_POSM_COMPETITOR_QTY)
            view.findViewById(R.id.tv_competitor_qty).setVisibility(View.GONE);
        else {

            try {
                if (view != null && bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_competitor_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_competitor_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_competitor_qty).getTag()));
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (view != null && !bmodel.assetTrackingHelper.SHOW_POSM_EXECUTED)
            view.findViewById(R.id.tv_exeuted_qty).setVisibility(View.GONE);
        else {

            try {
                if (view != null && bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.tv_exeuted_qty).getTag()) != null) {
                    ((TextView) view.findViewById(R.id.tv_exeuted_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.tv_exeuted_qty).getTag()));

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (!bmodel.assetTrackingHelper.SHOW_POSM_BARCODE)
            btnBarcode.setVisibility(View.GONE);

    }

    private void captureCustom() {
        final String ACTION_SCANNERINPUTPLUGIN = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
        final String EXTRA_PARAMETER = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
        final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
        try {
            Intent i = new Intent();
            i.setAction(ACTION_SCANNERINPUTPLUGIN);
            i.putExtra(EXTRA_PARAMETER, DISABLE_PLUGIN);
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

    private void switchProfile() {
        final String switchToProfile = "com.motorolasolutions.emdk.datawedge.api.ACTION_SWITCHTOPROFILE";
        final String extraData = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PROFILENAME";
        Intent i = new Intent();
        i.setAction(switchToProfile);
        // add additional info
        i.putExtra(extraData, "dist_asset");
        getActivity().sendBroadcast(i);
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            AssetTrackingBO bo = (AssetTrackingBO) dateBtn.getTag();
            if ("datePicker1".equals(this.getTag())) {

                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setMinstalldate(DateUtil.convertDateObjectToRequestedFormat(
                            Calendar.getInstance().getTime(), outPutDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {

                    bo.setMinstalldate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else if ("datePicker2".equals(this.getTag())) {

                if (bo.getMinstalldate() != null
                        && bo.getMinstalldate().length() > 0) {
                    Date installdate = DateUtil.convertStringToDateObject(
                            bo.getMinstalldate(), outPutDateFormat);
                    if (installdate != null && selectedDate.getTime() != null
                            && installdate.after(selectedDate.getTime())) {
                        Toast.makeText(getActivity(),
                                R.string.servicedate_set_after_installdate,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        bo.setMservicedate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                } else {

                    bo.setMservicedate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            }
        }
    }

    private void eff(int val) {
        if (mSNO != null && mSNO.getText() != null) {
            String s = mSNO.getText().toString();

            if ("0".equals(s) || "0.0".equals(s)) {

                mSNO.setText(String.valueOf(val));
            } else {
                String serialNO = mSNO.getText() + String.valueOf(val);
                mSNO.setText(serialNO);
            }
        }
    }


    private void loadBrandData() {
        ArrayAdapter<CharSequence> massetbrandsadapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        massetbrandsadapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Vector vbrand = bmodel.assetTrackingHelper.getassetbrandNames();
        if (vbrand == null) {
            mbrand.setAdapter(null);
            massetbrandsadapter.add(SELECT);
            mbrand.setAdapter(massetbrandsadapter);
            return;
        }
        int vbrandsiz = vbrand.size();
        if (vbrandsiz == 0)
            return;

        massetbrandsadapter.add(SELECT);

        for (int i = 0; i < vbrandsiz; ++i) {

            massetbrandsadapter.add(vbrand.elementAt(i).toString());

        }
        mbrand.setAdapter(massetbrandsadapter);

    }


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
            bundle.putString("filterHeader", bmodel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            bundle.putString("isFrom", "Survey");
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getRetailerModuleChildLevelBO());

            if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null
                    && bmodel.productHelper.getRetailerModuleParentLeveBO()
                    .size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0)
                        .getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        brandbutton = filtertext;
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
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void updategeneraltext(String filtertext) {
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updatebrandtext(BRAND, mSelectedLastFilterSelection);
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        myList = new ArrayList<>();
        mAssetTrackingList = mSelectedStandardListBO.getAssetTrackingList();
        for (LevelBO levelBO : parentidList) {
            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (levelBO.getProductID() == assetBO.getProductid()) {
                    if ("ALL".equals(strBarCodeSearch)) {
                        if ("".equals(mCapturedNFCTag)) {
                            if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductid()) {
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
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        myList = new ArrayList<>();
        mAssetTrackingList = mSelectedStandardListBO.getAssetTrackingList();
        brandbutton = filtertext;
        if (mAssetTrackingList == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
            for (LevelBO levelBO : parentidList) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (levelBO.getProductID() == assetBO.getProductid()) {
                        if ("ALL".equals(strBarCodeSearch)) {
                            if ("".equals(mCapturedNFCTag)) {
                                if ((mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductid())
                                        && mAttributeProducts.contains(assetBO.getProductid())) {
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
        } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
            if (mSelectedIdByLevelId.size() == 0 || bmodel.isMapEmpty(mSelectedIdByLevelId)) {
                myList.addAll(mAssetTrackingList);
            } else {
                for (LevelBO levelBO : parentidList) {
                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        if (levelBO.getProductID() == assetBO.getProductid()) {
                            if ("ALL".equals(strBarCodeSearch)) {
                                if ("".equals(mCapturedNFCTag)) {
                                    if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductid()) {
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
        } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
            for (int pid : mAttributeProducts) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (pid == assetBO.getProductid()) {
                        if ("ALL".equals(strBarCodeSearch)) {
                            if ("".equals(mCapturedNFCTag)) {
                                if (mSelectedLastFilterSelection == -1 || mSelectedLastFilterSelection == assetBO.getProductid()) {
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

        bmodel.applyAlertDialogTheme(builderDialog);
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
            bundle.putString("isFrom", "posm");
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

}
