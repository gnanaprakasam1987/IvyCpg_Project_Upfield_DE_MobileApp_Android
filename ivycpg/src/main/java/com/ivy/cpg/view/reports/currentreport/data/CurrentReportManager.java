package com.ivy.cpg.view.reports.currentreport.data;


import android.content.Context;

import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

import io.reactivex.Observable;

public interface CurrentReportManager {
    Observable downloadCurrentStockReport(Context context, BusinessModel bModel);
}
