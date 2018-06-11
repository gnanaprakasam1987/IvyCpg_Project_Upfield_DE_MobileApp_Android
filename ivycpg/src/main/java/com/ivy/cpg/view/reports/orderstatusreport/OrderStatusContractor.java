package com.ivy.cpg.view.reports.orderstatusreport;

import android.view.View;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.cpg.view.login.LoginContractor;

import java.util.HashMap;
import java.util.Vector;

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
