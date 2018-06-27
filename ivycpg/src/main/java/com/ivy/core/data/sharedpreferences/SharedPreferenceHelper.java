package com.ivy.core.data.sharedpreferences;

interface SharedPreferenceHelper {

    String getBaseUrl();

    void setBaseUrl(String baseUrl);

    String getApplicationName();

    void setApplicationName(String applicationName);

    String getActivationKey();

    void setActivationKey(String activationKey);

    String getPreferredLanguage();

    void setPreferredLanguage(String language);

}
