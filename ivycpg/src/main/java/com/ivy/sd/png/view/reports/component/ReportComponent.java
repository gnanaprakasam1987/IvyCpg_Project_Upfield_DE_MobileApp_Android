package com.ivy.sd.png.view.reports.component;

import com.ivy.sd.png.view.reports.module.ReportModule;
import com.ivy.sd.png.view.reports.orderreport.OrderReportHelper;
import com.ivy.sd.png.view.reports.orderreport.OrderReportModel;
import com.ivy.sd.png.view.reports.refactor.DayReportHelper;
import com.ivy.sd.png.view.reports.refactor.DayReportModel;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {ReportModule.class})
public interface ReportComponent {
    DayReportHelper provideDayReportHelper();
    void inject(DayReportModel main);
    void inject(OrderReportModel main);
    OrderReportHelper provideOrderReportHelper();
}
