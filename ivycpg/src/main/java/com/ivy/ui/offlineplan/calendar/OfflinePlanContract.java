package com.ivy.ui.offlineplan.calendar;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.CalenderBO;
import com.ivy.ui.offlineplan.OfflinePlanBaseContract;

import java.util.ArrayList;

/**
 * Created by mansoor on 27/03/2019
 */
public interface OfflinePlanContract {

    interface OfflinePlanView extends OfflinePlanBaseContract.OfflinePlanBaseView {
        void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList);
        void setMonthName(String monthName);
    }

    @PerActivity
    interface OfflinePlanPresenter<V extends OfflinePlanView> extends OfflinePlanBaseContract.OfflinePlanBasePresenter<V>{
        void setPlanDates();
        void loadCalendar();
        void onNextMonthClicked();
        void onPreviousMonthClicked();
    }
}
