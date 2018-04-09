package com.ivy.maplib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.location.LocationUpdater;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.VisitConfiguration;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.profile.ProfileActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static com.ivy.sd.png.util.StandardListMasterConstants.MENU_STK_ORD;

public class PlanningMapFragment extends SupportMapFragment implements
        OnMyLocationButtonClickListener, OnGlobalLayoutListener,
        OnMarkerClickListener, OnInfoWindowClickListener, LocationUpdater {

    int prog = 0;
    private DataPulling dataPull;
    private int mClick = 0;
    private double myLatitude, myLongitude;
    private GoogleMap mMap;
    private List<MarkerOptions> markerList = null;
    private Marker currLocMarker;
    private MarkerOptions currLocMarkerOption;
    private LatLng[] markerLatLng = new LatLng[2];
    private Vector<Polyline> line;
    private String toText;
    private LatLngBounds bounds = null;
    private LatLngBounds.Builder builder = null;
    private TextView fromTxt, toTxt, fromTv, toTv, hrsTextView;
    private View mapView;
    private View rootView;
    private MapWrapperLayout mainLayout;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet, infoDistance;
    private Button startVisitBtn;
    private LinearLayout startVisitLty;
    private ImageButton carDirBtn, walkDirBtn, clearRouteBtn;
    private LayoutInflater layInflater;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private boolean showToast = true;
    private BusinessModel bmodel;
    private List<Marker> markersLst;
    private int retRadius = 10;
    private boolean isLocationUpdated = false;
    ImageView visitView;
    FloatingActionButton fab1, fab2, fab3;
    private LinearLayout bottomLayout;
    private Marker rmarker;
    private Map<String, String> mRetailerProp;
    private Map<String, String> mRetTgtAchv;
    private String calledBy;
    CardView cardView, cardView1;
    private AutoCompleteTextView mBrandAutoCompleteTV;
    TextView tv_storeVisit;
    private static final String MENU_PLANNING = "Day Planning";
    private static final String MENU_VISIT = "Trade Coverage";
    private ArrayList<RetailerMasterBO> retailer = new ArrayList<>();
    private String mSelecteRetailerType = "ALL";
    private boolean hasOrderScreen;
    private static final String CODE_PRODUCTIVE = "Filt_01";
    private static final String CODE_NON_PRODUCTIVE = "Filt_02";
    private static final String CODE_VISITED = "Filt_03";
    private static final String CODE_QDVP3 = "Filt_04";
    private static final String CODE_INDICATIVE = "Filt_05";
    private static final String CODE_GOLDEN_STORE = "FIlt_06";
    private static final String CODE_DEAD_STORE = "Filt_07";
    private static final String CODE_HANGING = "Filt_08";
    private ArrayList<RetailerMasterBO> startVistitRetailers = new ArrayList<>();
    public boolean profileclick, isclickable;
    private boolean isBywalk = false;
    private boolean isRoute = false;
    private String durationStr;
    private ImageView crossLine;
    private TextView messagetv;
    private int mSelectedSubId = -1;


    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mapView = super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_planning_map, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        layInflater = inflater;
//        setHasOptionsMenu(false);
        visitView = (ImageView) rootView.findViewById(R.id.visit_viewchange);
        fromTxt = (TextView) rootView.findViewById(R.id.from_txtid);
        toTxt = (TextView) rootView.findViewById(R.id.to_txtid);
        fromTv = (TextView) rootView.findViewById(R.id.from_txt_value);
        toTv = (TextView) rootView.findViewById(R.id.to_txt_value);
        fab1 = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab2 = (FloatingActionButton) rootView.findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) rootView.findViewById(R.id.fab3);
        cardView = (CardView) rootView.findViewById(R.id.card_view);
        cardView1 = (CardView) rootView.findViewById(R.id.card_view1);
        carDirBtn = (ImageButton) rootView.findViewById(R.id.car_direction);
        walkDirBtn = (ImageButton) rootView.findViewById(R.id.walk_direction);
        bottomLayout = (LinearLayout) rootView.findViewById(R.id.bottom_view);
        hrsTextView = (TextView) rootView.findViewById(R.id.hrs_txt_value);
        clearRouteBtn = (ImageButton) rootView.findViewById(R.id.clear_route_id);
        crossLine = (ImageView) rootView.findViewById(R.id.cross_line);


        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(sizeLarge);
        if (!is7InchTablet) {
            bottomLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.ratiler_map_bottomview_height);
        }


        fromTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        toTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        fromTv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        toTv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        if (bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            mSelectedSubId = bmodel.mSelectedSubId;
        }


        crossLine.setRotation(-5);
        /** Show/Hide the "all route filter" **/
        if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
            cardView1.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
            cardView1.setVisibility(View.VISIBLE);
        }
        Spinner daySpinner = (Spinner) rootView.findViewById(R.id.routeSpinner);


        ArrayList<BeatMasterBO> beatBOArray = new ArrayList<>();
        beatBOArray.add(new BeatMasterBO(0, getResources().getString(
                R.string.all), 0));
        for (int i = 0; i < bmodel.beatMasterHealper.getBeatMaster().size(); i++) {
            beatBOArray
                    .add(bmodel.beatMasterHealper.getBeatMaster().get(i));
        }
        ArrayAdapter<BeatMasterBO> brandAdapter = new BeatAdapter(
                getActivity(), R.layout.row_dropdown, R.id.lbl_name,
                beatBOArray);
        brandAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(brandAdapter);

        mBrandAutoCompleteTV = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTextView1);
        mBrandAutoCompleteTV.setAdapter(brandAdapter);
        mBrandAutoCompleteTV.setThreshold(1);
        mBrandAutoCompleteTV.setSelection(0);


        bmodel.daySpinnerPositon = 0;
        BeatMasterBO beatmasterbo = brandAdapter.getItem(0);
        bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
        loadData(beatmasterbo.getBeatId(), null);
        mBrandAutoCompleteTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBrandAutoCompleteTV.showDropDown();
                return false;
            }
        });
        mBrandAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bmodel.daySpinnerPositon = position;
                BeatMasterBO beatmasterbo = (BeatMasterBO) parent
                        .getItemAtPosition(position);
                bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
                loadData(beatmasterbo.getBeatId(), null);
            }
        });
        if (brandAdapter.getCount() > 0) {
            daySpinner.setSelection(bmodel.daySpinnerPositon);
        }

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                bmodel.daySpinnerPositon = position;
                BeatMasterBO beatmasterbo = (BeatMasterBO) parent
                        .getItemAtPosition(position);
                bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
                loadData(beatmasterbo.getBeatId(), null);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /** End of show all routes **/
        mRetailerProp = new HashMap<>();
        mRetTgtAchv = new HashMap<>();
        if (getArguments() != null)
            calledBy = getArguments().getString("From");

        if (calledBy == null)
            calledBy = MENU_VISIT;

        updateRetailerAttributes();
        updateRetailerProperty();
        bmodel.mRetailerHelper.IsRetailerGivenNoVisitReason();

