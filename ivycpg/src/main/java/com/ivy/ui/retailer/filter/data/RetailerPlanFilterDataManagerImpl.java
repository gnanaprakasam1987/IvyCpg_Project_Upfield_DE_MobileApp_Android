package com.ivy.ui.retailer.filter.data;

import android.database.Cursor;
import android.text.TextUtils;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterConstants;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_TASK_DUE_DATE;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.hhtCodeList;

public class RetailerPlanFilterDataManagerImpl implements RetailerPlanFilterDataManager {

    private DBUtil mDbUtil;

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
    public Observable<ArrayList<String>> prepareConfigurationMaster() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> listValues = new ArrayList<>();

//                int size = hhtCodeList.size();

//                String hhtCodes ="("+TextUtils.join(",", Collections.nCopies(size, "?")) + "),";

//                String hhtCodes = TextUtils.join(",", RetailerPlanFilterConstants.hhtCodeList.toArray(new String[RetailerPlanFilterConstants.hhtCodeList.size()])) ;

//                String queryStr = "Select hhtcode from HhtModuleMaster where hhtCode in"+hhtCodes;

                initDb();

//                Cursor c = mDbUtil.selectSQL(queryStr,hhtCodeList.toArray(new String[hhtCodeList.size()]));
//
//                if (c != null&& c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        if (hhtCodeList.contains(c.getString(0))) {
//                            listValues.add(c.getString(0));
//                        }
//                    }
//                }

                listValues.add(CODE_IS_NOT_VISITED);
                listValues.add(CODE_TASK_DUE_DATE);
                listValues.add(CODE_LAST_VISIT_DATE);

                shutDownDb();

                return listValues;
            }
        });
    }

    @Override
    public Observable<ArrayList<AttributeBO>> prepareAttributeList(){
        return Observable.fromCallable(new Callable<ArrayList<AttributeBO>>() {
            @Override
            public ArrayList<AttributeBO> call() throws Exception {
                ArrayList<AttributeBO> mapValues = new ArrayList<>();

                String parentQry = "Select EAM.AttributeId,EAM.AttributeName,EAM.Sequence,EAM.ParentId,EAM.levels from EntityAttributeMaster EAM where EAM.parentId = 0 " +
                        "and isFilterable order by EAM.Sequence,EAM.AttributeId ASC";

                initDb();

                Cursor c = mDbUtil.selectSQL(parentQry);
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {

                        AttributeBO attributeBO = new AttributeBO();
                        attributeBO.setAttributeId(c.getInt(0));
                        attributeBO.setAttributeName(c.getString(1));
                        attributeBO.setLevelCount(c.getInt(4));

                        mapValues.add(attributeBO);

                    }
                }

                shutDownDb();

                return mapValues;
            }
        });
    }

    @Override
    public Observable<HashMap<String, ArrayList<AttributeBO>>> prepareChildAttributeList() {
        return Observable.fromCallable(new Callable<HashMap<String,ArrayList<AttributeBO>>>() {
            @Override
            public HashMap<String,ArrayList<AttributeBO>> call() throws Exception {
                HashMap<String,ArrayList<AttributeBO>> mapValues = new HashMap<>();

                String queryStr = "Select EAM.AttributeId as BaseAId,EAM.AttributeName as BaseName,EAM1.AttributeId,EAM1.AttributeName,EAM.ParentId from EntityAttributeMaster EAM " +
                        "inner join EntityAttributeMaster as EAM1 on EAM.AttributeId = EAM1.ParentId " +
                        "order by EAM1.Sequence,EAM.AttributeId ASC";

                initDb();

                Cursor c = mDbUtil.selectSQL(queryStr);
                if (c != null&& c.getCount() > 0) {
                    while (c.moveToNext()) {

                        AttributeBO attributeBO = new AttributeBO();
                        attributeBO.setAttributeId(c.getInt(2));
                        attributeBO.setAttributeName(c.getString(3));

                        if (mapValues.get(c.getString(0)) == null) {
                            ArrayList<AttributeBO> attributeBOS = new ArrayList<AttributeBO>(){{add(attributeBO);}};
                            mapValues.put(c.getString(0), attributeBOS);
                        }
                        else {
                            ArrayList<AttributeBO> attributeBOVal = mapValues.get(c.getString(0));
                            attributeBOVal.add(attributeBO);
                        }
                    }
                }

                shutDownDb();

                return mapValues;
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

                if (planFilterBo.getIsNotVisited() > 0)
                    queryStrng.append(" inner join OutletTimestamp as ots on ots.RetailerID = rm.retailerId ");


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
