package com.ivy.ui.retailer.viewretailers.data;

import android.database.Cursor;

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
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

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
    public Single<HashMap<String, ArrayList<DateWisePlanBo>>> getAllDateRetailerPlanList() {
        return Single.fromCallable(new Callable<HashMap<String, ArrayList<DateWisePlanBo>>>() {
            @Override
            public HashMap<String, ArrayList<DateWisePlanBo>> call() throws Exception {
                HashMap<String, ArrayList<DateWisePlanBo>> datePlanHashMap = new HashMap<>();

                String sql = "SELECT dwp.PlanId,dwp.DistributorId,dwp.UserId,dwp.Date,dwp.EntityId,dwp.EntityType,IFNULL(dwp.Status,'')" +
                        ",dwp.Sequence,rm.RetailerName,IFNULL(dwp.StartTime,''),IFNULL(dwp.EndTime,'') " +
                        " FROM " + DataMembers.tbl_date_wise_plan + " as dwp " +
                        " inner join RetailerMaster as rm on rm.RetailerID = dwp.EntityId " +
                        " Where dwp.status != 'D' and dwp.EntityType = 'RETAILER'";

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

                        if (dateWisePlanBO.getStatus() != null && dateWisePlanBO.getStatus().isEmpty())
                            dateWisePlanBO.setServerData(true);

                        if (datePlanHashMap.get(dateWisePlanBO.getDate()) == null) {
                            ArrayList<DateWisePlanBo> plannedList = new ArrayList<>();
                            plannedList.add(dateWisePlanBO);
                            datePlanHashMap.put(dateWisePlanBO.getDate(), plannedList);
                        } else {
                            ArrayList<DateWisePlanBo> plannedList = datePlanHashMap.get(dateWisePlanBO.getDate());
                            plannedList.add(dateWisePlanBO);
                            datePlanHashMap.put(dateWisePlanBO.getDate(), plannedList);
                        }
                    }
                }

                shutDownDb();

                return datePlanHashMap;
            }
        });
    }

    @Override
    public Single<HashMap<String, DateWisePlanBo>> getRetailerPlanList(String date) {
        return Single.fromCallable(new Callable<HashMap<String, DateWisePlanBo>>() {
            @Override
            public HashMap<String, DateWisePlanBo> call() throws Exception {
                HashMap<String, DateWisePlanBo> datePlanHashMap = new HashMap<>();

                String sql = "SELECT dwp.PlanId,dwp.DistributorId,dwp.UserId,dwp.Date,dwp.EntityId,dwp.EntityType,IFNULL(dwp.Status,''),dwp.Sequence,rm.RetailerName,StartTime,EndTime " +
                        " FROM " + DataMembers.tbl_date_wise_plan + " as dwp " +
                        " inner join RetailerMaster as rm on rm.RetailerID = dwp.EntityId " +
                        " Where dwp.status != 'D' and dwp.EntityType = 'RETAILER' and dwp.Date=" + StringUtils.QT(date);

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

                        if (dateWisePlanBO.getStatus() != null && dateWisePlanBO.getStatus().isEmpty())
                            dateWisePlanBO.setServerData(true);

                        datePlanHashMap.put(String.valueOf(dateWisePlanBO.getEntityId()), dateWisePlanBO);
                    }
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
