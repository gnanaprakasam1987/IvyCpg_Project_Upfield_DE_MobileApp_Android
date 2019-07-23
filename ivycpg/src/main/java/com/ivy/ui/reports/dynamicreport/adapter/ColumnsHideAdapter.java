package com.ivy.ui.reports.dynamicreport.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;

import java.util.ArrayList;

public class ColumnsHideAdapter extends ArrayAdapter<DynamicReportBO> {

    private ArrayList<DynamicReportBO> items;
    private Context context;

    public ColumnsHideAdapter(Context mContext, ArrayList<DynamicReportBO> items) {
        super(mContext, R.layout.dialog_sync_retailer_select_listview);
        this.items = items;
        this.context = mContext;
    }

    @Override
    public DynamicReportBO getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(
                    R.layout.dialog_sync_retailer_select_listview, parent,
                    false);
            holder = new ViewHolder();
            holder.columnName = row
                    .findViewById(R.id.dialog_sync_retailer_select_retailername);
            holder.columnChkBox = row
                    .findViewById(R.id.dialog_sync_retailer_select_chkbox);

            holder.columnChkBox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        holder.obj.setSelected(true);
                    } else {
                        holder.obj.setSelected(false);
                    }
                }
            });

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.obj = items.get(position);
        holder.columnName.setText(holder.obj.getDisplayName());
        holder.columnChkBox.setChecked(holder.obj.isSelected());
        return (row);
    }
}

class ViewHolder {
    DynamicReportBO obj;
    TextView columnName;
    CheckBox columnChkBox;
}


