package com.ivy.ui.reports.currentreport.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.Context;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.ui.reports.currentreport.ICurrentReportContract;
import com.ivy.ui.reports.currentreport.data.CurrentReportManager;
import com.ivy.sd.png.bo.StockReportBO;
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

    private ProductHelper productHelper;


    @Inject
    public CurrentReportPresenterImpl(DataManager dataManager,
                                      SchedulerProvider schedulerProvider,
                                      CompositeDisposable compositeDisposable,
                                      ConfigurationMasterHelper configurationMasterHelper,
                                      CurrentReportManager currentReportManager,
                                      UserMasterHelper userMasterHelper,
                                      LabelsMasterHelper labelsMasterHelper,
                                      ProductHelper productHelper,
                                      ICurrentReportContract.ICurrentReportView view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, (V) view);
        this.configurationMasterHelper = configurationMasterHelper;
        this.currentReportManager = currentReportManager;
        this.userMasterHelper = userMasterHelper;
        this.labelsMasterHelper = labelsMasterHelper;
        this.productHelper = productHelper;
    }


    @Override
    public void checkUserId() {
        if (userMasterHelper.getUserMasterBO().getUserid() == 0)
            getIvyView().finishActivity();
    }

    @Override
    public void downloadCurrentStockReport() {
        getCompositeDisposable().add((Disposable) currentReportManager.downloadCurrentStockReport(productHelper)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(getDownloadCurrentStockObserver()));
    }

    @Override
    public void updateBaseUOM(Context context, String order, int type) {
        currentReportManager.updateBaseUOM(context, order, type);
    }

    @Override
    public void getSpinnerData() {
        Vector<ChildLevelBo> items = new Vector<>();
        downloadCurrentStockReport();
        getIvyView().setUpBrandSpinner(items);

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
                getIvyView().showError(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
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

    @Override
    public void downLoadUserDetails() {
        userMasterHelper.downloadUserDetails();
        labelsMasterHelper.downloadLabelsMaster();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyView() {
       labelsMasterHelper = null;
       userMasterHelper = null;
       currentReportManager = null;
    }


}
