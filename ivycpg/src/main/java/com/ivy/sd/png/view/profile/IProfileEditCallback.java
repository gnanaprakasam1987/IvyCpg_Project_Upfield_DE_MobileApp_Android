package com.ivy.sd.png.view.profile;


import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;

import java.util.ArrayList;
import java.util.Vector;

public interface IProfileEditCallback {

    void onVerifyOTPCompleted(Integer integer , String type);
    void OnDownloadTaskCompleted(Vector<ChannelBO> channelMaster,
                                 ArrayList<LocationBO> mLocationMasterList1,
                                 ArrayList<LocationBO> mLocationMasterList2,
                                 ArrayList<LocationBO> mLocationMasterList3);
}
