package com.ivy.cpg.view.homescreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;
import com.ivy.cpg.locationservice.movementtracking.MovementTracking;
import com.ivy.cpg.primarysale.view.PrimarySaleFragment;
import com.ivy.cpg.view.acknowledgement.AcknowledgementActivity;
import com.ivy.cpg.view.acknowledgement.AcknowledgementFragment;
import com.ivy.cpg.view.attendance.AttendanceFragment;
import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.cpg.view.attendance.TLAttendanceActivity;
import com.ivy.cpg.view.backupseller.BackUpSellerFragment;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.IncentiveDashboardFragment;
import com.ivy.cpg.view.dashboard.olddashboard.DashboardFragment;
import com.ivy.cpg.view.dashboard.olddashboard.SkuWiseTargetFragment;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashboardFragment;
import com.ivy.cpg.view.delivery.invoice.DeliveryManagementRetailersFragment;
import com.ivy.cpg.view.denomination.DenominationFragment;
import com.ivy.cpg.view.digitalcontent.DigitalContentFragment;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationFragment;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper;
import com.ivy.cpg.view.expense.ExpenseFragment;
import com.ivy.cpg.view.homescreen.deviceStatus.DeviceStatusActivity;
import com.ivy.cpg.view.homescreen.userFeedback.UserFeedbackActivity;
import com.ivy.cpg.view.jointcall.JoinCallFragment;
import com.ivy.cpg.view.leaveapproval.LeaveApprovalFragment;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.login.TermsAndConditionsActivity;
import com.ivy.cpg.view.mvp.MVPFragment;
import com.ivy.cpg.view.nonfield.NonFieldHelper;
import com.ivy.cpg.view.nonfield.NonFieldHomeFragment;
import com.ivy.cpg.view.orderfullfillment.OrderFullfillmentRetailerSelection;
import com.ivy.cpg.view.quickcall.QuickCallFragment;
import com.ivy.cpg.view.reports.ReportMenuFragment;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.cpg.view.roadactivity.RoadActivityHelper;
import com.ivy.cpg.view.roadactivity.RoadFragment;
import com.ivy.cpg.view.settings.About;
import com.ivy.cpg.view.settings.UserSettingsActivity;
import com.ivy.cpg.view.subd.SubDFragment;
import com.ivy.cpg.view.supervisor.chat.StartChatActivity;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.sellerhomescreen.SellersMapHomeFragment;
import com.ivy.cpg.view.survey.SurveyActivityNewFragment;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.cpg.view.tradeCoverage.VisitFragment;
import com.ivy.cpg.view.van.LoadManagementFragment;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.cpg.view.van.stockproposal.StockProposalFragment;
import com.ivy.cpg.view.webview.WebViewActivity;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ChatApplicationHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.ChannelSelectionDialog;
import com.ivy.sd.png.view.NewOutletEditFragment;
import com.ivy.sd.png.view.NewoutletContainerFragment;
import com.ivy.sd.png.view.PlanDeviationFragment;
import com.ivy.sd.png.view.SynchronizationFragment;
import com.ivy.ui.attendance.inout.view.TimeTrackingFragment;
import com.ivy.ui.retailerplan.calendar.view.CalendarPlanFragment;
import com.ivy.ui.retailer.viewretailers.view.map.RetailerMapFragment;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.view.NotesListFragment;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskViewListener;
import com.ivy.ui.task.view.TaskFragment;
import com.ivy.ui.task.view.TaskUnplannedFragment;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_ATTENDANCE;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_BACKUP_SELLER;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_COUNTER;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DASH;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DASH_DAY;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DASH_INC;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DASH_KPI;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DELMGMT_RET;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DENOMINATION;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_DIGITIAL_SELLER;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_EMPTY_RECONCILIATION;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_EXPENSE;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_IN_OUT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_JOINT_ACK;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_JOINT_CALL;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_LEAVE_APR;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_LOAD_MANAGEMENT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_LOAD_REQUEST;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_MAP_PLAN;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_MVP;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_NEWRET_EDT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_NEW_RETAILER;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_NON_FIELD;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_NOTES_SW;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_OFLNE_PLAN;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_ORDER_FULLFILLMENT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PLANE_MAP;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PLANNING;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PLANNING_CONSTANT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PLANNING_SUB;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PRESENCE;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_PRIMARY_SALES;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_Q_CALL;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_REALLOCATION;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_REPORT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_ROAD_ACTIVITY;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_ROUTE_KPI;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_RTR_TASK_PENDING;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SKUWISESTGT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SUBD;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SUPERVISOR_CALLANALYSIS;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SUPERVISOR_MOVEMENT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SUPERVISOR_REALTIME;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SURVEY01_SW;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SURVEY_BA_CS;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SURVEY_SW;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_SYNC;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_TASK_NEW;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_VISIT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_VISIT_CONSTANT;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_WEB_VIEW;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_WEB_VIEW_APPR;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_WEB_VIEW_PLAN;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_WVW_PLAN;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.menuIcons;

