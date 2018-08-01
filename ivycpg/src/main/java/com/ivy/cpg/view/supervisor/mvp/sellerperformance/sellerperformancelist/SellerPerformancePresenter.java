package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.view.supervisor.Seller;
import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIRESTORE_BASE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerPerformancePresenter implements SellerPerformanceContractor.SellerPerformancePresenter {

    private SellerPerformanceContractor.SellerPerformanceView sellerPerformanceView;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration ;
    private int CHART_DAYS = 0;
    private ArrayList<String> chartDaysStr = new ArrayList<>();
    private ArrayList<Entry> sellerCoveredEntry = new ArrayList<>();
    private ArrayList<Entry> sellerBilledEntry = new ArrayList<>();

    private LinkedHashMap<Integer,SellerBo> sellerInfoHasMap = new LinkedHashMap<>();

    @Override
    public void setView(SellerPerformanceContractor.SellerPerformanceView sellerPerformanceView, Context context) {
        this.sellerPerformanceView = sellerPerformanceView;
        this.context = context;
    }

    @Override
    public void getSellerListAWS(){

        int totalSellerCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId) from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid where isDeviceuser!=1 and userlevel = '"+loadUserLevel()+"'  group by um.userid";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {

                    SellerBo sellerBo = new SellerBo();

                    sellerBo.setUserId(c.getInt(0));
                    sellerBo.setUserName(c.getString(1));
                    sellerBo.setTarget(c.getInt(2));

                    if (sellerInfoHasMap.get(sellerBo.getUserId()) == null) {
                        sellerInfoHasMap.put(sellerBo.getUserId(), sellerBo);
                    }

                    totalSellerCount = totalSellerCount + 1;
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

    private String loadUserLevel(){

        String code = "";
        try {
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SUP_USER_LOAD_LEVEL' and flag = 1";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    code = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return code;

    }

    @Override
    public void sellerActivityInfoListener(final int userId, final String date) {

        CollectionReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date);

        registration = queryRef
                .whereEqualTo(userId+"",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots!=null) {

                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (snapshot.getType()) {
                                    case ADDED:
                                        setValues(snapshot.getDocument());
                                        break;
                                    case MODIFIED:
                                        setValues(snapshot.getDocument());
                                        break;
                                }
                            }

                            sellerPerformanceView.updateSellerPerformanceList(new ArrayList<>(sellerInfoHasMap.values()));

                            prepareChartData(userId,getPreviousDays(date, -2));
                        }
                    }
                });
    }

    @Override
    public void prepareChartData(final int userId,final String date){

        CollectionReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date);

        queryRef
                .whereEqualTo(userId+"",true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.getResult() != null) {

                            int covered = 0;
                            int billed = 0;

                            for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {

                                SellerBo sellerBoDocumentSnapshot = snapshot.toObject((SellerBo.class));

                                if(sellerBoDocumentSnapshot != null) {
                                    covered = covered + sellerBoDocumentSnapshot.getCovered();
                                    billed = billed + sellerBoDocumentSnapshot.getBilled();
                                }
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

    private void setValues(DocumentSnapshot documentSnapshot){

        SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

        System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if(sellerBoDocumentSnapshot != null && sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId()) != null) {

            SellerBo sellerBoHashmap = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

            sellerBoHashmap.setBilled(sellerBoDocumentSnapshot.getBilled());
            sellerBoHashmap.setCovered(sellerBoDocumentSnapshot.getCovered());

        }
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

    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

}
