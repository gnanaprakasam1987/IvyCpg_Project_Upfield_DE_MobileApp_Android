package com.ivy.cpg.view.homescreen.userFeedback;


import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
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

import java.util.ArrayList;

/**
 * Created by subramanian.r on 19-11-2015.
 */
public class UserFeedbackActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private Spinner mFeedBackSpinner;
    private EditText mFeedBackText;
    private String mSelectedTypeId = "-1";
    private int mRank = 0;
    private LinearLayout mRankingLayout;
    private ArrayList<ImageView> mRanks;
    private int mRankSize;
    private UserFeedBackHelper userFeedBackHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.feedback));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        userFeedBackHelper = new UserFeedBackHelper(getApplicationContext());
        userFeedBackHelper.downloadFeedBackType();

        ArrayList<ReasonMaster> reason = userFeedBackHelper.getFeedBackType();

        ArrayAdapter<ReasonMaster> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, reason);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRankingLayout = findViewById(R.id.ranking_layout);
        mFeedBackSpinner = findViewById(R.id.feedback_type);
        mFeedBackSpinner.setAdapter(spinnerAdapter);

        mFeedBackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ReasonMaster reason = (ReasonMaster) parent.getItemAtPosition(pos);
                if (!mSelectedTypeId.equals(reason.getReasonID())) {

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


        mFeedBackText = findViewById(R.id.feedback_txt);
        Button mSubmit = findViewById(R.id.btn_submit);
        mSubmit.setOnClickListener(UserFeedbackActivity.this);

        ImageView mImgV1 = findViewById(R.id.iv1);
        ImageView mImgV2 = findViewById(R.id.iv2);
        ImageView mImgV3 = findViewById(R.id.iv3);
        ImageView mImgV4 = findViewById(R.id.iv4);
        ImageView mImgV5 = findViewById(R.id.iv5);

        mRanks = new ArrayList<>();
        mRanks.add(mImgV1);
        mRanks.add(mImgV2);
        mRanks.add(mImgV3);
        mRanks.add(mImgV4);
        mRanks.add(mImgV5);
        mRankSize = mRanks.size();

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
                userFeedBackHelper.saveFeedBack(mSelectedTypeId, txtFeedBack, mRank);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userFeedBackHelper = null;
    }
}
