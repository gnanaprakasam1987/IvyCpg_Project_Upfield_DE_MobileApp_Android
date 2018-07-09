package com.ivy.cpg.view.supervisor.helper;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.view.supervisor.Seller;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SupervisorActivityHelper {

    private boolean isMarkerRotating;

    private HashMap<String, DetailsBo> detailsBoHashMap = new HashMap<>();

    private static SupervisorActivityHelper instance = null;

    private SupervisorActivityHelper() {
    }

    public static SupervisorActivityHelper getInstance() {
        if (instance == null) {
            instance = new SupervisorActivityHelper();
        }
        return instance;
    }

    public HashMap<String, DetailsBo> getDetailsBoHashMap() {
        return detailsBoHashMap;
    }

    private void setDetailsBoHashMap(HashMap<String, DetailsBo> detailsBoHashMap) {
        this.detailsBoHashMap = detailsBoHashMap;
    }

    public void loginToFirebase(final Context context) {

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
                            Commons.print("firebase auth success");
                        } else {
                            Commons.print("firebase auth failed");
                        }
                    }
                });
            }
        }
    }

    /**
     * Get all Seller Complete details when updated or inserted
     */
    public void subscribeSellerLocationUpdates(Context context, final Seller seller, int trackingType) {

        if (FirebaseDatabase.getInstance() == null) {
            loginToFirebase(context);
        } else {

            String path;

            if (trackingType == 2)
                path = "/activity_tracking/";
            else
                path = "/movement_tracking/";

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(LocationConstants.FIREBASE_BASE_PATH + path);


            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    seller.setMarker(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    seller.setMarker(dataSnapshot);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Commons.print("Failed to read value." + error.toException());
                }
            });
        }
    }

    /**
     * Get Seller Details when Attendance details updated
     */
    public void subscribeSellersUpdates(Context context, final Seller seller) {

        if (FirebaseDatabase.getInstance() == null) {
            loginToFirebase(context);
        } else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(LocationConstants.FIREBASE_BASE_PATH + "/Attendance/");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    seller.updateSellerInfo(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Selected Seller Details From Firebase
     */
    public void subscribeSellerDetails(final Context context, final Seller seller, String pathNode) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(LocationConstants.FIREBASE_BASE_PATH + pathNode);

        ref.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                seller.setSellerMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                seller.setSellerMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Commons.print("Failed to read value." + error.toException());
            }
        });

    }

    /**
     * Return the seller Count under Supervisor
     */
    public int getSellersCount(Context context, BusinessModel bmodel) {
        int sellerCount = 0;
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append("select userid,username from Usermaster where isDeviceUser=0 AND ");
            if (bmodel.configurationMasterHelper.userLevel != null && bmodel.configurationMasterHelper.userLevel.length() > 0)
                sb.append("userLevel in (" + bmodel.configurationMasterHelper.userLevel + ")");
            else
                sb.append("relationship !='PARENT'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    DetailsBo detailsBo = new DetailsBo();
                    detailsBo.setUserId(c.getInt(0));
                    detailsBo.setUserName(c.getString(1));
                    detailsBo.setStatus("Absent");
                    detailsBoHashMap.put(String.valueOf(c.getInt(0)),detailsBo);
                }
                c.close();

                setDetailsBoHashMap(detailsBoHashMap);

                sellerCount = detailsBoHashMap.size();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }

        return sellerCount;
    }

    /**
     * Will return Address from Latlong
     */
    public String getAddressLatLong(Context context, LatLng latLng) {
        StringBuilder sb = new StringBuilder();

        Geocoder gc = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                    sb.append(address.getAddressLine(i) != null ? address.getAddressLine(i) : "").append("\n");
                sb.append(address.getLocality() != null ? address.getLocality() : "").append("\n");
                sb.append(address.getPostalCode() != null ? address.getPostalCode() : "").append("\n");
                sb.append(address.getCountryName() != null ? address.getCountryName() : "");
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return sb.toString();
    }

    /**
     * Animate the seller marker in map path
     */
    void moveMarkerInPath(final LatLng destination, final Marker marker, final GoogleMap googleMap) {

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

                        float bearing = getBearing(startPosition, destination);
                        if (bearing >= 0)
                            marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
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

    //Method for finding bearing between two points
    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    public String getTimeFromMillis(String millis) {
        if (millis == null || millis.trim().length() == 0 || millis.equalsIgnoreCase("0"))
            return "";

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(millis));
        // Pass date object
        return formatter.format(calendar.getTime());
    }

    Bitmap setMarkerDrawable(int number, Context context) {
        int background = R.drawable.location_icon;

        Bitmap icon = drawTextToBitmap(background, String.valueOf(number), context);

        return icon;
    }

    private Bitmap drawTextToBitmap(int gResId, String gText, Context context) {
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize((int) (15 * scale));
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 3;
        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    public void animateMarkerNew(final LatLng destination, final Marker marker, final GoogleMap googleMap) {

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

                        float bearing = getBearing(startPosition, destination);
                        if (bearing >= 0)
                            marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
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

    /**
     * Need To check these method for animation marker
     */
    private void animateMarker(final GoogleMap mMap, final Marker marker, final LatLng toPosition, final Location toLocation, final boolean hideMarker) {

        if (mMap == null || marker == null || marker.getPosition() == null) {
            return;
        }

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final double startRotation = marker.getRotation();
        final long duration = 100;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                float rotation = (float) (t * toLocation.getBearing() + (1 - t)
                        * startRotation);
                if (rotation != 0) {
                    marker.setRotation(rotation);
                }
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 6000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void animateMarker(GoogleMap myMap, final Marker marker, final LatLng latLng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                marker.setPosition(latLng);


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    marker.setVisible(true);
                }
            }
        });
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void rotateMarker(final Marker marker, final float toRotation) {

        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }

    private void animateMarker(final GoogleMap mMap, final Marker marker, final Location location) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float v = valueAnimator.getAnimatedFraction();
                        double lng = v * marker.getPosition().longitude + (1 - v)
                                * location.getLongitude();
                        double lat = v * marker.getPosition().latitude + (1 - v)
                                * location.getLatitude();
                        LatLng newPos = new LatLng(lat, lng);
                        marker.setPosition(newPos);
                        marker.setAnchor(0.5f, 0.5f);
                        marker.setRotation((float) bearingBetweenLocations(marker.getPosition(), newPos));
                        mMap.moveCamera(CameraUpdateFactory
                                .newCameraPosition
                                        (new CameraPosition.Builder()
                                                .target(newPos)
                                                .zoom(15.5f)
                                                .build()));
                    }
                });
                valueAnimator.start();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }


}
