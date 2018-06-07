package com.ivy.sd.png.view.reports.collectionreport;

import com.ivy.sd.png.view.reports.refactor.DayReportModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {CollectionModule.class})
public interface CollectionComponent {
    CollectionReportHelper provideCollectionReportHelper();
    void inject(CollectionReportModel main);
    void inject(CollectionFragmentAdapter main);
}
