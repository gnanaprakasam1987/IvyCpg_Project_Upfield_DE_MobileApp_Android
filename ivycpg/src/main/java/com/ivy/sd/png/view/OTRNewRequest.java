package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonFieldBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class OTRNewRequest extends IvyBaseActivityNoActionBar implements OnClickListener {


    // Global Varaibles
    private static String outPutDateFormat;
    private static Button btnFromDate;
    private static Button btnToDate;
    /**
     * this dialog used to allocate and show batchwise records
     */
    BatchAllocationDialog dialog;
    /**
     * this dialog used to show progress while save
     */
    ProgressDialog pd;
    //
    RadioButton rb_single, rb_multipple;
    RadioGroup radioGroup;
    TextView txt_fromDate, txt_toDate;
    boolean isSingleDay = true;
    private Toolbar toolbar;
    //private ProgressDialog progressDialogue;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private BusinessModel bmodel;
    private ArrayList<NonFieldBO> nonFieldList = new ArrayList<NonFieldBO>();
    private ArrayList<NonFieldBO> lstRadioBtns = new ArrayList<NonFieldBO>();
    private ArrayAdapter<NonFieldBO> spinnerAdapter, subReasonAdapter;
    private int session, childReasonId, parentReasonId;
    private String reasonName = "", reasondesc = "";
    // Views
    private Button btnAdd, btnCancel;
    private Spinner spnReason;
    private EditText edt_descr;
    private NonFieldBO nonfieldBO;
    private RadioGroup rdgrp;
    private LinearLayout remarklayout,ll_todate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otr_new_request);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            TextView toolBarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
            toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            toolBarTitle.setText("New Request");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        rb_single = (RadioButton) findViewById(R.id.rb_single);
        rb_single.setChecked(true);
        rb_multipple = (RadioButton) OTRNewRequest.this.findViewById(R.id.rb_multiple);

        txt_fromDate
                = (TextView) OTRNewRequest.this.findViewById(R.id.txt_fromDate);
        txt_toDate
                = (TextView) OTRNewRequest.this.findViewById(R.id.txt_to_Date);


        spnReason = (Spinner) OTRNewRequest.this.findViewById(R.id.spn_resn);


        edt_descr = (EditText) OTRNewRequest.this.findViewById(R.id.edt_reason);
        btnFromDate = (Button) OTRNewRequest.this.findViewById(R.id.txt_fromDateVal);
        btnToDate = (Button) OTRNewRequest.this.findViewById(R.id.txt_toDateVAl);
        btnAdd = (Button) OTRNewRequest.this.findViewById(R.id.btn_add);
        btnCancel = (Button) OTRNewRequest.this.findViewById(R.id.bt_Cancel);

        rdgrp = (RadioGroup) OTRNewRequest.this.findViewById(R.id.rdGrp_session);
        remarklayout = (LinearLayout) OTRNewRequest.this.findViewById(R.id.ll_descr);
        ll_todate = (LinearLayout) OTRNewRequest.this.findViewById(R.id.ll_todate);
        edt_descr.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().equals(""))
                    reasondesc = s.toString();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_single) {
                    txt_fromDate.setText(R.string.date);
                    txt_toDate.setVisibility(View.GONE);
                    btnToDate.setVisibility(View.GONE);
                    ll_todate.setVisibility(View.GONE);
                    isSingleDay = true;
                    creatDynamicRadioButton(true);
                    // initializeFields();
                } else {
                    txt_fromDate.setText(R.string.from);
                    txt_toDate.setVisibility(View.VISIBLE);
                    btnToDate.setVisibility(View.VISIBLE);
                    ll_todate.setVisibility(View.VISIBLE);
                    isSingleDay = false;
                    creatDynamicRadioButton(false);
                    // initializeFields();

                }
            }
        });
        btnFromDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFromDate.setTag("datePicker1");
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(OTRNewRequest.this.getSupportFragmentManager(), "datePicker1");
            }
        });
        btnToDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnToDate.setTag("datePicker2");
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(OTRNewRequest.this.getSupportFragmentManager(), "datePicker2");
            }
        });

        creatDynamicRadioButton(true);
        initializeFields();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_batch, menu);


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Change color if Filter is selected


        menu.findItem(R.id.menu_next).setVisible(false);


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backButtonClick() {

//        Intent i = new Intent(OTRNewRequest.this, HomeScreenActivity.class);
//        startActivity(i);
        OTRNewRequest.this.finish();
//        NonFieldFragment mFragment = null;
//        mFragment = new NonFieldFragment();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.fragment_content, mFragment).commit();
//    OTRNewRequest.this.finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        // load spinner
        spinnerAdapter = new ArrayAdapter<NonFieldBO>(this,
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (NonFieldBO nonField : bmodel.mAttendanceHelper
                .getNonFieldReasonList()) {
            Commons.print("sdfsa" + nonField.getReason());
            if (nonField.getpLevelId() == 0)
                spinnerAdapter.add(nonField);
        }

        spnReason.setAdapter(spinnerAdapter);
        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
//        btnToDate.setOnClickListener(this);
//        btnFromDate.setOnClickListener(this);

        spnReason.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                NonFieldBO reString = (NonFieldBO) arg0.getSelectedItem();
                reasonName = reString.getReason();
                if (reString.getReason().equalsIgnoreCase("others") || reString.getReason().equalsIgnoreCase("other"))
                    remarklayout.setVisibility(View.VISIBLE);
                else
                    remarklayout.setVisibility(View.GONE);
                if (position != 0) {
                    if (reString.getIsRequired() == 1) {

                        updateReasons(reString.getReasonID());

                    } else {

                        parentReasonId = reString.getReasonID();
                    }
                } else {

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    // Update Reason
    public void updateReasons(int plid) {
        subReasonAdapter = new ArrayAdapter<NonFieldBO>(this,
                android.R.layout.simple_spinner_item);
        subReasonAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (NonFieldBO fieldBo : bmodel.mAttendanceHelper
                .getNonFieldReasonList()) {
            if (fieldBo.getpLevelId() == plid) {
                subReasonAdapter.add(fieldBo);
            }
        }

        if (subReasonAdapter.isEmpty()) {

            parentReasonId = plid;
        }


    }

    // Dynamically create Radio Button inside the RadioGroup, Radio button size
    // comes from Sync
    public void creatDynamicRadioButton(boolean isSingle) {
        lstRadioBtns = bmodel.mAttendanceHelper.getRadioButtonNames();
        rdgrp.removeAllViews();
        for (int i = 0; i < lstRadioBtns.size(); i++) {
            RadioButton rdbtn = new RadioButton(this
                    .getApplicationContext());
            rdbtn = new RadioButton(this);
            rdbtn.setTextColor(Color.BLACK);

            if (isSingle) {
                rdbtn.setId(i);
                rdbtn.setText(lstRadioBtns.get(i).getSession());
                if (i == 0) {
                    rdbtn.setChecked(true);
                    session = lstRadioBtns.get(i).getsessionID();
                }
                rdgrp.addView(rdbtn);


            } else {
                if (lstRadioBtns.get(i).getSessionCode().equals("FD")) {
                    rdbtn.setId(i);
                    rdbtn.setTextColor(Color.BLACK);
                    rdbtn.setText(lstRadioBtns.get(i).getSession());
                    // if (i == 0) {
                    rdbtn.setChecked(true);
                    session = lstRadioBtns.get(i).getsessionID();
                    //}
                    rdgrp.addView(rdbtn);
                }
            }
        }
    }



    public void initializeFields() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        btnFromDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                calendar.getTime(), outPutDateFormat));
        btnToDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                calendar.getTime(), outPutDateFormat));
        /*edt_descr.setText("");
        reasondesc="";
        spnReason.setSelection(0);*/
       /* if (lstRadioBtns.size() > 0) {
            RadioButton rb = (RadioButton) getView().findViewById(0);
            rb.setChecked(true);
        }*/
    }

    /* // Dynamically create Radio Button inside the RadioGroup, Radio button size
     // comes from Sync
     public void creatDynamicRadioButton() {
         lstRadioBtns = bmodel.mAttendanceHelper.getRadioButtonNames();

         for (int i = 0; i < lstRadioBtns.size(); i++) {
             RadioButton rdbtn = new RadioButton(getActivity()
                     .getApplicationContext());
             rdbtn.setId(i);
             rdbtn.setTextColor(Color.BLACK);
             rdbtn.setText(lstRadioBtns.get(i).getSession());
             if (i == 0) {
                 rdbtn.setChecked(true);
                 session = lstRadioBtns.get(i).getsessionID();
             }
             rdgrp.addView(rdbtn);
         }

         rdgrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(RadioGroup group, int checkedId) {
                 // radioBtnPosition=rdgrp.indexOfChild(getView().findViewById(checkedId));
                 RadioButton rb = (RadioButton) getView()
                         .findViewById(checkedId);
                 session = bmodel.mAttendanceHelper.getSessionID(rb.getText()
                         .toString());

             }
         });

     }

     public void initializeFields() {

         btnFromDate.setText(DateUtil.convertDateStringFormat(
                 SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
         btnToDate.setText(DateUtil.convertDateStringFormat(
                 SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
         edt_descr.setText("");
         reasondesc="";
         spnReason.setSelection(0);
         if (lstRadioBtns.size() > 0) {
             RadioButton rb = (RadioButton) getView().findViewById(0);
             rb.setChecked(true);
         }
     }
 */
    @Override
    public void onClick(View v) {
        Button btnView = (Button) v;
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
//        if (btnView == btnCancel)
//            dismiss();
//
//        else
        if (btnView == btnAdd) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date toDate;
            Date frmDate;
            Commons.print("reason" + reasonName + reasondesc);
            try {
                String btnTempToDate = "";
                if (isSingleDay) {
                    btnTempToDate = btnFromDate.getText().toString();
                } else {
                    btnTempToDate = btnToDate.getText().toString();
                }

                toDate = sdf.parse(btnTempToDate);
                frmDate = sdf.parse(btnFromDate.getText().toString());
                if ((toDate.after(frmDate) || toDate.equals(frmDate))
                        && parentReasonId != 0) {

                    nonfieldBO = new NonFieldBO();
                    nonfieldBO.setFrmDate(btnFromDate.getText() + "");
                    if (!isSingleDay) {
                        nonfieldBO.setToDate(btnToDate.getText() + "");
                    } else {
                        nonfieldBO.setToDate(btnFromDate.getText() + "");
                    }
                    nonfieldBO.setSessionID(session);
                    nonfieldBO.setReasonID(parentReasonId);
                    nonfieldBO.setSubReasonId(childReasonId);
                    nonfieldBO.setReason(reasonName);
                    nonfieldBO.setDescription(reasondesc);
                    nonFieldList.add(nonfieldBO);

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
        }
//        else if (btnView == btnFromDate) {
//            btnFromDate.setTag("datePicker1");
//            DialogFragment newFragment = new DatePickerFragment();
//            newFragment.show(this.getSupportFragmentManager(), "datePicker1");
//        } else if (btnView == btnToDate) {
//            btnToDate.setTag("datePicker2");
//            DialogFragment newFragment = new DatePickerFragment();
//            newFragment.show(this.getSupportFragmentManager(), "datePicker2");
//        }

    }



    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            // From Date
            Calendar calendar = Calendar.getInstance();
            if (selectedDate.getTimeInMillis() > calendar.getTimeInMillis()) {
                if (this.getTag().equals("datePicker1")) {

                    btnFromDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));

                    // To Date Picker
                } else if (this.getTag().equals("datePicker2")) {
                    btnToDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else {
                Toast.makeText(getActivity(), R.string.pls_select_feature_date, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Save the Non Field Work Details
    class SaveNonFieldData extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.mAttendanceHelper.setNonFieldList(nonFieldList);
                bmodel.mAttendanceHelper.saveNonFieldWorkDetails();

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
					true, false);*/

            builder = new AlertDialog.Builder(OTRNewRequest.this);

            customProgressDialog(builder,  getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            //progressDialogue.dismiss();

            alertDialog.dismiss();
            Toast.makeText(OTRNewRequest.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            backButtonClick();

//            getTargetFragment().onActivityResult(getTargetRequestCode(),
//                    Activity.RESULT_OK, this.getIntent());
//            dismiss();
        }

    }

}
