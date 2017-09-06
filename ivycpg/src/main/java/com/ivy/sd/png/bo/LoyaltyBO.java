package com.ivy.sd.png.bo;

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
    private String uID;




    public LoyaltyBO(){

    }

    public LoyaltyBO(LoyaltyBO lotyObj) {

        this.loyaltyId = lotyObj.loyaltyId;
        this.loyaltyDescription = lotyObj.loyaltyDescription;
        this.selectedPoints = lotyObj.selectedPoints;
        this.givenPoints = lotyObj.givenPoints;
        this.balancePoints = lotyObj.balancePoints;
        this.uID = lotyObj.uID;
    }

    public int getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(int retailerId) {
        this.retailerId = retailerId;
    }

    public int getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(int loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public String getLoyaltyDescription() {
        return loyaltyDescription;
    }

    public void setLoyaltyDescription(String loyaltyDescription) {
        this.loyaltyDescription = loyaltyDescription;
    }

    public int getSelectedPoints() {
        return selectedPoints;
    }

    public void setSelectedPoints(int selectedPoints) {
        this.selectedPoints = selectedPoints;
    }

    public int getGivenPoints() {
        return givenPoints;
    }

    public void setGivenPoints(int givenPoints) {
        this.givenPoints = givenPoints;
    }

    public int getBalancePoints() {
        return balancePoints;
    }

    public void setBalancePoints(int balancePoints) {
        this.balancePoints = balancePoints;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    //Loyalty Benefits objects

    private ArrayList<LoyaltyBenifitsBO> loyaltyTrackingList;

    public ArrayList<LoyaltyBenifitsBO> getLoyaltyTrackingList() {
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
