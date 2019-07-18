package com.ivy.cpg.view.orderfullfillment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFullfillmentBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by nivetha.s on 13-08-2015.
 */
public class PartialFullfillment extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private String orderid = "";
    ArrayList<OrderFullfillmentBO> partial;
    private EditText QUANTITY;
    private String append = "";
    private OrderFullfillmentHelper orderFullfillmentHelper;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partial_fullfillment);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        orderFullfillmentHelper = OrderFullfillmentHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set title to toolbar
        getSupportActionBar().setTitle(getResources().getString(R.string.partial_fullfillment));
        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)

        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().getExtras() != null)
            orderid = getIntent().getExtras().getString("orderid");
        partial = orderFullfillmentHelper.downloadPartialFullfillment(orderid);
        ListView listView = findViewById(R.id.listView1);
        listView.setCacheColorHint(0);

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.caseTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
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
                Commons.printException(e);
            }
        }
        PartialFullfillmentAdapter mSchedule = new PartialFullfillmentAdapter(
                partial);
        listView.setAdapter(mSchedule);

        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                orderFullfillmentHelper.savePartialFullfillment(partial, orderid);
                Toast.makeText(PartialFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_fullfillment, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    OrderFullfillmentBO partialobj;

    class PartialFullfillmentAdapter extends ArrayAdapter<OrderFullfillmentBO> {

        private ArrayList<OrderFullfillmentBO> items;

        private PartialFullfillmentAdapter(ArrayList<OrderFullfillmentBO> items) {
            super(PartialFullfillment.this, R.layout.partial_fullfillment_list_item, items);
            this.items = items;
        }

        public OrderFullfillmentBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {
            try {
                final ViewHolder holder;
                partialobj = items.get(position);

                if (convertView == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    convertView = inflater.inflate(
                            R.layout.partial_fullfillment_list_item, parent, false);
                    holder = new ViewHolder();
                    holder.productname = convertView.findViewById(R.id.productnamecheckbox);
                    holder.pieceqty = convertView.findViewById(R.id.stock_and_order_listview_pcs_qty);
                    holder.caseqty = convertView.findViewById(R.id.stock_and_order_listview_case_qty);
                    holder.outerqty = convertView.findViewById(R.id.stock_and_order_listview_outer_case_qty);

                    // On/Off order case and pce
                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                        holder.caseqty.setVisibility(View.GONE);

                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        holder.pieceqty.setVisibility(View.GONE);

                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        holder.outerqty.setVisibility(View.GONE);
                    holder.productname
                            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(
                                        CompoundButton buttonView, boolean isChecked) {
                                    if (!isChecked) {
                                        holder.pieceqty.setEnabled(true);
                                        holder.caseqty.setEnabled(true);
                                        holder.outerqty.setEnabled(true);
                                        holder.pieceqty.setText("0");
                                        holder.caseqty.setText("0");
                                        holder.outerqty.setText("0");
                                    } else {


                                        holder.pieceqty.setEnabled(false);
                                        holder.caseqty.setEnabled(false);
                                        holder.outerqty.setEnabled(false);
                                    }
                                }

                            });
                    holder.pieceqty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            String qty;
                            if (!s.toString().equals("0")) {
                                qty = s.toString();
                                if (qty.length() > 0)
                                    holder.pieceqty.setSelection(qty.length());

                                holder.partilaobjectholder.setPieceqty(SDUtil.convertToInt(qty));
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }
                    });
                    holder.outerqty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            String qty;
                            if (!s.toString().equals("0")) {
                                qty = s.toString();
                                if (qty.length() > 0)
                                    holder.outerqty.setSelection(qty.length());
                                holder.partilaobjectholder.setOuterqty(SDUtil.convertToInt(qty));
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {

                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }
                    });

                    holder.caseqty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            String qty;
                            if (!s.toString().equals("0")) {
                                qty = s.toString();
                                if (qty.length() > 0)
                                    holder.caseqty.setSelection(qty.length());
                                Commons.print("qty" + qty);
                                holder.partilaobjectholder.setCaseqty(SDUtil.convertToInt(qty));
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }
                    });
                    holder.caseqty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {

                            QUANTITY = holder.caseqty;
                            QUANTITY.setTag(holder.partilaobjectholder);
                            int inType = holder.caseqty.getInputType();
                            holder.caseqty.setInputType(InputType.TYPE_NULL);
                            holder.caseqty.onTouchEvent(event);
                            holder.caseqty.setInputType(inType);
                            holder.caseqty.requestFocus();
                            if (holder.caseqty.getText().length() > 0)
                                holder.caseqty.setSelection(holder.caseqty.getText().length());
                            return true;
                        }
                    });
                    holder.outerqty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {

                            QUANTITY = holder.outerqty;
                            QUANTITY.setTag(holder.partilaobjectholder);
                            int inType = holder.outerqty.getInputType();
                            holder.outerqty.setInputType(InputType.TYPE_NULL);
                            holder.outerqty.onTouchEvent(event);
                            holder.outerqty.setInputType(inType);
                            holder.outerqty.requestFocus();
                            if (holder.outerqty.getText().length() > 0)
                                holder.outerqty.setSelection(holder.outerqty.getText().length());

                            return true;
                        }
                    });
                    holder.pieceqty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {

                            QUANTITY = holder.pieceqty;
                            QUANTITY.setTag(holder.partilaobjectholder);
                            int inType = holder.pieceqty.getInputType();
                            holder.pieceqty.setInputType(InputType.TYPE_NULL);
                            holder.pieceqty.onTouchEvent(event);
                            holder.pieceqty.setInputType(inType);
                            holder.pieceqty.requestFocus();
                            if (holder.pieceqty.getText().length() > 0)
                                holder.pieceqty.setSelection(holder.pieceqty.getText().length());
                            return true;
                        }
                    });

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();

                }
                try {
                    holder.partilaobjectholder = partialobj;
                    holder.productname.setText(holder.partilaobjectholder.getPname());
                    holder.pieceqty.setText(String.valueOf(holder.partilaobjectholder.getPieceqty()));
                    holder.caseqty.setText(String.valueOf(holder.partilaobjectholder.getCaseqty()));
                    holder.outerqty.setText(String.valueOf(holder.partilaobjectholder.getOuterqty()));

                } catch (Exception e) {
                    Commons.printException(e);
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            return convertView;
        }

        class ViewHolder {
            private OrderFullfillmentBO partilaobjectholder;

            private EditText caseqty, pieceqty, outerqty;
            private CheckBox productname;
        }

    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {

        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(String.valueOf(s));

            } else {
                Button ed = findViewById(vw.getId());
                append = ed.getText().toString();
                eff();

            }

        }
    }
}
