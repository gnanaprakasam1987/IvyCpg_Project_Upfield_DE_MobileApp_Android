package com.ivy.cpg.view.price;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PriceTrackFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, CompetitorFilterInterface, TextView.OnEditorActionListener, FiveLevelFilterCallBack {

    private BusinessModel businessModel;
    private PriceTrackingHelper priceTrackingHelper;
    private static final String BRAND = "Brand";
    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ListView lv;
    private ArrayList<ProductMasterBO> mylist;

    private EditText QUANTITY;
    private String append = "";

    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private ArrayAdapter<ReasonMaster> priceSpinnerAdapter;
    private HashMap<Integer, Integer> mCompetitorSelectedIdByLevelId;

    TextView tvCurPriceText, tvProdName;
    private View view;
    Button btnSave;
    FrameLayout drawer;
    private boolean isFromChild;
    private int filteredPid;
    private ArrayList<Integer> mAttributeProducts;
    private String filtertext;

    private ViewFlipper viewFlipper;
    private EditText mEdt_searchProductName;
    private Button mBtn_Search;
    private Button mBtn_clear;
    private Button mBtnFilterPopup;

    public String generalButton;
    public final String GENERAL = "General";
    public String strBarCodeSearch = "ALL";
    private TextView tvIsChanged, tvCompliance, tvPriceTagAvailability, tvCa, tvPc, tvOo;
    private LinearLayout ll_curPrice;
    private InputMethodManager inputManager;
    private TextView productName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_price_tracking, container,
                false);

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        //setting drawer width equal to scren width
        drawer = view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        initialiseViews(view);
        return view;
    }

    public void initialiseViews(View view) {
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(businessModel.mSelectedActivityName);
            getActionBar().setElevation(0);
        }
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                if (getActionBar() != null) {
                    setScreenTitle(businessModel.mSelectedActivityName);
                }
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().invalidateOptionsMenu();
            }
        };

        inputManager = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        priceTrackingHelper.prepareAdapters();

        viewFlipper = view.findViewById(R.id.view_flipper);
        productName = view.findViewById(R.id.productName);

        mEdt_searchProductName = view.findViewById(
                R.id.edt_searchproductName);
        mBtn_Search = view.findViewById(R.id.btn_search);
        mBtn_Search.setOnClickListener(this);
        mBtn_clear = view.findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);
        mBtnFilterPopup = view.findViewById(R.id.btn_filter_popup);
        mBtnFilterPopup.setOnClickListener(this);

        tvProdName = view.findViewById(R.id.sku);

        tvIsChanged = view.findViewById(R.id.changed);
        tvCompliance = view.findViewById(R.id.compliance);

        tvPriceTagAvailability = view.findViewById(R.id.priceTagAvailability);

        tvCurPriceText = view.findViewById(R.id.curtext);
        mEdt_searchProductName.setOnEditorActionListener(this);

        mEdt_searchProductName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                getActivity().supportInvalidateOptionsMenu();

                if (s.length() >= 3 || s.length() == 0) {
                    loadSearchedList();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });


        ll_curPrice = view.findViewById(R.id.ll_cur_price);

        tvCa = view.findViewById(R.id.ca_price);
        tvPc = view.findViewById(R.id.pc_price);
        tvOo = view.findViewById(R.id.oo_price);

        lv = view.findViewById(R.id.list);
        lv.setCacheColorHint(0);
        onLoadModule();
    }

    public void hideandSeek() {
        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.ca_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.ca_price))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.ca_price).getTag()));


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.pc_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.pc_price))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.pc_price).getTag()));


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.oo_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.oo_price))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.oo_price).getTag()));


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        if (!priceTrackingHelper.SHOW_PRICE_CA
                && !priceTrackingHelper.SHOW_PRICE_PC
                && !priceTrackingHelper.SHOW_PRICE_OU) {
            ll_curPrice.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.keypad).setVisibility(View.GONE);

        }

        if (!priceTrackingHelper.SHOW_PRICE_CA) {

            tvCa.setVisibility(View.GONE);
        }
        if (!priceTrackingHelper.SHOW_PRICE_PC) {

            tvPc.setVisibility(View.GONE);
        }

        if (!priceTrackingHelper.SHOW_PRICE_OU) {

            tvOo.setVisibility(View.GONE);
        }

        if (priceTrackingHelper.SHOW_PRICE_CHANGED)
            tvIsChanged.setVisibility(View.VISIBLE);


        if (priceTrackingHelper.SHOW_PRICE_COMPLIANCE) {
            tvCompliance.setVisibility(View.VISIBLE);
            // tvReason.setVisibility(View.VISIBLE);

            try {
                if (businessModel.labelsMasterHelper.applyLabels(view
                        .findViewById(R.id.compliance).getTag()) != null)
                    ((TextView) view.findViewById(R.id.compliance))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.compliance).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (priceTrackingHelper.SHOW_PRICE_TAG_CHECK) {
            tvPriceTagAvailability.setVisibility(View.VISIBLE);
            // tvReason.setVisibility(View.VISIBLE);

            try {
                if (businessModel.labelsMasterHelper.applyLabels(view
                        .findViewById(R.id.priceTagAvailability).getTag()) != null)
                    ((TextView) view.findViewById(R.id.priceTagAvailability))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.priceTagAvailability).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }


        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        (view.findViewById(R.id.calcdot))
                .setVisibility(View.VISIBLE);
    }

    public void prepareAdapters() {
        loadReasons();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(3);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());
        priceTrackingHelper = PriceTrackingHelper.getInstance(getContext());
        setHasOptionsMenu(true);
        priceTrackingHelper.mSelectedFilter = -1;
    }

    @Override
    public void onClick(View view) {
        if (view == mBtn_Search) {
            viewFlipper.showNext();
        } else if (view == mBtn_clear) {
            if (mEdt_searchProductName.getText().length() > 0)
                mEdt_searchProductName.setText("");
            onLoadModule();

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (view == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.select_dialog_singlechoice,
                    priceTrackingHelper.mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            businessModel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = priceTrackingHelper.mSearchTypeArray.indexOf(businessModel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            businessModel.setProductFilter(arrayAdapter.getItem(which));
                        }

                    });
            builderSingle.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                        }
                    });
            businessModel.applyAlertDialogTheme(builderSingle);

        } else if (view.getId() == R.id.btn_save) {
            nextButtonClick();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        try {
            boolean drawerOpen = false;
            boolean navDrawerOpen = false;
            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (businessModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                if (businessModel.productHelper.getInStoreLocation().size() > 1
                        || !priceTrackingHelper.SHOW_PRICE_LOCATION_FILTER)
                    menu.findItem(R.id.menu_location_filter).setVisible(false);
            }

            menu.findItem(R.id.menu_spl_filter).setVisible(false);
            menu.findItem(R.id.menu_remarks).setVisible(false);

            if (businessModel.configurationMasterHelper.floating_Survey)
                menu.findItem(R.id.menu_survey).setVisible(true);
            if (businessModel.configurationMasterHelper.IS_BAR_CODE_PRICE_CHECK) {
                menu.findItem(R.id.menu_barcode).setVisible(true);
            }
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (businessModel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }
            if (businessModel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                menu.findItem(R.id.menu_competitor_filter).setVisible(true);
            }

            if (businessModel.configurationMasterHelper.SHOW_COMPETITOR_FILTER && mCompetitorSelectedIdByLevelId != null) {
                for (Integer id : mCompetitorSelectedIdByLevelId.keySet()) {
                    if (mCompetitorSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_competitor_filter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }


            }
            if (drawerOpen || navDrawerOpen)
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
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_competitor_filter) {
            competitorFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_barcode) {
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        PriceTrackFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(priceTrackingHelper.getLocationAdapter(),
                priceTrackingHelper.mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        priceTrackingHelper.mSelectedLocationIndex = item;
                        dialog.dismiss();
                        refreshList();

                    }
                });

        businessModel.applyAlertDialogTheme(builder);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                strBarCodeSearch = result.getContents();
                if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                    businessModel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                    mEdt_searchProductName.setText(strBarCodeSearch);
                    if (viewFlipper.getDisplayedChild() == 0) {
                        viewFlipper.showNext();
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    private void competitorFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.competitor_filter));

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            CompetitorFilterFragment frag = (CompetitorFilterFragment) fm
                    .findFragmentByTag("competitor filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);

            if (mSelectedIdByLevelId != null && AppUtils.isMapEmpty(mSelectedIdByLevelId) == false) {
                mCompetitorSelectedIdByLevelId = new HashMap<>();
            }

            // set Fragmentclass Arguments
            CompetitorFilterFragment fragobj = new CompetitorFilterFragment();
            Bundle b = new Bundle();
            b.putSerializable("selectedFilter", mCompetitorSelectedIdByLevelId);
            fragobj.setCompetitorFilterInterface(this);
            fragobj.setArguments(b);
            ft.replace(R.id.right_drawer, fragobj, "competitor filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(businessModel.mSelectedActivityName);
            getActionBar().setElevation(0);
        }
        hideandSeek();
        prepareAdapters();
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void nextButtonClick() {
        try {
            if (priceTrackingHelper.hasDataTosave(businessModel.productHelper.getTaggedProducts())) {
                if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1)
                    if (priceTrackingHelper.hasPriceChangeReason(businessModel.productHelper.getTaggedProducts()))
                        new SaveAsyncTask().execute();
                    else
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.select_reason_for_price_change),
                                Toast.LENGTH_LONG).show();
                else
                    new SaveAsyncTask().execute();
            } else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateCompetitorProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText) {

        this.mCompetitorSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mSelectedIdByLevelId = new HashMap<>();// clearing product filter
        this.filtertext = filterText;

        if (mylist != null) {
            mylist.clear();
        }


        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        if (parentIdList != null && !parentIdList.isEmpty()) {
            for (CompetitorFilterLevelBO mParentBO : parentIdList) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (mParentBO.getProductId() == sku.getCompParentId()) {
                        mylist.add(sku);
                    }
                }
            }
        } else {
            mylist.addAll(items);
        }


        mDrawerLayout.closeDrawers();
        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);

        getActivity().invalidateOptionsMenu();

    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                priceTrackingHelper.savePriceTransaction(getContext().getApplicationContext(), businessModel.productHelper.getTaggedProducts());
                businessModel.saveModuleCompletion(HomeScreenTwo.MENU_PRICE, true);
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
            try {
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
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


    private void onLoadModule(int filteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts) {
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        if (items == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();

        if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 0) {
            if (mAttributeProducts != null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if ((sku.getParentHierarchy().contains("/" + filteredPid + "/")) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1) &&
                            (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID())))) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts == null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if ((sku.getParentHierarchy().contains("/" + filteredPid + "/")) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts != null && filteredPid != 0) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if ((pid == SDUtil.convertToInt(sku.getProductID())) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getIsSaleable() == 1 && sku.getOwn() == 1) {
                        mylist.add(sku);
                    }
                }
            }

        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 1) {
            if (mAttributeProducts != null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if ((sku.getParentHierarchy().contains("/" + filteredPid + "/")) &&
                            (sku.getIsSaleable() == 1 && sku.getOwn() == 0) &&
                            (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID())))) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts == null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + filteredPid + "/") &&
                            (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts != null && filteredPid != 0) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if ((pid == SDUtil.convertToInt(sku.getProductID())) &&
                                (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getIsSaleable() == 1 && sku.getOwn() == 0) {
                        mylist.add(sku);
                    }
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 2) {
            if (mAttributeProducts != null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if ((sku.getParentHierarchy().contains("/" + filteredPid + "/")) &&
                            (sku.getIsSaleable() == 1) &&
                            (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID())))) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts == null && filteredPid != 0) {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if ((sku.getParentHierarchy().contains("/" + filteredPid + "/")) &&
                            (sku.getIsSaleable() == 1)) {
                        mylist.add(sku);
                    }
                }
            } else if (mAttributeProducts != null && filteredPid != 0) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if ((pid == SDUtil.convertToInt(sku.getProductID())) &&
                                (sku.getIsSaleable() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else {
                for (ProductMasterBO sku : items) {
                    if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getIsSaleable() == 1) {
                        mylist.add(sku);
                    }
                }
            }
        }
        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
        if (mSelectedIdByLevelId != null && AppUtils.isMapEmpty(mSelectedIdByLevelId) == false) {
            mCompetitorSelectedIdByLevelId = new HashMap<>();
        }


    }

    private void onLoadModule() {
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();

        if (items == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();

        if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 0) {
            for (ProductMasterBO sku : items) {
                if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1) &&
                        (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                    mylist.add(sku);
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 1) {
            for (ProductMasterBO sku : items) {
                if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1) &&
                        (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                    mylist.add(sku);
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 2) {
            for (ProductMasterBO sku : items) {
                if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + businessModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1) &&
                        (sku.getIsSaleable() == 1)) {
                    mylist.add(sku);
                }
            }
        }


        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
    }

    public void refreshList() {
        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
    }

    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : businessModel.reasonHelper.getReasonList()) {
            if ("POR".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }
        priceSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        priceSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : businessModel.reasonHelper.getReasonList()) {
            if ("PRC".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                priceSpinnerAdapter.add(temp);
        }

    }

    private int getReasonIndex(String reasonId) {
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

    private int getPriceReasonIndex(String reasonId) {
        if (priceSpinnerAdapter.getCount() == 0)
            return 0;
        int len = priceSpinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = priceSpinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    class ViewHolder {
        ProductMasterBO mSKUBO;
        TextView mBarCode, mSrp;
        TextView mSKU, mPrev_CA, mPrev_PC, mPrev_OO, tv_prev_mrp_pc, tv_prev_mrp_ca, tv_prev_mrp_ou;
        TextView mPrev_CA_label, mPrev_PC_label, mPrev_OO_label, tv_prev_mrp_pc_label, tv_prev_mrp_ca_label, tv_prev_mrp_ou_label;
        CheckBox mChanged, mCompliance, checkbox_pricetag;
        EditText mCaPrice, mPcPrice, mOoPrice;
        Spinner mReason, mReason_price_change;
        RelativeLayout rl_PriceChanged, rl_PriceCompliance, rl_PriceTag;
        TextView mProductCodeTV;

        LinearLayout ll_prev_case, ll_prev_oo, ll_prev_pc, ll_prev_price_Lty;
        LinearLayout ll_prev_mrp_main_Lty, ll_prev_mrp_ca_Lty, ll_prev_mrp_oo_Lty, ll_prev_mrp_pc_Lty;
        String srpText;
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_price_tracking, items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
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

                row = inflater.inflate(
                        R.layout.row_price_tracking, parent, false);

                holder.mBarCode = row
                        .findViewById(R.id.barcode);
                holder.mSKU = row.findViewById(R.id.sku);
                holder.mSKU.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.mSrp = row.findViewById(R.id.tv_srp);

                holder.rl_PriceChanged = row
                        .findViewById(R.id.rl_PriceChanged);

                holder.mChanged = row
                        .findViewById(R.id.changed);

                holder.mPrev_CA = row
                        .findViewById(R.id.prev_ca);
                holder.mPrev_PC = row
                        .findViewById(R.id.prev_pc);
                holder.mPrev_OO = row
                        .findViewById(R.id.prev_oo);

                holder.mPrev_CA_label = row
                        .findViewById(R.id.prev_ca_label);
                holder.mPrev_PC_label = row
                        .findViewById(R.id.prev_pc_label);
                holder.mPrev_OO_label = row
                        .findViewById(R.id.prev_oo_label);


                holder.mCaPrice = row
                        .findViewById(R.id.caprice);
                holder.mCaPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.mPcPrice = row
                        .findViewById(R.id.pcprice);
                holder.mPcPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.mOoPrice = row
                        .findViewById(R.id.ooprice);
                holder.mOoPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.rl_PriceCompliance = row
                        .findViewById(R.id.rl_PriceCompliance);

                holder.rl_PriceTag = row
                        .findViewById(R.id.rl_price_tag);


                holder.mCompliance = row
                        .findViewById(R.id.compliance);

                holder.checkbox_pricetag = row
                        .findViewById(R.id.checkbox_pricetag);

                holder.mReason = row
                        .findViewById(R.id.reason);
                holder.mReason_price_change = row
                        .findViewById(R.id.reason_pc);

                holder.mProductCodeTV = row
                        .findViewById(R.id.prdcode_tv);

                holder.tv_prev_mrp_pc = row.findViewById(R.id.tv_prev_mrp_pc);
                holder.tv_prev_mrp_ca = row.findViewById(R.id.tv_prev_mrp_ca);
                holder.tv_prev_mrp_ou = row.findViewById(R.id.tv_prev_mrp_oo);

                holder.tv_prev_mrp_ca_label = row.findViewById(R.id.prev_mrp_ca_label);
                holder.tv_prev_mrp_ou_label = row.findViewById(R.id.prev_mrp_oo_label);
                holder.tv_prev_mrp_pc_label = row.findViewById(R.id.prev_mrp_pc_label);


                holder.ll_prev_case = row.findViewById(R.id.ll_prev_price_ca);
                holder.ll_prev_pc = row.findViewById(R.id.ll_prev_price_pc);
                holder.ll_prev_oo = row.findViewById(R.id.ll_prev_price_oo);
                holder.ll_prev_price_Lty = row.findViewById(R.id.ll_prev_price);
                holder.ll_prev_mrp_main_Lty = row.findViewById(R.id.ll_prev_mrp);
                holder.ll_prev_mrp_pc_Lty = row.findViewById(R.id.ll_prev_mrp_pc);
                holder.ll_prev_mrp_ca_Lty = row.findViewById(R.id.ll_prev_mrp_ca);
                holder.ll_prev_mrp_oo_Lty = row.findViewById(R.id.ll_prev_mrp_oo);

                if (priceTrackingHelper.SHOW_PREV_MRP_IN_PRICE) {
                    if (priceTrackingHelper.SHOW_PRICE_PC)
                        holder.ll_prev_mrp_pc_Lty.setVisibility(View.VISIBLE);
                    if (priceTrackingHelper.SHOW_PRICE_CA)
                        holder.ll_prev_mrp_ca_Lty.setVisibility(View.VISIBLE);
                    if (priceTrackingHelper.SHOW_PRICE_OU)
                        holder.ll_prev_mrp_oo_Lty.setVisibility(View.VISIBLE);
                } else if (!priceTrackingHelper.SHOW_PREV_MRP_IN_PRICE) {
                    holder.ll_prev_mrp_main_Lty.setVisibility(View.GONE);
                }

                holder.mChanged.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceChanged() == 1) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceChanged(0);
                            holder.mChanged.setChecked(false);
                            holder.mCaPrice.setEnabled(false);
                            holder.mPcPrice.setEnabled(false);
                            holder.mOoPrice.setEnabled(false);

                            if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1) {
                                holder.mReason_price_change.setEnabled(false);
                                holder.mReason_price_change.setSelected(false);
                                holder.mReason_price_change.setSelection(0);
                                holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceChangeReasonID("0");
                            }


                            if (businessModel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE) {
                                holder.mCaPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_ca());
                                holder.mCaPrice.clearFocus();

                                holder.mPcPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_pc());
                                holder.mPcPrice.clearFocus();

                                holder.mOoPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_oo());
                                holder.mOoPrice.clearFocus();

                                QUANTITY = null;
                            } else {
                                holder.mCaPrice.setText("0");
                                holder.mCaPrice.clearFocus();
                                if (QUANTITY == holder.mCaPrice)
                                    QUANTITY = null;
                                holder.mPcPrice.setText("0");
                                holder.mPcPrice.clearFocus();
                                if (QUANTITY == holder.mPcPrice)
                                    QUANTITY = null;
                                holder.mOoPrice.setText("0");
                                holder.mOoPrice.clearFocus();
                                if (QUANTITY == holder.mOoPrice)
                                    QUANTITY = null;

                            }


                        } else {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceChanged(1);
                            holder.mChanged.setChecked(true);

                            if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1) {
                                holder.mReason_price_change.setEnabled(true);
                                holder.mReason_price_change.setSelected(true);
                                holder.mReason_price_change.setSelection(0);
                                holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceChangeReasonID("0");
                            }

                            if (holder.mSKUBO.getOuUomid() == 0 || !holder.mSKUBO.isOuterMapped()) {
                                holder.mOoPrice.setEnabled(false);
                            } else {
                                holder.mOoPrice.setEnabled(true);
                            }
                            if (holder.mSKUBO.getCaseUomId() == 0 || !holder.mSKUBO.isCaseMapped()) {
                                holder.mCaPrice.setEnabled(false);
                            } else {
                                holder.mCaPrice.setEnabled(true);
                            }
                            if (holder.mSKUBO.getPcUomid() == 0 || !holder.mSKUBO.isPieceMapped()) {
                                holder.mPcPrice.setEnabled(false);
                            } else {
                                holder.mPcPrice.setEnabled(true);
                            }
                        }
                    }
                });

                holder.mCaPrice.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.mCaPrice.setSelection(qty.length());
                        if (SDUtil.isValidDecimal(qty, 8, businessModel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION)) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPrice_ca(qty);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getResources().getString(
                                            R.string.invalid_price),
                                    Toast.LENGTH_SHORT).show();
                            holder.mCaPrice.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                });

                holder.mCaPrice.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mCaPrice;
                        int inType = holder.mCaPrice.getInputType();
                        holder.mCaPrice.setInputType(InputType.TYPE_NULL);
                        holder.mCaPrice.onTouchEvent(event);
                        holder.mCaPrice.setInputType(inType);
                        holder.mCaPrice.requestFocus();
                        if (holder.mCaPrice.getText().length() > 0)
                            holder.mCaPrice.setSelection(holder.mCaPrice.getText().length());
                        return true;
                    }
                });

                holder.mPcPrice.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.mPcPrice.setSelection(qty.length());
                        if (SDUtil.isValidDecimal(qty, 8, businessModel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION)) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPrice_pc(qty);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getResources().getString(
                                            R.string.invalid_price),
                                    Toast.LENGTH_SHORT).show();
                            holder.mPcPrice.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                });

                holder.mPcPrice.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mPcPrice;
                        int inType = holder.mPcPrice.getInputType();
                        holder.mPcPrice.setInputType(InputType.TYPE_NULL);
                        holder.mPcPrice.onTouchEvent(event);
                        holder.mPcPrice.setInputType(inType);
                        holder.mPcPrice.requestFocus();
                        if (holder.mPcPrice.getText().length() > 0)
                            holder.mPcPrice.setSelection(holder.mPcPrice.getText().length());
                        return true;
                    }
                });

                holder.mOoPrice.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.mOoPrice.setSelection(qty.length());

                        if (SDUtil.isValidDecimal(qty, 8, businessModel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION)) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPrice_oo(qty);
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getResources().getString(
                                            R.string.invalid_price),
                                    Toast.LENGTH_SHORT).show();
                            holder.mOoPrice.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                });

                holder.mOoPrice.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mOoPrice;
                        int inType = holder.mOoPrice.getInputType(); // backup
                        holder.mOoPrice.setInputType(InputType.TYPE_NULL); // disable
                        holder.mOoPrice.onTouchEvent(event); // call native
                        holder.mOoPrice.setInputType(inType); // restore
                        holder.mOoPrice.requestFocus();
                        if (holder.mOoPrice.getText().length() > 0)
                            holder.mOoPrice.setSelection(holder.mOoPrice.getText().length());
                        return true;
                    }
                });

                holder.mCompliance.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceCompliance() == 1) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceCompliance(0);
                            holder.mCompliance.setChecked(false);
                            holder.mReason.setEnabled(true);
                            holder.mReason.setSelected(true);
                            holder.mReason.setSelection(0);
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setReasonId(0);
                        } else {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceCompliance(1);
                            holder.mCompliance.setChecked(true);
                            holder.mReason.setEnabled(false);
                            holder.mReason.setSelected(false);
                            holder.mReason.setSelection(0);
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setReasonId(0);
                        }
                    }
                });

                holder.checkbox_pricetag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceTagAvailability() == 1) {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceTagAvailability(0);
                            holder.checkbox_pricetag.setChecked(false);

                        } else {
                            holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceTagAvailability(1);
                            holder.checkbox_pricetag.setChecked(true);

                        }
                    }
                });


                holder.mReason.setAdapter(spinnerAdapter);
                holder.mReason
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.mReason
                                        .getSelectedItem();

                                holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setReasonId(Integer.parseInt(reString
                                        .getReasonID()));

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                holder.mReason_price_change.setAdapter(priceSpinnerAdapter);
                holder.mReason_price_change
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                ReasonMaster reString = (ReasonMaster) holder.mReason_price_change
                                        .getSelectedItem();

                                holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).setPriceChangeReasonID(reString
                                        .getReasonID());
                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                if (!priceTrackingHelper.SHOW_PRICE_LASTVP)
                    holder.ll_prev_price_Lty.setVisibility(View.GONE);

                if (!priceTrackingHelper.SHOW_PRICE_LASTVP) {
                    holder.mPrev_CA.setVisibility(View.GONE);
                    holder.mPrev_PC.setVisibility(View.GONE);
                    holder.mPrev_OO.setVisibility(View.GONE);

                }


                if (priceTrackingHelper.SHOW_PRICE_CA) {
                    if (priceTrackingHelper.SHOW_PRICE_LASTVP)
                        holder.ll_prev_case.setVisibility(View.VISIBLE);
                    holder.mCaPrice.setVisibility(View.VISIBLE);
                }
                if (priceTrackingHelper.SHOW_PRICE_PC) {
                    if (priceTrackingHelper.SHOW_PRICE_LASTVP)
                        holder.ll_prev_pc.setVisibility(View.VISIBLE);
                    holder.mPcPrice.setVisibility(View.VISIBLE);
                }
                if (priceTrackingHelper.SHOW_PRICE_OU) {
                    if (priceTrackingHelper.SHOW_PRICE_LASTVP)
                        holder.ll_prev_oo.setVisibility(View.VISIBLE);
                    holder.mOoPrice.setVisibility(View.VISIBLE);
                }

                if (priceTrackingHelper.SHOW_PRICE_SRP)
                    holder.mSrp.setVisibility(View.VISIBLE);

                if (priceTrackingHelper.SHOW_PRICE_CHANGED) {
                    holder.rl_PriceChanged.setVisibility(View.VISIBLE);
                    if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1)
                        holder.mReason_price_change.setVisibility(View.VISIBLE);
                    holder.mCaPrice.setEnabled(false);
                    holder.mPcPrice.setEnabled(false);
                    holder.mOoPrice.setEnabled(false);


                } else {
                    holder.mCaPrice.setEnabled(true);
                    holder.mPcPrice.setEnabled(true);
                    holder.mOoPrice.setEnabled(true);


                }

                if (priceTrackingHelper.SHOW_PRICE_COMPLIANCE) {
                    holder.rl_PriceCompliance.setVisibility(View.VISIBLE);
                    holder.mReason.setVisibility(View.VISIBLE);
                }

                if (priceTrackingHelper.SHOW_PRICE_TAG_CHECK) {
                    holder.rl_PriceTag.setVisibility(View.VISIBLE);
                }

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                        productName.setText(holder.mSKUBO.getProductShortName());

                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchProductName.getWindowToken(), 0);

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();
                        }
                    }
                });

                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSKUBO = items.get(position);

            holder.mBarCode.setText(holder.mSKUBO.getBarCode());
            holder.mSKU.setText(holder.mSKUBO.getProductShortName());
            try {
                if (businessModel.labelsMasterHelper.applyLabels(row.findViewById(
                        R.id.tv_srp).getTag()) != null)
                    holder.srpText = businessModel.labelsMasterHelper
                            .applyLabels(row.findViewById(
                                    R.id.tv_srp).getTag());
                else
                    holder.srpText = "SRP";

            } catch (Exception e) {
                Commons.printException(e + "");
            }
            holder.mSrp.setText(holder.srpText + ":" + String.valueOf(holder.mSKUBO.getSrp()));


            holder.mPrev_CA.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_ca())));
            holder.mPrev_PC.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_pc())));
            holder.mPrev_OO.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrevPrice_oo())));

            holder.mPrev_PC_label.setText(getResources().getString(R.string.pc) + ":");
            holder.mPrev_CA_label.setText(getResources().getString(R.string.ca) + ":");
            holder.mPrev_OO_label.setText(getResources().getString(R.string.ou) + ":");

            holder.tv_prev_mrp_pc_label.setText(getResources().getString(R.string.pc) + ":");
            holder.tv_prev_mrp_ca_label.setText(getResources().getString(R.string.ca) + ":");
            holder.tv_prev_mrp_ou_label.setText(getResources().getString(R.string.ou) + ":");

            holder.mCaPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrice_ca());
            holder.mPcPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrice_pc());
            holder.mOoPrice.setText(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPrice_oo());

            holder.mProductCodeTV.setText(holder.mSKUBO.getProductCode());


            holder.tv_prev_mrp_ca.setText(holder.mSKUBO.getPrevMRP_ca());
            holder.tv_prev_mrp_pc.setText(holder.mSKUBO.getPrevMRP_pc());
            holder.tv_prev_mrp_ou.setText(holder.mSKUBO.getPrevMRP_ou());


            holder.mReason.setSelection(getReasonIndex(String.valueOf(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getReasonId())));
            holder.mReason_price_change.setSelection(getPriceReasonIndex(holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceChangeReasonID()));

            if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceCompliance() == 1)
                holder.mCompliance.setChecked(true);

            if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceTagAvailability() == 1)
                holder.checkbox_pricetag.setChecked(true);
            else holder.checkbox_pricetag.setChecked(false);

            if (priceTrackingHelper.SHOW_PRICE_CHANGED) {
                if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceChanged() == 1) {
                    holder.mChanged.setChecked(true);

                    if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1)
                        holder.mReason_price_change.setEnabled(true);


                    if (holder.mSKUBO.getOuUomid() == 0 || !holder.mSKUBO.isOuterMapped()) {
                        holder.mOoPrice.setEnabled(false);

                    } else {
                        holder.mOoPrice.setEnabled(true);
                    }
                    if (holder.mSKUBO.getCaseUomId() == 0 || !holder.mSKUBO.isCaseMapped()) {
                        holder.mCaPrice.setEnabled(false);
                    } else {
                        holder.mCaPrice.setEnabled(true);
                    }
                    if (holder.mSKUBO.getPcUomid() == 0 || !holder.mSKUBO.isPieceMapped()) {
                        holder.mPcPrice.setEnabled(false);
                    } else {
                        holder.mPcPrice.setEnabled(true);
                    }


                } else {
                    holder.mChanged.setChecked(false);
                    holder.mCaPrice.setEnabled(false);
                    holder.mPcPrice.setEnabled(false);
                    holder.mOoPrice.setEnabled(false);
                    if (priceTrackingHelper.IS_PRICE_CHANGE_REASON == 1)
                        holder.mReason_price_change.setEnabled(false);

                }
            } else {
                if (!holder.mSKUBO.isOuterMapped()) {
                    holder.mOoPrice.setEnabled(false);
                } else {
                    holder.mOoPrice.setEnabled(true);
                }
                if (!holder.mSKUBO.isCaseMapped()) {
                    holder.mCaPrice.setEnabled(false);
                } else {
                    holder.mCaPrice.setEnabled(true);
                }
                if (!holder.mSKUBO.isPieceMapped()) {
                    holder.mPcPrice.setEnabled(false);
                } else {
                    holder.mPcPrice.setEnabled(true);
                }
            }


            if (!businessModel.configurationMasterHelper.SHOW_BARCODE) {
                holder.mBarCode.setVisibility(View.GONE);
            }
            if (!businessModel.configurationMasterHelper.SHOW_PRODUCT_CODE) {
                holder.mProductCodeTV.setVisibility(View.GONE);
            }

            if (holder.mSKUBO.getLocations().get(priceTrackingHelper.mSelectedLocationIndex).getPriceCompliance() == 1) {
                holder.mCompliance.setChecked(true);
                holder.mReason.setEnabled(false);
            } else {
                holder.mCompliance.setChecked(false);
                holder.mReason.setEnabled(true);
            }

          /*  TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }*/

            return row;
        }
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                String val = QUANTITY.getText().toString();

                if (!val.isEmpty()) {

                    val = val.substring(0, val.length() - 1);

                    if (val.length() == 0) {
                        val = "0";
                    }

                } else {
                    val = "0";
                }

                QUANTITY.setText(val);

            } else {
                Button ed = (Button) getActivity().findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            s = s + append;
            QUANTITY.setText(s);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        priceTrackingHelper.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        String generalTxt = generalButton;
        onLoadModule();
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        if (mFilterText.equals("")) {
            mFilterText = GENERAL;
        }
        generalButton = mFilterText;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mDrawerLayout.closeDrawers();
        this.filteredPid = mProductId;
        this.mAttributeProducts = mAttributeProducts;
        this.filtertext = mFilterText;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        onLoadModule(mProductId, mSelectedIdByLevelId, mAttributeProducts);
    }


    private void FiveFilterFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    businessModel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            bundle.putBoolean("isTag", true);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        priceTrackingHelper.clearInstance();
    }

    private void loadSearchedList() {
        loadSearchedList(mEdt_searchProductName.getText().toString());
    }

    public void loadSearchedList(String s) {
        ProductMasterBO ret;

        if (s.length() >= 3) {

            Vector<ProductMasterBO> items = new Vector<>();
            if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
                for (ProductMasterBO productBo : businessModel.productHelper.getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 1)
                        items.add(productBo);
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
                for (ProductMasterBO productBo : businessModel.productHelper.getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 0)
                        items.add(productBo);
                }
            } else if (businessModel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
                items = businessModel.productHelper.getTaggedProducts();
            }

            if (items.isEmpty()) {
                showAlert();
                return;
            }
            int siz = items.size();

            ArrayList<ProductMasterBO> stockList = new ArrayList<>();
            String mSelectedFilter = businessModel.getProductFilter();

            for (int i = 0; i < siz; ++i) {
                ret = items.elementAt(i);

                if (mSelectedFilter.equals(getContext().getResources().getString(
                        R.string.order_dialog_barcode))) {

                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(s.toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(s.toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(s.toLowerCase())) && ret.getIsSaleable() == 1) {

                        //if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                        stockList.add(ret);
//                        else if (applyProductAndSpecialFilter(ret))
//                            stockList.add(ret);
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.prod_code))) {
                    if (((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(s.toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(s.toLowerCase()))) && ret.getIsSaleable() == 1) {
                        stockList.add(ret);
                    }
                } else if (mSelectedFilter.equals(getContext().getResources().getString(
                        R.string.product_name))) {
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    s.toLowerCase()) && ret.getIsSaleable() == 1)
//                        if (generalButton.equalsIgnoreCase(GENERAL))//No filters selected
                        stockList.add(ret);
