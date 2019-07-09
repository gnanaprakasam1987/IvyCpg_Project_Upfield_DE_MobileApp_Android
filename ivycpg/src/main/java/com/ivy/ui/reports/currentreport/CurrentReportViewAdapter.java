package com.ivy.ui.reports.currentreport;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.ui.reports.currentreport.view.CurrentReportViewFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportBO;

import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class CurrentReportViewAdapter extends ArrayAdapter<StockReportBO> {
    private ArrayList<StockReportBO> items;
    private ConfigurationMasterHelper configurationMasterHelper;

    public CurrentReportViewAdapter(ArrayList<StockReportBO> items, Context context,
                                    ConfigurationMasterHelper configurationMasterHelper, CurrentReportViewFragment currentReportModel) {
        super(context, R.layout.row_stock_report, items);
        this.items = items;
        this.configurationMasterHelper = configurationMasterHelper;
        this.currentReportViewAdapterCallback = currentReportModel;
    }

    public StockReportBO getItem(int position) {
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
        StockReportBO product = items.get(position);
        View row = convertView;
        if (row == null) {

            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_stock_report, parent, false);
            holder = new ViewHolder();

            holder.psName = row.findViewById(R.id.orderPRODNAME);
            holder.psName.setMaxLines(configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
            holder.sih = row.findViewById(R.id.sih);
            holder.sihCase = row.findViewById(R.id.sih_case);
            holder.sihOuter = row.findViewById(R.id.sih_outer);
            holder.prodCode = row.findViewById(R.id.prdcode);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    currentReportViewAdapterCallback.productName(holder.pnNme);
                }
            });

            if (configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                holder.sihCase.setVisibility(View.VISIBLE);
                holder.sihOuter.setVisibility(View.VISIBLE);
            } else {
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
            }
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.ref = position;
        holder.psName.setText(product.getProductShortName());
        holder.pnNme = product.getProductName();
        if (configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
            boolean isUomWiseSplitted = false;
            int rem_sih = 0;
            int totalQty = product.getSih();

            if (product.isBaseUomCaseWise() && product.getCaseSize() != 0) {
                isUomWiseSplitted = true;

                holder.sihCase.setText(String.valueOf(totalQty / product.getCaseSize()));
                rem_sih = totalQty % product.getCaseSize();
            }
            if (product.isBaseUomOuterWise() && product.getOuterSize() != 0) {
                if (isUomWiseSplitted) {
                    holder.sihOuter.setText(String.valueOf(rem_sih / product.getOuterSize()));
                    rem_sih = rem_sih % product.getOuterSize();
                } else {
                    isUomWiseSplitted = true;
                    holder.sihOuter.setText(String.valueOf(totalQty / product.getOuterSize()));
                    rem_sih = totalQty % product.getOuterSize();
                }
            }

            if (isUomWiseSplitted) {
                holder.sih.setText(String.valueOf(rem_sih));
            } else {
                holder.sih.setText(String.valueOf(product.getSih()));
            }

        } else {
            holder.sih.setText(String.valueOf(product.getSih()));
        }

        holder.prodCode.setText(product.getProductCode());

        return (row);
    }

    class ViewHolder {
        private String pnNme;
        private TextView psName, sih, sihCase, sihOuter, prodCode;
        int ref;
    }

    private CurrentReportViewAdapterCallback currentReportViewAdapterCallback;

    public interface CurrentReportViewAdapterCallback {
        void productName(String pName);
    }
}
