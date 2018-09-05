package com.ivy.core.data.db;

import com.ivy.core.data.AppDataManagerContract;

import io.reactivex.Single;

public interface DbHelper extends AppDataManagerContract{

    public Single<String> getThemeColor();

    public Single<String> getFontSize();

    Single<Double> getOrderValue();

    Single<Boolean> updateModuleTime(String moduleName);

    Single<Boolean> saveModuleCompletion(String menuName);
}
