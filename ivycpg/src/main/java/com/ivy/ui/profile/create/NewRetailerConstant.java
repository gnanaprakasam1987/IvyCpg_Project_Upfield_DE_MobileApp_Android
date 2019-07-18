package com.ivy.ui.profile.create;

public class NewRetailerConstant {

    public enum MenuType {
        OTHER(0), VIEW(1), EDIT(2), CREATE_FRM_EDT_SCREEN(4);

        private final int menuType;

        MenuType(int menuType) {
            this.menuType = menuType;
        }

        public int getMenuType() {
            return menuType;
        }
    }

    public enum ContactTitleOption {
        FIRSTNAME, LASTNAME, TITLE;
    }

    public static final String MENU_NEW_RETAILER = "MENU_NEW_RET";
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final String CHANNEL = "CHANNEL";
    public static final String SUBCHANNEL = "SUBCHANNEL";
    public static final String ROUTE = "ROUTE";
    public static final String LOCATION = "LOCATION";
    public static final String LOCATION1 = "LOCATION01";
    public static final String LOCATION2 = "LOCATION02";
    public static final String USER = "USER";
    public static final String DISTRIBUTOR = "DISTRIBUTOR";
    public static final String ATTRIBUTE = "ATTRIBUTE";

    public static final String GST_NO = "GST_NO";
    public static final String PAN_NUMBER = "PAN_NUMBER";
    public static final String EMAIL = "EMAIL";
    public static final String STORENAME = "STORENAME";
    public static final String ADDRESS1 = "ADDRESS1";
    public static final String ADDRESS2 = "ADDRESS2";
    public static final String ADDRESS3 = "ADDRESS3";
    public static final String CONTACT_PERSON1 = "CONTACT_PERSON1";
    public static final String CONTACT_PERSON2 = "CONTACT_PERSON2";
    public static final String CITY = "CITY";
    public static final String STATE = "STATE";
    public static final String DISTRICT = "DISTRICT";
    public static final String PHNO1 = "PHNO1";
    public static final String PHNO2 = "PHNO2";
    public static final String PLAN = "PLAN";
    public static final String FAX = "FAX";
    public static final String CREDITLIMIT = "CREDITLIMIT";
    public static final String TIN_NUM = "TIN_NUM";
    public static final String TIN_EXP_DATE = "TIN_EXP_DATE";
    public static final String PINCODE = "PINCODE";
    public static final String RFIELD3 = "RFIELD3";
    public static final String RFIELD4 = "RFIELD4";
    public static final String RFIELD5 = "RFIELD5";
    public static final String RFIELD6 = "RFIELD6";
    public static final String RFIELD7 = "RFIELD7";

    public static final String RFIELD8 = "RFIELD8";
    public static final String RFIELD9 = "RFIELD9";
    public static final String RFIELD10 = "RFIELD10";
    public static final String RFIELD11 = "RFIELD11";
    public static final String RFIELD12 = "RFIELD12";
    public static final String RFIELD13 = "RFIELD13";
    public static final String RFIELD14 = "RFIELD14";
    public static final String RFIELD15 = "RFIELD15";
    public static final String RFIELD16 = "RFIELD16";
    public static final String RFIELD17 = "RFIELD17";
    public static final String RFIELD18 = "RFIELD18";
    public static final String RFIELD19 = "RFIELD19";

    public static final String CREDITPERIOD = "CREDITPERIOD";
    public static final String DRUG_LICENSE_NUM = "DRUG_LICENSE_NUM";
    public static final String FOOD_LICENCE_NUM = "FOOD_LICENCE_NUM";
    public static final String DRUG_LICENSE_EXP_DATE = "DRUG_LICENSE_EXP_DATE";
    public static final String FOOD_LICENCE_EXP_DATE = "FOOD_LICENCE_EXP_DATE";
    public static final String REGION = "REGION";
    public static final String COUNTRY = "COUNTRY";
    public static final String MOBILE = "MOBILE";
    public static final String LATLONG = "LATLONG";

    public static final String CONTACT_TITLE = "CONTACT_TITLE";
    public static final String CONTRACT = "CONTRACT";
    public static final String PAYMENTTYPE = "PAYMENTTYPE";
    public static final String TAXTYPE = "TAXTYPE";
    public static final String CLASS = "CLASS";
    public static final String PRIORITYPRODUCT = "PRIORITYPRODUCT";
    public static final String NEARBYRET = "NEARBYRET";
    public static final String IN_SEZ = "IN_SEZ";


    public static final int NUMBER_OF_WEEKS = 5;
    public static final int MAX_NO_OF_DAYS = 7;
    public static final int MAX_CLICK_DURATION = 200;

    public static final int CONTACT_PERSON_FIRSTNAME_KEY = 1100;
    public static final int CONTACT_PERSON_LASTNAME_KEY = 1101;
    public static final int CONTACT_PERSON_OTHERNAME_KEY = 1102;

    public static final int WEEK_TEXT_LABEL = 1000;
    public static final int DAY_TEXT_LABEL = 1001;

    public static final String NEW_RETAILER = "NEW_RETAILER";
    public static final String moduleName = "NO_";

    public static final int NO_RETAILER_DOWNLOAD_URL = 1;
}
