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
import com.ivy.utils.DeviceUtils;

import java.util.ArrayList;

/**
 * Created by mansoor on 28/03/2019
 */
public class WeekFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private ArrayList<CalenderBO> mCalenderAllList;
    private ArrayList<String> mAllowedDates;
    private CalendarClickListner calendarClickListner;
    private int itemWidth;

    public WeekFilterAdapter(Context context, ArrayList<CalenderBO> mCalenderAllList,
                             ArrayList<String> mAllowedDates, CalendarClickListner calendarClickListner,
                             int itemWidth) {
        mContext = context;
        this.mCalenderAllList = mCalenderAllList;
        this.mAllowedDates = mAllowedDates;
        this.calendarClickListner = calendarClickListner;
        this.itemWidth = itemWidth;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == 0)
            viewHolder = new WeekViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.day_week_item_weekno, parent, false));
        else
            viewHolder = new DayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.day_week_item_calender, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (position == 0) {
            WeekViewHolder holder = (WeekViewHolder) viewHolder;
            holder.tvWeekText.setText(mCalenderAllList.get(position).getDay());
            holder.tvWeekNo.setText(mCalenderAllList.get(position).getWeekDate());

            RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            holder.rlweekdate.setLayoutParams(rel_btn);

        } else {
            DayViewHolder holder = (DayViewHolder) viewHolder;
            holder.calBO = mCalenderAllList.get(position);
            holder.TVDate.setText(holder.calBO.getWeekDate());
            holder.TvDay.setText(holder.calBO.getDay());
            if (mAllowedDates.contains(holder.calBO.getCal_date())) {
                holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                holder.isValid = true;
            } else {
                holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                holder.isValid = false;
            }

            if (holder.calBO.isToday()) {
                holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.TVDate.setBackground(mContext.getResources().getDrawable(R.drawable.circle_blue_bg));
            } else if (holder.calBO.isSelected()) {
                holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                holder.TVDate.setBackground(mContext.getResources().getDrawable(R.drawable.circle_calendar_select));
            } else {
                holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                holder.TVDate.setBackgroundColor(mContext.getResources().getColor(R.color.zxing_transparent));
            }

            holder.rlItem.setOnClickListener(v -> {
                if (holder.calBO != null && holder.isValid) {
                    refreshList(holder.calBO.getCal_date());
                    calendarClickListner.onWeekDateSelected(holder.calBO.getCal_date());
                }

            });

            RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            holder.rlItem.setLayoutParams(rel_btn);
        }
    }


    class DayViewHolder extends RecyclerView.ViewHolder {
        CalenderBO calBO;
        TextView TVDate, TvDay;
        Boolean isValid = false;
        RelativeLayout rlItem;

        DayViewHolder(View itemView) {
            super(itemView);

            TVDate = itemView.findViewById(R.id.tv_date);
            TvDay = itemView.findViewById(R.id.tv_day);
            rlItem = itemView.findViewById(R.id.rl_week_date);
        }
    }

    class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekNo, tvWeekText;
        RelativeLayout rlweekdate;

        WeekViewHolder(View itemView) {
            super(itemView);

            tvWeekNo = itemView.findViewById(R.id.tv_wk_value);
            tvWeekText = itemView.findViewById(R.id.tv_wkText);
            rlweekdate = itemView.findViewById(R.id.rl_week_date);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCalenderAllList.size();
    }

    private void refreshList(String dateSelected) {

        for (CalenderBO calenderBO : mCalenderAllList)
            calenderBO.setSelected(false);
        for (CalenderBO calenderBO : mCalenderAllList)
            if (dateSelected.equalsIgnoreCase(calenderBO.getCal_date()))
                calenderBO.setSelected(true);

        notifyDataSetChanged();
    }
}
