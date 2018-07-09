package com.ivy.ui.reports.currentreport;


import android.content.Context;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;

import java.util.ArrayList;
import java.util.Vector;

public interface ICurrentReportContract {

    public interface ICurrentReportModelPresenter<V extends ICurrentReportContract.ICurrentReportView> extends BaseIvyPresenter<V> {
        void updateStockReportGrid(int productId, Vector<StockReportBO> myList);

        void setUpTitles();

        void setSihTitle(Object tag);

        void setLabelsMasterHelper(LabelsMasterHelper labelsMasterHelper);

        void setUserMasterHelper(UserMasterHelper userMasterHelper);

        void checkUserId();


        void downloadCurrentStockReport(Context context,BusinessModel bModel);

    }


    public interface ICurrentReportView extends BaseIvyView {

        void showNoProductError();

        void setAdapter(ArrayList<StockReportBO> myList, ConfigurationMasterHelper configurationMasterHelper);

        void setUpViewsVisible();

        void hideTitleViews();

        void setSihTitle(String s);

        void finishActivity();

        void showError();

        void setStockReportBOSList(Vector<StockReportBO> stockReportBO);

    }


}
