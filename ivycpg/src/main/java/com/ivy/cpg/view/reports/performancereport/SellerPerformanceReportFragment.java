package com.ivy.cpg.view.reports.performancereport;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
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
 * Created by mansoor.k on 11/01/2017.
 */

public class SellerPerformanceReportFragment extends IvyBaseFragment {

    View view;
    BusinessModel bmodel;
    private RecyclerView rvPerformance;
    private PerformanceAdapter performanceAdapter;
    private OutletPerfomanceHelper outletPerfomanceHelper;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_seller_performance, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        outletPerfomanceHelper = OutletPerfomanceHelper.getInstance(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        initializeViews();
        new LoadAsyncTask().execute();

    }

    private void initializeViews() {
        rvPerformance = (RecyclerView) view.findViewById(R.id.rvPerformance);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rvPerformance.setLayoutManager(mLayoutManager);
        rvPerformance.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_seller_perrpt, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String rptDownload = sharedPrefs.getString("rpt_dwntime", "");
        if (TimeUnit.MILLISECONDS.toMinutes(getDiffDurationMenu(rptDownload, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))) > bmodel.configurationMasterHelper.refreshMin)
            menu.findItem(R.id.menu_refresh).setVisible(true);
        else
            menu.findItem(R.id.menu_refresh).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            onBackButtonClick();
        }
        if (i == R.id.menu_refresh) {
            new PerformRptDownloadData().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        private ArrayList<SellerPerformanceBO> performanceList = new ArrayList<>();

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                ArrayList<OutletReportBO> outLetReportList = outletPerfomanceHelper.downloadOutletReports();
                ArrayList<OutletReportBO> userList = outletPerfomanceHelper.downloadUsers();

                for (OutletReportBO user : userList) {
                    int plannedCall = 0, deviateCall = 0, actualCall = 0, productiveCall = 0 ,salesVolume = 0;
                    double vistPer = 0, productivePer = 0, actual = 0, objective = 0, fitScore =0;
                    long duration = 0;
                    String lastSync = "";
                    SellerPerformanceBO sellerPerformanceBO = new SellerPerformanceBO();
                    sellerPerformanceBO.setUserId(user.getUserId());
                    sellerPerformanceBO.setUserName(user.getUserName());

                    for (OutletReportBO outletReport : outLetReportList) {
                        if (user.getUserId() == outletReport.getUserId()) {
                            actual += SDUtil.convertToDouble(outletReport.getSalesValue());
                            salesVolume += SDUtil.convertToInt(outletReport.getSalesVolume());
                            fitScore += SDUtil.convertToDouble(outletReport.getFitScore());
                            if (outletReport.getIsPlanned() == 1)
                                plannedCall += 1;
                            if (outletReport.getIsPlanned() == 0 && outletReport.isVisited() == 1)
                                deviateCall += 1;
                            if (outletReport.isVisited() == 1)
                                actualCall += 1;
                            if (outletReport.isVisited() == 1 && SDUtil.convertToDouble(outletReport.getSalesValue()) > 0)
                                productiveCall += 1;

                            //parse date and sum up intervals
                            duration += getDiffDuration(outletReport.getTimeIn(), outletReport.getTimeOut());

                            if (lastSync.length() == 0) {
                                lastSync = outletReport.getTimeOut();
                            } else {
                                if (DateTimeUtils.compareDate(outletReport.getTimeOut(), lastSync, "yyyy/MM/dd HH:mm:ss") > 0) {
                                    lastSync = outletReport.getTimeOut();
                                }
                            }
                        }
                    }
                    sellerPerformanceBO.setActual(actual + "");
                    sellerPerformanceBO.setObjective(objective + "");
                    sellerPerformanceBO.setPlannedCall(plannedCall);
                    sellerPerformanceBO.setDeviatedCall(deviateCall);
                    sellerPerformanceBO.setActualCall(actualCall);
                    sellerPerformanceBO.setProductiveCall(productiveCall);
                    sellerPerformanceBO.setSalesVolume(salesVolume + "");
                    sellerPerformanceBO.setFitScore(fitScore + "");

                    sellerPerformanceBO.setTimeSpent(String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(duration),
                            TimeUnit.MILLISECONDS.toMinutes(duration) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                            TimeUnit.MILLISECONDS.toSeconds(duration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));

                    sellerPerformanceBO.setLastSync(lastSync);
                    double totalCall = plannedCall + deviateCall;
                    vistPer = ((double) actualCall / totalCall) * 100;
                    sellerPerformanceBO.setVisitPer(bmodel.formatPercent(vistPer));
                    productivePer = ((double) productiveCall / (double) actualCall) * 100;
                    sellerPerformanceBO.setProductivePer(bmodel.formatPercent(productivePer));
                    performanceList.add(sellerPerformanceBO);
                }


                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.loading_data),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            progressDialogue.dismiss();
            if (performanceList.size() > 0) {
                performanceAdapter = new PerformanceAdapter(performanceList);
                rvPerformance.setAdapter(performanceAdapter);
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

    public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.MyViewHolder> {

        private ArrayList<SellerPerformanceBO> performList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvUserName, tvLastSync, tvTimeSpent, tvActual, tvVistPer, tvProdPer, tvPlannedCall, tvDeviateCall, tvActualCall, tvProdCall, tvSalesVolume,tvSalesVolumeTitle;
            public LinearLayout salesValueLayout;
            public TextView tvFitScore;

            public MyViewHolder(View view) {
                super(view);
                tvUserName = (TextView) view.findViewById(R.id.tv_username);
                tvLastSync = (TextView) view.findViewById(R.id.tv_last_sync_value);
                tvTimeSpent = (TextView) view.findViewById(R.id.tv_timespent_value);
                tvActual = (TextView) view.findViewById(R.id.tvactualValue);
                tvVistPer = (TextView) view.findViewById(R.id.tvVisitPerValue);
                tvProdPer = (TextView) view.findViewById(R.id.tvProdPerValue);
                tvPlannedCall = (TextView) view.findViewById(R.id.tvPlnCallValue);
                tvDeviateCall = (TextView) view.findViewById(R.id.tvadevCallValue);
                tvActualCall = (TextView) view.findViewById(R.id.tvActualCallValue);
                tvProdCall = (TextView) view.findViewById(R.id.tvProdCallValue);
                tvSalesVolume = (TextView) view.findViewById(R.id.tv_sales_volume_value);
                tvSalesVolumeTitle=(TextView) view.findViewById(R.id.tv_sales_volume_title);
                salesValueLayout=(LinearLayout) view.findViewById(R.id.salesValueLayout);
                tvFitScore = (TextView) view.findViewById(R.id.tvFitScore);

                tvUserName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvLastSync.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvTimeSpent.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvActual.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvVistPer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvProdPer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvPlannedCall.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvDeviateCall.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvActualCall.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvProdCall.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvSalesVolume.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvFitScore.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                ((TextView) view.findViewById(R.id.tv_last_sync_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tv_time_spent_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvActualTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvVisitPerTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvProdPerTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvplnCallTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvdevCallTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvActCallTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvProdCallTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tv_sales_volume_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tvFitScoreTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                if (bmodel.labelsMasterHelper
                        .applyLabels("actual_volume") != null){
                    tvSalesVolumeTitle.setText(bmodel.labelsMasterHelper
                            .applyLabels("actual_volume"));
                }

                if(bmodel.configurationMasterHelper.HIDE_SALES_VALUE_FIELD){
                    salesValueLayout.setVisibility(View.GONE);
                }

            }
        }

        public PerformanceAdapter(ArrayList<SellerPerformanceBO> performList) {
            this.performList = performList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.perform_rv_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final SellerPerformanceBO performanceBO = performList.get(position);

            holder.tvUserName.setText(performanceBO.getUserName());
            holder.tvLastSync.setText(performanceBO.getLastSync());
            holder.tvTimeSpent.setText(performanceBO.getTimeSpent());
            holder.tvActual.setText(performanceBO.getActual());
            holder.tvVistPer.setText(performanceBO.getVisitPer());
            holder.tvProdPer.setText(performanceBO.getProductivePer());
            holder.tvPlannedCall.setText(performanceBO.getPlannedCall() + "");
            holder.tvDeviateCall.setText(performanceBO.getDeviatedCall() + "");
            holder.tvActualCall.setText(performanceBO.getActualCall() + "");
            holder.tvProdCall.setText(performanceBO.getProductiveCall() + "");
            holder.tvSalesVolume.setText(performanceBO.getSalesVolume() + "");
            holder.tvFitScore.setText(performanceBO.getFitScore() + "");

        }

        @Override
        public int getItemCount() {
            return performList.size();
        }
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
                        new LoadAsyncTask().execute();
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
}


