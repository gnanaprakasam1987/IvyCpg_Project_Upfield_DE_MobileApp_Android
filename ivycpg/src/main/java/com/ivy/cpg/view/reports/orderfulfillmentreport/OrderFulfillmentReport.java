package com.ivy.cpg.view.reports.orderfulfillmentreport;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.orderfullfillment.OrderFullfillmentHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFulfillmentReportBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by anandasir on 25/9/18.
 */

public class OrderFulfillmentReport extends IvyBaseFragment implements View.OnClickListener {

    private static ListView lstOrderList;
    private static BusinessModel bmodel;
    private static Button orderfulfillmentdate;

    String currentDate = "";

    private static Context context;
    private static OrderfullfillmentAdapter adapter;
    private static ArrayList<OrderFulfillmentReportBO> orderFulfillmentReportList;
    private static OrderFullfillmentHelper orderFullfillmentHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_order_fulfillment,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        orderFullfillmentHelper = OrderFullfillmentHelper.getInstance(getActivity());
        view.findViewById(R.id.header).setVisibility(View.GONE);
        orderfulfillmentdate = (Button) view.findViewById(R.id.orderfulfillmentDate);
        orderfulfillmentdate.setOnClickListener(this);

        lstOrderList = (ListView) view.findViewById(R.id.list);
        getTodaysDate();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getTodaysDate() {
        try {
            Calendar origDay = Calendar.getInstance();
            currentDate = DateTimeUtils.convertDateObjectToRequestedFormat(origDay.getTime(),
                    ConfigurationMasterHelper.outDateFormat);
            orderfulfillmentdate.setText(currentDate);
            loadOrderList();
        } catch (Exception e) {
            Commons.printException(" " + e);// TODO: handle exception
        }
    }

    @Override
    public void onClick(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker2");
    }

    private static void loadOrderList() {
        orderFullfillmentHelper.downloadOrderFullfillmentReport(DateTimeUtils.convertFromServerDateToRequestedFormat(
                orderfulfillmentdate.getText().toString(), "yyyy-MM-dd"));
        orderFulfillmentReportList = orderFullfillmentHelper.getOrderFulfillmentList();

        adapter = new OrderfullfillmentAdapter(orderFulfillmentReportList);
        lstOrderList.setAdapter(adapter);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            orderfulfillmentdate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
            loadOrderList();
        }
    }

    public static class OrderfullfillmentAdapter extends ArrayAdapter<OrderFulfillmentReportBO> {

        ArrayList<OrderFulfillmentReportBO> items;

        private OrderfullfillmentAdapter(ArrayList<OrderFulfillmentReportBO> items) {
            super(context, R.layout.fragment_report_order_fulfillment_list, items);
            this.items = items;
        }

        public OrderFulfillmentReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.fragment_report_order_fulfillment_list, null, false);

                holder = new ViewHolder();

                holder.txtOrderID = (TextView) convertView.findViewById(R.id.txtOrderID);
                holder.txtRetailer = (TextView) convertView.findViewById(R.id.txtRetailer);
                holder.txtStatus = (TextView) convertView.findViewById(R.id.txtStatus);

                try {
                    ((TextView) convertView.findViewById(R.id.txtLabelOrderID)).setTypeface(
                            FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
                    if (bmodel.labelsMasterHelper.applyLabels(convertView.findViewById(R.id.txtLabelOrderID).getTag()) != null)
                        ((TextView) convertView.findViewById(R.id.txtLabelOrderID)).setText(bmodel.labelsMasterHelper
                                .applyLabels(convertView.findViewById(R.id.txtLabelOrderID).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                try {
                    ((TextView) convertView.findViewById(R.id.txtLabelRetailer)).setTypeface(
                            FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
                    if (bmodel.labelsMasterHelper.applyLabels(convertView.findViewById(R.id.txtLabelRetailer).getTag()) != null)
                        ((TextView) convertView.findViewById(R.id.txtLabelRetailer)).setText(bmodel.labelsMasterHelper
                                .applyLabels(convertView.findViewById(R.id.txtLabelRetailer).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                try {
                    ((TextView) convertView.findViewById(R.id.txtLabelStatus)).setTypeface(
                            FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
                    if (bmodel.labelsMasterHelper.applyLabels(convertView.findViewById(R.id.txtLabelStatus).getTag()) != null)
                        ((TextView) convertView.findViewById(R.id.txtLabelStatus)).setText(bmodel.labelsMasterHelper
                                .applyLabels(convertView.findViewById(R.id.txtLabelStatus).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }

                holder.inv_view_btn = (LinearLayout) convertView.findViewById(R.id.inv_view_layout);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.orderFulfillmentObjectHolder = items.get(position);

            holder.txtOrderID.setText(Html.fromHtml(holder.orderFulfillmentObjectHolder.getOrderID()));
            holder.inv_view_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, OrderFulfillmentDetailReportActivity.class);
                    i.putExtra("retid", holder.orderFulfillmentObjectHolder.getRetailerID());
                    i.putExtra("orderid", holder.orderFulfillmentObjectHolder.getOrderID());
                    i.putExtra("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                            orderfulfillmentdate.getText().toString(), "yyyy-MM-dd"));
                    context.startActivity(i);
                }
            });
            holder.txtRetailer.setText(holder.orderFulfillmentObjectHolder.getRetailerID() + "");
            holder.txtStatus.setText(holder.orderFulfillmentObjectHolder.getStatus() + "");

            return convertView;
        }

        class ViewHolder {
            private OrderFulfillmentReportBO orderFulfillmentObjectHolder;
            private TextView txtOrderID, txtRetailer, txtStatus;
            private LinearLayout inv_view_btn;
        }

    }

}
