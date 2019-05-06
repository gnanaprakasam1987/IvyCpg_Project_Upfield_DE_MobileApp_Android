package com.ivy.ui.filter.data;

import android.database.Cursor;
import android.text.format.DateUtils;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.filter.RetailerPlanFilterBo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_TASK_DUE_DATE;

public class RetailerPlanFilterDataManagerImpl implements RetailerPlanFilterDataManager {

    private DBUtil mDbUtil;
    private ArrayList<String> configurationList ;

    @Inject
    RetailerPlanFilterDataManagerImpl(@DataBaseInfo DBUtil dbUtil){
        this.mDbUtil = dbUtil;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }

    @Override
    public Single<ArrayList<String>> prepareConfigurationMaster() {
        return Single.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> listValues = new ArrayList<>();

                initDb();

                listValues.add(CODE_IS_NOT_VISITED);
                listValues.add(CODE_TASK_DUE_DATE);
                listValues.add(CODE_LAST_VISIT_DATE);

                shutDownDb();

                return listValues;
            }
        });
    }


    @Override
    public Single<ArrayList<String>> getFilterValues(RetailerPlanFilterBo planFilterBo) {
        return Single.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> listValues = new ArrayList<>();
                StringBuilder queryStrng = new StringBuilder();

                queryStrng.append("Select rm.RetailerId from RetailerMaster as rm ");

                if (planFilterBo.getLastVisitDate() != null) {
                    queryStrng.append(" inner join RetailerVisit as rv on rv.RetailerID = rm.retailerId ");
                    queryStrng.append(" And (rv.lastVisitDate BETWEEN '")
                            .append(DateTimeUtils.convertDateTimeObjectToRequestedFormat(planFilterBo.getLastVisitDate().getStringOne(), "dd/MM/yyyy", "yyyy/MM/dd"))
                            .append("' AND '")
                            .append(DateTimeUtils.convertDateTimeObjectToRequestedFormat(planFilterBo.getLastVisitDate().getStringTwo(), "dd/MM/yyyy", "yyyy/MM/dd"))
                            .append("')");
                }

                if (planFilterBo.getTaskDate() != null) {
                    queryStrng.append(" inner join TaskConfigurationMaster as tcm on tcm.retailerId = rm.retailerId ");
                    queryStrng.append(" inner join TaskMaster as tm on tm.taskid = tcm.taskid ");
                    queryStrng.append(" And (tm.DueDate BETWEEN '")
                            .append(DateTimeUtils.convertDateTimeObjectToRequestedFormat(planFilterBo.getTaskDate().getStringOne(), "dd/MM/yyyy", "yyyy/MM/dd"))
                            .append("' AND '")
                            .append(DateTimeUtils.convertDateTimeObjectToRequestedFormat(planFilterBo.getTaskDate().getStringTwo(), "dd/MM/yyyy", "yyyy/MM/dd"))
                            .append("')");
                }

                initDb();

                Cursor c = mDbUtil.selectSQL(queryStrng.toString());
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {
                        listValues.add(c.getString(0));
                    }
                }

                shutDownDb();

                return listValues;
            }
        });
    }
}
