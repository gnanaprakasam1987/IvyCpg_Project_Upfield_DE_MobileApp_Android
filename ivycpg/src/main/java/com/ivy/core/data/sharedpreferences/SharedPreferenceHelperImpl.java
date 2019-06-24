package com.ivy.core.data.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.PreferenceInfo;

import javax.inject.Inject;

import static com.ivy.sd.png.model.ApplicationConfigs.LANGUAGE;

public class SharedPreferenceHelperImpl implements SharedPreferenceHelper {


    private static final String BASE_URL = "appUrlNew";
    private static final String APPLICATION_NAME = "application";
    private static final String ACTIVATION_KEY = "activationKey";
    private static final String LANGUAGE_PREF = "languagePref";
    private static final String TASK_NOTIFICATION = "taskNotificationFlag";


    private SharedPreferences defaultPreferences;

    @Inject
    public SharedPreferenceHelperImpl(@ApplicationContext Context context, @PreferenceInfo String prefFileName) {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getBaseUrl() {
        return defaultPreferences.getString(BASE_URL, "");
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        defaultPreferences.edit().putString(BASE_URL, baseUrl).apply();
    }

    @Override
    public String getApplicationName() {
        return defaultPreferences.getString(APPLICATION_NAME, "");
    }

    @Override
    public void setApplicationName(String applicationName) {
        defaultPreferences.edit().putString(APPLICATION_NAME, applicationName).apply();
    }

    @Override
    public String getActivationKey() {
        return defaultPreferences.getString(ACTIVATION_KEY, "");
    }

    @Override
    public void setActivationKey(String activationKey) {
        defaultPreferences.edit().putString(ACTIVATION_KEY, activationKey).apply();
    }

    @Override
    public String getPreferredLanguage() {
        return defaultPreferences.getString(LANGUAGE_PREF, LANGUAGE);
    }

    @Override
    public void setPreferredLanguage(String language) {
        defaultPreferences.edit().putString(LANGUAGE_PREF, language).apply();
    }

    @Override
    public boolean getTaskNotificationFlag() {
        return defaultPreferences.getBoolean(TASK_NOTIFICATION, false);
    }

    @Override
    public void setTaskNotificationFlag(boolean taskNotificationFlag) {
        defaultPreferences.edit().putBoolean(TASK_NOTIFICATION, taskNotificationFlag).apply();
    }


}