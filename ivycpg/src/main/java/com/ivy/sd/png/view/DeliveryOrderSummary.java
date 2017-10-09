package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar.s on 9/20/2017.
 */

public class DeliveryOrderSummary extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    BusinessModel bmodel;
    Vector<ProductMasterBO> mylist;
    ListView listView;
    Button btnSave;
    Toolbar toolbar;
    boolean isPartialOrder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_order_summary);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isPartialOrder = extras.getBoolean("isPartial");
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if (isPartialOrder)
                setScreenTitle("Partial Invoice");
            else
                setScreenTitle("Invoice");

            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView = (ListView) findViewById(R.id.lvwplist);
        btnSave = (Button) findViewById(R.id.btn_next);
        btnSave.setOnClickListener(this);

        loadProducts();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnSave.getId()) {
            new Save().execute();
        }

    }

    AlertDialog alertDialog;

    private class Save extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryOrderSummary.this);

            bmodel.customProgressDialog(alertDialog, builder, DeliveryOrderSummary.this, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                updateSchemeDetails();

                bmodel.insertDeliveryOrderRecord(isPartialOrder);

                if (!isPartialOrder) {
                    // bmodel.saveDeliveryOrderInvoice();
                    bmodel.saveNewInvoice();
                }

                bmodel.productHelper.clearOrderTable();
            } catch (Exception ex) {
                Commons.printException(ex);
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            alertDialog.dismiss();


            Toast.makeText(DeliveryOrderSummary.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_LONG).show();
            startActivity(new Intent(DeliveryOrderSummary.this, HomeScreenTwo.class));
            finish();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            startActivity(new Intent(DeliveryOrderSummary.this, DeliveryOrderActivity.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void loadProducts() {
        try {
            mylist = new Vector<>();
            for (ProductMasterBO productMasterBO : bmodel.productHelper.getProductMaster()) {
                if (productMasterBO.getDeliveredOuterQty() > 0 || productMasterBO.getDeliveredCaseQty() > 0 || productMasterBO.getDeliveredPcsQty() > 0) {
                    mylist.add(productMasterBO);
                }
            }

            MyAdapter mSchedule = new MyAdapter(mylist);
            listView.setAdapter(mSchedule);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(DeliveryOrderSummary.this,
                    R.layout.row_delivery_order_summary, items);
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

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_delivery_order_summary, parent,
                        false);
                holder = new ViewHolder();


                holder.psname = (TextView) row
                        .findViewById(R.id.PRODUCTNAME);

                holder.tv_pcs = (TextView) row
                        .findViewById(R.id.P_QUANTITY);
                holder.tv_case = (TextView) row
                        .findViewById(R.id.C_QUANTITY);
                holder.tv_outer = (TextView) row
                        .findViewById(R.id.OC_QUANTITY);

                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                //setting typefaces
                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());

                holder.tv_pcs.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.tv_case.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.tv_outer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));


                // Order Field - Enable/Disable
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    ((LinearLayout) row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productObj = product;

            holder.psname.setText(holder.productObj.getProductShortName());

            holder.tv_outer.setText(holder.productObj.getDeliveredOuterQty() + "");
            holder.tv_case.setText(holder.productObj.getDeliveredCaseQty() + "");
            holder.tv_pcs.setText(holder.productObj.getDeliveredPcsQty() + "");


            return row;
        }
    }

    class ViewHolder {
        private ProductMasterBO productObj;
        private TextView psname;

        private TextView tv_pcs, tv_case, tv_outer;


    }


    /**
     * //@param produBo
     *
     * @author rajesh.k After applied scheme update scheme details in product
     * wise total and added scheme free product in any one of same
     * scheme Buy product.
     */
    private void updateSchemeDetails() {
        ArrayList<SchemeBO> appliedSchemeList = bmodel.schemeDetailsMasterHelper
                .getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {

                    List<SchemeProductBO> schemeproductList = schemeBO
                            .getBuyingProducts();
                    int i = 0;
                    boolean isBuyProductAvailable = false;
                    if (schemeproductList != null) {
                        ArrayList<String> productidList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeproductList) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(schemeProductBo
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

                                            } else if (schemeBO
                                                    .isDiscountPrecentSelected()) {
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

                                            } else if (schemeBO
                                                    .isQuantityTypeSelected()) {
                                                // no need to show free products here..
                                              /*  updateSchemeFreeproduct(schemeBO,
                                                        productBO);*/
                                                break;
                                            }
                                        } else {
                                            // no need to show free products here..
                                           /* if (schemeBO.isQuantityTypeSelected()) {
                                                // if  Accumulation scheme's buy product not avaliable, free product set in First order product object
                                                if (i == schemeproductList.size() && !isBuyProductAvailable) {
                                                    ProductMasterBO firstProductBO = mOrderedProductList.get(0);
                                                    updateSchemeFreeproduct(schemeBO,
                                                            firstProductBO);
                                                }
                                            }*/
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
