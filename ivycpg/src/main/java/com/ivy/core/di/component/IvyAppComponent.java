package com.ivy.core.di.component;

import android.app.Application;
import android.content.Context;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.module.ActivityModule;
import com.ivy.core.di.module.IvyAppModule;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.ui.profile.edit.di.Profile;

import java.util.Vector;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {IvyAppModule.class, ActivityModule.class})
public interface IvyAppComponent {


    @ApplicationContext
    Context context();

    DataManager dataManager();

    @DataBaseInfo
    DBUtil getDBUtil();

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
    Vector<RetailerMasterBO> getRetailerMaster();


    ConfigurationMasterHelper configurationMasterHelper();

    void inject(BusinessModel businessModel);

    void inject(BaseFragment baseFragment);

}
