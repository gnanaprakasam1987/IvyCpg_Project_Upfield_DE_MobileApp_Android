package com.ivy.core.data.sharedpreferences;

public interface SharedPreferenceHelper {

    public String getBaseUrl();

    public void setBaseUrl(String baseUrl);

    public String getApplicationName();

    public void setApplicationName(String applicationName);

    public String getActivationKey();

    public void setActivationKey(String activationKey);

    public String getPreferredLanguage();

    public void setPreferredLanguage(String language);

    boolean getTaskNotificationFlag();

    void setTaskNotificationFlag(boolean taskNotificationFlag);

}
