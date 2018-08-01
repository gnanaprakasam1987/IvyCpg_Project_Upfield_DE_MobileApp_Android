package com.ivy.cpg.view.supervisor.mvp.outletmapview;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.DETAIL_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIRESTORE_BASE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;

public class OutletMapViewPresenter  implements OutletMapViewContractor.OutletMapPresenter {

    private OutletMapViewContractor.OutletMapView outletMapView;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;

    private LinkedHashMap<Integer,ArrayList<RetailerBo>> retailerVisitDetailsByRId = new LinkedHashMap<>();

    private LinkedHashMap<Integer,RetailerBo> retailerMasterHashmap =  new LinkedHashMap<>();

    private ArrayList<RetailerBo> retailerList = new ArrayList<>();

    private int tabPosition;

    @Override
    public void setView(OutletMapViewContractor.OutletMapView outletMapView, Context context) {
        this.outletMapView = outletMapView;
        this.context = context;
    }

    @Override
    public void downloadOutletListAws() {
        retailerMasterHashmap = SupervisorActivityHelper.getInstance().getRetailerMasterHashmap();
    }

    @Override
    public void setTotalOutlet(){

        outletMapView.clearMap();
        retailerList.clear();
        for (RetailerBo retailerBo : retailerMasterHashmap.values()){
            retailerList.add(retailerBo);
            setMarker(retailerBo);
        }
        outletMapView.setOutletListAdapter(retailerList);

        //Focus all the retailer location in map
        getMarkerForFocus();
    }

    @Override
    public void setCoveredOutlet(){

        outletMapView.clearMap();
        retailerList.clear();
        for(RetailerBo retailerBo : retailerMasterHashmap.values()){
            if(retailerBo.getIsOrdered() || retailerBo.isVisited()) {
                retailerList.add(retailerBo);
                setMarker(retailerBo);
            }
        }

        outletMapView.setOutletListAdapter(retailerList);

        //Focus all the retailer location in map
        getMarkerForFocus();
    }

    @Override
    public void setUnbilledOutlet(){

        outletMapView.clearMap();
        retailerList.clear();
        for(RetailerBo retailerBo : retailerMasterHashmap.values()){
            if(!retailerBo.getIsOrdered() && retailerBo.isVisited()) {
                retailerList.add(retailerBo);
                setMarker(retailerBo);
            }
        }

        outletMapView.setOutletListAdapter(retailerList);

        //Focus all the retailer location in map
        getMarkerForFocus();
    }

