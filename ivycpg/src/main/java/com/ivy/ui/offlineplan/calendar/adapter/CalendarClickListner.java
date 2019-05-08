package com.ivy.ui.offlineplan.calendar.adapter;

import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;

/**
 * Created by mansoor on 01/04/2019
 */
public interface CalendarClickListner {
    void onDateSelected(String selectedDate); // go to date view
    void onWeekDateSelected(String selectedDate);
    ArrayList<DateWisePlanBo> getDaysPlan(String date);
    void onDateNoSelected(String selectedDate); //  Opens Bottom sheet with Retailer Info
}
