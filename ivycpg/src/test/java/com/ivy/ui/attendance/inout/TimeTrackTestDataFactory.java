package com.ivy.ui.attendance.inout;

import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.bo.ReasonMaster;

import java.util.ArrayList;

public abstract class TimeTrackTestDataFactory {

    public static ArrayList<NonFieldTwoBo> getTimeTrackList() {
        ArrayList<NonFieldTwoBo> timeTrackList = new ArrayList<>();
        NonFieldTwoBo nonFieldTwoBo = new NonFieldTwoBo();
        nonFieldTwoBo.setId("1");
        nonFieldTwoBo.setReasonText("abc");
        nonFieldTwoBo.setReason("1");
        nonFieldTwoBo.setFromDate("11/11/2018");
        nonFieldTwoBo.setInTime("11:11");
        nonFieldTwoBo.setOutTime("11:11");
        nonFieldTwoBo.setStatus("3");
        nonFieldTwoBo.setRemarks("5");

        timeTrackList.add(nonFieldTwoBo);

        return timeTrackList;
    }

    public static ArrayList<NonFieldTwoBo> getTimeTrackListInTime() {
        ArrayList<NonFieldTwoBo> timeTrackList = new ArrayList<>();
        NonFieldTwoBo nonFieldTwoBo = new NonFieldTwoBo();
        nonFieldTwoBo.setId("1");
        nonFieldTwoBo.setReasonText("abc");
        nonFieldTwoBo.setReason("1");
        nonFieldTwoBo.setFromDate("11/11/2018");
        nonFieldTwoBo.setInTime("11:11");
        nonFieldTwoBo.setOutTime("");
        nonFieldTwoBo.setStatus("3");
        nonFieldTwoBo.setRemarks("5");

        timeTrackList.add(nonFieldTwoBo);

        return timeTrackList;
    }

    public static ArrayList<ReasonMaster> getReasonList() {
        ArrayList<ReasonMaster> reasonMasterArrayList = new ArrayList<>();
        reasonMasterArrayList.add(new ReasonMaster("1", "reason1"));
        reasonMasterArrayList.add(new ReasonMaster("2", "reason1"));
        reasonMasterArrayList.add(new ReasonMaster("3", "reason1"));
        return reasonMasterArrayList;
    }


}
