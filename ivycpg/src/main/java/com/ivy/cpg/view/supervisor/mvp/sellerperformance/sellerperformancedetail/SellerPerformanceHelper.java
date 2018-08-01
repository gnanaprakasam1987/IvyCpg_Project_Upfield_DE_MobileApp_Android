package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;


import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class SellerPerformanceHelper {

    private String[] mMonths = new String[]{
            "May 8","May 09","May 10"
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
}
