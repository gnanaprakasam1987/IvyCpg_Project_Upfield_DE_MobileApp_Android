package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.DETAIL_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerPerformanceDetailPresenter implements SellerPerformanceDetailContractor.SellerPerformancePresenter{

    private Context context;
    private SellerPerformanceDetailContractor.SellerPerformanceDetailView sellerPerformanceView;
    private SellerBo selectedSeller;
    private ListenerRegistration registration ;
    private int CHART_DAYS = 0;
    private final int CHART_DAYS_COUNT = 4;
    private ArrayList<String> chartDaysStr = new ArrayList<>();
    private ArrayList<Entry> sellerCoveredEntry = new ArrayList<>();
    private ArrayList<Entry> sellerBilledEntry = new ArrayList<>();

    private LinkedHashMap<Integer,ArrayList<RetailerBo>> retailerVisitDetailsByRId = new LinkedHashMap<>();

    private LinkedHashMap<Integer,RetailerBo> retailerMasterHashmap =  new LinkedHashMap<>();
    private BusinessModel bmodel;
    private JSONObject json = new JSONObject();
    private AlertDialog alertDialog;
    private String basePath = "";


    @Override
    public void setDetailView(SellerPerformanceDetailContractor.SellerPerformanceDetailView view, Context context) {
        this.sellerPerformanceView =view;
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
        basePath = AppUtils.getSharedPreferences(context).getString(FIREBASE_ROOT_PATH,"");
    }

    @Override
    public void downloadSellerData(int userId, String date) {

        selectedSeller = new SellerBo();

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId),um.ProfileImagePath from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid where um.userId = '"+userId
                    +"' and date ='"+date+"'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null && c.moveToNext()) {

                selectedSeller.setUserId(c.getInt(0));
                selectedSeller.setUserName(c.getString(1));
                selectedSeller.setTarget(c.getInt(2));
                selectedSeller.setImagePath(c.getString(3));

                c.close();
            }

            setSellerData(selectedSeller);

            sellerPerformanceView.updateSellerPerformanceData(selectedSeller);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void downloadSellerKPI(int userId, String date, boolean isMTD){
        DBUtil db = null;
        try {

            selectedSeller.setTargetCoverage(0);
            selectedSeller.setAchievedCoverage(0);

            selectedSeller.setTargetValue(0);
            selectedSeller.setAchievedValue(0);

            selectedSeller.setTargetLines(0);
            selectedSeller.setAchievedLines(0);

            selectedSeller.setTargetTotalWeight(0);
            selectedSeller.setAchievedTotalWeight(0);

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select SKD.Target,SKD.Achievement,SLM.ListName,SLM.ListCode from SellerKPI SKP " +
                    "inner join SellerKPIDetail SKD on SKD.KPIId =  SKP.KPIId " +
                    "inner join StandardListMaster SLM on SLM.ListId = SKD.KPIParamLovId " +
                    "where SKP.userId = '"+userId+"' and '"+date+"' between fromdate and todate";

            if (isMTD)
                queryStr = queryStr + " and interval= 'MONTH'";
            else
                queryStr = queryStr + " and interval= 'DAY'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null ){
                while (c.moveToNext()) {

                    if(c.getString(3).equals("CM")) {
                        selectedSeller.setTargetCoverage(c.getInt(0));

                        if (isMTD)
                            selectedSeller.setAchievedCoverage(c.getInt(1));
                    }
                    else if(c.getString(3).equals("SV")) {
                        selectedSeller.setTargetValue(c.getLong(0));

                        if (isMTD)
                            selectedSeller.setAchievedValue(c.getLong(1));
                    }
                    else if(c.getString(3).equals("LPC")) {
                        selectedSeller.setTargetLines(c.getInt(0));

                        if (isMTD)
                            selectedSeller.setAchievedLines(c.getInt(1));
                    }
                    else if(c.getString(3).equals("VOL")) {
                        selectedSeller.setTargetTotalWeight(c.getDouble(0));

                        if (isMTD)
                            selectedSeller.setAchievedTotalWeight(c.getDouble(1));
                    }
                }
                c.close();
            }

            sellerPerformanceView.updateSellerTabViewInfo(selectedSeller);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void downloadSellerOutletAWS(int userId,String date) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select retailerId,retailerName,sequence,latitude,longitude,address,imgpath,date from " +
                    "SupRetailerMaster where userId ='" + userId + "' and date ='"+date+"' order by sequence ASC";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {
                    RetailerBo retailerBo = new RetailerBo();
                    retailerBo.setRetailerId(c.getInt(0));
                    retailerBo.setRetailerName(c.getString(1));
                    retailerBo.setMasterSequence(c.getInt(2));
                    retailerBo.setMasterLatitude(c.getDouble(3));
                    retailerBo.setMasterLongitude(c.getDouble(4));
                    retailerBo.setAddress(c.getString(5));
                    retailerBo.setImgPath(c.getString(6));
                    retailerBo.setDate(c.getString(7));

                    retailerMasterHashmap.put(retailerBo.getRetailerId(),retailerBo);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void setSellerActivityListener(final int userId, final String date) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference queryRef = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date).document(userId+"");

        registration = queryRef
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(documentSnapshot != null) {

                            SellerBo documentSnapshotBo = documentSnapshot.toObject((SellerBo.class));

                            if(documentSnapshotBo != null) {
                                selectedSeller.setCovered(documentSnapshotBo.getCovered());
                                selectedSeller.setBilled((documentSnapshotBo.getBilled()));
                                selectedSeller.setTotalOrderValue(documentSnapshotBo.getTotalOrderValue());
                                selectedSeller.setLpc(documentSnapshotBo.getLpc());
                                selectedSeller.setTotallpc(documentSnapshotBo.getTotallpc());
                                selectedSeller.setTotalweight(documentSnapshotBo.getTotalweight());

                                sellerPerformanceView.updateSellerPerformanceData(selectedSeller);

                                sellerPerformanceView.updateSellerTabViewInfo(selectedSeller);
                            }

                            sellerCoveredEntry.clear();
                            sellerBilledEntry.clear();

                            CHART_DAYS = 0;

                            prepareChartData(userId, getPreviousDays(date, -CHART_DAYS_COUNT));

                        }

                    }
                });
    }

    @Override
    public void setSellerActivityDetailListener(int userId,String date) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference queryRef = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date)
                .document(userId + "")
                .collection(DETAIL_PATH);

        registration = queryRef
                .orderBy("inTime", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (snapshot.getType()) {
                                    case ADDED:
                                        setSellerDetailValues(snapshot.getDocument());
                                        break;
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void prepareChartData(final int userId,final String date){

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") ||basePath.equals(""))
            return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference queryRef = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date).document(userId+"");

        queryRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful() && task.getResult() != null) {

                            int covered = 0;
                            int billed = 0;

                            DocumentSnapshot snapshot = task.getResult() ;

                            try {
                                SellerBo sellerBoDocumentSnapshot = snapshot.toObject((SellerBo.class));

                                if (sellerBoDocumentSnapshot != null) {
                                    covered = sellerBoDocumentSnapshot.getCovered();
                                    billed = sellerBoDocumentSnapshot.getBilled();
                                }

                                setChartData(date, covered, billed, userId);
                            }catch(Exception e){
                                Commons.printException(e);
                            }
                        }

                    }
                });
    }

    @Override
    public ArrayList<String> getChartDaysStr() {
        return chartDaysStr;
    }

    @Override
    public ArrayList<Entry> getSellerCoveredEntry(){
        return sellerCoveredEntry;
    }

    @Override
    public ArrayList<Entry> getSellerBilledEntry(){
        return sellerBilledEntry;
    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        try {
            RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

            //System.out.println("setSellerDetailValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            if (documentSnapshotBo != null) {

                //Update retailer info in master list

                RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

                if (retailerMasterBo == null) {
                    retailerMasterHashmap.put(documentSnapshotBo.getRetailerId(), documentSnapshotBo);

                    retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

                    if (retailerMasterBo.getIsDeviated())
                        selectedSeller.setDeviationCount(selectedSeller.getDeviationCount() + 1);
                }

                if (retailerMasterBo != null) {

                    if (retailerMasterBo.getIsOrdered() || documentSnapshotBo.getOrderValue() > 0) {
                        retailerMasterBo.setIsOrdered(true);
                    } else {
                        retailerMasterBo.setIsOrdered(false);
                    }

                    if (!retailerMasterBo.getIsDeviated() && documentSnapshotBo.getIsDeviated()){
                        retailerMasterBo.setIsDeviated(true);
                        selectedSeller.setDeviationCount(selectedSeller.getDeviationCount() + 1);
                    }

                    documentSnapshotBo.setIsOrdered(retailerMasterBo.getIsOrdered());
                    retailerMasterBo.setSkipped(false);
                    retailerMasterBo.setVisited(true);

                    double totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
                    retailerMasterBo.setTotalOrderValue(totalOrderValue);

                    retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
                    retailerMasterBo.setInTime(documentSnapshotBo.getInTime());
                    retailerMasterBo.setOutTime(documentSnapshotBo.getOutTime());

                    selectedSeller.setTotalCallDuration(selectedSeller.getTotalCallDuration()+(retailerMasterBo.getOutTime() - retailerMasterBo.getInTime()));

                    // Set Visited Retailer details in HashMap with retailer id as key

                    RetailerBo retailerBoObj = new RetailerBo();

                    retailerBoObj.setLatitude(documentSnapshotBo.getLatitude());
                    retailerBoObj.setLongitude(documentSnapshotBo.getLongitude());
                    retailerBoObj.setOrderValue(documentSnapshotBo.getOrderValue());
                    retailerBoObj.setIsOrdered(documentSnapshotBo.getIsOrdered());
                    retailerBoObj.setInTime(documentSnapshotBo.getInTime());
                    retailerBoObj.setOutTime(documentSnapshotBo.getOutTime());
                    retailerBoObj.setRetailerId(documentSnapshotBo.getRetailerId());
                    retailerBoObj.setRetailerName(documentSnapshotBo.getRetailerName()!=null?documentSnapshotBo.getRetailerName():"");

                    if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {
                        retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(retailerBoObj);
                    } else {
                        ArrayList<RetailerBo> visitedRetailerList = new ArrayList<>();
                        visitedRetailerList.add(retailerBoObj);
                        retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), visitedRetailerList);
                    }

                    sellerPerformanceView.updateSellerCallInfo(selectedSeller);
                    //ends
                }
            }
        }catch (Exception e){
            Commons.printException(e);
        }
    }

    @Override
    public String calculateDuration(long startTime,long endTime){

        return DateTimeUtils.convertMillisToHMmmSs((endTime-startTime));
    }

    @Override
    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    ArrayList<RetailerBo> getVisitedRetailers(){

        ArrayList<RetailerBo> retailerBos = new ArrayList<>();

        for(Integer id : retailerVisitDetailsByRId.keySet())
            retailerBos.add(retailerMasterHashmap.get(id));

        return retailerBos;
    }

    ArrayList<RetailerBo> getRetailerVisitDetailsByRId(int userId) {
        return retailerVisitDetailsByRId.get(userId);
    }

    private void setSellerData(SellerBo selectedSellerBo){
        this.selectedSeller = selectedSellerBo;
    }

    private void setChartData(String date, int covered, int billed,int userId){

        chartDaysStr.add(convertDateStrShort(date));
        sellerCoveredEntry.add(new Entry( CHART_DAYS,covered));
        sellerBilledEntry.add(new Entry(CHART_DAYS,billed));


        if(CHART_DAYS == CHART_DAYS_COUNT) {

            sellerPerformanceView.updateChartInfo();

            return;
        }

        CHART_DAYS = CHART_DAYS  + 1;

        prepareChartData(userId,getPreviousDays(date , 1));

    }

    private String convertDateStrShort(String dateStr){

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            Date date = sdf.parse(dateStr);
            sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
            dateStr = sdf.format(date);
        }catch(Exception e){
            e.printStackTrace();
        }
        return dateStr;
    }

    String convertPlaneDateToGlobal(String planeDate){
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            Date date = sdf.parse(planeDate);

            sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
            planeDate =sdf.format(date);

            return planeDate;

        }catch(Exception e){
            Commons.printException(e);
        }

        return planeDate;
    }

    private String getPreviousDays(String dateStr, int add){

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
        try {
            Date date = sdf.parse(dateStr);

            cal.setTime(date);
            cal.add(Calendar.DATE, add); //minus number would decrement the days

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sdf.format(cal.getTime());
    }

    void checkDownloadSelerKPIData(int sellerId,String date){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        String kpiParams ="";
        try {
            db.openDataBase();
            db.createDataBase();

            String sb = "select interval from SellerKPI where userid ="+sellerId+" and '"+date+"' between fromdate and todate and interval in('DAY') group by interval";

            Cursor c = db.selectSQL(sb);
            if (c != null  && c.getCount() > 0) {
                c.close();
            }else
                kpiParams = "DAY";


            sb = "select interval from SellerKPI where userid ="+sellerId+" and '"+date+"' between fromdate and todate and interval in('MONTH') group by interval";

            Cursor c1 = db.selectSQL(sb);
            if (c1 != null  && c1.getCount() > 0) {
                c1.close();
            }else{
                if (kpiParams.length() > 0)
                    kpiParams = kpiParams+",MONTH";
                else
                    kpiParams = "MONTH";
            }

            if (kpiParams.length() > 0) {
                prepareJson(sellerId,date,kpiParams);
                new DownloadKPIMaster(date,"SELLERKPI",false).execute();
            }else
                sellerPerformanceView.initializeMethods();

        }catch (Exception e){
            Commons.printException(e);
        }

    }

    class DownloadKPIMaster extends AsyncTask<String, Void, Boolean> {

        private String selectedDate;
        private String masterName;
        private boolean isDetailDownLoad = false;

        DownloadKPIMaster(String seletedDate,String masterName,boolean isDetailDownLoad){
            this.selectedDate = seletedDate;
            this.masterName = masterName;
            this.isDetailDownLoad = isDetailDownLoad;
        }

        protected void onPreExecute() {

            if (alertDialog == null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                customProgressDialog(builder,
                        context.getResources().getString(R.string.progress_dialog_title_downloading));
                alertDialog = builder.create();
                alertDialog.show();

            }

        }

        @Override
        protected Boolean doInBackground(String... params) {

            return NetworkUtils.isNetworkConnected(context) && prepareKPIData(masterName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                if (!isDetailDownLoad)
                    new DownloadKPIMaster(selectedDate,"SELLERKPIDETAIL",true).execute();
                else {
                    alertDialog.dismiss();
                    sellerPerformanceView.initializeMethods();
                }

                //Toast.makeText(context, masterName+" Download Successfull", Toast.LENGTH_SHORT).show();
            }else
                if (alertDialog != null)
                    alertDialog.dismiss();
                //Toast.makeText(context, masterName+" Download Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void prepareJson(int sellerId, String date,String kpiParams){
        try {
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("LoginId", bmodel.userMasterHelper.getUserMasterBO().getLoginName());
            json.put("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(sellerId);
            json.put("UserIds", jsonArray);
            json.put("key", kpiParams);
            json.put(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            json.put("RequestDate",date);
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    private boolean prepareKPIData(String masterName) {

        boolean isSuccess = false;
        try {

            String downloadurl = getDownloadUrl(masterName);

            Commons.print("downloadUrl "+downloadurl);
            Commons.print("json = " + json);

            Vector<String> responseVector = getKPIMasterResponse(json, downloadurl);

            try {
                if (responseVector.size() > 0) {

                    for (String s : responseVector) {

                        JSONObject jsonObject = new JSONObject(s);
                        Iterator itr = jsonObject.keys();
                        while (itr.hasNext()) {

                            String key = (String) itr.next();
                            if (key.equals("Master")) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonObject, false);
                                isSuccess = true;

                            } else if (key.equals("Errorcode")) {
                                String tokenResponse = jsonObject.getString("Errorcode");
                                if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                        || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                        || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                    isSuccess = false;

                                }
                            }
                        }
                    }
                }
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }

        }catch (Exception e){
            Commons.printException(e);
        }

        return isSuccess;

    }

    private Vector<String> getKPIMasterResponse(JSONObject data,
                                                       String appendurl) {
        // Update Security key
        bmodel.synchronizationHelper.updateAuthenticateToken(false);
        StringBuilder url = new StringBuilder();
        url.append(DataMembers.SERVER_URL);
        url.append(appendurl);
        if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
            try {
                MyHttpConnectionNew http = new MyHttpConnectionNew();
                http.create(MyHttpConnectionNew.POST, url.toString(), null);
                http.addHeader("SECURITY_TOKEN_KEY", bmodel.synchronizationHelper.getSecurityKey());
                http.setParamsJsonObject(data);

                http.connectMe();
                Vector<String> result = http.getResult();
                if (result == null) {
                    return new Vector<>();
                }
                return result;
            } catch (Exception e) {
                Commons.printException("" + e);
                return new Vector<>();
            }
        } else {
            return new Vector<>();
        }
    }

    private String getDownloadUrl(String masterName){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            String sb = "select url from urldownloadmaster where " +
                    "mastername='"+masterName+"' and typecode='SYNMAS'";

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0 && c.moveToNext()) {
                    downloadurl = c.getString(0);
                }
            }
        }catch (Exception e){
            Commons.printException(e);
        }

        return downloadurl;
    }

    public void customProgressDialog(AlertDialog.Builder builder, String message) {

        try {
            View view = View.inflate(context, R.layout.custom_alert_dialog, null);

            TextView title = view.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            TextView messagetv = view.findViewById(R.id.text);
            messagetv.setText(message);

            builder.setView(view);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
