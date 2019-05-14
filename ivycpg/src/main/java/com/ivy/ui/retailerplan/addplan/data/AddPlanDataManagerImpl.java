package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.utils.StringUtils;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

public class AddPlanDataManagerImpl implements AddPlanDataManager {

    private DBUtil mDbUtil;

    @Inject
    AddPlanDataManagerImpl(@DataBaseInfo DBUtil dbUtil){
        mDbUtil = dbUtil;
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
    public Single<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {
                    String values = dateWisePlanBo.getPlanId() + ","
                            + dateWisePlanBo.getDistributorId() + ","
                            + dateWisePlanBo.getUserId() + ","
                            + StringUtils.QT(dateWisePlanBo.getDate()) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + StringUtils.QT(dateWisePlanBo.getEntityType()) + ","
                            + StringUtils.QT(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence()+ ","
                            + StringUtils.QT(dateWisePlanBo.getStartTime()) + ","
                            + StringUtils.QT(dateWisePlanBo.getEndTime())+ ","
                            + StringUtils.QT("PLANNED")+ ","
                            + StringUtils.QT("MOBILE");

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);

                    shutDownDb();
                    return dateWisePlanBo;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return dateWisePlanBo;
                }
            }
        });
    }

    @Override
    public Single<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET StartTime = "+StringUtils.QT(dateWisePlanBo.getStartTime())+" , EndTime ="+StringUtils.QT(dateWisePlanBo.getEndTime())
                            +" where EntityId=" + dateWisePlanBo.getEntityId() +" and Date = " + StringUtils.QT(dateWisePlanBo.getDate())
                            + " and EntityType = " + StringUtils.QT(dateWisePlanBo.getEntityType()));
                    shutDownDb();
                    return dateWisePlanBo;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return dateWisePlanBo;
                }
            }
        });
    }

    @Override
    public Single<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {
                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET Status = 'D'"
                            +" where PlanId = "+dateWisePlanBo.getPlanId());
                    shutDownDb();
                    return dateWisePlanBo;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return dateWisePlanBo;
                }
            }
        });
    }

    @Override
    public Single<DateWisePlanBo> DeletePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.deleteSQL(DataMembers.tbl_date_wise_plan,
                            " where PlanId = "+dateWisePlanBo.getPlanId(),false);
                    shutDownDb();
                    return dateWisePlanBo;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return dateWisePlanBo;
                }
            }
        });
    }
}
