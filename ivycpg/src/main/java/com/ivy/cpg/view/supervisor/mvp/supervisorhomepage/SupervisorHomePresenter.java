package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class SupervisorHomePresenter implements SupervisorHomeContract.SupervisorHomePresenter{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference queryRef = db
            .collection("activity_tracking_v2");

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, SupervisorModelBo> sellerInfoHasMap = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, MarkerOptions> sellerMarkerHasmap = new HashMap<>();
    private SupervisorHomeContract.SupervisorHomeView supervisorHomeView;
    private Context context;
    private ListenerRegistration registration ;

    private boolean isRealTimeLocationOn = false;

    private int sellerCount = 0;
    private boolean isZoomed = false;

    @Override
    public void setView(SupervisorHomeContract.SupervisorHomeView supervisorHomeView,Context context) {
        this.supervisorHomeView = supervisorHomeView;
        this.context = context;
    }

    @Override
    public void computeSellerInfo() {

        ArrayList<SupervisorModelBo> markerList = new ArrayList<>();

        int totalSellerCount = sellerInfoHasMap.size();
        int marketSellerCount = 0;
        int absentSellerCount;
        long totatlOrderValue = 0L;
        int coveredOutlet = 0;
        int billedOutlet = 0;

        for(SupervisorModelBo supervisorModelBo : sellerInfoHasMap.values()){
            if(supervisorModelBo.isAttendanceDone())
                marketSellerCount = marketSellerCount+1;

            if(supervisorModelBo.getOrderValue() !=null )
                totatlOrderValue = totatlOrderValue + supervisorModelBo.getOrderValue();

            if(sellerMarkerHasmap.get(supervisorModelBo.getUserId()) != null)
                markerList.add(supervisorModelBo);

            coveredOutlet = coveredOutlet + supervisorModelBo.getCovered();
            billedOutlet = billedOutlet + supervisorModelBo.getBilled();

        }

        absentSellerCount = totalSellerCount - marketSellerCount;

        supervisorHomeView.updateSellerAttendance(absentSellerCount,marketSellerCount);
        supervisorHomeView.updateOrderValue((int)totatlOrderValue);
        supervisorHomeView.updateCoveredCount(coveredOutlet);

        int unBilledoutlet = coveredOutlet - billedOutlet;
        supervisorHomeView.updateUnbilledCount(unBilledoutlet);

        supervisorHomeView.setSellerListAdapter(markerList);
    }

    @Override
    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    @Override
    public void loginToFirebase(final Context context) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            String email = SupervisorModuleConstants.FIREBASE_EMAIL;
            String password = SupervisorModuleConstants.FIREBASE_PASSWORD;
            // Authenticate with Firebase and subscribe to updates

            if(email.trim().length() > 0 && password.trim().length() > 0) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            initiateAllMethods();
                        } else {
                            supervisorHomeView.firebaseLoginFailure();
                        }
                    }
                });
            }
        }else{
            initiateAllMethods();
        }
    }

    private void initiateAllMethods(){

        getSellerAttendanceInfoListener();

        getSellerActivityInfoListener();

//        if (isRealTimeLocationOn)
//            realtimeLocationInfoListener();

    }

    @Override
    public void getSellerMarkerInfo(String userId) {

    }

    @Override
    public void getSellerCount(Context context) {

    }

    @Override
    public void getSellerActivityInfoListener() {

        String dateString = "07052018";
        registration = queryRef
                .document("retailer_time_stamp")
                .collection(dateString)
                .whereEqualTo("4",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots!=null) {

                            if (!isRealTimeLocationOn)
                                sellerCount = queryDocumentSnapshots.size();

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
                        }
                    }
                });
    }

    @Override
    public void realtimeLocationInfoListener(){
        registration = queryRef
                .document("movement_tracking")
                .collection("07102018")
                .whereEqualTo("4",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots != null) {

                            sellerCount = queryDocumentSnapshots.size();

                            System.out.println("updateRealtimeLocationInfoListener = " );

                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (snapshot.getType()) {
                                    case ADDED:
                                        setLocationValues(snapshot.getDocument());
                                        break;
                                    case MODIFIED:
                                        setLocationValues(snapshot.getDocument());
                                        break;
                                }
                            }
                        }

                    }
                });
    }

    @Override
    public void getSellerAttendanceInfoListener(){
        registration = queryRef
                .document("Attendance")
                .collection("07102018")
                .whereEqualTo("4",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots != null) {
                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (snapshot.getType()) {
                                    case ADDED:
                                        setAttendanceValues(snapshot.getDocument());
                                        break;
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void getMarkerForFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (MarkerOptions markerOptions : sellerMarkerHasmap.values()) {
            if(markerOptions != null && markerOptions.getPosition().latitude > 0 && markerOptions.getPosition().longitude > 0)
                builder.include(markerOptions.getPosition());
        }

        supervisorHomeView.focusMarker(builder);
    }

    private void setAttendanceValues(DocumentSnapshot documentSnapshot){

        if (documentSnapshot.getData() != null) {

            System.out.println("setAttendanceValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            Long id = (Long) documentSnapshot.getData().get("userId");
            Integer userId = (int) (long) id;

            if (sellerInfoHasMap.get(userId) != null) {
                sellerInfoHasMap.get(userId).setAttendanceDone(true);
            }
        }

        computeSellerInfo();
    }

    private void computeAttendance(){

    }

    private void setValues(DocumentSnapshot documentSnapshot){

        SupervisorModelBo supervisorModelBo = documentSnapshot.toObject((SupervisorModelBo.class));

        if (supervisorModelBo != null) {

            System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            LatLng destLatLng = new LatLng(supervisorModelBo.getLatitude(), supervisorModelBo.getLongitude());

            supervisorModelBo.setMarkerOptions(new MarkerOptions()
                    .flat(true)
                    .title(supervisorModelBo.getUserName())
                    .position(destLatLng)
                    .snippet(String.valueOf(supervisorModelBo.getUserId())));

            supervisorModelBo.setAttendanceDone(true);

            if(!sellerInfoHasMap.containsKey(supervisorModelBo.getUserId())) {

                sellerInfoHasMap.put(supervisorModelBo.getUserId(), supervisorModelBo);

            }else{

                SupervisorModelBo supervisorModelObj = sellerInfoHasMap.get(supervisorModelBo.getUserId());

                supervisorModelObj.setBilled(supervisorModelBo.getBilled());
                supervisorModelObj.setCovered(supervisorModelBo.getCovered());
                supervisorModelObj.setLatitude(supervisorModelBo.getLatitude());
                supervisorModelObj.setLongitude(supervisorModelBo.getLongitude());
                supervisorModelObj.setOrderValue(supervisorModelBo.getOrderValue());
                supervisorModelObj.setOrdered(supervisorModelBo.isOrdered());
                supervisorModelObj.setTimeIn(supervisorModelBo.getTimeIn());
                supervisorModelObj.setTimeOut(supervisorModelBo.getTimeOut());
                supervisorModelObj.setRetailerId(supervisorModelBo.getRetailerId());
                supervisorModelObj.setRetailerName(supervisorModelBo.getRetailerName());
                supervisorModelObj.setMarkerOptions(supervisorModelBo.getMarkerOptions());
                supervisorModelObj.setAttendanceDone(supervisorModelBo.isAttendanceDone());

                supervisorModelBo = supervisorModelObj;

            }

            if (!isRealTimeLocationOn) {
                setMarkerHasMap(supervisorModelBo);
            }
        }

        computeSellerInfo();
    }

    private void setLocationValues(DocumentSnapshot documentSnapshot){

        SupervisorModelBo supervisorModelBo = documentSnapshot.toObject((SupervisorModelBo.class));

        if (supervisorModelBo != null) {

            System.out.println("setLocationValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            LatLng destLatLng = new LatLng(supervisorModelBo.getLatitude(), supervisorModelBo.getLongitude());

            supervisorModelBo.setMarkerOptions(new MarkerOptions()
                    .flat(true)
                    .title(supervisorModelBo.getUserName())
                    .position(destLatLng)
                    .snippet(String.valueOf(supervisorModelBo.getUserId())));

            supervisorModelBo.setAttendanceDone(true);

            sellerInfoHasMap.get(supervisorModelBo.getUserId()).setMarkerOptions(supervisorModelBo.getMarkerOptions());

            setMarkerHasMap(supervisorModelBo);

        }

//        getMarkerForFocus();
    }

    private void setMarkerHasMap(SupervisorModelBo supervisorModelBo) {
        if(!sellerMarkerHasmap.containsKey(supervisorModelBo.getUserId())) {
            supervisorHomeView.createMarker(sellerInfoHasMap.get(supervisorModelBo.getUserId()));
        }else{
            supervisorHomeView.updateMaker(sellerInfoHasMap.get(supervisorModelBo.getUserId()));
        }

        sellerMarkerHasmap.put(supervisorModelBo.getUserId(), supervisorModelBo.getMarkerOptions());

        if(sellerCount == sellerMarkerHasmap.size() && !isZoomed){
            isZoomed = true;
            getMarkerForFocus();

            computeSellerInfo();
        }
    }

    @Override
    public void isRealtimeLocation(){

        try {
            String sql = "select flag from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='FIRESTORE01'";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.moveToNext()) {
                isRealTimeLocationOn = (c.getInt(0)) == 1;
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private String loadUserLevel() {

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
    public void getSellerListAWS(){

        int totalSellerCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select userId,userName from usermaster where isDeviceuser!=1 and userlevel = '"+loadUserLevel()+"'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {

                    SupervisorModelBo supervisorModelBo = new SupervisorModelBo();

                    supervisorModelBo.setUserId(c.getInt(0));
                    supervisorModelBo.setUserName(c.getString(1));

                    if (!sellerInfoHasMap.containsKey(supervisorModelBo.getUserId())) {
                        sellerInfoHasMap.put(supervisorModelBo.getUserId(), supervisorModelBo);
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

        supervisorHomeView.displayTotalSellerCount(totalSellerCount);

    }

    @Override
    public void getSellerWiseRetailerAWS(){

        int totalOutletCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select sellerId,count(sellerId) from SupRetailerMaster group by sellerId";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {

                    int count = c.getInt(1);

                    totalOutletCount = totalOutletCount + count;

                    if(sellerInfoHasMap.get(c.getInt(0)) != null)
                        sellerInfoHasMap.get(c.getInt(0)).setTarget(c.getInt(1));
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }

        supervisorHomeView.displayTotalOutletCount(totalOutletCount);

    }

}
