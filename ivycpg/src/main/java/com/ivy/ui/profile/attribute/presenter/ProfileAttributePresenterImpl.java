package com.ivy.ui.profile.attribute.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.data.IProfileAttributeDataManager;
import com.ivy.ui.profile.data.ChannelWiseAttributeList;
import com.ivy.ui.profile.data.IProfileDataManager;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Function5;
import io.reactivex.observers.DisposableObserver;

public class ProfileAttributePresenterImpl<V extends IProfileAttributeContract.IProfileAttributeView>
        extends BasePresenter<V> implements IProfileAttributeContract.IProfileAttributePresenter<V> {

    private IProfileAttributeDataManager attributeDataManager;
    private RetailerMasterBO retailerMasterBO;

    private ArrayList<AttributeBO> commonAttributeList = new ArrayList<>();
    private ArrayList<AttributeBO> channelAttributeList = new ArrayList<>();
    private HashMap<String, ArrayList<AttributeBO>> childAttribute = new HashMap<>();
    @Inject
    public ProfileAttributePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                         CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper,
                                         V view, IProfileDataManager profileDataManager,
                                         @Profile RetailerMasterBO retailerMasterBO, IProfileAttributeDataManager attributeDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.attributeDataManager = attributeDataManager;
        this.retailerMasterBO = retailerMasterBO;
    }

    public void prepareAttributeList() {

        getCompositeDisposable().add(Observable.zip(attributeDataManager.prepareCommonAttributeList(),
                attributeDataManager.prepareChannelAttributeList(),
                attributeDataManager.prepareChildAttributeList(retailerMasterBO.getRetailerID()),
                new Function3<ArrayList<AttributeBO>, ArrayList<AttributeBO>, HashMap<String, ArrayList<AttributeBO>>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<AttributeBO> commonAttrList,
                                         ArrayList<AttributeBO> channelAttrList,
                                         HashMap<String, ArrayList<AttributeBO>> childAttrList) throws Exception {

                        setCommonAttributeList(commonAttrList);
                        setChannelAttributeList(channelAttrList);
                        setChildAttribute(childAttrList);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        getIvyView().displayAttributeSpinner();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                }));

    }

    public ArrayList<AttributeBO> getCommonAttributeList() {
        return commonAttributeList;
    }

    public void setCommonAttributeList(ArrayList<AttributeBO> commonAttributeList) {
        this.commonAttributeList = commonAttributeList;
    }

    public ArrayList<AttributeBO> getChannelAttributeList() {
        return channelAttributeList;
    }

    public void setChannelAttributeList(ArrayList<AttributeBO> channelAttributeList) {
        this.channelAttributeList = channelAttributeList;
    }

    public HashMap<String, ArrayList<AttributeBO>> getChildAttribute() {
        return childAttribute;
    }

    public void setChildAttribute(HashMap<String, ArrayList<AttributeBO>> childAttribute) {
        this.childAttribute = childAttribute;
    }
}
