package com.ivy.sd.png.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

/**
 * Created by subramanian.r on 19-11-2015.
 */
public class UserFeedbackActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private BusinessModel bmodel;
    private ArrayList<ReasonMaster> reason;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Spinner mFeedBackSpinner;
    private EditText mFeedBackText;
    private Button mSubmit;
    private String mSelectedTypeId = "-1";
    private int mRank = 0;
    private ImageView mImgV1, mImgV2, mImgV3, mImgV4, mImgV5;
    private LinearLayout mRankingLayout;
    private ArrayList<ImageView> mRanks;
    private int mRankSize;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_feedback);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.feedback));
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        bmodel.mUserFeedBackHelper.downloadFeedBackType();

        reason = bmodel.mUserFeedBackHelper.getFeedBackType();

        spinnerAdapter = new ArrayAdapter<ReasonMaster>(this,
                android.R.layout.simple_spinner_item, reason);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRankingLayout = (LinearLayout) findViewById(R.id.ranking_layout);

        mFeedBackSpinner = (Spinner) findViewById(R.id.feedback_type);
        mFeedBackSpinner.setAdapter(spinnerAdapter);

        mFeedBackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ReasonMaster reason = (ReasonMaster) parent.getItemAtPosition(pos);
                if (mSelectedTypeId != reason.getReasonID()) {

                    mSelectedTypeId = reason.getReasonID();

                    if (reason.getReasonCategory().equalsIgnoreCase("REV"))
                        mRankingLayout.setVisibility(View.VISIBLE);
                    else
                        mRankingLayout.setVisibility(View.GONE);

                    resetAllValues(false);

                }


            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });


        mFeedBackText = (EditText) findViewById(R.id.feedback_txt);
        mSubmit = (Button) findViewById(R.id.btn_submit);
        mSubmit.setOnClickListener(UserFeedbackActivity.this);

        mImgV1 = (ImageView) findViewById(R.id.iv1);
        mImgV2 = (ImageView) findViewById(R.id.iv2);
        mImgV3 = (ImageView) findViewById(R.id.iv3);
        mImgV4 = (ImageView) findViewById(R.id.iv4);
        mImgV5 = (ImageView) findViewById(R.id.iv5);

        mRanks = new ArrayList<ImageView>();
        mRanks.add(mImgV1);
        mRanks.add(mImgV2);
        mRanks.add(mImgV3);
        mRanks.add(mImgV4);
        mRanks.add(mImgV5);
        mRankSize = mRanks.size();

       /* mImgV1.setOnClickListener(UserFeedbackActivity.this);
        mImgV2.setOnClickListener(UserFeedbackActivity.this);
        mImgV3.setOnClickListener(UserFeedbackActivity.this);
        mImgV4.setOnClickListener(UserFeedbackActivity.this);
        mImgV5.setOnClickListener(UserFeedbackActivity.this);*/

        for (ImageView iv : mRanks) {
            iv.setOnClickListener(UserFeedbackActivity.this);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            String txtFeedBack = mFeedBackText.getText().toString().trim();
            if (mSelectedTypeId.equals("-1")
                    || txtFeedBack.equalsIgnoreCase("")) {
                if (mSelectedTypeId.equals("-1"))
                    ((TextView) mFeedBackSpinner.getSelectedView()).setError("");
                if (txtFeedBack.equalsIgnoreCase(""))
                    mFeedBackText.setError("This field can not be blank");
            } else {
                bmodel.mUserFeedBackHelper.saveFeedBack(mSelectedTypeId, txtFeedBack, mRank);
                resetAllValues(true);

            }
        } else {
            for (int k = 0; k < mRankSize; k++) {
                if (v.getId() == mRanks.get(k).getId()) {
                    mRank = k + 1;
                    break;
                }
            }

            for (int i = 0; i < mRank; i++) {
                mRanks.get(i).setBackgroundResource(R.drawable.ic_action_star_select);
            }
            for (int j = mRank; j < mRankSize; j++) {
                mRanks.get(j).setBackgroundResource(R.drawable.ic_icon_outlet_gold_left_new);
            }
        }

    }

    private void resetAllValues(boolean isResetSpinner) {
        if (isResetSpinner)
            mFeedBackSpinner.setSelection(0);

        mFeedBackText.setText("");

        mRank = 0;
        for (int j = 0; j < mRankSize; j++) {
            mRanks.get(j).setBackgroundResource(R.drawable.ic_icon_outlet_gold_left_new);
        }
    }


}
