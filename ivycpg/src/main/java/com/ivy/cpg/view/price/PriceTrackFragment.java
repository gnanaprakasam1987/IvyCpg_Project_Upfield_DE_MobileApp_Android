package com.ivy.cpg.view.price;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PriceTrackFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, CompetitorFilterInterface {

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

    TextView tvCurPriceText, tvProdName;
    private View view;
    Button btnSave;
    FrameLayout drawer;
    private boolean isFromChild;
    private String selectedCompetitorId = "";
    private Vector<LevelBO> parentidList;
    private ArrayList<Integer> mAttributeProducts;
    private String filtertext;

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
        drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Price Tracking");
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
        if (view.getId() == R.id.btn_save) {
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
            menu.findItem(R.id.menu_location_filter).setVisible(false);
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
            menu.findItem(R.id.menu_remarks).setVisible(false);

            if (businessModel.configurationMasterHelper.floating_Survey)
                menu.findItem(R.id.menu_survey).setVisible(true);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(false);

            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && businessModel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
             /*    else
                menu.findItem(R.id.menu_product_filter).setVisible(true);*/
            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }
            if (businessModel.productHelper.getCompetitorFilterList() != null && businessModel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                menu.findItem(R.id.menu_competitor_filter).setVisible(true);
            }

            if (businessModel.configurationMasterHelper.SHOW_COMPETITOR_FILTER && !selectedCompetitorId.equals("")) {
                menu.findItem(R.id.menu_competitor_filter).setIcon(
                        R.drawable.ic_action_filter_select);

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
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void competitorFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            CompetitorFilterFragment frag = (CompetitorFilterFragment) fm
                    .findFragmentByTag("competitor filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);


            // set Fragmentclass Arguments
            CompetitorFilterFragment fragobj = new CompetitorFilterFragment();
            Bundle b = new Bundle();
            b.putString("selectedCompetitorId", selectedCompetitorId);
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
        tvProdName = (TextView) view.findViewById(R.id.sku);
        tvProdName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView tvIsChanged = (TextView) view.findViewById(R.id.changed);
        TextView tvCompliance = (TextView) view.findViewById(R.id.compliance);
        // TextView tvReason = (TextView) view.findViewById(R.id.reason);

        tvCurPriceText = (TextView) view.findViewById(R.id.curtext);
        tvCurPriceText.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvIsChanged.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvCompliance.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        // tvReason.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        LinearLayout ll_curPrice = (LinearLayout) view.findViewById(R.id.ll_cur_price);

        TextView tvCa = (TextView) view.findViewById(R.id.ca_price);
        TextView tvPc = (TextView) view.findViewById(R.id.pc_price);
        TextView tvOo = (TextView) view.findViewById(R.id.oo_price);

        tvCa.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvPc.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvOo.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


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

        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        (view.findViewById(R.id.calcdot))
                .setVisibility(View.VISIBLE);

        lv = (ListView) view.findViewById(R.id.list);
        lv.setCacheColorHint(0);

        loadReasons();

        onLoadModule();
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void productFilterClickedFragment() {
        try {

            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            if (!businessModel.productHelper.getChildLevelBo().isEmpty())
                bundle.putString("filterHeader", businessModel.productHelper
                        .getChildLevelBo().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", businessModel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("serilizeContent",
                    businessModel.productHelper.getChildLevelBo());

            if (businessModel.productHelper.getParentLevelBo() != null
                    && !businessModel.productHelper.getParentLevelBo().isEmpty()) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", businessModel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());

                businessModel.productHelper.setPlevelMaster(businessModel.productHelper
                        .getParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);

            }


            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    private void nextButtonClick() {
        try {
            if (priceTrackingHelper.hasDataTosave(businessModel.productHelper.getTaggedProducts()))
                new SaveAsyncTask().execute();
            else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateCompetitorProducts(String filterId) {
        selectedCompetitorId = filterId;
        if (mylist != null) {
            mylist.clear();
        }

        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        if (filterId != null && !filterId.isEmpty()) {
            for (ProductMasterBO sku : items) {
                if (Integer.parseInt(filterId) == sku.getCompParentId()) {
                    mylist.add(sku);
                }
            }
        } else {
            mylist.addAll(items);
        }
        mDrawerLayout.closeDrawers();
        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
        if (!selectedCompetitorId.equals("")) {
            mSelectedIdByLevelId = new HashMap<>();
        }
        getActivity().invalidateOptionsMenu();
    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                priceTrackingHelper.savePriceTransaction(businessModel.productHelper.getTaggedProducts());
                businessModel.saveModuleCompletion(HomeScreenTwo.MENU_PRICE);
                businessModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                businessModel.updateIsVisitedFlag();

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
                        null, new CommonDialog.positiveOnClickListener() {
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

    private void onLoadModule(Vector<LevelBO> parentidList) {
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();

        if (items == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();

        if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 0) {
            for (LevelBO levelBO : parentidList) {
                for (ProductMasterBO sku : items) {
                    if ((levelBO.getProductID() == sku.getParentid()) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                        mylist.add(sku);
                    }
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 1) {
            for (LevelBO levelBO : parentidList) {
                for (ProductMasterBO sku : items) {
                    if ((levelBO.getProductID() == sku.getParentid()) && (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                        mylist.add(sku);
                    }
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 2) {
            for (LevelBO levelBO : parentidList) {
                for (ProductMasterBO sku : items) {
                    if ((levelBO.getProductID() == sku.getParentid()) && (sku.getIsSaleable() == 1)) {
                        mylist.add(sku);
                    }
                }
            }
        }

        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
    }

    private void onLoadModule(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts) {
        Vector<ProductMasterBO> items = businessModel.productHelper.getTaggedProducts();
        if (items == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();

        if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 0) {
            if (mAttributeProducts != null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1) &&
                                (mAttributeProducts.contains(Integer.parseInt(sku.getProductID())))) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts != null && parentidList.isEmpty()) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if ((pid == Integer.parseInt(sku.getProductID())) && (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            }

        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 1) {
            if (mAttributeProducts != null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) &&
                                (sku.getIsSaleable() == 1 && sku.getOwn() == 0) &&
                                (mAttributeProducts.contains(Integer.parseInt(sku.getProductID())))) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) &&
                                (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts != null && parentidList.isEmpty()) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if ((pid == Integer.parseInt(sku.getProductID())) &&
                                (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                            mylist.add(sku);
                        }
                    }
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 2) {
            if (mAttributeProducts != null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) &&
                                (sku.getIsSaleable() == 1) &&
                                (mAttributeProducts.contains(Integer.parseInt(sku.getProductID())))) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts == null && !parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (ProductMasterBO sku : items) {
                        if ((levelBO.getProductID() == sku.getParentid()) &&
                                (sku.getIsSaleable() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            } else if (mAttributeProducts != null && parentidList.isEmpty()) {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if ((pid == Integer.parseInt(sku.getProductID())) &&
                                (sku.getIsSaleable() == 1)) {
                            mylist.add(sku);
                        }
                    }
                }
            }
        }
        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
        if (mSelectedIdByLevelId != null && businessModel.isMapEmpty(mSelectedIdByLevelId) == false) {
            selectedCompetitorId = "";
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
                if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1) &&
                        (sku.getIsSaleable() == 1 && sku.getOwn() == 1)) {
                    mylist.add(sku);
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 1) {
            for (ProductMasterBO sku : items) {
                if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1) &&
                        (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                    mylist.add(sku);
                }
            }
        } else if (priceTrackingHelper.LOAD_PRICE_COMPETITOR == 2) {
            for (ProductMasterBO sku : items) {
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

    class ViewHolder {
        ProductMasterBO mSKUBO;
        TextView mBarCode, mSrp;
        TextView mSKU, mPrev_CA, mPrev_PC, mPrev_OO, tv_prev_mrp_pc, tv_prev_mrp_ca, tv_prev_mrp_ou;
        TextView mPrev_CA_label, mPrev_PC_label, mPrev_OO_label, tv_prev_mrp_pc_label, tv_prev_mrp_ca_label, tv_prev_mrp_ou_label;
        CheckBox mChanged, mCompliance;
        EditText mCaPrice, mPcPrice, mOoPrice;
        Spinner mReason;
        RelativeLayout rl_PriceChanged, rl_PriceCompliance;
        TextView mProductCodeTV;

        RelativeLayout rl_prev_price;
        LinearLayout ll_prev_case, ll_prev_oo, ll_prev_pc, ll_prev_price_Lty;
        LinearLayout ll_prev_mrp_main_Lty, ll_prev_mrp_ca_Lty, ll_prev_mrp_oo_Lty, ll_prev_mrp_pc_Lty;
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

                holder.mBarCode = (TextView) row
                        .findViewById(R.id.barcode);
                holder.mBarCode.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mSKU = (TextView) row.findViewById(R.id.sku);
                holder.mSKU.setTypeface(businessModel.configurationMasterHelper.getProductNameFont());
                holder.mSKU.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.mSrp = (TextView) row.findViewById(R.id.tv_srp);
                holder.mSrp.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.rl_PriceChanged = (RelativeLayout) row
                        .findViewById(R.id.rl_PriceChanged);

                holder.mChanged = (CheckBox) row
                        .findViewById(R.id.changed);

                holder.mPrev_CA = (TextView) row
                        .findViewById(R.id.prev_ca);
                holder.mPrev_CA.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.mPrev_PC = (TextView) row
                        .findViewById(R.id.prev_pc);
                holder.mPrev_PC.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.mPrev_OO = (TextView) row
                        .findViewById(R.id.prev_oo);
                holder.mPrev_OO.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.mPrev_CA_label = (TextView) row
                        .findViewById(R.id.prev_ca_label);
                holder.mPrev_CA_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mPrev_PC_label = (TextView) row
                        .findViewById(R.id.prev_pc_label);
                holder.mPrev_PC_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mPrev_OO_label = (TextView) row
                        .findViewById(R.id.prev_oo_label);
                holder.mPrev_OO_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                holder.mCaPrice = (EditText) row
                        .findViewById(R.id.caprice);
                holder.mCaPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.mPcPrice = (EditText) row
                        .findViewById(R.id.pcprice);
                holder.mPcPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.mOoPrice = (EditText) row
                        .findViewById(R.id.ooprice);
                holder.mOoPrice
                        .setInputType(InputType.TYPE_NULL);

                holder.rl_PriceCompliance = (RelativeLayout) row
                        .findViewById(R.id.rl_PriceCompliance);

                holder.mCompliance = (CheckBox) row
                        .findViewById(R.id.compliance);

                holder.mReason = (Spinner) row
                        .findViewById(R.id.reason);
                holder.mProductCodeTV = (TextView) row
                        .findViewById(R.id.prdcode_tv);
                holder.mProductCodeTV.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.tv_prev_mrp_pc = (TextView) row.findViewById(R.id.tv_prev_mrp_pc);
                holder.tv_prev_mrp_pc.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_prev_mrp_ca = (TextView) row.findViewById(R.id.tv_prev_mrp_ca);
                holder.tv_prev_mrp_ca.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_prev_mrp_ou = (TextView) row.findViewById(R.id.tv_prev_mrp_oo);
                holder.tv_prev_mrp_ou.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.tv_prev_mrp_ca_label = (TextView) row.findViewById(R.id.prev_mrp_ca_label);
                holder.tv_prev_mrp_ca_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_prev_mrp_ou_label = (TextView) row.findViewById(R.id.prev_mrp_oo_label);
                holder.tv_prev_mrp_ou_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_prev_mrp_pc_label = (TextView) row.findViewById(R.id.prev_mrp_pc_label);
                holder.tv_prev_mrp_pc_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                holder.rl_prev_price = (RelativeLayout) row.findViewById(R.id.rl_prev_price_n_mrp_layout);
                holder.ll_prev_case = (LinearLayout) row.findViewById(R.id.ll_prev_price_ca);
                holder.ll_prev_pc = (LinearLayout) row.findViewById(R.id.ll_prev_price_pc);
                holder.ll_prev_oo = (LinearLayout) row.findViewById(R.id.ll_prev_price_oo);
                holder.ll_prev_price_Lty = (LinearLayout) row.findViewById(R.id.ll_prev_price);
                holder.ll_prev_mrp_main_Lty = (LinearLayout) row.findViewById(R.id.ll_prev_mrp);
                holder.ll_prev_mrp_pc_Lty = (LinearLayout) row.findViewById(R.id.ll_prev_mrp_pc);
                holder.ll_prev_mrp_ca_Lty = (LinearLayout) row.findViewById(R.id.ll_prev_mrp_ca);
                holder.ll_prev_mrp_oo_Lty = (LinearLayout) row.findViewById(R.id.ll_prev_mrp_oo);

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

                        if (holder.mSKUBO.getPriceChanged() == 1) {
                            holder.mSKUBO.setPriceChanged(0);
                            holder.mChanged.setChecked(false);
                            holder.mCaPrice.setEnabled(false);
                            holder.mPcPrice.setEnabled(false);
                            holder.mOoPrice.setEnabled(false);


                            if (businessModel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE) {
                                holder.mCaPrice.setText(holder.mSKUBO.getPrevPrice_ca());
                                holder.mCaPrice.clearFocus();

                                holder.mPcPrice.setText(holder.mSKUBO.getPrevPrice_pc());
                                holder.mPcPrice.clearFocus();

                                holder.mOoPrice.setText(holder.mSKUBO.getPrevPrice_oo());
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
                            holder.mSKUBO.setPriceChanged(1);
                            holder.mChanged.setChecked(true);


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

                        if (businessModel.validDecimalValue(qty, 8, 2)) {
                            holder.mSKUBO.setPrice_ca(qty);
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
                        holder.mCaPrice.selectAll();
                        holder.mCaPrice.requestFocus();
                        return true;
                    }
                });

                holder.mPcPrice.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();

                        if (businessModel.validDecimalValue(qty, 8, 2)) {
                            holder.mSKUBO.setPrice_pc(qty);
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
                        holder.mPcPrice.selectAll();
                        holder.mPcPrice.requestFocus();
                        return true;
                    }
                });

                holder.mOoPrice.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();

                        if (businessModel.validDecimalValue(qty, 8, 2)) {
                            holder.mSKUBO.setPrice_oo(qty);
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
                        holder.mOoPrice.selectAll();
                        holder.mOoPrice.requestFocus();
                        return true;
                    }
                });

                holder.mCompliance.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.mSKUBO.getPriceCompliance() == 1) {
                            holder.mSKUBO.setPriceCompliance(0);
                            holder.mCompliance.setChecked(false);
                            holder.mReason.setEnabled(true);
                            holder.mReason.setSelected(true);
                            holder.mReason.setSelection(0);
                            holder.mSKUBO.setReasonID("0");
                        } else {
                            holder.mSKUBO.setPriceCompliance(1);
                            holder.mCompliance.setChecked(true);
                            holder.mReason.setEnabled(false);
                            holder.mReason.setSelected(false);
                            holder.mReason.setSelection(0);
                            holder.mSKUBO.setReasonID("0");
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

                                holder.mSKUBO.setReasonID(reString
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

                if (!priceTrackingHelper.SHOW_PRICE_LASTVP && !priceTrackingHelper.SHOW_PREV_MRP_IN_PRICE) {
                    holder.rl_prev_price.setVisibility(View.GONE);
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


                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.mSKUBO = items.get(position);

            holder.mBarCode.setText(holder.mSKUBO.getBarCode());
            holder.mSKU.setText(holder.mSKUBO.getProductShortName());
            holder.mSrp.setText("SRP:" + String.valueOf(holder.mSKUBO.getSrp()));


            holder.mPrev_CA.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_ca())));
            holder.mPrev_PC.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_pc())));
            holder.mPrev_OO.setText(businessModel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_oo())));

            holder.mPrev_PC_label.setText(getResources().getString(R.string.pc) + ":");
            holder.mPrev_CA_label.setText(getResources().getString(R.string.ca) + ":");
            holder.mPrev_OO_label.setText(getResources().getString(R.string.ou) + ":");

            holder.tv_prev_mrp_pc_label.setText(getResources().getString(R.string.pc) + ":");
            holder.tv_prev_mrp_ca_label.setText(getResources().getString(R.string.ca) + ":");
            holder.tv_prev_mrp_ou_label.setText(getResources().getString(R.string.ou) + ":");

            holder.mCaPrice.setText(holder.mSKUBO.getPrice_ca());
            holder.mPcPrice.setText(holder.mSKUBO.getPrice_pc());
            holder.mOoPrice.setText(holder.mSKUBO.getPrice_oo());

            holder.mProductCodeTV.setText(holder.mSKUBO.getProductCode());


            holder.tv_prev_mrp_ca.setText(holder.mSKUBO.getPrevMRP_ca());
            holder.tv_prev_mrp_pc.setText(holder.mSKUBO.getPrevMRP_pc());
            holder.tv_prev_mrp_ou.setText(holder.mSKUBO.getPrevMRP_ou());


            holder.mReason.setSelection(getReasonIndex(holder.mSKUBO.getReasonID()));
            if (holder.mSKUBO.getPriceCompliance() == 1)
                holder.mCompliance.setChecked(true);

            if (priceTrackingHelper.SHOW_PRICE_CHANGED) {
                if (holder.mSKUBO.getPriceChanged() == 1) {
                    holder.mChanged.setChecked(true);

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

            if (holder.mSKUBO.getPriceCompliance() == 1) {
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
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        priceTrackingHelper.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        onLoadModule();
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

        mDrawerLayout.closeDrawers();
        onLoadModule(mParentIdList);
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mDrawerLayout.closeDrawers();
        this.parentidList = parentidList;
        this.mAttributeProducts = mAttributeProducts;
        this.filtertext = filtertext;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        onLoadModule(parentidList, mSelectedIdByLevelId, mAttributeProducts);
    }


    private void FiveFilterFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
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

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
