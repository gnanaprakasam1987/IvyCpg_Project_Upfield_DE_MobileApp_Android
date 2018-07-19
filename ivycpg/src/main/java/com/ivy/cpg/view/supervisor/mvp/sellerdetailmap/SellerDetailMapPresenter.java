package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

public class SellerDetailMapPresenter implements SellerDetailMapContractor.SellerDetailMapPresenter {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;
    private boolean isRealTimeLocationOn = false;
    private Context context;
    private SellerDetailMapContractor.SellerDetailMapView sellerMapView;

    private SparseArray<ArrayList<SupervisorModelBo>> retailerVisitDetailsByRId = new SparseArray<>();
    private ArrayList<SupervisorModelBo> retailerMasterList = new ArrayList<>();

    //Maintaining previous id not to draw route for same retailer continuously received
    private int previousRetailerId;
    private LatLng previousRetailerLatLng;

    @Override
    public void setView(SellerDetailMapContractor.SellerDetailMapView sellerMapView, Context context) {
        this.sellerMapView = sellerMapView;
        this.context = context;
    }

    @Override
    public void getSellerInfoAWS(int userId) {
        DBUtil db = null;
        SupervisorModelBo supervisorModelBo = new SupervisorModelBo();

        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select UM.userId,UM.userName,SRM.retailerId,SRM.retailerName,SRM.sequence,SRM.latitude,SRM.longitude from " +
                    "usermaster UM inner join SupRetailerMaster SRM on SRM.sellerId = UM.userid  where UM.isDeviceuser!=1 and UM.userid ='" + userId + "' order by SRM.sequence ASC";

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
                    sellerDetailBo.setMasterSequence(c.getInt(4));
                    sellerDetailBo.setLatitude(c.getDouble(5));
                    sellerDetailBo.setLongitude(c.getDouble(6));

                    if (sellerDetailBo.getLatitude() != 0 && sellerDetailBo.getLongitude() != 0) {
                        LatLng destLatLng = new LatLng(sellerDetailBo.getLatitude(), sellerDetailBo.getLongitude());

                        sellerDetailBo.setMarkerOptions(new MarkerOptions()
                                .flat(true)
                                .title(sellerDetailBo.getRetailerName())
                                .position(destLatLng)
                                .snippet(String.valueOf(sellerDetailBo.getRetailerId())));

                        sellerDetailBo.setMarkerOptions(sellerDetailBo.getMarkerOptions());

                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_grey);
                        sellerMapView.setRetailerMarker(sellerDetailBo,icon);
                    }

