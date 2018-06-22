package com.ivy.ui.activation.data;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface ActivationDataManager {

    Single<Boolean> isServerOnline(String serverUrl);

    Observable doActivationAtHttp(String key, String applicationVersionName, String applicationVersionNumber, String ieMiNumber);

    Observable doIMEIActivationAtHttp(String imEi, String versionName, String versionNumber);
}
