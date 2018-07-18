package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class SellerMapViewPresenter implements SellerMapViewContractor.SellerViewPresenter {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference queryRef = db
            .collection("activity_tracking_v2");
    private ListenerRegistration registration ;
    private boolean isRealTimeLocationOn = false;
    private Context context;
    private SellerMapViewContractor.SellerMapView sellerMapView;
    private SupervisorModelBo supervisorModelBo;


    private int outletCount = 0;
    private boolean isZoomed = false;

    @Override
    public void setView(SellerMapViewContractor.SellerMapView sellerMapView, Context context) {
        this.sellerMapView = sellerMapView;
        this.context = context;
    }

    @Override
    public void getSellerInfoAWS(int userId) {
        DBUtil db = null;
        supervisorModelBo = new SupervisorModelBo();
        ArrayList<SupervisorModelBo> sellerInfoArrayList = new ArrayList<>();
        SparseArray<SupervisorModelBo> sellerDetails = new SparseArray<>();
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select UM.userId,UM.userName,SRM.retailerId,SRM.retailerName,SRM.sequence,SRM.latitude,SRM.longitude from " +
                    "usermaster UM inner join SupRetailerMaster SRM on SRM.sellerId = UM.userid  where UM.isDeviceuser!=1 and UM.userid ='"+userId+"' order by SRM.sequence ASC";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {

                    supervisorModelBo.setUserId(c.getInt(0));
                    supervisorModelBo.setUserName(c.getString(1));

                    SupervisorModelBo sellerDetailBo = new SupervisorModelBo();
                    sellerDetailBo.setUserId(c.getInt(0));
                    sellerDetailBo.setUserName(c.getString(1));
                    sellerDetailBo.setRetailerId(c.getInt(2));
                    sellerDetailBo.setRetailerName(c.getString(3));
                    sellerDetailBo.setSequence(c.getInt(4));
                    sellerDetailBo.setLatitude(c.getDouble(5));
                    sellerDetailBo.setLongitude(c.getDouble(6));

                    if(sellerDetailBo.getLatitude() != 0 && sellerDetailBo.getLongitude() != 0) {
                        LatLng destLatLng = new LatLng(sellerDetailBo.getLatitude(), sellerDetailBo.getLongitude());

                        sellerDetailBo.setMarkerOptions(new MarkerOptions()
                                .flat(true)
                                .title(sellerDetailBo.getRetailerName())
                                .position(destLatLng)
                                .snippet(String.valueOf(sellerDetailBo.getRetailerId())));

                        sellerDetailBo.setMarkerOptions(sellerDetailBo.getMarkerOptions());

                        sellerMapView.setRetailerMarker(sellerDetailBo);
                    }

//                    ArrayList<SupervisorModelBo> sellerDetailList = new ArrayList<>();
//                    sellerDetailList.add(sellerDetailBo);//


                    sellerInfoArrayList.add(sellerDetailBo);
                }
                c.close();
            }

            db.closeDB();

            supervisorModelBo.setSellerDetailArrayList(sellerInfoArrayList);

            supervisorModelBo.setTarget(sellerInfoArrayList.size());
            sellerMapView.updateSellerInfo(supervisorModelBo);
            sellerMapView.setOutletListAdapter(supervisorModelBo);

            getMarkerForFocus();

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void getSellerActivityListener(int userId) {

        DocumentReference documentReference = db
                .collection("activity_tracking_v2")
                .document("retailer_time_stamp")
                .collection("07052018")
                .document("7");

        String dateString = "07052018";
        registration = documentReference
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot!=null) {
                            setSellerActivityValues(documentSnapshot);
                        }
                    }
                });
    }

    private void setSellerActivityValues(DocumentSnapshot document) {

        SupervisorModelBo supervisorDocumentbo = document.toObject((SupervisorModelBo.class));

        if(supervisorDocumentbo != null) {
            supervisorModelBo.setBilled(supervisorDocumentbo.getBilled());
            supervisorModelBo.setOrderValue(supervisorDocumentbo.getOrderValue());
            supervisorModelBo.setRetailerId(supervisorDocumentbo.getRetailerId());
            supervisorModelBo.setRetailerName(supervisorDocumentbo.getRetailerName());
            supervisorModelBo.setTimeIn(supervisorDocumentbo.getTimeIn());
            supervisorModelBo.setTimeOut(supervisorDocumentbo.getTimeOut());
            supervisorModelBo.setCovered(supervisorDocumentbo.getCovered());

            sellerMapView.updateSellerInfo(supervisorModelBo);
        }

    }

    @Override
    public void getSellerActivityDetailListener(int userId) {

        String dateString = "07052018";
        registration = queryRef
                .document("retailer_time_stamp")
                .collection(dateString)
                .document(userId+"")
                .collection("details")
                .orderBy("timeIn", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots!=null) {

                            outletCount = queryDocumentSnapshots.size();

                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (snapshot.getType()) {
                                    case ADDED:
                                        setSellerDetailValues(snapshot.getDocument());
                                        break;
                                }
                            }
                        }
                    }
                });
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
    public void realtimeLocationInfoListener(){
        registration = queryRef
                .document("movement_tracking")
                .collection("07102018")
                .whereEqualTo("4",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots != null) {

                            System.out.println("updateRealtimeLocationInfoListener = " );

                            for (DocumentChange snapshot : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (snapshot.getType()) {
                                    case ADDED:
//                                        setLocationValues(snapshot.getDocument());
                                        break;
                                    case MODIFIED:
//                                        setLocationValues(snapshot.getDocument());
                                        break;
                                }
                            }
                        }

                    }
                });
    }

    @Override
    public void getMarkerForFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i< supervisorModelBo.getSellerDetailArrayList().size(); i++) {
            if(supervisorModelBo.getSellerDetailArrayList().get(i) != null
                    && supervisorModelBo.getSellerDetailArrayList().get(i).getMarkerOptions().getPosition().latitude > 0
                    && supervisorModelBo.getSellerDetailArrayList().get(i).getMarkerOptions().getPosition().longitude > 0)
                builder.include(supervisorModelBo.getSellerDetailArrayList().get(i).getMarkerOptions().getPosition());
        }

        sellerMapView.focusMarker(builder);
    }

    @Override
    public void getSellerList(int sellerInfoType) {

    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot){

        SupervisorModelBo documentSnapshotBo = documentSnapshot.toObject((SupervisorModelBo.class));

        System.out.println("setSellerDetailValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if(documentSnapshotBo != null){
            LatLng destLatLng = new LatLng(documentSnapshotBo.getLatitude(), supervisorModelBo.getLongitude());
            for(SupervisorModelBo supervisorModelBo : supervisorModelBo.getSellerDetailArrayList()) {
                if (supervisorModelBo.getRetailerId() == documentSnapshotBo.getRetailerId()){
                    supervisorModelBo.setOrderValue(documentSnapshotBo.getOrderValue()!=null?documentSnapshotBo.getOrderValue():0);
                    supervisorModelBo.setOrdered(documentSnapshotBo.isOrdered());
                    supervisorModelBo.setTimeIn(documentSnapshotBo.getTimeIn()!=null?documentSnapshotBo.getTimeIn():0);
                    supervisorModelBo.setTimeOut(documentSnapshotBo.getTimeOut()!=null?documentSnapshotBo.getTimeOut():0);

                    if(supervisorModelBo.getLongitude() == 0 && supervisorModelBo.getLongitude() == 0) {

                        supervisorModelBo.setMarkerOptions(new MarkerOptions()
                                .flat(true)
                                .title(supervisorModelBo.getRetailerName())
                                .position(destLatLng)
                                .snippet(String.valueOf(supervisorModelBo.getRetailerId())));

                        sellerMapView.setRetailerMarker(supervisorModelBo);
                    }

                }
            }

            SupervisorModelBo supervisorModelObj = new SupervisorModelBo();

            supervisorModelObj.setBilled(documentSnapshotBo.getBilled());
            supervisorModelObj.setCovered(documentSnapshotBo.getCovered());
            supervisorModelObj.setLatitude(documentSnapshotBo.getLatitude());
            supervisorModelObj.setLongitude(documentSnapshotBo.getLongitude());
            supervisorModelObj.setOrderValue(documentSnapshotBo.getOrderValue()!=null?documentSnapshotBo.getOrderValue():0);
            supervisorModelObj.setOrdered(documentSnapshotBo.isOrdered());
            supervisorModelObj.setTimeIn(documentSnapshotBo.getTimeIn()!=null?documentSnapshotBo.getTimeIn():0);
            supervisorModelObj.setTimeOut(documentSnapshotBo.getTimeOut()!=null?documentSnapshotBo.getTimeOut():0);
            supervisorModelObj.setRetailerId(documentSnapshotBo.getRetailerId());
            supervisorModelObj.setRetailerName(documentSnapshotBo.getRetailerName()!=null?documentSnapshotBo.getRetailerName():"");

            if(supervisorModelBo.getSellerDetailsList().get(documentSnapshotBo.getRetailerId()) != null) {

                supervisorModelBo.getSellerDetailsList().get(documentSnapshotBo.getRetailerId()).add(supervisorModelObj);

            }else {
                ArrayList<SupervisorModelBo> supervisorModelBos = new ArrayList<>();
                supervisorModelBos.add(supervisorModelObj);
                supervisorModelBo.getSellerDetailsList().put(documentSnapshotBo.getRetailerId(), supervisorModelBos);
            }
        }

        sellerMapView.setOutletListAdapter(supervisorModelBo);
    }
}
