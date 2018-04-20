package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ivy.ivyretail.service.BackgroundServiceHelper;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;

public class FireBaseRealtimeLocationUpload implements RealTimeLocation {

    /*Uploads Location details to Firebase*/
    @Override
    public void onRealTimeLocationReceived(LocationDetailBO locationDetailBO,Context context) {

        String userId = "",userName = "";
        UserMasterBO userMasterBO = BackgroundServiceHelper.getInstance(context).getUserDetail(context);
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
                    Commons.print("Service firebase onComplete: Failed=" + task.getException().getMessage());
                }
            }
        });
    }
}