//        TextView tvStoreLbl = (TextView)rootView.findViewById(R.id.tv_label);
//        tvStoreLbl.setTypeface(bmodel.configurationMasterHelper
//                .getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        TextView lbl_BeatLoc = (TextView) rootView.findViewById(R.id.label_BeatLoc);
        lbl_BeatLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView lbl_StoreToVisit = (TextView) rootView.findViewById(R.id.label_StoreToVisit);
        lbl_StoreToVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView lbl_TodayTgt = (TextView) rootView.findViewById(R.id.label_TodayTgt);
        lbl_TodayTgt.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView spinnerLabel = (TextView) rootView.findViewById(R.id.spinnerLabel);
        spinnerLabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView tv_areaLoc = (TextView) rootView.findViewById(R.id.daytv);
        tv_areaLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_areaLoc.setText(bmodel.getDay(bmodel.userMasterHelper
                .getUserMasterBO().getDownloadDate()));

        tv_storeVisit = (TextView) rootView.findViewById(R.id.tv_store_visit);
        tv_storeVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_storeVisit.setText(retailer.size() + "");

        TextView tv_target = (TextView) rootView.findViewById(R.id.tv_tgt);
        tv_target.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT) {
            tv_target.setText(String.valueOf(getStoreVisited()));
        } else {
            tv_target.setText(getTotalVisitActual());
        }

        TextView tv_target1 = (TextView) rootView.findViewById(R.id.tv_tgt1);
        tv_target1.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT) {
            tv_target1.setText(String.valueOf(getStoreVisited()));
        } else {
            tv_target1.setText(getTotalVisitActual());
        }
        TextView lbl_TodayTgt1 = (TextView) rootView.findViewById(R.id.label_TodayTgt1);
        lbl_TodayTgt1.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        visitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPull.switchVisitView();
            }
        });


        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRoute) {
                    bottomLayout.setVisibility(View.VISIBLE);
                    isRoute = true;
                } else {
                    isRoute = false;
                    isclickable = false;
                    isBywalk = false;
                    clearRoute();
                    bottomLayout.setVisibility(View.GONE);
                    //car icon's
                    carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                    carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.map_car_color));
                    //walk iocn's
                    walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                    walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.light_grey));
                }

            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeOnMap();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nearByRetailerDialog();
            }
        });

        walkDirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBywalk = true;
                isclickable = true;
                clearRoute();
                if (mClick == 2) {
                    if (!isSameLocation(rmarker))
                        drawRoute(rmarker);
                    hrsTextView.setText(durationStr);
                }
                //car icon's
                walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.map_car_color));
                //walk iocn's
                carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.light_grey));
            }
        });

        carDirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBywalk = false;
                isclickable = true;
                clearRoute();
                if (mClick == 2) {
                    if (!isSameLocation(rmarker))
                        drawRoute(rmarker);
                    hrsTextView.setText(durationStr);
                }
                //car icon's
                carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.map_car_color));
                //walk iocn's
                walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.light_grey));
            }
        });

        clearRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclickable = false;
                isBywalk = false;
                clearRoute();
                //car icon's
                carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.map_car_color));
                //walk iocn's
                walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.light_grey));
            }
        });

        return rootView;
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        markersLst = new ArrayList<>();
        line = new Vector<>();
        String testString;
        mainLayout = (MapWrapperLayout) rootView
                .findViewById(R.id.planningmapnew);
        mainLayout.addView(mapView);
