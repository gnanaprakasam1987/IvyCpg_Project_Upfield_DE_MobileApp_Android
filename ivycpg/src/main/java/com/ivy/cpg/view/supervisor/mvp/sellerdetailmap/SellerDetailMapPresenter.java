package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.supervisor.customviews.LatLngInterpolator;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.annotation.Nullable;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.DETAIL_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.TIME_STAMP_PATH;
import static com.ivy.sd.png.provider.SynchronizationHelper.JSON_DATA_KEY;
import static com.ivy.utils.AppUtils.getApplicationVersionNumber;

public class SellerDetailMapPresenter implements SellerDetailMapContractor.SellerDetailMapPresenter {

    private ListenerRegistration registration;
    private boolean isRealTimeLocationOn = false;
    private Context context;
    private SellerDetailMapContractor.SellerDetailMapView sellerMapView;

    private LinkedHashMap<Integer, ArrayList<RetailerBo>> retailerVisitDetailsByRId = new LinkedHashMap<>();

    private LinkedHashMap<Integer, RetailerBo> retailerMasterHashmap = new LinkedHashMap<>();

    private ArrayList<Integer> retailerVisitedOrder = new ArrayList<>();

    //Maintaining previous id not to draw route for same retailer continuously received
    private int previousRetailerId;
    private LatLng previousRetailerLatLng;
    private String totalOutletCount;

    private int retailersVisitedSequence = 0;
    private int lastVisited = 0;

    private AlertDialog alertDialog;
    private ArrayList<LatLng> valuesList = new ArrayList<>();
    private String basePath = "";

    private BusinessModel businessModel;

    @Override
    public void setView(SellerDetailMapContractor.SellerDetailMapView sellerMapView, Context context) {
        this.sellerMapView = sellerMapView;
        this.context = context;
        basePath = AppUtils.getSharedPreferences(context).getString(FIREBASE_ROOT_PATH, "");

        businessModel = (BusinessModel) context.getApplicationContext();
    }

    @Override
    public void downloadSellerOutletAWS(int userId, String selectedDate) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            try {

                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
                Date date = sdf.parse(selectedDate);

                sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                selectedDate = sdf.format(date);

            } catch (Exception e) {
                Commons.printException(e);
            }

            String queryStr = "select retailerId,retailerName,sequence,latitude,longitude,address,imgpath,date from " +
                    "SupRetailerMaster where userId ='" + userId + "' and date ='" + selectedDate + "' order by sequence ASC";

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
                    retailerBo.setImgPath(c.getString(6));
                    retailerBo.setDate(c.getString(7));

                    if (retailerBo.getMasterLatitude() != 0 && retailerBo.getMasterLongitude() != 0) {

                        LatLng destLatLng = new LatLng(retailerBo.getMasterLatitude(), retailerBo.getMasterLongitude());

                        MarkerOptions markerOptions = new MarkerOptions()
                                //.flat(true)
                                .title(retailerBo.getRetailerName())
                                .position(destLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey))
                                .snippet(String.valueOf(retailerBo.getRetailerId()));

