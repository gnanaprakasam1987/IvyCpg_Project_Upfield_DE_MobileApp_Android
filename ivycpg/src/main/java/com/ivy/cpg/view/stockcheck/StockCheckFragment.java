package com.ivy.cpg.view.stockcheck;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
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
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.MustSellReasonDialog;
import com.ivy.sd.png.view.ProductSchemeDetailsActivity;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SchemeDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class StockCheckFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener,
        CompetitorFilterInterface, StockCheckContractor.StockCheckView {


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
    private MustSellReasonDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stockcheck,
                container, false);

        businessModel = (BusinessModel) getActivity().getApplicationContext();


        initializeViews(view);

        stockCheckPresenter = new StockCheckPresenterImpl(getContext());
        stockCheckPresenter.setView(this);

        try {
            isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);

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
        ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(3);
        setHasOptionsMenu(true);
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


        listview = (ListView) view.findViewById(R.id.list);
        listview.setCacheColorHint(0);

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        drawer = (FrameLayout) view.findViewById(R.id.right_drawer);

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

        inputManager = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);
        productName = (TextView) view.findViewById(R.id.productName);
        productName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mEdt_searchProductName = (EditText) view.findViewById(
                R.id.edt_searchproductName);
        mEdt_searchProductName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mBtn_Search = (Button) view.findViewById(R.id.btn_search);
        mBtn_Search.setOnClickListener(this);
        mBtn_clear = (Button) view.findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);
        mBtnFilterPopup = (Button) view.findViewById(R.id.btn_filter_popup);
        mBtnFilterPopup.setOnClickListener(this);

        tv_total_stockCheckedProducts = (TextView) view.findViewById(R.id.tv_stockCheckedProductscount);
        tv_total_products = (TextView) view.findViewById(R.id.tv_productsCount);

        tv_total_stockCheckedProducts.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_total_products.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));


        btn_save = (Button) view.findViewById(R.id.btn_save);
        btn_save.setTypeface(businessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
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

        mBtnFilterPopup = (Button) view.findViewById(R.id.btn_filter_popup);

    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void hideAndSeek() {
        try {


            if (!businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
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

            if (!businessModel.configurationMasterHelper.SHOW_STOCK_SP) {
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

            if (!businessModel.configurationMasterHelper.SHOW_STOCK_CB)
                view.findViewById(R.id.shelfPcsCB).setVisibility(View.GONE);


            if (!businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
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

            if (!businessModel.configurationMasterHelper.SHOW_SHELF_OUTER
                    && !businessModel.configurationMasterHelper.SHOW_STOCK_SP
                    && !businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
                view.findViewById(R.id.shelf_layout).setVisibility(View.GONE);

            }

            if (!businessModel.configurationMasterHelper.SHOW_STOCK_TOTAL) {
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


            if (!businessModel.configurationMasterHelper.SHOW_STOCK_FC) {
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
            if (businessModel.configurationMasterHelper.SHOW_STOCK_CB && !businessModel.configurationMasterHelper.SHOW_STOCK_FC &&
                    !businessModel.configurationMasterHelper.SHOW_STOCK_SC && !businessModel.configurationMasterHelper.SHOW_STOCK_SP &&
                    !businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                view.findViewById(R.id.ll_keypad).setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void loadSearchedList() {
        stockCheckPresenter.loadSearchedList(mEdt_searchProductName.getText().toString());
    }

    private void refreshList(ArrayList<ProductMasterBO> stockList) {
        this.stockList = stockList;
        MyAdapter mSchedule = new MyAdapter(stockList);
        listview.setAdapter(mSchedule);
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

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_closingstock, items);
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

        @NonNull
        @SuppressLint("RestrictedApi")
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            try {
                final ViewHolder holder;
                final ProductMasterBO product = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.activity_stock_check_listview, parent,
                            false);
                    holder = new ViewHolder();
                    holder.audit = (ImageButton) row
                            .findViewById(R.id.btn_audit);
                    holder.psname = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.psname.setTypeface(businessModel.configurationMasterHelper.getProductNameFont());
                    holder.psname.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                    holder.ppq = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_ppq);
                    holder.ppq.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.psq = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_psq);
                    holder.psq.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    holder.mReason = (Spinner) row.findViewById(R.id.reason);

                    holder.shelfPcsQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sp_qty);
                    holder.shelfCaseQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sc_qty);
                    holder.shelfouter = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_shelfouter_qty);
                    holder.shelfPcsQty.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfCaseQty.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfouter.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    holder.ll_stkCB = (LinearLayout) row
                            .findViewById(R.id.ll_stock_and_order_listview_cb);

                    holder.imageButton_availability = (AppCompatCheckBox) row
                            .findViewById(R.id.btn_availability);
                    holder.total = (TextView) row
                            .findViewById(R.id.stock_check_listview_total);
                    holder.total.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                    holder.facingQty = (EditText) row
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

                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_FC)
                        holder.facingQty.setVisibility(View.GONE);
                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_SC)
                        holder.shelfCaseQty.setVisibility(View.GONE);
                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_SP)
                        holder.shelfPcsQty.setVisibility(View.GONE);
                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_CB)
                        holder.ll_stkCB.setVisibility(View.GONE);

                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_RSN)
                        holder.mReason.setVisibility(View.GONE);


                    if (!businessModel.configurationMasterHelper.SHOW_SHELF_OUTER)
                        holder.shelfouter.setVisibility(View.GONE);
                    if (!businessModel.configurationMasterHelper.SHOW_STOCK_TOTAL)
                        holder.total.setVisibility(View.GONE);

                    if (!businessModel.configurationMasterHelper.SHOW_SHELF_OUTER
                            && !businessModel.configurationMasterHelper.SHOW_STOCK_SP
                            && !businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
                        row.findViewById(R.id.layout_shelf).setVisibility(View.GONE);

                    }

                    holder.audit.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAudit() == 2) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAudit(1);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_yes);
                            } else if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAudit() == 1) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAudit(0);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_no);
                            } else if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAudit() == 0) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAudit(2);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_none);
                            }
                        }
                    });

                    holder.imageButton_availability.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == -1) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(1);

                                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.GREEN)));
                                holder.imageButton_availability.setChecked(true);

                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                    holder.mReason.setEnabled(false);
                                    holder.mReason.setSelected(false);
                                    holder.mReason.setSelection(0);
                                    holder.productObj.setReasonID("0");
                                }
                            } else if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(0);

                                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.RED)));
                                holder.imageButton_availability.setChecked(true);

                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                    holder.mReason.setEnabled(true);
                                    holder.mReason.setSelected(true);
                                    holder.mReason.setSelection(0);
                                }
                            } else if (holder.productObj.getLocations()
                                    .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex).setAvailability(-1);

                                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.checkbox_default_color)));
                                holder.imageButton_availability.setChecked(false);

                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                    holder.mReason.setEnabled(false);
                                    holder.mReason.setSelected(false);
                                    holder.mReason.setSelection(0);
                                    holder.productObj.setReasonID("0");
                                }
                            }

                            updateFooter();

                        }
                    });
                         /*   holder.avail_cb
                                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(
                                                CompoundButton buttonView,
                                                boolean isChecked) {
                                            if (isChecked
                                                    && holder.productObj
                                                    .getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex)
                                                    .getShelfPiece() == -1) {
                                                if (businessModel.configurationMasterHelper.SHOW_STOCK_SP) {
                                                    if (holder.shelfPcsQty.getText().toString().length() == 0)
                                                        holder.shelfPcsQty.setText("1");
                                                } else if (businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
                                                    if (holder.shelfCaseQty.getText().toString().length() == 0)
                                                        holder.shelfCaseQty.setText("1");
                                                } else if (businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                                    if (holder.shelfouter.getText().toString().length() == 0)
                                                        holder.shelfouter.setText("1");
                                                } else if (!businessModel.configurationMasterHelper.SHOW_STOCK_SP
                                                        && !businessModel.configurationMasterHelper.SHOW_STOCK_SC
                                                        && !businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                                    holder.productObj.getLocations()
                                                            .get(stockCheckPresenter.mSelectedLocationIndex)
                                                            .setShelfPiece(1);
                                                }
                                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                                    holder.mReason.setEnabled(false);
                                                    holder.mReason.setSelected(false);
                                                    holder.mReason.setSelection(0);
                                                    holder.productObj.setReasonID("0");
                                                }
                                            } else if (isChecked
                                                    && holder.productObj
                                                    .getLocations()
                                                    .get(stockCheckPresenter.mSelectedLocationIndex)
                                                    .getShelfPiece() > 0) {
                                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                                    holder.mReason.setEnabled(false);
                                                    holder.mReason.setSelected(false);
                                                    holder.mReason.setSelection(0);
                                                    holder.productObj.setReasonID("0");
                                                }
                                            } else if (!isChecked) {
                                                if (businessModel.configurationMasterHelper.SHOW_STOCK_SP) {
                                                    if (holder.shelfPcsQty.getText().toString().length() == 0)
                                                        holder.shelfPcsQty.setText("");
                                                    else if (holder.shelfPcsQty.getText().toString().length() > 0)
                                                        holder.shelfPcsQty.setText("");
                                                } else if (businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
                                                    if (holder.shelfCaseQty.getText().toString().length() == 0)
                                                        holder.shelfCaseQty.setText("");
                                                    else if (holder.shelfCaseQty.getText().toString().length() > 0)
                                                        holder.shelfCaseQty.setText("");
                                                } else if (businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                                    if (holder.shelfouter.getText().toString().length() == 0)
                                                        holder.shelfouter.setText("");
                                                    else if (holder.shelfouter.getText().toString().length() > 0)
                                                        holder.shelfouter.setText("");
                                                } else if (!businessModel.configurationMasterHelper.SHOW_STOCK_SP
                                                        && !businessModel.configurationMasterHelper.SHOW_STOCK_SC
                                                        && !businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                                    holder.productObj.getLocations()
                                                            .get(stockCheckPresenter.mSelectedLocationIndex)
                                                            .setShelfPiece(-1);
                                                }
                                                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                                    holder.mReason.setEnabled(true);
                                                    holder.mReason.setSelected(true);
                                                    holder.mReason.setSelection(0);
                                                }
                                            }
                                            updateFooter();
                                        }
                                    });*/

                    holder.mReason.setAdapter(stockCheckPresenter.getSpinnerAdapter());
                    holder.mReason
                            .setOnItemSelectedListener(new OnItemSelectedListener() {
                                public void onItemSelected(
                                        AdapterView<?> parent, View view,
                                        int position, long id) {

                                    ReasonMaster reString = (ReasonMaster) holder.mReason
                                            .getSelectedItem();

                                    holder.productObj.setReasonID(reString
                                            .getReasonID());

                                }

                                public void onNothingSelected(
                                        AdapterView<?> parent) {
                                }
                            });


                    holder.shelfPcsQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int sp_qty = SDUtil
                                                .convertToInt(holder.shelfPcsQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfPiece(sp_qty);

                                    } else {
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfPiece(-1);
                                    }

                                    int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                                    holder.total
                                            .setText(totValue + "");
                                    if (totValue > 0) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.setReasonID("0");
                                    } else {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(0);
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

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int shelf_case_qty = SDUtil
                                                .convertToInt(holder.shelfCaseQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfCase(shelf_case_qty);

                                    } else {
                                        holder.productObj.getLocations()
                                                .get(stockCheckPresenter.mSelectedLocationIndex)
                                                .setShelfCase(-1);
                                    }

                                    int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                                    holder.total
                                            .setText(totValue + "");
                                    if (totValue > 0) {
                                        holder.mReason.setEnabled(false);
                                        holder.mReason.setSelected(false);
                                        holder.mReason.setSelection(0);
                                        holder.productObj.setReasonID("0");
                                    } else {
                                        holder.mReason.setEnabled(true);
                                        holder.mReason.setSelected(true);
                                        holder.mReason.setSelection(0);

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

                        @Override
                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int shelf_o_qty = SDUtil
                                        .convertToInt(holder.shelfouter
                                                .getText().toString());
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setShelfOuter(shelf_o_qty);

                            } else {
                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setShelfOuter(-1);

                            }


                            int totValue = stockCheckPresenter.getProductTotalValue(holder.productObj);
                            holder.total
                                    .setText(totValue + "");
                            if (totValue > 0) {
                                holder.mReason.setEnabled(false);
                                holder.mReason.setSelected(false);
                                holder.mReason.setSelection(0);
                                holder.productObj.setReasonID("0");
                            } else {
                                holder.mReason.setEnabled(true);
                                holder.mReason.setSelected(true);
                                holder.mReason.setSelection(0);

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
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int w_cqty = SDUtil
                                        .convertToInt(holder.facingQty
                                                .getText().toString());

                                holder.productObj.getLocations()
                                        .get(stockCheckPresenter.mSelectedLocationIndex)
                                        .setFacingQty(w_cqty);
                                String strProductObj = stockCheckPresenter.getProductTotalValue(holder.productObj)
                                        + "";
                                holder.total
                                        .setText(strProductObj);
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
                                    holder.facingQty.selectAll();
                                    holder.facingQty.requestFocus();
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
                                    holder.shelfPcsQty.selectAll();
                                    holder.shelfPcsQty.requestFocus();
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
                                    holder.shelfCaseQty.selectAll();
                                    holder.shelfCaseQty.requestFocus();
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
                            holder.shelfouter.selectAll();
                            holder.shelfouter.requestFocus();
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
                            businessModel = (BusinessModel) getActivity().getApplicationContext();
                            businessModel.setContext(getActivity());
                            List<SchemeBO> schemeList = null;
                            SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext());
                            try {
                                schemeList = schemeHelper.getSchemeList();
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                            if (businessModel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                                if (schemeList == null
                                        || schemeList.size() == 0) {
                                    Toast.makeText(getActivity(),
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

                                Intent intent = new Intent(getActivity(), ProductSchemeDetailsActivity.class);
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
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                sc.show(fm, "");
                            }
                            return true;
                        }
                    });

                    if (businessModel.configurationMasterHelper.IS_TEAMLEAD) {
                        holder.audit.setVisibility(View.VISIBLE);
                        // holder.avail_cb.setEnabled(false);

                        holder.shelfPcsQty.setEnabled(false);
                        holder.shelfCaseQty.setEnabled(false);

                        holder.mReason.setEnabled(false);


                    }
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
                    holder.psname.setTextColor(ContextCompat.getColor(getActivity(),
                            android.R.color.black));
                }


                if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == 2)
                    holder.audit.setImageResource(R.drawable.ic_audit_none);
                else if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == 1)
                    holder.audit.setImageResource(R.drawable.ic_audit_yes);
                else if (holder.productObj
                        .getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex)
                        .getAudit() == 0)
                    holder.audit.setImageResource(R.drawable.ic_audit_no);

                holder.pname = holder.productObj.getProductName();

                holder.psname.setText(holder.productObj.getProductShortName());
                String strPPQ = getResources().getString(R.string.ppq) + ": "
                        + holder.productObj.getRetailerWiseProductWiseP4Qty() + "";
                holder.ppq.setText(strPPQ);
                String strPSQ = getResources().getString(R.string.psq) + ": "
                        + holder.productObj.getRetailerWiseP4StockQty();
                holder.psq.setText(strPSQ);


                if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                    holder.imageButton_availability.setChecked(true);
                    holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.GREEN)));

                } else if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 0) {
                    holder.imageButton_availability.setChecked(true);
                    holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.RED)));
                } else if (holder.productObj.getLocations()
                        .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == -1) {
                    holder.imageButton_availability.setChecked(false);
                    holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.checkbox_default_color)));
                }


                if (businessModel.configurationMasterHelper.SHOW_STOCK_RSN) {
                    if (holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex)
                            .getShelfPiece() > 0 || holder.productObj.getLocations()
                            .get(stockCheckPresenter.mSelectedLocationIndex).getAvailability() == 1) {
                        holder.mReason.setEnabled(false);
                        holder.mReason.setSelected(false);
                        holder.mReason.setSelection(0);
                    } else {
                        holder.mReason.setEnabled(true);
                        holder.mReason.setSelected(true);
                        holder.mReason.setSelection(stockCheckPresenter.getReasonIndex(holder.productObj
                                .getReasonID()));
                    }
                }
                if (businessModel.configurationMasterHelper.SHOW_STOCK_FC) {
                    String strFacingQty = holder.productObj.getLocations().get(stockCheckPresenter.mSelectedLocationIndex).getFacingQty() + "";
                    holder.facingQty.setText(strFacingQty);
                }

                if (businessModel.configurationMasterHelper.SHOW_STOCK_SP) {
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

                if (businessModel.configurationMasterHelper.SHOW_STOCK_SC) {
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
                if (businessModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
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

                TypedArray typeArr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
                if (position % 2 == 0) {
                    row.setBackgroundColor(typeArr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
                } else {
                    row.setBackgroundColor(typeArr.getColor(R.styleable.MyTextView_listcolor, 0));
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
        private TextView psq;
        private EditText shelfPcsQty;
        private EditText shelfCaseQty;

        private EditText shelfouter;
        private EditText facingQty;


        private TextView total;


        private LinearLayout ll_stkCB;

        private Spinner mReason;
        ImageButton audit;
        AppCompatCheckBox imageButton_availability;
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
                if (businessModel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
            }

            menu.findItem(R.id.menu_sih_apply).setVisible(false);

            menu.findItem(R.id.menu_sih_apply).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && businessModel.productHelper.isFilterAvaiable("MENU_STK_ORD")) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
                menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
            }

            if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && stockCheckPresenter.mSelectedIdByLevelId != null) {
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
            /*if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && stockCheckPresenter.mSelectedIdByLevelId != null
                    && !businessModel.isMapEmpty(stockCheckPresenter.mSelectedIdByLevelId)) {
                menu.findItem(R.id.menu_competitor_filter).setIcon(
                        R.drawable.ic_action_filter_select);

            }*/

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
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                stockCheckPresenter.returnToHome();
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_product_filter) {
            if (businessModel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                stockCheckPresenter.putValueToFilterMap("");
            }
            productFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
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
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
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
                        businessModel.saveModuleCompletion("MENU_STOCK");
                        startActivity(new Intent(getActivity(),
                                HomeScreenTwo.class));
                        getActivity().finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_STOCK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        } else if (i == R.id.menu_competitor_filter) {
            competitorFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
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
        FragmentTransaction ft = getActivity()
                .getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog("MENU_CLOSING");
        dialog.setCancelable(false);
        dialog.show(ft, "stk_chk_remark");
    }

    private void onNextButtonClick() {
        if (businessModel.configurationMasterHelper.IS_MUST_SELL_STK
                && !businessModel.productHelper.isMustSellFilledStockCheck(true)) {
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

                    .setTitle(getResources().getString(R.string.reason_required_for) + text)
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
        hscrl_spl_filter = (HorizontalScrollView) view.findViewById(R.id.hscrl_spl_filter);
        hscrl_spl_filter.setVisibility(View.VISIBLE);
        ll_spl_filter = (LinearLayout) view.findViewById(R.id.ll_spl_filter);
        ll_tab_selection = (LinearLayout) view.findViewById(R.id.ll_tab_selection);
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
        final TabLayout tabLay = (TabLayout) view.findViewById(R.id.dummy_tab_lay);
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
            tab.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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
                        updateBrandText(BRAND, -1);
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

    private void productFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));

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
            if (businessModel.productHelper.getChildLevelBo().size() > 0)
                bundle.putString("filterHeader", businessModel.productHelper
                        .getChildLevelBo().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", businessModel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());
            bundle.putSerializable("serilizeContent",
                    businessModel.productHelper.getChildLevelBo());

            if (businessModel.productHelper.getParentLevelBo() != null
                    && businessModel.productHelper.getParentLevelBo().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", businessModel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());

                businessModel.productHelper.setPlevelMaster(businessModel.productHelper
                        .getParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);
                bundle.putString("isFrom", "STK");
            }

            FilterFragment fragobj = new FilterFragment(stockCheckPresenter.getSelectedFilterMap());
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }


    @Override
    public void loadStartVisit() {
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
                    Button ed = (Button) getView().findViewById(vw.getId());
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

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    public void onClick(View v) {
        Button vw = (Button) v;
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtn_clear) {
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

        ;

        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        Commons.print("OnResume Called");
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        stockCheckPresenter.getFilteredList(mParentIdList, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
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
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    private void updateFooter() {

        int totalAvailableProduts = 0;
        for (ProductMasterBO bo : stockList) {

            for (LocationBO locationBO : bo.getLocations()) {

                if ((locationBO.getShelfCase() > 0 || locationBO.getShelfOuter() > 0 || locationBO.getShelfPiece() > 0)
                        || (locationBO.getAvailability() > -1)) {
                    totalAvailableProduts += 1;
                    break;
                }
            }
        }

        tv_total_stockCheckedProducts.setText(totalAvailableProduts + "");
        tv_total_products.setText("/" + stockList.size());

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
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", stockCheckPresenter.GENERAL);
            bundle.putBoolean("isFormBrand", false);

            bundle.putSerializable("serilizeContent",
                    stockCheckPresenter.getGeneralFilter());

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(stockCheckPresenter.getSelectedFilterMap());
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


}
