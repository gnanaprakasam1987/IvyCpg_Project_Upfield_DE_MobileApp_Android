package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.content.Context;

public interface SellerViewContractor {

    interface SellerView{
        void displaySellerList();
    }

    interface SellerViewPresenter{
        void getSellerList(Context context,int sellerInfoType);
    }
}
