package com.ivy.ui.attendance.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackingAdapter extends RecyclerView.Adapter<TimeTrackingAdapter.ViewHolder> {

    private ArrayList<NonFieldTwoBo> nonFieldTwoBos;
    private Context context;
    private ConfigurationMasterHelper configurationMasterHelper;
    private TimeTrackListClickListener timeTrackListClickListener;

    public TimeTrackingAdapter(Context context, ArrayList<NonFieldTwoBo> nonFieldTwoBos, TimeTrackListClickListener timeTrackListClickListener) {
        this.nonFieldTwoBos = nonFieldTwoBos;
        this.context = context;
        this.timeTrackListClickListener = timeTrackListClickListener;
        configurationMasterHelper = ((BusinessModel) context.getApplicationContext()).getComponent().configurationMasterHelper();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nonfield_two, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nonFieldTwoBO = nonFieldTwoBos.get(position);
        holder.tvOutTime.setText(holder.nonFieldTwoBO.getOutTime());
        holder.tvStatus.setText(holder.nonFieldTwoBO.getStatus());
        holder.btOutTime.setText(context.getResources().getString(R.string.endC));
        String inTime = holder.nonFieldTwoBO.getInTime() != null ? holder.nonFieldTwoBO.getInTime() : " ";
        String outTime = holder.nonFieldTwoBO.getOutTime() != null ? holder.nonFieldTwoBO.getOutTime() : " ";
        String date;
        String time;
        StringTokenizer tokenizer;


        if (holder.nonFieldTwoBO.getOutTime() != null && !holder.nonFieldTwoBO.getOutTime().trim().equalsIgnoreCase("")) {
            holder.btOutTime.setVisibility(View.GONE);
            holder.tvOutTime.setVisibility(View.VISIBLE);
            tokenizer = new StringTokenizer(outTime);
            date = tokenizer.nextToken();
            time = tokenizer.nextToken();
            holder.tvOutTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                    ConfigurationMasterHelper.outDateFormat) + "\n" + time);
        } else {
            holder.tvOutTime.setVisibility(View.GONE);
            holder.btOutTime.setVisibility(View.VISIBLE);
            holder.btOutTime.setText(context.getResources().getString(R.string.endC));
        }


        if (holder.nonFieldTwoBO.getInTime() != null && !holder.nonFieldTwoBO.getInTime().trim().equalsIgnoreCase("")) {
            holder.btInTime.setVisibility(View.GONE);
            holder.tvInTime.setVisibility(View.VISIBLE);
            tokenizer = new StringTokenizer(inTime);
            date = tokenizer.nextToken();
            time = tokenizer.nextToken();
            holder.tvInTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                    ConfigurationMasterHelper.outDateFormat) + "\n" + time);
        } else {
            holder.tvInTime.setVisibility(View.GONE);
            holder.btInTime.setVisibility(View.VISIBLE);
            tokenizer = new StringTokenizer(inTime);
            date = tokenizer.nextToken();
            time = tokenizer.nextToken();
            holder.btInTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                    ConfigurationMasterHelper.outDateFormat) + "\n" + time);
        }

        holder.btInTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timeTrackListClickListener.onInTimeClick(position);
            }
        });

        holder.btOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timeTrackListClickListener.onOutTimeClick(position);

            }
        });

        holder.tvReason.setText(holder.nonFieldTwoBO.getReasonText());

        holder.tvRemarks.setText(context.getResources().getString(R.string.remark_hint) + ":" + holder.nonFieldTwoBO.getRemarks());
    }

    @Override
    public int getItemCount() {
        return nonFieldTwoBos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        NonFieldTwoBo nonFieldTwoBO;
        private TextView tvOutTime, tvReason, tvInTime, tvStatus, tvRemarks;
        private Button btInTime, btOutTime;

        public ViewHolder(View itemView) {
            super(itemView);

            tvOutTime = itemView
                    .findViewById(R.id.txt_fromTime);
            btOutTime = itemView
                    .findViewById(R.id.btn_fromTime);
            btInTime = itemView
                    .findViewById(R.id.btn_toTime);
            tvInTime = itemView
                    .findViewById(R.id.txt_toTime);
            tvReason = itemView
                    .findViewById(R.id.txt_reason);
            tvStatus = itemView
                    .findViewById(R.id.txt_status);
            tvRemarks = itemView
                    .findViewById(R.id.txt_remarks);

            if (!configurationMasterHelper.IS_ATTENDANCE_REMARK)
                tvRemarks.setVisibility(View.GONE);
        }
    }
}