                    retailerMasterList.add(sellerDetailBo);
                }
                c.close();
            }

            db.closeDB();

            supervisorModelBo.setTarget(retailerMasterList.size());
            sellerMapView.updateSellerInfo(supervisorModelBo);
            sellerMapView.setOutletListAdapter(retailerMasterList);

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
                .document(userId+"");

        registration = documentReference
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            setSellerActivityValues(documentSnapshot);
                        }
                    }
                });
    }

    @Override
    public void getSellerActivityDetailListener(int userId) {

        CollectionReference queryRef = db
                .collection("activity_tracking_v2")
                .document("retailer_time_stamp")
                .collection("07052018")
                .document(userId + "")
                .collection("details");

        registration = queryRef
                .orderBy("timeIn", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
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
    public void isRealtimeLocation() {

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
    public void realtimeLocationInfoListener(int userId) {

        CollectionReference queryRef = db
                .collection("activity_tracking_v2")
                .document("movement_tracking")
                .collection("07102018");

        registration = queryRef
                .whereEqualTo(userId+"", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots != null) {

                            System.out.println("updateRealtimeLocationInfoListener = ");

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

        for (int i = 0; i < retailerMasterList.size(); i++) {
            if (retailerMasterList.get(i) != null
                    && retailerMasterList.get(i).getMarkerOptions().getPosition().latitude > 0
                    && retailerMasterList.get(i).getMarkerOptions().getPosition().longitude > 0)
                builder.include(retailerMasterList.get(i).getMarkerOptions().getPosition());
        }

        sellerMapView.focusMarker(builder);
    }

    @Override
    public String convertMillisToTime(Long time) {

        if (time != null && time != 0) {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("hh:mm a", Locale.US);
            return format.format(date);
        } else
            return "";
    }

    @Override
    public void drawRoute(ArrayList<LatLng> points) {
        sellerMapView.drawRoute(points);
    }

    @Override
    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    private void setSellerActivityValues(DocumentSnapshot document) {

        SupervisorModelBo supervisorDocumentbo = new SupervisorModelBo();

        if (document.getData() != null) {
            supervisorDocumentbo.setTimeIn((long)document.getData().get("timeIn"));
            supervisorDocumentbo.setTimeOut((long)document.getData().get("timeOut"));
            supervisorDocumentbo.setRetailerName((String)document.getData().get("retailerName"));
            supervisorDocumentbo.setCovered((int)(long)document.getData().get("covered"));
            sellerMapView.updateSellerInfo(supervisorDocumentbo);
        }

    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        SupervisorModelBo documentSnapshotBo = documentSnapshot.toObject((SupervisorModelBo.class));

        System.out.println("setSellerDetailValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if (documentSnapshotBo != null) {
            LatLng destLatLng = new LatLng(documentSnapshotBo.getLatitude(), documentSnapshotBo.getLongitude());
            for (SupervisorModelBo supervisorModelBo : retailerMasterList) {

                if (supervisorModelBo.getRetailerId() == documentSnapshotBo.getRetailerId()) {

                    BitmapDescriptor icon;
                    if(supervisorModelBo.getIsOrdered() || documentSnapshotBo.getIsOrdered()) {
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
                        supervisorModelBo.setIsOrdered(true);
                    }
                    else {
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
                        supervisorModelBo.setIsOrdered(documentSnapshotBo.getIsOrdered());
                    }

                    supervisorModelBo.setOrderValue(documentSnapshotBo.getOrderValue() != null ? documentSnapshotBo.getOrderValue() : 0);
                    supervisorModelBo.setTimeIn(documentSnapshotBo.getTimeIn() != null ? documentSnapshotBo.getTimeIn() : 0);
                    supervisorModelBo.setTimeOut(documentSnapshotBo.getTimeOut() != null ? documentSnapshotBo.getTimeOut() : 0);

                    if (supervisorModelBo.getLongitude() == 0 && supervisorModelBo.getLongitude() == 0) {

                        supervisorModelBo.setMarkerOptions(new MarkerOptions()
                                .flat(true)
                                .title(supervisorModelBo.getRetailerName() + "//" + supervisorModelBo.getTimeIn())
                                .position(destLatLng)
                                .snippet(String.valueOf(supervisorModelBo.getRetailerId())));


                        sellerMapView.setRetailerMarker(supervisorModelBo,icon);
                    } else {

                        String title = supervisorModelBo.getRetailerName() + "//" + supervisorModelBo.getTimeIn();

                        MarkerOptions markerOptions = supervisorModelBo.getMarkerOptions();
                        markerOptions.title(title);

                        supervisorModelBo.getMarker().setTitle(title);
                        supervisorModelBo.getMarker().setIcon(icon);

                        destLatLng = markerOptions.getPosition();
                    }

                }
            }

            SupervisorModelBo supervisorModelObj = new SupervisorModelBo();

            supervisorModelObj.setBilled(documentSnapshotBo.getBilled());
            supervisorModelObj.setCovered(documentSnapshotBo.getCovered());
            supervisorModelObj.setLatitude(documentSnapshotBo.getLatitude());
            supervisorModelObj.setLongitude(documentSnapshotBo.getLongitude());
            supervisorModelObj.setOrderValue(documentSnapshotBo.getOrderValue() != null ? documentSnapshotBo.getOrderValue() : 0);
            supervisorModelObj.setIsOrdered(documentSnapshotBo.getIsOrdered());
            supervisorModelObj.setTimeIn(documentSnapshotBo.getTimeIn() != null ? documentSnapshotBo.getTimeIn() : 0);
            supervisorModelObj.setTimeOut(documentSnapshotBo.getTimeOut() != null ? documentSnapshotBo.getTimeOut() : 0);
            supervisorModelObj.setRetailerId(documentSnapshotBo.getRetailerId());
            supervisorModelObj.setRetailerName(documentSnapshotBo.getRetailerName() != null ? documentSnapshotBo.getRetailerName() : "");

            if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {

                retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(supervisorModelObj);

            } else {
                ArrayList<SupervisorModelBo> supervisorModelBos = new ArrayList<>();
                supervisorModelBos.add(supervisorModelObj);
                retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), supervisorModelBos);
            }

            sellerMapView.setOutletListAdapter(retailerMasterList);

            if (previousRetailerId != 0 && previousRetailerId != documentSnapshotBo.getRetailerId()){
                fetchRouteUrl(previousRetailerLatLng,destLatLng);
            }

            previousRetailerId = documentSnapshotBo.getRetailerId();
            previousRetailerLatLng = destLatLng;
        }
    }

    private void fetchRouteUrl(LatLng startlatLng,LatLng endLatLng){

        String url = getUrl(startlatLng,endLatLng);
        Commons.print("drawRoute "+ url);
        FetchUrl fetchUrl = new FetchUrl(this);
        fetchUrl.execute(url);
    }

    private String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        String sensor = "sensor=false&units=metric";
        String alternatives = "alternatives=false&mode=driving";
        String mapKey = "key="+context.getString(R.string.google_maps_api_key);
        String parameters = str_origin + "&" + str_dest + "&" + alternatives+"&"+mapKey;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }
}
