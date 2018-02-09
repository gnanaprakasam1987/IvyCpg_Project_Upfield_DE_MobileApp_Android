package com.ivy.sd.png.model;

public interface
ApplicationConfigs {

    String DB_NAME = "ivycpg_v18";

    boolean withActivation = false;

    boolean checkUTCTime = true;
    String LANGUAGE = "ar";

    boolean expiryEnable = false;
    String expiryDate = "2013/09/01";

    boolean hasMotoBarcodeScanner = false;

    String LOG = "com.ivymobility.ivycpg";

    String LICENSE_SOAP_URL = "https://license.ivymobileapps.com/eistlicensing_v2/licenseservice.asmx";

    boolean isEncrypted = false;
}
