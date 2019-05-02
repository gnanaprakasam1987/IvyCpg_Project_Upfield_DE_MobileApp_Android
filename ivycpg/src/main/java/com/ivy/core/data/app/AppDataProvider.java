package com.ivy.core.data.app;

import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;

import java.util.ArrayList;

public interface AppDataProvider {

    void setInTime(String inTime);

    void setInTime(String inTime, boolean isFromBModel);

    String getInTime();


    void setUniqueId(String uniqueId);

    void setUniqueId(String uniqueId, boolean isFromBModel);

    String getUniqueId();


    void setModuleInTime(String moduleInTime);

    void setModuleInTime(String moduleInTime, boolean isFromBModel);

    String getModuleIntime();


    void setRetailerMaster(RetailerMasterBO retailerMaster);

    RetailerMasterBO getRetailMaster();


    void setCurrentUser(UserMasterBO userData);

    void setCurrentUser(UserMasterBO userData, boolean isFromBModel);

    UserMasterBO getUser();


    void setGlobalLocationIndex(int locationId);

    void setGlobalLocationIndex(int locationId, boolean isFromBModel);

    int getGlobalLocationIndex();


    void setTodayBeatMaster(BeatMasterBO beatMaster);

    void setTodayBeatMaster(BeatMasterBO beatMaster,  boolean isFromBModel);

    BeatMasterBO getBeatMasterBo();


    void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters);

    void setRetailerMasters(ArrayList<RetailerMasterBO> retailerMasters,  boolean isFromBModel);

    ArrayList<RetailerMasterBO> getRetailerMasters();


    void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList);

    void setSubDMasterList(ArrayList<RetailerMasterBO> subDMasterList,  boolean isFromBModel);

    ArrayList<RetailerMasterBO> getSubDMasterList();

    void setPausedRetailer(RetailerMasterBO retailerMasterBO);

    RetailerMasterBO getPausedRetailer();


}
