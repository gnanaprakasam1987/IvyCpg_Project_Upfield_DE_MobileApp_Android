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
    public Single<Boolean> savePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

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
                            + StringUtils.QT(dateWisePlanBo.getEndTime());

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);

                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return false;
                }
            }
        });
    }

    @Override
    public Single<Boolean> updatePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                try {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET StartTime = "+dateWisePlanBo.getStartTime()+" , EndTime ="+dateWisePlanBo.getEndTime()
                            +" where EntityId=" + dateWisePlanBo.getEntityId() +" and Date = " + StringUtils.QT(dateWisePlanBo.getDate())
                            + " and EntityType = " + StringUtils.QT(dateWisePlanBo.getEntityType()));
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return false;
                }
            }
        });
    }

    @Override
    public Single<Boolean> cancelPlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                try {
                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET Status = 'D'"
                            +" where PlanId = "+dateWisePlanBo.getPlanId());
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return false;
                }
            }
        });
    }

    @Override
    public Single<Boolean> DeletePlan(DateWisePlanBo dateWisePlanBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                try {

                    mDbUtil.deleteSQL(DataMembers.tbl_date_wise_plan,
                            " where PlanId = "+dateWisePlanBo.getPlanId(),false);
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();

                    return false;
                }
            }
        });
    }
}
