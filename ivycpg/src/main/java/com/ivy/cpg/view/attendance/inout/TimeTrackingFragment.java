package com.ivy.cpg.view.attendance.inout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.realtime.FireBaseRealtimeLocationUpload;
import com.ivy.cpg.locationservice.realtime.RealTimeLocation;
import com.ivy.cpg.locationservice.realtime.RealTimeLocationTracking;
import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.cpg.view.attendance.inout.InOutReasonDialog.OnMyDialogResult;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.ATTENDANCE_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;


/**
 * @deprecated
 */
public class TimeTrackingFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    int addDialogrequestCode;
    private ListView listview;
    ArrayList<NonFieldTwoBo> nonFieldTwoBos = new ArrayList<>();
    MyAdapter mAdapter;
    private InOutReasonDialog dialog;
    OnMyDialogResult onmydailogresult;
    TextView no_data_txt;
    private ArrayList<StandardListBO> childList;
    private int mSelectedIdIndex = -1;
    private String childUserName = "";
    private AttendanceHelper attendanceHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        attendanceHelper = AttendanceHelper.getInstance(getActivity());

        View view = inflater.inflate(R.layout.fragment_time_tracking, container, false);
        listview = view.findViewById(R.id.listview);
        no_data_txt = view.findViewById(R.id.no_data_txt);

        //typeface
        no_data_txt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = getActivity().getIntent().getExtras();

        setScreenTitle(bundle.getString("screentitle"));
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new MyAdapter();
        listview.setAdapter(mAdapter);

        loadListData();

        if (bmodel.configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE) {

            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(3);

            if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getContext());
                if (resultCode == ConnectionResult.SUCCESS) {
                    bmodel.requestLocation(getActivity());
                } else {
                    onCreateDialogNew();
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    //Displayes the dialog if GPS is not enabled
    protected void onCreateDialogNew() {
        new CommonDialog(getContext().getApplicationContext(), getContext(), "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Intent myIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

            }
        }).show();
    }


    public void loadNonFieldTwoDetails() {

        attendanceHelper.downloadNonFieldTwoDetails(getActivity().getApplicationContext());
        nonFieldTwoBos = attendanceHelper.getNonFieldTwoBoList();
        mAdapter.notifyDataSetChanged();
    }

    private class MyAdapter extends ArrayAdapter<NonFieldTwoBo> {


        public MyAdapter() {
            super(getActivity(), R.layout.row_nonfield_two, nonFieldTwoBos);

        }

        public NonFieldTwoBo getItem(int position) {
            return nonFieldTwoBos.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return nonFieldTwoBos.size();
        }

        @Override
        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(R.layout.row_nonfield_two,
                        parent, false);

                holder.tvOutTime = convertView
                        .findViewById(R.id.txt_fromTime);
                holder.btOutTime = convertView
                        .findViewById(R.id.btn_fromTime);
                holder.btInTime = convertView
                        .findViewById(R.id.btn_toTime);
                holder.tvInTime = convertView
                        .findViewById(R.id.txt_toTime);
                holder.tvReason = convertView
                        .findViewById(R.id.txt_reason);
                holder.tvStatus = convertView
                        .findViewById(R.id.txt_status);
                holder.tvRemarks = convertView
                        .findViewById(R.id.txt_remarks);

                //typefaces
                holder.tvOutTime.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tvInTime.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tvReason.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                holder.tvStatus.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                ((TextView) convertView.findViewById(R.id.txt_Tit_To)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_from)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_Status)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_Reason)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.btOutTime.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
                holder.btInTime.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

                if (!bmodel.configurationMasterHelper.IS_ATTENDANCE_REMARK)
                    holder.tvRemarks.setVisibility(View.GONE);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.nonFieldTwoBO = nonFieldTwoBos.get(position);
            holder.tvOutTime.setText(holder.nonFieldTwoBO.getOutTime());
            holder.tvStatus.setText(holder.nonFieldTwoBO.getStatus());
            holder.btOutTime.setText(getResources().getString(R.string.endC));
            String inTime = holder.nonFieldTwoBO.getInTime() != null ? holder.nonFieldTwoBO.getInTime() : " ";
            String outTime = holder.nonFieldTwoBO.getOutTime() != null ? holder.nonFieldTwoBO.getOutTime() : " ";
            String date;
            String time;
            StringTokenizer tokenizer;


            if (holder.nonFieldTwoBO.getOutTime() != null && !holder.nonFieldTwoBO.getOutTime().trim().equalsIgnoreCase("")) {
                holder.btOutTime.setVisibility(View.GONE);
                holder.tvOutTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(outTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.tvOutTime.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            } else {
                holder.tvOutTime.setVisibility(View.GONE);
                holder.btOutTime.setVisibility(View.VISIBLE);
                holder.btOutTime.setText(getResources().getString(R.string.endC));
            }


            if (holder.nonFieldTwoBO.getInTime() != null && !holder.nonFieldTwoBO.getInTime().trim().equalsIgnoreCase("")) {
                holder.btInTime.setVisibility(View.GONE);
                holder.tvInTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(inTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.tvInTime.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            } else {
                holder.tvInTime.setVisibility(View.GONE);
                holder.btInTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(inTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.btInTime.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            }

            holder.btInTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (startLocationService(holder.nonFieldTwoBO.getReason())) {
                        holder.nonFieldTwoBO.setInTime(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                        attendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO, getActivity());

                        loadNonFieldTwoDetails();
                    }
                }
            });

            holder.btOutTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.nonFieldTwoBO.setOutTime(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                    attendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO, getActivity());
                    loadNonFieldTwoDetails();

                    stopLocationService(holder.nonFieldTwoBO.getReason());

                }
            });

            holder.tvReason.setText(attendanceHelper
                    .getReasonName(holder.nonFieldTwoBO.getReason(), getActivity()));

            holder.tvRemarks.setText(getActivity().getResources().getString(R.string.remark_hint) + ":" + holder.nonFieldTwoBO.getRemarks());


            return convertView;
        }

    }

    class ViewHolder {
        NonFieldTwoBo nonFieldTwoBO;
        TextView tvOutTime, tvReason, tvInTime, tvStatus, tvRemarks;
        Button btInTime, btOutTime;

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        } else if (i1 == R.id.menu_select) {
            showUserDialog();
            return true;
        } else if (i1 == R.id.menu_add) {

            if (attendanceHelper.previousInOutTimeCompleted()) {
                dialog = new InOutReasonDialog(getActivity(), onmydailogresult, bmodel.configurationMasterHelper.IS_ATTENDANCE_REMARK);
                dialog.setDialogResult(new InOutReasonDialog.OnMyDialogResult() {

                    public void cancel(String reasonid, String remarks) {
                        dialog.dismiss();

                        NonFieldTwoBo addNonFieldTwoBo = new NonFieldTwoBo();
                        addNonFieldTwoBo.setId(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "");
                        addNonFieldTwoBo.setFromDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                        addNonFieldTwoBo.setInTime(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                        addNonFieldTwoBo.setOutTime(null);
                        addNonFieldTwoBo.setRemarks(remarks);
                        addNonFieldTwoBo.setReason(reasonid);

                        if (startLocationService(addNonFieldTwoBo.getReason())) {

                            attendanceHelper.saveNonFieldWorkTwoDetail(addNonFieldTwoBo, getActivity());
                            if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE) {
                                HomeScreenFragment.isLeave_today = attendanceHelper.checkLeaveAttendance(getActivity());
                            }

                            //}
                            listview.setVisibility(View.VISIBLE);
                            no_data_txt.setVisibility(View.GONE);
                            loadNonFieldTwoDetails();
                        }
                    }
                });
                dialog.show();

            } else {
                try {
                    bmodel.showAlert(getResources().getString(R.string.out_time_error), 0);
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserDialog() {
        childList = attendanceHelper.loadChildUserList(getActivity());
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }

    }

    private void showDialog() {
        ArrayAdapter<String> mChildUserNameAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : childList)
            mChildUserNameAdapter.add(temp.getChildUserName());

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select User");
        builder.setSingleChoiceItems(mChildUserNameAdapter, mSelectedIdIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedIdIndex = item;
                        bmodel.setSelectedUserId(childList.get(item).getChildUserId());
                        childUserName = childList.get(item).getChildUserName();
                        setScreenTitle(bmodel.getMenuName("MENU_IN_OUT") + " (" +
                                childUserName + ")");
                        loadListData();
                        dialog.dismiss();
                    }
                });

        AlertDialog objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }

    private void loadListData() {
        attendanceHelper.downloadNonFieldTwoDetails(getActivity());
        nonFieldTwoBos = attendanceHelper.getNonFieldTwoBoList();
        //data empty or not
        if (nonFieldTwoBos == null || !(nonFieldTwoBos.size() > 0)) {
            listview.setVisibility(View.GONE);
            no_data_txt.setVisibility(View.VISIBLE);
        } else {
            mAdapter.notifyDataSetChanged();
            listview.setVisibility(View.VISIBLE);
            no_data_txt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == addDialogrequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                loadNonFieldTwoDetails();
            }
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
    private boolean startLocationService(String reasonId) {

        boolean success = false;
        if (bmodel.configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE
                && AttendanceHelper.getInstance(getContext()).isWorkingStatus(Integer.parseInt(reasonId), getContext())) {
            RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
            realTimeLocation.validateLoginAndUpdate(getContext(), REALTIME_LOCATION_PATH, null, "AttendanceIn");
            int statusCode = RealTimeLocationTracking.startLocationTracking(realTimeLocation, getContext());
            if (statusCode == LocationConstants.STATUS_SUCCESS)
                success = true;

        } else {
            success = true;
        }

        uploadAttendance("IN", reasonId);

        return success;
    }

    /**
     * Stops the Location Track Service
     * Updates the outTime when stopped
     */
    private void stopLocationService(String reasonId) {

        if (bmodel.configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE
                && AttendanceHelper.getInstance(getContext()).isWorkingStatus(Integer.parseInt(reasonId), getContext())) {
            RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
            RealTimeLocationTracking.stopLocationTracking(getContext());
            realTimeLocation.validateLoginAndUpdate(getContext(), REALTIME_LOCATION_PATH, null, "AttendanceOut");
        }

        uploadAttendance("OUT", reasonId);
    }

    /**
     * Upload Attendance status - IN/OUT with Time in Firebase
     */
    private void uploadAttendance(String IN_OUT, String reasonId) {
        if (bmodel.configurationMasterHelper.IS_UPLOAD_ATTENDANCE
                && AttendanceHelper.getInstance(getContext()).isWorkingStatus(Integer.parseInt(reasonId), getContext())) {
            RealTimeLocation realTimeLocation = new FireBaseRealtimeLocationUpload();
            if (IN_OUT.equalsIgnoreCase("IN")) {
                realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceIn");
            } else {
                realTimeLocation.validateLoginAndUpdate(getContext(), ATTENDANCE_PATH, null, "AttendanceOut");
            }
        }
    }
}
