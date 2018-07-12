package com.ivy.ui.activation.data;

import com.ivy.core.IvyConstants;
import com.ivy.lib.rest.MyKsoapConnection;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.util.Commons;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
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
                Commons.print("Sync Url Success response code>>>>>>>>>>" + responseCode);
                return responseCode == HttpURLConnection.HTTP_OK;

            }
        });
    }


    @Override
    public Observable<JSONObject> doActivationAtHttp(final String activationKey, final String applicationVersionName,
                                                     final String applicationVersionNumber, final String imEiNumber) {

        return Observable.create(new ObservableOnSubscribe<JSONObject>() {
            @Override
            public void subscribe(final ObservableEmitter<JSONObject> subscriber) throws Exception {
                MyKsoapConnection myKsoapConnection = new MyKsoapConnection();


                if (activationKey != null) {
                    //Using ActivationKey
                    myKsoapConnection.create(IvyConstants.METHOD_NAME_SECURITYPOLICY1,
                            ApplicationConfigs.LICENSE_SOAP_URL,
                            IvyConstants.SOAP_ACTION_SECURITYPOLICY1, IvyConstants.NAMESPACE);

                    myKsoapConnection.addParam(IvyConstants.LICENSE_KEY, activationKey);

                } else {
                    //Using IMEI
                    myKsoapConnection.create(IvyConstants.METHOD_NAME_SECURITYPOLICY2,
                            ApplicationConfigs.LICENSE_SOAP_URL,
                            IvyConstants.SOAP_ACTION_SECURITYPOLICY2, IvyConstants.NAMESPACE);

                }


                myKsoapConnection.addParam(IvyConstants.VERSION_CODE, applicationVersionNumber);
                myKsoapConnection.addParam(IvyConstants.DEVICE_IMEI, imEiNumber);
                myKsoapConnection.addParam(IvyConstants.VERSION_NAME, applicationVersionName);

                myKsoapConnection.connectServer(new MyKsoapConnection.ResponseListener() {

                    @Override
                    public void onFailure(int status, String message) {

                        ActivationError myError = new ActivationError(status, message);

                        subscriber.onError(myError);

                    }

                    @Override
                    public void onSucess(JSONObject jsonObj) {
                        subscriber.onNext(jsonObj);
                    }

                });

                subscriber.onComplete();


            }

        });
    }

    @Override
    public Observable<JSONObject> doIMEIActivationAtHttp(final String imEi, final String versionName, final String versionNumber) {

        return doActivationAtHttp(null, versionName, versionNumber, imEi);
    }


}



