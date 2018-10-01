package com.ivy.cpg.view.reports.orderstatusreport;

import android.view.View;

/**
 * Created by anandasir on 28/5/18.
 */

public interface OrderStatusContractor {

    interface OrderStatusPresenter {

        void setView(OrderStatusView orderStatusView);

        void downloadOrderStatusReportList(boolean isOrderScreen);

        void filterList(String retailerID);
    }

    interface OrderStatusView {

        void initializeViews(View v);

        void setAdapter();

        void setSpinnerAdapter();

        void setEmptyView(String text);
    }

}
