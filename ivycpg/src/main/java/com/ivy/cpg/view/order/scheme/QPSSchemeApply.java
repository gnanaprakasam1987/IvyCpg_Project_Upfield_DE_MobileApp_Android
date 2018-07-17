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
import com.ivy.sd.png.view.CatalogOrder;
import com.ivy.sd.png.view.CrownReturnActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.InitiativeActivity;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.sd.png.view.RemarksDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This screen will show list of schemes applied for current order from that user can select/Reject/Modify
 */
public class QPSSchemeApply extends IvyBaseActivityNoActionBar {
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


        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

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

    }


    /**
     * Getting scheme applied list by giving product master list
     */
    private class SchemeApplyAsync extends AsyncTask<Void, Void, Boolean> {
        List<SchemeBO> schemeIDList;

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                schemeIDList = schemeHelper.getSchemeList();
                //schemeHelper.schemeApply(bModel.productHelper.getProductMaster());//mOrderedList,mOrderedProductBOById,bModel.batchAllocationHelper.getBatchlistByProductID());
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
                HashMap<String, ArrayList<ProductMasterBO>> schemeHistoryList = schemeHelper.getSchemeHistoryListBySchemeId();
                mSchemeDoneList = new ArrayList<>();
                mSchemeDoneList.addAll(schemeIDList);
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
        menu.findItem(R.id.menu_product_filter).setVisible(false);
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
                if ((bModel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bModel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                        && bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(QPSSchemeApply.this,
                            CrownReturnActivity.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    Intent intent = new Intent(QPSSchemeApply.this,
                            BatchAllocation.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else {
                    Intent intent;
                    if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                        intent = new Intent(QPSSchemeApply.this, CatalogOrder.class);
                    } else {
                        intent = new Intent(QPSSchemeApply.this, StockAndOrder.class);
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
                    Intent init = new Intent(QPSSchemeApply.this,
                            OrderDiscount.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);

                } else if (bModel.configurationMasterHelper.IS_INITIATIVE) {

                    Intent init = new Intent(QPSSchemeApply.this,
                            InitiativeActivity.class);
                    init.putExtra("ScreenCode", screenCode);
                    startActivity(init);

                } else if (bModel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                    Intent i = new Intent(QPSSchemeApply.this,
                            DigitalContentActivity.class);
                    i.putExtra("FromInit", "Initiative");
                    i.putExtra("ScreenCode", screenCode);
                    startActivity(i);

                } else {
                    Intent i = new Intent(QPSSchemeApply.this, OrderSummary.class);
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
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View view, ViewGroup parent) {
            SchemeProductDetailHolder holder;
            if (view == null) {
                holder = new SchemeProductDetailHolder();
                // Other views
                view = mInflater.inflate(R.layout.row_qps_scheme_slab_detail, parent, false);
                holder.tv_current_cumulative_purchase = view.findViewById(R.id.tv_current_cumulative_purchase);
                holder.tv_current_cumulative_scheme_discount = view.findViewById(R.id.tv_current_cumulative_scheme_discount);
                holder.tv_current_scheme_per_amt = view.findViewById(R.id.tv_current_scheme_per_amt);
                holder.tv_current_balance_to_nextslab = view.findViewById(R.id.tv_current_balance_to_nextslab);
                holder.tv_productName = view.findViewById(R.id.tv_productName);
                holder.tv_pcs_ordered_qty = view.findViewById(R.id.tv_pcs_ordered_qty);
                holder.tv_cases_ordered_qty = view.findViewById(R.id.tv_cases_ordered_qty);
                holder.tv_uom = view.findViewById(R.id.tv_uom);
                holder.tv_pcs_final_qty = view.findViewById(R.id.tv_pcs_final_qty);
                holder.tv_cases_final_qty = view.findViewById(R.id.tv_cases_final_qty);
                holder.lnrSchemeHeader = view.findViewById(R.id.lnrSchemeHeader);
                //typeface
                holder.tv_current_cumulative_purchase.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_current_cumulative_scheme_discount.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_current_scheme_per_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_current_balance_to_nextslab.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_productName.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_pcs_ordered_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cases_ordered_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_uom.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_pcs_final_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cases_final_qty.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                view.setTag(holder);
            } else {
                holder = (SchemeProductDetailHolder) view.getTag();
            }

            if (childPosition != 0) {
                holder.lnrSchemeHeader.setVisibility(View.GONE);
            } else {
                holder.lnrSchemeHeader.setVisibility(View.VISIBLE);
            }

            holder.schemeProducts = (SchemeProductBO) getChild(groupPosition, childPosition);
            holder.tv_productName.setText(holder.schemeProducts.getProductName().length()==0?holder.schemeProducts.getProductFullName():
                    holder.schemeProducts.getProductName());
            holder.tv_uom.setText(holder.schemeProducts.getUomDescription());
            return view;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            List<SchemeProductBO> productList = mSchemeDoneList.get(groupPosition).getBuyingProducts();
            return productList.get(childPosition);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mSchemeDoneList.get(groupPosition).getBuyingProducts().size();

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
            return groupPosition;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View view, ViewGroup parent) {

            final SchemeProductHolder holder;

            if (view == null) {
                holder = new SchemeProductHolder();
                view = mInflater.inflate(R.layout.row_qps_scheme, parent,
                        false);

                holder.tv_scheme = view
                        .findViewById(R.id.tv_scheme);
                holder.tv_schemeDuration = view
                        .findViewById(R.id.tv_schemeduration);
                holder.tv_schemeType = view.findViewById(R.id.tv_schemetype);
                holder.tv_cumulative_purchase = view
                        .findViewById(R.id.tv_cumulative_purchase);
                holder.tv_cumulative_scheme_discount = view
                        .findViewById(R.id.tv_cumulative_scheme_discount);
                holder.tv_scheme_per_amt = view
                        .findViewById(R.id.tv_scheme_per_amt);
                holder.tv_nextslab = view
                        .findViewById(R.id.tv_nextslab);
                //typeface
                holder.tv_scheme.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_schemeDuration.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.tv_schemeType.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_cumulative_purchase.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.tv_cumulative_scheme_discount.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.tv_scheme_per_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.tv_nextslab.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

//                holder.upArrow.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//
//                        schemeHelper.loadSchemePromotion(getApplicationContext(),
//                                holder.schemeBO.getSchemeId(),
//                                holder.schemeBO.getParentLogic(),
//                                holder.schemeBO.getChannelId(),
//                                holder.schemeBO.getSubChannelId(),
//                                holder.productBO.getProductID(),
//                                holder.schemeBO.getQuantity());
//                        if (schemeHelper.getSchemePromotion() != null
//                                && schemeHelper.getSchemePromotion()
//                                .size() > 0) {
//                            if (mSchemePromDialog == null) {
////                                mSchemePromDialog = new NextSlabSchemeDialog(
////                                        QPSSchemeApply.this, false, null,
////                                        QPSSchemeApply.this);
//
//                                mSchemePromDialog.setCancelable(false);
//                            }
//                            mSchemePromDialog.show();
//                        } else {
//                            Toast.makeText(QPSSchemeApply.this,
//                                    "No Better Promotional Scheme available",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });

                view.setTag(holder);
            } else {
                holder = (SchemeProductHolder) view.getTag();
            }

            holder.schemeBO = mSchemeDoneList.get(groupPosition);
            holder.tv_scheme.setText(holder.schemeBO.getProductName());
            String periodStart = (holder.schemeBO.getDisplayPeriodStart() == null) ? "" : holder.schemeBO.getDisplayPeriodStart();
            String periodEnd = (holder.schemeBO.getDisplayPeriodEnd() == null) ? "" : holder.schemeBO.getDisplayPeriodEnd();
            holder.tv_schemeDuration.setText(periodStart + " - " + periodEnd);
            holder.tv_schemeType.setText(holder.schemeBO.getGetType());

            ArrayList<ProductMasterBO> accumulationList = schemeHelper.getSchemeHistoryListBySchemeId().get(holder.schemeBO.getSchemeId() + "");

            holder.tv_cumulative_purchase.setText("");
            holder.tv_cumulative_scheme_discount.setText("");
            holder.tv_scheme_per_amt.setText("");
            holder.tv_nextslab.setText("");
            return view;
        }

        @Override

        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }


    private class SchemeProductHolder {
        private ProductMasterBO productBO;
        private SchemeBO schemeBO;
        private TextView tv_scheme;
        private TextView tv_schemeDuration;
        private TextView tv_schemeType;
        private TextView tv_cumulative_purchase;
        private TextView tv_cumulative_scheme_discount;
        private TextView tv_scheme_per_amt;
        private TextView tv_nextslab;
    }

    static class SchemeProductDetailHolder {
        SchemeProductBO schemeProducts;
        TextView tv_current_cumulative_purchase, tv_current_cumulative_scheme_discount, tv_current_scheme_per_amt, tv_current_balance_to_nextslab;
        TextView tv_productName, tv_pcs_ordered_qty, tv_cases_ordered_qty,
                tv_uom, tv_pcs_final_qty, tv_cases_final_qty;
        LinearLayout lnrSchemeHeader;
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