public class HomeScreenFragment extends IvyBaseFragment implements VisitFragment.MapViewListener
        , PlanningMapFragment.DataPulling, ChannelSelectionDialog.ChannelSelectionListener, TaskViewListener {

    private BusinessModel bmodel;

    public static boolean fromHomeScreen = false;
    public static boolean isLeave_today;
    private int taskDueDateCount = 0;

    private boolean isClicked;
    private boolean isInandOutModuleEnabled = false;
    private boolean isVisit;

    private Vector<ConfigureBO> leftmenuDB = new Vector<>();

    //Chat
    private String CHAT_AUTHENTICATION_KEY = "mj74gxbHLMvVfHK";
    private String CHAT_AUTHENTICATION_SECRET_KEY = "rQkkQgYJss9UCOA";

    private HomeScreenItemClickedListener mHomeScreenItemClickedListener;
    private Handler handler;

    // Profile related variables
    private TextView userNameTv, designation;

    private ImageView profileImageView;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int TERMS_COND_REQ_CODE = 2;
    private String imageFileName;

    private ListView listView;
    private ArrayList<ChannelBO> mChannelList;
    private ChannelSelectionDialog dialogFragment;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_homescreen, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        HomeScreenHelper homeScreenHelper = new HomeScreenHelper(getContext());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.title_homescreen));
        }

        //handle memory
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        // If no menu is downloaded then delete all tables
        if (!homeScreenHelper.checkMenusAvailable()) {
            new DeleteTables().execute();
        }

        // Load the HHTMenuTable
        loadHomeMenuConfiguration();

        // Load lables master if navigating from settings screen
        if (getActivity().getIntent().getBooleanExtra("fromSettingScreen", false))
            bmodel.labelsMasterHelper.downloadLabelsMaster();


        if (AttendanceHelper.getInstance(getActivity()).checkLeaveAttendance(getActivity()))
            isLeave_today = true;

        userNameTv = view.findViewById(R.id.tv_username);
        designation = view.findViewById(R.id.tv_designation);
        profileImageView = view.findViewById(R.id.im_user);

        listView = view.findViewById(R.id.listView1);

        profileImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                captureUserProfilePicture();
                return false;
            }
        });

        profileImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                File profileImage = null;

                if (bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null
                        && !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {

                    String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
                    String path = imgPaths[imgPaths.length - 1];

                    if (bmodel.userMasterHelper.hasProfileImageSetLocally(bmodel.userMasterHelper.getUserMasterBO())) {

                        profileImage = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                + DataMembers.photoFolderName + "/" + path);
                    } else {

                        profileImage = new File(getActivity().getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.USER + "/"
                                + path);
                    }

                }

                if (profileImage != null && profileImage.exists()) {
                    try {
                        openImage(profileImage.getAbsolutePath());
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

        LinearLayout ll_logout = view.findViewById(R.id.ll_logout);
        ll_logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                bmodel.synchronizationHelper.backUpDB();
                showDialog(0);
            }
        });

        LinearLayout ll_about = view.findViewById(R.id.ll_about);
        ll_about.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), About.class));
            }
        });

        ImageView settingView = view.findViewById(R.id.iv_setting);
        settingView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),
                        UserSettingsActivity.class).putExtra("fromHomeScreen", true);
                startActivity(i);
            }
        });

        ImageButton chatBtn = view.findViewById(R.id.img_chat);
        ImageButton firebaseChat = view.findViewById(R.id.img_chat_firebase);
        ImageButton divStatusBtn = view.findViewById(R.id.img_div_status);
        ImageButton feedBackBtn = view.findViewById(R.id.img_user_feedback);

        if (bmodel.configurationMasterHelper.IS_CHAT_ENABLED)
            chatBtn.setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_DEVICE_STATUS)
            divStatusBtn.setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_FEEDBACK)
            feedBackBtn.setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.IS_FIREBASE_CHAT_ENABLED)
            firebaseChat.setVisibility(View.VISIBLE);


        chatBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
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

            }
        });

        firebaseChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StartChatActivity.class);
                startActivity(intent);
            }
        });


        divStatusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DeviceStatusActivity.class);
                startActivity(i);
            }
        });


        feedBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), UserFeedbackActivity.class);
                startActivity(i);
            }
        });

        // Initilize photo filder path and create directory if not exisit.
        if (!FileUtils.createFilePathAndFolder(getContext())) {
            Toast.makeText(getContext(), "Photo storage folder not created..", Toast.LENGTH_LONG).show();
        }

        /** Initialising map view **/
        try {
            if (bmodel.configurationMasterHelper.IS_MAP) {
                MapsInitializer.initialize(getActivity());
                if (bmodel.configurationMasterHelper.IS_BAIDU_MAP)
                    SDKInitializer.initialize(getActivity().getApplication());
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        refreshList(true);

        if (bmodel.configurationMasterHelper.ISUPLOADUSERLOC) {

            MovementTracking.startTrackingLocation(getContext(),
                    bmodel.configurationMasterHelper.startTime,
                    bmodel.configurationMasterHelper.endTime,
                    bmodel.configurationMasterHelper.alarmTime);

        }

        return view;

    }

    public void loadHomeMenuConfiguration() {
        leftmenuDB = new Vector<>();
        bmodel.configurationMasterHelper.downloadMainMenu();
        for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {

            leftmenuDB.add(con);

            if (con.getConfigCode().equalsIgnoreCase(MENU_DASH)) {
                con.setConfigCode(MENU_DASH_KPI);
                con.setMenuName("Seller Kpi");
                leftmenuDB.add(con);
            }

            if (con.getConfigCode().equals(MENU_IN_OUT)) {
                isInandOutModuleEnabled = true;
            }
        }
    }

    public void refreshList(boolean showDefaultScreen) {

        listView.setCacheColorHint(0);
        listView.setAdapter(new LeftMenuBaseAdapter(leftmenuDB));

        if (showDefaultScreen) {
            showDefaultScreen();
        }

        userNameTv.setText(bmodel.userMasterHelper.getUserMasterBO().getUserName());
        userNameTv.setTypeface(FontUtils.getFontBalooHai(getContext(), FontUtils.FontType.REGULAR));
        designation.setText(bmodel.userMasterHelper.getUserMasterBO().getUserType());
        designation.setTypeface(FontUtils.getFontRoboto(getContext(), FontUtils.FontType.MEDIUM));

        if (bmodel.userMasterHelper.hasProfileImageSetLocally(bmodel.userMasterHelper.getUserMasterBO()))
            setImageFromInternalStorage();
        else
            setProfileImage();

        LoginHelper loginHelper = LoginHelper.getInstance(getActivity());
        loginHelper.downloadTermsAndConditions(getActivity());
        if (bmodel.configurationMasterHelper.IS_SHOW_TERMS_COND && !loginHelper.isTermsAccepted() && !showDefaultScreen) {
            Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
            intent.putExtra("fromScreen", "homescreen");
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.zoom_enter, R.anim.hold);
            ActivityCompat.startActivityForResult(getActivity(), intent, TERMS_COND_REQ_CODE, opts.toBundle());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();
        // showDefaultScreen();

    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onResume() {
        super.onResume();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        bmodel.configurationMasterHelper.getPrinterConfig();

        LoginHelper loginHelper = LoginHelper.getInstance(getActivity());
        taskDueDateCount = loginHelper.getNearByDueDataTaskAvail(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        getActivity().supportInvalidateOptionsMenu();
    }


    private void showDefaultScreen() {

        if (leftmenuDB.size() > 0) {
            if (getActivity().getIntent().getStringExtra("menuCode") != null) {
                if (MENU_PLANE_MAP.equals(getActivity().getIntent().getStringExtra("menuCode"))) {
                    ConfigureBO conBo = new ConfigureBO();
                    conBo.setConfigCode(MENU_PLANE_MAP);
                    gotoNextActivity(conBo);
                } else {
                    for (ConfigureBO configureBO : leftmenuDB) {
                        if (configureBO.getConfigCode().equalsIgnoreCase(getActivity().getIntent().getStringExtra("menuCode"))) {
                            gotoNextActivity(configureBO);
                            break;
                        }
                    }
                }
            } else {
                // showing first menu by default
                //gotoNextActivity(leftmenuDB.get(0));
                for (ConfigureBO configureBO : leftmenuDB) {
                    if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled && isLeave_today) {
                        if (configureBO.getConfigCode().equalsIgnoreCase(MENU_IN_OUT)) {
                            gotoNextActivity(configureBO);
                            break;
                        }
                        if (configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_KPI)) {
                            gotoNextActivity(configureBO);
                            break;
                        }
                    } else if (configureBO.getConfigCode().equalsIgnoreCase(MENU_VISIT)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_KPI)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_DAY)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DASH_INC)
                            || configureBO.getConfigCode().equalsIgnoreCase(MENU_DIGITIAL_SELLER)) {
                        gotoNextActivity(configureBO);
                        break;
                    }
                }
            }
        }
    }

    private void moveToSpecificMenu(String menuCode) {
        for (ConfigureBO configureBO : leftmenuDB) {
            if (configureBO.getConfigCode().equalsIgnoreCase(menuCode)) {
                gotoNextActivity(configureBO);
                break;
            }

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == 1) {
                    Uri uri = FileUtils.getUriFromFile(getActivity(), FileUtils.photoFolderPath + "/" + imageFileName);
                    bmodel.userMasterHelper.getUserMasterBO().setImagePath(imageFileName);
                    bmodel.userMasterHelper.saveUserProfile(bmodel.userMasterHelper.getUserMasterBO());
                    profileImageView.invalidate();
                    profileImageView.setImageURI(uri);
                }
                break;
            case TERMS_COND_REQ_CODE:
                if (getActivity() != null)
                    getActivity().overridePendingTransition(0, R.anim.zoom_exit);
                break;
            default:
                break;
        }
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
                                        Activity activity = getActivity();
                                        if (activity != null && isAdded())
                                            getActivity().finish();
                                        try {
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

            case 2:
                AlertDialog.Builder builderTrip = new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setNegativeButton(
                                getResources().getString(R.string.end),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        moveToSpecificMenu(MENU_LOAD_MANAGEMENT);

                                    }
                                });
                if (bmodel.configurationMasterHelper.IS_ALLOW_USER_TO_CONTINUE_FOR_MULTIPLE_DAYS_WITH_SAME_TRIP) {
                    builderTrip.setPositiveButton(getResources().getString(R.string.lbl_continue),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(bmodel.getApplicationContext());
                                    sharedPreferences.edit().putString("tripExtendedDate", DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)).apply();

                                    moveToSpecificMenu(MENU_VISIT);
                                }
                            });
                    builderTrip.setTitle(
                            getResources().getString(R.string.do_you_want_to_continue_same_trip_or_end_the_trip));
                } else {
                    builderTrip.setTitle(
                            getResources().getString(R.string.pls_end_the_previous_trip));
                }

                bmodel.applyAlertDialogTheme(builderTrip);
                break;
        }
    }


    private void gotoNextActivity(final ConfigureBO menuItem) {


        if (menuItem.getConfigCode().equals(MENU_PLANNING)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED
                    && (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                    "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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

            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else if (bmodel.configurationMasterHelper.IS_ENABLE_TRIP && !LoadManagementHelper.getInstance(getActivity().getApplicationContext()).isValidTrip()) {
                showDialog(2);
            } else if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE
                    && isInandOutModuleEnabled
                    && AttendanceHelper.getInstance(getContext()).isSellerWorking(getContext())) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.mark_attendance_working),
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
        } else if (menuItem.getConfigCode().equals(MENU_SUBD)) {
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

            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
                if (bmodel.getRetailerMaster().size() > 0) {
                    if (bmodel.getSubDMaster().size() > 0) {
                        if (!isClicked) {
                            isClicked = false;
                            bmodel.distributorMasterHelper.downloadDistributorsList();
                            bmodel.configurationMasterHelper
                                    .setSubdtitle(menuItem.getMenuName());

                            switchFragment(MENU_SUBD, menuItem.getMenuName());
                        }
                    } else {
                        Toast.makeText(getActivity(), "No Subd Available", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No Retailer Available", Toast.LENGTH_LONG).show();
                }

            }
        } else if (menuItem.getConfigCode().equals(MENU_ATTENDANCE)) {
            NonFieldHelper nonFieldHelper = NonFieldHelper.getInstance(getActivity());
            nonFieldHelper.downNonFieldReasons(getActivity());
            nonFieldHelper.downLeaveTypes(getActivity());
            nonFieldHelper.dynamicRadioButtton(getActivity());
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
                    dialogFragment = new ChannelSelectionDialog(getActivity(), mChannelList, bmodel.newOutletHelper.getLevelame());
                    dialogFragment.setChannelSelectionListener(this);
                    dialogFragment.show();
                    dialogFragment.setCancelable(false);
                } else {
                    Toast.makeText(getActivity(), "Channel Not Mapped ", Toast.LENGTH_SHORT).show();
                }


            } else {
                switchFragment(MENU_NEW_RETAILER, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_REPORT)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {

                switchFragment(MENU_REPORT, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_LOAD_MANAGEMENT)) {

            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_PLANNING_SUB, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_SYNC)) {
            switchFragment(MENU_SYNC, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_SKUWISESTGT)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_LOAD_REQUEST, menuItem.getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_DASH_KPI)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            }
