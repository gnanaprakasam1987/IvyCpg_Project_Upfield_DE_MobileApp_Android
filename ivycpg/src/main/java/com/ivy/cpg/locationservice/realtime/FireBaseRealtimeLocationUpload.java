package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;
import java.util.Map;

public class FireBaseRealtimeLocationUpload implements RealTimeLocation {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Firebase Authentication Method
     * Authenticate with Firebase, and request location updates
     */
    public FireBaseRealtimeLocationUpload(Context context) {

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {

            String email = LocationConstants.FIREBASE_EMAIL;
            String password = LocationConstants.FIREBASE_PASSWORD;

            if(email.trim().length() > 0 && password.trim().length() > 0) {
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
            }else{
                Commons.print("Firebase : No User Found");
            }
        }else{
            Commons.print("Firebase : User already Online");
        }
    }

    public FireBaseRealtimeLocationUpload(Parcel parcel) {

    }

    /**
     * Get Triggered when location received
     */
    @Override
    public void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context) {

        locationDetailBO.setOutTime("");
        locationDetailBO.setStatus("IN");

        updateFirebaseData(context, locationDetailBO, "movement_tracking");

    }

    /**
     * Update Firebase Attendance InTime and Status in Specified Node
     */
    @Override
    public void updateAttendanceIn(final Context context, String pathNode) {
        int userId = 0;
        String userName = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = userMasterBO.getUserid();
            userName = String.valueOf(userMasterBO.getUserName());
        }

        Map<String, Object> attendanceObj = new HashMap<>();
        attendanceObj.put("inTime", System.currentTimeMillis());
        attendanceObj.put("outTime", "");
        attendanceObj.put("status", "IN");
        attendanceObj.put("userId",userId);
        attendanceObj.put("userName",userName);

        String[] splitSupervisorIds = getSupervisorIds(context).split("/");

        for(String ids :splitSupervisorIds) {
            if (!ids.isEmpty())
                attendanceObj.put(ids, true);
        }

        db.collection(LocationConstants.FIRESTORE_BASE_PATH)
                .document("Attendance")
                .collection(SDUtil.now(SDUtil.DATE_DOB_FORMAT_PLAIN))
                .document(userId+"")
                .set(attendanceObj);

    }

    /**
     * Update Firebase Attendance OutTime and Status in Specified Node
     */
    @Override
    public void updateAttendanceOut(Context context, String pathNode) {

        String userId = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = String.valueOf(userMasterBO.getUserid());
        }

        Map<String, Object> attendanceObj = new HashMap<>();
        attendanceObj.put("outTime", System.currentTimeMillis());
        attendanceObj.put("status", "Day Closed");

        db.collection(LocationConstants.FIRESTORE_BASE_PATH)
                .document("Attendance")
                .collection(SDUtil.now(SDUtil.DATE_DOB_FORMAT_PLAIN))
                .document(userId)
                .update(attendanceObj);
    }

    /**
     * Insert or update Location data and attendance data in Firebase Node
     */
    private void updateFirebaseData(Context context, LocationDetailBO locationDetailBO, String nodePath) {
        int userId = 0 ;
        String userName = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = userMasterBO.getUserid();
            userName = String.valueOf(userMasterBO.getUserName());
        }

        Map<String, Object> locationObj = new HashMap<>();
        locationObj.put("userId",userId);
        locationObj.put("userName",userName);
        locationObj.put("latitude",Double.valueOf(locationDetailBO.getLatitude()));
        locationObj.put("longitude",Double.valueOf(locationDetailBO.getLongitude()));
        locationObj.put("accuracy",Double.valueOf(locationDetailBO.getAccuracy()));
        locationObj.put("batterStatus",locationDetailBO.getBatteryStatus());
        locationObj.put("gpsEnabled",locationDetailBO.isGpsEnabled());
        locationObj.put("mockLocationEnabled",locationDetailBO.isMockLocationEnabled());
        locationObj.put("activityType",locationDetailBO.getActivityType());
        locationObj.put("time",System.currentTimeMillis());

        String[] splitSupervisorIds = getSupervisorIds(context).split("/");

        for(String ids :splitSupervisorIds)
            if (!ids.isEmpty())
                locationObj.put(ids,true);

        db.collection(LocationConstants.FIRESTORE_BASE_PATH)
                .document(nodePath)
                .collection(SDUtil.now(SDUtil.DATE_DOB_FORMAT_PLAIN))
                .document(userId+"")
                .set(locationObj);
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

//                userMasterBO.setUserid(8);
//                userMasterBO.setUserName("Mansoor");

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
            } else
                supervisorIds = new StringBuilder();

            supervisorIds.append("1").append("/").append("3").append("/").append("4");

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return supervisorIds.toString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FireBaseRealtimeLocationUpload createFromParcel(Parcel in) {
            return new FireBaseRealtimeLocationUpload(in);
        }

        public FireBaseRealtimeLocationUpload[] newArray(int size) {
            return new FireBaseRealtimeLocationUpload[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
