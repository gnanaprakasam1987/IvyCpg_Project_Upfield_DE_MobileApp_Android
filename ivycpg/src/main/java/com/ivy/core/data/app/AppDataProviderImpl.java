package com.ivy.core.data.app;

import android.content.Context;

import com.ivy.core.CodeCleanUpUtil;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

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

    /**/
    private int globalLocationIndex;


    private BeatMasterBO todayBeatMaster;

    private ArrayList<RetailerMasterBO> retailerMasterList;

    private ArrayList<RetailerMasterBO> subDMasterList;

    private RetailerMasterBO pausedRetailer;

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
    public void setCurrentUser(UserMasterBO userData, boolean isFromBModel) {
        this.userData = userData;
    }

    @Override
    public UserMasterBO getUser() {
        return userData;
    }

    @Override
    public void setGlobalLocationIndex(int locationIndex) {
        //TODO to be removed post refactoring
        mContext.codeCleanUpUtil.setBmodelGlobalLocationId(locationIndex);

        this.globalLocationIndex = locationIndex;
    }

    @Override
    public void setGlobalLocationIndex(int locationIndex, boolean isFromBModel) {
        this.globalLocationIndex = locationIndex;
    }

    @Override
    public int getGlobalLocationIndex() {
        return globalLocationIndex;
    }

    @Override
    public void setTodayBeatMaster(BeatMasterBO beatMaster) {

        mContext.codeCleanUpUtil.setBModelTodayBeatMaster(beatMaster);

        this.todayBeatMaster = beatMaster;
    }

    @Override
    public void setTodayBeatMaster(BeatMasterBO beatMaster, boolean isFromBModel) {

        this.todayBeatMaster = beatMaster;
    }

    @Override
    public BeatMasterBO getBeatMasterBo() {
        return todayBeatMaster;
    }

    @Override
    public void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters) {

        this.retailerMasterList=retailerMasters;
        mContext.codeCleanUpUtil.setBmodelRetailerMaster(retailerMasters);
    }

    @Override
    public void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters, boolean isFromBModel) {

        this.retailerMasterList=retailerMasters;
    }

    @Override
    public ArrayList<RetailerMasterBO> getRetailerMasters() {
        return retailerMasterList;
    }

    @Override
    public void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList) {
        mContext.codeCleanUpUtil.setBmodelSubDMaster(subDMasterList);
        this.subDMasterList=subDMasterList;
    }

    @Override
    public void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList, boolean isFromBModel) {
        this.subDMasterList =subDMasterList;
    }

    @Override
    public ArrayList<RetailerMasterBO> getSubDMasterList() {
        return subDMasterList;
    }

    @Override
    public void setPausedRetailer(RetailerMasterBO retailerMasterBO) {
        this.pausedRetailer = retailerMasterBO;
    }

    @Override
    public RetailerMasterBO getPausedRetailer() {
        return pausedRetailer;
    }
}
