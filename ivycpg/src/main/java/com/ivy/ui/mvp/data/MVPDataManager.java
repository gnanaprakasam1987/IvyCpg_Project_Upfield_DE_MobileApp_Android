package com.ivy.ui.mvp.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface MVPDataManager extends AppDataManagerContract {

    Observable<ArrayList<MvpBO>> fetchSellerInfo();

    Observable<ArrayList<MvpBO>> fetchMvpKpiAchievements();

}
