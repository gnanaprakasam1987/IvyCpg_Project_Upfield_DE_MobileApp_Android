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
    public static HashMap<Integer, SupervisorModelBo> sellerInfoHasMap = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, MarkerOptions> sellerMarkerHasmap = new HashMap<>();
    private SupervisorHomeContract.SupervisorHomeView supervisorHomeView;
    private Context context;
    private ListenerRegistration registration ;

    private int inMarketSellerCount = 0;

    private boolean isRealTimeLocationOn = false;

    private int sellerCount = 0;
    private boolean isZoomed = false;

    @Override
    public void setView(SupervisorHomeContract.SupervisorHomeView supervisorHomeView,Context context) {
        this.supervisorHomeView = supervisorHomeView;
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
        supervisorHomeView.updateSellerAttendance(totalSellerCount,0);

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

        if (isRealTimeLocationOn)
            realtimeLocationInfoListener();

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

    @Override
    public void computeSellerInfo() {

        ArrayList<SupervisorModelBo> markerList = new ArrayList<>();

        long totatlOrderValue = 0L;
        int coveredOutlet = 0;
        int billedOutlet = 0;

        for(SupervisorModelBo supervisorModelBo : sellerInfoHasMap.values()){
            if(supervisorModelBo.getOrderValue() !=null )
                totatlOrderValue = totatlOrderValue + supervisorModelBo.getOrderValue();

            coveredOutlet = coveredOutlet + supervisorModelBo.getCovered();
            billedOutlet = billedOutlet + supervisorModelBo.getBilled();

            if(sellerMarkerHasmap.get(supervisorModelBo.getUserId()) != null)
                markerList.add(supervisorModelBo);
        }

        supervisorHomeView.updateOrderValue((int)totatlOrderValue);
        supervisorHomeView.updateCoveredCount(coveredOutlet);

        int unBilledoutlet = coveredOutlet - billedOutlet;
        supervisorHomeView.updateUnbilledCount(unBilledoutlet);

        int sellerProductive = 0;
        if (coveredOutlet!=0) {
            sellerProductive = (int)((float)billedOutlet / (float)coveredOutlet * 100);
        }
        supervisorHomeView.sellerProductivity(sellerProductive);

        supervisorHomeView.setSellerListAdapter(markerList);
    }

    @Override
    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    private void setAttendanceValues(DocumentSnapshot documentSnapshot){

        if (documentSnapshot.getData() != null) {

            System.out.println("setAttendanceValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            Integer userId = (int) (long) documentSnapshot.getData().get("userId");

            if (sellerInfoHasMap.get(userId) != null && !sellerInfoHasMap.get(userId).isAttendanceDone()) {
                computeSellerAttendance(userId);
            }
        }

        computeSellerInfo();
    }

    private void computeSellerAttendance(int userId){

        inMarketSellerCount = inMarketSellerCount + 1;
        sellerInfoHasMap.get(userId).setAttendanceDone(true);

        int totalSellerCount = sellerInfoHasMap.size();
        int absentSellerCount;

        absentSellerCount = totalSellerCount - inMarketSellerCount;

        supervisorHomeView.updateSellerAttendance(absentSellerCount,inMarketSellerCount);
    }

    private void setValues(DocumentSnapshot documentSnapshot){

        SupervisorModelBo supervisorModelBo = documentSnapshot.toObject((SupervisorModelBo.class));

        System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if(supervisorModelBo != null && sellerInfoHasMap.get(supervisorModelBo.getUserId()) != null) {

            SupervisorModelBo supervisorModelObj = sellerInfoHasMap.get(supervisorModelBo.getUserId());

            supervisorModelObj.setBilled(supervisorModelBo.getBilled());
            supervisorModelObj.setCovered(supervisorModelBo.getCovered());
            supervisorModelObj.setLatitude(supervisorModelBo.getLatitude());
            supervisorModelObj.setLongitude(supervisorModelBo.getLongitude());
            supervisorModelObj.setOrderValue(supervisorModelBo.getOrderValue()!=null?supervisorModelBo.getOrderValue():0);
            supervisorModelObj.setOrdered(supervisorModelBo.isOrdered());
            supervisorModelObj.setTimeIn(supervisorModelBo.getTimeIn()!=null?supervisorModelBo.getTimeIn():0);
            supervisorModelObj.setTimeOut(supervisorModelBo.getTimeOut()!=null?supervisorModelBo.getTimeOut():0);
            supervisorModelObj.setRetailerId(supervisorModelBo.getRetailerId());
            supervisorModelObj.setRetailerName(supervisorModelBo.getRetailerName()!=null?supervisorModelBo.getRetailerName():"");

            if (!supervisorModelObj.isAttendanceDone()) {
                computeSellerAttendance(supervisorModelBo.getUserId());
            }

            if (!isRealTimeLocationOn) {

                LatLng destLatLng = new LatLng(supervisorModelBo.getLatitude(), supervisorModelBo.getLongitude());

                supervisorModelBo.setMarkerOptions(new MarkerOptions()
                        .flat(true)
                        .title(supervisorModelObj.getUserName())
                        .position(destLatLng)
                        .snippet(String.valueOf(supervisorModelObj.getUserId())));

                supervisorModelObj.setMarkerOptions(supervisorModelBo.getMarkerOptions());

                setMarkerHasMap(supervisorModelObj);
            }

            computeSellerInfo();
        }
    }

    private void setLocationValues(DocumentSnapshot documentSnapshot){

        SupervisorModelBo supervisorModelBo = documentSnapshot.toObject((SupervisorModelBo.class));

        if (supervisorModelBo != null) {

            System.out.println("setLocationValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            LatLng destLatLng = new LatLng(supervisorModelBo.getLatitude(), supervisorModelBo.getLongitude());

            SupervisorModelBo sellerHasmapBo = sellerInfoHasMap.get(supervisorModelBo.getUserId());

            sellerHasmapBo.setMarkerOptions(new MarkerOptions()
                    .flat(true)
                    .title(sellerHasmapBo.getUserName())
                    .position(destLatLng)
                    .snippet(String.valueOf(sellerHasmapBo.getUserId())));



            sellerHasmapBo.setLatitude(supervisorModelBo.getLatitude());
            sellerHasmapBo.setLongitude(supervisorModelBo.getLongitude());

            if (!sellerHasmapBo.isAttendanceDone()) {
                computeSellerAttendance(sellerHasmapBo.getUserId());
            }

            sellerHasmapBo.setMarkerOptions(sellerHasmapBo.getMarkerOptions());

            setMarkerHasMap(sellerHasmapBo);

        }
    }

    private void setMarkerHasMap(SupervisorModelBo supervisorModelBo) {
        if(sellerMarkerHasmap.get(supervisorModelBo.getUserId()) == null) {
            supervisorHomeView.createMarker(sellerInfoHasMap.get(supervisorModelBo.getUserId()));
            sellerMarkerHasmap.put(supervisorModelBo.getUserId(), supervisorModelBo.getMarkerOptions());

            computeSellerInfo();

        }else{
            sellerMarkerHasmap.put(supervisorModelBo.getUserId(), supervisorModelBo.getMarkerOptions());
            supervisorHomeView.updateMaker(sellerInfoHasMap.get(supervisorModelBo.getUserId()));
        }

        if(sellerCount == sellerMarkerHasmap.size() && sellerMarkerHasmap.size() > 0 && !isZoomed){
            isZoomed = true;
            getMarkerForFocus();
        }
    }


}
