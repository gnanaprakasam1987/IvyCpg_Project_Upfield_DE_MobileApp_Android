package com.ivy.ui.attendance.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.cpg.view.attendance.inout.TimeTrackingFragment;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by mansoor on 27/12/2018
 */
class TimeTrackingAdapter extends ArrayAdapter<NonFieldTwoBo> {
    private ArrayList<NonFieldTwoBo> nonFieldTwoBos;
    private Context context;
    private ConfigurationMasterHelper configurationMasterHelper;

    public TimeTrackingAdapter(Context context, ArrayList<NonFieldTwoBo> nonFieldTwoBos, ConfigurationMasterHelper configurationMasterHelper) {
        super(context, R.layout.row_nonfield_two, nonFieldTwoBos);
        this.nonFieldTwoBos = nonFieldTwoBos;
        this.context = context;
        configurationMasterHelper = ((BusinessModel) context.getApplicationContext()).getComponent().configurationMasterHelper();

    }

    public NonFieldTwoBo getItem(int position) {
        return nonFieldTwoBos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return nonFieldTwoBos.size();
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.row_nonfield_two,
                    parent, false);

            holder.tvOutTime = convertView
                    .findViewById(R.id.txt_fromTime);
            holder.btOutTime = convertView
                    .findViewById(R.id.btn_fromTime);
            holder.btInTime = convertView
                    .findViewById(R.id.btn_toTime);
            holder.tvInTime = convertView
                    .findViewById(R.id.txt_toTime);
            holder.tvReason = convertView
                    .findViewById(R.id.txt_reason);
            holder.tvStatus = convertView
                    .findViewById(R.id.txt_status);
            holder.tvRemarks = convertView
                    .findViewById(R.id.txt_remarks);


            if (!configurationMasterHelper.IS_ATTENDANCE_REMARK)
                holder.tvRemarks.setVisibility(View.GONE);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

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
                if (startLocationService(holder.nonFieldTwoBO.getReason())) {
                    holder.nonFieldTwoBO.setInTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                    attendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO, getActivity());

                    loadNonFieldTwoDetails();
                }
            }
        });

        holder.btOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.nonFieldTwoBO.setOutTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                attendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO, getActivity());
                loadNonFieldTwoDetails();

                stopLocationService(holder.nonFieldTwoBO.getReason());

            }
        });

        holder.tvReason.setText(attendanceHelper
                .getReasonName(holder.nonFieldTwoBO.getReason(), getActivity()));

        holder.tvRemarks.setText(context.getResources().getString(R.string.remark_hint) + ":" + holder.nonFieldTwoBO.getRemarks());


        return convertView;
    }

}

class ViewHolder {
    NonFieldTwoBo nonFieldTwoBO;
    TextView tvOutTime, tvReason, tvInTime, tvStatus, tvRemarks;
    Button btInTime, btOutTime;

}