package com.ivy.ui.attendance.inout.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.realtime.FireBaseRealtimeLocationUpload;
import com.ivy.cpg.locationservice.realtime.RealTimeLocation;
import com.ivy.cpg.locationservice.realtime.RealTimeLocationTracking;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.adapter.TimeTrackListClickListener;
import com.ivy.ui.attendance.inout.adapter.TimeTrackingAdapter;
import com.ivy.ui.attendance.inout.di.DaggerTimeTrackComponent;
import com.ivy.ui.attendance.inout.di.TimeTrackModule;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

import static com.ivy.core.base.view.BaseActivity.LOCATION_PERMISSION;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.ATTENDANCE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackingFragment extends BaseFragment implements TimeTrackingContract.TimeTrackingView, TimeTrackListClickListener {

    private ArrayList<NonFieldTwoBo> timeTrackList;
    int addDialogrequestCode;
    private String screenTitle;
    private InOutReasonDialog dialog;
    InOutReasonDialog.OnMyDialogResult onmydailogresult;

    @Inject
    TimeTrackingContract.TimeTrackingPresenter<TimeTrackingContract.TimeTrackingView> presenter;

    @Inject
    LocationUtil locationUtil;

    @BindView(R.id.rv_inout)
    RecyclerView rvTimeTrack;

    @BindView(R.id.no_data_txt)
    TextView tvNoData;

    @Override
    public void initializeDi() {

        DaggerTimeTrackComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .timeTrackModule(new TimeTrackModule(this, getActivity()))
                .build()
                .inject(TimeTrackingFragment.this);


        setBasePresenter((BasePresenter) presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_time_tracking;
    }

    @Override
    public void initVariables(View view) {
        timeTrackList = new ArrayList<>();
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null)
            screenTitle = getArguments().getString("screentitle");
    }

    @Override
    protected void setUpViews() {
        setHasOptionsMenu(true);
        setUpToolbar(screenTitle);
        rvTimeTrack.setHasFixedSize(false);
        rvTimeTrack.setNestedScrollingEnabled(false);
        rvTimeTrack.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvTimeTrack.addItemDecoration(new DividerItemDecoration(rvTimeTrack.getContext(), DividerItemDecoration.HORIZONTAL));

        presenter.fetchData();
        if (presenter.isRealTimeLocationOn()) {
            checkAndRequestPermissionAtRunTime(LOCATION_PERMISSION);
            if (!locationUtil.isGPSProviderEnabled()) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
                if (resultCode == ConnectionResult.SUCCESS)
                    requestLocation(getActivity());
                else
                    showLocationEnableDialog();

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (presenter.isShowCapturedLocation()) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                locationUtil.startLocationListener();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (presenter.isShowCapturedLocation()) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                locationUtil.stopLocationListener();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nonfield_two, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_select).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            startActivityAndFinish(HomeScreenActivity.class);
            return true;
        } else if (i1 == R.id.menu_add) {
            if (presenter.isPreviousInOutCompleted())
                presenter.fetchInOutReason();

            else {
                try {
                    showAlert("", getResources().getString(R.string.out_time_error));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void populateDataToList(ArrayList<NonFieldTwoBo> timeTrackList) {
        this.timeTrackList.clear();
        this.timeTrackList = timeTrackList;

        if (this.timeTrackList.size() > 0) {
            for (NonFieldTwoBo timeTrackBo : timeTrackList) {
                if ((!AppUtils.isNullOrEmpty(timeTrackBo.getInTime())) &&
                        (!AppUtils.isNullOrEmpty(timeTrackBo.getOutTime()))) {
                    timeTrackBo.setStatus(getResources().getString(R.string.in_complete));
                } else {
                    timeTrackBo.setStatus(getResources().getString(R.string.in_partial));
                }
            }
            rvTimeTrack.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            TimeTrackingAdapter timeTrackingAdapter = new TimeTrackingAdapter(getActivity(), this.timeTrackList, this);
            rvTimeTrack.setAdapter(timeTrackingAdapter);
        } else {
            rvTimeTrack.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Starts the service to Track the Realtime Location and uploads in FIREBASE
     * <p>
     * Status code --- Realtime Location tracking
     * STATUS_SUCCESS - Service started Successfully
     * STATUS_LOCATION_PERMISSION - Location Permission is not enabled
     * STATUS_GPS - GPS Not enabled
     * STATUS_LOCATION_ACCURACY - Location Accuracy level is low
     * STATUS_MOCK_LOCATION - Mock Location is enabled
     * STATUS_SERVICE_ERROR - Problem in starting Service
     *
     * @return df
     */

    @Override
    public boolean isUpdateRealTimeIn() {
        boolean success = false;
        RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
        realTimeLocation.validateLoginAndUpdate(getContext(), REALTIME_LOCATION_PATH, null, "AttendanceIn");
        int statusCode = RealTimeLocationTracking.startLocationTracking(realTimeLocation, getContext());
        if (statusCode == LocationConstants.STATUS_SUCCESS)
            success = true;
        return success;
    }

    @Override
    public void updateRealTimeOut() {

        RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
        RealTimeLocationTracking.stopLocationTracking(getContext());
        realTimeLocation.validateLoginAndUpdate(getContext(), REALTIME_LOCATION_PATH, null, "AttendanceOut");

    }

    @Override
    public void showInOutDialog(ArrayList<ReasonMaster> reasonList) {
        dialog = new InOutReasonDialog(getActivity(), onmydailogresult, presenter.isAttendanceRemark(), reasonList);

        dialog.setDialogResult(new InOutReasonDialog.OnMyDialogResult() {

            @Override
            public void cancel(String reasonid, String remarks) {
                dialog.dismiss();
                presenter.saveInOutDetails(reasonid, remarks);
            }
        });
        dialog.show();
    }

    @Override
    public void uploadAttendance(String inOrOut) {
        RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
        if (inOrOut.equalsIgnoreCase("IN")) {
            realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceIn");
        } else {
            realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceOut");
        }
    }

    public void showLocationEnableDialog() {
        new CommonDialog(getContext(), "", getResources().getString(R.string.enable_gps), getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Intent myIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

            }
        }).show();
    }

    @Override
    public void onInTimeClick(int position) {
        presenter.startLocationService(position);
    }

    @Override
    public void onOutTimeClick(int position) {
        timeTrackList.get(position).setOutTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
        presenter.updateTimeTrackDetails(timeTrackList.get(position));
        presenter.stopLocationService(timeTrackList.get(position).getReason());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == addDialogrequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.fetchData();
            }
        }

    }


}
