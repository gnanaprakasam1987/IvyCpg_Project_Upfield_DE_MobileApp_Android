package com.ivy.cpg.view.nonfield;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LeaveRuleBO;
import com.ivy.sd.png.bo.LeaveSpinnerBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class NonFieldFragment extends IvyBaseActivityNoActionBar implements OnClickListener {
    private final static String outPutDateFormat = "yyyy/MM/dd";
    private final ArrayList<NonFieldBO> nonFieldnewList = new ArrayList<>();
    Toolbar toolbar;
    private RadioGroup rdgrp;
    private static BusinessModel bmodel;
    private TextView txt_fromDate;
    private TextView txt_toDate;
    private static TextView txt_total_value;
    private static Button btn_frmDate;
    private static Button btn_toDate;
    private LinearLayout remarklayout;
    private LinearLayout ll_session;
    private int parentReasonId;
    private int jointUserId;
    private static int session;
    private static int leaveTypeLovId = 0;
    private Button btn_add;
    private Button btn_traveltime;
    private NonFieldBO nonfieldBO;
    private String reasonName = "";
    private String reasondesc = "";
    private AlertDialog alertDialog;
    private static boolean isSingleDay = true;
    //for the puspose of hiding session to show only full day
    private boolean isAnnual = false;
    //if Leave selected to make end date selected after start date picked
    private static boolean isLeave = false;
    private static boolean isRuleAvailable = false;
    private LinearLayout ll_users;
    private LinearLayout ll_leaves;
    private LinearLayout ll_traveltime;
    private LinearLayout ll_total;
    private LinearLayout ll_dummy;
    private int hour;
    private int minute;
    private static String select;
    private static NonFieldHelper nonFieldHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otr_new_request);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        nonFieldHelper = NonFieldHelper.getInstance(this);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.new_request));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            new NonFieldActivity().passData(null);
            NonFieldFragment.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();

        RadioButton rb_single, rb_multiple;

        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        rb_single = findViewById(R.id.rb_single);
        rb_single.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        rb_multiple = findViewById(R.id.rb_multiple);
        rb_multiple.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        txt_fromDate
                = findViewById(R.id.txt_fromDate);
        txt_toDate
                = findViewById(R.id.txt_to_Date);
        txt_fromDate.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        txt_toDate.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        btn_frmDate = findViewById(R.id.txt_fromDateVal);
        btn_frmDate.setOnClickListener(this);
        btn_frmDate.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        btn_toDate = findViewById(R.id.txt_toDateVAl);
        btn_toDate.setOnClickListener(this);
        btn_toDate.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        btn_traveltime = findViewById(R.id.btn_traveltime);
        btn_traveltime.setOnClickListener(this);
        rdgrp = findViewById(R.id.rdGrp_session);
        btn_add = findViewById(R.id.btn_add);

        if (!NonFieldActivity.isSaved) {
            rb_single.setChecked(true);
            txt_fromDate.setText(R.string.date);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(btn_add.getTag()) != null)
                btn_add.setText(bmodel.labelsMasterHelper.applyLabels(btn_add.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        btn_add.setOnClickListener(this);
        btn_add.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        Spinner spnReason = findViewById(R.id.spn_resn);
        remarklayout = findViewById(R.id.ll_descr);
        EditText edt_descr = findViewById(R.id.edt_reason);
        Spinner spn_users = findViewById(R.id.spn_joint);
        Spinner spn_leaves = findViewById(R.id.spn_leaves);
        ll_users = findViewById(R.id.ll_joint);
        ll_leaves = findViewById(R.id.ll_leaves);
        ll_traveltime = findViewById(R.id.ll_travel_time);
        ll_total = findViewById(R.id.ll_total);
        ll_session = findViewById(R.id.ll_session);

        txt_total_value = findViewById(R.id.txt_total_value);

        TextView txt_reason = findViewById(R.id.txt_reason);
//        txt_reason.setBackgroundColor(ContextCompat.getColor(this,(R.color.list_odd_item_bg)));
        TextView txt_joint = findViewById(R.id.txt_joint);
        TextView txt_leaves_type = findViewById(R.id.txt_leaves_type);
        TextView per_txt = findViewById(R.id.per_txt);
        TextView txt_session = findViewById(R.id.txt_session);
        TextView txt_traveltime = findViewById(R.id.txt_traveltime);
        TextView txt_Descr = findViewById(R.id.txt_Descr);

        try {
            if (bmodel.labelsMasterHelper.applyLabels(per_txt.getTag()) != null)
                per_txt.setText(bmodel.labelsMasterHelper.applyLabels(per_txt.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        TextView txt_total = findViewById(R.id.txt_total);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(txt_total.getTag()) != null)
                txt_total.setText(bmodel.labelsMasterHelper.applyLabels(txt_total.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        txt_reason.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        txt_joint.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        txt_leaves_type.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        per_txt.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        txt_session.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        txt_traveltime.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        txt_Descr.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        select = getResources().getString(R.string.select);
        ll_dummy = findViewById(R.id.ll_dummy);

        try {
            if (bmodel.labelsMasterHelper.applyLabels(txt_leaves_type.getTag()) != null)
                txt_leaves_type.setText(bmodel.labelsMasterHelper.applyLabels(txt_leaves_type.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (!NonFieldActivity.isSaved) {
            edt_descr.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // no operation
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // no operation
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!"".equals(s.toString()))
                        reasondesc = s.toString();
                }
            });


            if (radioGroup != null) {
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.rb_single) {
                            txt_fromDate.setText(R.string.date);
                            txt_toDate.setVisibility(View.GONE);
                            btn_toDate.setVisibility(View.GONE);
                            isSingleDay = true;

                            creatDynamicRadioButton(!isAnnual);

                            initializeFields();
                        } else {
                            txt_fromDate.setText(R.string.from);
                            txt_toDate.setVisibility(View.VISIBLE);
                            btn_toDate.setVisibility(View.VISIBLE);
                            isSingleDay = false;
                            creatDynamicRadioButton(false);
                            initializeFields();

                        }
                    }
                });
            }


            ArrayAdapter<NonFieldBO> spinnerAdapter = new ArrayAdapter<>(
                    this, R.layout.spinner_bluetext_layout);
            spinnerAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

            for (NonFieldBO nonField : nonFieldHelper
                    .getNonFieldReasonList()) {
                Commons.print("sdfsa" + nonField.getReason());
                if (nonField.getpLevelId() == 0)
                    spinnerAdapter.add(nonField);
            }


            ArrayAdapter<UserMasterBO> userMasterBOArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_bluetext_layout);
            userMasterBOArrayAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

//        bmodel.userMasterHelper.downloadJoinCallusers();
            bmodel.userMasterHelper.downloadDistributionDetails();
            for (UserMasterBO bo : bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList()) {
                userMasterBOArrayAdapter.add(bo);
            }
            if (bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList().size() > 0)
                spn_users.setSelection(0);
            spn_users.setAdapter(userMasterBOArrayAdapter);
            spn_users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    jointUserId = bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList().get(i).getUserid();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // no operation
                }
            });

            ArrayAdapter<LeaveSpinnerBO> leavesSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_bluetext_layout);
            leavesSpinnerAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


            ArrayList<LeaveSpinnerBO> leavesTypes = nonFieldHelper.getLeavesTypeList();

            for (LeaveSpinnerBO lBo : leavesTypes) {
                leavesSpinnerAdapter.add(lBo);
            }

            if (!leavesTypes.isEmpty())
                spn_leaves.setSelection(0);

            spn_leaves.setAdapter(leavesSpinnerAdapter);
            spn_leaves.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View view, int i, long l) {
                    LeaveSpinnerBO leavesString = (LeaveSpinnerBO) arg0.getSelectedItem();
                    isAnnual = "ANNUAL".equalsIgnoreCase(leavesString.getSpinnerTxt());

                    if (isSingleDay)
                        creatDynamicRadioButton(!isAnnual);
                    else
                        creatDynamicRadioButton(false);

                    leaveTypeLovId = leavesString.getId();
                    initializeFields();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // no operation
                }
            });

            spnReason.setAdapter(spinnerAdapter);
            spnReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    NonFieldBO reString = (NonFieldBO) arg0.getSelectedItem();
                    reasonName = reString.getReason();
                    if ("others".equalsIgnoreCase(reString.getReason()) || "other".equalsIgnoreCase(reString.getReason()))
                        remarklayout.setVisibility(View.VISIBLE);
                    else
                        remarklayout.setVisibility(View.GONE);
                    if (position != 0) {
                        if (reString.getIsRequired() != 1) {
                            parentReasonId = reString.getReasonID();
                        }
                    } else {
                        parentReasonId = 0;
                    }

                    if ("JOINTUSER".equalsIgnoreCase(reString.getCode())) {
                        ll_users.setVisibility(View.VISIBLE);
                        ll_dummy.setVisibility(View.GONE);
                        ll_leaves.setVisibility(View.GONE);
                    } else {
                        ll_users.setVisibility(View.GONE);
                        //ll_dummy.setVisibility(View.INVISIBLE);
                        jointUserId = 0;
                    }

                    if ("LEAVE".equalsIgnoreCase(reString.getCode())) {
                        ll_leaves.setVisibility(View.VISIBLE);
                        ll_users.setVisibility(View.GONE);
                        ll_dummy.setVisibility(View.GONE);
                        ll_traveltime.setVisibility(View.GONE);
                        isLeave = true;
                        initializeFields();
                    } else {
                        ll_leaves.setVisibility(View.GONE);
                        //ll_dummy.setVisibility(View.INVISIBLE);
                        isLeave = false;
                        initializeFields();
                    }

                    if ("JOINTUSER".equalsIgnoreCase(reString.getCode())) {
                        ll_users.setVisibility(View.VISIBLE);
                        ll_dummy.setVisibility(View.GONE);
                        ll_leaves.setVisibility(View.GONE);

                    } else if ("LEAVE".equalsIgnoreCase(reString.getCode())) {
                        ll_users.setVisibility(View.GONE);
                        ll_dummy.setVisibility(View.GONE);
                        ll_leaves.setVisibility(View.VISIBLE);

                    } else {
                        ll_users.setVisibility(View.GONE);
                        ll_dummy.setVisibility(View.INVISIBLE);
                        ll_leaves.setVisibility(View.GONE);
                    }


                    if ("TRAVELTIME".equalsIgnoreCase(reString.getCode())) {
                        ll_traveltime.setVisibility(View.VISIBLE);
                        btn_traveltime.setText("0.0");
                    }

