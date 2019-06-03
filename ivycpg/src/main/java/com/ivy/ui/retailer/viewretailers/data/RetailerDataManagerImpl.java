package com.ivy.ui.retailer.viewretailers.data;

import android.database.Cursor;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.utils.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class RetailerDataManagerImpl implements RetailerDataManager {
    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    RetailerDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
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
    public Single<String> getRoutePath(String url) {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                JSONParser jParser = new JSONParser();
                return jParser.getJSONFromUrl(url);
            }
        });
    }

    @Override
    public Observable<HashMap<String, List<DateWisePlanBo>>> getAllDateRetailerPlanList() {
        return Observable.fromCallable(new Callable<HashMap<String, List<DateWisePlanBo>>>() {
            @Override
            public HashMap<String, List<DateWisePlanBo>> call() throws Exception {
                HashMap<String, List<DateWisePlanBo>> datePlanHashMap = new HashMap<>();

                String sql = "SELECT dwp.PlanId,dwp.DistributorId,dwp.UserId,dwp.Date,dwp.EntityId,dwp.EntityType,IFNULL(dwp.Status,'')" +
                        ",dwp.Sequence,rm.RetailerName,IFNULL(dwp.StartTime,''),IFNULL(dwp.EndTime,''),IFNULL(dwp.PlanSource,'')," +
                        " IFNULL(dwp.VisitStatus,''),cancelReasonId " +
                        " FROM " + DataMembers.tbl_date_wise_plan + " as dwp " +
                        " inner join RetailerMaster as rm on rm.RetailerID = dwp.EntityId " +
                        " LEFT join RetailerAddress as RA on RA.RetailerID = dwp.EntityId AND RA.IsPrimary=1 " +
                        " Where dwp.status != 'D' and dwp.EntityType = 'RETAILER'" +
                        " ORDER BY dwp.Date asc,dwp.StartTime asc";
                try {

                    initDb();

                    Cursor c = mDbUtil.selectSQL(sql);

                    if (c != null && c.getCount() > 0) {
                        DateWisePlanBo dateWisePlanBO;
                        while (c.moveToNext()) {
                            dateWisePlanBO = new DateWisePlanBo();

                            dateWisePlanBO.setPlanId(c.getInt(0));
                            dateWisePlanBO.setDistributorId(c.getInt(1));
                            dateWisePlanBO.setUserId(c.getInt(2));
                             dateWisePlanBO.setDate(c.getString(3));
                            dateWisePlanBO.setEntityId(c.getInt(4));
                            dateWisePlanBO.setEntityType(c.getString(5));
                            dateWisePlanBO.setStatus(c.getString(6));
                            dateWisePlanBO.setSequence(c.getInt(7));
                            dateWisePlanBO.setName(c.getString(8));
                            dateWisePlanBO.setStartTime(c.getString(9));
                            dateWisePlanBO.setEndTime(c.getString(10));

                            if (c.getString(11) != null && c.getString(11).equalsIgnoreCase("MOBILE"))
                                dateWisePlanBO.setServerData(false);
                            else
                                dateWisePlanBO.setServerData(true);

                            dateWisePlanBO.setVisitStatus(c.getString(12));
                            dateWisePlanBO.setCancelReasonId(c.getInt(13));

                            if (datePlanHashMap.get(dateWisePlanBO.getDate()) == null) {
                                ArrayList<DateWisePlanBo> plannedList = new ArrayList<>();
                                plannedList.add(dateWisePlanBO);
                                datePlanHashMap.put(dateWisePlanBO.getDate(), plannedList);
                            } else {
                                List<DateWisePlanBo> plannedList = datePlanHashMap.get(dateWisePlanBO.getDate());
                                plannedList.add(dateWisePlanBO);
                                datePlanHashMap.put(dateWisePlanBO.getDate(), plannedList);
                            }
                        }
                        c.close();
                    }
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return datePlanHashMap;
            }
        });
    }

    @Override
    public Observable<HashMap<String, DateWisePlanBo>> getRetailerPlanList(String date) {
        return Observable.fromCallable(new Callable<HashMap<String, DateWisePlanBo>>() {
            @Override
            public HashMap<String, DateWisePlanBo> call() throws Exception {
                HashMap<String, DateWisePlanBo> datePlanHashMap = new HashMap<>();

                String sql = "SELECT dwp.PlanId,dwp.DistributorId,dwp.UserId,dwp.Date,dwp.EntityId,dwp.EntityType," +
                        "IFNULL(dwp.Status,''),dwp.Sequence,rm.RetailerName,StartTime,EndTime,IFNULL(dwp.PlanSource,'')" +
                        ",IFNULL(dwp.VisitStatus,''),cancelReasonId " +
                        " FROM " + DataMembers.tbl_date_wise_plan + " as dwp " +
                        " inner join RetailerMaster as rm on rm.RetailerID = dwp.EntityId " +
                        " Where dwp.status != 'D' and dwp.EntityType = 'RETAILER' and dwp.Date=" + StringUtils.QT(date);
                try {

                    initDb();

                    Cursor c = mDbUtil.selectSQL(sql);

                    if (c != null && c.getCount() > 0) {
                        DateWisePlanBo dateWisePlanBO;
                        while (c.moveToNext()) {
                            dateWisePlanBO = new DateWisePlanBo();

                            dateWisePlanBO.setPlanId(c.getInt(0));
                            dateWisePlanBO.setDistributorId(c.getInt(1));
                            dateWisePlanBO.setUserId(c.getInt(2));
                            dateWisePlanBO.setDate(c.getString(3));
                            dateWisePlanBO.setEntityId(c.getInt(4));
                            dateWisePlanBO.setEntityType(c.getString(5));
                            dateWisePlanBO.setStatus(c.getString(6));
                            dateWisePlanBO.setSequence(c.getInt(7));
                            dateWisePlanBO.setName(c.getString(8));
                            dateWisePlanBO.setStartTime(c.getString(9));
                            dateWisePlanBO.setEndTime(c.getString(10));

                            if (c.getString(11) != null && !c.getString(11).equalsIgnoreCase("MOBILE"))
                                dateWisePlanBO.setServerData(true);
                            else
                                dateWisePlanBO.setServerData(false);

                            dateWisePlanBO.setVisitStatus(c.getString(12));

                            dateWisePlanBO.setCancelReasonId(c.getInt(13));

                            datePlanHashMap.put(String.valueOf(dateWisePlanBO.getEntityId()), dateWisePlanBO);
                        }
                        c.close();
                    }
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    shutDownDb();
                }

                return datePlanHashMap;
            }
        });
    }

    public class JSONParser {

        InputStream is = null;
        String json = "";

        JSONParser() {
        }

        public String getJSONFromUrl(String url) {
            try {
                URL urlobj = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlobj.openConnection();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = urlConnection.getInputStream();
                }

            } catch (Exception e) {
                Commons.printException(e);
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Commons.printException("Buffer Error," + e);
            }
            return json;

        }
    }

}