//        LayoutParams linearlprams = new LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT);
//        LinearLayout linearWidget = new LinearLayout(getActivity());
//        linearWidget.setLayoutParams(linearlprams);
//        linearWidget.setGravity(Gravity.BOTTOM);
//        linearWidget.setLayoutParams(linearlprams);
//        LayoutParams lprams = new LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT);
//        lprams.weight = 1;
//        fromTv = new TextView(getActivity());
//        testString = "From : ";
//        fromTv.setText(testString);
//        fromTv.setTextColor(Color.parseColor("#FFFFFF"));
//        fromTv.setLayoutParams(lprams);
//        fromTv.setPadding(5, 5, 5, 5);
//        fromTv.setBackgroundColor(Color.parseColor("#AA5A5A5E"));
//        fromTv.setSingleLine(true);
//        fromTv.setTypeface(null, Typeface.BOLD);
//        toTv = new TextView(getActivity());
//        testString = "To : ";
//        toTv.setText(testString);
//        toTv.setTextColor(Color.parseColor("#FFFFFF"));
//        toTv.setLayoutParams(lprams);
//        toTv.setPadding(5, 5, 5, 5);
//        toTv.setBackgroundColor(Color.parseColor("#AA5A5A5E"));
//        toTv.setSingleLine(true);
//        toTv.setTypeface(null, Typeface.BOLD);
//        linearWidget.addView(fromTv);
//        linearWidget.addView(toTv);
//        FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mainLayout
//                .getLayoutParams();
//        params.gravity = Gravity.BOTTOM;
//        mainLayout.addView(linearWidget, params);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        profileclick = false;
        bmodel.locationUtil.instantiateLocationUpdater(PlanningMapFragment.this);
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()
                        .getApplicationContext());

        try {
            retailer = new ArrayList<>();

            if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                displayTodayRoute(null);
            }

            markerList = dataPull.getData();
            if (resultCode == ConnectionResult.SUCCESS) {
                setUpMapIfNeeded();


                if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Enable GPS", Toast.LENGTH_LONG).show();
                }

                getRetailer();
            } else {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 1);
            }
        } catch (Exception e) {
            Commons.print("Exception:" + e);
        }
    }

