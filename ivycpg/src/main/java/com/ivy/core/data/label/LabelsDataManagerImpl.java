package com.ivy.core.data.label;

import android.database.Cursor;

import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;

import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.ivy.utils.AppUtils.QT;

public class LabelsDataManagerImpl implements LabelsDataManager {

    private DBUtil mDbUtil;

    private SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    public LabelsDataManagerImpl(@DataBaseInfo DBUtil dbUtil, SharedPreferenceHelper sharedPreferenceHelper) {
        this.mDbUtil = dbUtil;
        this.mSharedPreferenceHelper = sharedPreferenceHelper;
        mDbUtil.createDataBase();

        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    @Override
    public Single<String> getLabel(final String key) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {

                    Cursor c = mDbUtil
                            .selectSQL("SELECT value from LabelsMaster where lang = " + QT(mSharedPreferenceHelper.getPreferredLanguage()) + " AND key = " + QT(key));
                    if (c.moveToNext()) {
                        return c.getString(0);
                    }

                } catch (Exception ignored) {
                }
                return "";
            }
        });
    }

    @Override
    public Observable<HashMap<String, String>> getLabels(final String... keyList) {
        return Observable.fromCallable(new Callable<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> call() {
                HashMap<String, String> labelMap = new HashMap<>();
                try {


                    String query = "SELECT value from LabelsMaster where lang = " + QT(mSharedPreferenceHelper.getPreferredLanguage()) + " AND (";

                    for (int i = 0; i < keyList.length; i++) {
                        if (i != keyList.length - 1)
                            query = query + "key = " + QT(keyList[i]) + " OR ";
                        else
                            query = query + "key = " + QT(keyList[i]) + " )";

                    }
                    Cursor c = mDbUtil
                            .selectSQL(query);

                    if (c.moveToNext()) {
                        labelMap.put(c.getString(0), c.getString(1));
                    }

                } catch (Exception ignored) {

                }
                return labelMap;
            }
        });
    }

    @Override
    public Observable<HashMap<String, String>> getAllLabels() {
        return Observable.fromCallable(new Callable<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> call() {
                HashMap<String, String> labelMap = new HashMap<>();
                try {


                    String query = "SELECT value from LabelsMaster where lang = " + QT(mSharedPreferenceHelper.getPreferredLanguage());

                    Cursor c = mDbUtil
                            .selectSQL(query);

                    if (c.moveToNext()) {
                        labelMap.put(c.getString(0), c.getString(1));
                    }

                } catch (Exception ignored) {

                }
                return labelMap;
            }
        });
    }

    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }
}
