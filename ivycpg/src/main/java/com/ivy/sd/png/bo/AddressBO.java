package com.ivy.sd.png.bo;

/**
 * Created by rajkumar.s on 9/8/2017.
 */

public class AddressBO {

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
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

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    private String address1,address2,address3;
    private String city,state,pincode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String email,fax,phone;

    public int getAddressTypeId() {
        return addressTypeId;
    }

    public void setAddressTypeId(int addressTypeId) {
        this.addressTypeId = addressTypeId;
    }

       private int addressTypeId;

    public double getNewOutletlattitude() {
        return NewOutletlattitude;
    }

    public void setNewOutletlattitude(double newOutletlattitude) {
        NewOutletlattitude = newOutletlattitude;
    }

    public double getNewOutletLongitude() {
        return NewOutletLongitude;
    }

    public void setNewOutletLongitude(double newOutletLongitude) {
        NewOutletLongitude = newOutletLongitude;
    }

    double NewOutletlattitude, NewOutletLongitude;
}
