package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;

import java.util.ArrayList;

public interface SellerPerformanceDetailContractor {

    interface SellerPerformanceDetailView{

        void updateSellerPerformanceData(SellerBo sellerBo);

        void updateChartInfo();

        void updateSellerTabViewInfo(SellerBo sellerBo);

        void updateSellerCallInfo(SellerBo sellerBo);

        void initializeMethods();
    }

    interface SellerPerformancePresenter{

        void setDetailView(SellerPerformanceDetailView sellerPerformanceView, Context context);

        void downloadSellerData(int userId,String date);

        void prepareChartData(final int userId,final String date);

        ArrayList<String> getChartDaysStr();

        ArrayList<Entry> getSellerCoveredEntry();

        ArrayList<Entry> getSellerBilledEntry();

        void setSellerActivityListener(final int userId, final String date);

        void downloadSellerKPI(int userId, String date, boolean isMTD);

        void downloadSellerOutletAWS(int userId,String date);

        void setSellerActivityDetailListener(int userId,String date);

        String calculateDuration(long startTime,long endTime);

        void removeFirestoreListener();

    }
}
