package com.ivy.sd.png.commons;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        try {
            if (bmodel.configurationMasterHelper.MVPTheme == 0) {
                super.setTheme(bmodel.configurationMasterHelper.getMVPTheme());
            } else {
                super.setTheme(bmodel.configurationMasterHelper.MVPTheme);
            }
            if (bmodel.configurationMasterHelper.fontSize.equals("")) {
                setFontStyle(bmodel.configurationMasterHelper.getFontSize());
            } else {
                setFontStyle(bmodel.configurationMasterHelper.fontSize);
            }
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
        mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mScreenTitleTV.setText(title);
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

    public void setFontStyle(String font) {
        if (font.equalsIgnoreCase("Small")) {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
        } else if (font.equalsIgnoreCase("Medium")) {
            getTheme().applyStyle(R.style.FontStyle_Medium, true);
        } else if (font.equalsIgnoreCase("Large")) {
            getTheme().applyStyle(R.style.FontStyle_Large, true);
        } else {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
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

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm.findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
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


}
