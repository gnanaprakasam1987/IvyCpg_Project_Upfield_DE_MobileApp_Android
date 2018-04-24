package com.ivy.cpg.view.dashboard;

import java.util.ArrayList;
import java.util.HashMap;

public class DashBoardBO {

    private String text, score;
    private double target, initiative;
    private double acheived, percent;
    private String kpiTarget, kpiAcheived, kpiScore, kpiIncentive, kpiCode;
    private float convTargetPercentage;
    private float convAcheivedPercentage;
    private float calculatedPercentage;
    private String code = "";
    private String type = "";
    private int routeID, kpiID, kpiTypeLovID;
    private String beatDescription, monthName, monthID;
    private int index, isFlip, flex1, subDataCount;
    private int PId;
    private double ap3m;
    public boolean added;
    private String kpiFlex;


    private HashMap<String, ArrayList<String>> monthKpiList = new HashMap();

    public double getAp3m() {
        return ap3m;
    }

    public void setAp3m(double ap3m) {
        this.ap3m = ap3m;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double getAcheived() {
        return acheived;
    }

    public void setAcheived(double acheived) {
        this.acheived = acheived;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getBeatDescription() {
        return beatDescription;
    }

    public void setBeatDescription(String beatDescription) {
        this.beatDescription = beatDescription;
    }

    public float getConvTargetPercentage() {
        return convTargetPercentage;
    }

    public void setConvTargetPercentage(float convTargetPercentage) {
        this.convTargetPercentage = convTargetPercentage;
    }

    public float getConvAcheivedPercentage() {
        return convAcheivedPercentage;
    }

    public void setConvAcheivedPercentage(float convAcheivedPercentage) {
        this.convAcheivedPercentage = convAcheivedPercentage;
    }

    public double getIncentive() {
        return initiative;
    }

    public void setIncentive(double initiative) {
        this.initiative = initiative;
    }

    public int getIsFlip() {
        return isFlip;
    }

    public void setIsFlip(int isFlip) {
        this.isFlip = isFlip;
    }

    public float getCalculatedPercentage() {
        return calculatedPercentage;
    }

    public void setCalculatedPercentage(float calculatedPercentage) {
        this.calculatedPercentage = calculatedPercentage;
    }

    public int getPId() {
        return PId;
    }

    public void setPId(int pId) {
        PId = pId;
    }


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


    public int getKpiID() {
        return kpiID;
    }

    public void setKpiID(int kpiID) {
        this.kpiID = kpiID;
    }


    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public String getMonthID() {
        return monthID;
    }

    public void setMonthID(String monthID) {
        this.monthID = monthID;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public int getKpiTypeLovID() {
        return kpiTypeLovID;
    }

    public void setKpiTypeLovID(int kpiTypeLovID) {
        this.kpiTypeLovID = kpiTypeLovID;
    }

    public HashMap<String, ArrayList<String>> getMonthKpiList() {
        return monthKpiList;
    }

    public void setMonthKpiList(HashMap<String, ArrayList<String>> monthKpiList) {
        this.monthKpiList = monthKpiList;
    }

    public int getFlex1() {
        return flex1;
    }

    public void setFlex1(int flex1) {
        this.flex1 = flex1;
    }

    public int getSubDataCount() {
        return subDataCount;
    }

    public void setSubDataCount(int subDataCount) {
        this.subDataCount = subDataCount;
    }

    public String getKpiTarget() {
        return kpiTarget;
    }

    public void setKpiTarget(String kpiTarget) {
        this.kpiTarget = kpiTarget;
    }

    public String getKpiAcheived() {
        return kpiAcheived;
    }

    public void setKpiAcheived(String kpiAcheived) {
        this.kpiAcheived = kpiAcheived;
    }

    public String getKpiScore() {
        return kpiScore;
    }

    public void setKpiScore(String kpiScore) {
        this.kpiScore = kpiScore;
    }

    public String getKpiIncentive() {
        return kpiIncentive;
    }

    public void setKpiIncentive(String kpiIncentive) {
        this.kpiIncentive = kpiIncentive;
    }

    public String getKpiCode() {
        return kpiCode;
    }

    public void setKpiCode(String kpiCode) {
        this.kpiCode = kpiCode;
    }

    public String getP3m() {
        return p3m;
    }

    public void setP3m(String p3m) {
        this.p3m = p3m;
    }

    public String getYai() {
        return yai;
    }

    public void setYai(String yai) {
        this.yai = yai;
    }

    String p3m, yai;

    public String getKpiFlex() {
        return kpiFlex;
    }

    public void setKpiFlex(String kpiFlex) {
        this.kpiFlex = kpiFlex;
    }
}
