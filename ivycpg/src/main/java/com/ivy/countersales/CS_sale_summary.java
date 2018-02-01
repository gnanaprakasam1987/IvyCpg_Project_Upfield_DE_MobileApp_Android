package com.ivy.countersales;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SchemeApply;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar.s on 6/22/2017.
 */

public class CS_sale_summary extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    TextView txt_cust_name, txt_mob_no, txt_date, txt_counter, txt_store_name, txt_total;
    Toolbar toolbar;
    BusinessModel bmodel;
    String refid;
    ArrayList<ProductMasterBO> lstProducts;
    ListView listview;
    private ExpandableListView mExpListView;
    TextView txt_discountedAmount, tv_bill_amount;
    double totalValue = 0;
    boolean isFromSale = false;
    private double finalValue = 0;
    Button btnSave, btnDone;
    private double mBPERValue = 0;
    private double mSchemeDiscountedAmountOnBill = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        setContentView(R.layout.cs_sale_summary);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("Summary");

        if (getIntent().getExtras() != null) {
            refid = getIntent().getExtras().getString("refid");
        }

        txt_cust_name = (TextView) findViewById(R.id.val_cust_name);
        txt_mob_no = (TextView) findViewById(R.id.val_cust_mob);
        txt_date = (TextView) findViewById(R.id.val_cust_date);
        txt_counter = (TextView) findViewById(R.id.val_cust_counter);
        txt_store_name = (TextView) findViewById(R.id.val_cust_store);

        txt_total = (TextView) findViewById(R.id.val_cust_total);
        txt_discountedAmount = (TextView) findViewById(R.id.txt_discounted_amt);
        tv_bill_amount = (TextView) findViewById(R.id.txt_bill_amt);

        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        findViewById(R.id.vw_dotted_line_one).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        findViewById(R.id.vw_dotted_line_two).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        findViewById(R.id.vw_dotted_line_three).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (getIntent().getExtras() != null) {
            isFromSale = getIntent().getBooleanExtra("isFromSale", false);
            finalValue = getIntent().getDoubleExtra("finalValue", 0);
        }

        if (isFromSale) {
            btnDone.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
        } else {
            btnDone.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        }

        listview = (ListView) findViewById(R.id.list);
        mExpListView = (ExpandableListView) findViewById(R.id.elv);

        try {
            txt_cust_name.setText(bmodel.getCounterSaleBO().getCustomerName());
            txt_mob_no.setText(bmodel.getCounterSaleBO().getContactNumber());
            txt_date.setText(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat));
            txt_counter.setText(bmodel.userMasterHelper.getUserMasterBO().getCounterName());
            txt_store_name.setText(bmodel.getRetailerMasterBO().getRetailerName());


            lstProducts = new ArrayList<>();

            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmSalesproduct() != null
                    && bmodel.getCounterSaleBO().getmSalesproduct().size() > 0) {
                for (ProductMasterBO bo : bmodel.getCounterSaleBO().getmSalesproduct()) {
                    if (bo.getCsPiece() > 0 || bo.getCsCase() > 0 || bo.getCsOuter() > 0
                            || bo.getCsFreePiece() > 0) {
                        lstProducts.add(new ProductMasterBO(bo));
                        totalValue += (bo.getMRP() * (bo.getCsPiece() + (bo.getCsCase() * bo.getCaseSize()) + (bo.getCsOuter() * bo.getOutersize())));

                    }
                }
            } else {
                findViewById(R.id.card_sales_details).setVisibility(View.GONE);
            }

            txt_total.setText(bmodel.formatValue(totalValue));

            updateSchemeDetails();

            updateBillPercentageSchemeDiscount();

            if (finalValue > 0) {
                if (finalValue != totalValue) {
                    if (bmodel.getCounterSaleBO() != null) {
                        if (bmodel.getCounterSaleBO().getDisPercentage() > 0) {
                            bmodel.getCounterSaleBO().setDisPercentage(0);
                            bmodel.getCounterSaleBO().setDisAmount(0);
                        } else if (bmodel.getCounterSaleBO().getDisAmount() > 0) {
                            bmodel.getCounterSaleBO().setDisAmount(0);
                            bmodel.getCounterSaleBO().setDisPercentage(0);
                        }
                    }
                }
            }

            mExpListView.setAdapter(new ProductExpandableAdapter());

            for (int i = 0; i < lstProducts.size(); i++) {
                mExpListView.expandGroup(i);
            }

            mExpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });
        } catch (Exception e) {
            Commons.print("Exception" + e);
        }
    }

    private ProductMasterBO checkProduct(String mProductId) {
        for (ProductMasterBO bo : lstProducts) {
            if (bo.getProductID().equalsIgnoreCase(mProductId)) {
                return bo;
            }
        }
        return null;
    }

    private void updateBillPercentageSchemeDiscount() {
        mSchemeDiscountedAmountOnBill = totalValue * mBPERValue / 100;
        totalValue = totalValue - mSchemeDiscountedAmountOnBill;

        txt_discountedAmount.setText(bmodel.formatValue(mSchemeDiscountedAmountOnBill) + "");

        tv_bill_amount.setText(bmodel.formatValue(totalValue) + "");

        OrderHelper.getInstance(this).invoiceDisount = String.valueOf(mSchemeDiscountedAmountOnBill);
    }

    private void updateSchemeDetails() {
        mBPERValue = 0;
        ArrayList<String> mBPERAchievedSchemeList = new ArrayList<>();
        ArrayList<SchemeBO> appliedSchemeList = bmodel.schemeDetailsMasterHelper
                .getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {
                    if (schemeBO.isAmountTypeSelected()) {
                        totalValue = totalValue
                                - schemeBO.getSelectedAmount();

                        ProductMasterBO productMasterBO = new ProductMasterBO();
                        productMasterBO.setProductID(schemeBO.getSchemeId());
                        productMasterBO.setProductName(schemeBO.getProductName());
                        productMasterBO.setDiscount_order_value(schemeBO.getSelectedAmount());
                        productMasterBO.setSchemeDiscount(true);
                        lstProducts.add(new ProductMasterBO(productMasterBO));
                    }

                    List<SchemeProductBO> schemeproductList = schemeBO
                            .getBuyingProducts();
                    int i = 0;
                    boolean isBuyProductAvailable = false;
                    if (schemeproductList != null) {
                        ArrayList<String> productidList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeproductList) {
                            /*ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(schemeProductBo
                                            .getProductId());*/
                            ProductMasterBO productBO = checkProduct(schemeProductBo
                                    .getProductId());
                            if (productBO != null) {
                                if (!productidList.contains(productBO.getProductID())) {
                                    productidList.add(productBO.getProductID());
                                    i = i++;
                                    if (productBO != null) {
                                        if (productBO.getOrderedPcsQty() > 0
                                                || productBO.getOrderedCaseQty() > 0
                                                || productBO.getOrderedOuterQty() > 0) {
                                            isBuyProductAvailable = true;
                                            if (schemeBO.isAmountTypeSelected()) {
                                                schemeProductBo.setDiscountValue(schemeBO.getSelectedAmount());
                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                                                        if (batchList != null && !batchList.isEmpty()) {
                                                            for (ProductMasterBO batchProduct : batchList) {
                                                                int totalQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * productBO.getCaseSize())
                                                                        + (batchProduct.getOrderedOuterQty() * productBO.getOutersize());
                                                                if (totalQty > 0) {

                                                                    double discProd = schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount();
                                                                    batchProduct.setSchemeDiscAmount(batchProduct.getSchemeDiscAmount() + (discProd / productBO.getOrderedBatchCount()));
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + (schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount()));
                                                    }
                                                } else {
                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + (schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount()));
                                                }
                                            } else if (schemeBO.isPriceTypeSeleted()) {
                                                double totalpriceDiscount;

                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", true);
                                                    } else {
                                                        totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", false);
                                                    }

                                                } else {
                                                    totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                            .updateSchemeProducts(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrice(),
                                                                    "SCH_PR", false);
                                                }

                                                if (productBO.getDiscount_order_value() > 0) {
                                                    productBO
                                                            .setDiscount_order_value(productBO
                                                                    .getDiscount_order_value()
                                                                    - totalpriceDiscount);

                                                }
                                                if (productBO.getSchemeAppliedValue() > 0) {
                                                    productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalpriceDiscount);
                                                }

                                                schemeProductBo.setDiscountValue(totalpriceDiscount);

                                                totalValue = totalValue
                                                        - totalpriceDiscount;

                                                ProductMasterBO productMasterBO = new ProductMasterBO();
                                                productMasterBO.setProductID(schemeBO.getSchemeId());
                                                productMasterBO.setProductName(schemeBO.getProductName());
                                                productMasterBO.setDiscount_order_value(totalpriceDiscount);
                                                productMasterBO.setSchemeDiscount(true);
                                                lstProducts.add(new ProductMasterBO(productMasterBO));

                                            } else if (schemeBO
                                                    .isDiscountPrecentSelected()) {

                                                if (schemeBO.getGetType().equalsIgnoreCase(bmodel.schemeDetailsMasterHelper.SCHEME_PERCENTAGE_BILL)) {
                                                    if (!mBPERAchievedSchemeList.contains(schemeBO.getSchemeId())) {
                                                        mBPERValue = mBPERValue + schemeBO.getSelectedPrecent();
                                                        mBPERAchievedSchemeList.add(schemeBO.getSchemeId());

                                                        ProductMasterBO productMasterBO = new ProductMasterBO();
                                                        productMasterBO.setProductID(schemeBO.getSchemeId());
                                                        productMasterBO.setProductName(schemeBO.getProductName());
                                                        productMasterBO.setDiscount_order_value(mBPERValue);
                                                        productMasterBO.setTypeName("BPER");
                                                        productMasterBO.setSchemeDiscount(true);
                                                        lstProducts.add(new ProductMasterBO(productMasterBO));
                                                    }
                                                } else {
                                                    double totalPercentageDiscount;
                                                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                            && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                        if (productBO
                                                                .getBatchwiseProductCount() > 0) {
                                                            totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                                    .updateSchemeProducts(
                                                                            productBO,
                                                                            schemeBO.getSelectedPrecent(),
                                                                            "SCH_PER", true);
                                                        } else {
                                                            totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                                    .updateSchemeProducts(
                                                                            productBO,
                                                                            schemeBO.getSelectedPrecent(),
                                                                            "SCH_PER",
                                                                            false);
                                                        }
                                                    } else {
                                                        totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrecent(),
                                                                        "SCH_PER", false);
                                                    }

                                                    if (productBO.getDiscount_order_value() > 0) {
                                                        productBO
                                                                .setDiscount_order_value(productBO
                                                                        .getDiscount_order_value()
                                                                        - totalPercentageDiscount);
                                                    }

                                                    if (productBO.getSchemeAppliedValue() > 0) {
                                                        productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalPercentageDiscount);
                                                    }
                                                    schemeProductBo.setDiscountValue(totalPercentageDiscount);
                                                    totalValue = totalValue
                                                            - totalPercentageDiscount;

                                                    ProductMasterBO productMasterBO = new ProductMasterBO();
                                                    productMasterBO.setProductID(schemeBO.getSchemeId());
                                                    productMasterBO.setProductName(schemeBO.getProductName());
                                                    productMasterBO.setDiscount_order_value(totalPercentageDiscount);
                                                    productMasterBO.setSchemeDiscount(true);
                                                    lstProducts.add(new ProductMasterBO(productMasterBO));
                                                }


                                            } else if (schemeBO
                                                    .isQuantityTypeSelected()) {
                                                updateSchemeFreeproduct(schemeBO,
                                                        productBO);
                                                break;
                                            }
                                        } else {
                                            if (schemeBO.isQuantityTypeSelected()) {
                                                // if  Accumulation scheme's buy product not avaliable, free product set in First order product object
                                                if (i == schemeproductList.size() && !isBuyProductAvailable) {
                                                    ProductMasterBO firstProductBO = lstProducts.get(0);
                                                    updateSchemeFreeproduct(schemeBO,
                                                            firstProductBO);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private void updateSchemeFreeproduct(SchemeBO schemeBO,
                                         ProductMasterBO productBO) {
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (productBO.getSchemeProducts() == null) {
            productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
        }

        if (freeProductList != null) {
            for (SchemeProductBO freeProductBo : freeProductList) {
                if (freeProductBo.getQuantitySelected() > 0) {
                    ProductMasterBO product = bmodel.productHelper.getProductMasterBOById(freeProductBo
                            .getProductId());
                    if (product != null) {
                        productBO.getSchemeProducts().add(freeProductBo);

                        //For counter sales- If child logic enabled, then scheme
                        productBO.setIsscheme(1);
                    }
                }
            }
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_done) {
            Intent intent = new Intent(this, CustomerVisitActivity.class);
            startActivity(intent);
            finish();
        } else if (view.getId() == R.id.btn_save) {
            saveCustomerDetails(mSchemeDiscountedAmountOnBill);
        }
    }

    private void saveCustomerDetails(final double mSchemeDiscountedAmountOnBill) {
        String[] types = new String[]{"Draft", "Save"};
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Save as ?");
        builder.setSingleChoiceItems(types, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String flag = "I";
                if (i == 1) {
                    flag = "N";
                    if (bmodel.getCounterSaleBO().getCustomerName().equals("") || bmodel.getCounterSaleBO().getCustomerName() == null || bmodel.getCounterSaleBO().getCustomerName().equalsIgnoreCase("null")) {
                        Toast.makeText(CS_sale_summary.this, "Customer name is Mandatory to save", Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                        return;
                    }
                }


                bmodel.mCounterSalesHelper.saveCustomerVisitDetails(flag, refid, mSchemeDiscountedAmountOnBill);
                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(CS_sale_summary.this);
                surveyHelperNew.saveCSSurveyAnswer(flag);
                surveyHelperNew.deleteUnusedImages();
                dialogInterface.dismiss();

                Toast.makeText(CS_sale_summary
                        .this, R.string.saved_successfully, Toast.LENGTH_LONG).show();

                Vector<ProductMasterBO> printList = new Vector<ProductMasterBO>();
                //   printList.addAll(bmodel.getCounterSaleBO().getmSalesproduct());

                if (bmodel.getCounterSaleBO().getmSalesproduct() != null) {
                    for (ProductMasterBO bo : lstProducts) {
                        if (bo.getCsPiece() > 0) {
                            bo.setOrderedPcsQty(bo.getCsPiece());
                            bo.setSrp(Float.parseFloat(bo.getMRP() + ""));
                            bo.setDiscount_order_value(bo.getCsPiece() * bo.getMRP());

                            printList.add(bo);
                        } else if (bo.getCsFreePiece() > 0) {
                            printList.add(bo);
                        }
                    }
                }

                bmodel.invoiceNumber = bmodel.mCounterSalesHelper.getUid();
                bmodel.userMasterHelper.getUserMasterBO().setDistributorName(bmodel.userMasterHelper.getUserMasterBO().getCounterName());
                bmodel.mCS_commonPrintHelper.xmlRead("invoice", false, printList, null);

                if (bmodel.getCounterSaleBO().isDraft()
                        ? bmodel.getCounterSaleBO().getmSalesproduct().size() <= 0
                        : bmodel.getCounterSaleBO().getmSalesproduct() == null) {
                    Intent intent = new Intent(
                            CS_sale_summary.this,
                            HomeScreenActivity.class).putExtra("menuCode", ConfigurationMasterHelper.MENU_COUNTER);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                } else {
                    Intent intent = new Intent(CS_sale_summary.this,
                            CS_CommonPrintPreviewActivity.class);
                    intent.putExtra("IsFromOrder", false);
                    intent.putExtra("IsUpdatePrintCount", false);
                    intent.putExtra("isHomeBtnEnable", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                }

                finish();

              /*  Intent in = new Intent(CS_sale_summary.this, CSHomeScreen.class);
                startActivity(in);
                finish();*/

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isFromSale) {
                Intent intent = new Intent(this, SchemeApply.class);
                intent.putExtra("ScreenCode", "CSale");
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, CustomerVisitActivity.class);
                startActivity(intent);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class ViewHolder {
        private ProductMasterBO counterSaleBO;
        TextView psname, txt_qty, txt_free, txt_mrp, txt_value;
    }

    private class ProductExpandableAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_cs_summary,
                        parent, false);

                holder = new ViewHolder();

                holder.psname = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_productname);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.txt_qty = (TextView) row
                        .findViewById(R.id.txt_qty);
                holder.txt_free = (TextView) row
                        .findViewById(R.id.txt_free);

                holder.txt_mrp = (TextView) row
                        .findViewById(R.id.txt_mrp);

                holder.txt_value = (TextView) row
                        .findViewById(R.id.txt_value);


                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.txt_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.txt_free.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.txt_mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.txt_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = lstProducts.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.psname.setText(productBO.getProductName());
            holder.txt_qty.setText(productBO.getQuantitySelected() + "");

            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (lstProducts.get(groupPosition).getIsscheme() == 1
                    && lstProducts.get(groupPosition).getSchemeProducts() != null) {
                return lstProducts.get(groupPosition)
                        .getSchemeProducts().size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {
            if (lstProducts == null)
                return 0;

            return lstProducts.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View row, ViewGroup parent) {

            final ViewHolder holder;
            final ProductMasterBO counterBo = lstProducts.get(groupPosition);
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_cs_summary,
                        parent, false);

                holder = new ViewHolder();

                holder.psname = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_productname);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.txt_qty = (TextView) row
                        .findViewById(R.id.txt_qty);
                holder.txt_free = (TextView) row
                        .findViewById(R.id.txt_free);

                holder.txt_mrp = (TextView) row
                        .findViewById(R.id.txt_mrp);

                holder.txt_value = (TextView) row
                        .findViewById(R.id.txt_value);


                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.txt_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.txt_free.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.txt_mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.txt_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.counterSaleBO = counterBo;
            if (holder.counterSaleBO.isSchemeDiscount()) {
                holder.psname.setText(holder.counterSaleBO.getProductName());
                holder.txt_value.setText(bmodel.formatValue(holder.counterSaleBO.getDiscount_order_value()));
                holder.txt_qty.setText("-");
                holder.txt_free.setText("-");
                holder.txt_mrp.setText("-");
            } else if (holder.counterSaleBO.isSchemeDiscount() && holder.counterSaleBO.getTypeName() != null && holder.counterSaleBO.getTypeName().equals("BPER")) {
                holder.psname.setText(holder.counterSaleBO.getProductName());
                holder.txt_value.setText((holder.counterSaleBO.getDiscount_order_value() + "") + "%");
                holder.txt_qty.setText("-");
                holder.txt_free.setText("-");
                holder.txt_mrp.setText("-");

            } else {
                holder.psname.setText(holder.counterSaleBO.getProductName());
                holder.txt_qty.setText(holder.counterSaleBO.getCsPiece() + "");
                holder.txt_free.setText(holder.counterSaleBO.getCsFreePiece() + "");

                holder.txt_mrp.setText(SDUtil.format(holder.counterSaleBO.getMRP(), 2, 0) + "");
                holder.txt_value.setText(bmodel.formatValue((holder.counterSaleBO.getMRP() * (holder.counterSaleBO.getCsPiece() + (holder.counterSaleBO.getCsCase() * holder.counterSaleBO.getCaseSize()) + (holder.counterSaleBO.getCsOuter() * holder.counterSaleBO.getOutersize())))) + "");
            }

            return row;
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

}
