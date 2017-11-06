package com.ivy.sd.png.view;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThread;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.OrderSplitHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.LabelsKey;

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
import java.util.Vector;

public class SynchronizationFragment extends IvyBaseFragment implements View.OnClickListener {

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private static BusinessModel bmodel;
    private EditText txtUserName, txtPassword;
    private TextView tvwstatus;
    private CheckBox dayCloseCheckBox, withPhotosCheckBox, selectedRetailerDownloadCheckBox;
    private static Button btn = null;

    // instance variables
    private Thread downloaderThread;
    private ProgressDialog progressDialog;
    private boolean isClicked = false;
    private List<SyncRetailerBO> isVisitedRetailerList;
    private boolean Checked = false;
    private SyncronizationReceiver mSyncReceiver;

    private static final int UPLOAD_ALL = 0;
    private static final int RETAILER_WISE_UPLOAD = 1;
    private static final int UPLOAD_WITH_IMAGES = 2;
    private static final int UPLOAD_STOCK_IN_HAND = 3;
    private static final int UPLOAD_STOCK_APPLY = 4;
    private static final int UPLOAD_LOYALTY_POINTS = 6;
    private static final int UPLOAD_CS_SIH = 7;
    private static final int UPLOAD_CS_STOCK_APPLY = 8;
    private static final int UPLOAD_CS_REJECTED_VARIANCE = 9;
    SharedPreferences mLastSyncSharedPref;
    private NonVisitReasonDialog nvrd;

