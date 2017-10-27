package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.CS_sale_summary;
import com.ivy.countersales.CSsale;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

public class SchemeApply extends IvyBaseActivityNoActionBar {
    private static final String TAG = "Scheme Apply";


    private List<ProductMasterBO> mOrderedSchemeProducts;


    /**
     * Called when the activity is first created.
     */
    private ExpandableListView mExpandableLV;
    private BusinessModel bmodel;

    private boolean isClick;

    private EditText QUANTITY;

    private String append = "";
    private String screenCode = "MENU_STK_ORD";
    private SchemeExpandapleAdapterNew mExpandableAdapterNew;
    // ArrayList used to store scheme achieved list
    private ArrayList<SchemeBO> mSchemeDoneList;
    private Toolbar toolbar;
    private Button btnNext;
    private String fromOrderScreen = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.apply_scheme);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        // Set title to toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle(getResources().getString(R.string.Scheme_apply));
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
                fromOrderScreen = extras.getString("ForScheme", "STD_ORDER");
            }
        }
        mExpandableLV = (ExpandableListView) findViewById(R.id.elv);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnNext.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((Button) findViewById(R.id.calcdot)).setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                bmodel.schemeDetailsMasterHelper.clearOffInvoiceSchemeList();
                click(2);
            }
        });

        if (fromOrderScreen.equalsIgnoreCase("MENU_STK_ORD")) {
            updateSchemeDetails();
        } else {
            mSchemeDoneList = bmodel.schemeDetailsMasterHelper.getAppliedSchemeList();
            if (mSchemeDoneList.size() > 0) {
                mExpandableAdapterNew = new SchemeExpandapleAdapterNew();
                mExpandableLV.setAdapter(mExpandableAdapterNew);
            } else {
                return;
            }
        }

        // updateOrder();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        updateSchemeDetails();

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_only_next, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_next).setVisible(false);
        if (screenCode.equalsIgnoreCase("CSale")) {
            menu.findItem(R.id.menu_counter_remark).setVisible(true);
        }
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == android.R.id.home) {
            click(1);
            return true;
        } else if (i == R.id.menu_next) {
            bmodel.schemeDetailsMasterHelper.clearOffInvoiceSchemeList();
            click(2);
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
                // Intent returnIntent = new Intent();
                // setResult(RESULT_CANCELED, returnIntent);
                // super.onDestroy();
                if (screenCode.equalsIgnoreCase("CSale")) {
                    Intent i = new Intent(this,
                            CSsale.class);
                    startActivity(i);
                    finish();
                } else if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(SchemeApply.this,
                            CrownReturnActivity.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
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

                if (!isRangeWiseSchemeValidateDone()) {


                    return;
                }
                if (screenCode.equalsIgnoreCase("CSale")) {
                    Intent i = new Intent(this,
                            CS_sale_summary.class);
                    i.putExtra("refid", getIntent().getStringExtra("refid"));
                    i.putExtra("isFromSale", true);
                    i.putExtra("finalValue", getIntent().getDoubleExtra("finalValue", 0));
                    startActivity(i);
                } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                    Intent init = new Intent(SchemeApply.this,
                            OrderDiscount.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);
//                    finish();
                } else if (bmodel.configurationMasterHelper.IS_INITIATIVE) {

                    Intent init = new Intent(SchemeApply.this,
                            InitiativeActivity.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);
//                    finish();
                } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                    Intent i = new Intent(SchemeApply.this,
                            DigitalContentDisplay.class);
                    i.putExtra("FromInit", "Initiative");
                    i.putExtra("ScreenCode", screenCode);
                    startActivity(i);
//                    finish();
                } else {
                    Intent i = new Intent(SchemeApply.this, OrderSummary.class);
                    i.putExtra("ScreenCode", screenCode);
                    startActivity(i);
//                    finish();
                }
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
                // Intent i = new Intent(SchemeApply.this, OrderSummary.class);
                // startActivityForResult(i, 0);

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


    class SchemeExpandapleAdapterNew extends BaseExpandableListAdapter {

        LayoutInflater mInflater;

        public SchemeExpandapleAdapterNew() {
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

                holder.productNameTV = (TextView) view
                        .findViewById(R.id.tv_product_name);
                holder.orderQuantityTV = (TextView) view
                        .findViewById(R.id.tv_buying_qty);
                holder.schemeTV = (TextView) view.findViewById(R.id.tv_scheme);

                holder.quantityRangeTV = (TextView) view
                        .findViewById(R.id.tv_quantity_range);
                holder.priceRangeTV = (TextView) view
                        .findViewById(R.id.tv_price_range);
                holder.amountRangeTV = (TextView) view
                        .findViewById(R.id.tv_amount_range);
                holder.percentRangeTV = (TextView) view
                        .findViewById(R.id.tv_percent_range);

                holder.quantityCB = (CheckBox) view
                        .findViewById(R.id.cb_quantity);
                holder.priceCB = (CheckBox) view.findViewById(R.id.cb_price);
                holder.amountCB = (CheckBox) view.findViewById(R.id.cb_amount);
                holder.percentCB = (CheckBox) view
                        .findViewById(R.id.cb_percent);

                holder.priceET = (EditText) view
                        .findViewById(R.id.et_selected_price);
                holder.amountET = (EditText) view
                        .findViewById(R.id.et_selected_amount);
                holder.percentET = (EditText) view
                        .findViewById(R.id.et_selected_percent);

                holder.showFreeBTN = (Button) view
                        .findViewById(R.id.btn_show_free_products);

                holder.rateRL = (RelativeLayout) view
                        .findViewById(R.id.priceLayout);
                holder.amountRL = (RelativeLayout) view
                        .findViewById(R.id.amountLayout);
                holder.percentRL = (RelativeLayout) view
                        .findViewById(R.id.percentLayout);
                holder.qtyRL = (RelativeLayout) view
                        .findViewById(R.id.qtyLayout);
                holder.upArrow = (Button) view.findViewById(R.id.uparrow);

                //typeface
                holder.productNameTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.schemeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.orderQuantityTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.quantityRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.priceRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.amountRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.percentRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.showFreeBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                holder.priceET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.amountET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.percentET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                ((TextView) view.findViewById(R.id.tv_qtytitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_pricetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_amounttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) view.findViewById(R.id.tv_percenttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.upArrow.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        bmodel.promotionHelper.loadSchemePromotion(
                                holder.schemeBO.getSchemeId(),
                                holder.schemeBO.getType(),
                                holder.schemeBO.getChannelId(),
                                holder.schemeBO.getSubChannelId(),
                                holder.productBO.getProductID(),
                                holder.schemeBO.getQuantity());
                        if (bmodel.promotionHelper.getmSchemePromotion() != null
                                && bmodel.promotionHelper.getmSchemePromotion()
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
                        mCanShowSchemeDialog = true;

                        if (mCanShowSchemeDialog) {

                            if (mSchemeDialog == null) {

                                mSchemeDialog = new SchemeFreePorductSelectionDialog(
                                        SchemeApply.this, holder.schemeBO,
                                        null, screenCode);
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
                                    mSchemeDialog = new SchemeFreePorductSelectionDialog(
                                            SchemeApply.this, holder.schemeBO,
                                            null, screenCode);
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
                    }
                });

                holder.quantityCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
//								showFullName(holder.productBO.getProductName());

                                QUANTITY = null;
                                if (isChecked) {

									/*
                                     * if (!isValidatePriceEntered()) {
									 * holder.quantityCB.setChecked(false);
									 * holder.showFreeBTN.setEnabled(false);
									 * return; }
									 */

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
                                } else {
                                    holder.showFreeBTN.setEnabled(false);

                                }

                                holder.schemeBO
                                        .setQuantityTypeSelected(isChecked);
                            }
                        });

                holder.priceCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
//								showFullName(holder.productBO.getProductName());

								/*
                                 * if (!isValidatePriceEntered()) {
								 * holder.priceCB.setChecked(!isChecked);
								 * return; }
								 */
                                QUANTITY = null;
                                if (isChecked) {

                                    holder.priceET.setText(holder.schemeBO
                                            .getActualPrice() + "");

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

                                } else {
                                    holder.priceET.setText("");
                                    holder.priceET.setEnabled(false);
                                    holder.priceET.setClickable(false);

                                }
                                //  if(holder.schemeBO.getIsFreeCombination()==1) {
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
                                if (!bmodel.configurationMasterHelper.IS_SCHEME_EDITABLE) {
                                    holder.priceET.setEnabled(false);
                                }

                                holder.schemeBO.setPriceTypeSeleted(isChecked);
                                //  }
                                // mExpandableAdapter.notifyDataSetChanged();
                            }
                        });

                holder.amountCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
