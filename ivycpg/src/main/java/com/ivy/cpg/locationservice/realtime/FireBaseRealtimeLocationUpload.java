package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;
import java.util.Map;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.ATTENDANCE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_EMAIL;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_PASSWORD;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIRESTORE_BASE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;

public class FireBaseRealtimeLocationUpload implements RealTimeLocation {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Firebase Authentication Method
     * Authenticate with Firebase, and request location updates
     */
    public FireBaseRealtimeLocationUpload() {

    }

    @Override
    public void validateLoginAndUpdate(final Context context, final String pathNode, final LocationDetailBO locationDetailBO, final String from){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {

            String email = FIREBASE_EMAIL;
            String password = FIREBASE_PASSWORD;

            if(email.trim().length() > 0 && password.trim().length() > 0) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Commons.print("Service Firebase Uth Success");

                            if (from.equalsIgnoreCase("AttendanceIn")){
                                updateAttendanceIn(context,pathNode);
                            }else if (from.equalsIgnoreCase("AttendanceOut")){
                                updateAttendanceOut(context,pathNode);
                            }else if (from.equalsIgnoreCase("Location")){
                                updateFirebaseData(context, locationDetailBO);
                            }

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

            if (from.equalsIgnoreCase("AttendanceIn")){
                updateAttendanceIn(context,pathNode);
            }else if (from.equalsIgnoreCase("AttendanceOut")){
                updateAttendanceOut(context,pathNode);
            }else if (from.equalsIgnoreCase("Location")){
                updateFirebaseData(context, locationDetailBO);
            }
        }
    }

    private FireBaseRealtimeLocationUpload(Parcel parcel) {

    }

    /**
     * Get Triggered when location received
     */
    @Override
    public void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context) {

        locationDetailBO.setOutTime("");
        locationDetailBO.setStatus("IN");

        validateLoginAndUpdate(context,"", locationDetailBO,"Location");

    }

    /**
     * Update Firebase Attendance InTime and Status in Specified Node
     */
    @Override
    public void updateAttendanceIn(final Context context, String pathNode) {
        int userId = 0;
        String userName = "";
        String parentPositionIds="";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = userMasterBO.getUserid();
            userName = String.valueOf(userMasterBO.getUserName());
            parentPositionIds = userMasterBO.getBackupSellerID(); // Getting Parent Position ids
        }

        Map<String, Object> attendanceObj = new HashMap<>();
        attendanceObj.put("inTime", System.currentTimeMillis());
        attendanceObj.put("outTime", "");
        attendanceObj.put("status", "IN");
        attendanceObj.put("userId",userId);
        attendanceObj.put("userName",userName);

        String[] splitSupervisorIds = parentPositionIds.split("/");

        for(String ids :splitSupervisorIds) {
            if (!ids.isEmpty())
                attendanceObj.put(ids, true);
        }

        db.collection(FIRESTORE_BASE_PATH)
                .document(ATTENDANCE_PATH)
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

        db.collection(FIRESTORE_BASE_PATH)
                .document(ATTENDANCE_PATH)
                .collection(SDUtil.now(SDUtil.DATE_DOB_FORMAT_PLAIN))
                .document(userId)
                .update(attendanceObj);
    }

    /**
     * Insert or update Location data and attendance data in Firebase Node
     */
    private void updateFirebaseData(Context context, LocationDetailBO locationDetailBO) {
        int userId = 0 ;
        String userName = "";
        String parentPositionIds="";
        UserMasterBO userMasterBO = getUserDetail(context);
        if (userMasterBO != null) {
            userId = userMasterBO.getUserid();
            userName = String.valueOf(userMasterBO.getUserName());
            parentPositionIds = userMasterBO.getBackupSellerID(); // Getting Parent Position ids
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

        String[] splitSupervisorIds = parentPositionIds.split("/");

        for(String ids :splitSupervisorIds)
            if (!ids.isEmpty())
                locationObj.put(ids,true);

        db.collection(FIRESTORE_BASE_PATH)
                .document(REALTIME_LOCATION_PATH)
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

            Cursor cursor = db.selectSQL("select userid,username,parentpositionids from usermaster where isDeviceuser=1");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                userMasterBO = new UserMasterBO();
                userMasterBO.setUserid(cursor.getInt(0));
                userMasterBO.setUserName(cursor.getString(1));
                userMasterBO.setBackupSellerID(cursor.getString(2)); //Storing Parent Position Ids

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
