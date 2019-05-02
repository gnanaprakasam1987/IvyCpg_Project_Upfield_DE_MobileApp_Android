package com.ivy.cpg.view.sf;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

/**
 * Created by rajkumar.s on 4/25/2017.
 * SOS summary screen project specific
 */

public class SOSSummaryActivity_PRJSpecific extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    BusinessModel mBModel;
    LinearLayout ll_content;

    Toolbar toolbar;
    Button btnSave;
    TextView lbl_group, lbl_target, lbl_avail, tv_lbl_gap;

    SalesFundamentalHelper mSFHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sos_summary);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            setScreenTitle(getResources().getString(R.string.sos_summary));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        lbl_group = (TextView) findViewById(R.id.tv_group_lbl);
        lbl_group.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_target = (TextView) findViewById(R.id.tv_target_lbl);
        lbl_target.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_avail = (TextView) findViewById(R.id.tv_available_lbl);
        lbl_avail.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        tv_lbl_gap = (TextView) findViewById(R.id.tv_gap_lbl);
        tv_lbl_gap.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);


        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        loadSummaryView();

        mSFHelper = SalesFundamentalHelper.getInstance(this);
    }

    private void loadSummaryView() {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);

            View view;
            int inTargetTotal = 0, total = 0;
            double target;
            String groupName;
            for (int i = 0; i < mSFHelper.getLstSOS_PRJSpecific().size(); i++) {
                SOSBO bo = mSFHelper.getLstSOS_PRJSpecific().get(i);

                if (bo.getInTarget() == 1) {
                    inTargetTotal += bo.getAvailability();
                }
                total += bo.getAvailability();

                groupName = bo.getGroupName();
                target = bo.getGroupTarget();

                // Current list has data as groupWise product. So to show view group wise, this loop is used
                if (mSFHelper.getLstSOS_PRJSpecific().size() == (i + 1) ||
                        (mSFHelper.getLstSOS_PRJSpecific().size() > i + 1 && bo.getGroupId()
                                != mSFHelper.getLstSOS_PRJSpecific().get(i + 1).getGroupId())) {

                    if (inTargetTotal > 0 || total > 0) {// if availability>0 for any one of the product in the group

                        view = inflater.inflate(R.layout.layout_sos_summary_list_item_proj, null);
                        TextView tv_groupName = (TextView) view.findViewById(R.id.tv_group_name);
                        tv_groupName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        TextView tv_target = (TextView) view.findViewById(R.id.tv_target);
                        tv_target.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        TextView tv_available = (TextView) view.findViewById(R.id.tv_available);
                        tv_available.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        TextView tv_gap = (TextView) view.findViewById(R.id.tv_gap);
                        tv_gap.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                        tv_groupName.setText(groupName);
                        tv_target.setText(SDUtil.roundIt(target, 2) + "");
                        double achieved = 0;
                        if (total > 0)
                            achieved = (inTargetTotal / total) * 100;

                        tv_available.setText(SDUtil.roundIt(achieved, 2) + "");

                        double gap = target - achieved;
                        if (gap > 0) {
                            tv_gap.setTextColor(Color.RED);
                        } else {
                            tv_gap.setTextColor(Color.GREEN);
                        }

                        tv_gap.setText(SDUtil.roundIt(gap, 2) + "");

                        ll_content.addView(view);
                    }

                    inTargetTotal = 0;
                    total = 0;

                }


            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_save) {

            new SaveAsyncTask().execute();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SaveAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                mSFHelper.saveSOS_PRJSpecific_Transaction();
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SOS_PROJ, true);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(SOSSummaryActivity_PRJSpecific.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {

                Toast.makeText(SOSSummaryActivity_PRJSpecific.this,
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();

                mSFHelper.setLstSOS_PRJSpecific(null);
                startActivity(new Intent(SOSSummaryActivity_PRJSpecific.this, HomeScreenTwo.class));
                finish();

            }
        }
    }
}