//								showFullName(holder.productBO.getProductName());

								/*
                                 * if (!isValidatePriceEntered()) {
								 * holder.amountCB.setChecked(!isChecked);
								 * return; }
								 */
                                QUANTITY = null;
                                if (isChecked) {

                                    holder.amountET.setText(holder.schemeBO
                                            .getMinimumAmount() + "");

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

                                } else {

                                    holder.amountET.setText("");
                                    holder.amountET.setEnabled(false);
                                    holder.amountET.setClickable(false);
                                }
                                //   if(holder.schemeBO.getIsFreeCombination()==1) {
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
                                if (!bmodel.configurationMasterHelper.IS_SCHEME_EDITABLE) {
                                    holder.amountET.setEnabled(false);
                                }

                                holder.schemeBO
                                        .setAmountTypeSelected(isChecked);
                                // mExpandableAdapter.notifyDataSetChanged();
                                //   }
                            }
                        });

                holder.percentCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
//								showFullName(holder.productBO.getProductName());

								/*
                                 * if (!isValidatePriceEntered()) {
								 * holder.percentCB.setChecked(!isChecked);
								 * return; }
								 */
                                QUANTITY = null;
                                if (isChecked) {

                                    holder.percentET.setText(holder.schemeBO
                                            .getMinimumPrecent() + "");

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

                                } else {

                                    holder.percentET.setText("");
                                    holder.percentET.setEnabled(false);
                                    holder.percentET.setClickable(false);
                                }
                                //   if(holder.schemeBO.getIsFreeCombination()==1) {
                                SchemeProductBO schemeProductBO = holder.schemeBO
                                        .getFreeProducts().get(0);
                                if (schemeProductBO.getMaxPrecentCalculated() == schemeProductBO
                                        .getMinPercentCalculated()) {
                                    holder.percentET.setEnabled(false);
                                    if (QUANTITY == holder.percentET) {
                                        QUANTITY = null;
                                    }
                                }
                                if (!bmodel.configurationMasterHelper.IS_SCHEME_EDITABLE) {
                                    holder.percentET.setEnabled(false);
                                }
                                holder.schemeBO
                                        .setDiscountPrecentSelected(isChecked);
                                // mExpandableAdapter.notifyDataSetChanged();
                                //   }
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
                                priceEntered = Double.parseDouble(s.toString());
