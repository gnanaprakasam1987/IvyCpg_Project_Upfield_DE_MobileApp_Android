package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.utils.DateTimeUtils;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.ivy.ui.retailer.RetailerConstants.CANCELLED;
import static com.ivy.ui.retailer.RetailerConstants.DELETED;
import static com.ivy.ui.retailer.RetailerConstants.RESCHEDULED;
import static com.ivy.utils.StringUtils.getStringQueryParam;

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
                            + getStringQueryParam(dateWisePlanBo.getDate()) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                            + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence() + ","
                            + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                            + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                            + getStringQueryParam("PLANNED") + ","
                            + getStringQueryParam("MOBILE") + ","
                            + getStringQueryParam("APPROVED") + ","
                            + getStringQueryParam("N") + ","
                            + (dateWisePlanBo.isAdhoc() ? 1 : 0);

                    String columns = DataMembers.tbl_date_wise_plan_cols + ",upload,isAdhocPlan";

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);

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
    public Observable<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo, String reasonId, long planID) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus =  "+getStringQueryParam(RESCHEDULED)
                            + " where PlanId = " + dateWisePlanBo.getPlanId());

                    String values = planID + ","
                            + dateWisePlanBo.getDistributorId() + ","
                            + dateWisePlanBo.getUserId() + ","
                            + getStringQueryParam(dateWisePlanBo.getDate()) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                            + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence() + ","
                            + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                            + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                            + getStringQueryParam("PLANNED") + ","
                            + getStringQueryParam("MOBILE") + ","
                            + getStringQueryParam("APPROVED") + ","
                            + getStringQueryParam("N");

                    String columns = DataMembers.tbl_date_wise_plan_cols + ",upload";

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);

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
    public Observable<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo, String reasonId) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();
                try {
                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus = "+getStringQueryParam(CANCELLED)
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
    public Observable<DateWisePlanBo> deletePlan(DateWisePlanBo dateWisePlanBo, String reasonId) {
        return Observable.fromCallable(new Callable<DateWisePlanBo>() {
            @Override
            public DateWisePlanBo call() throws Exception {

                initDb();

                try {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus = "+getStringQueryParam(DELETED)
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
    public Single<Boolean> copyPlan(List<DateWisePlanBo> planList, String toDate, int userId) {


        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {
                        String id = userId
                                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                        String values = SDUtil.convertToLong(id) + ","
                                + dateWisePlanBo.getDistributorId() + ","
                                + dateWisePlanBo.getUserId() + ","
                                + getStringQueryParam(toDate) + ","
                                + dateWisePlanBo.getEntityId() + ","
                                + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                                + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                                + dateWisePlanBo.getSequence() + ","
                                + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                                + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                                + getStringQueryParam("PLANNED") + ","
                                + getStringQueryParam("MOBILE") + ","
                                + getStringQueryParam("APPROVED") + ","
                                + getStringQueryParam("N");

                        String columns = DataMembers.tbl_date_wise_plan_cols + ",upload";

                        mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);
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
    public Single<Boolean> copyPlan(List<DateWisePlanBo> planList, int userId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {

                        String id = userId
                                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                        String values = SDUtil.convertToLong(id) + ","
                                + dateWisePlanBo.getDistributorId() + ","
                                + dateWisePlanBo.getUserId() + ","
                                + getStringQueryParam(dateWisePlanBo.getDate()) + ","
                                + dateWisePlanBo.getEntityId() + ","
                                + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                                + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                                + dateWisePlanBo.getSequence() + ","
                                + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                                + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                                + getStringQueryParam("PLANNED") + ","
                                + getStringQueryParam("MOBILE") + ","
                                + getStringQueryParam("APPROVED") + ","
                                + getStringQueryParam("N");

                        String columns = DataMembers.tbl_date_wise_plan_cols + ",upload";

                        mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);
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
    public Single<Boolean> cancelPlan(List<DateWisePlanBo> planList, String reasonId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                initDb();

                try {
                    for (DateWisePlanBo dateWisePlanBo : planList) {
                        mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                                + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus = "+getStringQueryParam(CANCELLED)
                                + " where PlanId = " + dateWisePlanBo.getPlanId());
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
    public Single<Boolean> movePlan(List<DateWisePlanBo> planList, String toDate, String reasonId, int userId) {
        return Single.fromCallable(() -> {
            initDb();

            try {

                for (DateWisePlanBo dateWisePlanBo : planList) {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus = "+getStringQueryParam(RESCHEDULED)
                            + " where PlanId = " + dateWisePlanBo.getPlanId());

                    String id = userId
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                    String values = SDUtil.convertToLong(id) + ","
                            + dateWisePlanBo.getDistributorId() + ","
                            + dateWisePlanBo.getUserId() + ","
                            + getStringQueryParam(toDate) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                            + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence() + ","
                            + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                            + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                            + getStringQueryParam("PLANNED") + ","
                            + getStringQueryParam("MOBILE") + ","
                            + getStringQueryParam("APPROVED") + ","
                            + getStringQueryParam("N");

                    String columns = DataMembers.tbl_date_wise_plan_cols + ",upload";

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);
                }

                shutDownDb();
            } catch (Exception e) {
                Commons.printException("" + e);
                shutDownDb();
            }

            return true;
        });
    }

    @Override
    public Single<Boolean> movePlan(List<DateWisePlanBo> fromPlanList, List<DateWisePlanBo> toPlanList, String reasonId, int userId) {
        return Single.fromCallable(() -> {
            initDb();

            try {

                for (DateWisePlanBo dateWisePlanBo : fromPlanList) {

                    mDbUtil.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                            + " SET cancelReasonId= " + getStringQueryParam(reasonId) + ",Status = 'D', VisitStatus =  "+getStringQueryParam(RESCHEDULED)
                            + " where PlanId = " + dateWisePlanBo.getPlanId());
                }

                for (DateWisePlanBo dateWisePlanBo : toPlanList) {

                    String id = userId
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                    String values = SDUtil.convertToLong(id) + ","
                            + dateWisePlanBo.getDistributorId() + ","
                            + dateWisePlanBo.getUserId() + ","
                            + getStringQueryParam(dateWisePlanBo.getDate()) + ","
                            + dateWisePlanBo.getEntityId() + ","
                            + getStringQueryParam(dateWisePlanBo.getEntityType()) + ","
                            + getStringQueryParam(dateWisePlanBo.getStatus()) + ","
                            + dateWisePlanBo.getSequence() + ","
                            + getStringQueryParam(dateWisePlanBo.getStartTime()) + ","
                            + getStringQueryParam(dateWisePlanBo.getEndTime()) + ","
                            + getStringQueryParam("PLANNED") + ","
                            + getStringQueryParam("MOBILE") + ","
                            + getStringQueryParam("APPROVED") + ","
                            + getStringQueryParam("N");

                    String columns = DataMembers.tbl_date_wise_plan_cols + ",upload";

                    mDbUtil.insertSQL(DataMembers.tbl_date_wise_plan, columns, values);
                }

                shutDownDb();
            } catch (Exception e) {
                Commons.printException("" + e);
                shutDownDb();
            }

            return true;
        });

    }
}
