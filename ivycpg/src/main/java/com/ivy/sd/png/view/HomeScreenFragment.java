package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.countersales.CSHomeScreenFragment;
import com.ivy.cpg.primarysale.view.PrimarySaleFragment;
import com.ivy.cpg.view.digitalcontent.DigitalContentFragment;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.survey.SurveyActivityNewFragment;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.cpg.view.van.LoadManagementFragment;
import com.ivy.cpg.view.van.PlanningSubScreenFragment;
import com.ivy.cpg.view.van.StockProposalFragment;
import com.ivy.cpg.view.van.VanStockAdjustActivity;
import com.ivy.ivyretail.service.AlarmReceiver;
import com.ivy.lib.existing.DBUtil;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ChatApplicationHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.OrderSplitHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.reports.ReportMenufragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class HomeScreenFragment extends IvyBaseFragment implements VisitFragment.MapViewListener, PlanningMapFragment.DataPulling {
    private BusinessModel bmodel;

    //used to save the photo
    public static File folder;

    public static String photoPath;

    public static boolean fromHomeScreen = false;

    private static final String MENU_PLANNING_CONSTANT = "Day Planning";
    private static final String MENU_VISIT_CONSTANT = "Trade Coverage";

    private Intent stockpropintent;
    private About dialogFragment;
    private Intent vanUnloadStockAdjustmentIntent = null;
    private Intent planningIntent;

    private static final String MENU_PLANNING = "MENU_PLANNING";
    private static final String MENU_VISIT = "MENU_VISIT";
    private static final String MENU_NEW_RETAILER = "MENU_NEW_RET";
    private static final String MENU_REPORT = "MENU_REPORT";
    private static final String MENU_SYNC = "MENU_SYNC";
    private static final String MENU_LOAD_MANAGEMENT = "MENU_LOAD_MANAGEMENT";
    private static final String MENU_PLANNING_SUB = "MENU_PLANNING_SUB";
    private static final String MENU_LOAD_REQUEST = "MENU_STK_PRO";
    private static final String MENU_STOCK_ADJUSTMENT = "MENU_STOCK_ADJUSTMENT";
    private static final String MENU_PRIMARY_SALES = "MENU_PRIMARY_SALES";
    private static final String MENU_JOINT_CALL = "MENU_JOINT_CALL";
    private static final String MENU_SURVEY_SW = "MENU_SURVEY_SW";
    private static final String MENU_SURVEY01_SW = "MENU_SURVEY01_SW";
    private static final String MENU_SURVEY_BA_CS = "MENU_SURVEY_BA_CS";
    private static final String MENU_SKUWISESTGT = "MENU_SKUWISESTGT";
    private static final String MENU_DASH_KPI = "MENU_DASH_KPI";
    private static final String MENU_DASH = "MENU_DASH";
    private static final String MENU_DASH_DAY = "MENU_DASH_DAY";
    private static final String MENU_DIGITIAL_SELLER = "MENU_DGT_SW";
    private static final String MENU_ATTENDANCE = "MENU_ATTENDANCE";
    private static final String MENU_PRESENCE = "MENU_PRESENCE";
    private static final String MENU_IN_OUT = "MENU_IN_OUT";
    private static final String MENU_LEAVE_APR = "MENU_LEAVE_APR";
    private static final String MENU_REALLOCATION = "MENU_REALLOCATION";
    private static final String MENU_EMPTY_RECONCILIATION = "MENU_EMPTY_RECONCILIATION";
    private static final String MENU_ORDER_FULLFILLMENT = "MENU_FULLFILMENT";
    private static final String MENU_ORDER_SPLIT = "MENU_ORDER_SPLIT";
    private static final String MENU_ROAD_ACTIVITY = "MENU_ROAD_ACTIVITY";
    private static final String MENU_COUNTER = "MENU_COUNTER";
    private static final String MENU_MVP = "MENU_MVP";
    private static final String MENU_EXPENSE = "MENU_EXPENSE";
    private static final String MENU_WVW_PLAN = "MENU_WVW_PLAN";
    private static final String MENU_WEB_VIEW = "MENU_WEB_VIEW";
    private static final String MENU_NEWRET_EDT = "MENU_NEWRET_EDT";
    private static final String MENU_TASK_NEW = "MENU_TASK_NEW";
    private static final String MENU_PLANE_MAP = "MENU_PLANE_MAP";
    //private static final String MENU_COLLECTION_PRINT = "MENU_COLLECTION_PRINT";
    private static final String MENU_GROOM_CS = "MENU_GROOM_CS";
    private static final String MENU_JOINT_ACK = "MENU_JOINT_ACK";
    private static final String MENU_NON_FIELD = "MENU_NON_FIELD";

    //Deleiver MAnagement
    private static final String MENU_DELMGMT_RET = "MENU_DELMGMT_RET";

    private String roadTitle;
    private boolean isClicked;
    public static boolean isLeave_today;
    private boolean isMenuAttendCS = false;
    private boolean isInandOut = false;
    private boolean isVisit;

    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    private Vector<ConfigureBO> leftmenuDB = new Vector<>();

    private ImageView imgIconNotification;
    private TextView tv_counter;
    private int intcounter;
    private TypedArray typearr;

    private ArrayList<ChannelBO> mChannelList;

    //Chat
    // private String CHAT_APP_ID = "28908";
    private String CHAT_AUTHENTICATION_KEY = "mj74gxbHLMvVfHK";
    private String CHAT_AUTHENTICATION_SECRET_KEY = "rQkkQgYJss9UCOA";


    private ActionBar actionBar;

    private homeScreenItemClickedListener mHomeScreenItemClickedListener;

    LinearLayout ll_logout, ll_about;
    ImageView settingView;

    Handler handler;


    private List<MarkerOptions> markerList;
    private LatLng latLng;

    private List<com.baidu.mapapi.map.MarkerOptions> baiduMarkerList;
    com.baidu.mapapi.model.LatLng baidulatLng;
    private ImageView profileImageView;
    private static final int CAMERA_REQUEST_CODE = 1;
    private String imageFileName;
    private ListView listView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_homescreen, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_homescreen));
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (!checkMenusAvailable()) {
            new DeleteTables().execute();
        }

        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);


        bmodel.setOrderSplitScreenTitle(null);

        menuIcons.put(MENU_PLANNING, R.drawable.ic_vector_planning);
        menuIcons.put(MENU_MVP, R.drawable.ic_mvp_icon);
        menuIcons.put(MENU_VISIT, R.drawable.ic_vector_tradecoverage);
        menuIcons.put(MENU_LOAD_MANAGEMENT, R.drawable.ic_load_mgmt_icon);
        menuIcons.put(MENU_NEW_RETAILER, R.drawable.ic_new_retailer_icon);
        menuIcons.put(MENU_LOAD_REQUEST, R.drawable.ic_stock_proposal_icon);
        menuIcons.put(MENU_REPORT, R.drawable.ic_vector_reports);
        menuIcons.put(MENU_SYNC, R.drawable.ic_vector_sync);
        menuIcons.put(MENU_DASH_KPI, R.drawable.ic_vector_dashboard);
        menuIcons.put(MENU_DASH, R.drawable.ic_vector_dashboard);
        menuIcons.put(MENU_DASH_DAY, R.drawable.ic_vector_dashboard);
        menuIcons.put(MENU_SKUWISESTGT, R.drawable.ic_vector_dashboard);
        menuIcons.put(MENU_JOINT_CALL, R.drawable.ic_vector_jointcall);
        menuIcons.put(MENU_ORDER_SPLIT, R.drawable.icon_order_split);
        menuIcons.put(MENU_EMPTY_RECONCILIATION, R.drawable.ic_empty_reconcilation_icon);
        menuIcons.put(MENU_ATTENDANCE, R.drawable.ic_vector_out_of_trade);
        menuIcons.put(MENU_REALLOCATION, R.drawable.ic_reallocation_icon);
        menuIcons.put(MENU_PLANNING_SUB, R.drawable.icon_reports);
        menuIcons.put(MENU_DIGITIAL_SELLER, R.drawable.ic_vector_gallery);
        menuIcons.put(MENU_ROAD_ACTIVITY, R.drawable.icon_reports);
        menuIcons.put(MENU_PRESENCE, R.drawable.ic_vector_out_of_trade);
        menuIcons.put(MENU_IN_OUT, R.drawable.ic_vector_out_of_trade);
        menuIcons.put(MENU_LEAVE_APR, R.drawable.ic_vector_out_of_trade);
        menuIcons.put(MENU_EXPENSE, R.drawable.ic_expense_icon);
        menuIcons.put(MENU_NEWRET_EDT, R.drawable.ic_new_retailer_icon);
        menuIcons.put(MENU_TASK_NEW, R.drawable.ic_new_task);
        menuIcons.put(MENU_SURVEY_SW, R.drawable.ic_survey_icon);
        menuIcons.put(MENU_SURVEY01_SW, R.drawable.ic_survey_icon);
        menuIcons.put(MENU_SURVEY_BA_CS, R.drawable.ic_survey_icon);
        menuIcons.put(MENU_GROOM_CS, R.drawable.ic_survey_icon);
        menuIcons.put(MENU_JOINT_ACK, R.drawable.ic_survey_icon);
        menuIcons.put(MENU_NON_FIELD, R.drawable.ic_vector_planning);

        // Load the HHTMenuTable
        bmodel.configurationMasterHelper.downloadMainMenu();

        if (bmodel.mAttendanceHelper.checkLeaveAttendance())
            isLeave_today = true;

        TextView userNameTv = (TextView) view.findViewById(R.id.tv_username);
        TextView designation = (TextView) view.findViewById(R.id.tv_designation);
        profileImageView = (ImageView) view.findViewById(R.id.im_user);

        listView = (ListView) view.findViewById(R.id.listView1);

        if (bmodel.userMasterHelper.hasProfileImagePath(bmodel.userMasterHelper.getUserMasterBO()))
            setImageFromCamera();
        else
            setProfileImage();

        profileImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                takePhoto();
                return false;
            }
        });
        profileImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                File filePath = null;
                if (bmodel.userMasterHelper.hasProfileImagePath(bmodel.userMasterHelper.getUserMasterBO()) &&
                        bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null
                        && !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {
                    String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
                    String path = imgPaths[imgPaths.length - 1];
                    filePath = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/" + path);
                } else if (bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null
                        && !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {
                    String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
                    String path = imgPaths[imgPaths.length - 1];
                    filePath = new File(getActivity().getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.DIGITAL_CONTENT
                            + "/"
                            + DataMembers.USER + "/"
                            + path);
                }
                if (filePath != null && filePath.exists()) {
                    try {
                        openImage(filePath.getAbsolutePath());
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.unloadimage),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        userNameTv.setText(bmodel.userMasterHelper.getUserMasterBO().getUserName());

        userNameTv.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        designation.setText(bmodel.userMasterHelper.getUserMasterBO().getUserType());
        designation.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        ll_logout = (LinearLayout) view.findViewById(R.id.ll_logout);
        ll_logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                bmodel.synchronizationHelper.backUpDB();
                showDialog(0);
            }
        });

        ll_about = (LinearLayout) view.findViewById(R.id.ll_about);
        ll_about.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), About.class));
            }
        });

        settingView = (ImageView) view.findViewById(R.id.iv_setting);
        settingView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),
                        UserSettingsActivity.class);
                startActivity(i);
            }
        });


       /* ConfigData.setPowerAccuracy(LocationRequest.PRIORITY_HIGH_ACCURACY);*/

        // image path
        photoPath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + DataMembers.photoFolderName;

        //local photopath string will be removed soon
        BusinessModel.photoPath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + DataMembers.photoFolderName;
        folder = new File(BusinessModel.photoPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {

            leftmenuDB.add(con);

            if (con.getConfigCode().equals(MENU_PRESENCE)) {
                isMenuAttendCS = true;
            }
            if (con.getConfigCode().equals(MENU_IN_OUT)) {
                isInandOut = true;
            }
        }

        ListView listView = (ListView) view.findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        listView.setAdapter(new LeftMenuBaseAdapter(leftmenuDB));


        if (bmodel.configurationMasterHelper.ISUPLOADUSERLOC)
            setAlarm();


        /** Initialising map view **/
        markerList = new ArrayList<>();
        baiduMarkerList = new ArrayList<>();
        try {
            if (bmodel.configurationMasterHelper.IS_MAP) {
                MapsInitializer.initialize(getActivity());
                if (bmodel.configurationMasterHelper.IS_BAIDU_MAP)
                    SDKInitializer.initialize(getActivity().getApplication());
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        //showDefaultScreen();
        refreshList(true);

        return view;

    }

    public Handler getHandler() {
        return handler;
    }


    @Override
    public void onStart() {
        super.onStart();
        // showDefaultScreen();

    }

    private void showDefaultScreen() {

        if (leftmenuDB.size() > 0) {
            if (getActivity().getIntent().getStringExtra("menuCode") != null) {
                for (ConfigureBO configureBO : leftmenuDB) {
                    if (configureBO.getConfigCode().equalsIgnoreCase(getActivity().getIntent().getStringExtra("menuCode"))) {
                        gotoNextActivity(configureBO);
                        break;
                    }
                }
            } else {
                // showing first menu by default
                //gotoNextActivity(leftmenuDB.get(0));
                for (ConfigureBO configureBO : leftmenuDB) {
                    if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut && isLeave_today) {
                        if (configureBO.getConfigCode().equalsIgnoreCase(MENU_IN_OUT)) {
                            gotoNextActivity(configureBO);
                            break;
                        }
                    } else if (configureBO.getConfigCode().equalsIgnoreCase(MENU_VISIT)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_KPI)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_DAY)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DIGITIAL_SELLER)) {
                        gotoNextActivity(configureBO);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //  showDefaultScreen();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                Uri uri = bmodel.getUriFromFile(photoPath + "/" + imageFileName);
                bmodel.userMasterHelper.getUserMasterBO().setImagePath(imageFileName);
                bmodel.userMasterHelper.saveUserProfile(bmodel.userMasterHelper.getUserMasterBO());
                profileImageView.invalidate();
                profileImageView.setImageURI(uri);
            }
        }
    }

    private void setAlarm() {
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(
                "TimePref", 0); // 0 - for private mode
        int alarm_time = pref.getInt("AlarmTime", 0);

        boolean alarmUp = (PendingIntent.getBroadcast(getActivity(), 0, new Intent(getActivity(),
                AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            Intent i = new Intent(getActivity(), AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, i, 0);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
            long timeInMillis = System.currentTimeMillis() + (alarm_time * 60 * 1000);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            }

            // Below has been added to enable the receiver manually as we have
            // disabled in the manifest
            ComponentName receiver = new ComponentName(getActivity(),
                    AlarmReceiver.class);
            PackageManager pm = getActivity().getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        //GA screen tracking
        BusinessModel.getInstance().trackScreenView("Home Screen");

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        bmodel.configurationMasterHelper.getPrinterConfig();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        intcounter = bmodel.taskHelper.getTaskCount();

        getActivity().supportInvalidateOptionsMenu();
    }


    private void showDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(
                                getResources().getString(R.string.do_u_want_logout))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        getActivity().finish();
                                        try {
                                            bmodel.synchronizationHelper.backUpDB();
                                            ActivityCompat.finishAffinity(getActivity());

                                        } catch (Exception e) {
                                            Commons.printException(e);
                                        }
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            case 1:
                AlertDialog.Builder builderGPS = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setTitle(getResources().getString(R.string.enable_gps))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        Intent myIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(myIntent);

                                    }
                                });
                bmodel.applyAlertDialogTheme(builderGPS);
                break;
        }
    }


    private void gotoNextActivity(final ConfigureBO menuItem) {

        Commons.print("ATS," + "gotonext activity in home : menuItem.getConfigCode() ="
                + menuItem.getConfigCode());
        if (menuItem.getConfigCode().equals(MENU_PLANNING)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED
                    && (SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!isClicked) {
                    isClicked = false;
                    bmodel.distributorMasterHelper.downloadDistributorsList();
                    bmodel.configurationMasterHelper
                            .setTradecoveragetitle(menuItem.getMenuName());

                    switchFragment(MENU_PLANNING, menuItem.getMenuName());
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_VISIT)) {
            if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
                boolean bool = bmodel.locationUtil.isGPSProviderEnabled();
                if (!bool) {
                    Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
                    if (resultCode == ConnectionResult.SUCCESS)
                        bmodel.requestLocation(getActivity());
                    else
                        showDialog(1);
                    return;
                }

            }


            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!isClicked) {
                    isClicked = false;
                    bmodel.distributorMasterHelper.downloadDistributorsList();
                    bmodel.configurationMasterHelper
                            .setTradecoveragetitle(menuItem.getMenuName());

                    switchFragment(MENU_VISIT, menuItem.getMenuName());
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_ATTENDANCE)) {
            bmodel.mAttendanceHelper.downNonFieldReasons();
            bmodel.mAttendanceHelper.downLeaveTypes();
            bmodel.mAttendanceHelper.dynamicRadioButtton();
            bmodel.configurationMasterHelper.setTradecoveragetitle(menuItem
                    .getMenuName());
            switchFragment(MENU_ATTENDANCE, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_REALLOCATION)) {
            bmodel.mSelectedActivityName = menuItem.getMenuName();
            Intent i = new Intent(getActivity(), TLAttendanceActivity.class);
            bmodel.configurationMasterHelper.setTradecoveragetitle(menuItem
                    .getMenuName());
            startActivity(i);
            getActivity().finish();
        } else if (menuItem.getConfigCode().equals(MENU_NEW_RETAILER)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") != 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                bmodel.mSelectedActivityName = menuItem.getMenuName();
                mChannelList = bmodel.newOutletHelper.getChannelList();
                if (mChannelList != null && mChannelList.size() > 0) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    ChannelSelectionDialogFragment dialogFragment = new ChannelSelectionDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title", bmodel.newOutletHelper.getLevelame());
                    bundle.putString("screentitle", menuItem.getMenuName());
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(fm, "Sample Fragment");
                    dialogFragment.setCancelable(false);
                } else {
                    Toast.makeText(getActivity(), "Channel Not Mapped ", Toast.LENGTH_SHORT).show();
                }


            } else {
                switchFragment(MENU_NEW_RETAILER, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_REPORT)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {

                switchFragment(MENU_REPORT, menuItem.getMenuName());
//                Intent reportintent = new Intent(getActivity(),
//                        ReportMenuActivity.class);
//                reportintent.putExtra("screentitle", menuItem.getMenuName());
//                bmodel.productHelper.downloadProductFilter("MENU_STK_ORD");
//                reportintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(reportintent);
            }
        } else if (menuItem.getConfigCode().equals(MENU_LOAD_MANAGEMENT)) {

            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_LOAD_MANAGEMENT, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_PLANNING_SUB)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_PLANNING_SUB, menuItem.getMenuName());

