package com.ivy.sd.png.view.reports.collectionreport;


import android.content.Context;
import android.support.v4.app.FragmentActivity;

public class CollectionReportModel implements ICollectionReportModelPresenter {

    private Context mContext;
    private ICollectionReportView mCollectionReportView;

    public CollectionReportModel(Context activity,
                                 CollectionReportFragmentNew collectionReportFragmentNew) {
        this.mContext = activity;
        mCollectionReportView = collectionReportFragmentNew;

    }

    @Override
    public void setUpAdapter() {
        CollectionFragmentAdapter collectionAdapter = new CollectionFragmentAdapter(mContext);
        mCollectionReportView.setAdapter(collectionAdapter);

    }
}
