package com.ivy.cpg.view.stockcheck;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.order.productdetails.ProductSchemeDetailsActivity;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SchemeDialog;
import com.ivy.sd.png.view.SpecialFilterFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class StockCheckFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener,
        CompetitorFilterInterface, FiveLevelFilterCallBack, StockCheckContractor.StockCheckView {

    private static final String BRAND = "Brand";
    private String append = "";

    private DrawerLayout mDrawerLayout;
    private ListView listview;
    private EditText QUANTITY;
    private EditText mEdt_searchProductName;


    private BusinessModel businessModel;

    private ArrayList<ProductMasterBO> stockList;

    private InputMethodManager inputManager;
    private Button mBtn_Search, mBtn_clear;

    private View view;
    LinearLayout ll_spl_filter, ll_tab_selection;
    TextView tv_total_stockCheckedProducts, tv_total_products;
    Button btn_save;
    private ViewFlipper viewFlipper;
    private TextView productName;
    FrameLayout drawer;
    private boolean isFromChild;
    Button mBtnFilterPopup;

    private Object selectedTabTag;
    private int x, y;
    private HorizontalScrollView hscrl_spl_filter;

    private StockCheckPresenterImpl stockCheckPresenter;
    private AlertDialog alertDialog;
    private HashMap<Integer, Integer> mCompetitorSelectedIdByLevelId;

    private TextView tv_sharePercent;
    private LinearLayout ll_stockCheck_SharePercent;
    MyAdapter mSchedule;

    private StockCheckHelper stockCheckHelper;
    private int mTotalScreenWidth = 0;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stockcheck,
                container, false);

        businessModel = (BusinessModel) context.getApplicationContext();
        stockCheckHelper = StockCheckHelper.getInstance(getActivity());

        initializeViews(view);

        stockCheckPresenter = new StockCheckPresenterImpl(context);
        stockCheckPresenter.setView(this);

        // update config to load both salable and non salable products
        stockCheckPresenter.isLoadBothSalable(businessModel.configurationMasterHelper.SHOW_SALABLE_AND_NON_SALABLE_SKU);

        try {
            isFromChild = ((Activity) context).getIntent().getBooleanExtra("isFromChild", false);

            if (businessModel.configurationMasterHelper.SHOW_SPL_FILTER) {

                String defaultFilter = stockCheckPresenter.getDefaultFilter();
                if (!"".equals(defaultFilter)) {

                    stockCheckPresenter.putValueToFilterMap(defaultFilter);
                    if (businessModel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView(view);
                        updateGeneralText(defaultFilter);
                        selectTab(view, defaultFilter);
                    } else {
                        updateGeneralText(defaultFilter);
                    }


                } else {
                    stockCheckPresenter.putValueToFilterMap("");
                    if (businessModel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView(view);
                        updateGeneralText("");
                        selectTab(view, stockCheckPresenter.getGeneralFilter().get(0).getConfigCode());
                    } else {
                        updateGeneralText("");
                    }


                }
            } else {
                stockCheckPresenter.putValueToFilterMap("");
                updateGeneralText("");
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((IvyBaseActivityNoActionBar) context).checkAndRequestPermissionAtRunTime(3);
        setHasOptionsMenu(true);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(businessModel.mSelectedActivityName);
            getActionBar().setElevation(0);
        }
        stockCheckPresenter.loadInitialData();
        stockCheckPresenter.prepareAdapters();

        hideAndSeek();
        updateFooter();

    }

    private void initializeViews(View view) {

        listview = view.findViewById(R.id.list);
        listview.setCacheColorHint(0);

        mDrawerLayout = view.findViewById(
                R.id.drawer_layout);
        drawer = view.findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
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
                if (getActionBar() != null) {
                    setScreenTitle(businessModel.mSelectedActivityName);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                /*if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }*/

                getActivity().supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.closeDrawer(GravityCompat.END);

        inputManager = (InputMethodManager) context.getSystemService(
                INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

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

        tv_total_stockCheckedProducts = view.findViewById(R.id.tv_stockCheckedProductscount);
        tv_total_products = view.findViewById(R.id.tv_productsCount);


        btn_save = view.findViewById(R.id.btn_save);

        btn_save.setOnClickListener(this);
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

        mBtnFilterPopup = view.findViewById(R.id.btn_filter_popup);

        ll_stockCheck_SharePercent = view.findViewById(R.id.llstockCheckSharePercent);
        tv_sharePercent = view.findViewById(R.id.tv_sharePercent);

        if (businessModel.configurationMasterHelper.isAuditEnabled()) {
            TextView tvAudit = view.findViewById(R.id.audit);
            tvAudit.setVisibility(View.VISIBLE);

        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void hideAndSeek() {
        try {


            if (!stockCheckHelper.SHOW_STOCK_SC) {
                view.findViewById(R.id.shelfCaseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfCaseTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfCaseTitle))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfCaseTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!stockCheckHelper.SHOW_STOCK_SP) {
                view.findViewById(R.id.shelfPcsTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfPcsTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfPcsTitle))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfPcsTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!stockCheckHelper.SHOW_STOCK_CB)
                view.findViewById(R.id.shelfPcsCB).setVisibility(View.GONE);
            else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfPcsCB).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfPcsCB))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfPcsCB).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!stockCheckHelper.SHOW_SHELF_OUTER) {
                view.findViewById(R.id.shelfOuterTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfOuterTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfOuterTitle))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfOuterTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!stockCheckHelper.SHOW_SHELF_OUTER
                    && !stockCheckHelper.SHOW_STOCK_SP
                    && !stockCheckHelper.SHOW_STOCK_SC) {
                view.findViewById(R.id.shelfCaseTitle).setVisibility(View.GONE);
                view.findViewById(R.id.shelfPcsTitle).setVisibility(View.GONE);
                view.findViewById(R.id.shelfOuterTitle).setVisibility(View.GONE);
                // view.findViewById(R.id.shelf_layout).setVisibility(View.GONE);

            }

            if (!stockCheckHelper.SHOW_STOCK_TOTAL || stockCheckHelper.SHOW_STOCK_RSN) {
                view.findViewById(R.id.exp_stktotalTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.exp_stktotalTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.exp_stktotalTitle))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.exp_stktotalTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }


            if (!stockCheckHelper.SHOW_STOCK_PRICE_TAG_AVAIL) {
                view.findViewById(R.id.stock_price_tag).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.stock_price_tag).getTag()) != null)
                        ((TextView) view.findViewById(R.id.stock_price_tag))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.stock_price_tag).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }


            if (!stockCheckHelper.SHOW_STOCK_FC) {
                view.findViewById(R.id.et_facingQty).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.et_facingQty).getTag()) != null)
                        ((TextView) view.findViewById(R.id.et_facingQty))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.et_facingQty).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (stockCheckHelper.SHOW_STOCK_CB && !stockCheckHelper.SHOW_STOCK_FC &&
                    !stockCheckHelper.SHOW_STOCK_SC && !stockCheckHelper.SHOW_STOCK_SP &&
                    !stockCheckHelper.SHOW_SHELF_OUTER) {
                view.findViewById(R.id.ll_keypad).setVisibility(View.GONE);
            }

            if (businessModel.configurationMasterHelper.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) {
                ll_stockCheck_SharePercent.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void loadSearchedList() {
        stockCheckPresenter.loadSearchedList(mEdt_searchProductName.getText().toString());
    }

    private void refreshList(ArrayList<ProductMasterBO> list) {
        this.stockList = list;

        ProductTaggingHelper productTaggingHelper = ProductTaggingHelper.getInstance(getActivity());

        // Listing only products mapped to current location
        if (productTaggingHelper.getTaggedLocations().size() > 0) {
            ArrayList<ProductMasterBO> temp = new ArrayList<>();
            for (ProductMasterBO productMasterBO : stockList) {
                if (productMasterBO.getTaggedLocations().contains(stockCheckPresenter.getCurrentLocationId())) {
                    temp.add(productMasterBO);
                }
            }
            stockList.clear();
            stockList.addAll(temp);
        }

        if (mSchedule == null) {
            mSchedule = new MyAdapter(stockList);
            listview.setAdapter(mSchedule);
        } else {
            mSchedule.setListData(stockList);
            mSchedule.notifyDataSetChanged();
        }
    }


    @Override
    public void updateBrandText(String mFilterText, int bid) {
        stockCheckPresenter.updateBrandText();
    }

    @Override
    public void updateGeneralText(String mFilterText) {

        stockCheckPresenter.updateGeneralText(mFilterText);

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }


    @Override
    public void updateCompetitorProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText) {
        stockCheckPresenter.updateCompetitorFilteredProducts(parentIdList, mSelectedIdByLevelId, filterText);
    }

    public class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(context, R.layout.row_closingstock, items);
            this.items = items;
        }

        void setListData(ArrayList<ProductMasterBO> productList) {
            this.items = productList;
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

        @NonNull
        @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            try {
                final ViewHolder holder;
                final ProductMasterBO product = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.activity_stock_check_listview, parent,
                            false);
                    holder = new ViewHolder();
                    holder.audit = row
                            .findViewById(R.id.btn_audit);
                    holder.psname = row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.psname.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                    holder.productCode = row
                            .findViewById(R.id.stock_and_order_listview_produtCode);
                    holder.tvbarcode = row
                            .findViewById(R.id.stock_and_order_listview_productbarcode);

                    holder.ppq = row
                            .findViewById(R.id.stock_and_order_listview_ppq);

                    holder.psq = row
                            .findViewById(R.id.stock_and_order_listview_psq);

                    holder.mReason = row.findViewById(R.id.reason);

                    holder.shelfPcsQty = row
                            .findViewById(R.id.stock_and_order_listview_sp_qty);
                    holder.shelfCaseQty = row
                            .findViewById(R.id.stock_and_order_listview_sc_qty);
                    holder.shelfouter = row
                            .findViewById(R.id.stock_and_order_listview_shelfouter_qty);
                    holder.barcode = row
                            .findViewById(R.id.stock_and_order_listview_productbarcode);


                    holder.ll_stkCB = row
                            .findViewById(R.id.ll_stock_and_order_listview_cb);
                    holder.imageButton_availability = row
                            .findViewById(R.id.btn_availability);

                    holder.rl_priceTagCB = row
                            .findViewById(R.id.rl_price_tag);
                    holder.chkPriceTagAvail = row
                            .findViewById(R.id.checkbox_price_tag);

                    if (!stockCheckHelper.SHOW_STOCK_BARCODE) {
                        holder.barcode.setVisibility(View.GONE);
                    }
                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                        row.findViewById(R.id.stock_check_listview_total).setVisibility(View.GONE);
                        holder.total = row.findViewById(R.id.stock_check_listview_total2);
                    } else {
                        row.findViewById(R.id.ll_total2).setVisibility(View.GONE);
                        holder.total = row
                                .findViewById(R.id.stock_check_listview_total);
                    }

                    if (!stockCheckHelper.SHOW_SHELF_OUTER
                            && !stockCheckHelper.SHOW_STOCK_SP
                            && !stockCheckHelper.SHOW_STOCK_SC) {
                        row.findViewById(R.id.ll_total2).setVisibility(View.GONE);
                        row.findViewById(R.id.stock_check_listview_total).setVisibility(View.GONE);
                    }

                    holder.facingQty = row
                            .findViewById(R.id.stock_check_listview_fc_qty);

                    if (businessModel.configurationMasterHelper.IS_SHOW_PSQ) {
                        holder.psq.setVisibility(View.VISIBLE);
                    } else {
                        holder.psq.setVisibility(View.GONE);
                    }
                    if (businessModel.configurationMasterHelper.IS_SHOW_PPQ) {
                        holder.ppq.setVisibility(View.VISIBLE);
                    } else {
                        holder.ppq.setVisibility(View.GONE);
                    }

                    if (!stockCheckHelper.SHOW_STOCK_FC)
                        holder.facingQty.setVisibility(View.GONE);
                    if (!stockCheckHelper.SHOW_STOCK_SC)
                        holder.shelfCaseQty.setVisibility(View.GONE);
                    if (!stockCheckHelper.SHOW_STOCK_SP)
                        holder.shelfPcsQty.setVisibility(View.GONE);
                    if (!stockCheckHelper.SHOW_STOCK_CB)
                        holder.ll_stkCB.setVisibility(View.GONE);
                    if (!stockCheckHelper.SHOW_STOCK_PRICE_TAG_AVAIL)
                        holder.rl_priceTagCB.setVisibility(View.GONE);

                    if (!stockCheckHelper.SHOW_STOCK_RSN)
                        row.findViewById(R.id.ll_reason).setVisibility(View.GONE);


                    if (!stockCheckHelper.SHOW_SHELF_OUTER)
                        holder.shelfouter.setVisibility(View.GONE);
                    if (!stockCheckHelper.SHOW_STOCK_TOTAL)
                        holder.total.setVisibility(View.GONE);

                    if (!stockCheckHelper.SHOW_SHELF_OUTER
                            && !stockCheckHelper.SHOW_STOCK_SP
                            && !stockCheckHelper.SHOW_STOCK_SC) {
                        holder.shelfCaseQty.setVisibility(View.GONE);
                        holder.shelfPcsQty.setVisibility(View.GONE);
                        holder.shelfouter.setVisibility(View.GONE);

                    }

                    if (businessModel.configurationMasterHelper.IS_STK_DIGIT) {
                        holder.shelfCaseQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(businessModel.configurationMasterHelper.STK_DIGIT)});
                        holder.shelfPcsQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(businessModel.configurationMasterHelper.STK_DIGIT)});
                        holder.shelfouter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(businessModel.configurationMasterHelper.STK_DIGIT)});
                    }


                    if (!businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                        holder.productCode.setVisibility(View.GONE);

                    if (!businessModel.configurationMasterHelper.SHOW_BARCODE)
                        holder.tvbarcode.setVisibility(View.GONE);

                    holder.audit.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getAudit()
                                    == IvyConstants.AUDIT_DEFAULT) {

                                holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setAudit(IvyConstants.AUDIT_OK);
                                holder.audit.setImageResource(R.drawable.ic_audit_yes);
                            } else if (holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getAudit()
                                    == IvyConstants.AUDIT_OK) {

                                holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setAudit(IvyConstants.AUDIT_NOT_OK);
                                holder.audit.setImageResource(R.drawable.ic_audit_no);
                            } else if (holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getAudit()
                                    == IvyConstants.AUDIT_NOT_OK) {

                                holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setAudit(IvyConstants.AUDIT_DEFAULT);
                                holder.audit.setImageResource(R.drawable.ic_audit_none);
                            }
                        }
                    });

                    holder.imageButton_availability.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (stockCheckHelper.CHANGE_AVAL_FLOW) {
                                if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == -1) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                                    holder.imageButton_availability.setChecked(true);

                                    if (stockCheckHelper.SHOW_STOCK_SP)
                                        holder.shelfPcsQty.setText("0");
                                    if (stockCheckHelper.SHOW_STOCK_SC)
                                        holder.shelfCaseQty.setText("0");
                                    if (stockCheckHelper.SHOW_SHELF_OUTER)
                                        holder.shelfouter.setText("0");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(0);
                                    }
                                } else if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                                    holder.imageButton_availability.setChecked(false);


                                    if (stockCheckHelper.SHOW_STOCK_SP)
                                        holder.shelfPcsQty.setText("");
                                    if (stockCheckHelper.SHOW_STOCK_SC)
                                        holder.shelfCaseQty.setText("");
                                    if (stockCheckHelper.SHOW_SHELF_OUTER)
                                        holder.shelfouter.setText("");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex).setReasonId(0);
                                    }

                                } else if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_productivity)));
                                    holder.imageButton_availability.setChecked(true);

                                    if (stockCheckHelper.SHOW_STOCK_SP)
                                        holder.shelfPcsQty.setText("1");
                                    else if (stockCheckHelper.SHOW_STOCK_SC)
                                        holder.shelfCaseQty.setText("1");
                                    else if (stockCheckHelper.SHOW_SHELF_OUTER)
                                        holder.shelfouter.setText("1");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex).setReasonId(0);
                                    }
                                }
                            } else {
                                if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == -1) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_productivity)));
                                    holder.imageButton_availability.setChecked(true);

                                    if (stockCheckHelper.SHOW_STOCK_SP
                                            && holder.productObj.getPcUomid() != 0)
                                        holder.shelfPcsQty.setText("1");
                                    else if (stockCheckHelper.SHOW_STOCK_SC
                                            && holder.productObj.getCaseUomId() != 0)
                                        holder.shelfCaseQty.setText("1");
                                    else if (stockCheckHelper.SHOW_SHELF_OUTER
                                            && holder.productObj.getOuUomid() != 0)
                                        holder.shelfouter.setText("1");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex).setReasonId(0);
                                    }
                                } else if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                                    holder.imageButton_availability.setChecked(true);

                                    if (stockCheckHelper.SHOW_STOCK_SP
                                            && holder.productObj.getPcUomid() != 0)
                                        holder.shelfPcsQty.setText("0");
                                    if (stockCheckHelper.SHOW_STOCK_SC
                                            && holder.productObj.getCaseUomId() != 0)
                                        holder.shelfCaseQty.setText("0");
                                    if (stockCheckHelper.SHOW_SHELF_OUTER
                                            && holder.productObj.getOuUomid() != 0)
                                        holder.shelfouter.setText("0");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(0);
                                    }
                                } else if (holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);

                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                                    holder.imageButton_availability.setChecked(false);



                                    /*
                                     * When one of the config is not enable the default value set as 0 instead of -1
                                     *
                                     * so that remove config here
                                     * */

                                    holder.shelfPcsQty.setText("");
                                    holder.shelfCaseQty.setText("");
                                    holder.shelfouter.setText("");

                                    if (stockCheckHelper.SHOW_STOCK_RSN) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex).setReasonId(0);
                                    }
                                }
                            }

                            updateFooter();

                        }
                    });

                    holder.chkPriceTagAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            if (isChecked)
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setPriceTagAvailability(1);
                            else
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setPriceTagAvailability(0);
                        }
                    });


                    holder.mReason.setAdapter(stockCheckPresenter.getSpinnerAdapter());
                    holder.mReason
                            .setOnItemSelectedListener(new OnItemSelectedListener() {
                                public void onItemSelected(
                                        AdapterView<?> parent, View view,
                                        int position, long id) {

                                    ReasonMaster reString = (ReasonMaster) holder.mReason
                                            .getSelectedItem();

                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex)
                                            .setReasonId(SDUtil.convertToInt(reString
                                                    .getReasonID()));

                                }

                                public void onNothingSelected(
                                        AdapterView<?> parent) {
                                }
                            });


                    holder.shelfPcsQty
                            .addTextChangedListener(new TextWatcher() {

                                @SuppressLint("SetTextI18n")
                                public void afterTextChanged(Editable s) {

                                    if (holder.productObj.getPcUomid() == 0) {
                                        holder.shelfPcsQty.removeTextChangedListener(this);
                                        holder.shelfPcsQty.setText("");
                                        holder.shelfPcsQty.addTextChangedListener(this);
                                        return;
                                    }

                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.shelfPcsQty.setSelection(qty.length());

                                    if (!qty.equals("")) {
                                        int sp_qty = SDUtil
                                                .convertToInt(holder.shelfPcsQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfPiece(sp_qty);

                                        if (sp_qty > 0
                                                || SDUtil.convertToInt(holder.shelfCaseQty.getText().toString()) > 0
                                                || SDUtil.convertToInt(holder.shelfouter.getText().toString()) > 0) {
                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
                                            holder.imageButton_availability.setChecked(true);

                                        } else if (sp_qty == 0) {
                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                                            holder.imageButton_availability.setChecked(true);
                                        }

                                    } else {
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfPiece(-1);

                                        if (qty.length() == 0
                                                && holder.shelfCaseQty.getText().toString().length() == 0
                                                && holder.shelfouter.getText().toString().length() == 0) {

                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                                            holder.imageButton_availability.setChecked(false);
                                        }
                                    }

                                    int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                                    holder.total.setText(totValue + "");
                                    if (totValue > 0) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setReasonId(0);
                                    } else {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(stockCheckPresenter.getReasonIndex(holder.productObj
                                                .getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getReasonId() + ""));
                                    }

                                    updateFooter();
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                }
                            });

                    holder.shelfCaseQty
                            .addTextChangedListener(new TextWatcher() {

                                @SuppressLint("SetTextI18n")
                                public void afterTextChanged(Editable s) {

                                    if (holder.productObj.getCaseUomId() == 0) {
                                        holder.shelfCaseQty.removeTextChangedListener(this);
                                        holder.shelfCaseQty.setText("");
                                        holder.shelfCaseQty.addTextChangedListener(this);
                                        return;
                                    }

                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.shelfCaseQty.setSelection(qty.length());

                                    if (!qty.equals("")) {
                                        int shelf_case_qty = SDUtil
                                                .convertToInt(holder.shelfCaseQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfCase(shelf_case_qty);

                                        if (shelf_case_qty > 0
                                                || SDUtil.convertToInt(holder.shelfPcsQty.getText().toString()) > 0
                                                || SDUtil.convertToInt(holder.shelfouter.getText().toString()) > 0) {
                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
                                            holder.imageButton_availability.setChecked(true);

                                        } else if (shelf_case_qty == 0) {
                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                                            holder.imageButton_availability.setChecked(true);
                                        }


                                    } else {
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfCase(-1);


                                        if (qty.length() == 0
                                                && holder.shelfPcsQty.getText().toString().length() == 0
                                                && holder.shelfouter.getText().toString().length() == 0) {
                                            holder.productObj.getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);
                                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                                            holder.imageButton_availability.setChecked(false);
                                        }
                                    }

                                    int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                                    holder.total.setText(totValue + "");
                                    if (totValue > 0) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setReasonId(0);
                                    } else {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(stockCheckPresenter.getReasonIndex(holder.productObj
                                                .getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getReasonId() + ""));

                                    }
                                    updateFooter();
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                }
                            });

                    holder.shelfouter.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {

                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void afterTextChanged(Editable s) {

                            if (holder.productObj.getOuUomid() == 0) {
                                holder.shelfouter.removeTextChangedListener(this);
                                holder.shelfouter.setText("");
                                holder.shelfouter.addTextChangedListener(this);
                                return;
                            }

                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.shelfouter.setSelection(qty.length());
                            if (!qty.equals("")) {
                                int shelf_o_qty = SDUtil
                                        .convertToInt(holder.shelfouter
                                                .getText().toString());
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setShelfOuter(shelf_o_qty);

                                if (shelf_o_qty > 0
                                        || SDUtil.convertToInt(holder.shelfPcsQty.getText().toString()) > 0
                                        || SDUtil.convertToInt(holder.shelfCaseQty.getText().toString()) > 0) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);
                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
                                    holder.imageButton_availability.setChecked(true);

                                } else if (shelf_o_qty == 0) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);
                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                                    holder.imageButton_availability.setChecked(true);
                                }


                            } else {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setShelfOuter(-1);

                                if (qty.length() == 0
                                        && holder.shelfPcsQty.getText().toString().length() == 0
                                        && holder.shelfCaseQty.getText().toString().length() == 0) {
                                    holder.productObj.getLocations()
                                            .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);
                                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                                    holder.imageButton_availability.setChecked(false);
                                }


                            }


                            int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                            holder.total.setText(totValue + "");
                            if (totValue > 0) {
                                holder.mReason.setEnabled(false);
                                holder.mReason.setSelected(false);
                                holder.mReason.setSelection(0);
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setReasonId(0);
                            } else {
                                holder.mReason.setEnabled(true);
                                holder.mReason.setSelected(true);
                                holder.mReason.setSelection(stockCheckPresenter.getReasonIndex(holder.productObj
                                        .getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getReasonId() + ""));

                            }
                            updateFooter();
                        }
                    });


                    holder.facingQty.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            if (holder.productObj.getPcUomid() == 0
                                    && holder.productObj.getCaseUomId() == 0
                                    && holder.productObj.getOuUomid() == 0) {
                                holder.facingQty.removeTextChangedListener(this);
                                holder.facingQty.setText("0");
                                holder.facingQty.addTextChangedListener(this);
                                return;
                            }

                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.facingQty.setSelection(qty.length());
                            if (!qty.equals("")) {
                                int w_cqty = SDUtil
                                        .convertToInt(holder.facingQty
                                                .getText().toString());

                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setFacingQty(w_cqty);
                                String strProductObj = stockCheckPresenter.getProductTotalValue(holder.productObj)
                                        + "";
                                holder.total.setText(strProductObj);
                            } else {
                                holder.facingQty.setText("0");
                            }
                        }
                    });

                    holder.facingQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.facingQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.facingQty
                                            .getInputType();
                                    holder.facingQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.facingQty.onTouchEvent(event);
                                    holder.facingQty.setInputType(inType);
                                    holder.facingQty.requestFocus();
                                    if (holder.facingQty.getText().length() > 0)
                                        holder.facingQty.setSelection(holder.facingQty.getText().length());
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchProductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfPcsQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {

                                    QUANTITY = holder.shelfPcsQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfPcsQty
                                            .getInputType();
                                    holder.shelfPcsQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfPcsQty.onTouchEvent(event);
                                    holder.shelfPcsQty.setInputType(inType);
                                    holder.shelfPcsQty.requestFocus();
                                    if (holder.shelfPcsQty.getText().length() > 0)
                                        holder.shelfPcsQty.setSelection(holder.shelfPcsQty.getText().length());
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchProductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfCaseQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {

                                    QUANTITY = holder.shelfCaseQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfCaseQty
                                            .getInputType();
                                    holder.shelfCaseQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfCaseQty.onTouchEvent(event);
                                    holder.shelfCaseQty.setInputType(inType);
                                    holder.shelfCaseQty.requestFocus();
                                    if (holder.shelfCaseQty.getText().length() > 0)
                                        holder.shelfCaseQty.setSelection(holder.shelfCaseQty.getText().length());
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchProductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfouter.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {

                            QUANTITY = holder.shelfouter;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfouter.getInputType();
                            holder.shelfouter.setInputType(InputType.TYPE_NULL);
                            holder.shelfouter.onTouchEvent(event);
                            holder.shelfouter.setInputType(inType);
                            holder.shelfouter.requestFocus();
                            if (holder.shelfouter.getText().length() > 0)
                                holder.shelfouter.setSelection(holder.shelfouter.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchProductName.getWindowToken(), 0);
                            return true;
                        }
                    });


                    row.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);

                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchProductName.getWindowToken(), 0);

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.showPrevious();
                            }
                        }
                    });

                    row.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            businessModel = (BusinessModel) context.getApplicationContext();
                            businessModel.setContext((Activity) context);
                            List<SchemeBO> schemeList = null;
                            SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(context.getApplicationContext());
                            try {
                                schemeList = schemeHelper.getSchemeList();
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                            if (businessModel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                                if (schemeList == null
                                        || schemeList.size() == 0) {
                                    Toast.makeText(context,
                                            "scheme not available.",
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                businessModel.productHelper.setSchemes(schemeHelper.getSchemeList());
                                businessModel.productHelper.setPdname(holder.pname);
                                businessModel.productHelper.setProdId(holder.productId);
                                businessModel.productHelper.setProductObj(holder.productObj);
                                businessModel.productHelper.setFlag(1);
                                businessModel.productHelper.setTotalScreenSize(0);

                                Intent intent = new Intent(context, ProductSchemeDetailsActivity.class);
                                intent.putExtra("productId", holder.productId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            } else {
                                businessModel.productHelper.setPdname(holder.pname);
                                businessModel.productHelper.setProdId(holder.productId);
                                businessModel.productHelper.setProductObj(holder.productObj);
                                businessModel.productHelper.setFlag(1);
                                businessModel.productHelper.setTotalScreenSize(0);

                                SchemeDialog sc = new SchemeDialog(
                                        getActivity(), schemeList,
                                        holder.pname, holder.productId,
                                        holder.productObj, 1, 0);
                                FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
                                sc.show(fm, "");
                            }
                            return true;
                        }
                    });

                    holder.psname.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            businessModel = (BusinessModel) context.getApplicationContext();
                            businessModel.setContext(getActivity());

                            SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(context.getApplicationContext());

                            //if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                            if (schemeHelper
                                    .getSchemeList() == null
                                    || schemeHelper
                                    .getSchemeList().size() == 0) {
                                Toast.makeText(getActivity(),
                                        R.string.scheme_not_available,
                                        Toast.LENGTH_SHORT).show();
                            }

                            //This objects reference is used only in Product Detail screen.
                            // This should be removed while cleaning product detail screen
                            businessModel.productHelper.setSchemes(schemeHelper.getSchemeList());
                            businessModel.productHelper.setPdname(holder.pname);
                            businessModel.productHelper.setProdId(holder.productId);
                            businessModel.productHelper.setProductObj(holder.productObj);
                            businessModel.productHelper.setFlag(1);
                            businessModel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                            Intent intent = new Intent(getActivity(), ProductSchemeDetailsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("isFromStockCheck", true);
                            intent.putExtra("productId", holder.productId);
                            startActivity(intent);
                        }
                    });

                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.productObj = product;

                holder.productId = holder.productObj.getProductID();

                try {
                    holder.psname.setTextColor(holder.productObj.getTextColor());
                } catch (Exception e) {
                    Commons.printException(e);
                    holder.psname.setTextColor(ContextCompat.getColor(context, R.color.list_item_primary_text_color));
                }


                if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == IvyConstants.AUDIT_DEFAULT)
                    holder.audit.setImageResource(R.drawable.ic_audit_none);
                else if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == IvyConstants.AUDIT_OK)
                    holder.audit.setImageResource(R.drawable.ic_audit_yes);
                else if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == IvyConstants.AUDIT_NOT_OK)
                    holder.audit.setImageResource(R.drawable.ic_audit_no);

                holder.pname = holder.productObj.getProductName();


                holder.psname.setText(holder.productObj.getProductShortName());
                if (businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                    holder.productCode.setVisibility(View.VISIBLE);
                    String prodCode = getResources().getString(R.string.prod_code) + ": " +
                            holder.productObj.getProductCode() + " ";
                    if (businessModel.labelsMasterHelper.applyLabels(holder.productCode.getTag()) != null)
                        prodCode = businessModel.labelsMasterHelper
                                .applyLabels(holder.productCode.getTag()) + ": " +
                                holder.productObj.getProductCode() + " ";
                    holder.productCode.setText(prodCode);
                }

                if (businessModel.configurationMasterHelper.SHOW_BARCODE) {
                    holder.tvbarcode.setVisibility(View.VISIBLE);
                    String barCode = getResources().getString(R.string.barcode) + ": " +
                            holder.productObj.getBarCode() + " ";

                    if (businessModel.labelsMasterHelper.applyLabels(holder.tvbarcode.getTag()) != null)
                        barCode = businessModel.labelsMasterHelper
                                .applyLabels(holder.tvbarcode.getTag()) + ": " +
                                holder.productObj.getBarCode() + " ";

                    holder.tvbarcode.setText(barCode);
                }


                String strPPQ = getResources().getString(R.string.ppq) + ": "
                        + holder.productObj.getRetailerWiseProductWiseP4Qty() + " ";
                holder.ppq.setText(strPPQ);
                String strPSQ = getResources().getString(R.string.psq) + ": "
                        + holder.productObj.getRetailerWiseP4StockQty();
                holder.psq.setText(strPSQ);


                if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                    holder.imageButton_availability.setChecked(true);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));

                } else if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                    holder.imageButton_availability.setChecked(true);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.RED)));
                } else if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == -1) {
                    holder.imageButton_availability.setChecked(false);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.checkbox_default_color)));
                }

                if (stockCheckHelper.SHOW_STOCK_PRICE_TAG_AVAIL) {
                    if (holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex)
                            .getPriceTagAvailability() == 1)
                        holder.chkPriceTagAvail.setChecked(true);
                    else
                        holder.chkPriceTagAvail.setChecked(false);
                }

                if (stockCheckHelper.SHOW_STOCK_FC) {
                    String strFacingQty = holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getFacingQty() + "";
                    holder.facingQty.setText(strFacingQty);
                }

                if (stockCheckHelper.SHOW_STOCK_SP) {
                    if (holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex).getShelfPiece() >= 0) {
                        String strShelfPiece = holder.productObj.getLocations()
                                .get(stockCheckPresenter.mSelectedLocationIndex).getShelfPiece()
                                + "";
                        holder.shelfPcsQty.setText(strShelfPiece);
                    } else {
                        holder.shelfPcsQty.setText("");
                    }
                }

                if (stockCheckHelper.SHOW_STOCK_SC) {
                    if (holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex).getShelfCase() >= 0) {
                        String strShelfCase = holder.productObj.getLocations()
                                .get(stockCheckPresenter.mSelectedLocationIndex).getShelfCase()
                                + "";
                        holder.shelfCaseQty.setText(strShelfCase);
                    } else {
                        holder.shelfCaseQty.setText("");
                    }
                }
                if (stockCheckHelper.SHOW_SHELF_OUTER) {
                    if (holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex).getShelfOuter() >= 0) {
                        String strShelfOuter = holder.productObj.getLocations()
                                .get(stockCheckPresenter.mSelectedLocationIndex).getShelfOuter()
                                + "";
                        holder.shelfouter.setText(strShelfOuter);
                    } else {
                        holder.shelfouter.setText("");
                    }
                }

                if (stockCheckHelper.SHOW_STOCK_RSN) {
                    if (holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex)
                            .getShelfPiece() == 0 || holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                        holder.mReason.setEnabled(true);
                        holder.mReason.setSelected(true);
                        holder.mReason.setSelection(stockCheckPresenter.getReasonIndex(holder.productObj
                                .getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getReasonId() + ""));
                    } else {
                        holder.mReason.setEnabled(false);
                        holder.mReason.setSelected(false);
                        holder.mReason.setSelection(0);
                    }
                }

                if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                    holder.shelfouter.setEnabled(false);
                } else {
                    holder.shelfouter.setEnabled(true);
                }
                if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                    holder.shelfCaseQty.setEnabled(false);
                } else {
                    holder.shelfCaseQty.setEnabled(true);
                }
                if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                    holder.shelfPcsQty.setEnabled(false);
                } else {
                    holder.shelfPcsQty.setEnabled(true);
                }

                //Disable while all the UOM is not available
                if ((holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped())
                        && (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped())
                        && (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) || isPreVisit) {
                    holder.imageButton_availability.setEnabled(false);
                    holder.facingQty.setEnabled(false);
                } else {
                    holder.imageButton_availability.setEnabled(true);
                    holder.facingQty.setEnabled(true);
                }

                if (businessModel.configurationMasterHelper.isAuditEnabled()) {

                    holder.audit.setVisibility(View.VISIBLE);

                    // holder.avail_cb.setEnabled(false);

                    holder.shelfPcsQty.setEnabled(false);
                    holder.shelfCaseQty.setEnabled(false);
                    holder.shelfouter.setEnabled(false);
                    holder.imageButton_availability.setEnabled(false);

                    holder.mReason.setEnabled(false);


                }


            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return row;
        }
    }

    public class ViewHolder {

        private String productId;
        private String pname;
        private ProductMasterBO productObj;
        private TextView psname;
        private TextView ppq;
        private TextView tvbarcode;
        private TextView psq;
        private TextView barcode;
        private EditText shelfPcsQty;
        private EditText shelfCaseQty;
        private EditText shelfouter;
        private EditText facingQty;


        private TextView total, productCode;


        private LinearLayout ll_stkCB;

        private Spinner mReason;
        ImageButton audit;
        AppCompatCheckBox imageButton_availability;
        private RelativeLayout rl_priceTagCB;
        AppCompatCheckBox chkPriceTagAvail;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            if (!stockCheckPresenter.generalButton.equals(stockCheckPresenter.GENERAL))
                menu.findItem(R.id.menu_spl_filter).setIcon(
                        R.drawable.ic_action_star_select);

            if (businessModel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            if (businessModel.configurationMasterHelper.floating_Survey)
                menu.findItem(R.id.menu_survey).setVisible(true);

            if (!businessModel.configurationMasterHelper.SHOW_SPL_FILTER)
                stockCheckPresenter.hideSpecialFilter();

            boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
            if (businessModel.configurationMasterHelper.IS_BAR_CODE_STOCK_CHECK) {
                menu.findItem(R.id.menu_barcode).setVisible(true);
            }
            menu.findItem(R.id.menu_next).setVisible(false);

            if (stockCheckPresenter.remarks_button_enable)
                menu.findItem(R.id.menu_remarks).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_remarks).setVisible(false);

            menu.findItem(R.id.menu_scheme).setVisible(false);

            if (stockCheckPresenter.isSpecialFilter_enabled && !businessModel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_spl_filter).setVisible(false);

            menu.findItem(R.id.menu_apply_so).setVisible(false);

            menu.findItem(R.id.menu_apply_std_qty).setVisible(false);

            if (businessModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (stockCheckPresenter.getLocationAdapter().getCount() < 2
                        || !stockCheckHelper.SHOW_STOCK_LOCATION_FILTER)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
                else menu.findItem(R.id.menu_loc_filter).setVisible(true);
            }

            menu.findItem(R.id.menu_sih_apply).setVisible(false);

            menu.findItem(R.id.menu_sih_apply).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (businessModel.productHelper.isFilterAvaiable("MENU_STK_ORD")) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
                menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
            }

            if (stockCheckPresenter.mSelectedIdByLevelId != null) {
                for (Integer id : stockCheckPresenter.mSelectedIdByLevelId.keySet()) {
                    if (stockCheckPresenter.mSelectedIdByLevelId.get(id) > 0) {
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

            if (!businessModel.configurationMasterHelper.SHOW_REMARKS_STK_CHK) {
                stockCheckPresenter.hideRemarksButton();
                menu.findItem(R.id.menu_remarks).setVisible(false);
            } else
                menu.findItem(R.id.menu_remarks).setVisible(true);

            menu.findItem(R.id.menu_reason).setVisible(businessModel.configurationMasterHelper.floating_np_reason_photo);
            if (drawerOpen)
                menu.clear();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            ((FragmentActivity) context).supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_remarks) {
            onNoteButtonClick();
            return true;
        } else if (i == R.id.menu_apply_so) {
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            return true;
        } else if (i == R.id.menu_sih_apply) {
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (businessModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                stockCheckPresenter.putValueToFilterMap("");
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_barcode) {
            ((IvyBaseActivityNoActionBar) context).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        StockCheckFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
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
        } else if (i == R.id.menu_reason) {
            businessModel.reasonHelper.downloadNpReason(businessModel.retailerMasterBO.getRetailerID(), "MENU_STOCK");
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (businessModel.reasonHelper.isNpReasonPhotoAvaiable(businessModel.retailerMasterBO.getRetailerID(), "MENU_STOCK")) {
                        businessModel.saveModuleCompletion("MENU_STOCK", true);

                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        if (isPreVisit)
                            intent.putExtra("PreVisit", true);

                        startActivity(intent);
                        ((Activity) context).finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_STOCK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        } else if (i == R.id.menu_competitor_filter) {
            competitorFilterClickedFragment();
            ((FragmentActivity) context).supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void FiveFilterFragment() {
        try {


            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", stockCheckPresenter.mSelectedIdByLevelId);
            bundle.putBoolean("isTag", true);

            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(stockCheckPresenter.getLocationAdapter(),
                stockCheckPresenter.mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        stockCheckPresenter.mSelectedLocationIndex = item;
                        dialog.dismiss();
                        refreshList(stockList);
                    }
                });

        businessModel.applyAlertDialogTheme(builder);
    }

    private void onNoteButtonClick() {
        FragmentTransaction ft = ((FragmentActivity) context)
                .getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog("MENU_CLOSING");
        dialog.setCancelable(false);
        dialog.show(ft, "stk_chk_remark");
    }

    private void onNextButtonClick() {
        if (businessModel.configurationMasterHelper.IS_MUST_SELL_STK
                && !businessModel.productHelper.isMustSellFilledStockCheck(true, getActivity())) {
            Toast.makeText(getActivity(), R.string.fill_must_sell, Toast.LENGTH_SHORT).show();
            return;
        }
        stockCheckPresenter.saveClosingStock(stockList);
    }

    @Override
    public void savePromptMessage(int type, String text) {
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        if (type == 0) {
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)

                    .setTitle(getResources().getString(R.string.no_data_tosave))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                }
                            });
        } else if (type == 1) {

            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)

                    .setTitle(getResources().getString(R.string.reason_required_for) + getResources().getString(R.string.non_stock_products))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                }
                            });
        }
        businessModel.applyAlertDialogTheme(alertDialogBuilder1);
    }

    private void loadSpecialFilterView(View view) {
        hscrl_spl_filter = view.findViewById(R.id.hscrl_spl_filter);
        hscrl_spl_filter.setVisibility(View.VISIBLE);
        ll_spl_filter = view.findViewById(R.id.ll_spl_filter);
        ll_tab_selection = view.findViewById(R.id.ll_tab_selection);
        stockCheckPresenter.getGeneralFilter().add(0, new ConfigureBO("ALL", "All", "0", 0, 1, 1));

        Vector<ConfigureBO> generalFilter = stockCheckPresenter.getGeneralFilter();
        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        int width = (int) (scale / generalFilter.size());

        float den = getContext().getResources().getDisplayMetrics().density;
        float dimen_wd = getResources().getDimension(R.dimen.special_filter_item_width);
        if (width < (int) (dimen_wd * den + 25.5f)) {
            scale = den;
            width = (int) (dimen_wd * scale + 25.5f);
        }
        final TabLayout tabLay = view.findViewById(R.id.dummy_tab_lay);
        final TabLayout.Tab tab1 = tabLay.newTab();
        tab1.setText("ABCD");
        tabLay.addTab(tab1);

        tabLay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tabLay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    tabLay.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                LinearLayout.LayoutParams obj = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (tabLay.getHeight() - getResources().getDimension(R.dimen.tab_indicator_height)));
                ll_spl_filter.setLayoutParams(obj);
                tabLay.setVisibility(View.GONE);

            }

        });

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

                    if (view.getTag().toString().equalsIgnoreCase("ALL")) {
                        updateGeneralText("");
                    } else {
                        updateGeneralText(view.getTag().toString());
                    }
                    if (businessModel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        selectedTabTag = view.getTag();
                        selectTab(view.getTag());
                    }
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
                updateGeneralText("");
            } else {
                tv_selection_identifier.setVisibility(View.GONE);
            }

            ll_tab_selection.addView(tv_selection_identifier);


        }


    }

    @SuppressLint({"SetTextI18n", "UseSparseArrays"})
    private void selectTab(Object tag) {
        for (ConfigureBO config : stockCheckPresenter.getGeneralFilter()) {
            View view = getView().findViewWithTag(config.getConfigCode());
            View view1 = getView().findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName() + "(" + stockList.size() + ")");
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
        if (!tag.toString().equalsIgnoreCase("All")) {
            stockCheckPresenter.mCompetitorSelectedIdByLevelId = new HashMap<>();
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    @SuppressLint({"SetTextI18n", "UseSparseArrays"})
    private void selectTab(View pview, Object tag) {
        for (ConfigureBO config : stockCheckPresenter.getGeneralFilter()) {
            View view = pview.findViewWithTag(config.getConfigCode());
            View view1 = pview.findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName() + "(" + stockList.size() + ")");
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
        if (!tag.toString().equalsIgnoreCase("All")) {
            stockCheckPresenter.mCompetitorSelectedIdByLevelId = new HashMap<>();
        }
        getActivity().supportInvalidateOptionsMenu();

    }

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
                    Button ed = getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    public void onClick(View v) {
        Button vw = (Button) v;
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtn_clear) {
            viewFlipper.showPrevious();
            if (mEdt_searchProductName.getText().length() > 0)
                mEdt_searchProductName.setText("");
            stockCheckPresenter.loadProductList();

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.select_dialog_singlechoice,
                    stockCheckPresenter.mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            businessModel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = stockCheckPresenter.mSearchTypeArray.indexOf(businessModel
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

        } else if (vw == btn_save) {
            onNextButtonClick();
        }
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

    @Override
    public void onResume() {
        super.onResume();
        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        Commons.print("OnResume Called");
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        stockCheckPresenter.getFilteredList(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                stockCheckPresenter.strBarCodeSearch = result.getContents();
                if (stockCheckPresenter.strBarCodeSearch != null && !"".equals(stockCheckPresenter.strBarCodeSearch)) {
                    businessModel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                    mEdt_searchProductName.setText(stockCheckPresenter.strBarCodeSearch);
                    if (viewFlipper.getDisplayedChild() == 0) {
                        viewFlipper.showNext();
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFooter() {

        int totalAvailableProduts = 0;
        int totalownedProducts = 0;
        try {
            for (ProductMasterBO bo : stockList) {

                for (LocationBO locationBO : bo.getLocations()) {

                    if ((locationBO.getShelfCase() > 0 || locationBO.getShelfOuter() > 0 || locationBO.getShelfPiece() > 0)) {
                        if (locationBO.getAvailability() > -1) {
                            totalAvailableProduts += 1;
                        }
                        if (locationBO.getAvailability() == 1 && bo.getOwn() == 1) {
                            totalownedProducts += 1;
                        }
                    }
                    break;
                }
            }
            tv_total_stockCheckedProducts.setText(totalAvailableProduts + "");
            tv_total_products.setText("/" + stockList.size());
            if (stockList.size() > 0) {
                businessModel.setAvailablilityShare(businessModel.formatPercent(((double) totalownedProducts / stockList.size()) * 100));
                tv_sharePercent.setText(businessModel.getAvailablilityShare());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Special Filter Fragment.
     */
    private void generalFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", stockCheckPresenter.GENERAL);

            bundle.putSerializable("serilizeContent",
                    stockCheckPresenter.getGeneralFilter());

            // set Fragmentclass Arguments
            SpecialFilterFragment fragobj = new SpecialFilterFragment(stockCheckPresenter.getSelectedFilterMap());
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
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
    public void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getResources().getString(R.string.saving));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void showStockSavedDialog() {
        if (businessModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.next),
                    getActivity().getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                    Bundle extras = getActivity().getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtra("IsMoveNextActivity", businessModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                        intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));

                        if (isPreVisit)
                            intent.putExtra("PreVisit", true);
                    }

                    startActivity(intent);
                    getActivity().finish();

                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {

                    Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);
                    startActivity(intent);
                    getActivity().finish();

                }
            }).show();
        } else {
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
                        intent.putExtra("PreVisit", true);

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

    @Override
    public void showAlert() {
        businessModel.showAlert(getResources().getString(R.string.no_products_exists), 0);
    }

    @Override
    public void updateListFromFilter(ArrayList<ProductMasterBO> stockList) {
        refreshList(stockList);
        if (selectedTabTag != null) {
            selectTab(selectedTabTag);
        }
        //}
        getActivity().invalidateOptionsMenu();
        mDrawerLayout.closeDrawers();
        updateFooter();
    }

    @Override
    public void showSearchValidationToast() {
        Toast.makeText(getActivity(), getResources().getString(R.string.enter_atleast_three_letters), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void scrollToSelectedTabPosition() {
        if (hscrl_spl_filter != null) {
            hscrl_spl_filter.scrollTo(x, y);
        }
        selectTab("ALL");
    }

    private final DialogInterface.OnCancelListener diagDismissListen = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            onNextButtonClick();
        }
    };


    private void onBackButonClick() {

        if (stockCheckHelper.hasStockCheck()) {
            showBackDialog();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                stockCheckPresenter.returnToHome();
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit", true);

                if (isFromChild)
                    startActivity(intent.putExtra("isStoreMenu", true));
                else
                    startActivity(intent);

                ((Activity) context).finish();
            }
            ((Activity) context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showBackDialog() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                stockCheckPresenter.returnToHome();
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit", true);

                if (isFromChild)
                    startActivity(intent.putExtra("isStoreMenu", true));
                else
                    startActivity(intent);

                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

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
