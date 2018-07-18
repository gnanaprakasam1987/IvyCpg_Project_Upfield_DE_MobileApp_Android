package com.ivy.core.data.app;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;

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


}