//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        if (!(GooglePlayServicesUtil
//                .isGooglePlayServicesAvailable(getActivity()
//                        .getApplicationContext()) == ConnectionResult.SUCCESS)) {
//            menu.findItem(R.id.show_me).setVisible(false);
//            menu.findItem(R.id.show_places).setVisible(false);
//            menu.findItem(R.id.clear_route).setVisible(false);
//        }
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.map_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.show_me) {
//            showMeOnMap();
//            return true;
//        } else if (item.getItemId() == R.id.show_places) {
//            showToast = false;
//            clearRoute();
//            removeMarkersfromMap();
//            addMarkersToMap();
//            onGlobalLayout();
//            return true;
//        } else if (item.getItemId() == R.id.clear_route) {
//            clearRoute();
//            return true;
//        } else if (item.getItemId() == R.id.nearRet) {
//            nearByRetailerDialog();
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            if (activity instanceof DataPulling) {
                dataPull = (DataPulling) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DataPullingInterface");
        }
    }


    private void setUpMapIfNeeded() {
        try {
            final ViewGroup nullParent = null;
            if (mMap == null) {
                mMap = this.getMap();
                float pxlDp = 39 + 20;
                mainLayout.init(mMap, getPixelsFromDp(PlanningMapFragment.this.getActivity(), pxlDp));
                this.infoWindow = (ViewGroup) layInflater.inflate(
                        R.layout.custom_info_window, nullParent);
                this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
                this.infoSnippet = (TextView) infoWindow
                        .findViewById(R.id.snippet);
                this.infoDistance = (TextView) infoWindow.findViewById(R.id.distance_txt);
                startVisitLty = (LinearLayout) infoWindow.findViewById(R.id.start_visit_lty);
                startVisitBtn = (Button) infoWindow.findViewById(R.id.start_visitbtn);
                this.infoTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                this.infoSnippet.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                this.infoDistance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
//
                this.infoButtonListener = new OnInfoWindowElemTouchListener(startVisitLty) {
                    @Override
                    protected void onClickConfirmed(View v, Marker marker) {
                        for (RetailerMasterBO startVisitBo : retailer) {
                            if (startVisitBo.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                                bmodel.setRetailerMasterBO(startVisitBo);
                                if (!profileclick) {
                                    profileclick = true;
                                    bmodel.newOutletHelper.downloadLinkRetailer();
                                    Intent i = new Intent(getActivity(), ProfileActivity.class);
                                    i.putExtra("From", MENU_VISIT);
                                    i.putExtra("locvisit", true);
                                    startActivity(i);
                                }
                            }
                        }
                    }
                };
                startVisitLty.setOnTouchListener(infoButtonListener);
                setUpMap();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void setUpMap() {
        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMyLocationEnabled(false);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void locationUpdate() {
        myLatitude = LocationUtil.latitude;
        myLongitude = LocationUtil.longitude;
        updateMarker();
        if (myLatitude != 0 && myLongitude != 0) {
            if (!isLocationUpdated) {
                showMyLocation();
                isLocationUpdated = true;
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker mMarker) {
        String testStr;
        if (checkInternetConnection()) {
            try {
                if (mClick == 0) {
                    markerLatLng[0] = mMarker.getPosition();
                    testStr = (mMarker.getTitle().equals("My Location")) ? trimString(mMarker.getTitle(), 20)
                            : trimString(mMarker.getTitle().split(",")[0], 20);
                    fromTv.setText(testStr);
                    mClick = 1;
                    mMarker.hideInfoWindow();
                } else if (mClick == 1) {
                    rmarker = mMarker;
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = (mMarker.getTitle().equals("My Location")) ? trimString(mMarker.getTitle(), 20)
                            : trimString(mMarker.getTitle().split(",")[0], 20);
                    toTv.setText(testToRoute);
                    if (!isSameLocation(mMarker))
                        drawRoute(mMarker);

                } else if (mClick == 2) {
                    mClick = -1;
                    rmarker = mMarker;
                    testStr = trimString(toText, 20);
                    fromTv.setText(testStr);
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = trimString(mMarker.getTitle(), 20);
                    toTv.setText(testToRoute);
                    clearRoute();
                    if (mClick == 1) {
                        if (!isSameLocation(mMarker))
                            drawRoute(mMarker);
                    }
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Check Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSameLocation(Marker mMarker) {
        return (markerLatLng[0].latitude == mMarker.getPosition().latitude && markerLatLng[0].longitude == mMarker
                .getPosition().longitude);
    }

    private void drawRoute(Marker mMarker) {
        String url;
        String testRoute;
        mMarker.hideInfoWindow();
        markerLatLng[1] = mMarker.getPosition();
//        testRoute = trimString(mMarker.getTitle(), 20);
//        toTv.setText(testRoute);
        toText = (mMarker.getTitle().equals("My Location")) ? mMarker.getTitle()
                : mMarker.getTitle().split(",")[0];
        url = makeURL(markerLatLng[0].latitude, markerLatLng[0].longitude,
                markerLatLng[1].latitude, markerLatLng[1].longitude);
        ConnectAsyncTask getRoute = new ConnectAsyncTask(url);
        getRoute.execute();
        mClick = 2;
    }

    public void showMeOnMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLatitude,
                myLongitude)));
    }

    private void clearRoute() {
        String testClearRoute;
        try {
            if (mClick == -1) {
                markerLatLng[0] = markerLatLng[1];
                testClearRoute = " ";
                toTv.setText(testClearRoute);
                mClick = 1;
            } else if (!isclickable) {
                testClearRoute = " ";
                fromTv.setText(testClearRoute);
                testClearRoute = " ";
                toTv.setText(testClearRoute);
                toText = "";
                if (showToast) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Route Cleared, Select Two Marker",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showToast = true;
                }
                mClick = 0;
            }
            for (Polyline polyLine : line) {
                polyLine.remove();
            }
            line = null;
            System.gc();
            line = new Vector<>();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public boolean onMarkerClick(Marker mMarker) {
        if (isRoute) {
            isclickable = false;
            onInfoWindowClick(mMarker);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void onGlobalLayout() {
        try {
            if (markerList.size() > 0) {
                if (builder == null) {
                    builder = new LatLngBounds.Builder();
                } else {
                    builder = null;
                    builder = new LatLngBounds.Builder();
                }

                for (int i = 0; i < markerList.size(); i++) {
                    LatLng latLng = markerList.get(i).getPosition();
                    double lat = latLng.latitude;
                    double lng = latLng.longitude;
                    float dist = LocationUtil.calculateDistance(lat, lng);
                    if (lat != 0.0 && lng != 0.0 && LocationUtil.latitude != 0.0 && LocationUtil.longitude != 0.0) {
                        if (dist <= retRadius) {
                            builder.include(markerList.get(i).getPosition());
                        }
                    }
                }
                if (bounds == null) {
                    bounds = builder.build();
                } else {
                    bounds = null;
                    bounds = builder.build();
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(
                            this);
                } else {
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(
                            this);
                }
                if (bounds != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                            100));
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "No Location to display", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    public void showMyLocation() {
        LatLng latLng1;
        try {

            if (myLatitude != 0.0
                    && myLongitude != 0.0) {
                latLng1 = new LatLng(myLatitude, myLongitude);
                if (currLocMarker == null) {
                    currLocMarkerOption = new MarkerOptions()
                            .position(latLng1)
                            .title("My Location")
                            .snippet("I'm here")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.markergreen));
                    markerList.add(currLocMarkerOption);
                    currLocMarker = mMap.addMarker(currLocMarkerOption);
                } else {
                    markerList.remove(currLocMarkerOption);
                    currLocMarkerOption = null;
                    currLocMarker.remove();
                    currLocMarker = null;
                    currLocMarkerOption = new MarkerOptions()
                            .position(latLng1)
                            .title("My Location")
                            .snippet("I'm here")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.markergreen));
                    markerList.add(currLocMarkerOption);
                    currLocMarker = mMap.addMarker(currLocMarkerOption);
                }
                onGlobalLayout();
            } else {
                Toast.makeText(
                        getActivity().getApplicationContext(),
                        "Check Network Connection for getting current location",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        String mode;
        if (isBywalk)
            mode = "mode=walking";
        else
            mode = "mode=driving";

        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + Double.toString(sourcelat) + "," + Double.toString(sourcelog) +
                "&destination=" + Double.toString(destlat) + "," + Double.toString(destlog) + "&sensor=false&" + mode + "&alternatives=true";
    }

    public void drawPath(String result) {

        try {
            // Transform the String into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                line.add(mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(6).color(Color.argb(200, 200, 90, 50))
                        .geodesic(true)));
            }

        } catch (JSONException e) {
            Commons.printException("" + e);

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private String trimString(String string, int limit) {
        if (string.length() > limit) {
            string = string.substring(0, limit) + "...";
        }
        return string;
    }

    private void getRetailer() {

        addMarkersToMap();
        zoomToLocation();
        Commons.print("markerList.size()," + Integer.toString(markerList.size()));
    }

    private void addMarkersToMap() {
        markersLst = new ArrayList<>();
        Marker tempMarker;
        try {
            for (int i = 0; i < markerList.size(); i++) {
                LatLng latLng = markerList.get(i).getPosition();
                double lat = latLng.latitude;
                double lng = latLng.longitude;
                float dist = LocationUtil.calculateDistance(lat, lng);
                if (lat != 0.0 && lng != 0.0 && LocationUtil.latitude != 0.0 && LocationUtil.longitude != 0.0) {
                    if (dist <= retRadius) {
                        tempMarker = mMap.addMarker(markerList.get(i).icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    } else
                        tempMarker = mMap.addMarker(markerList.get(i).icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                } else
                    tempMarker = mMap.addMarker(markerList.get(i).icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                tempMarker.setSnippet(markerList.get(i).getSnippet() + "\n" +
                        formatDist(dist));
                markersLst.add(tempMarker);
            }
            mMap.setOnMarkerClickListener(PlanningMapFragment.this);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void zoomToLocation() {
        try {
            final View mainView = this.getView();
            if (mainView != null && mainView.getViewTreeObserver().isAlive()) {
                mainView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void removeMarkersfromMap() {
        mMap.clear();
    }


    /**
     * Check Network Connection
     */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Commons.print("Internet Check," + "Internet Connection Not Present");
            return false;
        }
    }

    public void updateMarker() {
        try {
            for (int i = 0; i < markersLst.size(); i++) {
                LatLng latLng = markerList.get(i).getPosition();
                double lat = latLng.latitude;
                double lng = latLng.longitude;
                float dist = LocationUtil.calculateDistance(lat, lng);
                Marker tempMarker = markersLst.get(i);
                String str_snippet = tempMarker.getSnippet().split("\n")[0];
                if (lat != 0.0 && lng != 0.0 && LocationUtil.latitude != 0.0 && LocationUtil.longitude != 0.0) {
                    if (dist <= retRadius) {
                        tempMarker.setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    } else
                        tempMarker.setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else
                    tempMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                tempMarker.setSnippet(str_snippet + "\n"
                        + formatDist(dist));
            }
            mMap.setOnMarkerClickListener(PlanningMapFragment.this);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void nearByRetailerDialog() {
        final ViewGroup nullParent = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.nearbyret_dialog, nullParent);
        final TextView textView = (TextView) view.findViewById(R.id.textValue);
        builder.setTitle(getActivity().getResources().getString(R.string.dist_find_near_ret));
        builder.setView(view);
        SeekBar seek1 = (SeekBar) view.findViewById(R.id.dialog_seekbar);
        seek1.setProgress(retRadius / 50);
        textView.setText(formatDist(retRadius));
        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress * 50;
                textView.setText(formatDist(progress * 50));
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // to do onStartTrackingTouch

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // to do onStopTrackingTouch
            }

        });

        builder.setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                retRadius = prog;
                updateMarker();
            }
        });
        builder.setNegativeButton(getActivity().getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // set onClick
                    }
                });
        bmodel.applyAlertDialogTheme(builder);
    }

    public interface DataPulling {
        List<MarkerOptions> getData();

        void switchVisitView();
    }

    public void setDataPull(DataPulling dataPull) {
        this.dataPull = dataPull;
    }

    private class CustomInfoWindowAdapter implements InfoWindowAdapter {


        public CustomInfoWindowAdapter() {
            // CustomInfoWindowAdapter
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            if (marker.getTitle().toString().equals("My Location")) {
                infoDistance.setVisibility(View.GONE);
                startVisitLty.setVisibility(View.GONE);
                startVisitBtn.setVisibility(View.GONE);
                infoTitle.setText(trimString(marker.getTitle(), 20));
                infoSnippet.setText(marker.getSnippet());

            } else {
                infoDistance.setVisibility(View.VISIBLE);
                startVisitLty.setVisibility(View.VISIBLE);
                startVisitBtn.setVisibility(View.VISIBLE);
                String[] str_snippet = marker.getSnippet().split("\n");
                String str_title = marker.getTitle().split(",")[0];
                infoTitle.setText(trimString(str_title, 20));
                infoSnippet.setText(str_snippet[0]);
                infoDistance.setText(str_snippet[1]);
            }
            infoButtonListener.setMarker(marker);
            mainLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    public class JSONParser {

        InputStream is = null;
        String json = "";

        public JSONParser() {
        }

        public String getJSONFromUrl(String url) {
            try {
                URL urlobj = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlobj.openConnection();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = urlConnection.getInputStream();
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(
//                        is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Commons.printException("Buffer Error," + e);
            }
            return json;

        }
    }

    private class ConnectAsyncTask extends AsyncTask<Void, Void, String> {
        String url;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        ConnectAsyncTask(String urlPass) {
            this.url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder);
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (result != null) {
                drawPath(result);
            }
        }
    }


    private String getTotalVisitActual() {
        String totalActual = "";
        double value = 0.0;

        for (RetailerMasterBO retObj : bmodel.getRetailerMaster()) {
            value += retObj.getVisit_Actual();
        }
        totalActual = bmodel.formatValue(value);

        return totalActual;
    }

    private int getStoreVisited() {
        int count = 0;
        try {
            for (RetailerMasterBO retObj : bmodel.getRetailerMaster()) {
                if (retObj.getIsVisited() != null || retObj.getIsDeviated() != null)
                    if (retObj.getIsVisited().equalsIgnoreCase("Y")
                            && (retObj.getIsToday() == 1 || retObj.getIsDeviated().equalsIgnoreCase("Y"))) {
                        count++;
                    }

            }
        } catch (Exception e) {

        }
        return count;
    }


    private String getTotalAchieved() {
        String strAchieved;
        double value = 0.0;
        for (RetailerMasterBO retObj : bmodel.getRetailerMaster()) {
            if (mRetTgtAchv.containsKey("VST01") || mRetTgtAchv.containsKey("VST02")) {
                value += retObj.getVisit_Actual();
                continue;
            }

            if (mRetTgtAchv.containsKey("VST08")) {
                value += Double.valueOf(retObj.getMslAch());
                continue;
            }

            if (mRetTgtAchv.containsKey("VST09")) {
                value += retObj.getMonthly_acheived();
                continue;
            }

            if (mRetTgtAchv.containsKey("VST17")) {
                retObj.getSalesValue();
            }
        }
        strAchieved = bmodel.formatValue(value);
        return strAchieved;
    }

    public void updateRetailerProperty() {

        mRetailerProp = new HashMap<>();
        for (String code : bmodel.configurationMasterHelper
                .getRetailerPropertyList()) {
            mRetailerProp.put(code, "1");
        }
    }

    private void updateRetailerAttributes() {
        List<VisitConfiguration> visitConfig;
        if (calledBy.equals(MENU_PLANNING)) {
            visitConfig = bmodel.mRetailerHelper.getVisitPlanning();
        } else {
            visitConfig = bmodel.mRetailerHelper.getVisitCoverage();
        }

        for (VisitConfiguration configObj : visitConfig)
            mRetTgtAchv.put(configObj.getCode(), configObj.getDesc());
    }


    private void displayTodayRoute(String filter) {

        int siz = bmodel.getRetailerMaster().size();
        retailer = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWIthSequence = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWithoutSequence = new ArrayList<>();
        if (!bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            /** Add today's retailers. **/
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    }
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase())) {

                            if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                                retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                            } else {
                                retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                            }

                        }
                    } else {
                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }
                        startVistitRetailers = new ArrayList<>();
                        startVistitRetailers.add(bmodel.getRetailerMaster().get(i));


                    }
                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);

            /** Add today'sdeviated retailers. **/
            for (int i = 0; i < siz; i++) {
                if ("Y".equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
                    if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    }
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            }
        } else {
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {

                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase()) ||
                                (bmodel.getRetailerMaster().get(i)
                                        .getRetailerCode().toLowerCase())
                                        .contains(filter.toLowerCase())) {

                            if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                                retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                            } else {
                                retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                            }

                        }
                    } else {
                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }
                    }
                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);
            startVistitRetailers.addAll(retailerWIthSequence);
        }

        tv_storeVisit.setText(retailer.size() + "");

    }


    private void loadData(int beatId, String filter) {

        retailer = new ArrayList<>();
        int siz = bmodel.getRetailerMaster().size();
        if (!bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            for (int i = 0; i < siz; i++) {

                if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                    if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                            && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;
                    } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                        continue;
                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                    if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                        continue;

                    } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                            && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;

                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                    continue;
                }

                if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)
                        && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {

                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }

                }

            }
        } else {
            for (int i = 0; i < siz; i++) {

                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {

                    if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)) {

                        if (filter != null) {
                            if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                    .toLowerCase()).contains(filter.toLowerCase())) {
                                retailer.add(bmodel.getRetailerMaster().get(i));
                            }
                        } else {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }

                    }
                }
            }
        }

    }

    class BeatAdapter extends ArrayAdapter<BeatMasterBO> {

        Context context;
        int resource, textViewResourceId;
        List<BeatMasterBO> items, tempItems, suggestions;

        public BeatAdapter(Context context, int resource, int textViewResourceId, List<BeatMasterBO> items) {
            super(context, resource, textViewResourceId, items);
            this.context = context;
            this.resource = resource;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
            tempItems = new ArrayList<BeatMasterBO>(items); // this makes the difference.
            suggestions = new ArrayList<BeatMasterBO>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row_dropdown, parent, false);
            }
            BeatMasterBO beatMasterBO = items.get(position);
            if (beatMasterBO != null) {
                TextView lblName = (TextView) view.findViewById(R.id.lbl_name);
                if (lblName != null)
                    lblName.setText(beatMasterBO.getBeatDescription());
            }
            return view;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        /**
         * Custom Filter implementation for custom suggestions we provide.
         */
        Filter nameFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                String str = ((BeatMasterBO) resultValue).toString();
                return str;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (BeatMasterBO bmBO : tempItems) {
                        if (bmBO.toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(bmBO);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (constraint != null) {
                    List<BeatMasterBO> filterList = (ArrayList<BeatMasterBO>) results.values;
                    if (results != null && results.count > 0) {
                        clear();
                        for (BeatMasterBO flList : filterList) {
                            add(flList);
                            notifyDataSetChanged();
                        }
                    }
                } else {
                    clear();
                    for (BeatMasterBO flList : tempItems) {
                        add(flList);
                        notifyDataSetChanged();
                    }

                }


            }
        };
    }

    private boolean hasOrderScreenEnabled() {
        List<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
        for (ConfigureBO configureBO : menuDB) {
            if ((("MENU_ORDER").equals(configureBO.getConfigCode()) ||
                    (MENU_STK_ORD).equals(configureBO.getConfigCode()) && configureBO.getHasLink() == 1)) {
                return true;
            }
        }
        return false;
    }

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    (ViewGroup) getActivity().findViewById(R.id.layout_root));

            TextView title = (TextView) layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) layout.findViewById(R.id.text);
            messagetv.setText("Fetching route, Please wait...");

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

