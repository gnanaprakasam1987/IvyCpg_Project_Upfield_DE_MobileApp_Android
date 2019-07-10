package com.ivy.core;

public class IvyConstants {


    public static final String UPDATE_TABLE_SUCCESS_CODE = "-1";
    public static final String AUTHENTICATION_SUCCESS_CODE = "0";

    /**
     * Class cannot be initialised.
     * To be used only for accessing Static constants
     */
    private IvyConstants(){

    }

    public static String IVY_PREFERENCE_NAME = "";
    public static String EMPTY_STRING = "";

    //ActivationError
    public static final int NOTIFY_URL_NOT_MAPPED_ERROR = 15;
    public final static int NOTIFY_INVALID_KEY = 5;
    public final static int NOTIFY_ACTIVATION_FAILED = 6;

    public static final String SOAP_ACTION_SECURITYPOLICY1 = "http://tempuri.org/GetKeySyncURL";
    public static final String SOAP_ACTION_SECURITYPOLICY2 = "http://tempuri.org/GetIMEISyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY1 = "GetKeySyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY2 = "GetIMEISyncURL";

    public static final String USER_IDENTITY = "UserIdentity";

    public static final String VERSION_NAME = "VersionName";
    public static final String VERSION_CODE = "VersionCode";
    public static final String DEVICE_IMEI = "DeviceIMEI";
    public static final String LICENSE_KEY = "LicenseKey";


    public static final String NAMESPACE = "http://tempuri.org/";

    public static String DEFAULT_DATE_FORMAT = "MM/dd/yyyy"; // Default Date Format

    public static final String PRIVACY_POLICY_URL = "http://ivymobility.com/index.php/privacy-policy/";

    public static final int AUDIT_NOT_OK = 0;
    public static final int AUDIT_OK = 1;
    public static final int AUDIT_DEFAULT = 2;

    public static final String SAS_KEY_TYPE ="SAS";
    public static final String DEFAULT_TIME_CONSTANT = "1970/01/01 00:00:00";


    public static final String DATA_NOT_AVAILABLE_ERROR = "E19";

    public static final String IS_MANDATORY ="isMandatory";
}
