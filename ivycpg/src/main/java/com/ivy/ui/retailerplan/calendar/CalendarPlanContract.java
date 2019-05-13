package com.ivy.ui.retailerplan.calendar;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;

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

        void loadTopWeekView(ArrayList<CalenderBO> mCalenderAllList, ArrayList<String> mAllowedDates);

        void loadBottomSheet(List<DateWisePlanBo> retailerInfoList);

    }

    @PerActivity
    interface CalendarPlanPresenter<V extends CalendarPlanView> extends BaseIvyPresenter<V> {

        void fetchEventsFromDb();

        void setPlanDates();

        void loadCalendar();

        void onNextMonthClicked();

        void onPreviousMonthClicked();

        void setSelectedDate(String selectedDate);

        void loadADay();

        void loadAWeek();

        void onNextWeekClicked(boolean isDaySelected);

        void onPreviousWeekClicked(boolean isDaySelected);

        List<DateWisePlanBo> getADayPlan(String date);

        List<WeekViewEvent> getPlannedEvents(int newYear, int newMonth);

        String getSelectedDate();

    }
}
