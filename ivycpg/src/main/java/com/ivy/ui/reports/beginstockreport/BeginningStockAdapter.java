package com.ivy.ui.reports.beginstockreport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.reports.beginstockreport.data.BeginningStockReportBO;

import java.util.ArrayList;


public class BeginningStockAdapter extends ArrayAdapter<BeginningStockReportBO> {
    private ArrayList<BeginningStockReportBO> items;
    private ConfigurationMasterHelper configurationMasterHelper;

    public BeginningStockAdapter(ArrayList<BeginningStockReportBO> items, Context context, ConfigurationMasterHelper masterHelper) {
        super(context, R.layout.row_begining_stock_listview, items);
        this.items = items;
        this.configurationMasterHelper = masterHelper;
    }

    public BeginningStockReportBO getItem(int position) {
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
        BeginningStockReportBO product = items.get(position);

        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_begining_stock_listview,
                    parent, false);
            holder = new ViewHolder();
            holder.psName = row.findViewById(R.id.productname);
            holder.caseQty = row.findViewById(R.id.caseqty);
            holder.pcsQty = row.findViewById(R.id.pieceqty);
            holder.lineValue = row.findViewById(R.id.lineValue);
            holder.outerQty = row.findViewById(R.id.outerqty);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    beginningStockAdapterCallback.productName(holder.pName);
                }
            });

            if (configurationMasterHelper.SHOW_ORDER_CASE)
                holder.caseQty.setVisibility(View.VISIBLE);
            else
                holder.caseQty.setVisibility(View.GONE);
            if (configurationMasterHelper.SHOW_ORDER_PCS)
                holder.pcsQty.setVisibility(View.VISIBLE);
            else
                holder.pcsQty.setVisibility(View.GONE);
            if (configurationMasterHelper.SHOW_OUTER_CASE)
                holder.outerQty.setVisibility(View.VISIBLE);
            else
                holder.outerQty.setVisibility(View.GONE);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.psName.setText(product.getProductShortName());
        holder.caseQty.setText(String.valueOf(product.getCaseQuantity()));
        holder.pcsQty.setText(String.valueOf(product.getPcsQuantity()));
        holder.outerQty.setText(String.valueOf(product.getOuterQty()));
        holder.pName = product.getProductName();
        double lineValue = (product.getCaseQuantity() * product.getCaseSize() + product
                .getPcsQuantity()) * product.getBasePrice();
        holder.lineValue.setText(formatValue(lineValue));
        return row;
    }

    class ViewHolder {
        TextView psName, caseQty, pcsQty, lineValue, outerQty;
        String pName;
    }

    public String formatValue(double value) {

        return SDUtil.format(value,
                configurationMasterHelper.VALUE_PRECISION_COUNT,
                configurationMasterHelper.VALUE_COMMA_COUNT, configurationMasterHelper.IS_DOT_FOR_GROUP);
    }


    private BeginningStockAdapterCallback beginningStockAdapterCallback;

    public void setBeginningStockAdapterCallback(BeginningStockAdapterCallback beginningStockAdapterCallback) {
        this.beginningStockAdapterCallback = beginningStockAdapterCallback;
    }

    public interface BeginningStockAdapterCallback {
        void productName(String pName);
    }


}
