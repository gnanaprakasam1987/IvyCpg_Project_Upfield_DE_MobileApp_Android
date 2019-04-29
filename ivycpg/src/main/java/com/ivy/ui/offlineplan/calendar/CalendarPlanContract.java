package com.ivy.ui.offlineplan.calendar;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.ui.offlineplan.OfflinePlanBaseContract;
import com.ivy.ui.offlineplan.calendar.bo.TimeSlotsBo;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by mansoor on 27/03/2019
 */
public interface CalendarPlanContract {

    interface CalendarPlanView extends OfflinePlanBaseContract.OfflinePlanBaseView {
        void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList);
        void setMonthName(String monthName);
        void loadDayView(Calendar date);
        void loadWeekView(Calendar date);
        void loadTimeSlotList(ArrayList<TimeSlotsBo> timeSlots);
        void updateSelectedDate(String selectedDate);
    }

    @PerActivity
    interface CalendarPlanPresenter<V extends CalendarPlanView> extends OfflinePlanBaseContract.OfflinePlanBasePresenter<V>{
        void setPlanDates();
        void loadCalendar();
        void onNextMonthClicked();
        void onPreviousMonthClicked();
        void setSelectedDate(String selectedDate);
        void loadADay();
        void loadAWeek();
        void loadDayTimeSlots();
        void onNextWeekClicked();
        void onPreviousWeekClicked();
    }
}
