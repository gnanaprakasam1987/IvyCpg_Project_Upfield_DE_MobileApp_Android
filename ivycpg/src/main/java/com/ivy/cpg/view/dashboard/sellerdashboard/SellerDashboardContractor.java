package com.ivy.cpg.view.dashboard.sellerdashboard;

import com.ivy.sd.png.commons.KeyPairBoolData;

import java.util.List;

public interface SellerDashboardContractor {

    interface SellerDashView{
        void loadUserSpinner(String mFilteredUser,List<KeyPairBoolData> userArray);
    }

    interface SellerDashPresenter{
        void setView(SellerDashView view);
        void gridListDataLoad(int position);
        void updateUserData(String distrubutorIds);
        void computeDayAchivements();
    }
}

