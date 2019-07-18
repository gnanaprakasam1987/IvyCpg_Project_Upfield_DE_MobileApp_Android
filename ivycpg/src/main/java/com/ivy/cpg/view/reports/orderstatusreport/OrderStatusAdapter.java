package com.ivy.cpg.view.reports.orderstatusreport;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.Vector;

/**
 * Created by anandasir on 28/5/18.
 */

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.MyViewHolder> {

    OrderStatusPresenterImpl orderStatusPresenter;
    Vector<OrderStatusReportBO> items;
    BusinessModel bmodel;
    Boolean isOrderScreen;

    public OrderStatusAdapter(BusinessModel bmodel, OrderStatusPresenterImpl orderStatusPresenter, Boolean isOrderScreen) {
        this.bmodel = bmodel;
        this.orderStatusPresenter = orderStatusPresenter;
        this.items = orderStatusPresenter.getOrderStatusReportList();
        this.isOrderScreen = isOrderScreen;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvRetailerName, tvOutletCode, tvOrderDate, tvOrderValue, tvStatus, tvOrderId;
        TextView lblOutletCode, lblOrderDate, lblOrderValue, lblStatus, lblOrderID;

        public MyViewHolder(View view) {
            super(view);

            tvRetailerName = view.findViewById(R.id.tv_outlet_name);
            tvOutletCode = view.findViewById(R.id.tv_outlet_code);
            tvOrderDate = view.findViewById(R.id.tv_order_date);
            tvOrderValue = view.findViewById(R.id.tv_order_value);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvOrderId = view.findViewById(R.id.tv_order_id);

            lblOrderDate = view.findViewById(R.id.lbl_order_date);
            lblOrderID = view.findViewById(R.id.lbl_order_id);
            lblOrderValue = view.findViewById(R.id.lbl_order_value);
            lblOutletCode = view.findViewById(R.id.lbl_outlet_code);
            lblStatus = view.findViewById(R.id.lbl_order_status);

            tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOutletCode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            try {
                if (bmodel.labelsMasterHelper.applyLabels(lblOrderDate.getTag()) != null)
                    lblOrderDate.setText(bmodel.labelsMasterHelper.applyLabels(lblOrderDate.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            lblOrderDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            try {
                if (bmodel.labelsMasterHelper.applyLabels(lblOrderID.getTag()) != null)
                    lblOrderID.setText(bmodel.labelsMasterHelper.applyLabels(lblOrderID.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            lblOrderID.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            try {
                if (bmodel.labelsMasterHelper.applyLabels(lblOrderValue.getTag()) != null)
                    lblOrderValue.setText(bmodel.labelsMasterHelper.applyLabels(lblOrderValue.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            lblOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            try {
                if (bmodel.labelsMasterHelper.applyLabels(lblOutletCode.getTag()) != null)
                    lblOutletCode.setText(bmodel.labelsMasterHelper.applyLabels(lblOutletCode.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            lblOutletCode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            try {
                if (bmodel.labelsMasterHelper.applyLabels(lblStatus.getTag()) != null)
                    lblStatus.setText(bmodel.labelsMasterHelper.applyLabels(lblStatus.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            lblStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }
    }

    @Override
    public OrderStatusAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (isOrderScreen) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_status_report_list_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invoice_status_report_list_item, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderStatusAdapter.MyViewHolder holder, final int position) {

        holder.tvRetailerName.setText(items.get(position).getRetailerName());
        holder.tvOutletCode.setText(items.get(position).getRetailerCode());
        holder.tvOrderDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(items.get(position).getOrderDate(), ConfigurationMasterHelper.outDateFormat));
        holder.tvOrderValue.setText(items.get(position).getOrderValue());
        holder.tvStatus.setText(items.get(position).getListName());
        holder.tvOrderId.setText(items.get(position).getOrderID());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}