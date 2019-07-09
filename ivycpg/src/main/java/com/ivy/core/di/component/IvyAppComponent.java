package com.ivy.core.di.component;


import android.content.Context;


import com.android.volley.RequestQueue;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.module.IvyAppModule;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.BeatInfo;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.core.di.scope.DistributorInfo;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.ReasonInfo;
import com.ivy.core.di.scope.RetailerInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.BeatMasterHelper;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.ui.profile.create.di.NewRetailer;
import com.ivy.ui.profile.edit.di.Profile;

import java.util.Vector;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {IvyAppModule.class})
public interface IvyAppComponent {


    @ApplicationContext
    Context context();

    DataManager dataManager();

    @Profile
    RetailerMasterBO retalserMasterBo();

    @Profile
    UserMasterHelper userMasterHelper();

    @Profile
    ChannelMasterHelper channelMasterHelper();

    @Profile
    SubChannelMasterHelper subChannelMasterHelper();

    @Profile
    RetailerHelper retailerHelper();

    @Profile
    NewOutletHelper newoutletHelper();

    @Profile
    Vector<RetailerMasterBO> getRetailerMaster();

    @DistributorInfo
    DistributorDataManager distributorDataManager();

    @ChannelInfo
    ChannelDataManager channelDataManager();

    @NewRetailer
    SurveyHelperNew provideSurveyHelperNew();


    ConfigurationMasterHelper configurationMasterHelper();

    @LabelMasterInfo
    LabelsMasterHelper labelsMasterHelper();

    @DataBaseInfo
    DBUtil dbUtil();

    @RetailerInfo
    RetailerDataManager retailerDataManager();

    @LabelMasterInfo
    LabelsDataManager labelsDataManager();

    AppDataProvider appDataProvider();

    @UserInfo
    UserDataManager userDataManager();

    @OutletTimeStampInfo
    OutletTimeStampDataManager outletTimeStampDataManager();

    BusinessModel provideApplication();

    @BeatInfo
    BeatDataManager beatDataManager();

    @ReasonInfo
    ReasonDataManager reasonDataManager();

    ProductHelper productHelper();

    ProductTaggingHelper productTaggingHelper();

    RequestQueue providesRequestQueue();

    void inject(BusinessModel businessModel);

    void inject(BaseFragment baseFragment);

    void inject(BaseActivity baseActivity);

}
