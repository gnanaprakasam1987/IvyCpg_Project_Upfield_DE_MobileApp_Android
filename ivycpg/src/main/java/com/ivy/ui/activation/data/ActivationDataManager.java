package com.ivy.ui.activation.data;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ActivationDataManager {

    Single<Boolean> isServerOnline(String serverUrl);

}
