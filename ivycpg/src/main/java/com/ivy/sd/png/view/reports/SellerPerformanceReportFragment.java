package com.ivy.sd.png.view.reports;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.bo.SellerPerformanceBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mansoor.k on 11/01/2017.
 */

public class SellerPerformanceReportFragment extends IvyBaseFragment {

    View view;
    BusinessModel bmodel;
    private RecyclerView rvPerformance;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_seller_performance, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            onBackButtonClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        Intent i = new Intent(getActivity(), HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_REPORT");
        i.putExtra("title", "aaa");
        startActivity(i);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        private ArrayList<SellerPerformanceBO> performanceList = new ArrayList<>();

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                ArrayList<OutletReportBO> outLetReportList = bmodel.reportHelper.downloadOutletReports();
                ArrayList<OutletReportBO> userList = bmodel.reportHelper.downloadUsers();

                for (OutletReportBO user : userList) {
                    int plannedCall = 0, deviateCall = 0, actualCall = 0, productiveCall = 0;
                    double vistPer = 0, productivePer = 0, actual = 0, objective = 0;
                    long duration = 0;
                    String lastSync = "";
                    SellerPerformanceBO sellerPerformanceBO = new SellerPerformanceBO();
                    sellerPerformanceBO.setUserId(user.getUserId());
                    sellerPerformanceBO.setUserName(user.getUserName());

                    for (OutletReportBO outletReport : outLetReportList) {
                        if (user.getUserId() == outletReport.getUserId()) {
                            actual += Double.parseDouble(outletReport.getSalesValue());
                            if (outletReport.getIsPlanned().equalsIgnoreCase("Y"))
                                plannedCall += 1;
                            if (!outletReport.getIsPlanned().equalsIgnoreCase("Y") && outletReport.getIsVisited().equalsIgnoreCase("Y"))
                                deviateCall += 1;
                            if (outletReport.getIsVisited().equalsIgnoreCase("Y"))
                                actualCall += 1;
                            if (outletReport.getIsVisited().equalsIgnoreCase("Y") && Double.parseDouble(outletReport.getSalesValue()) > 0)
                                productiveCall += 1;

                            //parse date and sum up intervals
                            duration += getDiffDuration(outletReport.getTimeIn(), outletReport.getTimeOut());

                            if (lastSync.length() == 0) {
                                lastSync = outletReport.getTimeOut();
                            } else {
                                if (SDUtil.compareDate(outletReport.getTimeOut(), lastSync, "yyyy/MM/dd HH:mm:ss") > 0) {
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

                    sellerPerformanceBO.setTimeSpent(TimeUnit.MILLISECONDS.toHours(duration) + ":" +
                            TimeUnit.MILLISECONDS.toMinutes(duration) + ":" +
                            TimeUnit.MILLISECONDS.toSeconds(duration));

                    sellerPerformanceBO.setLastSync(lastSync);
                    int totalCall = plannedCall + deviateCall;
                    vistPer = (actualCall / totalCall) * 100;
                    sellerPerformanceBO.setVisitPer(bmodel.formatPercent(vistPer));
                    productivePer = (productiveCall / actualCall) * 100;
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
        }

    }

    private long getDiffDuration(String startDate, String endData) {
        long diffDuration = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
            public Button btnFilter;

            public MyViewHolder(View view) {
                super(view);
                btnFilter = (Button) view.findViewById(R.id.btn_filter);
                btnFilter.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            }
        }

        public PerformanceAdapter(ArrayList<SellerPerformanceBO> performList) {
            this.performList = performList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_rv_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final SellerPerformanceBO performanceBO = performList.get(position);


        }

        @Override
        public int getItemCount() {
            return performList.size();
        }
    }

}