//                else {
//                    ll_traveltime.setVisibility(View.GONE);
//                }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // no operation
                }
            });

            if (rdgrp.getChildCount() <= 0) {
                creatDynamicRadioButton(true);
                initializeFields();
            }
        }
    }

    // Dynamically create Radio Button inside the RadioGroup, Radio button size
    // comes from Sync
    private void creatDynamicRadioButton(final boolean isSingle) {
        ArrayList<NonFieldBO> lstRadioBtns = nonFieldHelper.getRadioButtonNames();

        rdgrp.removeAllViews();

        if (!lstRadioBtns.isEmpty()) {
            ll_session.setVisibility(View.VISIBLE);


            for (int i = 0; i < lstRadioBtns.size(); i++) {
                AppCompatRadioButton rdbtn = new AppCompatRadioButton(this);
//                rdbtn = (RadioButton) View.inflate(this, R.layout.radio_btn_leave_session, null);
                if (isSingle) {
                    rdbtn.setId(i);
                    rdbtn.setTextColor(ContextCompat.getColor(this, R.color.half_Black));
                    rdbtn.setText(lstRadioBtns.get(i).getSession());
                    rdbtn.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                    if (i == 0) {
                        rdbtn.setChecked(true);
                        session = lstRadioBtns.get(i).getsessionID();
                    }


                    rdgrp.addView(rdbtn);
                } else {
                    if ("FD".equals(lstRadioBtns.get(i).getSessionCode())) {
                        rdbtn.setId(i);
                        rdbtn.setTextColor(ContextCompat.getColor(this, R.color.half_Black));
                        rdbtn.setText(lstRadioBtns.get(i).getSession());
                        rdbtn.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                        rdbtn.setChecked(true);
                        session = lstRadioBtns.get(i).getsessionID();
                        rdgrp.addView(rdbtn);

                    }
                }
            }
        } else {
            ll_session.setVisibility(View.GONE);
            session = 0;
        }

        rdgrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = findViewById(checkedId);

                if (rb != null)
                    session = nonFieldHelper.getSessionID(rb.getText()
                            .toString());
                if (isLeave && isSingle && !btn_frmDate.getText().equals(getResources().getString(R.string.select))) {
                    if (isRuleAvailable) {
                        nonFieldHelper.computeLeaves(leaveTypeLovId,
                                DateTimeUtils.convertFromServerDateToRequestedFormat(btn_frmDate.getText().toString(), outPutDateFormat),
                                DateTimeUtils.convertFromServerDateToRequestedFormat(btn_frmDate.getText().toString(), outPutDateFormat), 0, session, getApplicationContext());
                        ArrayList<LeaveRuleBO> multipleLeaves = nonFieldHelper.getLeavesBo();
                        if (!multipleLeaves.isEmpty()) {
                            if (multipleLeaves.get(0).isAvailable()) {
                                String strLeaves = multipleLeaves.get(0).getAppliedDays() + "";
                                txt_total_value.setText(strLeaves);
                            } else {
                                Toast.makeText(NonFieldFragment.this, getResources().getString(R.string.text_leave_notavailable), Toast.LENGTH_SHORT).show();
                                txt_total_value.setText("0");
                            }
                        }
                    } else {
                        String strSessionLeaveId = nonFieldHelper.getSessionLeaveById(session) + "";
                        txt_total_value.setText(strSessionLeaveId);
                    }
                }
            }
        });
    }

    private void initializeFields() {
        Calendar calendar = Calendar.getInstance();
        txt_total_value.setText("0");
        if (isLeave) {
            btn_frmDate.setText(getResources().getString(R.string.select));
            btn_toDate.setText(getResources().getString(R.string.select));
            ll_total.setVisibility(View.VISIBLE);
        } else {
            btn_frmDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                    calendar.getTime(), outPutDateFormat));
            btn_toDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                    calendar.getTime(), outPutDateFormat));
            ll_total.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Button btnView = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (btnView == btn_add) {
            String btnTempToDate;
            if (isSingleDay) {
                btnTempToDate = btn_frmDate.getText().toString();
            } else {
                btnTempToDate = btn_toDate.getText().toString();
            }
            if ((leaveTypeLovId != 0 && isLeave) ||
                    (!btnTempToDate.equals(getResources().getString(R.string.select))
                            && !btn_frmDate.getText().toString().equals(getResources().getString(R.string.select)))) {
                if (!nonFieldHelper.getCheckAlreadyApplied(parentReasonId, btn_frmDate.getText().toString(), btnTempToDate, session, getApplicationContext())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    Date toDate;
                    Date frmDate;
                    Commons.print("reason" + reasonName + reasondesc);
                    try {
                        toDate = sdf.parse(btnTempToDate);
                        frmDate = sdf.parse(btn_frmDate.getText().toString());
                        if ((toDate.after(frmDate) || toDate.equals(frmDate))
                                && parentReasonId != 0) {

                            nonfieldBO = new NonFieldBO();
                            nonfieldBO.setFrmDate(btn_frmDate.getText() + "");
                            if (!isSingleDay) {
                                nonfieldBO.setToDate(btn_toDate.getText() + "");
                            } else {
                                nonfieldBO.setToDate(btn_frmDate.getText() + "");
                            }
                            nonfieldBO.setSessionID(session);
                            nonfieldBO.setReasonID(parentReasonId);
                            nonfieldBO.setReason(reasonName);
                            nonfieldBO.setDescription(reasondesc);
                            nonfieldBO.setJointUserId(jointUserId);
                            nonfieldBO.setLeaveLovId(leaveTypeLovId);
                            //server has small int so 0.5 not allowed so if 0.5 changed to 1
                            //In server side will convert to 1 based on session id AN od FN
                            double leave = SDUtil.convertToDouble(txt_total_value.getText().toString());
                            if (leave < 1)
                                leave = 1;

                            nonfieldBO.setTotalDays(leave);
                            nonfieldBO.setTimeSpent(btn_traveltime.getText().toString());
                            nonFieldnewList.clear();
                            nonFieldnewList.add(nonfieldBO);

                            new SaveNonFieldData().execute();
                        } else {
                            if (parentReasonId == 0)
                                Toast.makeText(this,
                                        getResources().getString(R.string.select_reason),
                                        Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(this,
                                        getResources().getString(R.string.invaliddate),
                                        Toast.LENGTH_LONG).show();
                        }
                    } catch (ParseException e) {
                        Commons.printException(e);
                    }
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.selected_date_already_applied), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (leaveTypeLovId == 0 && isLeave)
                    Toast.makeText(this, getResources().getString(R.string.text_select_leavetype), Toast.LENGTH_SHORT).show();
                else if (btnTempToDate.equals(getResources().getString(R.string.select))) {
                    if (isSingleDay)
                        Toast.makeText(this, getResources().getString(R.string.text_select_date), Toast.LENGTH_SHORT).show();
                    else {
                        if (btn_frmDate.getText().toString().equals(getResources().getString(R.string.select))) {
                            Toast.makeText(this, getResources().getString(R.string.text_select_fromdate), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.text_select_todate), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else if (btnView == btn_frmDate) {
            if (isLeave) {
                if (leaveTypeLovId != 0) {
                    btn_frmDate.setTag("datePicker1");
                    com.ivy.lib.DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(NonFieldFragment.this.getSupportFragmentManager(), "datePicker1");
                } else
                    Toast.makeText(this, getResources().getString(R.string.text_select_leavetype), Toast.LENGTH_SHORT).show();
            } else {
                btn_toDate.setTag("datePicker1");
                com.ivy.lib.DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(NonFieldFragment.this.getSupportFragmentManager(), "datePicker1");
            }
        } else if (btnView == btn_toDate) {
            if (isLeave) {
                if (btn_frmDate.getText().toString().equals(getResources().getString(R.string.select)))
                    Toast.makeText(this, getResources().getString(R.string.text_select_fromdate), Toast.LENGTH_SHORT).show();
                else {
                    btn_toDate.setTag("datePicker2");
                    com.ivy.lib.DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(NonFieldFragment.this.getSupportFragmentManager(), "datePicker2");
                }
            } else {
                btn_toDate.setTag("datePicker2");
                com.ivy.lib.DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(NonFieldFragment.this.getSupportFragmentManager(), "datePicker2");
            }
        } else if (btnView == btn_traveltime) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, timePickerListener, hour, minute, true);
            timePickerDialog.show();
        }
    }


    // Save the Non Field Work Details
    private class SaveNonFieldData extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                nonFieldHelper.setNonFieldList(nonFieldnewList);
                if (isLeave && isRuleAvailable)
                    nonFieldHelper.saveLeaveDetails(nonfieldBO.getTotalDays(), leaveTypeLovId, getApplicationContext());
                else
                    nonFieldHelper.saveNonFieldWorkDetails(getApplicationContext());

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(NonFieldFragment.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            Toast.makeText(NonFieldFragment.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            if (isLeave)
                initializeFields();
        }
    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends com.ivy.lib.DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            // From Date
            Calendar calendar = Calendar.getInstance();
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            int day1 = calendar.get(Calendar.DAY_OF_MONTH);
            calendar = new GregorianCalendar(year1, month1, day1);
            if (bmodel.configurationMasterHelper.ALLOW_BACK_DATE || selectedDate.getTimeInMillis() >= calendar.getTimeInMillis()) {
                if ("datePicker1".equals(this.getTag())) {
                    if (isLeave) {
                        LeaveRuleBO leaveRuleBO = nonFieldHelper.checkRule(leaveTypeLovId, DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), outPutDateFormat), getActivity().getApplicationContext());
                        if (leaveRuleBO != null) {
                            if (leaveRuleBO.getNoticeDays() == 0 && leaveRuleBO.getEffectiveTo().length() == 0) {
                                isRuleAvailable = false;
                                btn_frmDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                        selectedDate.getTime(), outPutDateFormat));
                                if (!isSingleDay &&
                                        !btn_toDate.getText().toString().equals(getResources().getString(R.string.select)))
                                    updateTotalDays(selectedDate, getActivity().getApplicationContext());

                                if (isSingleDay) {
                                    String strNoOfDays = "" + getNoofDays(btn_frmDate.getText().toString(), btn_frmDate.getText().toString());
                                    txt_total_value.setText(strNoOfDays);
                                }
                            } else {
                                isRuleAvailable = true;
                                if (isSingleDay) {
                                    if (nonFieldHelper.isHoliday(DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), outPutDateFormat),
                                            getActivity().getApplicationContext()))
                                        Toast.makeText(getActivity(), getResources().getString(R.string.text_select_holiday), Toast.LENGTH_SHORT).show();
                                    else if (nonFieldHelper.isWeekOff(DateTimeUtils.convertDateObjectToRequestedFormat(
                                            selectedDate.getTime(), outPutDateFormat)))
                                        Toast.makeText(getActivity(), getResources().getString(R.string.text_select_weekoff), Toast.LENGTH_SHORT).show();
                                    else if (!bmodel.configurationMasterHelper.ALLOW_BACK_DATE && leaveRuleBO.getNoticeDays() > getDifferenceDays(calendar, selectedDate))
                                        Toast.makeText(getActivity(), getResources().getString(R.string.text_initmation_period), Toast.LENGTH_SHORT).show();
                                    else {
                                        nonFieldHelper.computeLeaves(leaveTypeLovId,
                                                DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(),
                                                        outPutDateFormat), DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(),
                                                        outPutDateFormat), 0, session, getActivity().getApplicationContext());

                                        ArrayList<LeaveRuleBO> multipleLeaves = nonFieldHelper.getLeavesBo();
                                        if (!multipleLeaves.isEmpty()) {
                                            if (multipleLeaves.get(0).isAvailable()) {
                                                btn_frmDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                                        selectedDate.getTime(), outPutDateFormat));
                                                String strLeaves = multipleLeaves.get(0).getAppliedDays() + "";
                                                txt_total_value.setText(strLeaves);
                                            } else {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.text_leave_notavailable), Toast.LENGTH_SHORT).show();
                                                txt_total_value.setText("0");
                                            }
                                        }
                                    }
                                } else {
                                    if (!bmodel.configurationMasterHelper.ALLOW_BACK_DATE && leaveRuleBO.getNoticeDays() > getDifferenceDays(calendar, selectedDate)) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.text_initmation_period), Toast.LENGTH_SHORT).show();
                                    } else {
                                        btn_frmDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                                selectedDate.getTime(), outPutDateFormat));
                                        if (!btn_toDate.getText().toString().equals(getResources().getString(R.string.select)))
                                            updateTotalDays(selectedDate, getActivity().getApplicationContext());
                                    }
                                }
                            }
                        }
                    } else {
                        btn_frmDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                    // To Date Picker
                } else if ("datePicker2".equals(this.getTag())) {
                    if (!isRuleAvailable && isLeave) {
                        btn_toDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        String strNoOfDays = "" + getNoofDays(btn_frmDate.getText().toString(), btn_toDate.getText().toString());
                        txt_total_value.setText(strNoOfDays);
                    } else if (isRuleAvailable && isLeave) {
                        nonFieldHelper.computeLeaves(leaveTypeLovId,
                                DateTimeUtils.convertFromServerDateToRequestedFormat(btn_frmDate.getText().toString(), outPutDateFormat),
                                DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), outPutDateFormat),
                                1, session, getActivity().getApplicationContext());
                        ArrayList<LeaveRuleBO> multipleLeaves = nonFieldHelper.getLeavesBo();
                        if (!multipleLeaves.isEmpty()) {
                            boolean isAvailable = true;
                            double total = 0;
                            for (LeaveRuleBO obj : multipleLeaves) {
                                if (!obj.isAvailable()) {
                                    isAvailable = false;
                                    break;
                                } else
                                    total += SDUtil.convertToDouble(obj.getAppliedDays());
                            }

                            if (isAvailable) {
                                btn_toDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                        selectedDate.getTime(), outPutDateFormat));
                                String strTotal = total + "";
                                txt_total_value.setText(strTotal);
                            } else {
                                Toast.makeText(getActivity(), getResources().getString(R.string.text_leave_notavailable), Toast.LENGTH_SHORT).show();
                                txt_total_value.setText("0");
                                btn_toDate.setText(getResources().getString(R.string.select));
                            }
                        }
                    } else {
                        btn_toDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                }
            } else {
                Toast.makeText(getActivity(), R.string.pls_select_feature_date, Toast.LENGTH_LONG).show();
            }
        }
    }


    private final TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int selectedHour,
                                      int selectedMinute) {
                    hour = selectedHour;
                    minute = selectedMinute;
                    String strHour_Minute = hour + ":" + minute;
                    btn_traveltime.setText(strHour_Minute);
                }
            };


    private static void updateTotalDays(Calendar selectedDate, Context context) {
        double total = 0;
        if (!btn_toDate.getText().toString().equals(select)) {
            nonFieldHelper.computeLeaves(leaveTypeLovId,
                    DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), outPutDateFormat),
                    DateTimeUtils.convertFromServerDateToRequestedFormat(btn_toDate.getText().toString(), outPutDateFormat), 1, session, context);
            ArrayList<LeaveRuleBO> multipleLeaves = nonFieldHelper.getLeavesBo();
            for (LeaveRuleBO obj : multipleLeaves) {
                if (!obj.isAvailable()) {
                    break;
                } else
                    total += SDUtil.convertToDouble(obj.getAppliedDays());
            }
            String strTotal = total + "";
            txt_total_value.setText(strTotal);

        }
    }

    private static long getDifferenceDays(Calendar todaydate, Calendar selecteddate) {
        long diff = selecteddate.getTimeInMillis() - todaydate.getTimeInMillis();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return hours / 24;
    }

    private static int getNoofDays(String fromDate, String toDate) {
        int count;
        try {
            ArrayList<String> dates = new ArrayList<>();
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            Date startDate = formatter.parse(fromDate);
            Date endDate = formatter.parse(toDate);
            long interval = 24 * 1000 * 60 * 60; // 1 hour in millis
            long endTime = endDate.getTime(); // create your endtime here, possibly using Calendar or Date
            long curTime = startDate.getTime();
            while (curTime <= endTime) {
                dates.add(formatter.format(new Date(curTime)));
                curTime += interval;
            }
            count = dates.size();
        } catch (Exception e) {
            Commons.printException(e);
            count = 0;
        }

        return count;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new NonFieldActivity().passData(outState);
    }

}
