package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.cpg.view.supervisor.customviews.LatLngInterpolator;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.ATTENDANCE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_EMAIL;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerMapHomePresenter implements SellerMapHomeContract.SellerMapHomePresenter {

    private LinkedHashMap<Integer,SellerBo> sellerInfoHasMap = new LinkedHashMap<>();
    private HashSet<Integer> sellerIdHashSet = new HashSet<>();
    private SellerMapHomeContract.SellerMapHomeView sellerMapHomeView;
    private Context context;
    private ListenerRegistration registration ;

    private int inMarketSellerCount = 0;
    private boolean isRealTimeLocationOn = false;
    private int sellerCountFirestore = 0;
    private boolean isZoomed = false;
    private int totalOutletCount = 0;
    private BusinessModel bmodel;
    private String selectedDate;

    private String basePath = "";


    @Override
    public void setView(SellerMapHomeContract.SellerMapHomeView sellerMapHomeView, Context context) {
        this.sellerMapHomeView = sellerMapHomeView;
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();

        basePath = AppUtils.getSharedPreferences(context).getString(FIREBASE_ROOT_PATH,"");
    }

    @Override
    public void getSellerListAWS(String date){

        int totalSellerCount = 0;

        sellerInfoHasMap.clear();
        sellerIdHashSet.clear();
        inMarketSellerCount = 0;
        sellerCountFirestore = 0;
        isZoomed = false;
        totalOutletCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId),um.ProfileImagePath from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid and sm.date = '"+date+"' " +
                    "where isDeviceuser!=1 and userlevel in( '"+loadUserLevel()+"')  group by um.userid";


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

        totalOutletCount  = getTotalOutletCount(date);

        sellerMapHomeView.displayTotalSellerCount(totalSellerCount);
        sellerMapHomeView.updateSellerAttendance(totalSellerCount,0);
        sellerMapHomeView.displayTotalOutletCount(totalOutletCount);
    }

    private int getTotalOutletCount(String date) {

        int retailerCount = 0;

        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select count(DISTINCT retailerId) from SupRetailerMaster where date = '" + date + "'";
            Cursor c = db.selectSQL(queryStr);

            if (c != null) {
                if (c.getCount() > 0 && c.moveToNext()) {
                    retailerCount = c.getInt(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
        return retailerCount;
    }

    @Override
    public int getLoginUserId(){

        int userId = 0;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userpositionid from usermaster um where isDeviceuser=1";

            Cursor c = db.selectSQL(queryStr);
            if (c != null && c.moveToNext()) {

                userId = c.getInt(0);

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
        return userId;
    }

    @Override
    public boolean isRealtimeLocation(){

        try {
            String sql = "select flag from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='REALTIME02'";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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
        return isRealTimeLocationOn;
    }

    @Override
    public void loginToFirebase(final Context context, final int userId) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");
        if (appId.equals(""))
            return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            String email = AppUtils.getSharedPreferences(context).getString(FIREBASE_EMAIL,"");

            if (email.equals("")){
                sellerMapHomeView.firebaseLoginFailure();
                return;
            }

            String password = SupervisorModuleConstants.FIREBASE_PASSWORD;
            // Authenticate with Firebase and subscribe to updates

            if(email.trim().length() > 0 && password.trim().length() > 0) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sellerMapHomeView.firebaseLoginSuccess();
                        } else {
                            sellerMapHomeView.firebaseLoginFailure();
                        }
                    }
                });
            }
        }else{
            sellerMapHomeView.firebaseLoginSuccess();
        }
    }

    @Override
    public void getSellerMarkerInfo(String userId) {

    }

    @Override
    public void sellerActivityInfoListener(int userId,String date) {

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
    public void realtimeLocationInfoListener(int userId,String date){

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference queryRef = db
                .collection(basePath)
                .document(REALTIME_LOCATION_PATH)
                .collection(date);

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
    public void sellerAttendanceInfoListener(int userId,String date){

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference queryRef = db
                .collection(basePath)
                .document(ATTENDANCE_PATH)
                .collection(date);

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
                                    case MODIFIED:
                                        updateAttendanceValues(snapshot.getDocument());
                                }
                            }
                        }
                    }
                });
    }

    private void updateAttendanceValues(DocumentSnapshot document) {

            Integer userId = (int) (long) document.getData().get("userId");

            if (sellerInfoHasMap.get(userId) != null) {

                if (document.getData().get("uid") != null){
                    sellerInfoHasMap.get(userId).setUid((String)document.getData().get("uid"));
                }

                if (document.getData().get("status") != null &&
                        ((String) document.getData().get("status")).equalsIgnoreCase("day closed")){
                    sellerInfoHasMap.get(userId).setSellerWorking(false);

                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_out);
                    if (sellerInfoHasMap.get(userId).getMarker() != null) {
                        sellerInfoHasMap.get(userId).getMarker().setIcon(icon);
                    }

                }else {
                    sellerInfoHasMap.get(userId).setSellerWorking(true);

                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_in);
                    if (sellerInfoHasMap.get(userId).getMarker() != null) {
                        sellerInfoHasMap.get(userId).getMarker().setIcon(icon);
                    }
                }

            }
    }

    @Override
    public void getMarkerValuesToFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean isFocus = false;

        for(Integer sellerId : sellerIdHashSet) {

            if(sellerInfoHasMap.get(sellerId).getMarker() != null) {

                LatLng builderLatLng = sellerInfoHasMap.get(sellerId).getMarker().getPosition();

                if (builderLatLng != null && builderLatLng.latitude > 0 && builderLatLng.longitude > 0) {
                    builder.include(builderLatLng);

                    isFocus = true;
                }
            }
        }
        if (isFocus)
            sellerMapHomeView.focusMarker(builder);
    }

    @Override
    public void computeSellerInfo() {

        ArrayList<SellerBo> inMarketSellerList = new ArrayList<>();

        double totatlOrderValue = 0;
        int coveredOutlet = 0;
        int billedOutlet = 0;

        for(Integer userId : sellerIdHashSet){
            SellerBo sellerBo = sellerInfoHasMap.get(userId);

            totatlOrderValue = totatlOrderValue + sellerBo.getTotalOrderValue();

            coveredOutlet = coveredOutlet + sellerBo.getCovered();
            billedOutlet = billedOutlet + sellerBo.getBilled();

            if(sellerIdHashSet.contains(sellerBo.getUserId()))
                inMarketSellerList.add(sellerBo);
        }

        sellerMapHomeView.updateOrderValue(totatlOrderValue);
        sellerMapHomeView.updateCoveredCount(coveredOutlet);

        int unBilledoutlet = coveredOutlet - billedOutlet;
        sellerMapHomeView.updateUnbilledCount(unBilledoutlet);

        int sellerProductive = 0;
        if (totalOutletCount!=0 ) {
            sellerProductive = (int)((float)billedOutlet / (float)totalOutletCount * 100);
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
    public void downloadSupRetailerMaster(String selectedDate) {
        new DownloadSupRetailMaster(selectedDate).execute();
    }

    boolean checkSelectedDateExist(String date){
        boolean isExist = false;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select count(date) from SupRetailerMaster where date='"+date+"'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null && c.moveToNext()) {
                if (c.getInt(0) > 0)
                    isExist = true;

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
        return isExist;
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

//                        double bearing = bearingBetweenLocations(startPosition, destination);
//                        if (bearing >= 0)
//                            marker.setRotation((float)bearing);
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

    public void setSupervisorLastVisit(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("lastUpdate", FieldValue.serverTimestamp());
        db.collection("SupervisorState")
                .document("State")
                .set(stringObjectMap);
    }

    private String loadUserLevel() {

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

//        String[] codes = code.split(",");
//        ArrayList<String> codeList = new ArrayList<>();
//        for(String userLevel : codes) {
//            code = "'" + userLevel + "'";
//            codeList.add(code);
//        }

        return code;

    }

    private void setAttendanceValues(DocumentSnapshot documentSnapshot){

        if (documentSnapshot.getData() != null) {

           // System.out.println("setAttendanceValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            Integer userId = (int) (long) documentSnapshot.getData().get("userId");

            if (sellerInfoHasMap.get(userId) != null &&
                    !sellerInfoHasMap.get(userId).isAttendanceDone()) {

                computeSellerAttendance(userId);

            }

            updateAttendanceValues(documentSnapshot);
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

        try {
            SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

            //System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

            if (sellerBoDocumentSnapshot != null && sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId()) != null) {

                SellerBo sellerBoHashmap = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

                sellerBoHashmap.setBilled(sellerBoDocumentSnapshot.getBilled());
                sellerBoHashmap.setCovered(sellerBoDocumentSnapshot.getCovered());
                sellerBoHashmap.setLatitude(sellerBoDocumentSnapshot.getLatitude());
                sellerBoHashmap.setLongitude(sellerBoDocumentSnapshot.getLongitude());
                sellerBoHashmap.setOrderValue(sellerBoDocumentSnapshot.getOrderValue());
                sellerBoHashmap.setTotalOrderValue(sellerBoDocumentSnapshot.getTotalOrderValue());
                sellerBoHashmap.setInTime(sellerBoDocumentSnapshot.getInTime());
                sellerBoHashmap.setOutTime(sellerBoDocumentSnapshot.getOutTime());

                sellerBoHashmap.setRetailerName(sellerBoDocumentSnapshot.getRetailerName()!=null?sellerBoDocumentSnapshot.getRetailerName():"");

                if(sellerBoHashmap.getLatitude() == 0 || sellerBoHashmap.getLongitude() == 0 ){
                    LatLng latLng = SupervisorActivityHelper.getInstance().retailerLatLngByRId(sellerBoHashmap.getRetailerId());
                    sellerBoHashmap.setLatitude(latLng.latitude);
                    sellerBoHashmap.setLongitude(latLng.longitude);
                }

                if (!sellerBoHashmap.isAttendanceDone()) {
                    computeSellerAttendance(sellerBoDocumentSnapshot.getUserId());
                    if (isToday())
                        notifyAttendance();
                }

                if (!isRealTimeLocationOn) {

                    LatLng destLatLng = new LatLng(sellerBoDocumentSnapshot.getLatitude(), sellerBoDocumentSnapshot.getLongitude());

                    MarkerOptions markerOptions = new MarkerOptions()
                            .title(sellerBoHashmap.getUserName())
                            .position(destLatLng)
//                            .flat(true)
                            .snippet(String.valueOf(sellerBoHashmap.getUserId()));

                    setMarkerHasMap(sellerBoHashmap, markerOptions);
                }

                computeSellerInfo();
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    private void setLocationValues(DocumentSnapshot documentSnapshot){

        try {
            SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

            if (sellerBoDocumentSnapshot != null) {

//                System.out.println("setLocationValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

                LatLng destLatLng = new LatLng(sellerBoDocumentSnapshot.getLatitude(), sellerBoDocumentSnapshot.getLongitude());

                SellerBo sellerHasmapBo = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

                if (sellerHasmapBo != null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .title(sellerHasmapBo.getUserName())
                            .position(destLatLng)
//                            .flat(true)
                            .snippet(String.valueOf(sellerHasmapBo.getUserId()));

                    sellerHasmapBo.setLatitude(sellerBoDocumentSnapshot.getLatitude());
                    sellerHasmapBo.setLongitude(sellerBoDocumentSnapshot.getLongitude());

                    if (!sellerHasmapBo.isAttendanceDone()) {
                        computeSellerAttendance(sellerHasmapBo.getUserId());

                        if (isToday())
                            notifyAttendance();
                    }

                    setMarkerHasMap(sellerHasmapBo, markerOptions);
                }
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    private void setMarkerHasMap(SellerBo sellerBo,MarkerOptions markerOptions) {
        if(!sellerIdHashSet.contains(sellerBo.getUserId())) {

            if (!sellerInfoHasMap.get(sellerBo.getUserId()).isSellerWorking()) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_out);
                markerOptions.icon(icon);
            }else{
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_in);
                markerOptions.icon(icon);
            }

            sellerMapHomeView.createMarker(sellerInfoHasMap.get(sellerBo.getUserId()),markerOptions);
            sellerIdHashSet.add(sellerBo.getUserId());

            computeSellerInfo();
        }else{
            LatLng destLatLng = new LatLng(sellerBo.getLatitude(), sellerBo.getLongitude());

            if (!sellerInfoHasMap.get(sellerBo.getUserId()).isSellerWorking()) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_out);
                sellerInfoHasMap.get(sellerBo.getUserId()).getMarker().setIcon(icon);
            }

            sellerMapHomeView.updateMaker(destLatLng,sellerInfoHasMap.get(sellerBo.getUserId()).getMarker());
        }

        if(sellerCountFirestore == sellerIdHashSet.size() && sellerIdHashSet.size() > 0 && !isZoomed){
            isZoomed = true;
            getMarkerValuesToFocus();
        }
    }

    ArrayList<SellerBo> getAllSellerList(){
        return new ArrayList<>(sellerInfoHasMap.values());
    }

    class DownloadSupRetailMaster extends AsyncTask<String, Void, Boolean> {

        private String selectedDate;

        DownloadSupRetailMaster(String seletedDate){
            this.selectedDate = seletedDate;
        }

        AlertDialog alertDialog;

        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            customProgressDialog(builder,
                    context.getResources().getString(R.string.progress_dialog_title_downloading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            return NetworkUtils.isNetworkConnected(context) && prepareHttpData(selectedDate);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (result) {
                Toast.makeText(context, "Download Successfull", Toast.LENGTH_SHORT).show();

                SupervisorActivityHelper.getInstance().downloadOutletListAws(context,selectedDate);
                getSellerListAWS(selectedDate);
                sellerMapHomeView.updateSellerInfoByDate(convertGlobalDateToPlane(selectedDate));

            }else
                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean prepareHttpData(String selectedDate) {

        boolean isSuccess = false;
        try {
            JSONObject json = new JSONObject();

            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("LoginId", bmodel.userMasterHelper.getUserMasterBO().getLoginName());
            json.put("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            json.put(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            json.put("RequestDate",selectedDate);

            String downloadurl = getDownloadUrl();

            Commons.print("downloadUrl "+downloadurl);
            Commons.print("json = " + json);

            Vector<String> responseVector = getSupRetailerMasterResponse(json, downloadurl);

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

    private Vector<String> getSupRetailerMasterResponse(JSONObject data,
                                            String appendurl) {
        // Update Security key
        bmodel.synchronizationHelper.updateAuthenticateToken(false);
        StringBuilder url = new StringBuilder();
        url.append(DataMembers.SERVER_URL);
        url.append(appendurl);
        if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
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

    private String getDownloadUrl(){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            String sb = "select url from urldownloadmaster where " +
                    "mastername='SUP_RTR_MASTER' and typecode='SYNMAS'";

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

    String convertGlobalDateToPlane(String globalDate){
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            Date date = sdf.parse(globalDate);

            sdf = new SimpleDateFormat("MMddyyyy",Locale.ENGLISH);
            globalDate =sdf.format(date);
            return globalDate;

        }catch(Exception e){
            Commons.printException(e);
        }

        return globalDate;
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

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    private void notifyAttendance(){
        try {
            Uri notification = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notify);

            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isToday(){
        return convertPlaneDateToGlobal(getSelectedDate()).equals(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
    }

    boolean isSameDateSelected(String selectedDate){
        boolean isSameDate = false;
        try {
            if (getSelectedDate().equals(convertGlobalDateToPlane(selectedDate)))
                isSameDate = true;

        }catch(Exception e){
            Commons.printException(e);
        }
        return isSameDate;
    }

    boolean checkAreaBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }

    private double bearingBetweenLocations(LatLng latLng1,LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

}
