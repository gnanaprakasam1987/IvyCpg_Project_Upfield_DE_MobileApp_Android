package com.ivy.cpg.view.reports.eodstockreport;


import com.ivy.sd.png.bo.StockReportBO;

import java.util.ArrayList;

public interface IEodStockModelPresenter {
    void setAdapter();
    void downloadEodReport();
    ArrayList<StockReportBO> getEODReportList();



}