//                Intent i = new Intent(getActivity(), PlanningSubScreen.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                bmodel.configurationMasterHelper
//                        .setLoadplanningsubttitle(menuItem.getMenuName());
//                startActivity(i);
//                getActivity().finish();
            }

        } else if (menuItem.getConfigCode().equals(MENU_SYNC)) {
            switchFragment(MENU_SYNC, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_SKUWISESTGT)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_SKUWISESTGT, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_LOAD_REQUEST)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
//                stockpropintent = new Intent(getActivity(),
//                        StockProposalScreen.class);
//                stockpropintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_T

                new DownloadStockProposal().execute();
                switchFragment(MENU_LOAD_REQUEST, menuItem.getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_DASH_KPI)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                bmodel.dashBoardHelper.checkDayAndP3MSpinner();
                bmodel.distributorMasterHelper.downloadDistributorsList();

                switchFragment(MENU_DASH_KPI, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_DASH)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_DASH, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_DASH_DAY)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_DASH_DAY, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_STOCK_ADJUSTMENT)) {

            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {

                vanUnloadStockAdjustmentSubroutine(menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_ORDER_SPLIT)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                // Creating order split helper
                bmodel.orderSplitHelper = OrderSplitHelper.clearInstance();
                bmodel.orderSplitHelper = OrderSplitHelper.getInstance(bmodel);
                bmodel.orderSplitHelper.setLast_split_master_index(0);

                checkForOrderSplitRecordAndGotoOrderSplit(menuItem
                        .getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_JOINT_CALL)) {
            if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_JOINT_CALL, menuItem
                        .getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_SURVEY_SW)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else {

                if (!bmodel.configurationMasterHelper.IS_ALLOW_SURVEY_WITHOUT_JOINTCALL)
                    bmodel.userMasterHelper.downloadJoinCallusers();
                bmodel.userMasterHelper.downloadDistributionDetails();

                if (bmodel.configurationMasterHelper.IS_ALLOW_SURVEY_WITHOUT_JOINTCALL || bmodel.outletTimeStampHelper
                        .isJointCall(bmodel.userMasterHelper.getUserMasterBO()
                                .getJoinCallUserList())) {

                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());

                    surveyHelperNew.setFromHomeScreen(true);

                    surveyHelperNew.downloadModuleId("SPECIAL");
                    surveyHelperNew.downloadQuestionDetails("MENU_SURVEY_SW");

                    surveyHelperNew
                            .loadSurveyAnswers(surveyHelperNew
                                    .getSuperVisiroID());

                    if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY) {

                        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                            bmodel.productHelper
                                    .downloadFiveLevelFilterNonProducts(MENU_SURVEY_SW);
                        else
                            bmodel.productHelper
                                    .downloadProductFilter(MENU_SURVEY_SW);
                    }

                    if (surveyHelperNew.getSurvey() != null
                            && surveyHelperNew.getSurvey().size() > 0) {
                        bmodel.mSelectedActivityName = menuItem.getMenuName();
                        bmodel.mSelectedActivityConfigCode = menuItem
                                .getConfigCode();
                        surveyHelperNew.loadSurveyConfig(MENU_SURVEY_SW);
                        switchFragment(MENU_SURVEY_SW, menuItem.getMenuName());
                    } else {

                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.data_not_mapped), 0);
                    }

                } else {
                    Toast.makeText(getActivity(),
                            "Please login joint call user", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_SURVEY01_SW)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else {
                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
                surveyHelperNew.setFromHomeScreen(true);

                surveyHelperNew.downloadModuleId("SPECIAL");
                surveyHelperNew.downloadQuestionDetails("MENU_SURVEY01_SW");

                if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY) {
                    if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                        bmodel.productHelper
                                .downloadFiveLevelFilterNonProducts("MENU_SURVEY01_SW");
                    else
                        bmodel.productHelper
                                .downloadProductFilter("MENU_SURVEY01_SW");
                }

                if (surveyHelperNew.getSurvey() != null
                        && surveyHelperNew.getSurvey().size() > 0) {
                    bmodel.mSelectedActivityName = menuItem.getMenuName();
                    bmodel.mSelectedActivityConfigCode = menuItem
                            .getConfigCode();
                    switchFragment(MENU_SURVEY01_SW, menuItem.getMenuName());
                } else {

                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.data_not_mapped), 0);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_SURVEY_BA_CS)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else {
                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
                surveyHelperNew.setFromHomeScreen(true);
                surveyHelperNew.setFromCSsurvey(false);

                surveyHelperNew.downloadModuleId("SPECIAL");
                surveyHelperNew.downloadQuestionDetails(MENU_SURVEY_BA_CS);

                surveyHelperNew
                        .loadSurveyAnswers(surveyHelperNew
                                .getSuperVisiroID());

                if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY) {

                    if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                        bmodel.productHelper
                                .downloadFiveLevelFilterNonProducts(MENU_SURVEY_BA_CS);
                    else
                        bmodel.productHelper
                                .downloadProductFilter(MENU_SURVEY_BA_CS);
                }

                if (surveyHelperNew.getSurvey() != null
                        && surveyHelperNew.getSurvey().size() > 0) {
                    bmodel.mSelectedActivityName = menuItem.getMenuName();
                    bmodel.mSelectedActivityConfigCode = menuItem
                            .getConfigCode();
                    switchFragment(MENU_SURVEY_BA_CS, menuItem.getMenuName());
                } else {

                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.data_not_mapped), 0);
                }

            }
        } else if (menuItem.getConfigCode().equals(MENU_EMPTY_RECONCILIATION)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                bmodel.mEmptyReconciliationhelper.downloadProducts();
                bmodel.mEmptyReconciliationhelper.downloadNonGenericProductID();
                bmodel.mEmptyReconciliationhelper
                        .downloadReturnProductsTypeNew();
                if (bmodel.mEmptyReconciliationhelper.getSkuTypeBO() != null
                        && bmodel.mEmptyReconciliationhelper.getSkuTypeBO()
                        .size() > 0) {
                    switchFragment(MENU_EMPTY_RECONCILIATION, menuItem.getMenuName());
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_DIGITIAL_SELLER)) {
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(getActivity());
                mDigitalContentHelper.downloadDigitalContent("SELLER");
                if (mDigitalContentHelper.getDigitalMaster() != null
                        && mDigitalContentHelper.getDigitalMaster()
                        .size() > 0) {
                    bmodel.mSelectedActivityName = menuItem.getMenuName();
                    switchFragment(MENU_DIGITIAL_SELLER, menuItem.getMenuName());
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_ROAD_ACTIVITY)) {
            roadTitle = (menuItem.getMenuName() == null) ? "" : menuItem.getMenuName();
            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else
                new LoadRoadActivityData().execute();
        } else if (menuItem.getConfigCode().equals(MENU_ORDER_FULLFILLMENT)) {
            Intent intent = new Intent(getActivity(),
                    OrderFullfillmentRetailerSelection.class);
            intent.putExtra("ScreenCode", "Order Fullfillment");

            //   bmodel.mSelectedActivityName = menuItem.getMenuName();
            startActivity(intent);
            getActivity().finish();
        } else if (menuItem.getConfigCode().equals(MENU_MVP)) {

            if (!isClicked) {
                isClicked = false;
                bmodel.mSelectedActivityName = menuItem.getMenuName();
                switchFragment(MENU_MVP, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_LEAVE_APR)) {

            if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (!isClicked) {
                isClicked = false;
                switchFragment(MENU_LEAVE_APR, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_EXPENSE)) {
            if (!isClicked) {
                isClicked = false;
                Intent i = new Intent(getActivity(), ExpenseActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        } else if (menuItem.getConfigCode().equals(MENU_TASK_NEW)) {
            if (!isClicked) {
                isClicked = false;
                Intent intent = new Intent(getActivity(), Task.class);
                intent.putExtra("screentitle", menuItem.getMenuName());
                intent.putExtra("IsRetailerwisetask", false);
                intent.putExtra("fromHomeScreen", true);
                startActivity(intent);
                getActivity().finish();
            }
        } else if (menuItem.getConfigCode().equals(MENU_PRIMARY_SALES)) {
            if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (!isClicked) {
                isClicked = false;
                bmodel.distributorMasterHelper.downloadDistributorsList();
                bmodel.configurationMasterHelper.downloadStockistMenu();

                //check for distributor list size and stockist menu size
                if (bmodel.distributorMasterHelper.getDistributors() != null &&
                        bmodel.distributorMasterHelper.getDistributors().size() != 0 &&
                        bmodel.configurationMasterHelper.getStockistMenu() != null &&
                        bmodel.configurationMasterHelper.getStockistMenu().size() != 0) {
                    bmodel.configurationMasterHelper.setPrimarysaleTitle(menuItem.getMenuName());

                    switchFragment(MENU_PRIMARY_SALES, menuItem.getMenuName());
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                }

            }

        } else if (menuItem.getConfigCode().equals(MENU_COUNTER)) {

            if (!isClicked) {
                isClicked = false;

                if (bmodel.configurationMasterHelper.SHOW_MENU_COUNTER_ALERT && isMenuAttendCS) {
                    if (!bmodel.mAttendanceHelper.loadAttendanceMaster()) {
                        bmodel.showAlert(getResources().getString(R.string.mark_attendance), 0);
                        return;
                    }
                }
                //bmodel.setRetailerMasterBO(new RetailerMasterBO());
                bmodel.getCounterIdForUser();
                String retailerId = bmodel.getRetailerIdForCounter();
                bmodel.setCounterRetailerId(retailerId);
                bmodel.CS_StockApplyHelper.updateCSSihFromRejectedVariance();
                for (RetailerMasterBO bo : bmodel.getRetailerMaster()) {
                    if (bo.getRetailerID().equals(retailerId)) {
                        bmodel.setRetailerMasterBO(bo);
                        break;

                    }
                }

                if (bmodel.getRetailerMasterBO() != null) {
                    bmodel.setCounterId(bmodel.userMasterHelper.getUserMasterBO().getCounterId());
                    bmodel.mSelectedActivityName = menuItem.getMenuName();
                    switchFragment(MENU_COUNTER, menuItem.getMenuName());

                } else
                    Toast.makeText(getActivity(), R.string.retailer_not_mapped, Toast.LENGTH_LONG).show();
            }


        } else if (menuItem.getConfigCode().equals(MENU_PRESENCE)) {
            switchFragment(MENU_PRESENCE, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_IN_OUT)) {
            bmodel.configurationMasterHelper.setTradecoveragetitle(menuItem.getMenuName());
            switchFragment(MENU_IN_OUT, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_WVW_PLAN)) {

            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewPlanActivity.class);
                startActivity(i);
                getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_WEB_VIEW)) {

            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                startActivity(i);
                getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_NEWRET_EDT)) {
//            Intent i = new Intent(getActivity(), NewOutletEdit.class);
//            i.putExtra("screentitle", menuItem.getMenuName());
//            i.putExtra("flag", 0);
//            bmodel.mSelectedActivityName = menuItem.getMenuName();
//            startActivity(i);
//            getActivity().finish();
            switchFragment(MENU_NEWRET_EDT, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_PLANE_MAP)) {
            switchFragment(MENU_PLANE_MAP, "Trade Coverage");

        } else if (menuItem.getConfigCode().equals(MENU_GROOM_CS)) {
            switchFragment(MENU_GROOM_CS, "Grooming Picture");
        } else if (menuItem.getConfigCode().equals(MENU_JOINT_ACK)) {
            Intent i = new Intent(getActivity(), AcknowledgementActivity.class);
            i.putExtra("screentitle", menuItem.getMenuName());
            startActivity(i);
            getActivity().finish();
        } else if (menuItem.getConfigCode().equals(MENU_NON_FIELD)) {
            bmodel.reasonHelper.downloadPlaneDeviateReasonMaster("FIELD_PLAN_TYPE");
            switchFragment(MENU_NON_FIELD, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_DELMGMT_RET)) {
            switchFragment(MENU_DELMGMT_RET, menuItem.getMenuName());
        }

          /*else if (menuItem.getConfigCode().equals(MENU_COLLECTION_PRINT)) {
            String printFile = readPrintFile();
            if (!"".equals(printFile)) {
                bmodel.mCommonPrintHelper.setInvoiceData(new StringBuilder(printFile));
                bmodel.mSelectedActivityName = menuItem.getMenuName();
                Intent i = new Intent(getActivity(), CommonPrintPreviewActivity.class);
                i.putExtra("isHomeBtnEnable", true);
                startActivity(i);
            } else {
                Toast.makeText(getActivity(), "No Data", Toast.LENGTH_LONG).show();
            }
        }*/


    }

    private void switchFragment(String fragmentName, String menuName) {
        android.support.v4.app.FragmentManager fm = getFragmentManager();

        NewOutletFragment mNewOutletFragment = (NewOutletFragment) fm
                .findFragmentByTag(MENU_NEW_RETAILER);

        VisitFragment mVisitFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_VISIT);

        VisitFragment mPlanningFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_PLANNING);


        SynchronizationFragment mSyncFragment = (SynchronizationFragment) fm
                .findFragmentByTag(MENU_SYNC);

        DeliveryManagementRetailersFragment deliveryRetailersFragment = (DeliveryManagementRetailersFragment) fm
                .findFragmentByTag(MENU_DELMGMT_RET);

        SellerDashboardFragment mSellerDashFragment = (SellerDashboardFragment) fm
                .findFragmentByTag(MENU_DASH_KPI);

        DashboardFragment mDashFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH);

        DashboardFragment mDashDayFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH_DAY);

        JoinCallFragment mJointCallFragment = (JoinCallFragment) fm
                .findFragmentByTag(MENU_JOINT_CALL);

        SurveyActivityNewFragment mJointCallSurveyFragment = (SurveyActivityNewFragment) fm
                .findFragmentByTag(MENU_SURVEY_SW);

        SurveyActivityNewFragment mJointCallSurvey1Fragment = (SurveyActivityNewFragment) fm
                .findFragmentByTag(MENU_SURVEY01_SW);

        SurveyActivityNewFragment mJointCallSurvey2Fragment = (SurveyActivityNewFragment) fm
                .findFragmentByTag(MENU_SURVEY_BA_CS);

        EmptyReconciliationFragment mEmptyReconFragment = (EmptyReconciliationFragment) fm
                .findFragmentByTag(MENU_EMPTY_RECONCILIATION);

        DigitalContentFragment mDigitalContentFragment = (DigitalContentFragment) fm
                .findFragmentByTag(MENU_DIGITIAL_SELLER);
        RoadFragment mRoadFragment = (RoadFragment) fm
                .findFragmentByTag(MENU_ROAD_ACTIVITY);
        MVPFragment mMvpFragment = (MVPFragment) fm
                .findFragmentByTag(MENU_MVP);

        PrimarySaleFragment mPrimSaleFragment = (PrimarySaleFragment) fm
                .findFragmentByTag(MENU_PRIMARY_SALES);

        LeaveApprovalFragment mLeaveApprFragment = (LeaveApprovalFragment) fm
                .findFragmentByTag(MENU_LEAVE_APR);
        AttendanceFragment mAttendFragment = (AttendanceFragment) fm
                .findFragmentByTag(MENU_PRESENCE);
        NonFieldHomeFragment mNonFieldFragment = (NonFieldHomeFragment) fm
                .findFragmentByTag(MENU_ATTENDANCE);
        TimeTrackingFragment mNonFieldTwoFragment = (TimeTrackingFragment) fm
                .findFragmentByTag(MENU_IN_OUT);


        PlanningMapFragment mPlanningMapFragment = (PlanningMapFragment) fm
                .findFragmentByTag(MENU_PLANE_MAP);
        LoadManagementFragment mLoadMgtFragment = (LoadManagementFragment) fm
                .findFragmentByTag(MENU_LOAD_MANAGEMENT);
        SkuWiseTargetFragment mSKUTgtFragment = (SkuWiseTargetFragment) fm
                .findFragmentByTag(MENU_SKUWISESTGT);
        PlanningSubScreenFragment mPlanningSubScreenFragment = (PlanningSubScreenFragment) fm
                .findFragmentByTag(MENU_PLANNING_SUB);
        ReportMenufragment mReportMenuFragment = (ReportMenufragment) fm
                .findFragmentByTag(MENU_REPORT);
        StockProposalFragment stockProposalFragment = (StockProposalFragment) fm
                .findFragmentByTag(MENU_LOAD_REQUEST);
        NewOutletEditFragment newOutletEditFragment = (NewOutletEditFragment) fm
                .findFragmentByTag(MENU_NEWRET_EDT);

        CSHomeScreenFragment csProfileActivity = (CSHomeScreenFragment) fm
                .findFragmentByTag(MENU_COUNTER);
        GroomingFragment groomingFragment = (GroomingFragment) fm
                .findFragmentByTag(MENU_GROOM_CS);
        AcknowledgementFragment acknowledgementFragment = (AcknowledgementFragment) fm
                .findFragmentByTag(MENU_JOINT_ACK);
        PlanDeviationFragment planDeviationFragment = (PlanDeviationFragment) fm
                .findFragmentByTag(MENU_NON_FIELD);

        if (mNewOutletFragment != null && (fragmentName.equals(MENU_NEW_RETAILER))
                && mNewOutletFragment.isVisible()) {
            return;
        } else if (mPlanningFragment != null && (fragmentName.equals(MENU_PLANNING))
                && mPlanningFragment.isVisible()) {
            return;
        } else if (mVisitFragment != null && (fragmentName.equals(MENU_VISIT))
                && mVisitFragment.isVisible()) {
            return;
        } else if (mSyncFragment != null && (fragmentName.equals(MENU_SYNC))
                && mSyncFragment.isVisible()) {
            return;
        } else if (deliveryRetailersFragment != null && (fragmentName.equals(MENU_DELMGMT_RET))
                && deliveryRetailersFragment.isVisible()) {
            return;
        } else if (mSellerDashFragment != null && (fragmentName.equals(MENU_DASH_KPI))
                && mSellerDashFragment.isVisible()) {
            return;
        } else if (mDashFragment != null && (fragmentName.equals(MENU_DASH))
                && mDashFragment.isVisible()) {
            return;
        } else if (mDashDayFragment != null && (fragmentName.equals(MENU_DASH_DAY))
                && mDashDayFragment.isVisible()) {
            return;
        } else if (mJointCallFragment != null && (fragmentName.equals(MENU_JOINT_CALL))
                && mJointCallFragment.isVisible()) {
            return;
        } else if (mJointCallSurveyFragment != null && (fragmentName.equals(MENU_SURVEY_SW))
                && mJointCallSurveyFragment.isVisible()) {
            return;
        } else if (mJointCallSurvey1Fragment != null && (fragmentName.equals(MENU_SURVEY01_SW))
                && mJointCallSurvey1Fragment.isVisible()) {
            return;
        } else if (mJointCallSurvey2Fragment != null && (fragmentName.equals(MENU_SURVEY_BA_CS))
                && mJointCallSurvey2Fragment.isVisible()) {
            return;
        } else if (mEmptyReconFragment != null && (fragmentName.equals(MENU_EMPTY_RECONCILIATION))
                && mEmptyReconFragment.isVisible()) {
            return;
        } else if (mDigitalContentFragment != null && (fragmentName.equals(MENU_DIGITIAL_SELLER))
                && mDigitalContentFragment.isVisible()) {
            return;
        } else if (mRoadFragment != null && (fragmentName.equals(MENU_ROAD_ACTIVITY))
                && mRoadFragment.isVisible()) {
            return;
        } else if (mMvpFragment != null && (fragmentName.equals(MENU_MVP))
                && mMvpFragment.isVisible()) {
            return;
        } else if (mPrimSaleFragment != null && (fragmentName.equals(MENU_PRIMARY_SALES))
                && mPrimSaleFragment.isVisible()) {
            return;
        } else if (mLeaveApprFragment != null && (fragmentName.equals(MENU_LEAVE_APR))
                && mLeaveApprFragment.isVisible()) {
            return;
        } else if (mAttendFragment != null && (fragmentName.equals(MENU_PRESENCE))
                && mAttendFragment.isVisible()) {
            return;
        } else if (mNonFieldFragment != null && (fragmentName.equals(MENU_ATTENDANCE))
                && mNonFieldFragment.isVisible()) {
            return;
        } else if (mNonFieldTwoFragment != null && (fragmentName.equals(MENU_IN_OUT))
                && mNonFieldTwoFragment.isVisible()) {
            return;
        } else if (mPlanningMapFragment != null && (fragmentName.equals(MENU_PLANE_MAP))
                && mPlanningMapFragment.isVisible()) {
            return;
        } else if (mLoadMgtFragment != null && (fragmentName.equals(MENU_LOAD_MANAGEMENT))
                && mLoadMgtFragment.isVisible()) {
            return;
        } else if (mSKUTgtFragment != null && (fragmentName.equals(MENU_SKUWISESTGT))
                && mSKUTgtFragment.isVisible()) {
            return;
        } else if (mPlanningSubScreenFragment != null && (fragmentName.equals(MENU_PLANNING_SUB))
                && mPlanningSubScreenFragment.isVisible()) {
            return;
        } else if (mReportMenuFragment != null && (fragmentName.equals(MENU_REPORT))
                && mReportMenuFragment.isVisible()) {
            return;
        } else if (stockProposalFragment != null && (fragmentName.equals(MENU_LOAD_REQUEST))
                && stockProposalFragment.isVisible()) {
            return;
        } else if
                (newOutletEditFragment != null && (fragmentName.equals(MENU_NEWRET_EDT))
                        && newOutletEditFragment.isVisible()) {
            return;
        } else if (csProfileActivity != null && (fragmentName.equals(MENU_COUNTER)
                && csProfileActivity.isVisible())) {
            return;
        } else if (groomingFragment != null && (fragmentName.equals(MENU_GROOM_CS)
                && groomingFragment.isVisible())) {
            return;
        } else if (acknowledgementFragment != null && fragmentName.equals(MENU_JOINT_ACK)
                && acknowledgementFragment.isVisible()) {
            return;
        } else if (planDeviationFragment != null && fragmentName.equals(MENU_NON_FIELD)
                && planDeviationFragment.isVisible()) {
            return;
        }

        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        if (mNewOutletFragment != null)
            ft.remove(mNewOutletFragment);
        if (mVisitFragment != null)
            ft.remove(mVisitFragment);
        if (mPlanningFragment != null)
            ft.remove(mPlanningFragment);
        if (mSyncFragment != null)
            ft.remove(mSyncFragment);
        if (deliveryRetailersFragment != null)
            ft.remove(deliveryRetailersFragment);
        if (mSellerDashFragment != null)
            ft.remove(mSellerDashFragment);
        if (mDashFragment != null)
            ft.remove(mDashFragment);
        if (mDashDayFragment != null)
            ft.remove(mDashDayFragment);
        if (mJointCallFragment != null)
            ft.remove(mJointCallFragment);
        if (mJointCallSurveyFragment != null)
            ft.remove(mJointCallSurveyFragment);
        if (mJointCallSurvey1Fragment != null)
            ft.remove(mJointCallSurvey1Fragment);
        if (mJointCallSurvey2Fragment != null)
            ft.remove(mJointCallSurvey2Fragment);
        if (mEmptyReconFragment != null)
            ft.remove(mEmptyReconFragment);
        if (mDigitalContentFragment != null)
            ft.remove(mDigitalContentFragment);
        if (mRoadFragment != null)
            ft.remove(mRoadFragment);
        if (mMvpFragment != null)
            ft.remove(mMvpFragment);
        if (mPrimSaleFragment != null)
            ft.remove(mPrimSaleFragment);
        if (mLeaveApprFragment != null)
            ft.remove(mLeaveApprFragment);
        if (mAttendFragment != null)
            ft.remove(mAttendFragment);
        if (mNonFieldFragment != null)
            ft.remove(mNonFieldFragment);
        if (mNonFieldTwoFragment != null)
            ft.remove(mNonFieldTwoFragment);
        if (mPlanningMapFragment != null)
            ft.remove(mPlanningMapFragment);
        if (mLoadMgtFragment != null)
            ft.remove(mLoadMgtFragment);
        if (mSKUTgtFragment != null)
            ft.remove(mSKUTgtFragment);
        if (mPlanningSubScreenFragment != null)
            ft.remove(mPlanningSubScreenFragment);
        if (mReportMenuFragment != null)
            ft.remove(mReportMenuFragment);
        if (stockProposalFragment != null)
            ft.remove(stockProposalFragment);
        if (newOutletEditFragment != null)
            ft.remove(newOutletEditFragment);
        if (csProfileActivity != null)
            ft.remove(csProfileActivity);
        if (groomingFragment != null)
            ft.remove(groomingFragment);
        if (acknowledgementFragment != null)
            ft.remove(acknowledgementFragment);
        if (planDeviationFragment != null)
            ft.remove(planDeviationFragment);

        Bundle bndl;
        Fragment fragment;
        switch (fragmentName) {
            case MENU_NEW_RETAILER:

                if (!bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    bmodel.newOutletHelper.downloadLinkRetailer();
                }

                if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER || bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels(MENU_NEW_RETAILER);
                    bmodel.productHelper
                            .downloadProductsNewOutlet(MENU_NEW_RETAILER);
                }

                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new NewOutletFragment();
                fragment.setArguments(bndl);
                fromHomeScreen = true;
                ft.add(R.id.fragment_content, fragment,
                        MENU_NEW_RETAILER);
                break;
            case MENU_VISIT:
                bndl = new Bundle();
                bndl.putString("From", MENU_VISIT_CONSTANT);
                isVisit = true;
                bndl.putString("Newplanningsub", "");
                fragment = new VisitFragment();
                fragment.setArguments(bndl);
                ((VisitFragment) fragment).setMapViewListener(this);
                ft.add(R.id.fragment_content, fragment,
                        MENU_VISIT);
                break;
            case MENU_PLANNING:
                bndl = new Bundle();
                bndl.putString("From", MENU_PLANNING_CONSTANT);
                isVisit = false;
                bndl.putString("Newplanningsub", "");
                fragment = new VisitFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_PLANNING);
                break;
            case MENU_SYNC:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                if (bmodel.labelsMasterHelper.getSyncContentHTML().equals("")) {
                    bmodel.labelsMasterHelper.downloadSyncContent();
                }
                fragment = new SynchronizationFragment();
                handler = ((SynchronizationFragment) fragment).getHandler();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SYNC);
                break;
            case MENU_DASH_KPI:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("retid", "0");
                bndl.putString("type", "MONTH");
                fragment = new SellerDashboardFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DASH_KPI);
                break;
            case MENU_DASH:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("retid", "0");
                fragment = new DashboardFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DASH);
                break;
            case MENU_DASH_DAY:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("retid", "0");
                bndl.putString("type", "DAY");
                fragment = new DashboardFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DASH_DAY);
                break;
            case MENU_JOINT_CALL:
                bmodel.configurationMasterHelper.setJointCallTitle(menuName);
                bndl = new Bundle();
                bndl.putString("from", fragmentName);
                fragment = new JoinCallFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_JOINT_CALL);
                break;
            case MENU_SURVEY_SW:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putInt("SurveyType", 1);
                bndl.putString("menucode", fragmentName);
                fragment = new SurveyActivityNewFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SURVEY_SW);
                break;
            case MENU_SURVEY01_SW:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putInt("SurveyType", 0);
                bndl.putString("menucode", fragmentName);
                fragment = new SurveyActivityNewFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SURVEY01_SW);
                break;
            case MENU_SURVEY_BA_CS:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putInt("SurveyType", 0);
                bndl.putString("menucode", fragmentName);
                fragment = new SurveyActivityNewFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SURVEY_BA_CS);
                break;
            case MENU_EMPTY_RECONCILIATION:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new EmptyReconciliationFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_EMPTY_RECONCILIATION);
                break;
            case MENU_DIGITIAL_SELLER:
                bndl = new Bundle();
                bndl.putString("ScreenCode", fragmentName);
                bndl.putString("FromInit", fragmentName);
                bndl.putString("screentitle", menuName);
                fragment = new DigitalContentFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DIGITIAL_SELLER);
                break;
            case MENU_ROAD_ACTIVITY:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new RoadFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_ROAD_ACTIVITY);
                break;
            case MENU_LOAD_MANAGEMENT:
                bmodel.configurationMasterHelper
                        .setLoadmanagementtitle(menuName);
                fragment = new LoadManagementFragment();
                ft.add(R.id.fragment_content, fragment,
                        MENU_LOAD_MANAGEMENT);
                break;
            case MENU_PLANNING_SUB:
                bmodel.configurationMasterHelper
                        .setLoadplanningsubttitle(menuName);
                fragment = new PlanningSubScreenFragment();
                ft.add(R.id.fragment_content, fragment,
                        MENU_PLANNING_SUB);
                break;

            case MENU_PRESENCE:
                ft.add(R.id.fragment_content, new AttendanceFragment(),
                        MENU_PRESENCE);
                break;
            case MENU_IN_OUT:
                bndl = new Bundle();
                bndl.putString("screentitle", fragmentName);
                fragment = new TimeTrackingFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_IN_OUT);
                break;
            case MENU_PRIMARY_SALES:
                ft.add(R.id.fragment_content, new PrimarySaleFragment(),
                        MENU_PRIMARY_SALES);
                break;
            case MENU_LEAVE_APR:
                ft.add(R.id.fragment_content, new LeaveApprovalFragment(),
                        MENU_LEAVE_APR);
                break;
            case MENU_MVP:
                ft.add(R.id.fragment_content, new MVPFragment(),
                        MENU_MVP);
                break;
            case MENU_ATTENDANCE:
                ft.add(R.id.fragment_content, new NonFieldHomeFragment(),
                        MENU_ATTENDANCE);
                break;
            case MENU_PLANE_MAP:
                displayTodayRoute(null);
                if (isVisit) {
                    bndl = new Bundle();
                    bndl.putString("From", MENU_VISIT_CONSTANT);
                } else {
                    bndl = new Bundle();
                    bndl.putString("From", MENU_PLANNING_CONSTANT);
                }
                fragment = new PlanningMapFragment();
                fragment.setArguments(bndl);
                ((PlanningMapFragment) fragment).setDataPull(this);
                ft.add(R.id.fragment_content, fragment,
                        MENU_PLANE_MAP);
                break;
            case MENU_SKUWISESTGT:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("from", "1");
                bndl.putString("rid", "0");
                bndl.putString("type", "MONTH");
                bndl.putString("code", "SV");
                fragment = new SkuWiseTargetFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SKUWISESTGT);
                break;

            case MENU_REPORT:
                bmodel.productHelper.downloadProductFilter("MENU_STK_ORD");
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new ReportMenufragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_REPORT);
                break;

            case MENU_LOAD_REQUEST:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new StockProposalFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_LOAD_REQUEST);
                break;
            case MENU_NEWRET_EDT:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("flag", "0");
                fragment = new NewOutletEditFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_NEWRET_EDT);
                break;

            case MENU_COUNTER:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new CSHomeScreenFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_COUNTER);
                break;
            case MENU_GROOM_CS:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new GroomingFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_GROOM_CS);
                break;

            case MENU_NON_FIELD:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new PlanDeviationFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_NON_FIELD);
                break;
            case MENU_DELMGMT_RET:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new DeliveryManagementRetailersFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DELMGMT_RET);
                break;
        }
        ft.commit();

    }

    public void onTabRemoved() {

        android.support.v4.app.FragmentManager fm = getFragmentManager();

        NewOutletFragment mNewOutletFragment = (NewOutletFragment) fm
                .findFragmentByTag(MENU_NEW_RETAILER);

        VisitFragment mVisitFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_VISIT);

        VisitFragment mPlanningFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_PLANNING);

        LoadManagementFragment mLoadManagementFragment = (LoadManagementFragment) fm
                .findFragmentByTag(MENU_LOAD_MANAGEMENT);

        PlanningSubScreenFragment mPlanningSubScreenFragment = (PlanningSubScreenFragment) fm
                .findFragmentByTag(MENU_PLANNING_SUB);

        SynchronizationFragment mSyncFragment = (SynchronizationFragment) fm
                .findFragmentByTag(MENU_SYNC);

        SellerDashboardFragment mSellerDashFragment = (SellerDashboardFragment) fm
                .findFragmentByTag(MENU_DASH_KPI);

        DashboardFragment mDashFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH);

        DashboardFragment mDashDayFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH_DAY);

        JoinCallFragment mJointCallFragment = (JoinCallFragment) fm
                .findFragmentByTag(MENU_JOINT_CALL);

        SurveyActivityNewFragment mJointCallSurveyFragment = (SurveyActivityNewFragment) fm
                .findFragmentByTag(MENU_SURVEY_SW);

        SurveyActivityNewFragment mJointCallSurvey1Fragment = (SurveyActivityNewFragment) fm
                .findFragmentByTag(MENU_SURVEY01_SW);

        EmptyReconciliationFragment mEmptyReconFragment = (EmptyReconciliationFragment) fm
                .findFragmentByTag(MENU_EMPTY_RECONCILIATION);

        DigitalContentFragment mDigitalContentFragment = (DigitalContentFragment) fm
                .findFragmentByTag(MENU_DIGITIAL_SELLER);
        RoadFragment mRoadFragment = (RoadFragment) fm
                .findFragmentByTag(MENU_ROAD_ACTIVITY);
        MVPFragment mMvpFragment = (MVPFragment) fm
                .findFragmentByTag(MENU_MVP);

        PrimarySaleFragment mPrimSaleFragment = (PrimarySaleFragment) fm
                .findFragmentByTag(MENU_PRIMARY_SALES);

        LeaveApprovalFragment mLeaveApprFragment = (LeaveApprovalFragment) fm
                .findFragmentByTag(MENU_LEAVE_APR);
        AttendanceFragment mAttendFragment = (AttendanceFragment) fm
                .findFragmentByTag(MENU_PRESENCE);
        NonFieldHomeFragment mNonFieldFragment = (NonFieldHomeFragment) fm
                .findFragmentByTag(MENU_ATTENDANCE);
        TimeTrackingFragment mNonFieldTwoFragment = (TimeTrackingFragment) fm
                .findFragmentByTag(MENU_IN_OUT);
        ReportMenufragment mReportMenuFragment = (ReportMenufragment) fm
                .findFragmentByTag(MENU_REPORT);
        LoadManagementFragment mLoadMgmtfragment = (LoadManagementFragment) fm
                .findFragmentByTag(MENU_LOAD_MANAGEMENT);

        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();


        if (mNewOutletFragment != null) {
            ft.detach(mNewOutletFragment);
        }
        if (mSellerDashFragment != null) {
            ft.detach(mSellerDashFragment);
        }
        if (mReportMenuFragment != null) {
            ft.detach(mReportMenuFragment);
        }
        if (mVisitFragment != null) {
            ft.detach(mVisitFragment);
        }
        if (mSyncFragment != null) {
            ft.detach(mSyncFragment);
        }
        if (mLoadMgmtfragment != null) {
            ft.detach(mLoadMgmtfragment);
        }
        if (mPlanningFragment != null) {
            ft.detach(mPlanningFragment);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbindDrawables(getView().findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * getActivity() would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.menu_homescreen, menu);

        MenuItem badge = menu.findItem(R.id.menu_notification);
        RelativeLayout badgeLayout = (RelativeLayout) MenuItemCompat
                .getActionView(badge);
        imgIconNotification = (ImageView) badgeLayout
                .findViewById(R.id.myButton);
        tv_counter = (TextView) badgeLayout.findViewById(R.id.textOne);

        imgIconNotification.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Task.class);
                intent.putExtra("IsRetailerwisetask", false);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_device_status).setVisible(
                bmodel.configurationMasterHelper.SHOW_DEVICE_STATUS);

        menu.findItem(R.id.menu_notification).setVisible(
                bmodel.configurationMasterHelper.SHOW_USER_TASK);

        menu.findItem(R.id.menu_pswd).setVisible(
                LoginHelper.getInstance(getContext()).SHOW_CHANGE_PASSWORD);

        menu.findItem(R.id.menu_feedback).setVisible(
                bmodel.configurationMasterHelper.SHOW_FEEDBACK);

        menu.findItem(R.id.menu_chat).setVisible(
                bmodel.configurationMasterHelper.IS_CHAT_ENABLED);

        if (intcounter == 0) {
            tv_counter.setVisibility(View.GONE);
        } else if (intcounter < 100) {
            tv_counter.setVisibility(View.VISIBLE);
            tv_counter.setText(intcounter + "");
        } else if (intcounter >= 100) {
            tv_counter.setVisibility(View.VISIBLE);
            tv_counter.setText("99+");
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();

        if (i1 == R.id.menu_device_status) {
            Intent i = new Intent(getActivity(), DeviceStatusActivity.class);
            startActivity(i);
            return true;
        } else if (i1 == R.id.menu_about) {
            startActivity(new Intent(getActivity(), About.class));
            return true;
        } else if (i1 == R.id.menu_back) {
            bmodel.synchronizationHelper.backUpDB();
            showDialog(0);
            return true;
        } else if (i1 == R.id.menu_pswd) {
            Intent in = new Intent(getActivity(), ChangePasswordActivity.class);
            in.putExtra("isExpired", false);
            startActivity(in);
            return true;
        } else if (i1 == R.id.menu_setting) {
            Intent i = new Intent(getActivity(), UserSettingsActivity.class);
            startActivity(i);
            return true;
        } else if (i1 == R.id.menu_feedback) {
            Intent i = new Intent(getActivity(), UserFeedbackActivity.class);
            startActivity(i);
            return true;
        } else if (i1 == R.id.menu_chat) {
            if (bmodel.getChatRegId() != null && bmodel.getChatUserName() != null
                    && bmodel.getChatPassword() != null && !bmodel.getChatRegId().equals("")
                    && !bmodel.getChatUserName().equals("") && !bmodel.getChatPassword().equals("")) {
                ChatApplicationHelper.getInstance(getActivity())
                        .openChatApplication(bmodel.getChatUserName(),
                                bmodel.getChatUserName().trim() + "@ivymobility.com", bmodel.getChatPassword(),
                                bmodel.getChatRegId(), CHAT_AUTHENTICATION_KEY, CHAT_AUTHENTICATION_SECRET_KEY);
            } else {
                Toast.makeText(getActivity(), R.string.not_registered, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void vanUnloadStockAdjustmentSubroutine(String menuName) {
        vanUnloadStockAdjustmentIntent = new Intent(getActivity(),
                VanStockAdjustActivity.class);
        vanUnloadStockAdjustmentIntent
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanUnloadStockAdjustmentIntent.putExtra("screentitle", menuName);

        (new DownloadStockAdjustment()).execute();
    }

    public void checkForOrderSplitRecordAndGotoOrderSplit(String menuName) {
        bmodel.orderSplitHelper = OrderSplitHelper.clearInstance();
        bmodel.orderSplitHelper = OrderSplitHelper.getInstance(bmodel);
        bmodel.orderSplitHelper.setLast_split_master_index(0);

        if (bmodel.orderSplitHelper.isThereAnyOrderToSplit()) {
            bmodel.setOrderSplitScreenTitle(menuName);

            Intent i = new Intent(getActivity(), OrderSplitMasterScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            getActivity().finish();
        } else {
            Toast.makeText(getActivity(), R.string.toast_nodata_available,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void switchMapView() {

        ConfigureBO conBo = new ConfigureBO();
        conBo.setConfigCode(MENU_PLANE_MAP);
        gotoNextActivity(conBo);

    }

    @Override
    public List<MarkerOptions> getData() {
        return markerList;
    }

    @Override
    public void switchVisitView() {
        // Load the HHTMenuTable
        bmodel.configurationMasterHelper.downloadMainMenu();
        for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {

            if (con.getConfigCode().equals(MENU_VISIT)) {
                gotoNextActivity(con);
                break;
            }
        }
    }

    public void refreshList(boolean showDefaultScreen) {
        leftmenuDB = new Vector<>();
        // Load the HHTMenuTable
        bmodel.configurationMasterHelper.downloadMainMenu();
        for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {

            leftmenuDB.add(con);

            if (con.getConfigCode().equals(MENU_PRESENCE)) {
                isMenuAttendCS = true;
            }
        }
        listView.setCacheColorHint(0);
        listView.setAdapter(new LeftMenuBaseAdapter(leftmenuDB));
        if (showDefaultScreen) {
            showDefaultScreen();
        }
    }

    private class LoadRoadActivityData extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            bmodel.mroadActivityHelper.loadTypeSpinnerData();
            bmodel.mroadActivityHelper.loadProductSpinnerData();

            // Location spinners
            bmodel.mroadActivityHelper.loadLocationNames();
            bmodel.mroadActivityHelper.loadLocation1SpinnerData();
            bmodel.mroadActivityHelper.loadLocation2SpinnerData();

            return true;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            switchFragment(MENU_ROAD_ACTIVITY, roadTitle);
        }

    }

    class LeftMenuBaseAdapter extends BaseAdapter {

        Vector<ConfigureBO> items;

        public LeftMenuBaseAdapter(Vector<ConfigureBO> menuDB) {
            items = menuDB;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ConfigureBO configTemp = items.get(position);
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_menu, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = (TextView) convertView
                        .findViewById(R.id.list_item_menu_tv_new);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        gotoNextActivity(holder.config);

                        mHomeScreenItemClickedListener.onListItemSelected();
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.position = position;
            holder.config = configTemp;
            holder.menuBTN.setText(configTemp.getMenuName());
            holder.menuBTN.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.menuIcon.setImageResource(i);
            else
                holder.menuIcon.setImageResource(menuIcons.get(MENU_PLANNING));


            return convertView;
        }

        class ViewHolder {

            ConfigureBO config;
            int position;
            private ImageView menuIcon;
            private TextView menuBTN;
        }
    }

    class DownloadStockProposal extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_STOCK_PROPOSAL");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_STOCK_PROPOSAL");

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get("MENU_STOCK_PROPOSAL"), 2);
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();

        }

    }

    class DownloadStockAdjustment extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                /** Load filter **/
                bmodel.productHelper
                        .downloadProductFilter("MENU_LOAD_MANAGEMENT");

                bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT", "MENU_STOCK_ADJUSTMENT");

            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            startActivity(vanUnloadStockAdjustmentIntent);
            getActivity().finish();

        }

    }

    private boolean checkMenusAvailable() {

        try {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE);

            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("select * from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag = 1 and MenuType= 'HOME_MENU' and lang like"
                    + bmodel.QT("%" + language + "%"));

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    @SuppressLint("ValidFragment")
    public class ChannelSelectionDialogFragment extends DialogFragment {
        private String mTitle = "";
        private String mMenuName = "";

        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mChannelLV;


        public ChannelSelectionDialogFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
            mMenuName = getArguments().getString("screentitle");


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            getDialog().setTitle(mTitle);
            mTitleTV = (TextView) getView().findViewById(R.id.title);
            mTitleTV.setVisibility(View.GONE);
            mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
            mOkBtn.setVisibility(View.GONE);
            mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
            mChannelLV = (ListView) getView().findViewById(R.id.lv_colletion_print);

            ArrayAdapter<ChannelBO> adapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_list_item_single_choice, mChannelList);
            mChannelLV.setAdapter(adapter);
            mChannelLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ChannelBO channelBO = mChannelList.get(position);
                    bmodel.newOutletHelper.setmSelectedChannelid(channelBO.getChannelId());
                    bmodel.newOutletHelper.setmSelectedChannelname(channelBO.getChannelName());
                    bmodel.newOutletHelper.loadNewOutletConfiguration(channelBO.getChannelId());
                    bmodel.newOutletHelper.loadRetailerType();
                    bmodel.newOutletHelper.downloadLinkRetailer();
                    switchFragment(MENU_NEW_RETAILER, mMenuName);
                    getDialog().dismiss();


                }
            });
            mOkBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mDismisBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();


                }
            });


        }


    }

    private class DeleteTables extends
            AsyncTask<Integer, Integer, Integer> {


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            bmodel.synchronizationHelper.deleteTables(false);

            return 0;
        }

        protected void onPostExecute(Integer result) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.menu_not_available),
                    Toast.LENGTH_LONG).show();

        }
    }

    public interface homeScreenItemClickedListener {
        void onListItemSelected();

    }

    void setmHomeScreenItemClickedListener(homeScreenItemClickedListener mListener) {
        mHomeScreenItemClickedListener = mListener;
    }


    private void displayTodayRoute(String filter) {
        LatLng latLng;

        List<RetailerMasterBO> retailer = new ArrayList<>();
        try {
            int siz = bmodel.getRetailerMaster().size();
            retailer.clear();
            // Add today's retailers.

            if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                for (int i = 0; i < siz; i++) {
                    if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            } else {
                for (int i = 0; i < siz; i++) {

                    retailer.add(bmodel.getRetailerMaster().get(i));

                }
            }

            Collections.sort(retailer,
                    RetailerMasterBO.WalkingSequenceComparator);


            // Add today'sdeviated retailers.
            for (int i = 0; i < siz; i++) {
                if (("Y").equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i)
                                .getRetailerName().toLowerCase())
                                .contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        try {

            if (bmodel.configurationMasterHelper.IS_MAP) {
                markerList.clear();
                baiduMarkerList.clear();
                for (int i = 0; i < retailer.size(); i++) {
                    if (bmodel.configurationMasterHelper.IS_BAIDU_MAP) {
                        Bundle bndl = new Bundle();
                        bndl.putCharSequence("addr", retailer.get(i).getAddress1());
                        baidulatLng = new com.baidu.mapapi.model.LatLng(retailer.get(i).getLatitude(), retailer
                                .get(i).getLongitude());
                        com.baidu.mapapi.map.MarkerOptions mBMarker = new com.baidu.mapapi.map.MarkerOptions().position(baidulatLng)
                                .title(retailer.get(i).getRetailerName())
                                .extraInfo(bndl)
                                .icon(com.baidu.mapapi.map.BitmapDescriptorFactory.fromResource(R.drawable.markergreen)
                                ).animateType(com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType.drop);
                        baiduMarkerList.add(mBMarker);
                    } else {
                        latLng = new LatLng(retailer.get(i).getLatitude(), retailer
                                .get(i).getLongitude());
                        MarkerOptions mMarkerOptions = new MarkerOptions()
                                .position(latLng)
                                .title(retailer.get(i).getRetailerName() + "," + retailer.get(i).getRetailerID())
                                .snippet(retailer.get(i).getAddress1())
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        markerList.add(mMarkerOptions);
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /*//For Collection Print
    public String readPrintFile() {
        try {
            String path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/";
            File f = new File(path + "IP_169505262017131306.txt");
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Commons.printException(e);
        }
        return "";
    }*/
    private void setProfileImage() {
        if (bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null
                && !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {
            String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            File imgFile = new File(getActivity().getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS)
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid()
                    + DataMembers.DIGITAL_CONTENT
                    + "/"
                    + DataMembers.USER + "/"
                    + path);
            if (imgFile.exists()) {
                try {
                    Bitmap myBitmap = bmodel.decodeFile(imgFile);
                    profileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    profileImageView.setAdjustViewBounds(true);
                    //  profileImageView.setImageBitmap(getCircularBitmapFrom(myBitmap));

                    Glide.with(getActivity())
                            .load(myBitmap)
                            .centerCrop()
                            .placeholder(R.drawable.face)
                            .error(R.drawable.no_image_available)
                            .transform(bmodel.circleTransform)
                            .into(profileImageView);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else {
                profileImageView
                        .setImageResource(R.drawable.face);
            }
        }
    }

    private void setImageFromCamera() {
        try {
            if (bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null &&
                    !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {
                String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
                String path = imgPaths[imgPaths.length - 1];
                File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                        + DataMembers.photoFolderName + "/" + path);
                Bitmap myBitmap = bmodel.decodeFile(file);
                profileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                profileImageView.setAdjustViewBounds(true);
                // profileImageView.setImageBitmap(getCircularBitmapFrom(myBitmap));

                Glide.with(getActivity()).load(myBitmap)
                        .centerCrop()
                        .placeholder(R.drawable.face)
                        .error(R.drawable.no_image_available)
                        .transform(bmodel.circleTransform)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.face);
            }
        } catch (Exception e) {
            profileImageView.setImageResource(R.drawable.face);
            Commons.printException("" + e);
        }
    }

    private void takePhoto() {
        if (bmodel.isExternalStorageAvailable()) {
            imageFileName = "USER_" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "_"
                    + Commons.now(Commons.DATE_TIME) + "_img.jpg";

            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), HomeScreenFragment.photoPath + "/" + imageFileName);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Open the Image in Photo Gallery while onClick
     */
    private void openImage(String fileName) {
        if (fileName.trim().length() > 0) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + fileName),
                        "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Commons.printException("" + e);
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    }

//    private Bitmap getCircularBitmapFrom(Bitmap source) {
//        if (source == null || source.isRecycled()) {
//            return null;
//        }
//        float radius = source.getWidth() > source.getHeight() ? ((float) source
//                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
//        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
//                source.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
//                Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
//                radius, paint);
//
//        return bitmap;
//    }
}