//                                Log.e("PriceValue",groupPosition+s.toString().trim());
                                holder.schemeBO.setActualPrice(Double.parseDouble(s.toString().trim()));
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
                                amountEntered = Double.parseDouble(s.toString());
                                //  Log.e("AmountValue",s.toString().trim());
                                holder.schemeBO.setMinimumAmount(Double.parseDouble(s.toString().trim()));
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

                        double precentEntered = 0;
                        if (s != null) {
                            if (!s.toString().trim().equals("")
                                    && !s.toString().trim().equals(".")) {
                                precentEntered = Double.parseDouble(s
                                        .toString());
                                holder.schemeBO.setMinimumPrecent(Double.parseDouble(s.toString().trim()));
                                //  Log.e("PercentValue",s.toString().trim());
                            }
                        }

                        holder.schemeBO.setSelectedPrecent(precentEntered);

                    }
                });

                holder.priceET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.priceET;
                        int inType = holder.priceET.getInputType();
                        holder.priceET.setInputType(InputType.TYPE_NULL);
                        holder.priceET.onTouchEvent(event);
                        holder.priceET.setInputType(inType);
                        holder.priceET.selectAll();
                        holder.priceET.requestFocus();
                        return true;

                    }
                });

                holder.percentET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.percentET;
                        int inType = holder.percentET.getInputType();
                        holder.percentET.setInputType(InputType.TYPE_NULL);
                        holder.percentET.onTouchEvent(event);
                        holder.percentET.setInputType(inType);
                        holder.percentET.selectAll();
                        holder.percentET.requestFocus();
                        return true;

                    }
                });

                holder.amountET.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.amountET;
                        int inType = holder.amountET.getInputType();
                        holder.amountET.setInputType(InputType.TYPE_NULL);
                        holder.amountET.onTouchEvent(event);
                        holder.amountET.setInputType(inType);
                        holder.amountET.selectAll();
                        holder.amountET.requestFocus();
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

            // holder.priceET.setText(SDUtil.roundIt(
            // holder.schemeBO.getSelectedPrice(), 2));

            holder.priceET.setTag(holder.schemeBO);
            holder.amountET.setTag(holder.schemeBO);
            holder.percentET.setTag(holder.schemeBO);

            if (holder.schemeBO.getFreeProducts() != null) {
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

            // holder.showFreeBTN.setText("Selected\n" +
            // holder.schemeBO.getSelectedFreeProductsQuantity(holder.productBO.getProductID()));

            if (holder.schemeBO.isQuantityTypeSelected()) {
                if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK
                        && bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.quantityCB.setChecked(true);
                    holder.quantityCB.setEnabled(false);
                } else if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK) {
                    holder.quantityCB.setChecked(false);
                } else {
                    holder.quantityCB.setChecked(true);
                }
                if (holder.schemeBO.getIsOnInvoice() == 0
                        || bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.quantityCB.setEnabled(false);
                } else {
                    holder.quantityCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.quantityCB.setChecked(true);
                    holder.quantityCB.setEnabled(false);
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

                if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK
                        && bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.priceCB.setChecked(true);
                    holder.priceCB.setEnabled(false);
                } else if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK) {
                    holder.priceCB.setChecked(false);
                } else {
                    holder.priceCB.setChecked(true);
                }
                if (holder.schemeBO.getIsOnInvoice() == 0
                        || bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.priceCB.setEnabled(false);
                } else {
                    holder.priceCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.priceCB.setChecked(true);
                    holder.priceCB.setEnabled(false);
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

                if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK
                        && bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.amountCB.setChecked(true);
                    holder.amountCB.setEnabled(false);
                } else if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK) {
                    holder.amountCB.setChecked(false);
                } else {
                    holder.amountCB.setChecked(true);
                }

                if (holder.schemeBO.getIsOnInvoice() == 0
                        || bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.amountCB.setEnabled(false);
                } else {
                    holder.amountCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.amountCB.setChecked(true);
                    holder.amountCB.setEnabled(false);
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

                if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK
                        && bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.percentCB.setChecked(true);
                    holder.percentCB.setEnabled(false);
                } else if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK) {
                    holder.percentCB.setChecked(false);
                } else {
                    holder.percentCB.setChecked(true);
                }
                if (holder.schemeBO.getIsOnInvoice() == 0
                        || bmodel.configurationMasterHelper.IS_SCHEME_CHECK_DISABLED) {
                    holder.percentCB.setEnabled(false);
                } else {
                    holder.percentCB.setEnabled(true);
                }

                if (holder.schemeBO.getIsAutoApply() == 1) {
                    holder.percentCB.setChecked(true);
                    holder.percentCB.setEnabled(false);
                }
                holder.percentET.setEnabled(true);
                holder.percentET.setClickable(true);

            } else {
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

           /* if (bmodel.configurationMasterHelper.IS_SCHEME_CHECK) {
                holder.quantityCB.setChecked(false);
                holder.percentCB.setChecked(false);
                holder.amountCB.setChecked(false);
                holder.priceCB.setChecked(false);
            }*/
            if (!bmodel.configurationMasterHelper.IS_SCHEME_EDITABLE) {

                holder.priceET.setEnabled(false);
                holder.amountET.setEnabled(false);
                holder.percentET.setEnabled(false);
            } else {
                holder.priceET.setEnabled(true);
                holder.amountET.setEnabled(true);
                holder.percentET.setEnabled(true);
            }


            // holder.priceTV.setText("");

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


    class SchemeProductHolder {
        private ProductMasterBO productBO;
        private SchemeBO schemeBO;
        // Info
        private TextView productNameTV;
        private TextView schemeTV;
        private TextView orderQuantityTV;
        // Range
        private TextView quantityRangeTV, percentRangeTV;
        private TextView priceRangeTV, amountRangeTV;
        // Entry
        private EditText priceET, amountET, percentET;
        // CheckBox
        private CheckBox quantityCB, percentCB;
        private CheckBox priceCB, amountCB;
        // Button
        private Button showFreeBTN, upArrow;
        private RelativeLayout rateRL, percentRL, amountRL, qtyRL;
    }

    private SchemeFreePorductSelectionDialog mSchemeDialog;
    private boolean mCanShowSchemeDialog = false;


    private void showAlert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Scheme Apply");
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mExpandableAdapterNew = new SchemeExpandapleAdapterNew();
                mExpandableLV.setAdapter(mExpandableAdapterNew);
                if (mOrderedSchemeProducts != null) {
                    int size = mOrderedSchemeProducts.size();
                    Commons.print("SchemeApply" + ",mOrderedSchemeProducts.size() "
                            + mOrderedSchemeProducts.size());
                    for (int i = 0; i < size; i++) {
                        mExpandableLV.expandGroup(i);
                    }

                }
                dialog.dismiss();


            }
        });

        bmodel.applyAlertDialogTheme(dialog);
    }

    public void eff() {
        // String s = (String) QUANTITY.getText().toString();
        // if (!s.equals("0")) {
        // QUANTITY.setText(QUANTITY.getText() + append);
        // } else
        // QUANTITY.setText(append);

        String s = (String) QUANTITY.getText().toString();
        int maxLength = 5;

        // if (QUANTITY.getInputType() == InputType.TYPE_NUMBER_FLAG_DECIMAL) {
        if (QUANTITY.getText().toString().contains(".")) {
            maxLength = 8;

        }
        // }

        if (s.length() < maxLength) {
            if (!s.equals("0") && !s.equals("0.0"))
                QUANTITY.setText(QUANTITY.getText() + append);
            else
                QUANTITY.setText(append);
        }

    }

    public void numberPressed(View vw) {

        if (QUANTITY == null) {

            showAlert(getResources().getString(R.string.please_select_item));

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
                int length = QUANTITY.getText().length();
                if (length > 1)
                    QUANTITY.setText(QUANTITY.getText().subSequence(0,
                            length - 1));
                else
                    QUANTITY.setText("0");

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
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    /**
     * Method to use correctly range applied between min and max value for
     * scheme products
     *
     * @return true if all value should be in range,else false
     */
    private boolean isRangeWiseSchemeValidateDone() {
        for (SchemeBO schemeBO : mSchemeDoneList) {

            if (schemeBO != null) {

                if (schemeBO.isPriceTypeSeleted()) {
                    if (!isValidateSchemePriceEntered(schemeBO)) {
                        return false;
                    }
                } else if (schemeBO.isAmountTypeSelected()) {
                    if (!isValidateSchemeAmountEntered(schemeBO)) {
                        return false;
                    }

                } else if (schemeBO.isDiscountPrecentSelected()) {
                    if (!isValidateSchemeDiscountEntered(schemeBO)) {
                        return false;
                    }
                }


            }

        }
        return true;

    }

    /**
     * @param schemeBO ,this is free product schemeBO
     * @return if scheme entered price between min and max price return
     * true,else false
     */
    private boolean isValidateSchemePriceEntered(SchemeBO schemeBO) {

        if (!(Double.parseDouble(SDUtil.format(schemeBO.getSelectedPrice(), 0, 2)) >= Double.parseDouble(SDUtil.format(schemeBO.getActualPrice(), 0, 2))
                && Double.parseDouble(SDUtil.format(schemeBO.getSelectedPrice(), 0, 2)) <= Double.parseDouble(SDUtil.format(schemeBO.getMaximumPrice(), 0, 2))
                && Double.parseDouble(SDUtil.format(schemeBO.getSelectedPrice(), 0, 2)) > 0)) {
            /*
             * if ((priceEntered < schemeBO.getMaximumPrice() && priceEntered >
			 * 0) || (priceEntered > schemeBO.getActualPrice())) {
			 */

            showAlert(getResources().getString(R.string.not_in_range_reset));

            return false;
        }

        return true;
    }

    /**
     * @param schemeBO ,this is free product schemeBO
     * @return if scheme entered discount between min and max discount return
     * true,else false
     */
    private boolean isValidateSchemeDiscountEntered(SchemeBO schemeBO) {

        if (!(schemeBO.getSelectedPrecent() >= schemeBO.getMinimumPrecent()
                && schemeBO.getSelectedPrecent() <= schemeBO
                .getMaximumPrecent() && schemeBO.getSelectedPrecent() > 0)) {
            /*
             * if ((priceEntered < schemeBO.getMaximumPrice() && priceEntered >
			 * 0) || (priceEntered > schemeBO.getActualPrice())) {
			 */

            showAlert("Not in Range, Resetted to default.");

            return false;
        }

        return true;
    }

    /**
     * @param schemeBO ,this is free product schemeBO
     * @return if scheme entered amount between min and max amount return
     * true,else false
     */
    private boolean isValidateSchemeAmountEntered(SchemeBO schemeBO) {

        if (!(Double.parseDouble(SDUtil.format(schemeBO.getSelectedAmount(), 0, 2)) >= Double.parseDouble(SDUtil.format(schemeBO.getMinimumAmount(), 0, 2))
                && Double.parseDouble(SDUtil.format(schemeBO.getSelectedAmount(), 0, 2)) <= Double.parseDouble(SDUtil.format(schemeBO.getMaximumAmount(), 0, 2))
                && Double.parseDouble(SDUtil.format(schemeBO.getSelectedAmount(), 0, 2)) > 0)) {
            /*
             * if ((priceEntered < schemeBO.getMaximumPrice() && priceEntered >
			 * 0) || (priceEntered > schemeBO.getActualPrice())) {
			 */

            showAlert("Not in Range, Resetted to default.");

            return false;
        }

        return true;
    }

    /**
     * Method to show applied scheme details in scheme apply screen.
     */
    private void updateSchemeDetails() {
        bmodel.schemeDetailsMasterHelper.schemeApply();
        mSchemeDoneList = bmodel.schemeDetailsMasterHelper.getAppliedSchemeList();
        if (mSchemeDoneList.size() > 0) {
            for (SchemeBO schemeBO : mSchemeDoneList) {
                if (schemeBO != null) {
                    if (schemeBO.getIsFreeCombination() == 0) {
                        bmodel.schemeDetailsMasterHelper
                                .freeCombinationNotAvailable(schemeBO);
                    } else if (schemeBO.getIsFreeCombination() == 1) {
                        bmodel.schemeDetailsMasterHelper
                                .freeCombinationAvailable(schemeBO);
                    }
                }
            }

            // to remove off invoice scheme product wihtout stock
            ArrayList<SchemeBO> mFilteredSchemeList = new ArrayList<>();
            for (SchemeBO schemeBO : mSchemeDoneList) {
                if (schemeBO != null) {

                    if (schemeBO.getIsOnInvoice() == 0) {
                        if (bmodel.schemeDetailsMasterHelper.isSihAvailableForSchemeGroupFreeProducts(schemeBO, schemeBO.getSchemeId())) {
//                            if (schemeBO.getIsFreeCombination() == 1)
                            mFilteredSchemeList.add(schemeBO);
                        }
                    } else if (schemeBO.getIsFreeCombination() == 1) {
                        mFilteredSchemeList.add(schemeBO);
                    }
                }
            }


            mSchemeDoneList.clear();
            mSchemeDoneList.addAll(mFilteredSchemeList);


            mExpandableAdapterNew = new SchemeExpandapleAdapterNew();
            mExpandableLV.setAdapter(mExpandableAdapterNew);
        } else {
            return;
        }

    }

}