package com.ivy.ui.attendance.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.attendance.TimeTrackingContract;
import com.ivy.ui.attendance.adapter.TimeTrackListClickListener;
import com.ivy.ui.attendance.adapter.TimeTrackingAdapter;
import com.ivy.ui.attendance.di.DaggerTimeTrackComponent;
import com.ivy.ui.attendance.di.TimeTrackModule;

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

    @BindView(R.id.listview)
    ListView lvTimeTrack;

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
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            screenTitle = bundle.getString("screentitle");
        }
    }

    @Override
    protected void setUpViews() {
        setHasOptionsMenu(true);
        setUpToolBar();
        presenter.fetchData();
        if (presenter.isRealTimeLocationOn()) {
            checkAndRequestPermissionAtRunTime(LOCATION_PERMISSION);
            if (!presenter.getLocationUtil().isGPSProviderEnabled()) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
                if (resultCode == ConnectionResult.SUCCESS) {
                    ((BusinessModel) getActivity().getApplicationContext()).requestLocation(getActivity());
                } else {
                    showLocationEnableDialog();
                }
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
                presenter.getLocationUtil().startLocationListener();
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
                presenter.getLocationUtil().stopLocationListener();
        }
    }

    private void setUpToolBar() {
        getActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
        }

        if (screenTitle != null)
            setScreenTitle(screenTitle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
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
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_add) {
            if (presenter.isPreviousInOutCompeleted())
                presenter.fetchReasonAndShowDialog();

            else {
                try {
                    ((BusinessModel) getActivity().getApplicationContext()).showAlert(getResources().getString(R.string.out_time_error), 0);
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
                if ((timeTrackBo.getInTime() != null && !timeTrackBo.getInTime().trim().equalsIgnoreCase(""))
                        && (timeTrackBo.getOutTime() != null && !timeTrackBo.getOutTime().trim().equalsIgnoreCase(""))) {
                    timeTrackBo.setStatus(getResources().getString(R.string.in_complete));
                } else {
                    timeTrackBo.setStatus(getResources().getString(R.string.in_partial));
                }
            }
            lvTimeTrack.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            TimeTrackingAdapter timeTrackingAdapter = new TimeTrackingAdapter(getActivity(), this.timeTrackList, this);
            lvTimeTrack.setAdapter(timeTrackingAdapter);
        } else {
            lvTimeTrack.setVisibility(View.GONE);
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
    public boolean updateRealTimeIn() {
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


    /**
     * Upload Attendance status - IN/OUT with Time in Firebase
     */
    @Override
    public void uploadAttendance(String IN_OUT, String reasonId) {
        if (presenter.checkConfigandWorkStatus(Integer.parseInt(reasonId))) {
            RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
            if (IN_OUT.equalsIgnoreCase("IN")) {
                realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceIn");
            } else {
                realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceOut");
            }
        }
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

    public void showLocationEnableDialog() {
        new CommonDialog(getContext().getApplicationContext(), getContext(), "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
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
        if (presenter.startLocationService(timeTrackList.get(position).getReason())) {
            timeTrackList.get(position).setOutTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
            presenter.updateTimeTrackDetails(timeTrackList.get(position));
        }

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
