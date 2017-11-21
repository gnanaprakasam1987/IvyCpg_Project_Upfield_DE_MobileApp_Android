package com.ivy.sd.png.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ActivationHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import me.relex.circleindicator.CircleIndicator;

public class ScreenActivationFragment extends IvyBaseFragment implements
        View.OnClickListener {
    private BusinessModel bmodel;
    private SharedPreferences appPreferences;
    private ImageButton activate;
    private EditText activationKey;
    private String appUrl;
    ActivationDialog activation;
    private View view;
    private TextView version;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Creating App short cut in Home screen during first visit
        appPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        view = inflater.inflate(R.layout.fragment_screen_activation, container, false);

        //hideToolBr();
        version = (TextView) view.findViewById(R.id.version);
        version.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        version.setText(getResources().getString(R.string.version)
                + bmodel.getApplicationVersionName());
        activate = (ImageButton) view.findViewById(R.id.activate);
        activate.setOnClickListener(this);
        activationKey = (EditText) view.findViewById(R.id.activationKey);

        bmodel.useNetworkProvidedValues();

        //Toast.makeText(getActivity(),R.string.please_tap_refresh_if_device_is_already_activated,Toast.LENGTH_LONG).show();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //getActivity().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multi_color_background));

        final ViewPager vpPager = (ViewPager) view.findViewById(R.id.pager);
        final CircleIndicator indicator = (CircleIndicator) view.findViewById(R.id.indicator);
        ViewPagerAdapter adapterViewPager = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        indicator.setViewPager(vpPager);

        TextView tv_activated = (TextView) view.findViewById(R.id.tv_already_activated);
        tv_activated.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_activated.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    String imei = bmodel.activationHelper.getIMEINumber();
                    if (!imei.matches("[0]+")) {
                        new DoActivation().execute(5);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.telephony_not_avail), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.permission_enable_msg) +
                            " " + getResources().getString(R.string.permission_phone), Toast.LENGTH_LONG).show();
                }
            }
        });
        /*view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                view.getWindowVisibleDisplayFrame(r);

                int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 500) { // if more than 100 pixels, its probably a keyboard...
                    if (Build.VERSION.SDK_INT < 16) {
                        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        View decorView = getActivity().getWindow().getDecorView();
                        // Hide the status bar.
                        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                        *//*version.setVisibility(View.GONE);
                        indicator.setVisibility(View.GONE);
                        vpPager.setVisibility(View.GONE);*//*
                    }
                }else{
                    version.setVisibility(View.VISIBLE);
                    indicator.setVisibility(View.VISIBLE);
                    vpPager.setVisibility(View.VISIBLE);
                }
            }
        });*/
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        ImageButton bt = (ImageButton) v;
        if (bt == activate) {
            String imei = bmodel.activationHelper.getIMEINumber();
            if (!imei.matches("[0]+")) {
                activateOnClick();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.telephony_not_avail), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void activateOnClick() {
        try {

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                if (activationKey.getText().toString().equals("")) {
                    bmodel.showAlert(
                            getResources().getString(R.string.enter_activation_id),
                            0);
                } else if (activationKey.getText().toString().length() != 16) {
//                    Toast.makeText(getActivity(),
//                            R.string.activation_key_should_be_sixteen_character,
//                            Toast.LENGTH_LONG).show();
                    bmodel.showAlert(
                            getResources().getString(R.string.activation_key_should_be_sixteen_character),
                            0);
                } else {
                    bmodel.activationHelper.activationKey = (activationKey
                            .getText().toString());
                    // Below code for checking license key based on download
                    new DoActivation().execute(0);
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.permission_enable_msg) +
                        " " + getResources().getString(R.string.permission_phone), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * This class responsible for call activation method and process postback
     * result
     *
     * @author vinodh.r
     */
    class DoActivation extends AsyncTask<Integer, Integer, Integer> {

        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(
                    ScreenActivationActivity.this, DataMembers.SD,
					getResources().getString(R.string.please_wait_some_time),
					true, false);*/

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder,  getResources().getString(R.string.please_wait_some_time));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int status = ActivationHelper.NOTIFY_SUCESSFULLY_ACTIVATED;
            try {
                if (bmodel.isOnline())
                    if (params[0] == 0)
                        status = bmodel.activationHelper.doActivationAtHttp();
                    else if (params[0] == 1) {
                        if (bmodel.activationHelper
                                .check200Status(bmodel.activationHelper
                                        .getSERVER_URL()))
                            status = ActivationHelper.NOTIFY_VALID_URL;
                        else
                            status = ActivationHelper.NOTIFY_NOT_VALID_URL;
                    } else if (params[0] == 2)
                        status = ActivationHelper.NOTIFY_URL_EMPTY;
                    else if (params[0] == 3) {
                        if (bmodel.activationHelper
                                .check200Status(bmodel.activationHelper
                                        .getSERVER_URL()))
                            status = ActivationHelper.NOTIFY_ACTIVATION_LIST_SINGLE_EXTEND;
                        else
                            status = ActivationHelper.NOTIFY_NOT_VALID_URL;
                    } else if (params[0] == 4) {
                        if (bmodel.activationHelper
                                .check200Status(bmodel.activationHelper
                                        .getSERVER_URL()))
                            status = ActivationHelper.NOTIFY_SUCESSFULLY_ACTIVATED_EXTENDED;
                        else
                            status = ActivationHelper.NOTIFY_NOT_VALID_URL;
                    } else
                        status = bmodel.activationHelper
                                .doIMEIActivationAtHttp();
                else
                    status = ActivationHelper.NOTIFY_CONNECTION_PROBLEM;
            } catch (Exception e) {
                Commons.printException(e);
            }
            return status; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer status) {
            // result is the value returned from doInBackground

            // NOTIFY_ALREADY_NOT_ACTIVATED = 0;
            // NOTIFY_ALREADY_ACTIVATED = 1;
            // NOTIFY_CONNECTIVITY_ERROR = -1;
            // NOTIFY_SERVER_ERROR = 2;
            // NOTIFY_CONNECTION_PROBLEM = 3;
            // NOTIFY_SUCESSFULLY_ACTIVATED = 4;
            // NOTIFY_INVALID_KEY = 5;
            // NOTIFY_ACTIVATION_FAILED = 6;
            // NOTIFY_ACTIVATION_LIST = 7;
            // NOTIFY_ACTIVATION_LIST_SINGLE = 8;
            // NOTIFY_ACTIVATION_LIST_NULL = 9;
            // NOTIFY_URL_EMPTY = 10;
            // NOTIFY_NOT_VALID_URL = 11;
            // NOTIFY_VALID_URL = 12;

            //	progressDialogue.dismiss();
            alertDialog.dismiss();
            switch (status) {
                case ActivationHelper.NOTIFY_SUCESSFULLY_ACTIVATED:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    appUrl = appPreferences.getString("appUrlNew", "");
                    bmodel.activationHelper.setSERVER_URL(appUrl);
                    new DoActivation().execute(4);
                    break;
                case ActivationHelper.NOTIFY_SUCESSFULLY_ACTIVATED_EXTENDED:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.successfully_activated),
                            DataMembers.NOTIFY_ACTIVATION_TO_LOGIN);
                    break;
                case ActivationHelper.NOTIFY_CONNECTIVITY_ERROR:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.communication_error_please_try_again),
                            0);
                    break;
                case ActivationHelper.NOTIFY_INVALID_KEY:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.invalid_key_try_with_valid_key), 0);
                    break;
                case ActivationHelper.NOTIFY_ACTIVATION_FAILED:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(R.string.activation_failed), 0);
                    break;
                case ActivationHelper.NOTIFY_CONNECTION_PROBLEM:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                    break;
                case ActivationHelper.NOTIFY_ACTIVATION_LIST:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    if (bmodel.activationHelper.getAppUrls() == null
                            || bmodel.activationHelper.getAppUrls().size() == 0) {
                        bmodel.activationHelper.clearAppUrl();
                        Toast.makeText(
                                getActivity(),
                                R.string.previous_activation_not_done_for_this_device,
                                Toast.LENGTH_LONG).show();
                    } else {
                        activation = new ActivationDialog(
                                getActivity(), addUrl);
                        activation.setCancelable(false);
                        activation.show();
                    }
                    break;
                case ActivationHelper.NOTIFY_ACTIVATION_LIST_SINGLE:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    appUrl = appPreferences.getString("appUrlNew", "");
                    bmodel.activationHelper.setSERVER_URL(appUrl);
                    new DoActivation().execute(3);
                    break;
                case ActivationHelper.NOTIFY_ACTIVATION_LIST_SINGLE_EXTEND:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    startActivity(new Intent(getActivity(),
                            LoginScreen.class));
                    getActivity().finish();
                    break;
                case ActivationHelper.NOTIFY_ACTIVATION_LIST_NULL:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    Toast.makeText(getActivity(),
                            R.string.previous_activation_not_done_for_this_device,
                            Toast.LENGTH_LONG).show();
                    break;
                case ActivationHelper.NOTIFY_URL_EMPTY:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.activationHelper.clearAppUrl();
                    Toast.makeText(getActivity(),
                            R.string.app_url_is_empty, Toast.LENGTH_LONG).show();
                    break;
                case ActivationHelper.NOTIFY_NOT_VALID_URL:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.activationHelper.clearAppUrl();
                    Toast.makeText(getActivity(),
                            R.string.please_check_app_url_configured,
                            Toast.LENGTH_LONG).show();
                    break;
                case ActivationHelper.NOTIFY_VALID_URL:
                    //progressDialogue.dismiss();
                    alertDialog.dismiss();
                    Intent in = new Intent(getActivity(),
                            LoginScreen.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(in);
                    getActivity().finish();
                    break;
                case ActivationHelper.NOTIFY_URL_NOT_MAPPED_ERROR:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.activationHelper.clearAppUrl();
                    Toast.makeText(getActivity(),
                            R.string.valid_key_oops_contact_device_admin,
                            Toast.LENGTH_LONG).show();
                    break;
                case ActivationHelper.NOTIFY_JSON_EXCEPTION:
                    //	progressDialogue.dismiss();
                    alertDialog.dismiss();
                    bmodel.activationHelper.clearAppUrl();
                    Toast.makeText(getActivity(),
                            R.string.contact_system_admin,
                            Toast.LENGTH_LONG).show();
                    break;
            }

        }

    }

    DialogInterface.OnDismissListener addUrl = new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            activation.dismiss();
            appUrl = appPreferences.getString("appUrlNew", "");
            if (appUrl.equals("")) {
                new DoActivation().execute(2);
            } else {
                bmodel.activationHelper.setSERVER_URL(appUrl);
                new DoActivation().execute(1);
            }

        }
    };


    public void onBackPressed() {
        super.onBackPressed();
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 0;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Fragment1 fragment1 = new Fragment1();
                    return fragment1;
                case 1:
                    Fragment1 fragment2 = new Fragment1();
                    return fragment2;
                case 2:
                    Fragment1 fragment3 = new Fragment1();
                    return fragment3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
