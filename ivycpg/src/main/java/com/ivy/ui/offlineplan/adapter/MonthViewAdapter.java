package com.ivy.ui.offlineplan.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.lib.MonthView.MonthRecyclerView;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CalenderBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by mansoor on 28/03/2019
 */
public class MonthViewAdapter extends MonthRecyclerView.Adapter<MonthViewAdapter.MonthViewHolder> {

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
                mItems2.add("No");
            }
        }

        for (int i = 0; i < mCalenderAllList.size(); i++) {
            mItems.add(String.valueOf(mCalenderAllList.get(i).getDate()));
            mItems1.add(String.valueOf(mCalenderAllList.get(i).getCal_date()));
        }

        mItems2.addAll(mAllowedDates);

        double rows = Math.ceil((double) mItems.size() / 7.0);
        int cellCount = (int) rows * 7;
        int loop = mItems.size();
        for (int i = 0; i < (cellCount - loop); i++) {
            mItems.add("No");
            mItems1.add("No");
            mItems2.add("No");
        }
    }


    @Override
    public MonthViewHolder onCreateHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_item_calender, parent, false);
        int height = parent.getMeasuredHeight() / 5;
        view.setMinimumHeight(height);
        return new MonthViewHolder(view);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public void onBindHolder(MonthViewHolder holder, int position) {

        if (mItems.get(position).equals("No")) {
            holder.TVDate.setText("");
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
        }

    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        CalenderBO calBO;
        TextView TVDate,TvRetailer;
        Boolean isValid = false;
        Boolean isDataPresent = false;

        MonthViewHolder(View itemView) {
            super(itemView);

            TVDate = itemView.findViewById(R.id.tv_date);
            TvRetailer = itemView.findViewById(R.id.tv_retailers);


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
}
