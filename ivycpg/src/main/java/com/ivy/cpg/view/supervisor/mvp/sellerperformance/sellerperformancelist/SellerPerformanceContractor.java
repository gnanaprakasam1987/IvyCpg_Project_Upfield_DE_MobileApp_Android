package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;

import java.util.ArrayList;

public interface SellerPerformanceContractor {

    interface SellerPerformanceView{

        void updateSellerPerformanceList(ArrayList<SellerBo> sellerList);

        void updateChartInfo();

        void notifyListChange();

    }

    interface SellerPerformancePresenter{

        void setView(SellerPerformanceContractor.SellerPerformanceView sellerPerformanceView, Context context);

        void getSellerListAWS(String date);

        void sellerActivityInfoListener(int userId,String date);

        void prepareChartData(final int userId,final String date);

        ArrayList<String> getChartDaysStr();

        ArrayList<Entry> getSellerCoveredEntry();

        ArrayList<Entry> getSellerBilledEntry();
    }

    interface SellerPerformanceDetailView{

        void updateSellerPerformanceData(SellerBo sellerBo);

        void updateChartInfo();
    }
}