                        sellerMapView.setRetailerMarker(retailerBo, markerOptions);
                    }

                    retailerMasterHashmap.put(retailerBo.getRetailerId(), retailerBo);
                }
                c.close();
            }

            db.closeDB();

            totalOutletCount = String.valueOf(retailerMasterHashmap.size());
            sellerMapView.updateSellerInfo("", "", totalOutletCount, "0", null);

            sellerMapView.setOutletListAdapter(new ArrayList<>(retailerMasterHashmap.values()), 0);

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    @Override
    public void setSellerActivityListener(int userId, String date) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");


        if (appId.equals("") || basePath.equals(""))
            return;


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference documentReference = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date)
                .document(userId + "");

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
    public void setSellerMovementListener(int userId, String date) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        if (isRealTimeLocationOn) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db
                    .collection(basePath)
                    .document(REALTIME_LOCATION_PATH)
                    .collection(date)
                    .document(userId + "");

            registration = documentReference
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                setSellerMovementValues(documentSnapshot);
                            }
                        }
                    });
        }
    }

    @Override
    public void setSellerActivityDetailListener(int userId, String date) {
        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");

        if (appId.equals("") || basePath.equals(""))
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference queryRef = db
                .collection(basePath)
                .document(TIME_STAMP_PATH)
                .collection(date)
                .document(userId + "")
                .collection(DETAIL_PATH);

        registration = queryRef
                .orderBy("inTime", Query.Direction.ASCENDING)
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
                    + " where hhtCode='REALTIME02'";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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

        boolean isFocus = false;

        for (RetailerBo retailerBo : retailerMasterHashmap.values()) {

            if (retailerBo.getLatitude() > 0 && retailerBo.getLongitude() > 0) {
                LatLng builderLatLng = new LatLng(retailerBo.getLatitude(), retailerBo.getLongitude());
                builder.include(builderLatLng);
                isFocus = true;
            } else if (retailerBo.getMasterLatitude() > 0 && retailerBo.getMasterLatitude() > 0) {
                LatLng builderLatLng = new LatLng(retailerBo.getMasterLatitude(), retailerBo.getMasterLongitude());
                builder.include(builderLatLng);

                isFocus = true;
            }
        }

        if (isFocus)
            sellerMapView.focusMarker(builder);
    }

    @Override
    public String convertMillisToTime(Long time) {

        if (time != null && time != 0) {
            Date date = new Date(time);
            DateFormat format = new SimpleDateFormat("hh:mm a", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.format(date);
        } else
            return "";
    }

    @Override
    public String calculateDuration(long startTime, long endTime) {

        String duratingStr = (String) DateUtils.getRelativeTimeSpanString(startTime, endTime, 0);

        duratingStr = duratingStr.replace("ago", "");

        return duratingStr;
    }

    @Override
    public void animateSellerMarker(final LatLng destination, final Marker marker) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);

            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
//                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                                .target(newPosition)
//                                .zoom(14f)
//                                .build()));