//            else if (isLeave_today) {
//                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOut)
//                    Toast.makeText(getActivity(),
//                            getResources().getString(R.string.mark_attendance),
//                            Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(getActivity(),
//                            getResources().getString(R.string.leaveToday),
//                            Toast.LENGTH_SHORT).show();
//            }
            else {
                DashBoardHelper.getInstance(getActivity()).checkDayAndP3MSpinner(false);
                bmodel.distributorMasterHelper.downloadDistributorsList();

                switchFragment(MENU_DASH_KPI, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_ROUTE_KPI)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                DashBoardHelper.getInstance(getActivity()).checkDayAndP3MSpinner(false);
                bmodel.distributorMasterHelper.downloadDistributorsList();

                switchFragment(MENU_ROUTE_KPI, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_DASH)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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

        } else if (menuItem.getConfigCode().equals(MENU_DASH_INC)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                switchFragment(MENU_DASH_INC, menuItem.getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_OFLNE_PLAN)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                /*Intent i = new Intent(getContext(), OfflinePlanningActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("screentitle", "" + "Call Planning");
                startActivity(i);
                getActivity().finish();*/
                switchFragment(MENU_OFLNE_PLAN, menuItem
                        .getMenuName());
            }

        } else if (menuItem.getConfigCode().equals(MENU_JOINT_CALL)) {
            if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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


                      /*  bmodel.productHelper
                                .downloadFiveLevelFilterNonProducts(MENU_SURVEY_SW);*/

                        bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel(MENU_SURVEY_SW));
                        bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                                bmodel.productHelper.getRetailerModuleSequenceValues(), false));

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
                            getString(R.string.login_joint_Call_user), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_SURVEY01_SW)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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
                   /* bmodel.productHelper
                            .downloadFiveLevelFilterNonProducts("MENU_SURVEY01_SW");*/
                    bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel("MENU_SURVEY01_SW"));
                    bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getRetailerModuleSequenceValues(), false));
                }

                if (surveyHelperNew.getSurvey() != null
                        && surveyHelperNew.getSurvey().size() > 0) {
                    bmodel.mSelectedActivityName = menuItem.getMenuName();
                    bmodel.mSelectedActivityConfigCode = menuItem
                            .getConfigCode();
                    surveyHelperNew.loadSurveyConfig(MENU_SURVEY01_SW);
                    switchFragment(MENU_SURVEY01_SW, menuItem.getMenuName());
                } else {

                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.data_not_mapped), 0);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_SURVEY_BA_CS)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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

                    /*bmodel.productHelper
                            .downloadFiveLevelFilterNonProducts(MENU_SURVEY_BA_CS);*/
                    bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel(MENU_SURVEY_BA_CS));
                    bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getRetailerModuleSequenceValues(), false));
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
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                EmptyReconciliationHelper emptyReconciliationHelper = EmptyReconciliationHelper.getInstance(getActivity());
                emptyReconciliationHelper.downloadProducts();
                emptyReconciliationHelper.downloadNonGenericProductID();
                emptyReconciliationHelper
                        .downloadReturnProductsTypeNew();
                if (emptyReconciliationHelper.getSkuTypeBO() != null
                        && emptyReconciliationHelper.getSkuTypeBO()
                        .size() > 0) {
                    switchFragment(MENU_EMPTY_RECONCILIATION, menuItem.getMenuName());
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                }
            }
        } else if (menuItem.getConfigCode().equals(MENU_DIGITIAL_SELLER)) {
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else {
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(getActivity());
                mDigitalContentHelper.downloadDigitalContent(getContext().getApplicationContext(), "SELLER");
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
            String roadTitle = (menuItem.getMenuName() == null) ? "" : menuItem.getMenuName();
            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.mark_attendance),
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.leaveToday),
                            Toast.LENGTH_SHORT).show();
            } else
                new LoadRoadActivityData(roadTitle).execute();
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
                bmodel.mSelectedActivityName = menuItem.getMenuName();
                switchFragment(MENU_EXPENSE, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_TASK_NEW)) {
            if (!isClicked) {
                isClicked = false;
                switchFragment(MENU_TASK_NEW, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_RTR_TASK_PENDING)) {
            if (!isClicked) {
                isClicked = false;
                switchFragment(MENU_RTR_TASK_PENDING, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_PRIMARY_SALES)) {
            if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE && isInandOutModuleEnabled)
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

        } else if (menuItem.getConfigCode().equals(MENU_PRESENCE)) {
            switchFragment(MENU_PRESENCE, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_IN_OUT)) {
            bmodel.configurationMasterHelper.setTradecoveragetitle(menuItem.getMenuName());
            switchFragment(MENU_IN_OUT, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_WVW_PLAN)) {

            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                i.putExtra("menucode", menuItem.getConfigCode());
                startActivity(i);
                // getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_WEB_VIEW)) {

            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                i.putExtra("menucode", menuItem.getConfigCode());
                startActivity(i);
                //getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_WEB_VIEW_PLAN)) {

            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                i.putExtra("menucode", menuItem.getConfigCode());
                startActivity(i);
                // getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_WEB_VIEW_APPR)) {
            if (bmodel.isOnline()) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                i.putExtra("menucode", menuItem.getConfigCode());
                startActivity(i);
                // getActivity().finish();
            } else
                Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();

        } else if (menuItem.getConfigCode().equals(MENU_NEWRET_EDT)) {
            switchFragment(MENU_NEWRET_EDT, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_PLANE_MAP)) {
            switchFragment(MENU_PLANE_MAP, "Trade Coverage");
        } else if (menuItem.getConfigCode().equals(MENU_JOINT_ACK)) {
            Intent i = new Intent(getActivity(), AcknowledgementActivity.class);
            i.putExtra("screentitle", menuItem.getMenuName());
            startActivity(i);
            getActivity().finish();
        } else if (menuItem.getConfigCode().equals(MENU_NON_FIELD)) {
            bmodel.reasonHelper.downloadPlaneDeviateReasonMaster("FIELD_PLAN_TYPE");
            bmodel.reasonHelper.downloadPlannedActivitiesReasonMaster("FIELD_PLAN_TYPE");
            bmodel.reasonHelper.downloadNonPlannedReason();
            switchFragment(MENU_NON_FIELD, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_DELMGMT_RET)) {
            switchFragment(MENU_DELMGMT_RET, menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_BACKUP_SELLER)) {
            if (!isClicked) {
                isClicked = false;
                bmodel.userMasterHelper.downloadBackupSeller();
                switchFragment(MENU_BACKUP_SELLER, menuItem.getMenuName());
            }
        } else if (menuItem.getConfigCode().equals(MENU_SUPERVISOR_REALTIME)
                || menuItem.getConfigCode().equals(MENU_SUPERVISOR_MOVEMENT)
                || menuItem.getConfigCode().equals(MENU_SUPERVISOR_CALLANALYSIS)) {
            switchFragment(menuItem.getConfigCode(), menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_Q_CALL)) {
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

            if (bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED &&
                    (DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                                    .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            "yyyy/MM/dd") > 0)) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (bmodel.getRetailerMaster().size() > 0) {
                    if (!isClicked) {
                        isClicked = false;
                        bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menuItem.getConfigCode());
                        bmodel.configurationMasterHelper.downloadFloatingSurveyConfig(menuItem.getConfigCode());
                        bmodel.distributorMasterHelper.downloadDistributorsList();
                        bmodel.configurationMasterHelper
                                .setSubdtitle(menuItem.getMenuName());

                        switchFragment(MENU_Q_CALL, menuItem.getMenuName());
                    }

                } else {
                    Toast.makeText(getActivity(), "No Retailer Available", Toast.LENGTH_LONG).show();
                }

            }
        } else if (menuItem.getConfigCode().equals(MENU_DENOMINATION)) {
            switchFragment(menuItem.getConfigCode(), menuItem.getMenuName());
        } else if (menuItem.getConfigCode().equals(MENU_NOTES_SW)) {

            switchFragment(menuItem.getConfigCode(), menuItem.getMenuName());
        }


    }

    private void switchFragment(String fragmentName, String menuName) {
        android.support.v4.app.FragmentManager fm = getFragmentManager();

        NewoutletContainerFragment mNewOutletFragment = (NewoutletContainerFragment) fm
                .findFragmentByTag(MENU_NEW_RETAILER);

        VisitFragment mVisitFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_VISIT);

        SubDFragment mSubDFragment = (SubDFragment) fm
                .findFragmentByTag(MENU_SUBD);

        VisitFragment mPlanningFragment = (VisitFragment) fm
                .findFragmentByTag(MENU_PLANNING);


        SynchronizationFragment mSyncFragment = (SynchronizationFragment) fm
                .findFragmentByTag(MENU_SYNC);

        DeliveryManagementRetailersFragment deliveryRetailersFragment = (DeliveryManagementRetailersFragment) fm
                .findFragmentByTag(MENU_DELMGMT_RET);

        SellerDashboardFragment mSellerDashFragment = (SellerDashboardFragment) fm
                .findFragmentByTag(MENU_DASH_KPI);

        SellerDashboardFragment mRouteDashFragment = (SellerDashboardFragment) fm
                .findFragmentByTag(MENU_ROUTE_KPI);

        DashboardFragment mDashFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH);

        DashboardFragment mDashDayFragment = (DashboardFragment) fm
                .findFragmentByTag(MENU_DASH_DAY);
        IncentiveDashboardFragment incentiveDashboardFragment = (IncentiveDashboardFragment) fm
                .findFragmentByTag(MENU_DASH_INC);

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
        TimeTrackingFragment timeTrackingFragment = (TimeTrackingFragment) fm
                .findFragmentByTag(MENU_IN_OUT);


        PlanningMapFragment mPlanningMapFragment = (PlanningMapFragment) fm
                .findFragmentByTag(MENU_PLANE_MAP);
        LoadManagementFragment mLoadMgtFragment = (LoadManagementFragment) fm
                .findFragmentByTag(MENU_LOAD_MANAGEMENT);
        LoadManagementFragment mPlanningSubFragment = (LoadManagementFragment) fm
                .findFragmentByTag(MENU_PLANNING_SUB);
        SkuWiseTargetFragment mSKUTgtFragment = (SkuWiseTargetFragment) fm
                .findFragmentByTag(MENU_SKUWISESTGT);
        ReportMenuFragment mReportMenuFragment = (ReportMenuFragment) fm
                .findFragmentByTag(MENU_REPORT);
        StockProposalFragment stockProposalFragment = (StockProposalFragment) fm
                .findFragmentByTag(MENU_LOAD_REQUEST);
        NewOutletEditFragment newOutletEditFragment = (NewOutletEditFragment) fm
                .findFragmentByTag(MENU_NEWRET_EDT);
        ExpenseFragment expenseFragment = (ExpenseFragment) fm
                .findFragmentByTag(MENU_EXPENSE);
        AcknowledgementFragment acknowledgementFragment = (AcknowledgementFragment) fm
                .findFragmentByTag(MENU_JOINT_ACK);
        PlanDeviationFragment planDeviationFragment = (PlanDeviationFragment) fm
                .findFragmentByTag(MENU_NON_FIELD);

        TaskUnplannedFragment   taskUnplannedFragment = (TaskUnplannedFragment) fm
                    .findFragmentByTag(MENU_RTR_TASK_PENDING);

        TaskFragment   taskFragment = (TaskFragment) fm
                    .findFragmentByTag(MENU_TASK_NEW);

        BackUpSellerFragment backUpSellerFragment = (BackUpSellerFragment) fm.findFragmentByTag(MENU_BACKUP_SELLER);

        SellersMapHomeFragment supervisorMapCFragment = (SellersMapHomeFragment) fm.findFragmentByTag(MENU_SUPERVISOR_CALLANALYSIS);

        QuickCallFragment mQuickCallFragment = (QuickCallFragment) fm
                .findFragmentByTag(MENU_Q_CALL);

        DenominationFragment denominationFragment = (DenominationFragment) fm.findFragmentByTag(MENU_DENOMINATION);

        CalendarPlanFragment calendarPlanFragment = (CalendarPlanFragment) fm.findFragmentByTag(MENU_OFLNE_PLAN);

        RetailerMapFragment retailerMapFragment = (RetailerMapFragment) fm.findFragmentByTag(MENU_MAP_PLAN);

        NotesListFragment notesListFragment = (NotesListFragment) fm.findFragmentByTag(MENU_NOTES_SW);

        if (mNewOutletFragment != null && (fragmentName.equals(MENU_NEW_RETAILER))
                && mNewOutletFragment.isVisible()
                && !bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
            return;
        } else if (mPlanningFragment != null && (fragmentName.equals(MENU_PLANNING))
                && mPlanningFragment.isVisible()) {
            return;
        } else if (mVisitFragment != null && (fragmentName.equals(MENU_VISIT))
                && mVisitFragment.isVisible()) {
            return;
        } else if (mSubDFragment != null && (fragmentName.equals(MENU_SUBD))
                && mSubDFragment.isVisible()) {
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
        } else if (mRouteDashFragment != null && (fragmentName.equals(MENU_ROUTE_KPI))
                && mRouteDashFragment.isVisible()) {
            return;
        } else if (mDashFragment != null && (fragmentName.equals(MENU_DASH))
                && mDashFragment.isVisible()) {
            return;
        } else if (mDashDayFragment != null && (fragmentName.equals(MENU_DASH_DAY))
                && mDashDayFragment.isVisible()) {
            return;

        } else if (incentiveDashboardFragment != null && (fragmentName.equals(MENU_DASH_INC)) &&
                incentiveDashboardFragment.isVisible()) {
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
        } else if (timeTrackingFragment != null && (fragmentName.equals(MENU_IN_OUT))
                && timeTrackingFragment.isVisible()) {
            return;
        } else if (mPlanningMapFragment != null && (fragmentName.equals(MENU_PLANE_MAP))
                && mPlanningMapFragment.isVisible()) {
            return;
        } else if (mLoadMgtFragment != null && (fragmentName.equals(MENU_LOAD_MANAGEMENT))
                && mLoadMgtFragment.isVisible()) {
            return;
        } else if (mPlanningSubFragment != null && (fragmentName.equals(MENU_PLANNING_SUB))
                && mPlanningSubFragment.isVisible()) {
            return;
        } else if (mSKUTgtFragment != null && (fragmentName.equals(MENU_SKUWISESTGT))
                && mSKUTgtFragment.isVisible()) {
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
        } else if (acknowledgementFragment != null && fragmentName.equals(MENU_JOINT_ACK)
                && acknowledgementFragment.isVisible()) {
            return;
        } else if (planDeviationFragment != null && fragmentName.equals(MENU_NON_FIELD)
                && planDeviationFragment.isVisible()) {
            return;
        } else if
        (expenseFragment != null && (fragmentName.equals(MENU_EXPENSE))
                        && expenseFragment.isVisible()) {
            return;
        } else if (taskFragment != null && fragmentName.equals(MENU_TASK_NEW)
                && taskFragment.isVisible()) {
            return;
        } else if (taskUnplannedFragment != null && fragmentName.equals(MENU_RTR_TASK_PENDING)
                && taskUnplannedFragment.isVisible()) {
            return;
        } else if (backUpSellerFragment != null && fragmentName.equals(MENU_BACKUP_SELLER)
                && backUpSellerFragment.isVisible()) {
            return;
        } else if (supervisorMapCFragment != null && (fragmentName.equals(MENU_SUPERVISOR_CALLANALYSIS))
                && supervisorMapCFragment.isVisible()) {
            return;
        } else if (mQuickCallFragment != null && (fragmentName.equals(MENU_Q_CALL))
                && mQuickCallFragment.isVisible()) {
            return;
        } else if (denominationFragment != null && (fragmentName.equals(MENU_DENOMINATION))
                && denominationFragment.isVisible()) {
            return;
        } else if (notesListFragment != null && (fragmentName.equals(MENU_NOTES_SW))
                && notesListFragment.isVisible()) {
            return;
        } else if (calendarPlanFragment != null && (fragmentName.equals(MENU_OFLNE_PLAN))
                && calendarPlanFragment.isVisible()) {
            return;
        } else if (retailerMapFragment != null && (fragmentName.equals(MENU_MAP_PLAN))
                && retailerMapFragment.isVisible()) {
            return;
        }
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        if (mNewOutletFragment != null)
            ft.remove(mNewOutletFragment);
        if (mVisitFragment != null)
            ft.remove(mVisitFragment);
        if (mSubDFragment != null)
            ft.remove(mSubDFragment);
        if (mPlanningFragment != null)
            ft.remove(mPlanningFragment);
        if (mSyncFragment != null)
            ft.remove(mSyncFragment);
        if (deliveryRetailersFragment != null)
            ft.remove(deliveryRetailersFragment);
        if (mSellerDashFragment != null)
            ft.remove(mSellerDashFragment);
        if (mRouteDashFragment != null)
            ft.remove(mRouteDashFragment);
        if (mDashFragment != null)
            ft.remove(mDashFragment);
        if (mDashDayFragment != null)
            ft.remove(mDashDayFragment);
        if (incentiveDashboardFragment != null)
            ft.remove(incentiveDashboardFragment);
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
        if (timeTrackingFragment != null)
            ft.remove(timeTrackingFragment);
        if (mPlanningMapFragment != null)
            ft.remove(mPlanningMapFragment);
        if (mLoadMgtFragment != null)
            ft.remove(mLoadMgtFragment);
        if (mPlanningSubFragment != null)
            ft.remove(mPlanningSubFragment);
        if (mSKUTgtFragment != null)
            ft.remove(mSKUTgtFragment);
        if (mReportMenuFragment != null)
            ft.remove(mReportMenuFragment);
        if (stockProposalFragment != null)
            ft.remove(stockProposalFragment);
        if (newOutletEditFragment != null)
            ft.remove(newOutletEditFragment);
        if (acknowledgementFragment != null)
            ft.remove(acknowledgementFragment);
        if (planDeviationFragment != null)
            ft.remove(planDeviationFragment);
        if (expenseFragment != null)
            ft.remove(expenseFragment);
        if (taskFragment != null)
            ft.remove(taskFragment);
        if (taskUnplannedFragment != null)
            ft.remove(taskUnplannedFragment);
        if (backUpSellerFragment != null)
            ft.remove(backUpSellerFragment);
        if (supervisorMapCFragment != null)
            ft.remove(supervisorMapCFragment);
        if (mQuickCallFragment != null)
            ft.remove(mQuickCallFragment);
        if (denominationFragment != null)
            ft.remove(denominationFragment);
        if (notesListFragment != null)
            ft.remove(notesListFragment);
        if (calendarPlanFragment != null)
            ft.remove(calendarPlanFragment);
        if (retailerMapFragment != null)
            ft.remove(retailerMapFragment);

        Bundle bndl;
        Fragment fragment;
        switch (fragmentName) {
            case MENU_NEW_RETAILER:

                if (!bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    bmodel.newOutletHelper.downloadLinkRetailer();
                }

                if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER || bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR) {

                    GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_NEW_RETAILER);
                    if (genericObjectPair != null) {
                        bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                        bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                    }

                    bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_NEW_RETAILER));
                    bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(bmodel.productHelper.getFilterProductLevels(), true));
                }

                bmodel.newOutletHelper.downloadLocationLevels();
                //clear distributor id and group id
                bmodel.getRetailerMasterBO().setDistributorId(0);
                bmodel.getRetailerMasterBO().setGroupId(0);
                bmodel.newOutletHelper.setRetailerContactList(new ArrayList<RetailerContactBo>());
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new NewoutletContainerFragment();
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

            case MENU_SUBD:
                fragment = new SubDFragment();
                ft.add(R.id.fragment_content, fragment,
                        MENU_SUBD);
                break;
            case MENU_PLANNING:
                bndl = new Bundle();
                bndl.putString("From", MENU_PLANNING_CONSTANT);
                bndl.putBoolean("isPlanning", true);
                isVisit = false;
                bndl.putString("Newplanningsub", "");
                fragment = new VisitFragment();
                fragment.setArguments(bndl);
                ((VisitFragment) fragment).setMapViewListener(this);
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
                // fragment = new SellerDashboardFragment();
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
            case MENU_DASH_INC:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new IncentiveDashboardFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DASH_INC);
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
                bndl.putString("from", "HomeScreen");
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
                bndl.putString("from", "HomeScreen");
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
                bndl.putString("from", "HomeScreen");
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
                bndl = new Bundle();
                bndl.putString("from", fragmentName);
                fragment = new LoadManagementFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_LOAD_MANAGEMENT);
                break;
            case MENU_PLANNING_SUB:
                bmodel.configurationMasterHelper
                        .setLoadmanagementtitle(menuName);
                bndl = new Bundle();
                bndl.putString("from", fragmentName);
                fragment = new LoadManagementFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_PLANNING_SUB);
                break;
            case MENU_PRESENCE:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new AttendanceFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_PRESENCE);
                break;
            case MENU_IN_OUT:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
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
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new NonFieldHomeFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_ATTENDANCE);
                break;
            case MENU_PLANE_MAP:
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
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new ReportMenuFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_REPORT);
                break;

            case MENU_LOAD_REQUEST:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("menuCode", MENU_LOAD_REQUEST);
                bndl.putBoolean("isFromLodMgt", false);
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

            case MENU_EXPENSE:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new ExpenseFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_EXPENSE);
                break;

            case MENU_COUNTER:

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
            case MENU_TASK_NEW:
                bndl = new Bundle();
                bndl.putString(TaskConstant.SCREEN_TITLE, menuName);
                bndl.putBoolean(TaskConstant.FROM_HOME_SCREEN, true);
                bndl.putString(TaskConstant.MENU_CODE, MENU_TASK_NEW);

                if (!getTaskNotificationFlag() && taskDueDateCount > 0) {
                    setTAskNotificationFlag(true);
                    fragment = new TaskUnplannedFragment();
                    fragment.setArguments(bndl);
                    ((TaskUnplannedFragment) fragment).setTaskViewListener(this);
                    ft.add(R.id.fragment_content, fragment,
                            MENU_RTR_TASK_PENDING);
                } else {
                    fragment = new TaskFragment();
                    fragment.setArguments(bndl);
                    ((TaskFragment) fragment).setTaskViewListener(this);
                    ft.add(R.id.fragment_content, fragment,
                            MENU_TASK_NEW);
                }
                break;
            case MENU_RTR_TASK_PENDING:
                bndl = new Bundle();
                bndl.putString(TaskConstant.SCREEN_TITLE, menuName);
                bndl.putBoolean(TaskConstant.FROM_HOME_SCREEN, true);
                bndl.putString(TaskConstant.MENU_CODE, MENU_RTR_TASK_PENDING);
                setTAskNotificationFlag(true);
                fragment = new TaskUnplannedFragment();
                fragment.setArguments(bndl);
                ((TaskUnplannedFragment) fragment).setTaskViewListener(this);
                ft.add(R.id.fragment_content, fragment,
                        MENU_RTR_TASK_PENDING);
                break;
            case MENU_BACKUP_SELLER:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new BackUpSellerFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_BACKUP_SELLER);
                break;

            case MENU_SUPERVISOR_CALLANALYSIS:

                SupervisorActivityHelper.getInstance().loginToFirebase(getContext());
                SupervisorActivityHelper.getInstance().downloadOutletListAws(getContext(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putInt("TrackingType", 2);
                fragment = new SellersMapHomeFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_SUPERVISOR_CALLANALYSIS);
                break;
            case MENU_ROUTE_KPI:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                bndl.putString("retid", "0");
                bndl.putString("type", "ROUTE");
                // fragment = new SellerDashboardFragment();

                fragment = new SellerDashboardFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_ROUTE_KPI);
                break;

            case MENU_Q_CALL:
                fragment = new QuickCallFragment();
                ft.add(R.id.fragment_content, fragment,
                        MENU_Q_CALL);
                break;
            case MENU_DENOMINATION:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new DenominationFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_DENOMINATION);
                break;

            case MENU_NOTES_SW:
                bndl = new Bundle();
                bndl.putString(NoteConstant.SCREEN_TITLE, menuName);
                bndl.putBoolean(NoteConstant.FROM_HOME_SCREEN, true);
                bndl.putString(NoteConstant.MENU_CODE, MENU_NOTES_SW);
                fragment = new NotesListFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_NOTES_SW);
                break;
            case MENU_OFLNE_PLAN:
                bndl = new Bundle();
                bndl.putString("screentitle", menuName);
                fragment = new CalendarPlanFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.fragment_content, fragment,
                        MENU_OFLNE_PLAN);
                break;
        }
        ft.commitAllowingStateLoss();

    }

    /**
     * @param menuCode detach current fragment based on passed menu code
     */
    public void detach(String menuCode) {
        android.support.v4.app.FragmentManager fm = getFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        if (fm.findFragmentByTag(menuCode) != null) {
            ft.detach(fm.findFragmentByTag(menuCode));
            ft.commit();
        }

        DrawerLayout mDrawerLayout = getActivity().findViewById(R.id.drawer_layout);
        mDrawerLayout.openDrawer(GravityCompat.START);

        setScreenTitle(getResources().getString(R.string.app_name));
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
    public void switchMapView() {

        ConfigureBO conBo = new ConfigureBO();
        conBo.setConfigCode(MENU_PLANE_MAP);
        gotoNextActivity(conBo);

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


    @Override
    public void loadNewOutLet(int position, String menuName) {
        ChannelBO channelBO = mChannelList.get(position);
        bmodel.newOutletHelper.setmSelectedChannelid(channelBO.getChannelId());
        bmodel.newOutletHelper.setmSelectedChannelname(channelBO.getChannelName());
        bmodel.newOutletHelper.loadNewOutletConfiguration(channelBO.getChannelId());
        bmodel.newOutletHelper.loadRetailerType();
        bmodel.newOutletHelper.downloadLinkRetailer();
        switchFragment(MENU_NEW_RETAILER, menuName);
        dialogFragment.dismiss();
    }

    @Override
    public void switchTaskView(boolean fromPendingTask) {
        if (!fromPendingTask) {
            for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {
                if (con.getConfigCode().equals(MENU_TASK_NEW)) {
                    gotoNextActivity(con);
                    break;
                }
            }
        } else {
            ConfigureBO conBo = new ConfigureBO();
            conBo.setConfigCode(MENU_RTR_TASK_PENDING);
            conBo.setMenuName(getString(R.string.pending_task));
            conBo.setFlag(1);
            conBo.setHasLink(1);
            gotoNextActivity(conBo);
        }

    }

    private class LoadRoadActivityData extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        private String title;

        public LoadRoadActivityData(String title) {
            this.title = title;
        }

        protected void onPreExecute(String str) {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            RoadActivityHelper roadActivityHelper = RoadActivityHelper.getInstance(getActivity());
            roadActivityHelper.loadTypeSpinnerData();
            roadActivityHelper.loadProductSpinnerData();

            // Location spinners
            roadActivityHelper.loadLocationNames();
            roadActivityHelper.loadLocation1SpinnerData();
            roadActivityHelper.loadLocation2SpinnerData();

            return true;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            switchFragment(MENU_ROAD_ACTIVITY, title);
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
                holder.menuIcon = convertView
                        .findViewById(R.id.list_item_icon_ib);
                holder.menuBTN = convertView
                        .findViewById(R.id.list_item_menu_tv_new);
                holder.badgeViewTv = convertView
                        .findViewById(R.id.list_item_badge_view);

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

            if (holder.config.getConfigCode().equalsIgnoreCase(MENU_TASK_NEW)
                    && taskDueDateCount > 0) {
                holder.badgeViewTv.setVisibility(View.VISIBLE);
            } else {
                holder.badgeViewTv.setVisibility(View.GONE);
            }

            return convertView;
        }

        class ViewHolder {

            ConfigureBO config;
            int position;
            private ImageView menuIcon;
            private TextView menuBTN;
            private TextView badgeViewTv;
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
            try {
                if (getFragmentManager() != null)
                    getFragmentManager().executePendingTransactions();
                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    Toast.makeText(activity,
                            getResources().getString(R.string.menu_not_available),
                            Toast.LENGTH_LONG).show();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }
    }

    public void setmHomeScreenItemClickedListener(HomeScreenItemClickedListener mListener) {
        mHomeScreenItemClickedListener = mListener;
    }

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
                    profileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    profileImageView.setAdjustViewBounds(true);
                    //  profileImageView.setImageBitmap(getCircularBitmapFrom(myBitmap));

                    Glide.with(getActivity())
                            .load(imgFile)
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

    private void setImageFromInternalStorage() {
        try {
            if (bmodel.userMasterHelper.getUserMasterBO().getImagePath() != null &&
                    !"".equals(bmodel.userMasterHelper.getUserMasterBO().getImagePath())) {
                String[] imgPaths = bmodel.userMasterHelper.getUserMasterBO().getImagePath().split("/");
                String path = imgPaths[imgPaths.length - 1];
                File file = new File(FileUtils.photoFolderPath + "/" + path);

                profileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                profileImageView.setAdjustViewBounds(true);


                Glide.with(getActivity()).load(file)
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

    /**
     * Open camera to capture profile picture.
     */
    private void captureUserProfilePicture() {
        if (FileUtils.isExternalStorageAvailable(10)) {
            imageFileName = "USER_" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "_"
                    + Commons.now(Commons.DATE_TIME) + "_img.jpg";

            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(CameraActivity.QUALITY, 40);
                intent.putExtra(CameraActivity.PATH, FileUtils.photoFolderPath + "/" + imageFileName);
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

    private void setTAskNotificationFlag(boolean flag) {
        SharedPreferences.Editor editor = AppUtils.getSharedPreferenceByName(bmodel.getApplicationContext(), HomeMenuConstants.TASK_NOTIFICATION).edit();
        editor.putBoolean(HomeMenuConstants.TASK_NOTIFICATION, flag);
        editor.apply();
    }

    private boolean getTaskNotificationFlag() {
        SharedPreferences editor = AppUtils.getSharedPreferenceByName(bmodel.getApplicationContext(), HomeMenuConstants.TASK_NOTIFICATION);
        return editor.getBoolean(HomeMenuConstants.TASK_NOTIFICATION, false);
    }


}