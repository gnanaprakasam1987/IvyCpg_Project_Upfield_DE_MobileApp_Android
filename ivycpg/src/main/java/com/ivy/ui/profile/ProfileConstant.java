package com.ivy.ui.profile;



public class ProfileConstant {

 // * ROFILE09, PROFILE10, PROFILE11, PROFILE12, PROFILE41, PROFILE42*/
    public static final String PROFILE_60 = "PROFILE60";//Retailer Image :Note:-Only Profile Config,Has Edit=0

    public static final String STORENAME = "PROFILE02";//STORENAME,length validation=Y,Has Edit=0,RetailerMaster
    public static final String ADDRESS1 = "PROFILE03";//ADDRESS1,length validation=Y,Has Edit=1,RetailerMaster
    public static final String ADDRESS2 = "PROFILE04";//ADDRESS2,length validation=Y,Has Edit=1,RetailerMaster
    public static final String ADDRESS3 = "PROFILE05";//ADDRESS3,length validation=Y,Has Edit=1,RetailerMaster
    public static final String CHANNEL = "PROFILE06";//CHANNEL,length validation=Y,Has Edit=1,RetailerMaster
    public static final String SUBCHANNEL = "PROFILE07";//SUBCHANNEL,length validation=Y,Has Edit=1,RetailerMaster
    public static final String LATTITUDE= "PROFILE08";//Latitude,Longitude,length validation=Y,Has Edit=1,RetailerAddress

    public static final String PROFILE_09 = "PROFILE09";//CONTACT_PERSON1,length validation=Y,Has Edit=0,RetailerContact
    public static final String PROFILE_10 = "PROFILE10";//PHNO1,length validation=Y,input validation=NUMBER,Has Edit=0,RetailerContact
    public static final String PROFILE_11 = "PROFILE11";//CONTACT_PERSON2,length validation=Y,Has Edit=0,RetailerContact
    public static final String PROFILE_12 = "PROFILE12";//PHNO2,length validation=Y,input validation=NUMBER,Has Edit=0,RetailerContact
    public static final String PROFILE_41 = "PROFILE41";//CONTACT_TITLE 1 ,length validation=Y,Has Edit=1,,RetailerContact
    public static final String PROFILE_42 = "PROFILE42";//CONTACT_TITLE 2,length validation=Y,Has Edit=1,RetailerContact

   public static final String LOCATION02 = "PROFILE14";//LOCATION02 two level up,length validation=Y,Has Edit=0,RetailerMaster
   public static final String LOCATION01 = "PROFILE13";//LOCATION01 One level up,length validation=Y,Has Edit=0,RetailerMaster
    public static final String LOCATION   = "PROFILE15";//LOCATION Least level,length validation=Y,Has Edit=0,RetailerMaster
    public static final String RFiled1 = "PROFILE20";//RFiled1, IF (FUN42) -> CreditLimit - Outstanding,Has Edit=0
    public static final String CONTRACT_TYPE = "PROFILE22";//CONTRACT_TYPE,Has Edit=0 Note:-Only Profile Config
    public static final String CREDITPERIOD = "PROFILE25";//CREDITPERIOD,level,length validation=Y,Has Edit=0,RetailerMaster
    public static final String RField2 = "PROFILE26";//RField2,Has Edit=1 Note:-Only Profile Config
    public static final String CREDIT_INVOICE_COUNT = "PROFILE27"; //Credit_invoice_count
    public static final String RField4 = "PROFILE28";//RField4,length validation=Y,Has Edit=1,RetailerMaster
    public static final String CONTACT_NUMBER = "PROFILE30";//CONTACT_NUMBER Note:-Only Profile Config
    public static final String LONGITUDE = "PROFILE31";//Latitude,Longitude,length validation=Y,Has Edit=1,RetailerMaster
    public static final String NEARBYRET = "PROFILE36";//NEARBYRET(retailers)length validation=Y,Has Edit=1,RetailerMaster
    public static final String PINCODE = "PROFILE38";//PINCODE,length validation=Y,Has Edit=0, RetailerAddress
    public static final String CITY = "PROFILE39";//CITY :Note:-Only Profile Config,Has Edit=1
    public static final String STATE = "PROFILE40";//STATE ,length validation=Y,Has Edit=0, RetailerAddress

