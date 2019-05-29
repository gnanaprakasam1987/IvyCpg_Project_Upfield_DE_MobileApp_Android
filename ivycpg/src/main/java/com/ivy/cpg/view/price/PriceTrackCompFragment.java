package com.ivy.cpg.view.price;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.ivy.sd.png.bo.CompanyBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.CompetitorTrackingHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class PriceTrackCompFragment extends IvyBaseFragment implements
        OnClickListener {

    private BusinessModel bmodel;
    private PriceTrackingHelper priceTrackingHelper;
    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ListView lv;
    private ArrayList<ProductMasterBO> mylist;

    private EditText QUANTITY;
    private String append = "";

    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayAdapter<ReasonMaster> spinnerAdapter;

    TextView tvCurPriceText, tvProdName;
    private View view;
    Button btnSave;
    FrameLayout drawer;
    private boolean isFromChild;
    private int mSelectedCompanyId = 0;
    private RecyclerView rvCompanyList;
    private CompanyAdapter companyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_price_comp_tracking, container,
                false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        //setting drawer width equal to scren width
        drawer = (FrameLayout) view.findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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

            if (bmodel.configurationMasterHelper.floating_Survey)
                menu.findItem(R.id.menu_survey).setVisible(true);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);


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
                if (!isPreVisit)
                    bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));

                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit",true);

                if (isFromChild)
                    startActivity(intent.putExtra("isStoreMenu", true));
                else
                    startActivity(intent);

                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            setScreenTitle(bmodel.mSelectedActivityName);
            getActionBar().setElevation(0);
        }
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                if (getActionBar() != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
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
        tvProdName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView tvIsChanged = (TextView) view.findViewById(R.id.changed);
        TextView tvCompliance = (TextView) view.findViewById(R.id.compliance);
        // TextView tvReason = (TextView) view.findViewById(R.id.reason);

        tvCurPriceText = (TextView) view.findViewById(R.id.curtext);
        tvCurPriceText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        tvIsChanged.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvCompliance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        // tvReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        LinearLayout ll_curPrice = (LinearLayout) view.findViewById(R.id.ll_cur_price);

        TextView tvCa = (TextView) view.findViewById(R.id.ca_price);
        TextView tvPc = (TextView) view.findViewById(R.id.pc_price);
        TextView tvOo = (TextView) view.findViewById(R.id.oo_price);

        tvCa.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvPc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvOo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.ca_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.ca_price))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.ca_price).getTag()));


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.pc_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.pc_price))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.pc_price).getTag()));


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.oo_price).getTag()) != null) {
                ((TextView) view.findViewById(R.id.oo_price))
                        .setText(bmodel.labelsMasterHelper
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
                if (bmodel.labelsMasterHelper.applyLabels(view
                        .findViewById(R.id.compliance).getTag()) != null)
                    ((TextView) view.findViewById(R.id.compliance))
                            .setText(bmodel.labelsMasterHelper
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
        rvCompanyList = (RecyclerView) view.findViewById(R.id.rvCompany);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCompanyList.setLayoutManager(mLayoutManager);
        rvCompanyList.setItemAnimator(new DefaultItemAnimator());

        loadReasons();
        onLoadModule();
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }


    private void nextButtonClick() {
        try {
            if (priceTrackingHelper.hasDataTosave(bmodel.productHelper.getTaggedProducts()))
                new SaveAsyncTask().execute();
            else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                priceTrackingHelper.savePriceTransaction(getContext().getApplicationContext(), bmodel.productHelper.getTaggedProducts());
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_PRICE_COMP, true);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
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
                            intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
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

    private void onLoadModule() {
        CompetitorTrackingHelper competitorTrackingHelper = CompetitorTrackingHelper.getInstance(getActivity());
        if (competitorTrackingHelper.getCompanyList().size() > 0) {
            CompanyBO companyBO = new CompanyBO();
            companyBO.setCompetitorid(0);
            companyBO.setCompetitorName(getResources().getString(R.string.all));
            ArrayList<CompanyBO> companyList = new ArrayList<>();
            companyList.add(companyBO);
            companyList.addAll(competitorTrackingHelper.getCompanyList());
            companyAdapter = new CompanyAdapter(companyList);
            rvCompanyList.setAdapter(companyAdapter);

        } else {
            rvCompanyList.setVisibility(View.GONE);
        }
        Vector<ProductMasterBO> items = bmodel.productHelper.getTaggedProducts();

        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();


        for (ProductMasterBO sku : items) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            if ((priceTrackingHelper.mSelectedFilter == sku.getParentid()
                    || priceTrackingHelper.mSelectedFilter == -1) &&
                    (sku.getIsSaleable() == 1 && sku.getOwn() == 0)) {
                mylist.add(sku);
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

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
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
                holder.mBarCode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mSKU = (TextView) row.findViewById(R.id.sku);
                holder.mSKU.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.mSKU.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.mSrp = (TextView) row.findViewById(R.id.tv_srp);
                holder.mSrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.rl_PriceChanged = (RelativeLayout) row
                        .findViewById(R.id.rl_PriceChanged);

                holder.mChanged = (CheckBox) row
                        .findViewById(R.id.changed);

                holder.mPrev_CA = (TextView) row
                        .findViewById(R.id.prev_ca);
                holder.mPrev_CA.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.mPrev_PC = (TextView) row
                        .findViewById(R.id.prev_pc);
                holder.mPrev_PC.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.mPrev_OO = (TextView) row
                        .findViewById(R.id.prev_oo);
                holder.mPrev_OO.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.mPrev_CA_label = (TextView) row
                        .findViewById(R.id.prev_ca_label);
                holder.mPrev_CA_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mPrev_PC_label = (TextView) row
                        .findViewById(R.id.prev_pc_label);
                holder.mPrev_PC_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mPrev_OO_label = (TextView) row
                        .findViewById(R.id.prev_oo_label);
                holder.mPrev_OO_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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
                holder.mProductCodeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.tv_prev_mrp_pc = (TextView) row.findViewById(R.id.tv_prev_mrp_pc);
                holder.tv_prev_mrp_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_prev_mrp_ca = (TextView) row.findViewById(R.id.tv_prev_mrp_ca);
                holder.tv_prev_mrp_ca.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_prev_mrp_ou = (TextView) row.findViewById(R.id.tv_prev_mrp_oo);
                holder.tv_prev_mrp_ou.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.tv_prev_mrp_ca_label = (TextView) row.findViewById(R.id.prev_mrp_ca_label);
                holder.tv_prev_mrp_ca_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_prev_mrp_ou_label = (TextView) row.findViewById(R.id.prev_mrp_oo_label);
                holder.tv_prev_mrp_ou_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_prev_mrp_pc_label = (TextView) row.findViewById(R.id.prev_mrp_pc_label);
                holder.tv_prev_mrp_pc_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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


                            if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE) {
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
                        if (qty.length() > 0)
                            holder.mCaPrice.setSelection(qty.length());
                        if (SDUtil.isValidDecimal(qty, 8, 2)) {
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
                        if (SDUtil.isValidDecimal(qty, 8, 2)) {
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
                        if (SDUtil.isValidDecimal(qty, 8, 2)) {
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
                        holder.mOoPrice.requestFocus();
                        if (holder.mOoPrice.getText().length() > 0)
                            holder.mOoPrice.setSelection(holder.mOoPrice.getText().length());
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


            holder.mPrev_CA.setText(bmodel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_ca())));
            holder.mPrev_PC.setText(bmodel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_pc())));
            holder.mPrev_OO.setText(bmodel.formatValue(SDUtil.convertToDouble(holder.mSKUBO.getPrevPrice_oo())));

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


            if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
                holder.mBarCode.setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.SHOW_PRODUCT_CODE) {
                holder.mProductCodeTV.setVisibility(View.GONE);
            }

            if (holder.mSKUBO.getPriceCompliance() == 1) {
                holder.mCompliance.setChecked(true);
                holder.mReason.setEnabled(false);
            } else {
                holder.mCompliance.setChecked(false);
                holder.mReason.setEnabled(true);
            }
            return row;
        }
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
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


    public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.MyViewHolder> {

        private ArrayList<CompanyBO> companyList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public Button btnFilter;

            public MyViewHolder(View view) {
                super(view);
                btnFilter = (Button) view.findViewById(R.id.btn_filter);
                btnFilter.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            }
        }

        public CompanyAdapter(ArrayList<CompanyBO> companyList) {
            this.companyList = companyList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_rv_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final CompanyBO companyBO = companyList.get(position);
            holder.btnFilter.setText(companyBO.getCompetitorName());
            try {
                if (mSelectedCompanyId == companyBO.getCompetitorid()) {
                    holder.btnFilter.setBackgroundResource(R.drawable.button_rounded_corner_blue);
                    holder.btnFilter.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                } else {
                    holder.btnFilter.setBackgroundResource(R.drawable.button_round_corner_grey);
                    holder.btnFilter.setTextColor(ContextCompat.getColor(getActivity(), R.color.half_Black));
                }

            } catch (Exception e) {
                Commons.printException(e);
            }

            holder.btnFilter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedCompanyId = companyBO.getCompetitorid();
                    updateList();
                }
            });

        }

        @Override
        public int getItemCount() {
            return companyList.size();
        }
    }

    //to update list based on filter selection
    private void updateList() {
        companyAdapter.notifyDataSetChanged();
        if (mylist.size() > 0) {
            ArrayList<ProductMasterBO> tempList = new ArrayList<>();
            if (mSelectedCompanyId != 0) {
                for (ProductMasterBO productMasterBO : mylist) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !productMasterBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (productMasterBO.getCompanyId() == mSelectedCompanyId)
                        tempList.add(productMasterBO);
                }
            } else {
                tempList = mylist;
            }

            MyAdapter adapter = new MyAdapter(tempList);
            lv.setAdapter(adapter);
        }
    }

}
