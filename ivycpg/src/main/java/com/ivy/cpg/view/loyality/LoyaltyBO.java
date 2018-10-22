package com.ivy.cpg.view.loyality;

import java.util.ArrayList;

/**
 * Created by hanifa.m on 10/4/2016.
 */
public class LoyaltyBO {

    private int loyaltyId;
    private String loyaltyDescription;
    private int selectedPoints;
    private int givenPoints;
    private int balancePoints;
    private int retailerId;

    public int getPointTypeId() {
        return pointTypeId;
    }

    public void setPointTypeId(int pointTypeId) {
        this.pointTypeId = pointTypeId;
    }

    private int pointTypeId;


    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
    }

    int getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(int loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    String getLoyaltyDescription() {
        return loyaltyDescription;
    }

    public void setLoyaltyDescription(String loyaltyDescription) {
        this.loyaltyDescription = loyaltyDescription;
    }

    int getSelectedPoints() {
        return selectedPoints;
    }

    void setSelectedPoints(int selectedPoints) {
        this.selectedPoints = selectedPoints;
    }

    int getGivenPoints() {
        return givenPoints;
    }

    public void setGivenPoints(int givenPoints) {
        this.givenPoints = givenPoints;
    }

    int getBalancePoints() {
        return balancePoints;
    }

    void setBalancePoints(int balancePoints) {
        this.balancePoints = balancePoints;
    }

    //Loyalty Benefits objects

    private ArrayList<LoyaltyBenifitsBO> loyaltyTrackingList;

    ArrayList<LoyaltyBenifitsBO> getLoyaltyTrackingList() {
        return loyaltyTrackingList;
    }

    public void setLoyaltyTrackingList(ArrayList<LoyaltyBenifitsBO> loyaltyTrackingList) {
        this.loyaltyTrackingList = loyaltyTrackingList;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.loyaltyDescription;
    }




}
