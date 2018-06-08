package com.ivy.cpg.view.reports.collectionreport;


import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;

public class CollectionReportModel implements ICollectionReportModelPresenter {

    private Context mContext;
    private ICollectionReportView mCollectionReportView;

    private CollectionReportHelper collectionReportHelper;


    public CollectionReportModel(Context activity,
                                 CollectionReportFragmentNew collectionReportFragmentNew) {
        this.mContext = activity;
        mCollectionReportView = collectionReportFragmentNew;
        CollectionComponent collectionComponent = DaggerCollectionComponent.builder().
                collectionModule(new CollectionModule((BusinessModel) mContext.getApplicationContext())).build();
        collectionReportHelper = collectionComponent.provideCollectionReportHelper();
        collectionComponent.inject(this);
    }

    @Override
    public void setUpAdapter() {
        CollectionFragmentAdapter collectionAdapter = new CollectionFragmentAdapter(mContext, (BusinessModel) mContext.getApplicationContext());
        mCollectionReportView.setAdapter(collectionAdapter);
    }

    @Override
    public void loadCollectionReport() {
        collectionReportHelper.loadCollectionReport();
    }
}
