package com.ivy.core.data.datamanager;

import android.content.Context;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.db.DbHelper;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.bo.RetailerMasterBO;

import javax.inject.Inject;

import io.reactivex.Single;

public class DataManagerImpl implements DataManager {

    private Context mContext;

    private SharedPreferenceHelper mSharedPreferenceHelper;
    private DbHelper dbHelper;
    private AppDataProvider appDataProvider;

    @Inject
    public DataManagerImpl(@ApplicationContext Context context, SharedPreferenceHelper sharedPreferenceHelper, DbHelper dbHelper, AppDataProvider appDataProvider) {
        this.mContext = context;
        this.mSharedPreferenceHelper = sharedPreferenceHelper;
        this.dbHelper = dbHelper;
        this.appDataProvider=appDataProvider;
    }

    @Override
    public String getBaseUrl() {
        return mSharedPreferenceHelper.getBaseUrl();
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        mSharedPreferenceHelper.setBaseUrl(baseUrl);
    }

    @Override
    public String getApplicationName() {
        return mSharedPreferenceHelper.getApplicationName();
    }

    @Override
    public void setApplicationName(String applicationName) {
        mSharedPreferenceHelper.setApplicationName(applicationName);
    }

    @Override
    public String getActivationKey() {
        return mSharedPreferenceHelper.getActivationKey();
    }

    @Override
    public void setActivationKey(String activationKey) {
        mSharedPreferenceHelper.setActivationKey(getActivationKey());
    }

    @Override
    public String getPreferredLanguage() {
        return mSharedPreferenceHelper.getPreferredLanguage();
    }

    @Override
    public void setPreferredLanguage(String language) {
        mSharedPreferenceHelper.setPreferredLanguage(language);
    }

    @Override
    public Single<String> getThemeColor() {
        return dbHelper.getThemeColor();
    }

    @Override
    public Single<String> getFontSize() {
        return dbHelper.getFontSize();
    }

    @Override
    public void setInTime(String inTime) {
        appDataProvider.setInTime(inTime);
    }

    @Override
    public String getInTime() {
        return appDataProvider.getInTime();
    }

    @Override
    public void setUniqueId(String uniqueId) {
        appDataProvider.setUniqueId(uniqueId);
    }

    @Override
    public String getUniqueId() {
        return appDataProvider.getUniqueId();
    }

    @Override
    public void setRetailerMaster(RetailerMasterBO retailerMaster) {
        appDataProvider.setRetailerMaster(retailerMaster);
    }

    @Override
    public RetailerMasterBO getRetailMaster() {
        return appDataProvider.getRetailMaster();
    }
}
