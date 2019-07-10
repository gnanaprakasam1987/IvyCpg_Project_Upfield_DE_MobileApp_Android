package com.ivy.cpg.view.delivery.foodempire;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 9/20/2017.
 */

public class DeliveryOrderScheme extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    BusinessModel bmodel;
    ListView listView;
    Button btnSave;
    Toolbar toolbar;
    CheckBox chk_isScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_order_scheme);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);


        toolbar =  findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Scheme Products");

            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chk_isScheme =  findViewById(R.id.chk_isScheme);
        listView =  findViewById(R.id.list);
        btnSave =  findViewById(R.id.btn_next);
        btnSave.setOnClickListener(this);

        loadProducts();

    }


    private void loadProducts() {
        try {
            ArrayList<SchemeProductBO> mFreeProductList = new ArrayList<>();
            for (SchemeBO schemeBO : SchemeDetailsMasterHelper.getInstance(getApplicationContext()).getAppliedSchemeList()) {

                if (schemeBO.isQuantityTypeSelected()) {
                    SchemeProductBO freeProduct;
                    for (SchemeProductBO schemeProductBO : schemeBO.getFreeProducts()) {
                        freeProduct = new SchemeProductBO();

                        freeProduct.setProductId(schemeProductBO.getProductId());
                        freeProduct.setProductName(schemeProductBO.getProductName());

                        if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getPcUomid())
                            freeProduct.setDeliverQtyPcs((freeProduct.getDeliverQtyPcs() + schemeProductBO.getQuantitySelected()));
                        else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getCaseUomId())
                            freeProduct.setDeliverQtyCase((freeProduct.getDeliverQtyCase() + schemeProductBO.getQuantitySelected()));
                        else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getOuUomid())
                            freeProduct.setDeliverQtyOuter((freeProduct.getDeliverQtyOuter() + schemeProductBO.getQuantitySelected()));

                        mFreeProductList.add(freeProduct);
                    }
                }
            }

            MyAdapter adapter = new MyAdapter(mFreeProductList);
            listView.setAdapter(adapter);
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            startActivity(new Intent(DeliveryOrderScheme.this, DeliveryOrderActivity.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyAdapter extends ArrayAdapter<SchemeProductBO> {
        private final ArrayList<SchemeProductBO> items;

        public MyAdapter(ArrayList<SchemeProductBO> items) {
            super(DeliveryOrderScheme.this,
                    R.layout.row_deliver_order_scheme, items);
            this.items = items;
        }

        public SchemeProductBO getItem(int position) {
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
            SchemeProductBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_deliver_order_scheme, parent,
                        false);
                holder = new ViewHolder();


                holder.psname =  row
                        .findViewById(R.id.stock_and_order_listview_productname);


                holder.tv_pcs_ordered =  row
                        .findViewById(R.id.tv_pcs);
                holder.tv_case_ordered =  row
                        .findViewById(R.id.tv_case);
                holder.tv_outer_ordered =  row
                        .findViewById(R.id.tv_outer);


                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                ( row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                //setting typefaces
                holder.psname.setTypeface(FontUtils.getProductNameFont(DeliveryOrderScheme.this));

                holder.tv_pcs_ordered.setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.MEDIUM));
                holder.tv_case_ordered.setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.MEDIUM));
                holder.tv_outer_ordered.setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.MEDIUM));


                // Order Field - Enable/Disable
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ( row.findViewById(R.id.ll_case)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.tv_case_Title)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_case_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_case_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_case_Title).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    ( row.findViewById(R.id.ll_pcs)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.tv_pcs_Title)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_pcs_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_pcs_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_pcs_Title).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    ( row.findViewById(R.id.ll_outer)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.tv_outer_Title)).setTypeface(FontUtils.getFontRoboto(DeliveryOrderScheme.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_outer_Title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.tv_outer_Title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.tv_outer_Title)
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

            holder.psname.setText(holder.productObj.getProductName());

            if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                holder.tv_case_ordered.setText(String.valueOf(holder.productObj.getDeliverQtyCase()));
            }
            if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                holder.tv_pcs_ordered.setText(String.valueOf(holder.productObj.getDeliverQtyPcs()));
            }
            if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                holder.tv_outer_ordered.setText(String.valueOf(holder.productObj.getDeliverQtyOuter()));
            }

            return row;
        }
    }

    class ViewHolder {
        private SchemeProductBO productObj;

        private TextView tv_pcs_ordered, tv_case_ordered, tv_outer_ordered, psname;


    }

    @Override
    public void onClick(View view) {
        Button vw = (Button) view;

        if (vw == btnSave) {

            if (chk_isScheme.isChecked()) {
                Intent intent = new Intent(DeliveryOrderScheme.this, DeliveryOrderSummary.class);
                intent.putExtra("isPartial", false);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(DeliveryOrderScheme.this, DeliveryOrderSummary.class);
                intent.putExtra("isPartial", true);
                startActivity(intent);
                finish();
            }

        }
    }
}
