package com.ivy.cpg.view.reports.currentreport;


import com.ivy.sd.png.bo.StockReportBO;

import java.util.Vector;

public interface ICurrentReportModelPresenter {
    void updateStockReportGrid(int productId, Vector<StockReportBO> myList);
}
