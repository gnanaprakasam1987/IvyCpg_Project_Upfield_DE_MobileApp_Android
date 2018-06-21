package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class BasePresenter<V extends BaseIvyView> implements BaseIvyPresenter<V> {

    private final DataManager mDataManager;
    private final SchedulerProvider mSchedulerProvider;
    private final CompositeDisposable mCompositeDisposable;


    private V ivyView;

    @Inject
    public BasePresenter(DataManager dataManager, SchedulerProvider schedulerProvider,
                         CompositeDisposable compositeDisposable) {
        this.mDataManager = dataManager;
        this.mSchedulerProvider = schedulerProvider;
        this.mCompositeDisposable = compositeDisposable;


    }


    @Override
    public void onAttach(V mvpView) {
        ivyView = mvpView;
        getIvyView().handleLayoutDirection(mDataManager.getPreferredLanguage());
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        ivyView = null;
    }

    public boolean isViewAttached() {
        return ivyView != null;
    }

    public V getIvyView() {
        return ivyView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new IvyViewNotAttachedException();
    }

    public DataManager getDataManager() {
        return mDataManager;
    }


    public static class IvyViewNotAttachedException extends RuntimeException {
        public IvyViewNotAttachedException() {
            super("Please call Presenter.onAttach(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }


    public SchedulerProvider getSchedulerProvider() {
        return mSchedulerProvider;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }
}
