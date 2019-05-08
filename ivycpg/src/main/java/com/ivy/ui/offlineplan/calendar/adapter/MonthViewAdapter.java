package com.ivy.ui.offlineplan.calendar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.calendarlibrary.monthview.MonthView;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by mansoor on 28/03/2019
 */
public class MonthViewAdapter extends MonthView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private CalendarClickListner calendarClickListner;
    private List<String> mItems, mItems1, mItems2;
    private ArrayList<String> mAllowedDates;
    private int dayInWeekCount;
    private ArrayList<CalenderBO> mCalenderAllList;

    public MonthViewAdapter(Context context, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList, ArrayList<String> mAllowedDates, CalendarClickListner calendarClickListner) {
        mContext = context;
        this.calendarClickListner = calendarClickListner;
        this.dayInWeekCount = dayInWeekCount;
        this.mCalenderAllList = mCalenderAllList;
        this.mAllowedDates = mAllowedDates;
        populateMonth();
    }

    private void populateMonth() {
        mItems = new ArrayList<>();  // Day  - dd - Calendar Day
        mItems1 = new ArrayList<>(); // Date  - yyyy/MM/dd - Calendar Date
        mItems2 = new ArrayList<>(); // Date - yyyy/MM/dd - Campaign Date
        if (dayInWeekCount > 1) {
            for (int i = 1; i < dayInWeekCount; i++) {
                mItems.add("No");
                mItems1.add("No");
            }
        }

        for (int i = 0; i < mCalenderAllList.size(); i++) {
            mItems.add(String.valueOf(mCalenderAllList.get(i).getDate()));
            mItems1.add(String.valueOf(mCalenderAllList.get(i).getCal_date()));
        }

        mItems2.addAll(mAllowedDates);

        double rows = Math.ceil((double) mItems.size() / 7.0);
        int cellCount = (int) rows * 8;
        int loop = mItems.size();
        for (int i = 0; i < (cellCount - loop); i++) {
            mItems.add("No");
            mItems1.add("No");
        }

        // to add week no on its place
        int count = 1;
        for (int i = 0; i < mItems.size(); i++) {
            if (i % 8 == 0 && count <= rows) {
                mItems.add(i, "" + count);
                mItems1.add(i, "" + count);
                count++;
            }
        }

        mItems.subList(cellCount, mItems.size()).clear();
        mItems1.subList(cellCount, mItems1.size()).clear();


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_item_weekno, parent, false);
            viewHolder = new WeekViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_item_calender, parent, false);
            viewHolder = new MonthViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public void onBindHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position % 8 == 0) {
            WeekViewHolder holder = (WeekViewHolder) viewHolder;
            holder.TvWeekNo.setText(mItems.get(position));
        } else {
            MonthViewHolder holder = (MonthViewHolder) viewHolder;
            if (mItems.get(position).equals("No")) {
                holder.TVDate.setText("");
                holder.TvRetailer.setVisibility(View.GONE);
                holder.isDataPresent = false;
            } else {
                holder.TVDate.setText(mItems.get(position));
                holder.isDataPresent = true;
                if (mItems2.contains(mItems1.get(position))) {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                    holder.isValid = true;
                } else {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                    holder.isValid = false;
                }
                if (Objects.requireNonNull(DateTimeUtils.convertStringToDateObject(mItems1.get(position), "yyyy/MM/dd")).before(new Date())) {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                }
            }

            int[] date = {};
            if (!mItems1.get(position).equals("No")) {
                date = getDateNew(mItems1.get(position));
            }
            if (date != null && date.length > 0) {

                for (int i = 0; i < mCalenderAllList.size(); i++) {
                    if ((mCalenderAllList.get(i).getDate() + "").equals(mItems.get(position)) && (mCalenderAllList.get(i).getCal_date() + "").equals(mItems1.get(position))) {
                        holder.calBO = mCalenderAllList.get(i);
                    }
                }

                if (holder.calBO.isToday()) {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.white));
                    holder.TVDate.setBackground(mContext.getResources().getDrawable(R.drawable.circle_blue_bg));
                } else if (holder.calBO.isSelected()) {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                    holder.TVDate.setBackground(mContext.getResources().getDrawable(R.drawable.circle_calendar_select));
                } else {
                    holder.TVDate.setTextColor(mContext.getResources().getColor(R.color.FullBlack));
                    holder.TVDate.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                }

                ArrayList<DateWisePlanBo> planList = calendarClickListner.getDaysPlan(holder.calBO.getCal_date());
                if (planList.size() > 0) {
                    holder.TvRetailer.setText(mContext.getResources().getString(R.string.plan_retailer_name, planList.get(0).getName()));
                    if (planList.size() > 1)
                        holder.TvExtras.setText(mContext.getResources().getString(R.string.plus_more, planList.size() - 1));

                }
            }

            holder.llDate.setOnClickListener(v -> {
                if (holder.calBO != null) {
                    refreshGrid(holder.calBO.getCal_date());
                    calendarClickListner.onDateNoSelected(holder.calBO.getCal_date());
                }
            });

            holder.lltext.setOnClickListener(v -> {
                if (holder.calBO != null) {
                    refreshGrid(holder.calBO.getCal_date());
                    calendarClickListner.onDateSelected(holder.calBO.getCal_date());
                }

            });
        }

    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        CalenderBO calBO;
        TextView TVDate, TvRetailer, TvExtras;
        Boolean isValid = false;
        Boolean isDataPresent = false;
        LinearLayout llDate,lltext;

        MonthViewHolder(View itemView) {
            super(itemView);

            TVDate = itemView.findViewById(R.id.tv_date);
            TvRetailer = itemView.findViewById(R.id.tv_retailers);
            TvExtras = itemView.findViewById(R.id.tv_extras);
            llDate = itemView.findViewById(R.id.llDate);
            lltext = itemView.findViewById(R.id.lltext);

        }
    }

    class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView TvWeekNo;

        WeekViewHolder(View itemView) {
            super(itemView);

            TvWeekNo = itemView.findViewById(R.id.tv_weekno);
        }
    }

    private int[] getDateNew(String dateString) {
        boolean pass = true;

        dateString = DateTimeUtils.convertDateTimeObjectToRequestedFormat(dateString, "yyyy/MM/dd", "dd/MM/yyyy");
        String[] strArray = dateString.split("/");
        int[] date = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            switch (i) {
                case 1:
                    int day = SDUtil.convertToInt(strArray[i]);
                    if (day > 0)
                        date[i] = SDUtil.convertToInt(strArray[i]) - 1;
                    else {
                        date[i] = SDUtil.convertToInt(strArray[i]) + 11;
                        pass = false;
                    }
                    break;
                case 2:
                    if (pass)
                        date[i] = SDUtil.convertToInt(strArray[i]);
                    else {
                        date[i] = SDUtil.convertToInt(strArray[i]) - 1;
                    }
                    break;
                default:
                    date[i] = SDUtil.convertToInt(strArray[i]);
                    break;
            }
        }
        return date;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 8;
    }

    private void refreshGrid(String dateSelected) {

        for (CalenderBO calenderBO : mCalenderAllList)
            calenderBO.setSelected(false);
        for (CalenderBO calenderBO : mCalenderAllList)
            if (dateSelected.equalsIgnoreCase(calenderBO.getCal_date()))
                calenderBO.setSelected(true);

        notifyDataSetChanged();
    }
}
