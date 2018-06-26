package com.ivy.cpg.view.supervisor.mvp.sellerperformance;


import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class SellerPerformanceHelper implements SellerPerformanceContractor.SellerPerformancePresenter{

    private String[] mMonths = new String[]{
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    };

    SellerPerformanceHelper() {
    }

    String[] getSellerPerformanceList() {
        return mMonths;
    }

    ArrayList<BarEntry> getBarEntries() {
        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();

        for (int index = 0; index < 10; index++) {
            entries1.add(new BarEntry(5, 55));
        }

        return entries1;
    }

    @Override
    public void getSellerListData() {

    }
}
