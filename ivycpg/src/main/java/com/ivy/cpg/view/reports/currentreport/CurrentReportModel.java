package com.ivy.cpg.view.reports.currentreport;


import android.content.Context;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Vector;

public class CurrentReportModel implements ICurrentReportModelPresenter,
        CurrentReportViewAdapter.CurrentReportViewAdapterCallback {
    private ICurrentReportView currentReportView;
    private BusinessModel businessModel;
    private Context mContext;

    public CurrentReportModel(Context activity, BusinessModel bModel,
                              CurrentReportViewFragment currentReportViewFragment) {
        this.mContext = activity;
        this.businessModel = bModel;
        this.currentReportView = currentReportViewFragment;
    }

    @Override
    public void updateStockReportGrid(int brandId, Vector<StockReportBO> myList) {
        if (myList == null) {
            businessModel.showAlert(
                    mContext.getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = myList.size();
        ArrayList<StockReportBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            StockReportBO ret = myList.get(i);
            if (brandId == 0 || brandId == ret.getBrandId()) {
                temp.add(ret);
            }
        }
        CurrentReportViewAdapter mSchedule = new CurrentReportViewAdapter(temp, mContext, businessModel, CurrentReportModel.this);
        currentReportView.setAdapter(mSchedule);
    }

    @Override
    public void productName(String pName) {
        currentReportView.setProductName(pName);
    }
}
