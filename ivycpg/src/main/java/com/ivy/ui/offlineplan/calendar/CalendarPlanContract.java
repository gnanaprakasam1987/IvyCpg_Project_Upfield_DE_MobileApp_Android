package com.ivy.ui.offlineplan.calendar;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by mansoor on 27/03/2019
 */
public interface CalendarPlanContract {

    interface CalendarPlanView extends BaseIvyView {
        void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList);

        void setMonthName(String monthName);

        void loadDayView(Calendar date);

        void loadWeekView(Calendar date);
    }

    @PerActivity
    interface CalendarPlanPresenter<V extends CalendarPlanView> extends BaseIvyPresenter<V> {
        void setPlanDates();

        void loadCalendar();

        void onNextMonthClicked();

        void onPreviousMonthClicked();

        void setSelectedDate(String selectedDate);

        void loadADay();

        void loadAWeek();

        void onNextDayClicked();

        void onPreviousDayClicked();

        void onNextWeekClicked();

        void onPreviousWeekClicked();

        List<WeekViewEvent> getEvents(int newYear, int newMonth);
    }
}
