package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class PlanningOutletBO {

    private String pname;
    private String targetm1;

    public String getTargetm1() {
        return targetm1;
    }

    public void setTargetm1(String targetm1) {
        this.targetm1 = targetm1;
    }



    public String getAchievedm1() {
        return achievedm1;
    }

    public void setAchievedm1(String achievedm1) {
        this.achievedm1 = achievedm1;
    }





    private String achievedm1;

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    private int pid;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    private ArrayList<PlanningOutletBO> planlist;

    public ArrayList<PlanningOutletBO> getPlanlist() {
        return planlist;
    }

    public void setPlanlist(ArrayList<PlanningOutletBO> planlist) {
        this.planlist = planlist;
    }

    public String getKeyBattles() {
        return KeyBattles;
    }

    public void setKeyBattles(String keyBattles) {
        KeyBattles = keyBattles;
    }

    private String KeyBattles;

}
