package com.ivy.ui.profile.data;


import com.ivy.sd.png.bo.NewOutletAttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelWiseAttributeList {

    private HashMap<Integer, ArrayList<NewOutletAttributeBO>>  mAttributeBOListByLocationID;

    private HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID;

    public ChannelWiseAttributeList(HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID,
                                    HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID) {
        this.mAttributeBOListByLocationID = mAttributeBOListByLocationID;
        this.mAttributeListByLocationID = mAttributeListByLocationID;
    }

    public HashMap<Integer, ArrayList<NewOutletAttributeBO>> getmAttributeBOListByLocationID() {
        return mAttributeBOListByLocationID;
    }


    public HashMap<Integer, ArrayList<Integer>> getmAttributeListByLocationID() {
        return mAttributeListByLocationID;
    }




}
