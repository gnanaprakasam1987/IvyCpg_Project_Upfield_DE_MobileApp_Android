package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.initiative.InitiativeActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;

import java.util.ArrayList;
import java.util.Vector;

public class OrderDiscount extends IvyBaseActivityNoActionBar implements OnClickListener {

    private BusinessModel bmodel;
    private Button back;
    private TextView productName, totalval;
    private ListView lvwplist;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY;
    private String append = "";
    private Vector<String> initiativeProductIds;
    private OrderSummary initAct;
    double sum = 0;
    private double totalOrderValue;
    private String screenCode = "MENU_STK_ORD";
    private Toolbar toolbar;
    RelativeLayout toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_discount);
        // if (true)
        // return;

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbarTitle = (RelativeLayout) findViewById(R.id.tolllayout);
        toolbarTitle.setVisibility(View.VISIBLE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }
        back = (Button) findViewById(R.id.closeButton);
        back.setOnClickListener(this);

        if (toolbar != null)
            setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(
                null);
        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        setScreenTitle(getResources().getString(R.string.discount));
        totalval = (TextView) findViewById(R.id.totalValue);
        productName = (TextView) findViewById(R.id.tvProductNameTitle);
        productName.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
            findViewById(R.id.dummycaseTitle).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
            findViewById(R.id.dummypcsTitle).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            findViewById(R.id.dummyoutercaseTitle).setVisibility(View.GONE);

        findViewById(R.id.LL_titleBar).setVisibility(View.GONE);

        // on/off d1,d2,d3,da
        if (!bmodel.configurationMasterHelper.SHOW_D1)
            findViewById(R.id.d1title).setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_DA)
            findViewById(R.id.datitle).setVisibility(View.GONE);

        updateOrderTable();

    }

    public void onBackPressed() {
        // do something on back.
        return;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        if (b == back) {
            // this.initAct.onResume();
            // dismiss();
        }
    }

    private void updateOrderTable() {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<ProductMasterBO>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {
                double temp = 0;
                temp = (ret.getOrderedPcsQty() * ret.getSrp())
                        + (ret.getOrderedCaseQty() * ret.getCsrp())
                        + (ret.getOrderedOuterQty() * ret.getOsrp());
                temp=SDUtil.formatAsPerCalculationConfig(temp);
                ret.setNetValue(temp);
                totalOrderValue = totalOrderValue + temp;
                mylist.add(ret);
            }
        }
        totalval.setText(bmodel.formatValue(totalOrderValue) + "");
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

    }

    private ProductMasterBO product;

    private class MyAdapter extends ArrayAdapter {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(OrderDiscount.this, R.layout.dialog_discount_row, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
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
            product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_discount_row, parent,
                        false);
                holder = new ViewHolder();
                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.caseqtyEditText = (TextView) row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = (TextView) row
                        .findViewById(R.id.orderQTYinpiece);
                holder.d1 = (EditText) row.findViewById(R.id.d1);
                holder.da = (EditText) row.findViewById(R.id.da);

                holder.mrp = (TextView) row.findViewById(R.id.mrp);
                holder.p4qty = (TextView) row.findViewById(R.id.ppq);
                holder.msq = (TextView) row.findViewById(R.id.msq);
                holder.total = (TextView) row.findViewById(R.id.total);
                holder.ou_type = (TextView) row.findViewById(R.id.OU_Type);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerorderQTYinCase);

                if (!bmodel.configurationMasterHelper.SHOW_D1)
                    holder.d1.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_DA)
                    holder.da.setVisibility(View.GONE);

                holder.d1.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.d1.setSelection(qty.length());

                        double i = SDUtil.convertToDouble(qty);
                        sum = 0;
                        if (i != 0 || i != 0.0) {
                            holder.productObj.setD1(i);
                            if (holder.productObj.getDA() > 0) {
                                holder.da.setText("0");
                                holder.productObj.setDA(0);
                            }
                            sum = holder.productObj.getD3()
                                    + holder.productObj.getD2()
                                    + holder.productObj.getD1();
                            if (sum > 100) {
                                Toast.makeText(OrderDiscount.this, "Value exceeded",
                                        Toast.LENGTH_SHORT).show();
                                int s1 = SDUtil.convertToInt(qty);
                                s1 = s1 / 10;
                                holder.productObj.setD1(s1);
                                holder.d1.removeTextChangedListener(this);
                                holder.d1.setText(s1 + "");
                                holder.d1.addTextChangedListener(this);
                            } else {
                                double tot = discountcalc(holder.productObj,
                                        sum);
                                holder.total.setText(bmodel.formatValue(tot));
                            }
                        } else {
                            holder.d1.removeTextChangedListener(this);
                            holder.d1.setText("0");
                            holder.productObj.setD1(0);
                            double tot = discountcalc(holder.productObj,
                                    holder.productObj.getD3()
                                            + holder.productObj.getD2()
                                            + holder.productObj.getD1());
                            holder.total.setText(bmodel.formatValue(tot));
                            holder.d1.addTextChangedListener(this);
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.da.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable arg0) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.da.setSelection(qty.length());
                        double i = SDUtil.convertToDouble(qty);

                        if (i != 0) {
                            holder.productObj.setDA(i);

                            if (holder.productObj.getD1() > 0) {
                                holder.d1.setText("0");
                                holder.productObj.setD1(0);
                            }

                            double tot = discountAmountCalc(holder.productObj,
                                    i);
                            holder.total.setText(bmodel.formatValue(tot));
                        } else {
                            holder.da.removeTextChangedListener(this);
                            holder.da.setText("0");
                            holder.productObj.setDA(0);
                            double tot = discountcalc(holder.productObj,
                                    holder.productObj.getD3()
                                            + holder.productObj.getD2()
                                            + holder.productObj.getD1());
                            holder.total.setText(bmodel.formatValue(tot));
                            holder.da.addTextChangedListener(this);
                        }
                    }

                });

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.caseqtyEditText.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pieceqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                holder.d1.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.d1;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.d1.getInputType();
                        holder.d1.setInputType(InputType.TYPE_NULL);
                        holder.d1.onTouchEvent(event);
                        holder.d1.setInputType(inType);
                        if (holder.d1.getText().length() > 0)
                            holder.d1.setSelection(holder.d1.getText().length());
                        return true;
                    }
                });

                holder.da.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.da;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.da.getInputType();
                        holder.da.setInputType(InputType.TYPE_NULL);
                        holder.da.onTouchEvent(event);
                        holder.da.setInputType(inType);
                        if (holder.da.getText().length() > 0)
                            holder.da.setSelection(holder.da.getText().length());
                        return true;
                    }

                });
                holder.pieceqty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.d1;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.pieceqty.getInputType();
                        holder.pieceqty.setInputType(InputType.TYPE_NULL);
                        holder.pieceqty.onTouchEvent(event);
                        holder.pieceqty.setInputType(inType);
                        return true;
                    }
                });

                holder.caseqtyEditText
                        .setOnTouchListener(new OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                productName.setText(holder.pname);
                                QUANTITY = holder.d1;
                                QUANTITY.setTag(holder.productObj);
                                int inType = holder.caseqtyEditText
                                        .getInputType();
                                holder.caseqtyEditText
                                        .setInputType(InputType.TYPE_NULL);
                                holder.caseqtyEditText.onTouchEvent(event);
                                holder.caseqtyEditText.setInputType(inType);
                                return true;
                            }
                        });

                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.d1;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        return true;
                    }
                });
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        productName.setText(holder.pname);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productObj = product;
            holder.ref = position;
            holder.productId = holder.productObj.getProductID();
            holder.productCode = holder.productObj.getProductCode();
            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.mrp.setText("Price: "
                    + bmodel.formatValue(holder.productObj.getSrp()) + "");
            holder.p4qty.setText("PPQ: "
                    + holder.productObj.getRetailerWiseProductWiseP4Qty());

            holder.outerQty
                    .setText(holder.productObj.getOrderedOuterQty() + "");
            holder.caseqtyEditText.setText(holder.productObj
                    .getOrderedCaseQty() + "");
            holder.pieceqty.setText(holder.productObj.getOrderedPcsQty() + "");
            holder.msq.setText("MSQ: " + holder.productObj.getMSQty() + "");

            holder.ou_type.setText("OU: " + holder.productObj.getOU() + "");
            holder.d1.setText(holder.productObj.getD1() + "");

            holder.da.setText(holder.productObj.getDA() + "");

            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.caseqtyEditText.setEnabled(false);
            } else {
                holder.caseqtyEditText.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.pieceqty.setEnabled(false);
            } else {
                holder.pieceqty.setEnabled(true);
            }


            return (row);
        }
    }

    class ViewHolder {

        ProductMasterBO productObj;
        String productId, productCode, pname;
        int caseSize, stockInHand;// product id
        TextView psname, mrp, p4qty;
        EditText d1, da;
        int ref;
        TextView msq, total, ou_type, caseqtyEditText, pieceqty, outerQty;
    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {

        // ProductMasterBO temp = (ProductMasterBO) QUANTITY.getTag();
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }


    public double discountcalc(ProductMasterBO productBO, double sum) {

        /* apply batchwise discount starts */
        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (productBO.getBatchwiseProductCount() > 0) {
                double total = bmodel.batchAllocationHelper
                        .updateDiscontBatchwise(productBO, sum);
                productBO.setNetValue(total);
                updateDiscountedOrderValue();
                return total;
            }
        }
        /* apply batchwise discount ends */

        double line_total_price = (productBO.getOrderedCaseQty() * productBO
                .getCsrp())
                + (productBO.getOrderedPcsQty() * productBO.getSrp())
                + (productBO.getOrderedOuterQty() * productBO.getOsrp());

        line_total_price=SDUtil.formatAsPerCalculationConfig(line_total_price);

        double total = line_total_price - (line_total_price * sum / 100);

        productBO.setNetValue(SDUtil.formatAsPerCalculationConfig(total));

        updateDiscountedOrderValue();

        return total;
    }

    private void updateDiscountedOrderValue() {
        double value = 0;
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        int siz = items.size();
        mylist = new ArrayList<ProductMasterBO>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {
                value = value + ret.getNetValue();
            }
        }
        totalval.setText(bmodel.formatValue(value) + "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_barcode).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.menu_next) {
            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                Intent init = new Intent(OrderDiscount.this,
                        InitiativeActivity.class);
                init.putExtra("ScreenCode", screenCode);
                startActivity(init);
//                finish();
            } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                Intent i = new Intent(OrderDiscount.this,
                        DigitalContentActivity.class);
                i.putExtra("FromInit", "Initiative");
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
//                finish();
            } else {
                Intent i = new Intent(OrderDiscount.this, OrderSummary.class);
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
//                finish();
            }
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
            return true;
        } else if (i1 == android.R.id.home) {
            onBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBack() {
        // Intent returnIntent = new Intent();
        // setResult(RESULT_CANCELED, returnIntent);
        // finish();
        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        if (schemeHelper.IS_SCHEME_ON
                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
            Intent intent = new Intent(OrderDiscount.this, SchemeApply.class);
            intent.putExtra("ScreenCode", screenCode);
            startActivity(intent);
        } else if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
            Intent intent = new Intent(OrderDiscount.this,
                    BatchAllocation.class);
            intent.putExtra("OrderFlag", "Nothing");
            intent.putExtra("ScreenCode", screenCode);
            startActivity(intent);
        } else {
            Intent intent;
            if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                intent = new Intent(OrderDiscount.this, CatalogOrder.class);
            } else {
                intent = new Intent(OrderDiscount.this, StockAndOrder.class);
            }
            intent.putExtra("OrderFlag", "Nothing");
            intent.putExtra("ScreenCode", screenCode);
            startActivity(intent);
        }
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        finish();

    }

    public double discountAmountCalc(ProductMasterBO productBO, double amount) {

        double line_total_price = (productBO.getOrderedCaseQty() * productBO
                .getCsrp())
                + (productBO.getOrderedPcsQty() * productBO.getSrp())
                + (productBO.getOrderedOuterQty() * productBO.getOsrp());

        SDUtil.formatAsPerCalculationConfig(line_total_price);

        double total = line_total_price - amount;
        productBO.setNetValue(total);
        updateDiscountedOrderValue();
        return total;
    }


}