//    private boolean isSurveyDone(String menucode, String rid) {
//        boolean flag = false;
//        try {
//            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
//                    DataMembers.DB_PATH);
//            db.openDataBase();
//            Cursor c = db.selectSQL("select uid from "
//                    + DataMembers.tbl_AnswerHeader + " where retailerid="
//                    + bmodel.QT(rid)
//                    + " and menucode=" + bmodel.QT(menucode));
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    flag = true;
//                }
//                c.close();
//            }
//
//            db.closeDB();
//        } catch (Exception e) {
//            Commons.printException(e);
//        }
//        return flag;
//    }
//
//    private void setRetailerDoneforNoOrderMenu(ArrayList<RetailerMasterBO> retailer) {
//        List<TempBO> outletDetails = null;
//        try {
//            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
//                    DataMembers.DB_PATH);
//            db.openDataBase();
//            Cursor c = db.selectSQL("SELECT DISTINCT ModuleCode, RetailerID FROM OutletTimeStampDetail INNER JOIN HhtMenuMaster ON (HHTCode = ModuleCode  AND MenuType = 'ACT_MENU' AND FLAG =1 AND hasLink = 1) WHERE (ModuleCode <>'MENU_CLOSE_CALL') ORDER BY RetailerID");
//            if (c != null) {
//                outletDetails = new ArrayList<>();
//                while (c.moveToNext()) {
//                    TempBO bo = new TempBO();
//                    bo.setModuleCode(c.getString(0));
//                    bo.setRetailerId(c.getString(1));
//                    outletDetails.add(bo);
//                }
//                c.close();
//            }
//            db.closeDB();
//            for (RetailerMasterBO ret : retailer) {
//                if (!ret.isSurveyDone())
//                    if (outletDetails != null && !outletDetails.isEmpty()) {
//                        for (TempBO tBo : outletDetails) {
//                            if (tBo.getRetailerId().equals(ret.getRetailerID()))
//                                if (isSurveyDone(tBo.getModuleCode(), ret.getRetailerID())) {
//                                    ret.setIsSurveyDone(true);
//                                    break;
//                                }
//                        }
//                    }
//            }
//        } catch (Exception e) {
//            Commons.printException(e);
//        }
//    }

//    private class TempBO {
//        private String moduleCode;
//        private String retailerId;
//
//        public String getModuleCode() {
//            return moduleCode;
//        }
//
//        public void setModuleCode(String moduleCode) {
//            this.moduleCode = moduleCode;
//        }
//
//        public String getRetailerId() {
//            return retailerId;
//        }
//
//        public void setRetailerId(String retailerId) {
//            this.retailerId = retailerId;
//        }
//    }
}
