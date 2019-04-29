package com.ivy.ui.offlineplan.calendar.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.ui.offlineplan.calendar.bo.TimeSlotsBo;
import com.ivy.utils.DeviceUtils;

import java.util.ArrayList;

/**
 * Created by mansoor on 28/03/2019
 */
public class TimeSlotsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private ArrayList<TimeSlotsBo> timeSlotList;

    public TimeSlotsAdapter(Context context, ArrayList<TimeSlotsBo> timeSlotList) {
        mContext = context;
        this.timeSlotList = timeSlotList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new TimeSlotsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.day_time_slot_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        TimeSlotsHolder holder = (TimeSlotsHolder) viewHolder;
        holder.timeSlotsBo = timeSlotList.get(position);
        holder.tvTimeSlots.setText(holder.timeSlotsBo.getTime());
    }

    class TimeSlotsHolder extends RecyclerView.ViewHolder {
        TimeSlotsBo timeSlotsBo;
        TextView tvTimeSlots;

        TimeSlotsHolder(View itemView) {
            super(itemView);

            tvTimeSlots = itemView.findViewById(R.id.tv_time_slot);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

}
