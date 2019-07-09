package com.ivy.cpg.view.van.manualvanload;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.Vector;


class ManualVanLoadDialog extends Dialog implements OnClickListener {
    private BusinessModel bmodel;
    private OnDismissListener disListner;
    private Context context;
    private LoadManagementBO product;
    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    private String[] batchno;
    private String tv;
//    private Button addLoadBtn,cancelLoadBtn;
//    private TextView headerText;


    ManualVanLoadDialog(Context context, LoadManagementBO productBO,
                        OnDismissListener vanloadDismissListener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;

        if (getWindow() != null) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        disListner = vanloadDismissListener;
        final ViewGroup nullParent = null;
        setContentView(R.layout.dialog_vanload);
        setCancelable(true);
        bmodel = (BusinessModel) context.getApplicationContext();
        Button back = findViewById(R.id.closeButton);
        Button addLoadBtn = findViewById(R.id.add_load);
        TextView headerText = findViewById(R.id.titlebar);
        back.setOnClickListener(this);
        addLoadBtn.setOnClickListener(this);
        product = productBO;
        intialize();
        inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);


        //hide and seek
        if (bmodel.configurationMasterHelper.SHOW_VANLOAD_LABELS) {

            if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OC) {
                findViewById(R.id.caseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.caseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.caseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.caseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OO) {
                findViewById(R.id.outercaseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outercaseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.outercaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outercaseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OP) {
                findViewById(R.id.pcsTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.pcsTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.pcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.pcsTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
        } else {
            findViewById(R.id.caseTitle).setVisibility(View.GONE);
            findViewById(R.id.outercaseTitle).setVisibility(View.GONE);
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        }


        ListView lvwplist = (ListView) findViewById(R.id.list);
        headerText.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        addLoadBtn.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

        MyAdapter mSchedule = new MyAdapter(product.getBatchlist());
        lvwplist.setAdapter(mSchedule);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.add_load) {
            disListner.onDismiss(this);
//            bmodel.showAlert(
//                    context.getResources().getString(
//                            R.string.please_select_item), 0);

        } else {
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                tv = s + "";
                QUANTITY.setText(tv);
            } else {
                Button ed = (Button) findViewById(v.getId());
                append = ed.getText().toString();
                numberPressed(ed);
            }
            updateValue();
        }

    }

    private void updateValue() {

        int pcsQty = 0;
        int caseQty = 0;
        int outerQty = 0;

        for (LoadManagementBO bo : product.getBatchlist()) {

            if (bo.getPieceqty() > 0 || bo.getCaseqty() > 0
                    || bo.getOuterQty() > 0) {

                pcsQty += bo.getPieceqty();
                caseQty += bo.getCaseqty();
                outerQty += bo.getOuterQty();
                Commons.print("batchno" + bo.getBatchNo());
            }

        }
        product.setPieceqty(pcsQty);
        product.setCaseqty(caseQty);
        product.setOuterQty(outerQty);

    }

    private void intialize() {
        Button calcone = (findViewById(R.id.keypad))
                .findViewById(R.id.calcone);
        Button calctwo = (findViewById(R.id.keypad))
                .findViewById(R.id.calctwo);
        Button calcthree = (findViewById(R.id.keypad))
                .findViewById(R.id.calcthree);
        Button calcfour = (findViewById(R.id.keypad))
                .findViewById(R.id.calcfour);
        Button calcfive = (findViewById(R.id.keypad))
                .findViewById(R.id.calcfive);
        Button calcsix = (findViewById(R.id.keypad))
                .findViewById(R.id.calcsix);
        Button calcseven = (findViewById(R.id.keypad))
                .findViewById(R.id.calcseven);
        Button calceight = (findViewById(R.id.keypad))
                .findViewById(R.id.calceight);
        Button calcnine = (findViewById(R.id.keypad))
                .findViewById(R.id.calcnine);
        Button calczero = (findViewById(R.id.keypad))
                .findViewById(R.id.calczero);
        Button calcdot = (findViewById(R.id.keypad))
                .findViewById(R.id.calcdot);
        Button calcdel = (findViewById(R.id.keypad))
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

    public String[] getbatchno(LoadManagementBO bo) {
        try {
            int size = 0;
            if (bo.getBatchnolist() != null)
                size = bo.getBatchnolist().size();

            if (size == 0) {
                batchno = new String[1];
                batchno[0] = "NA";
                return batchno;
            }
            batchno = new String[size];

            for (int i = 0; i < size; i++) {
                batchno[i] = bo.getBatchnolist().get(i).getBatchNo();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return batchno;
    }

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

            updateValue();
        }
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        Vector<LoadManagementBO> items;
        LoadManagementBO product1;

        MyAdapter(Vector<LoadManagementBO> vector) {
            super(context, R.layout.dialog_vanload_row, vector);
            this.items = vector;
        }

        public LoadManagementBO getItem(int position) {
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

            final ViewHolder holder;

            product1 = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_vanload_row, parent,
                        false);
                holder = new ViewHolder();
                holder.caseQty = row
                        .findViewById(R.id.productqtyCases);
                holder.pieceQty = row
                        .findViewById(R.id.productqtyPieces);
                holder.outerQty = row
                        .findViewById(R.id.outerproductqtyCases);
                holder.batchno = row.findViewById(R.id.batchno);

                holder.caseQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                holder.pieceQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                holder.outerQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                holder.batchno.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));


