package com.ivy.core.data.db;

import io.reactivex.Single;

public interface DbHelper {

    public Single<String> getThemeColor();
}
