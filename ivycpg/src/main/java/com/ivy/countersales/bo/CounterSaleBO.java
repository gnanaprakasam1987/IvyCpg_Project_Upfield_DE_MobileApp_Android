package com.ivy.countersales.bo;

import com.ivy.sd.png.bo.ApplyBo;
import com.ivy.sd.png.bo.ProductMasterBO;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 18-03-2016.
 */
public class CounterSaleBO {


    String customerName = "";
    String address = "";
    String contactNumber = "";
    String resolution = "";

    int attributeId;

    int sequance;
    String freqVisit = "";
    ArrayList<ProductMasterBO> mSampleProducts;
    String connsultingFeedback = "", testFeedback = "";
    ArrayList<ProductMasterBO> mSalesproduct;

    int counterId;
    String result = "";
    String retailerId = "";
    int testedProductId;
    int testTime;

    int testhour;

    String email, gender;
    String uid;
    double disAmount;
    double disPercentage;

    public boolean isSaleDrafted() {
        return isSaleDrafted;
    }

    public void setSaleDrafted(boolean saleDrafted) {
        isSaleDrafted = saleDrafted;
    }

    boolean isSaleDrafted;
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    String ageGroup;


    public String getFreqVisit() {
        return freqVisit;
    }

    public void setFreqVisit(String freqVisit) {
        this.freqVisit = freqVisit;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public ArrayList<ProductMasterBO> getmSampleProducts() {
        return mSampleProducts;
    }

    public void setmSampleProducts(ArrayList<ProductMasterBO> mSampleProducts) {
        this.mSampleProducts = mSampleProducts;
    }


    public ArrayList<ProductMasterBO> getmSalesproduct() {
        return mSalesproduct;
    }

    public void setmSalesproduct(ArrayList<ProductMasterBO> mSalesproduct) {
        this.mSalesproduct = mSalesproduct;
    }


    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }


    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }


    public String getConnsultingFeedback() {
        return connsultingFeedback;
    }

    public void setConnsultingFeedback(String connsultingFeedback) {
        this.connsultingFeedback = connsultingFeedback;
    }

    public String getTestFeedback() {
        return testFeedback;
    }

    public void setTestFeedback(String testFeedback) {
        this.testFeedback = testFeedback;
    }

    public int getSequance() {
        return sequance;
    }

    public void setSequance(int sequance) {
        this.sequance = sequance;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public int getTestedProductId() {
        return testedProductId;
    }

    public void setTestedProductId(int testedProductId) {
        this.testedProductId = testedProductId;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    boolean isDraft;

    public String getLastUid() {
        return lastUid;
    }

    public void setLastUid(String lastUid) {
        this.lastUid = lastUid;
    }

    String lastUid = "";


    public int getTesthour() {
        return testhour;
    }

    public void setTesthour(int testhour) {
        this.testhour = testhour;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public double getDisAmount() {
        return disAmount;
    }

    public void setDisAmount(double disAmount) {
        this.disAmount = disAmount;
    }

    public double getDisPercentage() {
        return disPercentage;
    }

    public void setDisPercentage(double disPercentage) {
        this.disPercentage = disPercentage;
    }

    ArrayList<ApplyBo> mTestProducts;
    public ArrayList<ApplyBo> getmTestProducts() {
        if (mTestProducts != null)
            return mTestProducts;
        else
            return new ArrayList<ApplyBo>();
    }

    public void setmTestProducts(ArrayList<ApplyBo> mTestProducts) {
        this.mTestProducts = mTestProducts;
    }

}