//                        float bearing = getBearing(startPosition, destination);
//                        if (bearing >= 0)
//                            marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
                        Commons.printException(ex);
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public void drawRoute(ArrayList<LatLng> points) {
        sellerMapView.drawRoute(points);
    }

    @Override
    public void removeFirestoreListener() {
        if (registration != null)
            registration.remove();
    }

    private void setSellerActivityValues(DocumentSnapshot document) {

        if (document.getData() != null) {

            SellerBo sellerBo = document.toObject((SellerBo.class));

            if (sellerBo != null) {
                String timeIn = DateTimeUtils.getTimeFromMillis(sellerBo.getInTime());
                String retailerName = sellerBo.getRetailerName()!=null?sellerBo.getRetailerName():"";
                String covered = String.valueOf(sellerBo.getCovered());

                double latitude = sellerBo.getLatitude();
                double longitude = sellerBo.getLongitude();

                LatLng sellerCurrentLocation = null;

                sellerCurrentLocation = new LatLng(latitude, longitude);

                sellerMapView.updateSellerInfo(timeIn, retailerName, totalOutletCount, covered, sellerCurrentLocation);
            }
        }
    }

    private void setSellerMovementValues(DocumentSnapshot document) {

        if (document.getData() != null) {

            SellerBo sellerBo = document.toObject((SellerBo.class));

            if (sellerBo != null) {

                double latitude = sellerBo.getLatitude();
                double longitude = sellerBo.getLongitude();

                if (latitude > 0 && longitude > 0) {
                    LatLng sellerCurrentLocation = null;

                    sellerCurrentLocation = new LatLng(latitude, longitude);

                    sellerMapView.updateSellerLocation(sellerCurrentLocation);
                }
            }
        }
    }

    public void addRoutePoint(LatLng sellerCurrentLocation) {
        ArrayList<LatLng> routeLatLngList = new ArrayList<>();
        if (valuesList.size() > 0) {
            routeLatLngList.add(valuesList.get(valuesList.size() - 1));
            routeLatLngList.add(sellerCurrentLocation);

            valuesList.add(sellerCurrentLocation);

            sellerMapView.drawRoute(routeLatLngList);
        }
    }

    private void setSellerDetailValues(DocumentSnapshot documentSnapshot) {

        try {
            RetailerBo documentSnapshotBo = documentSnapshot.toObject((RetailerBo.class));

            if (documentSnapshotBo != null) {

                retailerVisitedOrder.add(documentSnapshotBo.getRetailerId());

                LatLng destLatLng = new LatLng(documentSnapshotBo.getLatitude(), documentSnapshotBo.getLongitude());

                //Update retailer info in master list

                RetailerBo retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());

                if (retailerMasterBo == null) {
                    retailerMasterHashmap.put(documentSnapshotBo.getRetailerId(), documentSnapshotBo);

                    retailerMasterBo = retailerMasterHashmap.get(documentSnapshotBo.getRetailerId());
                }

                if (retailerMasterBo != null) {

                    if (!retailerMasterBo.getIsDeviated() && documentSnapshotBo.getIsDeviated()) {
                        retailerMasterBo.setIsDeviated(true);
                    }

                    BitmapDescriptor icon;
                    if (retailerMasterBo.getIsOrdered() || documentSnapshotBo.getOrderValue() > 0) {

                        if (retailerMasterBo.getIsDeviated())
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green_deviated);
                        else
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);


                        retailerMasterBo.setIsOrdered(true);
                    } else {
                        if (retailerMasterBo.getIsDeviated())
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange_deviated);
                        else
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);

                        retailerMasterBo.setIsOrdered(false);
                    }

                    documentSnapshotBo.setIsOrdered(retailerMasterBo.getIsOrdered());
                    retailerMasterBo.setSkipped(false);
                    retailerMasterBo.setVisited(true);

                    double totalOrderValue = retailerMasterBo.getTotalOrderValue() + documentSnapshotBo.getOrderValue();
                    retailerMasterBo.setTotalOrderValue(totalOrderValue);

                    retailerMasterBo.setOrderValue(documentSnapshotBo.getOrderValue());
                    retailerMasterBo.setInTime(documentSnapshotBo.getInTime());
                    retailerMasterBo.setOutTime(documentSnapshotBo.getOutTime());

                    if (lastVisited < retailerMasterBo.getMasterSequence())
                        lastVisited = retailerMasterBo.getMasterSequence();

                    if (retailerMasterBo.getMasterLatitude() == 0 || retailerMasterBo.getMasterLongitude() == 0) {

                        if (documentSnapshotBo.getLatitude() != 0 && documentSnapshotBo.getLongitude() != 0) {

                            retailerMasterBo.setMasterLatitude(documentSnapshotBo.getLatitude());
                            retailerMasterBo.setMasterLongitude(documentSnapshotBo.getLongitude());

                            retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                            retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions()
                                    //.flat(true)
                                    .title(retailerMasterBo.getRetailerName() + "//" + retailerMasterBo.getInTime())
                                    .position(destLatLng)
                                    .icon(icon)
                                    .snippet(String.valueOf(retailerMasterBo.getRetailerId()));

                            sellerMapView.setRetailerMarker(retailerMasterBo, markerOptions);
                        }

                    } else {

                        String title = retailerMasterBo.getRetailerName() + "//" + retailerMasterBo.getInTime();

                        if (documentSnapshotBo.getLatitude() == 0 || documentSnapshotBo.getLongitude() == 0) {
                            retailerMasterBo.setLatitude(retailerMasterBo.getMasterLatitude());
                            retailerMasterBo.setLongitude(retailerMasterBo.getMasterLongitude());
                        } else {
                            retailerMasterBo.setLatitude(documentSnapshotBo.getLatitude());
                            retailerMasterBo.setLongitude(documentSnapshotBo.getLongitude());
                        }

                        LatLng newRetailLatlng = new LatLng(retailerMasterBo.getLatitude(), retailerMasterBo.getLongitude());

                        retailerMasterBo.getMarker().setPosition(newRetailLatlng);
                        retailerMasterBo.getMarker().setTitle(title);
                        retailerMasterBo.getMarker().setIcon(icon);

                        destLatLng = retailerMasterBo.getMarker().getPosition();
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
                    retailerBoObj.setVisitedSequence(retailersVisitedSequence + 1);

                    if (retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()) != null) {
                        retailerVisitDetailsByRId.get(documentSnapshotBo.getRetailerId()).add(retailerBoObj);
                    } else {
                        ArrayList<RetailerBo> visitedRetailerList = new ArrayList<>();
                        visitedRetailerList.add(retailerBoObj);
                        retailerVisitDetailsByRId.put(documentSnapshotBo.getRetailerId(), visitedRetailerList);
                    }

                    //ends

                    sellerMapView.setOutletListAdapter(new ArrayList<>(retailerMasterHashmap.values()), lastVisited);

                    /*if (previousRetailerId != 0 &&
                            previousRetailerId != documentSnapshotBo.getRetailerId()) {
                        fetchRouteUrl(previousRetailerLatLng, destLatLng);
                    }*/

                    previousRetailerId = documentSnapshotBo.getRetailerId();
                    previousRetailerLatLng = destLatLng;

                    updateSkippedMarker();
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void fetchRouteUrl(LatLng startlatLng, LatLng endLatLng) {

        String url = getUrl(startlatLng, endLatLng);
        Commons.print("drawRouteUrl " + url);
        FetchUrl fetchUrl = new FetchUrl(this);
        fetchUrl.execute(url);
    }

    private String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        String sensor = "sensor=false&units=metric";
        String alternatives = "alternatives=false&mode=walking";
        String mapKey = "key=" + context.getString(R.string.google_maps_api_key);
        String parameters = str_origin + "&" + str_dest + "&" + alternatives + "&" + mapKey;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    public ArrayList<ArrayList<RetailerBo>> getVisitedRetailerList() {
        return new ArrayList<>(retailerVisitDetailsByRId.values());
    }

    boolean checkAreaBoundsTooSmall(LatLngBounds bounds) {
        int minDistanceInMeter = 300;
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }

    String convertPlaneDateToGlobal(String planeDate) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            Date date = sdf.parse(planeDate);

            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            planeDate = sdf.format(date);

            return planeDate;

        } catch (Exception e) {
            Commons.printException(e);
        }

        return planeDate;
    }

    public ArrayList<RetailerBo> getVisitedRetailers() {

        ArrayList<RetailerBo> retailerBos = new ArrayList<>();

        for (Integer id : retailerVisitDetailsByRId.keySet())
            retailerBos.add(retailerMasterHashmap.get(id));

        return retailerBos;
    }

    public ArrayList<RetailerBo> getRetailerVisitDetailsByRId(int userId) {
        return retailerVisitDetailsByRId.get(userId);
    }

    int getLastVisited() {
        return lastVisited;
    }

    private void updateSkippedMarker() {
        for (RetailerBo retailerBo : retailerMasterHashmap.values()) {

            if (getLastVisited() > retailerBo.getMasterSequence() && !retailerBo.isVisited())
                retailerMasterHashmap.get(retailerBo.getRetailerId()).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));

        }
    }

    @Override
    public void downloadSellerRoute(String userId, String date) {
        new DownloadSellerRoute(userId, date).execute();
    }

    /**
     * Downloading Seller Realtime location from server to draw route
     * Process Starts here
     */

    class DownloadSellerRoute extends AsyncTask<String, Void, Boolean> {

        private String userId, date;

        DownloadSellerRoute(String userId, String date) {
            this.userId = userId;
            this.date = date;
        }

        protected void onPreExecute() {

            if (alertDialog == null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                customProgressDialog(builder,
                        context.getResources().getString(R.string.progress_dialog_title_downloading));
                alertDialog = builder.create();
                alertDialog.show();

            }

        }

        @Override
        protected Boolean doInBackground(String... params) {

            prepareJson(userId, convertPlaneDateToGlobal(date));

            String loginId = businessModel.synchronizationHelper.
                    getSelectedUserLoginId(userId, context);

            return NetworkUtils.isNetworkConnected(context) && prepareDownloadData(loginId);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (result) {
                drawRoute(valuesList);
            }
        }

        private JSONObject json = new JSONObject();

        private void prepareJson(String sellerId, String date) {
            try {

                json.put("UserId", sellerId);
                json.put("LoginUserId", businessModel.getAppDataProvider().getUser().getUserid());
                json.put("VersionCode", getApplicationVersionNumber(context));
                json.put("RequestDate", date);

            } catch (Exception e) {
                Commons.printException(e);
            }

        }

        private boolean prepareDownloadData(String userLoginId) {

            boolean isSuccess = false;
            try {


                String downloadurl = SupervisorActivityHelper.getInstance().getDownloadUrl(context, "USERLOCATIONREPORT");

                Commons.print("downloadUrl " + downloadurl);
                Commons.print("json = " + json);

                Vector<String> responseVector = getMasterResponse(json, downloadurl, userLoginId);

                try {
                    if (responseVector.size() > 0) {

                        for (String s : responseVector) {

                            JSONObject jsonObject = new JSONObject(s);
                            Iterator itr = jsonObject.keys();
                            while (itr.hasNext()) {

                                String key = (String) itr.next();
                                if (key.equals("Master")) {
                                    parseJSON(jsonObject);
                                    isSuccess = true;

                                } else if (key.equals("Errorcode")) {
                                    String tokenResponse = jsonObject.getString("Errorcode");
                                    if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                            || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                            || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                        isSuccess = false;

                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException jsonException) {
                    Commons.print(jsonException.getMessage());
                }

            } catch (Exception e) {
                Commons.printException(e);
            }

            return isSuccess;

        }

        private Vector<String> getMasterResponse(JSONObject data,
                                                 String appendurl, String userLoginId) {

//        bmodel.synchronizationHelper.updateAuthenticateTokenWithoutPassword(userLoginId);
            // Update Security key
            businessModel.synchronizationHelper.updateAuthenticateToken(false);
            StringBuilder url = new StringBuilder();
            url.append(DataMembers.SERVER_URL);
            url.append(appendurl);
            if (businessModel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                try {
                    MyHttpConnectionNew http = new MyHttpConnectionNew();
                    http.create(MyHttpConnectionNew.POST, url.toString(), null);
                    http.addHeader("SECURITY_TOKEN_KEY", businessModel.synchronizationHelper.getSecurityKey());
                    http.setParamsJsonObject(data);

                    http.connectMe();
                    Vector<String> result = http.getResult();
                    if (result == null) {
                        return new Vector<>();
                    }
                    return result;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    return new Vector<>();
                }
            } else {
                return new Vector<>();
            }
        }


        public void parseJSON(JSONObject jsonObject) {

            try {

                JSONArray first = jsonObject.getJSONArray(JSON_DATA_KEY);

                for (int j = 0; j < first.length(); j++) {
                    JSONArray value = (JSONArray) first.get(j);

                    String firstValue = value.toString();

                    firstValue = firstValue.substring(1, firstValue.length() - 1);

                    firstValue = firstValue.replace("\\/", "/");

                    String[] strArray = firstValue.split(",");

                    LatLng latLng = new LatLng(SDUtil.convertToDouble(strArray[1]), SDUtil.convertToDouble(strArray[2]));

                    valuesList.add(latLng);

                }

                Commons.print("Route Values " + valuesList);

            } catch (JSONException e) {
                Commons.printException("" + e);
            }

        }

        public void customProgressDialog(AlertDialog.Builder builder, String message) {

            try {
                View view = View.inflate(context, R.layout.custom_alert_dialog, null);

                TextView title = view.findViewById(R.id.title);
                title.setText(DataMembers.SD);
                TextView messagetv = view.findViewById(R.id.text);
                messagetv.setText(message);

                builder.setView(view);
                builder.setCancelable(false);

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
    }

    // Download Process Ends


    public void setRetailerMaster(RetailerBo retailerBO) {

        RetailerMasterBO retailerMaster = new RetailerMasterBO();
        retailerMaster.setRetailerName(retailerBO.getRetailerName());
        retailerMaster.setRetailerID(retailerBO.getRetailerId() + "");
        retailerMaster.setLatitude(retailerBO.getLatitude());
        retailerMaster.setLongitude(retailerBO.getLongitude());

        businessModel.getAppDataProvider().setRetailerMaster(retailerMaster);
    }
}
