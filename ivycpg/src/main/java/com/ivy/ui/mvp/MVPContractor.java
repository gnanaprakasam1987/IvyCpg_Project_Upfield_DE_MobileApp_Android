package com.ivy.ui.mvp;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.mvp.model.MVPKPIGroupBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MVPContractor extends BaseIvyView {

    interface MVPView extends BaseIvyView{

        void setProfileImage();

        void populateHierarchy(ArrayList<String> mvpGroupList);

        void setSellerRanking(String groupName);

        void populateAchievementList();

        void setDashboardListAdapter(List<DashBoardBO> dashBoardBOS);

        void populateKPIFilter();

        void populateRankingList();

        void setSellerKPIDetails(ArrayList<DashBoardBO> dashBoardList);

        void showUserImageAlert();
    }

    interface MVPPresenter<V extends MVPView> extends BaseIvyPresenter<V>{

        //Seller Info
        UserMasterBO getUserInfo();

        void fetchSellerInfo();

        ArrayList<MvpBO> getMvpUserList();

        ArrayList<MvpBO> getMvpKPIList();

        //Achievements Info
        void fetchListRowLabels();

        HashMap<String, String> getLabelsMap();

        void fetchSellerDashboardForUserAndInterval(String selectedUser, String interval);

        //Ranking Info
        ArrayList<MVPKPIGroupBO> getMvpKPIGroupList();

        void fetchHierarchicalStructure();

        void fetchSellerDashboardDetails();

        //Badge Info
        void fetchBadgeList();

        void updateUserProfile(UserMasterBO userInfo);
    }
}
