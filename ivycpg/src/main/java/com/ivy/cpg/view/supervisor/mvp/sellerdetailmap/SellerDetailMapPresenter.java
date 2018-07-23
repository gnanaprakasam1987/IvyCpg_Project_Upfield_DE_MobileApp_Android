package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

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
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

import javax.annotation.Nullable;

public class SellerDetailMapPresenter implements SellerDetailMapContractor.SellerDetailMapPresenter {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;
    private boolean isRealTimeLocationOn = false;
    private Context context;
    private SellerDetailMapContractor.SellerDetailMapView sellerMapView;

    private LinkedHashMap<Integer,ArrayList<RetailerBo>> retailerVisitDetailsByRId = new LinkedHashMap<>();

    private LinkedHashMap<Integer,RetailerBo> retailerMasterHashmap =  new LinkedHashMap<>();

    private ArrayList<Integer> retailerVisitedOrder = new ArrayList<>();

    //Maintaining previous id not to draw route for same retailer continuously received
    private int previousRetailerId;
    private LatLng previousRetailerLatLng;
    private String totalOutletCount ;

    private int retailersVisitedSequence = 0;


    @Override
    public void setView(SellerDetailMapContractor.SellerDetailMapView sellerMapView, Context context) {
        this.sellerMapView = sellerMapView;
        this.context = context;
    }

    @Override
    public void downloadSellerOutletAWS(int userId) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select retailerId,retailerName,sequence,latitude,longitude,address,img_path,date from " +
                    "SupRetailerMaster where userId ='" + userId + "' order by sequence ASC";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {
                    RetailerBo retailerBo = new RetailerBo();
                    retailerBo.setRetailerId(c.getInt(0));
                    retailerBo.setRetailerName(c.getString(1));
                    retailerBo.setMasterSequence(c.getInt(2));
                    retailerBo.setMasterLatitude(c.getDouble(3));
                    retailerBo.setMasterLongitude(c.getDouble(4));
                    retailerBo.setAddress(c.getString(5));
                    retailerBo.setImgPath(c.getString(5));
                    retailerBo.setDate(c.getString(5));

                    if (retailerBo.getMasterLatitude() != 0 && retailerBo.getMasterLongitude() != 0) {

                        LatLng destLatLng = new LatLng(retailerBo.getMasterLatitude(), retailerBo.getMasterLongitude());

                        MarkerOptions markerOptions = new MarkerOptions()
                                .flat(true)
                                .title(retailerBo.getRetailerName())
                                .position(destLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey))
                                .snippet(String.valueOf(retailerBo.getRetailerId()));

                        sellerMapView.setRetailerMarker(retailerBo,markerOptions);
                    }