                holder.caseQty.clearFocus();
                holder.pieceQty.clearFocus();
                holder.outerQty.clearFocus();

                if (bmodel.configurationMasterHelper.SHOW_VANLOAD_LABELS) {
                    if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OC)
                        holder.caseQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OO)
                        holder.outerQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_VANLOAD_OP)
                        holder.pieceQty.setVisibility(View.GONE);
                } else {
                    holder.caseQty.setVisibility(View.GONE);
                    holder.pieceQty.setVisibility(View.GONE);
                    holder.outerQty.setVisibility(View.GONE);
                }

                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TO DO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TO DO Auto-generated method stub

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.outerQty.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            holder.productBO.setOuterQty(SDUtil
                                    .convertToInt(qty));
                            updateValue();
                        }

                    }
                });

                holder.pieceQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.pieceQty.setSelection(qty.length());
                        if (!"".equals(qty)) {
                            holder.productBO.setPieceqty(SDUtil
                                    .convertToInt(qty));
                            updateValue();
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

                holder.caseQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.caseQty.setSelection(qty.length());
                        if (!"".equals(qty)) {
                            holder.productBO.setCaseqty(SDUtil
                                    .convertToInt(qty));
                            updateValue();
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
                holder.caseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.caseQty;
                        int inType = holder.caseQty.getInputType();
                        holder.caseQty.setInputType(InputType.TYPE_NULL);
                        holder.caseQty.onTouchEvent(event);
                        holder.caseQty.setInputType(inType);
                        holder.caseQty.requestFocus();
                        if (holder.caseQty.getText().length() > 0)
                            holder.caseQty.setSelection(holder.caseQty.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.pieceQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.pieceQty;
                        int inType = holder.pieceQty.getInputType();
                        holder.pieceQty.setInputType(InputType.TYPE_NULL);
                        holder.pieceQty.onTouchEvent(event);
                        holder.pieceQty.setInputType(inType);
                        holder.pieceQty.requestFocus();
                        if (holder.pieceQty.getText().length() > 0)
                            holder.pieceQty.setSelection(holder.pieceQty.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.outerQty;
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        holder.outerQty.requestFocus();
                        if (holder.outerQty.getText().length() > 0)
                            holder.outerQty.setSelection(holder.outerQty.getText().length());
                        inputManager.hideSoftInputFromWindow(
                                QUANTITY.getWindowToken(), 0);
                        return true;
                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.position = position;
            holder.productBO = product1;
            ArrayAdapter spinnerAdapter = new ArrayAdapter<Object>(row.getContext(),
                    R.layout.spinner_blacktext_layout, getbatchno(product1));
            spinnerAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            holder.batchno.setAdapter(spinnerAdapter);
            if (getbatchno(product1).length > 1)
                holder.batchno.setSelection(getBatchNoPosition(holder.productBO.getBatchNo(), getbatchno(product1)));
            holder.batchno
                    .setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int arg2, long arg3) {

                            holder.productBO.setBatchNo(holder.batchno
                                    .getSelectedItem().toString());
                            Commons.print("num"
                                    + holder.batchno.getSelectedItem()
                                    .toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TO DO Auto-generated method stub

                        }
                    });
            tv = holder.productBO.getCaseqty() + "";
            holder.caseQty.setText(tv);
            tv = holder.productBO.getPieceqty() + "";
            holder.pieceQty.setText(tv);
            tv = holder.productBO.getOuterQty() + "";
            holder.outerQty.setText(tv);

            // Disable the User Entry if UomID is Zero
            if (product.getPiece_uomid() == 0 || !product.isPieceMapped()) {
                holder.pieceQty.setEnabled(false);
            } else {
                holder.pieceQty.setEnabled(true);
            }
            if (product.getdUomid() == 0 || !product.isCaseMapped()) {
                holder.caseQty.setEnabled(false);
            } else {
                holder.caseQty.setEnabled(true);
            }
            if (product.getdOuonid() == 0 || !product.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            return row;
        }
    }

    class ViewHolder {
        int position;
        LoadManagementBO productBO;
        MaterialSpinner batchno;
        EditText pieceQty;
        EditText caseQty;
        EditText outerQty;
    }

    private int getBatchNoPosition(String batchNo, String[] batchnoList) {
        int position = 0, setPos = 0;
        for (String temp : batchnoList) {
            if (temp.equals(batchNo))
                setPos = position;
            position++;
        }
        return setPos;
    }
}
