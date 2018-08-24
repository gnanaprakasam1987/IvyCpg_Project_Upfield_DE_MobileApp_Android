package com.ivy.cpg.view.supervisor.mvp.outletmapview;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

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

    private HashMap<Integer, Integer> mSelectedIdByChannelId = new HashMap<>();

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
                    registration = snapshot.getReference()
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
    public void setTotalOutlet(){

        outletMapView.clearMap();
        retailerList.clear();

        for (RetailerBo retailerBo : retailerMasterHashmap.values()){
            if(mSelectedIdByChannelId.size() > 0){
                if(( isChannelFilterClicked() && isProductFilterClicked())){

                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null && checkProductAvail(retailerBo)) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(!isChannelFilterClicked() && isProductFilterClicked()){

                    if(checkProductAvail(retailerBo)) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(isChannelFilterClicked() && !isProductFilterClicked()){
                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null) {

                    retailerList.add(retailerBo);
                    setMarker(retailerBo);
                }

                }
            }else{
                retailerList.add(retailerBo);
                setMarker(retailerBo);
            }
        }
        outletMapView.setOutletListAdapter(retailerList);

        //Focus all the retailer location in map
        getMarkerForFocus();
    }

    @Override
    public void setCoveredOutlet(){

        outletMapView.clearMap();
        retailerList.clear();

        for (RetailerBo retailerBo : retailerMasterHashmap.values()){
            if(mSelectedIdByChannelId.size() > 0){
                if(( isChannelFilterClicked() && isProductFilterClicked())){

                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null
                            && checkProductAvail(retailerBo) && (retailerBo.getIsOrdered() || retailerBo.isVisited()) ) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(!isChannelFilterClicked() && isProductFilterClicked()){

                    if(checkProductAvail(retailerBo) && (retailerBo.getIsOrdered() || retailerBo.isVisited())) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(isChannelFilterClicked() && !isProductFilterClicked()){
                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null && (retailerBo.getIsOrdered() || retailerBo.isVisited())) {

                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
            }else{
                if (retailerBo.getIsOrdered() || retailerBo.isVisited()) {
                    retailerList.add(retailerBo);
                    setMarker(retailerBo);
                }
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

        for (RetailerBo retailerBo : retailerMasterHashmap.values()){
            if(mSelectedIdByChannelId.size() > 0){
                if(( isChannelFilterClicked() && isProductFilterClicked())){

                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null && checkProductAvail(retailerBo)
                            && (!retailerBo.getIsOrdered() && retailerBo.isVisited())) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(!isChannelFilterClicked() && isProductFilterClicked()){

                    if(checkProductAvail(retailerBo) && (!retailerBo.getIsOrdered() && retailerBo.isVisited())) {
                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
                else if(isChannelFilterClicked() && !isProductFilterClicked()){
                    if(mSelectedIdByChannelId.get(retailerBo.getChannelId()) != null && (!retailerBo.getIsOrdered() && retailerBo.isVisited())) {

                        retailerList.add(retailerBo);
                        setMarker(retailerBo);
                    }

                }
            }else{
                if(!retailerBo.getIsOrdered() && retailerBo.isVisited()) {
                    retailerList.add(retailerBo);
                    setMarker(retailerBo);
                }
            }
        }

        outletMapView.setOutletListAdapter(retailerList);

        //Focus all the retailer location in map
        getMarkerForFocus();
    }

    private boolean isProductFilterClicked(){

        if (mSelectedIdByChannelId.values().contains(1))
            return true;

        return false;
    }

    private boolean isChannelFilterClicked(){
        if (mSelectedIdByChannelId.values().contains(0))
            return true;

        return false;
    }

    private boolean checkProductAvail(RetailerBo retailerBo){
        for(Integer integer : retailerBo.getProductIds()) {
            if (mSelectedIdByChannelId.get(integer) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void getMarkerForFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean isFocus = false;

        for (RetailerBo retailerBo : retailerList) {
//            if (retailerBo.getLatitude() > 0 && retailerBo.getLongitude() > 0) {
//                LatLng builderLatLng = new LatLng(retailerBo.getLatitude(),retailerBo.getLongitude());
//                builder.include(builderLatLng);
//                isFocus = true;
//            }else
                if (retailerBo.getMasterLatitude() > 0 && retailerBo.getMasterLatitude() > 0) {
                LatLng builderLatLng = new LatLng(retailerBo.getMasterLatitude(),retailerBo.getMasterLongitude());
                builder.include(builderLatLng);
                isFocus = true;
            }
        }

        if (isFocus)
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

    public void removeFirestoreListener() {
        if(registration != null)
            registration.remove();
    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        try {
            RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

            if (documentSnapshotBo != null) {

                //Update retailer info in master list
                RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

                if(retailerMasterBo != null) {

                    if (retailerMasterBo.getIsOrdered() || documentSnapshotBo.getOrderValue() > 0) {
                        retailerMasterBo.setIsOrdered(true);
                    } else {
                        retailerMasterBo.setIsOrdered(false);
                    }

                    documentSnapshotBo.setIsOrdered(retailerMasterBo.getIsOrdered());
                    retailerMasterBo.setSkipped(false);
                    retailerMasterBo.setVisited(true);

                    long totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
                    retailerMasterBo.setTotalOrderValue(totalOrderValue);

                    retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
                    retailerMasterBo.setInTime(documentSnapshotBo.getInTime());
                    retailerMasterBo.setOutTime(documentSnapshotBo.getOutTime());

                    if (retailerMasterBo.getMasterLatitude() == 0 || retailerMasterBo.getMasterLongitude() == 0) {

                        retailerMasterBo.setMasterLatitude(documentSnapshotBo.getLatitude());
                        retailerMasterBo.setMasterLongitude(documentSnapshotBo.getLongitude());

                        retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                        retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());

                    } else {

                        retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                        retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());
                    }

                    try {
                        Set<Integer> ids = retailerMasterBo.getProductIds();
                        String[] productids = documentSnapshotBo.getParentHierarchy().split("/");
                        for (String id : productids) {
                            if (!id.trim().equals(""))
                                ids.add(SDUtil.convertToInt(id));

                        }
                        retailerMasterBo.setProductIds(ids);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    // Set Visited Retailer details in HashMap with retailer id as key

                    RetailerBo retailerBoObj = new RetailerBo();

                    retailerBoObj.setLatitude(documentSnapshotBo.getLatitude());
                    retailerBoObj.setLongitude(documentSnapshotBo.getLongitude());
                    retailerBoObj.setOrderValue(documentSnapshotBo.getOrderValue());
                    retailerBoObj.setIsOrdered(documentSnapshotBo.getIsOrdered());
                    retailerBoObj.setInTime(documentSnapshotBo.getInTime());
                    retailerBoObj.setOutTime(documentSnapshotBo.getOutTime());
                    retailerBoObj.setRetailerId(documentSnapshotBo.getRetailerId());
                    retailerBoObj.setRetailerName(documentSnapshotBo.getRetailerName() != null ? documentSnapshotBo.getRetailerName() : "");
                    retailerBoObj.setParentHierarchy(documentSnapshotBo.getParentHierarchy());

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
        }catch (Exception e){
            Commons.printException(e);
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

    void channelFilterIds(HashMap<Integer, Integer> channelIds){
        mSelectedIdByChannelId = channelIds;
    }

    private void setMarker(RetailerBo retailerBo){

        String title = retailerBo.getRetailerName() + "//" + retailerBo.getInTime();

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

    String convertPlaneDateToGlobal(String planeDate){
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            Date date = sdf.parse(planeDate);

            sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
            planeDate =sdf.format(date);

            return planeDate;

        }catch(Exception e){
            Commons.printException(e);
        }

        return planeDate;
    }

    boolean areaBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }
}
