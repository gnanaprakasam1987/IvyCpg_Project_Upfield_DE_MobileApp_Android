package com.ivy.cpg.view.profile;


import android.os.AsyncTask;

import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class ProfileEditDownloadTask extends AsyncTask<Integer,Integer,Boolean> {

    private BusinessModel bmodel=null;
    private IProfileEditCallback editCallback;

    private Vector<ChannelBO> channelMaster=null;
    private ArrayList<LocationBO> mLocationMasterList1=null;
    private ArrayList<LocationBO> mLocationMasterList2=null;
    private ArrayList<LocationBO> mLocationMasterList3=null;

    public ProfileEditDownloadTask(BusinessModel bmodel,IProfileEditCallback editCallback) {
        this.bmodel = bmodel;
        this.editCallback=editCallback;
    }


    @Override
    protected Boolean doInBackground(Integer... integers) {

        bmodel.newOutletHelper.loadContactTitle();
        bmodel.newOutletHelper.loadContactStatus();
        bmodel.newOutletHelper.getPreviousProfileChanges(bmodel.getRetailerMasterBO().getRetailerID());
        bmodel.newOutletHelper.downloadLinkRetailer();
        bmodel.newOutletHelper.downloadLocationMaster();

        LinkedHashMap<Integer, ArrayList<LocationBO>> locationListByLevId = bmodel.newOutletHelper.getLocationListByLevId();
        if (locationListByLevId != null) {
            int count = 0;
            for (Map.Entry<Integer, ArrayList<LocationBO>> entry : locationListByLevId.entrySet()) {
                count++;
                Commons.print("level id," + entry.getKey() + "");
                if (entry.getValue() != null) {
                    if (count == 1) {
                        mLocationMasterList1 = entry.getValue();
                    } else if (count == 2) {
                        mLocationMasterList2 = entry.getValue();
                    } else if (count == 3) {
                        mLocationMasterList3 = entry.getValue();
                    }
                }
            }
        }

        bmodel.mRetailerHelper.loadContractData();
        channelMaster = bmodel.channelMasterHelper.getChannelMaster();

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        editCallback.OnDownloadTaskCompleted(channelMaster,mLocationMasterList1,mLocationMasterList2,mLocationMasterList3);
    }
}
