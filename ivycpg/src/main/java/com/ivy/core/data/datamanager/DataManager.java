package com.ivy.core.data.datamanager;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.db.AppDataManager;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;

public interface DataManager extends AppDataManager,SharedPreferenceHelper,AppDataProvider {

    int getSavedImageCount();

    String getMenuName(String menuCode);

    boolean isOpenOrderExisting();
}
