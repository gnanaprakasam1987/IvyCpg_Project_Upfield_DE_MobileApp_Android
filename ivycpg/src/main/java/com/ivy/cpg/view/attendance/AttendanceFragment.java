package com.ivy.cpg.view.attendance;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AttendanceFragment extends IvyBaseFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private Spinner leaveSpinner, leaveReasonSpinner;
    private TextView leaveReasonTextView;
    private String reason;
    private Button tvFromDate, tvToDate;
    private int atd_id = 0, reason_id = 0;
    private String atd_code = "";
    private String currentDate = "", fromDate = "", toDate = "";
    private AlertDialog mAlertDialog;
    private View view;
    private AttendanceHelper attendanceHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        attendanceHelper = AttendanceHelper.getInstance(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // Set title to actionbar
        if (bmodel.configurationMasterHelper.SHOW_ATTENDANCE && actionBar != null) {
            setScreenTitle(getResources().getString(R.string.attend));
            actionBar.setIcon(null);
        } else if (actionBar != null) {
            Bundle bundle = getArguments();
            if (bundle == null)
                bundle = getActivity().getIntent().getExtras();
            setScreenTitle(bundle.getString("screentitle"));
            actionBar.setIcon(null);
        }

        // Used to on / off the back arrow icon
        if (bmodel.configurationMasterHelper.SHOW_ATTENDANCE && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.attendance_fragment, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        leaveSpinner = view.findViewById(R.id.sp_reason);

        leaveReasonSpinner = view.findViewById(R.id.sp_special_reason);
        leaveReasonTextView = view.findViewById(R.id.leavereasonTextViewId);


        currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);

        fromDate = currentDate;
        toDate = currentDate;

        tvFromDate = view.findViewById(R.id.fromDatetxt);
        tvToDate = view.findViewById(R.id.todatetext);
        tvFromDate.setText(fromDate);
        tvToDate.setText(toDate);
        tvFromDate.setOnClickListener(this);
        tvToDate.setOnClickListener(this);
        //typeface
        ((TextView) view.findViewById(R.id.reasonTextViewId)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.leavereasonTextViewId)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        tvFromDate.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        tvToDate.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        Button btn_proceed = view.findViewById(R.id.buttonproceed);
        btn_proceed.setOnClickListener(this);
        btn_proceed.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

        ArrayAdapter<AttendanceBO> leaveAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        leaveAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        attendanceHelper.downloadAttendanceReasons(getActivity());

        for (int j = 0; j < attendanceHelper.getReasonList().size(); ++j) {
            if (attendanceHelper.getReasonList().get(j).getAtd_PLId() == 0) {
                leaveAdapter.add(attendanceHelper.getReasonList()
                        .get(j));
            }
        }

        leaveSpinner.setAdapter(leaveAdapter);
        leaveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {


                AttendanceBO stdBo = (AttendanceBO) arg0.getSelectedItem();
                reason = arg0.getSelectedItem().toString();
                atd_code = stdBo.getAtd_LCode();

                if (position != 0) {
                    if (stdBo.getAtd_isRequired() == 1) {
                        leaveReasonTextView.setVisibility(View.VISIBLE);
                        leaveReasonSpinner.setVisibility(View.VISIBLE);
                        updateReasons(stdBo.getAtd_Lid());

                    } else {
                        leaveReasonTextView.setVisibility(View.GONE);
                        leaveReasonSpinner.setVisibility(View.GONE);
                        atd_id = stdBo.getAtd_Lid();
                    }
                } else {
                    leaveReasonTextView.setVisibility(View.GONE);
                    leaveReasonSpinner.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {


            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        Commons.print("lang " + Locale.getDefault().getLanguage() + "/" + getActivity().getBaseContext().getResources().getConfiguration().locale);
        if (i == R.id.buttonproceed) {
            if (reason != null && reason
                    .equalsIgnoreCase(getResources().getString(R.string.none)))
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.select_reason),
                        Toast.LENGTH_SHORT).show();
            else {

                if (checkDate(fromDate, toDate))
                    showAlert(getResources().getString(R.string.attend),
                            getResources().getString(R.string.proceed), -1);
            }


        } else if (i == R.id.fromDatetxt) {
            final Calendar c = Calendar.getInstance();
            int mFromYear = c.get(Calendar.YEAR);
            int mFromMonth = c.get(Calendar.MONTH);
            int mFromDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle,
                    new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year1,
                                              int monthOfYear1, int dayOfMonth1) {
                            if (checkDate(currentDate, year1 + "/"
                                    + (monthOfYear1 + 1) + "/" + dayOfMonth1)) {
                                tvFromDate
                                        .setText(year1
                                                + "/"
                                                + ((monthOfYear1 + 1) < 10 ? "0"
                                                + (monthOfYear1 + 1)
                                                : (monthOfYear1 + 1))
                                                + "/"
                                                + ((dayOfMonth1) < 10 ? "0"
                                                + (dayOfMonth1)
                                                : (dayOfMonth1)));
                                fromDate = year1 + "/" + ((monthOfYear1 + 1))
                                        + "/" + ((dayOfMonth1));
                            }
                        }
                    }, mFromYear, mFromMonth, mFromDay);
            dpd.show();

        } else if (i == R.id.todatetext) {
            final Calendar c1 = Calendar.getInstance();
            int mToYear = c1.get(Calendar.YEAR);
            int mToMonth = c1.get(Calendar.MONTH);
            int mToDay = c1.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd1 = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle,
                    new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            if (checkDate(fromDate, year + "/"
                                    + (monthOfYear + 1) + "/" + dayOfMonth)) {
                                tvToDate.setText(year
                                        + "/"
                                        + ((monthOfYear + 1) < 10 ? "0"
                                        + (monthOfYear + 1)
                                        : (monthOfYear + 1))
                                        + "/"
                                        + ((dayOfMonth) < 10 ? "0"
                                        + (dayOfMonth) : (dayOfMonth)));
                                toDate = year + "/" + ((monthOfYear + 1)) + "/"
                                        + ((dayOfMonth));
                            }
                        }
                    }, mToYear, mToMonth, mToDay);
            dpd1.setButton(DatePickerDialog.BUTTON_POSITIVE, "ok", dpd1);
            dpd1.show();
        }
    }

    private void updateReasons(int lid) {
        ArrayAdapter<AttendanceBO> reasonAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        reasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        for (int j = 0; j < attendanceHelper.getReasonList().size(); ++j) {
            if (attendanceHelper.getReasonList().get(j).getAtd_PLId() == lid)
                reasonAdapter.add(attendanceHelper.getReasonList().get(
                        j));
        }
        if (reasonAdapter.isEmpty()) {
            leaveReasonTextView.setVisibility(View.INVISIBLE);
            leaveReasonSpinner.setVisibility(View.INVISIBLE);
            atd_id = lid;
        }

        leaveReasonSpinner.setAdapter(reasonAdapter);
        leaveReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {

                AttendanceBO reString = (AttendanceBO) arg0.getSelectedItem();
                atd_id = reString.getAtd_PLId();
                reason_id = reString.getAtd_Lid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {


            }
        });
    }

    public void showAlert(String title, String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok,
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if (idd == -1) {
                            attendanceHelper.saveAttendanceDetails(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), atd_id,
                                    reason_id, tvFromDate.getText().toString(),
                                    tvToDate.getText().toString(), atd_code, getActivity());

                            if (!bmodel.configurationMasterHelper.IS_ATTENDANCE_SYNCUPLOAD) {
                                Activity activity = getActivity();
                                if(activity != null && isAdded())
                                showUploadAlert(
                                        getResources().getString(R.string.attend),
                                        getResources().getString(
                                                R.string.saved_successfully), 1);
                            } else {
                                new UploadAttendance().execute();
                            }

                        }
                    }

                });
        builder.setNegativeButton(R.string.cancel,
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (idd == -1) {
                            leaveSpinner.setSelection(0);
                            leaveReasonTextView.setVisibility(View.INVISIBLE);
                            leaveReasonSpinner.setVisibility(View.INVISIBLE);
                        }
                        dialog.dismiss();
                    }

                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void showUploadAlert(String title, String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok,
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if (idd == 1) {

                            moveToHomeScreenActivity();
                            getActivity().finish();
                          /*  if (bmodel.configurationMasterHelper.SHOW_ATTENDANCE) {
                                //bmodel.loadDashBordHome();
                                BusinessModel.loadActivity(
                                        getActivity(),
                                        DataMembers.actHomeScreen);
                                getActivity().finish();
                            } else {
                                BusinessModel.loadActivity(
                                        getActivity(),
                                        DataMembers.actHomeScreen);
                                getActivity().finish();
                            }*/
                        } else if (idd == 2) {
                            getActivity().finish();
                            try {
                                android.os.Process
                                        .killProcess(android.os.Process.myPid());
                            } catch (Exception e) {
                                Commons.printException(e);
                            }
                        }

                    }

                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );
        bmodel.applyAlertDialogTheme(builder);
    }

    private void moveToHomeScreenActivity(){

      Intent  myIntent = new Intent(getActivity(), HomeScreenActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(myIntent, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_homescreen, menu);
        menu.findItem(R.id.menu_back).setVisible(false);
        if (bmodel.configurationMasterHelper.SHOW_ATTENDANCE) {
            menu.findItem(R.id.menu_back).setVisible(true);
        }
        menu.findItem(R.id.menu_notification).setVisible(false);
        menu.findItem(R.id.menu_device_status).setVisible(false);
        menu.findItem(R.id.menu_about).setVisible(false);
        menu.findItem(R.id.menu_setting).setVisible(false);
        menu.findItem(R.id.menu_feedback).setVisible(false);
        menu.findItem(R.id.menu_chat).setVisible(false);
        menu.findItem(R.id.menu_pswd).setVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
           /* BusinessModel.loadActivity(getActivity(),
                    DataMembers.actHomeScreen);*/
            moveToHomeScreenActivity();
            getActivity().finish();

            return true;
        } else if (i == R.id.menu_back) {
            showUploadAlert(getResources().getString(R.string.attend),
                    getResources().getString(R.string.do_u_want_logout), 2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    private boolean checkDate(String fromDate, String toDate) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            Date date1 = formatter.parse(fromDate);

            Date date2 = formatter.parse(toDate);

            if (date2.after(date1) || fromDate.equals(toDate)) {
                return true;
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.datevalidationmsg),
                        Toast.LENGTH_LONG).show();
                return false;
            }

        } catch (Exception e1) {
            Commons.printException("" + e1);
        }

        return true;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

        }
    };

    class UploadAttendance extends AsyncTask<Void, Void, Integer> {
        protected void onPreExecute() {

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            customProgressDialog(mBuilder, getResources().getString(R.string.uploading_data));
            mAlertDialog = mBuilder.create();
            mAlertDialog.show();

        }

        @Override
        protected Integer doInBackground(Void... params) {

            UploadHelper mUploadHelper = UploadHelper.getInstance(getActivity());
            return mUploadHelper.uploadUsingHttp(getHandler(), DataMembers.ATTENDANCE_UPLOAD, getActivity().getApplicationContext());
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            mAlertDialog.dismiss();

            if (result == 1) {
                showUploadAlert(getResources().getString(R.string.attend),
                        getResources()
                                .getString(R.string.successfully_uploaded), 1);
            } else if (result == 2) {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.upload_failed_please_try_again), 0);
            }

        }

    }
}
