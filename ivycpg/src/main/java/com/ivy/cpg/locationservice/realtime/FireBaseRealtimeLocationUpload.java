package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class FireBaseRealtimeLocationUpload implements RealTimeLocation {

    public FireBaseRealtimeLocationUpload(){

    }

    /*Firebase Authentication Method*/
    public FireBaseRealtimeLocationUpload(Context context) {
        // Authenticate with Firebase, and request location updates
        String email = context.getString(R.string.firebase_email);
        String password = context.getString(R.string.firebase_password);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Commons.print("Service Firebase Uth Success");
                } else {
                    Commons.print("Service firebase onComplete: Failed=");
                }
            }
        });
    }

    /**
     * Get Triggered when location received
     */
    @Override
    public void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context) {

        locationDetailBO.setOutTime("");
        locationDetailBO.setStatus("IN");

        updateFirebaseData(context,locationDetailBO,"RealtimeTracking");

    }

    @Override
    public void onRealTimeLocationStopped(Context context) {

        String userId = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = String.valueOf(userMasterBO.getUserid());
        }

        if(FirebaseDatabase.getInstance()!=null) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                    child(context.getString(R.string.firebase_path)).child("RealtimeTracking").child(userId);
            databaseReference.child("outTime").setValue(String.valueOf(System.currentTimeMillis()));
            databaseReference.child("status").setValue("Day Closed");
//            FirebaseDatabase.getInstance().goOffline();
        }
    }

    @Override
    public void movementTrackingAttendanceIn(Context context,String pathNode) {
        String userId = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = String.valueOf(userMasterBO.getUserid());
        }

        if(FirebaseDatabase.getInstance()!=null) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                    child(context.getString(R.string.firebase_path)).child(pathNode).child(userId);
            databaseReference.child("inTime").setValue(String.valueOf(System.currentTimeMillis()));
            databaseReference.child("status").setValue("IN");
            databaseReference.child("userId").setValue("userId");
            databaseReference.child("supervisorId").setValue(getSupervisorIds(context));
        }
    }

    @Override
    public void movementTrackingAttendanceOut(Context context,String pathNode) {

        String userId = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = String.valueOf(userMasterBO.getUserid());
        }

        if(FirebaseDatabase.getInstance()!=null) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                    child(context.getString(R.string.firebase_path)).child(pathNode).child(userId);
            databaseReference.child("outTime").setValue(String.valueOf(System.currentTimeMillis()));
            databaseReference.child("status").setValue("Day Closed");
//            FirebaseDatabase.getInstance().goOffline();
        }
    }

    /**
     * Insert or update Location data and attendance data in Firebase Node
     */
    private void updateFirebaseData(Context context, LocationDetailBO locationDetailBO,String nodePath) {
        String userId = "", userName = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = String.valueOf(userMasterBO.getUserid());
            userName = String.valueOf(userMasterBO.getUserName());
        }
        locationDetailBO.setUserId(userId);
        locationDetailBO.setUserName(userName);
        locationDetailBO.setSupervisorId(getSupervisorIds(context));

        final String path = context.getString(R.string.firebase_path) + "/"+nodePath+"/"+ userId;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.child("userId").setValue(userId);
        ref.child("userName").setValue(userName);
        ref.child("supervisorId").setValue(getSupervisorIds(context));
        ref.child("latitude").setValue(locationDetailBO.getLatitude());
        ref.child("longitude").setValue(locationDetailBO.getLongitude());
        ref.child("accuracy").setValue(locationDetailBO.getAccuracy());
        ref.child("batterStatus").setValue(locationDetailBO.getBatteryStatus());
        ref.child("gpsEnabled").setValue(locationDetailBO.isGpsEnabled());
        ref.child("mockLocationEnabled").setValue(locationDetailBO.isMockLocationEnabled());
        ref.child("activityType").setValue(locationDetailBO.getActivityType());
    }

    /**
     * Get User data from database
     */
    private UserMasterBO getUserDetail(Context context) {
        UserMasterBO userMasterBO = null;
        DBUtil db;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor cursor = db.selectSQL("select userid,username from usermaster where isDeviceuser=1");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                userMasterBO = new UserMasterBO();
                userMasterBO.setUserid(cursor.getInt(0));
                userMasterBO.setUserName(cursor.getString(1));
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return userMasterBO;
    }

    /**
     * Get User Id from usermaster with Relation Parent as SupervisorIds
     */
    private String getSupervisorIds(Context context) {
        StringBuilder supervisorIds = new StringBuilder("/");

        DBUtil db;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor cursor = db.selectSQL("select userid from usermaster where isDeviceuser=0 and relationship = 'PARENT'");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    supervisorIds.append(cursor.getString(0)).append("/");
                }
            }else
                supervisorIds = new StringBuilder();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return supervisorIds.toString();
    }
}
