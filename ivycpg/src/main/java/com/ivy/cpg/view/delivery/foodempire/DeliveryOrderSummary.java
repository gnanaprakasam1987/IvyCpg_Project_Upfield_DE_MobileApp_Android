package com.ivy.cpg.view.delivery.foodempire;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.OrderRemarkDialog;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.FontUtils;

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
    private OrderHelper orderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_order_summary);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        orderHelper = OrderHelper.getInstance(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isPartialOrder = extras.getBoolean("isPartial");
        }

        toolbar =  findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if (isPartialOrder)
                setScreenTitle(getResources().getString(R.string.partial_delivery));
            else
                setScreenTitle(getResources().getString(R.string.text_invoice) + "\n" + getResources().getString(R.string.invoice_creation));

            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView =  findViewById(R.id.list);
        btnSave =  findViewById(R.id.btn_next);
        if (isPartialOrder)
            btnSave.setText(getResources().getString(R.string.partial_delivery));
        else
            btnSave.setText(getResources().getString(R.string.text_invoice)
                    + "\n" + getResources().getString(R.string.invoice_creation));

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
        private boolean isSaved;

        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryOrderSummary.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                updateSchemeDetails();


                if (!isPartialOrder) {
                    // bmodel.saveDeliveryOrderInvoice();
                    isSaved = orderHelper.saveOrder(DeliveryOrderSummary.this,false);
                    if (isSaved)
                        orderHelper.saveInvoice(DeliveryOrderSummary.this);
                }
                if (isSaved) {
                    orderHelper.insertDeliveryOrderRecord(DeliveryOrderSummary.this, isPartialOrder);
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_DELIVERY_ORDER, true);
                }

            } catch (Exception ex) {
                Commons.printException(ex);
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (isSaved) {
                Toast.makeText(DeliveryOrderSummary.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_LONG).show();
                if (!isPartialOrder) {
                    bmodel.mCommonPrintHelper.xmlRead("invoice", false, mylist, null,null,null,null);

                    Intent i = new Intent(DeliveryOrderSummary.this, CommonPrintPreviewActivity.class);
                    i.putExtra("IsFromOrder", true);
                    i.putExtra("IsUpdatePrintCount", true);
                    i.putExtra("isHomeBtnEnable", true);
                    i.putExtra("isHidePrintBtn", true);
                    startActivity(i);
                    finish();
                } else {

                    startActivity(new Intent(DeliveryOrderSummary.this, HomeScreenTwo.class));
                    finish();
                }
                bmodel.productHelper.clearOrderTable();
            } else {
                Toast.makeText(DeliveryOrderSummary.this, getResources().getString(R.string.order_save_falied), Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deliverydetails, menu);

        menu.findItem(R.id.menu_save).setVisible(false);
        MenuItem reviewAndPo = menu.findItem(R.id.menu_review);
        reviewAndPo.setVisible(true);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            startActivity(new Intent(DeliveryOrderSummary.this, DeliveryOrderActivity.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_review) {
            OrderRemarkDialog ordRemarkDialog = new OrderRemarkDialog(
                    DeliveryOrderSummary.this, true);
            ordRemarkDialog.show();
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

        public @NonNull View getView(final int position, View convertView,
                     @NonNull ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_delivery_order_summary, parent,
                        false);
                holder = new ViewHolder();


                holder.psname =  row
                        .findViewById(R.id.PRODUCTNAME);

                holder.tv_pcs =  row
                        .findViewById(R.id.P_QUANTITY);
                holder.tv_case =  row
                        .findViewById(R.id.C_QUANTITY);
                holder.tv_outer =  row
                        .findViewById(R.id.OC_QUANTITY);

                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                ( row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                //setting typefaces
                holder.psname.setTypeface(FontUtils.getProductNameFont(DeliveryOrderSummary.this));

                holder.tv_pcs.setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.MEDIUM));
                holder.tv_case.setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.MEDIUM));
                holder.tv_outer.setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.MEDIUM));


                // Order Field - Enable/Disable
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ( row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.LIGHT));
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
                    ( row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.LIGHT));
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
                    ( row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderSummary.this, FontUtils.FontType.LIGHT));
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

            holder.tv_outer.setText(String.valueOf(holder.productObj.getDeliveredOuterQty()));
            holder.tv_case.setText(String.valueOf(holder.productObj.getDeliveredCaseQty()));
            holder.tv_pcs.setText(String.valueOf(holder.productObj.getDeliveredPcsQty()));


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
        SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        ArrayList<SchemeBO> appliedSchemeList = schemeHelper
                .getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {

                    List<SchemeProductBO> schemeproductList = schemeBO
                            .getBuyingProducts();
                    if (schemeproductList != null) {

                        // Getting total order value of buy products
                        double totalOrderValueOfBuyProducts=0;
                        if (schemeBO.isAmountTypeSelected()) {
                            for (SchemeProductBO schemeProductBo : schemeproductList) {
                                ProductMasterBO productBO = bmodel.productHelper
                                        .getProductMasterBOById(schemeProductBo
                                                .getProductId());
                                totalOrderValueOfBuyProducts += (productBO.getOrderedCaseQty() * productBO.getCsrp())
                                        + (productBO.getOrderedPcsQty() * productBO.getSrp())
                                        + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                            }
                        }
                        //

                        ArrayList<String> productidList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeproductList) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(schemeProductBo
                                            .getProductId());
                            if (productBO != null) {
                                if (!productidList.contains(productBO.getProductID())) {
                                    productidList.add(productBO.getProductID());
                                    if (productBO != null) {
                                        if (productBO.getOrderedPcsQty() > 0
                                                || productBO.getOrderedCaseQty() > 0
                                                || productBO.getOrderedOuterQty() > 0) {
                                            if (schemeBO.isAmountTypeSelected()) {
                                                schemeProductBo.setDiscountValue(schemeBO.getSelectedAmount());

                                                // calculating free amount for current product by contribution to total value of buy products
                                                double line_value = (productBO.getOrderedCaseQty() * productBO.getCsrp())
                                                        + (productBO.getOrderedPcsQty() * productBO.getSrp())
                                                        + (productBO.getOrderedOuterQty() * productBO.getOsrp());
                                                double percentage_productContribution=((line_value/totalOrderValueOfBuyProducts)*100);
                                                double amount_free=schemeBO.getSelectedAmount()*(percentage_productContribution/100);
                                                //

                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                                                        if (batchList != null && !batchList.isEmpty()) {

                                                            // To get total order value of batch buy products
                                                            double totalOrderValueOfBuyProducts_batch=0;
                                                            for (ProductMasterBO batchProduct : batchList) {
                                                                totalOrderValueOfBuyProducts_batch += (batchProduct.getOrderedCaseQty() * productBO.getCsrp())
                                                                        + (batchProduct.getOrderedPcsQty() * productBO.getSrp())
                                                                        + (batchProduct.getOrderedOuterQty() * productBO.getOsrp());
                                                            }
                                                            //

                                                            for (ProductMasterBO batchProduct : batchList) {
                                                                int totalQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * productBO.getCaseSize())
                                                                        + (batchProduct.getOrderedOuterQty() * productBO.getOutersize());
                                                                if (totalQty > 0) {

                                                                    // calculating free amount for current batch product(by contribution to total value(Sum of all line value of batches in a product)).
                                                                    double line_value_batch= (batchProduct.getOrderedCaseQty() * productBO.getCsrp())
                                                                            + (batchProduct.getOrderedPcsQty() * productBO.getSrp())
                                                                            + (batchProduct.getOrderedOuterQty() * productBO.getOsrp());
                                                                    double percentage_batchProductContribution=((line_value_batch/totalOrderValueOfBuyProducts_batch)*100);
                                                                    double amount_free_batch=amount_free*(percentage_batchProductContribution/100);
                                                                    //

                                                                    batchProduct.setSchemeDiscAmount(batchProduct.getSchemeDiscAmount() + amount_free_batch);
                                                                    if (batchProduct.getLineValueAfterSchemeApplied() > 0) {
                                                                        batchProduct.setLineValueAfterSchemeApplied(batchProduct.getLineValueAfterSchemeApplied() - amount_free_batch);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                        if (productBO.getNetValue() > 0) {
                                                            productBO.setNetValue(productBO
                                                                    .getNetValue()
                                                                    - amount_free);

                                                        }
                                                        if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                            productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - amount_free);
                                                        }
                                                    }
                                                } else {
                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + amount_free);
                                                    if (productBO.getNetValue() > 0) {
                                                        productBO.setNetValue(productBO
                                                                .getNetValue()
                                                                - amount_free);

                                                    }
                                                    if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                        productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - amount_free);
                                                    }
                                                }
                                            } else if (schemeBO.isPriceTypeSeleted()) {
                                                double totalpriceDiscount;

                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        totalpriceDiscount = schemeHelper
                                                                .calculateDiscountValue(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", true);
                                                    } else {
                                                        totalpriceDiscount = schemeHelper
                                                                .calculateDiscountValue(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", false);
                                                    }

                                                } else {
                                                    totalpriceDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrice(),
                                                                    "SCH_PR", false);
                                                }

                                                if (productBO.getNetValue() > 0) {
                                                    productBO
                                                            .setNetValue(productBO
                                                                    .getNetValue()
                                                                    - totalpriceDiscount);

                                                }
                                                if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - totalpriceDiscount);
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
                                                        totalPercentageDiscount = schemeHelper
                                                                .calculateDiscountValue(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrecent(),
                                                                        "SCH_PER", true);
                                                    } else {
                                                        totalPercentageDiscount = schemeHelper
                                                                .calculateDiscountValue(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrecent(),
                                                                        "SCH_PER",
                                                                        false);
                                                    }
                                                } else {
                                                    totalPercentageDiscount = schemeHelper
                                                            .calculateDiscountValue(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrecent(),
                                                                    "SCH_PER", false);
                                                }

                                                if (productBO.getNetValue() > 0) {
                                                    productBO
                                                            .setNetValue(productBO
                                                                    .getNetValue()
                                                                    - totalPercentageDiscount);
                                                }

                                                if (productBO.getLineValueAfterSchemeApplied() > 0) {
                                                    productBO.setLineValueAfterSchemeApplied(productBO.getLineValueAfterSchemeApplied() - totalPercentageDiscount);
                                                }
                                                schemeProductBo.setDiscountValue(totalPercentageDiscount);

                                            } else if (schemeBO
                                                    .isQuantityTypeSelected()) {
                                                break;
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
