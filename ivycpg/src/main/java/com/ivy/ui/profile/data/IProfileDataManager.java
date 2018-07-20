package com.ivy.ui.profile.data;


import android.content.Context;
import com.ivy.sd.png.bo.LocationBO;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface ProfileDataManager {
    void loadContactTitle(Context context);
    void loadContactStatus();
    void downloadLinkRetailer();
    void downloadLocationMaster();
    LinkedHashMap<Integer, ArrayList<LocationBO>> getLocationListByLevId();
    void loadContractData();
    void getChannelMaster();
    void getPreviousProfileChanges(String RetailerID);

}
