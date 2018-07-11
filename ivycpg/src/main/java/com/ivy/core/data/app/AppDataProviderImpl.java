package com.ivy.core.data.app;

import android.content.Context;

import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.bo.RetailerMasterBO;
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


    @Override
    public void setInTime(String inTime) {
        //TODO to be removed post refactoring
        mContext.setInTime(inTime, true);

        this.inTime = inTime;
    }

    @Override
    public String getInTime() {
        return inTime;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        //TODO to be removed post refactoring
        mContext.setUniqueId(uniqueId, true);

        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setModuleInTime(String moduleInTime) {
        //TODO to be removed post refactoring
        mContext.setModuleTime(uniqueId, true);

        this.moduleInTime = moduleInTime;
    }

    @Override
    public String getModuleIntime() {
        return moduleInTime;
    }

    @Override
    public void setRetailerMaster(RetailerMasterBO retailerMaster) {
        //TODO to be removed post refactoring
        mContext.setRetailerMasterBO(retailerMaster, true);

        this.retailerMaster = retailerMaster;
    }

    @Override
    public RetailerMasterBO getRetailMaster() {
        return retailerMaster;
    }
}
