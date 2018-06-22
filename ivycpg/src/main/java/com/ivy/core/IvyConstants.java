package com.ivy.core;

public class IvyConstants {


    /**
     * Class cannot be initialised.
     * To be used only for accessing Static constants
     */
    private IvyConstants(){

    }

    public static String IVY_PREFERENCE_NAME = "";

    public final static int NOTIFY_ALREADY_NOT_ACTIVATED = 0;
    public final static int NOTIFY_ALREADY_ACTIVATED = 1;
    public final static int NOTIFY_CONNECTIVITY_ERROR = -1;
    public final static int NOTIFY_SERVER_ERROR = 2;
    public final static int NOTIFY_CONNECTION_PROBLEM = 3;
    public final static int NOTIFY_SUCESSFULLY_ACTIVATED = 4;
    public final static int NOTIFY_INVALID_KEY = 5;
    public final static int NOTIFY_ACTIVATION_FAILED = 6;
    public final static int NOTIFY_ACTIVATION_LIST = 7;
    public final static int NOTIFY_ACTIVATION_LIST_SINGLE = 8;
    public final static int NOTIFY_ACTIVATION_LIST_NULL = 9;
    public final static int NOTIFY_URL_EMPTY = 10;
    public final static int NOTIFY_NOT_VALID_URL = 11;
    public final static int NOTIFY_VALID_URL = 12;
    public final static int NOTIFY_SUCESSFULLY_ACTIVATED_EXTENDED = 13;
    public final static int NOTIFY_ACTIVATION_LIST_SINGLE_EXTEND = 14;
    public final static int NOTIFY_URL_NOT_MAPPED_ERROR = 15;
    public final static int NOTIFY_JSON_EXCEPTION = 16;
    public static final String SOAP_ACTION_SECURITYPOLICY1 = "http://tempuri.org/GetKeySyncURL";
    public static final String SOAP_ACTION_SECURITYPOLICY2 = "http://tempuri.org/GetIMEISyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY1 = "GetKeySyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY2 = "GetIMEISyncURL";


    public static final String VERSION_NAME = "VersionName";

    public static final String NAMESPACE = "http://tempuri.org/";
}
