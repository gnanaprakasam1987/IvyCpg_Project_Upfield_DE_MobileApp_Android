package com.ivy.cpg.view.supervisor.mvp.sellerperformance;

public interface SellerPerformanceContractor {

    interface SellerPerformanceView{
        void displaySellerListData();
    }

    interface SellerPerformancePresenter{
        void getSellerListData();
    }
}
