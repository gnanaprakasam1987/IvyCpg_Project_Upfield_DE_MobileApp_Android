package com.ivy.cpg.view.orderfullfillment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFullfillmentBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by nivetha.s on 12-08-2015.
 */
public class OrderFullfillment extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private ArrayList<OrderFullfillmentBO> retailer;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    static Button dateBtn;
    private static String outPutDateFormat;
    private OrderFullfillmentHelper orderFullfillmentHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_fullfillment);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        orderFullfillmentHelper = OrderFullfillmentHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title to toolbar
        getSupportActionBar().setTitle(
                bmodel.getOrderfullfillmentbo().getRetailername());
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        outPutDateFormat = bmodel.configurationMasterHelper.outDateFormat;

        ListView listView = findViewById(R.id.listView1);
        listView.setCacheColorHint(0);

        bmodel.reasonHelper.downloadOrderFullfillmentReason();
        retailer = orderFullfillmentHelper.downloadOrderFullfillment(bmodel.getOrderfullfillmentbo().getRetailerid());
        OrderfullfillmentAdapter mSchedule = new OrderfullfillmentAdapter(
                retailer);
        // mSchedule.notifyDataSetChanged();
        listView.setAdapter(mSchedule);

        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                onSave();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds ofitems to the action bar if it is present.
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
        int id = item.getItemId();

        if (id == android.R.id.home) {

            startActivity(new Intent(OrderFullfillment.this,
                    OrderFullfillmentRetailerSelection.class));
            finish();
            return true;
        } else if (id == R.id.menu_save) {
            onSave();
            return true;
        }

        return false;
    }

    private void onSave() {
        if (hasNoData()) {
            Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.no_data_tosave), Toast.LENGTH_SHORT).show();
        } else if (isPartial()) {
            if (isPartial()) {
                if (hasRejectedDataForNoStatus()) {
                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                } else {
                    if (hasPartialist()) {
                        if (hasFullfill()) {
                            if (hasFullfillData()) {
                                if (hasRejected()) {
                                    if (hasRejectedData()) {
                                        orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                                        Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else
                                        Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                                } else {
                                    orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else
                                Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_delivery_date), Toast.LENGTH_SHORT).show();
                        } else {
                            if (hasRejected()) {
                                if (hasRejectedData()) {
                                    orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else
                                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                            } else {
                                orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                                Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    } else
                        Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.please_save_partial_details), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (hasFullfill()) {
                if (hasFullfillData()) {
                    if (hasRejected()) {
                        if (hasRejectedData()) {
                            orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                            Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        } else
                            Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                    } else {
                        orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                        Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else
                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_delivery_date), Toast.LENGTH_SHORT).show();
            } else {

                if (hasRejected() && hasRejectedDataForNoStatus()) {
                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                } else {
                    if (hasRejected()) {
                        if (hasRejectedData()) {
                            orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                            Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        } else
                            Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                    } else {

                        orderFullfillmentHelper.SaveOrderFullfillment(retailer);
                        Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
                        finish();


                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<ReasonMaster> reason = bmodel.reasonHelper.getOrderFullfillmentReason();

        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, reason);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
    }

    private ArrayList<OrderFullfillmentBO> ofitems;

    class OrderfullfillmentAdapter extends ArrayAdapter<OrderFullfillmentBO> {


        private OrderfullfillmentAdapter(ArrayList<OrderFullfillmentBO> items) {
            super(OrderFullfillment.this, R.layout.order_fullfillment_list_item, items);
            ofitems = items;
        }

        public OrderFullfillmentBO getItem(int position) {
            return ofitems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return ofitems.size();
        }

        public @NonNull
        View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            //retailerObj = (OrderFullfillmentBO) ofitems.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.order_fullfillment_list_item, parent, false);

                holder = new ViewHolder();

                holder.orderno = convertView.findViewById(R.id.orderno);
                holder.value = convertView.findViewById(R.id.value);
                holder.lines = convertView.findViewById(R.id.lines);

                holder.deliverydate = convertView.findViewById(R.id.deliverydate);

                holder.reason = convertView.findViewById(R.id.spinner);
                holder.reason.setAdapter(spinnerAdapter);

                holder.rej = convertView.findViewById(R.id.rejected);
                holder.partial = convertView.findViewById(R.id.pfullfilled);
                holder.fullfilled = convertView.findViewById(R.id.fullfilled);

                holder.rej.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                        if (isChecked) {

                            holder.retailerObjectHolder.setStatus("R");

                            holder.partial.setChecked(false);
                            holder.fullfilled.setChecked(false);

                            holder.deliverydate.setEnabled(false);
                            holder.retailerObjectHolder.setDeliverydate(getResources().getString(R.string.select_date));
                            holder.deliverydate.setText(holder.retailerObjectHolder.getDeliverydate());

                            holder.reason.setEnabled(true);

                            holder.orderno.setClickable(false);
                            holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());

                        }
                    }
                });
                holder.partial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {

                            holder.retailerObjectHolder.setStatus("P");

                            holder.rej.setChecked(false);
                            holder.fullfilled.setChecked(false);

                            holder.deliverydate.setEnabled(true);
                            holder.reason.setEnabled(true);

                            holder.orderno.setClickable(true);

                            SpannableString str = new SpannableString(holder.orderno
                                    .getText().toString());
                            str.setSpan(new UnderlineSpan(), 0, str.length(),
                                    Spanned.SPAN_PARAGRAPH);
                            str.setSpan(new ForegroundColorSpan(Color.BLUE), 0,
                                    str.length(), 0);
                            holder.orderno.setText(str);

                        }
                    }
                });
                holder.fullfilled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {

                            holder.retailerObjectHolder.setStatus("F");

                            holder.partial.setChecked(false);
                            holder.rej.setChecked(false);

                            holder.deliverydate.setEnabled(true);

                            holder.reason.setEnabled(false);
                            holder.retailerObjectHolder.setReasonid("0");
                            holder.reason.setSelection(getReasonName(holder.retailerObjectHolder.getReasonId()));

                            holder.orderno.setClickable(false);
                            holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());

                        }
                    }
                });
                holder.deliverydate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        dateBtn = holder.deliverydate;
                        dateBtn.setTag(holder.retailerObjectHolder);

                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getSupportFragmentManager(),
                                "datePicker1");
                    }
                });
                holder.orderno.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {
                            if (holder.retailerObjectHolder.getDeliverydate() != null && !holder.retailerObjectHolder.getDeliverydate().equalsIgnoreCase(getResources().getString(R.string.select_date))) {
                                if (!holder.reason.getSelectedItem().toString().equalsIgnoreCase(getResources().getString(R.string.select_reason))) {
                                    Intent myIntent = new Intent(OrderFullfillment.this, PartialFullfillment.class);
                                    myIntent.putExtra("orderid", holder.retailerObjectHolder.getOrderId());
                                    startActivity(myIntent);
                                } else {
                                    Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(OrderFullfillment.this, getResources().getString(R.string.select_delivery_date), Toast.LENGTH_SHORT).show();
                            }
                            // }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                });
                holder.reason
                        .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(
                                    AdapterView<?> parent, View view,
                                    int position, long id) {
                                ReasonMaster rbo = (ReasonMaster) parent
                                        .getItemAtPosition(position);

                                holder.retailerObjectHolder.setReasonid(rbo.getReasonID());
                            }

                            public void onNothingSelected(
                                    AdapterView<?> parent) {
                            }
                        });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            try {

                holder.retailerObjectHolder = ofitems.get(position);

                holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());
                holder.value.setText(String.valueOf(holder.retailerObjectHolder.getValue()));
                holder.lines.setText(String.valueOf(holder.retailerObjectHolder.getLineval()));

                if (holder.retailerObjectHolder.getStatus().equalsIgnoreCase("P")) {
                    holder.reason.setEnabled(true);
                    holder.deliverydate.setEnabled(true);
                    holder.partial.setChecked(true);

                    holder.orderno.setClickable(true);

                    SpannableString str = new SpannableString(holder.orderno
                            .getText().toString());
                    str.setSpan(new UnderlineSpan(), 0, str.length(),
                            Spanned.SPAN_PARAGRAPH);
                    str.setSpan(new ForegroundColorSpan(Color.BLUE), 0,
                            str.length(), 0);
                    holder.orderno.setText(str);

                } else if (holder.retailerObjectHolder.getStatus().equalsIgnoreCase("R")) {
                    holder.reason.setEnabled(true);
                    holder.deliverydate.setEnabled(false);
                    holder.rej.setChecked(true);

                    holder.orderno.setClickable(false);
                    holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());
                } else if (holder.retailerObjectHolder.getStatus().equalsIgnoreCase("F")) {
                    holder.reason.setEnabled(false);
                    holder.deliverydate.setEnabled(true);
                    holder.fullfilled.setChecked(true);

                    holder.orderno.setClickable(false);
                    holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());
                } else {
                    holder.reason.setEnabled(true);
                    holder.deliverydate.setEnabled(false);

                    holder.partial.setChecked(false);
                    holder.rej.setChecked(false);
                    holder.fullfilled.setChecked(false);

                    holder.orderno.setClickable(false);
                    holder.orderno.setText(holder.retailerObjectHolder.getOrderNo());
                }

                holder.deliverydate.setText(holder.retailerObjectHolder.getDeliverydate());
                holder.reason.setSelection(getReasonName(holder.retailerObjectHolder.getReasonId()));

                TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
                if (position % 2 == 0) {
                    convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
                } else {
                    convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
                }

            } catch (Exception e) {
                Commons.printException(e);
            }
            return convertView;
        }

        class ViewHolder {
            private OrderFullfillmentBO retailerObjectHolder;

            private TextView orderno, value, lines;
            private Spinner reason;
            private Button deliverydate;
            private RadioButton rej, partial, fullfilled;
        }

    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public @NonNull
        Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);

            OrderFullfillmentBO po;

            po = (OrderFullfillmentBO) dateBtn.getTag();
            if (this.getTag().equals("datePicker1")) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_LONG).show();
                    po.setDeliverydate(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                    dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    po.setDeliverydate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            }
        }

    }

    private boolean isPartial() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("P"))
                return true;
        }
        return false;
    }

    private boolean hasPartialist() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("P"))
                if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getPartialdetailslist().size() > 0)
                    return true;
        }
        return false;
    }

    private boolean hasFullfillData() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("F")) {
                if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getDeliverydate() == null)
                    return false;
            }
        }
        return true;
    }

    private boolean hasFullfill() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("F")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRejected() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("R")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRejectedData() {
        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("R")) {
                if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getReasonId() == null)
                    return false;
            }
        }
        return true;
    }

    private boolean hasRejectedDataForNoStatus() {
        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getReasonId() == null || !orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getReasonId().equals("0"))
                return false;

        }
        return true;
    }

    public int getReasonName(String reasonid) {
        try {
            if (reasonid == null)
                return 0;

            for (int i = 0; i < bmodel.reasonHelper.getOrderFullfillmentReason().size(); i++) {
                Commons.print("reasonid=" + bmodel.reasonHelper.getOrderFullfillmentReason().get(i).getReasonID() + " " + reasonid);
                if (reasonid != null)
                    if (bmodel.reasonHelper.getOrderFullfillmentReason().get(i).getReasonID().equals(reasonid)) {

                        return i;
                    }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

    private boolean hasNoData() {

        for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillmentHeader().size(); i++) {
            if (!orderFullfillmentHelper.getOrderFullfillmentHeader().get(i).getStatus().equalsIgnoreCase("D"))
                return false;
        }
        return true;
    }
}
