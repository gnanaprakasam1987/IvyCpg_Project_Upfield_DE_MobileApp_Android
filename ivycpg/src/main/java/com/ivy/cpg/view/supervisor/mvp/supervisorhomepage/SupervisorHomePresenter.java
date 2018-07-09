package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.cpg.view.supervisor.mvp.supervisorhomepage.SupervisorHomeContract.SupervisorHomeView;

import java.util.HashMap;

import javax.annotation.Nullable;

public class SupervisorHomePresenter implements SupervisorHomeContract.SupervisorHomePresenter{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String dateString = "07052018";

    private CollectionReference queryRef = db
            .collection("activity_tracking_v2")
            .document("retailer_time_stamp")
            .collection(dateString);

    private HashMap<Integer, SupervisorModelBo> sellerInfoHasMap = new HashMap<>();

    @Override
    public void loginToFirebase(final Context context,final SupervisorHomeView supervisorHomeView) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
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
        }else{
            getSellerUpdatesFirebase(supervisorHomeView);
        }
    }

    @Override
    public void getSellerMarkerInfo(SupervisorModelBo supervisorModelBo) {

    }

    @Override
    public void getSellerCount(Context context) {

    }

    @Override
    public void getSellerUpdatesFirebase(final SupervisorHomeView supervisorHomeView) {
        queryRef
            .whereEqualTo("4",true)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    System.out.println("collectionSupervisorVal base size = " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                        snapshot.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                                System.out.println("documentSnapshot = " + documentSnapshot);

                                setValues(documentSnapshot,supervisorHomeView);
                            }
                        });

                    }
                }
            });
    }

    private void setValues(DocumentSnapshot documentSnapshot,SupervisorHomeView supervisorHomeView){

        SupervisorModelBo detailsBo = documentSnapshot.toObject((SupervisorModelBo.class));
        if (detailsBo != null) {
            sellerInfoHasMap.put(detailsBo.getUserId(), detailsBo);
            supervisorHomeView.updateSellerFirebaseInfo(detailsBo);
        }
    }
}
