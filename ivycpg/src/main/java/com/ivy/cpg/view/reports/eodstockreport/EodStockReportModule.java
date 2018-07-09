package com.ivy.cpg.view.reports.eodstockreport;

import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EodStockReportModule {

    private BusinessModel application;

    @Inject
    public EodStockReportModule(BusinessModel application) {
        this.application = application;
    }

    @Provides
    EodReportHelper provideEodReportHelper() {
        return new EodReportHelper(application);
    }
}