    public static final String CONTRACT = "PROFILE43";//CONTRACT->contract status id,length validation=Y,Has Edit=0, ,RetailerMaster
    public static final String OUTSTANDING = "PROFILE47";//OUTSTANDING :Note:-Only Profile Config,Has Edit=0
    public static final String RETURN_CREDIT_LIMIT = "PROFILE48";//RETURN_CREDIT_LIMIT :Note:-Only Profile Config,Has Edit=0
    public static final String INVOICE = "PROFILE49";//INVOICE Amount Calculation :Note:-Only Profile Config,Has Edit=0
    public static final String LOCATION_NAME = "PROFILE50";//LOCATION_NAME.Retailer Master : locationid :Note:-Only Profile Config,Has Edit=0
    public static final String TAXTYPE = "PROFILE51";//TAXTYPE,length validation=Y,Has Edit=0, ,RetailerMaster
    public static final String RFIELD5 = "PROFILE53";//RFIELD5-->RetailerMaster
    public static final String RFIELD6 = "PROFILE54";//RFIELD6-->RetailerMaster
    public static final String RFIELD7 = "PROFILE55";//RFIELD7-->RetailerMaster
    public static final String PRIORITYPRODUCT = "PROFILE57";//PRIORITYPRODUCT -->From  PriorityProducts Table
    public static final String ATTRIBUTE = "PROFILE58";//ATTRIBUTE -->From RetailerAttribute Table
    public static final String GSTN = "PROFILE61";//GSTN Number,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String INSEZ = "PROFILE62";//IN_SEZ,length validation=Y,Has Edit=0, ,RetailerMaster
    public static final String PHOTO_CAPTURE = "PROFILE63";//PHOTO_CAPTURE , lat and Long, Note:-Only Profile Config,Has Edit=0
    public static final String EMAIL = "PROFILE78";//EMAIL,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String MOBILE = "PROFILE79";//MOBILE ,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String PAN_NUMBER = "PROFILE81";//PAN_NUMBER,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String FOOD_LICENCE_NUM = "PROFILE82";//FOOD_LICENCE_NUM,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String FOOD_LICENCE_EXP_DATE = "PROFILE83";//FOOD_LICENCE_EXP_DATE,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String DRUG_LICENSE_NUM = "PROFILE84";//DRUG_LICENSE_NUM,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String DRUG_LICENSE_EXP_DATE = "PROFILE85";//DRUG_LICENSE_EXP_DATE,length validation=Y,Has Edit=1,RetailerMaster;
    public static final String FAX = "PROFILE86";//FAX,length validation=Y,Has Edit=1,RetailerAddress;
    public static final String REGION = "PROFILE87";//REGION,length validation=Y,Has Edit=1,RetailerAddress;
    public static final String COUNTRY = "PROFILE88";//COUNTRY,length validation=Y,Has Edit=1,RetailerAddress;
   public static final String DISTRICT = "PROFILE89";//DISTRICT,length validation=Y,Has Edit=1,RetailerAddress;

    /*Configuration RField id's List */
    public static final String RFIELD_4 = "RFIELD4";
    public static final String RFIELD_5 = "RFIELD5";
    public static final String RFIELD_6 = "RFIELD6";
    public static final String RFIELD_7 = "RFIELD7";

   public static final String RFIELD10 = "PROFILE93";
   public static final String RFIELD11 = "PROFILE94";
   public static final String RFIELD12 = "PROFILE95";
   public static final String RFIELD13 = "PROFILE96";
   public static final String RFIELD14 = "PROFILE97";
   public static final String RFIELD15 = "PROFILE98";
   public static final String RFIELD16 = "PROFILE99";
   public static final String RFIELD17 = "PROFILE100";
   public static final String RFIELD18 = "PROFILE101";
   public static final String RFIELD19 = "PROFILE102";
   public static final String RFIELD20 = "PROFILE103";

    /*Configuration Static Constant */
    public static final String D = "D";
    public static final String LNAME = "LNAME";
    public static final String LNAME_2 = "LNAME2";
    public static final String CT_2_TITLE = "CT2TITLE";
    public static final String CT_1_TITLE = "CT1TITLE";

}
