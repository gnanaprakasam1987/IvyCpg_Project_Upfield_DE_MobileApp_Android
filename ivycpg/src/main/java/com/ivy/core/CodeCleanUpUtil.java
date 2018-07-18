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

    public void setUniqueId(String uniqueId) {
        appDataProvider.setUniqueId(uniqueId,true);
    }

    public void setBModelUniqueId(String uniqueId) {
        bmodel.outletTimeStampHelper.setUid(uniqueId);
    }

    public void setInTime(String inTime) {
        appDataProvider.setInTime(inTime,true);
    }

    public void setBModelInTime(String inTime) {
        bmodel.outletTimeStampHelper.setTimeIn(inTime);
    }

    public void setModuleTime(String moduleInTime) {
        appDataProvider.setModuleInTime(moduleInTime,true);
    }

    public void setBModelModuleTime(String moduleTime) {
        bmodel.outletTimeStampHelper.setTimeInModuleWise(moduleTime);
    }


    public void setRetailerMasterBO(RetailerMasterBO retailerMasterBO, boolean isFromProvider) {
        //TODO remove business model retailer master
        if (isFromProvider)
            bmodel.retailerMasterBO = retailerMasterBO;
    }


    public void setUserData(UserMasterBO userData) {
        appDataProvider.setCurrentUser(userData,true);
    }

    public void setBmodelUserBO(UserMasterBO userBO) {
        bmodel.userMasterHelper.setUserMasterBO(userBO);
    }

    public void setUserId(int userId){
        appDataProvider.getUser().setUserid(userId);
        bmodel.userMasterHelper.getUserMasterBO().setUserid(0);
    }

    public void setGlobalLocationId(int locationId){
        appDataProvider.setGlobalLocationIndex(locationId,true);
    }

    public void setBmodelGlobalLocationId(){

    }
}
