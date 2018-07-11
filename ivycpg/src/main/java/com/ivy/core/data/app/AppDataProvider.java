package com.ivy.core.data.app;

import com.ivy.sd.png.bo.RetailerMasterBO;

public interface AppDataProvider {

    void setInTime(String inTime);

    String getInTime();

    void setUniqueId(String uniqueId);

    String getUniqueId();

    void setRetailerMaster(RetailerMasterBO retailerMaster);

    RetailerMasterBO getRetailMaster();
}
