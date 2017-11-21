package com.ivy.countersales;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.countersales.bo.CS_StockApplyHeaderBO;
import com.ivy.countersales.bo.CS_StockApplyProductBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.FilterFiveFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class CS_StockApply_Detail extends IvyBaseActivityNoActionBar implements View.OnClickListener, DataPickerDialogFragment.UpdateDateInterface
        , BrandDialogInterface, TextView.OnEditorActionListener {

    private BusinessModel bmodel;
    private Toolbar toolbar;
    private TextView toolbarTxt;
    private Vector<CS_StockApplyProductBO> myList;
    private ListView lv;
    private EditText QUANTITY, mEdt_searchproductName;
    private Button mBtn_Search, mBtnFilterPopup, mBtn_clear;
    private ViewFlipper viewFlipper;
    private ArrayList<String> mSearchTypeArray = new ArrayList<String>();
    private String append = "";
    private String mReceiptId;
    private boolean isManualLoad;
    private CS_StockApplyHeaderBO mHeaderBO;
    private Button btnApply, btnReject;
    private String mTransactionType;
    private CardView headerLayout;
    private EditText mReceiptNo;
    private Button mReceiptDate;
    private String strBarCodeSearch = "ALL";
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private String screenCode;
    private Spinner spin_stock_type;
    private String mSelectedFilter;
    private int stockeTypeId;

    private HashMap<Integer, Integer> mSelectedIdByLevelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_cs_stock_apply_detail);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTxt = (TextView) findViewById(R.id.tv_toolbar_title);
        toolbarTxt.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mEdt_searchproductName = (EditText) findViewById(
                R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            toolbarTxt.setText(bmodel.mSelectedActivityName);
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final Bundle extras = getIntent().getExtras();
        mDrawerLayout = (DrawerLayout) findViewById(
                R.id.drawer_layout);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(extras.getString("screenName"));
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(extras.getString("screenName"));
                }

                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                supportInvalidateOptionsMenu();
            }
        };


        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mSearchTypeArray = new ArrayList<String>();
        mSearchTypeArray.add(getResources()
                .getString(R.string.product_name));
        //  mSearchTypeArray.add("GCAS Code");
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));
        isManualLoad = false;
        mReceiptId = "0";

        if (extras != null) {
            isManualLoad = extras.getBoolean("isManualLoad", false);
            mReceiptId = extras.getString("receipt_id", "0");
            screenCode = extras.getString("CurrentActivityCode");
            stockeTypeId = extras.getInt("typeid");

        }

        headerLayout = (CardView) findViewById(R.id.header);

        if (isManualLoad) {
            headerLayout.setVisibility(View.VISIBLE);
            mHeaderBO = new CS_StockApplyHeaderBO();
            myList = bmodel.CS_StockApplyHelper.getManualProducts();
            prepareHeader();
        } else {
            headerLayout.setVisibility(View.GONE);
            for (CS_StockApplyHeaderBO header : bmodel.CS_StockApplyHelper.getCSStockApplyHeader()) {
                if (header.getReceiptId().equalsIgnoreCase(mReceiptId)) {
                    mHeaderBO = header;
                }
            }

            myList = bmodel.CS_StockApplyHelper.getCounterStockHeaderDetails().get(mReceiptId);
        }

        MyAdapter mSchedule = new MyAdapter(myList);
        lv = (ListView) findViewById(R.id.lvwplist);
        lv.setCacheColorHint(0);
        lv.setAdapter(mSchedule);
        searchText();
        btnApply = (Button) findViewById(R.id.btn_apply);
        btnApply.setOnClickListener(this);

        btnReject = (Button) findViewById(R.id.btn_reject);
        btnReject.setOnClickListener(this);

        if (mHeaderBO.getUpload().equals("N"))
            (findViewById(R.id.footer)).setVisibility(View.GONE);
        if (isManualLoad)
            btnReject.setVisibility(View.GONE);

        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private void prepareHeader() {

        mReceiptNo = (EditText) findViewById(R.id.edit_receipt_no);
        mReceiptNo.setText(mHeaderBO.getReferenceNo());
        mReceiptNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mHeaderBO.setReferenceNo(s.toString());
            }
        });

        mReceiptDate = (Button) findViewById(R.id.btn_datepicker);

        mReceiptDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker1");
            }
        });

        spin_stock_type = (Spinner) findViewById(R.id.spin_stock_type);

        ArrayAdapter<StandardListBO> stockTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        stockTypeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

       /* StandardListBO type = new StandardListBO();
        type.setListID("-1");
        type.setListName("Select Type");

        stockTypeAdapter.add(type);*/

        stockTypeAdapter.addAll(bmodel.CS_StockApplyHelper.getStockType());

        spin_stock_type.setAdapter(stockTypeAdapter);

        spin_stock_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StandardListBO type = (StandardListBO) parent.getSelectedItem();
                mHeaderBO.setStockTypeId(Integer.parseInt(type.getListID()));
                if (isManualLoad) {
                    if (viewFlipper.getDisplayedChild() != 0) {
                        viewFlipper.showPrevious();
                        mEdt_searchproductName.setText("");
                        /** set the following value to clear the **/
                        strBarCodeSearch = "ALL";
                    }
                    loadProducts(type.getListCode(), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (bmodel.CS_StockApplyHelper.getStockType().size() > 0) {
            spin_stock_type.setSelection(0);
        }
    }

    private void loadProducts(String stockTypecode, boolean isResetData) {

        Vector<CS_StockApplyProductBO> items = new Vector<>();

        for (CS_StockApplyProductBO bo : bmodel.CS_StockApplyHelper.getManualProducts()) {
            if (isResetData) {
                bo.setQty(0);
                bo.setBalanceQty(0);
            }

            if (stockTypecode.equalsIgnoreCase("ACCESS")) {
                if (bo.getIsSalable() == 0 && bo.getIsReturnable() == 0) {
                    items.add(bo);
                }

            } else if (stockTypecode.equalsIgnoreCase("FREE")) {
                if (bo.getIsSalable() == 1 && bo.getMrp() == 0) {
                    items.add(bo);
                }

            } else if (stockTypecode.equalsIgnoreCase("NORMAL") || stockTypecode.equalsIgnoreCase("TESTER")) {
                if (bo.getIsSalable() == 1 && bo.getMrp() != 0) {
                    items.add(bo);
                }
            }
        }

        MyAdapter mSchedule = new MyAdapter(items);
        lv.setAdapter(mSchedule);

    }

    private boolean isValidateHeader() {

        if (!mHeaderBO.getReferenceNo().equalsIgnoreCase("")
                && !mHeaderBO.getReceiptDate().equalsIgnoreCase("")
                && mHeaderBO.getStockTypeId() != -1)
            return true;

        return false;
    }

    @Override
    public void updateDate(Date date, String tag) {

        String mDate = DateUtil.convertDateObjectToRequestedFormat(
                date, "yyyy/MM/dd");

        String mTodayDate = SDUtil.now(SDUtil.DATE_GLOBAL);

        int dateDifference = SDUtil.compareDate(mDate, mTodayDate, "yyyy/MM/dd");

        if (dateDifference <= 0) {
            mReceiptDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                    date, ConfigurationMasterHelper.outDateFormat));
            mHeaderBO.setReceiptDate(mDate);
        } else {
            Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                    getResources().getString(
                            R.string.future_date_not_allowed),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

    }

    private void disableApplyReject() {
        btnApply.setEnabled(false);
        btnReject.setEnabled(false);
        btnApply.setBackgroundResource(R.drawable.round_disabled_btn);
        btnReject.setBackgroundResource(R.drawable.round_disabled_btn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_counter_sales, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_barcode).setVisible(true);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);
        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        } else {
            menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
        }

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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            onBackButtonClick();
            return true;
        } else if (i == R.id.menu_barcode) {
            checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                new IntentIntegrator(this).setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (i == R.id.menu_fivefilter) {
            /*if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }*/
            FiveFilterFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void FiveFilterFragment() {
        try {


            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
            } else {
               /* strBarCodeSearch = result.getContents();
                if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                    bmodel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                    loadBarcodeSearchedList();
                }*/
                strBarCodeSearch = result.getContents();
                if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                    bmodel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                    mEdt_searchproductName.setText(strBarCodeSearch);
                    if (viewFlipper.getDisplayedChild() == 0) {
                        viewFlipper.showNext();


                    }
                }
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onBackButtonClick() {
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_apply) {

            if (isManualLoad) {
                mTransactionType = "M";
                if (isValidateHeader()) {
                    if (isDateToSave()) {
                        disableApplyReject();
                        new SaveAsyncTask().execute();
                    } else {
                        Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                                getResources().getString(
                                        R.string.no_data_tosave),
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                            getResources().getString(
                                    R.string.manual_stock_apply_header_validation),
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else {
                if (isDateToSave()) {
                    disableApplyReject();

                    mTransactionType = "A";

                    for (CS_StockApplyProductBO product : myList) {
                        if (product.getDamagedQty() > 0) {
                            mTransactionType = "P";
                            break;
                        }
                    }
                    new SaveAsyncTask().execute();
                } else {
                    Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                            getResources().getString(
                                    R.string.no_data_tosave),
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }


        } else if (view.getId() == R.id.btn_reject) {
            if (isDateToSave()) {
                disableApplyReject();

                mTransactionType = "R";
                new SaveAsyncTask().execute();
            } else {
                Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                        getResources().getString(
                                R.string.no_data_tosave),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else if (view.getId() == R.id.btn_search) {
            viewFlipper.showNext();
        } else if (view.getId() == R.id.btn_filter_popup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
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

        } else if (view.getId() == R.id.btn_clear) {
            viewFlipper.showPrevious();
            mEdt_searchproductName.setText("");
            /** set the following value to clear the **/
            strBarCodeSearch = "ALL";
            supportInvalidateOptionsMenu();
            StandardListBO type = (StandardListBO) spin_stock_type.getSelectedItem();
            if (isManualLoad) {
                loadProducts(type.getListCode(), false);
            }

        }
    }

    private boolean isDateToSave() {
        for (CS_StockApplyProductBO product : myList) {
            if (product.getQty() > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
        if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
            loadSearchedList();
            return true;
        }
        return false;
    }


    class ViewHolder {
        private CS_StockApplyProductBO productBO;
        private int mProductId;
        private TextView mProductName;
        private TextView mQty;
        private EditText mQtyET;
        private EditText mDamagedQty;
        private TextView mBalanceQty;
        private TextView mrp;
    }

    private class MyAdapter extends ArrayAdapter<CS_StockApplyProductBO> {
        private final Vector<CS_StockApplyProductBO> items;

        public MyAdapter(Vector<CS_StockApplyProductBO> items) {
            super(CS_StockApply_Detail.this, R.layout.activity_cs_stock_apply_detail_list, items);
            this.items = items;
        }

        public CS_StockApplyProductBO getItem(int position) {
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
                LayoutInflater inflater = LayoutInflater.from(CS_StockApply_Detail.this);

                row = inflater.inflate(
                        R.layout.activity_cs_stock_apply_detail_list, parent, false);

                // For dotted line
                //row.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                holder = new ViewHolder();
                holder.mProductName = (TextView) row.findViewById(R.id.sku);
                holder.mProductName.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.mProductName.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.mQty = (TextView) row.findViewById(R.id.server_qty);
                holder.mQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.mBalanceQty = (TextView) row.findViewById(R.id.balance);
                holder.mBalanceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.mDamagedQty = (EditText) row.findViewById(R.id.damaged_qty);

                holder.mrp = (TextView) row.findViewById(R.id.mrp);
                holder.mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.mDamagedQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mDamagedQty;
                        int inType = holder.mDamagedQty.getInputType();
                        holder.mDamagedQty.setInputType(InputType.TYPE_NULL);
                        holder.mDamagedQty.onTouchEvent(event);
                        holder.mDamagedQty.setInputType(inType);
                        holder.mDamagedQty.selectAll();
                        holder.mDamagedQty.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.mDamagedQty.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.mDamagedQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        int damagedQty;
                        if (!qty.equals("")) {
                            damagedQty = SDUtil.convertToInt(qty);
                            if (damagedQty > holder.productBO.getQty()) {
                                Toast toast = Toast.makeText(CS_StockApply_Detail.this,
                                        getResources().getString(
                                                R.string.exceed_allocation),
                                        Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                damagedQty = damagedQty / 10;
                                holder.mDamagedQty.setText("" + damagedQty);
                            }
                        } else {
                            damagedQty = 0;
                        }
                        holder.productBO.setDamagedQty(damagedQty);
                        holder.productBO.setBalanceQty(holder.productBO.getQty() - damagedQty);
                        holder.mBalanceQty.setText("" + holder.productBO.getBalanceQty());
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

                holder.mQtyET = (EditText) row.findViewById(R.id.server_qty_et);

                holder.mQtyET.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mQtyET;
                        int inType = holder.mQtyET.getInputType();
                        holder.mQtyET.setInputType(InputType.TYPE_NULL);
                        holder.mQtyET.onTouchEvent(event);
                        holder.mQtyET.setInputType(inType);
                        holder.mQtyET.selectAll();
                        holder.mQtyET.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.mQtyET.getWindowToken(), 0);

                        return true;
                    }
                });

                holder.mQtyET.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        int serverQty;
                        if (!qty.equals("")) {
                            serverQty = SDUtil.convertToInt(qty);
                        } else {
                            serverQty = 0;
                        }
                        holder.productBO.setQty(serverQty);
                        holder.mDamagedQty.setText("0");
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

                if (isManualLoad) {
                    holder.mQtyET.setVisibility(View.VISIBLE);
                    holder.mQty.setVisibility(View.GONE);
                } else {
                    holder.mQtyET.setVisibility(View.GONE);
                    holder.mQty.setVisibility(View.VISIBLE);
                }

                if (mHeaderBO.getUpload().equals("N")) {
                    holder.mDamagedQty.setEnabled(false);
                } else {
                    holder.mDamagedQty.setEnabled(true);
                }


                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = items.get(position);

            holder.mProductName.setText(holder.productBO.getProductName());
            if (isManualLoad) {
                holder.mQtyET.setText("" + holder.productBO.getQty());
            } else {
                holder.mQty.setText("" + holder.productBO.getQty());
            }
            holder.mDamagedQty.setText("" + holder.productBO.getDamagedQty());
            holder.mBalanceQty.setText("" + holder.productBO.getBalanceQty());

            TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            holder.mrp.setText(SDUtil.format(holder.productBO.getMrp(), 2, 0) + "");

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
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
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

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... type) {
            try {
                bmodel.CS_StockApplyHelper.saveTransaction(mHeaderBO, myList, mTransactionType);

                bmodel.CS_StockApplyHelper.loadStockDetails();

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(CS_StockApply_Detail.this);

            customProgressDialog(builder, CS_StockApply_Detail.this, getResources().getString(R.string.saving));
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
                //Toast.makeText(CS_StockApply_Detail.this,"Saved SucessFully",Toast.LENGTH_LONG).show();
                bmodel.saveModuleCompletion(screenCode);
                showSavedSuccessDialog();
            }

        }
    }

    private void showSavedSuccessDialog() {
        new CommonDialog(getApplicationContext(), CS_StockApply_Detail.this,
                "", getResources().getString(R.string.saved_successfully),
                false, getResources().getString(R.string.ok),
                null, new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {

                finish();
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        }).show();
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        Vector<CS_StockApplyProductBO> items;
        if (isManualLoad) {
            items = bmodel.CS_StockApplyHelper.getManualProducts();

        } else {

            items = bmodel.CS_StockApplyHelper.getCounterStockHeaderDetails().get(mReceiptId);
        }

        int count = 0;
        Vector<CS_StockApplyProductBO> myListTemp = new Vector<>();
        StandardListBO type = new StandardListBO();
        String typeCode = "";
        if (isManualLoad) {
            type = (StandardListBO) spin_stock_type.getSelectedItem();
            typeCode = type.getListCode();
        } else {
            for (StandardListBO typeBo : bmodel.CS_StockApplyHelper.getStockType()) {
                if ((stockeTypeId + "").equals(typeBo.getListID())) {
                    typeCode = typeBo.getListCode();
                    break;
                }

            }
        }

        if (mAttributeProducts != null) {
            count = 0;
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    count++;
                    for (CS_StockApplyProductBO productBO : items) {

                        if (levelBO.getProductID() == productBO.getParentId()) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductId())) {


                                if (typeCode.equalsIgnoreCase("ACCESS")) {
                                    if (productBO.getIsSalable() == 0 && productBO.getIsReturnable() == 0) {
                                        myListTemp.add(productBO);
                                    } else if (!isManualLoad) {
                                        myListTemp.add(productBO);
                                    }

                                } else if (typeCode.equalsIgnoreCase("FREE")) {
                                    if (productBO.getIsSalable() == 1 && productBO.getMrp() == 0) {
                                        myListTemp.add(productBO);
                                    } else if (!isManualLoad) {
                                        myListTemp.add(productBO);
                                    }

                                } else if (typeCode.equalsIgnoreCase("NORMAL") || typeCode.equalsIgnoreCase("TESTER")) {
                                    if (productBO.getIsSalable() == 1 && productBO.getMrp() != 0) {
                                        myListTemp.add(productBO);
                                    } else if (!isManualLoad) {
                                        myListTemp.add(productBO);
                                    }
                                }

                            }
                        }


                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (CS_StockApplyProductBO productBO : items) {
                        if (pid == productBO.getProductId()) {


                            if (typeCode.equalsIgnoreCase("ACCESS")) {
                                if (productBO.getIsSalable() == 0 && productBO.getIsReturnable() == 0) {
                                    myListTemp.add(productBO);
                                } else if (!isManualLoad) {
                                    myListTemp.add(productBO);
                                }

                            } else if (typeCode.equalsIgnoreCase("FREE")) {
                                if (productBO.getIsSalable() == 1 && productBO.getMrp() == 0) {
                                    myListTemp.add(productBO);
                                } else if (!isManualLoad) {
                                    myListTemp.add(productBO);
                                }

                            } else if (typeCode.equalsIgnoreCase("NORMAL") || typeCode.equalsIgnoreCase("TESTER")) {
                                if (productBO.getIsSalable() == 1 && productBO.getMrp() != 0) {
                                    myListTemp.add(productBO);
                                } else if (!isManualLoad) {
                                    myListTemp.add(productBO);
                                }
                            }

                        }


                    }
                }
            }
        } else {
            for (LevelBO levelBO : mParentIdList) {
                count++;
                for (CS_StockApplyProductBO productBO : items) {

                    if (levelBO.getProductID() == productBO.getParentId()) {


                        if (typeCode.equalsIgnoreCase("ACCESS")) {
                            if (productBO.getIsSalable() == 0 && productBO.getIsReturnable() == 0) {
                                myListTemp.add(productBO);
                            } else if (!isManualLoad) {
                                myListTemp.add(productBO);
                            }

                        } else if (typeCode.equalsIgnoreCase("FREE")) {
                            if (productBO.getIsSalable() == 1 && productBO.getMrp() == 0) {
                                myListTemp.add(productBO);
                            } else if (!isManualLoad) {
                                myListTemp.add(productBO);
                            }

                        } else if (typeCode.equalsIgnoreCase("NORMAL") || typeCode.equalsIgnoreCase("TESTER")) {
                            if (productBO.getIsSalable() == 1 && productBO.getMrp() != 0) {
                                myListTemp.add(productBO);
                            } else if (!isManualLoad) {
                                myListTemp.add(productBO);
                            }
                        }

                    }


                }
            }
        }
        if (!isManualLoad && mFilterText.equals("") && myListTemp.size() <= 0) {
            myListTemp = bmodel.CS_StockApplyHelper.getCounterStockHeaderDetails().get(mReceiptId);
        }
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;


        MyAdapter mSchedule = new MyAdapter(myListTemp);
        lv.setAdapter(mSchedule);


    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void loadStartVisit() {

    }

   /* private void loadBarcodeSearchedList() {
        try {
            Vector<CS_StockApplyProductBO> items = new Vector<>();
            if (isManualLoad) {
                StandardListBO type = (StandardListBO) spin_stock_type.getSelectedItem();
                for (CS_StockApplyProductBO bo : bmodel.CS_StockApplyHelper.getManualProducts()) {
                    bo.setQty(0);
                    bo.setBalanceQty(0);

                    if (type.getListCode().equalsIgnoreCase("ACCESS")) {
                        if (bo.getIsSalable() == 0 && bo.getIsReturnable() == 0) {
                            items.add(bo);
                        }

                    } else if (type.getListCode().equalsIgnoreCase("FREE")) {
                        if (bo.getIsSalable() == 1 && bo.getMrp() == 0) {
                            items.add(bo);
                        }

                    } else if (type.getListCode().equalsIgnoreCase("NORMAL") || type.getListCode().equalsIgnoreCase("TESTER")) {
                        if (bo.getIsSalable() == 1 && bo.getMrp() != 0) {
                            items.add(bo);
                        }
                    }
                }

            } else {

                items = bmodel.CS_StockApplyHelper.getCounterStockHeaderDetails().get(mReceiptId);
            }

            int siz = items.size();
            Vector<CS_StockApplyProductBO> myListTemp = new Vector<>();
            for (int i = 0; i < siz; ++i) {
                CS_StockApplyProductBO ret = items.elementAt(i);


                if (ret.getBarcode() != null
                        && (ret.getBarcode().toLowerCase()
                        .contains(strBarCodeSearch))) {

                    myListTemp.add(ret);

                }


            }


            MyAdapter mSchedule = new MyAdapter(myListTemp);
            lv.setAdapter(mSchedule);
            if (myListTemp.size() <= 0)
                Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();


        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }*/

    private void searchText() {
        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        loadSearchedList();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TODO Auto-generated method stub

                }
            });
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }


    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<CS_StockApplyProductBO> items = new Vector<>();
            if (isManualLoad) {
                StandardListBO type = (StandardListBO) spin_stock_type.getSelectedItem();
                for (CS_StockApplyProductBO bo : bmodel.CS_StockApplyHelper.getManualProducts()) {
                    // bo.setQty(0);
                    // bo.setBalanceQty(0);

                    if (type.getListCode().equalsIgnoreCase("ACCESS")) {
                        if (bo.getIsSalable() == 0 && bo.getIsReturnable() == 0) {
                            items.add(bo);
                        }

                    } else if (type.getListCode().equalsIgnoreCase("FREE")) {
                        if (bo.getIsSalable() == 1 && bo.getMrp() == 0) {
                            items.add(bo);
                        }

                    } else if (type.getListCode().equalsIgnoreCase("NORMAL") || type.getListCode().equalsIgnoreCase("TESTER")) {
                        if (bo.getIsSalable() == 1 && bo.getMrp() != 0) {
                            items.add(bo);
                        }
                    }
                }

            } else {

                items = bmodel.CS_StockApplyHelper.getCounterStockHeaderDetails().get(mReceiptId);
            }
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            Vector<CS_StockApplyProductBO> mylist = new Vector<CS_StockApplyProductBO>();
            CS_StockApplyProductBO ret;
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ret = (CS_StockApplyProductBO) items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarcode() != null && ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        mylist.add(ret);

                    }

                    Commons.print("siz Barcode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    Commons.print("siz product_name : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    if (ret.getProductName() != null && ret.getProductName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        mylist.add(ret);
                    }
                }
            }

            MyAdapter mSchedule = new MyAdapter(mylist);
            lv.setAdapter(mSchedule);
            if (mylist.size() <= 0)
                Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}





