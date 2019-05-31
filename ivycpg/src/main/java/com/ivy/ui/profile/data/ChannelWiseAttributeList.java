package com.ivy.ui.profile.data;


import com.ivy.sd.png.bo.NewOutletAttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelWiseAttributeList {

    private HashMap<Integer, ArrayList<NewOutletAttributeBO>>  mAttributeBOListByLocationID;

    private HashMap<Integer, ArrayList<Integer>> mAttributeIdListByLocationID;

    public ChannelWiseAttributeList(){}

    public ChannelWiseAttributeList(HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID,
                                    HashMap<Integer, ArrayList<Integer>> mAttributeListByLocationID) {
        this.mAttributeBOListByLocationID = mAttributeBOListByLocationID;
        this.mAttributeIdListByLocationID = mAttributeListByLocationID;
    }

    public HashMap<Integer, ArrayList<NewOutletAttributeBO>> getAttributeBOListByLocationID() {
        if(mAttributeBOListByLocationID!=null){
            return mAttributeBOListByLocationID;
        }else {
            return mAttributeBOListByLocationID=new HashMap<>();
        }

    }


    public HashMap<Integer, ArrayList<Integer>> getAttributeListByLocationID() {
        return mAttributeIdListByLocationID;
    }




}
