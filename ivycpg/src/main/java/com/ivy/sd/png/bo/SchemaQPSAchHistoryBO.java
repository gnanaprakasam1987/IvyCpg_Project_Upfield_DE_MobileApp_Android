package com.ivy.sd.png.bo;

/**
 * Created by anandasir on 9/8/18.
 */

public class SchemaQPSAchHistoryBO {

    private int SchemeID;
    private double Cumulative_Purchase, CurSlab_Sch_Amt, CurSlab_Rs_Per, NextSlab_balance, NextSlab_Sch_Amt, NextSlab_Rs_Per;

    public int getSchemeID() {
        return SchemeID;
    }

    public void setSchemeID(int schemeID) {
        SchemeID = schemeID;
    }

    public double getCumulative_Purchase() {
        return Cumulative_Purchase;
    }

    public void setCumulative_Purchase(double cumulative_Purchase) {
        Cumulative_Purchase = cumulative_Purchase;
    }

    public double getCurSlab_Sch_Amt() {
        return CurSlab_Sch_Amt;
    }

    public void setCurSlab_Sch_Amt(double curSlab_Sch_Amt) {
        CurSlab_Sch_Amt = curSlab_Sch_Amt;
    }

    public double getCurSlab_Rs_Per() {
        return CurSlab_Rs_Per;
    }

    public void setCurSlab_Rs_Per(double curSlab_Rs_Per) {
        CurSlab_Rs_Per = curSlab_Rs_Per;
    }

    public double getNextSlab_balance() {
        return NextSlab_balance;
    }

    public void setNextSlab_balance(double nextSlab_balance) {
        NextSlab_balance = nextSlab_balance;
    }

    public double getNextSlab_Sch_Amt() {
        return NextSlab_Sch_Amt;
    }

    public void setNextSlab_Sch_Amt(double nextSlab_Sch_Amt) {
        NextSlab_Sch_Amt = nextSlab_Sch_Amt;
    }

    public double getNextSlab_Rs_Per() {
        return NextSlab_Rs_Per;
    }

    public void setNextSlab_Rs_Per(double nextSlab_Rs_Per) {
        NextSlab_Rs_Per = nextSlab_Rs_Per;
    }
}
