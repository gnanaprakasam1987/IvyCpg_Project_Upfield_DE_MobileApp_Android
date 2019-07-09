package com.ivy.sd.png.bo;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class NewOutletBO {
    private int mSelectedImageIndex = 0;
    private double NewOutletlattitude, NewOutletLongitude;
    private String visitDays="";
    private String weekNo="";
    private String mName;
    private String valueText;
    private String srotText;
    private boolean checkedList;
    private int listId;
    private String listName;

    public ArrayList<String> ImageName = new ArrayList<>();
    public ArrayList<Integer> ImageId = new ArrayList<>();

    private String Market="";
    private int channel, subChannel;
    private String outletName="";
    private String Address="";
    private String Address2="";

    private String Address3 = "";
    private String City="", State="";
    private String Phone="", email="";
    private String distid = "0";
    private String contactpersonname="", contactpersonname2="", Phone2="";
    private String contactpersonnameLastName="";
    private String contactpersonname2LastName="";
    private String Contact1title="0", contact1titlelovid="0";
    private String Contact2title="0", contact2titlelovid="0";
    private int contractStatuslovid=0;
    private int routeid, locid, loc1id, loc2id;
    private String Fax="";
    private String CreditLimit="0";
    private String Payment="0";
    private String retailerId;
    private String creditDays;
    private String rfield5="0";
    private String rfield6="0";
    private String rField4="0";
    private String rField7="0";
    private String tinExpDate="";
    private int userId;
    private String panNo="";
    private String drugLicenseNo="";
    private String foodLicenseNo="";
    private String dlExpDate="";
    private String flExpDate="";
    private String region="";
    private String country="";
    private String mobile="";
    private String district="";

    public java.lang.String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(java.lang.String retailerId) {
        this.retailerId = retailerId;
    }

    private ArrayList<StandardListBO> priorityProductList;



    private ArrayList<String> editAttributeList;

    public ArrayList<String> getEditAttributeList() {
        return editAttributeList;
    }

    public void setEditAttributeList(ArrayList<String> editAttributeList) {
        this.editAttributeList = editAttributeList;
    }

    private ArrayList<NewOutletAttributeBO> attributeList;

    public ArrayList<NewOutletAttributeBO> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(ArrayList<NewOutletAttributeBO> attributeList) {
        this.attributeList = attributeList;
    }

    public ArrayList<StandardListBO> getPriorityProductList() {
        return priorityProductList;
    }

    public void setPriorityProductList(ArrayList<StandardListBO> priorityProductList) {
        this.priorityProductList = priorityProductList;
    }

    public java.lang.String getPriorityProductId() {
        return priorityProductId;
    }

    public void setPriorityProductId(java.lang.String priorityProductId) {
        this.priorityProductId = priorityProductId;
    }

    private String priorityProductId, priorityProductLevelId;

    public java.lang.String getPriorityProductLevelId() {
        return priorityProductLevelId;
    }

    public void setPriorityProductLevelId(java.lang.String priorityProductLevelId) {
        this.priorityProductLevelId = priorityProductLevelId;
    }

    public java.lang.String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    private String taxTypeId = "0";

    public java.lang.String getClassTypeId() {
        return classTypeId;
    }

    public void setClassTypeId(java.lang.String classTypeId) {
        this.classTypeId = classTypeId;
    }

    private String classTypeId = "0";


    int String = 0;

    public String getCreditLimit() {
        return CreditLimit;
    }

    public void setCreditLimit(String creditLimit) {
        CreditLimit = creditLimit;
    }

    public String getPayment() {
        return Payment;
    }

    public void setPayment(String payment) {
        Payment = payment;
    }

    public int getmSelectedImageIndex() {
        return mSelectedImageIndex;
    }

    public void setmSelectedImageIndex(int mSelectedImageIndex) {
        this.mSelectedImageIndex = mSelectedImageIndex;
    }

    public NewOutletBO() {

    }

    public NewOutletBO(int locid,  String listName) {
        super();
        this.listName = listName;
        this.listId = locid;

    }

    public java.lang.String getSrotText() {
        return srotText;
    }

    public void setSrotText(java.lang.String srotText) {
        this.srotText = srotText;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }


    public String getContactpersonnameLastName() {
        return contactpersonnameLastName;
    }

    public void setContactpersonnameLastName(String contactpersonnameLastName) {
        this.contactpersonnameLastName = contactpersonnameLastName;
    }


    public String getContactpersonname2LastName() {
        return contactpersonname2LastName;
    }

    public void setContactpersonname2LastName(String contactpersonname2LastName) {
        this.contactpersonname2LastName = contactpersonname2LastName;
    }


    public String getFax() {
        return Fax;
    }

    public void setFax(String fax) {
        Fax = fax;
    }

    public int getRouteid() {
        return routeid;
    }

    public void setRouteid(int routeid) {
        this.routeid = routeid;
    }

    public int getLocid() {
        return locid;
    }

    public void setLocid(int locid) {
        this.locid = locid;
    }

    public String getContactpersonname2() {
        return contactpersonname2;
    }

    public void setContactpersonname2(String contactpersonname2) {
        this.contactpersonname2 = contactpersonname2;
    }

    public String getPhone2() {
        return Phone2;
    }

    public void setPhone2(String phone2) {
        Phone2 = phone2;
    }

    public void setCheckedList(boolean checkedList) {
        this.checkedList = checkedList;
    }

    public boolean isCheckedList() {
        return checkedList;
    }

    public double getNewOutletlattitude() {
        return NewOutletlattitude;
    }

    public void setNewOutletlattitude(double newOutletlattitude) {
        this.NewOutletlattitude = newOutletlattitude;
    }

    public double getNewOutletLongitude() {
        return NewOutletLongitude;
    }

    public void setNewOutletLongitude(double newOutletLongitude) {
        NewOutletLongitude = newOutletLongitude;
    }


    public ArrayList<String> getImageName() {
        return ImageName;
    }

    public void setImageName(ArrayList<java.lang.String> imageName) {
        ImageName = imageName;
    }

    public ArrayList<Integer> getImageId() {
        return ImageId;
    }

    public void setImageId(ArrayList<Integer> imageId) {
        ImageId = imageId;
    }


    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }


    public String getAddress2() {
        return Address2;
    }

    public void setAddress2(String address2) {
        Address2 = address2;
    }

    public String getAddress3() {
        return Address3;
    }

    public void setAddress3(String address3) {
        Address3 = address3;
    }

    public String getContactpersonname() {
        return contactpersonname;
    }

    public void setContactpersonname(String contactpersonname) {
        this.contactpersonname = contactpersonname;
    }

    public String getMarket() {
        return Market;
    }

    public void setMarket(String market) {
        Market = market;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getSubChannel() {
        return subChannel;
    }

    public void setSubChannel(int subChannel) {
        this.subChannel = subChannel;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDistid(String distid) {
        this.distid = distid;
    }

    public String getDistid() {
        return distid;
    }

    /**
     * @return the visitDays
     */
    public String getVisitDays() {
        return visitDays;
    }

    /**
     * @param visitDays the visitDays to set
     */
    public void setVisitDays(String visitDays) {
        this.visitDays = visitDays;
    }

    /**
     * @return the weekNo
     */
    public String getWeekNo() {
        return weekNo;
    }

    /**
     * @param weekNo the weekNo to set
     */
    public void setWeekNo(String weekNo) {
        this.weekNo = weekNo;
    }

    public int getLoc2id() {
        return loc2id;
    }

    public void setLoc2id(int loc2id) {
        this.loc2id = loc2id;
    }

    public int getLoc1id() {
        return loc1id;
    }

    public void setLoc1id(int loc1id) {
        this.loc1id = loc1id;
    }

    public String toString() {
        return listName;
    }

    public String getTinno() {
        return tinno;
    }

    public void setTinno(String tinno) {
        this.tinno = tinno;
    }

    private String tinno="0";

    public java.lang.String getGstNum() {
        return gstNum;
    }

    public void setGstNum(java.lang.String gstNum) {
        this.gstNum = gstNum;
    }

    private String gstNum="";

    public int getIsSEZ() {
        return isSEZ;
    }

    public void setIsSEZ(int isSEZ) {
        this.isSEZ = isSEZ;
    }

    private int isSEZ;

    public String getRfield3() {
        return rfield3;
    }

    public void setRfield3(String rfield3) {
        this.rfield3 = rfield3;
    }

    private String rfield3="";

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    private String pincode="";


    public java.lang.String getContact1title() {
        return Contact1title;
    }

    public void setContact1title(String contact1title) {
        Contact1title = contact1title;
    }

    public String getContact1titlelovid() {
        return contact1titlelovid;
    }

    public void setContact1titlelovid(String contact1titlelovid) {
        this.contact1titlelovid = contact1titlelovid;
    }

    public String getContact2title() {
        return Contact2title;
    }

    public void setContact2title(String contact2title) {
        Contact2title = contact2title;
    }

    public String getContact2titlelovid() {
        return contact2titlelovid;
    }

    public void setContact2titlelovid(String contact2titlelovid) {
        this.contact2titlelovid = contact2titlelovid;
    }

    public int getContractStatuslovid() {
        return contractStatuslovid;
    }

    public void setContractStatuslovid(int contractStatuslovid) {
        this.contractStatuslovid = contractStatuslovid;
    }


    public String getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(String paymentTerm) {
        this.creditDays = paymentTerm;
    }




    //for use of profile screen


    public java.lang.String getmName() {
        return mName;
    }

    public void setmName(java.lang.String mName) {
        this.mName = mName;
    }

    public java.lang.String getValueText() {
        return valueText;
    }

    public void setValueText(java.lang.String valueText) {
        this.valueText = valueText;
    }

    public HashMap<java.lang.String, AddressBO> getmAddressByTag() {
        return mAddressByTag;
    }

    public void setmAddressByTag(HashMap<java.lang.String, AddressBO> mAddressByTag) {
        this.mAddressByTag = mAddressByTag;
    }

    private HashMap<String,AddressBO> mAddressByTag;

    public String getRfield5() {
        return rfield5;
    }

    public String getRfield6() {
        return rfield6;
    }

    public void setRfield5(String rfield5) {
        this.rfield5 = rfield5;
    }

    public void setRfield6(String rfield6) {
        this.rfield6 = rfield6;
    }

    public void setTinExpDate(String tinDate) {
        this.tinExpDate = tinDate;
    }

    public String getTinExpDate() {
        return tinExpDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public java.lang.String getPanNo() {
        return panNo;
    }

    public void setPanNo(java.lang.String panNo) {
        this.panNo = panNo;
    }

    public java.lang.String getDrugLicenseNo() {
        return drugLicenseNo;
    }

    public void setDrugLicenseNo(java.lang.String drugLicenseNo) {
        this.drugLicenseNo = drugLicenseNo;
    }

    public java.lang.String getFoodLicenseNo() {
        return foodLicenseNo;
    }

    public void setFoodLicenseNo(java.lang.String foodLicenseNo) {
        this.foodLicenseNo = foodLicenseNo;
    }

    public java.lang.String getDlExpDate() {
        return dlExpDate;
    }

    public void setDlExpDate(java.lang.String dlExpDate) {
        this.dlExpDate = dlExpDate;
    }

    public java.lang.String getFlExpDate() {
        return flExpDate;
    }

    public void setFlExpDate(java.lang.String flExpDate) {
        this.flExpDate = flExpDate;
    }

    public java.lang.String getrField4() {
        return rField4;
    }

    public void setrField4(java.lang.String rField4) {
        this.rField4 = rField4;
    }

    public java.lang.String getrField7() {
        return rField7;
    }

    public void setrField7(java.lang.String rField7) {
        this.rField7 = rField7;
    }

    public java.lang.String getRegion() {
        return region;
    }

    public void setRegion(java.lang.String region) {
        this.region = region;
    }

    public java.lang.String getCountry() {
        return country;
    }

    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    public java.lang.String getMobile() {
        return mobile;
    }

    public void setMobile(java.lang.String mobile) {
        this.mobile = mobile;
    }

    public java.lang.String getDistrict() {
        return district;
    }

    public void setDistrict(java.lang.String district) {
        this.district = district;
    }
}
