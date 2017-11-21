package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.TargetPlanBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

public class TargetPlanActivity extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private BusinessModel bmodel;
    private TextView mBase, mSbd, mInit1, mInit2, mInit3, mInit4, mInit5,
            mInit6, mInit7, mInit8, mInit9, mInit10;
    ;
    private EditText mBaseET, mSbdET, mInit1ET, mInit2ET, mInit3ET, mInit4ET,
            mInit5ET, mInit6ET, mInit7ET, mInit8ET, mInit9ET, mInit10ET;
    private TextView mPlanTGT, mSuggestTGT, mStoreProg, mMonth_act_obj,
            mGoldenPointTXT, mTotalLinesTXT, mStoreBalanceTXT, mHvp3mTXT;
    private String append = "";
    private TargetPlanBO targetPlan;
    private RetailerMasterBO retailer;
    private EditText QUANTITY;
    private double tot_value, base, sbd, init1, init2, init3, init4, init5,
            init6, init7, init8, init9, init10, stgt;
    private String calledFrom;
    private final String MENU_REV = "Review";
    private LinearLayout mInit1layout, mInit2layout, mInit3layout,
            mInit4layout, mInit5layout, mInit6layout, mInit7layout,
            mInit8layout, mInit9layout, mInit10layout;
    private RetailerMasterBO retailerObj;

    boolean bool = false;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_target_plan);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.focus_title).getTag()) != null)
                ((TextView) findViewById(R.id.focus_title))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.focus_title)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.mtd_actual).getTag()) != null)
                ((TextView) findViewById(R.id.mtd_actual))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.mtd_actual)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mBase = (TextView) findViewById(R.id.ref_base);
        mSbd = (TextView) findViewById(R.id.ref_sbd);
        mInit1 = (TextView) findViewById(R.id.ref_init1);
        mInit2 = (TextView) findViewById(R.id.ref_init2);
        mInit3 = (TextView) findViewById(R.id.ref_init3);
        mInit4 = (TextView) findViewById(R.id.ref_init4);
        mInit5 = (TextView) findViewById(R.id.ref_init5);
        mInit6 = (TextView) findViewById(R.id.ref_init6);
        mInit7 = (TextView) findViewById(R.id.ref_init7);
        mInit8 = (TextView) findViewById(R.id.ref_init8);

        mInit9 = (TextView) findViewById(R.id.ref_init9);
        mInit10 = (TextView) findViewById(R.id.ref_init10);

        mPlanTGT = (TextView) findViewById(R.id.plan_tgt);
        mSuggestTGT = (TextView) findViewById(R.id.suggest_tgt);
        mStoreProg = (TextView) findViewById(R.id.store_prog);
        mMonth_act_obj = (TextView) findViewById(R.id.mon_act_obj);
        mGoldenPointTXT = (TextView) findViewById(R.id.tv_goldenPoints);
        mHvp3mTXT = (TextView) findViewById(R.id.tv_hvp3m);
        mTotalLinesTXT = (TextView) findViewById(R.id.tv_lines);
        mStoreBalanceTXT = (TextView) findViewById(R.id.tv_store_balance);

        mBaseET = (EditText) findViewById(R.id.edt_base);
        mSbdET = (EditText) findViewById(R.id.edt_sbd);
        mInit1ET = (EditText) findViewById(R.id.edt_init1);
        mInit2ET = (EditText) findViewById(R.id.edt_init2);
        mInit3ET = (EditText) findViewById(R.id.edt_init3);
        mInit4ET = (EditText) findViewById(R.id.edt_init4);
        mInit5ET = (EditText) findViewById(R.id.edt_init5);
        mInit6ET = (EditText) findViewById(R.id.edt_init6);
        mInit7ET = (EditText) findViewById(R.id.edt_init7);
        mInit8ET = (EditText) findViewById(R.id.edt_init8);
        mInit9ET = (EditText) findViewById(R.id.edt_init9);
        mInit10ET = (EditText) findViewById(R.id.edt_init10);

        mInit1layout = (LinearLayout) findViewById(R.id.layout_init1);
        mInit2layout = (LinearLayout) findViewById(R.id.layout_init2);
        mInit3layout = (LinearLayout) findViewById(R.id.layout_init3);
        mInit4layout = (LinearLayout) findViewById(R.id.layout_init4);
        mInit5layout = (LinearLayout) findViewById(R.id.layout_init5);
        mInit6layout = (LinearLayout) findViewById(R.id.layout_init6);
        mInit7layout = (LinearLayout) findViewById(R.id.layout_init7);
        mInit8layout = (LinearLayout) findViewById(R.id.layout_init8);
        mInit9layout = (LinearLayout) findViewById(R.id.layout_init9);
        mInit10layout = (LinearLayout) findViewById(R.id.layout_init10);

        // set visibility to dot button in keypad
        findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        LinearLayout keypad = (LinearLayout) findViewById(R.id.layout_keypad);
        ((Button) findViewById(R.id.targetBack)).setOnClickListener(this);
        Button save = ((Button) findViewById(R.id.targetSave));
        Button remain = (Button) findViewById(R.id.targetRemainder);
        save.setOnClickListener(this);
        remain.setOnClickListener(this);

        setSupportActionBar(toolbar);

        calledFrom = getIntent().getStringExtra("From");

        if (calledFrom == null)
            calledFrom = MENU_REV;

        // Set title to actionbar
        if (calledFrom.equals("Visit"))
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.target_plan));
        else
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.title_dsr_heading));
        getSupportActionBar().setIcon(R.drawable.icon_visit);
        // Used to on/off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((Button) findViewById(R.id.targetBack)).setVisibility(View.GONE);

        if (MENU_REV.equals(calledFrom)) {
            save.setVisibility(View.GONE);
            remain.setVisibility(View.VISIBLE);
            mBaseET.setBackgroundResource(android.R.color.white);
            mSbdET.setBackgroundResource(android.R.color.white);
            mInit1ET.setBackgroundResource(android.R.color.white);
            mInit2ET.setBackgroundResource(android.R.color.white);
            mInit3ET.setBackgroundResource(android.R.color.white);
            mInit4ET.setBackgroundResource(android.R.color.white);
            mInit5ET.setBackgroundResource(android.R.color.white);
            // TextView title = (TextView) findViewById(R.id.title);
            // title.setText(getResources().getString(R.string.review_plan));
            keypad.setVisibility(View.GONE);
        }
        /*
         * mBaseET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString();
		 * 
		 * if (!value.equals("")) { base =
		 * SDUtil.convertToFloat(mBaseET.getText().toString());
		 * targetPlan.setBaseEdit(base); setEditValue(); } else {
		 * mBaseET.setText("0"); } } }); mSbdET.addTextChangedListener(new
		 * TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { sbd =
		 * SDUtil.convertToFloat(mSbdET.getText().toString());
		 * targetPlan.setSbdEdit(sbd); setEditValue();
		 * 
		 * } else { mSbdET.setText("0"); }
		 * 
		 * } }); mInit1ET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { init1 = SDUtil
		 * .convertToFloat(mInit1ET.getText().toString());
		 * targetPlan.setInitBalance1Edit(init1);
		 * 
		 * setEditValue(); } else { mInit1ET.setText("0"); } } });
		 * mInit2ET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { init2 = SDUtil
		 * .convertToFloat(mInit2ET.getText().toString());
		 * targetPlan.setInitBalance2Edit(init2);
		 * 
		 * setEditValue(); } else { mInit2ET.setText("0"); } } });
		 * mInit3ET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { init3 = SDUtil
		 * .convertToFloat(mInit3ET.getText().toString());
		 * targetPlan.setInitBalance3Edit(init3);
		 * 
		 * setEditValue();
		 * 
		 * } else { mInit3ET.setText("0"); } } });
		 * mInit4ET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { init4 = SDUtil
		 * .convertToFloat(mInit4ET.getText().toString());
		 * targetPlan.setInitBalance4Edit(init4);
		 * 
		 * setEditValue(); } else { mInit4ET.setText("0"); }
		 * 
		 * } }); mInit5ET.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { String value =
		 * s.toString(); if (!value.equals("")) { init5 = SDUtil
		 * .convertToFloat(mInit5ET.getText().toString());
		 * targetPlan.setInitBalance5Edit(init5);
		 * 
		 * setEditValue(); } else { mInit5ET.setText("0"); } } });
		 * 
		 * mBaseET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mBaseET; int inType = mBaseET.getInputType();
		 * mBaseET.setInputType(InputType.TYPE_NULL);
		 * mBaseET.onTouchEvent(event); mBaseET.setInputType(inType);
		 * mBaseET.selectAll(); mBaseET.requestFocus(); return true; } });
		 * mSbdET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mSbdET; int inType = mSbdET.getInputType();
		 * mSbdET.setInputType(InputType.TYPE_NULL); mSbdET.onTouchEvent(event);
		 * mSbdET.setInputType(inType); mSbdET.selectAll();
		 * mSbdET.requestFocus(); return true; } });
		 * mInit1ET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mInit1ET; int inType = mInit1ET.getInputType();
		 * mInit1ET.setInputType(InputType.TYPE_NULL);
		 * mInit1ET.onTouchEvent(event); mInit1ET.setInputType(inType);
		 * mInit1ET.selectAll(); mInit1ET.requestFocus(); return true; } });
		 * mInit2ET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mInit2ET; int inType = mInit2ET.getInputType();
		 * mInit2ET.setInputType(InputType.TYPE_NULL);
		 * mInit2ET.onTouchEvent(event); mInit2ET.setInputType(inType);
		 * mInit2ET.selectAll(); mInit2ET.requestFocus(); return true; } });
		 * mInit3ET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mInit3ET; int inType = mInit3ET.getInputType();
		 * mInit3ET.setInputType(InputType.TYPE_NULL);
		 * mInit3ET.onTouchEvent(event); mInit3ET.setInputType(inType);
		 * mInit3ET.selectAll(); mInit3ET.requestFocus(); return true; } });
		 * mInit4ET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mInit4ET; int inType = mInit4ET.getInputType();
		 * mInit4ET.setInputType(InputType.TYPE_NULL);
		 * mInit4ET.onTouchEvent(event); mInit4ET.setInputType(inType);
		 * mInit4ET.selectAll(); mInit4ET.requestFocus(); return true; } });
		 * mInit5ET.setOnTouchListener(new OnTouchListener() { public boolean
		 * onTouch(View v, MotionEvent event) {
		 * 
		 * QUANTITY = mInit5ET; int inType = mInit5ET.getInputType();
		 * mInit5ET.setInputType(InputType.TYPE_NULL);
		 * mInit5ET.onTouchEvent(event); mInit5ET.setInputType(inType);
		 * mInit5ET.selectAll(); mInit5ET.requestFocus(); return true; } });
		 */

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        bmodel = (BusinessModel) getApplicationContext();

        retailerObj = bmodel.getRetailerMasterBO();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        boolean bool = false;

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        retailer = bmodel.getRetailerMasterBO();

        bmodel.targetPlanHelper.downloadTargetPlan();

        targetPlan = bmodel.targetPlanHelper.getTargetplanBO();

        mBaseET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();

                if (!value.equals("")) {
                    base = SDUtil.convertToDouble(mBaseET.getText().toString()
                            .trim());
                    targetPlan.setBaseEdit(base);
                    setEditValue();
                } else {
                    mBaseET.setText("0");
                }
            }
        });
        mSbdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    sbd = SDUtil.convertToFloat(mSbdET.getText().toString());
                    targetPlan.setSbdEdit(sbd);
                    setEditValue();

                } else {
                    mSbdET.setText("0");
                }

            }
        });
        mInit1ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init1 = SDUtil
                            .convertToFloat(mInit1ET.getText().toString());
                    targetPlan.setInitBalance1Edit(init1);

                    setEditValue();
                } else {
                    mInit1ET.setText("0");
                }
            }
        });
        mInit2ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init2 = SDUtil
                            .convertToFloat(mInit2ET.getText().toString());
                    targetPlan.setInitBalance2Edit(init2);

                    setEditValue();
                } else {
                    mInit2ET.setText("0");
                }
            }
        });
        mInit3ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init3 = SDUtil
                            .convertToFloat(mInit3ET.getText().toString());
                    targetPlan.setInitBalance3Edit(init3);

                    setEditValue();

                } else {
                    mInit3ET.setText("0");
                }
            }
        });
        mInit4ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init4 = SDUtil
                            .convertToFloat(mInit4ET.getText().toString());
                    targetPlan.setInitBalance4Edit(init4);

                    setEditValue();
                } else {
                    mInit4ET.setText("0");
                }

            }
        });
        mInit5ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init5 = SDUtil
                            .convertToFloat(mInit5ET.getText().toString());
                    targetPlan.setInitBalance5Edit(init5);

                    setEditValue();
                } else {
                    mInit5ET.setText("0");
                }
            }
        });
        mInit6ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init6 = SDUtil
                            .convertToFloat(mInit6ET.getText().toString());
                    targetPlan.setInit6_edt(init6);

                    setEditValue();
                } else {
                    mInit6ET.setText("0");
                }
            }
        });
        mInit7ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init7 = SDUtil
                            .convertToFloat(mInit7ET.getText().toString());
                    targetPlan.setInit7_edt(init7);

                    setEditValue();
                } else {
                    mInit7ET.setText("0");
                }
            }
        });
        mInit8ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init8 = SDUtil
                            .convertToFloat(mInit8ET.getText().toString());
                    targetPlan.setInit8_edt(init8);

                    setEditValue();
                } else {
                    mInit8ET.setText("0");
                }
            }
        });
        mInit9ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init9 = SDUtil
                            .convertToFloat(mInit9ET.getText().toString());
                    targetPlan.setInit9_edt(init9);

                    setEditValue();
                } else {
                    mInit9ET.setText("0");
                }
            }
        });
        mInit10ET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.equals("")) {
                    init10 = SDUtil.convertToFloat(mInit10ET.getText()
                            .toString());
                    targetPlan.setInit10_edt(init10);

                    setEditValue();
                } else {
                    mInit10ET.setText("0");
                }
            }
        });
        mBaseET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mBaseET;
                int inType = mBaseET.getInputType();
                mBaseET.setInputType(InputType.TYPE_NULL);
                mBaseET.onTouchEvent(event);
                mBaseET.setInputType(inType);
                mBaseET.selectAll();
                mBaseET.requestFocus();
                return true;
            }
        });
        mSbdET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mSbdET;
                int inType = mSbdET.getInputType();
                mSbdET.setInputType(InputType.TYPE_NULL);
                mSbdET.onTouchEvent(event);
                mSbdET.setInputType(inType);
                mSbdET.selectAll();
                mSbdET.requestFocus();
                return true;
            }
        });
        mInit1ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit1ET;
                int inType = mInit1ET.getInputType();
                mInit1ET.setInputType(InputType.TYPE_NULL);
                mInit1ET.onTouchEvent(event);
                mInit1ET.setInputType(inType);
                mInit1ET.selectAll();
                mInit1ET.requestFocus();
                return true;
            }
        });
        mInit2ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit2ET;
                int inType = mInit2ET.getInputType();
                mInit2ET.setInputType(InputType.TYPE_NULL);
                mInit2ET.onTouchEvent(event);
                mInit2ET.setInputType(inType);
                mInit2ET.selectAll();
                mInit2ET.requestFocus();
                return true;
            }
        });
        mInit3ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit3ET;
                int inType = mInit3ET.getInputType();
                mInit3ET.setInputType(InputType.TYPE_NULL);
                mInit3ET.onTouchEvent(event);
                mInit3ET.setInputType(inType);
                mInit3ET.selectAll();
                mInit3ET.requestFocus();
                return true;
            }
        });
        mInit4ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit4ET;
                int inType = mInit4ET.getInputType();
                mInit4ET.setInputType(InputType.TYPE_NULL);
                mInit4ET.onTouchEvent(event);
                mInit4ET.setInputType(inType);
                mInit4ET.selectAll();
                mInit4ET.requestFocus();
                return true;
            }
        });
        mInit5ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit5ET;
                int inType = mInit5ET.getInputType();
                mInit5ET.setInputType(InputType.TYPE_NULL);
                mInit5ET.onTouchEvent(event);
                mInit5ET.setInputType(inType);
                mInit5ET.selectAll();
                mInit5ET.requestFocus();
                return true;
            }
        });
        mInit6ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit6ET;
                int inType = mInit6ET.getInputType();
                mInit6ET.setInputType(InputType.TYPE_NULL);
                mInit6ET.onTouchEvent(event);
                mInit6ET.setInputType(inType);
                mInit6ET.selectAll();
                mInit6ET.requestFocus();
                return true;
            }
        });
        mInit7ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit7ET;
                int inType = mInit7ET.getInputType();
                mInit7ET.setInputType(InputType.TYPE_NULL);
                mInit7ET.onTouchEvent(event);
                mInit7ET.setInputType(inType);
                mInit7ET.selectAll();
                mInit7ET.requestFocus();
                return true;
            }
        });
        mInit8ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit8ET;
                int inType = mInit8ET.getInputType();
                mInit8ET.setInputType(InputType.TYPE_NULL);
                mInit8ET.onTouchEvent(event);
                mInit8ET.setInputType(inType);
                mInit8ET.selectAll();
                mInit8ET.requestFocus();
                return true;
            }
        });

        mInit9ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit9ET;
                int inType = mInit9ET.getInputType();
                mInit9ET.setInputType(InputType.TYPE_NULL);
                mInit9ET.onTouchEvent(event);
                mInit9ET.setInputType(inType);
                mInit9ET.selectAll();
                mInit9ET.requestFocus();
                return true;
            }
        });
        mInit10ET.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mInit10ET;
                int inType = mInit10ET.getInputType();
                mInit10ET.setInputType(InputType.TYPE_NULL);
                mInit10ET.onTouchEvent(event);
                mInit10ET.setInputType(inType);
                mInit10ET.selectAll();
                mInit10ET.requestFocus();
                return true;
            }
        });

        try {

            TextView initDesc1 = (TextView) findViewById(R.id.TextViewLable1);
            initDesc1.setText(targetPlan.getInit1Desc() + "");
            TextView initDesc2 = (TextView) findViewById(R.id.TextViewLable2);
            initDesc2.setText(targetPlan.getInit2Desc() + "");
            TextView initDesc3 = (TextView) findViewById(R.id.TextViewLable3);
            initDesc3.setText(targetPlan.getInit3Desc() + "");
            TextView initDesc4 = (TextView) findViewById(R.id.TextViewLable4);
            initDesc4.setText(targetPlan.getInit4Desc() + "");
            TextView initDesc5 = (TextView) findViewById(R.id.TextViewLable5);
            initDesc5.setText(targetPlan.getInit5Desc() + "");

            TextView initDesc6 = (TextView) findViewById(R.id.TextViewLable6);
            initDesc6.setText(targetPlan.getInit6Desc() + "");
            TextView initDesc7 = (TextView) findViewById(R.id.TextViewLable7);
            initDesc7.setText(targetPlan.getInit7Desc() + "");
            TextView initDesc8 = (TextView) findViewById(R.id.TextViewLable8);
            initDesc8.setText(targetPlan.getInit8Desc() + "");
            TextView initDesc9 = (TextView) findViewById(R.id.TextViewLable9);
            initDesc9.setText(targetPlan.getInit9Desc() + "");
            TextView initDesc10 = (TextView) findViewById(R.id.TextViewLable10);
            initDesc10.setText(targetPlan.getInit10Desc() + "");

            if (targetPlan.getInit1Desc() == null)
                mInit1layout.setVisibility(View.GONE);
            else if (targetPlan.getInit1Desc().equals("null"))
                mInit1layout.setVisibility(View.GONE);

            if (targetPlan.getInit2Desc() == null)
                mInit2layout.setVisibility(View.GONE);
            else if (targetPlan.getInit2Desc().equals("null"))
                mInit2layout.setVisibility(View.GONE);

            if (targetPlan.getInit3Desc() == null)
                mInit3layout.setVisibility(View.GONE);
            else if (targetPlan.getInit3Desc().equals("null"))
                mInit3layout.setVisibility(View.GONE);

            if (targetPlan.getInit4Desc() == null)
                mInit4layout.setVisibility(View.GONE);
            else if (targetPlan.getInit4Desc().equals("null"))
                mInit4layout.setVisibility(View.GONE);

            if (targetPlan.getInit5Desc() == null)
                mInit5layout.setVisibility(View.GONE);
            else if (targetPlan.getInit5Desc().equals("null"))
                mInit5layout.setVisibility(View.GONE);

            if (targetPlan.getInit6Desc() == null)
                mInit6layout.setVisibility(View.GONE);
            else if (targetPlan.getInit6Desc().equals("null"))
                mInit6layout.setVisibility(View.GONE);

            if (targetPlan.getInit7Desc() == null)
                mInit7layout.setVisibility(View.GONE);
            else if (targetPlan.getInit7Desc().equals("null"))
                mInit7layout.setVisibility(View.GONE);

            if (targetPlan.getInit8Desc() == null)
                mInit8layout.setVisibility(View.GONE);
            else if (targetPlan.getInit8Desc().equals("null"))
                mInit8layout.setVisibility(View.GONE);

            if (targetPlan.getInit9Desc() == null)
                mInit9layout.setVisibility(View.GONE);
            else if (targetPlan.getInit9Desc().equals("null"))
                mInit9layout.setVisibility(View.GONE);

            if (targetPlan.getInit10Desc() == null)
                mInit10layout.setVisibility(View.GONE);
            else if (targetPlan.getInit10Desc().equals("null"))
                mInit10layout.setVisibility(View.GONE);
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }

        // Disbale the editable mode if this screen is considered as review
        if (MENU_REV.equals(calledFrom)) {
            mBaseET.setEnabled(false);
            mSbdET.setEnabled(false);
            mInit1ET.setEnabled(false);
            mInit2ET.setEnabled(false);
            mInit3ET.setEnabled(false);
            mInit4ET.setEnabled(false);
            mInit5ET.setEnabled(false);
            mInit6ET.setEnabled(false);
            mInit7ET.setEnabled(false);
            mInit8ET.setEnabled(false);
            mInit9ET.setEnabled(false);
            mInit10ET.setEnabled(false);
        }

        mBase.setText(bmodel.formatValue(targetPlan.getBase()));
        mSbd.setText(bmodel.formatValue(targetPlan.getSbd()));

        mInit1.setText(bmodel.formatValue(targetPlan.getInitBalance1()));
        mInit2.setText(bmodel.formatValue(targetPlan.getInitBalance2()));
        mInit3.setText(bmodel.formatValue(targetPlan.getInitBalance3()));
        mInit4.setText(bmodel.formatValue(targetPlan.getInitBalance4()));
        mInit5.setText(bmodel.formatValue(targetPlan.getInitBalance5()));
        mInit6.setText(bmodel.formatValue(targetPlan.getInit6()));
        mInit7.setText(bmodel.formatValue(targetPlan.getInit7()));
        mInit8.setText(bmodel.formatValue(targetPlan.getInit8()));
        mInit9.setText(bmodel.formatValue(targetPlan.getInit9()));
        mInit10.setText(bmodel.formatValue(targetPlan.getInit10()));

        mBaseET.setText(SDUtil.format(targetPlan.getBaseEdit(), 2, 0));
        mSbdET.setText(SDUtil.format(targetPlan.getSbdEdit(), 2, 0));
        mInit1ET.setText(SDUtil.format(targetPlan.getInitBalnce1Edit(), 2, 0));
        mInit2ET.setText(SDUtil.format(targetPlan.getInitBalnce2Edit(), 2, 0));
        mInit3ET.setText(SDUtil.format(targetPlan.getInitBalnce3Edit(), 2, 0));
        mInit4ET.setText(SDUtil.format(targetPlan.getInitBalnce4Edit(), 2, 0));
        mInit5ET.setText(SDUtil.format(targetPlan.getInitBalnce5Edit(), 2, 0));
        mInit6ET.setText(SDUtil.format(targetPlan.getInit6_edt(), 2, 0));
        mInit7ET.setText(SDUtil.format(targetPlan.getInit7_edt(), 2, 0));
        mInit8ET.setText(SDUtil.format(targetPlan.getInit8_edt(), 2, 0));
        mInit9ET.setText(SDUtil.format(targetPlan.getInit9_edt(), 2, 0));
        mInit10ET.setText(SDUtil.format(targetPlan.getInit10_edt(), 2, 0));
        double mnth_acheive = (retailerObj.getMonthly_acheived());
        double day_acheive = 0;

        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            day_acheive = bmodel.getInvoiceAmount();
        } else {
            day_acheive = bmodel.getOrderValue();
        }

        double mnth_actual = day_acheive + mnth_acheive;
        mPlanTGT.setText(bmodel.formatValue(retailer
                .getDaily_target_planned_temp()) + "");
        mSuggestTGT.setText(bmodel.formatValue(targetPlan.getSuggestTraget())
                + "");
        mMonth_act_obj.setText(bmodel.formatValue(mnth_actual) + "");
        mStoreProg.setText(bmodel.formatValue(retailer.getSpTarget()) + "");
        mGoldenPointTXT.setText(retailer.getSbdDistributionAchieve() + "/"
                + retailer.getSbdDistributionTarget() + "");
        mHvp3mTXT.setText(bmodel.formatValue(targetPlan.getHvp3m()) + "");
        mTotalLinesTXT.setText(bmodel.getTotalLinesTarget() + "");
        mStoreBalanceTXT.setText(bmodel.formatValue(bmodel.getStoreBalance())
                + "");

    }

    public void setEditValue() {

        tot_value = 0;
        tot_value = base + sbd + init1 + init2 + init3 + init4 + init5 + init6
                + init7 + init8 + init9 + init10;
        mPlanTGT.setText(bmodel.formatValue(tot_value) + "");
        retailer.setDaily_target_planned_temp(tot_value);

    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.targetSave) {
            if (!bool) {
                bool = true;

                if (tot_value < Math.round(targetPlan.getSuggestTraget())) {
                    showAlertOkCancel(
                            getResources()
                                    .getString(
                                            R.string.continue_plan_tgt_lessthan_suggest_tgt),
                            0);
                } else {
                    retailer.setDaily_target_planned(tot_value);
                    new SavePlanning().execute();
                }
            }

        } else if (i1 == R.id.targetBack) {
            if (calledFrom.equals(MENU_REV)) {

                bmodel.setIsReviewPlan("Y");

                bmodel.getRetailerMasterBO().setIsReviewPlan("Y");

                Intent i = new Intent(TargetPlanActivity.this,
                        HomeScreenTwo.class);
                startActivity(i);

            }

            finish();

        } else if (i1 == R.id.targetRemainder) {
            if (calledFrom.equals(MENU_REV)) {

                Intent intent = new Intent(getApplicationContext(), Task.class);
                intent.putExtra("IsRetailerwisetask", true);
                intent.putExtra("fromReviewScreen", true);
                startActivity(intent);

            }
        }

    }

    public void showAlertOkCancel(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                TargetPlanActivity.this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if (idd == 0) {
                            retailer.setDaily_target_planned(tot_value);
                            new SavePlanning().execute();
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        bool = false;
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                long s = SDUtil.convertToLong(QUANTITY.getText()
                        .toString());
                if (s == -1) {
                    String subStr = QUANTITY
                            .getText()
                            .toString()
                            .substring(
                                    0,
                                    (QUANTITY.getText().toString().length() - 1));
                    if ((subStr.charAt(subStr.length() - 1)) == '.')
                        subStr = subStr.substring(0, (subStr.length() - 1));
                    if (subStr.equals("-"))
                        subStr = "0";
                    if (subStr.equals("."))
                        subStr = "0";
                    if (subStr.equals(""))
                        subStr = "0";
                    QUANTITY.setText(subStr);
                } else {
                    s = s / 10;
                    QUANTITY.setText(s + "");
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }

    }

    class SavePlanning extends AsyncTask<Integer, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.targetPlanHelper.saveDailyTrgetPalnned(retailer
                        .getRetailerID());
                bmodel.targetPlanHelper.updateDailyTargetPlan();
                bmodel.targetPlanHelper.saveDailyTrgetPalnned(bmodel
                        .getRetailerMasterBO().getRetailerID());
                bmodel.targetPlanHelper.updateTargetPlanEdit(bmodel
                        .getRetailerMasterBO().getRetailerID());
                bmodel.setIsPlanned();
                bmodel.setIsPlannedInDB();
                bmodel.getRetailerMasterBO().setIsPlanned("Y");
                if (bmodel.configurationMasterHelper.IS_DB_BACKUP) {
                    boolean bool = bmodel.synchronizationHelper.backUpDB();
                }
            } catch (Exception e) {
                Commons.printException(e);
                bool = false;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
           /* progressDialogue = ProgressDialog.show(TargetPlanActivity.this,
                    DataMembers.SD,
                    getResources().getString(R.string.saving_target_plan),
                    true, false);*/
            builder = new AlertDialog.Builder(TargetPlanActivity.this);

            customProgressDialog(builder, TargetPlanActivity.this, getResources().getString(R.string.saving_target_plan));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            bool = false;
            //   progressDialogue.dismiss();

            alertDialog.dismiss();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_target_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (MENU_REV.equals(calledFrom)) {
            menu.findItem(R.id.menu_save).setVisible(false);
        } else {
            menu.findItem(R.id.menu_save).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (calledFrom.equals(MENU_REV)) {
                bmodel.setIsReviewPlan("Y");
                bmodel.getRetailerMasterBO().setIsReviewPlan("Y");
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                Intent i = new Intent(TargetPlanActivity.this,
                        HomeScreenTwo.class);
                startActivity(i);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        } else if (i1 == R.id.menu_save) {
            if (!bool) {
                bool = true;

                if (tot_value < Math.round(targetPlan.getSuggestTraget())) {
                    showAlertOkCancel(
                            getResources()
                                    .getString(
                                            R.string.continue_plan_tgt_lessthan_suggest_tgt),
                            0);
                } else {
                    retailer.setDaily_target_planned(tot_value);
                    new SavePlanning().execute();
                }
            }
            return true;
        } else if (i1 == R.id.menu_skutgt) {
            Intent i = new Intent(TargetPlanActivity.this,
                    SKUWiseTargetActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("screentitle", bmodel.getMenuName("MENU_SKUWISERTGT"));
            i.putExtra("from", "3");
            i.putExtra("rid", "" + bmodel.retailerMasterBO.getRetailerID());
            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * this will clear all the resources used of the layout.
     *
     * @param view
     */
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

}