//                        else if (applyProductAndSpecialFilter(ret))
//                            stockList.add(ret);
                } else {
                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(s.toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(s.toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(s.toLowerCase())) && ret.getIsSaleable() == 1) {
                        stockList.add(ret);
                    } else if (((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(s.toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(s.toLowerCase()))) && ret.getIsSaleable() == 1) {
                        stockList.add(ret);
                    } else if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    s.toLowerCase()) && ret.getIsSaleable() == 1)
                        stockList.add(ret);
                }

            }

            updateListFromFilter(stockList);

        } else if (s.length() == 0) {
            onLoadModule();
        } else {
            showSearchValidationToast();
        }
    }

    public void showAlert() {
        businessModel.showAlert(getResources().getString(R.string.no_products_exists), 0);
    }

    public void updateListFromFilter(ArrayList<ProductMasterBO> stockList) {
        this.mylist = stockList;
        MyAdapter mSchedule = new MyAdapter(mylist);
        lv.setAdapter(mSchedule);
    }

    public void showSearchValidationToast() {
        Toast.makeText(getActivity(), getResources().getString(R.string.enter_atleast_three_letters), Toast.LENGTH_SHORT)
                .show();
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
//        if (!GENERAL.equals(generalButton)) {
//            // both filter selected
//            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
//                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
//                        && isSpecialFilterAppliedProduct(generalButton, ret))
//                    return true;
//            } else {
//                if (isSpecialFilterAppliedProduct(generalButton, ret))
//                    return true;
//            }
//        } else if (GENERAL.equals(generalButton)) {
//            // product filter alone selected
//            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
//                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID()))
//                    return true;
//            } else {
//                if (isSpecialFilterAppliedProduct(generalButton, ret))
//                    return true;
//            }
//        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                getActivity().supportInvalidateOptionsMenu();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEdt_searchProductName.getWindowToken(), 0);
            }
            loadSearchedList();
            return true;
        }
        return false;
    }

    private void onBackButonClick() {

        if (priceTrackingHelper.hasDataTosave(businessModel.productHelper.getTaggedProducts())) {
            showBackDialog();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showBackDialog() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
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
