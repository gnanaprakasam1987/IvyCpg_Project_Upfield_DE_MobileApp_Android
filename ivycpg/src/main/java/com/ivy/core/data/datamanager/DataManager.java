package com.ivy.core.data.datamanager;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.db.DbHelper;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;

public interface DataManager extends DbHelper,SharedPreferenceHelper,AppDataProvider {
}
