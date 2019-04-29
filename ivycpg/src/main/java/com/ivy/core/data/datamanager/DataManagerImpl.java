package com.ivy.core.data.datamanager;

import android.content.Context;
import android.os.Environment;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.db.AppDataManager;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class DataManagerImpl implements DataManager {


    private SharedPreferenceHelper mSharedPreferenceHelper;
    private AppDataManager appDataManager;
    private AppDataProvider appDataProvider;
    private ConfigurationMasterHelper configurationMasterHelper;
    private Context mContext;

    @Inject
    public DataManagerImpl(@ApplicationContext Context context, SharedPreferenceHelper sharedPreferenceHelper, AppDataManager appDataManager, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper) {
        this.mSharedPreferenceHelper = sharedPreferenceHelper;
        this.appDataManager = appDataManager;
        this.appDataProvider = appDataProvider;
        this.configurationMasterHelper = configurationMasterHelper;
        this.mContext = context;
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
        return appDataManager.getThemeColor();
    }

    @Override
    public Single<String> getFontSize() {
        return appDataManager.getFontSize();
    }

    @Override
    public Single<Double> getOrderValue() {
        return appDataManager.getOrderValue();
    }

    @Override
    public Single<Boolean> updateModuleTime(String moduleName) {
        return appDataManager.updateModuleTime(moduleName);
    }

    @Override
    public Single<Boolean> saveModuleCompletion(String menuName) {
        return appDataManager.saveModuleCompletion(menuName);
    }

    @Override
    public void setInTime(String inTime) {
        appDataProvider.setInTime(inTime);
    }

    @Override
    public void setInTime(String inTime, boolean isFromBModel) {
        appDataProvider.setInTime(inTime,isFromBModel);
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
    public void setUniqueId(String uniqueId, boolean isFromBModel) {
        appDataProvider.setUniqueId(uniqueId,isFromBModel);
    }

    @Override
    public String getUniqueId() {
        return appDataProvider.getUniqueId();
    }

    @Override
    public void setModuleInTime(String moduleInTime) {
        appDataProvider.setModuleInTime(moduleInTime);
    }

    @Override
    public void setModuleInTime(String moduleInTime, boolean isFromBModel) {
        appDataProvider.setModuleInTime(moduleInTime, isFromBModel);
    }

    @Override
    public String getModuleIntime() {
        return appDataProvider.getModuleIntime();
    }

    @Override
    public void setRetailerMaster(RetailerMasterBO retailerMaster) {
        appDataProvider.setRetailerMaster(retailerMaster);
    }

    @Override
    public RetailerMasterBO getRetailMaster() {
        return appDataProvider.getRetailMaster();
    }

    @Override
    public void setCurrentUser(UserMasterBO userData) {
        appDataProvider.setCurrentUser(userData);
    }

    @Override
    public void setCurrentUser(UserMasterBO userData, boolean isFromBModel) {
        appDataProvider.setCurrentUser(userData, isFromBModel);
    }

    @Override
    public UserMasterBO getUser() {
        return appDataProvider.getUser();
    }

    @Override
    public void setGlobalLocationIndex(int locationId) {
        appDataProvider.setGlobalLocationIndex(locationId);
    }

    @Override
    public void setGlobalLocationIndex(int locationId, boolean isFromBModel) {
        appDataProvider.setGlobalLocationIndex(locationId,isFromBModel);
    }

    @Override
    public int getGlobalLocationIndex() {
        return appDataProvider.getGlobalLocationIndex();
    }

    @Override
    public void setTodayBeatMaster(BeatMasterBO beatMaster) {
        appDataProvider.setTodayBeatMaster(beatMaster);
    }

    @Override
    public void setTodayBeatMaster(BeatMasterBO beatMaster, boolean isFromBModel) {
        appDataProvider.setTodayBeatMaster(beatMaster,isFromBModel);
    }

    @Override
    public BeatMasterBO getBeatMasterBo() {
        return appDataProvider.getBeatMasterBo();
    }

    @Override
    public void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters) {
        appDataProvider.setRetailerMasters(retailerMasters);
    }

    @Override
    public void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters, boolean isFromBModel) {
        appDataProvider.setRetailerMasters(retailerMasters,isFromBModel);
    }

    @Override
    public ArrayList<RetailerMasterBO> getRetailerMasters() {
        return appDataProvider.getRetailerMasters();
    }

    @Override
    public void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList) {
        appDataProvider.setSubDMasterList(subDMasterList);
    }

    @Override
    public void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList, boolean isFromBModel) {
        appDataProvider.setSubDMasterList(subDMasterList, isFromBModel);

    }

    @Override
    public ArrayList<RetailerMasterBO> getSubDMasterList() {
        return appDataProvider.getSubDMasterList();
    }

    @Override
    public void setPausedRetailer(RetailerMasterBO retailerMasterBO) {
          appDataProvider.setPausedRetailer(retailerMasterBO);
    }

    @Override
    public RetailerMasterBO getPausedRetailer() {
        return appDataProvider.getPausedRetailer();
    }

    @Override
    public Observable<ArrayList<ConfigureBO>> fetchNewActivityMenu(String menuName) {
        return appDataManager.fetchNewActivityMenu(menuName);
    }

    @Override
    public int getSavedImageCount() {

        int imageSize = 0;
        try {
            File f = new File(
                    mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            + "/" + DataMembers.photoFolderName + "/");
            if (f.exists()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {

                        return fileName.endsWith(".jpg");
                    }
                });

                File printfiles[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {

                        return fileName.startsWith("PF");
                    }
                });

                if (configurationMasterHelper.IS_PRINT_FILE_SAVE)
                    imageSize = files.length + printfiles.length;
                else
                    imageSize = files.length;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageSize;

    }

    @Override
    public String getMenuName(String menuCode) {
        String menuName = "";
        try {

            for (ConfigureBO configureBO : configurationMasterHelper.getConfig()) {
                if (configureBO.getConfigCode().equals(menuCode)) {
                    menuName = configureBO.getMenuName();
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return menuName;
    }

    @Override
    public void tearDown() {
        appDataManager.tearDown();
    }
}
