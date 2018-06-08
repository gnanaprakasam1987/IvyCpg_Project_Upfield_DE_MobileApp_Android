package com.ivy.sd.png.view.reports.eodstockreport;

import com.ivy.sd.png.view.reports.module.ReportModule;
import com.ivy.sd.png.view.reports.refactor.DayReportModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {EodStockReportModule.class})
public interface EodReportComponent {

    EodReportHelper provideEodReport();
    void inject(EodStockModel main);

}

