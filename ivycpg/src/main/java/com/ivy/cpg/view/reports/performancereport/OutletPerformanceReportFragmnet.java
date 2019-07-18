package com.ivy.cpg.view.reports.performancereport;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by rajkumar.s on 11/3/2017.
 */

public class OutletPerformanceReportFragmnet extends IvyBaseFragment implements SellerListFragment.SellerSelectionInterface {

    View view;
    private BusinessModel bmodel;
    private OutletPerfomanceHelper outletPerfomanceHelper;
    private DrawerLayout mDrawerLayout;
    FrameLayout drawer;

    private ArrayList<OutletReportBO> lstReports;

    View cardView;
    LinearLayout ll_content;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_outlet_performance_report, container, false);
        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
            outletPerfomanceHelper = OutletPerfomanceHelper.getInstance(getActivity());

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }


            ll_content = (LinearLayout) view.findViewById(R.id.ll_content);

            downloadReportData();
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return view;
    }

    private void downloadReportData() {
        lstReports = outletPerfomanceHelper.downloadOutletReports();
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            setHasOptionsMenu(true);

            ActionBarDrawerToggle mDrawerToggle;
            drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

            mDrawerLayout = (DrawerLayout) getView().findViewById(
                    R.id.drawer_layout);

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.END);
            //  mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                    mDrawerLayout,
                    R.string.ok,
                    R.string.close
            ) {
                public void onDrawerClosed(View view) {
                    ((TextView) getActivity().findViewById(R.id.tv_toolbar_title)).setText(outletPerfomanceHelper.mSelectedActivityName);
                    getActivity().supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    ((TextView) getActivity().findViewById(R.id.tv_toolbar_title)).setText(getResources().getString(R.string.filter));

                    getActivity().supportInvalidateOptionsMenu();
                }
            };

            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.closeDrawer(GravityCompat.END);


            updateView(null, true);
        } catch (Exception ex) {
            Commons.printException(ex);
        }


    }


    private void updateView(ArrayList<Integer> mSelectedUsers, boolean isAllUser) {

        LinearLayout ll_product_layout;
        View detailView;

        ll_content.removeAllViews();
        if (isAllUser || mSelectedUsers != null) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());

            for (OutletReportBO bo : outletPerfomanceHelper.getLstUsers()) {

                if (isAllUser || mSelectedUsers.contains(bo.getUserId())) {

                    cardView = inflater.inflate(R.layout.layout_outlet_perf_report_header, null);
                    TextView tv_groupName = (TextView) cardView.findViewById(R.id.tv_groupName);
                    tv_groupName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    tv_groupName.setText(bo.getUserName());

                    ll_product_layout = (LinearLayout) cardView.findViewById(R.id.ll_products);

                    int sequence = 0;
                    for (OutletReportBO detailBO : lstReports) {
                        if (detailBO.getUserId() == bo.getUserId()) {
                            detailView = inflater.inflate(R.layout.layout_outlet_perf_child, null);
                            ((View) detailView.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


                            sequence += 1;
                            bo.setSequence(sequence);


                            TextView tv_retailername = (TextView) detailView.findViewById(R.id.tv_retailer_name);
                            TextView tv_location = (TextView) detailView.findViewById(R.id.tv_location);
                            TextView tv_address = (TextView) detailView.findViewById(R.id.tv_address);

                            TextView lbl_time_in = (TextView) detailView.findViewById(R.id.lbl_time_in);
                            TextView tv_time_in = (TextView) detailView.findViewById(R.id.tv_time_in);
                            TextView lbl_time_out = (TextView) detailView.findViewById(R.id.lbl_time_out);
                            TextView tv_time_out = (TextView) detailView.findViewById(R.id.tv_time_out);

                            TextView lbl_duration = (TextView) detailView.findViewById(R.id.lbl_duration);
                            TextView tv_duration = (TextView) detailView.findViewById(R.id.tv_duration);
                            TextView lbl_order_value = (TextView) detailView.findViewById(R.id.lbl_order_value);
                            TextView tv_order_value = (TextView) detailView.findViewById(R.id.tv_order_value);
                            TextView tv_sequence = (TextView) detailView.findViewById(R.id.tv_sequence);
                            TextView lbl_seller_volume = (TextView) detailView.findViewById(R.id.lbl_seller_volume);
                            TextView tv_selles_volume = (TextView) detailView.findViewById(R.id.tv_seller_volume);

                            tv_retailername.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_location.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_address.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_time_in.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_time_out.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_order_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_selles_volume.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                            tv_sequence.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                            lbl_time_in.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            lbl_time_out.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            lbl_duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            lbl_seller_volume.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                            lbl_order_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                            tv_retailername.setText(detailBO.getRetailerName());
                            tv_location.setText(detailBO.getLocationName());
                            tv_address.setText(detailBO.getAddress());
                            tv_time_in.setText(detailBO.getTimeIn());
                            tv_time_out.setText(detailBO.getTimeOut());
                            //tv_duration.setText(detailBO.getDuration());
                            tv_order_value.setText(detailBO.getSalesValue());
                            tv_selles_volume.setText(detailBO.getSalesVolume());


                            long duration = 0;
                            //parse date and sum up intervals
                            duration += getDiffDuration(detailBO.getTimeIn(), detailBO.getTimeOut());
                            tv_duration.setText(String.format("%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(duration),
                                    TimeUnit.MILLISECONDS.toMinutes(duration) -
                                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));

                            if (detailBO.getTimeOut() != null) {
                                tv_sequence.setVisibility(View.VISIBLE);
                                tv_sequence.setText("Seq:" + sequence);
                            } else {
                                tv_sequence.setVisibility(View.GONE);
                            }

                            ll_product_layout.addView(detailView);
                        }

                    }


                    ll_content.addView(cardView);

                }

            }
        }

    }

    private long getDiffDuration(String startDate, String endData) {
        long diffDuration = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Date d1 = format.parse(startDate);
            Date d2 = format.parse(endData);
            diffDuration = d2.getTime() - d1.getTime();
        } catch (ParseException e) {
            Commons.printException(e);
        }
        return diffDuration;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_seller_mapview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_users).setVisible(!drawerOpen);

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String rptDownload = sharedPrefs.getString("rpt_dwntime", "");
        if (TimeUnit.MILLISECONDS.toMinutes(getDiffDurationMenu(rptDownload, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))) > bmodel.configurationMasterHelper.refreshMin)
            menu.findItem(R.id.menu_refresh).setVisible(true);
        else
            menu.findItem(R.id.menu_refresh).setVisible(false);
    }

    private long getDiffDurationMenu(String startDate, String endData) {
        long diffDuration = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Date d1 = format.parse(startDate);
            Date d2 = format.parse(endData);
            diffDuration = d2.getTime() - d1.getTime();
        } catch (ParseException e) {
            Commons.printException(e);
        }
        return diffDuration;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButtonClick();
            }
        } else if (i == R.id.menu_users) {
            loadUsers();
        } else if (i == R.id.menu_refresh) {
            new PerformRptDownloadData().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    class PerformRptDownloadData extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;
        private ProgressDialog progressDialogue;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.downloading_rpt),
                    true, false);
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken(false);
            String response = bmodel.synchronizationHelper.sendPostMethod(outletPerfomanceHelper.getPerformRptUrl(), jsonObject);
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

                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putString("rpt_dwntime",
                                    DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                            editor.commit();

                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            progressDialogue.dismiss();
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                if (errorCode
                        .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    if (outletPerfomanceHelper.isPerformReport()) {

                        downloadReportData();
                        updateView(null, true);

                        getActivity().invalidateOptionsMenu();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
                        onBackButtonClick();
                    }

                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        bmodel.showAlert(errorMessage, 0);
                    }
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadUsers() {

        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = getActivity().getSupportFragmentManager();
            SellerListFragment frag = (SellerListFragment) fm.findFragmentByTag("filter");
            FragmentTransaction ft = fm.beginTransaction();
            if (frag != null)
                ft.detach(frag);

            SellerListFragment fragment = new SellerListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("users", outletPerfomanceHelper.getLstUsers());
            fragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragment, "filter");
            ft.commit();

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } catch (Exception ex) {

        }
    }

    private void onBackButtonClick() {
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    @Override
    public void updateUserSelection(ArrayList<Integer> mSelectedUsers, boolean isAllUser) {

        updateView(mSelectedUsers, isAllUser);

        mDrawerLayout.closeDrawers();
    }


    @Override
    public void updateClose() {
        mDrawerLayout.closeDrawers();
    }
}
