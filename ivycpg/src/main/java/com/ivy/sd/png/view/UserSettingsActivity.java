package com.ivy.sd.png.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//import android.support.v7.widget.Toolbar;

public class UserSettingsActivity extends PreferenceActivity {

    private BusinessModel bmodel;
    private SharedPreferences settings;
    private static ProgressDialog progressDialog;
    private static Context context;
    Preference mpmac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.settings_layout);

            addPreferencesFromResource(R.xml.preference);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            context = this;

            settings = getSharedPreferences(bmodel.PREFS_NAME, MODE_PRIVATE);


            getActionBar().setIcon(android.R.color.transparent);
            getActionBar().setDisplayHomeAsUpEnabled(true);

            getActionBar().setDisplayShowHomeEnabled(false);

            getActionBar().setTitle(getResources().getString(R.string.settings));


            getActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));


            /*Load Languages from StdListMaster : ListType - LANGUAGE_TYPE
               By Default, English will be listed from xml
             */
            ArrayList<ConfigureBO> list = bmodel.configurationMasterHelper
                    .getLanguageList();

            if (list != null && list.size() > 0) {
                List<CharSequence> tmpEntryLst = new ArrayList<>();
                List<CharSequence> tmpEntryvaluesLst = new ArrayList<>();
                ListPreference listPreference = (ListPreference) findPreference("languagePref");

                for (int i = 0; i < list.size(); i++) {
                    tmpEntryLst.add(list.get(i).getMenuName());
                    tmpEntryvaluesLst.add(list.get(i).getConfigCode());
                }
                CharSequence[] entries = new CharSequence[tmpEntryLst.size()];
                CharSequence[] entryValues = new CharSequence[tmpEntryvaluesLst.size()];
                entries = tmpEntryLst.toArray(entries);
                entryValues = tmpEntryvaluesLst.toArray(entryValues);
                listPreference.setEntries(entries);
                listPreference.setEntryValues(entryValues);
                listPreference.getEditor().commit();
            }

            //Printer Settings
            ArrayList<ConfigureBO> printerList = bmodel.configurationMasterHelper
                    .getPrinterList();

            ListPreference listPreference = (ListPreference) findPreference("PrinterPref");
            if (printerList != null && printerList.size() > 0) {
                List<CharSequence> tmpEntryLst = new ArrayList<>();
                List<CharSequence> tmpEntryvaluesLst = new ArrayList<>();


                for (int i = 0; i < printerList.size(); i++) {
                    tmpEntryLst.add(printerList.get(i).getMenuName());
                    tmpEntryvaluesLst.add(printerList.get(i).getConfigCode());
                }
                CharSequence[] entries = new CharSequence[tmpEntryLst.size()];
                CharSequence[] entryValues = new CharSequence[tmpEntryvaluesLst.size()];
                entries = tmpEntryLst.toArray(entries);
                entryValues = tmpEntryvaluesLst.toArray(entryValues);
                listPreference.setEntries(entries);
                listPreference.setEntryValues(entryValues);
                listPreference.getEditor().commit();

                if (listPreference.getValue() == null) {
                    listPreference.setValueIndex(0);
                }
            } else {
                listPreference.setEnabled(false);
            }


            mpmac = findPreference("pmac");
            mpmac.setSummary(settings.getString("MAC", ""));
            mpmac.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {

                    showDialog(1);
                    return true;
                }
            });

            /**
             * Export Options - App Log, All Log and DB
             * Export File will be stored in amazon
             */

            Preference export_file = findPreference("export_file");
            export_file.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {

                    progressDialog = ProgressDialog.show(UserSettingsActivity.this, "Export", "File is exporting...");
                    new MyThread(UserSettingsActivity.this,
                            DataMembers.UPLOAD_FILE_IN_AMAZON).start();


                    return true;
                }
            });

            if (!bmodel.userMasterHelper.getSyncStatus()) {
                export_file.setEnabled(false);
            }

            Preference clear_data = findPreference("clear_data");
            PreferenceCategory category = (PreferenceCategory) findPreference("clear");
            if (!bmodel.configurationMasterHelper.IS_CLEAR_DATA) {
                getPreferenceScreen().removePreference(category);
            }
            clear_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showDialog(2);
                    return true;
                }
            });

            boolean isFromLogin=false;
            if(getIntent().getExtras()!=null){
                isFromLogin=getIntent().getExtras().getBoolean("isFromLogin");
            }

            Preference change_password = findPreference("change_password");
            PreferenceCategory pswCategory = (PreferenceCategory) findPreference("psw");
            if (isFromLogin || !LoginHelper.getInstance(this).SHOW_CHANGE_PASSWORD) {
                //Change password option not needed before login screen
                getPreferenceScreen().removePreference(pswCategory);
            }
            change_password.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(context, ChangePasswordActivity.class);
                    i.putExtra("isExpired", false);
                    i.putExtra("isFromSetting",true);
                    startActivity(i);
                    return true;
                }
            });

            Preference device_status = findPreference("device_status");
            PreferenceCategory dsCategory = (PreferenceCategory) findPreference("ds");
            if (!bmodel.configurationMasterHelper.SHOW_DEVICE_STATUS) {
                getPreferenceScreen().removePreference(dsCategory);
            }
            device_status.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(context, DeviceStatusActivity.class);
                    startActivity(i);
                    return true;
                }
            });

            bmodel.userMasterHelper.downloadDistributionDetails();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case 1:
                return createmacDialog();
            case 2:
                if (bmodel.synchronizationHelper.checkDataForSync()) {
                    return clearSyncDialog();
                } else {
                    return clearDialog();
                }
            default:
                return null;
        }
    }


    private Dialog createmacDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        View view = View.inflate(this, R.layout.settings_printer_dialog, null);
        dialog.setContentView(view);
        //dialog.setContentView(R.layout.settings_printer_dialog);

        TextView textView9 = (TextView) view.findViewById(R.id.textView9);
        textView9.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView9.setText("Enter MAC Address");
        TextView uname = (TextView) view.findViewById(R.id.uname);
        uname.setVisibility(View.GONE);
        final EditText eText = (EditText) view.findViewById(R.id.edit_username);
        eText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        eText.setHint("MAC Address");

        eText.setInputType(InputType.TYPE_CLASS_TEXT);
        eText.setText(settings.getString("MAC", ""));

        EditText edit_password = (EditText) view.findViewById(R.id.edit_password);
        edit_password.setVisibility(View.GONE);
        Button mDoneBTN = (Button) view.findViewById(R.id.btn_done);
        mDoneBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        Button mCancelBTN = (Button) view.findViewById(R.id.btn_cancel);
        mCancelBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mDoneBTN.setText("Ok");
        //eText.setBackgroundResource(android.R.drawable.edit_text);
