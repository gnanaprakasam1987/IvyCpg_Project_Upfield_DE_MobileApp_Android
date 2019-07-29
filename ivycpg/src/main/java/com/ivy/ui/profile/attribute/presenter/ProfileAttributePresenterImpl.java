package com.ivy.ui.profile.attribute.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.data.IProfileAttributeDataManager;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.sd.png.view.ProfileContainerFragment.selectedChannelId;


public class ProfileAttributePresenterImpl<V extends IProfileAttributeContract.IProfileAttributeView>
        extends BasePresenter<V> implements IProfileAttributeContract.IProfileAttributePresenter<V> {

    private IProfileAttributeDataManager attributeDataManager;
    private RetailerMasterBO retailerMasterBO;

    private ArrayList<AttributeBO> commonAttributeList = new ArrayList<>();
    private ArrayList<AttributeBO> channelAttributeList = new ArrayList<>();
    private HashMap<String, ArrayList<AttributeBO>> childAttribute = new HashMap<>();
    private Vector<ConfigureBO> profileConfig = new Vector<>();

    @Inject
    public ProfileAttributePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                         CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper,
                                         V view,
                                         @Profile RetailerMasterBO retailerMasterBO, IProfileAttributeDataManager attributeDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.attributeDataManager = attributeDataManager;
        this.retailerMasterBO = retailerMasterBO;
    }

    public void prepareAttributeList(boolean isProfileEdit) {

        getCompositeDisposable().add(Observable.zip(attributeDataManager.prepareCommonAttributeList(isProfileEdit),
                attributeDataManager.prepareChannelAttributeList(isProfileEdit),
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

                        getIvyView().showChannelAttributeSpinner(getChannelAttributeList());

                        getIvyView().showCommonAttributeSpinner(getCommonAttributeList());

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

    public ArrayList<AttributeBO> getAttributeChildLst(String parentId) {
        return getChildAttribute().get(parentId) != null ? getChildAttribute().get(parentId) : new ArrayList<>();
    }

    public boolean validateAttribute(ArrayList<AttributeBO> selectedAttributeList) {

        try {

            attrParentId = 0;

            // to check all common mandatory attributes selected
            for (AttributeBO commonAttribute : commonAttributeList) {
                if (validateSelectedAttributeList(selectedAttributeList, commonAttribute))
                    return false;
            }

            //to check all mandatory channel's attributes selected
            if (attributeDataManager.isChannelAvailable()) {

                attrParentId = 0;

                try {
                    for (AttributeBO channelAttribute : channelAttributeList) {

                        if (channelAttribute.getChannelId() == selectedChannelId) {
                            if (validateSelectedAttributeList(selectedAttributeList, channelAttribute))
                                return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            getIvyView().hideLoading();
            Commons.printException(e);
        }

        return true;
    }

    private boolean validateSelectedAttributeList(ArrayList<AttributeBO> selectedAttributeList, AttributeBO commonAttribute) {
        boolean isAttributeSelected= false;
        if (commonAttribute.isMandatory()){
            for (AttributeBO selectedAttribute : selectedAttributeList){
                if (selectedAttribute.getParentId().equalsIgnoreCase("0"))
                    attrParentId = selectedAttribute.getAttributeParentId();
                getParentAttributeId(selectedAttribute);
                if (attrParentId == commonAttribute.getAttributeId() &&
                        !selectedAttribute.getStatus().equalsIgnoreCase("D")){
                    isAttributeSelected = true;
                    break;
                }
            }

            if (!isAttributeSelected){
                String errorMessage = commonAttribute.getAttributeName() + " is Mandatory";
                getIvyView().showAlert("Attribute", errorMessage);
                return true;
            }
        }
        return false;
    }

    private int attrParentId = 0;

    private void getParentAttributeId(AttributeBO attributeBO){

        String sId =  attributeBO.getParentId();

        if (!getAttributeChildLst(sId).isEmpty()) {
            if (!getAttributeChildLst(sId).get(0).getParentId().equalsIgnoreCase("0")) {
                getParentAttributeId(getAttributeChildLst(sId).get(0));
            }else {
                System.out.println("Return getParentId() = " + attributeBO.getParentId());
                attrParentId = Integer.parseInt(attributeBO.getParentId());
            }
        }
    }

}
