package com.ivy.ui.profile.create;

import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.UserMasterBO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class NewOutletTestDataFactory {

    public static ArrayList<NewOutletBO> getNewRetailerList() {
        ArrayList<NewOutletBO> newOutletBOS = new ArrayList<>();
        newOutletBOS.add(new NewOutletBO());
        return newOutletBOS;
    }

    public static UserMasterBO getUser() {

        UserMasterBO userMasterBO = new UserMasterBO();
        userMasterBO.setUserid(2);
        userMasterBO.setDistributorid(1);
        return userMasterBO;
    }

    public static ArrayList<ConfigureBO> getProfileConfig() {

        ArrayList<ConfigureBO> configureBOS = new ArrayList<>();

        ConfigureBO chenanlBo = new ConfigureBO();
        chenanlBo.setConfigCode(NewRetailerConstant.CHANNEL);
        configureBOS.add(chenanlBo);

        ConfigureBO subChannelBo = new ConfigureBO();
        subChannelBo.setConfigCode(NewRetailerConstant.SUBCHANNEL);
        configureBOS.add(subChannelBo);

        ConfigureBO routeBo = new ConfigureBO();
        routeBo.setConfigCode(NewRetailerConstant.ROUTE);
        configureBOS.add(routeBo);

        ConfigureBO locationBo = new ConfigureBO();
        locationBo.setConfigCode(NewRetailerConstant.LOCATION);
        configureBOS.add(locationBo);

        ConfigureBO storeName = new ConfigureBO();
        storeName.setConfigCode(NewRetailerConstant.STORENAME);
        configureBOS.add(storeName);

        ConfigureBO address1 = new ConfigureBO();
        address1.setConfigCode(NewRetailerConstant.ADDRESS1);
        configureBOS.add(address1);

        ConfigureBO address2 = new ConfigureBO();
        address2.setConfigCode(NewRetailerConstant.ADDRESS2);
        configureBOS.add(address2);

        ConfigureBO CITY = new ConfigureBO();
        CITY.setConfigCode(NewRetailerConstant.CITY);
        configureBOS.add(CITY);

        ConfigureBO STATE = new ConfigureBO();
        STATE.setConfigCode(NewRetailerConstant.STATE);
        configureBOS.add(STATE);

        ConfigureBO PAN_NUMBER = new ConfigureBO();
        PAN_NUMBER.setConfigCode(NewRetailerConstant.PAN_NUMBER);
        configureBOS.add(PAN_NUMBER);

        ConfigureBO DRUG_LICENSE_NUM = new ConfigureBO();
        DRUG_LICENSE_NUM.setConfigCode(NewRetailerConstant.DRUG_LICENSE_NUM);
        configureBOS.add(DRUG_LICENSE_NUM);

        ConfigureBO FOOD_LICENCE_NUM = new ConfigureBO();
        FOOD_LICENCE_NUM.setConfigCode(NewRetailerConstant.FOOD_LICENCE_NUM);
        configureBOS.add(FOOD_LICENCE_NUM);

        ConfigureBO REGION = new ConfigureBO();
        REGION.setConfigCode(NewRetailerConstant.REGION);
        configureBOS.add(REGION);

        ConfigureBO COUNTRY = new ConfigureBO();
        COUNTRY.setConfigCode(NewRetailerConstant.COUNTRY);
        configureBOS.add(COUNTRY);

        ConfigureBO CONTACTPERSON1 = new ConfigureBO();
        CONTACTPERSON1.setConfigCode(NewRetailerConstant.CONTACT_PERSON1);
        configureBOS.add(CONTACTPERSON1);

        ConfigureBO CONTACTPERSON2 = new ConfigureBO();
        CONTACTPERSON2.setConfigCode(NewRetailerConstant.CONTACT_PERSON2);
        configureBOS.add(CONTACTPERSON2);

        ConfigureBO CREDITPERIOD = new ConfigureBO();
        CREDITPERIOD.setConfigCode(NewRetailerConstant.CREDITPERIOD);
        configureBOS.add(CREDITPERIOD);

        ConfigureBO EMAIL = new ConfigureBO();
        EMAIL.setConfigCode(NewRetailerConstant.EMAIL);
        configureBOS.add(EMAIL);

        ConfigureBO PHNO1 = new ConfigureBO();
        PHNO1.setConfigCode(NewRetailerConstant.PHNO1);
        configureBOS.add(PHNO1);

        ConfigureBO PHNO2 = new ConfigureBO();
        PHNO2.setConfigCode(NewRetailerConstant.PHNO2);
        configureBOS.add(PHNO2);

        ConfigureBO CREDITLIMIT = new ConfigureBO();
        CREDITLIMIT.setConfigCode(NewRetailerConstant.CREDITLIMIT);
        configureBOS.add(CREDITLIMIT);

        ConfigureBO FAX = new ConfigureBO();
        FAX.setConfigCode(NewRetailerConstant.FAX);
        configureBOS.add(FAX);

        ConfigureBO MOBILE = new ConfigureBO();
        MOBILE.setConfigCode(NewRetailerConstant.MOBILE);
        configureBOS.add(MOBILE);

        ConfigureBO CONTRACT = new ConfigureBO();
        CONTRACT.setConfigCode(NewRetailerConstant.CONTRACT);
        configureBOS.add(CONTRACT);

        ConfigureBO TAXTYPE = new ConfigureBO();
        TAXTYPE.setConfigCode(NewRetailerConstant.TAXTYPE);
        configureBOS.add(TAXTYPE);

        ConfigureBO PRIORITYPRODUCT = new ConfigureBO();
        PRIORITYPRODUCT.setConfigCode(NewRetailerConstant.PRIORITYPRODUCT);
        configureBOS.add(PRIORITYPRODUCT);

        ConfigureBO CLASS = new ConfigureBO();
        CLASS.setConfigCode(NewRetailerConstant.CLASS);
        configureBOS.add(CLASS);

        ConfigureBO RFIELD5 = new ConfigureBO();
        RFIELD5.setConfigCode(NewRetailerConstant.RFIELD5);
        configureBOS.add(RFIELD5);

        ConfigureBO PINCODE = new ConfigureBO();
        PINCODE.setConfigCode(NewRetailerConstant.PINCODE);
        configureBOS.add(PINCODE);

        ConfigureBO GST_NO = new ConfigureBO();
        GST_NO.setConfigCode(NewRetailerConstant.GST_NO);
        configureBOS.add(GST_NO);

        ConfigureBO LATLONG = new ConfigureBO();
        LATLONG.setConfigCode(NewRetailerConstant.LATLONG);
        configureBOS.add(LATLONG);

        ConfigureBO TINEXPDATE = new ConfigureBO();
        TINEXPDATE.setConfigCode(NewRetailerConstant.TIN_EXP_DATE);
        configureBOS.add(TINEXPDATE);

        ConfigureBO DRUG_LICENSE_EXP_DATE = new ConfigureBO();
        DRUG_LICENSE_EXP_DATE.setConfigCode(NewRetailerConstant.DRUG_LICENSE_EXP_DATE);
        configureBOS.add(DRUG_LICENSE_EXP_DATE);

        ConfigureBO FOOD_LICENCE_EXP_DATE = new ConfigureBO();
        FOOD_LICENCE_EXP_DATE.setConfigCode(NewRetailerConstant.FOOD_LICENCE_EXP_DATE);
        configureBOS.add(FOOD_LICENCE_EXP_DATE);

        ConfigureBO NEARBYRET = new ConfigureBO();
        NEARBYRET.setConfigCode(NewRetailerConstant.NEARBYRET);
        configureBOS.add(NEARBYRET);

        ConfigureBO INSEZ = new ConfigureBO();
        INSEZ.setConfigCode(NewRetailerConstant.IN_SEZ);
        configureBOS.add(INSEZ);


        return configureBOS;
    }

    public static JSONObject successResponse;

    public static JSONObject errorResponse;

    static {
        try {
            errorResponse = new JSONObject("{\"Master\":\"UserMaster\",\"Field\":[],\"Data\":[],\"ErrorCode\":\"E05\",\"Next\":\"0\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            successResponse = new JSONObject("{\"Master\":\"UserMaster\",\"Field\":[\"OrganisationId\",\"UserId\",\"UserName\",\"UserType\",\"UserCode\",\"Credit_limit\",\"LoginId\",\"Password\",\"WarehouseId\",\"WarehouseName\",\"VanId\",\"VanName\",\"VanNo\",\"GroupCount\",\"Auditor_Name\",\"Auditor_Login_Id\",\"Auditor_Password\",\"TimeZone\",\"DistributorId\",\"BranchId\",\"DistributorName\",\"distributorTinNumber\",\"distContactNo\",\"UpliftFactor\",\"SchemeFactor\",\"Address1\",\"Address2\",\"Address3\",\"admincno\",\"IsDeviceUser\",\"custommsg\",\"downloaddate\",\"accountno\",\"RelationShip\"],\"Data\":[[73,1686,\"D Nat Admin dist1 - NSM\",\"National Sales Manager\",\"DNATADMIN\",0,\"d.nat.admin\",\"X03MO1qnZdYdgyfeuILPmQ==\",0,\"\",0,\"\",\"\",0,\"\",\"\",\"\",\"IST\",0,0,\"\",\"\",\"\",0,0,\"\",\"\",\"\",\"\",0,\"\",\"2016/12/29\",\"\",\"PARENT\"],[73,1687,\"D Div Admin dist1 - DSM\",\"Divisional Sales Manager\",\"DDIVADMIN\",0,\"d.div.admin\",\"X03MO1qnZdYdgyfeuILPmQ==\",0,\"\",0,\"\",\"\",0,\"\",\"\",\"\",\"IST\",0,0,\"\",\"\",\"\",0,0,\"\",\"\",\"\",\"\",0,\"\",\"2016/12/29\",\"\",\"PARENT\"],[73,1688,\"D Area Admin dist1 - SO\",\"SO\",\"DAREAADMIN\",0,\"d.area.admin\",\"X03MO1qnZdYdgyfeuILPmQ==\",0,\"\",0,\"\",\"\",0,\"\",\"\",\"\",\"IST\",0,0,\"\",\"\",\"\",0,0,\"\",\"\",\"\",\"\",0,\"\",\"2016/12/29\",\"\",\"PARENT\"],[73,1689,\"Kanjan Junga - DIS\",\"Distributor\",\"1689\",0,\"db001.admin\",\"X03MO1qnZdYdgyfeuILPmQ==\",0,\"\",0,\"\",\"\",0,\"\",\"\",\"\",\"IST\",0,0,\"\",\"\",\"\",0,0,\"\",\"\",\"\",\"\",0,\"\",\"2016/12/29\",\"\",\"PARENT\"],[73,3429,\"test  - DIS\",\"Distributor\",\"test\",0,\"test_test\",\"X03MO1qnZdYdgyfeuILPmQ==\",0,\"\",0,\"\",\"\",0,\"\",\"\",\"\",\"IST\",0,0,\"\",\"\",\"\",0,0,\"\",\"\",\"\",\"\",0,\"\",\"2016/12/29\",\"\",\"PARENT\"],[73,1695,\"VSR01 dist1\",\"Van Sales Rep\",\"VSR01\",30000,\"vsr01\",\"X03MO1qnZdYdgyfeuILPmQ==\",92,\"கிடங்கு\",81,\"Van 2\",\"TN 59 5578\",0,\"\",\"\",\"\",\"IST\",245,245,\"Akasar\",\"q2der\",\"Kanjan  Junga\",0,0,\"DB001 Address 1\",\"DB001 Address 2\",\"\",\"\",1,\"\",\"2016/12/29\",\"\",\"\"]],\"ErrorCode\":\"0\",\"Next\":\"0\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
