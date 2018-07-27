package com.ivy.core.di.component;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.common.util.DbUtils;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.module.ActivityModule;
import com.ivy.core.di.module.IvyAppModule;
import com.ivy.core.di.scope.ActivityContext;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {IvyAppModule.class})
public interface IvyAppComponent {


    @ApplicationContext
    Context context();

    DataManager dataManager();

    ConfigurationMasterHelper configurationMasterHelper();

    @LabelMasterInfo
    LabelsMasterHelper labelsMasterHelper();

    @DataBaseInfo
    DBUtil dbUtil();


    @LabelMasterInfo
    LabelsDataManager labelsDataManager();

    AppDataProvider appDataProvider();

    @UserInfo
    UserDataManager userDataManager();

    @OutletTimeStampInfo
    OutletTimeStampDataManager outletTimeStampDataManager();

    void inject(BusinessModel businessModel);

    void inject(BaseFragment baseFragment);

}
