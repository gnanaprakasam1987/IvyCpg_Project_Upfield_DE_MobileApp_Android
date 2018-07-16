package com.ivy.core.data.app;

import android.content.Context;

import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;

public class AppDataProviderImpl implements AppDataProvider {

    private BusinessModel mContext;

    @Inject
    public AppDataProviderImpl(@ApplicationContext Context context) {
        mContext = (BusinessModel) context;
    }

    /* In time at a store */
    private String inTime;

    /*In time in to a module*/
    private String moduleInTime;

    /* Unique Id*/
    private String uniqueId;

    /*Current Retailer*/
    private RetailerMasterBO retailerMaster;

    /*Current User*/
    private UserMasterBO userData;


    @Override
    public void setInTime(String inTime) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBModelInTime(inTime);

        this.inTime = inTime;
    }

    @Override
    public void setInTime(String inTime, boolean isFromBModel) {

        this.inTime = inTime;
    }

    @Override
    public String getInTime() {
        return inTime;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBModelUniqueId(uniqueId);

        this.uniqueId = uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId, boolean isFromBModel) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setModuleInTime(String moduleInTime) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBModelModuleTime(uniqueId);

        this.moduleInTime = moduleInTime;
    }

    @Override
    public void setModuleInTime(String moduleInTime, boolean isFromBModel) {
        this.moduleInTime = moduleInTime;
    }

    @Override
    public String getModuleIntime() {
        return moduleInTime;
    }

    @Override
    public void setRetailerMaster(RetailerMasterBO retailerMaster) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setRetailerMasterBO(retailerMaster, true);

        this.retailerMaster = retailerMaster;
    }

    @Override
    public RetailerMasterBO getRetailMaster() {
        return retailerMaster;
    }


    @Override
    public void setCurrentUser(UserMasterBO userData) {

        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBmodelUserBO(userData);

        this.userData = userData;
    }

    @Override
    public void setCurrentUser(UserMasterBO userData, boolean isFromBModelF) {
        this.userData = userData;
    }

    @Override
    public UserMasterBO getUser() {
        return userData;
    }
}
