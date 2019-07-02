package com.ivy.cpg.view.reports.performancereport;

/**
 * Created by mansoor.k on 11/01/2017.
 */

public class SellerPerformanceBO {

    private int userId;
    private int plannedCall, DeviatedCall, actualCall, productiveCall;
    private String userName;
    private String timeSpent, objective, actual, visitPer, productivePer, lastSync, salesVolume;
    private String fitScore;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getVisitPer() {
        return visitPer;
    }

    public void setVisitPer(String visitPer) {
        this.visitPer = visitPer;
    }

    public String getProductivePer() {
        return productivePer;
    }

    public void setProductivePer(String productivePer) {
        this.productivePer = productivePer;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public int getPlannedCall() {
        return plannedCall;
    }

    public void setPlannedCall(int plannedCall) {
        this.plannedCall = plannedCall;
    }

    public int getDeviatedCall() {
        return DeviatedCall;
    }

    public void setDeviatedCall(int deviatedCall) {
        DeviatedCall = deviatedCall;
    }

    public int getActualCall() {
        return actualCall;
    }

    public void setActualCall(int actualCall) {
        this.actualCall = actualCall;
    }

    public int getProductiveCall() {
        return productiveCall;
    }

    public void setProductiveCall(int productiveCall) {
        this.productiveCall = productiveCall;
    }

    public String getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(String salesVolume) {
        this.salesVolume = salesVolume;
    }

    public String getFitScore() {
        return fitScore;
    }

    public void setFitScore(String fitScore) {
        this.fitScore = fitScore;
    }
}
