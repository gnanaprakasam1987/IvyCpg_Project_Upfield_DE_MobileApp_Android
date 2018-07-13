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

    /*Current User Id*/
    private int userId;

    /*Current User's Distribution Id*/
    private int distributionId;

    /*Current User's Branch Id*/
    private int branchId;

    /*Data download date*/
    private String downloadDate;

    @Override
    public void setInTime(String inTime) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setInTime(inTime, true);

        this.inTime = inTime;
    }

    @Override
    public String getInTime() {
        return inTime;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setUniqueId(uniqueId, true);

        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setModuleInTime(String moduleInTime) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setModuleTime(uniqueId, true);

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
    public void setUserId(int userId) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setUserId(userId, true);

        this.userId = userId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void setDistributionId(int distributionId) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setDistributionId(distributionId, true);

        this.distributionId = distributionId;
    }

    @Override
    public int getDistributionId() {
        return distributionId;
    }

    @Override
    public void setBranchId(int branchId) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBranchId(branchId, true);

        this.branchId = branchId;
    }

    @Override
    public int getBranchId() {
        return branchId;
    }

    @Override
    public void setDownloadDate(String downloadDate) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setDownloadDate(downloadDate, true);

        this.downloadDate = downloadDate;
    }

    @Override
    public String getDownloadDate() {
        return downloadDate;
    }

    @Override
    public void setCurrentUser(UserMasterBO userData) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setUserData(userData, true);

        this.userData = userData;
    }

    @Override
    public UserMasterBO getUser() {
        return userData;
    }
}
