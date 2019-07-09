package com.ivy.cpg.view.order.moq;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.Vector;

public class MOQHighlightActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private BusinessModel bmodel;
    private EditText QUANTITY;
    public InputMethodManager inputManager;

    public static final int MOQ_RESULT_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moq_highlight_dialog);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null ) {

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayShowTitleEnabled(false);
//            // Used to on / off the back arrow icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            setScreenTitle(getResources().getString(R.string.minimum_order_required));
        }

        if (getWindow() != null)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        inputManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(new View(this).getWindowToken(), 0);

        ListView lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        Vector<ProductMasterBO> productBoRfield = new Vector<>();
        Vector<ProductMasterBO> productMstBo = bmodel.productHelper.getProductMaster();

        int count = productMstBo.size();
        for (int i = 0; i < count; i++) {
            ProductMasterBO product = productMstBo.elementAt(i);
            if (product.getOrderedPcsQty() > 0) {

                if (!TextUtils.isEmpty(product.getRField1())) {
                    int res = SDUtil.convertToInt(product.getRField1());
                    if (product.getOrderedPcsQty() % res != 0)
                        productBoRfield.add(product);
                }
            }

        }

        MOQHighlightAdaper mSchedule = new MOQHighlightAdaper(productBoRfield);
        lvwplist.setAdapter(mSchedule);

        Button save = findViewById(R.id.btn_next);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_next) {
            setResult(1);
            finish();
        }
    }

    private class MOQHighlightAdaper extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;

        private MOQHighlightAdaper(Vector<ProductMasterBO> items) {
            super(bmodel,
                    R.layout.moq_highlight_dialog_listview, items);
            this.items = items;
        }

        @NonNull
        @Override
        @SuppressLint({"RestrictedApi", "SetTextI18n", "ClickableViewAccessibility"})
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            final ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                LayoutInflater inflater = getLayoutInflater();

                //Configuration based row rendering
                row = inflater.inflate(
                        R.layout.moq_highlight_dialog_listview, parent,
                        false);
                holder = new ViewHolder();
                holder.productBo = product;
                holder.productNameTxt = row
                        .findViewById(R.id.orderPRODNAME);
                holder.orderQTYinpiece = row
                        .findViewById(R.id.orderQTYinpiece);

                holder.rField1Txt = row
                        .findViewById(R.id.rField1_qty);

                holder.productNameTxt.setTypeface(FontUtils.getFontRoboto(MOQHighlightActivity.this, FontUtils.FontType.REGULAR));
                holder.rField1Txt.setTypeface(FontUtils.getFontRoboto(MOQHighlightActivity.this, FontUtils.FontType.REGULAR));

                holder.productNameTxt.setText("" + product.getProductShortName());
                holder.orderQTYinpiece.setText(getString(R.string.multiple_of) + " " + product.getRField1());
                holder.rField1Txt.setText("" + product.getOrderedPcsQty());

                holder.rField1Txt.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.rField1Txt;
                        QUANTITY.setTag(holder.productBo);
                        int inType = holder.rField1Txt.getInputType();
                        holder.rField1Txt.setInputType(InputType.TYPE_NULL);
                        holder.rField1Txt.onTouchEvent(event);
                        holder.rField1Txt.setInputType(inType);
                        holder.rField1Txt.setSelection(holder.rField1Txt.length());
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
                        return true;
                    }
                });


                holder.rField1Txt.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        int res = SDUtil.convertToInt(product.getRField1());
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.rField1Txt.setSelection(qty.length());
                        Commons.print("qty" + qty + "res" + res);

                        if (!"".equals(qty)) {
                            Commons.print("Value" + SDUtil.convertToInt(qty) % res);
                            holder.productBo.setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                            double tot = (holder.productBo.getOrderedCaseQty() * holder.productBo
                                    .getCsrp())
                                    + (holder.productBo.getOrderedPcsQty() * holder.productBo
                                    .getSrp())
                                    + (holder.productBo.getOrderedOuterQty() * holder.productBo
                                    .getOsrp());
                            Commons.print("tot" + tot);
                            holder.productBo.setTotalamount(tot);
                        }

                    }
                });

                row.setTag(holder);
            } else
                holder = (ViewHolder) row.getTag();

            return row;
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

    }

    public class ViewHolder {
        ProductMasterBO productBo;
        TextView productNameTxt, orderQTYinpiece;
        EditText rField1Txt;
    }

    @SuppressLint("SetTextI18n")
    public void numberPressed(View vw) {


        if (QUANTITY == null) {
            Toast.makeText(MOQHighlightActivity.this, getResources().getString(R.string.please_select_item), Toast.LENGTH_SHORT).show();
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                String enterText = QUANTITY.getText().toString();
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            QUANTITY.setText(s + "");
                        } else {
                            s = s / 10;

                            QUANTITY.setText(splitValue[0] + "." + s);
                        }


                    } catch (ArrayIndexOutOfBoundsException e) {
                        QUANTITY.setText(SDUtil.convertToInt(enterText) + "");
                    }


                } else {

                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(s + "");

                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }

            } else {
                Button ed = findViewById(vw.getId());
                String append = ed.getText().toString();
                eff(append);

            }

        }
    }

    public void eff(String append) {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
