package com.ivy.core.data.db;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

public class DBHelperImpl implements DbHelper {

    private DBUtil mDbUtil;


    @Inject
    public DBHelperImpl(@DataBaseInfo DBUtil dbUtil) {
        mDbUtil = dbUtil;
    }

    @Override
    public Single<String> getThemeColor() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String query = "select RField from HhtModuleMaster where hhtcode='THEME01' and flag=1 and  ForSwitchSeller = 0";
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            return c.getString(0);
                        }
                    }
                    mDbUtil.close();
                    mDbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                return "blue";
            }
        });


    }


}
