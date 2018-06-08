package com.ivy.cpg.view.reports.eodstockreport;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {EodStockReportModule.class})
public interface EodReportComponent {

    EodReportHelper provideEodReport();
    void inject(EodStockModel main);

}

