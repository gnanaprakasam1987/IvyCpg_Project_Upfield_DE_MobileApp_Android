package com.ivy.cpg.view.callanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

public class CloseCallActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    Spinner reason_desc;
    CheckBox checkbox_yes, checkbox_no;
    EditText otp;
    Button otpSubmit, close;
    ArrayAdapter<ReasonMaster> clcrReasonAdapter;
    TextView timespent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closecall);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        reason_desc =  findViewById(R.id.reason_desc);
        checkbox_yes =  findViewById(R.id.checkbox_yes);
        checkbox_no =  findViewById(R.id.checkbox_no);
        checkbox_no.setChecked(true);
        otp =  findViewById(R.id.otp);
        otp.setEnabled(false);

        otpSubmit =  findViewById(R.id.otpSubmit);
        otpSubmit.setEnabled(false);


        close =  findViewById(R.id.closecall);
        timespent =  findViewById(R.id.timespent);

        //clcrReasonList = new Vector<ReasonMaster>();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.drawable.icon_competitor);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            String title;
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_CLOSE_CALL");
            getSupportActionBar().setTitle(title);
        }

        /* set handler for the Timer class */
        if (bmodel.timer != null) {
            bmodel.timer.setHandler(handler);
        }
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        checkbox_yes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otp.setText("");
                    otp.setEnabled(true);
                    otpSubmit.setEnabled(true);
                    checkbox_no.setChecked(false);
                    checkbox_yes.setChecked(true);
                } else {
                    otp.setEnabled(false);
                    otpSubmit.setEnabled(false);
                    checkbox_no.setChecked(true);
                    checkbox_yes.setChecked(false);
                }

            }
        });

        checkbox_no.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    otp.setText("");
                    otp.setEnabled(false);
                    otpSubmit.setEnabled(false);
                    checkbox_yes.setChecked(false);
                    checkbox_no.setChecked(true);
                } else {
                    otp.setEnabled(true);
                    otpSubmit.setEnabled(true);
                    checkbox_yes.setChecked(true);
                    checkbox_no.setChecked(false);
                }

            }
        });


        clcrReasonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        clcrReasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clcrReasonAdapter.add(new ReasonMaster(0 + "", getResources().getString(R.string.select)));
        for (int i = 0; i < bmodel.reasonHelper.getClosecallReasonList().size(); i++)
            clcrReasonAdapter.add(bmodel.reasonHelper.getClosecallReasonList().get(i));

        reason_desc.setAdapter(clcrReasonAdapter);
        reason_desc.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkbox_yes.isChecked()) {
                    if (bmodel.closecallhelper.isAllowOtp()) {
                        doCloseCall();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_otp), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    doCloseCall();
                }
            }
        });


        otpSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (otp.isEnabled()) {
                    if (bmodel.closecallhelper.isValidOtp(otp.getText().toString())) {
                        bmodel.closecallhelper.updateOtp();
                        otp.setText("");
                        Toast.makeText(getApplicationContext(), "Verified successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_otp), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void doCloseCall() {

        if (bmodel.timer != null) {
            bmodel.timer.stopTimer();
            bmodel.timer = null;
        }
        if (checkActivityCompletion()) {
            if (reason_desc.getSelectedItemId() != 0) {

                save();

                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                        .now(DateTimeUtils.TIME), "");
                bmodel.productHelper.clearProductHelper();

                finish();
            } else {
                bmodel.showAlert("Choose type of visit.", 0);
            }

        } else {
            bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
            bmodel.outletTimeStampHelper.deleteTimeStamp();

            finish();
        }
    }



    private boolean checkActivityCompletion() {

        boolean activtyDone = false;

        for (ConfigureBO config : bmodel.configurationMasterHelper.getActivityMenu()) {
            if (config.getHasLink() == 1 && config.isDone()
                    && !config.getConfigCode().equals("MENU_CLOSE_CALL")) {

                activtyDone = true;
            }

        }

        return activtyDone;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Handler getHandler() {
        return handler;
    }



    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            bmodel = (BusinessModel) getApplicationContext();
            timespent.setText(msg.obj.toString());
            return true;
        }
    });

    public void save() {
        ReasonMaster temp = (ReasonMaster) reason_desc.getSelectedItem();
        NonproductivereasonBO nonproductive = new NonproductivereasonBO();
        nonproductive.setReasonid(temp.getReasonID());
        nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate());
        nonproductive.setReasontype("CLCR");

        bmodel.closecallhelper.saveCloseCallreason(nonproductive);
        bmodel.updateIsVisitedFlag("Y");
        Toast.makeText(CloseCallActivity.this,
                getResources().getString(R.string.reason_saved),
                Toast.LENGTH_SHORT).show();
    }

    public void onBack() {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME));

        Intent myIntent = new Intent(this, HomeScreenTwo.class);
        startActivityForResult(myIntent, 0);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        finish();
    }

    public void onBackPressed() {
    }
}
