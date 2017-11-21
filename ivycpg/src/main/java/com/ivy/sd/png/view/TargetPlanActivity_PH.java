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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SBDMerchandisingBO;
import com.ivy.sd.png.bo.TargetPlanBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class TargetPlanActivity_PH extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private BusinessModel bmodel;
    private TextView mSbd;
    private EditText mBaseET, mSbdET;
    private TextView mdistribution, mMerchTXT, mMerchPricingTXT,
            mInitiativeTXT, mGoldenPointTXT, mStoreBalanceTXT, mTotalLinesTXT,
            mHvp3mTXT;
    private String append = "";
    private TargetPlanBO targetPlan;
    private RetailerMasterBO retailer;
    private EditText QUANTITY;
    private double tot_value, base, sbd, stgt;
    private String calledFrom;
    private final String MENU_REV = "Review";
    private RetailerMasterBO retailerObj;
    LinearLayout mLayout_golden_points, mLayout_total_lines, mLayout_store_balance, mLayout_highest_ever_sale;

    boolean bool = false;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_target_plan_ph);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

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

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSbd = (TextView) findViewById(R.id.ref_sbd);
        mdistribution = (TextView) findViewById(R.id.distribution);
        mMerchTXT = (TextView) findViewById(R.id.merchList);
        mMerchPricingTXT = (TextView) findViewById(R.id.merchPricing);
        mInitiativeTXT = (TextView) findViewById(R.id.initiative);
        mGoldenPointTXT = (TextView) findViewById(R.id.tv_goldenPoints);
        mHvp3mTXT = (TextView) findViewById(R.id.tv_hvp3m);
        mStoreBalanceTXT = (TextView) findViewById(R.id.tv_store_balance);
        mTotalLinesTXT = (TextView) findViewById(R.id.tv_lines);
        mBaseET = (EditText) findViewById(R.id.edt_base);
        mSbdET = (EditText) findViewById(R.id.edt_sbd);
        mLayout_golden_points = (LinearLayout) findViewById(R.id.layout_golden_points);
        mLayout_total_lines = (LinearLayout) findViewById(R.id.layout_total_lines);
        mLayout_store_balance = (LinearLayout) findViewById(R.id.layout_store_balance);
        mLayout_highest_ever_sale = (LinearLayout) findViewById(R.id.layout_highest_ever_sale);

        if (bmodel.configurationMasterHelper.HIDE_REVIEWPLAN_FIELD) {
            mLayout_golden_points.setVisibility(View.GONE);
            mLayout_total_lines.setVisibility(View.GONE);
            mLayout_store_balance.setVisibility(View.GONE);
            mLayout_highest_ever_sale.setVisibility(View.GONE);
        }

        // set visibility to dot button in keypad
        findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        LinearLayout keypad = (LinearLayout) findViewById(R.id.layout_keypad);
        ((Button) findViewById(R.id.targetBack)).setOnClickListener(this);
        ((Button) findViewById(R.id.targetBack)).setVisibility(View.GONE);
        Button save = ((Button) findViewById(R.id.targetSave));
        Button remain = (Button) findViewById(R.id.targetRemainder);
        save.setOnClickListener(this);
        remain.setOnClickListener(this);

        calledFrom = getIntent().getStringExtra("From");
        if (calledFrom == null)
            calledFrom = MENU_REV;

        setSupportActionBar(toolbar);
        // Set title to actionbar
        getSupportActionBar().setTitle(
                null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setScreenTitle(getResources().getString(R.string.title_dsr_heading));

        if (MENU_REV.equals(calledFrom)) {
            save.setVisibility(View.GONE);
            remain.setVisibility(View.GONE);
            mBaseET.setBackgroundResource(android.R.color.white);
            mBaseET.setInputType(InputType.TYPE_NULL);
            mSbdET.setBackgroundResource(android.R.color.white);
            keypad.setVisibility(View.GONE);
        }
        mBaseET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

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
                    base = SDUtil.convertToDouble(mBaseET.getText().toString());
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

        // Disbale the editable mode if this screen is considered as review
        if (MENU_REV.equals(calledFrom)) {
            mBaseET.setEnabled(false);
            mSbdET.setEnabled(false);
        }

        mSbd.setText(bmodel.formatValue(targetPlan.getSbd()) + "");

        mBaseET.setText(SDUtil.format(targetPlan.getBaseEdit(), 2, 0) + "");
        mSbdET.setText(SDUtil.format(targetPlan.getSbdEdit(), 2, 0) + "");

        double mnth_acheive = (retailerObj.getMonthly_acheived());
        double day_acheive = 0;

        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            day_acheive = bmodel.getInvoiceAmount();
        } else {
            day_acheive = bmodel.getOrderValue();
        }

        double mnth_actual = day_acheive + mnth_acheive;

        // distribution
        // int distribution = 0;
        // for (ProductMasterBO product :
        // bmodel.productHelper.getProductMaster()) {
        //
        // if (product.isRPS()) {// || product.getIsInitiativeProduct() == 1) {
        // distribution = distribution + 1;
        // }
        // }
        mdistribution.setText(bmodel.retailerMasterBO
                .getSbdDistributionTarget() + "");

        // Merchandising
        StringBuffer merchList = new StringBuffer();
        Vector<SBDMerchandisingBO> msbdMerchandisingVector = bmodel.sbdMerchandisingHelper
                .downloadSBDMerchandising("MERCH");
        for (SBDMerchandisingBO sbdMerchandisingBO : msbdMerchandisingVector) {
            merchList.append(sbdMerchandisingBO.getValueText());
            merchList.append("\n");
        }
        mMerchTXT.setText(merchList);

        Vector<SBDMerchandisingBO> MerchPricingVector = bmodel.sbdMerchandisingHelper
                .downloadSBDMerchandising("MERCH_INIT");
        StringBuffer merchpricing = new StringBuffer();
        for (SBDMerchandisingBO sbdMerchandisingBO : MerchPricingVector) {
            merchpricing.append(sbdMerchandisingBO.getValueText());
            merchpricing.append("\n");
        }
        mMerchPricingTXT.setText(merchpricing);
        StringBuffer initiative = new StringBuffer();
        if (targetPlan.getInit1Desc() != null) {
            initiative.append(targetPlan.getInit1Desc()).append("\n");
        }
        if (targetPlan.getInit2Desc() != null) {
            initiative.append(targetPlan.getInit2Desc()).append("\n");
        }
        if (targetPlan.getInit3Desc() != null) {
            initiative.append(targetPlan.getInit3Desc()).append("\n");
        }
        if (targetPlan.getInit4Desc() != null) {
            initiative.append(targetPlan.getInit4Desc()).append("\n");
        }
        if (targetPlan.getInit5Desc() != null) {
            initiative.append(targetPlan.getInit5Desc()).append("\n");
        }
        if (targetPlan.getInit6Desc() != null) {
            initiative.append(targetPlan.getInit6Desc()).append("\n");
        }
        if (targetPlan.getInit7Desc() != null) {
            initiative.append(targetPlan.getInit7Desc()).append("\n");
        }
        if (targetPlan.getInit8Desc() != null) {
            initiative.append(targetPlan.getInit8Desc()).append("\n");
        }
        if (targetPlan.getInit9Desc() != null) {
            initiative.append(targetPlan.getInit9Desc()).append("\n");
        }
        if (targetPlan.getInit10Desc() != null) {
            initiative.append(targetPlan.getInit10Desc()).append("\n");
        }
        if (initiative.length() == 0) {
            initiative.append("-");
        }

        mInitiativeTXT.setText(initiative);
        mHvp3mTXT.setText(bmodel.formatValue(targetPlan.getHvp3m()) + "");
        mGoldenPointTXT.setText(retailer.getSbdDistributionAchieve() + "/"
                + retailer.getSbdDistributionTarget() + "");
        mTotalLinesTXT.setText(bmodel.getTotalLinesTarget() + "");
        mStoreBalanceTXT.setText(bmodel.formatValue(bmodel.getStoreBalance())
                + "");
    }

    public void setEditValue() {

        tot_value = 0;
        tot_value = base;
        retailer.setDaily_target_planned_temp(tot_value);

    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.targetSave) {
            if (!bool) {
                bool = true;

                // if (tot_value < Math.round(targetPlan.getSuggestTraget())) {
                // showAlertOkCancel(
                // getResources()
                // .getString(
                // R.string.continue_plan_tgt_lessthan_suggest_tgt),
                // 0);
                // } else {
                retailer.setDaily_target_planned(tot_value);
                new SavePlanning().execute();
                // }
            }

        } else if (i1 == R.id.targetBack) {
            if (calledFrom.equals(MENU_REV)) {

                bmodel.setIsReviewPlan("Y");

                bmodel.getRetailerMasterBO().setIsReviewPlan("Y");
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

                Intent i = new Intent(TargetPlanActivity_PH.this,
                        HomeScreenTwo.class);
                startActivity(i);
            }

            finish();

        } else if (i1 == R.id.targetRemainder) {
            if (calledFrom.equals(MENU_REV)) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
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
                TargetPlanActivity_PH.this);
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
         /*   progressDialogue = ProgressDialog.show(TargetPlanActivity_PH.this,
                    DataMembers.SD,
                    getResources().getString(R.string.saving_target_plan),
                    true, false);*/
            builder = new AlertDialog.Builder(TargetPlanActivity_PH.this);

            customProgressDialog(builder,  getResources().getString(R.string.saving_target_plan));
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

    /**
     * this will clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
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
                Intent i = new Intent(TargetPlanActivity_PH.this,
                        HomeScreenTwo.class);
                startActivity(i);
            }
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
        }
        return super.onOptionsItemSelected(item);
    }

}
