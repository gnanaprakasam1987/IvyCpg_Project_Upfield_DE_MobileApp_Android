package com.ivy.cpg.view.reports.orderstatusreport;

import android.content.Context;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by anandasir on 28/5/18.
 */

public class OrderStatusPresenterImpl implements OrderStatusContractor.OrderStatusPresenter {
    private final Context context;
    private final BusinessModel businessModel;
    private OrderStatusContractor.OrderStatusView orderStatusView;
    Vector<OrderStatusReportBO> orderStatusReportList;
    Vector<OrderStatusRetailerReportBO> orderStatusRetailerReportList;

    OrderStatusPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        businessModel.configurationMasterHelper.loadOrderStatusReportConfiguration();
    }

    @Override
    public void setView(OrderStatusContractor.OrderStatusView orderStatusView) {
        this.orderStatusView = orderStatusView;
    }

    @Override
    public void downloadOrderStatusReportList() {
        try {
            businessModel.orderStatusReportHelper.getOrderStatusList();
            businessModel.orderStatusReportHelper.getOrderStatusRetailerList();
//        if (businessModel.configurationMasterHelper.IS_ENABLE_ORDER_STATUS_REPORT) {
//            if (!businessModel.configurationMasterHelper.IS_ORDER_STATUS_REPORT) {
//                businessModel.orderStatusReportHelper.getInvoiceStatusList();
//                businessModel.orderStatusReportHelper.getInvoiceStatusRetailerList();
//            }
//        }
            if (businessModel.orderStatusReportHelper.getOrderStatusReportList() == null ||
                    businessModel.orderStatusReportHelper.getOrderStatusReportList().size() == 0) {
                orderStatusView.setEmptyView(context.getResources().getString(R.string.no_data_exists));
                return;
            }
            setOrderStatusReportList(businessModel.orderStatusReportHelper.getOrderStatusReportList());
            setOrderStatusRetailerReportList(businessModel.orderStatusReportHelper.getOrderStatusRetailerReportList());

            orderStatusView.setAdapter();
            orderStatusView.setSpinnerAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<OrderStatusReportBO> getOrderStatusReportList() {
        return orderStatusReportList;
    }

    public void setOrderStatusReportList(Vector<OrderStatusReportBO> orderStatusReportList) {
        this.orderStatusReportList = orderStatusReportList;
    }

    public Vector<OrderStatusRetailerReportBO> getOrderStatusRetailerReportList() {
        return orderStatusRetailerReportList;
    }

    public void setOrderStatusRetailerReportList(Vector<OrderStatusRetailerReportBO> orderStatusRetailerReportList) {
        this.orderStatusRetailerReportList = orderStatusRetailerReportList;
    }

    @Override
    public void filterList(String retailerID) {
        setOrderStatusReportList(businessModel.orderStatusReportHelper.filterRetailerList(retailerID));
        orderStatusView.setAdapter();
    }
}
