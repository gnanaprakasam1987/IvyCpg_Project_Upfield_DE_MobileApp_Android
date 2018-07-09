package com.ivy.cpg.view.reports.beginstockreport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.Vector;


public class BeginningStockAdapter extends ArrayAdapter<StockReportMasterBO> {
    private Vector<StockReportMasterBO> items;
    private ConfigurationMasterHelper configurationMasterHelper;

    public BeginningStockAdapter(Vector<StockReportMasterBO> items, Context context, ConfigurationMasterHelper masterHelper) {
        super(context, R.layout.row_begining_stock_listview, items);
        this.items = items;
        this.configurationMasterHelper = masterHelper;
    }

    public StockReportMasterBO getItem(int position) {
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
        StockReportMasterBO product = items.get(position);

        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_begining_stock_listview,
                    parent, false);
            holder = new ViewHolder();
            holder.psName = row.findViewById(R.id.productname);
            holder.caseQty = row.findViewById(R.id.caseqty);
            holder.pcsQty = row.findViewById(R.id.pieceqty);
            holder.unitPrice = row.findViewById(R.id.unitprice);
            holder.outerQty = row.findViewById(R.id.outerqty);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    beginningStockAdapterCallback.productName(holder.pName);
                }
            });

            if (configurationMasterHelper.SHOW_ORDER_CASE)
                holder.caseQty.setVisibility(View.GONE);
            if (configurationMasterHelper.SHOW_ORDER_PCS)
                holder.pcsQty.setVisibility(View.GONE);
            if (configurationMasterHelper.SHOW_OUTER_CASE)
                holder.outerQty.setVisibility(View.GONE);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.psName.setText(product.getProductshortname());
        holder.caseQty.setText(String.valueOf(product.getCaseqty()));
        holder.pcsQty.setText(String.valueOf(product.getPieceqty()));
        holder.outerQty.setText(String.valueOf(product.getOuterQty()));
        holder.pName = product.getProductname();
        double unitprice = (product.getCaseqty() * product.getCasesize() + product
                .getPieceqty()) * product.getBasePrice();
        holder.unitPrice.setText(formatValue(unitprice));
        return row;
    }

    class ViewHolder {
        TextView psName, caseQty, pcsQty, unitPrice, outerQty;
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
