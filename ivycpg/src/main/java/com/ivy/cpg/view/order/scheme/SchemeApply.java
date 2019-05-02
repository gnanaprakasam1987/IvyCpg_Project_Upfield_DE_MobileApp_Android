package com.ivy.cpg.view.order.scheme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.cpg.view.initiative.InitiativeActivity;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;

/**
 * This screen will show list of schemes applied for current order from that user can select/Reject/Modify
 */
public class SchemeApply extends IvyBaseActivityNoActionBar {
    private static final String TAG = "Scheme Apply";

    private SchemeDetailsMasterHelper schemeHelper;
    private ExpandableListView mExpandableLV;
    private BusinessModel bModel;

    private boolean isClick;
    private EditText QUANTITY;
    private String append = "";
    private String screenCode = "MENU_STK_ORD";
    private SchemeExpandableAdapter mExpandableAdapterNew;
    private ArrayList<SchemeBO> mSchemeDoneList;
    private String fromOrderScreen = "";
    private String schemeViewTxt = "View";
    private SchemeFreeProductSelectionDialog mSchemeDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.apply_scheme);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null)
            setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.Scheme_apply));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());

        mExpandableLV = findViewById(R.id.elv);
        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
                fromOrderScreen = extras.getString("ForScheme", "STD_ORDER");
            }
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels("scheme_view") != null)
                schemeViewTxt = bModel.labelsMasterHelper.applyLabels("scheme_view");
            else schemeViewTxt = getResources().getString(R.string.view);
        } catch (Exception e) {
            Commons.printException(e);
        }


        btnNext.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {

                if (isSchemeAplied(schemeHelper.getAppliedSchemeList())) {
                    schemeHelper.clearOffInvoiceSchemeList();
                    click(2);
                } else {
                    showAlert(getResources().getString(R.string.you_have_unchecked_applicable_scheme), 1);
                }
            }
        });

        if (fromOrderScreen.equalsIgnoreCase("MENU_STK_ORD") ||
                fromOrderScreen.equalsIgnoreCase("MENU_ORDER") ||
                fromOrderScreen.equalsIgnoreCase("MENU_CATALOG_ORDER")) {
            new SchemeApplyAsync().execute();

        } else {
            mSchemeDoneList = schemeHelper.getAppliedSchemeList();
            if (mSchemeDoneList.size() > 0) {
                mExpandableAdapterNew = new SchemeExpandableAdapter();
                mExpandableLV.setAdapter(mExpandableAdapterNew);
            }
        }


        if (!schemeHelper.IS_SCHEME_EDITABLE)
            ((LinearLayout) findViewById(R.id.footer)).setVisibility(View.GONE);

    }

    /**
     * @param mSchemeDoneList
     * @return false  - if scheme apply done in partially
     * @defalut flag is true
     */
    private boolean isSchemeAplied(ArrayList<SchemeBO> mSchemeDoneList) {
        boolean isFlag = true;
        if (mSchemeDoneList.size() > 0)
            for (SchemeBO schBo : mSchemeDoneList) {
                if (!schBo.isPriceTypeSeleted() && !schBo.isAmountTypeSelected()
                        && !schBo.isQuantityTypeSelected() && !schBo.isDiscountPrecentSelected())
                    isFlag = false;
            }

        return isFlag;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bModel.configurationMasterHelper.IS_SKIP_SCHEME_APPLY) {
            Intent i = new Intent(SchemeApply.this, OrderSummary.class);
            i.putExtra("ScreenCode", screenCode);
            startActivity(i);
            finish();
        }
    }


    /**
     * Getting scheme applied list by giving product master list
     */
    private class SchemeApplyAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {

                schemeHelper.schemeApply(bModel.productHelper.getProductMaster());//mOrderedList,mOrderedProductBOById,bModel.batchAllocationHelper.getBatchlistByProductID());
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isDone) {
            super.onPostExecute(isDone);
            if (isDone) {
                mSchemeDoneList = schemeHelper.getAppliedSchemeList();
                if (mSchemeDoneList.size() > 0) {
                    mExpandableAdapterNew = new SchemeExpandableAdapter();
                    mExpandableLV.setAdapter(mExpandableAdapterNew);
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_only_next, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_next).setVisible(false);
        if (screenCode.equalsIgnoreCase("CSale")) {
            menu.findItem(R.id.menu_counter_remark).setVisible(true);
        }
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            click(1);
            return true;
        } else if (i == R.id.menu_counter_remark) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog = new RemarksDialog("MENU_COUNTER");
            dialog.setCancelable(false);
            dialog.show(ft, "counter_scheme_remark");
        }
        return super.onOptionsItemSelected(item);
    }

    private void click(int action) {

        if (!isClick) {

            if (action == 1) {
                isClick = true;
                if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(SchemeApply.this,
                            BatchAllocation.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else {
                    Intent intent;
                    if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                        intent = new Intent(SchemeApply.this, CatalogOrder.class);
                    } else {
                        intent = new Intent(SchemeApply.this, StockAndOrder.class);
                    }
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);

                }
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();

            } else if (action == 2) {
                if (mSchemeDoneList != null
                        && mSchemeDoneList.size() > 0)
                    if (!schemeHelper.isValuesAppliedBetweenTheRange(mSchemeDoneList)) {
                        showAlert(getResources().getString(R.string.not_in_range_reset), 0);

                        return;
                    }
                if (bModel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                    Intent init = new Intent(SchemeApply.this,
                            OrderDiscount.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);

                } else if (bModel.configurationMasterHelper.IS_INITIATIVE) {

                    Intent init = new Intent(SchemeApply.this,
                            InitiativeActivity.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);

                } else if (bModel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                    Intent i = new Intent(SchemeApply.this,
                            DigitalContentActivity.class);
                    i.putExtra("FromInit", "Initiative");
                    i.putExtra("ScreenCode", screenCode);
                    startActivity(i);

                } else {
                    Intent i = new Intent(SchemeApply.this, OrderSummary.class);
                    i.putExtra("ScreenCode", screenCode);
                    startActivity(i);

                }
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_CANCELED) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                } else if (resultCode == RESULT_OK) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
        }
    }

    NextSlabSchemeDialog mSchemePromDialog;


    class SchemeExpandableAdapter extends BaseExpandableListAdapter {

        LayoutInflater mInflater;

        public SchemeExpandableAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View view, ViewGroup parent) {

            return view;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;

        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {
            if (mSchemeDoneList == null)
                return 0;
            return mSchemeDoneList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View view, ViewGroup parent) {

            final SchemeProductHolder holder;

            if (view == null) {
                holder = new SchemeProductHolder();
                view = mInflater.inflate(R.layout.row_scheme_product, parent,
                        false);

                holder.productNameTV = view
                        .findViewById(R.id.tv_product_name);
                holder.orderQuantityTV = view
                        .findViewById(R.id.tv_buying_qty);
                holder.schemeTV = view.findViewById(R.id.tv_scheme);

                holder.quantityRangeTV = view
                        .findViewById(R.id.tv_quantity_range);
                holder.priceRangeTV = view
                        .findViewById(R.id.tv_price_range);
                holder.amountRangeTV = view
                        .findViewById(R.id.tv_amount_range);
                holder.percentRangeTV = view
                        .findViewById(R.id.tv_percent_range);

                holder.quantityCB = view
                        .findViewById(R.id.cb_quantity);
                holder.priceCB = view.findViewById(R.id.cb_price);
                holder.amountCB = view.findViewById(R.id.cb_amount);
                holder.percentCB = view
                        .findViewById(R.id.cb_percent);

                holder.priceET = view
                        .findViewById(R.id.et_selected_price);
                holder.amountET = view
                        .findViewById(R.id.et_selected_amount);
                holder.percentET = view
                        .findViewById(R.id.et_selected_percent);

                holder.showFreeBTN = view
                        .findViewById(R.id.btn_show_free_products);

                holder.rateRL = view
                        .findViewById(R.id.priceLayout);
                holder.amountRL = view
                        .findViewById(R.id.amountLayout);
                holder.percentRL = view
                        .findViewById(R.id.percentLayout);
                holder.qtyRL = view
                        .findViewById(R.id.qtyLayout);
                holder.upArrow = view.findViewById(R.id.uparrow);
                holder.tv_label_qtytitle = view.findViewById(R.id.tv_qtytitle);
                holder.text_stock_availability = (TextView) view
                        .findViewById(R.id.text_stock_availability);
                holder.text_maxslab = view.findViewById(R.id.text_maxslab);
                //typeface
                holder.productNameTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.schemeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.orderQuantityTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.quantityRangeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.priceRangeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.amountRangeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.percentRangeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.showFreeBTN.setTypeface(bModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                holder.priceET.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.amountET.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.percentET.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_label_qtytitle.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.text_stock_availability.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

                ((TextView) view.findViewById(R.id.tv_qtytitle)).setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_pricetitle)).setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_amounttitle)).setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_percenttitle)).setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.showFreeBTN.setText(schemeViewTxt);

                holder.upArrow.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        schemeHelper.loadSchemePromotion(getApplicationContext(),
                                holder.schemeBO.getSchemeId(),
                                holder.schemeBO.getParentLogic(),
                                holder.schemeBO.getChannelId(),
                                holder.schemeBO.getSubChannelId(),
                                holder.productBO.getProductID(),
                                holder.schemeBO.getQuantity());
                        if (schemeHelper.getSchemePromotion() != null
                                && schemeHelper.getSchemePromotion()
                                .size() > 0) {
                            if (mSchemePromDialog == null) {
                                mSchemePromDialog = new NextSlabSchemeDialog(
                                        SchemeApply.this, false, null,
                                        SchemeApply.this);

                                mSchemePromDialog.setCancelable(false);
                            }
                            mSchemePromDialog.show();
                        } else {
                            Toast.makeText(SchemeApply.this,
                                    "No Better Promotional Scheme available",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                holder.showFreeBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        if (mSchemeDialog == null) {

                            mSchemeDialog = new SchemeFreeProductSelectionDialog(
                                    SchemeApply.this, holder.schemeBO,
                                    null);
                            if (mSchemeDialog.getWindow() != null)
                                mSchemeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            mSchemeDialog.show();
                            mSchemeDialog.setCancelable(false);

                            mSchemeDialog
                                    .setOnDismissListener(new OnDismissListener() {

                                        @Override
                                        public void onDismiss(
                                                DialogInterface dialog) {
                                            mSchemeDialog = null;
                                        }
                                    });
                        } else {
                            if (!mSchemeDialog.isShowing()) {
                                mSchemeDialog = new SchemeFreeProductSelectionDialog(
                                        SchemeApply.this, holder.schemeBO,
                                        null);
                                if (mSchemeDialog.getWindow() != null)
                                    mSchemeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                mSchemeDialog.show();
                                mSchemeDialog.setCancelable(false);
                                mSchemeDialog
                                        .setOnDismissListener(new OnDismissListener() {
                                            @Override
                                            public void onDismiss(
                                                    DialogInterface dialog) {

                                                mSchemeDialog = null;
                                            }
                                        });
                            }
                        }
                    }
                });

                holder.quantityCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {

                                QUANTITY = null;
                                if (isChecked) {

                                    if (bModel.configurationMasterHelper.IS_SIH_VALIDATION
                                            && !holder.schemeBO.isSihAvailableForFreeProducts()) {
                                        holder.text_stock_availability.setVisibility(View.VISIBLE);
                                        holder.quantityCB.setChecked(false);
                                        holder.showFreeBTN.setEnabled(false);
                                        holder.schemeBO
                                                .setQuantityTypeSelected(false);
                                        holder.showFreeBTN.setEnabled(true);
                                        return;
                                    } else {
                                        holder.text_stock_availability.setVisibility(View.GONE);

                                    }

                                    if (holder.schemeBO.isPriceTypeSeleted()) {
                                        holder.priceCB.setChecked(false);
                                        holder.schemeBO
                                                .setPriceTypeSeleted(false);
                                        holder.priceET.setText("");
                                    }
                                    if (holder.schemeBO.isAmountTypeSelected()) {
                                        holder.amountCB.setChecked(false);
                                        holder.schemeBO
                                                .setAmountTypeSelected(false);
                                        holder.amountET.setText("");
                                    }
                                    if (holder.schemeBO
                                            .isDiscountPrecentSelected()) {
                                        holder.percentCB.setChecked(false);
                                        holder.schemeBO
                                                .setDiscountPrecentSelected(false);
                                        holder.percentET.setText("");
                                    }

                                    holder.showFreeBTN.setEnabled(true);
                                    holder.priceET.setEnabled(false);
                                    holder.priceET.setClickable(false);
                                    holder.amountET.setEnabled(false);
                                    holder.amountET.setClickable(false);
                                    holder.percentET.setEnabled(false);
                                    holder.percentET.setClickable(false);

                                    holder.schemeBO
                                            .setQuantityTypeSelected(isChecked);
                                    holder.schemeBO.setChecked(isChecked);
                                } else {
                                    holder.showFreeBTN.setEnabled(false);
                                    holder.schemeBO
                                            .setQuantityTypeSelected(isChecked);
                                    if (holder.schemeBO.getGetType().equalsIgnoreCase("QTY"))
                                        holder.schemeBO.setChecked(isChecked);

                                }


                            }
                        });

                holder.priceCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {

                                QUANTITY = null;
                                if (isChecked) {

                                    holder.priceET.setText(String.valueOf(holder.schemeBO
                                            .getActualPrice()));

                                    if (holder.schemeBO
                                            .isQuantityTypeSelected()) {
                                        holder.quantityCB.setChecked(false);
                                        holder.schemeBO
                                                .setQuantityTypeSelected(false);
                                    }

                                    if (holder.schemeBO.isAmountTypeSelected()) {
                                        holder.amountCB.setChecked(false);
                                        holder.schemeBO
                                                .setAmountTypeSelected(false);
                                        holder.amountET.setText("");
                                    }
                                    if (holder.schemeBO
                                            .isDiscountPrecentSelected()) {
                                        holder.percentCB.setChecked(false);
                                        holder.schemeBO
                                                .setDiscountPrecentSelected(false);
                                        holder.percentET.setText("");
                                    }

                                    holder.showFreeBTN.setEnabled(false);
                                    holder.priceET.setEnabled(true);
                                    holder.priceET.setClickable(true);
                                    holder.amountET.setEnabled(false);
                                    holder.amountET.setClickable(false);
                                    holder.percentET.setEnabled(false);
                                    holder.percentET.setClickable(false);
                                    holder.schemeBO.setPriceTypeSeleted(isChecked);
                                    holder.schemeBO.setChecked(isChecked);

                                } else {
                                    holder.priceET.setText("");
                                    holder.priceET.setEnabled(false);
                                    holder.priceET.setClickable(false);
                                    holder.schemeBO.setPriceTypeSeleted(isChecked);
                                    if (holder.schemeBO.getGetType().equalsIgnoreCase("PRICE"))
                                        holder.schemeBO.setChecked(isChecked);

                                }
                                if (holder.schemeBO.getFreeProducts() != null && !holder.schemeBO
                                        .getFreeProducts().isEmpty()) {
                                    SchemeProductBO schemeProductBO = holder.schemeBO
                                            .getFreeProducts().get(0);
                                    if (schemeProductBO != null) {
                                        if (schemeProductBO.getPriceActual() == schemeProductBO
                                                .getPriceMaximum()) {
                                            holder.priceET.setEnabled(false);
                                            if (QUANTITY == holder.priceET)
                                                QUANTITY = null;
                                        }
                                    }
                                }
                                if (!schemeHelper.IS_SCHEME_EDITABLE) {
                                    holder.priceET.setEnabled(false);
                                }


                            }
                        });

                holder.amountCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {

                                QUANTITY = null;
                                if (isChecked) {

                                    holder.amountET.setText(String.valueOf(holder.schemeBO
                                            .getMinimumAmount()));

                                    if (holder.schemeBO
                                            .isQuantityTypeSelected()) {
                                        holder.quantityCB.setChecked(false);
                                        holder.schemeBO
                                                .setQuantityTypeSelected(false);
                                    }

                                    if (holder.schemeBO.isPriceTypeSeleted()) {
                                        holder.priceCB.setChecked(false);
                                        holder.schemeBO
                                                .setPriceTypeSeleted(false);
                                        holder.priceET.setText("");
                                    }
                                    if (holder.schemeBO
                                            .isDiscountPrecentSelected()) {
                                        holder.percentCB.setChecked(false);
                                        holder.schemeBO
                                                .setDiscountPrecentSelected(false);
                                        holder.percentET.setText("");
                                    }

                                    holder.showFreeBTN.setEnabled(false);
                                    holder.priceET.setEnabled(false);
                                    holder.priceET.setClickable(false);
                                    holder.amountET.setEnabled(true);
                                    holder.amountET.setClickable(true);
                                    holder.percentET.setEnabled(false);
                                    holder.percentET.setClickable(false);

                                    holder.schemeBO
                                            .setAmountTypeSelected(isChecked);
                                    holder.schemeBO.setChecked(isChecked);

                                } else {

                                    holder.amountET.setText("");
                                    holder.amountET.setEnabled(false);
                                    holder.amountET.setClickable(false);

                                    holder.schemeBO
                                            .setAmountTypeSelected(isChecked);
                                    if (holder.schemeBO.getGetType().equalsIgnoreCase("SV")
                                            || holder.schemeBO.getGetType().equalsIgnoreCase("VALUE"))
                                        holder.schemeBO.setChecked(isChecked);
                                }

                                if (holder.schemeBO.getFreeProducts() != null && !holder.schemeBO
                                        .getFreeProducts().isEmpty()) {

                                    SchemeProductBO schemeProductBO = holder.schemeBO.getFreeProducts().get(0);
                                    if (schemeProductBO != null) {
                                        if (schemeProductBO
                                                .getMaxAmountCalculated() == schemeProductBO
                                                .getMinAmountCalculated()) {
                                            holder.amountET.setEnabled(false);
                                            if (QUANTITY == holder.amountET)
                                                QUANTITY = null;

                                        }
                                    }
                                }
                                if (!schemeHelper.IS_SCHEME_EDITABLE) {
                                    holder.amountET.setEnabled(false);
                                }


                            }
                        });

                holder.percentCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {

                                QUANTITY = null;
                                if (isChecked) {

                                    holder.percentET.setText(String.valueOf(holder.schemeBO
                                            .getMinimumPrecent()));

                                    if (holder.schemeBO
                                            .isQuantityTypeSelected()) {
                                        holder.quantityCB.setChecked(false);
                                        holder.schemeBO
                                                .setQuantityTypeSelected(false);
                                    }

                                    if (holder.schemeBO.isPriceTypeSeleted()) {
                                        holder.priceCB.setChecked(false);
                                        holder.schemeBO
                                                .setPriceTypeSeleted(false);
                                        holder.priceET.setText("");
                                    }
                                    if (holder.schemeBO.isAmountTypeSelected()) {
                                        holder.amountCB.setChecked(false);
                                        holder.schemeBO
                                                .setAmountTypeSelected(false);
                                        holder.amountET.setText("");
                                    }

                                    holder.showFreeBTN.setEnabled(false);
                                    holder.priceET.setEnabled(false);
                                    holder.priceET.setClickable(false);
                                    holder.percentET.setEnabled(true);
                                    holder.percentET.setClickable(true);
                                    holder.amountET.setEnabled(false);
                                    holder.amountET.setClickable(false);

                                    holder.schemeBO
                                            .setDiscountPrecentSelected(isChecked);
                                    holder.schemeBO.setChecked(isChecked);

                                } else {
                                    holder.percentET.setText("");
                                    holder.percentET.setEnabled(false);
                                    holder.percentET.setClickable(false);
                                    holder.schemeBO
                                            .setDiscountPrecentSelected(isChecked);
                                    if (holder.schemeBO.getGetType().equalsIgnoreCase("PER"))
                                        holder.schemeBO.setChecked(isChecked);

                                }

                                if (holder.schemeBO.getFreeProducts() != null && !holder.schemeBO
                                        .getFreeProducts().isEmpty()) {
                                    SchemeProductBO schemeProductBO = holder.schemeBO
                                            .getFreeProducts().get(0);
                                    if (schemeProductBO.getMaxPrecentCalculated() == schemeProductBO
                                            .getMinPercentCalculated()) {
                                        holder.percentET.setEnabled(false);
                                        if (QUANTITY == holder.percentET) {
                                            QUANTITY = null;
                                        }
                                    }
                                }
                                if (!schemeHelper.IS_SCHEME_EDITABLE) {
                                    holder.percentET.setEnabled(false);
                                }

                            }
                        });

                holder.priceET.addTextChangedListener(new TextWatcher() {

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

                        double priceEntered = 0;
                        if (s != null) {
                            if (!s.toString().trim().equals("")
                                    && !s.toString().trim().equals(".")) {
                                if (s.toString().length() > 0)
                                    holder.priceET.setSelection(s.toString().length());
                                priceEntered = SDUtil.convertToDouble(s.toString());
                            }
                        }

                        holder.schemeBO.setSelectedPrice(priceEntered);

                    }
                });

                holder.amountET.addTextChangedListener(new TextWatcher() {

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

                        double amountEntered = 0;
                        if (s != null) {
                            if (!s.toString().trim().equals("")
                                    && !s.toString().trim().equals(".")) {
                                if (s.toString().length() > 0)
                                    holder.amountET.setSelection(s.toString().length());
                                amountEntered = SDUtil.convertToDouble(s.toString());
                            }
                        }

                        holder.schemeBO.setSelectedAmount(amountEntered);

                    }
                });

                holder.percentET.addTextChangedListener(new TextWatcher() {

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

                        double percentEntered = 0;
                        if (s != null) {
                            if (!s.toString().trim().equals("")
                                    && !s.toString().trim().equals(".")) {
                                if (s.toString().length() > 0)
                                    holder.percentET.setSelection(s.toString().length());

                                percentEntered = SDUtil.convertToDouble(s
                                        .toString());
                            }
                        }

                        holder.schemeBO.setSelectedPrecent(percentEntered);

                    }
                });

                holder.priceET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.priceET;
                        QUANTITY.setTag(holder.schemeBO);
                        int inType = holder.priceET.getInputType();
                        holder.priceET.setInputType(InputType.TYPE_NULL);
                        holder.priceET.onTouchEvent(event);
                        holder.priceET.setInputType(inType);
                        holder.priceET.requestFocus();
                        if (holder.priceET.getText().length() > 0)
                            holder.priceET.setSelection(holder.priceET.getText().length());
                        return true;

                    }
                });

                holder.percentET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.percentET;
                        QUANTITY.setTag(holder.schemeBO);
                        int inType = holder.percentET.getInputType();
                        holder.percentET.setInputType(InputType.TYPE_NULL);
                        holder.percentET.onTouchEvent(event);
                        holder.percentET.setInputType(inType);
                        holder.percentET.requestFocus();
                        if (holder.percentET.getText().length() > 0)
                            holder.percentET.setSelection(holder.percentET.getText().length());
                        return true;

                    }
                });

                holder.amountET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.amountET;
                        QUANTITY.setTag(holder.schemeBO);
                        int inType = holder.amountET.getInputType();
                        holder.amountET.setInputType(InputType.TYPE_NULL);
                        holder.amountET.onTouchEvent(event);
                        holder.amountET.setInputType(inType);
                        holder.amountET.requestFocus();
                        if (holder.amountET.getText().length() > 0)
                            holder.amountET.setSelection(holder.amountET.getText().length());
                        return true;

                    }
                });

                view.setTag(holder);
            } else {
                holder = (SchemeProductHolder) view.getTag();
            }


            holder.schemeBO = mSchemeDoneList.get(groupPosition);

            holder.productNameTV.setText(holder.schemeBO
                    .getProductName());
            holder.schemeTV.setText(holder.schemeBO.getScheme());
            holder.orderQuantityTV.setText("Case : "
                    + 0 + " Pcs : "
                    + 0 + " Outer : "
                    + 0);


            holder.priceET.setTag(holder.schemeBO);
            holder.amountET.setTag(holder.schemeBO);
            holder.percentET.setTag(holder.schemeBO);

            if (holder.schemeBO.getFreeProducts() != null && !holder.schemeBO.getFreeProducts().isEmpty()) {
                SchemeProductBO schemeProductBO = holder.schemeBO.getFreeProducts().get(0);
                holder.quantityRangeTV.setText("Max: "
                        + holder.schemeBO.getMaximumQuantity());
                holder.priceRangeTV.setText("Min: "
                        + SDUtil.roundIt(schemeProductBO.getPriceActual(), 2)
                        + " Max: "
                        + SDUtil.roundIt(schemeProductBO.getPriceMaximum(), 2));
                holder.amountRangeTV.setText("Min: "
                        + SDUtil.roundIt(
                        schemeProductBO.getMinAmountCalculated(), 2)
                        + " Max: "
                        + SDUtil.roundIt(
                        schemeProductBO.getMaxAmountCalculated(), 2));
                holder.percentRangeTV.setText("Min: "
                        + SDUtil.roundIt(
                        schemeProductBO.getMinPercentCalculated(), 2)
                        + " Max: "
                        + SDUtil.roundIt(
                        schemeProductBO.getMaxPrecentCalculated(), 2));

                holder.tv_label_qtytitle.setText(schemeProductBO.getUomDescription() + " "
                        + getResources().getString(R.string.qty));

                // set visibility for qty,percent,amount and price calculation
                if (schemeProductBO.getQuantityMaxiumCalculated() == 0
                        && schemeProductBO.getQuantityActualCalculated() == 0) {
                    holder.qtyRL.setVisibility(View.GONE);
                } else {
                    holder.qtyRL.setVisibility(View.VISIBLE);
                }
                if (schemeProductBO.getMaxPrecentCalculated() == 0
                        && schemeProductBO.getMinPercentCalculated() == 0) {
                    holder.percentRL.setVisibility(View.GONE);
                } else if (schemeProductBO.getMaxPrecentCalculated() == schemeProductBO
                        .getMinPercentCalculated()) {
                    holder.percentET.setClickable(false);
                    holder.percentET.setEnabled(false);
                    holder.percentET.setFocusable(false);
                    holder.percentRL.setVisibility(View.VISIBLE);
                    holder.percentET.setText(holder.schemeBO
                            .getMinimumPrecent() + "");
                    holder.percentRangeTV.setText(SDUtil.roundIt(
                            schemeProductBO.getMinPercentCalculated(), 2) + "");
                    if (QUANTITY == holder.percentET)
                        QUANTITY = null;

                    holder.percentRL.setVisibility(View.VISIBLE);

                    if (holder.schemeBO.getMaximumSlab() != 0) {
                        double totalPercentageDiscount = 0.0;
                        ProductMasterBO productBO = bModel.productHelper.getProductMasterBOById(holder.schemeBO.getSkuBuyProdID());
                        if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && bModel.configurationMasterHelper.IS_SIH_VALIDATION
                                && bModel.configurationMasterHelper.IS_INVOICE) {

                            if (productBO
                                    .getBatchwiseProductCount() > 0) {
                                totalPercentageDiscount = schemeHelper
                                        .calculateDiscountValue(
                                                productBO,
                                                holder.schemeBO.getSelectedPrecent(),
                                                "SCH_PER", true);
                            } else {
                                totalPercentageDiscount = schemeHelper
                                        .calculateDiscountValue(
                                                productBO,
                                                holder.schemeBO.getSelectedPrecent(),
                                                "SCH_PER",
                                                false);
                            }
                        } else {
                            totalPercentageDiscount = schemeHelper
                                    .calculateDiscountValue(
                                            productBO,
                                            holder.schemeBO.getSelectedPrecent(),
                                            "SCH_PER", false);
                        }

                        if (totalPercentageDiscount > holder.schemeBO.getMaximumSlab()) {
                            holder.text_maxslab.setVisibility(View.VISIBLE);
                            holder.text_maxslab.setText("Scheme Amt : " + SDUtil.roundIt(
                                    totalPercentageDiscount, 2)
                                    + " Maximum Slab Amt : " + holder.schemeBO.getMaximumSlab());
                        }
                    }
                } else {
                    holder.percentRL.setVisibility(View.VISIBLE);
                }
                if (schemeProductBO.getMinAmountCalculated() == 0
                        && schemeProductBO.getMaxAmountCalculated() == 0) {
                    holder.amountRL.setVisibility(View.GONE);
                } else if (schemeProductBO.getMaxAmountCalculated() == schemeProductBO
                        .getMinAmountCalculated()) {
                    holder.amountET.setClickable(false);
                    holder.amountET.setEnabled(false);
                    holder.amountET.setFocusable(false);
                    holder.amountRL.setVisibility(View.VISIBLE);
                    holder.amountET.setText(holder.schemeBO
                            .getMinimumAmount() + "");
                    holder.amountRangeTV.setText(SDUtil.roundIt(
                            schemeProductBO.getMinAmountCalculated(), 2)
                            + "");
                    if (QUANTITY == holder.amountET)
                        QUANTITY = null;
                    holder.amountRL.setVisibility(View.VISIBLE);
                    if(holder.schemeBO.getMaximumSlab() != 0 && holder.schemeBO.getSelectedAmount() > holder.schemeBO.getMaximumSlab()){
                        holder.text_maxslab.setVisibility(View.VISIBLE);
                        holder.text_maxslab.setText("Scheme Amt : " + holder.schemeBO
                                .getMinimumAmount() +" Maximum Slab Amt : " + holder.schemeBO.getMaximumSlab() );
                        holder.amountET.setText(holder.schemeBO.getMaximumSlab() + "");
                        holder.schemeBO.setSelectedAmount(holder.schemeBO.getMaximumSlab());
                        if (holder.schemeBO.getMinimumAmount() > holder.schemeBO.getSelectedAmount()) {
                            holder.schemeBO.setMinimumAmount(holder.schemeBO.getSelectedAmount());
                            holder.schemeBO.setMaximumAmount(holder.schemeBO.getSelectedAmount());
                        } else if (holder.schemeBO.getMaximumAmount() > holder.schemeBO.getSelectedAmount()) {
                            holder.schemeBO.setMaximumAmount(holder.schemeBO.getSelectedAmount());
                        }
                    }
                } else {
                    holder.amountRL.setVisibility(View.VISIBLE);
                }
                if (schemeProductBO.getPriceActual() == 0
                        && schemeProductBO.getPriceMaximum() == 0) {
                    holder.rateRL.setVisibility(View.GONE);
                } else if (schemeProductBO.getPriceActual() == schemeProductBO
                        .getPriceMaximum()) {
                    holder.priceET.setEnabled(false);
                    holder.priceET.setClickable(false);
                    holder.priceET.setFocusable(false);
                    holder.rateRL.setVisibility(View.VISIBLE);
                    holder.priceET.setText(holder.schemeBO
                            .getActualPrice() + "");
                    holder.priceRangeTV.setText(SDUtil.roundIt(
                            schemeProductBO.getPriceActual(), 2)
                            + "");
                    if (QUANTITY == holder.percentET)
                        QUANTITY = null;
                    holder.rateRL.setVisibility(View.VISIBLE);
                } else {
                    holder.rateRL.setVisibility(View.VISIBLE);
                }

            }


            if (holder.schemeBO.isQuantityTypeSelected()) {
                if (schemeHelper.IS_SCHEME_CHECK
                        && schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.quantityCB.setChecked(true);
                    holder.quantityCB.setEnabled(false);
                } else if (schemeHelper.IS_SCHEME_CHECK && !holder.schemeBO.isChecked()) {
                    holder.quantityCB.setChecked(false);
                    holder.schemeBO.setChecked(false);
                    holder.schemeBO.setQuantityTypeSelected(false);
                } else {
                    holder.quantityCB.setChecked(true);
                    holder.schemeBO.setChecked(true);
                    holder.schemeBO.setQuantityTypeSelected(true);
                }
                if (holder.schemeBO.getIsOnInvoice() == 0
                        || schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.quantityCB.setEnabled(false);
                } else {
                    holder.quantityCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.quantityCB.setChecked(true);
                    holder.quantityCB.setEnabled(false);
                } else {
                    holder.quantityCB.setEnabled(true);
                }

                holder.showFreeBTN.setEnabled(true);

                holder.priceCB.setChecked(false);
                holder.priceET.setEnabled(false);
                holder.priceET.setClickable(false);

                holder.amountCB.setChecked(false);
                holder.amountET.setEnabled(false);
                holder.amountET.setClickable(false);

                holder.percentCB.setChecked(false);
                holder.percentET.setEnabled(false);
                holder.percentET.setClickable(false);

            } else if (holder.schemeBO.isPriceTypeSeleted()) {

                holder.quantityCB.setChecked(false);
                holder.showFreeBTN.setEnabled(false);

                if (schemeHelper.IS_SCHEME_CHECK
                        && schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.priceCB.setChecked(true);
                    holder.priceCB.setEnabled(false);
                } else if (schemeHelper.IS_SCHEME_CHECK && !holder.schemeBO.isChecked()) {
                    holder.priceCB.setChecked(false);
                    holder.schemeBO.setPriceTypeSeleted(false);
                    holder.schemeBO.setChecked(false);
                    holder.priceET.setText("");
                } else {
                    holder.priceCB.setChecked(true);
                    holder.schemeBO.setChecked(true);
                    holder.schemeBO.setPriceTypeSeleted(true);
                }
                if (holder.schemeBO.getIsOnInvoice() == 0
                        || schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.priceCB.setEnabled(false);
                } else {
                    holder.priceCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.priceCB.setChecked(true);
                    holder.priceCB.setEnabled(false);
                } else {
                    holder.priceCB.setEnabled(true);
                }

                holder.priceET.setEnabled(true);
                holder.priceET.setClickable(true);

                holder.amountCB.setChecked(false);
                holder.amountET.setEnabled(false);
                holder.amountET.setClickable(false);

                holder.percentCB.setChecked(false);
                holder.percentET.setEnabled(false);
                holder.percentET.setClickable(false);

            } else if (holder.schemeBO.isAmountTypeSelected()) {

                holder.quantityCB.setChecked(false);
                holder.showFreeBTN.setEnabled(false);

                holder.priceCB.setChecked(false);
                holder.priceET.setEnabled(false);
                holder.priceET.setClickable(false);

                if (schemeHelper.IS_SCHEME_CHECK
                        && schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.amountCB.setChecked(true);
                    holder.amountCB.setEnabled(false);
                } else if (schemeHelper.IS_SCHEME_CHECK && !holder.schemeBO.isChecked()) {
                    holder.amountCB.setChecked(false);
                    holder.amountCB.setChecked(false);
                    holder.schemeBO.setAmountTypeSelected(false);
                    holder.amountET.setText("");
                } else {
                    holder.amountCB.setChecked(true);
                    holder.schemeBO.setChecked(true);
                    holder.schemeBO.setAmountTypeSelected(true);
                }

                if (holder.schemeBO.getIsOnInvoice() == 0
                        || schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.amountCB.setEnabled(false);
                } else {
                    holder.amountCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.amountCB.setChecked(true);
                    holder.amountCB.setEnabled(false);
                } else {
                    holder.amountCB.setEnabled(true);
                }

                holder.amountET.setEnabled(true);
                holder.amountET.setClickable(true);

                holder.percentCB.setChecked(false);
                holder.percentET.setEnabled(false);
                holder.percentET.setClickable(false);

            } else if (holder.schemeBO
                    .isDiscountPrecentSelected()) {

                holder.quantityCB.setChecked(false);
                holder.showFreeBTN.setEnabled(false);

                holder.priceCB.setChecked(false);
                holder.priceET.setEnabled(false);
                holder.priceET.setClickable(false);

                holder.amountCB.setChecked(false);
                holder.amountET.setEnabled(false);
                holder.amountET.setClickable(false);

                if (schemeHelper.IS_SCHEME_CHECK
                        && schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.percentCB.setChecked(true);
                    holder.percentCB.setEnabled(false);
                } else if (schemeHelper.IS_SCHEME_CHECK && !holder.schemeBO.isChecked()) {
                    holder.percentCB.setChecked(false);
                    holder.schemeBO.setDiscountPrecentSelected(false);
                    holder.schemeBO.setChecked(false);
                    holder.percentET.setText("");
                } else {
                    holder.percentCB.setChecked(true);
                    holder.schemeBO.setDiscountPrecentSelected(true);
                    holder.schemeBO.setChecked(true);
                }

                if (holder.schemeBO.getIsOnInvoice() == 0
                        || schemeHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.percentCB.setEnabled(false);
                } else {
                    holder.percentCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.percentCB.setChecked(true);
                    holder.percentCB.setEnabled(false);
                } else {
                    holder.percentCB.setEnabled(true);
                }

                holder.percentET.setEnabled(true);
                holder.percentET.setClickable(true);

            } else {
                // this condition used for scrolling issue  view should be disable while scrolling
                if (holder.schemeBO.getIsAutoApply() == 0) {
                    holder.quantityCB.setEnabled(true);
                    holder.amountCB.setEnabled(true);
                    holder.percentCB.setEnabled(true);
                    holder.priceCB.setEnabled(true);
                } else {
                    holder.quantityCB.setEnabled(false);
                    holder.amountCB.setEnabled(false);
                    holder.percentCB.setEnabled(false);
                    holder.priceCB.setEnabled(false);
                }

                holder.quantityCB.setChecked(false);
                holder.showFreeBTN.setEnabled(false);

                holder.priceCB.setChecked(false);
                holder.priceET.setEnabled(false);
                holder.priceET.setClickable(false);

                holder.amountCB.setChecked(false);
                holder.amountET.setEnabled(false);
                holder.amountET.setClickable(false);

                holder.percentCB.setChecked(false);
                holder.percentET.setEnabled(false);
                holder.percentET.setClickable(false);
            }

            if (!schemeHelper.IS_SCHEME_EDITABLE) {

                holder.priceET.setEnabled(false);
                holder.amountET.setEnabled(false);
                holder.percentET.setEnabled(false);
            } else {
                holder.priceET.setEnabled(true);
                holder.amountET.setEnabled(true);
                holder.percentET.setEnabled(true);
            }

            if (bModel.configurationMasterHelper.IS_SIH_VALIDATION
                    && !holder.schemeBO.isSihAvailableForFreeProducts()
                    && holder.schemeBO.isQuantityTypeSelected()) {
                holder.quantityCB.setChecked(false);
                holder.schemeBO
                        .setQuantityTypeSelected(false);
                holder.showFreeBTN.setEnabled(true);
                holder.text_stock_availability.setVisibility(View.VISIBLE);
            } else {
                holder.text_stock_availability.setVisibility(View.GONE);
            }


            return view;
        }

        @Override

        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }


    }


    private class SchemeProductHolder {
        private ProductMasterBO productBO;
        private SchemeBO schemeBO;
        // Info
        private TextView productNameTV;
        private TextView schemeTV;
        private TextView orderQuantityTV, tv_label_qtytitle;
        ;
        // Range
        private TextView quantityRangeTV, percentRangeTV;
        private TextView priceRangeTV, amountRangeTV, text_stock_availability, text_maxslab;
        // Entry
        private EditText priceET, amountET, percentET;
        // CheckBox
        private CheckBox quantityCB, percentCB;
        private CheckBox priceCB, amountCB;
        // Button
        private Button showFreeBTN, upArrow;
        private RelativeLayout rateRL, percentRL, amountRL, qtyRL;
    }


    /**
     * Show alert dialog
     *
     * @param message Message to show in dialog
     */
    private void showAlert(String message, int id) {

        switch (id) {
            case 0:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getResources().getString(R.string.scheme_apply));
                dialog.setMessage(message);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                bModel.applyAlertDialogTheme(dialog);
                break;

            case 1:
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
                dialog1.setTitle(getResources().getString(R.string.Scheme_apply));
                dialog1.setMessage(message);
                dialog1.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        schemeHelper.clearOffInvoiceSchemeList();
                        click(2);
                        dialog.dismiss();
                    }
                });
                dialog1.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                bModel.applyAlertDialogTheme(dialog1);
                break;

        }
    }

    public void eff() {

        String s = QUANTITY.getText().toString();
        int maxLength = 5;

        if (QUANTITY.getText().toString().contains(".")) {
            maxLength = 8;

        }

        if (s.length() < maxLength) {
            if (!s.equals("0") && !s.equals("0.0"))
                QUANTITY.setText(QUANTITY.getText() + append);
            else
                QUANTITY.setText(append);
        }

    }

    public void numberPressed(View vw) {

        if (QUANTITY == null) {

            showAlert(getResources().getString(R.string.please_select_item), 0);

        } else {
            int id = vw.getId();
            if (id == R.id.calcone) {
                append = "1";
                eff();
            } else if (id == R.id.calctwo) {
                append = "2";
                eff();
            } else if (id == R.id.calcthree) {
                append = "3";
                eff();
            } else if (id == R.id.calcfour) {
                append = "4";
                eff();
            } else if (id == R.id.calcfive) {
                append = "5";
                eff();
            } else if (id == R.id.calcsix) {
                append = "6";
                eff();
            } else if (id == R.id.calcseven) {
                append = "7";
                eff();
            } else if (id == R.id.calceight) {
                append = "8";
                eff();
            } else if (id == R.id.calcnine) {
                append = "9";
                eff();
            } else if (id == R.id.calczero) {
                append = "0";
                eff();
            } else if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "0";
                    }
                }
                QUANTITY.setText(s);

            } else if (id == R.id.calcdot) {


                if (!QUANTITY.getText().toString().contains(".")) {
                    append = ".";
                    eff();

                } else {
                    append = "";
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}