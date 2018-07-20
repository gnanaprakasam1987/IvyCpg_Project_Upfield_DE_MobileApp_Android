package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.cpg.view.supervisor.customviews.LatLngInterpolator;
import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.annotation.Nullable;

public class SellerMapHomePresenter implements SellerMapHomeContract.SellerMapHomePresenter {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LinkedHashMap<Integer,SellerBo> sellerInfoHasMap = new LinkedHashMap<>();
    private HashSet<Integer> sellerIdHashSet = new HashSet<>();
    private SellerMapHomeContract.SellerMapHomeView sellerMapHomeView;
    private Context context;
    private ListenerRegistration registration ;

    private int inMarketSellerCount = 0;
    private boolean isRealTimeLocationOn = false;
    private int sellerCountFirestore = 0;
    private boolean isZoomed = false;

    @Override
    public void setView(SellerMapHomeContract.SellerMapHomeView sellerMapHomeView, Context context) {
        this.sellerMapHomeView = sellerMapHomeView;
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

                    SellerBo sellerBo = new SellerBo();

                    sellerBo.setUserId(c.getInt(0));
                    sellerBo.setUserName(c.getString(1));

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

        sellerMapHomeView.displayTotalSellerCount(totalSellerCount);
        sellerMapHomeView.updateSellerAttendance(totalSellerCount,0);
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

        sellerMapHomeView.displayTotalOutletCount(totalOutletCount);

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
    public void loginToFirebase(final Context context, final int userId) {

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
                            initiateAllMethods(userId);
                        } else {
                            sellerMapHomeView.firebaseLoginFailure();
                        }
                    }
                });
            }
        }else{
            initiateAllMethods(userId);
        }
    }

    @Override
    public void getSellerMarkerInfo(String userId) {

    }

    @Override
    public void sellerActivityInfoListener(int userId) {

        CollectionReference queryRef = db
                .collection("activity_tracking_v2")
                .document("retailer_time_stamp")
                .collection("07052018");

        registration = queryRef
                .whereEqualTo(userId+"",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots!=null) {

                            if (!isRealTimeLocationOn)
                                sellerCountFirestore = queryDocumentSnapshots.size();

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
    public void realtimeLocationInfoListener(int userId){

        CollectionReference queryRef = db
                .collection("activity_tracking_v2")
                .document("movement_tracking")
                .collection("07102018");

        registration = queryRef
                .whereEqualTo(userId+"",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots != null) {

                            sellerCountFirestore = queryDocumentSnapshots.size();

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
    public void sellerAttendanceInfoListener(int userId){

        CollectionReference queryRef = db
                .collection("activity_tracking_v2")
                .document("Attendance")
                .collection("07102018");

        registration = queryRef
                .whereEqualTo(userId+"",true)
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
    public void getMarkerValuesToFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Integer sellerId : sellerIdHashSet) {

            if(sellerInfoHasMap.get(sellerId).getMarker() != null) {

                LatLng builderLatLng = sellerInfoHasMap.get(sellerId).getMarker().getPosition();

                if (builderLatLng != null && builderLatLng.latitude > 0 && builderLatLng.longitude > 0)
                    builder.include(builderLatLng);
            }
        }

        sellerMapHomeView.focusMarker(builder);
    }

    @Override
    public void computeSellerInfo() {

        ArrayList<SellerBo> inMarketSellerList = new ArrayList<>();

        long totatlOrderValue = 0L;
        int coveredOutlet = 0;
        int billedOutlet = 0;

        for(Integer userId : sellerIdHashSet){
            SellerBo sellerBo = sellerInfoHasMap.get(userId);

            totatlOrderValue = totatlOrderValue + sellerBo.getOrderValue();

            coveredOutlet = coveredOutlet + sellerBo.getCovered();
            billedOutlet = billedOutlet + sellerBo.getBilled();

            if(sellerIdHashSet.contains(sellerBo.getUserId()))
                inMarketSellerList.add(sellerBo);
        }

        sellerMapHomeView.updateOrderValue((int)totatlOrderValue);
        sellerMapHomeView.updateCoveredCount(coveredOutlet);

        int unBilledoutlet = coveredOutlet - billedOutlet;
        sellerMapHomeView.updateUnbilledCount(unBilledoutlet);

        int sellerProductive = 0;
        if (coveredOutlet!=0) {
            sellerProductive = (int)((float)billedOutlet / (float)coveredOutlet * 100);
        }
        sellerMapHomeView.sellerProductivity(sellerProductive);

        sellerMapHomeView.setSellerListAdapter(inMarketSellerList);
    }

    @Override
    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    @Override
    public void animateSellerMarker(final LatLng destination, final Marker marker) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);

            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
//                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                                .target(newPosition)
//                                .zoom(14f)
//                                .build()));

//                        float bearing = getBearing(startPosition, destination);
//                        if (bearing >= 0)
//                            marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
                        Commons.printException(ex);
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                }
            });
            valueAnimator.start();
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

    private void initiateAllMethods(int userId){

        sellerAttendanceInfoListener(userId);

        sellerActivityInfoListener(userId);

        if (isRealTimeLocationOn)
            realtimeLocationInfoListener(userId);

    }

    private void setAttendanceValues(DocumentSnapshot documentSnapshot){

        if (documentSnapshot.getData() != null) {

            System.out.println("setAttendanceValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            Integer userId = (int) (long) documentSnapshot.getData().get("userId");

            if (sellerInfoHasMap.get(userId) != null &&
                    !sellerInfoHasMap.get(userId).isAttendanceDone()) {
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

        sellerMapHomeView.updateSellerAttendance(absentSellerCount,inMarketSellerCount);
    }

    private void setValues(DocumentSnapshot documentSnapshot){

        SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

        System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if(sellerBoDocumentSnapshot != null && sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId()) != null) {

            SellerBo sellerBoHashmap = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

            sellerBoHashmap.setBilled(sellerBoDocumentSnapshot.getBilled());
            sellerBoHashmap.setCovered(sellerBoDocumentSnapshot.getCovered());
            sellerBoHashmap.setLatitude(sellerBoDocumentSnapshot.getLatitude());
            sellerBoHashmap.setLongitude(sellerBoDocumentSnapshot.getLongitude());
            sellerBoHashmap.setOrderValue(sellerBoDocumentSnapshot.getOrderValue());
            sellerBoHashmap.setTimeIn(sellerBoDocumentSnapshot.getTimeIn());
            sellerBoHashmap.setTimeOut(sellerBoDocumentSnapshot.getTimeOut());
            sellerBoHashmap.setRetailerName(sellerBoDocumentSnapshot.getRetailerName()!=null?sellerBoDocumentSnapshot.getRetailerName():"");

            if (!sellerBoHashmap.isAttendanceDone()) {
                computeSellerAttendance(sellerBoDocumentSnapshot.getUserId());
            }

            if (!isRealTimeLocationOn) {

                LatLng destLatLng = new LatLng(sellerBoDocumentSnapshot.getLatitude(), sellerBoDocumentSnapshot.getLongitude());

                MarkerOptions markerOptions = new MarkerOptions()
                        .flat(true)
                        .title(sellerBoHashmap.getUserName())
                        .position(destLatLng)
                        .snippet(String.valueOf(sellerBoHashmap.getUserId()));

                setMarkerHasMap(sellerBoHashmap,markerOptions);
            }

            computeSellerInfo();
        }
    }

    private void setLocationValues(DocumentSnapshot documentSnapshot){

        SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

        if (sellerBoDocumentSnapshot != null) {

            System.out.println("setLocationValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            LatLng destLatLng = new LatLng(sellerBoDocumentSnapshot.getLatitude(), sellerBoDocumentSnapshot.getLongitude());

            SellerBo sellerHasmapBo = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

            MarkerOptions markerOptions = new MarkerOptions()
                    .flat(true)
                    .title(sellerHasmapBo.getUserName())
                    .position(destLatLng)
                    .snippet(String.valueOf(sellerHasmapBo.getUserId()));

            sellerHasmapBo.setLatitude(sellerBoDocumentSnapshot.getLatitude());
            sellerHasmapBo.setLongitude(sellerBoDocumentSnapshot.getLongitude());

            if (!sellerHasmapBo.isAttendanceDone()) {
                computeSellerAttendance(sellerHasmapBo.getUserId());
            }

            setMarkerHasMap(sellerHasmapBo,markerOptions);
        }
    }

    private void setMarkerHasMap(SellerBo sellerBo,MarkerOptions markerOptions) {
        if(!sellerIdHashSet.contains(sellerBo.getUserId())) {
            sellerMapHomeView.createMarker(sellerInfoHasMap.get(sellerBo.getUserId()),markerOptions);
            sellerIdHashSet.add(sellerBo.getUserId());

            computeSellerInfo();
        }else{
            LatLng destLatLng = new LatLng(sellerBo.getLatitude(), sellerBo.getLongitude());
            sellerMapHomeView.updateMaker(destLatLng,sellerInfoHasMap.get(sellerBo.getUserId()).getMarker());
        }

        if(sellerCountFirestore == sellerIdHashSet.size() && sellerIdHashSet.size() > 0 && !isZoomed){
            isZoomed = true;
            getMarkerValuesToFocus();
        }
    }

    public ArrayList<SellerBo> getAllSellerList(){
        return new ArrayList<>(sellerInfoHasMap.values());
    }

}
