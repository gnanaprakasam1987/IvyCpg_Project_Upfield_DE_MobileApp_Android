package com.ivy.sd.png.bo;

import java.util.ArrayList;
import java.util.Vector;

public class NewOutletBO {
    public int mSelectedImageIndex = 0;
    double NewOutletlattitude, NewOutletLongitude;
    private String visitDays;
    private String weekNo;
    private String mName;
    private String valueText;
    private String srotText;
    private boolean checkedList;
    private int listId;
    private String listName;

    public Vector<String> ImageName = new Vector<String>();
    public Vector<Integer> ImageId = new Vector<Integer>();

    private String Market;
    private int channel, subChannel;
    private String outletName;
    private String Address;
    private String Address2;


    private String Address3 = "";
    private String City, State;
    private String Phone, email;
    private String distid = "0";
    private String contactpersonname, contactpersonname2, Phone2;
    private String contactpersonnameLastName;
    private String contactpersonname2LastName;
    private String Contact1title, contact1titlelovid;
    private String Contact2title, contact2titlelovid;
    private int contractStatuslovid;
    private int routeid, locid, loc1id, loc2id;
    String Fax;
    String Email;
    String CreditLimit;
    String Vat;
    String Payment;
    String retailerId;
    String creditDays;


    public java.lang.String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(java.lang.String retailerId) {
        this.retailerId = retailerId;
    }

    private ArrayList<StandardListBO> priorityProductList;

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

    public void setTaxTypeId(java.lang.String taxTypeId) {
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

    public NewOutletBO(int locid, String listName) {
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


    public Vector<String> getImageName() {
        return ImageName;
    }

    public void setImageName(Vector<String> imageName) {
        ImageName = imageName;
    }

    public Vector<Integer> getImageId() {
        return ImageId;
    }

    public void setImageId(Vector<Integer> imageId) {
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
        // TODO Auto-generated method stub
        return listName;
    }

    public String getTinno() {
        return tinno;
    }

    public void setTinno(String tinno) {
        this.tinno = tinno;
    }

    private String tinno;

    public java.lang.String getGstNum() {
        return gstNum;
    }

    public void setGstNum(java.lang.String gstNum) {
        this.gstNum = gstNum;
    }

    private String gstNum;

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

    private String rfield3;

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    private String pincode;


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


}
