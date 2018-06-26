package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.supervisorhomepage.SupervisorHomeContract.SupervisorHomeView;

import java.util.HashMap;

public class SupervisorHomePresenter implements SupervisorHomeContract.SupervisorHomePresenter{

    @Override
    public void loginToFirebase(final Context context,final SupervisorHomeView supervisorHomeView) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || FirebaseDatabase.getInstance() == null) {
            String email = LocationConstants.FIREBASE_EMAIL;
            String password = LocationConstants.FIREBASE_PASSWORD;
            // Authenticate with Firebase and subscribe to updates

            if(email.trim().length() > 0 && password.trim().length() > 0) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getSellerUpdatesFirebase(supervisorHomeView);
                        } else {
                            supervisorHomeView.firebaseLoginFailure();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void getSellerMarkerInfo(DetailsBo detailsBo) {

    }

    @Override
    public void getSellerCount(Context context) {

    }

    @Override
    public void getSellerUpdatesFirebase(final SupervisorHomeView supervisorHomeView) {

    }

    public void setMarker(DataSnapshot dataSnapshot) {

        String key = dataSnapshot.getKey();
        HashMap<String, DetailsBo> sellerInfoHasMap = new HashMap<>();

        if(dataSnapshot.getValue() != null) {

            for (DataSnapshot child : dataSnapshot.getChildren()) {
                DetailsBo detailsBo = child.getValue(DetailsBo.class);
                sellerInfoHasMap.put(child.getKey(),detailsBo);
            }

//            setMarkerValuesInObj();
        }
    }
}
