package com.ivy.cpg.view.reports.orderstatusreport;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.Vector;

/**
 * Created by anandasir on 28/5/18.
 */

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.MyViewHolder> {

    OrderStatusPresenterImpl orderStatusPresenter;
    Vector<OrderStatusReportBO> items;
    BusinessModel bmodel;

    public OrderStatusAdapter(BusinessModel bmodel, OrderStatusPresenterImpl orderStatusPresenter) {
        this.bmodel = bmodel;
        this.orderStatusPresenter = orderStatusPresenter;
        this.items = orderStatusPresenter.getOrderStatusReportList();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvRetailerName, tvOutletCode, tvOrderDate, tvOrderValue, tvStatus, tvOrderId;

        public MyViewHolder(View view) {
            super(view);

            tvRetailerName = view.findViewById(R.id.tv_outlet_name);
            tvOutletCode = view.findViewById(R.id.tv_outlet_code);
            tvOrderDate = view.findViewById(R.id.tv_order_date);
            tvOrderValue = view.findViewById(R.id.tv_order_value);
            tvStatus = view.findViewById(R.id.tv_order_status);
            tvOrderId = view.findViewById(R.id.tv_order_id);

            tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOutletCode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvOrderId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }
    }

    @Override
    public OrderStatusAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_status_report_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderStatusAdapter.MyViewHolder holder, final int position) {

        holder.tvRetailerName.setText(items.get(position).getRetailerName());
        holder.tvOutletCode.setText(items.get(position).getRetailerCode());
        holder.tvOrderDate.setText(DateUtil.convertFromServerDateToRequestedFormat(items.get(position).getOrderDate(), ConfigurationMasterHelper.outDateFormat));
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