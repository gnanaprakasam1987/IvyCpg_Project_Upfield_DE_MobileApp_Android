package com.ivy.cpg.view.van;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.cpg.view.sync.UploadPresenterImpl;
import com.ivy.cpg.view.van.odameter.OdameterHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.bo.VanLoadMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.NonVisitReasonDialog;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class LoadManagementHelper {

    private static LoadManagementHelper instance = null;
    private Context context;
    private BusinessModel bmodel;
    private ArrayList<SubDepotBo> subDepotList = null;
    private ArrayList<SubDepotBo> distributorList = null;
    private static final String SECURITY_HEADER = "SECURITY_TOKEN_KEY";
    private static final String TAG_JSON_OBJ = "json_obj_req";
    private static final String TAG = "LoadManagementHelper";
    private RequestQueue mRequestQueue;

    public static String TRIP_CONSTANT="TRIP";

    private LoadManagementHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();

    }

    public static LoadManagementHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoadManagementHelper(context);
        }
        return instance;
    }


    /**
     * DownLoad the SubDepots from Distribution Master
     */
    public void downloadSubDepots() {
        SubDepotBo subDepots;
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            db.createDataBase();

            cursor = db.selectSQL("Select DISTINCT Did,Dname,IFNULL(CNumber,''),IFNULL(Address1,''),IFNULL(Address2,''),IFNULL(Address3,''), type from DistributorMaster");
            if (cursor != null) {
                subDepotList = new ArrayList<>();
                distributorList = new ArrayList<>();


                subDepots = new SubDepotBo();
                subDepots.setSubDepotId(0);
                subDepots.setdName("Select Distributor");
                subDepotList.add(subDepots);
                distributorList.add(subDepots);

                while (cursor.moveToNext()) {
                    subDepots = new SubDepotBo();

                    subDepots.setSubDepotId(cursor.getInt(0));

                    subDepots.setContactNumber(cursor.getString(2));
                    subDepots.setAddress1(cursor.getString(3));
                    subDepots.setAddress2(cursor.getString(4));
                    subDepots.setAddress3(cursor.getString(5));

                    String type = cursor.getString(6);
                    if (type != null && type.equalsIgnoreCase("distributor")) {
                        if (bmodel.getAppDataProvider().getUser().getDistributorid() == subDepots.getSubDepotId()) {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Primary");
                        } else {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Secondary");
                        }
                        distributorList.add(subDepots);

                    } else {
                        subDepots.setdName(cursor.getString(1));
                        subDepotList.add(subDepots);
                    }


                }

                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
    }

    public VanLoadMasterBO downloadOdameter() {
        VanLoadMasterBO temp = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT o.uid,o.date,o.start,o.end,o.isended,o.isstarted,o.starttime,o.endtime,IFNULL(o.startImage,''),IFNULL(O.endImage,'') FROM Odameter o");
            if (c != null) {

                while (c.moveToNext()) {
                    temp = new VanLoadMasterBO();
                    temp.setOdameteruid(c.getInt(0));
                    temp.setOdameterdate(c.getString(1));
                    temp.setOdameterstart(c.getDouble(2));
                    temp.setOdameterend(c.getDouble(3));
                    temp.setIsended(c.getInt(4));
                    temp.setIsstarted(c.getInt(5));
                    temp.setStartdatetime(c.getString(6));
                    temp.setEndtime(c.getString(7));

                    if (c.getString(8).length() > 0) {
                        String[] imjObj = c.getString(8).split("/");
                        if (imjObj.length > 3)
                            temp.setStartTripImg(imjObj[3]);
                    }
                    if (c.getString(9).length() > 0) {
                        String[] imjObj = c.getString(9).split("/");
                        if (imjObj.length > 3)
                            temp.setEndTripImg(imjObj[3]);
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return temp;
    }

    public ArrayList<VanLoadMasterBO> downloadExistingUid() {
        DBUtil db = null;
        ArrayList<VanLoadMasterBO> mUidList = new ArrayList<>();
        VanLoadMasterBO vanBo;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor cursor = db
                    .selectSQL("SELECT Distinct Uid FROM VanLoad ORDER BY Uid");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    vanBo = new VanLoadMasterBO();
                    vanBo.setRfield1(cursor.getString(0));
                    mUidList.add(vanBo);
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return mUidList;

    }


    public ArrayList<SubDepotBo> getSubDepotList() {
        return subDepotList;
    }

    public ArrayList<SubDepotBo> getDistributorList() {
        if (distributorList != null) {
            return distributorList;
        }
        return new ArrayList<>();
    }


    public boolean isSecondaryDistributorDone() {
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            db.createDataBase();
            for (SubDepotBo bo : getDistributorList()) {
                if (bmodel.getAppDataProvider().getUser()
                        .getDistributorid() != bo.getSubDepotId()) {
                    cursor = db
                            .selectSQL("Select pid from vanload where subdepotid="
                                    + bo.getSubDepotId());
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            return true;
                        }
                        cursor.close();

                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return false;
    }


    public float checkIsAllowed(String menuString) {
        try {
            DBUtil db = new DBUtil(context,
                    DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT latitude, longitude FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils.sqlEscapeString(menuString));
            double wareLatitude = 0;
            double wareLongitude = 0;
            if (c != null) {
                if (c.moveToNext()) {
                    wareLatitude = c.getDouble(0);
                    wareLongitude = c.getDouble(1);
                }
                c.close();
            }
            db.closeDB();

            if (wareLatitude == 0 && wareLongitude == 0) {
                return -1;
            } else if (LocationUtil.latitude == 0
                    && LocationUtil.longitude == 0) {
                return -2;
            } else {

                float distance = LocationUtil.calculateDistance(wareLatitude,
                        wareLongitude);
                return distance;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return -3;
    }


    Single<String> stockRefresh(final Context mcontext) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(final SingleEmitter<String> e) throws Exception {
                bmodel.synchronizationHelper.updateAuthenticateToken(false);
                if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    try {
                        String downloadurl = getDownloadUrl(mcontext);
                        if (downloadurl.length() > 0) {

                            downloadurl = DataMembers.SERVER_URL + downloadurl;
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                    (Request.Method.POST, downloadurl, getHeaderJson(),
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject jsonObject) {
                                                    Iterator itr = jsonObject.keys();
                                                    while (itr.hasNext()) {
                                                        String key = (String) itr.next();
                                                        if (key.equals("Master")) {
                                                            try {
                                                                if (jsonObject.getString(SynchronizationHelper.ERROR_CODE)
                                                                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                                                                    bmodel.synchronizationHelper.inserVanloadRecodrs(jsonObject);
                                                                    e.onSuccess(jsonObject.getString(SynchronizationHelper.ERROR_CODE));
                                                                }
                                                            } catch (JSONException ex) {
                                                                Commons.printException(ex);
                                                                e.onSuccess("E32");
                                                            }
                                                        } else if (key.equals("ErrorCode")) {
                                                            deleteAllRequestQueue();
                                                            try {
                                                                e.onSuccess(jsonObject.getString(key));
                                                            } catch (JSONException ex) {
                                                                Commons.printException(ex);
                                                                e.onSuccess("E32");
                                                            }
                                                        }
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            deleteAllRequestQueue();
                                            if (error instanceof TimeoutError) {
                                                e.onSuccess("E32");
                                            } else if (error instanceof NoConnectionError) {
                                                e.onSuccess("E06");
                                            } else if (error instanceof ServerError) {
                                                e.onSuccess("E01");
                                            } else if (error instanceof NetworkError) {
                                                e.onSuccess("E01");
                                            } else if (error instanceof ParseError) {
                                                e.onSuccess("E31");
                                            } else {
                                                e.onSuccess("E01");
                                            }

                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put(SECURITY_HEADER, bmodel.synchronizationHelper.getSecurityKey());
                                    headers.put("Content-Type", "application/json; charset=utf-8");

                                    return headers;
                                }
                            };
                            RetryPolicy policy = new DefaultRetryPolicy(
                                    (int) TimeUnit.SECONDS.toMillis(30),
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                            jsonObjectRequest.setRetryPolicy(policy);
                            jsonObjectRequest.setShouldCache(false);

                            addToRequestQueue(jsonObjectRequest);
                        } else
                            e.onSuccess(context.getResources().getString(R.string.download_url_empty));

                    } catch (Exception ex) {
                        Commons.printException(ex);
                        e.onSuccess("E32");
                    }
                } else
                    e.onSuccess(bmodel.synchronizationHelper.getAuthErroCode());

            }
        });
    }

    private <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(TAG_JSON_OBJ) ? TAG : TAG_JSON_OBJ);
        getRequestQueue().add(req);
    }

    private RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
            mRequestQueue.getCache().clear();

        }
        return mRequestQueue;

    }

    private void deleteAllRequestQueue() {
        mRequestQueue.cancelAll(TAG_JSON_OBJ);

    }

    private String getDownloadUrl(Context mContext) {
        String url = "";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            db.createDataBase();
            Cursor c = db.selectSQL("select url from urldownloadmaster where mastername='VANLOAD'");
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        url = c.getString(0);
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
        return url;
    }

    private JSONObject getHeaderJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("UserId", bmodel.getAppDataProvider().getUser()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            json.put("MobileDateTime", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!DataMembers.backDate.isEmpty())
                json.put("RequestDate",
                        DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
        } catch (Exception e) {
            Commons.printException(e);

        }

        return json;
    }

    public boolean isTripStarted(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            db.createDataBase();
            Cursor c = db.selectSQL("select startDate from TripMaster where userId="+bmodel.getAppDataProvider().getUser().getUserid());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {

                        String startDate=c.getString(0);
                        if(startDate!=null&&!startDate.equals(""))
                            return true;
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }

        return false;
    }

    public String getTripStartedDate(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            db.createDataBase();
            Cursor c = db.selectSQL("select startDate from TripMaster where userId="+bmodel.userMasterHelper.getUserMasterBO().getUserid());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {

                        String startDate=c.getString(0);
                            return startDate;
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }

        return "";
    }


    public String getTripId(){

        String tripId="";

        try {
            SharedPreferences sharedPreferences = AppUtils.getSharedPreferenceByName(bmodel.getApplicationContext(), TRIP_CONSTANT);
            tripId = sharedPreferences.getString("tripId", "");

            if (tripId.equals("")) {

                DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

                db.openDataBase();
                db.createDataBase();
                Cursor c = db.selectSQL("select uid from TripMaster where userId=" + bmodel.getAppDataProvider().getUser().getUserid());
                if (c != null) {
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            tripId = c.getString(0);
                        }
                    }
                }

                if(c!=null)
                c.close();


            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return tripId;

    }

    public boolean isTripEnded(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            db.createDataBase();
            Cursor c = db.selectSQL("select endDate from TripMaster where userId="+bmodel.getAppDataProvider().getUser().getUserid());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {

                        String endDate=c.getString(0);
                        if(endDate!=null&&!endDate.equals(""))
                            return true;
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }

        return false;
    }

    public boolean updateTrip(boolean isTripStart){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        try {
            db.openDataBase();
            db.createDataBase();



            if(isTripStart) {
                String columns="uid,userId,startDate,upload,status,startlatitude,startlongitude";
                String id = bmodel.getAppDataProvider().getUser().getUserid()
                        + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append(StringUtils.QT(id));
                stringBuilder.append(","+bmodel.getAppDataProvider().getUser().getUserid());
                stringBuilder.append(","+StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW)));
                stringBuilder.append(","+StringUtils.QT("N"));
                stringBuilder.append(","+StringUtils.QT("STARTED"));
                stringBuilder.append(","+LocationUtil.latitude);
                stringBuilder.append(","+LocationUtil.latitude);

                db.insertSQL(DataMembers.tbl_TripMaster, columns, stringBuilder.toString());

                SharedPreferences sharedPreferences = AppUtils.getSharedPreferenceByName(bmodel.getApplicationContext(), TRIP_CONSTANT);

                sharedPreferences.edit().putString("tripId",id);
                sharedPreferences.edit().apply();
            }
            else
                db.updateSQL("update "+DataMembers.tbl_TripMaster
                        +" set endDate="+StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                        +","+"upload='N',status='COMPLETED'"
                        +",endlatitude="+LocationUtil.latitude+",endlongitude="+LocationUtil.longitude
                        +" where userId="+bmodel.getAppDataProvider().getUser().getUserid());

        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return false;

    }

    public boolean isAllMandatoryModulesCompleted(Vector<ConfigureBO> menuDB){

        for (int i = 0; i < menuDB.size(); i++) {

            if(menuDB.get(i).getMandatory()==1) {

                if (!bmodel.isModuleCompleted(menuDB.get(i).getConfigCode())) {
                    return false;
                }
                else if (menuDB.get(i).getConfigCode().equals(LoadManagementFragment.MENU_ODAMETER)) {

                    return false;
                }


            }
        }

        return true;
    }

    public boolean isAllMandatoryPlanningSubModulesCompleted(Context context){


        Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                .downloadPlanningSubMenu();

        bmodel.isModuleDone(false);


        try {
            for (int i = 0; i < menuDB.size(); i++) {
                menuDB.get(i).setDone(false);
            }


            for (int i = 0; i < menuDB.size(); i++) {

                if(menuDB.get(i).getMandatory()==1) {

                     if(menuDB.get(i).getConfigCode().equalsIgnoreCase("MENU_ODAMETER")&&!OdameterHelper.getInstance(context).isOdameterStarted(context)){
                        return false;
                    }
                    else if (!bmodel.isModuleCompleted(menuDB.get(i).getConfigCode())) {
                       return false;
                    }




                }
            }


        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return true;
    }

    public boolean isValidTrip(){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(bmodel.getApplicationContext());
        String tripExtendedDate = sharedPreferences.getString("tripExtendedDate", "");
        if(!tripExtendedDate.equals("")&&DateTimeUtils.getDateCount(tripExtendedDate,DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),"yyyy/MM/dd")==0)
            return true;


        String tripStartDate="";
        db.openDataBase();
        db.createDataBase();
        Cursor c = db.selectSQL("select startDate from TripMaster where userId=" + bmodel.getAppDataProvider().getUser().getUserid());
        if (c != null) {
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    tripStartDate = c.getString(0);
                }
            }
        }

        if(c!=null)
            c.close();


        int difference=DateTimeUtils.getDateCount(tripStartDate,bmodel.getAppDataProvider().getUser().getDownloadDate(),"yyyy/MM/dd");

        if(difference>0)
            return false;


        return true;
    }

    private NonVisitReasonDialog nvrd;
    public boolean validateDayClose(Context context, boolean isFromSyncScreen, UploadPresenterImpl presenter, CheckBox dayCloseCheckBox){

        if (bmodel.getAppDataProvider().getPausedRetailer() != null) {
            Toast.makeText(context, R.string.visit_paused_msg, Toast.LENGTH_LONG).show();
            if (dayCloseCheckBox != null)
            dayCloseCheckBox.setChecked(false);
            return false;
        }

        if (bmodel.outletTimeStampHelper
                .isJointCall(bmodel.getAppDataProvider().getUser()
                        .getJoinCallUserList())) {
            bmodel.showAlert(
                    context.getResources().getString(
                            R.string.logout_joint_user_dayclose), 0);
           // dayCloseCheckBox.setChecked(false);
            return false;
        }

        if (DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), bmodel.getAppDataProvider().getUser().getDownloadDate(),
                "yyyy/MM/dd") < 0) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.download_date_mismatch),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (bmodel.configurationMasterHelper.SHOW_CLOSE_DAY_VALID) {
            if (bmodel.configurationMasterHelper.isOdaMeterOn() && !bmodel.endjourneyclicked) {
                bmodel.showAlert(
                        context.getResources()
                                .getString(
                                        R.string.journey_not_ended),
                        0);

                return false;

            }
        }

        if (!UploadHelper.getInstance(context.getApplicationContext()).isAttendanceCompleted(context.getApplicationContext())) {
            bmodel.showAlert(
                    context.getResources()
                            .getString(R.string.attendance_activity_not_completed), 0);
            return false;
        }

        final Vector<NonproductivereasonBO> nonProductiveRetailersVector = bmodel.getMissedCallRetailers();
        if ((nonProductiveRetailersVector.size() != 0 && bmodel.configurationMasterHelper.HAS_NO_VISIT_REASON_VALIDATION)) {


            nvrd = new NonVisitReasonDialog(context, nonProductiveRetailersVector);
            nvrd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (nonProductiveRetailersVector.size() == 0) {

                        if(isFromSyncScreen) {
                            dayCloseCheckBox.setChecked(true);
                            presenter.updateDayCloseStatus(true);
                        }
                    }
                }
            });

            nvrd.show();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = nvrd.getWindow();
            lp.copyFrom(window.getAttributes());
            lp.width = displaymetrics.widthPixels - 50;
            lp.height = (int) (displaymetrics.heightPixels / 1.1);
            window.setAttributes(lp);

            return false;

        }



        return true;
    }

    public boolean isDownloadedDateValid(){

        try {
            int difference = DateTimeUtils.getDateCount(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
            if (difference > 0) {
                return false;
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return true;
    }
}