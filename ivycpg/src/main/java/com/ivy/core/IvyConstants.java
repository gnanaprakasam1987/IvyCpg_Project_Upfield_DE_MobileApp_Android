package com.ivy.core;

public class IvyConstants {


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


    public static final String VERSION_NAME = "VersionName";
    public static final String VERSION_CODE = "VersionCode";
    public static final String DEVICE_IMEI = "DeviceIMEI";
    public static final String LICENSE_KEY = "LicenseKey";


    public static final String NAMESPACE = "http://tempuri.org/";
}
