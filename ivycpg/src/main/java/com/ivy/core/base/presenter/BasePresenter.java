package com.ivy.core.base.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

    public class BasePresenter<V extends BaseIvyView> implements BaseIvyPresenter<V>, LifecycleObserver {

    public static final String RED = "red";
    public static final String ORANGE = "orange";
    public static final String GREEN = "green";
    public static final String PINK = "pink";
    public static final String NBLUE = "nblue";
    private final DataManager mDataManager;
    private final SchedulerProvider mSchedulerProvider;
    private final CompositeDisposable mCompositeDisposable;
    private final ConfigurationMasterHelper mConfigurationMasterHelper;


    private V ivyView;

    @Inject
    public BasePresenter(DataManager dataManager, SchedulerProvider schedulerProvider,
                         CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, V view) {
        this.mDataManager = dataManager;
        this.mSchedulerProvider = schedulerProvider;
        this.mCompositeDisposable = compositeDisposable;
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.ivyView = (V) view;
        if (ivyView instanceof LifecycleOwner) {
            ((LifecycleOwner) ivyView).getLifecycle().addObserver(this);
        }
    }


    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        if (BaseActivity.mCurrentTheme == 0)
            getAppTheme();
        getAppFontSize();
        getIvyView().handleLayoutDirection(mDataManager.getPreferredLanguage());
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (isNFCConfigurationEnabled()) {
            getIvyView().resumeNFCManager();
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDetach() {
        mCompositeDisposable.dispose();
        mDataManager.tearDown();
        if (ivyView instanceof LifecycleOwner) {
            ((LifecycleOwner) ivyView).getLifecycle().removeObserver(this);
        }
        ivyView = null;
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        if (isNFCConfigurationEnabled()) {
            getIvyView().pauseNFCManager();
        }

    }

    public void getAppTheme() {
        getCompositeDisposable().add(getDataManager().getThemeColor()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String theme) {
                        if (theme.equalsIgnoreCase(RED))
                            getIvyView().setRedTheme();
                        else if (theme.equalsIgnoreCase(ORANGE))
                            getIvyView().setOrangeTheme();
                        else if (theme.equalsIgnoreCase(GREEN))
                            getIvyView().setGreenTheme();
                        else if (theme.equalsIgnoreCase(PINK))
                            getIvyView().setPinkTheme();
                        else if (theme.equalsIgnoreCase(NBLUE))
                            getIvyView().setNavyBlueTheme();
                        else
                            getIvyView().setBlueTheme();


                    }
                }));
    }

    public void getAppFontSize() {
        getCompositeDisposable().add(getDataManager().getFontSize()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String fontSize) {
                        getIvyView().setFontSize(fontSize);
                    }
                }));
    }

    public boolean isViewAttached() {
        return ivyView != null;
    }

    /**
     * @return <code>true</code> if RTRS10 Config is turned on
     */
    @Override
    public boolean isNFCConfigurationEnabled() {
        return mConfigurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER;
    }


    /**
     * @return <code>true</code> if FUN14 Config is turned on
     */
    @Override
    public boolean isLocationConfigurationEnabled() {
        return mConfigurationMasterHelper.SHOW_CAPTURED_LOCATION;
    }

    public V getIvyView() {
        return ivyView;
    }


    public DataManager getDataManager() {
        return mDataManager;
    }


    public SchedulerProvider getSchedulerProvider() {
        return mSchedulerProvider;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public ConfigurationMasterHelper getConfigurationMasterHelper() {
        return mConfigurationMasterHelper;
    }
}
