package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;

import javax.inject.Inject;

public class BasePresenter<V extends BaseIvyView> implements BaseIvyPresenter<V> {
    private final DataManager mDataManager;


    private V ivyView;

    @Inject
    public BasePresenter(DataManager dataManager) {
        this.mDataManager = dataManager;
    }


    @Override
    public void onAttach(V mvpView) {
        ivyView = mvpView;
    }

    @Override
    public void onDetach() {
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
}
