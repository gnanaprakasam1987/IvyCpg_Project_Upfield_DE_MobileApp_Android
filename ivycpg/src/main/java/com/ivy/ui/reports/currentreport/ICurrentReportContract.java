package com.ivy.ui.reports.currentreport;


import android.content.Context;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.ChildLevelBo;
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

        void downLoadUserDetails();

        void checkUserId();


        void downloadCurrentStockReport();

        void updateBaseUOM(Context context,String order, int type);

        void getSpinnerData();


    }


    public interface ICurrentReportView extends BaseIvyView {

        void showNoProductError();

        void setAdapter(ArrayList<StockReportBO> myList, ConfigurationMasterHelper configurationMasterHelper);

        void setUpViewsVisible();

        void hideTitleViews();

        void setSihTitle(String s);

        void finishActivity();

        void showError(String message);

        void setStockReportBOSList(Vector<StockReportBO> stockReportBO);

        void setUpBrandSpinner(Vector<ChildLevelBo> items);


    }


}
