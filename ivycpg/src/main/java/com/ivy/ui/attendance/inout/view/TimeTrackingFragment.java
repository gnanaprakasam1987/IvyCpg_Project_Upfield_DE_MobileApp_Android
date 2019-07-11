package com.ivy.ui.attendance.inout.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.adapter.TimeTrackListClickListener;
import com.ivy.ui.attendance.inout.adapter.TimeTrackingAdapter;
import com.ivy.ui.attendance.inout.di.DaggerTimeTrackComponent;
import com.ivy.ui.attendance.inout.di.TimeTrackModule;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

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
    private Context mContext;

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
    public void init(View view) {
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

        presenter.fetchData(false);
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
            if (presenter.isPreviousInOutCompleted(timeTrackList))
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
                if ((!StringUtils.isNullOrEmpty(timeTrackBo.getInTime())) &&
                        (!StringUtils.isNullOrEmpty(timeTrackBo.getOutTime()))) {
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
    public void onOutTimeClick(int position) {
        timeTrackList.get(position).setOutTime(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
        presenter.updateTimeTrackDetails(timeTrackList.get(position));
        presenter.stopLocationService(timeTrackList.get(position).getReason());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == addDialogrequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.fetchData(false);
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

        }
    };

    @Override
    public void uploadAttendanceToServer() {
        new UploadAttendance().execute();
    }

    class UploadAttendance extends AsyncTask<Void, Void, UploadHelper.UPLOAD_STATUS> {
        protected void onPreExecute() {
            if(mContext!=null)
                showLoading(mContext.getResources().getString(R.string.uploading_data));
        }

        @Override
        protected UploadHelper.UPLOAD_STATUS doInBackground(Void... params) {
            return UploadHelper.getInstance(mContext).uploadTransactionDataByType(getHandler(), DataMembers.ATTENDANCE_UPLOAD, mContext);
        }

        @Override
        protected void onPostExecute(UploadHelper.UPLOAD_STATUS result) {
            super.onPostExecute(result);
            hideLoading();
            if(mContext!=null) {
                if (result == UploadHelper.UPLOAD_STATUS.SUCCESS) {
                    showAlert("", mContext.getResources().getString(R.string.successfully_uploaded));
                } else if (result == UploadHelper.UPLOAD_STATUS.URL_NOTFOUND) {
                    showAlert("", mContext.getResources().getString(R.string.url_not_mapped));
                }else if (result == UploadHelper.UPLOAD_STATUS.TOKEN_ERROR) {
                    showAlert("", mContext.getResources().getString(R.string.token_expired));
                }else{
                    showAlert("", mContext.getResources().getString(R.string.upload_failed_please_try_again));
                }
            }
        }
    }


}
