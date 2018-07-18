package com.ivy.ui.reports.currentreport.data;


import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductHelper;

import io.reactivex.Observable;

public interface CurrentReportManager {
    Observable downloadCurrentStockReport( ProductHelper productHelper);

    void updateBaseUOM(Context context, String order, int type);
}