//        TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
//        eText.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));
        mCancelBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        mDoneBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String value = eText.getText().toString();
                // Writing data to SharedPreferences
                Editor editor = settings.edit();
                editor.putString("MAC", value);


                editor.apply();
                mpmac.setSummary(settings.getString("MAC", ""));
                eText.setText(settings.getString("MAC", ""));
                dialog.dismiss();
            }
        });


        dialog.show();


        return null;
    }

    public android.os.Handler getHandler() {
        return handler;
    }

    private static final android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case DataMembers.NOTIFY_FILE_UPLOADED__COMPLETED_IN_AMAZON:
                    progressDialog.dismiss();
                    Toast.makeText(context, "Exported",
                            Toast.LENGTH_LONG).show();
                    break;
                case DataMembers.NOTIFY_FILE_UPLOADED_FAILED_IN_AMAZON:
                    progressDialog.dismiss();
                    Toast.makeText(context, "Failed",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }

    };

    private Dialog clearDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.switch_user));
        builder.setMessage(getResources().getString(R.string.proceed));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                clearPreferences();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        bmodel.applyAlertDialogTheme(builder);
        return null;
    }

    private Dialog clearSyncDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.switch_user));
        builder.setMessage(getResources().getString(R.string.data_to_upload));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        bmodel.applyAlertDialogTheme(builder);
        return null;
    }

    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + getApplicationContext().getPackageName() + " HERE");
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0) {
            return 0;
        }
        final TypedValue typedvalueattr = new TypedValue();

        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public int getColorByName(String name) {
        int colorId = 0;

        try {
            Class res = R.color.class;
            Field field = res.getField(name);
            colorId = field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return colorId;
    }


    public boolean isValidFragment(String fragmentName) {
        return false;
    }
}