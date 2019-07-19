package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.core.IvyConstants;
import com.ivy.apptutoriallibrary.AppTutorialPlugin;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.reports.performancereport.OutletPerfomanceHelper;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.cpg.view.sync.LastSyncTimeHelper;
import com.ivy.cpg.view.sync.SyncContractor;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.cpg.view.sync.UploadPresenterImpl;
import com.ivy.cpg.view.sync.UploadThread;
import com.ivy.cpg.view.sync.catalogdownload.CatalogImagesDownlaod;
import com.ivy.cpg.view.sync.largefiledownload.DigitalContentModel;
import com.ivy.cpg.view.sync.largefiledownload.FileDownloadProvider;
import com.ivy.cpg.view.sync.largefiledownload.LargeFileDownloadActivity;
import com.ivy.cpg.view.sync.uploadStatusReport.UploadStatusActivity;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.cpg.view.van.vanunload.VanUnLoadModuleHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.ApkDownloaderThread;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.LabelsKey;
import com.ivy.ui.reports.syncreport.view.SyncReportActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.NetworkUtils;
import com.ivy.utils.view.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.ivy.utils.DateTimeUtils.DATE;

public class SynchronizationFragment extends IvyBaseFragment
        implements SwitchUserDialog.onSwitchUser, SyncContractor.SyncView {


    private static BusinessModel bmodel;
    private static Button btn = null;

    private EditText txtUserName, txtPassword;
    private TextView tvwstatus;
    private CheckBox dayCloseCheckBox, withPhotosCheckBox, selectedRetailerDownloadCheckBox;
    private Button sync, download, backDateSelection;


    private Thread downloaderThread;
    private ProgressDialog progressDialog;

    private SharedPreferences mLastSyncSharedPref;


    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private TransferUtility transferUtility;

    private View view;
    private DisplayMetrics displaymetrics;


    //switchUser UserName and password
    private String userName, password;
    private boolean isSwitchUser = false;

    private boolean isClicked = false;

    private SyncronizationReceiver mSyncReceiver;
    private UploadPresenterImpl uploadPresenter;
    private LastSyncTimeHelper lastSyncTimeHelper;

    private boolean isValidUser = false;

    private boolean aws = BuildConfig.FLAVOR.equalsIgnoreCase("aws");

    private Context context;

    private enum ALERT{
        DAY_CLOSE,WITH_IMAGE_UPLOAD
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lastSyncTimeHelper = new LastSyncTimeHelper(getContext());
        VanUnLoadModuleHelper mVanUnloadHelper = VanUnLoadModuleHelper.getInstance(getActivity());
        UploadHelper mUploadHelper = UploadHelper.getInstance(getActivity());
        uploadPresenter = new UploadPresenterImpl(context, bmodel, this, mUploadHelper, mVanUnloadHelper);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.synchronization_fragment, container, false);

        downloaderThread = null;
        progressDialog = null;


        initializeItem();
        mLastSyncSharedPref = getActivity().getSharedPreferences("lastSync", Context.MODE_PRIVATE);
        registerReceiver();

        displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setElevation(0);
        }

        setScreenTitle(getResources().getString(R.string.sync));
        return view;
    }

    private void initializeItem() {

        TextView tvtitle = view.findViewById(R.id.synctitle);
        tvtitle.setText(getArguments().getString("screentitle"));

        CardView alert_card = view.findViewById(R.id.alert_card);
        if (!bmodel.labelsMasterHelper.getSyncContentHTML().equals("NULL") && !bmodel.labelsMasterHelper.getSyncContentHTML().equals("")) {
            alert_card.setVisibility(View.VISIBLE);
            TextView alert_txt = view.findViewById(R.id.alert_txt);
            alert_txt.setText(Html.fromHtml(bmodel.labelsMasterHelper.getSyncContentHTML()));
        } else {
            alert_card.setVisibility(View.GONE);
        }

        txtUserName = view.findViewById(R.id.username);
        txtPassword = view.findViewById(R.id.password);

        selectedRetailerDownloadCheckBox = view.findViewById(R.id.download_retailer);

        withPhotosCheckBox = view.findViewById(R.id.withPhotos);
        withPhotosCheckBox.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        selectedRetailerDownloadCheckBox.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.withPhotos).getTag()) != null)
                ((TextView) view.findViewById(R.id.withPhotos))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.withPhotos)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        if (bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES) {
            withPhotosCheckBox.setVisibility(View.VISIBLE);

            int dbImageCount = bmodel.synchronizationHelper
                    .countImageFiles();
            if (dbImageCount >= bmodel.configurationMasterHelper.photocount) {

                withPhotosCheckBox.setChecked(true);
                uploadPresenter.updateIsWithImageStatus(true);
            }

        }

        if (!bmodel.configurationMasterHelper.IS_ALLOW_SURVEY_WITHOUT_JOINTCALL)
            bmodel.userMasterHelper.downloadJoinCallusers();

        withPhotosCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        int dbImageCount = uploadPresenter.getImageFilesCount();
                        if (!isChecked) {

                            if (dbImageCount >= bmodel.configurationMasterHelper.photocount) {
                                showAlertOk(
                                        getResources()
                                                .getString(
                                                        R.string.its_not_possible_to_upload_without_img),
                                        0);
                                withPhotosCheckBox.setChecked(true);
                                uploadPresenter.updateIsWithImageStatus(true);
                            } else {
                                uploadPresenter.updateIsWithImageStatus(false);
                            }

                        } else {
                            uploadPresenter.updateIsWithImageStatus(true);

                        }
                        updateSyncButtonStatus(1);

                    }
                });

        dayCloseCheckBox = view.findViewById(R.id.dayClose);

        if (bmodel.configurationMasterHelper.SHOW_SYNC_DAYCLOSE) {
            view.findViewById(R.id.dayclose_lty).setVisibility(View.VISIBLE);
            dayCloseCheckBox.setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.dayclose_lty).setVisibility(View.GONE);
            dayCloseCheckBox.setVisibility(View.GONE);
        }

        dayCloseCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {

                            boolean proceedDayClose = LoadManagementHelper.getInstance(context.getApplicationContext()).validateDayClose(context, true, uploadPresenter, dayCloseCheckBox);

                            if (proceedDayClose) {
                                uploadPresenter.updateDayCloseStatus(true);
                            } else {
                                dayCloseCheckBox.setChecked(false);
                                uploadPresenter.updateDayCloseStatus(false);
                            }


                        }
                        updateSyncButtonStatus(1);
                    }
                });

        if (bmodel.configurationMasterHelper.IS_RTR_WISE_DOWNLOAD) {
            selectedRetailerDownloadCheckBox.setVisibility(View.VISIBLE);
        }


        tvwstatus = view.findViewById(R.id.status);

        sync = view.findViewById(R.id.startsync);
        sync.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        sync.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                bmodel = (BusinessModel) getActivity().getApplicationContext();
                bmodel.setContext(getActivity());

                if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.sessionout_loginagain),
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

                if (uploadPresenter.isValidUser(txtUserName.getText().toString(), txtPassword.getText().toString())) {

                    if (bmodel.getAppDataProvider().getPausedRetailer() == null) {

                        // Below two lines commented by Abbas, bec it's not adding any value.
                        //isValidUser = !aws || presenter.isValidUser(txtUserName.getText().toString(), txtPassword.getText().toString());
                        //if (isValidUser)

                        if (dayCloseCheckBox.isChecked()) {
                            showAlertOkCancel(
                                    getResources()
                                            .getString(
                                                    R.string.do_u_want_to_close_the_day),
                                    ALERT.DAY_CLOSE);

                        } else {
                            uploadPresenter.validateAndUpload(false);
                        }

                    } else {
                        Toast.makeText(getActivity(), R.string.close_paused_visit_to_sync, Toast.LENGTH_LONG).show();
                    }
                } else {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.password_does_not_match), 0);
                }
            }
        });


        download = view.findViewById(R.id.download);
        download.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        download.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                bmodel = (BusinessModel) getActivity().getApplicationContext();
                bmodel.setContext(getActivity());

                if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.sessionout_loginagain),
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

                isSwitchUser = false;
                if (bmodel.outletTimeStampHelper
                        .isJointCall(bmodel.userMasterHelper.getUserMasterBO()
                                .getJoinCallUserList())) {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.logout_joint_user), 0);
                    return;
                }
                if (NetworkUtils.isNetworkConnected(getActivity())) {
                    if (uploadPresenter.checkDataForSync()) {

                        try {
                            if (bmodel.labelsMasterHelper
                                    .applyLabels(LabelsKey.PRESS_START_SYNC) != null)
                                bmodel.showAlert(bmodel.labelsMasterHelper
                                        .applyLabels(LabelsKey.PRESS_START_SYNC), 0);
                            else
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.press_start_sync), 0);

                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    } else if (bmodel.synchronizationHelper
                            .countImageFiles() > 0) {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.pls_upload_images_before_download), 0);
                    } else if (!UploadHelper.getInstance(getContext()).isAttendanceCompleted(getContext())) {
                        showAttendanceNotCompletedToast();
                    } else {
                        if (!selectedRetailerDownloadCheckBox.isChecked()) {
                            if (bmodel.configurationMasterHelper.SHOW_DOWNLOAD_ALERT)
                                showAlertForDownload();
                            else {

                                if (!bmodel.isAutoUpdateAvailable()) {
                                    isValidUser = !aws || bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword.getText().toString());

                                    if (isValidUser) {

                                        if (!isClicked) {
                                            isClicked = true;
                                            new CheckNewVersionTask()
                                                    .execute(0);
                                        }

                                    } else {
                                        bmodel.showAlert(
                                                getResources().getString(
                                                        R.string.password_does_not_match),
                                                0);
                                    }
                                } else {
                                    showAlertOk(
                                            getResources().getString(
                                                    R.string.update_available),
                                            DataMembers.NOTIFY_AUTOUPDATE_FOUND);
                                }

                            }
                        } else {
                            isValidUser = !aws || bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword.getText().toString());
                            if (isValidUser) {

                                Intent i = new Intent(getActivity(), RetailerSelectionActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            } else {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.password_does_not_match),
                                        0);
                            }

                        }
                    }
                } else {
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                }
            }
        });


        backDateSelection = view.findViewById(R.id.downloaddate);
        if (bmodel.configurationMasterHelper.IS_ENABLE_BACKDATE_REPORTING) {
            backDateSelection.setVisibility(View.VISIBLE);
        }

        backDateSelection.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        if (DataMembers.backDate.isEmpty()) {
            backDateSelection.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                    .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
        } else {
            backDateSelection.setText(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_HYPHEN));
        }
        backDateSelection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn = backDateSelection;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker1");
            }
        });

        Button close = view.findViewById(R.id.syncButtonBack);
        close.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                getActivity().finish();
            }
        });

        txtUserName.setText(bmodel.userMasterHelper.getUserMasterBO()
                .getLoginName());

        TextView closeDay_tv = view.findViewById(R.id.close_day_tv);
        closeDay_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView close_date = view.findViewById(R.id.closingDay);
        close_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        close_date.setText(DateTimeUtils
                .convertFromServerDateToRequestedFormat(
                        bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                        ConfigurationMasterHelper.outDateFormat));//changed bcz close_date shows current date, replaced to show downloaded date

        Button gprsAvailablityButton = view.findViewById(R.id.gprsAvailablityButton);
        if (NetworkUtils.isNetworkConnected(getActivity()))
            gprsAvailablityButton.setBackgroundDrawable(ContextCompat
                    .getDrawable(getActivity(), R.drawable.greenball));
        else
            gprsAvailablityButton.setBackgroundDrawable(ContextCompat
                    .getDrawable(getActivity(), R.drawable.redball));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.startsync).getTag()) != null)
                ((TextView) view.findViewById(R.id.startsync))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.startsync)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(closeDay_tv.getTag()) != null)
                closeDay_tv.setText(bmodel.labelsMasterHelper
                        .applyLabels(closeDay_tv.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            // Set title to actionbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.sync));
            //((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.icon_sync);
            // Used to on / off the back arrow icon
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        bmodel.synchronizationHelper.isLastVisitTranDownloadDone = true;
        bmodel.synchronizationHelper.isSihDownloadDone = false;
        bmodel.synchronizationHelper.isDistributorDownloadDone = false;


        if (!aws) {
            txtPassword.setVisibility(View.GONE);
        } else {
            txtPassword.setVisibility(View.VISIBLE);
        }


        updateSyncButtonStatus(2);
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 1 || s.toString().equals("")) {
                    updateSyncButtonStatus(2);
                } else {
                    updateSyncButtonStatus(1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        isSwitchUser = false;

    }

    private void updateSyncButtonStatus(int btn_count) {

        TypedArray type_arr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        int text_color = type_arr.getColor(R.styleable.MyTextView_textColor, 0);
        int background_color = type_arr.getColor(R.styleable.MyTextView_accentcolor, 0);

        if (!aws || txtPassword.getText().toString().length() > 0) {
            if (btn_count == 1) {
                if ((uploadPresenter.checkDataForSync() || withPhotosCheckBox.isChecked() || dayCloseCheckBox.isChecked()
                        && (bmodel.synchronizationHelper
                        .countImageFiles() > 0)) || (dayCloseCheckBox.isChecked())) {
                    sync.setBackgroundResource(R.drawable.round_light);
                    GradientDrawable drawable = (GradientDrawable) sync.getBackground();
                    drawable.setColor(background_color);
                    sync.setTextColor(text_color);
                    sync.setClickable(true);

                    download.setBackgroundResource(R.drawable.round_disabled_btn);
                    download.setTextColor(text_color);
                    download.setClickable(false);
                    if (bmodel.configurationMasterHelper.IS_ENABLE_BACKDATE_REPORTING)
                        backDateSelection.setClickable(false);

                } else {
                    sync.setBackgroundResource(R.drawable.round_disabled_btn);
                    sync.setTextColor(text_color);
                    sync.setClickable(false);

                    download.setBackgroundResource(R.drawable.round_light);
                    download.setTextColor(text_color);
                    download.setClickable(true);
                    GradientDrawable drawable = (GradientDrawable) download.getBackground();
                    drawable.setColor(background_color);
                    if (bmodel.configurationMasterHelper.IS_ENABLE_BACKDATE_REPORTING)
                        backDateSelection.setClickable(true);
                }
            }
        } else {
            download.setBackgroundResource(R.drawable.round_disabled_btn);
            download.setTextColor(text_color);
            download.setClickable(false);
            if (bmodel.configurationMasterHelper.IS_ENABLE_BACKDATE_REPORTING)
                backDateSelection.setClickable(false);

            sync.setBackgroundResource(R.drawable.round_disabled_btn);
            sync.setTextColor(text_color);
            sync.setClickable(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        // If trip enabled, then day close will be considered if trip is closed in load management screen.
        if (bmodel.configurationMasterHelper.IS_ENABLE_TRIP) {
            // Day close checkbox is not needed if this config enabled.
            if (LoadManagementHelper.getInstance(context.getApplicationContext()).isTripEnded(context))
                uploadPresenter.updateDayCloseStatus(true);
        }

        setDayCloseEnableDisable();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        updateLastTransactionTimeInView();

        AppTutorialPlugin.getInstance().setCurrentScreen("Synchronization");
    }


    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            final Calendar minCalemdar = Calendar.getInstance();
            minCalemdar.add(Calendar.DAY_OF_MONTH, -bmodel.configurationMasterHelper.MAXIMUM_BACKDATE_DAYS);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
            dialog.getDatePicker().setMinDate(minCalemdar.getTimeInMillis());

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (this.getTag().equals("datePicker1")) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    DataMembers.backDate = (DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                    btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                } else {
                    DataMembers.backDate = (DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                    btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));


                    if (DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), "MM/dd/yyyy").equals(DateTimeUtils.now(DATE)))
                        DataMembers.backDate = "";
                    else
                        DataMembers.backDate = DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), "MM/dd/yyyy");

                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getActivity()).edit();
                    editor.putString("backDate", DataMembers.backDate);
                    editor.commit();
                }
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sync, menu);

        MenuItem menuItem = menu.getItem(0);

        if (NetworkUtils.isNetworkConnected(getActivity())) {
            menuItem.setIcon(R.drawable.greenball);
        } else {
            menuItem.setIcon(R.drawable.redball);
        }
        if (!bmodel.configurationMasterHelper.SHOW_SYNC_EXPORT_TXT)
            menu.findItem(R.id.menu_export_txt).setVisible(false);

        if (!bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
            menu.findItem(R.id.menu_catalog_img).setVisible(false);

        if (bmodel.synchronizationHelper.isDayClosed() && !uploadPresenter.checkDataForSync())
            menu.findItem(R.id.menu_switch_user).setVisible(true);
        else
            menu.findItem(R.id.menu_switch_user).setVisible(false);

        if (!bmodel.configurationMasterHelper.SHOW_DATA_UPLOAD_STATUS)
            menu.findItem(R.id.menu_sync_report).setVisible(false);

        if (bmodel.getDigitalContentLargeFileURLS().size() > 0) {
            ArrayList<DigitalContentModel> digitalContentSavedList = FileDownloadProvider.getInstance(getContext()).getDigitalContentList();
            boolean isShowDownloadMenu = false;
            if (digitalContentSavedList != null && digitalContentSavedList.size() > 0) {
                int incrementList = 0;
                for (DigitalContentModel digitalContentModel : digitalContentSavedList) {
                    if (bmodel.getDigitalContenLargeFileModel(digitalContentModel.getImageID()) == null) {
                        FileDownloadProvider.getInstance(getContext()).removeDigitalContentModel(digitalContentModel.getImageID());
                    } else if (digitalContentModel.getStatus() != null
                            && digitalContentModel.getStatus().equals(FileDownloadProvider.STATUS_ERROR)) {
                        menu.findItem(R.id.menu_file_download).setIcon(R.drawable.ic_action_file_download_error);
                        isShowDownloadMenu = true;
                        break;
                    } else if (digitalContentModel.getStatus() != null
                            && digitalContentModel.getStatus().equals(FileDownloadProvider.DONE)) {
                        if (incrementList == digitalContentSavedList.size() - 1) {
                            isShowDownloadMenu = false;
                        }
                        incrementList = incrementList + 1;
                    } else
                        isShowDownloadMenu = true;
                }
            }
            if (isShowDownloadMenu)
                menu.findItem(R.id.menu_file_download).setVisible(true);
        } else
            menu.findItem(R.id.menu_file_download).setVisible(false);

        menu.findItem(R.id.menu_download_upload_report).setVisible(bmodel.configurationMasterHelper.SHOW_SYNC_EXPORT_TXT);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_gps_available) {
            if (NetworkUtils.isNetworkConnected(getActivity())) {
                item.setIcon(R.drawable.greenball);
                Toast.makeText(getActivity(), getResources().getString(R.string.network_connectionC_Avail),
                        Toast.LENGTH_SHORT).show();
            } else {
                item.setIcon(R.drawable.redball);
                Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection),
                        Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.menu_catalog_img) {
            startActivity(new Intent(getActivity(), CatalogImagesDownlaod.class));

        } else if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            getActivity().finish();

        } else if (i == R.id.menu_switch_user) {
            FragmentManager ft = getActivity().getSupportFragmentManager();
            SwitchUserDialog dialog = new SwitchUserDialog();
            dialog.setTargetFragment(this, 0);
            dialog.setCancelable(false);
            dialog.show(ft, "MENU_SYNC");

        } else if (i == R.id.menu_sync_report) {
            startActivity(new Intent(getActivity(), UploadStatusActivity.class));
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        } else if (i == R.id.menu_file_download) {
            startActivity(new Intent(getActivity(), LargeFileDownloadActivity.class));
        } else if (i == R.id.menu_download_upload_report) {
            startActivity(new Intent(getActivity(), SyncReportActivity.class));
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        }
        return true;
    }

    public Handler getHandler() {
        return handler;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            bmodel = (BusinessModel) context.getApplicationContext();
            isClicked = false;
            setDayCloseEnableDisable();
            SyncDownloadStatusDialog sdsd;
            switch (msg.what) {

                case DataMembers.NOTIFY_DATABASE_NOT_SAVED:
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(R.string.unable_to_back_up_db),
                            Toast.LENGTH_SHORT).show();
                    break;

                case DataMembers.NOTIFY_UPDATE:
                    builder = new AlertDialog.Builder(getActivity());
                    setMessageInProgressDialog(builder, msg.obj.toString());
                    alertDialog.show();
                    String s = tvwstatus.getText() + DataMembers.CR1
                            + msg.obj;
                    tvwstatus.setText(s);
                    break;

                case DataMembers.NOTIFY_NOT_USEREXIST:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.password_does_not_match), 0);
                    break;

                case DataMembers.NOTIFY_NO_INTERNET:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                    break;

                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                    break;

                case DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL:
                    isClicked = false;
                    alertDialog.dismiss();
                    bmodel.showAlert(getResources().getString(R.string.sessionout_loginagain), 0);
                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();
                    if (isSwitchUser) {
                        //getActivity().finish();
                        /*BusinessModel.loadActivity(getActivity(),
                                DataMembers.actHomeScreen);*/
                        // moveToHomeScreenActivity();
                        if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                            new UploadSyncLog().execute();
                        else {
                            getActivity().finish();
                            moveToHomeScreenActivity();
                        }
                    } else {
                        bmodel.daySpinnerPositon = 0;
                        bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
                        if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                            new UploadSyncLog().execute();
                    }

                    isClicked = false;
                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    dismissCurrentProgressDialog();
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                    getActivity().finish();
                    break;

                case DataMembers.NOTIFY_UPLOADED_CONTINUE:
                    alertDialog.dismiss();
                    uploadPresenter.upload();
                    break;

                case DataMembers.NOTIFY_UPLOADED:

                    if ((withPhotosCheckBox.isChecked() || !bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES)
                            && (uploadPresenter.getImageFilesCount() > 0 || uploadPresenter.getTextFilesCount() > 0)) {

                        String s1 = tvwstatus.getText()
                                + DataMembers.CR1
                                + getResources().getString(
                                R.string.data_upload_completed_sucessfully);
                        tvwstatus.setText(s1);

                        // Store last transaction time.
                        lastSyncTimeHelper.updateUploadedTime();
                        // Display last sync time on  UI
                        updateLastTransactionTimeInView();

                        builder = new AlertDialog.Builder(getActivity());
                        setMessageInProgressDialog(builder, getResources().getString(
                                R.string.image_uploading));
                        alertDialog.show();

                        uploadPresenter.uploadImages();

                    } else {

                        alertDialog.dismiss();
                        withPhotosCheckBox.setChecked(false);

                        //Not understand the purpose of below line
                        updateLastSync();

                        // Store last transaction time.
                        lastSyncTimeHelper.updateUploadedTime();

                        // Display last sync time on  UI
                        updateLastTransactionTimeInView();

                        tvwstatus.setText(getResources().getString(
                                R.string.data_upload_completed_sucessfully));

                        // Show Sucess Dialog.
                        displaymetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        sdsd = new SyncDownloadStatusDialog(getActivity(), getResources().getString(
                                R.string.data_upload_completed_sucessfully), displaymetrics);
                        sdsd.show();


                        if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                            new UploadSyncLog().execute();
                    }
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;
                case DataMembers.NOTIFY_URL_NOT_CONFIGURED:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.url_not_mapped), 0);
                    break;
                case DataMembers.NOTIFY_UPLOADED_IMAGE:
                    withPhotosCheckBox.setChecked(false);
                    if (bmodel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT)
                        uploadPresenter.loadRetailerSelectionScreen();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.images_sucessfully_uploaded), 0);
                    if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                        new UploadSyncLog().execute();
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR_IMAGE:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.images_upload_failed_please_try_again),
                            0);
                    break;


                case DataMembers.NOTIFY_WEB_UPLOAD_ERROR:
                    String s1 = tvwstatus.getText() + DataMembers.CR1
                            + msg.obj;
                    tvwstatus.setText(s1);
                    alertDialog.dismiss();
                    bmodel.showAlert((String) msg.obj, 0);
                    break;
                case DataMembers.NOTIFY_WEB_UPLOAD_SUCCESS:
                    String s2 = tvwstatus.getText() + DataMembers.CR1
                            + msg.obj;
                    tvwstatus.setText(s2);
                    withPhotosCheckBox.setChecked(false);
                    bmodel.photocount = 0;
                    alertDialog.dismiss();
                    //bmodel.showAlert(getResources().getString(R.string.successfully_uploaded), 0);
                    displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    sdsd = new SyncDownloadStatusDialog(getActivity(), getResources().getString(
                            R.string.successfully_uploaded), displaymetrics);
                    sdsd.show();
                    if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                        new UploadSyncLog().execute();
                    break;
                case DataMembers.NOTIFY_EXPORT_SUCCESS:
                    try {
                        File folder = new File(
                                getActivity()
                                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                        + "/" + DataMembers.photoFolderName + "/");

                        File sfFiles[] = folder.listFiles();
                        int ss = sfFiles.length;
                        for (int i = 0; i < ss; i++) {
                            new File(folder, "/" + sfFiles[i].getName()).delete();
                        }
                        isClicked = false;
                        alertDialog.dismiss();
                        tvwstatus.setText(getResources().getString(
                                R.string.file_export_success));
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.file_export_success), 0);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    break;
                case DataMembers.NOTIFY_EXPORT_FAILURE:
                    isClicked = false;
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(R.string.file_export_failure),
                            0);
                    break;

                default:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    uploadPresenter.upload();

                }
                break;

            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle returnValue = data.getExtras();
                    uploadPresenter.setIsVisitedRetailerList(returnValue.getParcelableArrayList("VisitedList"));
                    uploadPresenter.prepareSelectedRetailerIds();
                    if (uploadPresenter.getVisitedRetailerId() != null
                            && uploadPresenter.getVisitedRetailerId().toString().length() > 0) {
                        isClicked = false;
                        uploadPresenter.upload();
                    } else {
                        bmodel.showAlert(
                                getResources()
                                        .getString(R.string.no_unsubmitted_orders), 0);
                        isClicked = false;
                        //dialog.dismiss();
                    }

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    isClicked = false;
                }
                break;

            case SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    new InitiateDistributorDownload().execute();
                }
                break;
        }
    }

    public void showAlertOk(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (idd == DataMembers.NOTIFY_AUTOUPDATE_FOUND) {
                            Commons.printInformation(bmodel.getUpdateURL());
                            downloaderThread = new ApkDownloaderThread(
                                    getActivity(), activityHandler, bmodel
                                    .getUpdateURL(), false,
                                    ApkDownloaderThread.APK_DOWNLOAD);
                            downloaderThread.start();
                        }
                    }

                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void showAlertOkCancel(String msg, ALERT id) {
        final ALERT idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (idd == ALERT.DAY_CLOSE) {
                            uploadPresenter.validateAndUpload(true);
                        } else if (idd == ALERT.WITH_IMAGE_UPLOAD) {
                            isClicked = false;
                            withPhotosCheckBox.setChecked(true);
                            uploadPresenter.updateIsWithImageStatus(true);
                            uploadPresenter.upload();
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isClicked = false;
                        if (idd == ALERT.WITH_IMAGE_UPLOAD) {
                            uploadPresenter.upload();
                        }
                    }
                });


        bmodel.applyAlertDialogTheme(builder);
    }

    /**
     * This is the Handler for this activity. It will receive messages from the
     * apk ApkDownloaderThread and make the necessary updates to the UI.
     */
    @SuppressLint("HandlerLeak")
    public Handler activityHandler = new Handler() {
        public void handleMessage(Message msg) {
            setDayCloseEnableDisable();
            switch (msg.what) {
                /*
                 * Handling MESSAGE_UPDATE_PROGRESS_BAR: 1. Get the current
                 * progress, as indicated in the arg1 field of the Message. 2.
                 * Update the progress bar.
                 */
                case DataMembers.MESSAGE_UPDATE_PROGRESS_BAR:
                    if (progressDialog != null) {
                        int currentProgress = msg.arg1;
                        progressDialog.setProgress(currentProgress);
                    }
                    break;

                /*
                 * Handling MESSAGE_CONNECTING_STARTED: 1. Get the URL of the file
                 * being downloaded. This is stored in the obj field of the Message.
                 * 2. Create an indeterminate progress bar. 3. Set the message that
                 * should be sent if user cancels. 4. Show the progress bar.
                 */
                case DataMembers.MESSAGE_CONNECTING_STARTED:
                    if (msg.obj != null && msg.obj instanceof String) {
                        String url = (String) msg.obj;
                        // truncate the url
                        if (url.length() > 16) {
                            String tUrl = url.substring(0, 15);
                            tUrl += "...";
                            url = tUrl;
                        }
                        String pdTitle = getActivity()
                                .getString(R.string.progress_dialog_title_connecting);
                        String pdMsg = getActivity()
                                .getString(R.string.progress_dialog_message_prefix_connecting);
                        pdMsg += " " + url;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog
                                .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);

                        progressDialog.setCancelable(false);

                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                    }
                    break;

                /*
                 * Handling MESSAGE_DOWNLOAD_STARTED: 1. Create a progress bar with
                 * specified max value and current value 0; assign it to
                 * progressDialog. The arg1 field will contain the max value. 2. Set
                 * the title and text for the progress bar. The obj field of the
                 * Message will contain a String that represents the name of the
                 * file being downloaded. 3. Set the message that should be sent if
                 * dialog is canceled. 4. Make the progress bar visible.
                 */
                case DataMembers.MESSAGE_DOWNLOAD_STARTED:
                    dismissCurrentProgressDialog();
                    // obj will contain a String representing the file name
                    if (msg.obj != null && msg.obj instanceof String) {
                        int maxValue = msg.arg1;
                        String fileName = (String) msg.obj;
                        String pdTitle = getActivity()
                                .getString(R.string.progress_dialog_title_downloading);
                        String pdMsg = getActivity()
                                .getString(R.string.progress_dialog_message_prefix_downloading);
                        pdMsg += " " + fileName;


                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(maxValue);

                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                    }
                    break;

                /*
                 * Handling MESSAGE_APK_DOWNLOAD_COMPLETE: 1. Remove the progress bar
                 * from the screen. 2. Display Toast that says download is complete.
                 */
                case DataMembers.MESSAGE_APK_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    // Here Code to call dwnloaded apk.


                    LoginHelper.getInstance(getActivity()).deleteAllValues(getContext().getApplicationContext());
                    // bmodel.activationHelper.clearAppUrl();
                    clearAppUrl();
                    bmodel.userMasterHelper.getUserMasterBO().setUserid(0);
                    bmodel.codeCleanUpUtil.setUserId(0);
                    try {
                        Uri path;
                        if (Build.VERSION.SDK_INT >= 24) {
                            path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(
                                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + DataMembers.fileName));
                            Intent sintent = ShareCompat.IntentBuilder.from(getActivity())
                                    .setStream(path) // uri from FileProvider
                                    .getIntent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(path, "application/vnd.android.package-archive")
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(sintent);

                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            path = Uri.fromFile(new File(
                                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + DataMembers.fileName));
                            intent.setDataAndType(path, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    break;

                /*
                 * Handling MESSAGE_ENCOUNTERED_ERROR_APK: 1. Check the obj field of the
                 * message for the actual error message that will be displayed to
                 * the user. 2. Remove any progress bars from the screen. 3. Display
                 * a Toast with the error message.
                 */
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }

                    if (bmodel.isDigitalContentAvailable()) {


                        if (bmodel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE) {
                            AzureConnectionHelper.getInstance().setAzureCredentials(getActivity());
                            try {
                                downloaderThread = new DownloaderThreadNew(getActivity(), activityHandler, bmodel.getDigitalContentURLS(),
                                        bmodel.userMasterHelper.getUserMasterBO().getUserid(),
                                        AzureConnectionHelper.getInstance().initializeAzureStorageConnection(), bmodel.getDigitalContentSFDCURLS());
                            } catch (Exception e) {
                                Commons.printException(e);
                                bmodel.showAlert(context.getString(R.string.error_message_general), 0);
                            }
                        } else if (bmodel.configurationMasterHelper.IS_S3_CLOUD_STORAGE) {
                            AWSConnectionHelper.getInstance().setAmazonS3Credentials(getActivity());
                            transferUtility = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), getActivity());

                            downloaderThread = new DownloaderThreadNew(getActivity(),
                                    activityHandler, bmodel.getDigitalContentURLS(),
                                    bmodel.userMasterHelper.getUserMasterBO()
                                            .getUserid(), transferUtility, bmodel.getDigitalContentSFDCURLS());

                        }
                        downloaderThread.start();
                    }

                    break;

                case DataMembers.THIRD_PARTY_INSTALLATION_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    break;

                case DataMembers.SDCARD_NOT_AVAILABLE:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();
                    if (isSwitchUser) {
                        if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                            new UploadSyncLog().execute();
                        else {
                            getActivity().finish();
                            moveToHomeScreenActivity();
                        }
                    } else {
                        bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
                        if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                            new UploadSyncLog().execute();
                    }
                    isClicked = false;
                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    dismissCurrentProgressDialog();
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                    getActivity().finish();
                    break;

                default:
                    // nothing to do here
                    break;
            }
        }
    };


    private void moveToHomeScreenActivity() {

        Intent myIntent = new Intent(getActivity(), HomeScreenActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getActivity().startActivityForResult(myIntent, 0);
    }

    /**
     * If there is a progress dialog, dismiss it and set progressDialog to null.
     */
    public void dismissCurrentProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Displays a message to the user, in the form of a Toast.
     *
     * @param message Message to be displayed.
     */
    public void displayMessage(String message) {
        if (message != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    class CheckNewVersionTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (NetworkUtils.isNetworkConnected(getActivity())) {
                    if (!aws)
                        return false;

                    bmodel.synchronizationHelper.updateAuthenticateToken(true);
                    if (!bmodel.synchronizationHelper.getSecurityKey().equals(""))
                        return bmodel.synchronizationHelper.checkForAutoUpdate();
                    else
                        return Boolean.FALSE;
                } else
                    return Boolean.FALSE;

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.FALSE; // Return your real result here
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.checking_new_version));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            if (!aws) {
                bmodel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                    @Override
                    public String onSuccess(String result) {
                        new UrlDownloadData().execute();
                        return "Success";
                    }

                    @Override
                    public String onFailure(String errorresult) {
                        return "Failure";
                    }
                });
            } else {
                if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    if (!result) {
                        if (!bmodel.synchronizationHelper.getSecurityKey().equals(""))
                            new UrlDownloadData().execute();
                        else {
                            isClicked = false;
                            Toast.makeText(getActivity(), R.string.authentication_error, Toast.LENGTH_LONG).show();
                            if (alertDialog != null)
                                alertDialog.dismiss();
                        }
                    } else {
                        showAlertOk(
                                getResources().getString(R.string.update_available),
                                DataMembers.NOTIFY_AUTOUPDATE_FOUND);
                    }
                } else {
                    String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                    if (alertDialog != null)
                        alertDialog.dismiss();
                }
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterMyBroadcastReceiver();
    }

    private void unregisterMyBroadcastReceiver() {
        if (null != mSyncReceiver) {
            getActivity().unregisterReceiver(mSyncReceiver);
            mSyncReceiver = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
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
                    Commons.printException("" + e);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public void showAlertForDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(
                R.string.are_you_sure_you_want_to_download));
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if (!bmodel.isAutoUpdateAvailable()) {
                            isValidUser = !aws || bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword.getText().toString());
                            if (isValidUser) {

                                if (!isClicked) {
                                    isClicked = true;
                                    new CheckNewVersionTask()
                                            .execute(0);
                                }

                            } else {
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.password_does_not_match),
                                        0);
                            }
                        } else {
                            showAlertOk(
                                    getResources().getString(
                                            R.string.update_available),
                                    DataMembers.NOTIFY_AUTOUPDATE_FOUND);
                        }

                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }


    public class SyncronizationReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.SYNC";

        @Override
        public void onReceive(Context context, Intent intent) {

            updateReceiver(intent);
        }

    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);
        int updateTableCount = bundle.getInt("updateCount");
        int totalTableCount = bundle.getInt("totalCount");
        switch (method) {
            case SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT:
                if (errorCode.equals(IvyConstants.UPDATE_TABLE_SUCCESS_CODE)) {

                    updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                ConfigurationMasterHelper.outDateFormat));
                        edt.putString("time", DateTimeUtils.now(DateTimeUtils.TIME));
                        edt.apply();
                        lastSyncTimeHelper.updateDownloadTime();
                        updateLastTransactionTimeInView();
                    }
                } else if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    //outelet Performac
                    if (OutletPerfomanceHelper.getInstance(getActivity()).getPerformRptUrl().length() > 0) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(getActivity())
                                .edit();
                        editor.putString("rpt_dwntime",
                                DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                        editor.commit();
                    }
                    new UpdateFinish().execute();
                } else {
                    reDownloadAlert(bundle);
                }

                break;

            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (errorCode.equals(IvyConstants.UPDATE_TABLE_SUCCESS_CODE)) {

                    updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                ConfigurationMasterHelper.outDateFormat));
                        edt.putString("time", DateTimeUtils.now(DateTimeUtils.TIME));
                        edt.apply();
                        lastSyncTimeHelper.updateDownloadTime();
                        updateLastTransactionTimeInView();
                    }
                } else if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    new UpdateDistributorFinish().execute();
                } else {
                    reDownloadAlert(bundle);
                }
                break;
            default:
                break;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                SyncronizationReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mSyncReceiver = new SyncronizationReceiver();
        getActivity().registerReceiver(mSyncReceiver, filter);
    }

    @Override
    public void onBackPressed() {

        setDayCloseEnableDisable();
        super.onBackPressed();
    }

    private void clearAmazonDownload() {
        if (transferUtility != null) {
            transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
        }
    }

    public void setDayCloseEnableDisable() {
        if (bmodel.synchronizationHelper.isDayClosed()) {
            dayCloseCheckBox.setChecked(false);
            dayCloseCheckBox.setEnabled(false);
        } else {
            dayCloseCheckBox.setEnabled(true);
        }
        updateSyncButtonStatus(1);
    }

    private void updateLastSync() {
        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
        edt.putString("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        edt.putString("time", DateTimeUtils.now(DateTimeUtils.TIME));
        edt.apply();
    }

    private class DeleteTables extends
            AsyncTask<Integer, Integer, Integer> {
        boolean isDayClosed;
        boolean isDownloaded;

        DeleteTables(boolean isDayClosed, boolean isDownloaded) {
            this.isDayClosed = isDayClosed;
            this.isDownloaded = isDownloaded;
        }


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            if (isDayClosed) {
                bmodel.synchronizationHelper.deleteTables(true);
            } else {
                bmodel.synchronizationHelper.deleteTables(false);
            }

            return 0;
        }

        protected void onPostExecute(Integer result) {
            if (isDownloaded) {
                ArrayList<String> urlList = bmodel.synchronizationHelper.getUrlList();
                if (urlList != null && urlList.size() > 0) {
                    Activity activity = getActivity();
                    if (activity != null && isAdded()) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(getActivity())
                                .edit();
                        editor.putInt("trade_coverage_validation",
                                0);
                        editor.apply();
                    }
                    bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.SYNC, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
                } else {
                    alertDialog.dismiss();
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_data_download), Toast.LENGTH_SHORT).show();
                    isClicked = false;
                }
            }
        }
    }

    /**
     * UrlDownload Data class is download master mapping url from server
     * and insert into sqlite file
     */
    class UrlDownloadData extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bmodel.synchronizationHelper.setmJsonObjectResponseBytableName(new HashMap<String, JSONObject>());
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            if (!aws) {
                bmodel.synchronizationHelper.getUrldownloadMasterSFDC(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                    @Override
                    public String onSuccess(String result) {
                        String response = returnUrlDownloadResponse(result);//bmodel.synchronizationHelper.getUrlDownloadJson();
                        updateDeleteTableStatus(response);
                        return response;
                    }

                    @Override
                    public String onFailure(String errorresult) {
                        String response = returnUrlDownloadResponse(errorresult);
                        updateDeleteTableStatus(response);
                        return response;
                    }
                });

            } else {

                String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.URLDOWNLOAD_MASTER_APPEND_URL, jsonObject);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                            String errorCode = jsonObject.getString(key);
                            if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonObject, true);
                                bmodel.synchronizationHelper.loadMasterUrlFromDB(true);

                            }
                            return errorCode;
                        }
                    }
                } catch (JSONException jsonExpection) {
                    Commons.print("" + jsonExpection.getMessage());
                }
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);

            if (aws) {
                updateDeleteTableStatus(errorCode);
            }
        /*    if (errorCode
                    .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                if (dayCloseCheckBox.isChecked()) {
                    new DeleteTables(true, true).execute();
                } else {
                    new DeleteTables(false, true).execute();
                }
            } else {
                new DeleteTables(false, false).execute();
                String errorMessage = bmodel.synchronizationHelper
                        .getErrormessageByErrorCode().get(errorCode);
                if (errorMessage != null) {
                    Toast.makeText(getActivity(), errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
                isClicked = false;
            }*/
        }
    }


    private void updateDeleteTableStatus(String errorCode) {
        if (errorCode
                .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
            if (dayCloseCheckBox.isChecked()) {
                new DeleteTables(true, true).execute();
            } else {
                new DeleteTables(false, true).execute();
            }
        } else {
            new DeleteTables(false, false).execute();
            String errorMessage = bmodel.synchronizationHelper
                    .getErrormessageByErrorCode().get(errorCode);
            if (errorMessage != null) {
                Toast.makeText(getActivity(), errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
            alertDialog.dismiss();
            isClicked = false;
        }
    }


    private String returnUrlDownloadResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator itr = jsonObject.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                    String errorCode = jsonObject.getString(key);
                    if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                        bmodel.synchronizationHelper
                                .parseJSONAndInsert(jsonObject, true);
                        bmodel.synchronizationHelper.loadMasterUrlFromDB(true);

                    }
                    return errorCode;
                }
            }
        } catch (JSONException jsonExpection) {
            Commons.print(jsonExpection.getMessage());
        }
        return "E01";
    }

    /**
     * After download all data send acknowledge to server using this class
     */
    public class UpdateFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            json = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        String errorCode = jsonObject.getString(key);
                        bmodel.configurationMasterHelper.isDistributorWiseDownload();
                        return errorCode;
                    }
                }
                return "1";
            } catch (Exception jsonException) {
                Commons.print("" + jsonException.getMessage());
            }
            return "1";

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextSyncMethod(next_method);

        }
    }

    /**
     * Distributore wise master will be downloaded if configuration enable.
     * This class is initiate distributor wise master download.we will send all
     * distributorid with userid and version code  to server.
     */
    class InitiateDistributorDownload extends AsyncTask<String, String, String> {
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (alertDialog != null) {
                    builder = new AlertDialog.Builder(getActivity());
                    customProgressDialog(builder, getResources().getString(R.string.loading));
                    alertDialog = builder.create();
                    alertDialog.show();
                }

                ArrayList<DistributorMasterBO> distributorList = bmodel.distributorMasterHelper.getDistributors();
                json = bmodel.synchronizationHelper.getCommonJsonObject();
                JSONArray jsonArray = new JSONArray();
                for (DistributorMasterBO distributorBO : distributorList) {
                    if (distributorBO.isChecked()) {
                        jsonArray.put(distributorBO.getDId());

                        //update distributorid in usermaster
                        bmodel.userMasterHelper.updateDistributorId(distributorBO.getDId(), distributorBO.getParentID(), distributorBO.getDName());
                    }
                }
                json.put("DistributorIds", jsonArray);
            } catch (Exception jsonException) {
                Commons.print("" + jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.INCREMENTAL_SYNC_INITIATE_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "0";
            } catch (JSONException jsonException) {
                Commons.print("" + jsonException.getMessage());
            }
            return "0";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode.equals("1")) {
                if (!aws) {
                    bmodel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                        @Override
                        public String onSuccess(String result) {
                            bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.SYNC, SynchronizationHelper.DownloadType.DISTRIBUTOR_WISE_DOWNLOAD);
                            return "";
                        }

                        @Override
                        public String onFailure(String errorresult) {
                            return "";
                        }
                    });
                } else {
                    downloadOnDemandMasterUrl(true);
                }
            }
        }
    }

    private void downloadOnDemandMasterUrl(boolean isDistributorWise) {

        bmodel.synchronizationHelper.loadMasterUrlFromDB(false);

        if (bmodel.synchronizationHelper.getUrlList().size() > 0) {
            if (isDistributorWise) {
                bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.SYNC, SynchronizationHelper.DownloadType.DISTRIBUTOR_WISE_DOWNLOAD);
            } else {
                bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.SYNC, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
            }
        } else {
            //on demand url not available
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextSyncMethod(next_method);
        }

    }

    /**
     * After download all distributore wise data send acknowledge to server using this class
     */
    class UpdateDistributorFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                json = bmodel.synchronizationHelper.getCommonJsonObject();
            } catch (Exception jsonException) {
                Commons.print("" + jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "1";
            } catch (Exception jsonException) {
                Commons.print("" + jsonException.getMessage());
            }
            return "1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextSyncMethod(next_method);

        }
    }

    /**
     * download stock from stockinhandmaster web api
     */
    class SihDownloadTask extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            json = bmodel.synchronizationHelper.getCommonJsonObject();

        }

        @Override
        protected String doInBackground(String... params) {
            String responseCode = "E01";
            String response = bmodel.synchronizationHelper.sendPostMethod(bmodel.synchronizationHelper.getSIHUrl(), json);
            try {

                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray(SynchronizationHelper.JSON_KEY);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject value = (JSONObject) jsonArray.get(i);
                    Iterator itr = value.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                            String errorCode = value.getString(key);
                            if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(value, true);

                            }
                            responseCode = errorCode;
                        }
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print("" + jsonExpection.getMessage());
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextSyncMethod(next_method);
        }
    }

    /**
     * After download all data from server using this method to  update data from temprorary table to
     * maping table and load data from sqlite and update in objects
     */
    class LoadData extends AsyncTask<String, String, SynchronizationHelper.NEXT_METHOD> {


        @Override
        protected SynchronizationHelper.NEXT_METHOD doInBackground(String... params) {
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            if (next_method == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE || next_method == SynchronizationHelper.NEXT_METHOD.DEFAULT) {
                final long startTime = System.nanoTime();
                bmodel.synchronizationHelper
                        .updateProductAndRetailerMaster();
                bmodel.synchronizationHelper.loadMethodsNew();
                long endTime = (System.nanoTime() - startTime) / 1000000;
                if (bmodel.synchronizationHelper.mTableList == null) {
                    bmodel.synchronizationHelper.mTableList = new HashMap<>();
                }
                bmodel.synchronizationHelper.mTableList.put("temp table update**", endTime + "");

            }
            return next_method;
        }

        @Override
        protected void onPostExecute(SynchronizationHelper.NEXT_METHOD response) {
            super.onPostExecute(response);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (getActivity().isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
            } else if (getActivity().isFinishing()) {
                return;
            }
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            bmodel.synchronizationHelper.isLastVisitTranDownloadDone = true;
            bmodel.synchronizationHelper.isSihDownloadDone = false;
            bmodel.synchronizationHelper.isDistributorDownloadDone = false;


            if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE) {

                if (bmodel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE) {
                    AzureConnectionHelper.getInstance().setAzureCredentials(getActivity());
                    try {
                        downloaderThread = new DownloaderThreadNew(getActivity(), activityHandler, bmodel.getDigitalContentURLS(),
                                bmodel.userMasterHelper.getUserMasterBO().getUserid(),
                                AzureConnectionHelper.getInstance().initializeAzureStorageConnection(), bmodel.getDigitalContentSFDCURLS());
                    } catch (Exception e) {
                        Commons.printException(e);
                        bmodel.showAlert(context.getString(R.string.error_message_general), 0);
                    }
                } else if (bmodel.configurationMasterHelper.IS_S3_CLOUD_STORAGE) {
                    AWSConnectionHelper.getInstance().setAmazonS3Credentials(getActivity());
                    transferUtility = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), getActivity());

                    downloaderThread = new DownloaderThreadNew(getActivity(),
                            activityHandler, bmodel.getDigitalContentURLS(),
                            bmodel.userMasterHelper.getUserMasterBO()
                                    .getUserid(), transferUtility, bmodel.getDigitalContentSFDCURLS());

                }
                downloaderThread.start();
            } else {
                if (isSwitchUser) {
                    if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                        new UploadSyncLog().execute();
                    else {
                        getActivity().finish();
                        moveToHomeScreenActivity();
                    }
                } else {
                    bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
                    if (bmodel.synchronizationHelper.checkDataForSyncLogUpload())
                        new UploadSyncLog().execute();
                }
                isClicked = false;
                // getActivity().finish();
            }
        }
    }

    /**
     * call the next method from given response
     *
     * @param response
     */
    private void callNextSyncMethod(SynchronizationHelper.NEXT_METHOD response) {
        if (response == SynchronizationHelper.NEXT_METHOD.DISTRIBUTOR_DOWNLOAD) {

            bmodel.distributorMasterHelper.downloadDistributorsList();
            if (bmodel.distributorMasterHelper.getDistributors().size() > 0) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

                Intent intent = new Intent(getActivity(), DistributorSelectionActivity.class);
                intent.putExtra("isFromLogin", false);
                startActivityForResult(intent, SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE);
            } else {
                //No distributors, so downloading on demand url without distributor selection.
                downloadOnDemandMasterUrl(false);
            }

        } else if (response == SynchronizationHelper.NEXT_METHOD.NON_DISTRIBUTOR_DOWNLOAD) {
            downloadOnDemandMasterUrl(false);
        } else if (response == SynchronizationHelper.NEXT_METHOD.SIH_DOWNLOAD) {
            new SihDownloadTask().execute();
        } else {
            new LoadData().execute();

        }
    }

    /**
     * Server error is coming like 404 error  and IsMantory is 1 for corresponding url delete
     * all table and show alert message to please redownload
     *
     * @param bundle
     */
    private void reDownloadAlert(Bundle bundle) {
        String errorDownlodCode = bundle
                .getString(SynchronizationHelper.ERROR_CODE);
        String errorDownloadMessage = bmodel.synchronizationHelper
                .getErrormessageByErrorCode().get(errorDownlodCode);
        if (errorDownloadMessage != null) {
            Toast.makeText(getActivity(), errorDownloadMessage,
                    Toast.LENGTH_SHORT).show();
        }
        alertDialog.dismiss();
        new DeleteTables(false, false).execute();
        bmodel.showAlert(getResources().getString(R.string.please_redownload_data), 5003);
        isClicked = false;
    }

    private void setMessageInProgressDialog(AlertDialog.Builder builder, String message) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_alert_dialog,
                (ViewGroup) getActivity().findViewById(R.id.layout_root));
        TextView messagetv = layout.findViewById(R.id.text);
        messagetv.setText(message);
        builder.setView(layout);
        builder.setCancelable(false);
    }


    @Override
    public void setUserNamePwd(String userName, String password) {
        this.userName = userName;
        this.password = password;
        isSwitchUser = true;

        if (NetworkUtils.isNetworkConnected(getActivity())) {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.auth_and_downloading_masters));
            alertDialog = builder.create();
            alertDialog.show();

            if (!DataMembers.SERVER_URL.equals("")) {
                callAuthentication(false);
            } else {
                bmodel.showAlert(getActivity().getResources().getString(R.string.download_url_empty), 0);
                if (alertDialog != null)
                    alertDialog.dismiss();
            }
        } else {
            bmodel.showAlert(getActivity().getResources().getString(R.string.please_connect_to_internet), 0);
        }


    }

    public void callAuthentication(boolean isDeviceChanged) {
        new Authentication(isDeviceChanged).execute();
    }

    class Authentication extends AsyncTask<String, String, String> {
        JSONObject jsonObject;
        final boolean changeDeviceId;

        Authentication(boolean changeDeviceId) {
            this.changeDeviceId = changeDeviceId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("LoginId", userName);
                jsonObj.put("Password", password);
                jsonObj.put(SynchronizationHelper.VERSION_CODE, bmodel.getApplicationVersionNumber());
                jsonObj.put(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());

                jsonObj.put("Model", Build.MODEL);
                jsonObj.put("Platform", "Android");
                jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
                jsonObj.put("FirmWare", "");
                jsonObj.put("DeviceId",
                        DeviceUtils.getIMEINumber(getActivity()));
                jsonObj.put("RegistrationId", bmodel.fcmRegistrationToken);
                jsonObj.put("DeviceUniqueId", DeviceUtils.getDeviceId(getActivity()));
                if (DataMembers.ACTIVATION_KEY != null && !DataMembers.ACTIVATION_KEY.isEmpty())
                    jsonObj.put("ActivationKey", DataMembers.ACTIVATION_KEY);
                jsonObj.put(SynchronizationHelper.MOBILE_DATE_TIME,
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME,
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                if (!DataMembers.backDate.isEmpty())
                    jsonObj.put(SynchronizationHelper.REQUEST_MOBILE_DATE_TIME,
                            DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                this.jsonObject = jsonObj;
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String LoginResponse = bmodel.synchronizationHelper.userInitialAuthenticate(jsonObject, changeDeviceId);
            try {
                JSONObject jsonObject = new JSONObject(LoginResponse);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, false);
                            bmodel.userMasterHelper.downloadUserDetails();
                            bmodel.userMasterHelper.downloadDistributionDetails();
                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
            if (alertDialog != null)
                alertDialog.dismiss();
            if (output.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                bmodel.getAppDataProvider().setUserName(userName);
                bmodel.getAppDataProvider().setUserPassword(password);
                new CheckNewVersionTask()
                        .execute(0);
            } else {
                if (output.equals("E27")) {
                    showDialog();
                } else if (output.equals("E28")) {
                    bmodel.showAlert(getActivity().getResources().
                            getString(R.string.user_already_assigned), 0);
                } else {
                    /*if (output.equals("E25")) {
                        loginView.showForgotPassword();
                    }*/

                    String ErrorMessage = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(output);

                    if (ErrorMessage != null) {
                        bmodel.showAlert(ErrorMessage, 0);
                    } else {
                        bmodel.showAlert("Connection Exception", 0);
                    }
                }


            }

        }
    }

    public void showDialog() {
        new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                getResources().getString(R.string.deviceId_change_msg_title),
                getResources().getString(R.string.deviceId_change_msg),
                false, getResources().getString(R.string.yes),
                getResources().getString(R.string.no),
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        callAuthentication(true);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {


            }
        }).show();
    }


    @Override
    public void showAttendanceNotCompletedToast() {
        bmodel.showAlert(
                getResources()
                        .getString(R.string.attendance_activity_not_completed), 0);
    }

    @Override
    public void showNoInternetToast() {
        bmodel.showAlert(
                getResources()
                        .getString(R.string.no_network_connection), 0);
    }

    @Override
    public void showOrderExistWithoutInvoice() {
        bmodel.showAlert(
                getResources().getString(
                        R.string.order_exist_without_invoice),
                0);
    }

    @Override
    public void showNoDataExist() {
        Toast.makeText(getActivity(),
                getResources().getString(R.string.no_data_exists),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgressLoading() {

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void showProgressUploading() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getResources().getString(R.string.uploading_data));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void cancelProgress() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void showRetailerSelectionScreen(List<SyncRetailerBO> isVisitedRetailerList) {
//        Intent intent = new Intent(getActivity(), SyncRetailerSelectActivity.class);
//        SyncVisitedRetailer catObj = new SyncVisitedRetailer(isVisitedRetailerList);
//        Bundle bun = new Bundle();
//        bun.putParcelable("list", catObj);
//        intent.putExtras(bun);
//        startActivityForResult(intent, 1);

        uploadPresenter.prepareSelectedRetailerIds();
        if (uploadPresenter.getVisitedRetailerId() != null
                && uploadPresenter.getVisitedRetailerId().toString().length() > 0) {
            isClicked = false;
            uploadPresenter.upload();
        } else {
            bmodel.showAlert(
                    getResources()
                            .getString(R.string.no_unsubmitted_orders), 0);
            isClicked = false;
            //dialog.dismiss();
        }
    }

    @Override
    public void showAlertImageUploadRecommended() {
        showAlertOkCancel(
                getResources()
                        .getString(
                                R.string.image_upload_recommended),
                ALERT.WITH_IMAGE_UPLOAD);
    }

    @Override
    public void showAlertNoUnSubmittedOrder() {
        bmodel.showAlert(
                getResources().getString(
                        R.string.no_unsubmitted_orders),
                0);
        if (withPhotosCheckBox.isChecked())
            withPhotosCheckBox.setChecked(false);
    }


    /**
     * Upload last download and upload time in UI
     */
    private void updateLastTransactionTimeInView() {
        try {
            TextView textView = view.findViewById(R.id.text_last_sync);
            textView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            if (!lastSyncTimeHelper.getLastDownloadTime().equals("")) {

                String download = getResources().getString(R.string.last_download_on) +
                        DateTimeUtils.convertFromServerDateToRequestedFormat(lastSyncTimeHelper.getLastDownloadDate(),
                                ConfigurationMasterHelper.outDateFormat)
                        + " " + lastSyncTimeHelper.getLastDownloadTime();

                String upload = "";
                if (!lastSyncTimeHelper.getLastUploadTime().equals("")) {
                    upload = getResources().getString(R.string.last_upload_on) +
                            DateTimeUtils.convertFromServerDateToRequestedFormat(lastSyncTimeHelper.getLastUplaodDate(),
                                    ConfigurationMasterHelper.outDateFormat)
                            + " " + lastSyncTimeHelper.getLastUploadTime();

                }
                String value = download + (upload.equals("") ? "" : "\n" + upload);
                textView.setText(value);
            } else {
                textView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    public void clearAppUrl() {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit();
        editor.putString("appUrlNew", "");
        editor.putString("application", "");
        editor.putString("activationKey", "");
        editor.commit();
    }

    class UploadSyncLog extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Uploading Sync Log Details");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            UploadHelper mUploadHelper = UploadHelper.getInstance(getActivity());
            mUploadHelper.uploadSyncLogDetails();
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            alertDialog.dismiss();
            if (isSwitchUser) {
                getActivity().finish();
                moveToHomeScreenActivity();
            }
        }
    }
}
