package com.ivy.ui.offlineplan.calendar;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.CalenderBO;

import java.util.ArrayList;

/**
 * Created by mansoor on 27/03/2019
 */
public interface OfflinePlanContract {

    interface OfflinePlanView extends BaseIvyView {
        void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList);
        void setMonthName(String monthName);
    }

    @PerActivity
    interface OfflinePlanPresenter<V extends OfflinePlanView> extends BaseIvyPresenter<V>{
        void setPlanDates();
        void loadCalendar();
        void onNextMonthClicked();
        void onPreviousMonthClicked();
    }
}
