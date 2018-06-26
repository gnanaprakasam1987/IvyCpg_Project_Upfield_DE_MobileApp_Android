package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class BasePresenter<V extends BaseIvyView> implements BaseIvyPresenter<V> {

    private final DataManager mDataManager;
    private final SchedulerProvider mSchedulerProvider;
    private final CompositeDisposable mCompositeDisposable;
    private final ConfigurationMasterHelper mConfigurationMasterHelper;


    private V ivyView;

    @Inject
    public BasePresenter(DataManager dataManager, SchedulerProvider schedulerProvider,
                         CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper) {
        this.mDataManager = dataManager;
        this.mSchedulerProvider = schedulerProvider;
        this.mCompositeDisposable = compositeDisposable;
        this.mConfigurationMasterHelper = configurationMasterHelper;
    }


    @Override
    public void onAttach(V ivyView) {
        this.ivyView = ivyView;
        getIvyView().handleLayoutDirection(mDataManager.getPreferredLanguage());
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        ivyView = null;
    }

    @Override
    public void getAppTheme() {
        getCompositeDisposable().add(getDataManager().getThemeColor()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String theme) throws Exception {
                        if (theme.equalsIgnoreCase("red"))
                            getIvyView().setRedTheme();
                        else if (theme.equalsIgnoreCase("orange"))
                            getIvyView().setOrangeTheme();
                        else if (theme.equalsIgnoreCase("green"))
                            getIvyView().setGreenTheme();
                        else if (theme.equalsIgnoreCase("pink"))
                            getIvyView().setPinkTheme();
                        else if (theme.equalsIgnoreCase("nblue"))
                            getIvyView().setNavyBlueTheme();
                        else
                            getIvyView().setBlueTheme();


                    }
                }));
    }

    @Override
    public void getAppFontSize() {
        getCompositeDisposable().add(getDataManager().getFontSize()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String fontSize) throws Exception {
                        getIvyView().setFontSize(fontSize);
                    }
                }));
    }

    public boolean isViewAttached() {
        return ivyView != null;
    }

    /**
     * @return true if RTRS10 Config is turned on
     */
    @Override
    public boolean getShowNFCValidation() {
        return mConfigurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER;
    }


    /**
     * @return true if FUN14 Config is turned on
     */
    @Override
    public boolean getLocationConfiguration() {
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
}
