package com.ivy.cpg.view.order;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Vector;

public class DiscountEditActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private BusinessModel bmodel;
    private Button saveButton;
    private TextView totalval, oldTotalValue,discountLable;
    private ListView lvwplist;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY, D1;
    private String append = "", d1 = "0";
    double sum = 0;
    private Double result = 0.0;
    private double totalOrderValue;
    private DiscountHelper discountHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_discount);

        findViewById(R.id.tolllayout).setVisibility(View.VISIBLE);
        findViewById(R.id.LL_titleBar).setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null ) {

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayShowTitleEnabled(false);
//            // Used to on / off the back arrow icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            setScreenTitle(getResources().getString(R.string.discount));
        }

        if (getWindow() != null)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        discountHelper = DiscountHelper.getInstance(this);

        bmodel = (BusinessModel) getApplicationContext();
        Button back = findViewById(R.id.closeButton);
        saveButton = findViewById(R.id.saveButton);
        back.setVisibility(View.GONE);
        back.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        saveButton.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));
        totalval = findViewById(R.id.totalValue);
        oldTotalValue = findViewById(R.id.oldTotalValue);
        discountLable = findViewById(R.id.discount_lable);
        discountLable.setText(getResources().getText(R.string.enter) +" "+ getResources().getString(R.string.discount));
        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                R.id.old_order_volume).getTag()) != null) {
            ((TextView) findViewById(R.id.old_order_volume))
                    .setText(bmodel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.old_order_volume)
                                    .getTag()));
        }
        D1 = findViewById(R.id.d1);

        ((TextView) findViewById(R.id.titlebar)).setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));
        ((TextView) findViewById(R.id.tvProductNameTitle)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTotalTitle)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        ((TextView) findViewById(R.id.minmax)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvValuetitle)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        ((TextView) findViewById(R.id.discount_lable)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        totalval.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));

        if (bmodel.configurationMasterHelper.SHOW_DISCOUNTED_PRICE) {
            ((TextView) findViewById(R.id.disc_price_title)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.disc_price_title).getTag()) != null)
                    ((TextView) findViewById(R.id.disc_price_title))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.disc_price_title)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }

        } else
            findViewById(R.id.disc_price_title).setVisibility(View.GONE);


        ((TextView) findViewById(R.id.u_price_title)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.u_price_title).getTag()) != null)
                ((TextView) findViewById(R.id.u_price_title))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.u_price_title)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.dummycaseTitle).setVisibility(View.GONE);
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.dummypcsTitle).setVisibility(View.GONE);
        }

        //If Global Discount is ON, enable the layout and keypad
        if (bmodel.configurationMasterHelper.SHOW_GLOBAL_DISOCUNT_DIALOG) {
            findViewById(R.id.discountlayout).setVisibility(View.VISIBLE);
            findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
        }
        //Else disable the layout and Keypad
        else {
            findViewById(R.id.discountlayout).setVisibility(View.GONE);
            findViewById(R.id.calcdot).setVisibility(View.GONE);
        }


        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
            findViewById(R.id.dummyoutercaseTitle).setVisibility(View.GONE);
        }

        // on/off d1,d2,d3,da
        if (!bmodel.configurationMasterHelper.SHOW_D1) {
            findViewById(R.id.d1title).setVisibility(View.GONE);
            // findViewById(R.id.d1).setVisibility(View.GONE); //As the SHOW_D1 will be false by default, editText is disabled. But it should.
        } else {
            ((TextView) findViewById(R.id.d1title)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        }

        if (!bmodel.configurationMasterHelper.SHOW_DA)
            findViewById(R.id.datitle).setVisibility(View.GONE);
        else {
            ((TextView) findViewById(R.id.datitle)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.MEDIUM));
        }


        if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
            bmodel.productHelper.downloadDiscountRange();
        } else {
            (findViewById(R.id.minmax)).setVisibility(View.GONE);
        }

        updateOrderTable();

        D1.setText(d1);


        D1.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                QUANTITY = D1;
                int inType = D1.getInputType();
                D1.setInputType(InputType.TYPE_NULL);
                D1.onTouchEvent(event);
                D1.setInputType(inType);
                if (D1.getText().length() > 0)
                    D1.setSelection(D1.getText().length());
                return true;
            }
        });


        D1.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {


                String qty = s.toString();
                if (qty.length() > 0)
                    D1.setSelection(qty.length());
                double i = SDUtil.convertToDouble(qty);

                sum = 0;
                //if (i != 0 || i != 0.0) {

                sum = SDUtil.convertToDouble(D1.getText().toString());
                if (sum > bmodel.configurationMasterHelper.discount_max) {
                    Toast.makeText(DiscountEditActivity.this, getResources().getString(R.string.value_exceeded),
                            Toast.LENGTH_SHORT).show();
                    int s1 = SDUtil.convertToInt(qty);
                    s1 = s1 / 10;
                    D1.setText(s1 + "");

                } else {

                    updateDiscount(i, 1);

                }
                //}

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String str = D1.getText().toString();
                if (str.isEmpty()) return;
                String str2 = PerfectDecimal(str, 6, 2);

                if (!str2.equals(str)) {
                    D1.setText(str2);
                    int pos = D1.getText().length();
                    D1.setSelection(pos);
                }
            }
        });
    }

    public String PerfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && after == false) {
                up++;
                if (up > MAX_BEFORE_POINT) return rFinal;
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > MAX_DECIMAL)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }
        return rFinal;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;

        if (b == saveButton) {
            setResult(1);
            finish();
        }
    }

    private void updateDiscount(double discount, int j) {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();

        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {
                if (j == 1) {
                    ret.setD1(discount);

                }
                if (j == 2) {
                    ret.setD2(discount);

                }
                if (j == 3) {
                    ret.setD3(discount);

                }

            }
        }

        lvwplist.invalidateViews();

    }


    private void updateOrderTable() {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {

                if (ret.getD1() == 0) {
                    d1 = "0";
                } else {
                    d1 = ret.getD1() + "";
                }

                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && ret.getBatchwiseProductCount() > 0) {
                    ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(ret.getProductID());
                    if (batchList != null) {
                        for (ProductMasterBO batchProductBO : batchList) {
                            batchProductBO.setLineValueAfterSchemeApplied(batchProductBO.getLineValueAfterSchemeApplied() + batchProductBO.getApplyValue());
                        }
                    }

                } else {
                    ret.setLineValueAfterSchemeApplied(ret.getLineValueAfterSchemeApplied() + ret.getApplyValue());
                }
                totalOrderValue = totalOrderValue + ret.getLineValueAfterSchemeApplied();

                if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
                    if (ret.getGroupid() == 0 && !ret.isCbsihAvailable()) {
                        mylist.add(ret);
                    }

                } else {
                    mylist.add(ret);
                }

            }
        }
        totalval.setText(bmodel.formatValue(totalOrderValue) + "");
        oldTotalValue.setText(bmodel.formatValue(totalOrderValue));
        MyAdapter mSchedule = new MyAdapter(DiscountEditActivity.this, mylist);
        lvwplist.setAdapter(mSchedule);

    }

    private ProductMasterBO product;

    private class MyAdapter extends ArrayAdapter {
        private ArrayList<ProductMasterBO> items;
        private Context context;

        public MyAdapter(Context context, ArrayList<ProductMasterBO> items) {
            super(context, R.layout.dialog_discount_row, items);
            this.items = items;
            this.context = context;
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
                holder.psname = row.findViewById(R.id.orderPRODNAME);
                holder.caseqty = row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty =  row
                        .findViewById(R.id.orderQTYinpiece);
                holder.d1 = row.findViewById(R.id.d1);
                holder.da = row.findViewById(R.id.da);

                holder.mrp = row.findViewById(R.id.mrp);
                holder.total = row.findViewById(R.id.total);
                holder.outerQty = row
                        .findViewById(R.id.outerorderQTYinCase);
                holder.min_max = row
                        .findViewById(R.id.min_max);
                holder.discounted_price = row
                        .findViewById(R.id.discounted_price);
                holder.unit_price = row
                        .findViewById(R.id.unit_price);

                holder.psname.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.MEDIUM));
                holder.caseqty.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.LIGHT));
                holder.pieceqty.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.LIGHT));
                holder.outerQty.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.LIGHT));
                holder.d1.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.THIN));
                holder.da.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.THIN));
                holder.mrp.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.MEDIUM));
                holder.total.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.MEDIUM));
                holder.min_max.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.THIN));
                if (bmodel.configurationMasterHelper.SHOW_DISCOUNTED_PRICE) {//change with proper config later
                    holder.discounted_price.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.THIN));
                } else {
                    holder.discounted_price.setVisibility(View.GONE);
                }
                holder.unit_price.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.THIN));

                if (!bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS)
                    holder.min_max.setVisibility(View.GONE);
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
                            if (sum > bmodel.configurationMasterHelper.discount_max) {
                                Toast.makeText(context, context.getResources().getString(R.string.value_exceeded),
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
                                int totalQty = holder.productObj.getOrderedPcsQty()
                                        + holder.productObj.getOrderedCaseQty()
                                        * holder.productObj.getCaseSize()
                                        + holder.productObj.getOrderedOuterQty()
                                        * holder.productObj.getOutersize();
                                holder.discounted_price.setText(bmodel.formatValue(tot / totalQty));
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
                            int totalQty = holder.productObj.getOrderedPcsQty()
                                    + holder.productObj.getOrderedCaseQty()
                                    * holder.productObj.getCaseSize()
                                    + holder.productObj.getOrderedOuterQty()
                                    * holder.productObj.getOutersize();
                            holder.discounted_price.setText(bmodel.formatValue(tot / totalQty));

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
                            if (tot >= 0) {
                                holder.total.setText(bmodel.formatValue(tot));
                                int totalQty = holder.productObj.getOrderedPcsQty()
                                        + holder.productObj.getOrderedCaseQty()
                                        * holder.productObj.getCaseSize()
                                        + holder.productObj.getOrderedOuterQty()
                                        * holder.productObj.getOutersize();
                                holder.discounted_price.setText(bmodel.formatValue(tot / totalQty));
                            } else {
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                holder.da.setText(qty);
                                if (bmodel.configurationMasterHelper.IS_DISCOUNT_PRICE_PER) {
                                    Toast.makeText(
                                            context,
                                            String.format(
                                                    context.getResources().getString(
                                                            R.string.discount_amt_cannot_be_higher_than_percentage),
                                                    bmodel.configurationMasterHelper.DISCOUNT_PRICE_PER + "%"),
                                            Toast.LENGTH_SHORT).show();

                                } else
                                    Toast.makeText(context, context.getResources().getString(R.string.discount_amt_cannot_be_higher_than_price), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            holder.da.removeTextChangedListener(this);
                            holder.da.setText("0");
                            holder.productObj.setDA(0);
                            double tot = discountcalc(holder.productObj,
                                    holder.productObj.getD3()
                                            + holder.productObj.getD2()
                                            + holder.productObj.getD1());
                            holder.total.setText(bmodel.formatValue(tot));
                            int totalQty = holder.productObj.getOrderedPcsQty()
                                    + holder.productObj.getOrderedCaseQty()
                                    * holder.productObj.getCaseSize()
                                    + holder.productObj.getOrderedOuterQty()
                                    * holder.productObj.getOutersize();
                            holder.discounted_price.setText(bmodel.formatValue(tot / totalQty));
                            holder.da.addTextChangedListener(this);
                        }
                    }

                });

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.caseqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pieceqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                holder.d1.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
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


                holder.da.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
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

            holder.min_max.setText(holder.productObj.getFrom_range() + "/" + holder.productObj.getTo_range());
            holder.unit_price.setText(bmodel.formatValue(holder.productObj.getSrp())
                    + "");

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.mrp.setText(context.getResources().getString(R.string.price)+" : "
                    + bmodel.formatValue(holder.productObj.getSrp()) + "");


            holder.outerQty
                    .setText(context.getResources().getString(R.string.item_outer) + " : " + holder.productObj.getOrderedOuterQty() + "");
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.caseqty.getTag()) != null)
                    holder.caseqty.setText(bmodel.labelsMasterHelper
                            .applyLabels(holder.caseqty.getTag()) + " : " + holder.productObj.getOrderedCaseQty());
                else
                    holder.caseqty.setText(context.getResources().getString(R.string.item_case) + " : " + holder.productObj.getOrderedCaseQty());

            } catch (Exception e) {
                Commons.printException(e);
                holder.caseqty.setText(context.getResources().getString(R.string.item_case) + " : " + holder.productObj.getOrderedCaseQty());
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.pieceqty.getTag()) != null)
                    holder.pieceqty.setText(bmodel.labelsMasterHelper
                            .applyLabels(holder.pieceqty.getTag()) + " : " + holder.productObj.getOrderedPcsQty());
                else
                    holder.pieceqty.setText(context.getResources().getString(R.string.item_piece) + " : " + holder.productObj.getOrderedPcsQty());

            } catch (Exception e) {
                Commons.printException(e);
                holder.pieceqty.setText(context.getResources().getString(R.string.item_piece) + " : " + holder.productObj.getOrderedPcsQty());
            }


            holder.d1.setText(holder.productObj.getD1() + "");

            result = SDUtil.convertToDouble("" + holder.productObj.getD1());
            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.caseqty.setEnabled(false);
            } else {
                holder.caseqty.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.pieceqty.setEnabled(false);
            } else {
                holder.pieceqty.setEnabled(true);
            }

            if (bmodel.configurationMasterHelper.SHOW_GLOBAL_DISOCUNT_DIALOG) {
                holder.d1.setEnabled(false);
            }

            holder.da.setText(holder.productObj.getDA() + "");

            TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }
            return (row);
        }
    }

    class ViewHolder {

        ProductMasterBO productObj;
        String productId, productCode, pname;
        int caseSize, stockInHand;// product id
        TextView psname, mrp, caseqty, pieceqty, outerQty, min_max, discounted_price, unit_price;
        EditText d1, da;
        int ref;
        TextView total;
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
                    getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    public double discountAmountCalc(ProductMasterBO productBO, double amount) {

        /* apply batchwise discount starts */
        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (productBO.getBatchwiseProductCount() > 0) {
                double total = bmodel.batchAllocationHelper
                        .updateDiscontBatchwiseAmt(productBO, amount);
                updateDiscountedOrderValue();
                return total;
            }
        }
        /* apply batchwise discount ends */

        int totalQty = productBO.getOrderedPcsQty()
                + productBO.getOrderedCaseQty()
                * productBO.getCaseSize()
                + productBO.getOrderedOuterQty()
                * productBO.getOutersize();

        productBO.setApplyValue(SDUtil.formatAsPerCalculationConfig(totalQty * amount));
        double total = 0;
        if (bmodel.configurationMasterHelper.IS_DISCOUNT_PRICE_PER) {
            if (productBO.getApplyValue() > productBO.getLineValueAfterSchemeApplied() * (bmodel.configurationMasterHelper.DISCOUNT_PRICE_PER / 100))
                total = -1;//to avoid entering greater than given percentage value
            else
                total = productBO.getLineValueAfterSchemeApplied() - productBO.getApplyValue();
        } else {
            total = productBO.getLineValueAfterSchemeApplied() - productBO.getApplyValue();
        }

        updateDiscountedOrderValue();
        return total;

    }

    public double discountcalc(ProductMasterBO productBO, double sum) {

        /* apply batchwise discount starts */
        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (productBO.getBatchwiseProductCount() > 0) {
                double total = bmodel.batchAllocationHelper
                        .updateDiscontBatchwise(productBO, sum);
//				productBO.setNetValue(total);
                updateDiscountedOrderValue();
                return total;
            }
        }
        /* apply batchwise discount ends */

        double line_total_price = productBO.getLineValueAfterSchemeApplied();
        productBO.setApplyValue(SDUtil.formatAsPerCalculationConfig(line_total_price * sum / 100));

        double total = productBO.getLineValueAfterSchemeApplied() - productBO.getApplyValue();

//		productBO.setNetValue(total);

        updateDiscountedOrderValue();

        return total;
    }

    private void updateDiscountedOrderValue() {

        double value = 0;
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        int siz = items.size();
        mylist = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {
                double totalProductValue = 0.0;
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && ret.getBatchwiseProductCount() > 0) {
                    ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(ret.getProductID());
                    if (batchList != null) {
                        for (ProductMasterBO productBO : batchList) {
                            int totalQty = productBO.getOrderedPcsQty()
                                    + productBO.getOrderedCaseQty()
                                    * productBO.getCaseSize()
                                    + productBO.getOrderedOuterQty()
                                    * productBO.getOutersize();
                            if (totalQty > 0)
                                totalProductValue = totalProductValue + (productBO.getLineValueAfterSchemeApplied() - productBO.getApplyValue());
                        }
                    }
                } else {
                    totalProductValue = ret.getLineValueAfterSchemeApplied() - ret.getApplyValue();
                }

                value = value + totalProductValue;
            }
        }
        totalval.setText(bmodel.formatValue(value) + "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (result > 0) {
                setResult(1);
                finish();
            } else {
                finish();
                discountHelper.clearDiscountQuantity();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
