package com.ivy.countersales;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

/**
 * Created by rajkumar.s on 15-03-2016.
 */
public class CSConsultingActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {
    MaterialSpinner spn_concern;
    ArrayAdapter<AttributeBO> spinnerAdapter;
    BusinessModel bmodel;
    EditText edt_resolution, edt_feedback;
    int mselectedAttributeId = 0;
    private Toolbar toolbar;
    private Button cancelBtn, applyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        setContentView(R.layout.consulting_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);


        spn_concern = (MaterialSpinner) findViewById(R.id.spn_concern);
        spn_concern.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        edt_resolution = (EditText) findViewById(R.id.edt_resolution);
        edt_feedback = (EditText) findViewById(R.id.edt_feedback);

        edt_feedback.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_resolution.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        spinnerAdapter = new ArrayAdapter<AttributeBO>(
                CSConsultingActivity.this, android.R.layout.simple_spinner_item, bmodel.mCounterSalesHelper.getLstConcern());
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_concern.setAdapter(spinnerAdapter);
        spn_concern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    mselectedAttributeId = bmodel.mCounterSalesHelper.getLstConcern().get(position).getAttributeId();
                    isEditable(true);
                } else {
                    isEditable(false);
                    mselectedAttributeId = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (bmodel.getCounterSaleBO() != null) {
            edt_resolution.setText(bmodel.getCounterSaleBO().getResolution());
            edt_feedback.setText(bmodel.getCounterSaleBO().getConnsultingFeedback());
            mselectedAttributeId = bmodel.getCounterSaleBO().getAttributeId();
            if (mselectedAttributeId > 0)
                spn_concern.setSelection(bmodel.mCounterSalesHelper.getAttributeItemIndex(mselectedAttributeId, bmodel.mCounterSalesHelper.getLstConcern()));

        }

        cancelBtn = (Button) findViewById(R.id.cancelbtn);
        cancelBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        applyBtn = (Button) findViewById(R.id.applybtn);
        applyBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelBtn.setOnClickListener(this);
        applyBtn.setOnClickListener(this);

        ((TextView) findViewById(R.id.tv_concern)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.txt_resolution)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.txt_feedback)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
    }

    private void isEditable(boolean flag) {
        edt_resolution.setEnabled(flag);
        edt_feedback.setEnabled(flag);
        edt_feedback.setFocusable(flag);
        edt_resolution.setFocusable(flag);
        edt_resolution.setFocusableInTouchMode(flag);
        edt_feedback.setFocusableInTouchMode(flag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_counter_sales, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //setValues();
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    private void setValues() {
        String resolution = edt_resolution.getText().toString();

        String feedback = edt_feedback.getText().toString();
        if (mselectedAttributeId > 0 || !resolution.equals("") || !feedback.equals("") || !feedback.equals("")) {
            if (bmodel.getCounterSaleBO() != null) {
                bmodel.getCounterSaleBO().setResolution(resolution);
                bmodel.getCounterSaleBO().setConnsultingFeedback(feedback);
                bmodel.getCounterSaleBO().setAttributeId(mselectedAttributeId);

            } else {
                CounterSaleBO csBo = new CounterSaleBO();
                csBo.setResolution(resolution);
                csBo.setConnsultingFeedback(feedback);
                csBo.setAttributeId(mselectedAttributeId);
                bmodel.setCounterSaleBO(csBo);
            }
        } else {
            if (bmodel.getCounterSaleBO() != null) {
                bmodel.getCounterSaleBO().setResolution("");
                bmodel.getCounterSaleBO().setConnsultingFeedback("");
                bmodel.getCounterSaleBO().setAttributeId(0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Button id = (Button) v;
        if (id == applyBtn) {
            setValues();
            finish();
        } else if (id == cancelBtn) {
            finish();
        }
    }

}
