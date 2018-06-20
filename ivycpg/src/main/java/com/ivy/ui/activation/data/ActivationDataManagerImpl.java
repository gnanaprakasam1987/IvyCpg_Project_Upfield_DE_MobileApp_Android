package com.ivy.ui.activation.data;

import com.ivy.sd.png.util.Commons;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;

public class ActivationDataManagerImpl implements ActivationDataManager {

    @Override
    public Single<Boolean> isServerOnline(final String serverUrl) {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL urlobj = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) urlobj.openConnection();
                int responseCode = urlConnection.getResponseCode();
                Commons.print("Sync Url Success response code>>>>>>>>>>"
                        + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK)
                    return true;
                else
                    return false;
            }
        });
    }


}
