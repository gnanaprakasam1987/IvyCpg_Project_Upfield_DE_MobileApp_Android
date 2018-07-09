package com.ivy.ui.reports.currentreport.data;


import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;

import io.reactivex.Observable;

public interface CurrentReportManager {
    Observable downloadCurrentStockReport(Context context, BusinessModel bModel);
}