    TransferUtility transferUtility;
    AmazonS3Client s3;
    private View view;
    private DisplayMetrics displaymetrics;
    private Button sync, download, backDateSelection;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initializeItem() {
        TextView tvtitle = (TextView) view.findViewById(R.id.synctitle);
        tvtitle.setText(getArguments().getString("screentitle"));
        CardView alert_card = (CardView) view.findViewById(R.id.alert_card);
        if (!bmodel.labelsMasterHelper.getSyncContentHTML().equals("NULL") && !bmodel.labelsMasterHelper.getSyncContentHTML().equals("")) {
            alert_card.setVisibility(View.VISIBLE);
            TextView alert_txt = (TextView) view.findViewById(R.id.alert_txt);
            alert_txt.setText(Html.fromHtml(bmodel.labelsMasterHelper.getSyncContentHTML()));
        } else {
            alert_card.setVisibility(View.GONE);
        }

        txtUserName = (EditText) view.findViewById(R.id.username);
        txtPassword = (EditText) view.findViewById(R.id.password);
        selectedRetailerDownloadCheckBox = (CheckBox) view.findViewById(R.id.download_retailer);
        withPhotosCheckBox = (CheckBox) view.findViewById(R.id.withPhotos);
        withPhotosCheckBox.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        selectedRetailerDownloadCheckBox.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        if (bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES) {
            withPhotosCheckBox.setVisibility(View.VISIBLE);

            int dbImageCount = bmodel.synchronizationHelper
                    .countImageFiles();
            if (dbImageCount >= bmodel.configurationMasterHelper.photocount) {

                withPhotosCheckBox.setChecked(true);
                Checked = true;
            }

        }
        withPhotosCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        int dbImageCount = bmodel.synchronizationHelper
                                .countImageFiles();
                        if (!isChecked) {

                            if (dbImageCount >= bmodel.configurationMasterHelper.photocount) {
                                showAlertOk(
                                        getResources()
                                                .getString(
                                                        R.string.its_not_possible_to_upload_without_img),
                                        0);
                                withPhotosCheckBox.setChecked(true);
                                Checked = true;
                            } else
                                Checked = false;

                        } else {
                            Checked = true;

                        }
                        syncStatus(1);

                    }
                });

        dayCloseCheckBox = (CheckBox) view.findViewById(R.id.dayClose);
        if (bmodel.configurationMasterHelper.SHOW_SYNC_DAYCLOSE) {
            dayCloseCheckBox.setVisibility(View.VISIBLE);
        } else {
            dayCloseCheckBox.setVisibility(View.GONE);
        }

        dayCloseCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            if (SDUtil.compareDate(SDUtil.now(SDUtil.DATE_GLOBAL),
                                    bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                                    "yyyy/MM/dd") >= 0) {
                                if (!bmodel.synchronizationHelper.isDayClosed()) {
                                    Vector<NonproductivereasonBO> nonProductiveRetailersVector = bmodel.getMissedCallRetailers();
                                    if ((nonProductiveRetailersVector.size() != 0 && bmodel.configurationMasterHelper.HAS_NO_VISIT_REASON_VALIDATION)) {
                                        dayCloseCheckBox.setChecked(false);
                                        nvrd = new NonVisitReasonDialog(getActivity(), nonProductiveRetailersVector);
                                        nvrd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                if (bmodel.getMissedCallRetailers().size() == 0) {
                                                    dayCloseCheckBox.setChecked(true);
                                                }
                                            }
                                        });

                                        nvrd.show();
                                        DisplayMetrics displaymetrics = new DisplayMetrics();
                                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                        Window window = nvrd.getWindow();
                                        lp.copyFrom(window.getAttributes());
                                        lp.width = displaymetrics.widthPixels - 50;
                                        lp.height = (int) (displaymetrics.heightPixels / 1.1);
                                        window.setAttributes(lp);
                                        syncStatus(1);

                                    } else {
                                        try {
                                            if (bmodel.configurationMasterHelper.SHOW_CLOSE_DAY_VALID) {
                                                if (bmodel.configurationMasterHelper
                                                        .isOdaMeterOn()
                                                        && !bmodel.endjourneyclicked) {
                                                    bmodel.showAlert(
                                                            getResources()
                                                                    .getString(
                                                                            R.string.journey_not_ended),
                                                            0);
                                                    dayCloseCheckBox.setChecked(false);
                                                    syncStatus(1);

                                                } else {
                                                    showAlertOk(
                                                            getResources()
                                                                    .getString(
                                                                            R.string.you_are_closing)
                                                                    + " "
                                                                    + SDUtil.today()
                                                                    + " "
                                                                    + "("
                                                                    + DateUtil
                                                                    .convertFromServerDateToRequestedFormat(
                                                                            SDUtil.now(SDUtil.DATE_GLOBAL),
                                                                            ConfigurationMasterHelper.outDateFormat)
                                                                    + ")" + ".", 0);
                                                }
                                            } else {
                                                showAlertOk(
                                                        getResources()
                                                                .getString(
                                                                        R.string.you_are_closing)
                                                                + " "
                                                                + SDUtil.today()
                                                                + " "
                                                                + "("
                                                                + DateUtil
                                                                .convertFromServerDateToRequestedFormat(
                                                                        SDUtil.now(SDUtil.DATE_GLOBAL),
                                                                        ConfigurationMasterHelper.outDateFormat)
                                                                + ")" + ".", 0);
                                            }
                                        } catch (Exception e) {
                                            showAlertOk(
                                                    getResources().getString(
                                                            R.string.you_are_closing)
                                                            + " today "
                                                            + "("
                                                            + DateUtil
                                                            .convertFromServerDateToRequestedFormat(
                                                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                                                    ConfigurationMasterHelper.outDateFormat)
                                                            + ")" + ".", 0);
                                        }

                                    }

                                }
                            } else {
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.download_date_mismatch),
                                        Toast.LENGTH_SHORT).show();
                                dayCloseCheckBox.setChecked(false);

                            }
                        }
                        syncStatus(1);
                    }
                });
        if (bmodel.configurationMasterHelper.IS_RTR_WISE_DOWNLOAD) {
            selectedRetailerDownloadCheckBox.setVisibility(View.VISIBLE);
        }


        tvwstatus = (TextView) view.findViewById(R.id.status);

        sync = (Button) view.findViewById(R.id.startsync);
        sync.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        sync.setOnClickListener(this);


        download = (Button) view.findViewById(R.id.download);
        download.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        download.setOnClickListener(this);


        backDateSelection = (Button) view.findViewById(R.id.downloaddate);
        if (bmodel.configurationMasterHelper.IS_ENABLE_BACKDATE_REPORTING) {
            backDateSelection.setVisibility(View.VISIBLE);
        }

        backDateSelection.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        if (DataMembers.backDate.isEmpty()) {
            backDateSelection.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                    .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
        } else {
            backDateSelection.setText(SDUtil.now(SDUtil.DATE_GLOBAL_EIPHEN));
        }
        backDateSelection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn = backDateSelection;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker1");
            }
        });

        Button close = (Button) view.findViewById(R.id.syncButtonBack);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                getActivity().finish();
            }
        });

        txtUserName.setText(bmodel.userMasterHelper.getUserMasterBO()
                .getLoginName());

        TextView closeDay_tv = (TextView) view.findViewById(R.id.close_day_tv);
        closeDay_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView close_date = (TextView) view.findViewById(R.id.closingDay);
        close_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        close_date.setText(DateUtil
                .convertFromServerDateToRequestedFormat(
                        bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                        ConfigurationMasterHelper.outDateFormat));//changed bcz close_date shows current date, replaced to show downloaded date

        Button gprsAvailablityButton = (Button) view.findViewById(R.id.gprsAvailablityButton);
        if (isOnline())
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

        syncStatus(2);
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 1 || s.toString().equals("")) {
                    syncStatus(2);
                } else {
                    syncStatus(1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void syncStatus(int btn_count) {
        TypedArray type_arr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        int text_color = type_arr.getColor(R.styleable.MyTextView_textColor, 0);
        int background_color = type_arr.getColor(R.styleable.MyTextView_buttonBackground, 0);
        if (txtPassword.getText().toString().length() > 0) {
            if (btn_count == 1) {
                if (bmodel.synchronizationHelper.checkDataForSync() || withPhotosCheckBox.isChecked() || dayCloseCheckBox.isChecked()
                        && (bmodel.synchronizationHelper
                        .countImageFiles() > 0) ? true : (dayCloseCheckBox.isChecked() ? true : false)) {
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
        setDayCloseEnableDisable();
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

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

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
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
                    DataMembers.backDate = (DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), ConfigurationMasterHelper.outDateFormat));
                } else {
                    DataMembers.backDate = (DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));


                    if (DateUtil.convertDateObjectToRequestedFormat(selectedDate.getTime(), "MM/dd/yyyy").equals(Utils.getDate()))
                        DataMembers.backDate = "";
                    else
                        DataMembers.backDate = DateUtil.convertDateObjectToRequestedFormat(selectedDate.getTime(), "MM/dd/yyyy");

                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getActivity()).edit();
                    editor.putString("backDate", DataMembers.backDate);
                    editor.commit();
                }
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sync, menu);

        MenuItem menuItem = menu.getItem(0);

        if (bmodel.isOnline()) {
            menuItem.setIcon(R.drawable.greenball);
        } else {
            menuItem.setIcon(R.drawable.redball);
        }
        if (!bmodel.configurationMasterHelper.SHOW_SYNC_EXPORT_TXT)
            menu.findItem(R.id.menu_export_txt).setVisible(false);

        if (!bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
            menu.findItem(R.id.menu_catalog_img).setVisible(false);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_gps_available) {
            if (bmodel.isOnline()) {
                item.setIcon(R.drawable.greenball);
                Toast.makeText(getActivity(), getResources().getString(R.string.network_connectionC_Avail),
                        Toast.LENGTH_SHORT).show();
            } else {
                item.setIcon(R.drawable.redball);
                Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection),
                        Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.menu_export_txt) {
            getActivity().supportInvalidateOptionsMenu();
            if (!isClicked)
                isClicked = true;
            showAlertOkCancel(
                    getResources().getString(
                            R.string.do_u_want_to_close_the_day), 1);

        } else if (i == R.id.menu_catalog_img) {
            startActivity(new Intent(getActivity(), CatalogImagesDownlaod.class));

        } else if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            getActivity().finish();

        }
        return true;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            isClicked = false;
            setDayCloseEnableDisable();
            SyncDownloadStatusDialog sdsd;
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window;
            switch (msg.what) {

                case DataMembers.NOTIFY_DATABASE_NOT_SAVED:
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(R.string.unable_to_back_up_db),
                            Toast.LENGTH_SHORT).show();
                    break;

                case DataMembers.NOTIFY_UPDATE:
                    builder = new AlertDialog.Builder(getActivity());
                    bmodel.setMessageInProgressDialog(alertDialog, builder, getActivity(), msg.obj.toString());
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
                    bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
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

                case DataMembers.NOTIFY_SIH_UPLOADED:
                    alertDialog.dismiss();
                    if (bmodel.synchronizationHelper.checkStockTable())
                        startSync(UPLOAD_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                        startSync(UPLOAD_CS_SIH);
                    else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                        startSync(UPLOAD_CS_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                        startSync(UPLOAD_CS_REJECTED_VARIANCE);
                    else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else
                        startSync(UPLOAD_ALL);
                    break;
                case DataMembers.NOTIFY_SIH_UPLOAD_ERROR:
                    Commons.print("SIH ," + "Error");
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;

                case DataMembers.NOTIFY_STOCKAPLY_UPLOADED:
                    alertDialog.dismiss();
                    if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                        startSync(UPLOAD_CS_SIH);
                    else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                        startSync(UPLOAD_CS_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                        startSync(UPLOAD_CS_REJECTED_VARIANCE);
                    else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else {
                        startSync(UPLOAD_ALL);
                    }
                    break;
                case DataMembers.NOTIFY_STOCKAPLY_UPLOAD_ERROR:
                    Commons.print("Stock Apply Upload," + "Error");
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;
                case DataMembers.NOTIFY_COUNTER_SIH_UPLOADED:
                    alertDialog.dismiss();
                    if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                        startSync(UPLOAD_CS_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                        startSync(UPLOAD_CS_REJECTED_VARIANCE);
                    else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else {
                        startSync(UPLOAD_ALL);
                    }
                    break;
                case DataMembers.NOTIFY_COUNTER_STOCK_APPLY_UPLOADED:
                    alertDialog.dismiss();
                    if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                        startSync(UPLOAD_CS_REJECTED_VARIANCE);
                    else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else
                        startSync(UPLOAD_ALL);
                    break;
                case DataMembers.NOTIFY_CS_REJECTED_VARIANCE_UPLOADED:
                    alertDialog.dismiss();
                    if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else
                        startSync(UPLOAD_ALL);
                    break;
                case DataMembers.NOTIFY_LP_UPLOADED:
                    alertDialog.dismiss();
                    if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                            && !dayCloseCheckBox.isChecked()) {
                        startSync(RETAILER_WISE_UPLOAD);
                    } else {
                        startSync(UPLOAD_ALL);
                    }
                    break;
                case DataMembers.NOTIFY_LP_UPLOAD_ERROR:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;

                case DataMembers.NOTIFY_UPLOADED:
                    if ((withPhotosCheckBox.isChecked() || !bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES)
                            && bmodel.synchronizationHelper.countImageFiles() > 0) {
                        String s1 = tvwstatus.getText()
                                + DataMembers.CR1
                                + getResources().getString(
                                R.string.data_upload_completed_sucessfully);
                        tvwstatus.setText(s1);
                        builder = new AlertDialog.Builder(getActivity());
                        bmodel.setMessageInProgressDialog(alertDialog, builder, getActivity(), getResources().getString(
                                R.string.image_uploading));
                        alertDialog.show();
                        if (bmodel.configurationMasterHelper.ISAMAZON_IMGUPLOAD) {
                            new MyThread(getActivity(),
                                    DataMembers.AMAZONIMAGE_UPLOAD).start();

                        } else {
                            new MyThread(getActivity(),
                                    DataMembers.SYNCUPLOAD_IMAGE).start();

                        }

                    } else {
                        alertDialog.dismiss();
                        updateLastSync();
                        tvwstatus.setText(getResources().getString(
                                R.string.data_upload_completed_sucessfully));
                        displaymetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        sdsd = new SyncDownloadStatusDialog(getActivity(), getResources().getString(
                                R.string.data_upload_completed_sucessfully), displaymetrics);
                        sdsd.show();
                    }
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    break;
                case DataMembers.NOTIFY_UPLOADED_IMAGE:
                    if (bmodel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT)
                        new LoadRetailerIsVisited().execute();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.images_sucessfully_uploaded), 0);
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
                    bmodel.photocount = 0;
                    alertDialog.dismiss();
                    //bmodel.showAlert(getResources().getString(R.string.successfully_uploaded), 0);
                    displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    sdsd = new SyncDownloadStatusDialog(getActivity(), getResources().getString(
                            R.string.successfully_uploaded), displaymetrics);
                    sdsd.show();
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
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (v.getId() == R.id.startsync) {

            if (bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword.getText().toString())) {
                if (bmodel.synchronizationHelper.isAttendanceCompleted()) {
                    if (bmodel.synchronizationHelper.isSaleDrafted()) {
                        if (bmodel.isOnline()) {
                            if (!bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.isOrderExistToCreateInvoiceAll()) {

                                if (dayCloseCheckBox.isChecked()) {
                                    if (!isClicked) {
                                        showAlertOkCancel(
                                                getResources()
                                                        .getString(
                                                                R.string.do_u_want_to_close_the_day),
                                                0);
                                    }

                                } else {
                                    if (!isClicked) {
                                        isClicked = true;
                                        if (bmodel.synchronizationHelper.checkDataForSync() || bmodel.synchronizationHelper.checkSIHTable()
                                                || bmodel.synchronizationHelper.checkStockTable()) {

                                            if (bmodel.configurationMasterHelper.SHOW_ORDER_PROCESS_DIALOG)
                                                showDialogForOrderProcessing();
                                            else if (bmodel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT) {
                                                new LoadRetailerIsVisited().execute();
                                            } else {
                                                IsImagechecked();
                                            }

                                        } else if ((withPhotosCheckBox.isChecked() || !bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES) && bmodel.synchronizationHelper.countImageFiles() > 0) {
                                            startSync(UPLOAD_WITH_IMAGES);
                                        } else {
                                            isClicked = false;
                                            bmodel.showAlert(getResources().getString(R.string.no_unsubmitted_orders), 0);
                                        }
                                    }
                                }

                            } else {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.order_exist_without_invoice),
                                        0);
                            }

                        } else {
                            bmodel.showAlert(
                                    getResources()
                                            .getString(R.string.no_network_connection), 0);
                        }
                    } else {
                        bmodel.showAlert(
                                getResources()
                                        .getString(R.string.drafted_sales_not_processed), 0);
                    }
                } else {
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.attendance_activity_not_completed), 0);
                }

            } else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.password_does_not_match), 0);
            }

        } else if (v.getId() == R.id.download) {
            if (bmodel.isOnline()) {
                if (bmodel.synchronizationHelper.checkDataForSync()) {

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
                } else {
                    if (!selectedRetailerDownloadCheckBox.isChecked()) {
                        if (bmodel.configurationMasterHelper.SHOW_DOWNLOAD_ALERT)
                            showAlertForDownload();
                        else {

                            if (!bmodel.isAutoUpdateAvailable()) {

                                if (bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword
                                        .getText().toString())) {

                                    if (!isClicked) {
                                        isClicked = true;
                                        new CheckNewVersionTask()
                                                .execute(new Integer[]{0});
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
                        if (bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword
                                .getText().toString())) {

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

    }


    private void callSyncRetailerDialog() {
        if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                && !dayCloseCheckBox.isChecked()) {

            Intent intent = new Intent(getActivity(), SyncRetailerSelectActivity.class);
            SyncVisitedRetailer catObj = new SyncVisitedRetailer(isVisitedRetailerList);
            Bundle bun = new Bundle();
            bun.putParcelable("list", catObj);
            intent.putExtras(bun);
            startActivityForResult(intent, 1);
            //startActivity(intent);

        } else if (bmodel.synchronizationHelper.checkSIHTable())
            startSync(UPLOAD_STOCK_IN_HAND);
        else if (bmodel.synchronizationHelper.checkStockTable())
            startSync(UPLOAD_STOCK_APPLY);
        else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
            startSync(UPLOAD_CS_SIH);
        else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
            startSync(UPLOAD_CS_STOCK_APPLY);
        else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
            startSync(UPLOAD_CS_REJECTED_VARIANCE);
        else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
            startSync(UPLOAD_LOYALTY_POINTS);
        else if (bmodel.synchronizationHelper.checkDataForSync()) {
            startSync(UPLOAD_ALL);
        } else {
            isClicked = false;
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_data_exists),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {

                    if (bmodel.configurationMasterHelper.SHOW_ORDER_PROCESS_DIALOG)
                        showDialogForOrderProcessing();
                    else {
                        if (bmodel.synchronizationHelper.checkStockTable())
                            startSync(UPLOAD_STOCK_IN_HAND);
                        else if (bmodel.synchronizationHelper.checkStockTable())
                            startSync(UPLOAD_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                            startSync(UPLOAD_CS_SIH);
                        else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                            startSync(UPLOAD_CS_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                            startSync(UPLOAD_CS_REJECTED_VARIANCE);
                        else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                            startSync(UPLOAD_LOYALTY_POINTS);
                        else
                            startSync(UPLOAD_ALL);
                    }
                }

            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    bmodel.synchronizationHelper.setRetailerIds(new StringBuilder());
                    for (SyncRetailerBO sbo : isVisitedRetailerList) {
                        if (sbo.isChecked())
                            bmodel.synchronizationHelper.getRetailerIds().append(
                                    bmodel.QT(sbo.getRetailerId()));
                        bmodel.synchronizationHelper.getRetailerIds().append(",");
                    }
                    if (bmodel.synchronizationHelper.getRetailerIds() != null && bmodel.synchronizationHelper.getRetailerIds().toString().length() > 0) {
                        bmodel.synchronizationHelper.getRetailerIds().delete(bmodel.synchronizationHelper.getRetailerIds().length() - 1, bmodel.synchronizationHelper.getRetailerIds().length());
                        isClicked = false;
                        //dialog.dismiss();
                        if (bmodel.synchronizationHelper.checkSIHTable())
                            startSync(UPLOAD_STOCK_IN_HAND);
                        else if (bmodel.synchronizationHelper.checkStockTable())
                            startSync(UPLOAD_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                            startSync(UPLOAD_CS_SIH);
                        else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                            startSync(UPLOAD_CS_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                            startSync(UPLOAD_CS_REJECTED_VARIANCE);
                        else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                            startSync(UPLOAD_LOYALTY_POINTS);
                        else
                            startSync(RETAILER_WISE_UPLOAD);
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

            case SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    new InitiateDistributorDownload().execute();
                }
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
                            downloaderThread = new DownloaderThread(
                                    getActivity(), activityHandler, bmodel
                                    .getUpdateURL(), false,
                                    DownloaderThread.APK_DOWNLOAD);
                            downloaderThread.start();
                        }
                    }

                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void showAlertOkCancel(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (idd == 0) {
                            // To do extra
                            bmodel.synchronizationHelper.closeDay(1);

                            if (bmodel.deliveryManagementHelper.isDeliveryModuleAvailable()) {
                                bmodel.deliveryManagementHelper.updateNotDeliveryDetails();
                            }

                            bmodel.mEmptyReconciliationhelper.updateTable();
                            if (bmodel.configurationMasterHelper.CALCULATE_UNLOAD) {
                                bmodel.vanunloadmodulehelper
                                        .vanUnloadAutomatically();
                            }
                            if (bmodel.synchronizationHelper.checkDataForSync()
                                    || (withPhotosCheckBox.isChecked() && bmodel.synchronizationHelper
                                    .countImageFiles() > 0)) {
                                isClicked = true;
                                if (bmodel.configurationMasterHelper.SHOW_ORDER_PROCESS_DIALOG)
                                    showDialogForOrderProcessing();
                                else {
                                    if (bmodel.synchronizationHelper.checkSIHTable())
                                        startSync(3);
                                    else if (bmodel.synchronizationHelper.checkStockTable())
                                        startSync(4);
                                    else
                                        startSync(0);
                                }

                            } else {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.no_unsubmitted_orders),
                                        0);
                            }
                        } else if (idd == 1) {
                            if (bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword
                                    .getText().toString())) {
                                if (!bmodel.isOrderExistToCreateInvoiceAll()
                                        || !bmodel.configurationMasterHelper.IS_INVOICE) {
                                    if (bmodel.synchronizationHelper
                                            .checkDataForSync()) {
                                        if (dayCloseCheckBox.isChecked()) {
                                            if (!isOnline()) {
                                                bmodel.synchronizationHelper.closeDay(1);
                                                startSync(5);
                                            } else {
                                                isClicked = false;
                                                bmodel.showAlert(
                                                        getResources()
                                                                .getString(
                                                                        R.string.you_are_online_cant_export),
                                                        0);
                                            }
                                        } else {
                                            isClicked = false;
                                            bmodel.showAlert(
                                                    getResources()
                                                            .getString(
                                                                    R.string.select_close_for_the_day),
                                                    0);
                                        }
                                    } else {
                                        isClicked = false;
                                        bmodel.showAlert(
                                                getResources()
                                                        .getString(
                                                                R.string.no_unsubmitted_orders),
                                                0);
                                    }
                                } else {
                                    isClicked = false;
                                    bmodel.showAlert(
                                            getResources()
                                                    .getString(
                                                            R.string.order_exist_without_invoice),
                                            0);
                                }
                            } else {
                                isClicked = false;
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.password_does_not_match),
                                        0);
                            }
                        } else if (idd == 3) {
                            isClicked = false;
                            withPhotosCheckBox.setChecked(true);
                            if (bmodel.synchronizationHelper.checkSIHTable())
                                startSync(3);
                            else if (bmodel.synchronizationHelper.checkStockTable())
                                startSync(4);
                            else
                                startSync(0);
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isClicked = false;
                        if (idd == 3) {
                            if (bmodel.synchronizationHelper.checkSIHTable())
                                startSync(3);
                            else if (bmodel.synchronizationHelper.checkStockTable())
                                startSync(4);
                            else
                                startSync(0);
                        }
                    }
                });


        bmodel.applyAlertDialogTheme(builder);
    }

    /**
     * This is the Handler for this activity. It will receive messages from the
     * apk DownloaderThread and make the necessary updates to the UI.
     */
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
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this,
                                DataMembers.MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
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
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this,
                                DataMembers.MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                    }
                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_COMPLETE: 1. Remove the progress bar
			 * from the screen. 2. Display Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    // Here Code to call dwnloaded apk.

                    if (msg.arg1 == DownloaderThread.APK_DOWNLOAD) {
                        bmodel.deleteAllValues();
                        bmodel.activationHelper.clearAppUrl();
                        bmodel.userMasterHelper.getUserMasterBO().setUserid(0);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(
                                    Uri.fromFile(new File(getActivity().
                                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + DataMembers.fileName)),
                                    "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    } else {

                        startActivity(new Intent(getActivity(),
                                HomeScreenActivity.class));
                        getActivity().finish();
                    }

                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_CANCELLED: 1. Interrupt the downloader
			 * thread. 2. Remove the progress bar from the screen. 3. Display
			 * Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_CANCELED:
                    dismissCurrentProgressDialog();
                    clearAmazonDownload();
                    displayMessage(getString(R.string.user_message_download_canceled));

                    getActivity().finish();
                    BusinessModel.loadActivity(getActivity(),
                            DataMembers.actHomeScreen);

                    break;

			/*
             * Handling MESSAGE_ENCOUNTERED_ERROR: 1. Check the obj field of the
			 * message for the actual error message that will be displayed to
			 * the user. 2. Remove any progress bars from the screen. 3. Display
			 * a Toast with the error message.
			 */
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }

                    if (bmodel.isDigitalContentAvailable()) {
                        bmodel.configurationMasterHelper.setAmazonS3Credentials();
                        initializeTransferUtility();
                        downloaderThread = new DownloaderThreadNew(getActivity(),
                                activityHandler, bmodel.getDigitalContentURLS(),
                                bmodel.userMasterHelper.getUserMasterBO()
                                        .getUserid(), transferUtility);
                        downloaderThread.start();
                    }

                    if (msg.arg1 == DownloaderThread.ZIP_DOWNLOAD) {
                        getActivity().finish();
                        BusinessModel.loadActivity(getActivity(),
                                DataMembers.actHomeScreen);
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
                    bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
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
                if (bmodel.isOnline()) {
                    bmodel.synchronizationHelper.updateAuthenticateToken();
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

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.checking_new_version));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
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

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mSyncReceiver);
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

    public void startSync(int callFlag) {
        Commons.print(" callFlag : " + callFlag);
        if (bmodel.mAttendanceHelper.checkMenuInOut())
            bmodel.mAttendanceHelper.updateAttendaceDetailInTime();
        builder = new AlertDialog.Builder(getActivity());
        bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.uploading_data));

        alertDialog = builder.create();
        alertDialog.show();

        if (callFlag == UPLOAD_ALL)
            new MyThread(getActivity(), DataMembers.SYNCUPLOAD).start();
        else if (callFlag == RETAILER_WISE_UPLOAD)
            new MyThread(getActivity(), DataMembers.SYNCUPLOADRETAILERWISE).start();
        else if (callFlag == UPLOAD_WITH_IMAGES) {
            if (bmodel.configurationMasterHelper.ISAMAZON_IMGUPLOAD) {
                new MyThread(getActivity(),
                        DataMembers.AMAZONIMAGE_UPLOAD).start();
            } else {
                new MyThread(getActivity(), DataMembers.SYNCUPLOAD_IMAGE)
                        .start();
            }
        } else if (callFlag == UPLOAD_STOCK_IN_HAND)
            new MyThread(getActivity(), DataMembers.SYNCSIHUPLOAD).start();
        else if (callFlag == UPLOAD_STOCK_APPLY)
            new MyThread(getActivity(), DataMembers.SYNCSTKAPPLYUPLOAD).start();
        else if (callFlag == 5)
            new MyThread(getActivity(), DataMembers.SYNC_EXPORT).start();
        else if (callFlag == UPLOAD_LOYALTY_POINTS)
            new MyThread(getActivity(), DataMembers.SYNCLYTYPTUPLOAD).start();
        else if (callFlag == UPLOAD_CS_SIH)
            new MyThread(getActivity(), DataMembers.COUNTER_SIH_UPLOAD).start();
        else if (callFlag == UPLOAD_CS_STOCK_APPLY)
            new MyThread(getActivity(), DataMembers.COUNTER_STOCK_APPLY_UPLOAD).start();
        else if (callFlag == UPLOAD_CS_REJECTED_VARIANCE)
            new MyThread(getActivity(), DataMembers.CS_REJECTED_VARIANCE_UPLOAD).start();
    }

    DialogForOrderProcessing dialogForOrderProcessing = null;

    public void showDialogForOrderProcessing() {
        bmodel.orderSplitHelper = OrderSplitHelper.clearInstance();
        bmodel.orderSplitHelper = OrderSplitHelper.getInstance(bmodel);
        bmodel.orderSplitHelper.loadOrderSplitMasterBOListFromDBForSync();

        this.dialogForOrderProcessing = new DialogForOrderProcessing(getActivity());
        settOnclickListenerForDialogForOrderProcessing();

        this.dialogForOrderProcessing.show();
    }

    public void dismissDialogForOrderProcessing() {
        if ((this.dialogForOrderProcessing != null)
                && (this.dialogForOrderProcessing.isShowing())) {
            this.dialogForOrderProcessing.dismiss();
        }
    }

    public void settOnclickListenerForDialogForOrderProcessing() {
        class ButtonOnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {


                if (bmodel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT
                        && !dayCloseCheckBox.isChecked()) {
                    new LoadRetailerIsVisited().execute();
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PROCESS_DIALOG) { // Removed
                    // &&
                    // !dayCloseCheckBox.isChecked()
                    if (isOnline()) { // since
                        // dayCloseCheckBox.isChecked()
                        // same
                        // functionality
                        bmodel.orderSplitHelper.updateOrderProcessing();
                        dismissDialogForOrderProcessing();
                        if (bmodel.synchronizationHelper.checkSIHTable())
                            startSync(UPLOAD_STOCK_IN_HAND);
                        else if (bmodel.synchronizationHelper.checkStockTable())
                            startSync(UPLOAD_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                            startSync(UPLOAD_CS_SIH);
                        else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                            startSync(UPLOAD_CS_STOCK_APPLY);
                        else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                            startSync(UPLOAD_CS_REJECTED_VARIANCE);
                        else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                            startSync(UPLOAD_LOYALTY_POINTS);
                        else
                            startSync(UPLOAD_ALL);
                    } else {
                        dismissDialogForOrderProcessing();
                        bmodel.showAlert(
                                getResources()
                                        .getString(R.string.no_network_connection), 0);
                        isClicked = false;
                    }
                } else {
                    if (bmodel.synchronizationHelper.checkSIHTable())
                        startSync(UPLOAD_STOCK_IN_HAND);
                    else if (bmodel.synchronizationHelper.checkStockTable())
                        startSync(UPLOAD_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                        startSync(UPLOAD_CS_SIH);
                    else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                        startSync(UPLOAD_CS_STOCK_APPLY);
                    else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                        startSync(UPLOAD_CS_REJECTED_VARIANCE);
                    else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                        startSync(UPLOAD_LOYALTY_POINTS);
                    else
                        startSync(UPLOAD_ALL);
                }
            }

        }

        dialogForOrderProcessing
                .setButtonClickListener(new ButtonOnClickListener());
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
                            if (bmodel.synchronizationHelper.validateUser(txtUserName.getText().toString(), txtPassword
                                    .getText().toString())) {

                                if (!isClicked) {
                                    isClicked = true;
                                    new CheckNewVersionTask()
                                            .execute(new Integer[]{0});
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

    class LoadRetailerIsVisited extends AsyncTask<Integer, Integer, Boolean> {

        private ProgressDialog progressDialogue;

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.loading), true, false);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                isVisitedRetailerList = bmodel.synchronizationHelper.getRetailerIsVisited();
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            callSyncRetailerDialog();
            progressDialogue.dismiss();

        }

    }

    public void IsImagechecked() {
        int dbImageCount = bmodel.synchronizationHelper.countImageFiles();
        if (!Checked) {
            if (bmodel.configurationMasterHelper.photocount >= 10 && (((double) dbImageCount / bmodel.configurationMasterHelper.photocount) * 100) >= bmodel.configurationMasterHelper.photopercent) {
                showAlertOkCancel(
                        getResources()
                                .getString(
                                        R.string.image_upload_recommended),
                        3);
            } else {
                if (bmodel.synchronizationHelper.checkSIHTable())
                    startSync(UPLOAD_STOCK_IN_HAND);
                else if (bmodel.synchronizationHelper.checkStockTable())
                    startSync(UPLOAD_STOCK_APPLY);
                else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                    startSync(UPLOAD_CS_SIH);
                else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                    startSync(UPLOAD_CS_STOCK_APPLY);
                else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                    startSync(UPLOAD_CS_REJECTED_VARIANCE);
                else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                    startSync(UPLOAD_LOYALTY_POINTS);
                else
                    startSync(UPLOAD_ALL);
            }
        } else {
            if (bmodel.synchronizationHelper.checkSIHTable())
                startSync(UPLOAD_STOCK_IN_HAND);
            else if (bmodel.synchronizationHelper.checkStockTable())
                startSync(UPLOAD_STOCK_APPLY);
            else if (bmodel.CS_StockApplyHelper.isCounterSIHDataToUpload())
                startSync(UPLOAD_CS_SIH);
            else if (bmodel.CS_StockApplyHelper.isCounterStockApplyDataToUpload())
                startSync(UPLOAD_CS_STOCK_APPLY);
            else if (bmodel.CS_StockApplyHelper.isCSRejectedVarianceStatus())
                startSync(UPLOAD_CS_REJECTED_VARIANCE);
            else if (bmodel.synchronizationHelper.checkLoyaltyPoints())
                startSync(UPLOAD_LOYALTY_POINTS);
            else
                startSync(UPLOAD_ALL);
        }

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
                if (errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {

                    bmodel.updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                ConfigurationMasterHelper.outDateFormat));
                        edt.putString("time", SDUtil.now(SDUtil.TIME));
                        edt.apply();
                    }
                } else if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    //outelet Performac
                    if (bmodel.reportHelper.getPerformRptUrl().length() > 0) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(getActivity())
                                .edit();
                        editor.putString("rpt_dwntime",
                                SDUtil.now(SDUtil.DATE_TIME_NEW));
                        editor.commit();
                    }
                    new UpdateFinish().execute();
                } else {
                    reDownloadAlert(bundle);
                }

                break;

            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {

                    bmodel.updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                ConfigurationMasterHelper.outDateFormat));
                        edt.putString("time", SDUtil.now(SDUtil.TIME));
                        edt.apply();
                    }
                } else if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
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
        syncStatus(1);
    }

    private void updateLastSync() {
        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        edt.putString("time", SDUtil.now(SDUtil.TIME));
        edt.apply();
    }

    private void initializeTransferUtility() {
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        s3 = new AmazonS3Client(myCredentials);
        transferUtility = new TransferUtility(s3, getActivity());
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
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.URLDOWNLOAD_MASTER_APPEND_URL, jsonObject);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
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
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode
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
            }
        }
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
                    bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.loading));
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
                downloadOnDemandMasterUrl(true);
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
            String response = bmodel.synchronizationHelper.sendPostMethod(bmodel.synchronizationHelper.getSIHUrl(), json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, true);

                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print("" + jsonExpection.getMessage());
            }
            return "E01";
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
                bmodel.synchronizationHelper.mTableList.put("temp table update**", endTime + "");

            }
            return next_method;
        }

        @Override
        protected void onPostExecute(SynchronizationHelper.NEXT_METHOD response) {
            super.onPostExecute(response);
            alertDialog.dismiss();
            bmodel.synchronizationHelper.isLastVisitTranDownloadDone = true;
            bmodel.synchronizationHelper.isSihDownloadDone = false;
            bmodel.synchronizationHelper.isDistributorDownloadDone = false;


            if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE) {
                bmodel.configurationMasterHelper.setAmazonS3Credentials();
                initializeTransferUtility();
                downloaderThread = new DownloaderThreadNew(getActivity(),
                        activityHandler, bmodel.getDigitalContentURLS(),
                        bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid(), transferUtility);
                downloaderThread.start();
            } else {
                bmodel.showAlert(getResources().getString(R.string.downloaded_successfully), 8);
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


}
