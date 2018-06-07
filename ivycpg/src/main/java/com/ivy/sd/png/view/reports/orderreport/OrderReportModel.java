package com.ivy.sd.png.view.reports.orderreport;

import android.content.Context;


import com.ivy.cpg.view.reports.OrderReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.reports.component.DaggerReportComponent;
import com.ivy.sd.png.view.reports.component.ReportComponent;
import com.ivy.sd.png.view.reports.module.ReportModule;
import com.ivy.sd.png.view.reports.refactor.DayReportHelper;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by velraj.p on 6/1/2018.
 */

public class OrderReportModel implements IOrderReportModelPresenter {
    private Context mContext;
    private IOrderReportView mOrderReportView;
    @Inject
    public DayReportHelper reportHelper;

    public OrderReportModel(Context activityContext, IOrderReportView iOrderReportView) {
        this.mContext = activityContext;
        this.mOrderReportView = iOrderReportView;
        ReportComponent reportComponent = DaggerReportComponent.builder().reportModule(new ReportModule((BusinessModel) mContext.getApplicationContext())).build();
        reportHelper =    reportComponent.provideDayReportHelper();
    }

    @Override
    public ArrayList<OrderReportBO> getOrderReport() {
        return reportHelper.downloadOrderReport();
    }
}
