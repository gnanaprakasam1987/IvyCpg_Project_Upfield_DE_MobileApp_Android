package com.ivy.cpg.view.reports.collectionreport;

import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CollectionModule {
    private BusinessModel application;

    @Inject
    public CollectionModule(BusinessModel application) {
        this.application = application;
    }

    @Singleton
    @Provides
    CollectionReportHelper provideReportHelper() {
        return new CollectionReportHelper(application);
    }
}


