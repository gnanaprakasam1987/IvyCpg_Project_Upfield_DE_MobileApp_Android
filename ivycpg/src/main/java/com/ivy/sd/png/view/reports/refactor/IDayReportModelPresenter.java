package com.ivy.sd.png.view.reports.refactor;

/**
 * Created by velraj.p on 5/24/2018.
 */

public interface IDayReportModelPresenter {
    void downloadData();
    byte[] printDataFor3InchPrinter();

    void destroy();
}
