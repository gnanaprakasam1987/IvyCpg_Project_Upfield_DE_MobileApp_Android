package com.ivy.sd.png.view.reports.currentReport;


import com.ivy.sd.png.bo.StockReportBO;

import java.util.Vector;

public interface ICurrentReportModelPresenter {
    void updateStockReportGrid(int productId, Vector<StockReportBO> myList);
}
