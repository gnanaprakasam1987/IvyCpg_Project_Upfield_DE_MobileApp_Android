package com.ivy.core.di.module;

import dagger.Module;
import dagger.Provides;

import android.content.Context;

import com.ivy.core.IvyConstants;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.datamanager.DataManagerImpl;
import com.ivy.core.data.db.DBHelperImpl;
import com.ivy.core.data.db.DbHelper;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelperImpl;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.PreferenceInfo;

import javax.inject.Singleton;

@Module
public class IvyAppModule {

    private Context mContext;


    public IvyAppModule(Context context) {
        mContext = context;
    }

    @Provides
    @ApplicationContext
    protected Context providesContext() {
        return mContext;
    }

    @Provides
    @PreferenceInfo
    protected String providesSharedPreferenceName() {
        return IvyConstants.IVY_PREFERENCE_NAME;
    }

    @Provides
    @Singleton
    protected DbHelper providesDbHelper(DBHelperImpl dbHelper) {
        return dbHelper;
    }

    @Provides
    @Singleton
    protected SharedPreferenceHelper providesSharedPreferenceHelper(SharedPreferenceHelperImpl sharedPreferenceHelper) {
        return sharedPreferenceHelper;
    }


    @Provides
    @Singleton
    protected DataManager providesDataManager(DataManagerImpl dataManager) {
        return dataManager;
    }


}
