package com.ivy.sd.png.view.reports.module;

import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.provider.RoadActivityHelper;
import com.ivy.sd.png.view.reports.orderreport.OrderReportHelper;
import com.ivy.sd.png.view.reports.refactor.DayReportHelper;
import com.ivy.sd.png.view.reports.refactor.DayReportModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by velraj.p on 5/24/2018.
 */

@Module
public class ReportModule {

    private BusinessModel application;

    @Inject
    public ReportModule(BusinessModel application) {
        this.application = application;
    }

    @Singleton
    @Provides
    ReportHelper provideReportHelper() {
        return new ReportHelper(application);
    }


    @Singleton
    @Provides
    DayReportHelper provideDayReportHelper() {
        return new DayReportHelper(application);
    }

    @Provides
    OrderReportHelper proOrderReportHelper() {
        return new OrderReportHelper(application);
    }


}
