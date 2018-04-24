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

    /*Firebase Authentication Method*/
    public FireBaseRealtimeLocationUpload(Context context) {
        // Authenticate with Firebase, and request location updates
        String email = context.getString(R.string.firebase_email);
        String password = context.getString(R.string.firebase_password);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
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

    /*Uploads Location details to Firebase*/
    @Override
    public void onRealTimeLocationReceived(LocationDetailBO locationDetailBO,Context context) {

        String userId = "",userName = "";
        UserMasterBO userMasterBO = getUserDetail(context);
        if(userMasterBO!=null){
            userId = String.valueOf(userMasterBO.getUserid());
            userName = String.valueOf(userMasterBO.getUserName());
        }

        final String path = context.getString(R.string.firebase_path) + "/" + userId;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        locationDetailBO.setUserId(userId);
        locationDetailBO.setUserName(userName);
        ref.setValue(locationDetailBO);

    }

    /**
     * Get User data from database
     */
    private UserMasterBO getUserDetail(Context context){
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
}
