package com.ivy.cpg.view.reports.collectionreport;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {CollectionModule.class})
public interface CollectionComponent {
    CollectionReportHelper provideCollectionReportHelper();
    void inject(CollectionReportModel main);
    void inject(CollectionFragmentAdapter main);
}
