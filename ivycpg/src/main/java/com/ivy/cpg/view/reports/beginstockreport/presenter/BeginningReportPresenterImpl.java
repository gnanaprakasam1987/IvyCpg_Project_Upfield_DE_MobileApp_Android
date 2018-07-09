package com.ivy.cpg.view.reports.beginstockreport.presenter;


import android.content.Context;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.reports.beginstockreport.BeginningReportContract;
import com.ivy.cpg.view.reports.beginstockreport.data.BeginningReportManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;


import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class BeginningReportPresenterImpl<V extends BeginningReportContract.IBeginningStockView> extends BasePresenter<V>
        implements BeginningReportContract.IBeginningStockModelPresenter<V> {


    private BeginningReportManager beginningReportManager;
    private ConfigurationMasterHelper configurationMasterHelper;

    private LabelsMasterHelper labelsMasterHelper;

    @Inject
    UserMasterHelper userMasterHelper;


    @Inject
    public BeginningReportPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                        CompositeDisposable compositeDisposable,
                                        ConfigurationMasterHelper configurationMasterHelper,
                                        BeginningReportManager beginningReportManager, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.beginningReportManager = beginningReportManager;
        this.configurationMasterHelper = configurationMasterHelper;
    }


    public LabelsMasterHelper getLabelsMasterHelper() {
        return labelsMasterHelper;
    }

    public void setLabelsMasterHelper(LabelsMasterHelper labelsMasterHelper) {
        this.labelsMasterHelper = labelsMasterHelper;
        labelsMasterHelper.downloadLabelsMaster();
    }

    @Override
    public void setCaseAndPieceTitle(Object caseTag, Object pieceTag) {
        if ((labelsMasterHelper.applyLabels(caseTag)) != null)
            getIvyView().setCaseTitle(labelsMasterHelper.applyLabels(caseTag));

        if ((labelsMasterHelper.applyLabels(pieceTag)) != null)
            getIvyView().setPieceTitle(labelsMasterHelper.applyLabels(pieceTag));


    }

    @Override
    public void showTotalTitle(Object tag) {
        if (labelsMasterHelper.applyLabels(tag) != null)
            getIvyView().setTotalTitle(labelsMasterHelper.applyLabels(tag));
    }

    @Override
    public void setUserMasterHelper(UserMasterHelper userMasterHelper) {
        this.userMasterHelper = userMasterHelper;
        userMasterHelper.downloadUserDetails();
    }

    @Override
    public void checkUserId() {
        if(userMasterHelper.getUserMasterBO().getUserid() == 0)
            getIvyView().finishActivity();
    }


    public void downloadBeginningStock(Context context) {

        getIvyView().showLoading(R.string.please_wait_some_time);
        getCompositeDisposable().add((Disposable) beginningReportManager.downloadBeginningStock(context)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getObserver()));
    }



    private DisposableObserver<Vector<StockReportMasterBO>> getObserver() {
        return new DisposableObserver<Vector<StockReportMasterBO>>() {
            @Override
            public void onNext(Vector<StockReportMasterBO> stockReportMasterBOS) {
                getIvyView().hideLoading();
                getIvyView().setAdapter(stockReportMasterBOS, configurationMasterHelper);
            }

            @Override
            public void onError(Throwable e) {
                getIvyView().hideLoading();
                getIvyView().showError();
            }

            @Override
            public void onComplete() {

            }
        };
    }


    @Override
    public void showCaseOrder(Object caseTag) {
        if (!configurationMasterHelper.SHOW_ORDER_CASE)
            getIvyView().hideCaseTitle();
        else
            getIvyView().setCaseTitle(labelsMasterHelper.applyLabels(caseTag));
    }

    @Override
    public void showPieceOrder(Object pieceTag) {
        if (!configurationMasterHelper.SHOW_ORDER_PCS)
            getIvyView().hidePieceTitle();
        else
            getIvyView().setPieceTitle(labelsMasterHelper.applyLabels(pieceTag));
    }

    @Override
    public boolean ifShowCase() {
        return configurationMasterHelper.SHOW_OUTER_CASE;
    }
}
