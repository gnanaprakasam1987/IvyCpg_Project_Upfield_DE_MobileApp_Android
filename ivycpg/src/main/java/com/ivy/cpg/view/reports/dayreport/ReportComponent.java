package com.ivy.cpg.view.reports.dayreport;



import com.ivy.cpg.view.reports.orderreport.OrderReportHelper;
import com.ivy.cpg.view.reports.orderreport.OrderReportModel;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {ReportModule.class})
public interface ReportComponent {
    DayReportHelper provideDayReportHelper();
    void inject(DayReportPresenterImpl main);
    void inject(OrderReportModel main);
    OrderReportHelper provideOrderReportHelper();
}
