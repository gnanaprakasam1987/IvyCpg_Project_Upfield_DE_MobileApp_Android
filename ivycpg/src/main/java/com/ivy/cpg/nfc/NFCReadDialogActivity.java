package com.ivy.cpg.nfc;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

/**
 * Created by subramanian on 4/22/16.
 */
public class NFCReadDialogActivity extends IvyBaseActivityNoActionBar {

    NFCManager nfcManager;
    private String mNFCValue;
    private Spinner mNFCReason;
    private TextView mSpinnerLine;

    private TextView mTextView;
    private Button mSubmitButton;
    private ArrayAdapter<ReasonMaster> reasonAdapter;
    private String mSelectedReasonId = "0";
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.activity_nfc_read);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 600;
        params.width = 450;

        this.getWindow().setAttributes(params);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);


        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                mNFCValue = extras.getString("nfcvalue");
            }
        }

        nfcManager = new NFCManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                if(tagRead.equals(mNFCValue)){
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("VisitMode", "NFC");
                    resultIntent.putExtra("NFCReasonId", "0");
                    setResult(NFCManager.NFC_CODE_MATCHED,resultIntent);
                    finish();
                } else {
                    Toast.makeText(NFCReadDialogActivity.this, R.string.nfc_value_mismatched, Toast.LENGTH_LONG).show();
                }
            }
        });

        mTextView = (TextView) findViewById(R.id.hyperlink);
        mNFCReason = (Spinner) findViewById(R.id.nfcspinner);
        mSubmitButton = (Button) findViewById(R.id.btn_submit);
        mSpinnerLine = (TextView) findViewById(R.id.spinnerline);

        TextView title = (TextView) findViewById(R.id.title);
        TextView subtitle = (TextView) findViewById(R.id.subtitle);

        title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mSpinnerLine.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        subtitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mTextView.setText(Html.fromHtml("<a href=> Issue with NFC?"));
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNFCReason.setVisibility(View.VISIBLE);
                mSubmitButton.setVisibility(View.VISIBLE);
                mSpinnerLine.setVisibility(View.VISIBLE);
            }
        });

        reasonAdapter = new ArrayAdapter<>(NFCReadDialogActivity.this,
                android.R.layout.simple_spinner_item);

        loadReason();

        reasonAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNFCReason.setAdapter(reasonAdapter);

        mNFCReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0,
                                               View arg1, int arg2, long arg3) {
                        ReasonMaster reasonBO = (ReasonMaster) mNFCReason
                                .getSelectedItem();
                        mSelectedReasonId = reasonBO.getReasonID();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }
                });

    }

    public void loadReason() {
        try {
            ReasonMaster reason;

            reason = new ReasonMaster("0", NFCReadDialogActivity.this.getResources()
                    .getString(R.string.select_reason));
            reasonAdapter.add(reason);

            DBUtil db = new DBUtil(NFCReadDialogActivity.this, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster("NFCRETAILER"));

            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster(c.getString(0), c.getString(1));
                    reasonAdapter.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.print(""+e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent){
        nfcManager.onActivityNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
    }

    public void onClickCancel(View v){
        finish();
    }

    public void onClickSubmit(View v){
        if(!mSelectedReasonId.equalsIgnoreCase("0")) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NFCReasonId", mSelectedReasonId);
            resultIntent.putExtra("VisitMode", "NFC");
            setResult(NFCManager.NFC_CODE_SELECTING_REASON, resultIntent);
            finish();
        } else {
            Toast.makeText(NFCReadDialogActivity.this, "Select Reason", Toast.LENGTH_LONG).show();
        }
    }
}
