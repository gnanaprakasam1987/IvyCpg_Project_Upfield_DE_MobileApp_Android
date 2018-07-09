package com.ivy.cpg.view.reports.beginstockreport.data;

import android.content.Context;

import io.reactivex.Observable;

public interface BeginningReportManager {


    Observable downloadBeginningStock(Context context);
}
