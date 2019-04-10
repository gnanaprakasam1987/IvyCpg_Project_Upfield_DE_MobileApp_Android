package com.ivy.cpg.view.van.stockproposal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.SpecialFilterFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by hanifa.m on 5/5/2017.
 * on tha
 */

public class StockProposalFragment extends IvyBaseFragment implements
        View.OnClickListener, BrandDialogInterface, TextView.OnEditorActionListener, FiveLevelFilterCallBack {


    public static final String BRAND = "Brand";
    public static final String GENERAL = "General";


    protected BusinessModel bmodel;

    private ListView lvwplist;
    private TextView totalValueText, lpcText, productName;
    private EditText QUANTITY, mEdt_searchproductName, QUANTITY1;
    private String brandbutton, generalbutton;


    private DrawerLayout mDrawerLayout;
    private ViewFlipper viewFlipper;

    private ActionBarDrawerToggle mDrawerToggle;
    private boolean isSpecialFilter_enabled = true;


    private String append = "";
    Vector<String> mgeneralFilterList;
    private ArrayList<String> mSearchTypeArray;
    private InputMethodManager inputManager;


    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();

    public final String mCommon = "Filt01";
    public final String mSbd = "Filt02";
    public final String mSbdGaps = "Filt03";
    public final String mOrdered = "Filt04";
    public final String mPurchased = "Filt05";
    public final String mInitiative = "Filt06";
    public final String mOnAllocation = "Filt07";
    public final String mInStock = "Filt08";
    public final String mPromo = "Filt09";
    public final String mMustSell = "Filt10";
    public final String mFocusBrand = "Filt11";
    public final String mFocusBrand2 = "Filt12";
    public final String msih = "Filt13";
    public final String mOOS = "Filt14";
    public final String mDiscount = "Filt18";
    public String strBarCodeSearch = "ALL";
    private final String mFocusBrand3 = "Filt20";
    private final String mFocusBrand4 = "Filt21";
    private final String mSMP = "Filt22";
    public final String mNMustSell = "Filt16";
    public final String mStock = "Filt17";
    public boolean isSbd, isSbdGaps, isOrdered, isPurchased, isInitiative, isOnAllocation, isInStock, isPromo, isMustSell, isFocusBrand, isFocusBrand2, isSIH, isOOS, isNMustSell, isStock, isDiscount, isFocusBrand3, isFocusBrand4, isSMP;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    private View view;

    private Vector<LoadManagementBO> stockPropVector;
    private ArrayList<LoadManagementBO> stockPropMylist;


    private MyAdapter mSchedule;
    private boolean isCreditLimitExceedToast = false;
    private boolean isToastDisabled = false;
    private boolean isMaxExceedToast = false;
    private AlertDialog alertDialog;
    private int stdqtytotal, currentstdqty, currentouterqty, currentcaseqty, currentpcsqty;
    private int calculatedCaseQty, calculatedPieceQty, calculatedOuterQty;
    private double calculatedTotalvalue = 0;
    private LoadManagementBO stock;
    private String sihTitel = "", hvp3mTitlel = "", distInvTitle = "";

    SearchAsync searchAsync;
    Bundle bundle = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_stock_proposal, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        inputManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);

        mEdt_searchproductName = view.findViewById(R.id.edt_searchproductName);
        Button mBtn_Search = view.findViewById(R.id.btn_search);
        Button mBtnFilterPopup = view.findViewById(R.id.btn_filter_popup);
        Button mBtn_clear = view.findViewById(R.id.btn_clear);
        Button saveBtn = view.findViewById(R.id.btn_next);
        saveBtn.setText(getResources().getString(R.string.save));

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mBtn_clear.setOnEditorActionListener(this);
        saveBtn.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);


        getOverflowMenu();

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bundle = getArguments();
        if (bundle == null)
            bundle = getActivity().getIntent().getExtras();

        setScreenTitle(bundle.getString("screentitle"));

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        customProgressDialog(builder, getResources().getString(R.string.loading_data));
        alertDialog = builder.create();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object *//*
         * nav drawer image to replace
         * 'Up' caret
         */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                setScreenTitle(bundle.getString("screentitle"));
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter));
                getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        totalValueText = view.findViewById(R.id.totalValue);
        lpcText = view.findViewById(R.id.lcp);

        productName = view.findViewById(R.id.productName);
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        lvwplist = view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
            view.findViewById(R.id.outeritemcasetitle).setVisibility(View.GONE);
        } else {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outeritemcasetitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.outeritemcasetitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.outeritemcasetitle).getTag()));
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            view.findViewById(R.id.itemcasetitle).setVisibility(View.GONE);
        } else {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.itemcasetitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.itemcasetitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.itemcasetitle).getTag()));
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            view.findViewById(R.id.itempiecetitle).setVisibility(View.GONE);
        } else {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.itempiecetitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.itempiecetitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.itempiecetitle).getTag()));
        }

        if (!bmodel.configurationMasterHelper.SHOW_UNIT_PRICE)
            view.findViewById(R.id.unitpricetitle).setVisibility(View.GONE);
        view.findViewById(R.id.lpc_title).setVisibility(View.GONE);
        view.findViewById(R.id.lcp).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_STKPRO_SPL_FILTER) {
            hideSpecialFilter();
        } else {
            isSpecialFilter_enabled = true;
            generalbutton = GENERAL;
        }

        getActivity().supportInvalidateOptionsMenu();
        // new DownloadStockProposal().execute();
        updateBrandText("Brand", -1);


        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        Vector<String> vect = new Vector<>();
        vect.addAll(Arrays.asList(getResources().getStringArray(
                R.array.productFilterArray)));
        addSpecialFilterList(vect);
        mSelectedFilterMap.put("General", "Common");
        QUANTITY1 = new EditText(getActivity());
        QUANTITY1.setText("");
        if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER)
            hideSpecialFilter();

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    brandbutton = BRAND;
                    generalbutton = GENERAL;
                    getActivity().supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        searchAsync = new SearchAsync(StockProposalFragment.this);
                        searchAsync.execute();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                    if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                        searchAsync.cancel(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
            });
        } catch (Exception e) {
            Commons.printException("" + e);
        }


        mEdt_searchproductName
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(),
                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
        mBtn_clear
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(),
                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
        getMandatoryFilters();

        searchAsync = new SearchAsync(this);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setScreenTitle(bundle.getString("screentitle"));
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<Context> myClassWeakReference;

        public MyHandler(Context myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 1) {
                    Commons.print("Gor Handler");
                    cancelProgDialog();
                }
            } catch (Exception e) {
                Log.i("e", e.getMessage());
            }
        }
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        private final ArrayList<LoadManagementBO> items;
        private LoadManagementBO stockProposalBO;

        public MyAdapter(ArrayList<LoadManagementBO> items) {
            super(getActivity(), R.layout.row_stockproposal, items);
            this.items = items;
        }

        public LoadManagementBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            stockProposalBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_stockproposal, parent,
                        false);
                holder = new ViewHolder();

                holder.pname = row.findViewById(R.id.closePRODNAME);
                holder.newProposalQty = row
                        .findViewById(R.id.productqtyCases);
                holder.newproposalpcsQty = row
                        .findViewById(R.id.productqtyPieces);
                holder.proposalQty = row
                        .findViewById(R.id.proposalQty);
                holder.outerQty = row
                        .findViewById(R.id.outerproductqtyCases);

                holder.unitprice = row.findViewById(R.id.unitprice);


                holder.pname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);


                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.newProposalQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.newproposalpcsQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);


                if (!bmodel.configurationMasterHelper.SHOW_UNIT_PRICE)
                    holder.unitprice.setVisibility(View.GONE);
                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Commons.print("outer:" + holder.spbo.getOuterSize());
                        if (holder.spbo.getOuterSize() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        try {

                            int sum;
                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.outerQty.setSelection(qty.length());
                            if (!qty.equals("")) {

                                holder.spbo.setStkproouterqty(SDUtil
                                        .convertToInt(qty));
                                sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                        .getCaseSize())
                                        + holder.spbo.getStkpropcsqty()
                                        + (holder.spbo.getStkproouterqty() * holder.spbo
                                        .getOuterSize());
                                if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                    if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                            .getMaxQty())
                                            || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                            .getSuggestqty())) {

                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.outerQty
                                                .removeTextChangedListener(this);
                                        if (!isToastDisabled) {
                                            showMessage(String.format(getString(R.string.exceed),
                                                    holder.spbo.getMaxQty()));

                                        } else {
                                            isMaxExceedToast = true;
                                        }
                                        try {
                                            holder.spbo.setStkproouterqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.outerQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");

                                        } catch (Exception e) {
                                            holder.spbo.setStkproouterqty(0);
                                            holder.outerQty.setText("0");
                                        }

                                        holder.outerQty
                                                .addTextChangedListener(this);
                                    } else {
                                        holder.spbo.setStkproouterqty(SDUtil
                                                .convertToInt(qty));
                                        int totalQty = (holder.spbo.getStkprocaseqty() * holder.spbo
                                                .getCaseSize())
                                                + holder.spbo.getStkpropcsqty()
                                                + (holder.spbo.getStkproouterqty() * holder.spbo
                                                .getOuterSize());
                                        holder.unitprice.setText(bmodel.formatValue(totalQty
                                                * holder.spbo.getBaseprice())
                                                + "");
                                    }
                                } else {
                                    holder.spbo.setStkproouterqty(SDUtil
                                            .convertToInt(qty));
                                }

                                if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {

                                    if (updateValue() > bmodel.userMasterHelper
                                            .getUserMasterBO().getCreditlimit()) {
                                        holder.outerQty
                                                .removeTextChangedListener(this);
                                        try {
                                            holder.spbo.setStkproouterqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.outerQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                            Commons.print("Outer QTY"
                                                    + holder.spbo
                                                    .getStkproouterqty());
                                        } catch (Exception e) {
                                            holder.spbo.setStkproouterqty(0);
                                            holder.outerQty.setText("0");
                                        }

                                        if (!isToastDisabled) {
                                            showMessage(getString(R.string.exceeded_credit_limit));
                                        } else {
                                            isCreditLimitExceedToast = true;
                                        }
                                        holder.outerQty
                                                .addTextChangedListener(this);

                                    } else {
                                        holder.spbo.setStkproouterqty(SDUtil
                                                .convertToInt(qty));
                                    }
                                } else {
                                    holder.spbo.setStkproouterqty(SDUtil
                                            .convertToInt(qty));
                                }

                            }

                            int totalQty = (holder.spbo.getStkprocaseqty() * holder.spbo
                                    .getCaseSize())
                                    + holder.spbo.getStkpropcsqty()
                                    + (holder.spbo.getStkproouterqty() * holder.spbo
                                    .getOuterSize());
                            holder.unitprice.setText(bmodel.formatValue(totalQty
                                    * holder.spbo.getBaseprice())
                                    + "");
                            updateValue();
                        } catch (Exception e) {
                            Log.i("e", e.getMessage());
                        }
                    }
                });

                holder.outerQty.setOnTouchListener(new View.OnTouchListener() {

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.outerQty;
                            QUANTITY1 = holder.outerQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        int inType = holder.outerQty.getInputType(); // backup
                        holder.outerQty.setInputType(InputType.TYPE_NULL); // disable
                        holder.outerQty.onTouchEvent(event); // call
                        holder.outerQty.setInputType(inType); // restore
                        holder.outerQty.requestFocus(); // type
                        if (holder.outerQty.getText().length() > 0)
                            holder.outerQty.setSelection(holder.outerQty.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.newProposalQty.addTextChangedListener(new TextWatcher() {

                    public void afterTextChanged(Editable s) {
                        Commons.print("Case:" + holder.spbo.getCaseSize());
                        if (holder.spbo.getCaseSize() == 0) {
                            holder.newProposalQty
                                    .removeTextChangedListener(this);
                            holder.newProposalQty.setText("0");
                            holder.newProposalQty.addTextChangedListener(this);
                            return;
                        }

                        try {

                            int sum;
                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.newProposalQty.setSelection(qty.length());

                            if (!qty.equals("")) {

                                holder.spbo.setStkprocaseqty(SDUtil
                                        .convertToInt(qty));
                                sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                        .getCaseSize())
                                        + holder.spbo.getStkpropcsqty()
                                        + (holder.spbo.getStkproouterqty() * holder.spbo
                                        .getOuterSize());

                                if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                    if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                            .getMaxQty())
                                            || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                            .getSuggestqty())) {

                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.newProposalQty
                                                .removeTextChangedListener(this);
                                        if (!isToastDisabled) {

                                            showMessage(String.format(getString(R.string.exceed),
                                                    holder.spbo.getMaxQty()));

                                        } else {
                                            isMaxExceedToast = true;
                                        }
                                        try {
                                            holder.spbo.setStkprocaseqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.newProposalQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");

                                        } catch (Exception e) {
                                            holder.spbo.setStkprocaseqty(0);
                                            holder.newProposalQty.setText("0");
                                            holder.unitprice.setText("0");
                                        }

                                        holder.newProposalQty
                                                .addTextChangedListener(this);
                                    } else {
                                        holder.spbo.setStkprocaseqty(SDUtil
                                                .convertToInt(qty));
                                        int totalQty = (holder.spbo.getStkprocaseqty() * holder.spbo
                                                .getCaseSize())
                                                + holder.spbo.getStkpropcsqty()
                                                + (holder.spbo.getStkproouterqty() * holder.spbo
                                                .getOuterSize());
                                        holder.unitprice.setText(bmodel.formatValue(totalQty
                                                * holder.spbo.getBaseprice())
                                                + "");
                                    }
                                } else {
                                    holder.spbo.setStkprocaseqty(SDUtil
                                            .convertToInt(qty));
                                }
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
                                    if (updateValue() > bmodel.userMasterHelper
                                            .getUserMasterBO().getCreditlimit()) {
                                        holder.newProposalQty
                                                .removeTextChangedListener(this);

                                        try {
                                            holder.spbo.setStkprocaseqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.newProposalQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                            Commons.print("CASE QTY"
                                                    + holder.spbo
                                                    .getStkprocaseqty());
                                        } catch (Exception e) {
                                            holder.spbo.setStkprocaseqty(0);
                                            holder.newProposalQty.setText("0");
                                        }

                                        if (!isToastDisabled) {
                                            showMessage(getString(R.string.exceeded_credit_limit));
                                        } else {
                                            isCreditLimitExceedToast = true;
                                        }
                                        //  updateValue();
                                        holder.newProposalQty
                                                .addTextChangedListener(this);

                                    } else {
                                        holder.spbo.setStkprocaseqty(SDUtil
                                                .convertToInt(qty));
                                    }
                                } else {
                                    holder.spbo.setStkprocaseqty(SDUtil
                                            .convertToInt(qty));
                                }

                            }

                            int totalQty = (holder.spbo.getStkprocaseqty() * holder.spbo
                                    .getCaseSize())
                                    + holder.spbo.getStkpropcsqty()
                                    + (holder.spbo.getStkproouterqty() * holder.spbo
                                    .getOuterSize());
                            holder.unitprice.setText(bmodel.formatValue(totalQty
                                    * holder.spbo.getBaseprice())
                                    + "");
                            updateValue();
                        } catch (Exception e) {
                            Log.i("e", e.getMessage());
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });
                holder.newProposalQty.setOnTouchListener(new View.OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.newProposalQty;
                            QUANTITY1 = holder.newProposalQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        int inType = holder.newProposalQty.getInputType(); // backup
                        holder.newProposalQty.setInputType(InputType.TYPE_NULL); // disable
                        holder.newProposalQty.onTouchEvent(event); // call
                        holder.newProposalQty.setInputType(inType); // restore
                        holder.newProposalQty.requestFocus(); // type
                        if (holder.newProposalQty.getText().length() > 0)
                            holder.newProposalQty.setSelection(holder.newProposalQty.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.newproposalpcsQty
                        .addTextChangedListener(new TextWatcher() {

                            public void afterTextChanged(Editable s) {
                                try {
                                    int sum;
                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.newproposalpcsQty.setSelection(qty.length());
                                    if (!qty.equals("")) {
                                        holder.spbo.setStkpropcsqty(SDUtil
                                                .convertToInt(qty));
                                        sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                                .getCaseSize())
                                                + holder.spbo.getStkpropcsqty()
                                                + (holder.spbo
                                                .getStkproouterqty() * holder.spbo
                                                .getOuterSize());
                                        if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                            if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                                    .getMaxQty())
                                                    || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                                    .getSuggestqty())) {

                                                /**
                                                 * Delete the last entered
                                                 * number and reset the qty
                                                 **/
                                                holder.newproposalpcsQty
                                                        .removeTextChangedListener(this);
                                                if (!isToastDisabled) {
                                                    showMessage(String.format(getString(
                                                            R.string.exceed),
                                                            holder.spbo.getMaxQty()));
                                                } else {
                                                    isMaxExceedToast = true;
                                                }
                                                try {
                                                    holder.spbo
                                                            .setStkpropcsqty(SDUtil
                                                                    .convertToInt(qty
                                                                            .length() > 1 ? qty
                                                                            .substring(
                                                                                    0,
                                                                                    qty.length() - 1)
                                                                            : "0"));
                                                    holder.newproposalpcsQty.setText(qty
                                                            .length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0");

                                                    Commons.print("PCS QTY"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                } catch (Exception e) {
                                                    holder.spbo
                                                            .setStkpropcsqty(0);
                                                    holder.newproposalpcsQty
                                                            .setText("0");
                                                    holder.unitprice.setText("0");
                                                    Commons.print("PCS QTY e"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                }

                                                holder.newproposalpcsQty
                                                        .addTextChangedListener(this);
                                            } else {
                                                holder.spbo.setStkpropcsqty(SDUtil
                                                        .convertToInt(qty));

                                            }
                                        } else {
                                            holder.spbo.setStkpropcsqty(SDUtil
                                                    .convertToInt(qty));
                                        }
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
                                            if (updateValue() > bmodel.userMasterHelper
                                                    .getUserMasterBO()
                                                    .getCreditlimit()) {
                                                holder.newproposalpcsQty
                                                        .removeTextChangedListener(this);
                                                try {
                                                    holder.spbo
                                                            .setStkpropcsqty(SDUtil
                                                                    .convertToInt(qty
                                                                            .length() > 1 ? qty
                                                                            .substring(
                                                                                    0,
                                                                                    qty.length() - 1)
                                                                            : "0"));
                                                    holder.newproposalpcsQty.setText(qty
                                                            .length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0");
                                                    Commons.print("PCS QTY"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                } catch (Exception e) {
                                                    holder.spbo
                                                            .setStkpropcsqty(0);
                                                    holder.newproposalpcsQty
                                                            .setText("0");
                                                    holder.unitprice.setText("0");
                                                    Commons.print("PCS QTY E"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                }
                                                if (!isToastDisabled) {
                                                    showMessage(getString(R.string.exceeded_credit_limit));
                                                } else {
                                                    isCreditLimitExceedToast = true;
                                                }
                                                //updateValue();
                                                holder.newproposalpcsQty
                                                        .addTextChangedListener(this);

                                            } else {
                                                holder.spbo.setStkpropcsqty(SDUtil
                                                        .convertToInt(qty));
                                            }
                                        } else {
                                            holder.spbo.setStkpropcsqty(SDUtil
                                                    .convertToInt(qty));
                                        }

                                    }

                                    int totalQty = (holder.spbo.getStkprocaseqty() * holder.spbo
                                            .getCaseSize())
                                            + holder.spbo.getStkpropcsqty()
                                            + (holder.spbo.getStkproouterqty() * holder.spbo
                                            .getOuterSize());
                                    holder.unitprice.setText(bmodel.formatValue(totalQty
                                            * holder.spbo.getBaseprice())
                                            + "");
                                    updateValue();
                                } catch (Exception e) {
                                    Log.i("e", e.getMessage());
                                }
                            }

                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }
                        });
                holder.newproposalpcsQty
                        .setOnTouchListener(new View.OnTouchListener() {
                            // @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                productName.setText(holder.Pname);
                                if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                        || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                        .getIsMust() == 0)) {
                                    QUANTITY1 = null;
                                    QUANTITY = null;
                                } else {
                                    QUANTITY = holder.newproposalpcsQty;
                                    QUANTITY1 = holder.newproposalpcsQty;
                                    QUANTITY.setTag(holder.spbo);
                                    QUANTITY1.setTag(holder.spbo);
                                }

                                int inType = holder.newproposalpcsQty
                                        .getInputType(); // backup
                                holder.newproposalpcsQty
                                        .setInputType(InputType.TYPE_NULL); // disable
                                holder.newproposalpcsQty.onTouchEvent(event); // call
                                holder.newproposalpcsQty.setInputType(inType); // restore
                                holder.newproposalpcsQty.requestFocus(); // type
                                if (holder.newproposalpcsQty.getText().length() > 0)
                                    holder.newproposalpcsQty.setSelection(holder.newproposalpcsQty.getText().length());
                                inputManager.hideSoftInputFromWindow(
                                        mEdt_searchproductName.getWindowToken(),
                                        0);
                                return true;
                            }
                        });

                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.newproposalpcsQty;
                            QUANTITY1 = holder.newproposalpcsQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        holder.newproposalpcsQty.selectAll();
                        holder.newproposalpcsQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.setInAnimation(
                                    getActivity(),
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(
                                    getActivity(),
                                    R.anim.out_to_left);
                            viewFlipper.showPrevious();
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                        }

                    }
                });
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.spbo = stockProposalBO;
            holder.Pname = holder.spbo.getProductname();
            holder.pname.setText(holder.spbo.getProductshortname());

            getBottomValues(holder);
            holder.proposalQty.setText(holder.bottomStrTxt);

            holder.newProposalQty.setText(holder.spbo.getStkprocaseqty() + "");
            holder.newproposalpcsQty
                    .setText(holder.spbo.getStkpropcsqty() + "");
            holder.outerQty.setText(holder.spbo.getStkproouterqty() + "");
            double unitprice = ((holder.spbo.getStkprocaseqty() * holder.spbo
                    .getCaseSize()) + holder.spbo.getStkpropcsqty() + (holder.spbo
                    .getStkproouterqty() * holder.spbo.getOuterSize()))
                    * holder.spbo.getBaseprice();
            holder.unitprice.setText(bmodel.formatValue(unitprice) + "");

            // Disable the User Entry if UomID is Zero
            if (holder.spbo.getPiece_uomid() == 0 || !holder.spbo.isPieceMapped())

            {
                holder.newproposalpcsQty.setEnabled(false);
            } else

            {
                holder.newproposalpcsQty.setEnabled(true);
            }
            if (holder.spbo.getdUomid() == 0 || !holder.spbo.isCaseMapped())

            {
                holder.newProposalQty.setEnabled(false);
            } else

            {
                holder.newProposalQty.setEnabled(true);
            }
            if (holder.spbo.getdOuonid() == 0 || !holder.spbo.isOuterMapped())

            {
                holder.outerQty.setEnabled(false);
            } else

            {
                holder.outerQty.setEnabled(true);
            }

            return (row);
        }
    }

    class ViewHolder {
        private LoadManagementBO spbo;
        private TextView pname, proposalQty, unitprice;
        private EditText newProposalQty, newproposalpcsQty, outerQty;
        private String Pname;
        private String bottomStrTxt = "";
    }


    private void getBottomValues(ViewHolder holder) {
        holder.bottomStrTxt = "";

        if (bmodel.configurationMasterHelper.SHOW_STOCK_SO) {

            if (hvp3mTitlel.isEmpty())//to avoid multiple call's
                if (bmodel.labelsMasterHelper.applyLabels("so") != null)
                    hvp3mTitlel = bmodel.labelsMasterHelper
                            .applyLabels("so") + ": ";

                else
                    hvp3mTitlel = getResources().getString(R.string.hvp3m) + ": ";

            holder.bottomStrTxt += (hvp3mTitlel + holder.spbo.getSuggestqty() + "   ");
        }

        if (bmodel.configurationMasterHelper.STOCK_DIST_INV) {
            if (distInvTitle.isEmpty())//to avoid multiple call's
                if (bmodel.labelsMasterHelper.applyLabels("dist_inv") != null)
                    distInvTitle = bmodel.labelsMasterHelper
                            .applyLabels("dist_inv") + ": ";
                else
                    distInvTitle = getResources().getString(R.string.dist_inv) + ": ";


            holder.bottomStrTxt += (distInvTitle + holder.spbo.getWsih() + "   ");
        }

        getSIHValues(holder);

    }

    private void getSIHValues(ViewHolder holder) {

        if (sihTitel.isEmpty())
            if (bmodel.labelsMasterHelper.applyLabels("sih_piece") != null)
                sihTitel = bmodel.labelsMasterHelper
                        .applyLabels("sih_piece") + ": ";
            else
                sihTitel = getResources().getString(R.string.sih) + ": ";


        String sihCaseStr = "";
        String sihOuterStr = "";
        String sihPieceStr = "";

        //Update SIH Value
        if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
            if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (holder.spbo.getSih() == 0) {
                    sihCaseStr = "0";
                    sihOuterStr = "/0";
                    sihPieceStr = "/0";
                } else if (holder.spbo.getCaseSize() == 0) {
                    sihCaseStr = "0";
                    if (holder.spbo.getOuterSize() == 0) {
                        sihOuterStr = "/0";
                        sihPieceStr = ("/" + holder.spbo.getSih());
                    } else {
                        sihOuterStr = ("/" + holder.spbo.getSih()
                                / holder.spbo.getOuterSize() + "");
                        sihPieceStr = ("/" + holder.spbo.getSih()
                                % holder.spbo.getOuterSize() + "");
                    }
                } else {
                    sihCaseStr = (holder.spbo.getSih()
                            / holder.spbo.getCaseSize() + "");
                    if (holder.spbo.getOuterSize() > 0
                            && (holder.spbo.getSih() % holder.spbo
                            .getCaseSize()) >= holder.spbo
                            .getOuterSize()) {
                        sihOuterStr = ("/" + (holder.spbo.getSih() % holder.spbo
                                .getCaseSize())
                                / holder.spbo.getOuterSize() + "");
                        sihPieceStr = ("/" + (holder.spbo.getSih() % holder.spbo
                                .getCaseSize())
                                % holder.spbo.getOuterSize() + "");
                    } else {
                        sihOuterStr = "/0";
                        sihPieceStr = ("/" + holder.spbo.getSih()
                                % holder.spbo.getCaseSize() + "");
                    }
                }
            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (holder.spbo.getSih() == 0) {
                    sihCaseStr = "0";
                    sihOuterStr = "/0";
                } else if (holder.spbo.getCaseSize() == 0) {
                    sihCaseStr = "0";
                    if (holder.spbo.getOuterSize() == 0)
                        sihOuterStr = "/0";
                    else
                        sihOuterStr = ("/" + holder.spbo.getSih()
                                / holder.spbo.getOuterSize() + "");

                } else {
                    sihCaseStr = (holder.spbo.getSih()
                            / holder.spbo.getCaseSize() + "");
                    if (holder.spbo.getOuterSize() > 0
                            && (holder.spbo.getSih() % holder.spbo
                            .getCaseSize()) >= holder.spbo
                            .getOuterSize()) {
                        sihOuterStr = ("/" + (holder.spbo.getSih() % holder.spbo
                                .getCaseSize())
                                / holder.spbo.getOuterSize());
                    } else {
                        sihOuterStr = "/0";
                    }
                }
            } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (holder.spbo.getSih() == 0) {
                    sihPieceStr = "/0";
                    sihOuterStr = "/0";
                } else if (holder.spbo.getOuterSize() == 0) {
                    sihPieceStr = ("/" + holder.spbo.getSih());
                    sihOuterStr = "/0";
                } else {
                    sihOuterStr = ("/" + holder.spbo.getSih() / holder.spbo.getOuterSize());
                    sihPieceStr = ("/" + holder.spbo.getSih() % holder.spbo.getOuterSize());
                }
            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (holder.spbo.getSih() == 0) {
                    sihPieceStr = "/0";
                    sihCaseStr = "0";
                } else if (holder.spbo.getCaseSize() == 0) {
                    sihPieceStr = "/" + holder.spbo.getSih();
                    sihCaseStr = "0";
                } else {
                    sihCaseStr = (holder.spbo.getSih()
                            / holder.spbo.getCaseSize() + "");
                    sihPieceStr = ("/" + holder.spbo.getSih()
                            % holder.spbo.getCaseSize());
                }
            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                if (holder.spbo.getSih() == 0 || holder.spbo.getCaseSize() == 0)
                    sihCaseStr = "0";
                else
                    sihCaseStr = (holder.spbo.getSih()
                            / holder.spbo.getCaseSize() + "");
            } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (holder.spbo.getSih() == 0 || holder.spbo.getOuterSize() == 0)
                    sihOuterStr = "/0";
                else
                    sihOuterStr = (holder.spbo.getSih()
                            / holder.spbo.getOuterSize() + "");
            } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                sihPieceStr = ("/" + holder.spbo.getSih());
            }
        } else
            sihPieceStr = ("/" + holder.spbo.getSih());

        holder.bottomStrTxt += (sihTitel + sihCaseStr + sihOuterStr + sihPieceStr);

    }

    @SuppressLint("StaticFieldLeak")
    class SaveStockProposal extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                StockProposalModuleHelper.getInstance(getActivity())
                        .saveStockProposal(stockPropVector);

                bmodel.saveModuleCompletion("MENU_STOCK_PROPOSAL", false);

            } catch (Exception e) {
                Log.i("e", e.getMessage());
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            showMessage(getString(R.string.saved_successfully));

            if (bundle.getBoolean("isFromLodMgt")) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } else {
                HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) getActivity()).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                if (currentFragment != null) {
                    currentFragment.detach("MENU_STK_PRO");
                }
            }
        }

    }


    private void getMandatoryFilters() {
        if (bmodel.configurationMasterHelper.getGenFilter() != null) {

            for (ConfigureBO bo : bmodel.configurationMasterHelper.getGenFilter()) {
                if (bo.getMandatory() == 1) {
                    if (bo.getConfigCode().equals(mSbd))
                        isSbd = true;
                    else if (bo.getConfigCode().equals(mSbdGaps))
                        isSbdGaps = true;
                    else if (bo.getConfigCode().equals(mOrdered))
                        isOrdered = true;
                    else if (bo.getConfigCode().equals(mPurchased))
                        isPurchased = true;
                    else if (bo.getConfigCode().equals(mInitiative))
                        isInitiative = true;
                    else if (bo.getConfigCode().equals(mOnAllocation))
                        isOnAllocation = true;
                    else if (bo.getConfigCode().equals(mInStock))
                        isInStock = true;
                    else if (bo.getConfigCode().equals(mPromo))
                        isPromo = true;
                    else if (bo.getConfigCode().equals(mMustSell))
                        isMustSell = true;
                    else if (bo.getConfigCode().equals(mFocusBrand))
                        isFocusBrand = true;
                    else if (bo.getConfigCode().equals(mFocusBrand2))
                        isFocusBrand2 = true;
                    else if (bo.getConfigCode().equals(msih))
                        isSIH = true;
                    else if (bo.getConfigCode().equals(mOOS))
                        isOOS = true;
                    else if (bo.getConfigCode().equals(mDiscount))
                        isDiscount = true;
                    else if (bo.getConfigCode().equals(mFocusBrand3))
                        isFocusBrand3 = true;
                    else if (bo.getConfigCode().equals(mFocusBrand4))
                        isFocusBrand4 = true;
                    else if (bo.getConfigCode().equals(mSMP))
                        isSMP = true;
                    else if (bo.getConfigCode().equals(mNMustSell))
                        isNMustSell = true;
                    else if (bo.getConfigCode().equals(mStock))
                        isStock = true;

                }
            }
        }
    }


    /**
     * add items for SpecialFilter
     *
     * @param filterList
     */
    private void addSpecialFilterList(Vector filterList) {
        mgeneralFilterList = filterList;
    }


    /**
     * used to hide the specialFilter
     */
    private void hideSpecialFilter() {
        isSpecialFilter_enabled = false;
        generalbutton = "GENERAL";

    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(getActivity());
            java.lang.reflect.Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    private class SearchAsync extends
            AsyncTask<Integer, Integer, Boolean> {

        private WeakReference<StockProposalFragment> weakReference;//To avoid memory leaks

        public SearchAsync(StockProposalFragment context) {
            this.weakReference = new WeakReference<>(context);
        }

        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            loadSearchedList();

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (weakReference.get() != null) {
                mSchedule = new MyAdapter(stockPropMylist);
                lvwplist.setAdapter(mSchedule);
            }
        }
    }

    private void loadSearchedList() {
        try {
            Vector<LoadManagementBO> items = bmodel.productHelper
                    .getLoadMgmtProducts();

            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.no_products_exists), 0);
                return;
            }
            int siz = items.size();
            stockPropMylist = new ArrayList<>();
            String mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                LoadManagementBO ret = items
                        .elementAt(i);

                if (searchAsync.isCancelled()) {
                    break;
                }

                if (ret.getIssalable() == 1) {
                    if (mSelectedFilter.equals(getResources().getString(
                            R.string.order_dialog_barcode))) {
                        if (ret.getBarcode() != null && ret.getBarcode()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText()
                                                .toString().toLowerCase()))
                            stockPropMylist.add(ret);

                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.prod_code))) {
                        if ((ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(mEdt_searchproductName.getText().toString()
                                        .toLowerCase())) || (ret.getProductCode() != null && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                                .toLowerCase())))
                            stockPropMylist.add(ret);

                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.product_name))) {
                        if (ret.getProductshortname() != null && ret.getProductshortname()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText()
                                                .toString().toLowerCase()))
                            stockPropMylist.add(ret);
                    }
                }


            }

        } catch (Resources.NotFoundException e) {
            Log.i("e", e.getMessage());
        }
    }

    private void refreshList() {
        lvwplist.setAdapter(new MyAdapter(stockPropMylist));

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        int vw = v.getId();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (vw == R.id.btn_search) {
            viewFlipper.setInAnimation(getActivity(), R.anim.in_from_right);
            viewFlipper.setOutAnimation(getActivity(), R.anim.out_to_right);
            viewFlipper.showNext();
            mEdt_searchproductName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEdt_searchproductName,
                    InputMethodManager.SHOW_FORCED);
        } else if (vw == R.id.btn_filter_popup) {

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    getActivity());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));

                        }

                    });
            builderSingle.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                        }
                    });
            bmodel.applyAlertDialogTheme(builderSingle);
        } else if (vw == R.id.btn_clear) {
            mEdt_searchproductName.setText("");
            viewFlipper.showPrevious();
            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");
            getActivity().supportInvalidateOptionsMenu();
            try {
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }

            updateGeneralText(GENERAL);
        } else if (vw == R.id.btn_next) {
            onNextButtonClick();
        }
    }


    private void onNextButtonClick() {
        if (hasStockProposalDone()) {
            if (bmodel.configurationMasterHelper.IS_MUST_STOCK
                    && !StockProposalModuleHelper.getInstance(getActivity())
                    .isMustStockFilled(stockPropVector)) {
                onCreateDialog(1);

            } else
                new SaveStockProposal().execute();
        } else {

            bmodel.showAlert("No items found.", 0);
        }
    }


    private void onBackButtonClick() {
        if (hasStockProposalDone()) {
            onCreateDialog(0);
        } else {
            if (bundle.getBoolean("isFromLodMgt")) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } else {
                HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) getActivity()).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                if (currentFragment != null) {
                    currentFragment.detach("MENU_STK_PRO");
                }
            }

        }
    }

    private boolean hasStockProposalDone() {
        int siz = stockPropVector.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO product = stockPropVector
                    .get(i);
            if (product.getStkprocaseqty() > 0 || product.getStkpropcsqty() > 0
                    || product.getStkproouterqty() > 0)
                return true;
        }
        return false;
    }


    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case 0:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.doyouwantgoback))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (bundle.getBoolean("isFromLodMgt")) {
                                            getActivity().finish();
                                            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                        } else {
                                            HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) getActivity()).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                                            if (currentFragment != null) {
                                                currentFragment.detach("MENU_STK_PRO");
                                            }
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);

                break;

            case 1:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.Please_fill_must_stock))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            case 2:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.credit_limit_exceed_do_you_wish_to_apply_partially))
                        .setPositiveButton("Apply",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        showProgDialog();
                                        doApplyStdQtyStuff(true);
                                        mSchedule.notifyDataSetChanged();
                                        doToast();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 3:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.credit_limit_exceed_do_you_wish_to_apply_partially))
                        .setPositiveButton("Apply",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        showProgDialog();
                                        doApplyStockQtyStuff(true);
                                        mSchedule.notifyDataSetChanged();
                                        doToast();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

        }
        return null;
    }

    private void showProgDialog() {

        if (alertDialog != null)
            alertDialog.show();
    }

    private void cancelProgDialog() {

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private void doApplyStockQtyStuff(boolean isCreditLimitValidation) {

        LoadManagementBO stock;
        double tmpTotalvalue = 0;
        double previousTotal;
        for (int i = 0; i < stockPropMylist.size(); i++) {
            stock = stockPropMylist.get(i);
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                if (stock.getSuggestqty() > stock.getMaxQty())
                    stock.setStkpropcsqty(stock.getMaxQty());
                else
                    stock.setStkpropcsqty(stock.getSuggestqty());
                stock.setStkprocaseqty(0);
                stock.setStkproouterqty(0);
            } else {
                stock.setStkprocaseqty(0);
                stock.setStkproouterqty(0);
                stock.setStkpropcsqty(stock.getSuggestqty());
            }
            if (isCreditLimitValidation) {

                if (stock.getStkpropcsqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + (stock.getStkpropcsqty() * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkpropcsqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + (stock.getStkpropcsqty() * stock
                                .getBaseprice());
                    }
                }
            }
        }

    }

    private double doApplyStockQtyCalculation() {
        LoadManagementBO stock;
        int calculatedPieceQty;
        double calculatedTotalvalue = 0;
        try {
            for (int i = 0; i < stockPropMylist.size(); i++) {
                stock = stockPropMylist.get(i);
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                    if (stock.getSuggestqty() > stock.getMaxQty())
                        calculatedPieceQty = stock.getMaxQty();
                    else
                        calculatedPieceQty = stock.getSuggestqty();
                } else {
                    calculatedPieceQty = stock.getSuggestqty();
                }
                if (calculatedPieceQty > 0) {
                    calculatedTotalvalue = calculatedTotalvalue
                            + calculatedPieceQty * stock.getBaseprice();
                }
            }
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
        Commons.print("CalculatedTotalvalue >>>>>>" + calculatedTotalvalue);
        return calculatedTotalvalue;
    }

    private void doToast() {
        lvwplist.post(new Runnable() {
            @Override
            public void run() {
                Commons.print("Gor Run");
                subHandler();
                if (isMaxExceedToast)
                    showMessage(getString(R.string.exceed_allocation));

                if (isCreditLimitExceedToast)
                    showMessage(getString(R.string.exceeded_credit_limit));

                isToastDisabled = false;
                isMaxExceedToast = false;
                isCreditLimitExceedToast = false;
            }
        });

    }

    private void subHandler() {
        Handler sHandler = getHandler();
        sHandler.sendEmptyMessage(1);
    }

    private MyHandler handler = new MyHandler(getActivity());

    /**
     * An example getter to provide it to some external class
     * or just use 'new MyHandler(this)' if you are using it internally.
     * If you only use it internally you might even want it as final member:
     * private final MyHandler mHandler = new MyHandler(this);
     */
    private Handler getHandler() {
        return handler;
    }


    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            String qty = s + append;
            QUANTITY.setText(qty);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        Commons.print("number pressed");

        if (QUANTITY1 == null
                && (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER || bmodel.configurationMasterHelper.MUST_STOCK_ONLY))
            bmodel.showAlert(
                    getResources().getString(R.string.order_entry_not_allowed),
                    0);
        if (QUANTITY == null) {
            if (QUANTITY1 != null)
                bmodel.showAlert(
                        getResources().getString(R.string.please_select_item),
                        0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String enterText = QUANTITY.getText().toString();
                String strQty;
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            strQty = s + "";
                            QUANTITY.setText(strQty);
                        } else {
                            s = s / 10;
                            strQty = splitValue[0] + "." + s;
                            QUANTITY.setText(strQty);
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        Commons.printException("" + e);
                        strQty = SDUtil.convertToInt(enterText) + "";
                        QUANTITY.setText(strQty);
                    }


                } else {
                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    strQty = s + "";
                    QUANTITY.setText(strQty);
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (!s.contains(".")) {
                    s = s + ".";
                    QUANTITY.setText(s);
                }
            } else {
                Button ed = view.findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
            updateValue();
        }
    }

    private double updateValue() {
        int size = stockPropVector.size();
        LoadManagementBO stock;
        double temp, totalvalue = 0;
        for (int i = 0; i < size; i++) {
            stock = stockPropVector.get(i);
            if (stock.getStkprocaseqty() > 0 || stock.getStkproouterqty() > 0
                    || stock.getStkpropcsqty() > 0) {
                temp = ((stock.getStkprocaseqty() * stock.getCaseSize())
                        + stock.getStkpropcsqty() + (stock.getStkproouterqty() * stock
                        .getOuterSize())) * stock.getBaseprice();
                totalvalue = totalvalue + temp;
            }
        }
        totalValueText.setText(bmodel.formatValue(totalvalue) + "");
        return totalvalue;
    }

    private void doApplyStdQtyStuff(boolean isCreditLimitValidation) {

        LoadManagementBO stock;
        int stdqtytotal, currentstdqty, currentouterqty, currentcaseqty, currentpcsqty;
        double tmpTotalvalue = 0;
        double previousTotal;
        for (int i = 0; i < stockPropMylist.size(); i++) {
            stock = stockPropMylist.get(i);
            currentstdqty = 0;
            currentouterqty = 0;
            currentcaseqty = 0;
            currentpcsqty = 0;
            stdqtytotal = (stock.getStdcase() * stock.getCaseSize())
                    + (stock.getStdouter() * stock.getOuterSize())
                    + stock.getStdpcs();
            if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                if (stdqtytotal > 0)
                    if (stdqtytotal - stock.getSih() < 0)
                        currentstdqty = 0;
                    else
                        currentstdqty = stdqtytotal - stock.getSih();
                if (stock.getCaseSize() > 0) {
                    currentcaseqty = currentstdqty / stock.getCaseSize();
                }
                if (stock.getOuterSize() > 0 && stock.getCaseSize() > 0) {
                    currentouterqty = (currentstdqty % stock.getCaseSize())
                            / stock.getOuterSize();

                    currentpcsqty = (currentstdqty % stock.getCaseSize())
                            % stock.getOuterSize();
                }
            }
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                    && bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                if (currentstdqty > 0) {
                    if (currentstdqty > stock.getSuggestqty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getSuggestqty());
                    } else {
                        stock.setStkprocaseqty(currentcaseqty);
                        stock.setStkproouterqty(currentouterqty);
                        stock.setStkpropcsqty(currentpcsqty);
                    }
                } else {
                    if (stdqtytotal > stock.getSuggestqty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getSuggestqty());
                    } else {
                        stock.setStkprocaseqty(stock.getStdcase());
                        stock.setStkproouterqty(stock.getStdouter());
                        stock.setStkpropcsqty(stock.getStdpcs());
                    }

                }

                if (currentstdqty > 0) {
                    if (currentstdqty > stock.getMaxQty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getMaxQty());
                    } else {
                        stock.setStkprocaseqty(currentcaseqty);
                        stock.setStkproouterqty(currentouterqty);
                        stock.setStkpropcsqty(currentpcsqty);
                    }

                } else {
                    if (stdqtytotal > stock.getMaxQty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getMaxQty());
                    } else {
                        stock.setStkprocaseqty(stock.getStdcase());
                        stock.setStkproouterqty(stock.getStdouter());
                        stock.setStkpropcsqty(stock.getStdpcs());
                    }
                }

            }
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                    || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

                        if (currentstdqty > stock.getMaxQty()) {
                            stock.setStkprocaseqty(0);
                            stock.setStkproouterqty(0);
                            stock.setStkpropcsqty(stock.getMaxQty());
                        } else {
                            stock.setStkprocaseqty(currentcaseqty);
                            stock.setStkproouterqty(currentouterqty);
                            stock.setStkpropcsqty(currentpcsqty);
                        }
                    } else {
                        if (stdqtytotal > stock.getMaxQty()) {
                            stock.setStkprocaseqty(0);

                            stock.setStkpropcsqty(stock.getMaxQty());
                            stock.setStkproouterqty(0);
                        } else {
                            stock.setStkprocaseqty(stock.getStdcase());
                            stock.setStkproouterqty(stock.getStdouter());
                            stock.setStkpropcsqty(stock.getStdpcs());
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

                        if (currentstdqty > stock.getSuggestqty()) {
                            stock.setStkprocaseqty(0);
                            stock.setStkproouterqty(0);
                            stock.setStkpropcsqty(stock.getSuggestqty());
                        } else {
                            stock.setStkprocaseqty(currentcaseqty);
                            stock.setStkproouterqty(currentouterqty);
                            stock.setStkpropcsqty(currentpcsqty);
                        }
                    } else {
                        if ((stdqtytotal > stock.getSuggestqty())) {
                            stock.setStkprocaseqty(0);

                            stock.setStkpropcsqty(stock.getSuggestqty());
                            stock.setStkproouterqty(0);
                        } else {
                            stock.setStkprocaseqty(stock.getStdcase());
                            stock.setStkproouterqty(stock.getStdouter());
                            stock.setStkpropcsqty(stock.getStdpcs());
                        }
                    }
                }
            } else {
                if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                    stock.setStkprocaseqty(currentcaseqty);
                    stock.setStkproouterqty(currentouterqty);
                    stock.setStkpropcsqty(currentpcsqty);
                } else {
                    stock.setStkprocaseqty(stock.getStdcase());
                    stock.setStkproouterqty(stock.getStdouter());
                    stock.setStkpropcsqty(stock.getStdpcs());
                }
            }
            if (isCreditLimitValidation) {
                if (stock.getStkpropcsqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + (stock.getStkpropcsqty() * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkpropcsqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + (stock.getStkpropcsqty() * stock
                                .getBaseprice());
                    }
                }
                if (stock.getStkproouterqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + ((stock.getStkproouterqty() * stock
                            .getOuterSize()) * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkproouterqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + ((stock.getStkproouterqty() * stock
                                .getOuterSize()) * stock.getBaseprice());
                    }
                }
                if (stock.getStkprocaseqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + ((stock.getStkprocaseqty() * stock.getCaseSize()) * stock
                            .getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkprocaseqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + ((stock.getStkprocaseqty() * stock
                                .getCaseSize()) * stock.getBaseprice());
                    }
                }
            }
        }

    }

    private void showCurrentSTDqty() {
        if (stdqtytotal > 0)
            if (stdqtytotal - stock.getSih() < 0)
                currentstdqty = 0;
            else
                currentstdqty = stdqtytotal - stock.getSih();
        if (stock.getCaseSize() > 0) {
            currentcaseqty = currentstdqty / stock.getCaseSize();
        }
        if (stock.getOuterSize() > 0 && stock.getCaseSize() > 0) {
            currentouterqty = (currentstdqty % stock.getCaseSize())
                    / stock.getOuterSize();

            currentpcsqty = (currentstdqty % stock.getCaseSize())
                    % stock.getOuterSize();
        }
    }

    private void maxValidAndDistInv() {
        if (currentstdqty > 0) {
            if (currentstdqty > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }

        } else {
            if (stdqtytotal > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }

    }

    private void stockMaxValid() {
        if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

            if (currentstdqty > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }
        } else {
            if (stdqtytotal > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }
    }

    private void DistValidInv() {
        if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

            if (currentstdqty > stock.getSuggestqty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getSuggestqty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }
        } else {
            if ((stdqtytotal > stock.getSuggestqty())) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getSuggestqty();

            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }
    }

    private double doStdQtyCalculation() {
        try {
            for (int i = 0; i < stockPropMylist.size(); i++) {
                stock = stockPropMylist.get(i);
                currentstdqty = 0;
                currentouterqty = 0;
                currentcaseqty = 0;
                currentpcsqty = 0;
                calculatedCaseQty = 0;
                calculatedPieceQty = 0;
                calculatedOuterQty = 0;
                stdqtytotal = (stock.getStdcase() * stock.getCaseSize())
                        + (stock.getStdouter() * stock.getOuterSize())
                        + stock.getStdpcs();
                if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                    showCurrentSTDqty();
                }
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                        && bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    maxValidAndDistInv();
                }
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                        || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                        stockMaxValid();
                    } else if (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                        DistValidInv();
                    }
                } else {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                        calculatedCaseQty = currentcaseqty;
                        calculatedOuterQty = currentouterqty;
                        calculatedPieceQty = currentpcsqty;
                    } else {
                        calculatedCaseQty = stock.getStdcase();
                        calculatedOuterQty = stock.getStdouter();
                        calculatedPieceQty = stock.getStdpcs();
                    }
                }

                if (calculatedCaseQty > 0 || calculatedOuterQty > 0
                        || calculatedPieceQty > 0) {
                    calculatedTotalvalue = calculatedTotalvalue
                            + ((calculatedCaseQty * stock.getCaseSize())
                            + calculatedPieceQty + (calculatedOuterQty * stock
                            .getOuterSize())) * stock.getBaseprice();
                }

            }
            Commons.print("CalculatedTotalvalue >>>>>>" + calculatedTotalvalue);
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
        return calculatedTotalvalue;
    }


    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                brandbutton = BRAND;
                generalbutton = GENERAL;
                getActivity().supportInvalidateOptionsMenu();
            }
            if (mEdt_searchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync(this);
                searchAsync.execute();
            } else {
                showMessage(getString(R.string.enter_atleast_three_letters));
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        stockPropVector = bmodel.productHelper.getLoadMgmtProducts();

        // Close the drawer
        mDrawerLayout.closeDrawers();

        // Change the Brand button Name
        brandbutton = mFilterText;

        // Consider generalbutton text if it is dependent filter.
        String generaltxt = generalbutton;

        productName.setText("");

        if (stockPropVector == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = stockPropVector.size();
        stockPropMylist = new ArrayList<>();

        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = stockPropVector
                    .elementAt(i);
            if (ret.getIssalable() == 1) {
                if (isSpecialFilter_enabled) {
                    if (bid == ret.getParentid() || bid == -1) {

                        if (generaltxt.equals(mSbd) && ret.isRPS()) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mOrdered)
                                && (ret.getOrderedPcsQty() > 0
                                || ret.getOrderedCaseQty() > 0 || ret
                                .getOuterOrderedCaseQty() > 0)) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mPurchased)
                                && ret.getIsPurchased() == 1) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mInitiative)
                                && ret.getIsInitiativeProduct() == 1) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mCommon)
                                && (ret.isRPS()
                                || (ret.getIsInitiativeProduct() == 1) || (ret
                                .getIsPurchased() == 1))) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mSbdGaps)
                                && (ret.isRPS() && !ret.isSBDAcheived())) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(GENERAL)) {

                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mInStock) && ret.getWsih() > 0) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mOnAllocation)
                                && ret.isAllocation() == 1) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equals(mPromo) && ret.isPromo()) {
                            stockPropMylist.add(ret);
                        } else if (generaltxt.equalsIgnoreCase(msih) && ret.getSih() > 0) {
                            stockPropMylist.add(ret);
                        }
                    }
                } else {
                    if (bid == ret.getParentid() || bid == -1) {

                        stockPropMylist.add(ret);
                    }
                }
            }
        }

        // Filter name and product count in product name header

        mSchedule = new MyAdapter(stockPropMylist);
        lvwplist.setAdapter(mSchedule);
        getActivity().supportInvalidateOptionsMenu();
    }


    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display
        generalbutton = mFilterText;
        updateBrandText(BRAND, -1);

    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();

    }


    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        Commons.print("selected filter " + mFilteredPid + ", " + mSelectedIdByLevelId + ", " + mAttributeProducts + ", " + mFilterText);
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        stockPropMylist = new ArrayList<>();
        if (mAttributeProducts != null) {

            if (mFilteredPid != 0) {
                for (LoadManagementBO productBO : stockPropVector) {
                    if (productBO.getIssalable() == 1) {
                        if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductid())) {
                                stockPropMylist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : stockPropVector) {
                        if (productBO.getIssalable() == 1) {
                            if (pid == productBO.getProductid()) {
                                stockPropMylist.add(productBO);
                            }
                        }
                    }
                }
            }
        } else {
            if (mFilteredPid != 0 && !mFilterText.equalsIgnoreCase("")) {
                for (LoadManagementBO productBO : stockPropVector) {
                    if (productBO.getIssalable() == 1) {
                        if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                            stockPropMylist.add(productBO);
                        }
                    }

                }

            } else {
                updateGeneralText(GENERAL);
            }
        }


        mDrawerLayout.closeDrawers();

        if (!mFilterText.equalsIgnoreCase("")) {
            refreshList();
            updateValue();
        }

        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            // Change color if Filter is selected
            if (!generalbutton.equals(GENERAL))
                menu.findItem(R.id.menu_spl_filter).setIcon(
                        R.drawable.ic_action_star_select);

            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            if (isSpecialFilter_enabled)
                menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_spl_filter).setVisible(false);

            menu.findItem(R.id.menu_next).setVisible(false);

            menu.findItem(R.id.menu_remarks).setVisible(false);

            menu.findItem(R.id.menu_scheme).setVisible(false);


            if (bmodel.configurationMasterHelper.SHOW_SO_APPLY)
                menu.findItem(R.id.menu_apply_so).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_apply_so).setVisible(false);

            if (bmodel.configurationMasterHelper.SHOW_STD_QTY_APPLY)
                menu.findItem(R.id.menu_apply_std_qty).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_apply_std_qty).setVisible(false);


            if (bmodel.productHelper.getInStoreLocation().size() == 1)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);

            menu.findItem(R.id.menu_loc_filter).setVisible(false);


            menu.findItem(R.id.menu_sih_apply).setVisible(false);

            // menu.findItem(R.id.menu_fivefilter).setVisible(false);

            menu.findItem(R.id.menu_fivefilter).setVisible(bmodel.productHelper.isFilterAvaiable(bundle.getString("menuCode")));

            menu.findItem(R.id.menu_expand).setVisible(false);

            menu.findItem(R.id.menu_next).setVisible(false);

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
                onBackButtonClick();
            }
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            applyStdQty();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);

            loadFiveFilterFragment(bundle, R.id.right_drawer);

            return true;
        } else if (i == R.id.menu_apply_so) {
            applyStockSo();
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            applyStdQty();
            return true;
        }


        return false;
    }


    public void applyStdQty() {
        isToastDisabled = true;
        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
            if (doStdQtyCalculation() > bmodel.userMasterHelper
                    .getUserMasterBO().getCreditlimit()) {
                onCreateDialog(2);
            } else {
                showProgDialog();
                doApplyStdQtyStuff(false);
                mSchedule.notifyDataSetChanged();
                doToast();
            }
        } else {
            showProgDialog();
            doApplyStdQtyStuff(false);
            mSchedule.notifyDataSetChanged();
            doToast();
        }

    }

    public void applyStockSo() {

        isToastDisabled = true;
        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
            if (doApplyStockQtyCalculation() > bmodel.userMasterHelper
                    .getUserMasterBO().getCreditlimit()) {
                onCreateDialog(3);
            } else {
                showProgDialog();
                doApplyStockQtyStuff(false);
                mSchedule.notifyDataSetChanged();
                doToast();
            }
        } else {
            showProgDialog();
            doApplyStockQtyStuff(false);
            mSchedule.notifyDataSetChanged();
            doToast();
        }

    }


    private void generalFilterClickedFragment() {
        try {

            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", GENERAL);
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper
                            .getSpecialFilterList("STKPRO12"));

            // set Fragmentclass Arguments
            SpecialFilterFragment fragobj = new SpecialFilterFragment(mSelectedFilterMap);

            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindDrawables(view.findViewById(R.id.root));

    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }


}
