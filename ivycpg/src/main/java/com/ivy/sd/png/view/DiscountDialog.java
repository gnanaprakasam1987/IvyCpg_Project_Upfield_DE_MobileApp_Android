package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InitiativeHeaderBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

public class DiscountDialog extends Dialog implements OnClickListener {
    private BusinessModel bmodel;
    private Context context;
    private Button back,saveButton;
    private TextView totalval, oldTotalValue;
    private ListView lvwplist;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY, D1;
    private String append = "",  d1 = "0";
    private OrderSummary initAct;
    double sum = 0;
    private double totalOrderValue;
    OnDismissListener disListner;

    public DiscountDialog(final Context context, InitiativeHeaderBO initHeaderBO,
                          OnDismissListener discountDismissListener) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initAct = (OrderSummary) context;

        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_discount, null);
        setContentView(ll);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        // setContentView(R.layout.initiativedialog);
        setCancelable(true);
        disListner = discountDismissListener;

        bmodel = (BusinessModel) context.getApplicationContext();
        back = (Button) findViewById(R.id.closeButton);
        saveButton=(Button)findViewById(R.id.saveButton);
        back.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        saveButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        totalval = (TextView) findViewById(R.id.totalValue);
        oldTotalValue = (TextView) findViewById(R.id.oldTotalValue);
        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                R.id.old_order_volume).getTag()) != null) {
            ((TextView) findViewById(R.id.old_order_volume))
                    .setText(bmodel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.old_order_volume)
                                    .getTag()));
        }
        D1 = (EditText) findViewById(R.id.d1);

        ((TextView) findViewById(R.id.titlebar)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTotalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.minmax)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvValuetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        totalval.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        lvwplist = (ListView) findViewById(R.id.list);
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
            ((TextView) findViewById(R.id.d1title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }

        if (!bmodel.configurationMasterHelper.SHOW_DA)
            findViewById(R.id.datitle).setVisibility(View.GONE);
        else {
            ((TextView) findViewById(R.id.datitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }




        if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
            bmodel.productHelper.downloadDiscountRange();
        } else {
            ((TextView) findViewById(R.id.minmax)).setVisibility(View.GONE);
        }

        updateOrderTable();

        D1.setText(d1);


        D1.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                QUANTITY = D1;
                int inType = D1.getInputType();
                D1.setInputType(InputType.TYPE_NULL);
                D1.onTouchEvent(event);
                D1.setInputType(inType);
                return true;
            }
        });


        D1.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {


                String qty = s.toString();

                double i = SDUtil.convertToDouble(qty);

                sum = 0;
                //if (i != 0 || i != 0.0) {

                sum = SDUtil.convertToDouble(D1.getText().toString());
                if (sum > bmodel.configurationMasterHelper.discount_max) {
                    Toast.makeText(context, "value exceeded",
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


    public void onBackPressed() {
        // do something on back.
        return;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        if (b == back) {

            disListner.onDismiss(this);
            if(d1==null)
                bmodel.productHelper.clearDiscountQuantity();
            else
                this.initAct.onResume();
            // dismiss();
        }
        if (b == saveButton) {

            disListner.onDismiss(this);
            this.initAct.onResume();
            // dismiss();
        }
    }


    private boolean validateFromRange() {
        for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
            if (bo.getOrderedPcsQty() > 0 || bo.getOrderedCaseQty() > 0
                    || bo.getOrderedOuterQty() > 0) {
                if (bo.getGroupid() == 0 && !bo.isCbsihAvailable()) {
                    if (bo.getFrom_range() > 0 && ((bo.getD1() > 0 && bo.getD1() < bo.getFrom_range() && bmodel.configurationMasterHelper.SHOW_D1) || (bo.getD2() > 0 && bo.getD2() < bo.getFrom_range() && bmodel.configurationMasterHelper.SHOW_D2) || (bo.getD3() > 0 && bo.getD3() < bo.getFrom_range() && bmodel.configurationMasterHelper.SHOW_D3))) {
                        return false;
                    }
                }
            }
            /*else if((bo.getD1()>bo.getTo_range()&&bmodel.configurationMasterHelper.SHOW_D1)||(bo.getD2()>bo.getTo_range()&&bmodel.configurationMasterHelper.SHOW_D2)||(bo.getD3()>bo.getTo_range()&&bmodel.configurationMasterHelper.SHOW_D3)){
                return false;
            }*/
        }

        return true;
    }


    private void updateDiscount(double discount, int j) {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    context.getResources().getString(
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
                    context.getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<ProductMasterBO>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {

                if (ret.getD1() == 0) {
                    d1 = "0";
                } else {
                    d1 = ret.getD1() + "";
                }
                double temp = 0;
                /*temp = (ret.getOrderedPcsQty() * ret.getSrp())
                        + (ret.getOrderedCaseQty() * ret.getCsrp())
						+ (ret.getOrderedOuterQty() * ret.getOsrp());
				ret.setDiscount_order_value(temp);*/
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && ret.getBatchwiseProductCount() > 0) {
                    ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(ret.getProductID());
                    if (batchList != null) {
                        for (ProductMasterBO batchProductBO : batchList) {
                            batchProductBO.setDiscount_order_value(batchProductBO.getDiscount_order_value() + batchProductBO.getApplyValue());
                        }
                    }

                } else {
                    ret.setDiscount_order_value(ret.getDiscount_order_value() + ret.getApplyValue());
                }
                totalOrderValue = totalOrderValue + ret.getDiscount_order_value();

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
        MyAdapter mSchedule = new MyAdapter(context, mylist);
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
                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.caseqty = (TextView) row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = (TextView) row
                        .findViewById(R.id.orderQTYinpiece);
                holder.d1 = (EditText) row.findViewById(R.id.d1);
                holder.da = (EditText) row.findViewById(R.id.da);

                holder.mrp = (TextView) row.findViewById(R.id.mrp);
                holder.total = (TextView) row.findViewById(R.id.total);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerorderQTYinCase);
                holder.min_max = (TextView) row
                        .findViewById(R.id.min_max);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.pieceqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.d1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.da.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.min_max.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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
                                Toast.makeText(context, "value exceeded",
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
                        double i = SDUtil.convertToDouble(qty);

                        if (i != 0) {
                            holder.productObj.setDA(i);

                            if (holder.productObj.getD1() > 0) {
                                holder.d1.setText("0");
                                holder.productObj.setD1(0);
                            }

                            double tot = discountAmountCalc(holder.productObj,
                                    i);
                            if (tot >= 0)
                                holder.total.setText(bmodel.formatValue(tot));
                            else {
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                holder.da.setText(qty);
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

                holder.d1.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.d1;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.d1.getInputType();
                        holder.d1.setInputType(InputType.TYPE_NULL);
                        holder.d1.onTouchEvent(event);
                        holder.d1.setInputType(inType);
                        return true;
                    }
                });


                holder.da.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.da;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.da.getInputType();
                        holder.da.setInputType(InputType.TYPE_NULL);
                        holder.da.onTouchEvent(event);
                        holder.da.setInputType(inType);
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

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.mrp.setText("Price: "
                    + bmodel.formatValue(holder.productObj.getSrp()) + "");


            holder.outerQty
                    .setText(context.getResources().getString(R.string.item_outer) + ":" + holder.productObj.getOrderedOuterQty() + "");
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.caseqty.getTag()) != null)
                    holder.caseqty.setText(bmodel.labelsMasterHelper
                            .applyLabels(holder.caseqty.getTag()) + ":" + holder.productObj.getOrderedCaseQty());
                else
                    holder.caseqty.setText(context.getResources().getString(R.string.item_case) + ":" + holder.productObj.getOrderedCaseQty());

            } catch (Exception e) {
                Commons.printException(e);
                holder.caseqty.setText(context.getResources().getString(R.string.item_case) + ":" + holder.productObj.getOrderedCaseQty());
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.pieceqty.getTag()) != null)
                    holder.pieceqty.setText(bmodel.labelsMasterHelper
                            .applyLabels(holder.pieceqty.getTag()) + ":" + holder.productObj.getOrderedPcsQty());
                else
                    holder.pieceqty.setText(context.getResources().getString(R.string.item_piece) + ":" + holder.productObj.getOrderedPcsQty());

            } catch (Exception e) {
                Commons.printException(e);
                holder.pieceqty.setText(context.getResources().getString(R.string.item_piece) + ":" + holder.productObj.getOrderedPcsQty());
            }


            holder.d1.setText(holder.productObj.getD1() + "");

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
        TextView psname, mrp, caseqty, pieceqty, outerQty, min_max;
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
                    context.getResources().getString(
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

        productBO.setApplyValue(totalQty * amount);

        double total = productBO.getDiscount_order_value() - productBO.getApplyValue();
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
//				productBO.setDiscount_order_value(total);
                updateDiscountedOrderValue();
                return total;
            }
        }
        /* apply batchwise discount ends */

        double line_total_price = (productBO.getOrderedCaseQty() * productBO
                .getCsrp())
                + (productBO.getOrderedPcsQty() * productBO.getSrp())
                + (productBO.getOrderedOuterQty() * productBO.getOsrp());
        productBO.setApplyValue(line_total_price * sum / 100);

        double total = productBO.getDiscount_order_value() - productBO.getApplyValue();

//		productBO.setDiscount_order_value(total);

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
                                totalProductValue = totalProductValue + (productBO.getDiscount_order_value() - productBO.getApplyValue());
                        }
                    }
                } else {
                    totalProductValue = ret.getDiscount_order_value() - ret.getApplyValue();
                }

                value = value + totalProductValue;
            }
        }
        totalval.setText(bmodel.formatValue(value) + "");
    }
}
