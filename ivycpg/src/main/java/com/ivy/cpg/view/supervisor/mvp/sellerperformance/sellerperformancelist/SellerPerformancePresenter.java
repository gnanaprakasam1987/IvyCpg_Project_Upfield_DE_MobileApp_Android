package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.BarEntry;
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
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerPerformancePresenter implements SellerPerformanceContractor.SellerPerformancePresenter {

    private SellerPerformanceContractor.SellerPerformanceView sellerPerformanceView;
    private Context context;
    private ListenerRegistration registration ;
    private int CHART_DAYS = 0;
    private final int CHART_DAYS_COUNT = 4;
    private ArrayList<String> chartDaysStr = new ArrayList<>();
    private ArrayList<Entry> sellerCoveredEntry = new ArrayList<>();
    private ArrayList<Entry> sellerBilledEntry = new ArrayList<>();
    private ArrayList<BarEntry> barChartEntry = new ArrayList<>();

    private LinkedHashMap<Integer,SellerBo> sellerInfoHasMap = new LinkedHashMap<>();
    private String basePath="";

    @Override
    public void setView(SellerPerformanceContractor.SellerPerformanceView sellerPerformanceView, Context context) {
        this.sellerPerformanceView = sellerPerformanceView;
        this.context = context;
        basePath = AppUtils.getSharedPreferences(context).getString(FIREBASE_ROOT_PATH,"");
    }

    @Override
    public void getSellerListAWS(String date){

        int totalSellerCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId),um.ProfileImagePath from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid and sm.date ='"+date+"' where isDeviceuser!=1 and userlevel = '"+loadUserLevel()+"' group by um.userid";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {

                    SellerBo sellerBo = new SellerBo();

                    sellerBo.setUserId(c.getInt(0));
                    sellerBo.setUserName(c.getString(1));
                    sellerBo.setTarget(c.getInt(2));
                    sellerBo.setImagePath(c.getString(3));

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

    @Override
    public void sellerActivityInfoListener(final int userId, final String date) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference queryRef = db
                .collection(basePath)
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

                            prepareChartData(userId,getPreviousDays(date, -CHART_DAYS_COUNT));
                        }

                        sellerPerformanceView.updateSellerPerformanceList(new ArrayList<>(sellerInfoHasMap.values()));
                    }
                });
    }

    @Override
    public void prepareChartData(final int userId,final String date){

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference queryRef = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date);

        queryRef
                .whereEqualTo(userId+"",true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful() && task.getResult() != null) {

                            int covered = 0;
                            int billed = 0;

                            for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {

                                try {
                                    SellerBo sellerBoDocumentSnapshot = snapshot.toObject((SellerBo.class));

                                    if (sellerBoDocumentSnapshot != null) {
                                        covered = covered + sellerBoDocumentSnapshot.getCovered();
                                        billed = billed + sellerBoDocumentSnapshot.getBilled();
                                    }
                                }catch(Exception e){
                                    Commons.printException(e);
                                }
                            }

                            setChartData(date,covered,billed,userId);
                        }

                    }
                });
    }

    private void setChartData(String date, int covered, int billed,int userId){

        chartDaysStr.add(convertDateStrShort(date));
        sellerCoveredEntry.add(new Entry(CHART_DAYS,covered));
        sellerBilledEntry.add(new Entry(CHART_DAYS,billed));


        if(CHART_DAYS == CHART_DAYS_COUNT) {

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

    private String loadUserLevel(){

        String code = "";
        try {
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='REALTIME03' and flag = 1";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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

    private void setValues(DocumentSnapshot documentSnapshot){

        try {
            SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

            //System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            if (sellerBoDocumentSnapshot != null && sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId()) != null) {

                SellerBo sellerBoHashmap = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

                sellerBoHashmap.setBilled(sellerBoDocumentSnapshot.getBilled());
                sellerBoHashmap.setCovered(sellerBoDocumentSnapshot.getCovered());

            }
        }catch (Exception e){
            Commons.printException(e);
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

    //0 : A-Z --- 1 : Z-A ---- 2 : Performance
    void sortList(int sortBy,ArrayList<SellerBo> sellerBos){

        Commons.print("sortBy = " + sortBy);

        if(sortBy == 0) {
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {
                    return fstr.getUserName().compareTo(sstr.getUserName());

                }
            });
        }else if(sortBy == 1){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {
                    return sstr.getUserName().compareTo(fstr.getUserName());
                }
            });
        }
        else if(sortBy == 2){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {

                    int target1 = fstr.getTarget();
                    int billed1 = fstr.getBilled();
                    int sellerProductive1 = 0;
                    if (target1 != 0) {
                        sellerProductive1 = (int)((float)billed1 / (float)target1 * 100);
                    }
                    fstr.setProductivityPercent(sellerProductive1);

                    int target2 = sstr.getTarget();
                    int billed2 = sstr.getBilled();
                    int sellerProductive2 = 0;

                    if (target2 != 0) {
                        sellerProductive2 = (int)((float)billed2 / (float)target2 * 100);
                    }

                    sstr.setProductivityPercent(sellerProductive2);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Integer.compare(sstr.getProductivityPercent(),fstr.getProductivityPercent());
                    }else
                        return Integer.valueOf(sstr.getProductivityPercent()).compareTo(fstr.getProductivityPercent());

                }
            });
        }else if(sortBy == 3){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {

                    int target1 = fstr.getTarget();
                    int billed1 = fstr.getBilled();
                    int sellerProductive1 = 0;
                    if (target1 != 0) {
                        sellerProductive1 = (int)((float)billed1 / (float)target1 * 100);
                    }
                    fstr.setProductivityPercent(sellerProductive1);

                    int target2 = sstr.getTarget();
                    int billed2 = sstr.getBilled();
                    int sellerProductive2 = 0;

                    if (target2 != 0) {
                        sellerProductive2 = (int)((float)billed2 / (float)target2 * 100);
                    }

                    sstr.setProductivityPercent(sellerProductive2);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Integer.compare(fstr.getProductivityPercent(),sstr.getProductivityPercent());
                    }else
                        return Integer.valueOf(fstr.getProductivityPercent()).compareTo(sstr.getProductivityPercent());

                }
            });
        }

        sellerPerformanceView.notifyListChange();

    }

    ArrayList<BarEntry> barChartData(){
        return barChartEntry;
    }

    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

}
