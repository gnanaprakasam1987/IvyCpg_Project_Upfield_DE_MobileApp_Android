package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.DETAIL_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIRESTORE_BASE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerPerformanceDetailPresenter implements SellerPerformanceDetailContractor.SellerPerformancePresenter{

    private Context context;
    private SellerPerformanceDetailContractor.SellerPerformanceDetailView sellerPerformanceView;
    SellerBo selectedSeller;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration ;
    private int CHART_DAYS = 0;
    private ArrayList<String> chartDaysStr = new ArrayList<>();
    private ArrayList<Entry> sellerCoveredEntry = new ArrayList<>();
    private ArrayList<Entry> sellerBilledEntry = new ArrayList<>();

    private LinkedHashMap<Integer,ArrayList<RetailerBo>> retailerVisitDetailsByRId = new LinkedHashMap<>();

    private LinkedHashMap<Integer,RetailerBo> retailerMasterHashmap =  new LinkedHashMap<>();

    @Override
    public void setDetailView(SellerPerformanceDetailContractor.SellerPerformanceDetailView view, Context context) {
        this.sellerPerformanceView =view;
        this.context = context;
    }

    @Override
    public void downloadSellerData(int userId, String date) {

        selectedSeller = new SellerBo();

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId) from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid where um.userId = '"+userId
                    +"' and date ='"+date+"'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null && c.moveToNext()) {

                selectedSeller.setUserId(c.getInt(0));
                selectedSeller.setUserName(c.getString(1));
                selectedSeller.setTarget(c.getInt(2));

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
    public void setSellerActivityListener(final int userId, final String date) {

        DocumentReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date).document(userId+"");

        registration = queryRef
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if(documentSnapshot != null) {
                            selectedSeller.setCovered((int)(long)documentSnapshot.get("covered"));
                            selectedSeller.setBilled((int)(long)documentSnapshot.get("billed"));
                            selectedSeller.setTotalOrderValue(documentSnapshot.get("totalOrderValue")!=null?(long)documentSnapshot.get("totalOrderValue"):0);
                            selectedSeller.setLpc((documentSnapshot.get("lpc")!=null?(int)(long)documentSnapshot.get("lpc"):0));
                            sellerPerformanceView.updateSellerPerformanceData(selectedSeller);

                            prepareChartData(userId,getPreviousDays(date,-2));

                            sellerPerformanceView.updateSellerTabViewInfo(selectedSeller);

                        }
                    }
                });
    }

    @Override
    public void prepareChartData(final int userId,final String date){

        DocumentReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date).document(userId+"");

        queryRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult() != null) {

                            int covered = 0;
                            int billed = 0;

                            DocumentSnapshot snapshot = task.getResult() ;

                            SellerBo sellerBoDocumentSnapshot = snapshot.toObject((SellerBo.class));

                            if(sellerBoDocumentSnapshot != null) {
                                covered = sellerBoDocumentSnapshot.getCovered();
                                billed = sellerBoDocumentSnapshot.getBilled();
                            }

                            setChartData(date,covered,billed,userId);
                        }

                    }
                });
    }



    private void setChartData(String date, int covered, int billed,int userId){

        chartDaysStr.add(convertDateStrShort(date));
        sellerCoveredEntry.add(new Entry((float) CHART_DAYS,(float)covered));
        sellerBilledEntry.add(new Entry((float) CHART_DAYS,(float)billed));


        if(CHART_DAYS == 2) {

            sellerPerformanceView.updateChartInfo();

            return;
        }

        CHART_DAYS = CHART_DAYS  + 1;

        prepareChartData(userId,getPreviousDays(date , 1));

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

    @Override
    public void downloadSellerKPI(int userId, String date, boolean isMTD){

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,SKD.Target,SKD.Achievement,SLM.ListName from usermaster um " +
                    "inner join SellerKPI SKP on SKP.UserId = um.userid " +
                    "inner join SellerKPIDetail SKD on SKD.KPIId =  SKP.KPIId " +
                    "inner join StandardListMaster SLM on SLM.ListId = SKD.KPIParamLovId " +
                    "where um.userId = '"+userId+"'";

            if (isMTD)
                queryStr = queryStr + "and interval= 'MONTH'";
            else
                queryStr = queryStr + "and interval= 'DAY'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null ){
                while (c.moveToNext()) {

                    if(c.getString(4).contains("Coverage")) {
                        selectedSeller.setTargetCoverage(c.getInt(2));

                        if (isMTD)
                            selectedSeller.setAchievedCoverage(c.getInt(3));
                    }
                    else if(c.getString(4).contains("orderValue")) {
                        selectedSeller.setTargetValue(c.getInt(2));

                        if (isMTD)
                            selectedSeller.setAchievedValue(c.getInt(3));
                    }
                    else if(c.getString(4).contains("lines")) {
                        selectedSeller.setTargetLines(c.getInt(2));

                        if (isMTD)
                            selectedSeller.setAchievedLines(c.getInt(3));
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
    public void downloadSellerOutletAWS(int userId) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select retailerId,retailerName,sequence,latitude,longitude,address,imgpath,date from " +
                    "SupRetailerMaster where userId ='" + userId + "' order by sequence ASC";

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
    public void setSellerActivityDetailListener(int userId,String date) {

        CollectionReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date)
                .document(userId + "")
                .collection(DETAIL_PATH);

        registration = queryRef
                .orderBy("timeIn", Query.Direction.ASCENDING)
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


    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

        System.out.println("setSellerDetailValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if (documentSnapshotBo != null) {

            LatLng destLatLng = new LatLng(documentSnapshotBo.getLatitude(), documentSnapshotBo.getLongitude());

            //Update retailer info in master list

            RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

            if(retailerMasterBo != null) {


                if (retailerMasterBo.getIsOrdered() || documentSnapshotBo.getOrderValue() > 0) {
                    retailerMasterBo.setIsOrdered(true);
                } else {
                    retailerMasterBo.setIsOrdered(false);
                }

                documentSnapshotBo.setIsOrdered(retailerMasterBo.getIsOrdered());
                retailerMasterBo.setSkipped(false);
                retailerMasterBo.setVisited(true);

                long totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
                retailerMasterBo.setTotalOrderValue(totalOrderValue);

                retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
                retailerMasterBo.setTimeIn(documentSnapshotBo.getTimeIn());
                retailerMasterBo.setTimeOut(documentSnapshotBo.getTimeOut());

                // Set Visited Retailer details in HashMap with retailer id as key

                RetailerBo retailerBoObj = new RetailerBo();

                retailerBoObj.setLatitude(documentSnapshotBo.getLatitude());
                retailerBoObj.setLongitude(documentSnapshotBo.getLongitude());
                retailerBoObj.setOrderValue(documentSnapshotBo.getOrderValue());
                retailerBoObj.setIsOrdered(documentSnapshotBo.getIsOrdered());
                retailerBoObj.setTimeIn(documentSnapshotBo.getTimeIn());
                retailerBoObj.setTimeOut(documentSnapshotBo.getTimeOut());
                retailerBoObj.setRetailerId(documentSnapshotBo.getRetailerId());
                retailerBoObj.setRetailerName(SupervisorActivityHelper.getInstance().retailerNameById(documentSnapshotBo.getRetailerId()));

                if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {
                    retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(retailerBoObj);
                } else {
                    ArrayList<RetailerBo> visitedRetailerList = new ArrayList<>();
                    visitedRetailerList.add(retailerBoObj);
                    retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), visitedRetailerList);
                }

                //ends
            }
        }
    }

    @Override
    public String convertMillisToTime(Long time) {

        if (time != null && time != 0) {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("hh:mm a", Locale.US);
            return format.format(date);
        } else
            return "";
    }

    @Override
    public String calculateDuration(long startTime,long endTime){

        String duratingStr = (String) DateUtils.getRelativeTimeSpanString(startTime, endTime, 0);

        duratingStr = duratingStr.replace("ago","");

        return duratingStr;
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

    public SellerBo getSellerData(){
        return selectedSeller;
    }

    private void setSellerData(SellerBo selectedSellerBo){
        this.selectedSeller = selectedSellerBo;
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
}
