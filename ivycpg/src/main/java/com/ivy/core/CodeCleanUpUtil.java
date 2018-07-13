package com.ivy.core;

import android.content.Context;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;

public class CodeCleanUpUtil {

    private static CodeCleanUpUtil instance = null;

    private BusinessModel bmodel;

    private AppDataProvider appDataProvider;

    private CodeCleanUpUtil(Context context, AppDataProvider appDataProvider) {
        this.bmodel = (BusinessModel) context;
        this.appDataProvider = appDataProvider;
    }

    public static CodeCleanUpUtil getInstance(Context context, AppDataProvider appDataProvider) {
        if (instance == null) {
            instance = new CodeCleanUpUtil(context, appDataProvider);
        }
        return instance;
    }

    public void setUniqueId(String uniqueId, boolean isFromProvider) {
        if (!isFromProvider)
            appDataProvider.setUniqueId(uniqueId);
        else
            bmodel.outletTimeStampHelper.setUid(uniqueId);
    }

    public void setInTime(String inTime, boolean isFromProvider) {
        if (!isFromProvider)
            appDataProvider.setInTime(inTime);
        else
            bmodel.outletTimeStampHelper.setTimeIn(inTime);
    }

    public void setModuleTime(String moduleInTime, boolean isFromProvider) {
        if (!isFromProvider)
            appDataProvider.setModuleInTime(moduleInTime);
        else
            bmodel.outletTimeStampHelper.setTimeInModuleWise(moduleInTime);
    }


    public void setRetailerMasterBO(RetailerMasterBO retailerMasterBO, boolean isFromProvider) {
        //TODO remove business model retailer master
        if (isFromProvider)
            bmodel.retailerMasterBO = retailerMasterBO;
    }

    public void setUserId(int userId, boolean isFromProvider) {
        if (isFromProvider)
            bmodel.userMasterHelper.getUserMasterBO().setUserid(userId);
        else
            appDataProvider.setUserId(userId);

    }

    public void setDistributionId(int distributionId, boolean isFromProvider) {
        if (isFromProvider)
            bmodel.userMasterHelper.getUserMasterBO().setDistributorid(distributionId);
        else
            appDataProvider.setDistributionId(distributionId);

    }


    public void setBranchId(int distributionId, boolean isFromProvider) {
        if (isFromProvider)
            bmodel.userMasterHelper.getUserMasterBO().setBranchId(distributionId);
        else
            appDataProvider.setBranchId(distributionId);

    }

    public void setDownloadDate(String downloadDate, boolean isFromProvider) {
        if (isFromProvider)
            bmodel.userMasterHelper.getUserMasterBO().setDownloadDate(downloadDate);
        else
            appDataProvider.setDownloadDate(downloadDate);

    }

    public void setUserData(UserMasterBO userData, boolean isFromProvider) {
        if (isFromProvider)
            bmodel.userMasterHelper.setUserMasterBO(userData);
        else
            appDataProvider.setCurrentUser(userData);
    }
}
