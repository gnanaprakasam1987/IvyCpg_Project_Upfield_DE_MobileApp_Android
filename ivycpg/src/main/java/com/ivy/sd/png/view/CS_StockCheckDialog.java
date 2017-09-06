package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.bo.CS_StockReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 6/23/2017.
 */

public class CS_StockCheckDialog extends Dialog implements View.OnClickListener {

    private BusinessModel bmodel;
    private OnDismissListener disListner;
    private Context context;
    private EditText QUANTITY;
    private String append = "";
    ArrayList<CS_StockReasonBO> lstReasons;
    private InputMethodManager inputManager;
    ListView listView;
    TextView tv_titleBar;
    private int facingQty;

    public CS_StockCheckDialog(Context context, ArrayList<CS_StockReasonBO> reasons, String productNmae, int faQty) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;

        if (getWindow() != null) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        setContentView(R.layout.dialog_cs_stock_check);

        bmodel = (BusinessModel) context.getApplicationContext();
        lstReasons = reasons;
        facingQty = faQty;
        listView = (ListView) findViewById(R.id.lvwplist);
        tv_titleBar = (TextView) findViewById(R.id.titlebar);
        tv_titleBar.setText(productNmae);
        Button doneBtn = (Button) findViewById(R.id.btnDone);
        doneBtn.setOnClickListener(this);

        MyAdapter adapter = new MyAdapter(lstReasons);
        listView.setAdapter(adapter);

        intialize();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnDone) {
            if (!isVarianceDone())
                Toast.makeText(context, context.getResources().getString(R.string.enter_variance_reason_for_all_qty),
                        Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            if (view.getId() == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                tv = s + "";
                QUANTITY.setText(tv);
            } else {
                Button ed = (Button) findViewById(view.getId());
                append = ed.getText().toString();
                numberPressed(ed);
            }
        }

    }

    /*
    * return boolean value compare with total variance of facing qty with total psQtyCount
    * facingQty==psQtycount:true?false;
     */
    private boolean isVarianceDone() {
        int psQtyCount = 0;
        if (lstReasons != null)
            for (CS_StockReasonBO csBo : lstReasons) {
                if (csBo.getPieceQty() > 0)
                    psQtyCount += csBo.getPieceQty();
            }
        if (facingQty > 0 && facingQty == psQtyCount)
            return true;
        else
            return false;
    }

    class MyAdapter extends ArrayAdapter<CS_StockReasonBO> {
        ArrayList<CS_StockReasonBO> items;
        CS_StockReasonBO product1;

        MyAdapter(ArrayList<CS_StockReasonBO> vector) {
            super(context, R.layout.dialog_vanload_row, vector);
            this.items = vector;
        }

        public CS_StockReasonBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final CS_StockCheckDialog.ViewHolder holder;

            product1 = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_cs_stock, parent,
                        false);
                holder = new CS_StockCheckDialog.ViewHolder();

                holder.pieceQty = (EditText) row
                        .findViewById(R.id.productqtyPieces);

                holder.reason = (MaterialSpinner) row.findViewById(R.id.batchno);
                holder.reasonTextView = (TextView) row.findViewById(R.id.batchno1);

                holder.pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.reason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


                //  holder.pieceQty.clearFocus();


                holder.pieceQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();
                        if (!"".equals(qty)) {


                            if ((SDUtil.convertToInt(qty) + getTotalVarienceEntered(holder.productBO.getReasonID())) <= facingQty) {

                                holder.productBO.setPieceQty(SDUtil
                                        .convertToInt(qty));

                             /*   updateValue(SDUtil
                                        .convertToInt(qty), holder.reasonId);*/
                            } else {

                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                holder.productBO.setPieceQty(SDUtil
                                        .convertToInt(qty));
                                holder.pieceQty.setText(qty);

                                Toast.makeText(context, context.getResources().getString(R.string.maximum_count_is_exceed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TO DO Auto-generated method stub
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TO DO Auto-generated method stub
                    }
                });


                holder.pieceQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.pieceQty;
                        int inType = holder.pieceQty.getInputType();
                        holder.pieceQty.setInputType(InputType.TYPE_NULL);
                        holder.pieceQty.onTouchEvent(event);
                        holder.pieceQty.setInputType(inType);
                        holder.pieceQty.selectAll();
                        holder.pieceQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
                        return true;
                    }
                });

                row.setTag(holder);
            } else {
                holder = (CS_StockCheckDialog.ViewHolder) row.getTag();
            }

            holder.position = position;
            holder.productBO = product1;

            holder.reasonTextView.setText(holder.productBO.getReasonDesc());
            holder.pieceQty.setText(holder.productBO.getPieceQty() + "");

            return row;
        }
    }

    class ViewHolder {
        int position;
        CS_StockReasonBO productBO;
        MaterialSpinner reason;
        TextView reasonTextView;
        EditText pieceQty;
        String reasonId;
    }

    private void updateValue(int pQty, String reasonId) {
        try {
            if (pQty > 0 && !reasonId.equals("0")) {
                for (CS_StockReasonBO bo : lstReasons) {
                    if (bo.getReasonID().equals(reasonId)) {
                        bo.setPieceQty(pQty);
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    private int getTotalVarienceEntered(String reasonId) {
        int total = 0;
        try {
            for (CS_StockReasonBO bo : lstReasons) {
                if (!reasonId.equals(bo.getReasonID())) {
                    total += bo.getPieceQty();
                }
            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return total;
    }

    private void intialize() {
        Button calcone = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcone);
        Button calctwo = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calctwo);
        Button calcthree = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcthree);
        Button calcfour = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcfour);
        Button calcfive = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcfive);
        Button calcsix = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcsix);
        Button calcseven = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcseven);
        Button calceight = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calceight);
        Button calcnine = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcnine);
        Button calczero = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calczero);
        Button calcdot = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcdot);
        Button calcdel = (Button) (findViewById(R.id.keypad))
                .findViewById(R.id.calcdel);
        calcone.setOnClickListener(this);
        calctwo.setOnClickListener(this);
        calcthree.setOnClickListener(this);
        calcfour.setOnClickListener(this);
        calcfive.setOnClickListener(this);
        calcsix.setOnClickListener(this);
        calcseven.setOnClickListener(this);
        calceight.setOnClickListener(this);
        calcnine.setOnClickListener(this);
        calczero.setOnClickListener(this);
        calcdot.setOnClickListener(this);
        calcdel.setOnClickListener(this);
    }

    private String tv;

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            tv = QUANTITY.getText() + append;
            QUANTITY.setText(tv);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    context.getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                tv = s + "";
                QUANTITY.setText(tv);
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }

        }
    }


}
