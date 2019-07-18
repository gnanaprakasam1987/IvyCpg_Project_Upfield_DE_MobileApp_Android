package com.ivy.sd.png.model;

import com.ivy.sd.png.asean.view.BuildConfig;

public interface
ApplicationConfigs {

//    String DB_NAME = "ivycpg_v20";

    String DB_NAME = "ivycpg_v20_sfdc_sandbox";

    boolean withActivation = BuildConfig.ACTIVATION;

    boolean checkUTCTime = true;
    String LANGUAGE = "en";

    boolean expiryEnable = false;
    String expiryDate = "2013/09/01";

    boolean hasMotoBarcodeScanner = false;

    String LOG = "com.ivymobility.ivycpg";

    String LICENSE_SOAP_URL = "https://license.ivymobileapps.com/eistlicensing_v2/licenseservice.asmx";

    boolean isEncrypted = false;
}
