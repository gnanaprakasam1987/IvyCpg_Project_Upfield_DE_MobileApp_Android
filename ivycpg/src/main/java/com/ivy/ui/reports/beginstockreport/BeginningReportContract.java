package com.ivy.ui.reports.beginstockreport;


import android.content.Context;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;

import java.util.Vector;

public interface BeginningReportContract {


    interface IBeginningStockView extends BaseIvyView {
        void setAdapter(Vector<StockReportMasterBO> stockReportMasterBOS, ConfigurationMasterHelper configurationMasterHelper);

        void showError();

        void hideCaseTitle();

        void setCaseTitle(String s);

        void hidePieceTitle();


        void setPieceTitle(String s);

        void setTotalTitle(String s);

        void finishActivity();

    }

    interface IBeginningStockModelPresenter<V extends BeginningReportContract.IBeginningStockView> extends BaseIvyPresenter<V> {
        void downloadBeginningStock(Context context);

        void showCaseOrder(Object tag);

        void showPieceOrder(Object pieceTag);

        boolean ifShowCase();

        void setLabelsMasterHelper(LabelsMasterHelper labelsMasterHelper);

        void setCaseAndPieceTitle(Object tag,Object pieceTag);


        void showTotalTitle(Object tag);

        void setUserMasterHelper(UserMasterHelper userMasterHelper);

        void checkUserId();

    }
}
