package com.ivy.ui.activation.presenter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ActivationBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;

import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.ScreenActivationFragment;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.ui.activation.data.ActivationError;
import com.ivy.utils.rx.SchedulerProvider;
import com.ivy.ui.activation.ActivationContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class ActivationPresenterImpl<V extends ActivationContract.ActivationView> extends BasePresenter<V> implements ActivationContract.ActivationPresenter<V> {

    private ActivationDataManager activationDataManager;

    private DataManager dataManager;

    private List<ActivationBO> appUrls;

    public String SERVER_URL;

    @Inject
    public ActivationPresenterImpl(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable, ActivationDataManager activationDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable);
        this.activationDataManager = activationDataManager;
        this.dataManager = dataManager;
    }


    @Override
    public void validateActivationKey(String activationKey, String applicationVersionName, String applicationVersionNumber, String ieMiNumber) {
        if (activationKey.length() <= 0) {
            getIvyView().showActivationEmptyError();
        } else if (activationKey.length() != 16) {
            getIvyView().showInvalidActivationError();
        } else {
            doActivation(activationKey, applicationVersionName, applicationVersionNumber, ieMiNumber);
        }
    }

    private void doActivation(String key, String applicationVersionName, String applicationVersionNumber, String imEiNumber) {

        //   getIvyView().showLoading();
        getCompositeDisposable().add((Disposable) activationDataManager.doActivationAtHttp(key, applicationVersionName,
                applicationVersionNumber, imEiNumber)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getObserver()));


    }

    private DisposableObserver<JSONObject> getObserver() {
        getIvyView().hideLoading();
        return new DisposableObserver<JSONObject>() {
            @Override
            public void onNext(JSONObject jsonObject) {
                doActionBasedOnActivationResult(jsonObject);
            }

            @Override
            public void onError(Throwable e) {
                ActivationError activationError = (ActivationError) e;
                handleError(activationError);
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void doActionBasedOnActivationResult(JSONObject jsonObj) {

        if (jsonObj == null)
            return;

        try {
            JSONArray jsonArray = (JSONArray) jsonObj.get("Table");
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            if (jsonObject.getString("SyncServiceURL").isEmpty())
                getIvyView().showAppUrlIsEmptyError();
            else {
                setResponseInPreference(jsonObj);
                checkServerStatus(jsonObject.getString("SyncServiceURL"));
            }
        } catch (JSONException e) {
            Commons.printException(e);
            getIvyView().showJsonExceptionError();
        } catch (Exception e) {
            Commons.printException(e);
            getIvyView().showServerError();
        }
    }

    private void setResponseInPreference(JSONObject jsonObj) {

    }


    @Override
    public void triggerIMEIActivation(String imEi, String versionName, String versionNumber) {
        getCompositeDisposable().add((Disposable) activationDataManager.doIMEIActivationAtHttp(imEi, versionName,
                versionNumber)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getImEiObserver()));

    }

    private Observer getImEiObserver() {
        getIvyView().hideLoading();
        return new DisposableObserver<JSONObject>() {
            @Override
            public void onNext(JSONObject jsonObject) {
                doActionBasedOnImEiActivationResult(jsonObject);
            }

            @Override
            public void onError(Throwable e) {
                ActivationError activationError = (ActivationError) e;
                handleError(activationError);
                getIvyView().showActivationError(activationError);
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private void handleError(ActivationError activationError) {
        if (activationError.getStatus() == DataMembers.IVY_CODE_CUSTOM) {
            getIvyView().showTryValidKeyError();
            // int downloadReponse = SDUtil.convertToInt(e.getMessage());
        } else if ((activationError).getStatus() == DataMembers.IVY_CODE_EXCEPTION) {
            int downloadReponse = SDUtil.convertToInt(activationError.getMessage());
        } else
            //2--->
            getIvyView().showActivationError(activationError);

    }

    /*
    *  7 -----> NOTIFY_ACTIVATION_LIST
    *
    *  8 -----> NOTIFY_ACTIVATION_LIST_SINGLE
    *
    *  9 -----> NOTIFY_ACTIVATION_LIST_NULL
    *
    * 10 -----> NOTIFY_URL_EMPTY
    *
    * */

    private void doActionBasedOnImEiActivationResult(JSONObject jsonObj) {
        Commons.printInformation("Activation" + "onSucess Response"
                + jsonObj.toString());
        JSONObject jsonObject;
        try {
            JSONArray jsonArray = (JSONArray) jsonObj.get("Table");
            if (jsonArray == null || jsonArray.length() == 0) {
                // ---> 9
                getIvyView().showPreviousActivationError();

            } else {
                if (jsonArray.length() == 1) {
                    jsonObject = (JSONObject) jsonArray.get(0);
                    if (jsonObject.getString("SyncServiceURL").isEmpty())
                        // ---> 10;
                        getIvyView().showAppUrlIsEmptyError();
                    else {
                        setValueToPreference(jsonObject.getString("SyncServiceURL").replace(" ", ""),
                                jsonObject.getString("ApplicationName"));
                        // ----> 8
                        doActionThree3();
                    }
                } else {
                    if (getAppUrls() == null)
                        setAppUrls(new ArrayList<ActivationBO>());
                    else
                        getAppUrls().clear();
                    int size = jsonArray.length();
                    for (int i = 0; i < size; i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        ActivationBO bo = new ActivationBO();
                        bo.setUrl(jsonObject
                                .getString("SyncServiceURL").replace(" ", ""));
                        bo.setEnviroinment(jsonObject
                                .getString("ApplicationName"));
                        getAppUrls().add(bo);
                    }

                    // ---->7
                    showActivationDialog();
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
            getIvyView().showServerError();
        }

    }

    private void setValueToPreference(String url, String appName) {
        dataManager.setBaseUrl(url);
        dataManager.setApplicationName(appName);

    }

    private void showActivationDialog() {

        if (getAppUrls() == null || getAppUrls().size() == 0) {
            clearAppUrl();
            getIvyView().showPreviousActivationError();
        } else {
            getIvyView().showActivationDialog();
        }
    }

    private void clearAppUrl() {
        dataManager.setBaseUrl("");
        dataManager.setApplicationName("");
        dataManager.setActivationKey("");
    }

    private void doActionThree3() {
        // ---> 8
        String appUrl = dataManager.getBaseUrl();
        setSERVER_URL(appUrl);
        checkServerStatus(appUrl);

    }


    @Override
    public void checkServerStatus(String url) {
        getCompositeDisposable().add(activationDataManager.isServerOnline(url)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) throws Exception {
                        if (response)
                            getIvyView().navigateToLoginScreen();
                        else
                            getIvyView().showInvalidUrlError();
                    }
                }));
    }

    @Override
    public void doActionForActivationDismiss() {
        Context context = null;
        // String appUrl = appPreferences.getString("appUrlNew", "");
        String appUrl = "";
        if (appUrl.isEmpty()) {
            clearAppUrl();
            getIvyView().showAppUrlIsEmptyError();

        } else {
            setSERVER_URL(appUrl);
            checkServerStatus(appUrl);
        }

    }


    public String getSERVER_URL() {
        return SERVER_URL;
    }

    public void setSERVER_URL(String SERVER_URL) {
        this.SERVER_URL = SERVER_URL;
    }

    public List<ActivationBO> getAppUrls() {
        return appUrls;
    }

    public void setAppUrls(List<ActivationBO> appUrls) {
        this.appUrls = appUrls;
    }
}
