package com.ivy.ui.offlineplan.calendar.adapter;

import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mansoor on 01/04/2019
 */
public interface CalendarClickListner {
    void onADayRetailerSelected(String selectedDate); // go to date view
    void onWeekDateSelected(String selectedDate);
    void onDateNoSelected(String selectedDate,List<DateWisePlanBo> planList); //  Opens Bottom sheet with Retailer Info
}
