package com.ivy.sd.png.bo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class RetailerMasterBO implements Comparable<RetailerMasterBO> {

    public static final Comparator<RetailerMasterBO> WalkingSequenceComparator = new Comparator<RetailerMasterBO>() {

        public int compare(RetailerMasterBO mRetailerMasterBO1, RetailerMasterBO mRetailerMasterBO2) {

            int mWalkingSequence1 = mRetailerMasterBO1.getWalkingSequence();
            int mWalkingSequence2 = mRetailerMasterBO2.getWalkingSequence();

            if (mWalkingSequence1 == 0 || mWalkingSequence2 == 0) {
                return mRetailerMasterBO1.getRetailerName().compareToIgnoreCase(mRetailerMasterBO2.getRetailerName());
            }
            return mWalkingSequence1 - mWalkingSequence2;
        }

    };
    public static final Comparator<RetailerMasterBO> RetailerNameComparator = new Comparator<RetailerMasterBO>() {

        public int compare(RetailerMasterBO mRtrMasterBO1, RetailerMasterBO mRtrMasterBO2) {

            return mRtrMasterBO1.getRetailerName().compareTo(mRtrMasterBO2.getRetailerName());
        }

    };


    public static final Comparator<RetailerMasterBO> RetailerIsTodayComparator = new Comparator<RetailerMasterBO>() {

        public int compare(RetailerMasterBO mRtrMasterBO1, RetailerMasterBO mRtrMasterBO2) {

            return mRtrMasterBO2.getIsToday() - mRtrMasterBO1.getIsToday();
        }

    };
    public String visitday;
    public int accountid;
    int distributorId;
    int distParentId;
    private String retailerID = "0", movRetailerId = "0";
    private String RetailerCode = "";
    private String retailerName, movRetailerName;
    private String Addressid = "0";
    private String Cp1id = "0";
    private String Cp2id = "0";
    private String Address1;
    private String address2;
    private String address3;

    // Sales per category
    private String salesInvoiceId, salesInvoiceValue, salesLpc, salesQty, salesProductSName;

    public String getMovRetailerId() {
        return movRetailerId;
    }

    public void setMovRetailerId(String movRetailerId) {
        this.movRetailerId = movRetailerId;
    }

    public String getMovRetailerName() {
        return movRetailerName;
    }

    public void setMovRetailerName(String movRetailerName) {
        this.movRetailerName = movRetailerName;
    }

    private String city;
    private String state;
    private String pincode;
    private double latitude;
    private double longitude;
    private String contactname;
    private String contactLname;
    private String email;
    private String contactnumber;
    private String contactnumber1;
    private String contactnumber2;
    private String contactname2;
    private String contactLname2;
    private String tinnumber;
    private String tinExpDate;
    private String contact1_title;
    private String contact1_titlelovid;
    private String contact2_title;
    private String contact2_titlelovid;
    private int BeatID;
    private int channelID;
    private int subchannelid;
    private int classid, categoryid;
    private double creditLimit;
    private double monthly_target;
    private double monthly_acheived;
    private double daily_target;
    private double bottle_creditLimit;
    private int visit_frequencey;
    private String credit_invoice_count;
    private SupplierMasterBO supplierBO;
    private int isToday;
    private int visitDoneCount;
    private String weekNo;
    private String rpTypeCode;
    private String mslAch = "0";
    private String mslTaget = "0";
    private String salesValue = "0";
    private double credit_balance;// RField1
    private int creditDays = -1;
    private String rfield2;
    private int surveyHistoryScore;
    private String locName;
    private String orderTypeId;
    private double daily_target_planned;
    private double daily_target_planned_temp;
    private int walkingSequence;
    private float spTarget;
    private int sbd_dist_achieve;
    private int sbd_dist_target;
    private String isDeadStore;
    private String isPlanned;
    private float osAmt;
    private int billsCount;
    private String isOrderMerch;
    private String isDeviated = "";
    private String isNew;
    private String isDigitalContent;
    private String isReviewPlan;
    private String isSKUTGT = "";
    private String sbdMercPercent;
    private String sbdMerchInitPrecent;
    private String initiativePercent;
    private String isOrdered;
    private String isProductive;
    private String isInvoiceDone;
    private double distance;
    private double Visit_Actual;
    private String IsVisited;
    private int sbdMerchTarget;
    private int sbdMerchAcheived;
    private int sbdMerchInitTarget;
    private int sbdMerchInitAcheived;
    private String LastVisitStatus;
    private int isGoldStore;
    private String isMerchandisingDone;
    private String isInitMerchandisingDone;
    private int groupId;
    private int sbdDistStock;
    private int initiative_target;
    private int initiative_achieved;
    private String otpActivatedDate;
    private String skipActivatedDate;
    private boolean isSkip;
    private double coverage;
    private int totalLines;
    private String isCollectionView;
    private int gpsDistance;
    private int isVansales;
    private String indicativeOrderid;
    private int indicateFlag;
    private boolean isHangingOrder;
    private int sbdDistAchieved;
    private int taxTypeId;
    private int locationId;
    private int PlannedVisitCount;
    private int contractLovid;
    private int SelectedUserID;
    private String RField4;
    private boolean isSurveyDone;
    private HashSet<DateWisePlanBO> plannedDates;
    private double Ap3m;
    private int hasPaymentIssue;
    private boolean addedForDownload;
    private boolean checked;
    private String mNFCTagId;
    private boolean hasNoVisitReason;
    private String dob;
    private boolean isNearBy;
    private String RField1, profile_creditLimit;
    private boolean isOrderWithoutInvoice;
    private int salesTypeId = 0;
    private String profileImagePath;
    private int subDId;
    private String GSTNumber = "-";

    private String DLNo;
    private String DLNoExpDate;
    private String panNumber;
    private String foodLicenceNo;
    private String foodLicenceExpDate;
    private String mobile;
    private String fax;
    private String region;
    private String country;
    private double sbdPercent;
    private String interval;
    private int kpiid_month;
    private int kpiid_day;
    private int kpi_param_day;
    private int kpi_param_month;
    private int retailerTaxLocId;
    private int supplierTaxLocId;
    private String ridSF;
    private String district;
    private String lastVisitDate;
    private String lastVisitedBy;
    private int totalPlanned;
    private int totalVisited;
    private int visitTargetCount;
    private String webUrl;

    public int getIsSEZzone() {
        return isSEZzone;
    }

    public void setIsSEZzone(int isSEZzone) {
        this.isSEZzone = isSEZzone;
    }

    private int isSEZzone;

    public String getGSTNumber() {
        return GSTNumber;
    }

    public void setGSTNumber(String GSTNumber) {
        this.GSTNumber = GSTNumber;
    }


    public double getCurrentFitScore() {
        return currentFitScore;
    }

    public void setCurrentFitScore(double currentFitScore) {
        this.currentFitScore = currentFitScore;
    }

    private double currentFitScore;


    public int getPrioriryProductId() {
        return prioriryProductId;
    }

    public void setPrioriryProductId(int prioriryProductId) {
        this.prioriryProductId = prioriryProductId;
    }

    private int prioriryProductId;

    private ArrayList<NewOutletAttributeBO> attributeBOArrayList;

    public ArrayList<NewOutletAttributeBO> getAttributeBOArrayList() {
        return attributeBOArrayList;
    }

    public void setAttributeBOArrayList(ArrayList<NewOutletAttributeBO> attributeBOArrayList) {
        this.attributeBOArrayList = attributeBOArrayList;
    }

    public double getBillWiseCompanyDiscount() {
        return billWiseCompanyDiscount;
    }

    public void setBillWiseCompanyDiscount(double billWiseCompanyDiscount) {
        this.billWiseCompanyDiscount = billWiseCompanyDiscount;
    }

    public double getBillWiseDistributorDiscount() {
        return billWiseDistributorDiscount;
    }

    public void setBillWiseDistributorDiscount(double billWiseDistributorDiscount) {
        this.billWiseDistributorDiscount = billWiseDistributorDiscount;
    }

    private double billWiseCompanyDiscount;
    private double billWiseDistributorDiscount;

    private int attributeId;
    private String attributeName;

    public String getRField5() {
        return RField5;
    }

    public void setRField5(String RField5) {
        this.RField5 = RField5;
    }

    public String getRField6() {
        return RField6;
    }

    public void setRField6(String RField6) {
        this.RField6 = RField6;
    }

    public String getRField7() {
        return RField7;
    }

    public void setRField7(String RField7) {
        this.RField7 = RField7;
    }

    private String RField5;
    private String RField6;
    private String RField7;
    private String RField8;
    private String RField9;

    private String RField10;
    private String RField11;
    private String RField12;
    private String RField13;
    private String RField14;
    private String RField15;
    private String RField16;
    private String RField17;
    private String RField18;
    private String RField19;
    private String RField20;

    private boolean isAdhoc;

    public RetailerMasterBO() {

    }

    public RetailerMasterBO(String retailerid, String tretailername) {
        this.retailerID = retailerid;
        this.retailerName = tretailername;
    }

    public double getCredit_balance() {
        return credit_balance;
    }

    public void setCredit_balance(double credit_balance) {
        this.credit_balance = credit_balance;
    }

    public String getOrderTypeId() {
        return orderTypeId;
    }

    public void setOrderTypeId(String orderTypeId) {
        this.orderTypeId = orderTypeId;
    }

    public String getNFCTagId() {
        return mNFCTagId;
    }

    public void setNFCTagId(String mNFCTagId) {
        this.mNFCTagId = mNFCTagId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isAddedForDownload() {
        return addedForDownload;
    }

    public void setAddedForDownload(boolean addedForDownload) {
        this.addedForDownload = addedForDownload;
    }

    public int getHasPaymentIssue() {
        return hasPaymentIssue;
    }

    public void setHasPaymentIssue(int hasPaymentIssue) {
        this.hasPaymentIssue = hasPaymentIssue;
    }


    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public int getPlannedVisitCount() {
        return PlannedVisitCount;
    }

    public void setPlannedVisitCount(int plannedVisitCount) {
        PlannedVisitCount = plannedVisitCount;
    }

    public String getRField4() {
        return RField4;
    }

    public void setRField4(String rField4) {
        RField4 = rField4;
    }


    @Override
    public String toString() {
        return retailerName;
    }

    public int getSbdDistAchieved() {
        return sbdDistAchieved;
    }

    public void setSbdDistAchieved(int sbdDistAchieved) {
        this.sbdDistAchieved = sbdDistAchieved;
    }

    public double getAp3m() {
        return Ap3m;
    }

    public void setAp3m(double ap3m) {
        Ap3m = ap3m;
    }

    public int getGpsDistance() {
        return gpsDistance;
    }

    public void setGpsDistance(int gpsDistance) {
        this.gpsDistance = gpsDistance;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean isSkip) {
        this.isSkip = isSkip;
    }

    public int getInitiative_target() {
        return initiative_target;
    }

    public void setInitiative_target(int initiative_target) {
        this.initiative_target = initiative_target;
    }

    public int getInitiative_achieved() {
        return initiative_achieved;
    }

    public void setInitiative_achieved(int initiative_achieved) {
        this.initiative_achieved = initiative_achieved;
    }

    public int getSbdDistStock() {
        return sbdDistStock;
    }

    public void setSbdDistStock(int sbdDistStock) {
        this.sbdDistStock = sbdDistStock;
    }

    public int getIsGoldStore() {
        return isGoldStore;
    }

    public void setIsGoldStore(int isGoldStore) {
        this.isGoldStore = isGoldStore;
    }

    public String getIsInitMerchandisingDone() {
        return isInitMerchandisingDone;
    }

    public void setIsInitMerchandisingDone(String isInitMerchandisingDone) {
        this.isInitMerchandisingDone = isInitMerchandisingDone;
    }

    public String getIsMerchandisingDone() {
        return isMerchandisingDone;
    }

    public void setIsMerchandisingDone(String isMerchandisingDone) {
        this.isMerchandisingDone = isMerchandisingDone;
    }

    public String getLastVisitStatus() {
        return LastVisitStatus;
    }

    public void setLastVisitStatus(String lastVisitStatus) {
        LastVisitStatus = lastVisitStatus;
    }

    public int getSBDMerchAchieved() {
        return sbdMerchAcheived;
    }

    public void setSBDMerchAchieved(int sbdMAcheived) {
        sbdMerchAcheived = sbdMAcheived;
    }

    public int getSBDMerchTarget() {
        return sbdMerchTarget;
    }

    public void setSBDMerchTarget(int rPS_Merch_Target) {
        sbdMerchTarget = rPS_Merch_Target;
    }

    public String getIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(String isOrdered) {
        this.isOrdered = isOrdered;
    }

    public String getIsInvoiceDone() {
        return isInvoiceDone;
    }

    public void setIsInvoiceDone(String isInvoiceDone) {
        this.isInvoiceDone = isInvoiceDone;
    }

    public String getIsVisited() {
        return IsVisited;
    }

    public void setIsVisited(String isVisited) {
        IsVisited = isVisited;
    }

    public double getVisit_Actual() {
        return Visit_Actual;
    }

    public void setVisit_Actual(double visit_Actual) {
        Visit_Actual = visit_Actual;
    }

    public String getRetailerCode() {
        return RetailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        RetailerCode = retailerCode;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(String retailerID) {
        this.retailerID = retailerID;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getAddress1() {
        return Address1;
    }

    public void setAddress1(String address) {
        Address1 = address;
    }

    public int getBeatID() {
        return BeatID;
    }

    public void setBeatID(int beatID) {
        BeatID = beatID;
    }

    public float getOsAmt() {
        return osAmt;
    }

    public void setOsAmt(float osAmt) {
        this.osAmt = osAmt;
    }

    public double getMonthly_target() {
        return monthly_target;
    }

    public void setMonthly_target(double monthly_target) {
        this.monthly_target = monthly_target;
    }

    public double getMonthly_acheived() {
        return monthly_acheived;
    }

    public void setMonthly_acheived(double monthly_acheived) {
        this.monthly_acheived = monthly_acheived;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getBillsCount() {
        return billsCount;
    }

    public void setBillsCount(int billsCount) {
        this.billsCount = billsCount;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactnumber() {
        return contactnumber;
    }

    public void setContactnumber(String contactnumber) {
        this.contactnumber = contactnumber;
    }

    public String getTinnumber() {
        return tinnumber;
    }

    public void setTinnumber(String tinnumber) {
        this.tinnumber = tinnumber;
    }

    public String getTinExpDate() {
        return tinExpDate;
    }

    public void setTinExpDate(String tinExpDate) {
        this.tinExpDate = tinExpDate;
    }

    public double getDaily_target() {
        return daily_target;
    }

    public void setDaily_target(double daily_target) {
        this.daily_target = daily_target;
    }

    public int getClassid() {
        return classid;
    }

    public void setClassid(int classid) {
        this.classid = classid;
    }

    public int getSubchannelid() {
        return subchannelid;
    }

    public void setSubchannelid(int subchannelid) {
        this.subchannelid = subchannelid;
    }

    public double getDaily_target_planned() {
        return daily_target_planned;
    }

    public void setDaily_target_planned(double daily_target_planned) {
        this.daily_target_planned = daily_target_planned;
    }

    public String getIsDeviated() {
        return isDeviated;
    }

    public void setIsDeviated(String isDeviated) {
        this.isDeviated = isDeviated;
    }

    public String getSbdMercPercent() {
        return sbdMercPercent;
    }

    public void setSbdMercPercent(String sbdMercPercent) {
        this.sbdMercPercent = sbdMercPercent;
    }

    @Override
    public int compareTo(RetailerMasterBO another) {
        double s = this.latitude - another.latitude;
        return (int) s;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public String getInitiativePercent() {
        return initiativePercent;
    }

    public void setInitiativePercent(String initiativePercent) {
        this.initiativePercent = initiativePercent;
    }

    public String isOrdered() {
        return isOrdered;
    }

    public void setOrdered(String isOrdered) {
        this.isOrdered = isOrdered;
    }

    public String isProductive() {
        return isProductive;
    }

    public void setProductive(String productive) {
        isProductive = productive;
    }

    public String isInvoiceDone() {
        return isInvoiceDone;
    }

    public void setInvoiceDone(String isInvoiceDone) {
        this.isInvoiceDone = isInvoiceDone;
    }

    public String getIsDigitalContent() {
        return isDigitalContent;
    }

    public void setIsDigitalContent(String isDigitalContent) {
        this.isDigitalContent = isDigitalContent;
    }

    public int getSbdDistributionAchieve() {
        return sbd_dist_achieve;
    }

    public void setSbd_dist_achieve(int sbd_dist_achieve) {
        this.sbd_dist_achieve = sbd_dist_achieve;
    }

    public int getSbdDistributionTarget() {
        return sbd_dist_target;
    }

    public void setSbd_dist_target(int sbd_dist_target) {
        this.sbd_dist_target = sbd_dist_target;
    }

    public int getIsToday() {
        return isToday;
    }

    public void setIsToday(int isToday) {
        this.isToday = isToday;
    }

    public int getVisitDoneCount() {
        return visitDoneCount;
    }

    public void setVisitDoneCount(int visitDoneCount) {
        this.visitDoneCount = visitDoneCount;
    }

    public String getWeekNo() {
        return weekNo;
    }

    public void setWeekNo(String weekNo) {
        this.weekNo = weekNo;
    }

    public double getDaily_target_planned_temp() {
        return daily_target_planned_temp;
    }

    public void setDaily_target_planned_temp(double daily_target_planned_temp) {
        this.daily_target_planned_temp = daily_target_planned_temp;
    }

    public String getIsReviewPlan() {
        return isReviewPlan;
    }

    public void setIsReviewPlan(String isReviewPlan) {
        this.isReviewPlan = isReviewPlan;
    }

    public String getIsSKUTGT() {
        return isSKUTGT;
    }

    public void setIsSKUTGT(String isSKUTGT) {
        this.isSKUTGT = isSKUTGT;
    }

    public String getIsDeadStore() {
        return isDeadStore;
    }

    public void setIsDeadStore(String isDeadStore) {
        this.isDeadStore = isDeadStore;
    }

    public String getRpTypeCode() {
        if (rpTypeCode == null)
            return "";
        return rpTypeCode;
    }

    public void setRpTypeCode(String rpTypeCode) {
        this.rpTypeCode = rpTypeCode;
    }

    public String getIsPlanned() {
        return isPlanned;
    }

    public void setIsPlanned(String isPlanned) {
        this.isPlanned = isPlanned;
    }

    public float getSpTarget() {
        return spTarget;
    }

    public void setSpTarget(float spTarget) {
        this.spTarget = spTarget;
    }

    public String getIsOrderMerch() {
        return isOrderMerch;
    }

    public void setIsOrderMerch(String isOrderMerch) {
        this.isOrderMerch = isOrderMerch;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getWalkingSequence() {
        return walkingSequence;
    }

    public void setWalkingSequence(int walkingSequence) {
        this.walkingSequence = walkingSequence;
    }

    public int getSbdMerchInitTarget() {
        return sbdMerchInitTarget;
    }

    public void setSbdMerchInitTarget(int sbdMerchInitTarget) {
        this.sbdMerchInitTarget = sbdMerchInitTarget;
    }

    public int getSbdMerchInitAcheived() {
        return sbdMerchInitAcheived;
    }

    public void setSbdMerchInitAcheived(int sbdMerchInitAcheived) {
        this.sbdMerchInitAcheived = sbdMerchInitAcheived;
    }

    public String getSbdMerchInitPrecent() {
        return sbdMerchInitPrecent;
    }

    public void setSbdMerchInitPrecent(String sbdMerchInitPrecent) {
        this.sbdMerchInitPrecent = sbdMerchInitPrecent;
    }

    public String getOtpActivatedDate() {
        return otpActivatedDate;
    }

    public void setOtpActivatedDate(String otpActivatedDate) {
        this.otpActivatedDate = otpActivatedDate;
    }

    public String getSkipActivatedDate() {
        return skipActivatedDate;
    }

    public void setSkipActivatedDate(String skipActivatedDate) {
        this.skipActivatedDate = skipActivatedDate;
    }

    public int getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(int creditDays) {
        this.creditDays = creditDays;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    public String getIsCollectionView() {
        return isCollectionView;
    }

    public void setIsCollectionView(String isCollectionView) {
        this.isCollectionView = isCollectionView;
    }

    public int getIsVansales() {
        return isVansales;
    }

    public void setIsVansales(int isVansales) {
        this.isVansales = isVansales;
    }

    public String getIndicativeOrderid() {
        return indicativeOrderid;
    }

    public void setIndicativeOrderid(String indicativeOrderid) {
        this.indicativeOrderid = indicativeOrderid;
    }

    public int getIndicateFlag() {
        return indicateFlag;
    }

    public void setIndicateFlag(int indicateFlag) {
        this.indicateFlag = indicateFlag;
    }

    public SupplierMasterBO getSupplierBO() {
        if (supplierBO == null)
            supplierBO = new SupplierMasterBO();
        return supplierBO;
    }

    public void setSupplierBO(SupplierMasterBO supplierBO) {
        this.supplierBO = supplierBO;
    }

    public String getContactnumber2() {
        return contactnumber2;
    }

    public void setContactnumber2(String contactnumber2) {
        this.contactnumber2 = contactnumber2;
    }

    public String getContactname2() {
        return contactname2;
    }

    public void setContactname2(String contactname2) {
        this.contactname2 = contactname2;
    }

    public double getBottle_creditLimit() {
        return bottle_creditLimit;
    }

    public void setBottle_creditLimit(double bottle_creditLimit) {
        this.bottle_creditLimit = bottle_creditLimit;
    }

    public String getCredit_invoice_count() {
        return credit_invoice_count;
    }

    public void setCredit_invoice_count(String credit_invoice_count) {
        this.credit_invoice_count = credit_invoice_count;
    }

    public double getVisit_frequencey() {
        return visit_frequencey;
    }

    public void setVisit_frequencey(int visit_frequencey) {
        this.visit_frequencey = visit_frequencey;
    }

    public boolean isHangingOrder() {
        return isHangingOrder;
    }

    public void setHangingOrder(boolean isHangingOrder) {
        this.isHangingOrder = isHangingOrder;
    }

    public int getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(int taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getSurveyHistoryScore() {
        return surveyHistoryScore;
    }

    public void setSurveyHistoryScore(int surveyHistoryScore) {
        this.surveyHistoryScore = surveyHistoryScore;
    }

    public int getSelectedUserID() {
        return SelectedUserID;
    }

    public void setSelectedUserID(int selectedUserID) {
        SelectedUserID = selectedUserID;
    }

    public HashSet<DateWisePlanBO> getPlannedDates() {
        return plannedDates;
    }

    public void setPlannedDates(HashSet<DateWisePlanBO> plannedDates) {
        this.plannedDates = plannedDates;
    }

    public boolean isSurveyDone() {
        return isSurveyDone;
    }

    public void setIsSurveyDone(boolean isSurveyDone) {
        this.isSurveyDone = isSurveyDone;
    }

    public String getVisitday() {
        return visitday;
    }

    public void setVisitday(String visitday) {
        this.visitday = visitday;
    }

    public String getContactnumber1() {
        return contactnumber1;
    }

    public void setContactnumber1(String contactnumber1) {
        this.contactnumber1 = contactnumber1;
    }

    public String getAddressid() {
        return Addressid;
    }

    public void setAddressid(String addressid) {
        Addressid = addressid;
    }

    public String getCp1id() {
        return Cp1id;
    }

    public void setCp1id(String cp1id) {
        Cp1id = cp1id;
    }

    public String getCp2id() {
        return Cp2id;
    }

    public void setCp2id(String cp2id) {
        Cp2id = cp2id;
    }

    public int getAccountid() {
        return accountid;
    }

    public void setAccountid(int accountid) {
        this.accountid = accountid;
    }

    public boolean isHasNoVisitReason() {
        return hasNoVisitReason;
    }

    public void setHasNoVisitReason(boolean hasNoVisitReason) {
        this.hasNoVisitReason = hasNoVisitReason;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public boolean isNearBy() {
        return isNearBy;
    }

    public void setIsNearBy(boolean isNearBy) {
        this.isNearBy = isNearBy;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContact1_title() {
        return contact1_title;
    }

    public void setContact1_title(String contact1_title) {
        this.contact1_title = contact1_title;
    }

    public String getContact1_titlelovid() {
        return contact1_titlelovid;
    }

    public void setContact1_titlelovid(String contact1_titlelovid) {
        this.contact1_titlelovid = contact1_titlelovid;
    }

    public String getContact2_title() {
        return contact2_title;
    }

    public void setContact2_title(String contact2_title) {
        this.contact2_title = contact2_title;
    }

    public String getContact2_titlelovid() {
        return contact2_titlelovid;
    }

    public void setContact2_titlelovid(String contact2_titlelovid) {
        this.contact2_titlelovid = contact2_titlelovid;
    }

    public int getContractLovid() {
        return contractLovid;
    }

    public void setContractLovid(int contractLovid) {
        this.contractLovid = contractLovid;
    }

    public int getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(int distributorId) {
        this.distributorId = distributorId;
    }

    public int getDistParentId() {
        return distParentId;
    }

    public void setDistParentId(int distParentId) {
        this.distParentId = distParentId;
    }

    public String getContactLname() {
        return contactLname;
    }

    public void setContactLname(String contactLname) {
        this.contactLname = contactLname;
    }

    public String getContactLname2() {
        return contactLname2;
    }

    public void setContactLname2(String contactLname2) {
        this.contactLname2 = contactLname2;
    }

    public String getMslAch() {
        return mslAch;
    }

    public void setMslAch(String mslAch) {
        this.mslAch = mslAch;
    }

    public String getMslTaget() {
        return mslTaget;
    }

    public void setMslTaget(String mslTaget) {
        this.mslTaget = mslTaget;
    }

    public String getSalesValue() {
        return salesValue;
    }

    public void setSalesValue(String salesValue) {
        this.salesValue = salesValue;
    }

    public boolean isOrderWithoutInvoice() {
        return isOrderWithoutInvoice;
    }

    public void setOrderWithoutInvoice(boolean orderWithoutInvoice) {
        isOrderWithoutInvoice = orderWithoutInvoice;
    }


    public String getRField1() {
        return RField1;
    }

    public void setRField1(String RField1) {
        this.RField1 = RField1;
    }

    public String getProfile_creditLimit() {
        return profile_creditLimit;
    }

    public void setProfile_creditLimit(String profile_creditLimit) {
        this.profile_creditLimit = profile_creditLimit;
    }

    public String getRfield2() {
        return rfield2;
    }

    public void setRfield2(String rfield2) {
        this.rfield2 = rfield2;
    }


    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public int getSalesTypeId() {
        return salesTypeId;
    }

    public void setSalesTypeId(int salesTypeId) {
        this.salesTypeId = salesTypeId;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public int isSameZone() {
        return isSameZone;
    }

    public void setSameZone(int sameZone) {
        isSameZone = sameZone;
    }

    private int isSameZone;

    public boolean isBomAchieved() {
        return isBomAchieved;
    }

    public void setBomAchieved(boolean bomAchieved) {
        isBomAchieved = bomAchieved;
    }

    private boolean isBomAchieved = false;

    public String getSalesInvoiceId() {
        return salesInvoiceId;
    }

    public void setSalesInvoiceId(String salesInvoiceId) {
        this.salesInvoiceId = salesInvoiceId;
    }

    public String getSalesInvoiceValue() {
        return salesInvoiceValue;
    }

    public void setSalesInvoiceValue(String salesInvoiceValue) {
        this.salesInvoiceValue = salesInvoiceValue;
    }

    public String getSalesLpc() {
        return salesLpc;
    }

    public void setSalesLpc(String salesLpc) {
        this.salesLpc = salesLpc;
    }

    public String getSalesQty() {
        return salesQty;
    }

    public void setSalesQty(String salesQty) {
        this.salesQty = salesQty;
    }

    public String getSalesProductSName() {
        return salesProductSName;
    }

    public void setSalesProductSName(String salesProductSName) {
        this.salesProductSName = salesProductSName;
    }

    public void setDLNo(String DLNo) {
        this.DLNo = DLNo;
    }

    public String getDLNo() {
        return DLNo;
    }

    public void setDLNoExpDate(String DLNoExpDate) {
        this.DLNoExpDate = DLNoExpDate;
    }

    public String getDLNoExpDate() {
        return DLNoExpDate;
    }

    public int getSubdId() {
        return subDId;
    }

    public void setSubdId(int subdId) {
        this.subDId = subdId;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getFoodLicenceNo() {
        return foodLicenceNo;
    }

    public void setFoodLicenceNo(String foodLicenceNo) {
        this.foodLicenceNo = foodLicenceNo;
    }

    public String getFoodLicenceExpDate() {
        return foodLicenceExpDate;
    }

    public void setFoodLicenceExpDate(String foodLicenceExpDate) {
        this.foodLicenceExpDate = foodLicenceExpDate;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getSbdPercent() {
        return sbdPercent;
    }

    public void setSbdPercent(double sbdPercent) {
        this.sbdPercent = sbdPercent;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getKpiid_day() {
        return kpiid_day;
    }

    public void setKpiid_day(int kpiid_day) {
        this.kpiid_day = kpiid_day;
    }

    public int getKpi_param_day() {
        return kpi_param_day;
    }

    public void setKpi_param_day(int kpi_param_day) {
        this.kpi_param_day = kpi_param_day;
    }

    public void setRetailerTaxLocId(int retailerTaxLocId) {
        this.retailerTaxLocId = retailerTaxLocId;
    }

    public int getRetailerTaxLocId() {
        return retailerTaxLocId;
    }

    public void setSupplierTaxLocId(int supplierTaxLocId) {
        this.supplierTaxLocId = supplierTaxLocId;
    }

    public int getSupplierTaxLocId() {
        return supplierTaxLocId;
    }

    public String getRidSF() {
        return ridSF;
    }

    public void setRidSF(String ridSF) {
        this.ridSF = ridSF;
    }

    public double getmOrderedTotWgt() {
        return mOrderedTotWgt;
    }

    public void setmOrderedTotWgt(double mOrderedTotWgt) {
        this.mOrderedTotWgt = mOrderedTotWgt;
    }

    private double mOrderedTotWgt;

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRField8() {
        return RField8;
    }

    public void setRField8(String RField8) {
        this.RField8 = RField8;
    }

    public String getRField9() {
        return RField9;
    }

    public void setRField9(String RField9) {
        this.RField9 = RField9;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public String getLastVisitedBy() {
        return lastVisitedBy;
    }

    public void setLastVisitedBy(String lastVisitedBy) {
        this.lastVisitedBy = lastVisitedBy;
    }

    public int getTotalPlanned() {
        return totalPlanned;
    }

    public void setTotalPlanned(int totalPlanned) {
        this.totalPlanned = totalPlanned;
    }

    public int getTotalVisited() {
        return totalVisited;
    }

    public void setTotalVisited(int totalVisited) {
        this.totalVisited = totalVisited;
    }

    public boolean isAdhoc() {
        return isAdhoc;
    }

    public void setAdhoc(boolean adhoc) {
        isAdhoc = adhoc;
    }

    public String getRField10() {
        return RField10;
    }

    public void setRField10(String RField10) {
        this.RField10 = RField10;
    }

    public String getRField11() {
        return RField11;
    }

    public void setRField11(String RField11) {
        this.RField11 = RField11;
    }

    public String getRField12() {
        return RField12;
    }

    public void setRField12(String RField12) {
        this.RField12 = RField12;
    }

    public String getRField13() {
        return RField13;
    }

    public void setRField13(String RField13) {
        this.RField13 = RField13;
    }

    public String getRField14() {
        return RField14;
    }

    public void setRField14(String RField14) {
        this.RField14 = RField14;
    }

    public String getRField15() {
        return RField15;
    }

    public void setRField15(String RField15) {
        this.RField15 = RField15;
    }

    public String getRField16() {
        return RField16;
    }

    public void setRField16(String RField16) {
        this.RField16 = RField16;
    }

    public String getRField17() {
        return RField17;
    }

    public void setRField17(String RField17) {
        this.RField17 = RField17;
    }

    public String getRField18() {
        return RField18;
    }

    public void setRField18(String RField18) {
        this.RField18 = RField18;
    }

    public String getRField19() {
        return RField19;
    }

    public void setRField19(String RField19) {
        this.RField19 = RField19;
    }

    public String getRField20() {
        return RField20;
    }

    public void setRField20(String RField20) {
        this.RField20 = RField20;
    }

    public int getVisitTargetCount() {
        return visitTargetCount;
    }

    public void setVisitTargetCount(int visitTargetCount) {
        this.visitTargetCount = visitTargetCount;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
