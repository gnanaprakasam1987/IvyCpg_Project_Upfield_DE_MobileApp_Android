package com.ivy.cpg.view.reports.dayreport;

/**
 * Created by velraj.p on 5/24/2018.
 */

public interface DayReportPresenter {
    void downloadData();
    byte[] printDataFor3InchPrinter();

    void destroy();
}
