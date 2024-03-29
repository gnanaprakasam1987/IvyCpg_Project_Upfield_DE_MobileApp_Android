package com.ivy.ui.retailerplan.calendar.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.retailerplan.calendar.bo.PeriodBo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by mansoor on 27/03/2019
 */
public interface CalendarPlanDataManager extends AppDataManagerContract {

    Observable<List<String>> loadAllowedDates();
    Observable<List<PeriodBo>> loadPeriods();
    Observable<List<PeriodBo>> loadWeekData();
    Single<Integer> loadConfiguration();

}
