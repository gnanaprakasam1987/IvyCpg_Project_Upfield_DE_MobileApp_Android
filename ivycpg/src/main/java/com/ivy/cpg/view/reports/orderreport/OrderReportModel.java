package com.ivy.cpg.view.reports.orderreport;

import android.content.Context;


import com.ivy.cpg.view.reports.dayreport.DaggerReportComponent;
import com.ivy.cpg.view.reports.dayreport.DayReportHelper;
import com.ivy.cpg.view.reports.dayreport.ReportComponent;
import com.ivy.cpg.view.reports.dayreport.ReportModule;
import com.ivy.sd.png.model.BusinessModel;

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
