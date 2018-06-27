package com.ivy.ui.activation.presenter;

import com.ivy.core.IvyConstants;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.ActivationBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.ui.activation.data.ActivationError;
import com.ivy.utils.rx.SchedulerProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class ActivationPresenterImpl<V extends ActivationContract.ActivationView> extends BasePresenter<V> implements ActivationContract.ActivationPresenter<V> {

    private static final String SYNC_SERVICE_URL = "SyncServiceURL";
    private static final String TABLE = "Table";
    private static final String APPLICATION_NAME = "ApplicationName";
    private ActivationDataManager activationDataManager;

    private DataManager dataManager;
    private List<ActivationBO> appUrls;

    @Inject
    public ActivationPresenterImpl(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, ActivationDataManager activationDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper);
        this.activationDataManager = activationDataManager;
        this.dataManager = dataManager;
    }


    @Override
    public void validateActivationKey(String activationKey) {
        if (activationKey.length() <= 0) {
            getIvyView().showActivationEmptyError();
        } else if (activationKey.length() != 16) {
            getIvyView().showInvalidActivationError();
        } else {
            getIvyView().doValidationSuccess();
        }
    }

    @Override
    public void doActivation(String key, String applicationVersionName, String applicationVersionNumber, String imEiNumber) {

        getIvyView().showLoading();
        getCompositeDisposable().add((Disposable) activationDataManager.doActivationAtHttp(key, applicationVersionName, applicationVersionNumber, imEiNumber)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getObserver()));


    }

    @Override
    public void triggerIMEIActivation(String imEi, String versionName, String versionNumber) {
        getIvyView().showLoading();
        getCompositeDisposable().add((Disposable) activationDataManager.doIMEIActivationAtHttp(imEi, versionName, versionNumber)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getImEiObserver()));


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
            JSONArray jsonArray = (JSONArray) jsonObj.get(TABLE);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            if (jsonObject.getString(SYNC_SERVICE_URL).isEmpty())
                //---->10
                getIvyView().showAppUrlIsEmptyError();
            else {
                //---->4
                setValueToPreference(jsonObject.getString(SYNC_SERVICE_URL).replace(" ", ""),
                        jsonObject.getString(APPLICATION_NAME));
                // setSERVER_URL(jsonObject.getString(SYNC_SERVICE_URL).replace(" ", ""));
                checkServerStatusBasedOnActivation(jsonObject.getString(SYNC_SERVICE_URL));

            }
        } catch (JSONException e) {
            //--->16
            getIvyView().showJsonExceptionError();
        } catch (Exception e) {
            //---->2
            getIvyView().showServerError();
        }
    }


    private DisposableObserver<JSONObject> getImEiObserver() {
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
            }

            @Override
            public void onComplete() {

            }
        };
    }

    public void handleError(ActivationError activationError) {
        if (activationError.getStatus() == DataMembers.IVY_CODE_CUSTOM) {
            int downloadResponse = SDUtil.convertToInt(activationError.getMessage());
            showValidError(downloadResponse);
        } else if ((activationError).getStatus() == DataMembers.IVY_CODE_EXCEPTION) {
            //int downloadReponse = SDUtil.convertToInt(activationError.getMessage());
            getIvyView().showActivationFailedError();
        } else
            //2--->
            getIvyView().showActivationError(activationError);

    }

    public void showValidError(int messageNumber) {
        switch (messageNumber) {

            //---->15 NOTIFY_URL_NOT_MAPPED_ERROR
            case IvyConstants.NOTIFY_URL_NOT_MAPPED_ERROR:
                clearAppUrl();
                getIvyView().showToastValidKeyContactAdmin();
                break;
            //----> 6 NOTIFY_ACTIVATION_FAILED
            case IvyConstants.NOTIFY_ACTIVATION_FAILED:
                clearAppUrl();
                getIvyView().showActivationFailedError();
                break;
            //----> 5 NOTIFY_INVALID_KEY
            case IvyConstants.NOTIFY_INVALID_KEY:
                clearAppUrl();
                getIvyView().showTryValidKeyError();
                break;
            //----> 2 NOTIFY_SERVER_ERROR
            default:

                getIvyView().showServerError();

        }
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
        //  Commons.printInformation("Activation" + "onSucess Response"
        //   + jsonObj.toString());

        try {
            JSONArray jsonArray = (JSONArray) jsonObj.get(TABLE);
            if (jsonArray == null || jsonArray.length() == 0) {
                // ---> 9
                getIvyView().showPreviousActivationError();

            } else {
                if (jsonArray.length() == 1) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    if (jsonObject.getString(SYNC_SERVICE_URL).isEmpty())
                        // ---> 10;
                        getIvyView().showAppUrlIsEmptyError();
                    else {
                        setValueToPreference(jsonObject.getString(SYNC_SERVICE_URL).replace(" ", ""),
                                jsonObject.getString(APPLICATION_NAME));
                        //setSERVER_URL(jsonObject.getString(SYNC_SERVICE_URL).replace(" ", ""));
                        // ----> 8
                        doActionThree3();
                    }
                } else {
                    if (getAppUrls() == null)
                        setAppUrls(new ArrayList<ActivationBO>());
                    else
                        getAppUrls().clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        ActivationBO bo = new ActivationBO();
                        bo.setUrl(jsonObject
                                .getString(SYNC_SERVICE_URL).replace(" ", ""));
                        bo.setEnviroinment(jsonObject
                                .getString(APPLICATION_NAME));
                        getAppUrls().add(bo);
                    }

                    // ---->7  NOTIFY_ACTIVATION_LIST
                    showActivationDialog(getAppUrls());
                }
            }
        } catch (Exception e) {
            getIvyView().showServerError();
        }

    }

    private void setValueToPreference(String url, String appName) {
        dataManager.setBaseUrl(url);
        dataManager.setApplicationName(appName);

    }

    public void showActivationDialog(List<ActivationBO> appUrls) {

        if (appUrls == null || appUrls.size() == 0) {
            clearAppUrl();
            getIvyView().showPreviousActivationError();
        } else {
            getIvyView().showActivationDialog();
        }
    }

    private void clearAppUrl() {
        dataManager.setBaseUrl(IvyConstants.EMPTY_STRING);
        dataManager.setApplicationName(IvyConstants.EMPTY_STRING);
        dataManager.setActivationKey(IvyConstants.EMPTY_STRING);
    }

    private void doActionThree3() {
        // ---> 8  NOTIFY_ACTIVATION_LIST_SINGLE
        String appUrl = dataManager.getBaseUrl();
        //  if true  ----> 14 NOTIFY_ACTIVATION_LIST_SINGLE_EXTEND  go to login Screen
        //  else   ---->11  NOTIFY_NOT_VALID_URL
        checkServerStatus(appUrl);


    }


    public void checkServerStatusBasedOnActivation(String url) {
        getCompositeDisposable().add(activationDataManager.isServerOnline(url)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) throws Exception {
                        //----> 13 NOTIFY_SUCESSFULLY_ACTIVATED_EXTENDED
                        if (response)
                            getIvyView().showSuccessfullyActivatedAlert();
                        else {
                            //----> 11 NOTIFY_NOT_VALID_URL
                            clearAppUrl();
                            getIvyView().showToastAppUrlConfiguredMessage();
                        }
                    }
                }));

    }

    @Override
    public void checkServerStatus(String url) {
        getCompositeDisposable().add(activationDataManager.isServerOnline(url)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) {
                        if (response)
                            getIvyView().navigateToLoginScreen();
                        else
                            getIvyView().showInvalidUrlError();
                    }
                }));
    }

    @Override
    public void doActionForActivationDismiss() {
        String appUrl = dataManager.getBaseUrl();
        if (appUrl.isEmpty()) {
            clearAppUrl();
            //----->10 NOTIFY_URL_EMPTY
            getIvyView().showAppUrlIsEmptyError();

        } else {
            // setSERVER_URL(appUrl);
            checkServerStatus(appUrl);
        }

    }


    private List<ActivationBO> getAppUrls() {
        return appUrls;
    }

    private void setAppUrls(List<ActivationBO> appUrls) {
        this.appUrls = appUrls;
    }
}
