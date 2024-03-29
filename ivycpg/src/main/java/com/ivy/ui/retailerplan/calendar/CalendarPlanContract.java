package com.ivy.ui.retailerplan.calendar;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;
import com.ivy.ui.retailerplan.calendar.bo.PeriodBo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by mansoor on 27/03/2019
 */
public interface CalendarPlanContract {

    interface CalendarPlanView extends BaseIvyView {
        void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList , List<String> weekNoList);

        void setMonthName(String monthName);

        void loadDayView(Calendar date);

        void loadWeekView(Calendar date);

        void loadTopWeekView(ArrayList<CalenderBO> mCalenderAllList, ArrayList<String> mAllowedDates);

        void loadRetailerInfoBtmSheet(List<DateWisePlanBo> retailerInfoList);

        void reloadView();

        void loadAddPlanDialog(String date, RetailerMasterBO retailerMasterBO);

        void setWeekDayText(List<String> weekDayText);

        int getColorCode(int type);

    }

    @PerActivity
    interface CalendarPlanPresenter<V extends CalendarPlanView> extends BaseIvyPresenter<V> {

        void fetchEventsFromDb(boolean onCreate);

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

        void fetchSelectedDateRetailerPlan(String date, RetailerMasterBO retailerMasterBO);

        ArrayList<DateWisePlanBo> getSelectedDateRetailerPlanList();

        DateWisePlanBo getSelectedRetailerPlan(String retailerId);

        RetailerMasterBO getPlanedRetailerBo(String retailerId);

        boolean isPastDate(String mselectedDate);

        long getMaxPlanDate();


        void copyPlan(String fromDate, String toDate);

        void movePlan(String fromDate, String toDate, String reasonId);

        void setRetailerMasterBo(RetailerMasterBO retailerMasterBo);

        List<PeriodBo> getWeekList();

        int getWeeksPlanCount(String weekoNo);

        String getWeekNo(String dateOfWeek);

        List<String> getWeekNoList();

        void copyWeekPlan(String fromDate, String toDate);

        void moveWeekPlan(String fromDate, String toDate,String reasonId);

        void loadConfiguration();

        void cancelPlans(String reasonId);

        void cancelWeekPlans(String reasonId);

        boolean isCurrentDay();

        boolean isCurrentWeek();

        boolean showRescheduleFuture();
        boolean showRescheduleReasonFuture();
        boolean showCancelPlanReasonFuture();

    }
}
