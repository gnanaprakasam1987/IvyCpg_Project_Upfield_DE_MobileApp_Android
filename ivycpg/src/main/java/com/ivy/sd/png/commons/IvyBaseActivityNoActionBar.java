package com.ivy.sd.png.commons;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.services.common.CommonUtils;

public class IvyBaseActivityNoActionBar extends AppCompatActivity implements
        ApplicationConfigs {

    private TextView mScreenTitleTV;
    private BusinessModel bmodel;
    private NFCManager nfcManager;
    private HashMap<String, String> listPermissionsNeededGroupName;
    TextView messagetv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!BuildConfig.DEBUG) {
            if (DeviceUtils.isEmulator()) {
                Toast.makeText(this, getResources().getString(R.string.this_is_not_a_real_device), Toast.LENGTH_LONG).show();
                finish();
            }
            if (CommonUtils.isRooted(this)) {
                Toast.makeText(this, getResources().getString(R.string.app_will_not_work_in_rooted_device), Toast.LENGTH_LONG).show();
                finish();
            }
        }



        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        try {

            boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);

            int styleId ;

            if (bmodel.configurationMasterHelper.MVPTheme == 0)
                styleId = R.style.MVPTheme_Blue;
            else
                styleId = bmodel.configurationMasterHelper.MVPTheme;

            if (isPrevisit){

                switch (styleId){
                    case R.style.MVPTheme_Blue:
                        styleId = R.style.MVPTheme_Blue_disable;
                        break;
                    case R.style.MVPTheme_NBlue:
                        styleId = R.style.MVPTheme_NBlue_disable;
                        break;
                    case R.style.MVPTheme_Green:
                        styleId = R.style.MVPTheme_Green_disable;
                        break;
                    case R.style.MVPTheme_Red:
                        styleId = R.style.MVPTheme_Red;
                        break;
                    case R.style.MVPTheme_Orange:
                        styleId = R.style.MVPTheme_Orange_disable;
                        break;
                    default:
                        break;
                }
            }

            BaseActivity.mCurrentTheme = styleId;
            super.setTheme(styleId);

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        /*Local Configuration Change Language and layout direction */
        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(sharedPrefs.getString("languagePref", LANGUAGE))) {
            locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            /*Below code used to change the layout direction */
            if (locale.getLanguage().equalsIgnoreCase("ar"))
                layoutDirection(View.LAYOUT_DIRECTION_RTL);
            else
                layoutDirection(View.LAYOUT_DIRECTION_LTR);

        }

        preparePermissionGroupName();

        checkAndRequestPermissionAtRunTime(1);

        if (bmodel.configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER) {
            nfcManager = new NFCManager(this);
            nfcManager.onActivityCreate();
        }
        bmodel.useNetworkProvidedValues();
    }

    /*This is method is used to change the layout direction
     params 0  or 1
     View.LAYOUT_DIRECTION_RTL 1
     View.LAYOUT_DIRECTION_LTR 0
     */
    private void layoutDirection(int view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            IvyBaseActivityNoActionBar.this.getWindow().getDecorView().setLayoutDirection(view);
        }
    }

    private void preparePermissionGroupName() {
        listPermissionsNeededGroupName = new HashMap<>();
        listPermissionsNeededGroupName.put(Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_phone));
        listPermissionsNeededGroupName.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.permission_storage));
        listPermissionsNeededGroupName.put(Manifest.permission.CAMERA, getResources().getString(R.string.permission_camera));
        listPermissionsNeededGroupName.put(Manifest.permission.ACCESS_FINE_LOCATION, getResources().getString(R.string.permission_location));
        listPermissionsNeededGroupName.put(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.record_audio));
    }

    public void updateTheme(){

    }

    @Override
    protected void onResume() {
        super.onResume();


        if (bmodel.configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER) {
            if (nfcManager != null)
                nfcManager.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bmodel.configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER) {
            if (nfcManager != null)
                nfcManager.onActivityPause();
        }

    }

    public void setScreenTitle(String title) {
        mScreenTitleTV = (TextView) findViewById(R.id.tv_toolbar_title);
       // mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mScreenTitleTV.setText(title);
    }

    public void setUpToolbar(String title){
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        if (title != null)
            setScreenTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public String getScreenTitle() {
        return mScreenTitleTV.getText().toString();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();
    }

    public boolean checkAndRequestPermissionAtRunTime(int mGroup) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int permissionStatus;
        if (mGroup == 1) {
            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.READ_PHONE_STATE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }


            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(IvyBaseActivityNoActionBar.this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        1);
                return false;
            }
        } else if (mGroup == 2) {
            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.CAMERA);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(IvyBaseActivityNoActionBar.this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        2);
                return false;
            }
        } else if (mGroup == 3) {
            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                if (bmodel.configurationMasterHelper.checkLocationConfiguration()) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(IvyBaseActivityNoActionBar.this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        3);
                return false;
            }
        }
        else if (mGroup == 4) {
            permissionStatus = ContextCompat.checkSelfPermission(IvyBaseActivityNoActionBar.this,
                    Manifest.permission.RECORD_AUDIO);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                if (bmodel.configurationMasterHelper.checkLocationConfiguration()) {
                    listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(IvyBaseActivityNoActionBar.this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        3);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean temp = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //If Deny previously
                if (ActivityCompat.shouldShowRequestPermissionRationale(IvyBaseActivityNoActionBar.this, permissions[i])) {
                    temp = true;
                    //checkAndRequestPermissionAtRunTime(requestCode);
                } else {// If Check Never again and Deny Previously
                    Toast.makeText(IvyBaseActivityNoActionBar.this, getResources().getString(R.string.permission_enable_msg) +
                            " " + listPermissionsNeededGroupName.get(permissions[i]), Toast.LENGTH_LONG).show();
                }
            }
        }
        if (temp) {
            checkAndRequestPermissionAtRunTime(requestCode);
        }
    }


    public void customProgressDialog(AlertDialog.Builder builder, String message) {

        try {
            View view = View.inflate(this, R.layout.custom_alert_dialog, null);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) view.findViewById(R.id.text);
            messagetv.setText(message);

            builder.setView(view);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void updaterProgressMsg(String msg) {
        if (messagetv != null)
            messagetv.setText(msg);
    }

    public void showMessage(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    public void loadFiveFilterFragment(Bundle bundle, int resId) {
        try {

            FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm.findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm.beginTransaction();
            if (frag != null)
                ft.detach(frag);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<Object>();
            fragobj.setArguments(bundle);

            ft.replace(resId, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void startActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public void startActivityAndFinish(Class activity) {
        startActivity(activity);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
    protected static final int REQUEST_CHECK_SETTINGS = 1000;
    GoogleApiClient googleApiClient;
    private static final int UPDATE_INTERVAL = 1000 * 2;
    private static final int FASTEST_INTERVAL = 1000;

    public void requestLocation(final Activity ctxt) {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).build();
        }
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            status.startResolutionForResult(
                                    ctxt, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void showAlert(String title, String msg) {
        showAlert(title, msg, null);
    }

    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {
        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener);
        dialog.setCancelable(false);
        dialog.show();
    }



    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, boolean isCancelable) {

        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener, isCancelable);
        dialog.setCancelable(isCancelable);
        dialog.show();
    }

    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener) {
        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener, getString(R.string.cancel), negativeOnClickListener);
        dialog.setCancelable(false);
        dialog.show();
    }


    public void clearAppUrl() {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit();
        editor.putString("appUrlNew", "");
        editor.putString("application", "");
        editor.putString("activationKey", "");
        editor.commit();
    }


    public AlertDialog applyAlertDialogTheme(Context context, AlertDialog.Builder builder) {
        TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        AlertDialog dialog = builder.show();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        int screenWidth = (int) (metrics.widthPixels * 0.80);
        dialog.getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        int alertTitleId = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView alertTitle = dialog.getWindow().getDecorView().findViewById(alertTitleId);
        alertTitle.setTextColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0)); // change title text color

        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        negativeBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        Button postiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        postiveBtn.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        postiveBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        // Set title divider color
        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));

        return dialog;
    }
}
