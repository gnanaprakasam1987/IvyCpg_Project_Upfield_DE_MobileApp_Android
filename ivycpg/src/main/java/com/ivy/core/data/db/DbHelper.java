package com.ivy.core.data.db;

import io.reactivex.Single;

public interface DbHelper {

    Single<String> getThemeColor();

    Single<String> getFontSize();
}
