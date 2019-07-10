package com.ivy.ui.survey.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

import static com.ivy.utils.StringUtils.getStringQueryParam;

public class SurveyDataManagerImpl implements SurveyDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public SurveyDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if(mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb(){
        mDbUtil.closeDB();
    }

    /**
     *To check if a survey is enabled for a retailer Id
     *
     * @param retailerId for which survey check has to be done
     * @return @true if Survey Available
     */
    @Override
    public Single<Boolean> isSurveyAvailableForRetailer(String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean isavailable = false;
                try {
                    initDb();

                    String sql;
                    Cursor c;

                    sql = "select uid from NewRetailerSurveyResultHeader "
                            + "where retailerid = " + getStringQueryParam(retailerId)
                            + "AND Upload = " + getStringQueryParam("N");
                    c = mDbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        isavailable = true;
                    }
                    c.close();


                } catch (Exception ignored) {
                }
                shutDownDb();
                return isavailable;
            }
        });
    }

    /**
     * To delete the Survey taken for a retailer Id
     * @param retailerId for which the survey has to be deleted
     * @return {@code true} if delete is successful
     */
    @Override
    public Single<Boolean> deleteNewRetailerSurvey(String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean isavailable = false;
                try {
                    initDb();

                    mDbUtil.deleteSQL("NewRetailerSurveyResultDetail", "retailerid ="
                            + getStringQueryParam(retailerId), false);
                    mDbUtil.deleteSQL("NewRetailerSurveyResultHeader", "retailerid ="
                            + getStringQueryParam(retailerId), false);
                    isavailable=true;

                } catch (Exception ignored) {
                }
                shutDownDb();
                return isavailable;
            }
        });
    }
}
