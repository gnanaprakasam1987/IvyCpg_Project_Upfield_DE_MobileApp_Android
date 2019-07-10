package com.ivy.ui.reports.syncreport.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class SyncUploadReportAdapter extends RecyclerView.Adapter<SyncUploadReportAdapter.ViewHolder> {

    private ArrayList<SyncReportBO> data;
    private Context mContext;

    public SyncUploadReportAdapter(Context context, ArrayList<SyncReportBO> data) {
        this.mContext = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.sync_report_upload_list_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.syncReportBO = data.get(position);
        holder.tv_synctype.setText(holder.syncReportBO.getApiname());
        String records = holder.syncReportBO.getRecordCount() + " " + mContext.getResources().getString(R.string.records);
        holder.tv_records.setText(records);
        String time = DateTimeUtils.getSeconds(holder.syncReportBO.getStartTime(), holder.syncReportBO.getEndTime(), DateTimeUtils.DATE_TIME_NEW)
                + " " + mContext.getResources().getString(R.string.seconds);
        holder.tv_time.setText(time);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_synctype;
        TextView tv_time;
        TextView tv_records;
        SyncReportBO syncReportBO;

        ViewHolder(View itemView) {
            super(itemView);
            tv_synctype = itemView.findViewById(R.id.tv_synctype);
            tv_time = itemView.findViewById(R.id.tv_seconds);
            tv_records = itemView.findViewById(R.id.tv_totalrecords);
        }
    }
}
