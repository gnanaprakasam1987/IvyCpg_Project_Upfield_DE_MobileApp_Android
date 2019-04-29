package com.ivy.core.di.module;

import dagger.Module;
import dagger.Provides;

import android.content.Context;

import com.ivy.core.IvyConstants;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.beat.BeatDataManagerImpl;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.channel.ChannelDataManagerImpl;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.datamanager.DataManagerImpl;
import com.ivy.core.data.db.AppDataManagerImpl;
import com.ivy.core.data.db.AppDataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.distributor.DistributorDataManagerImpl;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.label.LabelsDataManagerImpl;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.reason.ReasonDataManagerImpl;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.core.data.retailer.RetailerDataManagerImpl;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelperImpl;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.BeatInfo;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.core.di.scope.DistributorInfo;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.PreferenceInfo;
import com.ivy.core.di.scope.ReasonInfo;
import com.ivy.core.di.scope.RetailerInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.profile.edit.di.Profile;

import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Module
public class IvyAppModule {

    private Context mContext;


    @Provides
    @Singleton
    BusinessModel provideApplication() {
        return (BusinessModel) mContext;
    }

    @Inject
    public IvyAppModule(Context context) {
        mContext = context;
    }


    @Provides
    @ApplicationContext
    protected Context providesContext() {
        return mContext;
    }

    @Provides
    @PreferenceInfo
    protected String providesSharedPreferenceName() {
        return IvyConstants.IVY_PREFERENCE_NAME;
    }


    @Provides
    @Singleton
    protected AppDataManager providesDbHelper(AppDataManagerImpl dbHelper) {
        return dbHelper;
    }

    @Provides
    @Singleton
    protected SharedPreferenceHelper providesSharedPreferenceHelper(SharedPreferenceHelperImpl sharedPreferenceHelper) {
        return sharedPreferenceHelper;
    }

    @Provides
    @DataBaseInfo
    protected DBUtil providesDBUtil() {
        return new DBUtil(mContext, DataMembers.DB_NAME);
    }


    @Provides
    @UserInfo
    UserDataManager providesUserDataManager(UserDataManagerImpl userDataManager) {
        return userDataManager;
    }

    @Provides
    @OutletTimeStampInfo
    OutletTimeStampDataManager providesOutletTimeStampManager(OutletTimeStampDataManagerImpl outletTimeStampDataManager) {
        return outletTimeStampDataManager;
    }


    @Provides
    ConfigurationMasterHelper providesConfigurationHelper() {
        return ((BusinessModel) mContext).configurationMasterHelper;
    }

    @Provides
    @LabelMasterInfo
    protected LabelsMasterHelper providesLabelMaster() {
        return ((BusinessModel) mContext).labelsMasterHelper;
    }

    @Provides
    @BeatInfo
    protected BeatDataManager providesBeatDataManager(BeatDataManagerImpl beatDataManager) {
        return beatDataManager;
    }

    @Provides
    @LabelMasterInfo
    protected LabelsDataManager providesLabelDataManager(LabelsDataManagerImpl labelsDataManager) {
        return labelsDataManager;
    }

    @Provides
    @Profile
    protected RetailerMasterBO providesRetailerMasterBO() {
        return ((BusinessModel) mContext).getRetailerMasterBO();
    }

    @Provides
    @Profile
    UserMasterHelper provideUserMasterHelper() {
        return ((BusinessModel) mContext).userMasterHelper;
    }

    @Provides
    @Profile
    ChannelMasterHelper provideChannelMasterHelper() {
        return ((BusinessModel) mContext).channelMasterHelper;
    }

    @Provides
    @Profile
    SubChannelMasterHelper provideSubChannelMasterHelper() {
        return ((BusinessModel) mContext).subChannelMasterHelper;
    }

    @Provides
    @Profile
    RetailerHelper provideRetailerHelper() {
        return ((BusinessModel) mContext).mRetailerHelper;
    }


    @Provides
    @Profile
    Vector<RetailerMasterBO> provideRetailerMaster() {
        return ((BusinessModel) mContext).getRetailerMaster();
    }

    @Provides
    @Profile
    NewOutletHelper provideNewoutletHelper() {
        return ((BusinessModel) mContext).newOutletHelper;
    }

    @Provides
    @Singleton
    protected DataManager providesDataManager(DataManagerImpl dataManager) {
        return dataManager;
    }


    @Singleton
    @Provides
    protected AppDataProvider providesAppData(AppDataProviderImpl appDataProvider) {
        return appDataProvider;
    }

    @Provides
    @DistributorInfo
    DistributorDataManager providesDistributorManager(DistributorDataManagerImpl distributorDataManager) {
        return distributorDataManager;
    }

    @Provides
    @RetailerInfo
    RetailerDataManager providesRetailerDataManager(RetailerDataManagerImpl retailerDataManager) {
        return retailerDataManager;
    }

    @Provides
    @ChannelInfo
    ChannelDataManager providesChannelDataManager(ChannelDataManagerImpl channelDataManager) {
        return channelDataManager;
    }

    @Provides
    @ReasonInfo
    ReasonDataManager provideReasonDataManager(ReasonDataManagerImpl reasonDataManager) {
        return reasonDataManager;
    }
}
