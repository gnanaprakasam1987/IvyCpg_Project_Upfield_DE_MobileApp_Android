package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.ATTENDANCE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIRESTORE_BASE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class SellerMapHomePresenter implements SellerMapHomeContract.SellerMapHomePresenter {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    private TextView messagetv;
    private BusinessModel bmodel;
    private String selectedDate;


    @Override
    public void setView(SellerMapHomeContract.SellerMapHomeView sellerMapHomeView, Context context) {
        this.sellerMapHomeView = sellerMapHomeView;
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
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

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select um.userId,um.userName,count(sm.userId) from usermaster um " +
                    "left join SupRetailerMaster sm on sm.userId = um.userid and sm.date = '"+date+"' " +
                    "where isDeviceuser!=1 and userlevel in( '"+loadUserLevel()+"')  group by um.userid";


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

                    totalOutletCount = totalOutletCount + sellerBo.getTarget();
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
        sellerMapHomeView.displayTotalOutletCount(totalOutletCount);
    }

    @Override
    public int getLoginUserId(){

        int userId = 0;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
        return isRealTimeLocationOn;
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

        CollectionReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
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

        CollectionReference queryRef = db
                .collection(FIRESTORE_BASE_PATH)
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

            totatlOrderValue = totatlOrderValue + sellerBo.getTotalOrderValue();

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

    public boolean checkSelectedDateExist(String date){
        boolean isExist = false;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
                    + " where hhtCode='REALTIME03' and flag = 1";
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

        try {
            SellerBo sellerBoDocumentSnapshot = documentSnapshot.toObject((SellerBo.class));

            System.out.println("setValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

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

                sellerBoHashmap.setRetailerName(SupervisorActivityHelper.getInstance().retailerNameById(sellerBoDocumentSnapshot.getRetailerId()));

                if(sellerBoHashmap.getLatitude() == 0 || sellerBoHashmap.getLongitude() == 0 ){
                    LatLng latLng = SupervisorActivityHelper.getInstance().retailerLatLngByRId(sellerBoHashmap.getRetailerId());
                    sellerBoHashmap.setLatitude(latLng.latitude);
                    sellerBoHashmap.setLongitude(latLng.longitude);
                }

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

                System.out.println("setLocationValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

                LatLng destLatLng = new LatLng(sellerBoDocumentSnapshot.getLatitude(), sellerBoDocumentSnapshot.getLongitude());

                SellerBo sellerHasmapBo = sellerInfoHasMap.get(sellerBoDocumentSnapshot.getUserId());

                if (sellerHasmapBo != null) {
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

                    setMarkerHasMap(sellerHasmapBo, markerOptions);
                }
            }
        }catch(Exception e){
            Commons.printException(e);
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

            return isOnline() && prepareData(selectedDate);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (result) {
                Toast.makeText(context, "Download Successfull", Toast.LENGTH_SHORT).show();

                getSellerListAWS(selectedDate);
                sellerMapHomeView.updateSellerInfoByDate(convertGlobalDateToPlane(selectedDate));

            }else
                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean prepareData(String selectedDate) {

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
            System.out.println("json = " + json);

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

    public Vector<String> getSupRetailerMasterResponse(JSONObject data,
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
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) view.findViewById(R.id.text);
            messagetv.setText(message);

            builder.setView(view);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private boolean isOnline() {

        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        }catch(Exception e){
            Commons.printException(e);
        }
        return false;

    }

    private String convertGlobalDateToPlane(String globalDate){
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

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }
}
