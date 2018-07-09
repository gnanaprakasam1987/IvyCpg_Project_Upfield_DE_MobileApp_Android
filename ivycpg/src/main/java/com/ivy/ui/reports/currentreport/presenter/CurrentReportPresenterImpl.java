package com.ivy.ui.reports.currentreport.presenter;

import android.content.Context;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.reports.currentreport.ICurrentReportContract;
import com.ivy.ui.reports.currentreport.data.CurrentReportManager;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class CurrentReportPresenterImpl<V extends ICurrentReportContract.ICurrentReportView> extends BasePresenter<V>
        implements ICurrentReportContract.ICurrentReportModelPresenter<V> {

    private ConfigurationMasterHelper configurationMasterHelper;

    private LabelsMasterHelper labelsMasterHelper;

    private UserMasterHelper userMasterHelper;

    private CurrentReportManager currentReportManager;


    @Inject
    public CurrentReportPresenterImpl(DataManager dataManager,
                                      SchedulerProvider schedulerProvider,
                                      CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper,
                                      CurrentReportManager currentReportManager, ICurrentReportContract.ICurrentReportView view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, (V) view);
        this.configurationMasterHelper = configurationMasterHelper;
        this.currentReportManager = currentReportManager;

    }


    @Override
    public void setUserMasterHelper(UserMasterHelper userMasterHelper) {
        this.userMasterHelper = userMasterHelper;
        userMasterHelper.downloadUserDetails();
    }

    @Override
    public void checkUserId() {
        if (userMasterHelper.getUserMasterBO().getUserid() == 0)
            getIvyView().finishActivity();
    }

    @Override
    public void downloadCurrentStockReport(Context context, BusinessModel bModel) {
        getCompositeDisposable().add((Disposable) currentReportManager.downloadCurrentStockReport(context, bModel)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getDownloadCurrentStockObserver()));
    }

    private DisposableObserver<Vector<StockReportBO>> getDownloadCurrentStockObserver() {
        return new DisposableObserver<Vector<StockReportBO>>() {
            @Override
            public void onNext(Vector<StockReportBO> StockReportBO) {
                getIvyView().hideLoading();
                getIvyView().setStockReportBOSList(StockReportBO);
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


    public void setLabelsMasterHelper(LabelsMasterHelper labelsMasterHelper) {
        this.labelsMasterHelper = labelsMasterHelper;
        labelsMasterHelper.downloadLabelsMaster();
    }

    @Override
    public void updateStockReportGrid(int brandId, Vector<StockReportBO> myList) {
        ArrayList<StockReportBO> temp;
        if (myList == null) {
            getIvyView().showNoProductError();
        } else if (brandId == 0) {
            temp = new ArrayList<>(myList);
            getIvyView().setAdapter(temp, configurationMasterHelper);
        } else if (myList.size() > 0) {
            temp = new ArrayList<>();
            for (int i = 0; i < myList.size(); ++i) {
                StockReportBO ret = myList.get(i);
                if (brandId == ret.getBrandId()) {
                    temp.add(ret);
                }
            }
            getIvyView().setAdapter(temp, configurationMasterHelper);
        }
    }

    @Override
    public void setUpTitles() {
        if (configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
            getIvyView().setUpViewsVisible();
        } else {
            getIvyView().hideTitleViews();
        }
    }

    @Override
    public void setSihTitle(Object tag) {
        if (labelsMasterHelper.applyLabels(tag) != null)
            getIvyView().setSihTitle(labelsMasterHelper.applyLabels(tag));
    }

}
