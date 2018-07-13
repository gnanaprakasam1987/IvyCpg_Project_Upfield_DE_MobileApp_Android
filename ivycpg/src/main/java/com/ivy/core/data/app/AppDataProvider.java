package com.ivy.core.data.app;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;

public interface AppDataProvider {

    void setInTime(String inTime);

    String getInTime();

    void setUniqueId(String uniqueId);

    String getUniqueId();

    void setModuleInTime(String moduleInTime);

    String getModuleIntime();

    void setRetailerMaster(RetailerMasterBO retailerMaster);

    RetailerMasterBO getRetailMaster();

    void setUserId(int userId);

    int getUserId();

    void setDistributionId(int distributionId);

    int getDistributionId();

    void setBranchId(int branchId);

    int getBranchId();

    void setDownloadDate(String downloadDate);

    String getDownloadDate();

    void setCurrentUser(UserMasterBO userData);

    UserMasterBO getUser();


}
