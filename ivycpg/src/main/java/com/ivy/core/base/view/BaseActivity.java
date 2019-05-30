package com.ivy.core.base.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity implements BaseIvyView {


    public static final int PHONE_STATE_AND_WRITE_PERMISSON = 1;
    public static final int CAMERA_AND_WRITE_PERMISSION = 2;
    public static final int LOCATION_PERMISSION = 3;
    private Unbinder mUnBinder;

    private NFCManager nfcManager;

    private Dialog dialog;

    private TextView progressMsgTxt;

    public static int mCurrentTheme = 0;


    /**
     * Always set you layout reference using this method
     *
     * @return
     */
    @LayoutRes
    public abstract int getLayoutId();


    /**
     * Initialize your view ids's or variables if needed. (Ex: ButterKnife)
     */
    protected abstract void initVariables();


    public abstract void initializeDi();

    private BasePresenter mBasePresenter;

    public void setBasePresenter(BasePresenter presenter) {
        mBasePresenter = presenter;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initializeDi();

        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);

        if (mCurrentTheme != 0) {

            if (!isPrevisit)
                setTheme(mCurrentTheme);
            else
                setTheme(preVisitTheme(mCurrentTheme));
            initScreen();
        } else
            showLoading();

    }

    private void initScreen() {

        this.setContentView(this.getLayoutId());
        mUnBinder = ButterKnife.bind(this);

        checkAndRequestPermissionAtRunTime(PHONE_STATE_AND_WRITE_PERMISSON);

        setUpDefaults();

        getMessageFromAliens();

        initVariables();
        setUpViews();
        hideLoading();
    }

    private void setUpDefaults() {
        AppUtils.useNetworkProvidedValues(this);
    }

    @Override
    public void createNFCManager() {
    }

    @Override
    public void resumeNFCManager() {
        if (nfcManager == null) {
            nfcManager = new NFCManager(this);
            nfcManager.onActivityCreate();
        }

        nfcManager.onActivityResume();
    }

    @Override
    public void pauseNFCManager() {
        if (nfcManager != null) {
            nfcManager.onActivityPause();
        }
    }


    public boolean checkAndRequestPermissionAtRunTime(int mGroup) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int permissionStatus;
        if (mGroup == PHONE_STATE_AND_WRITE_PERMISSON) {
            permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }


            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PHONE_STATE_AND_WRITE_PERMISSON);
                return false;
            }
        } else if (mGroup == CAMERA_AND_WRITE_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), CAMERA_AND_WRITE_PERMISSION);
                return false;
            }
        } else if (mGroup == LOCATION_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                if (mBasePresenter.isLocationConfigurationEnabled()) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), LOCATION_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean temp = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //If Deny previously
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    temp = true;
                } else {// If Check Never again and Deny Previously
                    String permissionName = "";
                    switch (permissions[i]) {
                        case Manifest.permission.READ_PHONE_STATE:
                            permissionName = getString(R.string.permission_phone);
                            break;
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                            permissionName = getString(R.string.permission_storage);
                            break;
                        case Manifest.permission.CAMERA:
                            permissionName = getString(R.string.permission_camera);
                            break;
                        case Manifest.permission.ACCESS_FINE_LOCATION:
                            permissionName = getString(R.string.permission_location);
                            break;
                    }

                    Toast.makeText(this, getResources().getString(R.string.permission_enable_msg) +
                            " " + permissionName, Toast.LENGTH_LONG).show();
                }
            }
        }
        if (temp) {
            checkAndRequestPermissionAtRunTime(requestCode);
        }
    }

    @Override
    public void showLoading() {
        showDialog(getString(R.string.loading));
    }

    @Override
    public void showLoading(String message) {
        showDialog(message);

    }


    @Override
    public void showLoading(int strinRes) {
        showDialog(getString(strinRes));
    }


    @Override
    public void hideLoading() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void onError(int resId) {
        onError(getString(resId));
    }


    @Override
    public void handleLayoutDirection(String language) {
        /*Local Configuration Change Language and layout direction */
        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(language)) {
            locale = new Locale(language.substring(0, CAMERA_AND_WRITE_PERMISSION));
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            /*Below code used to change the layout direction */
            setLayoutDirection(locale.getLanguage().equalsIgnoreCase("ar") ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        }
    }

    /**
     * Changes layout direction
     *
     * @param direction 0 or 1
     *                  View.LAYOUT_DIRECTION_RTL 1
     *                  View.LAYOUT_DIRECTION_LTR 0
     */
    @Override
    public void setLayoutDirection(int direction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.getWindow().getDecorView().setLayoutDirection(direction);
        }
    }

    @Override
    public void onError(String message) {
        showSnackBar(message != null ? message : getString(R.string.error));
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

    @Override
    public void showMessage(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }

    /**
     * To validateData if network is connected
     *
     * @return true if Connected. False if not connected
     */

    @Override
    public boolean isNetworkConnected() {
        return NetworkUtils.isNetworkConnected(this);
    }

    @Override
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Set the unBinder object from butter knife so that the unbinding can be
     * taken care from the base activity on destroy
     *
     * @param unBinder unBinder of the ButterKnife
     */
    public void setUnBinder(Unbinder unBinder) {
        mUnBinder = unBinder;
    }

    @Override
    protected void onDestroy() {

        if (mUnBinder != null) {
            mUnBinder.unbind();
        }

        super.onDestroy();
    }


    /**
     * Abstract method which can be used to get the data
     * via intent for other activities
     */
    protected abstract void getMessageFromAliens();

    /**
     * Set up the views.
     */
    protected abstract void setUpViews();


    public void addFragment(int containerViewId, Fragment fragment, boolean addStack,
                            boolean isReplace, int animationType) {

        if (fragment == null) {
            return;
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String tag = fragment.getClass().toString();


        if (isReplace) {
            for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                fragmentManager.popBackStack();
            }
            fragmentManager.executePendingTransactions();
        }


        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            if (!isReplace) {
                Fragment currentFragment;
                fragmentTransaction.add(containerViewId, fragment, tag);
                if ((currentFragment = getSupportFragmentManager().findFragmentById(containerViewId)) != null) {
                    fragmentTransaction.hide(currentFragment);
                }
            } else {
                fragmentTransaction.replace(containerViewId, fragment, tag);
            }
        }

        if (addStack) {
            fragmentTransaction.addToBackStack(tag);
        } else {
            fragmentManager.popBackStack(tag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }


    public void setOrientation(boolean isLandscape) {
        setRequestedOrientation(isLandscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    @Override
    public void setScreenTitle(String title) {
        this.screenTitle = title;
        TextView mScreenTitleTV = findViewById(R.id.tv_toolbar_title);
        mScreenTitleTV.setText(title);
    }

    @Override
    public void setUpToolbar(String title) {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        if (title != null)
            setScreenTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void showDialog(String msg) {

        if (dialog == null) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_alert_dialog);

            TextView title = dialog.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            progressMsgTxt = dialog.findViewById(R.id.text);
        }
        progressMsgTxt.setText(msg);

        dialog.show();

    }

    private String screenTitle;

    public String getScreenTitle() {
        return screenTitle;
    }

    @Override
    public void setBlueTheme() {
        mCurrentTheme = R.style.MVPTheme_Blue;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setPinkTheme() {
        mCurrentTheme = R.style.MVPTheme_Green;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setGreenTheme() {
        mCurrentTheme = R.style.MVPTheme_Green;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setNavyBlueTheme() {
        mCurrentTheme = R.style.MVPTheme_NBlue;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setOrangeTheme() {
        mCurrentTheme = R.style.MVPTheme_Orange;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setRedTheme() {
        mCurrentTheme = R.style.MVPTheme_Red;
        boolean isPrevisit = getIntent().getBooleanExtra("PreVisit",false);
        if (!isPrevisit)
            setTheme(mCurrentTheme);
        else
            setTheme(preVisitTheme(mCurrentTheme));
        initScreen();
    }

    @Override
    public void setFontSize(String fontSize) {
    }


    @Override
    public void showAlert(String title, String msg) {
        showAlert(title, msg, null);
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {
        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, boolean isCancelable) {

        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener, isCancelable);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener) {
        CommonDialog dialog = new CommonDialog(this, title, msg, getResources().getString(R.string.ok), positiveClickListener, getString(R.string.cancel), negativeOnClickListener);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
    }

    public void startActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);

    }

    public void startActivityAndFinish(Class activity) {
        startActivity(activity);
        finish();

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

    private int preVisitTheme(int styleId){
        switch (styleId){
            case R.style.MVPTheme_Blue:
                styleId = R.style.MVPTheme_Blue_disable;
                return styleId;
            case R.style.MVPTheme_NBlue:
                styleId = R.style.MVPTheme_NBlue_disable;
                return styleId;
            case R.style.MVPTheme_Green:
                styleId = R.style.MVPTheme_Green_disable;
                return styleId;
            case R.style.MVPTheme_Red:
                styleId = R.style.MVPTheme_Red;
                return styleId;
            case R.style.MVPTheme_Orange:
                styleId = R.style.MVPTheme_Orange_disable;
                return styleId;
            default:
                return styleId;
        }
    }
}


