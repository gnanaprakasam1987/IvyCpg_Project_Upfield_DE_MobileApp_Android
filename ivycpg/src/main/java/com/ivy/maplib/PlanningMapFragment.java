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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.cpg.view.profile.CommonReasonDialog;
import com.ivy.location.LocationUpdater;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.utils.NetworkUtils;

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
import java.util.List;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class PlanningMapFragment extends SupportMapFragment implements
        OnMyLocationButtonClickListener, OnGlobalLayoutListener,
        OnMarkerClickListener, OnInfoWindowClickListener, LocationUpdater, OnMapReadyCallback, CommonReasonDialog.AddNonVisitListener {

    int prog = 0;
    private DataPulling dataPull;
    private int mClick = 0;
    private double myLatitude, myLongitude;
    private GoogleMap mMap;
    private List<MarkerOptions> markerList = new ArrayList<>();
    private Marker currLocMarker;
    private MarkerOptions currLocMarkerOption;
    private LatLng[] markerLatLng = new LatLng[2];
    private Vector<Polyline> line;
    private String toText;
    private LatLngBounds bounds = null;
    private LatLngBounds.Builder builder = null;
    private TextView fromTv, toTv;
    private View rootView;
    private MapWrapperLayout mainLayout;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoProfile;
    private Button infoDeviate;
    private ImageButton carDirBtn;
    private ImageButton walkDirBtn;
    private LayoutInflater layInflater;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoDeviateListener;
    private boolean showToast = true;
    private BusinessModel bmodel;
    private int retRadius = 10;
    private boolean isLocationUpdated = false;
    ImageView visitView;
    FloatingActionButton fab1, fab2, fab3, fab4;
    private LinearLayout bottomLayout;
    private Marker rmarker;
    private String calledBy;
    CardView cardView, cardView1;
    private AutoCompleteTextView mBrandAutoCompleteTV;
    TextView tv_storeVisit;
    private static final String MENU_VISIT = "Trade Coverage";
    private ArrayList<RetailerMasterBO> retailer = new ArrayList<>();
    private String mSelecteRetailerType = "ALL";
    private static final String CODE_PRODUCTIVE = "Filt_01";
    private static final String CODE_NON_PRODUCTIVE = "Filt_02";
    private static final String CODE_VISITED = "Filt_03";
    private static final String CODE_QDVP3 = "Filt_04";
    private static final String CODE_INDICATIVE = "Filt_05";
    private static final String CODE_GOLDEN_STORE = "FIlt_06";
    private static final String CODE_DEAD_STORE = "Filt_07";
    private static final String CODE_HANGING = "Filt_08";
    public boolean profileclick, isclickable;
    private boolean isBywalk = false;
    private boolean isRoute = false;
    private int mSelectedSubId = -1;

    private RetailerMasterBO mSelectedRetailer;
    private Marker mSelectedMarker;
    boolean infoClicked;
    private  DisplayMetrics displaymetrics;


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

        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_planning_map, container,
                false);

        displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        layInflater = inflater;
        visitView = rootView.findViewById(R.id.visit_viewchange);
        TextView fromTxt = rootView.findViewById(R.id.from_txtid);
        TextView toTxt = rootView.findViewById(R.id.to_txtid);
        fromTv = rootView.findViewById(R.id.from_txt_value);
        toTv = rootView.findViewById(R.id.to_txt_value);
        fab1 = rootView.findViewById(R.id.fab);
        fab2 = rootView.findViewById(R.id.fab2);
        fab3 = rootView.findViewById(R.id.fab3);
        fab4 = rootView.findViewById(R.id.fab4);
        cardView = rootView.findViewById(R.id.card_view);
        cardView1 = rootView.findViewById(R.id.card_view1);
        carDirBtn = rootView.findViewById(R.id.car_direction);
        walkDirBtn = rootView.findViewById(R.id.walk_direction);
        bottomLayout = rootView.findViewById(R.id.bottom_view);
        ImageButton clearRouteBtn = rootView.findViewById(R.id.clear_route_id);
        ImageView crossLine = rootView.findViewById(R.id.cross_line);


        // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
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
        Spinner daySpinner = rootView.findViewById(R.id.routeSpinner);


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

        mBrandAutoCompleteTV = rootView.findViewById(R.id.autoCompleteTextView1);
        mBrandAutoCompleteTV.setAdapter(brandAdapter);
        mBrandAutoCompleteTV.setThreshold(1);
        mBrandAutoCompleteTV.setSelection(0);


        bmodel.daySpinnerPositon = 0;
        BeatMasterBO beatmasterbo = brandAdapter.getItem(0);
        bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
        loadData(beatmasterbo.getBeatId());
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
                loadData(beatmasterbo.getBeatId());
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
                loadData(beatmasterbo.getBeatId());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /** End of show all routes **/
        if (getArguments() != null)
            calledBy = getArguments().getString("From");

        if (calledBy == null)
            calledBy = MENU_VISIT;

        bmodel.mRetailerHelper.IsRetailerGivenNoVisitReason();

        TextView tv_areaLoc = rootView.findViewById(R.id.daytv);
        tv_areaLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_areaLoc.setText(bmodel.getDay(bmodel.userMasterHelper
                .getUserMasterBO().getDownloadDate()));

        TextView lbl_BeatLoc = rootView.findViewById(R.id.label_BeatLoc);
        lbl_BeatLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        ImageView img_beatloc = rootView.findViewById(R.id.img_beatloc);

        ArrayList<String> weekdays = new ArrayList<>();
        weekdays.add("Sunday");
        weekdays.add("Monday");
        weekdays.add("Tuesday");
        weekdays.add("Wednesday");
        weekdays.add("Thursday");
        weekdays.add("Friday");
        weekdays.add("Saturday");

        if (weekdays.contains(tv_areaLoc.getText().toString())) {
            lbl_BeatLoc.setText(getResources().getString(R.string.day_plan));
            img_beatloc.setImageResource(R.drawable.ic_calendar_visit);
        } else {
            lbl_BeatLoc.setText(getResources().getString(R.string.beat_loc));
            img_beatloc.setImageResource(R.drawable.arealocation);
        }


        try {
            if (bmodel.labelsMasterHelper.applyLabels(rootView.findViewById(
                    R.id.label_BeatLoc).getTag()) != null)
                ((TextView) rootView.findViewById(R.id.label_BeatLoc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(rootView.findViewById(
                                        R.id.label_BeatLoc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
            if (weekdays.contains(tv_areaLoc.getText().toString())) {
                lbl_BeatLoc.setText(getResources().getString(R.string.day_plan));
                img_beatloc.setImageResource(R.drawable.ic_calendar_visit);
            } else {
                lbl_BeatLoc.setText(getResources().getString(R.string.beat_loc));
                img_beatloc.setImageResource(R.drawable.arealocation);
            }
        }

        TextView lbl_StoreToVisit = rootView.findViewById(R.id.label_StoreToVisit);
        lbl_StoreToVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView lbl_TodayTgt = rootView.findViewById(R.id.label_TodayTgt);
        lbl_TodayTgt.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView spinnerLabel = rootView.findViewById(R.id.spinnerLabel);
        spinnerLabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        tv_storeVisit = rootView.findViewById(R.id.tv_store_visit);
        tv_storeVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_storeVisit.setText(String.valueOf(retailer.size()));

        TextView tv_target = rootView.findViewById(R.id.tv_tgt);
        tv_target.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT) {
            tv_target.setText(String.valueOf(getStoreVisited()));
        } else {
            tv_target.setText(getTotalVisitActual());
        }

        TextView tv_target1 = rootView.findViewById(R.id.tv_tgt1);
        tv_target1.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT) {
            tv_target1.setText(String.valueOf(getStoreVisited()));
        } else {
            tv_target1.setText(getTotalVisitActual());
        }
        TextView lbl_TodayTgt1 = rootView.findViewById(R.id.label_TodayTgt1);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                    } else {
                        carDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                    }
                    carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.highlighter));
                    //walk iocn's
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                    } else {
                        walkDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                    }
                    walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.divider_view_color));
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
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType((mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE :
                        GoogleMap.MAP_TYPE_NORMAL);
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
                }
                //car icon's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                } else {
                    walkDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                }
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.highlighter));
                //walk iocn's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                } else {
                    carDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                }
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.divider_view_color));
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
                }
                //car icon's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                } else {
                    carDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                }
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.highlighter));
                //walk iocn's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                } else {
                    walkDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                }
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.divider_view_color));
            }
        });

        clearRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclickable = false;
                isBywalk = false;
                clearRoute();
                //car icon's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    carDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                } else {
                    carDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.map_button_round_corner_white));
                }
                carDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.highlighter));
                //walk iocn's
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    walkDirBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                } else {
                    walkDirBtn.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_corner_transparent));
                }
                walkDirBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.divider_view_color));
            }
        });

        ConstraintLayout constraint_legends = rootView.findViewById(R.id.constraint_legends);

        ImageView img_info_legends = rootView.findViewById(R.id.img_legends_info);
        img_info_legends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!infoClicked) {
                    constraint_legends.setVisibility(View.VISIBLE);
                    infoClicked = true;
                } else {
                    constraint_legends.setVisibility(View.GONE);
                    infoClicked = false;
                }
            }
        });

        TextView tvAll = rootView.findViewById(R.id.tv_all);
        Switch plan_switch = rootView.findViewById(R.id.switch_plan);

       plan_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               String strPlannned;
               if (isChecked)
                   strPlannned = getResources().getString(R.string.all);
               else
                   strPlannned = getResources().getString(R.string.day_plan);


               tvAll.setText(strPlannned);
               if (mMap != null)
                   mMap.clear();
               displayTodayRoute(strPlannned);
               addMarkersToMap();
               showMyLocation();
               onGlobalLayout();
           }
       });

        return rootView;
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        line = new Vector<>();
        mainLayout = rootView
                .findViewById(R.id.planningmapnew);

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

        if (mSelectedRetailer != null && mSelectedMarker != null)
            updateRetailer();
    }


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
                SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                supportMapFragment.getMapAsync(this);

                this.infoWindow = (ViewGroup) layInflater.inflate(
                        R.layout.custom_info_window, nullParent);
                this.infoTitle = infoWindow.findViewById(R.id.title);
                this.infoSnippet = infoWindow
                        .findViewById(R.id.snippet);
                this.infoProfile = infoWindow
                        .findViewById(R.id.btn_profile);
                this.infoDeviate = infoWindow
                        .findViewById(R.id.btn_deviate);

                this.infoButtonListener = new OnInfoWindowElemTouchListener(infoProfile) {
                    @Override
                    protected void onClickConfirmed(View v, Marker marker) {
                        if (!getResources().getString(R.string.my_location).equals(marker.getTitle())) {
                            for (RetailerMasterBO startVisitBo : retailer) {
                                if (startVisitBo.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                                    bmodel.setRetailerMasterBO(startVisitBo);
                                    if (!profileclick) {
                                        mSelectedMarker = marker;
                                        mSelectedRetailer = startVisitBo;
                                        profileclick = true;
                                        bmodel.newOutletHelper.downloadLinkRetailer();
                                        Intent i = new Intent(getActivity(), ProfileActivity.class);
                                        i.putExtra("From", MENU_VISIT);
                                        i.putExtra("locvisit", true);
                                        i.putExtra("map", true);
                                        if ("N".equals(marker.getTitle().split(",")[2]))
                                        i.putExtra("hometwo", true);
                                        startActivity(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                };
                infoProfile.setOnTouchListener(infoButtonListener);

                this.infoDeviateListener = new OnInfoWindowElemTouchListener(infoDeviate) {
                    @Override
                    protected void onClickConfirmed(View v, Marker marker) {
                        if (!getResources().getString(R.string.my_location).equals(marker.getTitle())) {
                            for (RetailerMasterBO startVisitBo : retailer) {
                                if (startVisitBo.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                                    bmodel.setRetailerMasterBO(startVisitBo);
                                    if (!profileclick) {
                                        mSelectedMarker = marker;
                                        mSelectedRetailer = startVisitBo;
                                        profileclick = true;
                                        CommonReasonDialog comReasonDialog = new CommonReasonDialog(getActivity(), "deviate");
                                        comReasonDialog.setNonvisitListener(PlanningMapFragment.this);
                                        comReasonDialog.show();
                                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                        Window window = comReasonDialog.getWindow();
                                        lp.copyFrom(window != null ? window.getAttributes() : null);
                                        lp.width = displaymetrics.widthPixels - 100;
                                        lp.height = (displaymetrics.heightPixels / 2);
                                        if (window != null) {
                                            window.setAttributes(lp);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                };
                infoDeviate.setOnTouchListener(infoDeviateListener);

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setUpMap() {
        try {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.permission_enable_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.setMyLocationEnabled(false);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        } catch (Exception e) {
            Commons.printException(e);
        }


        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()
                        .getApplicationContext());

        try {


            if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                displayTodayRoute("PLANNED");
            }

            if (resultCode == ConnectionResult.SUCCESS) {
                setUpMapIfNeeded();


                if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getResources().getString(R.string.enable_gps), Toast.LENGTH_LONG).show();
                }

                getRetailer();
            } else {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 1);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void locationUpdate() {
        myLatitude = LocationUtil.latitude;
        myLongitude = LocationUtil.longitude;
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
        if (NetworkUtils.isNetworkConnected(getActivity())) {
            try {
                if (mClick == 0) {
                    markerLatLng[0] = mMarker.getPosition();
                    testStr = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                            : mMarker.getTitle().split(",")[0];
                    fromTv.setText(testStr);
                    mClick = 1;
                    mMarker.hideInfoWindow();
                } else if (mClick == 1) {
                    rmarker = mMarker;
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                            : mMarker.getTitle().split(",")[0];
                    toTv.setText(testToRoute);
                    if (!isSameLocation(mMarker))
                        drawRoute(mMarker);

                } else if (mClick == 2) {
                    mClick = -1;
                    rmarker = mMarker;
                    testStr = toText;
                    fromTv.setText(testStr);
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = mMarker.getTitle();
                    toTv.setText(testToRoute);
                    clearRoute();
                    if (mClick == 1) {
                        if (!isSameLocation(mMarker))
                            drawRoute(mMarker);
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSameLocation(Marker mMarker) {
        return (markerLatLng[0].latitude == mMarker.getPosition().latitude && markerLatLng[0].longitude == mMarker
                .getPosition().longitude);
    }

    private void drawRoute(Marker mMarker) {
        String url;
        mMarker.hideInfoWindow();
        markerLatLng[1] = mMarker.getPosition();
        toText = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                : mMarker.getTitle().split(",")[0];
        url = makeURL(markerLatLng[0].latitude, markerLatLng[0].longitude,
                markerLatLng[1].latitude, markerLatLng[1].longitude);
        ConnectAsyncTask getRoute = new ConnectAsyncTask(url);
        getRoute.execute();
        mClick = 2;
    }

    public void showMeOnMap() {
        try {
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLatitude,
                        myLongitude)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.route_cleared),
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
            Commons.printException(e);
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

                mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(
                        this);
                if (bounds != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                            100));
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_loc_to_display), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
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
                            .title(getResources().getString(R.string.my_location))
                            .snippet(getResources().getString(R.string.am_here))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.user_loc));
                    markerList.add(currLocMarkerOption);
                    currLocMarker = mMap.addMarker(currLocMarkerOption);
                } else {
                    markerList.remove(currLocMarkerOption);
                    currLocMarkerOption = null;
                    currLocMarker.remove();
                    currLocMarker = null;
                    currLocMarkerOption = new MarkerOptions()
                            .position(latLng1)
                            .title(getResources().getString(R.string.my_location))
                            .snippet(getResources().getString(R.string.am_here))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_loc));
                    markerList.add(currLocMarkerOption);
                    currLocMarker = mMap.addMarker(currLocMarkerOption);
                }
                onGlobalLayout();
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(R.string.check_network_curr_loc),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        String mode;
        if (isBywalk)
            mode = "mode=walking";
        else
            mode = "mode=driving";

        String mapKey = "key=" + getString(R.string.google_maps_api_key);

        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + Double.toString(sourcelat) + "," + Double.toString(sourcelog) +
                "&destination=" + Double.toString(destlat) + "," + Double.toString(destlog) +
                "&sensor=false&" + mode + "&alternatives=true" + "&" + mapKey;
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
            Commons.printException(e);

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

    private void getRetailer() {
        addMarkersToMap();
        zoomToLocation();
    }

    private void addMarkersToMap() {
        try {
            for (int i = 0; i < markerList.size(); i++) {
                mMap.addMarker(markerList.get(i));
            }
            mMap.setOnMarkerClickListener(PlanningMapFragment.this);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void zoomToLocation() {
        try {
            final View mainView = this.getView();
            if (mainView != null && mainView.getViewTreeObserver().isAlive()) {
                mainView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void nearByRetailerDialog() {
        final ViewGroup nullParent = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.nearbyret_dialog, nullParent);
        final TextView textView = view.findViewById(R.id.textValue);
        builder.setTitle(getActivity().getResources().getString(R.string.dist_find_near_ret));
        builder.setView(view);
        SeekBar seek1 = view.findViewById(R.id.dialog_seekbar);
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
                //updateMarker();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        float pxlDp = 39 + 20;
        mainLayout.init(mMap, getPixelsFromDp(PlanningMapFragment.this.getActivity(), pxlDp));
    }

    public interface DataPulling {
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
            if (getResources().getString(R.string.my_location).equals(marker.getTitle())) {
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoDeviate.setVisibility(View.GONE);
                infoProfile.setVisibility(View.GONE);
            } else {
                infoProfile.setVisibility(View.VISIBLE);
                String[] str_snippet = marker.getSnippet().split("\n");
                String str_title = marker.getTitle().split(",")[0];
                infoTitle.setText(str_title);
                infoSnippet.setText(str_snippet[0]);
                String isPlanned = marker.getTitle().split(",")[2];
                if ("Y".equals(isPlanned))
                    infoDeviate.setVisibility(View.GONE);
                 else
                    infoDeviate.setVisibility(View.VISIBLE);

            }
            infoButtonListener.setMarker(marker);
            infoDeviateListener.setMarker(marker);
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
                Commons.printException(e);
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
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
            Commons.printException(e);
        }
        return count;
    }

    private void displayTodayRoute(String displayAll) {

        int siz = bmodel.getRetailerMaster().size();
        retailer.clear();
        ArrayList<RetailerMasterBO> retailerWIthSequence = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWithoutSequence = new ArrayList<>();
        if (!bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            if ("PLANNED".equalsIgnoreCase(displayAll)) {
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
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    }
                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }

                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);

            /** Add today'sdeviated retailers. **/
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsDeviated() != null && "Y".equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
                    if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    }

                        retailer.add(bmodel.getRetailerMaster().get(i));
                }
            }

        } else {
            for (int i = 0; i < siz; i++) {

                retailer.add(bmodel.getRetailerMaster().get(i));

            }
        }

        } else {
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {


                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }

                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);
        }

        tv_storeVisit.setText(String.valueOf(retailer.size()));

        try {

            if (bmodel.configurationMasterHelper.IS_MAP) {
                markerList.clear();
                LatLng latLng;

                for (int i = 0; i < retailer.size(); i++) {

                    String planned = "N";

                    if ("Y".equals(retailer.get(i).getIsVisited()) || retailer.get(i).getIsToday() == 1 || "Y".equals(retailer.get(i).getIsDeviated()))
                        planned = "Y";

                        latLng = new LatLng(retailer.get(i).getLatitude(), retailer
                                .get(i).getLongitude());
                        MarkerOptions mMarkerOptions = new MarkerOptions()
                                .position(latLng)
                                .title(retailer.get(i).getRetailerName() + "," + retailer.get(i).getRetailerID() + "," + planned)
                                .snippet(retailer.get(i).getAddress1())
                                .icon(BitmapDescriptorFactory
                                        .fromResource(getMarkerIcon(retailer.get(i))));
                        markerList.add(mMarkerOptions);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    private void loadData(int beatId) {

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
                    if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;
                    } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                        continue;
                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                    if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                        continue;

                    } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;

                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                    continue;
                }

                if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)
                        && (bmodel.getRetailerMaster().get(i).getIsDeviated() != null && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeviated()))) {


                        retailer.add(bmodel.getRetailerMaster().get(i));

                }

            }
        } else {
            for (int i = 0; i < siz; i++) {

                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {

                    if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)) {

                            retailer.add(bmodel.getRetailerMaster().get(i));

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
            tempItems = new ArrayList<>(items); // this makes the difference.
            suggestions = new ArrayList<>();
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
                TextView lblName = view.findViewById(R.id.lbl_name);
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
                return resultValue.toString();
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
                    if (results.count > 0) {
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

    private void customProgressDialog(AlertDialog.Builder builder) {

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_alert_dialog,
                    getActivity().findViewById(R.id.layout_root));

            TextView title = layout.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            TextView messagetv = layout.findViewById(R.id.text);
            messagetv.setText(getResources().getString(R.string.fetching_route));

            builder.setView(layout);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void addReatailerReason() {
        showAlert(getResources().getString(
                R.string.saved_successfully));

    }

    @Override
    public void onDismiss() {
        profileclick = false;
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
               updateRetailer();
               profileclick = false;
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    private void updateRetailer() {

        String planned = "N";

        if ("Y".equals(mSelectedRetailer.getIsVisited()) || mSelectedRetailer.getIsToday() == 1 || "Y".equals(mSelectedRetailer.getIsDeviated()))
            planned = "Y";

        mSelectedMarker.setTitle(mSelectedRetailer.getRetailerName() + "," + mSelectedRetailer.getRetailerID() + "," + planned);
        mSelectedMarker.setIcon(BitmapDescriptorFactory
                .fromResource(getMarkerIcon(mSelectedRetailer)));

        mSelectedMarker.hideInfoWindow();
    }

    private int getMarkerIcon(RetailerMasterBO retailerMasterBO) {
        int drawable = R.drawable.marker_visit_unscheduled;

        if ("Y".equals(retailerMasterBO.getIsVisited())) {
            if (("N").equals(retailerMasterBO.isOrdered()))
                drawable = R.drawable.marker_visit_non_productive;
            else
                drawable = R.drawable.marker_visit_completed;
        } else if (retailerMasterBO.getIsToday() == 1 || "Y".equals(retailerMasterBO.getIsDeviated()))
            drawable = R.drawable.marker_visit_planned;


        if (retailerMasterBO.isHasNoVisitReason())
            drawable = R.drawable.marker_visit_cancelled;


        return drawable;
    }

}
