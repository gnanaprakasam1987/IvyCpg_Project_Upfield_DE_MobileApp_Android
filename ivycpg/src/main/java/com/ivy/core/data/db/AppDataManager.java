package com.ivy.core.data.db;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AppDataManager extends AppDataManagerContract{

    public Single<String> getThemeColor();

    public Single<String> getFontSize();

    Single<Double> getOrderValue();

    Single<Boolean> updateModuleTime(String moduleName);

    Single<Boolean> saveModuleCompletion(String menuName);


}