                    retailerMasterHashmap.put(retailerBo.getRetailerId(),retailerBo);
                }
                c.close();
            }

            db.closeDB();

            totalOutletCount = String.valueOf(retailerMasterHashmap.size());
            sellerMapView.updateSellerInfo("","",totalOutletCount,"0",null);

            sellerMapView.setOutletListAdapter(new ArrayList<>(retailerMasterHashmap.values()),0);

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void setSellerActivityListener(int userId) {

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
    public void setSellerActivityDetailListener(int userId) {

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
    public void getMarkerForFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (RetailerBo retailerBo : retailerMasterHashmap.values()) {
            if (retailerBo.getMasterLatitude() > 0 && retailerBo.getMasterLatitude() > 0) {
                LatLng builderLatLng = new LatLng(retailerBo.getMasterLatitude(),retailerBo.getMasterLongitude());
                builder.include(builderLatLng);
            }
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

        if (document.getData() != null) {
            String timeIn = convertMillisToTime((long)document.getData().get("timeIn"));
            String retailerName = (String)document.getData().get("retailerName");
            String covered = String.valueOf((long)document.getData().get("covered"));

            double latitude = (double)document.getData().get("latitude");
            double longitude = (double)document.getData().get("longitude");

            LatLng sellerCurrentLocation = null;

            if(isRealTimeLocationOn)
                sellerCurrentLocation = new LatLng(latitude,longitude);

            sellerMapView.updateSellerInfo(timeIn,retailerName,totalOutletCount,covered,sellerCurrentLocation);
        }
    }

    int lastVisited = 0;
    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

        System.out.println("setSellerDetailValues documentSnapshot = " + documentSnapshot.getData().get("userId"));

        if (documentSnapshotBo != null) {

            retailerVisitedOrder.add(documentSnapshotBo.getRetailerId());

            LatLng destLatLng = new LatLng(documentSnapshotBo.getLatitude(), documentSnapshotBo.getLongitude());

            //Update retailer info in master list

            RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

            BitmapDescriptor icon;
            if(retailerMasterBo.getIsOrdered() || documentSnapshotBo.getIsOrdered()) {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
                retailerMasterBo.setIsOrdered(true);
            }
            else {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
                retailerMasterBo.setIsOrdered(documentSnapshotBo.getIsOrdered());
            }

            retailerMasterBo.setSkipped(false);
            retailerMasterBo.setVisited(true);

            long totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
            retailerMasterBo.setTotalOrderValue(totalOrderValue);

            retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
            retailerMasterBo.setTimeIn(documentSnapshotBo.getTimeIn());
            retailerMasterBo.setTimeOut(documentSnapshotBo.getTimeOut());

            if(lastVisited < retailerMasterBo.getMasterSequence())
                lastVisited = retailerMasterBo.getMasterSequence();

            if (retailerMasterBo.getMasterLatitude() == 0 || retailerMasterBo.getMasterLongitude() == 0) {

                retailerMasterBo.setMasterLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setMasterLongitude(documentSnapshotBo.getLongitude());

                retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());

                MarkerOptions markerOptions = new MarkerOptions()
                        .flat(true)
                        .title(retailerMasterBo.getRetailerName() + "//" + retailerMasterBo.getTimeIn())
                        .position(destLatLng)
                        .icon(icon)
                        .snippet(String.valueOf(retailerMasterBo.getRetailerId()));

                sellerMapView.setRetailerMarker(retailerMasterBo,markerOptions);

            } else {

                String title = retailerMasterBo.getRetailerName() + "//" + retailerMasterBo.getTimeIn();

                retailerMasterBo.getMarker().setTitle(title);
                retailerMasterBo.getMarker().setIcon(icon);

                retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());

                destLatLng = retailerMasterBo.getMarker().getPosition();
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
            retailerBoObj.setVisitedSequence(retailersVisitedSequence + 1);

            if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {
                retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(retailerBoObj);
            } else {
                ArrayList<RetailerBo> visitedRetailerList = new ArrayList<>();
                visitedRetailerList.add(retailerBoObj);
                retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), visitedRetailerList);
            }

            //ends

            sellerMapView.setOutletListAdapter(new ArrayList<>(retailerMasterHashmap.values()),lastVisited);

            if (previousRetailerId != 0 && previousRetailerId != documentSnapshotBo.getRetailerId()){
                fetchRouteUrl(previousRetailerLatLng,destLatLng);
            }

            previousRetailerId = documentSnapshotBo.getRetailerId();
            previousRetailerLatLng = destLatLng;

            new UpdateSkippedMarker().execute();
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

    public ArrayList<ArrayList<RetailerBo>> getVisitedRetailerList(){
        return new ArrayList<>(retailerVisitDetailsByRId.values());
    }

    ArrayList<RetailerBo> getVisitedRetailers(){

        ArrayList<RetailerBo> retailerBos = new ArrayList<>();

        for(Integer id : retailerVisitDetailsByRId.keySet())
            retailerBos.add(retailerMasterHashmap.get(id));

        return retailerBos;
    }

    ArrayList<RetailerBo> getRetailerVisitDetailsByRId(int userId) {
        return retailerVisitDetailsByRId.get(userId);
    }

    public int getLastVisited() {
        return lastVisited;
    }

    class UpdateSkippedMarker extends AsyncTask<String, Void, ArrayList<Integer>> {

        @Override
        protected ArrayList<Integer> doInBackground(String... strings) {

            ArrayList<Integer> retailerId = new ArrayList<>();

            for(RetailerBo retailerBo : retailerMasterHashmap.values()){

                if(getLastVisited() > retailerBo.getMasterSequence() && !retailerBo.isVisited())
                    retailerId.add(retailerBo.getRetailerId());

            }

            return retailerId;
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> integers) {

            for (int retailerId : integers )
                retailerMasterHashmap.get(retailerId).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));

        }
    }

}
