package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.utils.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class AddPlanDataManagerImpl implements AddPlanDataManager {

    private DBUtil mDbUtil;

    @Inject
    AddPlanDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
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
    public Observable<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {
                    String values = dateWisePlanBo.getPlanId() + ","
                            + dateWisePlanBo.getDistributorId() + ","
                            + dateWisePlanBo.getUserId() + ","
                            + StringUtils.getStringQueryParam(dateWisePlanBo.getDate()) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                            + StringUtils.getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence() + ","
                            + StringUtils.getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                            + StringUtils.getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                            + StringUtils.getStringQueryParam("PLANNED") + ","
                            + StringUtils.getStringQueryParam("MOBILE") + ","
                            + StringUtils.getStringQueryParam("APPROVED");

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);

                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return dateWisePlanBo;
            }
        });
    }

    @Override
    public Observable<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET StartTime = " + StringUtils.getStringQueryParam(dateWisePlanBo.getStartTime()) + " , EndTime =" + StringUtils.getStringQueryParam(dateWisePlanBo.getEndTime())
                            + " where EntityId=" + dateWisePlanBo.getEntityId() + " and Date = " + StringUtils.getStringQueryParam(dateWisePlanBo.getDate())
                            + " and EntityType = " + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()));
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return dateWisePlanBo;
            }
        });
    }

    @Override
    public Observable<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {
                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET Status = 'D', VisitStatus = 'CANCELLED' "
                            + " where PlanId = " + dateWisePlanBo.getPlanId());
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return dateWisePlanBo;
            }
        });
    }

    @Override
    public Observable<DateWisePlanBo> DeletePlan(DateWisePlanBo dateWisePlanBo) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.deleteSQL(DataMembers.tbl_date_wise_plan,
                            " EntityId=" + dateWisePlanBo.getEntityId() + " and Date = " + StringUtils.getStringQueryParam(dateWisePlanBo.getDate())
                                    + " and EntityType = " + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()), false);
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return dateWisePlanBo;
            }
        });
    }

    @Override
    public Single<Boolean> deletePlan(List<DateWisePlanBo> planList) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {
                        mDbUtil.deleteSQL(DataMembers.tbl_date_wise_plan,
                                " EntityId=" + dateWisePlanBo.getEntityId() + " and Date = " + StringUtils.getStringQueryParam(dateWisePlanBo.getDate())
                                        + " and EntityType = " + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()), false);
                    }
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }
                return true;
            }
        });
    }

    @Override
    public Single<Boolean> copyPlan(List<DateWisePlanBo> planList, String toDate) {


        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {
                        String values = dateWisePlanBo.getPlanId() + ","
                                + dateWisePlanBo.getDistributorId() + ","
                                + dateWisePlanBo.getUserId() + ","
                                + StringUtils.getStringQueryParam(toDate) + ","
                                + dateWisePlanBo.getEntityId() + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                                + dateWisePlanBo.getSequence() + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                                + StringUtils.getStringQueryParam("PLANNED") + ","
                                + StringUtils.getStringQueryParam("MOBILE")+ ","
                                + StringUtils.getStringQueryParam("APPROVED");

                        mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);
                    }

                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return true;
            }
        });
    }

    @Override
    public Single<Boolean> copyPlan(List<DateWisePlanBo> planList) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {
                        String values = dateWisePlanBo.getPlanId() + ","
                                + dateWisePlanBo.getDistributorId() + ","
                                + dateWisePlanBo.getUserId() + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getDate()) + ","
                                + dateWisePlanBo.getEntityId() + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                                + dateWisePlanBo.getSequence() + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                                + StringUtils.getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                                + StringUtils.getStringQueryParam("PLANNED") + ","
                                + StringUtils.getStringQueryParam("MOBILE")+ ","
                                + StringUtils.getStringQueryParam("APPROVED");

                        mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);
                    }

                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return true;
            }
        });
    }
}
