package com.ivy.cpg.junit;

import com.ivy.sd.png.model.ApplicationConfigs;

import junit.framework.Assert;

import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by ramkumar.d on 12-05-2016.
 */
public class UTApplicationConfigs implements ApplicationConfigs {
    @Test
    public void checkWithActivation() {
        Assert.assertTrue("ApplicationConfig: Expected value for variable - withActivation is True but actual value is " + withActivation, withActivation);
    }

    @Test
    public void checkUTC_Time() {
        Assert.assertTrue("ApplicationConfig: Expected value for variable - checkUTCTime is True but actual value is " + checkUTCTime, checkUTCTime);
    }

    @Test
    public void checkExpiryEnable() {
        Assert.assertFalse("ApplicationConfig: Expected value for variable - expiryEnable is False but actual value is " + expiryEnable, expiryEnable);
    }

    @Test
    public void checkMotoBarcodeScanner() {
        Assert.assertFalse("ApplicationConfig: Expected value for variable - hasMotoBarcodeScanner is False but actual value is " + hasMotoBarcodeScanner, hasMotoBarcodeScanner);
    }

    @Test
    public void checkLanguage() {
        Assert.assertSame("ApplicationConfig: Expected value for variable - LANGUAGE is en but actual value is " + LANGUAGE, "en", LANGUAGE);
    }

    @Test
    public void checkDB_Name() {
        Assert.assertTrue("ApplicationConfig: Expected value for variable - DB_NAME is ivycpg_v followed by 2 digit version number but actual value is " + DB_NAME, Pattern.matches("^ivycpg_v\\d\\d$", DB_NAME));
    }

    @Test
    public void checkLog() {
        Assert.assertTrue("ApplicationConfig: Expected value for variable - LOG is com.ivymobility.ivycpg but actual value is " + LOG, LOG.equals("com.ivymobility.ivycpg"));
    }

    @Test
    public void checkLicense_Soap_Url() {
        Assert.assertTrue("ApplicationConfig: Expected value for variable - LICENSE_SOAP_URL is https://license.ivymobileapps.com/eistlicensing_v2/licenseservice.asmx but actual value is " + LICENSE_SOAP_URL, LICENSE_SOAP_URL.equals("https://license.ivymobileapps.com/eistlicensing_v2/licenseservice.asmx"));
    }
}