    @Override
    public void setOutletActivityDetail(int userId, String date) {
        Query queryRef = db
                .collection(FIRESTORE_BASE_PATH)
                .document(TIME_STAMP_PATH)
                .collection(date)
                .whereEqualTo(userId+"",true)
                ;

        queryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot snapshot : task.getResult()){
                    snapshot.getReference()
                            .collection(DETAIL_PATH)
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
            }
        });


    }

    @Override
    public void getMarkerForFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (RetailerBo retailerBo : retailerList) {
            if (retailerBo.getMasterLatitude() > 0 && retailerBo.getMasterLatitude() > 0) {
                LatLng builderLatLng = new LatLng(retailerBo.getMasterLatitude(),retailerBo.getMasterLongitude());
                builder.include(builderLatLng);
            }
        }

        outletMapView.focusMarker(builder);
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
    public String calculateDuration(long startTime,long endTime){

        String duratingStr = (String) DateUtils.getRelativeTimeSpanString(startTime, endTime, 0);

        duratingStr = duratingStr.replace("ago","");

        return duratingStr;
    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

        if (documentSnapshotBo != null) {

            //Update retailer info in master list

            RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

            if(retailerMasterBo.getIsOrdered() || documentSnapshotBo.getOrderValue() > 0) {
                retailerMasterBo.setIsOrdered(true);
            }
            else {
                retailerMasterBo.setIsOrdered(false);
            }

            documentSnapshotBo.setIsOrdered(retailerMasterBo.getIsOrdered());
            retailerMasterBo.setSkipped(false);
            retailerMasterBo.setVisited(true);

            long totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
            retailerMasterBo.setTotalOrderValue(totalOrderValue);

            retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
            retailerMasterBo.setTimeIn(documentSnapshotBo.getTimeIn());
            retailerMasterBo.setTimeOut(documentSnapshotBo.getTimeOut());

            if (retailerMasterBo.getMasterLatitude() == 0 || retailerMasterBo.getMasterLongitude() == 0) {

                retailerMasterBo.setMasterLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setMasterLongitude(documentSnapshotBo.getLongitude());

                retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());

            } else {

                retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());
            }


            // Set Visited Retailer details in HashMap with retailer id as key

            RetailerBo retailerBoObj = new RetailerBo();

            retailerBoObj.setLatitude(documentSnapshotBo.getLatitude());
            retailerBoObj.setLongitude(documentSnapshotBo.getLongitude());
            retailerBoObj.setOrderValue(documentSnapshotBo.getOrderValue());
            retailerBoObj.setIsOrdered(documentSnapshotBo.getIsOrdered());
            retailerBoObj.setTimeIn(documentSnapshotBo.getTimeIn());
            retailerBoObj.setTimeOut(documentSnapshotBo.getTimeOut());
            retailerBoObj.setRetailerId(documentSnapshotBo.getRetailerId());
            retailerBoObj.setRetailerName(documentSnapshotBo.getRetailerName() != null ? documentSnapshotBo.getRetailerName() : "");

            if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {
                retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(retailerBoObj);
            } else {
                ArrayList<RetailerBo> visitedRetailerList = new ArrayList<>();
                visitedRetailerList.add(retailerBoObj);
                retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), visitedRetailerList);
            }

            //ends

            setTabMapValues();

        }
    }

    ArrayList<RetailerBo> getVisitedRetailers(){

        ArrayList<RetailerBo> retailerBos = new ArrayList<>();

        for(RetailerBo retailerBo : retailerList) {
            if(retailerVisitDetailsByRId.get(retailerBo.getRetailerId()) != null)
                retailerBos.add(retailerMasterHashmap.get(retailerBo.getRetailerId()));
        }

        return retailerBos;
    }

    ArrayList<RetailerBo> getRetailerVisitDetailsByRId(int userId) {
        return retailerVisitDetailsByRId.get(userId);
    }

    private int getTabPosition() {
        return tabPosition;
    }

    void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;

        setTabMapValues();
    }

    private void setTabMapValues(){

        switch (getTabPosition()) {
            case 0:
                setTotalOutlet();
                break;
            case 1:
                setCoveredOutlet();
                break;
            case 2:
                setUnbilledOutlet();
                break;
        }
    }

    private void setMarker(RetailerBo retailerBo){

        String title = retailerBo.getRetailerName() + "//" + retailerBo.getTimeIn();

        BitmapDescriptor icon;
        if(retailerBo.getIsOrdered() && retailerBo.isVisited())
            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
        else if(!retailerBo.getIsOrdered() && retailerBo.isVisited())
            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
        else
            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_grey);

        if (retailerBo.getMasterLatitude() != 0 && retailerBo.getMasterLongitude() != 0) {

            LatLng destLatLng = new LatLng(retailerBo.getMasterLatitude(), retailerBo.getMasterLongitude());

            MarkerOptions markerOptions = new MarkerOptions()
                    .flat(true)
                    .title(title)
                    .position(destLatLng)
                    .icon(icon)
                    .snippet(String.valueOf(retailerBo.getRetailerId()));

            outletMapView.setRetailerMarker(retailerBo,markerOptions);
        }
        else if (retailerBo.getLatitude() != 0 && retailerBo.getLongitude() != 0){
            LatLng destLatLng = new LatLng(retailerBo.getLatitude(), retailerBo.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions()
                    .flat(true)
                    .title(title)
                    .position(destLatLng)
                    .icon(icon)
                    .snippet(String.valueOf(retailerBo.getRetailerId()));

            outletMapView.setRetailerMarker(retailerBo,markerOptions);
        }

    }
}
