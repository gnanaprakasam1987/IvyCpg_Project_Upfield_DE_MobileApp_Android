package com.ivy.ui.reports.syncreport.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SyncReportExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private HashMap<String, ArrayList<SyncReportBO>> hashMap;
    private ArrayList<SyncReportBO> listHeader;

    public SyncReportExpandableAdapter(Context context, HashMap<String, ArrayList<SyncReportBO>> hashMap, ArrayList<SyncReportBO> listHeader) {
        this.context = context;
        this.hashMap = hashMap;
        this.listHeader = listHeader;
    }

    @Override
    public int getGroupCount() {
        return this.listHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String api = String.valueOf(this.listHeader.get(groupPosition).getApiname());
        return this.hashMap.get(api).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String api = this.listHeader.get(groupPosition).getApiname();
        return this.hashMap.get(api).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View row = convertView;
        final GroupViewHolder holder;
        SyncReportBO syncReportBO = (SyncReportBO) getGroup(groupPosition);
        if (row == null) {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.sync_report_list_item_group, parent,
                    false);
            holder = new GroupViewHolder();
            holder.tv_apiname = row.findViewById(R.id.tv_api);
            holder.tv_timetaken = row.findViewById(R.id.tv_timetaken);
            holder.img_exp = row.findViewById(R.id.img_exp);
            row.setTag(holder);
        } else {
            holder = (GroupViewHolder) row.getTag();
        }

        holder.tv_apiname.setText(syncReportBO.getApiname());
        String time = DateTimeUtils.getSeconds(syncReportBO.getStartTime(), syncReportBO.getEndTime(), DateTimeUtils.DATE_TIME_NEW)
                + " " + context.getResources().getString(R.string.seconds);
        holder.tv_timetaken.setText(time);
        if (isExpanded)
            holder.img_exp.setRotation(180);
        else
            holder.img_exp.setRotation(0);

        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        View row = convertView;
        SyncReportBO syncReportBO = (SyncReportBO) getChild(groupPosition, childPosition);
        if (row == null) {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.sync_report_list_item_child, parent,
                    false);
            holder = new ViewHolder();
            holder.tv_tablename = row.findViewById(R.id.tv_tablename);
            holder.tv_record = row.findViewById(R.id.tv_recordCount);
            holder.img_line = row.findViewById(R.id.img_line);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.tv_tablename.setText(syncReportBO.getTablename());
        String records = syncReportBO.getRecordCount() + " " + context.getResources().getString(R.string.records);
        holder.tv_record.setText(records);
        if (isLastChild)
            holder.img_line.setVisibility(View.VISIBLE);
        else
            holder.img_line.setVisibility(View.GONE);
        return row;
    }

    private class GroupViewHolder {
        private TextView tv_apiname;
        private TextView tv_timetaken;
        private ImageView img_exp;
    }


    class ViewHolder {
        private TextView tv_tablename;
        private TextView tv_record;
        private ImageView img_line;
    }
}
